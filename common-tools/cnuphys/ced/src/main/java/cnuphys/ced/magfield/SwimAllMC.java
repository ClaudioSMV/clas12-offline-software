package cnuphys.ced.magfield;

import java.util.Vector;

import org.jlab.io.base.DataEvent;

import cnuphys.adaptiveSwim.AdaptiveSwimException;
import cnuphys.adaptiveSwim.AdaptiveSwimResult;
import cnuphys.adaptiveSwim.AdaptiveSwimmer;
import cnuphys.bCNU.log.Log;
import cnuphys.bCNU.magneticfield.swim.ISwimAll;
import cnuphys.ced.alldata.DataManager;
import cnuphys.ced.clasio.ClasIoEventManager;
import cnuphys.lund.LundId;
import cnuphys.lund.LundSupport;
import cnuphys.lund.TrajectoryRowData;
import cnuphys.rk4.RungeKuttaException;
import cnuphys.swim.DefaultSwimStopper;
import cnuphys.swim.SwimTrajectory;
import cnuphys.swim.Swimmer;
import cnuphys.swim.Swimming;

/**
 * Swims all the particles in the MC bank
 * 
 * @author heddle
 * 
 */
public class SwimAllMC implements ISwimAll {

	// integration cutoff
	private static final double RMAX = 10.0;
	private static final double PATHMAX = 11.0;

