 package backend;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ArrayBlockingQueue;
 
 /**
  * Server for the realtime collaborative editor.
  * 
  * Thread-safe argument
  * --------------------
  * Edits are are sent over sockets to the server and are added to the
  * EditQueue so that they are dealt with one by one. The Edits are not
  * inserted using indexes; instead, they are inserted using the Cursor's
  * location for that client. This means that an edit will always be 
  * inserted correctly into the ServerDocument even if its index changes 
  * in between when it is sent to the server and when it is actually added
  * to the ServerDocument. Additionally, Edits belong to the original 
  * client until that client moves the Cursor or presses space or newline.
  * This helps narrow down the case where clients will try to edit right on
  * top of each other. They still type right next to each other,
  */
 public class Server {
     private ServerSocket serverSocket = null;
 //    private int numUsers;
     private final EditController editCont;
     private static Map<String, ServerDocument> docList  = new HashMap<String, ServerDocument>();
     private final int CAPACITY = 500;
     private ArrayList<Socket> socketList;
     // TODO: implement things like flooding the socketList with all messages
 
     /**
      * Makes a Server that listens for connections on port.
      * @param port The port number, requires 0 <= port <= 65535
      * @throws IOException 
      */
     public Server (int port) throws IOException {
         serverSocket = new ServerSocket(port);
         editCont = new EditController(new ArrayBlockingQueue<String>(CAPACITY), docList);
         socketList = new ArrayList<Socket>();
     }
 
     /**
      * Runs the server, listening for client connections and handling them.
      */
     private Socket socket;
     public void serve() throws IOException {
         while (true) {
             System.out.println("youre before socket");
             // block until a client connects
             System.out.println(serverSocket.toString());
             socket = serverSocket.accept();
             socketList.add(socket);
             System.out.println("youve accepted the socket");
             // makes threads
             Thread clientThread = new Thread(new Runnable() {
                 public void run() {
                     try {
                         handleConnection(socket);
                     } catch (IOException e) {
                         e.printStackTrace();
                     } finally {
                         try {
                             System.out.println("preclose");
                             socket.close();
                             System.out.println("postclose");
                             // not sure if needed:
                             //serverSocket.close();
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
                     }
                 }
                 
                 /**
                  * Handles a single client connection. Returns when client disconnects.
                  * @param socket socket where the client is connected
                  * @throws IOException if connection has an error or terminates unexpectedly
                  */
                 private void handleConnection(Socket socket) throws IOException {
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                     try {
                         for (String line =in.readLine(); line!=null; line=in.readLine()) {
                             System.out.println("beginning of handleConnection for loop");
                             System.out.println("input from GUI: " + line);
                             String output = editCont.putOnQueue(line);
                             System.out.println("output from server: " + output);
                             if(output != null) {
                                 //out.println(output);
                                 //out.flush();
 
                                 String[] outTokens = output.split(" ");
                                 
                                 /**
                                  * Floods update messages to the sockets with messages according to the following:
                                  * If something was successful: send the original client the success message and send
                                  * the rest of the clients an update message.
                                  * If something was unsuccessful: update messages to all of the clients including the 
                                  * original one.
                                  * If something is a message that only the original client cares about, send the message to
                                  * that client only.
                                  */
                                 
                                 if (outTokens[0].equals("InvalidInput")) {
                                     // do nothing, skip this loop for indexing's sake
                                 } else if (outTokens[2].equals("new") || outTokens[2].equals("getDocNames") || 
                                         outTokens[2].equals("checkNames") || outTokens[2].equals("save") || 
                                         outTokens[2].equals("open") || outTokens[2].equals("cursorMoved")) {
                                     // These are outgoing messages that only the original client cares about
                                     out.println(output);
                                     out.flush();
                                 } else if (outTokens[3].equals("success")) {
                                     // These are for successful inserts, removes, and spaceEntereds
                                     out.println(output);
                                     out.flush();
 
                                     // The following is an update message
                                     // Output: clientName docName update lines content
                                     String linesAndContent = docList.get(outTokens[1]).getDocContent();
                                     String update = outTokens[0] + " " + outTokens[1] + " update " + linesAndContent;
 
                                     for (int i = 0; i < socketList.size(); i++) {
                                         Socket s = socketList.get(i);
                                         if (!s.equals(socket)) {
                                             PrintWriter tempOut = new PrintWriter(s.getOutputStream(), true);
                                             tempOut.println(update);
                                             tempOut.flush();
                                             tempOut.close();
                                         }
                                     }
                                     
                                 } else if (outTokens[3].equals("fail")) {
                                     // These are for unsuccessful inserts, removes, and spaceEntereds
                                     
                                     // The following is an update message
                                     // Output: clientName docName update lines content
                                     String linesAndContent = docList.get(outTokens[1]).getDocContent();
                                     String update = outTokens[0] + " " + outTokens[1] + " update " + linesAndContent;
                                     
                                     for (int i = 0; i < socketList.size(); i++) {
                                         Socket s = socketList.get(i);
                                         if (!s.equals(socket)) {
                                             PrintWriter tempOut = new PrintWriter(s.getOutputStream(), true);
                                             tempOut.println(update);
                                             tempOut.flush();
                                             tempOut.close();
                                         } else {
                                             out.println(update);
                                             out.flush();
                                         }
                                     }
                                 }
                                 
                                 
                               // TODO: make this return for more cases than just save.
 //                                if (output.equals("save EndEditDone")) {
 //                                    return;
 //                                } 
                             }
                         } 
                     } finally { 
                         out.close();
                         in.close();
                         System.out.println("closed");
                         // check this line if multithreading is wrong
                        socketList.remove(socket);
                     }
                 }
             });
             clientThread.start();
         }
     }
     
     
     /**
      * Start a Server running on the default port (4444).
      * 
      * The server runs. It waits for clients to connect to the correct port using
      * sockets. It calls handleConnection to deal with the socket of each client, and 
      * handleConnection gives the input to the editController to put on the 
      * EditQueue. The EditQueue deals with messages in order so that everything is 
      * threadsafe.
      */
     public static void main(String[] args) {
         // Always uses the same port. Clients connect their GUIs
         // to this port and the host's IP address.
         final int port = 4444;
         try {
             Server server = new Server(port);
             server.serve();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
