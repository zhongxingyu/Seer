 package nl.surfnet.bod.util;
 
 import java.sql.Timestamp;
 
 import org.hibernate.search.bridge.StringBridge;
 import org.joda.time.DateTime;
 
 /**
  * Handles parsing of timestamps to a String so it can be searched. Needed since
  * BoD uses joda timestamps and sql time stamps.
  * 
  */
 public class TimeStampBridge implements StringBridge {
 
   @Override
   public String objectToString(Object object) {
     String result = null;
 
     if (object == null) {
       // null
     }
     else if (DateTime.class.isAssignableFrom(object.getClass())) {
       DateTime dateTime = (DateTime) object;
 
       // Remove timezone offset
       String dtString = dateTime.toString();
      int timeZoneCharPos = dtString.indexOf("+");
       result = dtString.substring(0,  timeZoneCharPos);
     }
     else if (Timestamp.class.isAssignableFrom(object.getClass())) {
       Timestamp timestamp = (Timestamp) object;
       result = timestamp.toString();
     }
     else {
       throw new IllegalArgumentException("Bridge is not suitable for handling objects of type: " + object);
     }
 
     return result;
   }
 
 }
