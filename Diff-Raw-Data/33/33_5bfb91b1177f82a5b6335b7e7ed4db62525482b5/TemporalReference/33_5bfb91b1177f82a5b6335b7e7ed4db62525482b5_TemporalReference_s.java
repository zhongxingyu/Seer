 package edu.uw.cs.lil.tiny.tempeval;
 
 import java.util.Map;
 import java.util.Set;
 
 public class TemporalReference extends TemporalPredicate{
 	
 	private TemporalISO previous;
 
 	public TemporalReference(TemporalISO prev){
 		previous = prev;
 	}
 	
 	
 	public TemporalISO perform(){
 		if (previous == null || first.isSet("present_ref") || first.isSet("past_ref")
 				|| first.isSet("future_ref") || !(first instanceof TemporalDuration) || notSameFields())
 			return first;
 
 		
 		// TODO: Test here if first is a fully specified calendar date. If so, return it.
 		
 		Map<String, Set<Integer>> tmpMap = previous.getFullMapping();
 		
 		// For each thing set in first, add it's value to previous.
 		for (String s : first.getKeys()){
 			int firstValue = TemporalISO.getValueFromDate(first, s);
 			int prevValue = TemporalISO.getValueFromDate(previous, s);
 			if (firstValue + prevValue != 0){
 				tmpMap.get(s).remove(prevValue);
 				tmpMap.get(s).add(firstValue + prevValue);
 			}
 			
 		}
 		return new TemporalDate(tmpMap);
 	}
 	
 	 private boolean notSameFields(){
 		 for (String s : first.getKeys()){
 			 if (!previous.isSet(s))
 				 return true;
 		 }
 		 return false;
 	 }
 }
