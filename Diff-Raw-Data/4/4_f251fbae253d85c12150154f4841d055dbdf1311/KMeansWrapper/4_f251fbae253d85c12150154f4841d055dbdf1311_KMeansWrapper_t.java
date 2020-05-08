 package game_phase;
 
 import java.util.Arrays;
 
 import net.sf.javaml.clustering.KMeans;
 import net.sf.javaml.clustering.evaluation.ClusterEvaluation;
 import net.sf.javaml.clustering.evaluation.SumOfSquaredErrors;
 import net.sf.javaml.core.Dataset;
 import net.sf.javaml.core.DefaultDataset;
 import net.sf.javaml.core.DenseInstance;
 import net.sf.javaml.core.Instance;
 
 public class KMeansWrapper {
 	
 	private double[][][] clusterArr;
 	private int numClusters, numIterations;
 	private double[][] designMatrix;
 	
 	private double[][] centroids;
 	private boolean hasClustered = false;
 	
 	
 	/** 
 	 * Constructor!
 	 * @param clusters number of clusters
 	 * @param iterations number of iterations for which to run the algorithm
 	 * @param features each double array is a coordinate (a feature)
 	 */
 	public KMeansWrapper(int clusters, int iterations, double[][] features) {
 		designMatrix = doubleArrayCopy(features); //don't trust the client--ever
 		numClusters = clusters;
 		numIterations = iterations;
 		centroids = null; //centroids need to be populated after clustering -- null used as sentinel
 	}
 	
 	/** 
 	 *  @return Out to in--which cluster, which feature, the coordinate of the feature
 	 *  <br><b> clusterArr[out][middle][in]</b> <p> 
 	 *  
 	 *  Example:
 	 *  clusterArr[0][clusterArr[0].length - 1][4] gives the value of the 5th (4+1) coordinate
 	 *  of the last datapoint clustered with the first centroid (0+1).
 	 */
 	public void cluster(){
 		Dataset dataset = new DefaultDataset();
 		for (int i = 0; i < designMatrix.length; i++){
 			Instance datapoint = new DenseInstance(designMatrix[i]);
 			dataset.add(datapoint);
 		}
 		KMeans kmeans = new KMeans(numClusters, numIterations);
 		Dataset[] dataSetClusters = kmeans.cluster(dataset);
 		//perhaps use CluserEvaluation class to score the data / quanitfy how separate the clusters are, etc...
 		//http://java-ml.sourceforge.net/content/cluster-evaluation
 		
 		//ClusterEvaluation sse= new SumOfSquaredErrors();
 		/* Measure the quality of the clustering */
 		//double score=sse.score(dataSetClusters);
 		//System.out.println("Score (where 0.0 is the best): " + score);
 		
 		clusterArr = dataSetToDoubleArrays(dataSetClusters);
 		hasClustered = true; //information for centroids calculation
 	}
 	
 	/**
 	 * Returns the centroids (REQUIRES HAVING CALLED .cluster()).
 	 * @return The resulting double[][] will look like <br><b>centroids[whichCentroid][whichCoordinate]</b>
 	 */
 	public double[][] centroids() {
 		if (!hasClustered) {
 			throw new IllegalStateException("Cluster must be called before asking for centroids");
 	 	}
 		if (centroids != null) return doubleArrayCopy(centroids);
 		
 		// ------------------ number of clusters ; dimension of the feature
		centroids = new double[clusterArr.length][];
 		for (int centroid = 0; centroid < centroids.length; centroid++) {
 			int numFeaturesForCentroid = clusterArr[centroid].length;
 			
 			//sum up all features in the centroid
			centroids[centroid] = new double[designMatrix[0].length];
 			for (int feature = 0; feature < numFeaturesForCentroid; feature++) {
 				addArrays(centroids[centroid], clusterArr[centroid][feature]);
 			}
 			
 			//average all centroids
 			divide(centroids[centroid], numFeaturesForCentroid);
 		}
 		
 		return centroids(); //use the method expecting centroids to be not null
 	}
 	
 	/** Vector addition--result in dest */
 	private void addArrays(double[] dest, double[] toAdd) {
 		if (dest == null || toAdd == null)
 			throw new IllegalArgumentException("need non-null arrays to do calculations");
 		if (dest.length != toAdd.length)
 			throw new ArrayIndexOutOfBoundsException("two arrays have different lengths--cannot add");
 		
 		for (int i = 0; i < dest.length; i++)
 			dest[i] += toAdd[i];
 	}
 
 	/** Element-wise division of dividend by divisor */
 	private void divide(double[] dividend, double divisor) {
 		if (dividend == null)
 			throw new IllegalArgumentException("need non-null arrays to do calculations");
 		
 		for (int i = 0; i < dividend.length; i++)
 			dividend[i] /= divisor;
 	}
 	
 	/** Returns a deep copy of the double[][] arr */
 	private double[][] doubleArrayCopy(double[][] arr) {
 		if (arr == null) return null;
 		
 		double[][] copy = new double[arr.length][];
 		for (int i = 0; i < copy.length; i++) {
 			if (arr[i] == null) {
 				copy[i] = null;
 				continue;
 			}
 			copy[i] = Arrays.copyOf(arr[i], arr[i].length);
 		}
 		
 		return copy;
 	}
 
 	/** 
 	 * Parse the dataset array into clusters (of the format returned above).
 	 * Each Instance seems to be internally represented as a 
 	 * Map<array index, value at that index>. 
 	 */
 	private static double[][][] dataSetToDoubleArrays(Dataset[] dataSetClusters) {
 		double[][][] clusters = new double[dataSetClusters.length][][];
 		
 		for (int cluster = 0; cluster < clusters.length; cluster++) {
 			Dataset clusterDs = dataSetClusters[cluster];
 			
 			clusters[cluster] = new double[clusterDs.size()][];
 			for (int feature = 0; feature < clusters[cluster].length; feature++) {
 				Instance datapoint = clusterDs.get(feature);
 				
 				clusters[cluster][feature] = new double[datapoint.noAttributes()];
 				for (int coordinate = 0; coordinate < clusters[cluster][feature].length; coordinate++) {
 					clusters[cluster][feature][coordinate] = datapoint.get(coordinate);
 				}
 			}
 		}
 			
 		return clusters;
 	}
 	
 	
 	/** BASIC TEST CODE */
 	public static void main(String[] args) {
 		//very basic test code -- to see output
 		double[][] designMatrix = { {0, 1, 2}, {1, 0, 2}, {0, -1, -2}, {-1, 0, -2} };
 		int numClusters = 2;
 		int numIterations = 100;
 		
 		KMeansWrapper kmw = new KMeansWrapper(numClusters, numIterations, designMatrix);
 		kmw.cluster();
 		kmw.centroids();
 		
 		System.out.println("The clusters: " + Arrays.deepToString(kmw.clusterArr));
 		System.out.println("Centroids: " + Arrays.deepToString(kmw.centroids()));
 	}	
 }
 
 
