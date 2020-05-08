 package sculptnect;
 import java.awt.Frame;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.media.opengl.GLCapabilities;
 import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
 
 import org.openkinect.freenect.Context;
 import org.openkinect.freenect.Device;
 import org.openkinect.freenect.Freenect;
 
 import com.jogamp.opengl.util.FPSAnimator;
 
 public class Sculptnect {
 	private Context kinectContext = null;
 	private Device kinect = null;
 
 	public Sculptnect() {
 		// Set up Kinect
 		kinectContext = Freenect.createContext();
 		if (kinectContext.numDevices() > 0) {
 			kinect = kinectContext.openDevice(0);
 		} else {
 			System.err.println("Error, no Kinect detected.");
 		}
 
 		GLProfile glp = GLProfile.get(GLProfile.GL2);
 
 		// Set the OpenGL canvas creation parameters
 		GLCapabilities caps = new GLCapabilities(glp);
 		caps.setRedBits(8);
 		caps.setGreenBits(8);
 		caps.setBlueBits(8);
 		caps.setDepthBits(32);
 		caps.setSampleBuffers(true);
 		caps.setNumSamples(4);
 
 		final SculptScene scene = new SculptScene();
 
 		// Create GLCanvas
		GLCanvas canvas = new GLCanvas(caps);
 		canvas.addGLEventListener(scene);
 		canvas.setFocusable(false);
 
 		// Create new AWT window to contain the GLCanvas
 		Frame frame = new Frame("Sculptnect");
 		frame.add(canvas);
 		frame.setSize(800, 800);
 		frame.setVisible(true);
 
 		// Add and start a display link
 		FPSAnimator animator = new FPSAnimator(canvas, 60);
 		animator.start();
 
 		// Add listener to respond to window closing
 		frame.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				exit(0);
 			}
 		});
 
 		// Add key listener
 		frame.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 				// Exit if ESC was pressed
 				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
 					exit(0);
 				}
 			}
 		});
 
 		// Create mouse listener
 		MouseAdapter mouseAdapter = new MouseAdapter() {
 			int prevX, prevY;
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 				prevX = e.getX();
 				prevY = e.getY();
 			}
 
 			@Override
 			public void mouseDragged(MouseEvent e) {
 				scene.mouseDragged(prevX, prevY, e.getX(), e.getY());
 				prevX = e.getX();
 				prevY = e.getY();
 			}
 		};
 
 		// Add the mouse listener
 		canvas.addMouseMotionListener(mouseAdapter);
 		canvas.addMouseListener(mouseAdapter);
 	}
 
 	public void exit(int exitCode) {
 		// Clean up Kinect before exiting
 		cleanup();
 
 		System.exit(exitCode);
 	}
 
 	public void cleanup() {
 		// Shut down Kinect
 		if (kinectContext != null) {
 			if (kinect != null) {
 				kinect.close();
 			}
 		}
 		kinectContext.shutdown();
 	}
 
 	public static void main(String[] args) throws InterruptedException {
 		new Sculptnect();
 	}
 }
