 package pro.trousev.cleer.android.service;
 
 import java.io.IOException;
 
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.util.Log;
 import pro.trousev.cleer.Item;
 import pro.trousev.cleer.Messaging;
 import pro.trousev.cleer.Player;
 import pro.trousev.cleer.android.Constants;
 
 //TODO think about callback
 //TODO make state errors in PlayerException
 //TODO think about headset hot removal not in this file
 //TODO think how to implement volume up/down buttons using this interface
 //TODO make non-silent exit on asynchronous error
 //TODO: Implement more status-change messages via Messaging.fire(...).
 //TODO If you encounter problem with slow preparing you should make prepare in open.
 
 public class PlayerAndroid implements Player, MediaPlayer.OnPreparedListener,
 		MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
 	private static MediaPlayer mediaPlayer = null;
 	private static Item currentTrack = null;
 	private static Status currentStatus = Status.Closed;
 	private static Boolean prepared = false;
 	private static PlayerChangeEvent changeEvent = new PlayerChangeEvent();
 
 	public PlayerAndroid() {
 		currentStatus = Status.Closed;
 	};
 
 	@Override
 	public void open(Item track) throws PlayerException {
 		currentTrack = track;
 		if (currentStatus == Status.Closed) {
 			mediaPlayer = new MediaPlayer();
 		}
 		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
 		String t = currentTrack.filename().toString();
 		try {
 			mediaPlayer.setDataSource(t);
 			currentStatus = Status.Stopped;
 			mediaPlayer.setOnPreparedListener(this);
 			mediaPlayer.setOnCompletionListener(this);
 			Log.d(Constants.LOG_TAG, "Player is created");
 		} catch (Exception e) {
 			Log.e(Constants.LOG_TAG, "Unable to create MediaPlayer()");
 			close();
 			throw new PlayerException(e.getMessage());
 		} finally {
 			prepared = false;
 		}
 	}
 
 	@Override
 	public void close() {
 		if (mediaPlayer != null)
 			mediaPlayer.release();
 		mediaPlayer = null;
 		currentTrack = null;
 		currentStatus = Status.Closed;
 		prepared = false;
 		Log.d(Constants.LOG_TAG, "Player closed");
 	}
 
 	@Override
 	public void play() {
 		if ((currentStatus != Status.Stopped)
 				&& (currentStatus != Status.Paused))
 			return;
 		if (prepared) {
 			mediaPlayer.start();
 			currentStatus = Status.Playing;
 		} else {
 			currentStatus = Status.Processing;
 			//mediaPlayer.prepareAsync();
 			try {
 				mediaPlayer.prepare();
				mediaPlayer.start();
				currentStatus = Status.Playing;
 			} catch (IllegalStateException e) {
				Log.e(Constants.LOG_TAG, "PlayerAndroid: Called play() in illegal state");
 				//e.printStackTrace();
 			} catch (IOException e) {
 				Log.e(Constants.LOG_TAG, "PlayerAndroid: IOException");
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		changeEvent.error = null;
 		changeEvent.reason = null;
 		changeEvent.sender = this;
 		changeEvent.status = currentStatus;
 		changeEvent.track = currentTrack;
 		Messaging.fire(changeEvent);
 		Log.d(Constants.LOG_TAG, "Player is playing");
 	}
 
 	@Override
 	public void stop(Reason reason) {
 		if ((currentStatus != Status.Paused)
 				&& (currentStatus != Status.Playing)
 				&& (currentStatus != Status.Processing))
 			return;
 		mediaPlayer.stop();
 		mediaPlayer.reset();
 		prepared = false;
 		currentStatus = Status.Stopped;
 		changeEvent.error = null;
 		changeEvent.reason = reason;
 		changeEvent.sender = this;
 		changeEvent.status = currentStatus;
 		changeEvent.track = currentTrack;
 		Messaging.fire(changeEvent);
 		Log.d(Constants.LOG_TAG, "Player is stopped");
 	}
 
 	@Override
 	public void pause() {
 		if (currentStatus != Status.Playing)
 			return;
 		mediaPlayer.pause();
 		currentStatus = Status.Paused;
 		changeEvent.status = currentStatus;
 		changeEvent.track = currentTrack;
 		Messaging.fire(changeEvent);
 		Log.d(Constants.LOG_TAG, "Player is paused");
 	}
 
 	@Override
 	public void resume() {
 		this.play();
 	}
 
 	@Override
 	public Item now_playing() {
 		return currentTrack;
 	}
 
 	@Override
 	public Status getStatus() {
 		return currentStatus;
 	}
 
 	@Override
 	public void onPrepared(MediaPlayer mp) {
 		prepared = true;
 		mediaPlayer.start();
 		currentStatus = Status.Playing;
 		Log.d(Constants.LOG_TAG, "Player is prepared");
 	}
 
 	@Override
 	public boolean onError(MediaPlayer mp, int what, int extra) {
 		close();
 		currentStatus = Status.Error;
 		Log.e(Constants.LOG_TAG, "Error from media player");
 		return false;
 	}
 
 	@Override
 	public void onCompletion(MediaPlayer mp) {
 		stop(Reason.EndOfTrack);
 	}
 
 	@Override
 	public int getCurrentPosition() {
 		return mediaPlayer.getCurrentPosition();
 	}
 
 	@Override
 	public int getDuration() {
 		return mediaPlayer.getDuration();
 	}
 
 	@Override
 	public void setCurrentPosition(int msec) {
 		mediaPlayer.seekTo(msec);
 	}
 
 }
