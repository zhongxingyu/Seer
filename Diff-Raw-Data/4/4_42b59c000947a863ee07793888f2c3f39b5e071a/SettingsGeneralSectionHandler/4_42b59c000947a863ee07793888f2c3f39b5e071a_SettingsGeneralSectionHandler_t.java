 package net.frontlinesms.ui.handler.settings;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.frontlinesms.AppProperties;
 import net.frontlinesms.events.AppPropertiesEventNotification;
 import net.frontlinesms.settings.BaseSectionHandler;
 import net.frontlinesms.settings.FrontlineValidationMessage;
 import net.frontlinesms.ui.ThinletUiEventHandler;
 import net.frontlinesms.ui.UiGeneratorController;
 import net.frontlinesms.ui.i18n.InternationalisationUtils;
 import net.frontlinesms.ui.settings.UiSettingsSectionHandler;
 
 public class SettingsGeneralSectionHandler extends BaseSectionHandler implements UiSettingsSectionHandler, ThinletUiEventHandler {
 	private static final String UI_SECTION_GENERAL = "/ui/core/settings/general/pnGeneralSettings.xml";
 	private static final String UI_COMPONENT_CB_PROMPT_STATS = "cbPromptStats";
 	private static final String UI_COMPONENT_CB_AUTHORIZE_STATS = "cbAuthorizeStats";
 	private static final String UI_COMPONENT_TF_COST_PER_SMS_SENT = "tfCostPerSMSSent";
 	private static final String UI_COMPONENT_LB_COST_PER_SMS_SENT_PREFIX = "lbCostPerSmsSentPrefix";
 	private static final String UI_COMPONENT_LB_COST_PER_SMS_SENT_SUFFIX = "lbCostPerSmsSentSuffix";
 	private static final String UI_COMPONENT_TF_COST_PER_SMS_RECEIVED = "tfCostPerSMSReceived";
 	private static final String UI_COMPONENT_LB_COST_PER_SMS_RECEIVED_PREFIX = "lbCostPerSmsReceivedPrefix";
 	private static final String UI_COMPONENT_LB_COST_PER_SMS_RECEIVED_SUFFIX = "lbCostPerSmsReceivedSuffix";
 	
 	private static final String I18N_SETTINGS_INVALID_COST_PER_MESSAGE_RECEIVED = "settings.message.invalid.cost.per.message.received";
 	private static final String I18N_SETTINGS_INVALID_COST_PER_MESSAGE_SENT = "settings.message.invalid.cost.per.message.sent";
 	private static final String I18N_SETTINGS_MENU_GENERAL = "settings.menu.general";
 
 	private static final String SECTION_ITEM_PROMPT_STATS = "GENERAL_STATS_PROMPT_DIALOG";
 	private static final String SECTION_ITEM_AUTHORIZE_STATS = "GENERAL_STATS_AUTHORIZE_SENDING";
 	private static final String SECTION_ITEM_COST_PER_SMS_SENT = "GENERAL_COST_PER_SMS_SENT";
 	private static final String SECTION_ITEM_COST_PER_SMS_RECEIVED = "GENERAL_COST_PER_SMS_RECEIVED";
 	
 	public SettingsGeneralSectionHandler (UiGeneratorController ui) {
 		super(ui);
 		this.uiController = ui;
 		
 		this.init();
 	}
 	
 	private void init() {
 		this.panel = uiController.loadComponentFromFile(UI_SECTION_GENERAL, this);
 		
 		this.initStatisticsSettings();
 		this.initCostEstimatorSettings();
 	}
 	
 	private void initCostEstimatorSettings() {
 		boolean isCurrencySymbolPrefix = InternationalisationUtils.isCurrencySymbolPrefix();
 		String currencySymbol = InternationalisationUtils.getCurrencySymbol();
 		
 		AppProperties appProperties = AppProperties.getInstance();
 		
 		String costPerSmsReceived = InternationalisationUtils.formatCurrency(appProperties.getCostPerSmsReceived(), false);
 		String costPerSmsSent = InternationalisationUtils.formatCurrency(appProperties.getCostPerSmsSent(), false);
 
 		this.uiController.setText(find(UI_COMPONENT_TF_COST_PER_SMS_SENT), costPerSmsSent);
 		this.uiController.setText(find(UI_COMPONENT_LB_COST_PER_SMS_SENT_PREFIX), isCurrencySymbolPrefix ? currencySymbol : "");
 		this.uiController.setText(find(UI_COMPONENT_LB_COST_PER_SMS_SENT_SUFFIX), isCurrencySymbolPrefix ? "" : currencySymbol);
 		
 		this.uiController.setText(find(UI_COMPONENT_TF_COST_PER_SMS_RECEIVED), costPerSmsReceived);
 		this.uiController.setText(find(UI_COMPONENT_LB_COST_PER_SMS_RECEIVED_PREFIX), isCurrencySymbolPrefix ? currencySymbol : "");
 		this.uiController.setText(find(UI_COMPONENT_LB_COST_PER_SMS_RECEIVED_SUFFIX), isCurrencySymbolPrefix ? "" : currencySymbol);
 		
 		this.originalValues.put(SECTION_ITEM_COST_PER_SMS_RECEIVED, costPerSmsReceived);
 		this.originalValues.put(SECTION_ITEM_COST_PER_SMS_SENT, costPerSmsSent);
 	}
 
 	private void initStatisticsSettings() {
 		AppProperties appProperties = AppProperties.getInstance();
 		
 		boolean shouldPromptStatsDialog = appProperties.shouldPromptStatsDialog();
 		boolean isStatsSendingAuthorized = appProperties.isStatsSendingAuthorized();
 		
 		this.originalValues.put(SECTION_ITEM_PROMPT_STATS, shouldPromptStatsDialog);
 		this.originalValues.put(SECTION_ITEM_AUTHORIZE_STATS, isStatsSendingAuthorized);
 		
 		this.uiController.setSelected(find(UI_COMPONENT_CB_PROMPT_STATS), shouldPromptStatsDialog);
 		
 		this.uiController.setSelected(find(UI_COMPONENT_CB_AUTHORIZE_STATS), isStatsSendingAuthorized);
 		this.uiController.setEnabled(find(UI_COMPONENT_CB_AUTHORIZE_STATS), !shouldPromptStatsDialog);
 	}
 
 	/**
 	 * Called when the "Prompt the statistics dialog" checkbox has changed state.
 	 */
 	public void promptStatsChanged () {
 		boolean shouldPromptStatsDialog = this.uiController.isSelected(find(UI_COMPONENT_CB_PROMPT_STATS));
 		settingChanged(SECTION_ITEM_PROMPT_STATS, shouldPromptStatsDialog);
 		
 		this.uiController.setEnabled(find(UI_COMPONENT_CB_AUTHORIZE_STATS), !shouldPromptStatsDialog);
 	}
 	
 	/**
 	 * Called when the "Authorize statistics" checkbox has changed state.
 	 */
 	public void authorizeStatsChanged () {
 		boolean authorizeStats = this.uiController.isSelected(find(UI_COMPONENT_CB_AUTHORIZE_STATS));
 		settingChanged(SECTION_ITEM_AUTHORIZE_STATS, authorizeStats);
 	}
 	
 	/**
 	 * Called when the cost per SMS (sent or received) has changed.
 	 */
 	public void costPerSmsChanged(Object textField) {
 		if (textField.equals(find(UI_COMPONENT_TF_COST_PER_SMS_RECEIVED))) {
 			super.settingChanged(SECTION_ITEM_COST_PER_SMS_RECEIVED, this.uiController.getText(textField));
 		} else {
 			super.settingChanged(SECTION_ITEM_COST_PER_SMS_SENT, this.uiController.getText(textField));
 		}
 	}
 
 	public void save() {
 		/*** STATISTICS ***/
 		AppProperties appProperties = AppProperties.getInstance();
 		
 		appProperties.shouldPromptStatsDialog(this.uiController.isSelected(find(UI_COMPONENT_CB_PROMPT_STATS)));
 		appProperties.setAuthorizeStatsSending(this.uiController.isSelected(find(UI_COMPONENT_CB_AUTHORIZE_STATS)));
 		
 		try {
 			double costPerSmsSent = InternationalisationUtils.parseCurrency(this.uiController.getText(find(UI_COMPONENT_TF_COST_PER_SMS_SENT)));
 
 			if (costPerSmsSent != appProperties.getCostPerSmsSent()) {
 				appProperties.setCostPerSmsSent(costPerSmsSent);
 				this.eventBus.notifyObservers(new AppPropertiesEventNotification(AppProperties.class, AppProperties.KEY_SMS_COST_SENT_MESSAGES));
 			}
 			
			double costPerSmsReceived = InternationalisationUtils.parseCurrency(this.uiController.getText(find(UI_COMPONENT_TF_COST_PER_SMS_RECEIVED)));
			if (costPerSmsReceived != appProperties.getCostPerSmsReceived()) {
 				appProperties.setCostPerSmsReceived(costPerSmsReceived);
 				this.eventBus.notifyObservers(new AppPropertiesEventNotification(AppProperties.class, AppProperties.KEY_SMS_COST_RECEIVED_MESSAGES));
 			}
 		} catch (ParseException e) {
 			// Should never happen
 		}
 		
 		appProperties.saveToDisk();		
 	}
 
 	public List<FrontlineValidationMessage> validateFields() {
 		List<FrontlineValidationMessage> validationMessages = new ArrayList<FrontlineValidationMessage>();
 		
 		try {
 			double costPerSmsSent = InternationalisationUtils.parseCurrency(this.uiController.getText(find(UI_COMPONENT_TF_COST_PER_SMS_SENT)));
 			if (costPerSmsSent < 0) {
 				validationMessages.add(new FrontlineValidationMessage(I18N_SETTINGS_INVALID_COST_PER_MESSAGE_SENT, null));
 			}
 		} catch (ParseException e) {
 			validationMessages.add(new FrontlineValidationMessage(I18N_SETTINGS_INVALID_COST_PER_MESSAGE_SENT, null));
 		}
 			
 		try {
 			double costPerSmsReceived = InternationalisationUtils.parseCurrency(this.uiController.getText(find(UI_COMPONENT_TF_COST_PER_SMS_RECEIVED)));
 			if (costPerSmsReceived < 0) {
 				validationMessages.add(new FrontlineValidationMessage(I18N_SETTINGS_INVALID_COST_PER_MESSAGE_RECEIVED, null));
 			}
 		} catch (ParseException e) {
 			validationMessages.add(new FrontlineValidationMessage(I18N_SETTINGS_INVALID_COST_PER_MESSAGE_RECEIVED, null));
 		}
 		
 		return validationMessages;
 	}
 	
 	public String getTitle() {
 		return InternationalisationUtils.getI18NString(I18N_SETTINGS_MENU_GENERAL);
 	}
 }
