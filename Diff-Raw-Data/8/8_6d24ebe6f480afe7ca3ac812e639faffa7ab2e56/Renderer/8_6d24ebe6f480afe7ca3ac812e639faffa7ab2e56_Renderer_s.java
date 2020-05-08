 /*
  *	This is the thing that draws thing.  There's not much more to it.
  * 
  *	//TODO:  Add functions for modifying how light is produced/drawn
  */
 
 package engine.render;
 
 import java.awt.Canvas;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 
 import javax.vecmath.Vector3f;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GLContext;
 import org.lwjgl.opengl.PixelFormat;
 import org.lwjgl.util.glu.GLU;
 
 import engine.entity.Camera;
 import engine.entity.EntityList;
 import engine.window.WindowManager;
 
 public class Renderer {
 	private WindowManager window_manager;
 	private EntityList objectList;
 	private Camera camera;
 	private static boolean supportsVBO = false;
 	// private float x=0,y=0,z=0;
 
 	public static float nearClipping = 1f;
 	public static float farClipping = 10000.0f;
 	private float zoom = 1f; // The closer this value is to 0, the farther you
 								// are zoomed in.
 
 	// Default light (needs turning into an entity
 	private float lightAmbient[] = { 0.2f, 0.2f, 0.2f, 1.0f };
 	private float lightDiffuse[] = { 0.3f, 0.3f, 0.3f, 1f };
 	private float lightSpecular[] = { 0.5f, 0.5f, 0.5f, 1.0f };
 	private float lightPosition[] = { 0.0f, 10.0f, 0.0f, 1.0f };
 	
 	private Canvas display_parent;
 
 	public Renderer(EntityList objectList) {
 		this(objectList, null);
 	}
 
 	public Renderer(EntityList objectList, Canvas display_parent) {
 		this.objectList = objectList;
 		this.display_parent = display_parent;
 	}
 
 	public void draw() {
 		// Clear The Screen And The Depth Buffer
 		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 		GL11.glLoadIdentity();
 
 		Vector3f camPos;
 		Vector3f focusPos;
 		Vector3f up;
 
 		if (camera != null) {
 			camera.updatePosition();
 			
 			// Get its new position
 			camPos = camera.getPosition();
 			focusPos = camera.getFocusPosition();
 			up = camera.getUp();
 		} else {
 			camera = (Camera) objectList.getItem(Camera.NAME);
 			camPos = new Vector3f(0, 0, 20);
 			focusPos = new Vector3f(0, 0, 0);
 			up = new Vector3f(0, 1, 0);
 		}
 
 		// Look at the camera's focus
 		GLU.gluLookAt(camPos.x, camPos.y, camPos.z, // Camera Location
 			focusPos.x, focusPos.y, focusPos.z, // Focus On Location
 			up.x, up.y, up.z // Up Vector
 		);
 
 		// Draw the 3d stuff
 		objectList.drawList();
 
 		// Draw the window manager stuff
 		if (window_manager != null) window_manager.draw();
 
 		GL11.glFlush();
 		Display.update();
 
 		// Reduce input lag
 		// Display.processMessages(); // process new native messages since
 	}
 
 	public void initGL() {
 		// Setup Display
 		try {
 			if (display_parent != null) {
 				Display.setParent(display_parent);
 			}
			Display.setDisplayMode(new DisplayMode(900, 900));
 			Display.create(new PixelFormat(24,8,24,0,0));
 			Display.setTitle("JGE3d");
 
 			// Create a fullscreen window with 1:1 orthographic 2D projection
 			// (default)
 			Display.setFullscreen(false);
 
 			// Enable vsync if we can (due to how OpenGL works, it cannot be
 			// guarenteed to always work)
 			// TODO: Make Configurable by User
 			Display.setVSyncEnabled(true);
 		} catch (LWJGLException e) {
 			e.printStackTrace();
 		}
 		window_manager = new WindowManager();
 
 		// camera = (Camera) objectList.getItem(Camera.CAMERA_NAME);
 
 		setPerspective();
 
 		// Set default openGL for drawing
 		GL11.glShadeModel(GL11.GL_SMOOTH);
 		GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
 		GL11.glClearDepth(1.0f);
 		GL11.glEnable(GL11.GL_DEPTH_TEST);
 		GL11.glDepthFunc(GL11.GL_LEQUAL);
 
 		// Initialize default settings
 		ByteBuffer temp = ByteBuffer.allocateDirect(16);
 		temp.order(ByteOrder.nativeOrder());
 
 		if (GLContext.getCapabilities().GL_ARB_vertex_buffer_object) {
 			supportsVBO = true;
 		} else {
 			supportsVBO = false;
 		}
 
 		// Blending functions so we can have transparency
 		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 		GL11.glEnable(GL11.GL_BLEND);
 
 		// ???
 		// GL11.glEnable(GL11.GL_CULL_FACE);
 
 		// Enable texturing
 		// GL11.glEnable(GL11.GL_TEXTURE_2D);
 
 		// Enable color materials (hopefully will speedup since we don't call
 		// glMaterial anymore this way)
 		//GL11.glColorMaterial(
 		//	GL11.GL_FRONT_AND_BACK,
 		//	GL11.GL_AMBIENT_AND_DIFFUSE
 		//);
 		//GL11.glEnable(GL11.GL_COLOR_MATERIAL);
 
 		// Setup openGL hints for quality
 		//GL11.glHint(GL11.GL_POINT_SMOOTH_HINT, GL11.GL_NICEST);
 		//GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
 		//GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
 		//GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
 		// Anti-aliasing stuff (probably isn't working)
 		//GL11.glEnable(GL11.GL_POINT_SMOOTH);
 		//GL11.glEnable(GL11.GL_LINE_SMOOTH);
 		//GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
 
 		// Enable lighting
 		GL11.glEnable(GL11.GL_LIGHTING);
 		// Create some debug lights
 		// Setup The Ambient Light
 		GL11.glLight(
 			GL11.GL_LIGHT0, 
 			GL11.GL_AMBIENT, 
 			(FloatBuffer) temp.asFloatBuffer().put(lightAmbient).flip()
 		);
 		// Setup The Diffuse Light
 		GL11.glLight(
 			GL11.GL_LIGHT0, 
 			GL11.GL_DIFFUSE, 
 			(FloatBuffer) temp.asFloatBuffer().put(lightDiffuse).flip()
 		);
 		// Setup The Specular Light
 		GL11.glLight(
 			GL11.GL_LIGHT0, 
 			GL11.GL_SPECULAR, 
 			(FloatBuffer) temp.asFloatBuffer().put(lightSpecular).flip()
 		);
 		// Position The Light
 		GL11.glLight(
 			GL11.GL_LIGHT0, 
 			GL11.GL_POSITION, 
 			(FloatBuffer) temp.asFloatBuffer().put(lightPosition).flip()
 		);
 		GL11.glEnable(GL11.GL_LIGHT0);
 		
 		//GL11.glEnable(GL11.GL_NORMALIZE);
 	}
 
 	public void setPerspective() {
 		setPerspective(nearClipping, farClipping, zoom);
 	}
 
 	public void setPerspective(float near, float far, float zoomVal) {
 		nearClipping = near;
 		farClipping = far;
 		if (zoomVal <= 1.0 && zoomVal > 0) {
 			zoom = zoomVal;
 		} else if (zoomVal > 1000.0) {
 			zoom = 1.0f;
 		} else {
 			zoom = 0.1f; // TODO: I guess this is the smallest zoom we'd want?
 		}
 
 		// Calculate the shape of the screen and notify OpenGL
 		GL11.glMatrixMode(GL11.GL_PROJECTION);
 		GL11.glLoadIdentity();
 		GLU.gluPerspective(
 			45.0f / zoom, 
 			(float) Display.getDisplayMode().getWidth() / Display.getDisplayMode().getHeight(), 
 			nearClipping,
 			farClipping
 		);
 
 		GL11.glMatrixMode(GL11.GL_MODELVIEW);
 		GL11.glLoadIdentity();
 	}
 
 	public static boolean supportsVBO() {
 		return supportsVBO;
 	}
 	
 	public WindowManager getWindowManager() {
 		return window_manager;
 	}
 
 	public void destroy() {
 		window_manager.destroy();
 	}
 
 	public void setCamera(Camera camera) {
 		this.camera = camera;
 	}
 }
