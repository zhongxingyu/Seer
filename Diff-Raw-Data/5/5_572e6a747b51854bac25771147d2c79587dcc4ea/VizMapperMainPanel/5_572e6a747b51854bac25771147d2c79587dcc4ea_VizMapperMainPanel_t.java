 /*
  Copyright (c) 2006, 2007, 2010, The Cytoscape Consortium (www.cytoscape.org)
 
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
 
 
 import static cytoscape.visual.VisualPropertyDependency.Definition.NODE_SIZE_LOCKED;
 import static cytoscape.visual.VisualPropertyType.NODE_FONT_SIZE;
 import static cytoscape.visual.VisualPropertyType.NODE_HEIGHT;
 import static cytoscape.visual.VisualPropertyType.NODE_WIDTH;
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
 import java.util.ArrayList;
 import java.util.Arrays;
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
 
 import com.l2fprod.common.propertysheet.Property;
 import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
 import com.l2fprod.common.propertysheet.PropertyRendererRegistry;
 import com.l2fprod.common.propertysheet.PropertySheetPanel;
 import com.l2fprod.common.propertysheet.PropertySheetTable;
 import com.l2fprod.common.propertysheet.PropertySheetTableModel.Item;
 import com.l2fprod.common.swing.plaf.blue.BlueishButtonUI;
 
 import cytoscape.CyNetwork;
 import cytoscape.Cytoscape;
 import cytoscape.data.CyAttributes;
 import cytoscape.data.CyAttributesUtils;
 import cytoscape.data.attr.MultiHashMapDefinitionListener;
 import cytoscape.logger.CyLogger;
 import cytoscape.task.ui.JTaskConfig;
 import cytoscape.task.util.TaskManager;
 import cytoscape.util.SwingWorker;
 import cytoscape.util.swing.DropDownMenuButton;
 import cytoscape.view.CyNetworkView;
 import cytoscape.view.CytoscapeDesktop;
 import cytoscape.view.NetworkPanel;
 import cytoscape.visual.CalculatorCatalog;
 import cytoscape.visual.EdgeAppearanceCalculator;
 import cytoscape.visual.NodeAppearanceCalculator;
 import cytoscape.visual.VisualMappingManager;
 import cytoscape.visual.VisualPropertyDependency;
 import cytoscape.visual.VisualPropertyDependency.Definition;
 import cytoscape.visual.VisualPropertyType;
 import cytoscape.visual.VisualStyle;
 import cytoscape.visual.calculators.Calculator;
 import cytoscape.visual.mappings.ContinuousMapping;
 import cytoscape.visual.mappings.DiscreteMapping;
 import cytoscape.visual.mappings.ObjectMapping;
 import cytoscape.visual.mappings.PassThroughMapping;
 import cytoscape.visual.ui.editors.continuous.ContinuousMappingEditorPanel;
 import cytoscape.visual.ui.editors.discrete.CyComboBoxPropertyEditor;
 import ding.view.DGraphView;
 
 
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
 public class VizMapperMainPanel extends JPanel implements PropertyChangeListener, PopupMenuListener, ChangeListener, 
 															MultiHashMapDefinitionListener {
 	
 	private static final long serialVersionUID = 2010449914223315524L;
 	
 	private static final Color UNUSED_COLOR = new Color(100, 100, 100, 50);
 	
 	private static final int ROW_HEIGHT = 20;
 	private static final int ROW_HEIGHT_POSITION = 50;
 	private static final int ROW_HEIGHT_GRAPHICS = 100;
 	
 	public enum DefaultEditor {
 		NODE,
 		EDGE,
 		GLOBAL;
 	}
 
 	// Context menu items
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
 	private static JMenu dependencies;
 	private static Map<Definition,JCheckBoxMenuItem> dependencyMenuItems; 
 
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
 
 	/*
 	 * Visual mapping manager. All parameters should be taken from here.
 	 */
 	private final VisualMappingManager vmm;
 
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
 	private CyLogger logger = CyLogger.getLogger(VizMapperMainPanel.class);
 
 	private Set<VizMapperProperty> hiddenProperties = new HashSet<VizMapperProperty>();
 
 	
 	/** Creates new form AttributeOrientedPanel */
 	private VizMapperMainPanel() {
 		vmm = Cytoscape.getVisualMappingManager();
 		vmm.addChangeListener(this);
 
 		propertyMap = new HashMap<String, List<Property>>();
 		setMenu();
 		menu.addPopupMenuListener(this);
 
 		// Need to register listener here, instead of CytoscapeDesktop.
 		Cytoscape.getSwingPropertyChangeSupport().addPropertyChangeListener(this);
 
 		initComponents();
 		registerCellEditorListeners();
 		
 		// By default, force to sort property by prop name.
 		visualPropertySheetPanel.setSorting(true);
 
 		Cytoscape.getNodeAttributes().getMultiHashMapDefinition().addDataDefinitionListener(this);
 		Cytoscape.getEdgeAttributes().getMultiHashMapDefinition().addDataDefinitionListener(this);
 	}
 
 	/*
 	 * Register listeners for editors.
 	 */
 	private void registerCellEditorListeners() {
 		nodeAttrEditor.addPropertyChangeListener(this);
 		edgeAttrEditor.addPropertyChangeListener(this);
 
 		mappingTypeEditor.addPropertyChangeListener(this);
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
 	 * will be used to show/hide props based on a visualpropertydependency and
 	 * a specified VisualPropertyDependency.Definition.
 	 *
 	 * @param deps The VisualPropertyDependencies that will be queried.
 	 * @param def The VisualPropertyDependency.Definition in question.
 	 */
 	void syncDependencyStates(VisualPropertyDependency deps, Definition def) {
 		dependencyMenuItems.get(def).setSelected(deps.check(def));
 
 		updateDependencyStates(deps);
 
 		visualPropertySheetPanel.repaint();
 
 		final String vsName = vmm.getVisualStyle().getName();
 		updateDefaultImage(vsName, (DGraphView) ((DefaultViewPanel) DefaultAppearenceBuilder.getDefaultView(vsName)).getView(), defaultAppearencePanel.getSize());
 		setDefaultPanel(defaultImageManager.get(vsName),true);
 	}
 
 	
 	private void updateDependencyStates(VisualPropertyDependency deps) {
 		for (Property tmpprop : visualPropertySheetPanel.getProperties()) {
 			if ( !(tmpprop instanceof VizMapperProperty) )
 				continue;
 			updateDependentProperty( (VizMapperProperty)tmpprop, deps );
 		}
 
 		// to avoid concurrent modification exception
 		ArrayList<VizMapperProperty> propl = new ArrayList<VizMapperProperty>( hiddenProperties );	
 
 		for ( VizMapperProperty prop : propl )
 			updateDependentProperty( prop, deps );
 			
 	}
 
 	private void updateDependentProperty(VizMapperProperty prop, VisualPropertyDependency deps) {
 		Object hidden = prop.getHiddenObject();
 		if ( !(hidden instanceof VisualPropertyType) )
 				return;
 
 		VisualPropertyType type = (VisualPropertyType)hidden; 
 		if ( type.getVisualProperty().constrained(deps) ) {
 			hiddenProperties.add( prop );
 			visualPropertySheetPanel.removeProperty(prop);
 		} else {
 			if ( hiddenProperties.remove( prop ) )
 				visualPropertySheetPanel.addProperty(prop);
 		}
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
 
 		dependencies = new JMenu("Visual Property Dependencies");
 		dependencyMenuItems = new HashMap<Definition,JCheckBoxMenuItem>();
 		setupDependencyMenus( dependencies );
 
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
 		menu.add(dependencies);
 
 		delete.setEnabled(false);
 	}
 
 	
 	private void setupDependencyMenus(JMenu parentMenu) {
 		for ( final Definition def : Definition.values() ) {
 			final JCheckBoxMenuItem item = new JCheckBoxMenuItem(def.getTitle());
 			item.setSelected(def.getDefault());
 			item.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					final VisualPropertyDependency deps = vmm.getVisualStyle().getDependency();
 					deps.set(def,item.isSelected());
 					syncDependencyStates(deps,def);
 
 					Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
 				}
 			});
 
 			parentMenu.add(item);
 			dependencyMenuItems.put( def, item );
 		}
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
 		defaultAppearencePanel.setLayout(new BorderLayout());
 
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
 	}
 
 	// Variables declaration - do not modify
 	private JPanel defaultAppearencePanel;
 	private javax.swing.JSplitPane mainSplitPane;
 	private javax.swing.JSplitPane listSplitPane;
 	private DropDownMenuButton optionButton;
 	private PropertySheetPanel visualPropertySheetPanel;
 	private javax.swing.JComboBox vsNameComboBox;
 	private javax.swing.JPanel vsSelectPanel;
 
 	/*
 	 * Controlling attr selector
 	 */
 	private CyComboBoxPropertyEditor nodeAttrEditor = new CyComboBoxPropertyEditor();
 	private CyComboBoxPropertyEditor edgeAttrEditor = new CyComboBoxPropertyEditor();
 	
 	// 
 	private CyComboBoxPropertyEditor nodeNumericalAttrEditor = new CyComboBoxPropertyEditor();
 	private CyComboBoxPropertyEditor edgeNumericalAttrEditor = new CyComboBoxPropertyEditor();
 
 	// For mapping types.
 	private CyComboBoxPropertyEditor mappingTypeEditor = new CyComboBoxPropertyEditor();
 	
 	private PropertyRendererRegistry rendReg = new PropertyRendererRegistry();
 	private PropertyEditorRegistry editorReg = new PropertyEditorRegistry();
 	
 	private final CellRendererFactory rendFactory = new CellRendererFactory();
 	private final CellEditorFactory editorFactory = new CellEditorFactory(this);
 	private final DiscretePropertySetter discretePropSetter = new DiscretePropertySetter(this, rendReg, editorReg, rendFactory, editorFactory);
 
 	// End of variables declaration
 	private void vsNameComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
 		final String vsName = (String) vsNameComboBox.getSelectedItem();
 		final CyNetworkView currentView = Cytoscape.getCurrentNetworkView();
 		
 		if (vsName != null) {
 			if (currentView.equals(Cytoscape.getNullNetworkView()) || 
 					vsName.equals(lastVSName) || currentView.getVisualStyle().getName().equals(vsName)) {
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
 		if (ignore)
 			return;
 
 		// If new VS name is the same, ignore.
 		if (lastVSName == vsName)
 			return;
 
 		closeEditorWindow();
 
 		logger.debug("VS Switched --> " + vsName + ", Last = " + lastVSName);
 		vmm.setNetworkView(Cytoscape.getCurrentNetworkView());
 		vmm.setVisualStyle(vsName);
 
 		if (propertyMap.containsKey(vsName)) {
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
 		} else
 			setPropertyTable();
 		
 		final List<CyNetwork> selected = Cytoscape.getSelectedNetworks();
 		final Set<CyNetworkView> views = new HashSet<CyNetworkView>();
 		
 		// Find selected views
 		for (final CyNetwork network: selected) {
 			final CyNetworkView targetView = Cytoscape.getNetworkView(network.getIdentifier());
 			if (targetView != Cytoscape.getNullNetworkView())
 				views.add(targetView);
 		}
 		
 		// Apply it for selected views.
 		for (CyNetworkView view: views) {
 			view.setVisualStyle(vsName);
 			view.redrawGraph(false, true);
 			Cytoscape.getVisualMappingManager().setNetworkView(view);
 		}
 
 		/*
 		 * Draw default view
 		 */
 		Image defImg = defaultImageManager.get(vsName);
 
 		if(defImg == null) {
 			// Default image is not available in the buffer.  Create a new one.
 			updateDefaultImage(vsName, (DGraphView) ((DefaultViewPanel) DefaultAppearenceBuilder.getDefaultView(vsName)).getView(), defaultAppearencePanel.getSize());
 			defImg = defaultImageManager.get(vsName);
 		}
 		// Set the default view to the panel.
 		setDefaultPanel(defImg,false);
 
 		// Sync. locks
 		VisualPropertyDependency dep = vmm.getVisualStyle().getDependency();
 		for ( Definition d : Definition.values() ) {
 			JCheckBoxMenuItem jcbmi = dependencyMenuItems.get( d );
 			jcbmi.setSelected( dep.check(d) );
 			syncDependencyStates(dep,d);
 		}
 		
 		visualPropertySheetPanel.setSorting(true);
 		
 		// Cleanup desktop.
 		Cytoscape.getDesktop().repaint();
 		vsNameComboBox.setSelectedItem(vsName);
 		
 		// Update last visual style
 		lastVSName = vsName;
 	}
 
 	private static final String CATEGORY_UNUSED = "Unused Properties";
 	private static final String GRAPHICAL_MAP_VIEW = "Graphical View";
 	private static final String NODE_VISUAL_MAPPING = "Node Visual Mapping";
 	private static final String EDGE_VISUAL_MAPPING = "Edge Visual Mapping";
 
 	/*
 	 * Set Visual Style selector combo box.
 	 */
 	public void initVizmapperGUI() {
 		List<String> vsNames = new ArrayList<String>(vmm.getCalculatorCatalog().getVisualStyleNames());
 
 		final VisualStyle style = vmm.getVisualStyle();
 
 		// Disable action listeners
 		final ActionListener[] li = vsNameComboBox.getActionListeners();
 
 		for (int i = 0; i < li.length; i++)
 			vsNameComboBox.removeActionListener(li[i]);
 
 		vsNameComboBox.removeAllItems();
 
 		JPanel defPanel;
 
 		final Dimension panelSize = defaultAppearencePanel.getSize();
 		DGraphView view;
 
 		Collections.sort(vsNames);
 
 		for (String name : vsNames) {
 			vsNameComboBox.addItem(name);
 			// MLC 03/31/08:
 			// Deceptively, getDefaultView actually actually calls VisualMappingManager.setVisualStyle()
 			// so each time we add a combobox item, the visual style is changing.
 			// Make sure to set the lastVSName as we change the visual style:
 			defPanel = DefaultAppearenceBuilder.getDefaultView(name);
 			view = (DGraphView) ((DefaultViewPanel) defPanel).getView();
 
 			if (view != null) {
 				logger.debug("Creating Default Image for " + name);
 				updateDefaultImage(name, view, panelSize);
 			}
 		}
 
 		vmm.setNetworkView(Cytoscape.getCurrentNetworkView());
 
 		// Switch back to the original style.
 		switchVS(style.getName(), false);
 		
 		// Sync check box and actual lock state
 		updateDependencyStates(vmm.getVisualStyle().getDependency());
 
 		// Restore listeners
 		for (int i = 0; i < li.length; i++)
 			vsNameComboBox.addActionListener(li[i]);
 	}
 
 	/**
 	 * Create image of a default dummy network and save in a Map object.
 	 *
 	 * @param vsName
 	 * @param view
 	 * @param size
 	 */
 	private void updateDefaultImage(String vsName, DGraphView view, Dimension size) {
 		Image image = defaultImageManager.remove(vsName);
 
 		if (image != null) {
 			image.flush();
 			image = null;
 		}
 
 		defaultImageManager.put(vsName, view.createImage((int) size.getWidth(), (int) size.getHeight(), 0.9));
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
 				}
 
 				public void columnMarginChanged(ChangeEvent e) {
 					updateTableView();
 				}
 
 				public void columnMoved(TableColumnModelEvent e) {
 				}
 
 				public void columnRemoved(TableColumnModelEvent e) {
 				}
 
 				public void columnSelectionChanged(ListSelectionEvent e) {
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
 		table.setRowHeight(ROW_HEIGHT);
 		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 		table.setCategoryBackground(new Color(10, 10, 50, 20));
 		table.setCategoryForeground(Color.black);
 		table.setSelectionBackground(Color.white);
 		table.setSelectionForeground(Color.blue);
 
 
 		final Set<String> mappingTypes = Cytoscape.getVisualMappingManager().getCalculatorCatalog()
 		                                  .getMappingNames();
 
 		mappingTypeEditor.setAvailableValues(mappingTypes.toArray());
 		
 	}
 
 	
 	
 	/**
 	 * Refresh the appearence of the table.
 	 * This includes adjusting row height.
 	 * 
 	 */
 	private void updateTableView() {
 		final PropertySheetTable table = visualPropertySheetPanel.getTable();
 		
 		Property shownProp = null;
 		final DefaultTableCellRenderer emptyCellRenderer = new DefaultTableCellRenderer();
 
 		// Number of rows shown now.
 		final int rowCount = table.getRowCount();
 
 		for (int i = 0; i < rowCount; i++) {
 			try {
 				shownProp = ((Item) table.getValueAt(i, 0)).getProperty();
 			} catch (IndexOutOfBoundsException ex) {
 				shownProp = null;
 			} catch (NullPointerException ex) {
 				shownProp = null;
 			}
 
 
 			if ((shownProp != null) && (shownProp.getParentProperty() != null) && !shownProp.getDisplayName().equals(GRAPHICAL_MAP_VIEW)) {
 				final String parentText = shownProp.getParentProperty().getDisplayName();
 				final String displayName = shownProp.getDisplayName();
 				if(displayName.equals("Mapping Type") == false) {
 					// This is graphics icon cell. Need larger cell.
 					if(parentText.contains("Position"))
 						table.setRowHeight(i, ROW_HEIGHT_POSITION);
 					else if(shownProp.getParentProperty().getDisplayName()
 		                .startsWith("Node Custom Graphics"))
 						table.setRowHeight(i, ROW_HEIGHT_GRAPHICS);
 				}
 					
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
 						table.setRowHeight(i, ROW_HEIGHT_GRAPHICS);
 
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
 							cRenderer.setIcon(icon);
 							rendReg.registerRenderer(shownProp, cRenderer);
 						} else {
 							final DefaultTableCellRenderer dRenderer = new DefaultTableCellRenderer();
 							dRenderer.setIcon(icon);
 							rendReg.registerRenderer(shownProp, dRenderer);
 						}
 					}
 				}
 			} else if ((shownProp != null) && (shownProp.getCategory() != null)
 			           && shownProp.getCategory().equals(CATEGORY_UNUSED)) {
 				emptyCellRenderer.setForeground(UNUSED_COLOR);
 				rendReg.registerRenderer(shownProp, emptyCellRenderer);
 			}
 		}
 
 //		repaint();
 //		visualPropertySheetPanel.repaint();
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
 
 		repaint();
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
 				
 				VisualPropertyType type = (VisualPropertyType) ((VizMapperProperty) curProp).getHiddenObject();
 				visualPropertySheetPanel.removeProperty(curProp);
 
 				final VizMapperProperty newProp = new VizMapperProperty();
 				final VizMapperProperty mapProp = new VizMapperProperty();
 
 				newProp.setDisplayName(type.getName());
 				newProp.setHiddenObject(type);
 				newProp.setValue("Please select a value!");
 
 				if (type.isNodeProp()) {
 					newProp.setCategory(NODE_VISUAL_MAPPING);
 					editorReg.registerEditor(newProp, nodeAttrEditor);
 				} else {
 					newProp.setCategory(EDGE_VISUAL_MAPPING);
 					editorReg.registerEditor(newProp, edgeAttrEditor);
 				}
 
 				mapProp.setDisplayName("Mapping Type");
 				mapProp.setValue("Please select a mapping type!");
 				
 				newProp.addSubProperty(mapProp);
 				mapProp.setParentProperty(newProp);
 				visualPropertySheetPanel.addProperty(0, newProp);
 
 				expandLastSelectedItem(type.getName());
 
 				visualPropertySheetPanel.getTable().scrollRectToVisible(new Rectangle(0, 0, 10, 10));
 
 				editorReg.registerEditor(mapProp, mappingTypeEditor);
 			
 				// This is necessary because sometimes registory is lost when updating this sheet.
 				visualPropertySheetPanel.setEditorFactory(editorReg);
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
 							logger.warn("Unable to add listener to the contiuous editor", e1);
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
 
 			if (attrName == null)
 				calculatorTypeProp.setValue("Select Value");
 			else
 				calculatorTypeProp.setValue(attrName);
 
 			rendReg.registerRenderer(calculatorTypeProp, CellRendererFactory.DEF_RENDERER);
 			
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
 			boolean isNode = false;
 
 			if (calc.getVisualPropertyType().isNodeProp()) {
 				attr = Cytoscape.getNodeAttributes();
 				it = Cytoscape.getCurrentNetwork().nodesIterator();
 				editorReg.registerEditor(calculatorTypeProp, nodeAttrEditor);
 				isNode = true;
 			} else {
 				attr = Cytoscape.getEdgeAttributes();
 				it = Cytoscape.getCurrentNetwork().edgesIterator();
 				editorReg.registerEditor(calculatorTypeProp, edgeAttrEditor);
 				isNode = false;
 			}
 
 			/*
 			 * Discrete Mapping
 			 */
 			if ((firstMap.getClass() == DiscreteMapping.class) && (attrName != null)) {
 				final Map discMapping = ((DiscreteMapping) firstMap).getAll();
 				final Set<Object> attrSet = MappingKeyFactory.getKeySet(attrName, attr, firstMap, isNode);
 				discretePropSetter.setDiscreteProps(type, discMapping, attrSet, calculatorTypeProp);
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
 //					/*
 //					 * Size/Width related calcs.
 //					 */
 //					continuousRenderer.setIcon(icon);
 //					rendReg.registerRenderer(graphicalView, continuousRenderer);
 				} else {
 //					discreteRenderer.setIcon(icon);
 //					rendReg.registerRenderer(graphicalView, discreteRenderer);
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
 
 	
 	private void setDefaultPanel(final Image defImage, boolean repaint) {
 		if (defImage == null)
 			return;
 
 		defaultAppearencePanel.removeAll();
 
 		final JButton defaultImageButton = new JButton();
 		defaultImageButton.setUI(new BlueishButtonUI());
 		defaultImageButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 
 		defaultImageButton.setIcon(new ImageIcon(defImage));
 		defaultAppearencePanel.add(defaultImageButton, BorderLayout.CENTER);
 		defaultImageButton.addMouseListener(new DefaultMouseListener());
 		if ( repaint )
 			Cytoscape.getDesktop().repaint();
 	}
 
 	/**
 	 * Action listener for the default image button.
 	 * 
 	 * @author kono
 	 *
 	 */
 	class DefaultMouseListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent e) {
 			if (SwingUtilities.isLeftMouseButton(e)) {
 				final String targetName = vmm.getVisualStyle().getName();
 				final DefaultViewPanel panel = (DefaultViewPanel) DefaultAppearenceBuilder.showDialog(Cytoscape .getDesktop());
 				updateDefaultImage(targetName, (DGraphView) panel.getView(),
 				                   defaultAppearencePanel.getSize());
 				setDefaultPanel(defaultImageManager.get(targetName), false);
 
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
 			Cytoscape.getVisualMappingManager().addChangeListener(this);
 			syncStyleBox();
 			ignore = false;
 		} else {
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
 
 	private void manageWindow(final String status, VisualPropertyType vpt, Object source) {
 		if (status.equals(ContinuousMappingEditorPanel.EDITOR_WINDOW_OPENED)) {
 			this.editorWindowManager.put(vpt, (JDialog) source);
 		} else if (status.equals(ContinuousMappingEditorPanel.EDITOR_WINDOW_CLOSED)) {
 			final VisualPropertyType type = vpt;
 
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
 		}
 	}
 
 	private void closeEditorWindow() {
 		Set<VisualPropertyType> typeSet = editorWindowManager.keySet();
 		Set<VisualPropertyType> keySet = new HashSet<VisualPropertyType>();
 
 		for (VisualPropertyType vpt : typeSet) {
 			JDialog window = editorWindowManager.get(vpt);
 			manageWindow(ContinuousMappingEditorPanel.EDITOR_WINDOW_CLOSED, vpt, null);
 			window.dispose();
 			keySet.add(vpt);
 		}
 
 		for (VisualPropertyType type : keySet)
 			editorWindowManager.remove(type);
 	}
 
 	/**
 	 * Handle propeaty change events.
 	 *
 	 * @param e
 	 *            DOCUMENT ME!
 	 */
 	public void propertyChange(PropertyChangeEvent e) {
 		// Set ignore flag.
 		if (e.getPropertyName().equals(Integer.toString(Cytoscape.SESSION_OPENED))) {
 			ignore = true;
 			enableListeners(false);
 		}
 
 		if (ignore)
 			return;
 
 		/*
 		 * Managing editor windows.
 		 */
 		if (e.getPropertyName().equals(ContinuousMappingEditorPanel.EDITOR_WINDOW_OPENED)
 		    || e.getPropertyName().equals(ContinuousMappingEditorPanel.EDITOR_WINDOW_CLOSED)) {
 			manageWindow(e.getPropertyName(), (VisualPropertyType) e.getNewValue(), e.getSource());
 
 			if (e.getPropertyName().equals(ContinuousMappingEditorPanel.EDITOR_WINDOW_CLOSED))
 				editorWindowManager.remove((VisualPropertyType) e.getNewValue());
 
 			return;
 		}
 
 		/*
 		 * Got global event
 		 */
 		if (e.getPropertyName().equals(Cytoscape.SESSION_LOADED)
 		           || e.getPropertyName().equals(Cytoscape.VIZMAP_LOADED)) {
 			final String vsName = vmm.getVisualStyle().getName();
 
 			lastVSName = null;
 			initVizmapperGUI();
 			switchVS(vsName, false);
 			vsNameComboBox.setSelectedItem(vsName);
 			vmm.setVisualStyle(vsName);
 
 			return;
 		} else if (e.getPropertyName().equals(CytoscapeDesktop.NETWORK_VIEW_FOCUS)
 		           && (e.getSource().getClass() == NetworkPanel.class)) {
 			final VisualStyle vs = vmm.getNetworkView().getVisualStyle();
 
 			if (vs != null) {
 				vmm.setNetworkView(Cytoscape.getCurrentNetworkView());
 
 				if (vs.getName().equals(vsNameComboBox.getSelectedItem())) {
 					//TODO: is this necessary?
 					//Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
 				} else {
 					switchVS(vs.getName(), false);
 					vsNameComboBox.setSelectedItem(vs.getName());
 					setDefaultPanel(this.defaultImageManager.get(vs.getName()),false);
 				}
 			}
 
 			return;
 		}
 
 		/***********************************************************************
 		 * Below this line, accept only cell editor events.
 		 **********************************************************************/
 		if (e.getPropertyName().equalsIgnoreCase("value") == false)
 			return;
 
 		if (e.getNewValue() != null && e.getNewValue().equals(e.getOldValue()))
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
 
 		VisualPropertyType type = null;
 		String ctrAttrName = null;
 
 		VizMapperProperty typeRootProp = null;
 
 		if ((prop.getParentProperty() == null) && e.getNewValue() instanceof String) {
 			/*
 			 * This is a controlling attr name change signal.
 			 */
 			typeRootProp = (VizMapperProperty) prop;
 			type = (VisualPropertyType) ((VizMapperProperty) prop).getHiddenObject();
 			ctrAttrName = (String) e.getNewValue();
 		} else if ((prop.getParentProperty() == null) && (e.getNewValue() == null)) {
 			/*
 			 * Empty cell selected. no need to change anything.
 			 */
 			return;
 		} else {
 			typeRootProp = (VizMapperProperty) prop.getParentProperty();
 
 			if (prop.getParentProperty() == null)
 				return;
 
 			type = (VisualPropertyType) ((VizMapperProperty) prop.getParentProperty())
 			                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             .getHiddenObject();
 		}
 
 		/*
 		 * Mapping type changed
 		 */
 		if (prop.getHiddenObject() instanceof ObjectMapping
 		    || prop.getDisplayName().equals("Mapping Type")) {
 			logger.debug("Mapping type changed: " + prop.getHiddenObject());
 
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
 					
 					SwingUtilities.invokeLater(new Runnable() {
 						public void run() {
 							JOptionPane.showMessageDialog(Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST).getSelectedComponent(),
 		                              "Continuous Mapper can be used with Numbers only.",
 		                              "Incompatible Mapping Type!",
 		                              JOptionPane.ERROR_MESSAGE);
 						}
 					});
 					
 					return;
 				}
 			} else {
 				return;
 			}
 
 			if (e.getNewValue().toString().endsWith("Mapper") == false)
 				return;
 
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
 
 			// Buffer current discrete mapping
 			if (mapping instanceof DiscreteMapping) {
 				final String curMappingName = curCalc.toString() + "-"
 				                              + mapping.getControllingAttributeName();
 				final String newMappingName = curCalc.toString() + "-" + ctrAttrName;
 				final Map saved = discMapBuffer.get(newMappingName);
 
 				if (saved == null) {
 					discMapBuffer.put(curMappingName, ((DiscreteMapping) mapping).getAll());
 					mapping.setControllingAttributeName(ctrAttrName);
 				} else if (saved != null) {
 					// Mapping exists
 					discMapBuffer.put(curMappingName, ((DiscreteMapping) mapping).getAll());
 					mapping.setControllingAttributeName(ctrAttrName);
 					((DiscreteMapping) mapping).putAll(saved);
 				}
 			} else {
 				mapping.setControllingAttributeName(ctrAttrName);
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
 
 			if (propertyMap.get(vmm.getVisualStyle().getName()) != null)
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
 
 			if (type.getDataType() == Number.class)
 				editorReg.registerEditor(prop,  this.editorFactory.getPropertyEditor(Number.class));
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
 		if(newValue == null) {
 			((DiscreteMapping) mapping).putMapValue(key, newValue);
 			Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
 			return;
 		}
 		if (type.getDataType() == Number.class && newValue instanceof Number) {
 			// Validate Discrete Mapping Value
 			
 			if (type.getVisualProperty().isValidValue(newValue) == false) {
 				
 				// Out of range.  Use current value.
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
 
 		final VisualPropertyType type = (VisualPropertyType) ((VizMapperProperty) prop.getParentProperty()).getHiddenObject();
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
 			// Use task since this may take a white if it's a new image passthrough mapping.
 			NewMappingBuilder.createNewCalculator(type, newMapName, newCalcName, attrName.toString());
 			newCalc = vmm.getCalculatorCatalog().getCalculator(type, newCalcName);
 		}
 
 		newCalc.getMapping(0).setControllingAttributeName((String) attrName);
 
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
 
 			if (vmm.getCalculatorCatalog().getCalculator(type, oldCalcName) == null)
 				NewMappingBuilder.createNewCalculator(type, oldMappingTypeName, oldCalcName, attrName.toString());
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
 
 		if (propertyMap.get(vmm.getVisualStyle().getName()) != null) {
 			propertyMap.get(vmm.getVisualStyle().getName()).add(newRootProp);
 		}
 		
 		// This redraw may take a while, so run as a task.
 		redraw(Cytoscape.getCurrentNetworkView());
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
				if (item.isVisible() == false)
 					item.toggle();
 
 				return;
 			}
 		}
 	}
 
 	
 	private void redraw(final CyNetworkView view) {
 		// Create Task
 		RedrawTask task = new RedrawTask(view);
 		// Configure JTask Dialog Pop-Up Box
 		final JTaskConfig jTaskConfig = new JTaskConfig();
 
 		jTaskConfig.displayCancelButton(false);
 		jTaskConfig.setOwner(Cytoscape.getDesktop());
 		jTaskConfig.displayCloseButton(false);
 		jTaskConfig.displayStatus(true);
 		jTaskConfig.setAutoDispose(true);
 
 		// Execute Task in New Thread; pop open JTask Dialog Box.
 		TaskManager.executeTask(task, jTaskConfig);
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
 		
 		private static final long serialVersionUID = -401235126133833279L;
 
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
 				logger.debug("Creating Default Image for new visual style " + name);
 				updateDefaultImage(name, view, panelSize);
 				setDefaultPanel(defaultImageManager.get(name),false);
 			}
 
 			vmm.setNetworkView(Cytoscape.getCurrentNetworkView());
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
 
 			if (ret.indexOf('.') != -1) {
 				JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
 							      "Visual style names with dots are not allowed, please select a new name.",
 							      "Information",
 							      JOptionPane.INFORMATION_MESSAGE);
 				continue;
 			}
 
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
 				logger.warn("Clone not supported exception!");
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
 				logger.debug("Creating Default Image for new visual style " + newName);
 				updateDefaultImage(newName, view, panelSize);
 				setDefaultPanel(defaultImageManager.get(newName),false);
 			}
 
 			vmm.setNetworkView(Cytoscape.getCurrentNetworkView());
 			switchVS(newName);
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
 				final VisualPropertyType type = (VisualPropertyType) ((VizMapperProperty) curProp)
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
 					// If Continuous Mapper is displayed, kill it.
 					if (editorWindowManager.get(type) != null) {
 						JDialog editor = editorWindowManager.get(type);
 						editor.dispose();
 						editorWindowManager.remove(type);
 					}
 
 					if (type.isNodeProp()) {
 						vmm.getVisualStyle().getNodeAppearanceCalculator().removeCalculator(type);
 					} else {
 						vmm.getVisualStyle().getEdgeAppearanceCalculator().removeCalculator(type);
 					}
 
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
 
 		if (propertyMap.get(vmm.getVisualStyle().getName()) != null) {
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
 			logger.warn("Unable to show the descrete editor",e1);
 		}
 
 		if (newValue == null)
 			return;
 
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
 			logger.debug("Removed: " + p.getDisplayName());
 			propertyMap.get(vmm.getVisualStyle().getName()).remove(p);
 		}
 	}
 
 	private class GenerateValueListener extends AbstractAction {
 	
 		private static final long serialVersionUID = -4852790777403019117L;
 
 		private static final int MAX_COLOR = 256 * 256 * 256;
 		
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
 			//Check Selected poperty
 			final int selectedRow = visualPropertySheetPanel.getTable().getSelectedRow();
 
 			if (selectedRow < 0)
 				return;
 
 			final Item item = (Item) visualPropertySheetPanel.getTable().getValueAt(selectedRow, 0);
 			final VizMapperProperty prop = (VizMapperProperty) item.getProperty();
 			final Object hidden = prop.getHiddenObject();
 
 			if (hidden instanceof VisualPropertyType) {
 				final VisualPropertyType type = (VisualPropertyType) hidden;
 
 				final Map valueMap = new HashMap();
 				final long seed = System.currentTimeMillis();
 				final Random rand = new Random(seed);
 
 				final ObjectMapping oMap;
 
 				final CyAttributes attr;
 				final boolean isNode;
 
 				if (type.isNodeProp()) {
 					attr = Cytoscape.getNodeAttributes();
 					oMap = vmm.getVisualStyle().getNodeAppearanceCalculator().getCalculator(type)
 					          .getMapping(0);
 					isNode = true;
 				} else {
 					attr = Cytoscape.getEdgeAttributes();
 					oMap = vmm.getVisualStyle().getEdgeAppearanceCalculator().getCalculator(type)
 					          .getMapping(0);
 					isNode = false;
 				}
 
 				// This function is for discrete mapping only.
 				if ((oMap instanceof DiscreteMapping) == false)
 					return;
 
 				dm = (DiscreteMapping) oMap;
 
 				final Set<Object> attrSet = MappingKeyFactory.getKeySet(oMap.getControllingAttributeName(), attr,
 				                                     oMap, isNode);
 
 				// Show error if there is no attribute value.
 				if (attrSet.size() == 0) {
 					JOptionPane.showMessageDialog(panel, "No attribute value is available.",
 					                              "Cannot generate values",
 					                              JOptionPane.ERROR_MESSAGE);
 				}
 
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
 				logger.info("Invalid.");
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
 				final boolean isNode;
 
 				if (type.isNodeProp()) {
 					attr = Cytoscape.getNodeAttributes();
 					oMap = vmm.getVisualStyle().getNodeAppearanceCalculator().getCalculator(type)
 					          .getMapping(0);
 					isNode = true;
 				} else {
 					attr = Cytoscape.getEdgeAttributes();
 					oMap = vmm.getVisualStyle().getEdgeAppearanceCalculator().getCalculator(type)
 					          .getMapping(0);
 					isNode = false;
 				}
 
 				if ((oMap instanceof DiscreteMapping) == false)
 					return;
 
 				dm = (DiscreteMapping) oMap;
 
 				final Set<Object> attrSet = MappingKeyFactory.getKeySet(oMap.getControllingAttributeName(), attr,
 				                                     oMap, isNode);
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
 					JOptionPane.showMessageDialog(visualPropertySheetPanel,
 					                              "Start value and increment must be numeric values!",
 					                              "Non-numeric input error", JOptionPane.ERROR_MESSAGE);
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
 				logger.info("Invalid.");
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
 				dm.setControllingAttributeName(ctrAttrName);
 
 				// final Set<Object> attrSet =
 				// loadKeys(oMap.getControllingAttributeName(), attr, oMap);
 				if (vmm.getVisualStyle().getDependency().check(NODE_SIZE_LOCKED)) {
 					return;
 				}
 
 				DiscreteMapping wm = null;
 
 				if ((type == NODE_WIDTH)) {
 					wm = (DiscreteMapping) vmm.getVisualStyle().getNodeAppearanceCalculator()
 					                          .getCalculator(NODE_WIDTH).getMapping(0);
 
 					wm.setControllingAttributeName(ctrAttrName);
 
 					Set<Object> attrSet1;
 
 					if (ctrAttrName.equals("ID")) {
 						attrSet1 = new TreeSet<Object>();
 
 						for (Object node : Cytoscape.getCurrentNetwork().nodesList()) {
 							attrSet1.add(((Node) node).getIdentifier());
 						}
 					} else {
 						attrSet1 = MappingKeyFactory.getKeySet(wm.getControllingAttributeName(), attr, wm,
 						                    true);
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
 						wm.setControllingAttributeName("ID");
 
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
 
 					wm.setControllingAttributeName(ctrAttrName);
 
 					Set<Object> attrSet1;
 
 					if (ctrAttrName.equals("ID")) {
 						attrSet1 = new TreeSet<Object>();
 
 						for (Object node : Cytoscape.getCurrentNetwork().nodesList()) {
 							attrSet1.add(((Node) node).getIdentifier());
 						}
 					} else {
 						attrSet1 = MappingKeyFactory.getKeySet(wm.getControllingAttributeName(), attr, wm,
 						                    true);
 					}
 
 					Integer fontSize = ((Number) vmm.getVisualStyle().getNodeAppearanceCalculator()
 					                                .getDefaultAppearance().get(NODE_FONT_SIZE))
 					                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           .intValue();
 					int strLen;
 
 					String labelString = null;
 					String[] listObj;
 
 					if (attr.getType(ctrAttrName) == CyAttributes.TYPE_SIMPLE_LIST) {
 						wm.setControllingAttributeName("ID");
 
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
 				logger.info("Invalid.");
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
 				final boolean nOre;
 
 				if (type.isNodeProp()) {
 					attr = Cytoscape.getNodeAttributes();
 					oMap = vmm.getVisualStyle().getNodeAppearanceCalculator().getCalculator(type)
 					          .getMapping(0);
 					nOre = true;
 				} else {
 					attr = Cytoscape.getEdgeAttributes();
 					oMap = vmm.getVisualStyle().getEdgeAppearanceCalculator().getCalculator(type)
 					          .getMapping(0);
 					nOre = false;
 				}
 
 				if ((oMap instanceof DiscreteMapping) == false) {
 					return;
 				}
 
 				dm = (DiscreteMapping) oMap;
 
 				final Set<Object> attrSet = MappingKeyFactory.getKeySet(oMap.getControllingAttributeName(), attr,
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
 				logger.info("Invalid.");
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
 		disableAllPopup();
 	}
 
 	private void disableAllPopup() {
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
 		disableAllPopup();
 
 		final int selected = visualPropertySheetPanel.getTable().getSelectedRow();
 
 		if (0 > selected) {
 			return;
 		}
 
 		final Item item = (Item) visualPropertySheetPanel.getTable().getValueAt(selected, 0);
 		final Property curProp = item.getProperty();
 
 		if (curProp == null)
 			return;
 
 		VizMapperProperty prop = ((VizMapperProperty) curProp);
 
 		if (prop.getHiddenObject() instanceof VisualPropertyType
 		    && (prop.getDisplayName().contains("Mapping Type") == false)
 		    && (prop.getValue() != null)
 		    && (prop.getValue().toString().startsWith("Please select") == false)) {
 			// Enble delete menu
 			delete.setEnabled(true);
 
 			Property[] children = prop.getSubProperties();
 
 			for (Property p : children) {
 				if ((p.getDisplayName() != null) && p.getDisplayName().contains("Mapping Type")) {
 					if ((p.getValue() == null)
 					    || (p.getValue().equals("Discrete Mapping") == false)) {
 						return;
 					}
 				}
 			}
 
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
 	 * Listening to Visual Style states.
 	 * Will be called when Visual Style added/removed
 	 *
 	 * @param e
 	 *            DOCUMENT ME!
 	 */
 	public void stateChanged(ChangeEvent e) {
 		if (ignore)
 			return;
 
 		// Get current Visual Style
 		final String currentName = vmm.getVisualStyle().getName();
 		final Object selected = vsNameComboBox.getSelectedItem();
 		
 		if (selected != null && selected.toString().equals(currentName)) {
 			return;
 		}
 		
 		// Update GUI based on CalcCatalog's state.
 		if (!findVSName(currentName)) {
 			syncStyleBox();
 		} else
 			vsNameComboBox.setSelectedItem(currentName);
 		
 		lastVSName = currentName;
 	}
 
 	private void syncStyleBox() {
 
 		final String curStyleName = vmm.getVisualStyle().getName();
 		final CalculatorCatalog catalog = vmm.getCalculatorCatalog();
 		
 		String styleName;
 		final List<String> namesInBox = new ArrayList<String>();
 		namesInBox.addAll(catalog.getVisualStyleNames());
 		for (int i = 0; i < vsNameComboBox.getItemCount(); i++) {
 			styleName = vsNameComboBox.getItemAt(i).toString();
 
 			if (catalog.getVisualStyle(styleName) == null) {
 				// No longer exists in the VMM.  Remove.
 				vsNameComboBox.removeItem(styleName);
 				defaultImageManager.remove(styleName);
 				propertyMap.remove(styleName);
 			}
 		}
 
 		Collections.sort(namesInBox);
 
 		// Reset combobox items.
 		vsNameComboBox.removeAllItems();
 
 		for (final String name : namesInBox)
 			vsNameComboBox.addItem(name);
 
 		switchVS(curStyleName);
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
 
 	
 	public void attributeDefined(String attributeName) {
 		setAttrComboBox();
 	}
 
 	
 	public void attributeUndefined(String attributeName) {
 		setAttrComboBox();
 	}
 }
 
