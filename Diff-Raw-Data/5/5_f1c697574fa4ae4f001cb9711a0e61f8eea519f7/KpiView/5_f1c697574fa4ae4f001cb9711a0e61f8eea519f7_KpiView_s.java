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
 package org.amanzi.awe.views.kpi;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.amanzi.integrator.awe.AWEProjectManager;
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.GisTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.core.utils.Pair;
 import org.amanzi.neo.core.utils.PropertyHeader;
 import org.amanzi.splash.utilities.NeoSplashUtil;
 import org.apache.log4j.Logger;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 import org.jruby.Ruby;
 import org.jruby.parser.EvalStaticScope;
 import org.jruby.runtime.DynamicScope;
 import org.jruby.runtime.ThreadContext;
 import org.jruby.runtime.builtin.IRubyObject;
 import org.jruby.runtime.scope.ManyVarsDynamicScope;
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
  * Kpi builder view
  * </p>
  * 
  * @author Cinkel_A
  * @since 1.0.0
  */
 public class KpiView extends ViewPart {
     private static final Logger LOGGER = Logger.getLogger(KpiView.class);
 
     /** String TEST_TITLE field */
     private static final String TEST_TITLE = "Parsing script";
 
     /** String TITLE field */
     private static final String TITLE = "Execution Result";
 
     /**
      * The ID of the view as specified by the extension.
      */
 	public static final String ID = "org.amanzi.awe.views.kpi.KpiView";
 
     private static final String B_RUN = "Run";
 
     private static final String B_TEST = "Test";
 
     private static final String TEST_ERROR = "Script has errors:\n%s";
 
     private static final String TEST_NO_ERR = "Script parsing has passed successfully";
 
     private static final String LB_NETWORK = "Network:";
 
     private static final String LB_DRIVE = "Drive:";
     
     private final String LB_DIRECTORY="Directory:";
     
     private final String LB_COUNTER_FILE="Counter file:";
 
     private static final String B_SAVE = "Save";
 
     private static final String B_INIT = "Init";
 
     private static final String DIALOG_TITLE = "Open Ruby Script file";
 
     private static final String[] RUBY_FILE_NAMES = new String[] {"Ruby files (*.rb"};
     private static final String[] RUBY_FILE_EXT = new String[] {"*.rb"};
 
     private Text editor;
 
     private Button bRun;
 
     private Button bTest;
 
     private List formulaList;
 
     private Combo networkNode;
 
     private Combo driveNode;
 
     private Combo directoriesNode;
     
     private Combo counterFilesNode;
     
     private List propertyList;
 
     private LinkedHashMap<String, Node> networks = new LinkedHashMap<String, Node>();
 
     private LinkedHashMap<String, Node> drives = new LinkedHashMap<String, Node>();
 
     private LinkedHashMap<String, Node> directories= new LinkedHashMap<String, Node>();
     
     private LinkedHashMap<String, Node> counters= new LinkedHashMap<String, Node>();
     
     private Button bSave;
 
     private Button bInit;
 
 	private final String INIT_SCRIPT="init.rb";
 	
 	private String fileName="";
 	
 	private String formulaName="";
 	
 	private String parameters="";
 
     private Text txtFormulaName;
 
     private Text txtParameters;
 
     private Button btnElements;
 
     private Button btnCollections;
 
 	/**
 	 * The constructor.
 	 */
 	public KpiView() {
 	}
 
 	/**
 	 * This is a callback that will allow us
 	 * to create the viewer and initialize it.
 	 */
 	@Override
     public void createPartControl(Composite parent) {
         Composite frame = new Composite(parent, SWT.NONE);
         FormLayout mainLayout = new FormLayout();
         frame.setLayout(mainLayout);
 
         Composite bottom = new Composite(frame, SWT.NONE);
         editor = new Text(frame, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
         bRun = new Button(bottom, SWT.PUSH);
         bRun.setText(B_RUN);
         bRun.setImage(KPIPlugin.getImageDescriptor("icons/run.gif").createImage());
         bTest = new Button(bottom, SWT.PUSH);
         bTest.setText(B_TEST);
         bTest.setImage(KPIPlugin.getImageDescriptor("icons/test.gif").createImage());
         bSave = new Button(bottom, SWT.PUSH);
         bSave.setText(B_SAVE);
         bSave.setImage(KPIPlugin.getImageDescriptor("icons/save.png").createImage());
         bInit = new Button(bottom, SWT.PUSH);
         bInit.setText(B_INIT);
         bInit.setImage(KPIPlugin.getImageDescriptor("icons/init.png").createImage());
        
         Composite top = new Composite(frame, SWT.NONE);
         
         Label labelNetwork = new Label(top, SWT.LEFT);
         labelNetwork.setText(LB_NETWORK);
         networkNode = new Combo(top, SWT.DROP_DOWN | SWT.READ_ONLY);
         
         Label labelDrive = new Label(top, SWT.LEFT);
         labelDrive.setText(LB_DRIVE);
         driveNode = new Combo(top, SWT.DROP_DOWN | SWT.READ_ONLY);
         
         Label labelDirectory = new Label(top, SWT.LEFT);
         labelDirectory.setText(LB_DIRECTORY);
         directoriesNode = new Combo(top, SWT.DROP_DOWN | SWT.READ_ONLY);
         
         Label labelCounterFile = new Label(top, SWT.LEFT);
         labelCounterFile.setText(LB_COUNTER_FILE);
         counterFilesNode= new Combo(top, SWT.DROP_DOWN | SWT.READ_ONLY);
         
         /*Label labelFormulaName = new Label(top, SWT.LEFT);
         labelFormulaName.setText(LB_FORMULA_NAME);
         txtFormulaName=new Text(top, SWT.SINGLE);
         Label labelParameters = new Label(top, SWT.LEFT);
         labelParameters.setText(LB_PARAMETERS);
         txtParameters=new Text(top, SWT.SINGLE);
         Label labelType = new Label(top, SWT.LEFT);
         labelType.setText(LB_TYPE);
         btnElements=new Button(top,SWT.RADIO);
         btnElements.setText(LB_ELEMENTS);
         btnElements.setSelection(true);
         btnCollections=new Button(top,SWT.RADIO);
         btnCollections.setText(LB_COLLECTIONS);*/
         // top
         Composite right = new Composite(frame, SWT.NONE);
         FormData layoutData = new FormData();
         layoutData.top = new FormAttachment(0, 2);
         layoutData.left = new FormAttachment(0, 2);
         layoutData.right = new FormAttachment(right, -2);
         // layoutData.bottom = new FormAttachment(networkNode, );
         top.setLayoutData(layoutData);
         top.setLayout(new FormLayout());
 
         layoutData = new FormData();
         layoutData.top = new FormAttachment(networkNode, 5, SWT.CENTER);
         layoutData.left = new FormAttachment(0, 2);
         labelNetwork.setLayoutData(layoutData);
 
         layoutData = new FormData();
         layoutData.top = new FormAttachment(0, 2);
         layoutData.left = new FormAttachment(labelCounterFile, 5);
         layoutData.right = new FormAttachment(100, -5);
         networkNode.setLayoutData(layoutData);
 
         layoutData = new FormData();
         layoutData.top = new FormAttachment(driveNode, 15, SWT.CENTER);
         layoutData.left = new FormAttachment(0, 2);
         labelDrive.setLayoutData(layoutData);
 
         layoutData = new FormData();
         layoutData.top = new FormAttachment(networkNode, 2);
         layoutData.left = new FormAttachment(labelCounterFile, 5);
         layoutData.right = new FormAttachment(100, -5);
         driveNode.setLayoutData(layoutData);
         
         layoutData = new FormData();
         layoutData.top = new FormAttachment(directoriesNode, 15, SWT.CENTER);
         layoutData.left = new FormAttachment(0, 2);
         labelDirectory.setLayoutData(layoutData);
         
         layoutData = new FormData();
         layoutData.top = new FormAttachment(driveNode, 2);
         layoutData.left = new FormAttachment(labelCounterFile, 5);
         layoutData.right = new FormAttachment(100, -5);
         directoriesNode.setLayoutData(layoutData);
         
         layoutData = new FormData();
         layoutData.top = new FormAttachment(counterFilesNode, 15, SWT.CENTER);
         layoutData.left = new FormAttachment(0, 2);
         labelCounterFile.setLayoutData(layoutData);
         
         layoutData = new FormData();
         layoutData.top = new FormAttachment(directoriesNode, 2);
         layoutData.left = new FormAttachment(labelCounterFile, 5);
         layoutData.right = new FormAttachment(100, -5);
         counterFilesNode.setLayoutData(layoutData);
         /*
         layoutData=new FormData();
         layoutData.top=new FormAttachment(networkNode, 2);
         layoutData.left=new FormAttachment(0, 2);
         layoutData.right=new FormAttachment(25, 2);
         labelFormulaName.setLayoutData(layoutData);
         
         layoutData=new FormData();
         layoutData.top=new FormAttachment(networkNode, 2);
         layoutData.left=new FormAttachment(25, 0);
         layoutData.right=new FormAttachment(100, -2);
         txtFormulaName.setLayoutData(layoutData);
         
         layoutData=new FormData();
         layoutData.top=new FormAttachment(txtFormulaName, 2);
         layoutData.left=new FormAttachment(0, 2);
         layoutData.right=new FormAttachment(25, 2);
         labelParameters.setLayoutData(layoutData);
         
         layoutData=new FormData();
         layoutData.top=new FormAttachment(txtFormulaName, 2);
         layoutData.left=new FormAttachment(25, 0);
         layoutData.right=new FormAttachment(100, -2);
         txtParameters.setLayoutData(layoutData);
         
         layoutData=new FormData();
         layoutData.top=new FormAttachment(txtParameters, 2);
         layoutData.left=new FormAttachment(0, 2);
         layoutData.right=new FormAttachment(25, 2);
         labelType.setLayoutData(layoutData);
         
         layoutData=new FormData();
         layoutData.top=new FormAttachment(txtParameters, 2);
         layoutData.left=new FormAttachment(labelType, 2);
         layoutData.right=new FormAttachment(50, -2);
         btnElements.setLayoutData(layoutData);
         
         layoutData=new FormData();
         layoutData.top=new FormAttachment(txtParameters, 2);
         layoutData.left=new FormAttachment(50, 2);
         layoutData.right=new FormAttachment(75, -2);
         btnCollections.setLayoutData(layoutData);
         */
         
 
         // bottom
         layoutData = new FormData();
         layoutData.bottom = new FormAttachment(100, -2);
         layoutData.left = new FormAttachment(0, 2);
         layoutData.right = new FormAttachment(right, -2);
         // layoutData.top = new FormAttachment(100, -50);
         bottom.setLayoutData(layoutData);
 
         // right
         layoutData = new FormData();
         layoutData.top = new FormAttachment(0, 2);
         layoutData.bottom = new FormAttachment(100, -2);
         layoutData.left = new FormAttachment(60, 2);
         layoutData.right = new FormAttachment(100, -2);
         right.setLayoutData(layoutData);
 
         // editor
         layoutData = new FormData();
         layoutData.left = new FormAttachment(0, 2);
         layoutData.top = new FormAttachment(top, 2);
         layoutData.right = new FormAttachment(right, -2);
         layoutData.bottom = new FormAttachment(bottom, -2);
         editor.setLayoutData(layoutData);
 
         //buttons
         bottom.setLayout(new GridLayout(4, true));
 
         // bRun.setLayoutData(layoutData);
         //
         // layoutData = new FormData();
         // layoutData.left = new FormAttachment(60);
         // layoutData.right = new FormAttachment(90);
         // layoutData.bottom = new FormAttachment(100, -2);
         // bTest.setLayoutData(layoutData);
 
         right.setLayout(new GridLayout(2, true));
         // right
         Label label = new Label(right, SWT.CENTER);
         label.setText("Formulas:");
         label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
         label = new Label(right, SWT.CENTER);
         label.setText("Properties:");
         label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
 
         formulaList = new List(right, SWT.BORDER | SWT.V_SCROLL);
 
         GridData layoutDataPr = new GridData(GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL | GridData.FILL_HORIZONTAL);
         layoutDataPr.horizontalSpan = 1;
         formulaList.setLayoutData(layoutDataPr);
         layoutDataPr = new GridData(GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL | GridData.FILL_HORIZONTAL);
         layoutDataPr.horizontalSpan = 1;
         propertyList = new List(right, SWT.BORDER | SWT.V_SCROLL|SWT.H_SCROLL);
         propertyList.setLayoutData(layoutDataPr);
         // TODO implement save function
         bSave.setVisible(false);
         fillList();
         addListeners();
 
     }
 
     /**
      *
      */
     private void fillList() {
         fillFormulas();
         networkNode.setItems(getAllNetworks());
         driveNode.setItems(getAllDrives());
         directoriesNode.setItems(getAllCounterDirectories());
     }
 
     /**
      *
      */
     private void fillFormulas() {
         formulaList.setItems(getAllFormulas());
     }
 
     /**
      * @return
      */
     private String[] getAllDrives() {
         Transaction tx = NeoUtils.beginTransaction();
         try {
             GraphDatabaseService service = NeoServiceProvider.getProvider().getService();
             Node refNode = service.getReferenceNode();
             drives = new LinkedHashMap<String, Node>();
             for (Relationship relationship : refNode.getRelationships(Direction.OUTGOING)) {
                 Node node = relationship.getEndNode();
                 Object type = node.getProperty(INeoConstants.PROPERTY_GIS_TYPE_NAME, "");
                 if (node.hasProperty(INeoConstants.PROPERTY_TYPE_NAME)
                         && node.hasProperty(INeoConstants.PROPERTY_NAME_NAME)
                         && node.getProperty(INeoConstants.PROPERTY_TYPE_NAME).toString().equalsIgnoreCase(
                                 NodeTypes.GIS.getId()) && GisTypes.DRIVE.getHeader().equals(type)) {
                     String id = node.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
                     drives.put(id, node);
                 }
             }
             String[] result = drives.keySet().toArray(new String[] {});
             Arrays.sort(result);
             return result;
         } finally {
             tx.finish();
         }
     }
     /**
      * Gets counter directories
      * @return counter directories
      */
     private String[] getAllCounterDirectories() {
         Transaction tx = NeoUtils.beginTransaction();
         try {
             GraphDatabaseService service = NeoServiceProvider.getProvider().getService();
             Node refNode = service.getReferenceNode();
             directories = new LinkedHashMap<String, Node>();
             for (Relationship relationship : refNode.getRelationships(Direction.OUTGOING)) {
                 Node node = relationship.getEndNode();
                 if (node.hasProperty(INeoConstants.PROPERTY_TYPE_NAME)
                         && node.getProperty(INeoConstants.PROPERTY_TYPE_NAME).toString().equalsIgnoreCase(GisTypes.OSS.getHeader())) {
                     String id = node.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
                     directories.put(id, node);
                 }
             }
             
             for (Relationship relationship : refNode.getRelationships(Direction.OUTGOING)) {
                 Node node = relationship.getEndNode();
                 Object type = node.getProperty(INeoConstants.PROPERTY_GIS_TYPE_NAME, "");
                 if (node.hasProperty(INeoConstants.PROPERTY_TYPE_NAME)
                         && node.hasProperty(INeoConstants.PROPERTY_NAME_NAME)
                         && node.getProperty(INeoConstants.PROPERTY_TYPE_NAME).toString().equalsIgnoreCase(
                                 NodeTypes.GIS.getId()) && GisTypes.OSS.getHeader().equals(type)) {
                    String id = node.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
                    directories.put(id, node);
                 }
             }
             String[] result = directories.keySet().toArray(new String[] {});
             Arrays.sort(result);
             return result;
         } finally {
             tx.finish();
         }
     }
     /**
      * Gets counter directories
      * @return counter directories
      */
     private String[] getAllCounterFiles() {
         Transaction tx = NeoUtils.beginTransaction();
         try {
             Node directoryNode = directories.get(directoriesNode.getText());
             counters = new LinkedHashMap<String, Node>();
             for (Relationship relationship : directoryNode.getRelationships(GeoNeoRelationshipTypes.CHILD,Direction.OUTGOING)) {
                 Node node = relationship.getEndNode();
                 if (node.hasProperty(INeoConstants.PROPERTY_TYPE_NAME)) {
                     String id = node.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
                     counters.put(id, node);
                 }
                 Traverser traverse = node.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator(){
 
                     @Override
                     public boolean isReturnableNode(TraversalPosition currentPos) {
                         return true;
                     }
                     
                 }, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING);
                 for (Node fileNode:traverse){
                     String id = fileNode.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
                     counters.put(id, fileNode);
                 }
             }
             return counters.keySet().toArray(new String[] {});
         } finally {
             tx.finish();
         }
     }
 
     /**
      * @return
      */
     private String[] getAllNetworks() {
         Transaction tx = NeoUtils.beginTransaction();
         try {
             GraphDatabaseService service = NeoServiceProvider.getProvider().getService();
             Node refNode = service.getReferenceNode();
             networks = new LinkedHashMap<String, Node>();
             for (Relationship relationship : refNode.getRelationships(Direction.OUTGOING)) {
                 Node node = relationship.getEndNode();
                 Object type = node.getProperty(INeoConstants.PROPERTY_GIS_TYPE_NAME, "");
                 if (node.hasProperty(INeoConstants.PROPERTY_TYPE_NAME)
                         && node.hasProperty(INeoConstants.PROPERTY_NAME_NAME)
                         && node.getProperty(INeoConstants.PROPERTY_TYPE_NAME).toString().equalsIgnoreCase(
                                 NodeTypes.GIS.getId()) && GisTypes.NETWORK.getHeader().equals(type)) {
                     String id = node.getProperty(INeoConstants.PROPERTY_NAME_NAME).toString();
                     networks.put(id, node);
                 }
             }
             String[] result = networks.keySet().toArray(new String[] {});
             Arrays.sort(result);
             return result;
         } finally {
             tx.finish();
         }
     }
 
     /**
      * get List of all formula
      * 
      * @return String[]
      */
     private String[] getAllFormulas() {
         try {
             ArrayList<String> result = new ArrayList<String>();
             Ruby rubyRuntime = KPIPlugin.getDefault().getRubyRuntime();
             IRubyObject formula = rubyRuntime.evalScriptlet(getFormulaScript());
             LOGGER.debug(formula);
             Object[] array = formula.convertToArray().toArray();
             for (Object met : array) {
                 result.add(met.toString());
             }
             return result.toArray(new String[0]);
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
     }
 
     private void initRubyScript() {
         final FileDialog dlg = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
         dlg.setText(DIALOG_TITLE);
         dlg.setFilterNames(RUBY_FILE_NAMES);
         dlg.setFilterExtensions(RUBY_FILE_EXT);
         dlg.setFilterPath(KPIPlugin.getDefault().getDirectory());
         final String filename = dlg.open();
         if (filename != null) {
             String script = NeoSplashUtil.getScriptContent(filename);
             testInitScriptAndRun(script);
         }
     }
 
 	private void testInitScriptAndRun(String script) {
 		try {
 			Pair<Boolean, Exception> parseResult = testScript(script);
 			if (parseResult.getLeft()) {
 			    KPIPlugin.getDefault().getRubyRuntime().evalScriptlet(script);
 			    fillFormulas();
 			    LOGGER.debug("fillFormulas");
 			} else {
 			    testError(parseResult.getRight().getLocalizedMessage());
 			}
 		} catch (RuntimeException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 
     /**
      * get script content for "formula.rb"
      * 
      * @return script content
      */
     private String getFormulaScript() {
         return "allFormula";
 //        URL scriptURL;
 //        try {
 //            scriptURL = FileLocator.toFileURL(KPIPlugin.getDefault().getBundle().getEntry(JRUBY_SCRIPT));
 //        } catch (IOException e) {
 //            return null;
 //        }
 //        return NeoSplashUtil.getScriptContent(scriptURL.getPath());
     }
 
     /**
      *add listener
      */
     private void addListeners() {
         bRun.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 runScript();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         bTest.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 testScript();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         formulaList.addMouseListener(new MouseListener() {
 
             @Override
             public void mouseUp(MouseEvent e) {
             }
 
             @Override
             public void mouseDown(MouseEvent e) {
             }
 
             @Override
             public void mouseDoubleClick(MouseEvent e) {
                 insertFormula();
             }
         });
         propertyList.addMouseListener(new MouseListener() {
             
             @Override
             public void mouseUp(MouseEvent e) {
             }
             
             @Override
             public void mouseDown(MouseEvent e) {
             }
             
             @Override
             public void mouseDoubleClick(MouseEvent e) {
                 insertProperty();
             }
         });
         SelectionListener listener = new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 formPropertyList();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         };
         networkNode.addSelectionListener(listener);
         driveNode.addSelectionListener(listener);
         counterFilesNode.addSelectionListener(listener);
         directoriesNode.addSelectionListener(new SelectionAdapter(){
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                     counterFilesNode.setItems(getAllCounterFiles());
             }
             
         });
         bInit.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 initRubyScript();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
         bSave.addSelectionListener(new SelectionAdapter(){
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
                 try {
                     //validate
                     //if errors show dlg
                     //else save
                     //if all OK show dlg
                     String scriptText = getScriptText();
                     if (scriptText != null && scriptText.length() != 0) {
                         Shell shell = getSite().getWorkbenchWindow().getShell();
 
                         SaveFormulaDialog dialog = new SaveFormulaDialog(shell, "Save formula");
                         dialog.setFormulaText(scriptText);
                         dialog.setFileName(fileName);
                         dialog.setFormulaName(formulaName);
                         dialog.setParameters(parameters);
 //                        if (dialog.open() != null) {
                         formulaName = txtFormulaName.getText();
                             fileName = formulaName+".rb";
                             parameters = txtParameters.getText();
 //                            fileName = dialog.getFileName();
 //                            formulaName = dialog.getFormulaName();
 //                            parameters = dialog.getParameters();
                             LOGGER.debug(fileName);
                             LOGGER.debug(formulaName);
                             LOGGER.debug(parameters);
 
                             String aweProjectName = AWEProjectManager.getActiveProjectName();
                             IRubyProject rubyProject = NewRubyElementCreationWizard.configureRubyProject(null, aweProjectName);
                             IFolder folder = rubyProject.getProject().getFolder(new Path(KPIPlugin.KPI_FOLDER));
 
                             if (!folder.exists()) {
                                 folder.create(false, true, null);
                             }
                             // update formula script file
                             IFile formulaScript = folder.getFile(fileName);
                             if (!formulaScript.exists()) {
                                 ByteArrayInputStream is = new ByteArrayInputStream(scriptText.getBytes());
                                 formulaScript.create(is, true, null);
                                 is.close();
                                 LOGGER.debug("Formula script was created");
                             } else {
                                 ByteArrayInputStream is = new ByteArrayInputStream(scriptText.getBytes());
                                 formulaScript.setContents(is, IFile.FORCE, null);
                             }
                             String methodText = KPIUtils.generateRubyMethod(formulaName, parameters);
                             if (btnElements.getSelection()){
                                 IFile elementsScript = folder.getFile(KPIUtils.ELEMENT_FORMULAS_SCRIPT);
                                 createOrUpdateScript(methodText, elementsScript);
                             }else if (btnCollections.getSelection()){
                                 IFile collectionsScript = folder.getFile(KPIUtils.COLLECTION_FORMULAS_SCRIPT);
                                 createOrUpdateScript(methodText, collectionsScript);
                             }else{
                                 LOGGER.debug("Warning: Neither element type nor collection type selected!");
                             }
                             IFile initScript = folder.getFile(INIT_SCRIPT);
                             if (!initScript.exists()){
                                 String initScriptText=KPIUtils.generateInitScript();
                                 ByteArrayInputStream is = new ByteArrayInputStream(initScriptText.getBytes());
                                 initScript.create(is, true, null);
                                 is.close();
                                 IFile elementsScript = folder.getFile(KPIUtils.ELEMENT_FORMULAS_SCRIPT);
                                 if (!elementsScript.exists()) {
                                     createEmptyScriptFile(elementsScript);
                                 } 
                                 IFile collectionsScript = folder.getFile(KPIUtils.COLLECTION_FORMULAS_SCRIPT);
                                 if (!collectionsScript.exists()) {
                                     createEmptyScriptFile(collectionsScript);
                                 } 
                                 testInitScriptAndRun(initScriptText);
                                 LOGGER.debug("Init script was created");
                             }else{
                                 StringBuffer sb = new StringBuffer();
                                 KPIUtils.readContentToStringBuffer(initScript.getContents(), sb);
                                 testInitScriptAndRun(sb.toString());
                                 
                             }
 
                     }
                 } catch (Exception e1) {
                     // TODO Auto-generated catch block
                     e1.printStackTrace();
                 }
 
             }
 
             private void createEmptyScriptFile(IFile scriptFile) throws IOException, CoreException {
                 ByteArrayInputStream is = new ByteArrayInputStream(new String().getBytes());
                 scriptFile.create(is, true, null);
                 is.close();
                 LOGGER.debug("Empty script "+scriptFile.getName()+" was created");
             }
 
 			private ByteArrayInputStream createOrUpdateFormulaScript(
 					StringBuffer sb, String methodText) {
 				String newScript = KPIUtils.insertOrUpdateRubyMethod(sb.toString(), methodText);
 				ByteArrayInputStream is = new ByteArrayInputStream(newScript.getBytes());
 				return is;
 			}
 
 			private void createOrUpdateScript(String methodText, IFile scriptFile) throws CoreException, IOException {
                 StringBuffer sb;
                 sb = new StringBuffer();
                 if (!scriptFile.exists()){
                     ByteArrayInputStream is = new ByteArrayInputStream(methodText.getBytes());
                     scriptFile.create(is, true, null);
                     is.close();
                     LOGGER.debug(scriptFile.getName()+" was created");
                 }else{
                 KPIUtils.readContentToStringBuffer(scriptFile.getContents(), sb);
                 ByteArrayInputStream is = createOrUpdateFormulaScript(sb, methodText);
                 scriptFile.setContents(is, IFile.FORCE, null);
                 }
             }
         });
     }
 
     /**
      *
      */
     protected void insertProperty() {
         String[] selection = propertyList.getSelection();
         if (selection.length != 1) {
             return;
         }
         String formula = selection[0];
         StringBuilder builder = new StringBuilder(editor.getText());
         int position = editor.getCaretPosition();
         builder.insert(position, formula);
         editor.setText(builder.toString());
         editor.setSelection(position + formula.length() + 1);
         editor.setFocus();
     }
 
     /**
      * Fills property list and saves ids to KPIPlugin fields
      */
     protected void formPropertyList() {
         Node netNode = networks.get(networkNode.getText());
         Long networkId = netNode == null ? null : netNode.getId();
         KPIPlugin.getDefault().setNetworkId(networkId);
        
         Node drivNode = drives.get(driveNode.getText());
         Long driveId = drivNode == null ? null : drivNode.getId();
         KPIPlugin.getDefault().setDriveId(driveId);
         
         Node dirNode = directories.get(directoriesNode.getText());
         Long dirId = dirNode == null ? null : dirNode.getId();
         KPIPlugin.getDefault().setDirectoryId(dirId);
         
         Node countNode = counters.get(counterFilesNode.getText());
         Long countId = countNode == null ? null : countNode.getId();
         KPIPlugin.getDefault().setCounterId(countId);
         
         IRubyObject result=runScript("init;find_kpis");
         Object[] array = result.convertToArray().toArray();
         LOGGER.debug("The following KPIs were found:");
         for (Object met : array) {
             LOGGER.debug("  - "+met.toString());
         }
         fillPropertyList();
     }
 
     /**
      * Fills property list on the base of user selection
      */
     private void fillPropertyList() {
         Transaction tx = NeoUtils.beginTransaction();
         try {
             ArrayList<String> result = new ArrayList<String>();
             // TODO get possible properties from ruby?
             Node netNode = networks.get(networkNode.getText());
             if (netNode != null) {
                 result.add("sites");
                 result.add("sectors");
                 String[] fields = new PropertyHeader(netNode).getNumericFields();
                 for (String string : fields) {
                     result.add("sectors." + string);
                 }
             }
             Node drivNode = drives.get(driveNode.getText());
             if (drivNode != null) {
                 //TODO Pechko_E add properties correctly 
                 String[] fields = new PropertyHeader(drivNode).getNumericFields();
                 if (fields.length!=0)
                 result.add("properties");
                 for (String string : fields) {
                     result.add("properties." + string);
                 }
 //                fields = new PropertyHeader(drivNode).getNumericFields();
                 Collection<String> events = new PropertyHeader(drivNode).getEvents();
                 if (events.size()!=0)
                     result.add("events");
                 Set<String> regexes=new HashSet<String>();
                 for (String event : events) {
                     Matcher matcher = Pattern.compile("(.*)(?=\\s\\[.*)").matcher(event);
                     if (matcher.find()){
                         String e = matcher.group(0);
                         regexes.add(e.replace("(", "\\(").replace(")", "\\)")+" \\[.*\\]");
                     }
                     if (regexes.isEmpty()){
                         result.add("events('" + event+"')");
                     }
                 }
                 for (String regex:regexes){
                     result.add("events(/" + regex+"/)");
                 }
             }
             Node dirNode = directories.get(directoriesNode.getText());
             if (dirNode != null) {
                 String[] counters = new PropertyHeader(dirNode).getNumericFields();
                 if (counters.length!=0)
                     result.add("counters");
                     for (String counter : counters) {
                         result.add("counters." + counter);
                     }
             }
             Collections.sort(result);
             propertyList.setItems(result.toArray(new String[0]));
         } finally {
             tx.finish();
         }
     }
 
     /**
      *insert formula into editor
      */
     protected void insertFormula() {
         String[] selection = formulaList.getSelection();
         if (selection.length != 1) {
             return;
         }
         String formula = selection[0] + "()";
         StringBuilder builder = new StringBuilder(editor.getText());
         int position = editor.getCaretPosition();
         builder.insert(position, formula);
         editor.setText(builder.toString());
         editor.setSelection(position + formula.length() - 1);
         editor.setFocus();
     }
 
     /**
      * parsing script in editor
      */
     protected void testScript() {
         Pair<Boolean, Exception> parceResult = testScript(getScriptText());
         if (parceResult.getLeft()) {
             testOk();
         } else {
             testError(parceResult.getRight().getLocalizedMessage());
         }
     }
 
     /**
      * parsing script
      * 
      * @param aTestScript -parsing script content
      * @return Pair<true-no errors,null or exception if error present>
      */
     protected Pair<Boolean, Exception> testScript(String aTestScript) {
         Ruby rubyRuntime = KPIPlugin.getDefault().getRubyRuntime();
         try {
             ThreadContext context = rubyRuntime.getCurrentContext();
             DynamicScope currentScope = context.getCurrentScope();
             ManyVarsDynamicScope newScope = new ManyVarsDynamicScope(new EvalStaticScope(currentScope.getStaticScope()),
                     currentScope);
             rubyRuntime.parseEval(aTestScript, "<script>", newScope, 0);
             return new Pair<Boolean, Exception>(true, null);
         } catch (Exception e) {
             return new Pair<Boolean, Exception>(false, e);
         }
     }
 
     /**
      * show message that test failing
      * 
      * @param message - error message
      */
     private void testError(String message) {
         MessageBox msg = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.OK);
         msg.setText(TEST_TITLE);
         String resultStr = String.format(TEST_ERROR, message);
         msg.setMessage(resultStr);
         msg.open();
     }
 
     /**
      *show message that parsing ok
      */
     private void testOk() {
         MessageBox msg = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.OK);
         msg.setText(TEST_TITLE);
         msg.setMessage(TEST_NO_ERR);
         msg.open();
     }
 
     /**
      *run script
      */
     protected void runScript() {
         final Ruby rubyRuntime = KPIPlugin.getDefault().getRubyRuntime();
 //        TODO
 //        IWorkbench workbench = PlatformUI.getWorkbench();
 //        IWorkbenchWindow[] workbenchWindows = workbench.getWorkbenchWindows();
 //        for (IWorkbenchWindow window:workbenchWindows){
 //            IEditorPart activeEditor = window.getActivePage().getActiveEditor();
 //            LOGGER.debug("[DEBUG] activeEditor "+activeEditor);
 //            if (activeEditor instanceof AbstractSplashEditor){
 //                AbstractSplashEditor splashEditor=(AbstractSplashEditor)activeEditor;
 //                TableModel model = splashEditor.getTable().getModel();
 //                if (model instanceof SplashTableModel){
 //                  LOGGER.debug("[DEBUG] model "+model);
 //                  IRubyObject rubyObject = JavaEmbedUtils.javaToRuby(rubyRuntime, model);
 //                  rubyRuntime.getGlobalVariables().define("$tableModel", new ValueAccessor(rubyObject));
 //                }
 //                
 //            }
 //        }
         final Display display = PlatformUI.getWorkbench().getDisplay();
         display.asyncExec(new Runnable() {
 
             @Override
             public void run() {
                 IRubyObject result;
                 try {
                     result = rubyRuntime.evalScriptlet(getScriptText());
                     outputResult(result);
                 } catch (Exception e) {
                     e.printStackTrace();
                     outputError(e);
                 }
             }
 
         });
     }
 
     /**
      *run script
      */
     protected IRubyObject runScript(String script) {
 
         IRubyObject result;
         try {
             result = KPIPlugin.getDefault().getRubyRuntime().evalScriptlet(script);
             return result;
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
 
     }
     /**
      * shows message if runs script throw exception
      * 
      * @param e - exception
      */
     private void outputError(Exception e) {
         MessageBox msg = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.OK);
         msg.setText(TITLE);
         String resultStr = "Script error:\n" + e.getLocalizedMessage();
         msg.setMessage(resultStr);
         msg.open();
     }
 
     /**
      * Output result of script execution
      * 
      * @param result - result of script execution
      */
     private void outputResult(IRubyObject result) {
         MessageBox msg = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.OK);
         msg.setText(TITLE);
         String resultStr = result == null ? "ERROR" : result.toString();
         msg.setMessage(resultStr);
         msg.open();
     }
 
     /**
      * get script from editor
      * 
      * @return script content
      */
     protected String getScriptText() {
         return editor.getText();
     }
 
 
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	@Override
     public void setFocus() {
         editor.setFocus();
 	}
 
     
 }
