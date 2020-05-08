 package net.D3GN.MiracleM4n.SetSpeed;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class SSCommandExecutor implements CommandExecutor {
 	SetSpeed plugin;
 	
     public SSCommandExecutor(SetSpeed callbackPlugin) {
         plugin = callbackPlugin;
     }
 
     public boolean onCommand (CommandSender sender, Command command, String label, String[] args) {
     	if (!(sender instanceof Player))
 			return true;
 
         String cmd = command.getName();
 
     	Player player = ((Player) sender);
         String pName = player.getName();
         Double pSpeed= plugin.players.get(pName);
 
     	if (cmd.equalsIgnoreCase("setspeed")) {
             if (args.length == 0) {
                 sender.sendMessage(ChatColor.DARK_RED + "[SetSpeed Help]");
                 sender.sendMessage(ChatColor.DARK_GREEN + "/" + cmd + " 10 - sets your speed to 10x normal.");
                 sender.sendMessage(ChatColor.DARK_GREEN + "/" + cmd + " 10 *Player* - sets Player's speed to 10x normal.");
                 sender.sendMessage(ChatColor.DARK_GREEN + "/" + cmd + " 10 -world *World* - sets all player's speeds in world World to 10x normal.");
                 sender.sendMessage(ChatColor.DARK_GREEN + "/" + cmd + " 10 -world -all - sets all player's speeds to 10x normal.");
                 return true;
             }
 
             try {
 				plugin.speed = new Double(args[0]);
 				plugin.speedPerm = new Double (args[0]);
 			} catch (NumberFormatException e) {
 				player.sendMessage(ChatColor.DARK_RED + "[SetSpeed] " + (plugin.notNumber) + ".");
 				return true;
 			}
 
 			if (args.length == 1) {
 				if (plugin.checkPermissions(player, "setspeed.admin", true)) {
 					checkSpeedVars(player, plugin.speed, plugin.maxAdminSpeed);
                     return true;
 				} else if (plugin.checkPermissions(player, "setspeed.mod", true)) {
                     checkSpeedVars(player, plugin.speed, plugin.maxSpeed);
                     return true;
 				} else if (plugin.checkPermissions(player, plugin.speedPermValue, true)) {
                     checkSpeedVars(player, plugin.speed, plugin.speedPerm);
                 	return true;
 				} else {
 					player.sendMessage(ChatColor.DARK_RED + "[SetSpeed] " + (plugin.noPermissions) + ".");
 					return true;
 				}
 			} else if (args.length == 2) {
 				Player target = plugin.getServer().getPlayer(args[1]);
 
                if (target == null) {
                    player.sendMessage(ChatColor.DARK_RED + "[SetSpeed] Player " + args[1] + " Not Found.");
                    return true;
                }

 				if (plugin.checkPermissions(player, "setspeed.setothers", true)) {
                     checkSpeedVars(player, target, plugin.speed, plugin.maxOtherSpeed);
                     return true;
 				} else {
 					player.sendMessage(ChatColor.DARK_RED + "[SetSpeed] " + (plugin.noPermissions) + ".");
 					return true;
 				}
 			} else if (args.length == 3) {
                 World world = plugin.getServer().getWorld(args[2]);
                 if (plugin.checkPermissions(player, "setspeed.setworlds", true)) {
                     if (args[1].equals("-world")) {
                         if (args[1].equals("-all")) {
                             checkWorldSpeedVars(player, world, plugin.speed, plugin.maxWorldSpeed, true);
                             return true;
                         } else {
                             checkWorldSpeedVars(player, world, plugin.speed, plugin.maxWorldSpeed, false);
                             return true;
                         }
                     } else {
 						player.sendMessage(ChatColor.DARK_RED + "[SetSpeed] Please Use /setspeed # -world WORLDNAME.");
 					    return true;
                     }
 				} else {
 					player.sendMessage(ChatColor.DARK_RED + "[SetSpeed] " + (plugin.noPermissions) + ".");
 					return true;
 				}
 			}
 		    return true;
 		} else if (cmd.equalsIgnoreCase("speedoff")) {
     		if (plugin.isSpeedOn.get(pName)) {
     			plugin.isSpeedOn.put(pName, false);
                 setPlayersSpeed(player, 1, false);
             	return true;
     		}
     	} else if (cmd.equalsIgnoreCase("speedon")) {
     	    if (pSpeed != null) {
     		    plugin.isSpeedOn.put(pName, true);
                 setPlayersSpeed(player, pSpeed, true);
                 return true;
     		}
     	}
 
         return true;
 	}
 
     protected void checkSpeedVars(Player player, double speed, double maxSpeed) {
         checkSpeedVars(player, player, speed, maxSpeed);
     }
 
     protected void checkSpeedVars(Player player, Player target, double speed, double maxSpeed) {
         String tName = target.getName();
         Double tValue = plugin.players.get(tName);
         if (target == null)
             player.sendMessage(ChatColor.DARK_RED + "[SetSpeed] " + tName + " is not online" + ".");
 
 		if (plugin.speed == 1) {
 		    if (tValue != 1) {
 				plugin.speed = 1;
 				plugin.players.put(tName, plugin.speed);
                 setPlayersSpeed(target, tValue, true);
 				player.sendMessage(ChatColor.DARK_RED + "[SetSpeed] " + plugin.speedReset + ".");
 			}
 		    return;
 		}
 
         if (speed > plugin.hardMaxSpeed)
 			player.sendMessage(ChatColor.DARK_RED + "[SetSpeed] " + (plugin.tooHigh) + ".");
         else if (plugin.speed == (plugin.noSpeedValue))
 		    player.sendMessage(ChatColor.DARK_RED + "[SetSpeed] " + (plugin.noInteger) + ".");
 		else if (speed < (plugin.noSpeedValue))
 			player.sendMessage(ChatColor.DARK_RED + "[SetSpeed] " + (plugin.negativeInteger) + ".");
 		else if (speed <= maxSpeed) {
 			plugin.players.put(tName, plugin.speed);
             setPlayersSpeed(target, tValue, true);
             target.performCommand("speedon");
 
             if (target != player) {
                 player.sendMessage(ChatColor.DARK_RED + "[SetSpeed] " + tName + "'s " + (plugin.speedSet) + " " + plugin.speed + ".");
                 target.sendMessage(ChatColor.DARK_RED + "[SetSpeed] " + (plugin.speedSet) + " " + plugin.speed + ".");
             } else
                 target.sendMessage(ChatColor.DARK_RED + "[SetSpeed] " + (plugin.speedSet) + " " + plugin.speed + ".");
         } else if (speed > maxSpeed)
 			player.sendMessage(ChatColor.DARK_RED + "[SetSpeed] " + (plugin.tooHigh) + ".");
 		else
 		    player.sendMessage(ChatColor.DARK_RED + "[SetSpeed] " + (plugin.unKnown) + ".");
     }
 
     protected void checkWorldSpeedVars(Player player, World tWorld, double speed, double maxSpeed, Boolean allWorlds) {
 	    if (speed == 1) {
 		    if (allWorlds) {
 				for(Player playerList : (plugin.getServer().getOnlinePlayers())) {
                     String pLName = playerList.getName();
 				    plugin.players.put(pLName, plugin.speed);
                     setPlayersSpeed(playerList, plugin.players.get(pLName), true);
 			    }
                 plugin.getServer().broadcastMessage(ChatColor.DARK_RED + "[SetSpeed] " + plugin.speedReset + ".");
             } else if (tWorld != null) {
 				for (Player playerList : tWorld.getPlayers()) {
                     String pLName = playerList.getName();
                     plugin.players.put(pLName, plugin.speed);
                     setPlayersSpeed(playerList, plugin.players.get(pLName), true);
 					playerList.sendMessage(ChatColor.DARK_RED + "[SetSpeed] " + plugin.speedReset + ".");
 				}
 				player.sendMessage(ChatColor.DARK_RED + "[SetSpeed] " + plugin.speedReset + " in " + tWorld.getName() + ".");
 			} else
 			    player.sendMessage(ChatColor.DARK_RED + "[SetSpeed] World Not Found.");
 		}
 
         if (speed > plugin.hardMaxSpeed)
 			player.sendMessage(ChatColor.DARK_RED + "[SetSpeed] " + (plugin.tooHigh) + ".");
         else if (plugin.speed == (plugin.noSpeedValue))
 		    player.sendMessage(ChatColor.DARK_RED + "[SetSpeed] " + (plugin.noInteger) + ".");
 		else if (speed < (plugin.noSpeedValue))
 			player.sendMessage(ChatColor.DARK_RED + "[SetSpeed] " + (plugin.negativeInteger) + ".");
 		else if (speed <= maxSpeed) {
 			if (allWorlds) {
 				for(Player playerList : (plugin.getServer().getOnlinePlayers())) {
                     String pLName = playerList.getName();
 				    plugin.players.put(pLName, plugin.speed);
                     setPlayersSpeed(playerList, plugin.players.get(pLName), true);
                     playerList.performCommand("speedon");
 			    }
                 plugin.getServer().broadcastMessage(ChatColor.DARK_RED + "[SetSpeed] Speed Set To " + plugin.speed + " For All Players On The Server.");
             } else if (tWorld != null) {
 				for (Player playerList : tWorld.getPlayers()) {
                     String pLName = playerList.getName();
                     plugin.players.put(pLName, plugin.speed);
                     setPlayersSpeed(playerList, plugin.players.get(pLName), true);
                     playerList.performCommand("speedon");
                     playerList.sendMessage(ChatColor.DARK_RED + "[SetSpeed] Speed Set To " + plugin.speed + " For All Players In Your World.");
 				}
                 player.sendMessage(ChatColor.DARK_RED + "[SetSpeed] Speed Set To " + plugin.speed + " For All Players In " + tWorld.getName() + ".");
 			} else
 			    player.sendMessage(ChatColor.DARK_RED + "[SetSpeed] World Not Found.");
         } else if (speed > maxSpeed)
 			player.sendMessage(ChatColor.DARK_RED + "[SetSpeed] " + (plugin.tooHigh) + ".");
 		else
 		    player.sendMessage(ChatColor.DARK_RED + "[SetSpeed] " + (plugin.unKnown) + ".");
     }
 
     protected void setPlayersSpeed(Player player, double speed, Boolean flyCheck) {
         SpoutPlayer sPlayer = (SpoutPlayer)player;
         sPlayer.setWalkingMultiplier(speed);
         sPlayer.setSwimmingMultiplier(speed);
         //sPlayer.setGravityMultiplier(1/speed);
         sPlayer.setAirSpeedMultiplier(speed);
         sPlayer.setCanFly(flyCheck);
     }
 }
 
