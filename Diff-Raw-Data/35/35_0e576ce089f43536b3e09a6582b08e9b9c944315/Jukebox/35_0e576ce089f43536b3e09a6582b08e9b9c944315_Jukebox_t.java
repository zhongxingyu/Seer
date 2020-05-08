 package com.mindbadger.jukebox;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.mindbadger.broadcast.StatusBroadcaster;
 import com.mindbadger.cache.MediaPlayerCache;
 import com.mindbadger.library.Artist;
 import com.mindbadger.library.MediaItem;
 import com.mindbadger.library.Track;
 import com.mindbadger.library.Album;
 import com.mindbadger.player.AudioPlayer;
 import com.mindbadger.player.IPlayAudio;
 import com.mindbadger.player.IReceiveStatusUpdatesFromAMediaPlayer;
 import com.mindbadger.player.PlayerStatus;
 
 public class Jukebox implements IReceiveStatusUpdatesFromAMediaPlayer {
 	Logger logger = Logger.getLogger(Jukebox.class);
 
 	private IPlayAudio audioPlayer;
 	private MediaPlayerCache mediaPlayerCache;
 	private StatusBroadcaster statusBroadcaster;
 	private PlayList playList;
 
 	protected boolean repeat = false;
 	protected boolean shuffle = false;
 
 	public Jukebox(MediaPlayerCache mediaPlayerCache) {
 		this.mediaPlayerCache = mediaPlayerCache;
 	}
 
 	@Override
 	public void songStarted() {
 		logger.debug("PLAYER STATUS CHANGED: songStarted");
 		broadcastStatus();
 	}
 
 	@Override
 	public void songPaused() {
 		logger.debug("PLAYER STATUS CHANGED: songPaused");
 		broadcastStatus();
 	}
 
 	@Override
 	public void songEnded() {
 		logger.debug("PLAYER STATUS CHANGED: ended");
 		playList.nextTrack();
 		playTrackIfOneAvailable();
 		broadcastStatus();
 	}
 
 	private void playTrackIfOneAvailable() {
 		audioPlayer.stopPlayingAudioFile();
 
 		if (playList.getCurrentIndex() == PlayList.POSITION_START_OF_PLAYLIST) {
 			logger.debug("Can't play track - start of playlist");
 		} else if (playList.getCurrentIndex() == PlayList.POSITION_END_OF_PLAYLIST) {
 			logger.debug("Can't play track - end of playlist");
 		} else {
 			playTrack();
 		}
 	}
 
 	public void addItemToPlaylist(int mediaItemId) {
 		logger.debug("Adding Item to playlist: " + mediaItemId);
 
 		boolean nothingCurrentlyPlaying = (playList.getSize() == 0);
 
 		MediaItem mediaItem = mediaPlayerCache.getMediaItemWithId(mediaItemId);
 
 		if (mediaItem instanceof Track) {
 			addTrackWithIdToPlaylist((Track) mediaItem);
 		} else if (mediaItem instanceof Album) {
 			Album album = (Album) mediaItem;
 			addAlbumToPlaylist(album);
 		} else if (mediaItem instanceof Artist) {
 			Artist artist = (Artist) mediaItem;
 			addArtistToPlaylist(artist);
 		}
 
 
 		if (nothingCurrentlyPlaying) {
 			playList.nextTrack();
 			playTrack();
 		}
 	}
 
 	private void playTrack() {
 		Track trackToPlay = playList.getCurrentTrack();
 		File audioFile = new File(trackToPlay.getFullyQualifiedFileName());
 		logger.debug("Playing track: " + audioFile);
 		audioPlayer.playAudioFile(audioFile);
 	}
 
 	private void addTrackWithIdToPlaylist(Track track) {
 		playList.addTrack(track);
 	}
 
 	private void addAlbumToPlaylist(Album album) {
 		Map<Integer, Track> tracks = album.getTracks();
 		List<Track> trackList = new ArrayList<Track>(tracks.values());
 
 		for (Track track : trackList) {
 			addTrackWithIdToPlaylist(track);
 		}
 	}
 
 	private void addArtistToPlaylist(Artist artist) {
 		Map<String, Album> albums = artist.getAlbums();
 		List<Album> albumList = new ArrayList<Album>(albums.values());
 		Collections.sort(albumList);
 		for (Album album : albumList) {
 			addAlbumToPlaylist(album);
 		}
 	}
 
 	public void playOrPause() {
 		boolean pause = (audioPlayer.getAudioPlayerStatus() == PlayerStatus.PLAYING);
 		audioPlayer.pause(pause);
 	}
 
 	public void nextTrack() {
 		playList.nextTrack();
 		playTrackIfOneAvailable();
 	}
 
 	public void previousTrack() {
 		playList.previousTrack();
 		playTrackIfOneAvailable();
 	}
 
 	public void toggleShuffle() {
 		shuffle = !shuffle;
 
 		if (shuffle) {
 			playList.randomise();
 		} else {
 			playList.unRandomise();
 		}
 		playTrackIfOneAvailable();
 		broadcastStatus();
 	}
 
 	public void toggleRepeat() {
 		repeat = !repeat;
 		broadcastStatus();
 	}
 
 	public void clearPlaylist() {
 		playList.clear();
 		audioPlayer.stopPlayingAudioFile();
 		broadcastStatus();
 	}
 
 	public void broadcastStatus() {
 		int currentTrackId = 0;
 		if (playList.getCurrentTrack() != null) {
 			currentTrackId = playList.getCurrentTrack().getId();
 		}
 		statusBroadcaster.broadcast(audioPlayer.getAudioPlayerStatus().toString(), currentTrackId, repeat, shuffle, "");
 	}
 
 	public Track getCurrentTrack () {
		return (Track) mediaPlayerCache.getMediaItemWithId(playList.getCurrentTrack().getId());
 	}
 	
 	public String getArtworkForTrack(int trackId) {
 		
 		MediaItem mediaItem = mediaPlayerCache.getMediaItemWithId(trackId);
 		
 		if (!(mediaItem instanceof Track)) {
 			throw new IllegalArgumentException("You can only retrieve artwork for a Track");
 		}
 		
 		return ((Track) mediaItem).getArtworkFile();
 	}
 
 	public boolean isRepeat() {
 		return repeat;
 	}
 
 	public boolean isShuffle() {
 		return shuffle;
 	}
 
 	public void setStatusBroadcaster(StatusBroadcaster statusBroadcaster) {
 		this.statusBroadcaster = statusBroadcaster;
 	}
 
 	public StatusBroadcaster getStatusBroadcaster() {
 		return statusBroadcaster;
 	}
 
 	public void setAudioPlayer(AudioPlayer audioPlayer) {
 		this.audioPlayer = audioPlayer;
 	}
 
 	public PlayList getPlayList() {
 		return playList;
 	}
 
 	public void setPlayList(PlayList playList) {
 		this.playList = playList;
 	}
 	
 	
 	
 	public boolean isStartOfPlaylist () {
		return playList.isStartOfPlaylist();
 	}
 
 	public boolean isEndOfPlaylist() {
		return playList.isEndOfPlaylist();
 	}
 }
