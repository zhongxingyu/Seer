 /*
  * Bukkit plugin: CustomStaffList
  * Copyright (C) 2013 Stealth2800 <stealth2800@stealthyone.com>
  * Website: <http://stealthyone.com/bukkit>
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
 package com.stealthyone.mcb.customuserlist.backend.userlists;
 
 import com.stealthyone.mcb.customuserlist.CustomUserList;
 import com.stealthyone.mcb.customuserlist.CustomUserList.Log;
 import com.stealthyone.mcb.customuserlist.permissions.PermissionNode;
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.kitteh.vanish.VanishPlugin;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 public class UserList {
 
     private ConfigurationSection config;
 
     private UserListFormat format;
     private List<ListGroup> groups = new ArrayList<ListGroup>();
 
     public UserList(ConfigurationSection config) {
         this.config = config;
         format = new UserListFormat(config);
         Log.info("Loaded " + reloadGroups() + " groups for UserList type: " + config.getName());
     }
 
     public String getName() {
         return config.getName();
     }
 
     public int reloadGroups() {
         groups.clear();
         ConfigurationSection groupSection = config.getConfigurationSection("Groups");
         for (String groupName : groupSection.getKeys(false)) {
             ListGroup group = new ListGroup(groupSection.getConfigurationSection(groupName));
             groups.add(group);
         }
         return groups.size();
     }
 
     public List<String> getAliases() {
         Object aliases = config.get("aliases");
         if (aliases instanceof String) {
             return Arrays.asList((String) aliases);
         } else if (aliases instanceof List) {
             return (List<String>) aliases;
         } else {
             return null;
         }
     }
 
     public String getPermission() {
         return config.getString("permission");
     }
 
     public boolean checkVanish(Player player) {
         if (!CustomUserList.getInstance().hookedWithVanish()) {
             return false;
         } else if (!format.hideVanished()) {
             return false;
         } else {
             return ((VanishPlugin) Bukkit.getPluginManager().getPlugin("VanishNoPacket")).getManager().isVanished(player);
         }
     }
 
     public List<String> constructList() {
         List<String> returnList = new ArrayList<>();
         Player[] onlinePlayers = Bukkit.getOnlinePlayers();
         boolean useDisplayNames = format.useDisplayNames();
         List<String> addedNames = new ArrayList<>();
 
         /* Add header */
         String header = format.getHeader();
         if (header.length() > 0) returnList.add(header);
 
         /* Add name */
         returnList.add(format.getName());
 
         /* Construct groups */
         for (ListGroup group : groups) {
             StringBuilder groupLine = new StringBuilder();
             groupLine.append(format.getGroupNameFormat().replace("{GROUPNAME}", group.getColor() + group.getDisplayName()));
 
             /* Check players */
             StringBuilder playerList = new StringBuilder();
             String permission = group.getPermission();
 
             List<String> persistentPlayers = group.getPlayers();
             if (persistentPlayers == null) {
                 for (Player player : onlinePlayers) {
                     if (PermissionNode.checkCustomPermission(permission, player, group.ignoreOp())) {
                         if (checkVanish(player)) continue;
                         String playerName = useDisplayNames ? player.getDisplayName() : player.getName();
                        if (format.limitPlayersToOneGroup() && addedNames.contains(player.getName().toLowerCase())) continue;
                        addedNames.add(player.getName().toLowerCase());
                         if (playerList.length() > 0) playerList.append(", ");
                         playerList.append(group.getNameColor()).append(playerName);
                     }
                 }
             } else {
                 for (String name : persistentPlayers) {
                     boolean playerOnline = Bukkit.getOfflinePlayer(name).isOnline();
                    if (playerOnline && checkVanish(Bukkit.getPlayerExact(name))) playerOnline = false;
                     if (playerList.length() > 0) playerList.append(", ");
                     playerList.append(playerOnline ? group.getOnlineColor() : group.getColor()).append(name);
                    addedNames.add(name.toLowerCase());
                 }
             }
 
             groupLine.append(playerList.length() == 0 ? format.getNoneOnlineMessage() : playerList.toString());
             returnList.add(groupLine.toString());
         }
 
         /* Add footer */
         String footer = format.getFooter();
         if (footer.length() > 0) returnList.add(footer);
 
         return returnList;
     }
 
 }
