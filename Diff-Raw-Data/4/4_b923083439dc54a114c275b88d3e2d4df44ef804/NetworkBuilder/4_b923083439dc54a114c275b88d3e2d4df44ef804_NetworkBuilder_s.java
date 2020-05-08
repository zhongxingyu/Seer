 package callgraphanalyzer;
 
 import java.util.List;
 
 import models.Commit;
 import models.CommitTree;
 import models.Node;
 import db.CallGraphDb;
 
 public class NetworkBuilder
 {
 	private Comparator 			compare;
 	private CallGraphAnalyzer 	cga;
 	private CallGraphDb 		db;
 	private CommitTree			ct;
 	
 	private String				startCommit;
 	private String				endCommit;
 
 	public NetworkBuilder(CallGraphDb db, CommitTree ct, String startCommit)
 	{
 		super();
 		this.db = db;
 		this.ct = ct;
 		this.startCommit = startCommit;
 		
 		// Initialize both CGs to the start commit for updating
 		compare = new Comparator(db, startCommit, startCommit);
 	}
 	
 	public void buildAllNetworks() {
 		traverseCommitTree(this.ct.getRoot());
 	}
 	
 	private void traverseCommitTree(Node parent) {
 		while(parent != null) {
 			List<Node> children = parent.getChildren();
 			
 			if(children.size() == 1) {
 				if(!isMergeCommit(children.get(0).getCommitID())) {
 					compare.newCallGraph = compare.forwardUpdateCallGraph(compare.newCallGraph, children.get(0).getCommitID());
 					buildNetwork(parent.getCommitID(), children.get(0).getCommitID());
 					compare.oldCallGraph = compare.forwardUpdateCallGraph(compare.oldCallGraph, children.get(0).getCommitID());
 				}
 				parent = children.get(0);
 			}
 			
 			else if(children.size() > 1) {
 				for(Node child: children) {
 					if(!isMergeCommit(child.getCommitID())) {
 						compare.newCallGraph = compare.forwardUpdateCallGraph(compare.newCallGraph, child.getCommitID());
 						buildNetwork(parent.getCommitID(), child.getCommitID());
 						compare.oldCallGraph = compare.forwardUpdateCallGraph(compare.oldCallGraph, child.getCommitID());
 					}
 					else {
 						compare.newCallGraph = compare.forwardUpdateCallGraph(compare.newCallGraph, child.getCommitID());
 						compare.oldCallGraph = compare.forwardUpdateCallGraph(compare.oldCallGraph, child.getCommitID());
 					}
 					
 					traverseCommitTree(child);
 					
 					//Restore
 					compare.newCallGraph = compare.batchReverseUpdate(compare.newCallGraph, this.ct, parent.getCommitID());
 					compare.oldCallGraph = compare.batchReverseUpdate(compare.oldCallGraph, this.ct, parent.getCommitID());
 				}
 				parent = null;
 			}
 			
 			else {
 				parent = null;
 			}
 		}
 	}
 	
 	private boolean isMergeCommit(String commitID) {
 		List<Commit> parents = db.getCommitParents(commitID);
 		return parents.size() > 1;
 	}
 	
 	private void buildNetwork(String initial, String change) {
 		System.out.println("Generating network for: " + initial + " - " + change);
 		//compare = new Comparator(db, initial, change);
 		cga = new CallGraphAnalyzer();
 		
 		// Update the comparator with the commits
 		compare.updateCGVariables(initial, change);
 		
 		System.out.println("Comparing Commits...");
 		compare.CompareCommits(initial, change);
 		cga.init(compare);
 		
 		System.out.println("Generating the relationships...");
 		cga.generateRelationships();
 		cga.exportRelations();
 		System.out.println();
 	}
 }
