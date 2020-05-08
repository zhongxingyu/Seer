 package ping.pong.net.client.io;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 import javax.net.SocketFactory;
 import javax.net.ssl.SSLSocketFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ping.pong.net.client.Client;
 import ping.pong.net.client.ClientConnectionListener;
 import ping.pong.net.connection.Connection;
 import ping.pong.net.connection.ConnectionEvent;
 import ping.pong.net.connection.io.DataReader;
 import ping.pong.net.connection.io.DataWriter;
 import ping.pong.net.connection.DisconnectState;
 import ping.pong.net.connection.config.ConnectionConfiguration;
 import ping.pong.net.connection.config.ConnectionConfigFactory;
 import ping.pong.net.connection.DisconnectInfo;
 import ping.pong.net.connection.messaging.Envelope;
 import ping.pong.net.connection.messaging.MessageListener;
 
 /**
  * The Io Client Implementation of the Client interface.
  *
  * @author mfullen
  */
public final class IoClient<Message> implements Client<Message>
 {
     /**
      * The logger being user for this class
      */
     private static final Logger logger = LoggerFactory.getLogger(IoClient.class);
     /**
      * Invalid connection id
      */
     private static final int INVALID_CONNECTION_ID = -1;
     /**
      * The Connection for the Client
      */
     protected Connection<Message> connection = null;
     /**
      * The connection Configuration used when attempting to create a connection
      */
     protected ConnectionConfiguration config = null;
     /**
      * The list of Message listeners for this client
      */
     protected List<MessageListener> messageListeners = new ArrayList<MessageListener>();
     /**
      * The list of ConnectionListeners for this client
      */
     protected List<ClientConnectionListener> connectionListeners = new ArrayList<ClientConnectionListener>();
     /**
      * Allows for the use of a custom Data reader
      */
     protected DataReader customDataReader = null;
     /**
      * The custom Data Writer to be used
      */
     protected DataWriter customDataWriter = null;
 
     /**
      * Constructor for a default IoClient Implementation. Creates it based of
      * defaults for a Connection Configuration
      */
     public IoClient()
     {
         this(ConnectionConfigFactory.createConnectionConfiguration());
     }
 
     /**
      * Creates a Client Implementation based off the given
      * ConnectionConfiguration
      *
      * @param config the configuration to use
      */
     public IoClient(ConnectionConfiguration config)
     {
         this.config = config;
     }
 
     /**
      * getConnection creates a new connection if one doesn't exist. Attempts to connect
      * right away. If the connection fails to connect it returns null
      * If a connection is already active it returns that connection
      * @return
      */
     protected final Connection<Message> getConnection()
     {
         if (this.connection == null)
         {
             logger.warn("Creating new connection");
             try
             {
                 SocketFactory factory = config.isSsl() ? SSLSocketFactory.getDefault() : SocketFactory.getDefault();
                 Socket tcpSocket = factory.createSocket(config.getIpAddress(), config.getPort());
 
                 //if we have a custom data reader or writer use the ClientIoNonPPNConnection
                 boolean customSerialization = (this.customDataReader != null || this.customDataWriter != null);
                 this.connection = customSerialization
                         ? ClientIoConnectionFactory.<Message>createNonPPNConnection(config, customDataReader, customDataWriter, tcpSocket, null)
                         : ClientIoConnectionFactory.<Message>createPPNConnection(config, tcpSocket, null);
 
                 this.connection.addConnectionEventListener(new ConnectionEventImpl());
             }
             catch (IOException ex)
             {
                 logger.error("Error creating Client Socket", ex);
                 //todo add error handling to display why it couldnt connect
                 return null;
             }
         }
 
         return this.connection;
     }
 
     @Override
     public void start()
     {
         this.connection = this.getConnection();
         if (this.connection == null)
         {
             logger.error("Failed to connect to {} on port {}", this.config.getIpAddress(), this.config.getPort());
         }
         else if (this.connection.isConnected())
         {
             logger.warn("Can't start connection it is already running");
         }
         else
         {
             Thread connectionThread = new Thread(this.connection, "IoClientConnection");
             connectionThread.start();
             logger.info("Client connected to server {} on TCP port {}", this.config.getIpAddress(), this.config.getPort());
         }
     }
 
     @Override
     public void close()
     {
         if (this.connection == null)
         {
             logger.error("Connection is null");
             return;
         }
         this.connection.close();
         logger.info("Client Closed");
         this.connection = null;
     }
 
     @Override
     public boolean isConnected()
     {
         return this.connection != null && this.connection.isConnected();
     }
 
     @Override
     public int getId()
     {
         if (isConnected())
         {
             return this.connection.getConnectionId();
         }
         return INVALID_CONNECTION_ID;
     }
 
     @Override
     public void addMessageListener(MessageListener<? super Client, Message> listener)
     {
         boolean added = false;
         if (listener != null)
         {
             added = this.messageListeners.add(listener);
         }
         logger.trace("Add Message Listener: {}", added ? "Successful" : "Failure");
     }
 
     @Override
     public void removeMessageListener(MessageListener<? super Client, Message> listener)
     {
         boolean removed = false;
         if (listener != null)
         {
             removed = this.messageListeners.remove(listener);
         }
         logger.trace("Remove Message Listener: {}", removed ? "Successful" : "Failure");
     }
 
     @Override
     public void addConnectionListener(ClientConnectionListener listener)
     {
         boolean added = false;
         if (listener != null)
         {
             added = this.connectionListeners.add(listener);
         }
         logger.trace("Add Connection Listener: {}", added ? "Successful" : "Failure");
     }
 
     @Override
     public void removeConnectionListener(ClientConnectionListener listener)
     {
         boolean removed = false;
         if (listener != null)
         {
             removed = this.connectionListeners.remove(listener);
         }
         logger.trace("Remove Connection Listener: {}", removed ? "Successful" : "Failure");
     }
 
     @Override
     public void sendMessage(Envelope<Message> message)
     {
         if (this.connection == null)
         {
             logger.error("Connection is null. Please start the connection first and try again");
         }
         else if (this.connection.isConnected())
         {
             this.connection.sendMessage(message);
         }
         else
         {
             logger.warn("Cannot Send message, The client is not connected");
         }
     }
 
     public void setCustomDataReader(DataReader customDataReader)
     {
         this.customDataReader = customDataReader;
     }
 
     public void setCustomDataWriter(DataWriter customDataWriter)
     {
         this.customDataWriter = customDataWriter;
     }
 
     final class ConnectionEventImpl implements ConnectionEvent<Message>
     {
         public ConnectionEventImpl()
         {
         }
 
         @Override
         public void onSocketClosed()
         {
             for (ClientConnectionListener clientConnectionListener : connectionListeners)
             {
                 clientConnectionListener.clientDisconnected(IoClient.this, new DisconnectInfoImpl());
             }
         }
 
         @Override
         public void onSocketCreated()
         {
             for (ClientConnectionListener clientConnectionListener : connectionListeners)
             {
                 clientConnectionListener.clientConnected(IoClient.this);
             }
         }
 
         @Override
         public synchronized void onSocketReceivedMessage(Message message)
         {
             for (MessageListener<? super Client<Message>, Message> messageListener : messageListeners)
             {
                 messageListener.messageReceived(IoClient.this, message);
             }
         }
 
         final class DisconnectInfoImpl implements DisconnectInfo
         {
             public DisconnectInfoImpl()
             {
             }
 
             @Override
             public String getReason()
             {
                 return "something is wrong";
             }
 
             @Override
             public DisconnectState getDisconnectState()
             {
                 return DisconnectState.ERROR;
             }
         }
     }
 }
