 package com.mindbadger.jukebox;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import com.mindbadger.library.Track;
 
 public class PlayList {
 	Logger logger = Logger.getLogger(PlayList.class);
 	
 	public static final int POSITION_START_OF_PLAYLIST = -1;
 	public static final int POSITION_END_OF_PLAYLIST = -2;
 
 	private int currentIndex = POSITION_START_OF_PLAYLIST;
 	private List<Track> trackList = new ArrayList<Track> ();
 	
 	private PlaylistRandomiser playlistRandomiser;
 	
 	public int getSize() {
 		return trackList.size();
 	}
 
 	public int getCurrentIndex() {
 		return currentIndex;
 	}
 
 	public void addTrack(Track track) {
 		trackList.add(track);
 	}
 
 	public void nextTrack() {
 		if (currentIndex == POSITION_END_OF_PLAYLIST) return;
 		
 		currentIndex++;
 		
 		if (currentIndex >= getSize()) {
 			currentIndex = POSITION_END_OF_PLAYLIST;
 			logger.debug("...End of playlist");
 		}
 	}
 
 	public void previousTrack() {
 		if (currentIndex == POSITION_START_OF_PLAYLIST) return;
 		if (currentIndex == POSITION_END_OF_PLAYLIST) {
 			currentIndex = (trackList.size() - 1);
 			return;
 		}
 		currentIndex--;
 	}
 
 	public void clear() {
 		trackList.clear();
 		currentIndex = POSITION_START_OF_PLAYLIST;
 	}
 
 	public Track getCurrentTrack() {
 		if (currentIndex == POSITION_END_OF_PLAYLIST || currentIndex == POSITION_START_OF_PLAYLIST) {
 			return null;
 		}
 		return trackList.get(currentIndex);
 	}
 
 	public PlaylistRandomiser getPlaylistRandomiser() {
 		return playlistRandomiser;
 	}
 
 	public void setPlaylistRandomiser(PlaylistRandomiser playlistRandomiser) {
 		this.playlistRandomiser = playlistRandomiser;
 	}
 
 	public void randomise() {
 		trackList = playlistRandomiser.randomise(trackList);		
 	}
 
 	public void unRandomise() {
 		trackList = playlistRandomiser.backToOriginalState();
 	}
 	
 	protected List<Track> getTrackList () {
 		return trackList;
 	}
	
	public boolean isStartOfPlaylist () {
		return (currentIndex == POSITION_START_OF_PLAYLIST || currentIndex == 0);
	}

	public boolean isEndOfPlaylist() {
		return (currentIndex == POSITION_END_OF_PLAYLIST || currentIndex == (trackList.size() -1));
	}

 }
