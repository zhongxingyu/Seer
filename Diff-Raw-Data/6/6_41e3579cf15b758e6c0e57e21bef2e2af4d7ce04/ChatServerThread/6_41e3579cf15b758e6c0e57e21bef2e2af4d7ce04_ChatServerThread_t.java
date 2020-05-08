 package Server;
 import java.net.*;
 import java.util.Scanner;
 import java.io.*;
 import Common.Encryptor;
 
 public class ChatServerThread extends Thread {
     /* The client socket and IO we are going to handle in this thread */
     protected Socket         socket;
     protected PrintWriter    out;
     protected BufferedReader in;
 
     //the user this thread corresponds to
     private UserAccount user;
 
     //chat room this belongs to
     private ChatServerChatRoom chatRoom;
     //server this belongs to
     private ChatServerMain chatServer;
     
     private String pendingAudioChat; //if there is a pending request
 
     //id specific to room
     private int id;
 
     public static final boolean ENCRYPTED = true; //encryption enabled? false for server debug
 
     public boolean isLoggedIn(){ return (user!=null); }
 
     public int getID(){ return id; }
 
     public void setID(int a){ id = a; }
     
     public boolean getIsAdmin(){ return user.getIsAdmin(); }
 
     public String getUserName(){ if (user!=null) return user.getName(); return null; }
 
     public void setUserName(String n){ user.setName(n); }
 
     public ChatServerChatRoom getRoom(){ return chatRoom; }
     
     public void setRoom(ChatServerChatRoom room){ chatRoom = room; }
 
     public ChatServerThread(Socket socket, UserAccount account, ChatServerChatRoom  chatRoom, ChatServerMain chatServer){
         user = account;
         this.chatRoom = chatRoom;
         this.chatServer = chatServer;
 
         /* Create the I/O variables */
         try {
             this.out = new PrintWriter(socket.getOutputStream(), true);
             this.in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 
             /* Debug */
             System.out.println("Client handler thread created.");
         } catch (IOException e) {
             System.out.println("IOException: " + e);
         }
     }
 
     public void send(String a){
         if (a==null || a.length()==0) return;
         if (ENCRYPTED) a = Encryptor.encrypt(a, 5); //encrypt it 5 times
         this.out.println(a);
     }
 
     public String receive(){
         try{
             String input = this.in.readLine(); if (input==null){ System.out.println("Server thread received null string."); return null; }
             if (ENCRYPTED) return Encryptor.decrypt(input, 5); //decrypt it 5 times
             else return input;
         } catch (Exception e) {}
         return null;
     }
 
     @Override
     public void run() {
         //ask for login credentials
         send("Enter username (will be created if doesn't exist).");
         String name = receive();
         send("Enter your password (or desired password for new account).");
         String password = receive();
         System.out.println("Handling login for "+name+", password is "+password+".");
         user = chatServer.handleLogin(name, password);
         if (user==null){ send("Invalid password or username, disconnecting!"); System.out.println("Disconnecting user because of bad login."); forceDisconnect(); return; }
         user.setThread(this);
 
         this.tell("Server Message", "You've joined the chat room "+chatRoom.getName()+".");
         chatRoom.tellEveryone("Server Message", ""+user.getName()+" joined the room.");
         //else{ System.out.println("Unknown error run ChatServerThread run from handling login!!!"); this.out.println("Technical difficulties, disconnecting."); forceDisconnect(); }
 
         Scanner scanner; //for analyzing text
         while (true) {
             try {
                 if (user==null) return; //check if thread should be killed off
 
                 /* Get string from client */
                 String fromClient = receive();
                 if (user==null) return; //check if thread should be killed off
 
                 /* If null, connection is closed, so just finish */
                 if (fromClient == null) {
                     disconnect(null);
                 }
 
                 /* Handle the text. */
                 if (fromClient.length()>0){ 
                     if (fromClient.charAt(0)=='/'){ //if it's a command
                         scanner = new Scanner(fromClient);
                         String firstWord = scanner.next();
                         if (firstWord.equalsIgnoreCase("/whisper")){ //to tell a user a private message
                             if (!scanner.hasNext()) send("You must specify the target's name.");
                             String target = scanner.next();
                             if (!scanner.hasNext()) send("You must specify a message.");
                             String message = scanner.next();
                             System.out.println("User "+user.getName()+" saying (privately) "+message+" to target "+target+".");
                             if (!chatServer.tellUser(user.getName()+" (privately)", target, message)) send("User not found online.");
                         }
                         else if (firstWord.equalsIgnoreCase("/anonymous")){
                             String message = "";
                             message = fromClient.substring(10, fromClient.length()); //10 is the length of /anonymous
                             if (!user.getIsAdmin()){
                             chatRoom.tellEveryoneNotAdmins("Anonymous User", message);
                             chatRoom.tellAdmins(""+user.getName()+" (anonymously)", message); //users not anonymous to admins
                         }
                         else chatRoom.tellEveryone("Anonymous User", message); //admins are anonymous to admins
                         }
                         else if (firstWord.equalsIgnoreCase("/nick")){ //to change a user's name
                             if (!scanner.hasNext()) send("You must specify a name.");
                             String oldName = user.getName();
                             if (!chatServer.changeUserName(user.getName(), scanner.next())){ send("Name already taken or invalid."); return; }
                             if (chatRoom!=null) chatRoom.tellEveryone("Server Message", "User "+oldName+" is now known as "+user.getName()+"."); //server message  
                         }
                         else if (firstWord.equalsIgnoreCase("/disconnect")){ //to disconnect gracefully
                             String message = null;
                             if (scanner.hasNext()) message = scanner.next();
                             disconnect(message);
                         }
                         else if (firstWord.equalsIgnoreCase("/stop")){ //to close the server, ADMIN ONLY
                             if (user.getIsAdmin()) chatServer.quit();
                             else send("You do not have permission to use this command.");
                         }
                         else if (firstWord.equalsIgnoreCase("/audio")){ //to start an audio chat with someone
                             if (!scanner.hasNext()) send("You must specify the target's name.");
                             String target = scanner.next();
                             System.out.println("User "+user.getName()+" starting audio chat with "+target+".");
                             if (!chatServer.audioChat(user.getName(), target)) send("Unable to start audio chat with user.");
                             else{
                                 send("Sent audio chat request. You can end it at any time with /decline.");
                                 send("/accept"); //client interprets this and makes a chat thread in this case
                             }
                         }
                         else if (firstWord.equalsIgnoreCase("/accept")){
                             if (pendingAudioChat==null || pendingAudioChat.length()==0){ send("No pending audio chat request to accept."); }
                             else{
                             Scanner tempScanner = new Scanner(pendingAudioChat); String sender = tempScanner.next(); String target = tempScanner.next();
                             System.out.println("User "+target+" accepted audio chat with "+sender+".");
                             if (chatServer.audioChat(sender, target)){ send("/accept"); System.out.println("User "+user.getName()+" accepted audio chat."); }
                             else send("No pending audio chat request to accept.");
                             }
                         }
                         else if (firstWord.equalsIgnoreCase("/decline")){
                             chatServer.endAudioChat(pendingAudioChat); pendingAudioChat = null;
                             send("Audio chat cancelled.");
                         }
                         
                         else if (firstWord.equalsIgnoreCase("/changeroom")){
                         if (!scanner.hasNext()) send("You must specify a room name.");
                         else if (!chatServer.changeRoom(user.getName(), scanner.next())) send("Invalid room name specified. "); 
                         else{
                             this.tell("Server Message", "You've joined the chat room "+chatRoom.getName()+".");
         chatRoom.tellEveryone("Server Message", ""+user.getName()+" joined the room.");
                         }
                         }
                         
                         else if (firstWord.equalsIgnoreCase("/rooms")){
                             send(chatServer.getRoomNames());
                         }
                         
                         else if (firstWord.equalsIgnoreCase("/users")){
                             send(chatRoom.getUsers());
                         }
                         
                         else if (firstWord.equalsIgnoreCase("/addroom")){
                         if (!user.getIsAdmin()) send("You do not have permission to use this command.");
                         else{
                         if (!scanner.hasNext()) send("You must specify a room name.");
                         else if (!chatServer.addRoom(scanner.next())) send("Invalid room name specified. "); 
                         else send("New room created.");
                         }
                         }
                         
                         else if (firstWord.equalsIgnoreCase("/removeroom")){
                         if (!user.getIsAdmin()) send("You do not have permission to use this command.");
                         else{
                         if (!scanner.hasNext()) send("You must specify a room name.");
                         else if (!chatServer.removeRoom(scanner.next())) send("Invalid room name specified. "); 
                         else send("Room removed.");
                         }
                         }
                         
                         else if (firstWord.equalsIgnoreCase("/ban")){
                             if (!user.getIsAdmin()) send("You do not have permission to use this command.");
                             else{
                             if (!scanner.hasNext()) send("You must specify a user.");
                  
                             else{
                                 String target = scanner.next();
                                 if (chatServer.banUser(target)) send("Banned user "+target);
                                 else send("Could not ban user "+target);
                             }
                         }
                         }
                         
                         else if (firstWord.equalsIgnoreCase("/unban")){
                             if (!user.getIsAdmin()) send("You do not have permission to use this command.");
                             else{
                             if (!scanner.hasNext()) send("You must specify a user.");
                             
                             else{
                                 String target = scanner.next();
                                 if (chatServer.unbanUser(target)) send("Unbanned user "+target);
                                 else send("Could not unban user "+target);
                             }
                         }
                         }
                         
                         else if (firstWord.equalsIgnoreCase("/op")){
                             if (!user.getIsAdmin()) send("You do not have permission to use this command.");
                             else{
                             if (!scanner.hasNext()) send("You must specify a user.");
                    
                             else{
                                 String target = scanner.next();
                                 if (chatServer.promoteUser(target)) send("Opped user "+target);
                                 else send("Could not op user "+target);
                             }
                         }
                         }
                         
                         else if (firstWord.equalsIgnoreCase("/deop")){
                             if (!user.getIsAdmin()) send("You do not have permission to use this command.");
                             else{
                             if (!scanner.hasNext()) send("You must specify a user.");
                     
                             else{
                                 String target = scanner.next();
                                 if (chatServer.demoteUser(target)) send("Deopped user "+target);
                                 else send("Could not deop user "+target);
                             }
                         }
                         }
                         
                         else send("Invalid command.");
                         scanner.close();
                     }
                     
                 
 
                     else chatRoom.tellEveryone(user.getName(), fromClient);
                 }
 
             } catch (Exception e) {
                 /* On exception, stop the thread */
                 return;
             }
         }
     }
 
     public void disconnect(String message){ //called when disconnecting from server
         if (message == null) message = "No message given.";
         System.out.println("Client "+user.getName()+ " disconnected");
         if (message!=null && chatRoom!=null) chatRoom.tellEveryone(user.getName()+" (disconnecting)", message);
         if (chatRoom!=null) chatRoom.tellEveryone("Server Message", ""+user.getName()+" disconnected"); //server message
         tell("Server Message", "You have been disconnected: "+message);
         this.user = null; //causes the thread to stop
         try{
             this.socket.close();
             this.in.close();
             this.out.close();
         } catch (Exception e) { System.out.println("Caught non-problematic exception: "+e); }
     }
 
     public void forceDisconnect(){ //just disconnect, sending no messages
         System.out.println("Client disconnected forcibly");
         tell("Server Message", "You have been disconnected forcibly.");
         this.user = null; //causes the thread to stop
        try{
            this.socket.close();
            this.in.close();
            this.out.close();
        } catch (Exception e) { System.out.println("Caught non-problematic exception: "+e); }
     }
 
     public void tell(String user, String message){
         if (message==null || message.length()<=0) return;
         send(user+": "+message);
     }
     
     public boolean audioChat(String user){
         if (pendingAudioChat!=null) return false;
         tell(user, "I've invited you to an audio chat. Type /accept to accept or /decline to decline.");
         pendingAudioChat = (""+user+" "+this.user.getName());
         return true;
     }
 }
