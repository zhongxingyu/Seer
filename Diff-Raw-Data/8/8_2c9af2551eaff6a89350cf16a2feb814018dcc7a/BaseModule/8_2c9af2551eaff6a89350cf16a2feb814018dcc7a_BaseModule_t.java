 package main.java.org.elasticsearch.sorting.nativescript.script;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class BaseModule {
 	public static Date parse_date(String s){
 		Date d = new Date();
		try {			
			d = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(s);
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();			
 		}	
		return d;		
 	}
 }
