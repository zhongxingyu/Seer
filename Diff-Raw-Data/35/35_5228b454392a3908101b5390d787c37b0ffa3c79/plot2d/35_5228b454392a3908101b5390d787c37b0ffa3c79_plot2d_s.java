 package com.android.graphbutton;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.view.View;
 
 //This class was implemented by Ankit Srivastava. Please consider acknowledging the author if it ends up being useful to you.
 
 public class plot2d extends View {
 
 	private Paint paint;
 	private float[] xvalues,yvalues;
 	private float maxx,maxy,minx,miny,locxAxis,locyAxis;
 	private int vectorLength;
 	private int axes = 1;
 	
 	int circPosX = 0;
 	int circPosY = 0;
 	
 	public plot2d(Context context, float[] xvalues, float[] yvalues, int axes) {
 		super(context);
 		this.xvalues=xvalues;
 		this.yvalues=yvalues;
 		this.axes=axes;
 		vectorLength = xvalues.length;
 		paint = new Paint();
 
 		getAxes(xvalues, yvalues);
 		
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		
		float canvasHeight = getHeight();
		float canvasWidth = getWidth();
		int[] xvaluesInPixels = toPixel(canvasWidth, minx, maxx, xvalues); 
		int[] yvaluesInPixels = toPixel(canvasHeight, miny, maxy, yvalues);
 		int locxAxisInPixels = toPixelInt(canvasHeight, miny, maxy, locxAxis);
 		int locyAxisInPixels = toPixelInt(canvasWidth, minx, maxx, locyAxis);
 		String xAxis = "x-axis";
 		String yAxis = "y-axis";
 
 		paint.setStrokeWidth(2);
 		canvas.drawARGB(255, 255, 255, 255);
 		for (int i = 0; i < vectorLength-1; i++) {
 			paint.setColor(Color.RED);
 			canvas.drawLine(xvaluesInPixels[i],canvasHeight-yvaluesInPixels[i],xvaluesInPixels[i+1],canvasHeight-yvaluesInPixels[i+1],paint);
 		}
 		
 		paint.setColor(Color.BLACK);
 		canvas.drawLine(0,canvasHeight-locxAxisInPixels,canvasWidth,canvasHeight-locxAxisInPixels,paint);
 		canvas.drawLine(locyAxisInPixels,0,locyAxisInPixels,canvasHeight,paint);
 		
 		//Automatic axes markings, modify n to control the number of axes labels
 		if (axes!=0){
 			float temp = 0.0f;
 			int n=3;
 			paint.setTextAlign(Paint.Align.CENTER);
 			paint.setTextSize(20.0f);
 			for (int i=1;i<=n;i++){
 				temp = Math.round(10*(minx+(i-1)*(maxx-minx)/n))/10;
 				canvas.drawText(""+temp, toPixelInt(canvasWidth, minx, maxx, temp),canvasHeight-locxAxisInPixels+20, paint);
 				temp = Math.round(10*(miny+(i-1)*(maxy-miny)/n))/10;
 				canvas.drawText(""+temp, locyAxisInPixels+20,canvasHeight-toPixelInt(canvasHeight, miny, maxy, temp), paint);
 			}
 			canvas.drawText(""+maxx, toPixelInt(canvasWidth, minx, maxx, maxx),canvasHeight-locxAxisInPixels+20, paint);
 			canvas.drawText(""+maxy, locyAxisInPixels+20,canvasHeight-toPixelInt(canvasHeight, miny, maxy, maxy), paint);
 			//canvas.drawText(xAxis, canvasWidth/2,canvasHeight-locxAxisInPixels+45, paint);
 			//canvas.drawText(yAxis, locyAxisInPixels-40,canvasHeight/2, paint);
 		}
 		paint.setColor(Color.GREEN);
 		//0 in the y direction is the bottom of the screen in normal landscape mode.
 
		canvas.drawCircle(xvaluesInPixels[circPosX], canvasHeight-yvaluesInPixels[circPosY]-8, 8, paint); //moves circle along the graph
 
 	}
 	
 	private int[] toPixel(float pixels, float min, float max, float[] value) {
 		
 		double[] p = new double[value.length];
 		int[] pint = new int[value.length];
 		
 		for (int i = 0; i < value.length; i++) {
 			p[i] = .1*pixels+((value[i]-min)/(max-min))*.8*pixels;
 			pint[i] = (int)p[i];
 		}
 		
 		return (pint);
 	}
 	
 	private void getAxes(float[] xvalues, float[] yvalues) {
 		
 		minx=getMin(xvalues);
 		miny=getMin(yvalues);
 		maxx = getMax(xvalues);
 		maxy=getMax(yvalues);
 		
 		if (minx>=0)
 			locyAxis=minx;
 		else if (minx<0 && maxx>=0)
 			locyAxis=0;
 		else
 			locyAxis=maxx;
 		
 		if (miny>=0)
 			locxAxis=miny;
 		else if (miny<0 && maxy>=0)
 			locxAxis=0;
 		else
 			locxAxis=maxy;
 		
 	}
 	
 	private int toPixelInt(float pixels, float min, float max, float value) {
 		
 		double p;
 		int pint;
 		p = .1*pixels+((value-min)/(max-min))*.8*pixels;
 		pint = (int)p;
 		return (pint);
 	}
 
 	private float getMax(float[] v) {
 		float largest = v[0];
 		for (int i = 0; i < v.length; i++)
 			if (v[i] > largest)
 				largest = v[i];
 		return largest;
 	}
 
 	private float getMin(float[] v) {
 		float smallest = v[0];
 		for (int i = 0; i < v.length; i++)
 			if (v[i] < smallest)
 				smallest = v[i];
 		return smallest;
 	}
 
 	public plot2d translateCirc(int i, int j)
 	{
 		circPosX+= i;
 		circPosY+= j;
 		return this;
 	}
 	
 	/**
 	 * Jumps the circle 10 x-values ahead on its graph.
 	 * @return
 	 */
 	public plot2d advanceCirc()
 	{
		circPosX+= 10;
		circPosY+= 10;
 		return this;
 	}
 }
