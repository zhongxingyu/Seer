 package com.ouchadam.fang.audio;
 
 import android.content.Context;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.util.Log;
 
 import com.novoda.notils.logger.Novogger;
 import com.ouchadam.fang.domain.PodcastPosition;
 import com.ouchadam.fang.notification.FangNotification;
 import com.ouchadam.fang.presentation.AudioFocusManager;
 import com.ouchadam.fang.audio.event.PlayerEvent;
 
 import java.io.IOException;
 
 class PlayerHandler implements PlayerEventReceiver.PlayerEventCallbacks {
 
     private final PodcastPlayer podcastPlayer;
     private final AudioFocusManager audioFocusManager;
     private final AudioSync audioSync;
     private final PlayingItemStateManager itemStateManager;
     private final FangNotification notification;
     private final ServiceManipulator serviceManipulator;
     private final AudioCompletionHandler audioCompletionHandler;
     private final Playlist playlist;
     private final RemoteHelper remoteHelper;
 
     interface AudioSync {
         void onSync(long itemId, PlayerEvent playerEvent);
     }
 
     static PlayerHandler from(Context context, AudioSync audioSync, AudioCompletionHandler audioCompletionHandler, ServiceManipulator serviceManipulator, RemoteHelper remoteHelper) {
         PodcastPlayer player = new PodcastPlayer(context, new PodcastPositionBroadcaster(context));
         AudioFocusManager focusManager = new AudioFocusManager((AudioManager) context.getSystemService(Context.AUDIO_SERVICE));
         PlayingItemStateManager itemStateManager = PlayingItemStateManager.from(context);
         FangNotification notification = FangNotification.from(context);
         Playlist playlist = Playlist.from(context);
         return new PlayerHandler(player, focusManager, audioSync, itemStateManager, audioCompletionHandler, notification, serviceManipulator, playlist, remoteHelper);
     }
 
     PlayerHandler(PodcastPlayer podcastPlayer, AudioFocusManager audioFocusManager, AudioSync audioSync, PlayingItemStateManager itemStateManager, AudioCompletionHandler audioCompletionHandler, FangNotification notification, ServiceManipulator serviceManipulator, Playlist playlist, RemoteHelper remoteHelper) {
         this.podcastPlayer = podcastPlayer;
         this.audioFocusManager = audioFocusManager;
         this.audioSync = audioSync;
         this.itemStateManager = itemStateManager;
         this.audioCompletionHandler = audioCompletionHandler;
         this.notification = notification;
         this.serviceManipulator = serviceManipulator;
         this.playlist = playlist;
         this.remoteHelper = remoteHelper;
         podcastPlayer.setCompletionListener(onCompletionWrapper);
     }
 
     private final MediaPlayer.OnCompletionListener onCompletionWrapper = new MediaPlayer.OnCompletionListener() {
         @Override
         public void onCompletion(MediaPlayer mediaPlayer) {
             audioCompletionHandler.onCompletion(PlayerHandler.this);
         }
     };
 
     @Override
     public void onNewSource(int playlistPosition, String playlistName) {
         Log.e("!!!", "wants to play position : " + playlistPosition);
         playlist.load();
         playlist.moveTo(playlistPosition);
         setSource();
     }
 
     private void setSource() {
         Playlist.PlaylistItem playItem = playlist.get();
         playlist.setCurrent(playItem.id);
         remoteHelper.update(playItem);
         setAudioSource(playItem.source);
         sync(new PlayerEvent.Factory().newSource(playlist.getCurrentPosition(), "PLAYLIST"));
     }
 
     private void setAudioSource(Uri uri) {
         try {
             podcastPlayer.setSource(uri);
         } catch (IOException e) {
             e.printStackTrace();
             Novogger.e(e.getMessage());
             throw new RuntimeException("couldn't find : " + uri);
         }
     }
 
     @Override
     public void onPlay() {
         onPlay(playlist.get().podcastPosition);
     }
 
     @Override
     public void onPlay(PodcastPosition position) {
         play(position);
         sync(new PlayerEvent.Factory().play(position));
     }
 
     private void play(PodcastPosition position) {
         remoteHelper.setPlaying();
         audioFocusManager.requestFocus();
         podcastPlayer.play(position);
     }
 
     @Override
     public void onPause() {
         pauseAudio();
         saveCurrentPlayState();
         sync(new PlayerEvent.Factory().pause());
     }
 
     public boolean lastInPlaylist() {
         return playlist.isLast();
     }
 
     public void completeAndPlayNext() {
         if (playlist.hasNext()) {
             completeAudio();
             playlist.moveToNext();
             setSource();
             play();
         }
     }
 
     private void play() {
         onPlay(playlist.get().podcastPosition);
     }
 
     public void completeAudio() {
         pauseAudio();
         saveCompletedState();
         sync(new PlayerEvent.Factory().pause());
     }
 
     private void pauseAudio() {
         remoteHelper.setPaused();
         podcastPlayer.pause();
     }
 
     private void saveCompletedState() {
         itemStateManager.persist(playlist.getCurrentId(), podcastPlayer.getCompletedPosition(), podcastPlayer.getSource(), playlist.getCurrentPosition());
     }
 
     @Override
     public void onStop() {
         stopAudio();
         notification.dismiss();
         serviceManipulator.stop();
     }
 
     private void saveCurrentPlayState() {
         if (podcastPlayer.isPrepared() && podcastPlayer.hasChanged()) {
             itemStateManager.persist(playlist.getCurrentId(), podcastPlayer.getPosition(), podcastPlayer.getSource(), playlist.getCurrentPosition());
         }
     }
 
     private void stopAudio() {
         audioFocusManager.abandonFocus();
         podcastPlayer.release();
     }
 
     @Override
     public void gotoPosition(PodcastPosition position) {
         podcastPlayer.goTo(position.value());
         saveCurrentPlayState();
         sync(new PlayerEvent.Factory().goTo(position));
     }
 
     @Override
     public void onReset() {
         stopAudio();
         itemStateManager.resetCurrentItem();
         playlist.resetCurrent();
     }
 
     private void sync(PlayerEvent playerEvent) {
         audioSync.onSync(playlist.getCurrentId(), playerEvent);
     }
 
     public SyncEvent asSyncEvent() {
         return playlist.currentItemIsValid() ? createValidSyncEvent() : SyncEvent.fresh();
     }
 
     private SyncEvent createValidSyncEvent() {
         return podcastPlayer.isNotPrepared() ? SyncEvent.idle(playlist.getCurrentId()) : createCurrentSyncEvent();
     }
 
     private SyncEvent createCurrentSyncEvent() {
         return new SyncEvent(podcastPlayer.isPlaying(), podcastPlayer.getPosition(), playlist.getCurrentId());
     }
 
     public void restoreItem() {
         PlayItem playItem = itemStateManager.getStoredItem();
         if (playItem.isValid()) {
             playlist.setCurrent(playItem.getId());
             onNewSource(playItem.getPlaylistPosition(), "Playlist");
         }
     }
 
 }
