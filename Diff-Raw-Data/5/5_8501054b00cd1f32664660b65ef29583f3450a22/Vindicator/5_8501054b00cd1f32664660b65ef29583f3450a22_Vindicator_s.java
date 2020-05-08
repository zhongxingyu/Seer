 /*
  * Copyright 2012-2013 James Geboski <jgeboski@gmail.com>
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
 
 package org.jgeboski.vindicator;
 
 import java.io.File;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 
 import com.ensifera.animosity.craftirc.CraftIRC;
 import com.ensifera.animosity.craftirc.EndPoint;
 import com.ensifera.animosity.craftirc.RelayedMessage;
 
 import org.jgeboski.vindicator.command.*;
 import org.jgeboski.vindicator.exception.APIException;
 import org.jgeboski.vindicator.util.Log;
 import org.jgeboski.vindicator.util.Message;
 import org.jgeboski.vindicator.util.Utils;
 
 public class Vindicator extends JavaPlugin
 {
     public static final String pluginName = "Vindicator";
 
     public Configuration config;
     public VindicatorAPI api;
 
     private EventListener events;
 
     public CraftIRC craftirc;
     public VPoint   vPoint;
 
     public void onLoad()
     {
         config   = new Configuration(new File(getDataFolder(), "config.yml"));
         api      = null;
         events   = new EventListener(this);
 
         craftirc = null;
         vPoint   = null;
     }
 
     public void onEnable()
     {
         PluginManager pm;
         Plugin p;
 
         config.load();
 
         try {
             api = new VindicatorAPI(this);
         } catch (APIException e) {
             setEnabled(false);
             return;
         }
 
         if (config.ircEnabled) {
             pm = getServer().getPluginManager();
             p  = pm.getPlugin("CraftIRC");
 
             if ((p != null) && p.isEnabled()) {
                 craftirc = (CraftIRC) p;
                 vPoint   = new VPoint();
             }
 
             if (!registerEndPoint(config.ircTag, vPoint))
                 config.ircEnabled = false;
         }
 
         events.register();
 
         getCommand("ban").setExecutor(new CBan(this));
         getCommand("kick").setExecutor(new CKick(this));
         getCommand("lookup").setExecutor(new CLookup(this));
         getCommand("mute").setExecutor(new CMute(this));
         getCommand("noteadd").setExecutor(new CNoteAdd(this));
         getCommand("noterem").setExecutor(new CNoteRem(this));
         getCommand("unban").setExecutor(new CUnban(this));
         getCommand("unmute").setExecutor(new CUnmute(this));
         getCommand("vindicator").setExecutor(new CVindicator(this));
     }
 
     public void onDisable()
     {
         if (config.ircEnabled)
             craftirc.unregisterEndPoint(config.ircTag);
 
        api.close();
     }
 
     public void reload()
     {
         if (config.ircEnabled)
             craftirc.unregisterEndPoint(config.ircTag);
 
         api.close();
         config.load();
 
         try {
             api = new VindicatorAPI(this);
         } catch (APIException e) {
             setEnabled(false);
             return;
         }
 
         if (config.ircEnabled && !registerEndPoint(config.ircTag, vPoint))
             config.ircEnabled = false;
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
 
     public void broadcast(String perm, String format, Object ... args)
     {
         String msg;
 
         msg = String.format(format, args);
         Utils.broadcast(perm, msg);
 
         if (!config.ircEnabled)
             return;
 
         RelayedMessage rmsg;
 
         /* This typecasting is needed to prevent a ClassNotFoundException
          * from being thrown over com.ensifera.animosity.craftirc.EndPoint.
          */
         rmsg = craftirc.newMsg((EndPoint) ((Object) vPoint), null, "chat");
 
         if (!config.ircColored)
             msg = ChatColor.stripColor(msg);
 
         rmsg.setField("realSender", pluginName);
         rmsg.setField("sender",     pluginName);
         rmsg.setField("message",    msg);
 
         if (rmsg.post())
             return;
 
         registerEndPoint(config.ircTag, vPoint);
         rmsg.post();
     }
 }
