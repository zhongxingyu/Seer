 package com.tihonchik.lenonhonor360.custom;
 
 import com.tihonchik.lenonhonor360.util.AppUtils;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.util.AttributeSet;
 import android.widget.ImageView;
 
 public class ResizableImageView extends ImageView {
 	private int mMaxWidth;
 	private int mMaxHeight;
 
 	public ResizableImageView(Context context) {
 		super(context);
 	}
 
 	public ResizableImageView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 
 	public ResizableImageView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 	}
 
 	@Override
 	public void setMaxWidth(int maxWidth) {
 		super.setMaxWidth(maxWidth);
 		mMaxWidth = maxWidth;
 	}
 
 	@Override
 	public void setMaxHeight(int maxHeight) {
 		super.setMaxHeight(maxHeight);
 		mMaxHeight = maxHeight;
 	}
 
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 
		Drawable drawable = getBackground();
 		if (drawable != null) {
 
 			int wMode = MeasureSpec.getMode(widthMeasureSpec);
 			int hMode = MeasureSpec.getMode(heightMeasureSpec);
 			if (wMode == MeasureSpec.EXACTLY || hMode == MeasureSpec.EXACTLY) {
 				return;
 			}
 
 			int maxWidth = wMode == MeasureSpec.AT_MOST ? Math.min(
 					MeasureSpec.getSize(widthMeasureSpec), mMaxWidth)
 					: mMaxWidth;
 			int maxHeight = hMode == MeasureSpec.AT_MOST ? Math.min(
 					MeasureSpec.getSize(heightMeasureSpec), mMaxHeight)
 					: mMaxHeight;
 
 			float dWidth = AppUtils.dipToPixels(this.getContext(),
 					drawable.getIntrinsicWidth());
 			float dHeight = AppUtils.dipToPixels(this.getContext(),
 					drawable.getIntrinsicHeight());
 			float ratio = dWidth / dHeight;
 
 			int width = Math.min(
 					Math.max(Math.round(dWidth), getSuggestedMinimumWidth()),
 					maxWidth);
 			int height = (int) (width / ratio);
 
 			height = Math.min(Math.max(height, getSuggestedMinimumHeight()),
 					maxHeight);
 			width = (int) (height * ratio);
 
 			if (width > maxWidth) {
 				width = maxWidth;
 				height = (int) (width / ratio);
 			}
 
 			setMeasuredDimension(width, height);
 		}
 	}
 }
