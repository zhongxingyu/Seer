package edu.mines.csci598.recycler.bettyCrocker;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 
 /**
  * The Song class represents a collection of Tracks that are played 
  * synchronously to provide a final mix that is the song that is played.
  * 
  * In order to create a song, you have two options:
  * 1.) You can define a List of Tracks and initialize the Song with the List
  * 2.) You can create a Song with the default constructor and add Tracks later
  * 
  * Once a Song is created, the 'startPlaying' method needs to be called to
  * start the Song, and the 'stopPlaying' method is called to stop it.
  * 
  * @author John
  *
  */
 public class Song {
 	//Used for making sure the Tracks start at the same time
 	private final CountDownLatch startLatch = new CountDownLatch(1);
 	
 	private List<Track> tracks;
 	private boolean isPlaying = false;
 	private boolean isLooping = false;
 	
 	/**
 	 * The default constructor for a Song. It simply initializes the list
 	 * of Tracks to an empty list.
 	 */
 	public Song() {
 		tracks = new ArrayList<Track>();
 	}
 	
 	/**
 	 * Creates a Song and populates the list of Tracks with the given
 	 * list of Tracks.
 	 * @param tracks
 	 */
 	public Song(List<Track> tracks) {
 		for (Track track : tracks) {
 			addTrack(track);
 		}
 	}
 
 	/**
 	 * Plays the Song with the current looping setting.
 	 */
 	public void startPlaying() {
 		startPlaying(isLooping);
 	}
 	
 	/**
 	 * Plays the Song, allowing the user to pass in the looping
 	 * setting.
 	 * @param loop
 	 */
 	public void startPlaying(boolean loop) {
 		isLooping = loop;
 		isPlaying = true;
 		new PlayThread().start();
 	}
 	
 	/**
 	 * Stops the Song.
 	 */
 	public void stopPlaying() {
 		isPlaying = false;
 		isLooping = false;
 		
 		for(Track track : tracks) {
 			track.stopPlaying();
 		}
 	}
 	
 	/**
 	 * Adds a Track to the Song.
 	 * @param track
 	 */
 	public void addTrack(Track track) {
 		insertTrack(tracks.size(), track);
 	}
 	
 	/**
 	 * Allows the user to add a Track to the Song at a specific
 	 * index.
 	 * @param index
 	 * @param track
 	 */
 	public void insertTrack(int index, Track track) {
 		track.setStartLatch(startLatch);
 		tracks.add(index, track);
 	}
 	
 	/*
 	 * This method starts all of the Tracks assigned to this
 	 * song at the same time.
 	 */
 	private void playTracks() {
 		for(Track track : tracks) {
 			track.startPlaying();
 		}
 		
 		//Sleep for a short amount of time to give all of the tracks a chance to start playing
 		try {
 			Thread.sleep(200);
 		} catch (InterruptedException e) {
 			System.out.println("Problem sleeping before starting track playback.");
 			e.printStackTrace();
 		}
 		
 		//Count down the start latch to have all tracks begin playing at the same time
 		startLatch.countDown();
 	}
 	
 	/*
 	 * Determines if all tracks are finished playing
 	 */
 	private boolean allTracksFinished() {
 		boolean allTracksFinished = true;
 		
 		for (Track track : tracks) {
 			if (track.isPlaying()) {
 				allTracksFinished = false;
 				break;
 			}
 		}
 		
 		return allTracksFinished;
 	}
 	
 	/**
 	 * Sets the looping setting.
 	 * @param isLooping
 	 */
 	public void setLooping(boolean isLooping) {
 		this.isLooping = isLooping;
 	}
 	
 	/**
 	 * Gets the list of Tracks
 	 * @return
 	 */
 	public List<Track> getTracks() {
 		return tracks;
 	}
 	
 	
 	/*
 	 * This class is the thread that is used for playing the Tracks.
 	 * It is needed so that main flow of the application is not
 	 * interrupted while the Song plays.
 	 */
 	private class PlayThread extends Thread {
 		
 		public void run() {
 			playTracks();
 			
 			while (isPlaying && isLooping) {
 				if (allTracksFinished()) {
 					playTracks();
 				}
 			}
 		}
 		
 	}
 
 }
