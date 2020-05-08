 package com.randrdevelopment.propertygroup.command.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 
 import com.randrdevelopment.propertygroup.command.BaseCommand;
 import com.randrdevelopment.propertygroup.PropertyGroup;
import com.randrdevelopment.propertygroup.PropertyGroupConfig;
 
 public class ListGroupsCommand extends BaseCommand {
	private PropertyGroupConfig propertyConfig = null;
 	
 	public ListGroupsCommand(PropertyGroup plugin) {
         super(plugin);
         name = "ListGroups";
         description = "Lists all property groups.";
         usage = "/property listgroups";
         minArgs = 0;
         maxArgs = 0;
         identifiers.add("property listgroups");
     }
 	
     @Override
     public void execute(CommandSender sender, String[] args) {
     	propertyConfig = plugin.getPropertyConfig();
 		sender.sendMessage(plugin.getTag()+"Property Groups:");
 		int i = 0;
         for (String s : propertyConfig.getKeys(false)) {
         	sender.sendMessage(ChatColor.AQUA+s);
         	i++;
         }
         if (i == 0){
         	sender.sendMessage(ChatColor.RED+"No Property Groups");
         }
     }
 }
