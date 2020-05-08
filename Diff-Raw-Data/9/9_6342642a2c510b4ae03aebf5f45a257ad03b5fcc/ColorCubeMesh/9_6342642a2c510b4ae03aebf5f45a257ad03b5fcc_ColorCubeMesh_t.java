 package com.unknownloner.lonelib.graphics;
 
 import java.nio.ByteBuffer;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL15;
 
 import com.unknownloner.lonelib.graphics.buffers.VertexArrayObject;
 import com.unknownloner.lonelib.graphics.buffers.VertexAttribPointer;
 import com.unknownloner.lonelib.graphics.buffers.VertexBufferObject;
 import com.unknownloner.lonelib.math.Vec3;
 import org.lwjgl.opengl.GL31;
 
 /**
  * Represents a Mesh for use with a shader program that uses vertices(3), color data(4), and normals(3) as floats.
  */
 public class ColorCubeMesh implements Mesh {
 
     public static final Vec3[] CUBE_VERTS = new Vec3[] {
             new Vec3(0, 0, 0), new Vec3(0, 0, 1),
             new Vec3(0, 1, 0), new Vec3(0, 1, 1),
             new Vec3(1, 0, 0), new Vec3(1, 0, 1),
             new Vec3(1, 1, 0), new Vec3(1, 1, 1)
     };
 
     private final Vec3 position;
     private final VertexBufferObject vertBuffer;
     private final VertexBufferObject indexBuffer;
     private final VertexArrayObject vao;
 
     private final FloatBuffer verts;
     private final IntBuffer iBuffer;
     private final IntBuffer indices;
     private int index = 0;
     private int numIndices = 0;
 
     public static final short BYTES_PER_VERT = 28; // 7 values * 4 bytes;
     public static final short BYTES_PER_CUBE = BYTES_PER_VERT * 8; // 8 vertices per cube * 7 values * 4 bytes per value
     public static final int RESET_INDEX = Integer.MAX_VALUE;
 
     /**
      * Construct a mesh for use at the given world position, with the number of maximum triangles
      * @param position
      * @param maxCubes to render
      */
     public ColorCubeMesh(Vec3 position, int maxCubes) {
         this.position = position;
         vertBuffer = new VertexBufferObject(GL15.GL_ARRAY_BUFFER, GL15.GL_STATIC_DRAW, 0);
         indexBuffer = new VertexBufferObject(GL15.GL_ELEMENT_ARRAY_BUFFER, GL15.GL_STATIC_DRAW, 0);
         vao = new VertexArrayObject(
                 new VertexAttribPointer(vertBuffer, 0, 3, GL11.GL_FLOAT, false, BYTES_PER_VERT, 0), // 3 floats per vertex
                 new VertexAttribPointer(vertBuffer, 1, 3, GL11.GL_FLOAT, false, BYTES_PER_VERT, (3 * 4)), // 3 normals per vertex
                 new VertexAttribPointer(vertBuffer, 2, 1, GL11.GL_UNSIGNED_INT, false, BYTES_PER_VERT, (3 + 3) * 4) // 1 packed int for color data
         );
         ByteBuffer buffer = BufferUtils.createByteBuffer(maxCubes * BYTES_PER_CUBE);
         verts = buffer.asFloatBuffer();
         iBuffer = buffer.asIntBuffer();
         indices = BufferUtils.createIntBuffer(maxCubes * 17 + 1); // 17 indices per Cube
     }
 
     /**
      * Adds a cube at the given x, y, z into the mesh
      * @param x
      * @param y
      * @param z
      * @param colorData
      */
     public void addCube(float x, float y, float z, int colorData) {
         int[] p = new int[8];
         for(int i = 0; i < p.length; i++) {
             p[i] = addVertex(CUBE_VERTS[i].getX() + x, CUBE_VERTS[i].getY() + y, CUBE_VERTS[i].getZ() + z, colorData, 0, 0, 0); //TODO: fix normal data
         }
         addCubeStrip(new int[] { p[0], p[1], p[2], p[3], p[6], p[7], p[4], p[5], RESET_INDEX, p[2], p[6], p[0], p[4], p[1], p[5], p[3], p[7], RESET_INDEX });
     }
 
     /**
      * Adds vertex data to the float buffer and returns the index the vertex was put.
      *
      * @param x
      * @param y
      * @param z
      * @param abgr
      * @param normX
      * @param normY
      * @param normZ
      * @return index it was added at
      */
     private int addVertex(float x, float y, float z, int abgr, float normX, float normY, float normZ) {
         verts.put(new float[] {x, y, z, normX, normY, normZ});
         iBuffer.position(verts.position());
         iBuffer.put(abgr);
         verts.position(iBuffer.position());
         return index++;
     }
 
     private void addCubeStrip(int[] indices) {
         this.indices.put(indices);
         numIndices += indices.length;
     }
 
     @Override
     public void finalizeMesh() {
         verts.flip();
         indices.flip();
         vertBuffer.bufferData(verts, GL15.GL_STATIC_DRAW);
         indexBuffer.bufferData(indices, GL15.GL_STATIC_DRAW);
     }
 
     @Override
     public void reset() {
         verts.clear();
         indices.clear();
         index = numIndices = 0;
     }
 
     @Override
     public void render() {
         // render the mesh
        // Assumes that the proper shaderProgram is already active and that the model positions has already been uploaded to the shader
         vao.assign();
         indexBuffer.assign();
         GL11.glEnable(GL31.GL_PRIMITIVE_RESTART);
         GL31.glPrimitiveRestartIndex(RESET_INDEX);
         GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, numIndices, GL11.GL_UNSIGNED_INT, 0);
     }
 
     @Override
     public void delete() {
         vertBuffer.delete();
         indexBuffer.delete();
         vao.delete();
     }
 
     @Override
     public Vec3 getPosition() {
         return position;
     }
 }
