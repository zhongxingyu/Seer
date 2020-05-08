 package com.hci.activitydj;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import android.content.Context;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.SoundPool;
 
 public class DJManager {
 	
 	/* record properties of a song */
 	class Song {
 //		private final static int SONG_NOT_PLAYED = -1;
 //		
 //		private final int songId;	// actually the raw id in res
 //		private int streamId;		// used when being played, -1 for unplayed
 //		
 //		private double leftVolume, rightVolume;
 //		private int loopMode;
 //		private float playRate;
 //		
 //		public Song(int id) {
 //			songId = id;
 //			streamId = SONG_NOT_PLAYED;
 //			loopMode = -1;			// don't repeat
 //			playRate = 1.0f;
 //		}
 //		
 //		public void setStreamId(int id) {
 //			streamId = id;
 //		}
 //		
 //		public void setLoopMode(SoundPool pool, int loop) {
 //			this.loopMode = loop;
 //			if (streamId != SONG_NOT_PLAYED)
 //				pool.setLoop(streamId, loop);
 //		}
 //		
 //		public void setPlayRate(SoundPool pool, float rate) {
 //			this.playRate = rate;
 //			if (streamId != SONG_NOT_PLAYED)
 //				pool.setRate(streamId, rate);
 //		}
 //		
 //		/* play with current device volume */
 //		public void play(SoundPool pool, Context context) {
 //			AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
 //		    float volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
 //		    this.play(pool, volume, volume, -1);
 //		}
 //		
 //		public void play(SoundPool pool, Context context, int loop) {
 //			AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
 //		    float volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
 //		    this.play(pool, volume, volume, loop);
 //		}
 //		
 //		/* play with volume specified */
 //		public void play(SoundPool pool, float left, float right, int loop) {
 //			streamId = pool.play(songId, left, right, 1, loop, playRate);
 //			this.leftVolume = left;
 //			this.rightVolume = right;
 //		}
 //		
 //		public void stop(SoundPool pool) {
 //			pool.stop(streamId);
 //			streamId = SONG_NOT_PLAYED;
 //		}
 		
 		private int songId;				// raw id of res
 		private actionState state;		// the state this song corresponding to
 		private MediaPlayer player;		// player associated with the song when being played
 		
 		public Song(int id, actionState state) {
 			this.songId = id;
 			this.state = state;
 			player = null;
 		}
 		
 		public actionState getActionState() {
 			return this.state;
 		}
 		
 		/* play the song and randomly pick another now in the same state after completion */
 		public void play(Context context) {
 			final Context thisContext = context;
 			final Song thisSong = this;
 			player = MediaPlayer.create(thisContext, songId);
 			player.start();
 			player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
 				
 				@Override
 				public void onCompletion(MediaPlayer mp) {
 					Song next = list.pickSongRandom(thisSong, state);
					if (next != null) {
 						next.play(thisContext);
						songPlaying = next;
					}
 				}
 			});
 		}
 		
 		public void stop() {
 			if (player != null)
 				player.stop();
 		}
 	}
 	
 	
 	class songList {
 		private ArrayList[] songs;
 		
 		public songList() {
 			songs = new ArrayList [4];
 			for (int i = 0; i < 4; i++)
 				songs[i] = new ArrayList<Song>();
 		}
 		
 		public void addSong(Song song) {
 			int index = song.getActionState().getStateIndex();
 			if (index != -1)
 				((ArrayList<Song>)songs[index]).add(song);
 		}
 		
 		/*
 		 * randomly pick a song in the given state
 		 * return null if the state is none
 		 * return the same song only if there is no other choice
 		 */
 		public Song pickSongRandom(Song current, actionState state) {
 			int index = state.getStateIndex();
 			if (index == -1)
 				return null;
 			
 			ArrayList<Song> thisList = (ArrayList<Song>)songs[index];
 			if (thisList.size() == 1 && current != null && state == current.getActionState())
 				return current;
 			
 			Random generator = new Random();
 			Song next = null;
 			
 			while (true) {
 				next = thisList.get(generator.nextInt(thisList.size()));
 				if (next != current)
 					return next;
 			}
 		}
 	}
 	
 	private Song songPlaying;		// the song is currently being played
 	private songList list;
 	
 	private Context currentContext;
 	
 	/**
 	 * public APIs
 	 */
 	
 	public DJManager(Context context) {
 		songPlaying = null;
 		list = new songList();
 		currentContext = context;
 		
 		this.initSongList();
 	}
 	
 	public boolean isPlaying() {
 		return songPlaying != null;
 	}
 	
 	public void changeSong(actionState state) {
 		if (songPlaying != null)
 			songPlaying.stop();
 //			songPlaying.setLoopMode(pool, 0);
 		
 		songPlaying = list.pickSongRandom(songPlaying, state);
 		if (songPlaying != null)
 			songPlaying.play(currentContext);
 	}
 	
 	public void changeSongLoopStatus(int loop) {
 //		if (songPlaying != null)
 //			songPlaying.setLoopMode(pool, loop);
 	}
 	
 	public void stop() {
 		if (songPlaying != null)
 			songPlaying.stop();
 		songPlaying = null;
 	}
 	
 	public void setContextChanged(Context newContext) {
 		this.currentContext = newContext;
 	}
 	
 	
 	/**
 	 * helper functions
 	 */
 	
 	/* load previous prepared songs to own object
 	 * add the song to pool and tracking list */
 	private void initSongList() {
 		list.addSong(new Song(R.raw.sound5,  actionState.KINETIC_REST));
 		list.addSong(new Song(R.raw.sound1,  actionState.KINETIC_ACT));
 		list.addSong(new Song(R.raw.sound2,  actionState.KINETIC_ACT));
 	}
 }
