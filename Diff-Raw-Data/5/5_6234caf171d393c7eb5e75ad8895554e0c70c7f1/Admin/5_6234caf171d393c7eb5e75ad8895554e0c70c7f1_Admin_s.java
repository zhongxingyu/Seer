 package controllers;
 
 import play.Play;
 import play.data.validation.Required;
 import play.db.jpa.Transactional;
 import play.i18n.Messages;
 import play.libs.Files;
 import play.mvc.*;
 import play.vfs.VirtualFile;
 
 import java.io.File;
 import java.util.*;
 
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
     private static void checkAccess() throws Throwable {
         if(!session.contains("loggedin")) {
             login();
         }
     }
 
     public static void index() {
         List<MapSource> sources = MapSourceCollection.getInstance().allSortedBy("sort, id");
         List<MapLocation> locations = MapLocationCollection.getInstance().topLevel();
         render(sources, locations);
     }
 
     public static void login() {
         if(!session.contains("loggedin")) {
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
 
     public static void authenticate(String password) {
         if (checkUserPass(password)) {
             session.put("loggedin", true);
             index();
         }
         else {
             flash.put("admin.error", Messages.get("app.admin.error"));
             login();
         }
     }
 
     private static boolean checkUserPass(String inputPassword) {
         MapSetting panelPassword = MapSetting.findByKey(MapSetting.ADMIN_PASSWORD);
         String panelPasswordEncrypted = panelPassword.value;
         String inputPasswordEncrypted = MapSetting.encryptPassword(inputPassword);
         return inputPasswordEncrypted.equalsIgnoreCase(panelPasswordEncrypted);
     }
 
     public static void addSource(@Required String name)
     {
         MapSource source = new MapSource(name, name);
         request.format = "json";
         Long id = source.id;
         renderTemplate("@id", id);
     }
 
     public static void editSource(@Required Long id, @Required String name)
     {
         MapSource source = MapSource.findById(id);
         if (source == null)
             error(404, "Source not found");
         source.displayName = name;
         source.save();
         request.format = "json";
         renderTemplate("@id", id);
     }
 
     public static void deleteSource(@Required Long id)
     {
         MapSource source = MapSource.findById(id);
         if (source == null)
             error(404, "Source not found");
 
         source.delete();
         request.format = "json";
         renderTemplate("@id", id);
     }
 
     public static void addService(@Required String name, @Required String url, @Required String type, @Required Long sourceId)
     {
         MapSource source = MapSource.findById(sourceId);
         if (source == null)
             error(404, "Source not found");
         MapServiceType serviceType = MapServiceType.find("byName", type).first();
         if (type == null)
             error(404, "Type not found");
 
         MapService service = new MapService(name, name, url, serviceType, source);
         request.format = "json";
         Long id = service.id;
         renderTemplate("@id", id);
     }
 
     public static void editService(@Required Long id, String name, Long xCoordinate, Long yCoordinate, Long zoomLevel)
     {
         MapService service = MapService.findById(id);
         if (service == null)
             error(404, "Service not found");
 
         if (name != null)
             service.displayName = name;
         if (xCoordinate != null && yCoordinate != null && zoomLevel != null)
         {
             service.xCoordinate = xCoordinate;
             service.yCoordinate = yCoordinate;
             service.zoomLevel = zoomLevel;
         }
         service.save();
         request.format = "json";
         renderTemplate("@id", id);
     }
 
     public static void deleteService(@Required Long id)
     {
         MapService service = MapService.findById(id);
         if (service == null)
             error(404, "Service not found");
 
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
             error(404, "Service not found");
           entity.save();
           layers.add(entity);
         }
         request.format = "json";
         renderTemplate("@layers", layers);
     }
 
     public static void editLayer(@Required Long id, String name, Boolean defaultVisible)
     {
         MapLayer layer = MapLayer.findById(id);
         if (layer == null)
             error(404, "Layer not found");
 
         if (name != null)
             layer.displayName = name;
         if (defaultVisible != null)
             layer.defaultVisible = defaultVisible;
         layer.save();
         request.format = "json";
         renderTemplate("@id", id);
     }
 
     public static void deleteLayer(@Required Long id)
     {
         MapLayer layer = MapLayer.findById(id);
         if (layer == null)
             error(404, "Layer not found");
 
         layer.delete();
         request.format = "json";
         renderTemplate("@id", id);
     }
 
     public static void orderLayers(MapLayer[] listOfLayers)
     {
         Map<MapService, Long> servicesOrder = new HashMap<MapService, Long>();
         Map<MapSource, Long> sourcesOrder = new HashMap<MapSource, Long>();
         for (MapLayer entity : listOfLayers)
         {
             MapLayer layer = MapLayer.findById(entity.id);
             if (layer == null)
                 error(404, "Layer not found");
             layer.sort = entity.sort;
             layer.save();
             if (!servicesOrder.containsKey(layer.mapService) || servicesOrder.get(layer.mapService) > entity.sort)
                 servicesOrder.put(layer.mapService, entity.sort);
             if (!sourcesOrder.containsKey(layer.mapService.mapSource) || sourcesOrder.get(layer.mapService.mapSource) > entity.sort)
                 sourcesOrder.put(layer.mapService.mapSource, entity.sort);
         }
         for (MapService service: servicesOrder.keySet())
         {
             service.sort = servicesOrder.get(service);
             service.save();
         }
         for (MapSource source: sourcesOrder.keySet())
         {
             source.sort = sourcesOrder.get(source);
             source.save();
         }
         request.format = "json";
         renderJSON(listOfLayers);
     }
 
     public static void addLocation(@Required String name, @Required Long xCoordinate, @Required Long yCoordinate, @Required Long zoomLevel, Long parentId)
     {
         MapLocation location = new MapLocation();
         location.name = name;
         location.displayName = name;
         location.xCoordinate = xCoordinate;
         location.yCoordinate = yCoordinate;
         location.zoomLevel = zoomLevel;
         if (parentId != null)
             location.parent = MapLocation.findById(parentId);
         location.save();
         request.format = "json";
         Long id = location.id;
         renderTemplate("@id", id);
     }
 
     public static void editLocation(@Required Long id, String name, Long xCoordinate, Long yCoordinate, Long zoomLevel)
     {
         MapLocation location = MapLocation.findById(id);
         if (location == null)
             error(404, "Service not found");
 
         if (name != null)
             location.displayName = name;
         if (xCoordinate != null && yCoordinate != null && zoomLevel != null)
         {
             location.xCoordinate = xCoordinate;
             location.yCoordinate = yCoordinate;
             location.zoomLevel = zoomLevel;
         }
         location.save();
         request.format = "json";
         renderTemplate("@id", id);
     }
 
     public static void deleteLocation(@Required Long id)
     {
         MapLocation location = MapLocation.findById(id);
         if (location == null)
             error(404, "Service not found");
 
         location.delete();
         request.format = "json";
         renderTemplate("@id", id);
     }
 
     @Transactional
     public static void changePassword(@Required String oldPassword, @Required String newPassword)
     {
         MapSetting password = MapSetting.findByKey(MapSetting.ADMIN_PASSWORD);
         String encryptedOldPassword = MapSetting.encryptPassword(oldPassword);
         String encryptedPanelPassword = password.value;
         if (encryptedPanelPassword.compareToIgnoreCase(encryptedOldPassword) != 0)
             error(403, "Invalid password");
 
         MapSetting.createNewSalt();
         password.value = MapSetting.encryptPassword(newPassword);
         password.save();
         renderText("OK");
     }
 
     public static void changeInitialMap(@Required Long xCoordinate, @Required Long yCoordinate, @Required Long zoomLevel)
     {
         MapSetting xCoordinateSetting = MapSetting.findByKey(MapSetting.MAP_INITIAL_X_COORDINATE);
         MapSetting yCoordinateSetting = MapSetting.findByKey(MapSetting.MAP_INITIAL_Y_COORDINATE);
         MapSetting zoomLevelSetting = MapSetting.findByKey(MapSetting.MAP_INITIAL_Z);
 
         xCoordinateSetting.value = xCoordinate.toString();
         yCoordinateSetting.value = yCoordinate.toString();
         zoomLevelSetting.value = zoomLevel.toString();
 
         xCoordinateSetting.save();
         yCoordinateSetting.save();
         zoomLevelSetting.save();
 
         renderText("OK");
     }
 
     public static void changeBoundingBox(@Required Long mapBoundingLeft, @Required Long mapBoundingTop, @Required Long mapBoundingRight, @Required Long mapBoundingBottom)
     {
         MapSetting mapBoundingLeftSetting   = MapSetting.findByKey(MapSetting.MAP_BOUNDINGBOX_LEFT);
         MapSetting mapBoundingTopSetting    = MapSetting.findByKey(MapSetting.MAP_BOUNDINGBOX_TOP);
         MapSetting mapBoundingRightSetting  = MapSetting.findByKey(MapSetting.MAP_BOUNDINGBOX_RIGHT);
         MapSetting mapBoundingBottomSetting = MapSetting.findByKey(MapSetting.MAP_BOUNDINGBOX_BOTTOM);
 
         mapBoundingLeftSetting.value   = mapBoundingLeft.toString();
         mapBoundingTopSetting.value    = mapBoundingTop.toString();
         mapBoundingRightSetting.value  = mapBoundingRight.toString();
         mapBoundingBottomSetting.value = mapBoundingBottom.toString();
 
         mapBoundingLeftSetting.save();
         mapBoundingTopSetting.save();
         mapBoundingRightSetting.save();
         mapBoundingBottomSetting.save();
 
         renderText("OK");
     }
 
     public static void changeResolutions(@Required String resolutions)
     {
         MapSetting resolutionsSetting   = MapSetting.findByKey(MapSetting.MAP_RESOLUTIONS);
 
         resolutionsSetting.value = resolutions;
         resolutionsSetting.save();
 
         renderJSON(resolutionsSetting);
     }
 
     public static void uploadArms(@Required Long id, @Required File uploadFile) throws Exception
     {
         MapService mapService = MapService.findById(id);
         if (uploadFile == null)
             error(404, "File not found");
         if (mapService == null)
             error(404, "Service not found");
         mapService.coatOfArms = uploadFile.getName();
         mapService.save();
         File newArms = Play.getFile("public/images/arms/" + uploadFile.getName());
         Files.copy(uploadFile, newArms);
         Files.delete(uploadFile);
        VirtualFile arms = Play.getVirtualFile(newArms.getPath());
         renderTemplate("@upload", arms);
     }
 
     public static void uploadOwnerArms(@Required boolean armsUse, String appTitle, File armsFile) throws Exception
     {
         MapSetting useArmsSetting = MapSetting.findByKey(MapSetting.APPLICATION_ARMS);
         MapSetting appTitleSetting = MapSetting.findByKey(MapSetting.APPLICATION_TITLE);
         File newArms = Play.getFile("public/images/app_arms.png");
 
         useArmsSetting.value = Boolean.toString(armsUse);
         useArmsSetting.save();
         appTitleSetting.value = appTitle;
         appTitleSetting.save();
 
         if (armsFile != null)
         {
             Files.copy(armsFile, newArms);
             Files.delete(armsFile);
         }
 
         String title = armsUse ? appTitleSetting.value : Messages.get("app.owner");
        VirtualFile arms = Play.getVirtualFile("public/images/app_arms.png");
         renderTemplate("@upload", armsUse, arms, title);
     }
 }
