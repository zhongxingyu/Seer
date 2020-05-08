 package mse.tsm.mobop.starshooter;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.opengl.GLES20;
 import android.opengl.GLSurfaceView;
 import android.opengl.Matrix;
 import android.os.SystemClock;
 
 public class PlaygroundRenderer implements GLSurfaceView.Renderer
 {
   private ShipState myShip, opponentShip;
   
   private FloatBuffer triangleVB;
   
   private final String vertexShaderCode = 
       // This matrix member variable provides a hook to manipulate
       // the coordinates of the objects that use this vertex shader
       "uniform mat4 uMVPMatrix;   \n" +
       
       "attribute vec4 vPosition;  \n" +
       "void main(){               \n" +
       
       // the matrix must be included as a modifier of gl_Position
       " gl_Position = uMVPMatrix * vPosition; \n" +
       
       "}  \n";
   
   private final String fragmentShaderCode = 
       "precision mediump float;  \n" +
       "void main(){              \n" +
       " gl_FragColor = vec4 (0.63671875, 0.76953125, 0.22265625, 1.0); \n" +
       "}                         \n";
   
   private int mProgram;
   private int maPositionHandle;
   
   private int muMVPMatrixHandle;
   private float[] mMVPMatrix = new float[16];
   private float[] mMMatrix = new float[16];
   private float[] mVMatrix = new float[16];
   private float[] mProjMatrix = new float[16];
   public float mAngle;
   
   public PlaygroundRenderer(ShipState myShip, ShipState opponentShip)
   {
     this.myShip = myShip;
     this.opponentShip = opponentShip;
   }
   
   public void onSurfaceCreated(GL10 unused, EGLConfig config) {
     
     // Set the background frame color
     GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
     
     // initialize the triangle vertex array
     initShapes();
     
     int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
     int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
     
     mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
     GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
     GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
     GLES20.glLinkProgram(mProgram);                  // creates OpenGL program executables
     
     // get handle to the vertex shader's vPosition member
     maPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
   }
   
   public void onDrawFrame(GL10 unused) {
   
       // Redraw background color
       GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
       
       // Add program to OpenGL environment
       GLES20.glUseProgram(mProgram);
       
       // Prepare the triangle data
       GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 12, triangleVB);
       GLES20.glEnableVertexAttribArray(maPositionHandle);
       
       // Apply a ModelView Projection transformation
       //Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
       GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
       
    // Create a rotation for the triangle (Boring! Comment this out:)
       // long time = SystemClock.uptimeMillis() % 4000L;
       // float angle = 0.090f * ((int) time);
 
       // Use the mAngle member as the rotation value
       Matrix.setRotateM(mMMatrix, 0, mAngle, 0, 0, 1.0f);
       
       Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0);
       Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);
       
       // Apply a ModelView Projection transformation
       GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
      
       // Draw the triangle
       GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
   }
   
   public void onSurfaceChanged(GL10 unused, int width, int height) {
     GLES20.glViewport(0, 0, width, height);
     
     float ratio = (float) width / height;
     
     // this projection matrix is applied to object coodinates
     // in the onDrawFrame() method
     Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
     muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
     Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
   }
 
   private void initShapes(){
     
     float triangleCoords[] = {
         // X, Y, Z
        -0.5f, -0.25f, 0,
         0.5f, -0.25f, 0,
         0.0f,  0.559016994f, 0
     }; 
     
     // initialize vertex Buffer for triangle  
     ByteBuffer vbb = ByteBuffer.allocateDirect(
             // (# of coordinate values * 4 bytes per float)
             triangleCoords.length * 4); 
     vbb.order(ByteOrder.nativeOrder());// use the device hardware's native byte order
     triangleVB = vbb.asFloatBuffer();  // create a floating point buffer from the ByteBuffer
     triangleVB.put(triangleCoords);    // add the coordinates to the FloatBuffer
     triangleVB.position(0);            // set the buffer to read the first coordinate
 
   }
   
   private int loadShader(int type, String shaderCode){
     
     // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
     // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
     int shader = GLES20.glCreateShader(type); 
     
     // add the source code to the shader and compile it
     GLES20.glShaderSource(shader, shaderCode);
     GLES20.glCompileShader(shader);
     
     return shader;
   }
 }
