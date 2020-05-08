 package whoshuu.twitteractivity;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 
 import whoshuu.twitteractivity.models.Tweet;
 import whoshuu.twitteractivity.models.User;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 import com.activeandroid.query.Select;
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 import eu.erikw.PullToRefreshListView;
 import eu.erikw.PullToRefreshListView.OnRefreshListener;
 
 public class TimelineActivity extends Activity {
 	private static final int COMPOSE_CODE = 0;
 	private TweetAdapter tweetsAdapter;
 	private PullToRefreshListView lvTweets;
 	private long max_id;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_timeline);
 		max_id = -1;
 		lvTweets = (PullToRefreshListView) findViewById(R.id.lvTweets);
 		tweetsAdapter = new TweetAdapter(TimelineActivity.this, new ArrayList<Tweet>());
 		lvTweets.setAdapter(tweetsAdapter);
 		
 		
 		lvTweets.setOnScrollListener(new EndlessScrollListener() {
 		    @Override
 		    public void onLoadMore(int page, int totalItemsCount) {
 		        updateTweets();
 		    }
 	    });
 		
 		lvTweets.setOnRefreshListener(new OnRefreshListener() {
 	        @Override
 	        public void onRefresh() {
 	        	tweetsAdapter.clear();
 	        	ArrayList<Tweet> dbTweets = new Select()
 	        										.from(Tweet.class)
 	        										.execute();
 	        	for (Tweet tweet : dbTweets) {
 	        		tweet.delete();
 	        	}
 	        	ArrayList<User> dbUsers = new Select()
 													.from(User.class)
 													.execute();
 				for (User user : dbUsers) {
 					user.delete();
 				}
 	        	max_id = -1;
 	        }
 	    });
 	}
 
 	public boolean composeTweet(MenuItem menu) {
         Intent i = new Intent(this, ComposeActivity.class);
         startActivityForResult(i, COMPOSE_CODE);
         return false;
     }
 	
 	private void updateTweets() {
 		ArrayList<Tweet> dbTweets;
 		if (max_id >= 0) {
 			dbTweets = new Select().from(Tweet.class)
 								   .where("tid <= ?", max_id)
 								   .orderBy("tid DESC")
 								   .limit("10")
 								   .execute();
 		} else {
 			dbTweets = new Select().from(Tweet.class)
 								   .orderBy("tid DESC")
 								   .limit("10")
 								   .execute();
 			if (dbTweets.size() != 0) {
 				max_id = dbTweets.get(dbTweets.size() - 1).tid - 1;
 			}
 		}
 		if (dbTweets.size() < 10) {
 			if (dbTweets.size() != 0) {
 				tweetsAdapter.addAll(dbTweets);
 			}
 			TwitterClientApp.getRestClient().getHomeTimeline(new JsonHttpResponseHandler() {
 				@Override
 				public void onSuccess(JSONArray jsonTweets) {
 					Log.d("DEBUG", jsonTweets.toString());
 					ArrayList<Tweet> results = Tweet.fromJson(jsonTweets);
 					tweetsAdapter.addAll(results);
 					max_id = results.get(results.size() - 1).tid - 1;
 					lvTweets.onRefreshComplete();
 				}
 				
 				@Override
 				public void onFailure(Throwable e) {
 					Toast.makeText(TimelineActivity.this, "Failed to retrieve tweets", Toast.LENGTH_LONG).show();
 				}
				
				@Override
				protected void handleFailureMessage(Throwable e, String responseBody) {
					Toast.makeText(TimelineActivity.this, "Currently being rate limited", Toast.LENGTH_LONG).show();
				}
 			}, max_id, 10 - dbTweets.size());
 		} else {
 			tweetsAdapter.addAll(dbTweets);
 			max_id = dbTweets.get(dbTweets.size() - 1).tid - 1;
 			lvTweets.onRefreshComplete();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.timeline, menu);
 		return true;
 	}
 	
 	@SuppressLint("NewApi")
 	protected void onActivityResult(int rq, int rs, Intent data) {
 		if (rq == COMPOSE_CODE) {
 			if (rs == RESULT_OK) {
 				Tweet tweet = (Tweet) data.getSerializableExtra("tweet");
 				tweetsAdapter.insert(tweet, 0);
 				lvTweets.setSelection(0);
 			}
 		}
 	}
 
 }
