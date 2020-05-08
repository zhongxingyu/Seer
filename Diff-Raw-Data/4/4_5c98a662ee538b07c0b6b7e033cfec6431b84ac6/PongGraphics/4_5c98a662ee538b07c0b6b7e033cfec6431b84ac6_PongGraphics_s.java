 package arpong.graphics;
 
 import android.content.Context;
 import android.opengl.GLES20;
 import android.opengl.Matrix;
 import arpong.common.GameInterface;
 import arpong.logic.ar.VirtualRealityRenderer;
 import arpong.common.GraphicsInterface;
 import arpong.graphics.Models.FieldModel;
 import arpong.graphics.Models.PaddleModel;
 import arpong.graphics.Models.Sphere;
 import arpong.graphics.Utils.Programs;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 public class PongGraphics extends RenderBase implements GraphicsInterface, VirtualRealityRenderer {
     private final float[] projectionMatrix = new float[16];
     private final float[] modelviewMatrix = new float[16];
     private int projectionMatrixId, modelviewMatrixId, colorId;
     private int programId;
 
     final private int[] bindings = new int[StaticModel.NUM_BUFFERS];
     private StaticModel fieldModel, ballModel, paddleModel;
 
     private Coord2D ballCoord = new Coord2D(0.0f, 0.0f);
 
     private static final int NUM_PADDLES = 2;
     private Coord2D[] paddleCoords = new Coord2D[NUM_PADDLES];
 
     // Field parameters
     private float fieldMinX = 0.0f, fieldMaxX = 1.0f, fieldMinY = 0.0f, fieldMaxY = 1.0f;
     private float ballRadius = 0.05f;
     private float[] paddleSizeX = new float[NUM_PADDLES], paddleSizeY = new float[NUM_PADDLES];
 
     // FPS count
     private FPSCounter fpsCounter = new FPSCounter(5000, "JavaGL", "Render fps: ");
 
     private static final String kVertexShader =
             "precision mediump float; \n" +
                     "uniform mat4 projection; \n" +
                     "uniform mat4 modelview; \n" +
                     "attribute vec3 position; \n" +
                     "void main() { \n" +
                     "  gl_Position = projection * modelview * vec4(position, 1.0); \n" +
                     "}";
 
     private static final String kFragmentShader =
             "precision mediump float; \n" +
                     "uniform vec3 color; \n" +
                     "void main() { \n" +
                     "  gl_FragColor = vec4(color, 1.0); \n" +
                     "}";
 
     public PongGraphics(Context context) {
         super(context);
         for (int i = 0; i < NUM_PADDLES; ++i) {
             paddleCoords[i] = new Coord2D(0.0f, 0.0f);
             paddleSizeX[i] = paddleSizeY[i] = 0.0f;
         }
     }
 
     public void onSurfaceCreated(GL10 unused, EGLConfig config) {
         GLES20.glClearColor(0.0f, 0.0f, 0.5f, 1.0f);
         GLES20.glClearDepthf(1.0f);
         GLES20.glEnable(GLES20.GL_DEPTH_TEST);
         GLES20.glDepthFunc(GLES20.GL_LEQUAL);
 
         programId = Programs.loadProgram(kVertexShader, kFragmentShader);
         bindings[StaticModel.BUFFER_VERTS] = GLES20.glGetAttribLocation(programId, "position");
         GLES20.glEnableVertexAttribArray(bindings[StaticModel.BUFFER_VERTS]);
 
         projectionMatrixId = GLES20.glGetUniformLocation(programId, "projection");
         modelviewMatrixId  = GLES20.glGetUniformLocation(programId, "modelview" );
         colorId = GLES20.glGetUniformLocation(programId, "color");
 
         fieldModel = new FieldModel();
         ballModel = new Sphere(8, 1.0f);
         paddleModel = new PaddleModel();
     }
 
     public void onDrawFrame(GL10 unused) {
         float ballX, ballY;
         synchronized (ballCoord) {
             ballX = ballCoord.x;
             ballY = ballCoord.y;
         }
         float[] paddleX = new float[NUM_PADDLES];
         float[] paddleY = new float[NUM_PADDLES];
         synchronized (paddleCoords) {
             for (int i = 0; i < NUM_PADDLES; ++i) {
                 paddleX[i] = paddleCoords[i].x;
                 paddleY[i] = paddleCoords[i].y;
             }
         }
 
         final float spanX = fieldMaxX - fieldMinX, spanY = fieldMaxY - fieldMinY;
         //final float spanZ = spanY * 0.1f;
 
         GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
         GLES20.glUseProgram(programId);
 
         GLES20.glUniformMatrix4fv(projectionMatrixId, 1, false, projectionMatrix, 0);
 
         final float[] fieldModelView = modelviewMatrix.clone();
         Matrix.translateM(fieldModelView, 0, -spanX * 0.5f, -spanY * 0.5f, 0.0f);
         Matrix.translateM(fieldModelView, 0, -fieldMinX, -fieldMinY, 0.0f);
 
         float[] modelview1;
         modelview1 = fieldModelView.clone();
         Matrix.translateM(modelview1, 0, fieldMinX, fieldMinY, 0.0f);
         Matrix.scaleM(modelview1, 0, spanX, spanY, 1.0f);
         GLES20.glUniformMatrix4fv(modelviewMatrixId, 1, false, modelview1, 0);
 
         GLES20.glUniform3f(colorId, 0.0f, 0.25f, 0.0f);
         fieldModel.draw(bindings);
 
         modelview1 = fieldModelView.clone();
         Matrix.translateM(modelview1, 0, ballX, ballY, ballRadius);
         Matrix.scaleM(modelview1, 0, ballRadius, ballRadius, ballRadius);
         GLES20.glUniformMatrix4fv(modelviewMatrixId, 1, false, modelview1, 0);
 
         GLES20.glUniform3f(colorId, 1.0f, 0.5f, 0.0f);
         ballModel.draw(bindings);
 
         for (int i = 0; i < NUM_PADDLES; ++i) {
             modelview1 = fieldModelView.clone();
             Matrix.translateM(modelview1, 0, paddleX[i], paddleY[i], ballRadius);
             Matrix.scaleM(modelview1, 0, paddleSizeX[i] * 0.5f, paddleSizeY[i] * 0.5f, ballRadius);
             GLES20.glUniformMatrix4fv(modelviewMatrixId, 1, false, modelview1, 0);
 
             GLES20.glUniform3f(colorId, 1.0f, 1.0f, 1.0f);
             paddleModel.draw(bindings);
         }
 
         //paddleModel.draw(bindings);
 
         fpsCounter.update();
     }
 
     public void onSurfaceChanged(GL10 unused, int width, int height) {
         final float[] modelview = new float[16], projection = new float[16];
 
         GLES20.glViewport(0, 0, width, height);
 
         Matrix.setIdentityM(projection, 0);
         Matrix.perspectiveM(projection, 0, 45.0f, (float)width / (float)height, 0.1f, 1000.0f);
 
         final float ofs = Math.max(fieldMaxX - fieldMinX, fieldMaxY - fieldMinY);
         Matrix.setIdentityM(modelview, 0);
        Matrix.translateM(modelview, 0, 0.0f, 0.0f, -1.0f * ofs);
        Matrix.rotateM(modelview, 0, -30.0f, 1.0f, 0.0f, 0.0f);
 
         updateProjectionGlobal(modelview, projection);
     }
 
     @Override
     public void updateProjectionGlobal(float[] modelview, float[] projection) {
         for (int i = 0; i < 16; ++i) {
             modelviewMatrix[i] = modelview[i];
             projectionMatrix[i] = projection[i];
         }
     }
 
     @Override
     public void updatePlayerPaddleLocal(int paddleId, float x, float y) {
         synchronized (paddleCoords) {
             paddleCoords[paddleId].x = x;
             paddleCoords[paddleId].y = y;
         }
     }
 
     @Override
     public void updateBallLocal(float x, float y) {
         synchronized (ballCoord) {
             ballCoord.x = x;
             ballCoord.y = y;
         }
     }
 
     public void setGame(GameInterface game) {
         fieldMinX = game.getXMins();
         fieldMinY = game.getYMins();
         fieldMaxX = game.getXMaxs();
         fieldMaxY = game.getYMaxs();
         ballRadius = game.getBallRadius();
 
         for (int i = 0; i < NUM_PADDLES; ++i) {
             paddleSizeX[i] = game.getPaddleXSize(i);
             paddleSizeY[i] = game.getPaddleYSize(i);
         }
     }
 
 }
