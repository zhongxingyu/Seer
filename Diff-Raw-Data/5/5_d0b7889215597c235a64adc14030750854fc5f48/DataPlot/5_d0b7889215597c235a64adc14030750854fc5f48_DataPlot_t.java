 package org.bodytrack.client;
 
 import gwt.g2d.client.graphics.Color;
 import gwt.g2d.client.math.Vector2;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Represents a single set of data, along with references to its
  * associated axes.
  *
  * <p>Has the ability to draw itself and its axes on a
  * {@link org.bodytrack.client.Canvas Canvas} object, and to update
  * the positions of its dots based on the zoom level.  Also, if the
  * zoom level or position of the X-axis changes enough, this class will
  * automatically fetch the data from the server via Ajax and redraw
  * the data whenever it comes in from the server.</p>
  *
  * <p>A class that wishes to inherit this class can override
  * {@link DataPlot#paintAllDataPoints()}, but the easiest way to modify
  * functionality it to override {@link DataPlot#paintDataPoint()} and
  * {@link DataPlot#paintEdgePoint(double, double)}.  These two functions
  * are responsible for painting a single point on this DataPlot.
  * This (parent) class will automatically handle highlighting, zooming,
  * and the Ajax calls for pulling extra data from the server.</p>
  *
  * <p>A classes that wishes to inherit this class may also wish to
  * override {@link DataPlot#getDataPoints(GrapherTile)}, which
  * determines the points that {@link DataPlot#paintAllDataPoints()}
  * will draw, and the order in which paintAllDataPoints will draw
  * them.</p>
  */
 public class DataPlot {
 	// These two constants are used when highlighting this plot
 	protected static final int NORMAL_STROKE_WIDTH = 1;
 	protected static final int HIGHLIGHT_STROKE_WIDTH = 3;
 
 	/**
 	 * The radius to use when drawing a dot on the grapher.
 	 */
 	protected static final double DOT_RADIUS = 0.5;
 	
 	/**
 	 * The maximum size we allow currentData to be before we consider
 	 * pruning away unnecessary data.
 	 */
 	private static final int MAX_CURRENT_DATA_SIZE = 1024;
 
 	/**
 	 * Never render a point with value less than this - use anything
 	 * less as a sentinel for &quot;data point not present&quot;.
 	 *
 	 * <p>This value is intended to be used by subclasses as a sentinel
 	 * value.</p>
 	 */
 	protected static final double MIN_DRAWABLE_VALUE = -1e300;
 
 	private GraphWidget container;
 	private GraphAxis xAxis;
 	private GraphAxis yAxis;
 	private Canvas canvas;
 
 	private final int minLevel;
 	private final Color color;
 
 	private boolean shouldZoomIn;
 
 	// Values related to getting new values from the server
 	private String baseUrl;
 	private List<GrapherTile> currentData;
 	private Set<TileDescription> pendingDescriptions;
 	private List<GrapherTile> pendingData;
 
 	// Determining whether or not we should retrieve more data from
 	// the server
 	private int currentLevel;
 	private int currentMinOffset;
 	private int currentMaxOffset;
 
 	// Points to highlight in future invocations of paint
 	private boolean highlighted;
 
 	/**
 	 * Constructor for the DataPlot object that allows unlimited zoom.
 	 * 
 	 * The parameter url is the trickiest to get right.  This parameter
 	 * should be the <strong>beginning</strong> (the text up to, but
 	 * not including, the &lt;level&gt;.&lt;offset&gt;.json part of the
 	 * URL to fetch) of the URL which will be used to get more data.
 	 * Note that this <strong>must</strong> be a trusted BodyTrack
 	 * URL.  As described in the documentation for
 	 * {@link org.bodytrack.client.GrapherTile#retrieveTile(String, List)
 	 *  GrapherTile.retriveTile()}, an untrusted connection could allow
 	 * unauthorized access to all of a user's data.
 	 *
 	 * @param container
 	 * 		the {@link org.bodytrack.client.GraphWidget GraphWidget} on
 	 * 		which this DataPlot will draw itself and its axes
 	 * @param xAxis
 	 * 		the X-axis along which this data set will be aligned when
 	 * 		drawn.  Usually this is a
 	 * 		{@link org.bodytrack.client.TimeGraphAxis TimeGraphAxis}
 	 * @param yAxis
 	 * 		the Y-axis along which this data set will be aligned when
 	 * 		drawn
 	 * @param url
 	 * 		the beginning of the URL for fetching this data with Ajax
 	 * 		calls
 	 * @throws NullPointerException
 	 * 		if container, xAxis, yAxis, or url is <tt>null</tt>
 	 */
 	public DataPlot(GraphWidget container, GraphAxis xAxis, GraphAxis yAxis,
 			String url) {
 		this(container, xAxis, yAxis, url, Integer.MIN_VALUE, Canvas.BLACK);
 	}
 
 	/**
 	 * Main constructor for the DataPlot object.
 	 *
 	 * The parameter url is the trickiest to get right.  This parameter
 	 * should be the <strong>beginning</strong> (the text up to, but
 	 * not including, the &lt;level&gt;.&lt;offset&gt;.json part of the
 	 * URL to fetch) of the URL which will be used to get more data.
 	 * Note that this <strong>must</strong> be a trusted BodyTrack
 	 * URL.  As described in the documentation for
 	 * {@link org.bodytrack.client.GrapherTile#retrieveTile(String, List)
 	 *  GrapherTile.retriveTile()}, an untrusted connection could allow
 	 * unauthorized access to all of a user's data.
 	 *
 	 * @param container
 	 * 		the {@link org.bodytrack.client.GraphWidget GraphWidget} on
 	 * 		which this DataPlot will draw itself and its axes
 	 * @param xAxis
 	 * 		the X-axis along which this data set will be aligned when
 	 * 		drawn.  Usually this is a
 	 * 		{@link org.bodytrack.client.TimeGraphAxis TimeGraphAxis}
 	 * @param yAxis
 	 * 		the Y-axis along which this data set will be aligned when
 	 * 		drawn
 	 * @param url
 	 * 		the beginning of the URL for fetching this data with Ajax
 	 * 		calls
 	 * @param minLevel
 	 * 		the minimum level to which the user will be allowed to zoom
 	 * @param color
 	 * 		the color in which to draw these data points (note that
 	 * 		the axes are always drawn in black)
 	 * @throws NullPointerException
 	 * 		if container, xAxis, yAxis, url, or color is <tt>null</tt>
 	 */
 	public DataPlot(GraphWidget container, GraphAxis xAxis, GraphAxis yAxis,
 			String url, int minLevel, Color color) {
 		if (container == null || xAxis == null
 				|| yAxis == null || url == null || color == null)
 			throw new NullPointerException(
 				"Cannot have a null container, axis, url, or color");
 
 		this.container = container;
 		this.xAxis = xAxis;
 		this.yAxis = yAxis;
 		baseUrl = url;
 		shouldZoomIn = true;
 		this.minLevel = minLevel;
 
 		this.color = color;
 
 		canvas = Canvas.buildCanvas(this.container);
 
 		// The data will be pulled in with the checkForFetch call
 		pendingData = new ArrayList<GrapherTile>();
 		pendingDescriptions = new HashSet<TileDescription>();
 		currentData = new ArrayList<GrapherTile>();
 
 		currentLevel = Integer.MIN_VALUE;
 		currentMinOffset = Integer.MAX_VALUE;
 		currentMaxOffset = Integer.MIN_VALUE;
 
 		highlighted = false;
 
 		shouldZoomIn = checkForFetch();
 	}
 
 	/**
 	 * Returns <tt>true</tt> if and only if the X-axis is allowed to
 	 * zoom in farther, based on the zoom policy of this DataPlot.
 	 *
 	 * @return
 	 * 		<tt>true</tt> if the X-axis should be allowed to
 	 * 		zoom in more, <tt>false</tt> otherwise
 	 */
 	public boolean shouldZoomIn() {
 		return shouldZoomIn;
 	}
 
 	/**
 	 * Gives subclasses a reference to the
 	 * {@link org.bodytrack.client.Canvas Canvas} object on which they
 	 * can draw.
 	 *
 	 * @return
 	 * 		the Canvas on which this DataPlot draws
 	 */
 	protected Canvas getCanvas() {
 		return canvas;
 	}
 
 	/**
 	 * Checks for and performs a fetch for data from the server if
 	 * necessary.
 	 *
 	 * @return
 	 * 		<tt>true</tt> if the user should be allowed to zoom past
 	 * 		this point, <tt>false</tt> if the user shouldn't be allowed
 	 * 		to zoom past this point
 	 */
 	private boolean checkForFetch() {
 		int correctLevel = computeCurrentLevel();
 		int correctMinOffset = computeMinOffset(correctLevel);
 		int correctMaxOffset = computeMaxOffset(correctLevel);
 
 		if (correctLevel != currentLevel) {
 			for (int i = correctMinOffset; i <= correctMaxOffset; i++)
 				fetchFromServer(correctLevel, i);
 		} else if (correctMinOffset < currentMinOffset)
 			fetchFromServer(correctLevel, correctMinOffset);
 		else if (correctMaxOffset > currentMaxOffset)
 			fetchFromServer(correctLevel, correctMaxOffset);
 
 		// This way we don't fetch the same data multiple times
 		currentLevel = correctLevel;
 		currentMinOffset = correctMinOffset;
 		currentMaxOffset = correctMaxOffset;
 
 		// Remove for any data out of range, but only if we are not
 		// waiting on the data that is in range and we do not have
 		// enough available space to store our data
 		// TODO: Put more intelligent pruning in place
 		if (pendingDescriptions.size() > 0
 				|| currentData.size() < MAX_CURRENT_DATA_SIZE)
 			return correctLevel > minLevel;
 
 		/*
 		Iterator<GrapherTile> it = currentData.iterator();
 
 		while (it.hasNext()) {
 			GrapherTile curr = it.next();
 
 			int level = curr.getLevel();
 			int offset = curr.getOffset();
 
 			if (level != currentLevel) // Wrong level
 				it.remove();
 			else if (offset < currentMinOffset - 1) // Too far left
 				it.remove();
 			else if (offset > currentMaxOffset + 1) // Too far right
 				it.remove();
 		}
 		*/
 
 		return correctLevel > minLevel;
 	}
 
 	/**
 	 * Fetches the specified tile from the server.
 	 *
 	 * Note that this checks the pendingDescriptions instance variable
 	 * to determine if this tile has already been requested.  If so,
 	 * does not request anything from the server.
 	 *
 	 * @param level
 	 * 		the level of the tile to fetch
 	 * @param offset
 	 * 		the offset of the tile to fetch
 	 */
 	private void fetchFromServer(int level, int offset) {
 		TileDescription desc = new TileDescription(level, offset);
 
 		// Ensures we don't fetch the same tile twice unnecessarily
 		if (pendingDescriptions.contains(desc))
 			return;
 
 		String url = baseUrl + level + "." + offset + ".json";
 		GrapherTile.retrieveTile(url, pendingData);
 
 		// Make sure we don't fetch this again unnecessarily
 		pendingDescriptions.add(desc);
 	}
 
 	/**
 	 * Paints this DataPlot on the stored GraphWidget.
 	 *
 	 * <p>Does not draw the axes associated with this DataPlot.</p>
 	 *
 	 * <p>Note that it is <strong>not</strong> recommended that a subclass
 	 * override this method.  Instead, it is recommended that a subclass
 	 * override the {@link #paintAllDataPoints()} method.</p>
 	 */
 	public void paint() {
 		// If we have received data from the server
 		if (pendingData.size() > 0) {
 			// Pull all the data out of the tile
 			for (GrapherTile tile: pendingData) {
 				if (tile == null)
 					continue;
 
 				currentData.add(tile);
 
 				// Make sure we don't still mark this as pending
 				pendingDescriptions.remove(new TileDescription(
 					tile.getLevel(), tile.getOffset()));
 			}
 
 			pendingData.clear();
 		}
 
 		// Draw data points
 		canvas.getSurface().setStrokeStyle(color);
 		canvas.getSurface().setLineWidth(highlighted
 			? HIGHLIGHT_STROKE_WIDTH : NORMAL_STROKE_WIDTH);
 
 		paintAllDataPoints();
 
 		// Clean up after ourselves
 		canvas.getSurface().setStrokeStyle(Canvas.DEFAULT_COLOR);
 		canvas.getSurface().setLineWidth(NORMAL_STROKE_WIDTH);
 
 		// Make sure we shouldn't get any more info from the server
 		shouldZoomIn = checkForFetch();
 	}
 
 	/**
 	 * Renders all the salient data points in currentData.
 	 */
 	protected void paintAllDataPoints() {
 		// TODO: improve the algorithm for getting the best resolution tile
 		// Current algorithm is O(n m), where n is currentData.length()
 		// and m is getBestResolutionTiles.length()
 		// Could use a cache for the best resolution tiles, but would
 		// have to be careful to drop the cache if we pan or zoom too much,
 		// and definitely if we pull in more data
 
 		for (GrapherTile tile: getBestResolutionTiles()) {
 			double prevX = - Double.MAX_VALUE;
 			double prevY = - Double.MAX_VALUE;
 
 			List<PlottablePoint> dataPoints = getDataPoints(tile);
 
 			if (dataPoints == null)
 				continue;
 
 			canvas.beginPath();
 
 			for (PlottablePoint point: dataPoints) {
 				// Don't draw points too far to the left or right
 				if (point.getDate() < xAxis.getMin()
 						|| point.getDate() > xAxis.getMax()) {
 					// Make sure we don't draw lines between points
 					// that aren't adjacent
 					prevX = prevY = - Double.MAX_VALUE;
 					continue;
 				}
 
 				// Don't draw points too high or low to be rendered
 				if (point.getValue() < yAxis.getMin()
 						|| point.getValue() > yAxis.getMax()) {
 					// Make sure we don't draw lines between points
 					// that aren't adjacent
 					prevX = prevY = - Double.MAX_VALUE;
 					continue;
 				}
 
 				double x = xAxis.project2D(point.getDate()).getX();
 				double y = yAxis.project2D(point.getValue()).getY();
 
 				// Draw this part of the line
 				if (prevX > MIN_DRAWABLE_VALUE && prevY > MIN_DRAWABLE_VALUE)
 					paintDataPoint(prevX, prevY, x, y);
 				else
 					paintEdgePoint(x, y);
 
 				prevX = x;
 				prevY = y;
 			}
 
 			canvas.stroke();
 		}
 	}
 
 	/**
 	 * Paints a left edge point for a segment of the plot.
 	 *
 	 * This is only called for the left edge of a plot segment.  This
 	 * particular implementation draws a small dot.
 	 *
 	 * @param x
 	 * 		the X-coordinate of the point to draw
 	 * @param y
 	 * 		the Y-coordinate of the point to draw
 	 */
 	protected void paintEdgePoint(double x, double y) {
 		canvas.getRenderer().drawCircle(x, y, DOT_RADIUS);
 	}
 
 	/**
 	 * Returns the ordered list of points this DataPlot should draw
 	 * in {@link #paintAllDataPoints()}.
 	 *
 	 * It is acceptable, and not considered an error, if this or a subclass
 	 * implementation returns <tt>null</tt>.  Such a return should simply
 	 * be taken as a sign that the specified tile contains no data points
 	 * that paintAllDataPoints should draw.
 	 *
 	 * @param tile
 	 * 		the {@link org.bodytrack.client.GrapherTile GrapherTile}
 	 * 		from which to pull the data points
 	 * @return
 	 * 		a list of
 	 * 		{@link org.bodytrack.client.PlottablePoint PlottablePoint}
 	 * 		objects to be drawn by paintAllDataPoints
 	 */
 	protected List<PlottablePoint> getDataPoints(GrapherTile tile) {
 		return tile.getDataPoints();
 	}
 
 	/**
 	 * Draws a single data point on the graph.
 	 *
 	 * Note that this method has as preconditions that
 	 * {@code prevX < x} and that {@code prevY < y}.
 	 *
 	 * @param prevX
 	 * 		the previous X-value, which will be greater than
 	 * 		MIN_DRAWABLE_VALUE
 	 * @param prevY
 	 * 		the previous Y-value, which will be greater than
 	 * 		MIN_DRAWABLE_VALUE
 	 * @param x
 	 * 		the current X-value, which will be greater than
 	 * 		MIN_DRAWABLE_VALUE
 	 * @param y
 	 * 		the current Y-value, which will be greater than
 	 * 		MIN_DRAWABLE_VALUE
 	 * @see #MIN_DRAWABLE_VALUE
 	 */
 	protected void paintDataPoint(double prevX, double prevY,
 			double x, double y) {
 		canvas.getRenderer().drawLineSegment(prevX, prevY, x, y);
 	}
 
 	/**
 	 * Returns a sorted list of all best resolution tiles available.
 	 *
 	 * @return
 	 * 		a sorted list of all the best resolution tiles in
 	 * 		currentData
 	 */
 	private List<GrapherTile> getBestResolutionTiles() {
 		List<GrapherTile> best = new ArrayList<GrapherTile>();
 
 		// When minTime and maxTime are used in calculations, they are
 		// used to make the calculations scale-independent
 		double minTime = xAxis.getMin();
 		double maxTime = xAxis.getMax();
 
 		double maxCoveredTime = minTime;
 
 		int bestLevel = computeCurrentLevel();
 
 		while (maxCoveredTime <= maxTime) {
 			GrapherTile bestAtCurrTime = getBestResolutionTileAt(
 				maxCoveredTime + (maxTime - minTime) * 1e-6,
 				bestLevel);
 			// We need to move a little to the right of the current time
 			// so we don't get the same tile twice
 
 			if (bestAtCurrTime == null) {
 				maxCoveredTime += (maxTime - minTime) * 1e-3;
 			} else {
 				best.add(bestAtCurrTime);
 
 				maxCoveredTime =
 					bestAtCurrTime.getDescription().getMaxTime();
 			}
 		}
 
 		return best;
 	}
 
 	/**
 	 * Returns the best-resolution tile that covers the specified
 	 * point.
 	 *
 	 * @param time
 	 * 		the time which must be covered by the tile
 	 * @param bestLevel
 	 * 		the level to which we want the returned tile to be close
 	 * @return
 	 * 		the best-resolution (lowest-level) tile which has min value
 	 * 		less than or equal to time, and max value greater than or
 	 * 		equal to time, or <tt>null</tt> if no such tile exists
 	 */
 	private GrapherTile getBestResolutionTileAt(double time, int bestLevel) {
 		GrapherTile best = null;
 
 		for (GrapherTile tile: currentData) {
 			TileDescription desc = tile.getDescription();
 
 			if (desc.getMinTime() > time || desc.getMaxTime() < time)
 				continue;
 
 			if (best == null)
 				best = tile;
 			else if(Math.abs(desc.getLevel() - bestLevel) <
 					Math.abs(best.getLevel() - bestLevel))
 				best = tile;
 			else if (Math.abs(desc.getLevel() - bestLevel) ==
 					Math.abs(best.getLevel() - bestLevel)) {
 				if (desc.getLevel() < best.getLevel())
 					best = tile;
 			}
 		}
 
 		return best;
 	}
 
 	/**
 	 * Returns the X-Axis for this DataPlot.
 	 *
 	 * @return
 	 * 		the X-axis for this DataPlot
 	 */
 	public GraphAxis getXAxis() {
 		return xAxis;
 	}
 
 	/**
 	 * Returns the Y-Axis for this DataPlot.
 	 *
 	 * @return
 	 * 		the Y-axis for this DataPlot
 	 */
 	public GraphAxis getYAxis() {
 		return yAxis;
 	}
 
 	/**
 	 * Computes the value for currentLevel based on xAxis.
 	 *
 	 * @return
 	 * 		the level at which xAxis is operating
 	 */
 	private int computeCurrentLevel() {
 		double xAxisWidth = xAxis.getMax() - xAxis.getMin();
 		double dataPointWidth = xAxisWidth / GrapherTile.TILE_WIDTH;
 
 		return log2(dataPointWidth);
 	}
 
 	/**
 	 * Computes the floor of the log (base 2) of n.
 	 *
 	 * @param n
 	 * 		the value for which we want to take the log
 	 * @return
 	 * 		the floor of the log (base 2) of n
 	 */
 	private int log2(double x) {
 		if (x == 0)
 			return 0;
 
 		if (x < 0)
 			return -1 * log2(-x);
 
 		return (int) (Math.log(x) / Math.log(2));
 	}
 
 	/**
 	 * Returns the offset at which the left edge of the X-axis is operating.
 	 *
 	 * Returns the offset of the tile in which the minimum value
 	 * of the X-axis is found.
 	 *
 	 * @param level
 	 * 		the level at which we assume we are operating when calculating
 	 * 		offsets
 	 * @return
 	 * 		the current offset of the X-axis, based on level
 	 * 		and the private variable xAxis
 	 */
 	private int computeMinOffset(int level) {
 		double min = xAxis.getMin();
 
 		double tileWidth = getTileWidth(level);
 
 		// Tile offset computation
 		return (int) (min / tileWidth);
 	}
 
 	/**
 	 * Returns the offset at which the right edge of the X-axis is operating.
 	 *
 	 * Returns the offset of the tile in which the maximum value
 	 * of the X-axis is found.
 	 *
 	 * @param level
 	 * 		the level at which we assume we are operating when calculating
 	 * 		offsets
 	 * @return
 	 * 		the current offset of the X-axis, based on level
 	 * 		and the private variable xAxis
 	 */
 	private int computeMaxOffset(int level) {
 		double max = xAxis.getMax();
 
 		double tileWidth = getTileWidth(level);
 
 		// Tile number computation
 		return (int) (max / tileWidth);
 	}
 
 	/**
 	 * Returns the width of a single tile.
 	 *
 	 * @param level
 	 * 		the level of the tile for which we will find the width
 	 * @return
 	 * 		the width of a tile at the given level
 	 */
 	private double getTileWidth(int level) {
 		return (new TileDescription(level, 0)).getTileWidth();
 	}
 
 	/**
 	 * Returns a PlottablePoint if and only if there is a point, part of
 	 * this DataPlot, within threshold pixels of pos.  Otherwise, returns
 	 * <tt>null</tt>.
 	 *
 	 * This actually builds a square of 2 * threshold pixels on each
 	 * side, centered at pos, and checks if there is a data point within
 	 * that square, but that is a minor detail that should not affect
 	 * the workings of this method.
 	 *
 	 * @param pos
 	 *		the mouse position from which to check proximity to a data
 	 *		point
 	 * @param threshold
 	 * 		the maximum distance pos can be from a data point to be
 	 * 		considered &quot;near&quot; to it
 	 * @return
 	 * 		<tt>null</tt> if there is no point within threshold pixels
 	 * 		of pos, or one of the points, if there is such a point
 	 * @throws IllegalArgumentException
 	 * 		if threshold is negative
 	 */
 	public PlottablePoint closest(Vector2 pos, double threshold) {
 		if (threshold < 0)
 			throw new IllegalArgumentException(
 				"Cannot work with a negative distance");
 
 		double x = pos.getX();
 		double y = pos.getY();
 
 		// Build a square for checking location
 		Vector2 topLeft = new Vector2(x - threshold, y - threshold);
 		Vector2 bottomRight = new Vector2(x + threshold, y + threshold);
 
 		// Now convert that square into a square of times and values
 		double minTime = xAxis.unproject(topLeft);
 		double maxTime = xAxis.unproject(bottomRight);
 		double minValue = yAxis.unproject(bottomRight);
 		double maxValue = yAxis.unproject(topLeft);
 
 		double centerTime = xAxis.unproject(pos);
 		double centerValue = xAxis.unproject(pos);
 
 		// Get the tiles to check
 		int correctLevel = computeCurrentLevel();
 
 		GrapherTile bestTileMinTime =
 			getBestResolutionTileAt(minTime, correctLevel);
 		GrapherTile bestTileMaxTime =
 			getBestResolutionTileAt(maxTime, correctLevel);
 
 		PlottablePoint closest = getClosestPoint(bestTileMinTime,
 			minTime, maxTime, minValue, maxValue, centerTime,
 			centerValue);
 
 		// pos is right on the border between two tiles
 		if (bestTileMinTime != bestTileMaxTime) {
 			// This is unlikely but possible, especially if threshold
 			// is large
 
 			PlottablePoint closestMaxTime = getClosestPoint(
 				bestTileMaxTime, minTime, maxTime, minValue,
 				maxValue, centerTime, centerValue);
 
 			double distClosestSq = getDistanceSquared(closest,
 				centerTime, centerValue);
			double distClosestMaxTimeSq =
				getDistanceSquared(closestMaxTime, centerTime,
				centerValue);
 
 			if (distClosestMaxTimeSq < distClosestSq)
 				return closestMaxTime;
 		}
 
 		return closest;
 	}
 
 	/**
 	 * Helper method for {@link DataPlot#closest(Vector2, double)}.
 	 *
 	 * This method has a lot of similar parameters, which is normally
 	 * poor style, but it is an internal helper method, so this is
 	 * OK.
 	 *
 	 * @param tile
 	 * 		the {@link org.bodytrack.client.GrapherTile GrapherTile}
 	 * 		in which to search for the closest point
 	 * @param minTime
 	 * 		the minimum time at which we consider points
 	 * @param maxTime
 	 * 		the maximum time at which we consider points
 	 * @param minValue
 	 * 		the minimum value of a point for us to consider it
 	 * @param maxValue
 	 * 		the maximum value of a point at which we will consider it
 	 * @param centerTime
 	 * 		the time to which we will try to make our point close
 	 * @param centerValue
 	 * 		the value to which we will try to make our point close
 	 * @return
 	 * 		the point closest to (centerTime, centerValue)
 	 * 		in getDataPoints(tile), as long as that point is within the
 	 * 		square determined by (minTime, minValue) and
 	 * 		(maxTime, maxValue) and visible to the user.  If there is no
 	 * 		such point, returns <tt>null</tt>
 	 */
 	private PlottablePoint getClosestPoint(GrapherTile tile,
 			double minTime, double maxTime, double minValue,
 			double maxValue, double centerTime, double centerValue) {
 		if (tile == null)
 			return null;
 
 		PlottablePoint closest = null;
 		double shortestDistanceSq = Double.MAX_VALUE;
 
 		for (PlottablePoint point: getDataPoints(tile)) {
 			double time = point.getDate();
 			double val = point.getValue();
 
 			// Only check for proximity to points we can see
 			if (time < xAxis.getMin() || time > xAxis.getMax())
 				continue;
 
 			// Only check for proximity to points within the desired
 			// range
 			if (time >= minTime && time <= maxTime
 				&& val >= minValue && val <= maxValue) {
 
 				// If we don't have a value for closest, any point
 				// in the specified range is closer
 				if (closest == null) {
 					closest = point;
 					continue;
 				}
 
 				// Compute the square of the distance to pos
 				double distanceSq = getDistanceSquared(point,
 					centerTime, centerValue);
 
 				if (distanceSq < shortestDistanceSq) {
 					closest = point;
 					shortestDistanceSq = distanceSq;
 				}
 			}
 		}
 
 		return closest;
 	}
 
 	/**
 	 * Returns the square of the distance from point to (time, value).
 	 *
 	 * @param point
 	 * 		the first of the two points
 	 * @param time
 	 * 		the time for the second point
 	 * @param value
 	 * 		the distance for the second point
 	 * @return
 	 * 		the square of the distance from point to (time, value), or
 	 * 		{@link Double#MAX_VALUE} if point is <tt>null</tt>
 	 */
 	private double getDistanceSquared(PlottablePoint point,
 			double time, double value) {
 		if (point == null)
 			return Double.MAX_VALUE;
 
 		double pointTime = point.getDate();
 		double pointValue = point.getValue();
 
 		return (time - pointTime) * (time - pointTime)
 			+ (value - pointValue) * (value - pointValue);
 	}
 
 	/**
 	 * Highlights this DataPlot in future
 	 * {@link DataPlot#paint() paint} calls.
 	 *
 	 * Note that this does not highlight the axes associated with this
 	 * DataPlot.
 	 */
 	public void highlight() {
 		highlighted = true;
 	}
 
 	/**
 	 * Stops highlighting this DataPlot.
 	 *
 	 * Note that this does not affect the highlighting status on the
 	 * axes associated with this DataPlot.
 	 */
 	public void unhighlight() {
 		highlighted = false;
 	}
 
 	/**
 	 * Tells whether or not this DataPlot is highlighted.
 	 *
 	 * If {@link #highlight()} has been called since the constructor
 	 * and since the last call to {@link #unhighlight()}, returns
 	 * <tt>true</tt>.  Otherwise, returns <tt>false</tt>.
 	 *
 	 * @return
 	 * 		<tt>true</tt> if and only if this DataPlot is highlighted
 	 */
 	public boolean isHighlighted() {
 		return highlighted;
 	}
 
 	/**
 	 * Highlights the appropriate points on the axes if and only if
 	 * this DataPlot contains a point within threshold pixels of pos.
 	 *
 	 * <p>This method is defined to act exactly as if it were
 	 * implemented as
 	 *
 	 * <pre>
 	 * public boolean highlightIfNear(Vector2 pos, double threshold) {
 	 * 	PlottablePoint point = DataPlot.this.closest(pos, threshold);
 	 * 	if (point != null)
 	 * 		DataPlot.this.highlight();
 	 * 	return point != null;
 	 * }
 	 * </pre>
 	 *
 	 * </p>
 	 *
 	 * <p>However, this method may be implemented in any way that
 	 * accomplishes the same aims as the code.  Note that, in particular,
 	 * this method is not required to call overridden forms of isNear and
 	 * highlightPoints.  However, this method is not a final method, so
 	 * a subclass implementation of this method may call an overridden
 	 * form of closest and highlight.</p>
 	 *
 	 * @param pos
 	 * 		the position at which the mouse is hovering, and from which
 	 * 		we want to derive our highlighting
 	 * @param threshold
 	 * 		the maximum distance the mouse can be from a point, while
 	 * 		still causing the highlighting effect
 	 * @return
 	 * 		<tt>true</tt> if and only if this highlights the axes
 	 * @throws IllegalArgumentException
 	 * 		if threshold is negative
 	 */
 	public boolean highlightIfNear(Vector2 pos, double threshold) {
 		PlottablePoint point = DataPlot.this.closest(pos, threshold);
 		if (point != null)
 			DataPlot.this.highlight();
 
 		return point != null;
 	}
 }
