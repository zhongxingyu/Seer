 package com.twormobile.mytravelasia.http;
 
 import android.app.IntentService;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.widget.Toast;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.twormobile.mytravelasia.db.MtaPhProvider;
 import com.twormobile.mytravelasia.model.Poi;
 import com.twormobile.mytravelasia.util.Log;
 
 /**
  * Calls the get feed webservice and saves the response to the content provider.
  *
  * @author avendael
  */
 public class FeedIntentService extends IntentService {
     private static final String TAG = FeedIntentService.class.getSimpleName();
 
     public FeedIntentService() {
         super(TAG);
     }
 
     /**
      * Creates an IntentService.  Invoked by your subclass's constructor.
      *
      * @param name Used to name the worker thread, important only for debugging.
      */
     public FeedIntentService(String name) {
         super(name);
     }
 
     @Override
     protected void onHandleIntent(Intent intent) {
         Log.d(TAG, "handling intent");
         FeedHttpClient.getFeeds(new AsyncHttpResponseHandler() {
             @Override
             public void onFailure(Throwable error, String content) {
                 Log.d(TAG, "response failed");
                 // TODO: Implement a proper UI level response handler. Toast from a service is bad.
                 Toast.makeText(FeedIntentService.this, "Error retrieving feeds", Toast.LENGTH_LONG).show();
             }
 
             @Override
             public void onSuccess(String content) {
                if (null == content) {
                    // TODO: Implement a proper UI level response handler. Toast from a service is bad.
                    Toast.makeText(FeedIntentService.this, "Error retrieving feeds", Toast.LENGTH_LONG).show();
                    return;
                }

                 Log.d(TAG, "response is " + content);
                 FeedResponse feedResponse = FeedHttpClient.getFeedGsonParser().fromJson(content, FeedResponse.class);
 
                 getContentResolver().delete(MtaPhProvider.POI_URI, null, null);
 
                 for (Poi poi : feedResponse.getFeeds()) {
                     Log.d(TAG, "poi name: " + poi.getName());
                     ContentValues values = new ContentValues();
 
                     values.put(Poi.RESOURCE_ID, poi.getResourceId());
                     values.put(Poi.NAME, poi.getName());
                     values.put(Poi.ADDRESS, poi.getAddress());
                     values.put(Poi.CONTENT, poi.getContent());
                     values.put(Poi.FB_USER_PROFILE_ID, poi.getFbUserProfileId());
                     values.put(Poi.FB_USER_PROFILE_NAME, poi.getFbUserProfileName());
                     values.put(Poi.CREATED_AT, poi.getCreatedAt());
                     values.put(Poi.LONGITUDE, poi.getLongitude());
                     values.put(Poi.LATITUDE, poi.getLatitude());
                     values.put(Poi.FEED_TYPE, poi.getFeedType());
                     values.put(Poi.POI_TYPE, poi.getPoiType());
                     values.put(Poi.IMAGE_THUMB_URL, poi.getImageThumbUrl());
                     values.put(Poi.ANNOTATION_TYPE, poi.getAnnotationType());
                     values.put(Poi.TOTAL_COMMENTS, poi.getTotalComments());
                     values.put(Poi.TOTAL_LIKES, poi.getTotalLikes());
 
                     getContentResolver().insert(MtaPhProvider.POI_URI, values);
                 }
             }
         });
     }
 }
