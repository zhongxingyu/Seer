 package at.demos;
 
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.content.Context;
 import android.content.pm.ConfigurationInfo;
 import android.opengl.GLSurfaceView;
 import android.os.Bundle;
 import android.view.MotionEvent;
 
 public class SimpleLightActivity extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         if (detectOpenGLES20()) {
         	mGlView = new SimpleLightView(this);
             setContentView(mGlView);
             mGlView.requestFocus();
             mGlView.setFocusableInTouchMode(true);
         }
         else {
         	mGlView = null;
         	setContentView(R.layout.gl_error);
         }
     }
     
     private boolean detectOpenGLES20() {
         ActivityManager am =
             (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
         ConfigurationInfo info = am.getDeviceConfigurationInfo();
         return (info.reqGlEsVersion >= 0x20000);
     }
     
     /* (non-Javadoc)
 	 * @see android.app.Activity#onPause()
 	 */
 	@Override
 	protected void onPause() {
 		super.onPause();
 		
 		if (mGlView != null) {
 			mGlView.onPause();
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onResume()
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		if (mGlView != null) {
 			mGlView.onResume();
 		}
 	}
 
 
 	private SimpleLightView mGlView;
 }
 
 class SimpleLightView extends GLSurfaceView {
 	
 	public SimpleLightView(Context context) {
 	    super(context);
 	    
         mRenderer = new SimpleLightRenderer(context);
         setEGLContextClientVersion(2);
         setRenderer(mRenderer);
         setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
     }
 
     @Override public boolean onTrackballEvent(MotionEvent e) {
         mRenderer.mAngleX += e.getX() * TRACKBALL_SCALE_FACTOR;
        mRenderer.mAngleY += e.getY() * TRACKBALL_SCALE_FACTOR;
         
         return true;
     }
 
     @Override public boolean onTouchEvent(MotionEvent e) {
         float x = e.getX();
         float y = e.getY();
         switch (e.getAction()) {
         case MotionEvent.ACTION_MOVE:
             mRenderer.mAngleX += (x - mPreviousX) * TOUCH_SCALE_FACTOR;
            mRenderer.mAngleY += (y - mPreviousY) * TOUCH_SCALE_FACTOR;
         }
         mPreviousX = x;
         mPreviousY = y;
         
         return true;
     }
     
     
     // Touch constants
     private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
     private final float TRACKBALL_SCALE_FACTOR = 36.0f;
     
     private SimpleLightRenderer mRenderer;
     
     // Touch info
     private float mPreviousX;
     private float mPreviousY;
 }
