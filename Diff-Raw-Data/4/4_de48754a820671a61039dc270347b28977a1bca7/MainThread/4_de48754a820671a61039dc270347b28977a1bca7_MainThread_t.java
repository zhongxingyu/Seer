 /* -*- mode:java; coding:utf-8; -*- Time-stamp: <MainThread.java - root> */
 
 package rabbitmish.coloringchickens;
 
 import android.graphics.Canvas;
 import android.view.SurfaceHolder;
 
 public class MainThread extends Thread
 {
     private final static int FRAME_DURATION = 1000 / 25 /* frames per second */;
 
     private boolean _running;
     private SurfaceHolder _surface_holder;
     private MainView _view;
 
     private void draw()
     {
         Canvas canvas = null;
 
         try
         {
             canvas = _surface_holder.lockCanvas();
 
             synchronized (_surface_holder)
             {
                 _view.render(canvas);
             }
         }
         finally
         {
             if (canvas != null)
             {
                 _surface_holder.unlockCanvasAndPost(canvas);
             }
         }
     }
 
     public MainThread(SurfaceHolder h, MainView v)
     {
         super();
 
         _surface_holder = h;
         _view = v;
     }
 
     @Override
     public void run()
     {
         final long start_time = System.currentTimeMillis();
 
         long skip_interval = 0;
 
         while (_running)
         {
            final long frame_time = (start_time
                                     + (System.currentTimeMillis() - start_time) / FRAME_DURATION * FRAME_DURATION
                                     + skip_interval);
 
             _view.update();
 
             if (System.currentTimeMillis() > frame_time)
             {
                 draw();
 
                 final long delta = frame_time + FRAME_DURATION - System.currentTimeMillis();
 
                 if (delta < 0)
                 {
                     skip_interval = FRAME_DURATION;
                 }
                 else
                 {
                     try
                     {
                         Thread.sleep(delta);
                     }
                     catch (InterruptedException e)
                     {
                         // ignore
                     }
                     skip_interval = 0;
                 }
             }
         }
     }
 
     public void setRunning(boolean running)
     {
         _running = running;
     }
 }
