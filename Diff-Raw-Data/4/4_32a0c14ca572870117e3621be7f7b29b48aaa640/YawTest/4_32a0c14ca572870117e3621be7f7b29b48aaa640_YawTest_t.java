 package com.araeosia.yawtest;
 
 import java.util.logging.Logger;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class YawTest extends JavaPlugin implements Listener, CommandExecutor{
 	public Logger log;
 	
 	@Override
 	public void onEnable(){
 		log = getServer().getLogger();
 		log.info("Enabling!");
		getCommand("yaw").setExecutor(this);
 	}
 	@Override
 	public void onDisable(){
 		log.info("Disabling!");
 	}
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) { // Proper command handling.
 		if(cmd.getName().equalsIgnoreCase("yaw")){
 			if(sender instanceof Player){
 				Player p = (Player) sender;
 				if(args.length!=0){
 					int yaw = Integer.parseInt(args[0]);
 					Location newLoc = p.getLocation();
 					newLoc.setYaw(yaw);
					p.sendMessage(newLoc.toString());
 					p.teleport(newLoc);
 					return true;
 				}
 			}else{
 				sender.sendMessage("You cannot execute this command as you do not have a physical form!");
 			}
 		}
 		return false;
 	}
 }
