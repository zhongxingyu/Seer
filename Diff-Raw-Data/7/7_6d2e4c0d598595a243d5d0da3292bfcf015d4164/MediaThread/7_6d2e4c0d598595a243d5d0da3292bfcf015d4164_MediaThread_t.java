 package com.crankcode.threads;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 
 import com.crankcode.services.MediaService;
 import com.crankcode.utils.CrankLog;
 import com.crankcode.utils.MediaStatus;
 
 public class MediaThread extends Thread {
 
 	private MediaPlayer mediaPlayer;
 	private final List<File> playlist = new ArrayList<File>();
 	private int song = 0;
 	private MediaStatus status = MediaStatus.STOPPED;
 	private final int seekMiliseconds = 15000;
 	private final MediaService mediaService;
 
 	public MediaThread(MediaService mediaService) {
 		this.mediaService = mediaService;
 	}
 
 	@Override
 	public void run() {
 		CrankLog.v(this, "run()");
 		this.setMediaPlayer(new MediaPlayer());
 	}
 
 	public MediaPlayer getMediaPlayer() {
 		return mediaPlayer;
 	}
 
 	public void setMediaPlayer(MediaPlayer mediaPlayer) {
 		this.mediaPlayer = mediaPlayer;
 	}
 
 	public List<File> getPlaylist() {
 		return playlist;
 	}
 
 	public int getSong() {
 		return this.song;
 	}
 
 	public void setSong(int song) {
 		this.song = song;
 	}
 
 	public MediaStatus getStatus() {
 		return this.status;
 	}
 
 	public MediaStatus play() {
 		if (this.status.equals(MediaStatus.PLAYING)) {
 			this.stopPlayback();
 		}
 		if (!this.playlist.isEmpty()) {
 			this.play(this.song);
 		}
 		return this.status;
 	}
 
 	public MediaStatus play(int songPosition) {
 		try {
 			this.song = songPosition;
 			File selectedSong = this.playlist.get(songPosition);
 			if (this.mediaPlayer.isPlaying()) {
 				this.mediaPlayer.stop();
 			}
 			this.mediaPlayer.reset();
 			this.mediaPlayer.setDataSource(selectedSong.getAbsolutePath());
 			this.mediaPlayer.prepare();
 			this.mediaPlayer.start();
 			this.mediaPlayer
 					.setOnCompletionListener(new OnCompletionListener() {
 						public void onCompletion(MediaPlayer arg0) {
 							nextSong();
 						}
 					});
 
 			this.status = MediaStatus.PLAYING;
 			this.mediaService.updateNotification(this.status,
 					this.playlist.get(songPosition));
 		} catch (IllegalArgumentException e) {
 			CrankLog.e(this, "Error on play(): " + e.getLocalizedMessage());
 			this.status = MediaStatus.ERROR;
 		} catch (IllegalStateException e) {
 			CrankLog.e(this, "Error on play(): " + e.getLocalizedMessage());
 			this.status = MediaStatus.ERROR;
 		} catch (IOException e) {
 			CrankLog.e(this, "Error on play(): " + e.getLocalizedMessage());
 			this.status = MediaStatus.ERROR;
 		}
 		return this.status;
 	}
 
 	public MediaStatus stopPlayback() {
 		if (this.status.equals(MediaStatus.PLAYING)
 				|| this.status.equals(MediaStatus.PAUSED)) {
 			this.mediaPlayer.stop();
 			this.song = 0;
 			this.status = MediaStatus.STOPPED;
 		}
 		this.mediaService.updateNotification(this.status, null);
 		return this.status;
 	}
 
 	public MediaStatus pause() {
 		this.status = MediaStatus.PAUSED;
 		this.mediaPlayer.pause();
 		this.mediaService.updateNotification(this.status,
 				this.playlist.get(this.song));
 		return this.status;
 	}
 
 	public MediaStatus rewind() {
 		int songPosition = this.mediaPlayer.getCurrentPosition();
 		if ((songPosition - this.seekMiliseconds) < 0) {
 			this.previousSong();
 		} else {
 			this.mediaPlayer.seekTo(this.mediaPlayer.getCurrentPosition()
 					- this.seekMiliseconds);
 		}
 		return this.status;
 	}
 
 	public MediaStatus fastforward() {
 		int songPosition = this.mediaPlayer.getCurrentPosition();
 		if ((songPosition + this.seekMiliseconds) > this.mediaPlayer
 				.getDuration()) {
 			this.nextSong();
 		} else {
 			this.mediaPlayer.seekTo(this.mediaPlayer.getCurrentPosition()
 					+ this.seekMiliseconds);
 		}
 		return this.status;
 	}
 
 	public MediaStatus nextSong() {
 		// Check if last song or not
 		++this.song;
 		if (this.song >= this.playlist.size()) {
 			this.song = 0;
 		}
 		return play(this.song);
 	}
 
 	public MediaStatus previousSong() {
 		if (this.song > 0) {
 			--this.song;
 		}
 		return play(this.song);
 	}
 
 	public void clearPlaylist() {
 		this.playlist.clear();
 		this.song = 0;
 	}
 
 	public void end() {
 		this.stopPlayback();
 		this.playlist.clear();
		if (this.mediaPlayer != null) {
			this.mediaPlayer.release();
			this.mediaPlayer = null;
		}
 		// We make sure playlist and mediaPlayer are eligible for garbage
 		// collection
 		this.playlist.clear();
 	}
 }
