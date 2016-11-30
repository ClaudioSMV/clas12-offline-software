package org.jlab.rec.tof.banks.ftof;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;




import org.jlab.detector.geant4.v2.FTOFGeant4Factory;
//import org.jlab.detector.geant4.v2.FTOFGeant4Factory;
import org.jlab.detector.hits.DetHit;
import org.jlab.detector.hits.FTOFDetHit;
import org.jlab.geom.prim.Point3D;
import org.jlab.geometry.prim.Line3d;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.rec.ftof.CCDBConstantsLoader;
import org.jlab.rec.ftof.Constants;
import org.jlab.rec.tof.hit.ftof.Hit;

/**
 * 
 * @author ziegler
 *
 */
public class HitReader {

	public HitReader() {
		// TODO Auto-generated constructor stub
	}

	private List<Hit> _FTOF1AHits ;
	private List<Hit> _FTOF1BHits ;
	private List<Hit> _FTOF2Hits ;
	
	public List<Hit> get_FTOF1AHits() {
		return _FTOF1AHits;
	}

	public void set_FTOF1AHits(List<Hit> _FTOF1AHits) {
		this._FTOF1AHits = _FTOF1AHits;
	}

	public List<Hit> get_FTOF1BHits() {
		return _FTOF1BHits;
	}

	public void set_FTOF1BHits(List<Hit> _FTOF1BHits) {
		this._FTOF1BHits = _FTOF1BHits;
	}

	public List<Hit> get_FTOF2Hits() {
		return _FTOF2Hits;
	}

