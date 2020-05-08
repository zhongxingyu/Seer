 package enviroment;
 
 public class Sensor {
 	boolean carDetected = false;
	boolean valid = true;
 
 	public boolean isCarDetected() {
 		return carDetected;
 	}
 
 	public void setCarDetected(boolean carDetected) {
 		this.carDetected = carDetected;
 	}
 
	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
 	}
 	
 	
 }
