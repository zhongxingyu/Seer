 package core;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.util.glu.GLU.gluOrtho2D;
 import static org.lwjgl.util.glu.GLU.gluPerspective;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 
 public class FrameUtils {
 	public static final int DEFAULT_WINDOW_WIDTH = 1024;
 	public static final int DEFAULT_WINDOW_HEIGHT = 768;
 	public static final float NEAR_POINT = 0.1f;
 	public static final float FAR_POINT = 250f;
 	
 	public static void initWindow() throws LWJGLException {
 		Display.setDisplayMode(new DisplayMode(DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT));
 		Display.create();
 		Display.setResizable(true);
 		Display.setTitle("Shooter");
 		glViewport(0, 0, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		glMatrixMode(GL_MODELVIEW);
 		glClearColor(0, 0, 0, 1);
 		//glClearColor(94.0f/255.0f, 161.0f/255.0f, 255.0f/255.0f, 0.5f);
 		glClearDepth(1.0);
 		glEnable(GL_DEPTH_TEST);
 		glEnable(GL_BLEND);
 		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 		glEnable(GL_LIGHTING);
 		glEnable(GL_LIGHT0);
 		glShadeModel(GL_SMOOTH);
 	}
 	
 	public static void newFrame() {
 		gluPerspective(45.0f, ((float)Display.getWidth()/(float)Display.getHeight()), NEAR_POINT, FAR_POINT);
 		setViewport();
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		glMatrixMode(GL_MODELVIEW);
 		glLoadIdentity();
 	}
 	
 	public static void set3DMode()
 	{
 		glEnable(GL_DEPTH_TEST);
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		glMatrixMode(GL_MODELVIEW);
 		glLoadIdentity();
 		double aspectRatio = calculateAspectRatio();
 		gluPerspective(45.0f, (float)aspectRatio, NEAR_POINT, FAR_POINT);
 		//glEnable(GL_CULL_FACE);
 		//glCullFace(GL_BACK);
 		glEnable(GL_TEXTURE_2D);
 		glEnable(GL_LIGHTING);
 	}
 
 	public static void set2DMode() {
 		glDisable(GL_DEPTH_TEST);
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		double aspectRatio = calculateAspectRatio();
 		gluOrtho2D(0, (float)aspectRatio, 0, 1);
 		glMatrixMode(GL_MODELVIEW);
 		glLoadIdentity();
 		glEnable(GL_TEXTURE_2D);
 		glDisable(GL_LIGHTING);
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 	}
 
 	public static void setViewport() {
 		glViewport(0, 0, Display.getWidth(), Display.getHeight()); 
 	}
 
 	public static double calculateAspectRatio() {
 		double windowWidth, windowHeight;
 		if((Display.getWidth() == 0) || (Display.getHeight() == 0))
 		{
 			windowWidth = 100d;
 			windowHeight = 100d;
 		} else {
 			windowWidth = Display.getWidth();
 			windowHeight = Display.getHeight();
 		}
 		double aspectRatio = windowWidth/windowHeight;
 		return aspectRatio;
 	}
 }
