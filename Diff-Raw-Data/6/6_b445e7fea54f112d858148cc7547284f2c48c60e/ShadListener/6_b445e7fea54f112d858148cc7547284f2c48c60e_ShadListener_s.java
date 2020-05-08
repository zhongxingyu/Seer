 package com.shadoom.shadplug;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.World;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.kitteh.tag.PlayerReceiveNameTagEvent;
 import org.kitteh.tag.TagAPI;
 
 @SuppressWarnings("deprecation")
 public class ShadListener implements Listener {
 
 	public static ShadPlug plugin;
 	public static ShadConfig sConfig;
 
 	public ShadListener(ShadPlug instance) {
 		plugin = instance;
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 
 	/*
 	 * Automatic chat
 	 */
 	@EventHandler
 	public void onPlayerChat(PlayerChatEvent event) {
 
 		if (ShadConfig.instantchat.containsKey(event.getPlayer().getName())) {
 
 			event.setCancelled(true);
 			Player player = event.getPlayer();
 			String playername = event.getPlayer().getName();
 
 			List<String> peopletosendto = null;
 
 			String team = "nill";
 
 			if (ShadConfig.instantchat.get(playername).equals("red")) {
 				peopletosendto = plugin.getConfig().getStringList(
 						"ShadPlug.Teams.Red.Members");
 				team = "red";
 			}
 			if (ShadConfig.instantchat.get(playername).equals("blue")) {
 				peopletosendto = plugin.getConfig().getStringList(
 						"ShadPlug.Teams.Blue.Members");
 				team = "blue";
 			}
 
 			if (peopletosendto == null)
 				return;
 
 			int playeron = 0;
 			int sizeoflist = 0;
 			
 			if (player.isOp()) {
 				sizeoflist = ShadConfig.opChatOnline.size();
 			}
 
 			if (team.equals("blue")) {
 				sizeoflist = ShadConfig.bluechatonline.size();
 			}
 
 			if (team.equals("red")) {
 				sizeoflist = ShadConfig.redchatonline.size();
 			}
 
 			while (playeron < sizeoflist) {
 
 				String playerstring = "";
 				
 				if (player.isOp()) {
 					
 					playerstring = ShadConfig.opChatOnline.get(playeron);
 				}
 
 				if (team.equals("blue"))
 					playerstring = ShadConfig.bluechatonline.get(playeron);
 
 				if (team.equals("red"))
 					playerstring = ShadConfig.redchatonline.get(playeron);
 
 				Player playersend = Bukkit.getPlayer(playerstring);
 
 				if (playersend != null) {
 					
 					String message = event.getMessage();
 
 					if (team.equals("red")) {
 						playersend.sendMessage("6[4Red6] 2" + event.getPlayer().getDisplayName()
 								+ "f: e" + message);
 						System.out.println("6[4Red6] 2" + event.getPlayer().getDisplayName()
 								+ "f: e" + message);
 					}
 					if (team.equals("blue")) {
 						playersend.sendMessage("6[1Blue6] 2" + event.getPlayer().getDisplayName()
 								+ "f: e" + message);
 						System.out.println("6[1Blue6] 2" + event.getPlayer().getDisplayName()
 								+ "f: e" + message);
 					}
 				}
 
 				playeron++;
 
 			}
 		}
 
 	}
 
 	/*
 	 * Add Players to their group
 	 */
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		String player = event.getPlayer().getName();
 		Player opPlayer = event.getPlayer();
 		if (opPlayer.isOp()) {
 
 			ShadConfig.opChatOnline.add(player);
 
 			plugin.saveConfig();
 		}
 		
 		if (plugin.getConfig().getStringList("ShadPlug.Teams.Red.Members")
 				.contains(player)) {
 
 			ShadConfig.TeamRed.put(player, "red");
 
 			ShadConfig.redchatonline.add(player);
 
 			plugin.saveConfig();
 		}
 
 		if (plugin.getConfig().getStringList("ShadPlug.Teams.Blue.Members")
 				.contains(player)) {
 			ShadConfig.TeamBlue.put(player, "blue");
 
 			ShadConfig.bluechatonline.add(player);
 
 			plugin.saveConfig();
 		}
 
 	}
 
 	/*
 	 * Remove players from the hashmap when they leave
 	 */
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		String player = event.getPlayer().getName();
 		Player opPlayer = event.getPlayer();
 		if (opPlayer.isOp()) {
 
 			ShadConfig.opChatOnline.remove(player);
 
 			plugin.saveConfig();
 		}
 		if (plugin.getConfig().getStringList("ShadPlug.Teams.Red.Members")
 				.contains(player)) {
 
 			ShadConfig.TeamRed.remove(player);
 
 			ShadConfig.redchatonline.remove(player);
 
 			plugin.saveConfig();
 		}
 
 		if (plugin.getConfig().getStringList("ShadPlug.Teams.Blue.Members")
 				.contains(player)) {
 			ShadConfig.TeamBlue.remove(player);
 
 			ShadConfig.bluechatonline.remove(player);
 
 			plugin.saveConfig();
 		}
 
 	}
 
 	/*
 	 * Respawn on Teamspawn
 	 */
 	@EventHandler
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		Player player = event.getPlayer();
 
 		if (ShadConfig.lastworlddeath.containsKey(player.getName())) {
 
 			ShadConfig.lastworlddeath.remove(player.getName());
 
 			if (ShadConfig.TeamRed.containsKey(player.getName())) {
 				World world;
 				world = plugin.getServer().getWorld(
 						plugin.getConfig()
 								.getString("ShadPlug.Spawn.Red.World"));
 
 				Location location = new Location(world, plugin.getConfig()
 						.getDouble("ShadPlug.Spawn.Red.X"), plugin.getConfig()
 						.getDouble("ShadPlug.Spawn.Red.Y"), plugin.getConfig()
 						.getDouble("ShadPlug.Spawn.Red.Z"), (float) plugin
 						.getConfig().getDouble("ShadPlug.Spawn.Red.Yaw"),
 						(float) plugin.getConfig().getDouble(
 								"ShadPlug.Spawn.Red.Pitch"));
 
 				event.setRespawnLocation(location);
 				player.sendMessage("You have been respawned at the Team "
 						+ ChatColor.RED + "Red" + ChatColor.WHITE + "'s HQ.");
 				player.getInventory().setHelmet(
 						new ItemStack(Material.WOOL, 1,
 								(short) 14));
 			}
 			if (ShadConfig.TeamBlue.containsKey(player.getPlayer().getName())) {
 				World world;
 				world = plugin.getServer().getWorld(
 						plugin.getConfig().getString(
 								"ShadPlug.Spawn.Blue.World"));
 				Location location = new Location(world, plugin.getConfig()
 						.getDouble("ShadPlug.Spawn.Blue.X"), plugin.getConfig()
 						.getDouble("ShadPlug.Spawn.Blue.Y"), plugin.getConfig()
 						.getDouble("ShadPlug.Spawn.Blue.Z"), (float) plugin
 						.getConfig().getDouble("ShadPlug.Spawn.Blue.Yaw"),
 						(float) plugin.getConfig().getDouble(
 								"ShadPlug.Spawn.Blue.Pitch"));
 				event.setRespawnLocation(location);
 				player.sendMessage("You have been respawned at the Team "
 						+ ChatColor.BLUE + "Blue" + ChatColor.WHITE + "'s HQ.");
 				player.getInventory().setHelmet(
 						new ItemStack(Material.WOOL, 1,
 								(short) 11));
 			}
 
 		}
 	}
 
 	/*
 	 * Scoring
 	 */
 	public void onEntityDeath(PlayerDeathEvent event) {
 
 		Entity entitythatdied = event.getEntity();
 
 		if (entitythatdied instanceof Player) {
 
 			Player playerthatdied = (Player) entitythatdied;
 
 			if (playerthatdied.getKiller() instanceof Arrow) {
 
 				Arrow arrow = (Arrow) ((Player) entitythatdied).getKiller();
 
 				Entity shooter = arrow.getShooter();
 
 				if (shooter instanceof Player) {
 
 					Player playerthatkilled = (Player) shooter;
 
 					if (ShadConfig.TeamBlue.containsKey(playerthatkilled
 							.getName())
 							&& ShadConfig.TeamRed.containsKey(playerthatdied
 									.getName())
 							&& (plugin.getConfig().getStringList(
 									"ShadPlug.Worlds")
 									.contains(playerthatkilled.getWorld()
 											.getName()))) {
 
 						plugin.getServer().broadcastMessage("Blue Killed Red");
 
 						ShadConfig.blueScore++;
 						playerthatkilled.getWorld().playSound(playerthatkilled.getLocation(), Sound.ORB_PICKUP, 10, 1);
 						plugin.getConfig().set("ShadPlug.Teams.Blue.Score",
 								ShadConfig.blueScore);
 
 						ShadConfig.lastworlddeath.put(playerthatdied.getName(),
 								playerthatdied.getWorld().getName());
 
 						plugin.saveConfig();
 
 					}
 					if (ShadConfig.TeamRed.containsKey(playerthatkilled
 							.getName())
 							&& ShadConfig.TeamBlue.containsKey(playerthatdied
 									.getName())
 							&& (plugin.getConfig().getStringList(
 									"ShadPlug.Worlds")
 									.contains(playerthatkilled.getWorld()
 											.getName()))) {
 						plugin.getServer().broadcastMessage("Red Killed Blue");
 
 						ShadConfig.redScore++;
 						playerthatkilled.getWorld().playSound(playerthatkilled.getLocation(), Sound.ORB_PICKUP, 10, 1);
 						plugin.getConfig().set("ShadPlug.Teams.Red.Score",
 								ShadConfig.redScore);
 						plugin.saveConfig();
 						plugin.saveConfig();
 
 					}
 
 				}
 
 			}
 
 			if (playerthatdied.getKiller() instanceof Player) {
 
 				Player playerthatkilled = (Player) playerthatdied.getKiller();
 
 				if (ShadConfig.TeamBlue.containsKey(playerthatkilled.getName())
 						&& ShadConfig.TeamRed.containsKey(playerthatdied
 								.getName())
 						&& (plugin.getConfig().getStringList("ShadPlug.Worlds")
 								.contains(playerthatkilled.getWorld().getName()))) {
 
 					plugin.getServer().broadcastMessage("Blue Killed Red");
 
 					ShadConfig.blueScore++;
 					plugin.getConfig().set("ShadPlug.Teams.Blue.Score",
 							ShadConfig.blueScore);
 
 					ShadConfig.lastworlddeath.put(playerthatdied.getName(),
 							playerthatdied.getWorld().getName());
 
 					plugin.saveConfig();
 
 				}
 				if (ShadConfig.TeamRed.containsKey(playerthatkilled.getName())
 						&& ShadConfig.TeamBlue.containsKey(playerthatdied
 								.getName())
 						&& (plugin.getConfig().getStringList("ShadPlug.Worlds")
 								.contains(playerthatkilled.getWorld().getName()))) {
 					plugin.getServer().broadcastMessage("Red Killed Blue");
 
 					ShadConfig.redScore++;
 					plugin.getConfig().set("ShadPlug.Teams.Red.Score",
 							ShadConfig.redScore);
 					plugin.saveConfig();
 					plugin.saveConfig();
 
 				}
 			}
 
 		}
 	}
 
 	/*
 	 * FriendlyFire check
 	 */
 	@EventHandler
 	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
 		if (event.getEntity() instanceof Player
 				&& event.getDamager() instanceof Player) {
 			Player attacker = (Player) event.getDamager();
 			Player defender = (Player) event.getEntity();
 			if (ShadConfig.TeamRed.containsKey(defender.getName())
 					&& ShadConfig.TeamRed.containsKey(attacker.getName())
 					&& plugin.getConfig().getStringList("ShadPlug.Worlds")
 							.contains(attacker.getWorld().getName())) {
 				event.setCancelled(true);
 				attacker.sendMessage(ChatColor.LIGHT_PURPLE
 						+ "Do not attack your Teammates!");
 			}
 			if (ShadConfig.TeamBlue.containsKey(defender.getName())
 					&& ShadConfig.TeamBlue.containsKey(attacker.getName())
 					&& plugin.getConfig().getStringList("ShadPlug.Worlds")
 							.contains(attacker.getWorld().getName())) {
 				event.setCancelled(true);
 				attacker.sendMessage(ChatColor.LIGHT_PURPLE
 						+ "Do not attack your Teammates!");
 			}
 		}
 	}
 	/*
 	 * Make em not remove the wool helmet by clicking
 	 */
 	@EventHandler
 	public void onInventoryClick(InventoryClickEvent event) {
 		Player player = (Player) event.getWhoClicked();
 
 		if (event.getWhoClicked() instanceof Player
				&& ShadConfig.TeamBlue.containsKey(player.getName())
				|| ShadConfig.TeamRed.containsKey(player.getName()) 
 				&& plugin.getConfig().getStringList("ShadPlug.Worlds")
 					.contains(player.getWorld().getName())){
 			
 			if (event.getSlot() == 39) {
 				
 				event.setCancelled(true);
 				
 			}
 			
 		}
 	}
 	
 	@EventHandler
 	public void onNameTag(PlayerReceiveNameTagEvent event) {
 		
 		Player player = event.getPlayer();
 		String playerNameString = event.getNamedPlayer().getName();
 		if(ShadConfig.TeamBlue.containsKey(event.getNamedPlayer().getName()) && plugin.getConfig().getStringList("ShadPlug.Worlds")
 				.contains(player.getWorld().getName())) {
 			event.setTag(ChatColor.BLUE.toString() + ChatColor.BOLD.toString() + "#" + playerNameString + "#");
 		}
 		if(ShadConfig.TeamRed.containsKey(event.getNamedPlayer().getName()) && plugin.getConfig().getStringList("ShadPlug.Worlds")
 				.contains(player.getWorld().getName())) {
 			event.setTag(ChatColor.RED.toString() + "#" + playerNameString + "#");
 		}
 	}
 	
 	@EventHandler
 	public void onWorldChange(PlayerChangedWorldEvent event){
 		Player player = event.getPlayer();
 		TagAPI.refreshPlayer(player);
 	}
 
 }
