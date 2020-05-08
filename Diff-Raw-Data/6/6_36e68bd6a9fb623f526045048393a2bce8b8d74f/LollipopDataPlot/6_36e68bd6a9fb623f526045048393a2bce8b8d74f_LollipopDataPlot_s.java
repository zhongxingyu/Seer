 package org.bodytrack.client;
 
 import gwt.g2d.client.graphics.Color;
 import gwt.g2d.client.graphics.DirectShapeRenderer;
 import gwt.g2d.client.graphics.Surface;
 
 /**
  * <p>
  * <code>LollipopDataPlot</code> renders data points as "lollipops", which are dots with a line from the min y value to
  * the data point.
  * </p>
  *
  * @author Chris Bartley (bartley@cmu.edu)
  */
 public class LollipopDataPlot extends DotDataPlot {
 
    public LollipopDataPlot(final GraphWidget container, final GraphAxis xAxis, final GraphAxis yAxis,
                            final String deviceName, final String channelName, final String url, final int minLevel,
                            final Color color, final boolean publishValueOnHighlight) {
       super(container, xAxis, yAxis, deviceName, channelName, url, minLevel, color, publishValueOnHighlight);
    }
 
    @Override
    protected void paintEdgePoint(final BoundedDrawingBox drawing,
                                  final GrapherTile tile,
                                  final double x,
                                  final double y,
                                  final PlottablePoint rawDataPoint) {
 
       super.paintEdgePoint(drawing, tile, x, y, rawDataPoint);
 
       final Canvas canvas = getCanvas();
       final Surface surface = canvas.getSurface();
       final DirectShapeRenderer renderer = canvas.getRenderer();
       final GraphAxis yAxis = getYAxis();
 
       surface.setFillStyle(getColor());
 
      // The Y-value in units on the Y-axis corresponding to the lowest point to draw on the plot
      final double minDrawUnits = Math.max(0.0, yAxis.getMin());

       // The Y-value in pixels corresponding to the lowest point to draw on the rectangle
      final double minDrawY = yAxis.project2D(minDrawUnits).getY();
 
       // Draw a line
       final double oldLineWidth = surface.getLineWidth();
       surface.setLineWidth(isHighlighted() ? HIGHLIGHT_STROKE_WIDTH : NORMAL_STROKE_WIDTH);
 
       renderer.beginPath();
       renderer.moveTo(x, minDrawY);
       renderer.drawLineTo(x, y);
       renderer.closePath();
 
       renderer.stroke();
       surface.setLineWidth(oldLineWidth);
    }
 
    /**
     * Returns the type of this plot.
     *
     * @return
     * 		a string representing the type of this plot.  For objects
     * 		of runtime type <tt>DataPlot</tt>, this will always be
     * 		equal to the string &quot;plot&quot;, although subclasses
     * 		should override this implementation
     */
    public String getType() {
       return "lollipop";
    }
 }
