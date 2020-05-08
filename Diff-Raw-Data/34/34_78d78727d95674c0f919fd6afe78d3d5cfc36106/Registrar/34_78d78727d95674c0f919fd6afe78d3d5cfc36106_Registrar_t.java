 package solomonserver;
 
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class Registrar extends UnicastRemoteObject implements IRegistrar {
     final private static Logger l = Logger.getLogger("com.cs151.solomon.server");
     
     /**
      * A simple string to challenge anyone calling an administrative interface
      */
     static String auth = "";
     
    public final static int PORT = 1097;
     
     /**
      * the singleton instance of this class
      */
     static private Registrar registrar = null;
     
     private Registrar() throws RemoteException 
     {
     }
     
     /**
      * Construct a remote object
      * 
      * @throws RemoteException 
      */
     private void startServer() throws RemoteException, IOException 
     {
         FileHandler fh = new FileHandler("solomon.log",false);
         fh.setLevel(Level.FINE);
         Logger.getLogger("com.cs151.solomon.server").addHandler(fh);
         
         ConsoleHandler ch = new ConsoleHandler();
         ch.setLevel(Level.FINE);
         Logger.getLogger("con.cs151.solomon.server").addHandler(ch);
         
         Logger.getLogger("com.cs151.solomon.server").setLevel(Level.FINE);
         
         l.entering("Registrar","startServer");
         
         // TODO: if not running, programmatically start RMIRegistry with 
         // java.rmi.registry.LocateRegistry.createRegistry()
 /*
         System.out.println("Server: in Registrar constructor");
         try {
             Registrar.setLog(System.out);
         } catch (Exception e) {
             System.out.println( "Server: problem directing error log: " + e);
         }
 */
         
         // register this server object in RMI registry
         try {
            
            // get the local RMI Registry instance
            l.log(Level.INFO,"acquirinig RMI Registry reference");
            Registry registry = null;
            try {
               registry = LocateRegistry.getRegistry( PORT );
            }
            catch (RemoteException ex) {
                l.log(Level.SEVERE,"error locating RMI Registry: ", ex );
                System.exit(1);
            }
             
             try {
                 UnicastRemoteObject.unexportObject(this, true);
             } catch (Exception e) {
                l.log(Level.WARNING,"error unexporting previous Solomon Registrar object",e);
             }
             
             IRegistrar stub = (IRegistrar)UnicastRemoteObject.exportObject(registrar,0);
                        
            l.log(Level.INFO,"UNbinding previous Solomon Registrar");
             try { registry.unbind("Registrar"); } catch (Exception e) {}
             
            l.log(Level.INFO,"binding Solomon Registrar");
             registry.bind("Registrar",stub); 
             // TODO: make this global string
             // TOD: rebind?
             
             l.log(Level.INFO,"Solomon Registrar v0.9.0 is up");
             
         } catch (Exception e) {
             l.log(Level.SEVERE,"fatal error during initialization",e);
         }
     }
     
     /**
      * Main entry point
      * 
      * @param args 
      */
 
     public static void main(String[] args) {
         
         // capture administrative password
         if (args.length>0)
             auth = args[0];
         
         // construct our singular existence
         try {
             registrar = new Registrar();
             registrar.startServer();
         } catch (Exception e) {
             System.out.println( "Server: error initializing: " + e );
         }
         
     }
 
     
     /**
      * Register this client
      * 
      * @param teamName
      * @return
      * @throws RemoteException 
      */
     @Override
     public IConnection register( String teamName, IResponse response ) 
             throws RemoteException 
     {
         l.entering("Registrar","register",teamName);
         
         String origin = "";
         try {
             origin = Registrar.getClientHost();
 //            Registrar.setLog(System.out);
         } catch (Exception e) {
             l.log(Level.SEVERE,"error getting client host address",e);
             return null;
         }
 
         
         Connection conn = new Connection( teamName, response, origin );
         l.log(Level.INFO,"registered",conn);
         
         // TODO fail here if ConnTable full
         ConnectionTable.getInstance().addPlayer(conn);
         IConnection iConn  = (IConnection) conn;
         return iConn;
     }
 
     /* ************************
      * ADMINISTRATION INTERFACE 
      * ************************ */
 
     @Override
     public IAdministrator getAdminInterface(String challenge) throws RemoteException {
         l.entering("Registrar","getAdminInterface");
         
         return new Administrator();
     }
 
 }
 
