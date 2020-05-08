 /*
  * Funambol is a mobile platform developed by Funambol, Inc.
  * Copyright (C) 2009 Funambol, Inc.
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation with the addition of the following permission
  * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
  * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE
  * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  * 
  * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite
  * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
  *
  * The interactive user interfaces in modified source and object code versions
  * of this program must display Appropriate Legal Notices, as required under
  * Section 5 of the GNU Affero General Public License version 3.
  *
  * In accordance with Section 7(b) of the GNU Affero General Public License
  * version 3, these Appropriate Legal Notices must retain the display of the
  * "Powered by Funambol" logo. If the display of the logo is not reasonably
  * feasible for technical reasons, the Appropriate Legal Notices must display
  * the words "Powered by Funambol".
  */
 
 package com.funambol.client.controller;
 
 import java.util.Vector;
 import java.util.Enumeration;
 
 import com.funambol.org.json.me.JSONObject;
 import com.funambol.org.json.me.JSONArray;
 import com.funambol.org.json.me.JSONException;
 
 import com.funambol.sapisync.SapiSyncHandler;
 import com.funambol.sapisync.SapiException;
 import com.funambol.client.customization.Customization;
 import com.funambol.client.configuration.Configuration;
 import com.funambol.client.source.AppSyncSource;
 import com.funambol.client.source.AppSyncSourceConfig;
 import com.funambol.client.source.AppSyncSourceManager;
 import com.funambol.client.localization.Localization;
 import com.funambol.client.ui.AccountScreen;
 import com.funambol.client.ui.DisplayManager;
 import com.funambol.sync.SyncException;
 import com.funambol.util.StringUtil;
 import com.funambol.util.Log;
 
 /**
  * This class is the controller (in the MVC model) for the AccountScreen.
  */
 public class AccountScreenController extends SynchronizationController {
 
     private static final String TAG_LOG = "AccountScreenController";
 
     protected AccountScreen        screen = null;
 
     protected AppSyncSource        configAppSource  = null;
     protected boolean              failed           = false;
     protected boolean              sourceStarted    = false;
     protected Exception            exp              = null;
 
     protected String               originalUrl      = null;
     protected String               originalUser     = null;
     protected String               originalPassword = null;
 
     protected boolean              networkUsageWarningShown = false;
 
     public AccountScreenController(Controller controller, AccountScreen accountScreen) {
         super(controller, accountScreen, null);
 
         this.screen = accountScreen;
         
         // Save the original values so that we can revert changes at any time
         originalUrl = configuration.getSyncUrl() != null ?  configuration.getSyncUrl() : ""; 
         originalUser = configuration.getUsername() != null ?  configuration.getUsername() : "";
         originalPassword = configuration.getPassword() != null ?  configuration.getPassword() : "";
     }
 
     /**
      * TODO: Remove once the com.funambol.client.controller package integration is finished
      */
     public AccountScreenController(Controller controller, Customization customization, 
             Configuration configuration, Localization localization,
             AppSyncSourceManager appSyncSourceManager, AccountScreen accountScreen) {
 
         super(controller, customization, configuration, localization,
                 appSyncSourceManager, accountScreen, null);
 
         this.screen = accountScreen;
 
         // Save the original values so that we can revert changes at any time
         originalUrl = configuration.getSyncUrl() != null ?  configuration.getSyncUrl() : "";
         originalUser = configuration.getUsername() != null ?  configuration.getUsername() : "";
         originalPassword = configuration.getPassword() != null ?  configuration.getPassword() : "";
     }
 
     public AccountScreen getAccountScreen() {
         return screen;
     }
 
     public void saveAndCheck() {
         if(screen != null) {
             String serverUri;
             if (customization.syncUriEditable()) {
                 serverUri = screen.getSyncUrl();
             } else {
                 serverUri = customization.getServerUriDefault();
             }
             saveAndCheck(serverUri, screen.getUsername(), screen.getPassword());
         } else {
             Log.error(TAG_LOG, "Cannot save, account screen is null");
         }
     }
 
     public void synchronize(String syncType, Vector sources) throws SyncException {
         checkStarted();
         super.synchronize(syncType, sources);
     }
 
     public void saveAndCheck(String serverUri, String username, String password) {
 
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "saveAndCheck");
         }
 
         // Check if a sync is currently running, and in this case warn the user
         // that the account cannot be saved
         if (isSyncInProgress()) {
             showSyncInProgressMessage();
             return;
         }
 
         // Trim all values so that spaces
         serverUri = serverUri.trim();
         username  = username.trim();
         password  = password.trim();
 
         // Load all the default settings and overwrite the parameters edited
         // here
         configuration.load();
 
         // Preliminary check
         if (   StringUtil.isNullOrEmpty(username)
             || StringUtil.isNullOrEmpty(password)
             || StringUtil.isNullOrEmpty(serverUri))
         {
             showMessage(localization.getLanguage("login_failed_empty_params"));
             return;
         } else if (!StringUtil.isValidProtocol(serverUri)) {
             showMessage(localization.getLanguage("status_invalid_url"));
             return;
         }
 
         if(    !originalUser.equals(serverUri)
             || !originalUser.equals(username)
             || !originalPassword.equals(password)
             || configuration.getCredentialsCheckPending())
         {
             // Okay, save the configuration
             configuration.setSyncUrl(serverUri);
             configuration.setUsername(username);
             configuration.setPassword(password);
             // Reset the server dev inf
             configuration.setServerDevInf(null);
 
             if (configuration.save() != Configuration.CONF_OK) {
                 showMessage(localization.getLanguage("message_config_error") + ": " +
                         localization.getLanguage("message_config_error_save"));
                 return;
             }
 
             // If the credentials check is not needed we directly authenticate
             // the user
             if(!customization.getCheckCredentialsInLoginScreen()) {
                 new Thread() {
                     public void run() {
                         userAuthenticated();
                     }
                 }.start();
                 return;
             }
 
             ContinueSyncAction csa = new ContinueSyncAction(serverUri, username, password); 
             AbortAfterNetwokUsageWarning asa = new AbortAfterNetwokUsageWarning();
             boolean prompt = promptUserForNetworkUsage(csa, asa);
             if (!prompt) {
                 loginViaSapi(serverUri, username, password);
             }
         } else {
             // There was no need to authenticate
             if (Log.isLoggable(Log.DEBUG)) {
                 Log.debug(TAG_LOG, "No need to authenticate");
             }
         }
     }
 
     protected boolean promptUserForNetworkUsage(Runnable continueAction, Runnable abortAction) {
         // We always authenticate via SAPI. If the server does not support
         // SAPI, we let the user login directly
         boolean prompt = false;
         if (customization.getShowNetworkUsageWarningForProfiles() && !networkUsageWarningShown) {
             long profileExpireDate = configuration.getProfileExpireDate();
             // If this is the very first time, or the expire time has
             // expired, or the profile requires the warning, then we
             // show it
             if (   profileExpireDate == -1 
                 || profileExpireDate > System.currentTimeMillis()
                 || configuration.getProfileNetworkUsageWarning())
             {
                 networkUsageWarningShown = true;
                 NetworkUsageWarningController nuwc = new NetworkUsageWarningController(screen, controller,
                                                                                        continueAction,
                                                                                        abortAction);
                 nuwc.askUserNetworkUsageConfirmation();
                 prompt = true;
             }
         }
         return prompt;
     }
 
     protected boolean checkStarted() {
         return true;
     }
 
     private void loginViaSapi(String serverUri, String username, String password) {
         SapiLoginThread th = new SapiLoginThread(serverUri, username, password);
         th.start();
     }
 
 
     protected boolean isSyncInProgress() {
         HomeScreenController homeScreenController = controller.getHomeScreenController();
         return homeScreenController.isSynchronizing();
     }
 
     public void sourceFailed(AppSyncSource appSource, SyncException e) {
         super.sourceFailed(appSource, e);
 
         // In order to guarantee compatibility with servers without a
         // ConfigSyncSource, we must allow users to access the home screen even
         // if the server sends a 404 status for this sync
         if (e.getCode() != SyncException.NOT_FOUND_URI_ERROR) {
             exp = e;
             failed = true;
         } else {
             if (Log.isLoggable(Log.INFO)) {
                Log.info(TAG_LOG, "Server does not support login, most likely not a Funambol server or a comed one");
             }
             // Apply the default server configuration as the server did not
             // provide its capabilities. This has the side effect of hiding the
             // picture source
             controller.reapplyServerCaps(null);
         }
     }
 
     public void sourceStarted(AppSyncSource appSource) {
         super.sourceStarted(appSource);
         sourceStarted = true;
         configuration.setServerType(Configuration.SERVER_TYPE_OTHER);
     }
 
     public void sourceEnded(AppSyncSource appSource) {
         super.sourceEnded(appSource);
         configuration.setServerType(Configuration.SERVER_TYPE_FUNAMBOL_CARED);
     }
 
     public void syncEnded() {
         super.syncEnded();
 
         networkUsageWarningShown = false;
 
         screen.enableSave();
         if (failed && sourceStarted) {
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, "Cannot access home screen");
             }
             screen.checkFailed();
             // Clear the configuration for no pending credentials check
             configuration.setCredentialsCheckPending(true);
             configuration.save();
             // Now we show an error to the user, depending on the error we got
             String msg;
             if (exp instanceof SyncException) {
                 msg = getMessageFromSyncException((SyncException)exp);
                 if(msg == null) {
                     return;
                 }
             } else {
                 msg = localization.getLanguage("status_generic_error");
             }
             // We should never fall into this case, unless we miss some strings
             // in the language table
             if (msg == null) {
                 msg = localization.getLanguage("status_generic_error");
             }
             // Show an error to the user
             showMessage(msg);
 
         } else if (!sourceStarted) {
             // Sync the source did not start, an appropriate error was displayed
             // by the syncrhonizationController
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, "Cannot access home screen");
             }
             // Clear the configuration for no pending credentials check
             configuration.setCredentialsCheckPending(true);
             configuration.save();
             screen.checkFailed();
         } else {
             // The user is authenticated, hide the login and open the main view
             // screen
             userAuthenticated();
 
             originalUrl = screen.getSyncUrl();
             originalUser = screen.getUsername();
             originalPassword = screen.getPassword();
         }
     }
 
     protected String getMessageFromSyncException(SyncException ex) {
         String msg;
         switch (ex.getCode()) {
             case SyncException.AUTH_ERROR:
                 msg = localization.getLanguage("status_invalid_credentials");
                 break;
             case SyncException.FORBIDDEN_ERROR:
                 msg = localization.getLanguage("status_forbidden_error");
                 break;
             case SyncException.DATA_NULL:
             case SyncException.CONN_NOT_FOUND:
                 msg = localization.getLanguage("status_invalid_url");
                 break;
             case SyncException.READ_SERVER_RESPONSE_ERROR:
             case SyncException.WRITE_SERVER_REQUEST_ERROR:
             case SyncException.SERVER_CONNECTION_REQUEST_ERROR:
                 msg = localization.getLanguage("status_network_error");
                 break;
             case SyncException.CONNECTION_BLOCKED_BY_USER:
                 msg = localization.getLanguage("status_connection_blocked");
                 break;
             case SyncException.CANCELLED:
                 // In this case we shall simply go back to the account
                 // screen, so we just return from this method
                 msg = null;
             default:
                 msg = localization.getLanguage("status_generic_error");
                 break;
         }
         return msg;
     }
 
     public boolean hasChanges(String serverUri, String username, String password) {
         if (customization.syncUriEditable()) {
 
             // We are a bit flexible in the URL comparison as we allow users to
             // change for example from http to https and viceversa
             String url1 = StringUtil.removeProtocolFromUrl(originalUrl);
             String url2 = StringUtil.removeProtocolFromUrl(serverUri);
             boolean sameUrl = url1.equals(url2);
             if (!sameUrl) {
                 return true;
             }
         }
         if (!originalUser.equals(username) ||
             !originalPassword.equals(password)) {
             return true;
         }
         return false;
     }
 
     public void resetValues() {
         if (customization.syncUriEditable()) {
             screen.setSyncUrl(originalUrl);
             configuration.setSyncUrl(originalUrl);
         }
         screen.setUsername(originalUser);
         screen.setPassword(originalPassword);
         configuration.setUsername(originalUser);
         configuration.setPassword(originalPassword);
         configuration.save();
     }
 
     public void endSync(Vector sources, boolean hadErrors) {
         // Errors are handled in the syncEnded method
         setCancel(false);
     }
 
     public void hide() {
         controller.toBackground();
     }
 
     public void initScreen() {
         Configuration config = controller.getConfiguration();
         String url, usr, pwd;
         if (config.load() == Configuration.CONF_OK) {
             url = config.getSyncUrl();
             usr = config.getUsername();
             pwd = config.getPassword();
         } else {
             Log.error(TAG_LOG, "Error loading the configuration, using default values");
             url = customization.getServerUriDefault();
             usr = customization.getUserDefault();
             pwd = customization.getPasswordDefault();
         }
         initScreen(url, usr, pwd); 
     }
 
     public void initScreen(String url, String usr, String pwd) {
         if(screen != null) {
             screen.setSyncUrl(url);
             originalUrl = url;
             screen.setUsername(usr);
             originalUser = usr;
             screen.setPassword(pwd);
             originalPassword = pwd;
         }
     }
 
     protected void showSyncInProgressMessage() {
         // If the home screen is not displayed, we cannot show any warning and
         // just ignore this event
         if (screen != null) {
             DisplayManager dm = controller.getDisplayManager();
             String msg = localization.getLanguage("message_sync_running_wait");
             dm.showMessage(screen, msg);
         }
     }
 
     protected void userAuthenticated() {
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "Opening home screen");
         }
 
         // An account has been created. So keep track of it in order to not 
         // display the signup screen again
         configuration.setSignupAccountCreated(true);
         configuration.setCredentialsCheckPending(false);
         configuration.save();
 
         screen.checkSucceeded();
     }
 
     public void switchToSignupScreen() {
         try {
             controller.getDisplayManager().showScreen(screen, Controller.SIGNUP_SCREEN_ID);
             controller.getDisplayManager().hideScreen(screen);
         } catch(Exception ex) {
             Log.error(TAG_LOG, "Unable to switch to login screen", ex);
         }
     }
 
     private class SapiLoginThread extends Thread {
 
         private String serverUri;
         private String username;
         private String password;
 
         public SapiLoginThread(String serverUri, String username, String password) {
             this.serverUri = serverUri;
             this.username = username;
             this.password = password;
         }
 
         public void run() {
             // In this case we can simply check credentials invoking the
             // login SAPI and fecthing the list of available sources and
             // their status for the user
             AppSyncSource configAppSource = appSyncSourceManager.getSource(AppSyncSourceManager.CONFIG_ID);
             try {
                 boolean goAhead = checkStarted();
 
                 if (!goAhead) {
                     return;
                 }
 
                 sourceStarted(configAppSource);
                 configuration.setTempLogLevel(Log.TRACE);
                 new ProfileUpdateHelper(appSyncSourceManager, configuration).updateProfile();
                 sourceEnded(configAppSource);
             } catch (SapiException se) {
                 Log.error(TAG_LOG, "SapiException during login", se);
                 SyncException syncExc;
                 if (SapiException.HTTP_401.equals(se.getCode())) {
                     syncExc = new SyncException(SyncException.AUTH_ERROR, se.toString());
                 } else if (SapiException.CUS_0003.equals(se.getCode())) {
                    syncExc = new SyncException(SyncException.NOT_FOUND_URI_ERROR, se.toString());
                 } else if (SapiException.NO_CONNECTION.equals(se.getCode()) ||
                            SapiException.HTTP_400.equals(se.getCode()))
                 {
                     syncExc = new SyncException(SyncException.SERVER_CONNECTION_REQUEST_ERROR, se.toString());
                 } else if (SapiException.SAPI_EXCEPTION_CALL_NOT_SUPPORTED.equals(se.getCode())) {
                     syncExc = new SyncException(SyncException.NOT_FOUND_URI_ERROR, se.toString());
                 } else {
                     syncExc = new SyncException(SyncException.CLIENT_ERROR, se.toString());
                 }
                 sourceFailed(configAppSource, syncExc);
             } catch (Exception e) {
                 Log.error(TAG_LOG, "Config sync failed ", e);
                 SyncException se = new SyncException(SyncException.CLIENT_ERROR, e.toString());
                 sourceFailed(configAppSource, se);
             } finally {
                 syncEnded();
                 // Restore the original log level
                 configuration.restoreLogLevel();
                 controller.reapplyMiscConfiguration();
                 
                 failed = false;
             }
         }
     }
 
     private class ContinueSyncAction implements Runnable {
         private String serverUri;
         private String username;
         private String password;
         
         public ContinueSyncAction(String serverUri, String username, String password) {
             this.serverUri = serverUri;
             this.username = username;
             this.password = password;
         }
         
         public void run() {
             loginViaSapi(serverUri, username, password);
         }
     }
 
     private class AbortAfterNetwokUsageWarning implements Runnable {
         public void run() {
             networkUsageWarningShown = false;
         }
     }
 }
