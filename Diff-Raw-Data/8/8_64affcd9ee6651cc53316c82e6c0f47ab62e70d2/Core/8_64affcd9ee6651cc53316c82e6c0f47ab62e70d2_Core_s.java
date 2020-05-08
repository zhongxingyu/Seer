 /*
  * Copyright (C) 2013 MineStar.de 
  * 
  * This file is part of ConAir.
  * 
  * ConAir is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * ConAir is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with ConAir.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.conair.core;
 
 import java.io.File;
 import java.io.IOException;
import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitTask;
 
 import de.minestar.conair.listener.ChatListener;
 import de.minestar.conair.network.PacketType;
 import de.minestar.conair.network.client.ChatClient;
 import de.minestar.conair.network.packets.ChatPacket;
 
 public class Core extends JavaPlugin {
 
     public static String serverPrefix = "", serverName = "";
     public static ChatColor prefixColor = ChatColor.AQUA;
 
     public static final String NAME = "ConAir";
 
     private int port;
     private String host;
     private ChatClient chatClient;
     private BukkitTask clientTask = null;
 
     private ChatListener chatListener;
 
     @Override
     public void onEnable() {
         readConfig();
 
         this.registerPackets();
 
         if (createChatClient(port, host)) {
             System.out.println("Connected to " + host + ":" + port);
         } else {
             System.out.println("NO CHATCLIENT CREATED!");
         }
 
         enableListener(this.getServer().getPluginManager());
     }
 
     private void registerPackets() {
         PacketType.registerPacket(ChatPacket.class);
     }
 
     private void enableListener(PluginManager pm) {
         this.chatListener = new ChatListener(this.chatClient);
         pm.registerEvents(this.chatListener, this);
     }
 
     private void readConfig() {
         File configFile = new File(getDataFolder(), "config.yml");
         // Create default config when no config was found
         if (!configFile.exists()) {
             try {
                 YamlConfiguration.loadConfiguration(this.getResource("config.yml")).save(configFile);
             } catch (IOException e) {
                 e.printStackTrace();
                 return;
             }
         }
         YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
 
         port = config.getInt("port", 9000);
         serverPrefix = config.getString("prefix", "[M]");
         serverName = config.getString("servername", "Main");
         prefixColor = ChatColor.values()[config.getInt("color", ChatColor.AQUA.ordinal())];
         if (prefixColor == null) {
             prefixColor = ChatColor.AQUA;
         }
         host = config.getString("host", "localhost");
     }
 
     private boolean createChatClient(int port, String host) {
         try {
             this.chatClient = new ChatClient(new BukkitPacketHandler(), host, port);
             clientTask = this.getServer().getScheduler().runTaskAsynchronously(this, this.chatClient);
             return true;
         } catch (Exception e) {
             e.printStackTrace();
            Logger.getLogger(NAME).throwing("de.minestar.conair.core.Core", "createChatClient", e);
             return false;
         }
     }
 
     @Override
     public void onDisable() {
         if (this.chatClient != null) {
             this.chatClient.stop();
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (!sender.isOp()) {
             sender.sendMessage(ChatColor.RED + "You are not allowed to use this command!");
             return true;
         }
 
         if (!label.startsWith("/"))
             label = "/" + label;
 
        if (label.startsWith("/reconnect ")) {
             if (this.chatClient != null) {
                 this.clientTask.cancel();
                 this.chatClient.stop();
                 this.chatClient = null;
             }
 
             this.readConfig();
 
             if (createChatClient(port, host)) {
                 sender.sendMessage(ChatColor.GREEN + "Connected to " + host + ":" + port);
             } else {
                 sender.sendMessage(ChatColor.RED + "Could not connect!");
             }
             this.chatListener.setClient(this.chatClient);
         }
         return true;
     }
 }
