 package com.buglabs.nmea;
 
 public class DegreesMinutesSeconds {
 	
 	private String dms;
 	
 	public DegreesMinutesSeconds(String dms) {
 		int i = dms.indexOf(",");
 		this.dms = dms.substring(0, i);
 	}
 	
 	public double getDegrees() {
 		int i = dms.indexOf(".");
 		
 		if(dms.charAt(0) == '0') {
 			return Double.parseDouble("-" + dms.substring(1, i - 2));
 		}
 		
 		return Double.parseDouble(dms.substring(0, i - 2));
 	}
 	
 	public double getMinutes() {
 		int i = dms.indexOf(".");
 		
 		return Double.parseDouble(dms.substring(i - 2, i));
 	}
 	
 	public double getSeconds() {
 		int i = dms.indexOf(".");
 		
		String seconds = dms.substring(i+1);
 		
		if(seconds.length() > 2) {
			return Double.parseDouble(seconds.substring(0, 2) + "." + seconds.substring(2));
		}
		return Double.parseDouble(seconds);
 	}
 	
 	public double toDecimalDegrees() {
 		double decimal = getSeconds() / 60.0;
 		double result = (getMinutes() + decimal) / 60.0;
 		
 		double degrees = getDegrees();
 		
 		if(degrees < 0.0) {
 			return degrees - result;
 		}
 		
 		return degrees + result;
 	}
 }
