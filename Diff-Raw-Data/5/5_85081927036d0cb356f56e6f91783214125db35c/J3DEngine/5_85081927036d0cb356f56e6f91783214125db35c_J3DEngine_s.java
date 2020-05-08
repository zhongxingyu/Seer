 package se2.e.engine3d.j3d;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.GraphicsConfiguration;
 import java.awt.HeadlessException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.media.j3d.BranchGroup;
 import javax.media.j3d.Canvas3D;
 import javax.media.j3d.Group;
 import javax.media.j3d.Node;
 import javax.media.j3d.Transform3D;
 import javax.media.j3d.TransformGroup;
 import javax.media.j3d.View;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.vecmath.Point3d;
 import javax.vecmath.Vector3d;
 
 import se2.e.engine3d.Engine3D;
 import se2.e.engine3d.Engine3DListener;
 import se2.e.engine3d.GeometryAndAppearanceLoader;
 import se2.e.engine3d.j3d.animations.RuntimeAnimation;
 import se2.e.geometry.Geometry;
 import se2.e.simulator.runtime.petrinet.RuntimeToken;
 import animations.Animation;
 import appearance.AppearanceModel;
 
 import com.sun.j3d.utils.geometry.ColorCube;
 import com.sun.j3d.utils.universe.SimpleUniverse;
 import com.sun.j3d.utils.universe.Viewer;
 import com.sun.j3d.utils.universe.ViewingPlatform;
 
 /**
  * The Class J3DEngine that is an implementation of an {@link Engine3D} using Java 3D library.
  * 
  * @author cosmin
  */
 public class J3DEngine extends JFrame implements Engine3D, ActionListener {
 
 	/** The Constant serialVersionUID. */
 	private static final long serialVersionUID = 5165791727088692312L;
 
 	/** The universe. */
 	private SimpleUniverse universe;
 
 	/** The start button. */
 	private JButton btnStart;
 
 	/** The stop button. */
 	private JButton btnStop;
 
 	/** The pause button. */
 	private JButton btnPause;
 
 	/** The animation progress listener. */
 	private Engine3DListener engineListener = null;
 
 	/** The scene branch group. */
 	private BranchGroup sceneRoot;
 
 	/** The geometry. */
 	private GeometryAndAppearanceLoader loader;
 
 	/** The dynamic branch factory. */
 	private DynamicBranchFactory dynamicBranchFactory;
 
 	/** The geometry node factory. */
 	private GeometryNodeFactory geometryNodeFactory;
 
 	/** The logger. */
 	private Logger log = Logger.getLogger("J3DEngine");
 
 	/** The running animations. */
 	private List<RuntimeAnimation> runningAnimations;
 
 	private Canvas3D canvas;
 
 	/**
 	 * Instantiates a new Java 3D engine.
 	 * 
 	 * @throws HeadlessException the headless exception
 	 * @author cosmin
 	 */
 	public J3DEngine() throws HeadlessException {
 		// Setup a SimpleUniverse by referencing a Canvas3D
 		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
 		canvas = new Canvas3D(config);
 		Container cp = this.getContentPane();
 		cp.setLayout(new BorderLayout());
 		cp.add(canvas, BorderLayout.CENTER);
 
 		// Initialize the buttons
 		JPanel panel = new JPanel();
 		getContentPane().add(panel, BorderLayout.NORTH);
 
 		btnStart = new JButton("Start");
 		btnStart.addActionListener(this);
 		panel.add(btnStart);
 
 		btnStop = new JButton("Stop");
 		btnStop.addActionListener(this);
 		btnStop.setEnabled(false);
 		panel.add(btnStop);
 
 		btnPause = new JButton("Pause");
 		btnPause.addActionListener(this);
 		btnPause.setEnabled(false);
 		panel.add(btnPause);
 
 		// Configure this JFrame
 		this.setLocation(200, 200);
 		this.setSize(640, 480);
 		this.setTitle("Hello Universe");
 		// this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 
 		// Show the JFrame
 		this.setVisible(true);
 
 	}
 
 	/**
 	 * Creates the universe and set up the viewer.
 	 * 
 	 * @param canvas the canvas
 	 * @return the simple universe
 	 * @author cosmin
 	 */
 	private SimpleUniverse createUniverse(Canvas3D canvas) {
 		// Manually create the viewing platform so that we can customize it
 		ViewingPlatform viewingPlatform = new ViewingPlatform();
 
 		// Set the view position back far enough so that we can see things
 		TransformGroup viewTransform = viewingPlatform.getViewPlatformTransform();
 		Transform3D t3d = new Transform3D();
 		// Compute initial values for viewer
 		double xCenter = (loader.maxX + loader.minX) / 2;
 		double yCenter = (loader.maxY + loader.minY) / 2;
 		double zHeight = ((loader.maxX - loader.minX) + (loader.maxY - loader.minY));
 		t3d.lookAt(new Point3d(xCenter, yCenter, zHeight), new Point3d(xCenter, yCenter, 0), new Vector3d(0, 1, 0));
 		t3d.invert();
 		viewTransform.setTransform(t3d);
 
 		// Set the activation radius
 		viewingPlatform.getViewPlatform().setActivationRadius((float) (2 * zHeight));
 
 		// Set back clip distance so things don't disappear
 		Viewer viewer = new Viewer(canvas);
 		View view = viewer.getView();
 		view.setBackClipDistance(2 * zHeight);
 
 		return new SimpleUniverse(viewingPlatform, viewer);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see se2.e.engine3d.Engine3D#init(java.lang.Object, java.lang.Object)
 	 * 
	 * @author cosmin, marius (some fixes after update of loader class)
 	 */
 	@Override
 	public void init(Geometry geometry, AppearanceModel appearance) {
 
 		// Load the geometry and appearance
 		this.loader = new GeometryAndAppearanceLoader(geometry, appearance);
 		this.geometryNodeFactory = new GeometryNodeFactory(loader);
 
 		// Load the universe
 		universe = createUniverse(canvas);
 
 		// Create the initial scene and add it to the graph
 		sceneRoot = createSceneGraph();
 		universe.addBranchGraph(sceneRoot);
 
 		BranchGroup br = new BranchGroup();
 		br.addChild(new ColorCube());
 		sceneRoot.addChild(br);
 
 		// Initialize other objects
 		runningAnimations = new ArrayList<RuntimeAnimation>();
		this.dynamicBranchFactory = new DynamicBranchFactory(loader);
 
 		log.info("J3D Engine initialized...");
 	}
 
 	/**
 	 * Creates the scene graph (content branch) and add the static objects (e.g. {@link Place}s representations).
 	 * 
 	 * @return the branch group
 	 * @author cosmin
 	 */
 	public BranchGroup createSceneGraph() {
 		// Create the root node of the content branch
 		BranchGroup rootNode = new BranchGroup();
 
 		// Add representations for the static objects
 		Group trackGroup = new Group();
 		for (String label : loader.getTrackLabels()) {
 
 			// Create the node corresponding to tracks and add it to the scene graph
 			Node geometryNode = geometryNodeFactory.getGeometryNode(label);
 			if (geometryNode != null)
 				trackGroup.addChild(geometryNode);
 
 		}
 		rootNode.addChild(trackGroup);
 
 		// Compile to perform optimizations on this content branch.
 		rootNode.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
 		rootNode.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
 		rootNode.compile();
 
 		return rootNode;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 
 		// If the user clicked the Start Button
 		if (e.getSource() == btnStart) {
 			log.info("Starting engine 3D...");
 			if (this.engineListener != null) {
 				engineListener.onStartSimulation();
 				btnStart.setEnabled(false);
 			}
 		}
 
 		// TODO: Add implementation for Pause and Stop
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see se2.e.engine3d.Engine3D#startAnimation(petrinet.Token, petrinet.Place)
 	 * 
 	 * @author cosmin
 	 */
 	@Override
 	public void startAnimation(RuntimeToken token, Animation animationOnPlace) {
 		// TODO: Update all types of animations
 
 		// Build the branch representing the token
 		// TODO: Fix for dynamic objects (e.g. when using ShowAnimation)
 		DynamicBranch branch = dynamicBranchFactory.getTokenBranch(token.getLabel());
 
 		// Build the RuntimeAnimation
 		// RuntimeAnimation rtAnimation = RuntimeAnimationFactory.getRuntimeAnimation(branch, animation, token);
 		// runningAnimations.add(rtAnimation);
 
 		// Attach the Animation branch to the Scene graph
 		sceneRoot.addChild(branch.getBranchGroup());
 
 		// TODO: Create a behavior that runs the animation
 
 		// // Get the associated geometry
 		// GeometryObject geometryObj = loader.getGeometryObject(placeLabel);
 		// if (!(geometryObj instanceof Track)) {
 		// log.severe("Starting animation for token " + token + " on place with wrong type of geometry: "
 		// + geometryObj);
 		// return;
 		// }
 		// Track track = (Track) geometryObj;
 
 		// See if there is already an object representation for the token
 		// RunningAnimation animation;
 		// animation = runningAnimations.get(token);
 		// if (animation == null) {
 		//
 		// // Create a BranchGroup for this Move animation, which will contain the Token Representation, the
 		// // interpolator for the animation and a behavior to clean up when the animation is finished
 		// BranchGroup animationsRoot = new BranchGroup();
 		// animationsRoot.setCapability(BranchGroup.ALLOW_DETACH);
 		//
 		// // Create the TransformGroup node, which is writable to support animation
 		// TransformGroup tokenRepresGroup = new TransformGroup();
 		// tokenRepresGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 		// animationsRoot.addChild(tokenRepresGroup);
 		//
 		// // Create the token representation node (color cube) and add it to the group
 		// // TODO: Eventually get info from Place
 		// tokenRepresGroup.addChild(new ColorCube(0.2));
 		//
 		// // Save the running animation for future use
 		// animation = new RunningAnimation(track, tokenRepresGroup, animationsRoot, 0);
 		// runningAnimations.put(token, animation);
 		//
 		// }
 		//
 		// /** Start the animation **/
 		// // Get the track points and their coordinates
 		// TrackPosition firstPoint = track.getStartPosition();
 		// TrackPosition lastPoint = track.getEndPosition();
 		//
 		// // Put the token representation at the beginning of the path
 		// Transform3D initTransform = new Transform3D();
 		// initTransform.setTranslation(new Vector3d(firstPoint.getPosition().getX(), firstPoint.getPosition().getY(),
 		// DRAWING_PLANE_Z));
 		// animation.transformGroup.setTransform(initTransform);
 		//
 		// // Create a Behavior (Interpolator) node that moves the cube and add it to the scene
 		// Transform3D yAxis = new Transform3D();
 		// Alpha timing = new Alpha(1, 4000);
 		// timing.setStartTime(new Date().getTime());
 		// timing.setMode(Alpha.INCREASING_ENABLE);
 		// Point3f startPoint = new Point3f(new Point3d(firstPoint.getPosition().getX(),
 		// firstPoint.getPosition().getY(), DRAWING_PLANE_Z));
 		// Point3f endPoint = new Point3f(new Point3d(lastPoint.getPosition().getX(), lastPoint.getPosition().getY(),
 		// DRAWING_PLANE_Z));
 		// PositionPathInterpolator nodePositionInterpolator = new PositionPathInterpolator(timing,
 		// animation.transformGroup, yAxis, new float[] { 0, 1 }, new Point3f[] { startPoint, endPoint });
 		// BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
 		// nodePositionInterpolator.setSchedulingBounds(bounds);
 		// animation.animationBranchGroup.addChild(nodePositionInterpolator);
 		//
 		// // Create a behavior that handles the finish of the animation
 		// FinishAnimationBehavior beh = new FinishAnimationBehavior(this, animation, token, 4000);
 		// animation.animationBranchGroup.addChild(beh);
 		//
 		// // Add the branch to the root
 		// sceneRoot.addChild(animation.animationBranchGroup);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see se2.e.engine3d.Engine3D#setAnimationProgressListener(se2.e.engine3d.AnimationProgressListener)
 	 * 
 	 * @author cosmin
 	 */
 	@Override
 	public void setEngine3DListener(Engine3DListener listener) {
 		this.engineListener = listener;
 	}
 
 	/**
 	 * Gets the scene root.
 	 * 
 	 * @return the scene root
 	 */
 	protected BranchGroup getSceneRoot() {
 		return sceneRoot;
 	}
 
 	/**
 	 * Run when the animation for a token is finished. Notifies the listener.
 	 * 
 	 * @param token the token
 	 */
 	protected void animationFinished(RuntimeToken token) {
 		engineListener.onAnimationFinished(token);
 	}
 }
