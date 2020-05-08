 // color mapping class for graphs
 // TODO: extend functionality for ranged mappings etc.
 public class ColorMapper 
 {
 	// value that is considered optimal
 	private double optimalValue;
 	// maximum distance to optimal color
 	private double rangeScale;
 	
 	private Color goodValueColor;
 	private Color badValueColor;
 	
 	private Interpolator interpolator;
 	
 	public ColorMapper(double optimalValue, double minValue, double maxValue, Interpolator interpolator, Color goodValueColor, Color badValueColor)
 	{
 		this.optimalValue = optimalValue;
 		this.rangeScale = Math.max(maxValue - optimalValue, optimalValue - minValue);
 		
 		this.goodValueColor = goodValueColor;
 		this.badValueColor = badValueColor;
 		this.interpolator = interpolator;
 	}
 	
	public Color getOptimalColor()
	{
		return goodValueColor;
	}
	
	public Color getWorstColor()
	{
		return badValueColor;
	}
	
 	public ColorMapper(double optimalValue, double minValue, double maxValue, Interpolator interpolator)
 	{
 		this(optimalValue, minValue, maxValue, interpolator, Color.GREEN, Color.RED);
 	}
 	
 	public Color getInterpolatedColor(double value)
 	{
 		float t = 1.0f - (float) (Math.abs(value - optimalValue) / rangeScale);
 		// interpolate between good and bad color
 		return badValueColor.interpolate(goodValueColor, interpolator, t);
 	}
 }
