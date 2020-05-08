 package library;
 
 /**
  * The mutation operator. Extending it and overriding mutate allows users to create custom operators
  * 
  * @author Roma
  * 
  */
 public class Mutation {
 	
 	/**
 	 * Mutate a genetic program
 	 * 
 	 * @param gp The genetic program to mutate
 	 * @param config config to use
 	 */
 	public void mutate(GeneticProgram gp, GPConfig config) {
 
 		// Get a random node and the parent of that node
 		// need to get parent index here as well
 		int p = (int) Math.abs(config.randomNumGenerator.nextInt() % config.getNumRoots());
 		Node tmp = gp.getRandomNode(p, config);
 		Function parent = (Function) (tmp.getParent());
 
 		// Generate a new tree to replace the old one
		Node newTree = config.programGenerator.createGrowProgram(tmp.getDepth() - 1, config.maxDepth() ,
 				tmp.getReturnType());
 
 		// If parent is not NULL then newTree is not at the root of
 		// the tree
 		if (parent != null) {
 			int i;
 			for (i = 0; i < parent.getNumArgs(); i++) {
 				if (tmp == parent.getArgN(i)) break;
 			}
 
 			parent.setArgN(i, newTree);
 		} else {
 			// Need to use root index here
 			gp.setRoot(newTree, p);
 		}
 
 		// Delete the old tree
 		gp.deleteTree(tmp);
 		// Recompute the size and depth
 		gp.computeSizeAndDepth();
 	}
 
 }
