 package com.pg.marspg.fragments;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.R.array;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 
 import com.pg.marspg.OnTaskCompleted;
 import com.pg.marspg.R;
 import com.pg.marspg.RetrieveSiteData;
 import com.pg.marspg.twitter.Tweet;
 import com.pg.marspg.twitter.TweetItemAdapter;
 
 public class TwitterFeed extends Fragment {
 	private ArrayList<Tweet> curiosityFeed;
 	private ArrayList<Tweet> jplFeed;
 	
 	private String curiosityFeedURL = "http://search.twitter.com/search.json?q=@MarsCuriosity&rpp=15&page=1";
 	private String jplFeedURL = "http://search.twitter.com/search.json?q=@NASAJPL&rpp=15&page=1";
 	
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         // Inflate the layout for this fragment
     	View v = inflater.inflate(R.layout.twitter_feed, container, false);
     	
     	getTwitterFeeds( v);
         return v;
     }
     
     public ArrayList<Tweet> convertJsonToFeedArray(String JsonString) throws JSONException
     {
     	// Get Json
     	JSONObject head = new JSONObject(JsonString);
    	JSONObject test
     	JSONArray totalResults = head.getJSONArray("results");
     	
     	// Convert into tweet Object
     	ArrayList<Tweet> tweets = new ArrayList<Tweet>();
     	Tweet tempTweet;
     	JSONArray curResult;
     	String username, imageurl, text;
     	for(int i=0; i< totalResults.length(); i++)
     	{
     		curResult = totalResults.getJSONArray(i);
     		// username
     		username = curResult.optString( 1, "Nameless One");
     		// image
     		imageurl = curResult.optString( 10, "");
     		// text
     		text = curResult.optString( 13, "Empty");
     		
     		tempTweet = new Tweet( username, text, imageurl );
     		tweets.add( tempTweet );
     	}
     	
     	return tweets;
     }
     
     public void getTwitterFeeds( final View v)
     {
     	Log.e("getTwitterFeeds", "In getTwitterFeeds");
     	(new RetrieveSiteData(new OnTaskCompleted() {
 			@Override
 			public void onTaskCompleted(String result) {
 				try
 				{
 					
 					curiosityFeed = convertJsonToFeedArray(result);
 					popluateFeedListViews( v, curiosityFeed );			
 				}
 				catch(JSONException e)
 				{
 					// Dun Goofed
 					Log.e("Dun Goofed", e.toString());
 				}
 			}
 		})).execute(curiosityFeedURL);
     	/*
     	(new RetrieveSiteData(new OnTaskCompleted() {
 			@Override
 			public void onTaskCompleted(String result) {
 				try
 				{
 					jplFeed = convertJsonToFeedArray(result);
 				}
 				catch(JSONException e)
 				{
 					// Dun Goofed
 				}
 			}
 		})).execute(jplFeedURL);
 		*/
     }
     public void popluateFeedListViews( View v, ArrayList<Tweet> tweets)
     {
<<<<<<< HEAD
     	ListView listView = (ListView) v.findViewById(R.id.twitterfeed_ListView);
 		listView.setAdapter(new TweetItemAdapter(this.getActivity(), R.layout.listitem, tweets));
=======
//    	ListView listView = (ListView) findViewById(R.id.twitterfeed_ListView);
//		listView.setAdapter(new TweetItemAdapter(this.getActivity(), R.layout.listitem, tweets));
>>>>>>> e93ae5334af94f0cb3701388256005d69814650d
     }
 }
