 package com.moac.android.soundmap;
 
 import android.app.Application;
 import android.util.Log;
 import com.android.volley.RequestQueue;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.moac.android.soundmap.api.ApiClient;
 import com.moac.android.soundmap.model.GeoLocation;
 import com.moac.android.soundmap.model.GeoLocationDeserializer;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 public class SoundMapApplication extends Application {
 
     private static final String TAG = SoundMapApplication.class.getSimpleName();
 
     private static SimpleVolley sVolley;
     private static ApiClient sApiClient;
     private static Gson sGson =  new GsonBuilder().registerTypeAdapter(GeoLocation.class, new GeoLocationDeserializer()).create();
 
     @Override
     public void onCreate() {
         Log.i(TAG, "onCreate() - start");
         super.onCreate();
         sVolley = initVolley();
         sApiClient = initApiClient(sVolley.getRequestQueue(), sGson);
     }
 
     public static ApiClient getApiClient() { return sApiClient; }
     public static SimpleVolley getVolley() { return sVolley; }
 
     private SimpleVolley initVolley() {
         return new SimpleVolley(getApplicationContext());
     }
 
     private ApiClient initApiClient(RequestQueue _requestQueue, Gson _gson) {
        Log.i(TAG, "initApiClient() - start");
 
         InputStream inputStream = null;
         try {
             inputStream = getAssets().open("soundcloud.properties");
             Properties properties = new Properties();
             properties.load(inputStream);
 
             String apiScheme = properties.getProperty("host.scheme");
             String apiDomain = properties.getProperty("host.domain");
             String clientId = properties.getProperty("client.id");
             String clientSecret = properties.getProperty("client.secret");
            Log.i(TAG, "initApiClient() - creating with clientId: " + clientId + " clientSecret: " + clientSecret);
 
             return new ApiClient(_requestQueue, _gson, apiScheme, apiDomain, clientId);
         } catch(IOException e) {
             Log.e(TAG, "Failed to initialise API Client", e);
             throw new RuntimeException("Unable to initialise API Client");
         } finally {
             safeClose(inputStream);
         }
     }
 
     private void safeClose(InputStream _stream) {
         if(_stream != null) {
             try {
                 _stream.close();
             } catch(IOException e) { }
         }
     }
 }
