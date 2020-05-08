 //Wolves VS Pigs Main Class
 //The main class controls the bulk of the game, mostly with listeners.
 //By FourOhFour
 //http://fourohfour.github.io
 
 package io.github.fourohfour.wolvesvspigs;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.Set;
 
 import io.github.fourohfour.countdown.Fight;
 import io.github.fourohfour.countdown.PreGame;
 import io.github.fourohfour.countdown.Prepare;
 import io.github.fourohfour.devcountdown.Countdown;
 import io.github.fourohfour.wolvesvspigs.WvpCommandExecutor;
 import io.github.fourohfour.wolvesvspigs.WvpaCommandExecutor;
 import io.github.fourohfour.wolvesvspigs.WvpdCommandExecutor;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.InventoryDragEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Score;
 import org.bukkit.scoreboard.Scoreboard;
 import org.bukkit.scoreboard.ScoreboardManager;
 import org.bukkit.scoreboard.Team;
 
 public final class WolvesVSPigs extends JavaPlugin implements Listener{
 	public Map<Player, Integer> scores = new HashMap<Player, Integer>();
 	public Set<OfflinePlayer> betas = new HashSet<OfflinePlayer>();
 	public Kit kit = new Kit(this);
 
 	//On Enable class
 	@Override
 	public void onEnable(){
 
 		//Reset the global variables.
 		Resources.setGlobalsToDefaults();
 
 		//Set Command Executors
 
 		getCommand("wvpa").setExecutor(new WvpaCommandExecutor(this));
 		getCommand("wvp").setExecutor(new WvpCommandExecutor(this));
 		getCommand("wvpd").setExecutor(new WvpdCommandExecutor(this));
 		getServer().getPluginManager().registerEvents(this, this);
 
 		//Create an instance of PreferenceManager with the instance as argument.
 		new PreferenceManager(this);
 
 		//Iterate through players and reset their preference metadata values.
 		for (int playeri = 0; playeri < Bukkit.getOnlinePlayers().length; playeri++){
 			Player target = Bukkit.getOnlinePlayers()[playeri];
 			target.setMetadata("IngameTP", new FixedMetadataValue(this, true));
 			target.setMetadata("CanBreakGlass", new FixedMetadataValue(this, false));
 			target.setMetadata("CanPlaceGlass", new FixedMetadataValue(this, false));
 			target.setMetadata("CanBreakBlocksPreGame", new FixedMetadataValue(this, false));
 
 			scores.put(target, 0);
 		}
 
 		File beta = new File("./WVP/beta.txt");
 		if (!beta.exists()){
 			Bukkit.getLogger().info("Beta File not found... creating now.");
 			beta.mkdir();
 		}
 		Scanner s = null;
 		try {
 			s = new Scanner(beta);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		if (!(s == null)) {
 			while (s.hasNext()) {
 				betas.add(Bukkit.getOfflinePlayer(s.next()));
 			}
 		}
 
 	}
 	@Override
 	public void onDisable(){
 		//Nothing here as of yet
 	}
 
 	@EventHandler
 	public void onPlayerHurt(EntityDamageByEntityEvent e){
 		//Get the spectator scoreboard
 		ScoreboardManager m = Bukkit.getScoreboardManager();
 		Scoreboard mainboard = m.getMainScoreboard();
 		Team Spectator = mainboard.getTeam("Spectators");
 		if (!(Spectator == null)){
 			if (e.getEntity() instanceof Player && e.getDamager() instanceof Player){
 				Player p = (Player) e.getEntity();
 				Player d = (Player) e.getDamager();
 				if (Spectator.hasPlayer(p) || Spectator.hasPlayer(d)){
 					e.setCancelled(true);
 				}
 			}
 		}
 	}
 
 	@EventHandler
 	public void onPlayerRespawn(final PlayerRespawnEvent event){
 		if (Globals.globalvars.get("gamestage").equals("pregame") ||Globals.globalvars.get("gamestage").equals("prepare")){
 			if(Globals.lobby.contains(event.getPlayer())){
 				if(!(Bukkit.getScoreboardManager().getMainScoreboard().getTeam("Spectators").hasPlayer(event.getPlayer()))){
 					kit.Pig(event.getPlayer());
 					Bukkit.getScheduler().runTask(this, new Runnable(){
 
 						@Override
 						public void run() {
 							kit.tele(event.getPlayer());
 
 						}
 
 					});
 					Globals.lobby.remove(event.getPlayer());
 				}
 				else{
 					kit.Spectator(event.getPlayer());
 				}
 			}
 		}
 		else if (Globals.globalvars.get("gamestage").equals("fight")){
 			Team Wolf = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("Wolves");
 			Wolf.addPlayer(event.getPlayer());
 
 			kit.Wolf(event.getPlayer());
 			Bukkit.getScheduler().runTask(this, new Runnable(){
 
 				@Override
 				public void run() {
 					kit.tele(event.getPlayer());
 
 				}
 
 			});
 		}
 	}
 
 	//On Player Death listener
 	@EventHandler
 	public void onPlayerDeath(PlayerDeathEvent event){
 		if (!(Bukkit.getScoreboardManager().getMainScoreboard().getObjective("left") == null)){
 
 			//What to do if the gamestage is pregame
 			if(Globals.globalvars.get("gamestage") == "pregame"){
 				Globals.lobby.add(event.getEntity());
 			}
 
 			if(Globals.globalvars.get("gamestage") == "prepare"){
 				Globals.lobby.add(event.getEntity());
 			}
 
 			if(Globals.globalvars.get("gamestage") == "fight"){
 				Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
 				Team Pig = main.getTeam("Pigs");
 				Team Wolf = main.getTeam("Wolves");
 				if (!(event.getEntity().getKiller() == null)){
 					if (!(Pig == null) && !(Wolf == null)){
 						if (Wolf.hasPlayer(event.getEntity())){
 							scores.put(event.getEntity().getKiller(), scores.get(event.getEntity().getKiller()) + 20);
 							event.getEntity().getKiller().sendMessage("2" + "You got 20 points for killing a Wolf!" + "r");
 						}
 						else if (Pig.hasPlayer(event.getEntity())){
 
 							scores.put(event.getEntity().getKiller(), scores.get(event.getEntity().getKiller()) + 15);
 							event.getEntity().getKiller().sendMessage("2" + "You got 10 points for killing a Pig. Now enjoy their juicy bacon!" + "r");
 							ItemStack bacon = new ItemStack(Material.GRILLED_PORK);
 							ItemMeta meta = bacon.getItemMeta();
 							meta.setDisplayName(event.getEntity().getName());
 							bacon.setItemMeta(meta);
 							event.getEntity().getKiller().getInventory().addItem(bacon);
 
 						}
 					}
 				}
 				if (Pig.hasPlayer(event.getEntity())){
 					Pig.removePlayer(event.getEntity());
 					Wolf.addPlayer(event.getEntity());
 
 					kit.Wolf(event.getEntity());
 					
 					for (OfflinePlayer player : Pig.getPlayers()){
 						((Player) player).sendMessage("2" + "Another pig has fallen! But you get 5 points for surviving longer!" + "r");
 						scores.put((Player) player, scores.get(player) + 10);
 
 					}
 				}
 				if(Pig.getSize() == 0){
 					Globals.globalvars.put("gamestage", "wolfwin");
 					GameStateChangeEvent e = new GameStateChangeEvent();
 					Bukkit.getServer().getPluginManager().callEvent(e);
 				}
			}
 				
 				updateScores();
 
 				//Stop Sticky items being dropped
 
 				int droplen = event.getDrops().size();
 				//Iterate through items to be dropped
 				for (int dropi = 0; dropi < droplen; dropi++){
 					//Check if the item has lore
 					if(event.getDrops().get(dropi).getItemMeta().hasLore()){
 						//If it does and the lore is sticky (with a lime green format code) then...
 						if(event.getDrops().get(dropi).getItemMeta().getLore().get(0).equals("a" + "Sticky")){
 							//Set that value to null
 							event.getDrops().set(dropi, null);
 						}
 					}
 				}
 
 				//Will keep going round removing null values until there are no more null values
 				while(event.getDrops().remove(null)){
 				}
 		}
 
 	}
 	@EventHandler
 	public void onPlayerPickupItemEvent(PlayerPickupItemEvent event){
 		ScoreboardManager m = Bukkit.getScoreboardManager();
 		Scoreboard mainboard = m.getMainScoreboard();
 		Team Spectator = mainboard.getTeam("Spectators");
 		if (!(Spectator == null)){
 			if (Spectator.hasPlayer(event.getPlayer())){
 				event.setCancelled(true);
 			}
 		}
 	}
 
 	@EventHandler
 	public void onInteract(PlayerInteractEvent event){
 		Action a = event.getAction();
 		Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
 		Team w = main.getTeam("Wolves");
 		if (!(w == null)){
 			if(w.hasPlayer(event.getPlayer())){
 				if (a == Action.RIGHT_CLICK_AIR){
 					if (event.getPlayer().getItemInHand().getType() == Material.COMPASS){
 						double closest = Double.MAX_VALUE;
 						Player closestp = null;
 						Team p = main.getTeam("Pigs");
 						for(Player i : Bukkit.getOnlinePlayers()){
 							if(p.hasPlayer(i)){
 								double dist = i.getLocation().distance(event.getPlayer().getLocation());
 								if (closest == Double.MAX_VALUE || dist < closest){
 									closest = dist;
 									closestp = i;
 								}
 							}
 						}
 						if (closestp == null){
 							event.getPlayer().sendMessage("2" + "Could not lock onto new target" + "r");
 						}
 						else {
 							event.getPlayer().setCompassTarget(closestp.getLocation());
 							event.getPlayer().sendMessage("2" + "Set new target to " + closestp.getDisplayName() + "r");
 						}
 					}
 				}
 			}
 		}
 	}
 	@EventHandler
 	public void onPlayerDropItem(PlayerDropItemEvent event) {
 		if (event.getItemDrop().getItemStack().getItemMeta().hasLore()){
 			if (event.getItemDrop().getItemStack().getItemMeta().getLore().get(0).equals("a" + "Sticky")){
 				event.setCancelled(true);
 			}
 		}
 	}
 
 
 	@EventHandler
 	public void onInventoryDragEvent(InventoryDragEvent event){
 		ItemStack item = event.getOldCursor();
 		if(this.isArmour(item)){
 			event.setCancelled(true);
 		}
 	}
 	@EventHandler
 	public void onBlockPlace(final BlockPlaceEvent placeb){
 		Material blockt = placeb.getBlockPlaced().getType();
 		if (placeb.getPlayer().hasMetadata("CanPlaceGlass")){
 			if (blockt == Material.GLASS && (placeb.getPlayer().getMetadata("CanPlaceGlass").get(0).asBoolean() == false)){
 				Bukkit.getScheduler().runTask(this, new Runnable() {
 					@Override
 					public void run(){
 						Block nblock = placeb.getBlock();
 						nblock.setType(Material.SAND);
 						Player playert = placeb.getPlayer();
 						playert.sendMessage("2" + "Glass is not allowed. Transmuting..." + "r");
 					}
 				});
 			}
 		}
 	}
 
 	private boolean isArmour(ItemStack i){
 		Material t = i.getType();
 		if(t == Material.LEATHER_BOOTS || t == Material.LEATHER_CHESTPLATE || t == Material.LEATHER_HELMET || t == Material.LEATHER_LEGGINGS){
 			return true;
 		}
 		else if(t == Material.CHAINMAIL_BOOTS || t == Material.CHAINMAIL_LEGGINGS || t == Material.CHAINMAIL_CHESTPLATE || t == Material.CHAINMAIL_HELMET){
 			return true;
 		}
 		return false;
 	}
 	@EventHandler
 	public void onBlockBreak(final BlockBreakEvent breakb){
 		if((Globals.globalvars.get("gamestage").equals("pregame")) && (breakb.getPlayer().getMetadata("CanBreakBlocksPreGame").get(0).asBoolean() == false)){
 			breakb.getPlayer().sendMessage("2"+ "You may not break blocks until the game has begun" + "r");
 			breakb.setCancelled(true);
 		}
 		Material blockt = breakb.getBlock().getType();
 		if (blockt == Material.GLASS && (breakb.getPlayer().getMetadata("CanBreakGlass").get(0).asBoolean() == false)){
 			breakb.setCancelled(true);
 			breakb.getPlayer().sendMessage("2" + "You may not escape. Turn and Fight." + "r");
 		}
 		Team Spectator = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("Spectators");
 		if(!(Spectator == null)){
 			if(Spectator.hasPlayer(breakb.getPlayer())){
 				breakb.setCancelled(true);
 			}
 		}
 	}
 	@EventHandler
 	public void onJoinEvent(PlayerJoinEvent join){
 		scores.put(join.getPlayer(), 0);
 		ScoreboardManager m = Bukkit.getScoreboardManager();
 		Scoreboard mainboard = m.getMainScoreboard();
 		Team Spectator = mainboard.getTeam("Spectators");
 		if (!(Spectator == null)){
 			Spectator.addPlayer(join.getPlayer());
 			kit.Spectator(join.getPlayer());
 		}
 
 		Player joined = join.getPlayer();
 		joined.setMetadata("IngameTP", new FixedMetadataValue(this, true));
 		joined.setMetadata("CanBreakGlass", new FixedMetadataValue(this, false));
 		joined.setMetadata("CanPlaceGlass", new FixedMetadataValue(this, false));
 		joined.setMetadata("CanBreakBlocksPreGame", new FixedMetadataValue(this, false));
 	}
 
 	@EventHandler
 	public void onLeaveEvent(PlayerQuitEvent leave){
 		Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
 		Globals.lobby.remove(leave.getPlayer());
 		Set<Team> tset = main.getTeams();
 		List<Team> tlist = new ArrayList<Team>();
 		tlist.addAll(tset);
 		for (Team i : tset){
 			if (i.removePlayer(leave.getPlayer())){
 				updateScores();
 
 
 				if (i.getName() == ("Pigs")){
 					updateScores();
 				}
 				else if (i.getName() == ("Wolves")){
 					updateScores();
 				};
 			}
 
 		}
 		if (Globals.globalvars.get("gamestage") == "prepare" || Globals.globalvars.get("gamestage") == "pregame" ){
 			if (Bukkit.getScoreboardManager().getMainScoreboard().getTeam("Pigs").getSize() == 0){
 				Bukkit.broadcastMessage("2"+ "Not enough players. Game stopping..." + "r");
 				this.stopcount();
 			}
 
 		}
 	}
 
 	@EventHandler
 	public void onGameStateChangeEvent(GameStateChangeEvent event){
 		String state = event.getState();
 		if (state == "pregame"){
 
 			PluginDescriptionFile pyml = this.getDescription();
 			String pver = pyml.getVersion();
 			Bukkit.broadcastMessage("2"+ "Wolves Vs Pigs" + "r");
 			Bukkit.broadcastMessage("2"+ "Version "+ pver + "r");
 			Bukkit.broadcastMessage("2"+ "Plugin by ObsidianFire99 AKA fourohfour" + "r");
 			Bukkit.broadcastMessage("2"+ "/wvp help - Command and Game Help." + "r");
 
 			ScoreboardManager manager = Bukkit.getScoreboardManager();
 			Player[] onp = Bukkit.getOnlinePlayers();
 			int np = onp.length;
 			Scoreboard players = manager.getMainScoreboard();
 			Objective o = players.registerNewObjective("left", "dummy");
 			o.setDisplayName("Players left");
 			o.setDisplaySlot(DisplaySlot.SIDEBAR);
 			Score t = o.getScore(Bukkit.getOfflinePlayer("Time Left:"));
 			updateScores();
 			t.setScore(0);
 
 			Team pigs = players.registerNewTeam("Pigs");
 			Team spectators = players.registerNewTeam("Spectators");
 			spectators.setPrefix("1");
 			spectators.setSuffix("r");
 			pigs.setAllowFriendlyFire(false);
 			pigs.setPrefix("d");
 			pigs.setSuffix("r");
 			for(int index=0; index < np; index++){
 				pigs.addPlayer(onp[index]);
 				kit.Pig(onp[index]);
 				updateScores();
 				if (onp[index].getMetadata("IngameTP").get(0).asBoolean() == true){
 					kit.tele(onp[index]);
 				}
 			}
 
 			PreGame cd = new PreGame();
 			cd.run(Globals.cdpresets.get("pregame")[0], Globals.cdpresets.get("pregame")[1], this);
 			Globals.countdowns.add(cd);
 
 			for (Player i : Bukkit.getOnlinePlayers()){
 				if (!(scores.containsKey(i))){
 					scores.put(i, 0);
 				}
 			}
 		}
 		if (state == "prepare"){
 			for(int i = 0; i < Globals.lobby.size(); i++){
 				kit.Pig(Globals.lobby.get(i));
 			}
 			Globals.lobby.clear();
 
 			Prepare cd = new Prepare();
 			cd.run(Globals.cdpresets.get("prepare")[0], Globals.cdpresets.get("prepare")[1], this);
 			Globals.countdowns.add(cd);
 		}
 		if (state == "fight"){
 			ScoreboardManager mainmanager = Bukkit.getScoreboardManager();
 			Scoreboard mainboard = mainmanager.getMainScoreboard();
 			Team pigs = mainboard.getTeam("Pigs");
 			for(int i = 0; i < Globals.lobby.size(); i++){
 				kit.Pig(Globals.lobby.get(i));
 				pigs.addPlayer(Globals.lobby.get(i));
 			}
 			Globals.lobby.clear();
 			if (!CreateWolves.createWolves(kit)){
 				Bukkit.broadcastMessage("2" + "Not enough Pigs!" + "r");
 				this.stopcount();
 			}
 			Team wolves = mainboard.getTeam("Wolves");
 
 			updateScores();
 			Fight cd = new Fight();
 			cd.run(Globals.cdpresets.get("fight")[0], Globals.cdpresets.get("fight")[1], this);
 			Globals.countdowns.add(cd);
 		}
 
 		if (state == "wolfwin"){
 			Scoreboard b = Bukkit.getScoreboardManager().getMainScoreboard();
 			Team wolves = b.getTeam("Wolves");
 			Team pigs = b.getTeam("Pigs");
 			Bukkit.broadcastMessage("2" + "The Wolves have Won!" + "r");
 			for (Player i : Bukkit.getOnlinePlayers()){
 				Random r = new Random();
 				if (wolves.hasPlayer(i)){
 					int bonus = (scores.get(i) / 10) * r.nextInt(5) + 1;
 					scores.put(i, scores.get(i) + bonus);
 					i.sendMessage("2" + "You get a bonus of " + String.valueOf(bonus) + "r");
 					i.sendMessage("2" + "You get a final score of " + scores.get(i) + "r");
 				}
 				else if (pigs.hasPlayer(i)){
 					i.sendMessage("2" + "You get a final score of " + scores.get(i) + "r");
 				}
 			}
 			this.stopcount();
 		}
 		else if (state == "pigwin"){
 			Scoreboard b = Bukkit.getScoreboardManager().getMainScoreboard();
 			Team wolves = b.getTeam("Wolves");
 			Team pigs = b.getTeam("Pigs");
 			Bukkit.broadcastMessage("2" + "The Pigs have Won!" + "r");
 			for (Player i : Bukkit.getOnlinePlayers()){
 				Random r = new Random();
 				if (wolves.hasPlayer(i)){
 					i.sendMessage("2" + "You get a final score of " + scores.get(i) + "r");
 				}
 				else if (pigs.hasPlayer(i)){
 					int bonus = (scores.get(i) / 10) * r.nextInt(5) + 1;
 					scores.put(i, scores.get(i) + bonus);
 					i.sendMessage("2" + "You get a bonus of " + String.valueOf(bonus) + "r");
 					i.sendMessage("2" + "You get a final score of " + scores.get(i) + "r");
 				}
 			}
 			this.stopcount();
 		}
 		if (state == "none"){
 			this.stopcount();
 		}
 	}
 
 	public Boolean stopcount(){
 		for (int idex = 0; idex < Globals.countdowns.size(); idex++){
 			Globals.countdowns.get(idex).cancel();
 		}
 		Globals.countdowns = new ArrayList<Countdown>();
 		Globals.globalvars.put("gamestage", "none");
 		ScoreboardManager mainmanager = Bukkit.getScoreboardManager();
 		Scoreboard mainboard = mainmanager.getMainScoreboard();
 		Set<Team> teams = mainboard.getTeams();
 		Team[] tarray = teams.toArray(new Team[teams.size()]);
 		for (int index = 0; index < tarray.length; index++){
 			Team targett = tarray[index];
 			targett.unregister();
 		}
 		for (int indexp = 0; indexp < Bukkit.getOnlinePlayers().length; indexp ++){
 			clearinv(Bukkit.getOnlinePlayers()[indexp]);
 			(Bukkit.getOnlinePlayers()[indexp]).getActivePotionEffects().clear();
 		}
 		scores.clear();
 		Objective o = mainboard.getObjective("left");
 		o.unregister();
 		return true;
 	}
 
 	public static void clearinv(Player targ){
 		PlayerInventory inv = targ.getInventory();
 		inv.setArmorContents(new ItemStack[4]);
 		inv.clear();
 		targ.removePotionEffect(PotionEffectType.SPEED);
 	}
 
 	@EventHandler
 	public void onLooseHunger(FoodLevelChangeEvent e){
 		e.setFoodLevel(20);
 
 	}
 
 	@EventHandler
 	public void chatFormat(AsyncPlayerChatEvent e){
 		String prefix = "";
 		if (betas.contains(Bukkit.getOfflinePlayer(e.getPlayer().getName()))){
 			prefix = "Beta Tester ";
 		}
 		Team team = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(e.getPlayer());
 		if (!(team == null)){
 			String t = team.getName();
 
 			if (t == "Pigs"){
 				e.setFormat(prefix + "D" + e.getPlayer().getName() + ": " + "r" + e.getMessage());
 			}
 			else if (t == "Wolves"){
 				e.setFormat(prefix + "7" + e.getPlayer().getName() +  ": " + "r" + e.getMessage());
 			}
 			else if (t == "Spectators"){
 				e.setFormat(prefix + "1" + e.getPlayer().getName() +  ": " + "r" + e.getMessage());
 			}
 		}
 	}
 
 	public static boolean setTimeLeft(int i){
 		Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
 		if (!(main.getObjective("left") == null)){
 			Objective o = main.getObjective("left");
 			Score t = o.getScore(Bukkit.getOfflinePlayer("Time Left:"));
 			t.setScore(i);
 			return true;
 		}
 		return false;
 	}
 	
 	public static void updateScores(){
 		Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
 		Score w = main.getObjective("left").getScore(Bukkit.getOfflinePlayer("7" + "Wolves:" + "r"));
 		Score p = main.getObjective("left").getScore(Bukkit.getOfflinePlayer("D" + "Pigs:" + "r"));
 		
 		Team wolves = main.getTeam("Wolves");
 		Team pigs = main.getTeam("Pigs");
 		
 		if (!(wolves == null)){
 		w.setScore(wolves.getSize());
 		}
		if (!(pigs == null)){
 		p.setScore(pigs.getSize());
 		}
 		
 	}
 
 }
 
