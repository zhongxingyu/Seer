 /**
  * 
  * Copyright 2011 Greatman (https://github.com/greatman)
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  * 
  */
 
 package me.greatman.plugins.inn;
 
 import com.nijikokun.bukkit.Permissions.Permissions;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 /**
  * @description Handles all plugin permissions
  * @author Tagette
  */
 public class IPermissions {
 
     private enum PermissionHandler {
 
         PERMISSIONS, BUKKIT, NONE
     }
     private static PermissionHandler handler;
     public static Plugin PermissionPlugin;
     private static Inn plugin;
 
     public static void initialize(Inn instance) {
         IPermissions.plugin = instance;
         Plugin Permissions = plugin.getServer().getPluginManager().getPlugin("Permissions");
         handler = PermissionHandler.NONE;
 
         if (Permissions != null) {
             PermissionPlugin = Permissions;
             handler = PermissionHandler.PERMISSIONS;
             String version = PermissionPlugin.getDescription().getVersion();
                 ILogger.info("Permissions version " + version + " loaded.");
         } else{
        	ILogger.info("No permissions system found!");
         }
     }
 
     public static void onEnable(Plugin plugin) {
         if (PermissionPlugin == null) {
             String pluginName = plugin.getDescription().getName();
             handler = PermissionHandler.NONE;
 
             if (pluginName.equals("Permissions")) {
                 PermissionPlugin = plugin;
                 handler = PermissionHandler.PERMISSIONS;
                 String version = plugin.getDescription().getVersion();
                     ILogger.info("Permissions version " + version + " loaded.");
             } else{
             	ILogger.info("Bukkit permission system loaded.");
             	handler = PermissionHandler.BUKKIT;
             }
         }
     }
 
     public static boolean permission(Player player, String permission, boolean defaultPerm) {
         switch (handler) {
             case PERMISSIONS:
                 return ((Permissions) PermissionPlugin).getHandler().has(player, permission);
             case BUKKIT:
             	return player.hasPermission(permission);
             case NONE:
                 return defaultPerm;
             default:
                 return defaultPerm;
         }
     }
 
     public static boolean isAdmin(Player player) {
         return permission(player, "inn.admin", player.isOp());
     }
 }
