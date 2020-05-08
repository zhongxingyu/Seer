 /*
  * ComicsReader is an Android application to read comics
  * Copyright (C) 2011-2013 Cedric OCHS
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package net.kervala.comicsreader;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.util.AttributeSet;
 import android.view.View;
 
 public class FullImageView extends View {
 	private Bitmap mCurrentBitmap = null;
 	private Bitmap mNextBitmap = null;
 	private Bitmap mPreviousBitmap = null;
 	private int mBitmapWidth;
 	private int mBitmapHeight;
 	private int mOffset;
 	final private Rect mRect = new Rect();
 	final private Rect mRectSrc = new Rect();
 	final private Rect mRectDst = new Rect();
 	final private Paint mWhitePainter = new Paint(); 
 	
 	public FullImageView(Context context) {
 		super(context);
 
 		initFullImageView();
 	}
 
 	public FullImageView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 
 		initFullImageView();
 	}
 
 	public FullImageView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 
 		initFullImageView();
 	}
 	
 	public void initFullImageView() {
 		setBackgroundColor(0x00000000);
 		setDrawingCacheEnabled(false);
 		setSaveEnabled(false);
 		setWillNotCacheDrawing(true);
 		
 		mWhitePainter.setARGB(255, 255, 255, 255);
 	}
 
 	public void reset() {
 		recyclePreviousBitmap();
 		recycleNextBitmap();
 		recycleCurrentBitmap();
 	}
 	
 	public int getBitmapWidth() {
 		return mBitmapWidth;
 	}
 
 	public int getBitmapHeight() {
 		return mBitmapHeight;
 	}
 
 	/**
 	 * Return the view's current bitmap, or null if no bitmap has been assigned.
 	 */
 	public synchronized Bitmap getCurrentBitmap() {
 		return mCurrentBitmap;
 	}
 
 	/**
 	 * Return the view's next bitmap, or null if no bitmap has been assigned.
 	 */
 	public synchronized Bitmap getNextBitmap() {
 		return mNextBitmap;
 	}
 
 	/**
 	 * Return the view's previous bitmap, or null if no bitmap has been assigned.
 	 */
 	public synchronized Bitmap getPreviousBitmap() {
 		return mPreviousBitmap;
 	}
 	
 	/**
 	 * Sets a bitmap as the content of this ImageView.
 	 * 
 	 * @param bitmap
 	 *            The bitmap to set
 	 */
 	public synchronized void setCurrentBitmap(Bitmap bitmap) {
 		if (mCurrentBitmap != bitmap && bitmap != null) {
 			mCurrentBitmap = bitmap;
 			if (mBitmapWidth != bitmap.getWidth() || mBitmapHeight != bitmap.getHeight()) {
 				mBitmapWidth = bitmap.getWidth();
 				mBitmapHeight = bitmap.getHeight();
 				requestLayout();
 			}
 			invalidate();
 			mOffset = 0;
 		}
 	}
 
 	/**
 	 * Sets a bitmap as the content of this FullImageView.
 	 * 
 	 * @param bitmap
 	 *            The bitmap to set
 	 */
 	public synchronized void setNextBitmap(Bitmap bitmap) {
 		if (mNextBitmap != bitmap) {
 			mNextBitmap = bitmap;
 		}
 	}
 
 	/**
 	 * Sets a bitmap as the content of this FullImageView.
 	 * 
 	 * @param bitmap
 	 *            The bitmap to set
 	 */
 	public synchronized void setPreviousBitmap(Bitmap bitmap) {
 		if (mPreviousBitmap != bitmap) {
 			mPreviousBitmap = bitmap;
 		}
 	}
 	
 	public synchronized void setOffset(int offset) {
 		int minWidth = Math.max(ComicsParameters.sScreenWidth, mBitmapWidth);
 		
 		if (offset < -minWidth) {
 			offset = -minWidth;
 		} else if (offset > minWidth) {
 			offset = minWidth;
 		}
 
 		if (offset != mOffset) {
 			mOffset = offset;
 			invalidate();
 		}
 	}
 
 	public synchronized int getOffset() {
 		return mOffset;
 	}
 
 	public boolean swapNext() {
 		if (mNextBitmap == null) return false;
 
 		recyclePreviousBitmap();
 
 		setPreviousBitmap(mCurrentBitmap);
 		setCurrentBitmap(mNextBitmap);
 		setNextBitmap(null);
 
 		return true;
 	}
 
 	public boolean swapPrevious() {
 		if (mPreviousBitmap == null) return false;
 
 		recycleNextBitmap();
 
 		setNextBitmap(mCurrentBitmap);
 		setCurrentBitmap(mPreviousBitmap);
 		setPreviousBitmap(null);
 
 		return true;
 	}
 
 	public synchronized void recycleCurrentBitmap() {
 		if (mCurrentBitmap != null) {
 			mCurrentBitmap.recycle();
 			mCurrentBitmap = null;
 		}
 	}
 
 	public synchronized void recyclePreviousBitmap() {
 		if (mPreviousBitmap != null) {
 			mPreviousBitmap.recycle();
 			mPreviousBitmap = null;
 		}
 	}
 
 	public synchronized void recycleNextBitmap() {
 		if (mNextBitmap != null) {
 			mNextBitmap.recycle();
 			mNextBitmap = null;
 		}
 	}
 
 	@Override
 	protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		int w;
 		int h;
 
 		if (mCurrentBitmap == null) {
 			// If no bitmap, its intrinsic size is 0.
 			mBitmapWidth = -1;
 			mBitmapHeight = -1;
 			w = h = 0;
 		} else {
 			w = mBitmapWidth;
 			h = mBitmapHeight;
 
 			if (ComicsParameters.sScreenWidth > 0 && w < ComicsParameters.sScreenWidth) {
 				w = ComicsParameters.sScreenWidth;
 			} else if (w <= 0) {
 				w = 1;
 			}
 
 			if (ComicsParameters.sScreenHeight > 0 && h < ComicsParameters.sScreenHeight) {
 				h = ComicsParameters.sScreenHeight;
 			} else if (h <= 0) {
 				h = 1;
 			}
 		}
 
 		int pleft = getPaddingLeft();
 		int pright = getPaddingRight();
 		int ptop = getPaddingTop();
 		int pbottom = getPaddingBottom();
 
 		w += pleft + pright;
 		h += ptop + pbottom;
 
 		w = Math.max(w, getSuggestedMinimumWidth());
 		h = Math.max(h, getSuggestedMinimumHeight());
 
 		setMeasuredDimension(w, h);
 	}
 
 	@Override
 	protected synchronized void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 
 		if (mCurrentBitmap == null || mBitmapWidth == 0 || mBitmapHeight == 0) {
 			return;
 		}
 
 		canvas.getClipBounds(mRect);
 		
 		// the right limit of current bitmap
 		final int currRight = Math.min(mRect.right, mBitmapWidth);
 
 		int bottom = Math.min(mRect.bottom, mBitmapHeight);
 		
 		if (mOffset == 0) {
 			mRectSrc.set(mRect.left, mRect.top, currRight, bottom);
 
 			canvas.drawBitmap(mCurrentBitmap, mRectSrc, mRectSrc, null);
 		} else if (mOffset < 0) {
 			final int prevLeft = Math.max(mRect.right, mPreviousBitmap == null ? mBitmapWidth:mPreviousBitmap.getWidth()) + mOffset;
 			final int prevRight = Math.min(mRect.right, mPreviousBitmap == null ? mBitmapWidth:mPreviousBitmap.getWidth());
 			final int currWidth = Math.min(mRect.right + mOffset, mBitmapWidth);
 
 			// only display current image if its left border is visible
 			if (currWidth > 0) {
 				mRectSrc.set(0, mRect.top, currWidth, bottom);
 				mRectDst.set(-mOffset, mRect.top, -mOffset + currWidth, bottom);
 
 				canvas.drawBitmap(mCurrentBitmap, mRectSrc, mRectDst, null);
 			}
 
 			// only display previous image if its right border is visible
 			if (mOffset < prevRight - mRect.right) {
				if (mPreviousBitmap != null) {
 					bottom = Math.min(mRect.bottom, mPreviousBitmap.getHeight());
 					mRectDst.set(0, mRect.top, mPreviousBitmap.getWidth() - prevLeft, bottom);
 
 					mRectSrc.set(prevLeft, mRect.top, mPreviousBitmap.getWidth(), bottom);
 
 					canvas.drawBitmap(mPreviousBitmap, mRectSrc, mRectDst, null);
 				} else {
 					mRectDst.set(0, mRect.top, mBitmapWidth - prevLeft, bottom);
 					canvas.drawRect(mRectDst, mWhitePainter);
 				}
 			}
 		} else if (mOffset > 0) {
 			// only display current image if its right border is visible
 			if (mOffset < mBitmapWidth) {
 				mRectSrc.set(mOffset + mRect.left, mRect.top, mBitmapWidth, bottom);
 				mRectDst.set(mRect.left, mRect.top, mBitmapWidth - mOffset, bottom);
 
 				canvas.drawBitmap(mCurrentBitmap, mRectSrc, mRectDst, null);
 			}
 
 			final int nextLeft = Math.max(mRect.right, mBitmapWidth) - mOffset;
 			final int nextWidth = Math.min(mOffset, mNextBitmap != null ? mNextBitmap.getWidth():mBitmapWidth);  
 
			if (mNextBitmap != null) {
 				bottom = Math.min(mRect.bottom, mNextBitmap.getHeight());
 				mRectDst.set(nextLeft, mRect.top, nextLeft + nextWidth, bottom);
 				mRectSrc.set(0, mRect.top, nextWidth, bottom);
 				
 				canvas.drawBitmap(mNextBitmap, mRectSrc, mRectDst, null);
 			} else {
 				mRectDst.set(nextLeft, mRect.top, nextLeft + nextWidth, bottom);
 
 				canvas.drawRect(mRectDst, mWhitePainter);
 			}
 		}
 	}
 }
