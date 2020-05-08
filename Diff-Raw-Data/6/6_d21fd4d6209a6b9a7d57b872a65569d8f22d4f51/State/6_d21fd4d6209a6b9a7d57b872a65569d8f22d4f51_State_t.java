 package com.dspg.ule.cmbs;
 
 public enum State {
 	START(0),
 	
 	CMBS_CMD_HELLO(1), 
 	CMBS_CMD_HELLO_RPLY(2),
 	CMBS_EV_DSR_SYS_START(3),
 	CMBS_EV_DSR_SYS_START_RES(4),
 	
 	CMBS_EV_DSR_RF_RESUME(100),
 	CMBS_EV_DSR_HAN_MNGR_INIT(101),
 	CMBS_EV_DSR_HAN_MNGR_INIT_RES(102),
 	CMBS_EV_DSR_HAN_MNGR_START(103),
 	CMBS_EV_DSR_HAN_MNGR_START_RES(104),
 	CMBS_EV_DSR_PARAM_AREA_SET(105),
 	CMBS_EV_DSR_PARAM_AREA_SET_RES(106),
 	CMBS_EV_DSR_HAN_MSG_RECV_REGISTER(107),
 	CMBS_EV_DSR_HAN_DEVICE_READ_TABLE(108),
 	CMBS_EV_DSR_HAN_MSG_RECV_REGISTER_RES(109),
 	CMBS_EV_DSR_HAN_DEVICE_READ_TABLE_RES(110),
 	
 	IDLE(900),
 	QUIT(999);
 	
 	private final int numState;
 	
 	private State (int i) {
 		this.numState = i;
	}
	
	public int getState (int n) {
		return (this.numState);
	}
 }
