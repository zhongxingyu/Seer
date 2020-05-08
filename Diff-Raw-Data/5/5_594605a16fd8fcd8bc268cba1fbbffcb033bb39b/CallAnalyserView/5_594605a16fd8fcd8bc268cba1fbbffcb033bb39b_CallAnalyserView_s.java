 package org.amanzi.awe.views.calls.views;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.refractions.udig.project.IMap;
 import net.refractions.udig.project.ui.ApplicationGIS;
 
 import org.amanzi.awe.catalog.neo.NeoCatalogPlugin;
 import org.amanzi.awe.catalog.neo.upd_layers.events.ChangeSelectionEvent;
 import org.amanzi.awe.statistic.CallTimePeriods;
 import org.amanzi.awe.views.calls.CallAnalyserPlugin;
 import org.amanzi.awe.views.calls.ExportSpreadsheetWizard;
 import org.amanzi.awe.views.calls.Messages;
 import org.amanzi.awe.views.calls.enums.AggregationCallTypes;
 import org.amanzi.awe.views.calls.enums.AggregationStatisticsHeaders;
 import org.amanzi.awe.views.calls.enums.IStatisticsHeader;
 import org.amanzi.awe.views.calls.enums.InclInconclusiveStates;
 import org.amanzi.awe.views.calls.enums.StatisticsCallType;
 import org.amanzi.awe.views.calls.enums.StatisticsType;
 import org.amanzi.awe.views.calls.statistics.CallStatistics;
 import org.amanzi.integrator.awe.AWEProjectManager;
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.database.services.events.ShowPreparedViewEvent;
 import org.amanzi.neo.core.database.services.events.UpdateDrillDownEvent;
 import org.amanzi.neo.core.enums.CallProperties;
 import org.amanzi.neo.core.enums.ColoredFlags;
 import org.amanzi.neo.core.enums.DriveTypes;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.ProbeCallRelationshipType;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.ActionUtil;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.core.utils.Pair;
 import org.apache.log4j.Logger;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableColorProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.TableCursor;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DateTime;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.part.ViewPart;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.TraversalPosition;
 import org.neo4j.graphdb.Traverser;
 import org.neo4j.graphdb.Traverser.Order;
 import org.rubypeople.rdt.core.IRubyProject;
 import org.rubypeople.rdt.internal.ui.wizards.NewRubyElementCreationWizard;
 
 /**
  * <p>
  * Call Analyser view
  * </p>
  * 
  * @author Cinkel_A
  * @since 1.0.0
  */
 public class CallAnalyserView extends ViewPart {
     private static final Logger LOGGER = Logger.getLogger(CallAnalyserView.class);
     /** String DRIVE_ID field */
     private static final String DRIVE_ID = "org.amanzi.awe.views.tree.drive.views.DriveTreeView";
     /** String ERROR_VALUE field */
     private static final String ERROR_VALUE = Messages.CAV_ERROR_VALUE;
     private static final String ALL_VALUE = Messages.CAV_ALL_VALUE;
     // row labels
     private static final String LBL_DRIVE = Messages.CAV_LBL_DRIVE;
     private static final String LBL_PROBE = Messages.CAV_LBL_PROBE;
     private static final String LBL_PERIOD = Messages.CAV_LBL_PERIOD;
     private static final String LBL_CALL_TYPE = Messages.CAV_LBL_CALL_TYPE;
     private static final String LBL_START_TIME = Messages.CAV_LBL_START_TIME;
     private static final String LBL_END_TIME = Messages.CAV_LBL_END_TIME;
     private static final String LB_EXPORT = Messages.CAV_LB_EXPORT;
     private static final String LB_INCONCLUSIVE = Messages.CAV_LB_INCONCLUSIVE;
     private static final String LB_REPORT = Messages.CAV_LB_REPORT;
 
     // column name
     private static final String COL_PERIOD = Messages.CAV_COL_PERIOD;
     private static final String COL_HOST = Messages.CAV_COL_HOST;
 
     /**
      * The ID of the view as specified by the extension.
      */
     public static final String ID = "org.amanzi.awe.views.calls.views.CallAnalyserView";
 
     private static final int MIN_FIELD_WIDTH = 150;
     private static final int MIN_COLUMN_WIDTH = 220;
     public static final int DEF_SIZE = 100;
     private static final String KEY_ALL = ALL_VALUE;
     public static final int MAX_TABLE_LEN = 500;
     
     private List<ColumnHeaders> columnHeaders = new ArrayList<ColumnHeaders>();
     private LinkedHashMap<String, Node> callDataset = new LinkedHashMap<String, Node>();
     private LinkedHashMap<String, Node> probeCallDataset = new LinkedHashMap<String, Node>();
 
     private TableViewer tableViewer;        
     private ViewContentProvider provider;
     private ViewLabelProvider labelProvider;
     private Combo cDrive;
     private Combo cPeriod;
     private Combo cCallType;
     private Combo cProbe;
     private Combo cInclInconculsiveState;
     private TableCursor cursor;
     private Button bExport;
     private Color color1;
     private Color color2;
     private Comparator<PeriodWrapper> comparator;
     private List<Integer> sortedColumns = new LinkedList<Integer>();
     private Composite frame;
     private Composite parent;
     private DateTime dateStart;
     private DateTime timeStart;
     private DateTime dateEnd;
     private DateTime timeEnd;
     //private Button bInclInconclusive;
     private Button bReport;
     private Button bUpdate;
 
     
     private enum SortOrder{
         NONE("icons/None.gif"),
         ASC("icons/Asc.png"),
         DESC("icons/Desc.png");
         private String iconPath;
         
         private SortOrder(String icon) {
             iconPath = icon;
         }
         
         /**
          * @return Returns the iconPath.
          */
         public String getIconPath() {
             return iconPath;
         }
     }
 
     /*
      * The content provider class is responsible for providing objects to the view. It can wrap
      * existing objects in adapters or simply return objects as-is. These objects may be sensitive
      * to the current input of the view, or ignore it and always show the same content (like Task
      * List, for example).
      */
 
     class ViewContentProvider implements IStructuredContentProvider {
         private List<PeriodWrapper> elements = new ArrayList<PeriodWrapper>();
 
         public void inputChanged(Viewer v, Object oldInput, Object newInput) {
             elements.clear();
             if (newInput == null || !(newInput instanceof InputWrapper)) {
                 return;
             }
             InputWrapper inputWr = (InputWrapper)newInput;
             if (!inputWr.isCorrectInput()) {
                 return;
             }
             GraphDatabaseService service = NeoServiceProvider.getProvider().getService();
             InclInconclusiveStates inclInconclusive = getInclInconclusive();
             Traverser sRowTraverser = inputWr.getSrowTraverser(service,inclInconclusive);
             Transaction tx = service.beginTx();
             try {                
                 if (sRowTraverser != null) {
                     StatisticsCallType callType = getCallType();
                     for (Node sRow : sRowTraverser) {
                         if (isRowInTime(sRow)) {
                             elements.add(new PeriodWrapper(sRow, callType,!inclInconclusive.equals(InclInconclusiveStates.EXCLUDE)));
                         }
                     }
                 }
             } finally {
                 tx.finish();
             }
             sort();
 
         }
         
         private boolean isRowInTime(Node row){
             Long start = getStartTime();
             Long end = getEndTime();
             Long time = (Long)row.getProperty(INeoConstants.PROPERTY_TIME_NAME, null);            
             return (time!=null)&&((start==null||start<=time)&&(end==null||time<=end));
         }
 
         public void dispose() {
         }
 
         public Object[] getElements(Object parent) {
             return elements.toArray(new PeriodWrapper[0]);
         }
 
         /**
          *sort rows
          */
         public void sort() {
             Collections.sort(elements, comparator);
             if (elements.isEmpty()) {
                 return;
             }
             Color color = color1;
             PeriodWrapper element = elements.get(0);
             element.setColor(color);
             for (int i = 1; i < elements.size(); i++) {
                 element = elements.get(i);
                 if (comparator.compare(elements.get(i - 1), element) != 0) {
                     color = color == color1 ? color2 : color1;
                 }
                 element.setColor(color);
             }
         }
     }
 
     /**
      * <p>
      * Table Label provider
      * </p>
      * 
      * @author Cinkel_A
      * @since 1.0.0
      */
     class ViewLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider {
 
         private List<TableColumn> columns = new ArrayList<TableColumn>();
 
         public String getColumnText(Object obj, int index) {
             if (obj instanceof PeriodWrapper && index<columnHeaders.size()) {
                 PeriodWrapper period = (PeriodWrapper)obj;
                 return columnHeaders.get(index).getValue(period, index);
             } else {
                 return getText(obj);
             }
         }
 
         public Image getColumnImage(Object obj, int index) {
             return null;
         }
 
         /**
          *create column of table init label provider of tibleView
          */
         public void createTableColumn() {
             Table tabl = tableViewer.getTable();
             TableViewerColumn column;
             TableColumn col;
 
             if (columnHeaders.isEmpty()) {
                 column = new TableViewerColumn(tableViewer, SWT.LEFT);
                 col = column.getColumn();
                 col.setText(COL_PERIOD);
                 columnHeaders.add(new ColumnHeaders(col, null));
                 col.setWidth(DEF_SIZE);
                 col.addSelectionListener(new SelectionListener() {
 
                     @Override
                     public void widgetSelected(SelectionEvent e) {
                         sortedColumns = new LinkedList<Integer>();
                         if (provider != null) {
                             provider.sort();
                         }
                         tableViewer.refresh();
                         tableViewer.getTable().showSelection();
                     }
 
                     @Override
                     public void widgetDefaultSelected(SelectionEvent e) {
                         widgetSelected(e);
                     }
                 });
                 columns.add(col);
                 column = new TableViewerColumn(tableViewer, SWT.LEFT);
                 col = column.getColumn();
                 col.setText(COL_HOST);
                 columnHeaders.add(new ColumnHeaders(col, null));
                 col.setWidth(DEF_SIZE);
                 columns.add(col);
                 column = new TableViewerColumn(tableViewer, SWT.LEFT);
                 col = column.getColumn();
                 col.setText(INeoConstants.PROBE_LA);
                 columnHeaders.add(new ColumnHeaders(col, null));
                 col.setWidth(DEF_SIZE);
                 columns.add(col);
                 column = new TableViewerColumn(tableViewer, SWT.LEFT);
                 col = column.getColumn();
                 col.setText(INeoConstants.PROBE_F);
                 columnHeaders.add(new ColumnHeaders(col, null));
                 col.setWidth(DEF_SIZE);
                 columns.add(col);
                 // TODO move creation of group of single property in one method
                 //
                 StatisticsCallType callType = StatisticsCallType.INDIVIDUAL;
                 for(StatisticsCallType type : StatisticsCallType.values()){
                     if(type.getHeaders().size()>callType.getHeaders().size()){
                         callType = type;
                     }
                 }
                 for (IStatisticsHeader columnHeader : callType.getHeaders()) {
                     column = new TableViewerColumn(tableViewer, SWT.LEFT);
                     col = column.getColumn();             
                     String title = columnHeader.getTitle();
                     GC gc = new GC(col.getParent());
                     col.setText(title);
                     columnHeaders.add(new ColumnHeaders(col, columnHeader));
                     col.setWidth(gc.textExtent(title).x + 20);
                     gc.dispose();
                     columns.add(col);
                 }
             }
             addSotrListeners();
             tabl.setHeaderVisible(true);
             tabl.setLinesVisible(true);
             tableViewer.setLabelProvider(this);
             tableViewer.refresh();
         }
 
         private void addSotrListeners() {
             for (int i=0;i<columnHeaders.size();i++) {
                 final Integer ind = i;
                 final ColumnHeaders currHeader = columnHeaders.get(i);
                 currHeader.getColumn().addSelectionListener(new SelectionListener() {
                     
                     @Override
                     public void widgetSelected(SelectionEvent e) {
                         if(sortedColumns.contains(ind)){
                             sortedColumns.remove(ind);
                         }
                         sortedColumns.add(ind);
                         currHeader.updateSortOrder();
                         if (provider != null) {
                             provider.sort();
                         }
                         tableViewer.refresh();
                         tableViewer.getTable().showSelection();
                     }
                     
                     @Override
                     public void widgetDefaultSelected(SelectionEvent e) {
                         widgetSelected(e);
                     }
                 });
             }
         }
         
         public void updateHeaders(StatisticsCallType callType){
             Table tabl = tableViewer.getTable();
             columnHeaders = new ArrayList<ColumnHeaders>();
             sortedColumns.clear();
             int lastNum = 0;
             columnHeaders.add(new ColumnHeaders(columns.get(lastNum++), null));
             List<IStatisticsHeader> headers = callType.getHeaders();
             if (callType.getLevel().equals(StatisticsCallType.FIRST_LEVEL)) {
                 TableColumn column = columns.get(lastNum++);
                 column.setText(COL_HOST);
                 columnHeaders.add(new ColumnHeaders(column, null));
                 column.setWidth(DEF_SIZE);
                 column = columns.get(lastNum++);
                 column.setText(INeoConstants.PROBE_LA);
                 columnHeaders.add(new ColumnHeaders(column, null));
                 column.setWidth(DEF_SIZE);
                 column = columns.get(lastNum++);
                 column.setText(INeoConstants.PROBE_F);
                 columnHeaders.add(new ColumnHeaders(column, null));
                 column.setWidth(DEF_SIZE);
             }else{
                 headers = getAggregationHeaders();
             }            
             for(IStatisticsHeader header : headers){
                 TableColumn column = columns.get(lastNum++);
                 String title = header.getTitle();
                 GC gc = new GC(column.getParent());
                 column.setText(header.getTitle());
                 columnHeaders.add(new ColumnHeaders(column, header));
                 column.setWidth(gc.textExtent(title).x + 20);
                 gc.dispose();
             }
             for(int i=lastNum; i<columns.size(); i++){
                 TableColumn column = columns.get(i);
                 column.setText("");
                 column.setWidth(0); //hide not needed columns
             }
             addSotrListeners();
             tabl.setHeaderVisible(true);
             tabl.setLinesVisible(true);
             tableViewer.setLabelProvider(this);
             tableViewer.refresh();
         }
 
         @Override
         public Color getBackground(Object element, int columnIndex) {
             if(!(element instanceof PeriodWrapper)||columnHeaders.size()<=columnIndex){
                 return null;
             }
             return columnHeaders.get(columnIndex).getBackgroundColor((PeriodWrapper)element);
         }
 
         @Override
         public Color getForeground(Object element, int columnIndex) {
             if(!(element instanceof PeriodWrapper)||columnHeaders.size()<=columnIndex){
             return null;
         }
             return columnHeaders.get(columnIndex).getForegroundColor((PeriodWrapper)element);
     }
     }
     
     private int simpleCompare(PeriodWrapper o1, PeriodWrapper o2, int column){        
         ColumnHeaders header = columnHeaders.get(column);
         SortOrder order = sortedColumns.isEmpty()?SortOrder.ASC:header.sortOrder;
         if (header == null) {
             return 0;
         }
         Object value1 = header.getValueForSort(o1, column);
         Object value2 = header.getValueForSort(o2, column);
         switch (order) {
         case DESC:
             return compareObjects(value2, value1);
         case ASC:
             return compareObjects(value1, value2);
         default:
             return 0;
         }
         
     }
     
     private int compareObjects(Object value1,Object value2){
         if(value1==null&&value2==null){
             return 0;
         }
         if(value1==null){
             if(value2 instanceof String){
                 value1 = "";
             }
             if(value2 instanceof Number){
                 value1 = 0;
             }
         }
         if(value1 instanceof Number){
             Double num1 = ((Number)value1).doubleValue();
             return num1.compareTo(value2==null?0.0:((Number)value2).doubleValue());
         }
         return value1.toString().compareTo(value2==null?"":value2.toString());
     }
 
 
     /**
      * This is a callback that will allow us to create the viewer and initialize it.
      */
     public void createPartControl(Composite parent) {
         this.parent = parent;
         color1 = new Color(Display.getCurrent(), 240, 240, 240);
         color2 = new Color(Display.getCurrent(), 255, 255, 255);
         sortedColumns = new LinkedList<Integer>();
         comparator = new Comparator<PeriodWrapper>() {
             @Override
             public int compare(PeriodWrapper o1, PeriodWrapper o2) {
                 if(sortedColumns.isEmpty()){
                     return simpleCompare(o1, o2, 0);
                 }
                 int result = 0;
                 int ind = sortedColumns.size();
                 do{
                     ind--;
                     result = simpleCompare(o1, o2, sortedColumns.get(ind));
                 }while(ind>0&&result==0);
                 return result;
             }
         };
         frame = new Composite(parent, SWT.FILL);
         FormLayout formLayout = new FormLayout();
         formLayout.marginHeight = 0;
         formLayout.marginWidth = 0;
         formLayout.spacing = 0;
         frame.setLayout(formLayout);
 
         // create row composite, this is the composite that represents the entire form
         Composite rowComposite = new Composite(frame, SWT.FILL);
         FormData fData = new FormData();
         fData.left = new FormAttachment(0, 0);
         fData.right = new FormAttachment(100, 0);
         rowComposite.setLayoutData(fData);
         FormLayout layout = new FormLayout();
         layout.marginHeight = 2;
         layout.marginWidth = 3;
         rowComposite.setLayout(layout);  
 
         // The first column for dataset and statistics choices
         int width = 3*MIN_COLUMN_WIDTH/4;
         int field_width = 3*MIN_FIELD_WIDTH/4;
         Composite column1 = addColumn(rowComposite, null, width);
         Composite cell1 = createCellComposite(column1, null, width);
         Composite cell2 = createCellComposite(column1, cell1, width);
         cDrive = addSelection(cell1, LBL_DRIVE, field_width);
         cCallType = addSelection(cell2, LBL_CALL_TYPE, field_width);
         
         // The second column for probe and period filtering
         width = 3*MIN_COLUMN_WIDTH/4-20;
         field_width = 3*MIN_FIELD_WIDTH/4-20;
         Composite column2 = addColumn(rowComposite, column1, width);
         cell1 = createCellComposite(column2, null, width);
         cell2 = createCellComposite(column2, cell1, width);
         cProbe = addSelection(cell1, LBL_PROBE, field_width);
         cPeriod = addSelection(cell2, LBL_PERIOD, field_width);
 
         // The third column for time range filtering
         width = 6*MIN_COLUMN_WIDTH/5;
         Composite column3 = addColumn(rowComposite, column2, width);
         cell1 = createCellComposite(column3, null, width);
         cell2 = createCellComposite(column3, cell1, width);
         DateTime[] fields = addDateTimeSelection(cell1, LBL_START_TIME, width);
         dateStart = fields[0];
         timeStart = fields[1];
         fields = addDateTimeSelection(cell2, LBL_END_TIME, width);
         dateEnd = fields[0];
         timeEnd = fields[1];
 
         // The fourth column for additional options, and buttons
         Composite column4 = addColumn(rowComposite, column3, -1);
         cell1 = createCellComposite(column4, null, MIN_COLUMN_WIDTH);
         //((FormData)cell1.getLayoutData()).right = new FormAttachment(100, 0);
         cell2 = createCellComposite(column4, cell1, MIN_COLUMN_WIDTH);
         //((FormData)cell2.getLayoutData()).left = null;
         //((FormData)cell2.getLayoutData()).right = new FormAttachment(100, 0);
 
         cInclInconculsiveState = addSelection(cell1, LB_INCONCLUSIVE, MIN_FIELD_WIDTH);
         cInclInconculsiveState.setItems(InclInconclusiveStates.getAllStatesForSelect());
         cInclInconculsiveState.select(0);
         bUpdate = addButton(cell2, null, null, SWT.PUSH, 32);
         bUpdate.setImage(CallAnalyserPlugin.getImageDescriptor("/icons/refresh.gif").createImage());
         bUpdate.setToolTipText("Refresh table");
         bReport = addButton(cell2, bUpdate, LB_REPORT, SWT.PUSH, -1);
         bReport.setEnabled(false);
         bExport = addButton(cell2, bReport, LB_EXPORT, SWT.PUSH, -1);
 
         // ------- table
         tableViewer = new TableViewer(frame, SWT.BORDER | SWT.FULL_SELECTION);
         fData = new FormData();
         fData.left = new FormAttachment(0, 0);
         fData.right = new FormAttachment(100, 0);
         fData.top = new FormAttachment(rowComposite, 2);
         fData.bottom = new FormAttachment(100, -2);
         tableViewer.getControl().setLayoutData(fData);
         cursor = new TableCursor(tableViewer.getTable(), SWT.DefaultSelection);
 
         hookContextMenu();
         addListeners();
         initialize();
         setDefaultTime();
     }
 
     private Button addButton(Composite cell, Button previous, String text, int buttonType, int width) {
         Button button = new Button(cell, buttonType);
         if(text!=null) button.setText(text);
         FormData fData = new FormData();
         FormAttachment formAttachment = new FormAttachment(0, 2);
         if(previous!=null) formAttachment = new FormAttachment(previous, 2);
         fData.left = formAttachment;
         if(width>0) fData.width = width;
         button.setLayoutData(fData);
         return button;
     }
 
     private Composite addColumn(Composite row, Composite previous_column, int width) {
         Composite column = new Composite(row, SWT.FILL);
         FormData fData = new FormData();
         if(previous_column!=null) fData.left = new FormAttachment(previous_column, 2);
         else fData.left = new FormAttachment(0, 0);
         if(width>0) fData.width = width;
         column.setLayoutData(fData);
         FormLayout layout = new FormLayout();
         layout.marginHeight = 2;
         layout.marginWidth = 3;
         column.setLayout(layout);
         return column;
     }
 
     private DateTime[] addDateTimeSelection(Composite cell, String text, int width) {
         Label label = new Label(cell, SWT.FLAT);
         label.setText(text);
         FormData fData = new FormData();
         fData.left = new FormAttachment(0, 0);
         fData.bottom = new FormAttachment(100,-8);
         label.setLayoutData(fData);
         DateTime date = new DateTime(cell, SWT.FILL | SWT.BORDER | SWT.DATE | SWT.MEDIUM);
         fData = new FormData();
         fData.right = new FormAttachment(95, -width/4-10);
         fData.width = width/3+10;
         date.setLayoutData(fData);
         DateTime time = new DateTime(cell, SWT.FILL | SWT.BORDER | SWT.TIME | SWT.SHORT);
         fData = new FormData();
         fData.left = new FormAttachment(date, 2);
         fData.right = new FormAttachment(100, -2);
         fData.width = width/4;
         time.setLayoutData(fData);
         return new DateTime[]{date,time};
     }
 
     private Combo addSelection(Composite cell, String text, int width) {
         Label label = new Label(cell, SWT.FLAT);
         label.setText(text);
         FormData fData = new FormData();
         fData.left = new FormAttachment(0, 0);
         fData.bottom = new FormAttachment(100,-8);
         label.setLayoutData(fData);
         Combo selection = new Combo(cell, SWT.DROP_DOWN | SWT.READ_ONLY);
         fData = new FormData();
         fData.left = new FormAttachment(label, 2);
         fData.right = new FormAttachment(100, -2);
         //fData.width = width;               
         selection.setLayoutData(fData);
         return selection;
     }
 
     private Composite createCellComposite(Composite column, Composite cell_above, int width) {
         Composite cell = new Composite(column, SWT.FILL);
         FormData fData = new FormData();
         fData.left = new FormAttachment(0, 0);
         fData.height = 32;
         if(cell_above!=null) fData.top = new FormAttachment(cell_above,2);
         if(width>0) fData.width = width;
         cell.setLayoutData(fData);
         FormLayout layout = new FormLayout();
         layout.marginHeight = 2;
         layout.marginWidth = 3;
         cell.setLayout(layout);
         return cell;
     }
     
     private void setTime(DateTime dateFild,DateTime timeFild, Long time){
         Calendar calendar = new GregorianCalendar();
         calendar.setTimeInMillis(time);
         dateFild.setYear(calendar.get(Calendar.YEAR));
         dateFild.setMonth(calendar.get(Calendar.MONTH));
         dateFild.setDay(calendar.get(Calendar.DAY_OF_MONTH));
         timeFild.setHours(calendar.get(Calendar.HOUR_OF_DAY));
         timeFild.setMinutes(0);
         timeFild.setSeconds(0);
     }
     
     private Long getTime(DateTime dateFild,DateTime timeFild){
         Calendar calendar = new GregorianCalendar();
         calendar.setTimeInMillis(0L);
         calendar.set(dateFild.getYear(), dateFild.getMonth(), dateFild.getDay(), 
                 timeFild.getHours(), timeFild.getMinutes());
         return calendar.getTimeInMillis();
     }
 
     /**
      * @param sRow
      */
     protected void select(Node node) {
         //TODO refactor
         InputWrapper wr = (InputWrapper)tableViewer.getInput();
         List<Node> nodes = new ArrayList<Node>(2);
         nodes.add(node);
         nodes.add(wr.periodNode);
         NeoCorePlugin.getDefault().getUpdateViewManager().fireUpdateView(new ShowPreparedViewEvent(DRIVE_ID, nodes));
         NeoCorePlugin.getDefault().getUpdateViewManager().fireUpdateView(new UpdateDrillDownEvent(nodes,CallAnalyserView.ID));
         selectNodesOnMap(node);
     }
 
     /**
      * select nodes on map
      * 
      * @param drive
      * @param nodes nodes to select
      */
     // TODO use selection mechanism!
     private void selectNodesOnMap(Node node) {
         Node drive = callDataset.get(cDrive.getText());
         if (drive == null) {
             return;
         }
         GraphDatabaseService service = NeoServiceProvider.getProvider().getService();
         Set<Node> nodes = new HashSet<Node>();
         if(NeoUtils.isProbeNode(node)){
             nodes = NeoUtils.getCallsForProbeNode(node, service);
         }else if(NeoUtils.isSRowNode(node)){
             nodes = NeoUtils.getCallsForSRowNode(node, service);
         }else{
             nodes = NeoUtils.getCallsForSCellNode(node, service);
         }                
         Node gis = NeoUtils.findGisNodeByChild(drive);
         IMap activeMap = ApplicationGIS.getActiveMap();
         if (activeMap != ApplicationGIS.NO_MAP) {
             NeoCatalogPlugin.getDefault().getLayerManager().sendUpdateMessage(new ChangeSelectionEvent(gis, nodes));
         }
     }
     
     /**
      * initialize startup parameters
      */
     private void initialize() {
         labelProvider = new ViewLabelProvider();
         labelProvider.createTableColumn();
         provider = new ViewContentProvider();
         tableViewer.setContentProvider(provider);
         tableViewer.setInput(0);
         // formPeriods();
         formCallDataset();
     }
 
     /**
      *forms period list
      * 
      * @param statistics
      */
     private void formPeriods(CallStatistics statistics) {
         List<String> periods=new ArrayList<String>();
         if (statistics!=null){
             CallTimePeriods[] allPeriods = CallTimePeriods.values();
             for (int i = 0; i <= statistics.getHighPeriod().ordinal(); i++) {
                 periods.add(allPeriods[i].getId());
             }
         }
         cPeriod.setItems(periods.toArray(new String[0]));
         cPeriod.setText(periods.get(0));
     }
     
     /**
      *forms direction list
      * 
      * @param callTypes
      */
     private void formCallType(Set<StatisticsCallType> callTypesSet) {
         List<String> callTypes=new ArrayList<String>();
         for (StatisticsCallType callType : StatisticsCallType.getSortedTypesList(callTypesSet)) {
             callTypes.add(callType.getViewName());
         }
         cCallType.setItems(callTypes.toArray(new String[0]));
         cCallType.setText(callTypes.get(0));
     }
     
     private void formCallType(StatisticsCallType callType) {
         List<String> callTypes=new ArrayList<String>();
         callTypes.add(callType.getViewName());
         
         cCallType.setItems(callTypes.toArray(new String[0]));
         cCallType.setText(callType.getViewName());
     }
 
     /**
      *form call dataset list
      */
     private void formCallDataset() {
         callDataset.clear();
         callDataset = NeoUtils.getAllDatasetNodesByType(DriveTypes.AMS_CALLS, NeoServiceProvider.getProvider().getService());
         List<String> datasets = new ArrayList<String>(callDataset.keySet());
         Collections.sort(datasets);
         cDrive.setItems(datasets.toArray(new String[0]));
         cCallType.clearSelection();
         cProbe.clearSelection();
         cPeriod.clearSelection();
     }
 
     /**
      *add listeners
      */
     private void addListeners() {
         tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 
             @Override
             public void selectionChanged(SelectionChangedEvent event) {
                 if (event.getSelection() instanceof IStructuredSelection) {
                     IStructuredSelection selections = (IStructuredSelection)event.getSelection();
                     Object selRow = selections.getFirstElement();
                     if (selRow != null && selRow instanceof PeriodWrapper) {
                         PeriodWrapper wr = (PeriodWrapper)selRow;
                         int columnId = cursor.getColumn();
                         if (columnId == 0) {
                             select(wr.sRow);
                             return;
                         }
                         if ((columnId == 1 || columnId == 2 || columnId == 3)&&!getCallType().equals(StatisticsCallType.AGGREGATION_STATISTICS)) {
                             select(wr.getProbeNode());
                             return;
                         }
                         ColumnHeaders header = columnHeaders.get(columnId);
                         final String nodeName = header.header.getTitle();
                         Node cellNode = null;
                         Transaction tx = NeoServiceProvider.getProvider().getService().beginTx();
                         try {
                             Iterator<Node> iterator = NeoUtils.getChildTraverser(wr.sRow, new ReturnableEvaluator() {
 
                                 @Override
                                 public boolean isReturnableNode(TraversalPosition currentPos) {
                                     return NeoUtils.getNodeName(currentPos.currentNode(),null).equals(nodeName);
                                 }
                             }).iterator();
                             cellNode = iterator.hasNext() ? iterator.next() : null;
                         } finally {
                             tx.finish();
                         }
                         if (cellNode != null) {
                             select(cellNode);
                         }
                     }
                 }
             }
         });
 
         cDrive.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 formPropertyList();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         cProbe.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 changeProbe();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         cPeriod.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 changePeriod();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         
         cCallType.addSelectionListener(new SelectionListener() {
             
             @Override
             public void widgetSelected(SelectionEvent e) {
                 changeCallType();                
             }
             
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         bExport.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 startExport();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
          dateStart.addFocusListener(new FocusListener() {        
              @Override
              public void focusLost(FocusEvent e) {
                 changeDate();
              }
             
              @Override
              public void focusGained(FocusEvent e) {
              }
          });
          dateStart.addKeyListener(new KeyListener() {        
              @Override
              public void keyReleased(KeyEvent e) {
                  if (e.keyCode == '\r' || e.keyCode == SWT.KEYPAD_CR) {
                      changeDate();
                  }
              }
             
              @Override
              public void keyPressed(KeyEvent e) {
              }
          });
          timeStart.addFocusListener(new FocusListener() {             
              @Override
              public void focusLost(FocusEvent e) {
                  changeDate();
              }
             
              @Override
              public void focusGained(FocusEvent e) {
              }
          });
          timeStart.addKeyListener(new KeyListener() {        
              @Override
              public void keyReleased(KeyEvent e) {
                  if (e.keyCode == '\r' || e.keyCode == SWT.KEYPAD_CR) {
                      changeDate();
                  }
              }
             
              @Override
              public void keyPressed(KeyEvent e) {
              }
          });
          dateEnd.addFocusListener(new FocusListener() {        
              @Override
              public void focusLost(FocusEvent e) {
                  changeDate();
              }
             
              @Override
              public void focusGained(FocusEvent e) {
              }
          });
          dateEnd.addKeyListener(new KeyListener() {        
              @Override
              public void keyReleased(KeyEvent e) {
                  if (e.keyCode == '\r' || e.keyCode == SWT.KEYPAD_CR) {
                      changeDate();
                  }
              }            
              @Override
              public void keyPressed(KeyEvent e) {
              }
          });
          timeEnd.addFocusListener(new FocusListener() {             
              @Override
              public void focusLost(FocusEvent e) {
                  changeDate();
              }            
              @Override
              public void focusGained(FocusEvent e) {
              }
          });
          timeEnd.addKeyListener(new KeyListener() {        
              @Override
              public void keyReleased(KeyEvent e) {
                  if (e.keyCode == '\r' || e.keyCode == SWT.KEYPAD_CR) {
                      changeDate();
                  }
              }            
              @Override
              public void keyPressed(KeyEvent e) {
              }
          });
          cInclInconculsiveState.addSelectionListener(new SelectionListener() {
 
              @Override
              public void widgetSelected(SelectionEvent e) {
                  formPropertyList();
              }
 
              @Override
              public void widgetDefaultSelected(SelectionEvent e) {
                  widgetSelected(e);
              }
          });
          bReport.addSelectionListener(new SelectionAdapter(){
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 generateReport();
             }
              
          });
          bUpdate.addSelectionListener(new SelectionAdapter(){
 
              @Override
              public void widgetSelected(SelectionEvent e) {
                  updateTable(false);
              }
               
           });
 //        bUpdate.addSelectionListener(new SelectionListener() {
 //
 //            @Override
 //            public void widgetSelected(SelectionEvent e) {
 //                changeDate();
 //            }
 //
 //            @Override
 //            public void widgetDefaultSelected(SelectionEvent e) {
 //                widgetSelected(e);
 //            }
 //        });
     }
 
     private void generateReport() {
         String aggregation = cPeriod.getText();
         StringBuffer sb = new StringBuffer("report \"Overview of ").append(aggregation).append(" KPI's\n").append(cDrive.getText()).append("\" do\n  author '")
         .append(System.getProperty("user.name")).append("'\n  date '").append(new SimpleDateFormat("yyyy-MM-dd").format(new Date())).append("'\n");
         Node dsNode = callDataset.get(cDrive.getText());
         sb.append("  ds=dataset('").append(dsNode.getProperty("name")).append("')\n");
         sb
                 .append("  ca_root=find_first(ds,{'type'=>'call analysis root','call_type'=>'AGGREGATION_STATISTICS'},:CALL_ANALYSIS,:VIRTUAL_DATASET)\n");//$NON-NLS-1$
         sb.append("  ").append(aggregation).append("=find_first(ca_root,{'name'=>'").append(aggregation).append("'},:CHILD)\n");//$NON-NLS-1$
         for (AggregationStatisticsHeaders header : AggregationStatisticsHeaders.values()) {
             Float threshold;
             if ((threshold = header.getThreshold()) != null) {
                 sb.append("  chart \"").append(header.getTitle()).append("\n").append(header.getChartTitle());
                 String subtitle = MessageFormat.format(header.getThresholdTitle(), 
                         new Object[]{
                     header.getCondition().getInverseCondition().getText(),
                     header.getThreshold(),
                     header.getUnit().getText()});
                 sb.append(";\n").append(subtitle);//$NON-NLS-1$
                 sb.append("\" do |chart|\n");//$NON-NLS-1$
                 sb.append("    chart.data=select_properties [\"name\",\"time\"]  do\n");//$NON-NLS-1$
                 sb.append("      from do\n");//$NON-NLS-1$
                 sb.append("        root ").append(aggregation).append("\n");//$NON-NLS-1$
                 sb.append("        traverse :CHILD, :NEXT\n");//$NON-NLS-1$
                 sb.append("        depth :all\n");//$NON-NLS-1$
                 sb.append("        where {get_property(\"type\")==\"s_row\" and get_property(\"time\")>=");
                 sb.append(getTime(dateStart, timeStart)).append(" and get_property(\"time\")<=").append(getTime(dateEnd, timeEnd)).append("}\n");//$NON-NLS-1$
                 sb.append("        select_properties \"value\" do\n");//$NON-NLS-1$
                 sb.append("          from do\n");//$NON-NLS-1$
                 sb.append("            traverse :CHILD, :NEXT\n");//$NON-NLS-1$
                 sb.append("            depth :all\n");//$NON-NLS-1$
                 sb.append("            stop_on {get_property(\"type\")==\"s_row\"}\n");//$NON-NLS-1$
                 sb.append("            where {get_property(\"type\")==\"s_cell\" and get_property(\"name\")==\"").append(header.getTitle()).append("\"}\n");//$NON-NLS-1$
                 sb.append("          end\n");//$NON-NLS-1$
                 sb.append("        end\n");//$NON-NLS-1$
                 sb.append("      end\n");//$NON-NLS-1$
                 sb.append("    end\n");//$NON-NLS-1$
                 sb.append("    chart.type=:combined\n");//$NON-NLS-1$
                 sb.append("    chart.aggregation=:").append(aggregation).append("\n");//$NON-NLS-1$
                 sb.append("    chart.time=\"time\"\n");//$NON-NLS-1$
                 sb.append("    chart.categories=\"name\"\n");//$NON-NLS-1$
                 sb.append("    chart.values=\"value\"\n");//$NON-NLS-1$
                 sb.append("    chart.threshold=").append(threshold).append("\n");//$NON-NLS-1$
                 sb.append("    chart.threshold_label='").append(Messages.R_THRESHOLD).append("'\n");//$NON-NLS-1$
                 sb.append("    chart.range_axis_label='").append(header.getUnit().getText()).append("'\n");//$NON-NLS-1$
                 sb.append("  end\n");//$NON-NLS-1$
             }
         }
         sb.append("end");
         String aweProjectName = AWEProjectManager.getActiveProjectName();
         IRubyProject rubyProject;
         IFile file;
         try {
             rubyProject = NewRubyElementCreationWizard.configureRubyProject(null, aweProjectName);
             IProject project = rubyProject.getProject();
             int i = 0;
             while ((file = project.getFile(new Path(("report" + i) + ".r"))).exists()) { //$NON-NLS-1$ //$NON-NLS-2$
                 i++;
             }
             LOGGER.debug("Report script:\n" + new String(sb.toString().getBytes("UTF-8"), "UTF-8")); //$NON-NLS-1$
             InputStream is;
 
             // is = new ByteArrayInputStream(sb.toString().getBytes());
             is = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
             file.create(is, true, null);
             // file.setCharset("UTF-8",null);
             is.close();
             getViewSite().getPage().openEditor(new FileEditorInput(file), "org.amanzi.awe.report.editor.ReportEditor"); //$NON-NLS-1$
         } catch (Exception e) {
             LOGGER.error(e);
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
     }
 
 
     /**
      * start export job
      */
     protected void startExport() {
         final IWorkbenchWindow window = getSite().getWorkbenchWindow();
         final List<PeriodWrapper> elements = new ArrayList<PeriodWrapper>(provider.elements);
         if (!elements.isEmpty()) {
             ExportSpreadsheetWizard wizard = new ExportSpreadsheetWizard(elements, columnHeaders);
             wizard.init(window.getWorkbench(), null);
             Shell parent = window.getShell();
             WizardDialog dialog = new WizardDialog(parent, wizard);
             dialog.create();
             dialog.open();
         }
 
     }
     
     protected Long getStartTime(){
         return getTime(dateStart, timeStart);
     }
     
     protected Long getEndTime(){
         return getTime(dateEnd, timeEnd);
     }
 
     /**
      * change start or end time
      */
     protected void changeDate() {
         CallTimePeriods period = getTimePeriod();
         if (period!=null) {
             setTime(dateStart, timeStart, period.getFirstTime(getStartTime())); //set correct time for period
             setTime(dateEnd, timeEnd, period.getFirstTime(getEndTime()));
         }else{
             setDefaultTime();
         }
         updateTable(false);
     }
 
 
     private void setDefaultTime() {
         Long time = CallTimePeriods.HOURLY.getFirstTime(System.currentTimeMillis());
         setTime(dateStart, timeStart, time);
         setTime(dateEnd, timeEnd, time);
     }
 
     /**
      *change period
      */
     protected void changePeriod() {
         Node drive = callDataset.get(cDrive.getText());
         if(drive==null){
             setDefaultTime();
         }
         updateTable(false);
     }
     
     /**
      *change period
      */
     protected void changeCallType() {
         StatisticsCallType callType = getCallType();
         columnHeaders = new ArrayList<ColumnHeaders>();
         labelProvider.updateHeaders(callType);
         Node drive = callDataset.get(cDrive.getText());
         if (cProbe.getText().isEmpty()) {
             formProbeCall(drive, callType);
         }
         else {            
             if(callType.equals(StatisticsCallType.AGGREGATION_STATISTICS)){
                 bReport.setEnabled(true);
                 cProbe.setText(KEY_ALL);
             }else{
                 bReport.setEnabled(false);
             }
             String probeName = cProbe.getText();
             formProbeCall(drive, callType);
             if ((probeName.equals(ALL_VALUE)) ||(!probeName.isEmpty() &&
                 NeoUtils.hasCallsOfType(drive, callType.getId(), probeName))) {                
                 cProbe.setText(probeName);
                 updateTable(false);
             }            
             else {
                 updateTable(true);
             }
         }
     }
     
     private StatisticsCallType getCallType(){
         return StatisticsCallType.getTypeByViewName(cCallType.getText());
     }
 
     /**
      *change probe
      */
     protected void changeProbe() {
         updateTable(false);
     }
 
     /**
      *update table if has correct InputWrapper
      */
     private void updateTable(boolean showEmpty) {
         InputWrapper wrapper = createInputWrapper(showEmpty);
        if (wrapper.isCorrectInput()) {            
             tableViewer.setInput(wrapper);
         }
     }
 
     /**
      * create InputWrapper depends of user choices
      * 
      * @return InputWrapper
      */
     private InputWrapper createInputWrapper(boolean showEmpty) {
         return new InputWrapper(probeCallDataset.get(cProbe.getText()), callDataset.get(cDrive.getText()),
                 getTimePeriod(), cCallType.getText(), showEmpty);
     }
 
     private CallTimePeriods getTimePeriod() {
         return CallTimePeriods.findById(cPeriod.getText());
     }
 
     /**
      *forms property list depends of selected dataset
      */
     protected void formPropertyList() {
         final Node drive = callDataset.get(cDrive.getText());
         if (drive == null) {
             setDefaultTime();
             return;
         }
         final InclInconclusiveStates inclInconclusive = getInclInconclusive();
         parent.setCursor(new Cursor(parent.getDisplay(), SWT.CURSOR_WAIT));
         frame.setEnabled(false);        
         Job statGetter = new Job("Get statistics") {            
             @Override
             protected IStatus run(IProgressMonitor monitor) {
                 try {
                     GraphDatabaseService service = NeoServiceProvider.getProvider().getService();
                     final CallStatistics statistics = inclInconclusive.getStatistics(drive, service, monitor);
                     if(monitor.isCanceled()){
                         return Status.OK_STATUS;
                     }
                     final Pair<Long, Long> times = NeoUtils.getMinMaxTimeOfDataset(drive, service);
                     ActionUtil.getInstance().runTask(new Runnable() {
                         @Override
                         public void run() {
                             Transaction tx = NeoUtils.beginTransaction();
                             try {
                                 Set<StatisticsCallType> callTypes = statistics.getStatisticNode().keySet();
                                 if (callTypes.size() == 1) {
                                     StatisticsCallType type = callTypes.iterator().next();
                                     formCallType(type);                
                                 }
                                 else {
                                     formCallType(callTypes);
                                 }
                                 StatisticsCallType callType = getCallType();
                                 formProbeCall(drive, callType);
                                 formPeriods(statistics);
                                 setTime(dateStart, timeStart, times.getLeft());
                                 setTime(dateEnd, timeEnd, times.getRight());
                                 labelProvider.updateHeaders(callType);
                                 updateTable(false);
                             } catch (Exception e) {
                                 e.printStackTrace();
                             } finally {
                                 tx.finish();                                                                
                             }
                         }
                     }, true);
                     
                 } catch (Exception e) {
                     // TODO Handle IOException
                     throw (RuntimeException)new RuntimeException().initCause(e);
                 } finally {
                     ActionUtil.getInstance().runTask(new Runnable() {
                         @Override
                         public void run() {
                             frame.setEnabled(true);
                             parent.setCursor(new Cursor(parent.getDisplay(), SWT.CURSOR_ARROW));
                         }
                     }, true);
                 }
                 return Status.OK_STATUS;
             }
         };
         statGetter.schedule();
     }
 
 
     private InclInconclusiveStates getInclInconclusive() {
         if(cInclInconculsiveState.getSelectionIndex()<0){
             return InclInconclusiveStates.EXCLUDE;
         }
         return InclInconclusiveStates.getStateById(cInclInconculsiveState.getText());
     }
 
     /**
      * forms call probe depends of dataset
      * 
      * @param drive - drive dataset
      */
     private void formProbeCall(Node drive, StatisticsCallType callType) {
         probeCallDataset.clear();
         probeCallDataset.put(KEY_ALL, null);
         if ((drive != null) && (callType != null)&& !callType.equals(StatisticsCallType.AGGREGATION_STATISTICS)) {
             GraphDatabaseService service = NeoServiceProvider.getProvider().getService();
             Transaction tx = service.beginTx();
             try {
                 Collection<Node> allProbesOfDataset = NeoUtils.getAllProbesOfDataset(drive, callType.getId());
                 for (Node probe : allProbesOfDataset) {
                     probeCallDataset.put(NeoUtils.getNodeName(probe,service), probe);
                 }
             } finally {
                 tx.finish();
             }
         }
         String[] result = probeCallDataset.keySet().toArray(new String[0]);
         Arrays.sort(result);
         cProbe.setItems(result);
         cProbe.setText(KEY_ALL);
     }
     
     private List<IStatisticsHeader> getAggregationHeaders(){
         Node drive = callDataset.get(cDrive.getText());
         if (drive == null) {
             return StatisticsCallType.AGGREGATION_STATISTICS.getHeaders();
         }
         GraphDatabaseService service = NeoServiceProvider.getProvider().getService();
         Transaction tx = service.beginTx();
         try {
             Node statRoot = null;
             for (Relationship link : drive.getRelationships(ProbeCallRelationshipType.CALL_ANALYSIS, Direction.OUTGOING)) {
                 Node root = link.getEndNode();
                 String rootType = root.getProperty(CallProperties.CALL_TYPE.getId(), "").toString();
                 if (rootType.equals(StatisticsCallType.AGGREGATION_STATISTICS.toString())) {
                     statRoot = root;
                     break;
                 }
             }
             if (statRoot == null) {
                 return StatisticsCallType.AGGREGATION_STATISTICS.getHeaders();
             }
             List<IStatisticsHeader> result = new ArrayList<IStatisticsHeader>();
             for (AggregationCallTypes type : AggregationCallTypes.values()) {
                 boolean hasType = (Boolean)statRoot.getProperty(type.getRealType().getId().getProperty(), false);
                 if(hasType){
                     result.addAll(type.getAggrHeaders());
                 }
             }
             return result;
         } finally {
             tx.finish();
         }
     }
 
 
     // TODO implement if necessary
     private void hookContextMenu() {
         // MenuManager menuMgr = new MenuManager("#PopupMenu");
         // menuMgr.setRemoveAllWhenShown(true);
         // menuMgr.addMenuListener(new IMenuListener() {
         // public void menuAboutToShow(IMenuManager manager) {
         // CallAnalyserView.this.fillContextMenu(manager);
         // }
         // });
         // Menu menu = menuMgr.createContextMenu(viewer.getControl());
         // viewer.getControl().setMenu(menu);
         // getSite().registerContextMenu(menuMgr, viewer);
     }
 
     /**
      * Passing the focus request to the viewer's control.
      */
     public void setFocus() {
         tableViewer.getControl().setFocus();
     }
 
     /**
      * <p>
      * Column header - contains information about columns
      * </p>
      * 
      * @author Cinkel_A
      * @since 1.0.0
      */
     public class ColumnHeaders {
 
         final private TableColumn column;
         private final IStatisticsHeader header;
         private String name;
         private SortOrder sortOrder = SortOrder.NONE;
 
 
         /**
          * constructor - only for string properties property.needMappedCount() must be true
          * 
          * @param column TableColumn
          * @param properties - property value
          */
         public ColumnHeaders(TableColumn column, IStatisticsHeader header) {
             this.column = column;
             this.header = header;
             name = column.getText();
             updateColumnImage();
         }
 
         /**
          * get value depends PeriodWrapper
          * 
          * @param wr - PeriodWrapper
          * @param index
          * @return statistic value
          */
         public String getValue(PeriodWrapper wr, int index) {
             if (header == null) {
                 if (index==0){
                 return NeoUtils.getNodeName(wr.sRow,null);
                 }else if (index==1){
                     return wr.getHost();
                 } else if (index == 2) {
                     return wr.getProbeLA();
                 } else {
                     return wr.getProbeF();
                 }
             } else {
                 return wr.getValue(header);
             }
         }
         
         /**
          * get value depends PeriodWrapper
          * 
          * @param wr - PeriodWrapper
          * @param index
          * @return statistic value
          */
         public Object getValueForSort(PeriodWrapper wr, int index) {
             if (header == null) {
                 if (index==0){
                 return NeoUtils.getNodeName(wr.sRow,null);
                 }else if (index==1){
                     return wr.getHost();
                 } else if (index == 2) {
                     return wr.getRealLA();
                 } else {
                     return wr.getRealF();
                 }
             } else {
                 return wr.getValueForSort(header);
             }
         }
         
         /**
          * get value depends PeriodWrapper
          * 
          * @param wr - PeriodWrapper
          * @param index
          * @return statistic value
          */
         public Color getBackgroundColor(PeriodWrapper wr) {
             return wr.getBackgroundColor(header);
         }        
         
         /**
          * get value depends PeriodWrapper
          * 
          * @param wr - PeriodWrapper
          * @param index
          * @return statistic value
          */
         public Color getForegroundColor(PeriodWrapper wr) {
             return wr.getForegroundColor(header);
         }
 
         /**
          * @return Returns the name.
          */
         public String getName() {
             return name;
         }
 
         /**
          * @return Returns the column.
          */
         public TableColumn getColumn() {
             return column;
         }
         
         private void updateSortOrder() {
             switch (sortOrder) {
             case NONE:
             case ASC:
                 sortOrder = SortOrder.DESC;
                 break;
             case DESC:
                 sortOrder = SortOrder.ASC;
                 break;
             default:
                 break;
             }
             updateColumnImage();
         }
 
         private void updateColumnImage() {
             Image image = null;
             if (!sortOrder.equals(SortOrder.NONE)) {
                 image = CallAnalyserPlugin.getImageDescriptor(sortOrder.getIconPath()).createImage();
             }
             column.setImage(image);
         }
     }
 
     /**
      * <p>
      * Period wrapper contains information about calculated period
      * </p>
      * 
      * @author Cinkel_A
      * @since 1.0.0
      */
     public static class PeriodWrapper {
         private final Node sRow;
         private Map<IStatisticsHeader, String> mappedValue = new HashMap<IStatisticsHeader, String>();
         private Map<IStatisticsHeader, Object> sortedValue = new HashMap<IStatisticsHeader, Object>();
         private Map<IStatisticsHeader, ColoredFlags> flaggedValue = new HashMap<IStatisticsHeader, ColoredFlags>();
         private String host;
         private String probeF = "";
         private String probeLA = "";
         private Number realF = null;
         private Number realLA = null;
         private Node probeNode;
         private Color color;
         
         /**
          * Constructor
          * 
          * @param beginTime - begin time
          * @param endTime - end time
          * @param indexPartName - index name
          */
         public PeriodWrapper(Node sRow, StatisticsCallType callType, boolean isInconclusive) {
             super();
             this.sRow = sRow;
             mappedValue.clear();
             for (Node node : NeoUtils.getChildTraverser(sRow)) {
                 String name = NeoUtils.getNodeName(node,null);
                 IStatisticsHeader header = callType.getHeaderByTitle(name);
                 if (header != null) {                   
                     Object value = node.getProperty(INeoConstants.PROPERTY_VALUE_NAME, null);
                     sortedValue.put(header, value);
                     mappedValue.put(header, getFormattedValue(value, header));
                     ColoredFlags flag = ColoredFlags.getFlagById((String)node.getProperty(INeoConstants.PROPERTY_FLAGGED_NAME, ColoredFlags.NONE.getId()));
                     flaggedValue.put(header, flag);
                 }
             }
             if (!callType.equals(StatisticsCallType.AGGREGATION_STATISTICS)) {
                 Iterator<Node> source = sRow.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator() {
 
                     @Override
                     public boolean isReturnableNode(TraversalPosition currentPos) {
                         return NeoUtils.isProbeNode(currentPos.currentNode());
                     }
                 }, GeoNeoRelationshipTypes.SOURCE, Direction.OUTGOING).iterator();
                 if (source.hasNext()) {
                     probeNode = source.next();
                 }else if (isInconclusive){
                     source = sRow.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
                         @Override
                         public boolean isReturnableNode(TraversalPosition currentPos) {
                             return NeoUtils.isProbeNode(currentPos.currentNode());
                         }
                     }, GeoNeoRelationshipTypes.SOURCE, Direction.OUTGOING).iterator();
                     probeNode =  source.next();
                 }
                 host = NeoUtils.getNodeName(probeNode,null).split(" ")[0];
                 realF = (Number)probeNode.getProperty(INeoConstants.PROBE_F, null);
                 realLA = (Number)probeNode.getProperty(INeoConstants.PROBE_LA, null);
                 probeF = realF == null ? "" : realF.toString();
                 probeLA = realLA == null ? "" : realLA.toString();
             }
         }
         
         private String getFormattedValue(Object value,IStatisticsHeader header){
             if(value == null){
                if(header.getType().equals(StatisticsType.COUNT)){
                    value = 0;
                }else{
                    value = 0f;
                }
             }            
             if(value instanceof Float){
                 BigDecimal decValue = new BigDecimal(((Float)value).doubleValue());
                 if (decValue.equals(BigDecimal.ZERO)) {
                     decValue = decValue.setScale(1);
                 }else{
                     decValue = decValue.setScale(3, RoundingMode.HALF_EVEN);
                 }
                 return decValue.toString();
             }
             return value.toString();
         }
 
         /**
          * @return
          */
         public Color getBackgroundColor(IStatisticsHeader header) {
             return color;
         }
         
         /**
          * @return
          */
         public Color getForegroundColor(IStatisticsHeader header) {
             if(header==null){
                 return null;
             }
             ColoredFlags flagged = flaggedValue.get(header);
             if(flagged==null||flagged.equals(ColoredFlags.NONE)){
                 return null;
             }
             return flagged.getColor();
         }
 
         /**
          * @param header
          * @return
          */
         public String getValue(IStatisticsHeader header) {
             String string = mappedValue.get(header);
             return string == null ? ERROR_VALUE : string;
         }
         
         /**
          * @param header
          * @return
          */
         public Object getValueForSort(IStatisticsHeader header) {
             return sortedValue.get(header);
         }
 
         /**
          * @return Returns the host.
          */
         public String getHost() {
             return host;
         }
 
         /**
          * @return Returns the probeF.
          */
         public String getProbeF() {
             return probeF;
         }
         
         /**
          * @return Returns the realF.
          */
         public Number getRealF() {
             return realF;
         }
 
         /**
          * @return Returns the probeLA.
          */
         public String getProbeLA() {
             return probeLA;
         }
         
         /**
          * @return Returns the realLA.
          */
         public Number getRealLA() {
             return realLA;
         }
 
         /**
          * @return Returns the probeNode.
          */
         public Node getProbeNode() {
             return probeNode;
         }
 
         /**
          * @param color The color to set.
          */
         public void setColor(Color color) {
             this.color = color;
         }
 
     }
 
     /**
      * <p>
      * InputWrapper contains information about input
      * </p>
      * 
      * @author Cinkel_A
      * @since 1.0.0
      */
     public static class InputWrapper {
         private Node probe;
         private Node drive;
         private CallTimePeriods periods;
         private StatisticsCallType callType;
         private Node periodNode;
         
         private boolean showEmpty;
 
         /**
          * constructor
          * 
          * @param probe - probe call node
          * @param drive - call dataset node
          * @param periods - periods
          */
         public InputWrapper(Node probe, Node drive, CallTimePeriods periods, String callType, boolean showEmpty) {
             super();
             this.probe = probe;
             this.drive = drive;
             this.periods = periods;
             this.showEmpty = showEmpty;
             if (callType.isEmpty()) {
                 this.callType = null;
             }
             else {
                 this.callType = StatisticsCallType.getTypeByViewName(callType);
             }
         }
 
         /**
          * @return
          */
         public Traverser getSrowTraverser(final GraphDatabaseService service,final InclInconclusiveStates inclInconclusive) {
             try {
                 CallStatistics statistic = inclInconclusive.getStatistics(drive, service, null);
                 periodNode = statistic.getPeriodNode(periods, callType);
                 
                 if (showEmpty) {
                     return null;
                 }
                 
                 if (periodNode == null) {
                     return NeoUtils.emptyTraverser(probe);
                 }
                 if(callType.equals(StatisticsCallType.AGGREGATION_STATISTICS)){
                     return NeoUtils.getChildTraverser(periodNode);
                 }
                 return NeoUtils.getChildTraverser(periodNode, new ReturnableEvaluator() {
 
                     @Override
                     public boolean isReturnableNode(TraversalPosition currentPos) {
                         return (probe == null || currentPos.currentNode().traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
                                     @Override
                                     public boolean isReturnableNode(TraversalPosition currentPos) {
                                         return currentPos.currentNode().equals(probe);
                                     }
                                 }, GeoNeoRelationshipTypes.SOURCE, Direction.OUTGOING).iterator().hasNext());
                     }
                 });
             } catch (IOException e) {
                 NeoCorePlugin.error(e.getLocalizedMessage(), e);
                 return NeoUtils.emptyTraverser(probe);
             }            
         }
 
         /**
          * get index name
          * 
          * @return
          */
         public String getIndexName() {
             return NeoUtils.getNodeName(probe != null ? probe : drive,null);
         }
 
         /**
          * check
          * 
          * @return true if InputWrapper contains correct information
          */
         public boolean isCorrectInput() {
             return drive != null /* && probe != null */&& periods != null && callType != null;
         }
 
     }
 
     /**
      *update view
      */
     public void updateView(boolean showEmpty) {
         formCallDataset();
         formProbeCall(null, null);
         tableViewer.setInput(createInputWrapper(showEmpty));
     }
 
 }
