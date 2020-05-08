 package org.coolreader.agles;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2ES2;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLCapabilities;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.GLProfile;
 
 import android.opengl.GLES20;
 import android.opengl.GLSurfaceView;
 
 import com.jogamp.newt.event.KeyListener;
 import com.jogamp.newt.event.MouseListener;
 import com.jogamp.newt.opengl.GLWindow;
 import com.jogamp.opengl.util.Animator;
 
 public class Window implements GLEventListener{
 
 	private GLWindow glWindow;
 	
 	public Window(GLSurfaceView.Renderer renderer, KeyListener keyListener, MouseListener mouseListener) {
         this.renderer = renderer;
         GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2ES2));
 	// We may at this point tweak the caps and request a translucent drawable
         caps.setBackgroundOpaque(false);
         glWindow = GLWindow.create(caps);
 
         glWindow.setTitle("GLES20 Demo");
         glWindow.setSize(width,height);
         glWindow.setUndecorated(false);
         glWindow.setPointerVisible(true);
         glWindow.setVisible(true);
 
         glWindow.addGLEventListener(this);
         if (keyListener != null)
         	glWindow.addKeyListener(keyListener);
         if (mouseListener != null)
         	glWindow.addMouseListener(mouseListener);
 	}
 
 	public void run() {
         Animator animator = new Animator();
         animator.add(glWindow);
         animator.start();
 	}
 
 	public static void showWindow(GLSurfaceView.Renderer renderer, int w, int h, KeyListener keyListener, MouseListener mouseListener) {
 		width = w;
 		height = h;
 		Window wnd = new Window(renderer, keyListener, mouseListener);
 		wnd.run();
 	}
 	
     private static int width=800;
     private static int height=600;
 
     private GLSurfaceView.Renderer renderer;
     
     public void init(GLAutoDrawable drawable) {
         GL2ES2 gl = drawable.getGL().getGL2ES2();
 
         System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
         System.err.println("INIT GL IS: " + gl.getClass().getName());
         System.err.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
         System.err.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
         System.err.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));

     }
 
     
     public void reshape(GLAutoDrawable drawable, int x, int y, int z, int h) {
         if (!surfaceCreated) {
         	surfaceCreated = true;
 	        GL2ES2 gl = drawable.getGL().getGL2ES2();
 	        GLES20.setGL2ES2(gl);
 	        renderer.onSurfaceCreated(null, null);
         }
         System.out.println("Window resized to width=" + z + " height=" + h);
         width = z;
         height = h;
         
         renderer.onSurfaceChanged(null, width, height);
     }
 
     boolean surfaceCreated = false; 
     
     public void display(GLAutoDrawable drawable) {
         // Get gl
         if (!surfaceCreated) {
         	surfaceCreated = true;
 	        GL2ES2 gl = drawable.getGL().getGL2ES2();
 	        GLES20.setGL2ES2(gl);
 	        renderer.onSurfaceCreated(null, null);
         }
         
         renderer.onDrawFrame(null);
     }
 
     public void dispose(GLAutoDrawable drawable){
         System.out.println("cleanup, remember to release shaders");
         System.exit(0);
     }
 }
