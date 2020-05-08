 package com.tuit.ar.models.timeline;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.json.JSONArray;
 import org.json.JSONTokener;
 
 import android.os.Handler;
 
 import com.tuit.ar.api.TwitterAccount;
 import com.tuit.ar.api.TwitterRequest;
 import com.tuit.ar.api.request.Options;
 import com.tuit.ar.models.Timeline;
 
 abstract public class Status extends Timeline {
 	protected ArrayList<com.tuit.ar.models.Status> tweets = new ArrayList<com.tuit.ar.models.Status>();
 
 	protected Status(TwitterAccount account) {
 		super(account);
 	}
 
 	@Override
 	public void requestHasFinished(final TwitterRequest request) {
 		if (!request.getUrl().equals(this.getTimeline())) return;
 		final Handler handler = new Handler();
 		final StatusRunnable runnable = new StatusRunnable(); 
 		new Thread() {
 			public void run() {
 				try {
 					JSONArray tweets = new JSONArray(new JSONTokener(request.getResponse()));
 					int c = tweets.length();
 					if (c > 0) {
 						for (int i = c-1; i >= 0; i--) {
 							com.tuit.ar.models.Status tweet = new com.tuit.ar.models.Status(tweets.getJSONObject(i));
 							//is_home INTEGER, is_reply INTEGER, belongs_to_user
 							tweet.setBelongsToUser(account.getUser().getId());
 							// FIXME: some kind of way to recognize this with more abstraction?
 							if (request.getUrl().equals(Options.FRIENDS_TIMELINE) || request.getUrl().equals(Options.REPLIES_TIMELINE)) {
 								tweet.setHome(request.getUrl().equals(Options.FRIENDS_TIMELINE));
 								tweet.setReply(request.getUrl().equals(Options.REPLIES_TIMELINE));
 								tweet.replace();
 							}
 
 							if (i == 0)
 								newestTweet = tweet.getId();
 							Status.this.tweets.add(0, tweet);
 						}
 
 						if (Status.this.tweets.size() > MAX_SIZE) {
 							Status.this.tweets.subList(MAX_SIZE, Status.this.tweets.size()).clear();
 						}
 					}
 					runnable.success = true;
 				} catch (Exception e) {
					runnable.error = e.getLocalizedMessage();
 					runnable.success = false;
 				}
 				handler.post(runnable);
 			}
 		}.start();
 	}
 
 	public ArrayList<com.tuit.ar.models.Status> getTweets() {
 		return tweets;
 	}
 
 	public Collection<com.tuit.ar.models.Status> getTweetsNewerThan(com.tuit.ar.models.Status status) {
 		return tweets.subList(0, tweets.indexOf(status));
 	}
 
 	private class StatusRunnable implements Runnable {
 		public boolean success;
 		public String error = null;
 
 		public void run() {
 			if (success)
 				timelineChanged();
 			else
 				failedToUpdate(error);
 			finishedUpdate();
 		}
 	};
 
 }
