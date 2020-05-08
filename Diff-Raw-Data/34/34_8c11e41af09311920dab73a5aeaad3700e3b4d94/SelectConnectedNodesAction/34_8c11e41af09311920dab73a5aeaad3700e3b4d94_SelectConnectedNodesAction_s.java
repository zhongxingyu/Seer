 /*
  File: SelectConnectedNodesAction.java
 
  Copyright (c) 2007, The Cytoscape Consortium (www.cytoscape.org)
 
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
 
 //-------------------------------------------------------------------------
 package cytoscape.actions;
 
 import cytoscape.CyNetwork;
 import cytoscape.Cytoscape;

 import cytoscape.util.CytoscapeAction;

//-------------------------------------------------------------------------
 import giny.model.Node;
 import giny.model.Edge;

 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;

 import java.util.ArrayList;
import java.util.Iterator;
 import java.util.List;

import javax.swing.KeyStroke;

 import javax.swing.event.MenuEvent;
 
 //-------------------------------------------------------------------------
 /**
  * select every node connected to the current selected edges.
  */
 public class SelectConnectedNodesAction extends CytoscapeAction {
 	/**
 	 * Creates a new SelectConnectedNodesAction object.
 	 */
 	public SelectConnectedNodesAction() {
 		super("Nodes connected by selected edges");
 		setPreferredMenu("Select.Nodes");
 		setAcceleratorCombo(KeyEvent.VK_7, ActionEvent.CTRL_MASK);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param e DOCUMENT ME!
 	 */
 	public void actionPerformed(ActionEvent e) {
 		final CyNetwork currentNetwork = Cytoscape.getCurrentNetwork();
		final List<Edge> selectedEdges = new ArrayList<Edge>(currentNetwork.getSelectedEdges());
 
 		for (Edge edge: selectedEdges) {
			currentNetwork.setSelectedNodeState(edge.getSource(), true);
			currentNetwork.setSelectedNodeState(edge.getTarget(), true);
 		}

 		Cytoscape.getCurrentNetworkView().updateView();
 	} // actionPerformed
 
     public void menuSelected(MenuEvent e) {
         enableForNetwork();
     }
 }