	public void set_FTOF2Hits(List<Hit> _FTOF2Hits) {
		this._FTOF2Hits = _FTOF2Hits;
	}
	/**
	 * 
	 * @param event the evio event
	 * @param geometry the FTOF geometry from package
	 */
	public void fetch_Hits(DataEvent event, FTOFGeant4Factory geometry, List<Line3d> trks, double[] paths) {
		
		//if(event.hasBank("FTOF1A::dgtz")==false && event.hasBank("FTOF1B::dgtz")==false && event.hasBank("FTOF2B::dgtz")==false) {
		if(event.hasBank("FTOF::dgtz")==false ) {				
			//System.err.println("there is no FTOF bank ");
			
			_FTOF1AHits = new ArrayList<Hit>();
			_FTOF1BHits = new ArrayList<Hit>();
			_FTOF2Hits  = new ArrayList<Hit>();
			
			return;
		}

		if(event.getDictionary().getXML().contains("FTOF1A")) {
			if(event.hasBank("FTOF1A::dgtz")==true) {
				EvioDataBank bankDGTZ1A = (EvioDataBank) event.getBank("FTOF1A::dgtz");
				
				int[] id_1A 	= bankDGTZ1A.getInt("hitn");
		        int[] sector_1A = bankDGTZ1A.getInt("sector");
				int[] paddle_1A = bankDGTZ1A.getInt("paddle");
				int[] ADCL_1A 	= bankDGTZ1A.getInt("ADCL");
				int[] ADCR_1A 	= bankDGTZ1A.getInt("ADCR");
				int[] TDCL_1A 	= bankDGTZ1A.getInt("TDCL");
				int[] TDCR_1A 	= bankDGTZ1A.getInt("TDCR");
				// Instantiates the list of hits
				List<Hit> hits = new ArrayList<Hit>();
				
				for(int i = 0; i<id_1A.length; i++){
					if( passADC(ADCL_1A[i])==0 || passADC(ADCR_1A[i])==0 || passTDC(TDCL_1A[i])==0 || passTDC(TDCR_1A[i])==0 )
						continue;
					
				    // get the status
					int statusL = CCDBConstantsLoader.STATUSU[sector_1A[i]-1][0][paddle_1A[i]-1];
					int statusR = CCDBConstantsLoader.STATUSD[sector_1A[i]-1][0][paddle_1A[i]-1];
					String statusWord = this.set_StatusWord(statusL, statusR, ADCL_1A[i], TDCL_1A[i], ADCR_1A[i], TDCR_1A[i]);
									
					// create the hit object
					Hit hit = new Hit(id_1A[i], 1, sector_1A[i], paddle_1A[i], ADCL_1A[i], TDCL_1A[i], ADCR_1A[i], TDCR_1A[i]) ;				
					hit.set_StatusWord(statusWord);
					hit.setPaddleLine(geometry);
		    	    // add this hit
		            hits.add(hit); 
				}
				
				List<Hit> updated_hits= matchHitsToDCTrk(hits,  geometry, trks, paths);
				
				for(Hit hit : updated_hits) {
					// set the superlayer to get the paddle position from the geometry package
					// superlayer = 1;
					hit.set_HitParameters(1);				
				}
				
				Collections.sort(updated_hits);
				// fill the list of TOF hits
				this.set_FTOF1AHits(updated_hits);
			}
			if(event.hasBank("FTOF1B::dgtz")==true) {
				EvioDataBank bankDGTZ1B = (EvioDataBank) event.getBank("FTOF1B::dgtz");
				
				int[] id_1B 	= bankDGTZ1B.getInt("hitn");
		        int[] sector_1B = bankDGTZ1B.getInt("sector");
				int[] paddle_1B = bankDGTZ1B.getInt("paddle");
				int[] ADCL_1B 	= bankDGTZ1B.getInt("ADCL");
				int[] ADCR_1B 	= bankDGTZ1B.getInt("ADCR");
				int[] TDCL_1B 	= bankDGTZ1B.getInt("TDCL");
				int[] TDCR_1B 	= bankDGTZ1B.getInt("TDCR");
				// Instantiates the list of hits
				List<Hit> hits = new ArrayList<Hit>();
				
				for(int i = 0; i<id_1B.length; i++){
					if( passADC(ADCL_1B[i])==0 || passADC(ADCR_1B[i])==0 || passTDC(TDCL_1B[i])==0 || passTDC(TDCR_1B[i])==0 )
						continue;
					// get the status
					int statusL = CCDBConstantsLoader.STATUSU[sector_1B[i]-1][1][paddle_1B[i]-1];
					int statusR = CCDBConstantsLoader.STATUSD[sector_1B[i]-1][1][paddle_1B[i]-1];
					String statusWord = this.set_StatusWord(statusL, statusR, ADCL_1B[i], TDCL_1B[i], ADCR_1B[i], TDCR_1B[i]);			
					
					// create the hit object
					Hit hit = new Hit(id_1B[i], 2, sector_1B[i], paddle_1B[i], ADCL_1B[i], TDCL_1B[i], ADCR_1B[i], TDCR_1B[i]) ;
					hit.set_StatusWord(statusWord);
					hit.setPaddleLine(geometry);
					
		    	    // add this hit
		            hits.add(hit); 
				}
				
				List<Hit> updated_hits= matchHitsToDCTrk(hits,  geometry, trks, paths);
				
				for(Hit hit : updated_hits) {
					// set the superlayer to get the paddle position from the geometry package
					// superlayer = 2;
					hit.set_HitParameters(2);		
				}
				
				Collections.sort(updated_hits);
				// fill the list of TOF hits
				this.set_FTOF1BHits(updated_hits);
			}
			if(event.hasBank("FTOF2B::dgtz")==true) {
				EvioDataBank bankDGTZ2  = (EvioDataBank) event.getBank("FTOF2B::dgtz"); // not my fault it was 2B in the xml file ....
				
				int[] id_2 		= bankDGTZ2.getInt("hitn");
		        int[] sector_2 	= bankDGTZ2.getInt("sector");
				int[] paddle_2 	= bankDGTZ2.getInt("paddle");
				int[] ADCL_2 	= bankDGTZ2.getInt("ADCL");
				int[] ADCR_2 	= bankDGTZ2.getInt("ADCR");
				int[] TDCL_2 	= bankDGTZ2.getInt("TDCL");
				int[] TDCR_2 	= bankDGTZ2.getInt("TDCR");
				
				// Instantiates the list of hits
				List<Hit> hits = new ArrayList<Hit>();
				
				for(int i = 0; i<id_2.length; i++){// get the status
					if( passADC(ADCL_2[i])==0 || passADC(ADCR_2[i])==0 || passTDC(TDCL_2[i])==0 || passTDC(TDCR_2[i])==0 )
						continue;
					int statusL = CCDBConstantsLoader.STATUSU[sector_2[i]-1][2][paddle_2[i]-1];
					int statusR = CCDBConstantsLoader.STATUSD[sector_2[i]-1][2][paddle_2[i]-1];
					String statusWord = this.set_StatusWord(statusL, statusR, ADCL_2[i], TDCL_2[i], ADCR_2[i], TDCR_2[i]);
					
					// create the hit object
					Hit hit = new Hit(id_2[i], 3, sector_2[i], paddle_2[i], ADCL_2[i], TDCL_2[i], ADCR_2[i], TDCR_2[i]) ;
					hit.setPaddleLine(geometry);
					hit.set_StatusWord(statusWord);
		    	    // add this hit
		            hits.add(hit); 
				}
				
				List<Hit> updated_hits = matchHitsToDCTrk(hits, geometry, trks, paths);
				
				for(Hit hit : updated_hits) {
					// set the superlayer to get the paddle position from the geometry package
					// superlayer = 3;
					hit.set_HitParameters(3);				
				}
				Collections.sort(updated_hits);
				// fill the list of TOF hits
				this.set_FTOF2Hits(updated_hits);
			}
		}
		if(event.hasBank("FTOF::dgtz")==true) {
			EvioDataBank bankDGTZ = (EvioDataBank) event.getBank("FTOF::dgtz");
			
			int[] id 	= bankDGTZ.getInt("hitn");
	        int[] sector = bankDGTZ.getInt("sector");
	        int[] panel  = bankDGTZ.getInt("layer"); 
			int[] paddle = bankDGTZ.getInt("paddle");
			int[] ADCL 	= bankDGTZ.getInt("ADCL");
			int[] ADCR 	= bankDGTZ.getInt("ADCR");
			int[] TDCL 	= bankDGTZ.getInt("TDCL");
			int[] TDCR 	= bankDGTZ.getInt("TDCR");
			// Instantiates the lists of hits
			List<Hit> hits = new ArrayList<Hit>();
			
			for(int i = 0; i<id.length; i++){
				if( passADC(ADCL[i])==0 || passADC(ADCR[i])==0 || passTDC(TDCL[i])==0 || passTDC(TDCR[i])==0 )
					continue;
				
			    // get the status
				int statusL = CCDBConstantsLoader.STATUSU[sector[i]-1][0][paddle[i]-1];
				int statusR = CCDBConstantsLoader.STATUSD[sector[i]-1][0][paddle[i]-1];
				String statusWord = this.set_StatusWord(statusL, statusR, ADCL[i], TDCL[i], ADCR[i], TDCR[i]);
								
				// create the hit object
				Hit hit = new Hit(id[i], panel[i], sector[i], paddle[i], ADCL[i], TDCL[i], ADCR[i], TDCR[i]) ;				
				hit.set_StatusWord(statusWord);
				hit.setPaddleLine(geometry);
	    	    // add this hit
	            hits.add(hit); 
			}
			
			List<Hit> updated_hits= matchHitsToDCTrk(hits, geometry, trks, paths);
			
			ArrayList<ArrayList<Hit>> DetHits = new ArrayList<ArrayList<Hit>>();
			for(int j =0; j<3; j++)
				DetHits.add(j, new ArrayList<Hit>());
			
			for(Hit hit : updated_hits) {
				// set the layer to get the paddle position from the geometry package				
				hit.set_HitParameters(hit.get_Panel());
				DetHits.get(hit.get_Panel()-1).add(hit);
			}
			if(DetHits.get(0).size()>0) {
				Collections.sort(DetHits.get(0));
				// fill the list of TOF hits
				this.set_FTOF1AHits(DetHits.get(0));
			}
			if(DetHits.get(1).size()>0) {
				Collections.sort(DetHits.get(1));
				// fill the list of TOF hits
				this.set_FTOF1AHits(DetHits.get(1));
			}
			if(DetHits.get(2).size()>0) {
				Collections.sort(DetHits.get(2));
				// fill the list of TOF hits
				this.set_FTOF1AHits(DetHits.get(2));
			}
		}
	}

	 
	
