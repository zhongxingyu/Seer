 // PathVisio,
 // a tool for data visualization and analysis using Biological Pathways
 // Copyright 2006-2007 BiGCaT Bioinformatics
 //
 // Licensed under the Apache License, Version 2.0 (the "License"); 
 // you may not use this file except in compliance with the License. 
 // You may obtain a copy of the License at 
 // 
 // http://www.apache.org/licenses/LICENSE-2.0 
 //  
 // Unless required by applicable law or agreed to in writing, software 
 // distributed under the License is distributed on an "AS IS" BASIS, 
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 // See the License for the specific language governing permissions and 
 // limitations under the License.
 //
 package org.pathvisio.gui.swt;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.ActionContributionItem;
 import org.eclipse.jface.action.ControlContribution;
 import org.eclipse.jface.action.CoolBarManager;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.action.ToolBarContributionItem;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.window.ApplicationWindow;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.pathvisio.ApplicationEvent;
 import org.pathvisio.Engine;
 import org.pathvisio.Engine.ApplicationEventListener;
 import org.pathvisio.gui.BackpageTextProvider;
 import org.pathvisio.data.DBConnector;
 import org.pathvisio.data.DBConnectorSwt;
 import org.pathvisio.data.GdbEvent;
 import org.pathvisio.data.GdbManager;
 import org.pathvisio.data.GexManager;
 import org.pathvisio.data.GexSwt;
 import org.pathvisio.data.GdbManager.GdbEventListener;
 import org.pathvisio.data.GexManager.GexManagerEvent;
 import org.pathvisio.data.GexManager.GexManagerListener;
 import org.pathvisio.debug.Logger;
 import org.pathvisio.gui.swt.CommonActions;
 import org.pathvisio.gui.swt.SwtEngine;
 import org.pathvisio.gui.swt.awt.VPathwaySwingComposite;
