 /************** COPYRIGHT AND CONFIDENTIALITY INFORMATION *********************
  **                                                                          **
  ** Copyright (c) 2013 Technicolor                                           **
  ** All Rights Reserved                                                      **
  **                                                                          **
  ** This program contains proprietary information which is a trade           **
  ** secret of TECHNICOLOR and/or its affiliates and also is protected as     **
  ** an unpublished work under applicable Copyright laws. Recipient is        **
  ** to retain this program in confidence and is not permitted to use or      **
  ** make copies thereof other than as permitted in a written agreement       **
  ** with TECHNICOLOR, UNLESS OTHERWISE EXPRESSLY ALLOWED BY APPLICABLE LAWS. **
  **                                                                          **
  ******************************************************************************/
 package org.qeo.sms.rest.client;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Array;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.qeo.sms.rest.exceptions.InvalidAuthTokenException;
 import org.qeo.sms.rest.exceptions.InvalidJsonInputException;
 import org.qeo.sms.rest.exceptions.MaxRealmReachedException;
 import org.qeo.sms.rest.exceptions.MissingAuthTokenException;
 import org.qeo.sms.rest.exceptions.UnknownRealmIdException;
 import org.qeo.sms.rest.exceptions.UnknownRealmUserException;
 import org.qeo.sms.rest.models.ApiError;
 
 
 /**
  * Utility class with common methods used within SMS REST Client implementation.
  */
 public final class SmsRestUtils
 {
     private static final Log LOG = LogFactory.getLog("SmsRestUtils");
 
     /**
      * Default empty private constructor for utility class.
      */
     private SmsRestUtils()
     {
     }
 
     /**
      * Perform a REST/HTTP GET.
      * 
      * @param accessToken the OAUTH authentication token
      * @param uri the URI used within the REST call
      * @return JSONObject the JSON object retrieved by the GET
      */
     public static JSONObject execRestGet(String accessToken, URI uri)
     {
         final DefaultHttpClient httpClient;
         httpClient = new DefaultHttpClient();
 
         HttpGet request = new HttpGet(uri);
         request.addHeader("Accept", "application/json");
         request.addHeader("Authorization", "Bearer " + accessToken);
 
         try {
             HttpResponse response = httpClient.execute(request);
             if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                 JSONObject jsonError = responseToJson(response);
                 analyseJsonError(jsonError);
             }
 
             JSONObject json = responseToJson(response);
             httpClient.getConnectionManager().shutdown();
             return json;
         }
         catch (Exception e) {
             httpClient.getConnectionManager().shutdown();
             LOG.error("Exception occured in execRestGet", e);
             return null;
         }
     }
 
     /**
      * Perform a REST/HTTP POST.
      * 
      * @param accessToken the OAUTH authentication token
      * @param uri the URI used within the REST call
      * @param json the JSON body to be posted
      * @return JSONObject the JSON object retrieved by the POST
      */
     public static JSONObject execRestPost(String accessToken, URI uri, JSONObject json)
     {
         final DefaultHttpClient httpClient;
         httpClient = new DefaultHttpClient();
 
         HttpPost post = new HttpPost(uri);
         post.setHeader("Content-type", "application/json");
         post.addHeader("Authorization", "Bearer " + accessToken);
 
         try {
             post.setEntity(new StringEntity(json.toString(), "UTF8"));
             HttpResponse response = httpClient.execute(post);
             int status = response.getStatusLine().getStatusCode();
 
             if (!(status == HttpStatus.SC_CREATED || status == HttpStatus.SC_OK)) {
                 JSONObject jsonError = responseToJson(response);
                 analyseJsonError(jsonError);
             }
 
             JSONObject jsonReply = responseToJson(response);
             httpClient.getConnectionManager().shutdown();
             return jsonReply;
         }
         catch (Exception e) {
             httpClient.getConnectionManager().shutdown();
             LOG.error("Exception occured in execRestPost", e);
             return null;
         }
 
     }
 
     /**
      * Perform a REST/HTTP DELETE.
      * 
      * @param accessToken the OAUTH authentication token
      * @param uri the URI used within the REST call
      */
     public static void execRestDelete(String accessToken, URI uri)
     {
         final DefaultHttpClient httpClient;
         httpClient = new DefaultHttpClient();
 
         HttpDelete delete = new HttpDelete(uri);
         delete.setHeader("Content-type", "application/json");
         delete.addHeader("Authorization", "Bearer " + accessToken);
 
         try {
             HttpResponse response = httpClient.execute(delete);
             int status = response.getStatusLine().getStatusCode();
 
             if (status != HttpStatus.SC_OK) {
                 JSONObject jsonError = responseToJson(response);
                 analyseJsonError(jsonError);
             }
 
             httpClient.getConnectionManager().shutdown();
         }
         catch (Exception e) {
             httpClient.getConnectionManager().shutdown();
             LOG.error("Exception occured in execRestDelete", e);
         }
     }
 
     /**
      * Perform a REST/HTTP DELETE with a HTTP body (not standard !!!).
      * 
      * @param accessToken the OAUTH authentication token
      * @param uri the URI used within the REST call
      * @param json the JSON body to be attached to the HTTP DELETE
      */
     public static void execRestDeleteWithJson(String accessToken, URI uri, JSONObject json)
     {
         final DefaultHttpClient httpClient;
         httpClient = new DefaultHttpClient();
 
         HttpDeleteWithBody delete = new HttpDeleteWithBody(uri);
         delete.setHeader("Content-type", "application/json");
         delete.addHeader("Authorization", "Bearer " + accessToken);
 
         try {
             delete.setEntity(new StringEntity(json.toString(), "UTF8"));
             HttpResponse response = httpClient.execute(delete);
             int status = response.getStatusLine().getStatusCode();
 
             if (status != HttpStatus.SC_OK) {
                 JSONObject jsonError = responseToJson(response);
                 analyseJsonError(jsonError);
             }
 
             httpClient.getConnectionManager().shutdown();
         }
         catch (Exception e) {
             httpClient.getConnectionManager().shutdown();
             LOG.error("Exception occured in execRestDeleteWithJson", e);
         }
     }
 
     /**
      * Analyze the JSON representation of the fault error returned on a certain REST Call and throw a proper exception.
      * 
      * @param jsonError
      * @throws JSONException
      * @throws InvalidAuthTokenException
      * @throws MaxRealmReachedException
      * @throws UnknownRealmUserException
      * @throws UnknownRealmIdException
      * @throws MissingAuthTokenException
      * @throws InvalidJsonInputException
      */
     private static void analyseJsonError(JSONObject jsonError)
         throws InvalidAuthTokenException, MaxRealmReachedException, UnknownRealmUserException, UnknownRealmIdException,
         MissingAuthTokenException, InvalidJsonInputException
     {
         ApiError restApiError;
         try {
             restApiError = new ApiError(jsonError);
             switch (restApiError.getCode()) {
                 case 4000:
                     throw new InvalidJsonInputException();
                 case 4001:
                     throw new MaxRealmReachedException();
                 case 4002:
                     throw new UnknownRealmUserException();
                 case 4003:
                     throw new UnknownRealmIdException();
                 case 4010:
                     throw new InvalidAuthTokenException();
                 case 4011:
                     throw new MissingAuthTokenException();
                 default:
                     LOG.error("Unexpected error in analyseJsonError: " + restApiError.toString());
             }
         }
         catch (JSONException e) {
             LOG.error("JSONException occured in analyseJsonError", e);
         }
     }
 
     /**
      * Convert a HttpResponse Object to a JSON Object.
      * 
      * @param response the HttpResponse object.
      * @return JSON representation of the response body.
      * @throws IOException
      * @throws JSONException
      */
     private static JSONObject responseToJson(HttpResponse response)
         throws IOException, JSONException
     {
         InputStream inputStream = response.getEntity().getContent();
         InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
         BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
         StringBuilder stringBuilder = new StringBuilder();
 
         String line;
         while ((line = bufferedReader.readLine()) != null) {
             stringBuilder.append(line);
         }
 
         String output = stringBuilder.toString();
         JSONObject jsonReply = new JSONObject(output);

        inputStream.close();
        inputStreamReader.close();
        bufferedReader.close();
         return jsonReply;
     }
 
     /**
      * A helper method to convert JSONArrays to an array of objects of Class clazz.
      * 
      * @param jsonArray the JSONArray to convert
      * @param clazz the class of the target object for the JSON element
      * @param <K> whatever, check style is buggy
      * @return clazzArray and array of Objects from type clazz
      */
     @SuppressWarnings("unchecked")
     public static <K> ArrayList<K> getObjectArray(JSONArray jsonArray, Class<K> clazz)
     {
         K[] result = (K[]) Array.newInstance(clazz, jsonArray.length());
         for (int i = 0; i < jsonArray.length(); i++) {
             try {
                 result[i] = clazz.getConstructor(JSONObject.class).newInstance(jsonArray.getJSONObject(i));
             }
             catch (Exception e) {
                 LOG.error("Exception occured in getObjectArray", e);
             }
         }
         return new ArrayList<K>(Arrays.asList(result));
     }
 }
