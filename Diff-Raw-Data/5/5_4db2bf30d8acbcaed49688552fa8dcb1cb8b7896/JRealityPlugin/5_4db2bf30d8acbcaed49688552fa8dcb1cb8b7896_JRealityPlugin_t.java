 package de.tum.in.jrealityplugin;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Stack;
 
 import de.cinderella.api.cs.CindyScript;
 import de.cinderella.api.cs.CindyScriptPlugin;
 import de.cinderella.math.Vec;
 
 /**
  * Implementation of the plugin interface
  */
 @SuppressWarnings("unchecked")
 public class JRealityPlugin extends CindyScriptPlugin {
 	private Cindy3DViewer cindy3d = null;
 	
 	/**
 	 * Stack of saved point appearances
 	 * @see JRealityPlugin#gsave3d()
 	 * @see JRealityPlugin#grestore3d()
 	 */
 	private Stack<AppearanceState> pointAppearanceStack;
 	/**
 	 * The current point appearance
 	 */
 	private AppearanceState pointAppearance;
 
 	/**
 	 * Stack of saved line appearances
 	 * @see JRealityPlugin#gsave3d()
 	 * @see JRealityPlugin#grestore3d()
 	 */
 	private Stack<AppearanceState> lineAppearanceStack;
 	/**
 	 * The current line appearance
 	 */
 	private AppearanceState lineAppearance;
 
 	/**
 	 * Modifiers for the current CindyScript function call
 	 */
 	private Hashtable modifiers;
 	
 
 	public JRealityPlugin() {
 		pointAppearanceStack = new Stack<AppearanceState>();
 		pointAppearance = new AppearanceState(Color.RED, 1);
 		lineAppearanceStack = new Stack<AppearanceState>();
 		lineAppearance = new AppearanceState(Color.BLUE, 1);
 	}
 
 	@Override
 	public void register() {
 		if (cindy3d == null)
 			cindy3d = new JRealityViewer();
 	}
 
 	@Override
 	public void unregister() {
 		if (cindy3d != null)
 			cindy3d.shutdown();
 		cindy3d = null;
 	}
 
 	@Override
 	public void setModifiers(Hashtable m) {
 		modifiers = m;
 	}
 
 	@Override
 	public Hashtable getModifiers() {
 		return modifiers;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.cinderella.api.cs.CindyScriptPlugin#getAuthor()
 	 */
 	@Override
 	public String getAuthor() {
 		return "Jan Sommer und Matthias Reitinger";
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.cinderella.api.cs.CindyScriptPlugin#getName()
 	 */
 	@Override
 	public String getName() {
 		return "jReality for Cinderella";
 	}
 
 	/**
 	 * Squares the given number
 	 * 
 	 * @param x
 	 * @return The square of x
 	 */
 	@CindyScript("square")
 	public double square(double x) {
 		return x * x;
 	}
 
 	/**
 	 * Prepares drawing of 3D objects. Must be called before any 3D drawing
 	 * function. TODO: List these functions
 	 */
 	@CindyScript("begin3d")
 	public void begin3d() {
 		cindy3d.begin();
 	}
 
 	/**
 	 * Finalizes the drawing of 3D objects. Displays all objects drawn since the
 	 * last call to <code>begin3d</code>.
 	 */
 	@CindyScript("end3d")
 	public void end3d() {
 		cindy3d.end();
 	}
 
 	/**
 	 * Draws a point in 3D space
 	 * @param vec Euclidean coordinates of the point
 	 */
 	@CindyScript("draw3d")
 	public void draw3d(ArrayList<Double> vec) {
 		if (vec.size() != 3)
 			return;
 
 		cindy3d.addPoint(vec.get(0), vec.get(1), vec.get(2), pointAppearance);
 	}
 
 	/**
 	 * Draws a line in 3D space
 	 * @param vec1 Euclidean coordinates of the first endpoint
	 * @param vec2 Euclidean coordinates of the second endpoint
 	 */
 	@CindyScript("draw3d")
 	public void draw3d(ArrayList<Double> vec1, ArrayList<Double> vec2) {
 		if (vec1.size() != 3 || vec2.size() != 3)
 			return;
 		
 		// Fill in default modifiers
 		Hashtable<String, Object> modifiers = new Hashtable<String, Object>();
 		modifiers.put("type", "Segment");
 		
 		// Apply overrides
 		modifiers.putAll(this.modifiers);
 		
 		String type = modifiers.get("type").toString();
 		
 		if (type.equals("Segment")) {
 			cindy3d.addSegment(vec1.get(0), vec1.get(1), vec1.get(2),
 					vec2.get(0), vec2.get(1), vec2.get(2), lineAppearance);
 		} else if (type.equals("Line")) {
 			cindy3d.addLine(vec1.get(0), vec1.get(1), vec1.get(2),
 					vec2.get(0), vec2.get(1), vec2.get(2), lineAppearance);
 		} else if (type.equals("Ray")) {
 			cindy3d.addRay(vec1.get(0), vec1.get(1), vec1.get(2),
 					vec2.get(0), vec2.get(1), vec2.get(2), lineAppearance);
 		}
 	}
 	
 	/**
 	 * Connects the given list of points by line segments
 	 * @param points List of points
 	 */
 	@CindyScript("connect3d")
 	public void connect3d(ArrayList<Vec> points) {
 		Vec last = points.get(0);
 		for (int i = 1; i < points.size(); ++i) {
 			Vec current = points.get(i);
 			cindy3d.addSegment(last.getXR(), last.getYR(), last.getZR(),
 					current.getXR(), current.getYR(), current.getZR(),
 					lineAppearance);
 			last = current;
 		}
 	}
 	
 	/**
 	 * Draws a polygon
 	 * @param points Vertices of the polygon
 	 */
 	@CindyScript("drawpoly3d")
 	public void drawpoly3d(ArrayList<Vec> points) {
 		double vertices[][] = new double[points.size()][3];
 		for (int i = 0; i < points.size(); ++i) {
 			vertices[i][0] = points.get(i).getXR();
 			vertices[i][1] = points.get(i).getYR();
 			vertices[i][2] = points.get(i).getZR();
 		}
 		cindy3d.addPolygon(vertices, lineAppearance);
 	}
 	
 	/**
 	 * Pushes the current appearance on the appearance stack
 	 * @see JRealityPlugin#grestore3d()
 	 */
 	@CindyScript("gsave3d")
 	public void gsave3d() {
 		pointAppearanceStack.push(new AppearanceState(pointAppearance));
 		lineAppearanceStack.push(new AppearanceState(lineAppearance));
 	}
 
 	/**
 	 * Removes the top element of the appearance stack and replaces the current
 	 * appearance with it
 	 * @see JRealityPlugin#gsave3d()
 	 */
 	@CindyScript("grestore3d")
 	public void grestore3d() {
 		if (!pointAppearanceStack.isEmpty())
 			pointAppearance = pointAppearanceStack.pop();
 		if (!lineAppearanceStack.isEmpty())
 			lineAppearance = lineAppearanceStack.pop();
 	}
 
 	/**
 	 * Set color state
 	 * @param vec Color vector
 	 */
 	@CindyScript("color3d")
 	public void color3d(ArrayList<Double> vec) {
 		setColorState(pointAppearance, vec);
 		setColorState(lineAppearance, vec);
 	}
 
 	/**
 	 * Set size state
	 * @param size Size
 	 */
 	@CindyScript("size3d")
 	public void size3d(double size) {
 		if (size <= 0)
 			return;
 		pointAppearance.setSize(size);
 		lineAppearance.setSize(size);
 	}
 
 	/**
 	 * Set point color state
 	 * @param vec Color vector
 	 */
 	@CindyScript("pointcolor3d")
 	public void pointcolor3d(ArrayList<Double> vec) {
 		setColorState(pointAppearance, vec);
 	}
 
 	/**
 	 * Set point size state
 	 * @param size Point size
 	 */
 	@CindyScript("pointsize3d")
 	public void pointsize3d(double size) {
 		if (size <= 0)
 			return;
 		pointAppearance.setSize(size);
 	}
 
 	/**
 	 * Set line color state
 	 * @param vec Color vector
 	 */
 	@CindyScript("linecolor3d")
 	public void linecolor3d(ArrayList<Double> vec) {
 		setColorState(lineAppearance, vec);
 	}
 
 	/**
 	 * Set line size state
 	 * @param size Line size
 	 */
 	@CindyScript("linesize3d")
 	public void linesize3d(double size) {
 		if (size <= 0)
 			return;
 		lineAppearance.setSize(size);
 	}
 
 	protected void setColorState(AppearanceState appearance,
 			ArrayList<Double> vec) {
 		if (vec.size() != 3)
 			return;
 		appearance.setColor(new Color(
 				(float)Math.max(0, Math.min(1, vec.get(0))),
 				(float)Math.max(0, Math.min(1, vec.get(1))),
 				(float)Math.max(0, Math.min(1, vec.get(2)))));
 	}
 }
