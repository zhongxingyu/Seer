 package com.github.namrufus.harvest_time.crop_growth.chance_growth;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockGrowEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 import org.bukkit.event.player.PlayerFishEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.world.StructureGrowEvent;
 
 import com.github.namrufus.harvest_time.crop_growth.crop_directory.TreeGrowthType;
 import com.github.namrufus.harvest_time.plugin.InteractionConfiguration;
 
 // the purpose of this class is to prevent the growth of "chance" type crops with a frequency dependent on that
 // crop's environment and configuration.
 
 public class ChanceGrowthListener implements Listener {
 	ChanceCropListConfiguration chanceCropList;
 	InteractionConfiguration interactionConfiguration;
 	Logger log;
 	
 	public ChanceGrowthListener(InteractionConfiguration interactionConfiguration,
 			                    ChanceCropListConfiguration chanceCropList,
 			                    Logger log) {
 		this.interactionConfiguration = interactionConfiguration;
 		this.chanceCropList = chanceCropList;
 		this.log = log;
 	}
 	
 	@EventHandler
 	public void onBlockGrowthEvent(BlockGrowEvent event) {
 		Block block = event.getBlock();
		Material blockMaterial = event.getNewState().getType();
 		
 		// do nothing if the crop is not a chance growth type crop
 		if (!chanceCropList.containsBlockCrop(blockMaterial))
 			return;
 		
 		CropChanceGrowthConfiguration cropConfiguration = chanceCropList.getBlockCrop(blockMaterial);
 		
 		if (!cropConfiguration.growthSucceeds(block)) {
 			event.setCancelled(true);
 			log.finest("crop chance growth event canceled: " + blockMaterial + " " + block.getLocation());
 		}
 	}
 	
 	@EventHandler
 	public void onStructureGrowEvent(StructureGrowEvent event) {
 		TreeGrowthType treeGrowthType = TreeGrowthType.getTreeGrowthType(event.getSpecies());
 		
 		// if the crop is not supported, do nothing
 		if (!TreeGrowthType.hasTreeGrowthType(event.getSpecies())) {
 			// warn later if mojang changes tree types and I forget to add them
 			log.warning("tree type not identified: " + event.getSpecies() + " plugin probably was not updated properly to a new version, contact plugin maintainer.");
 			return;
 		}
 		
 		// do nothing if the crop is not a chance growth type crop
 		if (!chanceCropList.containsTreeCrop(treeGrowthType))
 			return;
 		
 		CropChanceGrowthConfiguration cropConfiguration = chanceCropList.getTreeCrop(treeGrowthType);
 		
 		if (!cropConfiguration.growthSucceeds(event.getLocation().getBlock())) {
 			event.setCancelled(true);
 			log.finest("crop chance growth event canceled: " + treeGrowthType + " " + event.getLocation());
 		}
 	}
 	
 	@EventHandler
 	public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
 		if (event.getSpawnReason() == SpawnReason.EGG) {
 			// do nothing if the egg_hatch configuration is not specified
 			if (!chanceCropList.containsEggHatchingCrop())
 				return;
 			
 			CropChanceGrowthConfiguration cropConfiguration = chanceCropList.getEggHatchingCrop();
 			
 			if (!cropConfiguration.growthSucceeds(event.getLocation().getBlock())) {
 				event.setCancelled(true);
 				log.finest("crop chance growth event canceled: " + "egg hatching" + " " + event.getLocation());
 			}
 		} else if (event.getSpawnReason() == SpawnReason.BREEDING) {
 			EntityType entityType = event.getEntityType();
 			
 			// do nothing if the crop is not a chance growth type crop
 			if (!chanceCropList.containsBreedingCrop(entityType))
 				return;
 			
 			CropChanceGrowthConfiguration cropConfiguration = chanceCropList.getBreedingCrop(entityType);
 			
 			if (!cropConfiguration.growthSucceeds(event.getLocation().getBlock())) {
 				event.setCancelled(true);
 				log.finest("crop chance growth event canceled: " + entityType + " " + event.getLocation());
 			}
 		}
 	}
 	
 	@EventHandler
 	public void onFishingEvent(PlayerFishEvent event) {
 		// if nothing has been caught, do nothing
 		if (!(event.getState() == PlayerFishEvent.State.CAUGHT_FISH || event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY))
 			return;
 		
 		// do nothing if the fishing configuration is not specified
 		if (!chanceCropList.containsFishingCrop())
 			return;
 		
 		CropChanceGrowthConfiguration cropConfiguration = chanceCropList.getFishingCrop();
 
 		if (!cropConfiguration.growthSucceeds(event.getCaught().getLocation().getBlock())) {
 			event.setCancelled(true);
 			log.finest("crop chance growth event canceled: " + "fishing" + " " + event.getCaught().getLocation());
 		}
 	}
 	
 	// ================================================================================================================
 	@EventHandler
 	public void onPlayerInteractEvent(PlayerInteractEvent event) {
 		Block block = event.getClickedBlock();
 		Material toolMaterial = event.getMaterial();
 		
 		// the player must have the growth check material in hand
 		if (toolMaterial == null/*nothing in hand*/)
 			return;
 		if (toolMaterial != interactionConfiguration.getCropGrowthCheckMaterial())
 			return;
 		
 		Player player = event.getPlayer();
 		
 		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 			Material blockMaterial = block.getType();
 			// non-exclusive if because a block's type might represent different crop types (mushrooms - tree and block)
 			
 			// block type crops
 			if (chanceCropList.containsBlockCrop(blockMaterial)) {
 				player.sendMessage("7[Harvest Time] Crop: 8block_"+blockMaterial.toString());
 				chanceCropList.getBlockCrop(blockMaterial).displayState(player, block);
 			}
 			
 			// tree type crops
 			TreeGrowthType treeGrowthType = TreeGrowthType.getTreeGrowthType(block);
 			if (treeGrowthType != null && chanceCropList.containsTreeCrop(treeGrowthType)) {
 				player.sendMessage("7[Harvest Time] Crop: 8tree_" + treeGrowthType);
 				chanceCropList.getTreeCrop(treeGrowthType).displayState(player, block);
 			}
 			
 			// fishing type crop
 			if (blockMaterial == Material.STATIONARY_WATER && chanceCropList.containsFishingCrop()) {
 				player.sendMessage("7[Harvest Time] Crop: 8fishing");
 				chanceCropList.getFishingCrop().displayState(player, block);
 			}
 		}
 	}
 	
 	// ================================================================================================================
 	@EventHandler
 	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
 		Player player = event.getPlayer();
 		
 		if (player.getItemInHand() == null)
 			return;
 		if (player.getItemInHand().getType() != interactionConfiguration.getCropGrowthCheckMaterial())
 			return;
 		
 		Entity entity = event.getRightClicked();
 		EntityType entityType = entity.getType();
 		
 		Block block = entity.getLocation().getBlock();
 		
 		if (chanceCropList.containsBreedingCrop(entityType)) {
 			player.sendMessage("7[Harvest Time] Crop: 8breeding_" + entityType);
 			chanceCropList.getBreedingCrop(entityType).displayState(player, block);
 		}
 		
 		if (entityType == EntityType.CHICKEN && chanceCropList.containsEggHatchingCrop()) {
 			player.sendMessage("7[Harvest Time] Crop: 8" + "egg hatching");
 			chanceCropList.getEggHatchingCrop().displayState(player, block);
 		}
 	}
 }
