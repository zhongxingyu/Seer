 package com.rallydev.lookback;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Scanner;
 
 public class LookbackApi {
 
     String server;
     String versionMajor;
     String versionMinor;
     String workspace;
     String username;
     String password;
 
     public LookbackApi() {
         server = "https://rally1.rallydev.com";
         versionMajor = "2";
         versionMinor = "0";
     }
 
     public LookbackApi setCredentials(String username, String password) {
         this.username = username;
         this.password = password;
 
         return this;
     }
 
     public LookbackApi setServer(String server) {
         this.server = server;
 
         return this;
     }
 
     public LookbackApi setWorkspace(String workspace) {
         this.workspace = workspace;
 
         return this;
     }
 
     public LookbackApi setVersion(String major, String minor) {
         this.versionMajor = major;
         this.versionMinor = minor;
 
         return this;
     }
 
     public LookbackQuery newSnapshotQuery() {
         return new LookbackQuery(this);
     }
 
     public LookbackQuery getQueryForNextPage(LookbackResult resultSet) {
         return new LookbackQuery(resultSet, this);
     }
 
     LookbackResult executeQuery(LookbackQuery query) throws IOException {
         String requestJson = query.getRequestJson();
         HttpResponse response = executeRequest(requestJson);
         LookbackResult result = buildLookbackResult(response);
         return result.validate(query);
     }
 
     private HttpResponse executeRequest(String requestJson) throws IOException {
         HttpUriRequest request = createRequest(requestJson);
         HttpClient httpClient = new DefaultHttpClient();
         return httpClient.execute(request);
     }
 
     private LookbackResult buildLookbackResult(HttpResponse response) throws IOException {
         HttpEntity responseBody = validateResponse(response);
         String json = getResponseJson(responseBody);
         return serializeLookbackResultFromJson(json);
     }
 
     private HttpUriRequest createRequest(String requestJson) throws IOException {
         HttpPost post = new HttpPost(buildUrl());
         addAuthHeaderToRequest(post);
         post.setEntity(new StringEntity(requestJson, "UTF-8"));
         return post;
     }
 
     private HttpEntity validateResponse(HttpResponse response) {
         HttpEntity responseBody = response.getEntity();
         if (responseBody == null) {
             throw new LookbackException("No data received from server");
         }
         return responseBody;
     }
 
     private String getResponseJson(HttpEntity responseBody) throws IOException {
         InputStream responseStream = responseBody.getContent();
         try {
             return readFromStream(responseStream);
         } finally {
             responseStream.close();
         }
     }
 
     private LookbackResult serializeLookbackResultFromJson(String json) {
         Gson serializer = new GsonBuilder().serializeNulls().create();
         return serializer.fromJson(json, LookbackResult.class);
     }
 
     private String buildUrl() {
         if (workspace == null) {
             throw new LookbackException("Workspace is required to execute query");
         }
 
         return String.format(
                 "%s/analytics/%s/service/rally/workspace/%s/artifact/snapshot/query.js",
                 server, buildApiVersion(), workspace);
     }
 
     private void addAuthHeaderToRequest(HttpRequest request) {
         request.addHeader("Authorization", getBasicAuthHeader());
     }
 
     private String readFromStream(InputStream stream) {
         Scanner scanner = new Scanner(stream);
         scanner.useDelimiter("\\A");
         if (scanner.hasNext()) {
             return scanner.next();
         } else {
             return "";
         }
     }
 
     private String buildApiVersion() {
         return "v" + versionMajor + "." + versionMinor;
     }
 
     private String getBasicAuthHeader() {
         byte[] token = getUnencodedAuthToken();
         byte[] encodedToken = Base64.encodeBase64(token);
         return buildAuthHeader(encodedToken);
     }
 
     private byte[] getUnencodedAuthToken() {
         if (username == null || password == null) {
             throw new LookbackException("Username and Password are required to execute query");
         }
 
         String tokenString = username + ":" + password;
         return tokenString.getBytes();
     }
 
     private String buildAuthHeader(byte[] encodedToken) {
         String tokenString = new String(encodedToken);
         return "Basic " + tokenString;
     }
 }
