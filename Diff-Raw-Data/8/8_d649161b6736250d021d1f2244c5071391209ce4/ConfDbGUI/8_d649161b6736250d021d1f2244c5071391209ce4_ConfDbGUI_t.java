 package confdb.gui;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.tree.*;
 import javax.swing.table.*;
 import java.awt.*;
 import java.awt.event.*;
 
 import java.util.ArrayList;
 
 import confdb.db.CfgDatabase;
 import confdb.db.DatabaseException;
 
 import confdb.data.Configuration;
 import confdb.data.ConfigInfo;
 import confdb.data.Template;
 
 
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
     
     /** the current configuration */
     private Configuration config = null;
     
     /** handle access to the database */
     private CfgDatabase database = null;
     
     /** main frame of the application */
     private JFrame frame = null; 
     
     /** content pane of the application */
     private JPanel contentPane = null;
 
     /** database information panel */
     private DatabaseInfoPanel dbInfoPanel = null;
     
     /** panel to display the currently selected instance and its properties */
     private InstancePanel instancePanel = null;
 
     /** panel to display information about the current configuration */
     private ConfigurationPanel configurationPanel = null;
     
     /** tree structure holding the current configuration */
     private JTree tree = null;
     private ConfigurationTreeModel treeModel = null;
     
     /** list of all available Event Data Source templates */
     private ArrayList<Template> edsourceTemplateList = null;
     
     /** list of all available Event Setup Source templates */
     private ArrayList<Template> essourceTemplateList = null;
     
     /** list of all available Service templates */
     private ArrayList<Template> serviceTemplateList = null;
     
     /** list of all available Module templates */
     private ArrayList<Template> moduleTemplateList = null;
     
 
     //
     // construction
     //
     
     /** standard constructor */
     public ConfDbGUI(JFrame frame)
     {
 	this.frame           = frame;
 	config               = new Configuration();
 	database             = new CfgDatabase();
 	serviceTemplateList  = new ArrayList<Template>();
 	edsourceTemplateList = new ArrayList<Template>();
 	essourceTemplateList = new ArrayList<Template>();
 	moduleTemplateList   = new ArrayList<Template>();
     }
     
     
     //
     // member functions
     //
 
     /** TableModelListener: tableChanged() */
     public void tableChanged(TableModelEvent e)
     {
 	treeModel.updateLevel1Nodes();
 	tree.updateUI();
     }
     
     // connect to the database
     public void connectToDatabase()
     {
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
     }
     
     // connect to the database
     public void disconnectFromDatabase()
     {
 	try {
 	    database.disconnect();
 	    dbInfoPanel.disconnectedFromDatabase();
 	    serviceTemplateList.clear();
 	    edsourceTemplateList.clear();
 	    essourceTemplateList.clear();
 	    moduleTemplateList.clear();
 	}
 	catch (DatabaseException e) {
 	    String msg = "Failed to disconnect from DB: " + e.getMessage();
 	    JOptionPane.showMessageDialog(frame,msg,"",JOptionPane.ERROR_MESSAGE);
 	}
     }
     
     /** new configuration */
     public void newConfiguration()
     {
 	closeConfiguration();
 	ConfigurationNameDialog dialog = new ConfigurationNameDialog(frame,database);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	if (dialog.validChoice()) {
 	    String cfgName    = dialog.name();
 	    String releaseTag = dialog.releaseTag();
 	    database.loadEDSourceTemplates(releaseTag,edsourceTemplateList);
 	    database.loadESSourceTemplates(releaseTag,essourceTemplateList);
 	    database.loadServiceTemplates(releaseTag,serviceTemplateList);
 	    database.loadModuleTemplates(releaseTag,moduleTemplateList);
 	    config.initialize(new ConfigInfo(cfgName,null,releaseTag),
 			      edsourceTemplateList,
 			      essourceTemplateList,
 			      serviceTemplateList,
 			      moduleTemplateList);
 	    //tree.setConfiguration(config);
 	    treeModel.setConfiguration(config);
 	    tree.updateUI();
 	    configurationPanel.update(config);
 	}
     }
     
     /** open configuration */
     public void openConfiguration()
     {
 	if (!closeConfiguration()) return;
 	
 	OpenConfigurationDialog dialog =
 	    new OpenConfigurationDialog(frame,database);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	if (dialog.validChoice()) {
 	    ConfigInfo configInfo = dialog.configInfo();
 	    config = database.loadConfiguration(dialog.configInfo(),
 						edsourceTemplateList,
 						essourceTemplateList,
 						serviceTemplateList,
 						moduleTemplateList);
 	    //tree.setConfiguration(config);
 	    treeModel.setConfiguration(config);
 	    tree.updateUI();
 	    configurationPanel.update(config);
 	}
     }
 
     /** close configuration */
     public boolean closeConfiguration()
     {
 	if (config.hasChanged()) {
 	    String msg =
 		"The current configuration has changed.\n" +
 		"Do you want to save it before closing?";
 	    int answer = 
 		JOptionPane.showConfirmDialog(frame,msg,"",
 					      JOptionPane.YES_NO_OPTION);
 	    if (answer==0) {
 		if (!saveConfiguration()) {
 		    msg =
 			"The current configuration can't be saved. " +
 			"Do you really want to close it?";
 		    answer = JOptionPane.showConfirmDialog(frame,msg,"",
 							   JOptionPane.YES_NO_OPTION);
 		    if (answer==1) return false;
 		}
 	    }
 	}
 	config.reset();
 	treeModel.setConfiguration(config);
 	tree.updateUI();
 	configurationPanel.update(config);
 	instancePanel.clear();
 	return true;
     }
     
     /** save configuration */
     public boolean saveConfiguration()
     {
 	if (!config.hasChanged()) return true;
 	if (!checkConfiguration()) return false;
 	if (config.version()==0) return saveAsConfiguration();
 	
 	if (database.insertConfiguration(config)) {
 	    config.setHasChanged(false);
 	    configurationPanel.update(config);
 	    return true;
 	}
 	return false;
     }
     
     /** saveAs configuration */
     public boolean saveAsConfiguration()
     {
 	SaveConfigurationDialog dialog =
 	    new SaveConfigurationDialog(frame,database,config);
 	dialog.pack();
 	dialog.setLocationRelativeTo(frame);
 	dialog.setVisible(true);
 	
 	if (dialog.validChoice()) {
 	    if (database.insertConfiguration(config)) {
 		config.setHasChanged(false);
 		configurationPanel.update(config);
 		return true;
 	    }
 	}
 	return false;
     }
     
     /** check if configuration is in a storable state */
     public boolean checkConfiguration()
     {
 	int unsetParamCount = config.unsetTrackedParameterCount();
 	if (unsetParamCount>0) {
 	    String msg =
 		"The configuration contains " + unsetParamCount +
 		" unset tracked parameters. They must be set before saving!";
 	    JOptionPane.showMessageDialog(frame,msg,"",
 					  JOptionPane.ERROR_MESSAGE);
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
 	String releaseTag = config.releaseTag();
 	database.loadEDSourceTemplates(releaseTag,edsourceTemplateList);
 	database.loadESSourceTemplates(releaseTag,essourceTemplateList);
 	database.loadServiceTemplates(releaseTag,serviceTemplateList);
 	database.loadModuleTemplates(releaseTag,moduleTemplateList);
 	config.updateHashMaps(edsourceTemplateList,
 			      essourceTemplateList,
 			      serviceTemplateList,
 			      moduleTemplateList);
     }
 
     /** create the content pane */
     private JPanel createContentPane()
     {
 	// the contentPane of the main frame
 	JPanel contentPane = new JPanel(new GridBagLayout());
 	contentPane.setOpaque(true);
 	
 	GridBagConstraints c = new GridBagConstraints();
 	c.fill = GridBagConstraints.BOTH;
 	c.weightx = 0.5;
 	
 	c.gridx=1;c.gridy=0; c.weighty=0.01;
 	contentPane.add(createDbInfoView(),c);
 	
 	// create the component panel and stuff it into a JScrollPane
 	JPanel instanceView = createInstanceView(new Dimension(500,800));
 	
 	// create the tree and stuff it into a JScrollPane
 	JPanel treeView = createTreeView(new Dimension(500,800));
 	
 	// create the tree/component panel split pane
 	JSplitPane  horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 							 treeView,instanceView);
 	horizontalSplitPane.setOneTouchExpandable(true);
 	horizontalSplitPane.setResizeWeight(0.5);
 	horizontalSplitPane.setDividerLocation(0.5);
 	
 	// add horizontal split pane to content pane
 	c.gridx=1;c.gridy=1; c.weighty=0.99;
 	contentPane.add(horizontalSplitPane,c);
 	
 	// return content pane, to be set in main frame
 	return contentPane;
     }
 
     /** craete the top panel with the database connection info */
     private JPanel createDbInfoView()
     {
 	dbInfoPanel = new DatabaseInfoPanel();
 	return dbInfoPanel;
     }
         
     /** create the component panel, to display the currently selected component */
     private JPanel createInstanceView(Dimension dim)
     {
 	instancePanel = new InstancePanel(dim);
 	instancePanel.addTableModelListener(this);
 	return instancePanel;
     }
     
     /** create the configuration tree */
     private JPanel createTreeView(Dimension dim)
     {
 	configurationPanel = new ConfigurationPanel();
 	treeModel = new ConfigurationTreeModel(config);
 	tree      = new JTree(treeModel);
 	tree.addTreeSelectionListener(instancePanel);
 
 	ConfigurationTreeMouseListener mouseListener =
 	    new ConfigurationTreeMouseListener(tree,frame,
 					       serviceTemplateList,
 					       edsourceTemplateList,
 					       essourceTemplateList,
 					       moduleTemplateList);
 	tree.addMouseListener(mouseListener);
 	treeModel.addTreeModelListener(mouseListener);
 	
 	tree.setRootVisible(false);
 	tree.setEditable(true);
 	tree.getSelectionModel()
 	    .setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 	
 	ConfigurationTreeRenderer renderer = new ConfigurationTreeRenderer();
 	tree.setCellRenderer(renderer);
 	tree.setCellEditor(new ConfigurationTreeEditor(tree,renderer));
 	
 	instancePanel.setConfigurationTree(tree);
 
 	JPanel treeView = new JPanel(new GridBagLayout());
 	treeView.setPreferredSize(dim);
 	
 	JScrollPane spTree = new JScrollPane(tree);
 	//spTree.setPreferredSize(??);
 	
 	GridBagConstraints c = new GridBagConstraints();
 	c.fill = GridBagConstraints.BOTH;
 	c.weightx = 0.5;
 	
 	c.gridx=0;c.gridy=0;c.gridwidth=1; c.weighty = 0.01;
 	treeView.add(configurationPanel,c);
 	
 	c.gridx=0;c.gridy=1;c.gridwidth=1; c.weighty=0.99;
 	treeView.add(spTree,c);
 	
 	treeView.setPreferredSize(dim);
 	return treeView;
     }
     
     /** create the menu bar */
     private void createMenuBar()
     {
 	CfgMenuBar menuBar = new CfgMenuBar(frame,this);
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
 
 	// reset configurations
 	app.closeConfiguration();
     }
     
 
     /** main, create and show GUI, thread-safe */
     public static void main(String[] args)
     {
 	javax.swing.SwingUtilities
	    .invokeLater(new Runnable() 
		{
		    public void run()
		    {
			createAndShowGUI();
		    }
		});
     }
     
 }
