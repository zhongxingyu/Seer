 package io.indy.beepboard.gfx;
 
 import io.indy.beepboard.logic.Cursor;
 import io.indy.beepboard.logic.Grid;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.opengl.GLUtils;
 import android.util.Log;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import javax.microedition.khronos.opengles.GL10;
 
 public class GLCursor
 {
     private static final String TAG = "GLCursor";
 
     private GLRenderer glRenderer;
     private Cursor logicalCursor;
 
     private FloatBuffer vertexBuffer;
 
     public GLCursor(GLRenderer g)
     {
         glRenderer = g;
     }
 
     public void setLogicalCursor(Cursor g)
     {
         logicalCursor = g;
     }
 
     public void setup(GL10 gl, float width, float height, float fov)
     {
         logicalCursor.dimensionChanged(width, height);
         vertexBuffer = asVertexBuffer(generateCursorVertices());
     }
 
     public void draw(GL10 gl)
     {
         gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
         gl.glColor4f(0f, 1f, 0f, 0.9f);
         gl.glNormal3f(0f, 0f, 1f);
         gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
     }
 
 
 
     private float[] generateCursorVertices()
     {
         float planeHeight = glRenderer.getPlaneHeight();
         float planeWidth = glRenderer.getPlaneWidth();
         float planeMaxSize = glRenderer.getPlaneMaxSize();
         float planeDistance = glRenderer.getPlaneDistance();
 
         Grid logicalGrid = logicalCursor.getGrid();
         int gridWidth = logicalGrid.getGridWidth();
 
         float cursorWidth = planeWidth / (float)gridWidth;
 
 
         float xOffset = -(planeMaxSize / 2f);
         float yOffset = -(planeMaxSize / 2f);;
 
         float xOrigin, yOrigin, zOrigin;
         int i, j, tBase;
 
         int tileNumCorners = 4;
         int tileDimensions = 3;
         float[] vertices;
         vertices = new float[tileNumCorners * tileDimensions];
 
         xOrigin = xOffset;
         yOrigin = yOffset;
         zOrigin = -planeDistance - 5f;
 
         vertices[ 0] = xOrigin;
         vertices[ 1] = yOrigin;
         vertices[ 2] = zOrigin;
 
         Log.d(TAG, "cursorWidth: "+cursorWidth);
 
         vertices[ 3] = xOrigin + cursorWidth;
         vertices[ 4] = yOrigin;
         vertices[ 5] = zOrigin;
 
         vertices[ 6] = xOrigin;
        vertices[ 7] = yOrigin + planeHeight;
         vertices[ 8] = zOrigin;
 
         vertices[ 9] = xOrigin + cursorWidth;
        vertices[10] = yOrigin + planeHeight;
         vertices[11] = zOrigin;
 
         return vertices;
     }
 
     private FloatBuffer asVertexBuffer(float[] vertices)
     {
         FloatBuffer vb;
         ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
         vbb.order(ByteOrder.nativeOrder());
         vb = vbb.asFloatBuffer();
         vb.put(vertices);
         vb.position(0);
         return vb;
     }
 }
