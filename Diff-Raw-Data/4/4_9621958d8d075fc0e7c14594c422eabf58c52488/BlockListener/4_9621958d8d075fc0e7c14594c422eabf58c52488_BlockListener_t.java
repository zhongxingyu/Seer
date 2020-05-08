 package me.arno.blocklog.listeners;
 
 import me.arno.blocklog.BlockLog;
 import me.arno.blocklog.Log;
 import me.arno.blocklog.logs.LoggedBlock;
 import me.ryanhamshire.GriefPrevention.Claim;
 import me.ryanhamshire.GriefPrevention.GriefPrevention;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.block.BlockFadeEvent;
 import org.bukkit.event.block.BlockFormEvent;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.BlockSpreadEvent;
 import org.bukkit.event.block.LeavesDecayEvent;
 import org.bukkit.event.entity.EntityCreatePortalEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.world.StructureGrowEvent;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 
 public class BlockListener extends BlockLogListener {
 	public BlockListener(BlockLog plugin) {
 		super(plugin);
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockPlace(BlockPlaceEvent event) {
 		BlockState block = event.getBlock().getState();
 		Player player = event.getPlayer();
 		
 		Boolean cancel = !isLoggingEnabled(player.getWorld());
 		
 		if(plugin.softDepends.containsKey("GriefPrevention")) {
 			GriefPrevention gp = (GriefPrevention) plugin.softDepends.get("GriefPrevention");
 			Claim claim = gp.dataStore.getClaimAt(block.getLocation(), false, null);
 			
 			if(claim != null)
 				cancel = claim.allowBuild(player) != null;
 		}
 		
 		if(plugin.softDepends.containsKey("WorldGuard")) {
 			WorldGuardPlugin wg = (WorldGuardPlugin) plugin.softDepends.get("WorldGuard");
 			cancel = !wg.canBuild(player, block.getLocation());
 		}
 		
 		int BLWand = getConfig().getInt("blocklog.wand");
 		boolean WandEnabled = plugin.users.contains(event.getPlayer().getName());
 		
 		if(event.getPlayer().getItemInHand().getTypeId() == BLWand && WandEnabled)
 			cancel = true;
 		
 		if(!event.isCancelled() && !cancel) {
 			plugin.addBlock(new LoggedBlock(plugin, player, block, Log.PLACE));
 			BlocksLimitReached();
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockBreak(BlockBreakEvent event) {
 		BlockState block = event.getBlock().getState();
 		Player player = event.getPlayer();
 		
 		Boolean cancel = !isLoggingEnabled(player.getWorld());
 		
 		if(plugin.softDepends.containsKey("GriefPrevention")) {
 			GriefPrevention gp = (GriefPrevention) plugin.softDepends.get("GriefPrevention");
 			Claim claim = gp.dataStore.getClaimAt(block.getLocation(), false, null);
 			
 			if(claim != null)
 				cancel = claim.allowBuild(player) != null;
 		}
 		
 		if(plugin.softDepends.containsKey("WorldGuard")) {
 			WorldGuardPlugin wg = (WorldGuardPlugin) plugin.softDepends.get("WorldGuard");
 			cancel = !wg.canBuild(player, block.getLocation());
 		}
 		
 		if(!event.isCancelled() && !cancel) {
 			plugin.addBlock(new LoggedBlock(plugin, player, block, Log.BREAK));
 			BlocksLimitReached();
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
 		BlockState block = event.getBlockClicked().getRelative(event.getBlockFace()).getState();
 		Player player = event.getPlayer();
 		
 		Boolean cancel = !isLoggingEnabled(player.getWorld());
 		
 		if(plugin.softDepends.containsKey("GriefPrevention")) {
 			GriefPrevention gp = (GriefPrevention) plugin.softDepends.get("GriefPrevention");
 			Claim claim = gp.dataStore.getClaimAt(block.getLocation(), false, null);
 			
 			if(claim != null)
 				cancel = claim.allowBuild(player) != null;
 		}
 		
 		if(plugin.softDepends.containsKey("WorldGuard")) {
 			WorldGuardPlugin wg = (WorldGuardPlugin) plugin.softDepends.get("WorldGuard");
 			cancel = !wg.canBuild(player, block.getLocation());
 		}
 		
 		if(!event.isCancelled() && !cancel) {
 			if(event.getBucket() == Material.WATER_BUCKET)
 				block.setType(Material.WATER);
 			else if(event.getBucket() == Material.LAVA_BUCKET)
 				block.setType(Material.LAVA);
 			
 			plugin.addBlock(new LoggedBlock(plugin, player, block, Log.PLACE));
 			BlocksLimitReached();
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockBurn(BlockBurnEvent event) {
 		if(!event.isCancelled() && isLoggingEnabled(event.getBlock().getWorld())) {
 			plugin.addBlock(new LoggedBlock(plugin, event.getBlock().getState(), Log.FIRE));
 			BlocksLimitReached();
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockIgnite(BlockIgniteEvent event) {
		if(!event.isCancelled() && event.getPlayer() != null) {
			if(event.getBlock().getType() == Material.TNT && isLoggingEnabled(event.getPlayer().getWorld())) {
 				plugin.addBlock(new LoggedBlock(plugin, event.getPlayer(), event.getBlock().getState(), Log.BREAK));
 				BlocksLimitReached();
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onEntityExplode(EntityExplodeEvent event) {
 		if(!event.isCancelled() && isLoggingEnabled(event.getEntity().getWorld())) {
 			Log log = Log.EXPLOSION;
 			Player target = null;
 			if(event.getEntityType() != null) {
 				if(event.getEntityType() == EntityType.CREEPER) {
 					log = Log.EXPLOSION_CREEPER;
 					Creeper creeper = (Creeper) event.getEntity();
 					if(creeper.getTarget() instanceof Player)
 						target = (Player) creeper.getTarget();
 				} else if(event.getEntityType() == EntityType.GHAST || event.getEntityType() == EntityType.FIREBALL) {
 					log = Log.EXPLOSION_GHAST;
 				} else if(event.getEntityType() == EntityType.PRIMED_TNT) {
 					log = Log.EXPLOSION_TNT;
 				}
 			}
 			
 			for(Block block : event.blockList()) {
 				if(target == null)
 					plugin.addBlock(new LoggedBlock(plugin, block.getState(), event.getEntityType(), log));
 				else
 					plugin.addBlock(new LoggedBlock(plugin, target, block.getState(), event.getEntityType(), log));
 				BlocksLimitReached();
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onLeavesDecay(LeavesDecayEvent event) {
 		if(!event.isCancelled() && isLoggingEnabled(event.getBlock().getWorld())) {
 			if(getConfig().getBoolean("logs.leaves")) {
 				plugin.addBlock(new LoggedBlock(plugin, event.getBlock().getState(), Log.LEAVES));
 				BlocksLimitReached();
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onStructureGrow(StructureGrowEvent event) {
 		if(!event.isCancelled() && isLoggingEnabled(event.getPlayer().getWorld())) {
 			if(getConfig().getBoolean("logs.grow")) {
 				Player player = event.getPlayer();
 				for(BlockState block : event.getBlocks()) {
 					plugin.addBlock(new LoggedBlock(plugin, player, block, Log.GROW));
 					BlocksLimitReached();
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onEntityCreatePortal(EntityCreatePortalEvent event) {
 		if(!event.isCancelled() && isLoggingEnabled(event.getEntity().getWorld())) {
 			if(getConfig().getBoolean("logs.portal")) {
 				Player player = (Player) event.getEntity();
 				for(BlockState block : event.getBlocks()) {
 					plugin.addBlock(new LoggedBlock(plugin, player, block, Log.PORTAL));
 					BlocksLimitReached();
 				}
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockForm(BlockFormEvent event) {
 		if(!event.isCancelled() && isLoggingEnabled(event.getBlock().getWorld())) {
 			if(getConfig().getBoolean("logs.form")) {
 				plugin.addBlock(new LoggedBlock(plugin, event.getNewState(), Log.FORM));
 				BlocksLimitReached();
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockSpread(BlockSpreadEvent event) {
 		if(!event.isCancelled() && isLoggingEnabled(event.getBlock().getWorld())) {
 			if(getConfig().getBoolean("logs.spread")) {
 				plugin.addBlock(new LoggedBlock(plugin, event.getNewState(), Log.SPREAD));
 				BlocksLimitReached();
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBlockFade(BlockFadeEvent event) {
 		if(!event.isCancelled() && isLoggingEnabled(event.getBlock().getWorld())) {
 			if(getConfig().getBoolean("logs.fade")) {
 				plugin.addBlock(new LoggedBlock(plugin, event.getNewState(), Log.FADE));
 				BlocksLimitReached();
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		if(!event.isCancelled() && isLoggingEnabled(event.getPlayer().getWorld())) {
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
 				plugin.addBlock(new LoggedBlock(plugin, event.getPlayer(), block.getState(), Log.BREAK));
 				BlocksLimitReached();
 			}
 		}
 	}
 }
