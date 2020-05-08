 import java.util.Iterator;
 import java.util.LinkedList;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import javax.media.opengl.GLAutoDrawable;
 
 public class ParameterGraph extends ParameterVisualization 
 {
 	private double totalTime;
 	// minimum value displayed
 	private double minValue;
 	// maximum value displayed
 	private double maxValue;
 	// visible value range
 	private double valueRange;
 	// maximum time visible at once in seconds
 	private double timeFrame;
 	// small constant offset that is added to time frame to close the gap
 	// between left frame border and beginning of the graph
 	// TODO: adjust value
 	private final double borderOffset = 0.1;
 	// utility to calculate time between frames
 	// TODO: move this somewhere it should rather be
 	// TODO: remove later on
 	private FrameTimer timer;
 	// points of the graph mapped to fit in value range
 	private LinkedList<Vec2> points;
 	// mapped colors for points
 	private LinkedList<Color> colors;
 
 	private GL2 gl;
 	
 	private ColorMapper mapper;
 	
 	private Color bgColor;
 	private Color axisColor;
 	
 	// constructors
	public ParameterGraph(double minValue, double maxValue, double optimalValue, double timeFrame, Color bgColor, Color axisColor)
 	{
 		// maxValue has to be larger than minValue
 		assert(maxValue >= minValue && optimalValue >= minValue && optimalValue <= maxValue);
 		
 		this.minValue = minValue;
 		this.maxValue = maxValue;
 		this.valueRange = maxValue - minValue;
 		this.timeFrame = timeFrame;
 		
 		this.bgColor = bgColor;
 		this.axisColor = axisColor;
 		
		this.mapper = new ColorMapper(optimalValue, minValue, maxValue, Interpolator.CUBIC_FIT, Color.GREEN, Color.ORANGE);
 		
 		totalTime = 0;
 		timer = new FrameTimer();
 		points = new LinkedList<Vec2>();
 		colors = new LinkedList<Color>();
 	}
 	
 	// default colors
 	public ParameterGraph(double minValue, double maxValue, double optimalValue, double timeFrame)
 	{
		this(minValue, maxValue, optimalValue, timeFrame, Color.DARK_GRAY, Color.WHITE);
 	}
 	
 	// centered around y-axis
 	public ParameterGraph(double range, double optimalValue, double timeFrame)
 	{
 		this(-range/2, range/2, optimalValue, timeFrame);
 		
 	}
 	
 	// setters
 	public void setMinValue(double minValue)
 	{
 		this.minValue = minValue;
 	}
 	
 	public void setMaxValue(double maxValue)
 	{
 		this.maxValue = maxValue;
 	}
 
 	// set graph color
 	public void setAxisColor(Color color)
 	{
 		this.axisColor = color;
 	}
 	
 	// set bg color
 	public void setBGColor(Color color)
 	{
 		this.bgColor = color;
 		bgColor.setClearColor(gl);
 	}
 	
 	@Override
 	public void init(GLAutoDrawable drawable) 
 	{
 		gl = drawable.getGL().getGL2();
 		bgColor.setClearColor(gl);
 	}
 
 	@Override
 	public void dispose(GLAutoDrawable drawable) 
 	{
 	}
 
 	// render method
 	@Override
 	public void display(GLAutoDrawable drawable) 
 	{
 		// update has to be called repeatedly
 		timer.update();
 		this.update(Math.sin(timer.currentTime()), timer.deltaTime());
 		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
 		
 		drawGraph(gl);
 		drawAxis();	
 	}
 	
 	// draw graph as line strip
 	private void drawGraph(GL2 gl)
 	{
 		double totalX = 1.0;
 		Iterator<Vec2> iterPoints = points.iterator();
 		Iterator<Color> iterColors = colors.iterator();
 		
 		Vec2 point;
 		gl.glBegin(GL.GL_LINE_STRIP);
 		while(iterPoints.hasNext())
 		{
 			point = iterPoints.next();
 			iterColors.next().setDrawColor(gl);
 			// viewport is defined from -1 to 1 in both x and y direction so values need to be transformed again
 			gl.glVertex2d(2*totalX-1, 2*point.y-1);
 			totalX -= point.x / timeFrame;
 		}
 		gl.glEnd();
 	}
 	
 	// draw axis and value range
 	private void drawAxis()
 	{
 		// set color
 		axisColor.setDrawColor(gl);
 		double zeroY = -2*minValue/valueRange - 1;
 		gl.glBegin(GL.GL_LINES);
 			gl.glVertex2d(-1, zeroY);
 			gl.glVertex2d( 1, zeroY);
 		gl.glEnd();
 	}
 
 	@Override
 	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) 
 	{
 		//TODO: implement
 	}
 
 	@Override
 	public void update(double newValue, double deltaTime) 
 	{
 		double mappedValue = (newValue - minValue) / valueRange;
 		Vec2 newPoint = new Vec2(deltaTime, mappedValue);
 		points.addFirst(newPoint);
 		colors.addFirst(mapper.getInterpolatedColor(newValue));
 		// update total time
 		totalTime += deltaTime;
 		// remove oldest points when total time is reached
 		while(totalTime > timeFrame + borderOffset)
 		{
 			Vec2 tail = points.removeLast();
 			totalTime -= tail.x;
 			colors.removeLast();
 		}
 	}
 
 }
