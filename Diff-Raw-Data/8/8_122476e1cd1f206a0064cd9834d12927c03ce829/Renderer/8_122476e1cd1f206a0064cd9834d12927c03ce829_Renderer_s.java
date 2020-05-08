 package pong.view;
 
 import static pong.model.Const.GAME_DEPTH;
 import static pong.model.Const.GAME_HEIGHT;
 import static pong.model.Const.GAME_WIDTH;
 import static pong.model.Const.MENU_BACK;
 import static pong.model.Const.MENU_BOTTOM;
 import static pong.model.Const.MENU_FRONT;
 import static pong.model.Const.MENU_LEFT;
 import static pong.model.Const.MENU_RIGHT;
 import static pong.model.Const.MENU_TOP;
 import static pong.model.Const.SCREEN_HEIGHT;
 import static pong.model.Const.SCREEN_WIDTH;
 
 import java.awt.Font;
 import java.awt.geom.Rectangle2D;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InvalidClassException;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLProfile;
 import javax.media.opengl.glu.GLU;
 import javax.media.opengl.glu.GLUquadric;
 
 import pong.model.Ball;
 import pong.model.GameItem;
 import pong.model.MenuCube;
 import pong.model.Paddle;
 
 import com.jogamp.opengl.util.awt.TextRenderer;
 import com.jogamp.opengl.util.gl2.GLUT;
 import com.jogamp.opengl.util.texture.Texture;
 import com.jogamp.opengl.util.texture.TextureData;
 import com.jogamp.opengl.util.texture.TextureIO;
 
 public class Renderer {
 	private GLU glu;
 
 	private float bgRotation = 90;
 	private Texture balltexture;
 	private Texture spacetexture;
 	private Texture paddletexture;
 	private Texture washedtexture;
 	private TextRenderer textrenderer;
 	private float textScaleFactor;
 
 	public Renderer(GLU glu) {
 		this.glu = glu;
 	}
 
 	/**
 	 * Loads the textures from imagefiles into Texture-objects
 	 */
 	public void setupTextures(){
 		spacetexture = loadTexture("outer_space_trip.jpg");
 		balltexture = loadTexture("earth-1k.png");
 		paddletexture = loadTexture("paddle_texture1.jpg");
 		washedtexture = loadTexture("washedtexture4.jpg");
 	}
 
 	/** 
 	 * Load texture into GraphicsEngine as a String example: earth-1k.png
 	 * @param String
 	 * @return Textureobject ready to be applied
 	 */
 	private Texture loadTexture(String texture){
 
 		// Load texture from resource directory, feel free to put files in there
 		try {
 			InputStream stream;
 			if( (stream = getClass().getResourceAsStream("/resource/" + texture)) == null )
 			{
 				System.out.println("Texture not loaded..");
 			}
 			TextureData data = TextureIO.newTextureData(GLProfile.getDefault(), stream, false, "png");
 			return TextureIO.newTexture(data);
 		}
 		catch (IOException exc) {
 			exc.printStackTrace();
 			System.exit(1);
 		}
 		return null;
 	}
 
 
 	/********************************************
 	 * 											*
 	 * 			Drawing-methods Below			*
 	 * 											*
 	 ********************************************/
 
 	/**
 	 * Renders a 3D ball.
 	 */
 	public void drawBall(GL2 gl, GameItem item) throws InvalidClassException {
 		float x, y, z, r;
 
 		// Check that item is a Paddle. Need to support all classes that have the same shape in the future.
 		if (item.getType().equals("BALL")) {
 			Ball Ball = (Ball) item;
 			x = Ball.getxPos();
 			y = Ball.getyPos();
 			z = Ball.getzPos();
 			r = Ball.getRadius();
 		} else {
 			throw new InvalidClassException("Wrong class of GameItem in draw3DRectangle(GL2 gl, GameItem item)");
 		}
 
 		// Enable texturing.
 		gl.glEnable(GL.GL_TEXTURE_2D);
 
 		// Apply earth texture
 		balltexture.enable(gl);
 		balltexture.bind(gl);
 
 		gl.glTranslatef(x, y, z);
 		// Draw Ball (possible styles: FILL, LINE, POINT).
 		//		gl.glRotatef(rotation, 1.0f, 1.0f, 1.0f);
 		gl.glColor3f(0.3f, 0.5f, 1f);
 		GLUquadric ball = glu.gluNewQuadric();
 		glu.gluQuadricTexture(ball, true);
 		glu.gluQuadricDrawStyle(ball, GLU.GLU_FILL);
 		glu.gluQuadricNormals(ball, GLU.GLU_FLAT);
 		glu.gluQuadricOrientation(ball, GLU.GLU_OUTSIDE);
 		final int slices = 16;
 		final int stacks = 16;
 		glu.gluSphere(ball, r, slices, stacks);
 		glu.gluDeleteQuadric(ball);
 
 		gl.glDisable(GL.GL_TEXTURE_2D);
 	}
 
 
 	/**
 	 * Renders a 3D rectangle.
 	 * This can be used to draw a paddle or obstacle or something else.
 	 */
 	public void draw3DRectangle(GL2 gl, Object item) throws InvalidClassException {
 		float x, y, z, w, h, d;
 		Texture texture;
 
 		// Check that item is a Paddle. Need to support all classes that have the same shape in the future.
 		if (item instanceof Paddle) {
 			Paddle pad = (Paddle) item;
 			x = pad.getxPos();
 			y = pad.getyPos();
 			z = pad.getzPos();
 			w = pad.getWidth();
 			h = pad.getHeight();
 			d = pad.getDepth();
 			texture = this.paddletexture;
 		} 
 		//check if object is a menu cube, set appropriate texture for menu cube
 		else if (item instanceof MenuCube) {
 			MenuCube menuCube = (MenuCube) item;
 			x = menuCube.getxPos();
 			y = menuCube.getyPos();
 			z = menuCube.getzPos();
 			w = menuCube.getWidth();
 			h = menuCube.getHeight();
 			d = menuCube.getDepth();
 			texture = this.paddletexture;
 		} else {
 			throw new InvalidClassException("Wrong class of GameItem in draw3DRectangle(GL2 gl, GameItem item)");
 		}
 
 		// Move to right coordinates.
 		gl.glTranslatef(x, y, z);
 
 		// Apply earth texture
 		texture.enable(gl);
 		texture.bind(gl);
 
 		gl.glBegin(GL2.GL_QUADS); // Draw A Quad
 
 		gl.glNormal3f(0.0f, 1.0f, 0.0f);
 		gl.glVertex3f(w / 2f, h / 2f, -d / 2f); gl.glTexCoord2d(0.0f, 0.0f);   // Top Right Of The Quad (Top)
 		gl.glVertex3f(-w / 2f, h / 2f, -d / 2f); gl.glTexCoord2d(1.0f, 0.0f);  // Top Left Of The Quad (Top)
 		gl.glVertex3f(-w / 2f, h / 2f, d / 2f); gl.glTexCoord2d(1.0f, 1.0f); // Bottom Left Of The Quad (Top)
 		gl.glVertex3f(w / 2f, h / 2f, d / 2f); gl.glTexCoord2d(0.0f, 1.0f); // Bottom Right Of The Quad (Top)
 		
 		gl.glNormal3f(0.0f, -1.0f, 0.0f);
 		gl.glVertex3f(w / 2f, -h / 2f, d / 2f); gl.glTexCoord2d(0.0f, 0.0f); // Top Right Of The Quad (Bottom)
 		gl.glVertex3f(-w / 2f, -h / 2f, d / 2f); gl.glTexCoord2d(1.0f, 0.0f);  // Top Left Of The Quad (Bottom)
 		gl.glVertex3f(-w / 2f, -h / 2f, -d / 2f); gl.glTexCoord2d(1.0f, 1.0f); // Bottom Left Of The Quad (Bottom)
 		gl.glVertex3f(w / 2f, -h / 2f, -d / 2f); gl.glTexCoord2d(0.0f, 1.0f); // Bottom Right Of The Quad (Bottom)
 
 		gl.glNormal3f(0.0f, 0.0f, 1.0f);
 		gl.glVertex3f(w / 2, h / 2f, d / 2f); gl.glTexCoord2d(0.0f, 0.0f); // Top Right Of The Quad (Front)
 		gl.glVertex3f(-w / 2, h / 2f, d / 2f); gl.glTexCoord2d(1.0f, 0.0f); // Top Left Of The Quad (Front)
 		gl.glVertex3f(-w / 2, -h / 2f, d / 2f); gl.glTexCoord2d(1.0f, 1.0f); // Bottom Left Of The Quad (Front)
 		gl.glVertex3f(w / 2, -h / 2f, d / 2f); gl.glTexCoord2d(0.0f, 1.0f); // Bottom Right Of The Quad (Front)
 
 		gl.glNormal3f(0.0f, 0.0f, -1.0f);
 		gl.glVertex3f(w / 2f, -h / 2f, -d / 2f); gl.glTexCoord2d(0.0f, 0.0f); // Bottom Left Of The Quad (Back)
 		gl.glVertex3f(-w / 2f, -h / 2f, -d / 2f); gl.glTexCoord2d(1.0f, 0.0f); // Bottom Right Of The Quad (Back)
 		gl.glVertex3f(-w / 2f, h / 2f, -d / 2f); gl.glTexCoord2d(1.0f, 1.0f); // Top Right Of The Quad (Back)
 		gl.glVertex3f(w / 2f, h / 2f, -d / 2f); gl.glTexCoord2d(0.0f, 1.0f); // Top Left Of The Quad (Back)
 
 		gl.glNormal3f(-1.0f, 0.0f, 0.0f);
 		gl.glVertex3f(-w / 2f, h / 2f, d / 2f); gl.glTexCoord2d(0.0f, 0.0f); // Top Right Of The Quad (Left)
 		gl.glVertex3f(-w / 2f, h / 2f, -d / 2f); gl.glTexCoord2d(1.0f, 0.0f); // Top Left Of The Quad (Left)
 		gl.glVertex3f(-w / 2f, -h / 2f, -d / 2f); gl.glTexCoord2d(1.0f, 1.0f); // Bottom Left Of The Quad (Left)
 		gl.glVertex3f(-w / 2f, -h / 2f, d / 2f); gl.glTexCoord2d(0.0f, 1.0f); // Bottom Right Of The Quad (Left)
 
 		gl.glNormal3f(1.0f, 0.0f, 0.0f);
 		gl.glVertex3f(w / 2f, h / 2f, -d / 2f); gl.glTexCoord2d(0.0f, 0.0f); // Top Right Of The Quad (Right)
 		gl.glVertex3f(w / 2f, h / 2f, d / 2f); gl.glTexCoord2d(1.0f, 0.0f); // Top Left Of The Quad (Right)
 		gl.glVertex3f(w / 2f, -h / 2f, d / 2f); gl.glTexCoord2d(1.0f, 1.0f); // Bottom Left Of The Quad (Right)
 		gl.glVertex3f(w / 2f, -h / 2f, -d / 2f); gl.glTexCoord2d(0.0f, 1.0f); // Bottom Right Of The Quad (Right)
 
 		gl.glEnd(); // Done Drawing The Quad
 
 		gl.glDisable(GL.GL_TEXTURE_2D);
 	}
 
 	/**
 	 * Draws the gamearea.
 	 */
 	public void drawGamearea(GL2 gl){
 		washedtexture.enable(gl);
 		washedtexture.bind(gl);
 		gl.glBegin(GL2.GL_QUADS);
 
 		gl.glNormal3f(1.0f, 0.0f, 0.0f);
 		gl.glVertex3f(GAME_WIDTH / 2f, GAME_HEIGHT / 2f, -GAME_DEPTH/2); gl.glTexCoord2d(1.0f, 1.0f); // Top Right Of The Quad (Top Wall)
 		gl.glVertex3f(-GAME_WIDTH / 2, GAME_HEIGHT / 2f, -GAME_DEPTH/2); gl.glTexCoord2d(0.0f, 1.0f); // Top Left Of The Quad (Top Wall)
 		gl.glVertex3f(-GAME_WIDTH / 2f, GAME_HEIGHT / 2f, GAME_DEPTH/2); gl.glTexCoord2d(0.0f, 0.0f); // Bottom Left Of The Quad (Top Wall)
 		gl.glVertex3f(GAME_WIDTH / 2f, GAME_HEIGHT / 2f, GAME_DEPTH/2);  gl.glTexCoord2d(1.0f, 0.0f);// Bottom Right Of The Quad (Top Wall)
 
 		gl.glNormal3f(1.0f, 0.0f, 0.0f);
 		gl.glVertex3f(GAME_WIDTH / 2f, -GAME_HEIGHT/2f, -GAME_DEPTH/2); gl.glTexCoord2d(1.0f, 1.0f); // Top Right Of The Quad (Bottom Wall)
 		gl.glVertex3f(-GAME_WIDTH / 2f, -GAME_HEIGHT/2f, -GAME_DEPTH/2); gl.glTexCoord2d(0.0f, 1.0f); // Top Left Of The Quad (Bottom Wall)
 		gl.glVertex3f(-GAME_WIDTH / 2f, -GAME_HEIGHT/2f, GAME_DEPTH/2);  gl.glTexCoord2d(0.0f, 0.0f); // Bottom Left Of The Quad (Bottom Wall)
 		gl.glVertex3f(GAME_WIDTH / 2f, -GAME_HEIGHT/2f, GAME_DEPTH/2); gl.glTexCoord2d(1.0f, 0.0f); // Bottom Right Of The Quad (Bottom Wall)
 
 		gl.glNormal3f(1.0f, 0.0f, 0.0f);
 		gl.glVertex3f(-GAME_WIDTH / 2f, GAME_HEIGHT / 2f, GAME_DEPTH/2); gl.glTexCoord2d(1.0f, 1.0f); // Top Right Of The Quad (Left Wall)
 		gl.glVertex3f(-GAME_WIDTH / 2f, GAME_HEIGHT / 2f, -GAME_DEPTH/2); gl.glTexCoord2d(0.0f, 1.0f); // Top Left Of The Quad (Left Wall)
 		gl.glVertex3f(-GAME_WIDTH / 2f, -GAME_HEIGHT / 2f, -GAME_DEPTH/2); gl.glTexCoord2d(0.0f, 0.0f); // Bottom Left Of The Quad (Left Wall)
 		gl.glVertex3f(-GAME_WIDTH / 2f, -GAME_HEIGHT / 2f, GAME_DEPTH/2); gl.glTexCoord2d(1.0f, 0.0f); // Bottom Right Of The Quad (Left Wall)
 
 		gl.glNormal3f(1.0f, 0.0f, 0.0f);
 		gl.glVertex3f(GAME_WIDTH / 2f, GAME_HEIGHT / 2f, -GAME_DEPTH/2); gl.glTexCoord2d(1.0f, 1.0f); // Top Right Of The Quad (Right Wall)
 		gl.glVertex3f(GAME_WIDTH / 2f, GAME_HEIGHT / 2f, GAME_DEPTH/2); gl.glTexCoord2d(0.0f, 1.0f); // Top Left Of The Quad (Right Wall)
 		gl.glVertex3f(GAME_WIDTH / 2f, -GAME_HEIGHT / 2f, GAME_DEPTH/2); gl.glTexCoord2d(0.0f, 0.0f); // Bottom Left Of The Quad (Right Wall)
 		gl.glVertex3f(GAME_WIDTH / 2f, -GAME_HEIGHT / 2f, -GAME_DEPTH/2); gl.glTexCoord2d(1.0f, 0.0f); // Bottom Right Of The Quad (Right Wall)
 
 		gl.glEnd();
 	}
 
 	/**
 	 * Draws the sphere around the game.
 	 */
 	public void drawBackground(GL2 gl){
 
 		gl.glTranslatef(0,0,0);
 		gl.glRotatef(bgRotation, 1.0f, 1.0f, 0.0f);
 		bgRotation+=0.01;
 
 		// Set radius of background sphere
 		int r = 100;
 
 		// Enable texturing.
 		gl.glEnable(GL.GL_TEXTURE_2D);
 
 		// Apply space texture
 		spacetexture.enable(gl);
 		spacetexture.bind(gl);
 
 		// Draw Ball (possible styles: FILL, LINE, POINT).
 		//gl.glRotatef(rotation, 1.0f, 1.0f, 1.0f);
 		gl.glColor3f(0.3f, 0.5f, 1f);
 		GLUquadric bkg = glu.gluNewQuadric();
 		glu.gluQuadricTexture(bkg, true);
 		glu.gluQuadricDrawStyle(bkg, GLU.GLU_FILL);
 		glu.gluQuadricNormals(bkg, GLU.GLU_FLAT);
 		glu.gluQuadricOrientation(bkg, GLU.GLU_OUTSIDE);
 		final int slices = 16;
 		final int stacks = 16;
 		glu.gluSphere(bkg, r, slices, stacks);
 		glu.gluDeleteQuadric(bkg);
 
 		gl.glDisable(GL.GL_TEXTURE_2D);
 
 	}
 	/**
 	 * Draws a string in 2D
 	 */
 	public void renderStrokeString(GL2 gl, int font, String string) {
 		GLUT glut = new GLUT();
 		// Center Our Text On The Screen
 		float width = glut.glutStrokeLength(font, string);
 		gl.glTranslatef(-width / 2f, 200, -700);
 		// Render The Text
 		glut.glutStrokeString(font, string);
 	}
 
 
 	public void renderTextAtPixels(int x, int y, int frameWidth, int frameHeight, String text, Font font){
 		TextRenderer tr = new TextRenderer(font);
 		tr.beginRendering(frameWidth, frameHeight);
 		tr.draw(text, x, y);
 		tr.endRendering();
 	}
 	
 	/**
 	 * Draws a string in 2D, is much nicer and can be customized in the init() method to set font and size
 	 * The text is draw depending on the position it gets from the call (x, y)
 	 */
 	public void render2DText(int x, int y, String text, GL2 gl){
 		
 		GLUT glut = new GLUT();
 		
 		// screen in an 18-point Helvetica font
 		gl.glRasterPos2i(x, y);
 		//gl.glColor4f(0.0f, 0.0f, 1.0f, 1.0f);
 		gl.glColor4f( 1f, 0.1f, 0.2f, .5f );
 	    gl.glTranslatef(0, 0, 0);
 	    //gl.glColor3f(1, 0, 0);
 		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, text);
 	}
 
 	public void render3DText(GLAutoDrawable drawable, int x, int y, String text){
 
 		textrenderer.begin3DRendering();
 
 		// optionally set the color
 		textrenderer.setColor(1.0f, 1.0f, 0.0f, 0.8f);
 		//MENU_ZPOS+MENU_SIZE/2
 		textrenderer.draw3D(text, SCREEN_HEIGHT/2+x, SCREEN_WIDTH/2+y, 0, 1f);
 		// ... more draw commands, color changes, etc.
 		textrenderer.end3DRendering();
 	}
 
 	/**
 	 * Draws a 3D cube with text on the sides, perhaps integrate this with draw3DRectangle() method?
 	 * @param drawable menuCube angles x,y,z to rotate
 	 */
 	//draws the menu cube
 	public void drawMenu(GLAutoDrawable drawable, MenuCube menu, float rx, float ry, float rz) {
 
 		//setup the text properties, size etc.
 		setUpText(drawable);
 
 		GL2 gl = drawable.getGL().getGL2();
 
 		//gl.glMatrixMode(GL2.GL_MODELVIEW);
 		//gl.glLoadIdentity();
 		/*glu.gluLookAt(0, 0, 2,
 				0, 0, 0,
 				0, 1, 0);*/
 		
 		gl.glTranslatef(menu.getxPos(),menu.getyPos(), menu.getzPos());
 		
 		//rotate the cube according to received arguments
 		gl.glRotatef(rx, 1, 0, 0);
 		gl.glRotatef(ry, 0, 1, 0);
 		gl.glRotatef(rz, 0, 1, 0);
 
 		// draw the six faces of the cube
 		// Top face
 		gl.glPushMatrix();
 		gl.glRotatef(-90, 1, 0, 0);
 		drawFace(gl, 1.0f, 0.2f, 0.2f, 0.8f, menu.getOptionsBySide(MENU_TOP).get(0));
 		gl.glPopMatrix();
 		// Front face
 		drawFace(gl, 1.0f, 0.8f, 0.2f, 0.2f, menu.getOptionsBySide(MENU_FRONT).get(0));
 		// Right face
 		gl.glPushMatrix();
 		gl.glRotatef(90, 0, 1, 0);
 		drawFace(gl, 1.0f, 0.2f, 0.8f, 0.2f, menu.getOptionsBySide(MENU_RIGHT).get(0));
 		// Back face    
 		gl.glRotatef(90, 0, 1, 0);
 		drawFace(gl, 1.0f, 0.8f, 0.8f, 0.2f, menu.getOptionsBySide(MENU_BACK).get(0));
 		// Left face    
 		gl.glRotatef(90, 0, 1, 0);
 		drawFace(gl, 1.0f, 0.2f, 0.8f, 0.8f, menu.getOptionsBySide(MENU_LEFT).get(0));
 		gl.glPopMatrix();
 		// Bottom face
 		gl.glPushMatrix();
 		gl.glRotatef(90, 1, 0, 0);
 		drawFace(gl, 1.0f, 0.8f, 0.2f, 0.8f, menu.getOptionsBySide(MENU_BOTTOM).get(0));
 		gl.glPopMatrix();
 	}
 
 	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
 		GL2 gl = drawable.getGL().getGL2();
 		gl.glMatrixMode(GL2.GL_PROJECTION);
 		gl.glLoadIdentity();
 		glu.gluPerspective(15, (float) width / (float) height, 5, 15);
 	}
 
 	//draws one face of a cube
 	private void drawFace(GL2 gl, float faceSize, float r, float g, float b, String text) {
 		//undersk 3d text http://www.geofx.com/html/OpenGL_Eclipse/TextRenderer3D.html			
 		//https://github.com/sgothel/jogl-utils/commit/bac504811d3fde4675b9a8f464a74f1c41be77d5#diff-2
 		//C:\Users\cf\Downloads\java3declipse-20090302	
 		
 		//set texture for the menu
 		Texture texture = this.paddletexture;
 		
 		float halfFaceSize = faceSize / 2;
 		gl.glColor3f(r, g, b);
 		
 		// Apply the texture
 		texture.enable(gl);
 		texture.bind(gl);
 
 		//draw side of cube+texture
 		gl.glBegin(GL2.GL_QUADS);
 		gl.glVertex3f(-halfFaceSize, -halfFaceSize, halfFaceSize);gl.glTexCoord2d(1.0f, 1.0f);//bottom left
 		gl.glVertex3f( halfFaceSize, -halfFaceSize, halfFaceSize);gl.glTexCoord2d(0.0f, 1.0f);//bottom right
 		gl.glVertex3f( halfFaceSize,  halfFaceSize, halfFaceSize);gl.glTexCoord2d(0.0f, 0.0f);//top right
 		gl.glVertex3f(-halfFaceSize,  halfFaceSize, halfFaceSize);gl.glTexCoord2d(1.0f, 0.0f);//top left
 		gl.glEnd();
 
 		gl.glDisable(GL.GL_TEXTURE_2D);
 		
 		textrenderer.begin3DRendering();
 		
 		//avoid z-fighting, that is between the text and the quad it sits on top of
 		gl.glDisable(GL2.GL_DEPTH_TEST);
 		//culling hides the backward facing sides of the cube ( the ones you are not supposed to see )
 		gl.glEnable(GL2.GL_CULL_FACE);
 		//scale text to quad
 		Rectangle2D bounds = textrenderer.getBounds(text);
 		float w = (float) bounds.getWidth();
 		float h = (float) bounds.getHeight();
 
 		textrenderer.draw3D(text,
 				w / -2.0f * textScaleFactor,
 				h / -2.0f * textScaleFactor,
 				halfFaceSize,
 				textScaleFactor);
 		
 		textrenderer.end3DRendering();
 		
 	}
 
 	//readies settings for printing 3D text
 	public void setUpText(GLAutoDrawable drawable) {
 		textrenderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 72));
 		GL2 gl = drawable.getGL().getGL2();
 		gl.glEnable(GL2.GL_DEPTH_TEST);
 
 		// Compute the scale factor of the largest string which will make
 		// them all fit on the faces of the cube
 		Rectangle2D bounds = textrenderer.getBounds("Bottom");
 		float w = (float) bounds.getWidth();
 		float h = (float) bounds.getHeight();
 		textScaleFactor = 1.0f / (w * 1.1f);
 	}
 	
 
 }
