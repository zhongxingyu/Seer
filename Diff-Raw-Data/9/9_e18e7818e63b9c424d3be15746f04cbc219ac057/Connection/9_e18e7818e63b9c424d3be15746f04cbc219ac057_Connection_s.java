 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.communication.connection;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InterruptedIOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.StringWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.apache.commons.lang.Validate;
 import org.eclipse.jubula.communication.ConfigurableLogger;
 import org.eclipse.jubula.communication.IExceptionHandler;
 import org.eclipse.jubula.communication.listener.IErrorHandler;
 import org.eclipse.jubula.communication.listener.IMessageHandler;
 import org.eclipse.jubula.communication.message.MessageHeader;
 import org.eclipse.jubula.communication.message.MessageHeader.InvalidHeaderVersionException;
 import org.eclipse.jubula.communication.parser.MessageHeaderSerializer;
 import org.eclipse.jubula.communication.writer.MessageWriter;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.SerialisationException;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * Class for sending and receiving messages (as strings) with a header which
  * contains the meta data. An instance is constructed with an open socket. 
  * 
  * This class is used by <code>Communicator</code> which is the class you
  * should use.
  * 
  * <p>
  * How to use (for further description see the documentation at the methods)
  * <p>
  * 
  * DefaultSocket socket = ... <br>
  * Connection con = new Connection(socket); <br>
  * <p>
  * con.addErrorHandler(...); <br>
  * con.addMessageHandler(...); <br>
  * <p>
  * start the reading thread con.startReading(); <br>
  * 
  * MessageHeader header = ... ; see documentation there for settings <br>
  * header.set...(...); <br>
  * String message = ... ;<br>
  * con.sendMessage(header, message); <br>
  * ...
  * <p>
  * to close the connection use <br>
  * con.close(); <br>
  * 
  * The connection will be closed, a new instance must be created. <br>
  * The reading thread is stopped immediately. <br>
  * <p>
  * 
  * @author BREDEX GmbH
  * @created 09.07.2004
  *
  */
 public class Connection {
     /**
      * <code>IO_STREAM_ENCODING</code>
      * Encoding of Output-/InputStream.
      */
     public static final String IO_STREAM_ENCODING = "UTF8"; //$NON-NLS-1$
 
     /** the first number for the sequence numbers */
     private static final long SEQUENCE_START = 1;
 
     /** the logger */
     private ConfigurableLogger m_logger = new ConfigurableLogger(
             LoggerFactory.getLogger(Connection.class));
 
     /**
      * the sequence number used by this connection to identify messages will be
      * increased after every send message
      */
     private long m_sequenceNumber;
 
     /** the socket to use for this connection */
     private Socket m_socket;
 
     /** reader for the input stream of the used socket */
     private BufferedReader m_inputStreamReader;
 
     /** the output stream of the used socket */
     private OutputStream m_outputStream;
 
     /** the set which contains the listeners for messages */
     private Set m_messageHandlers;
 
     /** the set which contains the listeners for error */
     private Set m_errorHandlers;
 
     /** the exception handler for reading from the network */
     private IExceptionHandler m_exceptionHandler = null;
     
     /** the thread which reads from the socket */
     private ReaderThread m_readerThread = null;
 
     /** boolean to avoid multiple notification of a shutdown */
     private boolean m_shutDownFired;
     
     /** The (de)-serializer of message headers. */
     private MessageHeaderSerializer m_headerSerializer;
     
     /**
      * constructor - initializes this connection, does not start with reading.
      * First register a message listener, then call startReading(). 
      * @param socket the socket to use
      * @throws IllegalArgumentException if the socket is null or the socket has no assigned streams
      */
     public Connection(DefaultClientSocket socket) 
         throws IOException, IllegalArgumentException {
         
         this(socket, socket.getInputStreamReader());
     }
 
     /**
      * constructor - initializes this connection, does not start with reading.
      * First register a message listener, then call startReading(). 
      * @param socket the socket to use
      * @param socketInputStreamReader The input stream reader for the given 
      *                                socket. This reader should always be used 
      *                                rather than creating a new reader.
      * @throws IllegalArgumentException if the socket is null or the socket has no assigned streams
      */
     public Connection(Socket socket, BufferedReader socketInputStreamReader) 
         throws IllegalArgumentException {
         
         // check parameter socket
         Validate.notNull(socket, "socket must not be null"); //$NON-NLS-1$    
         try {
             m_socket = socket;
             m_outputStream = m_socket.getOutputStream();
             m_inputStreamReader = socketInputStreamReader;
         } catch (IOException e) {
             throw new IllegalArgumentException("socket must be connected"); //$NON-NLS-1$
         }
         // initialize member variables
         m_shutDownFired = false;
         m_sequenceNumber = SEQUENCE_START;
         // use HashSets to store the registered handlers
         // the set for the handlers must be a set supporting remove!
         // see remove*Handler() AND fire*- methods
         m_messageHandlers = new HashSet();
         m_errorHandlers = new HashSet();
         m_headerSerializer = new MessageHeaderSerializer();
     }
 
     /**
      * synchronized method for retrieving a new sequence number from this
      * connection
      * @return an new sequenceNumber
      */
     public synchronized String getNextSequenceNumber() {
         if (m_sequenceNumber == Long.MAX_VALUE) {
             m_sequenceNumber = SEQUENCE_START;
         }
         String result = String.valueOf(m_sequenceNumber);
         m_sequenceNumber++;
         return result;
     }
 
     /**
      * Starts reading from the input stream of the socket (in a separated thread).
      * @param id for debugging purposes: mark the reader thread
      */
     public void startReading(String id) {
         ReaderThread readerThread = m_readerThread;
         if (readerThread == null) {
             readerThread = 
                 new ReaderThread("Connection.ReaderThread:" + id); //$NON-NLS-1$
             readerThread.setDaemon(true);
         }
         synchronized (readerThread) {
             if (!readerThread.isAlive()) {
                 readerThread.start();
             }
         }
         m_readerThread = readerThread;
     }
 
     /**
      * closes the socket immediately
      */
     public void close() {
         if (m_readerThread != null) {
             synchronized (m_readerThread) {
                 if (m_readerThread.isAlive()) {
                     m_readerThread.interrupt();
                 }
             }
         }
         try {
             m_socket.close();
         } catch (IOException ioe) {
             getLogger().debug("io error closing a socket", ioe); //$NON-NLS-1$
         }
     }
 
     /**
      * Adds a listener for received messages. An instance will not registered
      * twice. 
      * @param messageHandler - the listener to register, null objects are ignored
      */
     public void addMessageHandler(IMessageHandler messageHandler) {
         if (messageHandler != null) {
             synchronized (m_messageHandlers) {
                 m_messageHandlers.add(messageHandler);
             }
         }
     }
 
     /**
      * Removes the given messageHandler. 
      * @param messageHandler - the listener to remove, null objects are ignored
      */
     public void removeMessageHandler(IMessageHandler messageHandler) {
         if (messageHandler != null) {
             synchronized (m_messageHandlers) {
                 m_messageHandlers.remove(messageHandler);
             }
         }
     }
 
     /**
      * Adds an listener to notify in case of errors. An instance will not
      * registered twice. 
      * @param errorHandler - the listener to register, null objects are ignored
      */
     public void addErrorHandler(IErrorHandler errorHandler) {
         if (errorHandler != null) {
             synchronized (m_errorHandlers) {
                 m_errorHandlers.add(errorHandler);
             }
         }
     }
 
     /**
      * Removes the given errorHandler. 
      * @param errorHandler - the listener to remove, null objects are ignored
      */
     public void removeErrorHandler(IErrorHandler errorHandler) {
         if (errorHandler != null) {
             synchronized (m_errorHandlers) {
                 m_errorHandlers.remove(errorHandler);
             }
         }
     }
 
     /**
      * @return Returns the exceptionHandler.
      */
     public synchronized IExceptionHandler getExceptionHandler() {
         return m_exceptionHandler;
     }
     
     /**
      * @param exceptionHandler The exceptionHandler to set.
      */
     public synchronized void setExceptionHandler(
             IExceptionHandler exceptionHandler) {
         
         m_exceptionHandler = exceptionHandler;
     }
     
     /**
      * @return the remote IP address for this connection, or 
      *         <code>null</code> if not connected.
      */
     public InetAddress getAddress() {
         return m_socket.getInetAddress();
     }
     
     /**
      * A synchronized method for sending messages. If an IO error occurs, the
      * error handlers will be notified with sendFailed, shutDown AND an
      * IOException will be thrown. In case of a serialization error the error
      * handler will be notified with sendFailed(). The header is filled with the
      * message length 
      * @param header - the header for the message, must not be null
      * @param message - the message to send, must not be null
      * @throws IOException - if the message could not send due to an IOException
      * @throws IllegalArgumentException - if the given message is null
      */
     public synchronized void send(MessageHeader header, String message)
         throws IOException, IllegalArgumentException {
         // check parameter
         Validate.notNull(header, "Message header must not be null"); //$NON-NLS-1$
         Validate.notNull(message, "Message must not be null"); //$NON-NLS-1$
        MessageWriter writer = null;
         try {
             header.setMessageLength(message.length());
             String serializedHeader = m_headerSerializer.serialize(header);
             // create a buffered message writer
            writer = new MessageWriter(new OutputStreamWriter(
                     m_outputStream, IO_STREAM_ENCODING)); 
             // write header
             writer.write(MessageHeader.HEADER_START);
             writer.write(StringConstants.EMPTY + serializedHeader.length());
             writer.newLine();
             writer.write(serializedHeader); 
             // write message
             writer.write(message); 
             writer.flush();
             if (getLogger().isInfoEnabled()) {
                 getLogger().info("sent to " + m_socket.getRemoteSocketAddress() + " message with header: " + serializedHeader); //$NON-NLS-1$ //$NON-NLS-2$
             }
             if (getLogger().isDebugEnabled()) {
                 getLogger().debug("sent message: " + message); //$NON-NLS-1$
             }
         } catch (IOException ioe) {
             getLogger().error("send failed", ioe); //$NON-NLS-1$
             fireSendFailed(message, header);
             fireShutDown();
             throw ioe;
         } catch (SerialisationException se) {
             getLogger().error("serialisation of " //$NON-NLS-1$ 
                     + header.toString() + "failed", se); //$NON-NLS-1$
             fireSendFailed(message, header);
        } finally {
            if (writer != null) {
                writer.close();
            }
         }
     }
 
     /**
      * A synchronized method for notifying the error listeners with sendFailed()
      * @param message - the message which should be send
      * @param header - the header of the message
      */
     private synchronized void fireSendFailed(String message,
             MessageHeader header) {
         
         if (getLogger().isDebugEnabled()) {
             getLogger().debug("firing send failed, message=" + message); //$NON-NLS-1$
                     
         }
         Iterator iter = ((HashSet)((HashSet)m_errorHandlers).clone())
             .iterator();
         while (iter.hasNext()) {
             try {
                 ((IErrorHandler)iter.next()).sendFailed(header, message);
             } catch (Throwable t) {
                 getLogger().error("Exception while calling listener", t); //$NON-NLS-1$        
             }
         }
     }
 
     /**
      * A synchronized method for notifying the error listeners with shutDown().
      */
     private synchronized void fireShutDown() {
         if (!m_shutDownFired) {
             getLogger().debug("firing shutdown"); //$NON-NLS-1$
             Iterator iter = ((HashSet)((HashSet)m_errorHandlers).clone())
                 .iterator();
             while (iter.hasNext()) {
                 try {
                     ((IErrorHandler)iter.next()).shutDown();
                 } catch (Throwable t) {
                     getLogger().error("Exception while calling listener", t); //$NON-NLS-1$                         
                 }
             }
             // don't fire more than once
             m_shutDownFired = true;
         } else {
             getLogger().debug("shutdown already fired"); //$NON-NLS-1$
         }
     }
 
     /**
      * A synchronized method for notifying the message handlers 
      * @param header - the received message header
      * @param message - the received message
      */
     private void fireMessageReceived(MessageHeader header,
             String message) {
         if (getLogger().isDebugEnabled()) {
             getLogger().debug("firing message received, message=" + message); //$NON-NLS-1$         
         }
         Iterator iter;
         synchronized (this) {
             iter = ((HashSet)((HashSet)m_messageHandlers).clone()).iterator();
         }
         while (iter.hasNext()) {
             try {
                 ((IMessageHandler)iter.next()).received(header, message);
             } catch (Throwable t) {
                 getLogger().error("Exception while calling listener", t); //$NON-NLS-1$                   
             }            
         }
     }
     
     
 
     /**
      * A thread for reading from the inputStream of the socket. This thread
      * notifies the listeners. 
      * @author BREDEX GmbH
      * @created 13.07.2004
      *
      */
     private class ReaderThread extends Thread { 
         
         /**
          * default constructor
          * @param name identifies the thread
          */
         public ReaderThread(String name) {
             super(name);
         }
 
         /**
          * {@inheritDoc}
          */
         public void run() {
             while (!this.isInterrupted()) {
                 String headerLengthToken = null;
                 try {
                     if (!waitForInput()) {
                         return;
                     }
                     headerLengthToken = m_inputStreamReader.readLine();
                     int headerLength = Integer.parseInt(headerLengthToken);
                     String headerString = 
                         readString(m_inputStreamReader, headerLength);
                     if (getLogger().isInfoEnabled()) {
                         getLogger().info("read header: " + headerString); //$NON-NLS-1$ 
                     }
                     MessageHeader header = m_headerSerializer
                         .deserialize(headerString);
                     header.validateVersion();
                     String message = readString(m_inputStreamReader, header
                         .getMessageLength());
                     // notify message handlers
                     if (getLogger().isDebugEnabled()) {
                         getLogger().debug("read message: " + message); //$NON-NLS-1$ 
                     }
                     fireMessageReceived(header, message);
                 } catch (IOException ioe) {
                     // FIXME this is also used for stopping the AUT in a
                     // regular way (pressing the AUT-Stop Button).
                     // In a future release this should be handled in another way
                     /*
                      * exception while reading from input stream or writing to a
                      * buffered StringWriter => m_logger the message and stop
                      */
                     getLogger().debug("stopping reading either due to io exception or stopped AUT", ioe); //$NON-NLS-1$
                     fireShutDownAndFinish();
                 } catch (UnexpectedEofException e) {
                     getLogger().error("unexpected end of file while reading message", e); //$NON-NLS-1$
                     close();
                     fireShutDownAndFinish();
                 } catch (NumberFormatException e) {
                     getLogger().error("invalid header length token: " //$NON-NLS-1$
                         + headerLengthToken, e); 
                 } catch (InvalidHeaderVersionException ihve) {
                     getLogger().error(ihve.getLocalizedMessage(), ihve);
                 } catch (Throwable t) {
                     getLogger().error("exception raised", t); //$NON-NLS-1$
                     final IExceptionHandler exceptionHandler = 
                         getExceptionHandler();
                     if (exceptionHandler != null) {
                         if (!exceptionHandler.handle(t)) { // NOPMD by al on 3/19/07 1:44 PM
                             // handling the exception returns false -> stop
                             close();
                             fireShutDownAndFinish();
                         }
                     }
                 }
             }
 
             // expected shutdown
             fireShutDown();
         }
 
         /**
          * Reads a string containing <code>length</code> characters from the
          * passed buffered reader. 
          * @param reader The buffered socket reader
          * @param length The number of characters to read
          * @return The read string
          * @throws IOException If the read operation fails
          * @throws UnexpectedEofException If an end of file is encountered while reading length bytes of data.
          */
         private String readString(BufferedReader reader, int length)
             throws IOException, UnexpectedEofException {
             
             if (getLogger().isDebugEnabled()) {
                 getLogger().debug("readString len " + length); //$NON-NLS-1$
             }
             char[] headerChars = new char[length];
             int required = length;
             int filled = 0;
             while (required > 0) {
                 int nread = reader.read(headerChars, filled, required);
                 if (nread == -1) {
                     getLogger().error("received message part before unexpected eof: " //$NON-NLS-1$
                             + String.valueOf(headerChars));
                     // this is really a (serious) error !
                     throw new UnexpectedEofException("after reading " + filled  //$NON-NLS-1$
                                                 + " bytes of expected " //$NON-NLS-1$
                                                 + length + " bytes of string"); //$NON-NLS-1$
                 }
                 filled += nread;
                 required -= nread;
             }
             
             return String.valueOf(headerChars);
         }
         
         /**
          * waits for input, the sign MesageHeader.HEADER_START <br>
          * Partially transmitted messages are logged. 
          * @return true if the reading process should continue, false otherwise
          * @throws IOException if an IO errors occurs while reading from the input stream
          */
         private boolean waitForInput() throws IOException {
             int character = nextChar();
             final boolean createLogMessage = getLogger().isDebugEnabled();
             final StringWriter logMessage = new StringWriter();
             // character must be the sign for a new message
             while (!this.isInterrupted()
                     && (character != MessageHeader.HEADER_START)) {
                 if (character == -1) {
                     fireShutDownAndFinish();
                     if (this.isInterrupted()) {
                         return false;
                     }
                 }
                 character = nextChar();
                 if (createLogMessage) {
                     logMessage.write(character);
                 }
             }
             if (createLogMessage) {
                 logMessage.flush();
                 getLogger().debug("received a portion of a message:" //$NON-NLS-1$ 
                         + logMessage.toString());
             }
             return true;
         }
 
         /**
          * This method is a work around for some problems in the socket implementation. Sometimes
          * a broken connection is not detected. The timeout helps detecting some problems, but
          * there are still some left. 
          * @return next char from socket
          * @throws SocketException in case of network error
          * @throws IOException in case of network error
          */
         private int nextChar() throws SocketException, IOException {            
             int oldTimeout = m_socket.getSoTimeout();
             m_socket.setSoTimeout(5000); // loop every 5000 ms
             boolean loop = false;
             int character = -1;
             try {
                 do {
                     try {
                         loop = false;
                         character = m_inputStreamReader.read();
                     } catch (InterruptedIOException e) {
                         loop = true;
                     }
                 } while (loop && !isInterrupted());
             } finally {
                 m_socket.setSoTimeout(oldTimeout);
             }
             return character;
         }
 
         /**
          * end of stream is reached, so stop this thread,
          */
         private void fireShutDownAndFinish() {
             this.interrupt();
             fireShutDown();
         }
     }
 
     /**
      * Clears the list of error handlers and the list of message handlers.
      */
     public void clearListeners() {
         m_errorHandlers.clear();
         m_messageHandlers.clear();
     }
 
     /**
      * @return the logger
      */
     public ConfigurableLogger getLogger() {
         return m_logger;
     }
 }
