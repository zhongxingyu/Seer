 package com.eldridge.twitsync.controller;
 
 import android.content.Context;
 import android.util.Log;
 
 import com.eldridge.twitsync.BuildConfig;
 import com.eldridge.twitsync.message.beans.ErrorMessage;
 import com.eldridge.twitsync.message.beans.TimelineUpdateMessage;
 import com.eldridge.twitsync.message.beans.TwitterUserMessage;
 import com.eldridge.twitsync.rest.endpoints.StatusEndpoint;
 import com.eldridge.twitsync.rest.endpoints.payload.StatusUpdatePayload;
 import com.eldridge.twitsync.util.Utils;
 
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import retrofit.Callback;
 import retrofit.RestAdapter;
 import retrofit.RetrofitError;
 import retrofit.client.Response;
 import twitter4j.Paging;
 import twitter4j.ResponseList;
 import twitter4j.Status;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.User;
 import twitter4j.auth.AccessToken;
 import twitter4j.conf.Configuration;
 import twitter4j.conf.ConfigurationBuilder;
 
 /**
  * Created by ryaneldridge on 8/4/13.
  */
 public class TwitterApiController {
 
     private static final String TAG = TwitterApiController.class.getSimpleName();
 
     private static TwitterApiController instance;
     private Context context;
     private Twitter twitter;
 
     public static final int GET_USER_INFO_ERROR_CODE = 2000;
     public static final int GET_USER_TIMELINE_ERROR_CODE = 2001;
 
     private static final int COUNT = 50;
 
     private static final int THREAD_POOL_SIZE = 20;
     private ExecutorService executorService;
 
 
     private TwitterApiController(Context context) {
         executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
 
         String accessToken = PreferenceController.getInstance(context).getAcessToken();
         String secret = PreferenceController.getInstance(context).getSecret();
 
         AccessToken at = new AccessToken(accessToken, secret);
 
         Configuration cb =
                 new ConfigurationBuilder()
                     .setJSONStoreEnabled(true)
                     .build();
         twitter = new TwitterFactory(cb).getInstance();
         twitter.setOAuthConsumer(TwitterRegisterController.CONSUMER_KEY, TwitterRegisterController.CONSUMER_SECRET);
         twitter.setOAuthAccessToken(at);
     }
 
     public static TwitterApiController getInstance(Context context) {
         if (instance == null) {
             synchronized (TwitterApiController.class) {
                 instance = new TwitterApiController(context);
                 instance.context = context;
             }
         }
         return instance;
     }
 
     public void getUserInfo() {
         executorService.execute(new Runnable() {
             @Override
             public void run() {
                 try {
                     User user = twitter.verifyCredentials();
                     PreferenceController.getInstance(context).setUserId(user.getId());
                     BusController.getInstance().postMessage(new TwitterUserMessage(user));
                 } catch (TwitterException te) {
                     Log.e(TAG, "", te);
                     BusController.getInstance().postMessage(new ErrorMessage(te.getMessage(), GET_USER_INFO_ERROR_CODE));
                 }
             }
         });
     }
 
     public void getUserTimeLine() {
         executorService.execute(new Runnable() {
             @Override
             public void run() {
                try {
                    List<Status> tweets = CacheController.getInstance(context).getLatestCachedTweets();
                    if (tweets != null && !tweets.isEmpty()) {
                        BusController.getInstance().postMessage(new TimelineUpdateMessage(tweets));
                    } else {
                        getUserTimeLineFromTwitter();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                    getUserTimeLineFromTwitter();
                }
             }
         });
     }
 
     public void getUserTimeLineFromTwitter() {
         executorService.execute(new Runnable() {
             @Override
             public void run() {
                 try {
                     Paging paging = new Paging();
                     paging.setCount(COUNT);
                     ResponseList<Status> tweets = getPagedTweets(paging);
                     updateServerWithLatestMessage(tweets);
                     BusController.getInstance().postMessage(new TimelineUpdateMessage(tweets, true));
                     CacheController.getInstance(context).addToCache(tweets, false);
                 } catch (TwitterException te) {
                     Log.e(TAG, "", te);
                     BusController.getInstance().postMessage(new ErrorMessage(te.getMessage(), GET_USER_TIMELINE_ERROR_CODE));
                 }
             }
         });
     }
 
     public void refreshUserTimeLine(final Long statusId) {
         executorService.execute(new Runnable() {
             @Override
             public void run() {
                 try {
                     Paging paging = new Paging();
                     paging.setSinceId(statusId);
                     ResponseList<Status> tweets = getPagedTweets(paging);
                     updateServerWithLatestMessage(tweets);
                     BusController.getInstance().postMessage(new TimelineUpdateMessage(tweets, true, true));
                     CacheController.getInstance(context).addToCache(tweets, true);
                 } catch (TwitterException te) {
                     Log.e(TAG, "", te);
                     BusController.getInstance().postMessage(new ErrorMessage(te.getMessage(), GET_USER_TIMELINE_ERROR_CODE));
                 }
             }
         });
     }
 
     //This is NOT THREADED so it must be called from a background thread.
     public ResponseList<Status> syncGetUserTimeLineHistory(final Long statusId) throws TwitterException {
         Paging paging = new Paging();
         paging.setMaxId(statusId);
         paging.setCount(COUNT);
         ResponseList<Status> tweets = getPagedTweets(paging);
         if (tweets != null && !tweets.isEmpty()) {
             tweets.remove(0);
         }
         CacheController.getInstance(context).addToCache(tweets, false);
         return tweets;
     }
 
     public void syncFromGcm(Long messageId) {
         try {
             List<Status> cachedTweets = CacheController.getInstance(context).getLatestCachedTweets();
             Paging paging = new Paging();
             if (cachedTweets != null && !cachedTweets.isEmpty()) {
                 Status s = cachedTweets.get(0);
                 paging.setSinceId(s.getId());
                 paging.setMaxId(messageId);
 
                 ResponseList<Status> tweets = getPagedTweets(paging);
                 CacheController.getInstance(context).addToCache(tweets, true);
 
             } else {
                 paging.setMaxId(messageId);
                 paging.setCount(COUNT);
                 ResponseList<Status> tweets = getPagedTweets(paging);
                 CacheController.getInstance(context).addToCache(tweets, false);
             }
 
             CacheController.getInstance(context).trimCache();
         } catch (Exception e) {
             Log.e(TAG, "Error updating Tweets from Gcm");
             Log.e(TAG, "", e);
         }
     }
 
     private ResponseList<Status> getPagedTweets(Paging paging) throws TwitterException {
         return  (paging != null) ? twitter.getHomeTimeline(paging) : twitter.getHomeTimeline();
     }
 
     private void updateServerWithLatestMessage(final ResponseList<Status> tweets) {
         executorService.execute(new Runnable() {
             @Override
             public void run() {
                 try {
                    //TODO: Fix race condition where user/device registration is not complete but we attempt to store the last message
                    //We should queue the request and replay it a configurable amount of times.
                    if (tweets != null && !tweets.isEmpty() && PreferenceController.getInstance(context).getRegistrationId().length() > 0) {
                         String deviceId = Utils.getUniqueDeviceId(context);
                         Long twitterId = PreferenceController.getInstance(context).getUserId();
                         Long messageId = tweets.get(0).getId();
 
                         RestAdapter restAdapter = RestController.getInstance(context).getRestAdapter();
                         StatusEndpoint statusEndpoint = restAdapter.create(StatusEndpoint.class);
                         statusEndpoint.statusUpdate(new StatusUpdatePayload(String.valueOf(twitterId), String.valueOf(messageId), deviceId),
                                 new Callback<Response>() {
                                     @Override
                                     public void success(Response response, Response response2) {
                                         if (BuildConfig.DEBUG) {
                                             Log.d(TAG, "** Response Status: " + response.getStatus() + " **");
                                         }
                                     }
 
                                     @Override
                                     public void failure(RetrofitError retrofitError) {
                                         Log.e(TAG, "** Status Update failed **", retrofitError.fillInStackTrace());
                                     }
                                 });
 
                     } else {
                         Log.d(TAG, "** No Tweets returned - so no need to update **");
                     }
                 } catch (Exception e) {
                     Log.e(TAG, "** Error updating server with latest message **", e);
                 }
             }
         });
     }
 
 }
