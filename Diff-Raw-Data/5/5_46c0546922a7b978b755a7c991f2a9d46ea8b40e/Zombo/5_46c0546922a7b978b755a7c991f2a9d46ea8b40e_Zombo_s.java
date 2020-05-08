 package me.ayan4m1.plugins.zombo;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.yaml.snakeyaml.TypeDescription;
 import org.yaml.snakeyaml.Yaml;
 import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
 
 public class Zombo extends JavaPlugin implements Listener {
 	private final String	  configFile   = "config.yml";
 	private final String	  dataFile	   = "data.yml";
 	private final String	  dropsFile	   = "drops.yml";
 	private DataStore         dataStore    = new DataStore();
 	private Integer           wave         = 1;
 	private Integer			  waveXpBonus  = 5000;
 
 	public void onEnable() {
 		getServer().getPluginManager().registerEvents(this, this);
 
 		try {
 			File configFile = new File(getDataFolder(), this.configFile);
 			File dataFile = new File(getDataFolder(), this.dataFile);
 			File dropsFile = new File(getDataFolder(), this.dropsFile);
 
 			this.getLogger().info("Loading config from " + this.configFile);
 			getConfig().load(configFile);
 
 			//This allows snakeyaml to deserialize correctly
 			CustomClassLoaderConstructor dropLoader = new CustomClassLoaderConstructor(ZomboDropInfo.class.getClassLoader());
 			CustomClassLoaderConstructor dataLoader = new CustomClassLoaderConstructor(ZomboPlayerInfo.class.getClassLoader());
 			TypeDescription typeDesc = new TypeDescription(ZomboPlayerInfo.class);
 			typeDesc.putMapPropertyType("kills", EntityType.class, Integer.class);
 			dataLoader.addTypeDescription(typeDesc);
 			
 			if (dropsFile.length() > 0) {
 				this.getLogger().info("Loading drops file from " + this.dropsFile);
 
 				HashMap<EntityType, ArrayList<ZomboDropInfo>> drops = (HashMap<EntityType, ArrayList<ZomboDropInfo>>)new Yaml(dropLoader).load(new FileReader(dropsFile));
 				dataStore.setDrops(drops);
 
 				this.getLogger().info("Loaded drops for " + drops.size() + " entity types");
 			}
 			if (dataFile.length() > 0) {
 				this.getLogger().info("Loading data file from " + this.dataFile);
 
 				HashMap<String, ZomboPlayerInfo> players = (HashMap<String, ZomboPlayerInfo>)new Yaml(dataLoader).load(new FileReader(dataFile));
 				dataStore.setPlayers(players);
 
 				//Set online status for currently connected players
 				Integer onlineCount = 0;
 				for (String playerName : dataStore.getPlayers().keySet()) {
 					Player player = getServer().getPlayer(playerName);
 					ZomboPlayerInfo playerInfo = dataStore.getPlayerByName(playerName);
 
 					if (player == null || !player.getWorld().getName().equals(this.getWorldName())) {
 						playerInfo.setOnline(false);
 					} else {
 						playerInfo.setOnline(true);
 						onlineCount++;
 						getServer().broadcastMessage(player.getName() + " joined the fight!");
 					}
 
 					dataStore.putPlayer(playerName, playerInfo);
 				}
				this.getLogger().info("Loaded data for " + dataStore.getPlayers().size()  + " players, " + onlineCount + " are online now");
 			}
 		} catch (FileNotFoundException e) {
 			this.getLogger().warning("File was not found");
 		} catch (IOException e) {
 			this.getLogger().warning("Error reading file - " + e.getMessage());
 		} catch (InvalidConfigurationException e) {
 			this.getLogger().warning(this.configFile + " is invalid - " + e.getMessage());
 		}
 	}
 
 	public void onDisable() {
 		try {
 			CustomClassLoaderConstructor dataLoader = new CustomClassLoaderConstructor(ZomboPlayerInfo.class.getClassLoader());
 			TypeDescription typeDesc = new TypeDescription(ZomboPlayerInfo.class);
 			typeDesc.putMapPropertyType("kills", EntityType.class, Integer.class);
 			dataLoader.addTypeDescription(typeDesc);
 
 			this.getLogger().info("Saving data to " + this.dataFile);
 			FileWriter writer = new FileWriter(new File(getDataFolder(), this.dataFile));
 
 			//Serialize the player list
 			writer.write(new Yaml(dataLoader).dump(dataStore.getPlayers()));
 			writer.close();
 
 			CustomClassLoaderConstructor dropLoader = new CustomClassLoaderConstructor(ZomboDropInfo.class.getClassLoader());
 
 			this.getLogger().info("Saving drops to " + this.dropsFile);
 			writer = new FileWriter(new File(getDataFolder(), this.dropsFile));
 
 			//Serialize the drop list
 			writer.write(new Yaml(dropLoader).dump(dataStore.getDrops()));
 			writer.close();
 		} catch (IOException e) {
 			this.getLogger().warning("IO error - " + e.getMessage());
 		}
 	}
 
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		Player player = event.getPlayer();
 		if (!player.getWorld().getName().equals(this.getWorldName())) {
 			return;
 		}
 
 		//Add player or mark existing player as online
 		ZomboPlayerInfo playerInfo;
 		if (!dataStore.containsPlayer(player.getName())) {
 			playerInfo = new ZomboPlayerInfo();
 			playerInfo.setOnline(true);
 			//First time player, give them a starter kit
 			InventoryManager.starterKit(player);
 		} else {
 			playerInfo = dataStore.getPlayerByName(player.getName());
 			playerInfo.setOnline(true);
 		}
 
 		//Update player info
 		dataStore.putPlayer(player.getName(), playerInfo);
 		getServer().broadcastMessage(player.getName() + " joined the fight!");
 
 		getLogger().info(dataStore.getOnlinePlayers() + " online players");
 		if (dataStore.getOnlinePlayers() == 1) {
 			startWaves();
 			advanceWave();
 		}
 	}
 
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		Player player = event.getPlayer();
 		if (!player.getWorld().getName().equals(this.getWorldName())) {
 			return;
 		}
 
 		//Mark player as offline
 		if (dataStore.containsPlayer(player.getName())) {
 			ZomboPlayerInfo playerInfo = dataStore.getPlayerByName(player.getName());
 			playerInfo.setOnline(false);
 			dataStore.putPlayer(player.getName(), playerInfo);
 			getServer().broadcastMessage(player.getName() + " left the fight!");
 		}
 	}
 
 	@EventHandler
 	public Result onPlayerChangedWorldEvent(PlayerChangedWorldEvent event) {
 		Player player = event.getPlayer();
 
 		//Create new info object or fetch existing object
 		ZomboPlayerInfo playerInfo;
 		if (!dataStore.containsPlayer(player.getName())) {
 			playerInfo = new ZomboPlayerInfo();
 		} else {
 			playerInfo = dataStore.getPlayerByName(player.getName());
 		}
 
 		//Update online status based on event state
 		if (player.getWorld().getName().equals(this.getWorldName())) {
 			playerInfo.setOnline(true);
 			getServer().broadcastMessage(player.getName() + " joined the fight!");
 		} else if (event.getFrom().getName().equals(this.getWorldName())) {
 			playerInfo.setOnline(false);
 			getServer().broadcastMessage(player.getName() + " left the fight!");
 		}
 
 		//Update online status
 		dataStore.putPlayer(player.getName(), playerInfo);
 		return Result.ALLOW;
 	}
 
 	@EventHandler
 	public void onPlayerDeath(PlayerDeathEvent event) {
 		//Ensure the death occurred in the correct world
 		if (!event.getEntity().getWorld().getName().equals(this.getWorldName())) {
 			return;
 		}
 
 		//Temporarily store inventory for respawn event
 		dataStore.setTempInventoryForPlayer(event.getEntity().getName(), event.getEntity().getInventory().getContents());
 
 		//Prevent items from dropping
 		event.getDrops().clear();
 
 		//Inform players of the death
 		messagePlayers(event.getEntity().getName() + " has died!");
 	}
 
 	@EventHandler
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		//Ensure the respawn occurred in the correct world
 		if (!event.getPlayer().getWorld().getName().equals(this.getWorldName())) {
 			return;
 		}
 
 		if (!dataStore.containsPlayer(event.getPlayer().getName())) {
 			//TODO: Either this is an error or return or something
 			return;
 		}
 
 		ItemStack[] inventory = dataStore.getTempInventoryForPlayer(event.getPlayer().getName());
 		if (inventory != null) {
 			event.getPlayer().getInventory().setContents(inventory);
 		}
 	}
 	
 	@EventHandler
 	public void onCreatureSpawn(CreatureSpawnEvent event) {
 		//Ensure the entity is in the correct world
 		if (event.getEntity().getWorld().getName().equals(this.getWorldName())) {
 			return;
 		}
 
 		//Suppress most enemy mob spawning
 		if (event.getSpawnReason().equals(SpawnReason.NATURAL)
 			|| event.getSpawnReason().equals(SpawnReason.CHUNK_GEN)) {
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler
 	public void onEntityDeath(EntityDeathEvent event) {
 		//Ensure the death occurred in the correct world
 		if (!event.getEntity().getWorld().getName().equals(this.getWorldName())) {
 			return;
 		}
 
 		//Ensure the entity was a monster
 		if (!(event.getEntity() instanceof Monster)) {
 			return;
 		}
 
 		//Ensure the mob had a killer
 		Monster mob = (Monster)event.getEntity();
 		if (mob.getKiller() == null) {
 			return;
 		}
 
 		//Ensure the killer and entity are both in the data store
 		Player player = mob.getKiller();
 		if (!dataStore.containsPlayer(player.getName()) || !dataStore.containsMob(mob.getEntityId())) {
 			return;
 		}
 
 		//Fetch information from data store
 		ZomboPlayerInfo playerInfo = dataStore.getPlayerByName(player.getName());
 		ZomboMobInfo entityInfo = dataStore.getMobById(mob.getEntityId());
 
 		//Update player information
 		Integer oldLevel = playerInfo.getLevel();
 		playerInfo.addKill(entityInfo.getType());
 		playerInfo.addXp(entityInfo.getXp());
 		dataStore.putPlayer(player.getName(), playerInfo);
 
 		//Send message(s) to players
 		mob.getKiller().sendMessage("Killed a " + mob.getType().getName() + " [+" + entityInfo.getXp() + " XP]");
 		if (!oldLevel.equals(playerInfo.getLevel())) {
 			messagePlayers(mob.getKiller().getName() + " is now level " + playerInfo.getLevel());
 		}
 
 		//Disable vanilla drops
 		event.setDroppedExp(0);
 		event.getDrops().clear();
 
 		//Drop crafting items
 		ArrayList<ZomboDropInfo> mobDrops = dataStore.getDropsByType(mob.getType());
 		Random rand = new Random();
 		if (mobDrops != null && !mobDrops.isEmpty()) {
 			for (ZomboDropInfo dropInfo : mobDrops) {
 				if (dropInfo.canDrop(rand)) {
 					event.getDrops().add(new ItemStack(dropInfo.getType(), dropInfo.getAmount()));
 				}
 			}
 		}
 
 		//Stop tracking mob
 		dataStore.removeMob(mob.getEntityId());
 
 		if (dataStore.getMobs().isEmpty()) {
 			if (wave == 5) {
 				//Give XP bonus to online players
 				for (String playerName : dataStore.getPlayers().keySet()) {
 					Player msgPlayer = getServer().getPlayerExact(playerName);
 					ZomboPlayerInfo msgPlayerInfo = dataStore.getPlayerByName(playerName);
 
 					if (msgPlayer == null) {
 						continue;
 					}
 
 					msgPlayerInfo.addXp(waveXpBonus);
 					msgPlayer.sendMessage("Wave complete bonus! [+" +  + waveXpBonus + " XP]");
 				}
 			}
 			if (this.isAutoAdvance()) {
 				advanceWave();
 			} else {
 				//Tell players to ready up
 				messagePlayers("Wave complete! /zready to continue.");
 			}
 		} else if (dataStore.getMobs().size() <= 3) {
 			//Tell players that the wave is almost over
 			messagePlayers(dataStore.getMobs().size() + " mobs remaining");
 		}
 	}
 
 	private void messagePlayers(String message) {
 		for (Player msgPlayer : getServer().getWorld(this.getWorldName()).getPlayers()) {
 			msgPlayer.sendMessage(message);
 		}
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
 		if (!(sender instanceof Player)) {
 			sender.sendMessage("Must be a player to use this command.");
 			return false;
 		}
 
 		Player player = (Player)sender;
 		if (!this.getWorldName().equals(player.getWorld().getName())) {
 			return false;
 		}
 
 		if (cmd.getName().equalsIgnoreCase("zinfo")) {
 			ZomboPlayerInfo playerInfo = dataStore.getPlayerByName(player.getName());
 			player.sendMessage("Level " + playerInfo.getLevel() + " (" + playerInfo.getXp() + " XP)");
 			player.sendMessage("Next level at " + (playerInfo.getLevel() * 5000) + " XP");
 			player.sendMessage("Kills");
 			for(EntityType type : EntityType.values()) {
 				//Ensure that entity type descends from Monster
 				if (type.getEntityClass() == null || !Monster.class.isAssignableFrom(type.getEntityClass())) {
 					continue;
 				}
 
 				//Ensure that count is greater than zero
 				if (playerInfo.getKillsForType(type) == 0) {
 					continue;
 				}
 
 				player.sendMessage("  " + type.getName() + ": " + playerInfo.getKillsForType(type));
 			}
 		} else if (cmd.getName().equalsIgnoreCase("zwave")) {
 			player.sendMessage("Wave " + wave);
 		} else if (cmd.getName().equalsIgnoreCase("zresetinv")) {
 			InventoryManager.starterKit(player);
 			player.sendMessage("Your inventory has been reset.");
 		}
 
 		return false;
 	}
 
 	private void startWaves() {
 		wave = 0;
 	}
 
 	private void advanceWave() {
 		World world = getServer().getWorld(this.getWorldName());
 		Location loc = world.getSpawnLocation();
 		Random rand = new Random();
 
 		if (wave == 6) {
 			wave = 1;
 		} else {
 			wave++;
 		}
 
 		for (int i = 0; i < (wave * 2); i++) {
 			dataStore.spawnMob(loc.add(rand.nextInt(5), 0, rand.nextInt(5)), new ZomboMobInfo(EntityType.ZOMBIE, (500 * wave)));
 		}
 
 		for (int i = 0; i < wave; i++) {
 			dataStore.spawnMob(loc.add(rand.nextInt(5), 0, rand.nextInt(5)), new ZomboMobInfo(EntityType.SPIDER, (750 * wave)));
 		}
 
 		if (wave >= 3) {
 			dataStore.spawnMob(loc.add(rand.nextInt(5), 0, rand.nextInt(5)), new ZomboMobInfo(EntityType.PIG_ZOMBIE, (1500 * wave)));			
 		}
 
 		if (wave >= 4) {
 			dataStore.spawnMob(loc.add(rand.nextInt(5), 0, rand.nextInt(5)), new ZomboMobInfo(EntityType.BLAZE, (2000 * wave)));
 			dataStore.spawnMob(loc.add(rand.nextInt(5), 0, rand.nextInt(5)), new ZomboMobInfo(EntityType.BLAZE, (2000 * wave)));
 		}
 
 		if (wave == 5) {	
 			dataStore.spawnMob(loc.add(rand.nextInt(5), 0, rand.nextInt(5)), new ZomboMobInfo(EntityType.ENDER_DRAGON, (5000 * wave)));			
 		}
 
 		if (wave == 5) {
 			messagePlayers("Final Wave Spawned!");
 		} else {
 			messagePlayers("Wave " + wave + " Spawned!");
 		}
 	}
 
 	private String getWorldName() {
 		return getConfig().getString("world");
 	}
 
 	private boolean isAutoAdvance() {
 		return getConfig().getBoolean("autoadvance");
 	}
 }
 
