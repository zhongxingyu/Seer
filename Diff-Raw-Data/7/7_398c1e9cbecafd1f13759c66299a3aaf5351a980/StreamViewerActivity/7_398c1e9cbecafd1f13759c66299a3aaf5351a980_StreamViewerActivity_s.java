 package cryptocast.client;
 
 import java.io.InputStream;
 import java.net.InetSocketAddress;
 
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.support.v4.app.FragmentActivity;
 import android.view.MenuItem;
import cryptocast.comm.InChannel;
import cryptocast.playbacktest.SimpleHttpStreamServer;
 
 /**
  * This activity is responsible for decrypting the received data
  * and viewing it.
  */
 public class StreamViewerActivity extends FragmentActivity {
     MediaPlayer player;
     SimpleHttpStreamServer server;
 
     /**
      * Initializes a viewer.
      * @param inputStream The data stream
      */
     public StreamViewerActivity(InputStream inputStream) {
     }
 
    private void startPlayback(InputStream in) {
         player = new MediaPlayer();
         player.setAudioStreamType(AudioManager.STREAM_MUSIC);
         player.setDataSource("http://127.0.0.1:21337/");
         server = new SimpleHttpStreamServer(in, 
                 new InetSocketAddress("127.0.0.1", 21337), "audio/mpeg");
         new Thread(server).start();
         player.prepare();
         player.start();
     }
 
     /** Handles a click on the bottom menu.
      * @param item The clicked menu item
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     }
 
     /**
      * Toggle playback play/pause. Will pause if in play mode and continue if
      * in pause mode.
      */
     public void togglePlay() { }
 
     /**
      * @return Whether the player is in playing mode.
      */
     public boolean isPlaying() { return false; }
 }
