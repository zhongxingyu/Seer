 package org.bodytrack.client;
 
 import gwt.g2d.client.graphics.Color;
 import gwt.g2d.client.math.Vector2;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.NumberFormat;
 
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
  * {@link DataPlot#paintEdgePoint(BoundedDrawingBox, double, double)}.
  * These two functions are responsible for painting a single point on
  * this DataPlot.  This (parent) class will automatically handle
  * highlighting, zooming, and the Ajax calls for pulling extra data
  * from the server.</p>
  *
  * <p>A classes that wishes to inherit this class may also wish to
  * override {@link DataPlot#getDataPoints(GrapherTile)}, which
  * determines the points that {@link DataPlot#paintAllDataPoints()}
  * will draw, and the order in which paintAllDataPoints will draw
  * them.</p>
  */
 public class DataPlot implements Alertable<GrapherTile> {
 	/**
 	 * The width at which a normal line is drawn.
 	 */
 	protected static final int NORMAL_STROKE_WIDTH = 1;
 
 	/**
 	 * The width at which a highlighted line is drawn.
 	 */
 	protected static final int HIGHLIGHT_STROKE_WIDTH = 3;
 
 	/**
 	 * Never render a point with value less than this - use anything
 	 * less as a sentinel for &quot;data point not present&quot;.
 	 *
 	 * <p>This value is intended to be used by subclasses as a sentinel
 	 * value.</p>
 	 */
 	protected static final double MIN_DRAWABLE_VALUE = -1e300;
 
 	/**
 	 * Whenever the {@link #highlight()} method is called, we don't know
 	 * which points on the axes should be highlighted, so we use this
 	 * value to indicate this.  As such, testing with == is OK as a test
 	 * for this point, since we set highlightedPoint to this exact
 	 * memory location whenever we don't know which point should be
 	 * highlighted.
 	 */
 	protected static final PlottablePoint HIGHLIGHTED_NO_SINGLE_POINT =
 		new PlottablePoint(Double.MIN_VALUE, 0.0);
 
 	/**
 	 * The radius to use when drawing a dot on the grapher.
 	 */
 	private static final double DOT_RADIUS = 0.5;
 
 	/**
 	 * The radius to use when drawing a highlighted dot on the grapher.
 	 */
 	private static final double HIGHLIGHTED_DOT_RADIUS = 4;
 
 	/**
 	 * We never re-request a URL with MAX_REQUESTS_PER_URL or more failures
 	 * in a row.
 	 */
 	private static final int MAX_REQUESTS_PER_URL = 5;
 
 	/**
 	 * Used to speed up the log2 method.
 	 */
 	private static final double LN_2 = Math.log(2);
 
 	private final GraphWidget container;
 	private final GraphAxis xAxis;
 	private final GraphAxis yAxis;
 	private final Canvas canvas;
 
 	private final String deviceName;
 	private final String channelName;
 
 	private final int minLevel;
 	private final Color color;
 
 	private boolean shouldZoomIn;
 
 	// Values related to getting new values from the server
 	private final String baseUrl;
 	private final List<GrapherTile> currentData;
 	private final Set<TileDescription> pendingDescriptions;
 	private final Map<String, Integer> pendingUrls;
 	private final List<GrapherTile> pendingData;
 
 	private final Map<String, List<Integer>> loadingUrls;
 
 	// Determining whether or not we should retrieve more data from
 	// the server
 	private int currentLevel;
 	private int currentMinOffset;
 	private int currentMaxOffset;
 
 	// If highlightedPoint is null, then this should not be highlighted.
 	// Otherwise, this is the point to highlight on the axes
 	private PlottablePoint highlightedPoint;
 
 	// If publishValueOnHighlight is true, we will show our data value
 	// as a decimal whenever this axis is highlighted, using the
 	// data value publishing API in GraphWidget.
 	private final boolean publishValueOnHighlight;
 
 	private int publishedValueId;
 
 	/**
 	 * Main constructor for the DataPlot object.
 	 *
 	 * <p>The parameter url is the trickiest to get right.  This parameter
 	 * should be the <strong>beginning</strong> (the text up to, but
 	 * not including, the &lt;level&gt;.&lt;offset&gt;.json part of the
 	 * URL to fetch) of the URL which will be used to get more data.
 	 * Note that this <strong>must</strong> be a trusted BodyTrack
 	 * URL.  As described in the documentation for
 	 * {@link org.bodytrack.client.GrapherTile#retrieveTile(String, List)
 	 *  GrapherTile.retriveTile()}, an untrusted connection could allow
 	 * unauthorized access to all of a user's data.</p>
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
 	 * @param deviceName
 	 * 		the name of the device from which this channel came
 	 * @param channelName
 	 * 		the name of the channel on the device specified by deviceName
 	 * @param url
 	 * 		the beginning of the URL for fetching this data with Ajax
 	 * 		calls
 	 * @param minLevel
 	 * 		the minimum level to which the user will be allowed to zoom
 	 * @param color
 	 * 		the color in which to draw these data points (note that
 	 * 		this does not affect the color of the axes)
 	 * @param publishValueOnHighlight
 	 * 		<tt>true</tt> to signify that, whenever a point is highlighted
 	 * 		on this <tt>DataPlot</tt>, the value should show up in the
 	 * 		corner of container
 	 * @throws NullPointerException
 	 * 		if container, xAxis, yAxis, deviceName, channelName, url,
 	 * 		or color is <tt>null</tt>
 	 */
 	public DataPlot(GraphWidget container, GraphAxis xAxis, GraphAxis yAxis,
 			String deviceName, String channelName, String url, int minLevel,
 			Color color, boolean publishValueOnHighlight) {
 		if (container == null || xAxis == null || yAxis == null
 				|| deviceName == null || channelName == null
 				|| url == null || color == null)
 			throw new NullPointerException("Cannot have a null container, "
 				+ "axis, device or channel name, url, or color");
 
 		this.container = container;
 		this.xAxis = xAxis;
 		this.yAxis = yAxis;
 		this.deviceName = deviceName;
 		this.channelName = channelName;
 		baseUrl = url;
 		shouldZoomIn = true;
 		this.minLevel = minLevel;
 
 		this.color = color;
 		this.publishValueOnHighlight = publishValueOnHighlight;
 
 		canvas = Canvas.buildCanvas(this.container);
 
 		// The data will be pulled in with the checkForFetch call
 		pendingData = new ArrayList<GrapherTile>();
 		pendingDescriptions = new HashSet<TileDescription>();
 		pendingUrls = new HashMap<String, Integer>();
 		currentData = new ArrayList<GrapherTile>();
 
 		loadingUrls = new HashMap<String, List<Integer>>();
 
 		currentLevel = Integer.MIN_VALUE;
 		currentMinOffset = Integer.MAX_VALUE;
 		currentMaxOffset = Integer.MIN_VALUE;
 
 		highlightedPoint = null;
 		publishedValueId = 0;
 
 		shouldZoomIn = checkForFetch();
 	}
 
 	/**
 	 * Puts together the base URL for a channel, based on the URL
 	 * specification for tiles.
 	 *
 	 * @param userId
 	 * 		the ID of the current user
 	 * @param deviceName
 	 * 		the device for the channel we want to pull tiles from
 	 * @param channelName
 	 * 		the channel name for the channel we want to pull tiles from
 	 * @return
 	 * 		a base URL that is suitable for passing to one of the
 	 * 		constructors for <tt>DataPlot</tt> or one of its subclasses
 	 * @throws NullPointerException
 	 * 		if deviceName or channelName is <tt>null</tt>
 	 */
 	public static String buildBaseUrl(int userId, String deviceName,
 			String channelName) {
 		if (deviceName == null || channelName == null)
 			throw new NullPointerException(
 				"Null part of base URL not allowed");
 
		if (deviceName.equals(""))
			return "/tiles/" + userId + "/" + channelName + "/";

		return "/tiles/" + userId + "/" + deviceName + "."
			+ channelName + "/";
 	}
 
 	/**
 	 * Returns the device name for this plot.
 	 *
 	 * @return
 	 * 		the device name passed to the constructor
 	 */
 	public String getDeviceName() {
 		return deviceName;
 	}
 
 	/**
 	 * Returns the channel name for this plot.
 	 *
 	 * @return
 	 * 		the channel name passed to the constructor
 	 */
 	public String getChannelName() {
 		return channelName;
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
 	 * 		the <tt>Canvas</tt> on which this <tt>DataPlot</tt> draws
 	 */
 	protected Canvas getCanvas() {
 		return canvas;
 	}
 
 	/**
 	 * Gives subclasses a reference to the
 	 * {@link org.bodytrack.client.GraphWidget GraphWidget} object that
 	 * they can use for the <tt>GraphWidget</tt> API.
 	 *
 	 * <p>Do <em>not</em> use this method's return value for direct
 	 * drawing - this is why the {@link #getCanvas()} method was created.
 	 * Instead, this method's return value should be used for other
 	 * calls that are specific to the <tt>GraphWidget</tt> class.</p>
 	 *
 	 * @return
 	 * 		the <tt>GraphWidget</tt> on which this <tt>DataPlot</tt>
 	 * 		draws
 	 */
 	protected GraphWidget getContainer() {
 		return container;
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
 
 		String url = getTileUrl(level, offset);
 		GrapherTile.retrieveTile(url, level, offset, pendingData, this);
 
 		// Tell the user we are looking for information
 		addLoadingText(level, offset, url);
 
 		// Make sure we don't fetch this again unnecessarily
 		pendingDescriptions.add(desc);
 		pendingUrls.put(url, 0);
 	}
 
 	/**
 	 * A method to generate the correct URL for a given tile.
 	 *
 	 * <p>This method is designed so that subclasses can override
 	 * it and change the default behavior of tile fetching with
 	 * little effort.  For instance, a photo data plot can modify the
 	 * &quot;tags&quot; and &quot;nsfw&quot; parameters in the request
 	 * to the server.</p>
 	 *
 	 * @param level
 	 * 		the level at which the tile should come
 	 * @param offset
 	 * 		the offset of the tile
 	 * @return
 	 * 		the URL to use to get the tile
 	 */
 	protected String getTileUrl(int level, int offset) {
 		return baseUrl + level + "." + offset + ".json";
 	}
 
 	/**
 	 * Adds the specified loading text to container.
 	 *
 	 * <p>This is one of two methods to handle the loading text feature
 	 * for DataPlot objects.  The other is
 	 * {@link #removeLoadingText(String)}.  This method will create
 	 * a loading text string and publish it to container.</p>
 	 *
 	 * @param level
 	 * 		the level of the tile that is loading
 	 * @param offset
 	 * 		the offset of the tile that is loading
 	 * @param url
 	 * 		the URL of the tile that is loading
 	 */
 	private void addLoadingText(int level, int offset, String url) {
 		String msg = "Loading " + url;
 
 		// Actually add the message
 		int id = container.addLoadingMessage(msg);
 
 		List<Integer> ids = loadingUrls.containsKey(url) ?
 			loadingUrls.remove(url) : new ArrayList<Integer>();
 		ids.add(id);
 		loadingUrls.put(url, ids);
 	}
 
 	/**
 	 * Removes the specified loading text from container.
 	 *
 	 * <p>This is one of two methods to handle the loading text feature
 	 * for DataPlot objects.  The other is
 	 * {@link #addLoadingText(int, int, String)}.  This method will
 	 * remove from container the loading text string associated
 	 * with url.  Thus, it is required that this take the same
 	 * URL that was passed to addLoadingText to create the message.</p>
 	 *
 	 * @param url
 	 * 		the URL of the tile that is finished loading
 	 */
 	private void removeLoadingText(String url) {
 		if (loadingUrls.containsKey(url)) {
 			// Always maintain the invariant that each value in
 			// loadingUrls has at least one element.  Since this
 			// is the place where things are removed from loadingUrls,
 			// and since no empty lists are added to loadingUrls,
 			// this method is responsible for maintaining the
 			// invariant.
 
 			// Note that we remove from loadingUrls
 			List<Integer> ids = loadingUrls.remove(url);
 			int id = ids.remove(0);
 
 			if (ids.size() > 0)
 				loadingUrls.put(url, ids);
 
 			container.removeLoadingMessage(id);
 		}
 		// Don't do anything if we don't have an ID with this URL
 	}
 
 	/**
 	 * Called every time a new tile loads.
 	 *
 	 * @param tile
 	 * 		the <tt>GrapherTile</tt> representing the tile that loaded
 	 */
 	@Override
 	public void onSuccess(GrapherTile tile) {
 		String url = tile.getUrl();
 
 		if (pendingUrls.containsKey(url))
 			pendingUrls.remove(url);
 
 		removeLoadingText(url);
 
 		// It is important to call container.paintTwice() rather than
 		// simply paint() here, since the loading text does
 		// not update unless container.paint() is called at least once
 		container.paintTwice();
 	}
 
 	/**
 	 * Called every time a tile load fails.
 	 *
 	 * <p>Tries to re-request the tile.</p>
 	 *
 	 * @param tile
 	 * 		the <tt>GrapherTile</tt> representing the tile that failed
 	 * 		to load
 	 */
 	@Override
 	public void onFailure(GrapherTile tile) {
 		String url = tile.getUrl();
 		int level = tile.getLevel();
 		int offset = tile.getOffset();
 
 		if (pendingUrls.containsKey(url)) {
 			int oldValue = pendingUrls.get(url);
 			if (oldValue > MAX_REQUESTS_PER_URL) {
 				// TODO: Log or alert user whenever we can't get
 				// a piece of data
 				// Perhaps use InfoPublisher API
 				removeLoadingText(url);
 
 				return;
 			}
 
 			pendingUrls.remove(url);
 			pendingUrls.put(url, oldValue + 1);
 		} else
 			pendingUrls.put(url, 1);
 
 		GrapherTile.retrieveTile(url, level, offset, pendingData, this);
 
 		// See the documentation in onSuccess() to see why
 		// container.paintTwice() is important
 		container.paintTwice();
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
 		checkForNewData();
 
 		// Draw data points
 		canvas.getSurface().setStrokeStyle(color);
 		canvas.getSurface().setLineWidth(isHighlighted()
 			? HIGHLIGHT_STROKE_WIDTH : NORMAL_STROKE_WIDTH);
 
 		BoundedDrawingBox drawing = getDrawingBounds();
 
 		paintAllDataPoints(drawing);
 
 		if (highlightedPoint != null
 				&& highlightedPoint != HIGHLIGHTED_NO_SINGLE_POINT) {
 			drawing.beginClippedPath();
 			paintHighlightedPoint(drawing, highlightedPoint);
 			drawing.strokeClippedPath();
 		}
 
 		// Clean up after ourselves
 		canvas.getSurface().setStrokeStyle(Canvas.DEFAULT_COLOR);
 		canvas.getSurface().setLineWidth(NORMAL_STROKE_WIDTH);
 
 		// Make sure we shouldn't get any more info from the server
 		shouldZoomIn = checkForFetch();
 	}
 
 	/**
 	 * Builds and returns a new {@link org.bodytrack.client.BoundedDrawingBox
 	 * BoundedDrawingBox} that constrains drawing to the viewing window.
 	 *
 	 * @return
 	 * 		a <tt>BoundedDrawingBox</tt> that will only allow drawing
 	 * 		within the axes
 	 */
 	private BoundedDrawingBox getDrawingBounds() {
 		double minX = xAxis.project2D(xAxis.getMin()).getX();
 		double maxX = xAxis.project2D(xAxis.getMax()).getX();
 
 		// Although minY and maxY appear to be switched, this is actually
 		// the correct way to define these variables, since we draw the
 		// Y-axis from bottom to top but pixel values increase from top
 		// to bottom.  Thus, the max Y-value is associated with the min
 		// axis value, and vice versa.
 		double minY = yAxis.project2D(yAxis.getMax()).getY();
 		double maxY = yAxis.project2D(yAxis.getMin()).getY();
 
 		return new BoundedDrawingBox(canvas, minX, minY, maxX, maxY);
 	}
 
 	/**
 	 * Checks to see if we have received data from the server
 	 */
 	private void checkForNewData() {
 		if (pendingData.size() > 0) {
 			// Pull all the data out of pendingData
 			for (GrapherTile tile: pendingData) {
 				if (tile == null)
 					continue;
 
 				currentData.add(tile);
 
 				// Make sure we don't still mark this as pending
 				pendingDescriptions.remove(tile.getDescription());
 			}
 
 			pendingData.clear();
 		}
 	}
 
 	/**
 	 * Renders all the salient data points in currentData.
 	 *
 	 * @param drawing
 	 * 		the {@link org.bodytrack.client.BoundedDrawingBox
 	 * 		BoundedDrawingBox} in which all points should be
 	 * 		drawn
 	 */
 	protected void paintAllDataPoints(BoundedDrawingBox drawing) {
 		// TODO: improve the algorithm for getting the best resolution tile
 		// Current algorithm is O(n m), where n is currentData.length()
 		// and m is getBestResolutionTiles().length()
 		// Could use a cache for the best resolution tiles, but would
 		// have to be careful to drop the cache if we pan or zoom too much,
 		// and definitely if we pull in more data
 
 		drawing.beginClippedPath();
 
 		// Putting these declarations outside the loop ensures
 		// that no gaps appear between lines
 		double prevX = - Double.MAX_VALUE;
 		double prevY = - Double.MAX_VALUE;
 
 		for (GrapherTile tile: getBestResolutionTiles()) {
 			List<PlottablePoint> dataPoints = getDataPoints(tile);
 
 			if (dataPoints == null)
 				continue;
 
 			for (PlottablePoint point: dataPoints) {
 				double x = xAxis.project2D(point.getDate()).getX();
 				double y = yAxis.project2D(point.getValue()).getY();
 
 				if (x < MIN_DRAWABLE_VALUE || y < MIN_DRAWABLE_VALUE
 						|| Double.isInfinite(x) || Double.isInfinite(y)) {
 					// Don't draw a boundary point
 
 					// So that we don't draw a boundary point, we (relying
 					// on the fact that MIN_DRAWABLE_VALUE is negative)
 					// set prevY to something smaller than
 					// MIN_DRAWABLE_VALUE, ensuring that paintEdgePoint
 					// will be called on the next loop iteration
 					prevY = MIN_DRAWABLE_VALUE * 1.01;
 
 					continue;
 				}
 
 				// Skip any "reverse" drawing
 				if (prevX > x)
 					continue;
 
 				// Draw this part of the line
 				if (prevX > MIN_DRAWABLE_VALUE
 						&& prevY > MIN_DRAWABLE_VALUE)
 					paintDataPoint(drawing, prevX, prevY, x, y);
 				else
 					paintEdgePoint(drawing, x, y);
 
 				prevX = x;
 				prevY = y;
 			}
 		}
 
 		drawing.strokeClippedPath();
 	}
 
 	/**
 	 * Returns the ordered list of points this DataPlot should draw
 	 * in {@link #paintAllDataPoints()}.
 	 *
 	 * <p>It is acceptable, and not considered an error, if this or a subclass
 	 * implementation returns <tt>null</tt>.  Such a return should simply
 	 * be taken as a sign that the specified tile contains no data points
 	 * that paintAllDataPoints should draw.</p>
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
 	 * Paints a left edge point for a segment of the plot.
 	 *
 	 * <p>This method is designed to be overridden by subclasses.
 	 * Note that this is only called for the left edge of a plot
 	 * segment.  This particular implementation draws a small dot,
 	 * although a subclass implementation does not have to do the
 	 * same.  Note that all parameters (except drawing, of course)
 	 * are assumed to be in terms of pixels, not logical values
 	 * on the axes.</p>
 	 *
 	 * @param drawing
 	 * 		the
 	 * 		{@link org.bodytrack.client.BoundedDrawingBox BoundedDrawingBox}
 	 * 		that should constrain the drawing.  Forwarding graphics calls
 	 * 		through drawing will ensure that everything draws up to the edge
 	 * 		of the viewing window but no farther
 	 * @param x
 	 * 		the X-coordinate of the point to draw
 	 * @param y
 	 * 		the Y-coordinate of the point to draw
 	 */
 	protected void paintEdgePoint(BoundedDrawingBox drawing, double x,
 			double y) {
 		drawing.drawDot(x, y, DOT_RADIUS);
 	}
 
 	/**
 	 * Draws a single data point on the graph.
 	 *
 	 * <p>This method is designed to be overridden by subclasses.
 	 * Note that this method has as a precondition that
 	 * {@code prevX < x}.  Note that all parameters (except drawing,
 	 * of course) are assumed to be in terms of pixels.</p>
 	 *
 	 * @param drawing
 	 * 		the
 	 * 		{@link org.bodytrack.client.BoundedDrawingBox BoundedDrawingBox}
 	 * 		that should constrain the drawing.  Forwarding graphics calls
 	 * 		through drawing will ensure that everything draws up to the edge
 	 * 		of the viewing window but no farther
 	 * @param prevX
 	 * 		the previous X-value, which will be greater than
 	 * 		MIN_DRAWABLE_VALUE
 	 * @param prevY
 	 * 		the previous Y-value, which will be greater than
 	 * 		MIN_DRAWABLE_VALUE
 	 * @param x
 	 * 		the current X-value, which will be greater than
 	 * 		MIN_DRAWABLE_VALUE, and greater than or equal to
 	 * 		prevX
 	 * @param y
 	 * 		the current Y-value, which will be greater than
 	 * 		MIN_DRAWABLE_VALUE
 	 *
 	 * @see #MIN_DRAWABLE_VALUE
 	 */
 	protected void paintDataPoint(BoundedDrawingBox drawing, double prevX,
 			double prevY, double x, double y) {
 		drawing.drawLineSegment(prevX, prevY, x, y);
 	}
 
 	/**
 	 * Draws a single point on the graph, in highlighted style.
 	 *
 	 * <p>This is designed to be overridden by subclasses.  It is
 	 * called by {@link #paint()} after all data points have been
 	 * painted, and the parameter is the data point closest to
 	 * the mouse.  Note that this means that, by the time this
 	 * method is called, point has already been drawn.</p>
 	 *
 	 * <p>This draws a larger dot at point, although of course a subclass
 	 * implementation does not have to follow that lead.</p>
 	 *
 	 * @param drawing
 	 * 		the
 	 * 		{@link org.bodytrack.client.BoundedDrawingBox BoundedDrawingBox}
 	 * 		that should constrain the drawing.  Forwarding graphics calls
 	 * 		through drawing will ensure that everything draws up to the edge
 	 * 		of the viewing window but no farther
 	 * @param point
 	 * 		the data point closest to the mouse.  It is guaranteed that
 	 * 		point will never be <tt>null</tt> or equal to
 	 * 		{@link #HIGHLIGHTED_NO_SINGLE_POINT}
 	 */
 	protected void paintHighlightedPoint(BoundedDrawingBox drawing,
 			PlottablePoint point) {
 		double x = xAxis.project2D(point.getDate()).getX();
 		double y = yAxis.project2D(point.getValue()).getY();
 
 		// Draw three concentric circles to look like one filled-in circle
 		// The real radius is the first one used: HIGHLIGHTED_DOT_RADIUS
 		drawing.drawDot(x, y, HIGHLIGHTED_DOT_RADIUS);
 		drawing.drawDot(x, y, HIGHLIGHT_STROKE_WIDTH);
 		drawing.drawDot(x, y, NORMAL_STROKE_WIDTH);
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
 				maxCoveredTime + (maxTime - minTime) * 1e-3,
 				bestLevel);
 			// We need to move a little to the right of the current time
 			// so we don't get the same tile twice
 
 			if (bestAtCurrTime == null) {
 				maxCoveredTime += (maxTime - minTime) * 1e-2;
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
 		TileDescription bestDesc = null;
 
 		for (GrapherTile tile: currentData) {
 			TileDescription desc = tile.getDescription();
 
 			if (desc.getMinTime() > time || desc.getMaxTime() < time)
 				continue;
 
 			if (best == null) {
 				best = tile;
 				bestDesc = desc;
 			} else if(Math.abs(desc.getLevel() - bestLevel) <
 					Math.abs(bestDesc.getLevel() - bestLevel)) {
 				best = tile;
 				bestDesc = desc;
 			} else if (Math.abs(desc.getLevel() - bestLevel) ==
 					Math.abs(bestDesc.getLevel() - bestLevel)) {
 				if (desc.getLevel() < bestDesc.getLevel()) {
 					best = tile;
 					bestDesc = desc;
 				}
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
 	 * Computes the floor of the log (base 2) of x.
 	 *
 	 * @param x
 	 * 		the value for which we want to take the log
 	 * @return
 	 * 		the floor of the log (base 2) of x
 	 */
 	private static int log2(double x) {
 		if (x <= 0)
 			return Integer.MIN_VALUE;
 
 		return (int) Math.floor((Math.log(x) / LN_2));
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
 	private static double getTileWidth(int level) {
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
 
 		// Don't even bother trying to highlight if the mouse is out of
 		// bounds
 		if (maxTime < xAxis.getMin() || minTime > xAxis.getMax()
 				|| maxValue < yAxis.getMin() || minValue > yAxis.getMax())
 			return null;
 
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
 		List<PlottablePoint> points = getDataPoints(tile);
 		if (points == null)
 			return null;
 
 		for (PlottablePoint point: points) {
 			double time = point.getDate();
 			double val = point.getValue();
 
 			// Only check for proximity to points we can see
 			if (time < xAxis.getMin() || time > xAxis.getMax())
 				continue;
 			if (val < yAxis.getMin() || val > yAxis.getMax())
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
 	 * <p>Note that this does not highlight the axes associated with this
 	 * DataPlot.</p>
 	 */
 	public void highlight() {
 		highlightedPoint = HIGHLIGHTED_NO_SINGLE_POINT;
 	}
 
 	/**
 	 * Stops highlighting this DataPlot.
 	 *
 	 * <p>Note that this does not affect the highlighting status on the
 	 * axes associated with this DataPlot.</p>
 	 */
 	public void unhighlight() {
 		highlightedPoint = null;
 		possiblyDisplayHighlightedValue();
 	}
 
 	/**
 	 * Tells whether or not this DataPlot is highlighted.
 	 *
 	 * <p>If {@link #highlight()} has been called since the constructor
 	 * and since the last call to {@link #unhighlight()}, returns
 	 * <tt>true</tt>.  Otherwise, returns <tt>false</tt>.</p>
 	 *
 	 * @return
 	 * 		<tt>true</tt> if and only if this DataPlot is highlighted
 	 */
 	public boolean isHighlighted() {
 		return highlightedPoint != null;
 	}
 
 	/**
 	 * Highlights this <tt>DataPlot</tt> if and only if it contains a
 	 * point within threshold pixels of pos.
 	 *
 	 * <p>Also, if this data plot should be highlighted, this publishes
 	 * the highlighted data plot's value to the container widget, as long
 	 * as such a preference was indicated when this <tt>DataPlot</tt>
 	 * was created, using the publishValueOnHighlight constructor parameter.
 	 * On the other hand, if this data plot is currently highlighted but
 	 * should be unhighlighted, this removes the published value.  If a
 	 * subclass would like to change the formatting of the published
 	 * value, it should accomplish that by overriding
 	 * {@link #getDataLabel(PlottablePoint)}.</p>
 	 *
 	 * <p>Note that this does <strong>not</strong> unhighlight this
 	 * <tt>DataPlot</tt> if there is no point within threshold pixels of
 	 * pos.  A subclass may also change the measurement unit on threshold
 	 * (the unit is pixels here), as long as that fact is clearly
 	 * documented.</p>
 	 *
 	 * @param pos
 	 * 		the position at which the mouse is hovering, and from which
 	 * 		we want to derive our highlighting
 	 * @param threshold
 	 * 		the maximum distance the mouse can be from a point, while
 	 * 		still causing the highlighting effects
 	 * @return
 	 * 		<tt>true</tt> if and only if this highlights the axes
 	 * @throws IllegalArgumentException
 	 * 		if threshold is negative
 	 */
 	public boolean highlightIfNear(Vector2 pos, double threshold) {
 		highlightedPoint = closest(pos, threshold);
 		possiblyDisplayHighlightedValue();
 		return isHighlighted();
 	}
 
 	/**
 	 * Handles the value to be shown in the container as the highlighted
 	 * value.
 	 *
 	 * <p>This first checks publishValueOnHighlight.  If that is
 	 * <tt>false</tt>, does nothing.  Otherwise, checks highlightedPoint.
 	 * If it is <tt>null</tt> or equal to
 	 * {@link #HIGHLIGHTED_NO_SINGLE_POINT}, removes any messages that
 	 * might be showing for this data plot.  Otherwise, ensures that the
 	 * current value is being shown on the parent container, with the
 	 * value returned by {@link #getDataLabel(PlottablePoint)}.</p>
 	 */
 	private void possiblyDisplayHighlightedValue() {
 		if (! publishValueOnHighlight)
 			return;
 
 		if (highlightedPoint == null
 				|| highlightedPoint == HIGHLIGHTED_NO_SINGLE_POINT) {
 			// We can call this without problems because we know
 			// that container will ignore any invalid message IDs
 			container.removeValueMessage(publishedValueId);
 			publishedValueId = 0;
 		} else if (publishedValueId == 0) // Don't add message twice
 			publishedValueId = container.addValueMessage(
 				getDataLabel(highlightedPoint), color);
 	}
 
 	/**
 	 * Returns a label for the specified point.
 	 *
 	 * <p>This implementation takes the value of p out to three
 	 * significant digits and returns that value.  However, subclass
 	 * implementations might behave differently.</p>
 	 *
 	 * <p>This is designed to be overridden by subclasses that wish
 	 * to change the default behavior.  However, there are a few
 	 * requirements for subclass implementations, which unfortunately
 	 * cannot be expressed in code.  A subclass implementation of
 	 * this method must always return a non-<tt>null</tt> label in
 	 * finite (preferably very short) time, and must never throw
 	 * an exception.</p>
 	 *
 	 * @param p
 	 * 		the point for which to return a data label
 	 * @return
 	 * 		a data label to be displayed for p
 	 */
 	protected String getDataLabel(PlottablePoint p) {
 		double value = p.getValue();
 		double absValue = Math.abs(value);
 
 		String timeString =
 			getTimeString((long) (p.getDate() * 1000)) + "   ";
 
 		if (absValue == 0.0) // Rare, but possible
 			return timeString + "0.0";
 
 		if (absValue < 1e-3 || absValue > 1e7)
 			return timeString
 				+ NumberFormat.getScientificFormat().format(value);
 
 		return timeString
 			+ NumberFormat.getFormat("###,##0.0##").format(value);
 	}
 
 	/**
 	 * Returns a time string representing the specified time.
 	 *
 	 * <p>A caveat: time should be the number of <em>milliseconds</em>,
 	 * not seconds, since the epoch.  If a caller forgets to multiply
 	 * a time by 1000, wrong date strings (usually something
 	 * involving January 15, 1970) will come back.</p>
 	 *
 	 * @param time
 	 * 		the number of milliseconds since the epoch
 	 * @return
 	 * 		a string representation of time
 	 */
 	private String getTimeString(long time) {
 		String formatString = "EEE MMM dd yyyy, HH:mm:ss";
 		int fractionalSecondDigits = getFractionalSecondDigits();
 
 		// We know that fractionalSecondDigits will always be 0, 1, 2, or 3
 		switch (fractionalSecondDigits) {
 		case 1:
 			formatString += ".S";
 			break;
 		case 2:
 			formatString += ".SS";
 			break;
 		case 3:
 			formatString += ".SSS";
 		}
 
 		DateTimeFormat format = DateTimeFormat.getFormat(formatString);
 		return format.format(new Date(time));
 	}
 
 	/**
 	 * Computes the number of fractional second digits that should
 	 * appear in a displayed time string, based on the current level.
 	 *
 	 * <p>This <em>always</em> returns a nonnegative integer less than
 	 * or equal to 3.</p>
 	 *
 	 * @return
 	 * 		the number of fractional second digits that should appear
 	 * 		in a displayed times string
 	 */
 	private int getFractionalSecondDigits() {
 		int level = computeCurrentLevel();
 		if (level > 1)
 			return 0;
 		if (level == 1)
 			return 1;
 		if (level > -2) // 0 or -1
 			return 2;
 		return 3; // We can't get better than millisecond precision
 	}
 
 	/**
 	 * Returns the highlighted point maintained by this <tt>DataPlot</tt>.
 	 *
 	 * @return
 	 * 		the highlighted point this <tt>DataPlot</tt> keeps, or
 	 * 		<tt>null</tt> if there is no highlighted point
 	 */
 	public PlottablePoint getHighlightedPoint() {
 		return highlightedPoint;
 	}
 }
