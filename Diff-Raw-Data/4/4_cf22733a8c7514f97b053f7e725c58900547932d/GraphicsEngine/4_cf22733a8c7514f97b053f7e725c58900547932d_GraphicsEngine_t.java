 package com.avona.games.towerdefence.awt;
 
 import java.awt.Font;
 import java.awt.GraphicsEnvironment;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLCanvas;
 import javax.media.opengl.GLCapabilities;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.glu.GLU;
 import javax.vecmath.Point2d;
 
 import com.avona.games.towerdefence.Enemy;
 import com.avona.games.towerdefence.Game;
 import com.avona.games.towerdefence.Particle;
 import com.avona.games.towerdefence.TimeTrack;
 import com.avona.games.towerdefence.Tower;
 import com.avona.games.towerdefence.Util;
 import com.sun.opengl.util.j2d.TextRenderer;
 
 /**
  * The GraphicsEngine object currently incorporates all drawing operations. It
  * will iterate over all in-game objects and call (possibly overloaded) class
  * methods to perform the GL calls. It will not touch any in-game state, though.
  */
 public class GraphicsEngine implements GLEventListener {
 	protected Game game;
 	protected MainLoop main;
 	public GLCanvas canvas;
 	public GL gl;
 	public GLU glu;
 	public Point2d size;
 	public TimeTrack graphicsTime;
 
 	final public int defaultHeight = 600;
 	final public int defaultWidth = 800;
 
 	public GraphicsEngine(MainLoop main, Game game) {
 		this.main = main;
 		this.game = game;
 		this.graphicsTime = new TimeTrack();
 
 		glu = new GLU();
 		setupGlCanvas();
 	}
 
 	private void setupGlCanvas() {
 		GLCapabilities capabilities = new GLCapabilities();
 		capabilities.setDoubleBuffered(true);
 
 		canvas = new GLCanvas(capabilities);
 		canvas.addGLEventListener(this);
 		canvas.setAutoSwapBufferMode(true);
 	}
 
 	public void render(double gameDelta, double graphicsDelta) {
 		graphicsTime.updateTick(graphicsDelta);
 
 		// Paint background, clearing previous drawings.
 		gl.glColor3d(0.0, 0.0, 0.0);
 		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
 
 		for (Enemy e : game.enemies) {
 			renderEnemy(e);
 		}
 		for (Tower t : game.towers) {
 			renderTower(t);
 		}
 		for (Particle p : game.particles) {
 			renderParticle(p);
 		}
 		renderMouse();
 
 		renderer.beginRendering(800, 600);
 		// optionally set the color
 		renderer.setColor(1.0f, 0.2f, 0.2f, 0.8f);
 		renderer.draw("Text to draw", 100 + (int) (100 * Math
 				.sin(graphicsTime.clock)), 100 + (int) (100 * Math
 				.cos(graphicsTime.clock)));
 		// ... more draw commands, color changes, etc.
 		renderer.endRendering();
 	}
 
 	public void renderEnemy(final Enemy e) {
 		if (e.isDead())
 			return;
 
 		final double width = 0.04;
 		final Point2d location = e.location;
 
 		gl.glColor3d(0.0, 0.0, 1.0);
 
 		gl.glBegin(GL.GL_QUADS);
 		gl.glVertex2d(location.x - width / 2, location.y - width / 2);
 		gl.glVertex2d(location.x + width / 2, location.y - width / 2);
 		gl.glVertex2d(location.x + width / 2, location.y + width / 2);
 		gl.glVertex2d(location.x - width / 2, location.y + width / 2);
 		gl.glEnd();
 	}
 
 	public void renderTower(final Tower t) {
 		final double width = 0.03;
 		final Point2d location = t.location;
 
 		gl.glColor3d(1.0, 0.0, 0.0);
 
 		gl.glBegin(GL.GL_QUADS);
 		gl.glVertex2d(location.x - width / 2, location.y - width / 2);
 		gl.glVertex2d(location.x + width / 2, location.y - width / 2);
 		gl.glVertex2d(location.x + width / 2, location.y + width / 2);
 		gl.glVertex2d(location.x - width / 2, location.y + width / 2);
 		gl.glEnd();
 	}
 
 	public void renderParticle(final Particle p) {
 		if (p.isDead())
 			return;
 
 		final double width = 0.02;
 		final Point2d location = p.location;
 
 		gl.glColor3d(1.0, 1.0, 1.0);
 
 		gl.glBegin(GL.GL_QUADS);
 		gl.glVertex2d(location.x - width / 2, location.y - width / 2);
 		gl.glVertex2d(location.x + width / 2, location.y - width / 2);
 		gl.glVertex2d(location.x + width / 2, location.y + width / 2);
 		gl.glVertex2d(location.x - width / 2, location.y + width / 2);
 		gl.glEnd();
 	}
 
 	public void renderMouse() {
 		final Point2d p = game.mouse.location;
 		final double width = 0.04;
 
 		gl.glPushMatrix();
 		gl.glTranslated(p.x, p.y, 0.0);
 		
		Util.log("" + graphicsTime.clock);
		gl.glRotated(Math.sin(graphicsTime.clock) * 360, 0, 0, 1);
 		gl.glBegin(GL.GL_QUADS);
 		gl.glColor3d(1.0, 0.0, 0.0);
 		gl.glVertex2d(width / 2,width / 2);
 		gl.glVertex2d(width / 2, - width / 2);
 		gl.glVertex2d(- width / 2, - width / 2);
 		gl.glColor3d(0.0, 0.0, 1.0);
 		gl.glVertex2d(- width / 2, width / 2);
 		gl.glEnd();
 		gl.glPopMatrix();
 	}
 
 	@Override
 	public void display(GLAutoDrawable drawable) {
 	}
 
 	@Override
 	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {
 		// Not implemented by JOGL.
 	}
 
 	@Override
 	public void init(GLAutoDrawable drawable) {
 		// We have a fresh GL context, retrieve reference.
 		gl = canvas.getGL();
 
 		// ... and initialise.
 		gl.glMatrixMode(GL.GL_PROJECTION);
 		gl.glLoadIdentity();
 		glu.gluOrtho2D(-1.0f, 1.0f, -1.0f, 1.0f); // drawing square
 		gl.glMatrixMode(GL.GL_MODELVIEW);
 		gl.glLoadIdentity();
 
 		// Font info is obtained from the current graphics environment.
 		GraphicsEnvironment ge = GraphicsEnvironment
 				.getLocalGraphicsEnvironment();
 
 		// --- Get an array of fonts. It's preferable to use the names above.
 		Font[] allFonts = ge.getAllFonts();
 
 		for (Font f : allFonts) {
 			Util.log(f.toString());
 		}
 
 		renderer = new TextRenderer(new Font("Deja Vu Sans", Font.PLAIN, 36),
 				true, true);
 	}
 
 	TextRenderer renderer;
 
 	@Override
 	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
 			int height) {
 		// The canvas has been updated.
 		gl.glViewport(0, 0, width, height);
 		size = new Point2d(width, height);
 	}
 }
