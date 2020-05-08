 /**
  * Particle.java
  * @author amk
  *
  * A single particle in a particle filter for localization
  */
  
 public class Particle {
 	private double xCoord;
 	private double yCoord;
 	private double pose;
     private double weight;
     
 	public Particle() {
 		new Particle(0,0,0,1);
 	}
 	
 	public Particle(double x, double y, double h, double w) {
 		xCoord = x;
 		yCoord = y;
 		pose = h;
 		weight = w;
 	}
 	
 	public double getX() {
 		return xCoord;
 	}
 	
 	public double getY() {
 		return yCoord;
 	}
 	
 	public double getPose() {
 		return pose;
 	}
 	
 	public void setX(double x) {
 		xCoord = x;
 	}
 	
 	public void setY(double y) {
 		yCoord = y;
 	}
 	
 	public void setPose(double h) {
 		pose = h;
 	}
 	public void setWeight(double w) {
 		weight = w;
 	}
 	public double getWeight() {
 		return weight;
 	}
 	public void move(double x, double y, double h) {
 		xCoord = x;
 		yCoord = y;
 		pose = h;
 	}
 	
 	@Override
 	public String toString() {
 	    String s = "Particle @ (" + xCoord + ", " + yCoord + ", " + pose + ")";
 	    s += " weight= " + String.valueOf(weight);
 	    return s;
 	}
 	
 }
