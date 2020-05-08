 package fr.umlv.escape.front;
 
 import android.content.Context;
 import android.graphics.BitmapFactory;
 import android.graphics.Point;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceHolder.Callback;
 import android.view.SurfaceView;
 import fr.umlv.escape.R;
 import fr.umlv.escape.game.Game;
import fr.umlv.escape.gesture.BackOff;
 import fr.umlv.escape.gesture.GestureDetector;
import fr.umlv.escape.gesture.Horizontal;
import fr.umlv.escape.gesture.LeftCircle;
import fr.umlv.escape.gesture.LeftDiag;
import fr.umlv.escape.gesture.RightCircle;
import fr.umlv.escape.gesture.RightDiag;
 
  public class FrontApplication extends SurfaceView{
 	private SurfaceHolder holder;
 	private GestureDetector gestureDetector;
 	public static FrontImages frontImage;
 	public static int HEIGHT; 
 	public static int WIDTH;
 	final BattleField battleField;
 
 	public FrontApplication(Context context) {
 		super(context);
 		this.holder = getHolder();
 		frontImage = new FrontImages(getResources());
 		battleField = new BattleField(getWidth(),getHeight(),frontImage.getImage("level1"));
 		callback();
 	}
 
 	private void callback(){    	  
 		getHolder().addCallback(new Callback() {
 			private Thread drawThread;
 			
 			@Override
 			public void surfaceDestroyed(SurfaceHolder holder) {
 				drawThread.interrupt();
 			}
 
 			@Override
 			public void surfaceCreated(SurfaceHolder arg0) {
 				
				System.out.println("SURFACE CREATED");
 				battleField.updateSreenSize(getWidth(), getHeight());
 				gestureDetector = new GestureDetector();
				gestureDetector.addGesture(new BackOff());
				gestureDetector.addGesture(new LeftDiag());
				gestureDetector.addGesture(new RightDiag());
				gestureDetector.addGesture(new Horizontal());
				gestureDetector.addGesture(new LeftCircle());
				gestureDetector.addGesture(new RightCircle());
 				
 				drawThread = new DrawThread(holder, battleField);
 				drawThread.start();
 			}
 
 			@Override
 			public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3){    				
 				// TODO enelever
 			}
 
 		});
 	}
 	
 	public BattleField getBattleField() {
 		return battleField;
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent arg1) {
 		switch (arg1.getAction()) {
 		case MotionEvent.ACTION_DOWN:
 			gestureDetector.clear();
 			Point p = new Point((int)arg1.getX(), (int)arg1.getY());
 			gestureDetector.addPoint(p);
 			break;
 		case MotionEvent.ACTION_MOVE:
 			Point p2 = new Point((int)arg1.getX(), (int)arg1.getY());
 			gestureDetector.addPoint(p2);
 			break;
 		case MotionEvent.ACTION_UP:
 			if(gestureDetector.detect()){
 	//			gestureDetector.
 			}
 		default:
 			break;
 		}
 		return true;
 	}
 }
