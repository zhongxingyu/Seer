 package com.tikal.share;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.widget.Toast;
 
 import com.google.android.youtube.player.YouTubeBaseActivity;
 import com.google.android.youtube.player.YouTubeInitializationResult;
 import com.google.android.youtube.player.YouTubePlayer;
 import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
 import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
 import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener;
 import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
 import com.google.android.youtube.player.YouTubePlayer.Provider;
 import com.google.android.youtube.player.YouTubePlayerView;
 import com.google.cloud.backend.android.tikal.share.sync.CloudSync;
 
 public class PlayerActivity extends YouTubeBaseActivity 
 implements OnInitializedListener{
 
 	public static final String DEVELOPER_KEY = "AIzaSyAF1kx_N4utAzBWt_t1bdcRpgBQlwyAFgo";
 	private MHPlaybackEventListener playerEventListener;
 	private MHPlayerStateChangeListener playerStateChangeListener;
 	private YouTubePlayer playa; //hollla
 	private boolean playerLoaded;
 	private YouTubePlayerView youTubeView;
 	//these variables are static to keep through orientation changes
 	private static int previousReqSeek;
 	private static boolean firstTime = true;
 
 
 	//clientid and videoid passed from calling activity
 	private String videoID;
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_play);
 		if(firstTime){
 			playerLoaded = false;
 			previousReqSeek = 0;
 
 			firstTime = false;
 
 			videoID = getIntent().getExtras().getString(CloudSync.INTENT_VIDEO_ID);
 			String clientID = getClientID();
 			//if non-default client id
 			if(!clientID.equals("default")){
 				Intent syncStart = new Intent(this, CloudSync.class);
 				syncStart.putExtra(CloudSync.INTENT_VIDEO_ID, videoID);
 				syncStart.putExtra(CloudSync.INTENT_CLIENT_ID, clientID);
 				syncStart.putExtra(CloudSync.INTENT_COMMAND,CloudSync.INTENT_COMMAND_GET);
 				this.startActivityForResult(syncStart, 55);
 			}
 		}
		else{
			loadData(null);
		}
 	}
 	private String getClientID(){
 		SharedPreferences sharedPreferences = PreferenceManager
 		.getDefaultSharedPreferences(PlayerActivity.this);
 		return (sharedPreferences.getString("userName","default"));	
 	}
 	private void loadData(Intent data){
 		//check if previous offset received from sync
 		String offSet = "";
 		if(data != null){
 			offSet = ((String)data.getExtras().getString("result"));
 			if(!offSet.equals("")){
 				try{
 					previousReqSeek = Integer.parseInt(offSet);
 				}
 				catch(Exception ex){
 					previousReqSeek = 0;
 				}
 			}
 		}
 		//after sync data received or canceled or not available init player 
 		youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
 		youTubeView.initialize(DEVELOPER_KEY, this);  
 		//add listeners to YouTubePlayerView
 		playerEventListener = new MHPlaybackEventListener();
 		playerStateChangeListener = new MHPlayerStateChangeListener();
 	}
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		String clientID = getClientID();
 		if(!clientID.equals("default") && isFinishing()){
 			firstTime= true;
 			Intent syncStart = new Intent(PlayerActivity.this, CloudSync.class);
 			syncStart.putExtra(CloudSync.INTENT_VIDEO_ID, videoID);
 			syncStart.putExtra(CloudSync.INTENT_CLIENT_ID, clientID);
 			syncStart.putExtra(CloudSync.INTENT_OFFSET_MILIS, "" + previousReqSeek);
 			syncStart.putExtra(CloudSync.INTENT_COMMAND, CloudSync.INTENT_COMMAND_SET);
 			PlayerActivity.this.startActivity(syncStart);
 		}
 	}
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		loadData(data);
 	}
 
 	@Override
 	public void onInitializationFailure(Provider arg0,
 			YouTubeInitializationResult arg1) {
 		//on error toast and exit activity
 		Toast.makeText(PlayerActivity.this, "Error loading YouTube Player", Toast.LENGTH_LONG).show();
 		PlayerActivity.this.finish();
 	}
 
 
 	@Override
 	public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
 			boolean wasRestored) {
 		if (!wasRestored) {
 			playa = player;
 			playa.cueVideo(videoID);
 			playa.setPlaybackEventListener(playerEventListener);
 			playa.setPlayerStateChangeListener(playerStateChangeListener);
 			playa.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
 		}
 	}
 
 	private void startVideoAt(int milis){
 		if(playerLoaded){
 			previousReqSeek = 0;
 			playa.seekToMillis(milis);
 			playa.play();
 		}
 		else{
 			previousReqSeek = milis;
 		}
 	}
 
 	private class MHPlaybackEventListener implements PlaybackEventListener {
 
 		@Override
 		public void onBuffering(boolean arg0) {}
 
 		@Override
 		public void onPaused() {
 			//save seek position for sync
 			previousReqSeek = playa.getCurrentTimeMillis();
 		}
 
 		@Override
 		public void onPlaying() {}
 
 		@Override
 		public void onSeekTo(int arg0) {}
 
 		@Override
 		public void onStopped() {}
 	}
 
 	private class MHPlayerStateChangeListener implements PlayerStateChangeListener  {
 
 		@Override
 		public void onAdStarted() {}
 
 		@Override
 		public void onError(ErrorReason arg0) {}
 
 		@Override
 		public void onLoaded(String arg0) {
 			playerLoaded = true;
 			//in case previously requested seek seek to that point
 			if(previousReqSeek > 0){
 				startVideoAt(previousReqSeek);
 			}
 		}
 
 		@Override
 		public void onLoading() {}
 
 		@Override
 		public void onVideoEnded() {
 			//if video has ended then set seek position to 0
 			previousReqSeek = 0;
 		}
 
 		@Override
 		public void onVideoStarted() {}
 
 	}
 }
 
