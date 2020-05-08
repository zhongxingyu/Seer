 /**
  * Copyright (C) 2013 Alexander Szczuczko
  *
  * This file may be modified and distributed under the terms
  * of the MIT license. See the LICENSE file for details.
  */
 package ca.szc.keratin.core.net.io;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import javax.net.SocketFactory;
 
 import org.pmw.tinylog.Logger;
 
 import ca.szc.keratin.core.event.IrcMessageEvent;
 import ca.szc.keratin.core.event.connection.IrcConnect;
 import ca.szc.keratin.core.event.connection.IrcDisconnect;
 import ca.szc.keratin.core.event.message.recieve.ReceiveChannelMode;
 import ca.szc.keratin.core.event.message.recieve.ReceiveInvite;
 import ca.szc.keratin.core.event.message.recieve.ReceiveJoin;
 import ca.szc.keratin.core.event.message.recieve.ReceiveKick;
 import ca.szc.keratin.core.event.message.recieve.ReceiveMode;
 import ca.szc.keratin.core.event.message.recieve.ReceiveNick;
 import ca.szc.keratin.core.event.message.recieve.ReceiveNotice;
 import ca.szc.keratin.core.event.message.recieve.ReceivePart;
 import ca.szc.keratin.core.event.message.recieve.ReceivePing;
 import ca.szc.keratin.core.event.message.recieve.ReceivePong;
 import ca.szc.keratin.core.event.message.recieve.ReceivePrivmsg;
 import ca.szc.keratin.core.event.message.recieve.ReceiveQuit;
 import ca.szc.keratin.core.event.message.recieve.ReceiveReply;
 import ca.szc.keratin.core.event.message.recieve.ReceiveTopic;
 import ca.szc.keratin.core.event.message.recieve.ReceiveUserMode;
 import ca.szc.keratin.core.net.mbassador.MBassadorWrapper;
 import ca.szc.keratin.core.net.message.InvalidMessageCommandException;
 import ca.szc.keratin.core.net.message.InvalidMessageException;
 import ca.szc.keratin.core.net.message.InvalidMessageParamException;
 import ca.szc.keratin.core.net.message.InvalidMessagePrefixException;
 import ca.szc.keratin.core.net.message.IrcMessage;
 
 /**
  * Receives lines from the socket and sends out MessageRecieve events
  */
 public class ConnectionThread
     extends Thread
 {
     /**
      * States for the run loop
      */
     private enum RunState
     {
         CONNECT, DISCONNECT, END, READ
     }
 
     /**
      * How long a read on the socket will block before throwing SocketTimeoutException
      */
     private static final int SOCKET_TIMEOUT = 20 * 1000;
 
     private static boolean isDigits( String str )
     {
         try
         {
             Integer.parseInt( str );
             return true;
         }
         catch ( NumberFormatException e )
         {
             return false;
         }
     }
 
     private final MBassadorWrapper bus;
 
     private final InetSocketAddress endpoint;
 
     private final OutputQueue wrappedOutputQueue;
 
     private final BlockingQueue<IrcMessage> outputQueue;
 
     private BufferedWriter outputWriter;
 
     private final Object outputWriterMutex = new Object();
 
     private final SocketFactory socketFactory;
 
     public ConnectionThread( MBassadorWrapper bus, InetSocketAddress endpoint, SocketFactory socketFactory )
     {
         this.bus = bus;
         this.endpoint = endpoint;
         this.socketFactory = socketFactory;
 
         outputQueue = new LinkedBlockingQueue<IrcMessage>();
         wrappedOutputQueue = new OutputQueue( outputQueue );
     }
 
     private void createOutput( Socket socket )
         throws IOException
     {
         OutputStream outputStream = socket.getOutputStream();
         synchronized ( outputWriterMutex )
         {
             outputWriter = new BufferedWriter( new OutputStreamWriter( outputStream, IoConfig.CHARSET ) );
 
             // Don't want to carry forward the old stuff from before reconnecting
             outputQueue.clear();
         }
         Logger.trace( "Output created" );
     }
 
     /**
      * Get the output queue for the socket. May be called immediately.
      */
     public OutputQueue getOutputQueue()
     {
         return wrappedOutputQueue;
     }
 
     @Override
     public void run()
     {
         Thread.currentThread().setName( "InputThread" );
         Logger.trace( "Input thread running" );
 
         // Converts submitted IrcMessage objects to a raw message and sends it.
         new Thread()
         {
             @Override
             public void run()
             {
                 Thread.currentThread().setName( "OutputThread" );
                 Logger.trace( "Output thread running" );
                 while ( !Thread.interrupted() )
                 {
                     IrcMessage msg;
                     try
                     {
                         msg = outputQueue.take();
                     }
                     catch ( InterruptedException e1 )
                     {
                         Logger.trace( "Interrupted" );
                         break;
                     }
 
                     String rawCommand = msg.toString();
 
                     if ( outputWriter != null )
                     {
                         try
                         {
                             writeLine( rawCommand );
                         }
                         catch ( IOException e )
                         {
                             Logger.trace( "Error writing IrcMessage: " + rawCommand );
                         }
                     }
                     else
                     {
                         Logger.error( "First connection has not been established, can't send IRC message '"
                             + rawCommand + "'" );
                     }
 
                     try
                     {
                         // Sending messages too close to each other can cause problems on some servers, if the order of
                         // the messages matters (ex: NICK must proceed USER at the start of a connection).
                         Thread.sleep( 50 );
                     }
                     catch ( InterruptedException e )
                     {
                         Logger.trace( "Interrupted" );
                         break;
                     }
                 }
                 Logger.trace( "Run loop ends, thread exiting" );
             }
         }.start();
 
         RunState state = RunState.CONNECT;
 
         Socket socket = null;
         BufferedReader input = null;
 
         while ( state != RunState.END )
         {
             if ( Thread.interrupted() )
             {
                 Logger.trace( "Interrupted, exiting" );
                 state = RunState.END;
             }
 
             if ( state == RunState.CONNECT )
             {
                 try
                 {
                     Logger.trace( "Creating/connecting socket" );
                     socket = socketFactory.createSocket( endpoint.getAddress(), endpoint.getPort() );
                     Logger.info( "Successfully connected socket" );
 
                     socket.setSoTimeout( SOCKET_TIMEOUT );
                     createOutput( socket );
 
                     InputStream inputStream = socket.getInputStream();
                     input = new BufferedReader( new InputStreamReader( inputStream, IoConfig.CHARSET ) );
                     Logger.trace( "Input created" );
 
                     bus.publishAsync( new IrcConnect( wrappedOutputQueue, socket ) );
 
                     state = RunState.READ;
                 }
                 catch ( IOException e )
                 {
                     Logger.error( e, "Failed to connect socket, sleeping before retrying." );
                 }
 
                 try
                 {
                     Thread.sleep( IoConfig.WAIT_TIME );
                 }
                 catch ( InterruptedException e )
                 {
                     state = RunState.END;
                 }
             }
             else if ( state == RunState.READ )
             {
                 try
                 {
                     String line = input.readLine();
                     if ( line != null )
                     {
                         // Logger.trace( "Got line " + line );
                         try
                         {
                             IrcMessage message = IrcMessage.parseMessage( line );
                             // String prefix = message.getPrefix();
                             String command = message.getCommand();
                             // String[] params = message.getParams();
 
                             IrcMessageEvent messageEvent = null;
 
                             try
                             {
                                 // INVITE
                                 if ( ReceiveInvite.COMMAND.equals( command ) )
                                     messageEvent = new ReceiveInvite( wrappedOutputQueue, message );
 
                                 // JOIN
                                 else if ( ReceiveJoin.COMMAND.equals( command ) )
                                     messageEvent = new ReceiveJoin( wrappedOutputQueue, message );
 
                                 // KICK
                                 else if ( ReceiveKick.COMMAND.equals( command ) )
                                     messageEvent = new ReceiveKick( wrappedOutputQueue, message );
 
                                 // MODE
                                 else if ( ReceiveMode.COMMAND.equals( command ) )
                                 {
                                     if ( message.getParams().length == 2 )
                                         messageEvent = new ReceiveUserMode( wrappedOutputQueue, message );
                                     else
                                         messageEvent = new ReceiveChannelMode( wrappedOutputQueue, message );
                                 }
 
                                 // NICK
                                 else if ( ReceiveNick.COMMAND.equals( command ) )
                                     messageEvent = new ReceiveNick( wrappedOutputQueue, message );
 
                                 // NOTICE
                                 else if ( ReceiveNotice.COMMAND.equals( command ) )
                                     messageEvent = new ReceiveNotice( wrappedOutputQueue, message );
 
                                 // PART
                                 else if ( ReceivePart.COMMAND.equals( command ) )
                                     messageEvent = new ReceivePart( wrappedOutputQueue, message );
 
                                 // PING
                                 else if ( ReceivePing.COMMAND.equals( command ) )
                                     messageEvent = new ReceivePing( wrappedOutputQueue, message );
 
                                 // PONG
                                 else if ( ReceivePong.COMMAND.equals( command ) )
                                     messageEvent = new ReceivePong( wrappedOutputQueue, message );
 
                                 // PRIVMSG
                                 else if ( ReceivePrivmsg.COMMAND.equals( command ) )
                                     messageEvent = new ReceivePrivmsg( wrappedOutputQueue, message );
 
                                 // QUIT
                                 else if ( ReceiveQuit.COMMAND.equals( command ) )
                                     messageEvent = new ReceiveQuit( wrappedOutputQueue, message );
 
                                 // replies
                                 else if ( isDigits( command ) )
                                     messageEvent = new ReceiveReply( wrappedOutputQueue, message );
 
                                 // TOPIC
                                 else if ( ReceiveTopic.COMMAND.equals( command ) )
                                     messageEvent = new ReceiveTopic( wrappedOutputQueue, message );
 
                                 // others
                                 else
                                     Logger.error( "Unknown message '" + message.toString().replace( "\n", "\\n" ) + "'" );
                             }
                             catch ( Exception e )
                             {
                                String type = "null";
                                if ( messageEvent != null )
                                    type = messageEvent.getClass().getSimpleName();
                                Logger.error( "Error when creating message event, type: " + type + ", content: "
                                    + messageEvent );
                             }
 
                             if ( messageEvent != null )
                             {
                                 Logger.trace( "Publishing message event type: "
                                     + messageEvent.getClass().getSimpleName() + ", content: " + messageEvent );
                                 bus.publishAsync( messageEvent );
                             }
                         }
                         catch ( IndexOutOfBoundsException | InvalidMessagePrefixException
                                         | InvalidMessageCommandException | InvalidMessageParamException e )
                         {
                             Logger.error( e, "Couldn't create IrcMessage instance out of parsed data from line " + line );
                         }
                         catch ( InvalidMessageException e )
                         {
                             Logger.error( e, "Couldn't parse line " + line );
                         }
                     }
                     else
                     {
                         // Definite connection loss
                         Logger.error( "Input end of stream" );
                         state = RunState.DISCONNECT;
                     }
                 }
                 catch ( SocketTimeoutException e )
                 {
                     Logger.trace( "Read line timed out, checking if connection is active" );
 
                     // Sending some data is pretty much the only way to tell if the TCP connection is still alive. We
                     // don't really care about the reply, just that sending it doesn't cause a connection error.
                     try
                     {
                         writeLine( "PING client" );
                     }
                     catch ( IOException e1 )
                     {
                         state = RunState.DISCONNECT;
                     }
                 }
                 catch ( IOException e )
                 {
                     Logger.error( e, "Could not read line" );
                     state = RunState.DISCONNECT;
                 }
             }
             else if ( state == RunState.DISCONNECT )
             {
                 if ( socket != null && !socket.isClosed() )
                 {
                     try
                     {
                         socket.close();
                     }
                     catch ( IOException e )
                     {
                         Logger.trace( e, "Error when closing open socket" );
                     }
                 }
 
                 bus.publishAsync( new IrcDisconnect( wrappedOutputQueue, socket ) );
 
                 Logger.info( "Disconnected, attempting reconnect." );
 
                 state = RunState.CONNECT;
             }
         }
         Logger.trace( "Run loop ends, thread exiting" );
     }
 
     private void writeLine( String line )
         throws IOException
     {
         synchronized ( outputWriterMutex )
         {
             Logger.trace( "Writing line: " + line );
 
             outputWriter.write( line + "\n" );
 
             try
             {
                 outputWriter.flush();
             }
             catch ( IOException e )
             {
                 Logger.error( e, "Could not flush output stream" );
             }
         }
     }
 }
