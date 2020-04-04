package org.jlab.analysis.eventmerger;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jlab.detector.decode.DaqScalers;
import org.jlab.detector.decode.DaqScalersSequence;
import org.jlab.jnp.hipo4.data.*;
import org.jlab.jnp.hipo4.io.HipoReader;
import org.jlab.jnp.utils.data.TextHistogram;

/**
 * Hipo Reduce Worker: filters events based on beam current 
 * calculated from integrating scalers
 * 
 * Inputs: minimum accepted beam current
 * 
 * @author devita
 */

public class FilterFcup implements Worker {

    Bank runConfigBank = null;
    DaqScalersSequence chargeSeq = null;
    private double charge  = -1;
    private double current = -1;
    private double tsResolution = 4E-9;
    private int[]  currentBuffer = new int[21];
    private int    currentMax    = 80; 
    
    public FilterFcup(double current){
        this.current=current;
        System.out.println("\nInitializing Faraday Cup reduction: threshold current set to " + this.current + "\n");
    }

    
    /**
     * Initialize bank schema
     * 
     * @param reader
     */
    @Override
    public void init(HipoReader reader) {
       runConfigBank = new Bank(reader.getSchemaFactory().getSchema("RUN::config"));
    }

    /**
     * Set sequence of scaler readings
     * 
     * @param sequence
     */
    public void setScalerSequence(DaqScalersSequence sequence) {
        this.chargeSeq=sequence;
    }
    
    /**
     * Event filter: selects events in scaler interval with beam current greater than threshold and build statistics information
     * 
     * @param event
     * @return
     */
    @Override
    public boolean processEvent(Event event) {
        event.read(runConfigBank);
        
        if(runConfigBank.getRows()>0){
            long timeStamp  = runConfigBank.getLong("timestamp",0);
            
            // get integrating scaler values for current and previous scaler intervals
            DaqScalers dsCurrent  = chargeSeq.get(timeStamp);
            DaqScalers dsPrevious = chargeSeq.getPrevious(timeStamp);
            
            // calculate beam current
            double current=0;
            if(dsCurrent!=null && dsPrevious!=null) {
                current = (dsCurrent.getBeamChargeGated()-dsPrevious.getBeamChargeGated())/
                        (dsCurrent.getTimestamp()-dsPrevious.getTimestamp())/tsResolution;                
//                System.out.println(value);           
            }
            
            // fill statistics array
            int currentBins = currentBuffer.length-1;
            if(current>currentMax){
                currentBuffer[currentBins] = currentBuffer[currentBins] + 1;
            } else if(current<0){
                currentBuffer[0] = currentBuffer[0];
            } else{
                int bin =  (int) (currentBins*((double) current)/(currentMax));
                currentBuffer[bin] = currentBuffer[bin] + 1;
            }
            
            // set filter value
            if(current>current) return true;
        }
        return false;
    }
    
    // This function has to be implemented, but not used if
    // HipoStream is not trying to classify the events.
    @Override
    public long clasifyEvent(Event event) { return 0L; }

    /**
     * Get Map of beam current values
     * @return
     */
    public Map<String,Double> getCurrentMap(){
        Map<String,Double> sizeMap = new LinkedHashMap<String,Double>();
        int currentBins = currentBuffer.length-1;
        double     step =  ((double) currentMax)/currentBins;
        for(int i = 0; i < currentBins; i++){
           String key = String.format("[%6.1f -%6.1f]", (i*step),(i+1)*step);
           sizeMap.put(key, (double) currentBuffer[i]);
        }
        sizeMap.put("overflow", (double) currentBuffer[currentBins] );
        return sizeMap;
    }

    /**
     * Show beam current histogram
     */
    public void showStats() {
        System.out.println("\n\n");
        System.out.println(" BEAM CURRENT HISTOGRAM (ENTRIES ARE EVENTS)\n");        
        TextHistogram histo = new TextHistogram();
        Map<String,Double> sizeMap = this.getCurrentMap();
        histo.setPrecision(0);
        histo.setMinDecriptorWidth(28);
        histo.setWidth(80);
        histo.setData(sizeMap);
        histo.print();
    }
}
