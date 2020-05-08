 package package1;
 
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 public class Site implements Serializable, Comparable<Site>{
 		private static final long serialVersionUID = 1L;
 		
 		
 		/** The name of the person who is occupying the Site */
 		protected String nameReserving;
 		//hi there
 		/** The date the Site was checked-in (occupied) */
 		protected GregorianCalendar checkIn;
 
 		/** The estimated number of days the person is reserving */
 		/** This is just an estimate when the camper is  */
 		/** is checking in  */
 		protected int daysStaying; 
 
 		/** The date the Site was checked out */
 		/** This is the exact day they checked-out */
 		protected GregorianCalendar checkOutOn;
 
 		/** The Site number */
 		protected int siteNumber;
 		
 		/**************************************************************
 		 * Default Constructor
 		 *************************************************************/
 		public Site(){
 			nameReserving = "";
 			checkIn = new GregorianCalendar();	
 			checkOutOn = new GregorianCalendar();
 			daysStaying = 0;
			siteNumber = 1;
 		}
 		
 		/**************************************************************
 		 * Constructor for Site
 		 * @param checkIn takes in the date of check in
 		 * @param name takes in the name of the person
 		 * @param daysStaying takes in the days staying
 		 * @param out takes in the day of check out
 		 * @param siteNumber takes in the site number
 		 *************************************************************/
 		public Site(String name, String checkIn, int daysStaying, int siteNumber){
 			this.nameReserving = name;
 			
 			DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
 			Date date;
 			GregorianCalendar checkInDate = new GregorianCalendar();
 			try {
 				date = formatter.parse(checkIn);
 				checkInDate.setTime(date);
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 			
 			this.checkIn = checkInDate;
 			this.daysStaying = daysStaying;
 			this.siteNumber = siteNumber;
 		}
 		
 		/**************************************************************
 		 * @return the nameReserving
 		 *************************************************************/
 		public String getNameReserving() {
 			return nameReserving;
 		}
 
 		/**************************************************************
 		 * @param nameReserving the nameReserving to set
 		 *************************************************************/
 		public void setNameReserving(String nameReserving) {
 			this.nameReserving = nameReserving;
 		}
 
 		/**************************************************************
 		 * @return the checkIn
 		 *************************************************************/
 		public GregorianCalendar getCheckIn() {
 			return checkIn;
 		}
 
 		/**************************************************************
 		 * @param checkIn the checkIn to set
 		 *************************************************************/
 		public void setCheckIn(GregorianCalendar checkIn) {
 			this.checkIn = checkIn;
 		}
 
 		/**************************************************************
 		 * @return the daysStaying
 		 *************************************************************/
 		public int getDaysStaying() {
 			return daysStaying;
 		}
 
 		/**************************************************************
 		 * @param daysStaying the daysStaying to set
 		 *************************************************************/
 		public void setDaysStaying(int daysStaying) {
 			this.daysStaying = daysStaying;
 		}
 
 		/**************************************************************
 		 * @return the checkOutOn
 		 *************************************************************/
 		public GregorianCalendar getCheckOutOn() {
 			return checkOutOn;
 		}
 
 		/**************************************************************
 		 * @param checkOutOn the checkOutOn to set
 		 *************************************************************/
 		public void setCheckOutOn(GregorianCalendar checkOutOn) {
 			this.checkOutOn = checkOutOn;
 		}
 
 		/**************************************************************
 		 * @return the siteNumber
 		 *************************************************************/
 		public int getSiteNumber() {
 			return siteNumber;
 		}
 
 		/**************************************************************
 		 * @param siteNumber the siteNumber to set
 		 *************************************************************/
 		public void setSiteNumber(int siteNumber) {
 			this.siteNumber = siteNumber;
 		}
 
 		@Override
 		public int compareTo(Site o) {
 			return 0;
 		}  
 
 }
