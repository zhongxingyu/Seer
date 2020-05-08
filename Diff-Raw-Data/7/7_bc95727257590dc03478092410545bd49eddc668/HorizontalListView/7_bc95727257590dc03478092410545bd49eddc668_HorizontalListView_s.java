 /*
  * HorizontalListView.java v1.5
  *
  * 
  * The MIT License
  * Copyright (c) 2011 Paul Soucy (paul@dev-smart.com)
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  *
  */
 
 package com.fyodorwolf.studybudy.ui;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import com.fyodorwolf.studybudy.R;
 import com.nostra13.universalimageloader.core.DisplayImageOptions;
 import com.nostra13.universalimageloader.core.ImageLoader;
 import com.nostra13.universalimageloader.core.assist.ImageScaleType;
 
 import android.content.Context;
 import android.content.Intent;
 import android.database.DataSetObserver;
 import android.graphics.Bitmap;
 import android.graphics.Rect;
 import android.net.Uri;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.Scroller;
 
 public class HorizontalListView extends AdapterView<ListAdapter> {
 
 	private static final String TAG = "HorizontalListView";
 	public boolean mAlwaysOverrideTouch = true;
 	protected ListAdapter mAdapter;
 	private int mLeftViewIndex = -1;
 	private int mRightViewIndex = 0;
 	protected int mCurrentX;
 	protected int mNextX;
 	private int mMaxX = Integer.MAX_VALUE;
 	private int mDisplayOffset = 0;
 	protected Scroller mScroller;
 	private GestureDetector mGesture;
 	private Queue<View> mRemovedViewQueue = new LinkedList<View>();
 	private int selectedIndex;
 	private OnItemSelectedListener mOnItemSelected;
 	private OnItemClickListener mOnItemClicked;
 	private boolean mDataChanged = false;
 
 	public HorizontalListView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		initView();
 	}
 
 	private synchronized void initView() {
 		mLeftViewIndex = -1;
 		mRightViewIndex = 0;
 		mDisplayOffset = 0;
 		mCurrentX = 0;
 		mNextX = 0;
 		mMaxX = Integer.MAX_VALUE;
 		mScroller = new Scroller(getContext());
 		mGesture = new GestureDetector(getContext(), mOnGesture);
 	}
 
 	@Override
 	public void setOnItemSelectedListener(
 			AdapterView.OnItemSelectedListener listener) {
 		mOnItemSelected = listener;
 	}
 
 	@Override
 	public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
 		mOnItemClicked = listener;
 	}
 
 	private DataSetObserver mDataObserver = new DataSetObserver() {
 
 		@Override
 		public void onChanged() {
 			synchronized (HorizontalListView.this) {
 				mDataChanged = true;
 			}
 			invalidate();
 			requestLayout();
 		}
 
 		@Override
 		public void onInvalidated() {
 			reset();
 			invalidate();
 			requestLayout();
 		}
 
 	};
 
 	@Override
 	public ListAdapter getAdapter() {
 		return mAdapter;
 	}
 
 	@Override
 	public View getSelectedView() {
 		return mAdapter.getView(selectedIndex, null, null);
 	}
 
 	InflatorCallbacks mInflatorCallbacks = new InflatorCallbacks() {
 		@Override
 		public void onFinishInflate() {
 		}
 	};
 
 	@Override
 	protected void onFinishInflate() {
 		super.onFinishInflate();
 		this.mInflatorCallbacks.onFinishInflate();
 	}
 
 	public void setOnInflatorCallbacks(InflatorCallbacks inflatorCallbacks) {
 		this.mInflatorCallbacks = inflatorCallbacks;
 	}
 
 	public interface InflatorCallbacks {
 		public void onFinishInflate();
 	}
 
 	@Override
 	public void setAdapter(ListAdapter adapter) {
 		if (mAdapter != null) {
 			mAdapter.unregisterDataSetObserver(mDataObserver);
 		}
 		mAdapter = adapter;
 		mAdapter.registerDataSetObserver(mDataObserver);
 		reset();
 	}
 
 	private synchronized void reset() {
 		initView();
 		removeAllViewsInLayout();
 		requestLayout();
 	}
 
 	@Override
 	public void setSelection(int position) {
 		selectedIndex = position;
 	}
 
 	private void addAndMeasureChild(final View child, int viewPos) {
 		LayoutParams params = child.getLayoutParams();
 		if (params == null) {
 			params = new LayoutParams(LayoutParams.MATCH_PARENT,
 					LayoutParams.MATCH_PARENT);
 		}
 
 		addViewInLayout(child, viewPos, params, true);
 		child.measure(
 				MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
 				MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
 	}
 
 	@Override
 	protected synchronized void onLayout(boolean changed, int left, int top,
 			int right, int bottom) {
 		super.onLayout(changed, left, top, right, bottom);
 
		//log.d(TAG, changed + "," + top + "," + right + "," + bottom + ","
				+ left);
 
 		if (mAdapter == null) {
 			return;
 		}
 
 		if (mDataChanged) {
 			int oldCurrentX = mCurrentX;
 			initView();
 			removeAllViewsInLayout();
 			mNextX = oldCurrentX;
 			mDataChanged = false;
 		}
 
 		if (mScroller.computeScrollOffset()) {
 			int scrollx = mScroller.getCurrX();
 			mNextX = scrollx;
 		}
 
 		if (mNextX < 0) {
 			mNextX = 0;
 			mScroller.forceFinished(true);
 		}
 		if (mNextX > mMaxX) {
 			mNextX = mMaxX;
 			mScroller.forceFinished(true);
 		}
 
 		// int dx = (mCurrentX - mNextX) / 2;
 		// dx is distance traveled in x direction since last call.
 		int dx = (mCurrentX - mNextX);
 		removeNonVisibleItems(dx);
 		fillList(dx);
 		positionItems(dx);
 
 		mCurrentX = mNextX;
 
 		if (!mScroller.isFinished()) {
 			post(new Runnable() {
 				@Override
 				public void run() {
 					requestLayout();
 				}
 			});
 
 		}
 
 	}
 
 	private void fillList(final int dx) {
 		View groupChild;
 		int childEdge = 0;
 		if (dx <= 0) {
 			// fill items on the right or on init.
 			groupChild = getChildAt(getChildCount() - 1);
 			if (groupChild != null) {
 				childEdge = groupChild.getRight();
 			}
 			while (childEdge + dx < getWidth()
 					&& mRightViewIndex < mAdapter.getCount()) {
 				View child = mAdapter.getView(mRightViewIndex,
 						mRemovedViewQueue.poll(), this);
 				addAndMeasureChild(child, -1);
 				childEdge += child.getMeasuredWidth();
 				//log.d(TAG, "rE:" + childEdge);
 				if (mRightViewIndex == mAdapter.getCount() - 1) {
 					mMaxX = getWidth() - mCurrentX - childEdge;
					//log.d(TAG, mMaxX + " = " + mCurrentX + " + " + childEdge
							+ " - " + getWidth());
 				}
 				mRightViewIndex++;
 			}
 		} else {
 			// fill items on the left;
 			groupChild = getChildAt(0);
 			if (groupChild != null) {
 				childEdge = groupChild.getLeft();
 			}
 			while (childEdge + dx > 0 && mLeftViewIndex >= 0) {
 				View child = mAdapter.getView(mLeftViewIndex,
 						mRemovedViewQueue.poll(), this);
 				addAndMeasureChild(child, 0);
 				childEdge -= child.getMeasuredWidth();
 				//log.d(TAG, "lE:" + childEdge);
 				mLeftViewIndex--;
 				mDisplayOffset -= child.getMeasuredWidth();
 			}
 		}
 	}
 
 	private void removeNonVisibleItems(final int dx) {
 		View child = getChildAt(0);
 		while (child != null && child.getRight() + dx <= 0) {
 			mDisplayOffset += child.getMeasuredWidth();
 			mRemovedViewQueue.offer(child);
 			removeViewInLayout(child);
 			mLeftViewIndex++;
 			child = getChildAt(0);
 
 		}
 
 		child = getChildAt(getChildCount() - 1);
 		while (child != null && child.getLeft() + dx >= getWidth()) {
 			mRemovedViewQueue.offer(child);
 			removeViewInLayout(child);
 			mRightViewIndex--;
 			child = getChildAt(getChildCount() - 1);
 		}
 	}
 
 	private void positionItems(final int dx) {
 		//log.d(TAG, "DO:" + mDisplayOffset);
 		if (getChildCount() > 0) {
 			mDisplayOffset += dx;
 			int left = mDisplayOffset;
 			for (int i = 0; i < getChildCount(); i++) {
 				View child = getChildAt(i);
 				int childWidth = child.getMeasuredWidth();
 				child.layout(left, 0, left + childWidth,
 						child.getMeasuredHeight());
 				//log.d(TAG, "left:" + left);
 				left += childWidth;
 			}
 		}
 	}
 
 	public synchronized void scrollTo(int x) {
 		mScroller.startScroll(mNextX, 0, x - mNextX, 0);
 		requestLayout();
 	}
 
 	@Override
 	public boolean dispatchTouchEvent(MotionEvent ev) {
 		boolean handled = mGesture.onTouchEvent(ev);
 		return handled;
 	}
 
 	protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 			float velocityY) {
 		synchronized (HorizontalListView.this) {
 			mScroller.fling(mNextX, 0, (int) -velocityX, 0, 0, mMaxX, 0, 0);
 		}
 		requestLayout();
 
 		return true;
 	}
 
 	protected boolean onDown(MotionEvent e) {
 		mScroller.forceFinished(true);
 		return true;
 	}
 
 	private OnGestureListener mOnGesture = new GestureDetector.SimpleOnGestureListener() {
 		@Override
 		public boolean onDown(MotionEvent e) {
 			return HorizontalListView.this.onDown(e);
 		}
 
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 				float velocityY) {
 			return HorizontalListView.this
 					.onFling(e1, e2, velocityX, velocityY);
 		}
 
 		@Override
 		public boolean onScroll(MotionEvent e1, MotionEvent e2,
 				float distanceX, float distanceY) {
 			synchronized (HorizontalListView.this) {
 				mNextX += (int) distanceX;
 			}
 			requestLayout();
 			return true;
 		}
 
 		@Override
 		public boolean onSingleTapConfirmed(MotionEvent e) {
 			Rect viewRect = new Rect();
 			for (int i = 0; i < getChildCount(); i++) {
 				View child = getChildAt(i);
 				int left = child.getLeft();
 				int right = child.getRight();
 				int top = child.getTop();
 				int bottom = child.getBottom();
 				viewRect.set(left, top, right, bottom);
 				if (viewRect.contains((int) e.getX(), (int) e.getY())) {
 					if (mOnItemClicked != null) {
 						mOnItemClicked.onItemClick(HorizontalListView.this,
 								child, mLeftViewIndex + 1 + i,
 								mAdapter.getItemId(mLeftViewIndex + 1 + i));
 					}
 					if (mOnItemSelected != null) {
 						mOnItemSelected.onItemSelected(HorizontalListView.this,
 								child, mLeftViewIndex + 1 + i,
 								mAdapter.getItemId(mLeftViewIndex + 1 + i));
 					}
 					break;
 				}
 
 			}
 			return true;
 		}
 
 	};
 
 	public void setGalleryItems(ArrayList<File> photoes, Context context) {
 		this.setAdapter(new GalleryAdapter(photoes));
 		this.setOnItemClickListener(new GalleryClickListener(photoes, context));
 	}
 
 	private class GalleryAdapter implements ListAdapter {
 
 		public ArrayList<File> photoes;
 
 		public GalleryAdapter(ArrayList<File> photoes) {
 			this.photoes = photoes;
 		}
 
 		@Override
 		public int getItemViewType(int position) {
 			return 0;
 		}
 
 		@Override
 		public int getViewTypeCount() {
 			return 1;
 		}
 
 		@Override
 		public boolean hasStableIds() {
 			return false;
 		}
 
 		@Override
 		public boolean areAllItemsEnabled() {
 			return true;
 		}
 
 		@Override
 		public boolean isEnabled(int position) {
 			return true;
 		}
 
 		@Override
 		public void registerDataSetObserver(DataSetObserver observer) {
 		}
 
 		@Override
 		public void unregisterDataSetObserver(DataSetObserver observer) {
 		}
 
 		@Override
 		public boolean isEmpty() {
 			return photoes.isEmpty();
 		}
 
 		@Override
 		public int getCount() {
 			return photoes.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return photoes.get(position);
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return Long.valueOf(position);
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View photoItem = LayoutInflater.from(parent.getContext()).inflate(
 					R.layout.photo_item, null);
 			// RE-SAMPLE IMAGE INTO IMAGE_VIEW
 			String myUri = Uri.fromFile(photoes.get(position)).toString();
 			ImageView myImage = (ImageView) photoItem
 					.findViewById(R.id.galley_photo_item);
 			DisplayImageOptions opts = (new DisplayImageOptions.Builder())
 					.cacheInMemory().bitmapConfig(Bitmap.Config.RGB_565)
 					.imageScaleType(ImageScaleType.EXACTLY).build();
 			ImageLoader.getInstance().displayImage(myUri, myImage, opts);
 			return photoItem;
 		}
 	}
 
 	private class GalleryClickListener implements OnItemClickListener {
 
 		public ArrayList<File> photoes;
 		public Context caller;
 
 		public GalleryClickListener(ArrayList<File> photoes, Context context) {
 			this.photoes = photoes;
 			this.caller = context;
 		}
 
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			Intent intent = new Intent(Intent.ACTION_VIEW);
 			intent.setDataAndType(Uri.fromFile(photoes.get(position)),
 					"image/*");
 			caller.startActivity(intent);
 		}
 	}
 
 }
