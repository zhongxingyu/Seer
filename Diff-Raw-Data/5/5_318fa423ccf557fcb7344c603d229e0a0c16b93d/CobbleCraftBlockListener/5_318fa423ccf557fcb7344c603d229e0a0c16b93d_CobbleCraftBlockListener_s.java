 package com.bukkitfiller.CobbleCraft;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 
import com.bukkitfiller.CobbleCraft.CobbleCraftFileHandler.Types;

 public class CobbleCraftBlockListener extends BlockListener {
 	private CobbleCraft plugin;	
 	
 	CobbleCraftBlockListener(CobbleCraft plugin) {
 		this.plugin = plugin;
 	}
 	
 	@Override
 	public void onBlockBreak(BlockBreakEvent event) {
 		Player player = event.getPlayer();
 		String playerName = player.getName();
 		Block block = event.getBlock();
 		String fileName = plugin.FILEDIRECTORY + playerName + ".stats";
 		Material playersItem = player.getItemInHand().getType();
 		
 		if (playersItem == Material.WOOD_PICKAXE || playersItem == Material.STONE_PICKAXE || playersItem == Material.IRON_PICKAXE || playersItem == Material.GOLD_PICKAXE || playersItem == Material.DIAMOND_PICKAXE){
 			
 		hitMiningBlock(player, block, playersItem, fileName);
 		plugin.lvlValues.CheckLevelUp(fileName, player, plugin.lvlValues.DiggingLevels, "Mining");
 		
 		}else if (playersItem == Material.WOOD_SPADE || playersItem == Material.STONE_SPADE || playersItem == Material.IRON_SPADE || playersItem == Material.GOLD_SPADE || playersItem == Material.DIAMOND_SPADE){
 			
 		hitDiggingBlock(player, block, playersItem, fileName);
 		plugin.lvlValues.CheckLevelUp(fileName, player, plugin.lvlValues.DiggingLevels, "Digging");
 		
 		}
 		
 	}
 	
 	public void hitMiningBlock(Player player, Block block, Material playersItem, String fileName){
 		double miningMod = plugin.fileHandler.getLevel(fileName, "MINING") / 18;
 		
 		if (block.getType() == Material.COBBLESTONE){
 			if (playersItem == Material.WOOD_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.1 + miningMod);
 			}
 			if (playersItem == Material.STONE_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.2 + miningMod);
 			}
 			if (playersItem == Material.IRON_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.3 + miningMod);
 			}
 			if (playersItem == Material.GOLD_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.4 + miningMod);
 			}
 			if (playersItem == Material.DIAMOND_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.5 + miningMod);
 			}
 		}
 		
 		if (block.getType() == Material.STONE){
 			if (playersItem == Material.WOOD_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.2 + miningMod);
 			}
 			if (playersItem == Material.STONE_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.3 + miningMod);
 			}
 			if (playersItem == Material.IRON_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.4 + miningMod);
 			}
 			if (playersItem == Material.GOLD_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.5 + miningMod);
 			}
 			if (playersItem == Material.DIAMOND_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.6 + miningMod);
 			}
 		}
 		
 		if (block.getType() == Material.COAL_ORE){
 			if (playersItem == Material.WOOD_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.28 + miningMod);
 			}
 			if (playersItem == Material.STONE_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.38 + miningMod);
 			}
 			if (playersItem == Material.IRON_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.48 + miningMod);
 			}
 			if (playersItem == Material.GOLD_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.58 + miningMod);
 			}
 			if (playersItem == Material.DIAMOND_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.68 + miningMod);
 			}
 		}
 		
 		if (block.getType() == Material.IRON_ORE){
 			if (playersItem == Material.WOOD_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.34 + miningMod);
 			}
 			if (playersItem == Material.STONE_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.44 + miningMod);
 			}
 			if (playersItem == Material.IRON_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.54 + miningMod);
 			}
 			if (playersItem == Material.GOLD_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.64 + miningMod);
 			}
 			if (playersItem == Material.DIAMOND_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.74 + miningMod);
 			}
 		}
 		
 		if (block.getType() == Material.GOLD_ORE){
 			
 			//Gold Digga Achievement (5 Gold Ore).
			plugin.fileHandler.writeNumProperty(fileName, Types.Gold_Digga.get(), 1);
 			if(plugin.fileHandler.getNumProperty(fileName, "GOLD_DIGGA") == 5){
 				plugin.broadcastAchievement(player, "GOLD DIGGA");
 				plugin.fileHandler.getAchievements(fileName, player);
 			}
 			
 			if (playersItem == Material.WOOD_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.39 + miningMod);
 			}
 			if (playersItem == Material.STONE_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.49 + miningMod);
 			}
 			if (playersItem == Material.IRON_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.59 + miningMod);
 			}
 			if (playersItem == Material.GOLD_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.69 + miningMod);
 			}
 			if (playersItem == Material.DIAMOND_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.79 + miningMod);
 			}
 		}
 		
 		if (block.getType() == Material.REDSTONE_ORE){
 			if (playersItem == Material.WOOD_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.52 + miningMod);
 			}
 			if (playersItem == Material.STONE_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.62 + miningMod);
 			}
 			if (playersItem == Material.IRON_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.72 + miningMod);
 			}
 			if (playersItem == Material.GOLD_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.82 + miningMod);
 			}
 			if (playersItem == Material.DIAMOND_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.92 + miningMod);
 			}
 		}
 		
 		if (block.getType() == Material.DIAMOND_ORE){
 			if (playersItem == Material.WOOD_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.60 + miningMod);
 			}
 			if (playersItem == Material.STONE_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.70 + miningMod);
 			}
 			if (playersItem == Material.IRON_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.80 + miningMod);
 			}
 			if (playersItem == Material.GOLD_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 0.90 + miningMod);
 			}
 			if (playersItem == Material.DIAMOND_PICKAXE){
 				plugin.fileHandler.editNumProperty(fileName, "MINING", 1.00 + miningMod);
 			}
 		}
 		
 	}
 	
 	public void hitDiggingBlock(Player player, Block block, Material playersItem, String fileName){
 		
 		if (block.getType() == Material.DIRT){
 			if (playersItem == Material.WOOD_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.10);
 			}
 			if (playersItem == Material.STONE_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.15);
 			}
 			if (playersItem == Material.IRON_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.20);
 			}
 			if (playersItem == Material.GOLD_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.25);
 			}
 			if (playersItem == Material.DIAMOND_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.30);
 			}
 		}
 		
 		if (block.getType() == Material.GRASS){
 			if (playersItem == Material.WOOD_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.10);
 			}
 			if (playersItem == Material.STONE_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.15);
 			}
 			if (playersItem == Material.IRON_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.20);
 			}
 			if (playersItem == Material.GOLD_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.25);
 			}
 			if (playersItem == Material.DIAMOND_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.30);
 			}
 		}
 		
 		if (block.getType() == Material.SAND){
 			if (playersItem == Material.WOOD_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.12);
 			}
 			if (playersItem == Material.STONE_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.17);
 			}
 			if (playersItem == Material.IRON_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.22);
 			}
 			if (playersItem == Material.GOLD_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.27);
 			}
 			if (playersItem == Material.DIAMOND_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.32);
 			}
 		}
 		
 		if (block.getType() == Material.GRAVEL){
 			if (playersItem == Material.WOOD_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.17);
 			}
 			if (playersItem == Material.STONE_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.22);
 			}
 			if (playersItem == Material.IRON_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.27);
 			}
 			if (playersItem == Material.GOLD_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.32);
 			}
 			if (playersItem == Material.DIAMOND_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.37);
 			}
 		}
 		
 		if (block.getType() == Material.CLAY){
 			if (playersItem == Material.WOOD_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.24);
 			}
 			if (playersItem == Material.STONE_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.29);
 			}
 			if (playersItem == Material.IRON_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.34);
 			}
 			if (playersItem == Material.GOLD_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.39);
 			}
 			if (playersItem == Material.DIAMOND_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.44);
 			}
 		}
 		
 		if (block.getType() == Material.GLOWSTONE){
 			if (playersItem == Material.WOOD_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.17);
 			}
 			if (playersItem == Material.STONE_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.22);
 			}
 			if (playersItem == Material.IRON_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.27);
 			}
 			if (playersItem == Material.GOLD_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.32);
 			}
 			if (playersItem == Material.DIAMOND_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.37);
 			}
 		}
 		
 		if (block.getType() == Material.NETHERRACK){
 			if (playersItem == Material.WOOD_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.19);
 			}
 			if (playersItem == Material.STONE_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.24);
 			}
 			if (playersItem == Material.IRON_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.29);
 			}
 			if (playersItem == Material.GOLD_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.34);
 			}
 			if (playersItem == Material.DIAMOND_SPADE){
 				plugin.fileHandler.editNumProperty(fileName, "DIGGING", 0.39);
 			}
 		}
 		
 	}
 	
 }
