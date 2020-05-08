 package com.tzwm.deadalarm;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.PixelFormat;
 import android.graphics.PorterDuff;
 import android.graphics.Rect;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 import java.util.Random;
 
 /**
  * Created by tzwm on 10/8/13.
  */
 public class BeeSurfaceView extends SurfaceView implements SurfaceHolder.Callback,Runnable {
     private Context mContext;
     private SurfaceHolder mHolder;
     private Thread mThread;
 
     private int xCanvas, yCanvas;
     private int xCurrent, yCurrent;
     private int step, deviation;
     private int rCircle;
     private int cColor;
     private Random random;
 
     static private int[] d = {-1, 0, 1};
 
     public BeeSurfaceView(Context context) {
         super(context);
         init(context);
     }
 
     public BeeSurfaceView(Context context, AttributeSet attrs) {
         super(context, attrs);
         init(context);
     }
 
     public BeeSurfaceView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         init(context);
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         float x = event.getX();
         float y = event.getY();
 
         switch (event.getActionMasked()) {
             case MotionEvent.ACTION_DOWN:
                 if(Math.abs(x-xCurrent)<deviation && Math.abs(y-yCurrent)<deviation)
                     quit();
                 break;
 
             default:
                 break;
         }
         return true;
     }
 
     @Override
     public void run() {
        while(true){
             Canvas canvas = mHolder.lockCanvas(new Rect(xCurrent-4*rCircle, yCurrent-4*rCircle,
                                                         xCurrent+4*rCircle, yCurrent+4*rCircle));
             canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
 
             Paint mPaint = new Paint();
             mPaint.setAntiAlias(true);
             mPaint.setColor(cColor);
             mPaint.setStyle(Paint.Style.STROKE);
             int xTmp = xCurrent + step*(random.nextInt(3) - 1);
             while(xTmp<rCircle || xTmp>xCanvas-rCircle) {
                 xTmp = xCurrent + step*(random.nextInt(3) - 1);
             }
             int yTmp = xCurrent + step*(random.nextInt(3) - 1);
             while(yTmp<rCircle || yTmp>xCanvas-rCircle) {
                 yTmp = xCurrent + step*(random.nextInt(3) - 1);
             }
             canvas.drawCircle(xTmp, yTmp, rCircle, mPaint);
             xCurrent = xTmp;
             yCurrent = yTmp;
 
             mHolder.unlockCanvasAndPost(canvas);
         }
     }
 
     @Override
     public void surfaceCreated(SurfaceHolder holder) {
         xCanvas = getWidth();
         yCanvas = getHeight();
 
         xCurrent = random.nextInt(1<<30) % xCanvas;
         while(xCurrent<rCircle || xCurrent>xCanvas-rCircle)
             xCurrent = random.nextInt(1<<30) % xCanvas;
         yCurrent = random.nextInt(1<<30) % yCanvas;
         while(yCurrent<rCircle || yCurrent>xCanvas-rCircle)
             yCurrent = random.nextInt(1<<30) % yCanvas;
         cColor = Color.WHITE;
 
         mThread = new Thread(this);
         mThread.start();
     }
 
     @Override
     public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 
     }
 
     @Override
     public void surfaceDestroyed(SurfaceHolder holder) {

     }
 
     private void quit() {
         ((BeeActivity)mContext).finish();
     }
 
     private void init(Context _context) {
         mContext = _context;
         mHolder = getHolder();
         mHolder.addCallback(this);
         setZOrderOnTop(true);
         setFocusableInTouchMode(true);
 
         rCircle = 30;
         random = new Random();
         step = 10;
         deviation = 10;
     }
 
 }
