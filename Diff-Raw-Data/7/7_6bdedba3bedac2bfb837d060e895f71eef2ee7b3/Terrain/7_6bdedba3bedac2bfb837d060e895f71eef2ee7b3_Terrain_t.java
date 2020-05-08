 package gui.includes.Terrain;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.media.opengl.GL2;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.awt.GLJPanel;
 import javax.media.opengl.glu.GLU;
 import javax.media.opengl.glu.GLUquadric;
 
 import com.jogamp.opengl.util.FPSAnimator;
 
 import static javax.media.opengl.GL.*;
 import static javax.media.opengl.GL2.*;
  
 
 @SuppressWarnings("serial")
 public class Terrain extends GLJPanel implements GLEventListener, KeyListener {
  
    private GLU glu;
    
    // Couleurs R,G,B du terrain
    private byte r = (byte)50;
    private byte g = (byte)50;
    private byte b = (byte)50;
    private byte alpha = (byte)155; // Transparence du terrain
    
    // Paramtres de rotation de la camera
    private double rotation_x = 10; // Angle de 10 par dfaut
    private double rotation_y = 0;
    private double r_factor = 1;
    
    // Paramtres pour tourner le terrain en mode automatique (Test uniquement)
    private float angleCube = 0;
    private float speedCube = -1.5f;
    
    public static Terrain terrain = new Terrain();
    public static FPSAnimator animator = new FPSAnimator(terrain, 60, true);
    
    public Terrain() {
 	   this.addGLEventListener(this);
 	   this.addKeyListener(this);
 	   this.setFocusable(true);
    }
  
    
    /**
     * Initialisation OpenGL
     */
    @Override
    public void init(GLAutoDrawable drawable) {
       GL2 gl = drawable.getGL().getGL2();
       glu = new GLU();
       gl.glClearDepth(1.0f);
       gl.glEnable(GL_DEPTH_TEST);
       gl.glEnable(GL_BLEND); // Active le mode BLEND
       gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA) ; // Utilisation du mode BLEND en transparence
       gl.glDepthFunc(GL_LEQUAL);
       gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
       gl.glShadeModel(GL_SMOOTH);
    }
  
 
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
       GL2 gl = drawable.getGL().getGL2();
  
       if (height == 0) height = 1;
       float aspect = (float)width / height;
  
       gl.glViewport(0, 0, width, height);
  
       gl.glMatrixMode(GL_PROJECTION);
       gl.glLoadIdentity();
       glu.gluPerspective(45.0, aspect, 0.1, 100.0);
 
       gl.glMatrixMode(GL_MODELVIEW);
       gl.glLoadIdentity();
    }
  
    /**
     * Mthode appele par l'animateur pour effectuer le rendu 3D.
     */
    @Override
    public void display(GLAutoDrawable drawable) {
       GL2 gl = drawable.getGL().getGL2();
       gl.glClearColor(0.929f, 0.929f, 0.929f, 1.0f);
       gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
       gl.glLoadIdentity();
  
       gl.glTranslated(-3.2, -2.82, -20); // Placement au centre du terrain
       
       // Rotation de la camra
       gl.glRotated(rotation_x,1,0,0);
       gl.glRotated(rotation_y,0,1,0);
       
       
    //   gl.glRotatef(angleCube, 1.0f, 1.0f, 1.0f);
       
       gl.glBegin(GL_QUADS); // Dbut de dessin du terrain
       gl.glColor4ub(r,g,b, alpha);
       
       gl.glVertex3d(0, 0, 0);
       gl.glVertex3d(0, 5.640, 0);
       gl.glVertex3d(6.400, 5.640, 0);
       gl.glVertex3d(6.400, 0, 0);
       
       gl.glVertex3d(0, 0, 0);
       gl.glVertex3d(0, 5.640, 0);
       gl.glVertex3d(0, 5.640, 9.750);
       gl.glVertex3d(0, 0, 9.750);
       
       gl.glVertex3d(6.400, 0, 0);
       gl.glVertex3d(6.400, 5.640, 0);
       gl.glVertex3d(6.400, 5.640, 9.750);
       gl.glVertex3d(6.400, 0, 9.750);
 
       gl.glVertex3d(0, 0, 9.750);
       gl.glVertex3d(0, 5.640, 9.750);
       gl.glVertex3d(6.400, 5.640, 9.750);
       gl.glVertex3d(6.400, 0, 9.750);
 
       gl.glVertex3d(0, 0, 0);
       gl.glVertex3d(0, 0, 9.750);
       gl.glVertex3d(6.400, 0, 9.750);
       gl.glVertex3d(6.400, 0, 0);
 
       gl.glVertex3d(0, 5.640, 0);
       gl.glVertex3d(0, 5.640, 9.750);
       gl.glVertex3d(6.400, 5.640, 9.750);
       gl.glVertex3d(6.400, 5.640, 0);
 
       gl.glEnd(); // Fin de dessin
       
       angleCube += speedCube; // Incrmentation angle de rotation du terrain (utile en test uniquement).
       
       // Contours du terrain
       gl.glLineWidth(2);
       gl.glBegin(GL_LINES);
       gl.glColor4ub((byte)0,(byte)0,(byte)0, (byte)255);
       
       gl.glVertex3d(0, 0, 0);
       gl.glVertex3d(0, 5.640, 0);
       
       gl.glVertex3d(0, 0, 9.750);
       gl.glVertex3d(0, 5.640, 9.750);
       
       gl.glVertex3d(6.400, 0, 0);
       gl.glVertex3d(6.400, 5.640, 0);
       
       gl.glVertex3d(6.400, 0, 9.750);
       gl.glVertex3d(6.400, 5.640, 9.750);
       
       gl.glVertex3d(0, 0, 0);
       gl.glVertex3d(0, 0, 9.750);
       
       gl.glVertex3d(0, 5.640, 0);
       gl.glVertex3d(0, 5.640, 9.750);
       
       gl.glVertex3d(6.400, 0, 0);
       gl.glVertex3d(6.400, 0, 9.750);
       
       gl.glVertex3d(6.400, 5.640, 0);
       gl.glVertex3d(6.400, 5.640, 9.750);
       
       gl.glVertex3d(0, 5.640, 9.750);
       gl.glVertex3d(6.400, 5.640, 9.750);
       
       gl.glVertex3d(0, 0, 9.750);
       gl.glVertex3d(6.400, 0, 9.750);
       
       gl.glVertex3d(0, 5.640, 0);
       gl.glVertex3d(6.400, 5.640, 0);
       
       gl.glVertex3d(0, 0, 0);
       gl.glVertex3d(6.400, 0, 0);
       
       gl.glEnd();
       
       gl.glTranslated(3, 3, 10.750);
       
       // Balle de Squash
       GLUquadric sphere = glu.gluNewQuadric();
       glu.gluSphere(sphere, 0.04, 20, 20);
       glu.gluDeleteQuadric(sphere);
    }
  
    @Override
    public void dispose(GLAutoDrawable drawable) { }
    
    public void keyTyped(KeyEvent key)
    {
 	   // Rien  faire (pour le moment).
    }
    
    public void keyPressed(KeyEvent key)
    {
      if(key.getKeyCode() == KeyEvent.VK_UP) {
     	 rotation_x=rotation_x+r_factor;
      }
      if(key.getKeyCode() == KeyEvent.VK_DOWN) {
     	 rotation_x=rotation_x-r_factor;
      }
      if(key.getKeyCode() == KeyEvent.VK_LEFT) {
     	 rotation_y=rotation_y-r_factor;
      }
      if(key.getKeyCode() == KeyEvent.VK_RIGHT) {
     	 rotation_y=rotation_y+r_factor;
      }
      if(key.getKeyCode() == KeyEvent.VK_ESCAPE) {
          System.exit(0);
        }
    }
 
    public void keyReleased(KeyEvent key)
    {
 	 // Rien  faire (pour le moment) 
    }
 }
