 package things;
 
 import org.newdawn.slick.opengl.Texture;
 
 import entities.Agent.direction;
 
 public abstract class Thing {
 	//State
 	boolean blocking = true;
 	boolean crossable = false;
 	boolean ramp = false;
 	private boolean lightSource = false;
 	private int[] pos = new int[3]; //Position (x, y, z)
 	private direction dir;	//direction the thing is facing
 	
 	//Texture
 	Texture texture;
 	int texRow;
 	int texCol;
 	protected int TEXTURE_SIZE_X = 24;	//Sprite sheet character width, this can be overridden
 	protected int TEXTURE_SIZE_Y = 32;	//Sprite sheet character height, this can be overridden
 	protected int TEXTURE_SHEET_WIDTH = 256; //Sprite sheet height, this can be overridden (must be a multiple of 2)
 	protected int TEXTURE_SHEET_HEIGHT = 256; //Sprite sheet width, this can be overridden (must be a multiple of 2)
 	
 	/**
 	 * Load any required textures for displaying
 	 */
 	public abstract void loadTextures();
 	
 	/**
 	 * Render the Thing with OpenGL
 	 * @param pixelSize the ratio of real screen pixels to in game pixels
 	 * @param terrainTextureSize the pixel dimensions of a terrain tile
 	 */
 	public abstract void renderThing(int pixelSize, int terrainTextureSize);
 	
 	/**
 	 * Getter for blocking
 	 * @return true if blocking
 	 */
 	public boolean isBlocking()
 	{
 		return blocking;
 	}
 	
 	/**
 	 * Getter for crossable (i.e. an agent can walk over it even if there is no ground below)
 	 * @return true if crossable
 	 */
 	public boolean isCrossable()
 	{
 		return crossable;
 	}
 	
 	/**
 	 * Getter for whether or not something is a ramp (i.e. an agent walking on it will change
 	 * the (x,y) position and the z position)
 	 * @return true if it is a ramp
 	 */
 	public boolean isRamp()
 	{
 		return ramp;
 	}
 
 	public void setPos(int[] pos) {
 		this.pos = pos;
 	}
 
 	public int[] getPos() {
 		return pos;
 	}
 
 	public void setDir(direction dir) {
 		this.dir = dir;
 	}
 
 	public direction getDir() {
 		return dir;
 	}
 
 	public void setLightSource(boolean lightSource) {
 		this.lightSource = lightSource;
 	}
 
 	public boolean isLightSource() {
 		return lightSource;
 	}
 }
