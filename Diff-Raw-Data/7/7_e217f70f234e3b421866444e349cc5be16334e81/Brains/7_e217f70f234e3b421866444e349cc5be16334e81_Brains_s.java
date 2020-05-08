 package info.qrees.android.brains;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 
 import android.app.Activity;
 import android.content.res.AssetManager;
 import android.opengl.GLSurfaceView;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class Brains extends Activity {
 	private GLSurfaceView mGLSurfaceView;
 	AssetManager assets;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
         assets = getAssets();
         GL2JNILib.assets = assets;
         createStorageDirectory();
         //mGLSurfaceView = new TouchView(this, new ViewRenderer(getAssets()));
         mGLSurfaceView = new GL2JNIView(getApplication(), assets);
         //mGLSurfaceView.setRenderMode(GL2JNIView.RENDERMODE_WHEN_DIRTY);
         mGLSurfaceView.setRenderMode(GL2JNIView.RENDERMODE_CONTINUOUSLY);
         setContentView(mGLSurfaceView);
         GL2JNILib.onCreate();
     }
     
 	private void createStorageDirectory() {
         boolean mExternalStorageAvailable = false;
         boolean mExternalStorageWriteable = false;
         String state = Environment.getExternalStorageState();
         
     	if (Environment.MEDIA_MOUNTED.equals(state)) {
             mExternalStorageAvailable = mExternalStorageWriteable = true;
         } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
             mExternalStorageAvailable = true;
             mExternalStorageWriteable = false;
         } else {
             mExternalStorageAvailable = mExternalStorageWriteable = false;
         }
         if(mExternalStorageWriteable){
         	Log.d(Environment.getExternalStorageDirectory().getAbsolutePath());
         	File dir = new File(Environment.getExternalStorageDirectory(), "64b");
         	if(dir.mkdirs())
         		Log.d("Created storage directory");
         	else
         		Log.d("failed to create storage directory");
         	if(!dir.exists())
         		Log.w("Data directory does not exist");
         }else{
         	Log.d("Storage is not writable");
         }
 	}
 	
 	@Override
     protected void onResume() {
         // Ideally a game should implement onResume() and onPause()
         // to take appropriate action when the activity looses focus
         super.onResume();
         mGLSurfaceView.onResume();
         GL2JNILib.onResume();
     }
 
     @Override
     protected void onPause() {
         // Ideally a game should implement onResume() and onPause()
         // to take appropriate action when the activity looses focus
        super.onPause();
         mGLSurfaceView.onPause();
         GL2JNILib.onPause();
     }
     
     @Override
     protected void onDestroy() {
        super.onPause();
         GL2JNILib.onDestroy();
     }
 }
