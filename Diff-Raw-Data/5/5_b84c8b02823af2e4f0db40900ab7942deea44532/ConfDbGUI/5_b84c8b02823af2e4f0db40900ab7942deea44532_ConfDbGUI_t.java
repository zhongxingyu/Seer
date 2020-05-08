 package confdb.gui;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.tree.*;
 import javax.swing.table.*;
 import javax.swing.border.*;
 import javax.swing.plaf.basic.*;
 import java.awt.*;
 import java.awt.event.*;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.concurrent.ExecutionException;
 
 import java.io.FileWriter;
 import java.io.IOException;
 
 import confdb.gui.treetable.*;
 
 import confdb.db.ConfDB;
 import confdb.db.ConfOldDB;
 import confdb.db.DatabaseException;
 
 import confdb.migrator.DatabaseMigrator;
 import confdb.migrator.ReleaseMigrator;
 import confdb.migrator.MigratorException;
 
 import confdb.parser.PythonParser;
 import confdb.parser.ParserException;
 
 import confdb.converter.ConverterFactory;
 import confdb.converter.ConverterEngine;
 import confdb.converter.OfflineConverter;
 import confdb.converter.ConverterException;
 
 import confdb.data.*;
 
 
 /**
  * ConfDbGUI
  * ---------
  * @author Philipp Schieferdecker
  *
  * Graphical User Interface to create and manipulate CMSSW job
  * configurations stored in the relational configuration database,
  * ConfDB.
  */
 public class ConfDbGUI
 {
     //
     // member data
     //
 
     /** current user */
     private String userName = "";
     
     /** access to the ConfDB database */
     private ConfDB database = null;
     
     /** current software release (collection of all templates) */
     private SoftwareRelease currentRelease = null;
     
     /** the current configuration */
     private Configuration currentConfig = null;
     
     /** the current software release for imports */
     private SoftwareRelease importRelease = null;
 
     /** the import configuration */
     private Configuration importConfig = null;
     
     /** current parameter container (Instance | OuputModule) */
     private Object currentParameterContainer = null;
 
     /** ascii converter engine, to display config snippets (right-lower) */
     private ConverterEngine cnvEngine = null;
     
 
     /** TREE- & TABLE-MODELS */
     private ConfigurationTreeModel  treeModelCurrentConfig;
     private ConfigurationTreeModel  treeModelImportConfig;
     private ParameterTreeModel      treeModelParameters;
 
     /** GUI COMPONENTS */
     private JFrame        frame;
 
     private MenuBar       menuBar;
     private ToolBar       toolBar;
 
     private JPanel        jPanelContentPane         = new JPanel();
     private JMenuBar      jMenuBar                  = new JMenuBar();
     private JToolBar      jToolBar                  = new JToolBar();
     private JPanel        jPanelDbConnection        = new JPanel();
     private JSplitPane    jSplitPane                = new JSplitPane();
     private JSplitPane    jSplitPaneRight           = new JSplitPane();
     
     private JPanel        jPanelContentEditor       = new JPanel();
     
     private JPanel        jPanelLeft                = new JPanel();
     private JTextField    jTextFieldCurrentConfig   = new JTextField();
     private JLabel        jLabelLock                = new JLabel();
     private JTextField    jTextFieldProcess         = new JTextField();     // AL
     private JButton       jButtonRelease            = new JButton();        // AL
     private JTextField    jTextFieldCreated         = new JTextField();
     private JTextField    jTextFieldCreator         = new JTextField();
     private JTabbedPane   jTabbedPaneLeft           = new JTabbedPane();
 
     private JPanel        jPanelCurrentConfig       = new JPanel();
     private JLabel        jLabelSearch              = new JLabel();        // ML
     private JPopupMenu    jPopupMenuSearch          = new JPopupMenu();
     private ButtonGroup   buttonGroupSearch1;
     private ButtonGroup   buttonGroupSearch2;
     private JTextField    jTextFieldSearch          = new JTextField();    // KL
     private JButton       jButtonCancelSearch       = new JButton();       // AL
     private JToggleButton jToggleButtonImport       = new JToggleButton(); // AL
     private JSplitPane    jSplitPaneCurrentConfig   = new JSplitPane();
     private JScrollPane   jScrollPaneCurrentConfig  = new JScrollPane();
     private JTree         jTreeCurrentConfig;                              //TML+TSL
 
     private JPanel        jPanelImportConfig        = new JPanel();
     private JLabel        jLabelImportSearch        = new JLabel();        // ML
     private JPopupMenu    jPopupMenuImportSearch    = new JPopupMenu();
     private ButtonGroup   buttonGroupImportSearch1;
     private ButtonGroup   buttonGroupImportSearch2;
     private JTextField    jTextFieldImportSearch    = new JTextField();    // KL
     private JButton       jButtonImportCancelSearch = new JButton();       // AL
     private JScrollPane   jScrollPaneImportConfig   = new JScrollPane();
     private JTree         jTreeImportConfig;                               //TML+TSL
 
     private JPanel        jPanelRightUpper          = new JPanel();
     private JSplitPane    jSplitPaneRightUpper      = new JSplitPane();
     private JPanel        jPanelPlugin              = new JPanel();
     private JTextField    jTextFieldPackage         = new JTextField();
     private JTextField    jTextFieldCVS             = new JTextField();
     private JLabel        jLabelPlugin              = new JLabel();
     private JTextField    jTextFieldPlugin          = new JTextField();
     private JTextField    jTextFieldLabel           = new JTextField();
     private JComboBox     jComboBoxPaths            = new JComboBox();     // AL
     private JScrollPane   jScrollPaneParameters     = new JScrollPane();
     private TreeTable     jTreeTableParameters;
     
     private JPanel        jPanelRightLower          = new JPanel();
     private JTabbedPane   jTabbedPaneRightLower     = new JTabbedPane();
     private JScrollPane   jScrollPaneRightLower     = new JScrollPane();
     private JEditorPane   jEditorPaneSnippet        = new JEditorPane();
 
     private JComboBox     jComboBoxEventContent     = new JComboBox();
     private JList         jListStreams              = new JList();
     private JList         jListDatasets             = new JList();
     private JList         jListPaths                = new JList();
     private JComboBox     jComboBoxCommands         = new JComboBox();
     private JTextArea     jTextAreaOutputModule     = new JTextArea();
     private JTable        jTableCommands            = new JTable();
     
     private JProgressBar  jProgressBar              = new JProgressBar(); 
     
 
     //
     // construction
     //
     
     /** standard constructor */
     public ConfDbGUI(JFrame frame)
     {
 	this.userName = System.getProperty("user.name");
 	this.frame    = frame;
 	
 	this.database         = new ConfDB();
 	this.currentRelease   = new SoftwareRelease();
 	this.currentConfig    = new Configuration();
 	this.importRelease    = new SoftwareRelease();
 	this.importConfig     = new Configuration();
 	
 	//this.jTableCommands.setAutoCreateRowSorter(true);
 	
 	try {
 	    this.cnvEngine = ConverterFactory.getConverterEngine("python");
 	}
 	catch (Exception e) {
 	    System.err.println("failed to initialize converter engine: " +
 			       e.getMessage());
 	}
 	
 	createTreesAndTables();
 	createContentPane();
 	hideImportTree();
 	
 	frame.setContentPane(jPanelContentPane);
 	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     
 	jTextFieldProcess.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jButtonProcessActionPerformed(e);
 		}
 	    });
 	jButtonRelease.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jButtonReleaseActionPerformed(e);
 		}
 	    });
 	jLabelSearch.addMouseListener(new MouseAdapter() {
 		public void mousePressed(MouseEvent e)  { maybeShowPopup(e); }
 		public void mouseReleased(MouseEvent e) { maybeShowPopup(e); }
 		public void maybeShowPopup(MouseEvent e) {
 		    if (e.isPopupTrigger())
 			jPopupMenuSearch
 			    .show(e.getComponent(),e.getX(),e.getY());
 		}
 	    });
 	jTextFieldSearch.getDocument().addDocumentListener(new DocumentListener() {
 		public void insertUpdate(DocumentEvent e) {
 		    jTextFieldSearchInsertUpdate(e);
 		}
 		public void removeUpdate(DocumentEvent e) {
 		    jTextFieldSearchRemoveUpdate(e);
 		}
 		public void changedUpdate(DocumentEvent e) {}
 	    });
 	jButtonCancelSearch.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jButtonCancelSearchActionPerformed(e);
 		}
 	    });
 	jToggleButtonImport.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jToggleButtonImportActionPerformed(e);
 		}
 	    });
 	jTreeCurrentConfig
 	    .getModel().addTreeModelListener(new TreeModelListener() {
 		    public void treeNodesChanged(TreeModelEvent e) {
 			jTreeCurrentConfigTreeNodesChanged(e);
 		    }
 		    public void treeNodesInserted(TreeModelEvent e) {
 			jTreeCurrentConfigTreeNodesInserted(e);
 		    }
 		    public void treeNodesRemoved(TreeModelEvent e) {
 			jTreeCurrentConfigTreeNodesRemoved(e);
 		    }
 		    public void treeStructureChanged(TreeModelEvent e) {
 			jTreeCurrentConfigTreeStructureChanged(e);
 		    }
 		});
 	jTreeCurrentConfig.addTreeSelectionListener(new TreeSelectionListener() {
 		public void valueChanged(TreeSelectionEvent e) {
 			jTreeCurrentConfigValueChanged(e);
 		}
 	    });
 	
 	KeyStroke ks_F2  = KeyStroke.getKeyStroke("F2");
 	Object    key_F2 = jTreeCurrentConfig.getInputMap().get(ks_F2);
 	if (key_F2!=null) jTreeCurrentConfig.getInputMap().put(ks_F2,"none");
 
 	jLabelImportSearch.addMouseListener(new MouseAdapter() {
 		public void mousePressed(MouseEvent e)  { maybeShowPopup(e); }
 		public void mouseReleased(MouseEvent e) { maybeShowPopup(e); }
 		public void maybeShowPopup(MouseEvent e) {
 		    if (e.isPopupTrigger())
 			jPopupMenuImportSearch.show(e.getComponent(),
 						    e.getX(),e.getY());
 		}
 	    });
 	jTextFieldImportSearch
 	    .getDocument().addDocumentListener(new DocumentListener() {
 		    public void insertUpdate(DocumentEvent e) {
 			jTextFieldImportSearchInsertUpdate(e);
 		    }
 		    public void removeUpdate(DocumentEvent e) {
 			jTextFieldImportSearchRemoveUpdate(e);
 		    }
 		    public void changedUpdate(DocumentEvent e) {}
 		});
 	jButtonImportCancelSearch.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jButtonImportCancelSearchActionPerformed(e);
 		}
 	    });
 	jComboBoxPaths.addItemListener(new ItemListener() {
 		public void itemStateChanged(ItemEvent e) {
 		    jComboBoxPathsItemStateChanged(e);
 		}
 	    });
 	jTreeTableParameters.
 	    getTree().getModel().addTreeModelListener(new TreeModelListener() {
 		    public void treeNodesChanged(TreeModelEvent e) {
 			jTreeTableParametersTreeNodesChanged(e);
 		    }
 		    public void treeNodesInserted(TreeModelEvent e) {
 			jTreeTableParametersTreeNodesInserted(e);
 		    }
 		    public void treeNodesRemoved(TreeModelEvent e) {
 			jTreeTableParametersTreeNodesRemoved(e);
 		    }
 		    public void treeStructureChanged(TreeModelEvent e) {}
 		});
 	((BasicSplitPaneDivider)((BasicSplitPaneUI)jSplitPaneRight.
 				 getUI()).getDivider()).
 	    addComponentListener(new ComponentListener() {
 		    public void componentHidden(ComponentEvent e) {}
 		    public void componentMoved(ComponentEvent e) {
 			jSplitPaneRightComponentMoved(e);
 		    }
 		    public void componentResized(ComponentEvent e) {}
 		    public void componentShown(ComponentEvent e) {}
 		});
 	    
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
     // main
     //
     
     /** main method, thread-safe call to createAndShowGUI */
     public static void main(String[] args)
     {
 	javax.swing.SwingUtilities.invokeLater(new Runnable() {
 		public void run() { createAndShowGUI(); }
 	    });
     }
     
     /** create the GUI and show it */
     private static void createAndShowGUI()
     {
 	JFrame frame = new JFrame("ConfDbGUI");
 	ConfDbGUI gui = new ConfDbGUI(frame);
 	
 	int frameWidth =
 	    (int)(0.75*frame.getToolkit().getScreenSize().getWidth());
 	int frameHeight =
 	    (int)(0.75*frame.getToolkit().getScreenSize().getHeight());
 	int frameX =
 	    (int)(0.125*frame.getToolkit().getScreenSize().getWidth());
 	int frameY =
 	    (int)(0.10*frame.getToolkit().getScreenSize().getHeight());
 
 	frame.pack();
 	frame.setSize(frameWidth,frameHeight);
 	frame.setLocation(frameX,frameY);
 	frame.setVisible(true);
 	
 	gui.connectToDatabase();
     }
     
 
     //
     // member functions
     //
     
     /** get the main frame */
     public JFrame getFrame() { return frame; }
 
     /** show the 'about' dialog */
     public void showAboutDialog()
     {
 	AboutDialog dialog = new AboutDialog(frame);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
     }
     
     /** quit the GUI application */
     public void quitApplication()
     {
 	if (closeConfiguration()) {
 	    disconnectFromDatabase();
 	    System.exit(0);
 	}
     }
 
     /** create a new configuration */
     public void newConfiguration()
     {
 	if (!closeConfiguration()) return;
 	
 	NewConfigurationDialog dialog = new NewConfigurationDialog(frame,
 								   database);
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
 	    jProgressBar.setIndeterminate(true);
 	    jProgressBar.setVisible(true);
 	    jProgressBar.setString("Loading Templates for Release " +
 				  dialog.releaseTag() + " ... ");
 	    menuBar.configurationIsOpen();
 	    toolBar.configurationIsOpen();
 	}
     }
 
     /** parse a configuration from a *.py file */
     public void parseConfiguration()
     {
 	if (!closeConfiguration()) return;
 	
 	ParseConfigurationDialog dialog =
 	    new ParseConfigurationDialog(frame,database);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	if (dialog.validChoice()) {
 	    String fileName   = dialog.fileName();
 	    String releaseTag = dialog.releaseTag();
 	    
 	    ParseConfigurationThread worker =
 		new ParseConfigurationThread(fileName,releaseTag);
 	    worker.start();
 	    jProgressBar.setIndeterminate(true);
 	    jProgressBar.setVisible(true);
 	    jProgressBar.setString("Parsing '"+fileName+"' against Release " +
 				  releaseTag + " ... ");
 	    menuBar.configurationIsOpen();
 	    toolBar.configurationIsOpen();
 	}
     }
 
     /** open an existing configuration */
     public void openConfiguration()
     {
 	if (database.dbUrl().equals(new String())) return;
 	if (!closeConfiguration()) return;
 	
 	PickConfigurationDialog dialog =
 	    new PickConfigurationDialog(frame,"Open Configuration",database);
 	dialog.allowUnlocking();
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	if (dialog.validChoice()) {
 	    OpenConfigurationThread worker =
 		new OpenConfigurationThread(dialog.configInfo());
 	    worker.start();
 	    jProgressBar.setIndeterminate(true);
 	    jProgressBar.setVisible(true);
 	    jProgressBar.setString("Loading Configuration ...");
 	    menuBar.configurationIsOpen();
 	    toolBar.configurationIsOpen();
 	}
     }
 
     /** close the current configuration */
     public boolean closeConfiguration()
     {
 	return closeConfiguration(true);
     } 
 
     /** close the current configuration */
     public boolean closeConfiguration(boolean showDialog)
     {
 	if (currentConfig.isEmpty()) return true;
 	
 	if (currentConfig.hasChanged()&&showDialog) {
 	    Object[] options = { "OK", "CANCEL" };
 	    int answer = 
 		JOptionPane.showOptionDialog(null,
 					     "The current configuration "+
 					     "contains unsaved changes, "+
 					     "really close?","Warning",
 					     JOptionPane.DEFAULT_OPTION,
 					     JOptionPane.WARNING_MESSAGE,
 					     null, options, options[1]);
 	    if (answer==1) return false;
 	}
 	
 	if (!currentConfig.isLocked()&&currentConfig.version()>0) {
 	    try { database.unlockConfiguration(currentConfig); }
 	    catch (DatabaseException e) { System.err.println(e.getMessage()); }
 	}
 
 	resetConfiguration();
 	
 	return true;
     } 
     
     /** save a new version of the current configuration */
     public void saveConfiguration(boolean askForComment)
     {
 	if (currentConfig.isEmpty()||!currentConfig.hasChanged()||
 	    currentConfig.isLocked()||!checkConfiguration()) return;	
 	
 	if (currentConfig.version()==0) {
 	    saveAsConfiguration();
 	    return;
 	}
 	else {
 	    try { database.unlockConfiguration(currentConfig); }
 	    catch (DatabaseException e) { System.err.println(e.getMessage()); }
 	}
 	
 	String processName = jTextFieldProcess.getText();
 	String comment = "";
 
 	if (askForComment) {
 	    String fullConfigName =
 		currentConfig.parentDir().name()+"/"+currentConfig.name();
 	    comment = (String)JOptionPane
 		.showInputDialog(frame,"Enter comment for the new version of "+
 				 fullConfigName+":","Enter comment",
 				 JOptionPane.PLAIN_MESSAGE,
 				 null,null,"");
 	    if (comment==null) {
 		try { database.lockConfiguration(currentConfig,userName); }
 		catch (DatabaseException e) {
 		    System.err.println(e.getMessage());
 		}
 		return;
 	    }
 	}
 	
 	SaveConfigurationThread worker =
 	    new SaveConfigurationThread(processName,comment);
 	worker.start();
 	jProgressBar.setIndeterminate(true);
 	jProgressBar.setString("Save Configuration ...");
 	jProgressBar.setVisible(true);
 	
     }
     
     /** save the current configuration under a new name */
     public void saveAsConfiguration()
     {
 	if (!checkConfiguration()) return;
 	
 	boolean isLocked = currentConfig.isLocked();
 	if (currentConfig.version()!=0&&!isLocked) {
 	    try { database.unlockConfiguration(currentConfig); }
 	    catch (DatabaseException e) { System.err.println(e.getMessage()); }
 	}
 		
 	String processName = jTextFieldProcess.getText();
 	String comment = (currentConfig.version()==0) ?
 	    "first import" :
 	    "saveAs "+currentConfig+" ["+currentConfig.dbId()+"]";
 	
 	
 	SaveConfigurationDialog dialog =
 	    new SaveConfigurationDialog(frame,database,currentConfig,comment);
 	
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	if (dialog.validChoice()) {
 	    SaveConfigurationThread worker =
 		new SaveConfigurationThread(processName,dialog.comment());
 	    worker.start();
 	    
 	    jProgressBar.setIndeterminate(true);
 	    jProgressBar.setString("Save Configuration ...");
 	    jProgressBar.setVisible(true);
 	    currentConfig.setHasChanged(false);
 	    
 	}
 	else if (currentConfig.version()!=0&&!isLocked) {
 	    try { database.lockConfiguration(currentConfig,userName); }
 	    catch (DatabaseException e) { System.err.println(e.getMessage()); }
 	}
     }
     
     /** compare current configuration to another one */
     public void diffConfigurations()
     {
 	DiffDialog dialog = new DiffDialog(frame,database);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	if (!currentConfig.isEmpty()) {
 	    dialog.setNewConfig(currentConfig);
 	    dialog.setOldConfigs(currentConfig.configInfo());
 	}
 	dialog.setVisible(true);
     }
     
     /** open prescale editor */
     public void openPrescaleEditor()
     {
 	PrescaleDialog dialog = new PrescaleDialog(frame,currentConfig);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	ServiceInstance prescaleSvc = currentConfig.service("PrescaleService");
 	if (prescaleSvc!=null)
 	    treeModelCurrentConfig.nodeStructureChanged(prescaleSvc);
     }
 
     /** open prescale editor */
     public void openSmartPrescaleEditor()
     {
       
 	SmartPrescaleDialog dialog = new SmartPrescaleDialog(frame,currentConfig);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	ServiceInstance smartPrescaleSvc = currentConfig.service("SmartPrescaleService");
 	if (smartPrescaleSvc!=null)
 	    treeModelCurrentConfig.nodeStructureChanged(smartPrescaleSvc);
     }
 
 
             
     /** open message logger */
     public void openMessageLoggerEditor()
     {
 
 	ServiceInstance messageLoggerSvc=currentConfig.service("MessageLogger");
 	if (messageLoggerSvc==null) return;
 	
  	MessageLoggerDialog dialog = new MessageLoggerDialog(frame,
 							     currentConfig);
  	dialog.pack();
  	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
  	if (messageLoggerSvc!=null) {
      	    treeModelCurrentConfig.nodeStructureChanged(messageLoggerSvc);   
 	}
     }
 
  	
     /** add untracked parameter to the currently active component */
     public void addUntrackedParameter()
     {
  	AddParameterDialog dlg = new AddParameterDialog(frame);
  	dlg.pack();
  	dlg.setLocationRelativeTo(frame);
  	dlg.setVisible(true);
  	if (dlg.validChoice()) {
  	    if (currentParameterContainer instanceof ParameterContainer) {
  		ParameterContainer container =
 		    (ParameterContainer)currentParameterContainer;
  		Parameter p = container.parameter(dlg.name());
  		if(p!=null) {
  		    //JOptionPane.showMessageDialog(null,
 		    //"Parameter already exists",JOptionPane.ERROR_MESSAGE); 
  		    return;
  		}
  		if(dlg.valueAsString()==null)
  		    container.updateParameter(dlg.name(),dlg.type(),
 					      dlg.valueAsString());
  		else
  		    container.updateParameter(dlg.name(),dlg.type(),"");
  		displayParameters();
  	    }	
  	}
     }
     
 
     /** one another configuration to import components */
     public void importConfiguration()
     {
 	PickConfigurationDialog dialog =
 	    new PickConfigurationDialog(frame,"Import Configuration",database);
 	dialog.fixReleaseTag(currentRelease.releaseTag());
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	if (dialog.validChoice()&&
 	    dialog.configInfo().releaseTag().equals(currentRelease.releaseTag())) {
 	    ImportConfigurationThread worker =
 		new ImportConfigurationThread(dialog.configInfo());
 	    worker.start();
 	    jProgressBar.setIndeterminate(true);
 	    jProgressBar.setVisible(true);
 	    jProgressBar.setString("Importing Configuration ...");
 	}	
     }
     
     /** migrate the current configuration to a new release */
     public void migrateConfiguration()
     {
 	if (!checkConfiguration()) return;
 	
 	MigrateConfigurationDialog dialog =
 	    new MigrateConfigurationDialog(frame,database);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	String releaseTag = dialog.releaseTag();	
 	
 	if (releaseTag.length()>0) {
 	    MigrateConfigurationThread worker =
 		new MigrateConfigurationThread(releaseTag);
 	    worker.start();
 	    jProgressBar.setIndeterminate(true);
 	    jProgressBar.setVisible(true);
 	    jProgressBar.setString("Migrate configuration to release '" +
 				  releaseTag + "' ... ");
 	}
     }
     
     /** convert the current configuration to a text file (ascii,python,html) */
     public void convertConfiguration()
     {
 	if (!checkConfiguration()) return;
 	
 	ConvertConfigurationDialog dialog =
 	    new ConvertConfigurationDialog(frame,currentConfig);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	if (!dialog.isCanceled()) {
 	    ConvertConfigurationThread worker = 
 		new ConvertConfigurationThread(dialog.configToConvert(),
 					       dialog.fileName(),
 					       dialog.format(),
 					       dialog.asFragment());
 	    worker.start();
 	    jProgressBar.setIndeterminate(true);
 	    jProgressBar.setVisible(true);
 	    jProgressBar.setString("Convert configuration '"+
 				   currentConfig.name()+"' ... ");
 	}
     }
     
     /** search/replace parameters in the current configuration */
     public void searchAndReplace()
     {
 	if (currentConfig.isEmpty()) return;
 	SearchAndReplaceDialog dlg = new SearchAndReplaceDialog(frame,
 								currentConfig);
 	dlg.pack();
 	dlg.setLocationRelativeTo(frame);
 	dlg.setVisible(true);
     }
 
     /** set option 'Track InputTags' */
     public void setOptionTrackInputTags(boolean doTrack)
     {
 	ConfigurationTreeRenderer renderer =
 	    (ConfigurationTreeRenderer)jTreeCurrentConfig.getCellRenderer();
 	renderer.displayUnresolvedInputTags(doTrack);
 	
 	IConfiguration config=(IConfiguration)treeModelCurrentConfig.getRoot();
 	int pathIndices[] = new int[config.pathCount()];
 	for (int i=0;i<config.pathCount();i++) pathIndices[i]=i;
 	treeModelCurrentConfig.childNodesChanged(treeModelCurrentConfig
 						 .pathsNode(),pathIndices);
     }
 
     /** connect to the database */
     public void connectToDatabase()
     {
 	disconnectFromDatabase();
 	
 	DatabaseConnectionDialog dbDialog = new DatabaseConnectionDialog(frame);
 	dbDialog.pack();
 	dbDialog.setLocationRelativeTo(frame);
 	dbDialog.setVisible(true);
 	
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
 	    ((DatabaseInfoPanel)jPanelDbConnection).connectedToDatabase(dbType,
 									dbHost,
 									dbPort,
 									dbName,
 									dbUser);
 	}
 	catch (DatabaseException e) {
 	    String msg = "Failed to connect to DB: " + e.getMessage();
 	    JOptionPane.showMessageDialog(frame,msg,"",
 					  JOptionPane.ERROR_MESSAGE);
 	}
 	menuBar.dbConnectionIsEstablished();
 	toolBar.dbConnectionIsEstablished();
     }
 
     /** disconnect from the  database */
     public void disconnectFromDatabase()
     {
 	if (!closeConfiguration()) return;
 	
 	try {
 	    database.disconnect();
 	    ((DatabaseInfoPanel)jPanelDbConnection).disconnectedFromDatabase();
 	    currentRelease.clear("");
 	}
 	catch (DatabaseException e) {
 	    String msg = "Failed to disconnect from DB: " + e.getMessage();
 	    JOptionPane.showMessageDialog(frame,msg,"",
 					  JOptionPane.ERROR_MESSAGE);
 	}
 	catch (Exception e) {
 	    System.err.println("ERROR in disconnectFromDB(): "+e.getMessage());
 	}
 	menuBar.dbConnectionIsNotEstablished();
 	toolBar.dbConnectionIsNotEstablished();
     }
 
     /** export the current configuration to a new database */
     public void exportConfiguration()
     {
 	if (!checkConfiguration()) return;
 	
 	ExportConfigurationDialog dialog =
 	    new ExportConfigurationDialog(frame,
 					  currentConfig.releaseTag(),
 					  currentConfig.name());
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	if (dialog.validChoice()) {
 	    ConfDB      targetDB   = dialog.targetDB();
 	    String      targetName = dialog.targetName();
 	    Directory   targetDir  = dialog.targetDir();
 	    
 	    ExportConfigurationThread worker =
 		new ExportConfigurationThread(targetDB,targetName,targetDir);
 	    worker.start();
 	    jProgressBar.setIndeterminate(true);
 	    jProgressBar.setVisible(true);
 	    jProgressBar.setString("Migrate Configuration to " +
 				   targetDB.dbUrl() + " ... ");
 	}
     }
     
     
     /** export the current configuration to a new database */
     public void importConfigurationFromDBV1()
     {
 
 	if (database.dbUrl().equals(new String())) return;
 	if (!closeConfiguration()) return;
        
 	ConfOldDB databaseOld = new ConfOldDB();
 	
 	System.out.println();
 
 
 	DatabaseConnectionDialog dbDialog = new DatabaseConnectionDialog(frame,"/conf/confdbv0.properties");
 	dbDialog.pack();
 	dbDialog.setLocationRelativeTo(frame);
 	dbDialog.setVisible(true);
 	
 	if (!dbDialog.validChoice()) return;
 	String dbType = dbDialog.getDbType();
 	String dbHost = dbDialog.getDbHost();
 	String dbPort = dbDialog.getDbPort();
 	String dbName = dbDialog.getDbName();
 	String dbUrl  = dbDialog.getDbUrl();
 	String dbUser = dbDialog.getDbUser();
 	String dbPwrd = dbDialog.getDbPassword();
 	
 	try {
 	    databaseOld.connect(dbType,dbUrl,dbUser,dbPwrd);
 	    //  ((DatabaseInfoPanel)jPanelDbConnection).connectedToDatabase(dbType,
 	    //		dbHost,	dbPort,dbName,dbUser);
 	}
 	catch (DatabaseException e) {
 	    String msg = "Failed to connect to DB: " + e.getMessage();
 	    JOptionPane.showMessageDialog(frame,msg,"",
 					  JOptionPane.ERROR_MESSAGE);
 	}
 
 	PickOldConfigurationDialog dialog =
 	    new PickOldConfigurationDialog(frame,"Open Configuration from old Schema",databaseOld);
 
 	dialog.allowUnlocking();
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 
 
 	if (dialog.validChoice()) {
 	    OpenOldConfigurationThread worker =
 		new OpenOldConfigurationThread(dialog.configInfo(),databaseOld,database);
 	    worker.start();
 	    jProgressBar.setIndeterminate(true);
 	    jProgressBar.setVisible(true);
 	    jProgressBar.setString("Loading Configuration ...");
 	    menuBar.configurationIsOpen();
 	    toolBar.configurationIsOpen();
 	 
 	}
 
     }
 
     /** reset current and import configuration */
     private void resetConfiguration()
     {
 	currentRelease.clearInstances();
 	
 	currentConfig.reset();
 	treeModelCurrentConfig.setConfiguration(currentConfig);
 	jTextFieldCurrentConfig.setText("");
 	jTextFieldCurrentConfig.setToolTipText("");
 	jLabelLock.setIcon(null);
 	jTextFieldProcess.setText("");
 	jButtonRelease.setText("");
 	jTextFieldCreated.setText("");
 	jTextFieldCreator.setText("");
 	
 	jTextFieldSearch.setText("");
 	jTextFieldImportSearch.setText("");
 	jButtonCancelSearch.setEnabled(false);
 	jButtonImportCancelSearch.setEnabled(false);
 
 	clearParameters();
 	clearSnippet();
 	
 	menuBar.configurationIsNotOpen();
 	toolBar.configurationIsNotOpen();
 
 	importConfig.reset();
 	treeModelImportConfig.setConfiguration(importConfig);
 	hideImportTree();
 	
 	jTextFieldProcess.setEditable(false);
 	jToggleButtonImport.setEnabled(false);
 
 	jSplitPane.setRightComponent(jSplitPaneRight);
     }
 
     /** check if current configuration is in a valid state for save/convert */
     private boolean checkConfiguration()
     {
 	if (currentConfig.isEmpty()) return false;
 	
 	int unsetParamCount = currentConfig.unsetTrackedParameterCount();
 	if (unsetParamCount>0) {
 	    String msg =
 		"current configuration contains " + unsetParamCount +
 		" unset tracked parameters. They *should* be set before " +
 		"saving/converting!";
 	    JOptionPane.showMessageDialog(frame,msg,"",
 					  JOptionPane.WARNING_MESSAGE);
 	}
 	
 	int emptyContainerCount = currentConfig.emptyContainerCount();
 	if (emptyContainerCount>0) {
 	    String msg =
 		"current configuration contains " + emptyContainerCount +
 		" empty containers (paths/sequences). "+
 		"They must be filled before saving/converting!";
 	    JOptionPane.showMessageDialog(frame,msg,"",
 					  JOptionPane.ERROR_MESSAGE);
 	    return false;
 	}
 	
 	return true;
     }
     
 
     /** set the current configuration */
     private void setCurrentConfig(Configuration config)
     {
 	TreePath tp = jTreeCurrentConfig.getSelectionPath();
 	currentConfig = config;
 	treeModelCurrentConfig.setConfiguration(currentConfig);
 	
 	currentRelease = currentConfig.release();
 	jTreeCurrentConfig.scrollPathToVisible(tp);
 	jTreeCurrentConfig.setSelectionPath(tp);
 
 	jTextFieldCurrentConfig.setText(currentConfig.toString());
 	if (currentConfig.version()>0)
 	    jTextFieldCurrentConfig.setToolTipText("id:"+
 						   currentConfig.dbId()+
 						   "  comment:"+
 						   currentConfig.comment());
 	
 	if (currentConfig.isLocked()) {
 	    jLabelLock.setIcon(new ImageIcon(getClass().
 					     getResource("/LockedIcon.png")));
 	    jLabelLock.setToolTipText("locked by user " +
 				      currentConfig.lockedByUser());
 	}
 	else {
 	    jLabelLock.setIcon(new ImageIcon(getClass().
 					     getResource("/UnlockedIcon.png")));
 	    jLabelLock.setToolTipText("It's all yours, nobody else can "+
 				      "modify this configuration until closed!");
 	}
 	
 	jTextFieldProcess.setText(currentConfig.processName());
 	jButtonRelease.setText(currentRelease.releaseTag());
 	jTextFieldCreated.setText(currentConfig.created());
 	jTextFieldCreator.setText(currentConfig.creator());
 	
 	jTextFieldProcess.setEditable(true);
     }
     
     
     //
     // THREADS
     //
     
     /** migrate current configuration to another database  */
     private class ExportConfigurationThread extends SwingWorker<String>
     {
 	/** member data */
 	private ConfDB           targetDB   = null;
 	private String           targetName = null;
 	private Directory        targetDir  = null;
 	private DatabaseMigrator migrator   = null;
 	private long             startTime;
 	
 	/** standard constructor */
 	public ExportConfigurationThread(ConfDB targetDB,
 					 String targetName,Directory targetDir)
 	{
 	    this.targetDB   = targetDB;
 	    this.targetName = targetName;
 	    this.targetDir  = targetDir;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws DatabaseException, MigratorException
 	{
 	    startTime = System.currentTimeMillis();
 	    migrator = new DatabaseMigrator(currentConfig,database,targetDB);
 	    migrator.migrate(targetName,targetDir);
 	    targetDB.disconnect();
 	    return new String("Done!");
 	}
 	
 	/** SwingWorker: finished */
 	protected void finished()
 	{
 	    try {
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		jProgressBar.setString(jProgressBar.getString()+get()+" ("+
 				       elapsedTime+" ms)");
 		DiffDialog dialog =  new DiffDialog(frame,database);
 		dialog.setConfigurations(migrator.sourceConfig(),
 					 migrator.targetConfig());
 		dialog.setTitle("Configuration Export Report");
 		dialog.pack();
 		dialog.setLocationRelativeTo(frame);
 		jProgressBar.setIndeterminate(false);
 		dialog.setVisible(true);
 	    }
 	    catch (ExecutionException e) {
 		String errMsg =
 		    "Export Configuration FAILED:\n"+e.getCause().getMessage();
 		JOptionPane.showMessageDialog(frame,errMsg,
 					      "Export Configuration failed",
 					      JOptionPane.ERROR_MESSAGE,null);
 		jProgressBar.setString(jProgressBar.getString()+"FAILED!");
 		jProgressBar.setIndeterminate(false);
 	    }
 	    catch (Exception e) {
 		e.printStackTrace();
 		jProgressBar.setString(jProgressBar.getString() + "FAILED!");	
 		jProgressBar.setIndeterminate(false);
 	    }
 
 	}
     }
     
     
     /** load release templates from the database */
     private class NewConfigurationThread extends SwingWorker<String>
     {
 	/** member data */
 	private String name       = null;
 	private String process    = null;
 	private String releaseTag = null;
 	private long   startTime;
 	
 	/** standard constructor */
 	public NewConfigurationThread(String name,String process,
 				      String releaseTag)
 	{
 	    this.name       = name;
 	    this.process    = process;
 	    this.releaseTag = releaseTag;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws DatabaseException
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
 		Configuration config = new Configuration();
 		config.initialize(new ConfigInfo(name,null,releaseTag),
 				  currentRelease);
 		setCurrentConfig(config);
 		jTextFieldProcess.setText(process);
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		jProgressBar.setString(jProgressBar.getString()+get()+
 				       " ("+elapsedTime+" ms)");
 	    }
 	    catch (ExecutionException e) {
 		String errMsg =
 		    "New Configuration FAILED:\n"+e.getCause().getMessage();
 		JOptionPane.showMessageDialog(frame,errMsg,
 					      "New Configuration failed",
 					      JOptionPane.ERROR_MESSAGE,null);
 		jProgressBar.setString(jProgressBar.getString()+"FAILED!");
 	    } 
 	    catch (Exception e) {
 		e.printStackTrace();
 		jProgressBar.setString(jProgressBar.getString()+"FAILED!");	
 	    }
 	    jProgressBar.setIndeterminate(false);
 	    jTreeCurrentConfig.setEditable(true);
 	    jTreeTableParameters.getTree().setEditable(true);
 	}
     }
     
 
     /** load release templates from the database and parse config from *.py */
     private class ParseConfigurationThread extends SwingWorker<String>
     {
 	/** member data */
 	private PythonParser parser     = null;
 	private String       fileName   = null;
 	private String       releaseTag = null;
 	private long         startTime;
 	
 	/** standard constructor */
 	public ParseConfigurationThread(String fileName,String releaseTag)
 	{
 	    this.fileName   = fileName;
 	    this.releaseTag = releaseTag;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws DatabaseException,ParserException
 	{
 	    startTime = System.currentTimeMillis();
 	    if (!releaseTag.equals(currentRelease.releaseTag()))
 		database.loadSoftwareRelease(releaseTag,currentRelease);
 	    parser = new PythonParser(currentRelease);
 	    parser.parseFile(fileName);
 	    setCurrentConfig(parser.createConfiguration());
 	    return new String("Done!");
 	}
 	
 	/** SwingWorker: finished */
 	protected void finished()
 	{
 	    try {
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		jProgressBar.setString(jProgressBar.getString()+get()+
 				       " ("+elapsedTime+" ms)");
 	    }
 	    catch (ExecutionException e) {
 		String errMsg =
 		    "Parse Configuration FAILED:\n"+e.getCause().getMessage();
 		JOptionPane.showMessageDialog(frame,errMsg,
 					      "Parse Configuration failed",
 					      JOptionPane.ERROR_MESSAGE,null);
 		jProgressBar.setString(jProgressBar.getString()+"FAILED!");
 	    } 
 	    catch (Exception e) {
 		e.printStackTrace();
 		jProgressBar.setString(jProgressBar.getString()+"FAILED!");	
 	    }
 	    jProgressBar.setIndeterminate(false);
 	    jTreeCurrentConfig.setEditable(true);
 	    jTreeTableParameters.getTree().setEditable(true);
 
 	    if (parser.closeProblemStream()) {
 		System.err.println("problems encountered, see problems.txt.");
 		ParserProblemsDialog dialog=new ParserProblemsDialog(frame,
 								     parser);
 		dialog.pack();
 		dialog.setLocationRelativeTo(frame);
 		dialog.setVisible(true);
 	    }
 	    
 	}
     }
     
 
     /** load a configuration from the database  */
     private class OpenConfigurationThread extends SwingWorker<String>
     {
 	/** member data */
 	private ConfigInfo configInfo = null;
 	private long       startTime;
 	
 	/** standard constructor */
 	public OpenConfigurationThread(ConfigInfo configInfo)
 	{
 	    this.configInfo = configInfo;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws DatabaseException
 	{
 	    startTime = System.currentTimeMillis();
	    
	    if(currentRelease == null) currentRelease = new SoftwareRelease();
	    
 	    Configuration config = database.loadConfiguration(configInfo,currentRelease);
 	    setCurrentConfig(config);
 
 	    return new String("Done!");
 	}
 	
 	/** SwingWorker: finished */
 	protected void finished()
 	{
 	    try {
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		jProgressBar.setString(jProgressBar.getString() +
 				       get() + " (" + elapsedTime + " ms)");
 	    }
 	    catch (ExecutionException e) {
 		String errMsg =
 		    "Open Configuration FAILED:\n"+e.getCause().getMessage();
 		JOptionPane.showMessageDialog(frame,errMsg,
 					      "Open Configuration failed",
 					      JOptionPane.ERROR_MESSAGE,null);
 		jProgressBar.setString(jProgressBar.getString()+"FAILED!");
 		e.printStackTrace();
 	    } 
 	    catch (Exception e) {
 		e.printStackTrace();
 		jProgressBar.setString(jProgressBar.getString()+"FAILED!");
 	    }
 	    jProgressBar.setIndeterminate(false);
 
 	    if (currentConfig.isLocked()) {
 		jTreeCurrentConfig.setEditable(false);
 		jTreeTableParameters.getTree().setEditable(false);
 		String msg =
 		    "The configuration '"+currentConfig.toString()+
 		    " is locked by user '"+currentConfig.lockedByUser()+"'!\n"+
 		    "You can't manipulate it until it is released.";
 		JOptionPane.showMessageDialog(frame,msg,"READ ONLY!",
 					      JOptionPane.WARNING_MESSAGE,
 					      null);
 	    }
 	    else {
 		jTreeCurrentConfig.setEditable(true);
 		jTreeTableParameters.getTree().setEditable(true);
 		try {
 		    database.lockConfiguration(currentConfig,userName);
 		}
 		catch (DatabaseException e) {
 		    JOptionPane.showMessageDialog(frame,e.getMessage(),
 						  "Failed to lock configuration",
 						  JOptionPane.ERROR_MESSAGE,null);
 		}
 	    }
 	}
     }
 
 
     /** load a configuration from the old database  version*/
     private class OpenOldConfigurationThread extends SwingWorker<String>
     {
 	/** member data */
 	private ConfigInfo configInfo = null;
 	private long       startTime;
 	
 	private ConfOldDB databaseOld;
 	private ConfDB    database;
 
 	/** standard constructor */
 	public OpenOldConfigurationThread(ConfigInfo configInfo,ConfOldDB databaseOld,ConfDB database)
 	{
 	    this.configInfo = configInfo;
 	    this.databaseOld = databaseOld;
 	    this.database = database;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws DatabaseException
 	{
 	    startTime = System.currentTimeMillis();
 	    
  
 	    currentRelease = new SoftwareRelease();
 	    Configuration configOld = databaseOld.loadConfiguration(configInfo,currentRelease);
 	    databaseOld.disconnect();
 
       
 	    Configuration config = new Configuration();
 	    database.insertRelease(configInfo.releaseTag(),currentRelease);
 	    database.loadSoftwareRelease(configInfo.releaseTag(),currentRelease);
 	  
   
 	    config.initialize(new ConfigInfo(configInfo.name(),null,configInfo.releaseTag()),currentRelease);	    
 	    ReleaseMigrator releaseMigrator = new ReleaseMigrator(configOld,config);
 	    releaseMigrator.migrate();
 	   
 	    setCurrentConfig(config);
 	    
 	    jTextFieldProcess.setText(configOld.processName());
 
 	    return new String("Done!");	    
 	}
 	
 	/** SwingWorker: finished */
 	protected void finished()
 	{
 	    try {
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		jProgressBar.setString(jProgressBar.getString() +
 				       get() + " (" + elapsedTime + " ms)");
 	    }
 	    catch (ExecutionException e) {
 		String errMsg =
 		    "Open Configuration FAILED:\n"+e.getCause().getMessage();
 		JOptionPane.showMessageDialog(frame,errMsg,
 					      "Open Configuration failed",
 					      JOptionPane.ERROR_MESSAGE,null);
 		jProgressBar.setString(jProgressBar.getString()+"FAILED!");
 		e.printStackTrace();
 	    } 
 	    catch (Exception e) {
 		e.printStackTrace();
 		jProgressBar.setString(jProgressBar.getString()+"FAILED!");
 	    }
 	    jProgressBar.setIndeterminate(false);
 
 	    /*	    if (currentConfig.isLocked()) {
 		jTreeCurrentConfig.setEditable(false);
 		jTreeTableParameters.getTree().setEditable(false);
 		String msg =
 		    "The configuration '"+currentConfig.toString()+
 		    " is locked by user '"+currentConfig.lockedByUser()+"'!\n"+
 		    "You can't manipulate it until it is released.";
 		JOptionPane.showMessageDialog(frame,msg,"READ ONLY!",
 					      JOptionPane.WARNING_MESSAGE,
 					      null);
 	    }
 	    else {
 		jTreeCurrentConfig.setEditable(true);
 		jTreeTableParameters.getTree().setEditable(true);
 		try {
 		    database.lockConfiguration(currentConfig,userName);
 		}
 		catch (DatabaseException e) {
 		    JOptionPane.showMessageDialog(frame,e.getMessage(),
 						  "Failed to lock configuration",
 						  JOptionPane.ERROR_MESSAGE,null);
 		}
 		}*/
 	}
     }
 
     
     /** import a configuration from the database */
     private class ImportConfigurationThread extends SwingWorker<String>
     {
 	/** member data */
 	private ConfigInfo configInfo = null;
 	private long       startTime;
 	
 	/** standard constructor */
 	public ImportConfigurationThread(ConfigInfo configInfo)
 	{
 	    this.configInfo = configInfo;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws DatabaseException
 	{
 	    startTime = System.currentTimeMillis();
 	    if (importRelease.releaseTag()!=currentRelease.releaseTag())
 		importRelease = new SoftwareRelease(currentRelease);
 	    importConfig = database.loadConfiguration(configInfo,importRelease);
 	    return new String("Done!");
 	}
 	
 	/** SwingWorker: finished */
 	protected void finished()
 	{
 	    try {
 		treeModelImportConfig.setConfiguration(importConfig);
 		showImportTree();
 		jToggleButtonImport.setEnabled(true);
 		jToggleButtonImport.setSelected(true);
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		jProgressBar.setString(jProgressBar.getString()+get()+
 				       " ("+elapsedTime+" ms)");
 	    }
 	    catch (ExecutionException e) {
 		String errMsg =
 		    "Import Configuration FAILED:\n"+e.getCause().getMessage();
 		JOptionPane.showMessageDialog(frame,errMsg,
 					      "Import Configuration failed",
 					      JOptionPane.ERROR_MESSAGE,null);
 		jProgressBar.setString(jProgressBar.getString()+"FAILED!");
 	    } 
 	    catch (Exception e) {
 		e.printStackTrace();
 		jProgressBar.setString(jProgressBar.getString()+"FAILED!");
 	    }
 	    jProgressBar.setIndeterminate(false);
 	}
     }
     
     /** save a configuration in the database */
     private class SaveConfigurationThread extends SwingWorker<String>
     {
 	/** member data */
 	private long   startTime;
 	private String processName;
 	private String comment;
 	
 	/** standard constructor */
 	public SaveConfigurationThread(String processName,String comment)
 	{
 	    this.processName = processName;
 	    this.comment     = comment;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws DatabaseException
 	{
 	    startTime = System.currentTimeMillis();
 	    
 	    database.insertConfiguration(currentConfig,
 					 userName,processName,comment);
 	    if (!currentConfig.isLocked())
 		database.lockConfiguration(currentConfig,userName);
 	    
 	    return new String("Done!");
 	}
 	
 	/** SwingWorker: finished */
 	protected void finished()
 	{
 	    try {
 		setCurrentConfig(currentConfig);
 		currentConfig.setHasChanged(false);
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		jProgressBar.setString(jProgressBar.getString()+get()+
 				       " ("+elapsedTime+" ms)");
 	    }
 	    catch (ExecutionException e) {
 		String errMsg =
 		    "Save Configuration FAILED:\n"+e.getCause().getMessage();
 		JOptionPane.showMessageDialog(frame,errMsg,
 					      "Save Configuration failed",
 					      JOptionPane.ERROR_MESSAGE,null);
 		jProgressBar.setString(jProgressBar.getString()+"FAILED!");
 	    } 
 	    catch (Exception e) {
 		e.printStackTrace();
 		jProgressBar.setString(jProgressBar.getString()+"FAILED!");
 	    }
 	    jProgressBar.setIndeterminate(false);
 	}
     }
     
     /** migrate a configuration in the database to a new release */
     private class MigrateConfigurationThread extends SwingWorker<String>
     {
 	/** member data */
 	private Configuration   targetConfig     = null;
 	private String          targetReleaseTag = null;
 	private ReleaseMigrator migrator         = null;
 	private long            startTime;
 	
 	/** standard constructor */
 	public MigrateConfigurationThread(String targetReleaseTag)
 	{
 	    this.targetReleaseTag = targetReleaseTag;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws DatabaseException
 	{
 	    startTime = System.currentTimeMillis();
 	    
 	    SoftwareRelease targetRelease = new SoftwareRelease();
 	    database.loadSoftwareRelease(targetReleaseTag,targetRelease);
 	    
 	    String targetProcessName = currentConfig.processName();
 
 	    ConfigInfo targetConfigInfo =
 		new ConfigInfo(currentConfig.name(),currentConfig.parentDir(),
 			       -1,currentConfig.version(),"",userName,
 			       targetReleaseTag,targetProcessName,
 			       "migrated from "+currentRelease.releaseTag());
 
 	    targetConfig = new Configuration(targetConfigInfo,targetRelease);
 	    
 	    migrator = new ReleaseMigrator(currentConfig,targetConfig);
 	    migrator.migrate();
 
 	    return new String("Done!");
 	}
 	
 	/** SwingWorker: finished */
 	protected void finished()
 	{
 	    try {
 		clearParameters();
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		jProgressBar.setString(jProgressBar.getString()+get()+
 				       " ("+elapsedTime+" ms)");
 		DiffDialog dialog = new DiffDialog(frame,database);
 		dialog.setTitle("Release-Migration Report");
 		dialog.pack();
 		dialog.setLocationRelativeTo(frame);
 		dialog.setConfigurations(currentConfig,targetConfig);
 		setCurrentConfig(targetConfig);
 		jProgressBar.setIndeterminate(false);
 		dialog.setVisible(true);
 	    }
 	    catch (ExecutionException e) {
 		String errMsg =
 		    "Migrate Configuration FAILED:\n"+e.getCause().getMessage();
 		JOptionPane.showMessageDialog(frame,errMsg,
 					      "Migrate Configuration failed",
 					      JOptionPane.ERROR_MESSAGE,null);
 		jProgressBar.setString(jProgressBar.getString()+"FAILED!");
 		jProgressBar.setIndeterminate(false);
 	    } 
 	    catch (Exception e) {
 		e.printStackTrace();
 		jProgressBar.setString(jProgressBar.getString()+"FAILED!");
 		jProgressBar.setIndeterminate(false);
 	    }
 	}
     }
 
     /** convert configuration to a text file */
     private class ConvertConfigurationThread extends SwingWorker<String>
     {
 	/** member data */
 	private IConfiguration config     = null;
 	private String         fileName   = null;
 	private String         format     = null;
 	private boolean        asFragment = false;
 	private long           startTime;
 	
 	/** standard constructor */
 	public ConvertConfigurationThread(IConfiguration config,
 					  String  fileName,
 					  String  format,
 					  boolean asFragment)
 	{
 	    this.config     = config;
 	    this.fileName   = fileName;
 	    this.format     = format;
 	    this.asFragment = asFragment;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws ConverterException,IOException
 	{
 	    startTime = System.currentTimeMillis();
 	    String configAsString = "";
 	    OfflineConverter cnv = new OfflineConverter(format);
 	    configAsString = cnv.getConfigString(config,null,asFragment);
 	    if (configAsString.length()>0) {
 		FileWriter outputStream=null;
 		outputStream = new FileWriter(fileName);
 		outputStream.write(configAsString,0,configAsString.length());
 		outputStream.close();
 	    }
 	    return new String("Done!");
 	}
 	    
 	/** SwingWorker: finished */
 	protected void finished()
 	{
 	    try {
 		long elapsedTime = System.currentTimeMillis() - startTime;
 		jProgressBar.setString(jProgressBar.getString()+get()+
 				       " ("+elapsedTime+" ms)");
 	    }
 	    catch (ExecutionException e) {
 		String errMsg =
 		    "Convert Configuration FAILED:\n"+e.getCause().getMessage();
 		JOptionPane.showMessageDialog(frame,errMsg,
 					      "Convert Configuration failed",
 					      JOptionPane.ERROR_MESSAGE,null);
 		jProgressBar.setString(jProgressBar.getString()+"FAILED!");
 	    } 
 	    catch (Exception e) {
 		e.printStackTrace();
 		jProgressBar.setString(jProgressBar.getString()+"FAILED!");
 	    }
 	    jProgressBar.setIndeterminate(false);
 	}
     }
     
     
     /** load release templates from the database */
     private class UpdateTemplatesThread extends SwingWorker<String>
     {
 	/** member data */
 	private String releaseTag = null;
 	private long   startTime;
 	
 	/** standard constructor */
 	public UpdateTemplatesThread(String releaseTag)
 	{
 	    this.releaseTag = releaseTag;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws DatabaseException
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
 		jProgressBar.setString(jProgressBar.getString()+get()+
 				       " ("+elapsedTime+" ms)");
 	    }
 	    catch (ExecutionException e) {
 		String errMsg =
 		    "Update Templates FAILED:\n"+e.getCause().getMessage();
 		JOptionPane.showMessageDialog(frame,errMsg,
 					      "Update Templates failed",
 					      JOptionPane.ERROR_MESSAGE,null);
 		jProgressBar.setString(jProgressBar.getString()+"FAILED!");
 	    } 
 	    
 	    catch (Exception e) {
 		e.printStackTrace();
 		jProgressBar.setString(jProgressBar.getString()+"FAILED!");	
 	    }
 	    jProgressBar.setIndeterminate(false);
 	}
     }
     
     
     //--------------------------------------------------------------------------
     //
     // private member functions
     //
     //--------------------------------------------------------------------------
     
     /** create trees and tables, including models */
     private void createTreesAndTables()
     {
 	// current configuration tree
 	treeModelCurrentConfig = new ConfigurationTreeModel(currentConfig);
 	jTreeCurrentConfig     = new JTree(treeModelCurrentConfig) {
 		public String getToolTipText(MouseEvent evt) {
 		    String text = null;
 		    if (getRowForLocation(evt.getX(),evt.getY()) == -1)
 			return text;
 		    TreePath tp = getPathForLocation(evt.getX(),evt.getY());
 		    Object selectedNode = tp.getLastPathComponent();
 		    if (selectedNode instanceof Path) {
 			Path path = (Path)selectedNode;
 			if (path.datasetCount()>0) {
 			    text = "<html>"+path.name()+
 				" assigned to dataset(s): ";
 			    Iterator<PrimaryDataset> itPD =
 				path.datasetIterator();
 			    while (itPD.hasNext())
 				text += "<br>"+itPD.next().name();
 			}
 			String[] unresolved = path.unresolvedInputTags();
 			if (unresolved.length>0) {
 			    if (text!=null) text += "<br>"; else text="<html>";
 			    text += "Unresolved InputTags in "+path.name()+":";
 			    for (int i=0;i<unresolved.length;i++)
 				text+="<br>"+unresolved[i];
 			}
 			if (text!=null) text +="</html>";
 		    }
 		    else if (selectedNode instanceof ESSourceInstance||
 			     selectedNode instanceof ESModuleInstance||
 			     selectedNode instanceof ModuleInstance) {
 			Instance instance = (Instance)selectedNode;
 			text = instance.template().name();
 		    }
 		    else if (selectedNode instanceof ModuleReference) {
 			ModuleReference reference=(ModuleReference)selectedNode;
 			ModuleInstance  instance=(ModuleInstance)reference.parent();
 			text = instance.template().name();
 		    }
 		    else if (selectedNode instanceof Stream) {
 			Stream stream = (Stream)selectedNode;
 			text = "Event Content: " + stream.parentContent().name();
 		    }
 		    else if (selectedNode instanceof PrimaryDataset) {
 			PrimaryDataset dataset = (PrimaryDataset)selectedNode;
 			Stream         stream  = dataset.parentStream();
 			text = "Stream: " + stream.name();
 		    }
 		    return text;
 		}
 	    };
 	jTreeCurrentConfig.setToolTipText("");
 	jTreeCurrentConfig.setRootVisible(false);
 	jTreeCurrentConfig.setShowsRootHandles(true);
 	jTreeCurrentConfig.setEditable(true);
 	jTreeCurrentConfig.getSelectionModel()
 	    .setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 	
 	
 	jTreeCurrentConfig
 	    .setCellRenderer(new ConfigurationTreeRenderer());
 	jTreeCurrentConfig
 	    .setCellEditor(new ConfigurationTreeEditor(jTreeCurrentConfig,
 						       new ConfigurationTreeRenderer()));
 	
 	ConfigurationTreeMouseListener mouseListener =
 	    new ConfigurationTreeMouseListener(jTreeCurrentConfig,frame);
 	jTreeCurrentConfig.addMouseListener(mouseListener);
 	
 	ConfigurationTreeTransferHandler currentDndHandler =
 	    new ConfigurationTreeTransferHandler(jTreeCurrentConfig,currentRelease,
 						 treeModelParameters);
 	jTreeCurrentConfig.setTransferHandler(currentDndHandler);
 	jTreeCurrentConfig.setDropTarget(new ConfigurationTreeDropTarget());
 	jTreeCurrentConfig.setDragEnabled(true);
 	
 	// import tree
 	Color defaultTreeBackground = UIManager.getColor("Tree.textBackground");
 	Color importTreeBackground  = UIManager.getColor("Button.background");
 	UIManager.put("Tree.textBackground",importTreeBackground);
 	treeModelImportConfig = new ConfigurationTreeModel(importConfig);
 	jTreeImportConfig      = new JTree(treeModelImportConfig);
         jTreeImportConfig.setBackground(importTreeBackground);
 
 	ImportTreeMouseListener importMouseListener =
 	    new ImportTreeMouseListener(jTreeImportConfig,jTreeCurrentConfig);
 	jTreeImportConfig.addMouseListener(importMouseListener);
 	
 	jTreeImportConfig.setRootVisible(true);
 	jTreeImportConfig.setEditable(false);
 	jTreeImportConfig.getSelectionModel()
 	    .setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 	jTreeImportConfig.setCellRenderer(new ConfigurationTreeRenderer());
 	
 	
 	ConfigurationTreeTransferHandler importDndHandler =
 	    new ConfigurationTreeTransferHandler(jTreeImportConfig,null,null);
 	jTreeImportConfig.setTransferHandler(importDndHandler);
 	jTreeImportConfig.setDropTarget(new ConfigurationTreeDropTarget());
 	jTreeImportConfig.setDragEnabled(true);
 	
 	UIManager.put("Tree.textBackground",defaultTreeBackground);
 
 	// parameter table
 	treeModelParameters  = new ParameterTreeModel();
 	jTreeTableParameters = new TreeTable(treeModelParameters);
 	jTreeTableParameters
 	    .setTreeCellRenderer(new ParameterTreeCellRenderer());
 	
 	jTreeTableParameters.getColumnModel().getColumn(0)
 	    .setPreferredWidth(120);
 	jTreeTableParameters.getColumnModel().getColumn(1)
 	    .setPreferredWidth(90);
 	jTreeTableParameters.getColumnModel().getColumn(2)
 	    .setPreferredWidth(180);
 	jTreeTableParameters.getColumnModel().getColumn(3)
 	    .setPreferredWidth(30);
 	jTreeTableParameters.getColumnModel().getColumn(4)
 	    .setPreferredWidth(30);
 
 	jTreeTableParameters
 	    .addMouseListener
 	    (new ParameterTableMouseListener(frame,
 					     jTreeTableParameters));
     }
     
     /** show/hide the import-tree pane */
     private void showImportTree()
     {
 	jSplitPaneCurrentConfig.setRightComponent(jPanelImportConfig);
 	jSplitPaneCurrentConfig.setDividerLocation(0.5);
 	jSplitPaneCurrentConfig.setDividerSize(8);
     }
     private void hideImportTree()
     {
 	jSplitPaneCurrentConfig.setRightComponent(null);
 	jSplitPaneCurrentConfig.setDividerLocation(1);
 	jSplitPaneCurrentConfig.setDividerSize(1);
     }
     
     /** TEMPORARY! */
     public void refreshParameters()
     {
 	displayParameters();
     }
     
 
     /** display parameters of the instance in right upper area */
     private void displayParameters()
     {
 	TitledBorder border = (TitledBorder)jScrollPaneParameters.getBorder();
 
 	toolBar.disableAddUntrackedParameter();
 	
 	if (currentParameterContainer instanceof ParameterContainer) {
 	    toolBar.enableAddUntrackedParameter();
 	    jSplitPaneRightUpper.setDividerLocation(-1);
 	    jSplitPaneRightUpper.setDividerSize(8);
 
 	    ParameterContainer container =
 		(ParameterContainer)currentParameterContainer;
 	    
 	    if (container instanceof Instance) {
 		Instance i = (Instance)container;
 		String subName=i.template().parentPackage().subsystem().name();
 		String pkgName=i.template().parentPackage().name();
 		String cvsTag =i.template().cvsTag();
 		String type   =i.template().type();
 		String plugin =i.template().name();
 		
 		jTextFieldPackage.setText(subName+"/"+pkgName);
 		jTextFieldCVS.setText(cvsTag);
 		jLabelPlugin.setText(type + ":");
 		jTextFieldPlugin.setText(plugin);
 
 	    }
 	    else {
 		jTextFieldPackage.setText(new String());
 		jTextFieldCVS.setText(new String());
 		jLabelPlugin.setText(new String());
 		jTextFieldPlugin.setText(new String());
 		jTextFieldLabel.setText(new String());
 	    }
 
 
 	    
 	    DefaultComboBoxModel cbModel =
 		(DefaultComboBoxModel)jComboBoxPaths.getModel();
 	    cbModel.removeAllElements();
 	    
 	    if (container instanceof Referencable) {
 		Referencable module = (Referencable)container;
 		jTextFieldLabel.setText(module.name());
 		border.setTitle(module.name() + " Parameters");
 		jComboBoxPaths.setEnabled(true);
 		cbModel.addElement("");
 		Path[] paths = module.parentPaths();
 		for (Path p : paths) cbModel.addElement(p.name());
 	    }
 	    else {
 		jComboBoxPaths.setEnabled(false);
 	    }
 	    
 	    treeModelParameters.setParameterContainer(container);
 	}
 	else {
 	    clearParameters();
 	    treeModelParameters.setParameterContainer(currentConfig.psets());
 	    border.setTitle("Global PSets");
 	}
     }
 
     /** clear the right upper area */
     private void clearParameters()
     {
 	jSplitPaneRightUpper.setDividerLocation(0);
 	jSplitPaneRightUpper.setDividerSize(1);
 
 	jTextFieldPackage.setText("");
 	jTextFieldCVS.setText("");
 	jLabelPlugin.setText("Plugin:");
 	jTextFieldPlugin.setText("");
 	jTextFieldLabel.setText("");
 
 	toolBar.disableAddUntrackedParameter();
 
 	((DefaultComboBoxModel)jComboBoxPaths.getModel()).removeAllElements();
 	jComboBoxPaths.setEnabled(false);
 
 	currentParameterContainer = null;
 	treeModelParameters.setParameterContainer(currentParameterContainer);
 	
 	((TitledBorder)jScrollPaneParameters
 	 .getBorder()).setTitle("Parameters");
     }
     
     /** display the configuration snippet for currently selected component */
     private void displaySnippet()
     {
 	if (currentParameterContainer==treeModelCurrentConfig.psetsNode()) {
 	    String s="";
 	    Iterator<PSetParameter> itPSet = currentConfig.psetIterator();
 	    try {
 		while (itPSet.hasNext())
 		    s+= cnvEngine.getParameterWriter().toString(itPSet.next(),
 								cnvEngine,"");
 		jEditorPaneSnippet.setText(s);
 	    }
 	    catch (ConverterException e) {
 		jEditorPaneSnippet.setText(e.getMessage());
 	    }
 	}
 	else if (currentParameterContainer instanceof EDSourceInstance) {
 	    EDSourceInstance edsource = (EDSourceInstance)currentParameterContainer;
 	    try {
 		jEditorPaneSnippet.setText(cnvEngine.getEDSourceWriter().
 					   toString(edsource,cnvEngine,"  "));
 	    }
 	    catch (ConverterException e) {
 		jEditorPaneSnippet.setText(e.getMessage());
 	    }
 	}
 	else if (currentParameterContainer instanceof ESSourceInstance) {
 	    ESSourceInstance essource = (ESSourceInstance)currentParameterContainer;
 	    try {
 		jEditorPaneSnippet.setText(cnvEngine.getESSourceWriter().
 					   toString(essource,cnvEngine,"  "));
 	    }
 	    catch (ConverterException e) {
 		jEditorPaneSnippet.setText(e.getMessage());
 	    }
 	}
 	else if (currentParameterContainer instanceof ESModuleInstance) {
 	    ESModuleInstance esmodule = (ESModuleInstance)currentParameterContainer;
 	    try {
 		jEditorPaneSnippet.setText(cnvEngine.getESModuleWriter().
 					   toString(esmodule,cnvEngine,"  "));
 	    }
 	    catch (ConverterException e) {
 		jEditorPaneSnippet.setText(e.getMessage());
 	    }
 	}
 	else if (currentParameterContainer instanceof ServiceInstance) {
 	    ServiceInstance service = (ServiceInstance)currentParameterContainer;
 	    try {
 		jEditorPaneSnippet.setText(cnvEngine.getServiceWriter().
 					   toString(service,cnvEngine,"  "));
 	    }
 	    catch (ConverterException e) {
 		jEditorPaneSnippet.setText(e.getMessage());
 	    }
 	}
 	else if (currentParameterContainer instanceof ModuleInstance) {
 	    ModuleInstance module = (ModuleInstance)currentParameterContainer;
 	    try {
 		jEditorPaneSnippet.setText(cnvEngine.getModuleWriter().
 					   toString(module));
 	    }
 	    catch (ConverterException e) {
 		jEditorPaneSnippet.setText(e.getMessage());
 	    }
 	}
 	else if (currentParameterContainer instanceof OutputModule) {
 	    OutputModule output = (OutputModule)currentParameterContainer;
 	    try {
 		jEditorPaneSnippet.setText(cnvEngine.getOutputWriter()
 					   .toString(output));
 	    }
 	    catch (ConverterException e) {
 		jEditorPaneSnippet.setText(e.getMessage());
 	    }
 	}
 	else if (currentParameterContainer instanceof Path) {
 	    Path path = (Path)currentParameterContainer;
 	    jEditorPaneSnippet.setText(cnvEngine.getPathWriter().
 				       toString(path,cnvEngine,"  "));
 	}
 	else if (currentParameterContainer instanceof Sequence) {
 	    Sequence sequence = (Sequence)currentParameterContainer;
 	    jEditorPaneSnippet.setText(cnvEngine.getSequenceWriter().
 				       toString(sequence,cnvEngine,"  "));
 	}
 	else {
 	    clearSnippet();
 	}
 	jEditorPaneSnippet.setCaretPosition(0);
     }
     
     /** clear snippet pane (right-lower) */
     private void clearSnippet()
     {
 	jEditorPaneSnippet.setText("");
     }
 
 
     /** display the event content editor, fill all fields */
     private void fillEventContents()
     {
 	// fill combo box with all event contents
 	DefaultComboBoxModel cbm =
 	    (DefaultComboBoxModel)jComboBoxEventContent.getModel();
 	cbm.removeAllElements();
 	Iterator<EventContent> itEC = currentConfig.contentIterator();
 	while (itEC.hasNext()) cbm.addElement(itEC.next().name());
     }
 
 
     //
     // ACTIONLISTENER CALLBACKS
     //
 
     private void jButtonProcessActionPerformed(ActionEvent e)
     {
 	String processName = jTextFieldProcess.getText();
 	if (processName.length()==0||processName.indexOf('_')>=0)
 	    jTextFieldProcess.setText(currentConfig.processName());
 	else
 	    currentConfig.setHasChanged(true);
     }
     private void jButtonReleaseActionPerformed(ActionEvent e)
     {
 	if (currentConfig.isEmpty()) return;
 	SoftwareReleaseDialog dialog = new SoftwareReleaseDialog(frame,
 								 currentRelease);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
     }
     private void jButtonCancelSearchActionPerformed(ActionEvent e)
     {
 	TreePath tp = jTreeCurrentConfig.getSelectionPath();
 	jTextFieldSearch.setText("");
 	setCurrentConfig(currentConfig);
 	if (tp!=null) {
 	    Object   obj  = tp.getLastPathComponent();
 	    Object[] objs = tp.getPath();
 	    objs[0]=currentConfig;
 	    tp = new TreePath(objs);
 	    jTreeCurrentConfig.scrollPathToVisible(tp);
 	    jTreeCurrentConfig.setSelectionPath(tp);
 	}
     }
     private void jToggleButtonImportActionPerformed(ActionEvent e)
     {
 	AbstractButton b = (AbstractButton)e.getSource();
 	if (b.isSelected()) showImportTree();
 	else hideImportTree();
     }
     private void jButtonImportCancelSearchActionPerformed(ActionEvent e)
     {
 	TreePath tp = jTreeImportConfig.getSelectionPath();
 	jTextFieldImportSearch.setText("");
 	treeModelImportConfig.setConfiguration(importConfig);
 	if (tp!=null) {
 	    Object[] objs = tp.getPath();
 	    objs[0]=importConfig;
 	    tp = new TreePath(objs);
 	    jTreeImportConfig.scrollPathToVisible(tp);
 	    jTreeImportConfig.setSelectionPath(tp);
 	}
     }
     private void jComboBoxPathsItemStateChanged(ItemEvent e)
     {
 	if (e.getStateChange() == ItemEvent.SELECTED) {
 	    
 	    String moduleLabel = jTextFieldLabel.getText();
 	    String pathName = e.getItem().toString();
 	    if (moduleLabel==""||pathName=="") return;
 	    
 	    // collapse complete tree
 	    int row = jTreeCurrentConfig.getRowCount() - 1;
 	    while (row >= 0) {
 		jTreeCurrentConfig.collapseRow(row);
 		row--;
 	    }
 	    
 	    // construct the treepath to the selected reference
 	    Path path = currentConfig.path(pathName);
 	    ArrayList<Reference> pathToNode = new ArrayList<Reference>();
 	    String name = moduleLabel;
 	    while (name!=pathName) {
 		Iterator<Reference> itR = path.recursiveReferenceIterator();
 		while (itR.hasNext()) {
 		    Reference r = itR.next();
 		    if (r.name().equals(name)) {
 			name = r.container().name();
 			pathToNode.add(r);
 			break;
 		    }
 		}
 	    }
 	
 	    TreePath tp =
 		new TreePath(treeModelCurrentConfig.getPathToRoot(path));
 	    for (int i=pathToNode.size()-1;i>=0;i--)
 		tp = tp.pathByAddingChild(pathToNode.get(i));
 	    jTreeCurrentConfig.expandPath(tp);
 	    jTreeCurrentConfig.setSelectionPath(tp);
 	    jTreeCurrentConfig.scrollPathToVisible(tp);
 	}
     }
     private void jSplitPaneRightComponentMoved(ComponentEvent e)
     {
 	if (!(currentParameterContainer instanceof Instance)) {
 	    jSplitPaneRightUpper.setDividerLocation(0);
 	    jSplitPaneRightUpper.setDividerSize(1);
 	}
     }
     
 
     //
     // DOCUMENTLISTENER CALLBACKS
     //
     private void jTextFieldSearchInsertUpdate(DocumentEvent e)
     {
 	try {
 	    String search = e.getDocument().getText(0,e.getDocument().getLength());
 	    jTreeCurrentConfigUpdateSearch(search);
 	}
 	catch (Exception ex) {}
     }
     private void jTextFieldSearchRemoveUpdate(DocumentEvent e)
     {
 	try {
 	    String search = e.getDocument().getText(0,e.getDocument().getLength());
 	    jTreeCurrentConfigUpdateSearch(search);
 	}
 	catch (Exception ex) {}
     }
     private void jTreeCurrentConfigUpdateSearch(String search)
     {
 	if (search.length()>0) {
 	    String mode = 
 		buttonGroupSearch1.getSelection().getActionCommand()+":"+
 		buttonGroupSearch2.getSelection().getActionCommand();
 	    jButtonCancelSearch.setEnabled(true);
 	    ModifierInstructions modifications = new ModifierInstructions();
 	    modifications.interpretSearchString(search,mode,currentConfig);
 	    ConfigurationModifier modifier = 
 		new ConfigurationModifier(currentConfig);
 	    modifier.modify(modifications);
 	    treeModelCurrentConfig.setConfiguration(modifier);
 	    jTreeConfigExpandLevel1Nodes(jTreeCurrentConfig);
 	}
 	else {
 	    setCurrentConfig(currentConfig);
 	    jButtonCancelSearch.setEnabled(false);
 	}
     }
     private void jTreeConfigExpandLevel1Nodes(JTree t)
     {
 	ConfigurationTreeModel m = (ConfigurationTreeModel)t.getModel();
 	
 	TreePath tpPSets = new TreePath(m.getPathToRoot(m.psetsNode()));
 	t.expandPath(tpPSets);
 	TreePath tpEDSources = new TreePath(m.getPathToRoot(m.edsourcesNode()));
 	t.expandPath(tpEDSources);
 	TreePath tpESSources = new TreePath(m.getPathToRoot(m.essourcesNode()));
 	t.expandPath(tpESSources);
 	TreePath tpESModules = new TreePath(m.getPathToRoot(m.esmodulesNode()));
 	t.expandPath(tpESModules);
 	TreePath tpServices = new TreePath(m.getPathToRoot(m.servicesNode()));
 	t.expandPath(tpESSources);
 	TreePath tpPaths = new TreePath(m.getPathToRoot(m.pathsNode()));
 	t.expandPath(tpPaths);
 	TreePath tpSequences = new TreePath(m.getPathToRoot(m.sequencesNode()));
 	t.expandPath(tpSequences);
 	TreePath tpModules = new TreePath(m.getPathToRoot(m.modulesNode()));
 	t.expandPath(tpModules);
 	TreePath tpOutputs = new TreePath(m.getPathToRoot(m.outputsNode()));
 	t.expandPath(tpOutputs);
 	TreePath tpContents = new TreePath(m.getPathToRoot(m.contentsNode()));
 	t.expandPath(tpContents);
 	TreePath tpStreams = new TreePath(m.getPathToRoot(m.streamsNode()));
 	t.expandPath(tpStreams);
 	TreePath tpDatasets = new TreePath(m.getPathToRoot(m.datasetsNode()));
 	t.expandPath(tpDatasets);
     }
 
     private void jTextFieldImportSearchInsertUpdate(DocumentEvent e)
     {
 	try {
 	    String search = e.getDocument().getText(0,e.getDocument().getLength());
 	    jTreeImportConfigUpdateSearch(search);
 	}
 	catch (Exception ex) {}
     }
     private void jTextFieldImportSearchRemoveUpdate(DocumentEvent e)
     {
 	try {
 	    String search = e.getDocument().getText(0,e.getDocument().getLength());
 	    jTreeImportConfigUpdateSearch(search);
 	}
 	catch (Exception ex) {}
     }
     private void jTreeImportConfigUpdateSearch(String search)
     {
 	if (search.length()>0) {
 	    String mode = 
 		buttonGroupImportSearch1.getSelection().getActionCommand()+":"+
 		buttonGroupImportSearch2.getSelection().getActionCommand();
 	    jButtonImportCancelSearch.setEnabled(true);
 	    ModifierInstructions modifications = new ModifierInstructions();
 	    modifications.interpretSearchString(search,mode,importConfig);
 	    ConfigurationModifier modifier = 
 		new ConfigurationModifier(importConfig);
 	    modifier.modify(modifications);
 	    treeModelImportConfig.setConfiguration(modifier);
 	    jTreeConfigExpandLevel1Nodes(jTreeImportConfig);
 	}
 	else {
 	    treeModelImportConfig.setConfiguration(importConfig);
 	    jButtonImportCancelSearch.setEnabled(false);
 	}
     }
     
     //
     // TREEMODELLISTENER CALLBACKS
     //
     
     private void jTreeCurrentConfigTreeNodesChanged(TreeModelEvent e)
     {
 	if (currentConfig==null) return;
 	Object node = e.getChildren()[0];
 	if (node instanceof EventContent)
 	    fillEventContents();
 	else
 	    displaySnippet();
     }
     private void jTreeCurrentConfigTreeNodesInserted(TreeModelEvent e) {}
     private void jTreeCurrentConfigTreeNodesRemoved(TreeModelEvent e) {}
     private void jTreeCurrentConfigTreeStructureChanged(TreeModelEvent e) {}
 
     private void jTreeTableParametersTreeNodesChanged(TreeModelEvent e)
     {
 	Object changedNode = e.getChildren()[0];
 	
 	if (changedNode instanceof Parameter) {
 	    Parameter p = (Parameter)changedNode;
 	    treeModelCurrentConfig.nodeChanged(p);
 	    treeModelCurrentConfig.updateLevel1Nodes();
 	    ParameterContainer parentContainer = p.getParentContainer();
 	    if (parentContainer==null) currentConfig.setHasChanged(true);
 	    else if (parentContainer instanceof Referencable)
 		jTreeCurrentConfig.updateUI();
 	}
     }
     private void jTreeTableParametersTreeNodesInserted(TreeModelEvent e)
     {
 	Object parentNode = e.getTreePath().getLastPathComponent();
 	int    childIndex = e.getChildIndices()[0];
 	treeModelCurrentConfig.nodeInserted(parentNode,childIndex);
 	treeModelCurrentConfig.updateLevel1Nodes();
 	if (parentNode instanceof Parameter) {
 	    ParameterContainer parentContainer =
 		((Parameter)parentNode).getParentContainer();
 	    if (parentContainer==null) currentConfig.setHasChanged(true);
 	}
     }
     private void jTreeTableParametersTreeNodesRemoved(TreeModelEvent e)
     {
 	Object parentNode = e.getTreePath().getLastPathComponent();
 	Object childNode  = e.getChildren()[0];
 	int    childIndex = e.getChildIndices()[0];
 	treeModelCurrentConfig.nodeRemoved(parentNode,childIndex,childNode);
 	treeModelCurrentConfig.updateLevel1Nodes();
 	if (parentNode instanceof Parameter) {
 	    ParameterContainer parentContainer =
 		((Parameter)parentNode).getParentContainer();
 	    if (parentContainer==null) currentConfig.setHasChanged(true);
 	}
     }
     
 
     //
     // TREESELECTIONLISTENER CALLBACKS
     //
     
     private void jTreeCurrentConfigValueChanged(TreeSelectionEvent e)
     {
 	TreePath treePath=e.getNewLeadSelectionPath();
 	if (treePath==null) {
 	    clearParameters();
 	    clearSnippet();
 	    return;
 	}
 
 	Object node=treePath.getLastPathComponent();
 	if(node==null) {
 	    clearParameters();
 	    clearSnippet();
 	    return;
 	}
 
 
 	if (node instanceof EventContent) {
 	    jSplitPane.setRightComponent(jPanelContentEditor);
 	    fillEventContents();
 	    jComboBoxEventContent.getModel().setSelectedItem(node.toString());
 	    return;
 	}
 	
 	
 	jSplitPane.setRightComponent(jSplitPaneRight);
 	
 	while (node instanceof Parameter) {
 	    Parameter p = (Parameter)node;
 	    node = p.parent();
 	}
 	
 	if (node instanceof Reference) {
 	    node = ((Reference)node).parent();
 	}
 	
 	if (node instanceof ParameterContainer) {
 	    currentParameterContainer = node;
 	    displayParameters();
 	    displaySnippet();
 	}
 	else if (node==null||node==treeModelCurrentConfig.psetsNode()) {
 	    currentParameterContainer = currentConfig.psets();
 	    displayParameters();
 	    displaySnippet();
 	}
 	else if (node instanceof ReferenceContainer) {
 	    clearParameters();
 	    currentParameterContainer = node;
 	    displaySnippet();
 	}
 	else {
 	    clearParameters();
 	    clearSnippet();
 	}
     }
 
 
     //
     // CONTENT EDITOR CALLBACKS
     //
 
     private void jComboBoxEventContentActionPerformed(ActionEvent evt)
     {
 	// set selected event content, in combobox *and* tree!
 	Object selectedItem = jComboBoxEventContent.getSelectedItem();
 	if (selectedItem==null) return;
 	String contentName = selectedItem.toString();
 	EventContent content = currentConfig.content(contentName);
 	jTreeCurrentConfig
 	    .setSelectionPath(new TreePath(treeModelCurrentConfig
 					   .getPathToRoot(content)));
 	
 	// fill streams
 	DefaultListModel slm = (DefaultListModel)jListStreams.getModel();
 	slm.clear();
 	Iterator<Stream> itS = content.streamIterator();
 	while (itS.hasNext()) slm.addElement(itS.next().name());
 
 	// fill datasets
 	DefaultListModel dlm = (DefaultListModel)jListDatasets.getModel();
 	dlm.clear();
 	Iterator<PrimaryDataset> itPD = content.datasetIterator();
 	while (itPD.hasNext()) dlm.addElement(itPD.next().name());
 
 	// fill paths
 	DefaultListModel plm = (DefaultListModel)jListPaths.getModel();
 	plm.clear();
 	//Iterator<Path> itP = content.pathIterator();
 	Iterator<Path> itP = content.orderedPathIterator();
 	while (itP.hasNext()) plm.addElement(itP.next().name());
 	
 	// fill output command combobox menu
 	fillComboBoxCommandsMenu(null);
 	
 	// fill output commands
 	CommandTableModel ctm = (CommandTableModel)jTableCommands.getModel();
 	ctm.setContent(content);
 	
 	// clear output module text area
 	jTextAreaOutputModule.setText("");
     }
     private void jListStreamsValueChanged(ListSelectionEvent evt)
     {
 	ListSelectionModel lsmS = jListStreams.getSelectionModel();
 	if (lsmS.getValueIsAdjusting()) return;
 	
 	String contentName = jComboBoxEventContent.getSelectedItem().toString();
 	EventContent content = currentConfig.content(contentName);
 	
 	Stream stream = (lsmS.isSelectionEmpty()) ?
 	    null : content.stream(lsmS.getMinSelectionIndex());
 
 	// fill datasets
 	DefaultListModel dlm = (DefaultListModel)jListDatasets.getModel();
 	dlm.clear();
 	Iterator<PrimaryDataset> itPD = (stream==null) ?
 	    content.datasetIterator() : stream.datasetIterator();
 	while (itPD.hasNext()) dlm.addElement(itPD.next().name());
 	
 	// fill paths
 	DefaultListModel plm = (DefaultListModel)jListPaths.getModel();
 	plm.clear();
 	Iterator<Path> itP = (stream==null) ?
 	    content.orderedPathIterator() : stream.orderedPathIterator();
 	while (itP.hasNext()) plm.addElement(itP.next().name());
 	
 	// fill output command combobox menu
 	fillComboBoxCommandsMenu(null);
 	
 	// fill output commands
 	CommandTableModel ctm = (CommandTableModel)jTableCommands.getModel();
 	ctm.setStream(stream);
 	
 	// clear output module text area
 	updateOutputModulePreview();
     }
     private void jListDatasetsValueChanged(ListSelectionEvent evt)
     {
 	ListSelectionModel lsmS = jListStreams.getSelectionModel();
 	ListSelectionModel lsmD = jListDatasets.getSelectionModel();
 
 	if (lsmD.getValueIsAdjusting()) return;
 	
 	String contentName = jComboBoxEventContent.getSelectedItem().toString();
 	EventContent content = currentConfig.content(contentName);
 	
 	Stream stream = (lsmS.isSelectionEmpty()) ?
 	    null : content.stream(lsmS.getMinSelectionIndex());
 	
 	PrimaryDataset dataset = (lsmD.isSelectionEmpty()) ?
 	    null : content.dataset(lsmD.getMinSelectionIndex());
 	
 	// fill paths
 	DefaultListModel plm = (DefaultListModel)jListPaths.getModel();
 	plm.clear();
 	Iterator<Path> itP = (dataset==null) ?
 	    (stream==null) ?
 	    content.orderedPathIterator() : stream.orderedPathIterator() :
 	    dataset.orderedPathIterator();
 	while (itP.hasNext()) plm.addElement(itP.next().name());
 
 	// fill output command combobox menu
 	fillComboBoxCommandsMenu(null);
 	
 	// fill output commands
 	CommandTableModel ctm = (CommandTableModel)jTableCommands.getModel();
 	ctm.setDataset(dataset);
     }
     private void jListPathsValueChanged(ListSelectionEvent evt)
     {
 	ListSelectionModel lsmP = jListPaths.getSelectionModel();
 
 	if (lsmP.getValueIsAdjusting()) return;
 
 	Path path = null;
 	if (!lsmP.isSelectionEmpty()) {
 	    String pathName = jListPaths.getSelectedValue().toString();
 	    path = currentConfig.path(pathName);
 	}
 	if (path==null) jComboBoxCommands.setEditable(true);
 	else            jComboBoxCommands.setEditable(false);
 	
 	// fill output command combobox menu
 	fillComboBoxCommandsMenu(path);
 	
 	// fill output commands
 	CommandTableModel ctm = (CommandTableModel)jTableCommands.getModel();
 	ctm.setPath(path);
     }
     private void jComboBoxCommandsActionPerformed(ActionEvent evt)
     {
 	Object selectedItem = jComboBoxCommands.getSelectedItem();
 	if (selectedItem==null) return;
 	
 	String contentName = jComboBoxEventContent.getSelectedItem().toString();
 	EventContent content = currentConfig.content(contentName);
 	
 	if (selectedItem instanceof String) {
 	    String commandAsString = (String)selectedItem;
 	    OutputCommand command = new OutputCommand();
 	    if (command.initializeFromString(commandAsString)) {
 		content.insertCommand(command);
 		fillComboBoxCommandsMenu(null);
 	    }
 	}
 	else if (selectedItem instanceof OutputCommand) {
 	    System.out.println("It's an OutputCommand!");
 	    OutputCommand command = (OutputCommand)selectedItem;
 	    System.out.println("content.hasChanged = " + content.hasChanged());
 	    content.insertCommand(command);
 	    System.out.println("content.hasChanged = " + content.hasChanged());
 	    fillComboBoxCommandsMenu(command.parentPath());
 	}
 
 	CommandTableModel ctm = (CommandTableModel)jTableCommands.getModel();
 	ctm.fireTableDataChanged();
 	jComboBoxCommands.setSelectedIndex(0);
 
 	Iterator<Stream> itS = content.streamIterator();
 	while (itS.hasNext()) {
 	    OutputModule output = itS.next().outputModule();
 	    treeModelCurrentConfig.nodeChanged(output.parameter(1));
 	    if (output.referenceCount()>0)
 		treeModelCurrentConfig
 		    .nodeStructureChanged(output.reference(0));
 	}
 
 	updateOutputModulePreview();
     }
     private void jTableCommandsMousePressed(MouseEvent evt)
     {
 	if (evt.isPopupTrigger()) jTableCommandsShowPopup(evt);
     }
     private void jTableCommandsMouseReleased(MouseEvent evt)
     {
 	if (evt.isPopupTrigger()) jTableCommandsShowPopup(evt);
     }
 
     /** update the contents of the OutputModule config area at the bottom */
     private void updateOutputModulePreview()
     {
 	jTextAreaOutputModule.setText("");
 
 	ListSelectionModel lsmS = jListStreams.getSelectionModel();
 	String             contentName =
 	    jComboBoxEventContent.getSelectedItem().toString();
 	EventContent       content = currentConfig.content(contentName);
 	Stream             stream = (lsmS.isSelectionEmpty()) ?
 	    null : content.stream(lsmS.getMinSelectionIndex());
 	
 	if (stream!=null) {
 	    try {
 		jTextAreaOutputModule
 		    .setText(cnvEngine.getOutputWriter()
 			     .toString(stream.outputModule()));
 	    }
 	    catch (ConverterException e) {
 		jTextAreaOutputModule.setText(e.getMessage());
 	    }
 	}
     }
     
     /** fill the combo box menu for output commands to be added */
     private void fillComboBoxCommandsMenu(Path path)
     {
 	String contentName = jComboBoxEventContent.getSelectedItem().toString();
 	EventContent content = currentConfig.content(contentName);
 	
 	DefaultComboBoxModel cbm =
 	    (DefaultComboBoxModel)jComboBoxCommands.getModel();
 	cbm.removeAllElements();
 	cbm.addElement(null);
 	
 	if (path==null) {
 	    OutputCommand ocDropAll = new OutputCommand();
 	    ocDropAll.setDrop();
 	    if (content.indexOfCommand(ocDropAll)<0) cbm.addElement(ocDropAll);
 	    OutputCommand ocDropHLT = new OutputCommand();
 	    ocDropHLT.setDrop();
 	    ocDropHLT.setModuleName("hlt*");
 	    if (content.indexOfCommand(ocDropHLT)<0) cbm.addElement(ocDropHLT);
 	    OutputCommand ocRawOnl = new OutputCommand();
 	    ocRawOnl.setClassName("FEDRawDataCollection");
 	    ocRawOnl.setModuleName("source");
 	    if (content.indexOfCommand(ocRawOnl)<0) cbm.addElement(ocRawOnl);
 	    OutputCommand ocRawOff = new OutputCommand();
 	    ocRawOff.setClassName("FEDRawDataCollection");
 	    ocRawOff.setModuleName("rawDataCollector");
 	    if (content.indexOfCommand(ocRawOff)<0) cbm.addElement(ocRawOff);
 	    OutputCommand ocTrgRes = new OutputCommand();
 	    ocTrgRes.setClassName("edmTriggerResults");
 	    if (content.indexOfCommand(ocTrgRes)<0) cbm.addElement(ocTrgRes);
 	    OutputCommand ocTrgEvt = new OutputCommand();
 	    ocTrgEvt.setClassName("triggerTriggerEvent");
 	    if (content.indexOfCommand(ocTrgEvt)<0) cbm.addElement(ocTrgEvt);
 	    return;
 	}
 	
 	// path is not null
 	Iterator<Reference> itR = path.recursiveReferenceIterator();
 	while (itR.hasNext()) {
 	    Reference reference = itR.next();
 	    if (reference instanceof ModuleReference) {
 		ModuleReference module = (ModuleReference)reference;
 		String moduleType =
 		    ((ModuleInstance)module.parent()).template().type();
 		if (moduleType.equals("EDProducer")||
 		    moduleType.equals("EDFilter")||
 		    moduleType.equals("HLTFilter")) {
 		    OutputCommand command = new OutputCommand(path,reference);
 		    if (content.indexOfCommand(command)<0)
 			cbm.addElement(command);
 		}
 	    }
 	}
     }
     
     /** show popup menu for command in table being right-clicked */
     private void jTableCommandsShowPopup(MouseEvent e)
     {
 	int row = jTableCommands.rowAtPoint(new Point(e.getX(),e.getY()));
 	jTableCommands.getSelectionModel().setSelectionInterval(row,row);
 	if (row<0) return;
 	
 	String contentName = jComboBoxEventContent.getSelectedItem().toString();
 	EventContent content = currentConfig.content(contentName);
 	
 	CommandTableModel ctm = (CommandTableModel)jTableCommands.getModel();
 	OutputCommand command = (OutputCommand)ctm.getValueAt(row,1);
 	int           index   = content.indexOfCommand(command);
 
 	ListSelectionModel lsm = jListPaths.getSelectionModel();
 	
 	JMenuItem  item = null;
 
 	// Edit
 	JPopupMenu menu = new JPopupMenu();
 	item = new JMenuItem("Edit"); menu.add(item);
 	item.setActionCommand(content.name()+":"+
 			      content.indexOfCommand(command));
 	item.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jTableCommandsPopupEdit(e);
 		}
 	    });
 	
 	menu.addSeparator();
 
 	/*
 
 	  if (lsm.isSelectionEmpty()) {
 	  
 	  // Top
 	  item = new JMenuItem("Top"); menu.add(item);
 	    item.setActionCommand(content.name()+":"+index);
 	    item.addActionListener(new ActionListener() {
 	    public void actionPerformed(ActionEvent e) {
 	    jTableCommandsPopupTop(e);
 	    }
 	    });
 	    if (index==0) item.setEnabled(false);
 	    
 	    // Up
 	    item = new JMenuItem("Up");     menu.add(item);
 	    item.setActionCommand(content.name()+":"+
 	    content.indexOfCommand(command));
 	    item.addActionListener(new ActionListener() {
 	    public void actionPerformed(ActionEvent e) {
 	    jTableCommandsPopupUp(e);
 	    }
 	    });
 	    if (index==0) item.setEnabled(false);
 	    
 	    // Down
 	    item = new JMenuItem("Down");   menu.add(item);
 	    item.setActionCommand(content.name()+":"+
 	    content.indexOfCommand(command));
 	    item.addActionListener(new ActionListener() {
 	    public void actionPerformed(ActionEvent e) {
 	    jTableCommandsPopupDown(e);
 	    }
 	    });
 	    if (index==content.commandCount()-1) item.setEnabled(false);
 	    
 	    // Bottom
 	    item = new JMenuItem("Bottom"); menu.add(item);
 	    item.setActionCommand(content.name()+":"+
 	    content.indexOfCommand(command));
 	    item.addActionListener(new ActionListener() {
 	    public void actionPerformed(ActionEvent e) {
 	    jTableCommandsPopupBottom(e);
 	    }
 	    });
 	    if (index==content.commandCount()-1) item.setEnabled(false);
 	    
 	    menu.addSeparator();
 	    }
 	*/
 	
 	// Remove
 	item = new JMenuItem("Remove"); menu.add(item);
 	item.setActionCommand(content.name()+":"+
 			      content.indexOfCommand(command));
 	item.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jTableCommandsPopupRemove(e);
 		}
 	    });
 
 	menu.show(e.getComponent(),e.getX(),e.getY());
     }
    
     /** jTableCommands: popup action 'Edit' */
     private void jTableCommandsPopupEdit(ActionEvent e)
     {
 	String s[] = ((JMenuItem)e.getSource()).getActionCommand().split(":");
 	String contentName = s[0];
 	int    commandIndex = (new Integer(s[1])).intValue();
 	EventContent  content = currentConfig.content(contentName);
 	OutputCommand command = content.command(commandIndex);
 
 	OutputCommandEditorDialog dlg =
 	    new OutputCommandEditorDialog(frame,content,command);
 	dlg.pack();
 	dlg.setLocationRelativeTo(frame);
 	dlg.setVisible(true);
 	if (dlg.command()!=null) command.set(dlg.command());
 
 	CommandTableModel ctm = (CommandTableModel)jTableCommands.getModel();
 	ctm.fireTableDataChanged();
 
 	updateOutputModulePreview();
 	
 	Iterator<Stream> itS = content.streamIterator();
 	while (itS.hasNext()) {
 	    OutputModule output = itS.next().outputModule();
 	    treeModelCurrentConfig.nodeChanged(output.parameter(1));
 	    if (output.referenceCount()>0)
 		treeModelCurrentConfig
 		    .nodeStructureChanged(output.reference(0));
 	}
     }
     /** jTableCommands: popup action 'Top' */
     /*
       private void jTableCommandsPopupTop(ActionEvent e)
       {
       String s[] = ((JMenuItem)e.getSource()).getActionCommand().split(":");
       String contentName = s[0];
       int    commandIndex = (new Integer(s[1])).intValue();
       EventContent  content = currentConfig.content(contentName);
       OutputCommand command = content.command(commandIndex);
       
       int targetIndex = 0;
       content.moveCommand(command,targetIndex);
       
       CommandTableModel ctm = (CommandTableModel)jTableCommands.getModel();
       ctm.fireTableDataChanged();
       
       updateOutputModulePreview();
       
       Iterator<Stream> itS = content.streamIterator();
       while (itS.hasNext()) {
       OutputModule output = itS.next().outputModule();
       treeModelCurrentConfig.nodeChanged(output.parameter(1));
       if (output.referenceCount()>0)
       treeModelCurrentConfig
       .nodeStructureChanged(output.reference(0));
       }
       }
     */
     
     /** jTableCommands: popup action 'Up' */
     /*
       private void jTableCommandsPopupUp(ActionEvent e)
       {
       String s[] = ((JMenuItem)e.getSource()).getActionCommand().split(":");
       String contentName = s[0];
       int    commandIndex = (new Integer(s[1])).intValue();
       EventContent  content = currentConfig.content(contentName);
       OutputCommand command = content.command(commandIndex);
       
       int targetIndex = commandIndex-1;
       content.moveCommand(command,targetIndex);
       
       CommandTableModel ctm = (CommandTableModel)jTableCommands.getModel();
       ctm.fireTableDataChanged();
       
       updateOutputModulePreview();
       
       Iterator<Stream> itS = content.streamIterator();
       while (itS.hasNext()) {
       OutputModule output = itS.next().outputModule();
       treeModelCurrentConfig.nodeChanged(output.parameter(1));
       if (output.referenceCount()>0)
       treeModelCurrentConfig
       .nodeStructureChanged(output.reference(0));
       }
       }
     */
     
     /** jTableCommands: popup action 'Down' */
     /*
       private void jTableCommandsPopupDown(ActionEvent e)
       {
       String s[] = ((JMenuItem)e.getSource()).getActionCommand().split(":");
       String contentName = s[0];
       int    commandIndex = (new Integer(s[1])).intValue();
       EventContent  content = currentConfig.content(contentName);
       OutputCommand command = content.command(commandIndex);
       
       int targetIndex = commandIndex+1;
       content.moveCommand(command,targetIndex);
       
       CommandTableModel ctm = (CommandTableModel)jTableCommands.getModel();
       ctm.fireTableDataChanged();
       
       updateOutputModulePreview();
       
       Iterator<Stream> itS = content.streamIterator();
       while (itS.hasNext()) {
       OutputModule output = itS.next().outputModule();
       treeModelCurrentConfig.nodeChanged(output.parameter(1));
       if (output.referenceCount()>0)
       treeModelCurrentConfig
       .nodeStructureChanged(output.reference(0));
       }
       }
     */
     
     /** jTableCommands: popup action 'Bottom' */
     /*
       private void jTableCommandsPopupBottom(ActionEvent e)
       {
       String s[] = ((JMenuItem)e.getSource()).getActionCommand().split(":");
       String contentName = s[0];
       int    commandIndex = (new Integer(s[1])).intValue();
       EventContent  content = currentConfig.content(contentName);
       OutputCommand command = content.command(commandIndex);
       
       int targetIndex = content.commandCount()-1;
       content.moveCommand(command,targetIndex);
       
       CommandTableModel ctm = (CommandTableModel)jTableCommands.getModel();
       ctm.fireTableDataChanged();
       
       updateOutputModulePreview();
       
       Iterator<Stream> itS = content.streamIterator();
       while (itS.hasNext()) {
       OutputModule output = itS.next().outputModule();
       treeModelCurrentConfig.nodeChanged(output.parameter(1));
       if (output.referenceCount()>0)
       treeModelCurrentConfig
       .nodeStructureChanged(output.reference(0));
       }
     }
     */
     
     /** jTableCommands: popup action 'Remove' */
     private void jTableCommandsPopupRemove(ActionEvent e)
     {
 	String s[] = ((JMenuItem)e.getSource()).getActionCommand().split(":");
 	String contentName = s[0];
 	int    commandIndex = (new Integer(s[1])).intValue();
 	EventContent  content = currentConfig.content(contentName);
 	OutputCommand command = content.command(commandIndex);
 	
 	content.removeCommand(command);
 	
 	fillComboBoxCommandsMenu(command.parentPath());
 	CommandTableModel ctm = (CommandTableModel)jTableCommands.getModel();
 	ctm.fireTableDataChanged();
 
 	updateOutputModulePreview();
 	
 	Iterator<Stream> itS = content.streamIterator();
 	while (itS.hasNext()) {
 	    OutputModule output = itS.next().outputModule();
 	    treeModelCurrentConfig.nodeChanged(output.parameter(1));
 	    if (output.referenceCount()>0)
 		treeModelCurrentConfig
 		    .nodeStructureChanged(output.reference(0));
 	}
     }
     
     
     //
     // CREATE GUI COMPONENTS
     //
     
     /** create the  menubar */
     private void createMenuBar()
     {
 	menuBar = new MenuBar(jMenuBar,this);
 	frame.setJMenuBar(jMenuBar);
     }
 
     /** create the toolbar */
     private void createToolBar()
     {
 	jToolBar.setFloatable(false);
 	jToolBar.setRollover(true);
 	toolBar = new ToolBar(jToolBar,this);
     }
 
     /** create the database connection panel */
     private void createDbConnectionPanel()
     {
 	jPanelDbConnection = new DatabaseInfoPanel();
     }
 
     /** create the left panel */
     private void createLeftPanel()
     {
 	createConfigurationPanel();      // -> tab 1
 	//createStreamsAndDatasetsPanel(); // -> tab 2
 
         JLabel jLabelConfig  = new javax.swing.JLabel();
 	JLabel jLabelProcess = new javax.swing.JLabel();
         JLabel jLabelRelease = new javax.swing.JLabel();
         JLabel jLabelCreated = new javax.swing.JLabel();
         JLabel jLabelCreator = new javax.swing.JLabel();
 	
         jLabelConfig.setText("Configuration:");
 
         jTextFieldCurrentConfig.setBackground(new java.awt.Color(255, 255, 255));
         jTextFieldCurrentConfig.setEditable(false);
         jTextFieldCurrentConfig.setFont(new java.awt.Font("Dialog", 1, 12));
         jTextFieldCurrentConfig.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
 
         jLabelProcess.setText("Process:");
 
         jTextFieldProcess.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
 
         jLabelRelease.setText("Release:");
 
         jButtonRelease.setBackground(new java.awt.Color(255, 255, 255));
         jButtonRelease.setForeground(new java.awt.Color(0, 0, 204));
 	jButtonRelease.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
 
         jLabelCreated.setText("Created:");
 
         jTextFieldCreated.setBackground(new java.awt.Color(255, 255, 255));
         jTextFieldCreated.setEditable(false);
         jTextFieldCreated.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
 
         jLabelCreator.setText("Creator:");
 
         jTextFieldCreator.setBackground(new java.awt.Color(255, 255, 255));
         jTextFieldCreator.setEditable(false);
         jTextFieldCreator.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
 
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(jPanelLeft);
         jPanelLeft.setLayout(layout);
         layout.setHorizontalGroup(
 				  layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				  .add(layout.createSequentialGroup()
 				       .addContainerGap()
 				       .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 					    
 					    .add(jPanelCurrentConfig, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
 					    .add(layout.createSequentialGroup()
 						 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 						      .add(jLabelConfig)
 						      .add(jLabelProcess)
 						      .add(jLabelCreated))
 						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 						 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 						      .add(layout.createSequentialGroup()
 							   .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
 								.add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldCreated, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
 								.add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldProcess, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE))
 							   .add(22, 22, 22)
 							   .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 								.add(layout.createSequentialGroup()
 								     .add(jLabelRelease)
 								     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 								     .add(jButtonRelease, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE))
 								.add(layout.createSequentialGroup()
 								     .add(jLabelCreator)
 								     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 								     .add(jTextFieldCreator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE))))
 						      .add(layout.createSequentialGroup()
 							   .add(jTextFieldCurrentConfig, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
 							   .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 							   .add(jLabelLock, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
 						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
 				       .addContainerGap())
 				  );
         layout.setVerticalGroup(
 				layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				.add(layout.createSequentialGroup()
 				     .addContainerGap()
 				     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 					  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 					       .add(jLabelConfig)
 					       .add(jTextFieldCurrentConfig, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 					  .add(jLabelLock, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 				     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 				     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 					  .add(jLabelProcess)
 					  .add(jLabelRelease)
 					  .add(jTextFieldProcess, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 					  .add(jButtonRelease, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 				     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 				     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 					  .add(jLabelCreated)
 					  .add(jTextFieldCreated, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 					  .add(jLabelCreator)
 					  .add(jTextFieldCreator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 				     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 				     .add(jPanelCurrentConfig, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
 				     .addContainerGap())
 				);
 
         layout.linkSize(new java.awt.Component[] {jButtonRelease, jLabelRelease, jTextFieldProcess}, org.jdesktop.layout.GroupLayout.VERTICAL);
         layout.linkSize(new java.awt.Component[] {jLabelLock, jTextFieldCurrentConfig}, org.jdesktop.layout.GroupLayout.VERTICAL);
     }
     
     /** create the Import Configuration part of the configuration panel */
     private void createImportConfigPanel()
     {
 	createImportSearchPopupMenu();
 	jButtonImportCancelSearch.setIcon(new ImageIcon(getClass().
 							getResource("/CancelSearchIcon.png")));
 
         jLabelImportSearch.setText("Search:");
 
         jButtonImportCancelSearch.setEnabled(false);
         jButtonImportCancelSearch.setBorder(null);
 
         jScrollPaneImportConfig.setViewportView(jTreeImportConfig);
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(jPanelImportConfig);
         jPanelImportConfig.setLayout(layout);
         layout.setHorizontalGroup(
 				  layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				  .add(layout.createSequentialGroup()
 				       .add(jLabelImportSearch)
 				       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 				       .add(jTextFieldImportSearch, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
 				       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 				       .add(jButtonImportCancelSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 				  .add(jScrollPaneImportConfig, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
 				  );
         layout.setVerticalGroup(
 				layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				.add(layout.createSequentialGroup()
 				     .addContainerGap()
 				     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
 					  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 					       .add(jLabelImportSearch)
 					       .add(jTextFieldImportSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 					  .add(jButtonImportCancelSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 				     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 				     .add(jScrollPaneImportConfig, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE))
 				);
 
 	layout.linkSize(new java.awt.Component[] {jButtonImportCancelSearch, jTextFieldImportSearch}, org.jdesktop.layout.GroupLayout.VERTICAL);
     }
     
     /** create the 'Configuration' panel (tab1 in left panel) */
     private void createConfigurationPanel()
     {
 	createImportConfigPanel();
 	createSearchPopupMenu();
 	jButtonCancelSearch.
 	    setIcon(new ImageIcon(getClass().
 				  getResource("/CancelSearchIcon.png")));
 	jToggleButtonImport.
 	    setIcon(new ImageIcon(getClass().
 				  getResource("/ImportToggleIcon.png")));
 
 	jButtonCancelSearch.setEnabled(false);
 	jToggleButtonImport.setEnabled(false);
 	
 	jLabelSearch.setText("Search:");
 
         jSplitPaneCurrentConfig.setResizeWeight(0.5);
         jScrollPaneCurrentConfig.setViewportView(jTreeCurrentConfig);
 	
         jSplitPaneCurrentConfig.setLeftComponent(jScrollPaneCurrentConfig);
 	
 	jSplitPaneCurrentConfig.setRightComponent(jPanelImportConfig);
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(jPanelCurrentConfig);
         jPanelCurrentConfig.setLayout(layout);
         layout.setHorizontalGroup(
 				  layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				  .add(layout.createSequentialGroup()
 				       .addContainerGap()
 				       .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 					    .add(jSplitPaneCurrentConfig, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
 					    .add(layout.createSequentialGroup()
 						 .add(jLabelSearch)
 						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 						 .add(jTextFieldSearch, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
 						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 						 .add(jButtonCancelSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 						 .add(63, 63, 63)
 						 .add(jToggleButtonImport, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
 				       .addContainerGap())
 				  );
         layout.setVerticalGroup(
 				layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				.add(layout.createSequentialGroup()
 				     .addContainerGap()
 				     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 					  .add(jToggleButtonImport, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 					  .add(jButtonCancelSearch)
 					  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 					       .add(jLabelSearch)
 					       .add(jTextFieldSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
 				     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 				     .add(jSplitPaneCurrentConfig, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
 				     .addContainerGap())
 				);
 
         layout.linkSize(new java.awt.Component[] {jButtonCancelSearch, jTextFieldSearch, jToggleButtonImport}, org.jdesktop.layout.GroupLayout.VERTICAL);
 
     }
 
     
     /** create the 'Search:' popup menu */
     private void createSearchPopupMenu()
     {
 	buttonGroupSearch1 = new ButtonGroup();
 	buttonGroupSearch2 = new ButtonGroup();
 	
 	JRadioButtonMenuItem rbMenuItem;
 	
 	rbMenuItem = new JRadioButtonMenuItem("startsWith");
 	rbMenuItem.setActionCommand("startsWith");
 	rbMenuItem.setSelected(true);
 	rbMenuItem.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jTreeCurrentConfigUpdateSearch(jTextFieldSearch.getText());
 		}
 	    });
 	buttonGroupSearch1.add(rbMenuItem);
 	jPopupMenuSearch.add(rbMenuItem);
 	rbMenuItem = new JRadioButtonMenuItem("contains");
 	rbMenuItem.setActionCommand("contains");
 	rbMenuItem.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jTreeCurrentConfigUpdateSearch(jTextFieldSearch.getText());
 		}
 	    });
 	buttonGroupSearch1.add(rbMenuItem);
 	jPopupMenuSearch.add(rbMenuItem);
 	jPopupMenuSearch.addSeparator();
 	rbMenuItem = new JRadioButtonMenuItem("labels");
 	rbMenuItem.setActionCommand("matchLabels");
 	rbMenuItem.setSelected(true);
 	rbMenuItem.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jTreeCurrentConfigUpdateSearch(jTextFieldSearch.getText());
 		}
 	    });
 	buttonGroupSearch2.add(rbMenuItem);
 	jPopupMenuSearch.add(rbMenuItem);
 	rbMenuItem = new JRadioButtonMenuItem("plugins");
 	rbMenuItem.setActionCommand("matchPlugins");
 	rbMenuItem.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jTreeCurrentConfigUpdateSearch(jTextFieldSearch.getText());
 		}
 	    });
 	buttonGroupSearch2.add(rbMenuItem);
 	jPopupMenuSearch.add(rbMenuItem);
     }
 
     /** create the 'Search:' popup menu for the importConfig panel */
     private void createImportSearchPopupMenu()
     {
 	buttonGroupImportSearch1 = new ButtonGroup();
 	buttonGroupImportSearch2 = new ButtonGroup();
 	
 	JRadioButtonMenuItem rbMenuItem;
 	
 	rbMenuItem = new JRadioButtonMenuItem("startsWith");
 	rbMenuItem.setActionCommand("startsWith");
 	rbMenuItem.setSelected(true);
 	rbMenuItem.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jTreeImportConfigUpdateSearch(jTextFieldImportSearch.getText());
 		}
 	    });
 	buttonGroupImportSearch1.add(rbMenuItem);
 	jPopupMenuImportSearch.add(rbMenuItem);
 	rbMenuItem = new JRadioButtonMenuItem("contains");
 	rbMenuItem.setActionCommand("contains");
 	rbMenuItem.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jTreeImportConfigUpdateSearch(jTextFieldImportSearch.getText());
 		}
 	    });
 	buttonGroupImportSearch1.add(rbMenuItem);
 	jPopupMenuImportSearch.add(rbMenuItem);
 	jPopupMenuImportSearch.addSeparator();
 	rbMenuItem = new JRadioButtonMenuItem("labels");
 	rbMenuItem.setActionCommand("matchLabels");
 	rbMenuItem.setSelected(true);
 	rbMenuItem.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jTreeImportConfigUpdateSearch(jTextFieldImportSearch.getText());
 		}
 	    });
 	buttonGroupImportSearch2.add(rbMenuItem);
 	jPopupMenuImportSearch.add(rbMenuItem);
 	rbMenuItem = new JRadioButtonMenuItem("plugins");
 	rbMenuItem.setActionCommand("matchPlugins");
 	rbMenuItem.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jTreeImportConfigUpdateSearch(jTextFieldImportSearch.getText());
 		}
 	    });
 	buttonGroupImportSearch2.add(rbMenuItem);
 	jPopupMenuImportSearch.add(rbMenuItem);
     }
 
     
     /** create the right upper panel */
     private void createRightUpperPanel()
     {
         JLabel jLabelPackage = new javax.swing.JLabel();
         JLabel jLabelCVS     = new javax.swing.JLabel();
 
         JLabel jLabelLabel   = new javax.swing.JLabel();
         JLabel jLabelPaths   = new javax.swing.JLabel();
 	
         jSplitPaneRightUpper.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
         jLabelPackage.setFont(new java.awt.Font("Dialog", 0, 12));
         jLabelPackage.setText("Package:");
 
         jTextFieldPackage.setBackground(new java.awt.Color(250, 250, 250));
         jTextFieldPackage.setEditable(false);
         jTextFieldPackage.setFont(new java.awt.Font("Dialog", 0, 10));
         jTextFieldPackage.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
 
         jLabelCVS.setFont(new java.awt.Font("Dialog", 0, 12));
         jLabelCVS.setText("CVS:");
 
         jTextFieldCVS.setBackground(new java.awt.Color(250, 250, 250));
         jTextFieldCVS.setEditable(false);
         jTextFieldCVS.setFont(new java.awt.Font("Dialog", 0, 10));
         jTextFieldCVS.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
 
         jLabelPlugin.setFont(new java.awt.Font("Dialog", 0, 12));
         jLabelPlugin.setText("Plugin:");
 
         jTextFieldPlugin.setBackground(new java.awt.Color(250, 250, 250));
         jTextFieldPlugin.setEditable(false);
         jTextFieldPlugin.setFont(new java.awt.Font("Dialog", 0, 10));
         jTextFieldPlugin.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
 
         jLabelLabel.setText("Label:");
 
         jTextFieldLabel.setBackground(new java.awt.Color(255, 255, 255));
         jTextFieldLabel.setEditable(false);
         jTextFieldLabel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
 
         jLabelPaths.setText("Paths:");
 
 	jComboBoxPaths.setModel(new DefaultComboBoxModel());
         jComboBoxPaths.setBackground(new java.awt.Color(255, 255, 255));
 	
         org.jdesktop.layout.GroupLayout jPanelPluginLayout = new org.jdesktop.layout.GroupLayout(jPanelPlugin);
         jPanelPlugin.setLayout(jPanelPluginLayout);
         jPanelPluginLayout.setHorizontalGroup(
 					      jPanelPluginLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 					      .add(jPanelPluginLayout.createSequentialGroup()
 						   .addContainerGap()
 						   .add(jPanelPluginLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 							.add(jPanelPluginLayout.createSequentialGroup()
 							     .add(jPanelPluginLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
 								  .add(org.jdesktop.layout.GroupLayout.LEADING, jTextFieldLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
 								  .add(jPanelPluginLayout.createSequentialGroup()
 								       .add(jPanelPluginLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 									    .add(jLabelPackage)
 									    .add(jTextFieldPackage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE))
 								       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 								       .add(jPanelPluginLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 									    .add(jLabelCVS)
 									    .add(jTextFieldCVS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
 							     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
 							.add(jPanelPluginLayout.createSequentialGroup()
 							     .add(jLabelLabel)
 							     .add(219, 219, 219)))
 						   .add(jPanelPluginLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 							.add(jComboBoxPaths, 0, 131, Short.MAX_VALUE)
 							.add(jLabelPaths)
 							.add(jTextFieldPlugin, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)
 							.add(jLabelPlugin))
 						   .addContainerGap())
 					      );
         jPanelPluginLayout.setVerticalGroup(
 					    jPanelPluginLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 					    .add(jPanelPluginLayout.createSequentialGroup()
 						 .addContainerGap()
 						 .add(jPanelPluginLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 						      .add(jLabelPackage)
 						      .add(jLabelCVS)
 						      .add(jLabelPlugin))
 						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 						 .add(jPanelPluginLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 						      .add(jTextFieldPackage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 						      .add(jTextFieldCVS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 						      .add(jTextFieldPlugin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 						 .add(jPanelPluginLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 						      .add(jLabelLabel)
 						      .add(jLabelPaths))
 						 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 						 .add(jPanelPluginLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 						      .add(jTextFieldLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 						      .add(jComboBoxPaths, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 						 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 					    );
         jSplitPaneRightUpper.setTopComponent(jPanelPlugin);
 	
 	jScrollPaneParameters.setBackground(new java.awt.Color(255, 255, 255));
         jScrollPaneParameters.setBorder(javax.swing.BorderFactory.createTitledBorder("Parameters"));
 	
         jScrollPaneParameters.setViewportView(jTreeTableParameters);
 	
         jSplitPaneRightUpper.setRightComponent(jScrollPaneParameters);
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(jPanelRightUpper);
         jPanelRightUpper.setLayout(layout);
         layout.setHorizontalGroup(
 				  layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				  .add(jSplitPaneRightUpper, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
 				  );
         layout.setVerticalGroup(
 				layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				.add(jSplitPaneRightUpper, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
 				);
 	
 	jTreeTableParameters.getParent().setBackground(new Color(255,255,255));//PS
     }
     
     /** create the right lower panel */
     private void createRightLowerPanel()
     {
 	jEditorPaneSnippet.setEditable(false);
         jScrollPaneRightLower.setViewportView(jEditorPaneSnippet);
 	
         jTabbedPaneRightLower.addTab("Snippet", jScrollPaneRightLower);
 	
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(jPanelRightLower);
         jPanelRightLower.setLayout(layout);
         layout.setHorizontalGroup(
 				  layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				  .add(jTabbedPaneRightLower, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
 				  );
         layout.setVerticalGroup(
 				layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				.add(jTabbedPaneRightLower, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
 				);
     }
 
 
 
 
     
     /** create event content editor panel */
     private void createContentEditorPanel()
     {
         JPanel      jPanelScrollPane         = new JPanel();
 	JScrollPane jScrollPaneContentEditor = new JScrollPane();
         JScrollPane jScrollPaneStreams       = new JScrollPane();
         JScrollPane jScrollPaneDatasets      = new JScrollPane();
         JScrollPane jScrollPanePaths         = new JScrollPane();
         JScrollPane jScrollPaneOutputModule  = new JScrollPane();
         JScrollPane jScrollPaneCommands      = new JScrollPane();
         JLabel      jLabelEventContent       = new JLabel();
         JLabel      jLabelStreams            = new JLabel();
         JLabel      jLabelDatasets           = new JLabel();
         JLabel      jLabelPaths              = new JLabel();
         JLabel      jLabelCommands           = new JLabel();	
         JLabel      jLabelOutputModule       = new JLabel();
 
 
 	
         jScrollPaneContentEditor.setBorder(javax.swing.BorderFactory.createTitledBorder("Event Content Editor"));
 
         jPanelScrollPane.setPreferredSize(new java.awt.Dimension(400, 600));
 
         jLabelEventContent.setText("Event Content:");
 
         jComboBoxEventContent.setModel(new DefaultComboBoxModel(new String[]{}));
         jComboBoxEventContent.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 jComboBoxEventContentActionPerformed(evt);
             }
         });
 
         jLabelStreams.setText("Streams:");
 
         jListStreams.setModel(new DefaultListModel());
 	jListStreams.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         jListStreams.addListSelectionListener(new ListSelectionListener() {
             public void valueChanged(ListSelectionEvent evt) {
                 jListStreamsValueChanged(evt);
             }
         });
         jScrollPaneStreams.setViewportView(jListStreams);
 
         jListDatasets.setModel(new DefaultListModel());
 	jListDatasets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         jListDatasets.addListSelectionListener(new ListSelectionListener() {
 		public void valueChanged(ListSelectionEvent evt) {
                 jListDatasetsValueChanged(evt);
             }
         });
         jScrollPaneDatasets.setViewportView(jListDatasets);
 
         jLabelDatasets.setText("Primary Datasets:");
 
         jLabelPaths.setText("Paths:");
 
         jListPaths.setModel(new DefaultListModel());
 	jListPaths.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         jListPaths.addListSelectionListener(new ListSelectionListener() {
             public void valueChanged(ListSelectionEvent evt) {
                 jListPathsValueChanged(evt);
             }
         });
         jScrollPanePaths.setViewportView(jListPaths);
 
         jComboBoxCommands.setEditable(true);
         jComboBoxCommands.setModel(new DefaultComboBoxModel(new String[] {}));
         jComboBoxCommands.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 jComboBoxCommandsActionPerformed(evt);
             }
         });
 
         jLabelCommands.setText("Output Commands:");
 
         jLabelOutputModule.setText("OutputModule:");
 
         jTextAreaOutputModule.setColumns(20);
         jTextAreaOutputModule.setEditable(false);
         jTextAreaOutputModule.setRows(5);
         jScrollPaneOutputModule.setViewportView(jTextAreaOutputModule);
 
         jTableCommands.setModel(new CommandTableModel());
         jTableCommands.addMouseListener(new MouseAdapter() {
             public void mousePressed(MouseEvent evt) {
                 jTableCommandsMousePressed(evt);
             }
             public void mouseReleased(MouseEvent evt) {
                 jTableCommandsMouseReleased(evt);
             }
         });
 	jTableCommands.getColumnModel().getColumn(0).setPreferredWidth(30);
 	jTableCommands.getColumnModel().getColumn(1).setPreferredWidth(330);
 	jTableCommands.getColumnModel().getColumn(2).setPreferredWidth(90);
         jScrollPaneCommands.setViewportView(jTableCommands);
 
         org.jdesktop.layout.GroupLayout jPanelScrollPaneLayout = new org.jdesktop.layout.GroupLayout(jPanelScrollPane);
         jPanelScrollPane.setLayout(jPanelScrollPaneLayout);
         jPanelScrollPaneLayout.setHorizontalGroup(
             jPanelScrollPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanelScrollPaneLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanelScrollPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jPanelScrollPaneLayout.createSequentialGroup()
                         .add(jScrollPaneOutputModule, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
                         .addContainerGap())
                     .add(jPanelScrollPaneLayout.createSequentialGroup()
                         .add(jLabelEventContent)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jComboBoxEventContent, 0, 420, Short.MAX_VALUE))
                     .add(jPanelScrollPaneLayout.createSequentialGroup()
                         .add(jPanelScrollPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(jScrollPaneStreams, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
                             .add(jLabelStreams))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jPanelScrollPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(jLabelDatasets)
                             .add(jScrollPaneDatasets, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE))
                         .addContainerGap())
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelScrollPaneLayout.createSequentialGroup()
                         .add(jPanelScrollPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(jLabelPaths)
                             .add(jScrollPanePaths, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jPanelScrollPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(jPanelScrollPaneLayout.createSequentialGroup()
                                 .add(jLabelCommands)
                                 .addContainerGap(224, Short.MAX_VALUE))
                             .add(org.jdesktop.layout.GroupLayout.TRAILING, jComboBoxCommands, 0, 347, Short.MAX_VALUE)
                             .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelScrollPaneLayout.createSequentialGroup()
                                 .add(jScrollPaneCommands, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
                                 .addContainerGap())))
                     .add(jPanelScrollPaneLayout.createSequentialGroup()
                         .add(jLabelOutputModule)
                         .addContainerGap(424, Short.MAX_VALUE))))
         );
         jPanelScrollPaneLayout.setVerticalGroup(
             jPanelScrollPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanelScrollPaneLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanelScrollPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabelEventContent)
                     .add(jComboBoxEventContent, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .add(18, 18, 18)
                 .add(jPanelScrollPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(jLabelDatasets)
                     .add(jLabelStreams))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanelScrollPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                     .add(jScrollPaneDatasets, 0, 0, Short.MAX_VALUE)
                     .add(jScrollPaneStreams, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE))
                 .add(26, 26, 26)
                 .add(jPanelScrollPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabelCommands)
                     .add(jLabelPaths))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanelScrollPaneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(jPanelScrollPaneLayout.createSequentialGroup()
                         .add(jComboBoxCommands, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .add(1, 1, 1)
                         .add(jScrollPaneCommands, 0, 0, Short.MAX_VALUE))
                     .add(jScrollPanePaths, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE))
                 .add(18, 18, 18)
                 .add(jLabelOutputModule)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jScrollPaneOutputModule, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         jScrollPaneContentEditor.setViewportView(jPanelScrollPane);
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(jPanelContentEditor);
         jPanelContentEditor.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jScrollPaneContentEditor, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jScrollPaneContentEditor, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 686, Short.MAX_VALUE)
         );
     }
 
 
     /** create the content pane */
     private void createContentPane()
     {
 	createMenuBar();
 	createToolBar();
 	createDbConnectionPanel();
 	createLeftPanel();
 	createRightUpperPanel();
 	createRightLowerPanel();
 	
 	createContentEditorPanel();
 	
 	jSplitPane.setDividerLocation(0.55);
         jSplitPane.setResizeWeight(0.5);
 	jSplitPaneRight.setDividerLocation(0.5);
 	jSplitPaneRight.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
         jSplitPaneRight.setResizeWeight(0.5);
         jSplitPane.setRightComponent(jSplitPaneRight);
 	
 	jSplitPane.setLeftComponent(jPanelLeft);
 	jSplitPaneRight.setLeftComponent(jPanelRightUpper);
 	jSplitPaneRight.setRightComponent(jPanelRightLower);
 	jProgressBar.setStringPainted(true);
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(jPanelContentPane);
         jPanelContentPane.setLayout(layout);
         layout.setHorizontalGroup(
 				  layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				  .add(jProgressBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1019, Short.MAX_VALUE)
 				  .add(jSplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1019, Short.MAX_VALUE)
 				  .add(jPanelDbConnection, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 				  .add(jToolBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1019, Short.MAX_VALUE)
 				  );
         layout.setVerticalGroup(
 				layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 				.add(layout.createSequentialGroup()
 				     .add(jToolBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 				     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 				     .add(jPanelDbConnection, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 				     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 				     .add(jSplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)
 				     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 				     .add(jProgressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 				);
     }
     
 }
 
 
 
 //
 // CommandTableModel
 //
 class CommandTableModel extends AbstractTableModel
 {
     /** event content */
     private EventContent content = null;
     
     /** selected stream within content */
     private Stream stream = null;
 
     /** selected dataset within stream */
     private PrimaryDataset dataset = null;
 
     /** selected path */
     private Path path = null;
 
     /** construction */
     public CommandTableModel()
     {
 
     }
 
     /** set new event content */
     public void setContent(EventContent content)
     {
 	this.content = content;
 	this.stream = null;
 	this.dataset = null;
 	this.path = null;
 	fireTableDataChanged();
     }
     
     /** set selected stream */
     public void setStream(Stream stream)
     {
 	if (stream==null) {
 	    this.stream = null;
 	    fireTableDataChanged();
 	    return;
 	}
 	
 	if (content==null||content.indexOfStream(stream)<0) {
 	    System.err.println("CommandTableModel.setStream() ERROR: "+
 			       "stream not in currently set content: "+
 			       content+"!");
 	    return;
 	}
 	this.stream = stream;
 	this.dataset = null;
 	this.path = null;
 	fireTableDataChanged();
     }
 
     /** set selected dataset */
     public void setDataset(PrimaryDataset dataset)
     {
 	if (dataset==null) {
 	    this.dataset = null;
 	    fireTableDataChanged();
 	    return;
 	}
 	
 	if (content==null||content.indexOfDataset(dataset)<0) {
 	    System.err.println("CommandTableModel.setDataset() ERROR: "+
 			       "dataset not in currently set content: "+
 			       content+"!");
 	    return;
 	}
 	if (stream!=null&&stream.indexOfDataset(dataset)<0) {
 	    System.err.println("CommandTableModel.setDataset() ERROR: "+
 			       "dataset not in currently set stream: "+
 			       stream+"!");
 	    return;
 	}
 	this.dataset = dataset;
 	this.path = null;
 	fireTableDataChanged();
     }
 
     /** set the selected path */
     public void setPath(Path path)
     {
 	if (path==null) {
 	    this.path = null;
 	    fireTableDataChanged();
 	    return;
 	}
 
 	if (content==null||content.indexOfPath(path)<0) {
 	    System.err.println("CommandTableModel.setPath() ERROR: "+
 			       "path not in currently set content: "+
 			       content+"!");
 	    return;
 	}
 	if (stream!=null&&stream.indexOfPath(path)<0) {
 	    System.err.println("CommandTableModel.setPath() ERROR: "+
 			       "path not in currently set stream: "+
 			       stream+"!");
 	    return;
 	}
 	if (dataset!=null&&dataset.indexOfPath(path)<0) {
 	    System.err.println("CommandTableModel.setPath() ERROR: "+
 			       "path not in currently set dataset: "+
 			       dataset+"!");
 	    return;
 	}
 	this.path = path;
 	fireTableDataChanged();
     }
     
     /** AbstractTableModel: number of columns */
     public int getColumnCount() { return 3; }
     
     /** AbstractTableModel: number of rows */
     public int getRowCount()
     {
 	if      (path!=null)    return content.commandCount(path);
 	else if (dataset!=null) return content.commandCount(dataset);
 	else if (stream!=null)  return content.commandCount(stream);
 	else if (content!=null) return content.commandCount();
 	else return 0;
     }
     
     /** AbstractTableModel: get column names */
     public String getColumnName( int iColumn)
     {
 	if (iColumn==0) return "i";
 	if (iColumn==1) return "Output Command";
 	if (iColumn==2) return "Path";
 	return new String();
     }
 
     /** AbstractTableModel: get value from table cell */
     public Object getValueAt(int iRow,int iColumn)
     {
 	OutputCommand command = null;
 	if      (path!=null)    command = content.command(path,iRow);
 	else if (dataset!=null) command = content.command(dataset,iRow);
 	else if (stream!=null)  command = content.command(stream,iRow);
 	else if (content!=null) command = content.command(iRow);
 	else return new String("ERROR");
 	
 	Path path = command.parentPath();
 
 	if (iColumn==0) return new Integer(content.indexOfCommand(command));
 	if (iColumn==1) return command;
 	if (iColumn==2) return (path==null) ? "<GLOBAL>" : path.toString();
 	return new Object();
     }
     
     /** AbstractTableModel: get class for column index */
     public Class getColumnClass(int iColumn)
     {
 	if      (iColumn==0) return Integer.class;
 	else if (iColumn==1) return OutputCommand.class;
 	else                 return String.class;
 	// return getValueAt(0,iColumn).getClass();
     }
 }
 
