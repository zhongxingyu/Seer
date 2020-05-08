 /** -----------------------------------------------------------------
  *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
  *    Copyright (C) 2011 Jerome Wagener & Paul Bicheler
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ** ----------------------------------------------------------------- */
 
 package org.sammelbox.view;
 
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.util.HashMap;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Layout;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Monitor;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.sammelbox.controller.GuiController;
 import org.sammelbox.controller.events.EventObservable;
 import org.sammelbox.controller.events.Observer;
 import org.sammelbox.controller.events.SammelboxEvent;
 import org.sammelbox.controller.filesystem.FileSystemLocations;
 import org.sammelbox.controller.i18n.DictKeys;
 import org.sammelbox.controller.i18n.Translator;
 import org.sammelbox.controller.listeners.BrowserListener;
 import org.sammelbox.controller.managers.AlbumManager;
 import org.sammelbox.controller.managers.AlbumViewManager;
 import org.sammelbox.controller.managers.AlbumViewManager.AlbumView;
 import org.sammelbox.controller.managers.DatabaseIntegrityManager;
 import org.sammelbox.controller.managers.MenuManager;
import org.sammelbox.model.GuiState;
 import org.sammelbox.model.database.QueryBuilder;
 import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
 import org.sammelbox.model.database.operations.DatabaseOperations;
 import org.sammelbox.view.browser.BrowserFacade;
 import org.sammelbox.view.composites.BrowserComposite;
 import org.sammelbox.view.composites.StatusBarComposite;
 import org.sammelbox.view.composites.ToolbarComposite;
 import org.sammelbox.view.sidepanes.EmptySidepane;
 import org.sammelbox.view.sidepanes.QuickControlSidepane;
 import org.sammelbox.view.various.ComponentFactory;
 import org.sammelbox.view.various.PanelType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public final class ApplicationUI implements Observer {	
 	private static final Logger logger = LoggerFactory.getLogger(ApplicationUI.class);
 	/** A reference to the main display */
 	private static final Display display = new Display();
 	/** A reference to the main shell */
 	private static final Shell shell = new Shell(display);
 	
 	/** A reference to the SWT list containing all available albums */
 	private static List albumList;
 	/** A reference to the SWT Text representing the quickSearch field*/
 	private static Text quickSearchTextField;
 	/** A reference to the SWT list containing all available views */
 	private static List viewList;
 	
 	/** A reference to a composite being part of the general user interface */
 	private static Composite threePanelComposite = null, upperLeftSubComposite = null, lowerLeftSubComposite = null, 
 			leftComposite = null, rightComposite = null, centerComposite = null, statusComposite = null;
 	private static ToolbarComposite toolbarComposite = null;
 	/** A reference to the SWT browser in charge of presenting album items */
 	private static Browser albumItemBrowser;
 	/** A reference to the SWT album item browser listener*/
 	private static BrowserListener albumItemBrowserListener;
 	/** The panel type that is currently visible on the right of the main three panel composite */
 	private static PanelType currentRightPanelType = PanelType.EMPTY;
 	/** An instance in order to register as an observer to event observable */
 	private static ApplicationUI instance = null;
 	
 	/** Defines the panel size for the different panel types */
 	private static HashMap<PanelType, Integer> panelTypeToPixelSize = new HashMap<PanelType, Integer>() {
 		private static final long serialVersionUID = 1L;	{
 			put(PanelType.EMPTY, UIConstants.RIGHT_PANEL_NO_WIDTH);
 			put(PanelType.ADD_ALBUM, UIConstants.RIGHT_PANEL_LARGE_WIDTH);
 			put(PanelType.ADD_ENTRY, UIConstants.RIGHT_PANEL_LARGE_WIDTH);
 			put(PanelType.ADVANCED_SEARCH, UIConstants.RIGHT_PANEL_LARGE_WIDTH);
 			put(PanelType.ALTER_ALBUM, UIConstants.RIGHT_PANEL_LARGE_WIDTH);
 			put(PanelType.SYNCHRONIZATION, UIConstants.RIGHT_PANEL_MEDIUM_WIDTH);
 			put(PanelType.UPDATE_ENTRY, UIConstants.RIGHT_PANEL_LARGE_WIDTH);
 			put(PanelType.HELP, UIConstants.RIGHT_PANEL_SMALL_WIDTH);
 			put(PanelType.SETTINGS, UIConstants.RIGHT_PANEL_LARGE_WIDTH);
 			put(PanelType.IMPORT, UIConstants.RIGHT_PANEL_LARGE_WIDTH);
 		}
 	};
 	
 	private ApplicationUI() {
 		EventObservable.registerObserver(this);
 	}
 
 	public void unregisterFromObservables() {
 		EventObservable.unregisterObserver(instance);
 	}
 	
 	/** This method initializes the main user interface. This involves the creation of different sub-composites
 	 * @param shell the shell which should be initialized */
 	public static void initialize(final Shell shell) {
 		initialize(shell, true);
 	}
 		
 	/** This method initializes the main user interface. This involves the creation of different sub-composites
 	 * @param shell the shell which should be initialized
 	 * @param showShell true if the shell should be displayed, false otherwise (for testing purposes only) */
 	public static void initialize(final Shell shell, boolean showShell) {				
 		instance = new ApplicationUI();		
 		
 		// set program icon
 		shell.setImage(new Image(shell.getDisplay(), FileSystemLocations.getLogoSmallPNG()));
 		
 		// setup the Layout for the shell
 		GridLayout shellGridLayout = new GridLayout(1, false);
 		shellGridLayout.marginHeight = 0;
 		shellGridLayout.marginWidth = 0;
 		shell.setMinimumSize(UIConstants.MIN_SHELL_WIDTH, UIConstants.MIN_SHELL_HEIGHT);
 
 		// setup the Shell
 		shell.setText(Translator.get(DictKeys.TITLE_MAIN_WINDOW));				
 		shell.setLayout(shellGridLayout);
 		
 		// define toolbar composite layout data
 		GridData gridDataForToolbarComposite = new GridData(GridData.FILL_BOTH);
 		gridDataForToolbarComposite.grabExcessHorizontalSpace = true;
 		gridDataForToolbarComposite.grabExcessVerticalSpace = false;
 		
 		// define three panel composite layout data
 		GridData gridDataForThreePanelComposite = new GridData(GridData.FILL_BOTH);
 		gridDataForThreePanelComposite.grabExcessHorizontalSpace = true;
 		gridDataForThreePanelComposite.grabExcessVerticalSpace = true;
 		gridDataForThreePanelComposite.verticalAlignment = GridData.FILL;
 		gridDataForThreePanelComposite.horizontalAlignment = GridData.FILL;
 		GridLayout mainGridLayout = new GridLayout(3, false);
 
 		// define left (upper & lower) composite layout data
 		GridData gridDataForLeftComposite = new GridData(GridData.FILL_BOTH);
 		gridDataForLeftComposite.grabExcessHorizontalSpace = false;
 		GridData gridDataForUpperLeftComposite = new GridData(GridData.FILL_BOTH);
 		gridDataForUpperLeftComposite.verticalAlignment = GridData.BEGINNING;
 		GridData gridDataForLowerLeftComposite = new GridData(GridData.FILL_BOTH);
 		gridDataForLowerLeftComposite.verticalAlignment = GridData.END;
 
 		// define center composite layout data
 		GridData gridDataForCenterComposite = new GridData(SWT.FILL, SWT.FILL, true, true);
 
 		// define right composite layout data
 		GridData gridDataForRightComposite = new GridData(GridData.FILL_BOTH);
 		gridDataForRightComposite.grabExcessHorizontalSpace = false;
 		gridDataForRightComposite.verticalAlignment = GridData.BEGINNING;
 
 		// define statusbar composite layout data
 		GridData gridDataForStatusBarComposite = new GridData(GridData.FILL_BOTH);
 		gridDataForStatusBarComposite.grabExcessHorizontalSpace = true;
 		gridDataForStatusBarComposite.grabExcessVerticalSpace = false;
 
 		// Setup composites using layout definitions from before
 		toolbarComposite = new ToolbarComposite(shell);
 		GridLayout toolbarGridLayout = new GridLayout(1, false);
 		toolbarGridLayout.marginHeight = 0;
 		toolbarComposite.setLayout(toolbarGridLayout);
 		toolbarComposite.setLayoutData(gridDataForToolbarComposite);
 		
 		threePanelComposite = new Composite(shell, SWT.NONE);
 		threePanelComposite.setLayout(mainGridLayout);
 		threePanelComposite.setLayoutData(gridDataForThreePanelComposite);
 
 		leftComposite = new Composite(threePanelComposite, SWT.NONE);
 		leftComposite.setLayout(new GridLayout(1, false));
 		leftComposite.setLayoutData(gridDataForLeftComposite);
 		upperLeftSubComposite = QuickControlSidepane.build(leftComposite);
 		upperLeftSubComposite.setLayoutData(gridDataForUpperLeftComposite);
 		lowerLeftSubComposite = EmptySidepane.build(leftComposite);		
 		lowerLeftSubComposite.setLayoutData(gridDataForLowerLeftComposite);
 		albumItemBrowserListener = new BrowserListener(threePanelComposite);
 		centerComposite = BrowserComposite.buildAndStore(threePanelComposite, albumItemBrowserListener);
 		centerComposite.setLayout(new GridLayout(1, false));
 		centerComposite.setLayoutData(gridDataForCenterComposite);
 		rightComposite = EmptySidepane.build(threePanelComposite);
 		rightComposite.setLayout(new GridLayout(1, false));
 		rightComposite.setLayoutData(gridDataForRightComposite);
 
 		statusComposite = StatusBarComposite.getInstance(shell).getStatusbarComposite();
 		statusComposite.setLayout(new GridLayout(1, false));
 		statusComposite.setLayoutData(gridDataForStatusBarComposite);
 
 		// Create the menu bar
 		MenuManager.createAndInitializeMenuBar(shell);
 
 		// center the shell to primary screen
 		Rectangle primaryScreenClientArea = getPrimaryScreenClientArea();
 		int xCoordinateForShell = primaryScreenClientArea.width / 2 - shell.getBounds().width / 2;
 		int yCoordinateForShell = primaryScreenClientArea.height / 2 - shell.getBounds().height / 2;
 		shell.setLocation(xCoordinateForShell, yCoordinateForShell);
 
 		// Create the album manager
 		AlbumManager.initialize();
 		for (String albumName : AlbumManager.getAlbums()) {
 			albumList.add(albumName);
 		}
 		
 		// Create the album view manager
 		AlbumViewManager.initialize();
 		
 		// SWT display management
 		shell.pack();
 
 		Rectangle displayClientArea = display.getPrimaryMonitor().getClientArea();
 		if (maximizeShellOnStartUp(displayClientArea.width, displayClientArea.height)){
 			shell.setMaximized(true);
 		}
 		
 		if (showShell) {
 			shell.open();
 	
 			selectDefaultAndShowWelcomePage();		
 	
 			while (!shell.isDisposed()) {
 				if (!display.readAndDispatch()) {
 					display.sleep();
 				}
 			}
 	
 			display.dispose();
 		}
 		
 		try {
 			DatabaseIntegrityManager.backupAutoSave();
 		} catch (DatabaseWrapperOperationException e) {
 			logger.error("Couldn't create an auto save of the database file", e);
 		}
 	}
 
 	public static Composite getThreePanelComposite() {
 		return threePanelComposite;
 	}
 
 	public static void selectDefaultAndShowWelcomePage() {
 		if (albumList.getItemCount() > 0) {
 			albumList.setSelection(-1);
 		}
 
 		BrowserFacade.loadWelcomePage();
 	}
 	
 	/** This method exchanges the center composite with a composite provided as parameter. Hereby, the previous composite is disposed. 
 	 * @param newCenterComposite the new composite for the center element of the user interface */
 	public static void changeCenterCompositeTo(Composite newCenterComposite) {
 		Layout layout = centerComposite.getLayout();
 		GridData layoutData = (GridData) centerComposite.getLayoutData();
 
 		centerComposite.dispose();
 		newCenterComposite.setLayout(layout);
 		newCenterComposite.setLayoutData(layoutData);
 
 		centerComposite = newCenterComposite;
 		centerComposite.moveAbove(rightComposite);
 		centerComposite.getParent().layout();
 	}
 
 	/** This method exchanges the lower left composite with a composite provided as parameter. Hereby, the previous composite is disposed. 
 	 * @param newLowerLeftComposite the new composite for the lower left element of the user interface */
 	public static void changeLowerLeftCompositeTo(Composite newLowerLeftComposite) {
 		Layout layout = lowerLeftSubComposite.getLayout();
 		GridData layoutData = (GridData) lowerLeftSubComposite.getLayoutData();
 
 		lowerLeftSubComposite.dispose();
 		newLowerLeftComposite.setLayout(layout);
 		newLowerLeftComposite.setLayoutData(layoutData);
 
 		lowerLeftSubComposite = newLowerLeftComposite;
 		lowerLeftSubComposite.moveBelow(upperLeftSubComposite);
 		lowerLeftSubComposite.getParent().layout();
 	}
 
 	public static void resizeRightCompositeTo(int pixels) {
 		GridData layoutData = new GridData(GridData.FILL_BOTH);
 		layoutData.grabExcessHorizontalSpace = false;
 		layoutData.grabExcessVerticalSpace = true;
 		layoutData.verticalAlignment = GridData.BEGINNING;
 		layoutData.widthHint = pixels;
 
 		rightComposite.setLayoutData(layoutData);
 		rightComposite.getParent().layout();
 	}
 
 	/** This method exchanges the right composite with a composite provided as parameter. Hereby, the previous composite is disposed. 
 	 * @param newRightComposite the new composite for the right element of the user interface */
 	public static void changeRightCompositeTo(PanelType panelType, Composite newRightComposite) {
 		currentRightPanelType = panelType;
 
 		GridData layoutData = new GridData(GridData.FILL_BOTH);
 		layoutData.grabExcessHorizontalSpace = false;
 		layoutData.grabExcessVerticalSpace = true;
 		layoutData.verticalAlignment = GridData.BEGINNING;
 
 		if (panelTypeToPixelSize.containsKey(panelType)) {
 			if (ScrolledComposite.class.isInstance(newRightComposite)) {
 				ScrolledComposite sc = (ScrolledComposite) newRightComposite;
 				layoutData.widthHint = panelTypeToPixelSize.get(panelType) - sc.getVerticalBar().getSize().x;
 			} else {		
 				layoutData.widthHint = panelTypeToPixelSize.get(panelType);
 			}
 		} else {
 			if (ScrolledComposite.class.isInstance(newRightComposite)) {
 				ScrolledComposite sc = (ScrolledComposite) newRightComposite;
 				layoutData.widthHint = UIConstants.RIGHT_PANEL_MEDIUM_WIDTH - sc.getVerticalBar().getSize().x;
 			} else {		
 				layoutData.widthHint = UIConstants.RIGHT_PANEL_MEDIUM_WIDTH;
 			}
 		}
 
 		newRightComposite.setLayoutData(layoutData);
 
 		rightComposite.dispose();
 		rightComposite = newRightComposite;
 		rightComposite.moveBelow(centerComposite);
 		rightComposite.getParent().layout();
 
 		EventObservable.addEventToQueue(SammelboxEvent.RIGHT_SIDEPANE_CHANGED);
 	}
 
 	/** Returns the currently selected/active album or view
 	 * @return the currently selected/active album or view */
 	public static String getSelectedAlbum() {
 		return GuiController.getGuiState().getSelectedAlbum();
 	}
 
 	public static void setQuickSearchTextField(Text quickSearchTextField) {
 		ApplicationUI.quickSearchTextField = quickSearchTextField;
 	}
 	
 	public static Text getQuickSearchTextField() {
 		return ApplicationUI.quickSearchTextField;
 	}
 
 	/** Sets the currently selected/active album
 	 * @param albumName The name of the now selected/active album. If the albumName is null or empty then all albums are deselected.  
 	 * @return True if the album is selected internally and in the SWT Album list. If all albums were successfully deselected then true is also returned. 
 	 * False otherwise.*/
 	public static boolean setSelectedAlbum(String albumName) {
 		// Set the album name and verify that it is in the list
 		GuiController.getGuiState().setSelectedAlbum(albumName);
 		if (albumName== null || albumName.isEmpty()) {
 			ApplicationUI.albumList.deselectAll();
 			return true;
 		}
 		
		// Reset view
		GuiController.getGuiState().setSelectedView(GuiState.NO_VIEW_SELECTED);
		
 		int albumListItemCount = ApplicationUI.albumList.getItemCount();
 		boolean albumSelectionIsInSync = false;
 		for (int itemIndex = 0; itemIndex<albumListItemCount; itemIndex++) {
 			 if (ApplicationUI.albumList.getItem(itemIndex).equals(albumName) ) {
 				 ApplicationUI.albumList.setSelection(itemIndex);
 				 albumSelectionIsInSync = true;
 				 break;
 			 }
 		}
 		
 		if (!albumSelectionIsInSync) {
 			logger.error("The album list does not contain the album that is supposed to be selected");
 			return false;
 		}
 	
 		if (!ApplicationUI.getQuickSearchTextField().getText().isEmpty()) {
 			ApplicationUI.getQuickSearchTextField().setText("");
 		}
 		
 		try {
 			ApplicationUI.getQuickSearchTextField().setEnabled(DatabaseOperations.isAlbumQuicksearchable(albumName));
 		} catch (DatabaseWrapperOperationException ex) {
 			logger.error("An error occured while enabling the quick search field", ex);
 		}
 		
 		BrowserFacade.performBrowserQueryAndShow(QueryBuilder.createSelectStarQuery(albumName));
 		
 		ApplicationUI.getViewList().setEnabled(AlbumViewManager.hasAlbumViewsAttached(albumName));
 		EventObservable.addEventToQueue(SammelboxEvent.ALBUM_SELECTED);
 		toolbarComposite.enableAlbumButtons(albumName);
 		
 		return true;
 	}
 	
 	/** After adding/removing albums, this method should be used to refresh the album list with the current album names thus leaving no album selected.*/
 	public static void refreshAlbumList() {
 		EventObservable.addEventToQueue(SammelboxEvent.ALBUM_LIST_UPDATED);
 		EventObservable.addEventToQueue(SammelboxEvent.ALBUM_VIEW_LIST_UPDATED);
 		ApplicationUI.getQuickSearchTextField().setEnabled(false);
 	}
 
 	/** Sets the the list of albums
 	 * @param albumList the list of albums */ 
 	public static void setAlbumList(List albumList) {
 		ApplicationUI.albumList = albumList;
 	}
 
 	/** Sets the the list of views
 	 * @param albumList the list of albums */ 
 	public static void setViewList(List viewList) {
 		ApplicationUI.viewList = viewList;
 	}
 
 	/** Returns the list of views 
 	 * @return the album list */
 	public static List getViewList() {
 		return viewList;
 	}
 
 	/** Sets the album item browser
 	 * @param browser the reference to the albumItemBrowser */
 	public static void setAlbumItemBrowser(Browser browser) {
 		ApplicationUI.albumItemBrowser = browser;
 	}
 
 	/** Returns the album item browser
 	 * @return the album item browser */
 	public static Browser getAlbumItemBrowser() {
 		return albumItemBrowser;
 	}
 	
 	public static PanelType getCurrentRightPanelType() {
 		return currentRightPanelType;
 	}
 
 	public static void setCurrentRightPanelType(PanelType currentRightPanel) {
 		ApplicationUI.currentRightPanelType = currentRightPanel;
 	}
 
 	public static Shell getShell() {
 		return shell;
 	}
 	
 	/**
 	 * When the horizontal or vertical screen resolution is smaller than their respective thresholds
 	 * {@value #MIN_DISPLAY_WIDTH_BEFORE_MAXIMIZE} and {@link #MIN_DISPLAY_HEIGHT_BEFORE_MAXIMIZE} then
 	 * it returns true, False otherwise.
 	 */
 	public static boolean maximizeShellOnStartUp(int screenWidth, int screenHeight) {
 		if (UIConstants.MIN_SHELL_WIDTH >= screenWidth || UIConstants.MIN_SHELL_HEIGHT >= screenHeight){
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public void update(SammelboxEvent event) {		
 		if (event.equals(SammelboxEvent.ALBUM_LIST_UPDATED)) {
 			albumList.removeAll();
 			for (String album : AlbumManager.getAlbums()) {
 				albumList.add(album);
 			}
 		} else if (event.equals(SammelboxEvent.ALBUM_SELECTED)) {			
 			viewList.setItems(AlbumViewManager.getAlbumViewNamesArray(GuiController.getGuiState().getSelectedAlbum()));
 			BrowserFacade.resetFutureJumpAnchor();
 		} else if (event.equals(SammelboxEvent.ALBUM_VIEW_SELECTED)) {
 			BrowserFacade.resetFutureJumpAnchor();
 		} else if (event.equals(SammelboxEvent.ALBUM_VIEW_LIST_UPDATED)) {
 			viewList.removeAll();
 
 			for (AlbumView albumView : AlbumViewManager.getAlbumViews(GuiController.getGuiState().getSelectedAlbum())) {
 				viewList.add(albumView.getName());				
 			}
 			
 			if (viewList.isEnabled() == false && viewList.getItemCount() != 0) {
 				viewList.setEnabled(true);
 			}
 		} else if (event.equals(SammelboxEvent.NO_ALBUM_SELECTED)) {
 			ComponentFactory.showErrorDialog(
 					ApplicationUI.getShell(), 
 					Translator.get(DictKeys.DIALOG_TITLE_NO_ALBUM_SELECTED), 
 					Translator.get(DictKeys.DIALOG_CONTENT_NO_ALBUM_SELECTED));
 		}
 	}
 	
 	// TODO: ugly hack for alpha.
 	public static boolean isAlbumSelectedAndShowMessageIfNot() {
 		if (!GuiController.getGuiState().isAlbumSelected()) {
 			EventObservable.addEventToQueue(SammelboxEvent.NO_ALBUM_SELECTED);
 			return false;
 		}
 		return true;
 	}
 	
 	private static Rectangle getPrimaryScreenClientArea() {
 		Monitor primaryMonitorBySwt = display.getPrimaryMonitor();
 		Rectangle primaryMonitorClientAreaBySwt = primaryMonitorBySwt.getClientArea();
 		GraphicsDevice[]screens =  GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
 		  for (GraphicsDevice screen : screens) {
 			  if (isPrimaryMonitor(screen)) {
 				  // Cut off any excess area such as OS task-bars. 
 				  Rectangle primaryScreenBoundsByJava = new Rectangle(	
 						screen.getDefaultConfiguration().getBounds().x,
 					   	screen.getDefaultConfiguration().getBounds().y,
 					   	screen.getDefaultConfiguration().getBounds().width,
 						screen.getDefaultConfiguration().getBounds().height);
 				 
 				  return primaryMonitorClientAreaBySwt.intersection(primaryScreenBoundsByJava);				 
 			  } 			 
 		  }
 		  
 		  // No primary screen has been found by java, use SWT to get clientArea of PrimaryScreen as fallback.
 		  return primaryMonitorClientAreaBySwt;
 	}	
 	
 	private static boolean isPrimaryMonitor(GraphicsDevice screen) {
 		java.awt.Rectangle screenBounds = screen.getDefaultConfiguration().getBounds();
 		
 		// If the top left corner of the screen is (0,0) that means we consider it the primary screen.
 		// The x,y might be negative too depending on the virtual screens.
 		if (screenBounds.getX() == 0 && screenBounds.getY() == 0) {
 			return true;
 		}
 		return false;
 	}
 }
