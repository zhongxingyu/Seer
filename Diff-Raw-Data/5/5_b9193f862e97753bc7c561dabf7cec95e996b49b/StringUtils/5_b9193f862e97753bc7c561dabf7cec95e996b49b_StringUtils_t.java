 package se.jkrau.bukkit.secretword.utils;
 
 import se.jkrau.bukkit.secretword.settings.Configuration;
 
 public class StringUtils {
 	
 	private static String[] alpha = ("a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,0,1,2,3,4,5,6,7,8,9").split(",");
 	private static String[] evil = ("!,@,#,$,%,^,&,*,(,),|,},{,:,?,>,<,/,*,-,+," + '\247' + "," + '\u00a7').split(",");
 	
 	public static boolean checkUsername(String username) {
 		username = username.toLowerCase();
 		boolean hasallowedchars = false;
 		
		// first, check if it's empty.
		if (username.trim().length() == 0) {
			return false;
		}
 		
 		for (String e : evil)
 			if (username.contains(e)) return false;
 		
 		for (String a : alpha)
 			if (username.contains(a) && !hasallowedchars) hasallowedchars = true;
 		
 		Configuration.log("So, username " + username + " was " + Boolean.toString(hasallowedchars));
 		
 		return hasallowedchars;
 	}
 	
 }
