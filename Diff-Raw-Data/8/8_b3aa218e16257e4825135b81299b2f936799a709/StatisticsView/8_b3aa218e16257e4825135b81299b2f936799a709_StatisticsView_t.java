 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.awe.statistics.view;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import net.refractions.udig.project.IMap;
 import net.refractions.udig.project.ui.ApplicationGIS;
 
 import org.amanzi.awe.catalog.neo.NeoCatalogPlugin;
 import org.amanzi.awe.catalog.neo.upd_layers.events.ChangeSelectionEvent;
 import org.amanzi.awe.report.editor.ReportEditor;
 import org.amanzi.awe.statistics.CallTimePeriods;
 import org.amanzi.awe.statistics.builder.StatisticsBuilder;
 import org.amanzi.awe.statistics.database.entity.Statistics;
 import org.amanzi.awe.statistics.database.entity.StatisticsCell;
 import org.amanzi.awe.statistics.database.entity.StatisticsGroup;
 import org.amanzi.awe.statistics.database.entity.StatisticsRow;
 import org.amanzi.awe.statistics.functions.AggregationFunctions;
 import org.amanzi.awe.statistics.template.Template;
 import org.amanzi.awe.statistics.template.TemplateBuilder;
 import org.amanzi.awe.statistics.template.TemplateColumn;
 import org.amanzi.neo.services.DatasetService;
 import org.amanzi.neo.services.INeoConstants;
 import org.amanzi.neo.services.NeoServiceFactory;
 import org.amanzi.neo.services.enums.CorrelationRelationshipTypes;
 import org.amanzi.neo.services.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.amanzi.neo.services.events.ShowPreparedViewEvent;
 import org.amanzi.neo.services.events.UpdateDrillDownEvent;
 import org.amanzi.neo.services.statistic.IPropertyHeader;
 import org.amanzi.neo.services.statistic.PropertyHeader;
 import org.amanzi.neo.services.ui.NeoServiceProviderUi;
 import org.amanzi.neo.services.ui.NeoServicesUiPlugin;
 import org.amanzi.neo.services.ui.NeoUtils;
 import org.amanzi.scripting.jruby.ScriptUtils;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.viewers.CheckStateChangedEvent;
 import org.eclipse.jface.viewers.CheckboxTreeViewer;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.ICheckStateListener;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.ITableColorProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.TableLayout;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerComparator;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.events.MouseTrackAdapter;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.ShellAdapter;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Device;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.DateTime;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.part.ViewPart;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.TraversalPosition;
 import org.neo4j.graphdb.Traverser.Order;
 import org.rubypeople.rdt.core.IRubyProject;
 import org.rubypeople.rdt.internal.ui.wizards.NewRubyElementCreationWizard;
 
 /**
  * Statistics Table View
  * 
  * @author Pechko_E
  * @since 1.0.0
  */
 public class StatisticsView extends ViewPart {
     private static final String SELECT_ALL = "(Select All)";
     private static final String SEPARATOR = "----------";
     public static final String ID = "org.amanzi.awe.statistics.view.StatisticsTableView";
     private static final String DRIVE_ID = "org.amanzi.awe.views.tree.drive.views.DriveTreeView";
 
     private static final int MIN_FIELD_WIDTH = 150;
     private static final int MIN_COLUMN_WIDTH = 230;
 
     private static final String LBL_DATASET = "Dataset:";
 
     private static final String LBL_TEMPLATE = "Template:";
 
     private static final String LBL_PERIOD = "Period:";
 
     private static final String LBL_START_TIME = "Start time:";
 
     private static final String LBL_AGGREGATION = "Aggregation:";
 
     private static final String LBL_END_TIME = "End time:";
 
     private static final String LBL_REPORT = "Report";
 
     private static final int LABEL_WIDTH = 28;
 
     private static final int MAX_GROUPS_PER_CHART = 10;
 
     private Composite frame;
 
     private Combo cDataset;
 
     private Combo cTemplate;
 
     private Combo cPeriod;
 
     private DateTime dateStart;
 
     private DateTime timeStart;
 
     private Combo cAggregation;
 
     private DateTime dateEnd;
 
     private DateTime timeEnd;
 
     private Button bUpdate;
 
     private Button bReport;
     private boolean isTemplateChanged;
     private boolean isTimeChanged;
     private boolean isFilterWindowOpened;
 
     private Map<String, Node> datasets = new HashMap<String, Node>();
 
     private DatasetService datasetService = NeoServiceFactory.getInstance().getDatasetService();
     private List<String> properties;
     private TableViewer tableViewer;
     private FormData viewerData;
     private StatisticsLabelProvider labelProvider;
     private StatisticsContentProvider statisticsProvider;
     private ScrolledComposite scrolled;
 
     private Image selectedFilter;
     private Image deselectedFilter;
     private Image enabledFilter;
 
     private RegexViewerFilter regexViewerFilter = new RegexViewerFilter();
 
     private boolean filterSelected;
     private int lastVisitedColumn;
     private Point point;
     private ArrayList<String> groups;
     private List<String> selection;
     private Map<String, IFile> allTemplates;
     private HashMap<String, Template> templates;
     protected Statistics statistics;
     private Image sortAscImage;
     private Image sortDescImage;
     protected Shell shell;
     private Composite parent;
 
     @Override
     public void createPartControl(Composite parent) {
         this.parent = parent;
         scrolled = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
         int minScrolledWidth = 10;
 
         frame = new Composite(scrolled, SWT.FILL);
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
 
         // The first column
         int width = MIN_COLUMN_WIDTH;
         int field_width = MIN_FIELD_WIDTH;
         minScrolledWidth += width;
         Composite column1 = addColumn(rowComposite, null, width);
         Composite cell1 = createCellComposite(column1, null, width);
         Composite cell2 = createCellComposite(column1, cell1, width);
         cDataset = createCombo(cell1, LBL_DATASET, field_width);
         cPeriod = createCombo(cell2, LBL_PERIOD, field_width);
 
         // The second column
         width = MIN_COLUMN_WIDTH;
         field_width = MIN_FIELD_WIDTH;
         minScrolledWidth += width;
         Composite column2 = addColumn(rowComposite, column1, width);
         cell1 = createCellComposite(column2, null, width);
         cell2 = createCellComposite(column2, cell1, width);
         cTemplate = createCombo(cell1, LBL_TEMPLATE, field_width);
         DateTime[] fields = createDateTimeControl(cell2, LBL_START_TIME, width, true);
         dateStart = fields[0];
         timeStart = fields[1];
 
         // The third column
         width = MIN_COLUMN_WIDTH;
         minScrolledWidth += width;
         Composite column3 = addColumn(rowComposite, column2, width);
         cell1 = createCellComposite(column3, null, width);
         cell2 = createCellComposite(column3, cell1, width);
         cAggregation = createCombo(cell1, LBL_AGGREGATION, field_width);
         fields = createDateTimeControl(cell2, LBL_END_TIME, width, false);
         dateEnd = fields[0];
         timeEnd = fields[1];
 
         // The fourth column
         width = MIN_COLUMN_WIDTH;
         Composite column4 = addColumn(rowComposite, column3, -1);
         cell1 = createCellComposite(column4, null, width);
         cell2 = createCellComposite(column4, cell1, width);
 
         field_width = 3 * MIN_FIELD_WIDTH / 4 - 20;
         minScrolledWidth += field_width;
         bUpdate = createButton(cell1, null, null, SWT.PUSH, 32);
         bUpdate.setImage(StatisticsViewPlugin.getImageDescriptor("/icons/refresh.gif").createImage());
         // bUpdate.setToolTipText(BTN_UPDATE_TOOLTIP);
         bReport = createButton(cell2, bUpdate, LBL_REPORT, SWT.PUSH, -1);
         // bExport = addButton(cell2, bReport, LB_EXPORT, SWT.PUSH, -1);
 
         // ------- table
         tableViewer = new TableViewer(frame, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
         viewerData = new FormData();
         viewerData.left = new FormAttachment(0, 0);
         viewerData.right = new FormAttachment(100, 0);
         viewerData.top = new FormAttachment(rowComposite, 2);
         viewerData.bottom = new FormAttachment(100, -2);
 
         labelProvider = new StatisticsLabelProvider(getSite().getShell().getDisplay(), false);
         statisticsProvider = new StatisticsContentProvider();
         tableViewer.setLabelProvider(labelProvider);
         tableViewer.setContentProvider(statisticsProvider);
         tableViewer.getControl().setLayoutData(viewerData);
 
         scrolled.setContent(frame);
         scrolled.setExpandVertical(true);
         scrolled.setExpandHorizontal(true);
         scrolled.setMinWidth(minScrolledWidth);
         //        
         addListeners();
         initialize();
     }
 
     /**
      * Initializes controls with default values
      */
     private void initialize() {
        sortAscImage = StatisticsViewPlugin.getImageDescriptor("icons/Asc.png").createImage();
        sortDescImage = StatisticsViewPlugin.getImageDescriptor("icons/Desc.png").createImage();
 
         updateDatasets();
         updateTemplates();
     }
 
     /**
      * Updates the dataset combo box
      */
     private void updateDatasets() {
         cDataset.removeAll();
         for (Node node : datasetService.getAllDatasetNodes().nodes()) {
             String datasetName = (String)node.getProperty(INeoConstants.PROPERTY_NAME_NAME);
             cDataset.add(datasetName);
             datasets.put(datasetName, node);
         }
     }
 
     /**
      * Called when a new dataset is loaded
      */
     void fireDatasetLoaded() {
         updateDatasets();
     }
 
     /**
      * Adds listeners
      */
     private void addListeners() {
         cDataset.addSelectionListener(new SelectionAdapter() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 Node dataset = getDatasetNode();
                 updateAvailableTemplates(dataset);
                 updateDatasetTimeRange(dataset);
                 updateAggregation();
             }
         });
         cTemplate.addSelectionListener(new SelectionAdapter() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
 
             }
         });
         bUpdate.addSelectionListener(new SelectionAdapter() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 updateInput();
             }
         });
         bReport.addSelectionListener(new SelectionAdapter() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 try {
                     generateReport();
                 } catch (Exception e1) {
                     e1.printStackTrace();
                     // TODO Handle CoreException
                     throw (RuntimeException)new RuntimeException().initCause(e1);
                 }
             }
         });
         cAggregation.addSelectionListener(new SelectionAdapter() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 if (cAggregation.getSelectionIndex() < cAggregation.indexOf(SEPARATOR)) {
                     System.out.println("Network level selected");
                 } else {
                     System.out.println("Property selected");
                 }
             }
         });
 
         SelectionAdapter adapter = new SelectionAdapter() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 isTimeChanged = true;
             }
 
         };
         dateStart.addSelectionListener(adapter);
         timeStart.addSelectionListener(adapter);
         dateEnd.addSelectionListener(adapter);
         timeEnd.addSelectionListener(adapter);
 
     }
 
     /**
      * Generates a report
      * 
      * @throws IOException
      * @throws CoreException
      */
     protected void generateReport() throws IOException, CoreException {
         URL url = FileLocator.toFileURL(StatisticsViewPlugin.getDefault().getBundle().getEntry("ruby/report_template.r"));
         String reportFileTemplate = ScriptUtils.getScriptContent(url.getPath());
 
         IRubyProject rubyProject = NewRubyElementCreationWizard.configureRubyProject(null, ApplicationGIS.getActiveProject()
                 .getName());
 
         final IProject project = rubyProject.getProject();
         int i = 0;
         IFile file = null;
         StringBuilder sb = new StringBuilder();
         sb.append("dataset_name=\"").append(cDataset.getText()).append("\"\n");
         sb.append("template_name=\"").append(getTemplate().getTemplateName()).append("\"\n");
         sb.append("aggregation=:").append(statistics.getName().split(", ")[1]).append("\n");
         sb.append("statistics=\"").append(statistics.getName()).append("\"\n");
         sb.append("kpis=[");
         for (TemplateColumn col : getTemplate().getColumns()) {
             sb.append("[\"").append(col.getName()).append("\",").append(
                     col.getFunction().equals(AggregationFunctions.AVERAGE) ? "true" : "false").append("],");
         }
         addGroupsToReportScript(sb, selection == null ? groups : selection);
         sb.append(reportFileTemplate);
         while ((file = project.getFile(new Path(("report" + i) + ".r"))).exists()) {
             i++;
         }
         InputStream is = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
         file.create(is, true, null);
         is.close();
         PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new FileEditorInput(file),
                 ReportEditor.class.getName());
     }
 
     /**
      * Appends string buffer with statistics group names
      * 
      * @param sb string buffer to update
      */
     private void addGroupsToReportScript(StringBuilder sb, List<String> groupsToAdd) {
         sb.append("]\n");
         sb.append("groups=[");
         for (String group : groupsToAdd) {
             sb.append("\"").append(group).append("\",");
         }
         sb.append("]\n");
     }
 
     /**
      * Updates the template combo box with templates suitable for the dataset selected
      * 
      * @param dataset the dataset selected
      */
     protected void updateAvailableTemplates(Node dataset) {
         templates = new HashMap<String, Template>();
         for (Entry<String, IFile> entry : allTemplates.entrySet()) {
             IFile file = entry.getValue();
             Template template = TemplateBuilder.getInstance().build(ScriptUtils.getScriptContent(file.getLocationURI()));
             if (isSuitableForDataset(template, dataset)) {
                 templates.put(entry.getKey(), template);
             }
         }
         cTemplate.setItems(templates.keySet().toArray(new String[templates.size()]));
     }
 
     /**
      * Finds all templates
      */
     protected void updateTemplates() {
         allTemplates = new HashMap<String, IFile>();
         try {
             for (IResource res : ResourcesPlugin.getWorkspace().getRoot().members()) {
                 if (res instanceof IProject) {
                     for (IResource folder : ((IContainer)res).members()) {
                         getTemplates(folder, allTemplates);
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
             // TODO Handle CoreException
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
 
     }
 
     /**
      * Gets templates from RDT
      * 
      * @param res resource
      * @param templates templates
      * @throws CoreException
      */
     private void getTemplates(IResource res, Map<String, IFile> templates) throws CoreException {
         if (res instanceof IContainer) {
             for (IResource file : ((IContainer)res).members()) {
                 getTemplates(file, templates);
             }
         } else if (res instanceof IFile) {
             if (res.getFileExtension().equals("t")) {
                 templates.put(res.getProjectRelativePath().toOSString(), (IFile)res);
             }
         }
 
     }
 
     /**
      * Checks if the template is suitable for the dataset
      * 
      * @param template template to check
      * @param dataset selected dataset
      * @return true if the template's metadata matches the dataset's metadata
      */
     private boolean isSuitableForDataset(Template template, Node dataset) {
         HashMap<String, String> metadata = template.getMetadata();
         if (metadata == null) {
             // assume that template without metadata is suitable for all datasets
             return true;
         }
         for (Entry entry : metadata.entrySet()) {
             Object value = dataset.getProperty(entry.getKey().toString(), null);
             if (value == null) {
                 return false;
             }
             if (!value.toString().equals(entry.getValue().toString())) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Resets date/time control value to dataset min or max timestamp
      * 
      * @param isStart indicates that the controls for start date/time should be reset
      */
     private void resetDateTimeControl(boolean isStart) {
         Node dataset = getDatasetNode();
         if (dataset != null) {
             isTimeChanged = true;
             if (isStart) {
                 setTime(dateStart, timeStart, (Long)dataset.getProperty(INeoConstants.MIN_TIMESTAMP));
             } else {
                 setTime(dateEnd, timeEnd, (Long)dataset.getProperty(INeoConstants.MAX_TIMESTAMP));
             }
         }
     }
 
     /**
      * Updates the period combo box with available periods and also date/time controls
      * 
      * @param dataset the dataset selected
      */
     private void updateDatasetTimeRange(Node dataset) {
         Long min = (Long)dataset.getProperty(INeoConstants.MIN_TIMESTAMP);
         Long max = (Long)dataset.getProperty(INeoConstants.MAX_TIMESTAMP);
         setTime(dateStart, timeStart, min);
         setTime(dateEnd, timeEnd, max);
 
         long time = (max - min) / (1000 * 60 * 60);
         List<String> periods = new ArrayList<String>();
         periods.add(CallTimePeriods.HOURLY.getId());
         if ((time = time / 24) >= 1) {
             periods.add(CallTimePeriods.DAILY.getId());
             if ((time = time / 7) >= 1) {
                 periods.add(CallTimePeriods.WEEKLY.getId());
             }
             if ((time = time / 30) >= 1) {
                 periods.add(CallTimePeriods.MONTHLY.getId());
             }
         }
         cPeriod.setItems(periods.toArray(new String[periods.size()]));
     }
 
     /**
      * Updates aggregation combo box with network levels if dataset is correlated and properties 
      */
     private void updateAggregation() {
         Node dataset = datasets.get(cDataset.getText());
         final ArrayList<String> aggregations = new ArrayList<String>();
         if (dataset.hasRelationship(CorrelationRelationshipTypes.CORRELATED, Direction.OUTGOING)) {
             Relationship rel = dataset.getRelationships(CorrelationRelationshipTypes.CORRELATED, Direction.OUTGOING).iterator()
                     .next();
             Node rootSector = rel.getEndNode();
             Relationship relToNetwork = rootSector.getSingleRelationship(CorrelationRelationshipTypes.CORRELATION,
                     Direction.INCOMING);
             if (relToNetwork != null) {
                 Node networkNode = relToNetwork.getStartNode();
                 String[] structure = (String[])networkNode.getProperty(INeoConstants.PROPERTY_STRUCTURE_NAME, new String[0]);
                 for (String element : structure) {
                     aggregations.add(element);
                 }
             }
         } else {
             aggregations.add("network");
         }
         aggregations.add(SEPARATOR);
         IPropertyHeader propertyHeader = PropertyHeader.getPropertyStatistic(dataset);
         properties = Arrays.asList(propertyHeader.getAllFields("-main-type-"));
         Collections.sort(properties);
         aggregations.addAll(properties);
         cAggregation.setItems(aggregations.toArray(new String[aggregations.size()]));
 
     }
 
     /**
      * Add column.
      * 
      * @param row Composite
      * @param previous_column Composite
      * @param width int
      * @return Composite
      */
     private Composite addColumn(Composite row, Composite previous_column, int width) {
         Composite column = new Composite(row, SWT.FILL);
         FormData fData = new FormData();
         fData.left = previous_column != null ? new FormAttachment(previous_column, 2) : new FormAttachment(0, 0);
         if (width > 0) {
             fData.width = width;
         }
         column.setLayoutData(fData);
         FormLayout layout = new FormLayout();
         layout.marginHeight = 2;
         layout.marginWidth = 3;
         column.setLayout(layout);
         return column;
     }
 
     /**
      * Create cell.
      * 
      * @param column Composite
      * @param cell_above Composite
      * @param width int
      * @return Composite
      */
     private Composite createCellComposite(Composite column, Composite cell_above, int width) {
         Composite cell = new Composite(column, SWT.FILL);
         FormData fData = new FormData();
         fData.left = new FormAttachment(0, 0);
         fData.height = 32;
         if (cell_above != null)
             fData.top = new FormAttachment(cell_above, 2);
         if (width > 0)
             fData.width = width;
         cell.setLayoutData(fData);
         FormLayout layout = new FormLayout();
         layout.marginHeight = 2;
         layout.marginWidth = 3;
         cell.setLayout(layout);
         return cell;
     }
 
     /**
      * Add selection combo.
      * 
      * @param cell Composite
      * @param text String
      * @param width int
      * @return Combo
      */
     private Combo createCombo(Composite cell, String text, int width) {
         Label label = new Label(cell, SWT.LEFT);
         label.setText(text);
         FormData fData = new FormData();
         fData.left = new FormAttachment(0, 0);
         fData.right = new FormAttachment(LABEL_WIDTH, 0);
         fData.bottom = new FormAttachment(100, -8);
         label.setLayoutData(fData);
 
         Combo selection = new Combo(cell, SWT.DROP_DOWN | SWT.READ_ONLY);
         fData = new FormData();
         fData.left = new FormAttachment(label, 2);
         fData.right = new FormAttachment(100, -2);
         selection.setLayoutData(fData);
         return selection;
     }
 
     /**
      * Add button
      * 
      * @param cell Composite
      * @param previous Button
      * @param text String
      * @param buttonType int
      * @param width int
      * @return Button
      */
     private Button createButton(Composite cell, Button previous, String text, int buttonType, int width) {
         Button button = new Button(cell, buttonType);
         if (text != null)
             button.setText(text);
         FormData fData = new FormData();
         FormAttachment formAttachment = new FormAttachment(0, 2);
         if (previous != null)
             formAttachment = new FormAttachment(previous, 2);
         fData.left = formAttachment;
         if (width > 0)
             fData.width = width;
         button.setLayoutData(fData);
         return button;
     }
 
     /**
      * Add date time fields.
      * 
      * @param cell Composite
      * @param text String
      * @param width int
      * @param isStartTime TODO
      * @return DateTime[]
      */
     private DateTime[] createDateTimeControl(Composite cell, String text, int width, final boolean isStartTime) {
         Label label = new Label(cell, SWT.LEFT);
         label.setText(text);
         FormData fData = new FormData();
         fData.left = new FormAttachment(0, 0);
         fData.right = new FormAttachment(LABEL_WIDTH, 0);
         fData.bottom = new FormAttachment(100, -8);
         label.setLayoutData(fData);
 
         int controlWidth = (100 - LABEL_WIDTH - 6) / 2;
         DateTime date = new DateTime(cell, SWT.FILL | SWT.BORDER | SWT.DATE | SWT.MEDIUM);
         fData = new FormData();
         fData.left = new FormAttachment(label, 2);
         fData.right = new FormAttachment(100 - controlWidth - 2, -2);
         fData.width = controlWidth;
         date.setLayoutData(fData);
 
         DateTime time = new DateTime(cell, SWT.FILL | SWT.BORDER | SWT.TIME | SWT.SHORT);
         fData = new FormData();
         fData.left = new FormAttachment(date, 2);
         fData.right = new FormAttachment(90, -2);
         fData.width = controlWidth;
         time.setLayoutData(fData);
 
         Button btnClearFilter = new Button(cell, SWT.PUSH);
         btnClearFilter.setText("X");
         btnClearFilter.setToolTipText("Restore initial value");
         fData = new FormData();
         fData.left = new FormAttachment(time, 2);
         fData.right = new FormAttachment(100, -2);
         btnClearFilter.setLayoutData(fData);
 
         btnClearFilter.addSelectionListener(new SelectionAdapter() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 resetDateTimeControl(isStartTime);
             }
         });
         return new DateTime[] {date, time};
     }
 
     /**
      * Sets time.
      * 
      * @param dateField DateTime
      * @param timeField DateTime
      * @param time timestamp to set
      */
     private void setTime(DateTime dateField, DateTime timeField, Long time) {
         Calendar calendar = new GregorianCalendar();
         calendar.setTimeInMillis(time);
         dateField.setYear(calendar.get(Calendar.YEAR));
         dateField.setMonth(calendar.get(Calendar.MONTH));
         dateField.setDay(calendar.get(Calendar.DAY_OF_MONTH));
         timeField.setHours(calendar.get(Calendar.HOUR_OF_DAY));
         timeField.setMinutes(calendar.get(Calendar.MINUTE));
         timeField.setSeconds(calendar.get(Calendar.SECOND));
     }
 
     /**
      * Gets time.
      * 
      * @param dateField DateTime
      * @param timeField DateTime
      * @return timestamp
      */
     private Long getTime(DateTime dateField, DateTime timeField) {
         Calendar calendar = new GregorianCalendar();
         calendar.setTimeInMillis(0L);
         calendar.set(dateField.getYear(), dateField.getMonth(), dateField.getDay(), timeField.getHours(), timeField.getMinutes());
         return calendar.getTimeInMillis();
     }
 
     @Override
     public void setFocus() {
     }
 
     /**
      * Selects appropriated elements in the Data tree view and highlights points on a map
      * 
      * @param cells an array of statistics cells
      * @param columnIndex column index
      */
     private void drillDown(StatisticsCell[] cells, int columnIndex) {
         List<Node> nodes = new ArrayList<Node>();
         StatisticsRow row = cells[0].getParent();
         StatisticsGroup group = row.getParent();
         Node statNode = statistics.getNode();
         Node levelNode = NeoUtils.getParent(null, statNode);
         Node dimNode = NeoUtils.getParent(null, levelNode);
         Node rootNode = NeoUtils.getParent(null, dimNode);
 
         nodes.add(row.getNode());
         nodes.add(group.getNode());
         nodes.add(statNode);
         nodes.add(levelNode);
         nodes.add(dimNode);
         nodes.add(rootNode);
         nodes.add(getDatasetNode());
 
         NeoServicesUiPlugin.getDefault().getUpdateViewManager().fireUpdateView(new ShowPreparedViewEvent(DRIVE_ID, nodes));
         NeoServicesUiPlugin.getDefault().getUpdateViewManager().fireUpdateView(new UpdateDrillDownEvent(nodes, StatisticsView.ID));
         selectNodesOnMap(cells, columnIndex);
     }
 
     /**
      * Finds all source nodes and fires the update layer event to highlight points on a map
      * 
      * @param cells
      * @param columnNum
      */
     private void selectNodesOnMap(StatisticsCell[] cells, int columnNum) {
         IMap activeMap = ApplicationGIS.getActiveMap();
         if (activeMap != ApplicationGIS.NO_MAP) {
             Set<Node> nodes = new HashSet<Node>();
             if ((!isAdditionalColumnNecessary() && columnNum <= 1) || (isAdditionalColumnNecessary() && columnNum <= 2)) {
                 // period or aggregation column is selected, so add all source nodes for all stats
                 // cells
                 for (StatisticsCell cell : cells) {
                     getSourceNodesForCell(nodes, cell);
                 }
 
             } else {
                 // add source nodes only for a selected cell
                 StatisticsCell cell = cells[columnNum - (!isAdditionalColumnNecessary() ? 2 : 3)];
                 getSourceNodesForCell(nodes, cell);
             }
 
             ChangeSelectionEvent event = new ChangeSelectionEvent(null, nodes);
             NeoCatalogPlugin.getDefault().getLayerManager().sendUpdateMessage(event);
         }
     }
 
     /**
      * Gets source nodes for a cell recursively
      * 
      * @param nodes nodes collection
      * @param cell a statistics cell
      */
     private void getSourceNodesForCell(Set<Node> nodes, StatisticsCell cell) {
         if (selection == null || (selection != null && selection.contains(cell.getParent().getParent().getGroupName()))) {
             for (Node source : cell.getSources()) {
                 getAllSources(nodes, source);
             }
         }
     }
 
     /**
      * Gets all source nodes recursively
      * 
      * @param nodes nodes collection
      * @param source node to start traverse
      */
     private void getAllSources(Set<Node> nodes, Node source) {
         if (source.hasRelationship(GeoNeoRelationshipTypes.SOURCE, Direction.OUTGOING)) {
             Iterator<Relationship> iterator = source.getRelationships(GeoNeoRelationshipTypes.SOURCE, Direction.OUTGOING)
                     .iterator();
             while (iterator.hasNext()) {
                 getAllSources(nodes, iterator.next().getEndNode());
             }
         } else {
             nodes.add(source);
         }
     }
 
     /**
      * Getter for the dataset node
      * 
      * @return the dataset node
      */
     private Node getDatasetNode() {
         return datasets.get(cDataset.getText());
     }
 
     /**
      * Getter for the template
      * 
      * @return the template
      */
     private Template getTemplate() {
         return templates.get(cTemplate.getText());
     }
 
     /**
      * Updates input of the statistics table viewer
      */
     private void updateInput() {
         try {
             final Node dataset = getDatasetNode();
             final String aggregation = cAggregation.getText();
             final String period = cPeriod.getText();
             final Template template = getTemplate();
             System.out.println("Template " + template);
             Job job = new Job(String.format("Get %s,%s statistics for %s", aggregation, period, cDataset.getText())) {
 
                 @Override
                 protected IStatus run(IProgressMonitor monitor) {
                     // CallTimePeriods.
                     StatisticsBuilder builder = new StatisticsBuilder(NeoServiceProviderUi.getProvider().getService(), dataset);
                     statistics = builder.buildStatistics(template, aggregation, CallTimePeriods.findById(period), monitor);
                     Display.getDefault().asyncExec(new Runnable() {
 
                         @Override
                         public void run() {
                             Iterator<StatisticsGroup> iterator = statistics.getGroups().values().iterator();
                             groups = new ArrayList<String>();
                             StatisticsGroup group = iterator.next();
                             groups.add(group.getGroupName());
                             while (iterator.hasNext()) {
                                 groups.add(iterator.next().getGroupName());
                             }
                             selection = new ArrayList<String>(groups);
                             StatisticsRow row = group.getRows().values().iterator().next();
                             // dispose table
                             final Composite parent = tableViewer.getTable().getParent();
                             tableViewer.getTable().dispose();
 
                             // create table viewer from scratch
                             tableViewer = new TableViewer(parent);
                             final Table table = tableViewer.getTable();
                             table.setLinesVisible(true);
                             table.setHeaderVisible(true);
                             TableLayout layout = new TableLayout();
                             table.setLayout(layout);
                             Collection<StatisticsCell> values = row.getCells().values();
                             int width = (int)100.0 / (values.size() + (isAdditionalColumnNecessary() ? 3 : 2));
 
                             if (isAdditionalColumnNecessary()) {
                                 TableColumn column = new TableColumn(table, SWT.RIGHT);
                                 column.setText("site");
                                 column.setToolTipText("site");
                                 layout.addColumnData(new ColumnWeightData(width, true));
 
                             }
 
                             TableColumn column = new TableColumn(table, SWT.RIGHT);
                             column.setText(aggregation);
                             column.setToolTipText(aggregation + "(click to apply/change filter or sort)");
                             // column.setImage(deselectedFilter);
                             layout.addColumnData(new ColumnWeightData(width, true));
 
                             column = new TableColumn(table, SWT.RIGHT);
                             column.setText("Total");
                             column.setToolTipText("Total");
                             layout.addColumnData(new ColumnWeightData(width, true));
 
                             for (StatisticsCell cell : values) {
                                 column = new TableColumn(table, SWT.RIGHT);
                                 column.setText(cell.getName());
                                 // column.setImage(StatisticsViewPlugin.getImageDescriptor("/icons/empty_filter.png").createImage());
                                 column.setToolTipText(cell.getName());
                                 layout.addColumnData(new ColumnWeightData(width, true));
                             }
                             tableViewer.getTable().addMouseListener(new MouseAdapter() {
 
                                 @Override
                                 public void mouseDown(MouseEvent e) {
                                     Point point = new Point(e.x, e.y);
                                     Table table = (Table)e.widget;
                                     Rectangle firstRowRect = table.getItem(0).getBounds();
                                     Rectangle lastRowRect = table.getItem(table.getItemCount() - 1).getBounds();
                                     // check if a data row selected
                                     if (e.y >= firstRowRect.y && e.y <= lastRowRect.y + lastRowRect.height) {
                                         int rowNum = (e.y - firstRowRect.y) / table.getItemHeight();
                                         TableItem item = table.getItem(rowNum);
                                         for (int i = 0; i < tableViewer.getTable().getColumnCount(); i++) {
                                             if (item.getBounds(i).contains(point)) {
                                                 StatisticsCell[] cells = (StatisticsCell[])item.getData();
                                                 drillDown(cells, i);
                                                 break;
                                             }
                                         }
                                     }
                                 }
 
                             });
                             addSortListeners(table);
                             table.setSortColumn(table.getColumn(0));
                             table.setSortDirection(SWT.UP);
 
                             // table.getColumn(0).setImage(deselectedFilter);
 
                             tableViewer.getControl().setLayoutData(viewerData);
                             tableViewer.setLabelProvider(new StatisticsLabelProvider(getSite().getShell().getDisplay(),
                                     isAdditionalColumnNecessary()));
                             tableViewer.setContentProvider(new StatisticsContentProvider());
                             tableViewer.setComparator(new StatisticsComparator());
                             if (isTimeChanged) {
                                 isTimeChanged = false;
                                 tableViewer.setFilters(new ViewerFilter[] {new StatisticsRowFilter(getTime(dateStart, timeStart),
                                         getTime(dateEnd, timeEnd))});
                             }
                             tableViewer.setInput(statistics);
                             tableViewer.getTable().addMouseMoveListener(new MouseMoveListener() {
 
                                 @Override
                                 public void mouseMove(MouseEvent e) {
                                     point = new Point(e.x, e.y);
                                 }
                             });
                             // track mouse movements to see if the filter image is clicked
                             // Pechko_E: some experiments with using column image as a button are
                             // commented for now
                             // tableViewer.getTable().addMouseTrackListener(new MouseTrackAdapter()
                             // {
                             //
                             // @Override
                             // public void mouseExit(MouseEvent e) {
                             // Table table = (Table)e.widget;
                             // TableItem firstDataRow = table.getItem(0);
                             // int i = isAdditionalColumnNecessary() ? 1 : 0;
                             // final TableColumn col = table.getColumn(i);
                             // if (e.y < firstDataRow.getBounds().y) {
                             // System.out.println("mouseExit header " + e);
                             // // for (int i = 0; i < table.getColumnCount(); i++) {
                             // final Rectangle bounds = firstDataRow.getBounds(i);
                             // if (e.x >= bounds.x && e.x <= bounds.x + bounds.width) {
                             // Image image = col.getImage();
                             // if (image != null) {
                             // Rectangle imgBounds = image.getBounds();
                             // if (e.x - bounds.x <= imgBounds.width + 15) {
                             // System.out.println(col.getText() + " --> image selected ");
                             // // col.setImage(selectedFilter);
                             // filterSelected = true;
                             // lastVisitedColumn = i;
                             // point = new Point(e.x, e.y);
                             // table.update();
                             //
                             // } else {
                             // filterSelected = false;
                             // // col.setImage(deselectedFilter);
                             // table.update();
                             //
                             // }
                             // }
                             // } else {
                             // // col.setImage(deselectedFilter);
                             // table.update();
                             //
                             // }
                             // // }
                             // } else {
                             // // col.setImage(deselectedFilter);
                             // table.update();
                             //
                             // }
                             // }
                             //
                             // });
 
                             // re-layout
                             parent.layout(true);
 
                             updateButtons();
                         }
 
                     });
 
                     return Status.OK_STATUS;
                 }
             };
 
             job.schedule();
         } catch (RuntimeException e) {
             e.printStackTrace();
             // TODO Handle RuntimeException
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
     }
 
     /**
      * Checks if additional column is necessary
      * 
      * @return true if 'sector' is selected for aggregation and it's a network level
      */
     private boolean isAdditionalColumnNecessary() {
         return cAggregation.getText().equals("sector") && cAggregation.getSelectionIndex() < cAggregation.indexOf(SEPARATOR);
     }
 
     /**
      * Adds sort listeners
      * 
      * @param table table to add listeners
      */
     private void addSortListeners(final Table table) {
         for (int i = 0; i < table.getColumnCount(); i++) {
             final TableColumn col = table.getColumn(i);
             final int colNum = i;
             col.addSelectionListener(new SelectionAdapter() {
 
                 @Override
                 public void widgetSelected(SelectionEvent e) {
                     // System.out.println("selection event:" + e + "\tx=" + e.x + "\ty=" + e.y +
                     // e.item + "\theight=" + e.height
                     // + "\twidth=" + e.width);
                     final TableColumn currentColumn = (TableColumn)e.widget;
                     if (colNum == (isAdditionalColumnNecessary() ? 1 : 0)) {// TODO
                         Image image = col.getImage();
                         // if (image != null) {
                         Point location = table.getDisplay().getCursorLocation();
                         // if (point.x - table.getItem(0).getBounds(colNum).x <=
                         // imgBounds.width + 8) {
                         System.out.println(col.getText() + " --> image selected ");
                         filterSelected = false;
                         // create a new shell
                         shell = new Shell(table.getShell(), SWT.BORDER);
 
                         Rectangle clientArea = table.getDisplay().getClientArea();
                         int shellWidth = Math.min(300, clientArea.width - location.x);
                         int shellHeight = Math.min(300, clientArea.height - location.y);
                         shell.setSize(shellWidth, shellHeight);
                         shell.setLocation(location);
                         // shell.setLocation(200, 200);
                         shell.setLayout(new FormLayout());
 
                         CLabel lblSortAsc = new CLabel(shell, SWT.LEFT);
                         lblSortAsc.setImage(sortDescImage);
                         lblSortAsc.setText("Sort A To Z");
                         setLayoutData(lblSortAsc, 0);
                         lblSortAsc.addMouseListener(new MouseAdapter() {
 
                             @Override
                             public void mouseDown(MouseEvent e) {
                                 updateSorting(table, colNum, currentColumn, SWT.DOWN);
                                 shell.close();
                             }
                         });
 
                         CLabel lblSortDesc = new CLabel(shell, SWT.LEFT);
                         lblSortDesc.setText("Sort Z To A");
                         lblSortDesc.setImage(sortAscImage);
                         setLayoutData(lblSortDesc, lblSortAsc);
 
                         lblSortDesc.addMouseListener(new MouseAdapter() {
 
                             @Override
                             public void mouseDown(MouseEvent e) {
                                 updateSorting(table, colNum, currentColumn, SWT.UP);
                                 shell.close();
                             }
                         });
 
                         Label lblSeparator1 = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
                         setLayoutData(lblSeparator1, lblSortDesc);
 
                         Label lblClearFilter = new Label(shell, SWT.LEFT);
                         lblClearFilter.setText("Clear Filter");
                         lblClearFilter.setEnabled(selection.size() != groups.size());
                         setLayoutData(lblClearFilter, lblSeparator1);
 
                         lblClearFilter.addMouseListener(new MouseAdapter() {
 
                             @Override
                             public void mouseDown(MouseEvent e) {
                                 selection.clear();
                                 selection.addAll(groups);
                                 tableViewer.setFilters(new ViewerFilter[] {});
                                 updateButtons();
                                 shell.close();
                             }
                         });
 
                         Label lblTextFilters = new Label(shell, SWT.LEFT);
                         lblTextFilters.setText("Text Filters...");
                         setLayoutData(lblTextFilters, lblClearFilter);
 
                         lblTextFilters.addMouseListener(new MouseAdapter() {
 
                             @Override
                             public void mouseDown(MouseEvent e) {
                                 System.out.println("'Text filters' clicked");
                             }
                         });
 
                         Label lblSeparator2 = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
                         setLayoutData(lblSeparator2, lblTextFilters);
 
                         final Button btnSearch = new Button(shell, SWT.PUSH);
                         btnSearch.setText("Clear");
                         btnSearch.setEnabled(false);
 
                         final Text txtSearch = new Text(shell, SWT.SINGLE | SWT.BORDER);
                         FormData formData = new FormData();
                         formData.top = new FormAttachment(lblSeparator2, 5);
                         formData.left = new FormAttachment(0, 5);
                         formData.right = new FormAttachment(80, -5);
                         txtSearch.setLayoutData(formData);
 
                         formData = new FormData();
                         formData.top = new FormAttachment(lblSeparator2, 5);
                         formData.left = new FormAttachment(80, 2);
                         formData.right = new FormAttachment(100, -5);
                         btnSearch.setLayoutData(formData);
 
                         final CheckboxTreeViewer treeViewer = new CheckboxTreeViewer(shell);
                         configureTreeViewer(treeViewer);
 
                         // setLayoutData(treeViewer.getControl(), btnSearch);
 
                         btnSearch.addSelectionListener(new SelectionAdapter() {
 
                             @Override
                             public void widgetSelected(SelectionEvent e) {
                                 treeViewer.setFilters(new ViewerFilter[0]);
                                 txtSearch.setText("");
                                 btnSearch.setEnabled(false);
                             }
                         });
                         txtSearch.addModifyListener(new ModifyListener() {
 
                             @Override
                             public void modifyText(ModifyEvent e) {
                                 String text = txtSearch.getText();
                                 if (text != null && !text.isEmpty()) {
                                     treeViewer.setAllChecked(false);
                                     regexViewerFilter.setFilterText(text);
                                     treeViewer.setFilters(new ViewerFilter[] {regexViewerFilter});
                                     btnSearch.setEnabled(true);
                                 }
                             }
                         });
                         Button btnSave = new Button(shell, SWT.PUSH);
                         btnSave.setText("OK");
                         btnSave.addSelectionListener(new SelectionAdapter() {
 
                             @Override
                             public void widgetSelected(SelectionEvent e) {
                                 selection = new ArrayList<String>();
 
                                 for (Object element : treeViewer.getCheckedElements()) {
                                     if (txtSearch.getText().isEmpty()
                                             || (!txtSearch.getText().isEmpty() && matches(txtSearch.getText(), (String)element))) {
                                         selection.add((String)element);
 
                                     }
                                 }
                                 tableViewer.setFilters(new ViewerFilter[] {new StatisticsAggregationFilter(selection)});
                                 updateButtons();
                                 shell.close();
                             }
 
                         });
 
                         formData = new FormData();
                         // formData.top = new FormAttachment(treeViewer.getControl(), 5);
                         formData.bottom = new FormAttachment(100, -5);
                         formData.left = new FormAttachment(50, 5);
                         formData.right = new FormAttachment(75, -5);
                         btnSave.setLayoutData(formData);
 
                         formData = new FormData();
                         formData.top = new FormAttachment(btnSearch, 5);
                         formData.left = new FormAttachment(0, 5);
                         formData.right = new FormAttachment(100, -5);
                         formData.bottom = new FormAttachment(btnSave, -5);
                         treeViewer.getControl().setLayoutData(formData);
 
                         Button btnClose = new Button(shell, SWT.PUSH);
                         btnClose.setText("Close");
                         btnClose.addSelectionListener(new SelectionAdapter() {
 
                             @Override
                             public void widgetSelected(SelectionEvent e) {
                                 shell.close();
                             }
                         });
 
                         formData = new FormData();
                         // formData.top = new FormAttachment(treeViewer.getControl(), 5);
                         formData.bottom = new FormAttachment(100, -5);
                         formData.left = new FormAttachment(75, 5);
                         formData.right = new FormAttachment(100, -5);
                         btnClose.setLayoutData(formData);
 
                         shell.addShellListener(new ShellAdapter() {
 
                             @Override
                             public void shellDeactivated(ShellEvent e) {
                                 shell.close();
                             }
 
                         });
 
                         shell.open();
 
                     } else {
                         updateSorting(table, colNum, currentColumn);
                     }
                 }
 
             });
 
         }
     }
 
     /**
      * Enables and disables report button according to selection
      */
     private void updateButtons() {
 
         if (selection != null) {
             bReport.setEnabled(selection.size() < MAX_GROUPS_PER_CHART);
         } else {
             bReport.setEnabled(groups.size() < MAX_GROUPS_PER_CHART);
         }
     }
 
     /**
      * Updates sorting
      * 
      * @param table viewer table
      * @param colNum column number
      * @param currentColumn selected column
      */
     private void updateSorting(final Table table, final int colNum, TableColumn currentColumn) {
         int sortDirection = table.getSortDirection();
         if (tableViewer.getTable().getSortColumn().equals(currentColumn)) {
             sortDirection = sortDirection == SWT.UP ? SWT.DOWN : SWT.UP;
         } else {
             sortDirection = SWT.UP;
         }
         table.setSortDirection(sortDirection);
         table.setSortColumn(currentColumn);
         // if (colNum == 0 || (isAdditionalColumnNecessary() && colNum == 1)) {
         // currentColumn.setImage(deselectedFilter);
         // }
         ((StatisticsComparator)tableViewer.getComparator()).update(colNum, sortDirection, isAdditionalColumnNecessary());
         tableViewer.refresh();
     }
 
     /**
      * Updates sorting
      * 
      * @param table viewer table
      * @param colNum column number
      * @param currentColumn selected column
      * @param direction sort direction
      */
     private void updateSorting(final Table table, final int colNum, TableColumn currentColumn, int direction) {
         table.setSortDirection(direction);
         table.setSortColumn(currentColumn);
         // if (colNum == 0 || (isAdditionalColumnNecessary() && colNum == 1)) {
         // currentColumn.setImage(deselectedFilter);
         // }
         ((StatisticsComparator)tableViewer.getComparator()).update(colNum, direction, isAdditionalColumnNecessary());
         tableViewer.refresh();
     }
 
     /**
      * Creates a site iterator for a given sector node
      * 
      * @param sectorNode sector node
      * @return site iterator
      */
     private Iterator<Node> getSiteIterator(Node sectorNode) {
         Iterator<Node> iter = sectorNode.traverse(Order.DEPTH_FIRST, new StopEvaluator() {
 
             @Override
             public boolean isStopNode(TraversalPosition currentPos) {
                 String type = (String)currentPos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME);
                 if ("site".equals(type)) {
                     return true;
                 }
                 if (NodeTypes.NETWORK.getId().equals(type)) {
                     return true;
                 }
                 return false;
             }
 
         }, new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 String type = (String)currentPos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME);
                 return "site".equals(type);
             }
 
         }, GeoNeoRelationshipTypes.CHILD, Direction.INCOMING).iterator();
         return iter;
     }
 
     /**
      * Configures a tree viewer - sets a content provider, a lable provider, input and adds
      * listeners
      * 
      * @param treeViewer tree viewer to configure
      */
     private void configureTreeViewer(final CheckboxTreeViewer treeViewer) {
         treeViewer.setContentProvider(new ITreeContentProvider() {
 
             @Override
             public Object[] getChildren(Object parentElement) {
                 return null;
             }
 
             @Override
             public Object getParent(Object element) {
                 return null;
             }
 
             @Override
             public boolean hasChildren(Object element) {
                 return false;
             }
 
             @Override
             public Object[] getElements(Object inputElement) {
                 List<String> input = (List<String>)inputElement;
                 int size = input.size();
                 Object[] elements = new Object[size + 1];
                 System.arraycopy(input.toArray(), 0, elements, 1, size);
                 elements[0] = SELECT_ALL;
                 return elements;
             }
 
             @Override
             public void dispose() {
             }
 
             @Override
             public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
             }
 
         });
         treeViewer.setLabelProvider(new ILabelProvider() {
 
             @Override
             public Image getImage(Object element) {
                 return null;
             }
 
             @Override
             public String getText(Object element) {
                 return (String)element;
             }
 
             @Override
             public void addListener(ILabelProviderListener listener) {
             }
 
             @Override
             public void dispose() {
             }
 
             @Override
             public boolean isLabelProperty(Object element, String property) {
                 return false;
             }
 
             @Override
             public void removeListener(ILabelProviderListener listener) {
             }
 
         });
         treeViewer.setInput(groups);
         treeViewer.addCheckStateListener(new ICheckStateListener() {
 
             @Override
             public void checkStateChanged(CheckStateChangedEvent event) {
                 final boolean checked = event.getChecked();
                 if (event.getElement().toString().equals(SELECT_ALL)) {
                     treeViewer.setAllChecked(checked);
                 } else if (!checked) {
                     treeViewer.setChecked(SELECT_ALL, false);
                     selection.remove(event.getElement());
                 }
             }
         });
         if (selection != null) {
             treeViewer.setCheckedElements(selection.toArray());
             if (selection.size() == groups.size()) {
                 treeViewer.setChecked(SELECT_ALL, true);
             }
         }
     }
 
     /**
      * Sets layout data
      * 
      * @param control control to set layout data
      */
     private void setLayoutData(Control control, Control controlAbove) {
         FormData formData = new FormData();
         formData.top = new FormAttachment(controlAbove, 5);
         formData.left = new FormAttachment(0, 5);
         formData.right = new FormAttachment(100, -5);
         control.setLayoutData(formData);
     }
 
     /**
      * Creates and sets the layout data
      * 
      * @param control control to set layout data
      * @param top a control above
      */
     private void setLayoutData(Control control, Integer top) {
         FormData formData = new FormData();
         formData.top = new FormAttachment(top, 5);
         formData.left = new FormAttachment(0, 5);
         formData.right = new FormAttachment(100, -5);
         control.setLayoutData(formData);
     }
 
     /**
      * Checks if the given text matches the filter text
      * 
      * @param filterText
      * @param textToCompare
      * @return true if matches
      */
     private boolean matches(String filterText, String textToCompare) {
         return textToCompare.toLowerCase().matches(".*" + filterText + ".*");
     }
 
     /**
      * Statistics Label Provider
      * 
      * @author Pechko_E
      * @since 1.0.0
      */
     class StatisticsLabelProvider implements ITableLabelProvider, ITableColorProvider {
         private Color color;
         private Color defaultColor;
         private Color backgroundColor;
         private boolean showAdditionalColumn;
 
         /**
          * @param device
          */
         public StatisticsLabelProvider(Device device, boolean showAdditionalColumn) {
 
             this.color = new Color(device, java.awt.Color.red.getRed(), java.awt.Color.red.getGreen(), java.awt.Color.red.getBlue());
             this.defaultColor = new Color(device, java.awt.Color.black.getRed(), java.awt.Color.black.getGreen(),
                     java.awt.Color.black.getBlue());
             this.backgroundColor = new Color(device, java.awt.Color.white.getRed(), java.awt.Color.white.getGreen(),
                     java.awt.Color.white.getBlue());
             this.showAdditionalColumn = showAdditionalColumn;
         }
 
         @Override
         public Image getColumnImage(Object element, int columnIndex) {
             return null;
         }
 
         @Override
         public String getColumnText(Object element, int columnIndex) {
             if (element instanceof StatisticsCell[]) {
                 StatisticsCell[] cells = (StatisticsCell[])element;
                 StatisticsRow row = cells[0].getParent();
                 StatisticsGroup group = row.getParent();
                 switch (columnIndex) {
                 case 0:
                     if (showAdditionalColumn) {
                         final Node keyNode = group.getKeyNode();
                         if (keyNode != null) {
                             Iterator<Node> iter = getSiteIterator(keyNode);
                             if (iter.hasNext()) {
                                 return iter.next().getProperty(INeoConstants.PROPERTY_NAME_NAME, "unknown").toString();
                             }
                         }
                     } else {
                         return group.getGroupName();
                     }
                     break;
                 case 1:
                     if (showAdditionalColumn) {
                         return group.getGroupName();
                     } else {
                         return row.getName();
                     }
                 case 2:
                     if (showAdditionalColumn) {
                         return row.getName();
                     } else {
                         return getFormattedValue(cells[columnIndex - 2]);
                     }
                 default:
                     return getFormattedValue(cells[columnIndex - (showAdditionalColumn ? 3 : 2)]);
                 }
             }
             return "";
         }
 
         /**
          * @return
          */
         private String getFormattedValue(StatisticsCell cell) {
             Number value = cell.getValue();
             if (value == null) {
                 return "";
             }
             return new DecimalFormat("0.0").format(value.doubleValue());
         }
 
         @Override
         public void addListener(ILabelProviderListener listener) {
         }
 
         @Override
         public void dispose() {
             color.dispose();
             defaultColor.dispose();
             backgroundColor.dispose();
         }
 
         @Override
         public boolean isLabelProperty(Object element, String property) {
             return false;
         }
 
         @Override
         public void removeListener(ILabelProviderListener listener) {
         }
 
         @Override
         public Color getBackground(Object element, int columnIndex) {
             return backgroundColor;
         }
 
         @Override
         public Color getForeground(Object element, int columnIndex) {
             if (element instanceof StatisticsCell[]) {
                 StatisticsCell[] cells = (StatisticsCell[])element;
                 if (columnIndex > (showAdditionalColumn ? 2 : 1) && cells[columnIndex - (showAdditionalColumn ? 3 : 2)].isFlagged()) {
                     return color;
                 }
             }
             return defaultColor;
         }
 
         /**
          * @return Returns the showAdditionalColumn.
          */
         public boolean isShowAdditionalColumn() {
             return showAdditionalColumn;
         }
 
         /**
          * @param showAdditionalColumn The showAdditionalColumn to set.
          */
         public void setShowAdditionalColumn(boolean showAdditionalColumn) {
             this.showAdditionalColumn = showAdditionalColumn;
         }
     }
 
     /**
      * Statistics Content Provider
      * 
      * @author Pechko_E
      * @since 1.0.0
      */
     class StatisticsContentProvider implements IStructuredContentProvider {
 
         @Override
         public Object[] getElements(Object inputElement) {
             if (inputElement instanceof Statistics) {
                 List<StatisticsCell[]> elements = new ArrayList<StatisticsCell[]>();
                 Statistics statistics = (Statistics)inputElement;
                 for (StatisticsGroup group : statistics.getGroups().values()) {
                     Collection<StatisticsRow> values = group.getRows().values();
                     for (StatisticsRow row : values) {
                         StatisticsCell[] cellsAsArray = row.getCellsAsArray();
                         if (cellsAsArray.length != 0) {
                             elements.add(cellsAsArray);
                         }
                     }
                 }
                 return elements.toArray();
             }
             return null;
         }
 
         @Override
         public void dispose() {
         }
 
         @Override
         public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
         }
 
     }
 
     /**
      * Filter by period
      * 
      * @author Pechko_E
      * @since 1.0.0
      */
     class StatisticsRowFilter extends ViewerFilter {
         private long start;
         private long end;
 
         /**
          * @param start
          * @param end
          */
         private StatisticsRowFilter(long start, long end) {
             this.start = start;
             this.end = end;
         }
 
         @Override
         public boolean select(Viewer viewer, Object parentElement, Object element) {
             StatisticsCell[] cells = (StatisticsCell[])element;
             StatisticsRow row = cells[0].getParent();
             if (row.isSummaryNode()) {
                 return true;
             }
             Long period = row.getPeriod();
             return (period >= start) && (period <= end);
         }
 
     }
 
     /**
      * Filter by aggregation
      * 
      * @author Pechko_E
      * @since 1.0.0
      */
     class StatisticsAggregationFilter extends ViewerFilter {
         private List<String> values;
 
         /**
          * @param start
          * @param end
          */
         private StatisticsAggregationFilter(List<String> values) {
             this.values = values;
         }
 
         @Override
         public boolean select(Viewer viewer, Object parentElement, Object element) {
             StatisticsCell[] cells = (StatisticsCell[])element;
             StatisticsRow row = cells[0].getParent();
             StatisticsGroup group = row.getParent();
             return values.contains(group.getGroupName());
         }
 
     }
 
     /**
      * Comparator for statistics cells
      * 
      * @author Pechko_E
      * @since 1.0.0
      */
     class StatisticsComparator extends ViewerComparator {
         private int direction = SWT.UP;
         private int column = 0;
         private boolean showAdditionalColumn;
 
         public void update(int column, int direction, boolean showAdditionalColumn) {
             this.column = column;
             this.direction = direction;
             this.showAdditionalColumn = showAdditionalColumn;
         }
 
         @Override
         public int compare(Viewer viewer, Object e1, Object e2) {
             int result = 0;
             try {
                 StatisticsCell[] cells1 = (StatisticsCell[])e1;
                 StatisticsCell[] cells2 = (StatisticsCell[])e2;
                 StatisticsRow row1 = cells1[0].getParent();
                 StatisticsRow row2 = cells2[0].getParent();
                 StatisticsGroup group1 = row1.getParent();
                 StatisticsGroup group2 = row2.getParent();
                 switch (column) {
                 case 0:
                     if (showAdditionalColumn) {
                         final Node sectorNode1 = group1.getKeyNode();
                         final Node sectorNode2 = group2.getKeyNode();
                         if (sectorNode1 != null && sectorNode2 != null) {
                             result = getSiteName(sectorNode1).compareTo(getSiteName(sectorNode2));
                         } else {
                             result = 0;
                         }
                     } else {
                         if (row1 != null && row2 != null) {
                             result = group1.getGroupName().compareTo(group2.getGroupName());
                         }
                     }
                     break;
                 case 1:
                     if (showAdditionalColumn) {
                         result = group1.getGroupName().compareTo(group2.getGroupName());
                     } else {
                         result = comparePeriods(result, row1, row2);
                     }
                     break;
                 case 2:
                     if (showAdditionalColumn) {
                         result = comparePeriods(result, row1, row2);
                     } else {
                         result = compareValues(cells1, cells2, column - (showAdditionalColumn ? 3 : 2));
                     }
                     break;
                 default:
                     result = compareValues(cells1, cells2, column - (showAdditionalColumn ? 3 : 2));
                 }
                 if (direction == SWT.DOWN) {
                     result = -result;
                 }
             } catch (RuntimeException e) {
                 e.printStackTrace();
                 // TODO Handle RuntimeException
                 throw (RuntimeException)new RuntimeException().initCause(e);
             }
             return result;
         }
 
         /**
          * @param result
          * @param row1
          * @param row2
          * @return
          */
         private int comparePeriods(int result, StatisticsRow row1, StatisticsRow row2) {
             if (row1.isSummaryNode()) {
                 if (!row2.isSummaryNode()) {
                     result = 1;
                 }
             } else {
                 if (!row2.isSummaryNode()) {
                     Long period1 = row1.getPeriod();
                     Long period2 = row2.getPeriod();
                     result = period1 == period2 ? 0 : period1 < period2 ? -1 : 1;
                 } else {
                     result = -1;
                 }
             }
             return result;
         }
 
         private String getSiteName(Node node) {
             Iterator<Node> iter = getSiteIterator(node);
             if (iter.hasNext()) {
                 return iter.next().getProperty(INeoConstants.PROPERTY_NAME_NAME, "unknown").toString();
 
             }
             return "unknown";
         }
 
         /**
          * @param cells1 a first array of cells to be compared
          * @param cells2
          * @return
          */
         private int compareValues(StatisticsCell[] cells1, StatisticsCell[] cells2, int index) {
             int result;
             Number val1 = cells1[index].getValue();
             Number val2 = cells2[index].getValue();
             if (val1 == null) {
                 result = val2 == null ? 0 : -1;
             } else {
                 if (val2 == null) {
                     result = 1;
                 } else {
                     double value1 = val1.doubleValue();
                     double value2 = val2.doubleValue();
                     result = value1 == value2 ? 0 : value1 < value2 ? -1 : 1;
                 }
 
             }
             return result;
         }
     }
 
     /**
      * Filter that uses regular expression
      * 
      * @author Pechko_E
      * @since 1.0.0
      */
     private class RegexViewerFilter extends ViewerFilter {
 
         private String filter;
 
         public RegexViewerFilter() {
         }
 
         public void setFilterText(String text) {
             this.filter = text;
         }
 
         @Override
         public boolean select(Viewer viewer, Object parentElement, Object element) {
             if (filter == null || filter.isEmpty()) {
                 return true;
             }
             String clearedText = clearTextFromSpecialChars(filter);
             String elem = ((String)element);
             if (elem.equals(SELECT_ALL)) {
                 return true;
             }
             return matches(clearedText, elem);
         }
 
     }
 
     public String clearTextFromSpecialChars(String filter) {
         return filter.toLowerCase().replaceAll("\\.", "\\.");
     }
 }
