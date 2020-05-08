 package org.bh.validation;
 
 import java.util.Iterator;
 import java.util.Map;
 
 public class ValidateStochastic {
 
 	@SuppressWarnings("unchecked")
 	public static boolean validateRandomWalk(Map<String, Double> internalMap) {
 		boolean allZero = false;
 		boolean allOne = false;
 		Iterator iterator = internalMap.entrySet().iterator();
 		int mapsize = internalMap.size();
 
 		for (int i = 0; i < mapsize; i++) {
 
 			Map.Entry entry = (Map.Entry) iterator.next();
 			String key = (String) entry.getKey();
 
 			if (key.contains("chance")) {
 				double value = (Double) entry.getValue();
 				if (value == 0 && allOne == false) {
 					allZero = true;
 				} else if (value == 1 && allZero == false) {
 					allOne = true;
 				} else {
 					allZero = allOne = false;
 					break;
 				}
 			}
 		}
 		if (allZero == true || allOne == true) {
 			return false;
 		}
 		return true;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static boolean validateWienerProcess(Map<String, Double> internalMap) {
 		boolean allZero = false;
 		Iterator iterator = internalMap.entrySet().iterator();
 		int mapsize = internalMap.size();
 
 		for (int i = 0; i < mapsize; i++) {
 
 			Map.Entry entry = (Map.Entry) iterator.next();
 			String key = (String) entry.getKey();
 
			if (key.contains("slope") || key.contains("standard_deviation")) {
 				double value = (Double) entry.getValue();
 				if (value == 0) {
 					allZero = true;
 				} else {
 					allZero = false;
 					break;
 				}
 			}
 		}
 		if (allZero == true) {
 			return false;
 		}
 		return true;
 	}
 }
