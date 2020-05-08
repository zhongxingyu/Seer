 import java.util.ArrayList;
 import java.util.List;
 
 
 public class Pruner {
 
 	SampleSet pruningSet;
 	Node decisionTreeRoot;
 	
 	public Pruner(SampleSet pruningSet, Node decisionTree){
 		this.pruningSet = pruningSet;
 		this.decisionTreeRoot = decisionTree;
 	}
 	
 	public Node prune(){
 		divideSamples(decisionTreeRoot, pruningSet);
 		return pruneSubtree(decisionTreeRoot);
 	}
 
 	private void divideSamples(Node node, SampleSet nodeSet){
 		node.setSampleSet(nodeSet);
 		if(!node.isLeaf()){
 			InternalNode decisionNode = (InternalNode) node;
 			Test nodeTest = decisionNode.getTest();
 			if(nodeTest instanceof ContinuousTest){
 				SampleSet trueSet = new SampleSet();
 				trueSet.setAtrributes(nodeSet.getAttributes());
 				SampleSet falseSet = new SampleSet();
 				falseSet.setAtrributes(nodeSet.getAttributes());
 				switch(((ContinuousTest) nodeTest).getSplitType()){
 				case LESS:
 					for(Sample s: nodeSet.getSamples()){
 						if(s.getValue(nodeTest.getTestAttribute().getId()).doubleValue() < ((ContinuousTest) nodeTest).getSplitValue()){
 							trueSet.addSample(s);
 						}else{
 							falseSet.addSample(s);
 						}
 					}
 					break;
 				case LESSOREQUAL:
 					for(Sample s: nodeSet.getSamples()){
 						if(s.getValue(nodeTest.getTestAttribute().getId()).doubleValue() <= ((ContinuousTest) nodeTest).getSplitValue()){
 							trueSet.addSample(s);
 						}else{
 							falseSet.addSample(s);
 						}
 					}
 					break;
 				case MOREOREQUAL:
 					for(Sample s: nodeSet.getSamples()){
 						if(s.getValue(nodeTest.getTestAttribute().getId()).doubleValue() >= ((ContinuousTest) nodeTest).getSplitValue()){
 							trueSet.addSample(s);
 						}else{
 							falseSet.addSample(s);
 						}
 					}
 					break;
 				case MORE:
 					for(Sample s: nodeSet.getSamples()){
 						if(s.getValue(nodeTest.getTestAttribute().getId()).doubleValue() > ((ContinuousTest) nodeTest).getSplitValue()){
 							trueSet.addSample(s);
 						}else{
 							falseSet.addSample(s);
 						}
 					}
 					break;
 				}
 			}else{
 				for(Node child: decisionNode.getChildren()){
 					SampleSet childSet = new SampleSet();
 					childSet.setAtrributes(nodeSet.getAttributes());
 					for(Sample s: nodeSet.getSamples()){
 						if(s.getValue(nodeTest.getTestAttribute().getId()).equals(((CategoricalTestValue) child.getTestValue()).getValue())){
 							childSet.addSample(s);
 						}
 					}
 					child.setSampleSet(childSet);
 				}	
 			}
 		}
 	}
 	
 	private Node pruneSubtree(Node node) {
 		if(!node.isLeaf()){
 			return node;
 		}else{
 			InternalNode decisionNode = (InternalNode) node;
 			List<Node> newChildren = new ArrayList<Node>();
 			for(Node child: decisionNode.getChildren()){
 				newChildren.add(pruneSubtree(child));
 			}
 			decisionNode.setChildren(newChildren);
 			if(getNodeSuccessProbability(decisionNode)>getSuccessProbability(decisionNode)){
				Leaf replacementLeaf = new Leaf();
 				replacementLeaf.setSampleSet(decisionNode.getSampleSet());
				replacementLeaf.setResult(getMajorityClass(decisionNode.getSampleSet()));
 				replacementLeaf.setTestValue(node.getTestValue());
 				return replacementLeaf;
 			}
 			else return decisionNode;
 		}
 	}
 
 	public double getSuccessProbability(Node subtree) {
 		if(subtree.isLeaf()){
 			return getNodeSuccessProbability(subtree);
 		}else{
 			double successProbability=0;
 			double nodeSize = subtree.getSampleSet().getSamples().size();
 			for(Node child: ((InternalNode) subtree).getChildren()){
 				double childWeight = child.getSampleSet().getSamples().size()/nodeSize;
 				successProbability += childWeight * getSuccessProbability(child);
 			}
 			return successProbability;
 		}
 	}
 	
 	public double getNodeSuccessProbability(Node node) {
 		double trueCount=0;
 		double falseCount=0;
 		for(Sample s: node.getSampleSet().getSamples()){
 			if (s.getResult()){
 				trueCount++;
 			}else{
 				falseCount++;
 			}
 		}
 		double trueProbability = trueCount/(trueCount+falseCount);
 		double falseProbability = falseCount/(trueCount+falseCount);
 		if(node.isLeaf()){
 			if(((Leaf) node).isResult()){
 				return trueProbability;
 			}else{
 				return falseProbability;
 			}
 		}
 		else if(trueProbability>falseProbability){
 			return trueProbability;
 		}else{
 			return falseProbability;
 		}
 	}
 	
 	public boolean getMajorityClass(SampleSet samples){
 		int trueCount=0;
 		int falseCount=0;
 		for(Sample s: samples.getSamples()){
 			if (s.getResult()){
 				trueCount++;
 			}else{
 				falseCount++;
 			}
 		}
 		return trueCount>falseCount;
 	}
 }