	public String set_StatusWord(int statusL, int statusR, int ADCL, int TDCL, int ADCR, int TDCR) {
		String statusWord = new String(); //ADCL TDCL ADCR TDCR
		// selected ranges TDC in [0,1000], ADC in [0, 8192] requirement given by passTDC and passADC methods
		
		switch(statusL) {
			case 0:	statusWord = (""+1*passADC(ADCL)+""+1*passTDC(TDCL)+"");// fully functioning
					break;
			case 1:	statusWord = ("0"+""+1*passTDC(TDCL)+""); 				// no ADC
					break;
			case 2:	statusWord = (""+1*passADC(ADCL)+""+"0"); 				// no TDC
					break;
			case 3:	statusWord = "00";										// no TDC, no ADC
					break;
		} 
		switch(statusR) {
			case 0:	statusWord += (""+1*passADC(ADCR)+""+1*passTDC(TDCR)+"");// fully functioning
			break;
			case 1:	statusWord += ("0"+""+1*passTDC(TDCR)+""); 				// no ADC
					break;
			case 2:	statusWord += (""+1*passADC(ADCR)+""+"0"); 				// no TDC
					break;
			case 3:	statusWord += "00";										// no TDC, no ADC
					break;

		} 
	return statusWord; 
		
	}

	private int passTDC(int tDC) {
		// selected ranges TDC 
		int pass =0;
		if(Constants.LSBCONVFAC*tDC>Constants.TDCMINSCALE &&  Constants.LSBCONVFAC*tDC<Constants.TDCMAXSCALE)
			pass = 1; 
		pass = 1; 
		return pass;
	}

