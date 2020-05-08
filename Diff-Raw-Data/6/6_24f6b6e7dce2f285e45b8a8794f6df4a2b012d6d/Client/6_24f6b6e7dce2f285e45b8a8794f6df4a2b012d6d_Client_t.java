 package dungeon.client;
 
import dungeon.game.messages.DefeatEvent;
 import dungeon.game.messages.PlayerJoinCommand;
 import dungeon.game.messages.PlayerReadyCommand;
import dungeon.game.messages.WinEvent;
 import dungeon.messages.LifecycleEvent;
 import dungeon.messages.Mailman;
 import dungeon.messages.Message;
 import dungeon.messages.MessageHandler;
 import dungeon.models.Player;
 import dungeon.models.Room;
 import dungeon.models.World;
 import dungeon.models.messages.Transform;
 import dungeon.server.Server;
 import dungeon.ui.ServerConnection;
 import dungeon.ui.messages.ChatMessage;
 import dungeon.ui.messages.PlayerMessage;
 
 import java.io.IOException;
 import java.net.SocketException;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicReference;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * A client to the game server.
  *
  * This handles the communication with the server, holds a local version of the world and the ID of the player.
  */
 public class Client implements MessageHandler {
   private static final Logger LOGGER = Logger.getLogger(Client.class.getName());
 
   private final Mailman mailman;
 
   private final AtomicReference<Integer> playerId = new AtomicReference<>();
 
   private final AtomicReference<World> world = new AtomicReference<>();
 
   /**
    * The preferred name.
    */
   private String playerName;
 
   private ServerConnection serverConnection;
 
   private MessageForwarder messageForwarder;
 
   private Server server;
 
   public Client (Mailman mailman) {
     this.mailman = mailman;
   }
 
   @Override
   public void handleMessage (Message message) {
     if (message instanceof Transform) {
       this.world.set(this.world.get().apply((Transform)message));
    } else if (message == LifecycleEvent.SHUTDOWN || message instanceof WinEvent || message instanceof DefeatEvent) {
       this.stop();
     }
 
     if (this.serverConnection == null) {
       return;
     }
 
     if ((message instanceof PlayerMessage && ((PlayerMessage)message).getPlayerId() == this.playerId.get())
       || (message instanceof ChatMessage && ((ChatMessage)message).getAuthorId() == this.playerId.get())
       || (message instanceof PlayerReadyCommand && ((PlayerReadyCommand)message).getPlayerId() == this.playerId.get())) {
       try {
         this.serverConnection.write(message);
       } catch (IOException e) {
         LOGGER.log(Level.WARNING, "Could not send message to server", e);
         this.stop();
       }
     }
   }
 
   public void send (Message message) {
     this.mailman.send(message);
   }
 
   public void sendChatMessage (String message) {
     this.send(new ChatMessage(this.playerId.get(), this.playerName, message));
   }
 
   public int getPlayerId () {
     return this.playerId.get();
   }
 
   /**
    * @return The player object in the current {@link #world} belonging to this client
    */
   public Player getPlayer () {
     return this.world.get().getPlayer(this.getPlayerId());
   }
 
   /**
    * @return A list of all players in the world
    */
   public List<Player> getPlayers () {
     return this.world.get().getPlayers();
   }
 
   /**
    * @return A list of all players in the same room as {@code player}
    */
   public List<Player> getPlayersInRoom (Player player) {
     return this.world.get().getPlayersInRoom(player.getRoomId());
   }
 
   /**
    * @return The room that this client is currently in
    */
   public Room getCurrentRoom () {
     Player player = this.getPlayer();
 
     if (player == null) {
       return null;
     } else {
       return this.world.get().getCurrentRoom(player);
     }
   }
 
   /**
    * Set the name as whom the player wants to join the game.
    */
   public void setPlayerName (String playerName) {
     this.playerName = playerName;
   }
 
   public void connect (String host, int port) throws ConnectException {
     try {
       this.serverConnection = new ServerConnection(host, port);
     } catch (IOException e) {
       LOGGER.log(Level.WARNING, "Could not connect", e);
       throw new ConnectException();
     }
 
     LOGGER.info("Acquire ID");
 
     try {
       this.playerId.set((Integer)this.serverConnection.read());
     } catch (IOException e) {
       LOGGER.warning("Could not read from connection");
       throw new ConnectException();
     } catch (ClassNotFoundException e) {
       LOGGER.warning("Expected Integer");
       throw new ConnectException();
     }
 
     LOGGER.info("Synchronize world");
 
     try {
       this.world.set((World)this.serverConnection.read());
     } catch (IOException e) {
       LOGGER.log(Level.WARNING, "IOError", e);
       throw new ConnectException();
     } catch (ClassNotFoundException e) {
       LOGGER.log(Level.WARNING, "Expected World", e);
       throw new ConnectException();
     }
 
     LOGGER.info("Join player");
     Player player = new Player(this.playerId.get(), this.playerName);
 
     try {
       this.serverConnection.write(new PlayerJoinCommand(player));
     } catch (IOException e) {
       LOGGER.log(Level.WARNING, "Could not join", e);
       throw new ConnectException();
     }
 
     this.messageForwarder = new MessageForwarder(this);
     this.messageForwarder.start();
   }
 
   public void sendReady () {
     this.sendChatMessage("Ich bin bereit");
 
     this.send(new PlayerReadyCommand(this.playerId.get()));
   }
 
   public void startServer (int port) throws ServerStartException, ConnectException {
     try {
       this.server = new Server(port);
     } catch (Exception e) {
       LOGGER.log(Level.WARNING, "Could not start server", e);
       throw new ServerStartException();
     }
 
     this.server.start();
 
     try {
       Thread.sleep(500);
     } catch (InterruptedException e) {
       // Ignore
     }
 
     this.connect("localhost", port);
   }
 
   /**
    * Removes the player from the world/lobby and leaves the world.
    */
   public void disconnect () {
     this.send(new World.RemovePlayerTransform(this.getPlayerId()));
 
     this.stop();
   }
 
   private void stop () {
     LOGGER.info("Shutdown client");
 
     if (this.messageForwarder != null) {
       this.messageForwarder.stop();
       this.messageForwarder = null;
     }
 
     if (this.serverConnection != null) {
       this.serverConnection.close();
       this.serverConnection = null;
     }
 
     if (this.server != null) {
       this.server.stop();
       this.server = null;
     }
 
     this.mailman.send(new ServerDisconnected());
   }
 
   /**
    * Injects messages from the server into the local event system.
    */
   private static final class MessageForwarder implements Runnable {
     private static final Logger LOGGER = Logger.getLogger(MessageForwarder.class.getName());
 
     private final Client client;
 
     private final Thread thread = new Thread(this);
 
     private final AtomicBoolean running = new AtomicBoolean(false);
 
     private MessageForwarder (Client client) {
       this.client = client;
     }
 
     @Override
     public void run () {
       this.running.set(true);
 
       while (this.running.get()) {
         try {
           Object received = this.client.serverConnection.read();
 
           if (received instanceof Message) {
             if (received instanceof ChatMessage && ((ChatMessage)received).getAuthorId() == this.client.getPlayerId()) {
               // Prevent bouncing of chat messages
               continue;
             }
 
             this.client.mailman.send((Message)received);
           }
         } catch (SocketException e) {
           LOGGER.log(Level.INFO, "The socket has been closed");
           this.client.stop();
         } catch (IOException e) {
           LOGGER.log(Level.WARNING, "Something failed while receiving from the server", e);
           this.client.stop();
         } catch (ClassNotFoundException e) {
           LOGGER.log(Level.WARNING, "Received message of unknown class", e);
           this.client.stop();
         }
       }
     }
 
     public void start () {
       this.thread.start();
     }
 
     public void stop () {
       this.running.set(false);
     }
   }
 
   public static class ServerStartException extends Exception {
 
   }
 
   public static class ConnectException extends Exception {
 
   }
 }
