 package enviroment;
 
 public class Sensor {
 	boolean carDetected = false;
	boolean validity = true;
 
 	public boolean isCarDetected() {
 		return carDetected;
 	}
 
 	public void setCarDetected(boolean carDetected) {
 		this.carDetected = carDetected;
 	}
 
	public void setValidity(boolean b) {
		this.validity = b;
		
 	}
 	
 	
 }
