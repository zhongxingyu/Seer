 package com.comze_instancelabs.skins.mobs;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 
 public class Enderman {
 	public static void buildEnderman(Location start, String direction){
 		buildLegs(start, direction);
 		buildArms(start, direction);
 		buildBody(start, direction);
 		buildHead(start, direction);
 	}
 	
 	public static void buildLegs(Location start, String d){
 		for(int i__ = 0; i__ < 2; i__++){
 			for(int i_ = 0; i_ < 2; i_++){
 				for(int i = 0; i < 24; i++){
 					Block b;
 					if(d.equalsIgnoreCase("west")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i__ - 1, start.getBlockY() + i, start.getBlockZ() - i_));
 					}else if(d.equalsIgnoreCase("east")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + i__ + 1, start.getBlockY() + i, start.getBlockZ() + i_));
 					}else if(d.equalsIgnoreCase("north")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + i_, start.getBlockY() + i, start.getBlockZ() - i__ - 1));
 					}else if(d.equalsIgnoreCase("south")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i_, start.getBlockY() + i, start.getBlockZ() + i__ + 1));
 					}else{
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i__ - 1, start.getBlockY() + i, start.getBlockZ() - i_));
 					}
 					b.setType(Material.WOOL);
 					b.setData((byte)15);
 				}
 			}	
 		}
 		
 		for(int i__ = 0; i__ < 2; i__++){
 			for(int i_ = 0; i_ < 2; i_++){
 				for(int i = 0; i < 24; i++){
 					Block b;
 					if(d.equalsIgnoreCase("west")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i__ - 1, start.getBlockY() + i, start.getBlockZ() - i_ - 4));
 					}else if(d.equalsIgnoreCase("east")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + i__ + 1, start.getBlockY() + i, start.getBlockZ() + i_ + 4));
 					}else if(d.equalsIgnoreCase("north")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + i_ + 4, start.getBlockY() + i, start.getBlockZ() - i__ - 1));
 					}else if(d.equalsIgnoreCase("south")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i_ - 4, start.getBlockY() + i, start.getBlockZ() + i__ + 1));
 					}else{
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i__ - 1, start.getBlockY() + i, start.getBlockZ() - i_ - 4));
 					}
 					b.setType(Material.WOOL);
 					b.setData((byte)15);
 				}
 			}	
 		}
 	}
 	
 	public static void buildArms(Location start, String d){
 		for(int i__ = 0; i__ < 2; i__++){
 			for(int i_ = 0; i_ < 2; i_++){
 				for(int i = 0; i < 24; i++){
 					Block b;
 					if(d.equalsIgnoreCase("west")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i__ - 1, start.getBlockY() + i + 12, start.getBlockZ() - i_ + 3));
 					}else if(d.equalsIgnoreCase("east")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + i__ + 1, start.getBlockY() + i + 12, start.getBlockZ() + i_ - 3));
 					}else if(d.equalsIgnoreCase("north")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + i_ - 3, start.getBlockY() + i + 12, start.getBlockZ() - i__ - 1));
 					}else if(d.equalsIgnoreCase("south")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i_ + 3, start.getBlockY() + i + 12, start.getBlockZ() + i__ + 1));
 					}else{
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i__ - 1, start.getBlockY() + i + 12, start.getBlockZ() - i_));
 					}
 					b.setType(Material.WOOL);
 					b.setData((byte)15);
 				}
 			}	
 		}
 		
 		for(int i__ = 0; i__ < 2; i__++){
 			for(int i_ = 0; i_ < 2; i_++){
 				for(int i = 0; i < 24; i++){
 					Block b;
 					if(d.equalsIgnoreCase("west")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i__ - 1, start.getBlockY() + i + 12, start.getBlockZ() - i_ - 7));
 					}else if(d.equalsIgnoreCase("east")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + i__ + 1, start.getBlockY() + i + 12, start.getBlockZ() + i_ + 7));
 					}else if(d.equalsIgnoreCase("north")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + i_ + 7, start.getBlockY() + i + 12, start.getBlockZ() - i__ - 1));
 					}else if(d.equalsIgnoreCase("south")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i_ - 7, start.getBlockY() + i + 12, start.getBlockZ() + i__ + 1));
 					}else{
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i__ - 1, start.getBlockY() + i + 12, start.getBlockZ() - i_ - 7));
 					}
 					b.setType(Material.WOOL);
 					b.setData((byte)15);
 				}
 			}	
 		}
 	}
 	
 	public static void buildBody(Location start, String d){
 		for(int i__ = 0; i__ < 4; i__++){
 			for(int i_ = 0; i_ < 8; i_++){
 				for(int i = 0; i < 12; i++){
 					Block b;
 					if(d.equalsIgnoreCase("west")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i__, start.getBlockY() + i + 24, start.getBlockZ() - i_ + 1));
 					}else if(d.equalsIgnoreCase("east")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + i__, start.getBlockY() + i + 24, start.getBlockZ() + i_ - 1));
 					}else if(d.equalsIgnoreCase("north")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + i_ - 1, start.getBlockY() + i + 24, start.getBlockZ() - i__));
 					}else if(d.equalsIgnoreCase("south")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i_ + 1, start.getBlockY() + i + 24, start.getBlockZ() + i__));
 					}else{
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i__, start.getBlockY() + i + 24, start.getBlockZ() - i_ + 1));
 					}
 					b.setType(Material.WOOL);
 					b.setData((byte)15);
 				}
 			}	
 		}
 	}
 	
 	public static void buildHead(Location start, String d){
 		for(int i__ = 0; i__ < 8; i__++){
 			for(int i_ = 0; i_ < 8; i_++){
 				for(int i = 0; i < 8; i++){
 					Block b;
 					if(d.equalsIgnoreCase("west")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i__ + 2, start.getBlockY() + i + 36, start.getBlockZ() - i_ + 1));
 					}else if(d.equalsIgnoreCase("east")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + i__ - 2, start.getBlockY() + i + 36, start.getBlockZ() + i_ - 1));
 					}else if(d.equalsIgnoreCase("north")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + i_ - 1, start.getBlockY() + i + 36, start.getBlockZ() - i__ + 2));
 					}else if(d.equalsIgnoreCase("south")){
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i_ + 1, start.getBlockY() + i + 36, start.getBlockZ() + i__ - 2));
 					}else{
 						b = start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - i__ + 2, start.getBlockY() + i + 36, start.getBlockZ() - i_ + 1));
 					}
 					b.setType(Material.WOOL);
 					b.setData((byte)15);
 				}
 			}
 		}
 		
 		// eyes
 		if(d.equalsIgnoreCase("west")){
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + 2, start.getBlockY() + 39, start.getBlockZ() + 1)).setTypeIdAndData(35, (byte)0, true);
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + 2, start.getBlockY() + 39, start.getBlockZ())).setTypeIdAndData(35, (byte)10, true);
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + 2, start.getBlockY() + 39, start.getBlockZ() - 1)).setTypeIdAndData(35, (byte)0, true);
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + 2, start.getBlockY() + 39, start.getBlockZ() - 4)).setTypeIdAndData(35, (byte)0, true);
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + 2, start.getBlockY() + 39, start.getBlockZ() - 5)).setTypeIdAndData(35, (byte)10, true);
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + 2, start.getBlockY() + 39, start.getBlockZ() - 6)).setTypeIdAndData(35, (byte)0, true);
 		}else if(d.equalsIgnoreCase("east")){
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - 2, start.getBlockY() + 39, start.getBlockZ() - 1)).setTypeIdAndData(35, (byte)0, true);
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - 2, start.getBlockY() + 39, start.getBlockZ())).setTypeIdAndData(35, (byte)10, true);
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - 2, start.getBlockY() + 39, start.getBlockZ() + 1)).setTypeIdAndData(35, (byte)0, true);
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - 2, start.getBlockY() + 39, start.getBlockZ() + 4)).setTypeIdAndData(35, (byte)0, true);
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - 2, start.getBlockY() + 39, start.getBlockZ() + 5)).setTypeIdAndData(35, (byte)10, true);
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - 2, start.getBlockY() + 39, start.getBlockZ() + 6)).setTypeIdAndData(35, (byte)0, true);
 		}else if(d.equalsIgnoreCase("north")){
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - 1, start.getBlockY() + 39, start.getBlockZ() + 2)).setTypeIdAndData(35, (byte)0, true);
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX(), start.getBlockY() + 39, start.getBlockZ() + 2)).setTypeIdAndData(35, (byte)10, true);
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + 1, start.getBlockY() + 39, start.getBlockZ() + 2)).setTypeIdAndData(35, (byte)0, true);
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + 4, start.getBlockY() + 39, start.getBlockZ() + 2)).setTypeIdAndData(35, (byte)0, true);
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + 5, start.getBlockY() + 39, start.getBlockZ() + 2)).setTypeIdAndData(35, (byte)10, true);
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + 6, start.getBlockY() + 39, start.getBlockZ() + 2)).setTypeIdAndData(35, (byte)0, true);
 		}else if(d.equalsIgnoreCase("south")){
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + 1, start.getBlockY() + 39, start.getBlockZ() - 2)).setTypeIdAndData(35, (byte)0, true);
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX(), start.getBlockY() + 39, start.getBlockZ() - 2)).setTypeIdAndData(35, (byte)10, true);
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - 1, start.getBlockY() + 39, start.getBlockZ() - 2)).setTypeIdAndData(35, (byte)0, true);
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - 4, start.getBlockY() + 39, start.getBlockZ() - 2)).setTypeIdAndData(35, (byte)0, true);
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - 5, start.getBlockY() + 39, start.getBlockZ() - 2)).setTypeIdAndData(35, (byte)10, true);
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() - 6, start.getBlockY() + 39, start.getBlockZ() - 2)).setTypeIdAndData(35, (byte)0, true);
 		}else{
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + 2, start.getBlockY() + 39, start.getBlockZ() + 1)).setTypeIdAndData(35, (byte)0, true);
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + 2, start.getBlockY() + 39, start.getBlockZ())).setTypeIdAndData(35, (byte)10, true);
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + 2, start.getBlockY() + 39, start.getBlockZ() - 1)).setTypeIdAndData(35, (byte)0, true);
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + 2, start.getBlockY() + 39, start.getBlockZ() - 4)).setTypeIdAndData(35, (byte)0, true);
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + 2, start.getBlockY() + 39, start.getBlockZ() - 5)).setTypeIdAndData(35, (byte)10, true);
 			start.getWorld().getBlockAt(new Location(start.getWorld(), start.getBlockX() + 2, start.getBlockY() + 39, start.getBlockZ() - 6)).setTypeIdAndData(35, (byte)0, true);
 		}
 	}
 	
 	public static void undoEnderman(Location start, String d){
 		World w = start.getWorld();
 		for(int i__ = 0; i__ < 8; i__++){
 			for(int i_ = 0; i_ < 16; i_++){
 				for(int i = 0; i < 44; i++){
 					Block b;
 					if(d.equalsIgnoreCase("west")){
 						b = start.getWorld().getBlockAt(new Location(w, start.getBlockX() - i__ + 2, start.getBlockY() + i, start.getBlockZ() - i_ + 3));
 					}else if(d.equalsIgnoreCase("east")){
 						b = start.getWorld().getBlockAt(new Location(w, start.getBlockX() + i__ - 2, start.getBlockY() + i, start.getBlockZ() + i_ - 3));
 					}else if(d.equalsIgnoreCase("north")){
						b = start.getWorld().getBlockAt(new Location(w, start.getBlockX() + i_ - 3, start.getBlockY() + i, start.getBlockZ() - i__ + 2));
 					}else if(d.equalsIgnoreCase("south")){
						b = start.getWorld().getBlockAt(new Location(w, start.getBlockX() - i_ + 3, start.getBlockY() + i, start.getBlockZ() + i__ - 2));
 					}else{
 						b = start.getWorld().getBlockAt(new Location(w, start.getBlockX() - i__ + 2, start.getBlockY() + i, start.getBlockZ() - i_ + 3));
 					}
 					if(b.getType() == Material.WOOL){
 						b.setType(Material.AIR);
 					}
 				}
 			}	
 		}
 	}
 }
