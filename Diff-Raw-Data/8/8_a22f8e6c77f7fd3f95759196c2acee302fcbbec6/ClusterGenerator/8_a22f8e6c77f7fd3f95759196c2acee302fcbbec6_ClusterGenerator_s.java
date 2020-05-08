 package edu.umich.eecs.data;
 
 import java.util.List;
 import java.util.Set;
 
 import edu.umich.eecs.ClusterFinder;
 import edu.umich.eecs.dto.Cell;
 import edu.umich.eecs.dto.Cluster;
 import edu.umich.eecs.dto.DataSetType;
 import edu.umich.eecs.dto.OscillatingCellTowerPair;
 import edu.umich.eecs.service.ClusterService;
 import edu.umich.eecs.service.OscillationService;
 import edu.umich.eecs.util.Tic;
 /**
  * This class computes clusters out oscillation 
  * edges and persist them into database.
  * @author Mehrdad
  *
  */
 
 public class ClusterGenerator {
 	public static Tic clock = new Tic(true);
 
 	public static void computeCluster(DataSetType dataset){
 		OscillationService os= new OscillationService(dataset);
 		
 		clock.tic();
 		List<OscillatingCellTowerPair> oEdges=os.getOrderedOscillationPairs();
 		clock.toc("First Step:Obtained " + oEdges.size() + " Edges ");
 		
 		clock.tic();
 		Set<Cell> oCells=os.getAllOsiCells();
 		clock.toc("Second Step: Obtained " + oEdges.size() + " Vertices ");
 		
 		clock.tic();
 		ClusterFinder cf= new ClusterFinder(oEdges, oCells, new ClusterService(), dataset);
 		clock.toc("Third Step: Oscillation Graph is Created ");
 		
 		clock.tic();
 		cf.makeCluster();
 		clock.toc("Forth Step: Clusters Are Created and Mostly Persisted ");
 	}
 	
 	
 	public static void main(String[] args) {
 		
 		ClusterGenerator.computeCluster(DataSetType.SampledRealityMining);
 		ClusterGenerator.computeCluster(DataSetType.RealityMining);
 		ClusterGenerator.computeCluster(DataSetType.NokiaChallenge);	
 	}
 
 }
