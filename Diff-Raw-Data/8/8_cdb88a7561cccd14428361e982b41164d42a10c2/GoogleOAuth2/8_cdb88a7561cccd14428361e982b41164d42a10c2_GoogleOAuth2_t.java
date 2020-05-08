 package controllers;
 
 import java.util.Arrays;
 
 import models.User;
 import play.Logger;
 import play.libs.WS;
 import play.libs.WS.HttpResponse;
 import play.mvc.Controller;
 
import com.google.api.client.googleapis.auth.oauth2.GoogleBrowserClientRequestUrl;
 import com.google.gson.JsonElement;
 
 public class GoogleOAuth2 extends Controller {
 
     public static void googleLogin() {
         String url = new GoogleBrowserClientRequestUrl(
                 "593338536251.apps.googleusercontent.com",
                 "http://localhost:9000/oauth2callback", Arrays.asList(
                         "https://www.googleapis.com/auth/userinfo.email",
                         "https://www.googleapis.com/auth/userinfo.profile"))
                 .build();
         redirect(url);
     }
 
     public static void callback() {
         Logger.info("Google callback received!");
         render();
     }
 
     public static void realcallback(String access_token) {
         if (access_token == null) {
             Logger.error("Google access_token was null!");
             flash.put("error", "Google auth token was null.");
             Application.index();
         }
 
         HttpResponse response = WS.url(
                 "https://www.googleapis.com/oauth2/v1/userinfo?access_token="
                         + access_token).get();
         if (response.success()) {
             Logger.info("Successful google api call");
             long uid = loginGoogleUser(response.getJson());
             Application.login(uid);
         } else
             Application.index();
     }
 
     private static long loginGoogleUser(JsonElement json) {
         String displayname = json.getAsJsonObject().get("name").getAsString();
         String pictureURL = json.getAsJsonObject().get("picture").getAsString();
         String googleId = json.getAsJsonObject().get("id").getAsString();
 
         User u = User.find("byGoogleId", googleId).first();
         if (u == null) {
             u = new User(googleId);
             u.create();
         }
         u.pictureURL = pictureURL;
         u.displayname = displayname;
         u.save();
         Logger.info("save action was successful");
         return u.id;
     }
 }
