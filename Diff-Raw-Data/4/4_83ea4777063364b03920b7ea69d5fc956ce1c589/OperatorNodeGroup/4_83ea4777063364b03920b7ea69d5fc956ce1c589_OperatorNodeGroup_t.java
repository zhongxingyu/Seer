 /**
  * 
  */
 package org.hypothesis.common.expression;
 
 import java.util.LinkedList;
 
 /**
  * @author Kamil Morong - Hypothesis
  *
  * This class holds informations about operators with the same level
  */
 @SuppressWarnings("serial")
 class OperatorNodeGroup extends LinkedList<HasOperatorNode> implements HasOperatorNode {
 	private int beginPosition;
 	private int endPosition;
 	private int level;
 	private OperatorNodeGroup parent;
 	
 	public OperatorNodeGroup() {
 		this(0, 0, 0, null);
 	}
 	
 	public OperatorNodeGroup(int beginPosition, int endPosition, int level, OperatorNodeGroup parent) {
 		this.beginPosition = beginPosition;
 		this.endPosition = endPosition;
 		this.level = level;
 		this.parent = parent;
 	}
 	
 	public int getBeginPosition() {
 		return beginPosition;
 	}
 
 	public void setBeginPosition(int beginPosition) {
 		this.beginPosition = beginPosition;
 	}
 
 	public int getEndPosition() {
 		return endPosition;
 	}
 
 	public void setEndPosition(int endPosition) {
 		this.endPosition = endPosition;
 	}
 
 	public int getLevel() {
 		return level;
 	}
 
 	public void setLevel(int level) {
 		this.level = level;
 	}
 
 	public OperatorNodeGroup getParent() {
 		return parent;
 	}
 
 	public void setParent(OperatorNodeGroup parent) {
 		this.parent = parent;
 	}
 	
 	public OperatorNode getOperatorNode() {
 		OperatorNode node = null;
 		OperatorNode tempNode = null;
 
 		for (int i = 0; i < this.size(); ++i) {
 			int priority = 0;
 			int tempPriority = 0;
 			
 			if (node != null)
 				tempNode = node;
 			
 			node = this.get(i).getOperatorNode();
 			
 			if (this.get(i) instanceof OperatorNode) {
 				this.set(i, null);
 			}
 			
 			if (node != null && tempNode != null) {
 				priority = node.getOperatorPriority();
 				tempPriority = tempNode.getOperatorPriority();
 				
 				if (priority < tempPriority) {
 					tempNode.setRightNode(node);
 					node = tempNode;
 				} else {
 					if (node.getLeftNode() == null) {
 						node.setLeftNode(tempNode);
 					} else if (node.getRightNode() == null) {
 						node.setRightNode(tempNode);
 					}
 				}
 				
 				tempNode = null;
 			}
 		}
 		
 		if (node == null && tempNode != null)
 			return tempNode;
 		else {
 			OperatorNode operatorNode = new OperatorNode();
 			operatorNode.setLeftNode(node);
 			
			if (node == null && operatorNode.getOperator() == null) {
 				operatorNode.setPosition(-1);
 				operatorNode.setOperator(Operator.PLUS);
 				operatorNode.setUnary(true);
			}
 		
 			return operatorNode;
 		}
 	}
 	
 	public boolean place(OperatorNode operatorNode) {
 		boolean result = false;
 		
 		if (operatorNode.getPosition() > beginPosition && operatorNode.getPosition() < endPosition) {
 			for (int i = 0 ; i < this.size(); ++i) {
 				HasOperatorNode obj = get(i);
 				
 				if (this instanceof ClassNodeGroup && obj instanceof MethodArgumentGroup)
 					continue;
 				
 				if (obj instanceof OperatorNodeGroup) {
 					result = ((OperatorNodeGroup)obj).place(operatorNode);
 					
 					if (result)
 						break;
 				}
 			}
 			
 			if (!result) {
 				int begin = 0;
 				int end = 0;
 				
 				for (int i = 0 ; i < this.size(); ++i) {
 					Object obj = get(i);
 					
 					if (obj instanceof OperatorNodeGroup) {
 						end = ((OperatorNodeGroup)obj).beginPosition;
 					} else if (obj instanceof OperatorNode) {
 						end = ((OperatorNode)obj).getPosition();
 					}
 					
 					if (operatorNode.getPosition() > begin && operatorNode.getPosition() < end) {
 						add(i, operatorNode);
 						operatorNode.setGroup(this);
 						result = true;
 						break;
 					}
 					
 					if (obj instanceof OperatorNodeGroup) {
 						begin = ((OperatorNodeGroup)obj).endPosition;
 					} else {
 						begin = end;
 					}
 				}
 				
 				if (!result) {
 					add(operatorNode);
 					operatorNode.setGroup(this);
 					result = true;
 				}
 			}
 		}
 		
 		return result;
 	}
 }
