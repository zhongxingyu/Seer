 package net.frontlinesms.plugins.sync.ui;
 
 import net.frontlinesms.events.AppPropertiesEventNotification;
 import net.frontlinesms.events.EventBus;
 import net.frontlinesms.events.EventObserver;
 import net.frontlinesms.events.FrontlineEventNotification;
 import net.frontlinesms.plugins.BasePluginThinletTabController;
 import net.frontlinesms.plugins.sync.SyncPluginController;
 import net.frontlinesms.plugins.sync.SyncPluginProperties;
 import net.frontlinesms.ui.UiGeneratorController;
 
 public class SyncPluginThinletTabController extends BasePluginThinletTabController<SyncPluginController> implements EventObserver{
 //> UI FILES	
 	private static final String TAB_XML_FILE = "/ui/plugins/sync/syncPluginTab.xml";
 	
 //> UI COMPONENTS	
 	private static final String COMPONENT_CHK_STARTUP_MODE = "chkStartAutomatically";
 	private static final String COMPONENT_FLD_SYNCHRONISATION_URL = "txtSynchronisationURL";
 	private static final String COMPONENT_BTN_START_SYNC = "btnStartSynchronisation";
 	private static final String COMPONENT_BTN_STOP_SYNC = "btnStopSynchronisation";
 	private static final String COMPONENT_TB_SYNCHRONISATION_LOG = "tblSychronisationLog";
 	
 //> PROPERTIES	
 	private Object tabComponent;
 	private EventBus eventBus;
 	
 	public SyncPluginThinletTabController(SyncPluginController pluginController, UiGeneratorController uiController) {
 		super(pluginController, uiController);
 		this.tabComponent = uiController.loadComponentFromFile(TAB_XML_FILE, this);
 		super.setTabComponent(tabComponent);
 		
 		// Set the even bus
 		this.eventBus = ui.getFrontlineController().getEventBus();
 		
 		// Register with the event bus
 		this.eventBus.registerObserver(this);
 	}
 	
 	/** Returns a reference to the Sync plugin UI tab */
 	public Object getTab(){
 		return this.tabComponent;
 	}
 		
 	/** 
 	 * Sets the selected value of the the start up mode check box 
 	 */
 	public void setStartupMode(boolean mode) {
 		// Get references to the UI controls
 		Object startButton = ui.find(this.tabComponent, COMPONENT_BTN_START_SYNC);
 		Object stopButton = ui.find(tabComponent, COMPONENT_BTN_STOP_SYNC);
 		
 		modifySynchronisationState(stopButton, startButton, mode);
 		
 	}
 	
 	/** 
 	 * Sets the text for the synchronisation URL field 
 	 * 
 	 * @param syncURL Synchronisation URL 
 	 */
 	public void setSynchronisationURL(String syncURL) {
 		// Get the text field
 		Object txtField = this.ui.find(this.tabComponent, COMPONENT_FLD_SYNCHRONISATION_URL);
 		
 		// Set the new URL text
 		this.ui.setText(txtField, syncURL);
 		
 		// Repaint the component
 		this.ui.repaint(txtField);
 	}
 	
 	/** 
 	 * Gets the synchronisation URL value from the text field in the UI
 	 */ 
 	public String getSynchronisationURL() {
 		String url = ui.getText(getSyncUrlField());
 		return (url.length() == 0 || url == null)?"" : url;
 	}
 	
 	/** Gets a reference to the sync url textfield UI component */
 	private Object getSyncUrlField() {
 		return ui.find(tabComponent, COMPONENT_FLD_SYNCHRONISATION_URL);
 	}
 	
 	/** Gets a reference to the "auto start" checkbox UI component */
 	private Object getStartupModeCheckBox() {
 		return ui.find(this.tabComponent, COMPONENT_CHK_STARTUP_MODE);
 	}
 	
 	/**
 	 * Gets the start up mode currently specified in the UI 
 	 * @return
 	 */
 	public boolean getStartupMode() {
 		return ui.isSelected(ui.find(this.tabComponent, COMPONENT_CHK_STARTUP_MODE));
 	}
 	
 	/** Event helper method for pausing/stopping the synchronisation thread */
 	public void stopSynchronisation(Object startButton, Object stopButton) {
 		ui.setEnabled(ui.find(tabComponent, COMPONENT_FLD_SYNCHRONISATION_URL), true);
 		modifySynchronisationState(stopButton, startButton, false);
 	}
 	
 	/** Event helper method for starting the synchronisation thread */
 	public void startSynchronisation(Object startButton, Object stopButton) {
 		ui.setEnabled(ui.find(tabComponent, COMPONENT_FLD_SYNCHRONISATION_URL), false);
 		modifySynchronisationState(stopButton, startButton, true);
 	}
 	
 	private void modifySynchronisationState(Object stopButton, Object startButton, boolean state) {
		// Set the selection status of the "auto start" checkbox
		ui.setSelected(getStartupModeCheckBox(), state);
 		
 		// Enable/Disable UI controls
 		ui.setEnabled(stopButton, state);
 		ui.setEnabled(startButton, !state);
 		ui.setEnabled(getSyncUrlField(), !state);
 		ui.setEnabled(getStartupModeCheckBox(), !state);
 		
 		// Send stop signal to queue processor
 		SyncPluginController pluginController = (SyncPluginController)getPluginController();
 		pluginController.setQueueProcessorStatus(state);
 		
 		// Repaint the UI
 		ui.repaint(this.tabComponent);
 	}
 	
 	/** Updates the synchronisation log */
 	public void updateSynchronisationLog(String message) {
 		Object table = ui.find(this.tabComponent, COMPONENT_TB_SYNCHRONISATION_LOG);
 		Object row = ui.createTableRow();
 		
 		ui.add(row, ui.createTableCell(message));
 		ui.add(table, row);
 		ui.repaint(table);
 	}
 	
 	/** Removes all the items from the sychronisation log */
 	public void clearSynchronisationLog(Object table) {
 		ui.removeAll(table);
 		
 		ui.repaint();
 	}
 	
 //> EVENT LISTENER METHODS	
 	public void notify(FrontlineEventNotification notification) {
 		//Check for changes in the plugin settings
 		if (notification instanceof AppPropertiesEventNotification) {
 			// An application property has changed
 			AppPropertiesEventNotification appPropertiesNotification = (AppPropertiesEventNotification) notification;
 			
 			// Check if the changes are for the Sync plugin
 			if (appPropertiesNotification.getAppClass().equals(SyncPluginProperties.class)) {
 				
 				// Check for change in the sync URL and auto start up items simultaneously
 				if (appPropertiesNotification.getProperty().equals(SyncPluginProperties.PROP_SYNC_URL) ||
 						appPropertiesNotification.getProperty().equals(SyncPluginProperties.PROP_AUTO_START)) 
 				{
 					// Update the synchronisation URL text field
 					setSynchronisationURL(SyncPluginProperties.getInstance().getSynchronisationURL());
 					
 					// Update the automatic start up checkbox
 					setStartupMode(SyncPluginProperties.getInstance().isAutomaticStartup());
 				}
 			}
 		}
 	}
 }
