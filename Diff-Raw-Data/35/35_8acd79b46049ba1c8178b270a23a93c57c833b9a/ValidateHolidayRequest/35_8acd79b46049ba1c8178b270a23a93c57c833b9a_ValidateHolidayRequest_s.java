 package RequestHoliday;
 
 import globalMessage.SystemMsg;
 import database.DriverInfo;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Date;
 import org.joda.time.DateTime;
 import org.joda.time.Days;
 
 public class ValidateHolidayRequest
 {
   private static String[]
     monthNoValue = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug","Sep", "Oct", "Nov", "Dec"};
     
     public static SystemMsg systemMsg 
                   = new SystemMsg();
   //Check if a given interval is possible to be a driver holiday
   //Written by: Oak. Last modified: 17/02/13
   public static boolean validateRequest
     (GregorianCalendar dateFrom, GregorianCalendar dateTo, int driverID)
   {
     System.out.println("validating request");
     //validate holiday lenght
     if(dateTo.YEAR > dateFrom.YEAR) //Over the year
     {
       System.out.println("OVER THE YEAR REQUEST");
       GregorianCalendar endOfThisYear
        = new GregorianCalendar(dateFrom.YEAR, 11, 31, 0, 0, 0);
       GregorianCalendar startOfNextYear 
        = new GregorianCalendar(dateTo.YEAR, 0, 1, 0, 0, 0);
       if(!validateHolidayLength(dateFrom, endOfThisYear, driverID, true))
       {
         System.out.println("The request is not valid: This year");
         return false;
       }//if
       else if(!validateHolidayLength(startOfNextYear, dateTo, driverID, true))
       {
         System.out.println("The request is not valid: Next year");
         return false;    
       }//else if
     }//if. over the year  
     else if (!validateHolidayLength(dateFrom, dateTo, driverID, false))
     {
       //System.out.println("The length is greater than 25 or -ve");
       return false;
     }//with in one year
     
     //check if max driver at each DATE interval
     if(!checkDateInterval(dateFrom, dateTo, driverID))
     {
       //System.out.println("Problem with check date!");
       return false;
     }
       
     //System.out.println("The request is valid");
     return true;  
   }//validateRequest
   
   //For a given interval, check if each date in the interval has
   //been booked by less than 10 drivers. 
   //Written by: Oak. Last modified: 17/02/13
   public static boolean checkDateInterval(GregorianCalendar dateFrom, GregorianCalendar dateTo, int driverID)
   {
     GregorianCalendar currentCal = new GregorianCalendar
      (dateFrom.YEAR, dateFrom.MONTH, 
      dateFrom.DAY_OF_MONTH, 0, 0, 0);
     
     GregorianCalendar currentDate = new GregorianCalendar(dateFrom.get(Calendar.YEAR), dateFrom.get(Calendar.MONTH), 
                                 dateFrom.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
     do
     {
       //Check if more than 9 drivers inavailable and the holiday isn't 
       //already taken by this driver
       if(dateAvailable(currentDate, driverID) != 1) 
       {
         return false;  
       }
       
       currentCal.add(Calendar.DATE, 1);
       currentDate.set(currentCal.get(Calendar.YEAR), currentCal.get(Calendar.MONTH), currentCal.get(Calendar.DATE));
 
     }while(!currentDate.after(dateTo));//While not after
     return true;
   }//checkDateInterval
   
   //Check if a given date is possible to be a driver holiday
   //0 = Not available, 1 is Available, 2 driver has a holiday on that date  
   //Written by: Oak. Last modified: 17/02/13
   public static int dateAvailable(GregorianCalendar givenDate, int driverID)
   {   
     int[] driverIDs = DriverInfo.getDrivers();
     
     Date newGivenDate = new Date(givenDate.getTimeInMillis());
     
     //System.out.println("Checking: " + currentDate.getDate());
     if(!DriverInfo.isAvailable(driverID, newGivenDate))
     {
         systemMsg.message = "You cannot request a holiday on this date:"
                             + givenDate.DATE + "/" +
                            givenDate.MONTH + "/" + 
                            givenDate.YEAR + 
                             " as it alrready is your holiday";
         return 2;
     }
     
     int notAvailable = 0;
     for (int i=0; i<driverIDs.length; i++)
     {
       if(!(DriverInfo.isAvailable(driverIDs[i], newGivenDate)))
       {
         notAvailable++; 
         if(notAvailable > 9)
             break;
       }
     }//for
     if(notAvailable > 9) 
     {
       systemMsg.message =("Date: " + givenDate.get(Calendar.DATE) + "/" +
                               givenDate.get(Calendar.MONTH) + "/" + 
                               givenDate.get(Calendar.YEAR) + 
                               " is NOT available");
       return 0;
     }//if
     else 
     {
       return 1;
     }//else
   }//dateAvailable
   
   //Check if the requested holiday length is valid
   //Written by: Oak. Last modified: 17/02/13
   public static boolean validateHolidayLength
     (GregorianCalendar dateFrom, GregorianCalendar dateTo, int driverID, boolean overTheYear)
   {
     int interval = calculateInterval(dateFrom, dateTo);
     //System.out.println("Holiday length = " + interval);
     if(interval < 0)
     {
         systemMsg.message = "Your start date is after your finish date";
         return false;
     }
     int maxHoliday;
     if(!overTheYear)
     {
       //How many days does the driver have left for his holiday
       maxHoliday = 25 - DriverInfo.getHolidaysTaken(driverID);
     }//if
     else
     {
       maxHoliday = 25;
     }//else
     
     //System.out.println("Max Holiday = " + maxHoliday);
     if(interval > maxHoliday)
     {
       systemMsg.message = 
         ("You have requested " + interval + 
         (interval > 1? "days ": "day ") + "for " + dateTo.YEAR +
          ", while the maximum is " + maxHoliday + 
          (maxHoliday > 1? "days ": "day "));    
       return false;
     }
     return true;
   }//validateHolidayLength
   
   //Written by: Nathan. Last modified: 18/02/13
   public static DateTime gregorianCalendarToDateTime(GregorianCalendar date)
   {
      String dateString = date.toString();
      //System.out.println(dateString);
      String dateStringArray[] = dateString.split(" ", 6);
      //System.out.println(dateStringArray[1] + dateStringArray[2] + dateStringArray[5]);
       int newMonth = date.get(Calendar.MONTH);
       int newDay = date.get(Calendar.DATE);
       int newYear = date.get(Calendar.YEAR);
      return new DateTime(newYear, newMonth, newDay, 0, 0);
   }
   
   //Written by: Nathan. Last modified: 18/02/13
   public static int calculateInterval(GregorianCalendar dateFrom, GregorianCalendar dateTo)
   {
       if(dateFrom.equals(dateTo))
       {
           return 1;
       }
       
       DateTime dateTimeFrom = gregorianCalendarToDateTime(dateFrom);
       DateTime dateTimeTo = gregorianCalendarToDateTime(dateTo);
       // return days between + 1 becasue want to include the dateFrom and dateTo
       // in the interval
       return Days.daysBetween(dateTimeFrom, dateTimeTo).getDays() + 1;
   }  
 }//class
