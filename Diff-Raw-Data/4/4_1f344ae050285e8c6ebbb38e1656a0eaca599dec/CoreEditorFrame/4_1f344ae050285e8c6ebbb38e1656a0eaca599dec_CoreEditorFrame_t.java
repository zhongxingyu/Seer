 /*
  *  ___  ___   _      ___                 _    
  * |   \/ __| /_\    / __|___ _ _  ___ __(_)___
  * | |) \__ \/ _ \  | (_ / -_) ' \/ -_|_-< (_-<
  * |___/|___/_/ \_\  \___\___|_||_\___/__/_/__/
  *
  * -----------------------------------------------------------------------------
  * @author: Herbert Veitengruber 
  * @version: 1.0.0
  * -----------------------------------------------------------------------------
  *
  * Copyright (c) 2013 Herbert Veitengruber 
  *
  * Licensed under the MIT license:
  * http://www.opensource.org/licenses/mit-license.php
  */
 package dsagenesis.editor.coredata;
 
 import dsagenesis.core.config.GenesisConfig;
 import dsagenesis.core.config.IGenesisConfigKeys;
 import dsagenesis.core.model.sql.AbstractSQLTableModel;
 import dsagenesis.core.model.sql.system.CoreDataTableIndex;
 import dsagenesis.core.model.sql.system.CoreDataVersion;
 import dsagenesis.core.model.sql.system.TableColumnLabels;
 import dsagenesis.core.sqlite.DBConnector;
 import dsagenesis.core.sqlite.TableHelper;
 import dsagenesis.core.ui.AbstractGenesisFrame;
 import dsagenesis.core.ui.HelpDialog;
 import dsagenesis.core.ui.InfoDialog;
 import dsagenesis.core.ui.PopupDialogFactory;
 import dsagenesis.core.ui.StatusBar;
 import dsagenesis.editor.coredata.table.CoreEditorTable;
 import dsagenesis.editor.coredata.table.CoreEditorTableModel;
 import dsagenesis.editor.coredata.task.CommitTableRowTask;
 import dsagenesis.editor.coredata.task.CreateDBTaskCoreEditor;
 import dsagenesis.editor.coredata.task.DefaultErrorRunnable;
 import dsagenesis.editor.coredata.task.DefaultFinishedRunnable;
 import dsagenesis.editor.coredata.task.LoadTableDataTask;
 import dsagenesis.editor.coredata.task.RemoveTableRowTask;
 
 import javax.swing.JToolBar;
 import java.awt.BorderLayout;
 import java.awt.Cursor;
 import java.awt.Rectangle;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.KeyStroke;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.InputEvent;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JButton;
 
 import jhv.image.ImageResource;
 import jhv.swing.task.SerialTaskExecutor;
 import jhv.util.debug.logger.ApplicationLogger;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.net.MalformedURLException;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.nio.file.StandardCopyOption;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Vector;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.JTabbedPane;
 
 import pro.ddopson.ClassEnumerator;
 
 /**
  * JFrame for the Core Data Editor.
  * 
  * opens by default the DB from our config.
  */
 public class CoreEditorFrame 
 		extends AbstractGenesisFrame 
 		implements TableModelListener, ActionListener, ChangeListener
 {
 
 	// ============================================================================
 	//  Constants
 	// ============================================================================
 			
 	private static final long serialVersionUID = 1L;
 	
 	/**
 	 * action commands for menus.
 	 */
 	public static final String ACMD_COPY = "copy";
 	public static final String ACMD_PASTE = "paste";
 	public static final String ACMD_ADDROW = "addRow";
 	public static final String ACMD_DELETEROW = "deleteRow";
 	
 	public static final String ACMD_COMMITALL= "commitAll";
 	public static final String ACMD_REFRESH = "refresh";
 	
 	public static final String ACMD_NEW = "new";
 	public static final String ACMD_OPEN = "open";
 	public static final String ACMD_CLOSE = "close";
 	public static final String ACMD_BACKUP = "backup";
 	public static final String ACMD_IMPORT = "import";
 	public static final String ACMD_EXPORT = "export";
 	
 	public static final String ACMD_INFO = "info";
 	public static final String ACMD_HELP = "help";
 	
 	
 	// ============================================================================
 	//  Variables
 	// ============================================================================
 		
 	/**
 	 * for status messages
 	 */
 	private StatusBar statusBar;
 	
 	/**
 	 * TitleBorder for the not
 	 */
 	private TitledBorder titleBorder;
 	
 	/**
 	 * for displaying Notes on a tab
 	 */
 	private JLabel lblNote;
 	
 	/**
 	 * tab panel for the tables
 	 */
 	private JTabbedPane tabbedPane;
 	
 	/**
 	 * Vector for accessing the tables
 	 */
 	private Vector<CoreEditorTable> vecTables;
 
 	/**
 	 * commit all button.
 	 */
 	private JButton btnCommitAll;
 	
 	/**
 	 * for working the tasks
 	 */
 	private SerialTaskExecutor taskExecutor = new SerialTaskExecutor(false);
 	
 	// ============================================================================
 	//  Constructors
 	// ============================================================================
 		
 	/**
 	 * Constructor.
 	 */
 	public CoreEditorFrame()
 	{
 		super(IGenesisConfigKeys.KEY_WIN_BASE);
 		BorderLayout borderLayout = (BorderLayout) getContentPane().getLayout();
 		borderLayout.setVgap(3);
 		borderLayout.setHgap(3);
 		
 		this.setTitle(
 				GenesisConfig.getInstance().getAppTitle()
 					+ " - "
 					+ labelResource.getProperty("title", "title")
 			);
 		
 		initBars();
 		
 		JPanel panel = new JPanel();
 		panel.setLayout(new BorderLayout(0, 0));
 		getContentPane().add(panel, BorderLayout.CENTER);
 		{
 			this.titleBorder = BorderFactory.createTitledBorder("");
 			this.lblNote = new JLabel("");
 			this.lblNote.setBorder(titleBorder);
 			panel.add(this.lblNote,BorderLayout.SOUTH);
 		}
 		{
 			this.tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 			panel.add(this.tabbedPane, BorderLayout.CENTER);
 		}
 		
 		this.statusBar = new StatusBar();
 		this.statusBar.setStatus("",
 				StatusBar.STATUS_WORKING
 			);
 		getContentPane().add(this.statusBar, BorderLayout.SOUTH);
 		
 		initDBAndTabs(GenesisConfig.getInstance().getDBFile());
 	}
 	
 
 	// ============================================================================
 	//  Functions
 	// ============================================================================
 	
 	/**
 	 * connection to the database
 	 * and initializing tabs
 	 * 
 	 * @param dbfilename
 	 */
 	private void initDBAndTabs(String dbfilename)
 	{
 		this.statusBar.setStatus(
 				labelResource.getProperty("status.init", "status.init"),
 				StatusBar.STATUS_WORKING
 			);
 		
 		try
 		{
 			DBConnector connector = DBConnector.getInstance();
 			
 			if( !connector.hasConnection() )
 				connector.openConnection(dbfilename,false);
 			
 			this.setTitle(
 					GenesisConfig.getInstance().getAppTitle()
 						+ " - "
 						+ labelResource.getProperty("title", "title")
 						+ " - "
 						+ connector.getDBFilename()
 				);
 	
 			ApplicationLogger.separator();
 			ApplicationLogger.logInfo(
 					"DB Version: "
 						+ TableHelper.getDBVersion() + " "
 						+ TableHelper.getDBLanguage()
 				);
 			
 			
 			vecTables = new Vector<CoreEditorTable>();
 			
 			CoreEditorTable table;
 			ImageIcon sysIco = (new ImageResource("resources/images/icons/dbTableSystem.gif",this)).getImageIcon();
 			
 			// init system Tables
 			{
 				table = new CoreEditorTable(this, new CoreDataVersion());
 				vecTables.add(table);
 				
 				tabbedPane.addTab("CoreDataVersion", new JScrollPane(table)); 
 				tabbedPane.setIconAt(0,	sysIco);
 			}
 			{
 				table = new CoreEditorTable(this, new CoreDataTableIndex());
 				vecTables.add(table);
 				tabbedPane.addTab("CoreDataTableIndex", new JScrollPane(table)); 
 				tabbedPane.setIconAt(1,	sysIco);
 			}
 			{
 				table = new CoreEditorTable(this, new TableColumnLabels());
 				vecTables.add(table);
 				
 				tabbedPane.addTab("TableColumnLabels",	new JScrollPane(table)); 
 				tabbedPane.setIconAt(2,	sysIco);
 			}
 			
 			// create the rest.
 			String query = "SELECT * FROM CoreDataTableIndex ORDER BY ti_tab_index ASC";
 			ResultSet rs = DBConnector.getInstance().executeQuery(query);
 			// get a list of all sql model classes
 			ArrayList<Class<?>> classList =  ClassEnumerator.getClassesForPackage(
 					Package.getPackage("dsagenesis.core.model.sql")
 				);
 			
 			while( rs.next() )
 				this.initDynamicTab(rs, classList);
 			
 			this.statusBar.setStatus(
 					labelResource.getProperty("status.ready", "status.ready"),
 					StatusBar.STATUS_OK
 				);
 			
 			int tabIdx = GenesisConfig.getInstance().getInt(
 					GenesisConfig.KEY_WIN_CORE_ACTIVE_TAB
 				);
 			if( tabIdx < 0 || tabIdx > tabbedPane.getTabCount() )
 				tabIdx = 0;
 			
 			tabbedPane.setSelectedIndex(tabIdx);
 			
 			this.stateChanged(null);
 			tabbedPane.addChangeListener(this);
 		
 		} catch (SQLException e) {
 			// the rest was logged before.
 			ApplicationLogger.logError("Cannot init tabs for CoreEditorFrame.");
 			ApplicationLogger.logError("Not a DSAGenesis Database or DB is corrupt!");
 			
 			this.statusBar.setStatus(
 					labelResource.getProperty("status.init.error", "status.init.error"),
 					StatusBar.STATUS_ERROR
 				);
 		}
 	}
 	
 	/**
 	 * initDynamicTab
 	 * 
 	 * creates a tab with its table by class reflection.
 	 * 
 	 * @param rs
 	 * @param modelClasslist
 	 */
 	private void initDynamicTab(
 			ResultSet rs,
 			ArrayList<Class<?>> modelClassList
 		)
 	{
 		try 
 		{
 			Class<?> c = null;
 			String tablename = rs.getString("ti_table_name");
 			for( int i=0; i<modelClassList.size(); i++ )
 			{
 				if( modelClassList.get(i).getSimpleName().equals(tablename) )
 				{
 					c = modelClassList.get(i);
 					break;
 				}
 			}
 			
 			if( c == null )
 			{
 				ApplicationLogger.logError(
 						"Class not found: dsagenesis.core.model.sql.*."
 							+ tablename
 							+ " !" 
 					);
 				return;
 			}
 			
 			Class<?> parameterTypes[] = new Class[1];
 			parameterTypes[0] = ResultSet.class;
 			
             Constructor<?> con = c.getConstructor(parameterTypes);
             
             Object args[] = new Object[1];
 			args[0] = rs;
 			
 			AbstractSQLTableModel model = (AbstractSQLTableModel)con.newInstance(args);
 			CoreEditorTable table = new CoreEditorTable(this, model);
 			
 			KeyStroke ksCopy = KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.CTRL_MASK,false);
 	        KeyStroke ksPaste = KeyStroke.getKeyStroke(KeyEvent.VK_V,ActionEvent.CTRL_MASK,false);
 	        KeyStroke ksAddRow = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,ActionEvent.CTRL_MASK,false);
 	        KeyStroke ksDeleteRow = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,ActionEvent.CTRL_MASK,false);
 	        table.registerKeyboardAction(this,ACMD_COPY,ksCopy,JComponent.WHEN_FOCUSED);
 	        table.registerKeyboardAction(this,ACMD_PASTE,ksPaste,JComponent.WHEN_FOCUSED);
 	        table.registerKeyboardAction(this,ACMD_ADDROW,ksAddRow,JComponent.WHEN_FOCUSED);
 	        table.registerKeyboardAction(this,ACMD_DELETEROW,ksDeleteRow,JComponent.WHEN_FOCUSED);
             
 			tabbedPane.addTab(
 					rs.getString("ti_label"),
 					new JScrollPane(table)
 				); 
 			
 			vecTables.add(table);
 	
 		} catch ( SQLException 
 				| InstantiationException 
 				| IllegalAccessException 
 				| NoSuchMethodException 
 				| SecurityException
 				| IllegalArgumentException
 				| InvocationTargetException e 
 			)
 		{
 			ApplicationLogger.logError(e);
 		} catch ( RuntimeException e2 ) {
 			ApplicationLogger.logError(e2);
 		}
 	}
 	
 	/**
 	 * initializes the bars.
 	 */
 	private void initBars() 
 	{
 		// icons
 		ImageResource irCopy = new ImageResource("resources/images/icons/copy.gif",this);
 		ImageResource irPaste = new ImageResource("resources/images/icons/paste.gif",this);
 		ImageResource irAddRow = new ImageResource("resources/images/icons/dbAddRow.gif",this);
 		ImageResource irDeleteRow = new ImageResource("resources/images/icons/dbRemoveRow.gif",this);
 		ImageResource irCommit = new ImageResource("resources/images/icons/dbCommit.gif",this);
 		ImageResource irRefresh = new ImageResource("resources/images/icons/reload.gif",this);
 		
 		
 		ImageResource irExport = new ImageResource("resources/images/icons/dbExport.gif",this);
 		ImageResource irImport = new ImageResource("resources/images/icons/dbImport.gif",this);
 		ImageResource irBackup = new ImageResource("resources/images/icons/dbBackup.gif",this);
 		ImageResource irInfo = new ImageResource("resources/images/icons/info.gif",this);
 		ImageResource irHelp = new ImageResource("resources/images/icons/help.gif",this);
 		
 		// toolbar
 		{
 			JToolBar toolBar = new JToolBar();
 			toolBar.setFloatable(false);
 			getContentPane().add(toolBar, BorderLayout.NORTH);
 			
 			JButton btnCopy = new JButton("");
 			btnCopy.setToolTipText(labelResource.getProperty("copy", "copy"));
 			btnCopy.setIcon(irCopy.getImageIcon());
 			btnCopy.setActionCommand(ACMD_COPY);
 			btnCopy.addActionListener(this);
 			toolBar.add(btnCopy);
 			
 			JButton btnPaste = new JButton("");
 			btnPaste.setToolTipText(labelResource.getProperty("paste", "paste"));
 			btnPaste.setIcon(irPaste.getImageIcon());
 			btnPaste.setActionCommand(ACMD_PASTE);
 			btnPaste.addActionListener(this);
 			toolBar.add(btnPaste);
 			
 			toolBar.add(new JToolBar.Separator());
 			
 			JButton btnAddRow = new JButton("");
 			btnAddRow.setToolTipText(labelResource.getProperty("addRow", "addRow"));
 			btnAddRow.setIcon(irAddRow.getImageIcon());
 			btnAddRow.setActionCommand(ACMD_ADDROW);
 			btnAddRow.addActionListener(this);
 			toolBar.add(btnAddRow);
 			
 			JButton btnDeleteRow = new JButton("");
 			btnDeleteRow.setToolTipText(labelResource.getProperty("deleteRow", "deleteRow"));
 			btnDeleteRow.setIcon(irDeleteRow.getImageIcon());
 			btnDeleteRow.setActionCommand(ACMD_DELETEROW);
 			btnDeleteRow.addActionListener(this);
 			toolBar.add(btnDeleteRow);
 			
 			toolBar.add(new JToolBar.Separator());
 			
 			btnCommitAll = new JButton("");
 			btnCommitAll.setToolTipText(labelResource.getProperty("commitAll", "commitAll"));
 			btnCommitAll.setIcon(irCommit.getImageIcon());
 			btnCommitAll.setActionCommand(ACMD_COMMITALL);
 			btnCommitAll.addActionListener(this);
 			toolBar.add(btnCommitAll);
 			btnCommitAll.setEnabled(false);
 			
 			JButton btnRefresh = new JButton("");
 			btnRefresh.setToolTipText(
 					labelResource.getProperty("refresh", "refresh")
 						+ " (F5)"
 				);
 			btnRefresh.setIcon(irRefresh.getImageIcon());
 			btnRefresh.setActionCommand(ACMD_REFRESH);
 			KeyStroke keyRefresh = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0); 
 			Action aRefresh = new AbstractAction(ACMD_REFRESH) {  
 					private static final long serialVersionUID = 1L;
 	
 					public void actionPerformed(ActionEvent e) {     
 				        CoreEditorFrame.this.actionRefresh();
 				    }
 				};
 			btnRefresh.getActionMap().put(ACMD_REFRESH, aRefresh);
 			btnRefresh.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyRefresh, ACMD_REFRESH);
 			btnRefresh.addActionListener(this);
 			toolBar.add(btnRefresh);
 		}
 		
 		
 		// menubar
 		{
 			JMenuBar menuBar = new JMenuBar();
 			setJMenuBar(menuBar);
 			
 			JMenu mnFile = new JMenu(labelResource.getProperty("mnFile", "mnFile"));
 			menuBar.add(mnFile);
 			
 			JMenuItem mntmNew = new JMenuItem(labelResource.getProperty("mntmNew", "mntmNew"));
 			mntmNew.setActionCommand(ACMD_NEW);
 			mntmNew.addActionListener(this);
 			mnFile.add(mntmNew);
 			
 			JMenuItem mntmOpen = new JMenuItem(labelResource.getProperty("mntmOpen", "mntmOpen"));
 			mntmOpen.setActionCommand(ACMD_OPEN);
 			mntmOpen.addActionListener(this);
 			mnFile.add(mntmOpen);
 			
 			mnFile.add(new JPopupMenu.Separator());
 			
 			JMenuItem mntmClose = new JMenuItem(labelResource.getProperty("mntmClose", "mntmClose"));
 			mntmClose.setActionCommand(ACMD_CLOSE);
 			mntmClose.addActionListener(this);
 			mnFile.add(mntmClose);
 			
 			mnFile.add(new JPopupMenu.Separator());
 			
 			JMenuItem mntmBackup = new JMenuItem(labelResource.getProperty("mntmBackup", "mntmBackup"));
 			mntmBackup.setIcon(irBackup.getImageIcon());
 			mntmBackup.setActionCommand(ACMD_BACKUP);
 			mntmBackup.addActionListener(this);
 			mnFile.add(mntmBackup);
 			
 			mnFile.add(new JPopupMenu.Separator());
 			
 			JMenuItem mntmImport = new JMenuItem(labelResource.getProperty("mntmImport", "mntmImport"));
 			mntmImport.setIcon(irImport.getImageIcon());
 			mntmImport.setActionCommand(ACMD_IMPORT);
 // TODO disabled until I get the Extension to work			
 			mntmImport.setEnabled(false);
 			//mntmImport.addActionListener(this);
 			mnFile.add(mntmImport);
 			
 			JMenuItem mntmExport = new JMenuItem(labelResource.getProperty("mntmExport", "mntmExport"));
 			mntmExport.setIcon(irExport.getImageIcon());
 			mntmExport.setActionCommand(ACMD_EXPORT);
 // TODO disabled until I get the Extension to work			
 			mntmExport.setEnabled(false);
 			//mntmExport.addActionListener(this);
 			mnFile.add(mntmExport);
 			
 			JMenu mnEdit = new JMenu(labelResource.getProperty("mnEdit", "mnEdit"));
 			menuBar.add(mnEdit);
 			
 			JMenuItem mntmCopy = new JMenuItem(labelResource.getProperty("copy", "copy"));
 			mntmCopy.setIcon(irCopy.getImageIcon());
 			mntmCopy.setActionCommand(ACMD_COPY);
 			mntmCopy.addActionListener(this);
 			mnEdit.add(mntmCopy);
 			
 			JMenuItem mntmPaste = new JMenuItem(labelResource.getProperty("paste", "paste"));
 			mntmPaste.setIcon(irPaste.getImageIcon());
 			mntmPaste.setActionCommand(ACMD_PASTE);
 			mntmPaste.addActionListener(this);
 			mnEdit.add(mntmPaste);
 			
 			mnEdit.add(new JPopupMenu.Separator());
 			
 			JMenuItem mntmAddRow = new JMenuItem(labelResource.getProperty("addRow", "addRow"));
 			mntmAddRow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK));
 			mntmAddRow.setIcon(irAddRow.getImageIcon());
 			mntmAddRow.setActionCommand(ACMD_ADDROW);
 			mntmAddRow.addActionListener(this);
 			mnEdit.add(mntmAddRow);
 			
 			JMenuItem mntmDeleteRow = new JMenuItem(labelResource.getProperty("deleteRow", "deleteRow"));
 			mntmDeleteRow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_MASK));
 			mntmDeleteRow.setIcon(irDeleteRow.getImageIcon());
 			mntmDeleteRow.setActionCommand(ACMD_DELETEROW);
 			mntmDeleteRow.addActionListener(this);
 			mnEdit.add(mntmDeleteRow);
 			
 			JMenu mnHelp = new JMenu(labelResource.getProperty("mnHelp", "mnHelp"));
 			menuBar.add(mnHelp);
 			
 			JMenuItem mntmInfo = new JMenuItem(labelResource.getProperty("mntmInfo", "mntmInfo"));
 			mntmInfo.setActionCommand(ACMD_INFO);
 			mntmInfo.addActionListener(this);
 			mntmInfo.setIcon(irInfo.getImageIcon());
 			mnHelp.add(mntmInfo);
 			
 			JMenuItem mntmHelp = new JMenuItem(labelResource.getProperty("mntmHelp", "mntmHelp"));
 			mntmHelp.setIcon(irHelp.getImageIcon());
 			mntmHelp.setActionCommand(ACMD_HELP);
 			mntmHelp.addActionListener(this);
 			mntmHelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
 			mnHelp.add(mntmHelp);
 		}
 		
 	}
 	
 	/**
 	 * markUnsavedTabTitle
 	 * 
 	 * @param table
 	 * @param unsaved
 	 */
 	private void markUnsavedTabTitle(
 			CoreEditorTable table, 
 			boolean unsaved
 		)
 	{
 		int idx = vecTables.indexOf(table);
 		String title = tabbedPane.getTitleAt(idx);
 		title = CoreEditorFrame.markUnsaved(title, unsaved);
 		tabbedPane.setTitleAt(idx, title);
 	}
 	
 	/**
 	 * getStatusBar
 	 * 
 	 * @return
 	 */
 	public StatusBar getStatusBar()
 	{
 		return this.statusBar;
 	}
 	
 	/**
 	 * setupTask
 	 * 
 	 * prepares the task executor before adding tasks with execute.
 	 * set messages/handlers for success and error, too.
 	 * 
 	 * returns false if a task is already running
 	 * 
 	 * @param startMsg
 	 * @param successMsg can be null
 	 * @param errorMsg
 	 * 
 	 * @return
 	 */
 	private boolean setupTask(
 			final String startMsg,
 			final String successMsg, 
 			final String errorMsg
 		)
 	{
 		if( taskExecutor == null )
 			taskExecutor = new SerialTaskExecutor(false);
 		
 		if( taskExecutor.isRunning() )
 			return false;
 		
 		statusBar.setStatus(startMsg,StatusBar.STATUS_WORKING);
 		this.setEnabled(false);
 		this.setCursor(
 				Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
 			);
 		
 		// set default finished runnable
 		if( successMsg != null )
 			taskExecutor.setFinishedRunnable(new DefaultFinishedRunnable(
 					this, 
 					statusBar, 
 					successMsg
 				));
 		
 		taskExecutor.setErrorRunnable(new DefaultErrorRunnable(
 				this, 
 				statusBar, 
 				errorMsg
 			));
 			
 		return true;
 	}
 	
 	/**
 	 * startLoadTableDataTask
 	 * 
 	 * starts loading/refreshing the table data with a SwingWorker task
 	 * 
 	 * @param table
 	 */
 	private void startLoadTableDataTask(final CoreEditorTable table)
 	{
 		boolean startNewTask = this.setupTask(
 				labelResource.getProperty("status.table.load", "status.table.load"),
 				labelResource.getProperty("status.ready", "status.ready"),
 				labelResource.getProperty("status.table.load.error", "status.table.load.error")
 			);
 		
 		if( !startNewTask )
 			return;
 		
 		taskExecutor.execute(new LoadTableDataTask(
 				statusBar.getStatusLabel(), 
 				statusBar.getProgressBar(),
 				table, 
 				labelResource.getProperty("status.table.load", "status.table.load")
 			));
 		taskExecutor.execute(new Runnable(){
 				public void run() 
 				{
 					titleBorder.setTitle(table.getLabel());
 					lblNote.setText(
 							"<html>"
 								+ table.getNote()
 								+ "</html>"
 						);
 				}
 			});
 	}
 	
 	/**
 	 * startRemoveTableRowTask
 	 * 
 	 * starts the task for deleting a table row
 	 * 
 	 * @param table
 	 */
 	private void startRemoveTableRowTask(final CoreEditorTable table)
 	{
 		boolean startNewTask = this.setupTask(
 				labelResource.getProperty("status.delete", "status.delete"),
 				labelResource.getProperty("status.delete.success", "status.delete.success"),
 				labelResource.getProperty("status.delete.error", "status.delete.error")
 			);
 		
 		if( !startNewTask )
 			return;
 		
 		taskExecutor.execute(new RemoveTableRowTask(
 				statusBar.getStatusLabel(), 
 				table, 
 				labelResource.getProperty("status.delete", "status.delete")
 			));
 	}
 	
 	/**
 	 * startCommitTableRowTask
 	 * 
 	 * is used for single and multi commits.
 	 * 
 	 * if table is null row is -1 then everything is commited.
 	 * 
 	 * @param table
 	 * @param row
 	 */
 	public void startCommitTableRowTask(
 			final CoreEditorTable table, 
 			int row
 		)
 	{
 		boolean startNewTask = this.setupTask(
 				labelResource.getProperty("status.commit", "status.commit"),
 				null,
 				labelResource.getProperty("status.commit.error", "status.commit.error")
 			);
 		
 		if( !startNewTask )
 			return;
 		
 		if( table == null ||  row > -1 )
 		{
 			// commit single row
 			
 			// custom finished runnable since we need to update 
 			// the markers
 			taskExecutor.setFinishedRunnable(new DefaultFinishedRunnable(
 					this, 
 					statusBar, 
 					labelResource.getProperty("status.commit.success", "status.commit.success")
 				){
 						public void run() 
 						{
 							super.run();
 							
 							markUnsavedTabTitle(table, table.containsUncommitedData());
 				        	boolean contentChanged = hasContentChanged();
 				        	String title = CoreEditorFrame.markUnsaved(getTitle(), contentChanged);
 							setTitle(title);
 							btnCommitAll.setEnabled(contentChanged);
 						}
 					});
 			
 			taskExecutor.execute(new CommitTableRowTask(
 					statusBar.getStatusLabel(), 
 					table,
 					row,
 					labelResource.getProperty("status.commit", "status.commit")
 				));
 		} else {
 			// commit all
 			taskExecutor.setFinishedRunnable(new DefaultFinishedRunnable(
 					this, 
 					statusBar, 
 					labelResource.getProperty("status.commit.success", "status.commit.success")
 				){
 						public void run() 
 						{
 							for( int i=0; i< vecTables.size(); i++ )
 							{
 								markUnsavedTabTitle(
 										vecTables.elementAt(i), 
 										vecTables.elementAt(i).containsUncommitedData()
 									);
 							}
 				        	boolean contentChanged = hasContentChanged();
 				        	String title = CoreEditorFrame.markUnsaved(getTitle(), contentChanged);
 							setTitle(title);
 							btnCommitAll.setEnabled(contentChanged);
 						}
 					});
 			
 			for( int i=0; i< vecTables.size(); i++ )
 			{
 				if( vecTables.elementAt(i).containsUncommitedData() )
 				{
 					final CoreEditorTable t =  vecTables.elementAt(i);
 					Vector<Integer> indices = t.getUncommitedRowIndices();
 					
 					for( int j=0; j<indices.size(); j++ )
 						taskExecutor.execute(new CommitTableRowTask(
 								statusBar.getStatusLabel(), 
 								t,
 								indices.elementAt(j),
 								labelResource.getProperty("status.commit", "status.commit")
 									+ " " + t.getSQLTable().getDBTableLabel()
 							));
 				}
 			}
 		}
 	}
 	
 	/**
 	 * setFocusOnActiveTab
 	 */
 	public void setFocusOnActiveTab() 
 	{
 		int tabidx = tabbedPane.getSelectedIndex();
 		vecTables.get(tabidx).requestFocus();
 	}
 	
 	@Override
 	public boolean hasContentChanged() 
 	{
		// fail save if there was a problem to find the db
		if( vecTables == null )
			return false;
		
 		for( int i=0; i< vecTables.size(); i++ )
 			if( vecTables.elementAt(i).containsUncommitedData() )
 				return true;
 	
 		return false;
 	}
 
 	@Override
 	public void contentSaved()
 	{
 		// not used since we have the commit buttons.
 	}
 	
 	@Override
 	public void saveConfig() 
 	{
 		GenesisConfig conf = GenesisConfig.getInstance();
 		
 		conf.setUserProperty(
 				GenesisConfig.KEY_WIN_CORE_ACTIVE_TAB, 
 				Integer.toString(tabbedPane.getSelectedIndex())
 			);
 		
 		super.saveConfig();
 	}
 	
 	@Override
 	public boolean close( WindowEvent e )
 	{
 		boolean doClose = super.close(e);
 		if( doClose && DBConnector.getInstance().hasConnection() )
 			DBConnector.getInstance().closeConnection();
 		
 		return doClose;
 	}
 	
 	@Override
 	public void dispose()
 	{
 		if( DBConnector.getInstance().hasConnection() )
 			DBConnector.getInstance().closeConnection();
 		super.dispose();
 	}
 	/**
 	 * actionCopy
 	 */
 	private void actionCopy()
 	{
 		int idx = this.tabbedPane.getSelectedIndex();
 		CoreEditorTable table = vecTables.elementAt(idx);
 
 		if( table.isReadOnly() )
 		{
 			this.statusBar.setStatus(
 					labelResource.getProperty(
 							"status.readonly.error",
 							"status.readonly.error"
 						),
 					StatusBar.STATUS_ERROR
 				);
 			return;
 		}
 		ClipboardCellTransfer.getInstance().setClipboardContents(table);
 	}
 	
 	/**
 	 * actionPaste
 	 */
 	private void actionPaste()
 	{
 		int idx = this.tabbedPane.getSelectedIndex();
 		CoreEditorTable table = vecTables.elementAt(idx);
 
 		if( table.isReadOnly() )
 		{
 			this.statusBar.setStatus(
 					labelResource.getProperty(
 							"status.readonly.error",
 							"status.readonly.error"
 						),
 					StatusBar.STATUS_ERROR
 				);
 			return;
 		}
 		
 		ClipboardCellTransfer.getInstance().getClipboardContents(table);
 	}
 	
 	/**
 	 * actionAddRow
 	 */
 	private void actionAddRow()
 	{
 		int idx = this.tabbedPane.getSelectedIndex();
 	    CoreEditorTable table = vecTables.elementAt(idx);
 	   
 	    if( table.isReadOnly() )
 	    {
 	    	this.statusBar.setStatus(
 					labelResource.getProperty(
 							"status.readonly.error",
 							"status.readonly.error"
 						),
 					StatusBar.STATUS_ERROR
 				);
 	    	return;
 	    }
 	    
 	    table.addEmptyRow();
 	    
 	    // now scroll to new entry
         int height = table.getHeight();
         table.scrollRectToVisible(new Rectangle(0, height - 1,1, height));
         
         markUnsavedTabTitle(table,true);
 		
 		// set frame title
 		String title = CoreEditorFrame.markUnsaved(this.getTitle(), true);
 		this.setTitle(title);
 		btnCommitAll.setEnabled(true);
 	}
 	
 	/**
 	 * actionDeleteRow
 	 */
 	private void actionDeleteRow()
 	{
 		int idx = this.tabbedPane.getSelectedIndex();
 	    CoreEditorTable table = vecTables.elementAt(idx);
 	    
 	    if( table.isReadOnly() )
 	    {
 	    	this.statusBar.setStatus(
 					labelResource.getProperty(
 							"status.readonly.error",
 							"status.readonly.error"
 						),
 					StatusBar.STATUS_ERROR
 				);
 	    	return;
 	    }
 	    
 	    int rowidx = table.getSelectedRow();  
 	    
 	    if( rowidx == -1 )
 	    	return;
 	    
 	    // confirm
 	    int result = PopupDialogFactory.confirmDeleteDatabaseRow(this);
 	   
 	    // delete
 	    if( result == JOptionPane.YES_OPTION )
 	    	startRemoveTableRowTask(table);
 	}
 	
 	/**
 	 * actionCommitAll
 	 */
 	private void actionCommitAll()
 	{
 		startCommitTableRowTask(null,-1);
 	}
 	
 	/**
 	 * actionRefresh
 	 */
 	private void actionRefresh()
 	{
 		int idx = tabbedPane.getSelectedIndex();
 		CoreEditorTable table = vecTables.elementAt(idx);
 		boolean refresh = true;
 				
 		if( table.containsUncommitedData() )
 		{
 			int result = PopupDialogFactory.confirmRefreshWithUncommitedData(
 					this, 
 					tabbedPane.getTitleAt(idx)
 				);
 			if( result != JOptionPane.YES_OPTION )
 				refresh = false;
 		} 
 		
 		if( !refresh )
 			return;
 			
 		this.startLoadTableDataTask(table);
 	}
 	
 	/**
 	 * actionNew
 	 */
 	private void actionNew()
 	{
 		actionClose();
 	
 		String filepath = GenesisConfig.getInstance().getDBFile();
 		
 		JFileChooser chooser = new JFileChooser();
 		chooser.setMultiSelectionEnabled(false);
 		chooser.setSelectedFile(new File(filepath));
 		chooser.setDialogTitle(labelResource.getProperty("mntmNew", "mntmNew"));
 		chooser.setFileFilter(new FileNameExtensionFilter("SQLite3 File", "s3db", "db"));
 		{
 			int result = chooser.showOpenDialog(this);
 		
 			if( result != JFileChooser.APPROVE_OPTION )
 				return;
 		}
 		
 		final String selectedFile = chooser.getCurrentDirectory()
 				+ System.getProperty("file.separator")
 				+ chooser.getSelectedFile().getName();
 		
 		File f = new File(selectedFile);
 		if( f.exists() )
 		{
 			int result = PopupDialogFactory.confirmOverwriteFile(this, selectedFile);
 			if( result != JOptionPane.YES_OPTION )
 				return;
 			
 			f.delete();
 		}
 		
 		DBConnector.getInstance().openConnection(
 				selectedFile,false
 			);
 		
 		this.setupTask(
 				labelResource.getProperty("status.init", "status.init"),
 				labelResource.getProperty("status.db.create.success", "status.db.create.success"),
 				labelResource.getProperty("status.db.create.error", "status.db.create.error")
 			);
 		taskExecutor.execute(new CreateDBTaskCoreEditor(
 				statusBar.getStatusLabel(),
 				statusBar.getProgressBar(),
 				labelResource.getProperty("status.db.create","status.db.create")
 			));
 		// add runnable to perform tab init
 		taskExecutor.execute(new Runnable(){
 				public void run() 
 				{
 					initDBAndTabs(selectedFile);
 				}
 			});
 	}
 	
 	/**
 	 * actionOpen
 	 */
 	private void actionOpen()
 	{
 		actionClose();
 		
 		String filepath = GenesisConfig.getInstance().getDBFile();
 		
 		JFileChooser chooser = new JFileChooser();
 		chooser.setMultiSelectionEnabled(false);
 		chooser.setSelectedFile(new File(filepath));
 		chooser.setDialogTitle(labelResource.getProperty("mntmOpen", "mntmOpen"));
 		chooser.setFileFilter(new FileNameExtensionFilter("SQLite3 File", "s3db", "db"));
 		int result = chooser.showOpenDialog(this);
 		
 		if( result != JFileChooser.APPROVE_OPTION )
 			return;
 		
 		String selectedFile = chooser.getCurrentDirectory()
 				+ System.getProperty("file.separator")
 				+ chooser.getSelectedFile().getName();
 		
 		initDBAndTabs(selectedFile);
 	}
 	
 	/**
 	 * actionClose 
 	 * closes only the DB
 	 * 
 	 */
 	private void actionClose()
 	{
 		DBConnector connector = DBConnector.getInstance();
 		
 		//fail save
 		if( connector.getConnection() == null )
 			return;
 		
 		if( this.hasContentChanged() )
 		{
 			int result = PopupDialogFactory.confirmCloseWithUnsavedData(this);
 			if( result != JOptionPane.YES_OPTION )
 				return;
 		}
 		
 		this.setTitle(
 				GenesisConfig.getInstance().getAppTitle()
 					+ " - "
 					+ labelResource.getProperty("title", "title")
 			);
 		tabbedPane.removeChangeListener(this);
 		tabbedPane.removeAll();
 		connector.closeConnection();
 		
 		vecTables = new Vector<CoreEditorTable>();
 		btnCommitAll.setEnabled(false);
 		titleBorder.setTitle("");
 		lblNote.setText("");
 		this.statusBar.setStatus(
 				labelResource.getProperty("status.ready", "status.ready"),
 				StatusBar.STATUS_OK
 			);
 	}
 	
 	/**
 	 * actionBackup
 	 */
 	private void actionBackup()
 	{
 		if( this.hasContentChanged() )
 		{
 			PopupDialogFactory.actionAborted(
 					labelResource.getProperty("mntmBackup", "mntmBackup")
 				);
 			return;
 		}
 		
 		String filepath = DBConnector.getInstance().getDBFilename();
 		String filename = filepath.substring(
 				filepath.lastIndexOf(System.getProperty("file.separator"))+1,
 				filepath.lastIndexOf(".")
 			);
 		filename = GenesisConfig.getInstance().getPathUserHome()
 				+ filename 
 				+ "_backup_" 
 				+ new SimpleDateFormat( "yyyy_MM_dd" ).format(new Date(System.currentTimeMillis()))
 				+ ".s3db";
 		
 		JFileChooser chooser = new JFileChooser();
 		chooser.setMultiSelectionEnabled(false);
 		chooser.setSelectedFile(new File(filename));
 		chooser.setDialogTitle(labelResource.getProperty("mntmBackup", "mntmBackup"));
 		chooser.setFileFilter(new FileNameExtensionFilter("SQLite3 File", "s3db", "db"));
 		int result = chooser.showSaveDialog(this);
 		
 		if( result != JFileChooser.APPROVE_OPTION )
 			return;
 		
 		String selectedFile = chooser.getCurrentDirectory()
 				+ System.getProperty("file.separator")
 				+ chooser.getSelectedFile().getName();
 		try 
 		{
 			Files.copy(
 					Paths.get(filepath), 
 					Paths.get(selectedFile), 
 					StandardCopyOption.REPLACE_EXISTING
 				);
 			this.statusBar.setStatus(
 					labelResource.getProperty("status.backup.success", "status.backup.success")
 						+ " "
 						+ selectedFile,
 					StatusBar.STATUS_OK
 				);
 		} catch( IOException e) {
 			ApplicationLogger.logError(e);
 			this.statusBar.setStatus(
 					labelResource.getProperty("status.backup.error", "status.backup.error"),
 					StatusBar.STATUS_ERROR
 				);
 		}
 	}
 	
 	/**
 	 * actionImport
 	 */
 	private void actionImport()
 	{
 		// TODO disabled until I get the Extension to work	
 System.out.println("TODO actionImport");
 	}
 	
 	/**
 	 * actionExport
 	 */
 	private void actionExport()
 	{
 		/*
 		try {
 			BackupCommand bcmd = new BackupCommand("core_de_DE", "test.sql");
 			ExtendedCommand.parse("backup 'core_de_DE' to 'test.sql'");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		*/
 		// TODO disabled until I get the Extension to work	
 System.out.println("TODO actionExport");
 	}
 	
 	/**
 	 * actionPerformed
 	 * 
 	 * for menu and toolbar handling
 	 * @param ae
 	 */
 	@Override
 	public void actionPerformed(ActionEvent ae) 
 	{
 		if( ae.getActionCommand().equals(ACMD_COPY) )
 		{
 			this.actionCopy();
 		} else if( ae.getActionCommand().equals(ACMD_PASTE) ) {
 			this.actionPaste();
 			
 		} else if( ae.getActionCommand().equals(ACMD_ADDROW) ) {
 			this.actionAddRow();
 			
 		} else if( ae.getActionCommand().equals(ACMD_DELETEROW) ) {
 			this.actionDeleteRow();
 			
 		} else if( ae.getActionCommand().equals(ACMD_COMMITALL) ) {
 			this.actionCommitAll();
 			
 		} else if( ae.getActionCommand().equals(ACMD_REFRESH) ) {
 			this.actionRefresh();
 			
 		} else if( ae.getActionCommand().equals(ACMD_NEW) ) {
 			this.actionNew();
 			
 		} else if( ae.getActionCommand().equals(ACMD_OPEN) ) {
 			this.actionOpen();
 			
 		}  else if( ae.getActionCommand().equals(ACMD_CLOSE) ) {
 			this.actionClose();
 			
 		} else if( ae.getActionCommand().equals(ACMD_BACKUP) ) {
 			this.actionBackup();
 			
 		} else if( ae.getActionCommand().equals(ACMD_IMPORT) ) {
 			this.actionImport();
 			
 		} else if( ae.getActionCommand().equals(ACMD_EXPORT) ) {
 			this.actionExport();
 			
 		} else if( ae.getActionCommand().equals(ACMD_INFO) ) {
 			InfoDialog d = new InfoDialog(CoreEditorFrame.this);
 			d.setVisible(true);
 			
 		} else if( ae.getActionCommand().equals(ACMD_HELP) ) {
 			String helpfile = "./help/"
 					+ GenesisConfig.getInstance().getLanguage()
 					+ "/coreDataEditor.html";
 			
 			File page = new File(helpfile);
 			try {
 				HelpDialog d = HelpDialog.getInstance();
 				d.openURL(page.toURI().toURL().toExternalForm());
 				d.setVisible(true);
 			} catch (MalformedURLException e) {
 				// nothing to do
 			}
 		}
 	}
 	
 	/** 
 	 * tableChanged
 	 * 
 	 * table data changes  so we need to update 
 	 * btnCommitAll and unsaved data marker
 	 * 
 	 * @param e
 	 */
 	@Override
 	public void tableChanged(TableModelEvent e)
 	{
 		int column = e.getColumn();
         CoreEditorTableModel model = ((CoreEditorTableModel)e.getSource());
 		CoreEditorTable table = model.getTable();
 		
 		// fail saves
 		// that the button is not enabled unnecessarily by clicking commit
 		if( column == (table.getColumnCount()-1)
 				&& (!table.isReadOnly())
 			)
 			return;
 		
         if( e.getType() == TableModelEvent.UPDATE
         		|| e.getType() == TableModelEvent.DELETE
         		|| e.getType() == TableModelEvent.INSERT
         	)
         {
         	markUnsavedTabTitle(table, table.containsUncommitedData());
         	
         	boolean contentChanged = hasContentChanged();
         	
         	String title = CoreEditorFrame.markUnsaved(getTitle(), contentChanged);
 			setTitle(title);
 			btnCommitAll.setEnabled(contentChanged);
         }
 	}
 	
 	/**
 	 * stateChanged
 	 * 
 	 * called if a tab has changed
 	 * 
 	 * @param ce
 	 */
 	@Override
 	public void stateChanged(ChangeEvent ce) 
 	{
 		CoreEditorTable table = vecTables.elementAt(
 				tabbedPane.getSelectedIndex()
 			);
 		
 		if( table.containsUncommitedData() )
 			return;
 			
 		this.startLoadTableDataTask(table);
 	}
 	
 }
