 package com.github.sgdesmet.android.utils.service;
 
 import android.app.IntentService;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.ResultReceiver;
 import android.util.Log;
 import com.google.gson.*;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.*;
 
 
 /**
  * IntentService wrapper for SimpleRestJSON. IntentService and ResultReceiver provide background threading and callbacks.
  *
  * @author: sgdesmet
  */
 public class RestService extends IntentService {
 
     private static final String TAG  = RestService.class.getSimpleName();
     private static final String BASE = RestService.class.getName();
 
     //constants for intent extra data
     public static final String EXTRA_CALLBACK = BASE + ".Callback";
 
     public static final String ACTION_GET    = BASE + ".Get";
     public static final String ACTION_POST   = BASE + ".Create";
     public static final String ACTION_PUT    = BASE + ".Update";
     public static final String ACTION_DELETE = BASE + ".Delete";
 
     public static final String CONTENT       = BASE + ".Content";
     public static final String RESPONSE_TYPE = BASE + ".ResponseType";
 
     public static final String BASIC_AUTH_USERNAME = BASE + ".Username";
     public static final String BASIC_AUTH_PASSWORD = BASE + ".Password";
    public static final String QUERY_PARAMS        = BASE + ".QueryParams";
    public static final String FORM_PARAMS         = BASE + ".FormParams";
     public static final String HEADERS             = BASE + ".Headers";
     public static final String TIMEOUT             = BASE + ".Timeout";
 
     public static final String RESULT                 = BASE + ".Result";
     public static final String HTTP_STATUS            = BASE + ".HttpStatus";
     public static final String RESULT_ORIGINAL_INTENT = BASE + ".ResultIntent";
 
     protected static Gson gson = null;
 
     public RestService() {
 
         super( TAG );
     }
 
     @Override
     public void onCreate() {
 
         super.onCreate();
 
         Log.d( TAG, "Service created" );
     }
 
     @Override
     protected void onHandleIntent(Intent intent) {
 
         //fetch arguments to rest call
         //TODO: add timeout as configurable
         Log.d( TAG, "received intent: " + intent );
         Bundle extras = intent.getExtras();
         String action = intent.getAction();
         Uri data = intent.getData();
         String url = data.toString();
         if (extras == null || action == null || url == null || !extras.containsKey( EXTRA_CALLBACK )) {
             Log.e( TAG, "You did not pass the necessary params with the Intent." );
             return;
         }
         ResultReceiver callback = extras.getParcelable( EXTRA_CALLBACK );
         RestResource.Callback resultHandler = getResultHandler( callback, intent );
         Serializable content = extras.getSerializable( CONTENT );
         Class responseType = (Class) extras.getSerializable( RESPONSE_TYPE );
         //form params
         Bundle formBundle = extras.getBundle( FORM_PARAMS );
         HashMap<String, String> formParams = new HashMap<String, String>();
         if (formBundle != null)
             for (String key : formBundle.keySet()) {
                 formParams.put( key, formBundle.getString( key ) );
             }
 
         //query params
         Bundle queryBundle = extras.getBundle( QUERY_PARAMS );
         HashMap<String, String> queryParams = new HashMap<String, String>();
         if (queryBundle != null)
             for (String key : queryBundle.keySet()) {
                 queryParams.put( key, queryBundle.getString( key ) );
             }
         Bundle headerBundle = extras.getBundle( HEADERS );
         //custom headers
         HashMap<String, String> headers = new HashMap<String, String>();
         if (headerBundle != null)
             for (String key : headerBundle.keySet()) {
                 headers.put( key, headerBundle.getString( key ) );
             }
         //auth
         String username = extras.getString( BASIC_AUTH_USERNAME );
         String password = extras.getString( BASIC_AUTH_PASSWORD );
 
         RestResource resource = RestResource.build().gson( getGson() ).url( url ).query( queryParams ).headers( headers ).form( formParams );
         if (username != null || password != null) {
             resource.basicAuth( username, password );
         }
         if (extras.containsKey( TIMEOUT ))
             resource.timeout( extras.getInt( TIMEOUT ) );
         configure( resource );
 
         try {
             if (action.equals( ACTION_GET )) {
                 resource.get( content, responseType, resultHandler );
             } else if (action.equals( ACTION_POST )) {
                 resource.post( content, responseType, resultHandler );
             } else if (action.equals( ACTION_DELETE )) {
                 resource.delete( content, responseType, resultHandler );
             } else if (action.equals( ACTION_PUT )) {
                 resource.put( content, responseType, resultHandler );
             } else {
                 Log.e( TAG, "Unknown action received!" );
             }
         }
         catch (IOException e) {
             Log.e( TAG, "I/O Error: " + e, e );
             Bundle bundle = new Bundle();
             bundle.putParcelable( RESULT_ORIGINAL_INTENT, intent );
             bundle.putString( RESULT, "I/O Error: " + e );
             callback.send( -1, bundle );
         }
     }
 
     protected void configure(RestResource rest) {
         //override if necessary
     }
 
     protected Gson getGson() {
         //override if necessary
         if (gson == null) {
             GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().setDateFormat( "yyyy-MM-dd HH:mm:ss Z" );
             gson = gsonBuilder.create();
         }
         return gson;
     }
 
     protected RestResource.Callback getResultHandler(ResultReceiver callback, Intent intent) {
 
         return new RestServiceCallback<Serializable>( callback, intent );
     }
 
     protected class RestServiceCallback<R extends Serializable> implements RestResource.Callback<R> {
 
         protected final ResultReceiver callback;
         protected final Intent         originalIntent;
 
         protected RestServiceCallback(ResultReceiver callback, Intent originalIntent) {
 
             this.callback = callback;
             this.originalIntent = originalIntent;
         }
 
         public ResultReceiver getCallback() {
 
             return callback;
         }
 
         protected Bundle getBundle() {
 
             Bundle bundle = new Bundle();
             bundle.putParcelable( RESULT_ORIGINAL_INTENT, originalIntent );
             return bundle;
         }
 
         protected String getOriginalIntentString() {
 
             if (originalIntent != null)
                 return "Intent: " + originalIntent.getAction() + " " + originalIntent.getData() + " (" + originalIntent.getExtras() + ")";
             return "Request data not available (intent is null)";
         }
 
         private Bundle toBundle(final Map<String, List<String>> headers) {
 
             Bundle bundle = new Bundle();
             for (String key : headers.keySet()) {
                 String value = "";
                 for (String headerValue : headers.get( key )) {
                     value += headerValue + ",";
                 }
                 value = value.substring( 0, Math.max( 0, value.length() - 1 ) );
                 bundle.putString( key, value );
             }
             return bundle;
         }
 
         @Override
         public void onResponse(final R data, final int status, final Map<String, List<String>> headers) {
 
             Bundle bundle = getBundle();
             bundle.putSerializable( RESULT, data );
             bundle.putBundle( HEADERS, toBundle( headers ) );
             callback.send( status, bundle );
         }
 
         @Override
         public void onResponse(final String errorData, final int status, final Map<String, List<String>> headers) {
 
             Bundle bundle = getBundle();
             bundle.putSerializable( RESULT, errorData );
             bundle.putBundle( HEADERS, toBundle( headers ) );
             callback.send( status, bundle );
         }
     }
 }
