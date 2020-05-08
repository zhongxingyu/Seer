 /*
     MazeMania; a minecraft bukkit plugin for managing a maze as an arena.
     Copyright (C) 2012 Plugmania (Sorroko,korikisulda) and contributors.
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package info.plugmania.mazemania.listeners;
 
 
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import info.plugmania.mazemania.MazeMania;
 import info.plugmania.mazemania.Util;
 import info.plugmania.mazemania.helpers.PlayerStore;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.potion.PotionType;
 
 
 public class PlayerListener implements Listener {
 
 	MazeMania plugin;
 
 	public PlayerListener(MazeMania instance) {
 		plugin = instance;
 	}
 
 	@EventHandler
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
 		if (event.isCancelled()) return;
 		if (event.getMessage().startsWith("/maze")) return;
 		if (!plugin.arena.playing.contains(event.getPlayer())) return;
 		event.setCancelled(true);
 	}
 
 	@EventHandler
 	public void onEntityExplode(EntityExplodeEvent event) {
 		if (event.isCancelled()) return;
 		if (plugin.arena.isInArena(event.getEntity().getLocation()))
 			event.setCancelled(true);
 	}
 
 	@EventHandler
 	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
 		if (event.getDamager() instanceof Player) {
 			if (event.getEntity() instanceof Player) {
 				if (!plugin.mainConf.getBoolean("allowPvP", true))
 					event.setCancelled(true);
 			}
 		}
 
 		if (!(event.getEntity() instanceof Player)) { //is a mob
 			if (!plugin.arena.isInArena(event.getEntity().getLocation())) return; //return if not in arena
 
 			if (!event.getCause().equals(DamageCause.ENTITY_ATTACK)) //make sure the damage was 'natural'
 				event.setCancelled(true);
 		}
 
 		if(plugin.getConfig().getBoolean("noDeath")) { if(event.getEntity() instanceof Player) {
 			Player player = (Player) event.getEntity();
 			if(plugin.arena.playing.contains(player)){
 				
 				// CHECK IF DAMAGE KILLS PLAYER
 				if(plugin.getServer().getPlayer(player.getName()).getHealth()-event.getDamage()<=1) {
 
 					// CHECK FOR KEEP SPAWNING IN MAZE
 					if (plugin.getConfig().getBoolean("noDeath")) {
 						if (plugin.mainConf.getBoolean("randomSpawn", true)) {
 							player.teleport(plugin.arena.getRandomSpawn());
 						} else {
 							Location spawn = plugin.arena.getSpawn();
 							if (spawn == null) {
 								return;
 							}
 							player.teleport(spawn);
 						}
 					}
 					
 					String killer = "";
 					if(event.getCause().equals(DamageCause.ENTITY_ATTACK)) {
 						
 						if(event.getDamager() instanceof Player) {
 							Player damager = (Player) event.getDamager();
 							killer = damager.getDisplayName();
 						}
 						else {
							LivingEntity entity = (LivingEntity) event.getEntity();
 							killer = entity.getType().getName();
 						}
 					} else {
 						killer = event.getCause().name();
 					}
 					player.sendMessage(Util.formatMessage("If you want to leave the game type " + ChatColor.GOLD +"/maze leave"));
 					Util.broadcastInside(ChatColor.GOLD + "" + player.getName() + ChatColor.BLUE +  " was killed by " + ChatColor.GOLD + killer + ChatColor.BLUE + "!");
 					
 					// FIX FOR CLIENT NOT SHOWING ENTITIES
 					for (Player p:plugin.arena.playing) {
 					    if (p.canSee(player)) p.showPlayer(player);
 					}
 					
 					// CANCEL DAMAGE AKA DEATH
 					player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 0, 1));
 					player.setFireTicks(0);
 					player.setHealth(20);
 					event.setCancelled(true);
 				}
 			}
 		}}
 	}
 
 	@EventHandler
 	public void onPlayerDeath(PlayerDeathEvent event) {
 		Player player = event.getEntity();
 		if (!plugin.arena.playing.contains(player)) return;
 
 		Util.broadcastInside(ChatColor.GOLD + "" + player.getName() + ChatColor.BLUE +  " has died in the maze!");
 
 		plugin.arena.playing.remove(player);
 		
 		if (plugin.arena.playing.size() == 1){
 			Player winner = plugin.arena.playing.get(0);
 			Bukkit.broadcastMessage(Util.formatBroadcast(winner.getName() + " is the last man standing and won the maze!"));
 			plugin.mazeCommand.arenaCommand.leaveMatch(winner);
 			//WIN
 			plugin.arena.store.remove(winner);
 			player.sendMessage(Util.formatMessage("Thank you for playing MazeMania."));
 			plugin.arena.playing.clear();
 			plugin.arena.gameActive = false;
 
 			plugin.reward.rewardPlayer(winner);
 		} else if (plugin.arena.playing.isEmpty()) {
 			plugin.arena.gameActive = false;
 			Bukkit.broadcastMessage(Util.formatBroadcast("The MazeMania game was forfeited, all players left!"));
 		}
 	}
 
 	@EventHandler
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		Player player = event.getPlayer();
 		if (!plugin.arena.store.containsKey(player)) return;
 			
 		player.getInventory().clear();
 		player.setSneaking(false);
 	
 		Location back;
 		PlayerStore ps = plugin.arena.store.get(player);
 	
 		player.getInventory().setContents(ps.inv.getContents());
 		back = ps.previousLoc;
 		player.setGameMode(ps.gm);
 		player.setFoodLevel(ps.hunger);
 		player.setHealth(ps.health);
 		player.getInventory().setArmorContents(ps.armour);
 	
 		if (back == null) {
 			player.sendMessage(Util.formatMessage("Your previous location was not found."));
 			event.setRespawnLocation(player.getWorld().getSpawnLocation());
 		} else {
 			event.setRespawnLocation(back);
 		}
 	
 		plugin.arena.store.remove(player);
 	}
 
 	@EventHandler
 	public void onPlayerLeave(PlayerQuitEvent event) {
 		Player player = event.getPlayer();
 		if (plugin.arena.playing.contains(player)) {
 			plugin.mazeCommand.arenaCommand.leaveMatch(player);
 			Util.broadcastInside(ChatColor.GOLD + "" + player.getName() + ChatColor.BLUE + " has left the maze!");
 
 			plugin.arena.playing.remove(player);
 
 			if (plugin.arena.playing.isEmpty()) {
 				plugin.arena.gameActive = false;
 				Bukkit.broadcastMessage(Util.formatBroadcast("The MazeMania game was forfeited, all players left!"));
 			}
 		}
 		if (plugin.arena.waiting.contains(player)) {
 			plugin.arena.waiting.remove(player);
 		}
 		if (plugin.arena.waiting.isEmpty() && plugin.mazeCommand.arenaCommand.scheduleActive) {
 			Bukkit.getScheduler().cancelTask(plugin.mazeCommand.arenaCommand.scheduleId);
 			Bukkit.broadcastMessage(Util.formatBroadcast("MazeMania game cancelled, all waiting players left"));
 		}
 	}
 
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		Player player = event.getPlayer();
 		if (plugin.arena.store.containsKey(player)) {
 			player.getInventory().clear();
 
 			Location back = null;
 			if (plugin.arena.store.containsKey(player)) {
 				PlayerStore ps = plugin.arena.store.get(player);
 				player.getInventory().setContents(ps.inv.getContents());
 				back = ps.previousLoc;
 				player.setGameMode(ps.gm);
 				player.setFoodLevel(ps.hunger);
 				player.setHealth(ps.health);
 				player.getInventory().setArmorContents(ps.armour);
 			}
 
 			if (back == null) {
 				player.sendMessage(Util.formatMessage("Your previous location was not found."));
 			} else {
 				player.teleport(back);
 			}
 			plugin.arena.store.remove(player);
 		}
 		if (plugin.arena.isInArena(player.getLocation())) {
 			player.teleport(player.getWorld().getSpawnLocation());
 		}
 	}
 
 	@EventHandler
 	public void onChestInteract(PlayerInteractEvent event) {
 		if (event.isCancelled()) return;
 		Player player = event.getPlayer();
 		if (!plugin.arena.playing.contains(player)) return;
 
 		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
 			Block b = event.getClickedBlock();
 			if (b.getType().equals(Material.CHEST)) {
 				event.setCancelled(true);
 
 				PlayerStore ps = plugin.arena.store.get(player);
 				if (ps.chests.containsKey(b.getLocation())) {
 					player.openInventory(ps.chests.get(b.getLocation()));
 				} else {
 					Chest chest = (Chest) b.getState();
 					Inventory inv = Bukkit.createInventory(null, chest.getInventory().getSize());
 					inv.setContents(chest.getInventory().getContents());
 					ps.chests.put(b.getLocation(), inv);
 					player.openInventory(ps.chests.get(b.getLocation()));
 				}
 				ps.openChest=ps.chests.get(b.getLocation()).getContents();
 
 			}
 		}
 	}
 
 	@EventHandler
 	public void onItemDrop(PlayerDropItemEvent event) {
 		if (event.isCancelled()) return;
 		Player player = event.getPlayer();
 		if (plugin.arena.playing.contains(player))
 			event.setCancelled(true);
 	}
 
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		if (event.isCancelled()) return;
 		Player player = event.getPlayer();
 		if (!plugin.arena.playing.contains(player)) return;
 
 		if (event.getAction().equals(Action.RIGHT_CLICK_AIR)
 				|| event.getAction().equals(Action.LEFT_CLICK_AIR)) {
 			return;
 		}
 		Block block = event.getClickedBlock();
 
 		Material matBlock = Material.getMaterial(plugin.mainConf.getString("blockMaterial", "GOLD_BLOCK"));
 		if (matBlock == null) matBlock = Material.GOLD_BLOCK;
 
 		if (!block.getType().equals(matBlock)) return;
 
 		//WIN
 		Bukkit.broadcastMessage(Util.formatBroadcast(player.getName() + " has won the maze!"));
 		for (Player p : plugin.arena.playing) {
 			plugin.mazeCommand.arenaCommand.leaveMatch(p);
 			plugin.arena.store.remove(p);
 			p.sendMessage(Util.formatMessage("Thank you for playing MazeMania."));
 		}
 		plugin.arena.playing.clear();
 		plugin.arena.gameActive = false;
 		plugin.reward.rewardPlayer(player);
 		event.setCancelled(true);
 	}
 
 	@EventHandler
 	public void onPlayerMove(PlayerMoveEvent event) {
 		if (event.isCancelled()) return;
 
 		if (!plugin.mainConf.getString("mode", "collectItems").equalsIgnoreCase("collectItems")) return;
 
 		if (event.getFrom().getBlockX() != event.getTo().getBlockX()
 				|| event.getFrom().getBlockY() != event.getTo().getBlockY()
 				|| event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
 
 			if (!plugin.arena.playing.contains(event.getPlayer())) return;
 
 
 			if (event.getTo().getBlockX() == plugin.arena.getExit().getBlockX()
 					&& event.getTo().getBlockY() == plugin.arena.getExit().getBlockY()
 					&& event.getTo().getBlockZ() == plugin.arena.getExit().getBlockZ()) {
 				Player player = event.getPlayer();
 				Inventory inv = player.getInventory();
 
 				Material item = Material.getMaterial(plugin.mainConf.getString("itemToCollect", "GOLD_NUGGET"));
 				if (item == null) item = Material.GOLD_NUGGET;
 				int amount = plugin.mainConf.getInt("itemAmountToCollect", 10);
 				if (amount < 1) amount = 1;
 				if (inv.contains(item, amount)) {
 					//WIN
 					Bukkit.broadcastMessage(Util.formatBroadcast(player.getName() + " has won the maze!"));
 					for (Player p : plugin.arena.playing) {
 						plugin.mazeCommand.arenaCommand.leaveMatch(p);
 						plugin.arena.store.remove(p);
 						p.sendMessage(Util.formatMessage("Thank you for playing MazeMania."));
 					}
 					plugin.arena.playing.clear();
 					plugin.arena.gameActive = false;
 
 					plugin.reward.rewardPlayer(player);
 
 				} else {
 					player.sendMessage(Util.formatMessage("You found the exit but have not collected enough items!"));
 				}
 			}
 		}
 	}
 	@EventHandler
 	public void InventoryClose(InventoryCloseEvent event) {
 		if(!plugin.arena.playing.contains(event.getPlayer())) return;
 		if(!plugin.getConfig().getBoolean("notifyLoot",false)) return;
 		
 		PlayerStore ps=plugin.arena.store.get(event.getPlayer());
 		ItemStack[] before=ps.openChest;
 		ItemStack[] after=event.getInventory().getContents();
 		
 		String looted = plugin.util.createDifferenceString(plugin.util.compressInventory(before), plugin.util.compressInventory(after));
 		
 		if(looted.length()>=5) {
 			Util.broadcastInside(ChatColor.GOLD + event.getPlayer().getName() + ChatColor.BLUE + " found " + looted + "!");
 		}		
 	}
 	
 	String strCompress(HashMap<String,Integer> items){
 		String ret="";
 		for(Entry<String,Integer> itemst:items.entrySet()){
 			ret+=itemst.getKey() + " " +  itemst.getValue() + ";";
 		}
 		return ret;
 	}
 
 	@EventHandler
 	public void onTriggers(PlayerMoveEvent event) {
 		if (event.getFrom().getBlockX() != event.getTo().getBlockX()
 				|| event.getFrom().getBlockY() != event.getTo().getBlockY()
 				|| event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
 
 			if (!plugin.arena.playing.contains(event.getPlayer())) return;
 
 			plugin.triggers.handle(event.getTo().getBlock().getLocation(), event.getPlayer());
 			Block b= event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN);
 			if(plugin.TriggerManager.isTrigger(b.getType()))
 				plugin.TriggerManager.applyTrigger(b.getType(),plugin,event.getPlayer());
 		}
 	}
 
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event) {
 		if (event.isCancelled()) return;
 		if (!plugin.arena.playing.contains(event.getPlayer())) return;
 		if (!plugin.arena.isInArena(event.getBlock().getLocation())) return;
 		event.setCancelled(true);
 	}
 
 	@EventHandler
 	public void onPlaceBreak(BlockPlaceEvent event) {
 		if (event.isCancelled()) return;
 		if (!plugin.arena.playing.contains(event.getPlayer())) return;
 		if (!plugin.arena.isInArena(event.getBlock().getLocation())) return;
 		event.setCancelled(true);
 	}
 	
 	@EventHandler
 	public void onPlayerChat(PlayerChatEvent event) {
 		if(!plugin.arena.playing.contains(event.getPlayer())) return;
 		if(!plugin.mainConf.getBoolean("useSeparatePlayerChat", false)) return;
 		event.setCancelled(true);
 
 		Util.chatInside(ChatColor.GOLD + "<" + event.getPlayer().getName() + "> "+ ChatColor.WHITE + event.getMessage());
 	}
 	
 
 }
