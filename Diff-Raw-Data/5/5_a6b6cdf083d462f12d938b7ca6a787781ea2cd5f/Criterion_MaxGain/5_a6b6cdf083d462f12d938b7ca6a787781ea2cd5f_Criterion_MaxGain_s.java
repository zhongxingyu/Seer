 import java.util.ArrayList;
 
 public class Criterion_MaxGain implements Criterion {
 
 	private static double LOG2 = Math.log(2);
 	
 	private double calculateEntropy(ArrayList<Entry> entries) {
 		int countZero = 0;
 		int countOne = 0;
 		
 		for (Entry e : entries) {
 			if (e.label == 0)
 				++countZero;
 			else
 				++countOne;
 		}
 		
 		double p0 = (double)(countZero)/((double)entries.size()); 
 		double p1 = (double)(countOne)/((double)entries.size()); 
 				
		return (p0*Math.log(p0)+p1*Math.log(p1))/LOG2; 
 	}
 	
 	@Override
 	public double calculateSplitPerf(ArrayList<Entry> lchild,
 			ArrayList<Entry> rchild, ArrayList<Entry> all) {
 		
 		double entropyBefore = calculateEntropy(all);
 		double entropyAfter = calculateEntropy(lchild) + calculateEntropy(rchild);
 		
 		return entropyBefore - entropyAfter;
 	}
 
 }
