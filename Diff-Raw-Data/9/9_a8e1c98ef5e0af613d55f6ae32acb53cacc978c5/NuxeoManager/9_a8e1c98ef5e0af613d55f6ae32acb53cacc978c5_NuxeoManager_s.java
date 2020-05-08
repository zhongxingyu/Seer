 /* *********************************************************************** *
  * project: org.gots.*
  *                                                                         *
  * *********************************************************************** *
  *                                                                         *
  * copyright       : (C) 2013 by the members listed in the COPYING,        *
  *                   LICENSE and WARRANTY file.                            *
  * email           : contact at gardening-manager dot com                  *
  *                                                                         *
  * *********************************************************************** *
  *                                                                         *
  *   This program is free software; you can redistribute it and/or modify  *
  *   it under the terms of the GNU General Public License as published by  *
  *   the Free Software Foundation; either version 2 of the License, or     *
  *   (at your option) any later version.                                   *
  *   http://www.gnu.org/licenses/gpl-2.0.html                              *
  *   See also COPYING, LICENSE and WARRANTY file                           *
  *   Contributors:                                                         *
  *                - jcarsique                                                *
  *                                                                         *
  * *********************************************************************** */
 package org.gots.nuxeo;
 
 import org.gots.preferences.GotsPreferences;
 import org.gots.utils.NotConfiguredException;
 import org.nuxeo.android.config.NuxeoServerConfig;
 import org.nuxeo.android.context.NuxeoContext;
 import org.nuxeo.android.context.NuxeoContextFactory;
 import org.nuxeo.ecm.automation.client.android.AndroidAutomationClient;
 import org.nuxeo.ecm.automation.client.jaxrs.RemoteException;
 import org.nuxeo.ecm.automation.client.jaxrs.Session;
 import org.nuxeo.ecm.automation.client.jaxrs.impl.NotAvailableOffline;
 import org.nuxeo.ecm.automation.client.jaxrs.spi.auth.TokenRequestInterceptor;
 
 import android.content.Context;
 import android.util.Log;
 
 /**
  * @author jcarsique
  *
  */
 public class NuxeoManager {
     private static final String TAG = "NuxeoManager";
 
     private static NuxeoManager instance;
 
     private static Exception firstCall;
 
     private GotsPreferences gotsPrefs;
 
     private NuxeoContext nuxeoContext;
 
     private NuxeoServerConfig nxConfig;
 
     private AndroidAutomationClient nuxeoClient;
 
     private boolean initDone = false;
 
     private NuxeoManager() {
     }
 
     /**
      * After first call, {@link #initIfNew(Context)} must be called else a
      * {@link NotConfiguredException} will be thrown
      * on the second call attempt.
      */
     public static synchronized NuxeoManager getInstance() {
         if (instance == null) {
             instance = new NuxeoManager();
             firstCall = new Exception();
         } else if (!instance.initDone) {
             throw new NotConfiguredException(firstCall);
         }
         return instance;
     }
 
     /**
      * If it was already called once, the method returns without any change.
      */
     public synchronized void initIfNew(Context context) {
         if (initDone) {
             return;
         }
 
         gotsPrefs = GotsPreferences.getInstance();
         gotsPrefs.initIfNew(context);
 
         nxConfig = new NuxeoServerConfig(context);
         nxConfig.setSharedPrefs(gotsPrefs.getSharedPrefs());
         nxConfig.setCacheKey(NuxeoServerConfig.PREF_SERVER_TOKEN);
         nuxeoContext = NuxeoContextFactory.getNuxeoContext(context, nxConfig);
         initDone = true;
         Log.d(TAG, "getSession with: "
                 + nxConfig.getServerBaseUrl()
                 + " login="
                 + nxConfig.getLogin()
                 + " password="
                 + (GotsPreferences.ISDEVELOPMENT ? nxConfig.getPassword()
                         : "******"));
     }
 
     public AndroidAutomationClient getNuxeoClient() {
         if (nuxeoClient == null || nuxeoClient.isShutdown()) {
             nuxeoClient = nuxeoContext.getNuxeoClient();
             String myToken = gotsPrefs.getToken();
             String myLogin = gotsPrefs.getNuxeoLogin();
             String myDeviceId = gotsPrefs.getDeviceId();
             String myApp = gotsPrefs.getGardeningManagerAppname();
             nuxeoClient.setRequestInterceptor(new TokenRequestInterceptor(
                     myApp, myToken, myLogin, myDeviceId));
             Log.d(TAG, "Got new nuxeoClient " + nuxeoClient);
         }
         return nuxeoClient;
     }
 
     /**
      * Android 11+: raises a {@link android.os.NetworkOnMainThreadException} if
      * called from the main thread and tries to
      * perform a network call (Nuxeo server)
      */
     public Session getSession() throws NotAvailableOffline, RemoteException {
         return getNuxeoClient().getSession();
     }
 
     public void shutdown() {
         try {
             nuxeoContext.shutdown();
         } catch (Exception e) {
             Log.e(TAG, "nuxeoContext.shutdown() " + e.getMessage(), e);
         }
     }
 }
