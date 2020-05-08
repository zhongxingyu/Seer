 package collector.desktop.gui;
 
 import java.net.URI;
 import java.sql.Date;
 import java.sql.Time;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.HashMap;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.events.TraverseEvent;
 import org.eclipse.swt.events.TraverseListener;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.DateTime;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 
 import collector.desktop.Collector;
 import collector.desktop.database.AlbumItem;
 import collector.desktop.database.DatabaseWrapper;
 import collector.desktop.database.FieldType;
 import collector.desktop.database.MetaItemField;
 import collector.desktop.database.OptionType;
 import collector.desktop.database.StarRating;
 import collector.desktop.gui.AlbumViewManager.AlbumView;
 import collector.desktop.gui.QueryBuilder.QueryComponent;
 import collector.desktop.gui.QueryBuilder.QueryOperator;
 import collector.desktop.networking.NetworkGateway;
 
 public class CompositeFactory {
 	private static final int SCROLL_SPEED_MULTIPLICATOR = 3;
 
 	/** Returns a quick control composite (select-album-list, quick-search) used by the GUI 
 	 * @param parentComposite the parent composite
 	 * @return a new quick control composite */
 	public static Composite getQuickControlComposite(final Composite parentComposite) {
 		// setup quick control composite
 		Composite quickControlComposite = new Composite(parentComposite, SWT.NONE);
 		quickControlComposite.setLayout(new GridLayout());
 
 		// separator grid data
 		GridData seperatorGridData = new GridData(GridData.FILL_BOTH);
 		seperatorGridData.minimumHeight = 15;
 		
 		// quick-search label
 		Label quickSearchLabel = new Label(quickControlComposite, SWT.NONE);
 		quickSearchLabel.setText("Quicksearch:");
 		quickSearchLabel.setFont(new Font(parentComposite.getDisplay(), quickSearchLabel.getFont().getFontData()[0].getName(), 11, SWT.BOLD));
 
 		// quick-search text-box
 		final Text quickSearchText = new Text(quickControlComposite, SWT.BORDER);
 		quickSearchText.setLayoutData(new GridData(GridData.FILL_BOTH));
 		quickSearchText.addModifyListener(new QuickSearchModifyListener());
 		Collector.setQuickSearchTextField(quickSearchText);
 		
 		// separator
 		new Label(quickControlComposite, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(seperatorGridData);
 
 		// select album label
 		Label selectAlbumLabel = new Label(quickControlComposite, SWT.NONE);
 		selectAlbumLabel.setText("Album List:");
 		selectAlbumLabel.setFont(new Font(parentComposite.getDisplay(), selectAlbumLabel.getFont().getFontData()[0].getName(), 11, SWT.BOLD));
 
 		// the list of albums (listener is added later)
 		final List albumList = new List(quickControlComposite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
 
 		GridData gridData = new GridData(GridData.FILL_BOTH);
 		gridData.heightHint = 100;
 		gridData.widthHint = 125;
 		albumList.setLayoutData(gridData);
 
 		// Set the currently active album
 		Collector.setAlbumSWTList(albumList);
 		
 		// separator
 		new Label(quickControlComposite, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(seperatorGridData);
 
 		// select album label
 		Label selectViewLabel = new Label(quickControlComposite, SWT.NONE);
 		selectViewLabel.setText("Saved Searches:");
 		selectViewLabel.setFont(new Font(parentComposite.getDisplay(), selectAlbumLabel.getFont().getFontData()[0].getName(), 11, SWT.BOLD));
 
 		// the list of albums (listener is added later)
 		final List viewList = new List(quickControlComposite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
 		// initialize view list
 		AlbumViewManager.initialize();
 		
 		GridData gridData2 = new GridData(GridData.FILL_BOTH);
 		gridData2.heightHint = 200;
 		gridData2.widthHint = 125;
 		viewList.setLayoutData(gridData2);		
 		Collector.setViewSWTList(viewList);
 
 		albumList.addSelectionListener(new SelectionListener() {
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {}
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (albumList.getSelectionIndex() != -1)	{			
 
 					Collector.setSelectedAlbum(albumList.getItem(albumList.getSelectionIndex()));
 					
 					Collector.changeRightCompositeTo(PanelType.Empty, CompositeFactory.getEmptyComposite(Collector.getThreePanelComposite()));
 
 					WelcomePageManager.getInstance().increaseClickCountForAlbumOrView(albumList.getItem(albumList.getSelectionIndex()));
 				}
 			}
 		});
 
 		viewList.addSelectionListener(new SelectionListener() {
 			@Override
 			public void widgetDefaultSelected(SelectionEvent arg0) {}
 			
 			@Override
 			public void widgetSelected(SelectionEvent arg0) {				
 				BrowserContent.performBrowserQueryAndShow(
 						Collector.getAlbumItemSWTBrowser(), 							
 						AlbumViewManager.getSqlQueryByName(viewList.getItem(viewList.getSelectionIndex())));
 				
 				WelcomePageManager.getInstance().increaseClickCountForAlbumOrView(viewList.getItem(viewList.getSelectionIndex()));
 			}
 		});
 		
 		boolean first = true;
 		// Add all albums to album list
 		for (String album : DatabaseWrapper.listAllAlbums()) {
 			albumList.add(album);
 			
 			// If the first album retrieved is not quick-searchable, then disable the related textbox
 			// If the first album retrieved has no views attached, then disable the related list
 			if (first) {
 				if (!DatabaseWrapper.isAlbumQuicksearchable(album)) {
 					quickSearchText.setEnabled(false);
 				}
 				
 				if (!AlbumViewManager.hasAlbumViewsAttached(album)) {
 					viewList.setEnabled(false);
 				} else {
 					
 					for (AlbumView albumView : AlbumViewManager.getAlbumViews(albumList.getItem(0))) {
 						viewList.add(albumView.getName());
 					}
 					
 					viewList.setEnabled(true);
 				}
 				
 				first = false;
 			}
 		}		
 		
 		Menu popupMenu = new Menu(viewList);
 		
 		MenuItem moveTop = new MenuItem(popupMenu, SWT.NONE);
 		moveTop.setText("Move to top..");
 		moveTop.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (viewList.getSelectionIndex() > 0) {
 					AlbumViewManager.moveToFront(viewList.getSelectionIndex());
 				}
 			}
 		});
 		
 		MenuItem moveOneUp = new MenuItem(popupMenu, SWT.NONE);
 		moveOneUp.setText("Move one up..");
 		moveOneUp.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (viewList.getSelectionIndex() > 0) {
 					AlbumViewManager.moveOneUp(viewList.getSelectionIndex());
 				}
 			}
 		});
 		
 		MenuItem moveOneDown = new MenuItem(popupMenu, SWT.NONE);
 		moveOneDown.setText("Move one down..");
 		moveOneDown.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (viewList.getSelectionIndex() > 0) {
 					AlbumViewManager.moveOneDown(viewList.getSelectionIndex());
 				}
 			}
 		});
 		
 		MenuItem moveBottom = new MenuItem(popupMenu, SWT.NONE);
 		moveBottom.setText("Move to bottom..");
 		moveBottom.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (viewList.getSelectionIndex() < viewList.getItemCount()-1) {
 					AlbumViewManager.moveToBottom(viewList.getSelectionIndex());
 				}
 			}
 		});
 		
 		new MenuItem(popupMenu, SWT.SEPARATOR);
 		
 		MenuItem addSavedSearch = new MenuItem(popupMenu, SWT.NONE);
 		addSavedSearch.setText("Add another saved search..");
 		addSavedSearch.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (viewList.getSelectionIndex() > 0) {
 					Collector.changeRightCompositeTo(PanelType.AdvancedSearch, 
 							getAdvancedSearchComposite(Collector.getThreePanelComposite(), Collector.getSelectedAlbum()));
 				}
 			}
 		});		
 		
 		viewList.setMenu(popupMenu);
 		
 		return quickControlComposite;
 	}
 
 	/** Returns an advanced search composite providing the means for easily building and executing SQL queries. The composite is 
 	 * automatically created based on the fields of the specified album.
 	 * @param parentComposite the parent composite
 	 * @param album the album upon which the query should be executed. The composite will be based on the fields of this album.
 	 * @return a new advanced search composite */
 	public static Composite getAdvancedSearchComposite(final Composite parentComposite, final String album) {
 		// setup advanced composite
 		Composite advancedSearchComposite = new Composite(parentComposite, SWT.NONE);
 		advancedSearchComposite.setLayout(new GridLayout(1, false));
 		advancedSearchComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		ComponentFactory.getPanelHeaderComposite(advancedSearchComposite, "Advanced Search");
 
 		Composite innerComposite = new Composite(advancedSearchComposite, SWT.BORDER);
 		innerComposite.setLayout(new GridLayout(2, false));
 		innerComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		Label fieldToSearchLabel = new Label(innerComposite, SWT.NONE);
 		fieldToSearchLabel.setText("Field to search: ");
 		final Combo fieldToSearchCombo = new Combo(innerComposite, SWT.DROP_DOWN);
 		fieldToSearchCombo.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		// Fill the comboBox
 		fieldToSearchCombo.setData(
 				"validMetaItemFields", getValidMetaItemFields(DatabaseWrapper.getAlbumItemFieldNamesAndTypes(album)));
 		fieldToSearchCombo.setItems(
 				getValidFieldNamesAsStringArray(DatabaseWrapper.getAlbumItemFieldNamesAndTypes(album)));	
 
 		Label searchOperatorLabel = new Label(innerComposite, SWT.NONE);
 		searchOperatorLabel.setText("Search Operator: ");
 		final Combo searchOperatorCombo = new Combo(innerComposite, SWT.DROP_DOWN);	
 		searchOperatorCombo.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		Label valueToSearchLabel = new Label(innerComposite, SWT.NONE);
 		valueToSearchLabel.setText("Value to Search: ");
 		final Text valueToSearchText = new Text(innerComposite, SWT.BORDER);
 		valueToSearchText.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		fieldToSearchCombo.addSelectionListener(new SelectionAdapter() {
 			@SuppressWarnings("unchecked")
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (fieldToSearchCombo.getSelectionIndex() != -1) {
 					for (MetaItemField metaItemField : (java.util.List<MetaItemField>) fieldToSearchCombo.getData("validMetaItemFields")) {
 						if (metaItemField.getName().equals(fieldToSearchCombo.getItem(fieldToSearchCombo.getSelectionIndex()))) {
 							if (metaItemField.getType() == FieldType.Text) {
 								searchOperatorCombo.setItems(QueryOperator.toTextOperatorStringArray());
 							} else if (metaItemField.getType() == FieldType.Number) {
 								searchOperatorCombo.setItems(QueryOperator.toNumberAndDateOperatorStringArray());
 							} else if (metaItemField.getType() == FieldType.Date) {
 								searchOperatorCombo.setItems(QueryOperator.toNumberAndDateOperatorStringArray());
 							} else if (metaItemField.getType() == FieldType.Time) {
 								searchOperatorCombo.setItems(QueryOperator.toNumberAndDateOperatorStringArray());
 							} else if (metaItemField.getType() == FieldType.Option) {
 								searchOperatorCombo.setItems(QueryOperator.toYesNoOperatorStringArray());							
 							}							
 						}
 					}
 				}
 			}
 		});		
 
 		Button addToSearchButton = new Button(innerComposite, SWT.PUSH);
 		addToSearchButton.setText("Add to search");
 		GridData gridData = new GridData(GridData.FILL_BOTH);
 		gridData.horizontalSpan = 2;
 		addToSearchButton.setLayoutData(gridData);
 		// add-search-component button listener after table definition
 
 		// Field table
 		final Table searchQueryTable = new Table(advancedSearchComposite, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
 		searchQueryTable.setLinesVisible(true);
 		searchQueryTable.setHeaderVisible(true);
 
 		// add-search-component button listener
 		addToSearchButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if ((fieldToSearchCombo.getSelectionIndex() == -1) 
 						|| (searchOperatorCombo.getSelectionIndex() == -1) 
 						|| valueToSearchText.getText().equals("")) {				
 					MessageBox messageBox = ComponentFactory.getMessageBox(
 							parentComposite.getShell(),
 							"Collector-Warning",
 							"You must select a field and an operator, as well as a value to search for!",
 							SWT.ICON_WARNING | SWT.OK);
 					messageBox.open();
 					return;
 				}
 				TableItem item = new TableItem(searchQueryTable, SWT.NONE);
 				item.setText(0, fieldToSearchCombo.getItem(fieldToSearchCombo.getSelectionIndex()));
 				item.setText(1, searchOperatorCombo.getItem(searchOperatorCombo.getSelectionIndex()));
 				item.setText(2, valueToSearchText.getText());
 
 				valueToSearchText.setText("");
 			}
 		});	
 
 		// Setup table
 		TableColumn fieldNameColumn = new TableColumn(searchQueryTable, SWT.NONE);
 		fieldNameColumn.setText("Field Name");
 		TableColumn operatorColumn = new TableColumn(searchQueryTable, SWT.NONE);
 		operatorColumn.setText("Operator");
 		TableColumn valueColumn = new TableColumn(searchQueryTable, SWT.NONE);
 		valueColumn.setText("Value");
 		searchQueryTable.getColumn(0).pack ();
 		searchQueryTable.getColumn(1).pack ();
 		searchQueryTable.getColumn(2).pack ();
 
 		// Pop-Up menu
 		Menu popupMenu = new Menu(searchQueryTable);
 		MenuItem remove = new MenuItem(popupMenu, SWT.NONE);
 		remove.setText("Remove");
 		remove.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (searchQueryTable.getSelectionIndex() != -1) {
 					searchQueryTable.getItem(searchQueryTable.getSelectionIndex()).dispose();
 				}
 			}
 		});
 
 		MenuItem removeAll = new MenuItem(popupMenu, SWT.NONE);
 		removeAll.setText("Remove all");
 		removeAll.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (searchQueryTable.getSelectionIndex() != -1) {
 					for (TableItem tableItem : searchQueryTable.getItems()) {
 						tableItem.dispose();
 					}
 				}
 			}
 		});
 
 		searchQueryTable.setMenu(popupMenu);
 
 		// Set table layout data
 		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
 		data.heightHint = 150;
 		searchQueryTable.setLayoutData(data);	
 
 		ComponentFactory.getSmallBoldItalicLabel(advancedSearchComposite, "Connect search terms by: ");
 
 		Composite composite = new Composite(advancedSearchComposite, SWT.BORDER);
 		composite.setLayout(new RowLayout());
 		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
 
 		final Button andButton = new Button(composite, SWT.RADIO);
 		andButton.setText("AND");
 		andButton.setSelection(true);
 		Button orButton = new Button(composite, SWT.RADIO);
 		orButton.setText("OR");
 
 		Button searchButton = new Button(advancedSearchComposite, SWT.PUSH);
 		searchButton.setText("Execute search");
 		searchButton.setLayoutData(new GridData(GridData.FILL_BOTH));
 		searchButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				ArrayList<QueryComponent> queryComponents = new ArrayList<QueryComponent>();
 
 				for ( int i=0 ; i < searchQueryTable.getItemCount() ; i++ ) {					
 					queryComponents.add(QueryBuilder.getQueryComponent(
 							searchQueryTable.getItem(i).getText(0),
 							QueryOperator.toQueryOperator(searchQueryTable.getItem(i).getText(1)),
 							searchQueryTable.getItem(i).getText(2)));
 				}
 
 				boolean connectByAnd = false;
 				if (andButton.getSelection() == true) {
 					connectByAnd = true;
 				}
 
 				QueryBuilder.buildQueryAndExecute(queryComponents, connectByAnd, album);
 			}
 		});
 		
 		Button saveAsViewButton = new Button(advancedSearchComposite, SWT.PUSH);
 		saveAsViewButton.setText("Save this search");
 		saveAsViewButton.setLayoutData(new GridData(GridData.FILL_BOTH));
 		saveAsViewButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (!Collector.hasSelectedAlbum()) {
 					Collector.showErrorDialog("No album has been selected", "Please select an album from the list or create one first.");
 					return;
 				}
				// TODO save sql query
 				ArrayList<QueryComponent> queryComponents = new ArrayList<QueryComponent>();
 
 				for ( int i=0 ; i < searchQueryTable.getItemCount() ; i++ ) {					
 					queryComponents.add(QueryBuilder.getQueryComponent(
 							searchQueryTable.getItem(i).getText(0),
 							QueryOperator.toQueryOperator(searchQueryTable.getItem(i).getText(1)),
 							searchQueryTable.getItem(i).getText(2)));
 				}
 
 				boolean connectByAnd = false;
 				if (andButton.getSelection() == true) {
 					connectByAnd = true;
 				}
 
 				TextInputDialog textInputDialog = new TextInputDialog(parentComposite.getShell());
 				String viewName = textInputDialog.open("Please enter a name for the search", "Search Name: ", "My Search", "Save");
 
 				if (viewName != null && !AlbumViewManager.hasViewWithName(viewName)) {
 					AlbumViewManager.addAlbumView(viewName, Collector.getSelectedAlbum(), QueryBuilder.buildQuery(queryComponents, connectByAnd, album));
 				} else {
					// TODO error message
 				}
 			}
 		});
 
 		return advancedSearchComposite;
 	}
 
 	/** Returns a browser composite which is used to render HTML.
 	 * @param parentComposite the parent composite
 	 * @param browserListener a class of various listeners for the browser
 	 * @return a new browser composite */
 	public static Composite getBrowserComposite(Composite parentComposite, BrowserListener browserListener) {
 		// setup SWT browser composite
 		Composite browserComposite = new Composite(parentComposite, SWT.NONE);
 		browserComposite.setLayout(new GridLayout());
 
 		// the browser itself
 		Browser browser = new Browser(browserComposite, SWT.NONE);
 		browser.setDragDetect(false);
 
 		// setup layout data for the browser
 		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		browser.setLayoutData(gridData);
 
 		// store browser reference in the main shell & register location listener with the browser
 		Collector.setAlbumItemSWTBrowser(browser);
 		browser.addLocationListener(browserListener);
 		browser.addProgressListener(browserListener);
 		browser.addMenuDetectListener(browserListener);
 
 		return browserComposite;
 	}
 
 	/** Returns an empty composite
 	 * @param parentComposite the parent composite
 	 * @return a new empty composite */
 	public static Composite getEmptyComposite(Composite parentComposite) {
 		// Setup empty composite
 		Composite emptyComposite = new Composite(parentComposite, SWT.NONE);
 
 		return emptyComposite;
 	}
 
 	/** Returns a "create new album" composite. This composite provides the user interface to create a new album. Meaning that an 
 	 * album name can be specified, as well as an undefined number of fields (columns) with user defined types etc..
 	 * @param parentComposite the parent composite
 	 * @return a new "create new album" composite */
 	public static Composite getCreateNewAlbumComposite(final Composite parentComposite) {		
 		// setup create new album composite
 		Composite createNewAlbumComposite = new Composite(parentComposite, SWT.NONE);	
 		createNewAlbumComposite.setLayout(new GridLayout());
 
 		// description (header) label
 		ComponentFactory.getPanelHeaderComposite(createNewAlbumComposite, "Creating a new Album");
 
 		// album name label & text-box to enter album name
 		Label albumNameLabel = new Label(createNewAlbumComposite, SWT.NONE);
 		albumNameLabel.setText("The name of the new Album:");
 		final Text albumNameText = new Text(createNewAlbumComposite, SWT.BORDER);
 		albumNameText.setLayoutData(new GridData(GridData.FILL_BOTH));
 		albumNameText.setText("My new Album");
 
 		// picture question label & radio buttons
 		Label label = new Label(createNewAlbumComposite, SWT.NONE);
 		label.setText("Should this album contain pictures?");
 		Composite composite = new Composite(createNewAlbumComposite, SWT.NULL);
 		composite.setLayout(new RowLayout());
 		final Button yesButtonForIncludingImages = new Button(composite, SWT.RADIO);
 		yesButtonForIncludingImages.setText("Yes");
 		yesButtonForIncludingImages.setSelection(true);
 		Button noButtonForIncludingImages = new Button(composite, SWT.RADIO);
 		noButtonForIncludingImages.setText("No");
 
 		// fieldname label and text-box to enter the name of the field
 		Label fieldNameLabel = new Label(createNewAlbumComposite, SWT.NONE);
 		fieldNameLabel.setText("Name of the field:");
 		final Text fieldNameText = new Text(createNewAlbumComposite, SWT.BORDER);
 		fieldNameText.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		// fieldtype label and combo-box to enter the type of the field
 		Label fieldTypeLabel = new Label(createNewAlbumComposite, SWT.NONE);
 		fieldTypeLabel.setText("Type of the field:");
 		final Combo fieldTypeCombo = new Combo(createNewAlbumComposite, SWT.DROP_DOWN);
 		fieldTypeCombo.setItems(FieldType.toUserTypeStringArray());	    
 		fieldTypeCombo.setLayoutData(new GridData(GridData.FILL_BOTH));
 		fieldTypeCombo.setText(fieldTypeCombo.getItem(0).toString());
 
 		// Add-field-button --> listener comes after table
 		Button addFieldButton = new Button(createNewAlbumComposite, SWT.PUSH);
 		addFieldButton.setText("Add field to Album");
 		addFieldButton.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		// Field table
 		final Table albumFieldNamesAndTypesTable = 
 				new Table(createNewAlbumComposite, SWT.CHECK | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
 		albumFieldNamesAndTypesTable.setLinesVisible(true);
 		albumFieldNamesAndTypesTable.setHeaderVisible(true);
 
 		Menu popupMenu = new Menu(albumFieldNamesAndTypesTable);
 		MenuItem moveUp = new MenuItem(popupMenu, SWT.NONE);
 		moveUp.setText("Move one up..");
 		moveUp.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (albumFieldNamesAndTypesTable.getSelectionIndex() > 0) {
 					TableItem originalItem = albumFieldNamesAndTypesTable.getItem(albumFieldNamesAndTypesTable.getSelectionIndex());
 
 					TableItem itemAtNewPosition = new TableItem(
 							albumFieldNamesAndTypesTable, SWT.NONE, albumFieldNamesAndTypesTable.getSelectionIndex() - 1);
 					itemAtNewPosition.setText(1, originalItem.getText(1));
 					itemAtNewPosition.setText(2, originalItem.getText(2));
 
 					originalItem.dispose();
 				}
 			}
 		});
 
 		MenuItem moveDown = new MenuItem(popupMenu, SWT.NONE);
 		moveDown.setText("Move one down..");
 		moveDown.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (albumFieldNamesAndTypesTable.getSelectionIndex() != albumFieldNamesAndTypesTable.getItemCount()) {
 					TableItem originalItem = albumFieldNamesAndTypesTable.getItem(albumFieldNamesAndTypesTable.getSelectionIndex());
 
 					TableItem itemAtNewPosition = 
 							new TableItem(albumFieldNamesAndTypesTable, SWT.NONE, albumFieldNamesAndTypesTable.getSelectionIndex() + 2);
 					itemAtNewPosition.setText(1, originalItem.getText(1));
 					itemAtNewPosition.setText(2, originalItem.getText(2));
 
 					originalItem.dispose();
 				}
 			}
 		});
 
 		new MenuItem(popupMenu, SWT.SEPARATOR);
 		MenuItem rename = new MenuItem(popupMenu, SWT.NONE);
 		rename.setText("Rename");
 		rename.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				TableItem item = albumFieldNamesAndTypesTable.getItem(albumFieldNamesAndTypesTable.getSelectionIndex());
 
 				TextInputDialog textInputDialog = new TextInputDialog(parentComposite.getShell());
 				String newName = textInputDialog.open("Renaming the field", "Rename: ", item.getText(1), "Rename!");
 
 				if (newName != null) {
 					item.setText(1, newName);
 				}
 			}
 		});
 
 		new MenuItem(popupMenu, SWT.SEPARATOR);
 		MenuItem delete = new MenuItem(popupMenu, SWT.NONE);
 		delete.setText("Remove");
 		delete.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (albumFieldNamesAndTypesTable.getSelectionIndex() != -1) {
 					TableItem item = albumFieldNamesAndTypesTable.getItem(albumFieldNamesAndTypesTable.getSelectionIndex());					
 					item.dispose();
 				}
 			}
 		});
 
 		albumFieldNamesAndTypesTable.setMenu(popupMenu);
 
 		// Setup table
 		TableColumn isImportantColumn = new TableColumn(albumFieldNamesAndTypesTable, SWT.NONE);
 		isImportantColumn.setText("QuickSearch");
 		TableColumn fieldNameColumn = new TableColumn(albumFieldNamesAndTypesTable, SWT.NONE);
 		fieldNameColumn.setText("Field Name");
 		TableColumn fieldTypeColumn = new TableColumn(albumFieldNamesAndTypesTable, SWT.NONE);
 		fieldTypeColumn.setText("Field Type");
 		albumFieldNamesAndTypesTable.getColumn(0).pack ();
 		albumFieldNamesAndTypesTable.getColumn(1).pack ();
 		albumFieldNamesAndTypesTable.getColumn(2).pack ();
 
 		// Set table layout data
 		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
 		data.heightHint = 150;
 		albumFieldNamesAndTypesTable.setLayoutData(data);
 
 		// Add listener to Add-field-button
 		addFieldButton.addSelectionListener(new SelectionListener() {
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {}
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (fieldNameText.getText().isEmpty()) {				
 					MessageBox messageBox = ComponentFactory.getMessageBox(
 							parentComposite.getShell(), "Collector-Warning", "You must give your field a name!", SWT.ICON_WARNING | SWT.OK);
 					messageBox.open();
 					return;
 				}
 				TableItem item = new TableItem(albumFieldNamesAndTypesTable, SWT.NONE);
 				item.setText(1, fieldNameText.getText());
 				item.setText(2, fieldTypeCombo.getText());
 
 				fieldNameText.setText("");
 			}
 		});
 
 		// Create album button
 		Button createAlbumButton = new Button(createNewAlbumComposite, SWT.PUSH);
 		createAlbumButton.setText("Create the Album");
 		createAlbumButton.setLayoutData(new GridData(GridData.FILL_BOTH));
 		createAlbumButton.addSelectionListener(new SelectionListener() {
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {}
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (DatabaseWrapper.listAllAlbums().contains(albumNameText.getText())) {
 					ComponentFactory.getMessageBox(parentComposite, "Name already in use", "This name is already used by another album. Please choose another name.", SWT.ICON_INFORMATION).open();					
 					return;
 				}
 				
 				ArrayList<MetaItemField> metaItemFields = new ArrayList<MetaItemField>();
 
 				for ( int i=0 ; i < albumFieldNamesAndTypesTable.getItemCount() ; i++ ) {					
 					metaItemFields.add(
 							new MetaItemField(
 									albumFieldNamesAndTypesTable.getItem(i).getText(1),
 									FieldType.valueOf(albumFieldNamesAndTypesTable.getItem(i).getText(2)),
 									albumFieldNamesAndTypesTable.getItem(i).getChecked()));
 				}
 
 				boolean willContainImages = false;
 				if (yesButtonForIncludingImages.getSelection()) {
 					willContainImages = true;
 				}
 
 				DatabaseWrapper.createNewAlbum(albumNameText.getText(), metaItemFields, willContainImages);				
 				// Correctly select and display the selected album.
 				Collector.refreshSWTAlbumList();
 				Collector.setSelectedAlbum(albumNameText.getText());				
 				BrowserContent.performBrowserQueryAndShow(
 						Collector.getAlbumItemSWTBrowser(),
 						"select * from " + DatabaseWrapper.transformNameToDBName(albumNameText.getText()));
 
 				Collector.changeRightCompositeTo(PanelType.Empty, CompositeFactory.getEmptyComposite(parentComposite));
 			}
 		});
 
 		GridData gridData = new GridData();
 		gridData.widthHint = 600;
 		gridData.heightHint = 600;
 		createNewAlbumComposite.setLayoutData(gridData);
 
 		return createNewAlbumComposite;
 	}
 
 	/** Returns an "alter album" composite. This composite provides the user interface to alter an existing album. Meaning that an 
 	 * album name can be renamed, fields can be removed, fields can be added etc.. This composite is created based on an an existing
 	 * album and its field names/types.
 	 * @param parentComposite the parent composite
 	 * @param album the album which should be altered
 	 * @return a new "alter album" composite */
 	public static Composite getAlterAlbumComposite(final Composite parentComposite, final String album) {
 		// setup alter album composite
 		Composite alterAlbumComposite = new Composite(parentComposite, SWT.NONE);
 		alterAlbumComposite.setLayout(new GridLayout());
 
 		// description (header) label
 		ComponentFactory.getPanelHeaderComposite(alterAlbumComposite, "Alter an existing Album!");
 
 		// album name label & text-box to enter album name
 		Label albumNameLabel = new Label(alterAlbumComposite, SWT.NONE);
 		albumNameLabel.setText("The new name of the Album:");
 		final Text albumNameText = new Text(alterAlbumComposite, SWT.BORDER);
 		albumNameText.setLayoutData(new GridData(GridData.FILL_BOTH));
 		albumNameText.setText(album);
 
 		// Alter album button
 		Button renameAlbumButton = new Button(alterAlbumComposite, SWT.PUSH);
 		renameAlbumButton.setText("Rename the Album");
 		renameAlbumButton.setLayoutData(new GridData(GridData.FILL_BOTH));
 		renameAlbumButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (DatabaseWrapper.listAllAlbums().contains(albumNameText.getText())) {
 					ComponentFactory.getMessageBox(parentComposite, "Name already in use", "This name is already used by another album. Please choose another name.", SWT.ICON_INFORMATION).open();					
 					return;
 				}
 				
 				DatabaseWrapper.renameAlbum(album, albumNameText.getText());
 				Collector.refreshSWTAlbumList();
 			}
 		});
 
 		Label seperator = new Label(alterAlbumComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
 		GridData gridData= new GridData(GridData.FILL_BOTH);
 		gridData.heightHint = 15;
 		seperator.setLayoutData(gridData);
 
 		Composite innerComposite = new Composite(alterAlbumComposite, SWT.BORDER);
 		innerComposite.setLayout(new GridLayout());
 
 		// picture question label & radio buttons
 		Label label = new Label(innerComposite, SWT.NONE);
 		label.setText("Should this album contain pictures?");
 		Composite composite = new Composite(innerComposite, SWT.NULL);
 		composite.setLayout(new RowLayout());
 		final Button yesButtonForIncludingImages = new Button(composite, SWT.RADIO);
 		yesButtonForIncludingImages.setText("Yes");
 		yesButtonForIncludingImages.setSelection(DatabaseWrapper.albumHasPictureField(album));
 		yesButtonForIncludingImages.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (!DatabaseWrapper.albumHasPictureField(album)) {
 					DatabaseWrapper.appendPictureField(album);
 				}
 			}
 		});
 		Button noButtonForIncludingImages = new Button(composite, SWT.RADIO);
 		noButtonForIncludingImages.setText("No");
 		noButtonForIncludingImages.setSelection(!DatabaseWrapper.albumHasPictureField(album));
 		noButtonForIncludingImages.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (DatabaseWrapper.albumHasPictureField(album)) {
 					DatabaseWrapper.removePictureField(album);
 				}
 			}
 		});
 
 		// fieldname label and text-box to enter the name of the field
 		Label fieldNameLabel = new Label(innerComposite, SWT.NONE);
 		fieldNameLabel.setText("Name of the additional field:");
 		final Text fieldNameText = new Text(innerComposite, SWT.BORDER);
 		fieldNameText.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		// fieldtype label and combo-box to enter the type of the field
 		Label fieldTypeLabel = new Label(innerComposite, SWT.NONE);
 		fieldTypeLabel.setText("Type of the field:");
 		final Combo fieldTypeCombo = new Combo(innerComposite, SWT.DROP_DOWN);
 		fieldTypeCombo.setItems(FieldType.toUserTypeStringArray());	    
 		fieldTypeCombo.setLayoutData(new GridData(GridData.FILL_BOTH));
 		fieldTypeCombo.setText(fieldTypeCombo.getItem(0).toString());
 
 		// Add-field-button --> listener comes after table
 		Button addFieldButton = new Button(innerComposite, SWT.PUSH);
 		addFieldButton.setText("Add additional field to Album");
 		addFieldButton.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		// Field table
 		final Table albumFieldNamesAndTypesTable = new Table(innerComposite, SWT.CHECK | SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
 		albumFieldNamesAndTypesTable.setLinesVisible(true);
 		albumFieldNamesAndTypesTable.setHeaderVisible(true);
 
 		Menu popupMenu = new Menu(albumFieldNamesAndTypesTable);
 		MenuItem moveUp = new MenuItem(popupMenu, SWT.NONE);
 		moveUp.setText("Move one up..");
 		moveUp.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (albumFieldNamesAndTypesTable.getSelectionIndex() > 0) {
 					int newPosition = albumFieldNamesAndTypesTable.getSelectionIndex() - 1; // move one up
 					TableItem originalItem = albumFieldNamesAndTypesTable.getItem(albumFieldNamesAndTypesTable.getSelectionIndex());
 					MetaItemField metaItemField = new MetaItemField(originalItem.getText(1), FieldType.valueOf(originalItem.getText(2)), originalItem.getChecked());
 					originalItem.dispose();
 
 					TableItem itemAtNewPosition = new TableItem(albumFieldNamesAndTypesTable, SWT.NONE, newPosition);
 					itemAtNewPosition.setText(1, metaItemField.getName());
 					itemAtNewPosition.setText(2, metaItemField.getType().toString());
 					itemAtNewPosition.setChecked(metaItemField.isQuickSearchable());					
 
 					if (newPosition == 0) {
 						DatabaseWrapper.reorderAlbumItemField(album, metaItemField, null);
 					} else {
 						TableItem moveAfterTableItem = albumFieldNamesAndTypesTable.getItem(newPosition-1);
 						MetaItemField moveAfterField = new MetaItemField(moveAfterTableItem.getText(1), FieldType.valueOf(moveAfterTableItem.getText(2)), moveAfterTableItem.getChecked());
 						DatabaseWrapper.reorderAlbumItemField(album, metaItemField, moveAfterField);
 					}
 
 					BrowserContent.performLastQuery(Collector.getAlbumItemSWTBrowser());
 				}
 			}
 		});
 
 		MenuItem moveDown = new MenuItem(popupMenu, SWT.NONE);
 		moveDown.setText("Move one down..");
 		moveDown.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (albumFieldNamesAndTypesTable.getSelectionIndex() < (albumFieldNamesAndTypesTable.getItemCount() - 1)) {
 					int newPosition = albumFieldNamesAndTypesTable.getSelectionIndex() + 1; // move one down
 					TableItem originalItem = albumFieldNamesAndTypesTable.getItem(albumFieldNamesAndTypesTable.getSelectionIndex());
 					MetaItemField metaItemField = new MetaItemField(originalItem.getText(1), FieldType.valueOf(originalItem.getText(2)), originalItem.getChecked());
 					originalItem.dispose();
 
 					TableItem itemAtNewPosition = new TableItem(albumFieldNamesAndTypesTable, SWT.NONE, newPosition);
 					itemAtNewPosition.setText(1, metaItemField.getName());
 					itemAtNewPosition.setText(2, metaItemField.getType().toString());
 					itemAtNewPosition.setChecked(metaItemField.isQuickSearchable());
 
 					if (newPosition == 0) {
 						DatabaseWrapper.reorderAlbumItemField(album, metaItemField, null);
 					} else {
 						TableItem moveAfterTableItem = albumFieldNamesAndTypesTable.getItem(newPosition - 1);
 						MetaItemField moveAfterField = new MetaItemField(moveAfterTableItem.getText(1), FieldType.valueOf(moveAfterTableItem.getText(2)), moveAfterTableItem.getChecked());
 						DatabaseWrapper.reorderAlbumItemField(album, metaItemField, moveAfterField);
 					}
 
 					BrowserContent.performLastQuery(Collector.getAlbumItemSWTBrowser());
 				}
 			}
 		});
 
 		new MenuItem(popupMenu, SWT.SEPARATOR);
 		MenuItem rename = new MenuItem(popupMenu, SWT.NONE);
 		rename.setText("Rename");
 		rename.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				TableItem item = albumFieldNamesAndTypesTable.getItem(albumFieldNamesAndTypesTable.getSelectionIndex());
 
 				TextInputDialog textInputDialog = new TextInputDialog(parentComposite.getShell());
 				String newName = textInputDialog.open("Renaming the field", "Rename: ", item.getText(1), "Rename!");
 
 				if (newName != null) {	    			
 					MetaItemField oldMetaItemField = new MetaItemField(item.getText(1),  FieldType.valueOf(item.getText(2)), item.getChecked());
 					MetaItemField newMetaItemField = new MetaItemField(newName,  FieldType.valueOf(item.getText(2)), item.getChecked());
 
 					DatabaseWrapper.renameAlbumItemField(album, oldMetaItemField, newMetaItemField);
 
 					item.setText(1, newName);
 
 					BrowserContent.performLastQuery(Collector.getAlbumItemSWTBrowser());
 				}
 			}
 		});
 
 		new MenuItem(popupMenu, SWT.SEPARATOR);
 		MenuItem delete = new MenuItem(popupMenu, SWT.NONE);
 		delete.setText("Remove");
 		delete.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (albumFieldNamesAndTypesTable.getSelectionIndex() != -1) {	    			
 					TableItem item = albumFieldNamesAndTypesTable.getItem(albumFieldNamesAndTypesTable.getSelectionIndex());
 
 					DatabaseWrapper.removeAlbumItemField(
 							album, new MetaItemField(item.getText(1), FieldType.valueOf(item.getText(2)), item.getChecked()));
 
 					item.dispose();
 
 					BrowserContent.performLastQuery(Collector.getAlbumItemSWTBrowser());
 				}
 			}
 		});
 
 		albumFieldNamesAndTypesTable.setMenu(popupMenu);
 
 		// Setup table
 		TableColumn isImportantColumn = new TableColumn(albumFieldNamesAndTypesTable, SWT.NONE);
 		isImportantColumn.setText("QuickSearch");
 		TableColumn fieldNameColumn = new TableColumn(albumFieldNamesAndTypesTable, SWT.NONE);
 		fieldNameColumn.setText("Field Name");
 		TableColumn fieldTypeColumn = new TableColumn(albumFieldNamesAndTypesTable, SWT.NONE);
 		fieldTypeColumn.setText("Field Type");
 		albumFieldNamesAndTypesTable.getColumn(0).pack ();
 		albumFieldNamesAndTypesTable.getColumn(1).pack ();
 		albumFieldNamesAndTypesTable.getColumn(2).pack ();
 
 		// Fill the table
 		java.util.List<MetaItemField> validMetaItemFields = getValidMetaItemFields(DatabaseWrapper.getAlbumItemFieldNamesAndTypes(album));
 		for (MetaItemField metaItemField : validMetaItemFields) {
 			TableItem item = new TableItem(albumFieldNamesAndTypesTable, SWT.NONE);
 			item.setChecked(metaItemField.isQuickSearchable());
 			item.setText(1, metaItemField.getName());
 			item.setText(2, metaItemField.getType().toString());			
 		}
 
 		// Set table layout data
 		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
 		data.heightHint = 150;
 		albumFieldNamesAndTypesTable.setLayoutData(data);
 
 		albumFieldNamesAndTypesTable.addListener(SWT.Selection, new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 
 				int index =0;
 				for (index=0; index< albumFieldNamesAndTypesTable.getItemCount(); index++) {
 					if (((TableItem)event.item).equals(albumFieldNamesAndTypesTable.getItem(index)) ) {
 						break;
 					}
 				} 
 
 				if (event.detail == SWT.CHECK) {
 					MetaItemField metaItemField = new MetaItemField(
 							albumFieldNamesAndTypesTable.getItem(index).getText(1),
 							FieldType.valueOf(albumFieldNamesAndTypesTable.getItem(index).getText(2)),
 							albumFieldNamesAndTypesTable.getItem(index).getChecked());
 
 					DatabaseWrapper.setQuickSearchable(album, metaItemField);
 				}
 			}
 		});
 
 		// Add listener to Add-field-button
 		addFieldButton.addSelectionListener(new SelectionListener() {
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {}
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (fieldNameText.getText().isEmpty()) {				
 					MessageBox messageBox = ComponentFactory.getMessageBox(
 							parentComposite.getShell(), "Collector-Warning", "You must give your field a name!", SWT.ICON_WARNING | SWT.OK);
 					messageBox.open();
 					return;
 				}
 				TableItem item = new TableItem(albumFieldNamesAndTypesTable, SWT.NONE);
 				item.setText(1, fieldNameText.getText());
 				item.setText(2, fieldTypeCombo.getText());
 
 				MetaItemField metaItemField = new MetaItemField(fieldNameText.getText(), FieldType.valueOf(fieldTypeCombo.getText()), false);
 				DatabaseWrapper.appendNewAlbumFields(album, metaItemField);
 
 				fieldNameText.setText("");
 
 				BrowserContent.performLastQuery(Collector.getAlbumItemSWTBrowser());
 			}
 		});
 
 		return alterAlbumComposite;
 	}
 
 	/** Returns a "basic album item" composite. This composite provides the fields (field names and value input fields)
 	 *  needed by the add item composite.
 	 * @param parentComposite the parent composite
 	 * @param album the name of the album to which an item should be added
 	 * @param caption the caption/header of the basic album item composite.
 	 * @return a new "basic album item" composite */
 	private static Composite getBasicAlbumItemComposite(Composite parentComposite, final String album) {
 		return getBasicAlbumItemComposite(parentComposite, album, 0, false);
 	}
 
 	/** Returns a "basic album item" composite. This composite provides the fields (field names and value input fields) needed by the 
 	 * update item composite. The content from the specified album item is used to fill the different album fields.
 	 * @param parentComposite the parent composite
 	 * @param album the name of the album to which the item should should be updated
 	 * @param albumItemId the id of the album item which should be used to fill the fields
 	 * @param caption the caption/header of the basic album item composite.
 	 * @return a new "basic album item" composite */
 	private static Composite getBasicAlbumItemComposite(Composite parentComposite, final String album, final long albumItemId) {		
 		return getBasicAlbumItemComposite(parentComposite, album, albumItemId, true);
 	}
 
 	/** Returns a "basic album item" composite. This composite provides the fields (field names and value input fields) needed by the
 	 * add and update item composites.
 	 * @param parentComposite the parent composite
 	 * @param album the name of the album to which an item should be added, or an item should be updated
 	 * @param albumItemId the id of the album item which should be used to fill the fields. In case of the "add" composite, this id is not
 	 * required and can be set to any value. However the loadDataIntoFields should be set accordingly.
 	 * @param caption the caption/header of the basic album item composite.
 	 * @param loadDataIntoFields if the content of the specified album item should be loaded into the fields, then this should be true.
 	 * If it should not be loaded (E.g. in case of the "add" composite, then this should be false
 	 * @return a new "basic album item" composite */
 	private static Composite getBasicAlbumItemComposite(Composite parentComposite, final String album, final long albumItemId, boolean loadDataIntoFields) {
 		// setup the basic composite
 		final Composite basicAlbumItemComposite = new Composite(parentComposite, SWT.NONE);
 		GridLayout gridLayout = new GridLayout(1, false);
 		basicAlbumItemComposite.setLayout(gridLayout);
 		basicAlbumItemComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 		AlbumItem albumItem = null;
 		// if data should be loaded, it must be fetched from the database
 		if (loadDataIntoFields) {
 			albumItem = DatabaseWrapper.fetchAlbumItem(album, albumItemId);
 		}	
 
 		// Fetch the field names and types from the database
 		java.util.List<MetaItemField> metaItemFields = DatabaseWrapper.getAlbumItemFieldNamesAndTypes(album);
 
 		boolean addPictureComposite = false;
 		String pictureFieldName = "";
 
 		for (MetaItemField metaItem : metaItemFields) {
 			String fieldName = metaItem.getName();
 			FieldType fieldType = metaItem.getType();
 
 			// Do not show the id field!
 			if (fieldName.equals("id") || fieldName.equals("typeinfo")) {
 				continue;
 			}
 
 			switch (fieldType) {
 			case ID:
 				// not shown
 				break;
 			case UUID:
 				// not shown
 				break;
 			case Picture:
 				addPictureComposite = true;
 
 				pictureFieldName = metaItem.getName();
 
 				break;
 			case Text: 
 				ComponentFactory.getSmallBoldItalicLabel(basicAlbumItemComposite, fieldName + ":");
 
 				Text textText = new Text(
 						basicAlbumItemComposite,
 						SWT.WRAP
 						| SWT.MULTI
 						| SWT.BORDER
 						| SWT.V_SCROLL
 						);
 
 				GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
 				// TODO magic value!!!
 				gridData.widthHint = 200;
 				textText.setLayoutData(gridData);
 				// Override the normal tab behaviour of a multiline text widget.
 				// Instead of ctrl+Tab a simple text chnages focus.
 				textText.addTraverseListener(new TraverseListener() {
 					public void keyTraversed(TraverseEvent e) {
 						if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
 							e.doit = true;
 						}
 					}
 				});
 
 				if (loadDataIntoFields) {
 					textText.setText((String) albumItem.getField(fieldName).getValue());
 				}
 
 				textText.setData("FieldType", FieldType.Text);
 				textText.setData("FieldName", fieldName);
 
 				break;
 
 			case URL: 
 				ComponentFactory.getSmallBoldItalicLabel(basicAlbumItemComposite, fieldName + ":");
 
 				Text url = new Text(
 						basicAlbumItemComposite,
 						SWT.WRAP
 						| SWT.MULTI
 						| SWT.BORDER
 						| SWT.V_SCROLL);
 
 				url.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
 
 				if (loadDataIntoFields) {
 					url.setText((String) albumItem.getField(fieldName).getValue());
 				}
 
 				url.setData("FieldType", FieldType.URL);
 				url.setData("FieldName", fieldName);
 
 				break;	
 
 			case Number:
 				ComponentFactory.getSmallBoldItalicLabel(basicAlbumItemComposite, fieldName + ":");
 
 				final Text numberText = new Text(basicAlbumItemComposite, SWT.BORDER);
 				numberText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
 
 				if (loadDataIntoFields) {
 					numberText.setText(((Double) albumItem.getField(fieldName).getValue()).toString());
 				} 
 				numberText.addListener(SWT.Verify, new Listener() {
 					public void handleEvent(Event e) {
 						boolean hasPoint = false;
 						String newString = e.text;
 						hasPoint = numberText.getText().contains(".");
 
 						char[] chars = new char[newString.length()];
 						newString.getChars(0, chars.length, chars, 0);
 
 						for (int i = 0; i < chars.length; i++) {
 							System.out.println(hasPoint);
 							if (!hasPoint) {
 								if (!('0' <= chars[i] && chars[i] <= '9'|| chars[i] == '.')) {
 									e.doit = false;
 									return;
 								}
 							} else {
 								if (!('0' <= chars[i] && chars[i] <= '9')) {
 									e.doit = false;
 									return;
 								}
 							}
 						}						
 					}
 				});
 
 				numberText.setData("FieldType", FieldType.Number);
 				numberText.setData("FieldName", fieldName);
 
 				break;
 
 			case Integer:
 				ComponentFactory.getSmallBoldItalicLabel(basicAlbumItemComposite, fieldName + ":");
 
 				final Text integerText = new Text(basicAlbumItemComposite, SWT.BORDER);
 				integerText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
 
 				if (loadDataIntoFields) {
 					integerText.setText(((Integer) albumItem.getField(fieldName).getValue()).toString());
 				} 
 				integerText.addListener(SWT.Verify, new Listener() {
 					public void handleEvent(Event e) {
 						String newString = e.text;
 
 						char[] chars = new char[newString.length()];
 						newString.getChars(0, chars.length, chars, 0);
 
 						for (int i = 0; i < chars.length; i++) {
 							if (!('0' <= chars[i] && chars[i] <= '9')) {
 								e.doit = false;
 								return;
 							}
 						}						
 					}
 				});
 
 				integerText.setData("FieldType", FieldType.Integer);
 				integerText.setData("FieldName", fieldName);
 
 				break;	
 
 			case Date:
 				ComponentFactory.getSmallBoldItalicLabel(basicAlbumItemComposite, fieldName + ":");
 
 				DateTime datePicker = new DateTime(basicAlbumItemComposite, SWT.BORDER | SWT.DATE | SWT.DROP_DOWN);
 				datePicker.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
 
 				if (loadDataIntoFields) {
 					Date date = albumItem.getField(fieldName).getValue();
 					Calendar calendarForDate = Calendar.getInstance();
 					calendarForDate.setTimeInMillis(date.getTime());
 					datePicker.setDate(calendarForDate.get(Calendar.YEAR), calendarForDate.get(Calendar.MONTH), calendarForDate.get(Calendar.DAY_OF_MONTH));
 				}
 
 				datePicker.setData("FieldType", FieldType.Date);
 				datePicker.setData("FieldName", fieldName);
 
 				break;
 
 			case Time:
 				ComponentFactory.getSmallBoldItalicLabel(basicAlbumItemComposite, fieldName + ":");
 
 				DateTime timePicker = new DateTime(basicAlbumItemComposite, SWT.BORDER | SWT.TIME | SWT.DROP_DOWN);
 				timePicker.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
 
 				if (loadDataIntoFields) {
 					Time time = albumItem.getField(fieldName).getValue();
 					Calendar calendarForTime = Calendar.getInstance();
 					calendarForTime.setTimeInMillis(time.getTime());
 
 					timePicker.setTime(calendarForTime.get(Calendar.HOUR), calendarForTime.get(Calendar.MINUTE), calendarForTime.get(Calendar.SECOND));
 				}
 
 				timePicker.setData("FieldType", FieldType.Time);
 				timePicker.setData("FieldName", fieldName);
 
 				break;
 
 			case StarRating:
 				ComponentFactory.getSmallBoldItalicLabel(basicAlbumItemComposite, fieldName + ":");
 
 				final Combo ratingCombo = new Combo(basicAlbumItemComposite, SWT.DROP_DOWN);
 				ratingCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
 
 				// Fill the comboBox
 				ratingCombo.setData("FieldType", FieldType.StarRating);
 				ratingCombo.setData("FieldName", fieldName);
 
 				// TODO use real graphics
 				String ratings[] = {"ZeroStars", "OneStar", "TwoStars", "ThreeStars", "FourStars", "FiveStars"};
 
 				ratingCombo.setItems(ratings);
 
 				if (loadDataIntoFields) {
 					ratingCombo.setText(albumItem.getField(fieldName).getValue().toString());
 				}
 
 				break;
 
 			case Option:
 				ComponentFactory.getSmallBoldItalicLabel(basicAlbumItemComposite, fieldName + ":");
 
 				Composite yesNoComposite = new Composite(basicAlbumItemComposite, SWT.NULL);
 				yesNoComposite.setLayout(new RowLayout());
 				yesNoComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
 				yesNoComposite.setData("FieldType", FieldType.Option);
 				yesNoComposite.setData("FieldName", fieldName);
 
 				Button yesButton = new Button(yesNoComposite, SWT.RADIO);
 				yesButton.setText("Yes");
 				yesButton.setData("yesButton", true);
 				yesButton.setData("noButton", false);
 				yesButton.setData("unknownButton", false);
 				if (loadDataIntoFields) {
 					if (albumItem.getField(fieldName).getValue() == OptionType.Yes) {
 						yesButton.setSelection(true);
 					} else {
 						yesButton.setSelection(false);
 					}
 				}
 
 				Button noButton = new Button(yesNoComposite, SWT.RADIO);
 				noButton.setText("No");
 				noButton.setData("yesButton", false);
 				noButton.setData("noButton", true);
 				noButton.setData("unknownButton", false);
 				if (loadDataIntoFields) {
 					if (albumItem.getField(fieldName).getValue() == OptionType.No) {
 						noButton.setSelection(true);
 					} else {
 						noButton.setSelection(false);
 					}
 				}
 
 				Button unknownButton = new Button(yesNoComposite, SWT.RADIO);
 				unknownButton.setText("Unknown");
 				unknownButton.setData("yesButton", false);
 				unknownButton.setData("noButton", false);
 				unknownButton.setData("unknownButton", true);
 				if (loadDataIntoFields) {
 					if (albumItem.getField(fieldName).getValue() == OptionType.Option) {
 						unknownButton.setSelection(true);
 					} else {
 						unknownButton.setSelection(false);
 					}
 				} else {
 					unknownButton.setSelection(true);
 				}
 
 				break;
 			}
 		}
 
 		if (addPictureComposite) {
 			if (loadDataIntoFields) {
 				ArrayList<URI> uris = albumItem.getField(pictureFieldName).getValue();				
 				ImageDropAndManagementComposite imageDropAndManagementComposite = new ImageDropAndManagementComposite(basicAlbumItemComposite, uris);
 				imageDropAndManagementComposite.setData("FieldType", FieldType.Picture);
 				imageDropAndManagementComposite.setData("FieldName", pictureFieldName);
 			} else {
 				ImageDropAndManagementComposite imageDropAndManagementComposite = new ImageDropAndManagementComposite(basicAlbumItemComposite);					
 				imageDropAndManagementComposite.setData("FieldType", FieldType.Picture);
 				imageDropAndManagementComposite.setData("FieldName", pictureFieldName);
 			}
 		}
 
 		return basicAlbumItemComposite;
 	}
 
 	/** Returns a selection listener suitable for the add and update composite.
 	 * @param composite the composite to which the listener should be attached
 	 * @param isUpdateAlbumItemComposite if true, the listener is used for the update composite, otherwise for the add composite
 	 * @param albumItemId the albumItemId is only used in case isUpdateAlbumItemComposite is set to true
 	 * @return a new selection listener suitable for the add and update composite*/
 	private static SelectionListener getSelectionListenerForAddAndUpdateAlbumItemComposite(final Composite composite, final boolean isUpdateAlbumItemComposite, final long albumItemId) {
 		return new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (!Collector.hasSelectedAlbum()) {
 					Collector.showErrorDialog("No album has been selected", "Please select an album from the list or create one first.");
 					return;
 				}
 				AlbumItem albumItem = new AlbumItem(Collector.getSelectedAlbum());
 
 				for (Control control : composite.getChildren()) {
 					FieldType fieldType = null;
 					if ((fieldType = (FieldType) control.getData("FieldType")) != null) {
 						if (fieldType.equals(FieldType.Text)) {
 							Text text = (Text) control;
 
 							albumItem.addField(
 									(String) text.getData("FieldName"),
 									(FieldType) text.getData("FieldType"),
 									text.getText());
 						} else if (fieldType.equals(FieldType.URL)) {
 							Text url = (Text) control;
 
 							albumItem.addField(
 									(String) url.getData("FieldName"),
 									(FieldType) url.getData("FieldType"),
 									url.getText());
 						} else if (fieldType.equals(FieldType.Number)) {
 							Text text = (Text) control;
 							double number = 0.0;
 
 							if (!text.getText().isEmpty()) {
 								number = Double.parseDouble(text.getText());
 							}
 
 							albumItem.addField(
 									(String) text.getData("FieldName"),
 									(FieldType) text.getData("FieldType"),
 									number);
 						} else if (fieldType.equals(FieldType.Integer)) {
 							Text text = (Text) control;
 							int integer = 0;
 
 							if (!text.getText().isEmpty()) {
 								integer = Integer.parseInt(text.getText());
 							}
 
 							albumItem.addField(
 									(String) text.getData("FieldName"),
 									(FieldType) text.getData("FieldType"),
 									integer);
 						} else if (fieldType.equals(FieldType.Date)) {
 							DateTime dateTime = (DateTime) control;
 
 							Calendar calendar = Calendar.getInstance();
 							calendar.set(dateTime.getYear(), dateTime.getMonth(), dateTime.getDay());
 
 							albumItem.addField(
 									(String) dateTime.getData("FieldName"),
 									(FieldType) dateTime.getData("FieldType"),
 									new Date(calendar.getTimeInMillis()));
 						} else if (fieldType.equals(FieldType.Time)) {
 							DateTime dateTime = (DateTime) control;
 
 							albumItem.addField(
 									(String) dateTime.getData("FieldName"),
 									(FieldType) dateTime.getData("FieldType"),
 									new Time(dateTime.getHours(), dateTime.getMinutes(), dateTime.getSeconds()));
 
 						} else if (fieldType.equals(FieldType.Picture)) {
 							ImageDropAndManagementComposite imageDropAndManagementComposite = (ImageDropAndManagementComposite) control;
 
 							albumItem.addField(
 									(String) imageDropAndManagementComposite.getData("FieldName"),
 									(FieldType) imageDropAndManagementComposite.getData("FieldType"),
 									imageDropAndManagementComposite.getAllImageURIs());
 						} else if (fieldType.equals(FieldType.StarRating)) {							
 							Combo combo = (Combo) control;
 
 							albumItem.addField(
 									(String) combo.getData("FieldName"),
 									(FieldType) combo.getData("FieldType"),
 									StarRating.valueOf(combo.getText()));
 						} else if (fieldType.equals(FieldType.Option)) {
 							Composite yesNoComposite = (Composite) control;
 
 							for (Control yesNoControl : yesNoComposite.getChildren()) {
 								Button radioButton = (Button) yesNoControl;
 
 								if (((Boolean) radioButton.getData("yesButton")) == true) {
 									if (radioButton.getSelection() == true) {
 										albumItem.addField(
 												(String) control.getData("FieldName"),
 												(FieldType) fieldType,
 												OptionType.Yes);
 									}
 								}
 
 								if (((Boolean) radioButton.getData("noButton")) == true) {
 									if (radioButton.getSelection() == true) {
 										albumItem.addField(
 												(String) control.getData("FieldName"),
 												(FieldType) fieldType,
 												OptionType.No);
 									}
 								}
 
 								if (((Boolean) radioButton.getData("unknownButton")) == true) {
 									if (radioButton.getSelection() == true) {
 										albumItem.addField(
 												(String) control.getData("FieldName"),
 												(FieldType) fieldType,
 												OptionType.Option);
 									}
 								}
 							}
 						}
 					}
 				}
 
 				// Update Database and Browser
 				if (isUpdateAlbumItemComposite) {
 					albumItem.addField(
 							"id",
 							FieldType.ID,
 							albumItemId);
 
 					DatabaseWrapper.updateAlbumItem(albumItem);
 					BrowserContent.setFutureJumpAnchor(BrowserContent.getAnchorForAlbumItemId(albumItemId));
 				} else {			
 					BrowserContent.setFutureJumpAnchor(BrowserContent.getAnchorForAlbumItemId(DatabaseWrapper.addNewAlbumItem(albumItem, false, true)));
 				}
 
 				Collector.changeRightCompositeTo(PanelType.Empty, CompositeFactory.getEmptyComposite(Collector.getThreePanelComposite()));
 				WelcomePageManager.getInstance().updateLastModifiedWithCurrentDate(Collector.getSelectedAlbum());
 				
 				BrowserContent.performBrowserQueryAndShow(Collector.getAlbumItemSWTBrowser(), DatabaseWrapper.createSelectStarQuery(Collector.getSelectedAlbum()));
 			}
 		};
 	}
 
 	/** Returns an "add album item" composite. This composite provides the fields (field names and value input fields)
 	 *  needed by the add item composite.
 	 * @param parentComposite the parent composite
 	 * @param album the name of the album to which an item should be added
 	 * @return a new "add album item" composite */
 	public static Composite getAddAlbumItemComposite(Composite parentComposite, final String album) {
 		Composite resizeComposite = new Composite(parentComposite, SWT.NONE);
 		resizeComposite.setLayout(new GridLayout(1, false));
 		resizeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 
 		// description (header) label
 		ComponentFactory.getPanelHeaderComposite(resizeComposite, "Add Entry");
 
 		// Setup ScrolledComposite containing an normal (basic) Composite
 		ScrolledComposite scrolledComposite = new ScrolledComposite(resizeComposite,  SWT.NONE | SWT.H_SCROLL | SWT.V_SCROLL );
 		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 
 		final Composite addAlbumItemComposite = getBasicAlbumItemComposite(scrolledComposite, album);
 		scrolledComposite.setContent(addAlbumItemComposite);
 		scrolledComposite.setExpandHorizontal(true);
 		scrolledComposite.setExpandVertical(true);
 		scrolledComposite.getHorizontalBar().setIncrement(scrolledComposite.getHorizontalBar().getIncrement()*SCROLL_SPEED_MULTIPLICATOR);
 
 		// Add Button
 		Button addButton = new Button(addAlbumItemComposite, SWT.PUSH);
 		addButton.setText("Add Entry");
 		addButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		addButton.addSelectionListener(getSelectionListenerForAddAndUpdateAlbumItemComposite(addAlbumItemComposite, false, 0));
 
 		// Calculating the size of the scrolled composite at the end 
 		// avoids having crushed buttons and text-fields..
 		scrolledComposite.setMinSize(addAlbumItemComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 
 		return resizeComposite;
 	}
 
 	/** Returns an "update album item" composite. This composite provides the fields (field names and value input fields)
 	 *  needed by the update item composite.
 	 * @param parentComposite the parent composite
 	 * @param album the name of the album to which an item should be added
 	 * @param albumItemId the id of the album item whose content is loaded into the fields
 	 * @return a new "update album item" composite */
 	public static Composite getUpdateAlbumItemComposite(Composite parentComposite, final String album, final long albumItemId) {
 		Composite resizeComposite = new Composite(parentComposite, SWT.NONE);
 		resizeComposite.setLayout(new GridLayout(1, false));
 		resizeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 
 		// description (header) label
 		ComponentFactory.getPanelHeaderComposite(resizeComposite, "Update Entry");
 
 		// Setup ScrolledComposite containing an normal (basic) Composite
 		ScrolledComposite scrolledComposite = new ScrolledComposite(resizeComposite,  SWT.NONE | SWT.H_SCROLL | SWT.V_SCROLL );
 		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 
 		final Composite updateAlbumItemComposite = 
 				getBasicAlbumItemComposite(scrolledComposite, album, albumItemId);
 		scrolledComposite.setContent(updateAlbumItemComposite);
 		scrolledComposite.setExpandHorizontal(true);
 		scrolledComposite.setExpandVertical(true);
 		scrolledComposite.getHorizontalBar().setIncrement(scrolledComposite.getHorizontalBar().getIncrement()*SCROLL_SPEED_MULTIPLICATOR);
 
 		// Add Button
 		Button updateButton = new Button(updateAlbumItemComposite, SWT.PUSH);
 		updateButton.setText("Update Item");
 		updateButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
 		updateButton.addSelectionListener(
 				getSelectionListenerForAddAndUpdateAlbumItemComposite(updateAlbumItemComposite, true, albumItemId));
 
 		// Calculating the size of the scrolled composite at the end 
 		// avoids having crushed buttons and text-fields..
 		scrolledComposite.setMinSize(updateAlbumItemComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 
 		return resizeComposite;
 	}
 
 	public static Composite getSynchronizeComposite(Composite parentComposite) {		
 		// setup synchronize composite
 		Composite synchronizeComposite = new Composite(parentComposite, SWT.NONE);
 		synchronizeComposite.setLayout(new GridLayout(1, false));
 		synchronizeComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		// label header
 		ComponentFactory.getPanelHeaderComposite(synchronizeComposite, "Synchronize");
 
 		// min height griddata
 		GridData minHeightGridData = new GridData(GridData.FILL_BOTH);
 		minHeightGridData.minimumHeight = 20;
 
 		final Button startButton = new Button(synchronizeComposite, SWT.PUSH);
 		startButton.setText("Start Synchronization");
 		startButton.setLayoutData(new GridData(GridData.FILL_BOTH));
 		// listener after cancel button since this button reference is needed
 
 		// separator
 		new Label(synchronizeComposite, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(minHeightGridData);
 
 		final HashMap<SynchronizeStep, Label> synchronizeStepsToLabelsMap = new HashMap<SynchronizeStep, Label>();
 
 		final Label establishConnectionLabel = ComponentFactory.getH4Label(synchronizeComposite, "\u25CF Establishing Connection");
 		establishConnectionLabel.setLayoutData(minHeightGridData);
 		establishConnectionLabel.setEnabled(false);
 		synchronizeStepsToLabelsMap.put(SynchronizeStep.ESTABLISH_CONNECTION, establishConnectionLabel);
 
 		final Label uploadDataLabel = ComponentFactory.getH4Label(synchronizeComposite, "\u25CF Upload Data");
 		uploadDataLabel.setLayoutData(minHeightGridData);
 		uploadDataLabel.setEnabled(false);
 		synchronizeStepsToLabelsMap.put(SynchronizeStep.UPLOAD_DATA, uploadDataLabel);
 
 		final Label installDataLabel = ComponentFactory.getH4Label(synchronizeComposite, "\u25CF Install Data");
 		installDataLabel.setLayoutData(minHeightGridData);
 		installDataLabel.setEnabled(false);
 		synchronizeStepsToLabelsMap.put(SynchronizeStep.INSTALL_DATA, installDataLabel);
 
 		final Label finishLabel = ComponentFactory.getH4Label(synchronizeComposite, "\u25CF Finish");
 		finishLabel.setLayoutData(minHeightGridData);
 		finishLabel.setEnabled(false);
 		synchronizeStepsToLabelsMap.put(SynchronizeStep.FINISH, finishLabel);
 
 		// Add Observers
 		SynchronizeCompositeHelper synchronizeCompositeHelper = new SynchronizeCompositeHelper();
 		synchronizeCompositeHelper.storeSynchronizeCompositeLabels(synchronizeStepsToLabelsMap);
 		NetworkGateway.getInstance().addObserver(synchronizeCompositeHelper);
 
 		// separator
 		new Label(synchronizeComposite, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(minHeightGridData);
 
 		final Button cancelButton = new Button(synchronizeComposite, SWT.PUSH);
 		cancelButton.setText("Cancel Synchronization");
 		cancelButton.setEnabled(false);
 		cancelButton.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		startButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				establishConnectionLabel.setEnabled(true);
 				cancelButton.setEnabled(true);
 				startButton.setEnabled(false);
 
 				NetworkGateway.startSynchronization();
 			}
 		});
 
 		cancelButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				establishConnectionLabel.setEnabled(false);
 				uploadDataLabel.setEnabled(false);
 				installDataLabel.setEnabled(false);
 				finishLabel.setEnabled(false);
 				cancelButton.setEnabled(false);
 				startButton.setEnabled(true);
 
 				NetworkGateway.stopSynchronization();
 			}
 		});
 
 		return synchronizeComposite;
 	}
 
 	/** Returns all valid MetaItemFields. Hereby valid means that only user editable fields are returned
 	 * @param metaItemFields the list of all available meta item fields
 	 * @return a list containing only valid meta item fields */
 	private static java.util.List<MetaItemField> getValidMetaItemFields(java.util.List<MetaItemField> metaItemFields) {
 		java.util.List<MetaItemField> validMetaItemFields = new ArrayList<MetaItemField>();
 		java.util.List<String> validFieldTypes = Arrays.asList(FieldType.toUserTypeStringArray());
 
 		for (MetaItemField metaItemField : metaItemFields) {
 			if (validFieldTypes.contains(metaItemField.getType().toString())) {
 				validMetaItemFields.add(metaItemField);
 			}
 		}
 
 		return validMetaItemFields;
 	}
 
 
 	/** Returns all valid field names. Hereby valid means that only user editable fields are returned 
 	 * @param metaItemFields the list of all available meta item fields
 	 * @return a string array containing only valid field names */
 	private static String[] getValidFieldNamesAsStringArray(java.util.List<MetaItemField> metaItemFields) {
 		java.util.List<MetaItemField> validMetaItemFields = new ArrayList<MetaItemField>();
 		java.util.List<String> validFieldTypes = Arrays.asList(FieldType.toUserTypeStringArray());
 
 		for (MetaItemField metaItemField : metaItemFields) {
 			if (validFieldTypes.contains(metaItemField.getType().toString())) {
 				validMetaItemFields.add(metaItemField);
 			}
 		}
 
 		String[] validMetaItemFieldsAsStringArray = new String[validMetaItemFields.size()];
 		for(int i=0; i<validMetaItemFields.size(); i++) {
 			validMetaItemFieldsAsStringArray[i] = validMetaItemFields.get(i).getName();
 		}
 
 		return validMetaItemFieldsAsStringArray;
 	}
 }
