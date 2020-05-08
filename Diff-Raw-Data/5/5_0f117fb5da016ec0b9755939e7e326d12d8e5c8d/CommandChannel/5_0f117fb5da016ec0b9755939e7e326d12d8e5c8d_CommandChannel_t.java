 package com.mcnsa.mcnsachat2.commands;
 
 import com.mcnsa.mcnsachat2.MCNSAChat2;
 
 import org.bukkit.command.CommandSender;
 
 public class CommandChannel implements Command {
 	MCNSAChat2 plugin = null;
 	public CommandChannel(MCNSAChat2 instance) {
 		plugin = instance;
 	}
 
 	public boolean onCommand(CommandSender sender, String[] args) {
 		return true;
 	}
 
 	public String requiredPermission() {
 		return "user.channel";
 	}
 
 	public String getCommand() {
		return "ch";
 	}
 
 	public String getArguments() {
		return "<channel>";
 	}
 
 	public String getDescription() {
 		return "takes you to your chosen channel, creates one if it doesn't exist";
 	}
 }
