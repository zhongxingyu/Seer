 /*
  File: NetworkPanel.java
 
  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
 
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
 package cytoscape.view;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.swing.BoxLayout;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.InputMap;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTable;
 import javax.swing.JToggleButton;
 import javax.swing.JTree;
 import javax.swing.KeyStroke;
 import javax.swing.ListSelectionModel;
 import javax.swing.SpringLayout;
 import javax.swing.ToolTipManager;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.SwingPropertyChangeSupport;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 
 import cytoscape.CyNetwork;
 import cytoscape.CyNetworkTitleChange;
 import cytoscape.Cytoscape;
 import cytoscape.actions.ApplyVisualStyleAction;
 import cytoscape.actions.CreateNetworkViewAction;
 import cytoscape.data.SelectEvent;
 import cytoscape.data.SelectEventListener;
 import cytoscape.logger.CyLogger;
 import cytoscape.util.CyNetworkNaming;
 import cytoscape.util.swing.JTreeTable;
 import cytoscape.view.cytopanels.BiModalJSplitPane;
 import ding.view.DGraphView;
 
 
 /**
  * GUI component for managing network list in current session.
  */
 public class NetworkPanel extends JPanel implements PropertyChangeListener, TreeSelectionListener,
                                                     SelectEventListener, ChangeListener {
 	
 	private static final long serialVersionUID = -7102083850894612840L;
 	
 	private static final int DEF_DEVIDER_LOCATION = 280;
 	private static final int PANEL_PREFFERED_WIDTH = 250;
 	
 	private static final Dimension COMMAND_BUTTON_SIZE = new Dimension(16, 16);
 	
 	private static final int NETWORK_ICON_SIZE = 64;
 	private static final int DEF_ROW_HEIGHT = 20;
 	
 	// Make this panel as a source of events.
 	private final SwingPropertyChangeSupport pcs = new SwingPropertyChangeSupport(this);
 		
 	private final JTreeTable treeTable;
 	private final NetworkTreeNode root;
 	
 	private JPanel navigatorPanel;
 	
 	private JPanel networkTreePanel;
 		
 	private JPopupMenu popup;
 	private PopupActionListener popupActionListener;
 
 	private JMenuItem createViewItem;
 	private JMenuItem destroyViewItem;
 	private JMenuItem destroyNetworkItem;
 	private JMenuItem editNetworkTitle;
 	private JMenuItem applyVisualStyleMenu;
 	
 	private BiModalJSplitPane split;
 	
 	private final NetworkTreeTableModel treeTableModel;
 	private final CytoscapeDesktop cytoscapeDesktop;
 
 	/**
 	 * Constructor for the Network Panel.
 	 *
 	 * @param desktop
 	 */
 	public NetworkPanel(final CytoscapeDesktop desktop) {
 		super();
 		this.cytoscapeDesktop = desktop;
 
 		root = new NetworkTreeNode("Network Root", "root");
 		treeTableModel = new NetworkTreeTableModel(root);
 		
 		treeTable = new JTreeTable(treeTableModel);
 		treeTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 		initialize();
 
 		/*
 		 * Remove CTR-A for enabling select all function in the main window.
 		 */
 		for (KeyStroke listener : treeTable.getRegisteredKeyStrokes()) {
 			if (listener.toString().equals("ctrl pressed A")) {
 				final InputMap map = treeTable.getInputMap();
 				map.remove(listener);
 				treeTable.setInputMap(WHEN_FOCUSED, map);
 				treeTable.setInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, map);
 			}
 		}
 		
 		// Make this a prop change listener for Cytoscape global events.
 		Cytoscape.getPropertyChangeSupport().addPropertyChangeListener(this);
 		
 		// For listening to adding/removing Visual Style events.
 		Cytoscape.getVisualMappingManager().addChangeListener(this);
 	}
 
 	/**
 	 * Initialize GUI components
 	 */
 	private void initialize() {
 		setLayout(new BorderLayout());
 		setPreferredSize(new Dimension(PANEL_PREFFERED_WIDTH, 700));
 		
 		networkTreePanel = new JPanel();
 		networkTreePanel.setLayout(new BoxLayout(networkTreePanel, BoxLayout.Y_AXIS));
 
 		treeTable.getTree().addTreeSelectionListener(this);
 		treeTable.getTree().setRootVisible(false);
 
 		ToolTipManager.sharedInstance().registerComponent(treeTable);
 
 		treeTable.getTree().setCellRenderer(new TreeCellRenderer());
 
 		resetTable();
 
 		navigatorPanel = new JPanel();
 		navigatorPanel.setMinimumSize(new Dimension(180, 180));
 		navigatorPanel.setMaximumSize(new Dimension(180, 180));
 		navigatorPanel.setPreferredSize(new Dimension(180, 180));
 
 		final JScrollPane scroll = new JScrollPane(treeTable);
 
 		networkTreePanel.add(scroll);
 		split = new BiModalJSplitPane(cytoscapeDesktop, JSplitPane.VERTICAL_SPLIT,
 		                              BiModalJSplitPane.MODE_SHOW_SPLIT, networkTreePanel, navigatorPanel);
 		split.setResizeWeight(1);
 		split.setDividerLocation(DEF_DEVIDER_LOCATION);
 		add(split);
 
 		// this mouse listener listens for the right-click event and will show
 		// the pop-up
 		// window when that occurrs
 		treeTable.addMouseListener(new PopupListener());
 
 		// create and populate the popup window
 		popup = new JPopupMenu();
 		editNetworkTitle = new JMenuItem(PopupActionListener.EDIT_TITLE);
 		createViewItem = new JMenuItem(PopupActionListener.CREATE_VIEW);
 		destroyViewItem = new JMenuItem(PopupActionListener.DESTROY_VIEW);
 		destroyNetworkItem = new JMenuItem(PopupActionListener.DESTROY_NETWORK);
 		applyVisualStyleMenu = new JMenu(PopupActionListener.APPLY_VISUAL_STYLE);
 
 		// action listener which performs the tasks associated with the popup
 		// listener
 		popupActionListener = new PopupActionListener();
 		editNetworkTitle.addActionListener(popupActionListener);
 		createViewItem.addActionListener(popupActionListener);
 		destroyViewItem.addActionListener(popupActionListener);
 		destroyNetworkItem.addActionListener(popupActionListener);
 		applyVisualStyleMenu.addActionListener(popupActionListener);
 		popup.add(editNetworkTitle);
 		popup.add(createViewItem);
 		popup.add(destroyViewItem);
 		popup.add(destroyNetworkItem);
 		popup.addSeparator();
 		popup.add(applyVisualStyleMenu);
 	}
 	
 	
 	private void resetTable() {
 		treeTable.getColumn(ColumnTypes.NETWORK.getDisplayName()).setPreferredWidth(170);
 		treeTable.getColumn(ColumnTypes.NODES.getDisplayName()).setPreferredWidth(45);
 		treeTable.getColumn(ColumnTypes.EDGES.getDisplayName()).setPreferredWidth(45);
 		treeTable.setRowHeight(DEF_ROW_HEIGHT);
 	}
 	
 	
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param comp DOCUMENT ME!
 	 */
 	public void setNavigator(final Component comp) {
 		split.setRightComponent(comp);
 		split.validate();
 	}
 
 	/**
 	 * This is used by Session writer.
 	 *
 	 * @return
 	 */
 	public JTreeTable getTreeTable() {
 		return treeTable;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public JPanel getNavigatorPanel() {
 		return navigatorPanel;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public SwingPropertyChangeSupport getSwingPropertyChangeSupport() {
 		return pcs;
 	}
 
 	/**
 	 * Remove a network from the panel.
 	 *
 	 * @param network_id
 	 */
 	public void removeNetwork(final String network_id) {
 		final NetworkTreeNode node = getNetworkNode(network_id);
 		if (node == null) return;
 		
 		final Enumeration<NetworkTreeNode> children = node.children();
 		NetworkTreeNode child = null;
 		final List removed_children = new ArrayList();
 
 		while (children.hasMoreElements()) {
 			removed_children.add(children.nextElement());
 		}
 
 		for (Iterator i = removed_children.iterator(); i.hasNext();) {
 			child = (NetworkTreeNode) i.next();
 			child.removeFromParent();
 			root.add(child);
 		}
 
 		Cytoscape.getNetwork(network_id).removeSelectEventListener(this);
 		node.removeFromParent();
 		treeTable.getTree().updateUI();
 		treeTable.doLayout();
 	}
 
 	
 	/**
 	 * update a network title
 	 */
 	public void updateTitle(final CyNetwork network) {
 		// updates the title in the network panel 
 		if (treeTable.getTree().getSelectionPath() != null) { // user has selected something
 			treeTableModel.setValueAt(network.getTitle(),
 		                          treeTable.getTree().getSelectionPath().getLastPathComponent(), 0);
 		} else { // no selection, means the title has been changed programmatically
 			NetworkTreeNode node = getNetworkNode(network.getIdentifier());
 			treeTableModel.setValueAt(network.getTitle(), node, 0);
 		}
 		treeTable.getTree().updateUI();
 		treeTable.doLayout();
 		// updates the title in the networkViewMap
 		Cytoscape.getDesktop().getNetworkViewManager().updateNetworkTitle(network);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param event DOCUMENT ME!
 	 */
 	public void onSelectEvent(SelectEvent event) {
 		// TODO: is this method necessary?  Why this class is selecteventlistener?
 	}
 
 	
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param network_id DOCUMENT ME!
 	 * @param parent_id DOCUMENT ME!
 	 */
 	public void addNetwork(String network_id, String parent_id) {
 		// first see if it exists
 		if (getNetworkNode(network_id) == null) {
 			//logger.info("NetworkPanel: addNetwork " + network_id);
 			NetworkTreeNode dmtn = new NetworkTreeNode(Cytoscape.getNetwork(network_id).getTitle(),
 			                                           network_id);
 			Cytoscape.getNetwork(network_id).addSelectEventListener(this);
 
 			if (parent_id != null && getNetworkNode(parent_id) != null) {
 				getNetworkNode(parent_id).add(dmtn);
 			} else {
 				root.add(dmtn);
 			}
 
 			// apparently this doesn't fire valueChanged 
 			treeTable.getTree().collapsePath(new TreePath(new TreeNode[] { root }));
 
 			treeTable.getTree().updateUI();
 			TreePath path = new TreePath(dmtn.getPath());
 			treeTable.getTree().expandPath(path);
 			treeTable.getTree().scrollPathToVisible(path);
 			treeTable.doLayout();
 		
 			// this is necessary because valueChanged is not fired above 
 			focusNetworkNode(network_id);
 		}
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param network_id DOCUMENT ME!
 	 */
 	public void focusNetworkNode(String network_id) {
 		//logger.info("NetworkPanel: focus network node");
 		DefaultMutableTreeNode node = getNetworkNode(network_id);
 
 		if (node != null) {
 			// fires valueChanged if the network isn't already selected
 			treeTable.getTree().getSelectionModel().setSelectionPath(new TreePath(node.getPath()));
 			treeTable.getTree().scrollPathToVisible(new TreePath(node.getPath()));
 		} 
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param network_id DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public NetworkTreeNode getNetworkNode(String network_id) {
 		Enumeration tree_node_enum = root.breadthFirstEnumeration();
 
 		while (tree_node_enum.hasMoreElements()) {
 			NetworkTreeNode node = (NetworkTreeNode) tree_node_enum.nextElement();
 
 			if ((String) node.getNetworkID() == network_id) {
 				return node;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * This method highlights a network in the NetworkPanel. 
 	 *
 	 * @param e DOCUMENT ME!
 	 */
 	public void valueChanged(TreeSelectionEvent e) {
 		// TODO: Every time user select a network name, this method will be called 3 times! 
 		
 		final JTree mtree = treeTable.getTree();
 
 		// sets the "current" network based on last node in the tree selected
 		final NetworkTreeNode node = (NetworkTreeNode) mtree.getLastSelectedPathComponent();
 		if ( node == null || node.getUserObject() == null )
 			return;
 		
 		pcs.firePropertyChange(new PropertyChangeEvent(this, CytoscapeDesktop.NETWORK_VIEW_FOCUS,
 	            null, (String) node.getNetworkID()));
 
 		// creates a list of all selected networks 
 		final List<String> networkList = new LinkedList<String>();
 		try {
 			for ( int i = mtree.getMinSelectionRow(); i <= mtree.getMaxSelectionRow(); i++ ) {
 				NetworkTreeNode n = (NetworkTreeNode) mtree.getPathForRow(i).getLastPathComponent();
 				if ( n != null && n.getUserObject() != null && mtree.isRowSelected(i) )
 					networkList.add( n.getNetworkID() );
 			}
 		} catch (Exception ex) { 
 			CyLogger.getLogger().warn("Exception handling network panel change: "+ex.getMessage());
 			ex.printStackTrace();
 		}
 
 		if ( networkList.size() > 0 ) {
 			Cytoscape.setSelectedNetworks(networkList);
 			Cytoscape.setSelectedNetworkViews(networkList);
 		} 
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param e DOCUMENT ME!
 	 */
 	public void propertyChange(PropertyChangeEvent e) {
 		if (Cytoscape.NETWORK_CREATED.equals(e.getPropertyName())) {
 			addNetwork((String) e.getNewValue(), (String) e.getOldValue());
 		} else if (Cytoscape.NETWORK_DESTROYED.equals(e.getPropertyName())) {
 			removeNetwork((String) e.getNewValue());
 		} else if (CytoscapeDesktop.NETWORK_VIEW_FOCUSED.equals(e.getPropertyName())) {
 			if ( e.getSource() != this )
 				focusNetworkNode((String) e.getNewValue());
 		} else if (Cytoscape.NETWORK_TITLE_MODIFIED.equals(e.getPropertyName())) {
 			CyNetworkTitleChange cyNetworkTitleChange = (CyNetworkTitleChange) e.getNewValue();
 			String newID = cyNetworkTitleChange.getNetworkIdentifier();
 			//String newTitle = cyNetworkTitleChange.getNetworkTitle();
 			CyNetwork _network = Cytoscape.getNetwork(newID);
 			// Network "0" is the default and does not appear in the netowrk panel
 			if (_network != null && !_network.getIdentifier().equals("0")) 
 				updateTitle(_network);				
 		} else if(Cytoscape.CYTOSCAPE_INITIALIZED.equals(e.getPropertyName())) {
 			updateVSMenu();
 		}
 	}
 	
 	
 	/**
 	 * This class listens to mouse events from the TreeTable, if the mouse event
 	 * is one that is canonically associated with a popup menu (ie, a right
 	 * click) it will pop up the menu with option for destroying view, creating
 	 * view, and destroying network (this is platform specific apparently)
 	 */
 	protected class PopupListener extends MouseAdapter {
 		/**
 		 * Don't know why you need both of these, but this is how they did it in
 		 * the example
 		 */
 		public void mousePressed(MouseEvent e) {
 			maybeShowPopup(e);
 		}
 
 		/**
 		 * if the mouse press is of the correct type, this function will maybe
 		 * display the popup
 		 */
 		private void maybeShowPopup(MouseEvent e) {
 			// check for the popup type
 			if (e.isPopupTrigger()) {
 				// get the row where the mouse-click originated
 				final int[] selected = treeTable.getSelectedRows();
 
 				if (selected != null && selected.length != 0) {
 					boolean enableViewRelatedMenu = false;
 					final int selectedItemCount = selected.length;
 					CyNetwork cyNetwork = null;
 					final JTree tree = treeTable.getTree();
 					for (int i = 0; i<selectedItemCount; i++) {
 						
 						final TreePath treePath = tree.getPathForRow(selected[i]);
 						final String networkID = (String) ((NetworkTreeNode) treePath.getLastPathComponent())
 						                   .getNetworkID();
 	
 						cyNetwork = Cytoscape.getNetwork(networkID);
 						if(Cytoscape.viewExists(networkID)) {
 							enableViewRelatedMenu = true;
 						}
 					}
 					
 					// Edit title command will be enabled only when ONE network is selected.
 					if (selectedItemCount == 1) {
 						editNetworkTitle.setEnabled(true);
 						popupActionListener.setActiveNetwork(cyNetwork);
 					} else
 						editNetworkTitle.setEnabled(false);
 					
 					if (enableViewRelatedMenu) {
 						// At least one selected network has a view.
 						createViewItem.setEnabled(true);
 						destroyViewItem.setEnabled(true);
 						applyVisualStyleMenu.setEnabled(true);
 					} else {
 						// None of the selected networks has view.
 						createViewItem.setEnabled(true);
 						destroyViewItem.setEnabled(false);
 						applyVisualStyleMenu.setEnabled(false);
 					}
 
 					popup.show(e.getComponent(), e.getX(), e.getY());
 				}
 			}
 		}
 	}
 
 	public void stateChanged(ChangeEvent e) {		
 		updateVSMenu();
 	}
 	
 	
 	private void updateVSMenu() {
 		applyVisualStyleMenu.removeAll();
 		
 		final Set<String> vsNames = new TreeSet<String>(Cytoscape.getVisualMappingManager().getCalculatorCatalog().getVisualStyleNames());
 		for (String name: vsNames) {
 			final JMenuItem styleMenu = new JMenuItem(name);
 			styleMenu.setAction(new ApplyVisualStyleAction(name));
 			applyVisualStyleMenu.add(styleMenu);
 		}
 	}
 }
 
 
 /**
  * This class listens for actions from the popup menu, it is responsible for
  * performing actions related to destroying and creating views, and destroying
  * the network.
  */
 class PopupActionListener implements ActionListener  {
 	/**
 	 * Constants for JMenuItem labels
 	 */
 	public static final String DESTROY_VIEW = "Destroy View";
 
 	/**
 	 *
 	 */
 	public static final String CREATE_VIEW = "Create View";
 
 	/**
 	 *
 	 */
 	public static final String DESTROY_NETWORK = "Destroy Network";
 
 	/**
 	 *
 	 */
 	public static final String EDIT_TITLE = "Edit Network Title";
 	
 	public static final String APPLY_VISUAL_STYLE = "Apply Visual Style";
 
 	/**
 	 * This is the network which originated the mouse-click event (more
 	 * appropriately, the network associated with the ID associated with the row
 	 * associated with the JTable that originated the popup event
 	 */
 	protected CyNetwork cyNetwork;
 
 	/**
 	 * Based on the action event, destroy or create a view, or destroy a network
 	 */
 	public void actionPerformed(ActionEvent ae) {
 		final String label = ((JMenuItem) ae.getSource()).getText();
 
 		if (DESTROY_VIEW.equals(label)) {
 			final List<CyNetwork> selected = Cytoscape.getSelectedNetworks();	
 			for (final CyNetwork network: selected) {
 				final CyNetworkView targetView = Cytoscape.getNetworkView(network.getIdentifier());
 				if (targetView != Cytoscape.getNullNetworkView()) {
 					Cytoscape.destroyNetworkView(targetView);
 				}
 			}
 		} else if (CREATE_VIEW.equals(label)) {
 			final List<CyNetwork> selected = Cytoscape.getSelectedNetworks();	
 			
 			for(CyNetwork network: selected) {
 				if (!Cytoscape.viewExists(network.getIdentifier()))
 					CreateNetworkViewAction.createViewFromCurrentNetwork(network);
 			}
 		} else if (DESTROY_NETWORK.equals(label)) {
 			final List<CyNetwork> selected = Cytoscape.getSelectedNetworks();	
 			for (CyNetwork network: selected)
 				Cytoscape.destroyNetwork(network);
 		} else if (EDIT_TITLE.equals(label)) {
 			CyNetworkNaming.editNetworkTitle(cyNetwork);
 			Cytoscape.getDesktop().getNetworkPanel().updateTitle(cyNetwork);
 		} else {
 			CyLogger.getLogger().warn("Unexpected network panel popup option");
 		}
 	}
 
 	/**
 	 * Right before the popup menu is displayed, this function is called so we
 	 * know which network the user is clicking on to call for the popup menu
 	 */
 	public void setActiveNetwork(final CyNetwork cyNetwork) {
 		this.cyNetwork = cyNetwork;
 	}
 }
