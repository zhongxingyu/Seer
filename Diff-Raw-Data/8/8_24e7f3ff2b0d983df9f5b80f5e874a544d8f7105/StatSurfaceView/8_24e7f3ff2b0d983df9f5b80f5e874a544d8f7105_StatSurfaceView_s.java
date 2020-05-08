 package org.ktln2.android.callstat;
 
 import android.view.View;
 import android.content.Context;
 import android.util.AttributeSet;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.RectF;
 import android.graphics.Rect;
 
 
 /*
  * This class aim is to draw stuff.
  */
 class StatSurfaceView extends View {
     private String TAG = "StatSurfaceView";
     private float mWidth;
     private float mHeight;
     private float mRadius;
     private StatisticsMap mStat;
 
     /*
      * http://developer.android.com/guide/topics/graphics/2d-graphics.html#on-surfaceview
      *
      * When your SurfaceView is initialized, get the SurfaceHolder by calling getHolder().
      * You should then notify the SurfaceHolder that you'd like to receive SurfaceHolder
      * callbacks (from SurfaceHolder.Callback) by calling addCallback() (pass it this).
      */
     private void init() {
     }
 
     public StatSurfaceView(Context context) {
         super(context);
         init();
     }
 
     public StatSurfaceView(Context context, AttributeSet attrs) {
         super(context, attrs);
         init();
     }
 
     public StatSurfaceView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         init();
     }
 
     @Override
     protected void onDraw(Canvas canvas) {
         if (mStat == null) {
             return;
         }
 
        android.util.Log.d("StatSurfaceView", "ionDraw()");

         // Here we are using the height of the view
         // and not of the canvas: otherwise se obtain
         // a dirty flickering
        canvas.translate(0, getHeight()/2);
         canvas.scale(1.0F, -1.0F);
 
         drawHistogram(canvas);
 
         canvas.scale(1.0F, -1.0F);
        canvas.translate(0, canvas.getHeight()/2);
     }
 
     private void drawHistogram(Canvas canvas) {
         int[] bins = mStat.getBinsForDurations();
         Paint paint = new Paint();
         paint.setARGB(255, 0, 255, 0);
 
         int step_x = 10;
         int step_y = 10;
 
         Rect rect = new Rect();
         rect.right = step_x;
         for (int value : bins) {
             rect.offset(step_x, 0);
             rect.bottom = step_y*value;
             canvas.drawRect(
                 rect, paint
             );
         }
     }
 
 
     public void drawPie(Canvas canvas, float[] values) {
         Paint[] p = new Paint[]{
             new Paint(),
             new Paint()
         };
         p[0].setARGB(255, 255, 0, 0);
         p[1].setARGB(255, 0, 255, 0);
 
         float total = 0;
         for (int cycle = 0 ; cycle < values.length ; cycle++) {
             total += values[cycle];
         }
 
         float start_angle = 0;
         float end_angle = 0;
         for (int cycle = 0 ; cycle < values.length ; cycle++) {
             end_angle = (values[cycle]/total)*360;
             canvas.drawArc(
                 new RectF(0, 0, mRadius, mRadius),
                 start_angle,
                 end_angle,
                 true,
                 p[cycle]
             );
 
             start_angle += end_angle;
         }
     }
 
     public void update(StatisticsMap map) {
         mStat = map;
         postInvalidate();
     }
 }
