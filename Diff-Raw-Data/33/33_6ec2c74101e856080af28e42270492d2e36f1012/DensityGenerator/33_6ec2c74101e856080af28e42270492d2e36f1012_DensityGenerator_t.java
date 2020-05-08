 package edu.umich.eecs.data;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javassist.expr.NewArray;
 
 import edu.umich.eecs.dto.AreaDensity;
 import edu.umich.eecs.dto.Cell;
 import edu.umich.eecs.dto.CellSpan;
 import edu.umich.eecs.dto.Cluster;
 import edu.umich.eecs.dto.DataSetType;
 import edu.umich.eecs.service.AreaDensityService;
 import edu.umich.eecs.service.CellService;
 import edu.umich.eecs.service.CellSpanService;
 import edu.umich.eecs.service.CellSpanServiceInterface;
 import edu.umich.eecs.service.ClusterService;
import edu.umich.eecs.service.MDCCellSpanService;
import edu.umich.eecs.service.SampledCellSpanService;
 
 public class DensityGenerator {
 	
 	ArrayList<AreaDensity> listAreaDensity;
 	AreaDensityService aSrv;
 	int numberOfPersistence;
 	
 
 	public void computeDensity(CellSpanServiceInterface cSpaninf, DataSetType dataset){
 		
 		listAreaDensity= new ArrayList<>();
 		aSrv= new AreaDensityService();
 		numberOfPersistence=1;
 		List<Integer> areas= cSpaninf.getAllAreaID();
 		System.out.println("DataSet is: "+dataset.toString()+" There is #"+areas.size()+" areas");
 		
 		Map<Integer,Long> cc=new ClusterService().getCountOfClustersByAreaID(dataset, areas);
 		Map<Integer,Long> uc= new ClusterService().getCountUnclusteredCellbyAreaID(dataset, areas);
 		Map<Integer,Long> ac= cSpaninf.getCountAllCellsByAreaID(areas);
 		for(Integer i: areas){
 			
 			double numberOfCluster= cc.get(i)+uc.get(i);
 			double numberOfCells=ac.get(i);
 			double density= numberOfCells/numberOfCluster;
 			AreaDensity ad= new AreaDensity(i, (int)numberOfCluster, dataset, (int)numberOfCells,density );
 			addDensity(ad);
 		}	
 		System.out.println("#"+(50*numberOfPersistence+listAreaDensity.size())+" Area Densities is Persisted");
 		listAreaDensity.clear();
 	}
 	public void addDensity(AreaDensity ad){
 		
 		listAreaDensity.add(ad);
 		if(listAreaDensity.size()==50){
 			aSrv.saveListToAD(listAreaDensity);
 			listAreaDensity.clear();
 			System.out.println("#"+50*(numberOfPersistence++)+" Area Densities is Persisted");
 		
 		}
 	}
 	
 	
 	
 	public static void main(String[] args) {
 		
		new DensityGenerator().computeDensity(new MDCCellSpanService(), DataSetType.NokiaChallenge);
 		new DensityGenerator().computeDensity(new CellSpanService(), DataSetType.RealityMining);
		new DensityGenerator().computeDensity(new SampledCellSpanService(), DataSetType.SampledRealityMining);
		
 	}
 
 }
