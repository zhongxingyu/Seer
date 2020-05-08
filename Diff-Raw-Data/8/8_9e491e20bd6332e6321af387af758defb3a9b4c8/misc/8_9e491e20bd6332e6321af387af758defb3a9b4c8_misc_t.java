import java.util.Date;
import java.text.*;

 public class misc {
 	public static void log( String message ) {
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat ("yyyy.MM.dd hh:mm:ss:SSS");		
		System.err.println( "[" + ft.format(dNow) + "] " + message );
 	}
 }
