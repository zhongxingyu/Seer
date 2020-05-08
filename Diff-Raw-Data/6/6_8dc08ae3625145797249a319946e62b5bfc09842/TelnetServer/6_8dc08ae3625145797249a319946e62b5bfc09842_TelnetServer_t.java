 package polly.telnet;
 
 import java.io.IOException;
import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import javax.net.ServerSocketFactory;
 
 import org.apache.log4j.Logger;
 
 import polly.PollyConfiguration;
 import polly.core.IrcManagerImpl;
 
 import de.skuzzle.polly.sdk.AbstractDisposable;
 import de.skuzzle.polly.sdk.eventlistener.MessageListener;
 import de.skuzzle.polly.sdk.exceptions.DisposingException;
 
 
 /**
  * 
  * @author Simon
  * @version 27.07.2011 ae73250
  */
 public class TelnetServer extends AbstractDisposable implements Runnable {
 
     private static Logger logger = Logger.getLogger(TelnetServer.class.getName());
     
     private Thread serverThread;
     private ServerSocket serverSocket;
     private ExecutorService connectionPool;
     private TelnetConnection connection;
     private AtomicBoolean closing;
     private PollyConfiguration config;
     private MessageListener handler;
     private IrcManagerImpl ircManager;
 
     
     
     public TelnetServer(PollyConfiguration config, IrcManagerImpl 
             ircManager, MessageListener messageHandler) 
                 throws UnknownHostException, IOException {
         
         this.config = config;
         this.handler = messageHandler;
         this.ircManager = ircManager;
         this.connectionPool = Executors.newSingleThreadExecutor();
         this.closing = new AtomicBoolean();
         
         ServerSocketFactory factory = ServerSocketFactory.getDefault();
        // ISSUE: 0000035
        this.serverSocket = factory.createServerSocket(config.getTelnetPort(), 10, 
            InetAddress.getByName("localhost")); 
     }
     
     
     
     public synchronized void start() {
         if (this.serverThread != null) {
             throw new IllegalStateException("Server already running.");
         } else if (this.closing.get()) {
             throw new IllegalStateException("Server is closed.");
         }
         this.serverThread = new Thread(this);
         this.serverThread.setName("TELNET_SERVER");
         this.serverThread.start();
     }
 
     
     
     @Override
     public void run() {
         try {
             while (!this.closing.get()) {                
                 logger.info("Waiting for incomming telnet connections...");
 
                 Socket connection = this.serverSocket.accept();
                 TelnetConnection newConnection = new TelnetConnection(connection, 
                     this.config, this.ircManager, this.handler);
 
                 if (this.connection == null || !this.connection.isConnected()) {
                     logger.info("New Telnet connection from '" + 
                         connection.getInetAddress() + "' accpeted.");
                     this.connection = newConnection;
                     this.ircManager.setTelnetConnection(newConnection);
                     this.connectionPool.execute(newConnection);
                 } else {
                     logger.info("Telnet connection rejected.");
                     newConnection.send("Connection limit exceeded. Closing connection.");
                     newConnection.close();
                 }
 
             }
         } catch (IOException e) {
             if (!closing.get()) {
                 logger.error("Error while accepting connection request.", e);
             }
         }
         
         logger.info("Telnet server shut down.");
     }
     
     
     
     @Override
     protected void actualDispose() throws DisposingException {
         this.closing.set(true);
         logger.debug("Closing telnet connections.");
         if (this.connection != null) {
             this.connection.close();
         }
         logger.debug("Closing connection thread pool.");
         this.connectionPool.shutdownNow();
         
         logger.debug("Shutting down telnet server.");
         try {
             this.serverSocket.close();
         } catch (IOException e) {
             logger.warn("Error while closing server socket.", e);
         }
     }
 }
