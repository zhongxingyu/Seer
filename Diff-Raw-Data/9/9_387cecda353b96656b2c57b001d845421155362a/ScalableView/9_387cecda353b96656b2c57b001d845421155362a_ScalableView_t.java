 /*
  * Copyright (C) 2012 Jacek Marchwicki <jacek.marchwicki@gmail.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.appunite.scroll;
 
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.PointF;
 import android.os.Build;
 import android.support.v4.view.ViewCompat;
 import android.support.v4.widget.EdgeEffectCompat;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.VelocityTracker;
 import android.view.View;
 import android.view.ViewConfiguration;
 
 public abstract class ScalableView extends View {
 
 	protected static final int INVALID_POINTER_ID = -1;
 
 	private static final boolean DEBUG = false;
 	private static final String TAG = ScalableView.class.getCanonicalName();
 
 	private OverScrollerCompat mScroller;
 	private VelocityTracker mVelocityTracker = null;
 	private int mMinimumVelocity;
 
 	private int mActivePointerId = INVALID_POINTER_ID;
 	private PointF mLastMotionPoint = new PointF();
 
 	protected boolean mInteracting = false;
 	private int mOverscrollDistance;
 
 	private EdgeEffectCompat mEdgeGlowTop = null;
 	private EdgeEffectCompat mEdgeGlowBottom = null;
 	private EdgeEffectCompat mEdgeGlowLeft = null;
 	private EdgeEffectCompat mEdgeGlowRight = null;
 
 	private boolean mIsClicking = false;
 
 	public ScalableView(Context context) {
 		this(context, null, 0);
 	}
 
 	public ScalableView(Context context, AttributeSet attrs) {
 		this(context, attrs, 0);
 	}
 
 	public ScalableView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 
 		mScroller = new OverScrollerCompat(this.getContext());
 
 		this.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
 
 		final ViewConfiguration configuration = ViewConfiguration
 				.get(getContext());
 		mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
 		mOverscrollDistance = configuration.getScaledOverscrollDistance();
 	}
 
 	@Override
 	public void setOverScrollMode(int mode) {
 		if (mode != OVER_SCROLL_NEVER) {
 			if (mEdgeGlowTop == null) {
 				Context context = getContext();
 				mEdgeGlowTop = new EdgeEffectCompat(context);
 				mEdgeGlowBottom = new EdgeEffectCompat(context);
 				mEdgeGlowLeft = new EdgeEffectCompat(context);
 				mEdgeGlowRight = new EdgeEffectCompat(context);
 			}
 		} else {
 			mEdgeGlowTop = null;
 			mEdgeGlowBottom = null;
 			mEdgeGlowLeft = null;
 			mEdgeGlowRight = null;
 		}
 		super.setOverScrollMode(mode);
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		initVelocityTrackerIfNotExists();
 		mVelocityTracker.addMovement(event);
 
 		final int action = event.getAction();
 		switch (action & MotionEvent.ACTION_MASK) {
 		case MotionEvent.ACTION_DOWN: {
 			if (!mScroller.isFinished())
 				mScroller.abortAnimation();
 
 			final float x = event.getX();
 			final float y = event.getY();
 
 			mLastMotionPoint.set(x, y);
 			mActivePointerId = event.getPointerId(0);
 			startInteracting();
 			
 			mIsClicking  = touchDown(x, y);
 			break;
 		}
 
 		case MotionEvent.ACTION_MOVE: {
 			final int activePointerIndex = event
 					.findPointerIndex(mActivePointerId);
 			if (activePointerIndex >= 0) {
 				float x = event.getX(activePointerIndex);
 				float y = event.getY(activePointerIndex);
 				if (mIsClicking) {
 					mIsClicking = touchMove(x, y);
 					if (!mIsClicking) {
 						touchCanceled(x, y);
 					}
 				}
 				if (mIsClicking) {
 					mLastMotionPoint.set(x, y);
 				} else {
 					int deltaX = (int) (mLastMotionPoint.x - x);
 					int deltaY = (int) (mLastMotionPoint.y - y);
 					mLastMotionPoint.set(x, y);
 					
 					internalOverscroll(deltaX, deltaY);
 				}
 			}
 
 			break;
 		}
 
 		case MotionEvent.ACTION_UP: {
 			if (mIsClicking) {
 				mIsClicking = false;
 				touchClick(mLastMotionPoint.x, mLastMotionPoint.y);
 				this.stopInteracting();
 			} else {
 				final VelocityTracker velocityTracker = mVelocityTracker;
 				velocityTracker.computeCurrentVelocity(1000);
 				// velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
 				int initialXVelocity = (int) velocityTracker.getXVelocity();
 				int initialYVelocity = (int) velocityTracker.getYVelocity();
 	
 				if (Math.abs(initialXVelocity) > mMinimumVelocity
 						|| Math.abs(initialYVelocity) > mMinimumVelocity) {
 					this.fling(-initialXVelocity, -initialYVelocity);
 				} else {
 					if (mScroller.springBack(getScrollX(), getScrollY(), 0,
 							getScrollRangeX(), 0, getScrollRangeY())) {
 						oldPostInvalidateOnAnimation();
 					}
 	
 					this.stopInteracting();
 				}
 			}
 			mActivePointerId = INVALID_POINTER_ID;
 			break;
 		}
 		case MotionEvent.ACTION_CANCEL: {
 			if (mIsClicking) {
 				touchCanceled(mLastMotionPoint.x, mLastMotionPoint.y);
 			} else {
 				if (mScroller.springBack(getScrollX(), getScrollY(), 0,
 						getScrollRangeX(), 0, getScrollRangeY())) {
 					oldPostInvalidateOnAnimation();
 				}
 			}
 
 			mActivePointerId = INVALID_POINTER_ID;
 			break;
 		}
 
 		case MotionEvent.ACTION_POINTER_DOWN: {
 			final int index = event.getActionIndex();
 			mLastMotionPoint.set(event.getX(), event.getY());
 			mActivePointerId = event.getPointerId(index);
 			break;
 		}
 
 		case MotionEvent.ACTION_POINTER_UP: {
 			final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
 			final int pointerId = event.getPointerId(pointerIndex);
 			if (pointerId == mActivePointerId) {
 				// This was our active pointer going up. Choose a new
 				// active pointer and adjust accordingly.
 				final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
 				mLastMotionPoint.set(event.getX(newPointerIndex),
 						event.getY(newPointerIndex));
 				mActivePointerId = event.getPointerId(newPointerIndex);
 				if (mVelocityTracker != null) {
 					mVelocityTracker.clear();
 				}
 			}
 
 			break;
 		}
 
 		}
 
 		return true;
 	}
 	
 	protected void internalOverscroll(int deltaX, int deltaY) {
 		final int oldX = getScrollX();
 		final int oldY = getScrollY();
 		final int overscrollMode = getOverScrollMode();
 		final boolean canOverscroll = overscrollMode == OVER_SCROLL_ALWAYS
 				|| (overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS);
 		if (DEBUG) {
 			Log.v(TAG, String.format(
 					"onverScrollBy: %d, %d, %d, %d, %d, %d",
 					deltaX, deltaY, oldX, oldY, getScrollRangeX(),
 					getScrollRangeY()));
 		}
 		if (overScrollBy(deltaX, deltaY, oldX, oldY,
 				getScrollRangeX(), getScrollRangeY(),
 				mOverscrollDistance, mOverscrollDistance, true)) {
 			// Break our velocity if we hit a scroll barrier.
 			mVelocityTracker.clear();
 		}
 		onScrollChanged(getScrollX(), getScrollY(), oldX, oldY);
 
 		if (canOverscroll) {
 			final int pulledToY = oldY + deltaY;
 			final int pulledToX = oldX + deltaX;
 			if (pulledToY < 0) {
 				mEdgeGlowTop.onPull((float) deltaY / getHeight());
 				if (!mEdgeGlowBottom.isFinished()) {
 					mEdgeGlowBottom.onRelease();
 				}
 			} else if (pulledToY > getScrollRangeY()) {
 				mEdgeGlowBottom
 						.onPull((float) deltaY / getHeight());
 				if (!mEdgeGlowTop.isFinished()) {
 					mEdgeGlowTop.onRelease();
 				}
 			}
 			if (pulledToX < 0) {
 				mEdgeGlowLeft.onPull((float) deltaX / getWidth());
 				if (!mEdgeGlowRight.isFinished()) {
 					mEdgeGlowRight.onRelease();
 				}
 			} else if (pulledToX > getScrollRangeX()) {
 				mEdgeGlowRight.onPull((float) deltaX / getWidth());
 				if (!mEdgeGlowLeft.isFinished()) {
 					mEdgeGlowLeft.onRelease();
 				}
 			}
 			if (!mEdgeGlowTop.isFinished()
 					|| !mEdgeGlowBottom.isFinished()
 					|| !mEdgeGlowLeft.isFinished()
 					|| !mEdgeGlowRight.isFinished()) {
 				oldPostInvalidateOnAnimation();
 			}
 		}
 	}
 
 	protected float computeTouchX(float x) {
 		return x + getScrollX();
 	}
 	protected float computeTouchY(float y) {
 		return y + getScrollY();
 	}
 
 	private void touchCanceled(float x, float y) {
 		float touchX = computeTouchX(x);
 		float touchY = computeTouchY(y);
 		onTouchCanceled(touchX, touchY);
 	}
 
 	private void touchClick(float x, float y) {
 		float touchX = computeTouchX(x);
 		float touchY = computeTouchY(y);
 		onTouchClick(touchX, touchY);
 	}
 
 	private boolean touchMove(float x, float y) {
 		float touchX = computeTouchX(x);
 		float touchY = computeTouchY(y);
 		return onTouchMove(touchX, touchY);
 	}
 
 	private boolean touchDown(float x, float y) {
 		float touchX = computeTouchX(x);
 		float touchY = computeTouchY(y);
 		return onTouchDown(touchX, touchY);
 	}
 
 	protected void onTouchClick(float touchX, float touchY) {
 	}
 
 	protected boolean onTouchMove(float touchX, float touchY) {
 		return false;
 	}
 
 	protected void onTouchCanceled(float touchX, float touchY) {
 	}
 
 	protected boolean onTouchDown(float touchX, float touchY) {
 		return false;
 	}
 
 	private void initVelocityTrackerIfNotExists() {
 		if (mVelocityTracker == null) {
 			mVelocityTracker = VelocityTracker.obtain();
 		}
 	}
 
 	private int getScrollRangeY() {
 		int range = computeVerticalScrollRange() - this.getHeight();
 		if (range < 0) {
 			return 0;
 		}
 		return range;
 	}
 
 	private int getScrollRangeX() {
 		int range = computeHorizontalScrollRange() - this.getWidth();
 		if (range < 0) {
 			return 0;
 		}
 		return range;
 	}
 
 	private void oldPostInvalidateOnAnimation() {
 		ViewCompat.postInvalidateOnAnimation(this);
 	}
 
 	private void fling(int velocityX, int velocityY) {
 		int x = this.getScrollX();
 		int y = this.getScrollY();
 
 		this.startInteracting();
 		// fScroller.setFriction( ViewConfiguration.getScrollFriction( ) );
 		mScroller.fling(x, y, velocityX, velocityY, 0, getScrollRangeX(), 0,
 				getScrollRangeY());
 		oldPostInvalidateOnAnimation();
 	}
 
 	private void startInteracting() {
 		mInteracting = true;
 	}
 
 	private void stopInteracting() {
 		mInteracting = false;
 	}
 
 	@Override
 	public void computeScroll() {
 		if (DEBUG) {
 			Log.v(TAG, String.format("computeScroll: %d %d", getScrollX(),
 					getScrollY()));
 		}
 		if (mScroller.computeScrollOffset()) {
 			int oldX = getScrollX();
 			int oldY = getScrollY();
 			int x = mScroller.getCurrX();
 			int y = mScroller.getCurrY();
 			if (oldX == x && oldY == y) {
 				return;
 			}
 
 			if (DEBUG) {
 				Log.v(TAG, String.format(
 						"computeScrollOffset: %d, %d -> %d, %d", oldX, oldY, x,
 						y));
 			}
 			final int overscrollMode = getOverScrollMode();
 			final boolean canOverscroll = overscrollMode == OVER_SCROLL_ALWAYS
 					|| (overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS);
 
 			overScrollBy(x - oldX, y - oldY, oldX, oldY, getScrollRangeX(),
 					getScrollRangeY(), mOverscrollDistance,
 					mOverscrollDistance, false);
 			onScrollChanged(getScrollX(), getScrollY(), oldX, oldY);
 
 			if (canOverscroll) {
 				if (y < 0 && oldY >= 0) {
 					mEdgeGlowTop.onAbsorb(y);
 				} else if (y > getScrollRangeY() && oldY <= getScrollRangeY()) {
 					mEdgeGlowBottom.onAbsorb(y);
 				}
 
 				if (x < 0 && oldX >= 0) {
 					mEdgeGlowLeft.onAbsorb(x);
 				} else if (x > getScrollRangeX() && oldX <= getScrollRangeX()) {
 					mEdgeGlowRight.onAbsorb(x);
 				}
 			}
 
 			if (mScroller.isFinished()) {
 				this.stopInteracting();
 			}
 
 			oldPostInvalidateOnAnimation();
 		}
 	}
 	
 	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
 	private void setScrollXYCompat(int scrollX, int scrollY) {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
 			setScrollX(scrollX);
 			setScrollY(scrollY);
 		} else {
 			super.scrollTo(scrollX, scrollY);
 		}
 	}
 
 	@SuppressLint("NewApi")
 	@Override
 	protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX,
 			boolean clampedY) {
 		if (DEBUG) {
 			Log.v(TAG, String.format("onOverScrolled: %d, %d, %s, %s", scrollX,
 					scrollY, clampedX ? "true" : "false", clampedY ? "true"
 							: "false"));
 		}
 		// Treat animating scrolls differently; see #computeScroll() for why.
 		if (!mScroller.isFinished()) {
 			setScrollXYCompat(scrollX, scrollY);
 
 			if (clampedX || clampedY) {
 				mScroller.springBack(scrollX, scrollY, 0, getScrollRangeX(), 0,
 						getScrollRangeY());
 				oldPostInvalidateOnAnimation();
 			}
 		} else {
 			super.scrollTo(scrollX, scrollY);
 		}
 		awakenScrollBars();
 	}
 
 	@Override
 	protected int computeHorizontalScrollRange() {
 		return getWorksheetWidth();
 	}
 
 	@Override
 	protected int computeVerticalScrollRange() {
 		return getWorksheetHeight();
 	}
 
 	@Override
 	protected int computeHorizontalScrollOffset() {
 		return this.getScrollX();
 	}
 
 	@Override
 	protected int computeVerticalScrollOffset() {
 		return this.getScrollY();
 	}
 
 	protected abstract int getWorksheetWidth();
 
 	protected abstract int getWorksheetHeight();
 
 	protected abstract void onDraw(Canvas canvas, int left, int top, int right,
 			int bottom);
 
 	@Override
 	protected void onDraw(Canvas canvas) {
		int left = getScrollX();
		int top = getScrollY();
		int right = getWidth() + left;
		int bottom = getHeight() + top;
 
 		onDraw(canvas, left, top, right, bottom);
 
 		onDrawEdges(canvas);
 	}
 
 	private void onDrawEdges(Canvas canvas) {
 		if (mEdgeGlowTop != null) {
 			final int scrollX = getScrollX();
 			final int scrollY = getScrollY();
 			final int paddingLeft = getPaddingLeft();
 			final int paddingRight = getPaddingRight();
 			final int paddingTop = getPaddingTop();
 			final int paddingBottom = getPaddingBottom();
 			final int width = getWidth();
 			final int height = getHeight();
 
 			final int relativeLeft = paddingLeft + scrollX;
 			final int relativeTop = paddingTop + scrollY;
 
 			final int glowWidth = width - paddingLeft - paddingRight;
 			final int glowHeight = height - paddingTop - paddingBottom;
 
 			if (!mEdgeGlowTop.isFinished()) {
 				final int restoreCount = canvas.save();
 
 				canvas.translate(relativeLeft, Math.min(0, scrollY));
 				mEdgeGlowTop.setSize(glowWidth, height);
 				if (mEdgeGlowTop.draw(canvas)) {
 					oldPostInvalidateOnAnimation();
 				}
 				canvas.restoreToCount(restoreCount);
 			}
 			if (!mEdgeGlowBottom.isFinished()) {
 				final int restoreCount = canvas.save();
 
 				canvas.translate(-glowWidth + relativeLeft,
 						Math.max(getScrollRangeY(), scrollY) + height);
 				canvas.rotate(180, glowWidth, 0);
 				mEdgeGlowBottom.setSize(glowWidth, height);
 				if (mEdgeGlowBottom.draw(canvas)) {
 					oldPostInvalidateOnAnimation();
 				}
 				canvas.restoreToCount(restoreCount);
 			}
 
 			if (!mEdgeGlowLeft.isFinished()) {
 				final int restoreCount = canvas.save();
 				canvas.translate(Math.min(0, scrollX), relativeTop + glowHeight);
 				canvas.rotate(270, 0, 0);
 				mEdgeGlowLeft.setSize(glowHeight, width);
 				if (mEdgeGlowLeft.draw(canvas)) {
 					oldPostInvalidateOnAnimation();
 				}
 				canvas.restoreToCount(restoreCount);
 			}
 			if (!mEdgeGlowRight.isFinished()) {
 				final int restoreCount = canvas.save();
 
 				canvas.translate(Math.max(getScrollRangeX(), scrollX) + width,
 						relativeTop);
 				canvas.rotate(90, 0, 0);
 				mEdgeGlowRight.setSize(glowHeight, width);
 				if (mEdgeGlowRight.draw(canvas)) {
 					oldPostInvalidateOnAnimation();
 				}
 				canvas.restoreToCount(restoreCount);
 			}
 		}
 	}
 
 }
