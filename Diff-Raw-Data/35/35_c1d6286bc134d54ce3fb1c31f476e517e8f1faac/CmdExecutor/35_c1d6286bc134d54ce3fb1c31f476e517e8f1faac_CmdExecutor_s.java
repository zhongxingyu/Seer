 package org.jakub1221.herobrineai.commands;
 
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.jakub1221.herobrineai.HerobrineAI;
 import org.jakub1221.herobrineai.AI.AICore;
 import org.jakub1221.herobrineai.AI.Core.CoreType;
 
 public class CmdExecutor implements CommandExecutor{
 
 	private HerobrineAI P_Core = null;
 	private Logger log = null;
 	
 	public CmdExecutor(HerobrineAI i){
 		P_Core=i;
 		log=i.log;
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 			
 			if (sender instanceof Player){
 			Player player = (Player) sender;
 			if (player.isOp()){
 			if (args.length>0){
             if (args[0].equalsIgnoreCase("attack")){
 					if (args.length>1){
 						if (Bukkit.getServer().getPlayer(args[1])!=null){
 						if (Bukkit.getServer().getPlayer(args[1]).isOnline()){
 							if (P_Core.getSupport().checkAttack(Bukkit.getServer().getPlayer(args[1]).getLocation())){
 								if (P_Core.canAttackPlayer(Bukkit.getServer().getPlayer(args[1]), player)){
 								if (AICore.isTarget==false){
 								P_Core.getAICore().setAttackTarget(Bukkit.getServer().getPlayer(args[1]));
 								player.sendMessage(ChatColor.RED+"[HerobrineAI] Herobrine is now attacking the "+args[1]+"!");
 							}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Herobrine already has target! Use "+ChatColor.GREEN+"/hb-ai cancel"+ChatColor.RED+" to cancel actual target");}
 								}
 							}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is in secure area.");}
 							}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 						}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 					}else{player.sendMessage(ChatColor.RED+"Usage: "+ChatColor.GREEN+"/hb-ai attack <player name>");}
 				}else if (args[0].equalsIgnoreCase("pyramid")){
 					if (args.length>1){
 						if (Bukkit.getServer().getPlayer(args[1])!=null){
 						if (Bukkit.getServer().getPlayer(args[1]).isOnline()){
 							if (P_Core.getSupport().checkBuild(Bukkit.getServer().getPlayer(args[1]).getLocation())){
                                       Object[] data = {Bukkit.getServer().getPlayer(args[1])};
 								if(P_Core.getAICore().getCore(CoreType.PYRAMID).RunCore(data).getResult()){
 								player.sendMessage(ChatColor.RED+"[HerobrineAI] Creating pyramind near "+args[1]+"!");
 								}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Cannot find good place for pyramid!");}
 							}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is in secure area.");}
 							}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 						}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 					}else{player.sendMessage(ChatColor.RED+"Usage: "+ChatColor.GREEN+"/hb-ai pyramid <player name>");}
 				}
 				else if (args[0].equalsIgnoreCase("bury")){
 					if (args.length>1){
 						if (Bukkit.getServer().getPlayer(args[1])!=null){
 						if (Bukkit.getServer().getPlayer(args[1]).isOnline()){
 							if (P_Core.getSupport().checkHaunt(Bukkit.getServer().getPlayer(args[1]).getLocation())){
 								 Object[] data = {Bukkit.getServer().getPlayer(args[1])};
 								if(P_Core.getAICore().getCore(CoreType.BURY_PLAYER).RunCore(data).getResult()){
 								player.sendMessage(ChatColor.RED+"[HerobrineAI] Buried "+args[1]+"!");
 								
 								}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Cannot find good place!");}
 							}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is in secure area.");}
 							}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 						}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 					}else{player.sendMessage(ChatColor.RED+"Usage: "+ChatColor.GREEN+"/hb-ai bury <player name>");}
 				}else if (args[0].equalsIgnoreCase("temple")){
 					if (args.length>1){
 						
 						if (Bukkit.getServer().getPlayer(args[1])!=null){
 						if (Bukkit.getServer().getPlayer(args[1]).isOnline()){
 							if (P_Core.getSupport().checkBuild(Bukkit.getServer().getPlayer(args[1]).getLocation())){
 								 Object[] data = {Bukkit.getServer().getPlayer(args[1])};
 								if(P_Core.getAICore().getCore(CoreType.TEMPLE).RunCore(data).getResult()){
 									player.sendMessage(ChatColor.RED+"[HerobrineAI] Creating temple near "+args[1]+"!");
 								}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Cannot find good place for temple!");}
 							}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is in secure area.");}
 							}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 						}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 						
 						}else{player.sendMessage(ChatColor.RED+"Usage: "+ChatColor.GREEN+"/hb-ai temple <player name>");}
 				}else if (args[0].equalsIgnoreCase("heads")){
 					if (args.length>1){
 						
 						if (Bukkit.getServer().getPlayer(args[1])!=null){
 							Object[] data = {args[1]};
 							player.sendMessage(ChatColor.RED+"[HerobrineAI] "+P_Core.getAICore().getCore(CoreType.HEADS).RunCore(data).getResultString());
 						}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 						
 						}else{player.sendMessage(ChatColor.RED+"Usage: "+ChatColor.GREEN+"/hb-ai heads <player name>");}
 				}else if (args[0].equalsIgnoreCase("cave")){
 					if (args.length>1){
 						
 						if (Bukkit.getServer().getPlayer(args[1])!=null){
 							Object[] data = {Bukkit.getServer().getPlayer(args[1]).getLocation(),true};
 							player.sendMessage(ChatColor.RED+"[HerobrineAI] "+P_Core.getAICore().getCore(CoreType.BUILD_STUFF).RunCore(data).getResultString());
 						}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 						
 						}else{player.sendMessage(ChatColor.RED+"Usage: "+ChatColor.GREEN+"/hb-ai cave <player name>");}
 				}else if (args[0].equalsIgnoreCase("burn")){
 					if (args.length>1){
 						
 						if (Bukkit.getServer().getPlayer(args[1])!=null){
 							Object[] data = {Bukkit.getServer().getPlayer(args[1])};
 							player.sendMessage(ChatColor.RED+"[HerobrineAI] "+P_Core.getAICore().getCore(CoreType.BURN).RunCore(data).getResultString());
 						}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 						
 						}else{player.sendMessage(ChatColor.RED+"Usage: "+ChatColor.GREEN+"/hb-ai burn <player name>");}
 				}else if (args[0].equalsIgnoreCase("curse")){
 					if (args.length>1){
 						
 						if (Bukkit.getServer().getPlayer(args[1])!=null){
 							Object[] data = {Bukkit.getServer().getPlayer(args[1])};
 							player.sendMessage(ChatColor.RED+"[HerobrineAI] "+P_Core.getAICore().getCore(CoreType.CURSE).RunCore(data).getResultString());
 						}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 						
 						}else{player.sendMessage(ChatColor.RED+"Usage: "+ChatColor.GREEN+"/hb-ai curse <player name>");}
 				}
 				else if (args[0].equalsIgnoreCase("graveyard")){
       					if (args.length>1){
       						if (Bukkit.getServer().getPlayer(args[1])!=null){
       						if (Bukkit.getServer().getPlayer(args[1]).isOnline()){
       							if (AICore.isTarget==false){
       								P_Core.getAICore().GraveyardTeleport(Bukkit.getServer().getPlayer(args[1]));
       								player.sendMessage(ChatColor.RED+"[HerobrineAI] "+args[1]+" is now in the Graveyard world!");
       							}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Herobrine already has target! Use "+ChatColor.GREEN+"/hb-ai cancel"+ChatColor.RED+" to cancel actual target");}
       						}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
       						}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
       					}else{player.sendMessage(ChatColor.RED+"Usage: "+ChatColor.GREEN+"/hb-ai graveyard <player name>");}
       				}else if (args[0].equalsIgnoreCase("haunt")){
 					
 					if (args.length>1){
 						if (Bukkit.getServer().getPlayer(args[1])!=null){
 						if (Bukkit.getServer().getPlayer(args[1]).isOnline()){
 							if (P_Core.canAttackPlayer(Bukkit.getServer().getPlayer(args[1]), player)){
 							if (AICore.isTarget==false){
 								P_Core.getAICore().setHauntTarget(Bukkit.getServer().getPlayer(args[1]));
 								player.sendMessage(ChatColor.RED+"[HerobrineAI] Herobrine now haunts the "+args[1]+"!");
 							}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Herobrine already has target! Use "+ChatColor.GREEN+"/hb-ai cancel"+ChatColor.RED+" to cancel actual target.");}
 							}
 							}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 						}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 					}else{player.sendMessage(ChatColor.RED+"Usage: "+ChatColor.GREEN+"/hb-ai haunt <player name>");}
 					
 				}else if (args[0].equalsIgnoreCase("cancel")){
 					
 					P_Core.getAICore().CancelTarget(CoreType.ANY);
 					player.sendMessage(ChatColor.RED+"[HerobrineAI] Target cancelled!");
 					
 				}else if (args[0].equalsIgnoreCase("reload")){
 					
 				
 					
 					P_Core.getConfigDB().Reload();
 					
 					player.sendMessage(ChatColor.RED+"[HerobrineAI] Config reloaded!");
 					
 				}else if (args[0].equalsIgnoreCase("help")){
 				
 					player.sendMessage(ChatColor.RED+"[HerobrineAI] Command list");
 					player.sendMessage(ChatColor.GREEN+"/hb-ai help - shows all commands");
 					player.sendMessage(ChatColor.GREEN+"/hb-ai attack <player name> - herobrine attacks the player");
 					player.sendMessage(ChatColor.GREEN+"/hb-ai haunt <player name> - herobrine haunts the player");
 					player.sendMessage(ChatColor.GREEN+"/hb-ai cancel - cancel herobrines actual target");
 					player.sendMessage(ChatColor.GREEN+"/hb-ai reload - reload config");
 					player.sendMessage(ChatColor.GREEN+"/hb-ai position - gets actual position of Herobrine");
 					player.sendMessage(ChatColor.GREEN+"/hb-ai pyramid <player name> - build pyramid near the player");
 					player.sendMessage(ChatColor.GREEN+"/hb-ai bury <player name> - bury player");
 					player.sendMessage(ChatColor.GREEN+"/hb-ai graveyard <player name> - teleport player to the Graveyard world");
 					player.sendMessage(ChatColor.GREEN+"/hb-ai temple <player name> - build temple near the player");
 					player.sendMessage(ChatColor.GREEN+"/hb-ai heads <player name> - place heads near the player");
 					player.sendMessage(ChatColor.GREEN+"/hb-ai cave <player name> - create cave near the player");
 					player.sendMessage(ChatColor.GREEN+"/hb-ai burn <player name> - burn player");
 					player.sendMessage(ChatColor.GREEN+"/hb-ai curse <player name> - curse player");
 					
 				}else if (args[0].equalsIgnoreCase("position")){
 						
 						player.sendMessage(ChatColor.RED+"[HerobrineAI] Position");
 						player.sendMessage(ChatColor.RED+"World: "+P_Core.HerobrineNPC.getBukkitEntity().getLocation().getWorld().getName()+" X: "+((int)P_Core.HerobrineNPC.getBukkitEntity().getLocation().getX())+
 								" Y: "+((int)P_Core.HerobrineNPC.getBukkitEntity().getLocation().getY())+" Z: "+((int)P_Core.HerobrineNPC.getBukkitEntity().getLocation().getZ()));
 						player.sendMessage(ChatColor.RED+"InWalkingMode: "+AICore.getStringWalkingMode());
 						player.sendMessage(ChatColor.RED+"Available World: "+HerobrineAI.getPluginCore().getAvailableWorldString());
 				}else{player.sendMessage(ChatColor.RED+"Usage: /hb-ai help");}
 			}else{player.sendMessage(ChatColor.RED+"Usage: /hb-ai help");}
 			}else{
 				/* PERMISSION NODE */
 			
 				if (args.length>0){
 		            if (args[0].equalsIgnoreCase("attack")){
 							if (args.length>1){
 								if (player.hasPermission("hb-ai.attack")){
 								if (Bukkit.getServer().getPlayer(args[1])!=null){
 								if (Bukkit.getServer().getPlayer(args[1]).isOnline()){
 									if (P_Core.getSupport().checkAttack(Bukkit.getServer().getPlayer(args[1]).getLocation())){
 									if (P_Core.canAttackPlayer(Bukkit.getServer().getPlayer(args[1]), player)){
 									if (AICore.isTarget==false){
 										P_Core.getAICore().setAttackTarget(Bukkit.getServer().getPlayer(args[1]));
 										player.sendMessage(ChatColor.RED+"[HerobrineAI] Herobrine is now attacking the "+args[1]+"!");
 									
 									}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Herobrine already has target! Use "+ChatColor.GREEN+"/hb-ai cancel"+ChatColor.RED+" to cancel actual target");}
 									
 									}
 									}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is in secure area.");}
 									}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 								
 								}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 								}else{player.sendMessage(ChatColor.RED+"You dont have permissions to do that.");}
 							}else{player.sendMessage(ChatColor.RED+"Usage: "+ChatColor.GREEN+"/hb-ai attack <player name>");}
 						}else if (args[0].equalsIgnoreCase("pyramid")){
 							if (args.length>1){
 								if(player.hasPermission("hb-ai.pyramid")){
 								if (Bukkit.getServer().getPlayer(args[1])!=null){
 								if (Bukkit.getServer().getPlayer(args[1]).isOnline()){
 									if (P_Core.getSupport().checkBuild(Bukkit.getServer().getPlayer(args[1]).getLocation())){
 										Object[] data = {Bukkit.getServer().getPlayer(args[1])};
 										if(P_Core.getAICore().getCore(CoreType.PYRAMID).RunCore(data).getResult()){
 										player.sendMessage(ChatColor.RED+"[HerobrineAI] Creating pyramind near "+args[1]+"!");
 										}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Cannot find good place for pyramid!");}
 									}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is in secure area.");}
 									}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 								}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 							}else{player.sendMessage(ChatColor.RED+"You dont have permissions to do that.");}
 							}else{player.sendMessage(ChatColor.RED+"Usage: "+ChatColor.GREEN+"/hb-ai pyramid <player name>");}
 						}else if (args[0].equalsIgnoreCase("temple")){
 							if (args.length>1){
 								if(player.hasPermission("hb-ai.temple")){
 								if (Bukkit.getServer().getPlayer(args[1])!=null){
 								if (Bukkit.getServer().getPlayer(args[1]).isOnline()){
 									if (P_Core.getSupport().checkBuild(Bukkit.getServer().getPlayer(args[1]).getLocation())){
 										Object[] data = {Bukkit.getServer().getPlayer(args[1])};
 										if(P_Core.getAICore().getCore(CoreType.TEMPLE).RunCore(data).getResult()){
 											player.sendMessage(ChatColor.RED+"[HerobrineAI] Creating temple near "+args[1]+"!");
 										}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Cannot find good place for temple!");}
 									}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is in secure area.");}
 									}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 								}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 								}else{player.sendMessage(ChatColor.RED+"You dont have permissions to do that.");}
 								}else{player.sendMessage(ChatColor.RED+"Usage: "+ChatColor.GREEN+"/hb-ai temple <player name>");}
 						}else if (args[0].equalsIgnoreCase("bury")){
 							
 							if (args.length>1){
 								if (player.hasPermission("hb-ai.bury")){
 								if (Bukkit.getServer().getPlayer(args[1])!=null){
 								if (Bukkit.getServer().getPlayer(args[1]).isOnline()){
 									if (P_Core.getSupport().checkBuild(Bukkit.getServer().getPlayer(args[1]).getLocation())){
 										Object[] data = {Bukkit.getServer().getPlayer(args[1])};
 										if(P_Core.getAICore().getCore(CoreType.BURY_PLAYER).RunCore(data).getResult()){
 										player.sendMessage(ChatColor.RED+"[HerobrineAI] Buried "+args[1]+"!");
 										}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Cannot find good place!");}
 									}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is in secure area.");}
 									}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 								}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 							}else{player.sendMessage(ChatColor.RED+"You dont have permissions to do that.");}
 							}else{player.sendMessage(ChatColor.RED+"Usage: "+ChatColor.GREEN+"/hb-ai bury <player name>");}
 						}
 						else if (args[0].equalsIgnoreCase("cave")){
 							if (args.length>1){
 								if (player.hasPermission("hb-ai.cave")){
 								
 								if (Bukkit.getServer().getPlayer(args[1])!=null){
 									Object[] data = {Bukkit.getServer().getPlayer(args[1]).getLocation(),true};
 									player.sendMessage(ChatColor.RED+"[HerobrineAI] "+P_Core.getAICore().getCore(CoreType.BUILD_STUFF).RunCore(data).getResultString());
 								}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 								}else{player.sendMessage(ChatColor.RED+"You dont have permissions to do that.");}
 								}else{player.sendMessage(ChatColor.RED+"Usage: "+ChatColor.GREEN+"/hb-ai cave <player name>");}
 						}else if (args[0].equalsIgnoreCase("burn")){
 							if (args.length>1){
 								if (player.hasPermission("hb-ai.burn")){
 								
 								if (Bukkit.getServer().getPlayer(args[1])!=null){
 									Object[] data = {Bukkit.getServer().getPlayer(args[1])};
 									player.sendMessage(ChatColor.RED+"[HerobrineAI] "+P_Core.getAICore().getCore(CoreType.BURN).RunCore(data).getResultString());
 								}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 								}else{player.sendMessage(ChatColor.RED+"You dont have permissions to do that.");}
 								}else{player.sendMessage(ChatColor.RED+"Usage: "+ChatColor.GREEN+"/hb-ai burn <player name>");}
 						}else if (args[0].equalsIgnoreCase("curse")){
 							if (args.length>1){
 								if (player.hasPermission("hb-ai.curse")){
 								
 								if (Bukkit.getServer().getPlayer(args[1])!=null){
 									Object[] data = {Bukkit.getServer().getPlayer(args[1])};
 									player.sendMessage(ChatColor.RED+"[HerobrineAI] "+P_Core.getAICore().getCore(CoreType.CURSE).RunCore(data).getResultString());
 								}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 								}else{player.sendMessage(ChatColor.RED+"You dont have permissions to do that.");}
 								}else{player.sendMessage(ChatColor.RED+"Usage: "+ChatColor.GREEN+"/hb-ai curse <player name>");}
 						}else if (args[0].equalsIgnoreCase("heads")){
 							if (args.length>1){
 								if (player.hasPermission("hb-ai.heads")){
 								if (Bukkit.getServer().getPlayer(args[1])!=null){
 									Object[] data = {args[1]};
 									player.sendMessage(ChatColor.RED+"[HerobrineAI] "+P_Core.getAICore().getCore(CoreType.HEADS).RunCore(data).getResultString());
 								}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 								}else{player.sendMessage(ChatColor.RED+"You dont have permissions to do that.");}
 								}else{player.sendMessage(ChatColor.RED+"Usage: "+ChatColor.GREEN+"/hb-ai heads <player name>");}
 						}
 						else if (args[0].equalsIgnoreCase("graveyard")){
 		      					if (args.length>1){
 		      						if (player.hasPermission("hb-ai.graveyard")){
 		      						if (Bukkit.getServer().getPlayer(args[1])!=null){
 		      						if (Bukkit.getServer().getPlayer(args[1]).isOnline()){
 		      							if (AICore.isTarget==false){
 		      								P_Core.getAICore().GraveyardTeleport(Bukkit.getServer().getPlayer(args[1]));
 		      								player.sendMessage(ChatColor.RED+"[HerobrineAI] "+args[1]+" is now in the Graveyard world!");
 		      							}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Herobrine already has target! Use "+ChatColor.GREEN+"/hb-ai cancel"+ChatColor.RED+" to cancel actual target");}
 		      						}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 		      						}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 		      						}else{player.sendMessage(ChatColor.RED+"You dont have permissions to do that.");}
 		      					}else{player.sendMessage(ChatColor.RED+"Usage: "+ChatColor.GREEN+"/hb-ai graveyard <player name>");}
 		      				}else if (args[0].equalsIgnoreCase("haunt")){
 							
 							if (args.length>1){
 								if (player.hasPermission("hb-ai.haunt")){
 								if (Bukkit.getServer().getPlayer(args[1])!=null){
 								if (Bukkit.getServer().getPlayer(args[1]).isOnline()){
 									if (P_Core.getSupport().checkHaunt(Bukkit.getServer().getPlayer(args[1]).getLocation())){
 									if (P_Core.canAttackPlayer(Bukkit.getServer().getPlayer(args[1]), player)){
 									if (AICore.isTarget==false){
 										P_Core.getAICore().setHauntTarget(Bukkit.getServer().getPlayer(args[1]));
 										player.sendMessage(ChatColor.RED+"[HerobrineAI] Herobrine now haunts the "+args[1]+"!");
 									
 									}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Herobrine already has target! Use "+ChatColor.GREEN+"/hb-ai cancel"+ChatColor.RED+" to cancel actual target.");}
 									}
 									}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is in secure area.");}
 									}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 								}else{player.sendMessage(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 								}else{player.sendMessage(ChatColor.RED+"You dont have permissions to do that.");}
 							}else{player.sendMessage(ChatColor.RED+"Usage: "+ChatColor.GREEN+"/hb-ai haunt <player name>");}
 							
 						}else if (args[0].equalsIgnoreCase("cancel")){
 							if (player.hasPermission("hb-ai.cancel")){
 								P_Core.getAICore().CancelTarget(CoreType.ANY);
 							player.sendMessage(ChatColor.RED+"[HerobrineAI] Target cancelled!");
 							}else{player.sendMessage(ChatColor.RED+"You dont have permissions to do that.");}
 						}else if (args[0].equalsIgnoreCase("reload")){
 							if (player.hasPermission("hb-ai.reload")){
 							
 								P_Core.getConfigDB().Reload();
 							
 							player.sendMessage(ChatColor.RED+"[HerobrineAI] Config reloaded!");
 							}else{player.sendMessage(ChatColor.RED+"You dont have permissions to do that.");}
 						}else if (args[0].equalsIgnoreCase("help")){
 							if (player.hasPermission("hb-ai.help")){
 							player.sendMessage(ChatColor.RED+"[HerobrineAI] Command list");
 							player.sendMessage(ChatColor.GREEN+"/hb-ai help - shows all commands");
 							player.sendMessage(ChatColor.GREEN+"/hb-ai attack <player name> - herobrine attacks the player");
 							player.sendMessage(ChatColor.GREEN+"/hb-ai haunt <player name> - herobrine haunts the player");
 							player.sendMessage(ChatColor.GREEN+"/hb-ai cancel - cancel herobrines actual target");
 							player.sendMessage(ChatColor.GREEN+"/hb-ai reload - reload config");
 							player.sendMessage(ChatColor.GREEN+"/hb-ai position - gets actual position of Herobrine");
 							player.sendMessage(ChatColor.GREEN+"/hb-ai pyramid <player name> - build pyramid near the player");
 							player.sendMessage(ChatColor.GREEN+"/hb-ai bury <player name> - bury player");
 							player.sendMessage(ChatColor.GREEN+"/hb-ai graveyard <player name> - teleport player to the Graveyard world");
 							player.sendMessage(ChatColor.GREEN+"/hb-ai temple <player name> - build temple near the player");
 							player.sendMessage(ChatColor.GREEN+"/hb-ai heads <player name> - place heads near the player");
 							player.sendMessage(ChatColor.GREEN+"/hb-ai cave <player name> - create cave near the player");
 							player.sendMessage(ChatColor.GREEN+"/hb-ai burn <player name> - burn player");
							player.sendMessage(ChatColor.GREEN+"/hb-ai cuse <player name> - burn player");
 							
 							}else{player.sendMessage(ChatColor.RED+"You dont have permissions to do that.");}
 						}else if (args[0].equalsIgnoreCase("position")){
 							if (player.hasPermission("hb-ai.position")){
 							player.sendMessage(ChatColor.RED+"[HerobrineAI] Position");
 							player.sendMessage(ChatColor.RED+"World: "+P_Core.HerobrineNPC.getBukkitEntity().getLocation().getWorld().getName()+" X: "+((int)P_Core.HerobrineNPC.getBukkitEntity().getLocation().getX())+
 									" Y: "+((int)P_Core.HerobrineNPC.getBukkitEntity().getLocation().getY())+" Z: "+((int)P_Core.HerobrineNPC.getBukkitEntity().getLocation().getZ()));
 							player.sendMessage(ChatColor.RED+"InWalkingMode: "+AICore.getStringWalkingMode());
 							player.sendMessage(ChatColor.RED+"Available World: "+HerobrineAI.getPluginCore().getAvailableWorldString());
 					}else{player.sendMessage(ChatColor.RED+"You dont have permissions to do that.");}
 						}else{player.sendMessage(ChatColor.RED+"Usage: /hb-ai help");}
 					}else{player.sendMessage(ChatColor.RED+"Usage: /hb-ai help");}
 				
 				
 			
 			}
 			return true;
 		}else{
 			// CONSOLE
 			if (args.length>0){
 	            if (args[0].equalsIgnoreCase("attack")){
 						if (args.length>1){
 							if (Bukkit.getServer().getPlayer(args[1])!=null){
 							if (Bukkit.getServer().getPlayer(args[1]).isOnline()){
 								if (P_Core.getSupport().checkAttack(Bukkit.getServer().getPlayer(args[1]).getLocation())){
 								if (P_Core.canAttackPlayerConsole(Bukkit.getServer().getPlayer(args[1]))){
 								if (AICore.isTarget==false){
 									P_Core.getAICore().setAttackTarget(Bukkit.getServer().getPlayer(args[1]));
 									log.info("[HerobrineAI] Herobrine is now attacking the "+args[1]+"!");
 								}else{log.info("[HerobrineAI] Herobrine already has target! Use /hb-ai cancel to cancel actual target");}
 								}
 								}else{log.info("[HerobrineAI] Player is in secured area.");}
 								}else{log.info("[HerobrineAI] Player is offline.");}
 							}else{log.info("[HerobrineAI] Player is offline.");}
 						}else{log.info("Usage: /hb-ai attack <player name>");}
 					}else if (args[0].equalsIgnoreCase("pyramid")){
 						if (args.length>1){
 							if (Bukkit.getServer().getPlayer(args[1])!=null){
 							if (Bukkit.getServer().getPlayer(args[1]).isOnline()){
 								if (P_Core.getSupport().checkBuild(Bukkit.getServer().getPlayer(args[1]).getLocation())){
 									Object[] data = {Bukkit.getServer().getPlayer(args[1])};
 									if(P_Core.getAICore().getCore(CoreType.PYRAMID).RunCore(data).getResult()){
 										log.info("[HerobrineAI] Creating pyramind near "+args[1]+"!");
 									}else{log.info("[HerobrineAI] Cannot find good place for pyramid!");}
 								}else{log.info("[HerobrineAI] Player is in secure area.");}
 								}else{log.info("[HerobrineAI] Player is offline.");}
 							}else{log.info("[HerobrineAI] Player is offline.");}
 						}else{log.info("Usage: "+ChatColor.GREEN+"/hb-ai pyramid <player name>");}
 					}else if (args[0].equalsIgnoreCase("temple")){
 						if (args.length>1){
 							if (Bukkit.getServer().getPlayer(args[1])!=null){
 							if (Bukkit.getServer().getPlayer(args[1]).isOnline()){
 								if (P_Core.getSupport().checkBuild(Bukkit.getServer().getPlayer(args[1]).getLocation())){
 									Object[] data = {Bukkit.getServer().getPlayer(args[1])};
 									if(P_Core.getAICore().getCore(CoreType.TEMPLE).RunCore(data).getResult()){
 										log.info("[HerobrineAI] Creating temple near "+args[1]+"!");
 									}else{log.info("[HerobrineAI] Cannot find good place for temple!");}
 								}else{log.info("[HerobrineAI] Player is in secure area.");}
 								}else{log.info("[HerobrineAI] Player is offline.");}
 							}else{log.info("[HerobrineAI] Player is offline.");}
 						}else{log.info("Usage: "+ChatColor.GREEN+"/hb-ai temple <player name>");}
 					}else if (args[0].equalsIgnoreCase("bury")){
 						if (args.length>1){
 							if (Bukkit.getServer().getPlayer(args[1])!=null){
 							if (Bukkit.getServer().getPlayer(args[1]).isOnline()){
 								if (P_Core.getSupport().checkBuild(Bukkit.getServer().getPlayer(args[1]).getLocation())){
 									Object[] data = {Bukkit.getServer().getPlayer(args[1])};
 									if(P_Core.getAICore().getCore(CoreType.BURY_PLAYER).RunCore(data).getResult()){
 										log.info("[HerobrineAI] Buried "+args[1]+"!");
 									}else{log.info("[HerobrineAI] Cannot find good place!");}
 								}else{log.info("[HerobrineAI] Player is in secure area.");}
 								}else{log.info("[HerobrineAI] Player is offline.");}
 							}else{log.info("[HerobrineAI] Player is offline.");}
 						}else{log.info("Usage: /hb-ai bury <player name>");}
 					}else if (args[0].equalsIgnoreCase("cave")){
 						if (args.length>1){
 							
 							
 							if (Bukkit.getServer().getPlayer(args[1])!=null){
 								Object[] data = {Bukkit.getServer().getPlayer(args[1]).getLocation(),true};
 								log.info(ChatColor.RED+"[HerobrineAI] "+P_Core.getAICore().getCore(CoreType.BUILD_STUFF).RunCore(data).getResultString());
 							}else{log.info(ChatColor.RED+"[HerobrineAI] Player is offline.");}
 					
 							}else{log.info("Usage: /hb-ai cave <player name>");}
 					}else if (args[0].equalsIgnoreCase("burn")){
 						if (args.length>1){
 							
 							if (Bukkit.getServer().getPlayer(args[1])!=null){
 								Object[] data = {Bukkit.getServer().getPlayer(args[1])};
 								log.info("[HerobrineAI] "+P_Core.getAICore().getCore(CoreType.BURN).RunCore(data).getResultString());
 							}else{log.info("[HerobrineAI] Player is offline.");}
 							
 							}else{log.info("Usage: /hb-ai burn <player name>");}
 					}else if (args[0].equalsIgnoreCase("curse")){
 						if (args.length>1){
 							
 							if (Bukkit.getServer().getPlayer(args[1])!=null){
 								Object[] data = {Bukkit.getServer().getPlayer(args[1])};
 								log.info("[HerobrineAI] "+P_Core.getAICore().getCore(CoreType.CURSE).RunCore(data).getResultString());
 							}else{log.info("[HerobrineAI] Player is offline.");}
 							
 							}else{log.info("Usage: /hb-ai curse <player name>");}
 					}else if (args[0].equalsIgnoreCase("heads")){
 						if (args.length>1){
 							
 							if (Bukkit.getServer().getPlayer(args[1])!=null){
 								Object[] data = {args[1]};
 								log.info("[HerobrineAI] "+P_Core.getAICore().getCore(CoreType.HEADS).RunCore(data).getResultString());
 							}else{log.info("[HerobrineAI] Player is offline.");}
 	
 							}else{log.info("Usage: "+ChatColor.GREEN+"/hb-ai heads <player name>");}
 					}
 					else if (args[0].equalsIgnoreCase("graveyard")){
 	      					if (args.length>1){
 	      						if (Bukkit.getServer().getPlayer(args[1])!=null){
 	      						if (Bukkit.getServer().getPlayer(args[1]).isOnline()){
 	      							if (AICore.isTarget==false){
 	      								P_Core.getAICore().GraveyardTeleport(Bukkit.getServer().getPlayer(args[1]));
 	      								log.info("[HerobrineAI] "+args[1]+" is now in the Graveyard world!");
 	      							}else{log.info("[HerobrineAI] Herobrine already has target! Use "+ChatColor.GREEN+"/hb-ai cancel"+ChatColor.RED+" to cancel actual target");}
 	      						}else{log.info("[HerobrineAI] Player is offline.");}
 	      						}else{log.info("[HerobrineAI] Player is offline.");}
 	      					}else{log.info("Usage: /hb-ai graveyard <player name>");}
 	      				}else if (args[0].equalsIgnoreCase("haunt")){
 						
 						if (args.length>1){
 							if (Bukkit.getServer().getPlayer(args[1])!=null){
 							if (Bukkit.getServer().getPlayer(args[1]).isOnline()){
 								if (P_Core.getSupport().checkHaunt(Bukkit.getServer().getPlayer(args[1]).getLocation())){
 								if (P_Core.canAttackPlayerConsole(Bukkit.getServer().getPlayer(args[1]))){
 								if (AICore.isTarget==false){
 									P_Core.getAICore().setHauntTarget(Bukkit.getServer().getPlayer(args[1]));
 									log.info("[HerobrineAI] Herobrine now haunts the "+args[1]+"!");
 								}else{log.info("[HerobrineAI] Herobrine already has target! Use /hb-ai cancel to cancel actual target.");}
 								}
 								}else{log.info("[HerobrineAI] Player is in secure area.");}
 								}else{log.info("[HerobrineAI] Player is offline.");}
 							}else{log.info("[HerobrineAI] Player is offline.");}
 						}else{log.info("Usage: /hb-ai haunt <player name>");}
 						
 					}else if (args[0].equalsIgnoreCase("cancel")){
 						
 						P_Core.getAICore().CancelTarget(CoreType.ANY);
 						log.info(ChatColor.RED+"[HerobrineAI] Target cancelled!");
 						
 					}else if (args[0].equalsIgnoreCase("reload")){
 						
 						P_Core.getConfigDB().Reload();
 						
 						log.info("[HerobrineAI] Config reloaded!");
 						
 					}else if (args[0].equalsIgnoreCase("help")){
 						
 						log.info("[HerobrineAI] Command list");
 						log.info("/hb-ai help - shows all commands");
 						log.info("/hb-ai attack <player name> - herobrine attacks the player");
 						log.info("/hb-ai haunt <player name> - herobrine haunts the player");
 						log.info("/hb-ai cancel - cancel herobrines actual target");
 						log.info("/hb-ai reload - reload config");
 						log.info("/hb-ai position - gets actual position of Herobrine");
 						log.info("/hb-ai pyramid <player name> - build pyramid near the player");
 						log.info("/hb-ai bury <player name> - bury player");
 						log.info("/hb-ai graveyard <player name> - teleport player to the Graveyard world");
 						log.info("/hb-ai temple <player name> - build temple near the player");
 						log.info("/hb-ai heads <player name> - place heads near the player");
 						log.info("/hb-ai cave <player name> - create cave near the player");
 						log.info("/hb-ai burn <player name> - burn player");
						log.info("/hb-ai curse <player name> - burn player");
 						
 					}else if (args[0].equalsIgnoreCase("position")){
 							
 						log.info("[HerobrineAI] Position");
 						log.info("World: "+P_Core.HerobrineNPC.getBukkitEntity().getLocation().getWorld().getName()+" X: "+((int)P_Core.HerobrineNPC.getBukkitEntity().getLocation().getX())+
 									" Y: "+((int)P_Core.HerobrineNPC.getBukkitEntity().getLocation().getY())+" Z: "+((int)P_Core.HerobrineNPC.getBukkitEntity().getLocation().getZ()));
 						log.info("InWalkingMode: "+AICore.getStringWalkingMode());
 						log.info("Available World: "+HerobrineAI.getPluginCore().getAvailableWorldString());
 						
 			          
 					}else{log.info("Usage: /hb-ai help");}
 				}else{log.info("Usage: /hb-ai help");}
 			return true;
 		}
 	}
 	
 }
