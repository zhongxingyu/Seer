 package com.vgershman.whocall.database;
 
 import android.app.Application;
 import android.content.Context;
 import com.vgershman.whocall.R;
 import com.vgershman.whocall.app.AppInfo;
 
 import java.util.HashMap;
 
 /**
  * Created with IntelliJ IDEA.
  * User: User
  * Date: 26.02.13
  * Time: 16:42
  * To change this template use File | Settings | File Templates.
  */
 public class FieldsNameStorage {
 
     private static HashMap<String,String>stringHashMap = new HashMap<String, String>();
 
 
     static {
         Context context = AppInfo.getContext();
         stringHashMap.put("name",context.getResources().getString(R.string.field_name));
         stringHashMap.put("phone",context.getResources().getString(R.string.field_phone));
         stringHashMap.put("description",context.getResources().getString(R.string.field_description));
         stringHashMap.put("type",context.getResources().getString(R.string.field_type));
         stringHashMap.put("email","email");
         stringHashMap.put("operator",context.getResources().getString(R.string.field_operator));
         stringHashMap.put("o_region",context.getResources().getString(R.string.field_oregion));
         stringHashMap.put("o_site",context.getResources().getString(R.string.field_osite));
         stringHashMap.put("worktime",context.getResources().getString(R.string.field_worktime));
         stringHashMap.put("url",context.getResources().getString(R.string.field_url));
        stringHashMap.put("count","count");
     }
 
 
     public static String getFieldName(String key){
         return stringHashMap.get(key);
     }
 
 }
