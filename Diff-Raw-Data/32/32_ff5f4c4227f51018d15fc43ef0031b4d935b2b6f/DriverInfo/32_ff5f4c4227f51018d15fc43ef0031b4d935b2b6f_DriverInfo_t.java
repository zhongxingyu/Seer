 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ibms;
 
 
 import java.util.*;
 import java.text.*;
  
 /**
  * Class which represents information about drivers and their availability.
  * It also allows the application to get and set the number of hours a
  * driver has worked over time periods of a week or a year, and the
  * holidays taken by a driver in the current calendar year.<br><br>
  * 
  * As well as an ID, drivers (like buses) have numbers which are traditionally
  * used to identify them. You can also get the name of a specified driver<br><br>
  * 
  * The methods contain checks for invalid queries, which will result in
  * InvalidQueryExceptions being thrown, but it does not enforce "business
  * rules" such as checking for dates in the past.
  */
 public class DriverInfo 
 {
  public static int getNoOfUnavailableDrivers(Date i) {
      if (i == null) throw new InvalidQueryException("Date is null.");
         
      database db = database.busDatabase;
      DateFormat format = new SimpleDateFormat("dd-mm-yyyy");
 
      return db.getResult("driver_availability",  "day = '" + format.format(i) + "'");
  }
 
  public static int getTotalDrivers() {
      database db = database.busDatabase;
      return db.getResult("driver", null);
  }
 
  public static ArrayList getUnavailableDates(int driversID) {
      database db = database.busDatabase;
      return db.getResult("day", "driver_availability", "driver = '" + driversID + "' and available = '0'");
  }
   
   // This class is not intended to be instantiated
   private DriverInfo() 
   { 
        super();
   }
 
   /**
    * Get the IDs of all the drivers in the database
    */
   public static int[] getDrivers()
   {
     return database.busDatabase.select_ids("driver_id", "driver", "name");
   }
 
   /**
    * Find the driver with the specified driver number
    */
   public static int findDriver(String number)
   {
     return database.busDatabase.find_id("driver", "number", number);
   }
 
   /**
    * Get the real name of a driver
    */
   public static String getName(int driver)
   {
     if (driver == 0) throw new InvalidQueryException("Nonexistent driver");
     return database.busDatabase.get_string("driver", driver, "name");
   }
 
   /**
    * Get the number of days holiday taken, or planned to be taken, in the
    * current calendar year
    */
   public static int getHolidaysTaken(int driver)
   {
     if (driver == 0) throw new InvalidQueryException("Nonexistent driver");
     return database.busDatabase.get_int("driver", driver, "holidays_taken");
   }
 
   /**
    * Set the number of days holiday taken, or planned to be taken, in
    * the current calendar year.
    */
   public static void setHolidaysTaken(int driver, int value)
   {
     if (driver == 0) throw new InvalidQueryException("Nonexistent driver");
     database.busDatabase.set_value("driver", driver, "holidays_taken", value);
   }
 
   /**
    * Get the number of hours worked by a driver so far this calandar year
    */
   public static int getHoursThisYear(int driver)
   {
     if (driver == 0) throw new InvalidQueryException("Nonexistent driver");
     return database.busDatabase.get_int("driver", driver, "hours_this_year");
   }
 
   /**
    * Set the number of hours worked by a driver so far this calandar year
    */
   public static void setHoursThisYear(int driver, int value)
   {
     if (driver == 0) throw new InvalidQueryException("Nonexistent driver");
     database.busDatabase.set_value("driver", driver, "hours_this_year", value);
   }
 
   /**
    * Get the number of hours worked by a driver during the period of a
    * weekly roster
    */
   public static int getHoursThisWeek(int driver)
   {
     if (driver == 0) throw new InvalidQueryException("Nonexistent driver");
     return database.busDatabase.get_int("driver", driver, "hours_this_week");
   }
 
    /**
    * Set the number of hours worked by a driver during the period of a
    * weekly roster
    */
   public static void setHoursThisWeek(int driver, int value)
   {
     if (driver == 0) throw new InvalidQueryException("Nonexistent driver");
     database.busDatabase.set_value("driver", driver, "hours_this_week", value);
   }
 
   /**
    * Get the identification number of a driver
    */
   public static String getNumber(int driver)
   {
     if (driver == 0) throw new InvalidQueryException("Nonexistent driver");
     return database.busDatabase.get_string("driver", driver, "number");
   }
 
   /**
    * Determine whether a driver is available on a given date
    */
   public static boolean isAvailable(int driver, Date date)
   {
     if (date == null) throw new InvalidQueryException("Date is null");
     if (driver == 0) throw new InvalidQueryException("Nonexistent driver");
     database db = database.busDatabase;
     if (db.select_record("driver_availability", "driver", driver, "day", date))
       return (Integer)db.get_field("available") != 0;
     else
       return true;
   }
   
   /**
    * Determine whether a driver is available today
    */
   public static boolean isAvailable(int driver)
   {
     return isAvailable(driver, database.today());
   }
 
   /**
    * Set whether a driver is available on a given date
    */
   public static void setAvailable(int driver, Date date, boolean available)
   {
     if (date == null) throw new InvalidQueryException("Date is null");
     if (driver == 0) throw new InvalidQueryException("Nonexistent driver");
     if (available && !isAvailable(driver, date))
       database.busDatabase.delete_record("driver_availability", "driver", driver, "day", date);
     else if (!available && isAvailable(driver, date))
       database.busDatabase.new_record("driver_availability", new Object[][]{{"available", false}, {"day", date}, {"driver", driver}});
   }
 
   /**
    * Set whether a driver is available today
    */
   public static void setAvailable(int driver, boolean available)
   {
     setAvailable(driver, database.today(), available);
   }
 
 }
