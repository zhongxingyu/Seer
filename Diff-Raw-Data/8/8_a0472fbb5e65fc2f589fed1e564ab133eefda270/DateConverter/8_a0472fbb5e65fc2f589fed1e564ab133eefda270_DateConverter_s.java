 package ch.rollis.emma.util;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 import java.util.TimeZone;
 
 /**
  * The DateConverter class provides static methods to convert date objects into
  * string representations and date strings into date objects.
  * 
  * @author mrolli
  */
 public final class DateConverter {
     /**
      * Simple date formatter for dates in rfc1123 date format.
      */
     private static SimpleDateFormat rfc1123;
 
     /**
      * Simple date formatter for dates in log date format.
      */
     private static SimpleDateFormat logFormat;
 
     static {
         rfc1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
         rfc1123.setTimeZone(TimeZone.getTimeZone("GMT"));
 
         logFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
     }
 
     /**
      * Private constructor to deny class instantiation.
      */
     private DateConverter() {
     };
 
     /**
      * Converts a date object to a rfc1123 conform date string.
      * 
      * @param date
      *            The date to convert
      * @return The string representation in rfc1123 date format
     * @return
      */
     public static String formatRfc1123(final Date date) {
         return rfc1123.format(date);
     }
 
     /**
      * Converts a date object to a date string in log format.
      * 
      * @param date
      *            The date to convert
      * @return The string representation in log format
      */
     public static String formatLog(final Date date) {
         return logFormat.format(date);
     }
 
     /**
      * Converts a date string into a java.util.date object.
      * 
      * @param date
      *            The date string to convert
      * @return Date object
      * @throws DateConverterException
      *             In case the conversion leads to error states
      */
     public static Date dateFromString(final String date) throws DateConverterException {
         try {
             return rfc1123.parse(date);
         } catch (ParseException e) {
             // do nothing
         }
 
         try {
             return logFormat.parse(date);
         } catch (ParseException e) {
             // do nothing
         }
 
         // no conversion was possible
         throw new DateConverterException("Unable to parse date " + date);
     }
 }
 
