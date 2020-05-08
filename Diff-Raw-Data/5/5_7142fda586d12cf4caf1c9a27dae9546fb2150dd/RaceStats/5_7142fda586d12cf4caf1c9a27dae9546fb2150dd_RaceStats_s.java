 package com.gildorymrp.gildorym.api.stats;
 
 import org.bukkit.entity.Player;
 
 import com.gildorymrp.charactercards.Race;
 
 public class RaceStats {
 	
 	public static double getMeleeAttack(Player player){
 		int level = Util.getLevel(player);
 		Race race = Util.getRace(player);
 		int meleeAttack = 0;
 		
 		switch(race){
 		case GNOME:
 			meleeAttack -= 2;
 			if (level > 1)
				rangedAttack -= 0.125 * level;
 			break;
 		case HALFLING:
 			meleeAttack -= 2;
 			if (level > 1)
				rangedAttack -= 0.125 * level;
 			break;
 		case HALFORC:
 			meleeAttack += 2;
 			if (level > 1)
 				meleeAttack += (0.125 * level);
 			break;
 		default:
 			break;
 		}
 		
 		return meleeAttack;
 	}
 	
 	public static double getMeleeDefence(Player player){
 		int meleeDefence = 0;
 		
 		return meleeDefence;
 	}
 	
 	public static double getRangedAttack(Player player){
 		int level = Util.getLevel(player);
 		Race race = Util.getRace(player);
 		int rangedAttack = 0;
 		
 		switch (race){
 		case ELF:
 			rangedAttack += 2;
 			if (level > 1)
 				rangedAttack += 0.125 * level;
 		case GNOME:
 			rangedAttack -= 2;
 			if (level > 1)
 				rangedAttack -= 0.125 * level;
 		case HALFLING:
 			rangedAttack += 2;
 			if (level > 1)
 				rangedAttack += 0.125 * level;
 			break;
 		case HALFORC:
 			rangedAttack += 2;
 			if (level > 1)
 				rangedAttack += 0.125 * level;
 		default:
 			break;
 		}
 		
 		return rangedAttack;
 	}
 	
 	public static double getRangedDefence(Player player){
 		int rangedDefence = 0;
 		
 		return rangedDefence;
 	}
 	
 	public static double getMagicAttack(Player player){
 		int level = Util.getLevel(player);
 		Race race = Util.getRace(player);
 		int magicAttack = 0;
 		
 		switch (race){
 		case DWARF:
 			magicAttack -= 2;
 			if (level > 1)
 				magicAttack -= (0.125 * level);
 			break;
 		case HALFORC:
 			magicAttack -= 4;
 			if (level > 1)
 				magicAttack -= (0.25 * level);
 			break;
 		default:
 			break;
 		}
 		
 		return magicAttack;
 	}
 	
 	public static double getMagicDefence(Player player){
 		int level = Util.getLevel(player);
 		Race race = Util.getRace(player);
 		int magicDefence = 0;
 		
 		switch (race){
 		case HALFORC:
 			magicDefence -= 2;
 			if (level > 1)
 				magicDefence -= (0.125 * level);
 			break;
 		default:
 			break;
 		}
 		
 		return magicDefence;
 	}
 	
 	public static int getFortitude(Player player){
 		return 0;
 	}
 	
 	public static int getReflex(Player player){
 		return 0;
 	}
 	
 	public static int getWill(Player player){
 		return 0;
 	}
 	
 }
