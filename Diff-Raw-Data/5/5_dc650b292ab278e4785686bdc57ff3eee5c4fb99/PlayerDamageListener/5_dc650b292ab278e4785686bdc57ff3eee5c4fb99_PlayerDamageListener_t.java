 /*
  * Copyright (C) 2012 BangL <henno.rickowski@googlemail.com>
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
 package de.bangl.wgpdf.listener;
 
 import de.bangl.wgpdf.Utils;
 import de.bangl.wgpdf.WGPlayerDamageFlagsPlugin;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 
 /**
  *
  * @author BangL <henno.rickowski@googlemail.com>
  */
 public class PlayerDamageListener implements Listener {
     WGPlayerDamageFlagsPlugin plugin;
 
     public PlayerDamageListener(WGPlayerDamageFlagsPlugin plugin) {
         this.plugin = plugin;
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     @EventHandler
     public void onPlayerDamage(EntityDamageEvent event) {
         // Only handle if player and dmg cause is not null.
        if(event.getEntity() != null
                && event.getCause() != null
                && event.getEntity() instanceof Player) {
             // Cancel if dmg cause is denied here.
             if (!Utils.dmgAllowedAtLocation(plugin.getWGP(), event.getCause(), event.getEntity().getLocation())) {
                 event.setCancelled(true);
             }
         }
     }
 }
