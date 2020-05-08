 package stkl.spectropolarisclient;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class GameActivity extends Activity {
 	private static GameActivity instance;
 	
 	private GameThread d_gameThread;
 	private Model d_model;
 	private int d_centerHorizontal;
 	private int d_centerVertical;
 	private int d_motionPointerId = -2;
 	private int d_shootPointerId = -2;
	private float[] d_originX = new float[2];
	private float[] d_originY = new float[2];
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
             
         // Initialize window
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
         
         // Calculate screen dimension
         Display display = getWindowManager().getDefaultDisplay();
         d_centerHorizontal = (int)(display.getWidth() / 2);
         d_centerVertical = (int)(display.getHeight() / 2);
         
         // Start game
         int color = getIntent().getExtras().getInt("stkl.spectropolarisclient.color");
         d_model = new Model(this, color);
         
         instance = this;
         
         GameView gameView =  new GameView(this, d_model);
         //gameView.setModel(model);
         setContentView(gameView);
         
         d_gameThread = new GameThread(gameView, d_model);
         d_gameThread.start();
         
         
     }
     
     public static GameActivity getInstance() {
     	return instance;
     }
     
     public Model model() {
     	return d_model;
     }
     
     public int centerHorizontal() {
     	return d_centerHorizontal;
     }
     
     public int centerVertical() {
     	return d_centerVertical;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_game, menu);
         return true;
     }
     
     @Override
 	public boolean onTouchEvent(MotionEvent event) {
     	int pointerCount = event.getPointerCount();
     	
     	// Process no more than 2 touches
     	if (pointerCount > 2) {
     		pointerCount = 2;
     	}    	
     	
     	int actionId = event.getActionIndex();
     	
     	for (int i = 0; i < pointerCount; i++) {
     		
     		int id = event.getPointerId(i);
     		int action = (event.getAction() & MotionEvent.ACTION_MASK);
     		float x = event.getX(i);
     		float y = event.getY(i);
     		
     		switch (action){
 	    		case MotionEvent.ACTION_DOWN: case MotionEvent.ACTION_POINTER_DOWN:
 	    			if (actionId == i) { 
 		    			if(x > d_centerHorizontal) {
 		    				d_motionPointerId = id;
 		    				d_model.setMotionOrigin(x, y);
 		    			} else {
 		    				d_shootPointerId = id;
 		    				d_model.setShootOrigin(x, y);
 		    			}
 		    			d_originX[id] = x;
 		    			d_originY[id] = y;
 	    			}
 	    			break;
 	
 	    		case MotionEvent.ACTION_MOVE:
 	    			float deltaX = x - d_originX[id];
 					float deltaY = y - d_originY[id];
 					if (id == d_motionPointerId) {
 						d_model.setMotionControls(deltaX, deltaY);
 					} else if (id == d_shootPointerId) {
 						d_model.setShootControls(deltaX, deltaY);
 					}
 	    			break;
 	    			
 	    		case MotionEvent.ACTION_UP: case MotionEvent.ACTION_POINTER_UP: 
 	    			if (actionId == i) { 
 		    			if (id == d_motionPointerId) {
 		    				//System.out.println("Motion released, event: " + action + " as id: " + id);
 		    				d_motionPointerId = -2;
 		    				d_model.setMotionControls(0, 0);
 		    				d_model.setMotionOrigin(-1, -1);
 						} else if (id == d_shootPointerId) {
 							//System.out.println("Shoot released, event: " + action + " as id: " + id);
 							d_shootPointerId = -2;
 							d_model.setShootControls(0, 0);
 							d_model.setShootOrigin(-1, -1);
 						}
 	    			}
 	    			break;
     			
     		}
     	}
     	
 		return super.onTouchEvent(event);
 	}
 
     @Override
     protected void onStop() {
     	super.onStop();
     	instance = null;
     	// Tell JoinActivity this activity has ended
     	setResult(RESULT_OK);
     	d_gameThread.close();
     }
 
 }
