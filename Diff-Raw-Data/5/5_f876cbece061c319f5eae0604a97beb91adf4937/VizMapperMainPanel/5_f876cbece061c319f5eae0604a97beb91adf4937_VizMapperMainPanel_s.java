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
 
 import com.l2fprod.common.propertysheet.DefaultProperty;
 import com.l2fprod.common.propertysheet.Property;
 import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
 import com.l2fprod.common.propertysheet.PropertyRendererRegistry;
 import com.l2fprod.common.propertysheet.PropertySheetPanel;
 import com.l2fprod.common.propertysheet.PropertySheetTable;
 import com.l2fprod.common.propertysheet.PropertySheetTableModel.Item;
 import com.l2fprod.common.swing.plaf.blue.BlueishButtonUI;
 
 import cytoscape.CyEdge;
 import cytoscape.CyNode;
 import cytoscape.Cytoscape;
 
 import cytoscape.data.CyAttributes;
 import cytoscape.data.CyAttributesUtils;
 
 import cytoscape.util.SwingWorker;
 
 import cytoscape.util.swing.DropDownMenuButton;
 
 import cytoscape.view.CyNetworkView;
 import cytoscape.view.CytoscapeDesktop;
 import cytoscape.view.NetworkPanel;
 
 import cytoscape.visual.ArrowShape;
 import cytoscape.visual.CalculatorCatalog;
 import cytoscape.visual.EdgeAppearanceCalculator;
 import cytoscape.visual.LineStyle;
 import cytoscape.visual.NodeAppearanceCalculator;
 import cytoscape.visual.NodeShape;
 import cytoscape.visual.VisualMappingManager;
 import cytoscape.visual.VisualPropertyType;
 import static cytoscape.visual.VisualPropertyType.NODE_FONT_SIZE;
 import static cytoscape.visual.VisualPropertyType.NODE_HEIGHT;
 import static cytoscape.visual.VisualPropertyType.NODE_LABEL_POSITION;
 import static cytoscape.visual.VisualPropertyType.NODE_WIDTH;
 
 import cytoscape.visual.VisualStyle;
 
 import cytoscape.visual.calculators.BasicCalculator;
 import cytoscape.visual.calculators.Calculator;
 
 import cytoscape.visual.mappings.ContinuousMapping;
 import cytoscape.visual.mappings.DiscreteMapping;
 import cytoscape.visual.mappings.ObjectMapping;
 import cytoscape.visual.mappings.PassThroughMapping;
 
 import cytoscape.visual.ui.editors.continuous.ContinuousMappingEditorPanel;
 import cytoscape.visual.ui.editors.discrete.CyColorCellRenderer;
 import cytoscape.visual.ui.editors.discrete.CyColorPropertyEditor;
 import cytoscape.visual.ui.editors.discrete.CyComboBoxPropertyEditor;
 import cytoscape.visual.ui.editors.discrete.CyDoublePropertyEditor;
 import cytoscape.visual.ui.editors.discrete.CyFontPropertyEditor;
 import cytoscape.visual.ui.editors.discrete.CyLabelPositionPropertyEditor;
 import cytoscape.visual.ui.editors.discrete.CyStringPropertyEditor;
 import cytoscape.visual.ui.editors.discrete.FontCellRenderer;
 import cytoscape.visual.ui.editors.discrete.LabelPositionCellRenderer;
 import cytoscape.visual.ui.editors.discrete.ShapeCellRenderer;
 import cytoscape.visual.ui.icon.ArrowIcon;
 import cytoscape.visual.ui.icon.NodeIcon;
 import cytoscape.visual.ui.icon.VisualPropertyIcon;
 
 import ding.view.DGraphView;
 
 import giny.model.GraphObject;
 import giny.model.Node;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyEditor;
 
 import java.lang.reflect.Constructor;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import javax.swing.AbstractAction;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JDialog;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.PopupMenuEvent;
 import javax.swing.event.PopupMenuListener;
 import javax.swing.event.TableColumnModelEvent;
 import javax.swing.event.TableColumnModelListener;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableCellRenderer;
 
 
 /**
  * New VizMapper UI main panel.
  *
  * This panel consists of 3 panels:
  * <ul>
  * <li>Global Control Panel
  * <li>Default editor panel
  * <li>Visual Mapping Browser
  * </ul>
  *
  *
  * @version 0.5
  * @since Cytoscape 2.5
  * @author Keiichiro Ono
  * @param <syncronized>
  */
 public class VizMapperMainPanel extends JPanel implements PropertyChangeListener, PopupMenuListener,
                                                           ChangeListener {
 	private static final Color UNUSED_COLOR = new Color(100, 100, 100, 50);
 	public enum DefaultEditor {
 		NODE,
 		EDGE,
 		GLOBAL;
 	}
 
 	private static JPopupMenu menu;
 	private static JMenuItem delete;
 	private static JMenuItem rainbow1;
 	private static JMenuItem rainbow2;
 	private static JMenuItem randomize;
 	private static JMenuItem series;
 	private static JMenuItem fit;
 	private static JMenuItem editAll;
 	private static JPopupMenu optionMenu;
 	private static JMenuItem newVS;
 	private static JMenuItem renameVS;
 	private static JMenuItem deleteVS;
 	private static JMenuItem duplicateVS;
 	private static JMenuItem createLegend;
 	private static JMenu generateValues;
 	private static JMenu modifyValues;
 	private static JMenuItem brighter;
 	private static JMenuItem darker;
 	private static JCheckBoxMenuItem lockSize;
 
 	/*
 	 * Icons used in this panel.
 	 */
 	private static final ImageIcon optionIcon = new ImageIcon(Cytoscape.class.getResource("/cytoscape/images/ximian/stock_form-properties.png"));
 	private static final ImageIcon delIcon = new ImageIcon(Cytoscape.class.getResource("/cytoscape/images/ximian/stock_delete-16.png"));
 	private static final ImageIcon addIcon = new ImageIcon(Cytoscape.class.getResource("/cytoscape/images/ximian/stock_data-new-table-16.png"));
 	private static final ImageIcon rndIcon = new ImageIcon(Cytoscape.class.getResource("/cytoscape/images/ximian/stock_filters-16.png"));
 	private static final ImageIcon renameIcon = new ImageIcon(Cytoscape.class.getResource("/cytoscape/images/ximian/stock_redo-16.png"));
 	private static final ImageIcon duplicateIcon = new ImageIcon(Cytoscape.class.getResource("/cytoscape/images/ximian/stock_slide-duplicate.png"));
 	private static final ImageIcon legendIcon = new ImageIcon(Cytoscape.class.getResource("/cytoscape/images/ximian/stock_graphic-styles-16.png"));
 	private static final ImageIcon editIcon = new ImageIcon(Cytoscape.class.getResource("/cytoscape/images/ximian/stock_edit-16.png"));
 	private static final String DEFAULT_VS_NAME = "default";
 
 	/*
 	 * This is a singleton.
 	 */
 	private static VizMapperMainPanel panel;
 
 	static {
 		/*
 		 * Make dummy network nodes & edges
 		 */
 		final CyNode source = Cytoscape.getCyNode("Source", true);
 		final CyNode target = Cytoscape.getCyNode("Target", true);
 		final CyEdge edge = Cytoscape.getCyEdge(source, target, "dummyInteraction", "interaction",
 		                                        true, true);
 
 		final CyAttributes nodeAttr = Cytoscape.getNodeAttributes();
 		nodeAttr.setAttribute("Source", "hiddenLabel", "Source");
 		nodeAttr.setAttribute("Target", "hiddenLabel", "Target");
 		nodeAttr.setUserVisible("hiddenLabel", false);
 		nodeAttr.setUserEditable("hiddenLabel", false);
 
 		final CyAttributes edgeAttr = Cytoscape.getEdgeAttributes();
 		edgeAttr.setUserVisible("dummyInteraction", false);
 		edgeAttr.setUserEditable("dummyInteraction", false);
 	}
 
 	/*
 	 * Visual mapping manager. All parameters should be taken from here.
 	 */
 	private VisualMappingManager vmm;
 
 	/*
 	 * Keeps Properties in the browser.
 	 */
 	private Map<String, List<Property>> propertyMap;
 
 	// Keeps current discrete mappings.  NOT PERMANENT
 	private final Map<String, Map<Object, Object>> discMapBuffer = new HashMap<String, Map<Object, Object>>();
 	private String lastVSName = null;
 	private JScrollPane noMapListScrollPane;
 	private List<VisualPropertyType> noMapping;
 	private JPanel buttonPanel;
 	private JButton addButton;
 	private JPanel bottomPanel;
 	private Map<VisualPropertyType, JDialog> editorWindowManager = new HashMap<VisualPropertyType, JDialog>();
 	private Map<String, Image> defaultImageManager = new HashMap<String, Image>();
 
 	
 	private boolean ignore = false;
 	
 	// For node size lock
 	VizMapperProperty nodeSize;
 	VizMapperProperty nodeWidth;
 	VizMapperProperty nodeHeight;
 
 	/** Creates new form AttributeOrientedPanel */
 	private VizMapperMainPanel() {
 		vmm = Cytoscape.getVisualMappingManager();
 		vmm.addChangeListener(this);
 
 		propertyMap = new HashMap<String, List<Property>>();
 		setMenu();
 
 		// Need to register listener here, instead of CytoscapeDesktop.
 		Cytoscape.getSwingPropertyChangeSupport().addPropertyChangeListener(this);
 
 		initComponents();
 		registerCellEditorListeners();
 	}
 
 	/*
 	 * Register listeners for editors.
 	 */
 	private void registerCellEditorListeners() {
 		nodeAttrEditor.addPropertyChangeListener(this);
 		edgeAttrEditor.addPropertyChangeListener(this);
 
 		mappingTypeEditor.addPropertyChangeListener(this);
 
 		colorCellEditor.addPropertyChangeListener(this);
 		fontCellEditor.addPropertyChangeListener(this);
 		numberCellEditor.addPropertyChangeListener(this);
 		shapeCellEditor.addPropertyChangeListener(this);
 		stringCellEditor.addPropertyChangeListener(this);
 		lineCellEditor.addPropertyChangeListener(this);
 		arrowCellEditor.addPropertyChangeListener(this);
 
 		labelPositionEditor.addPropertyChangeListener(this);
 	}
 
 	/**
 	 * Get an instance of VizMapper UI panel. This is a singleton.
 	 *
 	 * @return
 	 */
 	public static VizMapperMainPanel getVizMapperUI() {
 		if (panel == null)
 			panel = new VizMapperMainPanel();
 
 		return panel;
 	}
 
 	/**
 	 * Will be used to show/hide node size props.
 	 *
 	 * @param isLock
 	 */
 	private void switchNodeSizeLock(boolean isLock) {
 		final Property[] props = visualPropertySheetPanel.getProperties();
 
 		if (isLock && (nodeSize != null)) {
 			// Case 1: Locked. Need to remove width/height props.
 			boolean isNodeSizeExist = false;
 
 			for (Property prop : props) {
 				if (prop.getDisplayName().equals(VisualPropertyType.NODE_SIZE.getName()))
 					isNodeSizeExist = true;
 
 				if (prop.getDisplayName().equals(VisualPropertyType.NODE_HEIGHT.getName())) {
 					nodeHeight = (VizMapperProperty) prop;
 					visualPropertySheetPanel.removeProperty(prop);
 				} else if (prop.getDisplayName().equals(VisualPropertyType.NODE_WIDTH.getName())) {
 					nodeWidth = (VizMapperProperty) prop;
 					visualPropertySheetPanel.removeProperty(prop);
 				}
 			}
 
 			if (isNodeSizeExist == false)
 				visualPropertySheetPanel.addProperty(nodeSize);
 		} else {
 			// Case 2: Unlocked. Need to add W/H.
 			boolean isNodeWExist = false;
 			boolean isNodeHExist = false;
 
 			for (Property prop : props) {
 				if (prop.getDisplayName().equals(VisualPropertyType.NODE_SIZE.getName())) {
 					nodeSize = (VizMapperProperty) prop;
 					visualPropertySheetPanel.removeProperty(prop);
 				}
 
 				if (prop.getDisplayName().equals(VisualPropertyType.NODE_WIDTH.getName()))
 					isNodeWExist = true;
 
 				if (prop.getDisplayName().equals(VisualPropertyType.NODE_HEIGHT.getName()))
 					isNodeHExist = true;
 			}
 
 			if (isNodeHExist == false)
 				visualPropertySheetPanel.addProperty(nodeHeight);
 
 			if (isNodeWExist == false)
 				visualPropertySheetPanel.addProperty(nodeWidth);
 		}
 
 		visualPropertySheetPanel.repaint();
 
 		final String targetName = vmm.getVisualStyle().getName();
 		final String focus = vmm.getNetwork().getIdentifier();
 
 		createDefaultImage(targetName,
 		                   (DGraphView) ((DefaultViewPanel) DefaultAppearenceBuilder.getDefaultView(targetName))
 		                   .getView(), defaultAppearencePanel.getSize());
 		setDefaultPanel(defaultImageManager.get(targetName));
 	}
 
 	/**
 	 * Setup menu items.<br>
 	 *
 	 * This includes both icon menu and right-click menu.
 	 *
 	 */
 	private void setMenu() {
 		/*
 		 * Option Menu
 		 */
 		newVS = new JMenuItem("Create new Visual Style...");
 		newVS.setIcon(addIcon);
 		newVS.addActionListener(new NewStyleListener());
 
 		deleteVS = new JMenuItem("Delete Visual Style...");
 		deleteVS.setIcon(delIcon);
 		deleteVS.addActionListener(new RemoveStyleListener());
 
 		renameVS = new JMenuItem("Rename Visual Style...");
 		renameVS.setIcon(renameIcon);
 		renameVS.addActionListener(new RenameStyleListener());
 
 		duplicateVS = new JMenuItem("Copy existing Visual Style...");
 		duplicateVS.setIcon(duplicateIcon);
 		duplicateVS.addActionListener(new CopyStyleListener());
 
 		createLegend = new JMenuItem("Create legend from current Visual Style");
 		createLegend.setIcon(legendIcon);
 		createLegend.addActionListener(new CreateLegendListener());
 		optionMenu = new JPopupMenu();
 		optionMenu.add(newVS);
 		optionMenu.add(deleteVS);
 		optionMenu.add(renameVS);
 		optionMenu.add(duplicateVS);
 		optionMenu.add(createLegend);
 
 		/*
 		 * Build right-click menu
 		 */
 		generateValues = new JMenu("Generate Discrete Values");
 		generateValues.setIcon(rndIcon);
 		modifyValues = new JMenu("Modify Discrete Values");
 
 		lockSize = new JCheckBoxMenuItem("Lock Node Width/Height");
 		lockSize.setSelected(true);
 		lockSize.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					if (lockSize.isSelected()) {
 						vmm.getVisualStyle().getNodeAppearanceCalculator().setNodeSizeLocked(true);
 						switchNodeSizeLock(true);
 					} else {
 						vmm.getVisualStyle().getNodeAppearanceCalculator().setNodeSizeLocked(false);
 						switchNodeSizeLock(false);
 					}
 
 					Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
 				}
 			});
 
 		delete = new JMenuItem("Delete mapping");
 
 		final Font italicMenu = new Font("SansSerif", Font.ITALIC, 14);
 		rainbow1 = new JMenuItem("Rainbow 1");
 		rainbow2 = new JMenuItem("Rainbow 2 (w/modulations)");
 		randomize = new JMenuItem("Randomize");
 		rainbow1.setFont(italicMenu);
 		rainbow2.setFont(italicMenu);
 
 		series = new JMenuItem("Series (Number Only)");
 		fit = new JMenuItem("Fit Node Width to Label");
 
 		brighter = new JMenuItem("Brighter");
 		darker = new JMenuItem("Darker");
 
 		editAll = new JMenuItem("Edit selected values at once...");
 
 		delete.setIcon(delIcon);
 		editAll.setIcon(editIcon);
 
 		rainbow1.addActionListener(new GenerateValueListener(GenerateValueListener.RAINBOW1));
 		rainbow2.addActionListener(new GenerateValueListener(GenerateValueListener.RAINBOW2));
 		randomize.addActionListener(new GenerateValueListener(GenerateValueListener.RANDOM));
 
 		series.addActionListener(new GenerateSeriesListener());
 		fit.addActionListener(new FitLabelListener());
 
 		brighter.addActionListener(new BrightnessListener(BrightnessListener.BRIGHTER));
 		darker.addActionListener(new BrightnessListener(BrightnessListener.DARKER));
 
 		delete.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					removeMapping();
 				}
 			});
 		editAll.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 					editSelectedCells();
 				}
 			});
 		// add.addActionListener(l)
 		// select.setIcon(vmIcon);
 		menu = new JPopupMenu();
 		generateValues.add(rainbow1);
 		generateValues.add(rainbow2);
 		generateValues.add(randomize);
 		generateValues.add(series);
 		generateValues.add(fit);
 
 		modifyValues.add(brighter);
 		modifyValues.add(darker);
 
 		rainbow1.setEnabled(false);
 		rainbow2.setEnabled(false);
 		randomize.setEnabled(false);
 		series.setEnabled(false);
 		fit.setEnabled(false);
 
 		brighter.setEnabled(false);
 		darker.setEnabled(false);
 
 		menu.add(delete);
 		menu.add(new JSeparator());
 		menu.add(generateValues);
 		menu.add(modifyValues);
 		menu.add(editAll);
 		menu.add(new JSeparator());
 		menu.add(lockSize);
 
 		delete.setEnabled(false);
 		menu.addPopupMenuListener(this);
 	}
 
 	public static void apply(Object newValue, VisualPropertyType type) {
 		if (newValue != null)
 			type.setDefault(Cytoscape.getVisualMappingManager().getVisualStyle(), newValue);
 	}
 
 	public static Object showValueSelectDialog(VisualPropertyType type, Component caller)
 	    throws Exception {
 		return type.showDiscreteEditor();
 	}
 
 	/**
 	 * GUI initialization code based on the auto-generated code from NetBeans
 	 *
 	 */
 	private void initComponents() {
 		mainSplitPane = new javax.swing.JSplitPane();
 		listSplitPane = new javax.swing.JSplitPane();
 
 		bottomPanel = new javax.swing.JPanel();
 
 		defaultAppearencePanel = new javax.swing.JPanel();
 		visualPropertySheetPanel = new PropertySheetPanel();
 		visualPropertySheetPanel.setTable(new PropertySheetTable());
 
 		vsSelectPanel = new javax.swing.JPanel();
 		vsNameComboBox = new javax.swing.JComboBox();
 
 		buttonPanel = new javax.swing.JPanel();
 
 		GridBagLayout gridbag = new GridBagLayout();
 		GridBagConstraints constraints = new GridBagConstraints();
 		buttonPanel.setLayout(gridbag);
 		constraints.gridx = 0;
 		constraints.gridy = 0;
 		constraints.gridwidth = 1;
 		constraints.gridheight = GridBagConstraints.REMAINDER;
 
 		addButton = new javax.swing.JButton();
 
 		addButton.setUI(new BlueishButtonUI());
 
 		gridbag.setConstraints(addButton, constraints);
 		buttonPanel.add(addButton);
 
 		constraints.gridx = 2;
 		constraints.gridy = 0;
 
 		defaultAppearencePanel.setMinimumSize(new Dimension(100, 100));
 		defaultAppearencePanel.setPreferredSize(new Dimension(mainSplitPane.getWidth(),
 		                                                      this.mainSplitPane.getDividerLocation()));
 		defaultAppearencePanel.setSize(defaultAppearencePanel.getPreferredSize());
 		// defaultAppearencePanel.addMouseListener(new DefaultMouseListener());
 		mainSplitPane.setDividerLocation(120);
 		mainSplitPane.setDividerSize(4);
 		listSplitPane.setDividerLocation(400);
 		listSplitPane.setDividerSize(5);
 		listSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
 
 		noMapListScrollPane = new javax.swing.JScrollPane();
 		noMapListScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
 		                                                                           "Unused Visual Properties",
 		                                                                           javax.swing.border.TitledBorder.CENTER,
 		                                                                           javax.swing.border.TitledBorder.DEFAULT_POSITION,
 		                                                                           new java.awt.Font("SansSerif",
 		                                                                                             1,
 		                                                                                             12)));
 		noMapListScrollPane.setToolTipText("To Create New Mapping, Drag & Drop List Item to Browser.");
 
 		org.jdesktop.layout.GroupLayout bottomPanelLayout = new org.jdesktop.layout.GroupLayout(bottomPanel);
 		bottomPanel.setLayout(bottomPanelLayout);
 		bottomPanelLayout.setHorizontalGroup(bottomPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                      .add(noMapListScrollPane,
 		                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                           272, Short.MAX_VALUE)
 		                                                      .add(buttonPanel,
 		                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                           Short.MAX_VALUE));
 		bottomPanelLayout.setVerticalGroup(bottomPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                    .add(bottomPanelLayout.createSequentialGroup()
 		                                                                          .add(buttonPanel,
 		                                                                               org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                               25,
 		                                                                               org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                          .add(noMapListScrollPane,
 		                                                                               org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                               135,
 		                                                                               Short.MAX_VALUE)));
 
 		listSplitPane.setLeftComponent(mainSplitPane);
 		listSplitPane.setRightComponent(bottomPanel);
 
 		mainSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
 		defaultAppearencePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
 		                                                                              "Defaults",
 		                                                                              javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
 		                                                                              javax.swing.border.TitledBorder.DEFAULT_POSITION,
 		                                                                              new java.awt.Font("SansSerif",
 		                                                                                                1,
 		                                                                                                12),
 		                                                                              java.awt.Color.darkGray));
 		// defaultTabbedPane
 		// .setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
 		// defaultTabbedPane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
 		mainSplitPane.setLeftComponent(defaultAppearencePanel);
 
 		visualPropertySheetPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
 		                                                                                "Visual Mapping Browser",
 		                                                                                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
 		                                                                                javax.swing.border.TitledBorder.DEFAULT_POSITION,
 		                                                                                new java.awt.Font("SansSerif",
 		                                                                                                  1,
 		                                                                                                  12),
 		                                                                                java.awt.Color.darkGray));
 
 		mainSplitPane.setRightComponent(visualPropertySheetPanel);
 
 		vsSelectPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
 		                                                                     "Current Visual Style",
 		                                                                     javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
 		                                                                     javax.swing.border.TitledBorder.DEFAULT_POSITION,
 		                                                                     new java.awt.Font("SansSerif",
 		                                                                                       1, 12),
 		                                                                     java.awt.Color.darkGray));
 
 		vsNameComboBox.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					vsNameComboBoxActionPerformed(evt);
 				}
 			});
 
 		optionButton = new DropDownMenuButton(new AbstractAction() {
 				public void actionPerformed(ActionEvent ae) {
 					DropDownMenuButton b = (DropDownMenuButton) ae.getSource();
 					optionMenu.show(b, 0, b.getHeight());
 				}
 			});
 
 		optionButton.setToolTipText("Options...");
 		optionButton.setIcon(optionIcon);
 		optionButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
 		optionButton.setComponentPopupMenu(optionMenu);
 
 		org.jdesktop.layout.GroupLayout vsSelectPanelLayout = new org.jdesktop.layout.GroupLayout(vsSelectPanel);
 		vsSelectPanel.setLayout(vsSelectPanelLayout);
 		vsSelectPanelLayout.setHorizontalGroup(vsSelectPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                          .add(vsSelectPanelLayout.createSequentialGroup()
 		                                                                                  .addContainerGap()
 		                                                                                  .add(vsNameComboBox,
 		                                                                                       0,
 		                                                                                       146,
 		                                                                                       Short.MAX_VALUE)
 		                                                                                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                  .add(optionButton,
 		                                                                                       org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                       64,
 		                                                                                       org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                                  .addContainerGap()));
 		vsSelectPanelLayout.setVerticalGroup(vsSelectPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                        .add(vsSelectPanelLayout.createSequentialGroup()
 		                                                                                .add(vsSelectPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 		                                                                                                        .add(vsNameComboBox,
 		                                                                                                             org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                             org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                             org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                                                        .add(optionButton)) // .addContainerGap(
 		                                                                                                                            // org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                                            // Short.MAX_VALUE)
 		));
 
 		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
 		this.setLayout(layout);
 		layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                .add(vsSelectPanel,
 		                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                     Short.MAX_VALUE)
 		                                .add(mainSplitPane,
 		                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 280,
 		                                     Short.MAX_VALUE));
 		layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                              .add(layout.createSequentialGroup()
 		                                         .add(vsSelectPanel,
 		                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                         .add(mainSplitPane,
 		                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                              510, Short.MAX_VALUE)));
 	} // </editor-fold>
 
 	// Variables declaration - do not modify
 	private JPanel defaultAppearencePanel;
 	private javax.swing.JSplitPane mainSplitPane;
 	private javax.swing.JSplitPane listSplitPane;
 	private DropDownMenuButton optionButton;
 	private PropertySheetPanel visualPropertySheetPanel;
 	private javax.swing.JComboBox vsNameComboBox;
 	private javax.swing.JPanel vsSelectPanel;
 
 	/*
 	 * Renderer and Editors for the cells
 	 */
 
 	// For general values (string & number)
 	private DefaultTableCellRenderer defCellRenderer = new DefaultTableCellRenderer();
 
 	// For String values
 	private CyStringPropertyEditor stringCellEditor = new CyStringPropertyEditor();
 
 	// For colors
 	private CyColorCellRenderer collorCellRenderer = new CyColorCellRenderer();
 	private CyColorPropertyEditor colorCellEditor = new CyColorPropertyEditor();
 
 	// For shapes
 	private ShapeCellRenderer shapeCellRenderer = new ShapeCellRenderer(VisualPropertyType.NODE_SHAPE);
 	private CyComboBoxPropertyEditor shapeCellEditor = new CyComboBoxPropertyEditor();
 
 	// For Lines
 	private ShapeCellRenderer lineCellRenderer = new ShapeCellRenderer(VisualPropertyType.EDGE_LINE_STYLE);
 	private CyComboBoxPropertyEditor lineCellEditor = new CyComboBoxPropertyEditor();
 
 	// For Arrow shapes
 	private CyComboBoxPropertyEditor arrowCellEditor = new CyComboBoxPropertyEditor();
 	private ShapeCellRenderer arrowShapeCellRenderer = new ShapeCellRenderer(VisualPropertyType.EDGE_TGTARROW_SHAPE);
 
 	// For sizes
 	private CyDoublePropertyEditor numberCellEditor = new CyDoublePropertyEditor();
 
 	// For font faces
 	private CyFontPropertyEditor fontCellEditor = new CyFontPropertyEditor();
 	private FontCellRenderer fontCellRenderer = new FontCellRenderer();
 
 	// For label positions
 	private LabelPositionCellRenderer labelPositionRenderer = new LabelPositionCellRenderer();
 	private CyLabelPositionPropertyEditor labelPositionEditor = new CyLabelPositionPropertyEditor();
 
 	// Others
 	private DefaultTableCellRenderer emptyBoxRenderer = new DefaultTableCellRenderer();
 	private DefaultTableCellRenderer filledBoxRenderer = new DefaultTableCellRenderer();
 	private DefaultTableCellRenderer continuousRenderer = new DefaultTableCellRenderer();
 	private DefaultTableCellRenderer discreteRenderer = new DefaultTableCellRenderer();
 
 	/*
 	 * Controlling attr selector
 	 */
 	private CyComboBoxPropertyEditor nodeAttrEditor = new CyComboBoxPropertyEditor();
 	private CyComboBoxPropertyEditor edgeAttrEditor = new CyComboBoxPropertyEditor();
 	private CyComboBoxPropertyEditor nodeNumericalAttrEditor = new CyComboBoxPropertyEditor();
 	private CyComboBoxPropertyEditor edgeNumericalAttrEditor = new CyComboBoxPropertyEditor();
 
 	// For mapping types.
 	private CyComboBoxPropertyEditor mappingTypeEditor = new CyComboBoxPropertyEditor();
 	private static final Map<Object, Icon> nodeShapeIcons = NodeShape.getIconSet();
 	private static final Map<Object, Icon> arrowShapeIcons = ArrowShape.getIconSet();
 	private static final Map<Object, Icon> lineTypeIcons = LineStyle.getIconSet();
 	private PropertyRendererRegistry rendReg = new PropertyRendererRegistry();
 	private PropertyEditorRegistry editorReg = new PropertyEditorRegistry();
 
 	private PropertyEditor getCellEditor(VisualPropertyType type) {
 		Class dataType = type.getDataType();
 
 		if (dataType == Number.class) {
 			return numberCellEditor;
 		}
 
 		return null;
 	}
 
 	// End of variables declaration
 	private void vsNameComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
 		final String vsName = (String) vsNameComboBox.getSelectedItem();
 
 		if (vsName != null) {
 			if (Cytoscape.getCurrentNetworkView().equals(Cytoscape.getNullNetworkView())) {
 				switchVS(vsName, false);
 			} else {
 				switchVS(vsName, true);
 			}
 		}
 	}
 
 	private void switchVS(String vsName) {
 		switchVS(vsName, true);
 	}
 
 	private void switchVS(String vsName, boolean redraw) {
 		
 		if(ignore)
 			return;
 		// If new VS name is the same, ignore.
 		if (lastVSName == vsName) {
 			return;
 		}
 		
 		//System.out.println("VS Switched --> " + vsName + ", Last = " + lastVSName);
 
 		vmm.setNetworkView(Cytoscape.getCurrentNetworkView());
 		vmm.setVisualStyle(vsName);
 
 		if (propertyMap.containsKey(vsName) && (vsName.equals(lastVSName) == false)) {
 			final List<Property> props = propertyMap.get(vsName);
 			final Map<String, Property> unused = new TreeMap<String, Property>();
 
 			/*
 			 * Remove currently shown property
 			 */
 			for (Property item : visualPropertySheetPanel.getProperties())
 				visualPropertySheetPanel.removeProperty(item);
 
 			/*
 			 * Add properties to current property sheet.
 			 */
 			for (Property prop : props) {
 				// System.out.println("======== renderer: " +
 				// editorReg.getEditor(prop));
 				if (prop.getCategory().startsWith(CATEGORY_UNUSED) == false) {
 					if (prop.getCategory().equals(NODE_VISUAL_MAPPING)) {
 						visualPropertySheetPanel.addProperty(0, prop);
 					} else {
 						visualPropertySheetPanel.addProperty(prop);
 					}
 				} else {
 					unused.put(prop.getDisplayName(), prop);
 				}
 			}
 
 			final List<String> keys = new ArrayList<String>(unused.keySet());
 			Collections.sort(keys);
 
 			for (Object key : keys) {
 				visualPropertySheetPanel.addProperty(unused.get(key));
 			}
 		} else {
 			setPropertyTable();
 		}
 
 		lastVSName = vsName;
 
 		Cytoscape.getCurrentNetworkView().setVisualStyle(vsName);
 
 		if (redraw) {
 			Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
 		}
 
 		/*
 		 * Draw default view
 		 */
 		Image defImg = defaultImageManager.get(vsName);
 
 		if (defImg == null) {
 			createDefaultImage(vsName,
 			                   (DGraphView) ((DefaultViewPanel) DefaultAppearenceBuilder
 			                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      .getDefaultView(vsName))
 			                   .getView(), defaultAppearencePanel.getSize());
 			defImg = defaultImageManager.get(vsName);
 		}
 
 		setDefaultPanel(defImg);
 
 		final boolean lockState = vmm.getVisualStyle().getNodeAppearanceCalculator()
 		                             .getNodeSizeLocked();
 		lockSize.setSelected(lockState);
 
 		switchNodeSizeLock(lockState);
 
 		Cytoscape.getDesktop().repaint();
 		vsNameComboBox.setSelectedItem(vsName);
 	}
 
 	private static final String CATEGORY_UNUSED = "Unused Properties";
 	private static final String GRAPHICAL_MAP_VIEW = "Graphical View";
 	private static final String NODE_VISUAL_MAPPING = "Node Visual Mapping";
 	private static final String EDGE_VISUAL_MAPPING = "Edge Visual Mapping";
 	private JButton addMappingButton;
 
 	/*
 	 * Set Visual Style selector combo box.
 	 */
 	private void setVSSelector() {
 		List<String> vsNames = new ArrayList<String>(vmm.getCalculatorCatalog().getVisualStyleNames());
 
 		vsNameComboBox.removeAllItems();
 
 		JPanel defPanel;
 
 		final Dimension panelSize = defaultAppearencePanel.getSize();
 		DGraphView view;
 
 		CyNetworkView oldView = vmm.getNetworkView();
 
 		Collections.sort(vsNames);
 		for (String name : vsNames) {
 			vsNameComboBox.addItem(name);
 
 			defPanel = DefaultAppearenceBuilder.getDefaultView(name);
 			view = (DGraphView) ((DefaultViewPanel) defPanel).getView();
 
 			if (view != null) {
 				System.out.println("Creating Default Image for " + name);
 				createDefaultImage(name, view, panelSize);
 			}
 		}
 		
 		// vmm.setNetworkView(oldView);
 		vmm.setNetworkView(Cytoscape.getCurrentNetworkView());
 
 		// Cytoscape.destroyNetwork(dummyNet);
 	}
 
 	/**
 	 * Create image of a default dummy network and save in a Map object.
 	 *
 	 * @param vsName
 	 * @param view
 	 * @param size
 	 */
 	private void createDefaultImage(String vsName, DGraphView view, Dimension size) {
 		defaultAppearencePanel.setLayout(new BorderLayout());
 
 		final Image image = view.createImage((int) size.getWidth(), (int) size.getHeight(), 0.9);
 
 		defaultImageManager.put(vsName, image);
 	}
 
 	private void setPropertySheetAppearence() {
 		/*
 		 * Set Tooltiptext for the table.
 		 */
 		visualPropertySheetPanel.setTable(new PropertySheetTable() {
 				public String getToolTipText(MouseEvent me) {
 					final Point pt = me.getPoint();
 					final int row = rowAtPoint(pt);
 
 					if (row < 0)
 						return null;
 					else {
 						final Property prop = ((Item) getValueAt(row, 0)).getProperty();
 
 						final Color fontColor;
 
 						if ((prop != null) && (prop.getValue() != null)
 						    && (prop.getValue().getClass() == Color.class))
 							fontColor = (Color) prop.getValue();
 						else
 							fontColor = Color.DARK_GRAY;
 
 						final String colorString = Integer.toHexString(fontColor.getRGB());
 
 						/*
 						 * Edit
 						 */
 						if (prop == null)
 							return null;
 
 						if (prop.getDisplayName().equals(GRAPHICAL_MAP_VIEW))
 							return "Click to edit this mapping...";
 
 						if ((prop.getDisplayName() == "Controlling Attribute")
 						    || (prop.getDisplayName() == "Mapping Type"))
 							return "<html><Body BgColor=\"white\"><font Size=\"4\" Color=\"#"
 							       + colorString.substring(2, 8) + "\"><strong>"
 							       + prop.getDisplayName() + " = " + prop.getValue()
 							       + "</font></strong></body></html>";
 						else if ((prop.getSubProperties() == null)
 						         || (prop.getSubProperties().length == 0))
 							return "<html><Body BgColor=\"white\"><font Size=\"4\" Color=\"#"
 							       + colorString.substring(2, 8) + "\"><strong>"
 							       + prop.getDisplayName() + "</font></strong></body></html>";
 
 						return null;
 					}
 				}
 			});
 
 		visualPropertySheetPanel.getTable().getColumnModel().addColumnModelListener(new TableColumnModelListener() {
 				public void columnAdded(TableColumnModelEvent arg0) {
 					// TODO Auto-generated method stub
 				}
 
 				public void columnMarginChanged(ChangeEvent e) {
 					updateTableView();
 				}
 
 				public void columnMoved(TableColumnModelEvent e) {
 					// TODO Auto-generated method stub
 				}
 
 				public void columnRemoved(TableColumnModelEvent e) {
 					// TODO Auto-generated method stub
 				}
 
 				public void columnSelectionChanged(ListSelectionEvent e) {
 					// TODO Auto-generated method stub
 				}
 			});
 
 		/*
 		 * By default, show category.
 		 */
 		visualPropertySheetPanel.setMode(PropertySheetPanel.VIEW_AS_CATEGORIES);
 
 		visualPropertySheetPanel.getTable().setComponentPopupMenu(menu);
 
 		visualPropertySheetPanel.getTable().addMouseListener(new MouseAdapter() {
 				public void mouseClicked(MouseEvent e) {
 					processMouseClick(e);
 				}
 			});
 
 		PropertySheetTable table = visualPropertySheetPanel.getTable();
 		table.setRowHeight(25);
 		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 		table.setCategoryBackground(new Color(10, 10, 50, 20));
 		table.setCategoryForeground(Color.black);
 		table.setSelectionBackground(Color.white);
 		table.setSelectionForeground(Color.blue);
 
 		/*
 		 * Set editors
 		 */
 		collorCellRenderer.setForeground(Color.DARK_GRAY);
 		collorCellRenderer.setOddBackgroundColor(new Color(150, 150, 150, 20));
 		collorCellRenderer.setEvenBackgroundColor(Color.white);
 
 		emptyBoxRenderer.setHorizontalTextPosition(SwingConstants.CENTER);
 		emptyBoxRenderer.setHorizontalAlignment(SwingConstants.CENTER);
 		emptyBoxRenderer.setBackground(new Color(0, 200, 255, 20));
 		emptyBoxRenderer.setForeground(Color.red);
 		emptyBoxRenderer.setFont(new Font("SansSerif", Font.BOLD, 12));
 
 		filledBoxRenderer.setBackground(Color.white);
 		filledBoxRenderer.setForeground(Color.blue);
 
 		setAttrComboBox();
 
 		final Set mappingTypes = Cytoscape.getVisualMappingManager().getCalculatorCatalog()
 		                                  .getMappingNames();
 
 		mappingTypeEditor.setAvailableValues(mappingTypes.toArray());
 
 		VisualPropertyIcon newIcon;
 
 		List<Icon> iconList = new ArrayList<Icon>();
 		final List<NodeShape> nodeShapes = new ArrayList<NodeShape>();
 
 		for (Object key : nodeShapeIcons.keySet()) {
 			NodeShape shape = (NodeShape) key;
 
 			if (shape.isSupported()) {
 				iconList.add(nodeShapeIcons.get(key));
 				nodeShapes.add(shape);
 			}
 		}
 
 		Icon[] iconArray = new Icon[iconList.size()];
 		String[] shapeNames = new String[iconList.size()];
 
 		for (int i = 0; i < iconArray.length; i++) {
 			newIcon = ((NodeIcon) iconList.get(i)).clone();
 			newIcon.setIconHeight(16);
 			newIcon.setIconWidth(16);
 			iconArray[i] = newIcon;
 			shapeNames[i] = nodeShapes.get(i).getShapeName();
 		}
 
 		shapeCellEditor.setAvailableValues(nodeShapes.toArray());
 		shapeCellEditor.setAvailableIcons(iconArray);
 
 		iconList.clear();
 		iconList.addAll(arrowShapeIcons.values());
 		iconArray = new Icon[iconList.size()];
 
 		String[] arrowNames = new String[iconList.size()];
 		Set arrowShapes = arrowShapeIcons.keySet();
 
 		for (int i = 0; i < iconArray.length; i++) {
 			newIcon = ((ArrowIcon) iconList.get(i));
 			newIcon.setIconHeight(16);
 			newIcon.setIconWidth(40);
 			newIcon.setBottomPadding(-9);
 			iconArray[i] = newIcon;
 			arrowNames[i] = newIcon.getName();
 		}
 
 		arrowCellEditor.setAvailableValues(arrowShapes.toArray());
 		arrowCellEditor.setAvailableIcons(iconArray);
 
 		iconList = new ArrayList();
 		iconList.addAll(lineTypeIcons.values());
 		iconArray = new Icon[iconList.size()];
 		shapeNames = new String[iconList.size()];
 
 		Set lineTypes = lineTypeIcons.keySet();
 
 		for (int i = 0; i < iconArray.length; i++) {
 			newIcon = (VisualPropertyIcon) (iconList.get(i));
 			newIcon.setIconHeight(16);
 			newIcon.setIconWidth(16);
 			iconArray[i] = newIcon;
 			shapeNames[i] = newIcon.getName();
 		}
 
 		lineCellEditor.setAvailableValues(lineTypes.toArray());
 		lineCellEditor.setAvailableIcons(iconArray);
 	}
 
 	private void updateTableView() {
 		final PropertySheetTable table = visualPropertySheetPanel.getTable();
 		Property shownProp = null;
 		final DefaultTableCellRenderer empRenderer = new DefaultTableCellRenderer();
 
 		// Number of rows shown now.
 		int rowCount = table.getRowCount();
 
 		for (int i = 0; i < rowCount; i++) {
 			shownProp = ((Item) table.getValueAt(i, 0)).getProperty();
 
 			if ((shownProp != null) && (shownProp.getParentProperty() != null)
 			    && shownProp.getParentProperty().getDisplayName()
 			                .equals(NODE_LABEL_POSITION.getName())) {
 				// This is label position cell. Need laeger cell.
 				table.setRowHeight(i, 50);
 			} else if ((shownProp != null) && shownProp.getDisplayName().equals(GRAPHICAL_MAP_VIEW)) {
 				// This is a Continuous Icon cell.
 				final Property parent = shownProp.getParentProperty();
 				final Object type = ((VizMapperProperty) parent).getHiddenObject();
 
 				if (type instanceof VisualPropertyType) {
 					ObjectMapping mapping;
 
 					if (((VisualPropertyType) type).isNodeProp())
 						mapping = vmm.getVisualStyle().getNodeAppearanceCalculator()
 						             .getCalculator(((VisualPropertyType) type)).getMapping(0);
 					else
 						mapping = vmm.getVisualStyle().getEdgeAppearanceCalculator()
 						             .getCalculator(((VisualPropertyType) type)).getMapping(0);
 
 					if (mapping instanceof ContinuousMapping) {
 						table.setRowHeight(i, 80);
 
 						int wi = table.getCellRect(0, 1, true).width;
 						final ImageIcon icon = ContinuousMappingEditorPanel.getIcon(wi, 70,
 						                                                            (VisualPropertyType) type);
 						final Class dataType = ((VisualPropertyType) type).getDataType();
 
 						if (dataType == Color.class) {
 							final DefaultTableCellRenderer gradientRenderer = new DefaultTableCellRenderer();
 							gradientRenderer.setIcon(icon);
 							rendReg.registerRenderer(shownProp, gradientRenderer);
 						} else if (dataType == Number.class) {
 							final DefaultTableCellRenderer cRenderer = new DefaultTableCellRenderer();
 							// continuousRenderer.setIcon(icon);
 							cRenderer.setIcon(icon);
 							rendReg.registerRenderer(shownProp, cRenderer);
 						} else {
 							final DefaultTableCellRenderer dRenderer = new DefaultTableCellRenderer();
 							// discreteRenderer.setIcon(icon);
 							dRenderer.setIcon(icon);
 							rendReg.registerRenderer(shownProp, dRenderer);
 						}
 					}
 				}
 			} else if ((shownProp != null) && (shownProp.getCategory() != null)
 			           && shownProp.getCategory().equals(CATEGORY_UNUSED)) {
 				empRenderer.setForeground(UNUSED_COLOR);
 				rendReg.registerRenderer(shownProp, empRenderer);
 			}
 		}
 
 		repaint();
 		visualPropertySheetPanel.repaint();
 	}
 
 	private void setAttrComboBox() {
 		final List<String> names = new ArrayList<String>();
 		CyAttributes attr = Cytoscape.getNodeAttributes();
 		String[] nameArray = attr.getAttributeNames();
 		Arrays.sort(nameArray);
 		names.add("ID");
 
 		for (String name : nameArray) {
 			if (attr.getUserVisible(name) && (attr.getType(name) != CyAttributes.TYPE_UNDEFINED)
 			    && (attr.getType(name) != CyAttributes.TYPE_COMPLEX)) {
 				names.add(name);
 			}
 		}
 
 		nodeAttrEditor.setAvailableValues(names.toArray());
 
 		names.clear();
 
 		Class dataClass;
 
 		for (String name : nameArray) {
 			dataClass = CyAttributesUtils.getClass(name, attr);
 
 			if ((dataClass == Integer.class) || (dataClass == Double.class)
 			    || (dataClass == Float.class))
 				names.add(name);
 		}
 
 		nodeNumericalAttrEditor.setAvailableValues(names.toArray());
 
 		names.clear();
 		attr = Cytoscape.getEdgeAttributes();
 		nameArray = attr.getAttributeNames();
 		Arrays.sort(nameArray);
 		names.add("ID");
 
 		for (String name : nameArray) {
 			if (attr.getUserVisible(name) && (attr.getType(name) != CyAttributes.TYPE_UNDEFINED)
 			    && (attr.getType(name) != CyAttributes.TYPE_COMPLEX)) {
 				names.add(name);
 			}
 		}
 
 		edgeAttrEditor.setAvailableValues(names.toArray());
 
 		names.clear();
 
 		for (String name : nameArray) {
 			dataClass = CyAttributesUtils.getClass(name, attr);
 
 			if ((dataClass == Integer.class) || (dataClass == Double.class)
 			    || (dataClass == Float.class))
 				names.add(name);
 		}
 
 		edgeNumericalAttrEditor.setAvailableValues(names.toArray());
 	}
 
 	private void processMouseClick(MouseEvent e) {
 		int selected = visualPropertySheetPanel.getTable().getSelectedRow();
 		/*
 		 * Adjust height if it's an legend icon.
 		 */
 		updateTableView();
 
 		if (SwingUtilities.isLeftMouseButton(e) && (0 <= selected)) {
 			final Item item = (Item) visualPropertySheetPanel.getTable().getValueAt(selected, 0);
 			final Property curProp = item.getProperty();
 
 			if (curProp == null)
 				return;
 
 			/*
 			 * Create new mapping if double-click on unused val.
 			 */
 			String category = curProp.getCategory();
 
 			if ((e.getClickCount() == 2) && (category != null)
 			    && category.equalsIgnoreCase("Unused Properties")) {
 				((VizMapperProperty) curProp).setEditable(true);
 
 				VisualPropertyType type = (VisualPropertyType) ((VizMapperProperty) curProp)
 				                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    .getHiddenObject();
 				visualPropertySheetPanel.removeProperty(curProp);
 
 				final VizMapperProperty newProp = new VizMapperProperty();
 				final VizMapperProperty mapProp = new VizMapperProperty();
 
 				newProp.setDisplayName(type.getName());
 				newProp.setHiddenObject(type);
 				newProp.setValue("Please select an attribute!");
 
 				if (type.isNodeProp()) {
 					newProp.setCategory(NODE_VISUAL_MAPPING);
 					editorReg.registerEditor(newProp, nodeAttrEditor);
 				} else {
 					newProp.setCategory(EDGE_VISUAL_MAPPING);
 					editorReg.registerEditor(newProp, edgeAttrEditor);
 				}
 
 				mapProp.setDisplayName("Mapping Type");
 				mapProp.setValue("Please select a mapping type!");
 				editorReg.registerEditor(mapProp, mappingTypeEditor);
 
 				newProp.addSubProperty(mapProp);
 				mapProp.setParentProperty(newProp);
 				visualPropertySheetPanel.addProperty(0, newProp);
 
 				expandLastSelectedItem(type.getName());
 
 				visualPropertySheetPanel.getTable().scrollRectToVisible(new Rectangle(0, 0, 10, 10));
 				visualPropertySheetPanel.repaint();
 
 				return;
 			} else if ((e.getClickCount() == 1) && (category == null)) {
 				/*
 				 * Single left-click
 				 */
 				VisualPropertyType type = null;
 
 				if ((curProp.getParentProperty() == null)
 				    && ((VizMapperProperty) curProp).getHiddenObject() instanceof VisualPropertyType)
 					type = (VisualPropertyType) ((VizMapperProperty) curProp).getHiddenObject();
 				else if (curProp.getParentProperty() != null)
 					type = (VisualPropertyType) ((VizMapperProperty) curProp.getParentProperty())
 					                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           .getHiddenObject();
 				else
 
 					return;
 
 				final ObjectMapping selectedMapping;
 				Calculator calc = null;
 
 				if (type.isNodeProp()) {
 					calc = vmm.getVisualStyle().getNodeAppearanceCalculator().getCalculator(type);
 				} else {
 					calc = vmm.getVisualStyle().getEdgeAppearanceCalculator().getCalculator(type);
 				}
 
 				if (calc == null) {
 					return;
 				}
 
 				selectedMapping = calc.getMapping(0);
 
 				if (selectedMapping instanceof ContinuousMapping) {
 					/*
 					 * Need to check other windows.
 					 */
 					if (editorWindowManager.containsKey(type)) {
 						// This means editor is already on display.
 						editorWindowManager.get(type).requestFocus();
 
 						return;
 					} else {
 						try {
 							((JDialog) type.showContinuousEditor()).addPropertyChangeListener(this);
 						} catch (Exception e1) {
 							e1.printStackTrace();
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/*
 	 * Set property sheet panel.
 	 *
 	 * TODO: need to find missing editor problem!
 	 */
 	private void setPropertyTable() {
 		setPropertySheetAppearence();
 
 		/*
 		 * Clean up sheet
 		 */
 		for (Property item : visualPropertySheetPanel.getProperties())
 			visualPropertySheetPanel.removeProperty(item);
 
 		final NodeAppearanceCalculator nac = Cytoscape.getVisualMappingManager().getVisualStyle()
 		                                              .getNodeAppearanceCalculator();
 
 		final EdgeAppearanceCalculator eac = Cytoscape.getVisualMappingManager().getVisualStyle()
 		                                              .getEdgeAppearanceCalculator();
 
 		final List<Calculator> nacList = nac.getCalculators();
 		final List<Calculator> eacList = eac.getCalculators();
 
 		editorReg.registerDefaults();
 
 		/*
 		 * Add properties to the browser.
 		 */
 		List<Property> propRecord = new ArrayList<Property>();
 
 		setPropertyFromCalculator(nacList, NODE_VISUAL_MAPPING, propRecord);
 		setPropertyFromCalculator(eacList, EDGE_VISUAL_MAPPING, propRecord);
 
 		// Save it for later use.
 		propertyMap.put(vmm.getVisualStyle().getName(), propRecord);
 
 		/*
 		 * Finally, build unused list
 		 */
 		setUnused(propRecord);
 	}
 
 	/*
 	 * Add unused visual properties to the property sheet
 	 *
 	 */
 	private void setUnused(List<Property> propList) {
 		buildList();
 		Collections.sort(noMapping);
 
 		for (VisualPropertyType type : noMapping) {
 			VizMapperProperty prop = new VizMapperProperty();
 			prop.setCategory(CATEGORY_UNUSED);
 			prop.setDisplayName(type.getName());
 			prop.setHiddenObject(type);
 			prop.setValue("Double-Click to create...");
 			// prop.setEditable(false);
 			visualPropertySheetPanel.addProperty(prop);
 			propList.add(prop);
 		}
 	}
 
 	/*
 	 * Set value, title, and renderer for each property in the category.
 	 */
 	private final void setDiscreteProps(VisualPropertyType type, Map discMapping,
 	                                    Set<Object> attrKeys, PropertyEditor editor,
 	                                    TableCellRenderer rend, DefaultProperty parent) {
 		if (attrKeys == null)
 			return;
 
 		Object val = null;
 		VizMapperProperty valProp;
 		String strVal;
 
 		final List<VizMapperProperty> children = new ArrayList<VizMapperProperty>();
 
 		for (Object key : attrKeys) {
 			valProp = new VizMapperProperty();
 			strVal = key.toString();
 			valProp.setDisplayName(strVal);
 			valProp.setName(strVal + "-" + type.toString());
 			valProp.setParentProperty(parent);
 
 			try {
 				val = discMapping.get(key);
 			} catch (Exception e) {
 				System.out.println("------- Map = " + discMapping.getClass() + ", class = "
 				                   + key.getClass() + ", err = " + e.getMessage());
 				System.out.println("------- Key = " + key + ", val = " + val + ", disp = " + strVal);
 			}
 
 			if (val != null)
 				valProp.setType(val.getClass());
 
 			children.add(valProp);
 			rendReg.registerRenderer(valProp, rend);
 			editorReg.registerEditor(valProp, editor);
 
 			valProp.setValue(val);
 		}
 
 		// Add all children.
 		parent.addSubProperties(children);
 	}
 
 	/*
 	 * Build one property for one visual property.
 	 */
 	private final void buildProperty(Calculator calc, VizMapperProperty calculatorTypeProp,
 	                                 String rootCategory) {
 		final VisualPropertyType type = calc.getVisualPropertyType();
 		/*
 		 * Set one calculator
 		 */
 		calculatorTypeProp.setCategory(rootCategory);
 		// calculatorTypeProp.setType(String.class);
 		calculatorTypeProp.setDisplayName(type.getName());
 		calculatorTypeProp.setHiddenObject(type);
 
 		/*
 		 * Mapping 0 is always currently used mapping.
 		 */
 		final ObjectMapping firstMap = calc.getMapping(0);
 		String attrName;
 
 		if (firstMap != null) {
 			final VizMapperProperty mappingHeader = new VizMapperProperty();
 
 			attrName = firstMap.getControllingAttributeName();
 
 			if (attrName == null) {
 				calculatorTypeProp.setValue("Please select a value!");
 				rendReg.registerRenderer(calculatorTypeProp, emptyBoxRenderer);
 			} else {
 				calculatorTypeProp.setValue(attrName);
 				rendReg.registerRenderer(calculatorTypeProp, filledBoxRenderer);
 			}
 
 			mappingHeader.setDisplayName("Mapping Type");
 			mappingHeader.setHiddenObject(firstMap.getClass());
 
 			if (firstMap.getClass() == DiscreteMapping.class)
 				mappingHeader.setValue("Discrete Mapping");
 			else if (firstMap.getClass() == ContinuousMapping.class)
 				mappingHeader.setValue("Continuous Mapping");
 			else
 				mappingHeader.setValue("Passthrough Mapping");
 
 			mappingHeader.setHiddenObject(firstMap);
 
 			mappingHeader.setParentProperty(calculatorTypeProp);
 			calculatorTypeProp.addSubProperty(mappingHeader);
 			editorReg.registerEditor(mappingHeader, mappingTypeEditor);
 
 			final CyAttributes attr;
 			final Iterator it;
 			final int nodeOrEdge;
 
 			if (calc.getVisualPropertyType().isNodeProp()) {
 				attr = Cytoscape.getNodeAttributes();
 				it = Cytoscape.getCurrentNetwork().nodesIterator();
 				editorReg.registerEditor(calculatorTypeProp, nodeAttrEditor);
 				nodeOrEdge = ObjectMapping.NODE_MAPPING;
 			} else {
 				attr = Cytoscape.getEdgeAttributes();
 				it = Cytoscape.getCurrentNetwork().edgesIterator();
 				editorReg.registerEditor(calculatorTypeProp, edgeAttrEditor);
 				nodeOrEdge = ObjectMapping.EDGE_MAPPING;
 			}
 
 			/*
 			 * Discrete Mapping
 			 */
 			if ((firstMap.getClass() == DiscreteMapping.class) && (attrName != null)) {
 				final Map discMapping = ((DiscreteMapping) firstMap).getAll();
 				final Set<Object> attrSet = loadKeys(attrName, attr, firstMap, nodeOrEdge);
 
 				switch (type) {
 					/*
 					 * Color calculators
 					 */
 					case NODE_FILL_COLOR:
 					case NODE_BORDER_COLOR:
 					case EDGE_COLOR:
 					case EDGE_SRCARROW_COLOR:
 					case EDGE_TGTARROW_COLOR:
 					case NODE_LABEL_COLOR:
 					case EDGE_LABEL_COLOR:
 						setDiscreteProps(type, discMapping, attrSet, colorCellEditor,
 						                 collorCellRenderer, calculatorTypeProp);
 
 						break;
 
 					case NODE_LINE_STYLE:
 					case EDGE_LINE_STYLE:
 						setDiscreteProps(type, discMapping, attrSet, lineCellEditor,
 						                 lineCellRenderer, calculatorTypeProp);
 
 						break;
 
 					/*
 					 * Shape property
 					 */
 					case NODE_SHAPE:
 						setDiscreteProps(type, discMapping, attrSet, shapeCellEditor,
 						                 shapeCellRenderer, calculatorTypeProp);
 
 						break;
 
 					/*
 					 * Arrow Head Shapes
 					 */
 					case EDGE_SRCARROW_SHAPE:
 					case EDGE_TGTARROW_SHAPE:
 						setDiscreteProps(type, discMapping, attrSet, arrowCellEditor,
 						                 arrowShapeCellRenderer, calculatorTypeProp);
 
 						break;
 
 					case NODE_LABEL:
 					case EDGE_LABEL:
 					case NODE_TOOLTIP:
 					case EDGE_TOOLTIP:
 						setDiscreteProps(type, discMapping, attrSet, stringCellEditor,
 						                 defCellRenderer, calculatorTypeProp);
 
 						break;
 
 					/*
 					 * Font props
 					 */
 					case NODE_FONT_FACE:
 					case EDGE_FONT_FACE:
 						setDiscreteProps(type, discMapping, attrSet, fontCellEditor,
 						                 fontCellRenderer, calculatorTypeProp);
 
 						break;
 
 					/*
 					 * Size-related props
 					 */
 					case NODE_FONT_SIZE:
 					case EDGE_FONT_SIZE:
 					case NODE_SIZE:
 					case NODE_WIDTH:
 					case NODE_HEIGHT:
 					case NODE_LINE_WIDTH:
 					case EDGE_LINE_WIDTH:
 					case NODE_OPACITY:
 					case EDGE_OPACITY:
 					case NODE_LABEL_OPACITY:
 					case EDGE_LABEL_OPACITY:
 					case NODE_BORDER_OPACITY:
 						setDiscreteProps(type, discMapping, attrSet, numberCellEditor,
 						                 defCellRenderer, calculatorTypeProp);
 
 						break;
 
 					/*
 					 * Node Label Position. Needs special editor
 					 */
 					case NODE_LABEL_POSITION:
 						setDiscreteProps(type, discMapping, attrSet, labelPositionEditor,
 						                 labelPositionRenderer, calculatorTypeProp);
 
 						break;
 
 					default:
 						break;
 				}
 			} else if ((firstMap.getClass() == ContinuousMapping.class) && (attrName != null)) {
 				int wi = this.visualPropertySheetPanel.getTable().getCellRect(0, 1, true).width;
 
 				VizMapperProperty graphicalView = new VizMapperProperty();
 				graphicalView.setDisplayName(GRAPHICAL_MAP_VIEW);
 				graphicalView.setName(type.getName());
 				graphicalView.setParentProperty(calculatorTypeProp);
 				calculatorTypeProp.addSubProperty(graphicalView);
 
 				final Class dataType = type.getDataType();
 				final ImageIcon icon = ContinuousMappingEditorPanel.getIcon(wi, 70,
 				                                                            (VisualPropertyType) type);
 
 				if (dataType == Color.class) {
 					/*
 					 * Color-related calcs.
 					 */
 					final DefaultTableCellRenderer gradientRenderer = new DefaultTableCellRenderer();
 					gradientRenderer.setIcon(icon);
 
 					rendReg.registerRenderer(graphicalView, gradientRenderer);
 				} else if (dataType == Number.class) {
 					/*
 					 * Size/Width related calcs.
 					 */
 					continuousRenderer.setIcon(icon);
 					rendReg.registerRenderer(graphicalView, continuousRenderer);
 				} else {
 					discreteRenderer.setIcon(icon);
 					rendReg.registerRenderer(graphicalView, discreteRenderer);
 				}
 			} else if ((firstMap.getClass() == PassThroughMapping.class) && (attrName != null)) {
 				/*
 				 * Passthrough
 				 */
 				String id;
 				String value;
 				VizMapperProperty oneProperty;
 
 				/*
 				 * Accept String only.
 				 */
 				if (attr.getType(attrName) == CyAttributes.TYPE_STRING) {
 					while (it.hasNext()) {
 						id = ((GraphObject) it.next()).getIdentifier();
 
 						value = attr.getStringAttribute(id, attrName);
 						oneProperty = new VizMapperProperty();
 
 						if (attrName.equals("ID"))
 							oneProperty.setValue(id);
 						else
 							oneProperty.setValue(value);
 
 						// This prop. should not be editable!
 						oneProperty.setEditable(false);
 
 						oneProperty.setParentProperty(calculatorTypeProp);
 						oneProperty.setDisplayName(id);
 						oneProperty.setType(String.class);
 
 						calculatorTypeProp.addSubProperty(oneProperty);
 					}
 				}
 			}
 		}
 
 		visualPropertySheetPanel.addProperty(0, calculatorTypeProp);
 		visualPropertySheetPanel.setRendererFactory(rendReg);
 		visualPropertySheetPanel.setEditorFactory(editorReg);
 	}
 
 	private void setPropertyFromCalculator(List<Calculator> calcList, String rootCategory,
 	                                       List<Property> propRecord) {
 		VisualPropertyType type = null;
 
 		for (Calculator calc : calcList) {
 			final VizMapperProperty calculatorTypeProp = new VizMapperProperty();
 			buildProperty(calc, calculatorTypeProp, rootCategory);
 
 			PropertyEditor editor = editorReg.getEditor(calculatorTypeProp);
 
 			if ((editor == null)
 			    && (calculatorTypeProp.getCategory().equals("Unused Properties") == false)) {
 				type = (VisualPropertyType) calculatorTypeProp.getHiddenObject();
 
 				if (type.isNodeProp()) {
 					editorReg.registerEditor(calculatorTypeProp, nodeAttrEditor);
 				} else {
 					editorReg.registerEditor(calculatorTypeProp, edgeAttrEditor);
 				}
 			}
 
 			propRecord.add(calculatorTypeProp);
 		}
 	}
 
 	private Set<Object> loadKeys(final String attrName, final CyAttributes attrs,
 	                             final ObjectMapping mapping, final int nOre) {
 		if (attrName.equals("ID")) {
 			return loadID(nOre);
 		}
 
 		Map mapAttrs;
 		mapAttrs = CyAttributesUtils.getAttribute(attrName, attrs);
 
 		if ((mapAttrs == null) || (mapAttrs.size() == 0))
 			return null;
 
 		List acceptedClasses = Arrays.asList(mapping.getAcceptedDataClasses());
 		Class mapAttrClass = CyAttributesUtils.getClass(attrName, attrs);
 
 		if ((mapAttrClass == null) || !(acceptedClasses.contains(mapAttrClass)))
 			return null;
 
 		return loadKeySet(mapAttrs);
 	}
 
 	/**
 	 * Loads the Key Set.
 	 */
 	private Set<Object> loadKeySet(final Map mapAttrs) {
 		final Set<Object> mappedKeys = new TreeSet<Object>();
 
 		final Iterator keyIter = mapAttrs.values().iterator();
 
 		Object o = null;
 
 		while (keyIter.hasNext()) {
 			o = keyIter.next();
 
 			if (o instanceof List) {
 				List list = (List) o;
 
 				for (int i = 0; i < list.size(); i++) {
 					Object vo = list.get(i);
 
 					if (!mappedKeys.contains(vo))
 						mappedKeys.add(vo);
 				}
 			} else {
 				if (!mappedKeys.contains(o))
 					mappedKeys.add(o);
 			}
 		}
 
 		return mappedKeys;
 	}
 
 	private void setDefaultPanel(final Image defImage) {
 		if (defImage == null) {
 			return;
 		}
 
 		defaultAppearencePanel.removeAll();
 
 		final JButton defaultImageButton = new JButton();
 		defaultImageButton.setUI(new BlueishButtonUI());
 		defaultImageButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 
 		defaultImageButton.setIcon(new ImageIcon(defImage));
 		// defaultImageButton.setBackground(bgColor);
 		defaultAppearencePanel.add(defaultImageButton, BorderLayout.CENTER);
 		defaultImageButton.addMouseListener(new DefaultMouseListener());
 	}
 
 	class DefaultMouseListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent e) {
 			if (javax.swing.SwingUtilities.isLeftMouseButton(e)) {
 				final String targetName = vmm.getVisualStyle().getName();
 				final String focus = vmm.getNetwork().getIdentifier();
 
 				final DefaultViewPanel panel = (DefaultViewPanel) DefaultAppearenceBuilder
 				                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    .showDialog(Cytoscape
 				                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                .getDesktop());
 				createDefaultImage(targetName, (DGraphView) panel.getView(),
 				                   defaultAppearencePanel.getSize());
 				setDefaultPanel(defaultImageManager.get(targetName));
 
 				vmm.setNetworkView(Cytoscape.getCurrentNetworkView());
 				vmm.setVisualStyle(targetName);
 				Cytoscape.getDesktop().setFocus(focus);
 				Cytoscape.getDesktop().repaint();
 			}
 		}
 	}
 
 	/**
 	 * On/Off listeners.
 	 * This is for performance.
 	 *
 	 * @param on
 	 *            DOCUMENT ME!
 	 */
 	public void enableListeners(boolean on) {
 		if (on) {
 			//System.out.println("=========Truning ON listeners!!!!!!!!!=============");
 			//Cytoscape.getSwingPropertyChangeSupport().addPropertyChangeListener(this);
 			Cytoscape.getVisualMappingManager().addChangeListener(this);
 			syncStyleBox();
 			ignore = false;
 		} else {
 			//System.out.println("=========REMOVING listeners!!!!!!!!!=============");
 			//Cytoscape.getSwingPropertyChangeSupport().removePropertyChangeListener(this);
 			Cytoscape.getVisualMappingManager().removeChangeListener(this);
 		}
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 */
 	public void initializeTableState() {
 		propertyMap = new HashMap<String, List<Property>>();
 		editorWindowManager = new HashMap<VisualPropertyType, JDialog>();
 		defaultImageManager = new HashMap<String, Image>();
 	}
 
 	/**
 	 * Handle propeaty change events.
 	 *
 	 * @param e
 	 *            DOCUMENT ME!
 	 */
 	public void propertyChange(PropertyChangeEvent e) {
 		
 		// Set ignore flag.
 		if(e.getPropertyName().equals(Integer.toString(Cytoscape.SESSION_OPENED))) {
 			ignore = true;;
 			enableListeners(false);
 		}
 //		} else if (e.getPropertyName().equals(Cytoscape.SESSION_LOADED) || e.getPropertyName().equals(Cytoscape.CYTOSCAPE_INITIALIZED)) {
 //			ignore = false;
 //		}	
 		
 		if(ignore) return;
 //		
 		/*
 		 * Managing editor windows.
 		 */
 		if (e.getPropertyName() == ContinuousMappingEditorPanel.EDITOR_WINDOW_OPENED) {
 			this.editorWindowManager.put((VisualPropertyType) e.getNewValue(),
 			                             (JDialog) e.getSource());
 
 			return;
 		} else if (e.getPropertyName() == ContinuousMappingEditorPanel.EDITOR_WINDOW_CLOSED) {
 			final VisualPropertyType type = (VisualPropertyType) e.getNewValue();
 			this.editorWindowManager.remove(type);
 
 			/*
 			 * Update icon
 			 */
 			final Property[] props = visualPropertySheetPanel.getProperties();
 			VizMapperProperty vprop = null;
 
 			for (Property prop : props) {
 				vprop = (VizMapperProperty) prop;
 
 				if ((vprop.getHiddenObject() != null) && (type == vprop.getHiddenObject())) {
 					vprop = (VizMapperProperty) prop;
 
 					break;
 				}
 			}
 
 			final Property[] subProps = vprop.getSubProperties();
 			vprop = null;
 
 			String name = null;
 
 			for (Property prop : subProps) {
 				name = prop.getName();
 
 				if ((name != null) && name.equals(type.getName())) {
 					vprop = (VizMapperProperty) prop;
 
 					break;
 				}
 			}
 
 			final int width = visualPropertySheetPanel.getTable().getCellRect(0, 1, true).width;
 
 			final DefaultTableCellRenderer cRenderer = new DefaultTableCellRenderer();
 			cRenderer.setIcon(ContinuousMappingEditorPanel.getIcon(width, 70, type));
 
 			rendReg.registerRenderer(vprop, cRenderer);
 			visualPropertySheetPanel.getTable().repaint();
 
 			return;
 		}
 
 		/*
 		 * Got global siginal
 		 */
 
 		//System.out.println("==================GLOBAL Signal: " + e.getPropertyName() + ", SRC = " + e.getSource().toString());
 		if (e.getPropertyName().equals(Cytoscape.CYTOSCAPE_INITIALIZED)) {
 			String vmName = vmm.getVisualStyle().getName();
 
 			if (vsNameComboBox.getItemCount() == 0) {
 				setVSSelector();
 			}
 
 			setDefaultPanel(defaultImageManager.get(vmName));
 
 			vsNameComboBox.setSelectedItem(vmName);
 			vmm.setVisualStyle(vmName);
 
 			return;
 		} else if (e.getPropertyName().equals(Cytoscape.SESSION_LOADED)
 		           || e.getPropertyName().equals(Cytoscape.VIZMAP_LOADED)) {
 			final String vsName = vmm.getVisualStyle().getName();
 			this.lastVSName = null;
 			setVSSelector();
 			vsNameComboBox.setSelectedItem(vsName);
 			vmm.setVisualStyle(vsName);
 
 			return;
 		} else if (e.getPropertyName().equals(CytoscapeDesktop.NETWORK_VIEW_FOCUS)
 		           && (e.getSource().getClass() == NetworkPanel.class)) {
 			final VisualStyle vs = vmm.getNetworkView().getVisualStyle();
 
 			if (vs != null) {
 				vmm.setNetworkView(Cytoscape.getCurrentNetworkView());
 
 				if (vs.getName().equals(vsNameComboBox.getSelectedItem())) {
 					// Do nothing here. Redaraw will be handled by Desktop.
 					// Cytoscape.getCurrentNetworkView().redrawGraph(false,
 					// true);
 				} else {
 					switchVS(vs.getName(), false);
 
 					vsNameComboBox.setSelectedItem(vs.getName());
 					setDefaultPanel(this.defaultImageManager.get(vs.getName()));
 				}
 			}
 
 			return;
 		} else if (e.getPropertyName().equals(Cytoscape.ATTRIBUTES_CHANGED)
 		           || e.getPropertyName().equals(Cytoscape.NETWORK_LOADED)) {
 			System.out.println("Updating attr: Event = " + e.getPropertyName() + ", Source = "
 			                   + e.getSource());
 
 			setAttrComboBox();
 		}
 
 		/***********************************************************************
 		 * Below this line, accept only cell editor events.
 		 **********************************************************************/
 		if (e.getPropertyName().equalsIgnoreCase("value") == false)
 			return;
 
 		if (e.getNewValue() == e.getOldValue())
 			return;
 
 		final PropertySheetTable table = visualPropertySheetPanel.getTable();
 		final int selected = table.getSelectedRow();
 
 		/*
 		 * Do nothing if not selected.
 		 */
 		if (selected < 0)
 			return;
 
 		Item selectedItem = (Item) visualPropertySheetPanel.getTable().getValueAt(selected, 0);
 		VizMapperProperty prop = (VizMapperProperty) selectedItem.getProperty();
 
 		final VisualPropertyType type;
 		String ctrAttrName = null;
 
 		VizMapperProperty typeRootProp;
 
 		if ((prop.getParentProperty() == null) && e.getNewValue() instanceof String) {
 			/*
 			 * This is a controlling attr name.
 			 */
 			typeRootProp = (VizMapperProperty) prop;
 			type = (VisualPropertyType) ((VizMapperProperty) prop).getHiddenObject();
 			ctrAttrName = (String) e.getNewValue();
 		} else if ((prop.getParentProperty() == null) && (e.getNewValue() == null)) {
 			/*
 			 * Empty cell celected. no need to change anything.
 			 */
 			return;
 		} else {
 			typeRootProp = (VizMapperProperty) prop.getParentProperty();
 
 			if (prop.getParentProperty() == null) {
 				return;
 			}
 
 			type = (VisualPropertyType) ((VizMapperProperty) prop.getParentProperty())
 			                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  .getHiddenObject();
 		}
 
 		/*
 		 * Mapping type changed
 		 */
 		if (prop.getHiddenObject() instanceof ObjectMapping
 		    || prop.getDisplayName().equals("Mapping Type")) {
 			System.out.println("Mapping type changed: " + prop.getHiddenObject());
 
 			if (e.getNewValue() == null)
 				return;
 
 			/*
 			 * If invalid data type, ignore.
 			 */
 			final Object parentValue = prop.getParentProperty().getValue();
 
 			if (parentValue != null) {
 				ctrAttrName = parentValue.toString();
 
 				final Class dataClass;
 
 				if (type.isNodeProp()) {
 					dataClass = CyAttributesUtils.getClass(ctrAttrName,
 					                                       Cytoscape.getNodeAttributes());
 				} else {
 					dataClass = CyAttributesUtils.getClass(ctrAttrName,
 					                                       Cytoscape.getEdgeAttributes());
 				}
 
 				if (e.getNewValue().equals("Continuous Mapper")
 				    && ((dataClass != Integer.class) && (dataClass != Double.class)
 				       && (dataClass != Float.class))) {
 					JOptionPane.showMessageDialog(this,
 					                              "Continuous Mapper can be used with Numbers only.",
 					                              "Incompatible Mapping Type!",
 					                              JOptionPane.INFORMATION_MESSAGE);
 
 					return;
 				}
 			} else {
 				return;
 			}
 
 			System.out.println("Mapping new val = " + e.getNewValue());
 
 			if (e.getNewValue().toString().endsWith("Mapper") == false) {
 				return;
 			}
 
 			switchMapping(prop, e.getNewValue().toString(), prop.getParentProperty().getValue());
 
 			/*
 			 * restore expanded props.
 			 */
 			expandLastSelectedItem(type.getName());
 			updateTableView();
 
 			return;
 		}
 
 		/*
 		 * Extract calculator
 		 */
 		ObjectMapping mapping;
 		final Calculator curCalc;
 
 		if (type.isNodeProp()) {
 			curCalc = vmm.getVisualStyle().getNodeAppearanceCalculator().getCalculator(type);
 		} else {
 			curCalc = vmm.getVisualStyle().getEdgeAppearanceCalculator().getCalculator(type);
 		}
 
 		if (curCalc == null) {
 			return;
 		}
 
 		mapping = curCalc.getMapping(0);
 
 		/*
 		 * Controlling Attribute has been changed.
 		 */
 		if (ctrAttrName != null) {
 			/*
 			 * Ignore if not compatible.
 			 */
 			final CyAttributes attrForTest;
 
 			if (type.isNodeProp()) {
 				attrForTest = Cytoscape.getNodeAttributes();
 			} else {
 				attrForTest = Cytoscape.getEdgeAttributes();
 			}
 
 			final Byte dataType = attrForTest.getType(ctrAttrName);
 
 			// This part is for Continuous Mapping.
 			if (mapping instanceof ContinuousMapping) {
 				if ((dataType == CyAttributes.TYPE_FLOATING)
 				    || (dataType == CyAttributes.TYPE_INTEGER)) {
 					// Do nothing
 				} else {
 					JOptionPane.showMessageDialog(this,
 					                              "Continuous Mapper can be used with Numbers only.\nPlease select numerical attributes.",
 					                              "Incompatible Mapping Type!",
 					                              JOptionPane.INFORMATION_MESSAGE);
 
 					return;
 				}
 			}
 
 			// If same, do nothing.
 			if (ctrAttrName.equals(mapping.getControllingAttributeName()))
 				return;
 
 			////////////////////////////////////////
 			// Buffer current discrete mapping
 			////////////////////////////////////////
 			if (mapping instanceof DiscreteMapping) {
 				final String curMappingName = curCalc.toString() + "-"
 				                              + mapping.getControllingAttributeName();
 				final String newMappingName = curCalc.toString() + "-" + ctrAttrName;
 				final Map saved = discMapBuffer.get(newMappingName);
 
 				if (saved == null) {
 					discMapBuffer.put(curMappingName, ((DiscreteMapping) mapping).getAll());
 					mapping.setControllingAttributeName(ctrAttrName, vmm.getNetwork(), false);
 				} else if (saved != null) {
 					// Mapping exists
 					discMapBuffer.put(curMappingName,
 					                  ((DiscreteMapping) mapping).getAll());
 					mapping.setControllingAttributeName(ctrAttrName, vmm.getNetwork(), false);
 					((DiscreteMapping) mapping).putAll(saved);
 				}
 			}
 
 			visualPropertySheetPanel.removeProperty(typeRootProp);
 
 			final VizMapperProperty newRootProp = new VizMapperProperty();
 
 			if (type.isNodeProp())
 				buildProperty(vmm.getVisualStyle().getNodeAppearanceCalculator().getCalculator(type),
 				              newRootProp, NODE_VISUAL_MAPPING);
 			else
 				buildProperty(vmm.getVisualStyle().getEdgeAppearanceCalculator().getCalculator(type),
 				              newRootProp, EDGE_VISUAL_MAPPING);
 
 			removeProperty(typeRootProp);
			propertyMap.get(vmm.getVisualStyle().getName()).add(newRootProp);

 			typeRootProp = null;
 
 			expandLastSelectedItem(type.getName());
 			updateTableView();
 
 			// Finally, update graph view and focus.
 			vmm.setNetworkView(Cytoscape.getCurrentNetworkView());
 			Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
 
 			return;
 		}
 
 		// Return if not a Discrete Mapping.
 		if (mapping instanceof ContinuousMapping || mapping instanceof PassThroughMapping)
 			return;
 
 		Object key = null;
 
 		if ((type.getDataType() == Number.class) || (type.getDataType() == String.class)) {
 			key = e.getOldValue();
 
 			if (type.getDataType() == Number.class) {
 				numberCellEditor = new CyDoublePropertyEditor();
 				numberCellEditor.addPropertyChangeListener(this);
 				editorReg.registerEditor(prop, numberCellEditor);
 			}
 		} else {
 			key = ((Item) visualPropertySheetPanel.getTable().getValueAt(selected, 0)).getProperty()
 			       .getDisplayName();
 		}
 
 		/*
 		 * Need to convert this string to proper data types.
 		 */
 		final CyAttributes attr;
 		ctrAttrName = mapping.getControllingAttributeName();
 
 		if (type.isNodeProp()) {
 			attr = Cytoscape.getNodeAttributes();
 		} else {
 			attr = Cytoscape.getEdgeAttributes();
 		}
 
 		Byte attrType = attr.getType(ctrAttrName);
 
 		if (attrType != CyAttributes.TYPE_STRING) {
 			switch (attrType) {
 				case CyAttributes.TYPE_BOOLEAN:
 					key = Boolean.valueOf((String) key);
 
 					break;
 
 				case CyAttributes.TYPE_INTEGER:
 					key = Integer.valueOf((String) key);
 
 					break;
 
 				case CyAttributes.TYPE_FLOATING:
 					key = Double.valueOf((String) key);
 
 					break;
 
 				default:
 					break;
 			}
 		}
 
 		Object newValue = e.getNewValue();
 
 		if (type.getDataType() == Number.class) {
 			if ((((Number) newValue).doubleValue() == 0)
 			    || (newValue instanceof Number && type.toString().endsWith("OPACITY")
 			       && (((Number) newValue).doubleValue() > 255))) {
 				// JOptionPane.showMessageDialog(this, type.getName() + " should
 				// be positive number.",
 				// "Value is out of range", JOptionPane.WARNING_MESSAGE);
 				int shownPropCount = table.getRowCount();
 				Property p = null;
 				Object val = null;
 
 				for (int i = 0; i < shownPropCount; i++) {
 					p = ((Item) table.getValueAt(i, 0)).getProperty();
 
 					if (p != null) {
 						val = p.getDisplayName();
 
 						if ((val != null) && val.equals(key.toString())) {
 							p.setValue(((DiscreteMapping) mapping).getMapValue(key));
 
 							return;
 						}
 					}
 				}
 
 				return;
 			}
 		}
 
 		((DiscreteMapping) mapping).putMapValue(key, newValue);
 
 		/*
 		 * Update table and current network view.
 		 */
 		updateTableView();
 
 		visualPropertySheetPanel.repaint();
 		vmm.setNetworkView(Cytoscape.getCurrentNetworkView());
 		Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
 	}
 
 	/**
 	 * Switching between mapppings. Each calcs has 3 mappings. The first one
 	 * (getMapping(0)) is the current mapping used by calculator.
 	 *
 	 */
 	private void switchMapping(VizMapperProperty prop, String newMapName, Object attrName) {
 		if (attrName == null) {
 			return;
 		}
 
 		final VisualPropertyType type = (VisualPropertyType) ((VizMapperProperty) prop
 		                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           .getParentProperty())
 		                                .getHiddenObject();
 		final String newCalcName = vmm.getVisualStyle().getName() + "-" + type.getName() + "-"
 		                           + newMapName;
 
 		// Extract target calculator
 		Calculator newCalc = vmm.getCalculatorCatalog().getCalculator(type, newCalcName);
 
 		Calculator oldCalc = null;
 
 		if (type.isNodeProp())
 			oldCalc = vmm.getVisualStyle().getNodeAppearanceCalculator().getCalculator(type);
 		else
 			oldCalc = vmm.getVisualStyle().getEdgeAppearanceCalculator().getCalculator(type);
 
 		/*
 		 * If not exist, create new one.
 		 */
 		if (newCalc == null) {
 			newCalc = getNewCalculator(type, newMapName, newCalcName);
 			newCalc.getMapping(0).setControllingAttributeName((String) attrName, null, true);
 			vmm.getCalculatorCatalog().addCalculator(newCalc);
 		}
 
 		newCalc.getMapping(0).setControllingAttributeName((String) attrName, null, true);
 
 		if (type.isNodeProp()) {
 			vmm.getVisualStyle().getNodeAppearanceCalculator().setCalculator(newCalc);
 		} else
 			vmm.getVisualStyle().getEdgeAppearanceCalculator().setCalculator(newCalc);
 
 		/*
 		 * If old calc is not standard name, rename it.
 		 */
 		if (oldCalc != null) {
 			final String oldMappingTypeName;
 
 			if (oldCalc.getMapping(0) instanceof DiscreteMapping)
 				oldMappingTypeName = "Discrete Mapper";
 			else if (oldCalc.getMapping(0) instanceof ContinuousMapping)
 				oldMappingTypeName = "Continuous Mapper";
 			else if (oldCalc.getMapping(0) instanceof PassThroughMapping)
 				oldMappingTypeName = "Passthrough Mapper";
 			else
 				oldMappingTypeName = null;
 
 			final String oldCalcName = type.getName() + "-" + oldMappingTypeName;
 
 			if (vmm.getCalculatorCatalog().getCalculator(type, oldCalcName) == null) {
 				final Calculator newC = getNewCalculator(type, oldMappingTypeName, oldCalcName);
 				newC.getMapping(0).setControllingAttributeName((String) attrName, null, false);
 				vmm.getCalculatorCatalog().addCalculator(newC);
 			}
 		}
 
 		Property parent = prop.getParentProperty();
 		visualPropertySheetPanel.removeProperty(parent);
 
 		final VizMapperProperty newRootProp = new VizMapperProperty();
 
 		if (type.isNodeProp())
 			buildProperty(vmm.getVisualStyle().getNodeAppearanceCalculator().getCalculator(type),
 			              newRootProp, NODE_VISUAL_MAPPING);
 		else
 			buildProperty(vmm.getVisualStyle().getEdgeAppearanceCalculator().getCalculator(type),
 			              newRootProp, EDGE_VISUAL_MAPPING);
 
 		expandLastSelectedItem(type.getName());
 
 		removeProperty(parent);
 
 		propertyMap.get(vmm.getVisualStyle().getName()).add(newRootProp);
 
 		// vmm.getNetworkView().redrawGraph(false, true);
 		Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
 		parent = null;
 	}
 
 	private void expandLastSelectedItem(String name) {
 		final PropertySheetTable table = visualPropertySheetPanel.getTable();
 		Item item = null;
 		Property curProp;
 
 		for (int i = 0; i < table.getRowCount(); i++) {
 			item = (Item) table.getValueAt(i, 0);
 
 			curProp = item.getProperty();
 
 			if ((curProp != null) && (curProp.getDisplayName().equals(name))) {
 				table.setRowSelectionInterval(i, i);
 
 				if (item.isVisible() == false) {
 					item.toggle();
 				}
 
 				return;
 			}
 		}
 	}
 
 	private Calculator getNewCalculator(final VisualPropertyType type, final String newMappingName,
 	                                    final String newCalcName) {
 		System.out.println("Mapper = " + newMappingName);
 
 		final CalculatorCatalog catalog = vmm.getCalculatorCatalog();
 
 		Class mapperClass = catalog.getMapping(newMappingName);
 
 		if (mapperClass == null) {
 			return null;
 		}
 
 		// create the selected mapper
 		Class[] conTypes = { Object.class, byte.class };
 		Constructor mapperCon;
 
 		try {
 			mapperCon = mapperClass.getConstructor(conTypes);
 		} catch (NoSuchMethodException exc) {
 			// Should not happen...
 			System.err.println("Invalid mapper " + mapperClass.getName());
 
 			return null;
 		}
 
 		// create the mapper
 		final byte mapType; // node or edge calculator
 
 		if (type.isNodeProp())
 			mapType = ObjectMapping.NODE_MAPPING;
 		else
 			mapType = ObjectMapping.EDGE_MAPPING;
 
 		final Object defaultObj = type.getDefault(vmm.getVisualStyle());
 
 		System.out.println("defobj = " + defaultObj.getClass() + ", Type = " + type.getName());
 
 		final Object[] invokeArgs = { defaultObj, new Byte(mapType) };
 		ObjectMapping mapper = null;
 
 		try {
 			mapper = (ObjectMapping) mapperCon.newInstance(invokeArgs);
 		} catch (Exception exc) {
 			System.err.println("Error creating mapping");
 
 			return null;
 		}
 
 		return new BasicCalculator(newCalcName, mapper, type);
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param vsName
 	 *            DOCUMENT ME!
 	 */
 	public void setCurrentVS(String vsName) {
 		vsNameComboBox.setSelectedItem(vsName);
 	}
 
 	private void buildList() {
 		noMapping = new ArrayList<VisualPropertyType>();
 
 		final VisualStyle vs = vmm.getVisualStyle();
 		final NodeAppearanceCalculator nac = vs.getNodeAppearanceCalculator();
 		final EdgeAppearanceCalculator eac = vs.getEdgeAppearanceCalculator();
 
 		ObjectMapping mapping = null;
 
 		for (VisualPropertyType type : VisualPropertyType.values()) {
 			Calculator calc = nac.getCalculator(type);
 
 			if (calc == null) {
 				calc = eac.getCalculator(type);
 
 				if (calc != null)
 					mapping = calc.getMapping(0);
 			} else
 				mapping = calc.getMapping(0);
 
 			if ((mapping == null) && type.isAllowed())
 				noMapping.add(type);
 
 			mapping = null;
 		}
 	}
 
 	/*
 	 * Actions for option menu
 	 */
 	protected class CreateLegendListener extends AbstractAction {
 		public void actionPerformed(ActionEvent e) {
 			final SwingWorker worker = new SwingWorker() {
 				public Object construct() {
 					LegendDialog ld = new LegendDialog(Cytoscape.getDesktop(), vmm.getVisualStyle());
 					ld.setLocationRelativeTo(Cytoscape.getDesktop());
 					ld.setVisible(true);
 
 					return null;
 				}
 			};
 
 			worker.start();
 		}
 	}
 
 	/**
 	 * Create a new Visual Style.
 	 *
 	 * @author kono
 	 *
 	 */
 	private class NewStyleListener extends AbstractAction {
 		public void actionPerformed(ActionEvent e) {
 			final String name = getStyleName(null);
 
 			/*
 			 * If name is null, do not create style.
 			 */
 			if (name == null)
 				return;
 
 			// Create the new style
 			final VisualStyle newStyle = new VisualStyle(name);
 			final List<Calculator> calcs = new ArrayList<Calculator>(vmm.getCalculatorCatalog()
 			                                                            .getCalculators());
 			final Calculator dummy = calcs.get(0);
 			newStyle.getNodeAppearanceCalculator().setCalculator(dummy);
 
 			// add it to the catalog
 			vmm.getCalculatorCatalog().addVisualStyle(newStyle);
 			// Apply the new style
 			vmm.setVisualStyle(newStyle);
 			Cytoscape.getCurrentNetworkView().setVisualStyle(newStyle.getName());
 
 			removeMapping(dummy.getVisualPropertyType());
 
 			final JPanel defPanel = DefaultAppearenceBuilder.getDefaultView(name);
 			final DGraphView view = (DGraphView) ((DefaultViewPanel) defPanel).getView();
 			final Dimension panelSize = defaultAppearencePanel.getSize();
 
 			if (view != null) {
 				System.out.println("Creating Default Image for new visual style " + name);
 				createDefaultImage(name, view, panelSize);
 				setDefaultPanel(defaultImageManager.get(name));
 			}
 
 			vmm.setNetworkView(Cytoscape.getCurrentNetworkView());
 			//Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
 			switchVS(name);
 		}
 	}
 
 	/**
 	 * Get a new Visual Style name
 	 *
 	 * @param s
 	 *            DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	private String getStyleName(VisualStyle s) {
 		String suggestedName = null;
 
 		if (s != null)
 			suggestedName = vmm.getCalculatorCatalog().checkVisualStyleName(s.getName());
 
 		// keep prompting for input until user cancels or we get a valid
 		// name
 		while (true) {
 			String ret = (String) JOptionPane.showInputDialog(Cytoscape.getDesktop(),
 			                                                  "Please enter new name for the visual style.",
 			                                                  "Enter Visual Style Name",
 			                                                  JOptionPane.QUESTION_MESSAGE, null,
 			                                                  null, suggestedName);
 
 			if (ret == null)
 				return null;
 
 			String newName = vmm.getCalculatorCatalog().checkVisualStyleName(ret);
 
 			if (newName.equals(ret))
 				return ret;
 
 			int alt = JOptionPane.showConfirmDialog(Cytoscape.getDesktop(),
 			                                        "Visual style with name " + ret
 			                                        + " already exists,\nrename to " + newName
 			                                        + " okay?", "Duplicate visual style name",
 			                                        JOptionPane.YES_NO_OPTION,
 			                                        JOptionPane.WARNING_MESSAGE, null);
 
 			if (alt == JOptionPane.YES_OPTION)
 				return newName;
 		}
 	}
 
 	/**
 	 * Rename a Visual Style<br>
 	 *
 	 */
 	private class RenameStyleListener extends AbstractAction {
 		public void actionPerformed(ActionEvent e) {
 			final VisualStyle currentStyle = vmm.getVisualStyle();
 			final String oldName = currentStyle.getName();
 			final String name = getStyleName(currentStyle);
 
 			if (name == null) {
 				return;
 			}
 
 			lastVSName = name;
 
 			final Image img = defaultImageManager.get(oldName);
 			defaultImageManager.put(name, img);
 			defaultImageManager.remove(oldName);
 
 			/*
 			 * Update name
 			 */
 			currentStyle.setName(name);
 
 			vmm.getCalculatorCatalog().removeVisualStyle(oldName);
 			vmm.getCalculatorCatalog().addVisualStyle(currentStyle);
 
 			vmm.setVisualStyle(currentStyle);
 			vmm.getNetworkView().setVisualStyle(name);
 
 			/*
 			 * Update combo box and
 			 */
 			vsNameComboBox.addItem(name);
 			vsNameComboBox.setSelectedItem(name);
 			vsNameComboBox.removeItem(oldName);
 
 			final List<Property> props = propertyMap.get(oldName);
 			propertyMap.put(name, props);
 			propertyMap.remove(oldName);
 		}
 	}
 
 	/**
 	 * Remove selected visual style.
 	 */
 	private class RemoveStyleListener extends AbstractAction {
 		public void actionPerformed(ActionEvent e) {
 			if (vmm.getVisualStyle().getName().equals(DEFAULT_VS_NAME)) {
 				JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
 				                              "You cannot delete default style.",
 				                              "Cannot remove style!", JOptionPane.ERROR_MESSAGE);
 
 				return;
 			}
 
 			// make sure the user really wants to do this
 			final String styleName = vmm.getVisualStyle().getName();
 			final String checkString = "Are you sure you want to permanently delete"
 			                           + " the visual style '" + styleName + "'?";
 			int ich = JOptionPane.showConfirmDialog(Cytoscape.getDesktop(), checkString,
 			                                        "Confirm Delete Style",
 			                                        JOptionPane.YES_NO_OPTION);
 
 			if (ich == JOptionPane.YES_OPTION) {
 				final CalculatorCatalog catalog = vmm.getCalculatorCatalog();
 				catalog.removeVisualStyle(styleName);
 
 				// try to switch to the default style
 				VisualStyle currentStyle = catalog.getVisualStyle(DEFAULT_VS_NAME);
 
 				/*
 				 * Update Visual Mapping Browser.
 				 */
 				vsNameComboBox.removeItem(styleName);
 				vsNameComboBox.setSelectedItem(currentStyle.getName());
 				switchVS(currentStyle.getName());
 				defaultImageManager.remove(styleName);
 				propertyMap.remove(styleName);
 
 				vmm.setVisualStyle(currentStyle);
 				vmm.getNetworkView().setVisualStyle(currentStyle.getName());
 				// vmm.getNetworkView().redrawGraph(false, true);
 				Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
 			}
 		}
 	}
 
 	protected class CopyStyleListener extends AbstractAction {
 		public void actionPerformed(ActionEvent e) {
 			final VisualStyle currentStyle = vmm.getVisualStyle();
 			VisualStyle clone = null;
 
 			try {
 				clone = (VisualStyle) currentStyle.clone();
 			} catch (CloneNotSupportedException exc) {
 				System.err.println("Clone not supported exception!");
 			}
 
 			final String newName = getStyleName(clone);
 
 			if ((newName == null) || (newName.trim().length() == 0)) {
 				return;
 			}
 
 			clone.setName(newName);
 
 			// add new style to the catalog
 			vmm.getCalculatorCatalog().addVisualStyle(clone);
 			vmm.setVisualStyle(clone);
 
 			final JPanel defPanel = DefaultAppearenceBuilder.getDefaultView(newName);
 			final DGraphView view = (DGraphView) ((DefaultViewPanel) defPanel).getView();
 			final Dimension panelSize = defaultAppearencePanel.getSize();
 
 			if (view != null) {
 				System.out.println("Creating Default Image for new visual style " + newName);
 				createDefaultImage(newName, view, panelSize);
 				setDefaultPanel(defaultImageManager.get(newName));
 			}
 
 			vmm.setNetworkView(Cytoscape.getCurrentNetworkView());
 			Cytoscape.getDesktop().setFocus(Cytoscape.getCurrentNetworkView().getIdentifier());
 		}
 	}
 
 	/**
 	 * Remove a mapping from current visual style.
 	 *
 	 */
 	private void removeMapping() {
 		final int selected = visualPropertySheetPanel.getTable().getSelectedRow();
 
 		if (0 <= selected) {
 			Item item = (Item) visualPropertySheetPanel.getTable().getValueAt(selected, 0);
 			Property curProp = item.getProperty();
 
 			if (curProp instanceof VizMapperProperty) {
 				VisualPropertyType type = (VisualPropertyType) ((VizMapperProperty) curProp)
 				                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            .getHiddenObject();
 
 				if (type == null)
 					return;
 
 				String[] message = {
 				                       "The Mapping for " + type.getName() + " will be removed.",
 				                       "Proceed?"
 				                   };
 				int value = JOptionPane.showConfirmDialog(Cytoscape.getDesktop(), message,
 				                                          "Remove Mapping",
 				                                          JOptionPane.YES_NO_OPTION);
 
 				if (value == JOptionPane.YES_OPTION) {
 					/*
 					 * First, remove from property sheet.
 					 */
 
 					// visualPropertySheetPanel.removeProperty(curProp);
 					/*
 					 * Then, remove from calculator & redraw
 					 */
 					if (type.isNodeProp()) {
 						vmm.getVisualStyle().getNodeAppearanceCalculator().removeCalculator(type);
 					} else {
 						vmm.getVisualStyle().getEdgeAppearanceCalculator().removeCalculator(type);
 					}
 
 					// vmm.getNetworkView().redrawGraph(false, true);
 					Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
 
 					/*
 					 * Finally, move the visual property to "unused list"
 					 */
 					noMapping.add(type);
 
 					VizMapperProperty prop = new VizMapperProperty();
 					prop.setCategory(CATEGORY_UNUSED);
 					prop.setDisplayName(type.getName());
 					prop.setHiddenObject(type);
 					prop.setValue("Double-Click to create...");
 					visualPropertySheetPanel.addProperty(prop);
 
 					visualPropertySheetPanel.removeProperty(curProp);
 
 					removeProperty(curProp);
 
 					propertyMap.get(vmm.getVisualStyle().getName()).add(prop);
 					visualPropertySheetPanel.repaint();
 				}
 			}
 		}
 	}
 
 	private void removeMapping(final VisualPropertyType type) {
 		if (type.isNodeProp()) {
 			vmm.getVisualStyle().getNodeAppearanceCalculator().removeCalculator(type);
 		} else {
 			vmm.getVisualStyle().getEdgeAppearanceCalculator().removeCalculator(type);
 		}
 
 		Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
 
 		final Property[] props = visualPropertySheetPanel.getProperties();
 		Property toBeRemoved = null;
 
 		for (Property p : props) {
 			if (p.getDisplayName().equals(type.getName())) {
 				toBeRemoved = p;
 
 				break;
 			}
 		}
 
 		visualPropertySheetPanel.removeProperty(toBeRemoved);
 
 		removeProperty(toBeRemoved);
 
 		/*
 		 * Finally, move the visual property to "unused list"
 		 */
 		noMapping.add(type);
 
 		VizMapperProperty prop = new VizMapperProperty();
 		prop.setCategory(CATEGORY_UNUSED);
 		prop.setDisplayName(type.getName());
 		prop.setHiddenObject(type);
 		prop.setValue("Double-Click to create...");
 		visualPropertySheetPanel.addProperty(prop);
 
 		if(propertyMap.get(vmm.getVisualStyle().getName()) != null) {
 			propertyMap.get(vmm.getVisualStyle().getName()).add(prop);
 		}
 		
 		visualPropertySheetPanel.repaint();
 	}
 
 	/**
 	 * Edit all selected cells at once.
 	 *
 	 * This is for Discrete Mapping only.
 	 *
 	 */
 	private void editSelectedCells() {
 		final PropertySheetTable table = visualPropertySheetPanel.getTable();
 		final int[] selected = table.getSelectedRows();
 
 		Item item = null;
 
 		// If nothing selected, return.
 		if ((selected == null) || (selected.length == 0)) {
 			return;
 		}
 
 		/*
 		 * Test with the first selected item
 		 */
 		item = (Item) visualPropertySheetPanel.getTable().getValueAt(selected[0], 0);
 
 		VizMapperProperty prop = (VizMapperProperty) item.getProperty();
 
 		if ((prop == null) || (prop.getParentProperty() == null)) {
 			return;
 		}
 
 		final VisualPropertyType type = (VisualPropertyType) ((VizMapperProperty) prop
 		                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                .getParentProperty())
 		                                .getHiddenObject();
 
 		/*
 		 * Extract calculator
 		 */
 		final ObjectMapping mapping;
 		final CyAttributes attr;
 
 		if (type.isNodeProp()) {
 			mapping = vmm.getVisualStyle().getNodeAppearanceCalculator().getCalculator(type)
 			             .getMapping(0);
 			attr = Cytoscape.getNodeAttributes();
 		} else {
 			mapping = vmm.getVisualStyle().getEdgeAppearanceCalculator().getCalculator(type)
 			             .getMapping(0);
 			attr = Cytoscape.getEdgeAttributes();
 		}
 
 		if (mapping instanceof ContinuousMapping || mapping instanceof PassThroughMapping)
 			return;
 
 		Object newValue = null;
 
 		try {
 			newValue = type.showDiscreteEditor();
 		} catch (Exception e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		Object key = null;
 		final Class keyClass = CyAttributesUtils.getClass(mapping.getControllingAttributeName(),
 		                                                  attr);
 
 		for (int i = 0; i < selected.length; i++) {
 			/*
 			 * First, update property sheet
 			 */
 			((Item) visualPropertySheetPanel.getTable().getValueAt(selected[i], 0)).getProperty()
 			 .setValue(newValue);
 			/*
 			 * Then update backend.
 			 */
 			key = ((Item) visualPropertySheetPanel.getTable().getValueAt(selected[i], 0)).getProperty()
 			       .getDisplayName();
 
 			if (keyClass == Integer.class) {
 				key = Integer.valueOf((String) key);
 			} else if (keyClass == Double.class) {
 				key = Double.valueOf((String) key);
 			} else if (keyClass == Boolean.class) {
 				key = Boolean.valueOf((String) key);
 			}
 
 			((DiscreteMapping) mapping).putMapValue(key, newValue);
 		}
 
 		/*
 		 * Update table and current network view.
 		 */
 		table.repaint();
 		vmm.setNetworkView(Cytoscape.getCurrentNetworkView());
 		Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
 	}
 
 	/*
 	 * Remove an entry in the browser.
 	 */
 	private void removeProperty(final Property prop) {
 		List<Property> targets = new ArrayList<Property>();
 
 		if (propertyMap.get(vmm.getVisualStyle().getName()) == null) {
 			return;
 		}
 
 		for (Property p : propertyMap.get(vmm.getVisualStyle().getName())) {
 			if (p.getDisplayName().equals(prop.getDisplayName())) {
 				targets.add(p);
 			}
 		}
 
 		for (Property p : targets) {
 			System.out.println("Removed: " + p.getDisplayName());
 			propertyMap.get(vmm.getVisualStyle().getName()).remove(p);
 		}
 	}
 
 	private class GenerateValueListener extends AbstractAction {
 		private final int MAX_COLOR = 256 * 256 * 256;
 		private DiscreteMapping dm;
 		protected static final int RAINBOW1 = 1;
 		protected static final int RAINBOW2 = 2;
 		protected static final int RANDOM = 3;
 		private final int functionType;
 
 		public GenerateValueListener(final int type) {
 			this.functionType = type;
 		}
 
 		/**
 		 * User wants to Seed the Discrete Mapper with Random Color Values.
 		 */
 		public void actionPerformed(ActionEvent e) {
 			/*
 			 * Check Selected poperty
 			 */
 			final int selectedRow = visualPropertySheetPanel.getTable().getSelectedRow();
 
 			if (selectedRow < 0) {
 				System.out.println("No entry selected.");
 
 				return;
 			}
 
 			final Item item = (Item) visualPropertySheetPanel.getTable().getValueAt(selectedRow, 0);
 			final VizMapperProperty prop = (VizMapperProperty) item.getProperty();
 			final Object hidden = prop.getHiddenObject();
 
 			if (hidden instanceof VisualPropertyType) {
 				final VisualPropertyType type = (VisualPropertyType) hidden;
 				System.out.println("This is category top.");
 
 				final Map valueMap = new HashMap();
 				final long seed = System.currentTimeMillis();
 				final Random rand = new Random(seed);
 
 				final ObjectMapping oMap;
 
 				final CyAttributes attr;
 				final int nOre;
 
 				if (type.isNodeProp()) {
 					attr = Cytoscape.getNodeAttributes();
 					oMap = vmm.getVisualStyle().getNodeAppearanceCalculator().getCalculator(type)
 					          .getMapping(0);
 					nOre = ObjectMapping.NODE_MAPPING;
 				} else {
 					attr = Cytoscape.getEdgeAttributes();
 					oMap = vmm.getVisualStyle().getEdgeAppearanceCalculator().getCalculator(type)
 					          .getMapping(0);
 					nOre = ObjectMapping.EDGE_MAPPING;
 				}
 
 				if ((oMap instanceof DiscreteMapping) == false) {
 					return;
 				}
 
 				dm = (DiscreteMapping) oMap;
 
 				final Set<Object> attrSet = loadKeys(oMap.getControllingAttributeName(), attr,
 				                                     oMap, nOre);
 
 				/*
 				 * Create random colors
 				 */
 				final float increment = 1f / ((Number) attrSet.size()).floatValue();
 
 				float hue = 0;
 				float sat = 0;
 				float br = 0;
 
 				if (type.getDataType() == Color.class) {
 					int i = 0;
 
 					if (functionType == RAINBOW1) {
 						for (Object key : attrSet) {
 							hue = hue + increment;
 							valueMap.put(key, new Color(Color.HSBtoRGB(hue, 1f, 1f)));
 						}
 					} else if (functionType == RAINBOW2) {
 						for (Object key : attrSet) {
 							hue = hue + increment;
 							// sat =
 							// Math.abs(((Number)Math.cos((i)/(2*Math.PI))).floatValue())/3
 							// + rand.nextFloat()*0.666f;
 							// br =
 							// Math.abs(((Number)Math.sin((((float)i)/2f)/(2*Math.PI))).floatValue())/2
 							// + rand.nextFloat()*0.5f;
 							sat = (Math.abs(((Number) Math.cos((8 * i) / (2 * Math.PI))).floatValue()) * 0.7f)
 							      + 0.3f;
 							br = (Math.abs(((Number) Math.sin(((i) / (2 * Math.PI)) + (Math.PI / 2)))
 							               .floatValue()) * 0.7f) + 0.3f;
 							valueMap.put(key, new Color(Color.HSBtoRGB(hue, sat, br)));
 							i++;
 						}
 					} else {
 						for (Object key : attrSet)
 							valueMap.put(key,
 							             new Color(((Number) (rand.nextFloat() * MAX_COLOR))
 							                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          .intValue()));
 					}
 				} else if ((type.getDataType() == Number.class) && (functionType == RANDOM)) {
 					final String range = JOptionPane.showInputDialog(visualPropertySheetPanel,
 					                                                 "Please enter the value range (example: 30-100)",
 					                                                 "Assign Random Numbers",
 					                                                 JOptionPane.PLAIN_MESSAGE);
 
 					String[] rangeVals = range.split("-");
 
 					if (rangeVals.length != 2)
 						return;
 
 					Float min = Float.valueOf(rangeVals[0]);
 					Float max = Float.valueOf(rangeVals[1]);
 					Float valueRange = max - min;
 
 					for (Object key : attrSet)
 						valueMap.put(key, (rand.nextFloat() * valueRange) + min);
 				}
 
 				dm.putAll(valueMap);
 				vmm.setNetworkView(Cytoscape.getCurrentNetworkView());
 				Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
 
 				visualPropertySheetPanel.removeProperty(prop);
 
 				final VizMapperProperty newRootProp = new VizMapperProperty();
 
 				if (type.isNodeProp())
 					buildProperty(vmm.getVisualStyle().getNodeAppearanceCalculator()
 					                 .getCalculator(type), newRootProp, NODE_VISUAL_MAPPING);
 				else
 					buildProperty(vmm.getVisualStyle().getEdgeAppearanceCalculator()
 					                 .getCalculator(type), newRootProp, EDGE_VISUAL_MAPPING);
 
 				removeProperty(prop);
 				propertyMap.get(vmm.getVisualStyle().getName()).add(newRootProp);
 
 				expandLastSelectedItem(type.getName());
 			} else {
 				System.out.println("Invalid.");
 			}
 
 			return;
 		}
 	}
 
 	private class GenerateSeriesListener extends AbstractAction {
 		private DiscreteMapping dm;
 
 		/**
 		 * User wants to Seed the Discrete Mapper with Random Color Values.
 		 */
 		public void actionPerformed(ActionEvent e) {
 			/*
 			 * Check Selected poperty
 			 */
 			final int selectedRow = visualPropertySheetPanel.getTable().getSelectedRow();
 
 			if (selectedRow < 0)
 				return;
 
 			final Item item = (Item) visualPropertySheetPanel.getTable().getValueAt(selectedRow, 0);
 			final VizMapperProperty prop = (VizMapperProperty) item.getProperty();
 			final Object hidden = prop.getHiddenObject();
 
 			if (hidden instanceof VisualPropertyType) {
 				final VisualPropertyType type = (VisualPropertyType) hidden;
 
 				final Map valueMap = new HashMap();
 				final ObjectMapping oMap;
 				final CyAttributes attr;
 				final int nOre;
 
 				if (type.isNodeProp()) {
 					attr = Cytoscape.getNodeAttributes();
 					oMap = vmm.getVisualStyle().getNodeAppearanceCalculator().getCalculator(type)
 					          .getMapping(0);
 					nOre = ObjectMapping.NODE_MAPPING;
 				} else {
 					attr = Cytoscape.getEdgeAttributes();
 					oMap = vmm.getVisualStyle().getEdgeAppearanceCalculator().getCalculator(type)
 					          .getMapping(0);
 					nOre = ObjectMapping.EDGE_MAPPING;
 				}
 
 				if ((oMap instanceof DiscreteMapping) == false)
 					return;
 
 				dm = (DiscreteMapping) oMap;
 
 				final Set<Object> attrSet = loadKeys(oMap.getControllingAttributeName(), attr,
 				                                     oMap, nOre);
 				final String start = JOptionPane.showInputDialog(visualPropertySheetPanel,
 				                                                 "Please enter start value (1st number in the series)",
 				                                                 "0");
 				final String increment = JOptionPane.showInputDialog(visualPropertySheetPanel,
 				                                                     "Please enter increment", "1");
 
 				if ((increment == null) || (start == null))
 					return;
 
 				Float inc;
 				Float st;
 
 				try {
 					inc = Float.valueOf(increment);
 					st = Float.valueOf(start);
 				} catch (Exception ex) {
 					ex.printStackTrace();
 					inc = null;
 					st = null;
 				}
 
 				if ((inc == null) || (inc < 0) || (st == null) || (st == null)) {
 					return;
 				}
 
 				if (type.getDataType() == Number.class) {
 					for (Object key : attrSet) {
 						valueMap.put(key, st);
 						st = st + inc;
 					}
 				}
 
 				dm.putAll(valueMap);
 
 				vmm.setNetworkView(Cytoscape.getCurrentNetworkView());
 				Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
 
 				visualPropertySheetPanel.removeProperty(prop);
 
 				final VizMapperProperty newRootProp = new VizMapperProperty();
 
 				if (type.isNodeProp())
 					buildProperty(vmm.getVisualStyle().getNodeAppearanceCalculator()
 					                 .getCalculator(type), newRootProp, NODE_VISUAL_MAPPING);
 				else
 					buildProperty(vmm.getVisualStyle().getEdgeAppearanceCalculator()
 					                 .getCalculator(type), newRootProp, EDGE_VISUAL_MAPPING);
 
 				removeProperty(prop);
 				propertyMap.get(vmm.getVisualStyle().getName()).add(newRootProp);
 
 				expandLastSelectedItem(type.getName());
 			} else {
 				System.out.println("Invalid.");
 			}
 
 			return;
 		}
 	}
 
 	private class FitLabelListener extends AbstractAction {
 		private DiscreteMapping dm;
 
 		/**
 		 * User wants to Seed the Discrete Mapper with Random Color Values.
 		 */
 		public void actionPerformed(ActionEvent e) {
 			/*
 			 * Check Selected poperty
 			 */
 			final int selectedRow = visualPropertySheetPanel.getTable().getSelectedRow();
 
 			if (selectedRow < 0)
 				return;
 
 			final Item item = (Item) visualPropertySheetPanel.getTable().getValueAt(selectedRow, 0);
 			final VizMapperProperty prop = (VizMapperProperty) item.getProperty();
 			final Object hidden = prop.getHiddenObject();
 
 			if (hidden instanceof VisualPropertyType) {
 				final VisualPropertyType type = (VisualPropertyType) hidden;
 
 				final Map valueMap = new HashMap();
 				final ObjectMapping oMap;
 				final CyAttributes attr;
 
 				if (type.isNodeProp()) {
 					attr = Cytoscape.getNodeAttributes();
 					oMap = vmm.getVisualStyle().getNodeAppearanceCalculator().getCalculator(type)
 					          .getMapping(0);
 				} else {
 					attr = Cytoscape.getEdgeAttributes();
 					oMap = vmm.getVisualStyle().getEdgeAppearanceCalculator().getCalculator(type)
 					          .getMapping(0);
 				}
 
 				if ((oMap instanceof DiscreteMapping) == false)
 					return;
 
 				dm = (DiscreteMapping) oMap;
 
 				final Calculator nodeLabelCalc = vmm.getVisualStyle().getNodeAppearanceCalculator()
 				                                    .getCalculator(VisualPropertyType.NODE_LABEL);
 
 				if (nodeLabelCalc == null) {
 					return;
 				}
 
 				final String ctrAttrName = nodeLabelCalc.getMapping(0).getControllingAttributeName();
 				dm.setControllingAttributeName(ctrAttrName, Cytoscape.getCurrentNetwork(), false);
 
 				// final Set<Object> attrSet =
 				// loadKeys(oMap.getControllingAttributeName(), attr, oMap);
 				if (vmm.getVisualStyle().getNodeAppearanceCalculator().getNodeSizeLocked()) {
 					return;
 				}
 
 				DiscreteMapping wm = null;
 
 				if ((type == NODE_WIDTH)) {
 					wm = (DiscreteMapping) vmm.getVisualStyle().getNodeAppearanceCalculator()
 					                          .getCalculator(NODE_WIDTH).getMapping(0);
 
 					wm.setControllingAttributeName(ctrAttrName, Cytoscape.getCurrentNetwork(), false);
 
 					Set<Object> attrSet1;
 
 					if (ctrAttrName.equals("ID")) {
 						attrSet1 = new TreeSet<Object>();
 
 						for (Object node : Cytoscape.getCurrentNetwork().nodesList()) {
 							attrSet1.add(((Node) node).getIdentifier());
 						}
 					} else {
 						attrSet1 = loadKeys(wm.getControllingAttributeName(), attr, wm,
 						                    ObjectMapping.NODE_MAPPING);
 					}
 
 					Integer height = ((Number) (vmm.getVisualStyle().getNodeAppearanceCalculator()
 					                               .getDefaultAppearance().get(NODE_FONT_SIZE)))
 					                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            .intValue();
 					vmm.getVisualStyle().getNodeAppearanceCalculator().getDefaultAppearance()
 					   .set(NODE_HEIGHT, height * 2.5);
 
 					Integer fontSize = ((Number) vmm.getVisualStyle().getNodeAppearanceCalculator()
 					                                .getDefaultAppearance().get(NODE_FONT_SIZE))
 					                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  .intValue();
 					int strLen;
 
 					String labelString = null;
 					String[] listObj;
 					int longest = 0;
 
 					if (attr.getType(ctrAttrName) == CyAttributes.TYPE_SIMPLE_LIST) {
 						wm.setControllingAttributeName("ID", Cytoscape.getCurrentNetwork(), false);
 
 						attrSet1 = new TreeSet<Object>();
 
 						for (Object node : Cytoscape.getCurrentNetwork().nodesList()) {
 							attrSet1.add(((Node) node).getIdentifier());
 						}
 
 						CyNetworkView net = Cytoscape.getCurrentNetworkView();
 						String text;
 
 						for (Object node : net.getNetwork().nodesList()) {
 							text = net.getNodeView((Node) node).getLabel().getText();
 							strLen = text.length();
 
 							if (strLen != 0) {
 								listObj = text.split("\\n");
 								longest = 0;
 
 								for (String s : listObj) {
 									if (s.length() > longest) {
 										longest = s.length();
 									}
 								}
 
 								strLen = longest;
 
 								if (strLen > 25) {
 									valueMap.put(((Node) node).getIdentifier(),
 									             strLen * fontSize * 0.6);
 								} else {
 									valueMap.put(((Node) node).getIdentifier(),
 									             strLen * fontSize * 0.8);
 								}
 							}
 						}
 					} else {
 						for (Object label : attrSet1) {
 							labelString = label.toString();
 							strLen = labelString.length();
 
 							if (strLen != 0) {
 								if (labelString.contains("\n")) {
 									listObj = labelString.split("\\n");
 									longest = 0;
 
 									for (String s : listObj) {
 										if (s.length() > longest) {
 											longest = s.length();
 										}
 									}
 
 									strLen = longest;
 								}
 
 								if (strLen > 25) {
 									valueMap.put(label, strLen * fontSize * 0.6);
 								} else {
 									valueMap.put(label, strLen * fontSize * 0.8);
 								}
 							}
 						}
 					}
 				} else if ((type == NODE_HEIGHT)) {
 					wm = (DiscreteMapping) vmm.getVisualStyle().getNodeAppearanceCalculator()
 					                          .getCalculator(NODE_HEIGHT).getMapping(0);
 
 					wm.setControllingAttributeName(ctrAttrName, Cytoscape.getCurrentNetwork(), false);
 
 					Set<Object> attrSet1;
 
 					if (ctrAttrName.equals("ID")) {
 						attrSet1 = new TreeSet<Object>();
 
 						for (Object node : Cytoscape.getCurrentNetwork().nodesList()) {
 							attrSet1.add(((Node) node).getIdentifier());
 						}
 					} else {
 						attrSet1 = loadKeys(wm.getControllingAttributeName(), attr, wm,
 						                    ObjectMapping.NODE_MAPPING);
 					}
 
 					Integer fontSize = ((Number) vmm.getVisualStyle().getNodeAppearanceCalculator()
 					                                .getDefaultAppearance().get(NODE_FONT_SIZE))
 					                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              .intValue();
 					int strLen;
 
 					String labelString = null;
 					String[] listObj;
 
 					if (attr.getType(ctrAttrName) == CyAttributes.TYPE_SIMPLE_LIST) {
 						wm.setControllingAttributeName("ID", Cytoscape.getCurrentNetwork(), false);
 
 						attrSet1 = new TreeSet<Object>();
 
 						for (Object node : Cytoscape.getCurrentNetwork().nodesList()) {
 							attrSet1.add(((Node) node).getIdentifier());
 						}
 
 						CyNetworkView net = Cytoscape.getCurrentNetworkView();
 						String text;
 
 						for (Object node : net.getNetwork().nodesList()) {
 							text = net.getNodeView((Node) node).getLabel().getText();
 							strLen = text.length();
 
 							if (strLen != 0) {
 								listObj = text.split("\\n");
 								valueMap.put(((Node) node).getIdentifier(),
 								             listObj.length * fontSize * 1.6);
 							}
 						}
 					} else {
 						for (Object label : attrSet1) {
 							labelString = label.toString();
 							strLen = labelString.length();
 
 							if (strLen != 0) {
 								if (labelString.contains("\n")) {
 									listObj = labelString.split("\\n");
 
 									strLen = listObj.length;
 								} else {
 									strLen = 1;
 								}
 
 								valueMap.put(label, strLen * fontSize * 1.6);
 							}
 						}
 					}
 				}
 
 				wm.putAll(valueMap);
 
 				vmm.setNetworkView(Cytoscape.getCurrentNetworkView());
 				Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
 
 				visualPropertySheetPanel.removeProperty(prop);
 
 				final VizMapperProperty newRootProp = new VizMapperProperty();
 
 				if (type.isNodeProp())
 					buildProperty(vmm.getVisualStyle().getNodeAppearanceCalculator()
 					                 .getCalculator(type), newRootProp, NODE_VISUAL_MAPPING);
 				else
 					buildProperty(vmm.getVisualStyle().getEdgeAppearanceCalculator()
 					                 .getCalculator(type), newRootProp, EDGE_VISUAL_MAPPING);
 
 				removeProperty(prop);
 				propertyMap.get(vmm.getVisualStyle().getName()).add(newRootProp);
 
 				expandLastSelectedItem(type.getName());
 			} else {
 				System.out.println("Invalid.");
 			}
 
 			return;
 		}
 	}
 
 	private class BrightnessListener extends AbstractAction {
 		private DiscreteMapping dm;
 		protected static final int DARKER = 1;
 		protected static final int BRIGHTER = 2;
 		private final int functionType;
 
 		public BrightnessListener(final int type) {
 			this.functionType = type;
 		}
 
 		/**
 		 * User wants to Seed the Discrete Mapper with Random Color Values.
 		 */
 		public void actionPerformed(ActionEvent e) {
 			/*
 			 * Check Selected poperty
 			 */
 			final int selectedRow = visualPropertySheetPanel.getTable().getSelectedRow();
 
 			if (selectedRow < 0) {
 				return;
 			}
 
 			final Item item = (Item) visualPropertySheetPanel.getTable().getValueAt(selectedRow, 0);
 			final VizMapperProperty prop = (VizMapperProperty) item.getProperty();
 			final Object hidden = prop.getHiddenObject();
 
 			if (hidden instanceof VisualPropertyType) {
 				final VisualPropertyType type = (VisualPropertyType) hidden;
 
 				final Map valueMap = new HashMap();
 				final ObjectMapping oMap;
 
 				final CyAttributes attr;
 				final int nOre;
 
 				if (type.isNodeProp()) {
 					attr = Cytoscape.getNodeAttributes();
 					oMap = vmm.getVisualStyle().getNodeAppearanceCalculator().getCalculator(type)
 					          .getMapping(0);
 					nOre = ObjectMapping.NODE_MAPPING;
 				} else {
 					attr = Cytoscape.getEdgeAttributes();
 					oMap = vmm.getVisualStyle().getEdgeAppearanceCalculator().getCalculator(type)
 					          .getMapping(0);
 					nOre = ObjectMapping.EDGE_MAPPING;
 				}
 
 				if ((oMap instanceof DiscreteMapping) == false) {
 					return;
 				}
 
 				dm = (DiscreteMapping) oMap;
 
 				final Set<Object> attrSet = loadKeys(oMap.getControllingAttributeName(), attr,
 				                                     oMap, nOre);
 
 				/*
 				 * Create random colors
 				 */
 				if (type.getDataType() == Color.class) {
 					Object c;
 
 					if (functionType == BRIGHTER) {
 						for (Object key : attrSet) {
 							c = dm.getMapValue(key);
 
 							if ((c != null) && c instanceof Color) {
 								valueMap.put(key, ((Color) c).brighter());
 							}
 						}
 					} else if (functionType == DARKER) {
 						for (Object key : attrSet) {
 							c = dm.getMapValue(key);
 
 							if ((c != null) && c instanceof Color) {
 								valueMap.put(key, ((Color) c).darker());
 							}
 						}
 					}
 				}
 
 				dm.putAll(valueMap);
 				vmm.setNetworkView(Cytoscape.getCurrentNetworkView());
 				Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
 
 				visualPropertySheetPanel.removeProperty(prop);
 
 				final VizMapperProperty newRootProp = new VizMapperProperty();
 
 				if (type.isNodeProp())
 					buildProperty(vmm.getVisualStyle().getNodeAppearanceCalculator()
 					                 .getCalculator(type), newRootProp, NODE_VISUAL_MAPPING);
 				else
 					buildProperty(vmm.getVisualStyle().getEdgeAppearanceCalculator()
 					                 .getCalculator(type), newRootProp, EDGE_VISUAL_MAPPING);
 
 				removeProperty(prop);
 				propertyMap.get(vmm.getVisualStyle().getName()).add(newRootProp);
 
 				expandLastSelectedItem(type.getName());
 			} else {
 				System.out.println("Invalid.");
 			}
 
 			return;
 		}
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param arg0
 	 *            DOCUMENT ME!
 	 */
 	public void popupMenuCanceled(PopupMenuEvent arg0) {
 		rainbow1.setEnabled(false);
 		rainbow2.setEnabled(false);
 		randomize.setEnabled(false);
 		series.setEnabled(false);
 		fit.setEnabled(false);
 		brighter.setEnabled(false);
 		darker.setEnabled(false);
 		delete.setEnabled(false);
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param e
 	 *            DOCUMENT ME!
 	 */
 	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
 	}
 
 	/**
 	 * Check the selected VPT and enable/disable menu items.
 	 *
 	 * @param e
 	 *            DOCUMENT ME!
 	 */
 	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
 		final int selected = visualPropertySheetPanel.getTable().getSelectedRow();
 
 		if (0 > selected) {
 			return;
 		}
 
 		final Item item = (Item) visualPropertySheetPanel.getTable().getValueAt(selected, 0);
 		final Property curProp = item.getProperty();
 
 		if (curProp == null)
 			return;
 
 		VizMapperProperty prop = ((VizMapperProperty) curProp);
 
 		if (prop.getHiddenObject() instanceof VisualPropertyType) {
 			// Enble delete menu
 			delete.setEnabled(true);
 
 			VisualPropertyType type = ((VisualPropertyType) prop.getHiddenObject());
 			Class dataType = type.getDataType();
 
 			if (dataType == Color.class) {
 				rainbow1.setEnabled(true);
 				rainbow2.setEnabled(true);
 				randomize.setEnabled(true);
 				brighter.setEnabled(true);
 				darker.setEnabled(true);
 			} else if (dataType == Number.class) {
 				randomize.setEnabled(true);
 				series.setEnabled(true);
 			}
 
 			if ((type == VisualPropertyType.NODE_WIDTH) || (type == VisualPropertyType.NODE_HEIGHT)) {
 				fit.setEnabled(true);
 			}
 		}
 
 		return;
 	}
 
 	/**
 	 * <p>
 	 * If user selects ID as controlling attributes name, cretate list of IDs
 	 * from actual list of nodes/edges.
 	 * </p>
 	 *
 	 * @return
 	 */
 	private Set<Object> loadID(final int nOre) {
 		Set<Object> ids = new TreeSet<Object>();
 
 		List<Object> obj;
 
 		if (nOre == ObjectMapping.NODE_MAPPING) {
 			obj = Cytoscape.getCurrentNetworkView().getNetwork().nodesList();
 		} else {
 			obj = Cytoscape.getCurrentNetworkView().getNetwork().edgesList();
 		}
 
 		for (Object o : obj) {
 			ids.add(((GraphObject) o).getIdentifier());
 		}
 
 		return ids;
 	}
 
 	// /**
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @param e
 	 *            DOCUMENT ME!
 	 */
 	public void stateChanged(ChangeEvent e) {
 		final String selectedName = (String) vsNameComboBox.getSelectedItem();
 		final String currentName = vmm.getVisualStyle().getName();
 		final CyNetworkView curView = Cytoscape.getCurrentNetworkView();
 
 		System.out.println("Got VMM Change event.  Cur VS in VMM: " + vmm.getVisualStyle().getName());
 		
 		if (selectedName == null || currentName == null || curView == null || curView.equals(Cytoscape.getNullNetworkView()) )
 			return;
 		
 //		// We need to update the combo box and switch visual styles.
 //		// Now check if we need to add a new item:
 //		if (!findVSName(currentName)) {
 //			vsNameComboBox.addItem(currentName);
 //		}
 		
 		// Update GUI based on CalcCatalog's state.
 		if(!findVSName(currentName)) {
 			syncStyleBox();
 		}
 	}
 	
 	private void syncStyleBox() {
 		
 		final CyNetworkView curView = Cytoscape.getCurrentNetworkView();
 		
 		String styleName;
 		List<String> namesInBox = new ArrayList<String>();
 		namesInBox.addAll(vmm.getCalculatorCatalog().getVisualStyleNames());
 		
 		for(int i=0; i<vsNameComboBox.getItemCount(); i++) {
 			styleName = vsNameComboBox.getItemAt(i).toString();
 			if(vmm.getCalculatorCatalog().getVisualStyle(styleName) == null) {
 				// No longer exists in the VMM.  Remove.
 				vsNameComboBox.removeItem(styleName);
 				defaultImageManager.remove(styleName);
 				propertyMap.remove(styleName);
 			} 
 		}
 		Collections.sort(namesInBox);
 		
 		// Reset combobox items.
 		vsNameComboBox.removeAllItems();
 		for(String name: namesInBox) {
 			vsNameComboBox.addItem(name);
 		}
 		
 		switchVS(vmm.getVisualStyle().getName());
 		
 		//vsNameComboBox.setSelectedItem(vmm.getVisualStyle().getName());
 		
 		// Sync style and selected item.
 		
 //		if(curView != null && curView.equals(Cytoscape.getNullNetworkView()) == false) {
 //			
 //			// Get visual style name associated with the current network view.
 //			String curNetVSName = curView.getVisualStyle().getName();
 //			
 //			if(curNetVSName.equals(vmm.getVisualStyle().getName()) == false) {
 //				vmm.removeChangeListener(this);
 //				vmm.setVisualStyle(curView.getVisualStyle());
 //				vmm.addChangeListener(this);
 //			}
 //			
 //			if(vsNameComboBox.getSelectedItem().toString().equals(curNetVSName) == false) {
 //				//vmm.setNetworkView(curView);
 //				//switchVS(curNetVSName);
 //			}
 //			
 //		}
 //		if(vmm.getVisualStyle().getName().equals(vsNameComboBox.getSelectedItem().toString()) == false) {
 //			vsNameComboBox.setSelectedItem(vmm.getVisualStyle().getName());
 //			switchVS(vmm.getVisualStyle().getName());
 //		}
 	}
 
 	// return true iff 'match' is found as a name within the
 	// vsNameComboBox.
 	private boolean findVSName(String match) {
 		for (int i = 0; i < vsNameComboBox.getItemCount(); i++) {
 			if (vsNameComboBox.getItemAt(i).equals(match)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 *
 	 * @return DOCUMENT ME!
 	 */
 	public Object getSelectedItem() {
 		final JTable table = visualPropertySheetPanel.getTable();
 
 		return table.getModel().getValueAt(table.getSelectedRow(), 0);
 	}
 }
