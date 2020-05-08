 package confdb.gui;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.tree.*;
 import javax.swing.table.*;
 import java.awt.*;
 import java.awt.event.*;
 
 import java.util.ArrayList;
 
 import java.util.concurrent.ExecutionException;
 
 
 import confdb.parser.PythonParser;
 import confdb.parser.ParserException;
 
 import confdb.db.ConfDB;
 import confdb.db.DatabaseException;
 
 import confdb.migrator.DatabaseMigrator;
 import confdb.migrator.ReleaseMigrator;
 
 import confdb.gui.treetable.TreeTableTableModel;
 
 import confdb.data.*;
 
 
 /**
  * ConfDbGUI
  * ---------
  * @author Philipp Schieferdecker
  *
  * Graphical User Interface to create and manipulate cmssw
  * configurations stored in the Configuration Database, ConfDB.
  */
 public class ConfDbGUI implements TableModelListener
 {
     //
     // member data
     //
 
     /** administrator accounts */
     private ArrayList<String> admins = new ArrayList<String>();
     
     /** name of the current user according to system env */
     private String userName = null;
 
     /** main frame of the application */
     private JFrame frame = null; 
     
     /** the current software release (collection of all templates) */
     private SoftwareRelease currentRelease = null;
 
     /** the current configuration */
     private Configuration currentConfig = null;
     
     /** the current software release for imports */
     private SoftwareRelease importRelease = null;
 
     /** the import configuration */
     private Configuration importConfig = null;
     
     /** handle access to the database */
     private ConfDB database = null;
 
     /** ConverterService */
     private ConverterService converterService = null;
 
     /** the menu bar of the application */
     private ConfDBMenuBar menuBar = null;
     
     /** tree models for both the current and the import tree */
     private ConfigurationTreeModel currentTreeModel = null;
     private ConfigurationTreeModel importTreeModel = null;
     private StreamTreeModel        streamTreeModel = null;
 
     /** GUI components */
     private DatabaseInfoPanel  dbInfoPanel        = new DatabaseInfoPanel();
     private ConfigurationPanel configurationPanel = null;
     private InstancePanel      instancePanel      = null;
     private JProgressBar       progressBar        = null;
     private JTree              currentTree        = null;
     private JTree              importTree         = null;
     private JTree              streamTree         = null;
 
     
     //
     // construction
     //
     
     /** standard constructor */
     public ConfDbGUI(JFrame frame)
     {
 	this.userName         = System.getProperty("user.name");
 	this.frame            = frame;
 	this.currentRelease   = new SoftwareRelease();
 	this.currentConfig    = new Configuration();
 	this.importRelease    = new SoftwareRelease();
 	this.importConfig     = new Configuration();
 	this.database         = new ConfDB();
 	this.converterService = new ConverterService(database);
 	this.instancePanel    = new InstancePanel(frame);
 	
 	this.admins.add("schiefer");
 	this.admins.add("meschi");
 	this.admins.add("mzanetti");
 	
 	// current configuration tree
 	currentTreeModel = new ConfigurationTreeModel(currentConfig);
 	currentTree      = new JTree(currentTreeModel) {
 		public String getToolTipText(MouseEvent evt) {
 		    String text = null;
 
 		    ConfigurationTreeModel model =
 			(ConfigurationTreeModel)getModel();
 		    Configuration config = (Configuration)model.getRoot();
 		    
 		    if (getRowForLocation(evt.getX(),evt.getY()) == -1)
 			return text;
 
 		    TreePath tp = getPathForLocation(evt.getX(),evt.getY());
 		    Object selectedNode = tp.getLastPathComponent();
 		    if (selectedNode instanceof Path) {
 			Path path = (Path)selectedNode;
 			if (path.streamCount()>0) {
 			    text = path.name()+" assigned to stream(s): ";
 			    for (int i=0;i<path.streamCount();i++)
 				text += path.stream(i) + " ";
 			}
 		    }
 		    else if (selectedNode instanceof ESSourceInstance||
 			     selectedNode instanceof ESModuleInstance||
 			     selectedNode instanceof ModuleInstance) {
 			Instance instance = (Instance)selectedNode;
 			text = instance.template().name();
 		    }
 		    else if (selectedNode instanceof ModuleReference) {
 			ModuleReference reference=(ModuleReference)selectedNode;
 			ModuleInstance  instance =(ModuleInstance)reference.parent();
 			text = instance.template().name();
 		    }
 		    return text;
 		}
 	    };
 	currentTree.setToolTipText("");
 	currentTree.setRootVisible(false);
 	currentTree.setEditable(true);
 	currentTree.getSelectionModel()
 	    .setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 
 	ConfigurationTreeRenderer renderer = new ConfigurationTreeRenderer();
 	currentTree.setCellRenderer(renderer);
 	currentTree.setCellEditor(new ConfigurationTreeEditor(currentTree,
 							      renderer));
 	
 	ConfigurationTreeMouseListener mouseListener =
 	    new ConfigurationTreeMouseListener(currentTree,frame,
 					       currentRelease);
 	currentTree.addMouseListener(mouseListener);
 	currentTreeModel.addTreeModelListener(mouseListener);
 	
 	ConfigurationTreeTransferHandler currentDndHandler =
 	    new ConfigurationTreeTransferHandler(currentTree,currentRelease,
 						 instancePanel
 						 .parameterTreeModel());
 	currentTree.setTransferHandler(currentDndHandler);
 	currentTree.setDropTarget(new ConfigurationTreeDropTarget());
 	currentTree.setDragEnabled(true);
 	
 	// import tree
 	Color defaultTreeBackground = UIManager.getColor("Tree.textBackground");
 	Color importTreeBackground  = UIManager.getColor("Button.background");
 	UIManager.put("Tree.textBackground",importTreeBackground);
 	importTreeModel = new ConfigurationTreeModel(importConfig);
 	importTree      = new JTree(importTreeModel);
         importTree.setBackground(importTreeBackground);
 
 	importTree.setRootVisible(false);
 	importTree.setEditable(false);
 	importTree.getSelectionModel().setSelectionMode(TreeSelectionModel
 							.SINGLE_TREE_SELECTION);
 	importTree.setCellRenderer(new ConfigurationTreeRenderer());
 	
 	
 	ConfigurationTreeTransferHandler importDndHandler =
 	    new ConfigurationTreeTransferHandler(importTree,null,null);
 	importTree.setTransferHandler(importDndHandler);
 	importTree.setDropTarget(new ConfigurationTreeDropTarget());
 	importTree.setDragEnabled(true);
 	
 	UIManager.put("Tree.textBackground",defaultTreeBackground);
 	
 	// stream tree TODO
 	streamTreeModel = new StreamTreeModel(currentConfig);
 	streamTree      = new JTree(streamTreeModel);
 	streamTree.setEditable(true);
 	streamTree.getSelectionModel().setSelectionMode(TreeSelectionModel
 							.SINGLE_TREE_SELECTION);
 	StreamTreeRenderer streamTreeRenderer = new StreamTreeRenderer();
 	streamTree.setCellRenderer(streamTreeRenderer);
 	streamTree.setCellEditor(new StreamTreeEditor(streamTree,streamTreeRenderer));
 	
 	StreamTreeMouseListener streamTreeMouseListener =
 	    new StreamTreeMouseListener(streamTree);
 	streamTree.addMouseListener(streamTreeMouseListener);
 	streamTreeModel.addTreeModelListener(streamTreeMouseListener);
 	
 	
 	// instance panel (right side), placed in createContentPane()
 	instancePanel.setConfigurationTreeModel(currentTreeModel);
 	instancePanel.addTableModelListener(this);
 	currentTree.addTreeSelectionListener(instancePanel);
 	  
 	// configuration panel (left side), placed in createContentPane()
 	configurationPanel = new ConfigurationPanel(this,
 						    currentTree,importTree,streamTree,
 						    converterService);
 
 	frame.addWindowListener(new WindowAdapter() {
 		public void windowClosing(WindowEvent e)
 		{
 		    closeConfiguration(false);
 		    disconnectFromDatabase();
 		}
         });
 	
 	
 	Runtime.getRuntime().addShutdownHook(new Thread() {
 		public void run()
 		{
 		    closeConfiguration(false);
 		    disconnectFromDatabase();
 		}
 	    });
     }
     
     
     //
     // member functions
     //
 
     /** TableModelListener: tableChanged() */
     public void tableChanged(TableModelEvent e)
     {
 	Object source = e.getSource();
 	if (source instanceof TreeTableTableModel) {
 	    TreeTableTableModel tableModel = (TreeTableTableModel)source;
 
 	    Parameter node         = (Parameter)tableModel.changedNode();
 	    Object    childNode    = tableModel.childNode();
 	    int       childIndex   = tableModel.childIndex();
 	    String    typeOfChange = tableModel.typeOfChange();
 
 	    if (node!=null) {
 		Object parent = node.parent();
 		while (parent instanceof Parameter) {
 		    Parameter p = (Parameter)parent;
 		    parent = p.parent();
 		}
 		
 		if (childNode==null)
 		    currentTreeModel.nodeChanged(node);
 		else if (typeOfChange.equals("REMOVE"))
 		    currentTreeModel.nodeRemoved(node,childIndex,childNode);
 		else if (typeOfChange.equals("INSERT"))
 		    currentTreeModel.nodeInserted(node,childIndex);
 		
 		if (parent instanceof ModuleInstance) currentTree.updateUI();
 		currentTreeModel.updateLevel1Nodes();
 		currentConfig.setHasChanged(true);
 	    }
 	}
     }
     
     /** connect to the database */
     public void connectToDatabase()
     {
 	// close currently open configuration
 	if (!closeConfiguration()) return;
 	
 	// query the database info from the user in a dialog
 	DatabaseConnectionDialog dbDialog = new DatabaseConnectionDialog(frame);
 	dbDialog.pack();
 	dbDialog.setLocationRelativeTo(frame);
 	dbDialog.setVisible(true);
 	
 	// retrieve the database parameters from the dialog
 	if (!dbDialog.validChoice()) return;
 	String dbType = dbDialog.getDbType();
 	String dbHost = dbDialog.getDbHost();
 	String dbPort = dbDialog.getDbPort();
 	String dbName = dbDialog.getDbName();
 	String dbUrl  = dbDialog.getDbUrl();
 	String dbUser = dbDialog.getDbUser();
 	String dbPwrd = dbDialog.getDbPassword();
 	
 	try {
 	    database.connect(dbType,dbUrl,dbUser,dbPwrd);
 	    dbInfoPanel.connectedToDatabase(dbType,dbHost,dbPort,dbName,dbUser);
 	}
 	catch (DatabaseException e) {
 	    String msg = "Failed to connect to DB: " + e.getMessage();
 	    JOptionPane.showMessageDialog(frame,msg,"",JOptionPane.ERROR_MESSAGE);
 	}
 	menuBar.dbConnectionIsEstablished();
     }
     
     /** connect to the database */
     public void disconnectFromDatabase()
     {
 	if (!closeConfiguration()) return;
 	
 	try {
 	    database.disconnect();
 	    dbInfoPanel.disconnectedFromDatabase();
 	    currentRelease.clear("");
 	}
 	catch (DatabaseException e) {
 	    String msg = "Failed to disconnect from DB: " + e.getMessage();
 	    JOptionPane.showMessageDialog(frame,msg,"",JOptionPane.ERROR_MESSAGE);
 	}
 	menuBar.dbConnectionIsNotEstablished();
     }
     
     /** migrate configuration to another database */
     public void exportConfiguration()
     {
 	if (!checkConfiguration()) return;
 	
 	ConfigurationExportDialog dialog =
 	    new ConfigurationExportDialog(frame,currentConfig.name());
 
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 
 	if (dialog.validChoice()) {
 	    ConfDB targetDB   = dialog.targetDB();
 	    String      targetName = dialog.targetName();
 	    Directory   targetDir  = dialog.targetDir();
 	    
 	    ExportConfigurationThread worker =
 		new ExportConfigurationThread(targetDB,targetName,targetDir);
 	    worker.start();
 	    progressBar.setIndeterminate(true);
 	    progressBar.setVisible(true);
 	    progressBar.setString("Migrate Configuration to " +
 				  targetDB.dbUrl() + " ... ");
 	}
     }
     
 
     /** new configuration */
     public void newConfiguration()
     {
 	if (!closeConfiguration()) return;
 	
 	ConfigurationNameDialog dialog = new ConfigurationNameDialog(frame,database);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	if (dialog.validChoice()) {
 	    String name       = dialog.name();
 	    String process    = dialog.process();
 	    String releaseTag = dialog.releaseTag();
 	    
 	    NewConfigurationThread worker =
 		new NewConfigurationThread(name,process,releaseTag);
 	    worker.start();
 	    progressBar.setIndeterminate(true);
 	    progressBar.setVisible(true);
 	    progressBar.setString("Loading Templates for Release " +
 				  dialog.releaseTag() + " ... ");
 	    menuBar.configurationIsOpen();
 	}
     }
     
     /** parse configuration from *.py file */
     public void parseConfiguration()
     {
 	if (!closeConfiguration()) return;
 	
 	ConfigurationParseDialog dialog =
 	    new ConfigurationParseDialog(frame,database);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	if (dialog.validChoice()) {
 	    String fileName   = dialog.fileName();
 	    String releaseTag = dialog.releaseTag();
 	    
 	    ParseConfigurationThread worker =
 		new ParseConfigurationThread(fileName,releaseTag);
 	    worker.start();
 	    progressBar.setIndeterminate(true);
 	    progressBar.setVisible(true);
 	    progressBar.setString("Parsing '"+fileName+"' against Release " +
 				  releaseTag + " ... ");
 	    menuBar.configurationIsOpen();
 	}
     }
     
     /** open configuration */
     public void openConfiguration()
     {
 	if (database.dbUrl().equals(new String())) return;
 	if (!closeConfiguration()) return;
 	
 	OpenConfigurationDialog dialog =
 	    new OpenConfigurationDialog(frame,database);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	if (dialog.validChoice()) {
 	    OpenConfigurationThread worker =
 		new OpenConfigurationThread(dialog.configInfo());
 	    worker.start();
 	    progressBar.setIndeterminate(true);
 	    progressBar.setVisible(true);
 	    progressBar.setString("Loading Configuration ...");
 	    menuBar.configurationIsOpen();
 	}
     }
 
     /** import configuration */
     public void importConfiguration()
     {
 	ImportConfigurationDialog dialog =
 	    new ImportConfigurationDialog(frame,database,currentRelease.releaseTag());
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	if (dialog.validChoice()) {
 	    ImportConfigurationThread worker =
 		new ImportConfigurationThread(dialog.configInfo());
 	    worker.start();
 	    progressBar.setIndeterminate(true);
 	    progressBar.setVisible(true);
 	    progressBar.setString("Importing Configuration ...");
 	}
     }
     
     /** close configuration */
     public boolean closeConfiguration()
     {
 	return closeConfiguration(true);
     }
     
     /** close configuration */
     public boolean closeConfiguration(boolean showDialog)
     {
 	importConfig.reset();
 	importTreeModel.setConfiguration(importConfig);
 	configurationPanel.setImportConfig(importConfig);
 	
 	if (currentConfig.isEmpty()) return true;
 	
 	if (currentConfig.hasChanged()&&showDialog) {
 	    Object[] options = { "OK", "CANCEL" };
 	    int answer = 
 		JOptionPane.showOptionDialog(null,
 					     "The current configuration contains "+
 					     "unsaved changes, really close?",
 					     "Warning",
 					     JOptionPane.DEFAULT_OPTION,
 					     JOptionPane.WARNING_MESSAGE,
 					     null, options, options[1]);
 	    if (answer==1) return false;
 	}
 	
 	if (!currentConfig.isLocked()&&currentConfig.version()>0)
 	    database.unlockConfiguration(currentConfig);
 	currentRelease.clearInstances();
 	currentConfig.reset();
 	currentTreeModel.setConfiguration(currentConfig);
 	streamTreeModel.setConfiguration(currentConfig);
 	configurationPanel.setCurrentConfig(currentConfig);
 	instancePanel.clear();
 	menuBar.configurationIsNotOpen();
 	return true;
     }
     
     /** save configuration */
     public void saveConfiguration()
     {
 	if (currentConfig.isEmpty()) return;
 	if (!currentConfig.hasChanged()) return;
 	if (currentConfig.isLocked()) return;
 	if (!checkConfiguration()) return;	
 	
 	if (currentConfig.version()==0) {
 	    saveAsConfiguration();
 	    return;
 	}
 	else database.unlockConfiguration(currentConfig);
 	
 	SaveConfigurationThread worker =
 	    new SaveConfigurationThread();
 	worker.start();
 	progressBar.setIndeterminate(true);
 	progressBar.setString("Save Configuration ...");
 	progressBar.setVisible(true);
     }
     
     /** saveAs configuration */
     public void saveAsConfiguration()
     {
 	if (!checkConfiguration()) return;
 	if (currentConfig.isLocked()) return;
 	
 	if (currentConfig.version()!=0) {
 	    database.unlockConfiguration(currentConfig);
 	}
 	
 	SaveConfigurationDialog dialog =
 	    new SaveConfigurationDialog(frame,database,currentConfig);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	if (dialog.validChoice()) {
 	    SaveConfigurationThread worker =
 		new SaveConfigurationThread();
 	    worker.start();
 	    progressBar.setIndeterminate(true);
 	    progressBar.setString("Save Configuration ...");
 	    progressBar.setVisible(true);
 	}
	else
 	    database.lockConfiguration(currentConfig,userName);
     }
     
     /** migrate configuration (to another release) */
     public void migrateConfiguration()
     {
 	if (!checkConfiguration()) return;
 	
 	ConfigurationMigrationDialog dialog =
 	    new ConfigurationMigrationDialog(frame,database);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	String releaseTag = dialog.releaseTag();	
 	
 	if (releaseTag.length()>0) {
 	    MigrateConfigurationThread worker =
 		new MigrateConfigurationThread(releaseTag);
 	    worker.start();
 	    progressBar.setIndeterminate(true);
 	    progressBar.setVisible(true);
 	    progressBar.setString("Migrate configuration to release '" +
 				  releaseTag + "' ... ");
 	}
     }
     
 
     /** check if configuration is in a storable state */
     public boolean checkConfiguration()
     {
 	if (currentConfig.isEmpty()) return false;
 	int unsetParamCount = currentConfig.unsetTrackedParameterCount();
 	if (unsetParamCount>0) {
 	    String msg =
 		"The configuration contains " + unsetParamCount +
 		" unset tracked parameters. They must be set before saving!";
 	    JOptionPane.showMessageDialog(frame,msg,"",JOptionPane.ERROR_MESSAGE);
 	    return false;
 	}
 	return true;
     }
     
     /** show 'create templates' dialog */
     public void createTemplates()
     {
 	CreateTemplateDialog dialog = new CreateTemplateDialog(frame,database);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	String releaseTag = currentConfig.releaseTag();
 	
 	UpdateTemplatesThread worker =
 	    new UpdateTemplatesThread(releaseTag);
 	worker.start();
 	progressBar.setIndeterminate(true);
 	progressBar.setVisible(true);
 	progressBar.setString("Updating Templates for Release "+releaseTag+" ... ");
     }
     
 
     /** create the content pane */
     private JPanel createContentPane()
     {
 	JPanel contentPane = new JPanel(new GridBagLayout());
 	contentPane.setOpaque(true);
 	
 	GridBagConstraints c = new GridBagConstraints();
 	c.fill = GridBagConstraints.BOTH;
 	c.weightx = 0.5;
 	
 	c.gridx=0;c.gridy=0; c.weighty=0.01;
 	contentPane.add(dbInfoPanel,c);
 
 	JSplitPane  splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 					       configurationPanel,
 					       instancePanel);
 	splitPane.setOneTouchExpandable(true);
 	splitPane.setResizeWeight(0.4);
 	splitPane.setDividerLocation(0.4);
 	
 	c.gridx=0;c.gridy=1; c.weighty=0.98;
 	contentPane.add(splitPane,c);
 	
 	JPanel statusPanel = new JPanel(new GridLayout());
 	progressBar = new JProgressBar(0);
 	progressBar.setIndeterminate(true);
 	progressBar.setStringPainted(true);
 	progressBar.setVisible(false);
 	statusPanel.add(progressBar);
 
 	c.gridx=0;c.gridy=2; c.weighty=0.01;
 	contentPane.add(statusPanel,c);
 	
 	return contentPane;
     }
 
     /** create the menu bar */
     private void createMenuBar()
     {
 	menuBar = new ConfDBMenuBar(frame,this,admins.contains(userName));
 	menuBar.dbConnectionIsNotEstablished();
     }
     
     
     //
     // static member functions
     //
     
     /** create and show the ConfDbGUI */
     private static void createAndShowGUI()
     {
 	// create the application's main frame
 	JFrame frame = new JFrame("ConfDbGUI");
 	
 	// create the ConfDbGUI app and set it as the main frame's content pane
 	ConfDbGUI app = new ConfDbGUI(frame);
 	
 	// add the application's content pane to the main frame
 	frame.setContentPane(app.createContentPane());
 	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	
 	// add menu bar to the main frame
 	app.createMenuBar();
 	
 	// display the main frame
 	int frameWidth  = (int)(0.75*frame.getToolkit().getScreenSize().getWidth());
 	int frameHeight = (int)(0.75*frame.getToolkit().getScreenSize().getHeight());
 	int frameX      = (int)(0.125*frame.getToolkit().getScreenSize().getWidth());
 	int frameY      = (int)(0.10*frame.getToolkit().getScreenSize().getHeight());
 	frame.pack();
 	frame.setSize(frameWidth,frameHeight);
 	frame.setLocation(frameX,frameY);
 	frame.setVisible(true);
 		
 	// try to etablish a database connection
 	app.connectToDatabase();
     }
     
 
     /**
      * MAIN create and show GUI, thread-safe
      */
     public static void main(String[] args)
     {
 	javax.swing.SwingUtilities.invokeLater(new Runnable() {
 		public void run() {  createAndShowGUI(); }
 	    });
     }
     
 
     //
     // threads *not* to be executed on the EDT
     //
 
     /**
      * migrate current configuration to another database
      */
     private class ExportConfigurationThread extends SwingWorker<String>
     {
 	/** target database */
 	private ConfDB targetDB = null;
 	
 	/** name of the configuration in the target DB */
 	private String targetName = null;
 	
 	/** target directory */
 	private Directory targetDir = null;
 	
 	/** database migrator */
 	DatabaseMigrator migrator = null;
 
 	/** start time */
 	private long startTime;
 	
 	/** standard constructor */
 	public ExportConfigurationThread(ConfDB targetDB,
 					 String targetName,Directory targetDir)
 	{
 	    this.targetDB   = targetDB;
 	    this.targetName = targetName;
 	    this.targetDir  = targetDir;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws InterruptedException
 	{
 	    startTime = System.currentTimeMillis();
 	    migrator = new DatabaseMigrator(currentConfig,database,targetDB);
 	    migrator.migrate(targetName,targetDir);
 	    return new String("Done!");
 	}
 	
 	/** SwingWorker: finished */
 	protected void finished()
 	{
 	    try {
 		targetDB.disconnect();
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		progressBar.setString(progressBar.getString() +
 				      get() + " (" + elapsedTime + " ms)");
 		MigrationReportDialog dialog =
 		    new MigrationReportDialog(frame,migrator.releaseMigrator());
 		dialog.setTitle("Configuration Export Report");
 		dialog.pack();
 		dialog.setLocationRelativeTo(frame);
 		dialog.setVisible(true);
 	    }
 	    catch (Exception e) {
 		System.out.println("EXCEPTION: "+ e.getMessage());
 		e.printStackTrace();
 		progressBar.setString(progressBar.getString() + "FAILED!");	
 	    }
 	    progressBar.setIndeterminate(false);
 	}
     }
     
 
     /**
      * load release templates from the database
      */
     private class NewConfigurationThread extends SwingWorker<String>
     {
 	/** name of the new configuration */
 	private String name = null;
 	
 	/** process of the new configuration */
 	private String process = null;
 	
 	/** release to be loaded */
 	private String releaseTag = null;
 	
 	/** start time */
 	private long startTime;
 	
 	/** standard constructor */
 	public NewConfigurationThread(String name,String process,String releaseTag)
 	{
 	    this.name       = name;
 	    this.process    = process;
 	    this.releaseTag = releaseTag;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws InterruptedException
 	{
 	    startTime = System.currentTimeMillis();
 	    if (!releaseTag.equals(currentRelease.releaseTag()))
 		database.loadSoftwareRelease(releaseTag,currentRelease);
 	    return new String("Done!");
 	}
 	
 	/** SwingWorker: finished */
 	protected void finished()
 	{
 	    try {
 		currentConfig = new Configuration();
 		currentConfig.initialize(new ConfigInfo(name,null,releaseTag),
 					 process,
 					 currentRelease);
 		currentTreeModel.setConfiguration(currentConfig);
 		streamTreeModel.setConfiguration(currentConfig);
 		configurationPanel.setCurrentConfig(currentConfig);
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		progressBar.setString(progressBar.getString() +
 				      get() + " (" + elapsedTime + " ms)");
 	    }
 	    catch (Exception e) {
 		System.out.println("EXCEPTION: "+ e.getMessage());
 		e.printStackTrace();
 		progressBar.setString(progressBar.getString() + "FAILED!");	
 	    }
 	    progressBar.setIndeterminate(false);
 
 	    currentTree.setEditable(true);
 	    instancePanel.setEditable(true);
 	}
     }
     
 
     /**
      * load release templates from the database and parse config from *.py
      */
     private class ParseConfigurationThread extends SwingWorker<String>
     {
 	/** name of the file to be parsed */
 	private String fileName = null;
 	
 	/** release to be loaded */
 	private String releaseTag = null;
 	
 	/** start time */
 	private long startTime;
 	
 	/** standard constructor */
 	public ParseConfigurationThread(String fileName,String releaseTag)
 	{
 	    this.fileName   = fileName;
 	    this.releaseTag = releaseTag;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws InterruptedException
 	{
 	    startTime = System.currentTimeMillis();
 	    if (!releaseTag.equals(currentRelease.releaseTag()))
 		database.loadSoftwareRelease(releaseTag,currentRelease);
 
 	    try {
 		PythonParser parser = new PythonParser(currentRelease);
 		parser.parseFile(fileName);
 		currentConfig = parser.createConfiguration();
 		if (parser.closeProblemStream())
 		    System.out.println("problems encountered, " +
 				       "see problems.txt.");
 	    }
 	    catch (ParserException e) {
 		System.err.println("Error parsing "+fileName+": "+
 				   e.getMessage());
 	    }
 	    
 	    return new String("Done!");
 	}
 	
 	/** SwingWorker: finished */
 	protected void finished()
 	{
 	    try {
 		currentTreeModel.setConfiguration(currentConfig);
 		streamTreeModel.setConfiguration(currentConfig);
 		configurationPanel.setCurrentConfig(currentConfig);
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		progressBar.setString(progressBar.getString() +
 				      get() + " (" + elapsedTime + " ms)");
 	    }
 	    catch (Exception e) {
 		System.err.println("EXCEPTION: "+ e.getMessage());
 		e.printStackTrace();
 		progressBar.setString(progressBar.getString() + "FAILED!");	
 	    }
 	    progressBar.setIndeterminate(false);
 
 	    currentTree.setEditable(true);
 	    instancePanel.setEditable(true);
 	}
     }
     
 
     /**
      * load a configuration from the database
      */
     private class OpenConfigurationThread extends SwingWorker<String>
     {
 	/** configuration info */
 	private ConfigInfo configInfo = null;
 	
 	/** start time */
 	private long startTime;
 	
 	/** standard constructor */
 	public OpenConfigurationThread(ConfigInfo configInfo)
 	{
 	    this.configInfo = configInfo;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws InterruptedException
 	{
 	    startTime = System.currentTimeMillis();
 	    currentConfig = database.loadConfiguration(configInfo,currentRelease);
 	    return new String("Done!");
 	}
 	
 	/** SwingWorker: finished */
 	protected void finished()
 	{
 	    try {
 		currentTreeModel.setConfiguration(currentConfig);
 		streamTreeModel.setConfiguration(currentConfig);
 		configurationPanel.setCurrentConfig(currentConfig);
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		progressBar.setString(progressBar.getString() +
 				      get() + " (" + elapsedTime + " ms)");
 	    
 	    }
 	    catch (ExecutionException e) {
 		System.out.println("EXECUTION-EXCEPTION: "+ e.getCause());
 		e.printStackTrace();
 	    }
 	    catch (Exception e) {
 		System.out.println("EXCEPTION: "+ e.getMessage());
 		e.printStackTrace();
 		progressBar.setString(progressBar.getString() + "FAILED!");
 	    }
 	    progressBar.setIndeterminate(false);
 
 	    // check if the loaded config is locked and thus read-only
 	    if (currentConfig.isLocked()) {
 		currentTree.setEditable(false);
 		instancePanel.setEditable(false);
 		String msg =
 		    "The configuration '" +
 		    currentConfig.parentDir().name() + "/" +
 		    currentConfig.name() + " is locked by user '" +
 		    currentConfig.lockedByUser() + "'!\n" +
 		    "You can't manipulate any of its versions " +
 		    "until it is released.";
 		JOptionPane.showMessageDialog(frame,msg,"READ ONLY!",
 					      JOptionPane.WARNING_MESSAGE,
 					      null);
 	    }
 	    else {
 		currentTree.setEditable(true);
 		instancePanel.setEditable(true);
 		database.lockConfiguration(currentConfig,userName);
 	    }
 	}
     }
 
     /**
      * import a configuration from the database
      */
     private class ImportConfigurationThread extends SwingWorker<String>
     {
 	/** configuration info */
 	private ConfigInfo configInfo = null;
 	
 	/** start time */
 	private long startTime;
 	
 	/** standard constructor */
 	public ImportConfigurationThread(ConfigInfo configInfo)
 	{
 	    this.configInfo = configInfo;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws InterruptedException
 	{
 	    startTime = System.currentTimeMillis();
 	    if (importRelease.releaseTag()!=currentRelease.releaseTag()) {
 		importRelease = new SoftwareRelease(currentRelease);
 	    }
 	    importConfig = database.loadConfiguration(configInfo,importRelease);
 	    return new String("Done!");
 	}
 	
 	/** SwingWorker: finished */
 	protected void finished()
 	{
 	    try {
 		importTreeModel.setConfiguration(importConfig);
 		configurationPanel.setImportConfig(importConfig);
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		progressBar.setString(progressBar.getString() +
 				      get() + " (" + elapsedTime + " ms)");
 	    }
 	    catch (ExecutionException e) {
 		System.out.println("EXECUTION-EXCEPTION: " + e.getCause());
 		e.printStackTrace();
 	    }
 	    catch (Exception e) {
 		System.out.println("EXCEPTION: "+ e.getMessage());
 		e.printStackTrace();
 		progressBar.setString(progressBar.getString() + "FAILED!");
 	    }
 	    progressBar.setIndeterminate(false);
 	}
     }
     
     /**
      * save a configuration in the database
      */
     private class SaveConfigurationThread extends SwingWorker<String>
     {
 	/** start time */
 	private long startTime;
 	
 	/** standard constructor */
 	public SaveConfigurationThread() {}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws InterruptedException
 	{
 	    startTime = System.currentTimeMillis();
 	    database.insertConfiguration(currentConfig,userName);
 	    if (!currentConfig.isLocked())
 		database.lockConfiguration(currentConfig,userName);
 	    return new String("Done!");
 	}
 	
 	/** SwingWorker: finished */
 	protected void finished()
 	{
 	    try {
 		currentConfig.setHasChanged(false);
 		configurationPanel.setCurrentConfig(currentConfig);
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		progressBar.setString(progressBar.getString() +
 				      get() + " (" + elapsedTime + " ms)");
 	    }
 	    catch (Exception e) {
 		System.out.println("EXCEPTION: "+ e.getMessage());
 		e.printStackTrace();
 		progressBar.setString(progressBar.getString() + "FAILED!");
 	    }
 	    progressBar.setIndeterminate(false);
 	}
     }
 
     /**
      * migrate a configuration in the database to a new release
      */
     private class MigrateConfigurationThread extends SwingWorker<String>
     {
 	/** the target release */
 	private String targetReleaseTag = null;
 	
 	/** release migrator */
 	private ReleaseMigrator migrator = null;
 	
 	/** start time */
 	private long startTime;
 	
 	/** standard constructor */
 	public MigrateConfigurationThread(String targetReleaseTag)
 	{
 	    this.targetReleaseTag = targetReleaseTag;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws InterruptedException
 	{
 	    startTime = System.currentTimeMillis();
 	    
 	    SoftwareRelease targetRelease = new SoftwareRelease();
 	    database.loadSoftwareRelease(targetReleaseTag,targetRelease);
 
 	    ConfigInfo targetConfigInfo =
 		new ConfigInfo(currentConfig.name(),currentConfig.parentDir(),
 			       -1,currentConfig.version(),
 			       "",userName,targetReleaseTag);
 	    String targetProcessName = currentConfig.processName();
 	    Configuration targetConfig = new Configuration(targetConfigInfo,
 							   targetProcessName,
 							   targetRelease);
 	    
 	    migrator = new ReleaseMigrator(currentConfig,targetConfig);
 	    migrator.migrate();
 	    
 	    database.insertConfiguration(targetConfig,userName);
 	    currentRelease.clearInstances();
 	    currentConfig =
 		database.loadConfiguration(targetConfigInfo,currentRelease);
 	    return new String("Done!");
 	}
 	
 	/** SwingWorker: finished */
 	protected void finished()
 	{
 	    try {
 		currentConfig.setHasChanged(false);
 		currentTreeModel.setConfiguration(currentConfig);
 		streamTreeModel.setConfiguration(currentConfig);
 		configurationPanel.setCurrentConfig(currentConfig);
 		instancePanel.clear();
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		progressBar.setString(progressBar.getString() +
 				      get() + " (" + elapsedTime + " ms)");
 		MigrationReportDialog dialog =
 		    new MigrationReportDialog(frame,migrator);
 		dialog.setTitle("Release-Migration Report");
 		dialog.pack();
 		dialog.setLocationRelativeTo(frame);
 		dialog.setVisible(true);
 	    }
 	    catch (Exception e) {
 		System.out.println("EXCEPTION: "+ e.getMessage());
 		e.printStackTrace();
 		progressBar.setString(progressBar.getString() + "FAILED!");
 	    }
 	    progressBar.setIndeterminate(false);
 	}
     }
 
     /**
      * load release templates from the database
      */
     private class UpdateTemplatesThread extends SwingWorker<String>
     {
 	/** release to be loaded */
 	private String releaseTag = null;
 	
 	/** start time */
 	private long startTime;
 	
 	/** standard constructor */
 	public UpdateTemplatesThread(String releaseTag)
 	{
 	    this.releaseTag = releaseTag;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws InterruptedException
 	{
 	    startTime = System.currentTimeMillis();
 	    if (!releaseTag.equals(currentRelease.releaseTag()))
 		database.loadSoftwareRelease(releaseTag,currentRelease);
 	    return new String("Done!");
 	}
 	
 	/** SwingWorker: finished */
 	protected void finished()
 	{
 	    try {
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		progressBar.setString(progressBar.getString() +
 				      get() + " (" + elapsedTime + " ms)");
 	    }
 	    catch (Exception e) {
 		System.out.println("EXCEPTION: "+ e.getMessage());
 		e.printStackTrace();
 		progressBar.setString(progressBar.getString() + "FAILED!");	
 	    }
 	    progressBar.setIndeterminate(false);
 	}
     }
 
 
 }
