 package controllers;
 
 import play.Play;
 import play.data.validation.Required;
 import play.i18n.Messages;
 import play.mvc.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import models.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: wstasiak
  * Date: 12.06.12
  * Time: 08:09
  * Administration panel
  */
 @With(CommonData.class)
 public class Admin extends Controller {
 
     // Every call is checked for authentication, except those listed below
     @Before(unless={"login", "authenticate", "logout"})
     static void checkAccess() throws Throwable {
         if(!session.contains("username")) {
             login();
         }
     }
 
     public static void index() {
         renderArgs.put("user", session.get("username"));
         List<MapSource> sources = MapSourceCollection.getInstance().allSortedBy("id");
         List<MapLocation> locations = MapLocationCollection.getInstance().topLevel();
         render(sources, locations);
     }
 
     public static void login() {
         if(!session.contains("username")) {
             render();
         }
         else {
             index();
         }
     }
 
     public static void logout() throws Throwable {
         session.clear();
         Application.index(null, null, null);
     }
 
     public static void authenticate(@Required String username, String password) {
         if (checkUserPass(username, password)) {
             session.put("username", username);
             index();
         }
         else {
             flash.put("admin.error", Messages.get("app.admin.error"));
             login();
         }
     }
 
     private static boolean checkUserPass(@Required String username, String password) {
         return username.equalsIgnoreCase(Play.configuration.getProperty("admin.user", "admin"))
                 && password.equals(Play.configuration.getProperty("admin.password", "1234"));
     }
 
     public static void addSource(@Required String name)
     {
         MapSource source = new MapSource(name, name);
         request.format = "json";
         Long id = source.id;
         renderTemplate("@id", id);
     }
 
     public static void deleteSource(@Required Long id)
     {
         MapSource source = MapSource.findById(id);
         if (source == null)
             error(418, "Source not found");
 
         source.delete();
         request.format = "json";
         renderTemplate("@id", id);
     }
 
    public static void addService(@Required String name, @Required String url, @Required Long typeId, @Required Long sourceId)
     {
         MapSource source = MapSource.findById(sourceId);
         if (source == null)
             error(418, "Source not found");
        MapServiceType type = MapServiceType.findById(typeId);
         if (type == null)
             error(418, "Type not found");
 
        MapService service = new MapService(name, name, url, type, source);
         request.format = "json";
         Long id = service.id;
         renderTemplate("@id", id);
     }
 
     public static void deleteService(@Required Long id)
     {
         MapService service = MapService.findById(id);
         if (service == null)
             error(418, "Service not found");
 
         service.delete();
         request.format = "json";
         renderTemplate("@id", id);
     }
 
     public static void addLayers(MapLayer[] layer)
     {
         List<MapLayer> layers = new ArrayList<MapLayer>();
         for (MapLayer entity : layer)
         {
           if (entity.mapService == null)
             error(418, "Service not found");
           entity.save();
           layers.add(entity);
         }
         request.format = "json";
         renderTemplate("@layers", layers);
     }
 
     public static void deleteLayer(@Required Long id)
     {
         MapLayer layer = MapLayer.findById(id);
         if (layer == null)
             error(418, "Layer not found");
 
         layer.delete();
         request.format = "json";
         renderTemplate("@id", id);
     }
 }
