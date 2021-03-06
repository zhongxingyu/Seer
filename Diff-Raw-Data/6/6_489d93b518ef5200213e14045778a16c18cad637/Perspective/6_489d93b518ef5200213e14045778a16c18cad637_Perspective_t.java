 /**
  *  Paintroid: An image manipulation application for Android.
  *  Copyright (C) 2010-2013 The Catrobat Team
  *  (<http://developer.catrobat.org/credits>)
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as
  *  published by the Free Software Foundation, either version 3 of the
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of the GNU Affero General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.catrobat.paintroid.ui;
 
 import java.io.Serializable;
 
 import org.catrobat.paintroid.MenuFileActivity;
 import org.catrobat.paintroid.PaintroidApplication;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Point;
 import android.graphics.PointF;
 import android.graphics.Rect;
 import android.util.DisplayMetrics;
 import android.view.Display;
 import android.view.SurfaceHolder;
 import android.view.WindowManager;
 
 /**
  * The purpose of this class is to provide an independent interface to
  * manipulate the scale and translation of the DrawingSurface. The direct
  * manipulation of the Canvas is synchronized on the SurfaceHolder on which the
  * DrawingSurface must also synchronize its own drawing.
  */
 public class Perspective implements Serializable {
 	private static final long serialVersionUID = 7742690846128292452L;
 
 	public static final float MIN_SCALE = 0.1f;
 	public static final float MAX_SCALE = 20f;
 	public static final float SCROLL_BORDER = 50f;
	private static final float BORDER_ZOOM_FACTOR = 0.95f;
 	private static final float ACTION_BAR_HEIGHT = MenuFileActivity.ACTION_BAR_HEIGHT;
 
 	private float mSurfaceWidth;
 	private float mSurfaceHeight;
 	private float mSurfaceCenterX;
 	private float mSurfaceCenterY;
 	private float mSurfaceScale;
 	private float mSurfaceTranslationX;
 	private float mSurfaceTranslationY;
 	private float mScreenWidth;
 	private float mScreenHeight;
 	private float mBitmapWidth;
 	private float mBitmapHeight;
 	private float mScreenDensity;
 	private boolean mIsFullscreen;
 
 	public Perspective(SurfaceHolder holder) {
 		setSurfaceHolder(holder);
 		mSurfaceScale = 1f;
 		DisplayMetrics metrics = new DisplayMetrics();
 		Display display = ((WindowManager) PaintroidApplication.applicationContext
 				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
 		display.getMetrics(metrics);
 		mScreenWidth = metrics.widthPixels;
 		mScreenHeight = metrics.heightPixels;
 		mScreenDensity = metrics.density;
 		mIsFullscreen = false;
 	}
 
 	public synchronized void setSurfaceHolder(SurfaceHolder holder) {
 		Rect surfaceFrame = holder.getSurfaceFrame();
 		mSurfaceWidth = surfaceFrame.right;
 		mSurfaceHeight = surfaceFrame.bottom;
 		mSurfaceCenterX = surfaceFrame.exactCenterX();
 		mSurfaceCenterY = surfaceFrame.exactCenterY();
 	}
 
 	public synchronized void resetScaleAndTranslation() {
 
 		float actionbarHeight = ACTION_BAR_HEIGHT * mScreenDensity;
 		mBitmapWidth = PaintroidApplication.drawingSurface.getBitmapWidth();
 		mBitmapHeight = PaintroidApplication.drawingSurface.getBitmapHeight();
 		mSurfaceScale = 1f;
 
 		if (mSurfaceWidth == 0 || mSurfaceHeight == 0) {
 			mSurfaceTranslationX = 0f;
 			mSurfaceTranslationY = -actionbarHeight;
 		}
 
 		else {
 			mSurfaceTranslationX = mScreenWidth / 2 - mBitmapWidth / 2;
 
 			mSurfaceTranslationY = (mScreenHeight / 2 - mBitmapHeight / 2)
 					- actionbarHeight;
 
 			if (mIsFullscreen) {
 				mSurfaceTranslationY += actionbarHeight;
 			}
 
 		}

		float zoomFactor = (mIsFullscreen) ? 1.0f : BORDER_ZOOM_FACTOR;
		mSurfaceScale = getScaleForCenterBitmap() * zoomFactor;
 
 	}
 
 	public synchronized void setScale(float scale) {
 		if (scale >= MIN_SCALE) {
 			mSurfaceScale = scale;
 		} else {
 			mSurfaceScale = MIN_SCALE;
 		}
 	}
 
 	public synchronized void multiplyScale(float factor) {
 		mSurfaceScale *= factor;
 		if (mSurfaceScale < MIN_SCALE) {
 			mSurfaceScale = MIN_SCALE;
 		} else if (mSurfaceScale > MAX_SCALE) {
 			mSurfaceScale = MAX_SCALE;
 		}
 	}
 
 	public synchronized void translate(float dx, float dy) {
 		mSurfaceTranslationX += dx / mSurfaceScale;
 		mSurfaceTranslationY += dy / mSurfaceScale;
 
 		float xmax = (mSurfaceWidth - mSurfaceCenterX - SCROLL_BORDER)
 				/ mSurfaceScale + mSurfaceCenterX;
 		if (mSurfaceTranslationX > xmax) {
 			mSurfaceTranslationX = xmax;
 		} else if (mSurfaceTranslationX < -xmax) {
 			mSurfaceTranslationX = -xmax;
 		}
 
 		float ymax = (mSurfaceHeight - mSurfaceCenterY - SCROLL_BORDER)
 				/ mSurfaceScale + mSurfaceCenterY;
 		if (mSurfaceTranslationY > ymax) {
 			mSurfaceTranslationY = ymax;
 		} else if (mSurfaceTranslationY < -ymax) {
 			mSurfaceTranslationY = -ymax;
 		}
 	}
 
 	public synchronized void convertFromScreenToCanvas(Point p) {
 		p.x = (int) ((p.x - mSurfaceCenterX) / mSurfaceScale + mSurfaceCenterX - mSurfaceTranslationX);
 		p.y = (int) ((p.y - mSurfaceCenterY) / mSurfaceScale + mSurfaceCenterY - mSurfaceTranslationY);
 	}
 
 	public synchronized void convertFromScreenToCanvas(PointF p) {
 		p.x = (p.x - mSurfaceCenterX) / mSurfaceScale + mSurfaceCenterX
 				- mSurfaceTranslationX;
 		p.y = (p.y - mSurfaceCenterY) / mSurfaceScale + mSurfaceCenterY
 				- mSurfaceTranslationY;
 	}
 
 	public synchronized void convertFromCanvasToScreen(PointF p) {
 		p.x = ((p.x + mSurfaceTranslationX - mSurfaceCenterX) * mSurfaceScale + mSurfaceCenterX);
 		p.y = ((p.y + mSurfaceTranslationY - mSurfaceCenterY) * mSurfaceScale + mSurfaceCenterY);
 
 	}
 
 	public synchronized void applyToCanvas(Canvas canvas) {
 		canvas.scale(mSurfaceScale, mSurfaceScale, mSurfaceCenterX,
 				mSurfaceCenterY);
 		canvas.translate(mSurfaceTranslationX, mSurfaceTranslationY);
 	}
 
 	public float getScale() {
 		return mSurfaceScale;
 	}
 
 	public float getScaleForCenterBitmap() {
 
 		float actionbarHeight = (mIsFullscreen) ? 0.0f : ACTION_BAR_HEIGHT
 				* mScreenDensity;
 
 		float ratioDependentScale;
 		float screenSizeRatio = mScreenWidth
 				/ (mScreenHeight - actionbarHeight * 2);
 		float bitmapSizeRatio = mBitmapWidth / mBitmapHeight;
 
 		if (screenSizeRatio > bitmapSizeRatio) {
 			ratioDependentScale = (mScreenHeight - actionbarHeight * 2)
 					/ mBitmapHeight;
 		} else {
 			ratioDependentScale = mScreenWidth / mBitmapWidth;
 		}
 
 		if (ratioDependentScale > 1f) {
 			ratioDependentScale = 1f;
 		}
 		if (ratioDependentScale < MIN_SCALE) {
 			ratioDependentScale = MIN_SCALE;
 		}
 
 		return ratioDependentScale;
 	}
 
 	public void setFullscreen(boolean isFullscreen) {
 		mIsFullscreen = isFullscreen;
 		resetScaleAndTranslation();
 	}
 
 }
