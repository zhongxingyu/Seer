 package ntu.csie.wcmlab.canvasnetcore;
 
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.PorterDuff;
 import android.graphics.PorterDuffXfermode;
 
 public class ClientDrawState {
 	private Path mPath;
 	private Paint mPaint,mCurrentPaint;
 	static private Paint mEraser;
 	public float mX,mY;
 	
 	public ClientDrawState()
 	{
 		mPaint = new Paint();
 		mPaint.setAntiAlias(true);
 		mPaint.setDither(true);
 		mPaint.setColor(0xFFFF0000);
 		mPaint.setStyle(Paint.Style.STROKE);
 		mPaint.setStrokeJoin(Paint.Join.ROUND);
 		mPaint.setStrokeCap(Paint.Cap.ROUND);
 		mPaint.setStrokeWidth(12);
 		
 		mEraser = new Paint();
 		mEraser.setAntiAlias(true);
 		mEraser.setDither(true);
		//mEraser.setARGB(0, 0, 0, 0);
		mEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
 		mEraser.setStyle(Paint.Style.STROKE);
 		mEraser.setStrokeJoin(Paint.Join.ROUND);
 		mEraser.setStrokeCap(Paint.Cap.ROUND);
 		mEraser.setStrokeWidth(15);
 		
 		mCurrentPaint = mPaint;
 		
 		mPath = new Path();
 	}
 	
 	
     public Path getPath()
     {
     	return mPath;
     }
 	
     
     public Paint getPaint()
     {
     	return mCurrentPaint;
     		
     }
 
     public void useEraser()
     {
     	mCurrentPaint = mEraser;
     }
     
     public void stopUseEraser()
     {
     	mCurrentPaint = mPaint;
     }
 
 }
