 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Server;
 import java.io.*;
 import java.util.*;
 import java.net.*;
 
 /**
  *
  * @author OckhamsRazor
  */
 public class Client {
     class TextListener implements Runnable {
         
         public TextListener() {
         }
         
         @Override
         public void run() {
             try {
                 init();
                 while(true) {
                     _msg = _in.readUTF();
                     System.out.println(_msg);
 //                    _mainServer.printMsg(_msg);
                     parseMsg(_msg);
                 }
             }
             catch (IOException e) {
                 if( e instanceof SocketException ) {
                     _mainServer.kickUser(Client.this);
                 }
                 else {
                     System.out.println(e.toString());
                     e.printStackTrace();
                 }
             }
         }
     }
     
     private Socket _socket;
     private Server _mainServer;
     private DataInputStream _in;
     private DataOutputStream _out;
     private String _msg, _userName;//, _passwd;
     private int _clientID;
     private int _status;
     
     private HashSet<Chatroom> _rooms;
     
 //    private ArrayList<Thread> _threads;
     private HashSet<String> _blackList; // banned users' names
     
     public int _userColor;
     
     public Client(Server server, Socket socket, int id) {
         _socket = socket;
         _mainServer = server;
         _clientID = id;
         _status = 0;
         _rooms = new HashSet<Chatroom>();
         _userColor = java.awt.Color.BLACK.getRGB();
         try {
             _in = new DataInputStream( socket.getInputStream() );
             _out = new DataOutputStream( socket.getOutputStream() );
             
         }
         catch(IOException ex) {
             System.out.println("WHYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
 //            System.out.println(ex.toString());
 //            ex.printStackTrace();
         }
 //        }
     }
     
     public void runAll() {
         Thread textListener = new Thread(new TextListener());
         textListener.start();
     }
     
     public void init() throws IOException {
         String[] tokens;
         _out.writeUTF("\001EST\000\004");
         while( true ) {
             tokens = _in.readUTF().split("\000");
             if (tokens.length > 1 && tokens[tokens.length-1].equals("\004")) {
                 if (tokens[0].equals("\001LOGIN")) {
                     assert tokens.length == 4; // 001-account-passwd-004
                     System.out.println("login");
                     
                     if( _mainServer.getUsers().contains(tokens[1]) ) {
                         System.out.println("has name");
                         String passwd = _mainServer.getPasswd(tokens[1]);
                         if (passwd != null && passwd.equals(tokens[2])) {
                             System.out.println("login success");
                             break; // TODO
                         }
                         break;
                     }
                     
                     System.out.println("login failed");
                     System.out.println(tokens[1]);
                     System.out.println(tokens[2]);
 //                    _out.writeUTF("Invalid Username or Password!!");
                     sendError("Invalid Username or Password!!");
                 }
                 else if (tokens[0].equals("\001NEWUSER")) {
                     assert tokens.length == 4; // login-account-passwd-\004
                     System.out.println("new user");
                     
                     if( _mainServer.getUsers().contains(tokens[1]) ) {
 //                        _out.writeUTF("Name already taken! Please use another name.");
                         sendError("Name already taken! Please use another name.");
                     }
                     else {
                         _mainServer.createUser(tokens[1], tokens[2]);
                         break; // TODO
                     }
                 }
             }
         }
         
         _userName = tokens[1];
         //_passwd = tokens[2];
         System.out.println(_userName);
         send( "\001LOGINACK\000\004" ); // send username ACK
         _mainServer.sendUserlist(this);
         _mainServer.loginInform(this);
   //      send( "\001MSG_GET\000Server\0000\000Welcome to the chatroom\000\004");
         _mainServer.getNameUsers().put(_userName, this);
 
 //        for( Client c: (_mainServer.getClients()) ) { // send the current userlist to the new user
 //            if (c!=this) send("/q+ "+ c._userName+" " + c._userColor);
 //                _mainServer.sendAll( "/q+ " + username+" " + userColor); // send the new user information to all other users
 //                _mainServer.adduser(username, clientID);
 //            break;
 //        }
     }
     
     public String getName() {
         return _userName;
     }
     
     public int getStatus() {
         return _status;
     }
     
     public HashSet<Chatroom> getRooms() {
         return _rooms;
     }
     
     public void send( String s ) {
         try {
             _out.writeUTF(s);
         } 
         catch (IOException ex) {
             System.out.println(ex.toString());
             ex.printStackTrace();
         }
     }
     
     public void sendError(String err) {
         send("\001ERROR\000"+err+"\000\004");
     }
     
     public void parseMsg( String msg ) {
         System.out.println("Parse!!");
         System.out.println(msg);
         
         String[] tokens = msg.split("\000");
 //        System.out.println(tokens.length);
 //        System.out.println(tokens[0]("\001"));
 //        System.out.println(tokens[tokens.length-1].equals("\004"));
         if (tokens.length > 1) {
             if (tokens[tokens.length-1].equals("\004")) {
                 int room, newStat;
                 String sender, receiver, content, account, passwd;
                 switch(tokens[0]) {
                     case "\001LOGIN": // for login again
                          send( "\001LOGINACK\000\004" );
                         _mainServer.sendUserlist(this);
                         break;
                         
                     case "\001NEWUSER": // 001-account-passwd-004
                         assert (tokens.length == 4);
                             sendError("Please logout first!!");
                         break;
                     
                     case "\001NEWROOM": // 001-name-clientRoomNO-004
                         assert (tokens.length == 4);
                         _mainServer.makeRoom(this, tokens[2]);
 //                        _mainServer.roomAdd(roomid, this);
                         System.out.println("NEWROOM");
                         break;
                         
                     case "\001MSG": // 001-sender-roomNO-content-004
                         assert (tokens.length == 5);
                         room = Integer.parseInt(tokens[2]);
 			//String temp = msg.split(" ", 3)[2];
                         if( ! _mainServer.sendRoom(this, room, tokens[3]) ) {
                             sendError("You're not in this room!");
                         }
                         else {
                             System.out.println("MSG");
                             System.out.println(tokens[3]);
                         }
                         break;
                         
                     case "\001MSG_P": // 001-sender-roomNO-receiver-content-004
                         assert (tokens.length == 6);
                         room = Integer.parseInt(tokens[2]);
                         receiver = tokens[3];
                         msg = tokens[4];
 			//String temp = msg.split(" ", 3)[2];
                         if( ! _mainServer.sendPrivate(this, receiver, room, msg) ) {
                             sendError("You're not in this room!");
                         }
                         else {
                             System.out.println("MSG");
                             System.out.println(tokens[3]);
                         }
                         break;
                         
                     case "\001INVITE": // 001-sender-roomNO-receiver-content-004
                         assert (tokens.length == 6);
 //                        sender = tokens[1];
                         room = Integer.parseInt(tokens[2]);
                         receiver = tokens[3];
                         content = tokens[4];
                         if( ! _mainServer.inviteToRoom(this, room, receiver, content) ) {
                             sendError("Cannot invite "+ receiver +" to room!");
                         }
                         else {
                             System.out.println("INVITE");
                             System.out.println(tokens[3]);
                         }
                         break;
                         
                     case "\001IN": // 001-client-roomNO-004
                         assert (tokens.length == 4);
                         room = Integer.parseInt(tokens[2]);
                         if( ! _mainServer.roomAdd(room, this) ) {
                             sendError("Cannot enter room"+tokens[2]+"!");
                         }
                         else {
                             send("\001IN\000"+ tokens[2] +"\000\004");
                             System.out.println("ENTER ROOM"+tokens[2]);
                         }
                         break;
                         
                     case "\001OUT": // 001-client-roomNO-004
                         assert (tokens.length == 4);
                         room = Integer.parseInt(tokens[2]);
                         if( ! _mainServer.roomRm(room, this) ) {
                             sendError("Cannot leave room"+tokens[2]+"!");
                         }
                         else {
                             send("\001OUT\000"+ tokens[2] +"\000\004");
                             System.out.println("LEAVE ROOM"+tokens[2]);
                         }
                         break;
                         
                     case "\001STAT": // 001-client-new_stat-004
                         assert (tokens.length == 4);
                         _status = Integer.parseInt(tokens[2]);
                         _mainServer.changeStat(tokens[1], tokens[2]);
                         
                         System.out.println("CHANGE STAT TO "+tokens[2]);
                         break;
                         
                     case "\001LOGOUT": // 001-004
                         assert (tokens.length == 2);
                        _mainServer.logoutUser(this);
                         break;
                         
                     case "\001FS_REQ":
                         assert(tokens.length == 6);
                         System.out.println("C_FS_REQ");
                         if(!_mainServer.sendReq(this, tokens[2],tokens[3], tokens[4])){
                             // no such client exist 
                             // retrun error message
                         }
                         break;
                         
                     case "\001FS_REP_Y":
                         assert(tokens.length == 5);
                         System.out.println("C_FS_REP_Y");
                         if(!_mainServer.sendReply(this, tokens[2], tokens[3])){
                             
                         }
                         break;
                         
                     case "\001FS_REP_N":
                         assert(tokens.length == 4);
                         System.out.println("C_FS_REP_N");
                         if(!_mainServer.sendReply(this,tokens[2])){
                             
                         }
                         break;
                         
                     default:
                         break;
                 }
             }
         }
     }
     
     public void closeConnection() {
         try {
             _socket.close();
         }
         catch (IOException ex) {
             System.out.println("CAN\'T CLOSE FXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
             System.out.println(ex.toString());
             ex.printStackTrace();
         }
     }
 }
