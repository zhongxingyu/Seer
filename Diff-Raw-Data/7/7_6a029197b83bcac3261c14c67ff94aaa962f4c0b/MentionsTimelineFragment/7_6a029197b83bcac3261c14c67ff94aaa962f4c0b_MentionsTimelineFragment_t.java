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
 
 public class MentionsTimelineFragment extends TweetListFragment{
 
 	/**
 	 * Gets and persists the tweets based on request parameters. Applies
 	 * UI treatment to scroll top and reset the refresh spinner
 	 *  
 	 * @param rparams
 	 * @param overWriteLocal
 	 * @param resetView
 	 */
 	@Override
 	protected void getHomeTimeline(RequestParams rparams, final boolean overWriteLocal, final boolean resetView){	
 		//get timeline feed
 		MyTwitterApp.getRestClient().getMentionsTimeline(rparams, new JsonHttpResponseHandler() {
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
