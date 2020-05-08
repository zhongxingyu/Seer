 package edificios.engineimplementation;
 
 import gps.GPSEngine;
 import gps.GPSNode;
 import gps.api.GPSProblem;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedList;
 
 public class BuildingsHybridIDFSEngine extends GPSEngine {
 	
	private static final int DEPTH_TO_SWITCH_TO_DFS = 4;
 	
 	private static final int MODE_DFS = 0;
 	private static final int MODE_BFS = 1;
 	
 	private Collection<GPSNode> frontieerNodes = new HashSet<GPSNode>();
	private int mode; 
	private int currentMaxDepth;
	// used for the DFS algorithm
	private int index = 0, indexDepth = 0;
 	
 	@Override
 	public void engine(GPSProblem myProblem) {
 		mode = MODE_BFS;
		currentMaxDepth = 1;
 		frontieerNodes.clear();
 		super.engine(myProblem);
 	}
 	
 	@Override
 	public void addNode(GPSNode node) {
 		switch(mode) {
 			case MODE_DFS:
				if (indexDepth != node.getDepth()) {
					indexDepth = node.getDepth();
					index = 0;
				}
				((LinkedList<GPSNode>) open).add(index, node);
				index++;
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
			// again with a deeper search starting from the frontieer modes
			// returned by the BFS
			resetToInitialState();
			open.add(new GPSNode(problem.getInitState(), 0));
 			currentMaxDepth++;
 			return true;
 		}
 		return super.explode(node);
 	}
 	
	private void resetToInitialState() {
		index = 0;
		indexDepth = 0;
		open.clear();
	}
	
 	@Override
 	public String getStrategyName() {
 		return "Hybrid Iterative DFS";
 	}
 }
