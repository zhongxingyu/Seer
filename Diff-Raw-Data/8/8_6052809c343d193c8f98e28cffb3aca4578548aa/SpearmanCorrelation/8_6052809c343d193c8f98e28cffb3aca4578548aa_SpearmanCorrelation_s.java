 package math.ranks;
 
 import java.util.Map;
 
 /**
  * Implementation of Spearman Rank's Correlation coefficient
  * 
  * @author javier
  *
  */
 public class SpearmanCorrelation extends RankingComparator {
 
 	public double compare(Map<Integer,Integer> rank1, Map<Integer,Integer> rank2) {
 		
 		double d2 = sumDiffSq(rank1, rank2);
 		
		System.err.println("d2 = "+ d2);
		
 		double n = (double) rank1.size();
 		
		System.err.println("n = " + n);
		
 		double nn_1 = n*(Math.pow(n, 2)-1);
 		
		System.err.println("n = " + nn_1);
		
 		return 1 - (6*d2)/nn_1;
 		
 	}
 	
 	
 	private double sumDiffSq(Map<Integer,Integer> rank1, Map<Integer,Integer> rank2) {
 		
 		double sumDiff = 0;
 		
 		for (Integer label : rank1.keySet()) {
 			
 			sumDiff = sumDiff + (double) Math.pow(rank1.get(label) - rank2.get(label),2);
 			
 		} 		
 		
 		return sumDiff;
 		
 	}
 
 }
