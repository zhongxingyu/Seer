 package com.cg.trashman;
 
 import static javax.media.opengl.GL.GL_COLOR_BUFFER_BIT;
 import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;
 import static javax.media.opengl.GL.GL_LINEAR;
 import static javax.media.opengl.GL.GL_LINEAR_MIPMAP_NEAREST;
 import static javax.media.opengl.GL.GL_NEAREST;
 import static javax.media.opengl.GL.GL_TEXTURE_2D;
 import static javax.media.opengl.GL.GL_TEXTURE_MAG_FILTER;
 import static javax.media.opengl.GL.GL_TEXTURE_MIN_FILTER;
 import static javax.media.opengl.GL.GL_TRIANGLES;
 import static javax.media.opengl.GL2.GL_QUADS;
 import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
 import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;
 
 import java.awt.Font;
 import java.awt.event.KeyEvent;
 import java.awt.geom.Rectangle2D;
 
 import javax.media.opengl.GL2;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.glu.GLU;
 
 import com.jogamp.opengl.util.awt.TextRenderer;
 import com.jogamp.opengl.util.texture.Texture;
 import com.jogamp.opengl.util.texture.TextureCoords;
 
 public class MenuScene implements IScene {
 	private GL2 gl;
 	private GLU glu;
 	private MainGLCanvas mainGLCanvas;
 	private Texture[] textures;
 	private float textureTop;
 	private float textureBottom;
 	private float textureLeft;
 	private float textureRight;
 
 	private float angleCar;
 	private TextRenderer textTitle;
 	private TextRenderer textCredit;
 	private TextRenderer textInfo;
 
 	public MenuScene() {
 
 	}
 
 	@Override
 	public void init(GL2 gl, GLU glu, MainGLCanvas mainGLCanvas) {
 		this.gl = gl;
 		this.glu = glu;
 		this.mainGLCanvas = mainGLCanvas;
 		textures = mainGLCanvas.textures;
 		TextureCoords textureCoords = textures[0].getImageTexCoords();
 		textureTop = textureCoords.top();
 		textureBottom = textureCoords.bottom();
 		textureLeft = textureCoords.left();
 		textureRight = textureCoords.right();
 
 		angleCar = 0f;
 		textTitle = new TextRenderer(new Font("SansSerif", Font.BOLD, 90));
 		textInfo = new TextRenderer(new Font("SansSerif", Font.BOLD, 40));
 		textCredit = new TextRenderer(new Font("SansSerif", Font.BOLD, 20));
 
 	}
 
 	@Override
 	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
 			int height) {
 
 	}
 
 	@Override
 	public void display(GLAutoDrawable drawable) {
 		gl.glMatrixMode(GL_PROJECTION);
 		gl.glLoadIdentity();
 		glu.gluPerspective(45.0, 1.55f, 0.1, 100.0);
 		gl.glMatrixMode(GL_MODELVIEW);
 
 		GL2 gl = drawable.getGL().getGL2(); // get the OpenGL 2 graphics context
 		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 		gl.glColor3f(1f, 1f, 1f);
 
 		drawBigCar();
 		angleCar += 0.8f;
 
 		// draw title
 		String str = "Trashman";
 		textTitle.beginRendering(drawable.getWidth(), drawable.getHeight());
 		textTitle.setColor(1f, 1f, 1f, 1f);
 		Rectangle2D textBox = textTitle.getBounds(str);
 		textTitle.draw(str, 400 - ((int) textBox.getWidth() / 2), 460);
 		textTitle.endRendering();
 
 		// draw credit
 		str = "made by 8-bit builder";
 		textCredit.beginRendering(drawable.getWidth(), drawable.getHeight());
 		textCredit.setColor(1f, 1f, 1f, 1f);
 		textBox = textCredit.getBounds(str);
 		textCredit.draw(str, 400 - ((int) textBox.getWidth() / 2), 436);
 		textCredit.endRendering();
 
 		// draw info
 		str = "Press Enter To Play";
 		textInfo.beginRendering(drawable.getWidth(), drawable.getHeight());
 		textInfo.setColor(1f, 1f, 1f, 1f);
 		textBox = textInfo.getBounds(str);
 		textInfo.draw(str, 400 - ((int) textBox.getWidth() / 2), 150);
 		textInfo.endRendering();
 
 		// How to play
		str = "Tip : use Arrow key or WASD to move the truck";
 		textCredit.beginRendering(drawable.getWidth(), drawable.getHeight());
 		textCredit.setColor(1f, 1f, 0f, 1f);
 		textBox = textCredit.getBounds(str);
 		textCredit.draw(str, 400 - ((int) textBox.getWidth() / 2), 120);
 		textCredit.endRendering();
 
 		// credit
 		str = "Thanakorn Panyapiang";
 		textCredit.beginRendering(drawable.getWidth(), drawable.getHeight());
 		textCredit.setColor(0.4f, 0.4f, 0.4f, 1f);
 		textCredit.draw(str, 20, 20);
 		textCredit.endRendering();
 
 		str = "Saran Siripuekpong";
 		textCredit.beginRendering(drawable.getWidth(), drawable.getHeight());
 		textCredit.setColor(0.4f, 0.4f, 0.4f, 1f);
 		textCredit.draw(str, 20, 50);
 		textCredit.endRendering();
 
 	}
 
 	@Override
 	public void dispose(GLAutoDrawable drawable) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void keyPressed(KeyEvent event) {
 		if (event.getKeyCode() == KeyEvent.VK_ENTER) {
 			mainGLCanvas.setScene(1);
 
 		}
 	}
 
 	@Override
 	public void keyReleased(KeyEvent event) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void keyTyped(KeyEvent event) {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void drawBigCar() {
 		float size = 1f;
 		// ----- Render the Color Cube -----
 		gl.glLoadIdentity(); // reset the current model-view matrix
 		// gl.glTranslatef(pX, 0, 0); // translate right and into the
 		// screen
 
 		gl.glTranslatef(0f, 0.0f, -5.0f);
 		gl.glRotatef(angleCar, 0f, 1f, 0f);
 		// side car (for front face)
 		textures[11].enable(gl);
 		textures[11].bind(gl);
 		gl.glBegin(GL_QUADS);
 		// Front Face
 		gl.glTexCoord2f(textureLeft, textureBottom);
 		gl.glVertex3f(-size, -0.5f * size, 0.5f * size);
 		gl.glTexCoord2f(textureRight, textureBottom);
 		gl.glVertex3f(size, -0.5f * size, 0.5f * size);
 		gl.glTexCoord2f(textureRight, textureTop);
 		gl.glVertex3f(size, 0.5f * size, 0.5f * size);
 		gl.glTexCoord2f(textureLeft, textureTop);
 		gl.glVertex3f(-size, 0.5f * size, 0.5f * size);
 		gl.glEnd();
 
 		// inverse front face
 		textures[11].enable(gl);
 		textures[11].bind(gl);
 		gl.glBegin(GL_QUADS);
 		// Back Face
 		gl.glTexCoord2f(textureLeft, textureBottom);
 		gl.glVertex3f(-size, -0.5f * size, -0.5f * size);
 		gl.glTexCoord2f(textureLeft, textureTop);
 		gl.glVertex3f(-size, 0.5f * size, -0.5f * size);
 		gl.glTexCoord2f(textureRight, textureTop);
 		gl.glVertex3f(size, 0.5f * size, -0.5f * size);
 		gl.glTexCoord2f(textureRight, textureBottom);
 		gl.glVertex3f(size, -0.5f * size, -0.5f * size);
 		gl.glEnd();
 
 		// top car (for Top face)
 		textures[14].enable(gl);
 		textures[14].bind(gl);
 		gl.glBegin(GL_QUADS);
 		// Top Face
 		gl.glTexCoord2f(textureLeft, textureTop);
 		gl.glVertex3f(-size, 0.5f * size, -0.5f * size);
 		gl.glTexCoord2f(textureLeft, textureBottom);
 		gl.glVertex3f(-size, 0.5f * size, 0.5f * size);
 		gl.glTexCoord2f(textureRight, textureBottom);
 		gl.glVertex3f(size, 0.5f * size, 0.5f * size);
 		gl.glTexCoord2f(textureRight, textureTop);
 		gl.glVertex3f(size, 0.5f * size, -0.5f * size);
 		gl.glEnd();
 
 		// carBack
 		textures[13].enable(gl);
 		textures[13].bind(gl);
 		gl.glBegin(GL_QUADS);
 		// Right face
 		gl.glTexCoord2f(textureRight, textureBottom);
 		gl.glVertex3f(size, -0.5f * size, -0.5f * size);
 		gl.glTexCoord2f(textureRight, textureTop);
 		gl.glVertex3f(size, 0.5f * size, -0.5f * size);
 		gl.glTexCoord2f(textureLeft, textureTop);
 		gl.glVertex3f(size, 0.5f * size, 0.5f * size);
 		gl.glTexCoord2f(textureLeft, textureBottom);
 		gl.glVertex3f(size, -0.5f * size, 0.5f * size);
 		gl.glEnd();
 
 		// carFront
 		textures[12].enable(gl);
 		textures[12].bind(gl);
 		gl.glBegin(GL_QUADS);
 		// Left Face
 		gl.glTexCoord2f(textureLeft, textureBottom);
 		gl.glVertex3f(-size, -0.5f * size, -0.5f * size);
 		gl.glTexCoord2f(textureRight, textureBottom);
 		gl.glVertex3f(-size, -0.5f * size, 0.5f * size);
 		gl.glTexCoord2f(textureRight, textureTop);
 		gl.glVertex3f(-size, 0.5f * size, 0.5f * size);
 		gl.glTexCoord2f(textureLeft, textureTop);
 		gl.glVertex3f(-size, 0.5f * size, -0.5f * size);
 		gl.glEnd();
 	}
 
 	@Override
 	public void refresh() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
