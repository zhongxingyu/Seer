 package com.titankingdoms.dev.TitanIRC;
 
 import net.milkbowl.vault.chat.Chat;
 import net.milkbowl.vault.permission.Permission;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.plugin.Plugin;
 
 /**
  * Copyright (C) 2012 Chris Ward
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
 public class PermsBridge implements Listener {
     private TitanIRC instance;
 
     /**
      * Initializes the PermsBridge. If you are using this from the API, use IRCApi.getPermsBridge();
      * @param instance Current TitanIRC instance
      */
     public PermsBridge(TitanIRC instance)
     {
         this.instance = instance;
     }
 
     /**
      * The name of the active permissions plugin
      */
     public static String PermissionsPluginName = null;
 
 
     /**
      * Load permissions plugins
      * @param e
      */
     @EventHandler
     public void onPluginEnable(PluginEnableEvent e)
     {
         if(PermissionsPluginName == null || PermissionsPluginName == "Vault")
         {
             if(e.getPlugin().getName().equalsIgnoreCase("DeathNotifier"))
             {
                 TitanIRC.useDeathNotifier = true;
                 instance.getServer().getPluginManager().registerEvents(new DNFListener(), instance);
             }
 
             if(e.getPlugin().getName().equalsIgnoreCase("bPermissions") && e.getPlugin() instanceof de.bananaco.bpermissions.imp.Permissions)
             {
                 PermissionsPluginName = "bPermissions";
             }
             else if(e.getPlugin().getName().equalsIgnoreCase("GroupManager") && e.getPlugin() instanceof org.anjocaido.groupmanager.GroupManager)
             {
                 PermissionsPluginName = "GroupManager";
             }
             else if(e.getPlugin().getName().equalsIgnoreCase("PermissionsBukkit") && e.getPlugin() instanceof com.platymuus.bukkit.permissions.PermissionsPlugin)
             {
                 PermissionsPluginName = "PermissionsBukkit";
             }
             else if(e.getPlugin().getName().equalsIgnoreCase("PermissionsEx") && e.getPlugin() instanceof ru.tehkode.permissions.bukkit.PermissionsEx)
             {
                 PermissionsPluginName = "PermissionsEx";
             }
             else if(e.getPlugin().getName().equalsIgnoreCase("zPermissions") && e.getPlugin() instanceof org.tyrannyofheaven.bukkit.zPermissions.ZPermissionsPlugin)
             {
                 PermissionsPluginName = "zPermissions";
             }
             else if(e.getPlugin().getName().equalsIgnoreCase("Vault") && e.getPlugin() instanceof net.milkbowl.vault.Vault && PermissionsPluginName == null)
             {
                 PermissionsPluginName = "Vault";
             }
             else
             {
                 instance.debug("The plugin " + e.getPlugin().getName() + " doesn't seem to be a perms plugin :(");
                 return;
             }
             instance.debug("Hooked in to " + PermissionsPluginName);
         }
     }
 
     /**
      * Get the prefix of a player.
      * It is recommended that you pass it through the colour format before using it.
      * @param player The *real name* of the player
      * @return The prefix of the player.
      */
     public String getPrefix(Player player)
     {
         String prefix = "";
         if(PermissionsPluginName == null)
             return "";
         if(PermissionsPluginName.equals("GroupManager"))
             prefix = ((org.anjocaido.groupmanager.GroupManager)instance.getServer().getPluginManager().getPlugin("GroupManager")).getWorldsHolder().getWorldData(player.getWorld().getName()).getUser(player.getName()).getGroup().getVariables().getVarString("prefix");
         else if(PermissionsPluginName.equals("bPermissions"))
            prefix = de.bananaco.bpermissions.api.WorldManager.getInstance().getWorld(player.getWorld().getName()).getGroup(player.getName()).getMeta().get("prefix");
         else if(PermissionsPluginName.equals("Vault"))
             prefix = instance.getServer().getServicesManager().getRegistration(Chat.class).getProvider().getPlayerPrefix(player);
         return Format.colour(prefix);
     }
 
     /**
      * Get the suffix of a player
      * @param player The *real name* of the player
      * @return The suffix of the player
      */
     public String getSuffix(Player player)
     {
         String suffix = "";
         if(PermissionsPluginName == null)
             return "";
         if(PermissionsPluginName.equals("GroupManager"))
             suffix = ((org.anjocaido.groupmanager.GroupManager)instance.getServer().getPluginManager().getPlugin("GroupManager")).getWorldsHolder().getWorldData(player.getWorld().getName()).getUser(player.getName()).getGroup().getVariables().getVarString("suffix");
         else if(PermissionsPluginName.equals("bPermissions"))
            suffix = de.bananaco.bpermissions.api.WorldManager.getInstance().getWorld(player.getWorld().getName()).getGroup(player.getName()).getMeta().get("sufffix");
         else if(PermissionsPluginName.equals("Vault"))
             suffix = instance.getServer().getServicesManager().getRegistration(Chat.class).getProvider().getPlayerSuffix(player);
         return Format.colour(suffix);
     }
 }
