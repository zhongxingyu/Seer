 /*
  * Copyright (c) 2008-2009 The Negev Project
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  * - Redistributions of source code must retain the above copyright notice, 
  *   this list of conditions and the following disclaimer.
  *
  * - Redistributions in binary form must reproduce the above copyright notice, 
  *   this list of conditions and the following disclaimer in the documentation 
  *   and/or other materials provided with the distribution.
  *
  * - Neither the name of The Negev Project nor the names of its contributors 
  *   may be used to endorse or promote products derived from this software 
  *   without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package negev.giyusit;
 
 import com.trolltech.qt.core.*;
 import com.trolltech.qt.gui.*;
 
 import java.io.File;
 import java.text.MessageFormat;
 
 import negev.giyusit.candidates.AddCandidatesDialog;
 import negev.giyusit.candidates.DeleteCandidateDialog;
 import negev.giyusit.candidates.FindCandidatesDialog;
 import negev.giyusit.candidates.ImportCandidatesDialog;
 import negev.giyusit.config.TableAdminDialog;
 import negev.giyusit.events.AddEventDialog;
 import negev.giyusit.staff.StaffAdminDialog;
 
 import negev.giyusit.db.ConnectionProvider;
 import negev.giyusit.db.DatabaseException;
 import negev.giyusit.db.DatabaseUtils;
 import negev.giyusit.db.RulerCache;
 import negev.giyusit.util.MessageDialog;
 
 public class GiyusitWindow extends QMainWindow {
 		
 	// Widgets
 	private DataTable dataTable;
 	private DataViewList dataViewList;
 	
 	private QStackedWidget centralStack;
 		
 	// Actions
 	private QAction newFileAct;
 	private QAction loadFileAct;
 	private QAction quitAct;
 		
 	private QAction addCandidatesAct;
 	private QAction importCandidatesAct;
 	private QAction findCandidatesAct;
 	private QAction deleteCandidateAct;
 	
 	private QAction addEventAct;
 	
 	private QAction staffAdminAct;
 	private QAction tableAdminAct;
 	
 	private QAction aboutGiyusitAct;
 	private QAction aboutQtAct;
 	
 	private QAction exportDataAct;
 	
 	// Menus
 	private QMenu candidatesMenu;
 	private QMenu eventsMenu;
 	private QMenu toolsMenu;
 	
 	private QToolBar toolBar;
 	
 	// Properties
 	private String currentFileName;
 		
 	public GiyusitWindow() {
 		initUI();
 		initActions();
 		initMenus();
 		initToolBar();
 		initStatusBar();
 		restoreWindowState();
 		
 		currentFileName = "";
 		
 		updateWindowTitle();
 		setUIEnabled(false);
 		
 		// Load the last-used file once the event loop starts
 		QTimer.singleShot(0, this, "loadDatabase()");
 	}
 	
 	protected void closeEvent(QCloseEvent e) {
 		saveWindowState();	
 		
 		super.closeEvent(e);
 	}
 		
 	private void initUI() {
 		dataTable = new DataTable(null);
 		dataViewList = new DataViewList(null);
 		
 		dataViewList.dataViewSelected.connect(dataTable, "setDataView(DataView)");
 			
 		// Workspace
 		QWidget workspace = new QWidget();
 		QHBoxLayout workspaceLayout = new QHBoxLayout(workspace);
 			
 		workspaceLayout.setMargin(0);
 		workspaceLayout.addWidget(dataViewList, 1);
 		workspaceLayout.addWidget(dataTable, 3);
 		
 		// Cover widget
 		QWidget coverWidget = new QWidget();
 		QHBoxLayout coverLayout = new QHBoxLayout(coverWidget);
 			
 		coverLayout.addWidget(new QLabel(tr("<b>No file loaded!</b>")), 0, 
 									Qt.AlignmentFlag.AlignCenter);
 		
 		// Central Stack
 		centralStack = new QStackedWidget();
 		centralStack.addWidget(workspace);
 		centralStack.addWidget(coverWidget);
 			
 		setCentralWidget(centralStack);
 	}
 		
 	private void initActions() {
 		newFileAct = new QAction(tr("&New File..."), this);
 		newFileAct.setIcon(new QIcon("classpath:/icons/new.png"));
 		newFileAct.triggered.connect(this, "newFile()");
 			
 		loadFileAct = new QAction(tr("&Load File..."), this);
 		loadFileAct.setIcon(new QIcon("classpath:/icons/open.png"));
 		loadFileAct.triggered.connect(this, "loadFile()");
 					
 		quitAct = new QAction(tr("&Quit"), this);
 		quitAct.triggered.connect(QApplication.instance(), "quit()");
 			
 		addCandidatesAct = new QAction(tr("&Add Candidates..."), this);
 		addCandidatesAct.setIcon(new QIcon("classpath:/icons/add-candidates.png"));
 		addCandidatesAct.triggered.connect(this, "addCandidates()");
 			
 		importCandidatesAct = new QAction(tr("&Import Candidates..."), this);
 		importCandidatesAct.triggered.connect(this, "importCandidates()");
 			
 		findCandidatesAct = new QAction(tr("&Find Candidates..."), this);
 		findCandidatesAct.setIcon(new QIcon("classpath:/icons/find-candidates.png"));
 		findCandidatesAct.triggered.connect(this, "findCandidates()");
 		
 		deleteCandidateAct = new QAction(tr("&Delete Candidate..."), this);
 		deleteCandidateAct.triggered.connect(this, "deleteCandidate()");
 		
 		addEventAct = new QAction(tr("&Add Event..."), this);
 		addEventAct.setIcon(new QIcon("classpath:/icons/add-event.png"));
 		addEventAct.triggered.connect(this, "addEvent()");
 			
 		staffAdminAct = new QAction(tr("&Staff Admin..."), this);
 		staffAdminAct.triggered.connect(this, "staffAdmin()");
 		
 		tableAdminAct = new QAction(tr("&Table Admin..."), this);
 		tableAdminAct.triggered.connect(this, "tableAdmin()");
 		
 		aboutGiyusitAct = new QAction(tr("About &Giyusit..."), this);
 		aboutGiyusitAct.triggered.connect(this, "aboutGiyusit()");
 		
 		aboutQtAct  = new QAction(tr("About &Qt..."), this);
 		aboutQtAct.triggered.connect(QApplication.instance(), "aboutQt()");
 		
 		exportDataAct = new QAction(tr("Export Data..."), this);
 		exportDataAct.setIcon(new QIcon("classpath:/icons/export.png"));
 		exportDataAct.triggered.connect(dataTable, "exportData()");
 	}
 		
 	private void initMenus() {
 		QMenu fileMenu = menuBar().addMenu(tr("&File"));
 		fileMenu.addAction(newFileAct);
 		fileMenu.addAction(loadFileAct);
 		fileMenu.addSeparator();
 		fileMenu.addAction(quitAct);
 			
 		candidatesMenu = menuBar().addMenu(tr("&Candidates"));
 		candidatesMenu.addAction(addCandidatesAct);
 		candidatesMenu.addAction(importCandidatesAct);
 		candidatesMenu.addSeparator();
 		candidatesMenu.addAction(findCandidatesAct);
 		candidatesMenu.addSeparator();
 		candidatesMenu.addAction(deleteCandidateAct);
 			
 		eventsMenu = menuBar().addMenu(tr("&Events"));
 		eventsMenu.addAction(addEventAct);
 			
 		toolsMenu = menuBar().addMenu(tr("&Tools"));
 		toolsMenu.addAction(staffAdminAct);
 		toolsMenu.addAction(tableAdminAct);
 			
 		QMenu helpMenu = menuBar().addMenu(tr("&Help"));
 		helpMenu.addAction(aboutGiyusitAct);
 		helpMenu.addAction(aboutQtAct);
 	}
 		
 	private void initToolBar() {
 		toolBar = addToolBar(tr("Tool Bar"));
 		toolBar.setMovable(false);
 		toolBar.setIconSize(new QSize(32, 32));
 		toolBar.setToolButtonStyle(Qt.ToolButtonStyle.ToolButtonTextUnderIcon);
 		
 		toolBar.addAction(addCandidatesAct);
 		toolBar.addAction(addEventAct);
 		toolBar.addSeparator();
 		toolBar.addAction(findCandidatesAct);
 		toolBar.addSeparator();
 		toolBar.addAction(exportDataAct);
 	}
 		
 	private void initStatusBar() {
 		statusBar();
 	}
 	
 	private void saveWindowState() {
 		QSettings settings = new QSettings();
 		
 		settings.setValue("window/geometry", saveGeometry());
 		
 		// If there is a file already loaded save the DataViewList state
 		if (!currentFileName.isEmpty())
 			saveViewListState(settings);
 	}
 	
 	private void restoreWindowState() {
 		QSettings settings = new QSettings();
 		
 		restoreGeometry((QByteArray) settings.value("window/geometry"));
 	}
 	
 	private void saveViewListState(QSettings settings) {
 		String f = currentFileName.replace(File.separatorChar, '@');
 		String key = "viewList/" + f;
 			
 		settings.setValue(key, dataViewList.saveState());
 	}
 	
 	private void restoreViewListState(QSettings settings) {
 		String f = currentFileName.replace(File.separatorChar, '@');
 		String key = "viewList/" + f;
 		
 		dataViewList.restoreState(settings.value(key, "").toString());
 	}
 	
 	private void updateWindowTitle() {
 		if (!currentFileName.isEmpty()) {
 			String template = tr("Giyusit - {0}");
 			String name = new File(currentFileName).getName();
 			
 			setWindowTitle(MessageFormat.format(template, name));
 		}
 		else
 			setWindowTitle(tr("Giyusit"));
 	}
 	
 	private void setUIEnabled(boolean enabled) {
 		centralStack.setCurrentIndex(enabled ? 0 : 1);
 		
 		candidatesMenu.setEnabled(enabled);
 		eventsMenu.setEnabled(enabled);
 		toolsMenu.setEnabled(enabled);
 		toolBar.setEnabled(enabled);
 	}
 	
 	@SuppressWarnings("unused")
 	private void loadDatabase() {
 		loadDatabase("", false);
 	}
 	
 	private void loadDatabase(String fileName, boolean initialize) {
 		QSettings settings = new QSettings();
 		
 		if (fileName == null || fileName.isEmpty()) {
 			Object obj = settings.value("lastDatabase");
 			
 			if (obj == null || obj.toString().isEmpty())
 				return;
 			else
 				fileName = obj.toString();
 			
 			// Check to see if the file was deleted from the last time
 			boolean stillExists = new File(fileName).exists();
 			
 			if (!stillExists)
 				return;
 		}
 		
 		// If there is a file already loaded save the DataViewList state
 		if (!currentFileName.isEmpty())
 			saveViewListState(settings);
 		
 		// Make sure the UI is disabled
 		currentFileName = "";
 		
 		updateWindowTitle();
 		setUIEnabled(false);
 		
 		// If a initialize has been requested, and the database already exists,
 		// delete it
 		File file = new File(fileName);
 		
 		if (initialize && file.exists()) {
 			file.delete();
 		}
 		
 		//
 		ConnectionProvider.setJdbcUrl("jdbc:sqlite:" + fileName);
 		
 		// 
 		try {
 			if (!initialize) {
 				int schemaRev = DatabaseUtils.getFileSchemaRevision();
 				
 				if (schemaRev > DatabaseUtils.APPLICATIVE_SCHEMA_REVISION) {
 					System.out.println("WRONG!");
 					return;
 				}
 			}
 			else {
 				DatabaseUtils.runSqlScript(
 					getClass().getResourceAsStream("/sql/schema.sql"));
 					
 				DatabaseUtils.runSqlScript(
 					getClass().getResourceAsStream("/sql/dataviews.sql"));
 			}
 		}
 		catch (DatabaseException e) {
 			MessageDialog.showException(this, e);
 		}
 		
 		// Save this database as the last one
 		settings.setValue("lastDatabase", fileName);
 		
 		//
 		RulerCache.rebuildCache();
 		dataViewList.rebuildList();
 		
 		//
 		restoreViewListState(settings);
 		
		//
		currentFileName = fileName;
		
 		updateWindowTitle();
 		setUIEnabled(true);
 	}
 	
 	@SuppressWarnings("unused")
 	private void newFile() {
 		String fileName = MessageDialog.getSaveFileName(this, tr("New File"),
 				tr("Giyusit Data Profile (*.gdp)"));
 		
 		if (fileName != null && !fileName.isEmpty())
 			loadDatabase(fileName, true);
 	}
 	
 	@SuppressWarnings("unused")
 	private void loadFile() {
 		String fileName = MessageDialog.getOpenFileName(this, tr("Load File"), 
 				tr("Giyusit Data Profile (*.gdp)"));
 		
 		if (fileName != null && !fileName.isEmpty())
 			loadDatabase(fileName, false);
 	}
 	
 	@SuppressWarnings("unused")
 	private void addCandidates() {
 		AddCandidatesDialog dlg = new AddCandidatesDialog(this);
 		
 		int result = dlg.exec();
 		if (result == QDialog.DialogCode.Accepted.value())
 			dataTable.refresh();
 	}
 	
 	@SuppressWarnings("unused")
 	private void importCandidates() {
 		ImportCandidatesDialog dlg = new ImportCandidatesDialog(this);
 			
 		int result = dlg.exec();
 		if (result == QDialog.DialogCode.Accepted.value())
 			dataTable.refresh();
 	}
 	
 	@SuppressWarnings("unused")
 	private void findCandidates() {
 		FindCandidatesDialog dlg = new FindCandidatesDialog(this, 
 											FindCandidatesDialog.Mode.FindOnly);
 			
 		dlg.exec();
 	}
 	
 	private void deleteCandidate() {
 		DeleteCandidateDialog dlg = new DeleteCandidateDialog(this);
 			
 		int result = dlg.exec();
 		if (result == QDialog.DialogCode.Accepted.value())
 			dataTable.refresh();
 	}
 	
 	@SuppressWarnings("unused")
 	private void addEvent() {
 		AddEventDialog dlg = new AddEventDialog(this);
 			
 		int result = dlg.exec();
 		if (result == QDialog.DialogCode.Accepted.value())
 			dataTable.refresh();
 	}
 		
 	@SuppressWarnings("unused")
 	private void staffAdmin() {
 		StaffAdminDialog dlg = new StaffAdminDialog(this);
 			
 		dlg.exec();
 	}
 	
 	private void tableAdmin() {
 		TableAdminDialog dlg = new TableAdminDialog(this);
 			
 		dlg.exec();
 	}
 	
 	private void aboutGiyusit() {
 		String aboutStr = tr("<span style=\"font-weight: bold; font-size: large\">" + 
 							 "Giyusit version {0}</span><br>" + 
 							 "Copyright (c) 2008-2009 The Negev Project<br><br>" +
 							 "Giyusit is a free software under the terms of the " + 
 							 "<a href=\"http://www.opensource.org/licenses/bsd-license.php\">" + 
 							 "BSD license</a>.<br><br>" + 
 							 "Giyusit uses icons from the " +
 							 "<a href=\"http://www.oxygen-icons.org/\">Oxygen Project</a>, " + 
 							 "available under the terms <br> of the " +
 							 "<a href=\"http://creativecommons.org/licenses/by-sa/3.0/\">" + 
 							 "CC-by-SA 3.0 license</a>.");
 		
 		// Insert version
 		aboutStr = MessageFormat.format(aboutStr, QApplication.applicationVersion());
 		
 		QMessageBox.about(this, tr("About Giyusit"), aboutStr);
 	}
 }
