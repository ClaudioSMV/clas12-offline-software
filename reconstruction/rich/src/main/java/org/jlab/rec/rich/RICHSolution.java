package org.jlab.rec.rich;

import eu.mihosoft.vrl.v3d.Vector3d;
import org.jlab.clas.pdg.PhysicsConstants;
import java.util.ArrayList;
import java.util.Arrays;

import org.jlab.geom.prim.Point3D;

// ----------------
public class RICHSolution {
// ----------------

    private int debugMode = 0;

    // ----------------
     public RICHSolution(){
    // ----------------
    }

    // ----------------
    public RICHSolution(int type) {
    // ----------------

        this.type        = type;
        this.raytracks.clear();

      }

    private int   type;                               //      Solution type
    private int   OK = -1;                            //      Solution type

    private float EtaC=0;                             //      Cherenkov angle
    private float aeron=0;                            //      Aerogel refrative index
    private float theta=0;                            //      Laboratory theta 
    private float phi=0;                              //      Laboratory phi 
    private float path=0;                             //      Path within the RICH
    private int   nrefle=0;                           //      Number of photon reflections 
    private int   nrefra=0;                           //      Number of photon refractions
    private float time=0;                             //      Transit time within the RICH (solution dependent)
    private double machi2 = 0.0;                      //      chi2 of the hit-trajectory matching
    private Vector3d hit = new Vector3d(0,0,0);;      //      Impact point of photon on the PMT

    private ArrayList<RICHRay> raytracks = new ArrayList<RICHRay>(); // Detailed path of the photon

    private double elprob = 0.0;                       //      Cherenkov probability for electron
    private double piprob = 0.0;                       //      Cherenkov probability for pion
    private double kprob = 0.0;                        //      Cherenkov probability for kaon
    private double prprob = 0.0;                       //      Cherenkov probability for proton
    private double bgprob = 0.0;                       //      Cherenkov probability for background

    private int    ndir   = 0;                         //      Number of direct photons
    private double chdir  = 0.0;                       //      Mean Cherenkov angle for direct photons
    private double sdir   = 0.0;                       //      RMS Cherenkov angle for direct photons
    private int    nlat   = 0;                         //      Number of photons reflected by lateral mirrors
    private double chlat  = 0.0;                       //      Mean Cherenkov angle for photons reflected by lateral mirrors
    private double slat   = 0.0;                       //      RMS Cherenkov angle for photons reflected by lateral mirrors
    private int    nspe   = 0;                         //      Number of photons reflected by ssperical mirrors
    private double chspe  = 0.0;                       //      Mean Cherenkov angle for photons reflected by ssperical mirrors
    private double sspe   = 0.0;                       //      RMS Cherenkov angle for photons reflected by ssperical mirrors

    private double bestprob = 0.0;                     //      best Cherenkov probability for hadron ID
    private double secprob  = 0.0;                     //      second best Cherenkov probability for hadron ID
    private int bestH = 0;                             //      best Cherenkov probability for hadron ID
    private int secH  = 0;                             //      second best Cherenkov probability for hadron ID
    private double R_QP  = 0.0;                        //      Quality parameter of PID assignment

    // ----------------
    public int get_type() { return type; }
    // ----------------

    // ----------------
    public int get_OK() { return OK; }
    // ----------------

    // ----------------
    public float get_EtaC() { return EtaC; }
    // ----------------

    // ----------------
    public float get_aeron() { return aeron; }
    // ----------------

    // ----------------
    public float get_theta() { return theta; }
    // ----------------

    // ----------------
    public float get_phi() { return phi; }
    // ----------------

    // ----------------
    public float get_path() { return path; }
    // ----------------

    // ----------------
    public float get_time() { return time; }
    // ----------------

    // ----------------
    public double get_machi2() { return machi2; }
    // ----------------

    // ----------------
    public float get_raypath() {
    // ----------------

        double rpath = 0.0;
        for (RICHRay ray : raytracks) {
            rpath = rpath + ray.direction().mag();
            if(debugMode>=2)System.out.format(" photon ray path %s --> %8.2f %8.2f \n",ray.direction().toStringBrief(3), ray.direction().mag(),rpath);
        }
        return (float) rpath;
    }

    // ----------------
    public float get_raytime() {
    // ----------------

        float time = 0;
        int ii=0;
        for (RICHRay ray : raytracks) {
            double dtime = ray.direction().mag()/PhysicsConstants.speedOfLight()*ray.get_refind();
            time = time + (float) dtime;
            if(debugMode>=3)System.out.format(" photon ray path %8.2f  n %8.2f  --> %8.2f %8.2f \n", ray.direction().mag(),ray.get_refind(),dtime,time);
        }
        return time;
    }

