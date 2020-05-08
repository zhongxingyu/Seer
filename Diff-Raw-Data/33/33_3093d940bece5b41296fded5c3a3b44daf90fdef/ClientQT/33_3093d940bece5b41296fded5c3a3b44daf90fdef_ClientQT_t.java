 package clientQT;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import util.CardGameInfo;
 
 import action.Message;
 
 import com.trolltech.qt.gui.QApplication;
 
 import clientData.Client;
 import clientData.GameSession;
 import clientData.ServerListener;
 
 /**
  * Client with QT-based GUI
  * 
  * @author mahogny
  *
  */
 public class ClientQT extends Client implements ServerListener
 	{
 	public Set<Integer> boardViewsExistFor=new HashSet<Integer>();
 	
 	@Override
 	public void eventNewGameSessions()
 		{
 		for(final Map.Entry<Integer, GameSession> e:gameSessions.entrySet())
 			{
 			if(e.getValue().joinedUsers.contains(getClientID()))
 				{
 				if(!boardViewsExistFor.contains(e.getKey()))
 					{
 					boardViewsExistFor.add(e.getKey());
 			
 					QApplication.invokeAndWait(new Runnable() {
 					public void run() {
 						BoardWindow boardWindow = new BoardWindow(ClientQT.this, e.getKey());
 						ClientQT.this.serverListeners.add(boardWindow); //TODO on close, unregister
 						boardWindow.show();
 					}
 					});
 
 
 					}
 				}
 			}
 		// TODO Auto-generated method stub
 		
 		}
 
 	@Override
 	public void eventNewUserList()
 		{
 		// TODO Auto-generated method stub
 		
 		}
 
 	@Override
 	public void eventServerMessage(Message msg)
 		{
 		// TODO Auto-generated method stub
 		
 		}
 
 	
 
 	/**
 	 * Entry point
 	 */
 	public static void main(String args[])
 		{
 		QApplication.initialize(args);
 		
 		
 		ClientQT client=new ClientQT();
 		
 		client.serverListeners.add(client);
 
 				
 		LobbyWindow lobbyWindow = new LobbyWindow(client);
 		client.serverListeners.add(lobbyWindow); //TODO on close, unregister
 		lobbyWindow.show();
 
 		//temp
		/*
 		BoardWindow boardWindow = new BoardWindow(client, 666);
 		client.serverListeners.add(boardWindow); //TODO on close, unregister
		boardWindow.show();*/
 /////
 
 		client.createServer();
 
 		int ca=0;
 		while(ca<args.length)
 			{
 			if(args[ca].equals("--connect"))
 				{
 				//Connect to a server
 				ca++;
 				String url=args[ca];
 				int port=CardGameInfo.defaultServerPort;
 				int colon=url.lastIndexOf(":");
 				if(colon!=-1)
 					{
 					port=Integer.parseInt(url.substring(colon+1));
 					url=url.substring(0, colon);
 					}
 				
 				try
 					{
 					InetAddress addr=InetAddress.getByName(url);
 					client.connectToServer(addr, port);
 					}
 				catch (UnknownHostException e)
 					{
 					System.out.println("Unknown host "+url);
 					System.exit(1);
 					}
 				catch (IOException e)
 					{
 					e.printStackTrace();
 					System.exit(1);
 					}
 				}
 			ca++;
 			}
 
 
 		QApplication.exec();
 		}
 
 	}
