 /*
  * ClientCommand.java
  *
  * Created on 7 December 2002, 23:40
  */
 
 package server;
 
 import java.io.IOException;
 import java.nio.CharBuffer;
 import java.nio.channels.SocketChannel;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.nio.charset.CharsetEncoder;
 import java.util.regex.Pattern;
 
 /**
  * This object deals with all client commands.
  *
  * @author  Ken Barber
  */
 public class ClientCommand {
     
     /* A pointer to the passed UserData object. */
     private UserData userData;
     
     /* These will be used for network commands. See the constructor. */
     private Charset charset;
     private CharsetDecoder decoder;
     private CharsetEncoder encoder;
     
     /** 
      * Creates a new instance of ClientCommand.
      *
      * @param userdata UserData to use for instantiation.
      */
     public ClientCommand(UserData userdata) {
         /* Keep a local pointer to the user data object */
         userData = userdata;
 
         /* Setup charset,decoder and encoder for use by networking commands*/
         charset = Charset.forName("ISO-8859-1");
         decoder = charset.newDecoder();
         encoder = charset.newEncoder();
     }
 
     /** 
      * Responsible for the proper signup of new clients. 
      *
      * @param username Username of client
      * @param socketchannel SocketChannel of client
      */
     public void clientSignup(String username, SocketChannel socketchannel) {
         /* Check that the username is valid */
         if(Pattern.matches("[a-zA-Z0-9_+-]{3,15}", username) == false) {
             /* Name is invalid, give error */
             returnError("I\'m sorry, usernames must be between 3 and 15 " +
                 "characters and only alphanumeric", socketchannel);
             
             /* Notify on terminal */
             Main.consoleOutput("Attempt to sign-in with invalid username " +
                "(not shown) from: " + socketchannel.socket().getInetAddress().toString());
         } else {
             /* See if the user registration is valid */
             if(userData.insertName(username, socketchannel)) {
                 /* Send a message to all users about the new user */
                 /* messageAllExcept("signup", un, sc); */
 
                 /* List all users currently logged in */
                 clientRoomlist("*", socketchannel);
                 
                 /* Notify on the terminal that new user has signed up */
                 Main.consoleOutput("New user signed in: \"" + username + "\"" + 
                     " from " + userData.getHostIP(socketchannel));
             } else {
                 /* Let the user know that there was an error with signup. */
                 returnError("Already registered, or username taken", 
                     socketchannel);
 
                 /* Notify on the terminal about the new user */
                 Main.consoleOutput("Attempt to sign in with duplicate " +
                     "username: " + username);            
             }
         }
     }
 
     /**
      * This method is responsible for the proper terminate of a user
      * when a quit command is submitted.
      */
     public void clientQuit(String pa, SocketChannel sc) {
         
         /* Make sure the quit reason is ... err ... reasonable */
         if(Pattern.matches("[ \ta-zA-Z0-9!\"#$%&'()*+,-./:;<=>?@\\^_`{|}~]{0,64}", pa) == false) {
             /* Reason doesn't match correct criteria, just clear it */
             pa = "";
         }
         
         if(userData.isSocketRegistered(sc)) {
             /* Get the username using the given SocketChannel */
             String userName = userData.getName(sc);
 
             //Main.consoleOutput("User quit: \"" + userName + "\" because \"" + pa + "\"");
         
             /* Let every room know that the user has quit */
             messageUserRooms("quit", userName, userName + "," + pa);
             
             /* We need to part every room */
             partAllRooms(userName);
             
             /* Remove the users entry from the UserData object */
             userData.deleteName(sc);
             
             /* Notify on the terminal that the user has quit */
             Main.consoleOutput("User quit: \"" + userName + "\" because \"" + pa + "\"");
             
         } else {
             /*The user never logged in, just log to console */
            Main.consoleOutput("An unknown user from: " + sc.socket().getInetAddress().toString() + " quit: " + pa);
         }
 
         try {
             /* Close the channel */
             sc.close();
         } catch(IOException e) {
             Main.programExit("Error closing socket: " + e);
         }
 
     }
     
     /**
      * Deal with a request to see the list of users.
      */
     public void clientUserlist(String se, SocketChannel sc) {
         
         if(Pattern.matches("[ \\a-zA-Z0-9\t\\[\\]!\"#$%&'()*+,-./:;<=>?@\\^_`{|}~]{0,64}", se) == false) {
             returnError("Room name not valid", sc);
         } else {
             /* Obtain an array which contains a list of the current
              * users in the room */
             
             if(userData.isRoomRegistered(se)) {
             } else {
                 returnError("Room name not valid", sc);
                 return;
             }
             
             Object[] users = userData.listNames(se);
 
             /* Parse the array of users, and create a single comma delimited
              * string */
             String list = new String();
             for(int loop = 0; loop <= (users.length -1); loop++) {
                 list = list + users[loop];
                 if(loop < (users.length -1)) {
                     list = list + ",";
                 }
             }           
         
             /* Return the list of users to the user who requested it */
             message("userlist", se + ":" + list, sc);
         }
     }
     
     /**
      * Deal with requests for a user message to be sent.
      */
     public void clientRoomsend(String se, SocketChannel sc) {
         String[] Commands = (Pattern.compile(":")).split(se);
         /* Make sure the message to send is using good characters */
         if(Pattern.matches("[ \\a-zA-Z0-9\t\\[\\]!\"#$%&'()*+,-./:;<=>?@\\^_`{|}~]{0,512}", Commands[1]) == false) {
             /* Reason doesn't match correct criteria, just clear it */
             returnError("The message you have sent has invalid characters or is too long.", sc);
         } else {
             /* Send the message to all users */
             messageRoom("roomsend", Commands[0], userData.getName(sc) + ":" + Commands[1]);
         }
     }
     
     /**
      * Send a user message to another user.
      */
     public void clientUsersend(String se, SocketChannel sc) {
         String[] Commands = (Pattern.compile(":")).split(se);
         /* Check for good characters */
         if(Pattern.matches("[ \\a-zA-Z0-9\t\\[\\]!\"#$%&'()*+,-./:;<=>?@\\^_`{|}~]{0,512}", Commands[1]) == false) {
             /* Reason doesn't match correct criteria, just clear it */
             returnError("The message you have sent has invalid characters or is too long.", sc);
         } else {
             /* Send the message to both the sender and originator */
             message("usersend", Commands[1], userData.getSocket(Commands[0]));
             message("usersend", Commands[1], sc);            
         }
     }
     
     /**
      * A user has joined a room
      */
     public void clientJoin(String se, SocketChannel sc) {
         /* Make sure the message to send is using good characters */
         if(Pattern.matches("[a-zA-Z0-9]{0,32}", se) == false) {
             /* Reason doesn't match correct criteria, just clear it */
             returnError("The room you are requesting has invalid characters or is too long.", sc);
         } else {
             /* Join a room */
             
             /* If room doesn't exist, create it */
             if(userData.isRoomRegistered(se) == false) {
                 userData.insertRoom(se);
                 
                 Main.consoleOutput("New room created: " + se);
             };
             
             /* If user isn't already a member, join it */
             Main.consoleOutput("The user: " + userData.getName(sc) + " is trying to join the room: " + se);
             userData.joinRoom(userData.getName(sc), se);
             
             /* Send a message to everyone in the room */
             messageRoom("join", se, userData.getName(sc));
             
         }
     }
     
     /**
      * A user has left a room
      */
     public void clientPart(String se, SocketChannel sc) {
         /* Make sure the message to send is using good characters */
         if(Pattern.matches("[a-zA-Z0-9]{0,32}", se) == false) {
             /* Reason doesn't match correct criteria, just clear it */
             returnError("The room you are attempting to part has invalid characters or is too long.", sc);
         } else {
             /* Part a room */
             
             /* Send a message to everyone in the room */
             messageRoom("part", se, userData.getName(sc));
             
             /* Update the userdata bit */
             userData.partRoom(userData.getName(sc), se);
             
             /* If last in room, remove room */
             if(userData.listNames(se).length == 0) {
                 userData.deleteRoom(se);
             }
         }
     }
     
     /**
      * Return a list of rooms
      */
     public void clientRoomlist(String se, SocketChannel sc) {
         /* Make sure the criteria is correct */
         if(se.equals("*")) {
             /* Obtain an array which contains a list of the currently
              * registered rooms */
             Object[] rooms = userData.listRooms();
 
             /* Parse the array of rooms, and create a single comma delimited
              * string */
             String list = new String();
             for(int loop = 0; loop <= (rooms.length -1); loop++) {
                 list = list + rooms[loop];
                 if(loop < (rooms.length -1)) {
                     list = list + ",";
                 }
             }           
         
             /* Return the list of users to the user who requested it */
             message("roomlist", list, sc);
 
         } else {
             returnError("Invalid search criteria.",  sc);
         }
         
     }
     
     /**
      * This method is generic and will return an error to a user.
      */
     public void returnError(String err, SocketChannel sc) {
         /* Send the error message to the requested SocketChannel */
         message("fail", err, sc);
     }
     
     /** 
      * This method will send a message to a SocketChannel in the GOB protocol 
      * format.
      */
     private void message(String type, String msg, SocketChannel sc) {
         try {
             /* Write the message to the SocketChannel */
             sc.write(encoder.encode(CharBuffer.wrap("GOB:" + type + ":" + msg + "\n")));
         } catch (IOException e) {    /* Is the SocketChannel closed? */
             /* Force a client quit */
             clientQuit("Error messaging users socket.", sc);
         } 
     }
 
     /**
      * This method will message everyone in a room.
      */
     private void messageRoom(String type, String room, String msg) {
         /* Obtain a list of all socketChannels in specified room */
         Object[] socketchannels = userData.listSockets(room);
         
         /* Cycle through each SocketChannel, and message each on in turn */
         for(int loop = 0; loop <= (socketchannels.length -1); loop++) {
             message(type + ":" + room, msg, (SocketChannel)socketchannels[loop]);
             Main.consoleOutput("Type: " + type + " Room: " + room + " Msg: " + msg);
         }
     }
     
     /**
      * This method will message each room a user belongs to.
      */
     private void messageUserRooms(String type, String username, String msg) {
         /* Obtain a list of all rooms for username */
         Object[] rooms = userData.listRooms(username);
         
         /* Cycle through each room, and message each on in turn */
         for(int loop = 0; loop <= (rooms.length -1); loop++) {
             messageRoom(type, (String)rooms[loop], msg);
             //Main.consoleOutput("MessageUserRoom - Type: " + type + " Room: " + room + " Msg: " + msg);
         }
     }
 
     
     /**
      * This method is an extended form of message. It will notify _all_ signed in
      * users.
      */
     private void messageAll(String type, String msg) {
         /* Obtain a list of all SocketChannels */
         Object[] socketchannels = userData.listSockets();
             
         /* Cycle through each SocketChannel, and message each one in turn */
         for(int loop = 0; loop <= (socketchannels.length -1); loop++) {
             message(type, msg, (SocketChannel)socketchannels[loop]);
         }
     }
     
     /**
      * This methods is the same as a messageAll, yet it skips the specified
      * exception.
      */
     private void messageAllExcept(String type, String msg, SocketChannel exception) {
         /* Obtain a list of all SocketChannels */
         Object[] socketchannels = userData.listSockets();
 
         /* Cycle through each SocketChannel, and message each one in turn */
         for(int loop = 0; loop <= (socketchannels.length -1); loop++) {
             /* Make sure we don't message the exception. Simple */
             if(socketchannels[loop].equals(exception)) {
                 continue;
             } else {
                 message(type, msg, (SocketChannel)socketchannels[loop]);
             }
         }
     }
     
     /**
      * This method will part each room a user belongs to.
      */
     private void partAllRooms(String username) {
         /* Obtain a list of all rooms for username */
         Object[] rooms = userData.listRooms(username);
         
         Main.consoleOutput("The user: " + username + " is parting all rooms");
         
         /* Cycle through each room, and part each in turn */
         for(int loop = 0; loop <= (rooms.length -1); loop++) {
             
             /* Update the userdata bit */
             userData.partRoom(username, (String)rooms[loop]);            
             
             Main.consoleOutput("Check if the room: " + rooms[loop] + " needs to be closed, with users: " + userData.listNames((String)rooms[loop]).length);
             
             /* If last in room, remove room */
             if((userData.listNames((String)rooms[loop])).length == 0) {
                 Main.consoleOutput("Now parting: " + rooms[loop]);
                 userData.deleteRoom((String)rooms[loop]);
             }            
         }
     }
 }
