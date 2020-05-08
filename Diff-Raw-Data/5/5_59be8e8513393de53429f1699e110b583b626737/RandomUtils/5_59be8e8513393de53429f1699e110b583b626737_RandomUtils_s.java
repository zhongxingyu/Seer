 package util;
 
 import java.util.Random;
 
 import edu.washington.cs.cse490h.lib.Utility;
 
 public class RandomUtils {
	private static Random random = new Random();
	
 	public static String randomString(int length)
 	{
 		StringBuffer sb = new StringBuffer();
 		
 	    for (int i = 0; i < length; i++)
 	    {
	    	sb.append((char)random.nextInt(26) + 'a');
 	    }
 	    
 	    return sb.toString();
 	}
 }
