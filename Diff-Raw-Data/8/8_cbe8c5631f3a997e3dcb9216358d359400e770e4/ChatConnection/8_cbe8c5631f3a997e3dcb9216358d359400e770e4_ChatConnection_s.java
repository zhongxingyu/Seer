 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 /**
  * ChatConnection represents a client's individual connection to the Chat Server
  *
  * Holds the clients private messages queue
  */
 public class ChatConnection extends Thread {
     private static final String WELCOME_FILE = "./welcome.txt";
 
     // Queue of messages
     private Queue<String> privMessages;
 
     // current msg client is on
     private int currentMsg;
 
     // Socket to client
     private Socket client;
 
     // inputStream from socket is read into this byte array
     byte[] b;
 
     // exiting flag
     boolean exiting;
     // Has client logged in?
     private boolean loggedIn;
 
     // name of client
     private String name;
 
     // Use InputStream for non-blocking IO call available
     private InputStream inputStream;
 
     // output writer to client
     private PrintWriter output;
     
     // client id
     private int cid;
     
     // reference to MessageDaemon
     private MessageDaemon md;
     
     // reference to database connection
     private DBConnection db;
 
     // grab input and output streams from Socket
     public ChatConnection(Socket s, int id, MessageDaemon m, DBConnection dbc) {
         // privMessages is a queue that stores a user's private messages.
         privMessages = new LinkedList<String>();
         // a reference to the MessageDaemon, used for communicating between clients
         md = m;
         // set the current message for this client to the current size of the messages 
         //  so client only gets messages sent, since they logged in.
         currentMsg = md.getMessagesSize();
         exiting = false;
         // a reference to the Database Connection object.
         db = dbc;
         client = s;
         
         try {
             inputStream = client.getInputStream();
             output = new PrintWriter(client.getOutputStream(), true);
             cid = id;
         } catch (IOException ie) {
             ie.printStackTrace();
         }
     }
 
     public void run() {
         String received = null;
         int bytes;
         int maxbytes=1024;
         
         try {
             BufferedReader reader = new BufferedReader(new FileReader(WELCOME_FILE));
             String line = "";
             while((line = reader.readLine() ) != null) {
                 privMessages.add(line);
             }
             reader.close();
         } catch (IOException ioe) {
             System.err.println("File not found.");
         }
 
         do {
             received = "";
             try {
                 if(inputStream.available() > 2) {
                     byte[] buf = new byte[ inputStream.available() ];
                     inputStream.read( buf );
                     received = new String(buf);
                     received = received.trim();
                 } 
             } catch (IOException ie) {
                 //inputStream.close();
                 ie.printStackTrace();
             }
 
             if(received.length() > 1) {
                 // DEBUG echo received msg
                 System.out.println("received from " + cid + ": " + received);
                 // process the msg
                 processMsg(received);
             }
 
             // check for private message
             checkPrivateMessages();
 
             if (loggedIn == true) {
                 // check send all messages
                 checkAllMessages();
             }
             
             // check if exiting has been set
             if(exiting == true) {
                 try {
                     client.close();
                     inputStream.close(); 
                 } catch (Exception e) {
                 }
             }
             
             // sleep interval, do I need this??
             try {
                 sleep(50);
             } catch (InterruptedException ine) {
             }
         } while (exiting == false);
     }
    
 
     public void processMsg(String inStr) {
         int space;
         String cmd;
         String receiver = "";
         // need to refactor code to use args, instead of reusing inStr
         String args = "";
 
         // is this a 1 word command?
         space = inStr.indexOf(' ');
         if(space > 0) {
             // this is a command with arguments
             cmd = inStr.substring(0,space);
             inStr = inStr.substring(space+1);
         } else { 
             // this is a command without arguments
             cmd = inStr;
             inStr = "";
         }
 
         cmd = cmd.toUpperCase();
 
         // Send a message to login, if not logged in
         if (loggedIn == false && !cmd.equals("LOGIN") && !cmd.equals("REGISTER")) {
             privMessages.add("Please Login first.");
             // DEBUG
             System.out.println("command is: " + cmd);
         } else {
             //System.out.println("logged in status: " + loggedIn);
             if (cmd.equals("WHO")) {
                 callWho();
             } else if (cmd.equals("SENDALL")) {
                 sendMsg(inStr);
             } else if (cmd.equals("SEND")) {
                 // break off name of receiver
                 space = inStr.indexOf(' ');
                 if(space > 0) {
                     receiver = inStr.substring(0,space);
                     inStr = inStr.substring(space+1);
                     if(receiver.equals("all")) {
                         sendMsg(inStr);
                     } else {
                        addPrivMsg(receiver + " (from you): " + inStr);
                         md.privateMsg(inStr, name, receiver);
                     }
                 } else {
                     // if there is just one word, send as a send all message
                     sendMsg(inStr);
                 }
             } else if (cmd.equals("LOGIN")) {
                 login(inStr);
             } else if (cmd.equals("LOGOUT")) {
                 logout();
             } else if (cmd.equals("REGISTER")) {
                 space = inStr.indexOf(' ');
                 if(space > 0) {
                     String u = inStr.substring(0,space);
                     String p = inStr.substring(space+1);
                     register(u,p);
                 } else {
                     privMessages.add("Usage: register username password");
                 }
             } else {
                 // Send a message to user, telling them command is unrecognized.
                 privMessages.add("Could not recognize command: " + cmd);
                 // LOG message
                 System.out.println("don't recognize msgType: " + cmd + ", with string: " + inStr);
             }
         }
     }
 
     // client has asked to log out, 
     // disconnect connection and send a message to all.
     private void logout() {
         if(client != null) {
             System.out.println("Closing down connection with " + cid);
         }
 
         // start message to all with SENDALL msgtype
         md.addMsg(name + " has left the chat room.");
 
         // remove this chatClient from MessageDaemon
         md.removeChat(this);
         exiting = true;
     }
 
     // Send Message to all users
     public void sendMsg(String msg) {
         // have Message Daemon add msg to queue
         md.addMsg(name + ": " + msg);
     }
 
     // Add message to the Chat Connection's private message Queue.
    // TODO this should be synchronized, both MessageDaemon and ChatConnection
     //   are updating the privMessages queue.
    public void addPrivMsg(String inStr) {
         privMessages.add(inStr);
     }
 
     // Return Chat client's name.
     public String getClientName() {
         return name;
     }
    
     public void checkPrivateMessages() {
         String msg;
         // while queue has more private message, write them to socket
         while((msg = privMessages.poll())!= null) {
             output.println(msg);
         }
     }
 
     public void checkAllMessages() {
         // check if there is a new message
         // TODO Change to while and test.
         if(md.getMessagesSize() > currentMsg ) {
             currentMsg++;
             System.out.println("checking message #" + currentMsg);
             output.println(md.getMessage(currentMsg));
         }
     }
 
     public void register(String username, String password) {
         if(db.registerUser(username,password) == true) {
             privMessages.add(username + " was successfully registered.");
             privMessages.add("Please log in.");
         } else {
             privMessages.add(username + " is already registered.");
             privMessages.add("Please choose another name or log in with old account.");
         }
     }
     // Get a List of people in the chat room.
     public void callWho() {
         String whoList = md.who();
         output.println(whoList);
     }
 
     // Log in to the Chat Room.
     public void login(String inStr) {
         // message: login username password
         if(inStr.length() < 1) {
             privMessages.add("Please provide a login name and password.");
             return;
         }
         StringTokenizer st = new StringTokenizer(inStr);
         // drop command, actually that's already chopped off
         // st.nextToken();
         String inName = st.nextToken();
         if(!st.hasMoreElements()) {
             privMessages.add("Please provide login name and password.");
             return;
         }
         String inPassword = st.nextToken();
         // TODO: check if already logged in!
         if(md.checkLoggedIn(inName)) {
            privMessages.add("Someone is already logged in with that account.");
            return;
         }
 
         // check that password is correct
         if(db.checkPassword(inName,inPassword)) {
             name = inName;
             privMessages.add("Login successful.");
             System.out.println(name + " has successfully logged in.");
             md.addMsg(name + " has logged in.");
             loggedIn = true;
         } else {
             privMessages.add("Login failed: please try again or you'll be reported to the Authoritay.");
             System.out.println(inName + " failed to log in with password: " + inPassword);
         }
     }
 
     // check if password is valid
     public boolean checkPassword(String name, String pass) {
         return true;
     }
 
 }
 