	private int passADC(int aDC) {
		// selected ranges  ADC 
		int pass =0;
		if(aDC>Constants.ADCMIN && aDC<Constants.ADCMAX)
			pass = 1; 
		pass = 1; 
		return pass;
	}

	private List<Hit> matchHitsToDCTrk(List<Hit>FTOFhits, FTOFGeant4Factory ftofDetector, List<Line3d> trks, double[] paths) { 
		if(trks==null || trks.size()==0)
			return FTOFhits; // no hits were matched with DC tracks
		
		// Instantiates the list of hits
		List<Hit> hitList = new ArrayList<Hit>();
		FTOFDetHit[][][][] HitArray = new FTOFDetHit[6][3][62][trks.size()] ;	
		for(int i = 0; i<trks.size(); i++) { // looping over the tracks find the intersection of the track with that plane
			Line3d trk = trks.get(i);
			
			List<DetHit> hits = ftofDetector.getIntersections(trk);
			
			if(hits != null && hits.size()>0) {
				for(DetHit hit: hits){
					FTOFDetHit fhit = new FTOFDetHit(hit); 
					HitArray[fhit.getSector()-1][fhit.getLayer()-1][fhit.getPaddle()-1][i] = fhit;
				}
			}
		}
		for(Hit fhit : FTOFhits) {
			boolean isAssociatedWTrk = false;
			for(int i = 0; i<trks.size(); i++) {
				
				if(HitArray[fhit.get_Sector()-1][fhit.get_Panel()-1][fhit.get_Paddle()-1][i]!=null) {
					isAssociatedWTrk = true;
					FTOFDetHit matchedHit = HitArray[fhit.get_Sector()-1][fhit.get_Panel()-1][fhit.get_Paddle()-1][i];
					
					// create a new FTOF hit for each intersecting track with this hit counter 
					// create the hit object
					Hit hit = new Hit(fhit.get_Id(), fhit.get_Panel(), fhit.get_Sector(), fhit.get_Paddle(), fhit.get_ADC1(), fhit.get_TDC1(), fhit.get_ADC2(), fhit.get_TDC2()) ;
					hit.set_StatusWord(fhit.get_StatusWord());			
					hit.set_paddleLine(fhit.get_paddleLine());
					hit.set_matchedTrackHit(matchedHit);
					hit.set_matchedTrack(trks.get(i));
					// get the pathlength of the track from its origin to the mid-point between the track entrance and exit from the bar
					double deltaPath = matchedHit.origin().distance(matchedHit.mid()); 
					hit.set_TrkPathLen(paths[i]+deltaPath);
					// get the coordinates for the track hit, which is defined as the mid-point between its entrance and its exit from the bar
					hit.set_TrkPosition(new Point3D(matchedHit.mid().x,matchedHit.mid().y,matchedHit.mid().z));
					hit._AssociatedTrkId = i;
					// compute the local y at the middle of the bar :
					//----------------------------------------------
			        Point3D origPaddleLine = hit.get_paddleLine().origin();
			        Point3D trkPosinMidlBar = new Point3D(matchedHit.mid().x, matchedHit.mid().y, matchedHit.mid().z);
			        double Lov2 = hit.get_paddleLine().length()/2;
			        double barOrigToTrkPos = origPaddleLine.distance(trkPosinMidlBar);
			        // local y:
			        hit.set_yTrk(barOrigToTrkPos-Lov2);
			        //---------------------------------------
			        hitList.add(hit);	// add this hit to the output list 
				}
			}
			if(isAssociatedWTrk == false )
				hitList.add(fhit);	// add this hit to the output list anyway
			
		}
			
			
		return hitList;
	}

	
}
