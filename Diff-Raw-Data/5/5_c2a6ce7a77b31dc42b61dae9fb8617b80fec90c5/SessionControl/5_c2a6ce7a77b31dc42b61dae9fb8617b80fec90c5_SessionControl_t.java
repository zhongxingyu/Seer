 package swsec;
 
 import javax.servlet.http.*;
 
 public class SessionControl {
 	
 	private final static int sessionshort = 5;
 	private final static int sessionormal = 30;
 	private final static int sessionlong = 60;
 	
 	/**TRUE if the session is expired
 	 * FALSE if is not expired*/
	public static boolean isExpiredAdmin(HttpSession session)
 	{
		if((session.getAttribute("user")==null || session.getAttribute("user")=="") && session.getAttribute("admin")!="yes")
 		{	
 			return true;
 		}	
 		return false;
 	}
 	
 	/*timeout of 5 seconds*/
 	public static int getSessShort()
 	{
 		return sessionshort;
 	}
 	/* timeout of30 second*/
 	public static int getSessNormal()
 	{
 		return sessionormal;
 	}
 	/*timeout of 60 second*/
 	public static int getSessLong()
 	{
 		return sessionlong;
 	}
 
 }
