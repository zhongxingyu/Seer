 package cpsc310.client;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.maps.client.InfoWindowContent;
 import com.google.gwt.maps.client.MapWidget;
 import com.google.gwt.maps.client.Maps;
 import com.google.gwt.maps.client.control.LargeMapControl;
 import com.google.gwt.maps.client.geom.LatLng;
 import com.google.gwt.maps.client.overlay.Marker;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.DockLayoutPanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.ColumnSortEvent;
 import com.google.gwt.user.cellview.client.TextColumn;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.view.client.AsyncDataProvider;
 import com.google.gwt.view.client.DefaultSelectionEventManager;
 import com.google.gwt.view.client.HasData;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.google.gwt.cell.client.NumberCell;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.cell.client.CheckboxCell;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;
 
 
 
 /**
  * Entry point classes define onModuleLoad().
  */
 
 public class Team_02 implements EntryPoint {
 
 	private VerticalPanel mainPanel = new VerticalPanel();
 	private HorizontalPanel loginPanel = new HorizontalPanel();	
 	private SimplePanel mapContainerPanel = new SimplePanel();	
 	private TabPanel controlPanel = new TabPanel();	
 	private VerticalPanel searchPanel = new VerticalPanel();
 	private VerticalPanel editPanel = new VerticalPanel();	
 	private HorizontalPanel lowerWrapPanel = new HorizontalPanel();
 	private VerticalPanel tableWrapPanel = new VerticalPanel();	
 	private CellTable.Resources resource = GWT.create(CellTableResources.class);
 	private CellTable<HouseData> homesCellTable = 
 			new CellTable<HouseData>(3, resource, HouseData.KEY_PROVIDER);
 	private SimplePager simplePager = new SimplePager();	
 	private	FlexTable searchSettingsFlexTable = new FlexTable();
 	private TextBox lowerCoordTextBox = new TextBox();
 	private TextBox upperCoordTextBox = new TextBox();
 	private TextBox lowerLandValTextBox = new TextBox();
 	private TextBox upperLandValTextBox = new TextBox();
 	private TextBox ownerTextBox = new TextBox();	
 	private Button searchBtn = new Button("Search");
 	private FlexTable editFlexTable = new FlexTable();
 	private Button editBtn = new Button("Edit");
 	private TextBox priceTextBox = new TextBox();
 	private Label propAddrLabel = new Label();
 	private RadioButton yesSellingRdBtn = new RadioButton("isSelling", "Yes");
 	private RadioButton noSellingRdBtn = new RadioButton("isSelling", "No");
 	private Button loginBtn = new Button("Login");
 	private Button logoutBtn = new Button("Log out");
 	private HouseData selectedHouse = null;
 	private HouseDataServiceAsync houseDataSvc = GWT.create(HouseDataService.class);
 	private AsyncDataProvider<HouseData> dataProvider;
 	private propertyMap theMap;
 	
 	/**
 	 * This is the entry point method.
 	 */
 	public void onModuleLoad() {	
 
 	    // Assemble Login Panel
 	    loginPanel.add(loginBtn);
 		loginPanel.add(logoutBtn);
 		
 		// The map
 		theMap = new propertyMap();
 		theMap.buildUi();
 		theMap.findLocation("4572 3RD AVE W VANCOUVER");
 
 		// Assemble map panel
 		mapContainerPanel.add(theMap.getMap());
 		 
 		// Create Cell Table
 		initCellTable();
 								
 		// Create Search Criteria table. **** Possibly replace with HTML form panel
 		searchSettingsFlexTable.setText(0, 0,"Coordinates");
 		searchSettingsFlexTable.setWidget(0, 1, lowerCoordTextBox);
 		searchSettingsFlexTable.setText(0, 2, "-");	
 		searchSettingsFlexTable.setWidget(0, 3, upperCoordTextBox);
 		searchSettingsFlexTable.setText(1, 0, "Land Value");
 		searchSettingsFlexTable.setWidget(1, 1, lowerLandValTextBox);
 		searchSettingsFlexTable.setText(1, 2,"-");
 		searchSettingsFlexTable.setWidget(1, 3, upperLandValTextBox);
 		searchSettingsFlexTable.setText(2, 0, "Realtor");
 		searchSettingsFlexTable.setWidget(2, 1, ownerTextBox);
 		
 		// Format Search Criteria table
 		searchSettingsFlexTable.getFlexCellFormatter().setColSpan(2, 1, 3);
 		searchSettingsFlexTable.setCellSpacing(10);
 		
 		// Assemble Search panel
 		searchPanel.add(searchSettingsFlexTable);
 		searchPanel.add(searchBtn);
 		searchPanel.setCellVerticalAlignment(searchBtn, HasVerticalAlignment.ALIGN_MIDDLE);
 		searchPanel.setCellHorizontalAlignment(searchBtn, HasHorizontalAlignment.ALIGN_CENTER);
 		
 		// Assemble Control Tab panel
 		controlPanel.add(searchPanel, "Search", false);
 		controlPanel.selectTab(0);
 		controlPanel.setAnimationEnabled(true);
 		controlPanel.setHeight("300px");
 		controlPanel.setWidth("330px");
 
 	  	// Assemble tableWrapPanel
 	  	tableWrapPanel.add(homesCellTable);
 	  	tableWrapPanel.add(simplePager);
 	  	tableWrapPanel.setCellHorizontalAlignment(simplePager, HasHorizontalAlignment.ALIGN_CENTER);
 		
 		// Assemble lowerWrapPanel
 		lowerWrapPanel.add(controlPanel);	  	
 	  	lowerWrapPanel.add(tableWrapPanel);
 	  	
 		// Assemble Main Panel
 	  	mainPanel.setWidth("80%");
 		mainPanel.add(loginPanel);		
 		mainPanel.add(mapContainerPanel);	
 		mainPanel.add(lowerWrapPanel);
 	  	
 		// Set style
 		loginPanel.setStyleName("loginPanel");		 
 		mainPanel.setStylePrimaryName("mainPanel");
 		mapContainerPanel.setStyleName("mapContainerPanel");
 		lowerWrapPanel.setStyleName("lowerWrapPanel");
 		searchSettingsFlexTable.getCellFormatter().setStyleName(0, 0, "searchText");
 		searchSettingsFlexTable.getCellFormatter().setStyleName(1, 0, "searchText");
 		searchSettingsFlexTable.getCellFormatter().setStyleName(2, 0, "searchText");
 		
 		allowEdit();		
 		
 		// Associate Main panel with the HTML host page
 		RootPanel.get("appPanel").add(mainPanel);
 		
 
 		
 		// Listen for mouse events on Search
 		searchBtn.addClickHandler(new ClickHandler() {
 			public void onClick (ClickEvent event) {
 				searchHouse();
 			}
 		});		
 		
 	}
 	
 	/**
 	 * Initializes homesCellTable.
 	 * Creates cell columns, adds those columns to the table,
 	 * populates the table using dataProvider, creates sort handler,
 	 * sets sort comparators, then sets the column width of the table. 
 	 * Note: sorting and populating will be replaced by server-side methods.
 	 */
 	private void initCellTable() {
 	  	// Create cell columns
 	  	Column<HouseData, Number> pidColumn = 
	  			new Column<HouseData, Number>(new NumberCell()) {
 	  		@Override
 	  		public Number getValue(HouseData house) {
 	  			return house.getPID();
 	  		}
 	  	};	  	
 	  	TextColumn<HouseData> addrColumn = 
 	  			new TextColumn<HouseData>() {
 	  		@Override
 	  		public String getValue(HouseData house) {
 	  			return house.getAddress();
 	  		}
 	  	};
 	  	TextColumn<HouseData> postalColumn = 
 	  			new TextColumn<HouseData>() {
 			@Override
 			public String getValue(HouseData house) {
 				return house.getPostalCode();
 			}
 		};
 	  	Column<HouseData, Number> coordColumn = 
 	  			new Column<HouseData, Number>(new NumberCell()) {
 	  		@Override
 	  		public Number getValue(HouseData house) {
 	  			return house.getCoordinate();
 	  		}
 	  	};
 	  	Column<HouseData, Number> landValColumn = 
 	  			new Column<HouseData, Number>(new NumberCell()) {
 	  		@Override
 	  		public Number getValue(HouseData house) {
 	  			return house.getLandValue();
 	  		}
 	  	};
 	  	TextColumn<HouseData> ownerColumn = 
 	  			new TextColumn<HouseData>() {
 	  		@Override
 	  		public String getValue(HouseData house) {
 	  			return house.getOwner();
 	  		}
 	  	};		
 	  	Column<HouseData, Number> priceColumn = 
 	  			new Column<HouseData, Number>(new NumberCell()) {
 	  		@Override
 	  		public Number getValue(HouseData house) {
 	  			return house.getPrice();
 	  		}
 	  	};
 	  	TextColumn<HouseData> isSellingColumn = 
 	  			new TextColumn<HouseData>() {
 	  		@Override
 	  		public String getValue(HouseData house) {
 	  			if (house.getIsSelling())
 	  				return "For Sale";
 	  			return "";
 	  		}
 	  	};
 	  	
 	  	// Enable sorting by clicking on the column headings
 	  	pidColumn.setSortable(true);
 	  	addrColumn.setSortable(true);
 	  	postalColumn.setSortable(true);
 	  	coordColumn.setSortable(true);
 	  	landValColumn.setSortable(true);
 	  	ownerColumn.setSortable(true);
 	  	priceColumn.setSortable(true);
 	  	isSellingColumn.setSortable(true);
 
 	  	
 	  	// Add columns to the table
 	  	homesCellTable.addColumn(pidColumn, "PID");
 	  	homesCellTable.addColumn(addrColumn, "Address");		
 	  	homesCellTable.addColumn(postalColumn, "Postal Code");		
 	  	homesCellTable.addColumn(coordColumn, "Land Coordinate");		
 	  	homesCellTable.addColumn(landValColumn, "Current Value");				
 	  	homesCellTable.addColumn(ownerColumn, "Owner");
 	  	homesCellTable.addColumn(priceColumn, "Price");
 	  	homesCellTable.addColumn(isSellingColumn, "Sale");	  	
 		simplePager.setDisplay(homesCellTable);
 		simplePager.setStylePrimaryName("pager");
 		
 		// Grab database length
 		AsyncCallback<Integer> callback = new AsyncCallback<Integer> () {
 			@Override
 			public void onFailure (Throwable caught) {
 			Window.alert(caught.getMessage());	
 			}
 			@Override
 			public void onSuccess (Integer result) {
 				homesCellTable.setRowCount(result);
 			}
 		};
 		houseDataSvc.getHouseDatabaseLength(callback);		
 		
 		// Data provider to populate Table
 		dataProvider = new AsyncDataProvider<HouseData>() {
 			@Override
 			protected void onRangeChanged(HasData<HouseData> display) {
 				final int start = display.getVisibleRange().getStart();
 				int range = display.getVisibleRange().getLength();
 				
 				AsyncCallback<List<HouseData>> callback = new AsyncCallback<List<HouseData>> () {
 					@Override
 					public void onFailure (Throwable caught) {
 					Window.alert(caught.getMessage());	
 					}
 					@Override
 					public void onSuccess (List<HouseData> result) {
 						updateRowData(start, result);
 					}
 				};
 				houseDataSvc.getHouses(start, range, callback);
 			}
 		};
 		
 		dataProvider.addDataDisplay(homesCellTable);
 		
 		// Create sort handler, associate sort handler to the table		
 		homesCellTable.addColumnSortHandler( new ColumnSortEvent.Handler() {
 			public void onColumnSort(ColumnSortEvent event) {
 				@SuppressWarnings("unchecked")
 				Column<HouseData,?> sortedColumn = (Column<HouseData, ?>) event.getColumn();
 				int sortedIndex = homesCellTable.getColumnIndex(sortedColumn);
 				List<HouseData> newData = new ArrayList<HouseData>(homesCellTable.getVisibleItems());
 				
 				Comparator<HouseData> c = HouseData.HousePidComparator;
 				switch(sortedIndex) {
 				case 0:
 					c = HouseData.HousePidComparator;
 					break;
 				case 1:
 					c = HouseData.HouseAddrComparator;
 					break;
 				case 2:
 					c = HouseData.HousePostalCodeComparator;
 					break;
 				case 3:
 					c = HouseData.HouseCoordinateComparator;
 					break;
 				case 4:
 					c = HouseData.HouseLandValueComparator;
 					break;
 				case 5:
 					c = HouseData.HouseOwnerComparator;
 					break;
 				case 6:
 					c = HouseData.HousePriceComparator;
 					break;
 				case 7:
 					c = HouseData.HouseIsSellingComparator;
 					break;
 				default:
 					break;
 				}
 				if (event.isSortAscending()) {
 					Collections.sort(newData, c);
 				}
 				else {
 					Collections.sort(newData, Collections.reverseOrder(c));
 				}
 				homesCellTable.setRowData(homesCellTable.getPageStart(), newData);
 			}
 		});
 			
 		/* Server side sorting for sprint 2
 		AsyncHandler columnSortHandler = new AsyncHandler(homesCellTable);
 		homesCellTable.addColumnSortHandler(columnSortHandler);
 		*/
 		
 		/* Old Sorting methods
 		List<HouseData> localData = new ArrayList<HouseData>(homesCellTable.getVisibleItems());
 		ListHandler<HouseData> sortHandler = new ListHandler<HouseData>(localData);
 		
 		homesCellTable.addColumnSortHandler(sortHandler);
 		
 		// Set comparators for sorting
 		sortHandler.setComparator(pidColumn, HouseData.HousePidComparator);	
 		sortHandler.setComparator(addrColumn, HouseData.HouseAddrComparator);
 		sortHandler.setComparator(postalColumn, HouseData.HousePostalCodeComparator);
 		sortHandler.setComparator(coordColumn, HouseData.HouseCoordinateComparator);
 		sortHandler.setComparator(landValColumn, HouseData.HouseLandValueComparator);
 		sortHandler.setComparator(ownerColumn, HouseData.HouseOwnerComparator);
 		sortHandler.setComparator(priceColumn, HouseData.HousePriceComparator);
 		sortHandler.setComparator(isSellingColumn, HouseData.HouseIsSellingComparator);
 		*/
 		
 		homesCellTable.setColumnWidth(addrColumn, 120.0, Unit.PX);
 		homesCellTable.setColumnWidth(ownerColumn,100.0, Unit.PX);
 		/*
 		// Set column width
 	  	homesCellTable.setColumnWidth(pidColumn, 20.0, Unit.PX);
 	  	homesCellTable.setColumnWidth(postalColumn, 20.0, Unit.PX);
 	  	homesCellTable.setColumnWidth(coordColumn, 20.0, Unit.PX);
 	  	homesCellTable.setColumnWidth(landValColumn, 20.0, Unit.PX);
 	  	homesCellTable.setColumnWidth(ownerColumn, 30.0, Unit.PX);
 	  	homesCellTable.setColumnWidth(priceColumn, 20.0, Unit.PX);
 	  	homesCellTable.setColumnWidth(isSellingColumn, 20.0, Unit.PX);		
 		homesCellTable.setWidth("auto", true);
 		*/
 	}
 
 	/**
 	 * Enables Edit function in the app. Called upon user login.
 	 * To enable edit, initializes selection in the CellTable.
 	 * Then creates Edit criteria tab and adds to the main controlTab.
 	 */
 	private void allowEdit() {
 		
 		// Initialize table selection
 		initCellTableSelection();	
 		
 		// Create Edit Criteria table.
 		editFlexTable.setText(0, 0, "Property Address");
 		editFlexTable.setWidget(0, 1, propAddrLabel);
 		editFlexTable.setText(1, 0, "Price");
 		editFlexTable.setWidget(1, 1, priceTextBox);
 		editFlexTable.setText(2, 0, "For Sale");
 		editFlexTable.setWidget(2, 1, yesSellingRdBtn);		
 		editFlexTable.setWidget(2, 2, noSellingRdBtn);		
 		noSellingRdBtn.setValue(true);
 		
 		// Format Edit Criteria Table
 		editFlexTable.getFlexCellFormatter().setColSpan(0, 1, 2);
 		editFlexTable.getFlexCellFormatter().setColSpan(1, 1, 2);
 		editFlexTable.setCellSpacing(15);
 		editFlexTable.getCellFormatter().setStyleName(0, 0, "searchText");
 		editFlexTable.getCellFormatter().setStyleName(1, 0, "searchText");
 		editFlexTable.getCellFormatter().setStyleName(2, 0, "searchText");
 		
 		// Assemble Edit panel
 		editPanel.add(editFlexTable);
 		editPanel.add(editBtn);
 		editPanel.setCellVerticalAlignment(editBtn, HasVerticalAlignment.ALIGN_MIDDLE);
 		editPanel.setCellHorizontalAlignment(editBtn, HasHorizontalAlignment.ALIGN_CENTER);
 		
 		// Add Edit tab to the controlPanel
 		controlPanel.add(editPanel, "Edit", false);
 		
 		// Listen for mouse events on Edit
 		editBtn.addClickHandler(new ClickHandler() {
 			public void onClick (ClickEvent event) {
 				editHouse();
 			}
 		});
 	}
 
 	/**
 	 * Creates selection column which selects/de-selects a HouseDataPoint 
 	 * upon clicking the check box.
 	 */
 	private void initCellTableSelection() {
 		
 		// Create selection model
 		final SingleSelectionModel<HouseData> selectionModel = 
 				new SingleSelectionModel<HouseData> (HouseData.KEY_PROVIDER);
 						
 		// Associate the selection model with the table
 		homesCellTable.setSelectionModel(selectionModel, 
 				DefaultSelectionEventManager.<HouseData> createCheckboxManager());
 		
 		// Handle selection event. Upon selection the address label in the edit panel 
 		// is updated with the selected house address.
 		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 			@Override
 			public void onSelectionChange(SelectionChangeEvent event) {
 				HouseData selected = selectionModel.getSelectedObject();
 				if (selected == null) {
 					propAddrLabel.setText(null);
 					theMap.removeMarker(selectedHouse.getAddress() + " VANCOUVER");
 					setSelectedHouse(null);			
 					return;
 				}
 				propAddrLabel.setText(selected.getAddress());
 				setSelectedHouse(selected);
 				// add marker onto map
 				theMap.findLocation(selected.getAddress() + " VANCOUVER");
 			}
 		});
 		
 		// Create checkBox column		
 		final Column<HouseData, Boolean> selectRowColumn = 
 				new Column<HouseData, Boolean>(new CheckboxCell(true, false)) {
 			@Override
 			public Boolean getValue(HouseData house) {
 				return selectionModel.isSelected(house);
 			}
 		};
 		
 		// Add checkBox column to the table
 		homesCellTable.setColumnWidth(selectRowColumn, 10.0, Unit.PX);
 		homesCellTable.addColumn(selectRowColumn, "Select");
 	
 	}
 	
 	/**
 	 * Helper to table selection event
 	 * Sets class variable selectedHouse to the given argument
 	 * 
 	 * @param house
 	 */
 	private void setSelectedHouse(HouseData house) {
 		selectedHouse = house;
 	}
 
 	/**
 	 * Gets user input from search tab and passes to server-side search
 	 */
 	private void searchHouse() {
 		boolean isCoordEmpty = true;
 		boolean isLandValEmpty = true;
 		boolean isOwnerEmpty = true;
 		final String lowerCoordInput = lowerCoordTextBox.getText().trim();
 		final String upperCoordInput = upperCoordTextBox.getText().trim();
 		final String lowerLandValInput = lowerLandValTextBox.getText().trim();
 		final String upperLandValInput = upperLandValTextBox.getText().trim();
 		final String ownerInput = ownerTextBox.getText().trim().toUpperCase();
 		
 		String checkDigit = "|[0-9]+[/.]?[0-9]*";
 		//String checkPostalCode = "|[A-Z][0-9][A-Z][ ][0-9][A-Z][0-9]";
 		String checkOwner = "|[A-Z]+[ ]?[A-Z]*";
 		
 		String numericAlert = "Only numeric values are allowed for coordinates and land values. \n";
 		String ownerAlert = "Only alphabet characters are allowed for realtor name";
 		String msg = "";
 		boolean checkNumeric = true;
 		boolean checkAlpha = true;
 	
 		// Coordinates and land values must be numeric value
 		if (!lowerCoordInput.matches(checkDigit) || !upperCoordInput.matches(checkDigit) ||
 				!lowerLandValInput.matches(checkDigit) || !upperLandValInput.matches(checkDigit)) {
 			checkNumeric = false;
 			msg = msg.concat(numericAlert);
 		}		
 		
 		// Realtor name must be alphabetical
 		if (!ownerInput.matches(checkOwner)) {
 			checkAlpha = false;
 			msg = msg.concat(ownerAlert);
 			ownerTextBox.selectAll();
 		}
 		
 		if (checkNumeric == false || checkAlpha == false) {
 			Window.alert(msg);
 			return;
 		}
 	
 		// Assemble search criteria
 		if (!lowerCoordInput.isEmpty() && !upperCoordInput.isEmpty()) {
 			isCoordEmpty = false;
 		}
 		else if (!lowerLandValInput.isEmpty() && !upperLandValInput.isEmpty()) {
 			isLandValEmpty = false;
 		}
 		else if (!ownerInput.isEmpty()) {
 			isOwnerEmpty = false;
 		}
 		
 		// TODO Server-side search
 		// Initailize the service proxy
 		if (houseDataSvc == null) {
 			houseDataSvc = GWT.create(HouseDataService.class);
 		}
 		
 		// Set up the callback object
 		AsyncCallback<List<HouseData>> callback = new AsyncCallback<List<HouseData>>() {
 			public void onFailure(Throwable caught) {
 				Window.alert(caught.getMessage());
 			}
 			public void onSuccess(List<HouseData> result) {
 				// TODO Display search results on the table
 				dataProvider.updateRowCount(result.size(), true);
 				dataProvider.updateRowData(0, result);
 			}
 		};
 		
 		// make the call to the house data service
 		if (isLandValEmpty == false) {
 			int lowerLandVal = Integer.parseInt(lowerLandValInput);
 			int upperLandVal = Integer.parseInt(upperLandValInput);
 			houseDataSvc.getHousesByPriceRange(lowerLandVal, upperLandVal, callback);
 		}
 		else if (isCoordEmpty == false) {
 			int lowerCoord = Integer.parseInt(lowerCoordInput);
 			int upperCoord = Integer.parseInt(upperCoordInput);			
 			houseDataSvc.getHousesByCoordRange(lowerCoord, upperCoord, callback);
 		}
 		else if (isOwnerEmpty == false) {
 			String owner = ownerInput;
 			houseDataSvc.getHousesByOwner(owner, callback);
 		}
 		else {
 			return;
 		}
 	}
 
 	/**
 	 * This is for Sprint 2
 	 * Edit handler.
 	 * Gets user input in the edit tab, checks for valid input,
 	 * then passes on to the server-side edit
 	 */
 	private void editHouse() {
 		final String priceInput = priceTextBox.getText().trim();
 		final boolean isSellingInput = yesSellingRdBtn.getValue();
 		final HouseData house = selectedHouse;  
 		
 		// Check for selection
 		if (house == null) {
 			Window.alert("Please select a house to edit");
 			return;
 		}
 		
 		// Price input must be numerical
 		if (!priceInput.matches("|[0-9]*")) {
 			Window.alert("Only non-decimal numeric value is allowed for price.");
 			priceTextBox.selectAll();
 			return;
 		}
 		
 		// Assemble edit request
 		double price = Double.parseDouble(priceInput);
 		boolean isSelling;
 		if (isSellingInput == true) 
 			isSelling = true;
 		else
 			isSelling = false;
 		// TODO connect to user service to get owner
 		String owner = "";
 		
 		// TODO Server-side edit
 		// Initialize the service proxy
 		if (houseDataSvc == null) {
 			houseDataSvc = GWT.create(HouseDataService.class);
 		}
 		
 		// Set up the callback object
 		AsyncCallback<Void> callback = new AsyncCallback<Void>() {
 			public void onFailure(Throwable caught) {
 				Window.alert(caught.getMessage());
 			}
 			public void onSuccess(Void result) {
 				// TODO Display edit results on the table
 			}
 		};
 		
 		// make the call to the house data service
 		houseDataSvc.updateHouses(owner, price, isSelling, house, callback);
 				
 		// Refresh table
 		refreshHouse(house);
 	}
 	
 	/**
 	 * This is for Sprint 2
 	 * Helper to call server-side refresh upon EDIT request
 	 * @param house
 	 */
 	private void refreshHouse(HouseData house) {
 		homesCellTable.redraw();
 		// TODO may need to reimplement
 	}
 	
 }
