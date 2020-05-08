 package com.example.gpu_cw1;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.opengl.GLES20;
 import android.opengl.GLSurfaceView;
 import android.util.Log;
 
 public class MyGL20Renderer implements GLSurfaceView.Renderer {
 	private Triangle mTriangle;
 	
 	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
 		// Set the background frame color
 		GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
 
 		// Initialise a triangle
 		mTriangle = new Triangle();
 	}
 
 	public void onDrawFrame(GL10 unused) {
		// Redraw background color
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
 	}
 
 	public void onSurfaceChanged(GL10 unused, int width, int height) {
 		GLES20.glViewport(0, 0, width, height);
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
             Log.e("MYGL", glOperation + ": glError " + error);
             throw new RuntimeException(glOperation + ": glError " + error);
         }
     }
 }
