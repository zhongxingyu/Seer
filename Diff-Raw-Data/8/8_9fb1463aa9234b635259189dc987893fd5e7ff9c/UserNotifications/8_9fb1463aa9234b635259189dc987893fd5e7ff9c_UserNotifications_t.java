 package serverTCP;
 
 import common.NotificationInfo;
 import common.rmi.RemoteNotifications;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.MalformedURLException;
 import java.net.Socket;
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: joaonuno
  * Date: 10/21/13
  * Time: 3:29 PM
  * To change this template use File | Settings | File Templates.
  */
 public class UserNotifications extends Thread
 {
 	protected static final long timeToSleep = 500; //In milliseconds.
 
 	//Socket and streams.
 	protected Socket clientSocket;
 	protected ObjectOutputStream outStream;
 
 	protected boolean shutdown = false;
 	protected int userID;
 
 	protected RemoteNotifications notifications;
 
 	public UserNotifications(Socket cSocket)
 	{
 		clientSocket = cSocket;
 
 		//Create output stream.
 		try {
 			outStream = new ObjectOutputStream(clientSocket.getOutputStream());
 		} catch (IOException ie) {
 			System.out.println("[Notifications] Could not create input and output streams:\n" + ie);
 		}
 
 		//Bind RMI object.
         String rmiAddress = "rmi://"+ServerTCP.rmiServerAddress+":"+ServerTCP.rmiRegistryPort+"/";
         try {
 			notifications = (RemoteNotifications) Naming.lookup(rmiAddress + "Notifications");
 		} catch (MalformedURLException mue) {
 			System.out.println("Wrong URL passed as argument:\n" + mue);
 			System.exit(-1);
 		} catch (NotBoundException nbe) {
 			System.out.println("Object is not bound:\n" + nbe);
 			System.exit(-1);
 		} catch (RemoteException re) {
 			System.out.println("Error looking up remote objects:\n" + re);
 			System.exit(-1);
 		}
 	}
 
 	@Override
 	public void run()
 	{
 		System.out.println("Notifications thread started.");
 
 		ArrayList<NotificationInfo> nots;
 		while(!shutdown)
 		{
 			try {
 				nots = notifications.getNotifications(userID);
 
 				for(int i=0; i < nots.size(); i++)
 					outStream.writeObject(nots.get(i).text);
 				outStream.flush();
 
 				notifications.removeNotifications(nots);
 
 				Thread.sleep(timeToSleep);
 			} catch (EOFException e) {
 				System.out.println("[Notifications] Client disconnected.");
 				shutdown = true;
 			} catch (Exception e) {
 			}
 		}
 	}
 
 	/**
 	 * Set userID.
 	 * @param userID The value to set.
 	 */
 	public void setUserID(int userID)
 	{
 		this.userID = userID;
 	}
 }
