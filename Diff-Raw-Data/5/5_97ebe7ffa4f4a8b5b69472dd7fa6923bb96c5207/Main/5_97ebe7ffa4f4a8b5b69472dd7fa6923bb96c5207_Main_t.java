 package psywerx.platformGl.game;
 
 import static javax.media.opengl.GL.GL_NEAREST;
 import static javax.media.opengl.GL.GL_RGB;
 import static javax.media.opengl.GL.GL_TEXTURE_2D;
 import static javax.media.opengl.GL.GL_TEXTURE_MAG_FILTER;
 import static javax.media.opengl.GL.GL_TEXTURE_MIN_FILTER;
 import static javax.media.opengl.GL.GL_UNPACK_ALIGNMENT;
 import static javax.media.opengl.GL.GL_UNSIGNED_BYTE;
 
 import java.nio.ByteBuffer;
 
 import javax.media.opengl.GL2ES2;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLCapabilities;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.GLProfile;
 
 import com.jogamp.newt.event.KeyEvent;
 import com.jogamp.newt.event.KeyListener;
 import com.jogamp.newt.opengl.GLWindow;
 import com.jogamp.opengl.util.Animator;
 
 public class Main implements GLEventListener {
 
     protected static int WIDTH = 600;
     protected static int HEIGHT = 800;
 
     protected static final Game game = new Game();
 
     private double t0 = System.currentTimeMillis();
     private double theta;
 
     protected static int shaderProgram;
     protected static int projectionMatrix_location;
     protected static int modelMatrix_location;
     protected static int sampler_location;
 
     private int vertShader;
     private int fragShader;
     protected static int texture1;
 
     public static void main(String[] args) {
 
         GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2ES2));
 
         caps.setBackgroundOpaque(false);
         GLWindow glWindow = GLWindow.create(caps);
 
         glWindow.setTitle("Ducking hipster");
         glWindow.setSize(WIDTH, HEIGHT);
         glWindow.setUndecorated(false);
         glWindow.setPointerVisible(true);
         glWindow.setVisible(true);
 
         glWindow.addGLEventListener(new Main());
         glWindow.addKeyListener(new KeyListener() {
 
             @Override
             public void keyTyped(KeyEvent key) {
                 if (key.getKeyCode() == KeyEvent.VK_SPACE) {
 
                     Main.game.reset();
                 }
             }
 
             @Override
             public void keyReleased(KeyEvent key) {
                 switch (key.getKeyCode()) {
                 case KeyEvent.VK_LEFT:
                     game.player.direction.x -= 1f;
                     break;
                 case KeyEvent.VK_RIGHT:
                     game.player.direction.x += 1f;
                     break;
                 }
                if(game.player.direction.x > 1) game.player.direction.x = 1f;
                if(game.player.direction.x < -1) game.player.direction.x = -1f;
 
             }
 
             @Override
             public void keyPressed(KeyEvent key) {
                 switch (key.getKeyCode()) {
                 case KeyEvent.VK_LEFT:
                     game.player.direction.x += 1f;
                     break;
                 case KeyEvent.VK_RIGHT:
                     game.player.direction.x -= 1f;
                     break;
                 }
                if(game.player.direction.x > 1) game.player.direction.x = 1f;
                if(game.player.direction.x < -1) game.player.direction.x = -1f;
             }
         });
         Animator animator = new Animator(glWindow);
         animator.add(glWindow);
         animator.start();
     }
     
     private int createSimpleTexture2D(GL2ES2 gl )
     {
         // Texture object handle
         int[] textureId = new int[1];
         
         // 2x2 Image, 3 bytes per pixel (R, G, B)
         byte[] pixels = 
             {  
                 (byte) 0xff,   0,   0, // Red
                 0, (byte) 0xff,   0, // Green
                 0,   0, (byte) 0xff, // Blue
                 (byte) 0xff, (byte) 0xff,   0  // Yellow
             };
         ByteBuffer pixelBuffer = ByteBuffer.allocateDirect(4*3);
         pixelBuffer.put(pixels).position(0);
 
         // Use tightly packed data
         gl.glPixelStorei ( GL_UNPACK_ALIGNMENT, 1 );
 
         //  Generate a texture object
         gl.glGenTextures ( 1, textureId, 0 );
 
         // Bind the texture object
         gl.glBindTexture ( GL_TEXTURE_2D, textureId[0] );
 
         //  Load the texture
         gl.glTexImage2D ( GL_TEXTURE_2D, 0, GL_RGB, 2, 2, 0, GL_RGB, GL_UNSIGNED_BYTE, pixelBuffer );
 
         // Set the filtering mode
         gl.glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST );
         gl.glTexParameteri ( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST );
 
         return textureId[0];        
     }
 
     @Override
     public void init(GLAutoDrawable drawable) {
         GL2ES2 gl = drawable.getGL().getGL2ES2();
 
         // Create shaders
         // OpenGL ES retuns a index id to be stored for future reference.
         vertShader = gl.glCreateShader(GL2ES2.GL_VERTEX_SHADER);
         fragShader = gl.glCreateShader(GL2ES2.GL_FRAGMENT_SHADER);
 
         // Compile the vertexShader String into a program.
         String[] vlines = { Shaders.vertexShader };
         int[] vlengths = { vlines[0].length() };
         gl.glShaderSource(vertShader, vlines.length, vlines, vlengths, 0);
         gl.glCompileShader(vertShader);
 
         // Check compile status.
         int[] compiled = new int[1];
         gl.glGetShaderiv(vertShader, GL2ES2.GL_COMPILE_STATUS, compiled, 0);
         if (compiled[0] == 0) {
             int[] logLength = new int[1];
             gl.glGetShaderiv(vertShader, GL2ES2.GL_INFO_LOG_LENGTH, logLength, 0);
 
             byte[] log = new byte[logLength[0]];
             gl.glGetShaderInfoLog(vertShader, logLength[0], (int[]) null, 0, log, 0);
 
             System.err.println("Error compiling the vertex shader: " + new String(log));
             System.exit(1);
         }
 
         // Compile the fragmentShader String into a program.
         String[] flines = new String[] { Shaders.fragmentShader };
         int[] flengths = new int[] { flines[0].length() };
         gl.glShaderSource(fragShader, flines.length, flines, flengths, 0);
         gl.glCompileShader(fragShader);
 
         // Check compile status.
         gl.glGetShaderiv(fragShader, GL2ES2.GL_COMPILE_STATUS, compiled, 0);
         if (compiled[0] == 0) {
             int[] logLength = new int[1];
             gl.glGetShaderiv(fragShader, GL2ES2.GL_INFO_LOG_LENGTH, logLength, 0);
 
             byte[] log = new byte[logLength[0]];
             gl.glGetShaderInfoLog(fragShader, logLength[0], (int[]) null, 0, log, 0);
 
             System.err.println("Error compiling the fragment shader: " + new String(log));
             System.exit(1);
         }
 
         // Each shaderProgram must have
         // one vertex shader and one fragment shader.
         shaderProgram = gl.glCreateProgram();
         gl.glAttachShader(shaderProgram, vertShader);
         gl.glAttachShader(shaderProgram, fragShader);
 
         // Associate attribute ids with the attribute names inside
         // the vertex shader.
         gl.glBindAttribLocation(shaderProgram, 0, "attribute_Position");
         gl.glBindAttribLocation(shaderProgram, 1, "attribute_Color");
         gl.glGetUniformLocation (shaderProgram, "s_texture" );
         gl.glLinkProgram(shaderProgram);
         
         texture1 = createSimpleTexture2D(gl);
         
         // Get a id number to the uniform_Projection matrix
         // so that we can update it.
         projectionMatrix_location = gl.glGetUniformLocation(shaderProgram, "uniform_Projection");
         modelMatrix_location = gl.glGetUniformLocation(shaderProgram, "uniform_Model");
         sampler_location = gl.glGetUniformLocation(shaderProgram, "s_texture");
 
     }
 
     @Override
     public void display(GLAutoDrawable drawable) {
 
         // Update variables used in animation
         double t1 = System.currentTimeMillis();
         theta = (t1 - t0) * 0.001;
         t0 = t1;
 
         GL2ES2 gl = drawable.getGL().getGL2ES2();
 
         game.tick(theta);
         game.draw(gl);
 
     }
 
     @Override
     public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
         WIDTH = w;
         HEIGHT = h;
 
         GL2ES2 gl = drawable.getGL().getGL2ES2();
 
         gl.glViewport(0, 0, WIDTH, HEIGHT);
     }
 
     @Override
     public void dispose(GLAutoDrawable drawable) {
         GL2ES2 gl = drawable.getGL().getGL2ES2();
         gl.glUseProgram(0);
         gl.glDetachShader(shaderProgram, vertShader);
         gl.glDeleteShader(vertShader);
         gl.glDetachShader(shaderProgram, fragShader);
         gl.glDeleteShader(fragShader);
         gl.glDeleteProgram(shaderProgram);
         System.exit(0);
     }
 }
