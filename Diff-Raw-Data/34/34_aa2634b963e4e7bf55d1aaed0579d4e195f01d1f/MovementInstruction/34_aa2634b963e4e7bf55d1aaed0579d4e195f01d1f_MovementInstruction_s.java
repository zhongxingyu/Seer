 package edu.bismarckstate.shortestpathhelper.util;
 
 public class MovementInstruction implements Instruction{
 
 	
 	private double distance;
 	private double direction;
 	
 	/**
 	 * Default constructor sets the direction as 0 and the distance as 0
 	 */
 	public MovementInstruction(){
 		setDirection(0.0);
 		setDistance(0.0);
 	}
 	
 	/**
 	 * Constructor setting the distance to 0 and the direction to d
 	 * @param direction Double for the direction
 	 */
 	public MovementInstruction(double direction){
 		setDirection(direction);
 		setDistance(0);
 	}
 	
 	/**
 	 * Constructor sets the distance and direction
 	 * @param direction direction for the robot to face
 	 * @param distanceToTravel distance to travel in that direction
 	 */
 	public MovementInstruction(double direction, double distanceToTravel){
 		setDirection(direction);
 		setDistance(distanceToTravel);
 	}
 	
 	/**
 	 * Get the currently set Directions double value
 	 * @return double of the direction it should be facing
 	 */
 	public double getDirection(){
 		return direction;
 	}
 	
 	/**
 	 * Gets a string of the direction in the reference of N S E W
 	 * @return N, NE, E, S, SE, S, SW, W, NW depending on current direction
 	 */
 	public String getDirectionString(){
 		if (this.direction == 0){
 			return "N";
 		}else if (this.direction > 0 && this.direction < 90){
 			return "NE";
 		}else if (this.direction == 90){
 			return "E";
 		}else if (this.direction > 90 && this.direction < 180){
 			return "SE";
 		}else if (this.direction == 180){
 			return "S";
 		}else if (this.direction > 180 && this.direction < 270){
 			return "SW";
 		}else if (this.direction == 270){
 			return "W";
 		}else if (this.direction > 270 && this.direction < 360){
 			return "NW";
 		}else{
 			return "INCORRECT VALUE";
 		}
 	}
 	
 	/**
 	 * Set the direction that the robot should face
 	 * @param d direction to face 0 for north 90 for east 180 for south 270 for west
 	 */
 	public void setDirection(Double d){
 		direction = d;
 	}
 	
 	/**
 	 * Get the current distance
 	 * @return double of the distance
 	 */
 	public double getDistance(){
 		return distance;
 	}
 	
 	/**
 	 * Set the distance to travel
 	 * @param dist the distance you want it to travel
 	 */
 	public void setDistance(double dist){
 		this.distance = dist;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		long temp1 = Double.doubleToLongBits(direction);
 		result = prime * result
 				+ (int) (temp1 ^ (temp1 >>> 32));
 		long temp;
 		temp = Double.doubleToLongBits(distance);
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		MovementInstruction other = (MovementInstruction) obj;
 		if (direction != other.direction)
 			return false;
 		if (Double.doubleToLongBits(distance) != Double
 				.doubleToLongBits(other.distance))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "<" + distance + ", " + direction
 				+ ">";
 	}
 
 	@Override
 	public double getNormalizedTurn(double currentAngle) {
		double dir = this.direction;
		double x = (dir + 360) - currentAngle;
		double y = (currentAngle + 360) - dir;
		
		return ((x<y) ? x : -y);
 	}
 
 	@Override
 	public double getNormalizedDistance() {
 		return this.distance;
 	}
 	
 	
 }
