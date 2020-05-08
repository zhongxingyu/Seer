 /*
  * Copyright (C) 2012 mewin <mewin001@hotmail.de>
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
 package com.mewin.WGRegionEffects;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 /**
  *
  * @author mewin <mewin001@hotmail.de>
  */
 public class WGRegionEffectsListener implements Listener {
 
     private WorldGuardPlugin wgPlugin;
     private WGRegionEffectsPlugin plugin;
     
     WGRegionEffectsListener(WorldGuardPlugin wgPlugin, WGRegionEffectsPlugin plugin)
     {
         this.wgPlugin = wgPlugin;
         this.plugin = plugin;
     }
     
     @EventHandler
     public void onPlayerMove(PlayerMoveEvent e)
     {
         updatePlayerEffects(e.getPlayer());
     }
     
     @EventHandler
     public void onPlayerRespawn(PlayerRespawnEvent e)
     {
         updatePlayerEffects(e.getPlayer());
     }
     
     @EventHandler
     public synchronized void onPlayerDeath(PlayerDeathEvent e)
     {
         WGRegionEffectsPlugin.playerEffects.remove(e.getEntity());
     }
     
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent e)
     {
         updatePlayerEffects(e.getPlayer());
     }
     
     
     private synchronized void updatePlayerEffects(Player p)
     {
         WGRegionEffectsPlugin.playerEffects.put(p, Util.getEffectsForLocation(wgPlugin, p.getLocation())); 
     }
 }
