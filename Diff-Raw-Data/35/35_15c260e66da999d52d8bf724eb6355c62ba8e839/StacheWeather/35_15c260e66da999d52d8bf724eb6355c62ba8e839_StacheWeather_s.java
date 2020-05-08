 package com.ACStache.StacheStash;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class StacheWeather
 {
     private static Logger log = Logger.getLogger("Minecraft");
     
     public static void weatherChanger(CommandSender sender, String[] args)
     {
         Player player = null;
         if(sender instanceof Player) //if sender is a player, set player
             player = (Player)sender;
         
         if(args.length < 1) //if player or console typed '/weather' or 'weather'
         {
             weatherCmds(sender); //display weather commands
         }
         else if(args[0].equalsIgnoreCase("set"))
         {
             if(sender instanceof Player && StacheStash.has(player, "StacheStash.WeatherSet")) //is a player and has permission
             {
                 if(args[1].equalsIgnoreCase("normal") || args[1].equalsIgnoreCase("rain") || args[1].equalsIgnoreCase("lightning"))
                 {
                     if(args.length < 3) //no world specified, defaults to player's current world
                     {
                         World pWorld = player.getWorld();
                         if(args[1].equalsIgnoreCase("normal"))
                             pWorld.setStorm(false); //set weather to sunny
                         else if(args[1].equalsIgnoreCase("rain"))
                             pWorld.setStorm(true); //set weather to rain
                         else if(args[1].equalsIgnoreCase("lightning"))
                         {
                             pWorld.setStorm(true); //set weather to rain
                             pWorld.setThundering(true); //then add thunder & lightning
                         }
                         else
                             player.sendMessage(ChatColor.RED + "You need to specify a weather condition");
                     }
                     else //changes weather in world specified
                     {
                         World cWorld = Bukkit.getServer().getWorld(args[2]); //get world from server
                         if(cWorld != null) //if world is not null
                         {
                             if(args[1].equalsIgnoreCase("normal"))
                                 cWorld.setStorm(false); //set weather to sunny
                             else if(args[1].equalsIgnoreCase("rain"))
                                 cWorld.setStorm(true); //set weather to rain
                             else if(args[1].equalsIgnoreCase("lightning"))
                             {
                                 cWorld.setStorm(true); //set weather to rain
                                 cWorld.setThundering(true); //then add thunder & lightning
                             }
                             else
                                 player.sendMessage(ChatColor.RED + "You need to specify a weather condition");
                         }
                         else //world typed incorrectly
                         {
                             player.sendMessage(ChatColor.RED + "You need to specify an existing world.");                                    
                         }
                     }
                 }
                 else //messed up command for setting weather
                 {
                     player.sendMessage(ChatColor.RED + "You need to specify a weather condition (normal/rain/lightning)");
                 }
             }
             else //console or no permission
             {
                 if(!(sender instanceof Player)) //if console
                 {
                     if(args.length < 3) //no world specified, console must specify a world
                     {
                         log.info("[StacheStash] You must specify an existing world");
                     }
                     else //changes weather in world specified
                     {
                         World cWorld = Bukkit.getServer().getWorld(args[2]); //get world from server
                         if(cWorld != null) //if world is not null
                         {
                             if(args[1].equalsIgnoreCase("normal"))
                                 cWorld.setStorm(false); //set weather to sunny
                             else if(args[1].equalsIgnoreCase("rain"))
                                 cWorld.setStorm(true); //set weather to rain
                             else if(args[1].equalsIgnoreCase("lightning"))
                             {
                                 cWorld.setStorm(true); //set weather to rain
                                 cWorld.setThundering(true); //then add thunder & lightning
                             }
                             else
                                 log.info("[StacheStash] You need to specify a weather condition (normal/rain/lightning)");
                         }
                         else //world typed incorrectly
                         {
                             log.info("[StacheStash] You must specify an existing world");                                    
                         }
                     }
                 }
                 else //no permission
                 {
                     player.sendMessage(ChatColor.RED + "You don't have Permission to do that");
                 }
             }
         }
         else if(args[0].equalsIgnoreCase("get")) //get weather condition
         {
             if(sender instanceof Player && StacheStash.has(player, "StacheStash.WeatherGet")) //is player and has permission
             {
                 if(args.length < 2) //only arg is get
                 {
                    World pWorld = Bukkit.getServer().getWorld(player.getName()); //get player's current world
                     if(pWorld.hasStorm()) //if there's a storm
                     {
                         if(pWorld.isThundering()) //check for thundering
                            player.sendMessage(ChatColor.GREEN + "The weather is thunder & lightning");
                         else //no thundering
                            player.sendMessage(ChatColor.GREEN + "The weather is rain");
                     }
                     else //no rain, must be sunny
                     {
                        player.sendMessage(ChatColor.GREEN + "The weather is sunny");
                     }
                 }
                 else //world specified
                 {
                     World cWorld = Bukkit.getServer().getWorld(args[1]);
                     if(cWorld != null) //checks world is not null
                         if(cWorld.hasStorm()) //if there's a storm
                         {
                             if(cWorld.isThundering()) //check for thundering
                                player.sendMessage(ChatColor.GREEN + "The weather in " + cWorld.getName() + " is thunder & lightning");
                             else //no thundering
                                player.sendMessage(ChatColor.GREEN + "The weather in " + cWorld.getName() + " is rain");
                         }
                         else //no rain, must be sunny
                         {
                            player.sendMessage(ChatColor.GREEN + "The weather in " + cWorld.getName() + " is sunny");
                         }
                     else //world typed incorrectly
                     {
                         player.sendMessage(ChatColor.RED + "You need to specify an existing world");
                     }
                 }
             }
             else //console or no permission
             {
                 if(!(sender instanceof Player)) //console
                 {
                     if(args.length < 2) //check to see if any world might be specified
                         log.info("[StacheStash] You need to specify an existing world");
                     else //world of some sort is specified
                     {
                         World cWorld = Bukkit.getServer().getWorld(args[1]);
                         if(cWorld != null) //checks world is not null
                             if(cWorld.hasStorm()) //if there's a storm
                             {
                                 if(cWorld.isThundering()) //check for thundering
                                    log.info("[StacheStash] The weather in " + cWorld.getName() + " is thunder & lightning");
                                 else //no thundering
                                    log.info("[StacheStash] The weather in " + cWorld.getName() + " is rain");
                             }
                             else //no rain, must be sunny
                             {
                                log.info("[StacheStash] The weather in " + cWorld.getName() + " is sunny");
                             }
                         else //world typed incorrectly
                         {
                             log.info("[StacheStash] You need to specify an existing world");
                         }
                     }
                 }
                 else //no permission
                 {
                     player.sendMessage(ChatColor.RED + "You don't have Permission to do that");
                 }
             }
         }
         else
         {
             if(sender instanceof Player) //player typed '/weather (jibberish)'
             {
                 player.sendMessage("That command is not recognized");
                 weatherCmds(sender);
             }
             else //console typed 'weather (jibberish)'
             {
                 log.info("[StacheStash] That command is not recognized");
                 weatherCmds(sender);
             }
         }
     }
     
     private static void weatherCmds(CommandSender sender)
     {
         if(sender instanceof Player) //send command syntax to player
         {
             Player player = (Player)sender;
             player.sendMessage(ChatColor.GREEN + "Any parameters in " + ChatColor.RED + "[]" + ChatColor.GREEN + "'s are optional");
             player.sendMessage(ChatColor.GREEN + "/weather set (normal/rain/lightning) " + ChatColor.RED + "[worldname]");
             player.sendMessage(ChatColor.GREEN + "/weather get " + ChatColor.RED + "[worldname]");
         }
         else //send command syntax to console
         {
             log.info("[StacheStash] Please enter one of the following commands:");
             log.info("[StacheStash] 'weather set (normal/rain/lightning) worldname'");
             log.info("[StacheStash] 'weather get worldname'");
         }
     }
 }
