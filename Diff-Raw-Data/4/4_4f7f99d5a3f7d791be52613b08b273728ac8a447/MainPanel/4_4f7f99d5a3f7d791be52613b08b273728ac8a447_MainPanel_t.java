 // PathVisio,
 // a tool for data visualization and analysis using Biological Pathways
 // Copyright 2006-2007 BiGCaT Bioinformatics
 //
 // Licensed under the Apache License, Version 2.0 (the "License"); 
 // you may not use this file except in compliance with the License. 
 // You may obtain a copy of the License at 
 // 
 // http://www.apache.org/licenses/LICENSE-2.0 
 //  
 // Unless required by applicable law or agreed to in writing, software 
 // distributed under the License is distributed on an "AS IS" BASIS, 
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 // See the License for the specific language governing permissions and 
 // limitations under the License.
 //
 package org.pathvisio.gui.swing;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.Action;
 import javax.swing.ActionMap;
 import javax.swing.ImageIcon;
 import javax.swing.InputMap;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JToolBar;
 import javax.swing.KeyStroke;
 import javax.swing.table.TableCellEditor;
 import javax.swing.table.TableCellRenderer;
 
 import org.pathvisio.ApplicationEvent;
 import org.pathvisio.Engine;
 import org.pathvisio.Engine.ApplicationEventListener;
 import org.pathvisio.data.GexManager;
 import org.pathvisio.gui.BackpageTextProvider;
 import org.pathvisio.gui.swing.CommonActions.ZoomAction;
 import org.pathvisio.gui.swing.dialogs.PathwayElementDialog;
 import org.pathvisio.gui.swing.dnd.PathwayImportHandler;
 import org.pathvisio.gui.swing.propertypanel.PathwayTableModel;
 import org.pathvisio.model.PathwayElement;
 import org.pathvisio.view.Graphics;
 import org.pathvisio.view.Handle;
 import org.pathvisio.view.SelectionBox;
 import org.pathvisio.view.VPathway;
 import org.pathvisio.view.VPathwayElement;
 import org.pathvisio.view.VPathwayEvent;
 import org.pathvisio.view.VPathwayListener;
 
 import com.mammothsoftware.frwk.ddb.DropDownButton;
 
 public class MainPanel extends JPanel implements VPathwayListener, ApplicationEventListener {
 	private static final long serialVersionUID = 1L;
 
 	private JSplitPane splitPane;
 
 	protected JToolBar toolBar;
 
 	private JScrollPane pathwayScrollPane;
 
 	private JScrollPane propertiesScrollPane;
 	
 	protected JTabbedPane sidebarTabbedPane;
 
 	protected JMenuBar menuBar;
 
 	private JTable propertyTable;
 
 	private BackpagePane backpagePane;
 	
 	protected CommonActions actions;
 	
 	Set<Action> hideActions;
 	
 	private boolean mayAddAction(Action a) {
 		return hideActions == null || !hideActions.contains(a);
 	}
 	
 	protected void addMenuActions(JMenuBar mb) {
 		JMenu fileMenu = new JMenu("File");
 		
 		addToMenu(actions.saveAction, fileMenu);
 		addToMenu(actions.saveAsAction, fileMenu);
 		fileMenu.addSeparator();
 		addToMenu(actions.importAction, fileMenu);
 		addToMenu(actions.exportAction, fileMenu);
 		fileMenu.addSeparator();
 		addToMenu(actions.exitAction, fileMenu);
 		
 		JMenu editMenu = new JMenu("Edit");
 		addToMenu(actions.undoAction, editMenu);
 		addToMenu(actions.copyAction, editMenu);
 		addToMenu(actions.pasteAction, editMenu);
 		editMenu.addSeparator();
 		
 		JMenu selectionMenu = new JMenu("Selection");
 		JMenu alignMenu = new JMenu("Align");
 		JMenu stackMenu = new JMenu("Stack");
 		
 		for(Action a : actions.alignActions) addToMenu(a, alignMenu);
 		for(Action a : actions.stackActions) addToMenu(a, stackMenu);
 		
 		selectionMenu.add(alignMenu);
 		selectionMenu.add(stackMenu);
 		editMenu.add (selectionMenu);
 		
 		JMenu viewMenu = new JMenu("View");
 		JMenu zoomMenu = new JMenu("Zoom");
 		viewMenu.add(zoomMenu);
 		for(Action a : actions.zoomActions) addToMenu(a, zoomMenu);
 
 		JMenu helpMenu = new JMenu("Help");
 		
 		mb.add(fileMenu);
 		mb.add(editMenu);
 		mb.add(viewMenu);
 		mb.add(helpMenu);
 	}
 
 	/**
 	 * Constructor for this class. Creates the main panel of this application, containing
 	 * the main GUI elements (menubar, toolbar, sidepanel, drawing pane). Actions that should
 	 * not be added to the menubar and toolbar should be specified in the hideActions parameter
	 * @param hideActions The {@link Action}s that should not be added to the toolbar and menubar
 	 */
 	public MainPanel(Set<Action> hideActions) {
 		this.hideActions = hideActions;
 		
 		setLayout(new BorderLayout());
 		setTransferHandler(new PathwayImportHandler());
 		Engine.getCurrent().addApplicationEventListener(this);
 		
 		actions = SwingEngine.getCurrent().getActions();
 		
 		toolBar = new JToolBar();
 		addToolBarActions(toolBar);
 
 		add(toolBar, BorderLayout.PAGE_START);
 		// menuBar will be added by container (JFrame or JApplet)
 
 		pathwayScrollPane = new JScrollPane();
 
 		final PathwayTableModel model = new PathwayTableModel();
 		propertyTable = new JTable(model) {
 			private static final long serialVersionUID = 1L;
 
 			public TableCellRenderer getCellRenderer(int row, int column) {
 				TableCellRenderer r = model.getCellRenderer(row, column);
 				return r == null ? super.getCellRenderer(row, column) : r;
 			}
 
 			public TableCellEditor getCellEditor(int row, int column) {
 				TableCellEditor e = model.getCellEditor(row, column);
 				return e == null ? super.getCellEditor(row, column) : e;
 			}
 		};
 		//TODO: make this prettier, it's not good for the tablemodel to have
 		//a reference to the table. Quick fix for preventing TableCellEditor
 		//to remain open upon selecting a new PathwayElement
 		model.setTable(propertyTable);
 		
 		propertiesScrollPane = new JScrollPane(propertyTable);
 		
 		backpagePane = new BackpagePane(new BackpageTextProvider (Engine.getCurrent(), SwingEngine.getCurrent().getGdbManager(), GexManager.getCurrent()));
 		
 		sidebarTabbedPane = new JTabbedPane();
 		sidebarTabbedPane.addTab( "Properties", propertiesScrollPane );
 		sidebarTabbedPane.addTab( "Backpage", new JScrollPane(backpagePane) );
 		
 		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 				pathwayScrollPane, sidebarTabbedPane);
 		splitPane.setResizeWeight(1);
 		splitPane.setOneTouchExpandable(true);
 		add(splitPane, BorderLayout.CENTER);
 		
 		Action[] keyStrokeActions = new Action[] {
 				actions.copyAction,
 				actions.pasteAction,
 		};
 		InputMap im = getInputMap();
 		ActionMap am = getActionMap();
 		for(Action a : keyStrokeActions) {
 			im.put((KeyStroke)a.getValue(Action.ACCELERATOR_KEY), a.getValue(Action.NAME));
 			am.put(a.getValue(Action.NAME), a);
 		}
 		
 		menuBar = new JMenuBar();
 		addMenuActions(menuBar);
 	}
 	
 	/**
 	 * Constructor for this class. Creates the main panel of this application, containing
 	 * the main GUI elements (menubar, toolbar, sidepanel, drawing pane).
 	 */
 	public MainPanel() {
 		this(null);
 	}
 	
 	protected void addToolBarActions(JToolBar tb) 
 	{
 		tb.setLayout(new WrapLayout(1, 1));
 		
 		addToToolbar(actions.importAction);
 		addToToolbar(actions.exportAction);
 		tb.addSeparator();
 		addToToolbar(actions.copyAction);
 		addToToolbar(actions.pasteAction);
 		
 		tb.addSeparator();
 
 		addToToolbar(actions.undoAction);
 		
 		tb.addSeparator();
 
 		addToToolbar(new JLabel("Zoom:", JLabel.LEFT));
 		JComboBox combo = new JComboBox(actions.zoomActions);
 		combo.setMaximumSize(combo.getPreferredSize());
 		combo.setEditable(true);
 		combo.setSelectedIndex(5); // 100%
 		combo.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JComboBox combo = (JComboBox) e.getSource();
 				Object s = combo.getSelectedItem();
 				if (s instanceof Action) {
 					((Action) s).actionPerformed(e);
 				} else if (s instanceof String) {
 					String zs = (String) s;
 					try {
 						double zf = Double.parseDouble(zs);
 						ZoomAction za = new ZoomAction(zf);
 						za.setEnabled(true);
 						za.actionPerformed(e);
 					} catch (Exception ex) {
 						// Ignore bad input
 					}
 				}
 			}
 		});
 		addToToolbar(combo, TB_GROUP_SHOW_IF_VPATHWAY);
 
 		tb.addSeparator();
 
 		String submenu = "line";
 		
 		for(Action[] aa : actions.newElementActions) {
 			if(aa.length == 1) {
 				addToToolbar(aa[0]);
 			} else { //This is the line/receptor sub-menu
 				String icon = "newlinemenu.gif";
 				String tooltip = "Select a line to draw"; 
 				
 				if(submenu.equals("receptors")) { //Next one is receptors
 					icon = "newlineshapemenu.gif";
 					tooltip = "Select a receptor/ligand to draw";
 				} else {
 					submenu = "receptors";
 				}
 				DropDownButton lineButton = new DropDownButton(new ImageIcon(Engine.getCurrent()
 						.getResourceURL(icon)));
 				lineButton.setToolTipText(tooltip);
 				for(Action a : aa) {
 					lineButton.addComponent(new JMenuItem(a));
 				}
 				addToToolbar(lineButton, TB_GROUP_SHOW_IF_EDITMODE);
 				lineButton.setEnabled(false);
 			}
 		}
 				
 		tb.addSeparator();
 		
 		addToToolbar(actions.alignActions);
 		addToToolbar(actions.stackActions);
 	}
 
 	public static final String TB_GROUP_SHOW_IF_EDITMODE = "edit";
 	public static final String TB_GROUP_SHOW_IF_VPATHWAY = "vpathway";
 	
 	HashMap<String, List<Component>> toolbarGroups = new HashMap<String, List<Component>>();
 	
 	public void addToToolbar(Component c, String group) {
 		JToolBar tb = getToolBar();
 		tb.add(c);
 		addToToolbarGroup(c, group);
 	}
 		
 	public void addToToolbar(Component c) {
 		addToToolbar(c, null);
 	}
 	
 	public void addToToolbar(Action[] actions) {
 		for(Action a : actions) {
 			addToToolbar(a);
 		}
 	}
 
 	public JButton addToToolbar(Action a, String group) {
 		if(mayAddAction(a)) {
 			JButton b = getToolBar().add(a);
 			addToToolbarGroup(b, group);
 			return b;
 		}
 		return null;
 	}
 	
 	public JButton addToToolbar(Action a) {
 		return addToToolbar(a, null);
 	}
 	
 	private void addToToolbarGroup(Component c, String group) {
 		if(group != null) {
 			List<Component> gb = toolbarGroups.get(group);
 			if(gb == null) {
 				toolbarGroups.put(group, gb = new ArrayList<Component>());
 			}
 			gb.add(c);
 		}
 	}
 	
 	public void addToMenu(Action a, JMenu parent) {
 		if(mayAddAction(a)) {
 			parent.add(a);
 		}
 	}
 	
 	public List<Component> getToolbarGroup(String group) {
 		List<Component> tbg = toolbarGroups.get(group);
 		if(tbg == null) tbg = new ArrayList<Component>();
 		return tbg;
 	}
 	
 	public JToolBar getToolBar() {
 		return toolBar;
 	}
 
 	public JScrollPane getScrollPane() {
 		return pathwayScrollPane;
 	}
 
 	public JSplitPane getSplitPane() {
 		return splitPane;
 	}
 	
 	public BackpagePane getBackpagePane() {
 		return backpagePane;
 	}
 	
 	public void vPathwayEvent(VPathwayEvent e) {
 		VPathway vp = (VPathway)e.getSource();
 		switch(e.getType()) {
 		case VPathwayEvent.ELEMENT_DOUBLE_CLICKED:
 			VPathwayElement pwe = e.getAffectedElement();
 			if(pwe instanceof Handle) {
 				pwe = ((Handle)pwe).getParent();
 			}
 			if(pwe instanceof Graphics && 
 					!(pwe instanceof SelectionBox)) {
 				PathwayElement p = ((Graphics)pwe).getPathwayElement();
 				if(p != null) {
 					PathwayElementDialog.getInstance(p, !vp.isEditMode(), null, this).setVisible(true);
 				}
 			}
 			break;
 		case VPathwayEvent.EDIT_MODE_ON:
 			for(Component b : getToolbarGroup(TB_GROUP_SHOW_IF_EDITMODE)) {
 				b.setEnabled(true);
 			}
 			break;
 		case VPathwayEvent.EDIT_MODE_OFF:
 			for(Component b : getToolbarGroup(TB_GROUP_SHOW_IF_EDITMODE)) {
 				b.setEnabled(false);
 			}
 			break;
 		}
 	}
 	
 	public void applicationEvent(ApplicationEvent e) {
 		switch(e.getType()) {
 		case ApplicationEvent.VPATHWAY_CREATED:
 			VPathway vp = (VPathway)e.getSource();
 			vp.addVPathwayListener(this);
 			vp.addVPathwayListener(new PathwayElementMenuListener());
 			for(Component b : getToolbarGroup(TB_GROUP_SHOW_IF_VPATHWAY)) {
 				b.setEnabled(true);
 			}
 			break;
 		}
 	}
 
 	public JMenuBar getMenuBar() {
 		return menuBar;
 	}
 
 	public JTabbedPane getSideBarTabbedPane()
 	{
 		return sidebarTabbedPane;
 	}
 
 }
