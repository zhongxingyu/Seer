 package com.discretesoftworks.framework;
 
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.opengl.GLES20;
 import android.opengl.GLSurfaceView;
 import android.opengl.GLUtils;
 import android.opengl.Matrix;
 import android.util.Log;
 
 import com.discretesoftworks.framework.Pool.PoolObjectFactory;
 import com.discretesoftworks.TestGame.R;
 
 public class MyGLRenderer extends GameRenderer implements GLSurfaceView.Renderer {
 
 	private static final String TAG = "MyRenderer";
 	
 	private static Context context;
 	
 	private final float[] mVPMatrix = new float[16];
     private final float[] mProjMatrix = new float[16];
     private final float[] mVMatrix = new float[16];
     //private final float[] mRotationMatrix = new float[16];
 	
 	
 	private float cX, cY; // Center x and y
 	private float cZ;	// Center z
 	
 	private boolean surfaceCreated;
 	
 	private long startTime;
 	private float deltaTime;
 	
 	private int frame, fps;
 	private long startFPSTime;
 	
 	private float[] bgColor = {
 								0.3f,
 								0.6f,
 								0.8f,
 								1.0f
 							  };
 	
 	private Pool<RenderModel> modelPool;
 	
 	private final float scale;
 	
 	public MyGLRenderer(AndroidGame game, final float scale){
 		s_instance = this;
 		cX = 0;
 		cY = 0;
 		context = (Context)game;
 		setGame(game);
 		surfaceCreated = false;
 		this.scale = scale;
 	}
 	
 	@Override
     public void onSurfaceCreated(GL10 unused, EGLConfig config) {
         GLES20.glClearColor(bgColor[0], bgColor[1], bgColor[2], bgColor[3]);
         surfaceCreated = true;
         
         Log.i(TAG,"Surface Created");
         
         Assets.load();
         
         PoolObjectFactory<RenderModel> factory = new PoolObjectFactory<RenderModel>() {
 			@Override
 			public RenderModel createObject() {
 				return new RenderModel();
 			}
 		};
 		modelPool = new Pool<RenderModel>(factory, 1000);
         
         startTime = System.nanoTime();
 		deltaTime = 1;
 		
 		startFPSTime = System.currentTimeMillis();
 		frame = fps = 1;
     }
 	
 	public void freeModel(RenderModel model){
 		modelPool.free(model);
 	}
 	
 	public AndroidGame getGame(){
 		return AndroidGame.s_instance;
 	}
 	
 	public int getViewWidth(){
 		return (int)(getScreenWidth() * getGame().getController().getView().getViewScale());
 	}
 	
 	public int getViewHeight(){
 		return (int)(getScreenHeight() * getGame().getController().getView().getViewScale());
 	}
 	
 	public float getViewScale(){
 		return getGame().getController().getView().getViewScale();
 	}
 	
 	public void setBGColor(float r, float g, float b, float a){
 		bgColor[0] = r;
 		bgColor[1] = g;
 		bgColor[2] = b;
 		bgColor[3] = a;
 		GLES20.glClearColor(bgColor[0], bgColor[1], bgColor[2], bgColor[3]);
 	}
 	
 	public boolean getSurfaceCreated(){
 		return surfaceCreated;
 	}
 	
 	public void setSurfaceCreated(boolean s){
 		surfaceCreated = s;
 	}
 	
 	public RenderModel getNewModel(float x, float y, Sprite sprite){
 		RenderModel square = modelPool.newObject();
 		square.resetColor();
 		square.set(x,y,sprite.getWidth(),sprite.getHeight(),sprite);
 		return square;
 	}
 	
 	@Override
 	public void update(float deltaTime){
 		super.update(deltaTime);
 	}
 	
     @Override
     public void onDrawFrame(GL10 unused) {
     	
     	deltaTime = (System.nanoTime() - startTime) / 10000000.000f;
 		startTime = System.nanoTime();
 		
 		if (deltaTime > 5.15){
 			deltaTime = (float)5.15;
 		}
     	
 		getGame().getController().update(deltaTime);
     	getGame().getController().paint();
     	
     	cX = getGame().getController().getView().getX();
     	cY = getGame().getController().getView().getY();
     	cZ = getGame().getController().getView().getZ();
     	
         GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
 
         // Set the camera position (View matrix)
         Matrix.setLookAtM(mVMatrix, 0,
         		0.0f, -10.0f, 7.0f,		// Eye
         		0.0f, 0.0f, 0.0f,	// Center
         		0.0f, 1.0f, 0.0f);	// Up
    
         // Calculate the projection and view transformation
         Matrix.multiplyMM(mVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
         
         for (int i = 0; i < s_instance.renderObjects.getCompiledData().size(); i++)
         	((GameObject)s_instance.renderObjects.getCompiledData().get(i)).draw(mVPMatrix, cX, cY, cZ);
         
         workFrameRate();
     }
     
     private void workFrameRate(){
 		frame += 1;
 		if (System.currentTimeMillis()-startFPSTime > 1000){
 			fps = frame < 60 ? frame : 60;
 			frame = 0;
 			startFPSTime = System.currentTimeMillis();
 			Log.i(context.getString(R.string.FPS),Integer.toString(fps));
 		}
 	}
 
     @Override
     public void onSurfaceChanged(GL10 unused, int width, int height) {
     	int w = (int)(width/scale);
     	int h = (int)(height/scale);
         GLES20.glViewport(0, 0, w, h);
         
         setScreenWidth(w);
         setScreenHeight(h);
         
         float ratio = (float) w / h;
         
         cX = w/2;
         cY = h/2;
 
        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        //Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
		Matrix.orthoM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
     }
     
     public static int loadShader(int type, String shaderCode){
 
         // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
         // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
         int shader = GLES20.glCreateShader(type);
 
         // add the source code to the shader and compile it
         GLES20.glShaderSource(shader, shaderCode);
         GLES20.glCompileShader(shader);
 
         return shader;
     }
 
     /**
      * Utility method for debugging OpenGL calls. Provide the name of the call
      * just after making it:
      *
      * <pre>
      * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
      * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
      *
      * If the operation is not successful, the check throws an error.
      *
      * @param glOperation - Name of the OpenGL call to check.
      */
     public static void checkGlError(String glOperation) {
         int error;
         while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
             Log.e(TAG, glOperation + ": glError " + error);
             throw new RuntimeException(glOperation + ": glError " + error);
         }
     }
     
     public static int loadTexture(Bitmap bitmap){
     	return loadTexture(context, bitmap);
     }
     
     public static int loadTexture(final Context context, Bitmap bitmap)
     {
         final int[] textureHandle = new int[1];
      
         GLES20.glGenTextures(1, textureHandle, 0);
      
         if (textureHandle[0] != 0)
         {
      
             // Bind to the texture in OpenGL
             GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
      
             // Set filtering
             GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
             GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
      
             // Load the bitmap into the bound texture.
             GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
      
             // Recycle the bitmap, since its data has been loaded into OpenGL.
             bitmap.recycle();
         }
      
         if (textureHandle[0] == 0)
         {
             throw new RuntimeException("Error loading texture.");
         }
      
         return textureHandle[0];
     }
 
 }
