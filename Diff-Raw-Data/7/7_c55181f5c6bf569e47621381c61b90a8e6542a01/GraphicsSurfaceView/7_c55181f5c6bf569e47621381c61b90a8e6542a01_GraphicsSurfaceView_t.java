 package com.JS.musictranscribe;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.drawable.Drawable;
 import android.os.Handler;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class GraphicsSurfaceView extends SurfaceView implements SurfaceHolder.Callback{
 
 	private static final String TAG = "Graphics_Surface";
 	
 	private SurfaceHolder sh;
 	private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 	private GraphicsThread thread;
 	private Context ctx;
 	private Bitmap note1;
 	private boolean notemove = false;
 	
 	
 	public GraphicsSurfaceView(Context context) {
 		super(context);
 		sh = getHolder();
 		sh.addCallback(this);
 		
 		//set Paint specs
 		paint.setColor(Color.BLACK);
 		paint.setStyle(Paint.Style.STROKE);
 		paint.setStrokeWidth((float)3);
 		
 		ctx = context;
 		setFocusable(true);
 		
 		Resources res = ctx.getResources();
 		note1 = BitmapFactory.decodeResource(res, R.drawable.note1);
 		
 		//Drawable note1 = res.getDrawable(R.drawable.note1);
 	}
 	
 	public GraphicsThread getThread(){
 		return thread;
 	}
 	
 	@Override
 	public void surfaceCreated(SurfaceHolder holder){
 		thread = new GraphicsThread(sh, ctx, new Handler());
 		thread.setRunning(true);
 		thread.start();
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 		thread.setSurfaceSize(width, height);
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		boolean retry = true;
 		thread.setRunning(false);
 		while(retry){
 			try {
 				thread.join();
 				retry = false;
 			} catch (InterruptedException e) {
 				Log.i(TAG, "Failed to join thread");
 			}
 		}
 	}
 	
 	public void noteMove(boolean move){
 		notemove = move;
 	}
 	
 	//Draw Canvas using another thread to get off of main UI thread
 	class GraphicsThread extends Thread {
 		private int canvasWidth = 200;
 		private int canvasHeight = 400;
 		private float cycle, cycle2, cycle3 = (float)0.3;
 		private float noterun = (float)0.5;
 		private float time = 0;
 		private boolean run = false;
 
 		
 		public GraphicsThread(SurfaceHolder surfaceHolder, Context context, Handler handler){
 			sh = surfaceHolder;
 			handler = handler;
 			ctx = context;
 			try {
 				setCycle();
 			} catch (InterruptedException e) {
 			}
 		}
 		
 		public void doStart(){
 			synchronized(sh){
 				//dont need this for now
 			}
 		}
 		
 		public void run(){
 			while(run){
 				Canvas c = null;
 				try{
 					c = sh.lockCanvas(null);
 					synchronized(sh) {
 						doDraw(c);
 					}
 				} finally {
 					if (c != null) {
 							sh.unlockCanvasAndPost(c);
 					}
 				}
 			}
 		}
 		
 		public void setRunning(boolean b){
 			run = b;
 		}
 		
 		public void setSurfaceSize(int width, int height){
 			synchronized(sh) {
 				canvasWidth = width;
 				canvasHeight = height;
 				doStart();
 			}
 		}
 		
 		private void doDraw(Canvas canvas){
 			try {
 				if(notemove)
 					setCycle();
				//canvas.restore(); //clears canvas, currently isnt working so commented
 				canvas.drawColor(Color.WHITE); //sets canvas background color
 				canvas.drawLine((float)(canvasWidth*(1./15)), (float)(canvasHeight*(.45)), (float)(canvasWidth*(14.0/15)), (float)(canvasHeight*(.45)), paint); //1st bar line
 				canvas.drawLine((float)(canvasWidth*(1./15)), (float)(canvasHeight*(.53)), (float)(canvasWidth*(14.0/15)), (float)(canvasHeight*(.53)), paint); //2nd bar line
 				canvas.drawLine((float)(canvasWidth*(1./15)), (float)(canvasHeight*(.61)), (float)(canvasWidth*(14.0/15)), (float)(canvasHeight*(.61)), paint); //3nd bar line
 				canvas.drawLine((float)(canvasWidth*(1./15)), (float)(canvasHeight*(.69)), (float)(canvasWidth*(14.0/15)), (float)(canvasHeight*(.69)), paint); //4nd bar line
 				canvas.drawLine((float)(canvasWidth*(1./15)), (float)(canvasHeight*(.77)), (float)(canvasWidth*(14.0/15)), (float)(canvasHeight*(.77)), paint); //5nd bar line
 				canvas.drawLine((float)(canvasWidth*(cycle)), (float)(canvasHeight*(.45)), (float)(canvasWidth*(cycle)), (float)(canvasHeight*(.77)), paint); //1st Vertical line
 				canvas.drawLine((float)(canvasWidth*(cycle2)), (float)(canvasHeight*(.45)), (float)(canvasWidth*(cycle2)), (float)(canvasHeight*(.77)), paint); //2nd vertical line
 				canvas.drawLine((float)(canvasWidth*(cycle3)), (float)(canvasHeight*(.45)), (float)(canvasWidth*(cycle3)), (float)(canvasHeight*(.77)), paint); //3nd vertical line
 				canvas.drawBitmap(note1, (float)(canvasWidth*(noterun)), (float)(canvasHeight*(.525)), paint);
 			} catch (Exception e) {
 				
 			}
 
 		}
 		
 		private void setCycle() throws InterruptedException{
 			if (noterun > (1./15))
 				noterun -= .005;
 			else
 				noterun = (float)(14./15);
 			
 			if (cycle > (float)(1./15))
 				cycle -= .005; //create movement
 			else
 				cycle = (float)(14./15); 
 			
 			if (cycle > (float)((14./15) - .3))
 				cycle2 = cycle + (float)(-(13./15) + .3);
 			else 
 				cycle2 = cycle + (float)0.3;
 			
 			
 			if (cycle2 > (float)((14./15) - .3))
 				cycle3 = cycle2 + (float)(-(13./15) + .3);
 			else
 				cycle3 = cycle2 + (float)0.3;
 			
			thread.sleep(40); //creates "frames" of movement, about 33 fps, no need to use up all memory
 			time+=30; //for notes later
 			Log.i(TAG, "Slept");
 		}
 		
 	}
 }
