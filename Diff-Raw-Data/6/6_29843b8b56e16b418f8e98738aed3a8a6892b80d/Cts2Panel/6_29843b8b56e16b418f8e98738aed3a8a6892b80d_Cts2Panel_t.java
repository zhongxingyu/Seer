 package edu.mayo.cts2Viewer.client;
 
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.smartgwt.client.core.DataClass;
 import com.smartgwt.client.data.Record;
 import com.smartgwt.client.types.Alignment;
 import com.smartgwt.client.types.VerticalAlignment;
 import com.smartgwt.client.util.SC;
 import com.smartgwt.client.widgets.IButton;
 import com.smartgwt.client.widgets.Label;
 import com.smartgwt.client.widgets.events.ClickEvent;
 import com.smartgwt.client.widgets.events.ClickHandler;
 import com.smartgwt.client.widgets.form.DynamicForm;
 import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
 import com.smartgwt.client.widgets.form.fields.StaticTextItem;
 import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
 import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
 import com.smartgwt.client.widgets.form.fields.events.KeyUpEvent;
 import com.smartgwt.client.widgets.form.fields.events.KeyUpHandler;
 import com.smartgwt.client.widgets.grid.ListGridField;
 import com.smartgwt.client.widgets.grid.events.CellSavedEvent;
 import com.smartgwt.client.widgets.grid.events.CellSavedHandler;
 import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
 import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
 import com.smartgwt.client.widgets.layout.HLayout;
 import com.smartgwt.client.widgets.layout.SectionStack;
 import com.smartgwt.client.widgets.layout.SectionStackSection;
 import com.smartgwt.client.widgets.layout.VLayout;
 
 import edu.mayo.cts2Viewer.client.authentication.Authentication;
 import edu.mayo.cts2Viewer.client.authentication.LoginInfoPanel;
 import edu.mayo.cts2Viewer.client.datasource.ValueSetsXmlDS;
 import edu.mayo.cts2Viewer.client.events.DefaultServerRetrievedEvent;
 import edu.mayo.cts2Viewer.client.events.FilterUpdatedEvent;
 import edu.mayo.cts2Viewer.client.events.FilterUpdatedEventHandler;
 import edu.mayo.cts2Viewer.client.events.LogOutRequestEvent;
 import edu.mayo.cts2Viewer.client.events.LogOutRequestEventHandler;
 import edu.mayo.cts2Viewer.client.events.LoginCancelledEvent;
 import edu.mayo.cts2Viewer.client.events.LoginCancelledEventHandler;
 import edu.mayo.cts2Viewer.client.events.LoginRequestEvent;
 import edu.mayo.cts2Viewer.client.events.LoginSuccessfulEvent;
 import edu.mayo.cts2Viewer.client.events.LoginSuccessfulEventHandler;
 import edu.mayo.cts2Viewer.client.events.ValueSetsReceivedEvent;
 import edu.mayo.cts2Viewer.client.events.ValueSetsReceivedEventHandler;
 import edu.mayo.cts2Viewer.client.utils.FilterPanel;
 import edu.mayo.cts2Viewer.client.utils.UiHelper;
 import edu.mayo.cts2Viewer.shared.Credentials;
 import edu.mayo.cts2Viewer.shared.ServerProperties;
 
 /*
  * Panel to hold data for the main area
  */
 public class Cts2Panel extends VLayout {
 
 	static Logger lgr = Logger.getLogger(Cts2Panel.class.getName());
 	
 	public static final String SELECT_SERVER_MSG = "<Select a Server>";
 
 	protected ServerProperties i_serverProperties;
 
 	private static final String BACKGROUND_COLOR = "#F5F5F3";
 	private static final int WIDGET_WIDTH = 150;
 	private static final String SERVICE_TITLE = "<b>Service</b>";
 	private static final String ROWS_RETRIEVED_TITLE = "Rows Matching Criteria:";
 	private static final String TITLE = "Value Sets";
 	private static final String TITLE_VS_INFO = "Value Set Properties";
 	private static final String TITLE_SEARCH_RESULTS = "Search Results";
 
 	private ValueSetsListGrid i_valueSetsListGrid;
 	private ValueSetPropertiesPanel i_valueSetPropertiesPanel;
 	private ResolvedValueSetPropertiesPanel i_resolvedValueSetPropertiesPanel;
 	private SearchTextItem i_searchItem;
 	private IButton i_clearButton;
 	private Label i_rowsRetrievedLabel;
 	private ComboBoxItem i_serverCombo;
 	private StaticTextItem i_defaultServerTextItem;
 	private String i_defaultServer;
 	private DownloadPanel i_downloadPanel;
 	private String i_lastValidServer;
 	private LoginInfoPanel i_loginInfoPanel;
 	private FilterPanel i_filterPanel;
 	private Map<String, ServerProperties> serverPropertiesMap;
 	private boolean loggedIn = false;
 
 	public Cts2Panel() {
 		super();
 		init();
 	}
 
 	private void init() {
 		lgr.log(Level.INFO, "init Cts2Panel...");
 		serverPropertiesMap = new HashMap<String, ServerProperties>();
 
 		i_lastValidServer = SELECT_SERVER_MSG;
 
 		Label titleLabel = UiHelper.createMainTitleLabel(TITLE);
 		titleLabel = UiHelper.createLabelWithBorders(titleLabel);
 
 		// main layout to hold all of the value set info
 		setWidth100();
 		setHeight100();
 		setBackgroundColor(BACKGROUND_COLOR);
 
 		// layout for main title
 		HLayout titleLayout = new HLayout();
 		titleLayout.setWidth100();
 		titleLayout.setAlign(Alignment.CENTER);
 		titleLayout.setMargin(10);
 		titleLayout.addMember(titleLabel);
 
 		// layout for any content
 		HLayout contentLayout = new HLayout();
 		contentLayout.setWidth100();
 		contentLayout.setHeight100();
 		contentLayout.setAlign(VerticalAlignment.TOP);
 		contentLayout.setMargin(10);
 		contentLayout.setMembersMargin(15);
 		contentLayout.setBackgroundColor(BACKGROUND_COLOR);
 
 		// left side that contains the server, search and search results
 		VLayout leftLayout = createLeftSideComponentsLayout();
 		contentLayout.addMember(leftLayout);
 
 		// right side that contains the value set and resolved value set
 		i_resolvedValueSetPropertiesPanel = new ResolvedValueSetPropertiesPanel(i_serverCombo);
 		contentLayout.addMember(i_resolvedValueSetPropertiesPanel);
 
 		// TODO CME make eventBus calls to update components.
 		linkWidgets();
 
 		addMember(contentLayout);
 
 		setClearButtonEnablement();
 		createValueSetsReceivedEvent();
 		createLoginSuccessfulEvent();
 		createLoginCancelEvent();
 		createLogOutEvent();
 		createFilterUpdatedReceivedEvent();
 	}
 
 	private VLayout createLeftSideComponentsLayout() {
 
 		VLayout componentsLayout = new VLayout();
 		componentsLayout.setWidth("55%");
 		componentsLayout.setHeight100();
 		componentsLayout.setMembersMargin(10);
 
 		// add rounded borders to the layout.
 		UiHelper.createLayoutWithBorders(componentsLayout);
 
 		componentsLayout.setShowResizeBar(true);
 
 		i_valueSetsListGrid = new ValueSetsListGrid();
 		
 		componentsLayout.addMember(createSearchWidgetLayout());
 
 		i_downloadPanel = new DownloadPanel(i_serverCombo, i_valueSetsListGrid);
 
 		i_rowsRetrievedLabel = new Label("");
 		i_rowsRetrievedLabel.setWidth100();
 		i_rowsRetrievedLabel.setMargin(5);
 		i_rowsRetrievedLabel.setHeight(20);
 
 		// layout to hold the rows retrieved label and the download form.
 		HLayout rowsRetrievedAndDownloadLayout = new HLayout();
 		rowsRetrievedAndDownloadLayout.setWidth100();
 		rowsRetrievedAndDownloadLayout.setHeight(20);
 
 		rowsRetrievedAndDownloadLayout.addMember(i_rowsRetrievedLabel);
 		rowsRetrievedAndDownloadLayout.addMember(i_downloadPanel);
 
 		componentsLayout.addMember(rowsRetrievedAndDownloadLayout);
 
 		// add the value set properties panel to the bottom
 		i_valueSetPropertiesPanel = new ValueSetPropertiesPanel();
 
 		SectionStack sectionStack = createSectionStack(i_valueSetsListGrid, i_valueSetPropertiesPanel);
 		componentsLayout.addMember(sectionStack);
 
 		return componentsLayout;
 	}
 
 	/**
 	 * Create a SectionStack for the search results and collapsable value set
 	 * section
 	 * 
 	 * @param valueSetsGrid
 	 * @param panel
 	 * @return
 	 */
 	private SectionStack createSectionStack(ValueSetsListGrid valueSetsGrid, ValueSetPropertiesPanel panel) {
 
 		SectionStack sectionStack = UiHelper.createSectionStack();
 
 		String searchResultsTitle = UiHelper.getSectionTitle(TITLE_SEARCH_RESULTS);
 
 		SectionStackSection sectionSearchResults = new SectionStackSection(searchResultsTitle);
 		sectionSearchResults.setExpanded(true);
 
 		sectionSearchResults.addItem(valueSetsGrid);
 		sectionStack.addSection(sectionSearchResults);
 
 		String resolvedValueSetsTitle = UiHelper.getSectionTitle(TITLE_VS_INFO);
 
 		SectionStackSection sectionValueSetInfo = new SectionStackSection(resolvedValueSetsTitle);
 		sectionValueSetInfo.setExpanded(false);
 		sectionValueSetInfo.addItem(panel);
 		sectionStack.addSection(sectionValueSetInfo);
 
 		return sectionStack;
 	}
 
 	private HLayout createSearchWidgetLayout() {
 
 		HLayout layout = new HLayout();
 		layout.setWidth100();
 		layout.setHeight(50);
 
 		// add rounded borders to the layout.
 		UiHelper.createLayoutWithBorders(layout);
 
 		// add the drop down to choose the server to connect to.
 		i_serverCombo = createServerCombo();
 
 		// create the single server to connect to.
 		i_defaultServerTextItem = createDefaultServerTextItem();
 
 		HLayout searchLayout = new HLayout();
 		searchLayout.setWidth100();
 		searchLayout.setHeight(55);
 		searchLayout.setMembersMargin(5);
 		searchLayout.setBackgroundColor("#f6faff");
 
 		i_searchItem = new SearchTextItem();
 
 		DynamicForm searchForm = new DynamicForm();
 		searchForm.setMargin(5);
 
 		// Determine if we show a combo with all services or just a single
 		// selection.
 		if (Cts2Viewer.s_showAll) {
 			searchForm.setItems(i_serverCombo, i_searchItem);
 		} else {
 			searchForm.setItems(i_defaultServerTextItem, i_searchItem);
			i_serverCombo.setValue(i_defaultServerTextItem.getValue());
 		}
 
 		searchForm.setHeight(55);
 		searchLayout.addMember(searchForm);
 		
 		int height;
 		int margin;
 		VerticalAlignment vAlignment;
 		
 		// settings different based on if showAll option is present.
 		if (Cts2Viewer.s_showAll) {
 			height = 62;
 			margin = 7;
 			vAlignment = VerticalAlignment.BOTTOM;
 		}
 		else {
 			height = 50;
 			margin = 5;
 			vAlignment = VerticalAlignment.BOTTOM;
 		}
 				
 		VLayout buttonLayout = new VLayout();
 		buttonLayout.setHeight(height);
 		buttonLayout.setMargin(margin);
 		buttonLayout.setAlign(vAlignment);
 		buttonLayout.setWidth(60);
 
 		i_loginInfoPanel = new LoginInfoPanel();
 		buttonLayout.addMember(i_loginInfoPanel);
 
 		// add a button to clear the search form.
 		i_clearButton = new IButton("Clear");
 		i_clearButton.setHeight(20);
 		i_clearButton.setWidth(50);
 		i_clearButton.addClickHandler(new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				i_searchItem.clearValue();
 				i_clearButton.setDisabled(true);
 				getValueSets("", i_filterPanel.getFilters());
 				clearPanels();
 			}
 		});
 		buttonLayout.addMember(i_clearButton);
 
 		searchLayout.addMember(buttonLayout);
 		layout.addMember(searchLayout);
 		i_filterPanel = new FilterPanel();
 		i_filterPanel.setVisible(false);
 		layout.addMember(i_filterPanel);
 
 		return layout;
 	}
 
 	/**
 	 * Create the server ComboBoxItem to select the server to connect to.
 	 * 
 	 * @return
 	 */
 	private ComboBoxItem createServerCombo() {
 		i_serverCombo = new ComboBoxItem();
 		i_serverCombo.setDefaultToFirstOption(true);
 		i_serverCombo.setTitle(SERVICE_TITLE);
 		i_serverCombo.setType("comboBox");
 		i_serverCombo.setWidth(WIDGET_WIDTH);
 		i_serverCombo.setWrapTitle(false);
 		i_serverCombo.setAttribute("browserSpellCheck", false);
 
 		i_serverCombo.addChangedHandler(new ChangedHandler() {
 
 			@Override
 			public void onChanged(ChangedEvent event) {
 				getServerProperties(getSelectedServer(), true);
 			}
 		});
 
 		retrieveServers();
 		return i_serverCombo;
 	}
 
 	/**
 	 * Create a StaticTextItem to display the single service.
 	 * 
 	 * @return
 	 */
 	private StaticTextItem createDefaultServerTextItem() {
 		i_defaultServerTextItem = new StaticTextItem();
 		i_defaultServerTextItem.setTitle(SERVICE_TITLE);
 		i_defaultServerTextItem.setWidth(WIDGET_WIDTH);
 		i_defaultServerTextItem.setWrapTitle(false);
 
 		retrieveDefaultServer();
 		return i_defaultServerTextItem;
 	}
 
 	private void updateServiceSelection() {
 		clearPanels();
 		i_filterPanel.setVisible(i_serverProperties != null && i_serverProperties.isShowFilters());
 		setSearchEnablement();
 		getValueSets(i_searchItem.getValueAsString(), i_filterPanel.getFilters());
 	}
 
 	/**
 	 * Update UI with the logged in user.
 	 * 
 	 * @param credentials
 	 */
 	private void updateWithLoggedInUser(Credentials credentials) {
 		i_loginInfoPanel.setUser(credentials);
 
 		// update the last valid server
 		i_lastValidServer = i_serverCombo.getValueAsString();
 
 		updateServiceSelection();
 	}
 
 	private String getSelectedServer() {
 
 		final String selectedServer;
 
 		if (Cts2Viewer.s_showAll) {
 			selectedServer = i_serverCombo.getValueAsString();
 		} else {
 			selectedServer = i_defaultServer;
 		}
 
 		return selectedServer;
 	}
 	
 	/**
 	 * Get the newly selected server. Check if credentials are needed for this
 	 * server selection.
 	 */
 	protected void getServerProperties(final String selectedServer, final boolean checkRequiresCredentials) {
 
 		if (serverPropertiesMap.containsKey(selectedServer)) {
 			setServerProperties(selectedServer, checkRequiresCredentials);
 		} else {
 			Cts2ServiceAsync service = GWT.create(Cts2Service.class);
 			try {
 				service.getServerProperties(selectedServer, new AsyncCallback<ServerProperties>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						// reset server selection
 						i_serverCombo.setValue(i_lastValidServer);
 						getServerProperties(i_lastValidServer, true);
 						SC.warn("Unable to retrieve the selected server properties.");
 					}
 
 					@Override
 					public void onSuccess(ServerProperties serverProperties) {
 						serverPropertiesMap.put(selectedServer, serverProperties);
 						setServerProperties(selectedServer, checkRequiresCredentials);
 					}
 				});
 			} catch (Exception e) {
 				SC.warn("Unable to get selected service properties.");
 			}
 		}
 	}
 
 	private void setServerProperties(String server, boolean checkRequiresCredentials) {
 		i_serverProperties = serverPropertiesMap.get(server);
 		i_filterPanel.setVisible(i_serverProperties != null && i_serverProperties.isShowFilters());
 		if (checkRequiresCredentials) {
 			determineIfSelectedServerRequiresCredentials(server, i_serverProperties);
 		}
 		i_resolvedValueSetPropertiesPanel.setEntityTransformService(i_serverProperties.getEntityTransformService());
 		setSearchEnablement();
 	}
 
 	/**
 	 * If a selected server requires a login, then display it.
 	 * 
 	 * @param selectedServer
 	 * @param serverProperties
 	 */
 	private void determineIfSelectedServerRequiresCredentials(String selectedServer, ServerProperties serverProperties) {
 
 		if (i_serverProperties.isRequireCredentials()) {
 			// Check if we have cached the credentials in
 			// Authentication before asking user to login again.
 			Credentials credentials = Authentication.getInstance().getCredentials(selectedServer);
 
 			if (credentials != null) {
 				updateWithLoggedInUser(credentials);
 
 			} else {
 				// fire the login event to show the login panel
 				Cts2Viewer.EVENT_BUS.fireEvent(new LoginRequestEvent(selectedServer));
 			}
 
 		} else {
 			// clear out the logged in user for this server
 			i_loginInfoPanel.clearUser();
 
 			// update the last valid server
 			i_lastValidServer = i_serverCombo.getValueAsString();
 			updateServiceSelection();
 		}
 	}
 
 	/**
 	 * Create a handler to listen for a login cancel request.
 	 */
 	private void createLoginCancelEvent() {
 		Cts2Viewer.EVENT_BUS.addHandler(LoginCancelledEvent.TYPE, new LoginCancelledEventHandler() {
 
 			@Override
 			public void onCancelRequest(LoginCancelledEvent loginCancelledEvent) {
 				// reset the server selection
 				i_serverCombo.setValue(i_lastValidServer);
 
 				if (!i_lastValidServer.equals(SELECT_SERVER_MSG)) {
 					getServerProperties(i_lastValidServer, true);
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create a handler to listen for a login successful event.
 	 */
 	private void createLoginSuccessfulEvent() {
 		Cts2Viewer.EVENT_BUS.addHandler(LoginSuccessfulEvent.TYPE, new LoginSuccessfulEventHandler() {
 
 			@Override
 			public void onLoginSuccessful(LoginSuccessfulEvent loginSuccessfulEvent) {
 				loggedIn = true;
 				Credentials credentials = loginSuccessfulEvent.getCredentials();
 				Authentication.getInstance().addAuthenticatedCredential(credentials);
 
 				updateWithLoggedInUser(credentials);
 			}
 		});
 	}
 
 	/**
 	 * Create a handler to listen for a logout event.
 	 */
 	private void createLogOutEvent() {
 		Cts2Viewer.EVENT_BUS.addHandler(LogOutRequestEvent.TYPE, new LogOutRequestEventHandler() {
 
 			@Override
 			public void onLogOutRequest(LogOutRequestEvent logOutRequestEvent) {
 				loggedIn = false;
 				Credentials credentials = logOutRequestEvent.getCredential();
 				logoutFromServer(credentials);
 				i_loginInfoPanel.clearUser();
 
 				// i_loginInfoPanel.clearUser();
 				Authentication.getInstance().removeCredential(credentials.getServer());
 
 				// reset the server selection and the server selection
 				if (Cts2Viewer.s_showAll) {
 					i_lastValidServer = SELECT_SERVER_MSG;
 					i_serverCombo.setValue(i_lastValidServer);
 					getServerProperties(i_lastValidServer, true);
 				}
				
 				clearPanels();
 				clearSearch();
 				updateServiceSelection();
 			}
 		});
 	}
 
 	private void clearPanels() {
 		EntityWindow.getInstance().hide();
 		i_valueSetPropertiesPanel.clearValueSetInfo();
 		i_resolvedValueSetPropertiesPanel.clearPanels();
 	}
 
 	private void clearSearch() {
 		i_filterPanel.clearForm();
 		i_searchItem.setValue("");
 	}
 
 	private void createFilterUpdatedReceivedEvent() {
 		Cts2Viewer.EVENT_BUS.addHandler(FilterUpdatedEvent.TYPE, new FilterUpdatedEventHandler() {
 			@Override
 			public void onFilterUpdate(FilterUpdatedEvent filterUpdatedEvent) {
 				clearPanels();
 				getValueSets(i_searchItem.getValueAsString(), i_filterPanel.getFilters());
 			}
 		});
 	}
 
 	/**
 	 * Listen for the event that ValueSets were retrieved.
 	 */
 	private void createValueSetsReceivedEvent() {
 		Cts2Viewer.EVENT_BUS.addHandler(ValueSetsReceivedEvent.TYPE, new ValueSetsReceivedEventHandler() {
 
 			@Override
 			public void onValueSetsReceived(ValueSetsReceivedEvent event) {
 
 				DataClass[] dc = ValueSetsXmlDS.getInstance().getTestData();
 
 				if (dc.length >= 1) {
 
 					String numEntries = dc[0].getAttribute("numEntries");
 					String complete = dc[0].getAttribute("complete");
 
 					i_rowsRetrievedLabel.setContents(ROWS_RETRIEVED_TITLE + " <b>" + numEntries + "</b>");
 					if (complete != null && !complete.equals("COMPLETE")) {
 						i_rowsRetrievedLabel.setContents(ROWS_RETRIEVED_TITLE + "<b> " + numEntries + "</b>+");
 					} else {
 						i_rowsRetrievedLabel.setContents(ROWS_RETRIEVED_TITLE + " <b>" + numEntries + "</b>");
 					}
 
 					boolean showCount = false;
 					for (String val : i_filterPanel.getFilters().values()) {
 						if (!val.trim().equals("")) {
 							showCount = true;
 							break;
 						}
 					}
 					String searchText = i_searchItem.getValueAsString();
 					if (showCount || (searchText != null && searchText.length() > 0)) {
 
 					} else {
 						i_rowsRetrievedLabel.setContents("");
 					}
 				} else {
 					i_rowsRetrievedLabel.setContents(ROWS_RETRIEVED_TITLE + " <b>0</b>");
 				}
 			}
 		});
 	}
 
 	protected void getValueSets(String searchText, Map<String, String> filters) {
 
 		if (Cts2Viewer.s_showAll) {
 			i_valueSetsListGrid.getData(i_serverCombo.getValueAsString(), searchText, filters);
 		} else {
 			i_valueSetsListGrid.getData(i_defaultServer, searchText, filters);
 		}
 	}
 
 	private void linkWidgets() {
 
 		i_searchItem.addKeyUpHandler(new KeyUpHandler() {
 
 			@Override
 			public void onKeyUp(KeyUpEvent event) {
 
 				event.getKeyName();
 
 				// TODO CME - filter what characters are allowed. Don't allow
 				// up/down arrows.
 
 				// ignore the arrow keys
 				if (i_searchItem.isValidSearchText()) {
 					setClearButtonEnablement();
 					getValueSets(i_searchItem.getValueAsString(), i_filterPanel.getFilters());
 					clearPanels();
 				}
 			}
 		});
 
 		// add a handler to determine when a user clicks on a record in the
 		// valueSets listgrid
 		i_valueSetsListGrid.addRecordClickHandler(new RecordClickHandler() {
 
 			@Override
 			public void onRecordClick(RecordClickEvent event) {
 
 				Record record = event.getRecord();
 				if (record != null) {
 
 					ListGridField selectedField = event.getField();
 
 					// if the user clicked on the download field then don't
 					// retrieve data.
 					if (!selectedField.getName().equals("download")) {
 						updateLinkedPanels(record);
 					}
 				}
 			}
 		});
 		
 //		i_valueSetsListGrid.addSelectionUpdatedHandler(new SelectionUpdatedHandler() {
 //			
 //			@Override
 //			public void onSelectionUpdated(SelectionUpdatedEvent event) {
 //				System.out.println("onSelectionUpdated");
 //				Record record = i_valueSetsListGrid.getSelectedRecord();
 //				
 //				if (record != null) {
 //					
 //					// if the user clicked on the download field then don't
 //					// retrieve data.
 //					//if (!selectedField.getName().equals("download")) {
 //						updateLinkedPanels(record);
 //					//}
 //				}
 //			}
 //		});
 		
 
 		i_valueSetsListGrid.getField("download").addCellSavedHandler(new CellSavedHandler() {
 			@Override
 			public void onCellSaved(CellSavedEvent event) {
 				i_downloadPanel.setDownloadEnabled(hasDownloadRecords());
 			}
 		});
 
 	}
 
 	private void updateLinkedPanels(Record record) {
 		clearPanels();
 		if (record != null) {
 			String link = record.getAttribute("href");
 			String valueSetName = record.getAttribute("valueSetName");
 			String formalName = record.getAttribute("formalName");
 
 			String selectedServer = Cts2Viewer.s_showAll ? i_serverCombo.getValueAsString() : i_defaultServer;
 
 			i_valueSetPropertiesPanel.updatePanel(selectedServer, valueSetName, link);
 			i_resolvedValueSetPropertiesPanel.updatePanel(selectedServer, valueSetName,
 			  formalName, link);
 		}
 	}
 
 	/**
 	 * Determine if there are any rows that are selected for download
 	 * 
 	 * @return
 	 */
 	private boolean hasDownloadRecords() {
 		boolean checkedRow = false;
 
 		Record[] records = i_valueSetsListGrid.getRecords();
 		for (int i = 0; !checkedRow && i < records.length; i++) {
 			checkedRow = records[i].getAttributeAsBoolean("download");
 		}
 
 		return checkedRow;
 	}
 
 	/**
 	 * Make RPC call to logout from the current server
 	 */
 	private void logoutFromServer(Credentials credentials) {
 		Cts2ServiceAsync service = GWT.create(Cts2Service.class);
 		try {
 			service.logout(credentials, new AsyncCallback<Boolean>() {
 
 				@Override
 				public void onFailure(Throwable caught) {
 					// Do nothing
 				}
 
 				@Override
 				public void onSuccess(Boolean result) {
 					// Do nothing
 				}
 			});
 		} catch (Exception e) {
 			lgr.log(Level.WARNING, "Logout from server failed: " + e);
 		}
 	}
 
 	/**
 	 * Make RPC call to retrieve the server(s) to connect to.
 	 */
 	private void retrieveServers() {
 		Cts2ServiceAsync service = GWT.create(Cts2Service.class);
 		try {
 			service.getAvailableServices(new AsyncCallback<LinkedHashMap<String, String>>() {
 
 				@Override
 				public void onFailure(Throwable caught) {
 					SC.warn("Unable to retrieve servers to connect to");
 				}
 
 				@Override
 				public void onSuccess(LinkedHashMap<String, String> result) {
 					String selected = null;
 
 					for (String key : result.keySet()) {
 						if (selected == null) {
 							selected = UiHelper.getSelected(result.get(key));
 							if (selected != null) {
 								result.put(key, selected);
 							}
 						}
 					}
 
 					i_serverCombo.setValueMap(result);
 
 					// Always show this message and let user select a server.
 					i_serverCombo.setValue(SELECT_SERVER_MSG);
 
 					if (Cts2Viewer.s_showAll) {
 						i_loginInfoPanel.addWidgets();
 					}
					else
						i_serverCombo.setValue(i_defaultServer);
 				}
 
 			});
 		} catch (Exception e) {
 			lgr.log(Level.WARNING, "Unable to retrieve servers to connect to.");
 		}
 	}
 
 	/**
 	 * Make RPC call to retrieve the default server to connect to.
 	 */
 	private void retrieveDefaultServer() {
 		Cts2ServiceAsync service = GWT.create(Cts2Service.class);
 		try {
 			service.getDefaultService(new AsyncCallback<String>() {
 
 				@Override
 				public void onFailure(Throwable caught) {
 					SC.warn("Unable to retrieve the default server to connect to.");
 				}
 
 				@Override
 				public void onSuccess(String defaultServer) {
 					i_defaultServer = defaultServer;
 					String title = defaultServer; //i_defaultServer.equals(Cts2Viewer.DEFAULT_SERVER) ? "CTS2 Service" : i_defaultServer;
 					i_defaultServerTextItem.setValue("<b>" + title + "</b>");
 
 					if (!Cts2Viewer.s_showAll) {
 						// fire the server retrieved event
 						Cts2Viewer.EVENT_BUS.fireEvent(new DefaultServerRetrievedEvent(i_defaultServer));
 
 						i_loginInfoPanel.addWidgets();
 
 						// get the server properties of the default server.
 						getServerProperties(i_defaultServer, false);
 					}
 				}
 			});
 		} catch (Exception e) {
 			lgr.log(Level.WARNING, "Unable to retrieve servers to connect to.");
 		}
 	}
 
 	private void setSearchEnablement() {
 		boolean requireCreds = i_serverProperties.isRequireCredentials();
 		if (!requireCreds || requireCreds && loggedIn) {
 			i_searchItem.enable();
 			i_filterPanel.enable();
 		} else {
 			i_searchItem.disable();
 			i_filterPanel.disable();
 		}
 	}
 
 	/**
 	 * The clear button should be disabled if the search text is empty.
 	 */
 	protected void setClearButtonEnablement() {
 		String currentText = i_searchItem.getValueAsString();
 		i_clearButton.setDisabled(currentText == null || currentText.length() == 0);
 	}
 	
 	public String getDefaultServer() {
 		return i_defaultServer;
 	}
 }
