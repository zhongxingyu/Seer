 package com.brentpanther.borderedviews;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Path;
 import android.graphics.Path.Direction;
 import android.graphics.RectF;
 import android.util.AttributeSet;
 import android.util.TypedValue;
 import android.view.View;
 
 public class Borders {
 	
 	private int borderWidth;
 	private int borderColor;
 	private int border;
 	private float[] radii;
 	private int background;
 	private Paint paint;
 	private boolean paddingCalculated = false;
 	
 	public Borders() {
 		paint = new Paint();
 	}
 	
 	public Borders(View view, Context context, AttributeSet attrs) {
 		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Borders, 0, 0);
 		radii = new float[4];
 		try {
 			background = Color.TRANSPARENT;
 			if(a.hasValue(R.styleable.Borders_android_background)) {
 				TypedValue v = a.peekValue(R.styleable.Borders_android_background);
 				if(v.type == TypedValue.TYPE_INT_COLOR_ARGB4 || v.type == TypedValue.TYPE_INT_COLOR_ARGB8
 					|| v.type == TypedValue.TYPE_INT_COLOR_RGB4 || v.type == TypedValue.TYPE_INT_COLOR_RGB8) {
 					background = a.getColor(R.styleable.Borders_android_background, -1);
 					view.setBackgroundColor(Color.TRANSPARENT);				
 				} 
 			}
 			if(a.hasValue(R.styleable.Borders_border_radius)) {
 				int radius = a.getInt(R.styleable.Borders_border_radius, 0);
 				radii = new float[] {radius, radius, radius, radius};
 			} else {
 				radii[0] = a.getInt(R.styleable.Borders_radiusTopLeft, 0);
 				radii[1] = a.getInt(R.styleable.Borders_radiusTopRight, 0);
 				radii[2] = a.getInt(R.styleable.Borders_radiusBottomRight, 0);
 				radii[3] = a.getInt(R.styleable.Borders_radiusBottomLeft, 0);
 			}
 			borderWidth = a.getDimensionPixelSize(R.styleable.Borders_borderWidth, 1);
 			borderColor = a.getColor(R.styleable.Borders_borderColor, Color.BLACK);
 			border = a.getInteger(R.styleable.Borders_borders, 15);
 			
 			paint = new Paint();
 		} finally {
 			a.recycle();
 		}
 	}
 	
 	public void onViewDraw(View view, Canvas canvas) {
 		if(!paddingCalculated) {
 			view.setPadding(view.getPaddingLeft() + borderWidth, view.getPaddingTop() + borderWidth,
 				view.getPaddingRight() + borderWidth, view.getPaddingBottom() + borderWidth);
 			paddingCalculated = true;
 		} else {
 			int width = view.getMeasuredWidth();
 			int height = view.getMeasuredHeight();
 			int d = borderWidth / 2;
 			Path path = new Path();
 			paint.setStrokeWidth(borderWidth);
 			paint.setColor(background);
 			paint.setStyle(Style.FILL);
 			paint.setAntiAlias(true);
 			float[] r = new float[]{radii[0], radii[0], radii[1], radii[1], radii[2], radii[2], radii[3], radii[3]};
 			RectF rectangle = new RectF(0+d, 0+d, width-d, height-d);
			if((border & 1) != 1) rectangle.top -= 2*d;
			if((border & 2) != 2) rectangle.bottom += 2*d;
			if((border & 4) != 4) rectangle.left -= 2*d;
			if((border & 8) != 8) rectangle.right += 2*d;
 			path.addRoundRect(rectangle, r, Direction.CW);
 			canvas.drawPath(path, paint);
 			paint.setColor(borderColor);
 			paint.setStyle(Paint.Style.STROKE);
 			path = new Path();
 			path.addRoundRect(rectangle, r, Direction.CW);
 			canvas.drawPath(path, paint);
 		}
 	}
 	
 	public void setBackgroundColor(int background) {
 		this.background = background;
 	}
 	
 	public int getBackgroundColor() {
 		return this.background;
 	}
 	
 	public void setBorders(boolean left, boolean top, boolean right, boolean bottom) {
 		int b = 0;
 		if(left) b += 4;
 		if(top) b += 1;
 		if(right) b+= 8;
 		if(bottom) b+= 2;
 		border = b;
 	}
 	
 	public void setBorderColor(int borderColor) {
 		this.borderColor = borderColor;
 	}
 	
 	public int getBorderColor() {
 		return borderColor;
 	}
 	
 	public void setBorderWidth(int borderWidth) {
 		this.borderWidth = borderWidth;
 	}
 	
 	public int getBorderWidth() {
 		return borderWidth;
 	}
 	
 	public void setRadii(float topLeft, float topRight, float bottomRight, float bottomLeft) {
 		radii = new float[] {topLeft, topRight, bottomRight, bottomLeft};
 	}
 	
 	public float[] getRadii() {
 		return radii;
 	}
 
 }
