 package ca.scotthyndman.sparklines;
 
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Path;
 import android.util.AttributeSet;
 
 /**
  * A line spark view is a type of graph, which displays information as a series of data points 
  * connected by straight line segments.
  *  
  * <h2>Features:</h2>
  * 
  * <h3>Normal Range</h3>
  * <p>Threshold values between which to draw a bar to denote the "normal" or expected range of values.
  * 
  * <h3>Min, max and latest spots</h3>
  * <p>Spots can be drawn to indicate the minimum, maximum and latest value markers.
  * 
  * <h3>Fill</h3>
  * <p>A color-configurable fill can be drawn below the line in the chart. 
  * 
  * @see http://en.wikipedia.org/wiki/Line_chart
  * @author Scott Hyndman
  */
 public class LineSparkView extends SparkView {
 
   // DEFAULT PAINTS (set at bottom in static constructor)
   
   private static final Paint DEFAULT_NORMAL_RANGE_PAINT;
   private static final Paint DEFAULT_SPOT_PAINT;
   
   private float normalRangeMin;
   private float normalRangeMax;
   
   private float spotRadius  = 2f;
   
   private boolean showMinPoint    = true;
   private boolean showMaxPoint    = true;
   private boolean showLatestPoint = true;
   private boolean showNormalRange = false;
   private boolean drawNormalOnTop = false;
   
   private Paint normalRangePaint = new Paint(DEFAULT_NORMAL_RANGE_PAINT);
   private Paint latestSpotPaint = new Paint(DEFAULT_SPOT_PAINT);
   private Paint minSpotPaint = new Paint(DEFAULT_SPOT_PAINT);
   private Paint maxSpotPaint = new Paint(DEFAULT_SPOT_PAINT);
   
   private DrawInfo drawInfo;
   
 
   public LineSparkView(Context context) { super(context); }
   public LineSparkView(Context context, AttributeSet attrs) { super(context, attrs); }
   
   
   public float getNormalRangeMin() { return normalRangeMin; }
   public float getNormalRangeMax() { return normalRangeMax; }
   public void setNormalRange(float min, float max) {
     normalRangeMin = min;
     normalRangeMax = max;
   }
   
   /**
    * Sets the ARGB color of the spot that indicates the minimum point on the line graph.
    */
   public void setMinSpotColor(int color) { minSpotPaint.setColor(color); }
   
   /**
    * Sets the ARGB color of the spot that indicates the maximum point on the line graph.
    */
   public void setMaxSpotColor(int color) { maxSpotPaint.setColor(color); }
   
   /**
    * Sets the ARGB color of the spot that indicates the most recent point on the line graph.
    */
   public void setLatestSpotColor(int color) { latestSpotPaint.setColor(color); }
   
   /**
    * Returns <code>true</code> if the normal range should be drawn above the line fill.
    */
   public boolean getDrawNormalOnTop() { return drawNormalOnTop; }
   
   /**
    * Sets whether the normal range should be drawn above the line fill.
    */
   public void setDrawNormalOnTop(boolean drawNormalOnTop) { this.drawNormalOnTop = drawNormalOnTop; }
   
   /**
    * Returns <code>true</code> if the chart should draw a rectangle over the chart which represents
    * the "normal" operating area of the stat.
    * 
    * @see #setNormalRange(float, float)
    */
   public boolean getShowNormalRange() { return showNormalRange; };
   
   /**
    * Sets whether the chart should draw a rectangle over the chart which represents the "normal" 
    * operating area of the stat.
    * 
    * @see #setNormalRange(float, float)
    */
   public void setShowNormalRange(boolean showNormalRange) { this.showNormalRange = showNormalRange; }
   
   /**
    * Returns <code>true</code> if the chart should draw a small circle over the most recent
    * value.
    */
   public boolean getShowLatestSpot() { return showLatestPoint; }
   
   /**
    * Sets whether the chart should draw a small circle over the most recent value.
    */
   public void setShowLatestSpot(boolean showLatestPoint) { this.showLatestPoint = showLatestPoint; }
   
   /**
    * Returns <code>true</code> if the chart should draw a small circle over the minimum value.
    */
   public boolean getShowMinSpot() { return showMinPoint; }
   
   /**
    * Sets whether the chart should draw a small circle over the minimum value.
    */
   public void setShowMinSpot(boolean showMinPoint) { this.showMinPoint = showMinPoint; }
   
   /**
    * Returns <code>true</code> if the chart should draw a small circle over the maximum value.
    */
   public boolean getShowMaxPoint() { return showMaxPoint; }
   
   /**
    * Sets whether the chart should draw a small circle over the maximum value.
    */
   public void setShowMaxPoint(boolean showMaxPoint) { this.showMaxPoint = showMaxPoint; }
   
   /**
    * Returns the width, in pixels, of the line used to connect chart values.
    */
  public float getLineWidth() { return linePaint.getStrokeWidth(); }
   
   /**
    * Sets the width, in pixels, of the line used to connect chart values.
    * @param lineWidth
    */
  public void setLineWidth(float lineWidth) { linePaint.setStrokeWidth(lineWidth); }
   
   /**
    * Returns the radius, in pixels, of the spot drawn over the minimum, maximum and latest values.
    */
   public float getSpotRadius() { return spotRadius; }
   
   /**
    * Sets the radius, in pixels, of the spot drawn over the minimum, maximum and latest values.
    */
   public void setSpotRadius(float spotRadius) { this.spotRadius = spotRadius; }
   
   
   //
   // INVALIDATION
   //
   
   protected DrawInfo getDrawInfo() {
     if (drawInfo == null)
       drawInfo = new DrawInfo();
     
     return drawInfo;
   }
 
   @Override
   protected void calculateFeatures() {
     if (drawInfo == null)
       drawInfo = getDrawInfo();
     
     findExtremes();
     preparePaths();
   }
   
   /**
    * Finds extremes in the data set, and the extremes used to draw the chart (which can differ).
    */
   private void findExtremes() {
     float yMin = values.isEmpty() ? 0f : values.get(0);
     float yMinX = 0;
     float yMax = values.isEmpty() ? 0f : values.get(0);
     float yMaxX = 0;
     
     float yVal = 0f;
     int i, len = values.size();
     for (i = 0; i < len; i++) {
       yVal = values.get(i);
       
       if (yVal < yMin) {
         yMin = yVal;
         yMinX = i;
       }
       else if (yVal > yMax) {
         yMax = yVal;
         yMaxX = i;
       }
     }
     
     drawInfo.minSpot[0] = yMinX;
     drawInfo.minSpot[1] = yMin;
     drawInfo.maxSpot[0] = yMaxX;
     drawInfo.maxSpot[1] = yMax;
     drawInfo.latestSpot[0] = i - 1;
     drawInfo.latestSpot[1] = yVal;
     drawInfo.minDrawnX = 0;
     drawInfo.maxDrawnX = values.size() - 1;
     drawInfo.drawRangeX = drawInfo.maxDrawnX - drawInfo.minDrawnX;
     drawInfo.drawRangeX = drawInfo.drawRangeX == 0f ? 1f : drawInfo.drawRangeX;
     drawInfo.minDrawnY = chartRangeSet 
       ? chartRangeMin 
       : showNormalRange ? Math.min(yMin, normalRangeMin) : yMin;
     drawInfo.maxDrawnY = chartRangeSet
       ? chartRangeMax
       : showNormalRange ? Math.max(yMax, normalRangeMax) : yMax;
     drawInfo.drawRangeY = drawInfo.maxDrawnY - drawInfo.minDrawnY;
     drawInfo.drawRangeY = drawInfo.drawRangeY == 0f ? 1f : drawInfo.drawRangeY;
   }
   
   /**
    * Prepares paths used for drawing the lines and fills in the chart.
    */
   private void preparePaths() {
     if (values.isEmpty())
       return;
     
     Path linePath = drawInfo.linePath;
     linePath.rewind();
     
     Path fillPath = drawInfo.fillPath;
     fillPath.rewind();
     
     int len = values.size();
     for (int i = 0; i < len; i++) {
       float yVal = values.get(i);
       
       if (i == 0)
         linePath.moveTo(toCanvasX(i), toCanvasY(yVal));
       else
         linePath.lineTo(toCanvasX(i), toCanvasY(yVal));
     }
     
     if (!showFill) {
       fillPath.addPath(linePath);
       fillPath.lineTo(drawInfo.right, drawInfo.bottom);
       fillPath.lineTo(drawInfo.left, drawInfo.bottom);
       fillPath.lineTo(drawInfo.left, toCanvasY(values.get(0)));
     }
   }
   
   
   //
   // DRAWING
   //
   
   @Override
   protected void internalDraw(Canvas canvas) {
     if (showNormalRange && !drawNormalOnTop)
       drawNormalRange(canvas);
     
     if (showFill)
       drawFill(canvas);
     
     if (showNormalRange && drawNormalOnTop)
       drawNormalRange(canvas);
     
     drawLines(canvas);
     
     if (showMinPoint)
       drawSpot(canvas, drawInfo.minSpot, minSpotPaint);
     if (showMaxPoint)
       drawSpot(canvas, drawInfo.maxSpot, maxSpotPaint);
     if (showLatestPoint)
       drawSpot(canvas, drawInfo.latestSpot, latestSpotPaint);
   }
 
   /**
    * Draws the fill onto the canvas.
    */
   protected void drawFill(Canvas canvas) {
     canvas.drawPath(drawInfo.fillPath, fillPaint);
   }
 
   /**
    * Draws the lines onto the canvas.
    */
   protected void drawLines(Canvas canvas) {
     canvas.drawPath(drawInfo.linePath, linePaint);
   }
   
   /**
    * Draws the normal range.
    */
   protected void drawNormalRange(Canvas canvas) {
     canvas.drawRect(
         drawInfo.left, 
         toCanvasY(normalRangeMax), 
         drawInfo.right, 
         toCanvasY(normalRangeMin), 
         normalRangePaint);
   }
   
   /**
    * Draws a spot.
    */
   protected void drawSpot(Canvas canvas, float[] spot, Paint paint) {
     canvas.drawCircle(toCanvasX(spot[0]), toCanvasY(spot[1]), spotRadius, paint);
   }
 
   
   //
   // Adds additional functionality required for the graph
   //
   
   protected static class DrawInfo extends SparkView.DrawInfo {
     public Path linePath = new Path();
     public Path fillPath = new Path();
     
     public float[] minSpot = new float[2];
     public float[] maxSpot = new float[2];
     public float[] latestSpot = new float[2];
   }
   
   
   static {
     DEFAULT_NORMAL_RANGE_PAINT = new Paint();
     DEFAULT_NORMAL_RANGE_PAINT.setColor(0xFFB1F2B1);
     
     DEFAULT_SPOT_PAINT = new Paint();
     DEFAULT_SPOT_PAINT.setStyle(Style.FILL);
     DEFAULT_SPOT_PAINT.setColor(0xFFFF8800);
   }
 }
