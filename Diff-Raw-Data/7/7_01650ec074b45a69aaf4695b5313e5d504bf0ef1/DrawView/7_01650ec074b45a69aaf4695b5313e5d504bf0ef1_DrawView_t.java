 package com.jsanc623.shabo.shell;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.view.MotionEvent;
 import android.view.View;
 //import android.widget.Toast;
 
 @SuppressLint("DrawAllocation")
 public class DrawView extends View {
 	private Bitmap  mBitmap;
 	private Canvas  mCanvas;
 	private Path    mPath;
 	private Paint   mBitmapPaint;
 	private Paint   mPaint;
 	private MenuActivity MenuActivity = new MenuActivity();
 	
 	public DrawView(Context c) {
 	    super(c);
 	
 	    mPath = new Path();
 	    mBitmapPaint = new Paint(Paint.DITHER_FLAG);
 	
 	    mPaint = new Paint();
 	    mPaint.setAntiAlias(true);
 	    mPaint.setDither(true);
 	    mPaint.setColor(0xFF000000);
 	    mPaint.setStyle(Paint.Style.STROKE);
 	    mPaint.setStrokeJoin(Paint.Join.ROUND);
 	    mPaint.setStrokeCap(Paint.Cap.ROUND);
 	    mPaint.setStrokeWidth(3);
 	}
 
 	@Override
 	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 	    super.onSizeChanged(w, h, oldw, oldh);
 	    mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
 	    mCanvas = new Canvas(mBitmap);
         //MenuActivity.saveImage(mBitmap, "drawing_");
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
 	    canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
 	    
 	    canvas.drawPath(mPath, mPaint);
 	}
 	
 	private float mX, mY;
 	private static final float TOUCH_TOLERANCE = 2;
 	
 	private void touch_start(float x, float y) {
 	    mPath.reset();
 	    mPath.moveTo(x, y);
 	    mX = x;
 	    mY = y;
 	}
 	private void touch_move(float x, float y) {
 	    float dx = Math.abs(x - mX);
 	    float dy = Math.abs(y - mY);
 	    if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
 	        mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
 	        mX = x;
 	        mY = y;
 	    }
 	}
 	private void touch_up() {
 	    mPath.lineTo(mX, mY);
 	    // commit the path to our offscreen
 	    mCanvas.drawPath(mPath, mPaint);
 	    // kill this so we don't double draw
 	    mPath.reset();
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 	    float x = event.getX();
 	    float y = event.getY();
 	
 	    switch (event.getAction()) {
 	        case MotionEvent.ACTION_DOWN:
 	            touch_start(x, y);
 	            invalidate();
 	            break;
 	        case MotionEvent.ACTION_MOVE:
 	            touch_move(x, y);
 	            invalidate();
 	            break;
 	        case MotionEvent.ACTION_UP:
 	            touch_up();
           	 Bitmap bitmap;
           	 View v1 = this.getRootView();
           	 v1.setDrawingCacheEnabled(true);
           	 bitmap = Bitmap.createBitmap(v1.getDrawingCache());
           	 v1.setDrawingCacheEnabled(false);
           	 MenuActivity.saveImage(bitmap, "drawing-", "Updated: ", true);
 	            invalidate();
 	            break;
 	    }
 	    return true;
 	}
 	
 	public void clear(){
 	    mBitmap.eraseColor(Color.TRANSPARENT);
 	    invalidate();
 	    System.gc();
 	}
 }
