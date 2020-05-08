 /************************************************\
 | Starts Obliterate, and passes on to the engine |
 |                                                |
 | @author David Saxon                            |
 \************************************************/
 package nz.co.withfire.obliterate;
 
 import nz.co.withfire.obliterate.R;
 import android.opengl.GLSurfaceView;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.text.Editable;
 import android.text.method.KeyListener;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnKeyListener;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class StartUpActivity extends Activity {
 
     //VARIABLES
     //the gl surface to render onto (for displaying the title)
     private extGLSurfaceView display;
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         
         //super call
         super.onCreate(savedInstanceState);
         
         //set to full screen mode
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
         
         //set the display
         display = new extGLSurfaceView(this);
         
         //set the content view to the display
         setContentView(display);
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
 
         if (keyCode == KeyEvent.KEYCODE_MENU) {
             
             if (!display.engine.isPaused()) {
                 
                 display.engine.settings = true;
                 return true;
             }
            else {
                
                display.engine.back = true;
                return true;
            }
         }
         if (keyCode == KeyEvent.KEYCODE_BACK) {
             
             if (display.engine.isPaused()) {
                 
                 display.engine.back = true;
                 return true;
             }
             else {
                 
                 super.onBackPressed();
                return true;
             }
         }
         
         
         return false;
     }
 }
 
 //TODO: turn this into a class
 //small class that extends GLSurfaceView
 class extGLSurfaceView extends GLSurfaceView {
     
     //VARIABLES
     //the engine
     public Engine engine;
     
     //CONSTRUCTOR
     public extGLSurfaceView(Context context) {
         
         //super call
         super(context);
         
         //create an OpenGL ES 2.0 context
         setEGLContextClientVersion(2);
         
         //create the engine
         engine = new Engine(context);
         
         setEGLConfigChooser(false);
         
         //set the renderer for drawing on this surface
         setRenderer(engine);
         
         setRenderMode(RENDERMODE_CONTINUOUSLY);
     }
     
     //METHODS
     @Override
     public boolean onTouchEvent(MotionEvent e) {
         
         //pass the event on to the engine
         engine.inputTouch(e);
         
         return true;
     }
 }
