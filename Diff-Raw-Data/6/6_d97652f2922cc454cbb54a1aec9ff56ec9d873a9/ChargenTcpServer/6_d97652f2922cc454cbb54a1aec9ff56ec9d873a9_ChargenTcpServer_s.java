 package edu.wcu.Chargen;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Scanner;
 
 /**
  * ChargenTcpServer is a class that extends AbstractChargenServer and provides
  * a concrete implementation of the listen() method using TCP. The method
  * repeatedly waits for a client to connect and responds appropriately.
  * This will be a serial server.
  *
  * @author Jeremy Stilwell
  * @author Alisha Hayman
  * @version 10/8/13.
  */
 public class ChargenTcpServer extends AbstractChargenServer {
 
     /* Default port for the to use */
     private static final int DEFAULT_PORT = 12343;
 
     /* number of the port to use */
     private final int port;
 
     /* the ChargenCharacterSource to be used */
     private final ChargenCharacterSource source;
 
     /**
      * The default constructor to be used.
      */
     public ChargenTcpServer ()
     {
         this(DEFAULT_PORT, new DefactoChargenCharacterSource());
     }
 
     /**
     * the constructor to be used when given a port number.
      *
      * @param port - the port number to be used.
      */
     public ChargenTcpServer (int port)
     {
         this(port, new DefactoChargenCharacterSource());
     }
 
     /**
      * The constructor to be used when passed the ChargenCharacterSource to use.
      *
      * @param source - the source to use for ChargenCharacterSource
      */
     public ChargenTcpServer (ChargenCharacterSource source)
     {
         this(DEFAULT_PORT, source);
     }
 
     /**
      * the constructor to be used by all other constructors to set the port and
      * source fields.
      *
      * @param port - the port number to be used.
      * @param source - the source to use for ChargenCharacterSource
      */
     public ChargenTcpServer (int port, ChargenCharacterSource source)
     {
         this.port = port;
         this.source = source;
     }
 
     /**
      * listen - this method listens for a connection from a client then receives
      * a flag to determine which character source to use.
      */
     @Override
     public void listen ()
     {
         String flag;
         /* try with resources to create sockets */
         try
                 (
             /* Make a connection to a client socket */
             ServerSocket serverSocket = new ServerSocket(this.port);
 
             /* Connect to a client socket */
             Socket clientSocket = serverSocket.accept();
 
             /* Create a scanner to catch the flag input of the client */
             Scanner inFromClient = new Scanner(new InputStreamReader(
                     clientSocket.getInputStream()));
 
             /* Create a print stream from the client socket to send the chars */
             PrintStream outToClient = new PrintStream(
                     clientSocket.getOutputStream())
                 ) {
             /* get the flag from the client */
             flag = inFromClient.next();
 
             /* Call helper method */
             flagHelper(flag);
 
             /* while the client is still going keep giving chars */
             while (!outToClient.checkError()) {
                 outToClient.println(this.getCharacterSource().getNextChar());
             }
         }
         catch (IOException ioe) {
            System.err.println("Unable to read data from an open socket.");
            System.err.println(ioe.toString());
         }
         /* start the server over */
         listen();
     }
 
     /**
      * flagHelper - helper method to set the character source
      *
      * @param flag the type of character source to be use
      */
     private void flagHelper (String flag)
     {
         switch (flag) {
             /* if its NAN set it to NonAlphaNumericCharacterSource */
             case "NAN":
                 this.changeSource(new NonAlphaNumericCharacterSource());
                 break;
             /* if its AN set it to AlphaNumericCharacterSource */
             case "AN":
                 this.changeSource(new AlphaNumericCharacterSource());
                 break;
             /* if its N set it to AlphaNumericCharacterSource */
             case "N":
                 this.changeSource(new NumericCharacterSource());
                 break;
             /* default is to set it to DefactoCharacterSource */
             default:
                 this.changeSource(new DefactoChargenCharacterSource());
                 break;
         }
     }
 }
