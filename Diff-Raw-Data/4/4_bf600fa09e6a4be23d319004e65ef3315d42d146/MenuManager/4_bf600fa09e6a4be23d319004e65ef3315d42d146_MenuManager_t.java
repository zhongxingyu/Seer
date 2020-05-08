 /** -----------------------------------------------------------------
  *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
  *    Copyright (C) 2011 Jérôme Wagener & Paul Bicheler
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
 
 package collector.desktop.controller.managers;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import collector.desktop.controller.filesystem.exporting.CSVExporter;
 import collector.desktop.controller.filesystem.exporting.HTMLExporter;
 import collector.desktop.controller.i18n.DictKeys;
 import collector.desktop.controller.i18n.Translator;
 import collector.desktop.model.database.exceptions.DatabaseWrapperOperationException;
 import collector.desktop.model.database.operations.DatabaseOperations;
 import collector.desktop.view.ApplicationUI;
 import collector.desktop.view.browser.BrowserFacade;
 import collector.desktop.view.sidepanes.AdvancedSearchSidepane;
 import collector.desktop.view.sidepanes.AlterAlbumSidepane;
 import collector.desktop.view.sidepanes.CreateAlbumSidepane;
 import collector.desktop.view.sidepanes.EmptySidepane;
 import collector.desktop.view.sidepanes.SettingsSidepane;
 import collector.desktop.view.sidepanes.SynchronizeSidepane;
 import collector.desktop.view.various.ComponentFactory;
 import collector.desktop.view.various.PanelType;
 
 public class MenuManager {
 	private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationUI.class);
 
 	/** This method creates and initializes the menu for the main user interface
 	 * @param shell the shell used to create the user interface */
 	public static void createAndInitializeMenuBar(Shell parentShell) {
 		// Create the bar menu itself
 		Menu menu = new Menu(ApplicationUI.getShell(), SWT.BAR);
 
 		// Create all the menu items for the bar menu
 		MenuItem collectorItem = new MenuItem(menu, SWT.CASCADE);
 		MenuItem albumItem = new MenuItem(menu, SWT.CASCADE);
 		MenuItem synchronizeItem = new MenuItem(menu, SWT.CASCADE);
 		MenuItem settingsItem = new MenuItem(menu, SWT.CASCADE);
 		MenuItem helpItem = new MenuItem(menu, SWT.CASCADE);
 
 		// Set the text labels for each of the main menu items
 		collectorItem.setText(Translator.get(DictKeys.MENU_COLLECTOR));
 		albumItem.setText(Translator.get(DictKeys.MENU_ALBUM));
 		synchronizeItem.setText(Translator.get(DictKeys.MENU_SYNCHRONIZE));
 		settingsItem.setText(Translator.get(DictKeys.MENU_SETTINGS));
 		helpItem.setText(Translator.get(DictKeys.MENU_HELP));
 
 		// Setup dropdown menus for the main menu items
 		createDropdownForCollectorItem(menu, collectorItem);
 		createDropdownForAlbumItem(menu, albumItem);
 		createDropdownSynchronizeItem(menu, synchronizeItem);
 		createDropdownSettingsItem(menu, settingsItem);
 		createDropdownHelpItem(menu, helpItem);
 
 		// Attach the menu bar to the given shell
 		ApplicationUI.getShell().setMenuBar(menu);
 	}
 
 	private static void createDropdownForCollectorItem(Menu menu, MenuItem collectorItem) {
 		Menu collectorMenu = new Menu(menu);
 		collectorItem.setMenu(collectorMenu);
 
 		MenuItem exportItem = new MenuItem(collectorMenu, SWT.NONE);
 		exportItem.setText(Translator.get(DictKeys.MENU_EXPORT_VISIBLE_ITEMS));
 		exportItem.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent evt) {
 				if (isAlbumSelectedAndShowMessageIfNot()) {
 					FileDialog saveFileDialog = new FileDialog(ApplicationUI.getShell(), SWT.SAVE);
 					saveFileDialog.setText(Translator.get(DictKeys.DIALOG_EXPORT_VISIBLE_ITEMS));
 					saveFileDialog.setFilterPath(System.getProperty("user.home"));
 					String[] filterExt = { "*.html", "*.csv"};
 					saveFileDialog.setFilterExtensions(filterExt);
 					String[] filterNames = { 
 							Translator.get(DictKeys.DIALOG_HTML_FOR_PRINT) , 
 							Translator.get(DictKeys.DIALOG_CSV_FOR_SPREADSHEET) };
 					saveFileDialog.setFilterNames(filterNames);
 	
 					String filepath = saveFileDialog.open();
 					if (filepath != null) {
 						if (filepath.endsWith(".csv")) {
 							CSVExporter.exportVisibleItems(filepath);
 						} else if (filepath.endsWith(".html")) {
 							HTMLExporter.exportVisibleItems(filepath);
 						}
 					}
 				}
 			}
 		});
 		
 		new MenuItem(collectorMenu, SWT.SEPARATOR);
 		
 		MenuItem backupItem = new MenuItem(collectorMenu, SWT.NONE);
 		backupItem.setText(Translator.get(DictKeys.MENU_BACKUP_ALBUMS_TO_FILE));
 		backupItem.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent evt) {
 				FileDialog saveFileDialog = new FileDialog(ApplicationUI.getShell(), SWT.SAVE);
 				saveFileDialog.setText(Translator.get(DictKeys.DIALOG_BACKUP_TO_FILE));
 				saveFileDialog.setFilterPath(System.getProperty("user.home"));
 				String[] filterExt = { "*.cbk" };
 				saveFileDialog.setFilterExtensions(filterExt);
 
 				String path = saveFileDialog.open();
 				if (path != null) {
 					try {
 						DatabaseIntegrityManager.backupToFile(path);
 					} catch (DatabaseWrapperOperationException ex) {
 						LOGGER.error("An error occured while trying to backup albums to a backup file", ex);
 					}
 				}
 			}
 		});
 		
 		MenuItem restoreItem = new MenuItem(collectorMenu, SWT.NONE);
 		restoreItem.setText(Translator.get(DictKeys.MENU_RESTORE_ALBUM_FROM_FILE));
 		restoreItem.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent evt) {
 				ApplicationUI.changeRightCompositeTo(PanelType.Empty, EmptySidepane.build(ApplicationUI.getThreePanelComposite()));
 
 				FileDialog openFileDialog = new FileDialog(ApplicationUI.getShell(), SWT.OPEN);
 				openFileDialog.setText(Translator.get(DictKeys.DIALOG_RESTORE_FROM_FILE));
 				openFileDialog.setFilterPath(System.getProperty("user.home"));
 				String[] filterExt = { "*.cbk" };
 				openFileDialog.setFilterExtensions(filterExt);
 
 				String path = openFileDialog.open();
 				if (path != null) {
 					try {
 						DatabaseIntegrityManager.restoreFromFile(path);
 						AlbumViewManager.initialize();
 					} catch (DatabaseWrapperOperationException ex) {
 						LOGGER.error("An error occured while trying to restore albums from a backup file", ex);
 					}
 					// No default album is selected on restore
 					ApplicationUI.refreshAlbumList();
 					BrowserFacade.showAlbumRestoredPage();
 				}
 			}
 		});
 		
 		new MenuItem(collectorMenu, SWT.SEPARATOR);
 		
 		MenuItem exitItem = new MenuItem(collectorMenu, SWT.NONE);
 		exitItem.setText(Translator.get(DictKeys.MENU_EXIT));
 		exitItem.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				ApplicationUI.getShell().close();
 			}
 		});
 	}
 
 	private static void createDropdownForAlbumItem(Menu menu, MenuItem albumItem) {
 		Menu albumMenu = new Menu(menu);
 		albumItem.setMenu(albumMenu);
 
 		MenuItem advancedSearch = new MenuItem(albumMenu, SWT.NONE);
 		advancedSearch.setText(Translator.get(DictKeys.MENU_ADVANCED_SEARCH));
 		advancedSearch.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				if (isAlbumSelectedAndShowMessageIfNot()) {
 					ApplicationUI.changeRightCompositeTo(PanelType.AdvancedSearch, AdvancedSearchSidepane.build(
 							ApplicationUI.getThreePanelComposite(), ApplicationUI.getSelectedAlbum()));
 				}
 			}
 		});
 		
 		new MenuItem(albumMenu, SWT.SEPARATOR);
 		
 		MenuItem createNewAlbumItem = new MenuItem(albumMenu, SWT.NONE);
 		createNewAlbumItem.setText(Translator.get(DictKeys.MENU_CREATE_NEW_ALBUM));
 		createNewAlbumItem.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				ApplicationUI.changeRightCompositeTo(
 						PanelType.AddAlbum, CreateAlbumSidepane.build(ApplicationUI.getThreePanelComposite()));
 			}
 		});
 		
 		MenuItem alterAlbum = new MenuItem(albumMenu, SWT.NONE);
 		alterAlbum.setText(Translator.get(DictKeys.MENU_ALTER_SELECTED_ALBUM));
 		alterAlbum.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				if (isAlbumSelectedAndShowMessageIfNot()) {
 					ApplicationUI.changeRightCompositeTo(PanelType.AlterAlbum, AlterAlbumSidepane.build(
 							ApplicationUI.getThreePanelComposite(), ApplicationUI.getSelectedAlbum()));
 				}
 			}
 		});
 		
 		new MenuItem(albumMenu, SWT.SEPARATOR);
 		
 		MenuItem deleteAlbum = new MenuItem(albumMenu, SWT.NONE);
 		deleteAlbum.setText(Translator.get(DictKeys.MENU_DELETE_SELECTED_ALBUM));
 		deleteAlbum.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				if (isAlbumSelectedAndShowMessageIfNot()) {					
 					MessageBox messageBox = ComponentFactory.getMessageBox(
 							ApplicationUI.getShell(), 
 							Translator.get(DictKeys.DIALOG_TITLE_DELETE_ALBUM), 
 							Translator.get(DictKeys.DIALOG_CONTENT_DELETE_ALBUM, ApplicationUI.getSelectedAlbum()), 
 							SWT.ICON_WARNING | SWT.YES | SWT.NO);
 					
 					if (messageBox.open() == SWT.YES) {
 						try {
							AlbumViewManager.removeAlbumViewsFromAlbum(ApplicationUI.getSelectedAlbum());
 							DatabaseOperations.removeAlbumAndAlbumPictures(ApplicationUI.getSelectedAlbum());
 							BrowserFacade.showAlbumDeletedPage(ApplicationUI.getSelectedAlbum());
 							ApplicationUI.refreshAlbumList();
 						} catch (DatabaseWrapperOperationException ex) {
							LOGGER.error("A database error occured while removing the following album: '" + ApplicationUI.getSelectedAlbum() + "'", ex);
 						}
 					}
 				}
 			}
 		});	
 
 	}
 
 	private static void createDropdownSynchronizeItem(Menu menu, MenuItem synchronizeItem) {
 		Menu synchronizeMenu = new Menu(menu);
 		synchronizeItem.setMenu(synchronizeMenu);
 
 		MenuItem Synchronize = new MenuItem(synchronizeMenu, SWT.NONE);
 		Synchronize.setText(Translator.get(DictKeys.MENU_SYNCHRONIZE));
 		Synchronize.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				ApplicationUI.changeRightCompositeTo(
 						PanelType.Synchronization, SynchronizeSidepane.build(ApplicationUI.getThreePanelComposite()));
 			}
 		});
 	}
 
 	private static void createDropdownSettingsItem(Menu menu, MenuItem settingsItem) {
 		Menu settingsMenu = new Menu(menu);
 		settingsItem.setMenu(settingsMenu);
 
 		MenuItem settings = new MenuItem(settingsMenu, SWT.NONE);
 		settings.setText(Translator.get(DictKeys.MENU_SETTINGS));
 		settings.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				ApplicationUI.changeRightCompositeTo(
 						PanelType.Settings, SettingsSidepane.build(ApplicationUI.getThreePanelComposite()));
 			}
 		});
 	}
 
 	private static void createDropdownHelpItem(Menu menu, MenuItem helpItem) {
 		Menu helpMenu = new Menu(menu);
 		helpItem.setMenu(helpMenu);
 
 		MenuItem helpContentsMenu = new MenuItem(helpMenu, SWT.NONE);
 		helpContentsMenu.setText(Translator.get(DictKeys.MENU_HELP_CONTENTS));
 		helpContentsMenu.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				// No default album is selected on help
 				ApplicationUI.refreshAlbumList();
 				BrowserFacade.loadHtmlFromInputStream(
 						ApplicationUI.getShell().getClass().getClassLoader().getResourceAsStream("htmlfiles/help.html"));
 				ApplicationUI.changeRightCompositeTo(PanelType.Help, EmptySidepane.build(ApplicationUI.getThreePanelComposite()));
 			}
 		});
 
 		MenuItem aboutMenu = new MenuItem(helpMenu, SWT.NONE);
 		aboutMenu.setText(Translator.get(DictKeys.MENU_ABOUT));
 		aboutMenu.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {
 				// No default album is selected on help
 				ApplicationUI.refreshAlbumList();
 				BrowserFacade.loadHtmlFromInputStream(
 						ApplicationUI.getShell().getClass().getClassLoader().getResourceAsStream("htmlfiles/about.html"));
 				ApplicationUI.changeRightCompositeTo(PanelType.Help, EmptySidepane.build(ApplicationUI.getThreePanelComposite()));
 			}
 		});
 	}
 
 	private static boolean isAlbumSelectedAndShowMessageIfNot() {
 		if (!ApplicationUI.hasSelectedAlbum()) {
 			ComponentFactory.showErrorDialog(
 					ApplicationUI.getShell(), 
 					Translator.get(DictKeys.DIALOG_TITLE_NO_ALBUM_SELECTED), 
 					Translator.get(DictKeys.DIALOG_CONTENT_NO_ALBUM_SELECTED));
 
 			return false;
 		}
 
 		return true;
 	}
 }
