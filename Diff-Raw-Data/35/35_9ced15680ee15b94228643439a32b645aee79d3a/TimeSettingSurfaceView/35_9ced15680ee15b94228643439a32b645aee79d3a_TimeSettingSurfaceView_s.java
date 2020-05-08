 package com.tzwm.deadalarm;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.PixelFormat;
 import android.graphics.PorterDuff;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 
 /**
  * Created by tzwm on 10/5/13.
  */
 public class TimeSettingSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
     private TimeSettingActivity timeSettingActivity;
     private SurfaceHolder countDownholder;
     private MediaController mediaController;
     private Thread drawThread;
     private int xCanvas, yCanvas, rCenterCircle, rFringeCircle;
     private float xCurrent, yCurrent;
     private int ccColor, currentColor, arcColor, arcAngle, currentArcAngle, lastAngle;
     private boolean isMove, isRecording, isHour;
     private int hourOfDay, minute;
 
     public TimeSettingSurfaceView(Context context) {
         super(context);
         this.init(context);
     }
 
     public TimeSettingSurfaceView(Context context, AttributeSet attrs) {
         super(context, attrs);
         this.init(context);
     }
 
     public TimeSettingSurfaceView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         this.init(context);
     }
 
     @Override
     public void surfaceCreated(SurfaceHolder holder) {
         xCanvas = getWidth();
         yCanvas = getHeight();
         rCenterCircle = getWidth() / 5;
         rFringeCircle = (int) (getWidth() / 2.5);
 
         drawThread = new Thread(this);
         drawThread.start();
     }
 
     @Override
     public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
     }
 
     @Override
     public void surfaceDestroyed(SurfaceHolder holder) {
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         float x = event.getX();
         float y = event.getY();
 
         xCurrent = x;
         yCurrent = y;
 
         switch (event.getActionMasked()) {
             case MotionEvent.ACTION_DOWN:
                 if (!(Math.abs(x - xCanvas / 2) < rCenterCircle && Math.abs(y - yCanvas / 2) < rCenterCircle)) {
                     break;
                 }
 
                 mediaController.startRecording();
                 isRecording = true;
                 ccColor = Color.RED;
                 timeSettingActivity.mTimeTextView.setVisibility(View.INVISIBLE);
 
                 break;
 
             case MotionEvent.ACTION_MOVE:
                 if(lastAngle == -1)
                     break;
 
                 if ((Math.abs(x - xCanvas / 2) < rCenterCircle && Math.abs(y - yCanvas / 2) < rCenterCircle)) {
                     break;
                 }
 
                 if(isRecording){
                     centerTouchUp();
                     break;
                 }
 
                 arcAngle = 360 - pointToAngle(x, y);
                 if((arcAngle>=1&&arcAngle<=3) || (arcAngle<=359&&arcAngle>=357)){
                     if(lastAngle > arcAngle){
                         updateTime(0);
                     }else{
                         updateTime(360);
                     }
                     lastAngle = -1;
                     break;
                 }
                 lastAngle = arcAngle;
 
                 updateTime(arcAngle);
 
                 break;
 
             case MotionEvent.ACTION_UP:
                 lastAngle = 0;
                 if (isRecording) {
                     centerTouchUp();
                     break;
                 }
 
                 if (event.getEventTime() - event.getDownTime() <= 200)
                     fringeTouchUp();
 
                 break;
 
             case MotionEvent.ACTION_CANCEL:
                 if (isRecording) {
                     centerTouchUp();
                     break;
                 }
 
                 break;
         }
 
         return true;
     }
 
     private void centerTouchUp() {
         mediaController.stopRecording();
         isRecording = false;
         ccColor = Color.TRANSPARENT;
         timeSettingActivity.mTimeTextView.setVisibility(View.VISIBLE);
     }
 
     private void fringeTouchUp() {
         arcColor = Color.GREEN;
         try {
             drawThread.sleep(100);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         arcColor = Color.WHITE;
         isHour = !isHour;
     }
 
     private void updateTime(int angle) {
         if(isHour)
             hourOfDay = (int)((float)angle/15.65);
         else
             minute = (int)((float)angle/6.1);
         timeSettingActivity.mTimeTextView.setTime(hourOfDay, minute);
     }
 
     private void init(Context context) {
         timeSettingActivity = (TimeSettingActivity) context;
         mediaController = new MediaController(context);
 
         countDownholder = this.getHolder();
         countDownholder.addCallback(this);
         countDownholder.setFormat(PixelFormat.TRANSPARENT);
         setZOrderOnTop(true);
         setFocusableInTouchMode(true);
 
         ccColor = Color.TRANSPARENT;
         currentColor = 0;
         arcColor = Color.WHITE;
         arcAngle = 3;
         lastAngle = 0;
         currentArcAngle = -1;
         xCurrent = 1000;
         yCurrent = 0;
 
         isRecording = false;
         isMove = false;
         isHour = true;
 
         hourOfDay = 0;
         minute = 0;
     }
 
     @Override
     public void run() {
        while (true) {
             if (ccColor != currentColor) {
                 Canvas canvas = countDownholder.lockCanvas(new Rect(xCanvas / 2 - rCenterCircle - 1,
                         yCanvas / 2 - rCenterCircle - 1,
                         xCanvas / 2 + rCenterCircle + 1,
                         yCanvas / 2 + rCenterCircle + 1));
                 Paint mPaint = new Paint();
                 mPaint.setAntiAlias(true);
                 if (ccColor == Color.TRANSPARENT)
                     canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                 else {
                     mPaint.setColor(ccColor);
                     canvas.drawCircle(xCanvas / 2, yCanvas / 2, rCenterCircle, mPaint);
                 }
                 countDownholder.unlockCanvasAndPost(canvas);
 
                 currentColor = ccColor;
             }
 
             if (arcAngle != currentArcAngle  || arcColor != Color.WHITE) {
                 Canvas canvas = countDownholder.lockCanvas(new Rect(xCanvas / 2 - rFringeCircle - 15,
                         yCanvas / 2 - rFringeCircle - 15,
                         xCanvas / 2 + rFringeCircle + 15,
                         yCanvas / 2 + rFringeCircle + 15));
                 Paint mPaint = new Paint();
                 mPaint.setAntiAlias(true);
                 mPaint.setStyle(Paint.Style.STROKE);
                 mPaint.setColor(arcColor);
                 mPaint.setStrokeWidth(2);
                 canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                 canvas.drawArc(new RectF(xCanvas / 2 - rFringeCircle, yCanvas / 2 - rFringeCircle,
                         xCanvas / 2 + rFringeCircle, yCanvas / 2 + rFringeCircle),
                         0 + 270, arcAngle-3, false, mPaint);
                 float xTmp = xCurrent - xCanvas / 2;
                 float yTmp = yCurrent - yCanvas / 2;
                 yTmp = -yTmp;
                 float tmp = (float) Math.sqrt(xTmp * xTmp + yTmp * yTmp);
                 tmp = rFringeCircle / tmp;
                 mPaint.setColor(Color.RED);
                 canvas.drawCircle((float)Math.sin((float)(arcAngle)/180*Math.PI)
                         * rFringeCircle + xCanvas / 2,
                         -(float)Math.cos((float)(arcAngle)/180*Math.PI)
                                 * rFringeCircle + yCanvas / 2,
                         10, mPaint);
                 countDownholder.unlockCanvasAndPost(canvas);
 
                 currentArcAngle = arcAngle;
             }
 
         }
     }
 
     private int pointToAngle(float x, float y) {
         x = x - xCanvas / 2;
         y = y - yCanvas / 2;
         y = -y;
 
         if (x == 0 && y == 0)
             return 0;
 
         if (y == 0)
             if (x > 0)
                 return 0;
             else
                 return 180;
         if (x == 0)
             if (y > 0)
                 return 270;
             else
                 return 90;
 
         if (x > 0 && y > 0)
             return (int) (180 * (1.5 + Math.atan(y / x) / Math.PI));
         if (x < 0 && y > 0)
             return (int) (180 * (0.5 + Math.atan(y / x) / Math.PI));
         if (x < 0 && y < 0)
             return (int) (180 * (0.5 + Math.atan(y / x) / Math.PI));
         if (x > 0 && y < 0)
             return (int) (180 * (1.5 + Math.atan(y / x) / Math.PI));
 
         return 0;
     }
 
 }
