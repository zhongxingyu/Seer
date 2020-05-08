 package com.hfk.yatv;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.hfk.yatv.R;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Toast;
 
 public class TouchVisualizerMultiTouchGraphicView extends View implements View.OnLongClickListener, View.OnClickListener {
 	
     public TouchVisualizerMultiTouchGraphicView(Context context) {
         super(context);
         
         eventDataMap = new HashMap<Integer, EventData>();
         
         this.setOnLongClickListener(this);
         this.setOnClickListener(this);
         
     }
  
     @Override
     public void onDraw(Canvas canvas) {
     	for(EventData event : eventDataMap.values())
     	{
             paint.setColor(Color.WHITE);
             paint.setStyle(Paint.Style.FILL);
             canvas.drawCircle(event.x, event.y, getScreenSize(touchCircleRadius), paint);
             paint.setStyle(Paint.Style.STROKE);
             if(event.pressure <= 0.001)
             {
             	paint.setColor(Color.RED);
             }
             canvas.drawCircle(event.x, event.y, getScreenSize(touchCircleRadius + pressureRingOffset + (pressureRingOffset * event.pressure)), paint);
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
 
     	int action = event.getActionMasked();
   	
     	int pointerIndex = event.getActionIndex();
     	int pointerId = event.getPointerId(pointerIndex);
 
    	boolean result = true;
 		switch (action) {
     	case MotionEvent.ACTION_DOWN:
     	case MotionEvent.ACTION_POINTER_DOWN:
     		EventData eventData = new EventData();
     		eventData.x = event.getX(pointerIndex);
     		eventData.y = event.getY(pointerIndex);
     		eventData.pressure = event.getPressure(pointerIndex) * pressureAmplification;
     		eventDataMap.put(new Integer(pointerId), eventData);
     		if (returnValueOnActionDown)
     		{
     			result = returnValueOnActionDown;
     		}
     		break;
     	case MotionEvent.ACTION_MOVE:
     		for(int i = 0; i < event.getPointerCount(); i++)
     		{
     			int curPointerId = event.getPointerId(i);
 	    		if(eventDataMap.containsKey(new Integer(curPointerId)))
 	    		{
 	        		EventData moveEventData = eventDataMap.get(new Integer(curPointerId));
 	        		moveEventData.x = event.getX(i);
 	        		moveEventData.y = event.getY(i);
 	        		moveEventData.pressure = event.getPressure(i) * pressureAmplification;
 	    		}
 			}
     		if (returnValueOnActionMove)
     		{
     			result = returnValueOnActionMove;
     		}
     		break;
     	case MotionEvent.ACTION_UP:
     	case MotionEvent.ACTION_POINTER_UP:
     		eventDataMap.remove(new Integer(pointerId));
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
 		Toast msg = Toast.makeText(TouchVisualizerMultiTouchGraphicView.this.getContext(), "onClick", Toast.LENGTH_SHORT);
 		msg.setGravity(Gravity.CENTER, msg.getXOffset() / 2, msg.getYOffset() / 2);
 		msg.show();		
 	}
 
 	@Override
 	public boolean onLongClick(View v) {
 		Toast msg = Toast.makeText(TouchVisualizerMultiTouchGraphicView.this.getContext(), "onLongClick", Toast.LENGTH_SHORT);
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
     
     private boolean callBaseClass = true;
     private boolean handleOnTouchEvent = true;
     private boolean returnValueOnActionDown = true;
     private boolean returnValueOnActionMove = true;
     private boolean returnValueOnActionUp = true;
     private boolean returnValueOnLongClick = false;
     
     private Map<Integer, EventData> eventDataMap; 
     
     private class EventData{
     	public float x;
     	public float y;
     	public float pressure;
     }
 }
