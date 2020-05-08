 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lighting;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  *
  * @author StompingBrokenGlass <stompingbrokenglass@gmail.com>
  * @since 2013-06-05
  */
 public class Server extends Thread {
     /* Connection configration */
     private String hostname ;
     private int port ;
     private String nickname ;
     private String password ;
     private String commandChar ;
     
     /* Connection stream */
     private Socket socket;
     private BufferedWriter outbound;
     private BufferedReader inbound;
     
     /* This Process status */
     private boolean running = false;
     
     /* Channels */
     private HashMap channels = new HashMap();
     
     Server () {
         // uses the local server
         this.hostname = "localhost";
         this.port = 6667;
         this.nickname = "Lighting";
         this.password = "";
         this.commandChar = "~";
         
     }
     
     Server (String hostname, int port, String nickname, String password,
             String commandChar){
         this.hostname = hostname;
         this.port = port;
         this.nickname = nickname;
         this.password = password;
         this.commandChar = commandChar;
     }
     
     /**
      * 
      * @param hostname 
      */
     public void setHostname (String hostname){
         this.hostname = hostname;
     }
     
     /**
      * 
      * @return Returns the server's hostname
      */
     public String getHostname () {
         return this.hostname;
     }
     
     /**
      * 
      * @param port 
      */
     public void setPort (int port){
         this.port = port;
     }
     
     /**
      * 
      * @return the server's port number
      */
     public int getPort (){
         return this.port;
     }
     
     /**
      * Set the nickname of the bot for DataOutputStreamthis server
      * @param nickname 
      */
     public void setNickname (String nickname){
         this.nickname = nickname;
     }
     
     /**
      * 
      * @return Nickname
      */
     public String getNickname (){
         return this.nickname;
     }
     
     /**
      * Sets the password of the bot for this server
      * @param password 
      */
     public void setPassword (String password){
         this.password = password;
     }
     
     public void initConnection (){
         
         System.out.println("Connecting to " + this.hostname + " on port " +
                 this.port + " ...");
         try {
             
             this.socket = new Socket(this.hostname,this.port);
             System.out.println("Connected.");
             
             // Getting streams
             
             this.outbound = new BufferedWriter( new OutputStreamWriter(
                     this.socket.getOutputStream()));
             this.inbound = new BufferedReader( new InputStreamReader(
                     this.socket.getInputStream()));
             
             // Identifying our self to the server
             this.outbound.write("NICK " + this.nickname);
             this.outbound.newLine();
             this.outbound.write("USER " + this.nickname +
                     " 0 * :Lighting Cat-bot");
             this.outbound.newLine();
             this.outbound.flush();
             
         } catch (Exception e){
             e.printStackTrace();
         }
         
     }
     
     /**
      * Implementing run from Thread Class
      */
     @Override
     public void run () {
         
         this.running = true;
         
         this.initConnection();
         try {
             
             String message;
             
             while(this.running){
 
                 message = this.inbound.readLine();
                 // checking if the buffer is not null
                 if (message != null){
                     // Printing inboud messages
                     System.out.println("<-- " + message);
                     
                     // Sending the message to the processor
                     this.ircProcessor(message);
                     
                 }else{
                     System.out.println("Disconnected from " + this.hostname + 
                             ":" + port);
                     this.running = false;
                 }
 
             }
             
         } catch (IOException e) {
             e.printStackTrace();
         }
         
     }
     
     /**
      * Processor for inbound messages
      * @param message Received message to be processed
      */
     private void ircProcessor (String message){
         
         String prefix = " ";
         String command ;
         String parameters ;
         
         // Check if there is a prefex and sets it
         if (message.indexOf(':') == 0){
             // Finding Spaces
             int firstSpace = message.indexOf(" ");
             int secondSpace = message.indexOf(" ", firstSpace+1);
             //
             prefix = message.substring(1, firstSpace);
             command = message.substring(firstSpace +1, secondSpace);
             parameters = message.substring(secondSpace+1);
         }else{
             // no pefix
             int firstSpace = message.indexOf(" ");
             
             command = message.substring(0, firstSpace);
             parameters = message.substring(firstSpace+1);
         }
         
         // Check up the progress
         
         /*System.out.println("Prefix: " + prefix +" Command: " + command + 
                 " Para:"+ parameters);*/
         
         // switch board of commands & replies according to IRC rfc2812
         switch(command){
             
             /* Replies */
             
             case "001" :    // RPL_WELCOME
             case "002" :    // RPL_YOURHOST
             case "003" :    // RPL_CREATED
             case "004" :    // RPL_MYINFO
                 
                             // Welcome messages, no action needed
                             break;
                 
             case "005" :    // RPL_BOUNCE /  RPL_ISUPPORT
                             /* from rfc2812 it's sent to indicate that
                             * the server is full and gives aa new address to
                             * try. 
                             * But IRCD2 uses RPL_ISUPPORT according to 
                             * IRC RPL_ISUPPORT Numeric Definition draft
                             */
                 
                             // TODO: Handle bouncing, and ISUPPORT
                             break;
                 
             case "020" :    // Not in rfc2812 or rfc1459
                             /* IRCD2 is informing us to wait for connection
                              * processing.
                              */
                              
                              // TODO: investgate reply 020 more
                              break;
                 
             case "042" :    // Not in rfc2812 or rfc1459
                             /* IRCD2 is sending an unique ID*/
                 
                             // TODO: invesgate reply 042 more
                             break;
                 
             case "251" :    // RPL_LUSERCLIENT
             case "252" :    // RPL_LUSEROP
             case "253" :    // RPL_LUSERUNKNOWN
             case "254" :    // RPL_LUSERCHANNELS
             case "255" :    // RPL_LUSERME
                 
                             // Network statictics 
                             break;
             case "265" :
             case "266" :
                             // Not in rfc2812 or rfc1459
                             /* IRCD2 sends locally connected stats for 265,
                              * and network stats for 266
                              */
                 
                             // TODO: invesgate replies 265, and 266 more
                             break;
                 
             case "353" :    // RPL_NAMREPLY
                 
                             // TODO: Add users to the channel's userlist
                             break;
                 
             case "366" :    // RPL_ENDOFNAMES
                             // Greet after Name List
                             int space = parameters.indexOf(" ");
                             String chan = parameters.substring(space+1,
                                     parameters.indexOf(" ", space +1));
                             this.sendMessage(chan, "Meow!");
                             break;
                 
             case "372" :    // RPL_MOTD
                             // Message of the day context
                             break;
                 
             case "375" :    // RPL_MOTDSTART
                             // indcate starting of message of the day
                             break;
                 
             case "376" :    // RPL_ENDOFMOTD
                             // End of message of the day
                             this.joinChannels();
                             break;
                 
             case "433" :    // ERR_NICKNAMEINUSE
                             // Nickname is in use
                 
                             // TODO: handle changing to another nickname
                             break;
                 
             case "474" :    // ERR_BANNEDFROMCHAN
                             // Banned from channel
                 
                             // TODO: handle bannig from channel
                 
                             break;
                 
             /* Comands */
             
             case "ERROR" :  // Error message from the server
                             System.out.println("Error from the server("+ 
                                     parameters +")");
                             break;
                 
             case "JOIN" :   // User have joinned the channel
                             // TODO: add user to list, and maybe greet
                             break;
             
             case "KICK" :   // User have been kicked
                             // TODO: remove user from channel's user list
                             break;
                 
             case "PART" :   // User have left the channel
                             // TODO: remove user from channel's user list
                             break;
                             
             case "PING" :   
                             this.sendPong(parameters);
                             break;
                 
             case "PRIVMSG" :    // Receiving a message
                             this.priMsgProcessor(message);
                             break;
                 
             case "MODE" :   // Mode changes
                             
                             break;
                 
             case "NOTICE" : // Receiving a notice
                 
                             // TODO: handle notice
                             break;
                 
             case "QUIT" :   // User have left the network
                             // TODO: remove user from channel's user list
                 
                             break;
                 
             default:        // unhandled replies and command
                             System.out.println ("Unhandled command or reply");
         }
         
     }
     
     
     /**
      * Sends a message through the stream
      * @param message The message to be sent to the server
      */
     private void sendRaw (String message) {
         
         try {
             System.out.println("--> " + message);
             this.outbound.write(message);
             this.outbound.newLine();
             this.outbound.flush();
         } catch (IOException e){
             e.printStackTrace();
         }
     }
     
     /**
      * Sends a PONG Message for keeping the connection alive
      * @param requester Server name from the ping
      */
     private void sendPong (String requester){
         String message = "PONG " + requester;
         sendRaw(message);
     }
     
     public void addChannel (String channel){
         Channel chan = new Channel();
         chan.setName(channel);
         this.channels.put(channel, chan);
     }
     
     public void removeChannel (String channel){
         this.channels.remove(channel);
     }
     
     private void joinChannels(){
       Iterator it  = this.channels.entrySet().iterator();
       
       while(it.hasNext()){
           Map.Entry pairs = (Map.Entry) it.next();
           String chanName = (String) pairs.getKey();
           this.joinChannel(chanName);
           it.remove();
       }
     }
     
     private void joinChannel(String channel){
         this.sendRaw("JOIN " + channel);
     }
     
     public void sendMessage (String traget, String message){
         this.sendRaw("PRIVMSG "+ traget + " :" + message);
     }
     
     public void sendNotice (String traget, String message){
         this.sendRaw("NOTICE " + traget + " :" + message);
     }
     
     private void priMsgProcessor (String rawMessage){
         
         Message message = new Message(rawMessage, this.nickname);
         
         String context = message.getContext();
         
         String response ="";
         String traget = "";
         
         if (message.isPM()){
             traget = message.getSender();
         } else {
             traget = message.getChannel();
             response = message.getSender() + ": ";
         }
         
         if (context.startsWith(this.commandChar)) {
             // handing commands
             int space = context.indexOf(" ");
             
             String botCommand;
             String botParameters = " ";
             
             if (space > 2){
                 botCommand = context.substring(1,space);
                 botParameters = context.substring(space+1);
             } else {
                 botCommand = context.substring(1);
             }
             
             
             switch(botCommand){
                 case "echo" : // echo
                                 response = response.concat(botParameters);
                                 break;
                 default:
                                 response = response.concat("Meow ?");
                                 
             }
             
             this.sendMessage(traget, response);
             
        } else if (context.startsWith("CTCP")){
             // TODO: CTCP support
         } else {
             /* do nothing at the moment, but can impletemt some sort of
              * a listener 
              * 
              */
         }
     }
 }
