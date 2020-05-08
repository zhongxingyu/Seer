 package com.tenko.cmdexe;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class CommanderCirno implements CommandExecutor {
 	
 	@Override
 	public boolean onCommand(CommandSender cs, Command c, String l, String[] args) {
 		if((c.getName().equalsIgnoreCase("attach") || c.getName().equalsIgnoreCase("detach")) && checkPermission(cs, "forcechat.attach")){
 			new AttachDetachCommand(c.getName(), cs, args);
 			return true;
 		}
 		
 		if(c.getName().equalsIgnoreCase("exe") && checkPermission(cs, "forcechat.exe")){
 			new ExeCommand(cs, args);
 			return true;
 		}
 		
 		return false;
 	}
 	
 	
 	/**
 	 * Checks permission for a command.
 	 * @param cs - The sender that we are trying to check.
 	 * @param cmd - The command.
 	 * @return Whether or not the player can use this command.
 	 */
	public boolean checkPermission(CommandSender cs, String perm) {
 		if(cs.isOp()){
 			return true;
 		}
		return cs.hasPermission(perm);
 	}
 }