    // ----------------
    public int get_FirstRefle() {
    // ----------------

        if(raytracks.size()>2) return raytracks.get(2).get_type();
        return 0;

    }

    // ----------------
    public int get_RefleType() {
    // ----------------

        int ifirst = get_FirstRefle();
        int ilay = (int) (ifirst-10000)/100;
        if(ifirst<10000){
            return 0;
        }else{
            if(ilay==11){
                return 2;
            }else{
                return 1;
            }
        }

    }

    // ----------------
    public int get_rayrefle() {
    // ----------------

        int debugMode = 0;

        if(debugMode==1)System.out.format("RICHSolution::get_rayrefle \n");
        int nrfl=0;
        int ira=0;
        for (RICHRay ray : raytracks) {
            int refe = (int) ray.get_type()/10000;
            if(refe == 1) nrfl++;
            if(debugMode==1)System.out.format(" ray %3d  type %6d  refe %3d  nrfl %4d \n",ira, ray.get_type(), refe, nrfl);
            ira++;
        }
        return nrfl;
    }

    // ----------------
    public int get_rayrefra() {
    // ----------------

        int nrfr=0;
        for (RICHRay ray : raytracks) {
            int refa = (int) ray.get_type()/10000;
            if(refa == 2) nrfr++;
        }
        return nrfr;
    }

    // ----------------
    public int get_nrefle() { return nrefle; }
    // ----------------

    // ----------------
    public int get_nrefra() { return nrefra; }
    // ----------------

    // -------------
    public int get_nrays() { return raytracks.size(); }
    // -------------

    // ----------------
    public RICHRay get_ray(int i){ return  this.raytracks.get(i); }
    // ----------------

    // -------------
    public RICHRay get_lastray() { return raytracks.get(raytracks.size()-1); }
    // -------------

    // ----------------
    public Vector3d get_hit() { return hit; }
    // ----------------

    // ----------------
    public double get_ElProb() { return elprob; }
    // ----------------

    // ----------------
    public double get_PiProb() { return piprob; }
    // ----------------

    // ----------------
    public double get_KProb() { return kprob; }
    // ----------------

    // ----------------
    public double get_PrProb() { return prprob; }
    // ----------------

    // ----------------
    public double get_BgProb() { return bgprob; }
    // ----------------

    // ----------------
    public int get_Ndir() { return ndir; }
    // ----------------

    // ----------------
    public double get_Chdir() { return chdir; }
    // ----------------

    // ----------------
    public double get_RMSdir() { return sdir; }
    // ----------------

    // ----------------
    public int get_Nlat() { return nlat; }
    // ----------------

    // ----------------
    public double get_Chlat() { return chlat; }
    // ----------------

    // ----------------
    public double get_RMSlat() { return slat; }
    // ----------------

    // ----------------
    public int get_Nspe() { return nspe; }
    // ----------------

    // ----------------
    public double get_Chspe() { return chspe; }
    // ----------------

    // ----------------
    public double get_RMSspe() { return sspe; }
    // ----------------

    // ----------------
    public int get_BestH() { return bestH; }
    // ----------------

    // ----------------
    public int get_secH() { return secH; }
    // ----------------

    // ----------------
    public double get_Bestprob() { return bestprob; }
    // ----------------

    // ----------------
    public double get_secprob() { return secprob; }
    // ----------------

    // ----------------
    public double get_RQP() { return R_QP; }
    // ----------------

    // ----------------
    public double assign_PID(double lh_el, double lh_pi, double lh_k, double lh_pr, double lh_bg) { 
    // ----------------

        this.set_ElProb(lh_el);
        this.set_PiProb(lh_pi);
        this.set_KProb(lh_k);
        this.set_PrProb(lh_pr);
        this.set_BgProb(lh_bg);

        double likeh[] = {this.piprob, this.kprob, this.prprob};
        Arrays.sort(likeh);
        this.bestprob = likeh[2];
        this.secprob  = likeh[1];

        double likehr[] = {this.piprob, this.kprob, this.prprob};
        for (int i=0; i<3; i++){
            if(Math.abs(this.bestprob-likehr[i])<1e-6) this.bestH=i+3;
            if(Math.abs(this.secprob-likehr[i])<1e-6) this.secH=i+3;
        }

        this.R_QP  = 0.0; 
        if(this.bestprob>0) this.R_QP = 1 - this.secprob/this.bestprob;
        return this.R_QP;

    }

    // ----------------
    public void set_type(int type) { this.type = type; }
    // ----------------

    // ----------------
    public void set_OK(int ok) { this.OK = ok; }
    // ----------------

