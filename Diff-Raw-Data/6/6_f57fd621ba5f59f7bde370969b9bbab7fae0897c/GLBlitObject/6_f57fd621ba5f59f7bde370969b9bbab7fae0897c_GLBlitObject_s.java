 /* Copyright 2012 Richard Sahlin
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 
 package com.super2k.openglen.objects;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.nio.ShortBuffer;
 
 import com.super2k.openglen.animation.Animation3D;
 import com.super2k.openglen.geometry.Material;
 import com.super2k.openglen.texture.Texture2D;
 
 /**
  * Each GLBlitObject holds the data needed to render a bitmap (image) to screen.
  * @author Richard Sahlin
  *
  */
 public class GLBlitObject extends GLObject {
 
     /**
      * Index into position array where the position offset is.
      * This position is added to the onscreen position after rotation and scaling.
      */
     public final static int POSITION_OFFSET_INDEX = 4;
 
     public final static float[] FRONT_FACING_NORMALS = new float[] {0f, 0f, 1f};
 
     /**
      * The normal stride in an array where
      * vertex x,y,z
      * normal x,y,z
      * texture s,t
      * is stored.
      */
     public final static int ARRAY_STRIDE = 8;
     /**
      * Index into array where vertex x,y,z is
      */
     public final static int VERTEX_INDEX = 0;
 
     /**
      * Index into array where normal x,y,z is
      */
     public final static int NORMAL_INDEX = 3;
 
     /**
      * Size of a stored float in bytes
      */
     public final static int FLOAT_BYTE_SIZE = 4;
     /**
      * Index into array where texture (s,t) coordinates are.
      */
     public final static int TEXTURE_COORD_INDEX = 6;
 
     /**
      * Number of elements in a texture coordinate s,t
      */
     public final static int TEXTURE_COORD_ELEMENTS = 2;
 
     /**
      * Number of elements in vertex coordinates x,y,z
      */
     public final static int VERTEX_COORD_ELEMENTS = 3;
     /**
      * X anchor will be at left side
      */
     public final static int ANCHOR_LEFT = 1;
     /**
      * X anchor will be center (middle)
      */
     public final static int ANCHOR_CENTER_X = 2;
     /**
      * X anchor will be at right side
      */
     public final static int ANCHOR_RIGHT = 4;
     /**
      * Y anchor will be at top
      */
     public final static int ANCHOR_TOP = 0x10;
     /**
      * Y anchor will be center (middle)
      */
     public final static int ANCHOR_CENTER_Y = 0x20;
     /**
      * Y anchor will be at bottom
      */
     public final static int ANCHOR_BOTTOM = 0x40;
 
     protected final static String[] ANCHOR_STRINGS = new String[] { "LEFT", "CENTER_X", "RIGHT",
         "TOP", "BOTTOM", "CENTER_Y"};
     protected final static int[] ANCHOR_VALUES = new int[] { ANCHOR_LEFT, ANCHOR_CENTER_X, ANCHOR_RIGHT,
         ANCHOR_TOP, ANCHOR_BOTTOM, ANCHOR_CENTER_Y};
 
     /**
      * Index in position array to x position
      */
     public final static int X_INDEX = 0;
     /**
      * Index in position array to y position
      */
     public final static int Y_INDEX = 1;
     /**
      * Index in position array to z position
      */
     public final static int Z_INDEX = 2;
     /**
      * Index in position array to w position
      */
     public final static int W_INDEX = 3;
     /**
      * Index in position array to x offset
      */
     public final static int X_OFFSET_INDEX = 4;
     /**
      * Index in position array to y offset
      */
     public final static int Y_OFFSET_INDEX = 5;
     /**
      * Index in position array to z offset
      */
     public final static int Z_OFFSET_INDEX = 6;
     /**
      * Index in position array to w offset
      */
     public final static int W_OFFSET_INDEX = 7;
 
     /**
      * Position on screen - if z is not used set to -1.
      * All z values are positive going into the screen. Highest value is at the back.
      * The array consists of 4 values for position and 4 values for position offset, the offset
      * is added after rotate and scale.
      * The position offset is used for animations that can be rotated.
      */
     public float[] position = new float[] {0, 0, 0, 1, 0, 0, 0, 1};
 
     /**
      * Scaling of object in x,y and z.
      * X is at index 0.
      */
     public float[] scale = new float[] { 1, 1, 1, 1 };
 
     /**
      * Rotation of object in x, y and z.
      * X is at index 0.
      */
     public float[] rotation = new float[] { 0, 0, 0, 0 };
 
     /**
      * Width of blit.
      */
     public float width;
     /**
      * Height of blit.
      */
     public float height;
 
     public ShortBuffer indices;
 
     protected int mVertexCount; //Number of vertices
     protected int mIndiceCount; //Number of indices for triangle list.
     protected int mDivision;
     private float[] mTemp2Float = new float[2];
     private short[] mTempIndices = new short[6];
     /**
      * Byte stride for vertices, this is used to align array
      * data in memory.
      */
     public int mArrayByteStride;
 
     public int elementVBOName = -1;
 
     /**
      * Default number of animations per object.
      */
     public final static int ANIMATION_COUNT = 3;
 
     /**
      * Linear transform animtion, room for one animation for translate, rotate and scale.
      * Animations are setup with a float[] as target.
      */
     public Animation3D[] anim = new Animation3D[ANIMATION_COUNT];
 
     /**
      * Default constructor
      */
     public GLBlitObject() {
         super();
         objectType = BLIT_OBJECT;
     }
 
     /**
      * Creates a new object with the specified position, size and material.
      * @param x
      * @param y
      * @param z
      * @param width
      * @param height
      * @param material The material, it is copied into the object
      * @param anchor
      * @param division
      */
     public GLBlitObject(float x, float y, float z, float width, float height, Material material,
             int anchor, int division) {
         create(division, 1, 1);
         set(width, height, anchor, material);
         setPosition(x, y, z);
         objectType = BLIT_OBJECT;
     }
 
     /**
      * Create a new GLBlit object using the specified position and bitmap.
      * The onscreen size and anchor is specified.
      * The material will get the following properties:
      * Ambient 0, 0, 0, 0
      * Diffuse 1, 1, 1, 1
      * Specular 0.5, 0.5, 0.5, 1
      * power 10
      *
      * @param x
      * @param y
      * @param z
      * @param width Width of blit
      * @param height Height of blit
      * @param textures Array containing the texture object to use for this material.
      * @param anchor The anchor position
      * @param division Number of times to divide the quad in x and y
      * eg ANCHOR_CENTER_Y | ANCHOR_CENTER_X for middle anchor
      * ANCHOR_LEFT | ANCHOR_TOP for upper left anchor.
      * Note that texture objects are not copied, instead a reference is kept.
      */
     public GLBlitObject(float x, float y, float z,
             float width, float height, Texture2D[] textures, int anchor, int division) {
         create(division, 1, 1);
         set(width, height, anchor, null);
         setPosition(x, y, z);
         objectType = BLIT_OBJECT;
         material.texture = textures;
 
         material.materialShading = Material.SHADING_UNLIT;
         material.setAmbient(0, 0, 0, 0);
         material.setDiffuse(1, 1, 1, 1);
         material.setSpecular(0.5f, 0.5f, 0.5f, 1);
         material.power = 10;
 
 
     }
 
 
     /**
      * Creates material, arraybuffers and index buffer storage,
      * setups indices, normals and texture coordinates
      * @param division
      * @param xRepeat Number of times texture will repeat in x (this is the u max value)
      * @param yRepeat Number of times texture will repeat in y (this is the v max value)
      */
     public void create(int division, float xRepeat, float yRepeat) {
 
         material = new Material();
         mVertexCount = division * division * 4; //Total vertexcount for one quad.
         mDivision = division;
         mIndiceCount = (mVertexCount >>> 1) * 3; //Each quad is 4 vertices,
                                                  //gives number of triangles * 3
         arrayBuffer = ByteBuffer.allocateDirect(
                 (mVertexCount * 3 * FLOAT_BYTE_SIZE) +
                 (mVertexCount * 3 * FLOAT_BYTE_SIZE) +
                 (mVertexCount * 2 * FLOAT_BYTE_SIZE)).order(ByteOrder.nativeOrder()).asFloatBuffer();
         indices = ByteBuffer.allocateDirect(mIndiceCount * 2).order(ByteOrder.nativeOrder()).
                 asShortBuffer();
         setupIndices(division);
         setupNormals(division, arrayBuffer, NORMAL_INDEX, ARRAY_STRIDE);
         setupTexCoords(division, xRepeat, yRepeat, arrayBuffer, TEXTURE_COORD_INDEX, ARRAY_STRIDE);
     }
 
     /**
      * Setup triangle indexes for a Quad.
      * @param subdivision Number of times the quad should be subdivided in x and y
      */
     protected void setupIndices(int subdivision) {
 
         short index = 0; //Count up after each quad.
 
         indices.rewind();
         for (int y = 0; y < subdivision; y++) {
             for (int x = 0; x < subdivision; x++) {
                 mTempIndices[0] = index;
                 mTempIndices[1] = (short)(index+3);
                 mTempIndices[2] = (short)(index+1);
                 mTempIndices[3] = (short)(index+0);
                 mTempIndices[4] = (short)(index+2);
                 mTempIndices[5] = (short)(index+3);
                 indices.put(mTempIndices);
                 index += 4;
             }
         }
 
     }
 
     /**
      * Setup normals for frontfacing ortho blit, only needed for
      * objects that will be lit
      * The stride value will be set in this object to be used when rendering.
      * @subdivision Number of times the quad is split (in x and y)
      * @param normals Normals are put here.
      * @param Offset into normal array.
      * @param stride Number of items between each set of normal coordinates,
      * 0 for packed array. 3 for an array with 3 float positions inbetween each set of normals.
      */
     protected void setupNormals(int subdivision, FloatBuffer normals, int offset, int stride) {
 
         int size = FLOAT_BYTE_SIZE * subdivision * subdivision;
         for (int i = 0; i < size; i++) {
             normals.position(offset);
             normals.put(FRONT_FACING_NORMALS);
             offset += stride;
         }
 
     }
 
     /**
      * Setup texture coordinates for a normal frontfacing ortho blit.
      * The stride value will be set in this object to be used when rendering.
      * @param texCoordinates The FloatBuffer where coordinates are stored.
      * @param division Number of times each quad is split (in x and y)
      * @param uMax max X value for coordinates, the range is 0 - uMax
      * @param vMax max Y value for coordinates, the range is 0 - vMax
      * @param offset Offset into texCoordinate array
      * @param stride Number of items between each set of normal coordinates,
      * 0 for packed array. 3 for an array with 3 float positions inbetween each set of normals.
      */
     protected void setupTexCoords(int division, float uMax, float vMax,
             FloatBuffer texCoordinates, int offset, int stride) {
 
         float xpos = 0;
         float ypos = vMax;
         float ysub = vMax / division;
         float xadd = uMax / division;
         for (int y = 0; y < division; y++) {
             for (int x = 0; x < division; x++) {
                 mTemp2Float[0] = xpos;
                 mTemp2Float[1] = ypos;
                 texCoordinates.position(offset);
                 texCoordinates.put(mTemp2Float);
                 offset += stride;
 
                 mTemp2Float[0] = xpos;
                 mTemp2Float[1] = ypos - ysub;
                 texCoordinates.position(offset);
                 texCoordinates.put(mTemp2Float);
                 offset += stride;
 
                 mTemp2Float[0] = xpos + xadd;
                 mTemp2Float[1] = ypos;
                 texCoordinates.position(offset);
                 texCoordinates.put(mTemp2Float);
                 offset += stride;
 
                 mTemp2Float[0] = xpos + xadd;
                 mTemp2Float[1] = ypos - ysub;
                 texCoordinates.position(offset);
                 texCoordinates.put(mTemp2Float);
                 offset += stride;
 
                 xpos += xadd;
             }
             xpos = 0;
             ypos -= ysub;
         }
 
     }
 
     /**
      * Sets the position in world coordinates.
      * Where this object will be shown depends on viewport, transform matrix and projection.
      * @param x World x coordinate
      * @param y World y coordinate
      * @param z Workd z coordinate.
      */
     public void setPosition(float x, float y, float z) {
         position[0] = x;
         position[1] = y;
         position[2] = z;
 
     }
 
     /**
      * Sets the texture coordinates from an array, this method currently only works for a blit object that
      * is setup with a normal quad (2 triangles). Will not work if the quad is divided.
      * Coordinates are ordered as follows:
      * upper left corner, lower left corner, upper right corner, lower right corner
      * For a normal (one whole) texture this would be 0,1 0,0 1,1 1,0
      * Note that if vertex buffer object is used the caller must make sure it is updated to the graphics
      * library for any changes to take effect.
      * @param ST Array containing 4 ST coordinates, this method will set the 4 S,T coordinates into
      * the array.
      * @param index Index into array where texture coordinates are.
      */
     public void setTextureCoordinates(float[] ST, int index) {
         int offset = TEXTURE_COORD_INDEX;
         arrayBuffer.position(offset);
         arrayBuffer.put(ST,index, TEXTURE_COORD_ELEMENTS);
         offset += ARRAY_STRIDE;
         arrayBuffer.position(offset);
         arrayBuffer.put(ST,index + TEXTURE_COORD_ELEMENTS, TEXTURE_COORD_ELEMENTS);
         offset += ARRAY_STRIDE;
         arrayBuffer.position(offset);
         arrayBuffer.put(ST,index + TEXTURE_COORD_ELEMENTS * 2, TEXTURE_COORD_ELEMENTS);
         offset += ARRAY_STRIDE;
         arrayBuffer.position(offset);
         arrayBuffer.put(ST,index + TEXTURE_COORD_ELEMENTS * 3, TEXTURE_COORD_ELEMENTS);
         offset += ARRAY_STRIDE;
     }
 
     /**
      * Sets the width for this blit object, using the specified anchor.
      * Ony supported for blits with 4 vertices.
      * NOTE!
      * This will not update the cull radius, to do that call setCullRadius()
      * @param width
      * @param anchor LEFT, RIGHT or CENTER_X
      */
     public void setWidth(float width, int anchor) {
         if (mDivision > 1) {
             throw new IllegalArgumentException("Not supported for division: " + mDivision);
         }
         float xpos = 0;
         switch (anchor) {
             case ANCHOR_LEFT:
             break;
             case ANCHOR_RIGHT:
                 xpos = -width;
             break;
             case ANCHOR_CENTER_X:
                 xpos = width /2;
             break;
         }
         int offset = VERTEX_INDEX;
         arrayBuffer.position(offset);
         offset += ARRAY_STRIDE;
         arrayBuffer.put(xpos);
         arrayBuffer.position(offset);
         offset += ARRAY_STRIDE;
         arrayBuffer.put(xpos);
         arrayBuffer.position(offset);
         offset += ARRAY_STRIDE;
         arrayBuffer.put(xpos + width);
         arrayBuffer.position(offset);
         arrayBuffer.put(xpos + width);
     }
 
     /**
      * Sets the height for this blit object, using the specified anchor.
      * Ony supported for blits with 4 vertices.
      * NOTE!
      * This will not update the cull radius, to do that call setCullRadius()
      * @param width
      * @param anchor TOP, BOTTOM or CENTER_Y
      */
     public void setHeight(float height, int anchor) {
         if (mDivision > 1) {
             throw new IllegalArgumentException("Not supported for division: " + mDivision);
         }
         float ypos = height;
         switch (anchor) {
             case ANCHOR_TOP:
             break;
             case ANCHOR_BOTTOM:
                 ypos = 0;
             break;
             case ANCHOR_CENTER_Y:
                 ypos = height /2;
             break;
         }
         arrayBuffer.position(VERTEX_INDEX + 1);
         arrayBuffer.put(ypos);
         arrayBuffer.position(VERTEX_INDEX + ARRAY_STRIDE + 1);
         arrayBuffer.put(ypos - height);
         arrayBuffer.position(VERTEX_INDEX + ARRAY_STRIDE * 2 + 1);
         arrayBuffer.put(ypos);
         arrayBuffer.position(VERTEX_INDEX + ARRAY_STRIDE * 3 + 1);
         arrayBuffer.put(ypos - height);
         this.height = height;
     }
 
     /**
      * Sets the width and height of the blit and the material to use.
      * This will set the vertex positions based on the division of the blit object.
      * The cull radius will be set according to the width and height.
      * @param width
      * @param height
      * @param anchor
      * @param mat The material Or null to not set a new material.
      */
     public void set(float width, float height, int anchor, Material mat) {
 
         setVertices(width, height, anchor, (int) Math.sqrt(mVertexCount>>>2), arrayBuffer,
                 VERTEX_INDEX, ARRAY_STRIDE);
         mArrayByteStride = ARRAY_STRIDE * FLOAT_BYTE_SIZE; //Byte stride for array.
         if (mat != null) {
             setMaterial(mat);
         }
         renderFlag = true;
     }
 
     /**
      * Sets the cull radius, this can be used to determine if the object is fully off screen.
      * @param radius
      */
     public void setCullRadius(float radius) {
         mCullRadius = radius;
     }
 
     /**
      * Sets the width and height of the blit and the material to
      * use.
      * This will set the vertex positions based on the division of the blit object.
      * @param width
      * @param height
      * @param anchor
      * @param mat Material to set, if null then the existing material is kept.
      * @param texture The texture to set in the material.
      * @throws IllegalArgumentException If texture is null
      */
     public void set(float width, float height, int anchor, Material mat, Texture2D texture) {
         if (texture == null) {
             throw new IllegalArgumentException("Texture is null.");
         }
         set(width, height, anchor, mat);
         material.texture = new Texture2D[] {texture};
     }
 
     /**
      * Sets the material. The material is copied into this object.
      * @param material Material to set.
      */
     public void setMaterial(Material material) {
         this.material.set(material);
     }
 
     /**
      * Set the specular power, this controlls the 'shininess' of phong shaded objects.
      * Higher values for a more defined smaller highlight.
      * @param specularPower
      */
     public void setSpecularPower(float specularPower) {
         material.power = specularPower;
     }
 
     /**
      * Sets the vertex positions based on the width and height, this objects width and height
      * will be set.
      * Anchor will be center x and y.
      * The cull radius will be set according to the width and height.
      * @param width Width of blit, how large it is on screen depends on the projection.
      * @param height Height of blit,
      */
     public void setVertices(float width, float height) {
         setVertices(width, height, GLBlitObject.ANCHOR_CENTER_X | GLBlitObject.ANCHOR_CENTER_Y,
                 1, arrayBuffer, VERTEX_INDEX, ARRAY_STRIDE);
     }
 
     /**
      * Set the vertex positions (for GLES), vertex can be interleaved or packed in the array.
      * The stride value will be set in this object to be used when rendering.
      * The cull radius will be set according to the width and height.
      * @param width Width of blit
      * @param height Height of blit
      * @param anchor Anchor value
      * @param division Number of subdivisions for the blit object.
      * @param vertices FloatBuffer where vertices are put.
      * @param offset Offset into FloatBuffer where vertices start.
      * @param stride Number of positions between each set of vertice data.
      * Set to 0 for packed array, 3 for an array with 3 float values inbetween each set of vertices.
      */
     public void setVertices(float width, float height, int anchor, int division,
             FloatBuffer vertices, int offset, int stride) {
 
         this.width = width;
         this.height = Math.abs(height);
         float xpos = 0;
         float ypos = height; //Texture (t) coordinates go from 1 to 0 on y axis
         if ((anchor & ANCHOR_CENTER_X) != 0) {
             xpos = -(width / 2);
         }
         else if ((anchor & ANCHOR_RIGHT) != 0) {
             xpos = -width;
         }
 
         if ((anchor & ANCHOR_CENTER_Y) != 0) {
             ypos = (height / 2);
         }
         else if ((anchor & ANCHOR_BOTTOM) != 0) {
             ypos = 0;
         }
 
         float xAdd = width/division;
         float ySub = height/division;
         float xCopy = xpos;
         for (int y = 0; y < division; y++) {
             for (int x = 0; x < division; x++) {
                 vertices.position(offset);
                 offset += stride;
                 vertices.put(xpos);
                 vertices.put(ypos);
                 vertices.put(0);
 
                 vertices.position(offset);
                 offset += stride;
                 vertices.put(xpos);
                 vertices.put(ypos-ySub);
                 vertices.put(0);
 
                 vertices.position(offset);
                 offset += stride;
                 vertices.put(xpos+xAdd);
                 vertices.put(ypos);
                 vertices.put(0);
 
                 vertices.position(offset);
                 offset += stride;
                 vertices.put(xpos+xAdd);
                 vertices.put(ypos-ySub);
                 vertices.put(0);
 
                 xpos += xAdd;
             }
             xpos = xCopy;
             ypos -= ySub;
         }
        setCullRadius((float) Math.sqrt(width * width + height * height));
 
     }
 
     /**
      * Return the number of vertices.
      * @return
      */
     public int getVertexCount() {
         return mVertexCount;
     }
 
     /**
      * Return the number of triangle indexes, used when outputting to GL as trianglelist.
      * @return
      */
     public int getIndexCount() {
         return mIndiceCount;
     }
 
     /**
      * Return buffer containing element indices (triangle list).
      * @return Buffer containing triangle indexes.
      */
     public ShortBuffer getElementBuffer() {
         return indices;
     }
 
     /**
      * Set VBO name for elementbuffer, this buffer contains the triangle indexes.
      * VBO buffer shall contain the same data as the indices buffer (in this class)
      * @param name
      */
     public void setElementVBOName(int name) {
         elementVBOName = name;
     }
 
     @Override
     public void destroy() {
         super.destroy();
         scale = null;
         rotation = null;
         position = null;
         arrayBuffer = null;
         indices = null;
         anim = null;
     }
 
 
     @Override
     public void releaseObject() {
         super.releaseObject();
         rotation[0] = 0;
         rotation[1] = 0;
         rotation[2] = 0;
         scale[0] = 1;
         scale[1] = 1;
         scale[2] = 1;
         int count = anim.length;
         if (anim != null) {
             for (int i = 0; i < count; i++) {
                 anim[i] = null;
             }
         }
         material.texture = null;
     }
 
     @Override
     public void createObject(Object obj) {
         // TODO Auto-generated method stub
     }
 
     /**
      * Converts a String anchor to the value for the anchor.
      * @param anchor The anchor String, 2 of the anchor String with | between.
      * @return The anchor value for the String.
      */
     public final static int getAnchor(String anchor) {
 
         int a = 0;
         int index = anchor.indexOf("|");
         if (index != -1) {
             a = fetchAnchor(anchor.substring(0, index));
         }
         return (a + fetchAnchor(anchor.substring(index + 1)));
     }
 
     /**
      * Internal method to fetch the value of one anchor.
      * @param oneAnchor
      * @return
      */
     private final static int fetchAnchor(String oneAnchor) {
 
         for (int i = 0; i < ANCHOR_STRINGS.length; i++) {
             if (oneAnchor.equals(ANCHOR_STRINGS[i])) {
                 return ANCHOR_VALUES[i];
             }
         }
         throw new IllegalArgumentException("Invalid anchor: " + oneAnchor);
     }
 
 }
