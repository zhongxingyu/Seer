 package com.hekai.camera;
 
 import java.util.Arrays;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.util.Log;
 import android.view.SurfaceView;
 
 public class HistogramView extends SurfaceView{
 	
 	private static final String TAG="HistogramView";
 	
 	private Paint whitePaint;
 	
 	private int centerX=0,centerY=0;
 	
 	private int[] drawBuffer,cacheBuffer;
 
 	public HistogramView(Context context) {
 		super(context);
 		
 		setWillNotDraw(false);		
 		
 		init();
 	}
 	
 	private void init(){
 		whitePaint=new Paint();
 		whitePaint.setColor(Color.WHITE);
 		whitePaint.setStyle(Style.STROKE);
 		
 		cacheBuffer=new int[256];
 		drawBuffer=new int[256];
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
 		Log.d(TAG, "onDraw");
 		super.onDraw(canvas);
 		
 		canvas.drawRect(centerX-129, centerY-128, centerX+128, centerY+127, whitePaint);
 		
 		int maxValue=getMaxValue(drawBuffer);
 		
 		float ratio=(float)maxValue/255;
 		
 		for(int i=0;i<drawBuffer.length;i++){
 			if(drawBuffer[i]>0){
 				int len=(int) (drawBuffer[i]/ratio);
 				canvas.drawLine(centerX-129+(i+1), centerY+127, centerX-129+(i+1), centerY+127-len, whitePaint);
 			}
 		}
 	}
 	
 	public void updateData(byte[] data,int width,int height,int format){
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				int y=(0xff & (int)(data[j*width+i]));
 				cacheBuffer[y]++;
 			}
 		}
 		System.arraycopy(cacheBuffer, 0, drawBuffer, 0, cacheBuffer.length);
 	}
 	
 	private int getMaxValue(int[] array){
 		int max=0;
 		int index=0;
 		for(int i=0;i<array.length;i++){
 			if(array[i]>max){
 				max=array[i];
 				index=i;
 			}
 		}
 		Log.d(TAG,"max="+max+",index="+index);
 		return max;
 	}
 	
 	public void updatePosition(int centerX,int centerY){
 		this.centerX=centerX;
 		this.centerY=centerY;
 	}
 }
