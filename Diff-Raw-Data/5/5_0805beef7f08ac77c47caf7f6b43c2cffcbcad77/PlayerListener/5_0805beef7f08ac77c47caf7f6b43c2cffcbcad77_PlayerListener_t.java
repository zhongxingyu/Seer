 package me.kintick.src;
 
 import java.util.Random;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerToggleSneakEvent;
 import org.bukkit.event.server.ServerListPingEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class PlayerListener implements Listener {
 	boolean allowPublic = true;
 	boolean isGame = true;
 	int playercounter = 0;
 	String playerCreeper = null;
 	int minForGame = 3;
 	public Logger _Logger = Logger.getLogger("Minecraft");
 	int creeperLegend = 0;
 	int Highscore = 0;
 	String HSPlayer = null;
 	int maxExplosions = 5;
 	int currExplosions = 0;
 	int numReq = 20;
 	Player[] onlinePlayerList;
 	int ammounttodamage;
 	int minammounttodamage = 5;
 	
 	@EventHandler
     public void onPlayerToggleSneakEvent(PlayerToggleSneakEvent event) {
 		Player player = event.getPlayer();
 	  	World world = player.getWorld();
 	  	if (player.getDisplayName() == playerCreeper){
 			if (player.isSneaking() == true){
 				currExplosions++;
 				world.createExplosion(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 4F, false, false);
 				for (Player playerToDamage : Bukkit.getOnlinePlayers()) {
 					int distance = (int) playerToDamage.getLocation().distance(player.getLocation());
 					if ( distance <= 5) {
 						ammounttodamage = (25 - (distance * 2));
 						if (playerToDamage.getDisplayName() == playerCreeper){ammounttodamage = 2;}
 						playerToDamage.damage(ammounttodamage);
 					}
 				}
 				doPlayerTeleport(player, world);
 				if (currExplosions == maxExplosions){
 					checkCreeperLegend(player);
 					currExplosions = 0;
 			    	playerCreeper = null;
 			    	chooseNewCreeper(null);
 				}
 			}
 		}
 	}
 	
   	@EventHandler(priority=EventPriority.HIGH)
 	public void onPlayerUse(PlayerInteractEvent event){
 	    Player player = event.getPlayer();
 		int numInHand = player.getItemInHand().getAmount();
 		if(player.getItemInHand().getTypeId() == 289){//Gunpowder
 			if (playerCreeper == null){
 				player.sendMessage(ChatColor.AQUA + "There Is No Current Creeper! No Pieces Of Gunpowder Have Been Deducted!");
 				return;
 			}
 			if (playerCreeper == player.getDisplayName()){
 				player.sendMessage(ChatColor.AQUA + "You Are The Creeper! Save Your Gunpowder For If You Survive! No Pieces Of Gunpowder Have Been Deducted!");
 				return;
 			}
 	    	try{
 	    		if (numInHand >= numReq){
 	    			player.sendMessage(ChatColor.RED + "The Creeper Is: " + playerCreeper);
 	    	    	player.setItemInHand(new ItemStack(Material.SULPHUR, numInHand - numReq));
 	    			player.sendMessage(ChatColor.AQUA + "" + numReq +" Piece(s) Of Gunpowder Have Been Deducted!");
 	    			Bukkit.getServer().broadcastMessage(ChatColor.GREEN + player.getDisplayName() + ChatColor.AQUA + " Knows The Identity Of The Creeper!");
 	    		}
 	    		else{
 	    			player.sendMessage(ChatColor.AQUA + "You Need " + (numReq - numInHand) + " More Pieces Of Gunpowder Before You Can Learn The Identity Of The Creeper!");
 	    		}
 	    	}
 	    	finally{
 	    		
 	    	}
 	    }
 		else if(player.getItemInHand().getTypeId() == 264){//diamond
 			Inventory vipChest = Bukkit.createInventory(player, 9, "VIP Chest");
 			if (new Random().nextInt(100) > 50){vipChest.setItem(0, new ItemStack(Material.DIAMOND_SWORD));}							// 50% chance
 			if (new Random().nextInt(100) > 25){vipChest.setItem(1, new ItemStack(Material.SULPHUR, new Random().nextInt(5)));}			// 75% chance
 			if (new Random().nextInt(100) > 0){vipChest.setItem(2, new ItemStack(Material.RAW_FISH, new Random().nextInt(5)));} 		// 100% chance
 			if (new Random().nextInt(100) > 60){vipChest.setItem(3, new ItemStack(Material.BOW));}										// 40% chance
 			if (new Random().nextInt(100) > 60){vipChest.setItem(4, new ItemStack(Material.ARROW, new Random().nextInt(15)));}			// 40% chance
 			if (new Random().nextInt(100) > 0){vipChest.setItem(5, new ItemStack(Material.BAKED_POTATO, new Random().nextInt(5)));}		// 100% chance
 			if (new Random().nextInt(100) > 80){vipChest.setItem(6, new ItemStack(Material.IRON_CHESTPLATE));}							// 20% chance
 			if (new Random().nextInt(100) > 0){vipChest.setItem(7, new ItemStack(Material.APPLE, new Random().nextInt(5)));}			// 100% chance
 			if (new Random().nextInt(100) > 0){vipChest.setItem(8, new ItemStack(Material.COOKIE, new Random().nextInt(5)));}			// 100% chance
 			player.openInventory(vipChest);
 			player.getInventory().setItemInHand(new ItemStack(264, numInHand - 1));
 		}
 		//else{ player.sendMessage("Id Of Item = " + player.getItemInHand().getTypeId());}
 	}
 	
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		//Player player = event.getPlayer();		
 	}
 	
 	@EventHandler
     public void onBlockBreak(BlockBreakEvent event) {
         //Player Player = event.getPlayer();
         //Block Block = event.getBlock();
         //World World = Player.getWorld();
         event.setCancelled(true);
     }
 	
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event){
 		Player player = event.getPlayer();
 		World world = player.getWorld();
 		if (allowPublic == true || player.isOp() == true){
 			playercounter++;
 			if (playercounter < minForGame){
 				event.setJoinMessage(ChatColor.AQUA + player.getName() + " Has joined the game. There are now " + ChatColor.RED + playercounter + ChatColor.AQUA + " players online! " + ChatColor.RED + (minForGame - playercounter) + ChatColor.AQUA + " More Required for the game to start!");
 			}
 			else{
 				event.setJoinMessage(ChatColor.AQUA + player.getName() + " Has joined the game. There are now " + ChatColor.RED + playercounter + ChatColor.AQUA + " players online! ");
 			}
 			player.sendMessage(ChatColor.GREEN + "Lone Creeper: " + ChatColor.AQUA + "HOW TO PLAY:");
 			player.sendMessage("");
 			player.sendMessage(ChatColor.AQUA + "Kill The Creeper And Prevent Them From Gaining A High Kill-Streak!");
 			player.sendMessage("");
 			player.sendMessage(ChatColor.AQUA + "Collect " + ChatColor.RED + numReq + ChatColor.AQUA +" Pieces Of Gunpowder To Aid You In Finding The Identity Of The Creeper!");
 			player.sendMessage("");
 			player.sendMessage(ChatColor.RED +" Right Click These " + numReq + " Pieces Of Gunpowder In Hotbar To Learn The Identity Of the Creeper!");
 			player.sendMessage("");
 			player.sendMessage(ChatColor.AQUA + "If You Are The Creeper, Try To Kill As Many People As Possible Using Whatever Means You Want Or By Using " + ChatColor.RED + "(Shift)" + ChatColor.AQUA + " To Explode! Max " + ChatColor.RED + maxExplosions + ChatColor.AQUA + " Explosions Per Creeper Session!");
 			player.sendMessage("");
 			
 			doPlayerTeleport(player, world);
 			if (playercounter > 0){
 			    onlinePlayerList = Bukkit.getServer().getOnlinePlayers();
 			    if (playercounter >= minForGame ){
 			    	if (playerCreeper == null){
 						chooseNewCreeper(player);
 					}
 			    }
 			}
 		}
 		else{
 			player.kickPlayer("Server is Not Currently Open To Non OPs");
 		}
 	}
 
 	private void doPlayerTeleport(Player player, World world) {
 		Random random = new Random();
 		if (random.nextInt(3) == 0){
 			player.teleport(new Location(world, 0.5 - new Random().nextInt(20), 60, 0.5 + new Random().nextInt(20)));			
 		}
 		else if (random.nextInt(3) == 1){
 			player.teleport(new Location(world, 0.5 - new Random().nextInt(20), 60, 0.5 - new Random().nextInt(20)));			
 		}
 		else if (random.nextInt(3) == 2){
 			player.teleport(new Location(world, 0.5 + new Random().nextInt(20), 60, 0.5 - new Random().nextInt(20)));			
 		}
 		else{
 			player.teleport(new Location(world, 0.5 + new Random().nextInt(20), 60, 0.5 + new Random().nextInt(20)));
 		}
 	}
 	
 	private void doPlayerRespawn(PlayerRespawnEvent event, World world, Player player) {
 		Random random = new Random();
 		if (random.nextInt(3) == 0){
 			event.setRespawnLocation(new Location(world, 0.5 - new Random().nextInt(20), 60, 0.5 + new Random().nextInt(20)));			
 		}
 		else if (random.nextInt(3) == 1){
 			event.setRespawnLocation(new Location(world, 0.5 - new Random().nextInt(20), 60, 0.5 - new Random().nextInt(20)));			
 		}
 		else if (random.nextInt(3) == 2){
 			event.setRespawnLocation(new Location(world, 0.5 + new Random().nextInt(20), 60, 0.5 - new Random().nextInt(20)));			
 		}
 		else{
 			event.setRespawnLocation(new Location(world, 0.5 + new Random().nextInt(20), 60, 0.5 + new Random().nextInt(20)));
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event) {
         Player player = event.getPlayer();
 	    onlinePlayerList = Bukkit.getServer().getOnlinePlayers(); 
 	    playercounter--;
 	    if(playercounter < 0){
 	    	playercounter = 0;
 	    }
 	    if (player.getDisplayName() == playerCreeper){
 	    	event.setQuitMessage(ChatColor.AQUA + playerCreeper + ChatColor.GREEN + "(Creeper)" + ChatColor.AQUA + " Has Left... Choosing New Creeper...");
 	    	playerCreeper = null;
 	    	if (playercounter >= minForGame ){
 		    	if (playerCreeper == null){
 					chooseNewCreeper(null);
 				}
 		    }
 	    }
     }
 		
 	public void chooseNewCreeper(Player ExceptionPlayer) {
 		if(playercounter <= 0){return;}
 		_Logger.info("Choosing creeper");
 		creeperLegend = 0;
 		Random random = new Random();
 		int ID = random.nextInt(playercounter);
 		String ExceptionPlayerName;
 		if (ExceptionPlayer != null)
 		{
 			ExceptionPlayerName = ExceptionPlayer.getDisplayName();
 		}
 		else{
 			ExceptionPlayerName = "";
 		}
 		if (onlinePlayerList[ID].getDisplayName() != ExceptionPlayerName)
 		{
 			playerCreeper = onlinePlayerList[ID].getDisplayName();	
 			Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "The new creeper has been chosen!");
 			for(int i=0; i < playercounter;i++){
 				if (onlinePlayerList[i].getDisplayName() == playerCreeper){
 					onlinePlayerList[i].sendMessage(ChatColor.RED + "You Are The New Creeper, Press 'Shift' To Explode... Try To Kill As Many Players As Possible Whilst Staying Undetected.");
 					onlinePlayerList[i].getInventory().addItem(new ItemStack(Material.SULPHUR, new Random().nextInt(10)));
 				}
 				else{
 					onlinePlayerList[i].getInventory().addItem(new ItemStack(Material.IRON_SWORD));
 				}
 			}
 		}
 		else{chooseNewCreeper(ExceptionPlayer);}
 	}
 
 	@EventHandler
 	public void onDeath(PlayerDeathEvent event){
 	    Player player = (Player) event.getEntity(); 
 		World world = player.getWorld();
 		if (isGame == true){
 		    if (player.getDisplayName() == playerCreeper){
 		    	checkCreeperLegend(player);
 		    	playerCreeper = null;
		    	chooseNewCreeper(player);
 		    	if (new Random().nextInt(10) > 5){ player.getKiller().getInventory().addItem(new ItemStack(264)); }
 		    }
 		    else{
 			    creeperLegend++;
 		    }
 		    if (playerCreeper != null) { world.strikeLightning(player.getLocation()); }
 		}
 	}
 
 	private void checkCreeperLegend(Player player) {
 		if (creeperLegend > Highscore){
 			HSPlayer = player.getDisplayName();
 			Highscore = creeperLegend;
 			Bukkit.getServer().broadcastMessage( ChatColor.AQUA + HSPlayer + ChatColor.GREEN + "(Creeper)" + ChatColor.AQUA + " has set a new high score of " + ChatColor.RED + Highscore);
 			
 		}
 		else{
 			Bukkit.getServer().broadcastMessage( ChatColor.AQUA + player.getDisplayName() + ChatColor.GREEN + "(Creeper)" + ChatColor.AQUA + " gained a score of  " + creeperLegend);
 		}
 	}
 	
 	@EventHandler
 	public void onRespawn(PlayerRespawnEvent event){ 
 		Player player = event.getPlayer();
 		World world = player.getWorld();
 		doPlayerRespawn(event, world, player);
 	}
 
 	@EventHandler
 	public void MOTDping(final ServerListPingEvent event){
 		if (allowPublic == false){
 		    	event.setMotd(ChatColor.GREEN + "Lone Creeper:" + ChatColor.AQUA + " Server Closed"); 
 		}else if (isGame == true){
 		    if (playerCreeper != null){
 			event.setMotd(ChatColor.GREEN + "Lone Creeper:" + ChatColor.AQUA + " Creeper Is Active");
 			}
 			else{
 				if (playercounter < minForGame ){
 					event.setMotd(ChatColor.GREEN + "Lone Creeper:" + ChatColor.AQUA + " Waiting for players.");
 			    }
 				else{
 					event.setMotd(ChatColor.GREEN + "Lone Creeper:" + ChatColor.AQUA + " Choosing New Creeper");
 				}
 			} 
 		}else{
 			event.setMotd(ChatColor.GREEN + "Lone Creeper:" + ChatColor.AQUA + " Game Disabled By OP");
 		}
     }
 }
