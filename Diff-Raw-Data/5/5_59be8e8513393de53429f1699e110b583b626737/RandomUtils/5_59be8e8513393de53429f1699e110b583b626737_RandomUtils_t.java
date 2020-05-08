 package util;
 
 import java.util.Random;
 
 import edu.washington.cs.cse490h.lib.Utility;
 
 public class RandomUtils {
 	public static String randomString(int length)
 	{
 		StringBuffer sb = new StringBuffer();
 		
 	    for (int i = 0; i < length; i++)
 	    {
	    	sb.append((char)Utility.getRNG().nextInt(26) + 'a');
 	    }
 	    
 	    return sb.toString();
 	}
 }
