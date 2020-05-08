 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.util.ArrayList;
 
 import javax.swing.JOptionPane;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.util.glu.GLU;
 
 
 public class Window {
 	
 	private int width;
 	private int height;
 	private float angle;
 	
 	private boolean forward;
 	private boolean back;
 	private boolean left;
 	private boolean right;
 	
 	private int[][] map= {
 			{1,0,1,0},
 			{0,1,0,1},
 			{1,0,1,0},
 			{0,1,0,1}
 		};
 	
 	private ArrayList<Block> blocks;
 	
 	float x;
 	float y;
 	float z;
 	float camAngle;
 	float lx;
 	float lz;
 	/**
 	 * Constructor creates a new Display
 	 * @param width
 	 * @param height
 	 */
 	public Window(int width, int height){
 		this.width = width;
 		this.height = height;
 		
 		angle = 0;
 		x = 5;
 		y = 0;
 		z= 0;
 		camAngle = 0;
 		
 		blocks = new ArrayList<Block>(map.length + map[0].length + 1);
 		
 		try {
 			Display.setDisplayMode(new DisplayMode(width, height));
 			Display.create();
 		} catch (LWJGLException e) {
 			JOptionPane.showMessageDialog(null, "Could not create display (see stack trace)");
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Performs rendering in a loop while the window is not closed
 	 */
 	public void renderLoop(){
 		initLights();
 		ImageBank.texInit();
 		
 		GL11.glMatrixMode(GL11.GL_PROJECTION);
 		GL11.glLoadIdentity();
 		GL11.glEnable(GL11.GL_TEXTURE_2D);
 		GL11.glEnable(GL11.GL_DEPTH_TEST);
 		GL11.glDepthFunc(GL11.GL_LEQUAL);
 		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
 		GLU.gluPerspective(45, width/height, 1, 100);
 		GL11.glMatrixMode(GL11.GL_MODELVIEW);
 		GL11.glViewport(0, 0, width, height);
 		
 		initBlocks();
 		
 		while(!Display.isCloseRequested()){
 			GL11.glLoadIdentity();
 			GLU.gluLookAt(x, y, z,
 						  x+lx, 0, z+lz,
 						  0, 1, 0);
 			
 			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 			GL11.glEnable(GL11.GL_LIGHTING);
 			GL11.glPushMatrix();
 			GL11.glTranslatef(0, -0.5f, -8);
 			//GL11.glRotatef(angle, 1, 0, 0);
 		
 			renderBlocks();
 			
 			GL11.glPopMatrix();
 			GL11.glDisable(GL11.GL_LIGHTING);
 			
 			incAngle(0.01f);
 			
 			Display.update();
 			
 			pollInput();
 			checkMovement();
 		}
 		Display.destroy();
 	}
 
 	/**
 	 * @return the width
 	 */
 	public int getWidth() {
 		return width;
 	}
 
 	/**
 	 * @return the height
 	 */
 	public int getHeight() {
 		return height;
 	}
 	
 	public void incAngle(float inc){
 		
 		angle += inc;
 		
 		while (angle > 360.0){
 			angle -= 360.0;
 		}
 	}
 	
 	/**
 	 * Processes keyboard input
 	 */
 	public void pollInput(){
 		forward = Keyboard.isKeyDown(Keyboard.KEY_W);
 		
 		back = Keyboard.isKeyDown(Keyboard.KEY_S);
 		
 		left = Keyboard.isKeyDown(Keyboard.KEY_A);
 		
 		right = Keyboard.isKeyDown(Keyboard.KEY_D);
 	}
 	
 	/**
 	 * Moves the camera
 	 */
 	public void checkMovement(){
 		
 		float fraction = 0.001f;
 		
 		if(right){
 			camAngle += 0.001f;
 			lx = (float)Math.sin(camAngle);
 			lz = -(float)Math.cos(camAngle);
 		}
 		
 		if(left){
 			camAngle -= 0.001f;
 			lx = (float)Math.sin(camAngle);
 			lz = -(float)Math.cos(camAngle);
 		}
 		
 		if(forward){
 			x += lx * fraction;
 			z += lz * fraction;
 		}
 		
 		if(back){
 			x -= lx * fraction;
 			z -= lz * fraction;
 		}
 	}
 	
 	/**
 	 * Initializes the blocks
 	 */
 	public void initBlocks(){
		for (int i=0; i<4; i++){
			for (int j=0; j<4; j++){
				if (map[i][j] == 1){
					blocks.add(new Block(i*3, 0, j*3, 2));
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Renders the blocks
 	 */
 	public void renderBlocks(){
 		for(int i = 0; i < blocks.size(); i++){
 			blocks.get(i).renderObject();
 		}
 	}
 	
 	public void initLights(){
 		GL11.glShadeModel(GL11.GL_SMOOTH);
 		float[] lightAmbient = {0.5f, 0.5f, 0.5f, 1.0f};
 		float[] lightDiffuse = { 1.0f, 1.0f, 1.0f, 1.0f };
 		float[] lightPosition = { 1.0f, 1.0f, 2.0f, 1.0f };
 		
 		ByteBuffer temp = ByteBuffer.allocateDirect(16);
         temp.order(ByteOrder.nativeOrder());
         GL11.glLight(GL11.GL_LIGHT1, GL11.GL_AMBIENT, (FloatBuffer)temp.asFloatBuffer().put(lightAmbient).flip());              // Setup The Ambient Light
         GL11.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, (FloatBuffer)temp.asFloatBuffer().put(lightDiffuse).flip());              // Setup The Diffuse Light
         GL11.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION,(FloatBuffer)temp.asFloatBuffer().put(lightPosition).flip());
 		
 		GL11.glEnable(GL11.GL_LIGHT1);
 	}
 }
