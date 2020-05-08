 package com.github.thebiologist13.commands.entities;
 
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.util.Vector;
 
 import com.github.thebiologist13.CustomSpawners;
 import com.github.thebiologist13.SpawnableEntity;
 import com.github.thebiologist13.commands.SpawnerCommand;
 
 public class EntityVelocityCommand extends SpawnerCommand {
 	private CustomSpawners plugin = null;
 	
 	private Logger log = null;
 	
 	public EntityVelocityCommand(CustomSpawners plugin) {
 		this.plugin = plugin;
 		this.log = plugin.log;
 	}
 	
 	@Override
 	public void run(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
 
 		//Command Syntax = /customspawners setvelocity [id] <x,y,z>
 		//Array Index with selection           0               1
 		//Without selection                    0         1     2
 		
 		//Player
 		Player p = null;
 		//Entity
 		SpawnableEntity s = null;
 		//Vector components
 		double x = 0;
 		double y = 0;
 		double z = 0;
 		//Permissions
 		String perm = "customspawners.entities.setvelocity";
 		
 		final String COMMAND_FORMAT = ChatColor.RED + "Invalid values for velocity. Please use the following format: " +
 				ChatColor.GOLD + "/entities setvelocity <x value>,<y value>,<z value>.";
 
 		if(!(arg0 instanceof Player)) {
 			log.info(NO_CONSOLE);
 			return;
 		}
 
 		p = (Player) arg0;
 
 		if(p.hasPermission(perm)) {
 			if(CustomSpawners.entitySelection.containsKey(p) && arg3.length == 2) {
 
 				s = plugin.getEntityById(CustomSpawners.entitySelection.get(p));
 				
 				int firstCommaIndex = arg3[1].indexOf(",");
 				int secondCommaIndex = arg3[1].indexOf(",", firstCommaIndex + 1);
 				
 				String xVal = arg3[1].substring(0, firstCommaIndex);
 				String yVal = arg3[1].substring(firstCommaIndex + 1, secondCommaIndex);
 				String zVal = arg3[1].substring(secondCommaIndex + 1, arg3[1].length());
 				
 				if(!plugin.isDouble(xVal) || !plugin.isDouble(yVal) || !plugin.isDouble(zVal)) {
 					p.sendMessage(COMMAND_FORMAT);
 					return;
 				}
 				
 				x = Double.parseDouble(xVal);
 				y = Double.parseDouble(yVal);
 				z = Double.parseDouble(zVal);
 				
 			} else if(arg3.length == 2) {
 				p.sendMessage(NEEDS_SELECTION);
 				return;
 			} else if(arg3.length == 3) {
 
 				int id = 0;
 
 				//Check that the ID entered is a number
 				if(!plugin.isInteger(arg3[1])) {
 					p.sendMessage(ID_NOT_NUMBER);
 					return;
 				}
 
 				id = Integer.parseInt(arg3[1]);
 
 				//Check if the ID entered is the ID of a entity
 				if(!plugin.isValidEntity(id)) {
 					p.sendMessage(NO_ID);
 					return;
 				}
 
 				s = plugin.getEntityById(id);
 				
 				int firstCommaIndex = arg3[2].indexOf(",");
 				int secondCommaIndex = arg3[2].indexOf(",", firstCommaIndex + 1);
 				
 				String xVal = arg3[2].substring(0, firstCommaIndex);
 				String yVal = arg3[2].substring(firstCommaIndex + 1, secondCommaIndex);
 				String zVal = arg3[2].substring(secondCommaIndex + 1, arg3[2].length());
 				
 				if(!plugin.isDouble(xVal) ||!plugin.isDouble(yVal) || !plugin.isDouble(zVal)) {
 					p.sendMessage(COMMAND_FORMAT);
 					return;
 				}
 				
				x = Integer.parseInt(xVal);
				y = Integer.parseInt(yVal);
				z = Integer.parseInt(zVal);
 				
 			} else {
 				p.sendMessage(GENERAL_ERROR);
 				return;
 			}
 
 			//Carry out command
 			s.setVelocity(new Vector(x,y,z));
 
 			//Success
 			p.sendMessage(ChatColor.GREEN + "Successfully set the velocity of spawnable entity with ID " 
 					+ ChatColor.GOLD + s.getId() + ChatColor.GREEN + " to " + ChatColor.GOLD 
 					+ "(" + x + "," + y + "," + z + ")" + ChatColor.GREEN + "!");
 		} else {
 			p.sendMessage(NO_PERMISSION);
 			return;
 		}
 
 	}
 
 }
