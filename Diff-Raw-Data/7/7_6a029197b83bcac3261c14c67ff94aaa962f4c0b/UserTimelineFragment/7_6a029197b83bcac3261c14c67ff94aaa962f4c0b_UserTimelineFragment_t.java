 package com.bootcamp.gattani.twitterapp.fragments;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.json.JSONArray;
 
 import android.util.Log;
 import android.widget.Toast;
 
 import com.bootcamp.gattani.twitterapp.MyTwitterApp;
 import com.bootcamp.gattani.twitterapp.models.Tweet;
 import com.loopj.android.http.JsonHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 
 public class UserTimelineFragment extends TweetListFragment {
 	
 	@Override
 	/**
 	 * Constructs the parameters to call the Twitter API with and UI treatment 
 	 * based on user action.
 	 * 
 	 * @param invoked
 	 */
 	public void getTweetsByInvoction(GET invoked){
 		RequestParams rparams = null;
 		boolean overWriteLocal = false;;
 		boolean resetPosition = true;
 		
 		switch(invoked){
 		case ON_LOAD:
 			if(screenName != null){
 				rparams = new RequestParams();
 				rparams.put("screen_name", screenName);				
 			}
 			overWriteLocal = false;
 			resetPosition = false;
 			break;
 			
 		case ON_SCROLL:
 			//construct the request params to fetch only older tweets
 			if(tweets != null && !tweets.isEmpty()){
 				rparams = new RequestParams();
 				rparams.put("max_id", String.valueOf(tweets.get(tweets.size() - 1).getTweetId()));
 			} 
 			
 			if(screenName != null){
 				rparams = rparams == null ? new RequestParams() : rparams;
 				rparams.put("screen_name", screenName);				
 			}
 			
 			//set overwrite to true
 			overWriteLocal = true;
 			resetPosition = false;
 			break;
 			
 		case ON_REFRESH:
 			//request only the tweets that are newer than the latest we have
 			if(tweets != null && !tweets.isEmpty()){
 				rparams = new RequestParams();
 				rparams.put("since_id", String.valueOf(tweets.get(0).getTweetId()));
 			} 
 			
 			if(screenName != null){
 				rparams = rparams == null ? new RequestParams() : rparams;
 				rparams.put("screen_name", screenName);				
 			}
 			
 			//set overwrite to true
 			overWriteLocal = true;
 			resetPosition = true;
 			break;
 		}		
 		
 		getHomeTimeline(rparams, overWriteLocal, resetPosition);
 
 	}
 	@Override
 	protected void getHomeTimeline(RequestParams rparams, final boolean overWriteLocal, final boolean resetView){	
 		//get timeline feed
 		MyTwitterApp.getRestClient().getUserTimeline(rparams, new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(JSONArray jsonTweets){
 				List<Tweet> newTweets = Tweet.fromJson(jsonTweets);
 				if(!tweets.containsAll(newTweets)){
 					tweets.addAll(newTweets);				
 				}
 				//Collections.sort(tweets);
 				Log.d("DEBUG", Arrays.deepToString(tweets.toArray()));
 				tweetLvAdapter.notifyDataSetChanged();
 				lvTweets.onRefreshComplete();
 				if(resetView){
 					//scroll to top
 					lvTweets.smoothScrollToPosition(0);
 				}
 			}
 			
 			@Override
 			public void onFailure(Throwable error, String content){
 				Toast.makeText(getActivity(), "Could not download tweets", Toast.LENGTH_SHORT).show();
 				tweets.clear();
 				tweetLvAdapter.notifyDataSetChanged();
 				lvTweets.onRefreshComplete();
 				return;
 			}
 		});				
 	}
 
 }
