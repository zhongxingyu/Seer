 package ch.ubx.startlist.client.ui;
 
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import ch.ubx.startlist.client.AirfieldServiceDelegate;
 import ch.ubx.startlist.client.FlightEntryListeProvider;
 import ch.ubx.startlist.client.FlightEntryServiceDelegate;
 import ch.ubx.startlist.client.GwtUtil;
 import ch.ubx.startlist.client.LoginServiceDelegate;
 import ch.ubx.startlist.client.PilotServiceDelegate;
 import ch.ubx.startlist.client.RowDoubleclickHandler;
 import ch.ubx.startlist.client.RowSelectionHandler;
 import ch.ubx.startlist.client.TimeFormat;
 import ch.ubx.startlist.client.admin.ui.AdminGUI;
 import ch.ubx.startlist.shared.Airfield;
 import ch.ubx.startlist.shared.FlightEntry;
 import ch.ubx.startlist.shared.LoginInfo;
 import ch.ubx.startlist.shared.Pilot;
 import ch.ubx.startlist.shared.TextConstants;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FocusWidget;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.StackPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class FlightEntryListGUI implements TimeFormat, TextConstants {
 
 	private static final String STATUS_ROOT_PANEL = "flightEntryStatus";
 	private static final String STACK_ROOT_PANEL = "flightEntryToolBar";
 	private static final String LOGIN_ROOT_PANEL = "loginrootpanel";
 
 	/* GUI Widgets */
 	public Button newButton;
 	public Button modifyButton;
 	public Button deleteButton;
 	public Button discardButton;
 	public Button saveButton;
 
 	protected TextBox registrationGliderBox;
 	protected TextBox registrationTowplaneBox;
 
 	protected DateBox2 startDateBox;
 	protected DateBox2 endGliderDateBox;
 	protected DateBox2 endTowplaneDateBox;
 
 	protected Label status;
 
 	protected StackPanel stackPanel;
 
 	public ListBox placeListBox;
 	public ListBox yearListBox;
 
 	public ListBox dateListBox;
 
 	public Button prevDayPushButton;
 	public Button nextDayPushButton;
 	public FlexTable flightEntryFlexTable;
 
 	protected DateBox2 dateBox;
 	protected SuggestBox2 pilotNameBox;
 	protected SuggestBox2 passengerOrInstructorNameBox;
 	protected SuggestBox2 towplanePilotNameBox;
 	protected CheckBox trainingCheckBox;
 	protected TextBox remarksTextBox;
 	protected SuggestBox2 landingPlacesSuggestBox;
 
 	public Button btnClose;
 
 	public FlightEntryServiceDelegate flightEntryService;
 	public LoginServiceDelegate loginServiceDelegate;
 	public AirfieldServiceDelegate airfieldServiceDelegate;
 	public PilotServiceDelegate pilotServiceDelegate;
 
 	/* Data model */
 	private FlightEntry currentFlightEntry;
 	private VerticalPanel mainPanel;
 	private DynaTableWidget dynaTableWidget;
 
 	private FlightEntryListeProvider provider = new FlightEntryListeProvider();
 
 	private VerticalPanel verticaPanel_2;
 	private RowSelectionHandler rowSelectionHandler = null;
 	private RowDoubleclickHandler rowDoubleclickHandler = null;
 	private Map<String, Long> strToDate = new LinkedHashMap<String, Long>();
 	private ListBox allPlacesListBox;
 	private MultiWordSuggestOracle landingPlacesSuggest = new MultiWordSuggestOracle();
 	private MultiWordSuggestOracle pilotNamesSuggest = new MultiWordSuggestOracle();
 	private Set<Airfield> allAirfields;
 	private DialogBox flightEntryDialogBox;
 	private FlightEntryValidator validator;
 	private HorizontalPanel operationNewModDel;
 	private Anchor signInLink;
 	private Label loggedInAs;
 	private LoginInfo currentLoginInfo;
 	private HTML excelLinkHTML;
 
 	private AdminGUI adminGUI;
 	private FlightEntry lastflightEntry;
 	
 	// Get preferred place from url. If not defined null
 	private String prefPlace = Window.Location.getParameter("place");
 	
 	// Get the current version
 	//private String version = SystemProperty.version.get(); // TODO -- display somewhere on screen.
 
 
 	/**
 	 * @wbp.parser.entryPoint
 	 */
 	public void init() {
 		placeWidgets();
 	}
 
 	private void placeWidgets() {
 
 		// Login panel
 		HorizontalPanel loginPanel = new HorizontalPanel();
 		RootPanel.get(LOGIN_ROOT_PANEL).add(loginPanel, 10, 60);
 		signInLink = new Anchor();
 		loginPanel.add(signInLink);
 		loggedInAs = new Label();
 		loginPanel.add(loggedInAs);
 
 		HorizontalPanel statusPanel = new HorizontalPanel();
 		RootPanel.get(STATUS_ROOT_PANEL).add(statusPanel, 10, 100);
 		status = new Label();
 		statusPanel.add(status);
 		
 		//statusPanel.add(new Label(version)); // TODO -- ???
 
 		stackPanel = new StackPanel();
 		RootPanel.get(STACK_ROOT_PANEL).add(stackPanel, 10, 130);
 
 		// main panel
 		mainPanel = new VerticalPanel();
 		stackPanel.add(mainPanel, TXT_STARTLIST, false);
 
 		HorizontalPanel selectionPanel = new HorizontalPanel();
 		selectionPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 		selectionPanel.addStyleName("selectionPanel");
 		mainPanel.add(selectionPanel);
 
 		Label yearLabel = new Label(TXT_YEAR);
 		selectionPanel.add(yearLabel);
 
 		yearListBox = new ListBox();
 		selectionPanel.add(yearListBox);
 		yearListBox.setVisibleItemCount(1);
 
 		Label ortLabel = new Label(TXT_START_PLACE);
 		selectionPanel.add(ortLabel);
 
 		placeListBox = new ListBox();
 		selectionPanel.add(placeListBox);
 		placeListBox.setVisibleItemCount(1);
 
 		HorizontalPanel datePanel = new HorizontalPanel();
 		datePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 		datePanel.addStyleName("datePanel");
 		selectionPanel.add(datePanel);
 
 		Label lblDatum = new Label(TXT_FLIGHT_DATUM);
 		datePanel.add(lblDatum);
 
 		prevDayPushButton = new Button(TXT_PREV);
 		prevDayPushButton.setText("<");
 		datePanel.add(prevDayPushButton);
 
 		dateListBox = new ListBox();
 		datePanel.add(dateListBox);
 		dateListBox.setVisibleItemCount(1);
 
 		nextDayPushButton = new Button(TXT_NEXT);
 		datePanel.add(nextDayPushButton);
 
 		Label dummyLabel = new Label();
 		dummyLabel.setWidth("20px");
 		selectionPanel.add(dummyLabel);
 
 		excelLinkHTML = new HTML();
 		selectionPanel.add(excelLinkHTML);
 
 		// flight entry table starts here
 		String[] columns = new String[] { TXT_START_TIME, TXT_LANDING_TIME_TOWPLANE, TXT_DURATION_TOWPLANE, TXT_SHORT_REGISTRATION_TOWPLANE,
 				TXT_LANDING_TIME_GLIDER, TXT_DURATION_GLIDER, TXT_SHORT_REGISTRATION_GLIDER, TXT_PILOT, TXT_PASSENGER_OR_INSTRUCTOR, TXT_TRAINING, TXT_REMARKS };
 
 		String[] styles = new String[] { "starttime", "endtimetowplane", "dauertowplane", "registrationtowplane", "endtimeglider", "dauerglider",
 				"registrationglider", "pilot", "passengerorinstructor", "schulung", "bemerkungen" };
 
 		verticaPanel_2 = new VerticalPanel();
 		mainPanel.add(verticaPanel_2);
 		dynaTableWidget = new DynaTableWidget(provider, columns, styles, 20);
 		dynaTableWidget.setWidth("1200px");
 		verticaPanel_2.add(dynaTableWidget);
 		dynaTableWidget.setStatusText("");
 
 		operationNewModDel = new HorizontalPanel();
 		operationNewModDel.setVisible(false);
 		verticaPanel_2.add(operationNewModDel);
 
 		newButton = new Button(TXT_NEW);
 		newButton.setEnabled(true);
 		operationNewModDel.add(newButton);
 
 		modifyButton = new Button(TXT_MODIFY);
 		modifyButton.setEnabled(false);
 		operationNewModDel.add(modifyButton);
 
 		deleteButton = new Button(TXT_DELETE);
 		deleteButton.setEnabled(false);
 		operationNewModDel.add(deleteButton);
 
 		// dialog for adding or modifying flight entrys
 		flightEntryDialogBox = new DialogBox();
 		VerticalPanel flightEntryVerticaPanel = new VerticalPanel();
 		flightEntryDialogBox.add(flightEntryVerticaPanel);
 		flightEntryDialogBox.hide();
 		flightEntryDialogBox.setModal(true);
 
 		flightEntryFlexTable = new FlexTable();
 		flightEntryVerticaPanel.add(flightEntryFlexTable);
 
 		// row 0: basic information, date and place
 		Label lblDatum2 = new Label(TXT_FLIGHT_DATUM);
 		flightEntryFlexTable.setWidget(0, 0, lblDatum2);
 
 		dateBox = new DateBox2();
 		dateBox.setFormat(DD_MMM_YYYY_FORMAT);
 		flightEntryFlexTable.setWidget(0, 1, dateBox);
 
 		Label lblAllPlaces = new Label(TXT_START_PLACE);
 		flightEntryFlexTable.setWidget(0, 2, lblAllPlaces);
 
 		allPlacesListBox = new ListBox();
 		flightEntryFlexTable.setWidget(0, 3, allPlacesListBox);
 
 		Label lblAllSuggestPlaces = new Label(TXT_LANDING_PLACE);
 		flightEntryFlexTable.setWidget(0, 4, lblAllSuggestPlaces);
 
 		landingPlacesSuggestBox = new SuggestBox2(landingPlacesSuggest);
 		flightEntryFlexTable.setWidget(0, 5, landingPlacesSuggestBox);
 
 		// row 1: time information
 		Label lblStart = new Label(TXT_START_TIME);
 		flightEntryFlexTable.setWidget(1, 0, lblStart);
 
 		startDateBox = new DateBox2();
 		startDateBox.setFormat(MM_HH_FORMAT);
 		startDateBox.getDatePicker().setVisible(false);
 		flightEntryFlexTable.setWidget(1, 1, startDateBox);
 
 		Label lblEndeTowplane = new Label(TXT_LANDING_TIME_TOWPLANE);
 		flightEntryFlexTable.setWidget(1, 2, lblEndeTowplane);
 
 		endTowplaneDateBox = new DateBox2();
 		endTowplaneDateBox.setFormat(MM_HH_FORMAT);
 		endTowplaneDateBox.getDatePicker().setVisible(false);
 		flightEntryFlexTable.setWidget(1, 3, endTowplaneDateBox);
 
 		Label lblEndeGlider = new Label(TXT_LANDING_TIME_GLIDER);
 		flightEntryFlexTable.setWidget(1, 4, lblEndeGlider);
 
 		endGliderDateBox = new DateBox2();
 		endGliderDateBox.setFormat(MM_HH_FORMAT);
 		endGliderDateBox.getDatePicker().setVisible(false);
 		flightEntryFlexTable.setWidget(1, 5, endGliderDateBox);
 
 		// row 2: registration of involved planes and towplane pilot
 		Label lblTowplane = new Label(TXT_REGISTRATION_TOWPLANE);
 		flightEntryFlexTable.setWidget(2, 0, lblTowplane);
 
 		registrationTowplaneBox = new TextBox();
 		flightEntryFlexTable.setWidget(2, 1, registrationTowplaneBox);
 
 		Label lblGlider = new Label(TXT_REGISTRATION_GLIDER);
 		flightEntryFlexTable.setWidget(2, 2, lblGlider);
 
 		registrationGliderBox = new TextBox();
 		flightEntryFlexTable.setWidget(2, 3, registrationGliderBox);
 		
 		Label lblTowplanePilot = new Label(TXT_TOWPLANE_PILOT);
 		flightEntryFlexTable.setWidget(2, 4, lblTowplanePilot);
 		
 		towplanePilotNameBox = new SuggestBox2(pilotNamesSuggest);
 		flightEntryFlexTable.setWidget(2, 5, towplanePilotNameBox);
 
 		// row 3: pilot information
 		Label lblPilot = new Label(TXT_PILOT);
 		flightEntryFlexTable.setWidget(3, 0, lblPilot);
 
 		pilotNameBox = new SuggestBox2(pilotNamesSuggest);
 		flightEntryFlexTable.setWidget(3, 1, pilotNameBox);
 
 		Label lblPassengerOrInstructor = new Label(TXT_PASSENGER_OR_INSTRUCTOR);
 		flightEntryFlexTable.setWidget(3, 2, lblPassengerOrInstructor);
 
 		passengerOrInstructorNameBox = new SuggestBox2(pilotNamesSuggest);
 		flightEntryFlexTable.setWidget(3, 3, passengerOrInstructorNameBox);
 
 		Label lblTraining = new Label(TXT_TRAINING);
 		flightEntryFlexTable.setWidget(3, 4, lblTraining);
 
 		trainingCheckBox = new CheckBox();
 		flightEntryFlexTable.setWidget(3, 5, trainingCheckBox);
 		// row 4: remarks
 		Label lblBemerkungen = new Label(TXT_REMARKS);
 		flightEntryFlexTable.setWidget(4, 0, lblBemerkungen);
 
 		remarksTextBox = new TextBox();
 		remarksTextBox.setVisibleLength(80);
 		flightEntryFlexTable.setWidget(4, 1, remarksTextBox);
 		flightEntryFlexTable.getFlexCellFormatter().setColSpan(4, 1, 5);// TODO - does not work?
 
 		HorizontalPanel operationsPanel;
 		operationsPanel = new HorizontalPanel();
 		operationsPanel.setSpacing(5);
 		flightEntryVerticaPanel.add(operationsPanel);
 
 		saveButton = new Button(TXT_SAVE);
 		saveButton.setEnabled(false);
 		operationsPanel.add(saveButton);
 
 		discardButton = new Button(TXT_DISCARD);
 		discardButton.setEnabled(false);
 		operationsPanel.add(discardButton);
 
 		btnClose = new Button(TXT_CLOSE);
 		btnClose.setEnabled(true);
 		operationsPanel.add(btnClose);
 
 		// Set tab order
 		setTabOrder(dateBox, allPlacesListBox, landingPlacesSuggestBox, startDateBox, endTowplaneDateBox, endGliderDateBox, registrationTowplaneBox,
 				    registrationGliderBox, towplanePilotNameBox, pilotNameBox, passengerOrInstructorNameBox, trainingCheckBox, remarksTextBox, 
 				    saveButton, discardButton, btnClose);
 		
 		loadYears();
 
 		loadAllPlaces();
 		
 		loadAllPilots();	
 
 		enablePilotFields(false);
 
 		initLogin();
 	}
 
 	public void loadYears() {
 		flightEntryService.listYears();
 	}
 
 	private void initLogin() {
 		loginServiceDelegate.login(GWT.getHostPageBaseURL());
 	}
 
 	private void loadPlaces(int year) {
 		flightEntryService.listPlaces(year);
 	}
 
 	private void loadAllPlaces() {
 		airfieldServiceDelegate.listAirfields();
 	}
 	
 	private void loadAllPilots() {
 		pilotServiceDelegate.listPilots();
 	}
 
 	private void reload() {
 		reload(null);
 	}
 
 	private void reload(FlightEntry flightEntry) {
 		if (flightEntry != null) {
 			provider.setCurrentPlace(flightEntry.getPlace());
 			provider.setCurrentDate(flightEntry.getStartTimeInMillis());
 		} else {
 			provider.setCurrentPlace(placeListBox.getItemCount() > 0 ? placeListBox.getItemText(placeListBox.getSelectedIndex()) : "");
 			provider.setCurrentDate(strToDate.get(dateListBox.getItemText(dateListBox.getSelectedIndex())));
 		}
 		lastflightEntry = flightEntry;
 		dynaTableWidget.reload();
 
 		if (rowSelectionHandler == null) {
 			rowSelectionHandler = new RowSelectionHandler() {
 				@Override
 				public void rowSelected(int row, boolean selected) {
 					enablePilotFields(false);
 					if (selected) {
 						FlightEntry flightEntry = provider.getFlightEntry(row);
 						if (flightEntry == null) {
 							clearForm();
 							newButton.setEnabled(true);
 						} else {
 							loadForm(flightEntry);
 							newButton.setEnabled(false);
 							enableCUDButtons();
 						}
 
 					} else {
 						newButton.setEnabled(true);
 						clearForm();
 					}
 				}
 			};
 			dynaTableWidget.addRowSelectionHandler(rowSelectionHandler);
 		}
 
 		if (rowDoubleclickHandler == null) {
 			rowDoubleclickHandler = new RowDoubleclickHandler() {
 				@Override
 				public void rowDoubleclicked(int row) {
 					enablePilotFields(false);
 					FlightEntry flightEntry = provider.getFlightEntry(row);
 					if (flightEntry == null) {
 						clearForm();
 					} else {
 						// open modify dialog
 						loadForm(flightEntry);
 						newButton.setEnabled(false);
 						modifyFlightEntry();
 					}
 
 				}
 			};
 			dynaTableWidget.addRowDoubleclickHandler(rowDoubleclickHandler);
 		}
 	}
 
 	private void enablePilotFields(boolean enable) {
 		dateBox.setEnabled(enable);
 		pilotNameBox.setEnabled(enable);
 		passengerOrInstructorNameBox.setEnabled(enable);
 		towplanePilotNameBox.setEnabled(enable);
 		startDateBox.setEnabled(enable);
 		endGliderDateBox.setEnabled(enable);
 		endTowplaneDateBox.setEnabled(enable);
 		trainingCheckBox.setEnabled(enable);
 		remarksTextBox.setEnabled(enable);
 		allPlacesListBox.setEnabled(enable);
 		landingPlacesSuggestBox.setEnabled(enable);
 		registrationGliderBox.setEnabled(enable);
 		registrationTowplaneBox.setEnabled(enable);
 	}
 
 	private void clearForm() {
 		disableCUDButtons();
 		currentFlightEntry = null;
 		dateBox.setValue(null);
 		pilotNameBox.setValue(null);
 		passengerOrInstructorNameBox.setValue(null);
 		towplanePilotNameBox.setValue(null);
 		startDateBox.setValue(null);
 		endGliderDateBox.setValue(null);
 		endTowplaneDateBox.setValue(null);
 		trainingCheckBox.setValue(false);
 		remarksTextBox.setValue(null);
 		allPlacesListBox.clear();
 		landingPlacesSuggestBox.setValue(null);
 		registrationGliderBox.setValue(null);
 		registrationTowplaneBox.setValue(null);
 	}
 
 	private void disableCUDButtons() {
 		modifyButton.setEnabled(false);
 		deleteButton.setEnabled(false);
 	}
 
 	private void enableCUDButtons() {
 		modifyButton.setEnabled(true);
 		deleteButton.setEnabled(true);
 		disableSCButtons();
 	}
 
 	private void enableSCButtons(boolean enable) {
 		saveButton.setEnabled(enable);
 		discardButton.setEnabled(enable);
 		btnClose.setEnabled(!enable);
 	}
 
 	private void disableSCButtons() {
 		saveButton.setEnabled(false);
 		discardButton.setEnabled(false);
 	}
 
 	private void loadForm(FlightEntry flightEntry) {
 		boolean newEntry = flightEntry.getId() == null;
 		currentFlightEntry = flightEntry;
 
 		// Date date = newEntry ? new Date() : new
 		// Date(strToDate.get(dateListBox.getItemText(dateListBox.getSelectedIndex())));
 
 		// Get date from current selected
 		Date date = new Date(strToDate.get(dateListBox.getItemText(dateListBox.getSelectedIndex())));
		dateBox.setValue(date);
 		if (flightEntry.isStartTimeValid()) {
 			date.setTime(flightEntry.getStartTimeInMillis());
 			startDateBox.setValue(date);
 		} else {
 			Date dateNow = new Date();
 			startDateBox.setValue(dateNow);
 		}
 		if (flightEntry.isEndTimeGliderValid()) {
 			date.setTime(flightEntry.getEndTimeGliderInMillis());
 			endGliderDateBox.setValue(date);
 		} else {
 			endGliderDateBox.setValue(null);
 		}
 		if (flightEntry.isEndTimeTowplaneValid()) {
 			date.setTime(flightEntry.getEndTimeTowplaneInMillis());
 			endTowplaneDateBox.setValue(date);
 		} else {
 			endTowplaneDateBox.setValue(null);
 		}
 		pilotNameBox.setValue(flightEntry.getPilot());
 		passengerOrInstructorNameBox.setValue(flightEntry.getPassengerOrInstructor());
 		towplanePilotNameBox.setValue(flightEntry.getTowplanePilot());
 		trainingCheckBox.setValue(flightEntry.isTraining());
 		remarksTextBox.setValue(flightEntry.getRemarks());
 		if (newEntry) {
 			String pl = placeListBox.getItemCount() > 0 ? placeListBox.getValue(placeListBox.getSelectedIndex()) : "";
 			flightEntry.setPlace(pl);
 			pilotNameBox.setValue(""); // don't set pilot name from login info at the moment
 			// pilotNameBox.setValue(currentLoginInfo.getLastName() + " " + currentLoginInfo.getFirstName());
 		}
 		GwtUtil.setItems(allPlacesListBox, GwtUtil.toAirfieldNames(allAirfields));
 		// TODO - it may be slow if lots of airfields -> optimize!
 		for (int i = 0; i < allPlacesListBox.getItemCount(); i++) {
 			if (flightEntry.getPlace().equals(allPlacesListBox.getValue(i))) {
 				allPlacesListBox.setSelectedIndex(i);
 				break;
 			}
 		}
 		landingPlacesSuggestBox.setValue(flightEntry.getLandingPlace());
 		registrationGliderBox.setValue(flightEntry.getRegistrationGlider());
 		registrationTowplaneBox.setValue(flightEntry.getRegistrationTowplane());
 	}
 
 	private void saveForm(FlightEntry flightEntry) {
 		// set name of pilot
 		String pilot = pilotNameBox.getValue();
 		if (pilot.length() == 0) {
 			pilot = "<Unknown>";
 		}
 		flightEntry.setPilot(pilot);
 
 		// set start time
 		Date date = null;
 		// TODO - find a better way to check
 		try {
 			date = startDateBox.getValue();
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 		flightEntry.setStartTimeValid(date != null);
 		if (flightEntry.isStartTimeValid()) {
 			toYMD(dateBox.getValue(), date);
 			flightEntry.setStartTimeInMillis(date.getTime());
 		} else {
 			// TODO - very crude workaround for time zone problem
 			date = dateBox.getValue();
 			long offset = timeZone.getStandardOffset();
 			date.setTime(date.getTime() - (offset * 60000));
 			flightEntry.setStartTimeInMillis(date.getTime());
 		}
 
 		// set landing time glider
 		date = null;
 		// TODO - find a better way to check
 		try {
 			date = endGliderDateBox.getValue();
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 		flightEntry.setEndTimeGliderValid(date != null);
 		if (flightEntry.isEndTimeGliderValid()) {
 			toYMD(dateBox.getValue(), date);
 			flightEntry.setEndTimeGliderInMillis(date.getTime());
 		} else {
 			flightEntry.setEndTimeGliderInMillis(0);
 		}
 
 		// set landing time towplane
 		date = null;
 		// TODO - find a better way to check
 		try {
 			date = endTowplaneDateBox.getValue();
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 		flightEntry.setEndTimeTowplaneValid(date != null);
 		if (flightEntry.isEndTimeTowplaneValid()) {
 			toYMD(dateBox.getValue(), date);
 			flightEntry.setEndTimeTowplaneInMillis(date.getTime());
 		} else {
 			flightEntry.setEndTimeTowplaneInMillis(0);
 		}
 
 		// set rest of values
 		flightEntry.setTraining(trainingCheckBox.getValue());
 		flightEntry.setRemarks(remarksTextBox.getValue());
 		flightEntry.setPlace(allPlacesListBox.getValue(allPlacesListBox.getSelectedIndex()));
 		flightEntry.setLandingPlace(landingPlacesSuggestBox.getValue());
 		flightEntry.setRegistrationGlider(registrationGliderBox.getValue());
 		flightEntry.setRegistrationTowplane(registrationTowplaneBox.getValue());
 		flightEntry.setPassengerOrInstructor(passengerOrInstructorNameBox.getValue());
 		flightEntry.setTowplanePilot(towplanePilotNameBox.getValue());
 	}
 
 	private void modifyFlightEntry() {
 		if (currentFlightEntry.isModifiable()) {
 			enablePilotFields(true);
 			flightEntryDialogBox.setTitle(TXT_TITLE_MODIFY_FLIGHT);
 			flightEntryDialogBox.setHTML(TXT_MODIFY_FLIGHT);
 			flightEntryDialogBox.setPopupPosition(modifyButton.getAbsoluteLeft() + modifyButton.getOffsetWidth(),
 					modifyButton.getAbsoluteTop() - flightEntryDialogBox.getOffsetHeight() - 260);
 			
 			flightEntryDialogBox.setWidth("800px");
 			flightEntryDialogBox.show();
 
 			// set focus depending on content of current flight entry, optimized for live entry through "Flugdienstleiter"
 			if (!currentFlightEntry.isStartTimeValid()) {
 				startDateBox.setFocus(true); // no start time yet, FDL will want to enter it first
 			} else if (!currentFlightEntry.isEndTimeTowplaneValid()) {
 				endTowplaneDateBox.setFocus(true); // usecase: towplane landed, FDL wants to enter landing time of towplane
 			} else if (!currentFlightEntry.isEndTimeGliderValid()) {
 				endGliderDateBox.setFocus(true); // usecase: glider landed, FDL wants to enter landing time of glider
 			} else if (currentFlightEntry.getRegistrationTowplane().length() == 0) {
 				registrationTowplaneBox.setFocus(true);
 			} else if (currentFlightEntry.getRegistrationGlider().length() == 0) {
 				registrationGliderBox.setFocus(true);
 			} else if (currentFlightEntry.getPilot().length() == 0) {
 				pilotNameBox.setFocus(true);
 			} else if (currentFlightEntry.getPassengerOrInstructor().length() == 0) {
 				passengerOrInstructorNameBox.setFocus(true);
 			} else if (currentFlightEntry.getTowplanePilot().length() == 0) {
 				towplanePilotNameBox.setFocus(true);
 			} else if (currentFlightEntry.getRemarks().length() == 0) {
 				remarksTextBox.setFocus(true);
 			} else if (currentFlightEntry.getLandingPlace().length() == 0) {
 				landingPlacesSuggestBox.setFocus(true);
 			} else {
 				remarksTextBox.setFocus(true);
 			}
 
 			disableCUDButtons();
 			saveButton.setEnabled(false);
 			discardButton.setEnabled(false);
 			btnClose.setEnabled(true);
 		} else {// Not the owner and not Admin
 			showMidifiableDialog(currentFlightEntry, TXT_ERROR_FLIGHTENTRY_MODIFY_OWNER_MISMATCH);
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	private void toYMD(Date srcDate, Date dstDate) {
 		dstDate.setDate(srcDate.getDate());
 		dstDate.setMonth(srcDate.getMonth());
 		dstDate.setYear(srcDate.getYear());
 	}
 
 	public void gui_eventNewButtonClicked() {
 		disableCUDButtons();
 		enablePilotFields(true);
 		FlightEntry flightEntry = new FlightEntry();
 		loadForm(flightEntry);
 		flightEntryDialogBox.setTitle(TXT_TITLE_CREATE_NEW_FLIGHT);
 		flightEntryDialogBox.setText(TXT_CREATE_NEW_FLIGHT);
 		flightEntryDialogBox.setPopupPosition(newButton.getAbsoluteLeft() + newButton.getOffsetWidth(),
 				newButton.getAbsoluteTop() - flightEntryDialogBox.getOffsetHeight() - 260);
 		flightEntryDialogBox.setWidth("800px");
 		flightEntryDialogBox.show();
 		startDateBox.setFocus(true);
 
 		newButton.setEnabled(false);
 		saveButton.setEnabled(true);
 		discardButton.setEnabled(true);
 		btnClose.setEnabled(false);
 	}
 
 	public void gui_eventModifyButtonClicked() {
 		modifyFlightEntry();
 	}
 
 	public void gui_eventSaveButtonClicked() {
 		if (currentFlightEntry == null) {
 			return;
 		}
 		if (validator == null) {
 			validator = new FlightEntryValidator(this);
 		}
 		if (validator.isValid()) {
 			disableSCButtons();
 			saveForm(currentFlightEntry);
 			flightEntryService.createOrUpdateFlightEntry(currentFlightEntry);
 			clearForm();
 			enablePilotFields(false);
 			newButton.setEnabled(true);
 			flightEntryDialogBox.hide();
 		}
 	}
 
 	public void gui_eventDiscardClicked() {
 		disableSCButtons();
 		clearForm();
 		enablePilotFields(false);
 		newButton.setEnabled(true);
 		dynaTableWidget.resetSelection();
 		flightEntryDialogBox.hide();
 	}
 
 	public void gui_eventCloseClicked() {
 		gui_eventDiscardClicked();
 	}
 
 	private boolean hasDate(FlightEntry flightEntry) {
 		return strToDate.containsKey(DATE_FORMAT.format(new Date(flightEntry.getStartTimeInMillis())));
 	}
 
 	private boolean hasPlace(FlightEntry flightEntry) {
 		Set<String> items = getItemList(placeListBox);
 		return items.contains(flightEntry.getPlace());
 	}
 
 	@SuppressWarnings("deprecation")
 	private boolean hasYear(FlightEntry flightEntry) {
 		Set<String> items = getItemList(yearListBox);
 		Date date = new Date(flightEntry.getStartTimeInMillis());
 		return items.contains(String.valueOf(date.getYear() + 1900));
 	}
 
 	private Set<String> getItemList(ListBox listBox) {
 		Set<String> items = new TreeSet<String>();
 		for (int i = 0; i < listBox.getItemCount(); i++) {
 			items.add(listBox.getValue(i));
 		}
 		return items;
 	}
 
 	private void setSelected(ListBox listBox, String item) {
 		for (int i = 0; i < listBox.getItemCount(); i++) {
 			if (item.equals(listBox.getValue(i))) {
 				listBox.setSelectedIndex(i);
 				break;
 			}
 		}
 	}
 
 	public void gui_eventDeleteButtonClicked() {
 		if (currentFlightEntry == null) {
 			return;
 		}
 		if (currentFlightEntry.isDeletable()) {
 			final DialogBox deleteDialogBox = new DialogBox();
 			deleteDialogBox.setModal(true);
 			deleteDialogBox.setPopupPosition(deleteButton.getAbsoluteLeft() + deleteButton.getOffsetWidth(),
 					deleteButton.getAbsoluteTop() - deleteDialogBox.getOffsetHeight());
 			deleteDialogBox.setHTML(TXT_REALLY_DELETE_QUESWTION);
 			Button yesButton = new Button(TXT_YES);
 			yesButton.addClickHandler(new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent event) {
 					disableCUDButtons();
 					flightEntryService.removeFlightEntry(currentFlightEntry);
 					reload();
 					deleteDialogBox.hide();
 				}
 			});
 			Button noButton = new Button(TXT_NO);
 			noButton.addClickHandler(new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent event) {
 					deleteDialogBox.hide();
 				}
 			});
 			HorizontalPanel hp = new HorizontalPanel();
 			hp.add(yesButton);
 			hp.add(noButton);
 			deleteDialogBox.setWidget(hp);
 			deleteDialogBox.show();
 		} else { // Not the owner and not Admin
 			showMidifiableDialog(currentFlightEntry, TXT_ERROR_FLIGHTENTRY_DELETE_OWNER_MISMATCH);
 		}
 	}
 
 	private void showMidifiableDialog(FlightEntry flightEntry, String msg) {
 		final DialogBox notmodDialogBox = new DialogBox();
 		notmodDialogBox.setModal(true);
 		notmodDialogBox.setPopupPosition(deleteButton.getAbsoluteLeft() + deleteButton.getOffsetWidth(),
 				deleteButton.getAbsoluteTop() - notmodDialogBox.getOffsetHeight());
 		notmodDialogBox.setHTML(msg + " " + flightEntry.getCreator());
 		Button okButton = new Button(TXT_OK);
 		okButton.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				disableCUDButtons();
 				notmodDialogBox.hide();
 			}
 		});
 		HorizontalPanel hp = new HorizontalPanel();
 		hp.add(okButton);
 		notmodDialogBox.setWidget(hp);
 		notmodDialogBox.show();
 	}
 
 	private void setTabOrder(Widget... widgets) {
 		int idx = 0;
 		for (Widget widget : widgets) {
 			Widget wdg = widget;
 			if (wdg instanceof SuggestBox2) {
 				wdg = ((SuggestBox2) wdg).getFocusWidget();
 			} else {
 				if (wdg instanceof DateBox2) {
 					wdg = ((DateBox2) wdg).getFocusWidget();
 				}
 			}
 			if (wdg instanceof FocusWidget) {
 				((FocusWidget) wdg).setTabIndex(idx++);
 			}
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	public void service_eventUpdateSuccessful(FlightEntry flightEntry) {
 		status.setText(TXT_SUCCESS_ADD_FLIGHTENTRY);
 		if (placeListBox.getItemCount() == 0) {
 			placeListBox.addItem(flightEntry.getPlace());
 		}
 		if (!hasYear(flightEntry)) {
 			loadYears();
 		} else {
 			Date date = new Date(flightEntry.getStartTimeInMillis());
 			setSelected(yearListBox, String.valueOf(date.getYear() + 1900));
 			if (!hasPlace(flightEntry)) {
 				date = new Date(flightEntry.getStartTimeInMillis());
 				loadPlaces(date.getYear() + 1900);
 			} else {
 				setSelected(placeListBox, flightEntry.getPlace());
 				if (!hasDate(flightEntry)) {
 					loadDates();
 				} else {
 					String dateStr = DATE_FORMAT.format(new Date(flightEntry.getStartTimeInMillis()));
 					setSelected(dateListBox, dateStr);
 					reload(flightEntry);
 				}
 			}
 		}
 	}
 
 	public void service_eventRemoveFlightEntrySuccessful(FlightEntry flightEntry) {
 		status.setText(TXT_SUCCESS_REMOVE_FLIGHTENTRY);
 		reload();
 
 	}
 
 	public void service_eventUpdateFlightEntryFailed(Throwable caught) {
 		status.setText(TXT_ERROR_FLIGHTENTRY_UPDATE);
 	}
 
 	public void service_eventAddFlightEntryFailed(Throwable caught) {
 		status.setText(TXT_ERROR_FLIGHTENTRY_ADD);
 	}
 
 	public void service_eventRemoveFlightEntryFailed(Throwable caught) {
 		status.setText(TXT_ERROR_FLIGHTENTRY_DELETE);
 	}
 
 	public void service_eventListFlightEntrysFailed(Throwable caught) {
 		status.setText(TXT_ERROR_LIST_LOAD);
 
 	}
 
 	public void gui_eventPlaceListBoxChanged() {
 		clearForm();
 		loadDates();
 	}
 
 	public void service_eventListPlacesSuccessful(Set<String> places) {
 		String oldPlace = "";
 		if (lastflightEntry != null) {
 			oldPlace = lastflightEntry.getPlace();
 		} else {
 			int oldIdx = placeListBox.getSelectedIndex();
 			if (oldIdx >= 0) {
 				oldPlace = placeListBox.getItemText(placeListBox.getSelectedIndex());
 			}
 		}
 		int newOldPlaceIdx = -1;
 		int homePlaceIdx = -1;
 		placeListBox.clear();
 		for (String place : places) {
 			placeListBox.addItem(place);
 			if (newOldPlaceIdx == -1 && place.equals(oldPlace)) {
 				newOldPlaceIdx = placeListBox.getItemCount() - 1;
 			}
 			if (currentLoginInfo != null && homePlaceIdx == -1 && currentLoginInfo.getHomeAirfield() != null
 					&& currentLoginInfo.getHomeAirfield().compareToIgnoreCase(place) == 0) {
 				homePlaceIdx = placeListBox.getItemCount() - 1;
 			} else if (prefPlace != null && (prefPlace.compareToIgnoreCase(place) == 0)) {
 				homePlaceIdx = placeListBox.getItemCount() - 1;
 			}
 		}
 		if (newOldPlaceIdx != -1) {
 			placeListBox.setSelectedIndex(newOldPlaceIdx);
 		} else if (homePlaceIdx != -1) {
 			placeListBox.setSelectedIndex(homePlaceIdx);
 		} else {
 			placeListBox.setSelectedIndex(0);
 		}
 		loadDates();
 	}
 
 	private void loadDates() {
 		String place = placeListBox.getItemCount() > 0 ? placeListBox.getItemText(placeListBox.getSelectedIndex()) : "";
 		int year = Integer.parseInt(yearListBox.getItemText(yearListBox.getSelectedIndex()));
 		flightEntryService.listDates(place, year);
 	}
 
 	public void service_eventListPlacesFailed(Throwable caught) {
 		// TODO Auto-generated method stub
 	}
 
 	/*
 	 * Year handling
 	 */
 
 	public void gui_eventYearListBoxChanged() {
 		clearForm();
 		int year = Integer.parseInt(yearListBox.getItemText(yearListBox.getSelectedIndex()));
 		flightEntryService.listPlaces(year);
 	}
 
 	public void service_eventListYearsFailed(Throwable caught) {
 		// TODO Auto-generated method stub
 	}
 
 	public void service_eventListYearsSuccessful(Set<Integer> years) {
 		yearListBox.clear();
 		for (Integer year : years) {
 			yearListBox.addItem(Integer.toString(year));
 		}
 		yearListBox.setSelectedIndex(yearListBox.getItemCount() - 1);
 		int year = Integer.parseInt(yearListBox.getItemText(yearListBox.getSelectedIndex()));
 		flightEntryService.listPlaces(year);
 	}
 
 	/*
 	 * Day handling
 	 */
 	private void adjustPrevNextDayButtons() {
 		nextDayPushButton.setEnabled(dateListBox.getSelectedIndex() < dateListBox.getItemCount() - 1);
 		prevDayPushButton.setEnabled(dateListBox.getSelectedIndex() > 0);
 		clearForm();
 	}
 
 	@SuppressWarnings("deprecation")
 	public void gui_eventDateListBoxChanged() {
 		adjustPrevNextDayButtons();
 
 		// adjust link to excel file
 		Date date = new Date(strToDate.get(dateListBox.getItemText(dateListBox.getSelectedIndex())));
 		String link = GWT.getModuleBaseURL() + "excelfile" + "/" + yearListBox.getValue(yearListBox.getSelectedIndex()) + "/" + date.getMonth() + "/"
 				+ date.getDate() + "/" + placeListBox.getValue(placeListBox.getSelectedIndex());
 		excelLinkHTML.setHTML("<a href=\"" + link + "\">Excel</a>");
 
 		// reload table values
 		reload();
 	}
 
 	public void gui_eventNextDayPushButtonClicked() {
 		if (dateListBox.getSelectedIndex() < dateListBox.getItemCount() - 1) {
 			dateListBox.setItemSelected(dateListBox.getSelectedIndex() + 1, true);
 		}
 		gui_eventDateListBoxChanged();
 	}
 
 	public void gui_eventPrevDayPushButtonClicked() {
 		if (dateListBox.getSelectedIndex() > 0) {
 			dateListBox.setItemSelected(dateListBox.getSelectedIndex() - 1, true);
 		}
 		gui_eventDateListBoxChanged();
 	}
 
 	public void service_eventListDatesFailed(Throwable caught) {
 		// TODO Auto-generated method stub
 	}
 
 	@SuppressWarnings("deprecation")
 	public void service_eventListDatesSuccessful(Set<Long> dates) {
 		dateListBox.clear();
 		strToDate.clear();
 		for (Long dateInMillies : dates) {
 			String dateStr = DATE_FORMAT.format(new Date(dateInMillies), timeZone);
 			dateListBox.addItem(dateStr);
 			strToDate.put(dateStr, dateInMillies);
 		}
 		if (lastflightEntry != null) {
 			Date feDate = new Date(lastflightEntry.getStartTimeInMillis());
 			for (int i = 0; i < dateListBox.getItemCount(); i++) {
 				Date date = new Date(strToDate.get(dateListBox.getValue(i)));
 				if (date.getYear() == feDate.getYear() && date.getMonth() == feDate.getMonth() && date.getDate() == feDate.getDate()) {
 					dateListBox.setSelectedIndex(i);
 					break;
 				}
 			}
 		} else {
 			dateListBox.setSelectedIndex(dateListBox.getItemCount() - 1);
 		}
 		gui_eventDateListBoxChanged();
 
 	}
 
 	public void service_eventListAllPlacesFailed(Throwable caught) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void service_eventAllListPlacesSuccessful(Set<Airfield> airfields) {
 		allAirfields = airfields;
 		GwtUtil.setItems(allPlacesListBox, GwtUtil.toAirfieldNames(airfields));
 		for (String place : GwtUtil.toAirfieldNames(airfields)) {
 			landingPlacesSuggest.add(place);
 		}
 	}
 	
 	public void service_eventAllListPilotsSuccessful(Set<Pilot> pilots) {
 		for (String pilot : GwtUtil.toPilotNames(pilots)){
 			pilotNamesSuggest.add(pilot);
 		}
 	}
 
 	public void gui_eventModifyPilotForm() {
 		if (currentFlightEntry.getId() != null) {
 			// compare only modified entry
 			// TODO - should we use tmpFlightEntry for save?
 			FlightEntry tmpFlightEntry = currentFlightEntry.copy();
 			saveForm(tmpFlightEntry);
 			enableSCButtons(tmpFlightEntry.compareTo(currentFlightEntry) != 0);
 		}
 	}
 
 	public void service_eventLoginSuccessful(LoginInfo loginInfo) {
 		if (loginInfo.isLoggedIn()) {
 			currentLoginInfo = loginInfo;
 			signInLink.setText(TXT_LOGOUT);
 			signInLink.setHref(loginInfo.getLogoutUrl());
 			loggedInAs.setText(TXT_LOGGED_IN_AS + loginInfo.getEmail() + ")");
 			operationNewModDel.setVisible(true);
 
 			if (loginInfo.isAdmin()) {
 				if (adminGUI == null) {
 					adminGUI = new AdminGUI(this);
 					RootPanel.get().add(adminGUI);
 				}
 				stackPanel.add(adminGUI, TXT_ADMIN, false);
 			}
 		} else {
 			currentLoginInfo = null;
 			signInLink.setText(TXT_LOGIN);
 			signInLink.setHref(loginInfo.getLoginUrl());
 			loggedInAs.setText("");
 			operationNewModDel.setVisible(false);
 
 			if (adminGUI != null) {
 				adminGUI.removeFromParent();
 			}
 		}
 	}
 
 	public void service_eventLoginFailed(Throwable caught) {
 		status.setText(TXT_ERROR_LOGIN + caught.getMessage());
 	}
 
 	public ListBox getYearListBox() {
 		return yearListBox;
 	}
 
 	public Set<Airfield> getAllAirfields() {
 		return allAirfields;
 	}
 
 }
