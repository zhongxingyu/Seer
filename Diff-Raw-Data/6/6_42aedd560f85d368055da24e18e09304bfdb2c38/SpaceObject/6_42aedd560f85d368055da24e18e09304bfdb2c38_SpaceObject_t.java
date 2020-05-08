 package com.marcouberti.gravity.bean;
 
 import android.content.Context;
 import android.graphics.BlurMaskFilter;
 import android.graphics.BlurMaskFilter.Blur;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.ColorFilter;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Rect;
 
 public class SpaceObject extends UniverseObject{
 	Rect destRect;
 	Path path;
 	int color = Color.WHITE;
 	Paint centerPaint;
 	
 	public SpaceObject(Context context, int shape) {
 		paint = new Paint();//Paint.ANTI_ALIAS_FLAG
 		paint.setColor(color);
 		paint.setStyle(Paint.Style.FILL);
 		//paint.setAlpha(180);
 		//paint.setStrokeWidth(2);
 		paint.setMaskFilter(new BlurMaskFilter(3, Blur.NORMAL));
 		this.shape = shape;
 		rotation = (float)(Math.random()*180);
 		path = new Path();
 		
 		centerPaint = new Paint();
 		centerPaint.setStyle(Paint.Style.FILL);
 		centerPaint.setColor(color);
 	}
 
 	public void setColor(int color) {
 		this.color = color;
 		paint.setColor(color);
 		centerPaint.setColor(color);
 	}
 	
 	@Override
 	public void draw(Canvas canvas) {
 		// rotate the canvas on center of the text to draw
 		//canvas.save();
 		//canvas.rotate(rotation, x, y);
 		switch (shape) {
 		case 1:
 			//1) CROSS
 			canvas.drawLine(x-radius/2, y-radius/2, x-radius/2, y-radius, paint);
 			canvas.drawLine(x-radius/2, y-radius, x+radius/2, y-radius, paint);
 			canvas.drawLine(x+radius/2, y-radius, x+radius/2, y-radius/2, paint);
 			
 			canvas.drawLine(x+radius/2, y-radius/2, x+radius, y-radius/2, paint);
 			canvas.drawLine(x+radius, y-radius/2, x+radius, y+radius/2, paint);
 			canvas.drawLine(x+radius, y+radius/2, x+radius/2, y+radius/2, paint);
 			
 			canvas.drawLine(x+radius/2, y+radius/2, x+radius/2, y+radius, paint);
 			canvas.drawLine(x+radius/2, y+radius, x-radius/2, y+radius, paint);
 			canvas.drawLine(x-radius/2, y+radius, x-radius/2, y+radius/2, paint);
 			
 			canvas.drawLine(x-radius/2, y+radius/2, x-radius, y+radius/2, paint);
 			canvas.drawLine(x-radius, y+radius/2, x-radius, y-radius/2, paint);
 			canvas.drawLine(x-radius, y-radius/2, x-radius/2, y-radius/2, paint);
 			break;
 		case 2:
 			//3) TRIANCLE
 			canvas.drawLine(x-radius, y+radius, x, y-radius, paint);
 			canvas.drawLine(x, y-radius, x+radius, y+radius, paint);
 			canvas.drawLine(x+radius, y+radius, x-radius, y+radius, paint);
 			break;
 		case 3:
 			//4) RING
 			canvas.drawCircle(x, y, radius, paint);
 			canvas.drawCircle(x, y, radius*1.4f, paint);
 			break;
 		case 4:
 			//4) POINT
 			canvas.drawCircle(x, y, radius, paint);
 			canvas.drawCircle(x, y, radius, centerPaint);
 			break;
 		default:
 			//0) POINTER
			/*
 			canvas.drawCircle(x, y, radius, paint);
 			canvas.drawLine(x-radius, y, x, y-(radius*2), paint);
 			canvas.drawLine(x+radius, y, x, y-(radius*2), paint);
			*/
			//4) POINT
			canvas.drawCircle(x, y, radius, paint);
			canvas.drawCircle(x, y, radius, centerPaint);
 			break;
 		}
 
 		//canvas.restore();
 	}
 
 	@Override
 	public int getOpacity() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	
 
 	@Override
 	public void setColorFilter(ColorFilter cf) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void setAlpha(int alpha) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 }
