 package mud.network.server.log;
 
 import java.text.SimpleDateFormat;
 import java.util.GregorianCalendar;
import java.util.TimeZone;
 
 /**
  * A class whose only purpose is to be used with its static log() method to
  * generate timestamps.
  *
  * @author Japhez
  */
 public abstract class ConsoleLog {
 
     static final GregorianCalendar calendar = new GregorianCalendar();
    static final SimpleDateFormat format = new SimpleDateFormat("HH:mm");
 
     public static String log() {
         return "[" + format.format(calendar.getTime()) + "] ";
     }
 }
