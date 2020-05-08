 import java.rmi.*;
 import java.rmi.server.*;
 import java.rmi.registry.LocateRegistry;
 
 public class Server {
 
     public static void main(String[] args) {
         if( args.length != 1 ) {
             System.out.println("Usage: java <PORT>");
             System.exit(-1);
         }
         try {
            int port = Integer.parseInt(args[0]);
            LocateRegistry.createRegistry(port);
            Naming.rebind("rmi://127.0.0.1:"+args[0]+"/Remote", new RemoteListener());
         } catch(Exception e) {
             e.printStackTrace();
         }
         
     }
 
 }
