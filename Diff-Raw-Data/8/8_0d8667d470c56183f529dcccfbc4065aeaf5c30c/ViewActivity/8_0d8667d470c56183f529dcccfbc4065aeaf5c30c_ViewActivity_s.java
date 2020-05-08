 package at.ac.univie.nappingplayer;
 
 import java.io.File;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnErrorListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.media.MediaPlayer.OnVideoSizeChangedListener;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceHolder.Callback;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.view.WindowManager;
 import android.widget.TextView;
 
 /**
  * Class that plays a video file specified through intent parameter
  * @author werner
  *
  */
 public class ViewActivity extends Activity implements Callback,
 OnCompletionListener, OnPreparedListener, OnVideoSizeChangedListener,
 OnErrorListener{
 	
 	private static final String TAG = ViewActivity.class.getSimpleName();
 	
 	TextView videoIDview;
 	
 	private MediaPlayer mPlayer;
 	private SurfaceView mPlayView;
 	private SurfaceHolder mHolder;
 	
 	private SharedPreferences mSharedPreferences;
 	
 	int mVideoId;
 	int pauseduration;
 	
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
         setContentView(R.layout.showvideo);
         
         mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
         
         View v = findViewById(R.id.layout_video);
         v.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
         
         this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         
         //catch Intent
         Intent intent= getIntent(); // gets the previously created intent
         mVideoId = intent.getIntExtra("videoId", -1);
         
         if (mVideoId == -1) {
         	Log.e(TAG, "No video ID passed, exiting");
     		setResult(Activity.RESULT_CANCELED);
         	finish();
         }
         
         videoIDview = (TextView) this.findViewById(R.id.text);
         videoIDview.setText("Video " + String.valueOf(mVideoId+1));
         
         
         try {
 			mPlayView = (SurfaceView) findViewById(R.id.video_surface);
 			mHolder = mPlayView.getHolder();
 			mHolder.addCallback(this);
 			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 		} catch (Exception e) {
 			Log.e(TAG, "Error while creating Surface:" + e.toString());
 		}
         
 		
          
         
     }
     
     
     @Override
 	/**
 	 * Called when the activity pauses
 	 */
     protected void onPause() {
 		super.onPause();
 		if (mPlayer != null) {
 			mPlayer.release();
 			mPlayer = null;
 		}
 	}
     
     
     @Override
 	/**
 	 * Called when the activity is destroyed.
 	 */
     protected void onDestroy() {
 		super.onDestroy();
 		if (mPlayer != null) {
 			mPlayer.release();
 			mPlayer = null;
 		}
 	}
     
 	@Override
 	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
 		Log.e(TAG, "Error while playing video.");
 		return false;
 	}
 
 	@Override
 	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
 		Log.d(TAG, "Video size changed to " + width + "x" + height);
 	}
 
 	/**
 	 * Called when the Media Player is finished preparing and ready to play.
 	 */
 	public void onPrepared(MediaPlayer player) {
 		delayedStartVideo();
 	}
 
 	@Override
 	public void onCompletion(MediaPlayer player) {
 		Log.d(TAG, "Finished playing video");
 		mPlayer.release();
 		Intent resultData = new Intent();
 		resultData.putExtra("videoId", mVideoId);
 		setResult(Activity.RESULT_OK, resultData);
 		finish();
 	}

 	//Starts video after showing Video ID
 	public void delayedStartVideo(){
 		videoIDview.bringToFront();
 		long delay = Integer.parseInt(mSharedPreferences.getString(PreferencesActivity.VIDEO_START_DELAY, "1000"));
 		SystemClock.sleep(delay);
 		videoIDview.setVisibility(View.INVISIBLE);
 		mPlayer.start();
 	}
 
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		Log.d(TAG, "Surface changed");
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder arg0) {
 		try {
 
 			mPlayer = new MediaPlayer();
 			
 			//resolve video ID
 			File videoFile = VideoPlaylist.getVideo(mVideoId);
 			if ((!videoFile.exists()) || (!videoFile.canRead())) {
 				throw new IOException("Video file " + videoFile.toString() + "not found!");
 			}
 			
 			Log.d(TAG, "Preparing player for video " + videoFile.toString());
 			
 			mPlayer.setDataSource(videoFile.getPath());
 			mPlayer.setDisplay(mHolder);
 			mPlayer.setScreenOnWhilePlaying(true);
 			mPlayer.setOnPreparedListener(this);
 			mPlayer.setOnCompletionListener(this);
 			mPlayer.setOnVideoSizeChangedListener(this);
 			mPlayer.setOnErrorListener(this);
 			mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
 			mPlayer.prepare();
 				
 			//Get the dimensions of the video
 		    int videoWidth 	= mPlayer.getVideoWidth();
 		    int videoHeight = mPlayer.getVideoHeight();
 		    
 		    // Get the SurfaceView layout parameters
 		    LayoutParams lp = mPlayView.getLayoutParams();
 		    
 		    
 		    //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		    String displaySetting = mSharedPreferences.getString(PreferencesActivity.LIST_SCALE, "");
 		    
 		    if (displaySetting.equalsIgnoreCase("original")) {
 		    	// original size
 			    lp.width = videoWidth;
 			    lp.height = videoHeight;
 		    } else if (displaySetting.equalsIgnoreCase("scale")) {
 		    	// scale to screen width
 				// http://stackoverflow.com/q/4835060
 			    // Get the width of the screen
 			    int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
 			    // Set the width of the SurfaceView to the width of the screen
 			    lp.width = screenWidth;
 			    // Set the height of the SurfaceView to match the aspect ratio of the video 
 			    // be sure to cast these as floats otherwise the calculation will likely be 0
 			    lp.height = (int) (((float)videoHeight / (float)videoWidth) * (float)screenWidth);
 		    } else {
 		    	// do nothing, will be fullscreen by default, I guess/hope/fear?
 		    }
 		    
 		    // Commit the layout parameters
 		    mPlayView.setLayoutParams(lp); 
 			
 		} catch (IllegalArgumentException e) {
 			Log.e(TAG, e.toString());
 			e.printStackTrace();
 		} catch (IllegalStateException e) {
 			Log.e(TAG, e.toString());
 			e.printStackTrace();
 		} catch (IOException e) {
 			Log.e(TAG, e.toString());
 			e.printStackTrace();
 		} catch (ArrayIndexOutOfBoundsException e) {
 			Log.e(TAG, e.toString());
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder arg0) {
 		Log.d(TAG, "Surface destroyed");
 	}
 	
 	@Override
 	public void onBackPressed() {
 		
 	}
 	
 }
