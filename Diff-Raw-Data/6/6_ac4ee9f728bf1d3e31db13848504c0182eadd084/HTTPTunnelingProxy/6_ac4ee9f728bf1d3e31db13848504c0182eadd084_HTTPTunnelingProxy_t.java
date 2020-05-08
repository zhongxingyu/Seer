 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.channels.ClosedChannelException;
 import java.nio.channels.Selector;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.nio.charset.CharsetEncoder;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ExecutorService;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.StringTokenizer;
 
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Logger;
 
 
 /**
  * Listens for HTTP CONNECT requests and forwards
  * established sockets to a local port. Implementing
  * the HTTP proxy tunneling spec defined in
  * http://tools.ietf.org/html/draft-luotonen-web-proxy-tunneling-01
  * @author charley
  */
 
 public class HTTPTunnelingProxy implements Runnable {
     private static final Logger logger = Logger.getLogger(HTTPTunnelingProxy.class);
     private static ConcurrentLinkedQueue<ByteBuffer> byteBuffers = new ConcurrentLinkedQueue<ByteBuffer>();
     public static Charset charset = Charset.forName("UTF-8");
     public static CharsetEncoder encoder = charset.newEncoder();
     public static CharsetDecoder decoder = charset.newDecoder();
     
     
     private int bindPort, forwardPort;
     private String forwardHost;
     private HashSet<SocketChannel> parsingChannels;
     private HashSet<SocketChannel> pendingCloseChannels;
     private HashSet<ChannelCoupler> couplers;
     private Selector selector;
     
     public ExecutorService executor;
     
     private ArrayList<ParseTask> connectTokenParser;
     private static interface ParseTask {
         public boolean parseToken(String token) throws TokenParsingException;
     }
     private static class TokenParsingException extends Exception {
         private static final long serialVersionUID = 1412549925669111979L;
         public String responseMessage;
         public TokenParsingException(String responseMessage) {
             this.responseMessage = responseMessage;
         }
     }
     
     private void setupParser() {
         //The parser is looking for something like this:
         //CONNECT home.netscape.com:443 HTTP/1.0
         //User-agent: Mozilla/4.0
         connectTokenParser = new ArrayList<ParseTask>(5);
         connectTokenParser.add(new ParseTask(){
             public boolean parseToken(String token) throws TokenParsingException {
                 if(token.equals("CONNECT")) {
                     return true;
                 } else {
                     throw new TokenParsingException("HTTP/1.0 501 Method Not Implemented\r\nProxy-agent: charley-proxy-extension/0.1\r\n");
                 }
             }
         });
         connectTokenParser.add(new ParseTask(){
             public boolean parseToken(String token) throws TokenParsingException {
                 if(token.startsWith(forwardHost+":") && token.endsWith(String.format("%d", forwardPort))) {
                     return true;
                 } else {
                     throw new TokenParsingException("HTTP/1.0 403 Forbidden\r\nProxy-agent: charley-proxy-extension/0.1\r\nProxy-message: this is not an open proxy, stupid\r\n");
                 }
             }
         });
         connectTokenParser.add(new ParseTask(){
             public boolean parseToken(String token) throws TokenParsingException {
                 if(token.equals("HTTP/1.0") || token.equals("HTTP/1.1")) {
                     return true;
                 } else {
                     throw new TokenParsingException("HTTP/1.0 400 Bad Request\r\nProxy-agent: charley-proxy-extension/0.1\r\n");
                 }
             }
         });
     }
     
     /**
      * Create a new proxy server on a given local port.
      */
     public HTTPTunnelingProxy(int bindPort, String forwardHost, int forwardPort) {
         this.bindPort = bindPort;
         this.forwardPort = forwardPort;
         this.forwardHost = forwardHost;
         couplers = new HashSet<ChannelCoupler>();
         parsingChannels = new HashSet<SocketChannel>();
         pendingCloseChannels = new HashSet<SocketChannel>();
         
         setupParser();
         
         //THIS CLASS BREAKS SUPERBAD IN MULTIPLE THREADS. DON'T DO THAT.
         this.executor = Executors.newSingleThreadExecutor();
         executor.submit(this);
     }
     
     public void run() {
         try {
             selector = Selector.open();
             
             ServerSocketChannel server = ServerSocketChannel.open();
             server.configureBlocking(false);
             server.socket().bind(new InetSocketAddress(this.bindPort));
             
             server.register(selector, SelectionKey.OP_ACCEPT);
             while(true){
                 if (selector.select() > 0) {
                     Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                     while (keys.hasNext()) {
                         SelectionKey selKey = keys.next();
                         keys.remove();
                         
                         if (selKey.isValid() && selKey.isAcceptable()) {
                             // a connection was accepted by a ServerSocketChannel.
                             ServerSocketChannel ssChannel = (ServerSocketChannel)selKey.channel();
                             //accept channel, create peer channel
                             acceptChannel(ssChannel.accept());
                         }
                         if (selKey.attachment() != null && selKey.attachment() instanceof ChannelCoupler) {
                             ChannelCoupler coupler = (ChannelCoupler)selKey.attachment();
                             if (!coupler.step(selKey)) {
                                 couplers.remove(coupler);
                             }
                         } else {
                             if (selKey.isValid() && selKey.isReadable()) {
                                 attemptUncoupledRead(selKey);
                             }
                             if (selKey.isValid() && selKey.isWritable()) {
                                 attemptUncoupledWrite(selKey);
                             }
                         }
                     }
                 }
             }
         } catch (Exception e) {
             logger.error("What the fuck is this shit?", e);
         }
     }
     
     private static synchronized ByteBuffer borrowByteBuffer() {
         ByteBuffer buffer = byteBuffers.poll();
         if (buffer == null) {
             return ByteBuffer.allocateDirect(4096);
         } else {
             return buffer;
         }
     }
     
     private static synchronized void returnByteBuffer(ByteBuffer buffer) {
         byteBuffers.offer(buffer);
     }
     
     /**
      * socket life cycle looks something like this:
      * accept -> read -> parse -> couple
      * @param channel
      */
     private void acceptChannel(SocketChannel channel) {
         if (channel == null) return;
         try {
             logger.info("Accepting Socket "+channel.socket().toString());
             channel.configureBlocking(false);
             parsingChannels.add(channel);
             SelectionKey key = channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
         } catch (ClosedChannelException e) {
             logger.error("", e);
         } catch (IOException e) {
             logger.error("", e);
         }
     }
     
     private void attemptUncoupledRead(SelectionKey key) {
         SocketChannel channel = (SocketChannel)key.channel();
         ByteBuffer buffer = borrowByteBuffer();
         buffer.clear();
         int bytesRead;
         try {
             bytesRead = channel.read(buffer);
             if (bytesRead > 0) {
                 buffer.flip();
                 String readString = decoder.decode(buffer).toString();
                 logger.debug("incoming unparsed data: "+readString);
                 StringTokenizer tokenizer = new StringTokenizer(readString);
                 if (tokenizer.countTokens() <= 1) {
                     throw new IOException("unable to tokenize input string");
                 }
                 Iterator<ParseTask> tokenParsers = connectTokenParser.iterator();
                 while (tokenizer.hasMoreTokens() && tokenParsers.hasNext()) {
                     String token = tokenizer.nextToken();
                     ParseTask task = tokenParsers.next();
                     logger.debug("Parsing token "+token);
                     try {
                         task.parseToken(token);
                     } catch (TokenParsingException e) {
                         logger.debug("Reached token parsing exception, will respond then destroy socket");
                         buffer.clear();
                         buffer.put(encoder.encode(CharBuffer.wrap(e.responseMessage)));
                         key.attach(buffer);
                         parsingChannels.remove(channel);
                         pendingCloseChannels.add(channel);
                         return;
                     }
                 }
                 parsingChannels.remove(channel);
                 openChannelCoupler(channel);
             } else {
                 logger.info(String.format("Destroying uncoupled socket %s (reached eof)", channel.socket().toString()));
                 destroySocketChannelSelectionKey(key);
             }
         } catch (IOException ioe) {
             logger.debug("ioexception on request parse. will destroy connection");
             destroySocketChannelSelectionKey(key);  
         }
     }
     
     /**
      * Kills a selection key, and the channel it was bound to.
      * If you want to send a message before killing the socket, use pendingCloseChannels
      * and schedule a write to the socket by attaching a ByteBuffer to it
      * @param key
      */
     private void destroySocketChannelSelectionKey(SelectionKey key) {
         try {
             if (key.attachment() != null && key.attachment() instanceof ByteBuffer) {
                 key.attach(null);
                 returnByteBuffer((ByteBuffer)key.attachment());
             }
             key.cancel();
             parsingChannels.remove(key.channel());
             pendingCloseChannels.remove(key.channel());
             key.channel().close();
         } catch (IOException e) {
             logger.error("", e);
         }
     }
     
     private void attemptUncoupledWrite(SelectionKey key) {
         try {
             SocketChannel channel = (SocketChannel)key.channel();
	    if (key.attachment() != null && key.attachment() instanceof ByteBuffer) {
		ByteBuffer buffer = (ByteBuffer)key.attachment();
		buffer.flip();
                 channel.write(buffer);
                 key.attach(null);
                 returnByteBuffer(buffer);
                 key.cancel();
                 if (parsingChannels.contains(channel)) {
                     logger.error("Inspect this error");
                 } else if (pendingCloseChannels.contains(channel)){
                     pendingCloseChannels.remove(channel);
                     logger.info(String.format("Destroying uncoupled socket %s (close pending)", channel.socket().toString()));
                     channel.close();
                 } else {
                     logger.error("Inspect this error");
                 }
             }
         } catch (IOException e) {
             logger.error("ioexception", e);
         }
     }
     
     private void openChannelCoupler(SocketChannel channel) throws IOException {
         SocketChannel sChannel = SocketChannel.open();
         sChannel.configureBlocking(false);
         InetSocketAddress address = new InetSocketAddress(forwardHost, forwardPort);
         sChannel.connect(address);
         
         logger.info(String.format("Opening Proxy Socket at %s for %s",address, channel.socket().toString()));
         ChannelCoupler coupler = new ChannelCoupler(channel, sChannel, selector);
         couplers.add(coupler);	
     }
     
     private static class ChannelCoupler {
         public SocketChannel leftToRight;
         public SocketChannel rightToLeft;
         public SelectionKey leftSelectionKey;
         public SelectionKey rightSelectionKey;
         
         private LinkedList<ByteBuffer> leftWriteQueue;
         private LinkedList<ByteBuffer> rightWriteQueue;
         
         public ChannelCoupler(SocketChannel channel1, SocketChannel channel2, Selector selector) throws IOException {
             leftToRight = channel1;
             rightToLeft = channel2;
             leftWriteQueue = new LinkedList<ByteBuffer>();
             rightWriteQueue = new LinkedList<ByteBuffer>();
             leftSelectionKey = leftToRight.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);
             rightSelectionKey = rightToLeft.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);
             leftSelectionKey.attach(this);
             rightSelectionKey.attach(this);
         }
         
         public boolean step(SelectionKey key) throws IOException {
             if (!(key == leftSelectionKey || key == rightSelectionKey)) {
                 logger.warn("invalid selectionkey for this channel coupler");
                 return false;
             }
             
             if (key.isValid() && key.isConnectable()) {
                 // a connection was established with a remote server.
                 SocketChannel sChannel = (SocketChannel)key.channel();
                 sChannel.finishConnect();
                 logger.info("Proxy Socket connected for peer "+leftToRight.socket().toString());
                 ByteBuffer buffer = borrowByteBuffer();
                 buffer.clear();
                 buffer.put(encoder.encode(CharBuffer.wrap("HTTP/1.0 200 Connection established\r\nProxy-agent: charley-proxy-extension/0.1\r\n\r\n")));
                 leftWriteQueue.add(buffer);
             }
             if (key.isValid() && key.isReadable()) {
                 // a channel is ready for reading
                 SocketChannel sChannel = (SocketChannel)key.channel();
                 readChannel(sChannel);
             }
             if (key.isValid() && key.isWritable()) {
                 // a channel is ready for writing
                 SocketChannel sChannel = (SocketChannel)key.channel();
                 writeChannel(sChannel);
             }
             
             return true;
         }
         
         private void readChannel(SocketChannel channel) throws IOException {
             ByteBuffer buffer = borrowByteBuffer();
             buffer.clear();
             int bytesRead = channel.read(buffer);
             if (bytesRead > 0) {
                 if (channel == rightToLeft) {
                     leftWriteQueue.addLast(buffer);
                 } else if (channel == leftToRight) {
                     rightWriteQueue.addLast(buffer);
                 } else {
                     logger.warn("Unknown socket");
                     returnByteBuffer(buffer);
                 }
             } else {
                 logger.info(String.format("Destroying socket couple %s and %s", leftToRight.socket().toString(), rightToLeft.socket().toString()));
                 returnByteBuffer(buffer);
                 leftSelectionKey.attach(null);
                 rightSelectionKey.attach(null);
                 leftSelectionKey.cancel();
                 rightSelectionKey.cancel();
                 rightToLeft.close();
                 leftToRight.close();
             }
         }
         
         private void writeChannel(SocketChannel channel) throws IOException {
             if (channel == rightToLeft) {
                 ByteBuffer buffer = rightWriteQueue.poll();
                 if (buffer != null) {
                     buffer.flip();
                     rightToLeft.write(buffer);
                     if (buffer.remaining() > 0) {
                         logger.debug("Requeueing further writes on right");
                         buffer.compact();
                         rightWriteQueue.addFirst(buffer);
                     } else {
                         returnByteBuffer(buffer);
                     }
                 }
             } else if (channel == leftToRight) {
                 ByteBuffer buffer = leftWriteQueue.poll();
                 if (buffer != null) {
                     buffer.flip();
                     leftToRight.write(buffer);
                     if (buffer.remaining() > 0) {
                         logger.debug("Requeueing further writes on left");
                         buffer.compact();
                         leftWriteQueue.addFirst(buffer);
                     } else {
                         returnByteBuffer(buffer);
                     }
                 }
             } else {
                 logger.warn("Unknown socket");
             }	
         }
         
     }
     
     public static void main(String[] args) {
         BasicConfigurator.configure();
         HTTPTunnelingProxy proxy = new HTTPTunnelingProxy(8080, "stage101-lax.tokbox.com", 5560);
         System.out.println("bound");
     }
 }
