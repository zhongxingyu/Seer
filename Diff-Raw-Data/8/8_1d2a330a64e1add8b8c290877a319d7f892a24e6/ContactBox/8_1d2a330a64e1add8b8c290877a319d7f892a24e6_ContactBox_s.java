 package com.ingloriouscoders.sheep;
 
 
 import android.view.*;
 import android.graphics.*;
 import android.content.*;
 import android.util.*;
 import android.graphics.PorterDuff.Mode;
 import android.view.InputEvent.*;
 import android.util.Log;
 
 
 public class ContactBox extends View {
 	
 	private Bitmap rounded_contact_bitmap;
 	
 	private int width = 300;
 	private int height = 300;
 	
 	private int border_radius = 20;
 	private int shadow_radius = 10;
 	
 	private boolean state_pressed = false;
 	
 	public ContactBox(Context context){
 
 		super(context);
 		initializeBitmap();
 
 	}
 
 	
 
 	public ContactBox(Context context, AttributeSet attr){
 
 		super(context,attr);
 		initializeBitmap();
 
 	}
 
 	
 	public ContactBox(Context context, AttributeSet attr, int defaultStyles){
 
 		super(context, attr, defaultStyles);
 		initializeBitmap();
 
 	}
 	
 	private void initializeBitmap()
 	{
 		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.nocontact);
 		
 		int img_width = bitmap.getWidth();
 		int img_height = bitmap.getHeight();
 		
 		rounded_contact_bitmap = Bitmap.createBitmap(width-shadow_radius,height-shadow_radius,Bitmap.Config.ARGB_8888);
 		
 		Canvas init_canvas = new Canvas(rounded_contact_bitmap);
 		
 		Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		
 		maskPaint.setColor(Color.RED);
 		
 		init_canvas.drawRoundRect(new RectF(shadow_radius+1,shadow_radius+1,width-1-shadow_radius,height-1-shadow_radius), border_radius, border_radius , maskPaint);
 		
 		maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
 		
 		init_canvas.drawBitmap(bitmap,new Rect(0,0,img_width,img_height),new Rect(shadow_radius+1,shadow_radius+1,width-1-shadow_radius,height-1-shadow_radius),maskPaint);		
 		
 	}
 	@Override
 	protected void onMeasure(int width,int height)
 	{
 		int measuredWidth = MeasureSpec.getSize(width);
 
 		int measuredHeight = MeasureSpec.getSize(height);
 		
 		
 		
 		setMeasuredDimension( measuredWidth, measuredHeight);
 		
 		width = getMeasuredWidth();
 		height = getMeasuredHeight();
 		
 		initializeBitmap();
 	}
 	@Override
 	protected void onDraw(Canvas canvas)
 	{
 		Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		
 		
 		
 		shadowPaint.setShadowLayer(shadow_radius,0,0,Color.BLACK);
		canvas.drawRoundRect(new RectF(shadow_radius,shadow_radius,width-shadow_radius,height-shadow_radius), 10, 20, shadowPaint);
 		
 		Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		borderPaint.setColor(Color.GRAY);
 		
 		if (state_pressed)
 		{
 			borderPaint.setColor(Color.BLACK);
 			
 		}
 		
		canvas.drawRoundRect(new RectF(shadow_radius,shadow_radius,width-shadow_radius,height-shadow_radius), 20, 20, borderPaint);
 		
 		Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		
 		if (rounded_contact_bitmap != null)
 		{
 			canvas.drawBitmap(rounded_contact_bitmap,0,0,bitmapPaint);
 		}
 		if (state_pressed)
 		{
 			Paint darkerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 			darkerPaint.setColor(Color.BLACK);
 			darkerPaint.setAlpha(100);
 			
			canvas.drawRoundRect(new RectF(shadow_radius,shadow_radius,width-shadow_radius,height-shadow_radius), 20, 20, darkerPaint);
 			
 		}
 		
 	}	
 	@Override
 	protected void onSizeChanged(int w,int h,int oldw,int oldh)
 	{
 		width = w;
 		height = h;
 		initializeBitmap();
 	}
 	@Override
 	public boolean onTouchEvent(MotionEvent touch_event)
 	{
 		switch (touch_event.getAction())
 		{
 		case MotionEvent.ACTION_DOWN:
 			state_pressed = true;
 			// Invalidate tut alles redrawen
 			invalidate();
 			return true;
 		case MotionEvent.ACTION_UP:
 			state_pressed = false;
 			invalidate();
 			callOnClick();
 			return true;
 		
 		}
 		
 		
 		return false;
 	}
 	
 }
 	
 
