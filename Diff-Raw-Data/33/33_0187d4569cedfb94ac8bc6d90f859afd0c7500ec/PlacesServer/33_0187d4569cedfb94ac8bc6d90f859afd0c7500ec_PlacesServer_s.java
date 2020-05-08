 import java.rmi.*;
 import java.rmi.server.*;
 /*
  * Server for the Places search. This just sets up the RMI server.
  */
 public class PlacesServer{
   public static void main(String args[]) {
     System.setProperty("java.security.policy","policy");
        //TODO: make port optional.
    if (args.length != 1) {
      System.err.println("usage: java PlacesServer rmi_port");
       System.exit(1);
     }
     // Create and install a security manager
     if (System.getSecurityManager() == null)
       System.setSecurityManager(new RMISecurityManager());
     try {
      int port = Integer.parseInt(args[0]);
       String url = "//localhost:" + port + "/PlacesSearch";
       Naming.rebind(url, new PlacesSearch());
       System.out.println("server " + url + " is running...");
     }
     catch (Exception e) {
       System.err.println("PlacesSearch server failed:" + e.getMessage());
             e.printStackTrace();
     }
   }
 }
