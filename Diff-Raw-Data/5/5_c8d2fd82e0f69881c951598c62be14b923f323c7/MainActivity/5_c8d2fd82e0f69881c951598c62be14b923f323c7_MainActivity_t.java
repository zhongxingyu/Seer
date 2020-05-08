 package com.ec327cassio.reversi;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 
 
 
 public class MainActivity extends Activity {
 	public Grid grid;
 	public int[][] gamestate_int = new int[8][8];
 	public Circle[][] gamestate_circles = new Circle[8][8];
 	 
 	//for the shake sensor
 	private SensorManager mSensorManager;
 	  private float mAccel; // acceleration apart from gravity
 	  private float mAccelCurrent; // current acceleration including gravity
 	  private float mAccelLast; // last acceleration including gravity
 	  
 	  
 	private int count = 0;
 	static {
         System.loadLibrary("reversi");
     }
 	public native String getString();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		 RelativeLayout main = (RelativeLayout) findViewById(R.id.board_view);
 		 this.grid = new Grid(main.getContext());
 		 main.addView(this.grid);
 		 //setup initial board
 		 this.grid.select(4, 3);
 		 this.tryMoveAtIndex(4,3);
 		 this.grid.select(4, 4);
 		 this.tryMoveAtIndex(4,4);
 		 this.grid.select(3, 4);
 		 this.tryMoveAtIndex(3,4);
 		 this.grid.select(3, 3);
 		 this.tryMoveAtIndex(3,3);
 
 		 		 
 		 mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 		    mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
 		    mAccel = 0.00f;
 		    mAccelCurrent = SensorManager.GRAVITY_EARTH;
 		    mAccelLast = SensorManager.GRAVITY_EARTH;		
 		
 		   this.grid.setOnTouchListener(new View.OnTouchListener() {
 			    public boolean onTouch(View v, MotionEvent e) {
 			    	if (e.getAction() == MotionEvent.ACTION_DOWN)  {
 			    		int x = (int) e.getX();
 			    		int y = (int) e.getY();
 			    		//update the grid property for the space touched.
 			    		MainActivity.this.grid.selectAtTouch(x, y);
 			    		if (gameisnotover()) {MainActivity.this.tryMoveAtIndex(grid.selX,grid.selY);}
 			    		return true;
 			    	}
 			    	else return false;
 			    }
 			    
 			    
 			    
 			});	
 	}
 	
 	public boolean gameisnotover() {
 		if (count < 64) {
 			return true;
 		}
 		else 
 		{
		//TOAST!
 		Context context = getApplicationContext();
 		CharSequence text = "Game Over!";
 		int duration = Toast.LENGTH_SHORT;
 
 		Toast toast = Toast.makeText(context, text, duration);
 		toast.show();
 		toast.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
 		return false;
 		}
 		
 	}
 	public void tryMoveAtIndex(int x,int y) {
 		Log.d("The value of the grid width", Float.toString(MainActivity.this.grid.tile_width));
 		//get board view
 		RelativeLayout gl = (RelativeLayout) findViewById(R.id.board_view);
 		//if move allowed, add this one to the array. (this line should call c, passing
 		// the index of the desired move, like this:
 		//if (moveIsAllowed(x,y,gamestate_ints,count %2)
 			//if it's an okay move, add to array of circles. Also add it to ints array
 			gamestate_circles[x][y] = new Circle(gl.getContext(),
 					(grid.tile_width/2)+grid.selX*(grid.tile_width),(grid.tile_height/2)+grid.selY*(grid.tile_height),25,count%2);
 			//gamestate_ints[x][y] = blah blah
 		//	pass un-updated array to C, then change colors as needed:
 			//upadateBoard(gamestate_ints) (pass reference)??
 			//iterate over gamestate_circles, fixing colors and reprinting.
 		gl.addView(gamestate_circles[x][y]);
 		Log.d("The count of this move is", Integer.toString(count));
 		count++;
 		MainActivity.this.gameisnotover();
 		
 	}
 
 	public void reset(View view) {
 		finish();
 		startActivity(new Intent(MainActivity.this, MainActivity.class));
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 
 
 	  private final SensorEventListener mSensorListener = new SensorEventListener() {
 
 	    public void onSensorChanged(SensorEvent se) {
 	      float x = se.values[0];
 	      float y = se.values[1];
 	      float z = se.values[2];
 	      mAccelLast = mAccelCurrent;
 	      mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
 	      float delta = mAccelCurrent - mAccelLast;
 	      mAccel = mAccel * 0.9f + delta; // perform low-cut filter
 	      
 	      //detect shake and reset
	      if (mAccel > 4)
 	      {
 	    	  MainActivity.this.reset(findViewById(R.id.main_view));
 	      }
 	    }
 
 	    public void onAccuracyChanged(Sensor sensor, int accuracy) {
 	    }
 	  };
 
 	  @Override
 	  protected void onResume() {
 	    super.onResume();
 	    mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
 	  }
 
 	  @Override
 	  protected void onStop() {
 	    mSensorManager.unregisterListener(mSensorListener);
 	    super.onStop();
 	  }
 	
 
 }
