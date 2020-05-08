 /*
  * Copyright (C) 2013 Dabo Ross <http://www.daboross.net/>
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
 package net.daboross.bukkitdev.displaynamemessages;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerBedLeaveEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitRunnable;
 
 /**
  *
  * @author daboross
  */
 public class DisplayNameMessagesPlugin extends JavaPlugin implements Listener {
 
     private final String MOLLY = ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + "Molly" + ChatColor.GRAY + "]" + ChatColor.WHITE;
 
     @Override
     public void onEnable() {
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvents(this, this);
     }
 
     @Override
     public void onDisable() {
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         sender.sendMessage("DisplayNameMessages doesn't know about the command /" + cmd.getName());
         return true;
     }
 
     @EventHandler
     public void onDeath(PlayerDeathEvent evt) {
         Player p = evt.getEntity();
         evt.setDeathMessage(ChatColor.GRAY + evt.getDeathMessage().replace(p.getName(), ChatColor.BLUE + p.getDisplayName() + ChatColor.GRAY));
     }
 
     @EventHandler
     public void onJoin(PlayerJoinEvent evt) {
         evt.setJoinMessage(null);
         final Player p = evt.getPlayer();
         new BukkitRunnable() {
             @Override
             public void run() {
                 if (p.isOnline()) {
                    Bukkit.broadcastMessage(MOLLY + " hello " + ChatColor.BLUE + p.getDisplayName());
                 }
             }
         }.runTaskLater(this, 2);
     }
 
     @EventHandler
     public void onQuit(PlayerQuitEvent evt) {
         Player p = evt.getPlayer();
        evt.setQuitMessage(MOLLY + " goodbye " + ChatColor.BLUE + p.getDisplayName());
     }
 }
