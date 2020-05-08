 package fedora.server.utilities;
 
 /**
  * <p>Title: DateUtility.java</p>
  * <p>Description: A collection of utility methods for performing</p>
  * <p>frequently require tasks.</p>
  * <p>Copyright: Copyright (c) 2002</p>
  * <p>Company: </p>
  * @author Ross Wayland
  * @version 1.0
  */
 
 import java.text.SimpleDateFormat;
 import java.text.ParsePosition;
 import java.util.Calendar;
 import java.util.Date;
 
 public abstract class DateUtility
 {
   private static final boolean debug = true; //Testing
   private static final SimpleDateFormat formatter =
       new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
 
   /**
    * Converts a datetime string into and instance of java.util.Calendar using
    * the date format: yyyy-MM-ddTHH:mm:ss
    *
    * @param dateTime A datetime string
    * @return Corresponding instance of java.util.Calendar (returns null
    * if dateTime string argument is empty string or null)
    */
   public static Calendar convertStringToCalendar(String dateTime)
   {
     Calendar calendar = null;
    if (!(dateTime == null) && !dateTime.equalsIgnoreCase(""))
     {
       calendar = Calendar.getInstance();
       ParsePosition pos = new ParsePosition(0);
       Date date = formatter.parse(dateTime, pos);
       calendar.setTime(date);
     }
     return(calendar);
 
   }
 
   /**
    * Converts a datetime string into and instance of java.util.Date using
    * the date format: yyyy-MM-ddTHH:mm:ss
    *
    * @param dateTime A datetime string
    * @return Corresponding instance of java.util.Date (returns null
    * if dateTime string argument is empty string or null)
    */
   public static Date convertStringToDate(String dateTime)
   {
     Date date = null;
    if (!(dateTime == null) && !dateTime.equalsIgnoreCase(""))
     {
       ParsePosition pos = new ParsePosition(0);
       date = formatter.parse(dateTime, pos);
     }
     return(date);
   }
 
   /**
    * Converts an instance of java.util.Calendar into a string using
    * the date format: yyyy-MM-ddTHH:mm:ss
    *
    * @param calendar An instance of java.util.Calendar
    * @return Corresponding datetime string (returns null if Calendar
    * argument is null)
    */
   public static String convertCalendarToString(Calendar calendar)
   {
     String dateTime = null;
     if (!(calendar == null))
     {
       Date date = calendar.getTime();
       dateTime = formatter.format(date);
     }
     return(dateTime);
   }
 
   /**
    * Converts an instance of java.util.Date into a String using
    * the date format: yyyy-MM-ddTHH:mm:ss
    *
    * @param date Instance of java.util.Date
    * @return Corresponding datetime string (returns null if Date argument
    * is null)
    */
   public static String convertDateToString(Date date)
   {
     String dateTime = null;
     if (!(date == null))
     {
       dateTime = formatter.format(date);
     }
     return(dateTime);
   }
 
   /**
    * Converts an instance of java.util.Calendar into and instance
    * of java.util.Date.
    *
    * @param calendar Instance of java.util.Calendar
    * @return Corresponding instance of java.util.Date (returns null
    * if Calendar argument is null)
    */
   public static Date convertCalendarToDate(Calendar calendar)
   {
     Date date = null;
     if(!(calendar == null))
     {
       date = calendar.getTime();
     }
     return(date);
   }
 
   /**
    * Converts an instance of java.util.Date into an instance
    * of java.util.Calendar.
    *
    * @param date Instance of java.util.Date
    * @return Corresponding instance of java.util.Calendar (returns null
    * if Date argument is null)
    */
   public static Calendar convertDateToCalendar(Date date)
   {
     Calendar calendar = null;
     if (!(date == null))
     {
       calendar = Calendar.getInstance();
       ParsePosition pos = new ParsePosition(0);
       calendar.setTime(date);
     }
     return(calendar);
   }
 
   public static void main(String[] args)
   {
     String dateTimeString = "2002-08-22T13:58:06";
     Calendar cal = convertStringToCalendar(dateTimeString);
     System.out.println("DateString: "+dateTimeString+"\nConvertCalendarToString: "+cal);
     Date date = convertStringToDate(dateTimeString);
     System.out.println("\nDateString: "+dateTimeString+"\nConvertDateToString: "+
                        convertDateToString(date));
     System.out.println("\nCalendar: "+cal+"\nConvertCalendarToString: "+
                        convertCalendarToString(cal));
     System.out.println("\nDate: "+convertDateToString(date)+"\nConvertDateT0String: "+
                        convertDateToString(date));
     System.out.println("\nCalendar: "+cal+"\nConvertCalendarToDate: "+
                        convertDateToString(convertCalendarToDate(cal)));
     System.out.println("\nDate: "+convertDateToString(date)+
                        "\nConvertDateToCalendar: "+convertDateToCalendar(date));
   }
 }
