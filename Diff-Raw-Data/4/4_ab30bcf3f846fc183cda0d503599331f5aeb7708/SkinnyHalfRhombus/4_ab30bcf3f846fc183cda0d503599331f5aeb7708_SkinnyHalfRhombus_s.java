 package org.jf.Penroser;
 
 import android.graphics.RectF;
 
 import javax.microedition.khronos.opengles.GL10;
 import javax.microedition.khronos.opengles.GL11;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 
 public class SkinnyHalfRhombus extends HalfRhombus {
     private static final int SKINNY = 0;
     private static final int FAT = 1;
 
     private static final int NUM_CHILDREN = 2;
     private static final float[] leftVertices;
     private static final int[] leftColors;
     private static final float[] rightVertices;
     private static final int[] rightColors;
 
     private static int leftVertexVbo;
     private static int leftColorVbo;
     private static int rightVertexVbo;
     private static int rightColorVbo;
 
     //light green (reverse RGB)
     private static final int leftColor = 0x9AFF9A;
 
     //dark green (reverse RGB)
     private static final int rightColor = 0x008900;
 
     static {
         float[][] vertices = new float[1][];
         int [][] colors = new int[1][];
 
         generateVertices(VBO_LEVEL, LEFT, vertices, colors);
         leftVertices = vertices[0];
         leftColors = colors[0];
 
         generateVertices(VBO_LEVEL, RIGHT, vertices, colors);
         rightVertices = vertices[0];
         rightColors = colors[0];
     }
 
     public SkinnyHalfRhombus() {
     }
 
     public SkinnyHalfRhombus(int level, int side, float x, float y, float scale, int rotation) {
         super(level, side, x, y, scale, rotation);
     }
 
     @Override
     protected void calculateVertices(float[] vertices) {
         EdgeLength edgeLength = EdgeLength.getEdgeLength(level);
         int sign = side==LEFT?1:-1;
 
         float sideX = x + edgeLength.x(rotation-(sign*4));
         float sideY = y + edgeLength.y(rotation-(sign*4));
         float topX = sideX + edgeLength.x(rotation+(sign*4));
         float topY = sideY + edgeLength.y(rotation+(sign*4));
 
         vertices[0] = x;
         vertices[1] = y;
         vertices[2] = sideX;
         vertices[3] = sideY;
         vertices[4] = topX;
         vertices[5] = topY;
     }
 
     @Override
     protected void calculateEnvelope(RectF envelope) {
         EdgeLength edgeLength = EdgeLength.getEdgeLength(level);
         int sign = side==LEFT?1:-1;
 
         float sideX = x + edgeLength.x(rotation-(sign*4));
         float sideY = y + edgeLength.y(rotation-(sign*4));
         float topX = sideX + edgeLength.x(rotation+(sign*4));
         float topY = sideY + edgeLength.y(rotation+(sign*4));
 
         float minX = Math.min(Math.min(x, sideX), topX);
         float maxX = Math.max(Math.max(x, sideX), topX);
         float minY = Math.min(Math.min(y, sideY), topY);
         float maxY = Math.max(Math.max(y, sideY), topY);
 
         envelope.set(minX, minY, maxX, maxY);
     }
 
     @Override
     public int draw(GL11 gl, RectF viewportEnvelope, int maxLevel) {
         return draw(gl, viewportEnvelope, maxLevel, true);
     }
 
     @Override
     protected int draw(GL11 gl, RectF viewportEnvelope, int maxLevel, boolean checkIntersect) {
         if (checkIntersect && !envelopeIntersects(viewportEnvelope)) {
             return 0;
         }
 
         if (checkIntersect && envelopeCoveredBy(viewportEnvelope)) {
             checkIntersect = false;
         }
 
         if (this.level < maxLevel) {
             int num=0;
             for (int i=0; i<NUM_CHILDREN; i++) {
                 HalfRhombus halfRhombus = getChild(i);
                 num += halfRhombus.draw(gl, viewportEnvelope, maxLevel, checkIntersect);
             }
             return num;
         } else {
             gl.glPushMatrix();
             gl.glTranslatef(this.x, this.y, 0);
             gl.glScalef(this.scale, this.scale, 0);
             gl.glRotatef(getRotationInDegrees(), 0, 0, -1);
 
             int vertexVbo;
             int colorVbo;
             int length;
             if (side == LEFT) {
                 vertexVbo = leftVertexVbo;
                 colorVbo = leftColorVbo;
                 length = leftColors.length;
             } else {
                 vertexVbo = rightVertexVbo;
                 colorVbo = rightColorVbo;
                 length = rightColors.length;
             }
 
             gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, vertexVbo);
             gl.glVertexPointer(2, GL10.GL_FLOAT, 0, 0);
             gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
 
             gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, colorVbo);
             gl.glColorPointer(4, GL10.GL_UNSIGNED_BYTE, 0, 0);
             gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
 
             gl.glDrawArrays(GL10.GL_TRIANGLES, 0, length);
 
             gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
 
             gl.glPopMatrix();
 
             return 1;
         }
     }
 
     @Override
     public HalfRhombus getChild(int i) {
         int sign = this.side==LEFT?1:-1;
 
         float newScale = scale / Constants.goldenRatio;
 
         EdgeLength edgeLength = EdgeLength.getEdgeLength(level);
 
         switch (i) {
             case SKINNY: {
                 float topVerticeX = x + edgeLength.x(rotation-(sign*4)) + edgeLength.x(rotation+(sign*4));
                 float topVerticeY = y + edgeLength.y(rotation-(sign*4)) + edgeLength.y(rotation+(sign*4));
                 return Penroser.halfRhombusPool.getSkinnyHalfRhombus(level+1, side, topVerticeX, topVerticeY, newScale, rotation-(sign*6));
             }
             case FAT: {
                 float sideVerticeX = x + edgeLength.x(rotation-(sign*4));
                 float sideVerticeY = y + edgeLength.y(rotation-(sign*4));
                 return Penroser.halfRhombusPool.getFatHalfRhombus(level+1, side, sideVerticeX, sideVerticeY, newScale, rotation+(sign*6));
             }
         }
 
         return null;
     }
 
     @Override
     public int getRandomParentType(int edge) {
         if (edge == HalfRhombus.UPPER_EDGE) {
             return FAT;
         }
         return Penroser.random.nextInt(2);
     }
 
     @Override
     public HalfRhombus getParent(int parentType) {
         float newScale = scale * Constants.goldenRatio;
         int sign = side==LEFT?1:-1;
 
         switch (parentType) {
             case SKINNY: {
                 EdgeLength edgeLength = EdgeLength.getEdgeLength(level);
                 float parentBottomX = x + edgeLength.x(rotation-(sign*4));
                 float parentBottomY = y + edgeLength.y(rotation-(sign*4));
                 return new SkinnyHalfRhombus(level-1, side, parentBottomX, parentBottomY, newScale, rotation+(sign*6));
             }
             case FAT: {
                 EdgeLength edgeLength = EdgeLength.getEdgeLength(level-1);
                 float parentBottomX = x + edgeLength.x(rotation-(sign*6));
                 float parentBottomY = y + edgeLength.y(rotation-(sign*6));
                 return new FatHalfRhombus(level-1, oppositeSide(), parentBottomX, parentBottomY, newScale, rotation+(sign*2));
             }
         }
         return null;
     }
 
     public static void onSurfaceCreated(GL11 gl) {
         int[] vboref = new int[1];
 
         //left vertex vbo
         gl.glGenBuffers(1, vboref, 0);
         leftVertexVbo = vboref[0];
 
         gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, leftVertexVbo);
         gl.glBufferData(GL11.GL_ARRAY_BUFFER, leftVertices.length * 4, FloatBuffer.wrap(leftVertices), GL11.GL_STATIC_DRAW);
 
         //left color vbo
         gl.glGenBuffers(1, vboref, 0);
         leftColorVbo = vboref[0];
 
         gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, leftColorVbo);
         gl.glBufferData(GL11.GL_ARRAY_BUFFER, leftColors.length * 4, IntBuffer.wrap(leftColors), GL11.GL_STATIC_DRAW);
 
         //right vertex vbo
         gl.glGenBuffers(1, vboref, 0);
         rightVertexVbo = vboref[0];
 
         gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, rightVertexVbo);
         gl.glBufferData(GL11.GL_ARRAY_BUFFER, rightVertices.length * 4, FloatBuffer.wrap(rightVertices), GL11.GL_STATIC_DRAW);
 
         //right color vbo
         gl.glGenBuffers(1, vboref, 0);
         rightColorVbo = vboref[0];
 
         gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, rightColorVbo);
         gl.glBufferData(GL11.GL_ARRAY_BUFFER, rightColors.length * 4, IntBuffer.wrap(rightColors), GL11.GL_STATIC_DRAW);
 
         gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
     }
 
     public static void generateVertices(int level, int side, float[][] vertices, int[][] colors) {
         assert vertices != null && vertices.length > 0;
         assert colors != null && colors.length > 0;
 
        int fat=1;
        int skinny=0;
 
         for (int i=0; i<level; i++) {
             fat = fat * 2 + skinny;
             skinny = fat+skinny;
         }
 
         int count = fat+skinny;
         vertices[0] = new float[count*3*2];
         colors[0] = new int[count*3];
 
         generateVertices(vertices[0], colors[0], 0, 0, level, 0, side, 0, 0);
     }
 
     //TODO: I'm curious whether using final for these args will cause javac to optimize the many instances of level+1, rotation+n, etc.
     protected static int generateVertices(final float[] vertices, final int[] colors, int index, final int level, final int maxLevel, final int rotation, final int side, final float x, final float y) {
         //x,y are the coordinates of the bottom vertex of the rhombus
 
         EdgeLength edgeLength = EdgeLength.getEdgeLength(level);
 
         float sideVertexX, sideVertexY;
         float topVertexX, topVertexY;
 
         int sign = side==LEFT?1:-1;
 
         sideVertexX = x + edgeLength.x(rotation-(sign*4));
         sideVertexY = y + edgeLength.y(rotation-(sign*4));
 
         topVertexX = sideVertexX + edgeLength.x(rotation+(sign*4));
         topVertexY = sideVertexY + edgeLength.y(rotation+(sign*4));
 
         if (level < maxLevel) {
             index = FatHalfRhombus.generateVertices(vertices, colors, index, level+1, maxLevel, rotation+(sign*6), side, sideVertexX, sideVertexY);
 
             return SkinnyHalfRhombus.generateVertices(vertices, colors, index, level+1, maxLevel, rotation-(sign*6), side, topVertexX, topVertexY);
         } else {
             int color = side==LEFT?leftColor:rightColor;
 
             colors[index>>1] = color;
 
             vertices[index++] = x;
             vertices[index++] = y;
 
             colors[index>>1] = color;
 
             vertices[index++] = sideVertexX;
             vertices[index++] = sideVertexY;
 
             colors[index>>1] = color;
 
             vertices[index++] = topVertexX;
             vertices[index++] = topVertexY;
 
             return index;
         }
     }
 }
