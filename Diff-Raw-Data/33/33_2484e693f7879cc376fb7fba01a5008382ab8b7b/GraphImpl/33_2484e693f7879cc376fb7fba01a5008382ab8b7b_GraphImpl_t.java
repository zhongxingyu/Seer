 package adp2.implementations;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import com.mxgraph.view.mxGraph;
 
 import adp2.interfaces.*;
 import static adp2.implementations.Values.*;
 
 public class GraphImpl extends mxGraph implements Graph {
 
 	private final Matrix<Double> distance;
 	private MutableMatrix<Double> pheromones;
 	private final HashMap<Integer,Object> VertexObjectList = new HashMap<Integer,Object>();
 	private final HashMap<Integer,Object> EdgeObjectList = new HashMap<Integer,Object>();
 
 	private GraphImpl(Matrix<Double> distance, MutableMatrix<Double> pheromones) {
 		this.distance = distance;
 		this.pheromones = pheromones;
 	}
 
 	public static Graph valueOf(Matrix<Double> distance) {
 		
 		if (distance == null) {
 			return Values.NaG();
 		}
 		return new GraphImpl(distance, calcPheromoneMatrix(distance.width(), distance.height()));
 	}	
 	
 	private static MutableMatrix<Double> calcPheromoneMatrix(int width, int height){
 		List<Double> resultList=new ArrayList<Double>();
 		for(int i=0; i<(width*height); i++){
 			resultList.add(0.);
 		}
 		return mutableMatrix(width, height, resultList);
 	}
 	
 	public void highlightPath(Path p) {
 	}
 
 	@Override
 	public double distance(int von, int nach) {
 		double d;
 		try {
 			d = distance.get(von - 1, nach - 1);
 		} catch (ArrayIndexOutOfBoundsException e) {
 			d = Double.POSITIVE_INFINITY;
 		}
 		return d;
 	}
 
 	@Override
 	public Set<Integer> neighbors(int node) {
 		Set<Integer> result = new HashSet<Integer>();
 		if (node <= 0 || node > distance.height()) {
 			return result;
 		}
 
 		for (int i = 0; i < distance.width(); i++) {
 			if (!distance.get(node - 1, i).isInfinite() && i != node - 1) {
 				result.add(i + 1);
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public Set<Integer> allNodes() {
 		Set<Integer> result = new HashSet<Integer>();
 		for (int i = 1; i <= distance.height(); i++) {
 			result.add(i);
 		}
 		return result;
 	}
 
 	@Override
 	public double intensity(int von, int nach) {
 		double p;
 		try {
 			p = pheromones.get(von - 1, nach - 1);
 		} catch (ArrayIndexOutOfBoundsException e) {
 			p = Double.POSITIVE_INFINITY;
 		}
 		return p;
 	}
 
 	@Override
 	public void decrementPheromone(double value) {
 		for (int i = 0; i < pheromones.width(); i++) {
 			for (int j = 0; j < pheromones.height(); j++) {
 				if (pheromones.get(i, j) - value < 0) {
 					pheromones.set(i, j, 0.);
 				} else {
 					pheromones.set(i, j, pheromones.get(i, j) - value);
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void incrementPheromone(int start, int end, double pheromone){
 			//0 und 1 -> Matrix Pos, 2 -> Pheromon Update
 			//Auf 0 und 1 wird -1 um von der externen Nummerierung (ab 1) auf die interne (ab 0) f�r die Matrix zu kommen
 			pheromones.set(start-1, end-1, pheromones.get(start-1, end-1)+pheromone);
 	}
 	
 //	@Override
 //	public void incrementPheromone(List<List<Integer>> pheromoneUpdateList){
 //		for(List<Integer> update : pheromoneUpdateList){
 //			//0 und 1 -> Matrix Pos, 2 -> Pheromon Update
 //			//Auf 0 und 1 wird -1 um von der externen Nummerierung (ab 1) auf die interne (ab 0) f�r die Matrix zu kommen
 //			pheromones.set(update.get(0)-1, update.get(1)-1,pheromones.get(update.get(0)-1, update.get(1)-1)+update.get(2));
 //			pheromones.set(update.get(1)-1, update.get(0)-1,pheromones.get(update.get(1)-1, update.get(0)-1)+update.get(2));
 //		}
 //	}
 	
 	public String toString(){
 		StringBuffer result = new StringBuffer();
 		result.append("Distance:\n");
 		result.append(distance.toString());
 		result.append("\n");
 		result.append("Pheromones:\n");
 		result.append(pheromones.toString());
 		result.append("\n");
 		
 		return result.toString();
 	}
 
 	@Override
 	public Graph deepClone() {
		return new GraphImpl(distance, pheromones.deepClone());
 	}
 
 	public Matrix<Double> distanceVar(){ return this.distance;}
 
 }
