 package de.tuberlin.techgi4.gpscalcWebservice;
 
 public class CoordinateVector {
 	public double distance;
 	public double velocity;
 	public CoordinateVector(double distance, double velocity) {
 		super();
 		this.distance = distance;
 		this.velocity = velocity;
 	}
 	@Override
 	public String toString() {
 		return "CoordinateVector [distance=" + distance + ", velocity="
 				+ velocity + "]";
 	}
 }
