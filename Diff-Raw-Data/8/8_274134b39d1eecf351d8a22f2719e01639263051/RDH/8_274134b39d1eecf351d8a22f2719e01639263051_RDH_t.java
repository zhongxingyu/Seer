 package ibms;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /* This class needs */
 
 /*
  
  4.	A completely working system for UC1 is defined as follows:
  a.	Its interface is interactive: it asks for the input and displays the output, and its display is understandable, clear, neat and near professional.
  b.	Given the driver ID as the input, the interface shows the number of holidays that the driver has.
  c.	The driver selects specific dates for the holidays and the interface will display one of the two outcomes: (1) holidays granted; (2) holidays rejected due to invalid dates. 
  d.	The above operations should be repeatable without crashing the system.
  
  */
 
 /*
  Driver logs into roster system.
  Driver gives notice about upcoming holiday.
  Enough notice is given.
  Request is confirmed and allowed.
  Driver logs out of system.
  */
 
 /*
  RULES
  1. A roster is generated for each week based upon the timetable for that week.
  2. The maximum driving time for any driver in any one day is 10 hours.
  3. There can be no more than 50 hours driven by any one driver in any one week.
  4. A driver can drive for a maximum of 5 hours at any one time and must have a break of at least one hour. Breaks can only be taken at the bus depot.
  5. A driver shift consists of one period of up to 5 hours driving time, or two such periods with a 1 hour break between them.
  6. Time spent with the bus whilst not actually moving counts as driving time for the driver (that is, while responsible for the bus).
  7. There is a sufficiency of fueled buses available for the roster.
  8. If a bus is available, it is available for the whole day.
  9. A driver may specify up to two resting days for each week in which they will not be available for work. (We assume not all drivers will choose the same two days.)
  */
 
 /*
  RELATED RULES
  10. Drivers can normally take 25 days of holidays a year – this is in addition to the two resting days a week they specified.  
  11. For any weekdays, the maximum number of drivers who can request the same holidays is 10 and the request is approved by the company management in the order of first-come-first –served.
  During Sundays and public holidays when fewer buses are in operation then it is possible for more than 10 drivers to request the holidays for the same period. This information should be available from the database provided. 
  12.	If a driver requests holidays, he or she should specify the intended starting date and the finishing date.
  */
 
 /*
  Basic workflow
  //Prompt user for driver ID
  //Display holidays driver has at the moment
  //Allow user to request new holidays 
  //Take start date and end date as input
  //Holiday is accepted or rejected due to invalid dates
  
  //Don't crash and allow go back to start
  */
 
 
 /**
  * Class used with a CLI to request a driver holiday
  * Takes driver ID and requests holidays
  */
 public class RDH {
   
   // Buffered reader to grab input
   static BufferedReader input =
                new BufferedReader(new InputStreamReader(System.in));
   static int maxHolidays = 25;
   static int minDrivers = 10;
   
   public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
   
   /**
    * Read a line of input from STDIn
    */
   public static String readLine() {
     String string = "";
     try {
       string = input.readLine();
     }
     catch (IOException e) {
       System.out.println("Error with STD IN"); 
       System.exit(1);
     }
     return string;
   }
   
   /**
    * Print out information to the user (alias)
    */
   private static void println(String message) {
     System.out.println(message);
   }
 
   /**
    * Print out information to the user (alias)
    */
   private static void print(String message) {
     System.out.print(message);
   }
   
   private static boolean submitHolidayRequest(int currentUserId, Date start, Date end) {
     if (start == null || end == null){
       System.out.println("Please choose a start and an end first!");
       return false;
     }
     
         
         
           //10?
         //mark holiday taken
     
     //calclate holiday lenth
     int holidayLength = 0;
     
     println("Taking an extra "+holidayLength+" days of holiday!");
     
     //check holiday legth + current holidays not bigger than toal holidays
     if(holidayLength+DriverInfo.getHolidaysTaken(currentUserId) > maxHolidays) {
       println("You trying to book more than the max of "+maxHolidays
               + "days of holiday this year!");
       println("Please choose a shorter holiday");
       return false;
     }
     
     //check their are are enough drivers over the period
       //by checking every day
     int totalDrivers = DriverInfo.getTotalDrivers();
     int availableDrivers = 0;
     Date compare = (Date) start.clone(); //make sure it's not a pointer
     while(compare.compareTo(end)<=0) {
       //THIS NEEDS CORRECTING...
       availableDrivers = DriverInfo.getNoOfUnavailableDrivers(compare);
       
       if(totalDrivers-availableDrivers<10) {
         println("Too many drivers have taken holidays on these dates...");
         return false;
       }
       
       //Move one forward
       compare.setDate(compare.getDate()+1);
     }
     
     //They are allowed to take the holiday
     compare = (Date) start.clone(); //make sure it's not a pointer
     while(compare.compareTo(end)<=0) {
       println("\nBooked date "+compare);
       DriverInfo.setAvailable(currentUserId, compare, false); //mark busy
       compare.setDate(compare.getDate()+1);
     }
     
     DriverInfo.setHolidaysTaken(currentUserId,
             holidayLength+DriverInfo.getHolidaysTaken(currentUserId));
     println("Your holidays have been approved!");
     
     return true;
   }  
   
   private static Date chooseDate(boolean endDate, Date start, Date end) {
     println("Enter "+(endDate ? "end" : "start")+" date (YYYY-MM-DD) :");
     String givenDate = readLine();
 
     try {
       if(endDate)
         end = (Date)dateFormat.parse(givenDate);
       else
         start = (Date)dateFormat.parse(givenDate);
       
       if(start != null&&end != null) {
         if(!(start.before(end))) {
           if(endDate)
             println("The start date should be before the end date!");
           else
             println("The end date should be after the start date!");
           println("Please choose again... ");
           return null;
         } //if
       } // if
     }
     catch (ParseException e) {
         System.out.println("Invalid date");
         return null;
     } // catch
     
     //If we got here the date is valid
     if(endDate)
       return end;
     else
       return start;
   }
   
   /**
    * Process for requesting a holiday
    */
   private static void requestHoliday(int currentUserId) {
     //read start date 0 - validate etc...
         //read end date - validate etc…
         //complete
         //exit
     boolean requestHolidayDone = false;
    Date start = null;
    Date end = null;
     do {
 
       println("Current request:");
       print("\tHoliday from "+(start==null ? "-" : dateFormat.format(start))+"");
       println("\tto "+(end==null ? "-" : dateFormat.format(end))+".\n");
       
       println("Please choose your option:");
       
       println("\t1: Select start date");
       println("\t2: Select end date");
       println("\t3: Submit holiday request");
       println("\t0: Cancel request");
 
       try {
         //Read user input
           String userInput = readLine();
           println(" "); //just be tidy
           int userOption = Integer.parseInt(userInput);
 
         //Switch on request
           switch(userOption) {
               case 1: //choose start date
                   start = chooseDate(false, start, end);
                   break;
               case 2: //choose end date
                   end = chooseDate(true, start, end);
                   break;
               case 3: //submit request
                   requestHolidayDone = submitHolidayRequest(currentUserId, start, end);
                   break;
               case 0: //exit
                   println("Returning to the main menu!\n");
                   requestHolidayDone = true;
                   break;
               default:
                   println("That isn't a valid option, please try again!\n");
           }
 
       }
       catch(NumberFormatException e) {
           println("You can only enter a number here...\n");
       }
       
     } while (!requestHolidayDone); 
   } //requestHoliday
   
   /**
    * Display the amount of days of holiday
    */
   private static void displayHolidaysLeft(int driverID) {
     println("You have " + (maxHolidays - DriverInfo.getHolidaysTaken(driverID))
                         + " days remaining\n");
   }
 
   /**
    * Display all current holidays
    */  
   private static void displayHolidays(int currentUserId) {
     try {
       //List all the holidays a driver has taken already
       GregorianCalendar gcal = new GregorianCalendar();
       SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
       Date start = sdf.parse("2010.01.01");
       Date end = sdf.parse("2010.01.14");
       gcal.setTime(start);
       while (gcal.getTime().before(end)) {
           gcal.add(Calendar.DAY_OF_YEAR, 1);
           System.out.println( gcal.getTime().toString());
       }
     } catch (ParseException ex) {
       println("That doesn't look like a date?!?");
     }
    //throw new UnsupportedOperationException("Not yet implemented");
   } 
   
   /**
    * @param args the command line arguments
    */
   public static void main(String[] args) {
       database.openBusDatabase();
      
       /*int[] driverIDs = DriverInfo.getDrivers();
         String[] driverNames = new String [driverIDs.length];
         for (int i=0; i<driverIDs.length; i++)
             System.out.println(driverIDs[i]);*/
       
       //Greet       
       println("=RDS H14="); 
       println("Welcome to the H14 request driver holiday system\n"); 
        
       //while not done loop
       boolean gotUser = false;
       int currentUserId = 0;
       do {
         //Take driver ID
         println("Please enter your driver id: ");
         String driverId = readLine();
         println("");
         
         try {
           int id = Integer.parseInt(driverId);
           int[] drivers = DriverInfo.getDrivers();
           
           boolean validId = false;
           for (int i = 0; i < drivers.length; i++) {
             if(id == drivers[i]) {
               currentUserId = drivers[i];
               break;
             } // if
           } // for
           
           //Search for driver ID
           if(currentUserId==0) {
               //Driver doesn't exist error and try again
               println("That ID was not found in the database, please try again");
           } else {
               //Found driver continue
               println("Thanks for logging in "+DriverInfo.getName(currentUserId));
               gotUser = true;
           } //driver id valid
         }
         //Catch none number
         catch (NumberFormatException e) {
           println("Your ID must be a number"); 
         }
        
       }
       while(!gotUser);
       
       println(" "); //just be tidy
       
       boolean done = false;
       do {  
         //Create data Format
           println("Please choose an option:");
           
           //Request holidays
           println("\t1: Request a holiday");
           
           //View holidays
           println("\t2: Display my current holidays");
           
           //Check Days Left
           println("\t3: Query days of holiday left");
           
           //Quit
           println("\t0: Quit");
           
           println("\nEnter your option: ");
 
         try {
           //Read user input
             String userInput = readLine();
             println(" "); //just be tidy
             int userOption = Integer.parseInt(userInput);
             
           //Switch on request
             switch(userOption) {
                 case 1: //request
                     requestHoliday(currentUserId);
                     break;
                 case 2: //display holidays
                     displayHolidays(currentUserId);
                     break;
                 case 3: //days of holiday left
                     displayHolidaysLeft(currentUserId);
                     break;
                 case 0: //exit
                     println("\nYou are now logged out!");
                     done = true;
                     break;
                 default:
                     println("That isn't a valid option, please try again!\n");
             }
  
         }
         catch(NumberFormatException e) {
             println("You can only enter a number here...\n");
         }
       }
       while(!done);
       
       println("Thank you for using our system");
       
   } // main
 } //RDH
