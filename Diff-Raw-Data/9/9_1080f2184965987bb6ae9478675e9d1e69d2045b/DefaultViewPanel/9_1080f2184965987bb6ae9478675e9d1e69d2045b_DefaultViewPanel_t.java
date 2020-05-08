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
 package cytoscape.visual.ui;
 
 import giny.view.GraphView;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JPanel;
 
 import cytoscape.CyEdge;
 import cytoscape.CyNetwork;
 import cytoscape.CyNode;
 import cytoscape.Cytoscape;
 import cytoscape.ding.DingNetworkView;
 import cytoscape.view.CyNetworkView;
 import ding.view.DGraphView;
 
 
 /**
  * Panel to show the default properties visually (as graphics).
  *
  * @version 0.6
  * @since Cytoscape 2.5
  * @author kono
   */
 public class DefaultViewPanel extends JPanel {
 	private static final int PADDING = 20;
 	private CyNetworkView view;
 	private CyNetworkView oldView;
 	private static CyNetwork dummyNet;
 	private Color background;
 
 	/*
 	 * Dummy graph component
 	 */
 	private static final CyNode source;
 	private static final CyNode target;
 	private static final CyEdge edge;
 	private Component canvas = null;
 
 	static {
 		source = Cytoscape.getCyNode("Source");
 		target = Cytoscape.getCyNode("Target");
 		edge = Cytoscape.getCyEdge(source.getIdentifier(), "Edge", target.getIdentifier(),
 		                           "interaction");
 
 		List nodes = new ArrayList();
 		List edges = new ArrayList();
 		nodes.add(source);
 		nodes.add(target);
 		edges.add(edge);
 
 		dummyNet = Cytoscape.getRootGraph().createNetwork(nodes, edges);
		dummyNet.setTitle("Default Appearance");
 	}
 
 	/**
 	 * Creates a new NodeFullDetailView object.
 	 */
 	public DefaultViewPanel() {
 		oldView = Cytoscape.getVisualMappingManager().getNetworkView();
 
 		background = Cytoscape.getVisualMappingManager().getVisualStyle()
 		                      .getGlobalAppearanceCalculator().getDefaultBackgroundColor();
 		this.setBackground(background);
 	}
 
 	protected void updateBackgroungColor(final Color newColor) {
 		background = newColor;
 		this.setBackground(background);
 		repaint();
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public Component getCanvas() {
 		return canvas;
 	}
 
 	/**
 	 * Create dummy network
 	 */
 	protected void createDummyNetworkView() {
 		view = new DingNetworkView(dummyNet, "Default Appearence");
 
 		view.setIdentifier(dummyNet.getIdentifier());
 		view.setTitle(dummyNet.getTitle());
 
 		view.getNodeView(source).setOffset(0, 0);
 		view.getNodeView(target).setOffset(150, 10);
 		Cytoscape.getVisualMappingManager().setNetworkView(view);
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 */
 	public void clean() {
 		Cytoscape.destroyNetwork(dummyNet);
 		Cytoscape.getVisualMappingManager().setNetworkView(oldView);
 		dummyNet = null;
 		canvas = null;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 */
 	protected void updateView() {
 		if (view != null) {
 			Cytoscape.getVisualMappingManager().setNetworkView(view);
 			view.setVisualStyle(Cytoscape.getVisualMappingManager().getVisualStyle().getName());
 
 			final Dimension panelSize = this.getSize();
 			((DGraphView) view).getCanvas()
 			 .setSize(new Dimension((int) panelSize.getWidth() - PADDING,
 			                        (int) panelSize.getHeight() - PADDING));
 			view.fitContent();
 			canvas = (view.getComponent());
 
 			for (MouseListener listener : canvas.getMouseListeners())
 				canvas.removeMouseListener(listener);
 
 			this.removeAll();
 			this.add(canvas);
 
 			canvas.setLocation(PADDING / 2, PADDING / 2);
 			Cytoscape.getVisualMappingManager().applyAppearances();
 
 			if ((background != null) && (canvas != null)) {
 				canvas.setBackground(background);
 			}
 		}
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public GraphView getView() {
 		return view;
 	}
 }
