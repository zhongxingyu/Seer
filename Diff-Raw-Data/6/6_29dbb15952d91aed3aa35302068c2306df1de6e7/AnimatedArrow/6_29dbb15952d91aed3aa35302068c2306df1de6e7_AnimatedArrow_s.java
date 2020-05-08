 package org.irmacard.androidverifier;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Movie;
 import android.util.AttributeSet;
 import android.view.View;
 
 /**
  * A view that shows our animated arrow. Please note that this needs android:hardwareAccelerated="false"
  * to be set for the Activity, otherwise the gif movie won't show.
  * @author Maarten Everts, TNO.
  *
  */
 public class AnimatedArrow extends View {
 
     private Movie mMovie;
     private long mMovieStart;
     private Bitmap mBitmap;
     private boolean showAnimation = false;
 
 	public AnimatedArrow(Context context, AttributeSet attrs) {
 		super(context, attrs);
         setFocusable(true);
         
         java.io.InputStream is;
 
        is = context.getResources().openRawResource(R.drawable.irma_arrow_080px);
         mBitmap = BitmapFactory.decodeStream(is);
         
        is = context.getResources().openRawResource(R.drawable.arrows_blue_animated_080px);

         mMovie = Movie.decodeStream(is);
 	}
 
 	public void startAnimation() {
 		showAnimation = true;
 		invalidate();
 	}
 	
 	public void stopAnimation() {
 		showAnimation = false;
 	}
 	
     @Override
     protected void onDraw(Canvas canvas) {
     	canvas.drawColor(0xFF004289);
     	
     	if (showAnimation) {
 	        long now = android.os.SystemClock.uptimeMillis();
 	        if (mMovieStart == 0) {   // first time
 	            mMovieStart = now;
 	        }
 	        if (mMovie != null) {
 	            int dur = mMovie.duration();
 	            if (dur == 0) {
 	                dur = 1000;
 	            }
 	            int relTime = (int)((now - mMovieStart) % dur);
 	            mMovie.setTime(relTime);
 	            mMovie.draw(canvas, 0, 0);
 	            invalidate();
 	        }
     	} else {
     		canvas.drawBitmap(mBitmap, 0, 0, null);
     	}
     }
 }
