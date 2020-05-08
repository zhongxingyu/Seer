 /*******************************************************************************
  * Copyright (c) 2011 James Richardson.
  * 
  * Message.java is part of TimedMessages.
  * 
  * TimedMessages is free software: you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * TimedMessages is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * TimedMessages. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 
 package name.richardson.james.bukkit.timedmessages;
 
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import name.richardson.james.bukkit.utilities.formatters.ColourFormatter;
 import name.richardson.james.bukkit.utilities.permissions.PermissionManager;
 
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 
 import com.sk89q.worldguard.protection.managers.RegionManager;
 
 public abstract class Message implements Runnable {
   
   protected final List<String> messages;
 
   private final Long ticks;
   private final String permission;
   private final Server server;
 
   private final PermissionManager permissionManager;
 
   private final List<String> worlds = new LinkedList<String>();
   private final Set<String> regions = new HashSet<String>();
 
   private final TimedMessages plugin;
 
   public Message(final TimedMessages plugin, final Server server, final Long milliseconds, final List<String> messages, final String permission, final List<String> worlds, List<String> regions) {
     final long seconds = milliseconds / 1000;
     this.ticks = seconds * 20;
     this.messages = messages;
     this.permission = permission;
     this.permissionManager = plugin.getPermissionManager();
     this.server = server;
     this.worlds.addAll(worlds);
     this.regions.addAll(regions);
     this.plugin = plugin;
     this.plugin.getCustomLogger().debug(this, String.format("Creating %s which broadcasts every %s seconds", this.getClass().getSimpleName(), seconds));
   }
 
   public List<String> getMessages() {
     return this.messages;
   }
 
   public String getPermission() {
     return this.permission;
   }
 
   public Long getTicks() {
     return this.ticks;
   }
 
   public void run() {
    this.plugin.getCustomLogger().debug(this, String.format("Running %s.", this.getClass().getSimpleName()));
     String message = this.getNextMessage();
     message = ColourFormatter.replace("&", message);
     final String[] parts = message.split("/n");
     final List<Player> players = new LinkedList<Player>();
  
     for (final Player player : this.server.getOnlinePlayers()) {
       // ignore the player if they are not in the world required
      if (!worlds.isEmpty() && !worlds.contains(player.getWorld().getName())) continue;    
       // if the player is not in the correct region ignore them
       if (!this.isPlayerInRegion(player)) continue;
       // ignore the player if they do not have the correct permission
       if (this.permission != null && !this.permissionManager.hasPlayerPermission(player, this.permission)) continue;
       players.add(player);
     }
   
     if (players.isEmpty()) return;
   
     this.plugin.getCustomLogger().debug(this, String.format("Sending message to following players: %s", players.toString()));
     for (final String part : parts) {
       for (final Player player : players) {
         player.sendMessage(part);
       }
     }
   }
   
   public boolean isPlayerInRegion(Player player) {
     if (this.worlds.isEmpty()) return true;
     if (this.plugin.getGlobalRegionManager() == null) return true;
     if (this.regions.isEmpty()) return true;
     for (String worldName : this.worlds) {
       if (!player.getWorld().getName().equals(worldName)) continue;
       RegionManager manager = this.plugin.getRegionManager(worldName);
       final int x = (int) player.getLocation().getX();
       final int y = (int) player.getLocation().getY();
       final int z = (int) player.getLocation().getZ();
       for (String regionName : this.regions) {
         if (manager.getRegion(regionName).contains(x, y, z)) return true;
       }
     }
     return false;
   }
 
   protected abstract String getNextMessage();
 
 }
