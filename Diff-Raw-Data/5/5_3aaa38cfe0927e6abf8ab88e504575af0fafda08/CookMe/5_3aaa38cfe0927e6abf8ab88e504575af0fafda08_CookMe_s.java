 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 import org.mcstats.Metrics;
 import org.mcstats.Metrics.Graph;
 
 /**
  * CookeMe for CraftBukkit/Bukkit
  * Handles some general stuff!
  * 
  * Refer to the forum thread:
  * http://bit.ly/cookmebukkit
  * Refer to the dev.bukkit.org page:
  * http://bit.ly/cookmebukkitdev
  *
  * @author xGhOsTkiLLeRx
  * @thanks nisovin for his awesome code snippet!
  * 
  */
 
 public class CookMe extends Plugin {
     // Logger
     public static final Logger log = Logger.getLogger("Minecraft");
     private String name = "CookMe";
     private String version = "1.0";
     private String author = "xGhOsTkiLLeRx";
     private CookMePlayerListener playerListener;
     public CooldownManager cooldownManager;
     public PropertiesFile config, localization;
     public List<String> itemList = new ArrayList<String>();
     public int cooldown, minDuration,maxDuration;
     public double[] percentages = new double[13];
     public boolean debug, messages, permissions, preventVanillaPoison;
     private String rawFood = "RawBeef,RawChicken,RawFish,Pork,RottenFlesh";
     public String[] effects = {"damage", "death", "venom", "hungervenom", "hungerdecrease", "confusion", "blindness", "weakness", "slowness", "slowness_blocks", "instant_damage", "refusing", "wither"};	
 
     // Shutdown
     public void disable() {
 	// Disable command
 	etc.getInstance().removeCommand("/cookme");
 
	// Command
	new CookMeCommands(this);

 	itemList.clear();
 	cooldownManager.clearCooldownList();
 	
 	log.info(name + " " + version + " by " + author + " disabled");
     }
 
     // Start
     public void enable() {
 	// Command
 	etc.getInstance().addCommand("/cookme", "CookMe admin command");
 	playerListener = new CookMePlayerListener(this);
     }
 
     public void initialize() {
 	// Event
 	etc.getLoader().addListener(PluginLoader.Hook.EAT, playerListener, this, PluginListener.Priority.MEDIUM);
 	// Command
 	etc.getLoader().addListener(PluginLoader.Hook.COMMAND, new CookMeCommands(this), this, PluginListener.Priority.MEDIUM);
 
 	config = etc.getLoader().getPlugin(name).getPropertiesFile("config");
 
 	// Localization
 	localization = etc.getLoader().getPlugin(name).getPropertiesFile("localization");
 	loadLocalization();
 	loadConfig();
 	// Try to load
 	try {
 	    localization.load();
 	    config.load();
 	} catch (IOException e) {
 	    log.warning("Failed to load the configs! Please report this! IOException");
 	}
 	checkStuff();
 
 	// Sets the cooldown
 	cooldownManager = new CooldownManager(cooldown);
 	
 	// Stats
 	try {
 	    Metrics metrics = new Metrics("CookMe", "1.0");
 	    // Construct a graph, which can be immediately used and considered as valid
 	    Graph graph = metrics.createGraph("Percentage of affected items");
 	    // Custom plotter for each item
 	    for (String itemName : itemList) {
 		graph.addPlotter(new Metrics.Plotter(itemName) {
 		    public int getValue() {
 			return 1;
 		    }
 		});
 	    }
 	    metrics.start();
 	} catch (IOException e) {
 	    log.warning("Could not start Metrics!");
 	    e.printStackTrace();
 	}
 	
 	log.info(name + " " + version + " by " + author + " initialized");
     }
 
     private void checkStuff() {
 	permissions = config.getBoolean("configuration.permissions");
 	messages = config.getBoolean("configuration.messages");
 	cooldown = config.getInt("configuration.cooldown");
 	minDuration = 20 * config.getInt("configuration.duration.min");
 	maxDuration = 20 * config.getInt("configuration.duration.max");
 	debug = config.getBoolean("configuration.debug");
 	preventVanillaPoison = config.getBoolean("configuration.preventVanillaPoison", false);
 	String[] tempArray = config.getString("food").split(",");
 	for (String tempString : tempArray) {
 	    itemList.add(tempString);
 	}
 	int i = 0;
 	double temp = 0;
 	for (i = 0; i < effects.length; i++) {
 	    percentages[i] = config.getDouble("effects." + effects[i]);
 	    temp += percentages[i];
 	}
 	// If percentage is higher than 100, reset it, log it
 	if ((int) temp > 100) {
 	    for (i = 0; i < percentages.length; i++) {
 		if (i == 1) {
 		    percentages[i] = 4.0;
 		    config.setDouble("effects." + effects[i], 4.0);
 		    continue;
 		}
 		percentages[i] = 8.0;
 		config.setDouble("effects." + effects[i], 8.0);
 	    }
 	    log.warning(Colors.Red + "Detected that the entire procentage is higer than 100. Resetting it to default...");
 	    config.save();
 	}
     }
 
     // Loads the config at start
     private void loadConfig() {
 	// TODO HEADER config.options().header("For help please refer to http://bit.ly/cookmebukkitdev or http://bit.ly/cookmebukkit");
 	if (!config.containsKey("configuration.permissions")) {
 	    config.setBoolean("configuration.permissions", true);
 	}
 	if (!config.containsKey("configuration.message")) {
 	    config.setBoolean("configuration.messages", true);
 	}
 	if (!config.containsKey("configuration.duration.min")) {
 	    config.setInt("configuration.duration.min", 15);
 	}
 	if (!config.containsKey("configuration.duration.max")) {
 	    config.setInt("configuration.duration.max", 30);
 	}
 	if (!config.containsKey("configuration.cooldown")) {
 	    config.setInt("configuration.cooldown", 30);
 	}
 	if (!config.containsKey("configuration.debug")) {
 	    config.setBoolean("configuration.debug", false);
 	}
 	if (!config.containsKey("configuration.preventVanillaPoison")) {
 	    config.setBoolean("configuration.preventVanillaPoison", false);
 	}
 	if (!config.containsKey("effects.death")) {
 	    config.setDouble("effects.damage", 8.0);
 	}
 	if (!config.containsKey("effects.death")) {
 	    config.setDouble("effects.death", 4.0);
 	}
 	if (!config.containsKey("effects.venom")) {
 	    config.setDouble("effects.venom", 8.0);
 	}
 	if (!config.containsKey("effects.hungervenom")) {
 	    config.setDouble("effects.hungervenom", 8.0);
 	}
 	if (!config.containsKey("effects.hungerdecrease")) {
 	    config.setDouble("effects.hungerdecrease", 8.0);
 	}
 	if (!config.containsKey("effects.confusion")) {
 	    config.setDouble("effects.confusion", 8.0);
 	}
 	if (!config.containsKey("effects.blindness")) {
 	    config.setDouble("effects.blindness", 8.0);
 	}
 	if (!config.containsKey("effects.weakness")) {
 	    config.setDouble("effects.weakness", 8.0);
 	}
 	if (!config.containsKey("effects.slowness")) {
 	    config.setDouble("effects.slowness", 8.0);
 	}
 	if (!config.containsKey("effects.slowness_blocks")) {
 	    config.setDouble("effects.slowness_blocks", 8.0);
 	}
 	if (!config.containsKey("effects.instant_damage")) {
 	    config.setDouble("effects.instant_damage", 8.0);
 	}
 	if (!config.containsKey("effects.refusing")) {
 	    config.setDouble("effects.refusing", 8.0);
 	}
 	if (!config.containsKey("effects.wither")) {
 	    config.setDouble("effects.wither", 8.0);
 	}
 	if (!config.containsKey("effects.food")) {
 	    config.setString("food", rawFood);
 	}
 	config.save();
     }
 
     // Loads the localization
     private void loadLocalization() {
 	// TODO HEADER localization.options().header("The underscores are used for the different lines!");
 	if (!localization.containsKey("damage")) {
 	    localization.setString("damage", "&4You got some random damage! Eat some cooked food!");
 	}
 	if (!localization.containsKey("hungervenom")) {
 	    localization.setString("hungervenom", "&4Your foodbar is a random time venomed! Eat some cooked food!");
 	}
 	if (!localization.containsKey("death")) {
 	    localization.setString("death", "&4The raw food killed you! :(");
 	}
 	if (!localization.containsKey("venom")) {
 	    localization.setString("venom", "&4You are for a random time venomed! Eat some cooked food!");
 	}
 	if (!localization.containsKey("hungerdecrease")) {
 	    localization.setString("hungerdecrease", "&4Your food level went down! Eat some cooked food!");
 	}
 	if (!localization.containsKey("confusion")) {
 	    localization.setString("confusion", "&4You are for a random time confused! Eat some cooked food!");
 	}
 	if (!localization.containsKey("blindness")) {
 	    localization.setString("blindness", "&4You are for a random time blind! Eat some cooked food!");
 	}
 	if (!localization.containsKey("weakness")) {
 	    localization.setString("weakness", "&4You are for a random time weak! Eat some cooked food!");
 	}
 	if (!localization.containsKey("slowness")) {
 	    localization.setString("slowness", "&4You are for a random time slower! Eat some cooked food!");
 	}
 	if (!localization.containsKey("slowness_blocks")) {
 	    localization.setString("slowness_blocks", "&4You mine for a random time slower! Eat some cooked food!");
 	}
 	if (!localization.containsKey("instant_damage")) {
 	    localization.setString("instant_damage", "&4You got some magic damage! Eat some cooked food!");
 	}
 	if (!localization.containsKey("refusing")) {
 	    localization.setString("refusing", "&4You decided to save your life and didn't eat this food!");
 	}
 	if (!localization.containsKey("permissison_denied")) {
 	    localization.setString("permission_denied", "&4You don't have the permission to do this!");
 	}
 	if (!localization.containsKey("enable_messages")) {
 	    localization.setString("enable_messages", "&2CookMe &4messages &2enabled!");
 	}
 	if (!localization.containsKey("enable_permissions_1")) {
 	    localization.setString("enable_permissions_1", "&2CookMe &4permissions &2enabled! Only OPs");
 	}
 	if (!localization.containsKey("enable_permissions_2")) {
 	    localization.setString("enable_permissions_2", "&2and players with the permission can use the plugin!");
 	}
 	if (!localization.containsKey("disable_messages")) {
 	    localization.setString("disable_messages", "&2CookMe &4messages &2disabled!");
 	}
 	if (!localization.containsKey("disable_permissions_1")) {
 	    localization.setString("disable_permissions_1", "&2CookMe &4permissions &4disabled!");
 	}
 	if (!localization.containsKey("disable_permissions_2")) {
 	    localization.setString("disable_permissions_2", "&2All players can use the plugin!");
 	}
 	if (!localization.containsKey("reload")) {
 	    localization.setString("reload", "&2CookMe &4%version &2reloaded!");
 	}
 	if (!localization.containsKey("changed_effect")) {
 	    localization.setString("changed_effect", "&2The percentage of the effect &e%effect &4 has been changed to &e%percentage%");
 	}
 	if (!localization.containsKey("changed_cooldown")) {
 	    localization.setString("changed_cooldown", "&2The cooldown time has been changed to &e%value!");
 	}
 	if (!localization.containsKey("changed_duration_max")) {
 	    localization.setString("changed_duration_max", "&2The maximum duration time has been changed to &e%value!");
 	}
 	if (!localization.containsKey("changed_duration_min")) {
 	    localization.setString("changed_duration_min", "&2The minimum duration time has been changed to &e%value!");
 	}
 	if (!localization.containsKey("help_1")) {
 	    localization.setString("help_1", "&2Welcome to the CookMe version &4%version &2help");
 	}
 	if (!localization.containsKey("help_2")) {
 	    localization.setString("help_2", "To see the help type &4/cookme help");
 	}
 	if (!localization.containsKey("help_3")) {
 	    localization.setString("help_3", "You can enable permissions and messages with &4/cookme enable &e<value> &fand &4/cookme disable &e<value>");
 	}
 	if (!localization.containsKey("help_4")) {
 	    localization.setString("help_4", "To reload use &4/cookme reload");
 	}
 	if (!localization.containsKey("help_5")) {
 	    localization.setString("help_5", "To change the cooldown or duration values, type");
 	}
 	if (!localization.containsKey("help_6")) {
 	    localization.setString("help_6", "&4/cookme set cooldown <value> &for &4/cookme set duration min <value>");
 	}
 	if (!localization.containsKey("help_7")) {
 	    localization.setString("help_7", "&4/cookme set duration max <value>");
 	}
 	if (!localization.containsKey("help_8")) {
 	    localization.setString("help_8", "Set the percentages with &4/cookme set &e<value> <percentage>");
 	}
 	if (!localization.containsKey("help_9")) {
 	    localization.setString("help_9", "&eValues: &fpermissions, messages, damage, death, venom,");
 	}
 	if (!localization.containsKey("help_10")) {
 	    localization.setString("help_10", "hungervenom, hungerdecrease, confusion, blindness, weakness");
 	}
 	if (!localization.containsKey("help_11")) {
 	    localization.setString("help_11", "slowness, slowness_blocks, instant_damage, refusing");
 	}
 	if (!localization.containsKey("no_number")) {
 	    localization.setString("no_number", "&4The given argument wasn't a number!");
 	}
 	localization.save();
     }
 
     // Reloads the configs via command /cookme reload
     public void loadConfigsAgain() {
 	try {
 	    config.load();
 	    config.save();
 	    checkStuff();
 	    cooldownManager.setCooldown(cooldown);
 	    localization.load();
 	    localization.save();
 	} catch (IOException e) {
 	    log.warning("Failed to save the localization! Please report this! IOException");
 	}
     }
 
     // Message the sender or player
     public void message(Player player, String message, String value, String percentage) {
 	message = message
 		.replaceAll("&([0-9a-fk-or])", "\u00A7$1")
 		.replaceAll("%version", "1.0")
 		.replaceAll("%effect", value)
 		.replaceAll("%value", value)
 		.replaceAll("%percentage", percentage);
 	if (player != null) {
 	    player.sendMessage(message);
 	}
     }
 }
