 /**
  * This file, PlayerManager.java, is part of MineQuest:
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
 package com.theminequest.MQCoreRPG.Player;
 
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByBlockEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerExpChangeEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 import com.theminequest.MQCoreRPG.MQCoreRPG;
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.BukkitEvents.QuestAvailableEvent;
 import com.theminequest.MineQuest.BukkitEvents.QuestCompleteEvent;
 import com.theminequest.MineQuest.BukkitEvents.GroupInviteEvent;
 
 public class PlayerManager implements Listener {
 
 	private Map<Player,PlayerDetails> players;
 	private volatile boolean shutdown;
 	private volatile boolean chill;
 
 	public PlayerManager(){
 		MineQuest.log("[Player] Starting Manager...");
 		players = Collections.synchronizedMap(new LinkedHashMap<Player,PlayerDetails>());
 		shutdown = false;
 		chill = false;
 
 		Bukkit.getScheduler().scheduleSyncRepeatingTask(MineQuest.activePlugin, new Runnable(){
 
 			@Override
 			public void run() {
 				saveAll();
 				MineQuest.log("[Player] Routine Record Save Finished.");
 			}
 
 		}, 1200, 18000);
 
 		Runnable r = new Runnable(){
 
 			@Override
 			public void run() {
 				Random r = new Random();
 				while (!shutdown){
 					if (!chill){
 						chill = true;
 						for (PlayerDetails d : players.values()){
 							d.modifyManaBy(1);
 						}
 						chill = false;
 					}
 					try {
 						Thread.sleep(r.nextInt(10000)+5000);
 					} catch (InterruptedException e) {
 						throw new RuntimeException(e);
 					}
 				}
 			}
 
 		};
 
 		Thread t = new Thread(r);
 		t.setDaemon(true);
 		t.setName("MineQuest-PlayerMana");
 		t.start();
 	}
 
 	public void shutdown(){
 		shutdown = true;
 	}
 
 	public void saveAll(){
 		for (PlayerDetails d : players.values()){
 			try {
 				PlayerSQL.updatePlayerObject(d.getPlayer().getName(), d);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void playerAcct(Player p){
 		if (!players.containsKey(p)){
 			PlayerDetails obj;
 			try {
 				obj = PlayerSQL.retrievePlayerObject(p.getName());
 				if (obj!=null){
 					players.put(p, obj);
 					return;
 				}
 			} catch (SQLException e1) {
 				e1.printStackTrace();
 			}
 			obj = new PlayerDetails(p);
 			try {
 				PlayerSQL.insertPlayerObject(p.getName(), obj);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			players.put(p, obj);
 		}
 	}
 
 	public PlayerDetails getPlayerDetails(Player p){
 		playerAcct(p);
 		return players.get(p);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerJoin(PlayerJoinEvent e){
 		MineQuest.log("[Player] Retrieving details for player " + e.getPlayer().getName());
 		playerAcct(e.getPlayer());
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onPlayerQuit(PlayerQuitEvent e){
 		MineQuest.log("[Player] Saving details for player " + e.getPlayer().getName());
 		try {
 			PlayerSQL.updatePlayerObject(e.getPlayer().getName(), getPlayerDetails(e.getPlayer()));
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onPlayerKick(PlayerKickEvent e){
 		MineQuest.log("[Player] Saving details for player " + e.getPlayer().getName());
 		try {
 			PlayerSQL.updatePlayerObject(e.getPlayer().getName(), getPlayerDetails(e.getPlayer()));
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onFoodLevelChangeEvent(FoodLevelChangeEvent e){
 		if (!(e.getEntity() instanceof Player))
 			return;
 		Player p = (Player) e.getEntity();
 		PlayerDetails d = getPlayerDetails(p);
 		e.setFoodLevel(d.getMinecraftMana(d.getMana()));
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onEntityRegainHealthEvent(EntityRegainHealthEvent e){
 		if (!(e.getEntity() instanceof Player))
 			return;
 		Player p = (Player) e.getEntity();
 		PlayerDetails d = getPlayerDetails(p);
 		int amount = e.getAmount();
 		long total = d.getHealth()+amount;
 		if (total>d.getMaxHealth())
 			total = d.getMaxHealth();
 		int minecrafthealth = d.getMinecraftHealth(total);
 		int minecraftcurrent = p.getHealth();
 		e.setAmount(minecrafthealth-minecraftcurrent);
 	}
 
 	// Damage START
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e){
 		if (!(e.getEntity() instanceof Player))
 			return;
 		Player p = (Player) e.getEntity();
 		PlayerDetails d = getPlayerDetails(p);
 		int amount = e.getDamage();
 		long total = d.getHealth()-amount;
 		if (total<0)
 			total = 0;
 		int minecrafthealth = d.getMinecraftHealth(total);
 		int minecraftcurrent = p.getHealth();
 		e.setDamage(minecraftcurrent-minecrafthealth);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onEntityDamageByBlockEvent(EntityDamageByBlockEvent e){
 		if (!(e.getEntity() instanceof Player))
 			return;
 		Player p = (Player) e.getEntity();
 		PlayerDetails d = getPlayerDetails(p);
 		int amount = e.getDamage();
 		long total = d.getHealth()-amount;
 		if (total<0)
 			total = 0;
 		int minecrafthealth = d.getMinecraftHealth(total);
 		int minecraftcurrent = p.getHealth();
 		e.setDamage(minecraftcurrent-minecrafthealth);
 	}
 
 	// Damage END
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerExpChangeEvent(PlayerExpChangeEvent e){
 		e.setAmount(0);
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerDeath(PlayerDeathEvent e){
 		e.setDroppedExp(0);
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerRespawn(PlayerRespawnEvent e){
		PlayerDetails p = getPlayerDetails(e.getPlayer());
		p.setHealth(p.getMaxHealth());
		p.updateMinecraftView();
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onEntityDeath(EntityDeathEvent e){
 		e.setDroppedExp(0);
 	}
 
 }
