 package ogo.spec.game.graphics.view;
 
 import com.jogamp.opengl.util.FPSAnimator;
 import com.jogamp.opengl.util.gl2.GLUT;
 import com.jogamp.opengl.util.texture.Texture;
 import com.jogamp.opengl.util.texture.TextureIO;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.awt.GLJPanel;
 import javax.media.opengl.glu.GLU;
 import javax.swing.UIManager;
 import ogo.spec.game.model.Game;
 
 /**
  * Handles all of the graphics functionality.
  */
 abstract public class Base {
 
     // Library version number.
     static public int LIBRARY_VERSION = 'M'; //Maikel's version
     // Minimum distance of camera to center point.
     static public float MIN_CAMERA_DISTANCE = 1f;
     // Distance multiplier per mouse wheel tick.
     static public float MOUSE_WHEEL_FACTOR = 1.2f;
     // Minimum value of theta.
     final static private float EPS = 0.01f;
     static public float THETA_MIN = -(float) Math.PI / 2f + EPS;
     // Maximum value of theta.
     static public float THETA_MAX = (float) Math.PI / 2f - EPS;
     // Ratio of distance in pixels dragged and radial change of camera.
     static public float DRAG_PIXEL_TO_RADIAN = 0.025f;
     // Minimum value of vWidth.
     static public float VWIDTH_MIN = 1f;
     // Maximum value of vWidth.
     static public float VWIDTH_MAX = 1000f;
     // Ratio of vertical distance dragged and change of vWidth;
     static public float DRAG_PIXEL_TO_VWIDTH = 0.1f;
     // Extent of center point change based on key input.
     static public float CENTER_POINT_CHANGE = 1f;
     // How many control options are enabled.
    static public boolean DEBUG = false;
     // Desired frames per second.
     static public int FPS = 30;
     // Global state, created at startup.
     protected GlobalState gs;
     // OpenGL reference, continuously updated for correct thread.
     protected GL2 gl;
     // OpenGL utility functions.
     protected GLU glu;
     protected GLUT glut;
     // Start time of animation.
     private long startTime;
     // Textures.
     protected Texture land, shallowWater, deepWater, empty, red, warning;
     MainFrame frame;
 
     /**
      * Constructs base class.
      */
     public Base() {
         // Global state.
         this.gs = new GlobalState();
 
         // Enable fancy GUI theme.
         try {
             UIManager.setLookAndFeel(
                     "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
         } catch (Exception ex) {
             Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         // GUI frame.
         frame = new MainFrame(gs);
 
         // OpenGL utility functions.
         this.glu = new GLU();
         this.glut = new GLUT();
 
         // Redirect OpenGL listener to the abstract render functions.
         GLJPanel glPanel = (GLJPanel) frame.glPanel;
         glPanel.addGLEventListener(new GLEventDelegate());
 
         // Attach mouse and keyboard listeners.
         GLListener listener = new GLListener();
         glPanel.addMouseListener(listener);
         glPanel.addMouseMotionListener(listener);
         glPanel.addMouseWheelListener(listener);
         glPanel.addKeyListener(listener);
         glPanel.setFocusable(true);
         glPanel.requestFocusInWindow();
 
         // Attach animator to OpenGL panel and begin refresh
         // at the specified number of frames per second.
         final FPSAnimator animator =
                 new FPSAnimator((GLJPanel) frame.glPanel, FPS, true);
         animator.setIgnoreExceptions(false);
         animator.setPrintExceptions(true);
 
         animator.start();
 
         // Stop animator when window is closed.
         frame.addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                 animator.stop();
             }
         });
 
         // Show frame.
         frame.setVisible(true);
     }
     
     /**
      * Called upon the start of the application. Primarily used to configure
      * OpenGL.
      */
     abstract public void initialize();
 
     /**
      * Configures the viewing transform.
      */
     abstract public void setView();
 
     /**
      * Draws the entire scene.
      */
     abstract public void drawScene();
 
     /**
      * Pass a vector as a vertex to OpenGL.
      */
     public void glVertex(Vector vector) {
         gl.glVertex3d(vector.x(),
                 vector.y(),
                 vector.z());
     }
 
     /**
      * Delegates OpenGL events to abstract methods.
      */
     public final class GLEventDelegate implements GLEventListener {
 
         /**
          * Initialization of OpenGL state.
          */
         @Override
         public void init(GLAutoDrawable drawable) {
             gl = drawable.getGL().getGL2();
 
             // Try to load textures.
             String path = "ogo/spec/game/graphics/textures/";
             land = loadTexture(path + "land.jpg");
             shallowWater = loadTexture(path + "shallow.jpg");
             deepWater = loadTexture(path + "deep.jpg");
             empty = loadTexture(path + "empty.jpg");
             red = loadTexture(path + "red.jpg");
             warning = loadTexture(path + "warning.jpg");
 
             // Print library version number.
             System.out.println("Using library version " + LIBRARY_VERSION);
 
             initialize();
         }
 
         /**
          * Try to load a texture from the given file.
          */
         private Texture loadTexture(String file) {
             Texture result = null;
 
             try {
                 // Try to load from local folder.
                 result = TextureIO.newTexture(new File(file), false);
             } catch (Exception e1) {
                 // Try to load from /src folder instead.
                 try {
                     result = TextureIO.newTexture(new File("src/" + file), false);
                 } catch (Exception e2) {
                 }
             }
 
             if (result != null) {
                 System.out.println("Loaded " + file);
                 result.enable(gl);
             }
 
             return result;
         }
 
         /**
          * Render scene.
          */
         @Override
         public void display(GLAutoDrawable drawable) {
             gl = drawable.getGL().getGL2();
 
             // Also update view, because global state may have changed.
             setView();
             drawScene();
 
             // Report OpenGL errors.
             int errorCode = gl.glGetError();
             while (errorCode != GL.GL_NO_ERROR) {
                 System.err.println(errorCode + " "
                         + glu.gluErrorString(errorCode));
                 errorCode = gl.glGetError();
             }
         }
 
         /**
          * Canvas reshape.
          */
         @Override
         public void reshape(GLAutoDrawable drawable,
                 int x, int y,
                 int width, int height) {
             gl = drawable.getGL().getGL2();
 
             // Update state.
             gs.w = width;
             gs.h = height;
 
             setView();
         }
 
         @Override
         public void dispose(GLAutoDrawable drawable) {
         }
     }
 
     /**
      * Handles mouse events of the GLJPanel to support the interactive change of
      * camera angles and distance in the global state.
      */
     public final class GLListener implements MouseMotionListener,
             MouseListener,
             MouseWheelListener,
             KeyListener {
         // Position of mouse drag source.
 
         private int dragSourceX, dragSourceY;
         // Last mouse button pressed.
         private int mouseButton;
 
         @Override
         public void mouseDragged(MouseEvent e) {
             float dX = e.getX() - dragSourceX;
             float dY = e.getY() - dragSourceY;
 
             // Change camera angle when left button is pressed.
             if (mouseButton == MouseEvent.BUTTON1) {
                 gs.phi += dX * DRAG_PIXEL_TO_RADIAN;
                 gs.theta = Math.max(THETA_MIN,
                         Math.min(THETA_MAX,
                         gs.theta + dY * DRAG_PIXEL_TO_RADIAN));
             } // Change vWidth when right button is pressed.
            else if (mouseButton == MouseEvent.BUTTON3 && DEBUG) {
                 gs.vWidth = Math.max(VWIDTH_MIN,
                         Math.min(VWIDTH_MAX,
                         gs.vWidth + dY * DRAG_PIXEL_TO_VWIDTH));
             }
 
             dragSourceX = e.getX();
             dragSourceY = e.getY();
         }
 
         @Override
         public void mouseMoved(MouseEvent e) {
         }
 
         @Override
         public void mouseWheelMoved(MouseWheelEvent e) {
             gs.vDist = (float) Math.max(MIN_CAMERA_DISTANCE,
                     gs.vDist
                     * Math.pow(MOUSE_WHEEL_FACTOR,
                     e.getWheelRotation()));
         }
 
         @Override
         public void mouseClicked(MouseEvent e) {
         }
 
         @Override
         public void mousePressed(MouseEvent e) {
             dragSourceX = e.getX();
             dragSourceY = e.getY();
             mouseButton = e.getButton();
         }
 
         @Override
         public void mouseReleased(MouseEvent e) {
         }
 
         @Override
         public void mouseEntered(MouseEvent e) {
         }
 
         @Override
         public void mouseExited(MouseEvent e) {
         }
 
         @Override
         public void keyTyped(KeyEvent e) {
         }
 
         @Override
         public void keyPressed(KeyEvent e) {
             // Move center point.
             double phiQ = gs.phi + Math.PI / 2.0;
 
             switch (e.getKeyChar()) {
                 // Right.
                 case 'a':
                     gs.cnt = gs.cnt.subtract(
                             new Vector(Math.cos(phiQ), Math.sin(phiQ), 0)
                             .scale(CENTER_POINT_CHANGE));
                     break;
                 // Left.
                 case 'd':
                     gs.cnt = gs.cnt.add(
                             new Vector(Math.cos(phiQ), Math.sin(phiQ), 0)
                             .scale(CENTER_POINT_CHANGE));
                     break;
                 // Forwards.
                 case 'w':
                     gs.cnt = gs.cnt.subtract(
                             new Vector(Math.cos(gs.phi), Math.sin(gs.phi), 0)
                             .scale(CENTER_POINT_CHANGE));
                     break;
                 // Backwards.
                 case 's':
                     gs.cnt = gs.cnt.add(
                             new Vector(Math.cos(gs.phi), Math.sin(gs.phi), 0)
                             .scale(CENTER_POINT_CHANGE));
                     break;
                 // Up.
                 case 'q':
                     gs.cnt = new Vector(gs.cnt.x,
                             gs.cnt.y,
                             gs.cnt.z + CENTER_POINT_CHANGE);
                     break;
                 // Down.
                 case 'z':
                     gs.cnt = new Vector(gs.cnt.x,
                             gs.cnt.y,
                             gs.cnt.z - CENTER_POINT_CHANGE);
                     break;
                 // Debug.
                 case 'D':
                     debug = !debug;
                     break;
             }
         }
 
         @Override
         public void keyReleased(KeyEvent e) {
         }
     }
 }
