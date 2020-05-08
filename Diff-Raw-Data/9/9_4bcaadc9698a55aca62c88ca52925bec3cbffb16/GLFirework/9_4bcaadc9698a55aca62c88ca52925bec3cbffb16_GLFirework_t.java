 package com.slsw.fiarworks.firework;
 
 import java.nio.IntBuffer;
 
 import android.opengl.GLES20;
 import android.opengl.Matrix;
 import java.nio.ByteBuffer;
 import java.nio.FloatBuffer;
 import java.nio.ByteOrder;
 
 /*
  * 
  */
 public class GLFirework
 {
 	static final int COORDS_PER_VERTEX = 3;
 	static final int TEX_COORDS_PER_VERTEX = 2;
 	static final int NORMAL_COORDS_PER_VERTEX = 3;
 
 	static final int tex_offset = (COORDS_PER_VERTEX + NORMAL_COORDS_PER_VERTEX) * 4;
 	static final int normal_offset = COORDS_PER_VERTEX * 4;
 	static final int vertex_offset = 0 * 4;
 	static final int stride = (COORDS_PER_VERTEX + TEX_COORDS_PER_VERTEX + NORMAL_COORDS_PER_VERTEX) * 4;
 
 	static private final String vertexShaderCode =
 		"attribute vec4 vPositionIn;" +
 		"attribute vec4 vColorIn;" +
 		"attribute vec2 vTextureIn;" +
 		"varying vec4 vColor;" +
 		"varying vec2 vTexture;" +
 		"uniform mat4 uMVPMatrix;" +
 		"void main() {" +
 		"  vColor = vColorIn;" +
 		"  vTexture = vTextureIn;" +
		"  gl_Position = uMVPMatrix * vPositionIn;" +
 		"}";
 
     static private final String fragmentShaderCode =
 		"precision mediump float;" +
 		"varying vec4 vColor;" +
 		"varying vec2 vTexture;" +
 		"void main() {" +
 		"  gl_FragColor = vColor;" +
 		"}";
 
     static int[] mGeometryBufferHandle = new int[1];
     static int mPosShaderLoc;
     static int mTexShaderLoc;
     static int mColShaderLoc;
     static int mMVPMatrixLoc;
 
     static int mNumGeometryFloats; 
 	static int mProgram;
 	GLFirework()
 	{
 		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
         int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
 
 		mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
         GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
         GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
         GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
         //error checking
         {
 			mPosShaderLoc = GLES20.glGetAttribLocation(mProgram, "vPositionIn");
 			if(mPosShaderLoc == -1)
 			{
 				System.out.println("mPosShaderLoc is -1. This is bad.");
 			}
 			mTexShaderLoc = GLES20.glGetAttribLocation(mProgram, "vTextureIn");
 			if(mTexShaderLoc == -1)
 			{
 				System.out.println("mTexShaderLoc is -1. This is bad.");
 			}
 			mColShaderLoc = GLES20.glGetAttribLocation(mProgram, "vColorIn");
 			if(mColShaderLoc == -1)
 			{
 				System.out.println("mColShaderLoc is -1. This is bad.");
 			}
 			mMVPMatrixLoc = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
 			if(mMVPMatrixLoc == -1)
 			{
 				System.out.println("mMVPMatrixLoc is -1. This is bad.");
 			}
 		}
 	}
 
 	public void updateFireworkAndDraw(float[] geometry, MatrixStack stackMV, float[] projection)
 	{
 		//fill geometry buffer
 
 		mNumGeometryFloats = geometry.length;
 		int numGeometryBytes = mNumGeometryFloats * 4;
 		System.out.println("Size of allocation: " + numGeometryBytes);
 		ByteBuffer bb = ByteBuffer.allocateDirect(numGeometryBytes);
 		bb.order(ByteOrder.nativeOrder());
 		FloatBuffer geometryBuffer = bb.asFloatBuffer();
 		geometryBuffer.put(geometry);
 		geometryBuffer.position(0);
 
 		GLES20.glGenBuffers(1, mGeometryBufferHandle, 0);
 		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mGeometryBufferHandle[0]);
 		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, numGeometryBytes, geometryBuffer, GLES20.GL_STATIC_DRAW);
 		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
 
 		//create ModelViewProjection matrix
 
 		float[] mvpMatrix = new float[16];
 		float[] mvMatrix = new float[16];
 		stackMV.getMatrix(mvMatrix, 0);
 		Matrix.multiplyMM(mvpMatrix, 0, mvMatrix, 0, projection, 0);
 
 		//set openGL state
 
 		GLES20.glUseProgram(mProgram);
 
 		GLES20.glUniformMatrix4fv(mMVPMatrixLoc, 1, false, mvpMatrix, 0);
 
 		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mGeometryBufferHandle[0]);
 
 		GLES20.glEnableVertexAttribArray(mPosShaderLoc);
 		GLES20.glVertexAttribPointer(mPosShaderLoc, 3, GLES20.GL_FLOAT, false, 8*4, 0*4);
 
 		GLES20.glEnableVertexAttribArray(mTexShaderLoc);
 		GLES20.glVertexAttribPointer(mTexShaderLoc, 2, GLES20.GL_FLOAT, false, 8*4, 3*4);
 
 		GLES20.glEnableVertexAttribArray(mColShaderLoc);
 		GLES20.glVertexAttribPointer(mColShaderLoc, 3, GLES20.GL_FLOAT, false, 8*4, 5*4);
 
 		//draw state
 
 		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mNumGeometryFloats);
 
 		//reset state
 
 		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
 	}
 
 	public int loadShader(int type, String shaderCode){
 
 		// create a vertex shader type (GLES20.GL_VERTEX_SHADER)
 		// or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
 		int shader = GLES20.glCreateShader(type);
 		System.out.println("Shader handle: " + shader);
 		int[] isCompiled = new int[1];
 		// add the source code to the shader and compile it
 		GLES20.glShaderSource(shader, shaderCode);
 		GLES20.glCompileShader(shader);
 		//complie error checking
 
 		GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, isCompiled, 0);
 		if(isCompiled[0] == 0){
 			System.out.println("Shader did not compile");
 			System.out.println("Sharder Log: ");
 			System.out.println(GLES20.glGetProgramInfoLog(shader));
 		}
 		return shader;
 	}
 
 }
