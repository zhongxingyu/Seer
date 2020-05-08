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
 package com.andune.minecraft.commonlib.server.bukkit;
 
 import com.andune.minecraft.commonlib.i18n.Colors;
 import com.andune.minecraft.commonlib.server.api.CommandSender;
 import com.andune.minecraft.commonlib.server.api.Location;
 import com.andune.minecraft.commonlib.server.api.PermissionSystem;
 import com.andune.minecraft.commonlib.server.api.Player;
 import com.andune.minecraft.commonlib.server.api.World;
 
 /**
  * Bukkit implementation of Player API.
  * 
  * @author andune
  *
  */
 public class BukkitPlayer extends BukkitCommandSender implements CommandSender, Player {
     protected org.bukkit.entity.Player bukkitPlayer;
     protected PermissionSystem perm;
     protected Colors colors;
     
     /** Protected constructor, should only be invoked from BukkitFactory.
      * 
      */
     protected BukkitPlayer(PermissionSystem perm, org.bukkit.entity.Player bukkitPlayer, Colors colors) {
         super(bukkitPlayer, colors);
         this.perm = perm;
         this.bukkitPlayer = bukkitPlayer;
     }
 
     /**
      *  Return the Bukkit Player object represented by this object.
      *  
      * @return
      */
     public org.bukkit.entity.Player getBukkitPlayer() {
         return bukkitPlayer;
     }
 
     @Override
     public boolean isNewPlayer() {
         return !bukkitPlayer.hasPlayedBefore();
     }
 
     @Override
     public String getName() {
         return bukkitPlayer.getName();
     }
 
     @Override
     public java.util.UUID getUUID() { return bukkitPlayer.getUniqueId(); }
 
     @Override
     public Location getLocation() {
         return new BukkitLocation(bukkitPlayer.getLocation());
     }
 
     @Override
     public boolean hasPermission(String permission) {
         return perm.has(this, permission);
     }
 
     @Override
     public Location getBedSpawnLocation() {
        return new BukkitLocation(bukkitPlayer.getBedSpawnLocation());
     }
 
     @Override
     public void sendMessage(String message) {
         bukkitPlayer.sendMessage(message);
     }
 
     @Override
     public void sendMessage(String[] messages) {
         bukkitPlayer.sendMessage(messages);
     }
 
     @Override
     public World getWorld() {
         return new BukkitWorld(bukkitPlayer.getWorld());
     }
 
     @Override
     public void setBedSpawnLocation(Location location) {
         // if BukkitPlayer is in use, it's because we're running on a Bukkit Server so
         // we can safely assume the incoming object is a BukkitLocation
         bukkitPlayer.setBedSpawnLocation( ((BukkitLocation) location).getBukkitLocation() );
     }
 
     @Override
     public void teleport(Location location) {
         // if BukkitPlayer is in use, it's because we're running on a Bukkit Server so
         // we can safely assume the incoming object is a BukkitLocation
         bukkitPlayer.teleport( ((BukkitLocation) location).getBukkitLocation() );
     }
     
     public boolean equals(Object o) {
         if( o == null )
             return false;
         if( !(o instanceof Player) )
             return false;
         String name = ((Player) o).getName();
         return getName().equals(name);
     }
 
     @Override
     public boolean isSneaking() {
         return bukkitPlayer.isSneaking();
     }
 
     @Override
     public boolean isOnline() {
         return bukkitPlayer.isOnline();
     }
     
     @Override
     public boolean hasPlayedBefore() {
         return bukkitPlayer.hasPlayedBefore();
     }
 
     @Override
     public long getLastPlayed() {
         return bukkitPlayer.getLastPlayed();
     }
 
     @Override
     public String toString() {
         return "{BukkitPlayer:"+getName()+"}";
     }
 }
