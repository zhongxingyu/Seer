 package confdb.gui;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 
 /**
  * MenuBar
  * -------
  * @author Philipp Schieferdecker
  */
 public class MenuBar
 {
     //
     // member data
     //
     
     /** the menu bar */
     private JMenuBar jMenuBar = null;
 
     /** menu bar item names: confdbMenu */
     private static final String confdbMenuAbout = "About";
     private static final String confdbMenuQuit  = "Quit";
     
     /** menu bar item names: configMenu */
     private static final String configMenuNew         = "New";
     private static final String configMenuParse       = "Parse";
     private static final String configMenuJParse      = "JParse";
     private static final String configMenuOpen        = "Open";
     private static final String configMenuOpenOld     = "Open Old Schema";
     private static final String configMenuClose       = "Close";
     private static final String configMenuSave        = "Save";
     private static final String configMenuCommentSave = "Comment&Save";
     private static final String configMenuSaveAs      = "Save As";
     private static final String configMenuImport      = "Import";
     private static final String configMenuMigrate     = "Migrate";
     private static final String configMenuConvert     = "Convert";
     
     /** menu bar item names: toolMenu */
     private static final String toolMenuReplace       = "Search&Replace";
     private static final String toolMenuDiff          = "Compare (Diff)";
     private static final String toolMenuSmartVersions = "Smart Path Versioning";
     private static final String toolMenuSmartRenaming = "Smart Renaming";
     private static final String toolMenuPSEditor      = "Edit Prescales";
     private static final String toolMenuSPSEditor     = "Edit SmartPrescales";
     private static final String toolMenuMLEditor      = "Edit MessageLogger";
 
     /** menu bar item names: options */
     private static final String optionsMenuTrack      			= "Track InputTags";
     private static final String optionsMenuEnablePathCloning	= "Enable Path Deep Cloning";
 
     /** menu bar item names: dbMenu */
     private static final String dbMenuConnectToDB      = "Connect to DB";
     private static final String dbMenuDisconnectFromDB = "Disconnect from DB";
     private static final String dbMenuExportConfig     = "Export Configuration";
     private static final String dbMenuImportFromPython = "Import from Python";
     private static final String dbMenuDBInfo     	   = "Database Info";
     
     /** meny bar items */
     private JMenuItem confdbMenuAboutItem       = null;
     private JMenuItem confdbMenuQuitItem        = null;
     
     private JMenuItem configMenuNewItem         = null;
     private JMenuItem configMenuParseItem       = null;
     private JMenuItem configMenuJParseItem      = null;
     private JMenuItem configMenuOpenItem        = null;
     private JMenuItem configMenuOpenOldItem     = null;
     private JMenuItem configMenuCloseItem       = null;
     private JMenuItem configMenuCommentSaveItem = null;
     private JMenuItem configMenuSaveItem        = null;
     private JMenuItem configMenuSaveAsItem      = null;
 
     private JMenuItem configMenuImportItem      = null;
     private JMenuItem configMenuMigrateItem     = null;
     private JMenuItem configMenuConvertItem     = null;
 
     private JMenuItem toolMenuDiffItem          = null;
     private JMenuItem toolMenuSmartVersionsItem = null;
     private JMenuItem toolMenuSmartRenamingItem = null;
     private JMenuItem toolMenuReplaceItem       = null;
     private JMenuItem toolMenuPSEditorItem      = null;
     private JMenuItem toolMenuSPSEditorItem      = null;
     private JMenuItem toolMenuMLEditorItem      = null;
     
     private JCheckBoxMenuItem optionsMenuTrackItem = null;
     private JCheckBoxMenuItem optionsMenuEnablePathCloningItem = null;
 
     private JMenuItem dbMenuConnectItem         = null;
     private JMenuItem dbMenuDisconnectItem      = null;
     private JMenuItem dbMenuExportItem          = null;
     private JMenuItem dbMenuImportFromPythonItem= null;
     private JMenuItem dbMenuDBInfoItem			= null;
     
     
 
     //
     // construction
     //
     public MenuBar(JMenuBar jMenuBar,ConfDbGUI app)
     {
 	this.jMenuBar = jMenuBar;
 	populateMenuBar(new CommandActionListener(app));
     }
     
     
     //
     // memeber functions
     //
     
     /** a configuration was opened */
     public void configurationIsOpen()
     {
 	dbConnectionIsEstablished();
 	configMenuNewItem.setEnabled(true);
 	if (configMenuParseItem!=null) configMenuParseItem.setEnabled(true);
 	if (configMenuJParseItem!=null) configMenuJParseItem.setEnabled(true);
 	configMenuOpenItem.setEnabled(true);
 	configMenuCloseItem.setEnabled(true);
 	configMenuSaveItem.setEnabled(true);
 	configMenuCommentSaveItem.setEnabled(true);
 	configMenuSaveAsItem.setEnabled(true);
 	configMenuImportItem.setEnabled(true);
 	configMenuMigrateItem.setEnabled(true);
 	configMenuConvertItem.setEnabled(true);
 	toolMenuReplaceItem.setEnabled(true);
 	toolMenuDiffItem.setEnabled(true);
 	toolMenuSmartVersionsItem.setEnabled(true);
 	toolMenuSmartRenamingItem.setEnabled(true);
 	toolMenuPSEditorItem.setEnabled(true);
 	toolMenuSPSEditorItem.setEnabled(true);
 	toolMenuMLEditorItem.setEnabled(true);
 	optionsMenuTrackItem.setEnabled(true);
 	optionsMenuEnablePathCloningItem.setEnabled(true);
 	dbMenuExportItem.setEnabled(true);
     }
 
     /** no configuration is open */
     public void configurationIsNotOpen()
     {
 	configMenuCloseItem.setEnabled(false);
 	configMenuSaveItem.setEnabled(false);
 	configMenuCommentSaveItem.setEnabled(false);
 	configMenuSaveAsItem.setEnabled(false);
 	configMenuImportItem.setEnabled(false);
 	configMenuMigrateItem.setEnabled(false);
 	configMenuConvertItem.setEnabled(false);
 	//toolMenuDiffItem.setEnabled(false);
 	toolMenuSmartVersionsItem.setEnabled(false);
 	toolMenuSmartRenamingItem.setEnabled(false);
 	toolMenuReplaceItem.setEnabled(false);
 	toolMenuPSEditorItem.setEnabled(false);
 	toolMenuSPSEditorItem.setEnabled(false);
 	toolMenuMLEditorItem.setEnabled(false);
 	optionsMenuTrackItem.setEnabled(false);
 	optionsMenuEnablePathCloningItem.setEnabled(false);
 	dbMenuExportItem.setEnabled(false);
     }
     
     /** database connection is established */
     public void dbConnectionIsEstablished()
     {
 	configMenuNewItem.setEnabled(true);
 	if (configMenuParseItem!=null) configMenuParseItem.setEnabled(true);
 	if (configMenuJParseItem!=null) configMenuJParseItem.setEnabled(true);
 	configMenuOpenItem.setEnabled(true);
 	configMenuOpenOldItem.setEnabled(true);
 	toolMenuDiffItem.setEnabled(true);
 	toolMenuSmartVersionsItem.setEnabled(true);
 	toolMenuSmartRenamingItem.setEnabled(true);
 	dbMenuDisconnectItem.setEnabled(true);
 	dbMenuDBInfoItem.setEnabled(true);
     }
     
     /** no database connection is established */
     public void dbConnectionIsNotEstablished()
     {
 	configurationIsNotOpen();
 	configMenuNewItem.setEnabled(false);
 	configMenuOpenOldItem.setEnabled(false);
 	if (configMenuParseItem!=null) configMenuParseItem.setEnabled(false);
 	if (configMenuJParseItem!=null) configMenuJParseItem.setEnabled(false);
 	configMenuOpenItem.setEnabled(false);
 	toolMenuDiffItem.setEnabled(false);
 	toolMenuSmartVersionsItem.setEnabled(false);
 	toolMenuSmartRenamingItem.setEnabled(false);
 	dbMenuDisconnectItem.setEnabled(false);
 	dbMenuDBInfoItem.setEnabled(false);
     }
 
     /** populate the menu bar with all menus and their items */
     private void populateMenuBar(CommandActionListener listener)
     {
 	JMenuItem menuItem;
 	
 	JMenu confdbMenu = new JMenu("ConfDbGUI");
 	confdbMenu.setMnemonic(KeyEvent.VK_E);
 	jMenuBar.add(confdbMenu);
 	confdbMenuAboutItem = new JMenuItem(confdbMenuAbout,KeyEvent.VK_A);
 	confdbMenuAboutItem.setActionCommand(confdbMenuAbout);
 	confdbMenuAboutItem.addActionListener(listener);
 	confdbMenu.add(confdbMenuAboutItem);
 	confdbMenuQuitItem = new JMenuItem(confdbMenuQuit,KeyEvent.VK_Q);
 	confdbMenuQuitItem.setActionCommand(confdbMenuQuit);
 	confdbMenuQuitItem.addActionListener(listener);
 	confdbMenu.add(confdbMenuQuitItem);
 	
 	JMenu configMenu = new JMenu("Configurations");
 	configMenu.setMnemonic(KeyEvent.VK_C);
 	jMenuBar.add(configMenu);
 	configMenuNewItem = new JMenuItem(configMenuNew,KeyEvent.VK_N);
 	configMenuNewItem.setActionCommand(configMenuNew);
 	configMenuNewItem.addActionListener(listener);
 	configMenu.add(configMenuNewItem);
 	configMenuParseItem = new JMenuItem(configMenuParse,KeyEvent.VK_P);
 	configMenuParseItem.setActionCommand(configMenuParse);
 	configMenuParseItem.addActionListener(listener);
 	configMenu.add(configMenuParseItem);
 	
 	/*
 	configMenuJParseItem = new JMenuItem(configMenuJParse,KeyEvent.VK_J);
 	configMenuJParseItem.setActionCommand(configMenuJParse);
 	configMenuJParseItem.addActionListener(listener);
 	configMenu.add(configMenuJParseItem);
 	*/
 	configMenuOpenItem = new JMenuItem(configMenuOpen,KeyEvent.VK_O);
 	configMenuOpenItem.setActionCommand(configMenuOpen);
 	configMenuOpenItem.addActionListener(listener);
 	configMenu.add(configMenuOpenItem);
 	configMenuOpenOldItem = new JMenuItem(configMenuOpenOld,KeyEvent.VK_Q);
 	configMenuOpenOldItem.setActionCommand(configMenuOpenOld);
 	configMenuOpenOldItem.addActionListener(listener);
 	configMenu.add(configMenuOpenOldItem);
 	configMenuCloseItem = new JMenuItem(configMenuClose,KeyEvent.VK_C);
 	configMenuCloseItem.setActionCommand(configMenuClose);
 	configMenuCloseItem.addActionListener(listener);
 	configMenu.add(configMenuCloseItem);
 	configMenu.addSeparator();
 	configMenuSaveItem = new JMenuItem(configMenuSave,KeyEvent.VK_S);
 	configMenuSaveItem.addActionListener(listener);
 	configMenu.add(configMenuSaveItem);
 	configMenuCommentSaveItem=new JMenuItem(configMenuCommentSave,KeyEvent.VK_R);
 	configMenuCommentSaveItem.setActionCommand(configMenuCommentSave);
 	configMenuCommentSaveItem.addActionListener(listener);
 	configMenu.add(configMenuCommentSaveItem);
 	configMenuSaveAsItem = new JMenuItem(configMenuSaveAs,KeyEvent.VK_A);
 	configMenuSaveAsItem.setActionCommand(configMenuSaveAs);
 	configMenuSaveAsItem.addActionListener(listener);
 	configMenu.add(configMenuSaveAsItem);
 	configMenu.addSeparator();
 	configMenuImportItem = new JMenuItem(configMenuImport,KeyEvent.VK_I);
 	configMenuImportItem.setActionCommand(configMenuImport);
 	configMenuImportItem.addActionListener(listener);
 	configMenu.add(configMenuImportItem);
 	configMenuMigrateItem = new JMenuItem(configMenuMigrate,KeyEvent.VK_M);
 	configMenuMigrateItem.setActionCommand(configMenuMigrate);
 	configMenuMigrateItem.addActionListener(listener);
 	configMenu.add(configMenuMigrateItem);
 	configMenuConvertItem = new JMenuItem(configMenuConvert,KeyEvent.VK_N);
 	configMenuConvertItem.setActionCommand(configMenuConvert);
 	configMenuConvertItem.addActionListener(listener);
 	configMenu.add(configMenuConvertItem);
 	configMenu.addSeparator();
 	
 	JMenu toolMenu = new JMenu("Tools");
 	toolMenu.setMnemonic(KeyEvent.VK_T);
 	jMenuBar.add(toolMenu);
 	toolMenuDiffItem = new JMenuItem(toolMenuDiff,KeyEvent.VK_D);
 	toolMenuDiffItem.setActionCommand(toolMenuDiff);
 	toolMenuDiffItem.addActionListener(listener);
 	toolMenu.add(toolMenuDiffItem);
 	toolMenuSmartVersionsItem = new JMenuItem(toolMenuSmartVersions,KeyEvent.VK_V);
 	toolMenuSmartVersionsItem.setActionCommand(toolMenuSmartVersions);
 	toolMenuSmartVersionsItem.addActionListener(listener);
 	toolMenu.add(toolMenuSmartVersionsItem);
 	toolMenuSmartRenamingItem = new JMenuItem(toolMenuSmartRenaming,KeyEvent.VK_N);
 	toolMenuSmartRenamingItem.setActionCommand(toolMenuSmartRenaming);
 	toolMenuSmartRenamingItem.addActionListener(listener);
 	toolMenu.add(toolMenuSmartRenamingItem);
 	toolMenuReplaceItem = new JMenuItem(toolMenuReplace,KeyEvent.VK_R);
 	toolMenuReplaceItem.setActionCommand(toolMenuReplace);
 	toolMenuReplaceItem.addActionListener(listener);
 	toolMenu.add(toolMenuReplaceItem);
 	toolMenuPSEditorItem = new JMenuItem(toolMenuPSEditor,KeyEvent.VK_P);
 	toolMenuPSEditorItem.setActionCommand(toolMenuPSEditor);
 	toolMenuPSEditorItem.addActionListener(listener);
 	toolMenu.add(toolMenuPSEditorItem);
 	toolMenuSPSEditorItem = new JMenuItem(toolMenuSPSEditor,KeyEvent.VK_S);
 	toolMenuSPSEditorItem.setActionCommand(toolMenuSPSEditor);
 	toolMenuSPSEditorItem.addActionListener(listener);
 	toolMenu.add(toolMenuSPSEditorItem);
 	toolMenuMLEditorItem = new JMenuItem(toolMenuMLEditor,KeyEvent.VK_Q);
 	toolMenuMLEditorItem.setActionCommand(toolMenuMLEditor);
 	toolMenuMLEditorItem.addActionListener(listener);
 	toolMenu.add(toolMenuMLEditorItem);
 	
 
 
 	JMenu optionsMenu = new JMenu("Options");
 	optionsMenu.setMnemonic(KeyEvent.VK_O);
 	jMenuBar.add(optionsMenu);
 	// Enable track.
 	optionsMenuTrackItem = new JCheckBoxMenuItem(optionsMenuTrack);
 	optionsMenuTrackItem.addActionListener(listener);
 	optionsMenu.add(optionsMenuTrackItem);
 	// Enable path cloning.
 	optionsMenuEnablePathCloningItem = new JCheckBoxMenuItem(optionsMenuEnablePathCloning);
 	optionsMenuEnablePathCloningItem.addActionListener(listener);
 	optionsMenu.add(optionsMenuEnablePathCloningItem);
 	
 	JMenu dbMenu = new JMenu("Database");
 	dbMenu.setMnemonic(KeyEvent.VK_D);
 	jMenuBar.add(dbMenu);
 	dbMenuConnectItem = new JMenuItem(dbMenuConnectToDB,KeyEvent.VK_C);
 	dbMenuConnectItem.setActionCommand(dbMenuConnectToDB);
 	dbMenuConnectItem.addActionListener(listener);
 	dbMenu.add(dbMenuConnectItem);
 	dbMenuDisconnectItem = new JMenuItem(dbMenuDisconnectFromDB,KeyEvent.VK_D);
 	dbMenuDisconnectItem.setActionCommand(dbMenuDisconnectFromDB);
 	dbMenuDisconnectItem.addActionListener(listener);
 	dbMenu.add(dbMenuDisconnectItem);
 	dbMenuExportItem = new JMenuItem(dbMenuExportConfig,KeyEvent.VK_E);
 	dbMenuExportItem.setActionCommand(dbMenuExportConfig);
 	dbMenuExportItem.addActionListener(listener);
 	dbMenu.add(dbMenuExportItem);
 	
 	// bug/feature  #76151 	import from python
 	dbMenuImportFromPythonItem = new JMenuItem(dbMenuImportFromPython, KeyEvent.VK_P);
 	dbMenuImportFromPythonItem.setActionCommand(dbMenuImportFromPython);
 	dbMenuImportFromPythonItem.addActionListener(listener);
	//dbMenu.add(dbMenuImportFromPythonItem);	// Temporary disable menu option until be ready.
 	
 	dbMenuDBInfoItem = new JMenuItem(dbMenuDBInfo, KeyEvent.VK_B);
 	dbMenuDBInfoItem.setActionCommand(dbMenuDBInfo);
 	dbMenuDBInfoItem.addActionListener(listener);
 	dbMenu.add(dbMenuDBInfoItem);
     }
     
 
 }
