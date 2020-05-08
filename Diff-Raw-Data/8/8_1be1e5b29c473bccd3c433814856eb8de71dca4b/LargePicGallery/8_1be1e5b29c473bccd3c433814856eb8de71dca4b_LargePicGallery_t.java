 package com.albaniliu.chuangxindemo.widget;
 
 import android.content.Context;
 import android.support.v4.view.ViewPager;
 import android.util.AttributeSet;
 import android.util.DisplayMetrics;
 import android.view.MotionEvent;
 
 /**
  * LargePicGallery 大图页界面gallery
  * 
  * @author Liang
  */
 public class LargePicGallery extends ViewPager {
 	private BaseImageView imageView;
 	public int mScreenWidth;
 	public int mScreenHeight;
 	
 	private SingleTapListner mTapUpListener;
 
 	/**
 	 * 监听左右滑动事件
 	 */
 	public static interface SingleTapListner {
 		void onSingleTapUp();
 	}
 
 	public LargePicGallery(Context context) {
 		super(context);
 		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
 		mScreenWidth = metrics.widthPixels;
 		mScreenHeight = metrics.heightPixels;
 	}
 
 	public void setTapUpListener(SingleTapListner listener) {
 		mTapUpListener = listener;
 	}
 
 	BaseImageView getImageView() {
 		int pos = this.getCurrentItem();
 		int count = getChildCount();
 		for (int i = 0; i < count; ++i) {
 			int index = (Integer) getChildAt(i).getTag();
 			if (pos == index) {
 				return (BaseImageView) getChildAt(i);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Constructor
 	 * 
 	 */
 	public LargePicGallery(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
 		mScreenWidth = metrics.widthPixels;
 		mScreenHeight = metrics.heightPixels;
 	}
 
 	float baseValue = 0;
 	float originalScale = 1;
 	float prePosX = 0f;
 	float prePosY = 0f;
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		imageView = getImageView();
 
 		if (imageView.isZoomIn()) {
 			return false;
 		}
 		return super.onTouchEvent(event);
 	}
 	
 	@Override
 	public boolean onInterceptTouchEvent(MotionEvent event) {
 		imageView = getImageView();
 
 		if (imageView.isZoomIn()) {
 			return false;
 		}
		try {
		    return super.onInterceptTouchEvent(event);
		} catch (IllegalArgumentException e) {
		    return false;
		} catch (RuntimeException e) {
		    return false;
		}
 	}
 }
