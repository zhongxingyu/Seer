 package com.zolli.rodolffoutilsreloaded.listeners;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.Bukkit;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.zolli.rodolffoutilsreloaded.rodolffoUtilsReloaded;
 import com.zolli.rodolffoutilsreloaded.utils.textUtils;
 import com.zolli.rodolffoutilsreloaded.utils.webUtils;
 
 public class commandExecutor implements CommandExecutor {
 
 	private String matchedPlayers = null;
 	private Player bannedPlayer;
 	private Player unbannedPlayer;
 	private Player freezedPlayer;
 	private rodolffoUtilsReloaded plugin;
 	private textUtils tu = new textUtils();
 	public commandExecutor(rodolffoUtilsReloaded instance) {
 		plugin = instance;
 	}
 	
 	
 	
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
 		if(command.getName().equalsIgnoreCase("achat") || command.getName().equalsIgnoreCase("ac")) {
 			if(args.length < 1) {
 				sender.sendMessage(plugin.messages.getString("common.badSyntax") + "/ac [üzenet]");
 				return false;
 			}
 			
 			if(sender.isOp() || plugin.perm.has(sender, "rur.adminChat")) {
 				String msgFormat = plugin.config.getString("adminChatFormat").replace("(NAME)", sender.getName());
 				String param = tu.arrayToString(args, 0);
 				
 				for(Player p : Bukkit.getServer().getOnlinePlayers()) {
 					if(p.isOp() || plugin.perm.has(p, "rur.adminchat")) {
 						p.sendMessage(msgFormat.replace("(MSG)", param).toString());
 					}
 				}
 			} else {
 				sender.sendMessage(plugin.messages.getString("common.noPerm"));
 				return false;
 			}
 			return true;
 		}
 		
 		if(command.getName().equalsIgnoreCase("fakechat") || command.getName().equalsIgnoreCase("/fc")) {
 			Player p = null;
 			String message = tu.arrayToString(args, 1);
 			
 			if(sender.isOp() || plugin.perm.has(sender, "rur.fakeChat")) {
 				if(args.length < 2) {
 					sender.sendMessage(plugin.messages.getString("common.badSyntax") + "/fc [név] [üzenet]");
 					return false;
 				}
 				List<Player> matchedPlayerList = Bukkit.getServer().matchPlayer(args[0]);
 				
 				if(matchedPlayerList.isEmpty() == false) {
 					if(matchedPlayerList.size() == 1) {
 						p = matchedPlayerList.get(0);
 						
 						if(!plugin.perm.has(p, "rur.fakeChat.exempt") && p != null) {
 							for(Player pl : plugin.getServer().getOnlinePlayers()) {
 								if(pl.isOp() || plugin.perm.has(pl, "rur.fakeChat.showname")) {
 									pl.sendMessage(sender.getName() + " írta:");
 								}
 							}
 							p.chat(message);
 						} else {
 							sender.sendMessage(plugin.messages.getString("fekechat.fakechatexempt"));
 						}
 					} else {
 						sender.sendMessage(plugin.messages.getString("common.multipleMatch"));
 						
 						for(Player matched : matchedPlayerList) {
 							if(matchedPlayers == null) {
 								matchedPlayers = matched.getName() + ", ";
 							} else {
 								matchedPlayers = matchedPlayers + matched.getName() + ", ";
 							}
 						}
 						sender.sendMessage("§2" + matchedPlayers);
 						matchedPlayers = null;
 					}
 				} else {
 					sender.sendMessage(plugin.messages.getString("common.noPlayerFound"));
 				}
 			} else {
 				sender.sendMessage(plugin.messages.getString("common.noPerm"));
 				return false;
 			}
 			return true;
 		}
 		
 		if(command.getName().equalsIgnoreCase("definebutton")) {
 			if(sender.isOp() || plugin.perm.has(sender, "rur.specialButton.define")) {
 				if(args.length < 2) {
 					sender.sendMessage(plugin.messages.getString("common.badSyntax") + "/definebutton [tipus] [név]");
 					return false;
 				}
 				if(args[0].equalsIgnoreCase("weathersun") || args[0].equalsIgnoreCase("promote")|| args[0].equalsIgnoreCase("spawn")) {
 					plugin.SelectorPlayer = sender.getName();
 					plugin.selectType = args[0];
 					plugin.selectName = args[1];
 					sender.sendMessage(plugin.messages.getString("definebutton.defineproc1") + args[0]);
 					sender.sendMessage(plugin.messages.getString("definebutton.defineproc2"));
 				} else {
 					sender.sendMessage(plugin.messages.getString("definebutton.onlytype"));
 				}
 			} else {
 				sender.sendMessage(plugin.messages.getString("common.noPerm"));
 				return false;
 			}
 			return true;
 		}
 		
 		if(command.getName().equalsIgnoreCase("idban")) {
 			if(sender.isOp() || plugin.perm.has(sender, "rur.idban")) {
 				if(args.length < 1) {
 					sender.sendMessage(plugin.messages.getString("common.badSyntax") + "/idban [név]");
 					return false;
 				}
 				List<Player> mathcPlayerList = Bukkit.matchPlayer(args[0]);
 				
 				if(mathcPlayerList.isEmpty() == false) {
 					if(mathcPlayerList.size() == 1) {
 						bannedPlayer = mathcPlayerList.get(0);
 						String answer = webUtils.idBan(bannedPlayer.getName());
 						
 						if(answer.equalsIgnoreCase("ok")) {
 							sender.sendMessage(bannedPlayer.getDisplayName() + plugin.messages.getString("banning.successbanned"));
 							
 							if(!bannedPlayer.isBanned() && plugin.config.getBoolean("idbanAlsoBanPlayer")) {
 								plugin.getServer().getPlayer(sender.getName()).performCommand("ban " + bannedPlayer.getName());
 							}
 						} else {
 							String result = plugin.messages.getString("banning.warningBan").replace("(NAME)", bannedPlayer.getName());
 							sender.sendMessage(result + answer);
 						}
 					} else {
 						sender.sendMessage(plugin.messages.getString("common.multipleMatch"));
 						
 						for(Player p : mathcPlayerList) {
 							if(matchedPlayers == null) {
 								matchedPlayers = p.getName() + ", ";
 							} else {
 								matchedPlayers = matchedPlayers + p.getName() + ", ";
 							}
 						}
 						sender.sendMessage("§2" + matchedPlayers);
 						matchedPlayers = null;
 					}
 				} else {
 					OfflinePlayer offlinePl = plugin.getServer().getOfflinePlayer(args[0]);
 				
 					if(offlinePl != null) {
 						bannedPlayer = offlinePl.getPlayer();
 						String answer = webUtils.idBan(bannedPlayer.getName());
 						if(answer.equalsIgnoreCase("ok")) {
 							sender.sendMessage(bannedPlayer.getName() + plugin.messages.getString("banning.successbanned"));
 							if(bannedPlayer.isBanned() == false && plugin.config.getBoolean("idbanAlsoBanPlayer")) {
 								plugin.getServer().getPlayer(sender.getName()).performCommand("ban " + args[0]);
 							}
 						} else {
 							String result = plugin.messages.getString("banning.warningBan").replace("(NAME)", bannedPlayer.getName());
 							sender.sendMessage(result + answer);	
 						}
 					}
 				}
 			} else {
 				sender.sendMessage(plugin.messages.getString("common.noPerm"));
 				return false;
 			}
 			return true;
 		}
 		
 		if(command.getName().equalsIgnoreCase("idunban")) {
 			if(sender.isOp() || plugin.perm.has(sender, "rur.idunban")) {
 				if(args.length < 1) {
 					sender.sendMessage(plugin.messages.getString("common.badSyntax") + "/idunban [név]");
 					return false;
 				}
 				
 				List<Player> mathcPlayerList = Bukkit.matchPlayer(args[0]);
 				
 				if(mathcPlayerList.isEmpty() == false) {
 					if(mathcPlayerList.size() == 1) {
 						unbannedPlayer = mathcPlayerList.get(0);
 						String answer = webUtils.idunBan(unbannedPlayer.getName());
 						
 						if(answer.equalsIgnoreCase("ok")) {
 							sender.sendMessage(unbannedPlayer.getDisplayName() + plugin.messages.getString("banning.successunbanned"));
 							
 							if(plugin.config.getBoolean("idUnbanAlsoUnbanPlayer")) {
 								plugin.getServer().getPlayer(sender.getName()).performCommand("unban " + unbannedPlayer.getName());
 							}
 						} else {
 							String result = plugin.messages.getString("banning.warningUnBan").replace("(NAME)", unbannedPlayer.getName());
 							sender.sendMessage(result + answer);
 						}
 					} else {
 						sender.sendMessage(plugin.messages.getString("common.multipleMatch"));
 						for(Player p : mathcPlayerList) {
 							if(matchedPlayers == null) {
 								matchedPlayers = p.getName() + ", ";
 							} else {
 								matchedPlayers = matchedPlayers + p.getName() + ", ";
 							}
 						}
 						sender.sendMessage("§2" + matchedPlayers);
 						matchedPlayers = null;	
 					}
 				} else {					
 					OfflinePlayer offlinePl = plugin.getServer().getOfflinePlayer(args[0]);
 
 					if(offlinePl != null) {
 						unbannedPlayer = offlinePl.getPlayer();
 						String answer = webUtils.idBan(unbannedPlayer.getName());
 						
 						if(answer.equalsIgnoreCase("ok")) {
 							sender.sendMessage(unbannedPlayer.getName() + plugin.messages.getString("banning.successunbanned"));
 							
 							if(unbannedPlayer.isBanned() == true && plugin.config.getBoolean("idUnbanAlsoUnbanPlayer")) {
 								plugin.getServer().getPlayer(sender.getName()).performCommand("unban " + unbannedPlayer.getName());
 							}
 						} else {
 							String result = plugin.messages.getString("banning.warningUnBan").replace("(NAME)", unbannedPlayer.getName());
 							sender.sendMessage(result + answer);	
 						}	
 					}	
 				}
 			} else {
 				sender.sendMessage(plugin.messages.getString("common.noPerm"));
 				return false;
 			}
 			return true;
 		}
 		
 		if(command.getName().equalsIgnoreCase("nappal")) {
 			if(sender.isOp() || plugin.perm.has(sender, "rur.nappal")) {
 				Player pl = plugin.getServer().getPlayer(sender.getName());
 				pl.getWorld().setTime(200L);
 				pl.sendMessage(plugin.messages.getString("othercommand.day"));
 				List<Player> players = pl.getWorld().getPlayers();
 				
 				for(Player msgTaker : players) {
 					if(!msgTaker.getName().equalsIgnoreCase(pl.getName())) {
 						msgTaker.sendMessage(pl.getName() + plugin.messages.getString("othercommand.daybroadcast"));
 					}
 				}
 			} else {
 				sender.sendMessage(plugin.messages.getString("common.noPerm"));
 				return false;
 			}
 			return true;
 		}
 		
 		if(command.getName().equalsIgnoreCase("napos")) {
 			if(sender.isOp() || plugin.perm.has(sender, "rur.napos")) {
 				Player pl = plugin.getServer().getPlayer(sender.getName());
 				pl.getWorld().setStorm(false);
 				pl.sendMessage(plugin.messages.getString("othercommand.sunny"));
 				List<Player> players = pl.getWorld().getPlayers();
 				
 				for(Player msgTaker : players) {
 					
 					if(!msgTaker.getName().equalsIgnoreCase(pl.getName())) {
 						msgTaker.sendMessage(pl.getName() + plugin.messages.getString("othercommand.sunnybroadcast"));
 					}
 				}
 			} else {
 				sender.sendMessage(plugin.messages.getString("common.noPerm"));
 				return false;
 			}
 			return true;
 		}
 		
 		if (command.getName().equalsIgnoreCase("rur")) {
 			if (!sender.isOp() && !plugin.perm.has(sender, "rur.admin")) {
 				return true;
 			}
 
 			if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
 				plugin.loadConfiguration();
 				sender.sendMessage(plugin.messages.getString("rurcommand.reload"));
 			} else if (args.length == 1 && args[0].equalsIgnoreCase("save")) {
 				plugin.saveConfiguration();
 				sender.sendMessage(plugin.messages.getString("rurcommand.save"));
 			} else if(args.length == 3 && args[0].equalsIgnoreCase("set")) {
 				String key = args[1];
 				String value = args[2];
 				Set<String> confKeys = plugin.config.getKeys(false);
 				
 				if(confKeys.contains(key)) {
 					if(plugin.config.isBoolean(key)) {
 						plugin.config.set(key, Boolean.parseBoolean(value));
 						plugin.saveConfiguration();
 						sender.sendMessage(plugin.messages.getString("config.sucess").replace("(KEY)", key).replace("(VAL)", value));
 					} else if(plugin.config.isInt(key)) {
 						plugin.config.set(key, Integer.parseInt(value));
 						plugin.saveConfiguration();
 						sender.sendMessage(plugin.messages.getString("config.sucess").replace("(KEY)", key).replace("(VAL)", value));	
 					} else if (plugin.config.isString(key)) {
 						plugin.config.set(key, value);
 						plugin.saveConfiguration();
 						sender.sendMessage(plugin.messages.getString("config.sucess").replace("(KEY)", key).replace("(VAL)", value));	
 					}
 				} else {
 					sender.sendMessage(plugin.messages.getString("config.nokey"));
 				}
 				
 			} else if(args.length == 1 && args[0].equalsIgnoreCase("get")) {
 				Set<String> confKey = plugin.config.getKeys(false);
 				Iterator<String> it = confKey.iterator();
 				
 				sender.sendMessage(plugin.messages.getString("config.allkey"));
 				
 				while(it.hasNext()) {
 					String ck = it.next();
 					
 					if(plugin.config.isBoolean(ck)) {
 						sender.sendMessage("§e(BOOLEAN) " + "§6" + ck + " - §f" + plugin.config.getBoolean(ck));
 					} else if(plugin.config.isInt(ck)) {
 						sender.sendMessage("§e(INT) " + "§6" + ck + " - §f" + plugin.config.getInt(ck));
 					} else if(plugin.config.isString(ck)) {
 						sender.sendMessage("§e(STRING) " + "§6" + ck + " - §f" + plugin.config.getString(ck));
 					}
 				}
 			} else {
 				sender.sendMessage(plugin.messages.getString("rurcommand.usage"));
 			}
 			return true;
 		}
 		
 		if (command.getName().equalsIgnoreCase("fullenchant")) {
 			
 			if (!sender.isOp() && !plugin.perm.has(sender, "rur.fullenchant")) {
 				return true;
 			}
 		
 			if(!(sender instanceof Player)) {
 				sender.sendMessage("This command you can use only ingame!");
 				return true;
 			}
 			
 			Player p = (Player)sender;
 			ItemStack is = p.getItemInHand();
 			Enchantment[] enc = Enchantment.values();
 			int newNum = 0;
 			int upNum = 0;
 			
 			for(int i=0;i<enc.length;i++)
 				try {
 					if(is.getEnchantmentLevel(enc[i])==0) {
 						newNum++;
 					}
 					if(args.length==1 && args[0].equalsIgnoreCase("extra")) {
 						if(is.getEnchantmentLevel(enc[i])<127) {
 							upNum++;
 						}
 						is.addUnsafeEnchantment(enc[i], 127);
 					} else {
 						if(is.getEnchantmentLevel(enc[i])<enc[i].getMaxLevel()) {
 							upNum++;
 						}
 						is.addEnchantment(enc[i], enc[i].getMaxLevel());
 					}
 				} catch (Exception ex) {
 					/**
 					 * Ignore all errors
 					 */
 				}
 			
 			p.sendMessage(plugin.messages.getString("othercommand.fullenchant").replace("%new%", Integer.toString(newNum)).replace("%up%", Integer.toString(upNum)));
 			plugin.log.info(p.getName()+" enchanted a "+is.getType().name().toLowerCase().replace("_", " ")+" to full!");
 		}
 
 		if (command.getName().equalsIgnoreCase("entitylist") || command.getName().equalsIgnoreCase("el")) {
 			if (!sender.isOp() && !plugin.perm.has(sender, "rur.entitylist")) {
 				sender.sendMessage(plugin.messages.getString("entitylist.noperm"));
 				return true;
 			}
 			
 			if(!(sender instanceof Player)) {
 				sender.sendMessage("This command you can use only ingame!");
 				return true;
 			}
 			
 			if(args.length>1) {
 				sender.sendMessage(plugin.messages.getString("entitylist.usage"));
 				return true;
 			}
 			
 			Player p = (Player)sender;
 			int dist = 20;
 			int count = 0;
 			
 			if(args.length == 1) {
 				try {
 					dist = Integer.parseInt(args[0]);
 				} catch (Exception ex) {
 					sender.sendMessage(plugin.messages.getString("entitylist.usage"));
 					return true;
 				}
 			}
 
 			Map<String,Integer> ents = new HashMap<String, Integer>();
 			List<Entity> el = p.getWorld().getEntities();
 			for(Entity ent : el) {
 				if(ent.getLocation().distance(p.getLocation())<=dist) {
 					count++;
 					if(ents.containsKey(ent.getType().name())) {
 						ents.put(ent.getType().name(), ents.get(ent.getType().name())+1);
 					} else {
 						ents.put(ent.getType().name(), 1);
 					}
 				}
 			}
 			
 			Iterator<Entry<String,Integer>> it = ents.entrySet().iterator();
 			while(it.hasNext()) {
 				Entry<String,Integer> entry = it.next();
 				p.sendMessage(plugin.messages.getString("entitylist.color")+entry.getKey()+": "+entry.getValue());
 			}
 			p.sendMessage(plugin.messages.getString("entitylist.color")+plugin.messages.getString("entitylist.msg").replace("%i", Integer.toString(count)));
 		}
 		
 		if(command.getName().equalsIgnoreCase("freeze")) {
 			if (!sender.isOp() && !plugin.perm.has(sender, "rur.freeze")) {
 				sender.sendMessage(plugin.messages.getString("common.noperm"));
 				return true;
 			}
 			
 			if(args.length != 1) {
 				sender.sendMessage(plugin.messages.getString("freeze.usage"));
 				return true;
 			}
 			List<Player> mathcPlayerList = Bukkit.matchPlayer(args[0]);
 			
 			if(mathcPlayerList.isEmpty() == false) {
 				if(mathcPlayerList.size() == 1) {
 					freezedPlayer = mathcPlayerList.get(0);
 					String playerName = freezedPlayer.getName();
 					
 					if(plugin.freezedPlayer.contains(playerName)) {
 						plugin.freezedPlayer.remove(playerName);
 						sender.sendMessage(plugin.messages.getString("freeze.removed").replace("(PLAYER)", playerName));
 					} else {
 						plugin.freezedPlayer.add(playerName);
 						sender.sendMessage(plugin.messages.getString("freeze.sucess").replace("(PLAYER)", playerName));
 					}
 				} else {
 					sender.sendMessage(plugin.messages.getString("common.multipleMatch"));
 					
 					for(Player p : mathcPlayerList) {
 						if(matchedPlayers == null) {
 							matchedPlayers = p.getName() + ", ";
 						} else {
 							matchedPlayers = matchedPlayers + p.getName() + ", ";
 						}
 					}
 					sender.sendMessage("§2" + matchedPlayers);
 					matchedPlayers = null;
 				}
 			} else {
 				sender.sendMessage(plugin.messages.getString("common.noPlayerFound"));
 			}
 		}
 		
 		return false;
 	}
 
 }
