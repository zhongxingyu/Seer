 package package1;
 import java.util.*;
 public class RV extends Site {
 	private static final long serialVersionUID = 1L;
 
 	/** Type for RV **/
 	public static final int TYPE = 1;
 	
 	/** Represents the power supplied to the site */
 	private int power; // 30, 40, 50 amps of service.
 
 	public RV(){
 		super();
 	}
 	
	
 	/**************************************************************
 	 * Constructor for RV
 	 * @param array of objects... to constuct a RV with
 	 *************************************************************/
 	public RV(Object[] vars) {
 		this((String) vars[0], (String)vars[2], (Integer) vars[4], (Integer) vars[1], (Integer) vars[3]);
 	}
 	
 	/**************************************************************
 	 * Constructor for RV
 	 * @param power
 	 *************************************************************/
 	public RV(String name, String checkIn, int daysStaying, int siteNumber, int power) {
 		super(name, checkIn, daysStaying,siteNumber);
 		this.power = power;
 	}
 
 	/**************************************************************
 	 * @return the power
 	 *************************************************************/
 	public int getPower() {
 		return power;
 	}
 
 	/**************************************************************
 	 * @param power the power to set
 	 *************************************************************/
 	public void setPower(int power) {
 		this.power = power;
 	}
 	
 	
 }
