 package org.bodytrack.client;
 
 import gwt.g2d.client.graphics.Surface;
 
 /**
  * A vertical axis for photos.
  *
  * <p>Logically, there is a scale, just as with any other
  * <tt>GraphAxis</tt>, and in this case the scale ranges from
  * <tt>INITIAL_MIN</tt> to <tt>INITIAL_MAX</tt>.  In this
  * axis, though, only part of the axis is drawn - this is the
  * portion of the axis, centered at <tt>PHOTO_CENTER_LOCATION</tt>
  * that takes up logical height <tt>PHOTO_HEIGHT</tt>.</p>
  */
 
 // TODO: FINISH DOCUMENTING
 
 public class PhotoGraphAxis extends GraphAxis {
 	private static final double INITIAL_MIN = 0.0;
 	private static final double INITIAL_MAX = 1.0;
 	private static final Basis VERTICAL_AXIS_BASIS = Basis.xRightYUp;
 
 	// We draw ticks to take up TICK_WIDTH_FACTOR, as a proportion
 	// of total width
 	private static final double TICK_WIDTH_FACTOR = 0.5;
 
 	/**
 	 * The logical Y-value at which the center of a photo should be
 	 * placed.  Note that a call to {@link #project2D(double)} is
 	 * required to get the actual value in pixels where the center
 	 * should be drawn.
 	 */
 	public static final double PHOTO_CENTER_LOCATION = 0.8;
 
 	/**
 	 * The logical height for a photo.  Note that calls to
 	 * {@link #project2D(double)} are required to get the actual
 	 * locations of the top and bottom of the image.  Also note
 	 * that this is for the original photo, not for an expanded
 	 * size photo that the user is mousing over.
 	 */
 	public static final double PHOTO_HEIGHT = 0.35;
 
 	public PhotoGraphAxis(String channelName, double width) {
 		super(channelName, INITIAL_MIN, INITIAL_MAX,
 			VERTICAL_AXIS_BASIS, width, false);
 	}
 
 	/**
 	 * Returns the Y-value at which the center of a photo should be
 	 * drawn.
 	 *
 	 * @return
 	 */
 	public double projectPhotoCenter() {
 		return projectY(PHOTO_CENTER_LOCATION);
 	}
 
 	/**
 	 * Returns the height of a photo in pixels.
 	 *
 	 * @return
 	 */
 	public double projectPhotoHeight() {
 		// Note that we count the stretched height of the axis as
 		// project(min) - project(max).  This is because the min actually
 		// has a greater Y-value than the max, since drawing starts from
 		// the top left corner
 		return PHOTO_HEIGHT * (projectY(INITIAL_MIN) - projectY(INITIAL_MAX));
 	}
 
 	private double projectY(double value) {
 		return project2D(value).getY();
 	}
 
 	@Override
 	public void paint(Surface surface) {
 		Canvas canvas = Canvas.buildCanvas(surface);
 
 		// Pick the color to use, based on highlighting status
 		if (isHighlighted())
 			canvas.getSurface().setStrokeStyle(HIGHLIGHTED_COLOR);
 		else
 			canvas.getSurface().setStrokeStyle(NORMAL_COLOR);
 
 		double x = project2D(INITIAL_MIN).getX();
 		double width = getWidth();
 
 		// Now figure out where the line should go, drawing ticks
 		// as we go along
 		double topValue = PHOTO_CENTER_LOCATION + PHOTO_HEIGHT / 2.0;
		double bottomValue = PHOTO_CENTER_LOCATION - PHOTO_HEIGHT / 2.0;

		// Don't draw anything at all if the line is out of bounds
		if (bottomValue > getMax() || topValue < getMin())
			return;

		// Allow ourselves to begin drawing
		canvas.beginPath();
		
 		if (topValue > getMax())
 			topValue = getMax();
 		else
 			drawTick(canvas, topValue, x, width);
 
 		if (bottomValue < getMin())
 			bottomValue = getMin();
 		else
 			drawTick(canvas, bottomValue, x, width);
 
 		// Get the values in pixels for where the line should go
 		double topY = projectY(topValue);
 		double bottomY = projectY(bottomValue);
 
 		// Now draw the vertical line
 		canvas.getRenderer().drawLineSegment(x, topY, x, bottomY);
 
 		// Actually render all our ticks and lines on the canvas
 		canvas.stroke();
 
 		// Clean up after ourselves
 		canvas.getSurface().setStrokeStyle(Canvas.DEFAULT_COLOR);
 	}
 
 	/**
 	 * Draws a single tick at the specified location.
 	 *
 	 * <p>Draws a tick of width {@code width * TICK_WIDTH_FACTOR}
 	 * with its left edge at the specified X-value, and at Y-value
	 * determined by {@code projectY(value)}.  If <tt>value</tt> is
	 * out of range, though (the range is determined by
	 * {@linkplain GraphAxis#getMin()} and {@linkplain GraphAxis#getMax()}),
	 * this method draws nothing.</p>
 	 *
 	 * <p>Note that this method does not call {@code canvas.beginPath()}
 	 * or {@code canvas.stroke()}.  It is up to a calling method to
 	 * call {@code canvas.beginPath()} before calling this method,
 	 * and to call {@code canvas.stroke()} after calling this method.</p>
 	 *
 	 * @param canvas
 	 * 		a {@link org.bodytrack.client.Canvas Canvas} on which the
 	 * 		tick will be drawn
 	 * @param value
 	 * 		the location in which to draw the tick, specified
 	 * 		not in pixels but in terms of the
 	 * 		(INITIAL_MIN_VALUE, INITIAL_MAX_VALUE) value
	 * 		scale this class keeps in place
 	 * @param x
 	 * 		the X-coordinate for the left edge of the tick
 	 * @param width
 	 * 		the width of this axis, <em>NOT</em> the width of
 	 * 		the tick
 	 */
 	private void drawTick(Canvas canvas, double value, double x,
 			double width) {
		// Don't draw anything that is out of bounds
 		if (getMin() > value || getMax() < value)
			return;
 
 		double y = projectY(value);
 
 		double tickWidth = TICK_WIDTH_FACTOR * width;
 		double xRight = x + tickWidth;
 
 		canvas.getRenderer().drawLineSegment(x, y, xRight, y);
 	}
 }
