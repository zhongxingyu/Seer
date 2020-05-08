 package edu.ntnu.EASY.selection.parent;
 
 import edu.ntnu.EASY.Population;
 import edu.ntnu.EASY.individual.Individual;
 import edu.ntnu.EASY.util.Util;
 
 public class SigmaScaledSelector<PType> implements ParentSelector<PType> {
 
 	int numParents;
 	
 	public SigmaScaledSelector(int numParents) {
 		this.numParents = numParents;
 	}
 
 	@Override
 	public <GType> Population<GType, PType> select(Population<GType, PType> population) {
 		double totalFitness = 0;
 		
 		for (Individual<GType, PType> individual : population) {
 			totalFitness += individual.getFitness();
 		}
 		population.sort(true);
 		
 		//A list of intervals. [0, 1).
 		double[] intervals = new double[population.size()];
 		double previousEndpoint = 0;
 		double fitness;
 		double meanFitness = totalFitness / population.size();
 		double sigmaFitness;
 		double fitnessVariance = Util.getFitnessVariance(population, meanFitness);
 		
 		//Put each endpoint into a linked list.
 		for (int adultIndex = 0; adultIndex < population.size(); adultIndex++) {
 			fitness = population.get( adultIndex ).getFitness();
 			sigmaFitness = 1 + ( ( fitness - meanFitness ) / ( 2 * fitnessVariance) ); 
 			intervals[adultIndex] = sigmaFitness + previousEndpoint;
 			previousEndpoint += sigmaFitness;
 		}
 		
 		//Normalize the sigma values so the intervals are in the range [0,1]
		for (Double interval : intervals) {
 			//The value of the last end point is the sum of all the sigmaFitness values.
			interval /= previousEndpoint;
 		}
 		return FitnessProportionateSelector.getParentList(population, intervals, numParents);
 	}
 }
