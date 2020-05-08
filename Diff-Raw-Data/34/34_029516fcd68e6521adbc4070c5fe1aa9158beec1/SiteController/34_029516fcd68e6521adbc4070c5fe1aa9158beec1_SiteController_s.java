 package controllers;
 
 import com.shopservice.domain.ClientSettings;
 import com.shopservice.domain.Site;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 public class SiteController extends Controller {
 
     public static Result getSites(String clientId)
     {
         return ok( Json.toJson( Site.get(clientId) ) );
     }
 
     public static Result addSite( String clientId )
     {
         ClientSettings settings = ClientSettings.findById(clientId);
         if (settings == null)
             return badRequest("Client with id "+clientId+" doesn't exist ");
 
         Site site = Json.fromJson( request().body().asJson(), Site.class );
         settings.sites.add(site);
 
         settings.save();
 
         return ok( Json.toJson(site) );
     }
 
 }
