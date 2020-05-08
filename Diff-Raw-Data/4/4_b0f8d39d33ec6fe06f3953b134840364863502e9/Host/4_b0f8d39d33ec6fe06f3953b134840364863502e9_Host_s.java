 package networking;
 
 import java.io.*;
 import java.util.*;
 import java.net.*;
 
 import networking.NetworkMessage.Type;
 
 /**********************************************************
  *
  * @author 
  *
  *The entry point of your server back end. Takes in the
  *port # the server will listen on as a command line param.
  *Then listens on that port for connecting clients.
  **********************************************************/
 public class Host extends Thread{
     private int localport;               //The local port the server will listen on.
     private ServerSocket serverSocket;   //The socket the server will accept connections on.
 
     private static int START_UID = 0;
     private static final int ELTS_PER_CLIENT = 5000;
     private List<ClientHandler> clients; //The list of currently connected clients.
     private Queue<ClientInfo> recoveryPriority;
     public Client localClient;
     private int hostId = 0;
     
     private int openId;
 
     /**************************************************************************
      * The constructor must initialize 'serverSocket' and 'clients'.
      * @param localport - the port the server will listen on
      * @throws IOException
      **************************************************************************/
     public Host(int _localport, String username, Networking net) throws IOException {
     	localport = _localport;
     	serverSocket = new ServerSocket(localport);
     	clients = new LinkedList<ClientHandler>();
     	localClient = new Client("localhost", localport, username, net);
     	recoveryPriority = new LinkedList<ClientInfo>();
     	localClient.start();
     	openId = 1;
     }
     
     public boolean send(NetworkMessage nm) {
         return localClient.send(nm);
     }
     
     public boolean signOff() {
     	if(localClient.signOff())
     		return this.shutDown();
     	else
     		return false;
     }
     
     public NetworkMessage receive (Type t) {
         return localClient.receive(t);
     }
     
     public int getNextOpenId() {
         return openId++;
     }
     
     /***************************************************************************************
      * Accept connections and add them to the clients queue.
      * This function should, in a while loop:
      * - Accept a connection using ServerSocket.accept()
      * - Create a new ClientHandler, giving it the Socket that accept() returns.
      * - Add the ClientHandler to the clients list. This is so that
      * broadcastMessage() function will be able to access it.
      * - Start the ClientHandler thread so that it can begin reading any incoming messages.
       **************************************************************************************/
     public void run() {
         Socket clientSocket;
         ClientHandler handler;
         /*
         try {
 			serverSocket = new ServerSocket(localport);
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		*/
         /*TODO*/
         while(true)	{
         	try {
         	    System.out.println("server: waiting to accept client");
 				clientSocket = serverSocket.accept();
 				handler = new ClientHandler(this, clientSocket);
 				clients.add(handler);
 				System.out.println("server: starting new client handler");
 				handler.start();
 			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
 			}
         }
     }
 
     /***************************************************************************
      * Broadcast a message to everybody currently connected to the server by 
      * @param message - message to broadcast
      * looping through the clients list.
      * Warning: More than one ClientHandler can call this function at a time. 
     ***************************************************************************/
     public synchronized void broadcastMessage(NetworkMessage message, ClientHandler client) {
         /*TODO*/
         System.out.println("server: broadcasting msg: " + message);
     	for (ClientHandler ch: clients) {
     	    if (ch != client) {
     	        System.out.println("server: sending msg to client: " + ch.id);
     	        ch.send(message);
     	    }
     	}
     }
     
     public synchronized void respondHandshake(Handshake message, ClientHandler clientHandler) {
         if (message.sender_id == -1) {
         	String username = message.client_username;
             int temp = getNextOpenId();
             //System.out.println("server: replying to handshake with id: " + temp);
             clientHandler.setUsernameAndId(username, temp);
             START_UID += ELTS_PER_CLIENT;
             clientHandler.send(new Handshake(hostId, temp, username, START_UID));
             registerClient(clientHandler);
             System.out.println("server: registered client with id: " + temp + ", uname: " + username);
             broadcastMessage(new ChatMessage(temp, username + " just joined the Brainstrom!\n", "Host"), clientHandler);
         } else {
             System.out.println("server: client already has received an id ERROR");
         }
     }
 
     private void registerClient(ClientHandler ch) {
 		recoveryPriority.add(new ClientInfo(ch.ip, ch.id, ch.username));
 	}
 
 	/**************************************************************************
      * Remove the given client from the clients list.
      * Warning: More than one ClientHandler can call this function at a time. 
      * Return whether the client was found or not.
      **************************************************************************/
     public synchronized boolean removeClient(ClientHandler client) {
     	if (clients.contains(client)) {
     		return clients.remove(client);
     	}
         return false;
     }
     /* TODO: need to have this shut down gracefully */
     public boolean shutDown() {
     	try {
 			serverSocket.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	return true;
     }
 
     /*
     /**************************************************************************
      * This function just creates the Server and runs it.
      * @param args - the port you will be listening on
      ************************************************************************** /
     public static void main(String[] args) {
         if (args.length < 1) {
             System.out.println("usage: Server portnumber");
             return;
         }
 
         int port = Integer.parseInt(args[0]);
         Host s;
         try {
             s = new Host(port);
         }
         catch (IOException e) {
             System.err.println("Unable to start server: " + e);
             return;
         }
 
         s.run();
     }
     */
 }
