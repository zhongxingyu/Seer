 /**
  * * Copyright (c) 2007 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
  * * Research, University of Toronto
  * *
  * * Code written by: Michael Matan
  * * Authors: Michael Matan, Gary D. Bader
  * *
  * * This library is free software; you can redistribute it and/or modify it
  * * under the terms of the GNU Lesser General Public License as published
  * * by the Free Software Foundation; either version 2.1 of the License, or
  * * any later version.
  * *
  * * This library is distributed in the hope that it will be useful, but
  * * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  * * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  * * documentation provided hereunder is on an "as is" basis, and
  * * University of Toronto
  * * has no obligations to provide maintenance, support,
  * * updates, enhancements or modifications.  In no event shall the
  * * University of Toronto
  * * be liable to any party for direct, indirect, special,
  * * incidental or consequential damages, including lost profits, arising
  * * out of the use of this software and its documentation, even if
  * * University of Toronto
  * * has been advised of the possibility of such damage.  See
  * * the GNU Lesser General Public License for more details.
  * *
  * * You should have received a copy of the GNU Lesser General Public License
  * * along with this library; if not, write to the Free Software Foundation,
  * * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
  * *
  * * Description: Aggregation class for objects participating in a GOSlimmer session
  */
 package org.ccbr.bader.yeast;
 
 import giny.model.Edge;
 import giny.model.Node;
 import giny.view.NodeView;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import java.awt.Color;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.xml.bind.JAXBException;
 
 import org.ccbr.bader.geneassociation.GeneAssociationReaderUtil;
 import org.ccbr.bader.yeast.controller.GOSlimmerController;
 import org.ccbr.bader.yeast.model.GOSlimmerCoverageStatBean;
 import org.ccbr.bader.yeast.view.gui.AdvancedViewSettingsPanel;
 import org.ccbr.bader.yeast.view.gui.FileExportPanel;
 import org.ccbr.bader.yeast.view.gui.GOSlimmerGeneAssociationDialog;
 import org.ccbr.bader.yeast.view.gui.NodeContextMenuActionListener;
 import org.ccbr.bader.yeast.view.gui.GOSlimmerGUIViewSettings;
 
 import cytoscape.CyNetwork;
 import cytoscape.Cytoscape;
 import cytoscape.bookmarks.Bookmarks;
 import cytoscape.data.CyAttributes;
 import cytoscape.task.Task;
 import cytoscape.task.TaskMonitor;
 import cytoscape.task.util.TaskManager;
 import cytoscape.view.CyNetworkView;
 import cytoscape.view.cytopanels.CytoPanelState;
 import cytoscape.visual.VisualMappingManager;
 import cytoscape.visual.VisualStyle;
 import ding.view.NodeContextMenuListener;
 
 /**Aggregates the different objects associated with a given GOSlimmer session, including the subgraph CyNetworks, 
  * controllers for each subgraph, and the associated model objects.
  * 
  * @author mikematan
  *
  */
 public class GOSlimmerSession {
 
 	//The Subgraphs of the GO Network representing each of the three sub ontologies of the GO DAG
 	private CyNetwork molFunSubGraph = null;
 	private CyNetwork bioProSubGraph = null; 
 	private CyNetwork celComSubGraph = null; 
 	
 	//TODO migrate this info to the GONamespace enum
 	private final String bio_pro_name = "biological_process";
 	private final String mol_fun_name = "molecular_function";
 	private final String cel_com_name = "cellular_component";
 	
 	private GOSlimmerController molFunController = null;
 	private GOSlimmerController bioProController = null;
 	private GOSlimmerController celComController = null;
 	
 	private boolean userGeneSetImported = false;
 	
 	private Collection<String> userSpecifiedGeneIdSet;
 	
 	private String ontologyName;
 	
 	private GeneAssociationReaderUtil garu;
 	
 	public GeneAssociationReaderUtil getGaru() {
 		return garu;
 	}
 
 	public void setGaru(GeneAssociationReaderUtil garu) {
 		this.garu = garu;
 	}
 
 	public GOSlimmerSession(CyNetwork goNetwork) {
 
 
 //		if (!alreadyOpened) {
 //			//initialize the goSlimPanel and add it to the cytopanel
 //			goSlimPanel = new GOSlimPanel();
 //			cytoPanel.add(goSlimPanel);
 //			alreadyOpened = true;
 //		}
 		VisualMappingManager vmm = Cytoscape.getVisualMappingManager();
         if (!vmm.getCalculatorCatalog().getVisualStyleNames().contains("GOSLIMMERVS")) {
             vmm.getCalculatorCatalog().addVisualStyle(new GOSlimmerVisualStyle(vmm.getVisualStyle(),"GOSLIMMERVS"));
         }
 		vmm.setVisualStyle("GOSLIMMERVS");
 		
 		CyNetworkView networkView = Cytoscape.getCurrentNetworkView();
 		CyNetwork network = Cytoscape.getCurrentNetwork();
 		
 		
 		//List<CyNetwork> subNetworks = createOntologyNamespaceSubGraphs(network);
 		List<CyNetwork> subNetworks = new ArrayList<CyNetwork>();
 
 		ontologyName = network.getTitle();
 
 		//GOSlimmerCoverageStatBean statBean=null;
 		
 		//we can use this to get a list of the ontology names
 		//Set<String> ontologyNames = Cytoscape.getOntologyServer().getOntologyNames();
 //		Cytoscape.getOntologyServer().addOntology(onto);
 
 		CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
 		
 		int associatedGeneCount = 0;
 
 		//create the subgraphs
 		createOntologyNamespaceSubGraphs(network);
 		//initialize the subgraphs with their controllers and coverage statistics beans
 
 		
 		subNetworks.add(molFunSubGraph);
 		subNetworks.add(bioProSubGraph);
 		subNetworks.add(celComSubGraph);
 		
 
 			
 
 		for (final CyNetwork subNetwork:subNetworks) {	
 			TaskManager.executeTask(new Task() {
 
 				public String getTitle() {
 					return "Creating Views for GO Subgraphs";
 				}
 
 				public void halt() {
 					// TODO Auto-generated method stub
 					
 				}
 
 				public void run() {
 					CyNetworkView subNetworkView = Cytoscape.createNetworkView(subNetwork);
 					//TODO replace these with undeprecated analogues
 					
 					VisualStyle vs = Cytoscape.getVisualMappingManager().setVisualStyle("GOSLIMMERVS");
 					subNetworkView.redrawGraph(false,false);
 					//Cytoscape.getVisualMappingManager().setVisualStyle(vs);
 					subNetworkView.setVisualStyle("GOSLIMMERVS");
 					
 				}
 
 				public void setTaskMonitor(TaskMonitor arg0) throws IllegalThreadStateException {
 					// TODO Auto-generated method stub
 					
 				}
 				
 			}, null);
 
 			
 		}
 		//Note, this must be done after the network views are created, since the controllers are associated with a particular view ; TODO eliminate this dependancy, view should be modifiable on it's own, though the hiding and unhiding of nodes should effect the model
 		//TODO continues from above:  view should need to know about the controller, but controller shouldn't need to know view enough to do detailed view manipulation;  implement view manipulation code in node context menu listener, and have it call the controller only when it needs to select/deselect nodes based on their being hidden or not
 		initializeControllersForSubGraphs();
 		
 		//initialize the nodecontextmenu's for each of the subgraphs views;  NOTE if a networkview does not yet exist for these graphs, then this fail without any warning or indication
 		Cytoscape.getNetworkView(molFunSubGraph.getIdentifier()).addNodeContextMenuListener(getGOSlimmerNodeContextMenuListener(molFunController));
 		Cytoscape.getNetworkView(bioProSubGraph.getIdentifier()).addNodeContextMenuListener(getGOSlimmerNodeContextMenuListener(bioProController));
 		Cytoscape.getNetworkView(celComSubGraph.getIdentifier()).addNodeContextMenuListener(getGOSlimmerNodeContextMenuListener(celComController));
 
 		
 		//perform some initialization of the subgraph views, so that the user isn't confronted with a hugh and covoluted network
 		//HierarchicalLayoutListener hll = new HierarchicalLayoutListener();
 		//For starters, show only the first few levels of the heirarchy
 		for(GOSlimmerController controller:namespaceToController.values()) {
 			Node rootNode  = GOSlimmerUtil.getRootNode(controller.getNetwork());
 			//collapse the root node; TODO revise to do in a more efficient manner (e.g.
 			
 			//controller.collapseNode(rootNode);
 			//Collection<Node> expandedNodes = controller.expandNodeToDepthAndReturnDAGNodes(rootNode,2);
 			
 //			controller.getNetworkView().fitContent();
 			
 			//select the dag nodes, and apply heirarchical layout upon them
 			//first we have to select the unhidden nodes, so that the heirarchical layout is only applied to them
 			//controller.getNetwork().setSelectedNodeState(expandedNodes, true);
 			//Cytoscape.setCurrentNetwork(controller.getNetwork().getIdentifier());
 			//Cytoscape.setCurrentNetworkView(controller.getNetworkView().getIdentifier());
 			//apply the heirarchical layout onto the newly created view
 			//hll will execute the layout algorithm on the currently selected view, which should be our newly created one
 			//hll.actionPerformed(null);
 			
 			//I only want the first level of expansion shown at first, though I want the 2nd level layout out properly for later expansion.
 			//TODO determine if this is acceptable given the performance penalty it entails.  \
             controller.showNode(rootNode);
             controller.collapseNode(rootNode);
 			controller.expandNodeToDepth(rootNode, 1, true);
 			
 			//zoom view to fit all content, and then update it.  Code inspired from cytoscape.actions.ZoomSelectedAction 
 			controller.getNetworkView().fitContent();
 			controller.getNetworkView().updateView();
 		}
 		
 	}
 	
 	private final CyAttributes nodeAtt = Cytoscape.getNodeAttributes();
 	
 	private List<CyNetwork> createOntologyNamespaceSubGraphs(CyNetwork network) {
 		
 		Set<Node> molFunNodes = new HashSet<Node>();
 		Set<Edge> molFunEdges = new HashSet<Edge>();
 		Set<Node> bioProNodes = new HashSet<Node>();
 		Set<Edge> bioProEdges = new HashSet<Edge>();
 		Set<Node> celComNodes = new HashSet<Node>();
 		Set<Edge> celComEdges = new HashSet<Edge>();
 		
 		Iterator nodeIterator = network.nodesIterator();
 		Node node = null;
 		while(nodeIterator.hasNext()) {
 			node = (Node) nodeIterator.next();
             // add formatted ontology name to node attributes
             String name = nodeAtt.getStringAttribute(node.getIdentifier(), "ontology.name");
             nodeAtt.setAttribute(node.getIdentifier(), GOSlimmer.formattedOntologyNameAttributeName, formatName(name));
             String nodeOntologyNamespace = nodeAtt.getStringAttribute(node.getIdentifier(), "ontology.namespace");
 			if (bio_pro_name.equalsIgnoreCase(nodeOntologyNamespace)) {
 				bioProNodes.add(node);
 				//bioProEdges.addAll(network.getAdjacentEdgesList(node, true, true, true));
 			}
 			else if (mol_fun_name.equalsIgnoreCase(nodeOntologyNamespace)) {
 				molFunNodes.add(node);
 				//molFunEdges.addAll(network.getAdjacentEdgesList(node, true, true, true));
 			}
 			else if (cel_com_name.equalsIgnoreCase(nodeOntologyNamespace)) {
 				celComNodes.add(node);
 				//celComEdges.addAll(network.getAdjacentEdgesList(node, true, true, true));
 			}
 		}
 
         bioProEdges.addAll(network.getConnectingEdges(new ArrayList(bioProNodes)));
         molFunEdges.addAll(network.getConnectingEdges(new ArrayList(molFunNodes)));
         celComEdges.addAll(network.getConnectingEdges(new ArrayList(celComNodes)));
 
         String networkTitle = network.getTitle();
		CyNetwork molFunNetwork = Cytoscape.createNetwork(molFunNodes, molFunEdges, networkTitle + "_molecular_function", network, false);
		CyNetwork bioProNetwork = Cytoscape.createNetwork(bioProNodes, bioProEdges, networkTitle + "_biological_process", network, false);
		CyNetwork celComNetwork = Cytoscape.createNetwork(celComNodes, celComEdges, networkTitle + "_cellular_component", network, false);
 		List<CyNetwork> subNetworks = new ArrayList<CyNetwork>();
 		subNetworks.add(molFunNetwork);
 		subNetworks.add(bioProNetwork);
 		subNetworks.add(celComNetwork);
 		
 		molFunSubGraph = molFunNetwork;
 		bioProSubGraph = bioProNetwork;
 		celComSubGraph = celComNetwork;
 		
 		return subNetworks;
 	}
 	
 	private NodeContextMenuListener getGOSlimmerNodeContextMenuListener(final GOSlimmerController controller) {
 		//final GOSlimmerController slimmerController = this.controller;
 		
 		//instead of adding the above graphviewchangeevent listener, could instead add to the node context menu
 		return new NodeContextMenuListener(){
 
 			public void addNodeContextMenuItems(NodeView node, JPopupMenu menu) {
 				
 				//initialize the listener which will act when the buttons are pressed
 				NodeContextMenuActionListener listener = new NodeContextMenuActionListener(node.getNode(),controller);
 				
 				//for each button, initialize it with the proper name, add the above actionlistener and add to the context menu
 				for(String menuItemName:new String[]{"Collapse","Prune","Expand","Deselect"}) {
 					JMenuItem menuItem = new JMenuItem(menuItemName);
 					//menuItem.addActionListener(slimmerController);
 					menuItem.addActionListener(listener);
 					menu.add(menuItem);
 				}
 
                 /* Add Select button to context menu */
                 /* If no genes are associated with this term, make menu item red and append '(No Associated Genes)' */
                 String itemName = "Select";
                 Color itemColor;
 
                 int numAssociatedTermsStr = nodeAtt.getIntegerAttribute(node.getNode().getIdentifier(), GOSlimmer.directlyAnnotatedGeneNumberAttributeName);
                 if (numAssociatedTermsStr == 0) {
                     itemName = itemName + " (No Associated Genes)";
                     itemColor = new Color(255,0,0);
                 }
                 else {
                     itemColor = new Color(0,0,0);
                 }
                 JMenuItem menuItem = new JMenuItem(itemName);
                 menuItem.setForeground(itemColor);
                 menuItem.addActionListener(listener);
                 menu.add(menuItem);
             }
 			
 		};
 	}
 	
 	private void initializeControllersForSubGraphs() {
 //		GOSlimmerCoverageStatBean molFunStatBean = new GOSlimmerCoverageStatBean(garu.getMolecularFunctionGeneIds().size());
 //		GOSlimmerCoverageStatBean bioProStatBean = new GOSlimmerCoverageStatBean(garu.getBiologicalProcessGeneIds().size());
 //		GOSlimmerCoverageStatBean celComStatBean = new GOSlimmerCoverageStatBean(garu.getCellularComponentGeneIds().size());
 		
 //		molFunController = new GOSlimmerController(molFunSubGraph,Cytoscape.getNetworkView(molFunSubGraph.getIdentifier()),molFunStatBean,goSlimPanel.getMolFunCoverage());
 //		bioProController = new GOSlimmerController(bioProSubGraph,Cytoscape.getNetworkView(bioProSubGraph.getIdentifier()),bioProStatBean,goSlimPanel.getBioProCoverage());
 //		celComController = new GOSlimmerController(celComSubGraph,Cytoscape.getNetworkView(celComSubGraph.getIdentifier()),celComStatBean,goSlimPanel.getCelComCoverage());
 		
 		
 		molFunController = new GOSlimmerController(GONamespace.MolFun,molFunSubGraph,Cytoscape.getNetworkView(molFunSubGraph.getIdentifier()),new GOSlimmerCoverageStatBean(1,this),this);
 		bioProController = new GOSlimmerController(GONamespace.BioPro,bioProSubGraph,Cytoscape.getNetworkView(bioProSubGraph.getIdentifier()),new GOSlimmerCoverageStatBean(1,this),this);
 		celComController = new GOSlimmerController(GONamespace.CelCom,celComSubGraph,Cytoscape.getNetworkView(celComSubGraph.getIdentifier()),new GOSlimmerCoverageStatBean(1,this),this);
 		this.namespaceToController.put(GONamespace.BioPro, bioProController);
 		this.namespaceToController.put(GONamespace.CelCom, celComController);
 		this.namespaceToController.put(GONamespace.MolFun, molFunController);
 		
 		molFunController.setExpansionDepth(1);molFunController.setUseFiniteExpansionDepth(true);
 		bioProController.setExpansionDepth(1);bioProController.setUseFiniteExpansionDepth(true);
 		celComController.setExpansionDepth(1);celComController.setUseFiniteExpansionDepth(true);
 		
 //		molFunController = new GOSlimmerController(molFunSubGraph,Cytoscape.getNetworkView(molFunSubGraph.getIdentifier()),molFunStatBean);
 //		bioProController = new GOSlimmerController(bioProSubGraph,Cytoscape.getNetworkView(bioProSubGraph.getIdentifier()),bioProStatBean);
 //		celComController = new GOSlimmerController(celComSubGraph,Cytoscape.getNetworkView(celComSubGraph.getIdentifier()),celComStatBean);
 		
 		
 		
 	}
 
 	private Map<GONamespace, GOSlimmerController> namespaceToController = new HashMap();
 //	private int userGeneCount;
 	private Collection<String> userGeneSet;
 	private Collection<String> unmatchedUserGeneIds;
 	private GOSlimPanel goSlimPanel;
 	
 	public Map<GONamespace, GOSlimmerController> getNamespaceToController() {
 		return namespaceToController;
 	}
 
 	public String getOntologyName() {
 		return ontologyName;
 	}
 
 	public void setOntologyName(String ontologyName) {
 		this.ontologyName = ontologyName;
 	}
 
 	public boolean isUserGeneSetImported() {
 		return userGeneSetImported;
 	}
 
 	public void setUserGeneSetImported(boolean userGeneSetImported) {
 		this.userGeneSetImported = userGeneSetImported;
 	}
 
 //	public void setUserGeneCount(int userGeneCount) {
 //		this.userGeneCount = userGeneCount;
 ////		for(GOSlimmerController controller: namespaceToController.values()) {
 ////			controller.getStatBean().resetUserGeneStatistics(userGeneCount);
 ////		}
 //	}
 	
 //	public int getUserGeneCount() {
 //		return this.userGeneCount;
 //	}
 
 	public Collection<String> getUserSpecifiedGeneIdSet() {
 		return userSpecifiedGeneIdSet;
 	}
 
 	public void setUserSpecifiedGeneIdSet(Collection<String> userSpecifiedGeneIdSet) {
 		this.userSpecifiedGeneIdSet = userSpecifiedGeneIdSet;
 	}
 
 	public void setUserGeneSet(Collection<String> userGeneSet) {
 		this.userGeneSet = userGeneSet;
 		
 	}
 
 	public Collection<String> getUserGeneSet() {
 		return this.userGeneSet;
 	}
 
 	public Collection<String> getUnmatchedUserGeneIds() {
 		return unmatchedUserGeneIds;
 	}
 
 	public void setUnmatchedUserGeneIds(Collection<String> unmatchedUserGeneIds) {
 		this.unmatchedUserGeneIds = unmatchedUserGeneIds;
 	}
 
 	public void setGOSlimPanel(GOSlimPanel goSlimPanel) {
 		this.goSlimPanel = goSlimPanel;
 	}
 	
 	public GOSlimPanel getGOSlimPanel() {
 		return this.goSlimPanel;
 	}
 
     private String formatName(String name) {
         int curLength = 0;
         String newName = "";
         int maxSize = GOSlimmerGUIViewSettings.formattedOntologyNameMaxLength;
 
         Pattern pattern = Pattern.compile("[ \t\n\f\r]");
         Matcher matcher = pattern.matcher(name);
         int index = 0;
         while (matcher.find(index)) {
             String word = name.substring(index, matcher.start());
             String whiteSpace = name.substring(matcher.start(), matcher.end());
 
             if (curLength + word.length() + whiteSpace.length() < maxSize) {
                 newName = newName + word + whiteSpace;
                 curLength = curLength + word.length() + whiteSpace.length();
             }
             else if (curLength + word.length() < maxSize) {
                 newName = newName + word + "\n";
                 curLength = 0;
             }
             else {
                 newName = newName + "\n" + word + whiteSpace;
                 curLength = word.length() + whiteSpace.length();
             }
 
             index = matcher.end();
         }
         String lastWord = name.substring(index);
         if (curLength + lastWord.length() > maxSize) {
             newName = newName + "\n" + lastWord;
         }
         else {
             newName = newName + lastWord;
         }
 
         return newName;
         
     }
 	
 	
 }
