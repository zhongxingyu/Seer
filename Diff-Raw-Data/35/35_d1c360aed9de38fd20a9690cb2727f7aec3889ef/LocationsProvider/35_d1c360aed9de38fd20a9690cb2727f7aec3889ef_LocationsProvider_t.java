 package com.cruzroja.android.app.utils;
 
 import android.content.ContentResolver;
 import android.database.Cursor;
 import android.net.Uri;
 import android.util.Log;
 import com.cruzroja.android.app.Location;
 import com.cruzroja.android.app.LoginResponse;
 import com.cruzroja.android.database.CreuRojaContract;
 import org.apache.http.HttpResponse;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by lapuente on 14.01.14.
  */
 public class LocationsProvider {
     public static List<Location> getLocationList(HttpResponse response) {
         List<Location> locationList = null;
         BufferedReader reader;
         StringBuilder builder = new StringBuilder();
         try {
             reader = new BufferedReader(new InputStreamReader(
                     response.getEntity().getContent()));
             String line;
             while ((line = reader.readLine()) != null) {
                 builder.append(line);
             }
 
             locationList = getLocationList(new JSONObject(builder.toString()));
         } catch (IOException e) {
             //Nothing to do, still return the empty list
         } catch (JSONException e) {
             Log.d("RESPONSE", builder.toString());
         }
 
         return locationList;
     }
 
     public static List<Location> getLocationList(JSONObject object) throws JSONException {
         ArrayList<Location> locationList = new ArrayList<>();
         JSONArray locations = object.getJSONArray(LoginResponse.sLocations);
         for (int i = 0; i < locations.length(); i++) {
            locationList.add(new Location(locations.getJSONObject(i)));
         }
         return locationList;
     }
 
     public static List<Location> getLocationList(Cursor cursor) {
         ArrayList<Location> locationsList = new ArrayList<Location>();
         if (cursor.moveToFirst()) {
             do {
                 locationsList.add(new Location(cursor));
             } while (cursor.moveToNext());
         }
         return locationsList;
     }
 
     public static List<Location> getCurrentLocations(ContentResolver cr){
         Uri uri = CreuRojaContract.Locations.CONTENT_LOCATIONS;
         return getLocationList(cr.query(uri, null, null, null, null));
     }
 
     public static void checkExpiredLocations(final ContentResolver cr) {
         new Thread(new Runnable() {
             @Override
             public void run() {
                String where = CreuRojaContract.Locations.EXPIRE_DATE + "<? AND "
                        + CreuRojaContract.Locations.EXPIRE_DATE + ">0";
                 String[] whereArgs = {
                         Long.toString(System.currentTimeMillis())
                 };
                 cr.delete(CreuRojaContract.Locations.CONTENT_LOCATIONS,
                     where, whereArgs);
 
             }
         }).start();
     }
 }
