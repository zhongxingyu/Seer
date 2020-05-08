 package jbookreader.book.impl;
 
 import jbookreader.book.IContainerNode;
 import jbookreader.book.INamedNode;
 import jbookreader.book.INode;
 
 abstract class AbstractNamedNode extends AbstractNode implements INamedNode {
 
 	private String nodeClass;
 	private String nodeTag;
 
 	public String getNodeClass() {
 		return nodeClass;
 	}
 
 	public String getNodeTag() {
 		return nodeTag;
 	}
 
 	public void setNodeClass(String nodeClass) {
 		this.nodeClass = nodeClass;
 	}
 
 	public void setNodeTag(String nodeTag) {
 		this.nodeTag = nodeTag;
 	}
 	
 	private String getNodeRefElement() {
 		StringBuilder builder = new StringBuilder();
 		String tag = this.nodeTag;
 
 		builder.append('/');
 		builder.append(tag);
 
 		IContainerNode parent = getParentNode();
 		if (parent != null) {
			int count = 1;
 
			INode node = this;
			while (parent.hasPrevious(node)) {
				node = parent.getPrevious(node);
 				if (node instanceof INamedNode) {
 					INamedNode namedNode = (INamedNode) node;
 					if (tag.equals(namedNode.getNodeTag())) {
 						count ++;
 					}
 				}
 			}
 			
 			builder.append('[');
 			builder.append(count);
 			builder.append(']');
 		}
 		
 		return builder.toString();
 	}
 	
 	public String getNodeRef() {
 		if (getParentNode() != null) {
 			return getParentNode().getNodeRef() + getNodeRefElement();
 		}
 		return getNodeRefElement();
 	}
 
 	@Override
 	public String toString() {
 		if (nodeClass != null) {
 			return nodeTag + "." + nodeClass;
 		}
 		return nodeTag;
 	}
 }
