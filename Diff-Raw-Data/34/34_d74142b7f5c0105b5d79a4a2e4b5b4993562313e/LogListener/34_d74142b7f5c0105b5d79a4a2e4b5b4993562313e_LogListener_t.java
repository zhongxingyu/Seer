 package me.arno.blocklog.listeners;
 
 import java.util.logging.Logger;
 
 import me.arno.blocklog.BlockLog;
 import me.arno.blocklog.Config;
 import me.arno.blocklog.Interaction;
 import me.arno.blocklog.Log;
 import me.arno.blocklog.logs.InteractedBlock;
 import me.arno.blocklog.logs.LoggedBlock;
 import me.ryanhamshire.GriefPrevention.Claim;
 import me.ryanhamshire.GriefPrevention.GriefPrevention;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.block.LeavesDecayEvent;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.world.StructureGrowEvent;
 import org.bukkit.event.entity.EntityCreatePortalEvent;
 import org.bukkit.event.block.BlockFormEvent;
 import org.bukkit.event.block.BlockSpreadEvent;
 
 public class LogListener implements Listener {
 	BlockLog plugin;
 	Logger log;
 	Config cfg;
 	float time;
 	
 	public LogListener(BlockLog plugin) {
 		this.plugin = plugin;
 		this.log = plugin.log;
 		this.cfg = plugin.cfg;
 	}
 	
 	public void sendAdminMessage(String msg) {
 		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
 	    	if (player.isOp() || player.hasPermission("blocklog.notices")) {
 	    		player.sendMessage(msg);
 	        }
 	    }
 	}
 	
 	public void BlocksLimitReached() {
 		int BlockSize = plugin.blocks.size();
 		int WarningBlockSize = cfg.getConfig().getInt("blocklog.warning.blocks");
 		int WarningDelay = cfg.getConfig().getInt("blocklog.warning.delay") * 1000;
 		int WarningRepeat = cfg.getConfig().getInt("blocklog.warning.repeat");
 		
 		if(BlockSize == plugin.autoSave && BlockSize != 0 && plugin.autoSave != 0) {
 			plugin.saveLogs(0);
 		} else if(plugin.autoSave == 0 && (BlockSize ==  WarningBlockSize || (BlockSize > WarningBlockSize && (BlockSize % WarningRepeat == 0)))) {
 			if(time < System.currentTimeMillis()) {
 				time = System.currentTimeMillis() +  WarningDelay;
 				sendAdminMessage(ChatColor.DARK_RED + "[BlockLog] " + ChatColor.GOLD + "BlockLog reached an internal storage of " + BlockSize + "!");
 				sendAdminMessage(ChatColor.DARK_RED + "[BlockLog] " + ChatColor.GOLD + "If you want to save all these blocks use " + ChatColor.DARK_BLUE + "/blfullsave" + ChatColor.GOLD + " or " + ChatColor.DARK_BLUE + "/blsave <blocks>");
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockPlace(BlockPlaceEvent event) {
 		BlockState block = event.getBlock().getState();
 		Player player = event.getPlayer();
 		
 		if(plugin.softDepends.containsKey("GriefPrevention")) {
 			plugin.softDepends.get("GriefPrevention");
 			GriefPrevention gp = (GriefPrevention) plugin.softDepends.get("GriefPrevention");
 			Claim claim = gp.dataStore.getClaimAt(block.getLocation(), false, null);
 			
 			if(claim != null)
 				event.setCancelled(claim.allowBuild(player) != null);
 		}
 		
 		if(!event.isCancelled()) {
 			int BLWand = cfg.getConfig().getInt("blocklog.wand");
 			boolean WandEnabled = plugin.users.contains(event.getPlayer().getName());
 			
 			if(event.getPlayer().getItemInHand().getTypeId() != BLWand || !WandEnabled) {
 				plugin.blocks.add(new LoggedBlock(plugin, player, block, Log.PLACE));
 				BlocksLimitReached();
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockBreak(BlockBreakEvent event) {
 		BlockState block = event.getBlock().getState();
 		Player player = event.getPlayer();
 		
 		if(plugin.softDepends.containsKey("GriefPrevention")) {
 			plugin.softDepends.get("GriefPrevention");
 			GriefPrevention gp = (GriefPrevention) plugin.softDepends.get("GriefPrevention");
 			Claim claim = gp.dataStore.getClaimAt(block.getLocation(), false, null);
 			
 			if(claim != null)
 				event.setCancelled(claim.allowBreak(player, block.getType()) != null);
 		}
 		
 		if(!event.isCancelled()) {
 			plugin.blocks.add(new LoggedBlock(plugin, player, block, Log.BREAK));
 			BlocksLimitReached();
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
 		BlockState block = event.getBlockClicked().getRelative(event.getBlockFace()).getState();
 		Player player = event.getPlayer();
 		
 		if(plugin.softDepends.containsKey("GriefPrevention")) {
 			plugin.softDepends.get("GriefPrevention");
 			GriefPrevention gp = (GriefPrevention) plugin.softDepends.get("GriefPrevention");
 			Claim claim = gp.dataStore.getClaimAt(block.getLocation(), false, null);
 			
 			if(claim != null)
 				event.setCancelled(claim.allowBuild(player) != null);
 		}
 		
 		if(!event.isCancelled()) {
 			if(event.getBucket() == Material.WATER_BUCKET)
 				block.setType(Material.WATER);
 			else if(event.getBucket() == Material.LAVA_BUCKET)
 				block.setType(Material.LAVA);
 			
 			plugin.blocks.add(new LoggedBlock(plugin, player, block, Log.PLACE));
 			BlocksLimitReached();
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockBurn(BlockBurnEvent event) {
 		if(!event.isCancelled()) {
 			plugin.blocks.add(new LoggedBlock(plugin, event.getBlock().getState(), Log.FIRE));
 			BlocksLimitReached();
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockIgnite(BlockIgniteEvent event) {
 		if(!event.isCancelled()) {
 			if(event.getBlock().getType() == Material.TNT) {
 				plugin.blocks.add(new LoggedBlock(plugin, event.getPlayer(), event.getBlock().getState(), Log.BREAK));
 				BlocksLimitReached();
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onEntityExplode(EntityExplodeEvent event) {
 		for(Block block : event.blockList()) {
 			plugin.blocks.add(new LoggedBlock(plugin, block.getState(), Log.EXPLOSION));
 			BlocksLimitReached();
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onLeavesDecay(LeavesDecayEvent event) {
 		if(!event.isCancelled()) {
 			if(cfg.getConfig().getBoolean("logs.leaves")) {
 				plugin.blocks.add(new LoggedBlock(plugin, event.getBlock().getState(), Log.LEAVES));
 				BlocksLimitReached();
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onStructureGrow(StructureGrowEvent event) {
 		if(!event.isCancelled()) {
 			if(cfg.getConfig().getBoolean("logs.grow")) {
 				Player player = event.getPlayer();
 				for(BlockState block : event.getBlocks()) {
 					plugin.blocks.add(new LoggedBlock(plugin, player, block, Log.GROW));
 					BlocksLimitReached();
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onEntityCreatePortal(EntityCreatePortalEvent event) {
 		if(!event.isCancelled()) {
 			if(cfg.getConfig().getBoolean("logs.portal")) {
 				Player player = (Player) event.getEntity();
 				for(BlockState block : event.getBlocks()) {
 					plugin.blocks.add(new LoggedBlock(plugin, player, block, Log.PORTAL));
 					BlocksLimitReached();
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockForm(BlockFormEvent event) {
 		if(!event.isCancelled()) {
 			if(cfg.getConfig().getBoolean("logs.form")) {
 				plugin.blocks.add(new LoggedBlock(plugin, event.getNewState(), Log.FORM));
 				BlocksLimitReached();
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockSpread(BlockSpreadEvent event) {
 		if(!event.isCancelled()) {
 			if(cfg.getConfig().getBoolean("logs.spread")) {
 				plugin.blocks.add(new LoggedBlock(plugin, event.getNewState(), Log.SPREAD));
 				BlocksLimitReached();
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		if(!event.isCancelled()) {
 			int BLWand = cfg.getConfig().getInt("blocklog.wand");
 			boolean WandEnabled = plugin.users.contains(event.getPlayer().getName());
 			
 			if(event.getPlayer().getItemInHand().getTypeId() != BLWand || !WandEnabled) {
				if(event.getClickedBlock().getType() == Material.WOODEN_DOOR) {
 					InteractedBlock block = new InteractedBlock(plugin, event.getPlayer(), event.getClickedBlock().getLocation(), Interaction.DOOR);
 					block.push();
 					BlocksLimitReached();
 				} else if(event.getClickedBlock().getType() == Material.TRAP_DOOR) {
 					InteractedBlock block = new InteractedBlock(plugin, event.getPlayer(), event.getClickedBlock().getLocation(), Interaction.TRAP_DOOR);
 					block.push();
 					BlocksLimitReached();
 				} else if(event.getClickedBlock().getType() == Material.CHEST) {
 					InteractedBlock block = new InteractedBlock(plugin, event.getPlayer(), event.getClickedBlock().getLocation(), Interaction.CHEST);
 					block.push();
 					BlocksLimitReached();
 				} else if(event.getClickedBlock().getType() == Material.DISPENSER) {
 					InteractedBlock block = new InteractedBlock(plugin, event.getPlayer(), event.getClickedBlock().getLocation(), Interaction.DISPENSER);
 					block.push();
 					BlocksLimitReached();
 				} else if(event.getClickedBlock().getType() == Material.STONE_BUTTON) {
 					InteractedBlock block = new InteractedBlock(plugin, event.getPlayer(), event.getClickedBlock().getLocation(), Interaction.BUTTON);
 					block.push();
 					BlocksLimitReached();
 				} else if(event.getClickedBlock().getType() == Material.LEVER) {
 					InteractedBlock block = new InteractedBlock(plugin, event.getPlayer(), event.getClickedBlock().getLocation(), Interaction.LEVER);
 					block.push();
 					BlocksLimitReached();
				} else {
					Block block;
					block = event.getClickedBlock().getRelative(BlockFace.UP);
					if(block.getType() != Material.FIRE)
						block = event.getClickedBlock().getRelative(BlockFace.NORTH);
					if(block.getType() != Material.FIRE)
						block = event.getClickedBlock().getRelative(BlockFace.EAST);
					if(block.getType() != Material.FIRE)
						block = event.getClickedBlock().getRelative(BlockFace.SOUTH);
					if(block.getType() != Material.FIRE)
						block = event.getClickedBlock().getRelative(BlockFace.WEST);
					if(block.getType() == Material.FIRE) {
						plugin.blocks.add(new LoggedBlock(plugin, event.getPlayer(), block.getState(), Log.BREAK));
						BlocksLimitReached();
					}
 				}
 			}
 		}
 	}
 }
