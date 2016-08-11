/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.detector.base;

import org.jlab.detector.calib.utils.DatabaseConstantProvider;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.base.Detector;
import org.jlab.geom.detector.dc.DCFactory;
import org.jlab.geom.detector.ec.ECFactory;

/**
 *
 * @author gavalian
 */
public class GeometryFactory {
    
    /**
     * Load constants for given detector, with RUN and VARIATION specified
     * @param type detector type
     * @param run run number
     * @param variation ccdb variation
     * @return 
     */
    public static ConstantProvider getConstants(DetectorType type, int run, String variation){
        DatabaseConstantProvider  provider = new DatabaseConstantProvider(run,variation);
        if(type==DetectorType.DC){
            provider.loadTable("/geometry/dc/dc");
            provider.loadTable("/geometry/dc/region");
            provider.loadTable("/geometry/dc/superlayer");
            provider.loadTable("/geometry/dc/layer");
        }
        
        if(type==DetectorType.EC){
            provider.loadTable("/geometry/pcal/pcal");
            provider.loadTable("/geometry/pcal/Uview");
            provider.loadTable("/geometry/pcal/Vview");
            provider.loadTable("/geometry/pcal/Wview");
            provider.loadTable("/geometry/ec/ec");
            provider.loadTable("/geometry/ec/uview");
            provider.loadTable("/geometry/ec/vview");
            provider.loadTable("/geometry/ec/wview");
        }
        
        if(type==DetectorType.FTOF){
            provider.loadTable("/geometry/ftof/panel1a/paddles");        
            provider.loadTable("/geometry/ftof/panel1a/panel");
            provider.loadTable("/geometry/ftof/panel1b/paddles");
            provider.loadTable("/geometry/ftof/panel1b/panel");
            provider.loadTable("/geometry/ftof/panel2/paddles");
            provider.loadTable("/geometry/ftof/panel2/panel");
        }
        provider.disconnect();
        return provider;
    }
    /**
     * Load constants for given detector, for default RUN=10 and VARIATION=default
     * @param type detector type
     * @return 
     */
    public static ConstantProvider getConstants(DetectorType type){
        return GeometryFactory.getConstants(type, 10, "default");
    }
    
    /**
     * 
     * @param type
     * @return 
     */    
    public static Detector getDetector(DetectorType type){
        return GeometryFactory.getDetector(type, 10, "default");
    }
    /**
     * Load a detector in CLAS coordinate system, for given RUN and VARIATION
     * @param type detector type
     * @param run run number
     * @param variation ccdb variation
     * @return 
     */
    public static Detector getDetector(DetectorType type, int run, String variation){
        ConstantProvider  provider = GeometryFactory.getConstants(type, run, variation);
        if(type==DetectorType.DC){
            DCFactory factory = new DCFactory();
            Detector dc = factory.createDetectorCLAS(provider);
            return dc;
        }
        
        if(type==DetectorType.EC){
            ECFactory factory = new ECFactory();
            Detector ec = factory.createDetectorCLAS(provider);
            return ec;
        }
        
        System.out.println("[GeometryFactory] --->  detector construction for " 
                + type.getName() + "  is not implemented");
        return null;
    }
    
    
    
}
