 package uk.codingbadgers.bFundamentals.bungee;
 
 import java.io.ByteArrayOutputStream;
 
 import org.bukkit.entity.Player;
 
 public interface BungeeMessenger {
 
    public void sendRawMessage(byte[] message);
     
    public void sendRawMessage(byte[] message, boolean queue);
 
     public void sendRawPlayerMessage(Player player, byte[] message);
 
     public void sendPlayerCommand(String command, Player player, String... args);
     
     public void sendBungeeCommand(String command, String... args);
 
     public void sendQueuedCommands();
 
     public void forwardMessage(String servers, String subchannel, ByteArrayOutputStream data);
 
     public void sendMessage(String player, String message);
 
     public void connect(String player, String server);
 
     public void getPlayerCount(String server);
 
     public void getPlayerList(String server);
 
     public void getIP(Player player);
 
 }
