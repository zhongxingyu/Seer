 package vibe;
 
 import javax.swing.SwingUtilities;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 /**
  * ProgramTimer - A threaded timer for timing how long a program has been
  * running for.
  * @author Ryan Norris
  *
  */
 
 public class ProgramTimer { 
     private int mMsPerTick;
     private int mTicks = 0;
     private TimerListener mListener;
     private TimerThread mThread;
 
     public ProgramTimer(int msPerTick, TimerListener listener) {
         mMsPerTick = msPerTick;
         mListener = listener;
     }
 
     public void start() {
        if (mThread == null || !mThread.isRunning()) {
             mThread = new TimerThread(this, mMsPerTick);
             mThread.start();
         }
     }
 
     public void stop() {
         if (mThread != null) {
             mThread.stopRunning();
         }
     }
 
     public void reset() {
         stop();
         mTicks = 0;
     }
 
     private void tick() {
         mTicks++;
         mListener.onTick();
     }
 
     public int getTicks() {
         return mTicks;
     }
 
     public static interface TimerListener {
         public void onTick();
     }
 
     private static class TimerThread extends Thread {
         private int mMsPerTick;
         private ProgramTimer mTimer;
         private boolean mRunning = false;
         
         public TimerThread(ProgramTimer timer, int msPerTick) {
             mTimer = timer;
             mMsPerTick = msPerTick;
         }
 
         public void run() {
             mRunning = true;
             while (mRunning) {
                 try {
                     Thread.sleep(mMsPerTick);
                 }
                 catch (InterruptedException e) {
                     //Do nothing
                 }
                 mTimer.tick();
             }
         }
         
         public void stopRunning() {
             mRunning = false;
         }
 
         public boolean isRunning() {
             return mRunning;
         }
     }
 }
