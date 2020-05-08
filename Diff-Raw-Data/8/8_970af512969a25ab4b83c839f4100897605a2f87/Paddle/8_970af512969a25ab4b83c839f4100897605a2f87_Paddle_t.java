 package com.nomath4u.breakbrick;
 
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.RectF;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Build;
 import android.os.Handler;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Display;
 import android.view.WindowManager;
 
 public class Paddle {
 	
 	public RectF selfimage;
 	public Paint selfstyle;
 	private Handler timer;
 	private Handler timer2;
 	private Runnable moveSelf;
 	private float maxval;
 	public int screenwidth;
 	public int screenheight;
 	private float unit;
 	public int paddlewidth;
 	private int paddleheight;
 	private MainActivity mainA;
     public double scalar; //Used for scaling the object based on the screen density
 	
 	Paddle(Context context){
 		this.selfimage = new RectF(0,0, 100, 100); //set coordinates to upper left
 		this.mainA = (MainActivity) context;
 
 
 
 		/*Set Paint Style*/
 		this.selfstyle = new Paint();
 		selfstyle.setColor(Color.BLUE);
 		
 		
 		
 		/*Get Window dimensions*/
 		Point size = new Point();
 		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
 		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
 		      wm.getDefaultDisplay().getSize(size);
 		      screenwidth = size.x;
 		      DisplayMetrics metrics = new DisplayMetrics();
 		      wm.getDefaultDisplay().getMetrics(metrics);
 		      
 
 		      switch (metrics.densityDpi) {
                   case DisplayMetrics.DENSITY_XXXHIGH:
                       Log.i("display", "XXXHIGH");
                       screenheight = size.y - 128;
                       scalar = 4;
                       break;
                   case DisplayMetrics.DENSITY_XXHIGH:
                       Log.i("display", "XXHIGH");
                       screenheight = size.y - 96;
                       scalar = 3;
                       break;
                   case DisplayMetrics.DENSITY_XHIGH:
                       Log.i("display", "XHIGH");
                       screenheight = size.y - 64;
                       scalar = 2;
                       break;
 		          case DisplayMetrics.DENSITY_HIGH:
 		              Log.i("display", "high");
 		              screenheight = size.y - 48;
                       scalar = 1.5;
 		              break;
 		          case DisplayMetrics.DENSITY_MEDIUM:
 		              Log.i("display", "medium/default");
 		              screenheight = size.y - 32;
                       scalar = 1;
 		              break;
 		          case DisplayMetrics.DENSITY_LOW:
 		              Log.i("display", "low");
 		              screenheight = size.y - 24;
                       scalar = .75;
 		              break;
 		          default:
 		              Log.i("display", "Unknown density"); 
 		      }
 		    
 		      }else{
 		      Display d = wm.getDefaultDisplay(); 
 		      DisplayMetrics metrics = new DisplayMetrics();
 		      wm.getDefaultDisplay().getMetrics(metrics);
 		   
 
 		      switch (metrics.densityDpi) {
 		          case DisplayMetrics.DENSITY_HIGH:
 		              Log.i("display", "high");
 		              screenheight = d.getHeight() - 48;
                       scalar = 1.5;
 		              break;
 		          case DisplayMetrics.DENSITY_MEDIUM:
 		              Log.i("display", "medium/default");
 		              screenheight = d.getHeight() - 32;
                       scalar = 1;
 		              break;
 		          case DisplayMetrics.DENSITY_LOW:
 		              Log.i("display", "low");
 		              screenheight = d.getHeight() - 24;
                       scalar = .75;
 		              break;
 		          default:
 		              Log.i("display", "Unknown density");
 		      }
 		    }
        //paddlewidth = (int)(53 * scalar);
        //paddleheight = (int)(10* scalar);
        float paddleXmult = ((float)78/(float)540);
        float paddleYmult = ((float)15/(float)912);
        paddlewidth = (int)(paddleXmult * screenwidth);
        paddleheight = (int)(paddleYmult * screenheight);
 		setUnit();
 		
 		/*Set up moving self */
 		timer = new Handler();
 		moveSelf = new Runnable(){
 			public void run(){
 				move();
 				timer.postDelayed(moveSelf, (17)); //just slower than 60fps
 			}
 		};
 		
 		/*Start Moving*/
 		this.startMoving();
 			
 	}
 	
 	public void move(){
 		
 		float top = screenheight - paddleheight;
 		float left = (mainA.adcval  * unit * -1) + (float)(screenwidth/2) - (paddlewidth/2);
         /*if(left > screenwidth){
              left = 0;
         }*/
 		float right = (mainA.adcval * unit * -1)+ (float)(screenwidth/2) + (paddlewidth/2);
         /*if(right < 0){
             right = screenwidth;
         }*/
 		float bottom = screenheight;
 		selfimage.set(left, top, right , bottom); 
 	}
 	
 	public void startMoving(){
 		moveSelf.run();
 	}
 	
 	public void stopMoving(){
 		timer.removeCallbacks(moveSelf);
 	}
 	
 	private void setUnit(){
 		unit = (screenwidth / ( .3f *(mainA.maxval)));
 	}
 	
 }
