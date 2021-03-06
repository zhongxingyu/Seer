 package net.dmulloy2.ultimatearena.arenas;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.dmulloy2.ultimatearena.UltimateArena;
 import net.dmulloy2.ultimatearena.arenas.objects.*;
 import net.dmulloy2.ultimatearena.events.*;
 import net.dmulloy2.ultimatearena.permissions.PermissionType;
 import net.dmulloy2.ultimatearena.util.*;
 
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.potion.Potion;
 import org.bukkit.potion.PotionEffect;
 
 import com.earth2me.essentials.IEssentials;
 import com.earth2me.essentials.User;
 
 /** Base Data Container for an Arena **/
 public abstract class Arena 
 {
 	protected List<ArenaPlayer> arenaPlayers = new ArrayList<ArenaPlayer>();
 	protected List<ArenaSpawn> spawns = new ArrayList<ArenaSpawn>();
 	protected List<ArenaFlag> flags = new ArrayList<ArenaFlag>();
 
 	private int startingAmount = 0;
 	private int broadcastTimer = 45;
 	private int winningTeam = 999;
 	private int announced = 0;
 	private int maxDeaths = 1;
 	private int maxwave = 15;
 	private int wave = 0;
 
 	private int amtPlayersStartingInArena;
 	private int amtPlayersInArena;
 	private int maxgametime;
 	private int starttimer;
 	private int gametimer;
 	private int team1size;
 	private int team2size;
 	
 	private boolean allowTeamKilling = false;
 	private boolean pauseStartTimer = false;
 	private boolean forceStop = false;
 	private boolean stopped = false;
 	private boolean start = false;
 
 	private boolean updatedTeams;
 	private boolean disabled;
 
 	private World world;
 
 	protected FieldType type;
 	private String name;
 
 	protected final UltimateArena plugin;
 	private ArenaConfig config;
 	private ArenaZone az;
 	
 	public Arena(ArenaZone az) 
 	{
 		this.az = az;
 		this.plugin = az.getPlugin();
 		this.name = az.getArenaName();
 		this.world = az.getWorld();
 		this.az.setTimesPlayed(this.az.getTimesPlayed() + 1);
 		this.plugin.arenasPlayed++;
 		
 		if (this.getMaxDeaths() < 1) 
 		{
 			this.setMaxDeaths(1);
 		}
 	}
 	
 	/** Reload the Config **/
 	public void reloadConfig() 
 	{
 		if (config != null) 
 		{
 			this.setMaxgametime(config.getGameTime());
 			this.setGametimer(config.getGameTime());
 			this.setStarttimer(config.getLobbyTime());
 			this.setMaxDeaths(config.getMaxDeaths());
 			this.setAllowTeamKilling(config.isAllowTeamKilling());
 			this.setMaxwave(config.getMaxWave());
 			
 			if (this.getMaxDeaths() < 1) 
 			{
 				this.setMaxDeaths(1);
 			}
 		}
 	}
 	
 	/** Add a Player to the Arena **/
 	public void addPlayer(Player player)
 	{
 		player.sendMessage(plugin.getPrefix() + "Joining the arena... Please wait.");
 		
 		ArenaPlayer pl = new ArenaPlayer(player, this, plugin);
 		arenaPlayers.add(pl);
 		
 		// Update Teams
 		pl.setTeam(getTeam());
 		setUpdatedTeams(true);
 		
 		// Save and clear Inventory
 		if (plugin.getConfig().getBoolean("saveInventories", true))
 		{
 			plugin.debug("Saving Inventory for Player: {0}", player.getName());
 			
 			pl.saveInventory();
 			pl.clearInventory();
 		}
 		
 		// Teleport the player to the lobby spawn
 		spawn(player.getName(), false);
 		
 		// Make sure the player is in survival
 		player.setGameMode(GameMode.SURVIVAL);
 		
 		// Heal up the Player
 		player.setFoodLevel(20);
 		player.setFireTicks(0);
 		player.setHealth(20);
 		
 		// Don't allow flight
 		player.setAllowFlight(false);
 		player.setFlySpeed(0.1F);
 		player.setFlying(false);
 		
 		
 		// If essentials is found, remove god mode.
 		PluginManager pm = plugin.getServer().getPluginManager();
 		if (pm.isPluginEnabled("Essentials"))
 		{
 			Plugin essPlugin = pm.getPlugin("Essentials");
 			IEssentials ess = (IEssentials) essPlugin;
 			User user = ess.getUser(player);
 			
 			// Disable GodMode in the arena
 			if (user.isGodModeEnabled())
 				user.setGodModeEnabled(false);
 		}
 		
 		// Clear potion effects
 		pl.clearPotionEffects();
 		
 		// Call ArenaJoinEvent
 		UltimateArenaJoinEvent joinEvent = new UltimateArenaJoinEvent(pl, this);
 		plugin.getServer().getPluginManager().callEvent(joinEvent);
 		
 		pl.sendMessage("&6You have joined the arena!");
 	}
 	
 	public int getTeam() 
 	{
 		return 1;
 	}
 	
 	/** Announce the Arena **/
 	public void announce() 
 	{
 		for (Player player : plugin.getServer().getOnlinePlayers())
 		{
 			if (! plugin.isInArena(player))
 			{
 				if (plugin.getPermissionHandler().hasPermission(player, PermissionType.JOIN.permission))
 				{
 					if (announced == 0) 
 					{
 						player.sendMessage(plugin.getPrefix() + FormatUtil.format("&b{0} &6arena has been created!", getArenaZone().getArenaType()));
 					}
 					else
 					{
 						player.sendMessage(plugin.getPrefix() + FormatUtil.format("&6Hurry up and join the &b{0} &6arena!", getArenaZone().getArenaType()));
 					}
 					
 					player.sendMessage(plugin.getPrefix() + FormatUtil.format("&6Type &b/ua join {0} &6to join!", getArenaZone().getArenaName()));
 				}
 			}
 		}
 
 		announced++;
 	}
 	
 	/** Returns the team a new player should be on **/
 	public int getBalancedTeam()
 	{
 		int amt1 = 0;
 		int amt2 = 0;
 		
 		for (ArenaPlayer ap : arenaPlayers)
 		{
 			if (ap != null && !ap.isOut())
 			{
 				if (ap.getTeam() == 1)
 				{
 					amt1++;
 				}
 				else
 				{
 					amt2++;
 				}
 			}
 		}
 		
 		if (amt1 > amt2) 
 		{
 			return 2;
 		}
 		
 		return 1;
 	}
 	
 	/** Check if a team is empty **/
 	public boolean simpleTeamCheck(boolean stopifEmpty) 
 	{
 		if (getTeam1size() == 0 || team2size == 0) 
 		{
 			if (stopifEmpty)
 			{
 				stop();
 			}
 			
 			if (this.getStartingAmount() > 1)
 			{
 				return false;
 			}
 			
 			return true;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * @param p - Player instance
 	 * @return The player's ArenaPlayer instance
 	 */
 	public ArenaPlayer getArenaPlayer(Player p) 
 	{
 		if (p != null) 
 		{
 			for (ArenaPlayer ap : arenaPlayers)
 			{
 				if (!ap.isOut())
 				{
 					Player player = Util.matchPlayer(ap.getPlayer().getName());
 					if (player != null && player.isOnline())
 					{
 						if (player.getName().equals(p.getName()))
 							return ap;
 					}
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	/** Spawns all players in the Arena **/
 	public void spawnAll() 
 	{
 		plugin.outConsole("Spawning players for Arena: {0}", getArenaZone().getArenaName());
 		for (ArenaPlayer ap : arenaPlayers)
 		{
 			if (ap != null && !ap.isOut())
 				spawn(ap.getPlayer().getName(), false);
 		}
 	}
 	
 	/**
 	 * @param ap - ArenaPlayer instance
 	 * @return the ArenaPlayer's spawn
 	 */
 	public Location getSpawn(ArenaPlayer ap) 
 	{
 		Location loc = null;
 		try 
 		{
 			if (getStarttimer() > 0)
 			{
 				loc = getArenaZone().getLobbyREDspawn().clone();
 				if (ap.getTeam() == 2)
 					loc = getArenaZone().getLobbyBLUspawn().clone();
 			}
 			else
 			{
 				loc = getArenaZone().getTeam1spawn().clone();
 				if (ap.getTeam() == 2) 
 					loc = getArenaZone().getTeam2spawn().clone();
 			}
 		}
 		catch (Exception e)
 		{
 			loc = getSpawns().get(Util.random(getSpawns().size())).getLocation().clone().add(0, 2, 0);
 		}
 		
 		if (loc != null)
 		{
 			loc = loc.clone().add(0.25, 1, 0.25);
 		}	
 		
 		return loc;
 	}
 	
 	/** Spawns a player and gives them their class items **/
 	public void spawn(String name, boolean alreadyspawned)
 	{
 		plugin.debug("Attempting to spawn player: {0}. Already Spawned: {1}", name, alreadyspawned);
 		
 		if (!isStopped())
 		{
 			Player p = Util.matchPlayer(name);
 			if (p != null) 
 			{
 				for (ArenaPlayer ap : arenaPlayers)
 				{
 					if (ap.getPlayer().getName().equals(name))
 					{
 						if (ap != null && !ap.isOut())
 						{
 							if (ap.getDeaths() < getMaxDeaths()) 
 							{
 								Location loc = getSpawn(ap);
 								if (loc != null) 
 								{
 									plugin.debug("Spawning player: {0}", name);
 									
 									Location nloc = new Location(loc.getWorld(), loc.getX() + 0.25, loc.getY() + 1.0, loc.getZ() + 0.25);
 									teleport(p, nloc);
 									
 									// Call spawn event
 									ArenaSpawn aSpawn = new ArenaSpawn(nloc.getWorld(), nloc.getBlockX(), nloc.getBlockY(), nloc.getBlockZ());
 									UltimateArenaSpawnEvent spawnEvent = new UltimateArenaSpawnEvent(ap, this, aSpawn);
 									plugin.getServer().getPluginManager().callEvent(spawnEvent);
 								}
 								
 								ap.spawn();
 								
 								if (! alreadyspawned)
 								{
 									onSpawn(ap);
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void onSpawn(ArenaPlayer apl) {}
 	
 	/** Player Death **/
 	public void onPlayerDeath(ArenaPlayer pl) 
 	{
 		pl.setAmtkicked(0);
 		
 		// Call ArenaDeathEvent
 		UltimateArenaDeathEvent deathEvent = new UltimateArenaDeathEvent(pl, this);
 		plugin.getServer().getPluginManager().callEvent(deathEvent);
 	}
 	
 	/** Default Rewarding System **/
 	public void reward(ArenaPlayer ap, Player player, boolean half)
 	{
 		plugin.debug("Rewarding player: {0}. Half: {1}", player.getName(), half);
 		
 		if (config != null) 
 		{
 			config.giveRewards(player, half);
 		}
 		else
 		{
 			InventoryHelper.addItem(player, new ItemStack(Material.GOLD_INGOT));
 		}
 	}
 	
 	/** Rewards the Winning Team **/
 	public void rewardTeam(int team, String string, boolean half)
 	{
 		for (ArenaPlayer ap : arenaPlayers)
 		{
 			if (ap != null && ap.canReward())
 			{
 				if (ap.getTeam() == team || team == -1)
 				{
 					Player player = ap.getPlayer();
 					if (player != null)
 					{
 						reward(ap, ap.getPlayer(), half);
 						player.sendMessage(plugin.getPrefix() + FormatUtil.format(string));
 					}
 				}
 			}
 		}
 	}
 	
 	/** Sets the Winning Team **/
 	public void setWinningTeam(int team)
 	{
 		for (ArenaPlayer ap : arenaPlayers)
 		{
 			if (ap != null && !ap.isOut())
 			{
 				ap.setCanReward(false);
 				if (ap.getTeam() == team || team == -1)
 				{
 					ap.setCanReward(true);
 				}
 
 			}
 		}
 		this.winningTeam = team;
 	}
 	
 	/** Check if any player has enough points to win **/
 	public void checkPlayerPoints(int max)
 	{
 		for (ArenaPlayer ap : arenaPlayers)
 		{
 			if (ap != null && !ap.isOut())
 			{
 				if (ap.getPoints() >= max)
 				{
 					reward(ap, Util.matchPlayer(ap.getUsername()), false);
 					tellPlayers("&7Player &6{0} &7has won!", ap.getUsername());
 					stop();
 				}
 			}
 		}
 	}
 	
 	/** Stops the Arena if empty **/
 	public boolean checkEmpty() 
 	{
 		boolean ret = isEmpty();
 		if (ret) stop();
 		
 		return ret;
 	}
 	
 	/** Checks if the arena is empty **/
 	public boolean isEmpty()
 	{
 		return (getStarttimer() <= 0 && getAmtPlayersInArena() <= 1);
 	}
 	
 	/** Tells all the players in the arena a message **/
 	public void tellPlayers(String string, Object...objects) 
 	{
 		for (ArenaPlayer ap : arenaPlayers)
 		{
 			if (ap != null && !ap.isOut()) 
 			{
 				Player player = Util.matchPlayer(ap.getPlayer().getName());
 				if (player != null && player.isOnline())
 				{
 					player.sendMessage(plugin.getPrefix() + FormatUtil.format(string, objects));
 				}
 			}
 		}
 	}
 	
 	/** Kills all players in the arena near a point **/
 	public void killAllNear(Location loc, int rad)
 	{
 		for (ArenaPlayer ap : arenaPlayers)
 		{
 			if (ap != null && !ap.isOut()) 
 			{
 				Player player = Util.matchPlayer(ap.getPlayer().getName());
 				if (player != null && player.isOnline())
 				{
 					Location ploc = player.getLocation();
 					if (Util.pointDistance(loc, ploc) < rad)
 						player.setHealth(0.0D);
 				}
 			}
 		}
 	}
 	
 	/** Spawns a player to a random spawnpoint **/
 	public void spawnRandom(String name)
 	{
 		if (getStarttimer() <= 0) 
 		{
 			Player p = Util.matchPlayer(name);
 			if (p != null) 
 			{
 				ArenaPlayer ap = plugin.getArenaPlayer(p);
 				if (ap != null && !ap.isOut())
 				{
 					if (getSpawns().size() > 0) 
 						teleport(p, (getSpawns().get(Util.random(getSpawns().size())).getLocation().clone()).add(0, 2, 0));
 				}
 			}
 		}
 	}
 	
 	/** Gives the player an item **/
 	public void giveItem(Player pl, int id, byte dat, int amt, String message)
 	{
 		pl.sendMessage(plugin.getPrefix() + message);
 		
 		ItemStack item = new ItemStack(id, amt);
 		if (dat != 0)
 		{
 			MaterialData data = new MaterialData(id);
 			data.setData(dat);
 			item.setData(data);
 		}
 
 		InventoryHelper.addItem(pl, item);
 	}
 	
 	public void givePotion(Player pl, String s, int amt, int level, boolean splash, String message)
 	{
 		pl.sendMessage(plugin.getPrefix() + message);
 		
 		org.bukkit.potion.PotionType type = PotionType.toType(s);
 		if (type != null)
 		{
 			Potion potion = new Potion(1);
 			potion.setType(type);
 			potion.setLevel(level);
 			potion.setSplash(splash);
 			
 			InventoryHelper.addItem(pl, potion.toItemStack(amt));
 		}
 	}
 	
 	/** Basic Killstreak System **/
 	public void doKillStreak(ArenaPlayer ap) 
 	{
 		plugin.debug("Doing KillStreak for player: {0}", ap.getUsername());
 		
 		Player pl = Util.matchPlayer(ap.getPlayer().getName());
 		if (pl != null)
 		{
 			/**Hunger Arena check**/
 			if (plugin.getArena(pl).getType().equals("Hunger"))
 				return;
 				
 			if (ap.getKillstreak() == 2)
 				givePotion(pl, "strength", 1, 1, false, "2 kills! Unlocked strength potion!");
 				
 			if (ap.getKillstreak() == 4)
 			{
 				givePotion(pl, "heal", 1, 1, false, "4 kills! Unlocked health potion!");
 				giveItem(pl, Material.GRILLED_PORK.getId(), (byte)0, 2, "4 kills! Unlocked Food!");
 			}
 			if (ap.getKillstreak() == 5) 
 			{
 				if (!(this.getArenaZone().getArenaType().equalsIgnoreCase("cq"))) 
 				{
 					pl.sendMessage(plugin.getPrefix() + "5 kills! Unlocked Zombies!");
 					for (int i = 0; i < 4; i++)
 						pl.getLocation().getWorld().spawnEntity(pl.getLocation(), EntityType.ZOMBIE);
 				}
 			}
 			if (ap.getKillstreak() == 8) 
 			{
 				pl.sendMessage(plugin.getPrefix() + "8 kills! Unlocked attackdogs!");
 				for (int i = 0; i < 2; i++)
 				{
 					Wolf wolf = (Wolf) pl.getLocation().getWorld().spawnEntity(pl.getLocation(), EntityType.WOLF);
 					wolf.setOwner(pl);
 				}
 			}
 			if (ap.getKillstreak() == 12)
 			{
 				givePotion(pl, "regen", 1, 1, false, "12 kills! Unlocked regen potion!");
 				giveItem(pl, Material.GRILLED_PORK.getId(), (byte)0, 2, "12 kills! Unlocked Food!");
 			}
 		}
 	}
 	
 	/** Disables an Arena **/
 	public void onDisable() 
 	{
 		tellPlayers("&cThis arena has been disabled!");
 		
 		setGametimer(-1);
 		setDisabled(true);
 		stop();
 	}
 	
 	/** Removes a Player from the Arena **/
 	public void removePlayer(ArenaPlayer ap) 
 	{
 		ap.setOut(true);
 		setUpdatedTeams(true);
 	}
 	
 	/** Ends the Arena **/
 	public void stop()
 	{
 		setStopped(true);
 		onStop();
 		
 		plugin.outConsole("Stopping arena: {0}!", name);
 
 		for (ArenaPlayer ap : arenaPlayers)
 		{
 			if (ap != null)
 			{
 				Player player = Util.matchPlayer(ap.getPlayer().getName());
 				if (player != null)
 				{
 					if (plugin.isInArena(player)) 
 					{
 						if (getGametimer() <= getMaxgametime())
 						{
 							ap.sendMessage("&9Game inturrupted/ended!");
 						}
 						else
 						{
 							ap.sendMessage("&9Game Over!");
 						}
 						
 						endPlayer(ap, false);
 					}
 				}
 				
 				ap.setOut(true);
 			}
 		}
 		
 		plugin.activeArena.remove(this);
 		
 		plugin.broadcast("&6Arena &b{0} &6has concluded!", name);
 	}
 	
 	public void onStop() {}
 
 	/** Removes all armour and inventory **/
 	public void normalize(Player p)
 	{
 		plugin.normalize(p);
 	}
 	
 	/** Safely teleports a player **/
 	public void teleport(Player p, Location add) 
 	{
 		p.teleport(add.clone().add(0.5, 0, 0.5));
 	}
 	
 	public void check() {}
 
 	/** End an ArenaPlayer **/
 	public void endPlayer(ArenaPlayer ap, boolean dead) 
 	{
 		plugin.debug("Ending Player: {0} Dead: {1}", ap.getUsername(), dead);
 		
 		Player player = ap.getPlayer();
 		if (player != null) 
 		{
 			teleport(player, ap.getSpawnBack().clone().add(0, 2.0, 0));
 			normalize(player);
 			returnXP(player);
 			ap.returnInventory();
 			
 			ap.sendMessage("&9Thanks for playing!");
 						
 			plugin.removePotions(player);
 		}
 		
 		// Call Arena leave event
 		UltimateArenaLeaveEvent leaveEvent = new UltimateArenaLeaveEvent(ap, this);
 		plugin.getServer().getPluginManager().callEvent(leaveEvent);
 
 		ap.setOut(true);
 		setUpdatedTeams(true);
 		
 		if (dead) 
 		{
 			ap.sendMessage("&9You have exceeded the death limit!");
 		}
 	}
 
	public void onStart()
 	{
 		setAmtPlayersStartingInArena(arenaPlayers.size());
 	}
 	
 	public void onOutOfTime() {}
 	
 	public void onPreOutOfTime() {}
 	
 	/** Check Timers **/
 	public void checkTimers() 
 	{
 		if (isStopped())
 		{
 			arenaPlayers.clear();
 			return;
 		}
 		
 		if (config == null)
 		{
 			config = plugin.getConfig(type.name);
 			reloadConfig();
 		}
 		
 		if (!isPauseStartTimer())
 		{
 			setStarttimer(getStarttimer() - 1);
 			broadcastTimer--;
 		}
 		
 		if (getStarttimer() <= 0)
 		{
 			start();
 			setGametimer(getGametimer() - 1);
 		}
 		else
 		{
 			if (broadcastTimer < 0)
 			{
 				broadcastTimer = 45;
 				announce();
 			}
 		}
 		
 		// End the game
 		if (getGametimer() <= 0) 
 		{
 			onPreOutOfTime();
 			plugin.forceStop(getArenaZone().getArenaName());
 			onOutOfTime();
 		}
 	}
 	
 	/** Starts the Arena **/
 	public void start()
 	{
 		if (start == false) 
 		{
 			plugin.outConsole("Starting arena: {0} Players: {1}", getName(), getAmtPlayersInArena());
 			
 			this.start = true;
 			this.setStartingAmount(this.getAmtPlayersInArena());
 			this.setAmtPlayersStartingInArena(this.getStartingAmount());
 			this.onStart();
 			
 			spawnAll();
 			setGametimer(getMaxgametime());
 			setStarttimer(-1);
 		}
 	}
 	
 	/** Arena Updater **/
 	public void update()
 	{
 		setTeam1size(0);
 		team2size = 0;
 		checkTimers();
 		
 		// Get how many people are in the arena
 		for (ArenaPlayer ap : arenaPlayers)
 		{
 			if (ap != null && !ap.isOut())
 			{
 				Player player = Util.matchPlayer(ap.getPlayer().getName());
 				if (player != null)
 				{
 					if (ap.getTeam() == 1)
 						setTeam1size(getTeam1size() + 1);
 					else
 						team2size++;
 				}
 			}
 		}
 		
 		check();
 
 		setAmtPlayersInArena(0);
 
 		for (ArenaPlayer ap : arenaPlayers)
 		{
 			if (ap != null && !ap.isOut())
 			{
 				Player player = ap.getPlayer();
 				if (player != null)
 				{
 					setAmtPlayersInArena(getAmtPlayersInArena() + 1);
 							
 					// Check players in the Arena
 					if (getStarttimer() > 0) 
 					{
 						player.setFireTicks(0);
 						player.setFoodLevel(20);
 						ap.decideHat(player);
 					}
 					
 					ap.setHealtimer(ap.getHealtimer() - 1);
 						
 					ArenaClass ac = ap.getArenaClass();
 					if (ac != null)
 					{
 						if (ac.getName().equalsIgnoreCase("healer") && ap.getHealtimer() <= 0) 
 						{
 							if (ap.getPlayer().getHealth() + 1 <= 20)
 							{
 								if (ap.getPlayer().getHealth() < 0) 
 									ap.getPlayer().setHealth(1);
 								ap.getPlayer().setHealth(ap.getPlayer().getHealth()+1);
 								ap.setHealtimer(2);
 							}
 						}
 								
 						// Class based potion effects
 						if (ac.hasPotionEffects())
 						{
 							if (ac.getPotionEffects().size() > 0)
 							{
 								for (PotionEffect effect : ac.getPotionEffects())
 								{
 									if (!ap.getPlayer().hasPotionEffect(effect.getType()))
 										player.addPotionEffect(effect);
 								}
 							}
 						}
 					}
 							
 					// Make sure they are still in the Arena
 					if (!plugin.isInArena(player.getLocation()))
 					{
 						plugin.outConsole("Player {0} got out of the arena! Putting him back in!", ap.getUsername());
 
 						spawn(ap.getPlayer().getName(), false);
 						ap.setAmtkicked(ap.getAmtkicked() + 1);
 					}
 					
 					// Timer Stuff
 					if (!isPauseStartTimer()) 
 					{
 						if (getStarttimer() == 120) 
 						{
 							ap.sendMessage("&6120 &7seconds until start!");
 						}
 						if (getStarttimer() == 60)
 						{
 							ap.sendMessage("&660 &7seconds until start!");
 						}
 						if (getStarttimer() == 45)
 						{
 							ap.sendMessage("&645 &7seconds until start!");
 						}
 						if (getStarttimer() == 30) 
 						{
 							ap.sendMessage("&630 &7seconds until start!");
 						}
 						if (getStarttimer() == 15)
 						{
 							ap.sendMessage("&615 &7seconds until start!");
 						}
 						if (getStarttimer() > 0 && getStarttimer() < 11) 
 						{
 							ap.sendMessage("&6{0} &7second(s) until start!", getStarttimer());
 						}
 					}
 							
 					if (getGametimer() > 0 && getGametimer() < 21)
 					{
 						ap.sendMessage("&6{0} &7second(s) until end!", getGametimer());
 					}
 					if (getGametimer() == 60 && getMaxgametime() > 60)
 					{
 						ap.sendMessage("&6{0} &7minute(s) until end!", (getGametimer() - 60) / 60);
 					}
 					if (getGametimer() == getMaxgametime()/2) 
 					{
 						ap.sendMessage("&6{0} &7second(s) until end!", getMaxgametime() / 2);
 					}
 					
 					// XP Bar
 					decideXPBar(ap);
 							
 					// End dead players
 					if (!isStopped()) 
 					{
 						if (ap.getDeaths() >= getMaxDeaths()) 
 						{
 							if (player != null) 
 							{
 								if (player.getHealth() > 0) 
 								{
 									endPlayer(ap, true);
 									removePlayer(ap);
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		// Stop the arena if there are no players
 		if (this.getAmtPlayersInArena() == 0)
 			plugin.forceStop(getArenaZone().getArenaName());
 	}
 	
 	/** Timer XP bar **/
 	public void decideXPBar(ArenaPlayer ap)
 	{
 		if (ap != null && ! ap.isOut())
 		{
 			if (plugin.getConfig().getBoolean("timerXPBar", false))
 			{
 				if (isInGame())
 				{
 					ap.getPlayer().setLevel(getGametimer());
 				}
 					
 				if (isInLobby())
 				{
 					ap.getPlayer().setLevel(getStarttimer());
 				}
 			}
 		}
 	}
 	
 	/** Return a player's xp after leaving an arena **/
 	public void returnXP(Player player)
 	{
 		plugin.debug("Returning XP for player: {0}", player.getName());
 		
 		ArenaPlayer ap = plugin.getArenaPlayer(player);
 		
 		// Clear XP
 		player.setExp((float) 0);
 		player.setLevel(0);
 		
 		// Give Base XP
 		player.setLevel(ap.getBaselevel());
 		
 		// TODO: Reward with extra XP based on ingame XP?
 	}
 
 	public void forceStart(Player player)
 	{
 		if (isInGame())
 		{
 			player.sendMessage(plugin.getPrefix() + FormatUtil.format("&cThis arena is already in progress!"));
 			return;
 		}
 		
 		plugin.outConsole("Forcefully starting arena: {0}!", name);
 		
 		setStarttimer(0);
 		
 		start();
 		setGametimer(getGametimer() - 1);
 		
 		player.sendMessage(plugin.getPrefix() + FormatUtil.format("&6You have forcefully started &b{0}&6!", name));
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public int getStartingAmount() 
 	{
 		return startingAmount;
 	}
 
 	public void setStartingAmount(int startingAmount) 
 	{
 		this.startingAmount = startingAmount;
 	}
 
 	public boolean isForceStop()
 	{
 		return forceStop;
 	}
 
 	public void setForceStop(boolean forceStop) 
 	{
 		this.forceStop = forceStop;
 	}
 
 	public List<ArenaPlayer> getArenaPlayers() 
 	{
 		return arenaPlayers;
 	}
 	
 	public int getStarttimer() 
 	{
 		return starttimer;
 	}
 
 	public void setStarttimer(int starttimer) 
 	{
 		this.starttimer = starttimer;
 	}
 
 	public ArenaZone getArenaZone() 
 	{
 		return az;
 	}
 
 	public int getAmtPlayersInArena()
 	{
 		return amtPlayersInArena;
 	}
 
 	public void setAmtPlayersInArena(int amtPlayersInArena) 
 	{
 		this.amtPlayersInArena = amtPlayersInArena;
 	}
 
 	public boolean isDisabled()
 	{
 		return disabled;
 	}
 
 	public void setDisabled(boolean disabled)
 	{
 		this.disabled = disabled;
 	}
 
 	public FieldType getType() 
 	{
 		return type;
 	}
 	
 	public int getGametimer()
 	{
 		return gametimer;
 	}
 
 	public void setGametimer(int gametimer) 
 	{
 		this.gametimer = gametimer;
 	}
 
 	public int getMaxgametime() 
 	{
 		return maxgametime;
 	}
 
 	public void setMaxgametime(int maxgametime) 
 	{
 		this.maxgametime = maxgametime;
 	}
 
 	public int getMaxDeaths()
 	{
 		return maxDeaths;
 	}
 
 	public void setMaxDeaths(int maxDeaths) 
 	{
 		this.maxDeaths = maxDeaths;
 	}
 	
 	public boolean isUpdatedTeams() 
 	{
 		return updatedTeams;
 	}
 
 	public void setUpdatedTeams(boolean updatedTeams)
 	{
 		this.updatedTeams = updatedTeams;
 	}
 
 	public List<ArenaFlag> getFlags() 
 	{
 		return flags;
 	}
 
 	public void setFlags(List<ArenaFlag> flags)
 	{
 		this.flags = flags;
 	}
 
 	public int getAmtPlayersStartingInArena()
 	{
 		return amtPlayersStartingInArena;
 	}
 
 	public void setAmtPlayersStartingInArena(int amtPlayersStartingInArena) 
 	{
 		this.amtPlayersStartingInArena = amtPlayersStartingInArena;
 	}
 
 	public int getWinningTeam() 
 	{
 		return winningTeam;
 	}
 
 	public boolean isStopped()
 	{
 		return stopped;
 	}
 
 	public void setStopped(boolean stopped)
 	{
 		this.stopped = stopped;
 	}
 
 	public boolean isAllowTeamKilling()
 	{
 		return allowTeamKilling;
 	}
 
 	public void setAllowTeamKilling(boolean allowTeamKilling) 
 	{
 		this.allowTeamKilling = allowTeamKilling;
 	}
 
 	public List<ArenaSpawn> getSpawns() 
 	{
 		return spawns;
 	}
 
 	public void setSpawns(List<ArenaSpawn> spawns)
 	{
 		this.spawns = spawns;
 	}
 
 	public World getWorld() 
 	{
 		return world;
 	}
 	
 	public int getWave() 
 	{
 		return wave;
 	}
 
 	public void setWave(int wave)
 	{
 		this.wave = wave;
 	}
 
 	public int getMaxwave() 
 	{
 		return maxwave;
 	}
 
 	public void setMaxwave(int maxwave)
 	{
 		this.maxwave = maxwave;
 	}
 
 	public int getTeam1size() 
 	{
 		return team1size;
 	}
 
 	public void setTeam1size(int team1size) 
 	{
 		this.team1size = team1size;
 	}
 
 	public boolean isPauseStartTimer()
 	{
 		return pauseStartTimer;
 	}
 
 	public void setPauseStartTimer(boolean pauseStartTimer) 
 	{
 		this.pauseStartTimer = pauseStartTimer;
 	}
 	
 	public void setType(FieldType type)
 	{
 		this.type = type;
 	}
 	
 	public boolean isInGame()
 	{
 		return (getStarttimer() < 1 && getGametimer() > 0);
 	}
 	
 	public boolean isInLobby()
 	{
 		return (getStarttimer() > 1);
 	}
 }
