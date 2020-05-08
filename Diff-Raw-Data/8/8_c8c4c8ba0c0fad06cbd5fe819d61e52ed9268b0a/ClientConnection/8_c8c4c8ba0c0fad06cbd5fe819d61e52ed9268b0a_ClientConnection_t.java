 package dungeon.server;
 
 import dungeon.game.LogicHandler;
 import dungeon.messages.Mailman;
 import dungeon.messages.Message;
 import dungeon.server.commands.CloseConnection;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
import java.net.SocketException;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Sends messages to the client and injects all received messages into the server mailman.
  */
 public class ClientConnection implements Runnable {
   private static final Logger LOGGER = Logger.getLogger(ClientConnection.class.getName());
 
   private final Server server;
 
   private final Socket socket;
 
   private final Mailman mailman;
 
   private final ObjectInputStream inputStream;
 
   private final ObjectOutputStream outputStream;
 
   private final LogicHandler logicHandler;
 
   /**
    * The player ID that will be assigned to the client.
    */
   private final int playerId;
 
   /**
    * Is the connection still open?
    */
   private final AtomicBoolean open = new AtomicBoolean(false);
 
   public ClientConnection (Server server, Socket socket, Mailman mailman, LogicHandler logicHandler) throws IOException {
     this.server = server;
     this.socket = socket;
     this.mailman = mailman;
     this.logicHandler = logicHandler;
 
     this.playerId = this.logicHandler.nextId();
 
     try {
       this.outputStream = new ObjectOutputStream(socket.getOutputStream());
       this.inputStream = new ObjectInputStream(socket.getInputStream());
     } catch (IOException e) {
       LOGGER.log(Level.WARNING, "Could not create in/out streams", e);
 
       throw e;
     }
   }
 
   @Override
   public void run () {
     this.open.set(true);
 
     LOGGER.info("Send ID");
     this.send(this.playerId);
 
     LOGGER.info("Send world object");
     this.send(this.logicHandler.getWorld());
 
     this.receiveMessages();
   }
 
   /**
    * Close the connection from the server side.
    */
   public void close () {
     if (!this.open.get()) {
       return;
     }
 
     this.open.set(false);
 
     LOGGER.info("Shutdown client connection");
 
     this.send(new CloseConnection());
 
     this.closeStreams();
   }
 
   public void send (Object object) {
     try {
       if (this.open.get()) {
         this.outputStream.writeObject(object);
       }
     } catch (IOException e) {
       LOGGER.log(Level.WARNING, "Could not send object " + object, e);
 
       this.closedByClient();
     }
   }
 
   private void receiveMessages () {
     while (this.open.get()) {
       Object received;
 
       try {
         received = this.read();
      } catch (SocketException e) {
        // Socket has been closed. Do nothing
        return;
       } catch (IOException e) {
         LOGGER.log(Level.WARNING, "Connection is broken", e);
         this.closedByClient();
         return;
       } catch (ClassNotFoundException e) {
         LOGGER.log(Level.WARNING, "Received object of unkown class", e);
         this.closedByClient();
         return;
       }
 
       if (received instanceof Message) {
         this.mailman.send((Message)received);
       } else if (received instanceof CloseConnection) {
         LOGGER.info("Client disconnected");
 
         this.closedByClient();
       }
     }
   }
 
   private Object read () throws IOException, ClassNotFoundException {
     return this.inputStream.readObject();
   }
 
   /**
    * The connection was somehow closed/corrupted from the client side.
    */
   private void closedByClient () {
     if (!this.open.get()) {
       return;
     }
 
     this.open.set(false);
 
     this.server.removeConnection(this);
 
     this.closeStreams();
   }
 
   private void closeStreams () {
     try {
       this.inputStream.close();
     } catch (IOException e) {
       LOGGER.log(Level.INFO, "Could not close stream", e);
     }
 
     try {
       this.outputStream.close();
     } catch (IOException e) {
       LOGGER.log(Level.INFO, "Could not close stream", e);
     }
 
     try {
       this.socket.close();
     } catch (IOException e) {
       LOGGER.log(Level.INFO, "Could not close socket", e);
     }
   }
 }
