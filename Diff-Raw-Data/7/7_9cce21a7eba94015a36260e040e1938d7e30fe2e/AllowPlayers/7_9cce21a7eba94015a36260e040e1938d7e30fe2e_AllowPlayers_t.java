 /*
  * Copyright 2011-2012 James Geboski <jgeboski@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.AllowPlayers;
 
 import java.io.File;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 
 import com.ensifera.animosity.craftirc.CraftIRC;
 import com.ensifera.animosity.craftirc.EndPoint;
 import com.ensifera.animosity.craftirc.RelayedMessage;
 
 import org.AllowPlayers.command.CAllowPlayers;
 import org.AllowPlayers.command.COnlineMode;
 import org.AllowPlayers.util.Log;
 import org.AllowPlayers.util.Message;
 import org.AllowPlayers.storage.Storage;
 import org.AllowPlayers.storage.StorageException;
 import org.AllowPlayers.storage.StorageManager;
 
 public class AllowPlayers extends JavaPlugin
 {
     public static final String pluginName = "AllowPlayers";
 
     public Configuration  config;
     public Watcher        watcher;
     public StorageManager storage;
 
     private EventListener events;
 
     public boolean enabled;
     public boolean online;
 
     public CraftIRC craftirc;
     public APPoint  apPoint;
 
     public void onLoad()
     {
         config   = new Configuration(new File(getDataFolder(), "config.yml"));
         events   = new EventListener(this);
         watcher  = new Watcher(this);
         storage  = null;
 
         enabled  = true;
         online   = true;
 
         craftirc = null;
         apPoint  = null;
     }
 
     public void onEnable()
     {
         PluginManager pm;
         Plugin p;
 
         pm = getServer().getPluginManager();
         config.load();
 
         try {
             if (config.storageType.equalsIgnoreCase("auto"))
                 storage = StorageManager.create(pm);
             else
                 storage = StorageManager.create(pm, config.storageType);
         } catch (StorageException e) {
             Log.severe(e.getMessage());
             setEnabled(false);
             return;
         }
 
         Log.info("Storage plugin: %s", storage.getType().getName());
 
         if (config.ircEnabled) {
             p  = pm.getPlugin("CraftIRC");
 
             if ((p != null) && p.isEnabled()) {
                 craftirc = (CraftIRC) p;
                 apPoint  = new APPoint();
             }
 
             if (!registerEndPoint(config.ircTag, apPoint))
                 config.ircEnabled = false;
         }
 
         events.register();
         watcher.start();
 
         getCommand("allowplayers").setExecutor(new CAllowPlayers(this));
         getCommand("onlinemode").setExecutor(new COnlineMode(this));
     }
 
     public void onDisable()
     {
         if (config.ircEnabled)
             craftirc.unregisterEndPoint(config.ircTag);
 
         try {
             watcher.quit();
             watcher.join();
         } catch (Exception e) {}
     }
 
     public void reload()
     {
         if (config.ircEnabled)
             craftirc.unregisterEndPoint(config.ircTag);
 
         config.load();
 
         if (config.ircEnabled && !registerEndPoint(config.ircTag, apPoint))
             config.ircEnabled = false;
 
         watcher.reset();
     }
 
     private boolean registerEndPoint(String tag, Object ep)
     {
         if (craftirc == null)
             return false;
 
         if (craftirc.registerEndPoint(tag, (EndPoint) ep))
             return true;
 
         Log.severe("Unable to register CraftIRC tag: %s", tag);
         return false;
     }
 
     public boolean hasPermissionM(CommandSender sender, String perm)
     {
         if (sender.hasPermission(perm))
             return true;
 
         Message.severe(sender, "You don't have permission for that.");
         return false;
     }
 
     public void broadcast(String perm, String format, Object ... args)
     {
         String msg;
 
         msg = String.format(format, args);
 
         for (Player p : getServer().getOnlinePlayers()) {
             if (p.hasPermission(perm))
                 Message.info(p, msg);
         }
 
         Log.info(format, args);
 
         if (!config.ircEnabled)
             return;
 
        RelayedMessage rmsg;

        /* This typecasting is needed to prevent a ClassNotFoundException
         * from being thrown over com.ensifera.animosity.craftirc.EndPoint.
         */
        rmsg = craftirc.newMsg((EndPoint) ((Object) apPoint), null, "chat");
 
         if (!config.ircColored)
             msg = ChatColor.stripColor(msg);
 
         rmsg.setField("realSender", pluginName);
         rmsg.setField("sender",     pluginName);
         rmsg.setField("message",    msg);
 
         if (rmsg.post())
             return;
 
         registerEndPoint(config.ircTag, apPoint);
         rmsg.post();
     }
 
     public void setOnline(boolean online)
     {
         String msg = null;
 
         if (!this.online && online)
             msg = ChatColor.GREEN + "Minecraft.net has come back online.";
         else if (this.online && !online)
             msg = ChatColor.RED   + "Minecraft.net has gone offline.";
 
         if (msg != null)
             broadcast("allowplayers.notify", msg);
 
         this.online = online;
     }
 
     public void setOnlineMode(boolean mode)
     {
         Object o;
         Class  c;
         Method m;
 
         /* Some hackery to with the CraftBukkit package naming */
 
         o = getServer();
         c = o.getClass();
 
         try {
             m = c.getDeclaredMethod("getServer");
             o = m.invoke(o);
 
             c = o.getClass();
             /* DedicatedServer -> MinecraftServer */
             c = c.getSuperclass();
 
             m = c.getDeclaredMethod("setOnlineMode", boolean.class);
             m.invoke(o, mode);
         } catch (Exception e) {
             e.printStackTrace();
             return;
         }
     }
 }
