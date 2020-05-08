 package org.geworkbench.bison.datastructure.biocollections;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 
 /**
  * AdjacencyMatrix.
  * 
  * @author not attributable
  * @version $Id$
  */
 
 public class AdjacencyMatrix implements Serializable {
 
 	private static final long serialVersionUID = 2986018836859246187L;
 
 	private static Log log = LogFactory.getLog(AdjacencyMatrix.class);
 
 	public static class EdgeInfo implements Serializable {
 		private static final long serialVersionUID = 8884626375173162244L;
 		public float value;
 		public String type;
 
 		EdgeInfo(float value, String type) {
 			this.value = value;
 			this.type = type;
 		}
 		
 		@Override
 		public boolean equals(Object obj) {
 			if (!(obj instanceof EdgeInfo))
 				return false;
 
 			EdgeInfo edgeInfo = (EdgeInfo) obj;	
 			
 			if (type == null || edgeInfo.type == null)
 				return true;
 			
 			if (edgeInfo.type.equals(this.type)	)				 
 				return true;
 		    else
 				return false;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
             int h = 5;  
             if (type != null)
                 h =  prime * h + this.type.hashCode() + 21;
 			return h;
 		}  
 		
 	}
 
 	public enum NodeType {
 		MARKER, GENE_SYMBOL, PROBESET_ID, STRING, NUMERIC, OTHER
 	};
 
 	// integer Id is not not used for now.
 	public static class Node implements Serializable {
 		private static final long serialVersionUID = -6472302414584701593L;
 
 		public NodeType type;
 		public DSGeneMarker marker;
 		public String stringId;
 		public int intId;
 
 		public Node(DSGeneMarker marker) {
 			this.type = NodeType.MARKER;
 			this.marker = marker;
 			stringId = null;
 			intId = -1;
 		}
 
 		public Node(NodeType type, String id) {
 			this.type = type;
 			stringId = id;
 			intId = -1;
 			marker = null;
 		}	
 		
 		/*
 		 * if node type is GENE_SYMBOL and intId is 0, it means that the gene does not
 		 * presented in the current microarray set.
 		 */
 		public Node(NodeType type, String stringId, int intId ) {
 			this.type = type;
 			this.stringId = stringId;
 			this.intId = intId;
 			marker = null;
 		}		
 		
 		
 
 		public Node(NodeType type, int id) {
 			this.type = type;
 			intId = id;
 			stringId = null;
 			marker = null;
 		}		
 	 
 
 		@Override
 		public boolean equals(Object obj) {
 			if (!(obj instanceof Node))
 				return false;
 
 			Node node = (Node) obj;
 			if (node.type == NodeType.MARKER && this.type == NodeType.MARKER
 					&& node.marker.equals(this.marker)) {
 				return true;
 			} else if (node.type == this.type) {
 				if (node.stringId == null || this.stringId == null)
 					return false;
 				else if (node.stringId.equals(this.stringId))
 					return true;
 				else
 					return false;
 			} else
 				return false;
 		}
 
 		@Override
 		public int hashCode() {
 			int h = 17;
 			if (type != null)
 				h = 31 * h + type.hashCode();
 			if (marker != null)
 				h = 31 * h + marker.hashCode();
 			if (stringId != null)
 				h = 31 * h + stringId.hashCode();
 			h = 31 * h + intId;
 			return h;
 		}
 	}
 
 	private HashMap<Node, HashMap<Node, Set<EdgeInfo>>> geneRows = new HashMap<Node, HashMap<Node, Set<EdgeInfo>>>();
 
 	private final DSMicroarraySet<DSMicroarray> maSet;	
 
 	private final String name;
 
 	private final Map<String, String> interactionTypeSifMap; // TODO check ?
 
     private Set<Node> nodeSet = new HashSet<Node>();
     
 
 	public AdjacencyMatrix(String name,
 			final DSMicroarraySet<DSMicroarray> microarraySet) {
 		this.name = name;
 		maSet = microarraySet;
 		interactionTypeSifMap = null;
 		log.debug("AdjacencyMatrix created with label " + name
 				+ " and microarray set " + maSet.getDataSetName());
 	}
 
 	public AdjacencyMatrix(String name,
 			final DSMicroarraySet<DSMicroarray> microarraySet,
 			Map<String, String> interactionTypeSifMap) {
 		this.name = name;
 		maSet = microarraySet;
 		this.interactionTypeSifMap = interactionTypeSifMap;
 		log.debug("AdjacencyMatrix created with label " + name
 				+ " and microarray set " + maSet.getDataSetName()
 				+ ", with interaction type map");
 	}
 
 	/**
 	 * Returns a map with all the edges to geneId. This is only used by
 	 * master regulator analysis.
 	 * 
 	 */
 	public Set<DSGeneMarker> get(DSGeneMarker marker) {
 		Set<DSGeneMarker> set = new HashSet<DSGeneMarker>();
 		HashMap<Node, Set<EdgeInfo>> row = geneRows.get(new Node(marker));
 		if (row == null) {
 			row = geneRows.get(new Node(NodeType.GENE_SYMBOL, marker
 					.getGeneName()));
 		}
 		if (row == null)
 			return null;
 		for (Node id : row.keySet()) {
 			if (id.type == NodeType.MARKER)
 				set.add(id.marker);
 			else if (id.type == NodeType.GENE_SYMBOL) {
 				DSGeneMarker m = maSet.getMarkers().get(id.stringId);
 				if (m != null)
 					set.add(m);
 			}
 		}
 		return set;
 	}
 
 	/**
 	 * Add a node only. This is useful only when there is no edged from this
 	 * node.
 	 * 
 	 * @param geneId
 	 */
 	// TODO this feature may no be necessary
 	public void addGeneRow(Node node) {
 		HashMap<Node, Set<EdgeInfo>> row = geneRows.get(node);
 		if (row == null) {
 			geneRows.put(node, new HashMap<Node, Set<EdgeInfo>>());
 			nodeSet.add(node);
 		}
 	}
 
 	public String getLabel() {
 		return name;
 	}
 
 	/**
 	 * Adds and edge between geneId1 and geneId2.
 	 * 
 	 */
 	public void add(Node node1, Node node2, float edge, String interaction) {
 		 
 		HashMap<Node, Set<EdgeInfo>> row = geneRows.get(node1);
 		if (row == null) {
 			row = new HashMap<Node, Set<EdgeInfo>>();
 			geneRows.put(node1, row);
 			nodeSet.add(node1);
 		}
 
 		Set<EdgeInfo> edgeSet = row.get(node2);
 		if (edgeSet == null)
 		{
 			edgeSet = new HashSet<EdgeInfo>();
 			nodeSet.add(node2);
 		}
 
 	    edgeSet.add(new EdgeInfo(edge, interaction));
 		 
 
 		row.put(node2, edgeSet);
 		
 	}
 
 	// this variation is used only by ARACNE
 	// the new edge is added only if the edge is larger
 	public void add(Node node1, Node node2, float edge) {
 
 		HashMap<Node, Set<EdgeInfo>> row = geneRows.get(node1);
 		if (row == null) {
 			row = new HashMap<Node, Set<EdgeInfo>>();
 			geneRows.put(node1, row);
 			nodeSet.add(node1);
 		}
 
 		Set<EdgeInfo> edgeSet = row.get(node2);
 		EdgeInfo existingEdge = null;
 		if (edgeSet != null && !edgeSet.isEmpty())
 			existingEdge = (EdgeInfo) (edgeSet.toArray()[0]);
 		if (existingEdge == null || existingEdge.value < edge) {
 			edgeSet = new HashSet<EdgeInfo>();
 			edgeSet.add(new EdgeInfo(edge, null));
 			row.put(node2, edgeSet);			 
 		}
 		nodeSet.add(node2);
 
 	}
 
 	public int getConnectionNo() {
 		return getEdges().size() ;
 	}
 
 	public int getNodeNumber() {
 		return nodeSet.size();
 	}
 
 	public DSMicroarraySet<DSMicroarray> getMicroarraySet() {
 		return maSet;
 	}
 
 	public Map<String, String> getInteractionTypeSifMap() {
 		return interactionTypeSifMap;
 	}
 
 	public static class Edge {
 		public Node node1;
 		public Node node2;
 		public EdgeInfo info;
 
 		Edge(Node node1, Node node2, EdgeInfo info) {
 			this.node1 = node1;
 			this.node2 = node2;
 			this.info = info;
 		}
 	}
 
 	/**
 	 * 
 	 * @return all edges
 	 */
 	public List<Edge> getEdges() {
 		List<Edge> list = new ArrayList<Edge>();
 		for (Node node1 : geneRows.keySet()) {
 			Map<Node, Set<EdgeInfo>> destGenes = geneRows.get(node1);
 			for (Node node2 : destGenes.keySet()) {
 				for (EdgeInfo e : destGenes.get(node2))
 					list.add(new Edge(node1, node2, e));
 			}
 		}
 		return list;
 	}
 
 	/**
 	 * 
 	 * @return edges starting from a given node
 	 */
 	public List<Edge> getEdges(Node node1) {
 		List<Edge> list = new ArrayList<Edge>();
 		Map<Node, Set<EdgeInfo>> destGenes = geneRows.get(node1);
 		for (Node node2 : destGenes.keySet()) {
 			for(EdgeInfo e : destGenes.get(node2))
 			list.add(new Edge(node1, node2, e));
 		}
 
 		return list;
 	}
 
 	/**
 	 * Return the starting nodes of all edges.
 	 * Please note that this method name may be misleading. It does not return all nodes.
 	 */
 	public List<Node> getNodes() {
 		return new ArrayList<Node>(geneRows.keySet());
 	}
 
 }
