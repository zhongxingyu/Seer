 package com.andrewsun.worldresetter;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.io.FileUtils;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public final class WorldResetter extends JavaPlugin {
 
 	@Override
 	public void onEnable(){
 		if (!getDataFolder().exists()) {
 			boolean success = getDataFolder().mkdirs();
 			if (!success) {
 				// Directory creation failed
 				getLogger().info("WorldResetter failed to create data directory!");
 			} else {
 				getLogger().info("WorldResetter enabled!");	
 			}
 		}
 	}
 
 	@Override
 	public void onDisable(){
 		getLogger().info("WorldResetter disabled!");
 	}
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
 		if(cmd.getName().equalsIgnoreCase("resetworld")){
 			if (args.length > 1) {
 				sender.sendMessage(ChatColor.RED + "Usage:");
				sender.sendMessage(ChatColor.RED + "/" + cmd + " <world>");
 				return true;
 			} else {
 				if (args.length < 1) {
 					sender.sendMessage(ChatColor.RED + "Usage:");
					sender.sendMessage(ChatColor.RED + "/" + cmd + " <world>");
 					return true;
 				} else {
 					if (Bukkit.getWorld(args[0]) == null) {
 						sender.sendMessage(ChatColor.RED + "World does not exist or isn't loaded!");
 						return true;
 					} else {
 						File worlddir=new File(getDataFolder() + args[0], null);
 						if (!worlddir.exists()) {
 							// No world backup directory
 							sender.sendMessage(ChatColor.RED + "There isn't a backup for this world!");
 						} else {
 							Bukkit.broadcastMessage(ChatColor.GREEN + "[WorldResetter] Resetting world \"" + args[0] + "\"...");
 							
 							Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "mv unload " + args[0]);
 							try {
 								File WorldDirRepl = Bukkit.getServer().getWorld(args[0]).getWorldFolder();
 								FileUtils.deleteDirectory(WorldDirRepl);
 								FileUtils.copyDirectory(new File(getDataFolder() + args[0], null), new File(WorldDirRepl + "/../"));
 								Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "mv load " + args[0]);
 								Bukkit.broadcastMessage(ChatColor.GREEN + "[WorldResetter] World \"" + args[0] + "\" sucessfully reset.");
 								
 							} catch (IOException e) {
 								Bukkit.broadcastMessage(ChatColor.RED + "[WorldResetter] Unable to delete world directory!");
 							}
 						}
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 }
