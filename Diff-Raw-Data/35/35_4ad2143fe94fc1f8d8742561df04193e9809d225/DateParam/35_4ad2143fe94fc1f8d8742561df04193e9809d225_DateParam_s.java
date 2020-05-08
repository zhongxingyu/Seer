 package com.nesscomputing.jersey.types;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.util.Objects;
 import java.util.regex.Pattern;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 
 /**
  * Simple Jersey date parameter class.  Accepts either milliseconds since epoch UTC or ISO formatted dates.
  * Will convert everything into UTC regardless of input timezone.
  */
 public class DateParam
 {
     private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+");
     private final DateTime dateTime;
 
     DateParam(DateTime dateTime)
     {
         this.dateTime = checkNotNull(dateTime, "null datetime").withZone(DateTimeZone.UTC);
     }
 
     public static DateParam valueOf(DateTime dateTime)
     {
         return new DateParam(dateTime);
     }
 
     public static DateParam valueOf(String string)
     {
         if (string == null) {
             return null;
         }
 
         if (NUMBER_PATTERN.matcher(string).matches()) {
             return new DateParam(new DateTime(Long.parseLong(string), DateTimeZone.UTC));
         } else {
             return new DateParam(new DateTime(string, DateTimeZone.UTC));
         }
     }
 
     /**
      * @return a DateTime if the parameter was provided, or null otherwise.
      */
     // This method is static so that you can handle optional parameters as null instances.
     public static DateTime getDateTime(DateParam param)
     {
         return param == null ? null : param.dateTime;
     }
 
     @Override
     public String toString()
     {
         return Objects.toString(dateTime);
     }
 }
