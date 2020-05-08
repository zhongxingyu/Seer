 package uk.codingbadgers.bFundamentals.bungee;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import uk.codingbadgers.bFundamentals.bFundamentals;
 
 public class SimpleBungeeMessenger implements BungeeMessenger {
 
     private List<byte[]> queue = null;
     
     public SimpleBungeeMessenger() {
         queue = new ArrayList<byte[]>();
         Bukkit.getPluginManager().registerEvents(new BungeePlayerListener(this), bFundamentals.getInstance());
     }
     
     @Override
    public void sendRawMessage(byte[] message) {
        sendRawMessage(message, true);
     }
 
     @Override
    public void sendRawMessage(byte[] message, boolean shouldQueue) {
         if (Bukkit.getOnlinePlayers().length > 0) {
             Player p = Bukkit.getOnlinePlayers()[0];
             p.sendPluginMessage(bFundamentals.getInstance(), "BungeeCord", message);
         } else if (shouldQueue){
             queue.add(message);
         }
     }
     
     @Override
     public void sendRawPlayerMessage(Player player, byte[] message) {
         if (player.isOnline()) {
             player.sendPluginMessage(bFundamentals.getInstance(), "BungeeCord", message);
         }
     }
 
     @Override
     public void sendPlayerCommand(String command, Player player, String... args) {
         byte[] message = new byte[0];
         
         try {
             ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(b);
             
             out.writeUTF(command);
             for (String arg : args) {
                 out.writeUTF(arg);
             }
             
             message = b.toByteArray();
         } catch (IOException e) {
             e.printStackTrace();
             return;
         }
         
         sendRawPlayerMessage(player, message);
     }
     
     @Override
     public void sendBungeeCommand(String command, String... args) {
         byte[] message = new byte[0];
         
         try {
             ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(b);
             
             out.writeUTF(command);
             for (String arg : args) {
                 out.writeUTF(arg);
             }
             
             message = b.toByteArray();
         } catch (IOException e) {
             e.printStackTrace();
             return;
         }
         
         sendRawMessage(message);
     }
     
     @Override
     public void sendQueuedCommands() {
        for (byte[] message : queue) {
            sendRawMessage(message, false);
         }
     }
     
     @Override
     public void forwardMessage(String servers, String subchannel, ByteArrayOutputStream data) {
         byte[] message = new byte[0];
         
         try {
             ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(b);
             out.writeUTF("FORWARD");
             out.writeUTF(servers);
             out.writeUTF(subchannel);
             
             out.writeShort(data.toByteArray().length);
             out.write(data.toByteArray());
             
             message = b.toByteArray();
         } catch (IOException e) {
             e.printStackTrace();
             return;
         }
         
         sendRawMessage(message);
     }
     
     @Override
     public void sendMessage(String player, String message) {
         sendBungeeCommand("Message", player, message);
     }
     
     @Override
     public void connect(String player, String server) {
         sendBungeeCommand("ConnectOther", player, server);
     }
     
     @Override
     public void getPlayerCount(String server) {
         sendBungeeCommand("PlayerCount", server);
     }
     
     @Override
     public void getPlayerList(String server) {
         sendBungeeCommand("PlayerList", server);
     }
     
     @Override
     public void getIP(Player player) {
         sendPlayerCommand("IP", player);
     }
 }
