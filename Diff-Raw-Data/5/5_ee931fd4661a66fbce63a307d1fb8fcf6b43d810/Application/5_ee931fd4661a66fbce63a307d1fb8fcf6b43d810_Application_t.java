 package controllers;
 
 import play.*;
 import play.libs.WS;
 import play.mvc.*;
 import utils.Secure;
 
 import java.util.*;
 
 import com.google.gson.JsonObject;
 
import flexjson.JSONSerializer;

 import models.*;
 
 @With(Secure.class)
 public class Application extends Controller {
     
     
     public static void index() {
         render();
     }
     
     public static void user() {
         User user=User.findById(Long.parseLong(session.get("uuid")));
        renderJSON(new JSONSerializer().exclude("*.class").serialize(user));
     }
 
 }
