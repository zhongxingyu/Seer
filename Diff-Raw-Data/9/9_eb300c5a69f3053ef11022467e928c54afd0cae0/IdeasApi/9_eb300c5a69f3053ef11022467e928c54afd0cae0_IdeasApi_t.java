 package com.masterofcode.android._10ideas.helpers;
 
 import com.masterofcode.android._10ideas.objects.Idea;
 import com.masterofcode.android._10ideas.objects.Ideas;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.StringBody;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.UnsupportedEncodingException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: boss1088
  * Date: 5/25/12
  * Time: 10:18 PM
  * To change this template use File | Settings | File Templates.
  */
 public class IdeasApi {
 
     public static void register(String userName, String pass) throws UnsupportedEncodingException {
         MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
 
         reqEntity.addPart("user[email]", new StringBody(userName));
         reqEntity.addPart("user[password]", new StringBody(pass));
 
         JSONObject json = RestClient.post(RestClient.BASE_URL + RestClient.BASE_USERS, reqEntity);
         String error = checkForError(json);
 
         if (json == null) {
             throw new UnsupportedEncodingException();
         } else if (!error.equals("")) {
                 PreferenceHelper.setError(error);
                 return;
         }
 
         PreferenceHelper.setUserEmail(userName);
         PreferenceHelper.setUserPass(pass);
         PreferenceHelper.setAuthToken(json.optString("auth_token").trim());
         PreferenceHelper.setUserId(json.optString("user_id").trim());
     }
 
     public static void sign_in(String userName, String pass) throws UnsupportedEncodingException {
         MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
 
         reqEntity.addPart("user[email]", new StringBody(userName));
         reqEntity.addPart("user[password]", new StringBody(pass));
 
         JSONObject json = RestClient.post(RestClient.BASE_URL + RestClient.BASE_USERS_SIGN_IN, reqEntity);
         String error = checkForError(json);
 
         if (json == null) {
             throw new UnsupportedEncodingException();
         }  else if (!error.equals("")) {
             PreferenceHelper.setError(error);
             return;
         }
 
         PreferenceHelper.setUserEmail(userName);
         PreferenceHelper.setUserPass(pass);
         PreferenceHelper.setAuthToken(json.optString("auth_token"));
         PreferenceHelper.setUserId(json.optString("user_id"));
     }
 
     public static void create(String essential, Boolean isPublic) throws UnsupportedEncodingException {
         MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
 
         reqEntity.addPart("user[email]", new StringBody(PreferenceHelper.getUserEmail()));
         reqEntity.addPart("user[password]", new StringBody(PreferenceHelper.getUserPass()));
         reqEntity.addPart("auth_token", new StringBody(PreferenceHelper.getAuthToken()));
         reqEntity.addPart("idea[essential]", new StringBody(essential));
         reqEntity.addPart("idea[user_id]", new StringBody(PreferenceHelper.getUserId()));
         if (isPublic) {
             reqEntity.addPart("idea[public]", new StringBody("1"));
         } else {
             reqEntity.addPart("idea[public]", new StringBody("0"));
         }
 
         RestClient.post(RestClient.BASE_URL + RestClient.BASE_IDEAS, reqEntity);
 
         //TODO rebuild if need some return
     }
 
     public static Ideas getIdeas(String wichIdeas) throws UnsupportedEncodingException, NullPointerException {
 
         Ideas ideas = null;
         JSONArray jsonArray = RestClient.get(RestClient.BASE_URL + wichIdeas
                                                 + "?auth_token=" + PreferenceHelper.getAuthToken());
 
         try {
            ideas = Ideas.fromJson(jsonArray);
         } catch (JSONException e) {
             e.printStackTrace();
         } catch (NullPointerException npe) {
             throw npe;
         }
 
         return ideas;
     }
 
     public static Idea getIdeaById(String id) {
        JSONObject json = RestClient.getObject(RestClient.BASE_URL + RestClient.BASE_IDEA + "/" + id + ".json"
                 + "?auth_token=" + PreferenceHelper.getAuthToken());
 
         return new Idea(json);
     }
 
     public static String vote(String id) throws UnsupportedEncodingException {
         MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
         reqEntity.addPart("auth_token", new StringBody(PreferenceHelper.getAuthToken()));
 
         return RestClient.sendPut(RestClient.BASE_URL + RestClient.BASE_IDEA + "/" + id + "/vote.json", reqEntity);
     }
 
     public static String publish(String id) throws UnsupportedEncodingException {
         MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
         reqEntity.addPart("auth_token", new StringBody(PreferenceHelper.getAuthToken()));
 
         return RestClient.sendPut(RestClient.BASE_URL + RestClient.BASE_IDEA + "/" + id + "/publish.json", reqEntity);
     }
 
     private static String checkForError(JSONObject json) {
         if (!json.optString("errors").trim().equals("")) {
             return json.optString("errors").trim();
         }
 
         if (!json.optString("error").trim().equals("")) {
             return json.optString("error").trim();
         }
 
         return "";
     }
 }
