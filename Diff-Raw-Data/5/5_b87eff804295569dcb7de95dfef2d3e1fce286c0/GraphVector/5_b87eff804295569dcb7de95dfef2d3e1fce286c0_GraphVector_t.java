 package autograph;
 
 /**
  * This object is used to hold a vector for use in the force-
  * directed graph layout method.
  * 
  * @author Jeff Farris
  * @version 1.0
  * 
  */
 public class GraphVector {
 	
 	private double xCor;
 	private double yCor;
 	
 	/**
 	 * Constructor with 0 parameters, initializes to 0,0
 	 */
 	public GraphVector() {
 		xCor = 0;
 		yCor = 0;
 	}
 	
 	/**
 	 * Constructor with x,y coordinate parameters
 	 * 
 	 * @param x - x coordinate
 	 * @param y - y coordinate
 	 */
 	public GraphVector(double x, double y) {
 		xCor = x;
 		yCor = y;
 	}
 	
 	/**
 	 *  Method to return the x coordinate
 	 * @return xCor
 	 */
 	public double mGetXCor() {
 		return xCor;
 	}
 	
 	/**
 	 *  Method to return the y coordinate
 	 * @return yCor
 	 */
 	public double mGetYCor() {
 		return yCor;
 	}
 	
 	/**
 	 *  Method to set the x coordinate
 	 * @param x - xCor
 	 */
 	public void mSetXCor(double x) {
 		xCor = x;
 	}
 	
 	/**
 	 *  Method to set the y coordinate
 	 * @param y - yCor
 	 */
 	public void mSetYCor(double y) {
 		yCor = y;
 	}
 	
 	/**
 	 *  Method to return the distance of the vector
 	 * @return the distance
 	 */
 	public double mGetDistance() {
 		return Math.sqrt(Math.pow(xCor, 2.0) + Math.pow(yCor, 2.0));
 	}
 
 	/**
 	 * Calculates the repulsive force between two nodes
 	 * 
 	 * @param distance - the distance between the two nodes in question
 	 * @param k - the ideal distance for nodes
 	 * @param temp - the max distance allowed to move
 	 * @return - the repulsive force for the nodes
 	 */
 	public double mCalcRepulsive(double k, double temp) {
		return /*Math.pow(k, 2.0)*/ k / mGetDistance();
 	}
 
 	/**
 	 * 
 	 * Calculates the attractive force between two nodes
 	 * 
 	 * @param distance - the distance between the two nodes in question
 	 * @param k - the ideal distance for nodes
 	 * @return - the attractive force for the nodes
 	 */
 	public double mCalcAttractive(double k) {
		return /*Math.pow(mGetDistance(), 2.0)*/mGetDistance() / k;
 	}
 }
