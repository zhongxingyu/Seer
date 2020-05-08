 package me.cakenggt.CakesMinerApocalypse;
 
 import java.io.*;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.*;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerToggleSneakEvent;
 import org.bukkit.inventory.ItemStack;
 
 
 public class CakesMinerApocalypsePlayerLogin implements Listener {
 	CakesMinerApocalypse p;
 	public CakesMinerApocalypsePlayerLogin(CakesMinerApocalypse plugin) {
 		p = plugin;
 	}
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerJoin(PlayerLoginEvent event) {
 		if (!this.p.getOn().get(event.getPlayer().getWorld())){
 			return;
 		}
 		if (p.getConfig().getBoolean("alwaysSneak", true)) {
 			event.getPlayer().setSneaking(true);
 		}
 		if (!this.p.getRandomSpawn())
 			return;
 		final Player thePlayer = event.getPlayer();
 		File playerDat = new File(event.getPlayer().getWorld().getName() + "/players/"+ thePlayer.getName() + ".dat");
 		if(!playerDat.exists())
 		{
 			System.out.println(thePlayer.getDisplayName() + " first join in " + thePlayer.getWorld().getName());
 			int size = this.p.getSize();
 			Location playerLoc = event.getPlayer().getWorld().getSpawnLocation();
 			Location teleLoc = playerLoc;
 			double randX = size * Math.random();
 			double randZ = size * Math.random();
 			randX += -1 * size/2;
 			randZ += -1 * size/2;
 			teleLoc.setX(randX);
 			teleLoc.setZ(randZ);
 			teleLoc.getWorld().loadChunk((int)randX, (int)randZ, true);
 			teleLoc.setY(teleLoc.getWorld().getHighestBlockAt(teleLoc).getY());
 			//System.out.println(randX + " " + randZ);
 			//event.getPlayer().getWorld().setSpawnLocation((int)teleLoc.getX(), (int)teleLoc.getY(), (int)teleLoc.getZ());
 			//System.out.println(teleLoc.getX() + " " + teleLoc.getZ());
 			final Location teleportLoc = teleLoc;
 			@SuppressWarnings("unused")
 			int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(p, new Runnable() {
 			    @Override
 			    public void run() {
 			        thePlayer.teleport(teleportLoc);
 			        thePlayer.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
 			        thePlayer.getInventory().addItem(new ItemStack(Material.BOAT, 1));
 			        thePlayer.getInventory().addItem(new ItemStack(Material.SEEDS, 20));
 			        thePlayer.getInventory().addItem(new ItemStack(Material.BREAD, 5));
 			        thePlayer.getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET, 1));
 			        thePlayer.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE, 1));
 			        thePlayer.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS, 1));
 			        thePlayer.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS, 1));
 			        thePlayer.setNoDamageTicks(100);
 			    }
 			}, 10L);
 			//event.getPlayer().teleport(teleLoc);
 		}
 	}
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerRespawn(PlayerRespawnEvent event){
 		if (!this.p.getOn().get(event.getPlayer().getWorld())){
 			return;
 		}
 		if (p.getConfig().getBoolean("alwaysSneak", true)) {
 			event.getPlayer().setSneaking(true);
 		}
 		if (!this.p.getRandomSpawn())
 			return;
 
 		boolean shouldRandomSpawn = event.getPlayer().getBedSpawnLocation() == null || event.getPlayer().getKiller() != null || event.getPlayer().getFoodLevel() < 10;
 
 		if (p.getConfig().getBoolean("randomRespawnAlways", false)) {
 			shouldRandomSpawn = true;
 		}
 
         if (shouldRandomSpawn) {
 			int size = this.p.getSize();
 			final Player thePlayer = event.getPlayer();
 			Location playerLoc = event.getPlayer().getWorld().getSpawnLocation();
 			Location teleLoc = playerLoc;
 			double randX = size * Math.random();
 			double randZ = size * Math.random();
 			randX += -1 * size/2;
 			randZ += -1 * size/2;
 			teleLoc.setX(randX);
 			teleLoc.setZ(randZ);
 			teleLoc.getWorld().loadChunk((int)randX, (int)randZ, true);
 			teleLoc.setY(teleLoc.getWorld().getHighestBlockAt(teleLoc).getY());
 			//event.setRespawnLocation(teleLoc);
 			final Location teleportLoc = teleLoc;
 			@SuppressWarnings("unused")
 			int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(p, new Runnable() {
 			    @Override
 			    public void run() {
 			        thePlayer.teleport(teleportLoc);
 			        thePlayer.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
 			        thePlayer.getInventory().addItem(new ItemStack(Material.BOAT, 1));
 			        thePlayer.getInventory().addItem(new ItemStack(Material.SEEDS, 20));
 			        thePlayer.getInventory().addItem(new ItemStack(Material.BREAD, 5));
 			        thePlayer.getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET, 1));
 			        thePlayer.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE, 1));
 			        thePlayer.getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS, 1));
 			        thePlayer.getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS, 1));
 			        thePlayer.setNoDamageTicks(100);
 			    }
 			}, 10L);
 			//event.getPlayer().teleport(teleLoc);
 		}
 		else
 			return;
 	}
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerChat(PlayerChatEvent event){
		double chatDistance = (double)this.p.getChatDistance();
		if (chatDistance == -5)
			return;

 		Player sender = event.getPlayer();
 		Set<Player> recipients = event.getRecipients();
 		String message = event.getMessage();
 		if (!this.p.getOn().get(event.getPlayer().getWorld())){
 			for (Player re : recipients){
 				if (!this.p.getOn().get(re.getWorld())){
 					re.sendMessage(sender.getPlayerListName() + ": " + message);
 				}
 			}
 			event.setCancelled(true);
 			return;
 		}
 		System.out.println("<" + sender.getPlayerListName() + "> " + message);
 		int rec = 0;
 		for (Player recipient : recipients){
 			if (recipient.getLocation().getWorld() == sender.getLocation().getWorld()) {
 				double distance = recipient.getLocation().distance(
 						sender.getLocation());
 				if (distance <= chatDistance/2) {
 					recipient.sendMessage(ChatColor.GREEN
 							+ sender.getPlayerListName() + ": " + message);
 					rec ++;
 				}
 				else if (distance > chatDistance/2 && distance <= chatDistance) {
 					int messageLength = message.length();
 					double percent = (distance - 25) / 25;
 					int amountRemoved = (int) (percent * ((double) messageLength));
 					char[] charString = message.toCharArray();
 					for (int k = 0; k < amountRemoved; k++) {
 						int removalPoint = (int) (Math.random() * (charString.length - 1));
 						charString[removalPoint] = ' ';
 					}
 					String charMessage = new String(charString);
 					recipient.sendMessage(ChatColor.GREEN
 							+ sender.getPlayerListName() + ": " + charMessage);
 					rec ++;
 				}
 			}
 		}
 		System.out.println("Message received by " + rec + " players.");
 		event.setCancelled(true);
 	}
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		//System.out.println("Player interact event");
 		if (!this.p.getOn().get(event.getPlayer().getWorld())){
 			return;
 		}
 		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
 			return;
 		Player player = event.getPlayer();
 		Block block = event.getClickedBlock();
 		//System.out.println(block.getBlockPower(BlockFace.NORTH) > 0);
 		//System.out.println(block.getBlockPower(BlockFace.SOUTH) > 0);
 		//System.out.println(block.getBlockPower(BlockFace.EAST) > 0);
 		//System.out.println(block.getBlockPower(BlockFace.WEST) > 0);
 		if (player.getItemInHand() == null){
 			return;
 		}
 		if (block.getType() == Material.JUKEBOX && player.getItemInHand().getType() == Material.COMPASS && event.getAction() == Action.RIGHT_CLICK_BLOCK && (block.getBlockPower(BlockFace.NORTH) > 0 || block.getBlockPower(BlockFace.SOUTH) > 0 || block.getBlockPower(BlockFace.EAST) > 0 || block.getBlockPower(BlockFace.WEST) > 0) ){
 			player.setCompassTarget(block.getLocation());
 		}
 		else
 			return;
 	}
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void blockBreak(BlockBreakEvent event) {
 		//System.out.println("block break event");
 		if (!this.p.getOn().get(event.getPlayer().getWorld())){
 			return;
 		}
 		if (event.getBlock().getType() == Material.IRON_DOOR_BLOCK && event.getBlock().getRelative(0, -1, 0).getType() == Material.IRON_DOOR_BLOCK){
 			//System.out.println("top block");
 			if (event.getBlock().getRelative(0, -1, 0).isBlockIndirectlyPowered() || event.getBlock().isBlockIndirectlyPowered())
 				event.setCancelled(true);
 		}
 		if (event.getBlock().getType() == Material.IRON_DOOR_BLOCK && event.getBlock().getRelative(0, 1, 0).getType() == Material.IRON_DOOR_BLOCK){
 			//System.out.println("bottom block");
 			if (event.getBlock().getRelative(0, 1, 0).isBlockIndirectlyPowered() || event.getBlock().isBlockIndirectlyPowered())
 				event.setCancelled(true);
 		}
 	}
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void sneakToggle (PlayerToggleSneakEvent event){
 		if (p.getConfig().getBoolean("alwaysSneak", true)) {
 			event.getPlayer().setSneaking(true);
 			event.setCancelled(true);
 		}
 	}
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void noJoinMessage(PlayerJoinEvent event) {
 		event.setJoinMessage(null);
 	}
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void noQuitMessage(PlayerQuitEvent event) {
 		event.setQuitMessage(null);
 	}
 }
      
