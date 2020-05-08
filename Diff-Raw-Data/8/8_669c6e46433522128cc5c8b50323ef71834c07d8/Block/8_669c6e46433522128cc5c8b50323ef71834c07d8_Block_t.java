 package de.genericproject.game;
 
 import javax.media.opengl.GL2;
 import javax.media.opengl.GLAutoDrawable;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 
 public class Block {
 
 	static int debug_counter;
 
 	float pos_x;
 	float pos_y;
 	float pos_z;
 
 	int type;
 	/**
 	 * defining the different block types
 	 */
 	final static int TYPE_DIRT = 0;
 	final static int TYPE_ROCK = 1;
 	final static int TYPE_SAND = 2;
 	final static int TYPE_WOOD = 3;
 	final static int TYPE_LEAF = 4;
 	final static int TYPE_GRASS = 5;
 	final static int TYPE_WATER = 6;
 
 
 	final static float BOX_SIZE = 0.5f;
 
 	/**
 	 * the points of the standard box
 	 *    F -- G
 	 *   /    /|
 	 *  E -- H |
 	 *  | B  | C
 	 *  |/   |/
 	 *  A -- D
 	 */
 
 	// vertex coords array
 	float vertices[];
 	int indices[];
 
 	private FloatBuffer vertexBuffer;
 	private FloatBuffer colorBuffer;
 	private FloatBuffer normalsBuffer;
 	private IntBuffer indicesBuffer;
 
 	final static float[] COLOR_DIRT = {0.82f, 0.48f, 0.18f, 1f};
 	final static float[] COLOR_ROCK = {0.67f, 0.63f, 0.60f, 1f};
 	final static float[] COLOR_SAND = {0.96f, 0.93f, 0.22f, 1f};
 	final static float[] COLOR_WOOD = {0.69f, 0.41f, 0.01f, 1f};
 	final static float[] COLOR_LEAF = {0.53f, 0.91f, 0.28f, 1f};
 	final static float[] COLOR_GRASS = {0.43f, 0.81f, 0.28f, 1f};
 	final static float[] COLOR_WATER = {0.13f, 0.34f, 0.84f, 0.7f};
 
 	final static float[] BOX_POINT_A = {0f, 0f, 0f};
 	final static float[] BOX_POINT_B = {0f, 0f, BOX_SIZE};
 	final static float[] BOX_POINT_C = {BOX_SIZE, 0f, BOX_SIZE};
 	final static float[] BOX_POINT_D = {BOX_SIZE, 0f, 0f};
 	final static float[] BOX_POINT_E = {0f, BOX_SIZE, 0f};
 	final static float[] BOX_POINT_F = {0f, BOX_SIZE, BOX_SIZE};
 	final static float[] BOX_POINT_G = {BOX_SIZE, BOX_SIZE, BOX_SIZE};
 	final static float[] BOX_POINT_H = {BOX_SIZE, BOX_SIZE, 0f};
 
 	final static float[][] COLOR_MAP = {COLOR_DIRT, COLOR_ROCK, COLOR_SAND, COLOR_WOOD, COLOR_LEAF, COLOR_GRASS, COLOR_WATER};
 	/**
 	 * map to retrieve the correct color for each block type
 	 */
 
 	/**
 	 * create a new block
 	 * @param x Position x
 	 * @param y Position y
 	 * @param z Position z
 	 * @param type Block type. See different block types
 	 */
 	public Block(float x, float y, float z, int type)
 	{
 		setUpVertexArrays(x,y,z);
 
 		pos_x = x;
 		pos_y = y;
 		pos_z = z;
 		this.type = type;
 	}
 
 	/**
 	 * render method. render this single block
 	 * @param drawable
 	 */
 	public void renderVertexArray(GLAutoDrawable drawable)
 	{
 		GL2 gl = drawable.getGL().getGL2();
 
 		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, COLOR_MAP[type], 0);
     	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, COLOR_MAP[type], 0);
     	gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 0.2f);
 
 		// Enable and specificy pointers to vertex arrays
 		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
 		gl.glEnableClientState(GL2.GL_INDEX_ARRAY);
 		//gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
 
 		gl.glVertexPointer(3, GL2.GL_FLOAT, 0, vertexBuffer);
 		gl.glIndexPointer(GL2.GL_INT, 0, indicesBuffer);
 		//gl.glColorPointer(3, GL2.GL_FLOAT, 0, colorBuffer);
 		gl.glDrawElements(GL2.GL_QUADS, 24, GL2.GL_UNSIGNED_INT, indicesBuffer);
 
 		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
 		gl.glDisableClientState(GL2.GL_INDEX_ARRAY);
 		//gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
 	}
 
 	public void renderVertex3f(GLAutoDrawable drawable)
 	{
 		debug_counter++;
 		GL2 gl = drawable.getGL().getGL2();
 
     	gl.glPushMatrix();
     	gl.glTranslatef(pos_x * BOX_SIZE, pos_y * BOX_SIZE, pos_z * BOX_SIZE);
     	gl.glBegin(GL2.GL_QUADS);										// Begin drawing square bottom
 
 		//gl.glColor4f( COLOR_MAP[type][0], COLOR_MAP[type][1], COLOR_MAP[type][2], COLOR_MAP[type][3]);
 
     	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, COLOR_MAP[type], 0);
     	gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, COLOR_MAP[type], 0);
     	gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 0.2f);
 
     	// bottom ABCD
     	gl.glNormal3f(0f, -1f, 0f);
 		gl.glVertex3fv(BOX_POINT_A, 0);
 		gl.glVertex3fv(BOX_POINT_B, 0);
 		gl.glVertex3fv(BOX_POINT_C, 0);
 		gl.glVertex3fv(BOX_POINT_D, 0);
 
 		// front ADHE
 		gl.glNormal3f(0f, 0f, -1f);
 		gl.glVertex3fv(BOX_POINT_A, 0);
 		gl.glVertex3fv(BOX_POINT_D, 0);
 		gl.glVertex3fv(BOX_POINT_H, 0);
 		gl.glVertex3fv(BOX_POINT_E, 0);
 
 		// left AEFB
 		gl.glNormal3f(-1f, 0f, 0f);
 		gl.glVertex3fv(BOX_POINT_A, 0);
 		gl.glVertex3fv(BOX_POINT_E, 0);
 		gl.glVertex3fv(BOX_POINT_F, 0);
 		gl.glVertex3fv(BOX_POINT_B, 0);
 
 		// back BFGC
 		gl.glNormal3f(0f, 0f, 1f);
 		gl.glVertex3fv(BOX_POINT_B, 0);
 		gl.glVertex3fv(BOX_POINT_F, 0);
 		gl.glVertex3fv(BOX_POINT_G, 0);
 		gl.glVertex3fv(BOX_POINT_C, 0);
 
 		// right DCGH
 		gl.glNormal3f(1f, 0f, 0f);
 		gl.glVertex3fv(BOX_POINT_D, 0);
 		gl.glVertex3fv(BOX_POINT_C, 0);
 		gl.glVertex3fv(BOX_POINT_G, 0);
 		gl.glVertex3fv(BOX_POINT_H, 0);
 
 		// top EHGF
 		gl.glNormal3f(0f, 1f, 0f);
 		gl.glVertex3fv(BOX_POINT_E, 0);
 		gl.glVertex3fv(BOX_POINT_H, 0);
 		gl.glVertex3fv(BOX_POINT_G, 0);
 		gl.glVertex3fv(BOX_POINT_F, 0);
 
 		gl.glEnd();														// Finish drawing square bottom
 		gl.glPopMatrix();
 	}
 
 	private void setUpVertexArrays(float x, float y, float z)
 	{
		x *= BOX_SIZE;
		y *= BOX_SIZE;
		z *= BOX_SIZE;
		
 		indices = new int[]
 		{
 			0,1,2,3,
 			4,5,6,7,
 			8,9,10,11,
 			12,13,14,15,
 			16,17,18,19,
 			20,21,22,23
 		};
 
 		vertices = new float[]
 		{
 			BOX_SIZE + x, 	BOX_SIZE + y , 	0f + z , 			0f + x , 		BOX_SIZE + y, 	0f + z,  			0f + x, 		0f + y, 		0f + z,  		BOX_SIZE + x, 	0f + y, 		0f + z,        	// h-e-a-d
 			BOX_SIZE + x, 	BOX_SIZE + y , 	0f + z, 			BOX_SIZE + x , 	0f + y, 		0f + z, 			BOX_SIZE + x, 	0f + y, 		BOX_SIZE + z, 	BOX_SIZE + x, 	BOX_SIZE + y, 	BOX_SIZE + z, 	// h-d-c-g
 			BOX_SIZE + x,	BOX_SIZE + y , 	0f + z, 			BOX_SIZE + x , 	BOX_SIZE + y, 	BOX_SIZE + z,  		0f + x, 		BOX_SIZE + y, 	BOX_SIZE + z,  	0f + x, 		BOX_SIZE + y, 	0f + z,        	// h-g-f-e
 			0f + x, 		BOX_SIZE + y , 	0f + z,  			0f + x , 		BOX_SIZE + y, 	BOX_SIZE + z,  		0f + x, 		0f + y, 		BOX_SIZE + z,  	0f + x, 		0f + y, 		0f + z,    		// e-f-b-a
 			0f + x, 		0f + y, 		BOX_SIZE + z ,  	BOX_SIZE + x , 	0f + y, 		BOX_SIZE + z,  		BOX_SIZE + x, 	0f + y, 		0f + z,  		0f + x, 		0f + y, 		0f + z,    		// b-c-d-a
 			BOX_SIZE + x, 	0f + y , 		BOX_SIZE + z ,  	0f + x , 		0f + y, 		BOX_SIZE + z,  		0f + x , 		BOX_SIZE + y, 	BOX_SIZE + z,  	BOX_SIZE + x, 	BOX_SIZE + y, 	BOX_SIZE + z  	// c-b-f-g
 		};
 
 		vertexBuffer =
 			ByteBuffer
 				.allocateDirect(vertices.length * 4)
 				.order(ByteOrder.nativeOrder())
 				.asFloatBuffer();
 		vertexBuffer.put(vertices);
 		vertexBuffer.rewind();
 
 		indicesBuffer =
 			(ByteBuffer.allocateDirect(indices.length * 4))
 				.order(ByteOrder.nativeOrder())
 				.asIntBuffer();
 		indicesBuffer.put(indices);
 		indicesBuffer.rewind();
 
 		normalsBuffer =
 			ByteBuffer
 				.allocateDirect(vertices.length * 4)
 				.order(ByteOrder.nativeOrder())
 				.asFloatBuffer();
 		normalsBuffer.put(vertices);
 		normalsBuffer.rewind();
 	}
 }
