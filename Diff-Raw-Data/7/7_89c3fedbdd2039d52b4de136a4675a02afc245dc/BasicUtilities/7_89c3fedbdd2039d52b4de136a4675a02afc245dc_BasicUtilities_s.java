 /*
  * Please refer to http://code.thejo.in/license/
  * for details about source code license.
  */
 
 package in.kote.ssf.util;
 
 import java.util.Random;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import in.kote.ssf.net.EndPoint;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 /**
  *
  * @author Thejo
  */
 public class BasicUtilities {
 
     /**
      * Given a host, check if it is the localhost
      *
      * @param host
      * @return true if the host is the localhost
      */
     public static boolean isHostLocalHost(String host)
     {
         //Check if it is localhost first
         //Hack required for Windows systems (?)
         if(host.equalsIgnoreCase("localhost") ||
                 host.equalsIgnoreCase("127.0.0.1")) {
             return true;
         }
 
         if(null == EndPoint.localHost) {
             return false;
         } else {
             return EndPoint.localHost.equalsIgnoreCase(host);
         }
     }
 
     /**
      * Given a string, returns the MD5 hash
      *
     * @param s String whose MD5 hash value is required
      * @return An MD5 hash string
      * @throws java.security.NoSuchAlgorithmException
      */
     public static BigInteger getMD5(String s) throws NoSuchAlgorithmException {
 
         MessageDigest m = MessageDigest.getInstance("MD5");
         m.update( s.getBytes(), 0, s.length() );
 
         //String md5 = new BigInteger(1, m.digest()).toString(16);
 
         return new BigInteger(1, m.digest());
     }
 
     /**
      * Given a range of integers, returns a random number in that range
      * (inclusive of the start of the range)
      *
      * @param start
      * @param end
      * @return
      */
     public static int getRandomNumberInRange(int start, int end) {
         Random r = new Random();
         return r.nextInt(end) + start;
     }
 
     /**
      * Calculate and return the amount of memory used by the JVM
      *
      * @return long - Memory usage in bytes
      */
     public static long getUsedMemory() {
         return Runtime.getRuntime().totalMemory() -
                 Runtime.getRuntime().freeMemory();
     }
 
     public static float getFreeMemoryPercentage() {
         return ( (float) Runtime.getRuntime().freeMemory() /
                 Runtime.getRuntime().totalMemory() ) * 100;
     }
 
     /**
      * Given a throwable exception, return the stack trace as a string.
      * 
     * @param t
     * @return
      */
     public static String getStackTraceAsString(Throwable t) {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         t.printStackTrace(pw);
         return sw.toString();
     }
 }
