 package domain;
 
 public class JsonResponse {
 	
 	public static String getCount(int count){
 		return String.format("{\"count\": %d}", count);
 	}
 	
 	public static String getCount(int count, boolean isLoggedIn){
		return String.format("{\"count\":%d, \"loggedIn\":\"%s\"}", count, isLoggedIn ? "true" : "false" );
 	}
 	
 }
