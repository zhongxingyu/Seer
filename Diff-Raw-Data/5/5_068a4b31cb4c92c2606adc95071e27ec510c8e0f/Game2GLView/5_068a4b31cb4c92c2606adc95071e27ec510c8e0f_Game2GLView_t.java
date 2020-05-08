 package com.hyston.games.game2;
 
 import android.content.Context;
 import android.graphics.PixelFormat;
import android.opengl.GLES20;
 import android.opengl.GLSurfaceView;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 
 import javax.microedition.khronos.egl.EGL10;
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.egl.EGLContext;
 import javax.microedition.khronos.egl.EGLDisplay;
 import javax.microedition.khronos.opengles.GL10;
 
 public class Game2GLView extends GLSurfaceView 
 {
 	private static String TAG = "Game2GLView";
 
 	public Game2GLView(Context context) 
 	{
 		super(context);
 		init(false, 0, 0);
 	}
 	
 	private void init(boolean translucent, int depth, int stencil)
 	{
 		if(translucent)
 			this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
 		
 		setEGLContextFactory(new ContextFactory());
 		
 		setEGLConfigChooser( translucent ?
                 new ConfigChooser(8, 8, 8, 8, depth, stencil) :
                 new ConfigChooser(5, 6, 5, 0, depth, stencil) );
 		
 		setRenderer(new Renderer());
 		
 	}
 	
 	public boolean onTouchEvent(final MotionEvent event)
 	{		
 		//int pointers_count = event.getPointerCount();
 		int Action = event.getAction();
 		//float x = event.getX();
 		//float y = event.getY();
 		
         switch(Action & MotionEvent.ACTION_MASK)
         {
             case MotionEvent.ACTION_DOWN:
             	Game2JNI.ontouch(0, 0, event.getX(), event.getY());
             break;
             
             case MotionEvent.ACTION_MOVE:
             	Game2JNI.ontouch(0, 1, event.getX(), event.getY());
             break;
             
             case MotionEvent.ACTION_UP:
             	Game2JNI.ontouch(0, 2, event.getX(), event.getY());
             break;
             
             case MotionEvent.ACTION_POINTER_DOWN:
             	Game2JNI.ontouch(0, 0, event.getX(), event.getY());
             break;
             	
             case MotionEvent.ACTION_POINTER_UP:
             	Game2JNI.ontouch(0, 2, event.getX(), event.getY());
             break;
             	
         }
         
 		return true;		
 	}
 
 	
 	private static class ContextFactory implements GLSurfaceView.EGLContextFactory
 	{
 		//TODO: google, what is it!
 		private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
 		
 		public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) 
 		{
             Log.w(TAG, "creating OpenGL ES 2.0 context");
             //checkEglError("Before eglCreateContext", egl);
             int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE };
             EGLContext context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
             //checkEglError("After eglCreateContext", egl);
             return context;
         }
 
         public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) 
         {
         	egl.eglDestroyContext(display, context);
         }
 	}
 	
 	
 	private static class ConfigChooser implements GLSurfaceView.EGLConfigChooser
 	{
 		// Subclasses can adjust these values:
         protected int mRedSize;
         protected int mGreenSize;
         protected int mBlueSize;
         protected int mAlphaSize;
         protected int mDepthSize;
         protected int mStencilSize;
         private int[] mValue = new int[1];
 		
 		public ConfigChooser(int r, int g, int b, int a, int depth, int stencil)
 		{
             mRedSize = r;
             mGreenSize = g;
             mBlueSize = b;
             mAlphaSize = a;
             mDepthSize = depth;
             mStencilSize = stencil;
         }
 		
 		private static int EGL_OPENGL_ES2_BIT = 4;
         private static int[] s_configAttribs2 =
         {
         	EGL10.EGL_RED_SIZE, 5,
             EGL10.EGL_GREEN_SIZE, 6,
             EGL10.EGL_BLUE_SIZE, 5,
             EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
             EGL10.EGL_NONE
         };
         
         public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) 
         {
 
             /* Get the number of minimally matching EGL configurations
              */
             int[] num_config = new int[1];
             egl.eglChooseConfig(display, s_configAttribs2, null, 0, num_config);
 
             int numConfigs = num_config[0];
 
             if (numConfigs <= 0) {
                 throw new IllegalArgumentException("No configs match configSpec");
             }
 
             /* Allocate then read the array of minimally matching EGL configs
              */
             EGLConfig[] configs = new EGLConfig[numConfigs];
             egl.eglChooseConfig(display, s_configAttribs2, configs, numConfigs, num_config);
            
             /* Now return the "best" one
              */
             return chooseConfig(egl, display, configs);
         }
         
         public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                 EGLConfig[] configs) 
         {
             for(EGLConfig config : configs) 
             {
                 int d = findConfigAttrib(egl, display, config,
                         EGL10.EGL_DEPTH_SIZE, 0);
                 int s = findConfigAttrib(egl, display, config,
                         EGL10.EGL_STENCIL_SIZE, 0);
 
                 // We need at least mDepthSize and mStencilSize bits
                 if (d < mDepthSize || s < mStencilSize)
                     continue;
 
                 // We want an *exact* match for red/green/blue/alpha
                 int r = findConfigAttrib(egl, display, config,
                         EGL10.EGL_RED_SIZE, 0);
                 int g = findConfigAttrib(egl, display, config,
                             EGL10.EGL_GREEN_SIZE, 0);
                 int b = findConfigAttrib(egl, display, config,
                             EGL10.EGL_BLUE_SIZE, 0);
                 int a = findConfigAttrib(egl, display, config,
                         EGL10.EGL_ALPHA_SIZE, 0);
 
                 if (r == mRedSize && g == mGreenSize && b == mBlueSize && a == mAlphaSize)
                     return config;
             }
             return null;
         }
         
         private int findConfigAttrib(EGL10 egl, EGLDisplay display,
                 EGLConfig config, int attribute, int defaultValue) 
         {
 
             if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) 
             {
                 return mValue[0];
             }
             return defaultValue;
         }
 
 	}
 	
 	
 	private static class Renderer implements GLSurfaceView.Renderer
 	{		
 	        public void onDrawFrame(GL10 gl) 
 	        {
 	        	Game2JNI.draw();
 	            Game2JNI.step();
 	        }
 
 	        public void onSurfaceChanged(GL10 gl, int width, int height) 
 	        {
	        	//TODO: may be separate init() and surfChange ?
	        	Game2JNI.init(width, height); 
 	        }
 
 	        public void onSurfaceCreated(GL10 gl, EGLConfig config) 
 	        {
 	            // Do nothing.
 	        }
 	    
 	}
 
 }
