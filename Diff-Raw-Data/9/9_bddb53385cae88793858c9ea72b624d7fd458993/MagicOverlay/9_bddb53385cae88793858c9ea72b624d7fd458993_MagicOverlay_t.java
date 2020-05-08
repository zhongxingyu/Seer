 package com.adaburrows.superpowers;
 
 import java.io.IOException;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Path;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.os.Bundle;
 import android.view.View;
 
 class MagicOverlay extends View {
 
   private static final String TAG = "MagicOverlay";
 
   // Member vars
   Bitmap mBitmap;
   Paint mPaintBlack;
   Paint mPaintYellow;
   Paint mPaintRed;
   Paint mPaintGreen;
   Paint mPaintBlue;
   int mImageWidth, mImageHeight;
   int mSamplingRate;
   int mCaptureSize;
   private byte[] mBytes;
   private int[] mPoints;
   private int[] mCutoffs;
   private Rect mRect = new Rect();
   private Paint mForePaint = new Paint();
 
   // Constructor
   public MagicOverlay(Context context) {
     super(context);
 
     mBytes = null;
 
     mForePaint.setStrokeWidth(1f);
     mForePaint.setAntiAlias(true);
     mForePaint.setColor(Color.rgb(0, 128, 255));
 
     mPaintBlack = new Paint();
     mPaintBlack.setStyle(Paint.Style.FILL);
     mPaintBlack.setColor(Color.BLACK);
     mPaintBlack.setAlpha(128);
     mPaintBlack.setTextSize(25);
         
     mPaintYellow = new Paint();
     mPaintYellow.setStyle(Paint.Style.FILL);
     mPaintYellow.setColor(Color.YELLOW);
     mPaintYellow.setAlpha(128);
     mPaintYellow.setTextSize(25);
         
     mPaintRed = new Paint();
     mPaintRed.setStyle(Paint.Style.FILL);
     mPaintRed.setColor(Color.RED);
     mPaintRed.setAlpha(128);
     mPaintRed.setTextSize(25);
         
     mPaintGreen = new Paint();
     mPaintGreen.setStyle(Paint.Style.FILL);
     mPaintGreen.setColor(Color.GREEN);
     mPaintGreen.setAlpha(128);
     mPaintGreen.setTextSize(25);
         
     mPaintBlue = new Paint();
     mPaintBlue.setStyle(Paint.Style.FILL);
     mPaintBlue.setColor(Color.BLUE);
     mPaintBlue.setAlpha(128);
     mPaintBlue.setTextSize(25);
         
     mBitmap = null;
   }
 
   public void updateData(byte[] data, int samplingRate, int captureSize) {
     mBytes = data;
     mSamplingRate = samplingRate;
     if(mCaptureSize != captureSize) {
       mCaptureSize = captureSize;
 
       // Divide input vector into 100 logarithmically equal segments
       // based on cutoff skew
       if(mCutoffs == null) {
         mCutoffs = new int[101];
       }
      int cutoff_skew = 6000;
       for(int i = 0; i < 101; i++) {
         mCutoffs[i] = (int)(
                         (mCaptureSize / 2) * 
                         (
                           Math.pow(
                             cutoff_skew,
                             ((float)i / 100)
                           ) - 1
                         ) / 
                         (cutoff_skew-1)
                       );
       }
     }
     invalidate();
   }
 
   @Override
   protected void onDraw(Canvas canvas) {
     int canvasWidth = canvas.getWidth();
     int canvasHeight = canvas.getHeight();
     int newImageWidth = canvasWidth;
     int newImageHeight = canvasHeight;
     int marginWidth = (canvasWidth - newImageWidth)/2;
 
     // Draw if we have a bitmap to draw on
     if (mBitmap != null) { }
 
     // Draw a string
     String imageMeanStr = "Superpowers";
     canvas.drawText(imageMeanStr, marginWidth+10-1, 30-1, mPaintRed);
     canvas.drawText(imageMeanStr, marginWidth+10+1, 30-1, mPaintGreen);
     canvas.drawText(imageMeanStr, marginWidth+10+1, 30+1, mPaintBlue);
     canvas.drawText(imageMeanStr, marginWidth+10-1, 30+1, mPaintBlack);
     canvas.drawText(imageMeanStr, marginWidth+10, 30, mPaintYellow);
 
     // Waveform or FFT is in mBytes (byte array), this function gets called every 
     // time there's new data.
     if (mBytes == null) {
         return;
     }
 
     // mPoints holds the average real components of the 100 frequency bands 
     // we're going to plot with circles.
     if (mPoints == null) {
         mPoints = new int[100];
     }
 
     // Find the largest real formant component for each frequency band
     for(int i = 0; i < 100; i++) {
       mPoints[i] = 0;
       for(int j = mCutoffs[i]; j < mCutoffs[i+1]; j++){
         if(mBytes[j*2] > mPoints[i]){
           mPoints[i] = mBytes[j*2];
         }
       }
     }
 
     // Amplify the composite formant vector
     for(int i = 0; i < 100; i++) {
       mPoints[i] = mPoints[i] * 10;
       if(mPoints[i] > 255){
         mPoints[i] = 255;
       }
     }
 
     // mRect is the bounding rectangle that contains the plot. Here, it's the whole screen.
     mRect.set(0, 0, getWidth(), getHeight());
 
     for (int i = 0; i < (mPoints.length) - 1; i++) {
 
       // Alpha: Scale 0..255
       // Conveniently, the real component of this fourier formant is already a byte.
       int alpha = mPoints[i];
 
       // Hue: Scale 0..360
       float hue = 420f - ( 
                     (float)Math.pow(
                       ((float)i / (mPoints.length)), 
                       2.65
                     ) * 360f
                   ); 
       if (hue > 360f) { hue = hue - 360f; }
 
       // Saturation: Scale 0..1
       // TODO This actually corresponds to timbre ("roughnss") between neighboring frequencies.
       // If anyone knows how to calculate this while preserving a reasonable frame rate, I'd love 
       // to hear about it. For now, I've just got this set at a decent "it looks alright" value.
       float saturation = 0.8f;
       
       // Value: Scale 0..1
       // Varies directly with hue and frequency.
       float value = mPoints[i]/255f;
 
       Paint color = new Paint();
       color.setColor(Color.HSVToColor(128,new float[]{hue,saturation,value}));
       color.setStyle(Style.FILL);
 
       // Centerpoint coordinates cx and cy
       // These start in the lower-left-hand corner, then sweep right and up the right-hand side of
       // the screen before tapering across the top, ending up about 2/3 the way back left.
       float cx =  (float)Math.sin(
                     ((float)i / (mPoints.length)) * 2.8f
                   ) * (
                     (float)mRect.width() * 0.95f
                   );
       float cy =  ((float)mRect.height() * 0.1f) + 
                   (
                     (
                       (float)Math.cos(
                         (
                           (float)i / (mPoints.length)
                         ) * 4.1f - 0.45f
                       ) + 1
                     ) / 2 
                   ) * (
                     (float)mRect.height() * 0.9f
                   );
 
       // Radius
       // Varies directly with hue, frequency, and value
       float radius =  (1 - 
                         (
                           (float)i / 
                           ((mPoints.length) * 1.1f)
                         )
                       ) * (
                         (mPoints[i] / 255f) * 
                         mRect.height() / 3
                       );
 
       // Draw this circle.
       canvas.drawCircle(cx, cy, radius, color);
     }
 
     // Draw our parent
     super.onDraw(canvas);
   }
 
 }