    // ----------------
    public void set_EtaC(float EtaC) { this.EtaC = EtaC; }
    // ----------------

    // ----------------
    public void set_aeron(float aeron) { this.aeron = aeron; }
    // ----------------

    // ----------------
    public void set_theta(float theta) { this.theta = theta; }
    // ----------------

    // ----------------
    public void set_phi(float phi) { this.phi = phi; }
    // ----------------

    // ----------------
    public void set_path(float path) { this.path = path; }
    // ----------------

    // ----------------
    public void set_time(float time) { this.time = time; }
    // ----------------

    // ----------------
    public void set_machi2(double machi2) { this.machi2 = machi2; }
    // ----------------

    // ----------------
    public void set_nrefle(int nrefle) { this.nrefle = nrefle; }
    // ----------------
          
    // ----------------
    public void set_nrefra(int nrefra) { this.nrefra = nrefra; }
    // ----------------

    // ----------------
    public void add_ray(RICHRay ray) {this.raytracks.add(ray);}
    // ----------------

    // ----------------
    public ArrayList<RICHRay> get_raytracks() {return this.raytracks;}
    // ----------------

    // ----------------
    public void set_raytracks(ArrayList<RICHRay> rays){
    // ----------------

        if(rays==null){System.out.format("ATT: CHECK TO BE DONE \n"); return;}
        for (RICHRay ray: rays){
            this.raytracks.add(ray);
        }
        this.hit  = toVector3d(this.get_lastray().end());
        this.time = this.get_raytime();
        this.path = this.get_raypath();
        this.nrefle = this.get_rayrefle();
        this.nrefra = this.get_rayrefra();
    }

    
    // ----------------
    public void set_hit(Vector3d hit) { this.hit = hit; }
    // ----------------

    // ----------------
    public void set_ElProb(double elprob) { this.elprob = elprob; }
    // ----------------

    // ----------------
    public void set_PiProb(double piprob) { this.piprob = piprob; }
    // ----------------

    // ----------------
    public void set_KProb(double kprob) { this.kprob = kprob; }
    // ----------------

    // ----------------
    public void set_PrProb(double prprob) { this.prprob = prprob; }
    // ----------------

    // ----------------
    public void set_BgProb(double bgprob) { this.bgprob = bgprob; }
    // ----------------

    // ----------------
    public void set_Ndir(int ndir) { this.ndir= ndir; }
    // ----------------

    // ----------------
    public void set_Chdir(double chdir) { this.chdir= chdir; }
    // ----------------

    // ----------------
    public void set_RMSdir(double sdir) { this.sdir= sdir; }
    // ----------------

    // ----------------
    public void set_Nlat(int nlat) { this.nlat= nlat; }
    // ----------------

    // ----------------
    public void set_Chlat(double chlat) { this.chlat= chlat; }
    // ----------------

    // ----------------
    public void set_RMSlat(double slat) { this.slat= slat; }
    // ----------------

    // ----------------
    public void set_Nspe(int nspe) { this.nspe= nspe; }
    // ----------------

    // ----------------
    public void set_Chspe(double chspe) { this.chspe= chspe; }
    // ----------------

    // ----------------
    public void set_RMSspe(double sspe) { this.sspe= sspe; }
    // ----------------

    // ----------------
    public void show_raytrack() {
    // ----------------

        int ii=0;
        for(RICHRay ray: raytracks){
            System.out.format(" %d",ii);
            ray.showRay();
            ii++;
        }
    }

     //------------------------------
     public Vector3d toVector3d(Point3D pin) {
     //------------------------------
        return new Vector3d(pin.x(), pin.y(), pin.z());
     }

    // ----------------
    public void dump_raytrack(String head) {
    // ----------------

        int ii=0;
        for(RICHRay ray: raytracks){
            if(head==null){
                System.out.format(" %d",ii);
            }else{
                System.out.format(" %s %d",head,ii);
            }
            ray.dumpRay();
            ii++;
        }
    }
    
    // ----------------
    public void showSolution() {
    // ----------------
        System.out.format("SOL type %3d  EtaC %8.3f  n %6.4f  the %7.3f  phi %7.3f  hit %6.1f %6.1 %6.1f  path %6.1f  time %6.2f  nrfl %2d  nfr %2d  pel %7.5f  pi %7.5g  k %7.5g  pr %7.5g  bg %7.5g \n",
             get_type(), get_EtaC(), get_aeron(), get_theta(), get_phi(), get_hit().x, get_hit().y, get_hit().z, get_path(), get_time(), get_nrefle(), get_nrefra(), 
             get_ElProb(), get_PiProb(), get_KProb(), get_PrProb(), get_BgProb());
    }
            
}
