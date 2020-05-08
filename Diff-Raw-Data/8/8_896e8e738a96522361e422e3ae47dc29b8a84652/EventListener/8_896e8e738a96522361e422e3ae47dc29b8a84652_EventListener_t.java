 package net.amoebaman.gamemaster;
 
 import java.util.HashMap;
 
 import net.amoebaman.gamemaster.api.TeamAutoGame;
 import net.amoebaman.gamemaster.enums.MasterStatus;
 import net.amoebaman.gamemaster.enums.PlayerStatus;
 import net.amoebaman.gamemaster.modules.MessagerModule;
 import net.amoebaman.gamemaster.modules.RespawnModule;
 import net.amoebaman.gamemaster.modules.SafeSpawnModule;
 import net.amoebaman.gamemaster.utils.ChatUtils;
 import net.amoebaman.gamemaster.utils.ChatUtils.ColorScheme;
 import net.amoebaman.gamemaster.utils.PlayerMap;
 import net.amoebaman.kitmaster.enums.GiveKitContext;
 import net.amoebaman.kitmaster.utilities.ClearKitsEvent;
 import net.amoebaman.kitmaster.utilities.GiveKitEvent;
 import net.amoebaman.statmaster.StatMaster;
 import net.amoebaman.statmaster.events.KillingSpreeEvent;
 import net.amoebaman.statmaster.events.MultiKillEvent;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.Sound;
 import org.bukkit.World;
