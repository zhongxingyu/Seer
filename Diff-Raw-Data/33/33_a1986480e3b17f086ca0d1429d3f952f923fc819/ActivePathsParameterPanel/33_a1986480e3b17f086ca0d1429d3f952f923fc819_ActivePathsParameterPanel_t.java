 // ActivePathsParametersPopupDialog
 //-----------------------------------------------------------------------------
 // $Revision: 14177 $
 // $Date: 2008-06-10 15:16:57 -0700 (Tue, 10 Jun 2008) $
 // $Author: rmkelley $
 //-----------------------------------------------------------------------------
 package csplugins.jActiveModules.dialogs;
 
 //-----------------------------------------------------------------------------
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingConstants;
 import javax.swing.border.Border;
 import javax.swing.border.TitledBorder;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.DefaultTableCellRenderer;
 import org.jdesktop.layout.GroupLayout;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.swing.JTable;
 
 import csplugins.jActiveModules.ActiveModulesUI;
 import csplugins.jActiveModules.CyHelpBrokerImpl;
 import csplugins.jActiveModules.data.ActivePathFinderParameters;
 
 import org.cytoscape.application.CyApplicationManager;
 import org.cytoscape.application.swing.CySwingApplication;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.model.CyNetworkManager;
 import org.cytoscape.model.CyTable;
 //import cytoscape.view.CyHelpBroker;
 import org.cytoscape.util.swing.NetworkSelectorPanel;
 
 import java.awt.Component;
 import javax.swing.table.TableColumn;
 import javax.swing.JComboBox;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.table.TableCellEditor;
 import javax.swing.AbstractCellEditor;
 import javax.swing.table.TableCellEditor;
 import javax.swing.DefaultCellEditor;
 import java.util.Collection;
 import javax.swing.JOptionPane;
 import javax.swing.event.CellEditorListener;
 import javax.swing.event.ChangeEvent;
 import java.util.Comparator;
 import org.cytoscape.util.swing.ColumnResizer;
 //import cytoscape.util.swing.CyCollapsiblePanel;
 import org.cytoscape.model.CyColumn;
 import java.util.Iterator;
 import org.cytoscape.model.CyTableUtil;
 
 
 public class ActivePathsParameterPanel extends JPanel implements ItemListener {
 
 	private static final long serialVersionUID = -6759180275710507653L;
 
 	private static final Logger logger = LoggerFactory.getLogger(ActivePathsParameterPanel.class);
 	
 	
 	JTextField readout;
 
 	ActivePathFinderParameters apfParams;
 
 	static String NONRANDOM = "Non-Random Starting Graph";
 	static String ANNEAL = "Anneal";
 	static String SEARCH = "Search";
 	
 	JPanel tStartPanel;
 	JPanel tEndPanel;
 	JTextField startNum;
 	JTextField endNum;
 	JLabel tempLabelStart;
 	JLabel tempLabelEnd;
 
 	JPanel intervalPanel;
 	JTextField intervalNum;
 	JLabel intervalLabel;
 
 	JPanel overlapPanel;
 	JTextField overlapNum;
 	JLabel overlapLabel;
 
 	JPanel pathPanel;
 	JTextField pathNum;
 	JLabel pathLabel;
 
 	JPanel iterPanel;
 	JTextField iterNum;
 	JLabel iterLabel;
 
 	JPanel annealExtPanel;
 	JPanel hfcPanel;
 	JCheckBox quenchCheck;
 	JCheckBox edgesCheck;
 	JCheckBox hubBox;
 	JTextField hubNum;
 
 	// hub adjustment
 	JPanel hAdjPanel;
 	JCheckBox hubAdjustmentBox;
 	JTextField hubAdjustmentNum;
 
 	// monte carlo: on/off
 	JCheckBox mcBox;
 
 	// regional scoring: on/off
 	JCheckBox regionalBox;
 
 	// greedy search rather than annealing
 	JPanel searchPanel;
 	JTextField searchDepth;
 	JCheckBox searchFromNodesBox;
 	JCheckBox maxBox;
 	JTextField maxDepth;
 
 	JRadioButton annealButton;
 	JRadioButton searchButton;
 	JPanel annealSearchControlPanel;
 	JPanel annealSearchContentPanel;
 	JPanel annealContentPanel;
 	JPanel searchContentPanel;
 	JPanel currentContentPanel;
 
 	JPanel optionsPanel;
 	private JList exprAttrsList;
 
 	// Button Panel for command buttons.
 	private JButton findModulesButton;
 
 	private ActiveModulesUI pluginMainClass;
 	JDialog helpDialog;
 	
 	private NetworkSelectorPanel networkPanel;
 	
 	private final CySwingApplication cySwingApplicationServiceRef;
 	private CyHelpBrokerImpl cyHelpBroker = new CyHelpBrokerImpl();
 	
 	// -----------------------------------------------------------------------------
 	public ActivePathsParameterPanel(
 			ActivePathFinderParameters incomingApfParams, CySwingApplication cySwingApplicationServiceRef,
 			CyApplicationManager cyApplicationManager, CyNetworkManager cyNetworkManager,
 			NetworkSelectorPanel networkSelectorPanel){
 
 		this.cySwingApplicationServiceRef = cySwingApplicationServiceRef;
 		// Set global parameters
 		this.setLayout(new BorderLayout());
 		this.setMinimumSize(new Dimension(320, 420));
 		this.setPreferredSize(new Dimension(320, 420));
 
 		this.networkPanel = networkSelectorPanel;
 		this.networkPanel.getJCombobox().addItemListener(this);
		this.networkPanel.setBorder(BorderFactory.createTitledBorder("Target Network"));
 		
 		// uses copy constructor so that changes aren't committed if you dismiss.
 		apfParams = incomingApfParams;
 
 		if (apfParams == null)
 			throw new IllegalStateException(
 					"Parametr object is null.  Could not initialize jActiveModules plugin.");
 		
 		initComponents();
 
 		populateAttributeTable();
 		
 		createHelpDialog();
 		addButtonEventListeners();
 		
 		/////
 		final JPanel mainPanel = new JPanel(new BorderLayout());
		
 		readout = new JTextField(new String("seed: "
 				+ apfParams.getRandomSeed()));
 		RandomSeedTextListener readoutListener = new RandomSeedTextListener();
 		readout.addFocusListener(readoutListener);
 		
 		final Container extController = createExtsControllerPanel();
 		createSearchContentPanel();
 		createAnnealContentPanel();		
 		createAnnealSearchControlPanel();
 		
 		final JPanel subOptionsPanel = new JPanel(new BorderLayout());
 		subOptionsPanel.add(extController, BorderLayout.PAGE_START);
 		subOptionsPanel.add(annealSearchControlPanel, BorderLayout.CENTER);
 		
 		optionsPanel = new JPanel(new CardLayout());
 		optionsPanel.add(subOptionsPanel, "ACTIVE");
 		mainPanel.add(optionsPanel, BorderLayout.CENTER);
 
 		collapsiblePanel.getContentPane().add(mainPanel);
 
 		// Disable the "search" button at very beginning, enable it only when user make selection	
 		this.findModulesButton.setEnabled(false); 
 		this.tblAttrSelection.addMouseListener(new ExprAttrsTableMouseListener());
 
 		ColumnResizer.adjustColumnPreferredWidths(this.tblAttrSelection);
 	}
 	
 	//private AttrSelectionPanel attrSelectionPanel;
 	private CyCollapsiblePanel collapsiblePanel = new CyCollapsiblePanel("Advanced");;
 	
 	
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc=" Generated Code ">                          
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         jSplitPane1 = new javax.swing.JSplitPane();
         attrSelectionPanel = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         tblAttrSelection = new javax.swing.JTable();
         jScrollPane2 = new javax.swing.JScrollPane();
         buttonPanel = new javax.swing.JPanel();
         helpButton = new javax.swing.JButton();
         aboutButton = new javax.swing.JButton();
         dismissButton = new javax.swing.JButton();
         findModulesButton = new javax.swing.JButton();
 
         setLayout(new java.awt.GridBagLayout());
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         add(networkPanel, gridBagConstraints);
 
         jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
         jSplitPane1.setResizeWeight(0.2);
         attrSelectionPanel.setLayout(new java.awt.GridBagLayout());
 
         attrSelectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Numeric Node Attributes"));
         tblAttrSelection.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null},
                 {null, null},
                 {null, null},
                 {null, null}
             },
             new String [] {
                 "Title 1", "Title 2"
             }
         ));
         jScrollPane1.setViewportView(tblAttrSelection);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         attrSelectionPanel.add(jScrollPane1, gridBagConstraints);
 
         jSplitPane1.setLeftComponent(attrSelectionPanel);
 
         jScrollPane2.setViewportView(collapsiblePanel);
 
         jSplitPane1.setRightComponent(jScrollPane2);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         add(jSplitPane1, gridBagConstraints);
 
         buttonPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         helpButton.setText("Help");
         helpButton.setPreferredSize(new java.awt.Dimension(67, 23));
 	//cyHelpBroker.getHelpBroker().enableHelpOnButton(helpButton, "jActiveModules", null);
 
         buttonPanel.add(helpButton);
 
         aboutButton.setText("About");
         aboutButton.setPreferredSize(new java.awt.Dimension(67, 23));
         buttonPanel.add(aboutButton);
 
         dismissButton.setText("Close");
         dismissButton.setPreferredSize(new java.awt.Dimension(67, 23));
         buttonPanel.add(dismissButton);
 
         findModulesButton.setText("Search");
         buttonPanel.add(findModulesButton);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridy = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         add(buttonPanel, gridBagConstraints);
 
     }// </editor-fold>                        
 
     private javax.swing.JPanel buttonPanel;
     private javax.swing.JSplitPane jSplitPane1;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JPanel attrSelectionPanel;
     private javax.swing.JTable tblAttrSelection;
 	
 	private void populateAttributeTable() {
 
 		AttrSelectionTableModel tableModel = new AttrSelectionTableModel(getDataVect());
 		this.tblAttrSelection.setModel(tableModel);
 		
 		// Add a exclamation point if attribute is not p-value 
 		TableColumn nameColumn = this.tblAttrSelection.getColumn("Name");
 		nameColumn.setCellRenderer(new NameColumnCellRenderer());
 		
 		// give user the option to switch sig
 		TableColumn switchSigColumn = this.tblAttrSelection.getColumn("Reverse sig");
 		//switchSigColumn.setCellEditor(new CheckBoxCellEditor());
 		//switchSigColumn.setCellRenderer(new CheckBoxCellRenderer());
 		switchSigColumn.setCellRenderer(this.tblAttrSelection.getDefaultRenderer(Boolean.class));
 		switchSigColumn.setCellEditor(this.tblAttrSelection.getDefaultEditor(Boolean.class));
 
 		CheckBoxCellEditorListener checkBoxCellEditorListener = new CheckBoxCellEditorListener();
 		this.tblAttrSelection.getDefaultEditor(Boolean.class).addCellEditorListener(checkBoxCellEditorListener);
 		
 		// Let user select normalization method with comboBox
 		TableColumn normColumn = this.tblAttrSelection.getColumn("Scaling");
 		NormalizationCellRenderer normCellRender= new NormalizationCellRenderer();
 		normColumn.setCellRenderer(normCellRender);
 		//TableCellEditor editor = new DefaultCellEditor(normCellRender);
 		normColumn.setCellEditor(new NormalizationComboboxEditor());
 		
 		// Adjust the table size
 		Dimension tableSize = this.tblAttrSelection.getPreferredSize();
 		attrSelectionPanel.setPreferredSize(new Dimension(attrSelectionPanel.getWidth(), (tableSize.height + 50)));
 	}
 
 	
 	private class CheckBoxCellEditorListener implements CellEditorListener {
 		public void editingCanceled(ChangeEvent e) {}
 		
 		public void editingStopped(ChangeEvent e) 
 		{
 			int rowCount = ActivePathsParameterPanel.this.tblAttrSelection.getModel().getRowCount();
 			for (int i=0; i< rowCount; i++){
 				boolean switchSig = Boolean.valueOf(ActivePathsParameterPanel.this.tblAttrSelection.getModel().getValueAt(i, 3).toString());
 				double min = Double.valueOf(ActivePathsParameterPanel.this.tblAttrSelection.getModel().getValueAt(i, 1).toString());
 				double max = Double.valueOf(ActivePathsParameterPanel.this.tblAttrSelection.getModel().getValueAt(i, 2).toString());
 				if (switchSig && min<max) 
 				{
 					ActivePathsParameterPanel.this.tblAttrSelection.getModel().setValueAt(max, i, 1);
 					ActivePathsParameterPanel.this.tblAttrSelection.getModel().setValueAt(min, i, 2);
 				}
 				if (!switchSig && min > max) 
 				{
 					ActivePathsParameterPanel.this.tblAttrSelection.getModel().setValueAt(max, i, 1);
 					ActivePathsParameterPanel.this.tblAttrSelection.getModel().setValueAt(min, i, 2);
 				}
 			}
 		}
 	}
 	
 	
 	private class ExprAttrsTableMouseListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent e) {
 			if (ActivePathsParameterPanel.this.tblAttrSelection.getSelectedRowCount() > 0){
 				ActivePathsParameterPanel.this.findModulesButton.setEnabled(true);
 			}
 			else {
 				ActivePathsParameterPanel.this.findModulesButton.setEnabled(false);				
 			}
 		}
 	}
 	
 		
 	private class NormalizationComboboxEditor extends AbstractCellEditor implements TableCellEditor {
 
 		// This is the component that will handle the editing of the cell value
 		JComboBox component = new JComboBox();
 
 		NormalizationComboboxEditor() {
 			super();
 			
 			for (final ScalingMethodX method : ScalingMethodX.values())
 				component.addItem(method.getDisplayString());
 		}
 
 		// This method is called when a cell value is edited by the user.
 		public Component getTableCellEditorComponent(JTable table, Object value,
 							     boolean isSelected, int rowIndex, int vColIndex)
 		{
 			final AttrSelectionTableModel tableModel = (AttrSelectionTableModel)table.getModel();
 			final double val1 = (Double)tableModel.getValueAt(rowIndex, 1);
 			final double val2 = (Double)tableModel.getValueAt(rowIndex, 2);
 			final boolean isPossiblePValue = Math.min(val1, val2) >= 0.0 && Math.max(val1, val2) <= 1.0;
 
 			final String firstItem = (String)component.getItemAt(0);
 			final boolean firstItemIsNONE = firstItem.equals(ScalingMethodX.NONE.getDisplayString());
 
 			if (isPossiblePValue) {
 				if (!firstItemIsNONE)
 					component.insertItemAt(ScalingMethodX.NONE.getDisplayString(), 0);
 			}
 			else {
 				if (firstItemIsNONE)
 					component.removeItemAt(0);
 			}
 
 			// Configure the component with the specified value
 			component.setSelectedItem((String)value);
 	        
 			// Return the configured component
 			return component;
 		}
 
 		// This method is called when editing is completed.
 		// It must return the new value to be stored in the cell.
 		public Object getCellEditorValue() {
 			return component.getSelectedItem();//((JTextField)component).getText();
 		}    
 	}
 
 	
 	private class NormalizationCellRenderer extends JComboBox implements TableCellRenderer {
 		public Component getTableCellRendererComponent(JTable table,
 							       Object value,
 							       boolean isSelected,
 							       boolean hasFocus,
 							       int row,
 							       int column)
 		{
 			DefaultComboBoxModel model = new DefaultComboBoxModel();
 			for (final ScalingMethodX method : ScalingMethodX.values())
 				model.addElement(method.getDisplayString());
 
 			this.setModel(model);
 			
 			if (value.toString().equalsIgnoreCase("")){
 				this.setSelectedItem(ScalingMethodX.NONE.getDisplayString());	
 			}
 			else {
 				this.setSelectedItem(value);								
 			}
 			
 			if (isSelected){
 				this.setBackground(table.getSelectionBackground());
 				this.setForeground(table.getSelectionForeground());
 			}
 			else {
 				this.setBackground(table.getBackground());
 				this.setForeground(table.getForeground());				
 			}
 			
 			Double min = (Double)table.getModel().getValueAt(row, 1);
 			Double max = (Double)table.getModel().getValueAt(row, 2);
 			
 			setEnabled(true);
 			
 			return this;
 		}
 	}
 	
 	
 	private Vector<Object[]> getDataVect(){
 		
 		Vector<Object[]> dataVect = new Vector<Object[]>();
 
 		if (this.networkPanel.getSelectedNetwork() == null){
 			return dataVect;
 		}
 		
 		CyTable table = this.networkPanel.getSelectedNetwork().getDefaultNodeTable();
 					
		Object[] objs = CyTableUtil.getColumnNames(table).toArray();
		
		String[] names = new String[objs.length];

		for (int i=0; i< objs.length; i++){
			names[i] = objs[i].toString();
		}
 
 		for (String name: names){
 			
 			CyColumn col = table.getColumn(name);
 			
			if ( col.getType() == Double.class) {
				List<Double> vals = (List<Double>) col.getValues(Double.class);
 				
 				if ( vals == null ) 
 					continue; // no values have been defined for the attr yet
 
 				Object[] row = new Object[5];
 				row[0] = name;
 				
 				boolean isPValue = true;
 				for ( Object value : vals ) {
 					double d = ((Double)value).doubleValue();	
 				if ( d < 0 || d > 1 ) {
 						isPValue = false;
 						break;
 					}
 				}
 
 				row[1] = Collections.min(vals);
 				row[2] = Collections.max(vals);
 				row[3] = false;
 
 				if (!isPValue) {
 					
 					row[4] = isPValue ? ScalingMethodX.NONE.getDisplayString() : ScalingMethodX.RANK_UPPER.getDisplayString();
 				}
 				else
 					row[4] = ScalingMethodX.NONE.getDisplayString();
 
 				dataVect.add(row);
 			}
 		}
 		
 		// do sorting, put the rows with value range 0-1 ahead to the rest
 		Collections.sort(dataVect,new CompareTableRow());
 		
 		return dataVect;
 	}
 	
 	// do sorting, put the rows with value range 0-1 ahead to the rest
 	class CompareTableRow implements Comparator<Object[]>
 	{
 		public int compare(Object[] row1, Object[] row2){
 			if (!(Math.min((Double)row1[1],(Double)row1[2]) >=0 && Math.max((Double)row1[1],(Double)row1[2]) <=1)){
 				return 1;
 			}
 			return 0;
 		}
 		
 		public boolean equals(Object[] o){
 			return false;
 		}
 	}
 	
 	
 	private class AttrSelectionTableModel extends AbstractTableModel {
 		Vector dataVect = null;
 		String[] columnNames = {"Name","Most sig", "Least sig", "Reverse sig", "Scaling"};
 		public AttrSelectionTableModel(Vector pDataVect){
 			dataVect = pDataVect;
 		}
 		
 		public String getColumnName(int columnIndex){
 			return columnNames[columnIndex];
 		}
 		
 		public int getRowCount() {
 			if (dataVect == null){
 				return 0;
 			}
 			return dataVect.size();
 		}
 
 		public int getColumnCount() {
 			return columnNames.length;
 		}
 		
 		public Object getValueAt(int row, int col){
 			Object[] oneRow = (Object[])dataVect.elementAt(row);
 			return oneRow[col];
 		}
 
 		public void setValueAt(Object value, int row, int col){
 			Object[] oneRow = (Object[])dataVect.elementAt(row);
 			oneRow[col] = value;
 			fireTableCellUpdated(row, col);
 		}
 
 		
 		public Class getColumnClass(int col){
 			Object[] oneRow = (Object[])dataVect.elementAt(0);
 			return oneRow[col].getClass();
 		}
 		
 		public boolean isCellEditable(int row, int col){
 			if (col == 3){
 				return true;
 			}
 
 			if (col == 4){
 				return true;
 			}
 			
 			return false;
 		}
 		
 		public Object[] getRow(int row){
 			return (Object[]) dataVect.elementAt(row);
 		}
 	}
 	
 
 	private class NameColumnCellRenderer extends DefaultTableCellRenderer {
 		private javax.swing.ImageIcon icon = new ImageIcon(getClass().getResource("/images/exclamationpoint.jpg"));
 		private javax.swing.ImageIcon icon1 = new ImageIcon(getClass().getResource("/images/empty.jpg"));
 		private String TOOLTIP_EXCLAIMATION="<html>jActiveModules requires that any numeric node attribute<br>"+
 			"used as a search parameter have a value between 0 and 1.<br>"+
 			"This attribute will be automatically adjusted using the<br>specified scaling approach</html>";
 		private String TOOLTIP_NONE="";
 
 		public Component getTableCellRendererComponent(JTable table,
                 Object value,
                 boolean isSelected,
                 boolean hasFocus,
                 int row,
                 int column){
 			this.setText(value.toString());
 			
 			double min = Double.valueOf(table.getModel().getValueAt(row,1).toString());
 			double max = Double.valueOf(table.getModel().getValueAt(row,2).toString());
 
 			if (Math.min(min,max) < 0 || Math.max(min, max) > 1){
 				setIcon(icon);
 				this.setToolTipText(this.TOOLTIP_EXCLAIMATION);
 			}
 			else {
 				setIcon(icon1);
 				this.setToolTipText(this.TOOLTIP_NONE);
 			}
 			if (isSelected){
 				this.setBackground(table.getSelectionBackground());
 				this.setForeground(table.getSelectionForeground());
 			}
 			else {
 				this.setBackground(table.getBackground());
 				this.setForeground(table.getForeground());
 			}
 			return this;
 		}
 	}
 	
 	
 	public CyNetwork getTargetNetwork() {
 		return this.networkPanel.getSelectedNetwork();
 	}
 
 	/**
 	 * Setup button panel components.
 	 * 
 	 */
 	private void addButtonEventListeners() {
 
         helpButton.setPreferredSize(new java.awt.Dimension(67, 23));
 
         aboutButton.setPreferredSize(new java.awt.Dimension(67, 23));
         aboutButton.setVisible(false); // just place holder now
         dismissButton.setPreferredSize(new java.awt.Dimension(67, 23));
         dismissButton.addActionListener(new DismissAction());
         
         findModulesButton.addActionListener(new FindModulesAction());
 	}
 
     // Variables declaration - do not modify
     private javax.swing.JButton aboutButton;
     private javax.swing.JButton dismissButton;
     //private javax.swing.JButton findModulesButton;
     private javax.swing.JButton helpButton;
     // End of variables declaration
   
 
 	protected void updateOptionsPanel() {
 
 		boolean showOptions = this.tblAttrSelection.getSelectedRowCount() != 0;
 		
 		CardLayout cl = (CardLayout) optionsPanel.getLayout();
 		if (showOptions) {
 			cl.show(optionsPanel, "ACTIVE");
 			findModulesButton.setEnabled(true);
 			
 			if (this.collapsiblePanel != null) {
 				this.collapsiblePanel.setVisible(true);				
 			}
 		} else {
 			cl.show(optionsPanel, "INACTIVE");
 			findModulesButton.setEnabled(false);
 
 			if (this.collapsiblePanel != null) {
 				this.collapsiblePanel.setVisible(false);
 			}
 		}
 	}
 
 	private void createAnnealContentPanel() {
 		annealContentPanel = new JPanel();
 		annealContentPanel.setLayout(new BoxLayout(annealContentPanel,
 				BoxLayout.PAGE_AXIS));
 		GridBagConstraints c = new GridBagConstraints();
 
 		createIterationsController();
 		annealContentPanel.add(iterPanel);
 
 		TempController tc = new TempController();
 		annealContentPanel.add(tStartPanel);
 
 		annealContentPanel.add(tEndPanel);
 		annealContentPanel.add(annealExtPanel);
 
 		JPanel rsPanel = createRandomSeedController();
 		annealContentPanel.add(rsPanel);
 
 		Border border = BorderFactory.createLineBorder(Color.black);
 		Border titledBorder = BorderFactory.createTitledBorder(border,
 				"Annealing Parameters", TitledBorder.CENTER,
 				TitledBorder.DEFAULT_POSITION);
 		annealContentPanel.setBorder(titledBorder);
 	}
 
 	private void createSearchContentPanel() {
 		searchContentPanel = new JPanel();
 		searchContentPanel.setMinimumSize(new Dimension(200, 200));
 		GridBagLayout gridbag = new GridBagLayout();
 		GridBagConstraints c = new GridBagConstraints();
 		searchContentPanel.setLayout(gridbag);
 
 		c.fill = GridBagConstraints.NONE;
 		c.weightx = 1.0;
 		c.ipadx = 10;
 		c.ipady = 10;
 		// /////////////////////////////////////////
 
 		c.gridx = 0;
 		c.gridy = 8;
 		c.gridwidth = GridBagConstraints.REMAINDER; // end row
 		c.anchor = GridBagConstraints.CENTER;
 		createSearchController();
 		gridbag.setConstraints(searchPanel, c);
 		searchContentPanel.add(searchPanel);
 
 		final Border border = BorderFactory.createLineBorder(Color.black);
 		final Border titledBorder = BorderFactory.createTitledBorder(border,
 				"Searching Parameters", TitledBorder.CENTER,
 				TitledBorder.DEFAULT_POSITION);
 		searchContentPanel.setBorder(titledBorder);
 	}
 
 	// -----------------------------------------------------------------------------
 	private Container createExtsControllerPanel() {
 		annealExtPanel = new JPanel();
 
 		createHubfindingController();
 		createHubAdjustmentController();
 		createMontecarloController();
 
 		quenchCheck = new JCheckBox("Quenching", apfParams.getToQuench());
 		QuenchCheckListener qcListener = new QuenchCheckListener();
 		quenchCheck.addItemListener(qcListener);
 		JPanel quenchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		quenchPanel.add(quenchCheck);
 
 		JLabel annealingExtLabel = new JLabel("Annealing Extensions:");
 
 		GroupLayout layout = new GroupLayout(annealExtPanel);
 		annealExtPanel.setLayout(layout);
 		layout.setHorizontalGroup(layout.createParallelGroup().add(
 				annealingExtLabel).add(quenchPanel).add(hfcPanel));
 		layout.setVerticalGroup(layout.createSequentialGroup().add(
 				annealingExtLabel).add(quenchPanel).add(hfcPanel));
 
 		// -------------------------------------------------------------
 
 		final JPanel generalExtPanel = new JPanel();
 		generalExtPanel.setPreferredSize(new Dimension(300, 150));
 		layout = new GroupLayout(generalExtPanel);
 		generalExtPanel.setLayout(layout);
 
 		final Border generalBorder = BorderFactory.createLineBorder(Color.black);
 		final Border generalTitledBorder = BorderFactory.createTitledBorder(
 				generalBorder, "General Parameters", TitledBorder.CENTER,
 				TitledBorder.DEFAULT_POSITION);
 		generalExtPanel.setBorder(generalTitledBorder);
 
 		createRegionalScoringController();
 		createPathsController();
 		createOverlapController();
 
 		layout.setHorizontalGroup(layout.createParallelGroup().add(pathPanel)
 				.add(overlapPanel).add(mcBox).add(regionalBox));
 
 		layout.setVerticalGroup(layout.createSequentialGroup().add(pathPanel)
 				.add(overlapPanel).add(mcBox).add(regionalBox));
 
 		return generalExtPanel;
 	}
 
 	private void createAnnealSearchControlPanel() {
 		annealSearchControlPanel = new JPanel();
 		annealSearchControlPanel.setMinimumSize(new Dimension(300, 400));
 		annealSearchControlPanel.setPreferredSize(new Dimension(300, 400));
 		annealSearchContentPanel = new JPanel(new CardLayout());
 		GroupLayout layout = new GroupLayout(annealSearchControlPanel);
 		annealSearchControlPanel.setLayout(layout);
 
 		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
 		annealButton = new JRadioButton(ANNEAL);
 		searchButton = new JRadioButton(SEARCH);
 		// temporarily disabled while option unavailable
 		ButtonGroup annealSearchGroup = new ButtonGroup();
 		annealSearchGroup.add(annealButton);
 		annealSearchGroup.add(searchButton);
 		buttonsPanel.add(annealButton);
 		buttonsPanel.add(searchButton);
 
 		layout.setHorizontalGroup(layout.createParallelGroup()
 				.add(buttonsPanel).add(annealSearchContentPanel));
 
 		layout.setVerticalGroup(layout.createSequentialGroup()
 				.add(buttonsPanel).add(annealSearchContentPanel));
 		Border ascBorder = BorderFactory.createLineBorder(Color.black);
 		Border ascTitledBorder = BorderFactory.createTitledBorder(ascBorder,
 				"Strategy", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION);
 		annealSearchControlPanel.setBorder(ascTitledBorder);
 
 		AnnealSearchSwitchListener switchListener = new AnnealSearchSwitchListener();
 		annealButton.addActionListener(switchListener);
 		searchButton.addActionListener(switchListener);
 
 		annealSearchContentPanel.add(searchContentPanel, SEARCH);
 		annealSearchContentPanel.add(annealContentPanel, ANNEAL);
 		if (apfParams.getGreedySearch()) {
 			currentContentPanel = searchContentPanel;
 			switchAnnealSearchContentPanel(SEARCH);
 			searchButton.setSelected(true);
 		} else {
 			currentContentPanel = annealContentPanel;
 			switchAnnealSearchContentPanel(ANNEAL);
 			annealButton.setSelected(true);
 		}
 		annealSearchControlPanel.add(annealSearchContentPanel);
 	}
 
 	private void switchAnnealSearchContentPanel(String name) {
 		CardLayout cl = (CardLayout) annealSearchContentPanel.getLayout();
 		cl.show(annealSearchContentPanel, name);
 	}
 
 	private JPanel createRandomSeedController() {
 		JPanel panel = new JPanel();
 		panel.setLayout(new GridLayout(0, 1));
 		JLabel seedGraphOptionsLabel = new JLabel("Seed Graph Options:");
 		JRadioButton smallPrimeNumberSeedButton = new JRadioButton(NONRANDOM);
 		JRadioButton dateBasedSeedButton = new JRadioButton(
 				"Random Based on Current Time");
 		dateBasedSeedButton.setSelected(true);
 
 		ButtonGroup buttonGroup = new ButtonGroup();
 		buttonGroup.add(smallPrimeNumberSeedButton);
 		buttonGroup.add(dateBasedSeedButton);
 
 		panel.add(seedGraphOptionsLabel);
 		panel.add(smallPrimeNumberSeedButton);
 		panel.add(dateBasedSeedButton);
 
 		RandomSeedListener listener = new RandomSeedListener();
 		smallPrimeNumberSeedButton.addActionListener(listener);
 		dateBasedSeedButton.addActionListener(listener);
 
 		panel.add(smallPrimeNumberSeedButton); // , BorderLayout.CENTER);
 		panel.add(dateBasedSeedButton); // , BorderLayout.CENTER);
 		panel.add(readout);
 		return panel;
 
 	} // createRandomSeedController
 
 	// -----------------------------------------------------------------------------
 	private void createHubfindingController() {
 		hfcPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
 		HFListener listener = new HFListener();
 		boolean hfInit = (apfParams.getMinHubSize() == 0) ? false : true;
 		hubBox = new JCheckBox("Hubfinding: ", hfInit);
 		hubNum = new JTextField(Integer.toString(apfParams.getMinHubSize()));
 
 		if (!hfInit) {
 			hubNum.setText("10");
 			hubNum.setEnabled(false);
 			hubBox.setSelected(false);
 			apfParams.setMinHubSize(0);
 		}
 		hubBox.addItemListener(listener);
 		hubNum.addFocusListener(listener);
 
 		hfcPanel.add(hubBox);
 		hfcPanel.add(hubNum);
 
 		return;
 
 	} // createHubfindingController
 
 	private void createHubAdjustmentController() {
 		hAdjPanel = new JPanel();
 		hAdjPanel.setLayout(new GridLayout(1, 2));
 		HAListener listener = new HAListener();
 		boolean haInit = (apfParams.getHubAdjustment() == 0) ? false : true;
 		hubAdjustmentBox = new JCheckBox("Hub Penalty", haInit);
 		// temporarily disabled while option not available
 		hubAdjustmentBox.setEnabled(false);
 		hubAdjustmentNum = new JTextField(Double.toString(apfParams
 				.getHubAdjustment()));
 
 		if (!haInit) {
 			hubAdjustmentNum.setText("0.406");
 			hubAdjustmentNum.setEnabled(false);
 			hubAdjustmentBox.setSelected(false);
 			apfParams.setHubAdjustment(0);
 		}
 		hubAdjustmentBox.addItemListener(listener);
 		hubAdjustmentNum.addFocusListener(listener);
 
 		hAdjPanel.add(hubAdjustmentBox);
 		hAdjPanel.add(hubAdjustmentNum);
 
 		return;
 
 	} // createHubAdjustmentController
 
 	private void createSearchController() {
 		searchPanel = new JPanel();
 		GroupLayout layout = new GroupLayout(searchPanel);
 		searchPanel.setLayout(layout);
 
 		AnnealSearchSwitchListener listener = new AnnealSearchSwitchListener();
 		searchDepth = new JTextField(Integer.toString(apfParams
 				.getSearchDepth()), 2);
 		searchFromNodesBox = new JCheckBox("Search from selected nodes?",
 				apfParams.getSearchFromNodes());
 		final JPanel searchFromNodesPanel = new JPanel(new BorderLayout());
 		searchFromNodesPanel.add(searchFromNodesBox, BorderLayout.LINE_START);
 
 		final JPanel searchDepthPanel = new JPanel(new BorderLayout());
 		searchDepthPanel.add(new JLabel("Search depth: "), BorderLayout.LINE_START);
 		searchDepthPanel.add(searchDepth, BorderLayout.CENTER);
 
 		maxBox = new JCheckBox("Max depth from start nodes:", apfParams
 				.getEnableMaxDepth());
 		maxDepth = new JTextField(Integer.toString(apfParams.getMaxDepth()), 2);
 		MListener mListener = new MListener();
 		maxDepth.setEnabled(apfParams.getEnableMaxDepth());
 		maxBox.addItemListener(mListener);
 		maxDepth.addFocusListener(mListener);
 
 		final JPanel maxDepthPanel = new JPanel(new BorderLayout());
 		maxDepthPanel.add(maxBox, BorderLayout.LINE_START);
 		maxDepthPanel.add(maxDepth, BorderLayout.CENTER);
 		searchDepth.addFocusListener(listener);
 		searchFromNodesBox.addItemListener(new SFNListener());
 
 		layout.setHorizontalGroup(layout.createParallelGroup().add(
 				searchDepthPanel).add(searchFromNodesPanel).add(maxDepthPanel));
 
 		layout.setVerticalGroup(layout.createSequentialGroup().add(
 				searchDepthPanel).add(searchFromNodesPanel).add(maxDepthPanel));
 	} // createSearchController
 
 	// -----------------------------------------------------------------------------
 	private void createIterationsController() {
 		iterPanel = new JPanel();
 		iterPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
 		IterListener listener = new IterListener();
 		iterLabel = new JLabel("Iterations (0-10^8)");
 		iterNum = new JTextField(Integer.toString(apfParams
 				.getTotalIterations()), 5);
 
 		iterNum.addFocusListener(listener);
 
 		iterPanel.add(iterLabel);
 		iterPanel.add(iterNum);
 
 		return;
 
 	} // createIterationsController
 
 	// -----------------------------------------------------------------------------
 	private void createPathsController() {
 		pathPanel = new JPanel();
 		GroupLayout layout = new GroupLayout(pathPanel);
 		pathPanel.setLayout(layout);
 
 		pathLabel = new JLabel("Number of Modules (1-1000): ");
 		pathNum = new JTextField(Integer.toString(apfParams.getNumberOfPaths()));
 		java.awt.FontMetrics fontMetrics = pathNum.getFontMetrics(pathNum
 				.getFont());
 		pathNum.setMaximumSize(new Dimension(fontMetrics.charWidth('m') * 7,
 				fontMetrics.getHeight()));
 		pathNum.addFocusListener(new PathListener());
 
 		layout.setHorizontalGroup(layout.createSequentialGroup().add(pathLabel)
 				.add(pathNum));
 
 		layout.setVerticalGroup(layout.createParallelGroup().add(pathLabel)
 				.add(pathNum));
 	} // createPathsController
 
 	private void createOverlapController() {
 		overlapPanel = new JPanel();
 		GroupLayout layout = new GroupLayout(overlapPanel);
 		overlapPanel.setLayout(layout);
 
 		overlapLabel = new JLabel("Overlap Threshold: ");
 		overlapNum = new JTextField(Double.toString(apfParams
 				.getOverlapThreshold()));
 		java.awt.FontMetrics fontMetrics = overlapNum.getFontMetrics(pathNum
 				.getFont());
 		overlapNum.setMaximumSize(new Dimension(fontMetrics.charWidth('m') * 7,
 				fontMetrics.getHeight()));
 		overlapNum.addFocusListener(new OverlapListener());
 
 		layout.setHorizontalGroup(layout.createSequentialGroup().add(
 				overlapLabel).add(overlapNum));
 
 		layout.setVerticalGroup(layout.createParallelGroup().add(overlapLabel)
 				.add(overlapNum));
 	} // createPathsController
 
 	// -----------------------------------------------------------------------------
 
 	class TempController {
 
 		public TempController() {
 
 			tStartPanel = new JPanel();
 			tEndPanel = new JPanel();
 			startNum = new JTextField(Double.toString(apfParams
 					.getInitialTemperature()), 5);
 			endNum = new JTextField(Double.toString(apfParams
 					.getFinalTemperature()), 5);
 
 			TempListener listener = new TempListener();
 			tempLabelStart = new JLabel("Start Temp (0.0001 - 100)");
 			tempLabelEnd = new JLabel("End Temp (0.0001 - Start)");
 
 			// tStartPanel.setLayout(new GridLayout(0, 1));
 			tStartPanel.setLayout(new java.awt.FlowLayout(
 					java.awt.FlowLayout.LEFT));
 			tStartPanel.add(tempLabelStart);
 			tStartPanel.add(startNum);
 
 			// tEndPanel.setLayout(new GridLayout(0, 1));
 			tEndPanel.setLayout(new java.awt.FlowLayout(
 					java.awt.FlowLayout.LEFT));
 			tEndPanel.add(tempLabelEnd);
 			tEndPanel.add(endNum);
 
 			startNum.addFocusListener(listener);
 			endNum.addFocusListener(listener);
 		}
 
 	} // TempController
 
 	// -----------------------------------------------------------------------------
 	private void createIntervalController() {
 
 		intervalNum = new JTextField(Integer.toString(apfParams
 				.getDisplayInterval()));
 		intervalNum.setEnabled(false);
 		intervalLabel = new JLabel("Display Interval (1-1e05)");
 		intervalPanel = new JPanel();
 
 		intervalPanel.setLayout(new GridLayout(0, 1));
 		IntervalListener listener = new IntervalListener();
 
 		intervalNum.addFocusListener(listener);
 
 		intervalPanel.add(intervalLabel);
 		intervalPanel.add(intervalNum);
 
 	} // createIntervalController
 
 	private void createMontecarloController() {
 		boolean mcInit = apfParams.getMCboolean();
 		mcBox = new JCheckBox("Adjust score for size?", mcInit);
 		mcBox.addItemListener(new MCListener());
 	} // createMontecarloController
 
 	private void createRegionalScoringController() {
 		boolean regionalInit = apfParams.getRegionalBoolean();
 		regionalBox = new JCheckBox("Regional Scoring?", regionalInit);
 		// temporarily disabled while option not available
 		// regionalBox.setEnabled(false);
 		regionalBox.addItemListener(new RSListener());
 	} // createRegionalScoringController
 
 	private void createHelpDialog() {
 		helpDialog = new JDialog(this.cySwingApplicationServiceRef.getJFrame(), "jActiveModules Help");
 		helpDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
 		try {
 			
 			JEditorPane helpPane = new JEditorPane(getClass().getResource("/help/help.html"));
 			helpPane.setEditable(false);
 			JScrollPane scrollPane = new JScrollPane(helpPane);
 			helpDialog.setContentPane(scrollPane);
 			helpDialog.setPreferredSize(new Dimension(750, 400));
 		} catch (IOException e) {
 			JLabel label = new JLabel("Could not find help.html.");
 			helpDialog.setContentPane(label);
 		}
 		helpDialog.pack();
 	}
 
 	// -----------------------------------------------------------------------------
 	class RandomSeedListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			String setting = e.getActionCommand();
 			if (setting.equals(NONRANDOM))
 				apfParams.setRandomSeed(17);
 			else
 				apfParams.setRandomSeed(Math.abs((int) System
 						.currentTimeMillis()));
 			readout.setText("seed: " + apfParams.getRandomSeed());
 		}
 
 	} // RandomSeedListener
 
 	// -----------------------------------------------------------------------------
 	class AnnealSearchSwitchListener implements ActionListener, FocusListener {
 		public void focusGained(FocusEvent e) {
 			validate();
 		}
 
 		public void focusLost(FocusEvent e) {
 			validate();
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			String setting = e.getActionCommand();
 			if (setting.equals(ANNEAL)) {
 				validate();
 				switchToAnneal();
 			} else if (setting.equals(SEARCH)) {
 				validate();
 				switchToSearch();
 			}
 		}
 
 		private void switchToAnneal() {
 			if (currentContentPanel != annealContentPanel) {
 				currentContentPanel = annealContentPanel;
 				switchAnnealSearchContentPanel(ANNEAL);
 				apfParams.setGreedySearch(false);
 			}
 		}
 
 		private void switchToSearch() {
 			if (currentContentPanel != searchContentPanel) {
 				currentContentPanel = searchContentPanel;
 				switchAnnealSearchContentPanel(SEARCH);
 				apfParams.setGreedySearch(true);
 			}
 		}
 
 		private void validate() {
 			if (searchButton.isSelected()) {
 				String st = searchDepth.getText();
 				// String st2 = st.replaceAll("[^0-9]", ""); // ditch all
 				// non-numeric
 				if (st.length() > 0) {
 					try {
 						int si = Integer.parseInt(st);
 						if (si < 0) {
 							searchDepth.setText("0");
 							apfParams.setSearchDepth(0);
 						} else if (si > 10) {
 							searchDepth.setText("10");
 							apfParams.setSearchDepth(10);
 						} else {
 							searchDepth.setText(st);
 							apfParams.setSearchDepth(si);
 						}
 					} catch (NumberFormatException nfe) {
 						logger.info("Not an int: " + st);
 						searchDepth.setText("1");
 						apfParams.setSearchDepth(1);
 					}
 				} else {
 					searchDepth.setText("1");
 					apfParams.setSearchDepth(1);
 				}
 			} else
 				apfParams.setSearchDepth(1);
 		}
 
 	} // AnnealSearchSwitchListener
 
 	// -----------------------------------------------------------------------------
 	class RandomSeedTextListener implements FocusListener {
 		public void focusGained(FocusEvent e) {
 			validate();
 		}
 
 		public void focusLost(FocusEvent e) {
 			validate();
 		}
 
 		private void validate() {
 			String rt = readout.getText();
 			String rt2 = rt.replaceAll("[^0-9]", ""); // ditch all non-numeric
 			if (rt2.length() > 0) {
 				logger.debug(" length " + rt2.length());
 				try {
 					int seed = Integer.parseInt(rt2);
 					apfParams.setRandomSeed(seed);
 					readout.setText("seed: " + apfParams.getRandomSeed());
 				} catch (NumberFormatException nfe) {
 					logger.info("Not an integer: " + rt2);
 					apfParams.setRandomSeed(0);
 					readout.setText("seed: " + apfParams.getRandomSeed());
 				}
 			} // if gt 0
 			else {
 				apfParams.setRandomSeed(0);
 				readout.setText("seed: " + apfParams.getRandomSeed());
 			} // if gt 0 (else)
 		} // validate()
 	} // RandomSeedTextListener
 
 	// -----------------------------------------------------------------------------
 	class MCListener implements ItemListener {
 		public void itemStateChanged(ItemEvent e) {
 			apfParams.setMCboolean(mcBox.isSelected());
 		}
 	}
 
 	class RSListener implements ItemListener {
 		public void itemStateChanged(ItemEvent e) {
 			apfParams.setRegionalBoolean(regionalBox.isSelected());
 		}
 	}
 
 	class SFNListener implements ItemListener {
 		public void itemStateChanged(ItemEvent e) {
 			apfParams.setSearchFromNodes(searchFromNodesBox.isSelected());
 		}
 	}
 
 	class QuenchCheckListener implements ItemListener {
 		public void itemStateChanged(ItemEvent e) {
 			JCheckBox jcb = (JCheckBox) e.getItem();
 			apfParams.setToQuench(jcb.isSelected());
 		}
 	}
 
 	class HFListener implements ItemListener, FocusListener {
 		public void focusGained(FocusEvent e) {
 			validate();
 		}
 
 		public void focusLost(FocusEvent e) {
 			validate();
 		}
 
 		public void itemStateChanged(ItemEvent e) {
 			hubNum.setEnabled(hubBox.isSelected());
 			validate();
 		}
 
 		private void validate() {
 			if (hubBox.isSelected()) {
 				String ht = hubNum.getText();
 				String ht2 = ht.replaceAll("[^0-9]", ""); // ditch all
 				// non-numeric
 				if (ht.length() > 0) {
 					logger.debug(" length " + ht.length());
 					try {
 						int hf = Integer.parseInt(ht2);
 						if (hf <= 0) {
 							hubNum.setText("10");
 							hubNum.setEnabled(false);
 							hubBox.setSelected(false);
 							apfParams.setMinHubSize(0);
 						} else if (hf > 10000) {
 							hubNum.setText("10000");
 							apfParams.setMinHubSize(10000);
 						} else {
 							hubNum.setText(ht2);
 							apfParams.setMinHubSize(hf);
 						}
 
 					} catch (NumberFormatException nfe) {
 						logger.info("Not an integer: " + ht2);
 						hubNum.setText("10");
 						apfParams.setMinHubSize(10);
 						// JOptionPane.showMessageDialog (mainPanel, "Not an
 						// integer: " + ht2);
 					}
 				} else {
 					logger.debug(" length " + ht.length());
 					logger.debug(" going for 10.");
 					hubNum.setText("10");
 					apfParams.setMinHubSize(10);
 				}
 			} else
 				apfParams.setMinHubSize(0);
 		}
 
 	} // HFListener
 
 	class HAListener implements ItemListener, FocusListener {
 		public void focusGained(FocusEvent e) {
 			validate();
 		}
 
 		public void focusLost(FocusEvent e) {
 			validate();
 		}
 
 		public void itemStateChanged(ItemEvent e) {
 			hubAdjustmentNum.setEnabled(hubAdjustmentBox.isSelected());
 			validate();
 		}
 
 		private void validate() {
 			if (hubAdjustmentBox.isSelected()) {
 				String ht = hubAdjustmentNum.getText();
 				String ht2 = ht.replaceAll("[^0-9.]", ""); // ditch all
 				// non-numeric
 				if (ht.length() > 0) {
 					logger.debug(" length " + ht.length());
 					try {
 						double hf = Double.parseDouble(ht2);
 						if (hf <= 0) {
 							hubAdjustmentNum.setText("0.406");
 							hubAdjustmentNum.setEnabled(false);
 							hubAdjustmentBox.setSelected(false);
 							apfParams.setHubAdjustment(0);
 						} else if (hf > 100) {
 							hubAdjustmentNum.setText("100");
 							apfParams.setHubAdjustment(100);
 						} else {
 							hubAdjustmentNum.setText(ht2);
 							apfParams.setHubAdjustment(hf);
 						}
 
 					} catch (NumberFormatException nfe) {
 						logger.info("Not a double: " + ht2);
 						hubAdjustmentNum.setText("0.406");
 						apfParams.setHubAdjustment(0.406);
 					}
 				} else {
 					hubAdjustmentNum.setText("0.406");
 					apfParams.setHubAdjustment(0.406);
 				}
 			} else
 				apfParams.setHubAdjustment(0);
 		}
 
 	} // HAListener
 
 	class MListener implements ItemListener, FocusListener {
 		public void focusGained(FocusEvent e) {
 			validate();
 		}
 
 		public void focusLost(FocusEvent e) {
 			validate();
 		}
 
 		public void itemStateChanged(ItemEvent e) {
 			maxDepth.setEnabled(maxBox.isSelected());
 			apfParams.setEnableMaxDepth(maxBox.isSelected());
 			validate();
 		}
 
 		private void validate() {
 			if (maxBox.isSelected()) {
 				String st = maxDepth.getText();
 				// String st2 = st.replaceAll("[^0-9]", ""); // ditch all
 				// non-numeric
 				if (st.length() > 0) {
 					try {
 						int si = Integer.parseInt(st);
 						if (si < 0) {
 							maxDepth.setText("0");
 							// maxDepth.setEnabled(false);
 							// maxBox.setSelected(false);
 							apfParams.setMaxDepth(0);
 						} else if (si > 10) {
 							maxDepth.setText("10");
 							apfParams.setMaxDepth(10);
 						} else {
 							maxDepth.setText(st);
 							apfParams.setMaxDepth(si);
 						}
 					} catch (NumberFormatException nfe) {
 						logger.info("Not an int: " + st);
 						maxDepth.setText("1");
 						apfParams.setMaxDepth(1);
 					}
 				} else {
 					maxDepth.setText("1");
 					apfParams.setMaxDepth(1);
 				}
 			} else {
 				maxDepth.setEnabled(false);
 				// apfParams.setMaxDepth(1);
 			}
 			// }
 		}
 	} // MListener
 
 	// -----------------------------------------------------------------------------
 	class IterListener implements FocusListener {
 		public void focusGained(FocusEvent e) {
 			validate();
 		}
 
 		public void focusLost(FocusEvent e) {
 			validate();
 		}
 
 		private void validate() {
 			String it = iterNum.getText();
 			String it2 = it.replaceAll("[^0-9]", ""); // ditch all
 			// non-numeric
 			if (it2.length() > 0) {
 				if (it2.length() >= 9) {
 					iterNum.setText("100000000");
 					apfParams.setTotalIterations(100000000);
 				} else {
 					logger.debug(" length " + it.length());
 					try {
 						int iters = Integer.parseInt(it2);
 						if (iters <= 0) {
 							iterNum.setText("0");
 							apfParams.setTotalIterations(0);
 						} else if (iters > 100000000) {
 							iterNum.setText("100000000");
 							apfParams.setTotalIterations(100000000);
 						} else {
 							iterNum.setText(it2);
 							apfParams.setTotalIterations(iters);
 						}
 					} catch (NumberFormatException nfe) {
 						logger.info("Not an integer: " + it2);
 						iterNum.setText("0");
 						apfParams.setTotalIterations(0);
 					}
 				} // if gt 9 (else)
 			} // if gt 0
 			else {
 				iterNum.setText("0");
 				apfParams.setTotalIterations(0);
 			} // if gt 0 (else)
 		}
 
 	} // IterListener
 
 	// -----------------------------------------------------------------------------
 	class OverlapListener implements FocusListener {
 		public void focusGained(FocusEvent e) {
 			validate();
 		}
 
 		public void focusLost(FocusEvent e) {
 			validate();
 		}
 
 		private void validate() {
 			String pt = overlapNum.getText();
 			try {
 				double overlap = Double.parseDouble(pt);
 				if (overlap < 0.0) {
 					overlap = 0.0;
 				}
 				if (overlap > 1.0) {
 					overlap = 1.0;
 				}
 				overlapNum.setText((new Double(overlap)).toString());
 				apfParams.setOverlapThreshold(overlap);
 			} catch (NumberFormatException nfe) {
 				overlapNum.setText("0.8");
 				apfParams.setOverlapThreshold(0.8);
 			}
 		}
 
 	} // PathListener
 
 	class PathListener implements FocusListener {
 		public void focusGained(FocusEvent e) {
 			validate();
 		}
 
 		public void focusLost(FocusEvent e) {
 			validate();
 		}
 
 		private void validate() {
 			String pt = pathNum.getText();
 			String pt2 = pt.replaceAll("[^0-9]", ""); // ditch all
 			// non-numeric
 			if (pt2.length() > 0) {
 				if (pt2.length() > 3) {
 					pathNum.setText("1000");
 					apfParams.setNumberOfPaths(1000);
 				} else {
 					logger.debug(" length " + pt.length());
 					try {
 						int paths = Integer.parseInt(pt2);
 						if (paths <= 0) {
 							pathNum.setText("0");
 							apfParams.setNumberOfPaths(0);
 						} else if (paths > 1000) {
 							pathNum.setText("1000");
 							apfParams.setNumberOfPaths(1000);
 						} else {
 							pathNum.setText(pt2);
 							apfParams.setNumberOfPaths(paths);
 						}
 					} catch (NumberFormatException nfe) {
 						logger.info("Not an integer: " + pt2);
 						pathNum.setText("0");
 						apfParams.setNumberOfPaths(0);
 					}
 				} // if gt 3 (else)
 			} // if gt 0
 			else {
 				pathNum.setText("0");
 				apfParams.setNumberOfPaths(0);
 			} // if gt 0 (else)
 		}
 
 	} // PathListener
 
 	// -----------------------------------------------------------------------------
 	class TempListener implements FocusListener {
 		public void focusGained(FocusEvent e) {
 			validate();
 		}
 
 		public void focusLost(FocusEvent e) {
 			validate();
 		}
 
 		private void validate() {
 			String st = startNum.getText();
 			String st2 = st.replaceAll("[^0-9.]", ""); // ditch all
 			// non-numeric
 			String et = endNum.getText();
 			String et2 = et.replaceAll("[^0-9.]", ""); // ditch all
 			// non-numeric
 
 			// //////////////////////////////////////////////
 			// first handle start temp
 			if (st2.length() > 0) {
 				logger.debug(" length " + st2.length());
 				try {
 					double startTemp = Double.parseDouble(st2);
 					if (startTemp <= 0) {
 						startNum.setText("0.0001");
 						apfParams.setInitialTemperature(0.0001);
 					} else if (startTemp > 100.0) {
 						startNum.setText("100.0");
 						apfParams.setInitialTemperature(100.0);
 					} else {
 						startNum.setText(st2);
 						apfParams.setInitialTemperature(startTemp);
 					}
 				} catch (NumberFormatException nfe) {
 					logger.info("Not a number: " + st2);
 					startNum.setText("1.000");
 					apfParams.setInitialTemperature(1.000);
 				}
 			} // if gt 0
 			else {
 				startNum.setText("1.000");
 				apfParams.setInitialTemperature(1.000);
 			} // if gt 0 (else)
 
 			// //////////////////////////////////////////////
 			// then handle end temp
 			if (et2.length() > 0) {
 				logger.debug(" length " + et2.length());
 				try {
 					double endTemp = Double.parseDouble(et2);
 					if (endTemp <= 0) {
 						endNum.setText("0.0001");
 						apfParams.setFinalTemperature(0.0001);
 					} else if (endTemp > apfParams.getInitialTemperature()) {
 						endNum.setText(startNum.getText());
 						apfParams.setFinalTemperature(apfParams
 								.getInitialTemperature());
 					} else {
 						endNum.setText(et2);
 						apfParams.setFinalTemperature(endTemp);
 					}
 				} catch (NumberFormatException nfe) {
 					logger.info("Not a number: " + et2);
 					endNum.setText("0.0001");
 					apfParams.setFinalTemperature(0.0001);
 				}
 			} // if gt 0
 			else {
 				endNum.setText("0.0001");
 				apfParams.setFinalTemperature(0.0001);
 			} // if gt 0 (else)
 
 		} // validate
 
 	} // TempListener
 
 	// -------------------------------------------------------------
 	class IntervalListener implements FocusListener {
 		public void focusGained(FocusEvent e) {
 			validate();
 		}
 
 		public void focusLost(FocusEvent e) {
 			validate();
 		}
 
 		private void validate() {
 			String it = intervalNum.getText();
 			String it2 = it.replaceAll("[^0-9]", ""); // ditch all
 			// non-numeric
 			if (it2.length() > 0) {
 				if (it2.length() >= 6) {
 					intervalNum.setText("100000");
 					apfParams.setDisplayInterval(100000);
 				} else {
 					logger.debug(" length " + it2.length());
 					try {
 						int intervals = Integer.parseInt(it2);
 						if (intervals <= 0) {
 							intervalNum.setText("0");
 							apfParams.setDisplayInterval(0);
 						} else if (intervals > 100000) {
 							intervalNum.setText("100000");
 							apfParams.setDisplayInterval(100000);
 						} else {
 							intervalNum.setText(it2);
 							apfParams.setDisplayInterval(intervals);
 						}
 					} catch (NumberFormatException nfe) {
 						logger.info("Not an integer: " + it2);
 						intervalNum.setText("0");
 						apfParams.setDisplayInterval(0);
 					}
 				} // if gte 6 (else)
 			} // if gt 0
 			else {
 				intervalNum.setText("0");
 				apfParams.setDisplayInterval(0);
 			} // if gt 0 (else)
 		}
 
 	} // IntervalListener
 
 	// } // QuitAction
 	// -----------------------------------------------------------------------------
 	public class DismissAction extends AbstractAction {
 
 		DismissAction() {
 			super("");
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			// listener.cancelActivePathsFinding ();
 			//CytoPanel cytoPanel = Cytoscape.getDesktop().getCytoPanel(
 			//		SwingConstants.WEST);
 			//cytoPanel.remove(ActivePathsParameterPanel.this);
 		}
 
 	} // DismissAction
 
 	public class FindModulesAction extends AbstractAction {
 
 		public FindModulesAction() {
 			super("Search");
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			int[] selectedIndices = ActivePathsParameterPanel.this.tblAttrSelection.getSelectedRows();
 	
 			final AttrSelectionTableModel model = (AttrSelectionTableModel) ActivePathsParameterPanel.this.tblAttrSelection.getModel();
 			
 			ArrayList<String> selectedNames = new ArrayList<String>();
 			ArrayList<Boolean> switchSigs = new ArrayList<Boolean>();
 			ArrayList<String> scalingMethods = new ArrayList<String>();
 			
 			for (int i = 0; i < selectedIndices.length; i++){
 				selectedNames.add((String)model.getRow(selectedIndices[i])[0]);
 				switchSigs.add((Boolean)model.getRow(selectedIndices[i])[3]);
 				scalingMethods.add((String)model.getRow(selectedIndices[i])[4]);
 			}
 		
 			apfParams.setExpressionAttributes(selectedNames);
 			apfParams.setSwitchSigs(switchSigs);
 			apfParams.setScalingMethods(scalingMethods);
 			pluginMainClass.startFindActivePaths(networkPanel.getSelectedNetwork());
 		}
 
 	}
 
 	class GenericListener extends FocusAdapter implements ActionListener {
 		public void focusLost(FocusEvent fe) {
 			this.actionPerformed(new ActionEvent(this, 0, ""));
 		}
 
 		public void actionPerformed(ActionEvent ae) {
 			// do nothing
 		}
 	}
 	
 	public void handlePanelSelected(){
 	}
 	
 	public void setPluginMainClass(ActiveModulesUI ui){
 		pluginMainClass = ui;
 	}
 
 	public void itemStateChanged(ItemEvent e) {
 		updateAttributePanel();
 	}
 	
 	private void updateAttributePanel(){
		AttrSelectionTableModel tableModel = new AttrSelectionTableModel(getDataVect());
		this.tblAttrSelection.setModel(tableModel);
 	}
 	
 } // class ActivePathsParametersPopupDialog
 
