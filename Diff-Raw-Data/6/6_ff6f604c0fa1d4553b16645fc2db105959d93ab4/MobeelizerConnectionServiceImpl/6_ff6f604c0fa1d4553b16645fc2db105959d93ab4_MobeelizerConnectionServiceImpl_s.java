 // 
 // MobeelizerConnectionServiceImpl.java
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
 
 package com.mobeelizer.java.connection;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.InputStreamBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.mobeelizer.java.api.MobeelizerMode;
 import com.mobeelizer.java.api.MobeelizerOperationError;
 import com.mobeelizer.java.api.MobeelizerOperationStatus;
 import com.mobeelizer.java.api.user.MobeelizerUser;
 
 public class MobeelizerConnectionServiceImpl implements MobeelizerConnectionService {
 
     private static final String DEFAULT_TEST_URL = "https://cloud.mobeelizer.com/sync/v2";
 
     private static final String DEFAULT_PRODUCTION_URL = "https://cloud.mobeelizer.com/sync/v2";
 
     private final MobeelizerConnectionServiceDelegate delegate;
 
     public MobeelizerConnectionServiceImpl(final MobeelizerConnectionServiceDelegate delegate) {
         this.delegate = delegate;
     }
 
     @Override
     public MobeelizerAuthenticateResponse authenticate(final String login, final String password) {
         return authenticate(login, password, null, null);
     }
 
     @Override
     public MobeelizerAuthenticateResponse authenticate(final String login, final String password, final String token) {
         return authenticate(login, password, "android", token);
     }
 
     @Override
     public MobeelizerAuthenticateResponse authenticate(final String login, final String password, final String deviceType,
             final String deviceToken) {
         try {
 
             String url;
             if (deviceType != null && deviceToken != null) {
                 url = getUrl("/authenticate", "deviceToken", deviceToken, "deviceType", deviceType);
             } else {
                 url = getUrl("/authenticate");
             }
             HttpGet request = new HttpGet(url);
 
             setHeaders(request, true, false);
 
             request.setHeader("mas-user-name", login);
             request.setHeader("mas-user-password", password);
 
            JSONObject json = new JSONObject(executeAndGetContent(request));
 
             return new MobeelizerAuthenticateResponseImpl(json.getString("role"), json.getString("instanceGuid"), null);
         } catch (JSONException e) {
             return new MobeelizerAuthenticateResponseImpl(null, null, MobeelizerOperationError.ioError(new IOException(e
                     .getMessage())));
         }
     }
 
     @Override
     public MobeelizerOperationStatus<List<String>> getGroups() {
         try {
             MobeelizerOperationStatus<String> response = executeGetAndGetContent("/client/user/groups");
             if (response.getError() != null) {
                 return new MobeelizerOperationStatus<List<String>>(response.getError());
             }
             JSONArray json = new JSONArray(response.getContent());
 
             List<String> groups = new ArrayList<String>();
 
             for (int i = 0; i < json.length(); i++) {
                 groups.add(json.getJSONObject(i).getString("name"));
             }
             return new MobeelizerOperationStatus<List<String>>(groups);
         } catch (JSONException e) {
             return new MobeelizerOperationStatus<List<String>>(MobeelizerOperationError.other(e));
         }
     }
 
     @Override
     public MobeelizerOperationStatus<List<MobeelizerUser>> getUsers() {
         try {
             MobeelizerOperationStatus<String> response = executeGetAndGetContent("/client/user/list");
             if (response.getError() != null) {
                 return new MobeelizerOperationStatus<List<MobeelizerUser>>(response.getError());
             }
 
             JSONArray json = new JSONArray(response.getContent());
 
             List<MobeelizerUser> users = new ArrayList<MobeelizerUser>(json.length());
 
             for (int i = 0; i < json.length(); i++) {
                 users.add(jsonObjectToUser(json.getJSONObject(i)));
             }
 
             return new MobeelizerOperationStatus<List<MobeelizerUser>>(users);
         } catch (JSONException e) {
             return new MobeelizerOperationStatus<List<MobeelizerUser>>(MobeelizerOperationError.other(e));
         }
     }
 
     @Override
     public MobeelizerOperationStatus<MobeelizerUser> getUser(final String login) {
         try {
             MobeelizerOperationStatus<String> response = executeGetAndGetContent("/client/user/get", "login", login);
             if (response.getError() != null) {
                 return new MobeelizerOperationStatus<MobeelizerUser>(response.getError());
             }
             JSONObject json = new JSONObject(response.getContent());
             return new MobeelizerOperationStatus<MobeelizerUser>(jsonObjectToUser(json));
         } catch (JSONException e) {
             return new MobeelizerOperationStatus<MobeelizerUser>(MobeelizerOperationError.other(e));
         }
     }
 
     @Override
     public MobeelizerOperationError createUser(final MobeelizerUser user) {
         try {
             return executePostAndGetContent("/client/user/create", userToJsonObject(user)).getError();
         } catch (JSONException e) {
             return MobeelizerOperationError.other(e);
         }
     }
 
     @Override
     public MobeelizerOperationError updateUser(final MobeelizerUser user) {
         try {
             return executePostAndGetContent("/client/user/update", userToJsonObject(user)).getError();
         } catch (JSONException e) {
             return MobeelizerOperationError.other(e);
         }
     }
 
     @Override
     public MobeelizerOperationError deleteUser(final String login) {
         return executePostAndGetContent("/client/user/delete", "login", login).getError();
     }
 
     @Override
     public MobeelizerOperationStatus<String> sendSyncAllRequest() {
         return executePostAndGetContent("/synchronizeAll");
     }
 
     @Override
     public MobeelizerOperationStatus<String> sendSyncDiffRequest(final File outputFile) {
         return executePostAndGetContent("/synchronize", "file", outputFile);
     }
 
     @Override
     public MobeelizerOperationError waitUntilSyncRequestComplete(final String ticket) {
         try {
             for (int i = 0; i < 100; i++) {
                 MobeelizerOperationStatus<String> checkStatusResult = executeGetAndGetContent("/checkStatus", new String[] {
                         "ticket", ticket });
                 if (checkStatusResult.getError() != null) {
                     return checkStatusResult.getError();
                 }
 
                 JSONObject json = new JSONObject(checkStatusResult.getContent());
 
                 String status = json.getString("status");
 
                 delegate.logInfo("Check task status: " + status);
 
                 if ("REJECTED".toString().equals(status)) {
 
                     String message = "Check task status success: " + status + " with result " + json.getString("result")
                             + " and message '" + json.getString("message") + "'";
                     delegate.logInfo(message);
                     return MobeelizerOperationError.syncRejected(json.getString("result"), json.getString("message"));
                 } else if ("FINISHED".toString().equals(status)) {
                     return null;
                 }
 
                 try {
                     Thread.sleep(5000);
                 } catch (InterruptedException e) {
                     return MobeelizerOperationError.other(e.getMessage());
                 }
             }
         } catch (JSONException e) {
             return MobeelizerOperationError.ioError(new IOException(e.getMessage()));
         }
         return null;
     }
 
     @Override
     public File getSyncData(final String ticket) throws IOException {
         return executeGetForFile("/data", "ticket", ticket);
     }
 
     @Override
     public MobeelizerOperationError confirmTask(final String ticket) {
         return executePostAndGetContent("/confirm", "ticket", ticket).getError();
     }
 
     @Override
     public MobeelizerOperationError registerForRemoteNotifications(final String token) {
         delegate.logInfo("Try to register for remote notifications with token: " + token);
         return executePostAndGetContent("/registerPushToken", "deviceToken", token, "deviceType", "android").getError();
     }
 
     @Override
     public MobeelizerOperationError unregisterForRemoteNotifications(final String token) {
         delegate.logInfo("Try to unregister for remote notifications with token: " + token);
         return executePostAndGetContent("/unregisterPushToken", "deviceToken", token, "deviceType", "android").getError();
     }
 
     @Override
     public MobeelizerOperationError sendRemoteNotification(final String device, final String group, final List<String> users,
             final Map<String, String> notification) {
         try {
             JSONObject object = new JSONObject();
             StringBuilder logBuilder = new StringBuilder();
             logBuilder.append("try to send remote notification ").append(notification).append(" to");
             if (device != null) {
                 object.put("device", device);
                 logBuilder.append(" device: ").append(device);
             }
             if (group != null) {
                 object.put("group", group);
                 logBuilder.append(" group: ").append(group);
             }
             if (users != null) {
                 object.put("users", new JSONArray(users));
                 logBuilder.append(" users: ").append(users);
             }
             if (device == null && group == null && users == null) {
                 logBuilder.append(" everyone");
             }
             object.put("notification", new JSONObject(notification));
             delegate.logInfo(logBuilder.toString());
             return executePostAndGetContent("/push", object).getError();
         } catch (JSONException e) {
             return MobeelizerOperationError.other(e.getMessage());
         }
     }
 
     private MobeelizerUser jsonObjectToUser(final JSONObject json) throws JSONException {
         MobeelizerUser user = new MobeelizerUser();
         user.setLogin(json.getString("login"));
         user.setGroup(json.getString("group"));
         user.setMail(json.getString("mail"));
         user.setAdmin("true".equals(json.getString("admin")));
         return user;
     }
 
     private JSONObject userToJsonObject(final MobeelizerUser user) throws JSONException {
         JSONObject json = new JSONObject();
         json.put("login", user.getLogin());
         json.put("group", user.getGroup());
         json.put("password", user.getPassword());
         json.put("mail", user.getMail());
         json.put("admin", user.isAdmin() ? "true" : "false");
         return json;
     }
 
     private MobeelizerOperationStatus<String> executeGetAndGetContent(final String path, final String... params) {
         HttpGet request = new HttpGet(getUrl(path, params));
         setHeaders(request, true, true);
         return executeAndGetContent(request);
     }
 
     private MobeelizerOperationStatus<String> executePostAndGetContent(final String path, final String... params) {
         HttpPost request = new HttpPost(getUrl(path, params));
         setHeaders(request, true, true);
         return executeAndGetContent(request);
     }
 
     private File executeGetForFile(final String path, final String... params) throws IOException {
         HttpGet request = new HttpGet(getUrl(path, params));
         setHeaders(request, true, true);
         return executeAndGetFile(request);
     }
 
     private MobeelizerOperationStatus<String> executePostAndGetContent(final String path, final JSONObject body) {
         HttpPost request = new HttpPost(getUrl(path, new String[0]));
         try {
             request.setEntity(new StringEntity(body.toString(), "UTF-8"));
         } catch (UnsupportedEncodingException e) {
             MobeelizerOperationError.other(e);
         }
 
         setHeaders(request, true, true);
 
         return executeAndGetContent(request);
     }
 
     private MobeelizerOperationStatus<String> executePostAndGetContent(final String path, final String name, final File file) {
         HttpPost request = new HttpPost(getUrl(path, new String[0]));
 
         MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
         try {
             entity.addPart(name, new InputStreamBody(new FileInputStream(file), name));
         } catch (FileNotFoundException e) {
             MobeelizerOperationError.other(e);
         }
         request.setEntity(entity);
 
         setHeaders(request, false, true);
 
         return executeAndGetContent(request);
     }
 
     private void setHeaders(final HttpRequestBase request, final boolean setJsonContentType, final boolean setUserPassword) {
         if (setJsonContentType) {
             request.setHeader("content-type", "application/json");
         }
         request.setHeader("mas-vendor-name", delegate.getVendor());
         request.setHeader("mas-application-name", delegate.getApplication());
         request.setHeader("mas-application-instance-name", delegate.getInstance());
         request.setHeader("mas-definition-digest", delegate.getVersionDigest());
         request.setHeader("mas-device-name", delegate.getDevice());
         request.setHeader("mas-device-identifier", delegate.getDeviceIdentifier());
         if (setUserPassword) {
             request.setHeader("mas-user-name", delegate.getUser());
             request.setHeader("mas-user-password", delegate.getPassword());
         }
         request.setHeader("mas-sdk-version", delegate.getSdkVersion());
     }
 
     private String getUrl(final String path, final String... params) {
         return getUrl() + path + createQuery(params);
     }
 
     private String getUrl() {
         if (delegate.getUrl() != null) {
             return delegate.getUrl();
         } else if (delegate.getMode().equals(MobeelizerMode.PRODUCTION)) {
             return DEFAULT_PRODUCTION_URL;
         } else {
             return DEFAULT_TEST_URL;
         }
     }
 
     private String createQuery(final String[] params) {
         if (params.length > 0) {
             List<NameValuePair> qparams = new ArrayList<NameValuePair>();
             for (int i = 0; i < params.length; i += 2) {
                 qparams.add(new BasicNameValuePair(params[i], params[i + 1]));
             }
             return "?" + URLEncodedUtils.format(qparams, "UTF-8");
         } else {
             return "";
         }
     }
 
     private HttpClient httpClient() {
         HttpParams params = new BasicHttpParams();
         HttpConnectionParams.setConnectionTimeout(params, 60000);
         HttpConnectionParams.setSoTimeout(params, 60000);
         return new DefaultHttpClient(params);
     }
 
     private MobeelizerOperationStatus<String> executeAndGetContent(final HttpRequestBase request) {
         HttpClient client = httpClient();
 
         InputStream is = null;
         Reader reader = null;
 
         if (!delegate.isNetworkAvailable()) {
             return new MobeelizerOperationStatus<String>(MobeelizerOperationError.missingConnectionError());
         }
 
         delegate.setProxyIfNecessary(request);
 
         try {
             HttpResponse response = client.execute(request);
 
             int status = response.getStatusLine().getStatusCode();
 
             HttpEntity entity = response.getEntity();
             if (entity == null) {
                 return new MobeelizerOperationStatus<String>(MobeelizerOperationError.connectionError());
             }
             is = entity.getContent();
             Writer writer = new StringWriter();
             char[] buffer = new char[1024];
             reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
             int n;
             while ((n = reader.read(buffer)) != -1) {
                 writer.write(buffer, 0, n);
             }
             String content = writer.toString().trim();
 
             if (status == HttpStatus.SC_OK) {
                 return new MobeelizerOperationStatus<String>(content);
             } else if (status == HttpStatus.SC_INTERNAL_SERVER_ERROR && !content.isEmpty()) {
                 JSONObject json = new JSONObject(content);
                 return new MobeelizerOperationStatus<String>(MobeelizerOperationError.serverError(json));
             } else {
                 return new MobeelizerOperationStatus<String>(MobeelizerOperationError.connectionError(status));
             }
 
         } catch (JSONException e) {
             MobeelizerOperationError.other(e);
         } catch (IOException e) {
             MobeelizerOperationError.other(e);
         } finally {
             if (is != null) {
                 try {
                     is.close();
                 } catch (IOException e) {
                     delegate.logDebug(e.getMessage());
                 }
             }
             if (reader != null) {
                 try {
                     reader.close();
                 } catch (IOException e) {
                     delegate.logDebug(e.getMessage());
                 }
             }
             client.getConnectionManager().shutdown();
         }
         return null;
     }
 
     private File executeAndGetFile(final HttpRequestBase request) throws IOException {
         HttpClient client = httpClient();
 
         BufferedInputStream in = null;
         BufferedOutputStream out = null;
 
         if (!delegate.isNetworkAvailable()) {
             throw new IOException("Cannot execute HTTP request, network connection not available");
         }
 
         delegate.setProxyIfNecessary(request);
 
         try {
             HttpResponse response = client.execute(request);
 
             if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                 HttpEntity entity = response.getEntity();
 
                 if (entity == null) {
                     throw new IOException("Connection failure: entity not found.");
                 }
 
                 in = new BufferedInputStream(entity.getContent());
 
                 File file = File.createTempFile("mobeelizer", "response");
 
                 out = new BufferedOutputStream(new FileOutputStream(file));
 
                 byte[] buffer = new byte[4096];
                 int n = -1;
 
                 while ((n = in.read(buffer)) != -1) {
                     out.write(buffer, 0, n);
                 }
 
                 return file;
             } else {
                 throw new IOException("Connection failure: " + response.getStatusLine().getStatusCode() + ".");
             }
         } finally {
             if (in != null) {
                 try {
                     in.close();
                 } catch (IOException e) {
                     delegate.logDebug(e.getMessage());
                 }
             }
             if (out != null) {
                 try {
                     out.close();
                 } catch (IOException e) {
                     delegate.logDebug(e.getMessage());
                 }
             }
             client.getConnectionManager().shutdown();
         }
     }
 
 }
