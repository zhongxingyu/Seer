 package com.RoboMobo;
 
 import android.content.Context;
import android.os.Handler;
 import android.util.AttributeSet;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Роман
  * Date: 30.07.13
  * Time: 13:04
  * To change this template use File | Settings | File Templates.
  */
 public class MainSurfaceView extends SurfaceView implements SurfaceHolder.Callback
 {
     public ThreadUpdate threadDraw;
 
     public MainSurfaceView(Context context)
     {
         super(context);
         getHolder().addCallback(this);
     }
 
     public MainSurfaceView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         getHolder().addCallback(this);
     }
 
     public MainSurfaceView(Context context, AttributeSet attrs) {
         super(context, attrs);
         getHolder().addCallback(this);
     }
 
     @Override
     public void surfaceCreated(SurfaceHolder surfaceHolder)
     {
         threadDraw = new ThreadUpdate(getHolder());
        new Handler().post(threadDraw);
     }
 
     @Override
     public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3)
     {
 
     }
 
     @Override
     public void surfaceDestroyed(SurfaceHolder surfaceHolder)
     {
         boolean retry = true;
         threadDraw.isRunning = false;
         while (retry)
         {
             try
             {
                 threadDraw.join();
                 retry = false;
             }
             catch (InterruptedException e)
             {
 
             }
         }
     }
 }
