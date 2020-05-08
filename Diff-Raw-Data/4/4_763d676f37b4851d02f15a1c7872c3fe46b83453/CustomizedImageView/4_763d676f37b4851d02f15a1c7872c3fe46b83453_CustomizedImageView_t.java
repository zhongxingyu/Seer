 package com.quanleimu.widget;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.util.AttributeSet;
 import android.widget.ImageView;
 
 public class CustomizedImageView extends ImageView {
 
 	protected int bitmapWidth = 0, bitmapHeight = 0;
 	public CustomizedImageView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		// TODO Auto-generated constructor stub
 	}
 
 	public CustomizedImageView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		// TODO Auto-generated constructor stub
 	}
 
 	public CustomizedImageView(Context context) {
 		super(context);
 		// TODO Auto-generated constructor stub
 	}
 	
 	@Override
 	public void setImageBitmap(Bitmap bitmap){
 		if(null != bitmap){
 			bitmapWidth = bitmap.getWidth();
 			bitmapHeight = bitmap.getHeight();
 		}
 		
 		super.setImageBitmap(bitmap);
 	}
 	
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		/*if(MeasureSpec.UNSPECIFIED == MeasureSpec.getMode(heightMeasureSpec)
				&& this.getScaleType() == ImageView.ScaleType.CENTER_INSIDE)*/
 		{
 			if(0 != bitmapWidth){
 					int width = MeasureSpec.getSize(widthMeasureSpec);
 					int height = bitmapHeight * width / bitmapWidth;
 					
 					setMeasuredDimension(width, height);
 			}		
 		}		
 	}
 }
