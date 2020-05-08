 package com.hauk142.hackles;
 
 import java.io.InputStream;
 import java.net.URL;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.AsyncTask;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.widget.ImageView;
 import android.widget.Toast;
 import android.widget.Button;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Environment;
 import android.view.GestureDetector;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.MotionEvent;
 import android.view.View;
 import android.os.Vibrator;
 
 public class HacklesActivity extends Activity implements OnGestureListener
 {
     int comic;
     ImageView image;
     SharedPreferences.Editor editor;
     private GestureDetector gesture;
    Context context;
    Vibrator vibrator;
 	    
     private static final int SWIPE_MIN_DISTANCE = 120;
     private static final int SWIPE_MAX_OFF_PATH = 250;
     private static final int SWIPE_THRESHOLD_VELOCITY = 200;
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
	context = getApplicationContext();
	vibrator = (Vibrator) getSystemService(context.VIBRATOR_SERVICE);
     	setContentView(R.layout.main);
 	gesture = new GestureDetector(this);
 	image = (ImageView) findViewById(R.id.image);
 	Button Next = (Button) findViewById(R.id.ButtonNext);
 	Button Previous = (Button) findViewById(R.id.ButtonPrevious);
 	SharedPreferences settings = getPreferences(MODE_PRIVATE);
 	comic = settings.getInt("comic", 1);	
 	editor = settings.edit();
 	new DownloadImage().execute("http://hackles.org/strips/cartoon" + comic + ".png");
 
 
 	Next.setOnClickListener(new Button.OnClickListener() 
 	{
 		public void onClick(View v) {
 			try
 			{
 			/*	if(comic = null)
 				{
 					try{
 						in
 				}*/
 				if(comic != 364)
 				{
 					vibrator.vibrate(50);
 					comic++;
 					new DownloadImage().execute("http://hackles.org/strips/cartoon" + comic + ".png");
 					editor.putInt("comic", comic);
 					editor.commit();
 					toast(Integer.toString(comic));
 				}
 				else
 				{
 					toast("Reached the last comic!");
 				}
 			}
 			catch (Exception e) {
 			}
 		}
 	});
 	Previous.setOnClickListener(new Button.OnClickListener()
 	{
 		public void onClick(View v) {
 			try
 			{
 				if(comic != 1)
 				{
 					vibrator.vibrate(50);
 					comic--;
 					new DownloadImage().execute("http://hackles.org/strips/cartoon" + comic + ".png");
 					editor.putInt("comic", comic);
 					editor.commit();
 					toast(Integer.toString(comic));
 				}
 				else
 				{
 					toast("Reached the first comic!");
 				}
 			}
 			catch (Exception e) {
 			}
 		}
 	});
     }
 
     public void toast(String text)
     {
 	    Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
 	    toast.show();
     }
 
 
     private class DownloadImage extends AsyncTask<String, Void, Bitmap> {
 	    protected Bitmap doInBackground(String... url) {
 		    return loadImageFromNetwork(url[0]);
 	    }
 
 	    protected void onPostExecute(Bitmap result) {
 		    image.setImageBitmap(result);
 	    }
     }
 
     private Bitmap loadImageFromNetwork(String url) {
 	    try {
 		    Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(url).getContent());
 		    return bitmap;
 	    }
 	    catch (Exception e) {
 		    e.printStackTrace();
 		    return null; 
 	    }
     }
     @Override
     public boolean onTouchEvent(MotionEvent event)
     {
 	    return gesture.onTouchEvent(event);
     }
     @Override
     public boolean onDown(MotionEvent e) 
     {
 	    return false;
     }
     @Override
     public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) 
     {
 	    try
 	    {
 		    if( Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH )
 			    return false;
 
 		    // Left to right
 		    if( e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY )
 		    {
 			    	if(comic != 1)
 				{
 					vibrator.vibrate(50);
 					comic--;
 					new DownloadImage().execute("http://hackles.org/strips/cartoon" + comic + ".png");
 					editor.putInt("comic", comic);
 					editor.commit();
 				}
 				else
 				{
 					toast("Reached the first comic!");
 				}
 		    }
 
 
 		    // Right to left
 		    else if( e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY )
 		    {
 			    	if(comic != 364)
 				{
 					vibrator.vibrate(50);
 					comic++;
 					new DownloadImage().execute("http://hackles.org/strips/cartoon" + comic + ".png");
 					editor.putInt("comic", comic);
 					editor.commit();
 				}
 				else
 				{
 					toast("Reached the first comic!");
 				}
 		    }
 	    }
 	    catch (Exception e)
 	    {
 	    }
 
 	    return false;
     }										 
     @Override
     public void onLongPress(MotionEvent e) 
     {
     }
     @Override
     public void onShowPress(MotionEvent e) 
     {
     }
     @Override
     public boolean onSingleTapUp(MotionEvent e) 
     {
 	    return false;
     }
     @Override
     public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
     {
 	    return false;
     }
 }
