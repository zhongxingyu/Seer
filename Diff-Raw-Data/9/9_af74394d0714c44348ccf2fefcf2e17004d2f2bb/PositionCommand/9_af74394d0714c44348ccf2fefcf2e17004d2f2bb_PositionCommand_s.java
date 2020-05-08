 package com.github.thebiologist13.commands.spawners;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.github.thebiologist13.CustomSpawners;
 import com.github.thebiologist13.commands.SpawnerCommand;
 
 public class PositionCommand extends SpawnerCommand {
 	
 	private Logger log = null;
 	
 	public PositionCommand(CustomSpawners plugin) {
 		this.log = plugin.log;
 	}
 	
 	@Override
 	public void run(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
 		//Player
 		Player p = null;
 		//Location
 		Location l = null;
 		//Perms
 		String perm = "customspawners.spawners.position";
 		
 		if(!(arg0 instanceof Player)) {
 			log.info(NO_CONSOLE);
 			return;
 		}
 		
 		p = (Player) arg0;
 		
 		if(p.hasPermission(perm) && arg3[0].equalsIgnoreCase("pos1")) {
 			
 			l = p.getLocation();
 			
 			CustomSpawners.selectionPointOne.put(p, l);
 			
 			p.sendMessage(ChatColor.GREEN + "Set spawn area selection point one to: " + ChatColor.GOLD + "(" +
 					l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + ")" + ChatColor.GREEN + ".");
 			
 		} else if(p.hasPermission(perm) && arg3[0].equalsIgnoreCase("pos2")) {
 			
 			l = p.getLocation();
 			
			CustomSpawners.selectionPointOne.put(p, l);
 			
 			p.sendMessage(ChatColor.GREEN + "Set spawn area selection point two to: " + ChatColor.GOLD + "(" +
 					l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + ")" + ChatColor.GREEN + ".");
 			
 		} else {
 			p.sendMessage(NO_PERMISSION);
 			return;
 		}
 	}
 
 }
