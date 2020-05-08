 package com.chess.genesis;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.widget.ImageView;
 
 class MyImageView extends ImageView
 {
 	public MyImageView(final Context context)
 	{
 		super(context);
 
 		// To fix brain-dead non-auto-updating view bounds on images!!!
 		setAdjustViewBounds(true);
 		setScaleType(ImageView.ScaleType.CENTER_CROP);
 	}
 
 	public MyImageView(final Context context, final AttributeSet attrs)
 	{
 		super(context, attrs);
 
 		// To fix brain-dead non-auto-updating view bounds on images!!!
 		setAdjustViewBounds(true);
 		setScaleType(ImageView.ScaleType.CENTER_CROP);
 	}
 }
