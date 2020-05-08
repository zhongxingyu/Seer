 /**
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Copyright (c) 2013 Andune (andune.alleria@gmail.com)
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer
  * in the documentation and/or other materials provided with the
  * distribution.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  */
 /**
  * 
  */
 package com.andune.minecraft.commonlib.server.bukkit.events;
 
 import javax.inject.Inject;
 
 import org.bukkit.plugin.Plugin;
 
 import com.andune.minecraft.commonlib.Logger;
 import com.andune.minecraft.commonlib.LoggerFactory;
 import com.andune.minecraft.commonlib.server.api.Location;
 import com.andune.minecraft.commonlib.server.api.Server;
 import com.andune.minecraft.commonlib.server.bukkit.BukkitFactory;
import static com.google.common.base.Preconditions.checkNotNull;
 
 
 /** Bukkit implementation of PlayerJoinEvent API.
  * 
  * @author andune
  *
  */
 public class PlayerJoinEvent extends PlayerEvent
 implements com.andune.minecraft.commonlib.server.api.events.PlayerJoinEvent
 {
     private final Logger log = LoggerFactory.getLogger(PlayerJoinEvent.class);
     private Plugin plugin;
     private Server server;
     @SuppressWarnings("unused")
     private org.bukkit.event.player.PlayerJoinEvent event;
 
     public PlayerJoinEvent(org.bukkit.event.player.PlayerJoinEvent event, BukkitFactory bukkitFactory) {
         super(event, bukkitFactory);
         this.event = event;
     }
     
     @Inject
     public void setPlugin(Plugin plugin) {
         this.plugin = plugin;
     }
 
     @Inject
     public void setServer(Server server) {
         this.server = server;
     }
 
     /**
      * This method doesn't exist on the actual bukkit event, so to simulate it we
      * setup a delayed teleport. This is not an ideal implementation since multiple
      * calls to setJoinLocation() will result in multiple delayed teleports. But
      * it works since HSP only invokes it once.
      * 
      * At some point hopefully Bukkit will implement a similar method and then
      * this method will just call the backing event.
      */
     @Override
     public void setJoinLocation(Location joinLocation) {
        checkNotNull(joinLocation);
        checkNotNull(server);

         /*
          * Bukkit does not support this event and teleporting the player directly
          * during a join event can crash the server. So we have to setup a delayed
          * event and teleport them after a small delay.
          */
        server.delayedTeleport(getPlayer(), joinLocation);
         
         // verify they ended up where we sent them by checking 5 tics later
         final Location hspLocation = joinLocation;
         plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
             public void run() {
                 Location currentLocation = getPlayer().getLocation();
                 
                 // do manual world/x/y/z check instead of .equals() so that we avoid comparing
                 // pitch/yaw and also so we round to integer blocks instead of exact double loc
                 if( currentLocation.getWorld() != hspLocation.getWorld()
                         || currentLocation.getBlockX() != hspLocation.getBlockX()
                         || currentLocation.getBlockY() != hspLocation.getBlockY()
                         || currentLocation.getBlockZ() != hspLocation.getBlockZ() ) {
                     log.info("onJoin: final player location is different than where HSP sent player, another plugin has changed the location."
                             +" Player {}, HSP location {}"
                             +", final player location {}",
                             getPlayer().getName(), hspLocation.shortLocationString(),
                             currentLocation.shortLocationString());
                 }
             }
         }, 5); 
     }
 }
