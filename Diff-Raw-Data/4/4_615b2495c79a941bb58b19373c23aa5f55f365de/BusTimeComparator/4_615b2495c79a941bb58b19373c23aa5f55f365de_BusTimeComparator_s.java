 package uk.co.mentalspace.android.bustimes.utils;
 
 import java.util.Comparator;
 
 import uk.co.mentalspace.android.bustimes.BusTime;
 
 public class BusTimeComparator implements Comparator<BusTime> {
 
     @Override
     public int compare(BusTime bt1, BusTime bt2) {
     	boolean bt1IsNumber = false;
     	boolean bt1IsValue = false;
     	Integer bt1Value = null;
     	if (null != bt1) {
     		bt1IsValue = true;
     		try {
     			bt1Value = Integer.parseInt(bt1.getEstimatedArrivalTime());
     			bt1IsNumber = true;
     		} catch (NumberFormatException nfe) {
     			//do nothing - just testing if a numeric value was supplied
     		}
     	}
     	
     	boolean bt2IsNumber = false;
     	boolean bt2IsValue = false;
     	Integer bt2Value = null;
     	if (null != bt2) {
     		bt2IsValue = true;
     		try {
     			bt2Value = Integer.parseInt(bt2.getEstimatedArrivalTime());
     			bt2IsNumber = true;
     		} catch (NumberFormatException nfe) {
     			//do nothing - just testing if a numeric value was supplied
     		}
     	}
     	
     	if (bt1IsValue && !bt2IsValue) return 1; //value (bt1) comes first
     	if (!bt1IsValue && bt2IsValue) return -1; //value (bt2) comes first
     	if (!bt1IsValue && !bt2IsValue) return 0; //both non-values - doesn't matter in order (but return here to avoid next tests
 
    	if (bt1IsNumber && !bt2IsNumber) return -1; //non-number (e.g. 'Due') comes first
    	if (!bt1IsNumber && bt2IsNumber) return 1; // non-number (e.g. 'Due') comes first
     	
     	//both have values, and both are numeric - so use standard integer comparison
     	if (bt1IsNumber && bt2IsNumber) return bt1Value.compareTo(bt2Value);
     	
     	//both have values, and neither are numeric - so use standard string comparison
     	return bt1.getEstimatedArrivalTime().compareTo(bt2.getEstimatedArrivalTime());
     }
 }
