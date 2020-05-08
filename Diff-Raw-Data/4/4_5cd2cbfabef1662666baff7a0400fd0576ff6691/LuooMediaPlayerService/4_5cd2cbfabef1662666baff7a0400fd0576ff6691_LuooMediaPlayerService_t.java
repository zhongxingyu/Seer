 package com.phonegap.luoo.plugin;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import java.util.Date;
 
 import android.app.Service;
 import android.content.Intent;
 import android.media.MediaPlayer;
 import android.os.Binder;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.Log;
 
 public class LuooMediaPlayerService extends Service {
 	private static final int INTIAL_KB_BUFFER =  128*10/8;//assume 128kbps*10secs/8bits per byte
 	private MediaPlayer mediaPlayer;
 	private final Handler handler = new Handler();  
 	private File downloadingMediaFile;
 	private int totalKbRead = 0;
 	private int counter = 0;
 	private boolean isInterrupted;
 	private LocalBinder localBinder = new LocalBinder();
 	private static Runnable r;
 	private static Thread playerThread;
 	private File destFile;
 	private int totalLength = 1;
 	private int totalBytesRead = 1;
 	private String lastDownloading = "";
 	private Runnable updater;
 	private Runnable updater1;
 	private boolean downloading;
 	private int lastKbRead = 0;
 	private FileInputStream fis;
 	
 	
 	public LuooMediaPlayerService() {
 	}
 	private void initUpdater(){
 		updater = new Runnable() {  
 			
 
 			public void run() {				
 				if (getMediaPlayer() == null) {  
 					//  Only create the MediaPlayer once we have the minimum buffered data  
 					if ( totalKbRead >= INTIAL_KB_BUFFER) {  
 						try {  
 							startMediaPlayer();  
 						} catch (Exception e) {  
 							Log.e(getClass().getName(), "Error copying buffered conent.", e);                  
 						}  
 					}  
 				} else{
 					try{
 						if ( mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() < 1000 || !mediaPlayer.isPlaying()
 								&& (totalKbRead - lastKbRead) > INTIAL_KB_BUFFER ){  
 							//  NOTE:  The media player has stopped at the end so transfer any existing buffered data  
 								//  We test for < 1second of data because the media player can stop when there is still  
 								//  a few milliseconds of data left to play  
 								transferBufferToMediaPlayer();  
 								lastKbRead = totalKbRead;
 							}			
 					}catch(Exception e){
 						e.printStackTrace();
 					}
 							
 				}  
 			}
 		};
 		
 		updater1 = new Runnable() {   
 			public void run() {  
 				// Delete the downloaded File as it's now been transferred to the currently playing buffer file.
 				try {
 					if (!destFile.exists()){
 						destFile.getParentFile().mkdirs();
 						destFile.createNewFile();
 					}					
 					moveFile(downloadingMediaFile, destFile);
 					playLocalMedia(destFile);
 					
 				} catch (IOException e){
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				downloadingMediaFile.delete(); 
 				for (int i = 0; i < counter; i++ ){
 					File bufferedFile = new File(getCacheDir(),"playingMedia" + i + ".dat");  
 					if (bufferedFile.exists()) bufferedFile.delete();
 				}
 				
 				//textStreamed.setText(("Audio full loaded: " + totalKbRead + " Kb read"));  
 			}  
 		};  
 	}
 	public void startStreaming(final String mediaUrl, File destFile) throws IOException {
 		/*
 		 * 如果播放不是同一首歌，则将mediaPlayer清空,如果播放的不是同一首歌，并且前一首还在下载中，设置标志位为true。终止上次下载。
 		 */
 		if (this.lastDownloading.equals(mediaUrl)) return;
 		this.isInterrupted = false;
 		if (this.downloading){
 			this.isInterrupted = true;
 			Log.d("my", "now plya" + mediaUrl + "cancel befor downloading");
 		}
 		if (this.mediaPlayer != null) {
 			this.mediaPlayer.stop();
 			this.mediaPlayer.release();
 			this.mediaPlayer = null;
 		}
 		
 		lastDownloading = mediaUrl;
 	    this.destFile = destFile;
 		r = new Runnable() {     
 			public void run() {     
 				try { 
 					downloadAudioIncrement(mediaUrl);  
 				} catch (IOException e) {  
 					Log.e(getClass().getName(), "Unable to initialize the MediaPlayer for fileUrl=" + mediaUrl, e);  
 				}     
 			}     
 		};     
 		this.handler.removeCallbacks(updater);
 		this.handler.removeCallbacks(updater1);
 		initUpdater();
 		playerThread = new Thread(r);  
 		playerThread.start();
 	}  
 
 	public void downloadAudioIncrement(String mediaUrl) throws IOException {  
 		HttpURLConnection cn = (HttpURLConnection)new URL(mediaUrl).openConnection();  
 		cn.addRequestProperty("User-Agent","LuooFM Player/10.0.0.4072");  
 		cn.setConnectTimeout(10000);
 		cn.setReadTimeout(30000);
 		cn.connect();		
 		totalLength = cn.getContentLength();
 		InputStream stream = cn.getInputStream();  
 		if (stream == null) {  
 			Log.e("my", "Unable to create InputStream for mediaUrl:" + mediaUrl);  
 		}  
 		downloadingMediaFile = new File(this.getCacheDir(),"downloadingMedia.dat");  
 		// Just in case a prior deletion failed because our code crashed or something, we also delete any previously   
 		// downloaded file to ensure we start fresh.  If you use this code, always delete   
 		// no longer used downloads else you'll quickly fill up your hard disk memory.  Of course, you can also   
 		// store any previously downloaded file in a separate data cache for instant replay if you wanted as well.  
 		if (downloadingMediaFile.exists()) {  
 			downloadingMediaFile.delete();  
 		}
 		FileOutputStream out = new FileOutputStream(downloadingMediaFile);     
 		byte buf[] = new byte[16384];  
 		totalBytesRead = 0;  
 		Log.d("my", "begin download ");
 		do {  
 			this.downloading = true;
 			int numread = stream.read(buf);     
 			if (numread <= 0)     
 				break;     
 			out.write(buf, 0, numread);  
 			totalBytesRead += numread;
 			this.totalKbRead = this.totalBytesRead/1024;
 			testMediaBuffer();  
 		} while (validateNotInterrupted());     
 		this.downloading = false;
 		if (totalBytesRead == totalLength) {
 			fireDataFullyLoaded();  
 		}  
 		stream.close();  
 		out.close();
 	}
 	public void flushCacheFiles() {
 		File file = new File(Environment.getExternalStorageDirectory() + "/LuooFm/download/");
 		this.deleteDirectory(file);
 	}
 	
 	private void testMediaBuffer() {
 		// TODO Auto-generated method stub		  
 		handler.post(updater);  
 	}   
 	private boolean validateNotInterrupted() {
 		if (isInterrupted) {  
 			this.isInterrupted = false;
 			return false;  
 		} else {  
 			return true;  
 		}  
 	}
 	private void startMediaPlayer() {  
 		try {     
 			File bufferedFile = new File(this.getCacheDir(),"playingMedia" + (counter++) + ".dat");  
 			Log.d("my", "start media player");
 			// We double buffer the data to avoid potential read/write errors that could happen if the   
 			// download thread attempted to write at the same time the MediaPlayer was trying to read.  
 			// For example, we can't guarantee that the MediaPlayer won't open a file for playing and leave it locked while   
 			// the media is playing.  This would permanently deadlock the file download.  To avoid such a deadloack,   
 			// we move the currently loaded data to a temporary buffer file that we start playing while the remaining   
 			// data downloads.    
 			moveFile(downloadingMediaFile, bufferedFile);  			
 			mediaPlayer = createMediaPlayer(bufferedFile);  
 			mediaPlayer.seekTo(0);
 			// We have pre-loaded enough content and started the MediaPlayer so update the buttons & progress meters.  
 			mediaPlayer.start();  
 		} catch (IOException e) {  
 			Log.e(getClass().getName(), "Error initializing the MediaPlayer.", e);  
 		}     
 	}  
 	
 	public String getPlayingStatus() {
 		try{
 			if (this.mediaPlayer != null){
 				return "{\"cur\":" + this.mediaPlayer.getCurrentPosition() + 
 						", \"dur\":" + this.mediaPlayer.getDuration() + ", \"playing\":" + 
 						this.mediaPlayer.isPlaying() + ", \"downloading\":" + 
 						(1.0 *this.totalBytesRead/this.totalLength)+ "}";
 			}else{
 				return "{}";
 			}		
 		}catch(Exception e){
 			e.printStackTrace();
 			return "{}";
 		}						
 	}
 
 	private MediaPlayer createMediaPlayer(File mediaFile) throws IOException { 
 		MediaPlayer mPlayer = new MediaPlayer();  
 		mPlayer.setOnErrorListener(  
 				new MediaPlayer.OnErrorListener() {  
 					public boolean onError(MediaPlayer mp, int what, int extra) {  
 						Log.e("my", "Error in MediaPlayer: (" + what +") with extra (" +extra +")" );  
 						return false;  
 					}  
 				});  
 
 		//  It appears that for security/permission reasons, it is better to pass a FileDescriptor rather than a direct path to the File.  
 		//  Also I have seen errors such as "PVMFErrNotSupported" and "Prepare failed.: status=0x1" if a file path String is passed to  
 		//  setDataSource().  So unless otherwise noted, we use a FileDescriptor here.  
 		if (fis != null) fis.close();
 		fis = new FileInputStream(mediaFile);  
 		mPlayer.setDataSource(fis.getFD());
 		mPlayer.prepare();  
 		//  release mediaPlayer cause it will be covered
 		if (this.mediaPlayer != null){
 			this.mediaPlayer.pause();
 			this.mediaPlayer.release();
 			this.mediaPlayer = null;
 		}
 		return mPlayer;  
 	} 
 	public void pausePlayer(){  
 		try {  
 			getMediaPlayer().pause();  
 		} catch (Exception e) {  
 			e.printStackTrace();  
 		}  
 	}  
 
 	public void moveFile(File oldLocation, File newLocation) throws IOException { 
 		if ( oldLocation.exists( )) {  
 			BufferedInputStream  reader = new BufferedInputStream( new FileInputStream(oldLocation) );  
 			BufferedOutputStream  writer = new BufferedOutputStream( new FileOutputStream(newLocation, false));  
 			try {  
 				byte[]  buff = new byte[8192];  
 				int numChars;  
 				while ( (numChars = reader.read(  buff, 0, buff.length ) ) != -1) {  
 					writer.write( buff, 0, numChars );  
 				}  
 			} catch( IOException ex ) {  
 				throw new IOException("IOException when transferring " + oldLocation.getPath() + " to " + newLocation.getPath());  
 			} finally {  
 				try {  
 					if ( reader != null ){                          
 						writer.close();  
 						reader.close();  
 					}  
 				} catch( IOException ex ){  
 					Log.e(getClass().getName(),"Error closing files when transferring " + oldLocation.getPath() + " to " + newLocation.getPath() );   
 				}  
 			}  
 		} else {  
 			throw new IOException("Old location does not exist when transferring " + oldLocation.getPath() + " to " + newLocation.getPath() );  
 		}  
 	}  
 
 	public MediaPlayer getMediaPlayer() {  
 		return this.mediaPlayer;  
 	}  
 
 
 	private void fireDataFullyLoaded() { 
 		handler.post(updater1);  
 	} 
 	public void playLocalMedia(File destFile, int seek) throws IOException{
 		Log.d("my", "Play local file" + destFile.getAbsolutePath());
 		if (mediaPlayer != null ){
 			mediaPlayer.pause(); 
 		}		 
 		// Create a new MediaPlayer rather than try to re-prepare the prior one.  
 		mediaPlayer = createMediaPlayer(destFile);
 		mediaPlayer.seekTo(seek);  
 		mediaPlayer.start();
 	}
 	public void playLocalMedia(File destFile) throws IOException{
 		Log.d("my", "Play local file" + destFile.getAbsolutePath());
 		int currentDuratition  = 0;
 		if (mediaPlayer != null ){
 			mediaPlayer.pause(); 
 			currentDuratition = mediaPlayer.getCurrentPosition();
 		}		 
 		// Create a new MediaPlayer rather than try to re-prepare the prior one.  
 		mediaPlayer = createMediaPlayer(destFile);
 		mediaPlayer.seekTo(currentDuratition);  
 		mediaPlayer.start();
 	}
 	private void transferBufferToMediaPlayer() {  
 		try {  
 			// First determine if we need to restart the player after transferring data...e.g. perhaps the user pressed pause 
 			int curPosition = mediaPlayer.getCurrentPosition();  
 			// Copy the currently downloaded content to a new buffered File.  Store the old File for deleting later.   
 			File oldBufferedFile = new File(this.getCacheDir(),"playingMedia" + counter + ".dat");  
 			File bufferedFile = new File(this.getCacheDir(),"playingMedia" + (counter++) + ".dat");  
 
 			//  This may be the last buffered File so ask that it be delete on exit.  If it's already deleted, then this won't mean anything.  If you want to   
 			// keep and track fully downloaded files for later use, write caching code and please send me a copy.  
 			bufferedFile.deleteOnExit();     
 			moveFile(downloadingMediaFile,bufferedFile); 
 			// Pause the current player now as we are about to create and start a new one.  So far (Android v1.5),  
 			// this always happens so quickly that the user never realized we've stopped the player and started a new one  
 			// Create a new MediaPlayer rather than try to re-prepare the prior one.  
 			mediaPlayer = createMediaPlayer(bufferedFile);  
 			mediaPlayer.seekTo(curPosition); 
 			//  Restart if at end of prior buffered content or mediaPlayer was previously playing.    
 			//    NOTE:  We test for < 1second of data because the media player can stop when there is still  
 			//  a few milliseconds of data left to play  
 			mediaPlayer.start();
 			// Lastly delete the previously playing buffered File as it's no longer needed.  
 			oldBufferedFile.delete(); 
 		}catch (Exception e) {  
 			Log.e(getClass().getName(), "Error updating to newly loaded content.", e);                      
 		}  
 	}  
 	
 	public void play() throws Exception {  
 		try {
 			MediaPlayer mediaPlayer = this.getMediaPlayer();
 			mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
 			mediaPlayer.start();   
 		} catch (Exception e) {  
 			e.printStackTrace();  
 		}  
 
 	}  
 	
 	private boolean deleteDirectory(File path) {
 		Log.d("my", "deleteDirectory");
 	    if( path.exists() ) {
 	      File[] files = path.listFiles();
 	      for(int i=0; i<files.length; i++) {
 	         if(files[i].isDirectory()) {
 	           deleteDirectory(files[i]);
 	         }
 	         else {
 	           files[i].delete();
 	           Log.d("my", files[i].toURI().toString());
 	         }
 	      }
 	    }
 	    return( path.delete() );
 	  }
 	public class LocalBinder extends Binder {  
 		public LuooMediaPlayerService getService() {  
 			return LuooMediaPlayerService.this;  
 		}  
 	}  
 	@Override
 	public void onCreate() {
 
 	}
 
 	@Override  
 	public void onStart(Intent intent, int startId) {  
 		Log.d("my", "on start");       
 	}
 
 	@Override  
 	public void onDestroy() { 
 		super.onDestroy();
 		Log.d("my", "GONE");		
 //		this.mediaPlayer.release();
 //		this.player.release();
		this.flushCacheFiles();
 		if (mediaPlayer != null){
 			mediaPlayer.stop();
 			mediaPlayer.release();
 			mediaPlayer = null;
 		}
 		
 	}  
 	
 
 
 	@Override  
 	public boolean onUnbind(Intent intent) {  
 		Log.d("my", "unbind");		
 		return super.onUnbind(intent);  
 	}  
 
 	@Override  
 	public IBinder onBind(Intent intent) { 
 		Log.e("my", "start IBinder~~~");  
 		return localBinder ;  
 	}
 
 	public String getSystemTime() {
 		// TODO Auto-generated method stub
 		Date date = new Date();
 		return date.toGMTString();
 	}  
 
 }
