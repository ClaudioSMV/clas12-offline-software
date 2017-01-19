/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.service.eb;

import static java.lang.Math.abs;
import java.util.List;
import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.clas.detector.*;
import org.jlab.detector.base.DetectorType;
import org.jlab.io.base.DataBank;
import org.jlab.service.pid.*;



/**
 *
 * @author gavalian
 */
public class EBEngine extends ReconstructionEngine {
    
    public EBEngine(){
        super("EB","gavalian","1.0");
    }
    
    @Override
    public boolean processDataEvent(DataEvent de) {
                
        int eventType = EBio.TRACKS_HB;
        
        if(EBio.isTimeBased(de)==true){
            eventType = EBio.TRACKS_TB;
        }
        
        int run   = 10;
        double rf = -100.0;
        
        if(de.hasBank("RUN::config")==true){
            
        }
        
        if(de.hasBank("RUN::rf")==true){
            DataBank bank = de.getBank("RUN::rf");
            if(bank.rows()>0){
                rf = bank.getFloat("time", 0);
            }
        }
        
        
        EventBuilder eb = new EventBuilder();
        
        List<DetectorResponse>  responseECAL = DetectorResponse.readHipoEvent(de, "ECAL::clusters", DetectorType.EC);
        List<DetectorResponse>  responseFTOF = DetectorResponse.readHipoEvent(de, "FTOF::hits", DetectorType.FTOF,8.0);
        /*
        System.out.println("---------");
        for(DetectorResponse r : responseFTOF ){
            System.out.println(r);
        }*/
        
        eb.addDetectorResponses(responseFTOF);
        eb.addDetectorResponses(responseECAL);
        
        if(eventType==EBio.TRACKS_HB){
            List<DetectorTrack>  tracks = DetectorData.readDetectorTracks(de, "HitBasedTrkg::HBTracks");
            eb.addTracks(tracks);
        } else {
            List<DetectorTrack>  tracks = DetectorData.readDetectorTracks(de, "TimeBasedTrkg::TBTracks");
            eb.addTracks(tracks);
        }
        
        eb.processHitMatching();
        eb.processNeutralTracks();        
        eb.assignPid();
        eb.getEvent().setRfTime(rf);
        
        EBAnalyzer analyzer = new EBAnalyzer();
        //System.out.println("analyzing");
        analyzer.processEvent(eb.getEvent());
        
        if(eb.getEvent().getParticles().size()>0){
            DataBank bankP = DetectorData.getDetectorParticleBank(eb.getEvent().getParticles(), de, "REC::Particle");
            List<DetectorResponse>  responses = eb.getEvent().getDetectorResponseList();
            DataBank bankD = DetectorData.getDetectorResponseBank(responses, de, "REC::Detector");
            de.appendBanks(bankP,bankD);
            if(eb.getEvent().getParticle(0).getPid()==11){
               
               /* eb.show();
                DataBank bank = de.getBank("MC::Particle");
                bank.show();*/
            }
        }
        /*
        
        List<DetectorParticle>  chargedParticles = EBio.readTracks(de, eventType);
        List<DetectorResponse>  ftofResponse     = EBio.readFTOF(de);
        List<DetectorResponse>  ecalResponse     = EBio.readECAL(de);
        List<CherenkovResponse> htccResponse     = EBio.readHTCC(de);
        
        EBProcessor processor = new EBProcessor(chargedParticles);
        processor.addTOF(ftofResponse);
        processor.addECAL(ecalResponse);
        processor.addHTCC(htccResponse);
        
        processor.matchTimeOfFlight();
        processor.matchCalorimeter();
        processor.matchHTCC();
        processor.matchNeutral();
       
        List<DetectorParticle> centralParticles = EBio.readCentralTracks(de);
        */
        /*
        int nparticles = processor.getParticles().size();
        System.out.println("--------------------------------- ### NPARTICLES " + nparticles);
        for(int i = 0; i < nparticles; i++){
            if(processor.getParticles().get(i).getCharge()<0){
                System.out.println(processor.getParticles().get(i).toString());
            }                        
        }
        for(int i = 0; i < processor.getResponses().size(); i++){
            System.out.println(processor.getResponses().get(i).toString());
        }*/
        
        //processor.getParticles().addAll(centralParticles);
        /*
        if(de.hasBank("RUN::config")==true){
            EvioDataBank bankHeader = (EvioDataBank) de.getBank("RUN::config");
            System.out.println(String.format("***>>> RUN # %6d   EVENT %6d", 
                    bankHeader.getInt("Run",0), bankHeader.getInt("Event",0)));
        }*/
        //processor.show();
        /*System.out.println("CHARGED PARTICLES = " + chargedParticles.size());
        for(DetectorParticle p : chargedParticles){
            System.out.println(p);
        }*/
        /*
         DetectorEvent  detectorEvent = new DetectorEvent();
        
        for(int i = 0 ; i < chargedParticles.size() ; i++){ 
            detectorEvent.addParticle(chargedParticles.get(i));
          
        }*/

      /*
        EventTrigger trigger = new EventTrigger();  
        //detectorEvent.setEventTrigger(trigger);
        
        EBTrigger triggerinfo = new EBTrigger();
        triggerinfo.setEvent(detectorEvent);
        triggerinfo.setDataEvent(de);
        
        triggerinfo.RFInformation(); //Obtain RF Time
        triggerinfo.Trigger();//Use Trigger Particle Vertex Time and RF Time for Start Time
        triggerinfo.CalcBetas(); //Calculate Speeds and Masses of Particles
        */

        /*
        EBPID pid = new EBPID();
        if(EBio.isTimeBased(de)==true){
           pid.setEvent(detectorEvent);
           pid.PIDAssignment();//PID Assignment
        }*/

        
//        for(int i = 0 ; i < chargedParticles.size() ; i++){ 
//            System.out.println(chargedParticles.get(i).getPid());
//            System.out.println(chargedParticles.get(i).getPIDResult().getPIDExamination());
//          }
        
        /*
        EvioDataBank pBank = (EvioDataBank) EBio.writeTraks(processor.getParticles(), eventType);
        EvioDataBank dBank = (EvioDataBank) EBio.writeResponses(processor.getResponses(), eventType);
        EvioDataBank cBank = (EvioDataBank) EBio.writeCherenkovResponses(processor.getCherenkovResponses(), eventType);
        EvioDataBank tbank = (EvioDataBank) EBio.writeTrigger(detectorEvent);

        de.appendBanks(pBank,dBank);
        */
        return true;
    }

    @Override
    public boolean init() {
        System.out.println("[EB::] --> event builder is ready....");
        return true;
    }
    
}
