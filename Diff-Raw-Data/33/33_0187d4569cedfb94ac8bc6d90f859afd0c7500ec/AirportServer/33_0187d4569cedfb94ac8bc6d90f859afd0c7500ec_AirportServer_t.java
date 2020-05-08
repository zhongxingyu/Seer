 import java.rmi.*;
 import java.rmi.server.*;
 
 /*
  * Server for the Airport search. This just sets up the RMI server.
  */
 
 public class AirportServer {
   public static void main(String args[]) {
     System.setProperty("java.security.policy","policy");
    int port = 1099;
    if (args.length == 1 ) {
      port = Integer.parseInt(args[0]);
    } else if (args.length > 1) {
      System.err.println("usage: java AirportServer <rmi_port>");
       System.exit(1);
     }

     // Create and install a security manager
     if (System.getSecurityManager() == null)
       System.setSecurityManager(new RMISecurityManager());
     try {
       String url = "//localhost:" + port + "/AirportSearch";
       Naming.rebind(url, new Airports());
       System.out.println("server " + url + " is running...");
     }
     catch (Exception e) {
       System.err.println("Airport server failed:" + e.getMessage());
             e.printStackTrace();
      System.exit(1);
     }
   }
 }
