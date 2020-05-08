 package tk.nekotech.war;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import net.minecraft.server.MobEffect;
 
 import org.bukkit.ChatColor;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class War extends JavaPlugin implements Listener {
 	public ArrayList<String> online = new ArrayList<String>();
 	public ArrayList<String> pyro = new ArrayList<String>();
 	public ArrayList<String> blu = new ArrayList<String>();
 	public ArrayList<String> red = new ArrayList<String>();
 	@SuppressWarnings("rawtypes")
 	public Map scores = new HashMap();
 	public int on = 0;
 	public int max = 0;
 	public int dead = 0;
 	public boolean blocks = false;
 	
 	public void onEnable() {
 		getLogger().info("Enabled!");
 		getCommand("war").setExecutor(new Commands(this));
 		getCommand("join").setExecutor(new Commands(this));
 		getCommand("blu").setExecutor(new Commands(this));
 		getCommand("red").setExecutor(new Commands(this));
 		getCommand("ready").setExecutor(new Commands(this));
 		getCommand("spectate").setExecutor(new Commands(this));
 		getCommand("reset").setExecutor(new Commands(this));
 		if (!getConfig().getBoolean("has-started")) {
 			getLogger().warning("This is the first logged startup event (did you clear your config?");
 			getLogger().warning("Please make sure you set the coordinates for the new spawn areas.");
 		}
 		getLogger().info("Please do not modify the config as it changes often during gameplay.");
 		getServer().getPluginManager().registerEvents(this, this);
 		getServer().getPluginManager().registerEvents(new SpawnManager(this), this);
 		max = getServer().getMaxPlayers();
 		
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 			public void run() {
 				int worlds = 0;
 				for (World w : getServer().getWorlds()) {
 					w.setStorm(false);
 					w.setTime(14000);
 					worlds++;
 				}
 				getLogger().info("Changed time to night in " + worlds + " worlds!");
 			}
 		}, 40L, 4800L);
 	}
 	
 	public void onDisable() {
 		getLogger().info("Disabled!");
 	}
 	
 	public ChunkGenerator getDefaultWorldGenerator(String worldname, String uid) {
 		return new Gen(this);
 	}
 	
 	public boolean onTeam(Player player) {
 		if (blu.contains(player.getName())) {
 			return true;
 		} else if (red.contains(player.getName())) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public boolean isAllowed(CreatureType lmao) {
 		if (lmao.equals(CreatureType.SPIDER)) return true;
 		if (lmao.equals(CreatureType.CAVE_SPIDER)) return true;
 		if (lmao.equals(CreatureType.SKELETON)) return true;
 		if (lmao.equals(CreatureType.CREEPER)) return true;
 		if (lmao.equals(CreatureType.PIG_ZOMBIE)) return true;
 		if (lmao.equals(CreatureType.ZOMBIE)) return true;
 		return false;
 	}
 	
 	public CreatureType randoMob() {
 		Random rand = new Random();
		int lmao = rand.nextInt(8);
 		if (lmao == 0) return CreatureType.SPIDER;
 		if (lmao == 1) return CreatureType.CAVE_SPIDER;
 		if (lmao == 2) return CreatureType.SKELETON;
 		if (lmao == 3) return CreatureType.CREEPER;
 		if (lmao == 4) return CreatureType.PIG_ZOMBIE;
 		if (lmao == 5) return CreatureType.ZOMBIE;
 		if (lmao == 6) return CreatureType.ZOMBIE;
 		if (lmao == 7) return CreatureType.ZOMBIE;
 		if (lmao == 8) return CreatureType.CAVE_SPIDER;
 		return null;
 	}
 	
 	public void killMob(Entity entity) {
 		if (entity != null) {
 			entity.getWorld().strikeLightningEffect(entity.getLocation());
 			entity.remove();
 		}
 	}
 		
 	public void teamSpawn(Player player) {
 		if (teamName(player) == 0) {
 			double x = getConfig().getDouble("blu-spawn-x");
 			double y = getConfig().getDouble("blu-spawn-y");
 			double z = getConfig().getDouble("blu-spawn-z");
 			float yaw = getConfig().getInt("blu-spawn-yaw");
 			float pitch = getConfig().getInt("blu-spawn-pitch");
 			player.teleport(new Location(player.getWorld(), x, y, z, yaw, pitch));
 		}
 		if (teamName(player) == 1) {
 			double x = getConfig().getDouble("red-spawn-x");
 			double y = getConfig().getDouble("red-spawn-y");
 			double z = getConfig().getDouble("red-spawn-z");
 			float yaw = getConfig().getInt("red-spawn-yaw");
 			float pitch = getConfig().getInt("red-spawn-pitch");
 			player.teleport(new Location(player.getWorld(), x, y, z, yaw, pitch));
 		}
 	}
 	
 	public int teamName(Player player) {
 		if (blu.contains(player.getName())) {
 			return 0;
 		} else if (red.contains(player.getName())) {
 			return 1;
 		} else {
 			return 9;
 		}
 	}
 	
 	public void teamMessage(int team, String message) {
 		if (team == 0) {
 			for (Player p : getServer().getOnlinePlayers()) {
 				if (blu.contains(p.getName())) {
 					p.sendMessage(message);
 				}
 			}
 			getLogger().info("[BLU] " + message);
 		} else if (team == 1) {
 			for (Player p : getServer().getOnlinePlayers()) {
 				if (red.contains(p.getName())) {
 					p.sendMessage(message);
 				}
 			}
 			getLogger().info("[RED] " + message);
 		} else {
 			getLogger().severe("Unknown team caught in teamMessage! Attempted message:");
 			getLogger().severe(message);
 		}
 	}
 	
 	public void assignPlayer(Player player, int team) {
 		if (blu.contains(player.getName())) blu.remove(player.getName());
 		if (red.contains(player.getName())) red.remove(player.getName());
 		if (pyro.contains(player.getName())) pyro.remove(player.getName());
 		if (player.getGameMode() == GameMode.CREATIVE) player.setGameMode(GameMode.SURVIVAL);
 		if (team == 0) {
 			blu.add(player.getName());
 			getServer().broadcastMessage(ChatColor.BLUE + player.getName() + " was auto assigned to team blu. Blu - " + blu.size() + " Red - " + red.size());
 			double x = getConfig().getDouble("blu-spawn-x");
 			double y = getConfig().getDouble("blu-spawn-y");
 			double z = getConfig().getDouble("blu-spawn-z");
 			float yaw = getConfig().getInt("blu-spawn-yaw");
 			float pitch = getConfig().getInt("blu-spawn-pitch");
 			player.teleport(new Location(player.getWorld(), x, y, z, yaw, pitch));
 			player.setDisplayName(ChatColor.BLUE + player.getName() + ChatColor.WHITE);
 			player.setPlayerListName(ChatColor.BLUE + player.getName() + ChatColor.WHITE);
 			assignClass(player);
 		} else if (team == 1) {
 			red.add(player.getName());
 			getServer().broadcastMessage(ChatColor.RED + player.getName() + " was auto assigned to team red. Blu - " + blu.size() + " Red - " + red.size());
 			double x = getConfig().getDouble("red-spawn-x");
 			double y = getConfig().getDouble("red-spawn-y");
 			double z = getConfig().getDouble("red-spawn-z");
 			float yaw = getConfig().getInt("red-spawn-yaw");
 			float pitch = getConfig().getInt("red-spawn-pitch");
 			player.teleport(new Location(player.getWorld(), x, y, z, yaw, pitch));
 			player.setDisplayName(ChatColor.RED + player.getName() + ChatColor.WHITE);
 			player.setPlayerListName(ChatColor.RED + player.getName() + ChatColor.WHITE);
 			assignClass(player);
 		} else {
 			player.kickPlayer(ChatColor.RED + "Uncaught error, try rejoining. If problem persists contact admin.");
 			getLogger().severe("Kicked " + player.getName() + " due to error in random generator!");
 			getLogger().severe("If this problem persists contact jamietech on GitHub!");
 		}
 	}
 	
 	public void assignClass(final Player player) {
 		Random rand = new Random();
 		int lols = rand.nextInt(4);
 		player.getInventory().clear();
 		if (lols == 0) {
 			// Heavy!
 			getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
 			    public void run() {
 			    	((CraftPlayer) player).getHandle().addEffect(new MobEffect(2, 999999999, 1)); // Slowness
 					((CraftPlayer) player).getHandle().addEffect(new MobEffect(11, 999999999, 0)); // Resistance
 					player.sendMessage(ChatColor.GOLD + "You are now a heavy!");
 					player.getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD, 1));
 					player.getInventory().getItemInHand().addEnchantment(Enchantment.DAMAGE_ALL, 2);
 					armorUp(player);
 			    }
 			}, 10L);
 		}
 		if (lols == 1) {
 			// Scout!
 			getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
 			    public void run() {
 			    	((CraftPlayer) player).getHandle().addEffect(new MobEffect(1, 999999999, 0)); // Speed
 					player.sendMessage(ChatColor.GOLD + "You are now a scout!");
 					player.getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD, 1));
 					player.getInventory().getItemInHand().addEnchantment(Enchantment.KNOCKBACK, 2);
 					armorUp(player);
 			    }
 			}, 10L);
 		}
 		if (lols == 2) {
 			// Sniper!
 			getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
 			    public void run() {
 			    	((CraftPlayer) player).getHandle().addEffect(new MobEffect(11, 999999999, 2)); // Resistance
 					player.sendMessage(ChatColor.GOLD + "You are now a sniper!");
 					player.getInventory().addItem(new ItemStack(Material.BOW, 1));
 					player.getInventory().addItem(new ItemStack(Material.ARROW, 1));
 					player.getInventory().getItemInHand().addEnchantment(Enchantment.ARROW_INFINITE, 0);
 					player.getInventory().getItemInHand().addEnchantment(Enchantment.ARROW_DAMAGE, 1);
 					armorUp(player);
 			    }
 			}, 10L);
 		}
 		if (lols == 3) {
 			// Pyro!
 			getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
 			    public void run() {
 			    	((CraftPlayer) player).getHandle().addEffect(new MobEffect(12, 999999999, 3)); // Fire Resistance
 					pyro.add(player.getName());
 					player.sendMessage(ChatColor.GOLD + "You are now a pyro!");
 					player.getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD, 1));
 					// "Enchantment" granted with 2s fire bursts against all entities via SpawnManger
 					armorUp(player);
 			    }
 			}, 10L);
 		}
 		
 	}
 	
 	public void armorUp(Player player) {
 		player.getInventory().getHelmet().setType(Material.DIAMOND_HELMET);
 		player.getInventory().getChestplate().setType(Material.DIAMOND_CHESTPLATE);
 		player.getInventory().getLeggings().setType(Material.DIAMOND_LEGGINGS);
 		player.getInventory().getBoots().setType(Material.DIAMOND_BOOTS);
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void holyShitNewMap(PlayerJoinEvent e) {
 		if (!getConfig().getBoolean("has-started")) {
 			if (e.getPlayer().hasPermission("jtwar.admin")) {
 				e.setJoinMessage(ChatColor.YELLOW + e.getPlayer().getName() + " is joining as an admin on the new map!");
 				e.getPlayer().sendMessage(ChatColor.RED + "[jtWAR] Teleporting you to spawn immediately to setup new map.");
 				e.getPlayer().sendMessage(ChatColor.RED + "[jtWAR] Please set new spawn points with /blu and /red");
 				e.getPlayer().teleport(e.getPlayer().getWorld().getSpawnLocation());
 				e.getPlayer().setGameMode(GameMode.CREATIVE);
 			}
 		}
 		
 		e.setJoinMessage(null);
 		for (Player p : this.getServer().getOnlinePlayers()) {
 			if (p.hasPermission("jtwar.admin")) {
 				StringBuilder mrow = new StringBuilder();
 				if (e.getPlayer().hasPlayedBefore()) {
 					mrow.append(", HasPlayed");
 				} else {
 					mrow.append(", New");
 				}
 				if (e.getPlayer().hasPermission("jtwar.admin")) {
 					mrow.append(", Admin");
 				}
 				p.sendMessage(ChatColor.YELLOW + "J: " + e.getPlayer().getName() + mrow);
 			} else {
 				p.sendMessage(ChatColor.YELLOW + e.getPlayer().getName() + " has joined the War!");
 			}
 		}
 		e.getPlayer().sendMessage(ChatColor.RED + "Welcome to the war! To join type /join");
 		e.getPlayer().sendMessage(ChatColor.RED + "For more information say /war");
 		on++;
 		online.add(e.getPlayer().getName());
 		getServer().broadcastMessage(on + " now online!");
 	}
 		
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void omgTagging(PlayerQuitEvent e) {
 		on--;
 		online.remove(e.getPlayer().getName());
 		if (red.contains(e.getPlayer().getName())) red.remove(e.getPlayer().getName());
 		if (blu.contains(e.getPlayer().getName())) blu.remove(e.getPlayer().getName());
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void dontTouchThat(BlockBreakEvent e) {
 		if (blocks) {
 			e.setCancelled(true);
 			e.getPlayer().sendMessage(ChatColor.RED + "The objective of the game is to kill all of the other team");
 			e.getPlayer().sendMessage(ChatColor.RED + "To do this you don't need to break blocks!");
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void dontPlaceThat(BlockPlaceEvent e) {
 		if (blocks) {
 			e.setCancelled(true);
 			e.getPlayer().sendMessage(ChatColor.RED + "The objective of the game is to kill all of the other team");
 			e.getPlayer().sendMessage(ChatColor.RED + "To do this you don't need to place blocks!");
 		}
 	}
 
 }
