 package com.example.bulletinator.data;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.location.Location;
 import android.view.View;
 
 import com.example.bulletinator.fragments.ParentFragment;
 import com.example.bulletinator.helpers.CallbackListener;
 import com.example.bulletinator.helpers.FunctionObj;
 import com.example.bulletinator.helpers.Rectangle;
 import com.example.bulletinator.server.AllBuildingsResponse;
 import com.example.bulletinator.server.BinResponse;
 import com.example.bulletinator.server.BuildingResponse;
 import com.example.bulletinator.server.DummyResponse;
 import com.example.bulletinator.server.EverythingRequest;
 import com.example.bulletinator.server.EverythingResponse;
 import com.example.bulletinator.server.GPSRequest;
 import com.example.bulletinator.server.GPSResponse;
 import com.example.bulletinator.server.ServerResponse;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class AppData {
     private static AppData instance = null;
 
     public static final String baseurl = "http://linux.ucla.edu/~cs130s/get.php";
 
     protected AppData() {
         bulletins = new HashMap<Integer, Bulletin>();
         buildings = new HashMap<Integer, Building>();
         files     = new HashMap<Integer, Bitmap>();
     }
 
 
     public static AppData getInstance(FunctionObj<ServerResponse> notifyMain) {
         if (instance == null) {
             instance = new AppData();
         }
         instance.notifyMain = notifyMain;
         return instance;
     }
 
     /* updaters */
     public static AppData update(Location loc) {
         //TODO: fix bad style
         if (loc == null)
         {
             lat = 0;
             lon = 0;
 
             GPSRequest gpsr =
                     new GPSRequest(instance.notifyMain, instance.baseurl,
                             instance.lat, instance.lon);
             gpsr.send();
         }
         else if (isLocationRelevant(loc)) {
             lat = loc.getLatitude();
             lon = loc.getLongitude();
 
             GPSRequest gpsr =
                     new GPSRequest(instance.notifyMain, instance.baseurl,
                             instance.lat, instance.lon);
             gpsr.send();
 
         }
 
         return instance;
     }
 
     public static AppData update(DummyResponse dr) {
         instance.dummy = dr.title;
         return instance;
     }
 
     public static AppData update(EverythingResponse er) {
         instance.buildings.putAll(er.buildings);
         instance.bulletins.putAll(er.bulletins);
         return instance;
     }
 
     public static AppData update(BuildingResponse bldr) {
         if (bldr.bld != null) {
             instance.buildings.put(bldr.bld.getId(), bldr.bld);
 
             instance.bulletins.putAll(bldr.bulletins);
         }
 
         return instance;
     }
 
     public static AppData update(AllBuildingsResponse bldr) {
         for (Map.Entry<Integer, Building> bldEntry : bldr.buildings.entrySet()) {
             if (!instance.buildings.containsKey(bldEntry.getKey())) {
                 instance.buildings.put(bldEntry.getKey(), bldEntry.getValue());
             }
         }
 
         return instance;
     }
 
     public static AppData update(GPSResponse gpsr) {
         if ((gpsr.curBld == null) ||
             ((instance.curBld != null) && (gpsr.curBld.getId() == instance.curBld.getId()))) {
             /* TODO
               once useful bounds rectangles or nearby buildings are returned,
               they may change even when curBld does not change, so we shouldn't just
               return instance
              */
             return instance;
         }
         else
         {
             instance.curBld = gpsr.curBld;
 
 
 
             if (!instance.buildings.containsKey(gpsr.curBld.getId())) {
                 instance.buildings.put(gpsr.curBld.getId(), gpsr.curBld);
             }
 
             instance.bulletins.putAll(gpsr.bulletins);
 
 
 
             /* insert any buildings that haven't been downloaded */
             for (Map.Entry<Integer, Building> bldEntry : gpsr.nearBuildings.entrySet()) {
                 if (!instance.buildings.containsKey(bldEntry.getKey())) {
                     instance.buildings.put(bldEntry.getKey(), bldEntry.getValue());
                 }
             }
 
             /* set the bounds rectangle */
             instance.bounds = gpsr.bounds;
 
            if (tabForCurrent != null) {
                tabForCurrent.dataArrived(gpsr);
            }
         }
 
         return instance;
     }
 
     public static AppData update(BinResponse br) {
         Bitmap bitmap = BitmapFactory.decodeByteArray(br.bytes, 0, br.bytes.length);
         instance.files.put(br.fid, bitmap);
         return instance;
     }
 
     public static AppData update(Building bld) {
         return instance;
     }
 
 
     /* Helper functions */
     public static boolean isLocationRelevant(Location loc) {
         return ((bounds == null) ||
                 (bounds.isOutside(loc.getLatitude(), loc.getLongitude())));
     }
 
 
     /* Accessor functions */
     public static Bulletin getBulletin(int btnid) {
         return instance.bulletins.get(btnid);
     }
 
     public static Building getBuilding(int bldid) {
         return instance.buildings.get(bldid);
     }
 
     public static List<Bulletin> getBulletinsIn(Building bld) {
         List<Bulletin> btnList = new ArrayList<Bulletin>();
 
         for (Integer ii : bld.getBtnIds()) {
             Bulletin btn = instance.bulletins.get(ii);
             if (btn != null) {
                 btnList.add(btn);
             }
         }
 
         return btnList;
     }
 
     public static List<Bulletin> getBulletinsIn(int bldid) {
         //TODO: Check if bulletins are missing
         Building bld = instance.buildings.get(bldid);
 
         if (bld == null) return null;
 
         return getBulletinsIn(bld);
     }
 
     public static String getSummaryString() {
         String running = "";
 
         for (Building bld : buildings.values()) {
             running += (bld.getName() + "\n");
             for (Bulletin btn : instance.getBulletinsIn(bld)) {
                 running += ("   [" + btn.getBulletinId() + "] " +
                         btn.getTitle() + "\n");
             }
         }
 
         return running;
     }
 
     public static String getNiceCoords() {
         return (
                 Location.convert(Math.abs(instance.lat), Location.FORMAT_MINUTES) +
                         " " + (instance.lat >= 0 ? "N" : "S") + ", " +
                         Location.convert(Math.abs(instance.lon), Location.FORMAT_MINUTES) +
                         " " + (instance.lon >= 0 ? "E" : "W"));
     }
 
 
     public static boolean wantNearbyBuildings(ParentFragment hungryTab) {
         return instance.wantAllBuildings(hungryTab);
     }
 
 
     public static boolean wantCurrentBuilding(ParentFragment hungryTab) {
         tabForCurrent = hungryTab;
 
         if(curBld != null)
         {
             if((hungryTab.getBldList().isEmpty()) ||
                (hungryTab.getBldList().get(0) != curBld))
             {
                 hungryTab.getBldList().clear();
                 hungryTab.getBldList().add(curBld);
             }
             return true;
         }
         else if (!buildings.isEmpty())
         {
             if((hungryTab.getBldList().isEmpty()) ||
                (hungryTab.getBldList().get(0) != buildings.values().iterator().next()))
             {
                 hungryTab.getBldList().clear();
                 hungryTab.getBldList().add(buildings.values().iterator().next());
             }
             return true;
         }
         else
         {
             return wantAllBuildings(hungryTab);
         }
     }
 
 
     public static boolean wantAllBuildings(ParentFragment hungryTab) {
         if(buildings.isEmpty())
         {
             EverythingRequest er =
                     new EverythingRequest(hungryTab.dataArrivedFunc, instance.baseurl);
             er.send();
             return false;
         }
         else
         {
             hungryTab.getBldList().addAll(buildings.values());
             return true;
         }
     }
 
     public static Building getCurBld()
     {
         return curBld;
     }
 
     public static Bitmap getFileForBulletin(int btnid)
     {
         return instance.files.get(btnid);
     }
 
     public static void loadEverything()
     {
         EverythingRequest er = new EverythingRequest(instance.notifyMain, instance.baseurl);
         er.send();
     }
 
     private static String dummy;
     private static FunctionObj<ServerResponse> notifyMain;
     private static double lat;
     private static double lon;
     private static Rectangle bounds;
     private static Map<Integer, Bulletin> bulletins;
     private static Map<Integer, Building> buildings;
     private static Map<Integer, Bitmap> files;
     private static Building curBld;
     private static ParentFragment tabForCurrent;
 }
 
