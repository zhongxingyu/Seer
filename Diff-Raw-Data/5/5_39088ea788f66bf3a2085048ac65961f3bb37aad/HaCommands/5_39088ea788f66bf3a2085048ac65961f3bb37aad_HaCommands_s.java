 package me.Travja.HungerArena;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.HandlerList;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 //Jeppa : add for eventremoval...
 
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.sk89q.worldedit.bukkit.selections.Selection;
 
 public class HaCommands implements CommandExecutor {
 	public Main plugin;
 	public HaCommands(Main m) {
 		this.plugin = m;
 	}
 	int i = 0;
 	int a = 1;
 	@SuppressWarnings("deprecation")
 	private void clearInv(Player p){
 		p.getInventory().clear();
 		p.getInventory().setBoots(null);
 		p.getInventory().setChestplate(null);
 		p.getInventory().setHelmet(null);
 		p.getInventory().setLeggings(null);
 		p.updateInventory();
 	}
 	@SuppressWarnings({ "unchecked" })
 	@Override
 	public boolean onCommand(final CommandSender sender, Command cmd, String commandLabel, String[] args){
 		String[] Spawncoords = plugin.spawns.getString("Spawn_coords").split(",");
 		double spawnx = Double.parseDouble(Spawncoords[0]);
 		double spawny = Double.parseDouble(Spawncoords[1]);
 		double spawnz = Double.parseDouble(Spawncoords[2]);
 		String spawnworld = Spawncoords[3];
 		World spawnw = plugin.getServer().getWorld(spawnworld);
 		Location Spawn = new Location(spawnw, spawnx, spawny, spawnz);
 		if(sender instanceof Player){
 			final Player p = (Player) sender;
 			final String pname = p.getName();
 			ChatColor c = ChatColor.AQUA;
 			if(cmd.getName().equalsIgnoreCase("Ha")){
 				if(args.length== 0){
 					p.sendMessage(ChatColor.GREEN + "[HungerArena] by " + ChatColor.AQUA + "travja! Version: " + plugin.getDescription().getVersion());
 					return false;
 				}else if(args[0].equalsIgnoreCase("SetSpawn")){
 					if(p.hasPermission("HungerArena.SetSpawn")){
 						double x = p.getLocation().getX();
 						double y = p.getLocation().getY();
 						double z = p.getLocation().getZ();
 						String w = p.getWorld().getName();
 						plugin.spawns.set("Spawn_coords", x + "," + y + "," + z + "," + w);
 						plugin.spawns.set("Spawns_set", "true");
 						plugin.saveSpawns();
 						p.sendMessage(ChatColor.AQUA + "You have set the spawn for dead tributes!");
 					}else{
 						p.sendMessage(ChatColor.RED + "You don't have permission!");
 					}
 				}else if(args[0].equalsIgnoreCase("Help")){
 					p.sendMessage(ChatColor.GREEN + "----HungerArena Help----");
 					sender.sendMessage(c + "/ha - Displays author message!");
 					sender.sendMessage(c + "/sponsor [Player] [ItemID] [Amount] - Lets you sponsor someone!");
 					sender.sendMessage(c + "/startpoint [1,2,3,4,etc] [1,2,3,4,etc] - Sets the starting points of tributes in a specific arena!");
 					if(plugin.hookWE() != null)
 						sender.sendMessage(c + "/ha addArena [1,2,3,4,etc] - Creates an arena using your current WorldEdit selection.");
 					sender.sendMessage(c + "/ha close (1,2,3,4,etc) - Prevents anyone from joining that arena! Numbers are optional");
 					sender.sendMessage(c + "/ha help - Displays this screen!");
 					sender.sendMessage(c + "/ha join [1,2,3,4,etc] - Makes you join the game!");
 					sender.sendMessage(c + "/ha kick [Player] - Kicks a player from the arena!");
 					sender.sendMessage(c + "/ha leave - Makes you leave the game!");
 					sender.sendMessage(c + "/ha list (1,2,3,4,etc) - Shows a list of players in the game and their health! Numbers are optional.");
 					sender.sendMessage(c + "/ha open (1,2,3,4,etc) - Opens the game allowing people to join! Numbers are optional");
 					sender.sendMessage(c + "/ha ready - Votes for the game to start!");
 					sender.sendMessage(c + "/ha refill (1,2,3,4,etc) - Refills all chests! Numbers are optional");
 					sender.sendMessage(c + "/ha reload - Reloads the config!");
 					sender.sendMessage(c + "/ha restart (1,2,3,4,etc) - Restarts the game! Numbers are optional");
 					sender.sendMessage(c + "/ha rlist (1,2,3,4,etc) - See who's ready! Numbers are optional");
 					sender.sendMessage(c + "/ha setspawn - Sets the spawn for dead tributes!");
 					sender.sendMessage(c + "/ha tp [player] - Teleports you to a tribute!");
 					sender.sendMessage(c + "/ha start [1,2,3,4,etc] - Unfreezes tributes allowing them to fight!");
 					sender.sendMessage(c + "/ha watch [1,2,3,4,etc] - Lets you watch the tributes!");
 					sender.sendMessage(c + "/ha warpall [1,2,3,4,etc] - Warps all tribute into position!");
 					sender.sendMessage(ChatColor.GREEN + "----------------------");
 				}else if(plugin.restricted && !plugin.worlds.contains(p.getWorld().getName())){
 					p.sendMessage(ChatColor.RED + "That can't be run in this world!");
 				}else if((plugin.restricted && plugin.worlds.contains(p.getWorld().getName())) || !plugin.restricted){
 					//////////////////////////////////////// LISTING ///////////////////////////////////////////////
 					if(args[0].equalsIgnoreCase("List")){
 						if(p.hasPermission("HungerArena.GameMaker") || plugin.Watching.get(a).contains(pname) || p.hasPermission("HungerArena.List")){
 							if(args.length>= 2){
 								try{
 									a = Integer.parseInt(args[1]);
 									sender.sendMessage(ChatColor.AQUA + "----- Arena " + a + " -----");
 									if(!plugin.Playing.get(a).isEmpty() && plugin.Playing.containsKey(a)){
 										for(String playernames: plugin.Playing.get(a)){
 											Player players = plugin.getServer().getPlayerExact(playernames);
 											if(p.hasPermission("HungerArena.GameMaker")){
 												sender.sendMessage(ChatColor.GREEN + playernames + " Life: " + players.getHealth() + "/20");
 											}else if(p.hasPermission("HungerArena.List")){
 												sender.sendMessage(ChatColor.GREEN + playernames);
 											}
 										}
 									}else{
 										p.sendMessage(ChatColor.GRAY + "No one is playing!");
 									}
 									p.sendMessage(ChatColor.AQUA + "-------------------");
 								}catch(Exception e){
 									p.sendMessage(ChatColor.RED + "Argument not an integer or is an invalid arena!");
 								}
 							}else{
 								if(plugin.getArena(p)== null){
 									p.sendMessage(ChatColor.AQUA + "----- Arena 1 -----");
 									if(!plugin.Playing.get(1).isEmpty() && plugin.Playing.containsKey(1)){
 										for(String playernames: plugin.Playing.get(1)){
 											Player players = plugin.getServer().getPlayerExact(playernames);
 											if(p.hasPermission("HungerArena.GameMaker")){
 												sender.sendMessage(ChatColor.GREEN + playernames + " Life: " + players.getHealth() + "/20");
 											}else if(p.hasPermission("HungerArena.List")){
 												sender.sendMessage(ChatColor.GREEN + playernames);
 											}
 										}
 									}else{
 										p.sendMessage(ChatColor.GRAY + "No one is playing!");
 									}
 								}else{
 									a = plugin.getArena(p);
 									sender.sendMessage(ChatColor.AQUA + "----- Arena " + a + " -----");
 									if(!plugin.Playing.get(a).isEmpty() && plugin.Playing.containsKey(a)){
 										for(String playernames: plugin.Playing.get(a)){
 											Player players = plugin.getServer().getPlayerExact(playernames);
 											if(p.hasPermission("HungerArena.GameMaker")){
 												sender.sendMessage(ChatColor.GREEN + playernames + " Life: " + players.getHealth() + "/20");
 											}else if(p.hasPermission("HungerArena.List")){
 												sender.sendMessage(ChatColor.GREEN + playernames);
 											}
 										}
 									}else{
 										p.sendMessage(ChatColor.GRAY + "No one is playing!");
 									}
 								}
 								p.sendMessage(ChatColor.AQUA + "-------------------");
 							}
 						}else{
 							p.sendMessage(ChatColor.RED + "You don't have permission!");
 						}
 					}else if(args[0].equalsIgnoreCase("rList")){
 						if(p.hasPermission("HungerArena.GameMaker")){
 							if(args.length>= 2){
 								try{
 									a = Integer.parseInt(args[1]);
 									sender.sendMessage(ChatColor.AQUA + "----- Arena " + a + " -----");
 									if(!plugin.Ready.get(a).isEmpty() && plugin.Ready.containsKey(a)){
 										for(String playernames: plugin.Ready.get(a)){
 											sender.sendMessage(ChatColor.GREEN + playernames);
 										}
 									}else{
 										p.sendMessage(ChatColor.GRAY + "No one is ready!");
 									}
 									p.sendMessage(ChatColor.AQUA + "-------------------");
 								}catch(Exception e){
 									p.sendMessage(ChatColor.RED + "Argument not an integer!");
 								}
 							}else{
 								p.sendMessage(ChatColor.AQUA + "----- Arena 1 -----");
 								if(!plugin.Ready.get(1).isEmpty() && plugin.Ready.containsKey(1)){
 									for(String playernames: plugin.Ready.get(1)){
 										sender.sendMessage(ChatColor.GREEN + playernames);
 									}
 								}else{
 									p.sendMessage(ChatColor.GRAY + "No one is ready!");
 								}
 								p.sendMessage(ChatColor.AQUA + "-------------------");
 							}
 						}else{
 							p.sendMessage(ChatColor.RED + "You don't have permission!");
 						}
 						////////////////////////////////////////////////////////////////////////////////////////////////
 						///////////////////////////////////// JOINING/LEAVING //////////////////////////////////////////
 					}else if(args[0].equalsIgnoreCase("Join")){
 						if(p.hasPermission("HungerArena.Join")){
 							boolean needconfirm = false;
							for(i= 1; i< plugin.NeedConfirm.size(); i++){
 								if(plugin.NeedConfirm.get(i).contains(pname)){
 									needconfirm = true;
 									p.sendMessage(ChatColor.GOLD + "You need to run /ha confirm");
 								}
 							}
 							if(!needconfirm){
 								try{
 									a = Integer.parseInt(args[1]);
 								}catch(Exception e){
 									i = 1;
 									while(i <= plugin.Playing.size()){
 										if(plugin.Playing.get(i).size()< plugin.maxPlayers.get(i)){
 											a = i;
 											i = plugin.Playing.size()+1;
 										}
 										if(plugin.Playing.size()== i){
 											p.sendMessage(ChatColor.RED + "All games are full!");
 										}
 										i++;
 									}
 								}
 								if(plugin.Playing.get(a)!= null){
 									if(plugin.Playing.get(a).contains(pname))
 										p.sendMessage(ChatColor.RED + "You are already playing!");
 									else if(plugin.Dead.get(a).contains(pname) || plugin.Quit.get(a).contains(pname))
 										p.sendMessage(ChatColor.RED + "You DIED/QUIT! You can't join again!");
 									else if(plugin.Playing.get(a).size()== plugin.maxPlayers.get(a))
 										p.sendMessage(ChatColor.RED + "There are already " + plugin.maxPlayers.get(a) + " Tributes in that Arena!");
 									else if(plugin.canjoin.get(a)== true)
 										p.sendMessage(ChatColor.RED + "That game is in progress!");
 									else if(!plugin.open.get(a))
 										p.sendMessage(ChatColor.RED + "That game is closed!");
 									else if(plugin.spawns.getString("Spawns_set").equalsIgnoreCase("false"))
 										p.sendMessage(ChatColor.RED + "/ha setspawn hasn't been run!");
 									else if(plugin.getArena(p)!= null)
 										p.sendMessage(ChatColor.RED + "You are already in an arena!");
 									else if(plugin.config.getString("Need_Confirm").equalsIgnoreCase("true")){
 										if(plugin.config.getBoolean("EntryFee.enabled") && plugin.config.getBoolean("EntryFee.eco")){
 											if(!(plugin.econ.getBalance(pname) < plugin.config.getDouble("EntryFee.cost"))){
 												i = 0;
 												for(ItemStack fee: plugin.Fee){
 													int total = plugin.Fee.size();
 													if(p.getInventory().containsAtLeast(fee, fee.getAmount())){
 														i = i+1;
 														if(total == i){
 															plugin.NeedConfirm.get(a).add(pname);
 															p.sendMessage(ChatColor.GOLD + "You're inventory will be cleared! Type /ha confirm to procede");
 														}
 													}
 												}
 												if(plugin.Fee.size() > i){
 													p.sendMessage(ChatColor.RED + "You are missing some items and can't join the games...");
 												}
 											}else{
 												p.sendMessage(ChatColor.RED + "You don't have enough money to join!");
 											}
 										}else if(plugin.config.getBoolean("EntryFee.enabled") && !plugin.config.getBoolean("EntryFee.eco")){
 											i = 0;
 											for(ItemStack fee: plugin.Fee){
 												int total = plugin.Fee.size();
 												if(p.getInventory().containsAtLeast(fee, fee.getAmount())){
 													i = i+1;
 													if(total == i){
 														plugin.NeedConfirm.get(a).add(pname);
 														p.sendMessage(ChatColor.GOLD + "You're inventory will be cleared! Type /ha confirm to procede");
 													}
 												}
 											}
 											if(plugin.Fee.size() > i){
 												p.sendMessage(ChatColor.RED + "You are missing some items and can't join the games...");
 											}
 										}else if(!plugin.config.getBoolean("EntryFee.enabled") && plugin.config.getBoolean("EntryFee.eco")){
 											if(!(plugin.econ.getBalance(pname) < plugin.config.getDouble("EntryFee.cost"))){
 												plugin.NeedConfirm.get(a).add(pname);
 												p.sendMessage(ChatColor.GOLD + "You're inventory will be cleared! Type /ha confirm to procede");
 											}else{
 												p.sendMessage(ChatColor.RED + "You don't have enough money to join!");
 											}
 										}else{
 											plugin.NeedConfirm.get(a).add(pname);
 											p.sendMessage(ChatColor.GOLD + "You're inventory will be cleared! Type /ha confirm to procede");
 										}
 									}else if(plugin.config.getString("Need_Confirm").equalsIgnoreCase("false")){
 										if(plugin.config.getBoolean("EntryFee.enabled") && plugin.config.getBoolean("EntryFee.eco")){
 											if(!(plugin.econ.getBalance(pname) < plugin.config.getDouble("EntryFee.cost"))){
 												i = 0;
 												for(ItemStack fee: plugin.Fee){
 													int total = plugin.Fee.size();
 													if(p.getInventory().containsAtLeast(fee, fee.getAmount())){
 														i = i+1;
 														if(total == i){
 															plugin.econ.withdrawPlayer(pname, plugin.config.getDouble("EntryFee.cost"));
 															p.sendMessage(ChatColor.GOLD + "[HungerArena] " + ChatColor.GREEN + "$" + plugin.config.getDouble("EntryFee.cost") + " has been taken from your account!");
 															for(ItemStack fees: plugin.Fee){
 																String beginning = fees.getType().toString().substring(0, 1);
 																String item = beginning + fees.getType().toString().substring(1).toLowerCase().replace("_", " ");
 																int amount = fees.getAmount();
 																if(amount> 1)
 																	p.sendMessage(ChatColor.GOLD + "[HungerArena] " + ChatColor.GREEN + amount + " " + item + "s was paid to join the games.");
 																else
 																	p.sendMessage(ChatColor.GOLD + "[HungerArena] " + ChatColor.GREEN + amount + " " + item + " was paid to join the games.");
 															}
 															plugin.Playing.get(a).add(pname);
 															plugin.NeedConfirm.get(a).remove(pname);
 															p.sendMessage(ChatColor.GREEN + "Do /ha ready to vote to start the games!");
 															FileConfiguration pinfo = plugin.getPConfig(pname);
 															pinfo.set("inv", p.getInventory().getContents());
 															pinfo.set("armor", p.getInventory().getArmorContents());
 															pinfo.set("world", p.getLocation().getWorld().getName());
 															plugin.savePFile(pname);
 															clearInv(p);
 															if(plugin.config.getBoolean("broadcastAll")){
 																plugin.getServer().broadcastMessage(ChatColor.AQUA + pname +  " has Joined Arena " + a + "!");
 															}else{
 																for(String gn: plugin.Playing.get(a)){
 																	Player g = plugin.getServer().getPlayer(gn);
 																	g.sendMessage(ChatColor.AQUA + pname + " has Joined the Game!");
 																}
 															}
 															if(plugin.Playing.get(a).size()== plugin.maxPlayers.get(a)){
 																plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "ha warpall " + a);
 															}
 														}
 													}
 												}
 												if(plugin.Fee.size() > i){
 													p.sendMessage(ChatColor.RED + "You are missing some items and can't join the games...");
 												}
 											}else{
 												p.sendMessage(ChatColor.RED + "You don't have enough money to join!");
 											}
 										}else if(plugin.config.getBoolean("EntryFee.enabled") && !plugin.config.getBoolean("EntryFee.eco")){
 											i = 0;
 											for(ItemStack fee: plugin.Fee){
 												int total = plugin.Fee.size();
 												if(p.getInventory().containsAtLeast(fee, fee.getAmount())){
 													i = i+1;
 													if(total == i){
 														for(ItemStack fees: plugin.Fee){
 															String beginning = fees.getType().toString().substring(0, 1);
 															String item = beginning + fees.getType().toString().substring(1).toLowerCase().replace("_", " ");
 															int amount = fees.getAmount();
 															if(amount> 1)
 																p.sendMessage(ChatColor.GOLD + "[HungerArena] " + ChatColor.GREEN + amount + " " + item + "s was paid to join the games.");
 															else
 																p.sendMessage(ChatColor.GOLD + "[HungerArena] " + ChatColor.GREEN + amount + " " + item + " was paid to join the games.");
 														}
 														plugin.Playing.get(a).add(pname);
 														plugin.NeedConfirm.get(a).remove(pname);
 														p.sendMessage(ChatColor.GREEN + "Do /ha ready to vote to start the games!");
 														FileConfiguration pinfo = plugin.getPConfig(pname);
 														pinfo.set("inv", p.getInventory().getContents());
 														pinfo.set("armor", p.getInventory().getArmorContents());
 														pinfo.set("world", p.getLocation().getWorld().getName());
 														plugin.savePFile(pname);
 														clearInv(p);
 														if(plugin.config.getBoolean("broadcastAll")){
 															plugin.getServer().broadcastMessage(ChatColor.AQUA + pname +  " has Joined Arena " + a + "!");
 														}else{
 															for(String gn: plugin.Playing.get(a)){
 																Player g = plugin.getServer().getPlayer(gn);
 																g.sendMessage(ChatColor.AQUA + pname + " has Joined the Game!");
 															}
 														}												
 														if(plugin.Playing.get(a).size()== plugin.maxPlayers.get(a)){
 															plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "ha warpall " + a);
 														}
 													}
 												}
 											}
 											if(plugin.Fee.size() > i){
 												p.sendMessage(ChatColor.RED + "You are missing some items and can't join the games...");
 											}
 										}else if(!plugin.config.getBoolean("EntryFee.enabled") && plugin.config.getBoolean("EntryFee.eco")){
 											if(!(plugin.econ.getBalance(pname) < plugin.config.getDouble("EntryFee.cost"))){
 												plugin.econ.withdrawPlayer(pname, plugin.config.getDouble("EntryFee.cost"));
 												p.sendMessage(ChatColor.GOLD + "[HungerArena] " + ChatColor.GREEN + "$" + plugin.config.getDouble("EntryFee.cost") + " has been taken from your account!");
 												plugin.Playing.get(a).add(pname);
 												plugin.NeedConfirm.get(a).remove(pname);
 												p.sendMessage(ChatColor.GREEN + "Do /ha ready to vote to start the games!");
 												FileConfiguration pinfo = plugin.getPConfig(pname);
 												pinfo.set("inv", p.getInventory().getContents());
 												pinfo.set("armor", p.getInventory().getArmorContents());
 												pinfo.set("world", p.getLocation().getWorld().getName());
 												plugin.savePFile(pname);
 												clearInv(p);
 												if(plugin.config.getBoolean("broadcastAll")){
 													plugin.getServer().broadcastMessage(ChatColor.AQUA + pname +  " has Joined Arena " + a + "!");
 												}else{
 													for(String gn: plugin.Playing.get(a)){
 														Player g = plugin.getServer().getPlayer(gn);
 														g.sendMessage(ChatColor.AQUA + pname + " has Joined the Game!");
 													}
 												}										
 												if(plugin.Playing.get(a).size()== plugin.maxPlayers.get(a)){
 													plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "ha warpall " + a);
 												}
 											}else{
 												p.sendMessage(ChatColor.RED + "You don't have enough money to join!");
 											}
 										}else{
 											plugin.Playing.get(a).add(pname);
 											plugin.NeedConfirm.get(a).remove(pname);
 											p.sendMessage(ChatColor.GREEN + "Do /ha ready to vote to start the games!");
 											FileConfiguration pinfo = plugin.getPConfig(pname);
 											pinfo.set("inv", p.getInventory().getContents());
 											pinfo.set("armor", p.getInventory().getArmorContents());
 											pinfo.set("world", p.getLocation().getWorld().getName());
 											plugin.savePFile(pname);
 											clearInv(p);
 											if(plugin.config.getBoolean("broadcastAll")){
 												plugin.getServer().broadcastMessage(ChatColor.AQUA + pname +  " has Joined Arena " + a + "!");
 											}else{
 												for(String gn: plugin.Playing.get(a)){
 													Player g = plugin.getServer().getPlayer(gn);
 													g.sendMessage(ChatColor.AQUA + pname + " has Joined the Game!");
 												}
 											}
 											if(plugin.Playing.get(a).size()== plugin.maxPlayers.get(a)){
 												plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "ha warpall " + a);
 											}
 										}
 									}
 								}else{
 									p.sendMessage(ChatColor.RED + "That arena doesn't exist!");
 								}
 							}
 						}else{
 							p.sendMessage(ChatColor.RED + "You don't have permission!");
 						}
 						////////////////////////////////////////////////////////////////////////////////////////////////
 						////////////////////////////////// CONFIRMATION ///////////////////////////////////////////////
 					}else if(args[0].equalsIgnoreCase("Confirm")){
 						int v = 0;
						for(v = 1; v < plugin.NeedConfirm.size(); v++){
 							if(plugin.NeedConfirm.get(v).contains(pname)){
 								a = v;
 								v = plugin.NeedConfirm.size()+1;
 								if(plugin.config.getBoolean("EntryFee.enabled") && plugin.config.getBoolean("EntryFee.eco")){
 									if(!(plugin.econ.getBalance(pname) < plugin.config.getDouble("EntryFee.cost"))){
 										i = 0;
 										for(ItemStack fee: plugin.Fee){
 											int total = plugin.Fee.size();
 											if(p.getInventory().containsAtLeast(fee, fee.getAmount())){
 												i = i+1;
 												if(total == i){
 													plugin.econ.withdrawPlayer(pname, plugin.config.getDouble("EntryFee.cost"));
 													p.sendMessage(ChatColor.GOLD + "[HungerArena] " + ChatColor.GREEN + "$" + plugin.config.getDouble("EntryFee.cost") + " has been taken from your account!");
 													for(ItemStack fees: plugin.Fee){
 														String beginning = fees.getType().toString().substring(0, 1);
 														String item = beginning + fees.getType().toString().substring(1).toLowerCase().replace("_", " ");
 														int amount = fees.getAmount();
 														if(amount> 1)
 															p.sendMessage(ChatColor.GOLD + "[HungerArena] " + ChatColor.GREEN + amount + " " + item + "s was paid to join the games.");
 														else
 															p.sendMessage(ChatColor.GOLD + "[HungerArena] " + ChatColor.GREEN + amount + " " + item + " was paid to join the games.");
 													}
 													plugin.Playing.get(a).add(pname);
 													plugin.NeedConfirm.get(a).remove(pname);
 													p.sendMessage(ChatColor.GREEN + "Do /ha ready to vote to start the games!");
 													FileConfiguration pinfo = plugin.getPConfig(pname);
 													pinfo.set("inv", p.getInventory().getContents());
 													pinfo.set("armor", p.getInventory().getArmorContents());
 													pinfo.set("world", p.getLocation().getWorld().getName());
 													plugin.savePFile(pname);
 													clearInv(p);
 													if(plugin.config.getBoolean("broadcastAll")){
 														plugin.getServer().broadcastMessage(ChatColor.AQUA + pname +  " has Joined Arena " + a + "!");
 													}else{
 														for(String gn: plugin.Playing.get(a)){
 															Player g = plugin.getServer().getPlayer(gn);
 															g.sendMessage(ChatColor.AQUA + pname + " has Joined the Game!");
 														}
 													}
 													if(plugin.Playing.get(a).size()== plugin.maxPlayers.get(a)){
 														plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "ha warpall " + a);
 													}
 												}
 											}
 										}
 										if(plugin.Fee.size() > i){
 											p.sendMessage(ChatColor.RED + "You are missing some items and can't join the games...");
 										}
 									}else{
 										p.sendMessage(ChatColor.RED + "You don't have enough money to join!");
 									}
 								}else if(plugin.config.getBoolean("EntryFee.enabled") && !plugin.config.getBoolean("EntryFee.eco")){
 									i = 0;
 									for(ItemStack fee: plugin.Fee){
 										int total = plugin.Fee.size();
 										if(p.getInventory().containsAtLeast(fee, fee.getAmount())){
 											i = i+1;
 											if(total == i){
 												for(ItemStack fees: plugin.Fee){
 													String beginning = fees.getType().toString().substring(0, 1);
 													String item = beginning + fees.getType().toString().substring(1).toLowerCase().replace("_", " ");
 													int amount = fees.getAmount();
 													if(amount> 1)
 														p.sendMessage(ChatColor.GOLD + "[HungerArena] " + ChatColor.GREEN + amount + " " + item + "s was paid to join the games.");
 													else
 														p.sendMessage(ChatColor.GOLD + "[HungerArena] " + ChatColor.GREEN + amount + " " + item + " was paid to join the games.");
 												}
 												plugin.Playing.get(a).add(pname);
 												plugin.NeedConfirm.get(a).remove(pname);
 												p.sendMessage(ChatColor.GREEN + "Do /ha ready to vote to start the games!");
 												FileConfiguration pinfo = plugin.getPConfig(pname);
 												pinfo.set("inv", p.getInventory().getContents());
 												pinfo.set("armor", p.getInventory().getArmorContents());
 												pinfo.set("world", p.getLocation().getWorld().getName());
 												plugin.savePFile(pname);
 												clearInv(p);
 												if(plugin.config.getBoolean("broadcastAll")){
 													plugin.getServer().broadcastMessage(ChatColor.AQUA + pname +  " has Joined Arena " + a + "!");
 												}else{
 													for(String gn: plugin.Playing.get(a)){
 														Player g = plugin.getServer().getPlayer(gn);
 														g.sendMessage(ChatColor.AQUA + pname + " has Joined the Game!");
 													}
 												}												
 												if(plugin.Playing.get(a).size()== plugin.maxPlayers.get(a)){
 													plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "ha warpall " + a);
 												}
 											}
 										}
 									}
 									if(plugin.Fee.size() > i){
 										p.sendMessage(ChatColor.RED + "You are missing some items and can't join the games...");
 									}
 								}else if(!plugin.config.getBoolean("EntryFee.enabled") && plugin.config.getBoolean("EntryFee.eco")){
 									if(!(plugin.econ.getBalance(pname) < plugin.config.getDouble("EntryFee.cost"))){
 										plugin.econ.withdrawPlayer(pname, plugin.config.getDouble("EntryFee.cost"));
 										p.sendMessage(ChatColor.GOLD + "[HungerArena] " + ChatColor.GREEN + "$" + plugin.config.getDouble("EntryFee.cost") + " has been taken from your account!");
 										plugin.Playing.get(a).add(pname);
 										plugin.NeedConfirm.get(a).remove(pname);
 										p.sendMessage(ChatColor.GREEN + "Do /ha ready to vote to start the games!");
 										FileConfiguration pinfo = plugin.getPConfig(pname);
 										pinfo.set("inv", p.getInventory().getContents());
 										pinfo.set("armor", p.getInventory().getArmorContents());
 										pinfo.set("world", p.getLocation().getWorld().getName());
 										plugin.savePFile(pname);
 										clearInv(p);
 										if(plugin.config.getBoolean("broadcastAll")){
 											plugin.getServer().broadcastMessage(ChatColor.AQUA + pname +  " has Joined Arena " + a + "!");
 										}else{
 											for(String gn: plugin.Playing.get(a)){
 												Player g = plugin.getServer().getPlayer(gn);
 												g.sendMessage(ChatColor.AQUA + pname + " has Joined the Game!");
 											}
 										}										
 										if(plugin.Playing.get(a).size()== plugin.maxPlayers.get(a)){
 											plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "ha warpall " + a);
 										}
 									}else{
 										p.sendMessage(ChatColor.RED + "You don't have enough money to join!");
 									}
 								}else{
 									plugin.Playing.get(a).add(pname);
 									plugin.NeedConfirm.get(a).remove(pname);
 									p.sendMessage(ChatColor.GREEN + "Do /ha ready to vote to start the games!");
 									FileConfiguration pinfo = plugin.getPConfig(pname);
 									pinfo.set("inv", p.getInventory().getContents());
 									pinfo.set("armor", p.getInventory().getArmorContents());
 									pinfo.set("world", p.getLocation().getWorld().getName());
 									plugin.savePFile(pname);
 									clearInv(p);
 									if(plugin.config.getBoolean("broadcastAll")){
 										plugin.getServer().broadcastMessage(ChatColor.AQUA + pname +  " has Joined Arena " + a + "!");
 									}else{
 										for(String gn: plugin.Playing.get(a)){
 											Player g = plugin.getServer().getPlayer(gn);
 											g.sendMessage(ChatColor.AQUA + pname + " has Joined the Game!");
 										}
 									}
 									if(plugin.Playing.get(a).size()== plugin.maxPlayers.get(a)){
 										plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "ha warpall " + a);
 									}
 								}
 							}
 							if(v== plugin.NeedConfirm.size()){
 								p.sendMessage(ChatColor.RED + "You haven't joined any games!");
 							}
 						}
 					}else if(args[0].equalsIgnoreCase("Ready")){
 						if(plugin.getArena(p)!= null){
 							a = plugin.getArena(p);
 							if(plugin.Playing.get(a).contains(pname)){
 								if(plugin.Ready.get(a).contains(pname)){
 									p.sendMessage(ChatColor.RED + "You're already ready!");
 								}else if(plugin.Playing.get(a).size()== 1){
 									p.sendMessage(ChatColor.RED + "You can't be ready when no one else is playing!");
 								}else{
 									plugin.Ready.get(a).add(pname);
 									p.sendMessage(ChatColor.AQUA + "You have marked yourself as READY!");
 									if(plugin.Playing.get(a).size()-4== plugin.Ready.get(a).size() || plugin.Playing.get(a).size()==plugin.Ready.get(a).size()){
 										Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "ha warpall " + a);
 									}
 								}
 							}
 						}else{
 							p.sendMessage(ChatColor.RED + "You aren't playing in any games!");
 						}
 					}else if(args[0].equalsIgnoreCase("Leave")){
 						//TODO **Give inv back if they enter the correct world.**
 						if(plugin.getArena(p)!= null){
 							a = plugin.getArena(p);
 							plugin.needInv.add(pname);
 							if(plugin.canjoin.get(a)== true){
 								plugin.Playing.get(a).remove(pname);
 								p.sendMessage(ChatColor.AQUA + "You have left the game!");
 								if(plugin.config.getBoolean("broadcastAll")){
 									p.getServer().broadcastMessage(ChatColor.RED + pname + " Left Arena " + a + "!");
 								}else{
 									for(String gn: plugin.Playing.get(a)){
 										Player g = plugin.getServer().getPlayer(gn);
 										g.sendMessage(ChatColor.RED + pname + " Quit!");
 									}
 								}
 								clearInv(p);
 								p.teleport(Spawn);
 								if(plugin.Frozen.get(a).contains(pname)){
 									plugin.Frozen.get(a).remove(pname);
 								}
 								plugin.winner(a);
 							}else{
 								plugin.Playing.get(a).remove(pname);
 								p.sendMessage(ChatColor.AQUA + "You have left the game!");
 								if(plugin.config.getBoolean("broadcastAll")){
 									p.getServer().broadcastMessage(ChatColor.RED + pname + " Left Arena " + a + "!");
 								}else{
 									for(String gn: plugin.Playing.get(a)){
 										Player g = plugin.getServer().getPlayer(gn);
 										g.sendMessage(ChatColor.RED + pname + " Quit!");
 									}
 								}
 								clearInv(p);
 								p.teleport(Spawn);
 								if(plugin.config.getBoolean("EntryFee.enabled") && plugin.config.getBoolean("EntryFee.eco")){
 									plugin.econ.depositPlayer(pname, plugin.config.getDouble("EntryFee.cost"));
 									p.sendMessage(ChatColor.GOLD + "[HungerArena] " + ChatColor.GREEN + "$" + plugin.config.getDouble("EntryFee.cost") + " has been added to your account!");
 									for(ItemStack fees: plugin.Fee){
 										p.sendMessage(ChatColor.GOLD + "[HungerArena] " + ChatColor.GREEN + fees.getType().toString().toLowerCase().replace("_", " ") + " was refunded because you left the games.");
 									}
 								}else if(plugin.config.getBoolean("EntryFee.enabled") && !plugin.config.getBoolean("EntryFee.eco")){
 									for(ItemStack fees: plugin.Fee){
 										p.sendMessage(ChatColor.GOLD + "[HungerArena] " + ChatColor.GREEN + fees.getType().toString().toLowerCase().replace("_", " ") + " was refunded because you left the games.");
 									}
 								}else if(!plugin.config.getBoolean("EntryFee.enabled") && plugin.config.getBoolean("EntryFee.eco")){
 									plugin.econ.depositPlayer(pname, plugin.config.getDouble("EntryFee.cost"));
 									p.sendMessage(ChatColor.GOLD + "[HungerArena] " + ChatColor.GREEN + "$" + plugin.config.getDouble("EntryFee.cost") + " has added to your account!");
 								}
 							}
 						}else{
 							p.sendMessage(ChatColor.RED + "You aren't in any games!");
 						}
 						////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 						//////////////////////////////// SPECTATOR RELATED //////////////////////////////////
 					}else if(args[0].equalsIgnoreCase("Watch")){
 						if(sender.hasPermission("HungerArena.Watch")){
 							if(args.length>= 2){
 								a = Integer.parseInt(args[1]);
 								if(!plugin.Watching.get(a).contains(pname) && plugin.getArena(p)== null && plugin.canjoin.get(a)== true){
 									plugin.Watching.get(a).add(pname);
 									for(Player online:plugin.getServer().getOnlinePlayers()){
 										online.hidePlayer(p);
 									}
 									p.setAllowFlight(true);
 									p.sendMessage(ChatColor.AQUA + "You can now spectate!");
 								}else if(plugin.canjoin.get(a)== false){
 									p.sendMessage(ChatColor.RED + "That game isn't in progress!");
 								}else if(plugin.Playing.get(a).contains(pname)){
 									p.sendMessage(ChatColor.RED + "You can't watch while you're playing!");
 								}else if(plugin.Watching.get(a).contains(pname)){
 									plugin.Watching.get(a).remove(pname);
 									for(Player online:plugin.getServer().getOnlinePlayers()){
 										online.showPlayer(p);
 									}
 									p.teleport(Spawn);
 									p.setAllowFlight(false);
 									p.sendMessage(ChatColor.AQUA + "You are not spectating any more");
 								}
 							}else{
 								p.sendMessage(ChatColor.RED + "Too few arguments!");
 							}
 						}else{
 							p.sendMessage(ChatColor.RED + "You don't have permission!");
 						}
 					}else if(args[0].equalsIgnoreCase("tp")){
 						int b = 0;
 						for(i = 1; i <= plugin.Watching.size(); i++){
 							if(plugin.Watching.get(a).contains(pname)){
 								if(plugin.getArena(Bukkit.getServer().getPlayer(args[1])) != null){
 									Player target = Bukkit.getServer().getPlayer(args[1]);
 									p.teleport(target);
 									p.sendMessage(ChatColor.AQUA + "You've been teleported to " + target.getName());
 									return true;
 								}else{
 									p.sendMessage(ChatColor.RED + "That person isn't in game!");
 									return true;
 								}
 							}else{
 								b = b+1;
 								if(b== plugin.Watching.size()){
 									p.sendMessage(ChatColor.RED + "You have to be spectating first!");
 									return true;
 								}
 							}
 						}
 						/////////////////////////////////////////////////////////////////////////////////
 					}else if(args[0].equalsIgnoreCase("addArena")){
 						if(plugin.hookWE() != null){
 							if(args.length != 2)
 								return false;
 							if(p.hasPermission("HungerArena.AddArena")){
 								WorldEditPlugin worldedit = plugin.hookWE(); 
 								Selection sel = worldedit.getSelection(p);
 								if(sel== null)
 									p.sendMessage(ChatColor.DARK_RED + "You must make a WorldEdit selection first!");
 								else{
 									Location min = sel.getMinimumPoint();
 									Location max = sel.getMaximumPoint();
 									plugin.spawns.set("Arenas." + args[1] + ".Max", max.getWorld().getName() + "," + max.getX() + "," 
 											+ max.getY() + "," + max.getZ());
 									plugin.spawns.set("Arenas." + args[1] + ".Min", min.getWorld().getName() + "," + min.getX() + "," 
 											+ min.getY() + "," + min.getZ());
 									plugin.saveConfig();
 									p.sendMessage(ChatColor.GREEN + "Arena " + ChatColor.DARK_AQUA + args[1] 
 											+ ChatColor.GREEN + " created with WorldEdit!");
 									return true;
 								}
 							}else{
 								p.sendMessage(ChatColor.RED + "You don't have permission!");
 								return true;
 							}
 						}else{
 							p.sendMessage(ChatColor.RED + "You don't have WorldEdit enabled for HungerArena!");
 							return true;
 						}
 					}else if(args[0].equalsIgnoreCase("Kick")){
 						if (args.length != 2) {
 							return false;
 						}
 						Player target = Bukkit.getServer().getPlayer(args[1]);
 						if(p.hasPermission("HungerArena.Kick")){
 							if(plugin.getArena(target) != null){
 								a = plugin.getArena(target);
 								plugin.Playing.get(a).remove(target.getName());
 								if(plugin.config.getBoolean("broadcastAll")){
 									p.getServer().broadcastMessage(ChatColor.RED + target.getName() + " was kicked from arena " + a + "!");
 								}else{
 									for(String gn: plugin.Playing.get(a)){
 										Player g = plugin.getServer().getPlayer(gn);
 										g.sendMessage(ChatColor.RED + target.getName() + " was kicked from the game!");
 									}
 								}
 								clearInv(target);
 								target.teleport(Spawn);
 								plugin.Quit.get(a).add(target.getName());
 								plugin.winner(a);
 								return true;
 							}else{
 								sender.sendMessage(ChatColor.RED + "That player isn't in the game!");
 								return true;
 							}
 						}else{
 							sender.sendMessage(ChatColor.RED + "You don't have permission!");
 							return true;
 						}
 					}else if(args[0].equalsIgnoreCase("Refill")){
 						if(p.hasPermission("HungerArena.Refill")){
 							if(args.length>= 2){
 								a = Integer.parseInt(args[1]);
 								int list056;
 								list056 = 0;
 								int limit = plugin.MyChests.getStringList("StorageXYZ").size(); 
 								while(limit > list056){
 									String xyz2 = plugin.getChests().getStringList("StorageXYZ").get(list056);
 									int chestx = plugin.getChests().getInt("Storage." + xyz2 + ".Location.X");
 									int chesty = plugin.getChests().getInt("Storage." + xyz2 + ".Location.Y");
 									int chestz = plugin.getChests().getInt("Storage." + xyz2 + ".Location.Z");
 									int chesta = plugin.getChests().getInt("Storage." + xyz2 + ".Arena");
 									String chestw = plugin.getChests().getString("Storage." + xyz2 + ".Location.W");
 									Block blockatlocation = Bukkit.getWorld(chestw).getBlockAt(chestx, chesty, chestz);  
 									plugin.exists = false;
 									if(chesta== a){
 										if(blockatlocation.getState() instanceof Chest){
 											plugin.exists = true;
 											Chest chest = (Chest) blockatlocation.getState();
 											if(chesta== a){
 												chest.getInventory().clear();
 												ItemStack[] itemsinchest = null;
 												Object o = plugin.getChests().get("Storage." + xyz2 + ".ItemsInStorage");
 												if(o instanceof ItemStack[]){
 													itemsinchest = (ItemStack[]) o;
 												}else if(o instanceof List){
 													itemsinchest = (ItemStack[]) ((List<ItemStack>) o).toArray(new ItemStack[0]);
 												}
 												list056 = list056+1;
 												chest.getInventory().setContents(itemsinchest);
 												chest.update();
 											}
 										}
 									}else{
 										list056 = list056+1;
 									}
 									if(limit== list056){
 										sender.sendMessage(ChatColor.GREEN + "All for arena " + a + " refilled!");
 									}
 								}
 							}else{
 								int list056;
 								list056 = 0;
 								int limit = plugin.MyChests.getStringList("StorageXYZ").size();
 								while(limit > list056){
 									String xyz2 = plugin.getChests().getStringList("StorageXYZ").get(list056);
 									int chestx = plugin.getChests().getInt("Storage." + xyz2 + ".Location.X");
 									int chesty = plugin.getChests().getInt("Storage." + xyz2 + ".Location.Y");
 									int chestz = plugin.getChests().getInt("Storage." + xyz2 + ".Location.Z");
 									String chestw = plugin.getChests().getString("Storage." + xyz2 + ".Location.W");
 									Block blockatlocation = Bukkit.getWorld(chestw).getBlockAt(chestx, chesty, chestz);  
 									plugin.exists = false;
 									if(blockatlocation.getState() instanceof Chest){
 										plugin.exists = true;
 										Chest chest = (Chest) blockatlocation.getState();
 										chest.getInventory().clear();
 										ItemStack[] itemsinchest = null;
 										Object o = plugin.getChests().get("Storage." + xyz2 + ".ItemsInStorage");
 										if(o instanceof ItemStack[]){
 											itemsinchest = (ItemStack[]) o;
 										}else if(o instanceof List){
 											itemsinchest = (ItemStack[]) ((List<ItemStack>) o).toArray(new ItemStack[0]);
 										}
 										list056 = list056+1;
 										chest.getInventory().setContents(itemsinchest);
 										chest.update();
 									}
 								}
 								if(limit== list056){
 									sender.sendMessage(ChatColor.GREEN + "All chests refilled!");
 								}
 								return true;
 							}
 						}else{
 							p.sendMessage(ChatColor.RED + "You don't have permission!");
 							return true;
 						}
 					}else if(args[0].equalsIgnoreCase("Restart")){
 						int b = 0;
 						if(p.hasPermission("HungerArena.Restart")){
 							i = 1;
 							int e = plugin.open.size();
 							if(args.length>= 2){
 								i = Integer.parseInt(args[1]);
 								if(i > e) i=e;
 								if(i < 1) i=1;
 								e = i;
 							}
 							for(a = i; a <= e; a++){
 								if(plugin.Playing.get(a).size() > 0){
 									for(b = 0; b < plugin.Playing.get(a).size(); b++){
 										String s = plugin.Playing.get(a).get(b);
 										Player tributes = plugin.getServer().getPlayerExact(s);
 										clearInv(tributes);
 										tributes.teleport(tributes.getWorld().getSpawnLocation());
 									}
 								}
 								if(plugin.Watching.get(a).size() > 0){
 									for(b = 0; b < plugin.Watching.get(a).size(); b++){
 										String s = plugin.Watching.get(a).get(b);
 										Player spectator = plugin.getServer().getPlayerExact(s);
 										spectator.setAllowFlight(false);
 										spectator.teleport(Spawn);
 										for(Player online:plugin.getServer().getOnlinePlayers()){
 											online.showPlayer(spectator);
 										}
 									}
 								}
 								plugin.Dead.get(a).clear();
 								plugin.Quit.get(a).clear();
 								plugin.Watching.get(a).clear();
 								plugin.Frozen.get(a).clear();
 								plugin.Ready.get(a).clear();
 								plugin.NeedConfirm.get(a).clear();
 								plugin.Out.get(a).clear();
 								plugin.Playing.get(a).clear();
 								plugin.inArena.get(a).clear();
 								plugin.canjoin.put(a, false);
 								plugin.open.put(a, true);
 								List<String> blocksbroken = plugin.data.getStringList("Blocks_Destroyed");
 								List<String> blocksplaced = plugin.data.getStringList("Blocks_Placed");
 								ArrayList<String> toremove = new ArrayList<String>();
 								ArrayList<String> toremove2 = new ArrayList<String>();
 								for(String blocks:blocksplaced){
 									String[] coords = blocks.split(",");
 									World w = plugin.getServer().getWorld(coords[0]);
 									double x = Double.parseDouble(coords[1]);
 									double y = Double.parseDouble(coords[2]);
 									double z = Double.parseDouble(coords[3]);
 									int arena = Integer.parseInt(coords[4]);
 									int d = 0;
 									byte m = 0;
 									Location blockl = new Location(w, x, y, z);
 									Block block = w.getBlockAt(blockl);
 									if(arena== a){
 										block.setTypeIdAndData(d, m, true);
 										block.getState().update();
 										toremove.add(blocks);
 									}
 								}
 								for(String blocks:blocksbroken){
 									String[] coords = blocks.split(",");
 									World w = plugin.getServer().getWorld(coords[0]);
 									double x = Double.parseDouble(coords[1]);
 									double y = Double.parseDouble(coords[2]);
 									double z = Double.parseDouble(coords[3]);
 									int d = Integer.parseInt(coords[4]);
 									byte m = Byte.parseByte(coords[5]);
 									int arena = Integer.parseInt(coords[6]);
 									Location blockl = new Location(w, x, y, z);
 									Block block = w.getBlockAt(blockl);
 									if(arena== a){
 										block.setTypeIdAndData(d, m, true);
 										block.getState().update();
 										toremove2.add(blocks);
 									}
 								}
 								for(String blocks: toremove){
 									blocksplaced.remove(blocks);
 								}
 								for(String blocks: toremove2){
 									blocksbroken.remove(blocks);
 								}
 								toremove.clear();
 								toremove2.clear();
 								plugin.data.set("Blocks_Destroyed", blocksbroken);
 								plugin.data.set("Blocks_Placed", blocksplaced);
 								plugin.data.options().copyDefaults();
 								plugin.saveData();
 								p.performCommand("ha refill " + a);
 								p.sendMessage(ChatColor.AQUA + "Arena " + a + " has been reset!");
 							}
 						}else{
 							p.sendMessage(ChatColor.RED + "You don't have permission!");
 						}
 						/////////////////////////////////// Toggle //////////////////////////////////////////////////
 					}else if(args[0].equalsIgnoreCase("close")){
 						if(p.hasPermission("HungerArena.toggle")){
 							i = 1;
 							int e = plugin.open.size();
 							if(args.length>= 2){
 								i = Integer.parseInt(args[1]);
 								if(i > e) i=e;
 								if(i < 1) i=1;
 								e = i;
 							}
 							for(a = i; a <= e; a++){
 								if(plugin.open.get(a)){
 									plugin.open.put(a, false);
 									if(plugin.Playing.get(a)!= null){
 										for(String players: plugin.Playing.get(a)){
 											Player tributes = plugin.getServer().getPlayerExact(players);
 											clearInv(tributes);
 											tributes.teleport(tributes.getWorld().getSpawnLocation());
 										}
 									}
 									if(plugin.Watching.get(a)!= null){
 										for(String sname: plugin.Watching.get(a)){
 											Player spectators = plugin.getServer().getPlayerExact(sname);
 											spectators.teleport(spectators.getWorld().getSpawnLocation());
 											spectators.setAllowFlight(false);
 											for(Player online:plugin.getServer().getOnlinePlayers()){
 												online.showPlayer(spectators);
 											}
 										}
 									}
 									plugin.Dead.get(a).clear();
 									plugin.Quit.get(a).clear();
 									plugin.Watching.get(a).clear();
 									plugin.Frozen.get(a).clear();
 									plugin.Ready.get(a).clear();
 									plugin.NeedConfirm.get(a).clear();
 									plugin.Out.get(a).clear();
 									plugin.Playing.get(a).clear();
 									plugin.inArena.get(a).clear();
 									p.performCommand("ha refill " + a);
 									p.sendMessage(ChatColor.GOLD + "Arena " + a + " Closed!");
 								}else{
 									p.sendMessage(ChatColor.RED + "Arena " + a + " already closed, type /ha open to re-open them!");
 								}
 							}
 						}else{
 							p.sendMessage(ChatColor.RED + "No Perms!");
 						}
 					}else if(args[0].equalsIgnoreCase("open")){
 						if(p.hasPermission("HungerArena.toggle")){
 							if(args.length>= 2){
 								a = Integer.parseInt(args[1]);
 								if(!plugin.open.get(a)){
 									plugin.open.put(a, true);
 									p.sendMessage(ChatColor.GOLD + "Arena " + a + " Open!");
 								}else{
 									p.sendMessage(ChatColor.RED + "Arena " + a + " already open, type /ha close to close them!");
 								}
 							}else{
 								for(i = 1; i <= plugin.open.size(); i++){
 									if(!plugin.open.get(i)){
 										plugin.open.put(i, true);
 										p.sendMessage(ChatColor.GOLD + "Arena " + i + " Open!");
 										//	i = i+1; // Jeppa: ???
 									}else{
 										p.sendMessage(ChatColor.RED + "Arena " + i + " already open, type /ha close to close them!");
 										//	i = i+1;
 									}
 								}
 							}
 						}else{
 							p.sendMessage(ChatColor.RED + "No Perms!");
 						}
 						////////////////////////////////////////////////////////////////////////////////////////////
 					}else if(args[0].equalsIgnoreCase("Reload")){
 						if(p.hasPermission("HungerArena.Reload")){
 							plugin.location.clear();
 							plugin.Reward.clear();
 							plugin.Cost.clear();
 							plugin.Fee.clear();
 							HandlerList.unregisterAll(plugin); 	//Jeppa: Close all running Listeners before reopening them!
 							plugin.reloadConfig();
 							plugin.onEnable();			//Jeppa: with this in here all Listeners get re-registered again each "reload"! so f.e the Block-break will result in multiple lines in Data.yml ...
 							p.sendMessage(ChatColor.AQUA + "HungerArena Reloaded!");
 							System.out.println(ChatColor.GREEN + pname + " reloaded HungerArena!");
 						}else{
 							p.sendMessage(ChatColor.RED + "You don't have permission");
 						}
 					}else if(args[0].equalsIgnoreCase("WarpAll")){
 						if(p.hasPermission("HungerArena.Warpall")){
 							if(plugin.spawns.getString("Spawns_set").equalsIgnoreCase("false")){
 								sender.sendMessage(ChatColor.RED + "/ha setspawn hasn't been run!");
 							}else{
 								if(args.length>= 2){
 									a = Integer.parseInt(args[1]);
 									if(plugin.Playing.get(a).size()== 1){
 										sender.sendMessage(ChatColor.RED + "There are not enough players!");
 									}else{
 										if(plugin.config.getString("Auto_Start").equalsIgnoreCase("true")){
 											plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
 												public void run(){
 													Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "ha start " + a);
 												}
 											}, 20L);
 										}
 										i = 1;
 										for(String playing:plugin.Playing.get(a)){
 											Player tribute = plugin.getServer().getPlayerExact(playing);
 											tribute.teleport(plugin.location.get(a).get(i));
 											tribute.setHealth(20);
 											tribute.setFoodLevel(20);
 											tribute.setSaturation(20);
 											tribute.setLevel(0);
 											clearInv(tribute);
 											for(PotionEffect pe: tribute.getActivePotionEffects()){
 												PotionEffectType potion = pe.getType();
 												tribute.removePotionEffect(potion);
 											}
 											if(tribute.getAllowFlight()){
 												tribute.setAllowFlight(false);
 											}
 											plugin.Frozen.get(a).add(tribute.getName());
 											i = i+1;
 										}
 										plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
 											public void run(){
 												p.sendMessage(ChatColor.AQUA + "All Tributes warped!");
 											}
 										}, 20L);
 									}
 								}else{
 									p.sendMessage(ChatColor.RED + "Too few arguments, specify an arena");
 								}
 							}
 						}
 					}else if(args[0].equalsIgnoreCase("Start")){
 						if(p.hasPermission("HungerArena.Start")){
 							if(args.length!= 2){
 								p.sendMessage(ChatColor.RED + "You need an arena!");
 							}else{
 								a = Integer.parseInt(args[1]);
 								if(plugin.canjoin.get(a)== true)
 									p.sendMessage(ChatColor.RED + "Game already in progress!");
 								else if(plugin.Playing.get(a).isEmpty())
 									p.sendMessage(ChatColor.RED + "No one is in that game!");
 								else
 									plugin.startGames(a);
 							}
 						}else{
 							p.sendMessage(ChatColor.RED + "You don't have permission!");
 						}
 					}else{
 						p.sendMessage(ChatColor.RED + "Unknown command, type /ha help for a list of commands");
 					}
 				}
 			}
 		}else if(sender instanceof ConsoleCommandSender){
 			if(cmd.getName().equalsIgnoreCase("Ha")){
 				if(args.length== 0){
 					sender.sendMessage(ChatColor.GREEN + "[HungerArena] by " + ChatColor.AQUA + "travja! Version: " + plugin.getDescription().getVersion());
 					return false;
 				}
 				if(args[0].equalsIgnoreCase("Help")){
 					ChatColor c = ChatColor.AQUA;
 					sender.sendMessage(ChatColor.GREEN + "----HungerArena Help----");
 					sender.sendMessage(c + "/ha - Displays author message!");
 					sender.sendMessage(c + "/sponsor [Player] [ItemID] [Amount] - Lets you sponsor someone!");
 					sender.sendMessage(c + "/startpoint [1,2,3,4,etc] [1,2,3,4,etc] - Sets the starting points of tributes in a specific arena!");
 					sender.sendMessage(c + "/ha close (1,2,3,4,etc) - Prevents anyone from joining that arena! Numbers are optional");
 					sender.sendMessage(c + "/ha help - Displays this screen!");
 					sender.sendMessage(c + "/ha join [1,2,3,4,etc] - Makes you join the game!");
 					sender.sendMessage(c + "/ha kick [Player] - Kicks a player from the arena!");
 					sender.sendMessage(c + "/ha leave - Makes you leave the game!");
 					sender.sendMessage(c + "/ha list (1,2,3,4,etc) - Shows a list of players in the game and their health! Numbers are optional.");
 					sender.sendMessage(c + "/ha open (1,2,3,4,etc) - Opens the game allowing people to join! Numbers are optional");
 					sender.sendMessage(c + "/ha ready - Votes for the game to start!");
 					sender.sendMessage(c + "/ha refill (1,2,3,4,etc) - Refills all chests! Numbers are optional");
 					sender.sendMessage(c + "/ha reload - Reloads the config!");
 					sender.sendMessage(c + "/ha restart (1,2,3,4,etc) - Restarts the game! Numbers are optional");
 					sender.sendMessage(c + "/ha rlist (1,2,3,4,etc) - See who's ready! Numbers are optional");
 					sender.sendMessage(c + "/ha setspawn - Sets the spawn for dead tributes!");
 					sender.sendMessage(c + "/ha tp [player] - Teleports you to a tribute!");
 					sender.sendMessage(c + "/ha start [1,2,3,4,etc] - Unfreezes tributes allowing them to fight!");
 					sender.sendMessage(c + "/ha watch [1,2,3,4,etc] - Lets you watch the tributes!");
 					sender.sendMessage(c + "/ha warpall [1,2,3,4,etc] - Warps all tribute into position!");
 					sender.sendMessage(ChatColor.GREEN + "----------------------");
 					return false;
 				}else if(args[0].equalsIgnoreCase("List")){
 					if(args.length>= 2){
 						try{
 							a = Integer.parseInt(args[1]);
 							sender.sendMessage(ChatColor.AQUA + "----- Arena " + a + " -----");
 							if(!plugin.Playing.get(a).isEmpty() && plugin.Playing.containsKey(a)){
 								for(String playernames: plugin.Playing.get(a)){
 									Player players = plugin.getServer().getPlayerExact(playernames);
 									sender.sendMessage(ChatColor.GREEN + playernames + " Life: " + players.getHealth() + "/20");
 								}
 							}else{
 								sender.sendMessage(ChatColor.GRAY + "No one is playing!");
 							}
 							sender.sendMessage(ChatColor.AQUA + "---------------------");
 						}catch(Exception e){
 							sender.sendMessage(ChatColor.RED + "Argument not an integer!");
 						}
 					}else{
 						sender.sendMessage(ChatColor.AQUA + "----- Arena 1 -----");
 						if(!plugin.Playing.get(1).isEmpty() && plugin.Playing.containsKey(1)){
 							for(String playernames: plugin.Playing.get(1)){
 								Player players = plugin.getServer().getPlayerExact(playernames);
 								sender.sendMessage(ChatColor.GREEN + playernames + " Life: " + players.getHealth() + "/20");
 							}
 						}else{
 							sender.sendMessage(ChatColor.GRAY + "No one is playing!");
 						}
 						sender.sendMessage(ChatColor.AQUA + "---------------------");
 					}
 				}else if(args[0].equalsIgnoreCase("rList")){
 					if(args.length>= 2){
 						try{
 							a = Integer.parseInt(args[1]);
 							sender.sendMessage(ChatColor.AQUA + "----- Arena " + a + " -----");
 							if(!plugin.Ready.get(a).isEmpty() && plugin.Ready.containsKey(a)){
 								for(String playernames: plugin.Ready.get(a)){
 									sender.sendMessage(ChatColor.GREEN + playernames);
 								}
 							}else{
 								sender.sendMessage(ChatColor.GRAY + "No one is ready!");
 							}
 							sender.sendMessage(ChatColor.AQUA + "---------------------");
 						}catch(Exception e){
 							sender.sendMessage(ChatColor.RED + "Argument not an integer!");
 						}
 					}else{
 						sender.sendMessage(ChatColor.AQUA + "----- Arena 1 -----");
 						if(!plugin.Ready.get(1).isEmpty() && plugin.Ready.containsKey(1)){
 							for(String playernames: plugin.Ready.get(1)){
 								sender.sendMessage(ChatColor.GREEN + playernames);
 							}
 						}else{
 							sender.sendMessage(ChatColor.GRAY + "No one is ready!");
 						}
 						sender.sendMessage(ChatColor.AQUA + "---------------------");
 					}
 				}else if(args[0].equalsIgnoreCase("SetSpawn") || args[0].equalsIgnoreCase("Join") || args[0].equalsIgnoreCase("Confirm") || args[0].equalsIgnoreCase("Ready") || args[0].equalsIgnoreCase("Leave") || args[0].equalsIgnoreCase("Watch")){
 					sender.sendMessage(ChatColor.RED + "That can only be run by a player!");
 				}else if(args[0].equalsIgnoreCase("Kick")){
 					if(args.length>= 2){
 						Player target = Bukkit.getPlayer(args[1]);
 						if(plugin.getArena(target) != null){
 							a = plugin.getArena(target);
 							plugin.Playing.get(a).remove(target.getName());
 							if(plugin.config.getBoolean("broadcastAll")){
 								sender.getServer().broadcastMessage(ChatColor.RED + target.getName() + " was kicked from arena " + a + "!");
 							}else{
 								for(String gn: plugin.Playing.get(a)){
 									Player g = plugin.getServer().getPlayer(gn);
 									g.sendMessage(ChatColor.RED + target.getName() + " was kicked from the game!");
 								}
 							}
 							clearInv(target);
 							target.teleport(Spawn);
 							plugin.Quit.get(a).add(target.getName());
 							plugin.winner(a);
 						}else{
 							sender.sendMessage(ChatColor.RED + "That player isn't in the game!");
 						}
 					}else{
 						sender.sendMessage(ChatColor.RED + "Too few arguments");
 					}
 				}else if(args[0].equalsIgnoreCase("Refill")){
 					if(args.length>= 2){
 						a = Integer.parseInt(args[1]);
 						int list056;
 						list056 = 0;
 						int limit = plugin.MyChests.getStringList("StorageXYZ").size();
 						while(limit > list056){
 							String xyz2 = plugin.getChests().getStringList("StorageXYZ").get(list056);
 							int chestx = plugin.getChests().getInt("Storage." + xyz2 + ".Location.X");
 							int chesty = plugin.getChests().getInt("Storage." + xyz2 + ".Location.Y");
 							int chestz = plugin.getChests().getInt("Storage." + xyz2 + ".Location.Z");
 							int chesta = plugin.getChests().getInt("Storage." + xyz2 + ".Arena");
 							String chestw = plugin.getChests().getString("Storage." + xyz2 + ".Location.W");
 							Block blockatlocation = Bukkit.getWorld(chestw).getBlockAt(chestx, chesty, chestz);  
 							plugin.exists = false;
 							if(chesta== a){
 								if(blockatlocation.getState() instanceof Chest){
 									plugin.exists = true;
 									Chest chest = (Chest) blockatlocation.getState();
 									if(chesta== a){
 										chest.getInventory().clear();
 										ItemStack[] itemsinchest = null;
 										Object o = plugin.getChests().get("Storage." + xyz2 + ".ItemsInStorage");
 										if(o instanceof ItemStack[]){
 											itemsinchest = (ItemStack[]) o;
 										}else if(o instanceof List){
 											itemsinchest = (ItemStack[]) ((List<ItemStack>) o).toArray(new ItemStack[0]);
 										}
 										list056 = list056+1;
 										chest.getInventory().setContents(itemsinchest);
 										chest.update();
 									}
 								}
 							}else{
 								list056 = list056+1;
 							}
 						}
 						if(limit== list056){
 							sender.sendMessage(ChatColor.GREEN + "All for arena " + a + " refilled!");
 						}
 					}else{
 						int list056;
 						list056 = 0;
 						int limit = plugin.MyChests.getStringList("StorageXYZ").size();
 						while(limit > list056){
 							String xyz2 = plugin.getChests().getStringList("StorageXYZ").get(list056);
 							int chestx = plugin.getChests().getInt("Storage." + xyz2 + ".Location.X");
 							int chesty = plugin.getChests().getInt("Storage." + xyz2 + ".Location.Y");
 							int chestz = plugin.getChests().getInt("Storage." + xyz2 + ".Location.Z");
 							String chestw = plugin.getChests().getString("Storage." + xyz2 + ".Location.W");
 							Block blockatlocation = Bukkit.getWorld(chestw).getBlockAt(chestx, chesty, chestz);  
 							plugin.exists = false;
 							if(blockatlocation.getState() instanceof Chest){
 								plugin.exists = true;
 								Chest chest = (Chest) blockatlocation.getState();
 								chest.getInventory().clear();
 								ItemStack[] itemsinchest = null;
 								Object o = plugin.getChests().get("Storage." + xyz2 + ".ItemsInStorage");
 								if(o instanceof ItemStack[]){
 									itemsinchest = (ItemStack[]) o;
 								}else if(o instanceof List){
 									itemsinchest = (ItemStack[]) ((List<ItemStack>) o).toArray(new ItemStack[0]);
 								}
 								list056 = list056+1;
 								chest.getInventory().setContents(itemsinchest);
 								chest.update();
 							}
 						}
 						if(limit== list056){
 							sender.sendMessage(ChatColor.GREEN + "All chests refilled!");
 						}
 					}
 				}else if(args[0].equalsIgnoreCase("Restart")){
 					int b = 0;
 					i = 1;
 					int e = plugin.open.size();
 					if(args.length>= 2){
 						i = Integer.parseInt(args[1]);
 						if(i > e) i=e;
 						if(i < 1) i=1;
 						e = i;
 					}
 					for(a = i; a <= e; a++){
 						if(!plugin.Playing.get(a).isEmpty()){
 							for(b = 0; b < plugin.Playing.get(a).size(); b++){
 								String s = plugin.Playing.get(a).get(b);
 								Player tributes = plugin.getServer().getPlayerExact(s);
 								clearInv(tributes);
 								tributes.teleport(tributes.getWorld().getSpawnLocation());
 							}
 						}
 						// ^^
 						if(!plugin.Watching.get(a).isEmpty()){
 							for(b = 0; b < plugin.Watching.get(a).size(); b++){
 								String s = plugin.Watching.get(a).get(b);
 								Player spectator = plugin.getServer().getPlayerExact(s);
 								spectator.setAllowFlight(false);
 								spectator.teleport(Spawn);
 								for(Player online:plugin.getServer().getOnlinePlayers()){
 									online.showPlayer(spectator);
 								}
 							}
 						}
 						plugin.Dead.get(a).clear();
 						plugin.Quit.get(a).clear();
 						plugin.Watching.get(a).clear();
 						plugin.Frozen.get(a).clear();
 						plugin.Ready.get(a).clear();
 						plugin.NeedConfirm.get(a).clear();
 						plugin.Out.get(a).clear();
 						plugin.Playing.get(a).clear();
 						plugin.inArena.get(a).clear();
 						plugin.canjoin.put(a, false);
 						plugin.open.put(a, true);
 						List<String> blocksbroken = plugin.data.getStringList("Blocks_Destroyed");
 						List<String> blocksplaced = plugin.data.getStringList("Blocks_Placed");
 						ArrayList<String> toremove = new ArrayList<String>();
 						ArrayList<String> toremove2 = new ArrayList<String>();
 						for(String blocks:blocksplaced){
 							String[] coords = blocks.split(",");
 							World w = plugin.getServer().getWorld(coords[0]);
 							double x = Double.parseDouble(coords[1]);
 							double y = Double.parseDouble(coords[2]);
 							double z = Double.parseDouble(coords[3]);
 							int arena = Integer.parseInt(coords[4]);
 							int d = 0;
 							byte m = 0;
 							Location blockl = new Location(w, x, y, z);
 							Block block = w.getBlockAt(blockl);
 							if(arena== a){
 								block.setTypeIdAndData(d, m, true);
 								block.getState().update();
 								toremove.add(blocks);
 							}
 						}
 						for(String blocks:blocksbroken){
 							String[] coords = blocks.split(",");
 							World w = plugin.getServer().getWorld(coords[0]);
 							double x = Double.parseDouble(coords[1]);
 							double y = Double.parseDouble(coords[2]);
 							double z = Double.parseDouble(coords[3]);
 							int d = Integer.parseInt(coords[4]);
 							byte m = Byte.parseByte(coords[5]);
 							int arena = Integer.parseInt(coords[6]);
 							Location blockl = new Location(w, x, y, z);
 							Block block = w.getBlockAt(blockl);
 							if(arena== a){
 								block.setTypeIdAndData(d, m, true);
 								block.getState().update();
 								toremove2.add(blocks);
 							}
 						}
 						for(String blocks: toremove){
 							blocksplaced.remove(blocks);
 						}
 						for(String blocks: toremove2){
 							blocksbroken.remove(blocks);
 						}
 						toremove.clear();
 						toremove2.clear();
 						plugin.data.set("Blocks_Destroyed", blocksbroken);
 						plugin.data.set("Blocks_Placed", blocksplaced);
 						plugin.data.options().copyDefaults();
 						plugin.saveData();
 						plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "ha refill " + a);
 						sender.sendMessage(ChatColor.AQUA + "Arena " + a + " has been reset!");
 					}
 					/////////////////////////////////// Toggle //////////////////////////////////////////////////
 				}else if(args[0].equalsIgnoreCase("close")){
 					i = 1;
 					int e = plugin.open.size();
 					if(args.length>= 2){
 						i = Integer.parseInt(args[1]);
 						if(i > e) i=e;
 						if(i < 1) i=1;
 						e = i;
 					}
 					for(a = i; a <= e; a++){
 						if(plugin.open.get(a)){
 							plugin.open.put(a, false);
 							if(plugin.Playing.get(a)!= null){
 								for(String players: plugin.Playing.get(a)){
 									Player tributes = plugin.getServer().getPlayerExact(players);
 									clearInv(tributes);
 									tributes.teleport(tributes.getWorld().getSpawnLocation());
 								}
 							}
 							if(plugin.Watching.get(a)!= null){
 								for(String sname: plugin.Watching.get(a)){
 									Player spectators = plugin.getServer().getPlayerExact(sname);
 									spectators.teleport(spectators.getWorld().getSpawnLocation());
 									spectators.setAllowFlight(false);
 									for(Player online:plugin.getServer().getOnlinePlayers()){
 										online.showPlayer(spectators);
 									}
 								}
 							}
 							plugin.Dead.get(a).clear();
 							plugin.Quit.get(a).clear();
 							plugin.Watching.get(a).clear();
 							plugin.Frozen.get(a).clear();
 							plugin.Ready.get(a).clear();
 							plugin.NeedConfirm.get(a).clear();
 							plugin.Out.get(a).clear();
 							plugin.Playing.get(a).clear();
 							plugin.inArena.get(a).clear();
 							Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "ha refill " + a);
 							sender.sendMessage(ChatColor.GOLD + "Arena " + a + " Closed!");
 						}else{
 							sender.sendMessage(ChatColor.RED + "Arena " + a + " already closed, type /ha open to re-open them!");
 						}
 					}
 				}else if(args[0].equalsIgnoreCase("open")){ // Jeppa: i / a fixed ;)
 					if(args.length>= 2){
 						a = Integer.parseInt(args[1]);
 						if(!plugin.open.get(a)){
 							plugin.open.put(a, true);
 							sender.sendMessage(ChatColor.GOLD + "Arena " + a + " Open!");
 						}else{
 							sender.sendMessage(ChatColor.RED + "Arena " + a + " already open, type /ha close to close them!");
 						}
 					}else{
 						for(i = 1; i <= plugin.open.size(); i++){
 							if(!plugin.open.get(i)){
 								plugin.open.put(i, true);
 								sender.sendMessage(ChatColor.GOLD + "Arena " + i + " Open!");
 							}else{
 								sender.sendMessage(ChatColor.RED + "Arena " + i + " already open, type /ha close to close them!");
 							}
 						}
 					}
 					////////////////////////////////////////////////////////////////////////////////////////////
 				}else if(args[0].equalsIgnoreCase("Reload")){
 					plugin.location.clear();
 					plugin.Reward.clear();
 					plugin.Cost.clear();
 					plugin.Fee.clear();
 					HandlerList.unregisterAll(plugin); 	//Jeppa: Close all running Listeners before reopening them!
 					plugin.reloadConfig();
 					plugin.onEnable();
 					sender.sendMessage(ChatColor.AQUA + "HungerArena Reloaded!");
 				}else if(args[0].equalsIgnoreCase("WarpAll")){
 					if(plugin.spawns.getString("Spawns_set").equalsIgnoreCase("false")){
 						sender.sendMessage(ChatColor.RED + "/ha setspawn hasn't been run!");
 					}else{
 						if(args.length>= 2){
 							a = Integer.parseInt(args[1]);
 							if(plugin.Playing.get(a).size()== 1){
 								sender.sendMessage(ChatColor.RED + "There are not enough players!");
 							}else{
 								if(plugin.config.getString("Auto_Start").equalsIgnoreCase("true")){
 									plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
 										public void run(){
 											Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "ha start " + a);
 										}
 									}, 20L);
 								}
 								i = 1;
 								for(String playing: plugin.Playing.get(a)){
 									Player tribute = plugin.getServer().getPlayerExact(playing);
 									tribute.teleport(plugin.location.get(a).get(i));
 									tribute.setHealth(20);
 									tribute.setFoodLevel(20);
 									tribute.setSaturation(20);
 									tribute.setLevel(0);
 									clearInv(tribute);
 									for(PotionEffect pe: tribute.getActivePotionEffects()){
 										PotionEffectType potion = pe.getType();
 										tribute.removePotionEffect(potion);
 									}
 									if(tribute.getAllowFlight()){
 										tribute.setAllowFlight(false);
 									}
 									plugin.Frozen.get(a).add(tribute.getName());
 									i = i+1;
 								}
 								plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
 									public void run(){
 										sender.sendMessage(ChatColor.AQUA + "All Tributes warped!");
 									}
 								}, 20L);
 							}
 						}else{
 							sender.sendMessage(ChatColor.RED + "Too few arguments, specify an arena");
 						}
 					}
 				}else if(args[0].equalsIgnoreCase("Start")){
 					if(args.length!= 2){
 						sender.sendMessage(ChatColor.RED + "You need an arena!");
 					}else{
 						a = Integer.parseInt(args[1]);
 						if(plugin.canjoin.get(a)== true)
 							sender.sendMessage(ChatColor.RED + "Game already in progress!");
 						else if(plugin.Playing.get(a).isEmpty())
 							sender.sendMessage(ChatColor.RED + "No one is in that game!");
 						else
 							plugin.startGames(a);
 					}
 				}else{
 					sender.sendMessage(ChatColor.RED + "Unknown command, type /ha help to see all commands!");
 				}
 			}
 		}
 		return false;
 	}
 }
