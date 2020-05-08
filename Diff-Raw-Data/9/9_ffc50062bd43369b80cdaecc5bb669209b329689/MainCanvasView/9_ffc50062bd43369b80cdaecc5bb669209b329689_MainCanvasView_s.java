 package com.csewannabe;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Rect;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.Toast;
 
 public class MainCanvasView extends SurfaceView implements OnTouchListener, Runnable {
 
 	SurfaceHolder sHolder;
 	Bitmap picture;
 	Paint painter;
 	private Thread mThread;
 	private Rect src;
 	private Rect dest;
 	private Canvas bmCanvas;
 	private boolean isRunning;
 	private Bitmap blankMap;
 	private Path path;
 	private Bitmap buffer;
 	private Canvas bufCanvas;
 	boolean doubleTouch;
 	private float prevY;
 	private int newY;
 	private Rect blanksrc;
 	private Rect blankdest;
 	private Rect bufsrc;
 	private Rect bufdest;
 	private int picScale;
 	private int[] picCoords;
 	
 	public MainCanvasView(Context context, AttributeSet attributeSet) {
 		super(context, attributeSet);
 	}
 	
 	public MainCanvasView(Context context, Bitmap picture) {
 		super(context);
 		this.picture = picture;
 		sHolder = getHolder();
 		painter = new Paint(Paint.ANTI_ALIAS_FLAG);
 		painter.setColor(Color.BLACK);
 		painter.setStyle(Paint.Style.STROKE);
 		painter.setStrokeWidth(2);
 		setOnTouchListener(this);
 		
 		picScale = 1;
 		picCoords = new int[] {0,0};
 		src = new Rect(0,0,picture.getWidth(),picture.getHeight());			//default size of pic
 		dest = new Rect(picCoords[0],picCoords[1], picture.getWidth() * picScale + picCoords[0],
 							picture.getHeight() * picScale + picCoords[1]); //choose the location, left.top.right.btm
 		
 		
 		blankMap = Bitmap.createBitmap(1000, 2000, Bitmap.Config.ARGB_8888);
 		blanksrc = new Rect(0,0,blankMap.getWidth(), blankMap.getHeight());
 		blankdest = new Rect(0,0,blankMap.getWidth(), blankMap.getHeight());
 		bmCanvas = new Canvas(blankMap);
 		
 		
 		buffer = Bitmap.createBitmap(1000, 2000, Bitmap.Config.ARGB_8888);
 		bufsrc = new Rect(0,0,buffer.getWidth(), buffer.getHeight());
 		bufdest = new Rect(0,0,buffer.getWidth(), buffer.getHeight());
 		bufCanvas = new Canvas(buffer);
 		path = new Path();
 	}
 	
 	public void resume() {
 		mThread = new Thread(this);
 		mThread.start();
 		isRunning = true;
 	}
 	
 	public void pause() {
 		isRunning = false;
 		
 		try {
 			mThread.join();
 		} catch (InterruptedException e) {}
 	}
 	
 	public void undo() {
 		path =  new Path();
 	}
 	
 	public void clear() {
 		blankMap = Bitmap.createBitmap(1000, 2000, Bitmap.Config.ARGB_8888);
		blanksrc = new Rect(0,0,blankMap.getWidth(), blankMap.getHeight());
		blankdest = new Rect(0,0,blankMap.getWidth(), blankMap.getHeight());
 		bmCanvas = new Canvas(blankMap);
 		
 		
 		buffer = Bitmap.createBitmap(1000, 2000, Bitmap.Config.ARGB_8888);
		bufsrc = new Rect(0,0,buffer.getWidth(), buffer.getHeight());
		bufdest = new Rect(0,0,buffer.getWidth(), buffer.getHeight());
 		bufCanvas = new Canvas(buffer);
 		path = new Path();
 		
 	}
 
 	public boolean onTouch(View v, MotionEvent event) {
 		
 		switch (event.getAction()) {
 		case MotionEvent.ACTION_MOVE: 
 			final int historySize = event.getHistorySize();	
 			int numPointers = event.getPointerCount();
 
 			if (numPointers < 2) {
 				for (int h = 0; h < historySize; h++) {
 					for(int p = 0; p < event.getPointerCount(); p++) {
 						path.lineTo(event.getHistoricalX(p, h),event.getHistoricalY(p,h)+newY);
 						//bmCanvas.drawCircle(event.getHistoricalX(p, h), event.getHistoricalY(p, h), 3,painter);
 						bufCanvas.drawPath(path, painter);
 						
 						path.moveTo(event.getHistoricalX(p, h),event.getHistoricalY(p,h)+newY);
 				    }
 				}
 			} else 	if (numPointers > 1) {
 				Log.d("TOUCHED", "Am scrolling!");
                 float currY = event.getRawY();
                 float deltaY = -(currY - prevY);
                 newY += deltaY;
                 if (newY < 0) {
                 	newY = 0;
                 	Toast.makeText(getContext(), "Reached End", Toast.LENGTH_SHORT).show();
                 }
                 if (newY > 1500) {
                 	newY = 1500;
                 	Toast.makeText(getContext(), "Reached End", Toast.LENGTH_SHORT).show();
                 }
                 dest.set(picCoords[0], picCoords[1]-newY, picCoords[0]+picture.getWidth() * picScale, picCoords[1]-newY + picture.getHeight() * picScale);
                 blankdest.set(0,0-newY, blankMap.getWidth(), blankMap.getHeight() - newY);
                 bufdest.set(0,0-newY, buffer.getWidth(), buffer.getHeight() -  newY);
                 prevY = currY;
 			}
 			
 			break;
 		
 		case MotionEvent.ACTION_DOWN: 
 			//bmCanvas.drawCircle(event.getX(), event.getY(), 3,painter);
 			bmCanvas.drawPath(path, painter);
 				path = new Path();
 				path.moveTo(event.getX(), event.getY()+newY);
 			//Sets the start scroll point
 				
 				prevY = event.getRawY();
 				break;
 		default:
 			break;
 		
 		}
 		return true;
 	}
 		
 
 	public void run() {
 		while(isRunning) {
 			if(sHolder.getSurface().isValid()) {
 				Canvas drawingCanvas = sHolder.lockCanvas();
 				drawingCanvas.drawARGB(255, 180, 99, 36);
 				drawingCanvas.drawBitmap(picture, src,dest, null);
 				drawingCanvas.drawBitmap(blankMap, blanksrc,blankdest, null);
 				drawingCanvas.drawBitmap(buffer,  bufsrc, bufdest, null);
 				
 				
 				sHolder.unlockCanvasAndPost(drawingCanvas);
 			}
 		}
 	}
 
 
 	
 }
