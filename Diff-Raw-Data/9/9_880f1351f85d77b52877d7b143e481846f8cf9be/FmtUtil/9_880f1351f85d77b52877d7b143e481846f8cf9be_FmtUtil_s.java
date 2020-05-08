 package no.kantega.android.afp.utils;
 
import java.math.RoundingMode;
import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 /**
  * This class handles formatting and validation of input and output values
  */
 public class FmtUtil {
 
     /**
      * Convert the given date to string using the given format
      *
      * @param format Date format to use when converting
      * @param date   Date to convert
      * @return The date
      */
     public static String dateToString(String format, Date date) {
         final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
         return simpleDateFormat.format(date);
     }
 
     /**
      * Format currency according to the default locale
      *
      * @param number Number to format
      * @return Formatted currency
      */
     public static String currency(double number) {
         return currency(number, Locale.getDefault());
     }
 
     /**
      * Format currency according to the given locale
      *
      * @param number Number to format
      * @param locale Locale to use when formatting
      * @return Formatted currency
      */
     public static String currency(double number, Locale locale) {
         final NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
         nf.setMinimumFractionDigits(2);
         nf.setMaximumFractionDigits(2);
         return nf.format(number);
     }
 
     /**
      * Format currency without prefix using the default locale
      *
      * @param number Number to format
      * @return Formatted currency
      */
     public static String currencyWithoutPrefix(double number) {
         return currencyWithoutPrefix(number, Locale.getDefault());
     }
 
     /**
      * Format currency according to the given locale
      *
      * @param number Number to format
      * @param locale Locale to use when formatting
      * @return Formatted currency
      */
     public static String currencyWithoutPrefix(double number, Locale locale) {
        NumberFormat df = DecimalFormat.getInstance(locale);
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(2);
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(number);
     }
 
     /**
      * Trim useless data from the given transaction text
      *
      * @param text Text to trim
      * @return Trimmed text
      */
     public static String trimTransactionText(String text) {
         final String pattern = "(^\\d{16}\\s)?" +
                 "(\\d{2}\\.\\d{2}\\s)?" +
                 "([A-Z]{3}\\s\\d+,\\d+\\s)?" +
                 "(TIL\\:\\s)?" +
                 "(BETNR:\\s*\\d*)?";
         if (text != null) {
             String out = text.trim();
             out = out.replaceAll(pattern, "").trim();
             return out;
         } else {
             return "";
         }
     }
 
     /**
      * Convert the given string to date using the given format
      *
      * @param format Format to use when converting
      * @param date   Date to convert
      * @return The date
      */
     public static Date stringToDate(String format, String date) {
         final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
         try {
             return simpleDateFormat.parse(date);
         } catch (ParseException e) {
             return null;
         }
     }
 
     /**
      * Check if given string is a number with optional decimals
      *
      * @param s String to check
      * @return True if string contains one or more numbers
      */
     public static boolean isNumber(String s) {
         return s != null && s.matches("^\\d+([,\\.]\\d+)?$");
     }
 
     /**
      * Get first word in string split by space
      *
      * @param s String
      * @return First word
      */
     public static String firstWord(String s) {
         if (s != null) {
             final String[] words = s.split(" ");
             if (words.length > 0) {
                 return words[0];
             }
         }
         return "";
     }
 }
