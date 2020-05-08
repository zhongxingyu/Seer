 package com.tzwm.deadalarm;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
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
 
 import java.util.Calendar;
 
 /**
  * Created by tzwm on 10/5/13.
  */
 public class CountDownSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
     private CountDownActivity countDownActivity;
     private SurfaceHolder countDownholder;
     private MediaController mediaController;
     private Thread drawThread;
     private int xCanvas, yCanvas, rCenterCircle, rFringeCircle;
     private float xCurrent, yCurrent;
     private int ccColor, currentColor, arcColor, arcAngle, currentArcAngle, lastAngle;
     private boolean isMove, isRecording;
     private int secondRemain;
 
     public CountDownSurfaceView(Context context) {
         super(context);
         this.init(context);
     }
 
     public CountDownSurfaceView(Context context, AttributeSet attrs) {
         super(context, attrs);
         this.init(context);
     }
 
     public CountDownSurfaceView(Context context, AttributeSet attrs, int defStyle) {
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
                     countDownActivity.mCountDownTextView.stopTimer();
                     countDownActivity.mCountDownTextView.setBase(secondRemain);
                     break;
                 }
 
                 mediaController.startRecording();
                 isRecording = true;
                 ccColor = Color.RED;
 
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
                         secondRemain = 0;
                     }else{
                         secondRemain = 3600;
                     }
                     countDownActivity.mCountDownTextView.stopTimer();
                     countDownActivity.mCountDownTextView.setBase(secondRemain);
                     lastAngle = -1;
                     break;
                 }
                 lastAngle = arcAngle;
 
                 secondRemain = arcAngle * 10;
                 countDownActivity.mCountDownTextView.stopTimer();
                 countDownActivity.mCountDownTextView.setBase(secondRemain);
 
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
 
 
     private void sendAlarm() {
         Intent intent = new Intent("android.tzwm.hello");
         PendingIntent pi = PendingIntent.getBroadcast(countDownActivity,
                 1, intent,
                 PendingIntent.FLAG_ONE_SHOT);
         AlarmManager arm = (AlarmManager) countDownActivity.getSystemService(Context.ALARM_SERVICE);
         Calendar cal = Calendar.getInstance();
         cal.add(Calendar.SECOND, secondRemain);
         arm.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
     }
 
     private void centerTouchUp() {
         mediaController.stopRecording();
         isRecording = false;
         ccColor = Color.TRANSPARENT;
         countDownActivity.runOnUiThread(countDownActivity.mCountDownTextView);
         sendAlarm();
     }
 
     private void fringeTouchUp() {
         ccColor = Color.GREEN;
         try {
             drawThread.sleep(100);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         ccColor = Color.TRANSPARENT;
         countDownActivity.runOnUiThread(countDownActivity.mCountDownTextView);
         sendAlarm();
     }
 
     private void init(Context context) {
         countDownActivity = (CountDownActivity) context;
         mediaController = new MediaController();
 
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
 
             if (arcAngle != currentArcAngle) {
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
