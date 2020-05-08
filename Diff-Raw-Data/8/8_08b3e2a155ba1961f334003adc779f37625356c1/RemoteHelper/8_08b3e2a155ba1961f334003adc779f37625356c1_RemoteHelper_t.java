 package com.ouchadam.fang.audio;
 
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.media.AudioManager;
 import android.media.MediaMetadataRetriever;
 import android.media.RemoteControlClient;
 import android.util.Log;
 import android.view.KeyEvent;
 
 import com.ouchadam.fang.R;
 import com.squareup.picasso.Picasso;
 
 import java.io.IOException;
 
 public class RemoteHelper {
 
     private final Context context;
     private final AudioManager audioManager;
     private RemoteControlClient remoteControlClient;
 
     public RemoteHelper(Context context) {
         audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
         this.context = context;
     }
 
     public void init() {
         Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
 
         ComponentName mMediaButtonReceiverComponent = new ComponentName(context, MusicIntentReceiver.class);
 
         audioManager.registerMediaButtonEventReceiver(mMediaButtonReceiverComponent);
 
         Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
         intent.setComponent(mMediaButtonReceiverComponent);
         remoteControlClient = new RemoteControlClient(PendingIntent.getBroadcast(context, 0, intent, 0));
 
         audioManager.registerRemoteControlClient(remoteControlClient);
 
 
         remoteControlClient.setTransportControlFlags(
                 RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE |
                         RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
                         RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS);
 
         remoteControlClient.editMetadata(true)
                 .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, "Title")
                 .putString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, "Artist")
                 .putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, bitmap)
                 .apply();
 
     }
 
     public void update(Playlist.PlaylistItem playlistItem) {
         update(playlistItem.imageUrl, playlistItem.title, playlistItem.channel);
     }
 
     public void update(final String imgUrl, final String title, final String channel) {
         new Thread(new Runnable() {
             @Override
             public void run() {
                 try {
                     Log.e("???", "loading img : " + imgUrl);
                     Bitmap bitmap = Picasso.with(context).load(imgUrl).get();
                     remoteControlClient.editMetadata(true).putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, bitmap).putString(MediaMetadataRetriever.METADATA_KEY_TITLE, title)
                             .putString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, channel).apply();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }).start();
 
     }
 
     public void setPlaying() {
         remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
     }
 
     public void setPaused() {
        remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
     }
 
     public void unregister() {
         audioManager.unregisterRemoteControlClient(remoteControlClient);
     }
 
     private static class MusicIntentReceiver extends BroadcastReceiver {
         @Override
         public void onReceive(Context context, Intent intent) {
 
             Log.e("???", "Music intent receiver : " + intent.getAction());
 
             if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
                 KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
                 if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                     return;
 
                 switch (keyEvent.getKeyCode()) {
                     case KeyEvent.KEYCODE_HEADSETHOOK:
                     case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                         break;
                     case KeyEvent.KEYCODE_MEDIA_PLAY:
                         break;
                     case KeyEvent.KEYCODE_MEDIA_PAUSE:
                         break;
                     case KeyEvent.KEYCODE_MEDIA_STOP:
                         break;
                     case KeyEvent.KEYCODE_MEDIA_NEXT:
                         break;
                     case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                         // TODO: ensure that doing this in rapid succession actually plays the
                         // previous song
                         break;
                 }
             }
         }
     }
 
 }
