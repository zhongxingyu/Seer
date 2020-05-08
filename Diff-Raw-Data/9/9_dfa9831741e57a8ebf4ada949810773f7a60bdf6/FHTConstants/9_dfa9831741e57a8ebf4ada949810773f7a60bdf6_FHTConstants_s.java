 package de.tobiaswegner.communication.cul4java.impl;
 
 public class FHTConstants {
 	public static final int FHT_ACTUATOR_1	= 0x00;
 	
	public static String getIDByCode(byte code) {
 		if (code == 0x00)
 			return "actuator0";
 		if (code == 0x01)
 			return "actuator1";
 		if (code == 0x02)
 			return "actuator2";
 		if (code == 0x03)
 			return "actuator3";
 		if (code == 0x04)
 			return "actuator4";
 		if (code == 0x05)
 			return "actuator5";
 		if (code == 0x06)
 			return "actuator6";
 		if (code == 0x07)
 			return "actuator7";
 		
 		if (code == 0x42)
 			return "measured-low";
 		if (code == 0x43)
 			return "measured-high";
 		
 		if (code == 0x82)
 			return "day-temp";
 		if (code == 0x84)
 			return "night-temp";
 		if (code == 0x85)
 			return "lowtemp-offset";
 		if (code == 0x8a)
 			return "windowopen-temp";
 
 		return "unknown";
 	}
 	
 	public static final int FHT8b_STATE_WINDOW_OPEN = 1;
 	public static final int FHT8b_STATE_WINDOW_CLOSED = 2;
 	public static final int FHT8b_STATE_LOWBAT = 9;
 }
