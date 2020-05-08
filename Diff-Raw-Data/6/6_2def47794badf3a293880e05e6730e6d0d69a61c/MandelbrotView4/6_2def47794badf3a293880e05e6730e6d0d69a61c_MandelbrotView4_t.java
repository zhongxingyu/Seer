 package com.jooyunghan.mandelbrot;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.Canvas;
 import android.renderscript.Allocation;
 import android.renderscript.Double2;
 import android.renderscript.RenderScript;
 import android.util.AttributeSet;
 import android.view.View;
 
 @SuppressLint("DrawAllocation")
 public class MandelbrotView4 extends View {
 	private RenderScript mRS;
 	private ScriptC_mandelbrot mScript;
 	private int width;
 	private int height;
 	private double scale;
 	private double dx_begin;
 	private double dy_begin;
 
 	public MandelbrotView4(Context context) {
 		this(context, null);
 	}
 	
 	public MandelbrotView4(Context context, AttributeSet attr) {
 		super(context, attr);
 		createScript();
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		
 		width = canvas.getWidth();
 		height = canvas.getHeight();
 		setScale();
 		
 		Bitmap b = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(),
 				Config.ARGB_8888);
 		Allocation out = Allocation.createFromBitmap(mRS, b);
 		mScript.set_d_begin(new Double2(dx_begin, dy_begin));
 		mScript.set_i_begin(new Double2(0, 0));
 		mScript.set_scale(scale);
 		mScript.forEach_root(out);
 		out.copyTo(b);
 		canvas.drawBitmap(b, 0, 0, null);
 	}
 
 	private void setScale() {
 		double scaleX = (width - 1) / 3.0;
 		double scaleY = (height - 1) / 2.0;
 		if (scaleX < scaleY) {
 			scale = scaleX;
 			dx_begin = -2.0;
 			dy_begin = -(height / 2) / scale;
 		} else {
 			scale = scaleY;
 			dx_begin = -2 - ((width - (int) (3 * scale)) / 2) / scale;
 			dy_begin = -1;
 		}
 	}
 	
 	private void createScript() {
 		mRS = RenderScript.create(getContext());
 		mScript = new ScriptC_mandelbrot(mRS, getResources(), R.raw.mandelbrot);
 	}
	
	@Override
	protected void onDetachedFromWindow() {
		mRS.destroy();
		super.onDetachedFromWindow();
	}
 
 }
