 package org.hampelratte.net.mms.client;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.mina.core.future.CloseFuture;
 import org.apache.mina.core.future.ConnectFuture;
 import org.apache.mina.core.future.IoFuture;
 import org.apache.mina.core.future.IoFutureListener;
 import org.apache.mina.core.service.IoHandler;
 import org.apache.mina.core.service.IoHandlerAdapter;
 import org.apache.mina.core.session.IdleStatus;
 import org.apache.mina.core.session.IoSession;
 import org.apache.mina.filter.codec.ProtocolCodecFilter;
 import org.apache.mina.transport.socket.SocketConnector;
 import org.apache.mina.transport.socket.nio.NioSocketConnector;
 import org.hampelratte.net.mms.MMSObject;
 import org.hampelratte.net.mms.client.listeners.MMSMessageListener;
 import org.hampelratte.net.mms.client.listeners.MMSPacketListener;
 import org.hampelratte.net.mms.data.MMSPacket;
 import org.hampelratte.net.mms.messages.MMSMessage;
 import org.hampelratte.net.mms.messages.client.CancelProtocol;
 import org.hampelratte.net.mms.messages.client.MMSRequest;
 import org.hampelratte.net.mms.messages.client.StartPlaying;
 import org.hampelratte.net.mms.messages.server.ReportOpenFile;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class MMSClient extends IoHandlerAdapter {
     private static transient Logger logger = LoggerFactory.getLogger(MMSClient.class);
     
     /** Connect timeout in millisecods */ 
     public static int CONNECT_TIMEOUT = 30000;
 
     private List<MMSMessageListener> messageListeners = new ArrayList<MMSMessageListener>();
     private List<MMSPacketListener> packetListeners = new ArrayList<MMSPacketListener>();
     
     private List<IoHandler> additionalIoHandlers = new ArrayList<IoHandler>();
     
     private String host;
     private int port;
     private SocketConnector connector;
     private IoSession session;
     private MMSNegotiator negotiator;
     
     private long lastUpdate = 0;
     
     private long packetsReceived = 0;
     private long packetsreceivedAtLastLog = 0;
     
     public MMSClient(String host, int port, MMSNegotiator negotiator) {
         this.host = host;
         this.port = port;
         connector = new NioSocketConnector();
         //connector.getFilterChain().addFirst("logger", new RawInputStreamDumpFilter());
         connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ClientProtocolCodecFactory()));
         connector.setHandler(this);
         this.negotiator = negotiator;
         this.addMessageListener(negotiator);
         this.addPacketListener(negotiator);
     }
 
     // TODO fehler abfangen wie z.b. ein connect timeout
     public void connect() throws Exception {
         ConnectFuture connectFuture = connector.connect(new InetSocketAddress(host, port));
        connectFuture.awaitUninterruptibly(CONNECT_TIMEOUT);
        session = connectFuture.getSession();
         
         if(connectFuture != null) {
             connectFuture.addListener(new IoFutureListener<ConnectFuture>() {
                 public void operationComplete(ConnectFuture cf) {
                     // set throughput calculation interval
                     session.getConfig().setThroughputCalculationInterval(1);
                     
                     // set the first sequence number
                     session.setAttribute("mms.sequence", 0);
                     
                     // start the streaming negotiation
                     negotiator.start(session);
                 }
             });
         } else {
             exceptionCaught(session, new IOException("Connect to host failed"));
         }
     }
 
     public void disconnect(IoFutureListener<IoFuture> listener) {
         sendRequest(new CancelProtocol());
         
         // cancel protocol doesn't work -> kill the connection
         if (session != null) {
             CloseFuture future = session.close(true);
             future.addListener(listener);
         }
         
         if(connector != null) {
             connector.dispose();
         }
     }
     
     public void sendRequest(MMSRequest request) {
         if (session == null) {
             throw new RuntimeException("Not connected");
         } else {
             session.write(request);
             logger.debug("--OUT--> " + request.toString());
         }
     }
 
     public void messageReceived(IoSession session, Object message) throws Exception {
         for (IoHandler handler : additionalIoHandlers) {
             handler.messageReceived(session, message);
         }
         
         MMSObject mmso = (MMSObject) message;
         if(mmso instanceof MMSMessage) {
             logger.debug("<--IN-- " + mmso.toString());
             fireMessageReceived(mmso);
         } else {
             if( (++packetsReceived - packetsreceivedAtLastLog) >= 100) {
                 packetsreceivedAtLastLog = packetsReceived;
                 logger.debug("{} data packets received", packetsReceived);
             }
             firePacketReceived(mmso);
         }
     }
 
     private void firePacketReceived(MMSObject mmso) {
         for (MMSPacketListener listener : packetListeners) {
             listener.packetReceived((MMSPacket) mmso);
         }
     }
 
     private void fireMessageReceived(MMSObject mmso) {
         for (MMSMessageListener listener : messageListeners) {
             listener.messageReceived((MMSMessage) mmso);
         }
     }
 
     public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
         logger.warn("Exception occured", cause);
         for (IoHandler handler : additionalIoHandlers) {
             handler.exceptionCaught(session, cause);
         }
     }
     
     @Override
     public void sessionClosed(IoSession iosession) throws Exception {
         super.sessionClosed(iosession);
         logger.info("MMS connection closed");
         for (IoHandler handler : additionalIoHandlers) {
             handler.sessionClosed(iosession);
         }
     }
 
     public void addMessageListener(MMSMessageListener listener) {
         messageListeners.add(listener);
     }
     
     public void removeMessageListener(MMSMessageListener listener) {
         messageListeners.remove(listener);
     }
     
     public void addPacketListener(MMSPacketListener listener) {
         packetListeners.add(listener);
     }
     
     public void removePacketListener(MMSPacketListener listener) {
         packetListeners.remove(listener);
     }
     
     public void addAdditionalIoHandler(IoHandler handler) {
         additionalIoHandlers.add(handler);
     }
     
     public void removeAdditionalIoHandler(IoHandler handler) {
         additionalIoHandlers.remove(handler);
     }
     
     public double getSpeed() {
         if(session != null) {
             if( (System.currentTimeMillis() - lastUpdate) > 1000) {
                 lastUpdate = System.currentTimeMillis();
                 session.updateThroughput(System.currentTimeMillis(), true);
             }
             return session.getReadBytesThroughput() / 1024;
         }
         
         return 0;
     }
     
     @Override
     public void messageSent(IoSession iosession, Object obj) throws Exception {
         super.messageSent(iosession, obj);
         for (IoHandler handler : additionalIoHandlers) {
             handler.messageSent(iosession, obj);
         }
     }
 
     @Override
     public void sessionCreated(IoSession iosession) throws Exception {
         super.sessionCreated(iosession);
         for (IoHandler handler : additionalIoHandlers) {
             handler.sessionCreated(iosession);
         }
     }
 
     @Override
     public void sessionIdle(IoSession iosession, IdleStatus idlestatus) throws Exception {
         super.sessionIdle(iosession, idlestatus);
         for (IoHandler handler : additionalIoHandlers) {
             handler.sessionIdle(iosession, idlestatus);
         }
     }
 
     @Override
     public void sessionOpened(IoSession iosession) throws Exception {
         super.sessionOpened(iosession);
         for (IoHandler handler : additionalIoHandlers) {
             handler.sessionOpened(iosession);
         }
     }
 
     /**
      * Starts the streaming
      * @param startPacket the packetNumber from which the streaming should start
      */
     public void startStreaming(long startPacket) {
         StartPlaying sp = new StartPlaying();
         ReportOpenFile rof = (ReportOpenFile) session.getAttribute(ReportOpenFile.class);
         sp.setOpenFileId(rof.getOpenFileId());
         
         /* this confuses me: we use the packet number to seek the start of streaming.
          * in my opinion we should have to use setLocationId for packet numbers, but it
          * only works correctly with setAsfOffset ?!? 
          * maybe the sequence of the values is wrong in the spec */
         sp.setPosition(Double.MAX_VALUE);
         sp.setLocationId(0xFFFFFFFF);
         if(startPacket > 0) {
             sp.setAsfOffset(startPacket);
         }
         sendRequest(sp);
     }
 }
