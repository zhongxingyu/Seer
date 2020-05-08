 package com.imdeity.deitynether.util;
 
 import java.io.File;
 
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.WorldCreator;
 import org.bukkit.entity.Player;
 
 import com.imdeity.deitynether.DeityNether;
 
 public class WorldManager {
 
 	private WorldCreator newNether = null;
 	private DeityNether plugin = null;
 	
 	public WorldManager(DeityNether instance){
 		plugin = instance;
 		newNether = new WorldCreator(plugin.config.getNetherWorldName());
 	}
 	
 	public void generateNewNether(){
 		unloadNether();
 		File file = new File(plugin.config.getNetherWorldName() + "/");
 		if(file.exists()){
 			
 		}
 		newNether.environment(Environment.NETHER).generateStructures(true).seed(new java.util.Random().nextLong());
 		
 	}
 	
 	public boolean deleteWorld(String worldName){
 		unloadNether();
 		return deleteFilesInFolder(new File(worldName));
 	}
 	
 	private void unloadNether(){
 		World nether = plugin.getServer().getWorld(plugin.config.getNetherWorldName());
		World main = plugin.getServer().getWorld(plugin.config.getMainWorldName());
 		for(Player p : nether.getPlayers()){
 			p.sendMessage(DeityNether.HEADER + "Teleporting you to main world for Nether reset...");
			p.teleport(main.getSpawnLocation());
 		}
 		plugin.getServer().unloadWorld(nether, false);
 	}
 	
 	private boolean deleteFilesInFolder(File folder){
 		for(File f : folder.listFiles()){
 			if(f.isFile()){
 				f.delete();
 			}else if (f.isDirectory()){
 				deleteFilesInFolder(f);
 			}
 		}
 		
 		return folder.delete();
 	}
 	
 }
