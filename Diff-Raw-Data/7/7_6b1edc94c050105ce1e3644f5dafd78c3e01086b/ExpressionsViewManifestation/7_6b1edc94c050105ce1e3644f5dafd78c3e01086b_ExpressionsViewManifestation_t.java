 /*******************************************************************************
  * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
  * as represented by the Administrator of the National Aeronautics and Space 
  * Administration. All rights reserved.
  *
  * The MCT platform is licensed under the Apache License, Version 2.0 (the 
  * "License"); you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  * http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
  * License for the specific language governing permissions and limitations under 
  * the License.
  *
  * MCT includes source code licensed under additional open source licenses. See 
  * the MCT Open Source Licenses file included with this distribution or the About 
  * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
  * information. 
  *******************************************************************************/
 package gov.nasa.arc.mct.evaluator.expressions;
 
 import gov.nasa.arc.mct.components.AbstractComponent;
 import gov.nasa.arc.mct.components.FeedProvider;
 import gov.nasa.arc.mct.evaluator.component.EvaluatorComponent;
 import gov.nasa.arc.mct.evaluator.enums.EnumEvaluator;
 import gov.nasa.arc.mct.gui.SelectionProvider;
 import gov.nasa.arc.mct.gui.View;
 import gov.nasa.arc.mct.roles.events.AddChildEvent;
 import gov.nasa.arc.mct.roles.events.RemoveChildEvent;
 import gov.nasa.arc.mct.services.component.ViewInfo;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.ResourceBundle;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultCellEditor;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.UIManager;
 import javax.swing.border.Border;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableColumn;
 
 /**
  * Implements a visible manifestation of the expression view role.
  */
 @SuppressWarnings("serial")
 public class ExpressionsViewManifestation extends View {
 	private static final ResourceBundle bundle = ResourceBundle.getBundle("Enumerator");
 	
 	/** Expression view role name. */
 	public static final String VIEW_NAME = bundle.getString("ExpressionsViewRoleName");
 
 	private Expression selectedExpression;
 	private ExpressionList currentExpressions;
 	private ArrayList<AbstractComponent> telemetryElements;
 	private AbstractComponent selectedTelemetry;
 	private final EvaluatorComponent ec;
 	private ExpressionsFormattingControlsPanel controlPanel;
 	
 	@SuppressWarnings("unused")
 	private ExpressionsModel expModel;
 	/** The telemetry model. */
 	public TelemetryModel telModel;
 	private JTable expressionsTable;
 	private JTable telemetryTable;
 	private JPanel expressionsPanel;
 	private JLabel resultOutput;
 	private JTextField testValueInput;
 	private Border componentBorder = null;
 
 	/**
 	 * The expression view manifestation initialization.
 	 * @param ac the component.
 	 * @param vi the view info.
 	 */
 	public ExpressionsViewManifestation(AbstractComponent ac, ViewInfo vi) { 
 		super(ac,vi);
 		this.ec = getManifestedComponent().getCapability(EvaluatorComponent.class);
 		this.selectedExpression = new Expression();
 		this.currentExpressions = new ExpressionList(ec.getData().getCode());
 		this.telemetryElements = new ArrayList<AbstractComponent>();
 		refreshTelemetry();
 		expressionsPanel = new JPanel();
 		expressionsPanel.getAccessibleContext().setAccessibleName("Expressions");
 
 		if (getColor("border") != null) {
 			componentBorder = BorderFactory.createLineBorder(getColor("border"));
 		}
 		
 		load();
 		
 		expressionsPanel.setAutoscrolls(true);
 	}
 	
 	private Color getColor(String name) {
         return UIManager.getColor(name);        
     }
 	
 	/**
 	 * Gets the evaluator component.
 	 * @return the evaluator component.
 	 */
 	public EvaluatorComponent getEnum() {
 		return ec;
 	}
 	
 	/**
 	 * Gets the expression list.
 	 * @return the expression list.
 	 */
 	public ExpressionList getExpressions(){
 		return currentExpressions;
 	}
 	
 	/**
 	 * Gets the array list of telemetry components.
 	 * @return array list of telemetry components.
 	 */
 	public ArrayList<AbstractComponent> getTelemetry(){
 		return telemetryElements;
 	}
 	
 	private void refreshTelemetry(){
 		telemetryElements.clear();
 		for (AbstractComponent component : getManifestedComponent().getComponents()){
 			if (component.getCapability(FeedProvider.class) != null){
 				telemetryElements.add(component);
 			}
 		}
 	}
 	
 	private void load() {			
 		buildGUI();
 	}
 	
 	@SuppressWarnings("unused")
 	private boolean containsChildComponent(AbstractComponent parentComponent, String childComponentId) {
 		for (AbstractComponent childComponent : parentComponent.getComponents()) {
 			if (childComponentId.equals(childComponent.getComponentId())){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private void buildGUI() {
 		//show associated telemetry element, table of expressions, test value
 		setLayout(new GridBagLayout());
 		
 		//Input area for test value at top of view
 		testValueInput = new JTextField();
 		testValueInput.setEditable(true);
 		JLabel testValue = new JLabel("Test Value: ");
 		JLabel result = new JLabel("Result: ");
 		resultOutput = new JLabel();
 		
 		testValueInput.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				String t = testValueInput.getText();
 				t = evaluate(t);
 				resultOutput.setText(t);		
 			}
 		});
 		testValueInput.getAccessibleContext().setAccessibleName(testValue.getText());
 		
 		
 		//Add table of expressions
 		expressionsTable = new JTable(expModel = new ExpressionsModel());
 		expressionsTable.setAutoCreateColumnsFromModel(true);
 		expressionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
 		expressionsTable.setRowSelectionAllowed(true);
 		setOpColumn(expressionsTable, expressionsTable.getColumnModel().getColumn(0));
 		JScrollPane expressionsTableScrollPane = new JScrollPane(expressionsTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 				
 		GridBagConstraints eTableConstraints = getConstraints(0,2);
 		eTableConstraints.gridwidth = 3;
 		eTableConstraints.fill = GridBagConstraints.HORIZONTAL;
 		eTableConstraints.gridheight = 7;
 		eTableConstraints.insets = new Insets(1,9,9,9);
 		eTableConstraints.weightx = 1;
 		eTableConstraints.weighty = .7;
 		add(expressionsTableScrollPane, eTableConstraints);
 		
 		//Add table of telemetry elements
 		telemetryTable = new JTable(telModel = new TelemetryModel());
 		telemetryTable.setAutoCreateColumnsFromModel(true);
 		telemetryTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
 		telemetryTable.setRowSelectionAllowed(true);
 		telemetryTable.setAutoCreateRowSorter(true);
 		JScrollPane telemetryTableScrollPane = new JScrollPane(telemetryTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		
 		if (componentBorder != null) {
 			expressionsTableScrollPane.setBorder(componentBorder);
 			telemetryTableScrollPane.setBorder(componentBorder);
 			testValueInput.setBorder(componentBorder);
 		}
 		
 		GridBagConstraints tTableConstraints = getConstraints(0,13);
 		tTableConstraints.gridwidth = 3;
 		tTableConstraints.gridheight = 1;
 		tTableConstraints.insets = new Insets(1,9,9,9);
 		tTableConstraints.weightx = 1;
 		tTableConstraints.weighty = .3;
 		tTableConstraints.anchor = GridBagConstraints.WEST;
 		tTableConstraints.fill = GridBagConstraints.HORIZONTAL;
 		add(telemetryTableScrollPane, tTableConstraints);
 	
 		//Add test value + results
 		GridBagConstraints tValueConstraints = getConstraints(0,0);
 		tValueConstraints.insets = new Insets(9,9,2,1);
 		add(testValue, tValueConstraints);
 		
 		GridBagConstraints tValueInputConstraints = getConstraints(1,0);
 		tValueInputConstraints.insets = new Insets(9,1,2,9);
 		add(testValueInput, tValueInputConstraints);
 		
 		GridBagConstraints rConstraints = getConstraints(0,1);
 		rConstraints.insets = new Insets(3,9,5,1);
 		add(result, rConstraints);
 		
 		GridBagConstraints rOutputConstraints = getConstraints(1,1);
 		rOutputConstraints.insets = new Insets (3,1,5,9);
 		add(resultOutput, rOutputConstraints);
 	}
 
 	/**
 	 * Combo box for operator choices in a table column.
 	 * @param table the JTable.
 	 * @param opCol the table column.
 	 */
 	public void setOpColumn(JTable table, TableColumn opCol) {
 		JComboBox selector = new JComboBox();
 		selector.addItem("=");
 		selector.addItem("<");
 		selector.addItem(">");
 		selector.addItem(EnumEvaluator.NOT_EQUALS);
 		opCol.setCellEditor(new DefaultCellEditor(selector));
 	}
 	
 	private GridBagConstraints getConstraints(int x, int y) {
 		GridBagConstraints gbc = new GridBagConstraints();
 		gbc.gridx = x;
 		gbc.gridy = y;
 		gbc.weightx = x == 0 ? 0 : 1;
 		gbc.weighty = 0;
 		gbc.insets = new Insets(0,9,4,9);
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.anchor = GridBagConstraints.NORTHWEST;
 		return gbc;
 	}
 	
 	/**
 	 * Evaluates the result.
 	 * @param value the string value.
 	 * @return the evaluated result.
 	 */
 	public String evaluate(String value){
 		EnumEvaluator e = new EnumEvaluator();
 		e.compile(ec.getData().getCode());
 		return e.evaluate(value);
 	}
 	
 	/**
 	 * Expressions table model.
 	 */
 	@SuppressWarnings("unchecked")
 	class ExpressionsModel extends AbstractTableModel {
 		private ExpressionList eList = currentExpressions;
 		private List<String> ops = new ArrayList<String>();
 		private List<String> values = new ArrayList<String>();
 		private List<String> displays = new ArrayList<String>();
 		private List<List<String>> lists = Arrays.asList(ops, values, displays);
 		private final List<String> columnNames = new ArrayList<String>();
 		
 		/**
 		 * Initializes the expression model.
 		 */
 		ExpressionsModel(){
 			columnNames.add(bundle.getString("OpLabel"));
 			columnNames.add(bundle.getString("ValueLabel"));
 			columnNames.add(bundle.getString("DisplayLabel"));
 			loadExpressions();
 		}
 		
 		@Override
 		public int getColumnCount() {
 			return columnNames.size();
 		}
 		
 		@Override
 		public int getRowCount() {
 			return eList.size();
 		}
 		
 		@Override
 		public boolean isCellEditable(int row, int col) {
 			return true;
 		}
 		
 		@Override 
 		public Class<?> getColumnClass(int columnIndex) {
 			return columnIndex == 1 ? Double.class : String.class;
 		}
 		
 		@Override
 		public String getColumnName(int column) {
 			return columnNames.get(column);
 		}
 		
 		@Override
 		public Object getValueAt(int rowIndex, int columnIndex){
 			List<String> list = getListForColumn(columnIndex);
 			String value = list.get(rowIndex);
 			return columnIndex == 1 ? Double.valueOf(value) : value;
 		}
 	
 		private List<String> getListForColumn(int col){
 			return lists.get(col);
 		}
 		
 		@Override
		public void setValueAt(Object value, int row, int col) {
			// The row value can be obsolete because this method is called in the 
			// EDT for persisting changes from the previous selection.
			if (row >= eList.size())
				return;
			
 			if (col==0){
 				eList.getExp(row).setOperator(value.toString());
 			}
 			if (col==1 && value != null){
 				eList.getExp(row).setVal(Double.valueOf(value.toString()));
 			}
 			if (col==2){
 				eList.getExp(row).setDisplay(value.toString());
 			}
 			ec.getData().setCode(eList.toString());
 			fireTableCellUpdated(row, col);
 			fireFocusPersist();
 		}	
 		
 		private void loadExpressions(){
 			Expression e;
 			for (int i = 0; i < eList.size(); i++){
 				e = eList.getExp(i);
 				ops.add(e.getOperator());
 				values.add(Double.toString(e.getVal()));
 				displays.add(e.getDisplay());
 			}
 		}
 		
 		/**
 		 * Clears the model.
 		 */
 		public void clearModel() {
 			ops.clear();
 			values.clear();
 			displays.clear();
 			loadExpressions();
 			fireTableDataChanged();
 		}
 	}
 	
 	/**
 	 * Telemetry model.
 	 */
 	@SuppressWarnings("unchecked")
 	class TelemetryModel extends AbstractTableModel{
 		private List<String> pui = new ArrayList<String>();
 		private List<String> baseDisplay = new ArrayList<String>();
 		private List<List<String>> lists = Arrays.asList(pui, baseDisplay);
 		private final List<String> columnNames = new ArrayList<String>();
 		
 		/**
 		 * Initializes the telemetry model.
 		 */
 		TelemetryModel(){
 			columnNames.add(bundle.getString("PuiLabel"));
 			columnNames.add(bundle.getString("BaseDisplayLabel"));
 			loadTelemetry();
 		}
 		
 		@Override
 		public int getColumnCount() {
 			return columnNames.size();
 		}
 		
 		@Override
 		public int getRowCount() {
 			return telemetryElements.size();
 		}
 		
 		@Override
 		public boolean isCellEditable(int row, int col) {
 			return false;
 		}
 		
 		@Override 
 		public Class<?> getColumnClass(int columnIndex) {
 			return String.class;
 		}
 		
 		@Override
 		public String getColumnName(int column) {
 			return columnNames.get(column);
 		}
 		
 		@Override
 		public Object getValueAt(int rowIndex, int columnIndex){
 			List<String> list = getListForColumn(columnIndex);
 			return list.get(rowIndex);
 		}
 	
 		private List<String> getListForColumn(int col){
 			return lists.get(col);
 		}
 		
 		/**
 		 * Loads the telemetry.
 		 */
 		protected void loadTelemetry(){
 			refreshTelemetry();
 			for (AbstractComponent ac : telemetryElements){
 				pui.add(ac.getExternalKey());
 				baseDisplay.add(ac.getDisplayName());
 			}
 		}
 		
 		/**
 		 * Clears the telemetry model.
 		 */
 		public void clearModel() {
 			pui.clear();
 			baseDisplay.clear();
 			loadTelemetry();
 			fireTableDataChanged();
 		}
 	}
 	
 	@Override
 	protected JComponent initializeControlManifestation() {
 		//Set canvas control
 		this.controlPanel = new ExpressionsFormattingControlsPanel(this);
 		Dimension d = controlPanel.getMinimumSize();
 		d.setSize(0,0);
 		controlPanel.setMinimumSize(d);
 		
 		JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
 		pane.setOneTouchExpandable(true);
 		pane.setBorder(BorderFactory.createEmptyBorder());
 		JScrollPane controlScrollPane = new JScrollPane(controlPanel,
 				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
 				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		return controlScrollPane;
 	}			
 	
 	/**
 	 * Gets the selected expression.
 	 * @return the expression.
 	 */
 	public Expression getSelectedExpression(){
 		int row = expressionsTable.getSelectedRowCount();
 		if (row == 1){
 			selectedExpression = currentExpressions.getExp(expressionsTable.getSelectedRow());
 		}
 		else {
 			selectedExpression = null;
 		}
 		return selectedExpression;
 	}
 	
 	/**
 	 * Gets the selected telemetry.
 	 * @return the component.
 	 */
 	public AbstractComponent getSelectedTelemetry(){
 		int row = telemetryTable.getSelectedRowCount();
 		if (row == 1){
 			selectedTelemetry = telemetryElements.get(telemetryTable.getSelectedRow());
 		}
 		else {
 			selectedTelemetry = null;
 		}
 		return selectedTelemetry;
 	}
 	
 	/**
 	 * Saves the manifested component.
 	 */
 	public void fireFocusPersist(){
 		if (!isLocked()) {
 			getManifestedComponent().save();
 			updateMonitoredGUI();
 		}
 	}
 	
 	/**
 	 * Fires the selection property changes.
 	 */
 	public void fireManifestationChanged() {
 		firePropertyChange(SelectionProvider.SELECTION_CHANGED_PROP, null, getSelectedExpression());
 	}
 	
 	@Override
 	public void updateMonitoredGUI(){
 		((ExpressionsModel)expressionsTable.getModel()).clearModel();
 		((TelemetryModel)telemetryTable.getModel()).clearModel();
 	}
 	
 	@Override
 	public void updateMonitoredGUI(AddChildEvent event) {
 		((TelemetryModel)telemetryTable.getModel()).clearModel();
 	}
 	
 	@Override
 	public void updateMonitoredGUI(RemoveChildEvent event) {
 		((TelemetryModel)telemetryTable.getModel()).clearModel();
 	}
 	
 }
