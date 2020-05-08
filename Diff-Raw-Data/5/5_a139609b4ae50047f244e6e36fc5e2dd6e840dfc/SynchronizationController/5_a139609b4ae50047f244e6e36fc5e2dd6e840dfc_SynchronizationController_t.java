 /*
  * Funambol is a mobile platform developed by Funambol, Inc.
  * Copyright (C) 2008 Funambol, Inc.
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
 
 import com.funambol.client.configuration.Configuration;
 import com.funambol.client.localization.Localization;
 import com.funambol.client.customization.Customization;
 import com.funambol.client.controller.Controller;
 import com.funambol.client.engine.SyncEngine;
 import com.funambol.client.engine.Poller;
 import com.funambol.client.engine.SyncEngineListener;
 import com.funambol.client.engine.AppSyncRequest;
 import com.funambol.client.source.AppSyncSource;
 import com.funambol.client.source.AppSyncSourceManager;
 import com.funambol.client.source.AppSyncSourceConfig;
 import com.funambol.client.push.SyncScheduler;
 import com.funambol.client.ui.Screen;
 import com.funambol.platform.NetworkStatus;
 import com.funambol.sync.SyncListener;
 import com.funambol.sync.SyncSource;
 import com.funambol.sync.SourceConfig;
 import com.funambol.sync.SyncException;
 import com.funambol.sapisync.sapi.SapiHandler;
 import com.funambol.org.json.me.JSONObject;
 import com.funambol.org.json.me.JSONArray;
 import com.funambol.util.StringUtil;
 import com.funambol.util.Log;
 
 /**
  * This interface includes all basic functions of a SynchronizationController
  * implementation that are currently shared between Android and BlackBerry
  * versions of SynchronizationController.
  */
 public class SynchronizationController implements SyncEngineListener {
 
     private static final String TAG_LOG = "SynchronizationController";
 
     public static final int REFRESH_FROM_SERVER = 0;
     public static final int REFRESH_TO_SERVER   = 1;
 
     public static final String MANUAL    = "manual";
     public static final String SCHEDULED = "scheduled";
     public static final String PUSH      = "push";
 
     protected Configuration configuration;
     protected Screen        screen;
     protected AppSyncSourceManager appSyncSourceManager;
     protected Controller    controller;
     protected Customization customization;
     protected Localization  localization;
     protected SyncEngine    engine;
     protected RequestHandler reqHandler;
     protected final AppSyncRequest appSyncRequestArr[] = new AppSyncRequest[1];
     protected SyncScheduler  syncScheduler;
 
     protected int   RETRY_POLL_TIME = 1;
 
     protected NetworkStatus networkStatus;
     
     private Vector localStorageFullSources = new Vector();
     private Vector serverQuotaFullSources = new Vector();
     private SyncRequest currentRequest;
 
 
     protected boolean    doCancel        = false;
 
     protected AppSyncSource currentSource = null;
 
     protected boolean    showTCPAlert;
 
     protected boolean    logConnectivityError;
 
     private int            scheduledAttempt = 0;
 
     private Poller         retryPoller = null;
 
 
     public SynchronizationController(Controller controller, Screen screen, NetworkStatus networkStatus) {
         this.controller = controller;
         this.screen     = screen;
         this.networkStatus = networkStatus;
 
         configuration = controller.getConfiguration();
         appSyncSourceManager = controller.getAppSyncSourceManager();
         localization = controller.getLocalization();
         customization = controller.getCustomization();
 
         initSyncScheduler();
     }
 
     public SynchronizationController(Controller controller, Customization customization,
                                      Configuration configuration, Localization localization,
                                      AppSyncSourceManager appSyncSourceManager, Screen screen,
                                      NetworkStatus networkStatus) {
         this.controller    = controller;
         this.customization = customization;
         this.configuration = configuration;
         this.localization  = localization;
         this.appSyncSourceManager = appSyncSourceManager;
         this.screen        = screen;
         this.networkStatus = networkStatus;
 
         initSyncScheduler();
     }
 
     // TEMP TODO FIXME: This is here until the HomeScreenController is merged
     // between android and BB
     SynchronizationController() {
     }
 
     /**
      * Triggers a synchronization for the given syncSources. The caller can
      * specify its type (manual, scheduled, push) to change the error handling
      * behavior
      *
      * @param syncType the caller type (SYNC_TYPE_MANUAL, SYNC_TYPE_SCHEDULED)
      * @param syncSources is a vector of AppSyncSource to be synchronized
      *
      */
     public synchronized void synchronize(String syncType, Vector syncSources) {
         synchronize(syncType, syncSources, 0);
     }
 
     /**
      * Schedules a synchronization for the given syncSources. The sync is
      * scheduled in "delay" milliseconds from now. The caller can
      * specify its type (manual, scheduled, push) to change the error handling
      * behavior
      *
      * @param syncType the caller type (SYNC_TYPE_MANUAL, SYNC_TYPE_SCHEDULED)
      * @param syncSources is a vector of AppSyncSource to be synced
      * @param delay the interval at which the sync shall be performed (relative
      *              to now)
      *
      */
     public synchronized void synchronize(String syncType, Vector syncSources, int delay) {
         synchronize(syncType, syncSources, delay, false);
     }
 
     /**
      * Perform a refresh for a set of sources and a given direction. The method
      * gets blocked until the sync terminates.
      *
      * @param mask the set of sources to sync
      * @param direction the refresh direction
      */
     public void refresh(int mask, int direction) {
 
         if (isSynchronizing()) {
             return;
         }
  
         Enumeration sources = appSyncSourceManager.getEnabledAndWorkingSources();
         Vector syncSources = new Vector();
         while(sources.hasMoreElements()) {
             AppSyncSource appSource = (AppSyncSource)sources.nextElement();
             if ((appSource.getId() & mask) != 0) {
                 syncSources.addElement(appSource);
             }
         }
 
        continueRefresh(syncSources, direction);
     }
 
     public synchronized void continueRefresh(Vector syncSources, int direction) {
         // A refresh is always a manual sync, force the sync type here
         currentRequest = new SyncRequest(MANUAL, syncSources, true, direction, 0, false);
         forceSynchronization(currentRequest);
     }
 
     /**
      * Schedules a synchronization for the given syncSources. The sync is
      * scheduled in "delay" milliseconds from now. The caller can
      * specify its type (manual, scheduled, push) to change the error handling
      * behavior.
      * The caller can also specify it the sync request is generated outside of
      * the application. In such a case the handling is special and the
      * synchronization is actually performed by the Sync Client process. This
      * calls notifies the SyncClient to schedule a sync at the given interval.
      * This is useful when syncs are triggered on external events, such as
      * modification of PIM (c2s push).
      *
      * @param syncType the caller type (SYNC_TYPE_MANUAL, SYNC_TYPE_SCHEDULED)
      * @param syncSources is a vector of AppSyncSource to be synced
      * @param delay the interval at which the sync shall be performed (relative
      *              to now)
      * @param fromOutside specifies if the request is generated outside of the
      * application
      */
     public synchronized void synchronize(String syncType, Vector syncSources,
                                          int delay, boolean fromOutside) {
 
         if (Log.isLoggable(Log.INFO)) {
             Log.info(TAG_LOG, "synchronize " + syncType);
         }
 
         if (isSynchronizing()) {
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, "A sync is already in progress");
             }
             return;
         }
 
         currentRequest = new SyncRequest(syncType, syncSources, false, 0, delay, fromOutside);
         forceSynchronization(currentRequest);
     }
 
     protected synchronized void forceSynchronization(SyncRequest syncRequest) {
         // Search if at least one of the selected sources has a warning on the
         // first sync
         Vector syncSources  = syncRequest.getSources();
         String syncType     = syncRequest.getType();
         int delay           = syncRequest.getDelay();
         boolean fromOutside = syncRequest.getFromOutside();
         boolean refresh     = syncRequest.getRefresh();
         int direction       = syncRequest.getDirection();
 
         Vector sourcesWithQuestion = new Vector();
         syncSources = applyBandwidthSaver(syncSources, sourcesWithQuestion, syncType);
 
         // We cannot ask the question if there is no app visible 
         if (screen == null && sourcesWithQuestion.size() > 0) {
             AppSyncSource[] dialogDependentSources = new AppSyncSource[sourcesWithQuestion.size()];
             sourcesWithQuestion.copyInto(dialogDependentSources);
         } else {
             if (sourcesWithQuestion.isEmpty()) {
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "Continue sync without prompts");
                 }
                 //No dialog is prompted for any sources: the sync can begin
                 continueSynchronizationAfterBandwithSaverDialog(syncRequest); 
             } else {
                 if (Log.isLoggable(Log.DEBUG)) {
                     Log.debug(TAG_LOG, "Continue sync displaying bandwith prompt");
                 }
                 ContinueAfterBandwidthSaverAction cabsa = new ContinueAfterBandwidthSaverAction(syncRequest);
                 InterruptSyncAction isa = new InterruptSyncAction();
 
 
                 DialogController dialControll = controller.getDialogController();
 
                 dialControll.askContinueCancelQuestion(screen,localization.getLanguage("dialog_no_wifi_availabale"), false,
                                                        cabsa, isa);
 
                 //The sync request is started when the user has finished to reply
                 //all the first sync request dialogs (the last sync request dialog)
                 //(calling the continueSynchronizationAfterDialogCheck method)
             }
         }
     }
 
 
     /**
      * Displays warnings in the proper form if the outcome of the latest sync requires so.
      * This method must be called when all synchronization operations are finished and the
      * user can be warned about problems that trigger a notification or a pop-up message
      * like those connected with storage limits (locally or in the cloud). 
      */
     protected void displayEndOfSyncWarnings() {
 
         if (localStorageFullSources != null && localStorageFullSources.size() > 0) {
             Log.debug(TAG_LOG, "Notifying storage limit warning");
             displayStorageLimitWarning(localStorageFullSources);
             localStorageFullSources.removeAllElements();
         }
         if (serverQuotaFullSources != null && serverQuotaFullSources.size() > 0) {
             Log.debug(TAG_LOG, "Notifying server quota warning");
             displayServerQuotaWarning(serverQuotaFullSources);
             serverQuotaFullSources.removeAllElements();
         }
 
     }
 
     protected void checkSourcesForStorageOrQuotaFullErrors(Vector sources) {
         for (int i = 0; i < sources.size(); i++) {
             AppSyncSource appSource = (AppSyncSource) sources.elementAt(i);
 
             switch (appSource.getConfig().getLastSyncStatus()) {
                 case SyncListener.LOCAL_CLIENT_FULL_ERROR:
                     // If one of the sources has risked to break the storage limit,
                     // a warning message can have to be displayed
                     localStorageFullSources.addElement(appSource);
                 break;
                 case SyncListener.SERVER_FULL_ERROR:
                     serverQuotaFullSources.addElement(appSource);
                 break;
             }
         }
     }
 
     /**
      * Display a background notification when max storage limit on local device
      * is reached. Children can override this method and implement
      * a foreground behavior
      * @param localStorageFullSources
      */
     protected void displayStorageLimitWarning(Vector localStorageFullSources) {
         controller.getNotificationController().showNotificationClientFull();
         localStorageFullSources.removeAllElements();
     }
 
     /**
      * Display a background notification when server quota is reached. Children
      * can override this method and implement a foreground behavior
      * @param serverQuotaFullSources
      */
     protected void displayServerQuotaWarning(Vector serverQuotaFullSources) {
         controller.getNotificationController().showNotificationServerFull();
         serverQuotaFullSources.removeAllElements();
     }
 
     protected SyncEngine createSyncEngine() {
         return new SyncEngine(customization, configuration, appSyncSourceManager, null);
     }
 
     
     /**
      * Applies the Bandwidth Saver by filtering out some sources or by populating
      * the Vector of sources that need to be synchronized only if the user accepts
      * to do so.
      * The synchronizations for sources that are filtered out are immediately set 
      * as pending and terminated.
      * This method has to be called before the synchronizations actually start.
      * 
      * @param syncSources all sources to be synchronized
      * @param sourcesWithQuestion an empty Vector
      * @param syncType the synchronization type
      * @return a sub-vector of sync sources containing only those sources that have
      *         passed the check
      */
     protected Vector applyBandwidthSaver(Vector syncSources, Vector sourcesWithQuestion, String syncType) {
 
         // This class cannot guarantee that these two members are not null,
         // therefore we check for their validity and do not filter if any of
         // these two is undefined
         if (configuration == null || networkStatus == null) {
             return syncSources;
         }
         
         if (configuration.getBandwidthSaverActivated() && !networkStatus.isWiFiConnected()) {
 
             if (Log.isLoggable(Log.TRACE)) {
                 Log.trace(TAG_LOG, "Bandwidth saver is enabled, wifi not connected and sync type " + syncType);
             }
             
             // If the syncType is automatic (i.e. not manual) and WiFi is not available, 
             // we shall skip all the sources which are to be synchronized only in WiFi 
             if (!MANUAL.equals(syncType)) {
                 Vector prefilteredSources = new Vector();
                 for (int i = 0; i < syncSources.size(); ++i) {
                     AppSyncSource appSource = (AppSyncSource)syncSources.elementAt(i);
                     // We need to check if the source requires to be sync'ed only in WiFi
                     // In v9 we excluded also sync sources with online quota full, but this
                     // behavior was modified in v10 
                     if (appSource.getBandwidthSaverUse()) {
                         // Skip this source because of the Bandwidth Saver.
                         // Remember that we have a pending sync now
                         AppSyncSourceConfig sourceConfig = appSource.getConfig();
                         sourceConfig.setPendingSync(syncType, sourceConfig.getSyncMode());
                         configuration.save();
                         // The sync for this source is terminated
                         if (Log.isLoggable(Log.INFO)) {
                             Log.info(TAG_LOG, "Ignoring sync for source: " + appSource.getName());
                         }
                         sourceEnded(appSource);
                     } else {
                         // It's OK
                         prefilteredSources.addElement(appSource);
                     }
                 }
                 syncSources = prefilteredSources;
                 
             } else {
                 // Now check if any source to be synchronized requires user confirmation
                 // because of the bandwidth saver
                 for(int y = 0; y < syncSources.size(); ++y) {
                     AppSyncSource appSource = (AppSyncSource)syncSources.elementAt(y);
                     if(appSource.getBandwidthSaverUse()) {
                         if (Log.isLoggable(Log.TRACE)) {
                             Log.trace(TAG_LOG, "Found a source which requires bandwidth saver question");
                         } 
                         sourcesWithQuestion.addElement(appSource);
                     }
                 }
             }
         }
         return syncSources;
     }
 
     protected void initSyncScheduler() {
         engine = createSyncEngine();
         syncScheduler = new SyncScheduler(engine);
         // The request handler is a daemon serving external requests
         reqHandler = new RequestHandler();
         reqHandler.start();
     }
 
 
     /**
      * Returns true iff a synchronization is in progress
      */
     public boolean isSynchronizing() {
         return engine.isSynchronizing();
     }
 
     /**
      * Returns the sync source currently being synchronized. If a sync is not
      * in progress, then null is returned. Please note that this method is not
      * completely equivalent to isSynchronizing. At the beginning of a sync,
      * isSynchronizing returns true, but getCurrentSource may return null until
      * the source is prepared for the synchronization.
      */
     public AppSyncSource getCurrentSource() {
         return engine.getCurrentSource();
     }
 
     /**
      * @return the current <code>SyncEngine</code> instance
      */
     public SyncEngine getSyncEngine() {
         return engine;
     }
 
 
 
     /**
      * Try to cancel the current sync. This works for cooperative sources that
      * check the synchronizationController status.
      */
     public void cancelSync() {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "Cancelling sync " + isSynchronizing() + " currentSource=" + currentSource);
         }
         setCancel(true);
 
         if (isSynchronizing() && currentSource != null) {
             UISyncSourceController uiSourceController = currentSource.getUISyncSourceController();
             if (uiSourceController != null) {
                 uiSourceController.startCancelling();
             }
             engine.cancelSync();
         }
     }
 
     public void syncEnded() {
 
         displayEndOfSyncWarnings();
       
         /* TODO FIXME!!!!!!!
         if(customization.enableUpdaterManager()){
             UpdaterManager upm = UpdaterManager.getInstance();
             upm.setController(controller);
             upm.check();
         }
         */
     }
 
     protected void showMessage(String msg) {
         controller.getDisplayManager().showMessage(screen, msg);
     }
 
     public void noCredentials() {
         showMessage(localization.getLanguage("message_login_required"));
     }
 
     public void noSources() {
         showMessage(localization.getLanguage("message_nothing_to_sync"));
     }
 
     public void noConnection() {
         showMessage(localization.getLanguage("message_radio_off"));
     }
 
     public void noSignal() {
         showMessage(localization.getLanguage("message_no_signal"));
     }
 
     public void setCancel(boolean value) {
         doCancel = value;
     }
 
     /**
      * Check if the current sync should be cancelled
      * 
      * @return
      */
     public boolean isCancelled() {
         return doCancel;
     }
 
     public void beginSync() {
         clearErrors();
         setCancel(false);
     }
 
 
     public boolean syncStarted(Vector sources) {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "syncStarted");
         }
         return true;
     }
 
     public void endSync(Vector sources, boolean hadErrors) {
         if (Log.isLoggable(Log.DEBUG)) {
             Log.debug(TAG_LOG, "endSync reached");
         }
         
         setCancel(false);
 
         // Disable the retry poller if not null
         if(retryPoller != null) {
             retryPoller.disable();
             retryPoller = null;
         }
 
         // If we had a CONNECTION BLOCKED BY THE USER error (user does not allow
         // any network configuration) then we show an error because the user
         // had already interacted with the app for this sync
         if (hadErrors && showTCPAlert) {
             controller.toForeground();
             if (Log.isLoggable(Log.DEBUG)) {
                 Log.debug(TAG_LOG, "showing tcp settings alert!");
             }
             showMessage(localization.getLanguage("message_enter_TCP_settings"));
         }
 
         // Re-checks
         checkSourcesForStorageOrQuotaFullErrors(sources);
 
         // We reset these errors because this sync is over (if we are retrying,
         // we must consider the new one with no errors)
         logConnectivityError = false;
         showTCPAlert = false;
     }
 
     public void sourceStarted(AppSyncSource appSource) {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "sourceStarted " + appSource.getName());
         }
         currentSource = appSource;
         UISyncSourceController sourceController = appSource.getUISyncSourceController();
         if (sourceController != null) {
             sourceController.setSelected(true, false);
         }
         
         if (currentSource.getSyncSource().getConfig().getSyncMode() == SyncSource.FULL_DOWNLOAD) {
             refreshClientData(appSource, sourceController);
         }
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "sourceStarted " + currentSource);
         }
     }
 
     public void sourceEnded(AppSyncSource appSource) {
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "sourceEnded " + appSource.getName());
         }
         currentSource = null;
 
         // Set synced source
         appSource.getConfig().setSynced(true);
         
         saveSourceConfig(appSource);
         
         UISyncSourceController sourceController = appSource.getUISyncSourceController();
         if (sourceController != null) {
             sourceController.setSelected(false, false);
         }
     }
 
     public void sourceFailed(AppSyncSource appSource, SyncException e) {
 
         if (Log.isLoggable(Log.TRACE)) {
             Log.trace(TAG_LOG, "sourceFailed");
         }
 
         int code = e.getCode();
         if (   code == SyncException.READ_SERVER_RESPONSE_ERROR
             || code == SyncException.WRITE_SERVER_REQUEST_ERROR
             || code == SyncException.CONN_NOT_FOUND) {
 
             logConnectivityError = true;
         } else if (code == SyncException.CONNECTION_BLOCKED_BY_USER) {
             showTCPAlert = true;
         } else if (code == SyncException.PAYMENT_REQUIRED) {
             // In order to sync the user shall accept a payment
             // Compute the remaining sources
             Vector nextSources = new Vector();
             Vector originalSources = currentRequest.getSources();
             boolean include = false;
             for(int i=0;i<originalSources.size();++i) {
                 AppSyncSource ss = (AppSyncSource)originalSources.elementAt(i);
                 if (ss.getId() == appSource.getId()) {
                     include = true;
                 }
                 if (include) {
                     nextSources.addElement(ss);
                 }
             }
             askForPayment(nextSources);
         }
     }
 
     public String getRemoteUri(AppSyncSource appSource) {
         SourceConfig config = appSource.getSyncSource().getConfig();
         return config.getRemoteUri();
     }
 
     public void serverOperationFailed() {
         showMessage(localization.getLanguage("message_not_send_to_server"));
     }
 
     public Controller getController() {
         return controller;
     }
 
     public void clearErrors() {
         showTCPAlert = false;
         logConnectivityError = false;
     }
 
     protected void setScreen(Screen screen) {
         this.screen = screen;
     }
 
     private void saveSourceConfig(AppSyncSource appSource) {
         appSource.getConfig().saveSourceSyncConfig();
         appSource.getConfig().commit();
     }
 
     private boolean retry(Vector sources) {
 
         boolean willRetry = false;
 
         if (retryPoller != null) {
             retryPoller.disable();
         }
 
         if (scheduledAttempt < 3) {
             scheduledAttempt++;
             Log.error(TAG_LOG, "Scheduled sync: Connection attempt failed. " + "Try again in "
                     + RETRY_POLL_TIME + " minutes");
 
             retryPoller = new Poller(this, RETRY_POLL_TIME, true, false);
             retryPoller.start();
             willRetry = true;
         } else {
             retryPoller = null;
             scheduledAttempt = 0;
         }
         return willRetry;
     }
 
     protected void askForPayment(Vector nextSources) {
 
         // On BB dialogs are blocking, here we really need to create a thread so
         // that the current sync terminates and a new one is restarted afterward
         // (otherwise events get messed up)
         // Creating the thread on all platforms is a safe solution.
         PaymentThread pt = new PaymentThread(nextSources, currentRequest);
         pt.start();
     }
 
     protected class PaymentYesAction implements Runnable {
         private Vector remainingSources;
         private SyncRequest originalRequest;
 
         public PaymentYesAction(Vector remainingSources, SyncRequest originalRequest) {
             this.remainingSources = remainingSources;
             this.originalRequest = originalRequest;
         }
 
         public void run() {
             String sapiUrl = StringUtil.extractAddressFromUrl(configuration.getSyncUrl());
             SapiHandler sapiHandler = new SapiHandler(sapiUrl, configuration.getUsername(),
                                                       configuration.getPassword());
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, "User accepted payment request, continue sync");
             }
  
             try {
                 JSONObject req = new JSONObject();
                 JSONArray  sources = new JSONArray();
                 Enumeration workingSources = appSyncSourceManager.getWorkingSources();
                 while(workingSources.hasMoreElements()) {
                     AppSyncSource appSource = (AppSyncSource)workingSources.nextElement();
                     JSONObject restoreSource = new JSONObject();
                     restoreSource.put("service","restore");
                     restoreSource.put("resource",appSource.getSyncSource().getConfig().getRemoteUri());
                     sources.put(restoreSource);
                 }
                 req.put("data", sources);
 
                 sapiHandler.query("system/payment","buy",null,null,req);
 
                 // Restart the sync for the given sources
                 continueSyncAfterNetworkUsage(originalRequest.getType(), remainingSources,
                                               originalRequest.getRefresh(), originalRequest.getDirection(),
                                               originalRequest.getDelay(), originalRequest.getFromOutside());
             } catch (Exception e) {
                 Log.error(TAG_LOG, "Cannot perform payment", e);
                 // TODO FIXME: show an error to the user
             }
         }
     }
 
     protected class PaymentNoAction implements Runnable {
         public void run() {
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, "User did not accept payment request, stop sync");
             }
         }
     }
 
     protected class PaymentThread extends Thread {
         private Vector sources;
         private SyncRequest syncRequest;
 
         public PaymentThread(Vector sources, SyncRequest syncRequest) {
             this.sources = sources;
             this.syncRequest = syncRequest;
         }
 
         public void run() {
             DialogController dc = controller.getDialogController();
             String syncType = com.funambol.client.controller.SynchronizationController.MANUAL;
             PaymentYesAction yesAction = new PaymentYesAction(sources, syncRequest);
             PaymentNoAction  noAction  = new PaymentNoAction();
             // TODO FIXME: use a localized message
             dc.askYesNoQuestion(screen, "A payment is required", false, yesAction, noAction);
         }
     }
 
     protected void continueSyncAfterNetworkUsage(String syncType, Vector syncSources, boolean refresh,
                                                  int direction, int delay, boolean fromOutside)
     {
         // We register as listeners for the sync
         engine.setListener(this);
 
         int sourceSyncType = 0;
         AppSyncRequest appSyncRequest = new AppSyncRequest(null, delay);
         Enumeration sources = syncSources.elements();
         while(sources.hasMoreElements()) {
             AppSyncSource appSource = (AppSyncSource) sources.nextElement();
             SyncSource source = appSource.getSyncSource();
 
             if (refresh) {
                 int syncMode = appSource.prepareRefresh(direction);
                 source.getConfig().setSyncMode(syncMode);
             } else {
                 sourceSyncType = appSource.getConfig().getSyncMode();
                 // If this source has no config set, then we cannot force a sync
                 // mode, but this is a logical error
                 if (source.getConfig() != null) {
                     source.getConfig().setSyncMode(sourceSyncType);
                 } else {
                     Log.error(TAG_LOG, "Source has no config, cannot set sync mode");
                 }
             }
 
             // Clear any pending sync information here, because we are about to
             // start a sync
             AppSyncSourceConfig sourceConfig = appSource.getConfig();
             sourceConfig.setPendingSync("", -1);
             configuration.save();
             // Add the request for this synchronization
             appSyncRequest.addRequestContent(appSource);
         }
 
         if (fromOutside) {
             synchronized(appSyncRequestArr) {
                 appSyncRequestArr[0] = appSyncRequest;
                 appSyncRequestArr.notify();
             }
         } else {
             syncScheduler.addRequest(appSyncRequest);
         }
 
     }
 
     protected synchronized void
     continueSynchronizationAfterBandwithSaverDialog(SyncRequest syncRequest) {
         // If no sources left, we simply return and do not update/change
         // anything
         if (syncRequest.getSources().isEmpty()) {
             syncEnded();
             return;
         }
 
         // Ask network usage permission if required
         ContinueSyncAction csa = new ContinueSyncAction(syncRequest);
         NetworkUsageWarningController nuwc = new NetworkUsageWarningController(screen, controller, csa);
         nuwc.askUserNetworkUsageConfirmation();
     }
 
 
     protected String getListOfSourceNames(Enumeration sourceNameList) {
         StringBuffer sourceNames = new StringBuffer();
 
         int x = 0;
         AppSyncSource appSource = (AppSyncSource)sourceNameList.nextElement();
 
         while(appSource != null) {
 
             String name = appSource.getName();
             appSource = (AppSyncSource)sourceNameList.nextElement();
 
             if (x > 0) {
                 sourceNames.append(", ");
                 if (appSource == null) {
                     sourceNames.append(localization.getLanguage("dialog_and").toLowerCase());
                 }
             }
 
             sourceNames.append(name);
         }
 
         return sourceNames.toString();
     }
 
     private class RequestHandler extends Thread {
 
         private boolean stop = false;
 
         public RequestHandler() {
         }
 
         public void run() {
             if (Log.isLoggable(Log.INFO)) {
                 Log.info(TAG_LOG, "Starting request handler");
             }
             while (!stop) {
                 try {
                     synchronized (appSyncRequestArr) {
                         appSyncRequestArr.wait();
                         syncScheduler.addRequest(appSyncRequestArr[0]);
                     }
                 } catch (Exception e) {
                     // All handled exceptions are trapped below, this is just a
                     // safety net for runtime exception because we don't want
                     // this thread to die.
                     Log.error(TAG_LOG, "Exception while performing a programmed sync " + e.toString());
                 }
             }
         }
     }
 
     protected class ContinueRefreshAction implements Runnable {
         private Vector sources;
         private int direction;
 
         public ContinueRefreshAction(Vector sources, int direction) {
             this.sources   = sources;
             this.direction = direction;
         }
 
         public void run() {
             continueRefresh(sources, direction);
         }
     }
 
     protected class ContinueSyncAction implements Runnable {
         private SyncRequest request;
 
         public ContinueSyncAction(SyncRequest syncRequest) {
             this.request = syncRequest;
         }
 
         public void run() {
             continueSyncAfterNetworkUsage(request.getType(), request.getSources(),
                                           request.getRefresh(), request.getDirection(),
                                           request.getDelay(), request.getFromOutside());
         }
     }
 
     protected class InterruptSyncAction implements Runnable {
 
         public void run() {
             syncEnded();
         }
     }
 
     protected class ContinueAfterBandwidthSaverAction implements Runnable {
         private SyncRequest request;
 
         public ContinueAfterBandwidthSaverAction(SyncRequest syncRequest) {
             this.request = syncRequest;
         }
 
         public void run() {
             continueSynchronizationAfterBandwithSaverDialog(request);
         }
     }
 
     protected class SyncRequest {
         private String syncType;
         private Vector sources;
         private boolean refresh;
         private int direction;
         private int delay;
         private boolean fromOutside;
 
         public SyncRequest(String syncType, Vector syncSources, boolean refresh, int direction,
                            int delay, boolean fromOutside)
         {
             this.syncType = syncType;
             this.sources = syncSources;
             this.refresh = refresh;
             this.direction = direction;
             this.delay = delay;
             this.fromOutside = fromOutside;
         }
 
         public String getType() {
             return syncType;
         }
 
         public Vector getSources() {
             return sources;
         }
 
         public boolean getRefresh() {
             return refresh;
         }
 
         public int getDirection() {
             return direction;
         }
 
         public int getDelay() {
             return delay;
         }
 
         public boolean getFromOutside() {
             return fromOutside;
         }
     }
 
     private void refreshClientData(AppSyncSource appSource, UISyncSourceController controller) {
     }
 }
