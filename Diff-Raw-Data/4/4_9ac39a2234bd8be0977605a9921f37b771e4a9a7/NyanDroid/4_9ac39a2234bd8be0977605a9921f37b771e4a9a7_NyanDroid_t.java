 package com.powerje.nyan.sprites;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import com.powerje.nyan.NyanUtils;
 import com.powerje.nyan.R;
 
 import java.util.ArrayList;
 
 /**
  * NyanDroid is a class for drawing an animated nyan droid.
  * 
  * @author powerj
  * 
  */
 public class NyanDroid {
 	/** Context NyanDroid is being drawn in. */
 	final Context mContext;
 	/** NyanDroid frames. */
 	final ArrayList<Bitmap> mFrames;
 
 	/** Current y offset. */
 	private int yOffset;
 	/** True iff NyanDroid is moving upwards */
 	boolean mMovingUp;
 	/** Center x coordinate. */
 	private int mCenterX;
 	/** Center y coordinate. */
 	private int mCenterY;
 
 	/** Paint with which to draw. */
 	private Paint mPaint;
 	/** Current frame NyanDroid is in. */
 	private int currentFrame;
 
 	private String mDroid;
 
     private boolean isBlank = false;
 
 	/**
 	 * Construct NyanDroid.
 	 * 
 	 * @param c
 	 *            context to be drawn in.
 	 * @param maxDim
 	 *            maximize size of a side.
 	 * @param paint
 	 *            used to draw.
 	 */
 	public NyanDroid(Context c, int maxDim, Paint paint, String image) {
 
 		mContext = c;
 		mPaint = paint;
 		mDroid = image;
 		Bitmap repeatingFrame;
 
 		mFrames = new ArrayList<Bitmap>();
 
 		if (image.equals("droidtv")) {
 			repeatingFrame = NyanUtils.imageResourceToBitmap(c,
 					R.drawable.superman_gtv0, maxDim);
 			mFrames.add(repeatingFrame);
 			mFrames.add(repeatingFrame);
 			mFrames.add(repeatingFrame);
 			repeatingFrame = NyanUtils.imageResourceToBitmap(c,
 					R.drawable.superman_gtv1, maxDim);
 			mFrames.add(repeatingFrame);
 			mFrames.add(repeatingFrame);
 			mFrames.add(repeatingFrame);
 		} else if (image.equals("ics_egg")) {
 			// hack because image sizes are different
 			maxDim += 20;
 			mFrames.add(NyanUtils.imageResourceToBitmap(c,
 					R.drawable.nyandroid00, maxDim));
 			mFrames.add(NyanUtils.imageResourceToBitmap(c,
 					R.drawable.nyandroid01, maxDim));
 			mFrames.add(NyanUtils.imageResourceToBitmap(c,
 					R.drawable.nyandroid02, maxDim));
 			mFrames.add(NyanUtils.imageResourceToBitmap(c,
 					R.drawable.nyandroid03, maxDim));
 			mFrames.add(NyanUtils.imageResourceToBitmap(c,
 					R.drawable.nyandroid04, maxDim));
 			mFrames.add(NyanUtils.imageResourceToBitmap(c,
 					R.drawable.nyandroid05, maxDim));
 			mFrames.add(NyanUtils.imageResourceToBitmap(c,
 					R.drawable.nyandroid06, maxDim));
 			mFrames.add(NyanUtils.imageResourceToBitmap(c,
 					R.drawable.nyandroid07, maxDim));
 			mFrames.add(NyanUtils.imageResourceToBitmap(c,
 					R.drawable.nyandroid08, maxDim));
 			mFrames.add(NyanUtils.imageResourceToBitmap(c,
 					R.drawable.nyandroid09, maxDim));
 			mFrames.add(NyanUtils.imageResourceToBitmap(c,
 					R.drawable.nyandroid10, maxDim));
 			mFrames.add(NyanUtils.imageResourceToBitmap(c,
 					R.drawable.nyandroid11, maxDim));
 		} else if (image.equals("tardis")) {
 			mFrames.add(NyanUtils.imageResourceToBitmap(c, R.drawable.tardis,
 					maxDim));
         } else if (image.equals("grump")) {
             mFrames.add(NyanUtils.imageResourceToBitmap(c, R.drawable.grump_frame_0, maxDim));
             mFrames.add(NyanUtils.imageResourceToBitmap(c, R.drawable.grump_frame_1, maxDim));
             mFrames.add(NyanUtils.imageResourceToBitmap(c, R.drawable.grump_frame_2, maxDim));
             mFrames.add(NyanUtils.imageResourceToBitmap(c, R.drawable.grump_frame_3, maxDim));
             mFrames.add(NyanUtils.imageResourceToBitmap(c, R.drawable.grump_frame_4, maxDim));
             mFrames.add(NyanUtils.imageResourceToBitmap(c, R.drawable.grump_frame_5, maxDim));
 		} else if (image.equals("nyanwich")) {
 			mFrames.add(NyanUtils.imageResourceToBitmap(c, R.drawable.frame0,
 					maxDim));
 			mFrames.add(NyanUtils.imageResourceToBitmap(c, R.drawable.frame1,
 					maxDim));
 			mFrames.add(NyanUtils.imageResourceToBitmap(c, R.drawable.frame2,
 					maxDim));
 			mFrames.add(NyanUtils.imageResourceToBitmap(c, R.drawable.frame3,
 					maxDim));
 			mFrames.add(NyanUtils.imageResourceToBitmap(c,
 					R.drawable.superman0, maxDim));
 
 			repeatingFrame = NyanUtils.imageResourceToBitmap(c,
 					R.drawable.superman1, maxDim);
 			mFrames.add(repeatingFrame);
 			mFrames.add(repeatingFrame);
 			mFrames.add(repeatingFrame);
 
 			mFrames.add(NyanUtils.imageResourceToBitmap(c, R.drawable.frame4,
 					maxDim));
 			mFrames.add(NyanUtils.imageResourceToBitmap(c, R.drawable.frame5,
 					maxDim));
 			mFrames.add(NyanUtils.imageResourceToBitmap(c, R.drawable.frame6,
 					maxDim));
 			mFrames.add(NyanUtils.imageResourceToBitmap(c, R.drawable.frame7,
 					maxDim));
 		} else {
             // None
             isBlank = true;
         }
 	}
 
 	/**
 	 * Draw NyanDroid.
 	 * 
 	 * @param c
 	 *            canvas to draw on.
 	 * @param animate
 	 *            move to next frame after drawing.
 	 */
 	public void draw(Canvas c, boolean animate) {
         // Okay I'm lazy and implemented this year, whatever sue me, yes this is terrible
         if (isBlank) {
             return;
         }
 
 		Bitmap toDraw = mFrames.get(currentFrame);
 		c.drawBitmap(toDraw, mCenterX - (toDraw.getWidth() / 2),
 				(mCenterY - toDraw.getHeight() / 2) + yOffset, mPaint);
 		if (animate) {
 			currentFrame = (currentFrame == mFrames.size() - 1) ? 0
 					: (currentFrame + 1);
 
 			if (!mDroid.equals("ics_egg")) {
 				if (mMovingUp) {
 					yOffset += 3;
 					if (yOffset > 2)
 						mMovingUp = false;
 				} else {
 					yOffset -= 3;
 					if (yOffset < -2)
 						mMovingUp = true;
 				}
 			}
 		}
 	}
 
 	/**
 	 * @return the height of an individual frame.
 	 */
 	public int getFrameHeight() {
        // Oh god just redo this stuff, this is terrible
        if (isBlank) return 256;
 		return mFrames.get(0).getHeight();
 	}
 
 	/**
 	 * @return the width of an individual frame.
 	 */
 	public int getFrameWidth() {
        if (isBlank) return 256;
 		return mFrames.get(0).getWidth();
 	}
 
 	/**
 	 * Set the coordinates with which to center the drawing.
 	 * 
 	 * @param x
 	 *            center x coordinate
 	 * @param y
 	 *            center y coordinate
 	 */
 	public void setCenter(int x, int y) {
 		mCenterX = x;
 		mCenterY = y;
 	}
 }
