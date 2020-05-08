 /*
  * Authour: Joshua Abbott feb 2013
  *  A class to represent a Holiday
  */ 
 import java.util.*;
 import static java.util.Calendar.*;
 import org.joda.time.MutableDateTime;
 import org.joda.time.Days;
 import org.joda.time.IllegalFieldValueException;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 public class Holiday extends RuntimeException
 {
   //Instance variables for a holiday
   private int noOfDays;
   private MutableDateTime startDate;
   private MutableDateTime endDate;
   
   //Holiday constructor
   public Holiday(int reqStartDay, int reqStartMonth, int reqStartYear,
        int reqEndDay, int reqEndMonth, int reqEndYear)
   {
     try{
     startDate = new MutableDateTime(reqStartYear, reqStartMonth, reqStartDay,0,0,0,0);
     endDate = new MutableDateTime(reqEndYear, reqEndMonth, reqEndDay,0,0,0,0);
     noOfDays = calcNoDays(startDate, endDate);
     daysAway();
     }
     catch(IllegalFieldValueException e)
     {
       throw new HolidayException(e.getMessage());
     }
   }//constructor
   
   //Blank Constructor
   public Holiday()
   {
   
   }//constructor
   
   //Calculates the no of days
   private int calcNoDays(MutableDateTime startDate, MutableDateTime endDate)
   {
   	int posNoOfDays = Days.daysBetween(startDate, endDate).getDays();
   	if(posNoOfDays < 0)
   	{
   	  System.out.println("Your start date must be before your end date");
   		throw new HolidayException("Your start date must be before your end date");
   	}
   	else if(posNoOfDays == 0)
   	  throw new HolidayException("You must select more than 0 days");
   	else
     	return Days.daysBetween(startDate, endDate).getDays();
   }//calcNoOfDays
   
   //returns noOfDays
   public int getNoOfDays()
   {
     return noOfDays;
   }//getNoOfDays
   
   //returns startDate
   public MutableDateTime getStartDate()
   {
     return startDate;
   }//getStartDate
   
   //returns endDate
   public MutableDateTime getEndDate()
   {
     return endDate;
   }//getEndDate
   
   //Sets the start date and re calculates no of days
   public void setStartDate(int reqStartDay, int reqStartMonth, int reqStartYear)
   {
     this.startDate.setDate(reqStartYear, reqStartMonth, reqStartDay);
     this.noOfDays = calcNoDays(this.startDate, this.endDate);
   }//setStartDate
   
   //Sets the end date and re calculates no of days
   public void setEndDate(int reqEndDay, int reqEndMonth, int reqEndYear)
   {
     this.endDate.setDate(reqEndYear, reqEndMonth, reqEndDay);
     this.noOfDays = calcNoDays(this.startDate, this.endDate);
   }//setStartDate
   
   //returns the number of days untill the start date
   public int daysAway()
   {
   	int theDaysAway = Days.daysBetween(MutableDateTime.now(), this.startDate).getDays();
   	if(theDaysAway < 7)
   		throw new HolidayException("Your holiday must start at least 7 days away");
   	else 
       return Days.daysBetween(MutableDateTime.now(), this.startDate).getDays(); 
   }//daysAway
   
   //Checks if there is already 10 people on hilday on any day of the given holiday
   private void hasDaysReachedMax()
   {
     int noNotAvailable;
     MutableDateTime currentDay = this.startDate;
     //loop over days of holiday
     for(int i = 0; i < this.noOfDays; i++)
     {
       noNotAvailable = 0;
       int[] driverIDs = DriverInfo.getDrivers();
       for (int j=0; j<driverIDs.length; j++)
       {
         if(!DriverInfo.isAvailable(driverIDs[j],currentDay.toDate()))
         {
           noNotAvailable++;
         }//if
       }//for
       if(noNotAvailable > 10)
       {
       	Calendar calDay = currentDay.toCalendar(Locale.ENGLISH);
         throw new HolidayException("Max people on holiday on "+ calDay.get(DAY_OF_MONTH) + "/" + calDay.get(MONTH) + "/" + calDay.get(YEAR));
       }//if
       currentDay.addDays(1);
     }//for
   }
   
   //Check if the person is already on holiday during the selected period
   public void checkIfOnHolidayAlready(int driverID)
   {
    MutableDateTime currentDay = this.startDate;
    System.out.println(this.startDate);
     //loop over days of holiday
    Calendar calDay=currentDay.toCalendar(null);
     for(int i = 0; i < this.noOfDays; i++)
     {
      if(!DriverInfo.isAvailable(driverID,currentDay.toDate()))
         {
      		calDay = currentDay.toCalendar(null);
         	throw new HolidayException("You are already off on that date");
       }//if
      currentDay.addDays(1);
     }//for
   }//checkIfOnHolidayAlready
   
   //Save the holiday to the DB
   public void saveToDB(int driverID)
   {
     try
     {
       this.hasDaysReachedMax();
       checkIfOnHolidayAlready(driverID);
      MutableDateTime currentDay = this.startDate;
       //loop over days of holiday
       for(int i = 0; i < this.noOfDays; i++)
       {
        DriverInfo.setAvailable(driverID, currentDay.toDate(), false);
        currentDay.addDays(1);
       }//for
       DriverInfo.setHolidaysTaken(driverID, DriverInfo.getHolidaysTaken(driverID) + this.getNoOfDays());
     }
     catch(HolidayException e)
     {
       throw new HolidayException(e.getMessage());
     }
     catch(InvalidQueryException e)
     {
       throw new InvalidQueryException(e.getMessage());
     }
   }//saveToDB
   
 }//Holiday
