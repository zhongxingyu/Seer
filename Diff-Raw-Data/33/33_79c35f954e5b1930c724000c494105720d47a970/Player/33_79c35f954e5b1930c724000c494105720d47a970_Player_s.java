 package com.serb.podpamp.utils;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.Uri;
 import android.text.TextUtils;
 import com.serb.podpamp.model.managers.FeedsManager;
 import com.serb.podpamp.model.managers.PlaylistManager;
 import com.serb.podpamp.model.domain.FeedItem;
 import com.serb.podpamp.ui.activities.FeedItemDetailsActivity;
 
 import java.util.Queue;
 
 //todo Make an interface IPlayer. Use and instance of IPlayer instead of static members.
 public class Player {
 
 	//region Private Members.
 
 	private static final String ACTION_API_COMMAND = "com.maxmpz.audioplayer.API_COMMAND";
 	private static final String ACTION_STATUS_CHANGED = "com.maxmpz.audioplayer.STATUS_CHANGED";
 	private static final String COMMAND = "cmd";
 	private static final String STATUS = "status";
 	private static final String PAUSED = "paused";
 	//private static final String FAILED = "failed";
 	private static final String POSITION = "pos";
 
 	private static final int TRACK_PLAYING = 1;
 	private static final int TRACK_ENDED = 2;
 	private static final int PLAYING_ENDED = 3;
 
 	private static final int PLAY_COMMAND = 20;
 	//private static final int RESUME_COMMAND = 3;
 	private static final int PAUSE_COMMAND = 2;
 
 	private static boolean paused;
 	private static boolean playing;
 
 	private static Intent statusIntent;
 
 	private static Context context;
 	private static long itemId;
 	private static PlayerListener listener;
 
 	private static Queue<FeedItem> playlist;
 
 	private static BroadcastReceiver statusReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			statusIntent = intent;
 			updateStatusUI();
 		}
 	};
 
 	//endregion
 
 	//region Public Methods.
 
 	public static boolean isPlaying(long id) {
 		return id == itemId && playing;
 	}
 
 
 
 	/*public static void attach(Context c, long id, PlayerListener playerListener) {
 		if (c == null || itemId != id || playerListener == null)
 			return;
 		context = c;
 		itemId = id;
 		listener = playerListener;
 	}*/
 
 
 
 	public static boolean play(Context c, long id, String filePath, int position, PlayerListener playerListener) {
 		if (c == null || id < 0 || playerListener == null || TextUtils.isEmpty(filePath))
 			return false;
 
 		context = c;
 		itemId = id;
 		listener = playerListener;
 
 		if (playlist == null || playlist.size() == 0)
 			playlist = PlaylistManager.getPlaylist(context, itemId);
 
 		registerAndLoadStatus(context);
 
 		Intent intent = new Intent(ACTION_API_COMMAND)
 			.putExtra(COMMAND, PLAY_COMMAND)
 			.setData(Uri.parse("file://" + filePath));
 
 		if (position > 0)
 			intent.putExtra(POSITION, position);
 
 		context.startService(intent);
 
 		return true;
 	}
 
 
 
 	public static boolean pause(Context context) {
 		context.startService(new Intent(ACTION_API_COMMAND).putExtra(COMMAND, PAUSE_COMMAND));
 		return true;
 	}
 
 
 
 	public static void playNext()
 	{
 		if (playlist != null && playlist.size() > 0)
 		{
 			FeedItem item = playlist.poll();
 
 			play(context, item.getId(), item.getFilePath(), item.getElapsed(), listener);
 
 			Intent intent = new Intent(context, FeedItemDetailsActivity.class);
 			intent.putExtra("item_id", item.getId());
 			context.startActivity(intent);
 		}
 	}
 
 	//endregion
 
 	//region Private Methods.
 
 	private static void registerAndLoadStatus(Context context) {
 		statusIntent = context.registerReceiver(statusReceiver, new IntentFilter(ACTION_STATUS_CHANGED));
 	}
 
 
 
 	private static void updateStatusUI() {
 		if(statusIntent != null) {
 			int status = statusIntent.getIntExtra(STATUS, -1);
 
 			switch(status) {
 				case TRACK_PLAYING:
 					paused = statusIntent.getBooleanExtra(PAUSED, false);
 					if (paused)
 					{
 						playing = false;
 						int elapsed = statusIntent.getIntExtra(POSITION, -1);
 						FeedsManager.updateFeedItemElapsed(context, itemId, elapsed);
 						listener.onPaused(elapsed);
 					}
 					else
 					{
 						playing = true;
 						listener.onResumed();
 					}
 					break;
 				case TRACK_ENDED:
 				case PLAYING_ENDED:
 					if (!paused && playing)
 					{
 						playing = false;
 						FeedsManager.updateFeedItemElapsed(context, itemId, 0);
 						FeedsManager.markFeedItemAsReadOrUnread(context, itemId, true);
 						listener.onFinished();
 
 						playNext();
 					}
 					break;
 			}
 		}
 	}
 
 	//endregion
 
 	//region Public Interfaces.
 
 	public static interface PlayerListener {
 		void onResumed();
 
 		void onPaused(int elapsed);
 
 		void onFinished();
 	}
 
 	//endregion
 }
