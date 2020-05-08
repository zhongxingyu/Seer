 package com.codepath.apps.twitterapp.fragments;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 
 import android.os.Bundle;
 import android.util.Log;
 
 import com.codepath.apps.twitterapp.TwitterApp;
 import com.codepath.apps.twitterapp.models.Tweet;
 import com.codepath.apps.twitterapp.models.User;
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 public class UserTimelineFragment extends TweetsListFragment {
 
     private ArrayList<Tweet> tweets;
     private String userId;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
 	// TODO Auto-generated method stub
 	super.onCreate(savedInstanceState);
     }
     
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
 	super.onActivityCreated(savedInstanceState);
	//TODO: QUESTION: when I try to retrieve the param that was passed in through intent, should I retrieve it here or in onCreate()
	userId = getActivity().getIntent().getStringExtra("userId");
     }
     
     @Override
     public void getTweets(String max) {
 	// TODO Auto-generated method stub
 	
 	 TwitterApp.getRestClient().getUserTimeline(userId, max, new JsonHttpResponseHandler() {
 		@Override
 		public void onSuccess(JSONArray jsonTweets){
 		    addToAdapter(jsonTweets);
 		}
 	    
 		public void onFailure(Throwable e){
 		    Log.d("DEBUG", e.toString());
 		}
 	    });
     }
 
 }
