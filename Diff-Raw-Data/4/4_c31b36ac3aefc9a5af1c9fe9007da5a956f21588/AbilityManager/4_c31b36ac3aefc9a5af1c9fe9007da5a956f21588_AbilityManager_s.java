 /**
  * This file, AbilityManager.java, is part of MineQuest:
  * A full featured and customizable quest/mission system.
  * Copyright (C) 2012 The MineQuest Team
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
  **/
 package com.theminequest.MQCoreRPG.API.Abilities;
 
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Entity;
 import org.bukkit.event.Event;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerEggThrowEvent;
 import org.bukkit.event.player.PlayerEvent;
 import org.bukkit.event.player.PlayerFishEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.theminequest.MQCoreRPG.BukkitEvents.AbilityRefreshedEvent;
 import com.theminequest.MineQuest.MineQuest;
 
 public class AbilityManager implements Listener {
 	
 	private HashMap<String,Ability> abilities;
 	
 	public AbilityManager(){
 		MineQuest.log("[Ability] Starting Manager...");
 		abilities = new HashMap<String,Ability>();
 	}
 	
 	public void registerAbility(Ability a){
 		MineQuest.log("[Ability] Registered " + a.getName());
 		abilities.put(a.getName(), a);
 	}
 
 	@EventHandler
 	public void abilityRefreshed(AbilityRefreshedEvent e){
 		e.getPlayer().sendMessage(
 				ChatColor.GRAY+"Ability " + e.getAbility().getName() + " recharged!");
 	}
 	
 	@EventHandler
 	public void onPlayerEggThrowEvent(PlayerEggThrowEvent e){
 		onEvent(e);
 	}
 	
 	@EventHandler
 	public void onPlayerFishEvent(PlayerFishEvent e){
 		onEvent(e);
 	}
 	
 	@EventHandler
 	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent e){
 		onEvent(e);
 	}
 	
 	@EventHandler
 	public void onPlayerInteractEvent(PlayerInteractEvent e){
 		onEvent(e);
 	}
 	
 	private void onEvent(PlayerEvent e){
 		for (Ability a : abilities.values()){
 			if (a.onEventCaught(e))
 				return;
 		}
 	}
 }
