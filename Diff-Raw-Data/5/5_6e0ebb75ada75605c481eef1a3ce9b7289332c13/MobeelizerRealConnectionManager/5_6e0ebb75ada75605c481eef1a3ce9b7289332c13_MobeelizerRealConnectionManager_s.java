 // 
 // MobeelizerRealConnectionManager.java
 // 
 // Copyright (C) 2012 Mobeelizer Ltd. All Rights Reserved.
 //
 // Mobeelizer SDK is free software; you can redistribute it and/or modify it 
 // under the terms of the GNU Affero General Public License as published by 
 // the Free Software Foundation; either version 3 of the License, or (at your
 // option) any later version.
 //
 // This program is distributed in the hope that it will be useful, but WITHOUT
 // ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 // FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 // for more details.
 //
 // You should have received a copy of the GNU Affero General Public License 
 // along with this program; if not, write to the Free Software Foundation, Inc., 
 // 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
 // 
 
 package com.mobeelizer.mobile.android;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.conn.params.ConnRouteParams;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.InputStreamBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.Proxy;
 import android.util.Config;
 import android.util.Log;
 
 import com.mobeelizer.mobile.android.api.MobeelizerLoginStatus;
 
 class MobeelizerRealConnectionManager implements MobeelizerConnectionManager {
 
     private static final String STATUS_ERROR = "ERROR";
 
     private static final String STATUS_OK = "OK";
 
     private static final String MESSAGE = "message";
 
     private static final String CONTENT = "content";
 
     private static final String STATUS = "status";
 
     private static final String TAG = "mobeelizer:mobeelizerrealconnectionmanager";
 
     private final MobeelizerApplication application;
 
     public MobeelizerRealConnectionManager(final MobeelizerApplication application) {
         this.application = application;
     }
 
     @Override
     public MobeelizerLoginResponse login() {
         boolean networkConnected = isNetworkAvailable();
 
         if (!networkConnected) {
             String[] roleAndInstanceGuid = getRoleAndInstanceGuidFromDatabase(application);
 
             if (roleAndInstanceGuid[0] == null) {
                 Log.e(TAG, "Login failure. Missing connection failure.");
                 return new MobeelizerLoginResponse(MobeelizerLoginStatus.MISSING_CONNECTION_FAILURE);
             } else {
                 Log.i(TAG, "Login '" + application.getUser() + "' from database successful.");
                 return new MobeelizerLoginResponse(MobeelizerLoginStatus.OK, roleAndInstanceGuid[1], roleAndInstanceGuid[0],
                         false);
             }
         }
 
         try {
             String response;
 
             try {
                 response = executeGetAndGetString("/authenticate", new String[0]);
             } catch (ConnectionException e) {
                 String[] roleAndInstanceGuid = getRoleAndInstanceGuidFromDatabase(application);
 
                 if (roleAndInstanceGuid[0] == null) {
                     return new MobeelizerLoginResponse(MobeelizerLoginStatus.CONNECTION_FAILURE);
                 } else {
                     return new MobeelizerLoginResponse(MobeelizerLoginStatus.OK, roleAndInstanceGuid[1], roleAndInstanceGuid[0],
                             false);
                 }
             }
 
             JSONObject json = new JSONObject(response);
 
             if (STATUS_OK.equals(json.getString(STATUS))) {
                 String role = json.getJSONObject(CONTENT).getString("role");
                 String instanceGuid = json.getJSONObject(CONTENT).getString("instanceGuid");
 
                 boolean initialSyncRequired = isInitialSyncRequired(application, instanceGuid);
 
                 setRoleAndInstanceGuidInDatabase(application, role, instanceGuid);
                 Log.i(TAG, "Login '" + application.getUser() + "' successful.");
                 return new MobeelizerLoginResponse(MobeelizerLoginStatus.OK, instanceGuid, role, initialSyncRequired);
             } else if (STATUS_ERROR.equals(json.getString(STATUS))
                     && "authenticationFailure".equals(json.getJSONObject(CONTENT).getString("messageCode"))) {
                 Log.e(TAG, "Login failure. Authentication error: " + response);
                 clearRoleAndInstanceGuidInDatabase(application);
                 return new MobeelizerLoginResponse(MobeelizerLoginStatus.AUTHENTICATION_FAILURE);
             } else {
                 clearRoleAndInstanceGuidInDatabase(application);
                 Log.e(TAG, "Login failure. Invalid response: " + response);
                 return new MobeelizerLoginResponse(MobeelizerLoginStatus.OTHER_FAILURE);
             }
         } catch (JSONException e) {
             Log.e(TAG, e.getMessage(), e);
             return new MobeelizerLoginResponse(MobeelizerLoginStatus.OTHER_FAILURE);
         }
     }
 
     private boolean isInitialSyncRequired(final MobeelizerApplication application, final String instanceGuid) {
         return application.getInternalDatabase().isInitialSyncRequired(application.getInstance(), instanceGuid,
                 application.getUser());
     }
 
     private String[] getRoleAndInstanceGuidFromDatabase(final MobeelizerApplication application) {
         return application.getInternalDatabase().getRoleAndInstanceGuid(application.getInstance(), application.getUser(),
                 application.getPassword());
     }
 
     private void setRoleAndInstanceGuidInDatabase(final MobeelizerApplication application, final String role,
             final String instanceGuid) {
         application.getInternalDatabase().setRoleAndInstanceGuid(application.getInstance(), application.getUser(),
                 application.getPassword(), role, instanceGuid);
     }
 
     private void clearRoleAndInstanceGuidInDatabase(final MobeelizerApplication application) {
         application.getInternalDatabase().clearRoleAndInstanceGuid(application.getInstance(), application.getUser());
     }
 
     @Override
     public boolean isNetworkAvailable() {
         ConnectivityManager connectivityManager = (ConnectivityManager) application.getContext().getSystemService(
                 Context.CONNECTIVITY_SERVICE);
 
         if (isConnected(connectivityManager)) {
             return true;
         }
 
         for (int i = 0; i < 10; i++) {
             if (isConnecting(connectivityManager)) {
                 // wait for connection
                 try {
                     Thread.sleep(500);
                 } catch (InterruptedException e) {
                     Log.w(TAG, e.getMessage(), e);
                     break;
                 }
 
                 if (isConnected(connectivityManager)) {
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     private boolean isConnecting(final ConnectivityManager connectivityManager) {
         return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting()
                 || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
     }
 
     private boolean isConnected(final ConnectivityManager connectivityManager) {
         return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()
                 || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected();
     }
 
     @Override
     public String sendSyncAllRequest() throws ConnectionException {
         try {
             String response = executePostAndGetString("/synchronizeAll",
                     new String[] { "version", application.getVersionDigest() }, new Object[0]);
 
             JSONObject json = new JSONObject(response);
 
             if (STATUS_OK.equals(json.getString(STATUS))) {
                 return json.getString(CONTENT);
             } else if (STATUS_ERROR.equals(json.getString(STATUS))) {
                 throw new ConnectionException("Sync failure with message: " + json.getJSONObject(CONTENT).getString(MESSAGE));
             } else {
                 throw new ConnectionException("Sync failure: " + json.getString(CONTENT));
             }
         } catch (JSONException e) {
             throw new ConnectionException(e.getMessage(), e);
         }
     }
 
     @Override
     public String sendSyncDiffRequest(final File outputFile) throws ConnectionException {
         try {
             String response = executePostAndGetString("/synchronize", new String[0], new Object[] { "file", outputFile });
 
             JSONObject json = new JSONObject(response);
 
             if (STATUS_OK.equals(json.getString(STATUS))) {
                 return json.getString(CONTENT);
             } else if (STATUS_ERROR.equals(json.getString(STATUS))) {
                 throw new ConnectionException("Sync failure with message: " + json.getJSONObject(CONTENT).getString(MESSAGE));
             } else {
                 throw new ConnectionException("Sync failure: " + json.getString(CONTENT));
             }
         } catch (JSONException e) {
             throw new ConnectionException(e.getMessage(), e);
         }
     }
 
     @Override
     public boolean waitUntilSyncRequestComplete(final String ticket) throws ConnectionException {
         try {
             for (int i = 0; i < 100; i++) {
                 String response = executeGetAndGetString("/checkStatus", new String[] { "ticket", ticket });
 
                 JSONObject json = new JSONObject(response);
 
                 if (STATUS_OK.equals(json.getString(STATUS))) {
                     String status = json.getJSONObject(CONTENT).getString(STATUS);
 
                     Log.i(TAG, "Check task status: " + status);
 
                     if ("REJECTED".toString().equals(status)) {
                         Log.i(TAG, "Check task status success: " + status + " with result "
                                 + json.getJSONObject(CONTENT).getString("result") + " and message '"
                                 + json.getJSONObject(CONTENT).getString(MESSAGE) + "'");
                         return false;
                     } else if ("FINISHED".toString().equals(status)) {
                         return true;
                     }
                 } else if (STATUS_ERROR.equals(json.getString(STATUS))) {
                     throw new ConnectionException("Check task status failure: " + json.getJSONObject(CONTENT).getString(MESSAGE));
                 } else {
                     throw new ConnectionException("Check task status failure: " + json.getString(CONTENT));
                 }
 
                 try {
                     Thread.sleep(5000);
                 } catch (InterruptedException e) {
                     throw new ConnectionException(e.getMessage(), e);
                 }
             }
         } catch (JSONException e) {
             throw new ConnectionException(e.getMessage(), e);
         }
 
         return false;
 
     }
 
     @Override
     public File getSyncData(final String ticket) throws ConnectionException {
         return executeGetAndGetFile("/data", new String[] { "ticket", ticket });
     }
 
     @Override
     public void confirmTask(final String ticket) throws ConnectionException {
         executePostAndGetString("/confirm", new String[] { "ticket", ticket }, new Object[0]);
     }
 
     private String executeGetAndGetString(final String path, final String[] params) throws ConnectionException {
         HttpGet request = new HttpGet(application.getUrl() + path + createQuery(params));
         setHeaders(request, true);
         return executeAndGetString(request);
     }
 
     private File executeGetAndGetFile(final String path, final String[] params) throws ConnectionException {
         HttpGet request = new HttpGet(application.getUrl() + path + createQuery(params));
         setHeaders(request, true);
         return executeAndGetFile(request);
     }
 
     private String executePostAndGetString(final String path, final String[] params, final Object[] files)
             throws ConnectionException {
         HttpPost request = null;
 
         if (files.length > 0) {
             request = new HttpPost(application.getUrl() + path);
 
             try {
                 // TODO V3 is it possible to make it without httpmime library?
                 MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                 for (int i = 0; i < files.length; i += 2) {
                     entity.addPart((String) files[0],
                             new InputStreamBody(new FileInputStream((File) files[1]), (String) files[0]));
                 }
                 for (int i = 0; i < params.length; i += 2) {
                    entity.addPart(params[0], new StringBody(params[1]));
                 }
                 request.setEntity(entity);
             } catch (IOException e) {
                 throw new ConnectionException(e.getMessage(), e);
             }
 
             setHeaders(request, false);
         } else {
             request = new HttpPost(application.getUrl() + path + createQuery(params));
             setHeaders(request, true);
         }
 
         return executeAndGetString(request);
     }
 
     private void setHeaders(final HttpRequestBase request, final boolean setJsonContentType) {
         if (setJsonContentType) {
             request.setHeader("content-type", "application/json");
         }
         request.setHeader("mas-vendor-name", application.getVendor());
         request.setHeader("mas-application-name", application.getApplication());
         request.setHeader("mas-application-instance-name", application.getInstance());
         request.setHeader("mas-definition-digest", application.getVersionDigest());
         request.setHeader("mas-device-name", application.getDevice());
         request.setHeader("mas-device-identifier", application.getDeviceIdentifier());
         request.setHeader("mas-user-name", application.getUser());
         request.setHeader("mas-user-password", application.getPassword());
         request.setHeader("mas-sdk-version", "android-sdk-" + Mobeelizer.VERSION);
     }
 
     private String createQuery(final String... params) {
         if (params.length > 0) {
             List<NameValuePair> qparams = new ArrayList<NameValuePair>();
             for (int i = 0; i < params.length; i += 2) {
                qparams.add(new BasicNameValuePair(params[0], params[1]));
             }
             return "?" + URLEncodedUtils.format(qparams, "UTF-8");
         } else {
             return "";
         }
     }
 
     private String executeAndGetString(final HttpRequestBase request) throws ConnectionException {
         HttpClient client = getHttpClient();
 
         InputStream is = null;
         Reader reader = null;
 
         if (!isNetworkAvailable()) {
             throw new ConnectionException("Cannot execute HTTP request, network connection not available");
         }
 
         setProxyIfNecessary(request);
 
         try {
             HttpResponse response = client.execute(request);
 
             if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                 HttpEntity entity = response.getEntity();
 
                 if (entity == null) {
                     throw new ConnectionException("Connection failure: entity not found.");
                 }
 
                 is = entity.getContent();
 
                 Writer writer = new StringWriter();
 
                 char[] buffer = new char[1024];
 
                 reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                 int n;
                 while ((n = reader.read(buffer)) != -1) {
                     writer.write(buffer, 0, n);
                 }
 
                 return writer.toString();
             } else {
                 throw new ConnectionException("Connection failure: " + response.getStatusLine().getStatusCode() + ".");
             }
         } catch (IOException e) {
             throw new ConnectionException(e.getMessage(), e);
         } finally {
             if (is != null) {
                 try {
                     is.close();
                 } catch (IOException e) {
                     if (Config.LOGD) {
                         Log.d(TAG, e.getMessage(), e);
                     }
                 }
             }
             if (reader != null) {
                 try {
                     reader.close();
                 } catch (IOException e) {
                     if (Config.LOGD) {
                         Log.d(TAG, e.getMessage(), e);
                     }
                 }
             }
             client.getConnectionManager().shutdown();
         }
     }
 
     private File executeAndGetFile(final HttpRequestBase request) throws ConnectionException {
         HttpClient client = getHttpClient();
 
         BufferedInputStream in = null;
         BufferedOutputStream out = null;
 
         if (!isNetworkAvailable()) {
             throw new ConnectionException("Cannot execute HTTP request, network connection not available");
         }
 
         setProxyIfNecessary(request);
 
         try {
             HttpResponse response = client.execute(request);
 
             if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                 HttpEntity entity = response.getEntity();
 
                 if (entity == null) {
                     throw new ConnectionException("Connection failure: entity not found.");
                 }
 
                 in = new BufferedInputStream(entity.getContent());
 
                 File file = File
                         .createTempFile("sync", "response", application.getContext().getDir("sync", Context.MODE_PRIVATE));
 
                 out = new BufferedOutputStream(new FileOutputStream(file));
 
                 byte[] buffer = new byte[4096];
                 int n = -1;
 
                 while ((n = in.read(buffer)) != -1) {
                     out.write(buffer, 0, n);
                 }
 
                 return file;
             } else {
                 throw new ConnectionException("Connection failure: " + response.getStatusLine().getStatusCode() + ".");
             }
         } catch (IOException e) {
             throw new ConnectionException(e.getMessage(), e);
         } finally {
             if (in != null) {
                 try {
                     in.close();
                 } catch (IOException e) {
                     if (Config.LOGD) {
                         Log.d(TAG, e.getMessage(), e);
                     }
                 }
             }
             if (out != null) {
                 try {
                     out.close();
                 } catch (IOException e) {
                     if (Config.LOGD) {
                         Log.d(TAG, e.getMessage(), e);
                     }
                 }
             }
             client.getConnectionManager().shutdown();
         }
     }
 
     private void setProxyIfNecessary(final HttpRequestBase request) {
         String proxyHost = Proxy.getHost(application.getContext());
         if (proxyHost == null) {
             return;
         }
 
         int proxyPort = Proxy.getPort(application.getContext());
         if (proxyPort < 0) {
             return;
         }
 
         HttpHost proxy = new HttpHost(proxyHost, proxyPort);
         ConnRouteParams.setDefaultProxy(request.getParams(), proxy);
     }
 
     public static class ConnectionException extends Exception {
 
         private static final long serialVersionUID = 8495472053163912742L;
 
         public ConnectionException(final String message) {
             super(message);
         }
 
         public ConnectionException(final String message, final Throwable throwable) {
             super(message, throwable);
         }
 
     }
 
     private HttpClient getHttpClient() {
         HttpParams parameters = new BasicHttpParams();
         HttpConnectionParams.setConnectionTimeout(parameters, 10000);
         HttpClient client = new DefaultHttpClient(parameters);
         return client;
     }
 
 }
