 /**
  * <p>Title: ExperimentDataCategoryGridPanel Class>
  * Copyright:    Copyright (c) year
  * Company: Washington University, School of Medicine, St. Louis.
  * @author Hrishikesh Rajpathak/Deepak Shingan/Chetan 
  * @version 1.4
  */
 
 package edu.wustl.cab2b.client.ui.experiment;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.table.TableColumn;
 
 import org.jdesktop.swingx.LinkRenderer;
 import org.jdesktop.swingx.action.LinkAction;
 import org.jdesktop.swingx.decorator.Filter;
 import org.jdesktop.swingx.decorator.FilterPipeline;
 
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.PermissibleValueInterface;
 import edu.common.dynamicextensions.entitymanager.EntityRecordInterface;
 import edu.common.dynamicextensions.entitymanager.EntityRecordResultInterface;
 import edu.wustl.cab2b.client.ui.charts.Cab2bChartPanel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bButton;
 import edu.wustl.cab2b.client.ui.controls.Cab2bComboBox;
 import edu.wustl.cab2b.client.ui.controls.Cab2bHyperlink;
 import edu.wustl.cab2b.client.ui.controls.Cab2bLabel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.controls.Cab2bTable;
 import edu.wustl.cab2b.client.ui.filter.CaB2BFilterInterface;
 import edu.wustl.cab2b.client.ui.filter.CaB2BPatternFilter;
 import edu.wustl.cab2b.client.ui.filter.Cab2bFilterPopup;
 import edu.wustl.cab2b.client.ui.filter.EnumeratedFilterPopUp;
 import edu.wustl.cab2b.client.ui.filter.FilterComponent;
 import edu.wustl.cab2b.client.ui.filter.PatternPopup;
 import edu.wustl.cab2b.client.ui.filter.RangeFilter;
 import edu.wustl.cab2b.client.ui.mainframe.MainFrame;
 import edu.wustl.cab2b.client.ui.util.CommonUtils;
 import edu.wustl.cab2b.client.ui.util.CustomSwingWorker;
 import edu.wustl.cab2b.client.ui.util.UserObjectWrapper;
 import edu.wustl.cab2b.common.domain.Experiment;
 import edu.wustl.cab2b.common.ejb.EjbNamesConstants;
 import edu.wustl.cab2b.common.exception.RuntimeException;
 import edu.wustl.cab2b.common.experiment.ExperimentBusinessInterface;
 import edu.wustl.cab2b.common.experiment.ExperimentHome;
 import edu.wustl.cab2b.common.util.Utility;
 import edu.wustl.common.querysuite.queryobject.DataType;
 
 /**
  * This class displays the experiment table. Also provides filtering tool for
  * the class
  * 
  * @author hrishikesh_rajpathak
  * @author deepak_shingan
  * @author chetan_patil
  */
 public class ExperimentDataCategoryGridPanel extends Cab2bPanel {
     private static final long serialVersionUID = 1L;
 
     private ExperimentOpenPanel parent;
 
     private JTabbedPane tabComponent;
 
     /** Refers to the table in the current tab */
     private Cab2bTable currentTable;
 
     /**
      * Panel to display experiment data when data category node is selected
      */
     private Cab2bPanel experimentDataPanel;
 
     /**
      * Panel to display analysis performed on experiment Second tab panel on tab
      * component
      */
     private Cab2bPanel analysisDataPanel;
 
     /**
      * Reference to the current tab which is displaying the chart diagram.
      */
     private Cab2bPanel currentChartPanel;
 
     /**
      * Table to display records on Experiment Data panels, when user selects any
      * data category node
      */
     private ExperimentTableModel table;
 
     /**
      * Table to display records on Analysis Data panels
      */
     private Cab2bTable analysisTable;
 
     final public String[] ANALYSIS_TABLE_HEADERS = new String[] { "Data Category", "Analysis Type", "Date", "Status" };
 
     private Vector tableColumnVector;
 
     private Vector tableDataRecordVector;
 
     /** Previous button */
     private Cab2bButton prevButton;
 
     /** Button to save data category */
     private Cab2bButton saveDataCategoryButton;
 
     public static ArrayList<String> values = new ArrayList<String>();
 
     private static Map<String, CaB2BFilterInterface> filterMap = new HashMap<String, CaB2BFilterInterface>();
 
     // fields used by Save Data Category functionality
     private EntityInterface dataCategoryEntity;
 
     private String dataCategoryTitle;
 
     private Cab2bLabel filterLabel;
 
     private Cab2bComboBox combo;
 
     public static Map<String, Integer> indexToName = new HashMap<String, Integer>();
 
     public ExperimentDataCategoryGridPanel(ExperimentOpenPanel parent) {
         this(new Vector(), new Vector());
         this.parent = parent;
     }
 
     /**
      * @return the tabComponent
      */
     public JTabbedPane getTabComponent() {
         return tabComponent;
     }
 
     /**
      * @return the table
      */
     public ExperimentTableModel getTable() {
         return table;
     }
 
     /**
      * @return the visualizeDataPanel
      */
     public Cab2bPanel getCurrentChartPanel() {
         return currentChartPanel;
     }
 
     /**
      * @param visualizeDataPanel2
      */
     public void setCurrentChartPanel(Cab2bPanel currentChartPanel) {
         this.currentChartPanel = currentChartPanel;
     }
 
     /**
      * @return the analysisDataPanel
      */
     public Cab2bPanel getAnalysisDataPanel() {
         return analysisDataPanel;
     }
 
     /**
      * @return the analysisTable
      */
     public Cab2bTable getAnalysisTable() {
         return analysisTable;
     }
 
     /**
      * @return the currentTable
      */
     public Cab2bTable getCurrentTable() {
         return currentTable;
     }
 
     /**
      * @param currentTable
      *            the currentTable to set
      */
     public void setCurrentTable(Cab2bTable currentTable) {
         this.currentTable = currentTable;
     }
 
     /**
      * Adding a filter to the filter map
      */
     public static void addFilter(String columnName, CaB2BFilterInterface filter) {
         filterMap.put(columnName, filter);
     }
 
     public static void clearMap() {
         filterMap.clear();
     }
 
     public static Vector<CaB2BFilterInterface> getFilterMap() {
         Vector<CaB2BFilterInterface> vector = new Vector<CaB2BFilterInterface>();
         for (CaB2BFilterInterface filter : filterMap.values()) {
             vector.add(filter);
         }
         return vector;
     }
 
     public ExperimentDataCategoryGridPanel(Vector columnVector, Vector dataRecordVector) {
         tableColumnVector = columnVector;
         tableDataRecordVector = dataRecordVector;
         clearMap();
         initGUI();
     }
 
     /**
      * Building/refreshing the table
      */
     public void refreshTable(Object columnVector[], Object[][] dataRecordVector,
                              Map<String, AttributeInterface> attributeMap) {
         this.removeAll();
         table = new ExperimentTableModel(false, dataRecordVector, columnVector, attributeMap);
         table.addFocusListener(new TableFocusListener(this.parent));
         refreshUI();
         updateUI();
     }
 
     /**
      * This method refreshes analysis table
      * 
      * @param columns
      *            array of column names
      * @param dataRecords
      *            array of record values
      */
     public void refreshAnalysisTable(Object[][] dataRecords) {
         Experiment selectedExperiment = ((ExperimentOpenPanel) this.getParent().getParent()).getSelectedExperiment();
         Cab2bLabel experimentLabel = new Cab2bLabel("Analysis performed for '" + selectedExperiment.getName()
                 + "'");
         Font textFont = new Font(experimentLabel.getFont().getName(), Font.BOLD,
                 experimentLabel.getFont().getSize() + 2);
         experimentLabel.setFont(textFont);
 
         analysisDataPanel.removeAll();
         analysisDataPanel.add("br br", experimentLabel);
 
         analysisTable = new Cab2bTable(false, dataRecords, ANALYSIS_TABLE_HEADERS);
         analysisTable.setRowSelectionAllowed(false);
         analysisTable.setColumnMargin(10);
 
         TableColumn tableColumn = null;
         tableColumn = analysisTable.getColumnModel().getColumn(2);
         tableColumn.setMinWidth(85);
         tableColumn.setMaxWidth(85);
 
         tableColumn = analysisTable.getColumnModel().getColumn(3);
         tableColumn.setMinWidth(75);
         tableColumn.setMaxWidth(75);
 
         JScrollPane jScrollPane = new JScrollPane();
         jScrollPane.getViewport().add(analysisTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                       JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         analysisDataPanel.add("br center hfill vfill", jScrollPane);
 
         if (CommonUtils.getComponentByName(tabComponent, "analysisDataPanel") == null) {
             tabComponent.add("Analysis", analysisDataPanel);
         }
         tabComponent.setSelectedComponent(analysisDataPanel);
 
         TableLinkAction myLinkAction = new TableLinkAction();
         analysisTable.getColumn(1).setCellRenderer(new LinkRenderer(myLinkAction));
         analysisTable.getColumn(1).setCellEditor(new LinkRenderer(myLinkAction));
 
         updateUI();
     }
 
     /**
      * Initializing the GUI. Building the table initially.
      */
     public void initGUI() {
         this.setLayout(new BorderLayout());
 
         tabComponent = new JTabbedPane();
         tabComponent.setBorder(null);
         tabComponent.addChangeListener(new TabSelectListner(this));
         filterLabel = new Cab2bLabel("Apply Filter:");
         filterLabel.setFont(new Font("Arial", Font.BOLD, 13));
         experimentDataPanel = new Cab2bPanel();
         experimentDataPanel.setName("experimentDataPanel");
         experimentDataPanel.setBorder(null);
         table = new ExperimentTableModel(false, tableDataRecordVector, tableColumnVector);
         table.addFocusListener(new TableFocusListener(this.parent));
 
         analysisDataPanel = new Cab2bPanel();
         analysisDataPanel.setName("analysisDataPanel");
         analysisDataPanel.setBorder(null);
 
         saveDataCategoryButton = new Cab2bButton("Save Data Category");
         saveDataCategoryButton.setPreferredSize(new Dimension(160, 22));
         saveDataCategoryButton.addActionListener(new SaveCategoryActionListener(this));
 
         prevButton = new Cab2bButton("Previous");
         prevButton.setEnabled(false);
         this.setBorder(null);
 
         filterLabel.setPreferredSize(new Dimension(100, 25));
 
         refreshUI();
     }
 
     /**
      * This method is common among the different UI initialization methods
      */
     private void refreshUI() {
         int columnCount = table.getModel().getColumnCount();
         combo = new Cab2bComboBox();
         for (int i = 0; i < columnCount; i++) {
             String colName = table.getModel().getColumnName(i);
             combo.addItem(colName);
             indexToName.put(colName, i);
         }
         combo.setPreferredSize(null);
         combo.addItemListener(new ItemListener() {
             public void itemStateChanged(ItemEvent ie) {
                 if (ie.getStateChange() == ItemEvent.SELECTED) {
                     String columnName = ie.getItem().toString();
                     int columnIndex = indexToName.get(columnName);
                     CaB2BFilterInterface oldFilter = null;
                     if (filterMap.containsKey(columnName)) {
                         oldFilter = filterMap.get(columnName);
                     }
 
                     Cab2bFilterPopup filterPopup = null;
                     AttributeInterface attribute = table.getColumnAttribute(columnName);
                     if (Utility.isEnumerated(attribute)) {
                         Collection<PermissibleValueInterface> permissibleValueCollection = Utility.getPermissibleValues(attribute);
                         filterPopup = new EnumeratedFilterPopUp(columnName, columnIndex,
                                 permissibleValueCollection, (CaB2BPatternFilter) oldFilter);
                     } else {
                         DataType dataType = Utility.getDataType(attribute.getAttributeTypeInformation());
                         // If the clicked column is of type String.
                         if (DataType.String == dataType) {
                             filterPopup = new PatternPopup((CaB2BPatternFilter) oldFilter, columnName, columnIndex);
                             // If the clicked column is of type int/long
                         } else if (DataType.Long == dataType || DataType.Integer == dataType
                                 || DataType.Double == dataType || DataType.Float == dataType) {
                             int len = table.getRowCount();
                             double columnVal[] = null;
                             if (oldFilter == null) {
                                 columnVal = new double[len];
                                 for (int i = 0; i < len; i++) {
                                     String val = (String) table.getValueAt(i, columnIndex);
                                     if (val != null) {
                                         double f = Double.parseDouble(val);
                                         columnVal[i] = f;
                                     }
                                 }
                             }
 
                             filterPopup = new FilterComponent("Range Filter", columnVal, columnName, columnIndex,
                                     (RangeFilter) oldFilter);
                         }
                     }
                     filterPopup.showInDialog();
                     applyFilter();
                 }
             }
 
             public void applyFilter() {
                 int len = filterMap.size();
                 Filter[] filters = new Filter[len];
                 int i = 0;
                 for (CaB2BFilterInterface filter : filterMap.values()) {
                     filters[i] = (Filter) filter.copy();
                     i++;
                 }
                 table.setFilters(new FilterPipeline(filters));
             }
         });
 
         experimentDataPanel.removeAll();
         experimentDataPanel.setBorder(null);
         experimentDataPanel.add(new JLabel(""));
         experimentDataPanel.add("tab ", filterLabel);
         experimentDataPanel.add(combo);
         experimentDataPanel.add("br ", new JLabel(""));
         JScrollPane jScrollPane = addTableToScrollPanel(table);
         experimentDataPanel.add("br hfill vfill ", jScrollPane);
 
         Cab2bPanel northPanel = new Cab2bPanel();
         northPanel.add(saveDataCategoryButton);
         northPanel.setBorder(null);
         this.add(northPanel, BorderLayout.NORTH);
 
         tabComponent.add("Experiment Data", experimentDataPanel);
         //currentTable = table;
         this.add(tabComponent, BorderLayout.CENTER);
         // this.add(filterbutton, BorderLayout.CENTER);
         Cab2bPanel bottomPanel = new Cab2bPanel();
         bottomPanel.add(prevButton);
         bottomPanel.setBorder(null);
         this.add(bottomPanel, BorderLayout.SOUTH);
     }
 
     /**
      * This method configures the given table for the filters and adds it to the
      * JScrollPane.
      * 
      * @param table
      *            table to be configured
      * @return JScrollPane having the given table
      */
     private JScrollPane addTableToScrollPanel(Cab2bTable table) {
         table.setColumnSelectionAllowed(true);
         JScrollPane jScrollPane = new JScrollPane();
         jScrollPane.getViewport().add(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                       JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         jScrollPane.setName("jScrollPane");
         jScrollPane.setBorder(null);
         return jScrollPane;
     }
 
     /**
      * This method adds a dynamic tab that displays the analyzed data.
      * 
      * @param userObjectWrapper
      *            the analyzed data
      */
     final private void addAnalysisViewTabPanel(UserObjectWrapper<EntityRecordResultInterface> userObjectWrapper) {
         EntityRecordResultInterface entityRecordResult = userObjectWrapper.getUserObject();
         String tabTitle = userObjectWrapper.getDisplayName();
 
         Cab2bPanel analysisView = (Cab2bPanel) CommonUtils.getComponentByName(tabComponent, tabTitle);
         if (analysisView == null) {
             List<AbstractAttributeInterface> headerList = entityRecordResult.getEntityRecordMetadata().getAttributeList();
             String[] columnNames = generateColumnNames(headerList);
 
             // getting actual records
             List<EntityRecordInterface> recordList = entityRecordResult.getEntityRecordList();
             Object[][] dataRecordArray = generateGridData(recordList);
 
             final Cab2bPanel analysisViewPanel = new Cab2bPanel();
             analysisViewPanel.setName(tabTitle);
             analysisViewPanel.setBorder(null);
 
             ExperimentTableModel analysisDataTable = new ExperimentTableModel(false, dataRecordArray, columnNames);
             analysisDataTable.addFocusListener(new TableFocusListener(this.parent));
             JScrollPane jScrollPane = addTableToScrollPanel(analysisDataTable);
             analysisViewPanel.add("br br center hfill vfill", jScrollPane);
 
             Cab2bButton closeButton = new Cab2bButton("Close");
             closeButton.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent actionEvent) {
                     tabComponent.remove(analysisViewPanel);
                 }
             });
             analysisViewPanel.add("br right", closeButton);
 
             analysisView = analysisViewPanel;
             tabComponent.add(tabTitle, analysisViewPanel);
         }
         tabComponent.setSelectedComponent(analysisView);
     }
 
     /**
      * This method returns the column names given the list of the attribute list
      * 
      * @param headerList
      *            list of the attribute list
      * @return array of column names
      */
     private String[] generateColumnNames(List<AbstractAttributeInterface> headerList) {
         String[] columnNames = new String[headerList.size()];
         Map<String, AttributeInterface> attributeMap = new HashMap<String, AttributeInterface>();
         int i = 0;
         for (AbstractAttributeInterface abstractAttribute : headerList) {
             AttributeInterface attribute = (AttributeInterface) abstractAttribute;
             columnNames[i] = CommonUtils.getFormattedString(attribute.getName());
             attributeMap.put(columnNames[i++], attribute);
         }
 
         return columnNames;
     }
 
     /**
      * This method returns the array of data records given the List of
      * EntityRecord
      * 
      * @param recordList
      *            List of EntityRecord
      * @param numOfColumns
      *            number of columns in a
      * @return
      */
     private Object[][] generateGridData(List<EntityRecordInterface> recordList) {
         Object[][] dataRecordArray = new Object[recordList.size()][];
         int i = 0, j = 0;
         for (EntityRecordInterface record : recordList) {
             List recordValueList = record.getRecordValueList();
             dataRecordArray[i] = new Object[recordValueList.size()];
             j = 0;
             for (Object object : recordValueList) {
                 dataRecordArray[i][j] = new Object();
                 dataRecordArray[i][j] = object;
                 j++;
             }
             i++;
         }
 
         return dataRecordArray;
     }
 
     /**
      * Make list of attributes of the parent entity as well as a 2D array of
      * data and pass it to the server to make new entity this method is called
      * by the {@link SaveDataCategoryPanel} to save a subset of of a datalist as
      * a category
      * 
      * @param title
      *            the title for the category
      */
     public void saveDataCategory(String title) {
         dataCategoryTitle = title;
 
         MainFrame.setStatus(MainFrame.Status.BUSY);
         MainFrame.setStatusMessage("saving data category '" + title + "'");
 
         CustomSwingWorker swingWorker = new CustomSwingWorker(MainFrame.openExperimentWelcomePanel) {
             protected void doNonUILogic() throws RuntimeException {
                 // get the columns, including hidden columns. Not well
                 // documented
                 List<TableColumn> columnList = table.getColumns(true);
                 List<AttributeInterface> attributes = new ArrayList<AttributeInterface>();
 
                 // skip filterd-out rows but include hidden columns
                 int rows = table.getRowCount();
                 int cols = table.getColumnCount(true);
                 Object[][] data = new Object[rows][cols];
 
                 // populate attribute list
                 for (TableColumn column : columnList) {
                     String columnName = column.getIdentifier().toString();
                     attributes.add(table.getColumnAttribute(columnName));
                 }
 
                 for (int i = 0; i < rows; i++) {
                     for (int j = 0; j < cols; j++) {
                         // JXTable does not provide API to access hidden data
                         // JXTable.getValueAt() works only on visible columns
                         // using TableModel.getValueAt() requires converting row
                         // view index to row model index
                         data[i][j] = table.getModel().getValueAt(table.convertRowIndexToModel(i), j);
                     }
                     // Logger.out.debug(table.getValueAt(i, 4).toString());
                 }
 
                 // make a call to the server
                 ExperimentBusinessInterface bi = (ExperimentBusinessInterface) CommonUtils.getBusinessInterface(
                                                                                                                 EjbNamesConstants.EXPERIMENT,
                                                                                                                 ExperimentHome.class);
 
                 try {
                     dataCategoryEntity = bi.saveDataCategory(dataCategoryTitle, attributes, data);
                 } catch (RemoteException e) {
                     CommonUtils.handleException(e, MainFrame.newWelcomePanel, true, true, true, false);
                 }
             }
 
             protected void doUIUpdateLogic() throws RuntimeException {
                 // update the tree in the stack box
                 parent.updateOpenPanel(dataCategoryEntity);
                 MainFrame.setStatus(MainFrame.Status.READY);
                 MainFrame.setStatusMessage(dataCategoryTitle + " saved");
             }
         };
         swingWorker.start();
     }
 
     class SaveCategoryActionListener implements ActionListener {
         private ExperimentDataCategoryGridPanel gridPanel;
 
         public SaveCategoryActionListener(ExperimentDataCategoryGridPanel gridPanel) {
             this.gridPanel = gridPanel;
         }
 
         public void actionPerformed(ActionEvent event) {
             SaveDataCategoryPanel saveDialogPanel = new SaveDataCategoryPanel(gridPanel);
         }
     }
 
     /**
      * Class used for adding hyperlinks in Jtable rows
      */
     class TableLinkAction extends LinkAction {
         private static final long serialVersionUID = 1L;
 
         public TableLinkAction() {
         }
 
         public void actionPerformed(ActionEvent actionEvent) {
             setVisited(true);
             MainFrame.setStatus(MainFrame.Status.READY);
             MainFrame.setStatusMessage("Displaying the analyzed data");
 
             // getting the selected hyperlink row
             int selectionIndex = analysisTable.getSelectionModel().getLeadSelectionIndex();
 
             // Get the EntityRecordResultInterface object associated with
             // hyperlink at column 1
             final UserObjectWrapper<EntityRecordResultInterface> userObjectWrapper = (UserObjectWrapper<EntityRecordResultInterface>) analysisTable.getValueAt(
                                                                                                                                                                selectionIndex,
                                                                                                                                                                1);
 
             addAnalysisViewTabPanel(userObjectWrapper);
         }
     }
 
     /**
      * This class sets the current table on the change of the tab.
      * 
      * @author chetan_patil
      */
     class TabSelectListner implements ChangeListener {
         ExperimentDataCategoryGridPanel experimentDataCategoryGridPanel;
 
         TabSelectListner(ExperimentDataCategoryGridPanel experimentDataCategoryGridPanel) {
             this.experimentDataCategoryGridPanel = experimentDataCategoryGridPanel;
         }
 
         public void stateChanged(ChangeEvent changeEvent) {
             JTabbedPane jTabbedPane = (JTabbedPane) changeEvent.getSource();
             Cab2bPanel selectedTabPanel = (Cab2bPanel) jTabbedPane.getSelectedComponent();
             if (selectedTabPanel != null && selectedTabPanel.getComponentCount() > 0) {
                 Component component = CommonUtils.getComponentByName(selectedTabPanel, "jScrollPane");
                 if (component instanceof JScrollPane) {
                     JScrollPane jScrollPane = (JScrollPane) component;
                     Cab2bTable currentTable = (Cab2bTable) jScrollPane.getViewport().getComponent(0);
                     experimentDataCategoryGridPanel.setCurrentTable(currentTable);
                     experimentDataCategoryGridPanel.setCurrentChartPanel(null);
                } else if (component instanceof Cab2bChartPanel) {
                    experimentDataCategoryGridPanel.setCurrentChartPanel(selectedTabPanel);
                 }
             }
             updateUI();
         }
 
     }
 
     /**
      * This class enables or disables the chart links in Visualise Panel on
      * selection of row in the data grid.
      */
     class TableFocusListener implements FocusListener {
         private ExperimentOpenPanel experimentOpenPanel;
 
         TableFocusListener(ExperimentOpenPanel experimentOpenPanel) {
             this.experimentOpenPanel = experimentOpenPanel;
         }
 
         public void focusGained(FocusEvent focusEvent) {
             final Cab2bTable cab2bTable = (Cab2bTable) focusEvent.getComponent();
             focusAction(cab2bTable);
         }
 
         public void focusLost(FocusEvent focusEvent) {
             final Cab2bTable cab2bTable = (Cab2bTable) focusEvent.getComponent();
             focusAction(cab2bTable);
         }
 
         private void focusAction(final Cab2bTable cab2bTable) {
             boolean setEnabled = false;
 
             if (cab2bTable.getSelectedRowCount() > 0) {
                 setEnabled = true;
             }
 
             ExperimentStackBox experimentStackBox = experimentOpenPanel.getExperimentStackBox();
             Cab2bPanel visualiseDataPanel = experimentStackBox.getVisualiseDataPanel();
             Component[] components = visualiseDataPanel.getComponents();
             for (Component component : components) {
                 if (component instanceof Cab2bHyperlink) {
                     Cab2bHyperlink hyperLink = (Cab2bHyperlink) component;
                     hyperLink.setEnabled(setEnabled);
                     hyperLink.setVisible(true);
                 }
             }
             visualiseDataPanel.revalidate();
         }
 
     }
 
 }
