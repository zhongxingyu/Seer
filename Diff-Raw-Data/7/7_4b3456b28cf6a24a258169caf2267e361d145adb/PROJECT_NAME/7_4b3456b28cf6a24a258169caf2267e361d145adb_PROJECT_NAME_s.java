 
 
 
 package com.__PROJECT_VENDOR__.__PROJECT_PACKAGE__;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.app.Activity;
 import android.content.Context;
 import android.opengl.GLSurfaceView;
 import android.os.Bundle;
 import android.view.MotionEvent;
 // For No Title : 
 import android.view.Window;
 
 // For the full screen : 
 import android.view.WindowManager;
 
 import java.io.File;
 import android.content.Context;
 
 // For the getting apk name : 
 import android.content.pm.ActivityInfo;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.res.AssetFileDescriptor;
 import android.content.res.AssetManager;
 
 /**
  * @brief Class : 
  *
  */
 public class __PROJECT_NAME__ extends Activity {
 
 	private GLSurfaceView mGLView;
 
 	static {
 		System.loadLibrary("__PROJECT_PACKAGE__");
 	}
 
 	@Override protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// Remove the title of the current display : 
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		// set full screen Mode : 
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		//Force landscape
 		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 		// create bsurface system
 		mGLView = new EwolGLSurfaceView(this);
 		setContentView(mGLView);
 	}
 
 	@Override protected void onPause() {
 		super.onPause();
 		mGLView.onPause();
 	}
 
 	@Override protected void onResume() {
 		super.onResume();
 		mGLView.onResume();
 	}
 }
 
 
 /**
  * @brief Class : 
  *
  */
 class EwolGLSurfaceView extends GLSurfaceView {
 	private static native void nativeApplicationInit();
 	private static native void nativeApplicationUnInit();
 	private static native void nativeEventInputMotion(int pointerID, float x, float y);
 	private static native void nativeEventInputState(int pointerID, boolean isDown, float x, float y);
 	private static native void nativeEventUnknow(int eventID);
 	private static native void nativeParamSetArchiveDir(int mode, String myString);
 	
 	public EwolGLSurfaceView(Context context) {
 		// super must be first statement in constructor
 		super(context);
 		// Load the application directory
 		nativeParamSetArchiveDir(1, context.getFilesDir().toString());
 		nativeParamSetArchiveDir(2, context.getCacheDir().toString());
 		// to enable extarnal storage: add in the manifest the restriction needed ...
 		//nativeParamSetArchiveDir(3, context.getExternalCacheDir().toString());
 		
 		// return apk file path (or null on error)
 		String apkFilePath = null;
 		ApplicationInfo appInfo = null;
 		PackageManager packMgmr = context.getPackageManager();
 		try {
 			appInfo = packMgmr.getApplicationInfo("com.__PROJECT_VENDOR__.__PROJECT_PACKAGE__", 0);
 		} catch (NameNotFoundException e) {
 			e.printStackTrace();
 			throw new RuntimeException("Unable to locate assets, aborting...");
 		}
 		apkFilePath = appInfo.sourceDir;
 		nativeParamSetArchiveDir(0, apkFilePath);
 		
 		// je n'ai pas compris ...
 		mRenderer = new EwolRenderer();
 		setRenderer(mRenderer);
 		nativeApplicationInit();
 	}
 	
 	private boolean InputDown1 = false;
 	private boolean InputDown2 = false;
 	private boolean InputDown3 = false;
 	
 	public boolean onTouchEvent(final MotionEvent event) {
 		// Wrapper on input events : 
 		int tmpActionType = event.getAction();
 		
 		if (tmpActionType == MotionEvent.ACTION_MOVE) {
 			final int pointerCount = event.getPointerCount();
 			for (int p = 0; p < pointerCount; p++) {
 				nativeEventInputMotion(event.getPointerId(p), (float)event.getX(p), (float)event.getY(p));
 			}
 		} else if(    tmpActionType == MotionEvent.ACTION_POINTER_1_DOWN 
 		           || tmpActionType == MotionEvent.ACTION_DOWN) {
 			nativeEventInputState(0, true, (float)event.getX(0), (float)event.getY(0));
 			InputDown1 = true;
 		} else if(tmpActionType == MotionEvent.ACTION_POINTER_1_UP) {
 			nativeEventInputState(0, false, (float)event.getX(0), (float)event.getY(0));
 			InputDown1 = false;
 		} else if (tmpActionType == MotionEvent.ACTION_POINTER_2_DOWN) {
 			nativeEventInputState(1, true, (float)event.getX(1), (float)event.getY(1));
 			InputDown2 = true;
 		} else if (tmpActionType == MotionEvent.ACTION_POINTER_2_UP) {
 			nativeEventInputState(1, false, (float)event.getX(1), (float)event.getY(1));
 			InputDown2 = false;
 		} else if (tmpActionType == MotionEvent.ACTION_POINTER_3_DOWN) {
 			nativeEventInputState(2, true, (float)event.getX(2), (float)event.getY(2));
 			InputDown3 = true;
 		} else if (tmpActionType == MotionEvent.ACTION_POINTER_3_UP) {
 			nativeEventInputState(2, false, (float)event.getX(2), (float)event.getY(2));
 			InputDown3 = false;
 		} else if(tmpActionType == MotionEvent.ACTION_UP){
 			if (InputDown1) {
 				nativeEventInputState(0, false, (float)event.getX(0), (float)event.getY(0));
 				InputDown1 = false;
 			} else if (InputDown2) {
 				nativeEventInputState(1, false, (float)event.getX(1), (float)event.getY(1));
 				InputDown2 = false;
 			} else {
 				nativeEventInputState(2, false, (float)event.getX(2), (float)event.getY(2));
 				InputDown3 = false;
 			}
 		} else {
 			nativeEventUnknow(tmpActionType);
 		}
 		return true;
 	}
 	
 	EwolRenderer mRenderer;
 }
 
 
 
 /**
  * @brief Class : 
  *
  */
 class EwolRenderer implements GLSurfaceView.Renderer {
 	private static native void nativeInit();
 	private static native void nativeResize(int w, int h);
 	private static native void nativeRender();
 	private static native void nativeDone();
 	
 	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 		nativeInit();
 	}
 	
 	public void onSurfaceChanged(GL10 gl, int w, int h) {
 		nativeResize(w, h);
 	}
 	
 	public void onDrawFrame(GL10 gl) {
 		nativeRender();
 	}
 }
 
