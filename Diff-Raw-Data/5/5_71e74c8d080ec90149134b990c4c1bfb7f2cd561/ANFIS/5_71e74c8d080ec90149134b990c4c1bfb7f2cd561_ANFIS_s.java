 package anfis;
 
 import nodes.*;
 import util.Settings;
 
 public class ANFIS {
 
 	// all layers of an anfis network;
 	private Layer layer1, layer2, layer3, layer4, layer5;
 
 	public ANFIS() {
 
 	}
 	
 	
 
 	// generating new network -- overwrite old if there was one
 	public void generateNetwork() {
 		initializeLayers();
 		generateLayer1();
 		generateLayer2();
 		generateLayer3();
 		generateLayer4();
 		generateLayer5();
 	}
 	
 	public double test(double[][] dataSet, double[] expectedOutput) {
 		FeedforwardFunction fff = new FeedforwardFunction(false);
 		BackpropagationFunction bpf = new BackpropagationFunction(true);
 		
 		for(int i = 0; i < dataSet.length; i++) {
 			fff.setInput(dataSet[i]);
 			bpf.setInput(dataSet[i]);
 			bpf.setExpectedOutput(expectedOutput[i]);
 			
 			layer1.sendVisitorToAllNodes(fff);
 			layer2.sendVisitorToAllNodes(fff);
 			layer3.sendVisitorToAllNodes(fff);
 			layer4.sendVisitorToAllNodes(fff);
 			layer5.sendVisitorToAllNodes(fff);
 			
 			layer5.sendVisitorToAllNodes(bpf);
 		}
 		
 		return ((OutputNode) layer5.getNodes().get(0)).getSumOfError(true);
 	}
 	
 	public double training (double[][] trainingSet, double[] expectedOutput){
 		
 		// TODO perhaps using layer instead of using nodes 
 		
 		FeedforwardFunction fff = new FeedforwardFunction(true);
 		LeastSquaresEstimate lse = new LeastSquaresEstimate(trainingSet, expectedOutput, layer4.getNodes().size());
 		BackpropagationFunction bpf = new BackpropagationFunction(true);
 		GradientDecent gd = new GradientDecent();
 		
 		//Train consequent parameters (Polynomial Node)
 		for(double[] input : trainingSet) {
 			fff.setInput(input);
 			layer1.sendVisitorToAllNodes(fff);
 			layer2.sendVisitorToAllNodes(fff);
 			layer3.sendVisitorToAllNodes(fff);
 			layer4.sendVisitorToAllNodes(fff);
 		}
 		
 		layer3.sendVisitorToAllNodes(lse);
 		layer4.sendVisitorToAllNodes(lse);
 		
 		// Consequent parameters trained!
 		
 		// Train premise parameters (Membership Function Node)
 		fff.setSaveNormFSOutput(false);
 		
 		for(int i = 0; i < trainingSet.length; i++) {
 			fff.setInput(trainingSet[i]);
 			bpf.setInput(trainingSet[i]);
 			bpf.setExpectedOutput(expectedOutput[i]);
 			
 			layer1.sendVisitorToAllNodes(fff);
 			layer2.sendVisitorToAllNodes(fff);
 			layer3.sendVisitorToAllNodes(fff);
 			layer4.sendVisitorToAllNodes(fff);
 			layer5.sendVisitorToAllNodes(fff);
 			
 			layer5.sendVisitorToAllNodes(bpf);
 			layer4.sendVisitorToAllNodes(bpf);
 			layer3.sendVisitorToAllNodes(bpf);
 			layer2.sendVisitorToAllNodes(bpf);
 			layer1.sendVisitorToAllNodes(bpf);
 		}
 		
 		gd.setLearningRate(0.1D);
 		
 		layer1.sendVisitorToAllNodes(gd);
 		//Premise parameters trained!
 		
 		return ((OutputNode) layer5.getNodes().get(0)).getSumOfError(true);
 	}
 
 	private void initializeLayers() {
 		layer1 = new Layer();
 		layer2 = new Layer();
 		layer3 = new Layer();
 		layer4 = new Layer();
 		layer5 = new Layer();
 	}
 
 	private void generateLayer1() {
 
 		for (int i = 1; i <= Settings.numberOfInputVaribles; i++) {
 
 			layer1.addNode(getDefaultMemberships(i, Settings.getMin(i),
 					Settings.getMax(i), Settings.numberOfShapes,
 					Settings.bellSlope));
 		}
 
 	}
 
 	private void generateLayer2() {
 
 		int numberOfNodes = 
 		(int) Math.pow(Settings.numberOfShapes, Settings.numberOfInputVaribles);
 		int[] counter = new int[Settings.numberOfInputVaribles];
 		initializeArray(counter);
 
 		for (int i = 0; i < numberOfNodes; i++) {
 			FiringStrengthNode fsn = new FiringStrengthNode();
 			layer2.addNode(fsn);
 
 			for (int j = 0; j < counter.length; j++) {
 				fsn.addPredecessorLink(this.searchMembersphipFunctionNode(
 						j + 1, counter[j] + 1));
 			}
 
 			countUpArray(counter, Settings.numberOfShapes);
 		}
 
 	}
 
 	private void countUpArray(int[] input, int limit) {
		for (int i = input.length - 1; i <= 0; i--) {
 
 			input[i]++;
 			if (input[i] == limit) {
 				input[i] = 0;
 			} else {
 				break;
 			}
 		}
 	}
 
 	private void initializeArray(int[] input) {
 		for (int i = 0; i < input.length; i++) {
 			input[i] = 0;
 		}
 	}
 
 	private MembershipFunctionNode searchMembersphipFunctionNode(int varNumber,
 			int setNumber) {
 		return layer1.getMFSNode(varNumber, setNumber);
 	}
 
 	private void generateLayer3() {
 
 		for (int i = 0; i < layer2.getNodes().size(); i++) {
 			NormFSNode n = new NormFSNode((FiringStrengthNode) layer2
 					.getNodes().get(i));
 			layer3.addNode(n);
 
 			for (int j = 0; j < layer2.getNodes().size(); j++) {
 				layer2.getNodes().get(j).addSuccessorLink(n);
 			}
 		}
 	}
 
 	private void generateLayer4() {
 
 		for (int i = 0; i < layer3.getNodes().size(); i++) {
 			PolynomialNode pn = new PolynomialNode(Settings.numberOfInputVaribles + 1);
 			layer4.addNode(pn);
 			layer3.getNodes().get(i).addSuccessorLink(pn);
 		}
 	}
 
 	private void generateLayer5() {
 
 		Node lastNode = new OutputNode();
 		layer5.addNode(lastNode);
 
 		for (int i = 0; i < layer4.getNodes().size(); i++) {
 			layer4.getNodes().get(i).addSuccessorLink(lastNode);
 		}
 	}
 
 	/**
 	 * returns a set of function nodes from min to max divided by number of
 	 * shapes
 	 * 
 	 * @param min
 	 *            - min value
 	 * @param max
 	 *            - max value
 	 * @param shapes
 	 *            - number of shapes
 	 * @param slope
 	 *            - slope of the bell function (default could be 2)
 	 * @return returns a array of MembershipFunctionNode with length = shapes
 	 */
 	public static MembershipFunctionNode[] getDefaultMemberships(int varNumber,
 			double min, double max, int shapes, double slope) {
 
 		MembershipFunctionNode[] back = new MembershipFunctionNode[shapes];
 
 		if (shapes == 1) {
 
 			double a = (max - min) / 2;
 			double b = slope * a;
 			double c = min + a;
 
 			back[0] = new MembershipFunctionNode(a, b, c, varNumber, 1);
 
 		} else {
 
 			double a = (max - min) / (2 * shapes - 2);
 			double b = 2 * a;
 			double c = min - 2 * a;
 
 			for (int i = 0; i < shapes; i++) {
 				c = c + 2 * a;
 				back[i] = new MembershipFunctionNode(a, b, c, varNumber, i + 1);
 
 			}
 
 		}
 
 		return back;
 	}
 
 }
 
 // Code for Least Sqare estimation and other stashed things
 
 
 //	public void computeLeastSquareEstimate() { 
 //		Layer l3 = this.getLayer(3);
 //		double[][] normFS = new double[l3.getNodes().size()][];
 //		int i = 0;
 //		for (Node n : l3.getNodes()) {
 //			normFS[i][] = ((NormFSNode) n).getNormFS();
 //			i++;
 //		}
 //	
 //		double[] lseHelperArray = new double[inputSize * normFS.length];
 //		i = 0;
 //		for	(double nfs : normFS) {
 //			for (double in : input) {
 //				lseHelperArray[i] = nfs * in;
 //				i++;
 //			}
 //		}
 //	
 //		Matrix lseHelperMatrix = new Matrix(lseHelperArray,lseHelperArray.length);
 //		Matrix lse = lseHelperMatrix.transpose().times(lseHelperMatrix).inverse().times(lseHelperMatrix.transpose()).times(expectedOutput);
 //		consequentParameter = lse.getArray()[0];
 //	}
 
 
 /*
  * public double[] getConsequentParameter(int id) { return
  * Arrays.copyOfRange(consequentParameter, id * inputSize, id inputSize +
  * inputSize); }
  */
 
 // part earlier was in ANFIS consturctor
 /*
  * premiseParameter = new double[3 * 4]; // size of array equal to 3 // times
  * number of membership // functions consequentParameter = new double[3 * 4]; //
  * size of array equal to // number of polynomial // nodes times (number of //
  * input parameter + 1) for (int i = 0; i < consequentParameter.length; i++) {
  * consequentParameter[i] = Math.random(); }
  */
 
 // *********************************************************************************************************
 // old Versions of generating layers
 
 /*
  * // definition of layers layer = new Layer[4]; for (int i = 0; i < 4; i++) {
  * layer[i] = new Layer(); }
  * 
  * 
  * // save all msfNodes for generating the FiringStrengthNodes
  * MembershipFunctionNode[][] msfNodes = new
  * MembershipFunctionNode[Settings.numberOfShapes][inputSize];
  * 
  * // adds new nodes to layer 1 -- for each input numberOfShapes-times //
  * MembershipfunctionNodes for (int i = 0; i < inputSize; i++) { double[]
  * statistics = DataReader .getStatistics(getDataColumn(i + 1));
  * MembershipFunctionNode[] inputNodes = getDefaultMemberships( statistics[0],
  * statistics[1], Settings.numberOfShapes, Settings.bellSlope); for (int j = 0;
  * j < inputNodes.length; j++) { layer[0].addNode(inputNodes[j]); msfNodes[j][i]
  * = inputNodes[j]; } }
  */
 
 // generating new nodes for layer 2
 /*
  * int[] countingArray = new int[msfNodes[0].length]; for (int x :
  * countingArray) { x = 0; } generateFiringStrengthNodes(layer[1], msfNodes,
  * countingArray, msfNodes.length, 0);
  */
 
 /*
  * Generating layer 2 nodes by using all nodes from layer 1 and connecting them
  * using backtracking algorithm
  * 
  * @param layer -- layer 2
  * 
  * @param msfNodes all nodes of layer one [a1][a2][a3] example structure
  * [b1][b2][b3] [c1][c2][c3]
  * 
  * @param countingArray -- for backtrack [0][1][1] --> leads to [a1][b2][c2] -
  * start [0][0][0] * msfNodes[0].length
  * 
  * @param big -- for backtrack - start msfNodes.lenght
  * 
  * @param pos -- for backtrack - start is 0
  * 
  * private void generateFiringStrengthNodes(Layer layer,
  * MembershipFunctionNode[][] msfNodes, int[] countingArray, int big, int pos) {
  * 
  * if (pos < countingArray.length) { for (int p = 0; p < big; p++) {
  * countingArray[pos] = p; generateFiringStrengthNodes(layer, msfNodes,
  * countingArray, big, pos + 1); } } else { FiringStrengthNode f = new
  * FiringStrengthNode(); layer.addNode(f); for (int i = 0; i <
  * msfNodes[0].length; i++) { f.addSuccessorLink(msfNodes[countingArray[i]][i]);
  * } }
  * 
  * }
  */
 
 /*
  * /**
  * 
  * @param column - number
  * 
  * @return column of data
  * 
  * private double[] getDataColumn(int column) { double[] back = new
  * double[data.length];
  * 
  * for (int i = 0; i < back.length; i++) { back[i] = data[i][column]; }
  * 
  * return back; }
  */
