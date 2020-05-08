 import com.sun.opengl.util.Animator;
 import com.sun.opengl.util.GLUT;
 import java.awt.Frame;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import javax.media.opengl.GL;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLCanvas;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.glu.GLU;
 
 import java.util.*;
 import java.io.*;
 
 /**
  * author: Brian Paul (converted to Java by Ron Cemer and Sven Goethel) <P>
  *
  * This version is equal to Brian Paul's version 1.2 1999/10/21
  */
 public class Main
 extends Fly
 implements GLEventListener {
     private float frustumBegin =    1f;
     private float frustumEnd   = 1000f;
 
     private float angle = 0;
     private float color = 0;
 
     private float d_angle = 0.5f;
 
     private float pointSize = 3f;
     private float lineWidth = 1f;
 
     private float[] ambientLighting  = {0.50f, 0.50f, 0.50f, 1f};
     private float[] diffuseLighting  = {0.50f, 0.50f, 0.50f, 1f};
     private float[] specularLighting = {1.00f, 1.00f, 1.00f, 1f};
 
     private float[] lightPos = {0.00f, 50.00f, 0.00f, 0f};
 
     private float[] bgColor = {0.20f, 0.20f, 0.20f, 1.00f};
 
     private Cache theCache;
 
     public static void main(String[] args) {
         Frame frame = new Frame("Function Plotter by Brian Mock");
         GLCanvas canvas = new GLCanvas();
 
         Main inst = new Main();
         canvas.addGLEventListener(inst);
         canvas.addKeyListener(inst);
         frame.add(canvas);
         frame.setSize(640, 480);
         final Animator animator = new Animator(canvas);
         frame.addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                 // Run this on another thread than the AWT event queue to
                 // make sure the call to Animator.stop() completes before
                 // exiting
                 new Thread(new Runnable() {
                     public void run() {
                         animator.stop();
                         System.exit(0);
                     }
                 }).start();
             }
         });
         // Center frame
         frame.setLocationRelativeTo(null);
         frame.setVisible(true);
         animator.start();
         canvas.requestFocus();
     }
 
     public void init(GLAutoDrawable drawable) {
         // Use debug pipeline
         // drawable.setGL(new DebugGL(drawable.getGL()));
 
         GL gl = drawable.getGL();
         //System.err.println("INIT GL IS: " + gl.getClass().getName());
 
         // Enable VSync
         gl.setSwapInterval(1);
 
         // Enable depth testing
         gl.glEnable(GL.GL_DEPTH_TEST);
 
         // Setup the drawing area and shading mode
         gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
 
         // try setting this to GL_FLAT and see what happens.
         gl.glShadeModel(GL.GL_SMOOTH);
         //gl.glShadeModel(GL.GL_FLAT);
 
         //System.out.println(elevations);
 
         // Set up larger, rounded points
         gl.glPointSize(pointSize);
         gl.glEnable(gl.GL_POINT_SMOOTH);
 
         // Set up thicker lines, smoother lines
         gl.glLineWidth(lineWidth);
         //gl.glEnable(gl.GL_LINE_SMOOTH);
 
         // Enable lighting
         gl.glEnable(gl.GL_LIGHTING);
         gl.glEnable(gl.GL_COLOR_MATERIAL);
         gl.glEnable(gl.GL_LIGHT0);
 
         // Enable vertex arrays
         gl.glEnable(gl.GL_VERTEX_ARRAY);
 
         // Normalize surface normals
         gl.glEnable(gl.GL_NORMALIZE);
 
         // Enable alpha blending
         //gl.glEnable(gl.GL_BLEND);
         //gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);
 
         initKeyBinds();
         changeLighting(gl);
         calcFSU();
         makeCache();
     }
 
     public void makeCache() {
         theCache = new Cache(new WavierFunc());
         //theCache = new Cache(new WaveFunc());
         //theCache = new Cache(new ConeFunc());
     }
 
     public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
         GL gl = drawable.getGL();
         GLU glu = new GLU();
 
         // avoid a divide by zero error!
         if (height <= 0) {
             height = 1;
         }
 
         final float h = (float) width / (float) height;
         gl.glViewport(0, 0, width, height);
         gl.glMatrixMode(GL.GL_PROJECTION);
         gl.glLoadIdentity();
         glu.gluPerspective(45.0f, h, frustumBegin, frustumEnd);
         gl.glMatrixMode(GL.GL_MODELVIEW);
         gl.glLoadIdentity();
     }
 
     public void display(GLAutoDrawable drawable) {
         GL  gl  = drawable.getGL();
         GLU glu = new GLU();
 
         // Clear the drawing area
         gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
 
         gl.glLoadIdentity();
 
         dispatchKeyActions();
         myLookAt(gl);
 
         gl.glClearColor(bgColor[0], bgColor[1], bgColor[2], bgColor[3]);
 
         angle += d_angle;
         angle %= 360;
 
         Debug.println("Entering theCache.draw(gl)");
         theCache.draw(gl);
         Debug.println("Exiting theCache.draw(gl)");
         //theCache.drawImmediate(gl);
 
         gl.glFlush();
     }
 
     private void changeLighting(GL gl) {
         gl.glLightfv(gl.GL_LIGHT0, gl.GL_POSITION, lightPos,         0);
         gl.glLightfv(gl.GL_LIGHT0, gl.GL_AMBIENT,  ambientLighting,  0);
         gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE,  diffuseLighting,  0);
         gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPECULAR, specularLighting, 0);
     }
 
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
     }
 }
 
