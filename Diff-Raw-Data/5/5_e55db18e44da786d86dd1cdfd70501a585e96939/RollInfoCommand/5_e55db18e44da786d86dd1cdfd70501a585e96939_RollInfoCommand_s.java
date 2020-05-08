 package com.gildorymrp.gildorym;
 
 
 //import java.util.Iterator;
 
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 //import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 
 
 import com.gildorymrp.charactercards.GildorymCharacterCards;
 import com.gildorymrp.charactercards.Race;
 import com.gildorymrp.gildorymclasses.CharacterClass;
 import com.gildorymrp.gildorymclasses.GildorymClasses;
 
 
 public class RollInfoCommand implements CommandExecutor {
 
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		Player player;
 		if (args.length == 0) {
 			player = (Player) sender;
 		} else {
 			player = Bukkit.getServer().getPlayer(args[0]);
 		}
 
 
 		//Heroes heroes = (Heroes) Bukkit.getServer().getPluginManager().getPlugin("Heroes");
 		GildorymClasses gildorymClasses = (GildorymClasses) Bukkit.getServer().getPluginManager().getPlugin("GildorymClasses");
 		GildorymCharacterCards gildorymCharacterCards = (GildorymCharacterCards) Bukkit.getServer().getPluginManager().getPlugin("GildorymCharacterCards");
 
 
 		if (player != null) {
 			//HeroClass heroClass = heroes.getCharacterManager().getHero(player).getHeroClass();
 			//Integer heroLevel = Integer.valueOf(heroes.getCharacterManager().getHero(player).getLevel(heroClass));
 			CharacterClass characterClass = gildorymClasses.classes.get(player.getName());
 			int level = gildorymClasses.levels.get(player.getName());
 
 
 			double meleeAttack = 0.0D;
 			double meleeDefence = 0.0D;
 			double rangedAttack = 0.0D;
 			double rangedDefence = 0.0D;
 			double magicAttack = 0.0D;
 			double magicDefence = 0.0D;
 			double luck = 0.0D;
 			double reflex = 0.0D;
 			int	BAB_good = 0;
 			int BAB_avg = 0;
 			int BAB_poor = 0;
 		
 			if (level <= 20) {
 				BAB_good = level;
 				luck += (level + 2);
 				reflex += (level + 2);
 			} else {
 				BAB_good = 20;
 				luck += (level + 2);
 				reflex += (20 + 2);
 			}
 			if (level == 1) {
 				BAB_avg = 1; 
 				BAB_poor = 1;
 			} else if (level == 2) {
 				BAB_avg = 1; 
 				BAB_poor = 1;
 			} else if (level == 3) {
 				BAB_avg = 2; 
 				BAB_poor = 1;
			} else if ({level == 4) {
 				BAB_avg = 3; 
 				BAB_poor = 2;
 			} else if (level == 5) {
 				BAB_avg = 3; 
 				BAB_poor = 2;
 			} else if (level == 6) {
 				BAB_avg = 4; 
 				BAB_poor = 3;
 			} else if (level == 7) {
 				BAB_avg = 5; 
 				BAB_poor = 3;
 			} else if (level == 8) {
 				BAB_avg = 6; 
 				BAB_poor = 4;
 			} else if (level == 9) {
 				BAB_avg = 6; 
 				BAB_poor = 4;
 			} else if (level == 10) {
 				BAB_avg = 7; 
 				BAB_poor = 5;
 			} else if (level == 11) {
 				BAB_avg = 8; 
 				BAB_poor = 5;
 			} else if (level == 12) {
 				BAB_avg = 9; 
 				BAB_poor = 6;
 			} else if (level == 13) {
 				BAB_avg = 9; 
 				BAB_poor = 6;
 			} else if (level == 14) {
 				BAB_avg = 10; 
 				BAB_poor = 7;
 			} else if (level == 15) {
 				BAB_avg = 11; 
 				BAB_poor = 7;
 			} else if (level == 16) {
 				BAB_avg = 12; 
 				BAB_poor = 8;
 			} else if (level == 17) {
 				BAB_avg = 12; 
 				BAB_poor = 8;
 			} else if (level == 18) {
 				BAB_avg = 13; 
 				BAB_poor = 9;
 			} else if (level == 19) {
 				BAB_avg = 14; 
 				BAB_poor = 9;
 			} else if (level == 20) {
 				BAB_avg = 15; 
 				BAB_poor = 10;
 			} else {
 				BAB_avg = 15; 
 				BAB_poor = 10;
 			}
 			
 			//meleeAttack += level;
 			//meleeDefence += level;
 			//rangedAttack += level;
 			//rangedDefence += level;
 			//magicAttack += level;
 			//magicDefence += level;
 		
 			if (characterClass == CharacterClass.BARBARIAN) {
 				meleeAttack += (5 + BAB_good);
 				meleeDefence += (3 + BAB_good);
 				rangedAttack += (5 + BAB_avg);
 				rangedDefence += (3 + BAB_avg);
 				magicAttack = 0.0D;
 				magicDefence += (1 + BAB_poor);
 			}
 
 
 			if (characterClass == CharacterClass.BARD) {
 				meleeAttack += (3 + BAB_avg);
 				meleeDefence += (1 + BAB_avg);
 				rangedAttack += (3 + BAB_avg);
 				rangedDefence += (1 + BAB_avg);
 				magicAttack += (2 + BAB_avg);
 				magicDefence += (1 + BAB_avg);
 				reflex += (0.25 * level);
 			}
 
 
 			if (characterClass == CharacterClass.CLERIC) {
 				meleeAttack += (3 + BAB_avg);
 				meleeDefence += (2 + BAB_good);
 				rangedAttack += (1 + BAB_poor);
 				rangedDefence += (1 + BAB_avg);
 				magicAttack += (4 + BAB_good);
 				magicDefence += (3 + BAB_good);
 			}
 
 
 			if (characterClass == CharacterClass.DRUID) {
 				meleeAttack += (3 + BAB_avg);
 				meleeDefence += (2 + BAB_avg);
 				rangedAttack += (1 + BAB_avg);
 				rangedDefence += (1 + BAB_avg);
 				magicAttack += (4 + BAB_avg);
 				magicDefence += (3 + BAB_avg);
 			}
 
 
 			if (characterClass == CharacterClass.FIGHTER) {
 				meleeAttack += (5 + BAB_good);
 				meleeDefence += (5 + BAB_good);
 				rangedAttack += (3 + BAB_avg);
 				rangedDefence += (3 + BAB_good);
 				magicAttack = 0.0D;
 				magicDefence += (1 + BAB_good);
 			}
 
 
 			if (characterClass == CharacterClass.MONK) {
 				meleeAttack += (5 + BAB_good);
 				meleeDefence += (6 + BAB_good);
 				rangedAttack += (3 + BAB_avg);
 				rangedDefence += (3 + BAB_good);
 				magicAttack = 0.0D;
				magicDefense += (1 + BAB_poor);
 				reflex += (0.5 * level);
 				if ((player.getInventory().getBoots() == null) && (player.getInventory().getLeggings() == null) && (player.getInventory().getChestplate() == null) && (player.getInventory().getHelmet() == null)) {
 					meleeDefence += (0.25 * level);
 					rangedDefence += (0.25 * level);
 				}
 			}
 
 
 			if (characterClass == CharacterClass.PALADIN) {
 				meleeAttack += (4 + BAB_avg);
 				meleeDefence += (4 + BAB_good);
 				rangedAttack += (2 + BAB_avg);
 				rangedDefence += (2 + BAB_good);
 				magicAttack += (3 + BAB_avg);
 				magicDefence += (3 + BAB_avg);
 			}
 
 
 			if (characterClass == CharacterClass.RANGER) {
 				meleeAttack += (2 + BAB_avg);
 				meleeDefence += (2 + BAB_avg);
 				rangedAttack += (4 + BAB_good);
 				rangedDefence += (2 + BAB_avg);
 				magicAttack += (1 + BAB_avg);
 				magicDefence += (2 + BAB_avg);
 				reflex += (0.25 * level);
 			}
 
 
 			if (characterClass == CharacterClass.ROGUE) {
 				meleeAttack += (5 + BAB_good);
 				meleeDefence += (4 + BAB_avg);
 				rangedAttack += (3 + BAB_good);
 				rangedDefence += (3 + BAB_avg);
 				magicAttack = 0.0D;
 				magicDefence += (2 + BAB_avg);
 				reflex += (0.5 * level);
 			}
 
 
 			if (characterClass == CharacterClass.SORCERER) {
 				meleeAttack += (2 + BAB_poor);
 				meleeDefence += (2 + BAB_poor);
 				rangedAttack += (1 + BAB_avg);
 				rangedDefence += (1 + BAB_avg);
 				magicAttack += (5 + BAB_good);
 				magicDefence += (5 + BAB_good);
 			}
 
 
 			if (characterClass == CharacterClass.WIZARD) {
 				meleeAttack += (1 + BAB_poor);
 				meleeDefence += (1 + BAB_poor);
 				rangedAttack += (2 + BAB_avg);
 				rangedDefence += (2 + BAB_avg);
 				magicAttack += (5 + BAB_good);
 				magicDefence += (5 + BAB_good);
 			}
 
 
 			if (gildorymCharacterCards.getCharacterCards().get(player.getName()).getRace() == Race.DWARF) {
 				magicAttack -= 2;
 				if (level > 1) {
 					magicAttack -= (0.125 * level);
 				}	
 			}
 
 
 			if (gildorymCharacterCards.getCharacterCards().get(player.getName()).getRace() == Race.ELF) {
 				rangedAttack += 2;
 				reflex += 2;
 				if (level > 1) {
 					rangedAttack += (0.125 * level);
 					reflex += (0.125 * level);
 				}	
 			}
 
 
 			if (gildorymCharacterCards.getCharacterCards().get(player.getName()).getRace() == Race.GNOME) {
 				meleeAttack -= 2;
 				rangedAttack -= 2;
 				if (level > 1) {
 					meleeAttack -= (0.125 * level);
 					rangedAttack -= (0.125 * level);
 				}	
 			}
 
 
 			if (gildorymCharacterCards.getCharacterCards().get(player.getName()).getRace() == Race.HALFLING) {
 				reflex += 2;
 				rangedAttack += 2;
 				meleeAttack -= 2;
 				if (level > 1) {
 					meleeAttack -= (0.125 * level);
 					rangedAttack += (0.125 * level);
 					reflex += (0.125 * level);
 				}	
 			}
 
 
 			if (gildorymCharacterCards.getCharacterCards().get(player.getName()).getRace() == Race.HALFORC) {
 				meleeAttack += 2;
 				rangedAttack += 2;
 				magicAttack -= 4;
 				magicDefence -= 2;
 				if (level > 1) {
 					meleeAttack += (0.125 * level);
 					rangedAttack += (0.125 * level);
 					magicAttack -= (0.25 * level);
 					magicDefence -= (0.125 * level);
 				}
 			}
 
 
 			/*if (gildorymCharacterCards.getCharacterCards().get(player.getName()).getRace() == Race.DROW) {
 				Block block = ((Player)sender).getWorld().getBlockAt(((Player)sender).getLocation());
 
 
 				if (block.getLightLevel() <= 7) {
 					meleeAttack += 1.0D;
 					meleeDefence += 1.0D;
 					magicAttack += 1.0D;
 					magicDefence += 1.0D;
 					rangedAttack += 1.0D;
 					rangedDefence += 1.0D;
 					reflex += 1.0D;
 					luck += 1.0D;
 				}
 
 
 				if (block.getLightLevel() >= 8) {
 					meleeAttack -= 1.0D;
 					meleeDefence -= 1.0D;
 					magicAttack -= 1.0D;
 					magicDefence -= 1.0D;
 					rangedAttack -= 1.0D;
 					rangedDefence -= 1.0D;
 					reflex -= 1.0D;
 					luck -= 1.0D;
 				}
 			}*/
 
 			//WOOD_SWORD
 			if (player.getItemInHand().getType() == Material.getMaterial(268)) {
 				meleeAttack += 2.0D;
 			}
 			
 			//WOOD_PICKAXE
 			if (player.getItemInHand().getType() == Material.getMaterial(270)) {
 				meleeAttack += 2.0D;
 			}
 			
 			//WOOD_AXE
 			if (player.getItemInHand().getType() == Material.getMaterial(271)) {
 				meleeAttack += 2.0D;
 			}
 			
 			//WOOD_HOE
 			if (player.getItemInHand().getType() == Material.getMaterial(290)) {
 				meleeAttack += 2.0D;
 			}
 
 			//STONE_SWORD
 			if (player.getItemInHand().getType() == Material.getMaterial(272)) {
 				meleeAttack += 3.0D;
 			}
 			
 			//STONE_PICKAXE
 			if (player.getItemInHand().getType() == Material.getMaterial(274)) {
 				meleeAttack += 3.0D;
 			}
 			
 			//STONE_AXE
 			if (player.getItemInHand().getType() == Material.getMaterial(271)) {
 				meleeAttack += 3.0D;
 			}
 			
 			//IRON_SWORD
 			if (player.getItemInHand().getType() == Material.getMaterial(267)) {
 				meleeAttack += 4.0D;
 			}
 			
 			//IRON_PICKAXE
 			if (player.getItemInHand().getType() == Material.getMaterial(257)) {
 				meleeAttack += 4.0D;
 			}
 
 			//IRON_AXE
 			if (player.getItemInHand().getType() == Material.getMaterial(258)) {
 				meleeAttack += 4.0D;
 			}
 			
 			//DIAMOND_SWORD
 			if (player.getItemInHand().getType() == Material.getMaterial(276)) {
 				meleeAttack += 5.0D;
 			}
 			
 			//DIAMOND_PICKAXE
 			if (player.getItemInHand().getType() == Material.getMaterial(278)) {
 				meleeAttack += 5.0D;
 			}
 			
 			//DIAMOND_AXE
 			if (player.getItemInHand().getType() == Material.getMaterial(279)) {
 				meleeAttack += 5.0D;
 			}
 			
 			//DIAMOND_HOE
 			if (player.getItemInHand().getType() == Material.getMaterial(293)) {
 				magicAttack += 5.0D;
 			}
 			
 			//DIAMOND_SPADE
 			if (player.getItemInHand().getType() == Material.getMaterial(277)) {
 				magicAttack += 5.0D;
 			}
 			
 			//BOW
 			if (player.getItemInHand().getType() == Material.getMaterial(261)) {
 				rangedAttack += 5.0D;
 			}
 
 			//STICK
 			if (player.getItemInHand().getType() == Material.getMaterial(280)) {
 				magicAttack += 2.0D;
 			}
 
 			//BLAZE_POWDER
 			if (player.getItemInHand().getType() == Material.getMaterial(387)) {
 				magicAttack += 3.0D;
 			}
 
 			//ENDER_PEARL
 			if (player.getItemInHand().getType() == Material.getMaterial(368)) {
 				magicAttack += 4.0D;
 			}
 
 			//BLAZE_ROD
 			if (player.getItemInHand().getType() == Material.getMaterial(369)) {
 				magicAttack += 5.0D;
 			}
 			
 			//GOLDEN_HOE
 			if (player.getItemInHand().getType() == Material.getMaterial(294)) {
 				magicAttack += 5.0D;
 			}
 
 			//LEATHER_HELMET
 			if ((player.getInventory().getHelmet() != null) && (player.getInventory().getHelmet().getType() == Material.getMaterial(298))) {
 				meleeDefence += 0.25D;
 				rangedDefence += 0.25D;
 			}
 
 			//LEATHER_CHESTPLATE
 			if ((player.getInventory().getChestplate() != null) && (player.getInventory().getChestplate().getType() == Material.getMaterial(299))) {
 				meleeDefence += 1.0D;
 				rangedDefence += 1.0D;
 			}
 
 			//LEATHER_LEGGINGS
 			if ((player.getInventory().getLeggings() != null) && (player.getInventory().getLeggings().getType() == Material.getMaterial(300))) {
 				meleeDefence += 0.5D;
 				rangedDefence += 0.5D;
 			}
 
 			//LEATHER_BOOTS
 			if ((player.getInventory().getBoots() != null) && (player.getInventory().getBoots().getType() == Material.getMaterial(301))) {
 				meleeDefence += 0.25D;
 				rangedDefence += 0.25D;
 			}
 
 			//CHAINMAIL_HELMET
 			if ((player.getInventory().getHelmet() != null) && (player.getInventory().getHelmet().getType() == Material.getMaterial(302))) {
 				meleeDefence += 0.5D;
 				rangedDefence += 0.5D;
 			}
 
 			//CHAINMAIL_CHESTPLATE
 			if ((player.getInventory().getChestplate() != null) && (player.getInventory().getChestplate().getType() == Material.getMaterial(303))) {
 				meleeDefence += 1.25D;
 				rangedDefence += 1.25D;
 			}
 
 			//CHAINMAIL_LEGGINGS
 			if ((player.getInventory().getLeggings() != null) && (player.getInventory().getLeggings().getType() == Material.getMaterial(304))) {
 				meleeDefence += 0.75D;
 				rangedDefence += 0.75D;
 			}
 
 			//CHAINMAIL_BOOTS
 			if ((player.getInventory().getBoots() != null) && (player.getInventory().getBoots().getType() == Material.getMaterial(305))) {
 				meleeDefence += 0.5D;
 				rangedDefence += 0.5D;
 			}
 
 			//IRON_HELMET
 			if ((player.getInventory().getHelmet() != null) && (player.getInventory().getHelmet().getType() == Material.getMaterial(306))) {
 				meleeDefence += 0.75D;
 				rangedDefence += 0.75D;
 			}
 
 			//IRON_CHESTPLATE
 			if ((player.getInventory().getChestplate() != null) && (player.getInventory().getChestplate().getType() == Material.getMaterial(307))) {
 				meleeDefence += 1.5D;
 				rangedDefence += 1.5D;
 			}
 
 			//IRON_LEGGINGS
 			if ((player.getInventory().getLeggings() != null) && (player.getInventory().getLeggings().getType() == Material.getMaterial(308))) {
 				meleeDefence += 1.0D;
 				rangedDefence += 1.0D;
 			}
 
 			//IRON_BOOTS
 			if ((player.getInventory().getBoots() != null) && (player.getInventory().getBoots().getType() == Material.getMaterial(309))) {
 				meleeDefence += 0.75D;
 				rangedDefence += 0.75D;
 			}
 
 			//DIAMOND_HELMET
 			if ((player.getInventory().getHelmet() != null) && (player.getInventory().getHelmet().getType() == Material.getMaterial(310))) {
 				meleeDefence += 1.0D;
 				rangedDefence += 1.0D;
 			}
 
 			//DIAMOND_CHESTPLATE
 			if ((player.getInventory().getChestplate() != null) && (player.getInventory().getChestplate().getType() == Material.getMaterial(311))) {
 				meleeDefence += 1.75D;
 				rangedDefence += 1.75D;
 			}
 
 			//DIAMOND_LEGGINGS
 			if ((player.getInventory().getLeggings() != null) && (player.getInventory().getLeggings().getType() == Material.getMaterial(312))) {
 				meleeDefence += 1.25D;
 				rangedDefence += 1.25D;
 			}
 
 			//DIAMOND_BOOTS
 			if ((player.getInventory().getBoots() != null) && (player.getInventory().getBoots().getType() == Material.getMaterial(313))) {
 				meleeDefence += 1.0D;
 				rangedDefence += 1.0D;
 			}
 			
 
 
 			/*Iterator<Enchantment> enchantmentIterator;
 			enchantmentIterator = ((Player) sender).getItemInHand().getEnchantments().keySet().iterator();
 			while (enchantmentIterator.hasNext()) {
 				Enchantment enchantment = enchantmentIterator.next();
 				if (((Player) sender).getItemInHand().getType() == Material.BOW) {
 					if (enchantment == Enchantment.ARROW_DAMAGE || enchantment == Enchantment.ARROW_FIRE) {
 						rangedAttack += ((Player) sender).getItemInHand().getEnchantments().get(enchantment);
 					}
 				}
 				
 				if (((Player) sender).getItemInHand().getType() == Material.WOOD_SWORD
 						|| ((Player) sender).getItemInHand().getType() == Material.STONE_SWORD
 						|| ((Player) sender).getItemInHand().getType() == Material.IRON_SWORD
 						|| ((Player) sender).getItemInHand().getType() == Material.GOLD_SWORD
 						|| ((Player) sender).getItemInHand().getType() == Material.DIAMOND_SWORD) {
 					if (enchantment == Enchantment.DAMAGE_ALL || enchantment == Enchantment.FIRE_ASPECT) {
 						meleeAttack += ((Player) sender).getItemInHand().getEnchantments().get(enchantment);
 					}
 				}
 				
 				if (((Player) sender).getItemInHand().getType() == Material.STICK
 						|| ((Player) sender).getItemInHand().getType() == Material.BLAZE_ROD
 						|| ((Player) sender).getItemInHand().getType() == Material.ENDER_PEARL
 						|| ((Player) sender).getItemInHand().getType() == Material.EYE_OF_ENDER) {
 					if (enchantment == Enchantment.DAMAGE_ALL || enchantment == Enchantment.FIRE_ASPECT) {
 						magicAttack += ((Player) sender).getItemInHand().getEnchantments().get(enchantment);
 					}
 				}
 				
 				if (((Player) sender).getInventory().getHelmet() != null) {
 					enchantmentIterator = ((Player) sender).getInventory().getHelmet().getEnchantments().keySet().iterator();
 					while (enchantmentIterator.hasNext()) {
 						if (enchantment == Enchantment.PROTECTION_ENVIRONMENTAL) {
 							meleeDefence += 0.25D * ((Player) sender).getItemInHand().getEnchantments().get(enchantment);
 						}
 						
 						if (enchantment == Enchantment.PROTECTION_EXPLOSIONS) {
 							magicDefence += 0.25D * ((Player) sender).getItemInHand().getEnchantments().get(enchantment);
 						}
 						
 						if (enchantment == Enchantment.PROTECTION_PROJECTILE) {
 							rangedDefence += 0.25D * ((Player) sender).getItemInHand().getEnchantments().get(enchantment);
 						}
 					}
 				}
 				
 				if (((Player) sender).getInventory().getChestplate() != null) {
 					enchantmentIterator = ((Player) sender).getInventory().getHelmet().getEnchantments().keySet().iterator();
 					while (enchantmentIterator.hasNext()) {
 						if (enchantment == Enchantment.PROTECTION_ENVIRONMENTAL) {
 							meleeDefence += 0.25D * ((Player) sender).getItemInHand().getEnchantments().get(enchantment);
 						}
 						
 						if (enchantment == Enchantment.PROTECTION_EXPLOSIONS) {
 							magicDefence += 0.25D * ((Player) sender).getItemInHand().getEnchantments().get(enchantment);
 						}
 						
 						if (enchantment == Enchantment.PROTECTION_PROJECTILE) {
 							rangedDefence += 0.25D * ((Player) sender).getItemInHand().getEnchantments().get(enchantment);
 						}
 					}
 				}
 				
 				if (((Player) sender).getInventory().getLeggings() != null) {
 					enchantmentIterator = ((Player) sender).getInventory().getHelmet().getEnchantments().keySet().iterator();
 					while (enchantmentIterator.hasNext()) {
 						if (enchantment == Enchantment.PROTECTION_ENVIRONMENTAL) {
 							meleeDefence += 0.25D * ((Player) sender).getItemInHand().getEnchantments().get(enchantment);
 						}
 						
 						if (enchantment == Enchantment.PROTECTION_EXPLOSIONS) {
 							magicDefence += 0.25D * ((Player) sender).getItemInHand().getEnchantments().get(enchantment);
 						}
 						
 						if (enchantment == Enchantment.PROTECTION_PROJECTILE) {
 							rangedDefence += 0.25D * ((Player) sender).getItemInHand().getEnchantments().get(enchantment);
 						}
 					}
 				}
 				
 				if (((Player) sender).getInventory().getBoots() != null) {
 					enchantmentIterator = ((Player) sender).getInventory().getHelmet().getEnchantments().keySet().iterator();
 					while (enchantmentIterator.hasNext()) {
 						if (enchantment == Enchantment.PROTECTION_ENVIRONMENTAL) {
 							meleeDefence += 0.25D * ((Player) sender).getItemInHand().getEnchantments().get(enchantment);
 						}
 						
 						if (enchantment == Enchantment.PROTECTION_EXPLOSIONS) {
 							magicDefence += 0.25D * ((Player) sender).getItemInHand().getEnchantments().get(enchantment);
 						}
 						
 						if (enchantment == Enchantment.PROTECTION_PROJECTILE) {
 							rangedDefence += 0.25D * ((Player) sender).getItemInHand().getEnchantments().get(enchantment);
 						}
 					}
 				}
 			}*/
 
 
 			sender.sendMessage(ChatColor.GRAY + "======================");
 			sender.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + player.getDisplayName() + "'s Roll Info");
 			sender.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Class: " + characterClass);
 			sender.sendMessage(ChatColor.GRAY + "======================");
 			sender.sendMessage(ChatColor.RED + "Melee   - Attack: " + ChatColor.WHITE + (int) Math.floor(meleeAttack) + ChatColor.RED + "  Defence: " + ChatColor.WHITE + (int)Math.floor(meleeDefence));
 			sender.sendMessage(ChatColor.RED + "Ranged - Attack: " + ChatColor.WHITE + (int) Math.floor(rangedAttack) + ChatColor.RED + "  Defence: " + ChatColor.WHITE + (int)Math.floor(rangedDefence));
 			sender.sendMessage(ChatColor.RED + "Magic   - Attack: " + ChatColor.WHITE + (int) Math.floor(magicAttack) + ChatColor.RED + "  Defence: " + ChatColor.WHITE + (int)Math.floor(magicDefence));
 			sender.sendMessage(ChatColor.RED + "Luck: " + ChatColor.WHITE + (int) Math.floor(luck));
 			sender.sendMessage(ChatColor.RED + "Reflex: " + ChatColor.WHITE + (int) Math.floor(reflex));
 		} else {
 			sender.sendMessage(ChatColor.DARK_RED + "Could not find that player!");
 		}
 		return true;
 	}
 
 
 }
 
