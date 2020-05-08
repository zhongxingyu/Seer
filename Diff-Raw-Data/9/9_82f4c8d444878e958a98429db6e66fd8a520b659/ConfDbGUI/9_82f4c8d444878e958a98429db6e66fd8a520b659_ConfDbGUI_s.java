 package confdb.gui;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.tree.*;
 import javax.swing.table.*;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.border.*;
 import javax.swing.plaf.basic.*;
 
 import org.python.core.PyObject;
 import org.python.util.PythonInterpreter;
 
 import sun.security.pkcs11.Secmod.Module;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import java.sql.DatabaseMetaData;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.concurrent.ExecutionException;
 
 import java.io.FileWriter;
 import java.io.IOException;
 
 import confdb.gui.treetable.*;
 
 import confdb.db.ConfDB;
 import confdb.db.ConfOldDB;
 import confdb.db.DatabaseException;
 import confdb.db.OracleDatabaseConnector;
 
 import confdb.migrator.DatabaseMigrator;
 import confdb.migrator.ReleaseMigrator;
 import confdb.migrator.MigratorException;
 
 import confdb.parser.PythonParser;
 import confdb.parser.ParserException;
 import confdb.parser.JPythonParser;
 import confdb.parser.JParserException;
 
 import confdb.converter.ConverterFactory;
 import confdb.converter.ConverterEngine;
 import confdb.converter.OfflineConverter;
 import confdb.converter.ConverterException;
 
 import confdb.data.*;
 import confdb.diff.*;
 
 
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
 
     // JScrollPane Tabs for Right lower panel:
     private JEditorPane   jEditorPanePathsToDataset = new JEditorPane();
     private JScrollPane   TAB_assignedToDatasets    = new JScrollPane();
     private JEditorPane   jEditorPaneUnresolvedITags= new JEditorPane();
     private JScrollPane   TAB_unresolvedInputTags   = new JScrollPane();
     private JEditorPane   jEditorContainedInPaths	= new JEditorPane();
     private JScrollPane	  TAB_containedInPaths		= new JScrollPane();
     private JEditorPane   jEditorContainedInSequence= new JEditorPane();
     private JScrollPane	  TAB_containedInSequence	= new JScrollPane();
     
     // Path fields in right upper panel
     private JPanel        jPanelPathFields          = new JPanel();
     private JEditorPane   jEditorPathDescription    = new JEditorPane();
     private JEditorPane   jEditorPathContacts	    = new JEditorPane();
     private JScrollPane	  jScrollPanePathContacts	= new JScrollPane();
     private JScrollPane	  jScrollPanePathDescription= new JScrollPane();
     private JButton		  jButtonSavePathFields		= new JButton("Apply");
     private JButton		  jButtonCancelPathFields	= new JButton("Cancel");
     private JTextField    jTextFieldPathName		= new JTextField();
     private JTable     	  jTablePrescales  			= new javax.swing.JTable(); // Prescales for rightUpperPanel.
     private JScrollPane	  jScrollPanePrescales		= new JScrollPane();
    private PrescaleTableService PrescaleTServ		= new PrescaleTableService();
 
     // DB INFO fields:
     public boolean extraPathFieldsAvailability;
     
     // Instrumentation variables:
     private long elapsedTime_OpenConfiguration		= 0;
     
     
     static SimpleAttributeSet ITALIC_GRAY = new SimpleAttributeSet();
     static SimpleAttributeSet BOLD_BLACK = new SimpleAttributeSet();
     static SimpleAttributeSet BLACK = new SimpleAttributeSet(); 
     static {
         StyleConstants.setForeground(ITALIC_GRAY, Color.gray);
         StyleConstants.setItalic(ITALIC_GRAY, true);
         StyleConstants.setFontFamily(ITALIC_GRAY, "Helvetica");
         StyleConstants.setFontSize(ITALIC_GRAY, 12);
 
         StyleConstants.setForeground(BOLD_BLACK, Color.black);
         StyleConstants.setBold(BOLD_BLACK, true);
         StyleConstants.setFontFamily(BOLD_BLACK, "Helvetica");
         StyleConstants.setFontSize(BOLD_BLACK, 12);
 
         StyleConstants.setForeground(BLACK, Color.black);
         StyleConstants.setFontFamily(BLACK, "Helvetica");
         StyleConstants.setFontSize(BLACK, 14);
       }
     
     /** Other program state values. */
     //boolean enablePathCloning = false;	// ToolBar Option to enable the path cloning context menu option.
 
     
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
 	
 	/** Register ActionListener to save extra path fields. */
 	jButtonSavePathFields.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 			SaveDocumentationFieldsActionPerformed();
 			
 		}
 	});
 	
 	/** Register ActionListener to cancel/undo changes in extra path fields. */
 	jButtonCancelPathFields.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    // It will be automatically enabled when Text is modified.
 		    jButtonSavePathFields.setEnabled(false); 
 		    jButtonCancelPathFields.setEnabled(false);
 		    
 		    // Reload values
 		    displayPathFields();
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
 	
 	// CHECH ConfdbSofware version:
 	ConfdbSoftwareVersion softwareVersion = new ConfdbSoftwareVersion();
 	softwareVersion.CheckSoftwareVersion();	// Against the container version!
 	
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
 
     /** parse a configuration from a *.py file */
     public void jparseConfiguration()
     {
 	if (!closeConfiguration()) return;
 	
 	JParseConfigurationDialog dialog =
 	    new JParseConfigurationDialog(frame,database);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	if (dialog.validChoice()) {
 	    String fileName   = dialog.fileName();
 	    String releaseTag = dialog.releaseTag();
 	    boolean compiledFile 	=  dialog.compiledFile();
 	    boolean ignorePrescales = dialog.ignorePrescaleService();
 	    
 	    JParseConfigurationThread worker =
 		new JParseConfigurationThread(fileName,releaseTag, compiledFile, ignorePrescales);
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
 	    OpenConfigurationThread worker = new OpenConfigurationThread(dialog.configInfo());
 	    
 	    worker.start();	    
 	    
 	    jProgressBar.setIndeterminate(true);
 	    jProgressBar.setVisible(true);
 	    jProgressBar.setString("Loading Configuration ...");
 	    menuBar.configurationIsOpen();
 	    toolBar.configurationIsOpen();
 	    
 	    //System.out.println("ElapsedTime: " + worker.getElapsedTime());
 	    
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
 
 	if (dialog.getApply()) {
 	    Diff diff = dialog.getDiff();
 	    String oldConfigName = diff.configName1();
 	    String newConfigName = diff.configName2();
 	    // System.out.println("ConfDbGUI.diffConfiguration: old:"+oldConfigName+" new:"+newConfigName);
 	    
 	    closeConfiguration(false);
 
 	    //openConfiguration();
 	    if (database.dbUrl().equals(new String())) return;
 	    if (!closeConfiguration()) return;
 	    
 	    PickConfigurationDialog cdialog =
 		new PickConfigurationDialog(frame,"Open Configuration to be updated",database);
 	    cdialog.allowUnlocking();
 	    cdialog.pack();
 	    cdialog.setLocationRelativeTo(frame);
 	    cdialog.setVisible(true);
 	    
 	    if (cdialog.validChoice()) { } else {return;}
 
 	    OpenConfigurationThread cworker =
 		new OpenConfigurationThread(cdialog.configInfo());
 	    cworker.start();
 	    jProgressBar.setIndeterminate(true);
 	    jProgressBar.setVisible(true);
 	    jProgressBar.setString("Loading Configuration to be updated...");
 	    menuBar.configurationIsOpen();
 	    toolBar.configurationIsOpen();
 
 	    try {
 		cworker.get();
 	    } catch ( InterruptedException e) {
 		System.err.println("ConfDbGUI.diffConfiguration: cI: "+e.getMessage());
 	    } catch ( ExecutionException e) {
 		System.err.println("ConfDbGUI.diffConfiguration: cE: "+e.getMessage());
 	    }
 
 	    // System.out.println("ConfDbGUI.diffConfiguration: current: "+currentConfig.toString());
 
 	    //importConfiguration();
 	    PickConfigurationDialog idialog =
 		new PickConfigurationDialog(frame,"Open Configuration containing updates",database);
 	    idialog.fixReleaseTag(currentRelease.releaseTag());
 	    idialog.pack();
 	    idialog.setLocationRelativeTo(frame);
 	    idialog.setVisible(true);
 	    
 	    if (idialog.validChoice()&&
 		idialog.configInfo().releaseTag().equals(currentRelease.releaseTag())) {
 	    } else {return;}
 
 	    ImportConfigurationThread iworker =
 		new ImportConfigurationThread(idialog.configInfo());
 	    iworker.start();
 	    jProgressBar.setIndeterminate(true);
 	    jProgressBar.setVisible(true);
 	    jProgressBar.setString("Loading Configuration containing updates...");
 
 	    try {
 		iworker.get();
 	    } catch ( InterruptedException e) {
 		System.err.println("ConfDbGUI.diffConfiguration: iI: "+e.getMessage());
 	    } catch ( ExecutionException e) {
 		System.err.println("ConfDbGUI.diffConfiguration: iE: "+e.getMessage());
 	    }
 
 	    // System.out.println("ConfDbGUI.diffConfiguration: import : "+importConfig.toString());
 	    
 	    //
 	    boolean  result = false;
 	    String pathName = null;
 	    ReferenceContainer  external       = null;
 	    ContainerComparison pathComparison = null;
 	    //
 	    // System.out.println("ConfDbGUI.diffConfiguration: # of diffs: "+diff.pathCount());
 	    for (int i=0; i<diff.pathCount(); i++) {
 		pathComparison = (ContainerComparison)diff.path(i);
 		// System.out.println("ConfDbGUI.diffConfiguration: "+i+" "+pathComparison.resultAsString());
 		if (pathComparison.result()==Comparison.RESULT_CHANGED || pathComparison.result()==Comparison.RESULT_ADDED) {
 		    pathName = pathComparison.newContainer().name();
 		    // System.out.println("ConfDbGUI.diffConfiguration: path to update/add:"+pathName);
 		    external = importConfig.path(pathName);
 		    if (external==null) {
 			// System.out.println("ConfDbGUI.diffConfiguration: path not found in importConfig - skip!");
 			// } else if ( ((Path)external).isEndPath() ) {
 			// System.out.println("ConfDbGUI.diffConfiguration: path is endpath - skip!");
 		    } else {
 			/*
 			ReferenceContainer internal = currentConfig.path(external.name());
 			if (internal==null) {
 			    // System.out.println("ConfDbGUI.diffConfiguration: path not found in currentConfig - add!");
 			    internal = currentConfig.insertPath(currentConfig.pathCount(),external.name());
 			    ((Path)internal).setAsEndPath(((Path)external).isSetAsEndPath());
 			}
 			// System.out.println("ConfDbGUI.diffConfiguration: start "+pathName+" "+external.name()+" "+internal.name());
 			// result = ConfigurationTreeActions.DeepImportContainerEntriesSimulation(currentConfig, importConfig, external, internal);
 			*/
 			// System.out.println("ConfDbGUI.diffConfiguration: start "+pathName+" "+external.name());
 			result = ConfigurationTreeActions.DeepImportReferenceContainer(jTreeCurrentConfig,importConfig,external);
 			if (!result) System.out.println("ConfDbGUI.diffConfiguration: path not updated: "+pathName);
 		    }
 		} else {
 		    pathName = pathComparison.oldContainer().name();
 		    System.out.println("ConfDbGUI.diffConfiguration: path not removed: "+pathName);
 		}
 	    }
 	    //
 	}
     }
     
     /** compare current configuration to another one */
     public void smartVersionsConfigurations()
     {
 	// make a diff to previously deplyed version to find all changed paths
 	DiffDialog dialog = new DiffDialog(frame,database);
 	dialog.setTitle("List of changed paths from Diff");
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	if (!currentConfig.isEmpty()) {
 	    dialog.setNewConfig(currentConfig);
 	    dialog.setOldConfigs(currentConfig.configInfo());
 	}
 	dialog.setVisible(true);
 
 	// look at paths which have changed (not added nor removed)
 	Diff diff = dialog.getDiff();
 	Path      path = null;
 	String oldName = null;
 	String newName = null;
 	int index   = 0;
 	int oldVersion = 0;
 	int newVersion = 0;
 	String number = null;
 	for (int i=0; i<diff.pathCount(); i++) {
 	    ContainerComparison pathComparison = (ContainerComparison)diff.path(i);
 	    if (pathComparison.result()==Comparison.RESULT_CHANGED) {
 		path = (Path)pathComparison.newContainer();
 
 		// no re-versioning of endpaths
 		if (path.isSetAsEndPath()) break;
 
 		oldName = path.name();
 		index   = oldName.lastIndexOf("_v");
 		// re-version only versioned paths
 		if (index>=0) {
 		    number = oldName.substring(index+2);
 		    if (number.equals("")) {
 			oldVersion=0;
 		    } else {
 			oldVersion=Integer.decode(number);
 		    }
 		    newVersion=oldVersion;
 		    do {
 			newVersion++;
 			newName = oldName.substring(0,index+2)+String.valueOf(newVersion);
 		    } while (!currentConfig.isUniqueQualifier(newName));
 		    try {
 			path = currentConfig.path(oldName);
 			path.setNameAndPropagate(newName);
 			treeModelCurrentConfig.nodeChanged(path);
 			System.out.println("SmartVersions: +"+(newVersion-oldVersion)+" "+newName);
 		    }
 		    catch (DataException e) {
 			System.err.println(e.getMessage());
 		    }
 		}
 	    }
 	}
     }
     
     /** compare current configuration to another one */
     public void smartRenamingConfigurations()
     {
 	SmartRenameDialog dialog = new SmartRenameDialog(frame,currentConfig);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	if (dialog.validChoice()) {
 	    String filPattern = dialog.filPattern();
 	    if (filPattern.equals("")) filPattern="*";
 	    filPattern=filPattern.replace("*",".*").replace("?",".");
 	    String oldPattern = dialog.oldPattern();
 	    String newPattern = dialog.newPattern();
 	    System.out.println("SmartRenamingConfigurations "+filPattern+" "+oldPattern+" "+newPattern);
 	    String oldName = null;
 	    String newName = null;
 
 	    String applyTo = dialog.applyTo();
 	    System.out.println("SmartRenaming applyTo: "+applyTo);
 
 	    if (applyTo.equals("All") || applyTo.equals("Modules")) {
 		ModuleInstance module = null;
 		for (int i=0; i<currentConfig.moduleCount(); i++) {
 		    module = currentConfig.module(i);
 		    oldName = module.name();
 		    if (oldName.matches(filPattern)) {
 			newName = oldName.replace(oldPattern,newPattern);
 			if (!oldName.equals(newName)) {
 			    if (currentConfig.isUniqueQualifier(newName)) {
 				System.out.println("SmartRenaming Module: "+newName+" ["+oldName+"]");
 				try {
 				    module.setNameAndPropagate(newName);
 				    treeModelCurrentConfig.nodeChanged(module);
 				}
 				catch (DataException e) {
 				    System.err.println(e.getMessage());
 				}
 			    } else {
 				System.out.println("SmartRenaming Module: "+newName+" ["+oldName+"] not changed: new name already exists!");
 			    }
 			}
 		    }
 		}
 	    }
 	    
 	    if (applyTo.equals("All") || applyTo.equals("Sequences")) {
 		Sequence sequence = null;
 		for (int i=0; i<currentConfig.sequenceCount(); i++) {
 		    sequence = currentConfig.sequence(i);
 		    oldName = sequence.name();
 		    if (oldName.matches(filPattern)) {
 			newName = oldName.replace(oldPattern,newPattern);
 			if (!oldName.equals(newName)) {
 			    if (currentConfig.isUniqueQualifier(newName)) {
 				System.out.println("SmartRenaming Sequence: "+newName+" ["+oldName+"]");
 				try {
 				    sequence.setName(newName);
 				    treeModelCurrentConfig.nodeChanged(sequence);
 				}
 				catch (DataException e) {
 				    System.err.println(e.getMessage());
 				}
 			    } else {
 				System.out.println("SmartRenaming Sequence: "+newName+" ["+oldName+"] not changed: new name already exists!");
 			    }
 			}
 		    }
 		}
 	    }
 
 	    if (applyTo.equals("All") || applyTo.equals("Paths")) {
 		Path path = null;
 		for (int i=0; i<currentConfig.pathCount(); i++) {
 		    path = currentConfig.path(i);
 		    oldName = path.name();
 		    if (oldName.matches(filPattern)) {
 			newName = oldName.replace(oldPattern,newPattern);
 			if (!oldName.equals(newName)) {
 			    if (currentConfig.isUniqueQualifier(newName)) {
 				System.out.println("SmartRenaming Path: "+newName+" ["+oldName+"]");
 				try {
 				    path.setNameAndPropagate(newName);
 				    treeModelCurrentConfig.nodeChanged(path);
 				}
 				catch (DataException e) {
 				    System.err.println(e.getMessage());
 				}
 			    } else {
 				System.out.println("SmartRenaming Path: "+newName+" ["+oldName+"] not changed: new name already exists!");
 			    }
 			}
 		    }
 		}
 	    }
 
 	    treeModelCurrentConfig.setConfiguration(currentConfig);
 	}
     }
     
     /** open prescale editor */
     public void openPrescaleEditor()
     {
     	// NOTE: clearPathFields() is necessary to do not interfere with 
     	// the embedded editor in the rightUpperPanel (documentation panel):
     	clearPathFields();  
     	
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
 
 	Path[] children = new Path[config.pathCount()];	
 	for (int i=0; i<config.pathCount(); i++) {
 	    children[i]=(Path)treeModelCurrentConfig.getChild(treeModelCurrentConfig.pathsNode(),i);
 	    
 	    Path newParent = children[i];
 	    int newIndices[] = new int[newParent.entryCount()];
 		for (int j=0; j<newParent.entryCount(); j++) {
 		newIndices[j]=j;
 	    }
 	    treeModelCurrentConfig.childNodesChanged(newParent,newIndices);
 	}
 	
     }
     
     /** Set option 'Enable path cloning' */
     public void setEnablePathCloning(boolean enableCloning) {
     	
     	MouseListener[] mls = (MouseListener[])(jTreeCurrentConfig.getListeners(MouseListener.class));
     	
 		for(int i = 0; i < mls.length; i++) {
 			if(mls[i].getClass() == ConfigurationTreeMouseListener.class) {
 				((ConfigurationTreeMouseListener)mls[i]).setEnablePathClonig(enableCloning);
 			}
 		}
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
 
 		// Use TNSNames format to connect to oracle:
 		if (dbType.equals(database.dbTypeOracle)) {
 			dbUrl = database.setDbParameters(dbPwrd, dbName, dbHost, dbPort); 
 		}
 			
 		
 		
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
 	
 		
 		extraPathFieldsAvailability = database.getExtraPathFieldsAvailability();
 	
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
     
     /** Show Db information */
     // TODO show instrumentation results.
     public void showDBInfo() {
 		String getDatabaseProductVersion = "";
 		String getDriverName = "";
 		String getDriverVersion = "";
     	 JFrame		infoFrame;
     	 JPanel		infoPanel = new JPanel();
     	 JTextArea 	Output		;
     	 infoFrame = new JFrame("Database Info");
     	 infoFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
     	 infoFrame.setResizable(false);
     	 
 	 		try {
 	 			DatabaseMetaData dbmd = database.getDatabaseMetaData();
 	 			getDatabaseProductVersion = dbmd.getDatabaseProductVersion();
 	 			getDriverName = dbmd.getDriverName();
 	 			getDriverVersion = dbmd.getDriverVersion();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			
     	    
     	    JTextArea jlabelExtraPathsAvailability	= new JTextArea();
     	    jlabelExtraPathsAvailability.setText("Documentation fields availability:");
     	    jlabelExtraPathsAvailability.setEditable(false);
     	    jlabelExtraPathsAvailability.setBackground(null);
     	    
     	    JTextArea DatabaseHost	= new JTextArea("db Host: ");
     	    DatabaseHost.setEditable(false);
     	    DatabaseHost.setBackground(null);
     	    
     	    JTextArea DatabaseHostValue	= new JTextArea(database.getHostName());
     	    DatabaseHostValue.setEditable(false);
     	    DatabaseHostValue.setBackground(null);
     	    
     	    JTextArea DatabaseName	= new JTextArea("db Name: ");
     	    DatabaseName.setEditable(false);
     	    DatabaseName.setBackground(null);
     	    
     	    JTextArea DatabaseNameValue	= new JTextArea(database.getDbName());
     	    DatabaseNameValue.setEditable(false);
     	    DatabaseNameValue.setBackground(null);
     	    
     	    JTextArea DatabaseProductVersion	= new JTextArea("DatabaseProductVersion: ");
     	    DatabaseProductVersion.setEditable(false);
     	    DatabaseProductVersion.setBackground(null);
     	    JTextArea DatabaseProductVersionValue	= new JTextArea(getDatabaseProductVersion);
     	    DatabaseProductVersionValue.setEditable(false);
     	    DatabaseProductVersionValue.setBackground(null);
     	    
     	    JTextArea DriverName	= new JTextArea("DriverName: ");
     	    DriverName.setEditable(false);
     	    DriverName.setBackground(null);
     	    
     	    JTextArea DriverNameValue	= new JTextArea(getDriverName);
     	    DriverNameValue.setEditable(false);
     	    DriverNameValue.setBackground(null);
     	    
     	    JTextArea DriverVersion	= new JTextArea("DriverVersion: ");
     	    DriverVersion.setEditable(false);
     	    DriverVersion.setBackground(null);
     	    
     	    JTextArea DriverVersionValue	= new JTextArea(getDriverVersion);
     	    DriverVersionValue.setEditable(false);
     	    DriverVersionValue.setBackground(null);
     	    
     	    JTextArea elapsedTimeOpenConfiguration	= new JTextArea("Elapsed time to open last configuration: ");
     	    elapsedTimeOpenConfiguration.setEditable(false);
     	    elapsedTimeOpenConfiguration.setBackground(null);
     	    
     	    JTextArea elapsedTimeOpenConfigurationValue	= new JTextArea( elapsedTime_OpenConfiguration + " milliseconds.");
     	    elapsedTimeOpenConfigurationValue.setEditable(false);
     	    elapsedTimeOpenConfigurationValue.setBackground(null);
     	    
     	    
     	    JLabel ico1 = new JLabel();	// icon
     	    JLabel ico2 = new JLabel();	// icon
     	    if(extraPathFieldsAvailability) ico1.setIcon(new ImageIcon(getClass().getResource("/ESSourcesDirIcon.png")));
     	    else 							ico1.setIcon(new ImageIcon(getClass().getResource("/ModulesDirIcon.png")));
     	    
     	    
     	    
     	    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(infoPanel);
     	    infoPanel.setLayout(layout);
     	    infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
     	    
     	 // Using TRAILING alignment the button will be aligned to the right.
     	    layout.setHorizontalGroup(layout.createSequentialGroup()
     	    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
     	    		.addComponent(DatabaseProductVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 260, Short.MAX_VALUE)
     	    		.addComponent(DriverName, javax.swing.GroupLayout.PREFERRED_SIZE, 260, Short.MAX_VALUE)
     	    		.addComponent(DriverVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 260, Short.MAX_VALUE)
     	    		.addComponent(DatabaseHost, javax.swing.GroupLayout.PREFERRED_SIZE, 260, Short.MAX_VALUE)
     	    		.addComponent(DatabaseName, javax.swing.GroupLayout.PREFERRED_SIZE, 260, Short.MAX_VALUE)
     	    		.addComponent(elapsedTimeOpenConfiguration, javax.swing.GroupLayout.PREFERRED_SIZE, 260, Short.MAX_VALUE)
     	    		
     	    		.addComponent(jlabelExtraPathsAvailability, javax.swing.GroupLayout.PREFERRED_SIZE, 260, Short.MAX_VALUE))
     	    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 		    		.addComponent(DatabaseProductVersionValue, javax.swing.GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 		    		.addComponent(DriverNameValue, javax.swing.GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 		    		.addComponent(DriverVersionValue, javax.swing.GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 		    		.addComponent(DatabaseHostValue, javax.swing.GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 		    		.addComponent(DatabaseNameValue, javax.swing.GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 		    		.addComponent(elapsedTimeOpenConfigurationValue, javax.swing.GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 		    		
     	    		.addComponent(ico1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, 25))
     	    );
     	    
     	    layout.setVerticalGroup(layout.createSequentialGroup()
         	    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)	
         	    		.addComponent(DatabaseProductVersion, javax.swing.GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, 100)
         	    		.addComponent(DatabaseProductVersionValue, javax.swing.GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, 100))
         	    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)	
         	    		.addComponent(DriverName, javax.swing.GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, 100)
         	    		.addComponent(DriverNameValue, javax.swing.GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, 100))
         	    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)	
         	    		.addComponent(DriverVersion, javax.swing.GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, 100)
         	    		.addComponent(DriverVersionValue, javax.swing.GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, 100))
         	    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
         	    		.addComponent(DatabaseHost, javax.swing.GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, 100)
         	    		.addComponent(DatabaseHostValue, javax.swing.GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, 100))
         	    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
         	    		.addComponent(DatabaseName, javax.swing.GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, 100)
         	    		.addComponent(DatabaseNameValue, javax.swing.GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, 100))
         	    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
         	    		.addComponent(elapsedTimeOpenConfiguration, javax.swing.GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, 100)
         	    		.addComponent(elapsedTimeOpenConfigurationValue, javax.swing.GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, 100))
 	    	    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
 	    	    		.addComponent(jlabelExtraPathsAvailability, javax.swing.GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, 25)
 	    	    		.addComponent(ico1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, 25))
     	    );
 
         
         // Create and set up the content pane.
     	infoPanel.setOpaque(true);
         infoFrame.setContentPane(infoPanel);
 
         // Display the window.
         infoFrame.pack();
         infoFrame.setVisible(true);
         
         // Setting the position of the dialog on the center of the screen
         Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
         infoFrame.setLocation((int)d.getWidth()/2 - (int)infoFrame.getPreferredSize().getWidth()/2,
                 (int)d.getHeight()/2 - (int)infoFrame.getPreferredSize().getHeight()/2);	
     }
     
     
     
     /** Show import tool window to import a Python file into the database 
      * bug/feature  #76151 	
      * */
     public void importFromPythonToolDialog() {
     	jparseConfiguration();
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
 	clearPathFields(); 
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
 	
	// Set current configuration to the prescaleService
	PrescaleTServ = new PrescaleTableService(currentConfig);
	
     }
     
     /** Time spent in opening a configuration. */
     private void setElapsedTime(long time) {
     	elapsedTime_OpenConfiguration = time;
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
     
 
     /** load release templates from the database and parse config from *.py */
     private class JParseConfigurationThread extends SwingWorker<String>
     {
 	/** member data */
 	private JPythonParser parser     = null;
 	private String        fileName   = null;
 	private String        releaseTag = null;
 	private boolean 	  compiledFile 		= false;
 	private boolean 	  ignorePrescales	= false;
 	private long          startTime;
 	
 	/** standard constructor */
 	public JParseConfigurationThread(String fileName,String releaseTag, boolean compiledFile, boolean ignorePrescales)
 	{
 	    this.fileName   = fileName;
 	    this.releaseTag = releaseTag;
 	    this.compiledFile 		= compiledFile;
 	    this.ignorePrescales 	= ignorePrescales;
 	}
 	
 	/** SwingWorker: construct() */
 	protected String construct() throws DatabaseException,JParserException
 	{
 	    startTime = System.currentTimeMillis();
 	    
 	    if (!releaseTag.equals(currentRelease.releaseTag())) 
 	    	database.loadSoftwareRelease(releaseTag,currentRelease);
 	    
 	    parser = new JPythonParser(currentRelease);
 	    
 	    if(compiledFile) parser.parseCompileFile(fileName);
 	    else parser.parseFileBatchMode(fileName, ignorePrescales);
 	    
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
 
 	    if(parser == null) {
 			// Add the configuration details:
 	    	AboutDialog ad = new AboutDialog(null); // Only to get version and contact info. //DONT SHOW DIALOG!
 			String StackTrace = "ConfDb Version: " 	+ ad.getConfDbVersion() 	+ "\n";
 			StackTrace+= "Release Tag: " 			+ currentRelease.releaseTag() 		+ "\n";
 			StackTrace+= "-----------------------------------------------------------------\n";			
 	    	String errMsg = "Parse Python configuration FAILED!\n"	+
 			"Please send us an email to:\n" + ad.getContactPerson();
 			
 	    	errorNotificationPanel cd = new errorNotificationPanel("ERROR", errMsg, StackTrace);
 			cd.createAndShowGUI();
 	    } else { //  if (parser.closeProblemStream()) {
 			//System.err.println("problems encountered, see problems.txt.");
 			JParserProblemsDialog dialog=new JParserProblemsDialog(frame, parser);
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
 	private long 	   elapsedTime;
 	
 	/** standard constructor */
 	public OpenConfigurationThread(ConfigInfo configInfo)
 	{
 	    this.configInfo = configInfo;
 	    elapsedTime = 0;
 	    startTime = 0;
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
 		boolean failed = false;
 	    try {
 		elapsedTime = System.currentTimeMillis() - startTime;
 		jProgressBar.setString(jProgressBar.getString() + get() + " (" + elapsedTime + " ms)");
 		
 		setElapsedTime(elapsedTime);
 	    }
 	    catch (ExecutionException e) {
 	    	//String errMsg = "Open Configuration FAILED:\n"+e.getCause().getMessage();
 	    	failed = true;
 	    	System.out.println("ERROR: [confdb.gui.ConfDbGUI.OpenConfigurationThread] " + e.getCause());
 
 		jProgressBar.setString(jProgressBar.getString()+"FAILED!");
 		//e.printStackTrace();
 		
 		AboutDialog ad = new AboutDialog(null); // Only to get version and contact info. //DONT SHOW DIALOG!
 		// Add the configuration details:
 		String StackTrace = "ConfDb Version: " 	+ ad.getConfDbVersion() 	+ "\n";
 		StackTrace+= "Release Tag: " 			+ configInfo.releaseTag() 			+ "\n";
 		StackTrace+= "Configuration: " 			+ configInfo.fullName() 			+ "\n";
 		StackTrace+= "-----------------------------------------------------------------\n";
 		// get the Stack Trace in one String.
 		StackTraceElement st[] = e.getStackTrace();
 		for(int i = 0; i < st.length; i++) StackTrace+=st[i] + "\n";
 		
     	String errMsg = "Open Configuration FAILED!\n"	+
 		"This configuration might be broken, working with it " + 
 		"may cause serious problems in the future.\n" + 
 		"It is highly recommended to save a copy of it in a different area.\n" + 
     	"If you have experienced any problem and you need to recover " + 
     	"a broken configuration, please send us an email to:\n" + ad.getContactPerson();
 		
     	errorNotificationPanel cd = new errorNotificationPanel("ERROR", errMsg, StackTrace);
 		cd.createAndShowGUI();
 		
 		System.out.println("ERROR: [confdb.gui.ConfDbGUI.OpenConfigurationThread] ");
 		e.printStackTrace();
 		
 	    } catch (Exception e) {
 			e.printStackTrace();
 			jProgressBar.setString(jProgressBar.getString()+"FAILED!");
 	    }
 	    
 	    jProgressBar.setIndeterminate(false);
 
 	    if(!failed) {
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
 			 } else {
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
 	
 	/* Instrumenting this task. */
 	public long getElapsedTime() { return elapsedTime; }
 	
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
 	    
 	    // fix bug76148
 	    // if (importRelease.releaseTag()!=currentRelease.releaseTag())
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
 		targetConfig.setConfigInfo(currentConfig.configInfo());
 		targetConfig.setReleaseTag(currentConfig.releaseTag());
 		setCurrentConfig(targetConfig);
 		currentConfig.setHasChanged(true);
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
 	
 		    	// Do not display neither "unresolved input tags" nor "datasets" for Paths. bug/feature 82524 
 			    if (	selectedNode instanceof ESSourceInstance	||
 						     selectedNode instanceof ESModuleInstance	||
 						     selectedNode instanceof ModuleInstance) 	{
 					Instance instance = (Instance)selectedNode;
 					text = instance.template().name();
 			    } else if (selectedNode instanceof ModuleReference) 	{
 					ModuleReference reference=(ModuleReference)selectedNode;
 					ModuleInstance  instance=(ModuleInstance)reference.parent();
 					text = "<html>"+instance.template().name();
 		
 					Object component = (tp.getPathComponent(2));
 					if(component instanceof Path) {
 						Path path = (Path)(tp.getPathComponent(2));
 						String[] unresolved = path.unresolvedInputTags();
 						if (unresolved.length>0) text+="<br>Unresolved InputTags out of the "+unresolved.length+" in the current path:";
 						for (String un : unresolved) {
 						    String[] tokens = un.split("[/:]");
 						    for (int i=0; i<tokens.length; i++) {
 							if (instance.name().equals(tokens[i])) {
 							    text += "<br>"+un;
 							    break;
 							}
 						    }
 						}
 					}
 					text +="<html>";
 			    } else if (selectedNode instanceof SequenceReference) {
 			    	// Do not display "unresolved input tags" for Sequences. bug/feature 82524
 			    	
 					SequenceReference reference=(SequenceReference)selectedNode;
 					Sequence instance=(Sequence)reference.parent();
 					text = "<html>"+instance.name();
 					text += "<html>";
 					
 			    } else if (selectedNode instanceof Stream) {
 					Stream stream = (Stream)selectedNode;
 					text = "Event Content: " + stream.parentContent().name();
 			    } else if (selectedNode instanceof PrimaryDataset) {
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
     	new ImportTreeMouseListener(jTreeImportConfig,jTreeCurrentConfig, importConfig);
  
     
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
 	treeModelParameters  = new ParameterTreeModel(currentConfig);
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
 	
 	// Linking jTreeTableParameters to ConfigurationTreeMouseListener Listener - bug 75952
 	mouseListener.setTreeTable(jTreeTableParameters); // set the TreeTable to stop editing.
     }
     
     
     
     
     /** return a text list with assigned datasets to the current path. */
 	public String getAssignedDatasets() {
 	    String text = "";
 	    Path path;
 	    if (currentParameterContainer instanceof Path) {
 		    path = (Path)currentParameterContainer;
 	    } else return "";
 
 		if (path.datasetCount()>0) {
 		    Iterator<PrimaryDataset> itPD = path.datasetIterator();
 		    while (itPD.hasNext())
 					text += itPD.next().name() + "\n";
 		}
 		return text;
 	}
 	
     /** return a text list of Paths which contains the current Module/Sequence. */
 	public String getAssignedPaths() {
 	    String text = "";
 	    ModuleInstance moduleInstance	= null;
 	    Sequence	sequence	= null;
 	    Path[] paths = null;
 	    
 	    if (currentParameterContainer instanceof ModuleInstance) {
 		    moduleInstance = (ModuleInstance)currentParameterContainer;
 		    paths = moduleInstance.parentPaths();
 	    } else if (currentParameterContainer instanceof Sequence) {
 	    	sequence = (Sequence) currentParameterContainer;
 	    	paths = sequence.parentPaths();
 	    	
 	    } else return "";
 	    
 	    if(paths != null)
 	    for(int i = 0; i < paths.length; i++) {
 	    	text+= "<a href='"+paths[i]+"'>" + paths[i] + "</a> <br>";
 	    }
 	    
 		return text;
 	}
 	
 	
 	/**
 	 * return a html string format with a list of Sequences containing the
 	 * current parameter container.
 	 * Used to fill ContainedInSequence tab. (bug 88620).*/
 	public String getAssignedSequences() {
 	    String text = "";
 	    ModuleInstance moduleInstance	= null;
 	    Sequence	sequence	= null;
 	    if (currentParameterContainer instanceof ModuleInstance) {
 		    moduleInstance = (ModuleInstance)currentParameterContainer;
 		    
 		    Iterator<Sequence> SeqIt = currentConfig.sequenceIterator();
 		    while(SeqIt.hasNext()) {
 		    	Sequence Seq = SeqIt.next();
 		    	Reference ref = Seq.entry(moduleInstance.name());
 		    	if(ref != null) {
 			    	text+= "<a href='"+Seq.name()+"'>" + Seq.name() + "</a> <br>";
 		    	}
 		    }
 	    } else if (currentParameterContainer instanceof Sequence) {
 	    	sequence = (Sequence) currentParameterContainer;
 	    	
 		    Iterator<Sequence> SeqIt = currentConfig.sequenceIterator();
 		    while(SeqIt.hasNext()) {
 		    	Sequence Seq = SeqIt.next();
 		    	Reference ref = Seq.entry(sequence.name());
 		    	if(ref != null) {
 			    	text+= "<a href='"+Seq.name()+"'>" + Seq.name() + "</a> <br>";
 		    	}
 		    }
 	    	
 	    	
 	    } else return "";
 	    return text;
 	}
 	
 	
 	/** Prepare a summary of unassigned input tags using the original 	*/
 	/** python code. This uses links to expand the tree and show the	*/
 	/** selected Module													*/
 	/** NOTE: The string returned is not the original python code. 		*/
 	/** It only contains the relevant InputTags	in a familiar format	*/
 	public String getUnresolvedInputTagsSummary() {
 	    String text = "";
 	    String module 	= null;
 	    String tag		= null;
 	    Path path;
 	    Sequence sequence;
 	    String[] unresolved;
 	    if (currentParameterContainer instanceof Path) {
 		    path = (Path)currentParameterContainer;
 		    unresolved = path.unresolvedInputTags(); // return duplicated modules.
 	    } else if(currentParameterContainer instanceof Sequence){
 	    	sequence = (Sequence) currentParameterContainer;
 	    	unresolved = sequence.unresolvedInputTags();
 	    } else return "ERROR: getUnresolvedInputTagsSummary(): unknown currentParameterContainer";
 	    
 		
 		String[] modules	= new String [unresolved.length]; // as maximum.
 		int MLength			= 0;
 		
 		if (unresolved.length>0) {
 			jEditorPaneUnresolvedITags.setText("");
 			
 			// Coalesce duplicated modules.
 			for (int i=0;i<unresolved.length;i++) {
 				if(i==0)	{
 					modules[i] = this.getModuleFromUnresolvedInputTag(unresolved[i]);
 					MLength++;
 				} else {
 					boolean found = false;
 					for (int j=0;j<MLength;j++) {
 						if((modules[j].compareTo(this.getModuleFromUnresolvedInputTag(unresolved[i])))==0) found = true;
 					}
 					if(!found) {
 						modules[MLength] = this.getModuleFromUnresolvedInputTag(unresolved[i]);
 						MLength++;
 					}
 				}
 			}
 			
 			// link and sort various inputTags according to modules
 			String pythonCode;
 			String [] sortedTags= new String[unresolved.length];
 			int Ntags;
 			for (int i=0; i < MLength; i++) {
 				pythonCode = getPythonCodeForModule(modules[i]);
 				Ntags	= 0;
 				
 				// separate tags for this module.
 				for (int t=0;t<unresolved.length;t++) { 
 					String _tag = this.getUnresolvedInputTag(unresolved[t]);
 
 					if(modules[i].compareTo(this.getModuleFromUnresolvedInputTag(unresolved[t])) == 0) {
 						// also coalesce
 						boolean found = false;
 						for(int tt=0; tt <Ntags; tt++) {
 							if(sortedTags[tt].compareTo(_tag) == 0) found = true;
 						}
 						if(!found) {
 							sortedTags[Ntags] = _tag;
 							Ntags++;	
 						}
 					}	
 				}
 				
 				// Highlights the Tag for this module in order:
 				// Display the first python line.
 				String header = "<a href='" + modules[i] + "'>" + 
 								modules[i] + " </a> " + 
 								pythonCode.substring(modules[i].length(), pythonCode.indexOf("(") + 1) + 
 								"... <br>";
 				text+= header;
 
 				// Displays list of unresolved tags:
 				for(int t =0; t < Ntags; t++) {
 					String tagLine = "";
 					if(pythonCode.indexOf(sortedTags[t]) == -1) {
 						System.err.println("ERROR: " + pythonCode);
 						System.err.println("ERROR: [confdb.gui.ConfDbGUI.getUnresolvedInputTagsSummary] Unresolved input tag not found! --> ["+t+"]" + sortedTags[t]);
 						System.err.println("ERROR: SEE: PythonParameterWriter.java(66). --> strange things happen here: from time to time the value is empty!");
 						// SEE: PythonParameterWriter.java(66). --> strange things happen here: from time to time the value is empty!
 					}
 						tagLine = "<b>" + pythonCode.substring(pythonCode.indexOf(sortedTags[t]), pythonCode.indexOf(")", pythonCode.indexOf(sortedTags[t]) + sortedTags[t].length()) + 1);
 						tagLine+=",</b><br>"; 
 
 					text+= tagLine;
 			        	
 				}
 				// Display dots:
 				text+= "    ... )<br><br>";
 			}
 		}
 		return text;
 	}
 
 	
 	
 	/** Look for Python code for a module of an unassigned input tag. */
 	private String getPythonCodeForModule(String module) {
 		String text 	= "";
 		// Looks for Python code of a module:
 		ModuleInstance moduleObj = currentConfig.module(module);
 		if(moduleObj != null) {
 		    try {
 				text = cnvEngine.getModuleWriter().toString(moduleObj);
 		    }
 		    catch (ConverterException e) {
 		    	System.out.println(e.getMessage());
 		    	return e.getMessage();
 		    }
 		} else System.out.println("module "+module+" NOT found!"); 
 		return text;
 	}
 	
 	/** get the module name from the old format of unassigned tag string */
 	/** this will be used to highlight the module name in the view, etc. */
 	private String getModuleFromUnresolvedInputTag(String unInTag) {
 		String text 	= "";
 		String module 	= null;
 		
 		java.util.List<String> temp= Arrays.asList(unInTag.split("/"));
 		if(temp.size() > 1) {
 			String relevant	= temp.get(temp.size() - 1);
 			temp= Arrays.asList(relevant.split("::")); // more than one if Vtag.
 			if(temp.size() > 1) {
 				module 	= temp.get(0);
 			}
 		} else return "";
 		return module;
 	}
 	
 	/** get the unassigned tag name from the old format of unassigned tag string */
 	/** this will be used to highlight the tag name in the view.                 */
 	private String getUnresolvedInputTag(String unInTag) {
 		String text 	= "";
 		String tag		= null;
 		
 		java.util.List<String> temp= Arrays.asList(unInTag.split("/"));
 		if(temp.size() > 1) {
 			String relevant	= temp.get(temp.size() - 1);
 			temp= Arrays.asList(relevant.split("::")); // more than one if Vtag.
 			if(temp.size() > 1) {
 				if(relevant.indexOf("::[") != -1) 	tag	= temp.get(temp.size() - 2); // Take the second to last tag.
 				else 								tag	= temp.get(temp.size() - 1); // Take the last tag.
 				
 				if(tag.indexOf('=') != -1) tag = tag.substring(0, tag.indexOf('='));
 				if(tag.indexOf('[') != -1) tag = tag.substring(0, tag.indexOf('['));
 			}
 		} else return "";
 		return tag;
 	}
 	
     
     ///////////////
     
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
 
 	treeModelParameters.setConfiguration(currentConfig);
 
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
     
     /** clear the paths fields panel - right upper area. */
     private void clearPathFields() {
     	// Restore the original jPanelPlugin panel.
     	jSplitPaneRightUpper.setTopComponent(jPanelPlugin);
     	jScrollPaneParameters.setVisible(true);
     	jSplitPaneRightUpper.setDividerLocation(0);
     	jSplitPaneRightUpper.setDividerSize(-1);
    }
     
     /** displays the paths fields panel - right upper area. */
     private void displayPathFields() {
     	// There only can be one Component. jPanelPathFields or jPanelPlugin.
     	if(jSplitPaneRightUpper.getComponents()[0].equals(jPanelPathFields)) return;
 
     	boolean extrafields = false;
 		try {
 			extrafields = database.checkExtraPathFields();
 		} catch (DatabaseException e) {
 			e.printStackTrace();
 		}
     	if(!extrafields) return;
     	
     	if(currentParameterContainer instanceof Path) {
     		Path container = (Path)currentParameterContainer;
     	    jSplitPaneRightUpper.setDividerLocation(100);	// Set the vertical size of the panel.
     	    jSplitPaneRightUpper.setDividerSize(-1);
         	jSplitPaneRightUpper.setTopComponent(jPanelPathFields);
         	jScrollPaneParameters.setVisible(false);
         	
         	
         	
         	jEditorPathDescription.setText(container.getDescription());
         	jEditorPathContacts.setText(container.getContacts());
         	jTextFieldPathName.setText(container.name());
         	
         	// Reset Save button when a new path is selected.
             jButtonSavePathFields.setEnabled(false);
             jButtonCancelPathFields.setEnabled(false);
             
             // Set prescales fot the current path.
             jTablePrescales = PrescaleTServ.getPrescaleTableEditable(container);
             
             jTablePrescales.getModel().addTableModelListener(new TableModelListener() {
 				public void tableChanged(TableModelEvent e) {
 					PrescaleTServ.setHasChanged();		// set the prescale as changed to allow save the value by jButtonSavePathFields.
 					setDocumentationFieldsChanged();	// Enable the buttons.
 				}
 			});
             
             
             jScrollPanePrescales.setViewportView(jTablePrescales);
         	
     	} else {
         	jEditorPathDescription.setText(new String());
         	jEditorPathContacts.setText(new String());
     	}
 
    }
     
 	// Set save button as enable to save documentation fields and prescales.      
    public void setDocumentationFieldsChanged() {
 		jButtonSavePathFields.setEnabled(true);
 		jButtonCancelPathFields.setEnabled(true);
    } 
     
     
     /** display the configuration snippet for currently selected component */
     private void displaySnippet()
     {
     //by default some tabs are disabled.
     if ((!(currentParameterContainer instanceof Path))	||
     	(!(currentParameterContainer instanceof Sequence))) 
     	restoreRightLowerTabs();
     	
     
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
 		jTabbedPaneRightLower.setEnabledAt(3, true); // sets second tab enabled
 		jTabbedPaneRightLower.setEnabledAt(4, true); // sets containedInSequence tab enabled
 		
 		jEditorContainedInPaths.setText(this.getAssignedPaths());
 		jEditorContainedInSequence.setText(this.getAssignedSequences());
 		
 		
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
 	    jEditorPaneSnippet.setText(cnvEngine.getPathWriter().toString(path,cnvEngine,"  "));
 	    
         jTabbedPaneRightLower.setEnabledAt(1, true); // sets second tab enabled
         jTabbedPaneRightLower.setEnabledAt(2, true); // sets third  tab enabled
         jEditorPanePathsToDataset.setText(this.getAssignedDatasets());
        
         jEditorPaneUnresolvedITags.setText(getUnresolvedInputTagsSummary());
 	}
 	else if (currentParameterContainer instanceof Sequence) {
 	    Sequence sequence = (Sequence)currentParameterContainer;
 	    jEditorPaneSnippet.setText(cnvEngine.getSequenceWriter().
 				       toString(sequence,cnvEngine,"  "));
         
 	    jTabbedPaneRightLower.setEnabledAt(2, true); // sets third  tab enabled
 
 	    jEditorPaneUnresolvedITags.setText(getUnresolvedInputTagsSummary());
         
 		jTabbedPaneRightLower.setEnabledAt(3, true); // sets second tab enabled
 		jEditorContainedInPaths.setText(this.getAssignedPaths());
 		
 		jTabbedPaneRightLower.setEnabledAt(4, true); // sets containedInSequence tab enabled
 		jEditorContainedInSequence.setText(this.getAssignedSequences());
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
     
     /** restore the snippet tabs to default 							*/
     /** block the assigned datasets and unassigned input tag Tabs in	*/
     /** the right lower panel when a path is not selected anymore. 		*/
     private void restoreRightLowerTabs(){
         jTabbedPaneRightLower.setEnabledAt(1, false); // sets tab as Disabled
         jTabbedPaneRightLower.setEnabledAt(2, false); // sets tab as Disabled
         jTabbedPaneRightLower.setEnabledAt(3, false); // sets tab as Disabled
         jTabbedPaneRightLower.setEnabledAt(4, false); // sets containedInSequences disabled.
         jTabbedPaneRightLower.setSelectedIndex(0);
 	    jEditorPanePathsToDataset.setText("");
 	    jEditorPaneUnresolvedITags.setText("");
 	    jEditorContainedInPaths.setText("");
 	    jEditorContainedInSequence.setText("");
 	    
 	    // Hyperlink listener to catch the path request.
 	    jEditorContainedInPaths.addHyperlinkListener(new HyperlinkListener() {
 			        public void hyperlinkUpdate(HyperlinkEvent event) {
 			            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
 			            	String pathname = event.getDescription();
 			            	ConfigurationTreeActions.scrollToPathByName(pathname, jTreeCurrentConfig);
 			            }
 			          }
 	    			}
 	    		);
 	    
 	    // Hyperlink listener to catch the module requests.	    
 	    jEditorPaneUnresolvedITags.addHyperlinkListener(new HyperlinkListener() {
 	        public void hyperlinkUpdate(HyperlinkEvent event) {
 	            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
 	            	String modulename = event.getDescription();
 	            	ConfigurationTreeActions.scrollToModuleByName(modulename, jTreeCurrentConfig);
 	            }
 	          }
 			}
 		);
 	    // Hyperlink listener to catch the sequence request.
 	    jEditorContainedInSequence.addHyperlinkListener(new HyperlinkListener() {
 	        public void hyperlinkUpdate(HyperlinkEvent event) {
 	            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
 	            	String sequenceName = event.getDescription();
 	            	ConfigurationTreeActions.scrollToSequenceByName(sequenceName, jTreeCurrentConfig);
 	            }
 	          }
 			}
 		);
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
     
     /** record new values for extra path fields. */
     private void SaveDocumentationFieldsActionPerformed()
     {
     	if(currentParameterContainer == null) return;
     	
     	if (currentParameterContainer instanceof Path) {
 		    Path p = (Path)currentParameterContainer;
 		    boolean save = false;
 		    // Only save if something has changed.
 		    if((p.getContacts() == null) && (!jEditorPathContacts.getText().equals(""))) 				save = true;
 		    if((p.getContacts() != null) && (!jEditorPathContacts.getText().equals(p.getContacts()))) 	save = true;
 		    if((p.getDescription() == null) && (!jEditorPathDescription.getText().equals(""))) 					save = true;
 		    if((p.getDescription() != null) && (!jEditorPathDescription.getText().equals(p.getDescription()))) 	save = true;
 		    
 		    if((PrescaleTServ != null) && (PrescaleTServ.hasChanged())) save = true;
 		    	
 		    if(save) {
 			    p.setDescription(jEditorPathDescription.getText());
 			    p.setContacts(jEditorPathContacts.getText());
 
 			    PrescaleTServ.savePrescales();	// Save Prescale values from Documentation Field Panel.
 			    
 			    p.setHasChanged();
 			    treeModelCurrentConfig.nodeChanged(p);
 			    treeModelCurrentConfig.updateLevel1Nodes();
 			    currentConfig.setHasChanged(true);
 			    
 			    // It will be automatically enabled when Text is modified.
 			    jButtonSavePathFields.setEnabled(false);
 			    jButtonCancelPathFields.setEnabled(false);
 			    
 		    }
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
     
     /** This event control the SplitPanel position in right
      * upper panel. */
     private void jSplitPaneRightComponentMoved(ComponentEvent e)
     {
     	// If the current selected item is not an Instance or a path
     	// then hide the upper panel.
 	if ((!(currentParameterContainer instanceof Instance))&&
 		(!(currentParameterContainer instanceof Path))) {
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
 	if(node instanceof Path) displayPathFields();
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
 
 	// AutoSave documentation fields
 	SaveDocumentationFieldsActionPerformed();
 
 
 	if (node instanceof EventContent) {
 	    jSplitPane.setRightComponent(jPanelContentEditor);
 	    fillEventContents();
 	    jComboBoxEventContent.getModel().setSelectedItem(node.toString());
 	    return;
 	}
 	 
 	// Bug 82525: "import" feature resets the size / position of the panels.
 	// RightComponent is only restored when needed.
 	if(jSplitPane.getRightComponent().equals(jPanelContentEditor))
 		jSplitPane.setRightComponent(jSplitPaneRight);
 	
 	clearPathFields();
 	
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
 	    
 	} else if (node==null||node==treeModelCurrentConfig.psetsNode()) {
 	    currentParameterContainer = currentConfig.psets();
 	    displayParameters();
 	    displaySnippet();
 	} else if (node instanceof ReferenceContainer) {
 	    clearParameters();
 	    currentParameterContainer = node;
 	    displaySnippet();
 	    if(currentParameterContainer instanceof Path) displayPathFields();
 	    
 	} else {
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
 	slm.addElement("<ALL>");
 	Iterator<Stream> itS = content.streamIterator();
 	while (itS.hasNext()) slm.addElement(itS.next().name());
 
 	// fill datasets
 	DefaultListModel dlm = (DefaultListModel)jListDatasets.getModel();
 	dlm.clear();
 	dlm.addElement("<ALL>");
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
 	
 	Stream stream = (lsmS.isSelectionEmpty() || lsmS.getMinSelectionIndex()==0 ) ?
 	    null : content.stream(lsmS.getMinSelectionIndex()-1);
 
 	// fill datasets
 	DefaultListModel dlm = (DefaultListModel)jListDatasets.getModel();
 	dlm.clear();
 	dlm.addElement("<ALL>");
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
 	
 	Stream stream = (lsmS.isSelectionEmpty() || lsmS.getMinSelectionIndex()==0 ) ?
 	    null : content.stream(lsmS.getMinSelectionIndex()-1);
 	
 	PrimaryDataset dataset = (lsmD.isSelectionEmpty() || lsmD.getMinSelectionIndex()==0 ) ?
 	    null : content.dataset(lsmD.getMinSelectionIndex()-1);
 	
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
 	Stream             stream = (lsmS.isSelectionEmpty() || lsmS.getMinSelectionIndex()==0 ) ?
 	    null : content.stream(lsmS.getMinSelectionIndex()-1);
 	
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
 	  item = new JMenuItem("Top"); menu.addComponent(item);
 	    item.setActionCommand(content.name()+":"+index);
 	    item.addActionListener(new ActionListener() {
 	    public void actionPerformed(ActionEvent e) {
 	    jTableCommandsPopupTop(e);
 	    }
 	    });
 	    if (index==0) item.setEnabled(false);
 	    
 	    // Up
 	    item = new JMenuItem("Up");     menu.addComponent(item);
 	    item.setActionCommand(content.name()+":"+
 	    content.indexOfCommand(command));
 	    item.addActionListener(new ActionListener() {
 	    public void actionPerformed(ActionEvent e) {
 	    jTableCommandsPopupUp(e);
 	    }
 	    });
 	    if (index==0) item.setEnabled(false);
 	    
 	    // Down
 	    item = new JMenuItem("Down");   menu.addComponent(item);
 	    item.setActionCommand(content.name()+":"+
 	    content.indexOfCommand(command));
 	    item.addActionListener(new ActionListener() {
 	    public void actionPerformed(ActionEvent e) {
 	    jTableCommandsPopupDown(e);
 	    }
 	    });
 	    if (index==content.commandCount()-1) item.setEnabled(false);
 	    
 	    // Bottom
 	    item = new JMenuItem("Bottom"); menu.addComponent(item);
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
 
 	// Remove All
 	item = new JMenuItem("Remove All"); menu.add(item);
 	item.setActionCommand(content.name()+":"+
 			      content.indexOfCommand(command));
 	item.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jTableCommandsPopupRemoveAll(e);
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
 	if (dlg.command()!=null) {
 		content.setHasChanged(); // TODO
 		command.set(dlg.command());
 	}
 
 	CommandTableModel ctm = (CommandTableModel)jTableCommands.getModel();
 	ctm.fireTableDataChanged();
 
 	updateOutputModulePreview();
 	
 	Iterator<Stream> itS = content.streamIterator();
 	while (itS.hasNext()) {
 	    OutputModule output = itS.next().outputModule();
 	    //output.setHasChanged(); //TODO
 
 	    
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
     
     
     /** jTableCommands: popup action 'Remove All' */
     private void jTableCommandsPopupRemoveAll(ActionEvent e)
     {
 	String s[] = ((JMenuItem)e.getSource()).getActionCommand().split(":");
 	String contentName = s[0];
 	int    commandIndex = (new Integer(s[1])).intValue();
 	EventContent  content = currentConfig.content(contentName);
 	OutputCommand command = content.command(commandIndex);
 	
 	content.removeAllCommands();
 	
 	fillComboBoxCommandsMenu(null);
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
 
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(jPanelLeft);
         jPanelLeft.setLayout(layout);
         layout.setHorizontalGroup(
 				  layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				  .addGroup(layout.createSequentialGroup()
 				       .addContainerGap()
 				       .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 					    
 					    .addComponent(jPanelCurrentConfig, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
 					    .addGroup(layout.createSequentialGroup()
 						 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 						      .addComponent(jLabelConfig)
 						      .addComponent(jLabelProcess)
 						      .addComponent(jLabelCreated))
 						 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 						 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 						      .addGroup(layout.createSequentialGroup()
 							   .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
 								.addComponent(jTextFieldCreated, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
 								.addComponent(jTextFieldProcess, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE))
 							   .addGap(22)
 							   .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 								.addGroup(layout.createSequentialGroup()
 								     .addComponent(jLabelRelease)
 								     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 								     .addComponent(jButtonRelease, javax.swing.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE))
 								.addGroup(layout.createSequentialGroup()
 								     .addComponent(jLabelCreator)
 								     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 								     .addComponent(jTextFieldCreator, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE))))
 						      .addGroup(layout.createSequentialGroup()
 							   .addComponent(jTextFieldCurrentConfig, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
 							   .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 							   .addComponent(jLabelLock, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
 						 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
 				       .addContainerGap())
 				  );
         layout.setVerticalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(layout.createSequentialGroup()
 				     .addContainerGap()
 				     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 					  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 					       .addComponent(jLabelConfig)
 					       .addComponent(jTextFieldCurrentConfig, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
 					  .addComponent(jLabelLock, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
 				     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 				     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 					  .addComponent(jLabelProcess)
 					  .addComponent(jLabelRelease)
 					  .addComponent(jTextFieldProcess, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
 					  .addComponent(jButtonRelease, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
 				     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 				     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 					  .addComponent(jLabelCreated)
 					  .addComponent(jTextFieldCreated, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
 					  .addComponent(jLabelCreator)
 					  .addComponent(jTextFieldCreator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
 				     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 				     .addComponent(jPanelCurrentConfig, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
 				     .addContainerGap())
 				);
 
         layout.linkSize(SwingConstants.VERTICAL, new java.awt.Component[] {jButtonRelease, jLabelRelease, jTextFieldProcess});
         layout.linkSize(SwingConstants.VERTICAL, new java.awt.Component[] {jLabelLock, jTextFieldCurrentConfig});
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
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(jPanelImportConfig);
         jPanelImportConfig.setLayout(layout);
         layout.setHorizontalGroup(
 				  layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				  .addGroup(layout.createSequentialGroup()
 				       .addComponent(jLabelImportSearch)
 				       .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 				       .addComponent(jTextFieldImportSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
 				       .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 				       .addComponent(jButtonImportCancelSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
 				  .addComponent(jScrollPaneImportConfig, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
 				  );
         layout.setVerticalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(layout.createSequentialGroup()
 				     .addContainerGap()
 				     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
 					  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 					       .addComponent(jLabelImportSearch)
 					       .addComponent(jTextFieldImportSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
 					  .addComponent(jButtonImportCancelSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
 				     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 				     .addComponent(jScrollPaneImportConfig, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE))
 				);
 
 	layout.linkSize(SwingConstants.VERTICAL, new java.awt.Component[] {jButtonImportCancelSearch, jTextFieldImportSearch});
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
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(jPanelCurrentConfig);
         jPanelCurrentConfig.setLayout(layout);
         layout.setHorizontalGroup(
 				  layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				  .addGroup(layout.createSequentialGroup()
 				       .addContainerGap()
 				       .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 					    .addComponent(jSplitPaneCurrentConfig, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
 					    .addGroup(layout.createSequentialGroup()
 						 .addComponent(jLabelSearch)
 						 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 						 .addComponent(jTextFieldSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
 						 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 						 .addComponent(jButtonCancelSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
 						 .addGap(63)
 						 .addComponent(jToggleButtonImport, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
 				       .addContainerGap())
 				  );
         layout.setVerticalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(layout.createSequentialGroup()
 				     .addContainerGap()
 				     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 					  .addComponent(jToggleButtonImport, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
 					  .addComponent(jButtonCancelSearch)
 					  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 					       .addComponent(jLabelSearch)
 					       .addComponent(jTextFieldSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
 				     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 				     .addComponent(jSplitPaneCurrentConfig, javax.swing.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
 				     .addContainerGap())
 				);
 
         layout.linkSize(SwingConstants.VERTICAL, new java.awt.Component[] {jButtonCancelSearch, jTextFieldSearch, jToggleButtonImport});
 
     }
 
     
     /** create the 'Search:' popup menu */
     private void createSearchPopupMenu()
     {
 	buttonGroupSearch1 = new ButtonGroup();
 	buttonGroupSearch2 = new ButtonGroup();
 	
 	JRadioButtonMenuItem rbMenuItem;
 	
 	rbMenuItem = new JRadioButtonMenuItem("contains");
 	rbMenuItem.setActionCommand("contains");
 	rbMenuItem.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jTreeCurrentConfigUpdateSearch(jTextFieldSearch.getText());
 		}
 	    });
 	buttonGroupSearch1.add(rbMenuItem);
 	jPopupMenuSearch.add(rbMenuItem);
 	rbMenuItem = new JRadioButtonMenuItem("matches");
 	rbMenuItem.setActionCommand("matches");
 	rbMenuItem.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jTreeCurrentConfigUpdateSearch(jTextFieldSearch.getText());
 		}
 	    });
 	buttonGroupSearch1.add(rbMenuItem);
 	jPopupMenuSearch.add(rbMenuItem);
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
 	jPopupMenuSearch.addSeparator();
 	rbMenuItem = new JRadioButtonMenuItem("names/labels");
 	rbMenuItem.setActionCommand("matchNames");
 	rbMenuItem.setSelected(true);
 	rbMenuItem.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jTreeCurrentConfigUpdateSearch(jTextFieldSearch.getText());
 		}
 	    });
 	buttonGroupSearch2.add(rbMenuItem);
 	jPopupMenuSearch.add(rbMenuItem);
 	rbMenuItem = new JRadioButtonMenuItem("types/plugins");
 	rbMenuItem.setActionCommand("matchTypes");
 	rbMenuItem.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jTreeCurrentConfigUpdateSearch(jTextFieldSearch.getText());
 		}
 	    });
 	buttonGroupSearch2.add(rbMenuItem);
 	jPopupMenuSearch.add(rbMenuItem);
 	rbMenuItem = new JRadioButtonMenuItem("parameter values");
 	rbMenuItem.setActionCommand("matchValues");
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
 	
 	rbMenuItem = new JRadioButtonMenuItem("contains");
 	rbMenuItem.setActionCommand("contains");
 	rbMenuItem.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jTreeImportConfigUpdateSearch(jTextFieldImportSearch.getText());
 		}
 	    });
 	buttonGroupImportSearch1.add(rbMenuItem);
 	jPopupMenuImportSearch.add(rbMenuItem);
 	rbMenuItem = new JRadioButtonMenuItem("matches");
 	rbMenuItem.setActionCommand("matches");
 	rbMenuItem.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jTreeImportConfigUpdateSearch(jTextFieldImportSearch.getText());
 		}
 	    });
 	buttonGroupImportSearch1.add(rbMenuItem);
 	jPopupMenuImportSearch.add(rbMenuItem);
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
 	jPopupMenuImportSearch.addSeparator();
 	rbMenuItem = new JRadioButtonMenuItem("names/labels");
 	rbMenuItem.setActionCommand("matchNames");
 	rbMenuItem.setSelected(true);
 	rbMenuItem.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jTreeImportConfigUpdateSearch(jTextFieldImportSearch.getText());
 		}
 	    });
 	buttonGroupImportSearch2.add(rbMenuItem);
 	jPopupMenuImportSearch.add(rbMenuItem);
 	rbMenuItem = new JRadioButtonMenuItem("types/plugins");
 	rbMenuItem.setActionCommand("matchTypes");
 	rbMenuItem.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    jTreeImportConfigUpdateSearch(jTextFieldImportSearch.getText());
 		}
 	    });
 	buttonGroupImportSearch2.add(rbMenuItem);
 	jPopupMenuImportSearch.add(rbMenuItem);
 	rbMenuItem = new JRadioButtonMenuItem("parameter values");
 	rbMenuItem.setActionCommand("matchValues");
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
     	// module's section:
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
 	
         javax.swing.GroupLayout jPanelPluginLayout = new javax.swing.GroupLayout(jPanelPlugin);
         jPanelPlugin.setLayout(jPanelPluginLayout);
         jPanelPluginLayout.setHorizontalGroup(
 					      jPanelPluginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 					      .addGroup(jPanelPluginLayout.createSequentialGroup()
 						   .addContainerGap()
 						   .addGroup(jPanelPluginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 							.addGroup(jPanelPluginLayout.createSequentialGroup()
 							     .addGroup(jPanelPluginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
 								  .addComponent(jTextFieldLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
 								  .addGroup(jPanelPluginLayout.createSequentialGroup()
 								       .addGroup(jPanelPluginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 									    .addComponent(jLabelPackage)
 									    .addComponent(jTextFieldPackage, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE))
 								       .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 								       .addGroup(jPanelPluginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 									    .addComponent(jLabelCVS)
 									    .addComponent(jTextFieldCVS, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))))
 							     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
 							.addGroup(jPanelPluginLayout.createSequentialGroup()
 							     .addComponent(jLabelLabel)
 							     .addGap(219)))
 						   .addGroup(jPanelPluginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 							.addComponent(jComboBoxPaths, 0, 131, Short.MAX_VALUE)
 							.addComponent(jLabelPaths)
 							.addComponent(jTextFieldPlugin, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)
 							.addComponent(jLabelPlugin))
 						   .addContainerGap())
 					      );
         jPanelPluginLayout.setVerticalGroup(
 					    jPanelPluginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 					    .addGroup(jPanelPluginLayout.createSequentialGroup()
 						 .addContainerGap()
 						 .addGroup(jPanelPluginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 						      .addComponent(jLabelPackage)
 						      .addComponent(jLabelCVS)
 						      .addComponent(jLabelPlugin))
 						 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 						 .addGroup(jPanelPluginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 						      .addComponent(jTextFieldPackage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
 						      .addComponent(jTextFieldCVS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
 						      .addComponent(jTextFieldPlugin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
 						 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 						 .addGroup(jPanelPluginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 						      .addComponent(jLabelLabel)
 						      .addComponent(jLabelPaths))
 						 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 						 .addGroup(jPanelPluginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
 						      .addComponent(jTextFieldLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
 						      .addComponent(jComboBoxPaths, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
 						 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 					    );
         jSplitPaneRightUpper.setTopComponent(jPanelPlugin);
 	
         //////////////////////////////////////////
         // path extra fields section - bug 75958 
         //
         JLabel jLabelPathDescription	= new javax.swing.JLabel();
         JLabel jLabelPathContacts		= new javax.swing.JLabel();
         JLabel jLabelPathName 			= new javax.swing.JLabel();
         JLabel jLabelPrescales 			= new javax.swing.JLabel();
 
         jLabelPathDescription.setFont(new java.awt.Font("Dialog", 0, 12));
         jLabelPathDescription.setText("Description:");
         
         jLabelPathContacts.setFont(new java.awt.Font("Dialog", 0, 12));
         jLabelPathContacts.setText("Contacts:");
         
         jLabelPathName.setFont(new java.awt.Font("Dialog", 0, 12));
         jLabelPathName.setText("Path:");
         
         jLabelPrescales.setFont(new java.awt.Font("Dialog", 0, 12));
         jLabelPrescales.setText("Prescales:");
         
         jTextFieldPathName.setBackground(new java.awt.Color(250, 250, 250));
         jTextFieldPathName.setEditable(false);
         jTextFieldPathName.setFont(new java.awt.Font("Dialog", 0, 10));
         jTextFieldPathName.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
         
         jScrollPanePathContacts.setViewportView(jEditorPathContacts);
         jScrollPanePathDescription.setViewportView(jEditorPathDescription);
         
         // Set Document Listener:
         jEditorPathContacts.getDocument().addDocumentListener(new DocumentListener() {
 			@Override
 			public void removeUpdate(DocumentEvent e) { somethingHasChanged(); }
 			
 			@Override
 			public void insertUpdate(DocumentEvent e) { somethingHasChanged(); }
 			
 			@Override
 			public void changedUpdate(DocumentEvent e) { somethingHasChanged(); }
 			
 			public void somethingHasChanged() {
 				setDocumentationFieldsChanged();
 			}
 		});
         // Set Document Listener:
         jEditorPathDescription.getDocument().addDocumentListener(new DocumentListener() {
 			@Override
 			public void removeUpdate(DocumentEvent e) { somethingHasChanged(); }
 			
 			@Override
 			public void insertUpdate(DocumentEvent e) { somethingHasChanged(); }
 			
 			@Override
 			public void changedUpdate(DocumentEvent e) { somethingHasChanged(); }
 			
 			public void somethingHasChanged() {
 				setDocumentationFieldsChanged();
 			}
 		});
         
         jButtonSavePathFields.setEnabled(false);
         jButtonCancelPathFields.setEnabled(false);
         jPanelPathFields.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
         
         // Prescales:
         //jScrollPanePrescales.setViewportView(jTablePrescales);
     	
     	/* set the elements: */
         javax.swing.GroupLayout jPanelPathLayout = new javax.swing.GroupLayout(jPanelPathFields);
         jPanelPathFields.setLayout(jPanelPathLayout);
         // Using TRAILING alignment the button will be aligned to the right.
         jPanelPathLayout.setHorizontalGroup(jPanelPathLayout.createSequentialGroup()
         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
         .addGroup(jPanelPathLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(jLabelPathName)
         .addComponent(jLabelPathDescription)
         .addComponent(jLabelPathContacts)
         .addComponent(jLabelPrescales)
         )
         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
         .addGroup(jPanelPathLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
         .addComponent(jTextFieldPathName, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
         .addComponent(jScrollPanePathDescription, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
         .addComponent(jScrollPanePathContacts, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
         .addComponent(jScrollPanePrescales, javax.swing.GroupLayout.DEFAULT_SIZE, 708, Short.MAX_VALUE)
         .addGroup(jPanelPathLayout.createSequentialGroup()
                 .addComponent(jButtonCancelPathFields, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jButtonSavePathFields, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
         )
         );
         
         jPanelPathLayout.setVerticalGroup(jPanelPathLayout.createSequentialGroup()
         .addGroup(jPanelPathLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
         .addComponent(jLabelPathName)
         .addComponent(jTextFieldPathName, javax.swing.GroupLayout.DEFAULT_SIZE, 18, Short.MAX_VALUE)
         )
         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
         .addGroup(jPanelPathLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
         .addComponent(jLabelPathDescription)
         .addComponent(jScrollPanePathDescription, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
         )
         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
         .addGroup(jPanelPathLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
         .addComponent(jLabelPathContacts)
         .addComponent(jScrollPanePathContacts, 80, 80, 80)
         )
         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
         .addGroup(jPanelPathLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
         .addComponent(jLabelPrescales)
         .addComponent(jScrollPanePrescales, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
         )
         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
         .addGroup(jPanelPathLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
         		.addComponent(jButtonCancelPathFields, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
         		.addComponent(jButtonSavePathFields, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
         
 
         //////////////////////////////////////////
         
         
         // Parameters Section:
 	jScrollPaneParameters.setBackground(new java.awt.Color(255, 255, 255));
         jScrollPaneParameters.setBorder(javax.swing.BorderFactory.createTitledBorder("Parameters"));
 	
         jScrollPaneParameters.setViewportView(jTreeTableParameters);
 	
         jSplitPaneRightUpper.setRightComponent(jScrollPaneParameters);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(jPanelRightUpper);
         jPanelRightUpper.setLayout(layout);
         layout.setHorizontalGroup(
 				  layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				  .addComponent(jSplitPaneRightUpper, javax.swing.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
 				  );
         layout.setVerticalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addComponent(jSplitPaneRightUpper, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
 				);
 	
 	jTreeTableParameters.getParent().setBackground(new Color(255,255,255));//PS
     }
     
     /** create the right lower panel */
     private void createRightLowerPanel()
     {
     	jEditorPaneSnippet.setEditable(false);
         jScrollPaneRightLower.setViewportView(jEditorPaneSnippet);
         jTabbedPaneRightLower.addTab("Snippet", jScrollPaneRightLower);
         
         // Initialize the right lower tabs by default.
         jEditorPanePathsToDataset.setEditable(false);
         TAB_assignedToDatasets.setViewportView(jEditorPanePathsToDataset);
         jTabbedPaneRightLower.addTab("Assigned to Datasets", TAB_assignedToDatasets);
         
         jEditorPaneUnresolvedITags.setEditable(false);
         jEditorPaneUnresolvedITags.setContentType("text/html");
         TAB_unresolvedInputTags.setViewportView(jEditorPaneUnresolvedITags);
         jTabbedPaneRightLower.addTab("Unresolved Input Tags", TAB_unresolvedInputTags);
         
         jEditorContainedInPaths.setEditable(false);
         jEditorContainedInPaths.setContentType("text/html");
         TAB_containedInPaths.setViewportView(jEditorContainedInPaths);
         jTabbedPaneRightLower.addTab("Contained in Paths", TAB_containedInPaths);
         
         jEditorContainedInSequence.setEditable(false);
         jEditorContainedInSequence.setContentType("text/html");
         TAB_containedInSequence.setViewportView(jEditorContainedInSequence);
         jTabbedPaneRightLower.addTab("Contained in Sequences", TAB_containedInSequence);
         
         
         jTabbedPaneRightLower.setEnabledAt(1, false); // sets the second tab as Disabled
         jTabbedPaneRightLower.setEnabledAt(2, false); // sets the third  tab as Disabled
         jTabbedPaneRightLower.setEnabledAt(3, false); // sets containedInPath tab as Disabled
         jTabbedPaneRightLower.setEnabledAt(4, false); // sets containedInSequence tab as Disabled
         
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(jPanelRightLower);
         jPanelRightLower.setLayout(layout);
         layout.setHorizontalGroup(
 				  layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				  .addComponent(jTabbedPaneRightLower, javax.swing.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
 				  );
         layout.setVerticalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addComponent(jTabbedPaneRightLower, javax.swing.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
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
 	jTableCommands.setDefaultRenderer(Integer.class, new CommandTableCellRenderer());
 	jTableCommands.setDefaultRenderer(OutputCommand.class, new CommandTableCellRenderer());
 	jTableCommands.setDefaultRenderer(String.class, new CommandTableCellRenderer());
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
 
         javax.swing.GroupLayout jPanelScrollPaneLayout = new javax.swing.GroupLayout(jPanelScrollPane);
         jPanelScrollPane.setLayout(jPanelScrollPaneLayout);
         jPanelScrollPaneLayout.setHorizontalGroup(
             jPanelScrollPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanelScrollPaneLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanelScrollPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanelScrollPaneLayout.createSequentialGroup()
                         .addComponent(jScrollPaneOutputModule, javax.swing.GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
                         .addContainerGap())
                     .addGroup(jPanelScrollPaneLayout.createSequentialGroup()
                         .addComponent(jLabelEventContent)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jComboBoxEventContent, 0, 420, Short.MAX_VALUE))
                     .addGroup(jPanelScrollPaneLayout.createSequentialGroup()
                         .addGroup(jPanelScrollPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jScrollPaneStreams, javax.swing.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
                             .addComponent(jLabelStreams))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(jPanelScrollPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabelDatasets)
                             .addComponent(jScrollPaneDatasets, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE))
                         .addContainerGap())
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelScrollPaneLayout.createSequentialGroup()
                         .addGroup(jPanelScrollPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabelPaths)
                             .addComponent(jScrollPanePaths, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(jPanelScrollPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jPanelScrollPaneLayout.createSequentialGroup()
                                 .addComponent(jLabelCommands)
                                 .addContainerGap(224, Short.MAX_VALUE))
                             .addComponent(jComboBoxCommands, javax.swing.GroupLayout.Alignment.TRAILING, 0, 347, Short.MAX_VALUE)
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelScrollPaneLayout.createSequentialGroup()
                                 .addComponent(jScrollPaneCommands, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
                                 .addContainerGap())
                             ))
                     .addGroup(jPanelScrollPaneLayout.createSequentialGroup()
                         .addComponent(jLabelOutputModule)
                         .addContainerGap(424, Short.MAX_VALUE))))
         );
         jPanelScrollPaneLayout.setVerticalGroup(
             jPanelScrollPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanelScrollPaneLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanelScrollPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabelEventContent)
                     .addComponent(jComboBoxEventContent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18)
                 .addGroup(jPanelScrollPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jLabelDatasets)
                     .addComponent(jLabelStreams))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanelScrollPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(jScrollPaneDatasets, 0, 0, Short.MAX_VALUE)
                     .addComponent(jScrollPaneStreams, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE))
                 .addGap(26)
                 .addGroup(jPanelScrollPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabelCommands)
                     .addComponent(jLabelPaths))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanelScrollPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(jPanelScrollPaneLayout.createSequentialGroup()
                         .addComponent(jComboBoxCommands, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(1)
                         .addComponent(jScrollPaneCommands, 0, 0, Short.MAX_VALUE))
                     .addComponent(jScrollPanePaths, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE))
                 .addGap(18)
                 .addComponent(jLabelOutputModule)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPaneOutputModule, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         jScrollPaneContentEditor.setViewportView(jPanelScrollPane);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(jPanelContentEditor);
         jPanelContentEditor.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPaneContentEditor, javax.swing.GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPaneContentEditor, javax.swing.GroupLayout.DEFAULT_SIZE, 686, Short.MAX_VALUE)
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
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(jPanelContentPane);
         jPanelContentPane.setLayout(layout);
         layout.setHorizontalGroup(
 				  layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				  .addComponent(jProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 1019, Short.MAX_VALUE)
 				  .addComponent(jSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1019, Short.MAX_VALUE)
 				  .addComponent(jPanelDbConnection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 				  .addComponent(jToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 1019, Short.MAX_VALUE)
 				  );
         layout.setVerticalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(layout.createSequentialGroup()
 				     .addComponent(jToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
 				     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 				     .addComponent(jPanelDbConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
 				     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 				     .addComponent(jSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)
 				     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 				     .addComponent(jProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
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
 
     /** Getters */
     public EventContent   getContent(){return content;}
     public Stream         getStream(){return stream;}
     public PrimaryDataset getDataset(){return dataset;}
     public Path           getPath(){return path;}
 
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
 
 //
 // CommandTableCellRenderer
 //
 class CommandTableCellRenderer extends DefaultTableCellRenderer
 {
     public Component getTableCellRendererComponent(JTable table,
 						   Object value,
 						   boolean isSelected,
 						   boolean hasFocus,
 						   int row,int column)
     {
 	setText(value.toString());
 	if ((value instanceof Integer) || (value instanceof OutputCommand) || (value instanceof String)) {
 
 	    OutputCommand oc = (OutputCommand)table.getValueAt(row,1);
 	    String       soc = oc.toString();
 	    
 	    setBackground(Color.LIGHT_GRAY);
 	    OutputCommand ocDropAll = new OutputCommand();
 	    ocDropAll.setDrop();
 	    if (soc.equals(ocDropAll.toString())) {
 		return this;
 	    }
 	    OutputCommand ocDropHLT = new OutputCommand();
 	    ocDropHLT.setDrop();
 	    ocDropHLT.setModuleName("hlt*");
 	    if (soc.equals(ocDropHLT.toString())) {
 		return this;
 	    }
 	    OutputCommand ocRawOnl = new OutputCommand();
 	    ocRawOnl.setClassName("FEDRawDataCollection");
 	    ocRawOnl.setModuleName("source");
 	    if (soc.equals(ocRawOnl.toString())) {
 		return this;
 	    }
 	    OutputCommand ocRawOff = new OutputCommand();
 	    ocRawOff.setClassName("FEDRawDataCollection");
 	    ocRawOff.setModuleName("rawDataCollector");
 	    if (soc.equals(ocRawOff.toString())) {
 		return this;
 	    }
 	    OutputCommand ocTrgRes = new OutputCommand();
 	    ocTrgRes.setClassName("edmTriggerResults");
 	    if (soc.equals(ocTrgRes.toString())) {
 		return this;
 	    }
 	    OutputCommand ocTrgEvt = new OutputCommand();
 	    ocTrgEvt.setClassName("triggerTriggerEvent");
 	    if (soc.equals(ocTrgEvt.toString())) {
 		return this;
 	    }
 
 	    setBackground(Color.RED);
 	    CommandTableModel ctm = (CommandTableModel)table.getModel();
 	    Path           path    = ctm.getPath();
 	    PrimaryDataset dataset = ctm.getDataset();
 	    Stream         stream  = ctm.getStream();
 	    EventContent   content = ctm.getContent();
 	    IConfiguration config  = content.config();
 	    if (config==null) return this;
 
 	    setBackground(Color.ORANGE);
 	    String label = oc.moduleName();
 	    if (label.equals("*")) return this;
 
 	    setBackground(Color.RED);
 	    ModuleInstance instance = config.module(label);
 	    if (instance==null) return this;
 
 	    setBackground(Color.ORANGE);
 	    Path[] paths = instance.parentPaths();
 	    boolean ok = false;
 	    if (path!=null) {
 		for (Path p : paths) {
 		    ok = (path.equals(p));
 		    if (ok) break;
 		}
 	    } else if (dataset!=null) {
 		for (Path p : paths) {
 		    ok = (dataset.indexOfPath(p)>=0);
 		    if (ok) break;
 		}
 	    } else if (stream!=null) {
 		ArrayList<Path>   assigned = new ArrayList<Path>(stream.listOfAssignedPaths());
 		ArrayList<Path> unassigned = new ArrayList<Path>(stream.listOfUnassignedPaths());
 		for (Path p : paths) {
 		    ok = (assigned.indexOf(p)>=0 || unassigned.indexOf(p)>=0);
 		    if (ok) break;
 		}
 	    } else if (content!=null) {
 		ArrayList<Path>   assigned = new ArrayList<Path>();
 		ArrayList<Path> unassigned = new ArrayList<Path>();
 		Iterator<Stream> itS = content.streamIterator();
 		while (itS.hasNext()) {
 		    Stream s = itS.next();
 		    assigned.clear();
 		    assigned = s.listOfAssignedPaths();
 		    unassigned.clear();
 		    unassigned = s.listOfUnassignedPaths();
 		    for (Path p : paths) {
 			ok = (assigned.indexOf(p)>=0 || unassigned.indexOf(p)>=0);
 			if (ok) break;
 		    }
 		    if (ok) break;
 		}
 	    } else {
 	    }
 
 	    if (ok) setBackground(Color.GREEN);
 
 	}
 	return this;
     }
 
 }
