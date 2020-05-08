 /*
  * Copyright (C) 2012 Arjun Govindjee.
  */
 
 package com.carboncraft.freeze;
 
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.Command;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import java.util.logging.Logger;
 import java.util.logging.Level;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.PriorityQueue;
 import java.util.Scanner;
 
 import com.carboncraft.freeze.cmd.*;
 
 public class FreezeCommandExecutor implements CommandExecutor {
 
     private Map<String, Class> commandMap;
 
     private final Logger log = Logger.getLogger("Minecraft.Freeze");
 
     private Freeze plugin;
 
     public void mapCommand(String command, Class commandClass) {
         commandMap.put(command, commandClass);
     }
 
     public FreezeCommandExecutor(Freeze plugin) {
         commandMap = new HashMap<String, Class>();
        mapCommand("cl", ClearCommand.class);
         mapCommand("clear", ClearCommand.class);
         mapCommand("h", HelpCommand.class);
         mapCommand("help", HelpCommand.class);
         mapCommand("on", EnableCommand.class);
         mapCommand("enable", EnableCommand.class);
         mapCommand("al", PlayerLimitCommand.class);
         mapCommand("addl", PlayerLimitCommand.class);
         mapCommand("alimit", PlayerLimitCommand.class);
         mapCommand("addlimit", PlayerLimitCommand.class);
         mapCommand("s", SaveCommand.class);
         mapCommand("save", SaveCommand.class);
         mapCommand("ld", LoadCommand.class);
         mapCommand("load", LoadCommand.class);
         mapCommand("ls", ListSavedCommand.class);
         mapCommand("list", ListSavedCommand.class);
         mapCommand("rm", DeleteSaveCommand.class);
         mapCommand("delete", DeleteSaveCommand.class);
        mapCommand("fz", ListSavedCommand.class);
        mapCommand("freeze", ListSavedCommand.class);
         this.plugin = plugin;
     }
 
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         if (args.length == 0) {
             sender.sendMessage(ChatColor.RED + "No arguments given!");
             if (sender instanceof Player)
                 sender.sendMessage(ChatColor.RED + "Type \"/freeze help\" for help.");
             else
                 sender.sendMessage(ChatColor.RED + "Type \"freeze help\" for help.");
             return true;
         }
 
         PriorityQueue<CommandComponent> commandQueue = new PriorityQueue<CommandComponent>();
         int commandIndex = 0;
         for (String arg : args) {
             String[] subArgs = arg.split(":");
             if (subArgs.length > 0 && subArgs[0].length() > 0) {
                 Class command = commandMap.get(subArgs[0].toLowerCase());
                 if (command != null) {
                     try {
                         @SuppressWarnings("unchecked")
                             CommandComponent component = (CommandComponent)command.getConstructor(Freeze.class, int.class, String[].class).newInstance(plugin, commandIndex, subArgs);
                         commandQueue.add(component);
                         commandIndex++;
                     }
                     catch (Exception e) {
                         e.printStackTrace();
                     }
                 }
                 else {
                     sender.sendMessage(ChatColor.RED + "Invalid command: " + arg);
                     sender.sendMessage(ChatColor.RED + "Type \"/freeze help\" for help.");
                     return true;
                 }
             }
             else {
                 sender.sendMessage(ChatColor.RED + "Invalid command: " + arg);
                 sender.sendMessage(ChatColor.RED + "Type \"/freeze help\" for help.");
                 return true;
             }
         }
         for (CommandComponent cc : commandQueue) {
             cc.execute(sender);
         }
         return true;
     }
 }
