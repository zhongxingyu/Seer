 package fedora.client.purge;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.rmi.RemoteException;
 import javax.xml.rpc.ServiceException;
 
 import fedora.client.APIMStubFactory;
 import fedora.server.management.FedoraAPIM;
 import fedora.server.utilities.StreamUtility;
 
 public class AutoPurger {
 
     private FedoraAPIM m_apim;    
 
     public AutoPurger(String host, int port, String user, String pass) 
             throws MalformedURLException, ServiceException {
         m_apim=APIMStubFactory.getStub(host, port, user, pass);
     }
 
     public void purge(String pid, String logMessage) throws RemoteException, IOException {
         purge(m_apim, pid, logMessage);
     }
 
     public static void purge(FedoraAPIM skeleton, String pid, String logMessage) 
             throws RemoteException, IOException {
         skeleton.purgeObject(pid, logMessage); 
     }
 
     public static void showUsage(String errMessage) {
         System.out.println("Error: " + errMessage);
         System.out.println("");
         System.out.println("Usage: AutoPurger host port username password pid logMessage");
     }
 
     public static void main(String[] args) {
         try {
            if (args.length!=6) {
                 AutoPurger.showUsage("You must provide six arguments.");
             } else {
                 String hostName=args[0];
                 int portNum=Integer.parseInt(args[1]);
                 String pid=args[4];
                 String logMessage=args[5];
                 AutoPurger a=new AutoPurger(hostName, portNum, args[2], args[3]);
                 a.purge(pid, logMessage);
             }
         } catch (Exception e) {
             AutoPurger.showUsage(e.getClass().getName() + " - " 
                 + (e.getMessage()==null ? "(no detail provided)" : e.getMessage()));
         }
     }
 
}
