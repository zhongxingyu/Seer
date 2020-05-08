 package com.operativus.senacrs.audit.graph.edges.webdriver;
 
 import com.operativus.senacrs.audit.common.AbstractHasLogger;
 import com.operativus.senacrs.audit.graph.edges.Edge;
 import com.operativus.senacrs.audit.graph.edges.IllegalSourceNodeException;
 import com.operativus.senacrs.audit.graph.nodes.Node;
 
 public class OpenDashboard
 		extends AbstractHasLogger
 		implements Edge {
 
 	private static final OpenDashboard instance = new OpenDashboard();
 
 	private OpenDashboard() {
 		
 		super();
 	}
 	
 	@Override
 	public Node traverse(Node source) {
 
 		if (!Node.START.equals(source)) {
			throw new IllegalSourceNodeException(source);
		}
 		return null;
 	}
 	
 	public static OpenDashboard getInstance() {
 
 		return instance;
 	}
 }
