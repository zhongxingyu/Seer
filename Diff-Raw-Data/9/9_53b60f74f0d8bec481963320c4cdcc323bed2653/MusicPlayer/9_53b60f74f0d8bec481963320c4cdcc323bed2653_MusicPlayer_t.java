 /*
  *  Wezzle
  *  Copyright (c) 2007-2008 Couchware Inc.  All rights reserved.
  */
 
 package ca.couchware.wezzle2d.audio;
 
import ca.couchware.wezzle2d.Game;
 import ca.couchware.wezzle2d.util.CouchLogger;
 import ca.couchware.wezzle2d.util.AtomicDouble;
 import ca.couchware.wezzle2d.util.NumUtil;
 import java.io.BufferedInputStream;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Map;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javazoom.jlgui.basicplayer.BasicController;
 import javazoom.jlgui.basicplayer.BasicPlayer;
 import javazoom.jlgui.basicplayer.BasicPlayerEvent;
 import javazoom.jlgui.basicplayer.BasicPlayerException;
 import javazoom.jlgui.basicplayer.BasicPlayerListener;
 
 /**
  * An extension to the JavaZoom BasicPlayer class that incorporates more
  * advanced features such as fading in and out and looping.
  * 
  * @author cdmckay
  */
 public class MusicPlayer
 {
     private static final double MIN_GAIN = 0.0;
     private static final double MAX_GAIN = 1.0;
     private static final double EPSILON = 0.001;
     private static final double FADE_DELTA = 0.02;
 
     private static final int FADE_PERIOD = 100;
     private static final int STOP_PERIOD = 500;
 
     /** It is used to fade the volume. */
     private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);        
     
     /** The basic player that this wraps. */
     final private BasicPlayer player;
     
     /**
      * The player lock.
      */
     final private Object playerLock = new Object();
     
     /**
      * The future reference for the fader.
      */
     private Future fadeFuture;
     
     /**
      * The future reference lock.
      */
     final private Object fadeFutureLock = new Object();
     
     /**
      * The future reference for the stop at gain thing.
      */
     private Future stopFuture;
     
      /**
      * The future reference lock.
      */
     final private Object stopFutureLock = new Object();
     
     /**
      * The current normalizedGain.
      */
     private AtomicDouble normalizedGain = new AtomicDouble();
     
     /**
      * Should we loop this player?
      */
     private AtomicBoolean loop = new AtomicBoolean(false);
     
     /**
      * Has the player finished playing the track?
      */
     private AtomicBoolean finished = new AtomicBoolean(false);
 
     static
     {
        // Turn off logging on the basic player.
        if (!Game.isApplet())
        {
            Logger.getLogger(BasicPlayer.class.getName()).setLevel(Level.OFF);
        }
     }
 
     /**
      * The constructor.
      */
     private MusicPlayer()
     {         
         // Create a new basic player.
         this.player = new BasicPlayer();
         this.player.addBasicPlayerListener(new MusicPlayerListener());
     }    
     
     /**
      * Create a new music player instance.
      * 
      * @return
      */
     public static MusicPlayer newInstance()
     {
         return new MusicPlayer();
     }
     
     public void open(InputStream stream) throws BasicPlayerException
     {
         BufferedInputStream buffered = new BufferedInputStream(stream);
         synchronized (playerLock)
         { 
             player.open(buffered);
         }
     }
     
     public void play() throws BasicPlayerException
     {
         if (finished.get() == true)
         {
             CouchLogger.get().recordWarning(this.getClass(), "Attempted to play a track that was finished");
             return;
         }
         
         synchronized (playerLock)
         { 
             player.play();
         }
     }
     
     public void stop() throws BasicPlayerException
     {
         synchronized (playerLock)
         { 
             player.stop();
         }
         
         // Clear the finished status.
         finished.set(false);
     }
     
     public void pause() throws BasicPlayerException
     {
         synchronized (playerLock)
         { 
             player.pause();
         }
     }
     
     public void resume() throws BasicPlayerException
     {
         synchronized (playerLock)
         { 
             player.resume();
         }
     }
        
     public void setNormalizedGain(double nGain) throws BasicPlayerException
     {
         // Make sure gain is between 0.0 and 1.0.
         nGain = Math.max( nGain, MIN_GAIN );
         nGain = Math.min( nGain, MAX_GAIN );
         
         // Invoke the super.
         synchronized (playerLock)
         { 
             player.setGain(nGain); 
         }
         
         // Set the normalized gain.
         this.normalizedGain.set(nGain);
     }              
     
     /**
      * Slowly fade the volume to the passed volume level.
      * 
      * @param targetGain The target gain to fade to, from 0.0 to 1.0.    
      */
     public void fadeToGain(final double nGain)
     {                
         Runnable r = new Runnable()
         {
             private double targetNormalizedGain = nGain;
             
             public void run()
             {
                 double n = normalizedGain.get();
                 double delta = FADE_DELTA;
 
                 if (NumUtil.equalsDouble(n, targetNormalizedGain, EPSILON))
                 {                                        
                     // Cancel this runnable.          
                     synchronized (fadeFutureLock)
                     {
                         if (fadeFuture != null)
                             fadeFuture.cancel(false);
                     }                   
                     
                     // End execution.
                     return;
                 }
                 
                 if (n > targetNormalizedGain)
                 {
                     delta *= -1;                                        
                 }          
                 
                 try
                 {                    
                     double g = n + delta;                                       
                     setNormalizedGain(g);
                 }
                 catch (BasicPlayerException e)
                 {
                     CouchLogger.get().recordException(this.getClass(), e, true /* Fatal */);
                 }
             }            
         };
                 
         synchronized (fadeFutureLock)
         {
             // Cancel existing fader.
             if (this.fadeFuture != null)
                 this.fadeFuture.cancel(false);
         
             // Start a new fader.
             this.fadeFuture = executor.scheduleWithFixedDelay(r, 0, FADE_PERIOD, TimeUnit.MILLISECONDS);
         } // end sync            
     }
 
     /**
      * Is the player looping?
      * 
      * @return
      */
     public boolean isLooping()
     {
         return loop.get();
     }
     
     /**
      * Set the loop status of the player.  True for looping, false for not.
      * 
      * @param loop
      */
     public void setLooping(boolean loop)
     {
         this.loop.set(loop);
     }   
     
     /**
      * Is the player at the end of the track?
      */
     public boolean isFinished()
     {
         return finished.get();
     }   
 
     /**
      * This method will call stop() once the music player
      * reaches the specified gain.  The order will expire
      * once the gain is reached, or if the track is finished
      * as decided by isFinished().
      *
      * @see stop
      * @see isFinished
      * @param nGain
      */
     public void stopAtGain(final double nGain)
     {
         Runnable r = new Runnable()
         {
             final private double targetGain = nGain;
             
             public void run()
             {                
                 if (NumUtil.equalsDouble(targetGain, normalizedGain.get(), EPSILON))
                 {                                        
                     synchronized (player)
                     {                        
                         try
                         {
                             player.stop();
                             
                             // Cancel this runnable.          
                             synchronized (stopFutureLock)
                             {
                                 if (stopFuture != null)
                                     stopFuture.cancel(false);
                             }  
                         }
                         catch (BasicPlayerException e)
                         {
                             CouchLogger.get().recordException(this.getClass(), e, true /* Fatal */);
                         }
                     } // end sync
                 }
                 else if (finished.get())
                 {                    
                     synchronized (stopFutureLock)
                     {
                         if (stopFuture != null)
                             stopFuture.cancel(false);
                     } // end sync
                 } // end if
             }
         };
         
         synchronized (stopFutureLock)
         {
             // Cancel existing stopper.
             if (this.stopFuture != null)
                 this.stopFuture.cancel(false);
         
             // Start a new stopper.
             this.stopFuture = executor.scheduleWithFixedDelay(r, 0, STOP_PERIOD, TimeUnit.MILLISECONDS);
         } // end sync  
     }
             
     /**
      * A private class that listens for certain events in order to implement
      * looping.
      */
     private class MusicPlayerListener implements BasicPlayerListener
     {
         public void opened(Object stream, Map properties)
         {
             // Intentionally left blank.
         }
 
         public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties)
         {
             // Intentionally left blank.
         }
 
         public void stateUpdated(BasicPlayerEvent event)
         {
             // Wait for EOM.  If looping is enabled, then loop.
             switch (event.getCode())
             {
                 case BasicPlayerEvent.EOM:
                     try
                     {           
                         synchronized (playerLock)
                         {
                             player.stop();
                             if (loop.get() == true)
                             {                     
                                 player.play();
                                 setNormalizedGain(normalizedGain.get());                            
                             }    
                             else
                             {
                                 finished.set(true);
                             }
                         } // end sync                                                                
                     }
                     catch (BasicPlayerException e)
                     {
                         CouchLogger.get().recordException(this.getClass(), e, true /* Fatal */);
                     }
                     break;
             } // end switch
         }
 
         public void setController(BasicController controller)
         {
             // Intentionally left blank.
         }
     }         
 
 }
