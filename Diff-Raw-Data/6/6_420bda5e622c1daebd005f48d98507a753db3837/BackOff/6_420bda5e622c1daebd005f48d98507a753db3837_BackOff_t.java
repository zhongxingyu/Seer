 import java.util.Map;
 
 
 public class BackOff {
 	
 	/**
 	 * Private C'tor for static class
 	 */
 	private BackOff() {}
 	
 	/**
 	 * Method calculates backOff probability
 	 * @param tuple
 	 * @param tupleMap
 	 * @param eventsMap
 	 * @param lamda
 	 * @return
 	 */
 	public static double calculateBackOff(Tuple<String,String> tuple, Map<Tuple<String,String>,Integer> tupleMap, Map<String,Integer> eventsMap, double lamda) {
 		
 		// Seen tuple
 		if (tupleMap.containsKey(tuple)) {
 			return (Lidstone.computeBigramLidstoneValueOfEvent(tupleMap, tupleMap.keySet().size(), tuple, lamda));
 		// Unseen tuple
 		} else {
 			return (computeAlpha(tuple.getFirst(), tupleMap, lamda, eventsMap, tupleMap.keySet().size()) *
 					Lidstone.computeUnigramLidstoneValueOfEvent(eventsMap, eventsMap.keySet().size(), tuple.getSecond(), ApplicationConstants.LAMDA1, true));
 		}
 	}
 	
 	/**
 	 * Method computes the coefficient alpha for the unseen tuples in the back-off calculation.
 	 * @param word
 	 * @param tupleMap
 	 * @param lamda
 	 * @param eventsMap
 	 * @param eventsMapSize
 	 * @return
 	 */
 	public static double computeAlpha(String word, Map<Tuple<String,String>,Integer> tupleMap, double lamda, Map<String,Integer> eventsMap, int eventsMapSize) {
 		double nominator = 0.0;
 		double denominator = 0.0;
 		
 		
 		// Iterate all the tuples that appear at least one time with the given word appears first
 		for (Tuple<String,String> tuple : tupleMap.keySet()) {
 			
 			if (tuple.getFirst().equals(word)) {
				nominator += Lidstone.computeBigramLidstoneValueOfEvent(tupleMap, eventsMapSize, tuple, lamda);
				denominator += Lidstone.computeUnigramLidstoneValueOfEvent(eventsMap, eventsMapSize, 
						tuple.getSecond(), ApplicationConstants.LAMDA1, true);
 			}
 		}
 		
 		return ((1.0 - nominator) / (1.0 - denominator));
 	}
 
 }
