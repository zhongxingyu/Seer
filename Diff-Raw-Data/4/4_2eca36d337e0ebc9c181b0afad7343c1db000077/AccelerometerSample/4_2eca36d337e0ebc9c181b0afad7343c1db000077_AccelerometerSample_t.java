 package com.buglabs.bug.accelerometer.pub;
 
 /**
  * Represents an sample for a 3-axis accelerometer
  * 
  * @author Angel Roman
  */
 public class AccelerometerSample {
 	private float accelerationX;
 	private float accelerationY;
 	private float accelerationZ;
 	
 	public AccelerometerSample(float accelx, float accely, float accelz) {
 		this.accelerationX = accelx;
 		this.accelerationY = accely;
 		this.accelerationZ = accelz;
 	}
 	
 	public float getAccelerationX() {
 		return accelerationX;
 	}
 	
 	public void setAccelerationX(float accelerationX) {
 		this.accelerationX = accelerationX;
 	}
 	
 	public float getAccelerationY() {
 		return accelerationY;
 	}
 	
 	public void setAccelerationY(float accelerationY) {
 		this.accelerationY = accelerationY;
 	}
 	
 	public float getAccelerationZ() {
 		return accelerationZ;
 	}
 	
 	public void setAccelerationZ(float accelerationZ) {
 		this.accelerationZ = accelerationZ;
 	}
	
	public String toString() {
		return "X = " + accelerationX + " Y = " + accelerationY + "Z = " + accelerationZ;
	}
 }
