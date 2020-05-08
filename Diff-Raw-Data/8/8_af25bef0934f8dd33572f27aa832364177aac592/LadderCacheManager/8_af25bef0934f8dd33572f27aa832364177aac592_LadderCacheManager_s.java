 package com.libcorp.shootmaniacenter.utilities.cache;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * Created by Jacopo on 02/07/13.
  */
 public class LadderCacheManager {
 
 
     static public String loadFromCache(Activity activity, String param)
     {
         if(activity != null)
         {
         SharedPreferences preferences = activity.getSharedPreferences("Ladders_Cache", Context.MODE_PRIVATE);
         return preferences.getString(param, null);
         }
         return null;
     }
 
     static public void saveToCache(Activity activity, String param, String data)
     {
         if(activity != null)
         {
         SharedPreferences preferences = activity.getSharedPreferences("Ladders_Cache", Context.MODE_PRIVATE);
         SharedPreferences.Editor editor = preferences.edit();
         editor.putString(param, data);
         editor.apply();
         updateData(activity, param);
         }
     }
 
     static public boolean hasToUpdate(Activity activity, String param){
         if(activity != null)
         {
         SharedPreferences preferences = activity.getSharedPreferences("Ladders_Cache", Context.MODE_PRIVATE);
         if(preferences.getString("lastUpdate_" + param, null) != null)
         {
                 SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
 
             try {
                Date date = sdf.parse(preferences.getString("lastUpdate_" + param, null));
                if(new Date(Calendar.getInstance().getTime().getTime() - date.getTime()).getHours() > 24)
                     return true;
                 else
                     return false;
             } catch (ParseException e) {
                 e.printStackTrace();
                 return true;
             }
         }
         }
         return true;
     }
 
     static public void updateData(Activity activity, String param)
     {
         if(activity != null)
         {
         SharedPreferences preferences = activity.getSharedPreferences("Ladders_Cache", Context.MODE_PRIVATE);
         SharedPreferences.Editor editor = preferences.edit();
 
         SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
         Calendar calendar = Calendar.getInstance();
         Date time = calendar.getTime();
         editor.putString("lastUpdate_" + param, sdf.format(time));
         editor.putString("lastUpdate", sdf.format(time));
         editor.apply();
         }
     }
 
 }
