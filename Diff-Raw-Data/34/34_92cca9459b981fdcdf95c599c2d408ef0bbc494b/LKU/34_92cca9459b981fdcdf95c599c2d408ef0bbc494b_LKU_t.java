 package com.cs.fwk.sim.cbs;
 
 
 import com.cs.fwk.api.MatElementDefs;
 import com.cs.fwk.api.util.ConfigItem;
 import com.cs.fwk.sim.bricks.BaseElement;
 import com.cs.fwk.sim.model.LookupResult;
 import com.cs.fwk.sim.model.SimElement;
 import com.cs.fwk.sim.model.SimHost;
 import com.cs.fwk.sim.model.TickdataResult;
 import com.cs.fwk.sim.types.Event;
 
 /**
  * Data value lookup
  * 
  *--	Looks up values for incoming instr from LKU or Tickdata bus
  *--	Supports 4 separate channels: ipx -> fn -> opx
  *--
  *--	Config:
  *--		C_CHN_SPEC	= CB.FN.ARG.[TY.EV.IP]
  *--			CB  = id of CB to address, or FF (8 bit)
  *--			FN  = id of LKU or TD fn (8 bit)
  *--			ARG = arg to provide to LKU fn (8 bit) or Tickdata bitselect (0:low 32 bits, 1: high 32 bits)
  *--			TY  = 00:Tickdata, 01:LKU (2 bit)
  *--			EV  = TF - sens to ip=True,False, or both. (2 bit)
  *--			IP  = which input to apply fn to (4 bit = 0,1,2,3)
  * 
  * @author pjf
  *
  */
 public class LKU extends BaseElement implements SimElement {
 	private final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(LKU.class);
 	private ChnSpec c_chnSpecs[];
 	private static final int LATENCY = 5;	// input to output latency (microticks)
 
 	/**
 	 * Class to hold channel spec
 	 * 
 	 * Access the attributes directly as a struct
 	 */
 	private class ChnSpec {
 		final int target;
 		final int fn;
 		final int arg;
 		final boolean isLKU;
 		final int ev;
 		
 		public ChnSpec(int target, int fn, int arg, boolean isLKU, int ev) {
 			super();
 			this.target = target;
 			this.fn = fn;
 			this.arg = arg;
 			this.isLKU = isLKU;
 			this.ev = ev;
 		}
 
 		@Override
 		public String toString() {
 			return "ChnSpec [target=" + target + ", fn=" + fn + ", arg=" + arg
 					+ ", isLKU=" + isLKU + ", ev=" + ev + "]";
 		}
 		
 	}
 	
 	public LKU(int id, SimHost host) {
 		super(id, MatElementDefs.EL_TYP_LOG,host);
 		c_chnSpecs = new ChnSpec[4];
 	}
 
 	@Override
 	protected void processConfig(ConfigItem cfg) {
 		switch (cfg.getItemId()) {
 		case MatElementDefs.EL_LKU_C_CHN_SPEC: 
 			int chn = cfg.getRawData() & 0xf;
 			int ev = (cfg.getRawData() >> 4) & 0x3;
 			int ty = (cfg.getRawData() >> 6) & 0x3;
 			int arg = (cfg.getRawData() >> 8) & 0xff;
 			int fn = (cfg.getRawData() >> 16) & 0xff;
 			int cb = (cfg.getRawData() >> 24) & 0xff;
 			ChnSpec cs = new ChnSpec(cb,fn,arg,(ty==1),ev);
 			if (chn < 4) {
 				c_chnSpecs[chn] = cs;
 				logger.info("CFG for ip: " + chn + ": " + cs);;
 			} else {
 				logger.error(getIdStr() + "Unexpected configuration (bad ip): " + cfg);
 			}
 			break;
 			
 		default: logger.warn(getIdStr() + "Unexpected configuration: " + cfg); break;
 		}
 	}
 
 
 	@Override
 	protected void processEvent(int input, Event evt) {
		logger.info("processEvent() - " + evt + " on input " + input);
 		int ip = input - 1;
 		ChnSpec cs = c_chnSpecs[ip];
 		if (cs != null) {
 			// check event
 			boolean trigger = false;
 			switch(cs.ev) {
 			case 0:		trigger = false;					break;
 			case 1:		trigger = evt.getRawData() == 0;	break;
 			case 2:		trigger = evt.getRawData() != 0;	break;
 			case 3:		trigger = true;						break;
 			}
 			if (trigger) {
 				// determine data for event
 				boolean gotData = false;
 				int data = 0;
 				if (cs.isLKU) {
 					// perform lookup
 					try {
 						LookupResult rslt = lookup(evt.getInstrument_id(), cs.arg, evt.getTickref(), 
 								cs.fn, cs.target);
 						if (rslt.isValid()) {
 							gotData = true;
 							data = rslt.getIntData();
 						}
 					} catch (Exception e) {
 						logger.warn("Error in LKU lookup - " + e.getMessage());
 					}
 				} else {
 					// get tickdata
 					try {
 						TickdataResult rslt = host.tickdata(elementId, evt.getTickref(), cs.fn);
 						if (rslt.isValid()) {
 							gotData = true;
 							if ((cs.arg & 0x1) == 0x1) {
 								data = (int) (rslt.getRawData() >> 32);
 							} else {
 								data = (int) (rslt.getRawData() & 0xffffffff);
 							}
 						}
 					} catch (Exception e) {
 						logger.warn("Error in tickdata lookup - " + e.getMessage());
 					}
 				}
 				if (gotData) {
 					// put event out
 					Event evtOut = new Event(host.getCurrentSimTime(),elementId,input, evt.getInstrument_id(),
 							evt.getTickref(),data);
 					host.publishEvent(evtOut,LATENCY);
 					
 				}
 			}
 		}		
 	}
 
 
 	@Override
 	protected String getTypeName() {
 		return "LKU";
 	}
 
 
 }
