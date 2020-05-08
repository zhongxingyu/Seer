 package com.imaginea.android.sugarcrm;
 
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 
 import com.imaginea.android.sugarcrm.util.Util;
 
 import java.io.Serializable;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 /**
  * ServiceHelper
  * 
  * @author chander
  */
 public class ServiceHelper {
 
     public static void startService(Context context, Uri uri, String module, String[] projection,
                                     String sortOrder) {
         // send a notify command to the service
         Intent serviceIntent = new Intent(context, SugarService.class);
         serviceIntent.setData(uri);
         serviceIntent.putExtra(Util.COMMAND, R.id.get);
         serviceIntent.putExtra(RestUtilConstants.MODULE_NAME, module);
         serviceIntent.putExtra(Util.PROJECTION, projection);
         serviceIntent.putExtra(Util.SORT_ORDER, sortOrder);
         context.startService(serviceIntent);
     }
 
     public static void startServiceForDelete(Context context, Uri uri, String module, String beanId) {
         Intent serviceIntent = new Intent(context, SugarService.class);
         serviceIntent.setData(uri);
         Map<String, String> nameValuePairs = new LinkedHashMap<String, String>();
         nameValuePairs.put(RestUtilConstants.BEAN_ID, beanId);
        nameValuePairs.put(ModuleFields.DELETED, "1");
 
         serviceIntent.putExtra(Util.COMMAND, R.id.delete);
         serviceIntent.putExtra(RestUtilConstants.MODULE_NAME, module);
         serviceIntent.putExtra(RestUtilConstants.NAME_VALUE_LIST, (Serializable) nameValuePairs);
         context.startService(serviceIntent);
     }
 
     public static void startServiceForUpdate(Context context, Uri uri, String module,
                                     String beanId, Map<String, String> nameValueList) {
         Intent serviceIntent = new Intent(context, SugarService.class);
         serviceIntent.setData(uri);
 
         serviceIntent.putExtra(Util.COMMAND, R.id.update);
         serviceIntent.putExtra(RestUtilConstants.BEAN_ID, beanId);
         serviceIntent.putExtra(RestUtilConstants.MODULE_NAME, module);
         serviceIntent.putExtra(RestUtilConstants.NAME_VALUE_LIST, (Serializable) nameValueList);
         context.startService(serviceIntent);
     }
 
     public static void startServiceForInsert(Context context, Uri uri, String moduleName,
                                     Map<String, String> nameValueList) {
         Intent serviceIntent = new Intent(context, SugarService.class);
         serviceIntent.setData(uri);
 
         serviceIntent.putExtra(Util.COMMAND, R.id.insert);
         serviceIntent.putExtra(RestUtilConstants.MODULE_NAME, moduleName);
         serviceIntent.putExtra(RestUtilConstants.NAME_VALUE_LIST, (Serializable) nameValueList);
         context.startService(serviceIntent);
     }
 }
