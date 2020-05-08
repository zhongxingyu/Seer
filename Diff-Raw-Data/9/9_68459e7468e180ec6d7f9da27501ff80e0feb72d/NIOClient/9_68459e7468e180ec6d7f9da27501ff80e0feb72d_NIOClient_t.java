 /*
  * Created on May 12, 2006
  */
 package ecologylab.services.nio;
 
 import java.io.IOException;
 import java.net.BindException;
 import java.net.InetSocketAddress;
 import java.net.PortUnreachableException;
 import java.net.SocketException;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.SocketChannel;
 import java.nio.charset.CharacterCodingException;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.nio.charset.CharsetEncoder;
 import java.util.Iterator;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import ecologylab.appframework.ObjectRegistry;
 import ecologylab.generic.StartAndStoppable;
 import ecologylab.services.ServerConstants;
 import ecologylab.services.ServicesClientBase;
 import ecologylab.services.ServicesServer;
 import ecologylab.services.messages.RequestMessage;
 import ecologylab.services.messages.ResponseMessage;
 import ecologylab.xml.TranslationSpace;
 import ecologylab.xml.XmlTranslationException;
 
 /**
  * Services Client using NIO; a major difference with the NIO version is state
  * tracking. Since the sending methods do not wait for the server to return.
  * 
  * This object will listen for incoming messages from the server, and will send
  * any messages that it recieves on its end.
  * 
  * Since the underlying implementation is TCP/IP, messages sent should be sent
  * in order, and the responses should match that order.
  * 
  * Another major difference between this and the non-NIO version of
  * ServicesClient is that it is StartAndStoppable.
  * 
  * @author Zach Toups (toupsz@gmail.com)
  */
 public class NIOClient extends ServicesClientBase implements StartAndStoppable,
         Runnable, ServerConstants
 {
     protected Selector                            selector               = null;
 
     protected boolean                             running                = false;
 
     protected SocketChannel                       channel                = null;
 
     private Thread                                thread;
 
     protected SelectionKey                        key                    = null;
 
     private ByteBuffer                            incomingRawBytes       = ByteBuffer
                                                                                  .allocate(MAX_PACKET_SIZE);
 
     protected CharBuffer                          outgoingChars          = CharBuffer
                                                                                  .allocate(MAX_PACKET_SIZE);
 
     private StringBuilder                         accumulator            = new StringBuilder(
                                                                                  MAX_PACKET_SIZE);
 
     private CharsetDecoder                        decoder                = Charset
                                                                                  .forName(
                                                                                          "ASCII")
                                                                                  .newDecoder();
 
     protected CharsetEncoder                      encoder                = Charset
                                                                                  .forName(
                                                                                          "ASCII")
                                                                                  .newEncoder();
 
     private ResponseMessage                       responseMessage        = null;
 
     protected Iterator                            incoming;
 
     private volatile boolean                      blockingRequestPending = false;
 
     private LinkedBlockingQueue<ResponseMessage>  blockingResponsesQueue = new LinkedBlockingQueue<ResponseMessage>();
 
     protected LinkedBlockingQueue<RequestMessage> requestsQueue          = new LinkedBlockingQueue<RequestMessage>();
 
     /**
      * selectInterval is passed to select() when it is called in the run loop.
      * It is set to 0 indicating that the loop should block until the selector
      * picks up something interesting. However, if this class is subclassed, it
      * is possible to modify this value so that the select() will only block for
      * the number of ms supplied by this field. Thus, it is possible (by also
      * subclassing the sendData() method) to have this send data on an interval,
      * and then select.
      */
     protected long                                selectInterval         = 0;
 
     protected boolean                             isSending              = false;
 
     public NIOClient(String server, int port, TranslationSpace messageSpace,
             ObjectRegistry objectRegistry)
     {
         super(server, port, messageSpace, objectRegistry);
     }
 
     public void enqueueRequest(RequestMessage request)
     {
         synchronized (requestsQueue)
         {
             requestsQueue.add(request);
         }
 
         // debug("setting interest");
         key.interestOps(key.interestOps() | (SelectionKey.OP_WRITE));
 
         // debug("booting selector");
         selector.wakeup();
     }
 
     public void disconnect()
     {
         debug("Disconnecting...");
 
         try
         {
             if (connected())
             {
                 debug("connected; closing channel and selector.");
 
                 channel.close();
 
                 selector.wakeup();
 
                 selector.selectNow();
 
                 selector.close();
 
                 nullOut();
             }
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
     }
 
     private void nullOut()
     {
         debug("null out");
 
         socket = null;
         channel = null;
         selector = null;
         key = null;
     }
 
     public boolean connected()
     {
         return (channel != null) && !channel.isConnectionPending()
                 && channel.isConnected() && socket.isConnected();
     }
 
     /**
      * Side effect of calling start().
      */
     protected boolean createConnection()
     {
         try
         {
             // get the selector
             selector = Selector.open();
 
             // create the channel and connect it to the server
             channel = SocketChannel.open(new InetSocketAddress(server, port));
 
             // disable blocking
             channel.configureBlocking(false);
 
             // link in the socket from the channel
             socket = channel.socket();
 
             if (connected())
             {
                 // register the channel for read operations, now that it is
                 // connected
                 channel.register(selector, SelectionKey.OP_READ);
                 // channel.register(selector, SelectionKey.OP_WRITE);
                
                this.key = channel.keyFor(selector);
             }
         }
         catch (BindException e)
         {
             debug("Couldnt create socket connection to server '" + server
                     + "': " + e);
 
             nullOut();
         }
         catch (PortUnreachableException e)
         {
             debug("Server is alive, but has no daemon on port " + port + ": "
                     + e);
 
             nullOut();
         }
         catch (SocketException e)
         {
             debug("Server '" + server + "' unreachable: " + e);
 
             nullOut();
         }
         catch (IOException e)
         {
             debug("Bad response from server: " + e);
 
             nullOut();
         }
 
         return connected();
     }
 
     /**
      * Sends request, but does not wait for the response. The response gets
      * processed later in a non-stateful way by the run method.
      * 
      * @param request
      *            the request to send to the server.
      * 
      * @return the UID of request.
      */
     public long nonBlockingSendMessage(RequestMessage request)
             throws IOException
     {
         long outgoingUid = this.getUid();
 
         // debug("UID: "+outgoingUid);
 
         // translate the response and store it, then
         // encode it and write it
         if (connected())
         {
             try
             {
                 request.setUid(outgoingUid);
 
                 String outgoingReq = request.translateToXML(false);
                 String header = "content-length:" + outgoingReq.length()
                         + "\r\n\r\n";
 
                 outgoingChars.clear();
                 outgoingChars.put(header).put(outgoingReq);
                 outgoingChars.flip();
 
                 channel.write(encoder.encode(outgoingChars));
             }
             catch (NullPointerException e)
             {
                 e.printStackTrace();
                 System.out.println("recovering.");
             }
             catch (XmlTranslationException e)
             {
                 e.printStackTrace();
             }
             catch (CharacterCodingException e)
             {
                 e.printStackTrace();
             }
             catch (IOException e)
             {
                 debug("connection shut down at a bad time; stopping.");
                 this.stop();
             }
         }
         else
         {
             debug("Attempted to send message, but not connected.");
             throw new IOException(
                     "Attempted to send message, but not connected.");
         }
 
         // debug("just sent message: " + outgoingUid);
 
         return outgoingUid;
     }
 
     public synchronized ResponseMessage sendMessage(RequestMessage request)
     {
         return this.sendMessage(request, -1);
     }
 
     public synchronized ResponseMessage sendMessage(RequestMessage request,
             int timeOutMillis)
     {
         ResponseMessage returnValue = null;
 
         // notify the connection thread that we are waiting on a response
         blockingRequestPending = true;
 
         long currentMessageUid;
 
         boolean blockingRequestFailed = false;
         long startTime = System.currentTimeMillis();
         int accumulator = 0;
 
         try
         {
             currentMessageUid = this.nonBlockingSendMessage(request);
 
             selector.wakeup();
 
             // wait to be notified that the response has arrived
             while (blockingRequestPending && !blockingRequestFailed)
             {
                 debug("waiting on blocking request");
 
                 try
                 {
                     if (timeOutMillis > -1)
                     {
                         wait(timeOutMillis);
                     }
                     else
                     {
                         wait();
                     }
                 }
                 catch (InterruptedException e)
                 {
                     e.printStackTrace();
                 }
 
                 debug("waking");
 
                 accumulator += System.currentTimeMillis() - startTime;
                 startTime = System.currentTimeMillis();
 
                 while ((blockingRequestPending)
                         && (!blockingResponsesQueue.isEmpty()))
                 {
                     returnValue = blockingResponsesQueue.poll();
 
                     if (returnValue.getUid() == currentMessageUid)
                     {
                         debug("got the right response");
 
                         blockingRequestPending = false;
 
                         blockingResponsesQueue.clear();
 
                         return returnValue;
                     }
                     else
                     {
                         returnValue = null;
                     }
                 }
 
                 if ((timeOutMillis > -1) && (accumulator >= timeOutMillis)
                         && (blockingRequestPending))
                 {
                     blockingRequestFailed = true;
                 }
             }
 
             if (blockingRequestFailed)
             {
                 debug("Request failed due to timeout!");
             }
 
         }
         catch (IOException e1)
         {
             e1.printStackTrace();
         }
 
         return returnValue;
     }
 
     public void start()
     {
         running = true;
 
         if (thread == null)
         {
             thread = new Thread(this, "Client thread.");
             thread.start();
         }
     }
 
     public void stop()
     {
         System.err.println("shutting down client listening thread.");
 
         running = false;
 
         this.disconnect();
 
         // dispose of thread
         thread = null;
     }
 
     public final void run()
     {
         System.out.println("starting up run method.");
         while (running)
         {
             try
             {
                 if (connected())
                 {
                     /*
                      * if (isSending) { sendData(); }
                      * 
                      * if (selectInterval > 0) { runStartTime =
                      * System.currentTimeMillis(); }
                      * 
                      * debug("selectInterval = "+selectInterval);
                      */
 
                     // debug("going to select now");
                     if (selector.select(/* selectInterval */) > 0)
                     {
                         // there is something to read; only register one
                         // channel, so...
                         incoming = selector.selectedKeys().iterator();
 
                         while (incoming.hasNext())
                         {
                             key = (SelectionKey) incoming.next();
 
                             incoming.remove();
 
                             if (key.isValid())
                             {
                                 if (key.isReadable())
                                 {
                                     // debug("this key is readable!");
 
                                     readChannel();
                                 }
                                 else if (key.isWritable())
                                 {
                                     // debug("this key is writable!");
 
                                     synchronized (requestsQueue)
                                     {
                                         while (!requestsQueue.isEmpty())
                                         {
                                             this
                                                     .nonBlockingSendMessage(requestsQueue
                                                             .poll());
                                         }
                                     }
 
                                     key.interestOps(key.interestOps()
                                             & (~SelectionKey.OP_WRITE));
                                 }
                             }
                             else
                             { // the key is invalid; server disconnected
                                 disconnect();
 
                             }
                         }
                     }
                 }
             }
             catch (IOException e)
             {
                 e.printStackTrace();
             }
         }
 
         System.out.println("Thread shutting down.");
 
     }
 
     protected void readChannel()
     {
         int bytesRead = 0;
 
         try
         {
             while ((bytesRead = channel.read(incomingRawBytes)) > 0)
             {
                 incomingRawBytes.flip();
 
                 accumulator.append(decoder.decode(incomingRawBytes));
 
                 incomingRawBytes.clear();
 
                 if (accumulator.length() > 0)
                 {
                     // look for HTTP header
                     int endOfFirstHeader = accumulator.indexOf("\r\n\r\n");
 
                     if (endOfFirstHeader == -1)
                         break;
 
                     int length = ServicesServer.parseHeader(accumulator
                             .substring(0, endOfFirstHeader));
 
                     if (length == -1)
                         break;
 
                     String firstMessage;
 
                     try
                     {
                         firstMessage = accumulator.substring(
                                 endOfFirstHeader + 4, endOfFirstHeader + 4
                                         + length);
                         accumulator.delete(0, endOfFirstHeader + 4 + length);
                         // System.out.println(firstMessage);
                     }
                     catch (IndexOutOfBoundsException e)
                     {
                         debug("don't have a complete message yet; total length is "+accumulator.length());
                         e.printStackTrace();
                         break;
                     }
 
                     if (firstMessage != null)
                     {
 //                        System.out.println(firstMessage);
                         
                         if (!this.blockingRequestPending)
                         {
                             processString(firstMessage);
                         }
                         else
                         {
                             blockingResponsesQueue
                                     .add(processString(firstMessage));
                             synchronized (this)
                             {
                                 notify();
                             }
                         }
                     }
                 }
             }
 
             if (bytesRead == -1)
             {
                 debug("Read returned -1; disconnecting.");
 
                 disconnect();
             }
         }
         catch (CharacterCodingException e1)
         {
             e1.printStackTrace();
         }
         catch (IOException e1)
         {
             debug("IOException");
 
             if ("An existing connection was forcibly closed by the remote host"
                     .equals(e1.getMessage()))
             {
                 debug("Server shut down; disconnecting.");
 
                 disconnect();
             }
         }
     }
 
     /**
      * Hook method to allow subclasses to send data before selecting.
      * 
      */
     protected void sendData()
     {
     }
 
     protected void resetSelectInterval()
     {
         selectInterval = 0;
     }
 
     private ResponseMessage processString(String incomingMessage)
     {
         if (show(5))
             debug("incoming message: " + incomingMessage);
 
         try
         {
             responseMessage = translateXMLStringToResponseMessage(incomingMessage);
 
         }
         catch (XmlTranslationException e)
         {
             e.printStackTrace();
         }
 
         if (responseMessage == null)
         {
             debug("ERROR: translation failed: ");
         }
         else
         {
             // perform the service being requested
             processResponse(responseMessage);
         }
 
         // debug("just translated response: "+responseMessage.getUid());
 
         return responseMessage;
     }
 }
