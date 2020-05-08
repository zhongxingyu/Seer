 package com.silvercode.droidz;
 
 import android.app.Activity;
 import android.util.Log;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 import com.silvercode.driodz.model.components.Speed;
 import com.silvercode.droidz.model.Droid;
 import com.silvercode.droidz.model.ElaineAnimated;
 import com.silvercode.droidz.model.Explosion;
 
 public class MainGamePanel extends SurfaceView implements SurfaceHolder.Callback
 {
 	private static final String TAG = MainGamePanel.class.getSimpleName();
     
 	private static final int EXPLOSION_SIZE = 100;
 	
 	private MainThread thread;
 	private ElaineAnimated elaine;
     private String avgFps;
     private Explosion[] explosions;
     
     public void setAvgFps(String avgFps)
     {
     	this.avgFps = avgFps;
     }
 	
 	public MainGamePanel(Context context)
 	{
 		super(context);		
 		getHolder().addCallback(this);
 		
         Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.walk_elaine);
         elaine = new ElaineAnimated(bitmap, 10, 50, 30, 47, 5, 5);
 		
 		thread = new MainThread(getHolder(), this);
 		
 		setFocusable(true);
 	}
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
 	{
 		// TODO Auto-generated method stub		
 	}
 
 	public void surfaceCreated(SurfaceHolder holder)
 	{
         explosions = new Explosion[10];
         for (int i = 0; i < explosions.length; i++)
         {
         	explosions[i] = null;
         }
         
 		thread.setRunning(true);
 		thread.start();
 	}
 
 	public void surfaceDestroyed(SurfaceHolder holder)
 	{
 		boolean retry = true;
 		
 		while (retry)
 		{
 			try
 			{
 				thread.join();
 				retry = false;
 			} catch (InterruptedException e)
 			{
 				// try shut down the thread again
 			}
 		}
 	}
 	
 	public boolean onTouchEvent(MotionEvent event)
 	{
 		if (event.getAction() == MotionEvent.ACTION_DOWN)
 		{
 			if (event.getY() > getHeight() - 50)
 			{
 				thread.setRunning(false);				
 				((Activity)getContext()).finish();
 			}
             
 			int currentExplosion = 0;
 			Explosion explosion = explosions[currentExplosion];
             
			while (explosion != null && explosion.isAlive() && currentExplosion < explosions.length)
 			{
 				currentExplosion++;
 				explosion = explosions[currentExplosion];
 			}
             
 			if (explosion == null || explosion.isDead())
 			{
 				explosion = new Explosion(EXPLOSION_SIZE, (int)event.getX(), (int)event.getY());
                 explosions[currentExplosion] = explosion;
 			}
 		}
 		
 		if (event.getAction() == MotionEvent.ACTION_MOVE)
 		{
 		}
 		
 		if (event.getAction() == MotionEvent.ACTION_UP)
 		{
 		}
 		
 		return true;
 	}
 	
 	protected void render(Canvas canvas)
 	{
 		canvas.drawColor(Color.BLACK);
         elaine.draw(canvas);
         for (int i = 0; i < explosions.length; i++)
         {
             if (explosions[i] != null)
                 explosions[i].draw(canvas);
         }
 		displayFps(canvas, avgFps);
 	}
     
 	private void displayFps(Canvas canvas, String fps)
 	{
         if (canvas != null && fps != null)
         {
         	Paint paint = new Paint();
         	paint.setARGB(255, 255, 255, 255);
         	canvas.drawText(fps, this.getWidth() - 50, this.getHeight() - 50, paint);
         }
 	}
 
 	public void update()
 	{
         elaine.update(System.currentTimeMillis());
         
         for (int i = 0; i < explosions.length; i++)
         {
             if (explosions[i] != null)
                 explosions[i].update();
         }
 	}
 }
