 package com.evanhoffman.sunrise;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 public class SunPositionTester {
 	
 	public static void main(String args[]) {
 		System.out.println(" = "+Math.PI);
 		MapCoordinate mineola = new MapCoordinate("Mineola",40.738675, -73.645687);
 		MapCoordinate jfk = new MapCoordinate("JFK Airport",40.64465, -73.78296);
 		// http://transition.fcc.gov/mb/audio/bickel/DDDMMSS-decimal.html
 		// JFK Airport:
 		// LAT: 		40 38' 40.7394"
 		// LONGITUDE: 	-73 46' 58.656"
 
 		System.out.println("Position: "+jfk);
 //		Date d = new Date();
 
 		
 //		TimeZone tz = TimeZone.getTimeZone("America/New_York");
 		Calendar cal = Calendar.getInstance();
 //		cal.setTimeZone(tz);
 		cal.set(Calendar.YEAR, 2012);
 		cal.set(Calendar.MONTH, Calendar.JANUARY);
 		cal.set(Calendar.DAY_OF_MONTH, 1);
 		cal.set(Calendar.HOUR_OF_DAY, 12);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		
 		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
 		
 		System.out.println("Date\tElevation\tAzimuth");
 //		SunPosition positions[] = new SunPosition[24];
		for (int i = 0; i < 24; i++) {
 			cal.set(Calendar.HOUR_OF_DAY, i);
 			Date d = cal.getTime();
 //			d = new Date(d.getTime() + tz.getOffset(d.getTime()));
 			SunPosition p = new SunPosition(d,jfk);
 			System.out.println(df.format(d)+"\t"+p.getElevation()+"\t"+p.getAzimuth());
 //			System.out.println("Date "+d);
 //			System.out.println("Sun at Azimuth: "+p.getAzimuth()+", elevation: "+p.getElevation()+"\n--");
 		}
 	}
 }
