 package networking;
 
 import java.io.*;
 import java.net.*;
 
 import networking.NetworkMessage.Type;
 
 /**************************************************************************
  * 
  * @author 
  *
  *  The ClientHandler class is a Thread which handles all I/O with the
  *  clients. Each client will be associated with a different ClientHandler.
  *  The first line the ClientHandler reads is the client's username.
  *  Any subsequent lines are what the user is typing to the chatroom.
  *  When the client exits, you must call Server.removeClient() to inform
  *  the server that the client is no longer connected.  
  **************************************************************************/
 public class ClientHandler extends Thread {
     private Host server;
 
     //private PrintWriter out;
     //private BufferedReader in;
     private ObjectOutputStream writer;
     private ObjectInputStream reader;
     public InetAddress ip;
 
     String username;
     int id;
 
     /***************************************************************************
      * Initialize 'server' and 'clientSocket', and create the input and output
      * streams using the socket's getInputStream() and getOutputStream() functions.
     ****************************************************************************/
     public ClientHandler(Host serv, Socket clientSock) throws IOException {
         /*TODO*/
         //System.out.println("Setting up handler");
     	server = serv;
     	ip = clientSock.getInetAddress();
     	//out = new PrintWriter(clientSock.getOutputStream());
     	//in = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
     	writer = new ObjectOutputStream(clientSock.getOutputStream());
     	reader = new ObjectInputStream(new BufferedInputStream(clientSock.getInputStream()));
     }
 
     /**************************************************************************
      * Send a message to the client.
      * @param message
      **************************************************************************/
     public void send(NetworkMessage message) {
         /*TODO*/
         if (message != null) {
             //System.out.println("Handler sending: " + message);
             //out.println(message);
             //out.flush();
             try {
                 writer.writeObject(message);
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
     }
 
     /********************************************************************
      * Sign the client off.
      * Here you have to:
      * - Send a sign off message
      * - Close the input and output streams.
      * - Remove the client from the server.
       *******************************************************************/
     public void signOff() {
         /*TODO*/
     	//out.write(id + " has signed off.");
     	//out.close();
    	server.broadcastMessage(new ChatMessage(id, username + "just left the Brainstrom!\n", "Host"), this);
     	try {
             writer.close();
             reader.close();
 			//in.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	server.removeClient(this);
     }
 
     /***********************************************************************************************
      * Read messages and broadcast them to everybody else.
      *  In a while loop:
      *  - Read a line of input using readLine().
      *  -Use the Server to broadcast it.
      *  -Remember the first message is always the username
      *  Note: If readLine() throws an exception or returns null, this means that either something
      *   went wrong or the client signed off. In either case, you want to sign off and stop the
      *   thread.  
      **********************************************************************************************/  
     public void run() {
         //System.out.println("Running new handler");
         //String message;
         NetworkMessage message;
         /*TODO*/
         while(true) {
         	try {
         	    //System.out.println("Waiting to read");
         	    //System.out.flush();
         	    //username = in.readLine();
 				//message = in.readLine();
 				//System.out.println(message);
         	    message = (NetworkMessage) reader.readObject();
         	    if (message.sender_id == -1) {
         	        //We need to issue this client an id
         	        System.out.println("server: message has no id");
         	    } else {
         	        System.out.println("server: message has id: " + message.sender_id);
         	    }
 				//if (message != null && username != null) {
         	    if (message != null) {
         	        if (message.type == Type.HANDSHAKE) {
         	            server.respondHandshake((Handshake) message, this);
         	        } else {
         	            //server.broadcastMessage(username + ": " + message);
         	            server.broadcastMessage(message, this);
         	        }
 				}
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				System.out.println("server: client <" + id + ", " + username + "> disconnected");
 				this.signOff();
 				break;
 			} catch (ClassNotFoundException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
     }
 
 	public void setUsernameAndId(String _username, int temp) {
 		username = _username;
 		id = temp;
 	}
 }
