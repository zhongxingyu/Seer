 package edificios.engineimplementation;
 
 import gps.GPSEngine;
 import gps.GPSNode;
 import gps.api.GPSProblem;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedList;
 
 public class BuildingsHybridIDFSEngine extends GPSEngine {
 	
	private static final int DEPTH_TO_SWITCH_TO_DFS = 5;
 	
 	private static final int MODE_DFS = 0;
 	private static final int MODE_BFS = 1;
 	
 	private Collection<GPSNode> frontieerNodes = new HashSet<GPSNode>();
	private int mode = MODE_BFS; 
	private int currentMaxDepth = 1;
 	
 	@Override
 	public void engine(GPSProblem myProblem) {
 		mode = MODE_BFS;
 		frontieerNodes.clear();
 		super.engine(myProblem);
 	}
 	
 	@Override
 	public void addNode(GPSNode node) {
 		switch(mode) {
 			case MODE_DFS:
				((LinkedList<GPSNode>) open).addFirst(node);
 				break;
 			case MODE_BFS:
 				if (!open.contains(node)) {
 					open.add(node);
 				}
 				break;
 		}
 	}
 	
 	@Override
 	protected boolean explode(GPSNode node) {
 		if (mode == MODE_BFS) {
 			if (node.getDepth() == DEPTH_TO_SWITCH_TO_DFS) {
 				mode = MODE_DFS;
 				currentMaxDepth = node.getDepth() + 1;
 				frontieerNodes.addAll(open);
 				System.out.println("switching to DFS!");
 				return false;
 			} else {
 				return super.explode(node);
 			}
 		}
 		if (node.getDepth() >= currentMaxDepth) {
 			// when bottom of tree is reached, clean open list to start  
			// again with a deeper search
			open.clear();
			open.addAll(frontieerNodes);
 			currentMaxDepth++;
 			return true;
 		}
 		return super.explode(node);
 	}
 	
 	@Override
 	public String getStrategyName() {
 		return "Hybrid Iterative DFS";
 	}
 }
