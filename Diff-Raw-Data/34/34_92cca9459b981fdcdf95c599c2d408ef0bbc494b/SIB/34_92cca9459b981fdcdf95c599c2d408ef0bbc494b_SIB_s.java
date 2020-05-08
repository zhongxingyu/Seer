 package com.cs.fwk.sim.cbs;
 
 import org.apache.log4j.Logger;
 
 import com.cs.fwk.api.MatElementDefs;
 import com.cs.fwk.api.util.ConfigItem;
 import com.cs.fwk.sim.bricks.SIBCore;
 import com.cs.fwk.sim.bricks.SynthLegData;
 import com.cs.fwk.sim.model.SimHost;
 import com.cs.fwk.sim.types.Event;
 import com.cs.fwk.util.Conversion;
 
 /**
  * SIB - synthetic instrument builder 
  * 
  * @author pjf
  *
  */
 public class SIB extends SIBCore {
 	private final static Logger logger = Logger.getLogger(SIB.class);
 	private final static int MAX_OUT = 4;
 	private static final int LATENCY = 5;	// input to output latency (microticks)
 	private int c_opLeg[];
 	
 	public SIB(int id, SimHost host) {
 		super(id, MatElementDefs.EL_TYP_SIB,host);
 		c_opLeg = new int[MAX_OUT];
 		for (int i=0; i<MAX_OUT; i++) {
 			c_opLeg[i] = -1;	// all outputs off
 		}
 	}
 
 	@Override
 	protected void processSyntheticLegEvent(int synthInstrId, int tickref, int leg, SynthLegData legData[]) {
 		// for each output, put out the synth instr with the requested leg data if data is valid
 		logger.info("processSyntheticLegEvent(): sid= " + synthInstrId + ", tickref=" + tickref + 
 				", leg=" + leg + ", legData=" + Conversion.arrayToString(legData));
 		for (int op=0; op<MAX_OUT; op++) {
 			int opLeg = c_opLeg[op];
 			if (opLeg >= 0  &&  opLeg < legData.length) {
 				if (legData[opLeg] != null  &&  legData[opLeg].isValid()) {
					Event evtOut = new Event(host.getCurrentSimTime(),elementId,op,synthInstrId,
 							tickref, legData[opLeg].getRawData());
 					host.publishEvent(evtOut,LATENCY);
 				}
 			}
 		}
 		
 	}
 
 
 	@Override
 	protected void processExtConfig(ConfigItem cfg) {
 		switch (cfg.getItemId()) {
 		case MatElementDefs.EL_SIB_C_OPCFG: processOpCfg(cfg);	break;
 		default: logger.warn(getIdStr() + "Unexpected configuration: " + cfg);
 		}
 	}
 
 	private void processOpCfg(ConfigItem cfg) {
 		int cdata = cfg.getRawData();
 		for (int op=0; op<MAX_OUT; op++) {
 			int opc = cdata & 0xf;
 			int opd = -1; // default off
 			if (opc != 0xf) {
 				opd = opc;
 				logger.info("processOpCfg(): op" + op + " = leg " + opd);
 			}
 			c_opLeg[op] = opd;
 			cdata /= 16;
 		}
 	}
 
 
 	@Override
 	protected String getTypeName() {
 		return "SIB";
 	}
 
 }
