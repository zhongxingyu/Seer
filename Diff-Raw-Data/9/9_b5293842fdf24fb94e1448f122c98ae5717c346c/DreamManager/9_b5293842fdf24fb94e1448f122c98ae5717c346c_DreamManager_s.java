 package de.blablubbabc.dreamworld.managers;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Random;
 import java.util.Map.Entry;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.WeatherType;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import de.blablubbabc.dreamworld.DreamworldPlugin;
 import de.blablubbabc.dreamworld.messages.Message;
 import de.blablubbabc.dreamworld.messages.Messages;
 import de.blablubbabc.dreamworld.objects.DreamData;
 import de.blablubbabc.dreamworld.objects.PlayerDataStore;
 
 public class DreamManager {
 	private DreamworldPlugin plugin;
 	private Map<String, DreamData> dreamingPlayers = new HashMap<String, DreamData>();
 	private Random random = new Random();
 	
 	public DreamManager(DreamworldPlugin plugin) {
 		this.plugin = plugin;
 		
 		// dream duration check:
 		plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
 			
 			@Override
 			public void run() {
 				Iterator<Entry<String, DreamData>> iterator = dreamingPlayers.entrySet().iterator();
 				while (iterator.hasNext()) {
 					Entry<String, DreamData> entry = iterator.next();
 					String playerName = entry.getKey();
 					DreamData dreamData = entry.getValue();
 					
 					Player player = Bukkit.getServer().getPlayerExact(playerName);
 					if (player != null && player.isOnline()) {
 						
 						int remainingSeconds = dreamData.getRemainingSeconds();
 						if (remainingSeconds <= 1) {
 							leaveDream(player, dreamData);
 							iterator.remove();
 						} else {
 							dreamData.decreaseRemainingSeconds();
 						}
 					}
 				}
 			}
 		}, 20L, 20L);
 	}
 	
 	private void leaveDream(Player player) {
 		leaveDream(player, dreamingPlayers.remove(player.getName()));
 	}
 	
 	private void leaveDream(Player player, DreamData dreamData) {
 		if (dreamData != null) {
 			// restore player:
 			dreamData.getPlayerData().restorePlayerFromDream(player);
 			
 			// inform player:
 			player.sendMessage(Messages.getMessage(Message.DREAM_LEAVE));
 		}
 	}
 	
 	public boolean isDreaming(String playerName) {
 		return dreamingPlayers.containsKey(playerName);
 	}
 	
 	public void onPlayerDeath(Player player) {
 		leaveDream(player);
 	}
 	
 	public void spawnPlayer(Player player) {
 		player.leaveVehicle(); // just in case..
 		player.teleport(getSpawnLocation());
 	}
 	
 	private Location getSpawnLocation() {
 		ConfigManager config = plugin.getConfigManager();
 		Location dreamSpawn = null;
 		if (!config.spawnRandomly || config.dreamSpawns.isEmpty() || (dreamSpawn = config.dreamSpawns.get(random.nextInt(config.dreamSpawns.size())).getBukkitLocation()) == null) {
 			World dreamWorld = plugin.getDreamWorld();
 			dreamSpawn = dreamWorld.getSpawnLocation();
 		}
 		return dreamSpawn;
 	}
 	
 	private void startDreaming(Player player) {
 		String playerName = player.getName();
 		if (isDreaming(playerName)) return;
 		ConfigManager config = plugin.getConfigManager();
 		
 		// duration:
 		int dreamDuration = config.minDurationSeconds + random.nextInt(config.maxDurationSeconds - config.minDurationSeconds);
 		if (dreamDuration <= config.ignoreIfRemainingTimeIsLowerThan) return;
 		
 		// store current player state:
 		PlayerDataStore playerData = new PlayerDataStore(player, getSpawnLocation(), config.applyInitialGamemode ? config.initialGamemode : null);
 		
 		// create dream data:
 		DreamData dreamData = new DreamData(playerData, System.currentTimeMillis(), dreamDuration);
 		
 		// do initialization:
 		
 		// spawning and gamemode is already handled by the player's data store
 		//if (config.applyInitialGamemode) player.setGameMode(config.initialGamemode);
 		if (config.applyInitialHealth) player.setHealth(config.initialHealth);
 		if (config.applyInitialHunger) player.setFoodLevel(config.initialHunger);
 		if (config.applyInitialPotionEffects && !config.initialPotionEffects.isEmpty()) player.addPotionEffects(config.initialPotionEffects);
 		
 		// fake time:
 		if (config.fakeTimeEnabled) {
			int time = config.fakeTime - config.fakeTimeRandomBounds + random.nextInt(config.fakeTime + config.fakeTimeRandomBounds);
 			player.setPlayerTime(time, !config.fakeTimeFixed);
 		}
 		
 		// fake rain:
 		if (config.fakeRain) {
 			player.setPlayerWeather(WeatherType.DOWNFALL);
 		}
 		
 		
 		onDreamingStart(player, dreamData);
 	}
 	
 	private void onDreamingStart(Player player, DreamData dreamData) {
 		// mark as dreaming:
 		dreamingPlayers.put(player.getName(), dreamData);
 		
 		// inform player:
 		player.sendMessage(Messages.getMessage(Message.DREAM_ENTER));
 	}
 	
 	public void continueDreaming(Player player, boolean save) {
 		String playerName = player.getName();
 		DreamData dreamData = plugin.getDreamDataStore().getStoredDreamData(playerName);
 		if (dreamData != null) {
 			plugin.getDreamDataStore().removeDreamData(playerName, save);
 			ConfigManager config = plugin.getConfigManager();
 			
 			if (dreamData.getRemainingSeconds() >= config.ignoreIfRemainingTimeIsLowerThan) {
 				// stored dream:
 				PlayerDataStore storedDream = dreamData.getPlayerData();
 				
 				// store current player and start dreaming:
 				Location dreamLocation = storedDream.getLocation().getBukkitLocation();
 				if (dreamLocation == null) {
 					plugin.getLogger().warning("Continue dream failed: Couldn't find old dream location (world: '" + storedDream.getLocation().getWorldName() + "') for player '" + playerName + "'. Respawning him now at another dream spawn.");
 					dreamLocation = getSpawnLocation();
 				}
 				
 				PlayerDataStore newStore = new PlayerDataStore(player, dreamLocation, storedDream.getGameMode());
 				
 				// restore dream state:
 				storedDream.restorePlayerToDream(player);
 				// fake rain:
 				if (config.fakeRain) {
 					player.setPlayerWeather(WeatherType.DOWNFALL);
 				}
 				
 				// save the old player state:
 				dreamData.setPlayerData(newStore);
 				
 				onDreamingStart(player, dreamData);
 			}
 		}
 	}
 	
 	public void onBedLeave(final Player player) {
 		String playerName = player.getName();
 		if (!isDreaming(playerName) && player.hasPermission(plugin.DREAM_PERMISSION)) {
 			int chance = plugin.getConfigManager().dreamChance;
 			int real = random.nextInt(100);
 			if (real < chance) {
 				plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
 					
 					@Override
 					public void run() {
 						startDreaming(player);
 					}
 				}, 1L);
 			}
 		}
 	}
 	
 	public void onDisconnect(Player player) {
 		handleDreamBreak(player, dreamingPlayers.remove( player.getName()), true);
 	}
 	
 	private void handleDreamBreak(Player player, DreamData dreamData, boolean save) {
 		if (dreamData != null) {
 			PlayerDataStore oldPlayerData = dreamData.getPlayerData();
 			
 			if (dreamData.getRemainingSeconds() >= plugin.getConfigManager().ignoreIfRemainingTimeIsLowerThan) {
 				// store current dreaming player:
 				PlayerDataStore newStore = new PlayerDataStore(player);
 				
 				// save new dream state for later continuation:
 				dreamData.setPlayerData(newStore);
 				plugin.getDreamDataStore().storeDreamState(player.getName(), dreamData, save);
 			}
 			
 			// restore to old player data:
 			oldPlayerData.restorePlayerFromDream(player);
 		}
 	}
 	
 	public void onDisable() {
 		for (Entry<String, DreamData> entry : dreamingPlayers.entrySet()) {
 			String playerName = entry.getKey();
 			DreamData dreamData = entry.getValue();
 			
 			handleDreamBreak(Bukkit.getPlayerExact(playerName), dreamData, false);
 		}
 		dreamingPlayers.clear();
 		plugin.getDreamDataStore().saveCurrentDreamData();
 	}
 }
