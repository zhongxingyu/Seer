 package no.jts.android.simple.gameapi.setup;
 
 import no.jts.android.simple.gameapi.Setup;
 import android.app.Activity;
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.BitmapFactory;
 import android.os.Build;
 import android.util.Log;
 import android.view.Display;
 
 public class Globals {
 
 	public static final String TAG = "Globals";
 
 	public static Context context;
 	public static Resources resources;
 	public static int displayWidth;
 	public static int displayHeight;
 	public static float scaleFactorWidth;
 	public static float scaleFactorHeight;
 	public static BitmapFactory.Options options;
 	
 	public static void init(Activity activity, Setup setup){
 		context = activity.getApplicationContext();
 		resources = activity.getResources();
 		Display display = activity.getWindowManager().getDefaultDisplay();
 		displayWidth = display.getWidth();
 		displayHeight = display.getHeight();
 		Log.i(TAG, "Display width: " + displayWidth + " height: " + displayHeight );
 		
 		if(setup.getOriginDesignWidth() != 0){
			scaleFactorWidth = (float)display.getWidth()/(float)setup.getOriginDesignWidth();
 		} else {
 			throw new RuntimeException("You have to set the width of your origin design to get the scaling to work");
 		}
 		
 		if(setup.getOriginDesignHeight() != 0){
			scaleFactorHeight = (float)display.getHeight()/(float)setup.getOriginDesignHeight();
 		} else {
 			throw new RuntimeException("You have to set the height of your origin design to get the scaling to work");
 		}
 		options = new BitmapFactory.Options();
 		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD){
 			options.inPurgeable = true;
 		}
 	}
 }
