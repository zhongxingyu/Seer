 package net.tensory.apps.icantbelieveitsnottwitter.fragments;
 
 import java.util.ArrayList;
 
 import net.tensory.apps.icantbelieveitsnottwitter.EndlessScrollListener;
 import net.tensory.apps.icantbelieveitsnottwitter.R;
 import net.tensory.apps.icantbelieveitsnottwitter.TweetsAdapter;
 import net.tensory.apps.icantbelieveitsnottwitter.TwitterClientApp;
 import net.tensory.apps.icantbelieveitsnottwitter.models.Tweet;
 
 import org.json.JSONArray;
 
 import com.loopj.android.http.JsonHttpResponseHandler;
 
import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.ListView;
 
 public class HomeTimelineFragment extends TweetsListFragment {
 	private TweetsListFragment fragmentTweets;
 	private TimelineJsonHttpResponseHandler tweetRequestHandler;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		 
 		ListView lv = (ListView) getActivity().findViewById(R.id.lvTweets);
 		
 		fragmentTweets = (TweetsListFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.frameLayout);
 		
		tweetRequestHandler = new TimelineJsonHttpResponseHandler(getActivity()) {
 			@Override
 			public void onSuccess(JSONArray jsonTweets) {
 				jsonTweets = TweetsListFragment.sanitizeStream(jsonTweets);
 				ArrayList<Tweet> tweets = Tweet.fromJson(jsonTweets);
 				fragmentTweets.getAdapter().addAll(tweets);
 				// Hook up the fragment's listView to this instance of the scroll handler
 				this.setListView(fragmentTweets.getListView());
 			}
 		};
 		
 		ArrayList<Tweet> tweets = Tweet.getAll();
 		try {
 			if (tweets.isEmpty()) {
 				throw new Exception("Not enough tweets");
 			}
 			TweetsAdapter tweetsAdapter = new TweetsAdapter(getActivity(), tweets);
 			lv.setAdapter(tweetsAdapter);
 			tweetRequestHandler.setListView(lv);
 		} catch (Exception e) {
 			TwitterClientApp.getRestClient().getHomeTimeline(tweetRequestHandler);			
 		}
 	}
 }
