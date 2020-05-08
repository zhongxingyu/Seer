 /*
 * Copyright 2012 INRIA
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 package com.mymed.tests.unit.handler.utils;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.nio.charset.Charset;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.message.BasicNameValuePair;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.mymed.controller.core.requesthandler.message.JsonMessage;
 import com.mymed.model.data.session.MSessionBean;
 import com.mymed.model.data.user.MUserBean;
 import com.mymed.utils.MLogger;
 
 /**
  * Class with utilities used to perform tests on the various requests handler on
  * the backend.
  * <p>
  * In oder for these tests to work, it is necessary to have the Mymed backend
  * code running within Glassfish. These tests make the assumption that the
  * backend is running on {@code localhost}, on port {@code 8080}.
  * 
  * @author Milo Casagrande
  * 
  */
 public class TestUtils {
   public static final String MYMED_EMAIL = "ema.nymton@example.org";
   public static final String FIRST_NAME = "Ema";
   public static final String LAST_NAME = "Nymton";
   public static final String NAME = FIRST_NAME + " " + LAST_NAME;
   public static final String MYMED_ID = "MYMED_" + MYMED_EMAIL;
 
   /* Where the backend is running */
   private static final String AUTHORITY = "localhost:8080";
   /* The path to the backend servlet */
  private static final String SERVLET_PATH = "/backend/";
   private static final String PROTOCOL = "http";
 
   private static final String DIGEST_SHA_512 = "SHA-512";
   private static final String DIGEST_SHA_1 = "SHA-1";
   private static final String CHAR_NAME = "UTF-8";
   private static final String FAKE_PASSWORD = "one, two, three, star";
 
   /**
    * Create the URL to use to query the backend servlet
    * 
    * @param params
    *          a list of pair values
    * @return the String that represents the query URL encoded in UTF-8
    */
   public static String createQueryParams(final List<NameValuePair> params) {
     return URLEncodedUtils.format(params, "UTF-8");
   }
 
   /**
    * Create the exact path to the backend handler we want to test
    * 
    * @param handlerName
    *          the name of the handler, should be the name of the class
    * @return the path to the servlet
    */
   public static String createPath(final String handlerName) {
     String returnString = "";
 
     if (handlerName != null) {
       returnString = SERVLET_PATH + handlerName;
     }
 
     return returnString;
   }
 
   /**
    * Create the URI to use for the test
    * 
    * @param path
    *          the path to the servlet
    * @param query
    *          the query string
    * @return the URI to use for the test
    * @throws URISyntaxException
    */
   public static URI createUri(final String path, final String query) throws URISyntaxException {
     return new URI(PROTOCOL, AUTHORITY, path, query, null);
   }
 
   /**
    * Add name-value pairs to a NameValuePair list
    * 
    * @param params
    *          a {@link List} where to add the parameters
    * @param name
    *          the parameter name
    * @param value
    *          the value of the parameter
    */
   public static void addParameter(final List<NameValuePair> params, final String name, final String value) {
     params.add(new BasicNameValuePair(name, value));
   }
 
   /**
    * Check that a JSON string is valid with regards to the Mymed JSON format
    * defined in {@link JsonMessage}
    * 
    * @param json
    *          the JSON string to be validated
    * @return true if is valid, false otherwise
    */
   public static boolean isValidJson(final String json) {
     boolean validJson = false;
 
     final JsonParser parser = new JsonParser();
     final JsonObject obj = parser.parse(json).getAsJsonObject();
 
     final Iterator<Entry<String, JsonElement>> iter = obj.entrySet().iterator();
 
     while (iter.hasNext()) {
       validJson = true;
       final Entry<String, JsonElement> entry = iter.next();
       validJson &= MJson.isValidElement(entry.getKey());
     }
 
     return validJson;
   }
 
   /**
    * Check that a 'user' JSON string is valid with regards to the 'user' JSON
    * format
    * 
    * @param json
    *          the JSON string to be validated
    * @return true if is valid, false otherwise
    */
   public static boolean isValidUserJson(final String json) {
     boolean validJson = false;
 
     final JsonParser parser = new JsonParser();
     final JsonObject obj = parser.parse(json).getAsJsonObject().get("data").getAsJsonObject();
 
     // Should be "user" in the response, dunno if it has been fixed everywhere
     final JsonElement element = obj.get("user") == null ? obj.get("profile") : obj.get("user");
     final JsonObject userObject = parser.parse(element.getAsString()).getAsJsonObject();
 
     if (userObject.isJsonObject()) {
       final Iterator<Entry<String, JsonElement>> iter = userObject.entrySet().iterator();
 
       while (iter.hasNext()) {
         validJson = true;
         final Entry<String, JsonElement> entry = iter.next();
         validJson &= MUserJson.isValidElement(entry.getKey());
       }
     }
 
     return validJson;
   }
 
   /**
    * @return a JSON object for a user
    */
   public static JsonObject createUserJson() {
     final JsonObject user = new JsonObject();
 
     user.addProperty("id", MYMED_ID);
     user.addProperty("login", MYMED_EMAIL);
     user.addProperty("email", MYMED_EMAIL);
     user.addProperty("name", NAME);
     user.addProperty("firstName", FIRST_NAME);
     user.addProperty("lastName", LAST_NAME);
     user.addProperty("birthday", "");
     user.addProperty("lastConnection", "0");
     user.addProperty("profilePicture", "");
     user.addProperty("socialNetworkID", "MYMED");
     user.addProperty("socialNetworkName", "myMed");
 
     return user;
   }
 
   /**
    * @return a JSON object for the authentication
    */
   public static JsonObject createAuthenticationJson() {
     final JsonObject auth = new JsonObject();
 
     auth.addProperty("login", MYMED_EMAIL);
     auth.addProperty("user", MYMED_ID);
     auth.addProperty("password", createPassword(FAKE_PASSWORD));
 
     return auth;
   }
 
   /**
    * @return a JSON object for the session
    */
   public static JsonObject createSessionJson() {
     final JsonObject session = new JsonObject();
     final String accessToken = createAccessToken();
 
     session.addProperty("accessToken", accessToken);
     session.addProperty("user", MYMED_ID);
     session.addProperty("ip", "localhost");
     session.addProperty("id", accessToken);
 
     return session;
   }
 
   /**
    * @return a fake password to be used for testing
    */
   public static String getFakePassword() {
     return createPassword(FAKE_PASSWORD);
   }
 
   /**
    * Create a SHA-512 string from a fictitious password
    * 
    * @param pwd
    *          the fictitious password
    * @return the SHA-512 of the password
    */
   private static String createPassword(final String pwd) {
     String password = "";
 
     try {
       final StringBuffer hex = new StringBuffer(250);
       final MessageDigest digest = MessageDigest.getInstance(DIGEST_SHA_512);
       final byte[] mdbytes = digest.digest(pwd.getBytes(Charset.forName(CHAR_NAME)));
 
       for (final byte b : mdbytes) {
         hex.append(Integer.toHexString(0xFF & b));
       }
 
       hex.trimToSize();
       password = hex.toString();
     } catch (final NoSuchAlgorithmException ex) {
       // We should never get here
       MLogger.getLogger().debug("Digest algorithm '{}' does not exist!", DIGEST_SHA_512, ex.getCause());
     }
 
     return password;
   }
 
   /**
    * Create a fake access token as an hash string
    * 
    * @return an access token
    */
   public static String createAccessToken() {
     String token = "";
 
     final long time = System.currentTimeMillis();
 
     final StringBuffer hex = new StringBuffer(250);
     try {
       final MessageDigest digest = MessageDigest.getInstance(DIGEST_SHA_1);
       final byte[] mdBytes = digest
           .digest((MYMED_EMAIL + getFakePassword() + time).getBytes(Charset.forName(CHAR_NAME)));
 
       for (final byte b : mdBytes) {
         hex.append(Integer.toHexString(0xFF & b));
       }
 
       hex.trimToSize();
       token = hex.toString();
     } catch (final NoSuchAlgorithmException ex) {
       // We should never get here
       MLogger.getLogger().debug("Digest algorithm '{}' does not exist!", DIGEST_SHA_1, ex.getCause());
     }
 
     return token;
   }
 
   /**
    * Create a new session bean
    * 
    * @param accessToken
    *          the access token to set for this session
    * @return a session bean
    */
   public static MSessionBean createSessionBean(final String accessToken) {
     final MSessionBean sessionBean = new MSessionBean();
 
     sessionBean.setAccessToken(accessToken);
     sessionBean.setId(accessToken);
     sessionBean.setUser(MYMED_ID);
     sessionBean.setIp("localhost");
     sessionBean.setCurrentApplications("");
     sessionBean.setP2P(false);
 
     return sessionBean;
   }
 
   /**
    * Create a new user bean
    * 
    * @return a user bean
    */
   public static MUserBean createUserBean(final String accessToken) {
     final MUserBean user = new MUserBean();
 
     user.setId(MYMED_ID);
     user.setLogin(MYMED_EMAIL);
     user.setEmail(MYMED_EMAIL);
     user.setName(NAME);
     user.setSession(accessToken);
     user.setFirstName(FIRST_NAME);
     user.setLastName(LAST_NAME);
     user.setSocialNetworkID("MYMED");
     user.setSocialNetworkName("myMed");
 
     return user;
   }
 }
