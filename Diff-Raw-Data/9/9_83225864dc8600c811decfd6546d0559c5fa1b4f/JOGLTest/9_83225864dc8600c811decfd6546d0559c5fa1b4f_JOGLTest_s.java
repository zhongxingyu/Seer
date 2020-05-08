 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Frame;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import javax.media.opengl.GL2ES1;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.awt.GLCanvas;
 import javax.media.opengl.fixedfunc.GLLightingFunc;
 import javax.media.opengl.fixedfunc.GLMatrixFunc;
 import javax.media.opengl.glu.GLU;
 import com.jogamp.opengl.util.Animator;
 
 // Imported from local libraries
 
 public class JOGLTest implements GLEventListener, KeyListener {
 	float rotateT = 0.0f;
 	static GLU glu = new GLU();
 	static GLCanvas canvas = new GLCanvas();
 	static Frame frame = new Frame("Jogl Quad drawing");
 	static Animator animator = new Animator(canvas);
 
 	// Color Palette
 	private final Color BLACK = new Color(0, 0, 0);
 	private final Color RED = new Color(255, 0, 0);
 	private final Color GREEN = new Color(0, 255, 0);
 	private final Color BLUE = new Color(0, 0, 255);
 	private final Color YELLOW = new Color(255, 255, 0);
 	private final Color CYAN = new Color(0, 255, 255);
 	private final Color FUCHSIA = new Color(255, 0, 255);
 	private final Color SILVER = new Color(192, 192, 192);
 	private final Color WHITE = new Color(255, 255, 255);
 
 	public JOGLTest() {
 
 	}
 
 	// Going to try a sphere. Check out [http://www.land-of-kain.de/docs/jogl/]
 	// for an awesome example.
 	// More Examples:
 	// http://www.felixgers.de/teaching/jogl/
 	// http://www.falloutsoftware.com/tutorials/gl/gl3.htm
 	// http://www.java-tips.org/other-api-tips/jogl/introduction-to-jogl.html
 	
 
 	public void drawTriangle(GL2 gl2, Point p1, Point p2, Point p3, Color color) {
 		final GL2 gl = gl2.getGL().getGL2();
 		gl.glBegin(GL2.GL_TRIANGLES); // Begin Drawing
 		gl.glColor3f((float) (color.getRed() / 255),
 				(float) (color.getGreen() / 255),
 				(float) (color.getBlue() / 255));
 		gl.glVertex3f((float) p1.getX(), (float) p1.getY(), (float) p1.getZ());
 		gl.glVertex3f((float) p2.getX(), (float) p2.getY(), (float) p2.getZ());
 		gl.glVertex3f((float) p3.getX(), (float) p3.getY(), (float) p3.getZ());
 		gl.glEnd(); // Done Drawing
 	}
 
 	public void drawSquare(GL2 gl2, Point p1, Point p2, Point p3, Point p4,
 			Color color) {
 		final GL2 gl = gl2.getGL().getGL2();
 		gl.glBegin(GL2.GL_QUADS); // Begin Drawing
 		gl.glColor3f((float) (color.getRed() / 255),
 				(float) (color.getGreen() / 255),
 				(float) (color.getBlue() / 255));
 		gl.glVertex3f((float) p1.getX(), (float) p1.getY(), (float) p1.getZ());
 		gl.glVertex3f((float) p2.getX(), (float) p2.getY(), (float) p2.getZ());
 		gl.glVertex3f((float) p3.getX(), (float) p3.getY(), (float) p3.getZ());
 		gl.glVertex3f((float) p4.getX(), (float) p4.getY(), (float) p4.getZ());
 		gl.glEnd(); // Done Drawing
 	}
 
 	public void display(GLAutoDrawable gLDrawable) {
 		final GL2 gl = gLDrawable.getGL().getGL2();
 		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
 		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
 		gl.glLoadIdentity();
 		gl.glTranslatef(0.0f, 0.0f, -5.0f);
 
 		// rotate on the three axis
 
 		gl.glRotatef(rotateT, 0.0f, 1.0f, 0.0f); // y - pitch
 		gl.glRotatef(rotateT, 0.0f, 0.0f, 1.0f); // z - yaw
 		gl.glRotatef(rotateT, 1.0f, 0.0f, 0.0f); // x - roll
 
 		// Awesome! [http://www.falloutsoftware.com/tutorials/gl/gl3.htm] -Ryan
 
 		// Draw a Square Pyramid
		Point top = new Point(0.0, 0.0, 1.5);
 		Point q1 = new Point(1.0, 1.0, 0.0);
 		Point q2 = new Point(-1.0, 1.0, 0.0);
 		Point q3 = new Point(-1.0, -1.0, 0.0);
 		Point q4 = new Point(1.0, -1.0, 0.0);
		drawSquare(gl, q1, q2, q3, q4, WHITE);
		drawTriangle(gl, q1, q2, top, RED);
 		drawTriangle(gl, q2, q3, top, YELLOW);
 		drawTriangle(gl, q3, q4, top, GREEN);
 		drawTriangle(gl, q4, q1, top, BLUE);
 
 		// Draw a Trapazoidal Cube
 		Point b_q1 = new Point(3.0, 3.0, -1.0);
 		Point b_q2 = new Point(3.0, 2.0, -1.0);
 		Point b_q3 = new Point(2.0, 2.0, -1.0);
 		Point b_q4 = new Point(2.0, 3.0, -1.0);
 		Point t_q1 = new Point(2.75, 2.75, 1.0);
 		Point t_q2 = new Point(2.75, 2.25, 1.0);
 		Point t_q3 = new Point(2.25, 2.25, 1.0);
 		Point t_q4 = new Point(2.25, 2.75, 1.0);
 		drawSquare(gl, b_q1, b_q2, b_q3, b_q4, WHITE);
 		drawSquare(gl, t_q1, t_q2, b_q2, b_q1, RED);
 		drawSquare(gl, t_q2, t_q3, b_q3, b_q2, YELLOW);
 		drawSquare(gl, t_q3, t_q4, b_q4, b_q3, GREEN);
 		drawSquare(gl, t_q4, t_q1, b_q1, b_q4, CYAN);
 		drawSquare(gl, t_q1, t_q2, t_q3, t_q4, FUCHSIA);
 
 		/**
 		 * // Draw Triangles gl.glBegin(GL2.GL_TRIANGLES); // Begin Larger
 		 * Triangle gl.glVertex3f(-1.0f, -0.5f, -4.0f); // Lower left vertex
 		 * gl.glColor3f(1.0f, 0.0f, 0.0f); // Red gl.glVertex3f(1.0f, -0.5f,
 		 * -4.0f); // Lower right vertex gl.glColor3f(0.0f, 1.0f, 0.0f); //
 		 * Green gl.glVertex3f(0.0f, 0.5f, -4.0f); // Upper vertex
 		 * gl.glColor3f(1.0f, 0.0f, 1.0f); // Magenta gl.glEnd(); // Done
 		 * Drawing Larger Triangle
 		 * 
 		 * gl.glBegin(GL2.GL_TRIANGLES); // Begin Smaller Triangle
 		 * gl.glVertex3f(-0.5f, -1.5f, -4.0f); // Lower left vertex
 		 * gl.glColor3f(1.0f, 1.0f, 0.0f); // Yellow gl.glVertex3f(0.5f, -1.5f,
 		 * -4.0f); // Lower right vertex gl.glColor3f(0.0f, 1.0f, 1.0f); // Cyan
 		 * gl.glVertex3f(0.0f, -1.0f, -4.0f); // Upper vertex gl.glColor3f(0.0f,
 		 * 0.0f, 1.0f); // Blue gl.glEnd(); // Done Drawing Smaller Triangle
 		 * 
 		 * // Draw A Quad gl.glBegin(GL2.GL_QUADS); gl.glVertex3f(-0.5f, 0.5f,
 		 * 0.0f); // Top Left gl.glColor3f(1.0f, 0.0f, 0.0f); // Red
 		 * gl.glVertex3f(0.5f, 0.5f, 0.0f); // Top Right gl.glColor3f(1.0f,
 		 * 1.0f, 0.0f); // Yellow gl.glVertex3f(0.5f, -0.5f, 0.0f); // Bottom
 		 * Right gl.glColor3f(0.0f, 1.0f, 0.0f); // Green gl.glVertex3f(-0.5f,
 		 * -0.5f, 0.0f); // Bottom Left gl.glColor3f(0.0f, 0.0f, 1.0f); // Blue
 		 * gl.glEnd(); // Done Drawing The Quad
 		 */
 		// increasing rotation for the next iteration
 		rotateT += 0.2f;
 	}
 
 	public void displayChanged(GLAutoDrawable gLDrawable, boolean modeChanged,
 			boolean deviceChanged) {
 	}
 
 	public void init(GLAutoDrawable gLDrawable) {
 		GL2 gl = gLDrawable.getGL().getGL2();
 		gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
 		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
 		gl.glClearDepth(1.0f);
 		gl.glEnable(GL.GL_DEPTH_TEST);
 		gl.glDepthFunc(GL.GL_LEQUAL);
 		gl.glHint(GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
 		((Component) gLDrawable).addKeyListener(this);
 	}
 
 	public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width,
 			int height) {
 		GL2 gl = gLDrawable.getGL().getGL2();
 		if (height <= 0) {
 			height = 1;
 		}
 		float h = (float) width / (float) height;
 		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
 		gl.glLoadIdentity();
 		glu.gluPerspective(100.0f, h, 1.0, 1000.0);
 		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
 		gl.glLoadIdentity();
 	}
 
 	public void keyPressed(KeyEvent e) {
 		// Exit Program
 		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
 			exit();
 		}
 	}
 
 	public void keyReleased(KeyEvent e) {
 	}
 
 	public void keyTyped(KeyEvent e) {
 		// Increase Rotation speed
 		if (e.getKeyCode() == KeyEvent.VK_UP) {
 			rotateT += 1f;
 		}
 		// Decrease Rotation speed
 		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
 			rotateT -= 1f;
 		}
 	}
 
 	public static void exit() {
 		animator.stop();
 		frame.dispose();
 		System.exit(0);
 	}
 
 	public static void main(String[] args) {
 		canvas.addGLEventListener(new JOGLTest());
 		frame.add(canvas);
 		frame.setSize(640, 480);
 		frame.setUndecorated(true);
 		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
 		frame.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				exit();
 			}
 		});
 		frame.setVisible(true);
 		animator.start();
 		canvas.requestFocus();
 	}
 
 	public void dispose(GLAutoDrawable gLDrawable) {
 		// do nothing
 	}
 }
