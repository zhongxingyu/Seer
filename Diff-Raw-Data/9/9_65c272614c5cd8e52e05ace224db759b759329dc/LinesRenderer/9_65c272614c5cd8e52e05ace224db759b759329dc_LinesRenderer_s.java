 package fi.dy.esav.lines;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.PointF;
 import android.util.Config;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class LinesRenderer extends SurfaceView implements Runnable {
 
 	Thread renderThread = null;
 	SurfaceHolder holder;
 	volatile boolean running;
 	
 	public LinesRenderer(Context context) {
 		super(context);
 		holder = this.getHolder();
 	}
 	
 	public void resume() {
 		running = true;
 		renderThread = new Thread(this);
 		renderThread.start();
 	}
 	
 	public void pause() {
 		running = false;
 		while(true) {
 			try {
 				renderThread.join();
 				return;
 			} catch(InterruptedException e) {
 				// retry
 			}
 		}
 	}
 	
 	public void run() {
		if(getWidth() == 0 || getHeight() == 0) return;
 		
 		long startTime;
 
 		PointF center = new PointF(this.getWidth()/2, this.getHeight()/2);
 		float freeSpace = Math.min(this.getWidth(), this.getHeight())/2;
 		
 		Line line1 = new Line(center, freeSpace / 2, 0, 0.01);
 		freeSpace = freeSpace / 2;
 		Line line2 = new Line(line1.end, freeSpace / 2, 0, 0.05);
 		//freeSpace = freeSpace / 2;
 		
 		Paint paint = new Paint();
 		paint.setColor(Color.BLUE);
 		
 		Bitmap legend = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
 		legend.eraseColor(Color.WHITE);
 
 		while(running) {
 			startTime = System.currentTimeMillis();
 			
 			if(!holder.getSurface().isValid()) continue;
 			Canvas canvas = holder.lockCanvas();
 			
 			//canvas.drawRGB(255, 255, 255);
 			canvas.drawBitmap(legend, 0, 0, null);
 			canvas.drawLines(line1.getPoints(), paint);
 			canvas.drawLines(line2.getPoints(), paint);
 			
 			legend.setPixel((int)line2.end.x, (int)line2.end.y, Color.BLACK);
 			
 			line1.incrementAngle();
 			line2.incrementAngle();
 		    
 			holder.unlockCanvasAndPost(canvas);
 			delay(startTime);
 		}
 	}
 	
 	private static void delay(long start) {
 		long end = System.currentTimeMillis();
 	    long dt = end - start;
 	    if (dt < 20) {
 	    	while(true) {
 				try {
 					Thread.sleep(20 - dt);
 					return;
 				} catch (InterruptedException e) {
 					
 				}
 	    	}
 	    }
 	}
 }
