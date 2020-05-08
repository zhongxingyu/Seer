 //File: Bot.java
 //Purpose: Game Bot class.
 
 package edu.sru.andgate.bitbot.graphics;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import javax.microedition.khronos.opengles.GL10;
 
 import edu.sru.andgate.bitbot.Bot;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.opengl.GLUtils;
 import java.util.*;
 
 public class DrawableBot implements Drawable
 {
 	float[] parameters;
 	int ID = 0;
 	int textureCount = 0;
 	int MAX_TEXTURE_ARRAY_SIZE = 5;
 	int SELECTED_TEXTURE = 0;
 	ArrayList<Integer> textureHopper;
 	ArrayList<Integer> layerIdList;
 	boolean textureLoaded = false;
 	
 	public FloatBuffer vertexBuffer;	// buffer holding the vertices
 	public float vertices[] = {
 			-1.0f, -1.0f,  0.0f,		// V1 - bottom left
 			-1.0f,  1.0f,  0.0f,		// V2 - top left
 			 1.0f, -1.0f,  0.0f,		// V3 - bottom right
 			 1.0f,  1.0f,  0.0f			// V4 - top right
 	};
 
 	public FloatBuffer textureBuffer;	// buffer holding the texture coordinates
 	public float texture[] = {    		
 			// Mapping coordinates for the vertices
 			0.0f, 1.0f,		// top left		(V2)
 			0.0f, 0.0f,		// bottom left	(V1)
 			1.0f, 1.0f,		// top right	(V4)
 			1.0f, 0.0f		// bottom right	(V3)
 	};
 	
 	//Texture pointer
 	public int[] textures = new int[MAX_TEXTURE_ARRAY_SIZE];
 
 	public DrawableBot()
 	{
 		// a float has 4 bytes so we allocate for each coordinate 4 bytes
 		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
 		byteBuffer.order(ByteOrder.nativeOrder());
 		
 		// allocates the memory from the byte buffer
 		vertexBuffer = byteBuffer.asFloatBuffer();
 		
 		// fill the vertexBuffer with the vertices
 		vertexBuffer.put(vertices);
 		
 		// set the cursor position to the beginning of the buffer
 		vertexBuffer.position(0);
 		
 		byteBuffer = ByteBuffer.allocateDirect(texture.length * 4);
 		byteBuffer.order(ByteOrder.nativeOrder());
 		textureBuffer = byteBuffer.asFloatBuffer();
 		textureBuffer.put(texture);
 		textureBuffer.position(0);
 		
 		parameters = new float[11];
 		
 		textureHopper = new ArrayList<Integer>(MAX_TEXTURE_ARRAY_SIZE);
 		
 		//Initialize parameters
 		parameters[0] = 0.3f;	//tX
 		parameters[1] = 0.1f;	//tY
 		parameters[2] = -5.0f;	//tZ
 		parameters[3] = 0.0f;	//rotationAngle
 		parameters[4] = 0.0f;	//rX
 		parameters[5] = 0.0f;	//rY
 		parameters[6] = 1.0f;	//rZ
 		parameters[7] = 0.85f;	//sX 0.3
 		parameters[8] = 0.85f;	//sY
 		parameters[9] = 0.85f;	//sZ
 		parameters[10] = 1.0f;	//textureUpdateFlag (NO = 0.0, YES = 1.0) (Avoid at all costs)
 	}
 	
 
 	@Override
 	public void attachBot(Bot bot)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 	
 	/* (non-Javadoc)
 	 * @see edu.sru.andgate.bitbot.graphics.Drawable#getCurrentParameters()
 	 */
 	@Override
 	public float[] getCurrentParameters()
 	{
 		return parameters;
 	}
 	
 	/* (non-Javadoc)
 	 * @see edu.sru.andgate.bitbot.graphics.Drawable#setTranslation(float, float, float)
 	 */
 	@Override
 	public void setTranslation(float newTX, float newTY, float newTZ)
 	{
 		parameters[0] = newTX;
 		parameters[1] = newTY;
 		parameters[2] = newTZ;
 	}
 	
 	/* (non-Javadoc)
 	 * @see edu.sru.andgate.bitbot.graphics.Drawable#setRotation(float, float, float, float)
 	 */
 	@Override
 	public void setRotation(float newAngle, float newRX, float newRY, float newRZ)
 	{
 		parameters[3] = newAngle;
 		parameters[4] = newRX;
 		parameters[5] = newRY;
 		parameters[6] = newRZ;
 	}
 	
 	/* (non-Javadoc)
 	 * @see edu.sru.andgate.bitbot.graphics.Drawable#setRotationAngle(float)
 	 */
 	@Override
 	public void setRotationAngle(float newAngle)
 	{
 		parameters[3] = newAngle;
 	}
 	
 	/* (non-Javadoc)
 	 * @see edu.sru.andgate.bitbot.graphics.Drawable#move(float, float, float)
 	 */
 	@Override
 	public void move(float angle, float speed, float distance){
 		//
 	}
 	
 	/* (non-Javadoc)
 	 * @see edu.sru.andgate.bitbot.graphics.Drawable#move(float, float)
 	 */
 	@Override
 	public void move(float angle, float stepSize)
 	{
		float rise = (float)(Math.sin(angle) * stepSize) + parameters[1];
		float run = (float)(Math.cos(angle) * stepSize) + parameters[0];
 		
		parameters[0] = run;
		parameters[1] = rise;;
 	}
 	
 	/* (non-Javadoc)
 	 * @see edu.sru.andgate.bitbot.graphics.Drawable#setScale(float, float, float)
 	 */
 	@Override
 	public void setScale(float newSX, float newSY, float newSZ)
 	{
 		parameters[7] = newSX;
 		parameters[8] = newSY;
 		parameters[9] = newSZ;
 	}
 	
 	/* (non-Javadoc)
 	 * @see edu.sru.andgate.bitbot.graphics.Drawable#setTextureUpdateFlag(float)
 	 */
 	@Override
 	public void setTextureUpdateFlag(float flag)
 	{
 		parameters[10] = flag;
 	}
 	
 	/* (non-Javadoc)
 	 * @see edu.sru.andgate.bitbot.graphics.Drawable#setSelectedTexture(int)
 	 */
 	@Override
 	public void setSelectedTexture(int selectedTex)
 	{
 		SELECTED_TEXTURE = selectedTex;
 	}
 	
 	/* (non-Javadoc)
 	 * @see edu.sru.andgate.bitbot.graphics.Drawable#getSelectedTexture()
 	 */
 	@Override
 	public int getSelectedTexture()
 	{
 		return SELECTED_TEXTURE;
 	}
 	
 	/*
 	public void setID(int id)
 	{
 		ID = id;
 	}
 	public int getID()
 	{
 		return ID;
 	}
 	*/
 	
 	/* (non-Javadoc)
 	 * @see edu.sru.andgate.bitbot.graphics.Drawable#addTexture(int)
 	 */
 	@Override
 	public void addTexture(int texturePointer)
 	{
 		textureHopper.add(texturePointer);
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.sru.andgate.bitbot.graphics.Drawable#loadGLTexture(javax.microedition.khronos.opengles.GL10, android.content.Context, int)
 	 */
 	@Override
 	public void loadGLTexture(GL10 gl, Context context, int curTexPointer)
 	{		
 		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),textureHopper.get(curTexPointer));
 
 		// generate one texture pointer
 		gl.glGenTextures(1, textures, curTexPointer);
 		// ...and bind it to our array
 		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[curTexPointer]);
 		
 		// create nearest filtered texture
 		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
 		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
 		
 		// Use Android GLUtils to specify a two-dimensional texture image from our bitmap 
 		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
 		
 		// Clean up
 		bitmap.recycle();
 		
 		//textureLoaded = true;
 	}
 	
 	/* (non-Javadoc)
 	 * @see edu.sru.andgate.bitbot.graphics.Drawable#draw(javax.microedition.khronos.opengles.GL10)
 	 */
 	@Override
 	public void draw(GL10 gl)
 	{
 		// bind the previously generated texture
 		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[SELECTED_TEXTURE]);
 		
 		// Point to our buffers
 		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
 		
 		// Set the face rotation
 		gl.glFrontFace(GL10.GL_CW);
 		
 		// Point to our vertex buffer
 		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
 		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
 		
 		//Prepare openGL for drawing
 		gl.glTranslatef(parameters[0],parameters[1], parameters[2]);				//Translate Object
 		gl.glRotatef(parameters[3], parameters[4], parameters[5], parameters[6]);	//Rotate Object
 		gl.glScalef(parameters[7], parameters[8], parameters[9]);					//Scale Object
 		
 		// Draw the vertices as triangle strip
 		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);
 
 		//Disable the client state before leaving
 		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
 	}
 
 }
