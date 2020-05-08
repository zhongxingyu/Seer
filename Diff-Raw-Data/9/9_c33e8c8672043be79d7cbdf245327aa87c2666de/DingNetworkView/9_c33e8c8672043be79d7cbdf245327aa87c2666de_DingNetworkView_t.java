 /*
  Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)
 
  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies
 
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
 
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 package cytoscape.ding;
 
 import giny.view.EdgeView;
 import giny.view.NodeView;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import cytoscape.CyEdge;
 import cytoscape.CyNetwork;
 import cytoscape.CyNode;
 import cytoscape.Cytoscape;
 import cytoscape.CytoscapeInit;
 import cytoscape.layout.CyLayoutAlgorithm;
 import cytoscape.view.CyEdgeView;
 import cytoscape.view.CyNetworkView;
 import cytoscape.view.CyNodeView;
 import cytoscape.view.CytoscapeDesktop;
 import cytoscape.view.FlagAndSelectionHandler;
 import cytoscape.visual.VisualMappingManager;
 import cytoscape.visual.VisualStyle;
 import cytoscape.visual.ui.VizMapUI;
 import ding.view.DGraphView;
 import ding.view.EdgeContextMenuListener;
 import ding.view.NodeContextMenuListener;
 
 
 // AJK: 05/19/06 END
 /**
  *
  */
 public class DingNetworkView extends DGraphView implements CyNetworkView {
 	private String title;
 	private boolean vizmapEnabled = true;
 	private HashMap clientData = new HashMap();
 	private VisualStyle vs;
 	private final FlagAndSelectionHandler flagHandler;
 
 	/**
 	 * Creates a new DingNetworkView object.
 	 *
 	 * @param network  DOCUMENT ME!
 	 * @param title  DOCUMENT ME!
 	 */
 	public DingNetworkView(CyNetwork network, String title) {
 		super(network);
 		this.title = title;
 
 		final int[] nodes = network.getNodeIndicesArray();
 		final int[] edges = network.getEdgeIndicesArray();
 
 		final int nodeCount = nodes.length;
 		final int edgeCount = edges.length;
 
 		for (int i = 0; i < nodeCount; i++)
 			addNodeView(nodes[i]);
 
 		for (int i = 0; i < edgeCount; i++)
 			addEdgeView(edges[i]);
 
 		flagHandler = new FlagAndSelectionHandler(getNetwork().getSelectFilter(), this);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param vsName DOCUMENT ME!
 	 */
 	public void setVisualStyle(String vsName) {
 		vs = Cytoscape.getVisualMappingManager().getCalculatorCatalog().getVisualStyle(vsName);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public VisualStyle getVisualStyle() {
 		if (vs == null) {
 			String defaultStyle = CytoscapeInit.getProperties().getProperty("defaultVisualStyle");
 
 			if (defaultStyle == null)
 				defaultStyle = "default";
 
 			setVisualStyle(defaultStyle);
 		}
 
 		return vs;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public CyNetwork getNetwork() {
 		return (CyNetwork) getGraphPerspective();
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param title DOCUMENT ME!
 	 */
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public String getTitle() {
 		return title;
 	}
 
 	/**
	 *  Apply visual style and redraw graph.
 	 *
	 * @param layout Always ignored!
	 * @param vizmap Always ignored!
 	 */
 	public void redrawGraph(boolean layout, boolean vizmap) {
 		final VisualMappingManager vmm = Cytoscape.getVisualMappingManager();
 		vmm.setNetworkView(this);
 		vmm.applyAppearances();
 		updateView();
 	}
	
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public CyNetworkView getView() {
 		return this;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public VisualMappingManager getVizMapManager() {
 		// Believe it or not, this is the correct f***ing implementation.
 		return null;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public VizMapUI getVizMapUI() {
 		// Believe it or not, this is the correct f***ing implementation.
 		return null;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 */
 	public void toggleVisualMapperEnabled() {
 		vizmapEnabled = !vizmapEnabled;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param state DOCUMENT ME!
 	 */
 	public void setVisualMapperEnabled(boolean state) {
 		vizmapEnabled = state;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public boolean getVisualMapperEnabled() {
 		return vizmapEnabled;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param data_name DOCUMENT ME!
 	 * @param data DOCUMENT ME!
 	 */
 	public void putClientData(String data_name, Object data) {
 		clientData.put(data_name, data);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public Collection getClientDataNames() {
 		return clientData.keySet();
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param data_name DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public Object getClientData(String data_name) {
 		return clientData.get(data_name);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param nodes DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public boolean setSelected(CyNode[] nodes) {
 		return setSelected(convertToViews(nodes));
 	}
 
 	private NodeView[] convertToViews(CyNode[] nodes) {
 		NodeView[] views = new NodeView[nodes.length];
 
 		for (int i = 0; i < nodes.length; i++) {
 			views[i] = getNodeView(nodes[i]);
 		}
 
 		return views;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param node_views DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public boolean setSelected(NodeView[] node_views) {
 		for (int i = 0; i < node_views.length; i++) {
 			node_views[i].select();
 		}
 
 		return true;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param edge DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public boolean applyVizMap(CyEdge edge) {
 		return applyVizMap(getEdgeView(edge));
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param edge_view DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public boolean applyVizMap(EdgeView edge_view) {
 		return applyVizMap(edge_view, (VisualStyle) getClientData(CytoscapeDesktop.VISUAL_STYLE));
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param node DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public boolean applyVizMap(CyNode node) {
 		return applyVizMap(getNodeView(node));
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param node_view DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public boolean applyVizMap(NodeView node_view) {
 		return applyVizMap(node_view, (VisualStyle) getClientData(CytoscapeDesktop.VISUAL_STYLE));
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param edge DOCUMENT ME!
 	 * @param style DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public boolean applyVizMap(CyEdge edge, VisualStyle style) {
 		return applyVizMap(getEdgeView(edge), style);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param edge_view DOCUMENT ME!
 	 * @param style DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public boolean applyVizMap(EdgeView edge_view, VisualStyle style) {
 		VisualStyle old_style = Cytoscape.getDesktop().setVisualStyle(style);
 		Cytoscape.getVisualMappingManager().vizmapEdge(edge_view, this);
 		Cytoscape.getDesktop().setVisualStyle(old_style);
 
 		return true;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param node DOCUMENT ME!
 	 * @param style DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public boolean applyVizMap(CyNode node, VisualStyle style) {
 		return applyVizMap(getNodeView(node), style);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param node_view DOCUMENT ME!
 	 * @param style DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public boolean applyVizMap(NodeView node_view, VisualStyle style) {
 		VisualStyle old_style = Cytoscape.getDesktop().setVisualStyle(style);
 		Cytoscape.getVisualMappingManager().vizmapNode(node_view, this);
 		Cytoscape.getDesktop().setVisualStyle(old_style);
 
 		return true;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param edges DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public boolean setSelected(CyEdge[] edges) {
 		return setSelected(convertToViews(edges));
 	}
 
 	private EdgeView[] convertToViews(CyEdge[] edges) {
 		EdgeView[] views = new EdgeView[edges.length];
 
 		for (int i = 0; i < edges.length; i++) {
 			views[i] = getEdgeView(edges[i]);
 		}
 
 		return views;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param edge_views DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public boolean setSelected(EdgeView[] edge_views) {
 		for (int i = 0; i < edge_views.length; i++) {
 			edge_views[i].select();
 		}
 
 		return true;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param style DOCUMENT ME!
 	 */
 	public void applyVizmapper(VisualStyle style) {
 		final VisualStyle old_style = Cytoscape.getDesktop().setVisualStyle(style);
 		redrawGraph(false, true);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param layout DOCUMENT ME!
 	 */
 	public void applyLayout(CyLayoutAlgorithm layout) {
 		layout.doLayout(this);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param layout DOCUMENT ME!
 	 * @param nodes DOCUMENT ME!
 	 * @param edges DOCUMENT ME!
 	 */
 	public void applyLockedLayout(CyLayoutAlgorithm layout, CyNode[] nodes, CyEdge[] edges) {
 		layout.lockNodes(convertToViews(nodes));
 		layout.setSelectedOnly(true);
 		layout.doLayout(this);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param layout DOCUMENT ME!
 	 * @param nodes DOCUMENT ME!
 	 * @param edges DOCUMENT ME!
 	 */
 	public void applyLayout(CyLayoutAlgorithm layout, CyNode[] nodes, CyEdge[] edges) {
 		layout.lockNodes(getInverseViews(convertToViews(nodes)));
 		layout.setSelectedOnly(true);
 		layout.doLayout(this);
 	}
 
 	private NodeView[] getInverseViews(NodeView[] given) {
 		// This code, like most all of the code in this class, is copied from
 		// PhoebeNetworkView.  Zum kotzen.
 		final NodeView[] inverse = new NodeView[getNodeViewCount() - given.length];
 		final List<NodeView> node_views = getNodeViewsList();
 
 		int count = 0;
 		// Arrays.sort(given);
 		HashSet<NodeView>viewSet = new HashSet();
 		for (int g = 0; g < given.length; g++)
 			viewSet.add(given[g]);
 
 		for (NodeView view : node_views) {
 			if (!viewSet.contains(view)) {
 				inverse[count] = view;
 				count++;
 			}
 		}
 		return inverse;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public List getNodeViewsList() {
 		final List<NodeView> list = new ArrayList<NodeView>(getNodeViewCount());
 		final int[] gp_indices = getGraphPerspective().getNodeIndicesArray();
 		final int gpSize = gp_indices.length;
 
 		for (int i = 0; i < gpSize; i++)
 			list.add(getNodeView(gp_indices[i]));
 
 		return list;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param layout DOCUMENT ME!
 	 * @param nodes DOCUMENT ME!
 	 * @param edges DOCUMENT ME!
 	 */
 	public void applyLockedLayout(CyLayoutAlgorithm layout, CyNodeView[] nodes, CyEdgeView[] edges) {
 		layout.lockNodes(nodes);
 		layout.setSelectedOnly(true);
 		layout.doLayout();
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param layout DOCUMENT ME!
 	 * @param nodes DOCUMENT ME!
 	 * @param edges DOCUMENT ME!
 	 */
 	public void applyLayout(CyLayoutAlgorithm layout, CyNodeView[] nodes, CyEdgeView[] edges) {
 		layout.lockNodes(getInverseViews(nodes));
 		layout.setSelectedOnly(true);
 		layout.doLayout(this);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param layout DOCUMENT ME!
 	 * @param nodes DOCUMENT ME!
 	 * @param edges DOCUMENT ME!
 	 */
 	public void applyLockedLayout(CyLayoutAlgorithm layout, int[] nodes, int[] edges) {
 		layout.lockNodes(convertToNodeViews(nodes));
 		layout.setSelectedOnly(true);
 		layout.doLayout(this);
 	}
 
 	private NodeView[] convertToNodeViews(int[] nodes) {
 		final NodeView[] views = new NodeView[nodes.length];
 		final int size = views.length;
 
 		for (int i = 0; i < size; i++)
 			views[i] = getNodeView(nodes[i]);
 
 		return views;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param layout DOCUMENT ME!
 	 * @param nodes DOCUMENT ME!
 	 * @param edges DOCUMENT ME!
 	 */
 	public void applyLayout(CyLayoutAlgorithm layout, int[] nodes, int[] edges) {
 		layout.lockNodes(getInverseViews(convertToNodeViews(nodes)));
 		layout.setSelectedOnly(true);
 		layout.doLayout(this);
 	}
 
 	// AJK: 05/19/06 BEGIN
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param l DOCUMENT ME!
 	 */
 	public void addNodeContextMenuListener(NodeContextMenuListener l) {
 		super.addNodeContextMenuListener(l);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param l DOCUMENT ME!
 	 */
 	public void removeNodeContextMenuListener(NodeContextMenuListener l) {
 		super.removeNodeContextMenuListener(l);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param l DOCUMENT ME!
 	 */
 	public void addEdgeContextMenuListener(EdgeContextMenuListener l) {
 		super.addEdgeContextMenuListener(l);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param l DOCUMENT ME!
 	 */
 	public void removeEdgeContextMenuListener(EdgeContextMenuListener l) {
 		super.removeEdgeContextMenuListener(l);
 	}
 
 	// AJK: 05/19/06 END
 }
