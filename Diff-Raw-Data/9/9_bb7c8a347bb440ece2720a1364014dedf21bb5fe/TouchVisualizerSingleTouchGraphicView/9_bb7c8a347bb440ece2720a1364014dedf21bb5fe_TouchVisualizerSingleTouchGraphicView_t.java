 package com.hfk.yatv;
 
 import com.hfk.yatv.R;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.os.Bundle;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Toast;
 
 public class TouchVisualizerSingleTouchGraphicView extends View implements View.OnLongClickListener, View.OnClickListener {
 	
     public TouchVisualizerSingleTouchGraphicView(Context context) {
         super(context);
         
         this.setOnLongClickListener(this);
         this.setOnClickListener(this);
         
         paint.setColor(Color.WHITE);
     }
  
     @Override
     public void onDraw(Canvas canvas) {
     	if(downX > 0)
     	{
             paint.setStyle(Paint.Style.FILL);
             canvas.drawCircle(downX, downY, getScreenSize(touchCircleRadius), paint);
             paint.setStyle(Paint.Style.STROKE);
             canvas.drawCircle(downX, downY, getScreenSize(touchCircleRadius + pressureRingOffset + (pressureRingOffset * pressure)), paint);
     	}
 
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
     	if(callBaseClass)
     	{
     		super.onTouchEvent(event);
     	}
     	
     	if(!handleOnTouchEvent)
     	{
     		return false;
     	}
 
     	int action = event.getAction();
     	pressure = event.getPressure() * pressureAmplification;   	
 
    	boolean result = false;
 		switch (action) {
     	case MotionEvent.ACTION_DOWN:
     		downX = event.getX();
     		downY = event.getY();
     		if (returnValueOnActionDown)
     		{
     			result = returnValueOnActionDown;
     		}
     		break;
     	case MotionEvent.ACTION_MOVE:
     		downX = event.getX();
     		downY = event.getY();
     		if (returnValueOnActionMove)
     		{
     			result = returnValueOnActionMove;
     		}
     		break;
     	case MotionEvent.ACTION_UP:
     		downX = -1;
     		downY = -1;
     		if (returnValueOnActionUp)
     		{
     			result = returnValueOnActionUp;
     		}
     		break;
     	case MotionEvent.ACTION_OUTSIDE:
     		break;
     	}
     	invalidate();
     	return result;
     }
 
 	@Override
 	public void onClick(View v) {
 		Toast msg = Toast.makeText(TouchVisualizerSingleTouchGraphicView.this.getContext(), "onClick", Toast.LENGTH_SHORT);
 		msg.setGravity(Gravity.CENTER, msg.getXOffset() / 2, msg.getYOffset() / 2);
 		msg.show();		
 	}
 
 	@Override
 	public boolean onLongClick(View v) {
 		Toast msg = Toast.makeText(TouchVisualizerSingleTouchGraphicView.this.getContext(), "onLongClick", Toast.LENGTH_SHORT);
 		msg.setGravity(Gravity.CENTER, msg.getXOffset() / 2, msg.getYOffset() / 2);
 		msg.show();		
 		return returnValueOnLongClick;
 	}
 	
 	public float getScreenSize(float lengthInMm)
 	{
     	//Size_in_mm = Size_in_inches * 25.4;
     	//Size_in_inches = Size_in_mm / 25.4;
     	//Size_in_dp = Size_in_inches * 160;
     	//Size_in_dp = (Size_in_mm / 25.4) * 160;
     	//Size_in_inches = Size_in_dp / 160;    
     	return TypedValue.applyDimension(
     			TypedValue.COMPLEX_UNIT_DIP, 
     			(float)(lengthInMm * 160 / 25.4), 
     			getResources().getDisplayMetrics());
 	}
 	
 	public void setCallBaseClass(boolean process)
 	{
 		callBaseClass = process;
 	}
 	
 	public boolean getCallBaseClass()
 	{
 		return callBaseClass;
 	}
 	
 	public void setHandleTouchEvent(boolean process)
 	{
 		handleOnTouchEvent = process;
 	}
 	
 	public boolean getHandleTouchEvent()
 	{
 		return handleOnTouchEvent;
 	}
 	
 	public void setReturnValueOnActionDown(boolean value)
 	{
 		returnValueOnActionDown = value;
 	}
 	
 	public boolean getReturnValueOnActionDown()
 	{
 		return returnValueOnActionDown;
 	}
 	
 	public void setReturnValueOnActionMove(boolean value)
 	{
 		returnValueOnActionMove = value;
 	}
 	
 	public boolean getReturnValueOnActionMove()
 	{
 		return returnValueOnActionMove;
 	}
 	
 	public void setReturnValueOnActionUp(boolean value)
 	{
 		returnValueOnActionUp = value;
 	}
 	
 	public boolean getReturnValueOnActionUp()
 	{
 		return returnValueOnActionUp;
 	}
 	
 	public void setPressureAmplification(float value)
 	{
 		pressureAmplification = value;
 	}
 	
 	public float getPressureAmplification()
 	{
 		return pressureAmplification;
 	}
 	
 	public void setReturnValueOnLongClick(boolean value)
 	{
 		returnValueOnLongClick = value;
 	}
 	
 	public boolean getReturnValueOnLongClick()
 	{
 		return returnValueOnLongClick;
 	}
    
     private float touchCircleRadius = (float) DefaultValues.TouchCircleRadius;
     private float pressureRingOffset = (float) DefaultValues.PressureRingOffset;
     private float pressureAmplification = (float) DefaultValues.PressureAmplificaton;
     
     private Paint paint = new Paint();
 
     private float downX = -1;
     private float downY = -1;
     private float pressure = 1;
     
     private boolean callBaseClass = true;
     private boolean handleOnTouchEvent = true;
     private boolean returnValueOnActionDown = true;
     private boolean returnValueOnActionMove = true;
     private boolean returnValueOnActionUp = true;
     private boolean returnValueOnLongClick = false;
 }
