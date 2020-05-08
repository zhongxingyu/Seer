 package de.uni_hamburg.informatik.sep.zuul;
 
 import java.net.MalformedURLException;
 import java.rmi.AlreadyBoundException;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.util.Observable;
 
 import de.uni_hamburg.informatik.sep.zuul.client.ClientConsole;
 import de.uni_hamburg.informatik.sep.zuul.client.ClientGUI;
 import de.uni_hamburg.informatik.sep.zuul.client.oberflaeche.gui.StartFenster;
 import de.uni_hamburg.informatik.sep.zuul.server.Server;
 
 public class StartUp
 {
 
 	private StartFenster _startFenster;
 
 	public static void main(String args[]) throws RemoteException,
 			AlreadyBoundException, NumberFormatException,
 			MalformedURLException, NotBoundException
 	{
 		new Server();
 
 		if(args.length == 5 && args[4].equals("console"))
 		{
 			new ClientConsole(args[0], args[1], Integer.parseInt(args[2]),
 					args[3]);
 		}

		new StartFenster();
 
 	}
 
 }
