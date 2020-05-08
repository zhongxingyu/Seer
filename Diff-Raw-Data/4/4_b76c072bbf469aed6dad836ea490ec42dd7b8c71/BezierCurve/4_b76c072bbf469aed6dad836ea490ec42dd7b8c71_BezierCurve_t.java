 package linewars.gamestate;
 
 public class BezierCurve {
 	/*
  	 * These points represent the 4 control
 	 * points in a bezier curve, with p0 and p3 being the end points.
 	 * 
 	 * -Ryan Tew
 	 */
 	private Position p0;
 	private Position p1;
 	private Position p2;
 	private Position p3;
 	
 	private double length;
 	//TODO Test what the right stepsize is to balance time vs accuracy.
 	static final double STEP_SIZE = 0.001;
 	
 	public BezierCurve(Position p0, Position p1, Position p2, Position p3)
 	{
 		this.p0 = p0;
 		this.p1 = p1;
 		this.p2 = p2;
 		this.p3 = p3;
 		
 		calculateLength(STEP_SIZE);
 	}
 	
 	public Position getP0()
 	{
 		return p0;
 	}
 
 	public Position getP1()
 	{
 		return p1;
 	}
 	
 	public Position getP2()
 	{
 		return p2;
 	}
 
 	public Position getP3()
 	{
 		return p3;
 	}
 	
 	public double getLength()
 	{
 		return length;
 	}
 	
 	/**
 	 * Approximates the length of this Lane by stepping along it and calculating line segment lenghts.
 	 * @param stepSize The size of the step to be taken. Smaller steps increase accuracy and calculation time.
 	 */
 	private void calculateLength(double stepSize)
 	{
 		if(stepSize <= 0 || stepSize >= 1)
 		{
 			throw new IllegalArgumentException("stepSize must be between 0 and 1");
 		}
 		double total = 0;
 		Transformation t1 = this.getPosition(0);
 		Transformation t2;
 
 		for(double d = stepSize; d <= 1; d += stepSize)
 		{
 			t2 = this.getPosition(d);
 			double distance = Math.sqrt(t1.getPosition().distanceSquared(t2.getPosition()));
 			total = total + distance;
 			t1 = t2;
 		}
 		length = total;
 	}
 	
 	/**
 	 * Gets the position along the bezier curve represented by the percentage
 	 * pos. This follows the equation found at
 	 * 		<a href="http://en.wikipedia.org/wiki/Bezier_curve#Cubic_B.C3.A9zier_curves">http://en.wikipedia.org/wiki/Bezier_curve</a>
 	 * B(t)= (1-t)^3 * P0 + 3(1-t)^2 * t * P1 + 3(1-t) * t^2 * P 2 + t^3 * P3 where t = [0,1].
 	 * 
 	 * @param pos
 	 *            The percentage along the bezier curve to get a position.
 	 * 
 	 * @return The position along the bezier curve represented by the percentage
 	 *         pos.
 	 */
 	public Transformation getPosition(double pos)
 	{
 		double term0 = Math.pow((1 - pos), 3);
 		double term1 = 3 * Math.pow(1 - pos, 2) * pos;
 		double term2 = 3 * (1 - pos) * Math.pow(pos, 2);
 		double term3 = Math.pow(pos, 3);
 
 		double posX = term0 * getP0().getX() + term1 * getP1().getX()
 				+ term2 * getP2().getX() + term3 * getP3().getX();
 		double posY = term0 * getP0().getY() + term1 * getP1().getY()
 				+ term2 * getP2().getY() + term3 * getP3().getY();
 
 		Position quad = getQuadraticPos(pos);
 		Position cube = new Position(posX, posY);
 		
 		double rot = calculateRot(quad, cube);
 		return new Transformation(cube, rot);
 	}
 	
 	/**
 	 * Calculate the rotation at a point on the cubic bezier curve given the position on it.
 	 * Implements the algorithm found at http://bimixual.org/AnimationLibrary/beziertangents.html
 	 * @param quad The position along the quadratic bezier curve represented by the first 3 points.
 	 * @param cube The position along the cubic bezier curve (all 4 points)
 	 * @return The rotation at point cube.
 	 */
 	private double calculateRot(Position quad, Position cube)
 	{
 		double ret;
 		
		double dy = (quad.getY() - cube.getY());
		double dx = (quad.getX() - cube.getX());
 
 		ret = Math.atan2(dy, dx) * (180 / Math.PI);
 
 		if (dx < 0 && dy < 0) ret *= -1;
 		if (dx > 0 && dy < 0) ret *= -1;
 		if (dx < 0 && dy > 0) ret = 360.0d - ret;
 		if (dx > 0 && dy > 0) ret = 360.0d - ret;
 		
 		return ret;
 	}
 	
 	/**
 	 * This method calculates the position along the 3-point bezier curve based on the actual 4-point curve.
 	 * This will be used in getPosition to get the rotatation of the curve using the formula found at
 	 * http://bimixual.org/AnimationLibrary/beziertangents.html
 	 * @param pos
 	 * @return
 	 */
 	private Position getQuadraticPos(double pos)
 	{
 		double term0 = Math.pow((1-pos), 2);
 		double term1 = 2 * (1-pos) * pos;
 		double term2 = Math.pow(pos, 2);
 		
 		double posX = term0 * getP0().getX() + term1 * getP1().getX()
 						+ term2 * getP2().getX();
 		double posY = term0 * getP0().getY() + term1 * getP1().getY()
 						+ term2 * getP2().getY();
 		
 		return new Position(posX, posY);
 	}
 }
