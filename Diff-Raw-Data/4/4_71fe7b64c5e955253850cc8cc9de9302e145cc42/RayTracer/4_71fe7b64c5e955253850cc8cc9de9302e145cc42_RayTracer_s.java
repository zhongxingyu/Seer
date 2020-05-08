 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.vecmath.Point3d;
 import javax.vecmath.Vector3d;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.io.File;
 import java.util.ArrayList;
 
 public class RayTracer {
 
 	final static Point3d RSPHERECENTER = new Point3d(0.25, 0.95, -1.0);
 	final static double RSPHERERADIUS = 0.466;
 
 	final static Point3d LSPHERECENTER = new Point3d(-0.4, 0.65, -1.5);
 	final static double LSPHERERADIUS = 0.40;
 
 	final static Point3d CAMERACENTER = new Point3d(0.0, 1.0, 2.0);
 	final static Point3d CAMERALOOKAT = new Point3d(0.0, 0.0, -1.0); //Direction
 	final static Vector3d CAMERAUP = new Vector3d(0.0, 1.0, 0.0);
 	
 	//final static Point3d LIGHTCENTER = new Point3d(1.0, 1.0, -1.0);
 	final static Point3d LIGHTCENTER = new Point3d(0.3, 2.75, 1.0);
 	final static Point3d LIGHTLEFT = new Point3d(-0.3, 0.75, 1.0);
 	//final static Point3d LIGHTCENTER = new Point3d(.0, 1.0, -2.0);
 	
 	//final static Point3d PLANEVERTLF = new Point3d(-3.0, 0.0, 0.0);
 	//final static Point3d PLANEVERTRF = new Point3d( 2.0, 0.0, 0.0);
 	//final static Point3d PLANEVERTRR = new Point3d( 2.0, 0.0, -20.0);
 	//final static Point3d PLANEVERTLR = new Point3d(-3.0, 0.0, -20.0);
 	
 	final static Point3d PLANEVERTLF = new Point3d(-2.25, 0.0, 0.0);
 	final static Point3d PLANEVERTRF = new Point3d( 1.25, 0.0, 0.0);
 	final static Point3d PLANEVERTRR = new Point3d( 1.25, 0.0, -10.0);
 	final static Point3d PLANEVERTLR = new Point3d(-2.25, 0.0, -10.0);
 
 	final static Point3d TRIANGLEVERTLF = new Point3d(-2.25, 0.0, 0.0);
 	final static Point3d TRIANGLEVERTRF = new Point3d(1.25, 0.0, 0.0);
 	final static Point3d TRIANGLEVERTBC = new Point3d(-1.00, 0.0, -10.0);
 	
 	//Testing Values
 	final static Point3d TEST_CAMERA = new Point3d(0.0, 0.0, 0.0);
 	final static Point3d TEST_SPHERECENTER = new Point3d(0.0, 0.0, -2.0);
 	final static Point3d TEST_SPHERECENTER2 = new Point3d(0.0, 0.0, 2.0);
 	final static double TEST_SPHERERADIUS = 0.50;
 	final static double TEST_SPHERERADIU2 = 0.25;
 	final static Point3d TEST_LIGHT = new Point3d(0.0, 0.0, 0.0);
 	final static Point3d TESTPLANEVERTLF = new Point3d( -0.5, -0.5, -2.0);
 	final static Point3d TESTPLANEVERTRF = new Point3d( 0.5, -0.5, -2.0);
 	final static Point3d TESTPLANEVERTRR = new Point3d( 0.5, 0.5, -2.0);
 	final static Point3d TESTPLANEVERTLR = new Point3d(-0.5, 0.5, -2.0);
 	final static Point3d TEST_TRI_VERTTL = new Point3d(-0.5, -0.5, -10.0);
 	final static Point3d TEST_TRI_VERTTR = new Point3d(0.5, -0.5, -10.0);
 	final static Point3d TEST_TRI_VERTBL = new Point3d(-0.5, -0.5, 0.0);
 	final static Point3d TEST_TRI_VERTBR = new Point3d(0.5, -0.5, 0.0);
 
 	
 	public static void main(String[] args) {
 
 		ArrayList<Point3d> planeVertices = new ArrayList<Point3d>();
 		ArrayList<Point3d> triAVertices = new ArrayList<Point3d>();
 		ArrayList<Point3d> triBVertices = new ArrayList<Point3d>();
 		ArrayList<Point3d> testPlaneVertices = new ArrayList<Point3d>();
 		ArrayList<Point3d> testTriVertices = new ArrayList<Point3d>();
 		ArrayList<Point3d> testTriVertices2 = new ArrayList<Point3d>();
 		ArrayList<Point3d> triangleVertices = new ArrayList<Point3d>();
 		
 
 		planeVertices.add(PLANEVERTLF);
 		planeVertices.add(PLANEVERTRF);
 		planeVertices.add(PLANEVERTRR);
 		planeVertices.add(PLANEVERTLR);
 		
 		triAVertices.add(PLANEVERTLF);
 		triAVertices.add(PLANEVERTRF);
 		triAVertices.add(PLANEVERTRR);
 		
 		triBVertices.add(PLANEVERTLR);
 		triBVertices.add(PLANEVERTRR);
 		triBVertices.add(PLANEVERTLF);
 		
 		testPlaneVertices.add(TESTPLANEVERTLF);
 		testPlaneVertices.add(TESTPLANEVERTRF);
 		testPlaneVertices.add(TESTPLANEVERTRR);
 		testPlaneVertices.add(TESTPLANEVERTLR);
 		
 		testTriVertices.add(TEST_TRI_VERTTL);
 		testTriVertices.add(TEST_TRI_VERTTR);
 		testTriVertices.add(TEST_TRI_VERTBL);
 		
 		testTriVertices2.add(TEST_TRI_VERTBL);
 		testTriVertices2.add(TEST_TRI_VERTBR);
 		testTriVertices2.add(TEST_TRI_VERTTR);
 
 		triangleVertices.add(TRIANGLEVERTLF);
 		triangleVertices.add(TRIANGLEVERTBC);
 		triangleVertices.add(TRIANGLEVERTRF);
 		
 		
 
 		World world = new World();
 		
 		//Add Objects to World
 		world.add( new Sphere(LSPHERECENTER, LSPHERERADIUS, new Color(178.5, 178.5, 178.5), 1.0, 0.0)); //0, 255, 0
 		world.add( new Sphere(RSPHERECENTER, RSPHERERADIUS, new Color(255.0, 255.0, 255.0), 0.0, 0.85)); //200, 10, 10
		world.add( new Triangle (triAVertices, new Vector3d(0,1,0), new Color(200, 200, 10)));
		world.add( new Triangle (triBVertices, new Vector3d(0,1,0), new Color(200, 200, 10)));
 		
 		world.add( new PointLight(LIGHTCENTER, new Color(255.0, 255.0, 255.0)));
 		//world.add( new PointLight(LIGHTLEFT, new Color(255.0, 255.0, 255.0)));
 		
 		//Testing World
 		//world.add(new Sphere(TEST_SPHERECENTER, TEST_SPHERERADIUS, new Color(255.0, 0.0, 0.0), 0.3));
 		//world.add(new Sphere(TEST_SPHERECENTER2, TEST_SPHERERADIU2, new Color(0.0, 255.0, 0.0), 0.3));
 		//world.add( new Rectangle(testPlaneVertices, new Vector3d(0,0,1), new Color(200, 200, 10)));
 		//world.add( new Triangle (testTriVertices, new Vector3d(0,0,1), new Color(200, 200, 10)));
 		//world.add( new Triangle (testTriVertices2, new Vector3d(0,0,1), new Color(200, 200, 10)));
 		//world.add(new PointLight(TEST_LIGHT, new Color(255.0, 255.0, 255.0)));
 
 		
 		//Extras
 		//world.add( new Triangle(triangleVertices, new Vector3d(0,1,0)));
 		//world.add( new Rectangle(planeVertices, new Vector3d(0,1,0), new Color(200, 200, 10)));
 
 		//Camera
 		Camera camera = new Camera(CAMERACENTER, CAMERALOOKAT, CAMERAUP);
 		
 		//Testing Camera
 		//Camera camera = new Camera(TEST_CAMERA, CAMERALOOKAT, CAMERAUP);
 		
 		
 		
 		//World[] worlds = new World[]{ world, world, world, world }; // Array or worlds to render
 		WorldGenerator gen = new WorldTest();
 		World[] worlds = gen.getWorlds();
 		
 		// Setup progress window
 		JFrame frame = new JFrame("Rendering...");
 		frame.setBounds(0, 0, 300, 100);
 		frame.setLayout( new BorderLayout() );
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		// Create progress bars
 		JProgressBar main = new JProgressBar(0, worlds.length);
 		JProgressBar bar = new JProgressBar();
 		main.setValue(0);
 		bar.setValue(0);
 		main.setStringPainted(true);
 		bar.setStringPainted(true);
 		main.setString("Rendering");
 		
 		// Setup left panel, with progress bar labels
 		JPanel left = new JPanel();
 		left.setLayout( new GridLayout(2,1) );
 		left.add( new JLabel("Total:" ) );
 		left.add( new JLabel("Frame:" ) );
 		
 		// Setup right panel, with progress bars
 		JPanel right = new JPanel();
 		right.setLayout( new GridLayout(2,1) );
 		right.add( main );
 		right.add( bar );
 		
 		// Finalize layout and display
 		frame.add( left, BorderLayout.WEST );
 		frame.add( right, BorderLayout.CENTER );
 		frame.setVisible(true);
 		
 		// Render all worlds and save to file
 		for( int i = 0; i < worlds.length; i++ ){
 			bar.setString( "Frame " + i );
 			bar.setValue(0);
 			camera.render( worlds[i], new File( "render_" + String.format( "%1$04d" , i) + ".png" ), bar );
 			main.setValue( i+1 );
 		}
 
 		bar.setString("Done!");
 		main.setString("Done!");
 
 	}
 
 } 