	/**
	 * Get all the row data so the trajectory dialog can be updated.
	 * 
	 * @param manager the swim manager
	 * @return a vector of TrajectoryRowData objects.
	 */
	@Override
	public Vector<TrajectoryRowData> getRowData() {

		DataEvent event = ClasIoEventManager.getInstance().getCurrentEvent();
		if (event == null) {
			return null;
		}

		boolean hasBank = event.hasBank("MC::Particle");
		if (!hasBank) {
			return null;
		}

		DataManager dm = DataManager.getInstance();

		int pid[] = dm.getIntArray(event, "MC::Particle.pid");

		int len = (pid == null) ? 0 : pid.length;
		if (len < 1) {
			return null;
		}

		float px[] = dm.getFloatArray(event, "MC::Particle.px");
		if (px == null) {
			return null;
		}
		float py[] = dm.getFloatArray(event, "MC::Particle.py");
		float pz[] = dm.getFloatArray(event, "MC::Particle.pz");
		float vx[] = dm.getFloatArray(event, "MC::Particle.vx");
		float vy[] = dm.getFloatArray(event, "MC::Particle.vy");
		float vz[] = dm.getFloatArray(event, "MC::Particle.vz");

		// According to email from Raffella of 11/14/18 the units in MC::Particle
		// are now cm and GeV.

		Vector<TrajectoryRowData> v = new Vector<TrajectoryRowData>(len);

		try {

			for (int index = 0; index < len; index++) {
				LundId lid = LundSupport.getInstance().get(pid[index]);

				if (lid != null) {
					double pxo = 1000 * px[index]; // Convert GeV to MeV
					double pyo = 1000 * py[index];
					double pzo = 1000 * pz[index];
					double p = Math.sqrt(pxo * pxo + pyo * pyo + pzo * pzo);
					double theta = Math.toDegrees(Math.acos(pzo / p));
					// filter out 0 theta
					if (theta > 0.01) {

						double x = vx[index]; // keep in cm
						double y = vy[index];
						double z = vz[index];

						double phi = Math.toDegrees(Math.atan2(pyo, pxo));

						v.add(new TrajectoryRowData(index, lid, x, y, z, p, theta, phi, 0, "MC::Particle"));
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.getInstance().error("Exception while load MC records into table");
			Log.getInstance().exception(e);
			return null;
		}

		return v;
	}

	/**
	 * Swim all Monte Carlo particles
	 * 
	 * @param manager the swim manager
	 */
	@Override
	public void swimAll() {

		// System.err.println("SWIM ALL MC");
		if (ClasIoEventManager.getInstance().isAccumulating()) {
			return;
		}

		Swimming.clearMCTrajectories(); // clear all existing trajectories

		DataEvent event = ClasIoEventManager.getInstance().getCurrentEvent();
		if (event == null) {
			return;
		}

		boolean hasBank = event.hasBank("MC::Particle");
		if (!hasBank) {
			return;
		}

//		if (ClasIoEventManager.getInstance().isSourceEvioFile()) {
//			System.err.println("not swimming for evio file");
//			return;
//		}

		DataManager dm = DataManager.getInstance();

		int pid[] = dm.getIntArray(event, "MC::Particle.pid");

		int len = (pid == null) ? 0 : pid.length;
		if (len < 1) {
			return;
		}

		float px[] = dm.getFloatArray(event, "MC::Particle.px");
		if (px == null) {
			return;
		}

		// According to email from Raffella of 11/14/18 the units in MC::Particle
		// are now cm and GeV.

		float py[] = dm.getFloatArray(event, "MC::Particle.py"); // GeV
		float pz[] = dm.getFloatArray(event, "MC::Particle.pz");
		float vx[] = dm.getFloatArray(event, "MC::Particle.vx"); // cm
		float vy[] = dm.getFloatArray(event, "MC::Particle.vy");
		float vz[] = dm.getFloatArray(event, "MC::Particle.vz");

		try {

			for (int index = 0; index < pid.length; index++) {
				int pdgid = pid[index];
				LundId lid = LundSupport.getInstance().get(pdgid);

				if (lid == null) {
					System.err.println("null LundId object for id: " + pdgid);
				} else {
					if (lid != null) {
						double pxo = px[index]; // in Gev/c
						double pyo = py[index];
						double pzo = pz[index];
						double p = Math.sqrt(pxo * pxo + pyo * pyo + pzo * pzo);
						double theta = Math.toDegrees(Math.acos(pzo / p));
						// filter out 0 theta
						if (theta > 0.01) {

							// covert momenta to GeV/c from MeV/c
							// note vertices are in cm must convert to meters
							double x = vx[index] / 100.0;
							double y = vy[index] / 100.0;
							double z = vz[index] / 100.0;

							swim(lid, pxo, pyo, pzo, x, y, z);
						}
					} // lid != null
				} // else

			}
		} catch (Exception e) {
			Log.getInstance().error("Exception while swimming all MC particles");
			Log.getInstance().exception(e);
			return;
		}

	}

	/**
	 * 
	 * @param lid the lund id
	 * @param px  x component of momentum in GeV/c
	 * @param py  y component of momentum in GeV/c
	 * @param pz  z component of momentum in GeV/c
	 * @param x   x vertex coordinate in meters
	 * @param y   y vertex coordinate in meters
	 * @param z   z vertex coordinate in meters
	 */
	private void swim(LundId lid, double px, double py, double pz, double x, double y, double z) {
		
		
		double p = Math.sqrt(px * px + py * py + pz * pz);
		double theta = Math.toDegrees(Math.acos(pz / p));
		double phi = Math.toDegrees(Math.atan2(py, px));

		AdaptiveSwimmer swimmer = new AdaptiveSwimmer();
		double stepSize = 1.0e-3;
		AdaptiveSwimResult result = new AdaptiveSwimResult(true);
		double eps = 1.0e-6;
		try {
			swimmer.swim(lid.getCharge(), x, y, z, p, theta, phi, PATHMAX, stepSize, eps, result);
			result.getTrajectory().setLundId(lid);
			Swimming.addMCTrajectory(result.getTrajectory());
		} catch (AdaptiveSwimException e) {
			e.printStackTrace();
		}
		
		
		
//		Swimmer swimmer = new Swimmer();
//		double stepSize = 5e-4; // m
//		DefaultSwimStopper stopper = new DefaultSwimStopper(RMAX);
//
//		// System.err.println("swim vertex: (" + x + ", " + y + ", "
//		// + z + ")");
//		SwimTrajectory traj;
//		try {
//			traj = swimmer.swim(lid.getCharge(), x, y, z, p, theta, phi, stopper, 0, PATHMAX, stepSize,
//					Swimmer.CLAS_Tolerance, null);
//			traj.setLundId(lid);
//			Swimming.addMCTrajectory(traj);
//		} catch (RungeKuttaException e) {
//			Log.getInstance().error("Exception while swimming all MC particles");
//			Log.getInstance().exception(e);
//		}
	}
}