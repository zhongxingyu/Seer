 package me.butkicker12.Shotgun;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.logging.Level;
 
 import me.butkicker12.Shotgun.Metrics.Metrics;
 import me.butkicker12.Shotgun.Metrics.Metrics.Graph;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Shotgun extends JavaPlugin {
 
 	private File configFile = null;
 	private FileConfiguration config = null;
 
 	public void onEnable() {
 
 		this.loadConfig();
 		this.saveConfig();
 
 		this.checkUpdate();
 		this.setupMetrics();
 
 		this.registerEvents();
 	}
 
 	public void onDisable() {
		this.saveConfig();
 	}
 
 	private void registerEvents() {
 		getServer().getPluginManager().registerEvents(new PlayerListener(this),
 				this);
 		getServer().getPluginManager().registerEvents(new EntityListener(this),
 				this);
 	}
 
 	public void isWeaponEnabled() {
 		// TODO
 	}
 
 	public boolean onCommand(CommandSender sender, Command command,
 			String cmdLabel, String[] args) {
 
 		// Player player = (Player) sender;
 		Block target = ((Player) sender).getTargetBlock(null, 200);
 		Location targetLocation = target.getLocation();
 
 		/*
 		 * Check if sender is player
 		 */
 		if ((sender instanceof Player)) {
 			/*
 			 * Airstrike command
 			 */
 			if (command.getName().equalsIgnoreCase("airstrike")) {
 				if (!(args.length == 0)) {
 					sender.sendMessage(ChatColor.BLUE
 							+ "[Shotgun] Did you mean /airstrike?");
 				}
 				if (getCustomConfig().getBoolean("weapon.enabled.airstrike",
 						true)) {
 
 					if (sender.hasPermission("shotgun.airstrike")) {
 						target.getWorld().strikeLightning(targetLocation);
 						target.getWorld().createExplosion(targetLocation, 5);
 						sender.sendMessage(ChatColor.BLUE
 								+ "[Shotgun] Airstrike called at your crosshairs");
 					} else {
 						sender.sendMessage(ChatColor.RED
 								+ "You do not have permission!");
 					}
 				}
 				return true;
 			}
 
 			/*
 			 * Nuke command
 			 */
 			if (command.getName().equalsIgnoreCase("nuke")) {
 				if (!(args.length == 0)) {
 					sender.sendMessage(ChatColor.BLUE
 							+ "[Shotgun] Did you mean /nuke?");
 				}
 				if (getCustomConfig().getBoolean("weapon.enabled.nuke", true)) {
 					if (sender.hasPermission("shotgun.nuke")) {
 						target.getWorld().createExplosion(targetLocation, 30F);
 					} else {
 						sender.sendMessage(ChatColor.RED
 								+ "You do not have permission!");
 					}
 				}
 				return true;
 			}
 
 			/*
 			 * Shotgun stick fire alternative
 			 */
 			if (command.getName().equalsIgnoreCase("shotgun")) {
 				if (sender.hasPermission("shotgun.shotgun")) {
 					if (getCustomConfig().getBoolean(
 							"weapons.shotgun.fire-via-command", true)) {
 						/*
 						 * Checks if player has 5 arrows. If they do then it
 						 * fires.
 						 */
 						if (((Player) sender).getInventory().contains(
 								Material.ARROW, 5)) {
 
 							Inventory inv = ((Player) sender).getInventory();
 							Material type = Material.ARROW;
 							//int amount = 5;
 							int amount = getCustomConfig().getInt("options.weapon.shotgun.inventory-amount");
 							
 							if (amount > 0) {
 								amount = 0;
 							}
 
 							for (ItemStack is : inv.getContents()) {
 								if (is != null && is.getType() == type) {
 									int newamount = is.getAmount() - amount;
 									if (newamount > 0) {
 										is.setAmount(newamount);
 										break;
 									} else {
 										inv.remove(is);
 										amount = -newamount;
 										if (amount == 0)
 											break;
 									}
 								}
							}
 
 							// run task twice
 							for (int i = 0; i < 2; i++) {
 								((Player) sender).getWorld().createExplosion(
 										((Player) sender).getLocation(), -1);
 							}
 
 							// Runs the task 5 times
 							for (int i = 0; i < 5; i++) {
 								((Player) sender).launchProjectile(Arrow.class);
 							}
 						} else {
 							sender.sendMessage(ChatColor.BLUE
 									+ "[Shotgun] You need at least 5 arrows to use the shotgun!");
 						}
 					} else {
 						sender.sendMessage("[Shotgun] enable: 'weapons.shotgun.fire-via-command' for this command to work");
 						return false;
 					}
 				}
 				return true;
 			}
 		} else {
 			sender.sendMessage(ChatColor.RED
 					+ "[Shotgun] You must be a player to use that command!");
 		}
 		return false;
 	}
 
 	public void loadConfig() {
 		if (configFile == null) {
 			configFile = new File(getDataFolder(), "config.yml");
 		}
 		config = YamlConfiguration.loadConfiguration(configFile);
 		writeYaml();
 	}
 
 	public void saveConfig() {
 		if (config == null || configFile == null) {
 			return;
 		}
 		try {
 			getCustomConfig().save(configFile);
 		} catch (IOException ex) {
 			this.getLogger().log(Level.SEVERE,
 					"Could not save config to " + configFile, ex);
 		}
 	}
 
 	public FileConfiguration getCustomConfig() {
 		if (config == null) {
 			this.loadConfig();
 		}
 		return config;
 	}
 
 	private void writeYaml() {
 		// Options
 		if (!(getCustomConfig().contains("options.automatic-update-checker"))) {
 			getCustomConfig().set("options.automatic-update-checker", true);
 		}
 		if (!(getCustomConfig().contains("options.shotgun.fire-via-command"))) {
 			getCustomConfig().set("options.shotgun.fire-via-command", false);
 		}
 		if (!(getCustomConfig().contains("options.log-weapon-use-to-file"))) {
 			getCustomConfig().set("options.log-weapon-use-to-file", false);
 		}
 		if (!(getCustomConfig().contains("options.verbose"))) {
 			getCustomConfig().set("options.verbose", false);
 		}
 		if (!(getCustomConfig().contains("options.weapon.shotgun.inventory-amount"))) {
 			getCustomConfig().set("options.weapon.shotgun.inventory-amount", 5);
 		}
 		// Weapon cooldown
 		getCustomConfig().createSection(
 				"#options below are not used and don't work");
 		if (!getCustomConfig().contains("weapon.cooldown.shotgun")) {
 			getCustomConfig().set("weapon.cooldown.shotgun", 5);
 		}
 		if (!getCustomConfig().contains("weapon.cooldown.nuke")) {
 			getCustomConfig().set("weapon.cooldown.nuke", "20");
 		}
 		if (!getCustomConfig().contains("weapon.cooldown.smoke")) {
 			getCustomConfig().set("weapon.cooldown.smoke", "10");
 		}
 		if (!(getCustomConfig().contains("weapon.cooldown.grenade"))) {
 			getCustomConfig().set("weapon.cooldown.grenade", "10");
 		}
 		if (!(getCustomConfig().contains("weapon.cooldown.grenade-launcher"))) {
 			getCustomConfig().set("weapon.cooldown.grenade-launcher", "20");
 		}
 		/*
 		 * Weapond enabled
 		 */
 		if (!(getCustomConfig().contains("weapon.enabled.shotgun"))) {
 			getCustomConfig().set("weapon.enabled.shotgun", true);
 		}
 		if (!(getCustomConfig().contains("weapon.enabled.nuke"))) {
 			getCustomConfig().set("weapon.enabled.nuke", true);
 		}
 		if (!(getCustomConfig().contains("weapon.enabled.smoke"))) {
 			getCustomConfig().set("weapon.enabled.smoke", true);
 		}
 		if (!(getCustomConfig().contains("weapon.enabled.grenade"))) {
 			getCustomConfig().set("weapon.enabled.grenade", true);
 		}
 		if (!(getCustomConfig().getBoolean("weapon.enabled.airstrike"))) {
 			getCustomConfig().set("weapon.enabled.airstrike", true);
 		}
 		/*
 		 * Not used
 		 * 
 		 * if
 		 * (!(getCustomConfig().contains("weapon.enabled..grenade-launcher"))) {
 		 * getCustomConfig().set("weapon.enabled..grenade-launcher", true); }
 		 * getCustomConfig().set("log plugin use to file", false");
 		 */
 	}
 
 	private void checkUpdate() {
 		if (getCustomConfig().getBoolean("options.automatic-update-checker",
 				true)) {
 			getLogger().log(Level.INFO, "Checking for updates.......");
 
 			URL url;
 			URLConnection connection;
 			InputStreamReader inputstream = null;
 
 			try {
 				url = new URL(
 						"https://dl.dropbox.com/u/39012172/Bukkit/Shotgun/version.txt");
 				connection = url.openConnection();
 				inputstream = new InputStreamReader(connection.getInputStream());
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			BufferedReader reader = new BufferedReader(inputstream);
 			String remoteVersion = "";
 			String pluginVersion = this.getDescription().getVersion();
 
 			try {
 				remoteVersion = reader.readLine();
 				connection = null;
 				inputstream = null;
 				reader.close();
 				reader = null;
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			if (!(remoteVersion.equalsIgnoreCase(pluginVersion))) {
 				getLogger()
 						.log(Level.INFO,
 								"You have updates! Please download version: "
 										+ remoteVersion
 										+ " from the plugin page (http://dev.bukkit.org/server-mods/shotgun/files/)");
 			} else {
 				getLogger().log(Level.INFO, "You have no updates! :D");
 			}
 		}
 	}
	
 	private void setupMetrics() {
 		try {
 			Metrics metrics = new Metrics(this);
 			Graph weapons = metrics.createGraph("Weapons used");
 
 			weapons.addPlotter(new Metrics.Plotter("Shotgun") {
 				public int getValue() {
 					if (getCustomConfig().getBoolean("weapon.enabled.shotgun")) {
 						return 1;
 					}
 					return 0;
 				}
 			});
 
 			weapons.addPlotter(new Metrics.Plotter("Nuke") {
 				public int getValue() {
 					if (getCustomConfig().getBoolean("weapon.enabled.nuke")) {
 						return 1;
 					}
 					return 0;
 				}
 			});
 
 			weapons.addPlotter(new Metrics.Plotter("Smoke Grenade") {
 				public int getValue() {
 					if (getCustomConfig().getBoolean("weapon.enabled.smoke")) {
 						return 1;
 					}
 					return 0;
 				}
 			});
 
 			weapons.addPlotter(new Metrics.Plotter("Frag Grenade") {
 				public int getValue() {
 					if (getCustomConfig().getBoolean("weapon.enabled.grenade")) {
 						return 1;
 					}
 					return 0;
 				}
 			});
 
 			weapons.addPlotter(new Metrics.Plotter("Airstrike") {
 				public int getValue() {
 					if (getCustomConfig()
 							.getBoolean("weapon.enabled.airstrike")) {
 						return 1;
 					}
 					return 0;
 				}
 			});
 
 			metrics.start();
 		} catch (IOException e) {
 			getLogger().log(Level.WARNING,
 					"Failed to submit plugin stats: " + e.getMessage());
 		}
 	}
 
 	public void logUse(String message) {
 		if (getCustomConfig().getBoolean("options.log-weapon-use-to-file")) {
 			File folder = getDataFolder();
 			if (!folder.exists()) {
 				folder.mkdir();
 			}
 
 			File file = new File(folder, "log.txt");
 			if (!file.exists()) {
 				try {
 					file.createNewFile();
 
 					PrintWriter pw = new PrintWriter(new FileWriter(file, true));
 
 					pw.println(message);
 					pw.flush();
 					pw.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 					getLogger().log(Level.WARNING, "Unable to create log file (" + getDataFolder() + "/log.txt)" + e.getMessage());
 				}
 			}
 		}
 	}
 
 	public void shotgunVerbose(Level level, String message) {
 		if (getCustomConfig().getBoolean("options.verbose")) {
 			getLogger().log(level, message);
 		}
 	}
 }
