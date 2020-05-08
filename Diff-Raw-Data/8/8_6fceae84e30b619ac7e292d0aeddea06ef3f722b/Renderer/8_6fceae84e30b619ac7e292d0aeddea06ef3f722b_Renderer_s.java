 package graphics;
 
 import game.Game;
 
 import java.awt.Component;
 import java.awt.Frame;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.List;
 import java.io.IOException;
 
 import javax.media.opengl.GL2;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.awt.GLCanvas;
 import javax.media.opengl.fixedfunc.GLMatrixFunc;
 import javax.media.opengl.glu.GLU;
 
 import actor.Actor;
 
 import com.jogamp.opengl.util.FPSAnimator;
 
 /* @author Chris Lundquist
  *  Based on the work by  Julien Gouesse (http://tuer.sourceforge.net)
  */
 public class Renderer implements GLEventListener {
     GLU glu;
     GLCanvas canvas;
     Frame frame;
     //Animator animator;
 
     FPSAnimator animator;
     Shader shader;
 
     public Renderer(){
         glu = new GLU();
         canvas = new GLCanvas();
         frame = new Frame("cs143 project");
         animator = new FPSAnimator(canvas,60);
         shader = new Shader("texture.vert","texture.frag");
     }
 
     // Display is our main game loop since the animator calls it
     public void display(GLAutoDrawable glDrawable) {
         // CL - We need to get input even if the game is paused,
         //      that way we can unpause the game.
         game.Game.getInputHandler().update();
         // Don't update the game state if we are paused
         if(game.Game.isPaused())
             return;
         
 
         game.Game.getPlayer().updateCamera();
 
         GL2 gl = getGL2();
         // Update the actors
         actor.Actor.updateActors();
 
 
 
         gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
         gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
 
         gl.glLoadIdentity();
         // update the camera position here so it doesn't fire on the dedicated server
         // Push the transformation for our player's Camera
<<<<<<< HEAD
        //game.Game.getPlayer().getCamera().setPerspective(gl);
        
        Game.getPlayer().getCamera().setPerspective(gl);
=======
         Game.getPlayer().updateCamera().setPerspective(gl);
>>>>>>> c4b8c8eb256cc2b9c70c572391e2a716e6c22fc8
         Game.getMap().getSkybox().render(gl);
         
         
         
         //drawHud(gl);
 
         // Render each actor       
         List<Actor> actors = Actor.getActors();
         synchronized(actors) {
             for(Actor a: actors)
                 a.render(gl);
         }
         
         checkForGLErrors(gl);
 
         
     }
     /**
      * Draws hud, just a red square for now, still getting familiar with the code and testing
      * @param gl
      */
     public void drawHud(GL2 gl) {
         // Temporary disable lighting
         gl.glDisable(GL2.GL_LIGHTING);
         gl.glDisable(GL2.GL_TEXTURE_2D);
 
         // Our HUD consists of a simple rectangle
         gl.glMatrixMode(GL2.GL_PROJECTION );
         gl.glPushMatrix(); /*  save projection matrix */
         gl.glLoadIdentity();
         gl.glOrtho( -100.0f, 100.0f, -100.0f, 100.0f, -100.0f, 100.0f );
 
         gl.glMatrixMode(GL2.GL_MODELVIEW );
         gl.glPushMatrix(); /* save our model matrix */
         gl.glLoadIdentity();
         
         gl.glColor3f( 1.0f, 0.0f, 0.0f );
         gl.glBegin(GL2.GL_QUADS );
         gl.glVertex2f( -90.0f, 90.0f );
         gl.glVertex2f( -90.0f, 40.0f );
         gl.glVertex2f( -40.0f, 40.0f );
         gl.glVertex2f( -40.0f, 90.0f );
         gl.glEnd();
         
         gl.glPopMatrix(); /* recover model matrix*/
         gl.glMatrixMode(GL2.GL_PROJECTION );
         
         gl.glPopMatrix(); /* recover projection matrix */
         gl.glMatrixMode(GL2.GL_MODELVIEW );
 
         gl.glEnable(GL2.GL_TEXTURE_2D);
         gl.glEnable(GL2.GL_LIGHTING );
     }
 
 
     private static void checkForGLErrors(GL2 gl) {
         int errno = gl.glGetError();
         switch (errno) {
             case GL2.GL_INVALID_ENUM:
                 System.err.println("OpenGL Error: Invalid ENUM");
                 break;
             case GL2.GL_INVALID_VALUE:
                 System.err.println("OpenGL Error: Invalid Value");
                 break;
             case GL2.GL_INVALID_OPERATION:
                 System.err.println("OpenGL Error: Invalid Operation");
                 break;
             case GL2.GL_STACK_OVERFLOW:
                 System.err.println("OpenGL Error: Stack Overflow");
                 break;
             case GL2.GL_STACK_UNDERFLOW:
                 System.err.println("OpenGL Error: Stack Underflow");
                 break;
             case GL2.GL_OUT_OF_MEMORY:
                 System.err.println("OpenGL Error: Out of Memory");
                 break;
             default:
                 return;
         }
     }
 
     private void setLighting(GL2 gl) {
         float light_ambient[] = { 0.5f, 0.5f, 0.5f, 1.0f };
         float light_diffuse[] = { 1.0f, 1.0f, 1.0f, 1.0f };
         float light_specular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
 
         float[] light0 = {-1.0f,-2.0f,2.0f,0.0f};
         float[] light1 = {1.0f,2.0f,-2.0f,0.0f};
         gl.glEnable(GL2.GL_LIGHTING);
         gl.glEnable(GL2.GL_LIGHT0);
         gl.glEnable(GL2.GL_LIGHT1);
         gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, light_ambient, 0);
         gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, light_diffuse, 0);
         gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, light_specular, 0);
         gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, light0, 0);
 
         gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, light_ambient, 0);
         gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, light_diffuse, 0);
         gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, light_specular, 0);
         gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, light1, 0);
         gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, light_ambient, 0);
     }
 
     public void displayChanged(GLAutoDrawable gLDrawable, boolean modeChanged, boolean deviceChanged) {
     }
 
     public void init(GLAutoDrawable gLDrawable) {
         GL2 gl = getGL2();
         gl.glShadeModel(GL2.GL_SMOOTH);
         gl.setSwapInterval(1); // Enable V-Sync supposedly
         gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
         gl.glClearDepth(1.0f);
         gl.glEnable(GL2.GL_CULL_FACE); // TODO, change our skybox to be textured from the inside out
         gl.glEnable(GL2.GL_DEPTH_TEST);
         gl.glEnable(GL2.GL_TEXTURE_2D);
         gl.glDepthFunc(GL2.GL_LEQUAL);
         gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
         ((Component) gLDrawable).addKeyListener(game.Game.getInputHandler());
         setLighting(gl);
         Model.initialize(gl); /* calls Texture.initialize */
         try {
             shader.init(gl);
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         shader.enable(gl);
         System.gc(); // This is probably a good a idea
     }
 
 
     public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width, int height) {
         GL2 gl = getGL2();
         if (height <= 0) {
             height = 1;
         }
         float h = (float) width / (float) height;
         gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
         gl.glLoadIdentity();
         glu.gluPerspective(50.0f, h, 1.0, 1000.0);
         gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
         gl.glLoadIdentity();
     }
 
     public void dispose(GLAutoDrawable gLDrawable) {
         animator.stop();
         frame.dispose();
         canvas.destroy();
     }
 
     public void start() {
         canvas.addGLEventListener(this);
         frame.add(canvas);
         frame.setSize(640, 480);
         frame.setUndecorated(true);
         frame.setExtendedState(Frame.MAXIMIZED_BOTH);
         frame.addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 Game.exit();
             }
         });
         frame.setVisible(true);
         animator.start();
         canvas.requestFocus();
     }
 
     // CL - Private method that handles the exception code that would otherwise
     // be copy pasted. It seems that if other people use this method getGL() 
     // usually returns null and crashes the program
     private GL2 getGL2(){
         GL2 gl = null;
         try {
             gl = canvas.getGL().getGL2();
         } catch(Exception e) {
             System.err.println("Error getting OpenGL Context:\n" + e.toString());
             System.err.println("--------");
             e.printStackTrace();
             System.exit(-1);
         }
         return gl;
     }
 }
