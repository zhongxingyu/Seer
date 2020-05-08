 package com.msingleton.templecraft;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.CommandExecutor;
 
 public class TCCommands implements CommandExecutor
 {
     /**
      * Handles all command parsing.
      * Unrecognized commands return false, giving the sender a list of
      * valid commands (from plugin.yml).
      */
     public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
     {       
         // Only accept commands from players.
         if ((sender == null) || !(sender instanceof Player))
         {
             System.out.println("Only players can use these commands, silly.");
             return true;
         }
         
         // Cast the sender to a Player object.
         Player p = (Player) sender;
         
         /* If more than one argument, must be an advanced command.
          * Only allow operators to access these commands. */
         if (args.length > 1)
        		if(advancedCommands(p, args))
        			return true;
         
         // If not exactly one argument, must be an invalid command.
         if (args.length == 1)
         	if(basicCommands(p, args[0].toLowerCase()))
         		return true;
         
         TCPermissionHandler.sendResponse(p);
         return true;
     }
 
 	/**
      * Handles basic commands.
      */
 	private boolean basicCommands(Player p, String cmd)
     {   
 		TemplePlayer tp = TempleManager.templePlayerMap.get(p);
 		
     	if((cmd.equals("join") || cmd.equals("j")) && TCPermissionHandler.hasPermission(p, "templecraft.join"))
     	{
     		for(Temple temple : TempleManager.templeSet){
     			if(!temple.isRunning && temple.isSetup){
     				temple.playerJoin(p);
     				return true;
     			}
     		}
     		TempleManager.tellPlayer(p, "All temples are currently running or disabled! Please try again later.");
     		return true;
     	}
     	
     	/*
     	if((cmd.equals("spectate") || cmd.equals("spec")) && TCPermissionHandler.hasPermission(p, "templecraft.spectate"))
     	{
     		for(Temple temple : TempleManager.templeSet){
     			if(temple.isRunning){
     				temple.playerSpectate(p);
     				return true;
     			}
     		}
     		TempleManager.tellPlayer(p, "There are no temples currently running! Please try again later.");
     		return true;
     	}
     	*/
     	
     	if ((cmd.equals("leave") || cmd.equals("l")) && TCPermissionHandler.hasPermission(p, "templecraft.leave"))
         {
         	TempleManager.playerLeave(p);
             return true;
         }
     	
     	if (cmd.equals("save"))
         {
     		Temple temple = tp.currentTemple;
     		
     		if(temple == null){
     			if(TCPermissionHandler.hasPermission(p, "templecraft.save"))
         			tp.save();
     		} else {
     			if(TCPermissionHandler.hasPermission(p, "templecraft.savetemple")){
     				if(p.getWorld().getName().contains("EditWorld_")){
     					temple.saveTemple(p.getWorld(), p, temple.templeName);
     				}
     			}
     		}
     			
             return true;
         }
     	
     	if (cmd.equals("reload") && TCPermissionHandler.hasPermission(p, "templecraft.reload"))
         {
     		for(Player tempp : TempleManager.world.getPlayers()){
     			TemplePlayer temptp = TempleManager.templePlayerMap.get(tempp);
     			if(temptp.currentTemple != null){
     				TempleManager.tellPlayer(p, "TempleWorld is currently in use. Please wait or use \"/forceend <templename>\" to end the temples.");
     				return true;
     			}
    			TempleManager.loadCustomTemples();
        		TempleManager.tellPlayer(p, "Temples Reloaded");
     		}
             return true;
         }
     	
         if ((cmd.equals("playerlist") || cmd.equals("plist")) && TCPermissionHandler.hasPermission(p, "templecraft.playerlist"))
         {
         	if(TempleManager.playerSet.contains(p))
         		TempleManager.playerList(p);
             return true;
         }
         
         if ((cmd.equals("templelist") || cmd.equals("tlist")) && TCPermissionHandler.hasPermission(p, "templecraft.templelist"))
         {
         	p.sendMessage(ChatColor.GREEN+"Temple List:");
         	for(Temple temple : TempleManager.templeSet)
         		if(temple != null)
         			p.sendMessage(temple.templeName);
             return true;
         }
         
         if ((cmd.equals("ready") || cmd.equals("notready"))  && TCPermissionHandler.hasPermission(p, "templecraft.ready"))
         {
         	Temple temple = TCUtils.getTemple(p);
         	if(temple == null)
         		TempleManager.tellPlayer(p, "You need to be in a temple to use this command.");
         	else if(temple.playerSet.contains(p))
         		temple.notReadyList(p);
             return true;
         }
         
         if (cmd.equals("nullclass") && TCPermissionHandler.hasPermission(p, "templecraft.nullclass"))
         {
         	tp.removeClass();
             return true;
         }
         
         if (cmd.equals("enable") && TCPermissionHandler.hasPermission(p, "templecraft.enable"))
         {            
             // Set the boolean
             TempleManager.isEnabled = Boolean.valueOf(!TempleManager.isEnabled);
             TempleManager.tellPlayer(p, "Enabled: " + TempleManager.isEnabled);
             return true;
         }
         
         if (cmd.equals("checkupdates") && TCPermissionHandler.hasPermission(p, "templecraft.checkupdates"))
         {            
             TCUtils.checkForUpdates(p, false);
             return true;
         }
         
         return false;
     }
     
     private boolean advancedCommands(Player p, String[] args){    	
     	//Room commands
     	String cmd = args[0].toLowerCase();
     	String arg = args[1].toLowerCase();	
     	
     	TemplePlayer tp = TempleManager.templePlayerMap.get(p);
     	
         if (cmd.equals("new") && TCPermissionHandler.hasPermission(p, "templecraft.newtemple"))
         {        	
     		TCUtils.newTemple(p, arg);
     		return true;
         }
         
         if (cmd.equals("delete") && TCPermissionHandler.hasPermission(p, "templecraft.deletetemple"))
         {
         	Temple temple = TCUtils.getTempleByName(arg);
     		
     		if(temple == null){
     			TempleManager.tellPlayer(p, "Temple \""+arg+"\" does not exist");
     			return true;
     		}
     		
         	TCUtils.removeTemple(temple);
         	TempleManager.tellPlayer(p, "Temple \""+arg+"\" deleted");
             return true;
         }
         
         if (cmd.equals("edit") && TCPermissionHandler.hasPermission(p, "templecraft.edittemple"))
         {        	
         	Temple temple = TCUtils.getTempleByName(arg);
     		
     		if(temple == null){
     			TempleManager.tellPlayer(p, "Temple \""+arg+"\" does not exist");
     			return true;
     		}
         	
     		TCUtils.editTemple(p, temple);
             return true;
         }
         
         if (cmd.equals("add") && TCPermissionHandler.hasPermission(p, "templecraft.addplayer"))
         {        	
         	Temple temple = tp.currentTemple;
     		
     		if(temple == null){
     			TempleManager.tellPlayer(p, "You need to be in a temple to use this command.");
     			return true;
     		}
         	
     		if(!tp.ownerOfSet.contains(temple.templeName) && !TCPermissionHandler.hasPermission(p, "templecraft.editall")){
     			TempleManager.tellPlayer(p, "Only the owner of the temple can use this command.");
     			return true;
     		}
     		
     		//Find player in config based on what was entered;
     		String playerName = TCUtils.getKey(TemplePlayer.config, "Players", arg);
     		
     		if(playerName == null){
     			TempleManager.tellPlayer(p, "Player not found.");
     		} else {
     			if(TCUtils.addAccessTo(playerName, temple))
     				TempleManager.tellPlayer(p, "Added \""+playerName+"\" to temple.");
     			else
     				TempleManager.tellPlayer(p, "\""+playerName+"\" already has access to this temple.");
     		}
     		return true;
         }
         
         if (cmd.equals("remove") && TCPermissionHandler.hasPermission(p, "templecraft.removeplayer"))
         {        	
         	Temple temple = tp.currentTemple;
     		
     		if(temple == null){
     			TempleManager.tellPlayer(p, "You need to be in a temple to use this command.");
     			return true;
     		}
         	
     		if(!tp.ownerOfSet.contains(temple.templeName) && !TCPermissionHandler.hasPermission(p, "templecraft.editall")){
     			TempleManager.tellPlayer(p, "Only the owner of the temple can use this command.");
     			return true;
     		}
     		
     		//Find player in config based on what was entered;
     		String playerName = TCUtils.getKey(TemplePlayer.config, "Players", arg);
     		
     		if(playerName == null){
     			TempleManager.tellPlayer(p, "Player not found.");
     		} else {
     			if(TCUtils.removeAccessTo(playerName, temple))
     				TempleManager.tellPlayer(p, "Removed \""+playerName+"\" from temple.");
     			else
     				TempleManager.tellPlayer(p, "\""+playerName+"\" does not have access to this temple.");
     		}
             return true;
         }
         
         //Temple commands
         String templename = args[1].toLowerCase();
         Temple temple = TCUtils.getTempleByName(templename);
         
         if(temple == null){
         	TempleManager.tellPlayer(p, "There is no temple with name " + templename);
         	return true;
         }
         
         if ((cmd.equals("join") || cmd.equals("j")) && TCPermissionHandler.hasPermission(p, "templecraft.join"))
         {
             temple.playerJoin(p);
             return true;
         }
         
         /*
         if ((cmd.equals("spectate") || cmd.equals("spec")) && TCPermissionHandler.hasPermission(p, "templecraft.spectate"))
         {
             temple.playerSpectate(p);
             return true;
         }
         */
         
         // tc forcestart [templeName]
         if (cmd.equals("forcestart") && TCPermissionHandler.hasPermission(p, "templecraft.forcestart"))
         {
             temple.forceStart(p);
             return true;
         }        
         
         if (cmd.equals("forceend") && TCPermissionHandler.hasPermission(p, "templecraft.forceend"))
         {
             temple.forceEnd(p);
             return true;
         }
         
     	return false;
     }
 }
