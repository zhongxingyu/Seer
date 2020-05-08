 package com.turt2live.antishare;
 
 import java.util.HashMap;
 
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerGameModeChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerPortalEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class AntiShareListener implements Listener {
 
 	private AntiShare plugin;
 	private HashMap<Player, Long> blockDropTextWarnings = new HashMap<Player, Long>();
 
 	public AntiShareListener(AntiShare p){
 		plugin = p;
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onBlockBreak(BlockBreakEvent event){
 		Player player = event.getPlayer();
 		if(player != null && !event.isCancelled()){
 			boolean itemIsBlocked = false;
 			int item = event.getBlock().getTypeId();
 			itemIsBlocked = ASUtils.isBlocked(plugin.config().getString("events.block_break", player.getWorld()), item);
 			if(plugin.config().getBoolean("other.only_if_creative", player.getWorld()) && itemIsBlocked){
 				if(player.getGameMode() == GameMode.CREATIVE){
 					if(player.hasPermission("AntiShare.break") && !player.hasPermission("AntiShare.allow.break")){
 						event.setCancelled(true);
 						ASUtils.sendToPlayer(player, plugin.config().getString("messages.block_break", player.getWorld()));
 						ASNotification.sendNotification(NotificationType.ILLEGAL_BLOCK_BREAK, plugin, player, event.getBlock().getType().name());
 					}else{
 						ASNotification.sendNotification(NotificationType.LEGAL_BLOCK_BREAK, plugin, player, event.getBlock().getType().name());
 					}
 				}
 			}else if(player.hasPermission("AntiShare.break") && !player.hasPermission("AntiShare.allow.break") && itemIsBlocked){
 				event.setCancelled(true);
 				ASUtils.sendToPlayer(player, plugin.config().getString("messages.block_break", player.getWorld()));
 				ASNotification.sendNotification(NotificationType.ILLEGAL_BLOCK_BREAK, plugin, player, event.getBlock().getType().name());
 			}
 			//Bedrock check
 			if(!event.isCancelled()
 					&& !plugin.config().getBoolean("other.allow_bedrock", player.getWorld())
 					&& !player.hasPermission("AntiShare.bedrock")
 					&& event.getBlock().getType() == Material.BEDROCK){
 				if(plugin.getConfig().getBoolean("other.only_if_creative")){
 					if(player.getGameMode() == GameMode.CREATIVE){
 						ASUtils.sendToPlayer(player, plugin.config().getString("messages.bedrock", player.getWorld()));
 						ASNotification.sendNotification(NotificationType.ILLEGAL_BEDROCK, plugin, player, event.getBlock().getType().name());
 						event.setCancelled(true);
 					}
 				}else{
 					ASUtils.sendToPlayer(player, plugin.config().getString("messages.bedrock", player.getWorld()));
 					ASNotification.sendNotification(NotificationType.ILLEGAL_BEDROCK, plugin, player, event.getBlock().getType().name());
 					event.setCancelled(true);
 				}
 			}else if(event.getBlock().getType() == Material.BEDROCK
 					&& player.hasPermission("AntiShare.bedrock")
 					&& !plugin.config().getBoolean("other.allow_bedrock", player.getWorld())
 					&& !event.isCancelled()){
 				ASNotification.sendNotification(NotificationType.LEGAL_BEDROCK, plugin, player, event.getBlock().getType().name());
 			}
 			//Creative Mode Blocking
 			if(!event.isCancelled()
 					&& plugin.config().getBoolean("other.track_blocks", player.getWorld())
 					&& !player.hasPermission("AntiShare.blockBypass")){
 				if(player.getGameMode() == GameMode.SURVIVAL){
 					boolean isBlocked = ASBlockRegistry.isBlockCreative(event.getBlock());
 					if(isBlocked){
 						if(!plugin.getConfig().getBoolean("other.blockDrops")){
 							ASUtils.sendToPlayer(player, plugin.config().getString("messages.creativeModeBlock", player.getWorld()));
 							event.setCancelled(true);
 						}else{
 							ASUtils.sendToPlayer(player, plugin.config().getString("messages.creativeModeBlock", player.getWorld()));
 							ASBlockRegistry.unregisterCreativeBlock(event.getBlock());
 							Block block = event.getBlock();
 							event.setCancelled(true);
 							block.setTypeId(0); // Fakes a break
 						}
 						ASNotification.sendNotification(NotificationType.ILLEGAL_CREATIVE_BLOCK_BREAK, plugin, player, event.getBlock().getType().name());
 					}
 				}else{
 					//ASNotification.sendNotification(NotificationType.LEGAL_CREATIVE_BLOCK_BREAK, plugin, player, event.getBlock().getType().name());
 					ASBlockRegistry.unregisterCreativeBlock(event.getBlock());
 				}
 			}
 		}
 		if(!event.isCancelled()){
 			ASNotification.sendNotification(NotificationType.LEGAL_BLOCK_BREAK, plugin, player, event.getBlock().getType().name());
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onBlockDamage(BlockDamageEvent event){
 		Player player = event.getPlayer();
 		if(player != null && !event.isCancelled()){
 			boolean itemIsBlocked = false;
 			int item = event.getBlock().getTypeId();
 			itemIsBlocked = ASUtils.isBlocked(plugin.config().getString("events.block_break", player.getWorld()), item);
 			if(plugin.config().getBoolean("other.only_if_creative", player.getWorld()) && itemIsBlocked){
 				if(player.getGameMode() == GameMode.CREATIVE){
 					if(player.hasPermission("AntiShare.break") && !player.hasPermission("AntiShare.allow.break")){
 						event.setCancelled(true);
 						ASUtils.sendToPlayer(player, plugin.config().getString("messages.block_break", player.getWorld()));
 					}
 				}
 			}else if(player.hasPermission("AntiShare.break") && !player.hasPermission("AntiShare.allow.break") && itemIsBlocked){
 				event.setCancelled(true);
 				ASUtils.sendToPlayer(player, plugin.config().getString("messages.block_break", player.getWorld()));
 			}
 		}
 		// Warning message for block drops
 		if(!event.isCancelled()){
 			if(plugin.getConfig().getBoolean("other.blockDrops")
 					&& !player.hasPermission("AntiShare.blockBypass")
 					&& ASBlockRegistry.isBlockCreative(event.getBlock())){
 				long systemTime = System.currentTimeMillis();
 				if(blockDropTextWarnings.containsKey(player)){
 					if((systemTime - blockDropTextWarnings.get(player)) > 1000){
 						ASUtils.sendToPlayer(player, plugin.getConfig().getString("messages.noBlockDrop"));
 						blockDropTextWarnings.remove(player);
 						blockDropTextWarnings.put(player, systemTime);
 					}
 				}else{
 					ASUtils.sendToPlayer(player, plugin.getConfig().getString("messages.noBlockDrop"));
 					blockDropTextWarnings.put(player, systemTime);
 				}
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onBlockPlace(BlockPlaceEvent event){
 		Player player = event.getPlayer();
 		if(player != null && !event.isCancelled()){
 			boolean itemIsBlocked = false;
 			int item = event.getBlockPlaced().getTypeId();
 			itemIsBlocked = ASUtils.isBlocked(plugin.config().getString("events.block_place", player.getWorld()), item);
 			if(plugin.config().getBoolean("other.only_if_creative", player.getWorld()) && itemIsBlocked){
 				if(player.getGameMode() == GameMode.CREATIVE){
 					if(player.hasPermission("AntiShare.place") && !player.hasPermission("AntiShare.allow.place")){
 						event.setCancelled(true);
 						ASUtils.sendToPlayer(player, plugin.config().getString("messages.block_place", player.getWorld()));
 						ASNotification.sendNotification(NotificationType.ILLEGAL_BLOCK_PLACE, plugin, player, event.getBlock().getType().name());
 					}else{
 						ASNotification.sendNotification(NotificationType.LEGAL_BLOCK_PLACE, plugin, player, event.getBlock().getType().name());
 					}
 				}
 			}else if(player.hasPermission("AntiShare.place") && !player.hasPermission("AntiShare.allow.place") && itemIsBlocked){
 				event.setCancelled(true);
 				ASUtils.sendToPlayer(player, plugin.config().getString("messages.block_place", player.getWorld()));
 			}
 		}
 		//Bedrock check
 		if(!event.isCancelled()
 				&& !plugin.config().getBoolean("other.allow_bedrock", player.getWorld())
 				&& !player.hasPermission("AntiShare.bedrock")
 				&& event.getBlock().getType() == Material.BEDROCK){
 			if(plugin.getConfig().getBoolean("other.only_if_creative")){
 				if(player.getGameMode() == GameMode.CREATIVE){
 					ASUtils.sendToPlayer(player, plugin.config().getString("messages.bedrock", player.getWorld()));
 					event.setCancelled(true);
 				}
 			}else{
 				ASUtils.sendToPlayer(player, plugin.config().getString("messages.bedrock", player.getWorld()));
 				event.setCancelled(true);
 			}
 			ASNotification.sendNotification(NotificationType.ILLEGAL_BEDROCK, plugin, player, event.getBlock().getType().name());
 		}else if(event.getBlock().getType() == Material.BEDROCK
 				&& player.hasPermission("AntiShare.bedrock")
 				&& !plugin.config().getBoolean("other.allow_bedrock", player.getWorld())
 				&& !event.isCancelled()){
 			ASNotification.sendNotification(NotificationType.LEGAL_BEDROCK, plugin, player, event.getBlock().getType().name());
 		}
 		//Creative Mode Placing
 		if(!event.isCancelled()
 				&& plugin.config().getBoolean("other.track_blocks", player.getWorld())
 				&& player.getGameMode() == GameMode.CREATIVE
 				&& !player.hasPermission("AntiShare.freePlace")){
 			ASBlockRegistry.saveCreativeBlock(event.getBlock(), player.getName());
 		}
 		if(!event.isCancelled()){
 			ASNotification.sendNotification(NotificationType.LEGAL_BLOCK_PLACE, plugin, player, event.getBlock().getType().name());
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onEntityDamage(EntityDamageEvent event){
 		if(!(event instanceof EntityDamageByEntityEvent)
 				|| event.isCancelled()){
 			return;
 		}
 		Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
 		if(damager instanceof Player){
 			Player dealer = (Player) damager;
 			if(dealer.getGameMode() != GameMode.CREATIVE && !plugin.getConfig().getBoolean("other.only_if_creative")){
 				return;
 			}else if(dealer.getGameMode() != GameMode.CREATIVE){
 				return;
 			}
 			//System.out.println("GM: " + dealer.getGameMode().toString());
 			if(event.getEntity() instanceof Player){
 				if(plugin.getConfig().getBoolean("other.pvp")){
 					ASNotification.sendNotification(NotificationType.LEGAL_PLAYER_PVP, plugin, dealer, ((Player) event.getEntity()).getName());
 					return;
 				}
 				if(!dealer.hasPermission("AntiShare.pvp")){
 					ASUtils.sendToPlayer(dealer, plugin.config().getString("messages.pvp", event.getEntity().getWorld()));
 					ASNotification.sendNotification(NotificationType.ILLEGAL_PLAYER_PVP, plugin, dealer, ((Player) event.getEntity()).getName());
 					event.setCancelled(true);
 				}
 			}else{
 				if(plugin.getConfig().getBoolean("other.pvp-mob")){
 					ASNotification.sendNotification(NotificationType.LEGAL_MOB_PVP, plugin, dealer, event.getEntity().getClass().getName().replace("Craft", "").replace("org.bukkit.craftbukkit.entity.", ""));
 					return;
 				}
 				if(!dealer.hasPermission("AntiShare.mobpvp")){
 					ASNotification.sendNotification(NotificationType.ILLEGAL_MOB_PVP, plugin, dealer, event.getEntity().getClass().getName().replace("Craft", "").replace("org.bukkit.craftbukkit.entity.", ""));
 					ASUtils.sendToPlayer(dealer, plugin.config().getString("messages.mobpvp", event.getEntity().getWorld()));
 					event.setCancelled(true);
 				}else{
 					ASNotification.sendNotification(NotificationType.LEGAL_MOB_PVP, plugin, dealer, event.getEntity().getClass().getName().replace("Craft", "").replace("org.bukkit.craftbukkit.entity.", ""));
 				}
 			}
 		}else if(damager instanceof Projectile){
 			LivingEntity shooter = ((Projectile) damager).getShooter();
 			if(shooter instanceof Player){
 				Player dealer = ((Player) shooter);
 				if(dealer.getGameMode() != GameMode.CREATIVE && !plugin.getConfig().getBoolean("other.only_if_creative")){
 					ASNotification.sendNotification(NotificationType.LEGAL_PLAYER_PVP, plugin, dealer, ((Player) event.getEntity()).getName());
 					return;
 				}else if(dealer.getGameMode() != GameMode.CREATIVE){
 					ASNotification.sendNotification(NotificationType.LEGAL_PLAYER_PVP, plugin, dealer, ((Player) event.getEntity()).getName());
 					return;
 				}
 				if(!dealer.hasPermission("AntiShare.pvp")){
 					ASUtils.sendToPlayer(dealer, plugin.config().getString("messages.pvp", dealer.getWorld()));
 					ASNotification.sendNotification(NotificationType.ILLEGAL_PLAYER_PVP, plugin, dealer, ((Player) event.getEntity()).getName());
 					event.setCancelled(true);
 				}
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onEntityDeath(EntityDeathEvent event){
 		// System.out.println("onDeath | " + event.getEntity());
 		if(event.getEntity() instanceof Player){
 			Player player = (Player) event.getEntity();
 			if(player != null){
 				boolean illegal = false;
 				for(ItemStack item : event.getDrops()){
 					boolean itemIsBlocked = false;
 					itemIsBlocked = ASUtils.isBlocked(plugin.config().getString("events.death", player.getWorld()), item.getTypeId());
 					if(plugin.config().getBoolean("other.only_if_creative", player.getWorld()) && itemIsBlocked){
 						if(player.getGameMode() == GameMode.CREATIVE){
 							if(player.hasPermission("AntiShare.death") && !player.hasPermission("AntiShare.allow.death")){
 								illegal = true;
 								item.setAmount(0);
 							}
 						}
 					}else if(player.hasPermission("AntiShare.death") && !player.hasPermission("AntiShare.allow.death") && itemIsBlocked){
 						illegal = true;
 						item.setAmount(0);
 					}
 				}
 				if(illegal){
 					ASUtils.sendToPlayer(player, plugin.config().getString("messages.death", player.getWorld()));
 					ASNotification.sendNotification(NotificationType.ILLEGAL_DEATH, plugin, player, player.getGameMode().toString());
 				}else{
 					ASNotification.sendNotification(NotificationType.LEGAL_DEATH, plugin, player, player.getGameMode().toString());
 				}
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onEntityTarget(EntityTargetEvent event){
 		if(event.isCancelled())
 			return;
 
 		Entity targetEntity = event.getTarget();
 		if(event.getEntity() instanceof Monster
 				&& targetEntity != null
 				&& targetEntity instanceof Player){
 			Player player = (Player) targetEntity;
 			if(plugin.getConfig().getBoolean("other.only_if_creative")){
 				if(player.getGameMode() == GameMode.CREATIVE){
 					if(!player.hasPermission("AntiShare.mobpvp")
 							&& !plugin.getConfig().getBoolean("other.mobpvp")){
 						event.setCancelled(true);
 					}
 				}
 			}else{
 				if(!player.hasPermission("AntiShare.mobpvp")
 						&& !plugin.getConfig().getBoolean("other.mobpvp")){
 					event.setCancelled(true);
 				}
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onPlayerCommand(PlayerCommandPreprocessEvent event){
 		String commandSent = event.getMessage();
 		if(event.isCancelled()){
 			return;
 		}
 		Player sender = event.getPlayer();
 		if(sender.hasPermission("AntiShare.allow.commands")){
 			ASNotification.sendNotification(NotificationType.LEGAL_COMMAND, plugin, sender, commandSent);
 			return;
 		}
 		if(plugin.getConfig().getBoolean("other.only_if_creative")){
 			if(sender.getGameMode() != GameMode.CREATIVE){
 				ASNotification.sendNotification(NotificationType.LEGAL_COMMAND, plugin, sender, commandSent);
 				return;
 			}
 		}
 		String commandsToBlock[] = plugin.getConfig().getString("events.commands").split(" ");
 		for(String check : commandsToBlock){
 			if(check.equalsIgnoreCase(commandSent)){
 				ASUtils.sendToPlayer(sender, plugin.getConfig().getString("messages.illegalCommand"));
 				ASNotification.sendNotification(NotificationType.ILLEGAL_COMMAND, plugin, sender, commandSent);
 				event.setCancelled(true);
 				return;
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onPlayerDropItem(PlayerDropItemEvent event){
 		// System.out.println("onDrop | " + event.getPlayer() + " | " + event.getItemDrop().getItemStack().getTypeId());
 		Player player = event.getPlayer();
 		if(player != null && !event.isCancelled()){
 			boolean itemIsBlocked = false;
 			ItemStack item = event.getItemDrop().getItemStack();
 			itemIsBlocked = ASUtils.isBlocked(plugin.config().getString("events.drop_item", player.getWorld()), item.getTypeId());
 			if(plugin.config().getBoolean("other.only_if_creative", player.getWorld()) && itemIsBlocked){
 				if(player.getGameMode() == GameMode.CREATIVE){
 					if(player.hasPermission("AntiShare.drop") && !player.hasPermission("AntiShare.allow.drop")){
 						event.setCancelled(true);
 						ASUtils.sendToPlayer(player, plugin.config().getString("messages.drop_item", player.getWorld()));
 						ASNotification.sendNotification(NotificationType.ILLEGAL_DROP_ITEM, plugin, player, item.getType().name());
 					}else{
 						ASNotification.sendNotification(NotificationType.LEGAL_DROP_ITEM, plugin, player, item.getType().name());
 					}
 				}
 			}else if(player.hasPermission("AntiShare.drop") && !player.hasPermission("AntiShare.allow.drop") && itemIsBlocked){
 				event.setCancelled(true);
 				ASUtils.sendToPlayer(player, plugin.config().getString("messages.drop_item", player.getWorld()));
 				ASNotification.sendNotification(NotificationType.ILLEGAL_DROP_ITEM, plugin, player, item.getType().name());
 			}else{
 				ASNotification.sendNotification(NotificationType.LEGAL_DROP_ITEM, plugin, player, item.getType().name());
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onPlayerGameModeChange(PlayerGameModeChangeEvent event){
 		Player player = event.getPlayer();
 		if(plugin.config().getBoolean("other.inventory_swap", event.getPlayer().getWorld())){
 			if(player != null){
 				if(!player.hasPermission("AntiShare.noswap")){
 					ASInventory.save(player, player.getGameMode(), player.getWorld());
 					ASInventory.load(player, event.getNewGameMode(), player.getWorld());
 					ASUtils.sendToPlayer(player, plugin.config().getString("messages.inventory_swap", player.getWorld()));
 				}
 			}
 		}
 		ASNotification.sendNotification(NotificationType.GAMEMODE_CHANGE, plugin, player, event.getNewGameMode().toString());
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onPlayerInteract(PlayerInteractEvent event){
 		// System.out.println("onInteract | " + event.getPlayer() + " | " + event.getClickedBlock().getTypeId());
 		Player player = event.getPlayer();
 		if(player != null && !event.isCancelled() && event.getClickedBlock() != null){
 			boolean itemIsBlocked = false;
 			int item = event.getClickedBlock().getTypeId();
 			itemIsBlocked = ASUtils.isBlocked(plugin.config().getString("events.interact", player.getWorld()), item);
 			if(plugin.config().getBoolean("other.only_if_creative", player.getWorld()) && itemIsBlocked){
 				if(player.getGameMode() == GameMode.CREATIVE){
 					if(player.hasPermission("AntiShare.interact") && !player.hasPermission("AntiShare.allow.interact")){
 						event.setCancelled(true);
 						ASUtils.sendToPlayer(player, plugin.config().getString("messages.interact", player.getWorld()));
 						ASNotification.sendNotification(NotificationType.ILLEGAL_INTERACTION, plugin, player, event.getClickedBlock().getType().name());
 					}else{
 						ASNotification.sendNotification(NotificationType.LEGAL_INTERACTION, plugin, player, event.getClickedBlock().getType().name());
 					}
 				}
 			}else if(player.hasPermission("AntiShare.interact") && !player.hasPermission("AntiShare.allow.interact") && itemIsBlocked){
 				event.setCancelled(true);
 				ASUtils.sendToPlayer(player, plugin.config().getString("messages.interact", player.getWorld()));
 				ASNotification.sendNotification(NotificationType.ILLEGAL_INTERACTION, plugin, player, event.getClickedBlock().getType().name());
 			}else if(itemIsBlocked){
 				ASNotification.sendNotification(NotificationType.LEGAL_INTERACTION, plugin, player, event.getClickedBlock().getType().name());
 			}
 			//Egg check
 			if(plugin.config().getBoolean("other.allow_eggs", player.getWorld()) == false){
 				boolean filter = false;
 				ItemStack possibleEgg = event.getItem();
 				if(plugin.config().getBoolean("other.only_if_creative", player.getWorld()) && player.getGameMode() == GameMode.CREATIVE){
 					filter = true;
 				}else if(!plugin.config().getBoolean("other.only_if_creative", player.getWorld())){
 					filter = true;
 				}
 				if(player.hasPermission("AntiShare.allow.eggs")){
 					filter = false;
 				}
 				if(filter && (player.hasPermission("AntiShare.eggs"))){
 					if(possibleEgg != null){
 						if(possibleEgg.getTypeId() == 383){
 							event.setCancelled(true);
 							ASUtils.sendToPlayer(player, plugin.config().getString("messages.eggs", player.getWorld()));
 							ASNotification.sendNotification(NotificationType.ILLEGAL_EGG, plugin, player, possibleEgg.getType().name());
 						}
 					}
 				}else if(possibleEgg != null){
 					if((filter || !filter) && possibleEgg.getTypeId() == 383){
 						ASNotification.sendNotification(NotificationType.LEGAL_EGG, plugin, player, possibleEgg.getType().name());
 					}
 				}
 			}
 		}
 	}
 
 	@EventHandler (priority = EventPriority.LOWEST)
 	public void onPlayerPortal(PlayerPortalEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		Player player = event.getPlayer();
 		if(!event.getFrom().getWorld().equals(event.getTo().getWorld())){
 			boolean cancel = !ASMultiWorld.worldSwap(plugin, player, event.getFrom(), event.getTo());
 			if(cancel){
 				ASUtils.sendToPlayer(player, plugin.config().getString("messages.worldSwap", event.getTo().getWorld()));
				ASInventory.save(player, player.getGameMode(), event.getFrom().getWorld());
				ASInventory.load(player, player.getGameMode(), event.getTo().getWorld());
 				ASNotification.sendNotification(NotificationType.ILLEGAL_WORLD_CHANGE, plugin, player, event.getTo().getWorld().getName());
 				event.setCancelled(true);
 			}else{
				ASNotification.sendNotification(NotificationType.LEGAL_WORLD_CHANGE, plugin, player, event.getTo().getWorld().getName());
 			}
 		}
 	}
 
	@EventHandler (priority = EventPriority.LOWEST)
	public void onPlayerTeleport(PlayerTeleportEvent event){
		if(event.isCancelled()){
			return;
		}
		Player player = event.getPlayer();
		if(!event.getFrom().getWorld().equals(event.getTo().getWorld())){
			boolean cancel = !ASMultiWorld.worldSwap(plugin, player, event.getFrom(), event.getTo());
			if(cancel){
				ASUtils.sendToPlayer(player, plugin.config().getString("messages.worldSwap", event.getTo().getWorld()));
 				ASInventory.save(player, player.getGameMode(), event.getFrom().getWorld());
 				ASInventory.load(player, player.getGameMode(), event.getTo().getWorld());
				ASNotification.sendNotification(NotificationType.ILLEGAL_WORLD_CHANGE, plugin, player, event.getTo().getWorld().getName());
				event.setCancelled(true);
			}else{
 				ASNotification.sendNotification(NotificationType.LEGAL_WORLD_CHANGE, plugin, player, event.getTo().getWorld().getName());
 			}
		}
 	}
 }
