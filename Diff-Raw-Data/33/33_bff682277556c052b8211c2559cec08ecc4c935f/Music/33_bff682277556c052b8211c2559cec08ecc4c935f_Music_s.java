 /***
  * Excerpted from "Hello, Android!",
  * published by The Pragmatic Bookshelf.
  * Copyrights apply to this code. It may not be used to create training material, 
  * courses, books, articles, and the like. Contact us if you are in doubt.
  * We make no guarantees that this code is fit for any purpose. 
  * Visit http://www.pragmaticprogrammer.com/titles/eband for more book information.
 ***/
 package jbs.org.MemoryMap;
 
 import android.content.Context;
 import android.media.MediaPlayer;
 
 public class Music {
    private static MediaPlayer mp = null;
 
    
    /** Stop old song and start new one */
    public static void play(Context context, int resource) {
       stop(context);
 
          mp = MediaPlayer.create(context, resource);
          mp.setLooping(true);
          mp.start();
    }
    
 
    /** Stop the music */
    public static void stop(Context context) { 
       if (mp != null) {
          mp.stop();
         mp.release();
          mp = null;
       }
    }
 }
