 /**
  * 
  */
 package net.frontlinesms.ui.handler.core;
 
 import java.util.List;
 
 import org.springframework.context.ApplicationContext;
 
 import net.frontlinesms.AppProperties;
 import net.frontlinesms.ui.DatabaseSettings;
 import net.frontlinesms.ui.UiGeneratorController;
 import net.frontlinesms.ui.handler.BasePanelHandler;
 import net.frontlinesms.ui.i18n.InternationalisationUtils;
 
 /**
  * @author aga
  *
  */
 public class DatabaseSettingsDialog extends BasePanelHandler {
 
 //> STATIC CONSTANTS
 	private static final String XML_SETTINGS_PANEL = "/ui/core/database/pnSettings.xml";
 	private static final String I18N_KEY_DATABASE_CONFIG = "common.database.config";
 	
 	private static final String COMPONENT_SETTINGS_SELECTION = "cbConfigFile";
 
 //> INSTANCE PROPERTIES
 	private List<DatabaseSettings> databaseSettings;
 	/** The settings currently selected in the combobox */
 	private DatabaseSettings selectedSettings;
 	
 	/** Dialog UI Component.  This should only be used if {@link #showAsDialog()} is called, and then should only be used by {@link #removeDialog()}. */
 	private Object dialogComponent;
 	
 	/**
 	 * Set <code>true</code> if a restart is required after changing settings.  Set <code>false</code> if we are
 	 * modifying the settings before the {@link ApplicationContext} has been started.
 	 */
 	private boolean restartRequired;
 	
 //> CONSTRUCTORS
 	public DatabaseSettingsDialog(UiGeneratorController ui) {
 		super(ui);
 	}
 	
 	/**
 	 * 
 	 * @param restartRequired
 	 */
 	public void init(boolean restartRequired) {
 		this.restartRequired = restartRequired;
 		super.loadPanel(XML_SETTINGS_PANEL);
 		
 		// Populate combobox
 		String selectedDatabaseConfigPath = AppProperties.getInstance().getDatabaseConfigPath();
		List<DatabaseSettings> databaseSettings = DatabaseSettings.getSettings();
 		Object settingsSelection = getConfigFileSelecter();
 		for(int settingsIndex=0; settingsIndex<this.databaseSettings.size(); ++settingsIndex) {
 			DatabaseSettings settings = databaseSettings.get(settingsIndex);
 			Object comboBox = createComboBox(settings);
 			ui.add(settingsSelection, comboBox);
 
 			// if appropriate, choose the combobox selection
 			if(settings.getFilePath().equals(selectedDatabaseConfigPath)) {
				selectedSettings = settings;
 				ui.setSelectedIndex(settingsSelection, settingsIndex);
 			}
 		}
		this.databaseSettings = databaseSettings;
 		
 		// populate settings panel
 		refreshSettingsPanel();
 	}
 
 //> INSTANCE HELPER METHODS
 	private void setSelectedConfigFile(DatabaseSettings settings) {
 		this.selectedSettings = settings;
 	}
 	
 //> UI HELPER METHODS
 	/** Refresh the panel containing settings specific to the currently-selected {@link DatabaseSettings}. */
 	private void refreshSettingsPanel() {
 		// TODO populate the settings panel
 	}
 
 	private Object getConfigFileSelecter() {
 		return super.find(COMPONENT_SETTINGS_SELECTION);
 	}
 	
 	private Object createComboBox(DatabaseSettings settings) {
 		Object cb = ui.createComboboxChoice(settings.getName(), settings);
 		// TODO perhaps we could set a settings-specific icon here
 		return cb;
 	}
 	
 //> UI EVENT METHODS
 	public void configFileChanged() {
 		Object selected = ui.getSelectedItem(getConfigFileSelecter());
 		if(selected != null) {
 			setSelectedConfigFile(ui.getAttachedObject(selected, DatabaseSettings.class));
 			
 			refreshSettingsPanel();
 		}
 	}
 	
 	/**
 	 * Save button pressed: Saves the database settings and restarts FrontlineSMS to use
 	 * the new settings.
 	 */
 	public void save() {
 		// get the settings we are modifying
 		DatabaseSettings selectedSettings = this.selectedSettings;
 		
 		// check if the settings file has changed
 		AppProperties appProperties = AppProperties.getInstance();
 		boolean settingsFileChanged = !selectedSettings.getFilePath().equals(appProperties.getDatabaseConfigPath());
 		// If settings file has NOT changed, check if individual settings have changed
 		boolean updateIndividualSettings = false;
 		if(!settingsFileChanged) {
 			// We are modifying the current settings rather than changing to a whole new database config
 			
 			// TODO check if the settings have changed at all 
 		}
 		
 		if(!settingsFileChanged && !updateIndividualSettings) {
 			// Nothing has changed, so no need to update anything
 			ui.alert("You did not make any changes to the settings."); // FIXME i18n
 		} else {
 			if(settingsFileChanged) {
 				appProperties.setDatabaseConfigPath(selectedSettings.getFilePath());
 				appProperties.saveToDisk();
 			} else if(updateIndividualSettings) {
 				// TODO implement saving of settings changes
 			}
 			
 			if(restartRequired) {
 				// Display alert warning the user that their settings have changed, and they
 				// must restart FrontlineSMS for the changes to take effect.  Restarting automatically
 				// may be quite complicated, and the gain would be little.
 				ui.alert("Settings saved.  You are advised to restart FrontlineSMS immediately."); // FIXME i18n
 			}
 		}
 		
 		removeDialog();
 	}
 	
 	/** Cancel button pressed. */
 	public void cancel() {
 		// If we are displaying the settings panel as a dialog, remove it.  Otherwise TODO call the callback method?
 		if(this.dialogComponent != null) {
 			removeDialog();
 		}
 	}
 
 	/**
 	 * Show this panel as a dialog.  The dialog will be removed by default by the removeDialog method.
 	 * @param titleI18nKey
 	 */
 	public void showAsDialog() {
 		Object dialogComponent = ui.createDialog(InternationalisationUtils.getI18NString(I18N_KEY_DATABASE_CONFIG));
 		ui.add(dialogComponent, this.getPanelComponent());
 		ui.setCloseAction(dialogComponent, "removeDialog", dialogComponent, this);
 		ui.add(dialogComponent);
 		this.dialogComponent = dialogComponent;
 	}
 	
 //> UI EVENT METHODS
 	public void removeDialog() {
 		this.ui.removeDialog(this.dialogComponent);
 	}
 }