import org.pathvisio.model.ConnectorType;
 import org.pathvisio.model.DataNodeType;
 import org.pathvisio.model.LineStyle;
 import org.pathvisio.model.LineType;
 import org.pathvisio.model.ShapeType;
 import org.pathvisio.preferences.GlobalPreference;
 import org.pathvisio.preferences.PreferenceManager;
 import org.pathvisio.search.PathwaySearchComposite;
 import org.pathvisio.util.swt.ProgressKeeperDialog;
 import org.pathvisio.view.AlignType;
 import org.pathvisio.view.DefaultTemplates;
 import org.pathvisio.view.GeneProduct;
 import org.pathvisio.view.StackType;
 import org.pathvisio.view.UndoManagerEvent;
 import org.pathvisio.view.UndoManagerListener;
 import org.pathvisio.view.VPathway;
 import org.pathvisio.view.VPathwayEvent;
 import org.pathvisio.view.VPathwayListener;
 import org.pathvisio.visualization.LegendPanel;
 
 /**
  * MainWindowBase is an abstract and incomplete Main Window that contains some
  * core functionality. This way we can create different flavours of the main window
  * without having too much duplicate code. Descendants should at least provide
  * a constructor, and override createCoolBarManager and createMenuManager.
  */
 //TODO: we mix coolbar and toolbar in this class. Evaluate and select one of the two for our needs.
 public class MainWindowBase extends ApplicationWindow implements 
 	ApplicationEventListener, GexManagerListener, GdbEventListener, VPathwayListener, UndoManagerListener
 															   
 {
 	private static final long serialVersionUID = 1L;
 		
 	protected CommonActions.UndoAction undoAction = new CommonActions.UndoAction(this);	
 	protected CommonActions.NewAction newAction = new CommonActions.NewAction (this);
 	protected CommonActions.OpenAction openAction = new CommonActions.OpenAction (this);	
 	protected CommonActions.ImportAction importAction = new CommonActions.ImportAction (this);	
 	protected CommonActions.SaveAction saveAction = new CommonActions.SaveAction(this);	
 	protected CommonActions.SaveAsAction saveAsAction = new CommonActions.SaveAsAction (this);
 	protected CommonActions.ExportAction exportAction = new CommonActions.ExportAction (this);
 	protected CommonActions.ExitAction exitAction = new CommonActions.ExitAction(this);
 	protected CommonActions.PreferencesAction preferencesAction = new CommonActions.PreferencesAction(this);
 	protected CommonActions.AboutAction aboutAction = new CommonActions.AboutAction(this);
 	protected CommonActions.CopyAction copyAction = new CommonActions.CopyAction(this);
 	protected CommonActions.HelpAction helpAction = new CommonActions.HelpAction(this);	
 	protected CommonActions.PasteAction pasteAction = new CommonActions.PasteAction(this);
 	protected CommonActions.SwitchEditModeAction switchEditModeAction = new CommonActions.SwitchEditModeAction(this);
 	public ShowRightPanelAction showRightPanelAction = new ShowRightPanelAction(this);
 	protected SelectGdbAction selectGdbAction = new SelectGdbAction(this);
 	protected AlignActions alignCenterXAction = new AlignActions(AlignType.CENTERX, this);
 	protected AlignActions alignCenterYAction = new AlignActions(AlignType.CENTERY, this);
 	protected AlignActions alignLeftAction = new AlignActions(AlignType.LEFT, this);
 	protected AlignActions alignRightAction = new AlignActions(AlignType.RIGHT, this);
 	protected AlignActions alignTopAction = new AlignActions(AlignType.TOP, this);
 	protected AlignActions alignBottomAction = new AlignActions(AlignType.BOTTOM, this);
 	protected AlignActions setCommonWidthAction = new AlignActions(AlignType.WIDTH, this);
 	protected AlignActions setCommonHeightAction = new AlignActions(AlignType.HEIGHT, this);
 	protected StackActions stackCenterXAction = new StackActions(StackType.CENTERX, this);
 	protected StackActions stackCenterYAction = new StackActions(StackType.CENTERY, this);
 	protected StackActions stackLeftAction = new StackActions(StackType.LEFT, this);
 	protected StackActions stackRightAction = new StackActions(StackType.RIGHT, this);
 	protected StackActions stackTopAction = new StackActions(StackType.TOP, this);
 	protected StackActions stackBottomAction = new StackActions(StackType.BOTTOM, this);
 
 	/**
 	 * {@link Action} to select a Gene Database
 	 */
 	private class SelectGdbAction extends Action
 	{
 		MainWindowBase window;
 		public SelectGdbAction(MainWindowBase w)
 		{
 			window = w;
 			setText("Select &Gene Database");
 			setToolTipText("Select Gene Database");
 		}
 		
 		public void run () {			
 			try {
 				DBConnectorSwt dbcon = SwtEngine.getCurrent().getSwtDbConnector(DBConnector.TYPE_GDB);
 				String dbName = dbcon.openChooseDbDialog(getShell());
 				
 				if(dbName == null) return;
 				
 				SwtEngine.getCurrent().getGdbManager().setGeneDb(dbName);
 			} catch(Exception e) {
 				String msg = "Failed to open Gene Database; " + e.getMessage();
 				MessageDialog.openError (window.getShell(), "Error", 
 						"Error: " + msg + "\n\n" + 
 						"See the error log for details.");
 				Logger.log.error(msg, e);
 			}
 		}
 	}
 
 	/**
 	 * deselect all buttons in the alignActionsCI coolbar
 	 */
 	public void deselectAlignItemActions()
 	{
 		IContributionItem[] items = alignActionsCI.getToolBarManager().getItems();
 		for(int i = 0; i < items.length; i++)
 		{
 			if(items[i] instanceof ActionContributionItem)
 			{
 				((ActionContributionItem)items[i]).getAction().setChecked(false);
 			}
 		}
 		Engine.getCurrent().getActiveVPathway().setNewTemplate(null);
 	}
 	
 	/**
 	 * {@link Action} to show or hide the right sidepanel
 	 */
 	public class ShowRightPanelAction extends Action
 	{
 		MainWindowBase window;
 		public ShowRightPanelAction (MainWindowBase w)
 		{
 			super("Show &information panel", IAction.AS_CHECK_BOX);
 			window = w;
 			setChecked(true);
 		}
 		
 		public void run() {
 			if(isChecked()) rightPanel.show();
 			else rightPanel.hide();
 		}
 	}
 
 	/**
 	 * Loads expression data for all {@link GeneProduct}s in the loaded pathway
 	 */
 	private void cacheExpressionData()
 	{
 		if(Engine.getCurrent().hasVPathway())
 		{
 			VPathway drawing = Engine.getCurrent().getActiveVPathway();
 			GdbManager gdbm = SwtEngine.getCurrent().getGdbManager();
 			//Check for necessary connections
 			if(GexManager.getCurrent().isConnected() && gdbm.isConnected())
 			{
 				ProgressKeeperDialog dialog = new ProgressKeeperDialog(getShell());
 				try {
 					dialog.run(
 							true, true, 
 							new GexSwt.CacheProgressKeeper(
 									drawing.getPathwayModel().getDataNodeXrefs(), gdbm.getCurrentGdb()
 								)
 					);
 					drawing.redraw();
 				} catch(Exception e) {
 					String msg = "while caching expression data: " + e.getMessage();					
 					MessageDialog.openError (getShell(), "Error", 
 							"Error: " + msg + "\n\n" + 
 							"See the error log for details.");
 					Logger.log.error(msg, e);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Deselects all {@link NewElementAction}s on the toolbar and sets 
 	 * {@link VPathway}.newGraphics to {@link VPathway}.NEWNONE
 	 */
 	public void deselectNewItemActions()
 	{
 		IContributionItem[] items = editActionsCI.getToolBarManager().getItems();
 		for(int i = 0; i < items.length; i++)
 		{
 			if(items[i] instanceof ActionContributionItem)
 			{
 				((ActionContributionItem)items[i]).getAction().setChecked(false);
 			}
 		}
 		Engine.getCurrent().getActiveVPathway().setNewTemplate(null);
 	}
 
 	// Elements of the coolbar
 	ToolBarContributionItem commonActionsCI;
 	ToolBarContributionItem editActionsCI;
 	ToolBarContributionItem alignActionsCI;
 	ToolBarContributionItem viewActionsCI;
 	
 	/**
 	 * Creates element of the coolbar containing common actions as new, save etc.
 	 */
 	protected void createCommonActionsCI()
 	{
 		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
 		toolBarManager.add(newAction);
 		toolBarManager.add(openAction);
 		toolBarManager.add(saveAction);
 		toolBarManager.add(undoAction);
 		commonActionsCI = new ToolBarContributionItem(toolBarManager, "CommonActions");
 	}
 
 	/**
 	 * Creates element of the coolbar only shown in edit mode (new element actions)
 	 */
 	protected void createEditActionsCI()
 	{
 		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);		
 		toolBarManager.add(new NewElementAction(
 				new DefaultTemplates.DataNodeTemplate(DataNodeType.GENEPRODUCT)));
 		toolBarManager.add(new NewElementAction(
 				new DefaultTemplates.DataNodeTemplate(DataNodeType.METABOLITE)));
 		toolBarManager.add(new NewElementAction(new DefaultTemplates.LabelTemplate()));
 		toolBarManager.add(new NewElementAction(NewElementAction.MENULINE));
 		toolBarManager.add(new NewElementAction(
 				new DefaultTemplates.ShapeTemplate(ShapeType.RECTANGLE)));
 		toolBarManager.add(new NewElementAction(
 				new DefaultTemplates.ShapeTemplate(ShapeType.OVAL)));
 		toolBarManager.add(new NewElementAction(
 				new DefaultTemplates.ShapeTemplate(ShapeType.ARC)));
 		toolBarManager.add(new NewElementAction(
 				new DefaultTemplates.ShapeTemplate(ShapeType.BRACE)));
 		toolBarManager.add(new NewElementAction(new DefaultTemplates.LineTemplate(
				"tbar", LineStyle.SOLID, LineType.LINE, LineType.TBAR, ConnectorType.STRAIGHT)));
 		toolBarManager.add(new NewElementAction(NewElementAction.MENULINESHAPE));
 		toolBarManager.add(new NewElementAction(new DefaultTemplates.InteractionTemplate()));
 
 
 		editActionsCI = new ToolBarContributionItem(toolBarManager, "EditModeActions");
 	}
 	
 	/**
 	 * Creates element of the coolbar containing controls related to viewing a pathway
 	 */
 	protected void createViewActionsCI()
 	{
 		final MainWindowBase window = this;
 		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
 		//Add zoomCombo
 		toolBarManager.add(new ControlContribution("ZoomCombo") {
 			protected Control createControl(Composite parent) {
 				final CCombo zoomCombo = new CCombo(parent, SWT.DROP_DOWN);
 				zoomCombo.setItems(new String[] { "200%", "100%", "75%", "50%", "Zoom to fit" });
 				zoomCombo.setText("100%");
 				zoomCombo.addSelectionListener(new SelectionAdapter() {
 					public void widgetSelected(SelectionEvent e) {
 						int pctZoom = 100;
 						String zoomText = zoomCombo.getText().replace("%", "");
 						try {
 							pctZoom = Integer.parseInt(zoomText);
 							new CommonActions.ZoomAction(window, pctZoom).run();
 						} catch (Exception ex) { 
 							if(zoomText.equals("Zoom to fit"))
 							{ 
 								new CommonActions.ZoomToFitAction(window).run(); 
 							} else { return; }
 						}
 						
 					}
 					public void widgetDefaultSelected(SelectionEvent e) { widgetSelected(e); }
 				});
 				return zoomCombo;
 			}
 		});		
 		viewActionsCI =  new ToolBarContributionItem(toolBarManager, "SwitchActions");
 	}
 		
 	/**
 	 * Shows or hides the editActionsCI
 	 * @param show	true/false for either show or hide
 	 */
 	public void showEditActionsCI(boolean show)
 	{
 		if(show) {
 			getCoolBarManager().insertAfter(viewActionsCI.getId(), editActionsCI);
 		}
 		else {
 			getCoolBarManager().remove(editActionsCI);
 		}
 //		showVisualizationCI(!show); //Visualizations can show up in edit mode...
 		getCoolBarManager().update(true);
 		getShell().layout();
 	}
 
 	/**
 	   Shows or hides the alignActionsCI.
 	   @param show	true/false for either show or hide
 	*/
 	public void showAlignActionsCI(boolean show)
 	{
 		if(show) {
 			getCoolBarManager().insertAfter(editActionsCI.getId(), alignActionsCI);
 		}
 		else {
 			getCoolBarManager().remove(alignActionsCI);
 		}
 //		showVisualizationCI(!show); //Visualizations can show up in edit mode...
 		getCoolBarManager().update(true);
 		//getContents().redraw();
 		getShell().layout();
 	}
 	
 	//	KH 20070514 begin
 	/**
 	 * set up the alignActions coolbar
 	 */
 	protected void createAlignActionsCI()
 	{
 		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
 		toolBarManager.add(alignCenterXAction);
 		toolBarManager.add(alignCenterYAction);
 		//toolBarManager.add(alignLeftAction);
 		//toolBarManager.add(alignRightAction);
 		//toolBarManager.add(alignTopAction);
 		//toolBarManager.add(alignBottomAction);
 		toolBarManager.add(setCommonWidthAction);
 		toolBarManager.add(setCommonHeightAction);
 		toolBarManager.add(stackCenterXAction);
 		toolBarManager.add(stackCenterYAction);
 		//toolBarManager.add(stackLeftAction);
 		//toolBarManager.add(stackRightAction);
 		//toolBarManager.add(stackTopAction);
 		//toolBarManager.add(stackBottomAction);
 
 	
 		alignActionsCI = new ToolBarContributionItem(toolBarManager, "AlignActions");
 	}
 	
 	protected Control createContents(Composite parent) {
 		Shell shell = parent.getShell();
 		shell.setSize(800, 600);
 		shell.setLocation(100, 100);
 		
 		GuiMain.loadImages(shell.getDisplay());
 		
 		shell.setImage(SwtEngine.getCurrent().getImageRegistry().get("shell.icon"));
 		
 		Composite viewComposite = new Composite(parent, SWT.NULL);
 		viewComposite.setLayout(new FillLayout());
 		
 		sashForm = new SashForm(viewComposite, SWT.HORIZONTAL);
 		
 		swingPathwayComposite = new VPathwaySwingComposite(sashForm, SWT.NONE);
 		
 		rightPanel = new TabbedSidePanel(sashForm, SWT.NULL);
 		
 		//rightPanel controls
 		BackpageTextProvider bpt = new BackpageTextProvider(Engine.getCurrent(), SwtEngine.getCurrent().getGdbManager(), GexManager.getCurrent());
 		bpBrowser = new BackpagePanel(rightPanel.getTabFolder(), SWT.NONE, bpt);
 		propertyTable = new PropertyPanel(
 				rightPanel.getTabFolder(), SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
 		pwSearchComposite = new PathwaySearchComposite(rightPanel.getTabFolder(), SWT.NONE, this);
 		legend = new LegendPanel(rightPanel.getTabFolder(), SWT.V_SCROLL | SWT.H_SCROLL);
 		
 		addPanelTabs();
 		
 		int sidePanelSize = PreferenceManager.getCurrent().getInt (GlobalPreference.GUI_SIDEPANEL_SIZE);
 		sashForm.setWeights(new int[] {100 - sidePanelSize, sidePanelSize});
 		showRightPanelAction.setChecked(sidePanelSize > 0);
 		
 		rightPanel.getTabFolder().setSelection(0); //select backpage browser tab
 		rightPanel.hideTab("Legend"); //hide legend on startup
 
 		updateStatusBar();
 
 		SwtEngine.getCurrent().updateTitle();
 
 		getCoolBarManager().setLockLayout(true);
 		return parent;
 	}
 	
 //	KH end
 	/**
 	   Invoked when user tries to close window.
 	   We'll ask the user if he wants to save the pathway
 	*/
 	protected boolean canHandleShellCloseEvent()
 	{
 		return SwtEngine.getCurrent().canDiscardPathway();
 	}
 	
 	protected MenuManager menuManager = null;
 	/**
 	 * can be accessed by plugins etc. 
 	 * to add menu items and even complete menus.
 	 * 
 	 * (plugin API)
 	 */
 	public MenuManager getMenuManager()
 	{
 		return menuManager;
 	}
 		
 	public boolean close() {
 		Engine.getCurrent().close(); // fire application close event
 		return super.close();
 	}
 	
 	VPathwaySwingComposite swingPathwayComposite;
 	public BackpagePanel bpBrowser; //Browser for showing backpage information
 	public PropertyPanel propertyTable;	//Table showing properties of Graphics objects
 	SashForm sashForm; //SashForm containing the drawing area and sidebar
 	TabbedSidePanel rightPanel; //side panel containing backbage browser and property editor
 	PathwaySearchComposite pwSearchComposite; //Composite that handles pathway searches and displays results
 	LegendPanel legend; //Legend to display colorset information
 	
 	public TabbedSidePanel getSidePanel() { return rightPanel; }
 	
 	public LegendPanel getLegend() { return legend; }
 	
 	public void showLegend(boolean show) {	
 		if(show && GexManager.getCurrent().isConnected()) 
 		{
 			if(rightPanel.isVisible("Legend")) return; //Legend already visible, only refresh
 			rightPanel.unhideTab("Legend", 0);
 			rightPanel.selectTab("Legend");
 		}
 		
 		else rightPanel.hideTab("Legend");
 	}
 					
 	public void applicationEvent(ApplicationEvent e) {
 		switch(e.getType())
 		{
 		case ApplicationEvent.PATHWAY_OPENED:
 			if(GexManager.getCurrent().isConnected()) cacheExpressionData();
 			break;
 		case ApplicationEvent.VPATHWAY_NEW:
 		case ApplicationEvent.VPATHWAY_OPENED:
 			Engine.getCurrent().getActiveVPathway().addVPathwayListener(this);
 			Engine.getCurrent().getActiveVPathway().getUndoManager().addListener(this);
 			break;
 		}
 	}
 	
 	public void gdbEvent (GdbEvent e)
 	{
 		if (e.getType() == GdbEvent.GDB_CONNECTED)
 		{
 			updateStatusBar();
 			cacheExpressionData();
 		}
 	}
 
 	public void gexManagerEvent(GexManagerEvent e) 
 	{
 		switch(e.getType()) 
 		{
 		case GexManagerEvent.CONNECTION_CLOSED:
 			getShell().getDisplay().syncExec(new Runnable() {
 				public void run() {
 					showLegend(false);
 				}
 			});
 			break;
 		case GexManagerEvent.CONNECTION_OPENED:
 			getShell().getDisplay().syncExec(new Runnable() {
 				public void run() {
 					cacheExpressionData();
 					showLegend(true);
 				}
 			});
 			break;
 		}
 	}
 
 	/**
 	   Update the status bar with information on the current Gene Database.
 	 */
 	private void updateStatusBar()
 	{
 		setStatus("Using Gene Database: '" +
 				  SwtEngine.getCurrent().getGdbManager().getCurrentGdb().getDbName() + "'");
 	}
 	
 	public void vPathwayEvent(VPathwayEvent e) {
 		switch(e.getType()) {
 		case VPathwayEvent.EDIT_MODE_OFF:
 			threadSafe(new Runnable() {
 				public void run() {
 					showLegend(true);
 					showEditActionsCI(false);
 					showAlignActionsCI(false);
 					// show backpage table in sidebar
 					rightPanel.getTabFolder().setSelection(0);
 				}
 			});
 			break;
 		case VPathwayEvent.EDIT_MODE_ON:
 			threadSafe(new Runnable() {
 				public void run() {
 					showLegend(false);
 					showEditActionsCI(true);
 					showAlignActionsCI(true);
 					// show property table in sidebar
 					rightPanel.getTabFolder().setSelection(1);
 				}
 			});
 			break;
 		case VPathwayEvent.ELEMENT_ADDED:
 			threadSafe(new Runnable() {
 				public void run() {
 					deselectNewItemActions();
 				}
 			});
 			break;
 		}
 	}
 
 	protected void threadSafe(Runnable r) {
 		Display d = getShell() == null ? Display.getDefault() : getShell().getDisplay();
 		if(Thread.currentThread() == d.getThread()) {
 			r.run();
 		} else {
 			d.syncExec(r);
 		}
 	}
 	
 	public void undoManagerEvent (UndoManagerEvent e)
 	{
 		undoAction.setText ("&Undo: " + e.getMessage() + "@Ctrl+Z");
 	}
 		
 	public MainWindowBase()
 	{
 		this(null);
 	}
 	
 	/**
 	 *Constructor for the MainWindow class
 	 *Initializes new MainWindow and sets properties for frame
 	 */
 	public MainWindowBase(Shell shell)
 	{
 		super(shell);
 		
 		addMenuBar();
 		addStatusLine();
 		addCoolBar(SWT.FLAT | SWT.LEFT);
 		
 		Engine.getCurrent().addApplicationEventListener(this);
 		SwtEngine.getCurrent().getGdbManager().addGdbEventListener(this);
 		GexManager.getCurrent().addListener(this);
 	}
 	
 	/**
 	 *Builds and ads a menu to the frame
 	 */
 	protected MenuManager createMenuManager()
 	{
 		menuManager = new MenuManager();
 		MenuManager fileMenu = new MenuManager ("&File");
 		fileMenu.add(newAction);
 		fileMenu.add(openAction);
 		fileMenu.add(saveAction);
 		fileMenu.add(saveAsAction);
 		fileMenu.add(new Separator());
 		fileMenu.add(importAction);
 		fileMenu.add(exportAction);
 		fileMenu.add(new Separator());
 		fileMenu.add(exitAction);
 		MenuManager editMenu = new MenuManager ("&Edit");
 		editMenu.add(undoAction);
 		editMenu.add(new Separator());
 		editMenu.add(copyAction);
 		editMenu.add(pasteAction);
 		editMenu.add(new Separator());
 		editMenu.add(switchEditModeAction);
 		editMenu.add(preferencesAction);
 		MenuManager viewMenu = new MenuManager ("&View");
 		viewMenu.add(showRightPanelAction);
 		MenuManager zoomMenu = new MenuManager("&Zoom");
 		zoomMenu.add(new CommonActions.ZoomAction(this, 50));
 		zoomMenu.add(new CommonActions.ZoomAction(this, 75));
 		zoomMenu.add(new CommonActions.ZoomAction(this, 100));
 		zoomMenu.add(new CommonActions.ZoomAction(this, 125));
 		zoomMenu.add(new CommonActions.ZoomAction(this, 150));
 		zoomMenu.add(new CommonActions.ZoomAction(this, 200));
 		zoomMenu.add(new CommonActions.ZoomToFitAction(this));
 		viewMenu.add(zoomMenu);
 		MenuManager dataMenu = new MenuManager ("&Data");
 		dataMenu.add(selectGdbAction);
 		
 		MenuManager helpMenu = new MenuManager ("&Help");
 		helpMenu.add(aboutAction);
 		helpMenu.add(helpAction);
 		menuManager.add(fileMenu);
 		menuManager.add(editMenu);
 		menuManager.add(viewMenu);
 		menuManager.add(dataMenu);
 		menuManager.add(helpMenu);
 		return menuManager;
 	}
 	
 	protected CoolBarManager createCoolBarManager(int style)
 	{
 		createCommonActionsCI();
 		createEditActionsCI();
 		createAlignActionsCI();
 		createViewActionsCI();
 		
 		CoolBarManager coolBarManager = new CoolBarManager(style);		
 		coolBarManager.add(commonActionsCI);
 		coolBarManager.add(viewActionsCI);
 		return coolBarManager;
 	}
 		
 	protected void addPanelTabs() {
 		rightPanel.addTab(bpBrowser, "Backpage");
 		rightPanel.addTab(propertyTable, "Properties");
 		rightPanel.addTab(pwSearchComposite, "Pathway Search");
 		rightPanel.addTab(legend, "Legend");
 	}
 
 	public boolean editOnOpen() {
 		return true; //Force edit mode on open pathways
 	}	
 }
