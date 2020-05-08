 /**
  * 
  */
 package rtree;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import spatialindex.IEntry;
 import spatialindex.IQueryStrategy;
 
 /**
  * @author chenqian
  *
  */
 public class Records {
 
 	private HashSet<Integer> nodes = new HashSet<Integer>();  
 	private HashSet<Integer> visited = null;
 	
 	public void add(Integer id) {
 		nodes.add(id);
 	}
 	
 	public void clear() {
 		nodes.clear();
 	}
 	
	public ArrayList<Integer> getDataIds (RTree tree) {
 		visited = new HashSet<Integer>();
 		RetrieveStrategy qs = new RetrieveStrategy();
		tree.queryStrategy(tree.getRootId(), qs);
 		return qs.getIds();
 	}
 	
 	public HashSet<Integer> getVisitedIds() {
 		return nodes;
 	}
 	
 	
 	/**
 	 * Record the revised nodes.
 	 * 
 	 */
 	public Records() {
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
 	}
 	
 	class RetrieveStrategy implements IQueryStrategy {
 
 		private ArrayList<Integer> 	toVisit = new ArrayList<Integer>();
 		private ArrayList<Integer> ids = new ArrayList<Integer>();
 		
 		public ArrayList<Integer> getIds() {
 			return ids;
 		}
 		
 
 		/* (non-Javadoc)
 		 * @see memoryindex.IQueryStrategy#getNextEntry(memoryindex.BinaryTree, memoryindex.BinaryTree[], boolean[])
 		 */
 		@Override
 		public void getNextEntry(IEntry e, int [] next, boolean[] hasNext) {
 			// TODO Auto-generated method stub
 			Node node = (Node) e;
 			if (!visited.contains(node.getIdentifier())) {
 				visited.add(node.getIdentifier());
 				for (int i = 0; i < node.getChildrenCount(); i ++) {
 					int cId = node.getChildIdentifier(i);
 					if (node.isLeaf()) {
 						ids.add(node.getChildIdentifier(i));
 					} else {
 						toVisit.add(cId);
 					}
 				}
 			}
 
 			if (!toVisit.isEmpty()) {
 				next[0] = toVisit.remove(0);
 				hasNext[0] = true;
 			} else {
 				hasNext[0] = false;
 			}
 		}
 
 	}
 
 }
