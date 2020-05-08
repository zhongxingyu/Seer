 package com.acmetelecom;
 
 public class PeakOffPeakTime {
 	private final long peakTime;
 	private final long offPeakTime;
 
 	public PeakOffPeakTime(long peakTime, long offPeakTime){
		this.offPeakTime = peakTime;
		this.peakTime = offPeakTime;
 	}
 
 	public long getPeakTime() {
 		return peakTime;
 	}
 
 	public long getOffPeakTime() {
 		return offPeakTime;
 	}
 	
 	public long durationSeconds(){
 		return peakTime + offPeakTime;
 	}
 }
