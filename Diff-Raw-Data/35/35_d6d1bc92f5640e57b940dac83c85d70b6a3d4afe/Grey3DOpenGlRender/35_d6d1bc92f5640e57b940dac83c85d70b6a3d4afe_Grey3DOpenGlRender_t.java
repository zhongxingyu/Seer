 package com.example.ivcam.demo;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.opengl.GLES20;
 import android.opengl.GLSurfaceView;
 import android.opengl.Matrix;
 import android.os.SystemClock;
 
 public class Grey3DOpenGlRender  implements GLSurfaceView.Renderer 
 {
 	private float[] mModelMatrix = new float[16];
 	private float[] mViewMatrix = new float[16];
 	private float[] mProjectionMatrix = new float[16];
 	private float[] mMVPMatrix = new float[16];
 	private int mMVPMatrixHandle;
 	private int mPositionHandle;
 	private int mColorHandle;
 	private final int mBytesPerFloat = 4;
 	private final int mStrideBytes = mBytesPerFloat*3;
 	private final int mPositionOffset = 0;
 	private final int mPositionDataSize = 3;
 	private int mNumberOfVertices = 0;
 	public volatile float mDeltaX;					
 	public volatile float mDeltaY;	
 	private CalibrationDoublePrecision mCalibration;
 	private FloatBuffer mImageVertices;
 	
 	public Grey3DOpenGlRender(double[] calibration, int[] depth, int width, int height)
 	{	
 		mCalibration = new CalibrationDoublePrecision(calibration, CalibrationDoublePrecision.nParamters());
 		setVertices(depth, width, height);
 	}
 	
 	private Boolean truePoint(double x, double y, double z) {
 		if (x != 0.0 && y != 0.0 && z != 0.0 && x != -0.0 && y != -0.0
 				&& z != -0.0) {
 			return true;
 		}
 		return false;
 	}
 
 	private int getVerticesNumber(int[] depth, int width, int height) {
 		int i = 0;
 		int j = 0;
 		int counter = 0;
 		double[] x = new double[1];
 		double[] y = new double[1];
 		double[] z = new double[1];
 		for (; i < width; i++) {
 			for (j = 0; j < height; j++) {
 				mCalibration.unproject(i, j, depth[j * width + i], x, y, z);
 				if (truePoint(x[0],y[0],z[0])) {
 					counter++;
 				}
 			}
 		}
 		return counter;
 	}
 	
 	private void setVertices(int[] depth, int width, int height) {
 		int i = 0;
 		int j = 0;
 		int vId = 0;
 		double[] x = new double[1];
 		double[] y = new double[1];
 		double[] z = new double[1];
 		mNumberOfVertices = getVerticesNumber(depth, width, height);
 		int virticesRawSize = mNumberOfVertices*mStrideBytes;
 		float[] vrtRawData = new float[virticesRawSize/mBytesPerFloat];
 		for (; i < width; i++) {
 			for (j = 0; j < height; j++) {
 				mCalibration.unproject(i, j, depth[j * width + i], x, y, z);
 				if (truePoint(x[0], y[0], z[0])) {
					vrtRawData[vId++] = (float) (x[0]);
					vrtRawData[vId++] = (float) (y[0]);
					vrtRawData[vId++] = (float) (z[0]);
 				}
 			}
 		}
 		mImageVertices = ByteBuffer
 				.allocateDirect(vrtRawData.length * mBytesPerFloat)
 				.order(ByteOrder.nativeOrder()).asFloatBuffer();
 		mImageVertices.put(vrtRawData).position(0);
 	}
 	
 	@Override
 	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) 
 	{
 		// Set the background clear color to gray.
 		GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);
 		// Position the eye behind the origin.
 		final float eyeX = 0.0f;
 		final float eyeY = 0.0f;
 		final float eyeZ = 1.5f;
 		// We are looking toward the distance
 		final float lookX = 0.0f;
 		final float lookY = 0.0f;
 		final float lookZ = -5.0f;
 		// Set our up vector. This is where our head would be pointing were we holding the camera.
 		final float upX = 0.0f;
 		final float upY = 1.0f;
 		final float upZ = 0.0f;
 		// Set the view matrix. This matrix can be said to represent the camera position.
 		Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
 		final String vertexShader =
 			"uniform mat4 u_MVPMatrix;      \n"		// A constant representing the combined model/view/projection matrix.
 		  + "attribute vec4 a_Position;     \n"		// Per-vertex position information we will pass in.
  
 		  + "void main()                    \n"		// The entry point for our vertex shader.
 		  + "{                              \n"
 		  + "   gl_Position = u_MVPMatrix   \n" 	// gl_Position is a special variable used to store the final position.
 		  + "               * a_Position;   \n"     // Multiply the vertex by the matrix to get the final point in 			                                            			 
 		  + "}                              \n";    // normalized screen coordinates.
 		final String fragmentShader =
 			"precision mediump float;       \n"		// Set the default precision to medium. We don't need as high of a 
 													// precision in the fragment shader.				
 		  + "uniform vec4 v_Color;          \n"		// This is the color from the vertex shader interpolated across the 
 		  											// triangle per fragment.			  
 		  + "void main()                    \n"		// The entry point for our fragment shader.
 		  + "{                              \n"
 		  + "   gl_FragColor = v_Color;     \n"		// Pass the color directly through the pipeline.		  
 		  + "}                              \n";												
 		// Load in the vertex shader.
 		int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
 		if (vertexShaderHandle != 0) 
 		{
 			// Pass in the shader source.
 			GLES20.glShaderSource(vertexShaderHandle, vertexShader);
 			// Compile the shader.
 			GLES20.glCompileShader(vertexShaderHandle);
 			// Get the compilation status.
 			final int[] compileStatus = new int[1];
 			GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
 			// If the compilation failed, delete the shader.
 			if (compileStatus[0] == 0) 
 			{				
 				GLES20.glDeleteShader(vertexShaderHandle);
 				vertexShaderHandle = 0;
 			}
 		}
 		if (vertexShaderHandle == 0)
 		{
 			throw new RuntimeException("Error creating vertex shader.");
 		}
 		// Load in the fragment shader shader.
 		int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
 		if (fragmentShaderHandle != 0) 
 		{
 			// Pass in the shader source.
 			GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);
 			// Compile the shader.
 			GLES20.glCompileShader(fragmentShaderHandle);
 			// Get the compilation status.
 			final int[] compileStatus = new int[1];
 			GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
 			// If the compilation failed, delete the shader.
 			if (compileStatus[0] == 0) 
 			{				
 				GLES20.glDeleteShader(fragmentShaderHandle);
 				fragmentShaderHandle = 0;
 			}
 		}
 		if (fragmentShaderHandle == 0)
 		{
 			throw new RuntimeException("Error creating fragment shader.");
 		}
 		// Create a program object and store the handle to it.
 		int programHandle = GLES20.glCreateProgram();
 		if (programHandle != 0) 
 		{
 			// Bind the vertex shader to the program.
 			GLES20.glAttachShader(programHandle, vertexShaderHandle);			
 			// Bind the fragment shader to the program.
 			GLES20.glAttachShader(programHandle, fragmentShaderHandle);
 			// Bind attributes
 			GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
 			GLES20.glBindAttribLocation(programHandle, 1, "v_Color");
 			// Link the two shaders together into a program.
 			GLES20.glLinkProgram(programHandle);
 			// Get the link status.
 			final int[] linkStatus = new int[1];
 			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
 			// If the link failed, delete the program.
 			if (linkStatus[0] == 0) 
 			{				
 				GLES20.glDeleteProgram(programHandle);
 				programHandle = 0;
 			}
 		}
 		if (programHandle == 0)
 		{
 			throw new RuntimeException("Error creating program.");
 		}
         // Set program handles. These will later be used to pass in values to the program.
         mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");        
         mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
         mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");        
         // Tell OpenGL to use this program when rendering.
         GLES20.glUseProgram(programHandle);        
 	}	
 	
 	@Override
 	public void onSurfaceChanged(GL10 glUnused, int width, int height) 
 	{
 		// Set the OpenGL viewport to the same size as the surface.
 		GLES20.glViewport(0, 0, width, height);
 		// Create a new perspective projection matrix. The height will stay the same
 		// while the width will vary as per aspect ratio.
 		final float ratio = (float) width / height;
 		final float left = -ratio;
 		final float right = ratio;
 		final float bottom = -1.0f;
 		final float top = 1.0f;
 		final float near = 1.0f;
 		final float far = 10.0f;
 		Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
 	}	
 
 	@Override
 	public void onDrawFrame(GL10 glUnused) 
 	{
 		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);			        
         // Draw the triangle facing straight on.
         Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, mDeltaX, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, mDeltaY, 1.0f, 0.0f, 0.0f);
         drawImageAsPointsCloud(mImageVertices);
      }	
 	
 	private void drawImageAsPointsCloud(FloatBuffer buffer)
 	{	
 		float color[] = {0.0f,0.5f,0.0f,1.0f};
 		GLES20.glUniform4fv(mColorHandle, 1, color, 0);
 		buffer.position(mPositionOffset);
         GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
         		mStrideBytes, buffer);        
         GLES20.glEnableVertexAttribArray(mPositionHandle);        
         Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
         Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
         GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
         GLES20.glDrawArrays(GLES20.GL_POINTS, 0, mNumberOfVertices);                               
 	}
 }
