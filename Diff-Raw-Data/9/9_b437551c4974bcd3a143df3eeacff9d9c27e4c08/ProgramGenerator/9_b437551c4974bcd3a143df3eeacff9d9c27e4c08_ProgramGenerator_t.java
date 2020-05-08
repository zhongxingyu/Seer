 package library;
 
 import java.util.List;
 
 public class ProgramGenerator {
 	private GPConfig config;
 
 	private NodeVector<Node> growTable[];
 
 	private NodeVector<Node> fullTable[];
 
 	public ProgramGenerator(GPConfig conf) {
 		config = conf;
 	}
 
 	public void generateInitialPopulation(List<GeneticProgram> pop, int numIndividuals, int expectedReturnType[]) {
 		generateTables();
 		Node tmp;
 		int indivPerSize = 0;
 		int tmpSize = config.minDepth() - 1;
 		int indiv = 0;
 
 		int sizeSteps = (config.maxDepth() - 1) - tmpSize;
 		int sizeIncrement = 0;
 		int numIndividualsForRamping = numIndividuals / 2;
 
 		if (config.minDepth() > config.maxDepth()) throw new RuntimeException("minSize is greater than maxSize");
 
 		if (sizeSteps <= 0) {
 			sizeIncrement = 0;
 			indivPerSize = numIndividualsForRamping;
 		} else if ((numIndividualsForRamping / sizeSteps) > 1) {
 			// If there are more individuals than size steps
 			// then we can use every size from minSize to maxSize
 			sizeIncrement = 1;
 			indivPerSize = numIndividualsForRamping / sizeSteps;
 		} else {
 			// If we have less individuals than we have steps
 			// then we can't use all the sizes when ramping
 			indivPerSize = 1;
 			sizeIncrement = sizeSteps / numIndividualsForRamping;
 			if ((sizeSteps % numIndividualsForRamping) > 0) sizeIncrement++;
 		}
 
 		for (indiv = 0; indiv < numIndividualsForRamping; indiv++) {
 			if ((indiv % indivPerSize) == 0) {
 				tmpSize += sizeIncrement;
 				if (tmpSize >= config.maxDepth()) tmpSize = (config.maxDepth() - 1);
 			}
 
 			try {
 				// TODO dodgy
 				for (int i = 0; i < config.getNumRoots(); i++) {
 					tmp = createGrowProgram(0, tmpSize, pop.get(indiv).getReturnType(i));
 					pop.get(indiv).setRoot(tmp, i);
 				}
 			} catch (Exception error) {
 				error.printStackTrace();
 				if ((tmpSize += sizeIncrement) >= config.maxDepth()) tmpSize = (config.maxDepth() - 1);
 
 				indiv--;
 				continue;
 			}
 		}
 
 		tmpSize = config.minDepth() - 1;
 
 		for (; indiv < numIndividuals; indiv++) {
 			if ((indiv % indivPerSize) == 0) {
 				tmpSize += sizeIncrement;
 				if (tmpSize >= config.maxDepth()) tmpSize = (config.maxDepth() - 1);
 			}
 
 			try {
 				for (int i = 0; i < config.getNumRoots(); i++) {
 					tmp = createFullProgram(0, tmpSize, pop.get(indiv).getReturnType(i));
 					pop.get(indiv).setRoot(tmp, i);
 				}
 			} catch (Exception error) {
 				if (tmpSize >= (config.maxDepth() - 1)) {
 					throw new RuntimeException(
 							"ProgramGenerator::generateInitialPopulation \nUnrecoverable error\nUnable to generate program. Check depth limits and for insufficient terminals and functions\n");
 				}
 
 				if ((tmpSize += sizeIncrement) >= config.maxDepth()) tmpSize = (config.maxDepth() - 1);
 
 				indiv--;
 				continue;
 			}
 		}
 	}
 
 	public Node createFullProgram(int curDepth, int maxDepth, int expectedReturnType) {
 		int depth;
 		Node node = null;
 
 		depth = maxDepth - curDepth;
 		if (depth < 0) {
 			depth = 0;
 		}
 
 		node = fullTable[depth].generateRandomNode(expectedReturnType,config);
 
 		if (node == null) {
 			System.err.println("Warning, unable to create Full program for this set of Functions and Terminals");
 			return createGrowProgram(curDepth, maxDepth, expectedReturnType);
 		}
 
 		if (node.getNumArgs() > 0) {
 			Function func = (Function) (node);
 
 			for (int i = 0; i < func.getNumArgs(); i++) {
 				func.setArgN(i, createFullProgram(curDepth + 1, maxDepth, func.getArgNReturnType(i)));
 			}
 		}
 
 		return node;
 	}
 
 	public Node createGrowProgram(int curDepth, int maxDepth, int expectedReturnType) {
 		int i;
 		int depth;
 		Node node;
 
		depth = maxDepth - curDepth;
 		if (depth < 0) {
 			depth = 0;
 		}
 
 		node = growTable[depth].generateRandomNode(expectedReturnType,config);
 
 		if (node == null) throw new RuntimeException("getRandomNode returned NULL");
 
 		if (node.getNumArgs() > 0) {
 			Function func = (Function) (node);
 
 			for (i = 0; i < node.getNumArgs(); i++) {
 				func.setArgN(i, createGrowProgram(curDepth + 1, maxDepth, func.getArgNReturnType(i)));
 			}
 		}
 		return node;
 	}
 
 	@SuppressWarnings("unchecked")
 	void generateTables() {
 
 		int numFunctions = config.funcSet.size();
 		int numTerminals = config.termSet.size();
 
 		int i;
 
 		int maxDepth = config.maxDepth();
 
 		growTable = (NodeVector<Node>[]) new NodeVector[maxDepth];
 		fullTable = (NodeVector<Node>[]) new NodeVector[maxDepth];
 
 		for (i = 0; i < maxDepth; i++) {
 			growTable[i] = new NodeVector<Node>();
 			fullTable[i] = new NodeVector<Node>();
 		}
 
 
 		// Add in the terminals at the bottom of the tree
 		for (i = 0; i < numTerminals; i++) {
 			Node n = config.termSet.generate(i,config);
 
 			growTable[0].add(n);
 			fullTable[0].add(n);
 		}
 
 		int curDepth = 0;
 
 		// grow table creation
 		for (curDepth = 1; curDepth < maxDepth; curDepth++) {
 			// Add the terminals
 			for (i = 0; i < numTerminals; i++) {
 				Node n = config.termSet.generate(i,config);
 
 				growTable[curDepth].add(n);
 			}
 
 			// Add the functions
 			for (i = 0; i < numFunctions; i++) {
 				Function tmpFunc = (Function) config.funcSet.generate(i,config);
 				boolean valid = true;
 
 				for (int arg = 0; arg < tmpFunc.getNumArgs(); arg++) {
 					boolean found = false;
 					int argNReturnType = tmpFunc.getArgNReturnType(arg);
 
 					for (int tSize = 0; tSize < growTable[curDepth - 1].size(); tSize++) {
 						Node tmpNode = growTable[curDepth - 1].generate(tSize,config);
 
 						if (argNReturnType == tmpNode.getReturnType()) {
 							found = true;
 							break;
 						}
 					}
 
 					if (!found) {
 						valid = false;
 						break;
 					}
 				}
 
 				if (valid) {
 					Node n = config.funcSet.generate(i, config);
 					growTable[curDepth].add(n);
 				}
 			}
 		}
 
 		// full table creation
 		for (curDepth = 1; curDepth < maxDepth; curDepth++) {
 			// Add the functions
 			for (i = 0; i < numFunctions; i++) {
 				Function tmpFunc = (Function) config.funcSet.generate(i,config);
 				boolean valid = true;
 
 				for (int arg = 0; arg < tmpFunc.getNumArgs(); arg++) {
 					boolean found = false;
 					int argNReturnType = tmpFunc.getArgNReturnType(arg);
 
 					for (int tSize = 0; tSize < fullTable[curDepth - 1].size(); tSize++) {
 						Node tmpNode = fullTable[curDepth - 1].generate(tSize,config);
 
 						if (argNReturnType == tmpNode.getReturnType()) {
 							found = true;
 							break;
 						}
 					}
 
 					if (!found) {
 						valid = false;
 						break;
 					}
 				}
 
 				if (valid) {
 					Node n = config.funcSet.generate(i,config);
 					fullTable[curDepth].add(n);
 				}
 			}
 		}
 
 	}
 }
