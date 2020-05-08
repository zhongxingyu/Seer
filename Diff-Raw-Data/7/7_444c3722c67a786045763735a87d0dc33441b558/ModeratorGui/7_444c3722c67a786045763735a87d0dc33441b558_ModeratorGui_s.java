 package me.heldplayer.ModeratorGui;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.PersistenceException;
 
 import me.heldplayer.ModeratorGui.WebGui.ThreadWebserver;
 import me.heldplayer.ModeratorGui.tables.Bans;
 import me.heldplayer.ModeratorGui.tables.Demotions;
 import me.heldplayer.ModeratorGui.tables.Issues;
 import me.heldplayer.ModeratorGui.tables.Lists;
 import me.heldplayer.ModeratorGui.tables.Promotions;
 import me.heldplayer.ModeratorGui.tables.Unbans;
 
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.avaje.ebean.SqlUpdate;
 
 public class ModeratorGui extends JavaPlugin {
 
 	public PluginDescriptionFile pdf;
 	public List<String> ranks;
 	private ThreadWebserver serverThread;
 	public static ModeratorGui instance;
 	public static final int version = 4;
 	public String[] displayStrings = new String[6];
 	public SimpleDateFormat dateFormat;
 	private FileConfiguration config = null;
 
 	@Override
 	public void onDisable() {
 		serverThread.disconnect();
 
 		getLogger().info("Disabled!");
 	}
 
 	@Override
 	public void onEnable() {
 		instance = this;
 
 		setupDatabase();
 
 		pdf = getDescription();
 
 		config = getConfig();
 
 		FileConfiguration defConfig = YamlConfiguration.loadConfiguration(getResource("config.yml"));
 
 		ranks = config.getStringList("ranks");
 
 		if (!config.isSet("config-version")) {
 			config.set("config-version", 1);
 		}
 
 		if (config.getInt("config-version") < 2) {
 			getServer().getConsoleSender().sendMessage("[" + pdf.getPrefix() + "] " + ChatColor.LIGHT_PURPLE + "Updating table `mgui_issues` for ModeratorGui 1.2");
 
 			SqlUpdate update = getDatabase().createSqlUpdate("ALTER TABLE `mgui_issues` ADD `is_closed` BOOLEAN NOT NULL DEFAULT '0' AFTER `issue`");
 
 			update.execute();
 		}
 
 		if (config.getInt("config-version") < 3) {
 			getServer().getConsoleSender().sendMessage("[" + pdf.getPrefix() + "] " + ChatColor.LIGHT_PURPLE + "Updating config file for for ModeratorGui 1.2");
 
 			config.set("messages", defConfig.get("messages"));
 		}
 
 		if (config.getInt("config-version") < 4) {
 			getServer().getConsoleSender().sendMessage("[" + pdf.getPrefix() + "] " + ChatColor.LIGHT_PURPLE + "Updating config file for for ModeratorGui 1.3");
 
 			config.set("perform", defConfig.get("perform"));
 		}
 
 		config.set("config-version", version);
 
 		displayStrings[0] = config.getString("messages.issue");
 		displayStrings[1] = config.getString("messages.resolved-issue");
 		displayStrings[2] = config.getString("messages.promotion");
 		displayStrings[3] = config.getString("messages.demotion");
 		displayStrings[4] = config.getString("messages.ban");
 		displayStrings[5] = config.getString("messages.unban");
 
 		saveConfig();
 
 		serverThread = new ThreadWebserver(config.getInt("port", 8273), config.getString("host", ""));
 
 		getServer().getPluginManager().registerEvents(new ModGuiListener(this), this);
 
 		getCommand("report").setExecutor(new ReportCommand(this));
 		getCommand("review").setExecutor(new ReviewCommand(this));
 
 		dateFormat = new SimpleDateFormat("MM-dd-yyyy");
 
 		getLogger().info("Enabled!");
 	}
 
 	private void setupDatabase() {
 		try {
 			getDatabase().find(Issues.class).findRowCount();
 			getDatabase().find(Bans.class).findRowCount();
 			getDatabase().find(Unbans.class).findRowCount();
 			getDatabase().find(Promotions.class).findRowCount();
 			getDatabase().find(Demotions.class).findRowCount();
 			getDatabase().find(Lists.class).findRowCount();
 		} catch (PersistenceException ex) {
 			getLogger().info("Installing database due to first time usage");
 			try {
 				installDDL();
 			} catch (Exception ex2) {
 				getLogger().severe("Unable to set up the database!");
 			}
 		}
 	}
 
 	@Override
 	public List<Class<?>> getDatabaseClasses() {
 		List<Class<?>> list = new ArrayList<Class<?>>();
 		list.add(Issues.class);
 		list.add(Bans.class);
 		list.add(Unbans.class);
 		list.add(Promotions.class);
 		list.add(Demotions.class);
 		list.add(Lists.class);
 		return list;
 	}
 
 	public String formatReport(String patern, int id, String target, String reporter, String reason, long date, String oldRank, String newRank) {
 		String result = patern;
 
 		result = result.replaceAll("%id%", id + "");
 		result = result.replaceAll("%target%", target);
 		result = result.replaceAll("%reporter%", reporter);
 		result = result.replaceAll("%reason%", reason);
 		result = result.replaceAll("%date%", dateFormat.format(Long.valueOf(date)));
 		result = result.replaceAll("%oldrank%", oldRank);
 		result = result.replaceAll("%newrank%", newRank);
 
 		result = ChatColor.translateAlternateColorCodes('&', result);
 
 		return result;
 	}
 
 	public void performCommands(String section, CommandSender sender, int id, String target, String reporter, String reason, long date, String oldRank, String newRank) {
 		List<?> commands = config.getList("perform." + section);
 
 		for (Object commandObj : commands) {
 			String command = (String) commandObj;
			
 			command = formatReport(command, id, target, reporter, reason, date, oldRank, newRank);
 
 			if (command.startsWith("C:")) {
 				getServer().dispatchCommand(getServer().getConsoleSender(), command.substring(2));
 			} else if (command.startsWith("P:")) {
 				if (sender instanceof Player) {
 					getServer().dispatchCommand(sender, command.substring(2));
 				} else {
 					getServer().dispatchCommand(sender, command.substring(3));
 				}
 			} else {
 				getServer().dispatchCommand(sender, command);
 			}
 		}
 	}
 
 	public static List<String> getPlayerMatches(String name) {
 		OfflinePlayer[] players = instance.getServer().getOfflinePlayers();
 
 		List<String> matched = new ArrayList<String>();
 
 		for (OfflinePlayer player : players) {
 			if (player.getName().equalsIgnoreCase(name)) {
 				matched.clear();
 				matched.add(player.getName());
 				return matched;
 			}
 			if (player.getName().length() < name.length()) {
 				continue;
 			}
 			if (player.getName().substring(0, name.length()).equalsIgnoreCase(name)) {
 				matched.add(player.getName());
 			}
 		}
 
 		return matched;
 	}
 
 	public static List<String> getRankMatches(String rank) {
 		List<String> ranks = instance.ranks;
 
 		List<String> matched = new ArrayList<String>();
 
 		for (String matchedRank : ranks) {
 			if (matchedRank.equalsIgnoreCase(rank)) {
 				matched.clear();
 				matched.add(matchedRank);
 				return matched;
 			}
 			if (matchedRank.length() < rank.length()) {
 				continue;
 			}
 			if (matchedRank.substring(0, rank.length()).equalsIgnoreCase(rank)) {
 				matched.add(matchedRank);
 			}
 		}
 
 		return matched;
 	}
 }
