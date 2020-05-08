 /**
  * 
  */
 package com.smmsp.time;
 
 /**
  * @author sean
  *
  */
 public class GregorianDateRange {
 
 	private GregorianDate _start;
 	private GregorianDate _end;
 	
 	public GregorianDateRange(int startYear, int startMonth, int startDay,
 			int endYear, int endMonth, int endDay){
 		_start = new GregorianDate(startYear, startMonth, startDay);
 		_end = new GregorianDate(endYear, endMonth, endDay);
 	}
 	
 	public GregorianDateRange(GregorianDate start, GregorianDate end){
 		_start = start;
 		_end = end;
 	}
 	
 	public boolean isWithinRange(GregorianDate date){
		
		return false;
 	}
 }
