 package fr.mildlyusefulsoftware.mpdremote.service;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Transformer;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.bff.javampd.MPD;
 import org.bff.javampd.MPDPlayer;
 import org.bff.javampd.MPDDatabase.ScopeType;
 import org.bff.javampd.events.ConnectionChangeEvent;
 import org.bff.javampd.events.ConnectionChangeListener;
 import org.bff.javampd.events.PlayerBasicChangeEvent;
 import org.bff.javampd.events.PlayerBasicChangeListener;
 import org.bff.javampd.events.PlaylistBasicChangeEvent;
 import org.bff.javampd.events.TrackPositionChangeEvent;
 import org.bff.javampd.events.TrackPositionChangeListener;
 import org.bff.javampd.exception.MPDConnectionException;
 import org.bff.javampd.exception.MPDDatabaseException;
 import org.bff.javampd.exception.MPDPlayerException;
 import org.bff.javampd.exception.MPDPlaylistException;
 import org.bff.javampd.exception.MPDResponseException;
 import org.bff.javampd.monitor.MPDStandAloneMonitor;
 import org.bff.javampd.objects.MPDSong;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import fr.mildlyusefulsoftware.mpdremote.bo.CurrentlyPlayingSong;
 import fr.mildlyusefulsoftware.mpdremote.bo.Song;
 import fr.mildlyusefulsoftware.mpdremote.util.MPDRemoteUtils;
 
 public class MPDService implements
 		org.bff.javampd.events.PlaylistBasicChangeListener,
 		PlayerBasicChangeListener, TrackPositionChangeListener,
 		ConnectionChangeListener {
 
 	private static MPDService instance;
 	private MPD mpd;
 	private List<MPDListener> MPDListeners = new ArrayList<MPDListener>();
 	private boolean connected;
 	private Context context;
 
 	public static MPDService getInstance(Context context) {
 		if (instance == null) {
 			instance = new MPDService(context);
 		}
 		return instance;
 	}
 
 	private MPDService(Context context) {
 		this.context = context;
 		launchConnectThread();
 	}
 
 	public void launchConnectThread() {
 		Log.d(MPDRemoteUtils.TAG, "launching connect thread");
 		final MPDService mpdService = this;
 		setConnected(false);
 		Thread t = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				while (!connected) {
 					try {
 						SharedPreferences prefs = PreferenceManager
 								.getDefaultSharedPreferences(context);
 						String portString = prefs.getString(
 								"mpd_port_preference", "6600");
 						if (!NumberUtils.isNumber(portString)) {
 							portString = "6600";
 						}
 						final String hostname = prefs.getString(
 								"mpd_host_preference", "localhost");
 						mpd = connect(hostname, Integer.valueOf(portString));
 						connected = true;
 						MPDStandAloneMonitor monitor = new MPDStandAloneMonitor(
 								mpd, 500);
 						monitor.addPlaylistChangeListener(mpdService);
 						monitor.addPlayerChangeListener(mpdService);
 						monitor.addTrackPositionChangeListener(mpdService);
 						monitor.addConnectionChangeListener(mpdService);
 						Thread th = new Thread(monitor);
 						th.start();
 						connectionChanged(connected);
 						Log.d(MPDRemoteUtils.TAG, "connected !");
 					} catch (Exception e) {
 						Log.e(MPDRemoteUtils.TAG, e.getMessage(), e);
 						try {
 							Thread.sleep(2000);
 						} catch (InterruptedException f) {
 							Log.e(MPDRemoteUtils.TAG, f.getMessage(), f);
 						}
 					}
 
 				}
 			}
 		});
 		t.start();
 	}
 
 	private List<Song> getCurrentPlayList() {
 		List<Song> songs = new ArrayList<Song>();
 		try {
 			List<MPDSong> mpdSongList;
 			mpdSongList = mpd.getMPDPlaylist().getSongList();
 
 			CollectionUtils.collect(mpdSongList,
 					new MPDSongToSongTransformer(), songs);
 		} catch (MPDPlaylistException e) {
 			Log.e(MPDRemoteUtils.TAG, e.getMessage(), e);
 		} catch (MPDConnectionException e) {
 			handleMPDConnectionException(e);
 		}
 
 		return songs;
 	}
 
 	public void playOrPauseSong(Song s) {
 
 		try {
 			MPDSong currentSong = mpd.getMPDPlayer().getCurrentSong();
 			boolean newSongToPlay = s != null
 					&& (currentSong == null || s
 							.getId()!=currentSong.getId());
 			Log.d(MPDRemoteUtils.TAG, "newsongtoplay ? "+newSongToPlay);
 			if (!newSongToPlay
 					&& mpd.getMPDPlayer().getStatus() == MPDPlayer.PlayerStatus.STATUS_PLAYING) {
 				mpd.getMPDPlayer().pause();
 			} else if (s != null) {
 				MPDSong mpdSong = new MPDSong();
 				mpdSong.setId(s.getId());
 				mpd.getMPDPlayer().playId(mpdSong);
 			} else {
 				mpd.getMPDPlayer().play();
 			}
 		} catch (MPDPlayerException e) {
 			Log.e(MPDRemoteUtils.TAG, e.getMessage(), e);
 		} catch (MPDConnectionException e) {
 			handleMPDConnectionException(e);
 		} catch (MPDResponseException e) {
 			Log.e(MPDRemoteUtils.TAG, e.getMessage(), e);
 		}
 
 	}
 
 	public void playPreviousSong() {
 		try {
 			mpd.getMPDPlayer().playPrev();
 		} catch (MPDPlayerException e) {
 			Log.e(MPDRemoteUtils.TAG, e.getMessage(), e);
 		} catch (MPDConnectionException e) {
 			handleMPDConnectionException(e);
 		}
 	}
 
 	public void playNextSong() {
 		try {
 			mpd.getMPDPlayer().playNext();
 		} catch (MPDPlayerException e) {
 			Log.e(MPDRemoteUtils.TAG, e.getMessage(), e);
 		} catch (MPDConnectionException e) {
 			handleMPDConnectionException(e);
 		}
 	}
 
 	public void seek(int seconds) {
 		try {
 			mpd.getMPDPlayer().seek(seconds);
 		} catch (MPDPlayerException e) {
 			Log.e(MPDRemoteUtils.TAG, e.getMessage(), e);
 		} catch (MPDConnectionException e) {
 			handleMPDConnectionException(e);
 		}
 	}
 
 	public void addSongToPlayList(Song s) {
 		if (s != null) {
 			MPDSong mpdSong = new MPDSong();
 			mpdSong.setId(s.getId());
 			mpdSong.setFile(s.getFilename());
 			Log.d(MPDRemoteUtils.TAG,"add song to playlist : "+s.getFilename());
 			try {
 				mpd.getMPDPlaylist().addSong(mpdSong);
 			} catch (MPDPlaylistException e) {
 				Log.e(MPDRemoteUtils.TAG, e.getMessage(), e);
 			} catch (MPDConnectionException e) {
 				handleMPDConnectionException(e);
 			}
 		}
 
 	}
 
 	public void removeSongFromPlaylist(Song s) {
 		if (s != null) {
 			MPDSong mpdSong = new MPDSong();
 			mpdSong.setId(s.getId());
 			try {
 				mpd.getMPDPlaylist().removeSong(mpdSong);
 			} catch (MPDPlaylistException e) {
 				Log.e(MPDRemoteUtils.TAG, e.getMessage(), e);
 			} catch (MPDConnectionException e) {
 				handleMPDConnectionException(e);
 			}
 		}
 	}
 
 	private MPD connect(String ip, int port) throws UnknownHostException,
 			MPDConnectionException {
 		return new MPD(ip, port);
 	}
 
 	public void addMPDListener(MPDListener p) {
 		MPDListeners.add(p);
 	}
 
 	@Override
 	public void playlistBasicChange(PlaylistBasicChangeEvent event) {
 		List<Song> currentPlaylist = getCurrentPlayList();
 		for (MPDListener p : MPDListeners) {
 			p.playListChanged(currentPlaylist);
 		}
 	}
 
 	public List<Song> getSongsInLibrary(String searchFilter) {
 		List<Song> songs = new ArrayList<Song>();
 		try {
 			Collection<MPDSong> mpdSongs=new ArrayList<MPDSong>();;
 			if (StringUtils.isEmpty(searchFilter)) {
 				mpdSongs.addAll(mpd.getMPDDatabase().listAllSongs());
 			}else{
 				mpdSongs.addAll(mpd.getMPDDatabase().search(ScopeType.ANY, searchFilter));
 			}
 			CollectionUtils.collect(mpdSongs, new MPDSongToSongTransformer(),
 					songs);
 		} catch (MPDDatabaseException e) {
 			Log.e(MPDRemoteUtils.TAG, e.getMessage(), e);
 		} catch (MPDConnectionException e) {
 			handleMPDConnectionException(e);
 		}
 		return songs;
 	}
 
 	private void handleMPDConnectionException(MPDConnectionException e) {
 		Log.e(MPDRemoteUtils.TAG, e.getMessage(), e);
 		connected = false;
 		connectionChanged(connected);
 		launchConnectThread();
 	}
 
 	@Override
 	public void playerBasicChange(PlayerBasicChangeEvent event) {
 		try {
 			CurrentlyPlayingSong currentlyPlayingSong = new CurrentlyPlayingSong();
 			currentlyPlayingSong.setElapsedTime(mpd.getMPDPlayer()
 					.getElapsedTime());
 			currentlyPlayingSong.setSong((Song) new MPDSongToSongTransformer()
 					.transform(mpd.getMPDPlayer().getCurrentSong()));
 
 			for (MPDListener p : MPDListeners) {
 				p.currentlyPlayingSongChanged(currentlyPlayingSong);
 			}
 		} catch (MPDPlayerException e) {
 			Log.e(MPDRemoteUtils.TAG, e.getMessage(), e);
 		} catch (MPDConnectionException e) {
 			handleMPDConnectionException(e);
 		}
 
 	}
 
 	@Override
 	public void trackPositionChanged(TrackPositionChangeEvent event) {
 		playerBasicChange(null);
 	}
 
 	public void connectionChanged(boolean connected) {
 		for (MPDListener p : MPDListeners) {
 			p.connectionChanged(connected);
 		}
		if (connected){
			List<Song> currentPlaylist = getCurrentPlayList();
			for (MPDListener p : MPDListeners) {
				p.playListChanged(currentPlaylist);
			}
		}
 	}
 
 	public boolean isConnected() {
 		return connected;
 	}
 
 	public boolean isPaused() {
 		try {
 			return MPDPlayer.PlayerStatus.STATUS_PAUSED == mpd.getMPDPlayer()
 					.getStatus();
 		} catch (MPDResponseException e) {
 			Log.e(MPDRemoteUtils.TAG, e.getMessage(), e);
 		} catch (MPDConnectionException e) {
 			handleMPDConnectionException(e);
 		}
 		return true;
 	}
 
 	public boolean isPlaying() {
 		try {
 			return MPDPlayer.PlayerStatus.STATUS_PLAYING == mpd.getMPDPlayer()
 					.getStatus();
 		} catch (MPDResponseException e) {
 			Log.e(MPDRemoteUtils.TAG, e.getMessage(), e);
 		} catch (MPDConnectionException e) {
 			handleMPDConnectionException(e);
 		}
 		return false;
 	}
 
 	public void setConnected(boolean connected) {
 		this.connected = connected;
 	}
 
 	@Override
 	public void connectionChangeEventReceived(ConnectionChangeEvent event) {
 		Log.d(MPDRemoteUtils.TAG, "cnx changed");
 		setConnected(mpd != null && mpd.isConnected());
 		Log.d(MPDRemoteUtils.TAG, "cnx changed before is connected");
 		connectionChanged(isConnected());
 		Log.d(MPDRemoteUtils.TAG, "cnx changed after is connected");
 		launchConnectThread();
 		Log.d(MPDRemoteUtils.TAG, "cnx changed after launch connect");
 	}
 
 	public void addSongToPlayList(List<Song> songsToAdd) {
 		Log.d(MPDRemoteUtils.TAG,"add song to playlist");
 		for (Song s:songsToAdd){
 			Log.d(MPDRemoteUtils.TAG,"add song "+s.getId());
 			addSongToPlayList(s);
 		}
 		
 	}
 
 }
 
 class MPDSongToSongTransformer implements Transformer {
 
 	@Override
 	public Object transform(Object m) {
 		Song s = new Song();
 		MPDSong mpdSong = (MPDSong) m;
 		s.setId(mpdSong.getId());
 		s.setTitle(mpdSong.getTitle());
 		if (StringUtils.isBlank(s.getTitle())) {
 			s.setTitle(mpdSong.getFile());
 		}
 		s.setFilename(mpdSong.getFile());
 		s.setLength(mpdSong.getLength());
 		return s;
 	}
 
 }
