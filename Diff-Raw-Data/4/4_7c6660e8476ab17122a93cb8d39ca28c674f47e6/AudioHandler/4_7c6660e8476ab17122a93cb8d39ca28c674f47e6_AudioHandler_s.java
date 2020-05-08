 package com.phonegap.demo;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 
 import android.content.Context;
 import android.content.res.AssetManager;
 import android.content.res.AssetFileDescriptor;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnErrorListener;
 import android.media.MediaRecorder;
 import android.media.MediaPlayer.OnBufferingUpdateListener;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.util.Log;
 
 public class AudioHandler implements OnCompletionListener, OnPreparedListener, OnErrorListener {
 	private MediaRecorder recorder;
 	private boolean isRecording = false;
 //	MediaPlayer mPlayer;
 	private boolean isPlaying = false;
 	private String recording;
 	private String saveFile;
 	private Context mCtx;
 	
 	private HashMap<String, MPlayerStatus> mPlayers_file;
 	private HashMap<MediaPlayer, MPlayerStatus> mPlayers_player;
 
 	private boolean isPaused = false;
 	private AssetManager assets;
 	private String curPlaying = null;
 	private AudioManager volumeControl;
 	
 	private static final int MUSIC_STREAM = AudioManager.STREAM_MUSIC;
 
 	private class MPlayerStatus {
 		public String file;
 		public MediaPlayer player;
 		public boolean isPaused;
 		
 		public MPlayerStatus(String theFile, MediaPlayer thePlayer) {
 			file = theFile;
 			player = thePlayer;
 			isPaused = false;
 		}
 	}
 	
 	public AudioHandler(String file, Context ctx, AssetManager assets) {
 //		this.recording = file;
 		this.mCtx = ctx;
 		this.assets = assets;
 		volumeControl = (AudioManager) mCtx.getSystemService(Context.AUDIO_SERVICE);
 		mPlayers_file = new HashMap<String, MPlayerStatus>();
 		mPlayers_player = new HashMap<MediaPlayer, MPlayerStatus>();
 	}
 	
 /*
 	protected void startRecording(String file){
 		if (!isRecording){
 			saveFile=file;
 			recorder = new MediaRecorder();
 			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
 			recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
 			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
 			recorder.setOutputFile(this.recording);
 			try {
 				recorder.prepare();
 			} catch (IllegalStateException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 //			} catch (IOException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 			}
 			isRecording = true;
 			recorder.start();
 		}
 	}
 	
 	private void moveFile(String file) {
 		// this is a hack to save the file as the specified name
 		File f = new File (this.recording);
 		f.renameTo(new File("/sdcard" + file));
 	}
 	
 	protected void stopRecording(){
 		try{
 			if((recorder != null)&&(isRecording)) {
 				isRecording = false;
 				recorder.stop();
 		        recorder.release(); 
 			}
 			moveFile(saveFile);
 		}catch (Exception e){e.printStackTrace();}
 	}
 */	
 	
 	protected void startPlaying(String file) {
 		if ( !mPlayers_file.containsKey(file) ) {
 			try {
 //				if (curPlaying != null) {
 //					Log.d("Audio startPlaying", "New file to play, stopping " + curPlaying);
 //					stopPlaying();
 //				}
 				AssetFileDescriptor fileAsset = getAssetFileDesc(file);
 				
 				MediaPlayer mPlayer = new MediaPlayer();
 				MPlayerStatus status = new MPlayerStatus(file, mPlayer);
 				mPlayers_file.put(file, status);
 				mPlayers_player.put(mPlayer, status);
 //				isPlaying = true;
 //				isPaused = false;
 //				curPlaying = file;
 				Log.d("Audio startPlaying", "audio: " + file);
 				if (isStreaming(file))
 				{
 					Log.d("AudioStartPlaying", "Streaming");
 					// Streaming prepare async
 					mPlayer.setDataSource(file);
 					mPlayer.setAudioStreamType(MUSIC_STREAM);  
 					mPlayer.prepareAsync();
 				} else {
 					Log.d("AudioStartPlaying", "File");
 					// Not streaming prepare synchronous, abstract base directory
 					if (fileAsset == null) {
 						mPlayer.setDataSource(file);
 					}
 					else {
						mPlayer.setDataSource(fileAsset.getFileDescriptor());
 					}
 					mPlayer.prepare();
 				}
 				mPlayer.setOnPreparedListener(this);
 			} catch (Exception e) { e.printStackTrace(); }
 		}
 		// Otherwise check to see if it's paused, if it is, resume
 		else if ( mPlayers_file.get(file).isPaused ) {
 			resumePlaying(file);
 		}
 	}
 
 	private AssetFileDescriptor getAssetFileDesc(String file) {
 		if ( !file.startsWith("file:///android_asset/") ) {
 			return null;
 		}
 		
 		try {
 			String filePath = file.substring(22);
 			return assets.openFd(filePath);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	protected void pausePlaying(String file) {
 		if ( mPlayers_file.containsKey(file) && !mPlayers_file.get(file).isPaused ) {
 			mPlayers_file.get(file).player.pause();
 			mPlayers_file.get(file).isPaused = true;
 		}
 	}
 
 	protected void resumePlaying(String file) {
 		if ( mPlayers_file.containsKey(file) && mPlayers_file.get(file).isPaused ) {
 			mPlayers_file.get(file).player.start();
 			mPlayers_file.get(file).isPaused = false;
 		}
 	}
 
 	protected void stopPlaying(String file) {
 		if ( mPlayers_file.containsKey(file) ) {
 			MediaPlayer mPlayer = mPlayers_file.get(file).player;
 			mPlayer.stop();
 			mPlayer.release();
 			mPlayers_file.remove(file);
 			mPlayers_player.remove(mPlayer);
 		}
 	}
 	
 	public void onCompletion(MediaPlayer mPlayer) {
 		mPlayer.stop();
 		mPlayer.release();
 		String file = mPlayers_player.get(mPlayer).file;
 		mPlayers_file.remove(file);
 		mPlayers_player.remove(mPlayer);
 //		isPlaying=false;
 //		isPaused = false;
 //		curPlaying = null;
    	}
 
 	public void stopAllPlaying() {
 		for (MPlayerStatus status : mPlayers_file.values()) {
 			status.player.stop();
 			status.player.release();
 		}
 		mPlayers_file.clear();
 		mPlayers_player.clear();
 	}
 
 	public void increaseVolume() {
 		volumeControl.adjustStreamVolume(MUSIC_STREAM,
 		                                 AudioManager.ADJUST_RAISE,
 		                                 0);
 	}
 
 	public void decreaseVolume() {
 		volumeControl.adjustStreamVolume(MUSIC_STREAM,
 		                                 AudioManager.ADJUST_LOWER,
 		                                 0);
 	}
 
 	public boolean setVolume(int vol) {
 		if (vol < 0 || vol < volumeControl.getStreamMaxVolume(MUSIC_STREAM))
 			return false;
 
 		volumeControl.setStreamVolume(MUSIC_STREAM, vol, 0);
 		return true;
 	}
 	
 	protected long getCurrentPosition(String file) {
 		MediaPlayer mPlayer = mPlayers_file.get(file).player;
 		if (mPlayer != null) 
 		{
 			return(mPlayer.getCurrentPosition());
 		} else { return(-1); }
 	}
 	
 	private boolean isStreaming(String file) 
 	{
 		if (file.contains("http://")) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	protected long getDuration(String file) {
 		long duration = -2;
 		if (!mPlayers_file.containsKey(file) & !isStreaming(file)) {
 			try {
 				AssetFileDescriptor fileAsset = getAssetFileDesc(file);
 				MediaPlayer mPlayer = new MediaPlayer();
 				if (fileAsset == null) {
 					mPlayer.setDataSource(file);
 				}
 				else {
 					mPlayer.setDataSource(fileAsset.getFileDescriptor());
 				}
 				mPlayer.prepare();
 				duration = mPlayer.getDuration();
 				mPlayer.release();
 			} catch (Exception e) { e.printStackTrace(); return(-3); }
 		} else if (mPlayers_file.containsKey(file)) {
 			try {
 				duration = mPlayers_file.get(file).player.getDuration();
 			} catch (Exception e) { e.printStackTrace(); return(-4); }
 		} else { return -1; }
 		return duration;
 	}
 
 	public void onPrepared(MediaPlayer mPlayer) {
 		if ( mPlayers_player.containsKey(mPlayer) ) {
 			mPlayer.setOnCompletionListener(this);
 			mPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener()
 			{
 				public void onBufferingUpdate(MediaPlayer mPlayer, int percent)
 				{
 					/* TODO: call back, e.g. update outer progress bar */
 					Log.d("AudioOnBufferingUpdate", "percent: " + percent); 
 				}
 			});
 			mPlayer.start();
 		}
 	}
 
 	public boolean onError(MediaPlayer mPlayer, int arg1, int arg2) {
 		Log.e("AUDIO onError", "error " + arg1 + " " + arg2);
 		return false;
 	}
 	
 	protected void setAudioOutputDevice(int output){
 		// Changes the default audio output device to speaker or earpiece 
 		AudioManager audiMgr = (AudioManager) mCtx.getSystemService(Context.AUDIO_SERVICE);
 		if (output == (2))
 			audiMgr.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_SPEAKER, AudioManager.ROUTE_ALL);
 		else if (output == (1)){
 			audiMgr.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);
 		}else
 			Log.e("AudioHandler setAudioOutputDevice", " unknown output device");	
 	}
 	
 	protected int getAudioOutputDevice(){
 		AudioManager audiMgr = (AudioManager) mCtx.getSystemService(Context.AUDIO_SERVICE);
 		if (audiMgr.getRouting(AudioManager.MODE_NORMAL) == AudioManager.ROUTE_EARPIECE)
 			return 1;
 		else if (audiMgr.getRouting(AudioManager.MODE_NORMAL) == AudioManager.ROUTE_SPEAKER)
 			return 2;
 		else
 			return -1;
 	}
 }
