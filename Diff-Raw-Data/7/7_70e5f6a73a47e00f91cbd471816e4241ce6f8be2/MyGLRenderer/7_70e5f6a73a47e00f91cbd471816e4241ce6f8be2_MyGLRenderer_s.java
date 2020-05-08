 /*
  * Copyright (C) 2012 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.example.android.opengl;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.nio.ShortBuffer;
 import java.util.Arrays;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.opengl.GLES20;
 import android.opengl.GLSurfaceView;
 import android.opengl.GLU;
 import android.opengl.Matrix;
 import android.os.AsyncTask;
 import android.util.Log;
 
 
 public class MyGLRenderer implements GLSurfaceView.Renderer {
 
     public static final String TAG = "OO";
     private Shader shader;
     private VBO vbo;
     private boolean m_hasChunk = false;
     //DataFetcher m_dataFetcher;
 
     private final String vertexShaderCode =
         // This matrix member variable provides a hook to manipulate
         // the coordinates of the objects that use this vertex shader
         "uniform mat4 mvMatrix;" +
         "uniform mat4 pMatrix;" +
         "attribute vec3 vertex;" +
         "varying float height;" +
 
         "void main() {" +
         "	gl_PointSize = 3.0;" +
         "	height = vertex.y;" +
         // the matrix must be included as a modifier of gl_Position
         "  gl_Position = pMatrix * mvMatrix * vec4(vertex,1.0);" +
         "}";
 
     private final String fragmentShaderCode =
         "precision mediump float;" +
         "varying float height;" +
         "void main() {" +
         "  gl_FragColor = vec4(height*10.0,height*5.0,1.0-abs(height*2.0 + .1),1);" +
         "}";
     
     private float[] pMatrix = new float[16];
     private float[] mvMatrix = new float[16];
     private float[] tmpMatrix = new float[16];
     // Declare as volatile because we are updating it from another thread
     public volatile float mxAngle;
     public volatile float myAngle;
 
     public void onSurfaceCreated(GL10 unused, EGLConfig config) {
 
         // Set the background frame color
     	
     	Log.d(TAG, "==============\nStarting glstuff");
     	
         GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
         GLES20.glEnable(GLES20.GL_DEPTH_TEST);
         shader = new Shader(vertexShaderCode, fragmentShaderCode);
         checkGlError("Before vbo init");
         vbo = new VBO(shader.getProgram());
 
         
         //m_dataFetcher = new DataFetcher();
         //m_dataFetcher.execute("banana");
         
         Matrix.setIdentityM(pMatrix, 0);
         Matrix.perspectiveM(pMatrix, 0, 60.0f, 1.0f, 1f,100.0f);
         
         int div = 100;
 
        float scale = .05f;
         
         int vcount = 0;
         int icount = 0;
         
         float vertices[] = new float[3*4*div*div];
         int indices[] = new int[6*div*div];
         float vals[][] = new float[div][div];
         
         for (int i = 0; i < div; i ++){
        	for (int j = 0; j < div; j+=3){
        		vals[i][j] = (float)Math.sin(i * j * .002) * .2f;
         	}
         }
 		for (int i = 0; i < div-1; i ++){
         	for (int j = 0; j < div-1; j++){
         		
         		int tl, tr, bl, br;
         		
         		float x = (i-div/2) * scale;
         		float z = (j-div/2) * scale;
         		
         		tl = vcount/3;
         		
         		vertices[vcount] = x;
         		vertices[vcount+1] = vals[i][j];
         		vertices[vcount+2] = z;
         		
         		vcount +=3;
         		
         		br = vcount/3;
         		
         		vertices[vcount] = x+1*scale;
         		vertices[vcount+1] = vals[i+1][j+1];
         		vertices[vcount+2] = z+1*scale;
         		
         		vcount +=3;
         		
         		bl = vcount/3;
         		
         		vertices[vcount] = x;
         		vertices[vcount+1] = vals[i][j+1];
         		vertices[vcount+2] = z+1*scale;
         		
         		vcount +=3;
         		
         		tr = vcount/3;
         		
         		vertices[vcount] = x+1*scale;
         		vertices[vcount+1] = vals[i+1][j];
         		vertices[vcount+2] = z;
         		
         		vcount +=3;
         		
         		indices[icount] = tl;
         		icount++;
         		indices[icount] = tr;
         		icount++;
         		indices[icount] = br;
         		icount++;
         		
         		indices[icount] = tl;
         		icount++;
         		indices[icount] = bl;
         		icount++;
         		indices[icount] = br;
         		icount++;
         		
         		
         	}
         	
         }
         
         
       
         
         vbo.setBuffers(vertices, indices);
         
     }
 
     public void onDrawFrame(GL10 unused) {
 
         /*if(m_dataFetcher.getStatus() == AsyncTask.Status.FINISHED && !m_hasChunk ) {
             float[] vertex_data = m_dataFetcher.getVertexData();
             
             int[] index_data = new int[vertex_data.length / 6];
 
             for(int i = 0; i < index_data.length; i+=2) {
                 index_data[i] = i;
             }
 
             vbo.setBuffers(vertex_data, index_data);
             MyGLRenderer.checkGlError("vbo setBuffers");
             m_hasChunk = true;
             Log.d(MyGLRenderer.TAG, "Vertex buffer complete");
         }*/
 
         // Draw background color
         GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
         
         Matrix.setIdentityM(mvMatrix, 0);
         
         Matrix.translateM(mvMatrix, 0, 0.0f,0.0f, -5.0f);
         
         Matrix.rotateM(mvMatrix, 0, mxAngle, 1.0f, 0.0f, 0.0f);
         Matrix.rotateM(mvMatrix, 0, myAngle, 0, 1.0f, 0.0f);
         
         //Matrix.translateM(mvMatrix, 0, -2.0f,0f, -1.0f);
         
         // Draw triangle
         shader.useProgram();
         
         shader.setMatrices(mvMatrix, pMatrix);
         
         vbo.draw();
     }
 
     public void onSurfaceChanged(GL10 unused, int width, int height) {
         // Adjust the viewport based on geometry changes,
         // such as screen rotation
         GLES20.glViewport(0, 0, width, height);
 
         float ratio = (float) width / height;
 
         // this projection matrix is applied to object coordinates
         // in the onDrawFrame() method
         Matrix.setIdentityM(pMatrix, 0);
         Matrix.perspectiveM(pMatrix, 0, 60.0f, ratio, .01f,100.0f);
         //Matrix.orthoM(pMatrix, 0, -2*ratio, 2*ratio, -2, 2, -4, 4);
 
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
             Log.e(TAG, glOperation + ": glError " + GLU.gluErrorString(error));
             //throw new RuntimeException(glOperation + ": glError " + error);
         }
     }
 }
