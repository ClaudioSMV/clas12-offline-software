/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.reco;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;
import org.jlab.io.hipo.HipoDataSync;
import org.jlab.utils.benchmark.ProgressPrintout;
import org.jlab.utils.options.OptionParser;

/**
 *
 * @author gavalian
 */
public class EngineProcessor {
    
    private final Map<String,ReconstructionEngine>  processorEngines = new LinkedHashMap<String,ReconstructionEngine>();
    ReconstructionEngine  engineDummy = null;
    
    public EngineProcessor(){
        this.engineDummy = new DummyEngine();
    }
    
    /**
     * add a reconstruction engine to the chain
     * @param name name of the engine in the chain
     * @param engine engine class
     */
    public void addEngine(String name, ReconstructionEngine engine){
        this.processorEngines.put(name, engine);
    }
    /**
     * Adding engine to the map the order of the services matters, since they will 
     * be executed in order added.
     * @param name name for the service
     * @param clazz class name including the package name
     */
    public void addEngine(String name, String clazz) {
        Class c = null;
        try {
            c = Class.forName(clazz);
            if( ReconstructionEngine.class.isAssignableFrom(c)==true){
                ReconstructionEngine engine = (ReconstructionEngine) c.newInstance();
                this.processorEngines.put(name, engine);
            } else {
                System.out.println(">>>> ERROR: class is not a reconstruction engine : " + clazz);
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(EngineProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(EngineProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(EngineProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    /**
     * Add reconstruction engine to the chain
     * @param clazz Engine class.
     */
    public void addEngine( String clazz) {
        Class c = null;
        try {
            c = Class.forName(clazz);
            if( ReconstructionEngine.class.isAssignableFrom(c)==true){
                ReconstructionEngine engine = (ReconstructionEngine) c.newInstance();
                this.processorEngines.put(engine.getName(), engine);
            } else {
                System.out.println(">>>> ERROR: class is not a reconstruction engine : " + clazz);
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(EngineProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(EngineProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(EngineProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    /**
     * Initialize all the engines in the chain.
     */
    public void init(){
        for(Map.Entry<String,ReconstructionEngine> entry : this.processorEngines.entrySet()){
            entry.getValue().init();
        }
    }
    /**
     * process a single event through the chain.
     * @param event 
     */
    public void processEvent(DataEvent event){
        for(Map.Entry<String,ReconstructionEngine>  engine : this.processorEngines.entrySet()){
            try {
                //System.out.println("processing engine : " + engine.getKey());
                //System.out.println("processing event");
                engine.getValue().processDataEvent(event);
            } catch (Exception e){
                
                System.out.println("[Exception] >>>>> engine : " + engine.getKey());
                System.out.println();
                e.printStackTrace();
            }
        }
    }
    /**
     * process entire file through engine chain.
     * @param file file name to process.
     * @param output
     */
    public void processFile(String file, String output){
        if(file.endsWith(".hipo")==true){
            HipoDataSource reader = new HipoDataSource();
            reader.open(file);
            int eventCounter = 0;
            HipoDataSync   writer = new HipoDataSync();
            writer.setCompressionType(2);
            writer.open(output);
            
            ProgressPrintout  progress = new ProgressPrintout();            
            while(reader.hasEvent()==true){
                DataEvent event = reader.getNextEvent();                
                processEvent(event);
                writer.writeEvent(event);
                eventCounter++;
                progress.updateStatus();
            }
            progress.showStatus();
            writer.close();
        }
    }
    /**
     * display services registered with the processor.
     */
    public void show(){
        System.out.println("----->>> EngineProcessor:");
        for(Map.Entry<String,ReconstructionEngine> entry : this.processorEngines.entrySet()){
            System.out.println(String.format("%-24s | %s", entry.getKey(),entry.getValue().getClass().getName()));
        }
    }
    
    public static void main(String[] args){
        
        OptionParser parser = new OptionParser();
        parser.addRequired("-o");
        parser.addRequired("-i");
        
        parser.parse(args);
        
        if(parser.hasOption("-i")==true&&parser.hasOption("-o")==true){
        
            List<String> services = parser.getInputList();
            
            String  inputFile = parser.getOption("-i").stringValue();
            String outputFile = parser.getOption("-o").stringValue();
            
            for(int i =1; i < args.length; i++){
                services.add(args[i]);
            }
                
            EngineProcessor proc = new EngineProcessor();
            for(String engine : services){
                proc.addEngine(engine);
            }
            proc.init();
            proc.processFile(inputFile,outputFile);        
        }
    }
}
