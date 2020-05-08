 package net.daboross.bukkitdev.playerdata;
 
 import org.omg.CORBA.ACTIVITY_COMPLETED;
 
 /**
  *
  * @author daboross
  */
 public class IPLogin {
 
     private long time;
     private String ip;
 
     /**
      * The Constructor for an IPLogin. You CAN use null IP's if you don't know
      * what it is. If the IP is null it will be replaced with "Unknown".
      */
     public IPLogin(final long time, final String ip) {
         this.time = time;
         this.ip = ip == null ? "Unknown" : ip;
     }
 
     public IPLogin(long time) {
         this.time = time;
         this.ip = "Unknown";
     }
 
     public String ip() {
         return ip;
     }
 
     public long time() {
         return time;
     }
 
     /**
      * Gets an IPLogin from a string that has been gotten by IPLogin.toString().
      * THis will work over multiple JVM's, and will decode from a regular login
      * (just a long).
      */
     public static IPLogin fromString(final String coded) {
        String[] firstLast = coded.split("-");
         if (firstLast.length > 2) {
             return null;
         } else if (firstLast.length == 0) {
             return null;
         } else if (firstLast.length == 1) {
             long time;
             try {
                time = Long.parseLong(firstLast[0]);
             } catch (Exception e) {
                 return null;
             }
             return new IPLogin(time, null);
         } else if (firstLast.length == 2) {
             long time;
             try {
                 time = Long.parseLong(firstLast[0]);
             } catch (Exception e) {
                 return null;
             }
             String ip = firstLast[1];
             return new IPLogin(time, ip);
         } else {
             return null;
         }

     }
 
     /**
     * This returns a coded version of the IPLogin. It is in the form of
     * time+"-"+ip.
      */
     @Override
     public String toString() {
        return time + "-" + ip;
     }
 }
