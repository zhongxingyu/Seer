 /*
  * ConnectionControl.java
  *
  * Created on 29 December 2002, 18:12
  */
 
 package server;
 
 import java.io.*;
 import java.net.*;
 import java.nio.*;
 import java.nio.channels.*;
 import java.nio.charset.*;
 import java.util.*;
 import java.util.regex.*;
 
 /**
  * This is the object responsible for accepting user connections and accepting all user commands.
  *
  * @author  Ken Barber
  */
 public class ConnectionControl {
     
     /**
      * The port for the server to listen on - this will eventually be dynamic. 
      */
     private static final int serverPort = 6666;
     
     /** 
      * These are the pointers used for the server networking 
      */
     private ServerSocketChannel listenServerSC;
     private Selector listenSelector;
     
     /** 
      * References for the UserData and ClientCommand objects passed to us 
      */
     private UserData userData;
     private ClientCommand clientCommand;
     
     /** 
      * Creates a new instance of ConnectionControl.
      */
     public ConnectionControl() {
         
         /* 
          * The following code prepares all the necessary objects for 
          * network programming
          */
         
         /* Prepare a ServerSocketChannel, and register it with a selector */
         listenServerSC = readyServerSocketChannel();
         listenSelector = readySelector(listenServerSC);
         
         /* This is the character encoding parts required by the networking commands */
         Charset charset = Charset.forName("ISO-8859-1");
         CharsetDecoder decoder = charset.newDecoder();
         CharsetEncoder encoder = charset.newEncoder();
         
         /* Allocate buffers for receiving */
         ByteBuffer inputByteBuffer = ByteBuffer.allocateDirect(1024);
         CharBuffer inputCharBuffer = CharBuffer.allocate(1024);
         
         /* Create new instances of the UserData and ClientCommand objects */
         userData = new UserData();
         clientCommand = new ClientCommand(userData);
         
         /* 
          * This is the main networking loop, it will continue looping forever
          * continually accepting connections etc. 
          */
         for(;;) {            
             
             /* Used for testing the select response */
             int selectResponse = 0;
             
             /* Attempt a select */
             try {
                 /* Wait until network activity is detected on any attached sockets */
                 selectResponse = listenSelector.select(500);
             } catch(IOException e) {
                /* Log problems to the terminal */
                Main.programExit("Problem with select: " + e);
             }
             
             /* If the selectResponse is greater than 0, than the selector has detected
              * activity on one of its registered SocketChannel's */
             if(selectResponse > 0) {
                 /* Obtain Set of detected activities or Keys */
                 Set readyKeys = listenSelector.selectedKeys();
                 Iterator readyItor = readyKeys.iterator();
 
                 /* Cycle through each Key until the queue is empty */
                 while(readyItor.hasNext()) {
                     /* Get this SelectionKey */
                     SelectionKey key = (SelectionKey)readyItor.next();
 
                     /* Remove this key from the Iterator, so we don't repeat 
                      * ourselves */
                     readyItor.remove();
 
                     /* Deal with the activity for the SelectionKey */
                     if(!key.isValid()) { /* The SelectionKey/SocketChannel is not valid */
                         /* Altert the problem on the terminal
                          *
                          * I don't actually know what would generate this condition */
                         Main.consoleOutput("This SelectionKey isn\'t valid. This is unexpected behaviour.");
                     } else if(key.isAcceptable()) { /* We have received a new connection */
                         /* Prepare and register new connection with the Selector */
                         SocketChannel sc = readySocketChannel((ServerSocketChannel)key.channel());
 
                         /* Give GOB greeting */
                         try {
                             /* Send the message */
                             sc.write(encoder.encode(CharBuffer.wrap("GOB:greeting:Welcome to the server!\n")));
                             
                             /* Log new connection */
                             Main.consoleOutput("New connection from: " + sc.socket().getInetAddress().toString());
                         } catch(IOException e) {
                             /* Close the SocketChannel, if I can't write to it - then I don't want
                              * to continue */
                             try {
                                 sc.close();
                                 
                                 /* Send errors to the terminal */
                                 Main.consoleOutput("Error giving welcoming greeting to a socket, it has been closed: " + e);
                             } catch(IOException ne) {
                                 /* Send error to console and exit, I should be able to close an invalid socket */
                                 Main.programExit("I am unable to close a socket that couldn\'t be written to in the first place: " + ne);
                             }
                         }
 
                     } else if (key.isReadable()) {  /* Someone has sent a command */
                             
                         /* Reset all buffers */
                         inputByteBuffer.clear();                        
                         inputCharBuffer.clear();
                             
                         /* Get the SocketChannel assocaited with this Key */
                         SocketChannel sc = (SocketChannel)key.channel();
                                         
                         /* Check to make sure this SocketChannel is actually connected */
                         if(!sc.isConnected()) {
                             /* Output the problem on the terminal */
                             Main.consoleOutput("Received activity on a connected socket. However this socket isn\'t connected.");
                             
                             /* Close the socket, just in case */
                             try {
                                 sc.close();
                             } catch(IOException e) {
                                 Main.programExit("I\'ve attempted to close a socket, that was marked as not connected and it failed: " + e);
                             }
                             
                             /* Next selected key (if any) */
                             continue;
                         }
                             
                         // readResult used for socketchannel read.
                         int readResult = 0;
                         
                         /* Read any pending data into a buffer */
                         try {
                             readResult = sc.read(inputByteBuffer);
                         } catch(IOException e) {
                             /* Output any problems to the terminal */
                             Main.consoleOutput("Error receiving data on network: " + e);
                             
                             /* Close the channel */
                             try {
                                 sc.close();
                             } catch(IOException ne) {
                                 Main.programExit("I\'ve attempted to close a socket that I was having problems reading on: " + e);
                             }
                             
                             /* Next SelectedKey */
                             continue;
                         }
                             
                         /* See if we have received an end-of-stream */
                         if(readResult == -1) {
                             clientCommand.clientQuit("Client forcefully disconnected", sc);
                                 
                             /* Go the next next pending SelectionKey */
                             continue;
                         }
 
                         /* Flip the buffer */
                         inputByteBuffer.flip();
                         
                         /* Decode the buffer using the character set specified above */
                         decoder.decode(inputByteBuffer, inputCharBuffer, false);
                         
                         /* Flip the buffer we have justed decoded */
                         inputCharBuffer.flip();
                                                
                         /* Now convert this buffer to a string */
                         String inputString = inputCharBuffer.toString();
                             
                         /* Ensure strings are of reasonable length */
                         if(inputString.length() < 6) {
                             /* Return an error to the user */
                             clientCommand.returnError("Invalid Command", sc);
                             
                             /* Go to the next pending SelectionKey */
                             continue;
                         }
 
                         /* Remove line-break at the end of the String */
                         inputString = inputString.substring(0, inputString.length() -1);
                         
                         /* Remove carriage-return if there is one from the String */
                         if(inputString.substring(inputString.length() -1).equals("\r")) {
                             inputString = inputString.substring(0, inputString.length() -1);
                         }
                         
                         /* Break up string into command and value */
                         String command[] = inputString.split(":", 2);
 
                         /* All client commands should be have two parts */
                         if(command.length == 2) {
 
                             /* Ensure the command is reasonable, make sure it is
                              * alphabetical and between 3 and 15 characters 
                              */
                             if(Pattern.matches("[a-z]{3,15}", command[0]) == false) {
                                 clientCommand.returnError("Command format incorrect", sc);
                                 continue;
                             }
                             
                             /* Deal with any command */
                             if(command[0].equals("signup")) { /* The command is a signup */
                                 /* Signup the user */
                                 clientCommand.clientSignup(command[1], sc);
                             } else if(command[0].equals("quit")) { /* The command is quit */
                                 /* Quit the user */
                                 clientCommand.clientQuit(command[1], sc);
                             } else if(command[0].equals("list")) { /* The command is list */
                                 /* If the user is signed in, return a list of users */
                                 if(userData.isRegistered(sc)) {
                                     clientCommand.clientList(command[1], sc);
                                 } else { /* User isn't logged in */
                                     /* Return an error, informing the user he is not logged in */
                                     clientCommand.returnError("Not logged in", sc);
                                 }
                                 
                             } else if(command[0].equals("send")) { /* The command is send */
                                 /* If the user is signed in, send the user message */
                                 if(userData.isRegistered(sc)) {
                                     clientCommand.clientSend(command[1], sc);
                                 } else { /* User isn't logged in */
                                     /* Return an error, informing the user he is not logged in */
                                     clientCommand.returnError("Not logged in", sc);
                                 }
                                 
                             } else {
                                 /* Other commands are not recognised, so return an error */
                                 clientCommand.returnError("Unknown command", sc);
                                 
                             }
                                 
                         } else { /* The command did not have 2 parts */
                             /* Return an error */
                             clientCommand.returnError("Command format incorrect", sc);
                                                                 
                         }
                     }                        
                 } /* While loop */
             } /* If test */
         } /* Networking infinite loop */
     }
     
     /** 
      * Prepares and returns a server socket channel. We register this with a selector. 
      */
     private ServerSocketChannel readyServerSocketChannel() {
         /* Declare a new ServerSocketChannel pointer, currently null */
         ServerSocketChannel ssc = null;
         
         try {
             /* Open a new ServerSocketChannel */
             ssc = ServerSocketChannel.open();
             
             /* Turn off blocking */
             ssc.configureBlocking(false);
             
             /* Now bind this ServerSocketChannel to the correct TCP port */
             ssc.socket().bind(new InetSocketAddress(serverPort));        
         } catch(IOException e) {
             /* Alert a problem on the terminal and close the program */
             Main.programExit("Unable to ready ServerSocketChannel: " + e);
         }
         
         /* Return the channel, which is now configured */
         return ssc;
     }
     
     /** 
      * Prepares and returns a socket channel. We register this with a selector. 
      */
     private SocketChannel readySocketChannel(ServerSocketChannel ssc) {
         /* Declare a new SocketChannel pointer, currently null */
         SocketChannel sc = null;
         
         try {
             /* Accept and configure connection */
             sc = ssc.accept();
 
             /* Turn off blocking */
             sc.configureBlocking(false);
                         
             /* Register this SocketChannel with the selector, requesting that 
              * read operations will be the thing to look for */
             sc.register(listenSelector, SelectionKey.OP_READ);
         } catch (IOException e) {
             /* Alert a problem on the terminal */
             Main.consoleOutput("Unable to ready SocketChannel: " + e);
         }
         
         /* Return the channel, now accepted and registered to the selector */
         return sc;
     }
           
     /** 
      * Prepares and returns a selector. 
      */
     private Selector readySelector(ServerSocketChannel ssc) {
         
         /* Create a new Selector pointer */
         Selector selector = null;
         
         try {
             /* Open a selector, and register the given ServerSocketChannel to it */
             selector = Selector.open();
             ssc.register(selector, SelectionKey.OP_ACCEPT);
         } catch (ClosedChannelException e) {
             /* For some reason the server socket channel has closed?? */
             Main.programExit("ServerSocketChannel was closed before I could register: " + e);
         } catch (IOException e) {
             /* Alert a problem on the terminal */
             Main.programExit("Problem opening selector: " + e);
         }
         
         /* Return the selector, now with a registered ServerSocketChannel */
         return selector;
     }
     
 }
