 package package1;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 public class Tent extends Site {
 	private static final long serialVersionUID = 1L;
 	
 	/** Type for Tent **/
 	public static final int TYPE = 0;
 	
 	/** Represents the number of tenters on this site */
 	private int numOfTenters;
 
 	public Tent(){
 		super();
 	}
 	/**************************************************************
 	 * Constructor for tents
 	 * @param tenters
 	 *************************************************************/
 	public Tent(Object[] vars) {
 		this((String) vars[0], (String)vars[2], (Integer) vars[4], (Integer) vars[1], (Integer) vars[3]);
 	}
 	/**************************************************************
 	 * Constructor for tents
 	 * @param tenters
 	 *************************************************************/
 	public Tent(String name, String checkIn, int daysStaying, int siteNumber, int tenters) {
 		super(name, checkIn, daysStaying, siteNumber);
 		this.numOfTenters = tenters;
 	}
 
 	/**************************************************************
 	 * @return the numOfTenters
 	 *************************************************************/
 	public int getNumOfTenters() {
 		return numOfTenters;
 	}
 
 	/**************************************************************
 	 * @param numOfTenters the numOfTenters to set
 	 *************************************************************/
 	public void setNumOfTenters(int numOfTenters) {
 		this.numOfTenters = numOfTenters;
 	}
 	
 	
 }
