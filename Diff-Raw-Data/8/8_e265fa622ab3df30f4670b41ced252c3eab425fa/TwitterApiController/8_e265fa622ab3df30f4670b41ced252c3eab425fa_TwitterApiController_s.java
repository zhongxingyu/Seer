 package com.eldridge.twitsync.controller;
 
 import android.content.Context;
 import android.util.Log;
 
 import com.crashlytics.android.Crashlytics;
 import com.eldridge.twitsync.BuildConfig;
 import com.eldridge.twitsync.db.Tweet;
 import com.eldridge.twitsync.message.beans.ConversationMessage;
 import com.eldridge.twitsync.message.beans.DirectMessagesMessage;
 import com.eldridge.twitsync.message.beans.ErrorMessage;
 import com.eldridge.twitsync.message.beans.MentionsMessage;
 import com.eldridge.twitsync.message.beans.TimelineUpdateMessage;
 import com.eldridge.twitsync.message.beans.TweetMessage;
 import com.eldridge.twitsync.message.beans.TwitterUserMessage;
 import com.eldridge.twitsync.rest.endpoints.StatusEndpoint;
 import com.eldridge.twitsync.rest.endpoints.payload.StatusUpdatePayload;
 import com.eldridge.twitsync.util.Utils;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import retrofit.Callback;
 import retrofit.RestAdapter;
 import retrofit.RetrofitError;
 import retrofit.client.Response;
 import twitter4j.DirectMessage;
 import twitter4j.Paging;
 import twitter4j.RelatedResults;
 import twitter4j.ResponseList;
 import twitter4j.Status;
 import twitter4j.StatusUpdate;
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
 
    private static final int COUNT = 20;
     private static final int HISTORY_COUNT = 50;
     private static final int MAX_PAGE_NUMBER = 3;
 
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
 
     public void sendTweet(final String text) {
         executorService.execute(new Runnable() {
             @Override
             public void run() {
                 try {
                     StatusUpdate statusUpdate = new StatusUpdate(text);
                     Status update = twitter.updateStatus(statusUpdate);
                     BusController.getInstance().postMessage(new TweetMessage(true, update));
                 } catch (TwitterException te) {
                     Log.e(TAG, "", te);
                     BusController.getInstance().postMessage(new TweetMessage(false, te));
                 }
             }
         });
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
 
     public synchronized void getUserTimeLineFromTwitter() {
         executorService.execute(new Runnable() {
             @Override
             public void run() {
                 boolean keepFetching = true;
                 int pageNumber = 1;
                 Paging paging = null;
                 try {
                     paging = new Paging(pageNumber, COUNT);
                     ResponseList<Status> tweets = getPagedTweets(paging);
                     if (tweets.isEmpty()) {
                         keepFetching = false;
                         Log.d(TAG, "*** No tweets returned ***");
                     }
                     CacheController.getInstance(context).addToCache(tweets, false);
                     Log.d(TAG, "**** Fetching PageNumber: " + pageNumber + " ****");
                     while (keepFetching) {
                         if (pageNumber < MAX_PAGE_NUMBER ) {
                             pageNumber++;
                             Log.d(TAG, "**** Fetching PageNumber: " + pageNumber + " ****");
                             paging = new Paging(pageNumber, COUNT);
                             //Have to do this hack to update the in mem cache in real time because of limitation of Twitter4J not
                             //being able to get access to the raw json after things are added to the original tweets list
                             ResponseList<Status> moreTweets = getPagedTweets(paging);
                             if (moreTweets.isEmpty()) {
                                 keepFetching = false;
                                 Log.d(TAG, "*** No [MORE] tweets returned ***");
                             }
                             tweets.addAll(moreTweets);
                             CacheController.getInstance(context).addToCache(moreTweets, false);
                         } else {
                             keepFetching = false;
                         }
                     }
 
                     updateServerWithLatestMessage(tweets);
                     BusController.getInstance().postMessage(new TimelineUpdateMessage(tweets, true));
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
                 boolean keepFetching = true;
                 int pageNumber = 1;
                 Paging paging = null;
                 ArrayList<Tweet> items = new ArrayList<Tweet>();
                 try {
                    paging = new Paging(pageNumber, 5, statusId);
                     ResponseList<Status> tweets = getPagedTweets(paging);
                     Log.d(TAG, "**** [Refresh] Fetching PageNumber: " + pageNumber + " ****");
                     if (tweets.isEmpty()) {
                         Log.d(TAG, "**** [Refresh] Fetching PageNumber: " + pageNumber + " returned no new tweets! ****");
                         keepFetching = false;
                     } else {
                         Log.d(TAG, "**** Adding " + tweets.size() + " to cache [PageNumber: 1] ****");
                         for (Status s : tweets) {
                             items.add(CacheController.getInstance(context).createTweetObject(s));
                         }
                     }
                     while (keepFetching) {
                         pageNumber++;
                        paging = new Paging(pageNumber, 5, statusId);
                         Log.d(TAG, "**** [Refresh] Fetching PageNumber: " + pageNumber + " ****");
                         ResponseList<Status> moreTweets = getPagedTweets(paging);
                         if (moreTweets.isEmpty()) {
                             keepFetching = false;
                             Log.d(TAG, "*** [Refresh] PageNumber: " + pageNumber + " No [MORE] tweets returned ***");
                         } else {
                             Log.d(TAG, "**** Adding " + moreTweets.size() + " to base tweets list ****");
                             for (Status s : moreTweets) {
                                 items.add(CacheController.getInstance(context).createTweetObject(s));
                                 tweets.add(s);
                             }
                         }
                     }
                     Collections.reverse(tweets);
                     CacheController.getInstance(context).addToCache(items, true);
                     updateServerWithLatestMessage(tweets);
                     BusController.getInstance().postMessage(new TimelineUpdateMessage(tweets, true, true));
                 } catch (TwitterException te) {
                     Log.e(TAG, "", te);
                     Crashlytics.log("Exception refreshing timeLine");
                     Crashlytics.logException(te);
                     BusController.getInstance().postMessage(new ErrorMessage(te.getMessage(), GET_USER_TIMELINE_ERROR_CODE));
                 }
             }
         });
     }
 
     /**
      * NOTE: This is not threaded so it should only be called from a background thread.
      * @param statusId
      * @return ResponseList<Status> tweets
      * @throws TwitterException
      */
     public ResponseList<Status> syncGetUserTimeLineHistory(final Long statusId) throws TwitterException {
         Paging paging = new Paging();
         paging.setMaxId(statusId);
         paging.setCount(HISTORY_COUNT);
         ResponseList<Status> tweets = getPagedTweets(paging);
         if (tweets != null && !tweets.isEmpty()) {
             tweets.remove(0);
         }
         CacheController.getInstance(context).addToCache(tweets, false);
         return tweets;
     }
 
     public void syncFromGcm(Long messageId) {
         Paging paging;
         boolean keepFetching = true;
         int pageNumber = 1;
         long sinceId = -1;
         boolean hasCache;
         try {
             List<Status> cachedTweets = CacheController.getInstance(context).getLatestCachedTweets();
             hasCache = cachedTweets != null && !cachedTweets.isEmpty();
             if (hasCache) {
                 sinceId = cachedTweets.get(0).getId();
             }
             paging = new Paging(pageNumber, COUNT, sinceId, messageId);
             ResponseList<Status> tweets = getPagedTweets(paging);
             if (tweets.isEmpty()) {
                 keepFetching = false;
             }
             if (hasCache) {
                 CacheController.getInstance(context).addToCache(tweets, true);
             } else {
                 CacheController.getInstance(context).addToCache(tweets, false);
             }
             Log.d(TAG, "**** Sync tweets from Server - PageNumber: " + pageNumber + " ****");
             while (keepFetching) {
                 pageNumber++;
                 Log.d(TAG, "**** Sync tweets from Server - PageNumber: " + pageNumber + " *****");
                 paging = new Paging(pageNumber, COUNT, sinceId, messageId);
                 ResponseList<Status> moreTweets = getPagedTweets(paging);
                 if (moreTweets.isEmpty()) {
                     keepFetching = false;
                     Log.d(TAG, "**** Sync tweets No [More] Tweets returned ****");
                 }
                 tweets.addAll(moreTweets);
                 if (hasCache) {
                     CacheController.getInstance(context).addToCache(moreTweets, true);
                 } else {
                     CacheController.getInstance(context).addToCache(moreTweets, false);
                 }
             }
             /*if (cachedTweets != null && !cachedTweets.isEmpty()) {
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
             }*/
 
             CacheController.getInstance(context).trimCache();
         } catch (Exception e) {
             Log.e(TAG, "Error updating Tweets from Gcm");
             Log.e(TAG, "", e);
         }
     }
 
     public void getRelatedResults(final long tweetId) {
         executorService.execute(new Runnable() {
             @Override
             public void run() {
                 try {
                     RelatedResults relatedResults = twitter.getRelatedResults(tweetId);
                     ResponseList<Status> conversation = relatedResults.getTweetsWithConversation();
                     BusController.getInstance().postMessage(new ConversationMessage(true, conversation));
                 } catch (TwitterException te) {
                     Log.e(TAG, "", te);
                     BusController.getInstance().postMessage(new ConversationMessage(false, te));
                 }
             }
         });
     }
 
     public void getDirectMessages() {
         executorService.execute(new Runnable() {
             @Override
             public void run() {
                 try {
                     List<DirectMessage> messages = twitter.getDirectMessages();
                     BusController.getInstance().postMessage(new DirectMessagesMessage(true, messages));
                 } catch (TwitterException te) {
                     Log.e(TAG, "", te);
                     BusController.getInstance().postMessage(new DirectMessagesMessage(false, te));
                 }
             }
         });
     }
 
     public void getMentions() {
         executorService.execute(new Runnable() {
             @Override
             public void run() {
                 try {
                     List<Status> mentions = twitter.getMentionsTimeline();
                     BusController.getInstance().postMessage(new MentionsMessage(true, mentions));
                 } catch (TwitterException te) {
                     Log.e(TAG, "", te);
                     BusController.getInstance().postMessage(new MentionsMessage(false, te));
                 }
             }
         });
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
