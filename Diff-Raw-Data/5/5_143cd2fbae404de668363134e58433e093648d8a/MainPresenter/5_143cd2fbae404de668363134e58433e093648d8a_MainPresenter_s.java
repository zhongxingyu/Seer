 package org.palaso.languageforge.client.lex.main.presenter;
 
 import org.palaso.languageforge.client.lex.common.ConsoleLog;
 import org.palaso.languageforge.client.lex.common.PermissionManager;
 import org.palaso.languageforge.client.lex.common.callback.CallbackComm;
 import org.palaso.languageforge.client.lex.common.callback.CallbackResultString;
 import org.palaso.languageforge.client.lex.common.callback.ICallback;
 import org.palaso.languageforge.client.lex.common.enums.DomainPermissionType;
 import org.palaso.languageforge.client.lex.common.enums.EntryFieldType;
 import org.palaso.languageforge.client.lex.common.enums.OperationPermissionType;
 import org.palaso.languageforge.client.lex.controls.JSNIJQueryWrapper;
 import org.palaso.languageforge.client.lex.jsonrpc.JsonRpcRequestStateListener;
 import org.palaso.languageforge.client.lex.main.MainEventBus;
 import org.palaso.languageforge.client.lex.main.service.BaseService;
 import org.palaso.languageforge.client.lex.main.service.ILexService;
 import org.palaso.languageforge.client.lex.main.view.MainView;
 import org.palaso.languageforge.client.lex.model.CurrentEnvironmentDto;
 import org.palaso.languageforge.client.lex.model.UserDto;
 import org.palaso.languageforge.client.lex.model.settings.tasks.SettingTasksDto;
 import org.palaso.languageforge.client.lex.model.settings.tasks.SettingTasksTaskElementDto;
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.inject.Inject;
 import com.mvp4g.client.annotation.Presenter;
 import com.mvp4g.client.presenter.BasePresenter;
 
 @Presenter(view = MainView.class)
 public class MainPresenter extends BasePresenter<MainPresenter.IView, MainEventBus> {
 
 	private static final String MENU_SETTING = "setting";
 	private static final String MENU_REVIEW = "review";
 	private static final String MENU_ADDEXAMPLES = "addexamples";
 	private static final String MENU_ADDGRAMMATICALUSAGE = "addgrammaticalusage";
 	private static final String MENU_ADDMEANINGS = "addmeanings";
 	private static final String MENU_BROWSEANDEDIT = "browseandedit";
 	private static final String MENU_FROMWORDLIST = "fromwordlist";
 	private static final String MENU_FROMTEXTS = "fromtexts";
 	private static final String MENU_FROMDOMAIN = "fromdomain";
 	private static final String MENU_DASHBOARD = "dashboard";
 
 	private boolean isDashboardMenuVisible = false;
 	private boolean isReviewRecentChangesVisible = false;
 	private boolean isGatherFromTextsVisible = false;
 	private boolean isGatherFromWordListVisible = false;
 	private boolean isGatherFromSemanticDomainsVisible = false;
 	private boolean isReviewBrowseEditVisible = false;
 	private boolean isAddMeaningVisible = false;
 	private boolean isAddGrammaticalPanelVisible = false;
 	private boolean isAddExamplePanelVisible = false;
 	private boolean isConfigureMenuVisible = false;
 	
 	@Inject
 	public ILexService lexService;
 
 	public interface IView {
 
 		void displayErrorMessage(String message);
 
 		void displayText(String message);
 
 	}
 	
 	public void onStart()
 	{
 		onTaskSettingChanged();
 		if (!urlCommand()) {
 			// default Page!
 			eventBus.goToDashboard();
 		}
 		createNativeMenuCallBack();
 	}
 	
 	@Override
 	public void bind() {
 		((BaseService) lexService).getJsonRpc().addReqestStateListener(new JsonRpcRequestStateListener() {
 
 			@Override
 			public void requestStateChanged(boolean requestRunning, boolean isBackgroundRequest) {
 				if (!isBackgroundRequest) { // will not show indicator if it is
 											// a background request
 					if (requestRunning) {
 						eventBus.showLoadingIndicator();
 					} else {
 						eventBus.hideLoadingIndicator();
 					}
 				}
 
 			}
 		});
 		Window.addResizeHandler(new ResizeHandler() {
 			@Override
 			public void onResize(ResizeEvent event) {
 				eventBus.mainWindowResize(event);
 			}
 		});
 	}
 
 	public void onErrorOnLoad(Throwable reason) {
 		view.displayErrorMessage(reason.getMessage());
 	}
 
 	public void onOpenNewWindow(String url) {
 		com.google.gwt.user.client.Window.open(url, "_blank", "");
 	}
 
 	public void onReloadLex() {
 		com.google.gwt.user.client.Window.Location.reload();
 	}
 
 	protected static native void topWindowRedirect(String url) /*-{
 		window.top.location = url;
 	}-*/;
 	
 	
 	
 	private boolean urlCommand() {
 		RootPanel rootPanel = RootPanel.get("GWTContent");
 		if (rootPanel == null) {
 			return false;
 		}
 		String urlCmdName = rootPanel.getElement().getAttribute("targetpage");
 		if (urlCmdName == null) {
 			return false;
 		}
 		urlCmdName = urlCmdName.trim().toLowerCase();
 
 		if (urlCmdName.equals(MENU_FROMTEXTS)) {
 			if (isGatherFromTextsVisible) {
 				eventBus.goToGatherFromTexts();
 			}
 		} else if (urlCmdName.equals(MENU_FROMWORDLIST)) {
 			if (isGatherFromWordListVisible) {
 				eventBus.goToGatherFromWordList();
 			}
 		} else if (urlCmdName.equals(MENU_BROWSEANDEDIT)) {
 			if (isReviewBrowseEditVisible) {
 				eventBus.goToLexDicBrowseAndEdit();
 			}
 		} else if (urlCmdName.equals(MENU_ADDMEANINGS)) {
 			if (isAddMeaningVisible) {
 				eventBus.goToLexMissingInfo(EntryFieldType.DEFINITION);
 			}
 		} else if (urlCmdName.equals(MENU_ADDGRAMMATICALUSAGE)) {
 			if (isAddGrammaticalPanelVisible) {
 				eventBus.goToLexMissingInfo(EntryFieldType.POS);
 			}
 		} else if (urlCmdName.equals(MENU_ADDEXAMPLES)) {
 			if (isAddExamplePanelVisible) {
 				eventBus.goToLexMissingInfo(EntryFieldType.EXAMPLESENTENCE);
 			}
 		} else if (urlCmdName.equals(MENU_SETTING)) {
 			if (isConfigureMenuVisible) {
 				eventBus.goToConfigureSettings();
 			}
 		} else {
 			return false;
 		}
 		return true;
 
 	}
 
 
 
 	public void onTaskSettingChanged() {
 
 		isDashboardMenuVisible = true;
 		isReviewRecentChangesVisible = false;
 		isGatherFromTextsVisible = false;
 		isGatherFromWordListVisible = false;
 		isGatherFromSemanticDomainsVisible = false;
 		isReviewBrowseEditVisible = false;
 		isAddMeaningVisible = false;
 		isAddGrammaticalPanelVisible = false;
 		isAddExamplePanelVisible = false;
 		isConfigureMenuVisible = false;
 
 		JsArray<SettingTasksTaskElementDto> tasksDto = SettingTasksDto
 				.getCurrentUserSetting().getEntries();
 		if (tasksDto.length() > 0) {
 			for (int i = 0; i < tasksDto.length(); i++) {
 				SettingTasksTaskElementDto taskElementDto = tasksDto.get(i);
 
 				switch (taskElementDto.getTaskName()) {
 				case NOTESBROWSER:
 				case ADVANCEDHISTORY:
 					continue;
 				case DASHBOARD:
 					isDashboardMenuVisible = taskElementDto.getVisible();
 					notifyOutsideTasksChanges(MENU_DASHBOARD, taskElementDto.getVisible());
 					continue;
 				case ADDMISSINGINFO:
 					if (taskElementDto.getField()
 							.equalsIgnoreCase("definition")) {
 						isAddMeaningVisible = taskElementDto.getVisible();
 						notifyOutsideTasksChanges(MENU_ADDMEANINGS, taskElementDto.getVisible());
 					} else if (taskElementDto.getField()
 							.equalsIgnoreCase("POS")) {
 						isAddGrammaticalPanelVisible = taskElementDto
 								.getVisible();
 						notifyOutsideTasksChanges(MENU_ADDGRAMMATICALUSAGE, taskElementDto.getVisible());
 					} else if (taskElementDto.getField().equalsIgnoreCase(
 							"ExampleSentence")) {
 						isAddExamplePanelVisible = taskElementDto.getVisible();
 						notifyOutsideTasksChanges(MENU_ADDEXAMPLES, taskElementDto.getVisible());
 					}
 					continue;
 				case GATHERWORDLIST:
 					if (taskElementDto.getWordListFileName().equals("SILCAWL")
 							&& taskElementDto.getLongLabel().isEmpty()) {
 						isGatherFromWordListVisible = taskElementDto
 								.getVisible();
 						notifyOutsideTasksChanges(MENU_FROMWORDLIST, taskElementDto.getVisible());
 					} else if (taskElementDto.getWordListFileName().isEmpty()
 							&& taskElementDto.getLongLabel().equalsIgnoreCase(
 									"from texts")) {
 						isGatherFromTextsVisible = taskElementDto.getVisible();
 						notifyOutsideTasksChanges(MENU_FROMTEXTS, taskElementDto.getVisible());
 					}
 					continue;
 				case DICTIONARY:
 					isReviewBrowseEditVisible = taskElementDto.getVisible();
 					notifyOutsideTasksChanges(MENU_BROWSEANDEDIT, taskElementDto.getVisible());
 					continue;
 				case GATHERWORDSBYSEMANTICDOMAINS:
 					isGatherFromSemanticDomainsVisible = taskElementDto
 							.getVisible();
 					notifyOutsideTasksChanges(MENU_FROMDOMAIN, taskElementDto.getVisible());
 					continue;
 				case CONFIGURESETTINGS:
 					isConfigureMenuVisible = taskElementDto.getVisible();
 					notifyOutsideTasksChanges(MENU_SETTING, taskElementDto.getVisible());
 					continue;
 				case REVIEW:
 					isReviewRecentChangesVisible = taskElementDto.getVisible();
 					notifyOutsideTasksChanges(MENU_REVIEW, taskElementDto.getVisible());
 					continue;
 				default:
 					continue;
 				}
 			}
 		}
 
 		UserDto user = CurrentEnvironmentDto.getCurrentUser();
 		if (user == null) {
 			ConsoleLog.log("Work with out user logged in!");
 			notifyOutsideTasksChanges(MENU_SETTING, false);
 		} else {
 			ConsoleLog.log("Work with user!");
 			notifyOutsideTasksChanges(MENU_SETTING, false);
 			if (PermissionManager.getPermission(
 					DomainPermissionType.DOMAIN_PROJECTS,
 					OperationPermissionType.CAN_EDIT_OTHER)) {
 				notifyOutsideTasksChanges(MENU_SETTING, true);
 			}
 		}
 	}
 	
 	protected void createNativeMenuCallBack() {
 		/*
 		 * eventBus.goToGatherFromSemanticDomains();
 		 * eventBus.goToGatherFromTexts(); eventBus.goToGatherFromWordList();
 		 * eventBus.goToReviewRecentChanges();
 		 * eventBus.goToLexDicBrowseAndEdit();
 		 * eventBus.goToLexMissingInfo(EntryFieldType.DEFINITION);
 		 * eventBus.goToLexMissingInfo(EntryFieldType.POS);
 		 * eventBus.goToLexMissingInfo(EntryFieldType.EXAMPLESENTENCE);
 		 * eventBus.goToConfigureSettings(); eventBus.goToDashboard();
 		 */
 		ICallback<CallbackResultString> externalLinkMenuCallBack = new ICallback<CallbackResultString>() {
 			@Override
 			public void onReturn(CallbackResultString result) {
 				if (result.getReturnValue().trim().equalsIgnoreCase(MainPresenter.MENU_FROMDOMAIN)) {
 					if (isGatherFromSemanticDomainsVisible) {
 						eventBus.goToGatherFromSemanticDomains();
 					}
 
 				} else if (result.getReturnValue().trim().equalsIgnoreCase(MainPresenter.MENU_FROMTEXTS)) {
 					if (isGatherFromTextsVisible) {
 						eventBus.goToGatherFromTexts();
 					}
 
 				} else if (result.getReturnValue().trim().equalsIgnoreCase(MainPresenter.MENU_FROMWORDLIST)) {
 					if (isGatherFromWordListVisible) {
 						eventBus.goToGatherFromWordList();
 					}
 
 				} else if (result.getReturnValue().trim().equalsIgnoreCase(MainPresenter.MENU_REVIEW)) {
 					if (isReviewRecentChangesVisible) {
 						eventBus.goToReviewRecentChanges();
 					}
 				} else if (result.getReturnValue().trim().equalsIgnoreCase(MainPresenter.MENU_BROWSEANDEDIT)) {
 					if (isReviewBrowseEditVisible) {
 						eventBus.goToLexDicBrowseAndEdit();
 					}
 				} else if (result.getReturnValue().trim().equalsIgnoreCase(MainPresenter.MENU_ADDMEANINGS)) {
 					if (isAddMeaningVisible) {
 						eventBus.goToLexMissingInfo(EntryFieldType.DEFINITION);
 					}
 				} else if (result.getReturnValue().trim().equalsIgnoreCase(MainPresenter.MENU_ADDGRAMMATICALUSAGE)) {
 					if (isAddGrammaticalPanelVisible) {
 						eventBus.goToLexMissingInfo(EntryFieldType.POS);
 					}
 				} else if (result.getReturnValue().trim().equalsIgnoreCase(MainPresenter.MENU_ADDEXAMPLES)) {
 					if (isAddExamplePanelVisible) {
 						eventBus.goToLexMissingInfo(EntryFieldType.EXAMPLESENTENCE);
 					}
 				} else if (result.getReturnValue().trim().equalsIgnoreCase(MainPresenter.MENU_SETTING)) {
 					if (isConfigureMenuVisible) {
 						eventBus.goToConfigureSettings();
 					}
 				} else if (result.getReturnValue().trim().equalsIgnoreCase(MainPresenter.MENU_DASHBOARD)) {
 					if (isDashboardMenuVisible) {
 						eventBus.goToDashboard();
 					}
 				}
 				JSNIJQueryWrapper.closeAllJQueryOpenClose();
 				eventBus.clearMessageBox();
 			}
 		};
 		JavaScriptObject jsCallback = CallbackComm.createNativeCallback(externalLinkMenuCallBack);
 		exportMenuToJavaScript(jsCallback);
 	}
 
 	protected static native void notifyOutsideTasksChanges(String taskName, boolean visible) /*-{
 		if (typeof ($wnd.GWTTaskSettingsChanged) == "function") {
 			$wnd.GWTTaskSettingsChanged(taskName, visible);
 		} else {
 			console.log("window.GWTTaskSettingsChanged(tasks) not defined!");
 		}
 		;
 	}-*/;
 
 	protected static native void exportMenuToJavaScript(JavaScriptObject externalLinkMenuCallBack) /*-{
 		$wnd.openGWTPage = function(pageName) {
 			var callbackObj = new Object();
 			callbackObj.value = pageName;
 			callbackObj.success = true;
 			callbackObj.data = pageName;
 			externalLinkMenuCallBack(callbackObj);
 		};
 	}-*/;
 
 }
