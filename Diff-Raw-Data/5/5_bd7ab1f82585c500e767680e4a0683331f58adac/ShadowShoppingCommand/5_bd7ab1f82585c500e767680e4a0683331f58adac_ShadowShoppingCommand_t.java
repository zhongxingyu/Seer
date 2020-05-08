 package com.shadowblox.sw123.ShadowShopping.commands;
 
 import org.bukkit.command.CommandSender;
 
import com.shadowblox.sw123.ShadowShopping.korikutils.SubCommandExecutor;
 
 
 

public class ShadowShoppingCommand extends SubCommandExecutor{
 	
 	@command
 	public void shop(CommandSender sender, String[] args){//shadowshopper shop create 
 		if(args.length >= 1){
 			if(args[0].equalsIgnoreCase("create")){
 				
 			}
 		}
 	}
 
 
 }
