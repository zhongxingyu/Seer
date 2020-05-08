 package com.minecarts.familyjewels;
 
 import java.util.logging.Logger;
 import java.util.logging.Level;
 
 import com.minecarts.familyjewels.listener.PlayerListener;
 import org.bukkit.Location;
 import org.bukkit.event.Event;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 import net.minecraft.server.NetServerHandler;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.craftbukkit.CraftServer;
 
 
 public class FamilyJewels extends JavaPlugin{
     public final Logger log = Logger.getLogger("com.minecarts.familyjewels");
 
     private PluginDescriptionFile pdf;
     private Configuration config;
     private PlayerListener playerListener;
 
     public void onEnable() {
         PluginManager pm = getServer().getPluginManager();
         PluginDescriptionFile pdf = getDescription();
         this.playerListener = new PlayerListener(this);
 
         pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener, Event.Priority.Monitor, this);
         pm.registerEvent(Event.Type.PLAYER_QUIT, this.playerListener, Event.Priority.Monitor, this);
 
        for(Player p : getServer().getOnlinePlayers()){
            this.hookNSH(p);
        }

        log("Enabled");
     }
 
     public void onDisable(){
         for(Player p : getServer().getOnlinePlayers()){
             this.unhookNSH(p);
         }
        log("Disabled");
     }
 
     public void hookNSH(Player player){
         CraftPlayer craftPlayer = (CraftPlayer) player;
         CraftServer server = (CraftServer) getServer();
 
         Location loc = player.getLocation();
         NetServerHandlerHook handlerHook = new NetServerHandlerHook(server.getHandle().server, craftPlayer.getHandle().netServerHandler.networkManager, craftPlayer.getHandle());
         handlerHook.a(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
         craftPlayer.getHandle().netServerHandler = handlerHook;
 
         System.out.println("Hooked NSH for: " + player);
     }
     public void unhookNSH(Player player){
         CraftPlayer craftPlayer = (CraftPlayer) player;
         CraftServer server = (CraftServer) getServer();
 
         Location loc = player.getLocation();
         NetServerHandler handler = new NetServerHandler(server.getHandle().server,craftPlayer.getHandle().netServerHandler.networkManager, craftPlayer.getHandle());
         handler.a(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
         craftPlayer.getHandle().netServerHandler = handler;
 
         System.out.println("Unhooked NSH for: " + player);
     }
 
     public void log(String message, java.util.logging.Level level){
         this.log.log(level, "Something:" + "> " + message);
     }
     public void log(String message){
         this.log(message,Level.INFO);
     }
 }
