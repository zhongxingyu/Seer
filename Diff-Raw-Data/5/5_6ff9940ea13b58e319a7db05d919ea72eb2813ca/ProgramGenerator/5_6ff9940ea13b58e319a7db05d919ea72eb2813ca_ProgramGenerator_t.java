 package library;
 
 import java.util.List;
 
 /**
  * Generates programs. Can generate both grow and full programs. Default behaviour is generate 50-50 but can be changed
  * 
  * @author Roma
  * 
  */
 public class ProgramGenerator {
 	private NodeVector<Node> growTable[];
 
 	private NodeVector<Node> fullTable[];
 
 	public ProgramGenerator() {
 	}
 
 	/**
 	 * Generate the initial population.
 	 * 
 	 * @param pop
 	 *            The list of programs to set trees for
 	 * @param expectedReturnType
 	 *            the array of return types for each root
 	 * @param config
 	 *            the config
 	 */
 	public void generateInitialPopulation(List<GeneticProgram> pop, int expectedReturnType[], GPConfig config) {
 		generateTables(config);
 		if (config.minDepth() > config.maxDepth()) throw new RuntimeException("minDepth is greater than maxDepth");
 
 		int numIndividualsForRamping = pop.size() / 2;
 		int numSizes = config.maxDepth() - config.minDepth() + 1;
 		int sizeInc = numIndividualsForRamping < numSizes ? numSizes / numIndividualsForRamping : 1;
 		int minSize = numIndividualsForRamping < numSizes ? config.minDepth() : config.maxDepth()
 				- (numIndividualsForRamping - 1);
 
 		int indiv;
 		int maxSize = minSize;
 		for (indiv = 0; indiv < numIndividualsForRamping; indiv++) {
 			if (maxSize < config.maxDepth() && (indiv % sizeInc) == 0) {
 				maxSize++;
 			}
 
 			for (int i = 0; i < config.getNumRoots(); i++) {
				Node tmp = createGrowProgram(1, maxSize, pop.get(indiv).getReturnType(i), config);
 				pop.get(indiv).setRoot(tmp, i);
 			}
 		}
 
 		maxSize = minSize;
 		for (; indiv < pop.size(); indiv++) {
 			if (maxSize < config.maxDepth() && (indiv % sizeInc) == 0) {
 				maxSize++;
 			}
 			for (int i = 0; i < config.getNumRoots(); i++) {
				Node tmp = createFullProgram(1, config.maxDepth(), pop.get(indiv).getReturnType(i),
 						config);
 				pop.get(indiv).setRoot(tmp, i);
 			}
 		}
 	}
 
 	/**
 	 * Create a program from curDept to maxDepth of full size
 	 * 
 	 * @param curDepth
 	 *            Current depth
 	 * @param maxDepth
 	 *            max program depth
 	 * @param expectedReturnType
 	 *            return type of this subtree
 	 * @param config
 	 *            config
 	 * @return Node that fits the requested description
 	 */
 	public Node createFullProgram(int curDepth, int maxDepth, int expectedReturnType, GPConfig config) {
 		int depth = maxDepth - curDepth;
 		Node node = fullTable[depth].generateRandomNode(expectedReturnType, config);
 
 		if (node == null) {
 			System.err.println("Warning, unable to create Full program for this set of Functions and Terminals");
 			return createGrowProgram(curDepth, maxDepth, expectedReturnType, config);
 		}
 
 		if (node.getNumArgs() > 0) {
 			Function func = (Function) (node);
 
 			for (int i = 0; i < func.getNumArgs(); i++) {
 				func.setArgN(i, createFullProgram(curDepth + 1, maxDepth, func.getArgNReturnType(i), config));
 			}
 		}
 
 		return node;
 	}
 
 	/**
 	 * Grow a program from curDept to maxDepth of random size
 	 * 
 	 * @param curDepth
 	 *            Current depth
 	 * @param maxDepth
 	 *            max program depth
 	 * @param expectedReturnType
 	 *            return type of this subtree
 	 * @param config
 	 *            config
 	 * @return Node that fits the requested description
 	 */
 	public Node createGrowProgram(int curDepth, int maxDepth, int expectedReturnType, GPConfig config) {
 		Node node = growTable[curDepth - 1].generateRandomNode(expectedReturnType, config);
 
 		if (node.getNumArgs() > 0) {
 			Function func = (Function) (node);
 
 			for (int i = 0; i < node.getNumArgs(); i++) {
 				func.setArgN(i, createGrowProgram(curDepth + 1, maxDepth, func.getArgNReturnType(i), config));
 			}
 		}
 		return node;
 	}
 
 	/**
 	 * Generate the tables used for making programs
 	 * 
 	 * @param config
 	 *            config to use
 	 */
 	@SuppressWarnings("unchecked")
 	private void generateTables(GPConfig config) {
 
 		int numFunctions = config.funcSet.size();
 		int numTerminals = config.termSet.size();
 
 		int maxDepth = config.maxDepth();
 
 		growTable = (NodeVector<Node>[]) new NodeVector[maxDepth];
 		fullTable = (NodeVector<Node>[]) new NodeVector[maxDepth];
 
 		for (int i = 0; i < maxDepth; i++) {
 			growTable[i] = new NodeVector<Node>();
 			fullTable[i] = new NodeVector<Node>();
 		}
 
 		// Add in the terminals at the top of the matrix, this is the top of the tree
 		for (int i = 0; i < numTerminals; i++) {
 			Node n = config.termSet.generate(i, config);
 
 			growTable[maxDepth - 1].add(n);
 			fullTable[maxDepth - 1].add(n);
 		}
 
 		// grow table creation, by level
 		for (int curDepth = maxDepth - 2; curDepth >= 0; curDepth--) {
 
 			// Add the terminals - they can be at any level but not below minDepth
 			if (curDepth >= config.minDepth()) {
 				for (int i = 0; i < numTerminals; i++) {
 					Node n = config.termSet.generate(i, config);
 					growTable[curDepth].add(n);
 				}
 			}
 
 			// Add the functions
 			for (int i = 0; i < numFunctions; i++) {
 				// Try every function
 				Function tmpFunc = config.funcSet.generate(i, config);
 				boolean valid = true;
 
 				// For each of its arguments
 				for (int arg = 0; valid && arg < tmpFunc.getNumArgs(); arg++) {
 					boolean found = false;
 					int argNReturnType = tmpFunc.getArgNReturnType(arg);
 
 					// Can you find something in the level below to attach it to
 					for (int tSize = 0; !found && tSize < growTable[curDepth + 1].size(); tSize++) {
 						Node tmpNode = growTable[curDepth + 1].generate(tSize, config);
 
 						if (argNReturnType == tmpNode.getReturnType()) {
 							found = true;
 						}
 						NodeFactory.delete(tmpNode);
 					}
 
 					if (!found) {
 						valid = false;
 					}
 				}
 
 				if (valid) {
 					Node n = config.funcSet.generate(i, config);
 					growTable[curDepth].add(n);
 				}
 			}
 		}
 
 		// full table creation
 		for (int curDepth = maxDepth - 2; curDepth >= 0; curDepth--) {
 			// Add the functions
 			for (int i = 0; i < numFunctions; i++) {
 				Function tmpFunc = config.funcSet.generate(i, config);
 				boolean valid = true;
 
 				for (int arg = 0; valid && arg < tmpFunc.getNumArgs(); arg++) {
 					boolean found = false;
 					int argNReturnType = tmpFunc.getArgNReturnType(arg);
 
 					for (int tSize = 0; !found && tSize < fullTable[curDepth + 1].size(); tSize++) {
 						Node tmpNode = fullTable[curDepth + 1].generate(tSize, config);
 
 						if (argNReturnType == tmpNode.getReturnType()) {
 							found = true;
 							break;
 						}
 						NodeFactory.delete(tmpNode);
 					}
 
 					if (!found) {
 						valid = false;
 						break;
 					}
 				}
 
 				if (valid) {
 					Node n = config.funcSet.generate(i, config);
 					fullTable[curDepth].add(n);
 				}
 			}
 		}
 
 	}
 }
