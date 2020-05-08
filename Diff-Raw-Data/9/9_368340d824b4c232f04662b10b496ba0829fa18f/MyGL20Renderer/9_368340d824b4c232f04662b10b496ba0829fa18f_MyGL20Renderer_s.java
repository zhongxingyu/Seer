 package org.newdawn.opengltest;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.opengl.GLES20;
 import android.opengl.GLSurfaceView;
 import android.opengl.Matrix;
 import android.os.SystemClock;
 
 public class MyGL20Renderer implements GLSurfaceView.Renderer {
 	private List<Mesh> meshes = new ArrayList<Mesh>();
 
     private final float[] mProjMatrix = new float[16];
     private final float[] mVMatrix = new float[16];
     private Square square;
     private Triangle triangle;
     private Cube cube;
 	
 	@Override
 	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
         // Set the background frame color
         GLES20.glClearColor(0f, 0f, 0f, 1.0f);
         GLES20.glEnable(GLES20.GL_CULL_FACE);
         GLES20.glEnable(GLES20.GL_DEPTH_TEST);
         
         square = new Square();
         triangle = new Triangle();
         cube = new Cube();
         
         meshes.add(square);
 		meshes.add(triangle);
 		meshes.add(cube);
     }
 
 	@Override
     public void onDrawFrame(GL10 unused) {
         // Redraw background color
         GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
         
         long time = SystemClock.uptimeMillis() % 2000;
         float offset = time/2000f;
         offset = (float) Math.sin(offset * 2 * Math.PI);
         
         square.setPosition(new float[] {offset,0,-0.1f});
         triangle.setPosition(new float[] {-offset,0,0});
         cube.setPosition(new float[] {0,-1+offset,-0.8f});
 
         for(Mesh mesh: meshes) {
         	mesh.draw(mVMatrix, mProjMatrix);
         }
     }
 
 	@Override
     public void onSurfaceChanged(GL10 unused, int width, int height) {
         GLES20.glViewport(0, 0, width, height);
         
         float ratio = (float) width / height;
 		float frustumH = (float) (Math.tan(60 / 360.0f * Math.PI) * 3);
 		float frustumW = frustumH * ratio;
 
 		Matrix.frustumM(mProjMatrix, 0, -frustumW, frustumW, -frustumH,
 				frustumH, 3, 300);
 		
         // Set the camera position (View matrix)
        Matrix.setLookAtM(mVMatrix, 0, 0, 0, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
 
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
 }
