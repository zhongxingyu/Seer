 package mesquite.treecmp.clustering;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Random;
 
 public class IterativeClusteringAlgorithm<TreeType> {
 	private final ClusterCentresCalculation<TreeType> centresCalculation;
 	
 	public IterativeClusteringAlgorithm(ClusterCentresCalculation<TreeType> centresCalculation) {
 		this.centresCalculation = centresCalculation;
 	}
 	
 	public Collection<Collection<Integer>> computeClusters(List<TreeType> trees, int numberOfClusters, int numberOfIterations) {
 		final List<Collection<Integer>> associations = new ArrayList<Collection<Integer>>();
 		
 		List<TreeType> means = new ArrayList<TreeType>(numberOfClusters);
 		
 		final int[] randomIndices = drawNNumbers(numberOfClusters, trees.size()); 
 		for (int i=0; i<numberOfClusters; i++)
 			means.add(trees.get(randomIndices[i]));
 		double error = computeAssociations(trees, means, associations), newError;
 
 		int iterations_left = numberOfIterations;
 		do {
 			final List<TreeType> newCenters = centresCalculation.computeCentres(associations);
 			
 			newError = computeAssociations(trees, newCenters, associations);
 			
 			if (newError < error)
 				means = newCenters;
 			else
 				break;
 			
 			error = newError;
 			
 			iterations_left--;
 		} while (iterations_left>0);
 		
 		if (newError > error)
 			computeAssociations(trees, means, associations);
 		
 		return associations;
 	}
 			
 	private double computeAssociations(List<TreeType> trees, List<TreeType> centers,
 			List<Collection<Integer>> associations) {
 		associations.clear();
 		for (int i=0; i<centers.size(); i++)
 			associations.add(new ArrayList<Integer>());
 		
 		double error = Double.MIN_VALUE;
 		
 		for (int i=0; i<trees.size(); i++) {
 			int closestCenterIndex = 0;
 			double distanceToClosest = centresCalculation.getDistanceFromCenterToTree(centers.get(closestCenterIndex), trees.get(i));
 			
 			for (int j=1; j<centers.size(); j++) {				
 				final double distance = centresCalculation.getDistanceFromCenterToTree(centers.get(j), trees.get(i));
 				if (distance < distanceToClosest) {
 					distanceToClosest = distance;
 					closestCenterIndex = j;
 				}
 			}
 			associations.get(closestCenterIndex).add(i);
 			if (distanceToClosest > error)
 				error = distanceToClosest;
 		}
		for (int i=associations.size()-1; i>=0; i-=1) {
			if (associations.get(i).isEmpty()) {
				associations.remove(i);
			}
		}
 		return error;
 	}
 
 	private int[] drawNNumbers(int n, int maxValue) {		
 		final int[] result = new int[n];
 		final Random r = new Random();
 		for (int i=0; i<n; i++) {
 			final int proposal = r.nextInt(maxValue);
 			if (contains(result, proposal))
 				i--;
 			else
 				result[i] = proposal;
 		}
 		return result;
 	}
 	
 	private boolean contains(int[] array, int value) {
 		for (final int item : array)
 			if (item == value)
 				return true;
 		return false;
 	}
 }
