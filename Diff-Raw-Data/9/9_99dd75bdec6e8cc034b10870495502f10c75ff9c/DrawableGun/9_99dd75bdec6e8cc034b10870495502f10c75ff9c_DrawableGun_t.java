 //File: Bot.java
 //Purpose: Game Bot class.
 
 package edu.sru.andgate.bitbot.graphics;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import javax.microedition.khronos.opengles.GL10;
 
 import edu.sru.andgate.bitbot.Bot;
 import edu.sru.andgate.bitbot.R;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.media.MediaPlayer;
 import android.opengl.GLUtils;
 import java.util.*;
 
 public class DrawableGun implements Drawable
 {
 	float[] parameters;
 	int ID = 0;
 	int textureCount = 0;
 	int MAX_TEXTURE_ARRAY_SIZE = 5;
 	int SELECTED_TEXTURE = 0;
 	float moveAngle = 90.0f;
 	float moveStepSize = 0.05f;	//1.7 MAX before tunneling
 	ArrayList<Integer> textureHopper;
 	ArrayList<Integer> layerIdList;
 	boolean textureLoaded = false;
 	BotLayer masterTurret;
 	DrawableBot masterBot;
 	int masterBotID = -1;
 	int numLiveRounds = 0;
 	float[][] bullets;
 	int[] liveRounds;
 	
 	int MAX_BULLETS = 4;
 	float BOUNDING_RADIUS = 0.2f;
 	int DAMAGE = 20; //35
 	float SPEED = 0.4f;
 	float LIFESPAN = 6.0f;
	float BARREL_LENGTH = 0.8f;
 	
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
 
 	public DrawableGun(DrawableBot bot, BotLayer turret)
 	{
 		//Attach to bot
 		masterTurret = turret;
 		masterBot = bot;
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
 		
 		//liveRounds = new ArrayList<Integer>(MAX_BULLETS);
 		liveRounds = new int[MAX_BULLETS];
 		for(int i=0;i<MAX_BULLETS;i++)
 		{
 			liveRounds[i] = 0;
 		}
 		
 		bullets = new float[MAX_BULLETS][4]; //Index -> LocX, LocY, Lifetime Remaining, Trajectory
 		
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
 	public void moveByTouch(float speed)
 	{
 		//
 	}
 	
 	/* (non-Javadoc)
 	 * @see edu.sru.andgate.bitbot.graphics.Drawable#move(float, float)
 	 */
 	@Override
 	public void move(float angle, float stepSize)
 	{
 		float rise = (float)(Math.sin(angle * (Math.PI / 180)) * stepSize) + parameters[1];
 		float run = (float)(Math.cos(angle * (Math.PI / 180)) * stepSize) + parameters[0];
 		
 		parameters[0] = run;
 		parameters[1] = rise;
 	}
 	
 	public void move()
 	{
 		float rise = (float)(Math.sin(moveAngle * (Math.PI / 180)) * moveStepSize) + parameters[1];
 		float run = (float)(Math.cos(moveAngle * (Math.PI / 180)) * moveStepSize) + parameters[0];
 		
 		parameters[0] = run;
 		parameters[1] = rise;
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
 	
 	public void setBoundingRadius(float radius)
 	{
 		BOUNDING_RADIUS = radius;
 	}
 	
 	public void onBoundaryCollision()
 	{
 		//For now, flip angle and continue
 		moveAngle = Math.abs(moveAngle - 360.0f) % 360.0f;
 		parameters[3] = moveAngle + 90.0f;
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
 	
 	public void attachMasterBot(DrawableBot bot)
 	{
 		masterBot = bot;
 		masterBotID = masterBot.ID;
 	}
 	
 	public void setMasterBotID(int ID)
 	{
 		masterBotID = ID;
 	}
 	
 	public void fire()
 	{
 		int workingBulletID = -1;
 		
 		//Get an available BulletID
 		if(numLiveRounds < MAX_BULLETS)
 		{
 			for(int i=0;i<MAX_BULLETS;i++)
 			{
 				if(liveRounds[i] == 0)
 				{
 					workingBulletID = i;
 					liveRounds[i] = 1;
 					numLiveRounds++;
 					break;
 				}
 			}
 		}
 		//If a bulletID was available, calculate bullet info
 		if(workingBulletID != -1)
 		{
 			//Calculate Trajectory Info
 			float trajectoryAngle = masterTurret.layerParameters[3];
 			float startingX = masterTurret.masterBotLayer.parameters[0] - ((float)(Math.sin(trajectoryAngle * (Math.PI / 180)) * BARREL_LENGTH));
 			float startingY = (float)(Math.cos(trajectoryAngle * (Math.PI / 180)) * BARREL_LENGTH) + masterTurret.masterBotLayer.parameters[1];
 			//Index -> LocX, LocY, Lifetime Remaining, Trajectory
 			bullets[workingBulletID][0] = startingX;
 			bullets[workingBulletID][1] = startingY;
 			bullets[workingBulletID][2] = LIFESPAN;
 			bullets[workingBulletID][3] = trajectoryAngle;
 		}
 	}
 	
 	public void update()
 	{
 		//Update live bullet positions, decrement lifetime remaining
 		for(int i=0;i<MAX_BULLETS;i++)
 		{
 			if(liveRounds[i] == 1)
 			{
 				//Calculate next bullet x and y position				
 				float nextX = bullets[i][0] - ((float)(Math.sin(bullets[i][3] * (Math.PI / 180)) * SPEED));
 				float nextY = (float)(Math.cos(bullets[i][3] * (Math.PI / 180)) * SPEED) + bullets[i][1];
 				
 				//Update Bullet Position
 				bullets[i][0] = nextX;
 				bullets[i][1] = nextY;
 				//Decrement Lifespan
 				bullets[i][2] -= 0.2f;
 				//If the bullet has reached the end of its life, make it inactive
 				if(bullets[i][2] <= 0)
 				{
 					liveRounds[i] = 0;
 					numLiveRounds--;
 				}
 			}
 		}
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
 		
 		//Draw live rounds to screen
 		for(int i=0;i<MAX_BULLETS;i++)
 		{
 			//If live round, prepare and draw
 			if(liveRounds[i] == 1)
 			{
 				gl.glLoadIdentity();
 				//Translate Bullet
 				gl.glTranslatef(bullets[i][0],bullets[i][1], -5.0f);						//Translate Object
 				//gl.glRotatef(parameters[3], parameters[4], parameters[5], parameters[6]);	//Rotate Object
 				gl.glScalef(0.2f, 0.2f, 0.2f);												//Scale Object
 				
 				//Draw
 				gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);
 			}
 		}
 
 		//Disable the client state before leaving
 		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
 		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
 	}
 
 }
