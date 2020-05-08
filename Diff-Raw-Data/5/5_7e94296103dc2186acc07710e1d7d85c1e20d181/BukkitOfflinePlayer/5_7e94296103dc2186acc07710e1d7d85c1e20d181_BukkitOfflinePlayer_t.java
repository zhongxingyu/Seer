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
 
 import com.andune.minecraft.commonlib.server.api.OfflinePlayer;
 
 /**
  * @author andune
  *
  */
 public class BukkitOfflinePlayer implements OfflinePlayer {
     private final org.bukkit.OfflinePlayer bukkitOfflinePlayer;
     
     public BukkitOfflinePlayer(org.bukkit.OfflinePlayer bukkitOfflinePlayer) {
         this.bukkitOfflinePlayer = bukkitOfflinePlayer;
     }
 
     @Override
     public String getName() {
         return bukkitOfflinePlayer.getName();
     }

    @Override
    public java.util.UUID getUUID() { return bukkitOfflinePlayer.getUniqueId(); }

     @Override
     public boolean isOnline() {
         return bukkitOfflinePlayer.isOnline();
     }
     
     @Override
     public boolean hasPlayedBefore() {
         return bukkitOfflinePlayer.hasPlayedBefore();
     }
 
     @Override
     public long getLastPlayed() {
         return bukkitOfflinePlayer.getLastPlayed();
     }
 }
