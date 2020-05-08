 package com.kii.push;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import android.content.Context;
 import android.util.Log;
 
 public class PropertyManager {
 
     private static final String TAG = "PropertyManager";
     private boolean loaded = false;
     private PropertyManager() {
     }
 
     private static PropertyManager INSTANCE = new PropertyManager();
 
     public static PropertyManager getInstance() {
         return INSTANCE;
     }
 
     public synchronized void load(Context ctx) {
         if (this.loaded)
             return;
         Properties p = System.getProperties();
         BufferedInputStream in = null;
         try {
             in = new BufferedInputStream(ctx.getResources().getAssets()
                     .open("app.properties"));
             p.load(in);
             this.loaded = true;
         } catch (IOException e) {
             throw new RuntimeException("failed to read prop file", e);
         } finally {
             if (in !=null) {
                 try {
                     in.close();
                 } catch (IOException e) {
                 }
             }
         }
     }
 
     public String getAppId() {
         Properties p = System.getProperties();
         String value = p.getProperty("app-id");
         Log.v(TAG, "App id: " + value);
         return value;
     }
 
     public String getAppKey() {
         Properties p = System.getProperties();
         String value = p.getProperty("app-key");
         Log.v(TAG, "App key: " + value);
         return value;
     }
 
     public String getBaseUri() {
         Properties p = System.getProperties();
         String host = p.getProperty("host");
         String value =  "https://" + host + "/api";
         Log.v(TAG, "Base uri: " + value);
         return value;
 
     }
 
     public String getAppTopicName() {
         Properties p = System.getProperties();
         String value =  p.getProperty("app-topic-name");
         Log.v(TAG, "App topic name: " + value);
         return value;
     }
 
     public String getAppBucketName() {
         Properties p = System.getProperties();
         String value =  p.getProperty("app-bucket-name");
         Log.v(TAG, "App topic name: " + value);
         return value;
     }
 
    public String getGCMSenderId() {
        Properties p = System.getProperties();
        String value =  p.getProperty("gcm-sender-id");
        Log.v(TAG, "GCM sender id: " + value);
        return value;
    }

 }
