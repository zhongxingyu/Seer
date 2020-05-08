 // CyTargetLinker,
 // a Cytoscape plugin to extend biological networks with regulatory interaction
 //
 // Copyright 2011-2013 Department of Bioinformatics - BiGCaT, Maastricht University
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //       http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 //
 package org.cytargetlinker.app.internal.data;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.cytoscape.model.CyEdge;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.model.CyNode;
 
 /**
  * 
  * @author martina kutmon
  * One extension step with multiple datasources
  * creates all the necessary CyNodes and CyEdges for the extended network
  *
  */
 public class ExtensionStep {
 
 	private List<Result> results;
 	private int stepNum = 0;
 	private CyNetwork network;
 	private String idAttribute;
 	
 	private Map<String, CyNode> nodesAdded;
 	
 	public ExtensionStep(CyNetwork network, String idAttribute) {
 		this.network = network;
 		this.idAttribute = idAttribute;
 		results = new ArrayList<Result>();
 		nodesAdded = new HashMap<String, CyNode>();
 	}
 
 	public List<Result> getResults() {
 		return results;
 	}
 	
 	public int getStepNum() {
 		return stepNum;
 	}
 
 	public void setStepNum(int stepNum) {
 		this.stepNum = stepNum;
 	}
 
 	public void execute() {
 		Map<String, CyNode> nodesPresent = new HashMap<String, CyNode>();
 		
 		for(CyNode node : network.getNodeList()) {
 			String value = network.getRow(node).get(idAttribute, String.class);
 			nodesPresent.put(value, node);
 			if(network.getRow(node).getTable().getColumn("ctl.type") == null) {
 				network.getRow(node).getTable().createColumn("ctl.type", String.class, false);
 			}
 			if(network.getRow(node).get("ctl.type", String.class) == null) {
 				network.getRow(node).set("ctl.type", "query");
 			}
 			if(network.getRow(node).getTable().getColumn("ctl.nodeType") == null) {
 				network.getRow(node).getTable().createColumn("ctl.nodeType", String.class, false);
 			}
 			network.getRow(node).set("ctl.nodeType", NodeType.INITIAL.toString());
 			if(network.getRow(node).getTable().getColumn("biologicalType") == null) {
 				network.getRow(node).getTable().createColumn("biologicalType", String.class, false);
 			}
 			if(network.getRow(node).get("biologicalType", String.class) == null) {
 				network.getRow(node).set("biologicalType", "initial");
 			}
 		}
 			
 		for(Result r : results) {
 			for(Edge e : r.getEdges()) {
 				Node source = e.getSource();
 				Node target = e.getTarget();
 				
 				for(String id : source.getIdentifiers()) {
 					if(nodesPresent.containsKey(id)) {
 						e.setCySource(nodesPresent.get(id));
 					}
 				}
 				for(String id : target.getIdentifiers()) {
 					if(nodesPresent.containsKey(id)) {
 						e.setCyTarget(nodesPresent.get(id));
 					}
 				}
 				
 				if(e.getCySource() == null) {
 					CyNode s = nodeAdded(source);
 					if(s == null) {
 						s = addNode(source);
 					}
 					e.setCySource(s);
 				} else if (e.getCyTarget() == null) {
 					CyNode t = nodeAdded(target);
 					if(t == null) {
 						t = addNode(target);
 					}
 					e.setCyTarget(t);
 				}
 				
				// create edge
				CyEdge cyEdge = network.addEdge(e.getCySource(), e.getCyTarget(), false);
				e.setCyEdge(cyEdge);
				for(String str : e.getAttributes().keySet()) {
					if(network.getRow(cyEdge).getTable().getColumn(str) == null) {
						network.getRow(cyEdge).getTable().createColumn(str, String.class, false);
 					}
					network.getRow(cyEdge).set(str, e.getAttributes().get(str));
 				}
				network.getRow(cyEdge).set("datasource", e.getDs().getName());
				r.getDs().getEdges().add(cyEdge);
 			}
 		}
 	}
 		
 	private CyNode addNode(Node node) {
 		CyNode cyNode = network.addNode();
 		network.getRow(cyNode).set(idAttribute, node.getId());
 		if(network.getRow(cyNode).getTable().getColumn("ctl.nodeType") == null) {
 			network.getRow(cyNode).getTable().createColumn("ctl.nodeType", String.class, false);
 		}
 		network.getRow(cyNode).set("ctl.nodeType", node.getNodeType().toString());
 		for(String str : node.getAttributes().keySet()) {
 			if(network.getRow(cyNode).getTable().getColumn(str) == null) {
 				network.getRow(cyNode).getTable().createColumn(str, String.class, false);
 			}
 			network.getRow(cyNode).set(str, node.getAttributes().get(str));
 		}
 		if(!node.getAttributes().containsKey("name")) {
 			if(node.getAttributes().containsKey("label")) {
 				network.getRow(cyNode).set("name", node.getAttributes().get("label"));
 			}
 		}
 		network.getRow(cyNode).set("ctl.type", "step " + stepNum);
 		
 		for(String str : node.getIdentifiers()) {
 			nodesAdded.put(str, cyNode);
 		}
 		return cyNode;
 	}
 
 	private CyNode nodeAdded(Node node) {
 		for(String id : node.getIdentifiers()) {
 			if(nodesAdded.keySet().contains(id)) {
 				return nodesAdded.get(id);
 			}
 		}
 		return null;
 	}
 }
