 package models;
 
 import java.util.*;
 
 import play.db.ebean.*;
 import play.data.validation.Constraints.*;
 
 import java.util.ArrayList;
 
 
 public class Url {
 		@Required 
 		public String url;
 		
 		public String place;
 		public String title;
 		
 		public Url(){};
 		
 		public static String randomUrl(String[] urlArray){
				int size = urlArray.length;
 				Random rn = new Random();
 				int randomInt = rn.nextInt(size-1);
 				
				return urlArray[randomInt];
 				
 						
 			}
         
 }
