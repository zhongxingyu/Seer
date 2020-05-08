 package net.kiwz.ThePlugin.listeners;
 
 import java.util.logging.Logger;
 
 import net.kiwz.ThePlugin.ThePlugin;
 import net.kiwz.ThePlugin.utils.HandleItems;
 import net.kiwz.ThePlugin.utils.HandlePlaces;
 import net.kiwz.ThePlugin.utils.HandlePlayers;
 import net.kiwz.ThePlugin.utils.HandleWorlds;
 import net.kiwz.ThePlugin.utils.Log;
 import net.kiwz.ThePlugin.utils.MsgToOthers;
 import net.kiwz.ThePlugin.utils.OfflinePlayer;
 import net.kiwz.ThePlugin.utils.Permissions;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.help.HelpTopic;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 
 public class PlayerListener implements Listener {
 	private HandlePlaces places = new HandlePlaces();
 	private HandlePlayers players = new HandlePlayers();
 	private HandleWorlds worlds = new HandleWorlds();
     private HandleItems hItems = new HandleItems();
     private MsgToOthers msg = new MsgToOthers();
     private Permissions perm = new Permissions();
 	private String denyString = ThePlugin.c2 + "Du har ingen tilgang her";
 	
 	@SuppressWarnings("deprecation")
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		Player player = event.getPlayer();
 		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
 
 		if (!worlds.isTrample(block.getWorld()) && event.getAction() == Action.PHYSICAL) {
 			if ((block.getType() == Material.SOIL) || (block.getType() == Material.CROPS)) {
 				event.setCancelled(true);
 				return;
 			}
 		}
 		
 		if (event.getClickedBlock() != null) {
			Material material = event.getClickedBlock().getType();
			if (material == Material.WOOD_DOOR || material == Material.TRAP_DOOR) {
 				return;
 			}
 			if (!places.hasAccess(player, event.getClickedBlock().getLocation())) {
 				event.setCancelled(true);
 				player.updateInventory();
 				player.sendMessage(denyString);
 				return;
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
 		Player player = event.getPlayer();
 		
 		if (event.getRightClicked() != null) {
 			Location loc = event.getRightClicked().getLocation();
 			if (!places.hasAccess(player, loc) && !places.isWilderness(loc)) {
 				event.setCancelled(true);
 				player.sendMessage(denyString);
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
     public void onInventoryClose(InventoryCloseEvent event) {
 		Inventory inv = event.getView().getTopInventory();
 		String invType = inv.getType().toString();
 		if (invType == "PLAYER") {
 			Player holder = (Player) inv.getHolder();
 			if (!holder.isOnline()) {
 				ItemStack[] content = inv.getHolder().getInventory().getContents();
 				OfflinePlayer offlinePlayer = new OfflinePlayer();
 				Player player = offlinePlayer.getPlayer(holder.getName());
 				player.getInventory().setContents(content);
 				player.saveData();
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
 		if (!event.isCancelled()) {
 			Player player = event.getPlayer();
 			String cmd = event.getMessage().split(" ")[0];
 	    	HelpTopic topic = Bukkit.getServer().getHelpMap().getHelpTopic(cmd);
 	    	if (topic != null) {
 	    		Log log = new Log();
 	    		log.logString(" [COMMAND] " + player.getName() + ": " + event.getMessage());
 	    	}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerChat(AsyncPlayerChatEvent event) {
 		//event.setFormat("%s: %s");
 		Player player = event.getPlayer();
 		String msg = player.getName() + ": " + event.getMessage();
 		
 		if (players.isMuted(player.getName())) {
 			player.sendMessage(ThePlugin.c2 + "En admin har bestemt at du ikke fr snakke av gode grunner");
 			event.setCancelled(true);
 			return;
 		}
 		if (player.isOp() || perm.isAdmin(player)) {
 			//event.setFormat(ChatColor.RED + "%s: " + ChatColor.WHITE + "%s");
 			msg = ChatColor.RED + player.getName() + ": " + ChatColor.WHITE + event.getMessage();
 		}
 		for (Player thisPlayer : Bukkit.getServer().getOnlinePlayers()) {
 			thisPlayer.sendMessage(msg);
 		}
 		Log log = new Log();
 		log.logString(" [CHAT] " + player.getName() + ": " + event.getMessage());
 		event.setCancelled(true);
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerDeath(PlayerDeathEvent event) {
 		event.setKeepLevel(true);
 		event.setDroppedExp(0);
 		event.setDeathMessage("");
 		Location loc = event.getEntity().getLocation();
 		String world = loc.getWorld().getName();
 		String x = Double.toString(loc.getX()).replaceAll("\\..*","");
 		String y = Double.toString(loc.getY()).replaceAll("\\..*","");
 		String z = Double.toString(loc.getZ()).replaceAll("\\..*","");
 		event.getEntity().getPlayer().sendMessage(ThePlugin.c2 + "Du dde i " + world
 				+ " X: " + x + " Y: " + y + " Z: " + z);
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		Plugin pl = Bukkit.getServer().getPluginManager().getPlugin("ThePlugin");
 		final Player player = event.getPlayer();
 		final Location loc = worlds.getSpawn(player, player.getWorld().getName());
 		if (player.getBedSpawnLocation() == null) {
 			Bukkit.getScheduler().scheduleSyncDelayedTask(pl, new Runnable() {
 					@Override
 	                public void run() {
 					player.teleport(loc);
 				}
 			}, 1);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerJoin(PlayerJoinEvent event) {
         Player player = event.getPlayer();
         String playerName = player.getName();
         String ip = player.getAddress().toString();
         String worldName = player.getWorld().getName();
         String coords = player.getLocation().getBlockX() + ", " + player.getLocation().getBlockY() + ", " + player.getLocation().getBlockZ();
         String log = "Spiller logget inn";
         String loginMsg = "Noen logget inn";
         event.setJoinMessage("");
         //new Tablist().setColor(player);
         
     	if (perm.isAdmin(player)) {
     		perm.setPermissions(player);
     	}
     	
         if (!player.isOp()) {
         	player.setGameMode(GameMode.SURVIVAL);
         	player.setAllowFlight(false);
         }
         
         if (players.hasPlayedBefore(playerName)) {
 	        players.setLastLogin(playerName);
 	        players.setIP(playerName);
 	        log = playerName + " [" + ip + "] logget inn ([" + worldName + "] " + coords + ")";
 			loginMsg = ThePlugin.c3 + playerName + " logget inn";
 	        player.sendMessage(ThePlugin.c1 + "Velkommen til LarvikGaming");
 	        player.sendMessage(ThePlugin.c1 + "Besk vr hjemmeside p http://larvikgaming.net");
         }
         
         else {
         	players.addPlayer(playerName);
 	        log = playerName + " [" + ip + "] logget inn for frste gang ([" + worldName + "] " + coords + ")";
 			loginMsg = ThePlugin.c3 + playerName + " logget inn for frste gang";
 			Location loc = worlds.getSpawn(player, player.getWorld().getName());
 			player.teleport(loc);
 			hItems.giveItem(player, Material.IRON_PICKAXE, 1);
 			hItems.giveItem(player, Material.IRON_AXE, 1);
 			hItems.giveItem(player, Material.IRON_PICKAXE, 1);
 			hItems.giveItem(player, Material.IRON_AXE, 1);
 			hItems.giveItem(player, Material.GOLD_INGOT, 5);
 			hItems.giveItem(player, Material.WOOD, 64);
 			hItems.giveItem(player, Material.IRON_HELMET, 1);
 			hItems.giveItem(player, Material.IRON_CHESTPLATE, 1);
 			player.sendMessage(ThePlugin.c1 + "############################################");
 			player.sendMessage(ThePlugin.c1 + "Velkommen som ny spiller p LarvikGaming.net");
 			player.sendMessage(ThePlugin.c1 + "Kjekt om du vil lese Info-Tavlen i spawnen");
 			player.sendMessage(ThePlugin.c1 + "Skriv " + ThePlugin.c3 + "/hjelp" + ThePlugin.c1 +
 					" for hjelp, skriv " + ThePlugin.c3 + "/plass" + ThePlugin.c1 + " for beskyttelse");
 			player.sendMessage(ThePlugin.c1 + "Skriv " + ThePlugin.c3 + "/spawn farm" + ThePlugin.c1 +" for  skaffe materialer");
 			player.sendMessage(ThePlugin.c1 + "Skriv " + ThePlugin.c3 + "/spawn world " + ThePlugin.c1 + "for  finne ett sted du vil bygge");
 			player.sendMessage(ThePlugin.c1 + "Skriv " + ThePlugin.c3 + "/plass ny <plass-navn>" + ThePlugin.c1 + " for  lage plass");
 			player.sendMessage(ThePlugin.c1 + "Kostnad for  lage eller flytte plass er 5 gullbarer");
 			player.sendMessage(ThePlugin.c1 + "Du kan eie 3 plasser og invitere hvem du nsker til din plass");
 			player.sendMessage(ThePlugin.c1 + "############################################");
         }
         
         Logger.getLogger("Minecraft").info(log);
 		Log log1 = new Log();
 		log1.logString(" [INFO] " + log);
 		msg.sendMessage(player, loginMsg);
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void OnPlayerQuit(PlayerQuitEvent event) {
         players.setTimePlayed(event.getPlayer().getName());
 		event.setQuitMessage(ThePlugin.c2 + event.getPlayer().getName() + " logget ut");
 		Log log1 = new Log();
 		log1.logString(" [INFO] " + event.getPlayer().getName() + " logget ut");
 		/*for (Player players : Bukkit.getServer().getOnlinePlayers()) {
 			players.getLocation().getWorld().strikeLightningEffect(players.getLocation());
 		}*/
 	}
 }
