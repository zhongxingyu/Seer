 package com.example.opengl;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.opengl.GLUtils;
 
 public class Square {
   private FloatBuffer vertexBuffer;
   private FloatBuffer textureBuffer;
   private float vertices[] = {
     0.0f,  0.0f, 0.0f,
      0.0f, -1.0f, 0.0f,
     1.0f,  0.0f, 0.0f,
     1.0f, -1.0f, 0.0f
   };
   private float texture[] = {
     0.0f, 1.0f,
     0.0f, 0.0f,
     1.0f, 1.0f,
     1.0f, 0.0f
   };
   private int[] textures = new int[1];
 
 
   public Square() {
     // Allocate 4 bytes per float.
     ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
     byteBuffer.order(ByteOrder.nativeOrder());
     vertexBuffer = byteBuffer.asFloatBuffer();
     vertexBuffer.put(vertices);
     vertexBuffer.position(0);
     
     byteBuffer = ByteBuffer.allocateDirect(texture.length * 4);
     byteBuffer.order(ByteOrder.nativeOrder());
     textureBuffer = byteBuffer.asFloatBuffer();
     textureBuffer.put(texture);
     textureBuffer.position(0);
   }
 
 
   public void draw(GL10 gl) {
     gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
     
     gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
     gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
     
     gl.glFrontFace(GL10.GL_CW);
     gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
     gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
     gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);
     
     gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
     gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
   }
   
   
   public void loadGLTexture(GL10 gl, Context context) {
     Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ship);
     
     gl.glGenTextures(1, textures, 0);
     gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
     gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
     gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
     GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
     
     bitmap.recycle();
   }
 }
