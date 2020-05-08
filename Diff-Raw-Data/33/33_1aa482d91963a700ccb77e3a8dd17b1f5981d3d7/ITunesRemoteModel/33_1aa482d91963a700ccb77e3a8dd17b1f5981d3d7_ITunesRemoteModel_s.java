 /*
  * Copyright (c) 2011, dan.clark@nekocode.org
  *
  * Licensed under FreeBSD license.  See README for details.
  */
 
 package org.nekocode.itunes.remote;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.nekocode.itunes.remote.connection.*;
 import org.nekocode.nowplaying.AbstractMediaPlayer;
 import org.nekocode.nowplaying.MediaPlayer;
 import org.nekocode.nowplaying.NowPlayingProperties;
 import org.nekocode.nowplaying.events.TrackChangeEvent;
 import org.nekocode.nowplaying.objects.Playlist;
 import org.nekocode.nowplaying.objects.Track;
 
 import java.io.IOException;
 import java.net.SocketTimeoutException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Properties;
 
 import static org.nekocode.itunes.remote.connection.ContentCode.*;
 import static org.nekocode.nowplaying.events.TrackChangeEvent.ChangeType.*;
 
 /**
  * Media Player that uses the iTunes Remote protocol (Digital Audio Control Protocol (DACP))
  */
 public class ITunesRemoteModel extends AbstractMediaPlayer {
     private static final Logger log = LogManager.getLogger(ITunesRemoteModel.class);
 
     private RemoteSession session;
     private String playerGUID;
     private ITunesRemoteListener remoteListener;
 
     private Track currentTrack;
     private ITunesRemoteResponse currentPlayStatus;
 
     public ITunesRemoteModel() {
         Properties properties = NowPlayingProperties.loadProperties();
 
 
         playerGUID = properties.getProperty(NowPlayingProperties.MEDIA_PLAYER_GUID.name());
         final String host = properties.getProperty(NowPlayingProperties.REMOTE_MACHINE.name(), "localhost");
 
         RemoteSession remoteSession = null;
         if (playerGUID != null && playerGUID.length() == 16) {
             try {
                 remoteSession = new RemoteSession(host, playerGUID);
             } catch (IOException e) {
                 // this will happen if the RemoteSession is not able to connect.
                 // an "expected" exception.
                 // remoteSession remains null and the following block handles this
             }
         }
 
         if (remoteSession == null) {
             log.info("Unable to connect using existing pairing: " + playerGUID);
 
             Runnable pairingServer = new Runnable() {
                 public void run() {
                     // we were unable to connect using a previous GUID, so initiate a new pairing
                     try {
                         RemoteSession remoteSession = RemoteSession.pairWithITunes();
                         setSession(remoteSession);
                     } catch (IOException e) {
                         log.error("Unable to pair", e);
                     }
                 }
             };
             Thread pairingServerThread = new Thread(pairingServer);
             pairingServerThread.setDaemon(true);
             pairingServerThread.start();
         } else {
             // we didn't need to pair, so use the session we just set up
             setSession(remoteSession);
             log.info("Able to connect using existing pairing: " + playerGUID);
         }
     }
 
     public void setSession(RemoteSession session) {
         this.session = session;
 
         if (!session.getPairingGuid().equals(playerGUID)) {
             playerGUID = session.getPairingGuid();
             log.info("Changing registered GUID to " + playerGUID);
             NowPlayingProperties.loadProperties().setProperty(NowPlayingProperties.MEDIA_PLAYER_GUID.name(), playerGUID);
             log.info("Changing connected host to " + session.getHost());
             NowPlayingProperties.loadProperties().setProperty(NowPlayingProperties.REMOTE_MACHINE.name(), session.getHost());
         }
 
         // loads the current track and triggers an update
         getCurrentTrack();
 
         remoteListener = new ITunesRemoteListener();
         Thread remoteListenerThread = new Thread(remoteListener, "Remote Listener Thread");
         remoteListenerThread.setDaemon(true);
         remoteListenerThread.start();
     }
 
     @Override
     public Track getCurrentTrack() {
         if (session == null) {
             return null;
         }
 
         if (currentTrack == null) {
             loadNowPlaying();
         }
         return currentTrack;
     }
 
     /**
      * Force a load of the currently playing.
      */
     public void loadNowPlaying() {
         try {
             ITunesRemoteResponse response = session.getPlayStatusUpdate();
             handlePlayStatusUpdate(response);
         } catch (IOException e) {
             e.printStackTrace();
             currentTrack = null;
         }
     }
 
     private void handlePlayStatusUpdate(ITunesRemoteResponse update) {
         TrackChangeEvent.ChangeType change = null;
 
         if (currentPlayStatus == null) {
             change = FILE_CHANGE;
         } else {
             ITunesRemoteResponse diff = new ITunesRemoteResponseDiff(currentPlayStatus, update);
             ITunesRemoteResponse status = diff.getBranch(DMCP_STATUS);
 
             if (status.isEmpty()) {
                 // nothing changed, do nothing
                 return;
             }
 
             if (status.containsLeaf(ITEM_ID)) {
                 change = CURRENT_SONG_CHANGE;
             } else if (status.containsLeaf(DACP_PLAY_STATUS)) {
                 change = PLAY_STATE_CHANGE;
             } else if (status.containsLeaf(DACP_ALBUM_SHUFFLE_STATUS)) {
                 // no need to do an update
             } else if (status.containsLeaf(DACP_REPEAT_STATUS)) {
                 // no need to do an update
             } else if (status.containsLeaf(DACP_SHUFFLE_STATUS)) {
                 // no need to do an update
             } else if (status.containsLeaf(DACP_REMAINING_TIME)) {
                 // DEFINITELY don't do an update (this will constantly be changing)
             } else  {
                 // something changed... not sure what
                 // do a file change just to be safe
                 change = FILE_CHANGE;
                 if (log.isDebugEnabled()) {
                     log.debug("Doing FILE_CHANGE because of the following change:\n" + diff.toString());
                 }
             }
         }
 
         // always update with the latest
         currentPlayStatus = update;
         if (change == null)
             return;
         
         switch (change) {
             case FILE_CHANGE:
             case CURRENT_SONG_CHANGE:
                 handleTrackChange(update, change);
                 break;
             case PLAY_STATE_CHANGE:
             case TAG_CHANGE:
                 fireTrackChanged(new TrackChangeEvent(currentTrack, change));
                 break;
         }
     }
 
     /**
      * Change tracks.  This should be the only place that currentTrack is set.
      *
      * @param playStatusUpdate message received from server
      * @param change update type
      */
     private void handleTrackChange(ITunesRemoteResponse playStatusUpdate, TrackChangeEvent.ChangeType change) {
         if (!playStatusUpdate.hasLeaf(DMCP_STATUS, ITEM_ID)) {
             log.debug("No song playing.  Using EmptyTrack");
             currentTrack = new EmptyTrack();
             fireTrackChanged(new TrackChangeEvent(currentTrack, change));
         } else {
             try {
                 currentTrack = new SelfLoadingTrack(
                         playStatusUpdate.getInt(DMCP_STATUS, ITEM_ID),
                         session);
                 fireTrackChanged(new TrackChangeEvent(currentTrack, change));
             } catch (IOException e) {
                 log.error("Error loading track information", e);
             }
         }
     }
 
     @Override
     public void play() {
         if (session == null) {
             log.info("Unable to process command: not yet paired with iTunes");
             return;
         }
 
         if (getPlayerState() == MediaPlayer.PlayerState.PLAYING) {
             // no need to change state
             return;
         }
 
         try {
             session.playPause();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public void pause() {
         if (session == null) {
             log.info("Unable to process command: not yet paired with iTunes");
             return;
         }
 
         if (getPlayerState() != MediaPlayer.PlayerState.PLAYING) {
             // can't pause unless we're currently playing
             return;
         }
 
         try {
             session.playPause();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public void next() {
         if (session == null) {
             log.info("Unable to process command: not yet paired with iTunes");
             return;
         }
         try {
             session.next();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public void previous() {
         if (session == null) {
             log.info("Unable to process command: not yet paired with iTunes");
             return;
         }
         try {
             session.prev();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public void onShutdown() {
         if (remoteListener != null) {
             log.debug("Shutting down RemoteListener");
             remoteListener.stop();
         }
         log.debug("Shutting down RequestManager");
         RequestManager.shutdown();
         log.debug("Finished shutting down RequestManager");
     }
 
     @Override
     public void updateTrackRating(Track track, int newRating) {
         if (track.getTrackId() != currentTrack.getTrackId()) {
             log.warn("Attempted to set rating of track that isn't current track.  Rejecting execution.");
             return;
         }
         try {
             session.setRating(track.getTrackId(), newRating);
         } catch (IOException e) {
             log.error("Error setting track rating", e);
         }
     }
 
     @Override
     public double getCurrentTrackPosition() {
         if (session == null) {
             return 0;
         }
 
         try {
             ITunesRemoteResponse response = session.getPlayStatusUpdate();
             if (response.hasLeaf(DMCP_STATUS, DACP_TOTAL_TIME)) {
                 int totalTime = response.getInt(DMCP_STATUS, DACP_TOTAL_TIME);
                 int remainingTime = response.getInt(DMCP_STATUS, DACP_REMAINING_TIME);
                 return (totalTime - remainingTime) / 1000.0;
             } else {
                 return 0;
             }
         } catch (IOException e) {
             e.printStackTrace();
             return 0;
         }
     }
 
     @Override
     public Playlist getCurrentPlaylist() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public PlayerState getPlayerState() {
         if (session == null) {
             return PlayerState.STOPPED;
         }
 
         int playStatus = currentPlayStatus.getBranch(DMCP_STATUS).getNumber(DACP_PLAY_STATUS).intValue();
 //        log.debug("Current Play Status: " + (playStatus == 4 ? PlayerState.PLAYING : PlayerState.STOPPED));
         return playStatus == 4 ? PlayerState.PLAYING : PlayerState.STOPPED;
     }
 
     @Override
     public List<Track> findTracks(String title, String artist, String album) {
         try {
             ITunesRemoteResponse response = session.findTracks(title, artist, album, ITunesRemoteTrack.DEFAULT_CONTENT_CODES);
             ContentCode listType = response.hasLeaf(DAAP_DATABASE_SONGS) ? DAAP_DATABASE_SONGS : DAAP_PLAYLIST_SONGS;
             List<ITunesRemoteResponse> tracksRaw = response.getMultiBranch(listType, DMAP_LIST, DMAP_LIST_ITEM);
             List<Track> tracks = new ArrayList<>(tracksRaw.size());
             for (ITunesRemoteResponse trackRaw : tracksRaw) {
                 ITunesRemoteTrack remoteTrack = new ITunesRemoteTrack(session.getDatabaseId(), trackRaw);
                 // MonkeyTunes has a bug where you can't do an AND search, only OR, so need to manually filter results
                 // (make sure to do this filtering before creating the self-loading track)
 
                if (title == null || title.equals(remoteTrack.getTitle()))
 //                    if (artist == null || artist.equals(remoteTrack.getArtist())) // multiple artists don't work right
                        if (album == null || album.equals(remoteTrack.getAlbum()))
                             tracks.add(remoteTrack);
             }
             return tracks;
         } catch (IOException e) {
             log.error("error searching for tracks", e);
             return Collections.emptyList();
         }
     }
 
     @Override
     public List<String> findTrackIds(String title, String artist, String album) {
         try {
             ITunesRemoteResponse response = session.findTracks(title, artist, album, DMAP_PERSISTENT_ID);
             List<ITunesRemoteResponse> tracksRaw = response.getMultiBranch(DAAP_DATABASE_SONGS, DMAP_LIST, DMAP_LIST_ITEM);
             List<String> trackIds = new ArrayList<>(tracksRaw.size());
             for (ITunesRemoteResponse trackRaw : tracksRaw) {
                 if (!trackRaw.hasLeaf(DMAP_PERSISTENT_ID)) {
                     trackRaw = trackRaw.getMultiBranch(DAAP_DATABASE_SONGS, DMAP_LIST, DMAP_LIST_ITEM).get(0);
                 }
                 trackIds.add(trackRaw.getHexNumber(DMAP_PERSISTENT_ID));
             }
             return trackIds;
         } catch (IOException e) {
             log.error("error searching for tracks", e);
             return Collections.emptyList();
         }
     }
 
     private class ITunesRemoteListener implements Runnable {
 
         private volatile boolean running;
 
         public ITunesRemoteListener() {
             running = false;
         }
 
         @Override
         public void run() {
             running = true;
             while (running) {
                 try {
                     // sleep between updates so we don't flood the server
                     Thread.sleep(1000);
                     ITunesRemoteResponse response = session.waitForPlayStatusUpdate();
                     handlePlayStatusUpdate(response);
                 } catch (SocketTimeoutException e) {
                     // refresh the revision number
                     try {
                         session.getPlayStatusUpdate();
                     } catch (IOException e1) {
                         log.error("Socket Timed out, unable to reconnect.  Original Exception", e);
                         log.error("Socket Timed out, unable to reconnect.  Reconnection Exception", e1);
                     }
                 } catch (InterruptedException e) {
                     // do nothing
                 } catch (IOException e) {
                     if (running) {
                         log.error("Error ", e);
                     }
                     // if we are not running anymore, we'll probably get some
                     // IOExceptions from connections that got interrupted
                 }
             }
             log.debug("finished shutting down RemoteListener");
         }
 
         public void stop() {
             running = false;
         }
     }
 }