import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Minecart;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Tameable;
 import org.bukkit.entity.ThrownPotion;
 import org.bukkit.entity.Vehicle;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.entity.PotionSplashEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.hanging.HangingBreakByEntityEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerItemConsumeEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.event.server.ServerListPingEvent;
 import org.bukkit.event.vehicle.VehicleDamageEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import com.vexsoftware.votifier.model.Vote;
 import com.vexsoftware.votifier.model.VotifierEvent;
 
 public class EventListener implements Listener {
 	
 	@EventHandler(priority=EventPriority.LOW)
 	public void blockPlace(BlockPlaceEvent event){
 		if(GameMaster.getStatus(event.getPlayer()) != PlayerStatus.ADMIN)
 			event.setCancelled(true);
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	public void blockBreak(BlockBreakEvent event){
 		if(GameMaster.getStatus(event.getPlayer()) != PlayerStatus.ADMIN)
 			event.setCancelled(true);
 	}
 	
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void entityDamageModify(final EntityDamageEvent event){
 		/*
 		 * Don't even bother if no event is running
 		 */
 		if(GameMaster.status != MasterStatus.RUNNING){
 			event.setCancelled(true);
 			return;
 		}
 		/*
 		 * Determine the victim
 		 */
 		Player victim = null;
 		if(event.getEntity() instanceof Player)
 			victim = (Player) event.getEntity();
 		if(event.getEntity() instanceof Tameable && ((Tameable) event.getEntity()).getOwner() instanceof Player)
 			victim = (Player) ((Tameable) event.getEntity()).getOwner();
 		if(victim == null)
 			return;
 		/*
 		 * Cancel if the victim isn't playing, or is respawning
 		 */
 		if(GameMaster.getStatus(victim) != PlayerStatus.PLAYING || GameMaster.respawning.contains(victim)){
 			event.setCancelled(true);
 			return;
 		}
 		/*
 		 * We're only really concerned about players damaged by entities here...
 		 */
 		if(event instanceof EntityDamageByEntityEvent){
 			EntityDamageByEntityEvent eEvent = (EntityDamageByEntityEvent) event;
 			/*
 			 * Determine who did the damage
 			 */
 			Player damager = null;
 			if(eEvent.getDamager() instanceof Player)
 				damager = (Player) eEvent.getDamager();
 			if(eEvent.getDamager() instanceof Projectile){
 				Projectile proj = (Projectile) eEvent.getDamager();
 				if(proj.getShooter() instanceof Player)
 					damager = (Player) proj.getShooter();
 			}
 			if(eEvent.getDamager() instanceof Tameable && ((Tameable) eEvent.getDamager()).getOwner() instanceof Player)
 				damager = (Player) ((Tameable) eEvent.getDamager()).getOwner();
 			if(damager == null)
 				return;
 			/*
 			 * Teammates can't hurt each other
 			 */
 			if(GameMaster.activeGame instanceof TeamAutoGame){
 				TeamAutoGame game = (TeamAutoGame) GameMaster.activeGame;
 				if(game.getTeam(victim) == game.getTeam(damager))
 					event.setCancelled(true);
 			}
 			/*
 			 * Safe spawn games
 			 */
 			if(GameMaster.activeGame instanceof SafeSpawnModule){
 				SafeSpawnModule game = (SafeSpawnModule) GameMaster.activeGame;
 				if(victim.getLocation().distance(game.getRespawnLoc(victim)) < game.getSpawnRadius(victim)){
 					event.setCancelled(true);
 					damager.sendMessage(ChatUtils.format("You can't damage enemies that are in their spawn", ColorScheme.ERROR));
 				}
 				if(damager.getLocation().distance(game.getRespawnLoc(damager)) < game.getSpawnRadius(damager) && victim.getLocation().distance(game.getRespawnLoc(damager)) > game.getSpawnRadius(damager)){
 					event.setCancelled(true);
 					damager.sendMessage(ChatUtils.format("You can't attack enemies from the safety of your spawn", ColorScheme.ERROR));
 				}
 			}
 			/*
 			 * Schedule to modify the damage so that the real damager (wolf owner, arrow shooter) gets the credit
 			 */
 			final Player fVictim = victim, fDamager = damager;
 			Bukkit.getScheduler().scheduleSyncDelayedTask(GameMaster.plugin(), new Runnable(){ public void run(){
 				fVictim.setLastDamageCause(new EntityDamageByEntityEvent(fVictim, fDamager, DamageCause.ENTITY_ATTACK, event.getDamage()));
 			}});
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
 	public void entityDamageMonitor(EntityDamageEvent event){
 		if(!(event.getEntity() instanceof Player))
 			return;
 		if(GameMaster.status.active && GameMaster.activeGame instanceof SafeSpawnModule){
 			final Player player = (Player) event.getEntity();
 			if(GameMaster.getStatus(player) == PlayerStatus.PLAYING && (event.getCause() == DamageCause.ENTITY_ATTACK || event.getCause() == DamageCause.PROJECTILE || event.getCause() == DamageCause.MAGIC))
 				GameMaster.lastDamage.put(player, System.currentTimeMillis());
 		}
 		if(event.getCause() != DamageCause.MAGIC)
 			splashHarms.remove((Player) event.getEntity());
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	public void foodLevelChange(FoodLevelChangeEvent event){
 		if(GameMaster.status != MasterStatus.RUNNING || GameMaster.getStatus((Player) event.getEntity()) == PlayerStatus.ADMIN)
 			event.setCancelled(true);
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	public void playerJoin(final PlayerJoinEvent event){
 		final Player player = event.getPlayer();
 		Bukkit.getScheduler().scheduleSyncDelayedTask(GameMaster.plugin(), new Runnable(){ public void run(){
 			switch(GameMaster.status){
 				case PREP:
 					player.sendMessage(ChatUtils.format("The server is currently preparing to launch [[" + GameMaster.activeGame.getGameName() + "]]", ColorScheme.HIGHLIGHT));
 					player.sendMessage(ChatUtils.format("Type [['/help games']] for more info", ColorScheme.HIGHLIGHT));
 					break;
 				case INTERMISSION:
 					player.sendMessage(ChatUtils.format("The server is waiting to begin the next event", ColorScheme.HIGHLIGHT));
 				default: }
 		} }, 20);
 		/*
 		 * If the player is an admin, leave them be
 		 */
 		if(player.hasPermission("gamemaster.admin"))
 			GameMaster.changeStatus(player, PlayerStatus.ADMIN);
 		/*
 		 * If the player is new, welcome them and send them to the newbie initiation room
 		 */
 		else if(!player.hasPlayedBefore()){
 			GameMaster.changeStatus(player, PlayerStatus.EXTERIOR);
 			//TODO Welcome player to server
 		}
 		/*
 		 * Otherwise, just shove them headfirst into the games
 		 */
 		else{
 			GameMaster.changeStatus(player, PlayerStatus.PLAYING);
 			if(GameMaster.status.active)
 				GameMaster.activeGame.addPlayer(player);
 			else
 				player.teleport(GameMaster.mainLobby);
 		}
 		/*
 		 * Notify admins with a sound
 		 */
 		for(Player other : Bukkit.getOnlinePlayers())
 			if(other.hasPermission("gamemaster.admin"))
 				other.playSound(other.getLocation(), Sound.LEVEL_UP, 0.5f, 1f);
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	public void playerQuit(PlayerQuitEvent event){
 		Player player = event.getPlayer();
 		GameMaster.players.remove(player);
 		GameMaster.lastDamage.remove(player);
 		if(GameMaster.status.active && GameMaster.getStatus(player) == PlayerStatus.PLAYING){
 			player.teleport(GameMaster.mainLobby);
 			GameMaster.resetPlayer(player);
 			GameMaster.activeGame.removePlayer(player);
 			player.setPlayerListName(player.getName());
 		}
 		for(Player other : Bukkit.getOnlinePlayers())
 			if(other.hasPermission("gamemaster.admin"))
 				other.playSound(other.getLocation(), Sound.ENDERDRAGON_GROWL, 0.5f, 1f);
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	public void playerKick(PlayerKickEvent event){
 		playerQuit(new PlayerQuitEvent(event.getPlayer(), "redirect"));
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	public void playerRespawn(final PlayerRespawnEvent event){
 		final Player player = event.getPlayer();
 		player.removePotionEffect(PotionEffectType.INVISIBILITY);
 		if(!GameMaster.status.active)
 			Bukkit.getScheduler().scheduleSyncDelayedTask(GameMaster.plugin(), new Runnable(){ public void run(){
 				player.teleport(GameMaster.mainLobby);
 			}});
 		else if(GameMaster.activeGame instanceof RespawnModule){
 			final RespawnModule game = (RespawnModule) GameMaster.activeGame;
 			/*
 			 * If and only if the player is playing...
 			 */
 			if(GameMaster.getStatus(player) == PlayerStatus.PLAYING){
 				/*
 				 * First send them to the waiting location
 				 */
 				Bukkit.getScheduler().scheduleSyncDelayedTask(GameMaster.plugin(), new Runnable(){ public void run(){
 					player.teleport(game.getWaitingLoc(player));
 					GameMaster.respawning.add(player);
 				}});
 				/*
 				 * After the delay has passed...
 				 */
 				Bukkit.getScheduler().scheduleSyncDelayedTask(GameMaster.plugin(), new Runnable(){ public void run(){
 					/*
 					 * If they're still active, respawn them
 					 */
 					if(GameMaster.status.active && GameMaster.getStatus(player) == PlayerStatus.PLAYING && player.isOnline()){
 						player.teleport(game.getRespawnLoc(player));
 						player.setNoDamageTicks(20 * game.getRespawnInvulnSeconds(player));
 						/*
 						 * If the game also happens to be MessagerCompatible, send messages as well
 						 */
 						if(game instanceof MessagerModule){
 							player.sendMessage(ChatUtils.spacerLine());
 							for(String line : ((MessagerModule) game).getSpawnMessage(player))
 								player.sendMessage(ChatUtils.centerAlign(ChatUtils.format(line, ColorScheme.HIGHLIGHT)));
 							player.sendMessage(ChatUtils.spacerLine());
 						}
 						GameMaster.respawning.remove(player);
 					}
 				}}, 20 * game.getRespawnSeconds(player));
 				return;
 			}
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	public void playerDeath(EntityDeathEvent event){
 		event.getDrops().clear();
 		GameMaster.lastDamage.remove(event.getEntity());
 		if(event instanceof PlayerDeathEvent){
 			Player player = (Player) event.getEntity();
 			if(splashHarms.containsKey(player)){
 				Player thrower = Bukkit.getPlayer(splashHarms.get(player));
 				if(thrower != null){
 					player.setLastDamageCause(new EntityDamageByEntityEvent(thrower, player, DamageCause.ENTITY_ATTACK, 10.0));
 					splashHarms.remove(player);
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void inventoryClick(InventoryClickEvent event){
 		Player player = (Player) event.getWhoClicked();
 		if(GameMaster.getStatus(player) == PlayerStatus.PLAYING){
 			Inventory topInv = event.getView().getTopInventory();
 			if(topInv != null && (topInv.getHolder() instanceof BlockState || topInv.getHolder() instanceof Vehicle))
 				event.setCancelled(true);
 		}
 	}
 	
 	private HashMap<Player, Horse> steeds = new HashMap<Player, Horse>();
 	@EventHandler(priority=EventPriority.LOW)
 	public void creatureSpawn(CreatureSpawnEvent event){
 		if(event.getEntity() instanceof Tameable && (event.getSpawnReason() == SpawnReason.BREEDING || event.getSpawnReason() == SpawnReason.SPAWNER_EGG)){
 			Tameable pet = (Tameable) event.getEntity();
 			for(Entity other : event.getEntity().getNearbyEntities(5, 5, 5))
 				if(other instanceof Player){
 					Player tamer = (Player) other;
 					if(tamer.getItemInHand().getType() == Material.MONSTER_EGG){
 						pet.setOwner(tamer);
 						if(pet instanceof Wolf){
 							int tamed = 0;
 							for(Wolf each : tamer.getWorld().getEntitiesByClass(Wolf.class))
 								if(tamer.equals(each.getOwner()))
 									tamed++;
 							if(tamed >= 16){
 								event.setCancelled(true);
 								tamer.sendMessage(ChatColor.DARK_RED + "You can only spawn in 16 wolves at once");
 								return;
 							}
 							
 							Wolf wolf = (Wolf) pet;
 							wolf.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 24000, 1));
 							wolf.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 24000, 1));
 							wolf.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 24000, 1));
 							wolf.setMaxHealth(20);
 							if(GameMaster.activeGame instanceof TeamAutoGame && GameMaster.getStatus(tamer) == PlayerStatus.PLAYING)
 								wolf.setCollarColor(((TeamAutoGame) GameMaster.activeGame).getTeam(tamer).dye);
 						}
 						if(pet instanceof Horse){
 							if(steeds.containsKey(tamer) && !steeds.get(tamer).isDead()){
 								event.setCancelled(true);
 								tamer.sendMessage(ChatColor.DARK_RED + "You can only spawn in 1 horse at once");
 								return;
 							}
 							
 							Horse horse = (Horse) pet;
 							horse.setDomestication(horse.getMaxDomestication());
 							horse.setJumpStrength(0.75);
 							horse.setMaxHealth(40);
 							horse.setAdult();
 							horse.setTamed(true);
 							horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
 							steeds.put(tamer, horse);
 						}
 						return;
 					}
 				}
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	public void wolfRecall(PlayerInteractEvent event){
 		Player player = event.getPlayer();
 		if((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && player.getItemInHand() != null && player.getItemInHand().getType() == Material.BONE)
 			for(LivingEntity e : player.getWorld().getLivingEntities())
 				if(e instanceof Wolf && player.equals(((Wolf) e).getOwner()))
 					((Wolf) e).setTarget(null);
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	public void playerChat(AsyncPlayerChatEvent event){
 		Player player = event.getPlayer();
 		if(GameMaster.teamChatters.contains(player) && GameMaster.getStatus(player) == PlayerStatus.PLAYING && GameMaster.status.active && GameMaster.activeGame instanceof TeamAutoGame){
 			TeamAutoGame game = (TeamAutoGame) GameMaster.activeGame;
 			for(Player other : Bukkit.getOnlinePlayers())
 				if(game.getTeam(player) != game.getTeam(other))
 					event.getRecipients().remove(other);
 			event.setMessage("(" + game.getTeam(player).chat + "TEAM" + ChatColor.WHITE + ") " + event.getMessage());
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	public void votifier(VotifierEvent event){
 		Vote vote = event.getVote();
 		if(vote == null){
 			GameMaster.logger().severe("VotifierEvent returned null vote");
 			return;
 		}
 		GameMaster.logger().info("Received vote from " + vote.getServiceName() + " by " + vote.getUsername() + " from " + vote.getAddress() + " at " + vote.getTimeStamp());
 		OfflinePlayer player = Bukkit.getPlayer(vote.getUsername());
 		if(player == null)
 			player = Bukkit.getOfflinePlayer(vote.getUsername());
 		if(player.hasPlayedBefore() || player.isOnline()){
 			StatMaster.getHandler().incrementStat(player, "charges");
 			StatMaster.getHandler().incrementCommunityStat("votes");
 			Bukkit.broadcastMessage(ChatUtils.format("[[" + player.getName() + "]] voted for the server, and now has [[" + StatMaster.getHandler().getStat(player, "charges") + "]] charges", ColorScheme.HIGHLIGHT));
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
 	public void giveKit(GiveKitEvent event){
 		final Player player = event.getPlayer();
 		if(GameMaster.getStatus(player) == PlayerStatus.PLAYING && GameMaster.status == MasterStatus.RUNNING)
 			if(GameMaster.activeGame instanceof SafeSpawnModule && player.getLocation().distance(((SafeSpawnModule) GameMaster.activeGame).getRespawnLoc(player)) > 10)
 				if(!event.getContext().overrides && event.getContext() != GiveKitContext.SIGN_TAKEN && !player.hasPermission("gamemaster.globalkit")){
 					player.sendMessage(ChatUtils.format("You must be within [[10 blocks]] of your spawn to choose a kit.", ColorScheme.ERROR));
 					event.setCancelled(true);
 					return;
 				}
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	public void clearKits(ClearKitsEvent event){
 		Player player = event.getPlayer();
 		for(World world : Bukkit.getWorlds())
 			for(Entity e : world.getEntities()){
 				if(e instanceof Tameable && player.equals(((Tameable) e).getOwner())){
 					e.getWorld().playSound(e.getLocation(), Sound.FIZZ, 1f, 1f);
 					e.remove();
 				}
 				if(e instanceof Projectile){
 					Projectile proj = (Projectile) e;
 					if(player.equals(proj.getShooter()))
 						proj.remove();
 				}
 			}
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	public void playerInteract(PlayerInteractEvent event){
 		final Player player = event.getPlayer();
 		/*
 		 * Prevent players messing with scenery
 		 */
 		if(GameMaster.getStatus(player) != PlayerStatus.ADMIN && event.getAction().name().contains("CLICK_BLOCK")){
 			Material mat = event.getClickedBlock().getType();
			if(mat == Material.TRAP_DOOR || mat == Material.LEVER || mat == Material.ITEM_FRAME)
 				event.setCancelled(true);
			for(BlockFace face : BlockFace.values())
				if(event.getClickedBlock().getRelative(face).getType() == Material.FIRE)
					event.setCancelled(true);
 		}
 	}
 	
 	/*
 	 * This is needed to make infinite stacks of consumable items work properly.
 	 */
 	@EventHandler(priority=EventPriority.LOW)
 	public void playerItemConsume(PlayerItemConsumeEvent event){
 		ItemStack item = event.getItem().clone();
 		if(item.getAmount() < 0){
 			/*
 			 * According to Bukkit APIDocs, modifying the event item consumed will
 			 * apply the effects the new item, but WILL NOT consume the original.
 			 * Changing the amount of the item should be sufficient to trigger this.
 			 */
 			item.setAmount(1);
 			event.setItem(item);
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	public void hangingBreak(HangingBreakByEntityEvent event){
 		Player player = null;
 		if(event.getEntity() instanceof Player)
 			player = (Player) event.getEntity();
 		if(event.getEntity() instanceof Projectile){
 			Projectile proj = (Projectile) event.getEntity();
 			if(proj.getShooter() instanceof Player)
 				player = (Player) proj.getShooter();
 		}
 		if(player == null)
 			return;
 		if(GameMaster.getStatus(player) != PlayerStatus.ADMIN)
 			event.setCancelled(true);
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	public void vehicleDamage(VehicleDamageEvent event){
 		Player player = null;
 		if(event.getAttacker() instanceof Player)
 			player = (Player) event.getAttacker();
 		if(event.getAttacker() instanceof Projectile){
 			Projectile proj = (Projectile) event.getAttacker();
 			if(proj.getShooter() instanceof Player)
 				player = (Player) proj.getShooter();
 		}
 		if(player == null)
 			return;
 		if(GameMaster.getStatus(player) != PlayerStatus.ADMIN && event.getVehicle() instanceof Minecart)
 			event.setCancelled(true);
 	}
 	
 	private static PlayerMap<String> splashHarms = new PlayerMap<String>();
 	@EventHandler(priority=EventPriority.LOW)
 	public void potionSplash(PotionSplashEvent event){
 		ThrownPotion potion = event.getPotion();
 		Player thrower = null;
 		if(potion.getShooter() instanceof Player)
 			thrower = (Player) potion.getShooter();
 		if(thrower == null || GameMaster.status != MasterStatus.RUNNING)
 			return;
 		for(LivingEntity entity : event.getAffectedEntities()){
 			if(entity instanceof Player){
 				Player victim = (Player) entity;
 				if(GameMaster.activeGame instanceof TeamAutoGame){
 					TeamAutoGame game = (TeamAutoGame) GameMaster.activeGame;
 					if(game.getTeam(thrower) != game.getTeam(victim)){
 						if(victim.getLocation().distance(game.getRespawnLoc(victim)) < game.getSpawnRadius(victim)){
 							event.setIntensity(victim, 0);
 							thrower.sendMessage(ChatUtils.format("You can't damage enemies that are in their spawn", ColorScheme.ERROR));
 						}
 						if(thrower.getLocation().distance(game.getRespawnLoc(thrower)) < game.getSpawnRadius(thrower) && victim.getLocation().distance(game.getRespawnLoc(thrower)) >= game.getSpawnRadius(thrower)){
 							event.setIntensity(victim, 0);
 							thrower.sendMessage(ChatUtils.format("You can't damage enemies out of your spawn", ColorScheme.ERROR));
 						}
 					}
 					else if(!victim.equals(thrower))
 						for(PotionEffect effect : potion.getEffects())
 							switch(effect.getType().getId()){
 								case 2:
 								case 4:
 								case 7:
 								case 9:
 								case 15:
 								case 17:
 								case 18:
 								case 19:
 								case 20:
 									event.setIntensity(victim, 0);
 							}
 				}
 				for(PotionEffect effect : potion.getEffects())
 					if(effect.getType().equals(PotionEffectType.HARM))
 						splashHarms.put(victim, thrower.getName());
 			}
 		}
 	}
 	
 	private static PlayerMap<Long> teleports = new PlayerMap<Long>(0L);
 	@EventHandler(priority=EventPriority.LOW)
 	public void preventEnderPearlSpam(PlayerTeleportEvent event){
 		Player player = event.getPlayer();
 		if(GameMaster.getStatus(player) == PlayerStatus.PLAYING && event.getCause() == TeleportCause.ENDER_PEARL){
 			if(System.currentTimeMillis() - teleports.get(player) < 3000){
 				player.sendMessage(ChatUtils.format("You need to wait [[" + (int)((teleports.get(player) + 3000 - System.currentTimeMillis()) / 1000) + "]] seconds to teleport again", ColorScheme.ERROR));
 				event.setCancelled(true);
 			}
 			else
 				teleports.put(player, System.currentTimeMillis());
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	public void updateServerList(ServerListPingEvent event){
 		switch(GameMaster.status){
 			case INTERMISSION:
 				event.setMotd(ChatUtils.format("Voting on the next game", ColorScheme.HIGHLIGHT));
 				break;
 			case PREP:
 				event.setMotd(ChatUtils.format("Voting on a map for [[" + GameMaster.activeGame + "]]", ColorScheme.HIGHLIGHT));
 				break;
 			case RUNNING:
 			case SUSPENDED:
 				event.setMotd(ChatUtils.format("Playing [[" + GameMaster.activeGame + "]] on [[" + GameMaster.activeMap + "]]", ColorScheme.HIGHLIGHT));
 				break;
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	public void multiKill(MultiKillEvent event){
 		if(event.getAmount() >= 3){
 			StatMaster.getHandler().adjustStat(event.getPlayer(), "charges", (event.getAmount() - 2) * 0.25);
 			event.getPlayer().sendMessage(ChatUtils.format("You have received [[" + (event.getAmount() - 2) * 0.25 + "]] charges for your [[" + event.getAmount() + "]]x multikill", ColorScheme.HIGHLIGHT));
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.LOW)
 	public void killingSpree(KillingSpreeEvent event){
 		if(event.getSpree() >= 5 && event.isEnded()){
 			StatMaster.getHandler().adjustStat(event.getPlayer(), "charges", (event.getSpree() / 5) * 0.1);
 			event.getPlayer().sendMessage(ChatUtils.format("You have received [[" + ((event.getSpree() / 5) * 0.1) + "]] charges for your [[" + event.getSpree() + "]] kill spree", ColorScheme.HIGHLIGHT));
 		}
 	}
 	
 }
