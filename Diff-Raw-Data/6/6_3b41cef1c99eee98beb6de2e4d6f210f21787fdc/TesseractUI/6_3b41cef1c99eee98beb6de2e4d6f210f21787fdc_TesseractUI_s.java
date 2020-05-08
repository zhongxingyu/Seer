 package tesseract;
 
 import java.awt.GraphicsConfiguration;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 
 import javax.media.j3d.BoundingBox;
 import javax.media.j3d.Canvas3D;
 import javax.media.j3d.Transform3D;
 import javax.media.j3d.TransformGroup;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 import javax.vecmath.Point3d;
 import javax.vecmath.Vector3f;
 
 import tesseract.forces.Gravity;
 import tesseract.objects.Particle;
 import tesseract.objects.emitters.ParticleEmitter;
 
 import com.sun.j3d.utils.universe.SimpleUniverse;
 
 /**
  * This class is the main UI for the Tesseract Project.
  * 
  * @author Jesse Morgan
  */
 public class TesseractUI extends JFrame {
 	
 	/**
 	 * Generated serialVersionUID.
 	 */
 	private static final long serialVersionUID = 4097744746899308736L;
 	
 	/**
 	 * Update Rate.
 	 */
 	private static final int UPDATE_RATE = 30;
 	
 	/**
 	 * Measure of 1 unite of space in the world.
 	 */
 	private static final float UNIT = 1;
 
 	/**
 	 * Number of miliseconds in 1 second.
 	 */
 	private static final int MILISECONDS_IN_SECOND = 1000;
 	
 	/**
 	 * A reference to the world.
 	 */
 	private World myWorld;
 	
 	/**
 	 * The Canvas.
 	 */
 	private Canvas3D myCanvas;
 
 	/**
 	 * Camera TransformGroup.
 	 */
 	private TransformGroup cameraTG;
 	
 	/**
 	 * Camera position information.
 	 */
 	private double cameraXRotation, cameraYRotation, cameraDistance;
 	
 	/**
 	 * UI Constructor.
 	 */
 	public TesseractUI() {
 		super("Tesseract Project");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
 		
 		myWorld = new World(
 				new BoundingBox(new Point3d(-UNIT / 2, -UNIT / 2, -UNIT / 2), 
 						new Point3d(UNIT / 2, UNIT / 2, UNIT / 2)));
 		
 		createMenu();
 		setupCanvas();
 		pack();
 		
 		// Maximize the windows
 		if (Toolkit.getDefaultToolkit().
 				isFrameStateSupported(JFrame.MAXIMIZED_BOTH)) {
 			setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
 		}
 		
 		// THIS IS WHERE OBJECTS ARE FORCED INTO EXISTANCE
 		// TODO: REMOVE TEST CODE
 		myWorld.addObject(new Particle(new Vector3f(0, 0, 0), null));
 		myWorld.addForce(new Gravity());
 		myWorld.addObject(new ParticleEmitter(new Vector3f(0, 0.49f, 0), 0.5f, null));
 	}
 	
 	/**
 	 * Create the menu.
 	 */
 	private void createMenu() {
 		JMenuBar menuBar = new JMenuBar();
 		
 		JMenu simulationMenu = new JMenu("Simulation");
 		menuBar.add(simulationMenu);
 		
 		/*
 		JCheckBoxMenuItem cMenuItem = new JCheckBoxMenuItem("Enable Particle Emitters", enableEmitters);
 		cMenuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				enableEmitters = !enableEmitters;
 			}			
 		});
 		menu.add(cMenuItem);
 		
 		for (int i = 0; i < forces.length; i++) {
 			cMenuItem = new JCheckBoxMenuItem(forces[i].toString(), activeForces[i]);
 			cMenuItem.setActionCommand(i + "");
 			cMenuItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					int index = Integer.parseInt(e.getActionCommand());
 					activeForces[index] = !activeForces[index];
 				}			
 			});
 			menu.add(cMenuItem);
 		}
 		*/
 		
 		// Exit Menu Item
 		JMenuItem exit = new JMenuItem("Exit");
 		exit.addActionListener(new ActionListener() {
 			public void actionPerformed(final ActionEvent e) {
 				// TODO: I feel this is the wrong way of exiting...
 				System.exit(0);
 			}
 		});
 		simulationMenu.add(exit);
 		
 		setJMenuBar(menuBar);
 	}
 	
 	/**
 	 * Create and show the UI.
 	 */
 	private void setupCanvas() {
 		GraphicsConfiguration config
 			= SimpleUniverse.getPreferredConfiguration();
 		
 		myCanvas = new Canvas3D(config);
 		
 		SimpleUniverse universe = new SimpleUniverse(myCanvas);
 		universe.getViewer().getView().setSceneAntialiasingEnable(true);
 
 		// Set the camera
 		cameraTG = universe.getViewingPlatform().getViewPlatformTransform();
 		cameraDistance = 3 * UNIT;
 		updateCamera();
 		
 		// Add the scene BG.
 		universe.addBranchGraph(myWorld.getScene());
 
 		// Add the canvas to the frame.
 		add(myCanvas);
 		
 		
 		// Event listener time
 		myCanvas.addMouseMotionListener(new MouseMotionAdapter() {
 			private MouseEvent lastDragEvent = null;
 			
 			public void mouseDragged(final MouseEvent e) {
 				if (lastDragEvent != null) {
 					cameraXRotation += 
 						Math.toRadians(e.getY() - lastDragEvent.getY()) / 3;
 					
 					if (cameraXRotation > Math.PI / 2) {
 						cameraXRotation = Math.PI / 2;
 						
 					} else if (cameraXRotation < -Math.PI / 2) {
 						cameraXRotation = -Math.PI / 2;
 					}
 					
 					cameraYRotation += 
 						Math.toRadians(e.getX() - lastDragEvent.getX()) / 3;
 					
 					updateCamera();
 				}
 				
 				lastDragEvent = e;
 			}
 			
 			public void mouseMoved(final MouseEvent e) {
 				lastDragEvent = null;
 			}
 		});
 		
 		myCanvas.addMouseWheelListener(new MouseWheelListener() {
 			public void mouseWheelMoved(final MouseWheelEvent e) {
 				if (e.getWheelRotation() > 0) {
 					cameraDistance *= 1.05;
 				
 				} else if (e.getWheelRotation() < 0) {
 					cameraDistance *= 0.95;
 				}
 				
 				updateCamera();
 			}
 		});
 		
 		// Setup the timer.
  		new Timer(MILISECONDS_IN_SECOND / UPDATE_RATE, new ActionListener() {
 			public void actionPerformed(final ActionEvent e) {
 				myCanvas.stopRenderer();
 				myWorld.tick();
 				myCanvas.startRenderer();
 			}
 		}).start();
 		
 	}
 	
 	/**
 	 * Method to update the camera.
 	 */
 	private void updateCamera() {
 		Transform3D camera3D = new Transform3D();
 		camera3D.setTranslation(new Vector3f(0f, 0f, (float) -cameraDistance));
 		Transform3D tmp = new Transform3D();
 		tmp.rotX(cameraXRotation);
 		camera3D.mul(tmp);
 		tmp.rotY(cameraYRotation);
 		camera3D.mul(tmp);
 		camera3D.invert();
 		cameraTG.setTransform(camera3D);
 	}
 	
 	/**
 	 * Start up the program.
 	 * 
 	 * @param args Unused commandline arguments.
 	 */
 	public static void main(final String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				new TesseractUI().setVisible(true);
 			}
 		});
 	}
 }
