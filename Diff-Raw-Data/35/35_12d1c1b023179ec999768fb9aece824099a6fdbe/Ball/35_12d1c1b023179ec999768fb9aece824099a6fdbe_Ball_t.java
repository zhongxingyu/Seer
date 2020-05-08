 package com.nomath4u.breakbrick;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 import java.util.Vector;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.RectF;
 import android.media.AudioManager;
 import android.os.Build;
 import android.os.CountDownTimer;
 import android.os.Handler;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Display;
 import android.view.WindowManager;
 
 public class Ball {
 	public Paint paint;
 	public RectF image;
 	private int screenwidth;
 	private int screenheight;
 	private int ballwidth;
 	private int ballheight;
 	public PhysVector velocity;
 	private MainActivity parent;
 	private static int maxAngle = 80;
 	private Random r;
     public double scalar;
     public boolean timing = true;
 	private CountDownTimer timey;
     public boolean spawned = false;
     public int counter = 4;
     public double mult = .01;
     Ball(Context context){
 		parent = (MainActivity) context;
 		getDisplay(context);
         setBallCharacteristics(1);
         image = new RectF(0,0,0,0);
 
 		r = new Random();
         timey = new CountDownTimer(3100,1000) {
             @Override
             public void onTick(long l) {
                 /*Show stuff*/
                 counter--;
             }
 
             @Override
             public void onFinish() {
                 timing = false;
                 image = new RectF((screenwidth/2) - (ballwidth/2), (screenheight/2)- (ballheight/2), (screenwidth/2) + (ballwidth/2), (screenheight/2) + (ballheight/2));
                 //Spawn center of ball at center of screen
                 counter = 4;
             }
         };
 	}
 	
 	
 	
 	public void setBallCharacteristics(int speed){
 			paint = new Paint();
 			paint.setColor(Color.RED);
 			ballwidth = (int)((.01) * screenwidth); //(3.33 * scalar);
             //ballwidth = 5;
             Log.v("blah",String.valueOf(ballwidth));
 			ballheight = ballwidth; //Because the ball should be square
         velocity = new PhysVector((int)((speed * .001 +.007 ) * screenheight),-90);
 	}
 	
 	public void spawn(){
         timing = true;
         counter = 4;
         timey.start();
         image = new RectF(0,0,0,0); //So that there isn't one left behind when counting down
         spawned = true;
         //velocity = new PhysVector((int)((parent.level + 5)*(scalar)),-90);
         velocity = new PhysVector((int)((parent.level * .001 +.007 ) * screenheight),-90);
 	}
 
 	/*Gets the Display characteristics and does it the way it should regardless of android version they are running*/
 	/*Cases are for subtracting the notification bar height*/
 	@SuppressWarnings("deprecation")
 	private void getDisplay(Context context){
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
            wm.getDefaultDisplay().getSize(size);
            screenwidth = size.x;
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
 	}
 	
 	public void tick(){
         if(!timing){
 
         image.offset((float)velocity.speedX(), -1*(float)velocity.speedY());
 		
 		/*fix the size of the ball that sometimes gets messed up on collision*/ /*This is just a hack still need to find the real problem*/
 		if(image.bottom - image.top != (int) (mult * screenwidth)){
 			float factor = ((int)(mult * screenwidth) - (image.bottom - image.top))/2;
 			image.bottom = image.bottom + factor;
 			image.top = image.top - factor;
 		}
 		
 		if(image.right - image.left != (int) (mult * screenwidth)){
 			float factor = ((int) (mult * screenwidth) - (image.right - image.left))/2;
 			image.right = image.right + factor;
 			image.left = image.left - factor;
 		}
 		
 		
 		/*Check for paddle collisions*/
 		if(image.intersect(parent.panel.mainPaddle.selfimage)){
 			rotateToAngle(parent.panel.mainPaddle.selfimage.centerX(), parent.panel.mainPaddle.paddlewidth);
 			   AudioManager audioManager = (AudioManager) parent.getSystemService(parent.AUDIO_SERVICE);
 	            float actualVolume = (float) audioManager
 	                    .getStreamVolume(AudioManager.STREAM_MUSIC);
 	            float maxVolume = (float) audioManager
 	                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 	            float volume = actualVolume / maxVolume;
 	            // Is the sound loaded already?
 	            if (parent.loaded) {
 	                parent.pool.play(parent.soundID, volume, volume, 1, 0, 1f);
 	                Log.e("Test", "Played sound");
 	            }
 		}
 		
 		/*Check against screen edges*/
 		
 		if((image.left < 0) || (image.right > screenwidth)){
 			flipXSpeed();
 		}
 		
 		if((image.top < 0)){
 			flipYSpeed();
 		}
 		
 		if(image.bottom > screenheight){
 			parent.addLife(-1);
 			spawn();
 		}
 		
 		/*Check for brick collisions*/
 		
 		
 		 List<Brick> bricks = parent.panel.bricks;
 		    Iterator<Brick> brickIterator = bricks.iterator();
 		    Brick brick = null;
 		    boolean intersected = false;
 		    while (! intersected && brickIterator.hasNext())
 		    {
 		      brick = brickIterator.next();
 		      intersected = image.intersect(brick.image);
 		    }
 		    if (intersected)
 		    {
 		      bricks.remove(brick);
 		      parent.mOutbox.mbrickAchievement++;
               flipYSpeed();
 		      parent.addScore(500);
               //parent.panel.scoreView.
 		      /*play sound*/
 		      AudioManager audioManager = (AudioManager) parent.getSystemService(parent.AUDIO_SERVICE);
 	            float actualVolume = (float) audioManager
 	                    .getStreamVolume(AudioManager.STREAM_MUSIC);
 	            float maxVolume = (float) audioManager
 	                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 	            float volume = actualVolume / maxVolume;
 	            // Is the sound loaded already?
 	            if (parent.loaded) {
 	                parent.pool.play(parent.soundIDa, volume, volume, 1, 0, 1f);
 	                Log.e("Test", "Played sound");
 	            }
 	            if(parent.panel.eLife == null)
 	            	if(r.nextInt()%20 == 0)
 	            		parent.panel.eLife = new ExtraLife((int)brick.image.left,(int)brick.image.top, parent.panel);
 		    }
 		
         }
 
 	}
 	
 	private void flipXSpeed(){
 		velocity.flipX();
 	}
 	
 	private void flipYSpeed(){
 		velocity.flipY();
 	}
 	
 	private void rotateToAngle(float paddleCenter, int paddlewidth){
 		float distance = (paddleCenter - this.image.right);
 		float multiplier = distance/paddlewidth;
 		if(distance >0 ){
 			velocity.direction = 90 + (multiplier * maxAngle);
 		}
 		else{
 			velocity.direction = 90 - (-1 * multiplier * maxAngle);
 		}
 		
 		
 		
 	}
 }
