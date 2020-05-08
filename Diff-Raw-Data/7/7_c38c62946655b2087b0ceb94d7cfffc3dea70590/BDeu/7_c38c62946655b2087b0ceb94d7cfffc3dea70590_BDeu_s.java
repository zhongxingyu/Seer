 import java.util.ArrayList;
 
 import org.apache.commons.math3.special.Gamma;
 
 
 public class BDeu extends Scorer{
 
 	public BDeu(int[][] data, int alleleStates, int diseaseStates, double a) {
 		super(data, alleleStates, diseaseStates, a);
 	}
 
 	
 	@Override
 	
 	// produces the BDeu score of the BN where q=number of configurations, r=DiseaseStates, 
 	// NJK=number of times parents takes jth configuration and X=k, alpha= given alpha value
 
 	
 	double score(ArrayList<Integer> parents) {
 		double score = 1;
 		double term1=0, term2=1, NJK = 0;
 		
 		ArrayList<TreeNode> configCounts = getParentConfigurationCounts(parents);
 		
 		double alpha_numconfigs = alpha/getNumConfigurations();
 		double alpha_disease_numconfigs = alpha/(DiseaseStates*getNumConfigurations());
 		
 		for (int j=0; j<getNumConfigurations(); j++)
 		{
 			double sumNJK=0;
 			for (int k=0; k<DiseaseStates; k++)
 			{
 				NJK = configCounts.get(j).diseaseCounts[k];
 				sumNJK = sumNJK + NJK;
 			}
 			
 			double denominator_term1 = Gamma.logGamma(alpha_numconfigs + sumNJK);
 			
 			term1 = Gamma.logGamma(alpha_numconfigs)-denominator_term1;
 			term1 = Math.exp(term1);
			// term one of BDeu formula
 			
 			term2 = 1;
 			
 			for (int k=0; k<DiseaseStates; k++)
 			{ 
 				NJK = configCounts.get(j).diseaseCounts[k];
 				double numerator_term2 = Gamma.logGamma(alpha_disease_numconfigs+NJK);
 				
 				term2 = term2*(Math.exp(numerator_term2 - Gamma.logGamma(alpha_disease_numconfigs)));
				// term2 of BDeu formula
 			}
 			
 			if (Double.isInfinite(term2))
 			// check if term2 is infinity
 			{
 				term2 = Double.MAX_VALUE;
 			}
 			
 			// finally, find the product of all the terms
 			score = score * term1 * term2;	
 		}
 		return score;
 	}
 
 	@Override
 	double getProbOfData(double score) {
 		return score;
 	}
 }
