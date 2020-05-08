 package org.bodytrack.client;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import gwt.g2d.client.graphics.Color;
 import gwt.g2d.client.graphics.DirectShapeRenderer;
 import gwt.g2d.client.graphics.Surface;
 
 /**
  * Wrapper for a G2D Surface/DirectShapeRenderer pair.
  *
  * <p>This is intended to collect many of the important methods of the
  * {@link gwt.g2d.client.graphics.Surface Surface} and its associated
  * {@link gwt.g2d.client.graphics.DirectShapeRenderer DirectShapeRenderer}
  * in one class.  Note that not all methods of the two classes are
  * represented here.  However, the important methods of the two classes are
  * wrapped here, and calls to {@link #getSurface()} and to
  * {@link #getRenderer()}, which simply return references to those two
  * objects, allow calls to the other methods.</p>
  *
  * <p>This class is instance-controlled for efficiency: under this
  * system, only one DirectShapeRenderer is created per Surface.</p>
  */
 public final class Canvas {
 	/*
 	 * All the following colors should have exactly the same values as
 	 * their CSS counterparts by the same (lowercase) name.
 	 */
 	public static final Color BLACK = new Color(0x00, 0x00, 0x00);
 	public static final Color DARK_GRAY = new Color(0xA9, 0xA9, 0xA9);
 	public static final Color GRAY = new Color(0x80, 0x80, 0x80);
 	public static final Color RED = new Color(0xFF, 0x00, 0x00);
 	public static final Color GREEN = new Color(0x00, 0x80, 0x00);
 	public static final Color BLUE = new Color(0x00, 0x00, 0xFF);
 	public static final Color YELLOW = new Color(0xFF, 0xFF, 0x00);
 
 	private static final Map<Color, String> colorsToNames =
 		new HashMap<Color, String>();
 
 	static {
 		colorsToNames.put(BLACK, "Black");
 		colorsToNames.put(DARK_GRAY, "DarkGray");
 		colorsToNames.put(GRAY, "Gray");
 		colorsToNames.put(RED, "Red");
 		colorsToNames.put(GREEN, "Green");
 		colorsToNames.put(BLUE, "Blue");
		colorsToNames.put(YELLOW, "yellow");
 	}
 
 	/**
 	 * The default color, which classes should set as the stroke color
 	 * if wishing to &quot;clean up after themselves&quot; when done
 	 * changing colors and drawing.
 	 */
 	public static final Color DEFAULT_COLOR = BLACK;
 
 	/**
 	 * The default alpha value, which classes should <em>always</em>
 	 * set as the alpha after changing alpha on a Canvas.
 	 */
 	public static final double DEFAULT_ALPHA = 1.0;
 
 	private Surface surface;
 	private DirectShapeRenderer renderer;
 
 	private static Map<Surface, Canvas> instances;
 
 	static {
 		instances = new HashMap<Surface, Canvas>();
 	}
 
 	/**
 	 * Converts color to a human-readable string if color is one
 	 * of the constants defined by this class, and converts color
 	 * to a color code otherwise.
 	 *
 	 * <p>Regardless, returns a string that can be used by a
 	 * browser as a color specification.</p>
 	 *
 	 * @param color
 	 * 		the Color object we want to examine
 	 * @return
 	 * 		a code representing color, hopefully in a human-readable
 	 * 		form
 	 */
 	public static String friendlyName(Color color) {
 		if (colorsToNames.containsKey(color))
 			return colorsToNames.get(color);
 
 		return color.getColorCode();
 	}
 
 	private Canvas() { }
 
 	/**
 	 * Factory method to create a new Canvas object.
 	 *
 	 * @param s
 	 * 		the {@link gwt.g2d.client.graphics.Surface Surface}
 	 * 		on which the new Canvas will draw
 	 * @return
 	 * 		a Canvas with a pointer to s and to an associated
 	 * 		{@link gwt.g2d.client.graphics.DirectShapeRenderer
 	 * 		DirectShapeRenderer}
 	 */
 	public static Canvas buildCanvas(Surface s) {
 		if (s == null)
 			throw new NullPointerException("Surface cannot be null");
 
 		// We have already made a Canvas that points to s
 		if (instances.containsKey(s))
 			return instances.get(s);
 
 		Canvas result = new Canvas();
 		result.surface = s;
 		result.renderer = new DirectShapeRenderer(result.surface);
 
 		return result;
 	}
 
 	/**
 	 * Returns the Surface passed in to this object's constructor.
 	 *
 	 * @return
 	 * 		the surface
 	 */
 	public Surface getSurface() {
 		return surface;
 	}
 
 	/**
 	 * Returns the DirectShapeRenderer derived from the Surface passed
 	 * in to this object's constructor.
 	 *
 	 * @return
 	 * 		the renderer
 	 */
 	public DirectShapeRenderer getRenderer() {
 		return renderer;
 	}
 
 	// --------------------------------------------------------------
 	// Copies of Surface methods
 	// --------------------------------------------------------------
 	// None here yet!
 
 	// --------------------------------------------------------------
 	// Copies of DirectShapeRenderer methods
 	// --------------------------------------------------------------
 
 	/**
 	 * Is exactly equivalent to a call to getRenderer().beginPath().
 	 *
 	 * @return
 	 * 		the DirectShapeRenderer used for the beginPath call
 	 */
 	public DirectShapeRenderer beginPath() {
 		return renderer.beginPath();
 	}
 
 	/**
 	 * Is exactly equivalent to a call to getRenderer().stroke().
 	 *
 	 * @return
 	 * 		the DirectShapeRenderer used for the stroke call
 	 */
 	public DirectShapeRenderer stroke() {
 		return renderer.stroke();
 	}
 }
