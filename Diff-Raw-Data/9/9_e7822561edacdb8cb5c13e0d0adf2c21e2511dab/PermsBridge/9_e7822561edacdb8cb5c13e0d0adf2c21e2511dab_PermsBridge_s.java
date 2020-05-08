 package com.titankingdoms.dev.TitanIRC;
 
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
     public PermsBridge(TitanIRC instance)
     {
         this.instance = instance;
         this.instance = instance;
     }
     public static String PermissionsPluginName = null;
     public static Plugin PermissionsPlugin = null;
 
     @EventHandler
     public void onPluginEnable(PluginEnableEvent e)
     {
         if(PermissionsPluginName == null || PermissionsPluginName == "Vault")
         {
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
             PermissionsPlugin = e.getPlugin();
             instance.debug("Hooked in to " + PermissionsPluginName);
         }
     }
 
     public String getPrefix(Player player)
     {
        String prefix = null;
         if(PermissionsPluginName.equals("GroupManager"))
             prefix = ((org.anjocaido.groupmanager.GroupManager)PermissionsPlugin).getWorldsHolder().getWorldData(player.getWorld().getName()).getUser(player.getName()).getGroup().getVariables().getVarString("prefix");
         else if(PermissionsPluginName.equals("bPermissions"))
             prefix = de.bananaco.bpermissions.api.WorldManager.getInstance().getWorld(player.getWorld().getName()).getGroup(player.getName()).getMeta().get("prefix");
         return Format.colour(prefix);
     }
 
     public String getSuffix(Player player)
     {
        String suffix = null;
         if(PermissionsPluginName.equals("GroupManager"))
             suffix = ((org.anjocaido.groupmanager.GroupManager)PermissionsPlugin).getWorldsHolder().getWorldData(player.getWorld().getName()).getUser(player.getName()).getGroup().getVariables().getVarString("suffix");
         else if(PermissionsPluginName.equals("bPermissions"))
             suffix = de.bananaco.bpermissions.api.WorldManager.getInstance().getWorld(player.getWorld().getName()).getGroup(player.getName()).getMeta().get("sufffix");
         return Format.colour(suffix);
     }
 }
