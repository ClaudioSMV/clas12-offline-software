package org.jlab.service.ft;

import org.jlab.clas.reco.ReconstructionEngine;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.rec.ft.hodo.FTHODOReconstruction;


public class FTHODORecEngine extends ReconstructionEngine {

	public FTHODORecEngine() {
		super("FTHODO", "devita", "3.0");
	}

	FTHODOReconstruction reco;
	int Run = -1;
	FTRecConfig config;
	
	@Override
	public boolean init() {
		config = new FTRecConfig();
		reco = new FTHODOReconstruction();
		reco.debugMode=0;
		return true;
	}

	@Override
	public boolean processDataEvent(DataEvent event) {
		Run = config.setRunConditionsParameters(event, "FTHODO", Run);
		reco.processEvent((EvioDataEvent) event);
		return true;
	}

	
}
