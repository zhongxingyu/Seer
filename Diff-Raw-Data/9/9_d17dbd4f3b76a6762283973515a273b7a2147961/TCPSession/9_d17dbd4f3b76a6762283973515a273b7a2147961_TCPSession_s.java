 /*
 * TCPSession.java  $Revision: 1.15 $ $Date: 2001/11/08 05:26:29 $
  *
  * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
  * Copyright (c) 2001 Huston Franklin.  All rights reserved.
  *
  * The contents of this file are subject to the Blocks Public License (the
  * "License"); You may not use this file except in compliance with the License.
  *
  * You may obtain a copy of the License at http://www.beepcore.org/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  */
 package org.beepcore.beep.transport.tcp;
 
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import java.net.Socket;
 import java.net.SocketException;
 
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.StringTokenizer;
 
 import org.beepcore.beep.core.BEEPException;
 import org.beepcore.beep.core.Channel;
 import org.beepcore.beep.core.Frame;
 import org.beepcore.beep.core.Message;
 import org.beepcore.beep.core.ProfileRegistry;
 import org.beepcore.beep.core.Session;
 import org.beepcore.beep.core.SessionCredential;
 import org.beepcore.beep.core.SessionTuningProperties;
 import org.beepcore.beep.util.BufferSegment;
 import org.beepcore.beep.util.Log;
 
 
 /**
  * Provides the TCP transport mapping for BEEP according to RFC 3081.
  *
  * @author Eric Dixon
  * @author Huston Franklin
  * @author Jay Kint
  * @author Scott Pead
 * @version $Revision: 1.15 $, $Date: 2001/11/08 05:26:29 $
  */
 public class TCPSession extends Session {
 
     // Constants
     private static final String ERR_SEND_FRAME_FAILED =
         "Unable to send a frame";
     private static final String ERR_TCP_BUFFER_TOO_LARGE = "";
     private static final String SEQ_PREFIX = "SEQ ";
     private static final char NEWLINE_CHAR = '\n';
     private static final int DEFAULT_PROPERTIES_SIZE = 4;
     private static final int DEFAULT_RECEIVE_BUFFER_SIZE = 4 * 1024;
     private static final int MAX_RECEIVE_BUFFER_SIZE = 64 * 1024;
     private static final int MIN_RECEIVE_BUFFER_SIZE = 4 * 1024;
     private static final int SEQ_LENGTH = SEQ_PREFIX.length();
     private static final String TCP_MAPPING = "TCP Mapping";
     private static final String CRLF = "\r\n";
 
     private static final int CHANNEL_START_ODD = 1;
     private static final int CHANNEL_START_EVEN = 2;
 
 
     // Instance Data
     // @todo had these per stack, but have
     // since changed my tune, since we'll be thread/session
     // for probably a while yet...this'll help on performance.
     // reusing fixed size buffers.
     private byte headerBuffer[] = new byte[Frame.MAX_HEADER_SIZE];
     private Object writerLock;
     private Socket socket;
     private boolean running;
     private static int THREAD_COUNT = 0;
     private static final String THREAD_NAME = "TCPSession Thread #";
     private Thread thread;
 
     /**
      * @param sock the Socket for this TCPConnection
      *
      * @registry the ProfileRegistry (set of profiles) to be used on
      * this Session.
      *
      * @firstChannel the integer indicating the
      * ordinality for this Peer, which determines whether Channels
      * started by this peer have odd or even numbers.
      *
      * @param registry
      * @param localCred
      * @param peerCred
      *
      * The hack-ish part of this is the credential parameter.  Here's
      * the deal.  (1) TCPSessionCreator has public methods that don't
      * expose the credential (2) Therefore if it's called by the User,
      * it can only have a null credential.  (3) If it's called via a
      * Tuning Profile reset though, it can have a real value.  (4)
      * When that's the case, we call a 'different' init method that
      * doesn't block the thread of the former (soon to die) session.
      *
      * @param firstChannel
      *
      * @throws BEEPException
      */
     private TCPSession(Socket sock, ProfileRegistry registry, int firstChannel,
                        SessionCredential localCred, SessionCredential peerCred,
                        SessionTuningProperties tuning)
             throws BEEPException
     {
         super(registry, firstChannel, localCred, peerCred, tuning);
 
         socket = sock;
         writerLock = new Object();
 
         if ((peerCred != null) || (localCred != null) || (tuning != null)) {
             tuningInit();
         } else {
             init();
         }
 
         try {
             socket.setReceiveBufferSize(MAX_RECEIVE_BUFFER_SIZE);
         } catch (Exception x) {
             Log.logEntry(Log.SEV_DEBUG,
                          "Socket doesn't support setting receive buffer size");
         }
     }
 
     /**
      * Creates a TCPSession for a Socket that was created by
      * initiating a connection.
      *
      *
      * @param sock
      * @param registry
      *
      * @throws BEEPException
      *
      */
     public static TCPSession createInitiator(Socket sock,
                                              ProfileRegistry registry)
             throws BEEPException
     {
         return new TCPSession(sock, (ProfileRegistry) registry.clone(),
                               CHANNEL_START_ODD, null, null, null);
     }
 
     /**
      * Creates a TCPSession for a Socket that was created by
      * listening and accepting a connection.
      *
      *
      * @param sock
      * @param registry
      *
      * @throws BEEPException
      *
      */
     public static TCPSession createListener(Socket sock,
                                             ProfileRegistry registry)
             throws BEEPException
     {
         return new TCPSession(sock, (ProfileRegistry) registry.clone(),
                               CHANNEL_START_EVEN, null, null, null);
     }
 
     // Overrides method in Session
     public synchronized void close() throws BEEPException
     {
         super.close();
 
         if (socket != null) {
             try {
                 socket.close();
             } catch (IOException e) {
             }
 
             socket = null;
         }
     }
 
     public Socket getSocket()
     {
         return this.socket;
     }
 
     // Overrides method in Session
     public void terminate(String reason)
     {
         super.terminate(reason);
 
         if (socket != null) {
             try {
                 socket.close();
             } catch (IOException e) {
             }
 
             socket = null;
         }
     }
 
     // Implementation of method declared in Session
     protected void disableIO()
     {
         running = false;
     }
 
     // Implementation of method declared in Session
     protected void enableIO()
     {
         running = false;
         thread = null;
 
         if (thread == null) {
             String threadName;
 
             synchronized (THREAD_NAME) {
                 threadName = new String(THREAD_NAME + THREAD_COUNT++);
             }
 
             thread = new Thread(new SessionThread(), threadName);
 
             thread.setDaemon(true);
             thread.start();
         }
     }
 
     // Implementation of method declared in Session
     protected int getMaxFrameSize()
     {
         /**
          * @todo - test this and find an optimal frame size, key it up
          * to approximate ethernet packet size, less header, trailer
          */
         return 1400;
     }
 
     /**
      * Generates a header, then writes the header, payload, and
      * trailer to the wire.
      *
      * @param f the Frame to send.
      * @returns boolean true of the frame was sent, false otherwise.
      *
      * @throws BEEPException
      *
      * @todo make this one write operation later.
      */
     protected void sendFrame(Frame f) throws BEEPException
     {
         try {
 
             // @todo consider the costs of colliding these into one
             // buffer and one write..test to see if it's faster
             // this way or faster copying.
             OutputStream os = socket.getOutputStream();
             byte[] header = null;
 
             synchronized (writerLock) {
                 if (Log.isLogged(Log.SEV_DEBUG_VERBOSE)) {
                     Log.logEntry(Log.SEV_DEBUG_VERBOSE, TCP_MAPPING,
                                  "Wrote the following\n");
                 }
 
                 Iterator i = f.getBytes();
 
                 while (i.hasNext()) {
                     BufferSegment b = (BufferSegment) i.next();
 
                     os.write(b.getData(), b.getOffset(), b.getLength());
 
                     if (Log.isLogged(Log.SEV_DEBUG_VERBOSE)) {
                         Log.logEntry(Log.SEV_DEBUG_VERBOSE, TCP_MAPPING,
                                      new String(b.getData(), b.getOffset(),
                                                 b.getLength()));
                     }
                 }
 
                 os.flush();
             }
         } catch (IOException e) {
             throw new BEEPException(e.toString());
         } catch (Exception e) {
             throw new BEEPException(e.toString());
         }
     }
 
     // Implementation of method declared in Session
     protected Session reset(SessionCredential localCred,
                             SessionCredential peerCred,
                             SessionTuningProperties tuning,
                             ProfileRegistry reg, Object argument)
             throws BEEPException
     {
         Log.logEntry(Log.SEV_DEBUG, TCP_MAPPING,
                      "Reset as "
                      + (isInitiator() ? "INITIATOR" : "LISTENER"));
 
         Socket s = null;
 
         try {
             s = (Socket) argument;
         } catch (ClassCastException x) {
             s = socket;
         }
 
         if (reg == null) {
             reg = this.getProfileRegistry();
         }
 
         if (isInitiator()) {
             return new TCPSession(s, reg, CHANNEL_START_ODD,
                                   localCred, peerCred, tuning);
         } else {
             return new TCPSession(s, reg, CHANNEL_START_EVEN,
                                   localCred, peerCred, tuning);
         }
     }
 
     /**
      * Update the channel window size with the remote peer by sending
      * SEQ frames as per RFC 3081.
      *
      *
      * @param channel
      * @param previouslySeq
      * @param currentSeq
      * @param previouslyUsed
      * @param currentlyUsed
      * @param bufferSize
      *
      * @return true if the Receive Buffer Size was updated
      *
      * @throws BEEPException if a specified buffer size is larger
      *    than what's available on the Socket.
      *
      */
     protected boolean updateMyReceiveBufferSize(Channel channel,
                                                 long previouslySeq,
                                                 long currentSeq,
                                                 int previouslyUsed,
                                                 int currentlyUsed,
                                                 int bufferSize)
             throws BEEPException
     {
         if (Log.isLogged(Log.SEV_DEBUG)) {
             Log.logEntry(Log.SEV_DEBUG, TCP_MAPPING,
                          "update SEQ channel=" + channel.getNumber()
                          + " prevSeq=" + previouslySeq + " curSeq="
                          + currentSeq + " prevUsed=" + previouslyUsed
                          + " curUsed=" + currentlyUsed + " bufSize="
                          + bufferSize);
         }
 
         // @todo update the java-doc to correctly identify the params
         /*
         if (currentSeq > 0) {    // don't send it the first time
             if (((currentSeq - previouslySeq) < (bufferSize / 2))
                     || (currentlyUsed > (bufferSize / 2))) {
                 return false;
             }
         }
         */
 
         StringBuffer sb = new StringBuffer(Frame.MAX_HEADER_SIZE);
 
         sb.append(SEQ_PREFIX);
         sb.append(this.getChannelNumberAsString(channel));
         sb.append(' ');
         sb.append(Long.toString(currentSeq));
         sb.append(' ');
         sb.append(Integer.toString(bufferSize - currentlyUsed));
         sb.append(CRLF);
 
         try {
             if (Log.isLogged(Log.SEV_DEBUG)) {
                 Log.logEntry(Log.SEV_DEBUG, TCP_MAPPING,
                              "Wrote: " + sb.toString());
             }
 
             OutputStream os = socket.getOutputStream();
 
             synchronized (writerLock) {
                 os.write(sb.toString().getBytes("UTF-8"));
                 os.flush();
             }
         } catch (IOException x) {
             throw new BEEPException("Unable to send SEQ" + x.getMessage());
         }
 
         return true;
     }
 
     // Lame hack for J++
     private boolean modState(int i) throws BEEPException
     {
         return super.changeState(i);
     }
 
     /**
      * This specialization is designed to process Frames unique
      * to the TCP mapping (e.g. SEQ frames).  If the Frame is
      * not an SEQ frame, it calls the regular TCPSession
      * processNextFrame( int ) with an integer that simply
      * indicates how far it's read ahead into the stream.
      * @exception Throws an exception if it encounters a poorly
      * formed header or an IOException in the underlying socket
      * stream.
      *
      * @throws BEEPException
      */
     private void processNextFrame()
         throws BEEPException, IOException, SessionAbortedException
     {
         if (Log.isLogged(Log.SEV_DEBUG_VERBOSE)) {
             Log.logEntry(Log.SEV_DEBUG_VERBOSE, TCP_MAPPING,
                          "Processing next frame");
         }
 
         int length = 0;
         InputStream is = socket.getInputStream();
 
         headerBuffer[SEQ_LENGTH] = 0;
 
         while (true) {
             try {
                 int b = is.read();
 
                 if (b == -1) {
                     throw new SessionAbortedException();
                 }
 
                 headerBuffer[length] = (byte) b;
 
             } catch (java.net.SocketException e) {
                 if (getState() == SESSION_STATE_ACTIVE) {
                     throw e;
                 }
 
                 // socket closed intentionally (session closing) so just return
                 return;
             }
 
             if (headerBuffer[length] == '\n') {
                 if ((length == 0) || (headerBuffer[length - 1] != '\r')) {
                     throw new BEEPException("Malformed BEEP header");
                 }
 
                 break;
             }
 
             ++length;
 
             if (length == Frame.MAX_HEADER_SIZE) {
                 throw new BEEPException("Malformed BEEP header, no CRLF");
             }
         }
 
         if (Log.isLogged(Log.SEV_DEBUG)) {
             Log.logEntry(Log.SEV_DEBUG, TCP_MAPPING,
                          "Processing: "
                          + new String(headerBuffer, 0, length));
         }
 
         // If this is not a SEQ frame build a <code>Frame</code> and
         // read in the payload and verify the TRAILER.
         if (headerBuffer[0] != (byte) SEQ_PREFIX.charAt(0)) {
             Frame f = super.createFrame(headerBuffer, length);
             byte[] payload = new byte[f.getSize()];
 
             for (int count = 0; count < payload.length; ) {
 		int n = is.read(payload, count, payload.length - count);
                 if (n == -1) {
                     throw new SessionAbortedException();
                 }
                 count += n;
             }
 
             if (Log.isLogged(Log.SEV_DEBUG_VERBOSE)) {
                 Log.logEntry(Log.SEV_DEBUG_VERBOSE, TCP_MAPPING,
                              new String(payload));
             }
 
             for (int i = 0; i < Frame.TRAILER.length(); ++i) {
                 int b = is.read();
 
                 if (b == -1) {
                     throw new SessionAbortedException();
                 }
 
                 if (((byte) b) != ((byte) Frame.TRAILER.charAt(i))) {
                     throw new BEEPException("Malformed BEEP frame, "
                                             + "invalid trailer");
                 }
             }
 
             f.addPayload(new BufferSegment(payload));
             super.postFrame(f);
 
             return;
         }
 
         // Process the header
         StringTokenizer st = new StringTokenizer(new String(headerBuffer, 0,
                                                             length));
 
         if (st.countTokens() != 4) {
 
             // This should just shut the session down.
             Log.logEntry(Log.SEV_ERROR, TCP_MAPPING, "Malformed BEEP header");
 
             throw new BEEPException("Malformed BEEP header");
         }
 
         // Skip the SEQ
         if (st.nextToken().equals("SEQ") == false) {
             throw new BEEPException("Malformed BEEP header");
         }
 
         // Read the Channel Number
         int channelNum = Integer.parseInt(st.nextToken());
 
         // Read the Ack Number
         long ackNum = Long.parseLong(st.nextToken());
 
         // Read the Window Number
         int window = Integer.parseInt(st.nextToken());
 
         // update the channel with the new receive window size
         this.updatePeerReceiveBufferSize(channelNum, ackNum, window);
     }
 
     private class SessionThread implements Runnable {
 
         public void run()
         {
             try {
                 running = true;
 
                 // Listen for and post the Greeting Frame
                 while ((getState() != SESSION_STATE_ACTIVE) && running) {
                     processNextFrame();
                 }
 
                 // Keep processing frames as long as we're active
                 while ((getState() == SESSION_STATE_ACTIVE) && running) {
                     processNextFrame();
                 }
             } catch (IOException e) {
                 Log.logEntry(Log.SEV_ERROR, TCP_MAPPING, e);
 
                 socket = null;
 
                 terminate(e.getMessage());
             } catch (SessionAbortedException e) {
                 terminate("Session aborted by remote peer.");
             } catch (Throwable e) {
                 Log.logEntry(Log.SEV_ERROR, TCP_MAPPING, e);
                 terminate(e.getMessage());
             }
 
             Log.logEntry(Log.SEV_DEBUG, TCP_MAPPING,
                          "Session listener thread exiting.  State = "
                          + TCPSession.this.getState());
         }
     }
 
     private static class SessionAbortedException extends Exception {
     }
 }
