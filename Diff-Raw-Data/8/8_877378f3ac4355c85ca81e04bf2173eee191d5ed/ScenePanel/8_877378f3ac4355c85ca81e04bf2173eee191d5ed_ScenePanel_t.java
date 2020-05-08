 package game;
 import input.InputHandler;
 
 import javax.media.opengl.*;
 import javax.media.opengl.awt.GLCanvas;
 
 import actor.Actor;
 
 import com.jogamp.opengl.util.*;
 import com.jogamp.opengl.util.gl2.GLUT;
 
 import java.awt.Dimension;
 import javax.media.opengl.glu.GLU;
 import javax.media.opengl.fixedfunc.GLMatrixFunc;
 
 /*
  * Author: Chris Lundquist
  * Description: This class handles the interface to OpenGL and delegates KeyListener to Asteroids.
  *              it loops through the actor array rendering each actor with its respective location, rotation, and texture.
  */
 public class ScenePanel extends GLCanvas {
     private static final long serialVersionUID = 702382815287044105L;
     private static final int VIEWPORT_WIDTH = 500;
     private static final int VIEWPORT_HEIGHT = 500;
     private FPSAnimator animator;
     private GLU glu;
     private InputHandler input;
 
     public ScenePanel() {
         input = new InputHandler();
 
         setPreferredSize(new Dimension(VIEWPORT_WIDTH, VIEWPORT_HEIGHT));
 
         addGLEventListener(new GLEventHandler(this));
         // Register our keyboard listener
         addKeyListener(input);
 
         glu = new GLU();
         animator = new FPSAnimator(this, 60); // 60 fps
     }
 
     /**
      * This private inner class implements the OpenGL calls backs.
      */
     private class GLEventHandler implements GLEventListener {
         private GLCanvas canvas;
 
         public GLEventHandler(GLCanvas canvas) {
             this.canvas = canvas;
         }
 
         @Override
         public void init(GLAutoDrawable drawable) {
             /*  It is where we should initialize various OpenGL features. */
             GL2 gl = drawable.getGL().getGL2();
 
             /* Set background color to black */
             gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
             gl.glClearDepthf(1.0f);
             // Enable Textures
             gl.glEnable(GL.GL_TEXTURE_2D);
             // Enable alpha blending
             gl.glEnable (GL.GL_BLEND);
             gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
 
             // Create sprite and load the texture files
 
             Game.init();
             // We have to have focus for our KeyListener to get messages
             requestFocus();
 
             animator.start();
         }
 
         @Override
         public void display(GLAutoDrawable drawable) {
             /* Only update if the canvas is in focus */
             if (canvas.hasFocus() == false)
                 return;
 
             input.update();
 
             Game.update();
 
             render(drawable);
         }
 
         @Override
         public void dispose(GLAutoDrawable drawable) {
             /*
 			 Exception in thread "AWT-EventQueue-0" java.lang.NullPointerException
 				at com.jogamp.opengl.util.FPSAnimator.stop(FPSAnimator.java:140)
 				at ScenePanel$GLEventHandler.dispose(ScenePanel.java:68)
              * animator.stop();
              */
             Game.dispose();
         }
 
         @Override
         public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
             GL2 gl = drawable.getGL().getGL2();
             // Make the height at least one so we don't divide by zero
             if (height <= 0) {
                 height = 1;
             }
             float aspectRatio = (float) width / (float) height;
             // Reset the projection matrix to the identity
             gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
             gl.glLoadIdentity();
             // Tell the perspective matrix the Field of view, aspectRatio, ZNEAR, ZFAR
             glu.gluPerspective(90.0f, aspectRatio, 1.0, 1000.0);
             // Go back to ModelView matrix mode now that we are done
             gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
             // Be nice and load the Identity
             gl.glLoadIdentity();
         }
     }
 
     /*
      * Description: This is the main render loop where we draw each actor onto the frame buffer.
      */
     private void render(GLAutoDrawable drawable) {
         /* Fetch the OpenGL context */
         GL2 gl = drawable.getGL().getGL2();
 
         // Clear the buffer in case we don't draw in every position
         // we won't have ghosting
         gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
 
         // Render background
         // FIXME gl.glClear(GL.GL_COLOR_BUFFER_BIT) should reset the color but doesn't seem to
         gl.glLoadIdentity();
         drawSkybox(gl);
 
         // The Polygon for our background image to map a texture to
         //drawNormalSquare(gl);
 
 
         /* Loop through all our actors in reverse order rendering them
          * so the PlayerShip gets drawn last.
          */
         for (int i = Actor.actors.size() - 1; i >= 0; i--) {
             // Get the ith Actor
             Actor actor = Actor.actors.get(i);
 
             // Clear the Current matrix from the GL Stack to Identity so we aren't
             // working with left over data from someone else
             gl.glLoadIdentity();
 
             // Bind our texture for this actor. This tells OpenGL which texture we want to use
             // for the next glBegin()
             //gl.glBindTexture(GL.GL_TEXTURE_2D, actor.getSprite().getTextureId());
             gl.glColor4f(1.0f, 1.0f, 1.0f,1.0f);
 
             // Transformations are on a Stack so its "First In Last Out"
             // Meaning we Translate, Rotate, Scale.
             // OpenGL uses a +Y-axis up and +Z-axis out coordinate system.
 
             // Translate our object to its position
             //gl.glTranslatef(actor.getPosition().x(), actor.getPosition().y, -1.0f);
             // Rotate it by its rotation about the Z axis
             /* NOTE OpenGL expects rotations to be in degrees */
             //gl.glRotatef(actor.getThetaDegrees(),0,0,1);
             // Scale our image by its size in the X and Y dimension
             gl.glScalef(actor.getSize(), actor.getSize(), 1);
             // Draw a Normal Square at the origin at will be transformed as
             // described above in glScale,glRotate,glTranslate
             // For our actor to map a texture to
             drawNormalSquare(gl);  
         }
 
         renderParticles(gl);
         // FIXME ugly hack. Shields aren't actors but belong to actors. If Shields were actors,
         // then you have to deal with lots of collision crap and update their position to their
         // owners. Its really half a dozen or the other since the render code is centralized.
         // -CL
         renderShields(gl);
         renderMessages(gl);
         /*
          * Display message if game is paused
          */
         if (Game.isPaused())
             renderText(gl, "Paused", -0.25f, 0.25f);
     }
 
     private void renderText(GL2 gl, String string, float x, float y) {
         gl.glDisable(GL.GL_TEXTURE_2D);
         GLUT glut = new GLUT();
 
         gl.glLoadIdentity();
         gl.glTranslatef(0, 0, -1.5f);
         gl.glColor3f((float)GUI.titleColor().getRed() / 256,
                 (float)GUI.titleColor().getGreen() / 256,
                 (float)GUI.titleColor().getBlue() / 256);
         gl.glRasterPos2f(x, y);
 
         glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, string);
         gl.glEnable(GL.GL_TEXTURE_2D);
     }
 
     private void renderShields(GL2 gl){
        //actor.Player player = Game.getPlayer();
 
         gl.glLoadIdentity();
         // Texture of the player's shield
         // Someone left the lights off?
         /*
         gl.glBindTexture(GL.GL_TEXTURE_2D, player.shield.getSprite().getTextureId());
         gl.glColor4f(1.0f, 1.0f, 1.0f,player.shield.getIntegrity());
         // At the Player's position
         gl.glTranslatef(player.getPosition().x(), player.getPosition().y(), -1.0f);
         gl.glRotatef(player.getThetaDegrees(), 0, 0, 1);
         // The Shield's Size
         gl.glScalef(player.shield.getSize(), player.shield.getSize(), 1);
         // Fade it with the shield's strength
          
          */
         drawNormalSquare(gl); 
     }
 
     private void renderParticles(GL2 gl) {
         /*
         if (ParticleSystem.enabled == false)
            return;
 
         gl.glDisable(GL.GL_TEXTURE_2D);
         for(int i = 0; i < ParticleSystem.particles.size(); i++) {
             Particle p = ParticleSystem.particles.get(i);
             gl.glLoadIdentity();
             gl.glTranslatef(p.getPosition().x(), p.getPosition().y(), -1.0f);
             gl.glRotatef(p.getThetaDegrees(),0,0,1);
             gl.glScalef(p.getSize(), p.getSize(), 1);
             gl.glColor4f(p.colorR, p.colorG, p.colorB,p.colorA);
             //System.err.println(p.colorR +" " + p.colorG+ " " +p.colorB);
             gl.glBegin(7 /* GL.GL_QUADS*/ /*);
             gl.glVertex2d(-0.5f, -0.5f);
             // Bottom Right
             //gl.glTexCoord2f(1, 1);
             gl.glVertex2d(0.5f, -0.5f);
             // Top Right
             //gl.glTexCoord2f(1, 0);
             gl.glVertex2d(0.5f, 0.5f);
             // Top Left
             //gl.glTexCoord2f(0, 0);
             gl.glVertex2d(-0.5f, 0.5f);
             gl.glEnd();
         }
         gl.glEnable(GL.GL_TEXTURE_2D);
         */
     }
 
     private void renderMessages(GL2 gl) {
         /*
         GLUT glut = new GLUT();
 
         gl.glDisable(GL.GL_TEXTURE_2D);
         for (OnscreenMessage msg: OnscreenMessage.getMessages()) {
             gl.glLoadIdentity();
             gl.glColor4f((float)GUI.titleColor().getRed() / 256,
                     (float)GUI.titleColor().getGreen() / 256,
                     (float)GUI.titleColor().getBlue() / 256,
                     msg.getAlpha());
             gl.glTranslatef(msg.getPosition().x(), msg.getPosition().y(), -1.0f);
 
             // center text
             float width = 2 * glut.glutBitmapLength(GLUT.BITMAP_HELVETICA_18, msg.getText()) / (float)VIEWPORT_WIDTH;
             gl.glRasterPos2f(width / -2, 0.0f);
 
             glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, msg.getText());
 
         }
 
         gl.glEnable(GL.GL_TEXTURE_2D);
         */
     }
 
     private static float rot = 0.0f;
     private void drawSkybox(GL2 gl){
         // TODO OPTIMIZE we don't want to load the texture each frame, this was
         // just a basic proof test.
         //gl.glBindTexture(GL.GL_TEXTURE_2D, new graphics.Texture(gl,new java.io.File("/Users/Durandal/code/java/tcc/project/data/background.jpg")).getGlTexture());
         gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
         gl.glTranslatef(0, 0, -6);
         gl.glRotatef(1.0f, 1.0f, 0.0f, 40.0f);
         gl.glScalef(4, 4, 1);
 
         rot += 10.0f;
         graphics.Model.renderCube(gl);
     }
     /**
      * Draw a normalized square at the origin
      * @param gl - the OpenGL context
      */
     private void drawNormalSquare(GL2 gl) {
         // These points will be multiplied by the transformations above
         // to produce the desired and described transformations.
         gl.glBegin(/* GL.GL_QUADS */ 7);//GL_QUADS isn't defined in the JOGL
         // Points must be counter clockwise defined or they will
         // be removed by back face culling
 
         // Bottom Left
         gl.glTexCoord2f(0, 0);
         gl.glVertex2d(-0.5f, -0.5f);
         // Bottom Right
         gl.glTexCoord2f(1, 0);
         gl.glVertex2d(0.5f, -0.5f);
         // Top Right
         gl.glTexCoord2f(1, 1);
         gl.glVertex2d(0.5f, 0.5f);
         // Top Left
         gl.glTexCoord2f(0, 1);
         gl.glVertex2d(-0.5f, 0.5f);
         gl.glEnd();
     }
 }
