 /**
  * Config file mimicking DiddiZ's Config class file in LB. Tailored for this
  * plugin.
  * 
  * @author Mitsugaru
  */
 package com.mitsugaru.Karmiconomy;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import com.mitsugaru.Karmiconomy.DatabaseHandler.Field;
 
 public class Config
 {
 	// Class variables
 	private Karmiconomy plugin;
 	public String host, port, database, user, password, tablePrefix;
 	public boolean debugTime, debugEvents, debugEconomy, debugUnhandled,
 			useMySQL, importSQL, chat, chatDenyPay, chatDenyLimit, command,
 			commandDenyPay, commandDenyLimit, blockPlace, blockPlaceDenyPay,
 			blockPlaceDenyLimit, blockDestroy, blockDestroyDenyPay,
 			blockDestroyDenyLimit, craftItem, craftItemDenyPay,
 			craftItemDenyLimit, enchantItem, enchantItemDenyPay,
 			enchantItemDenyLimit, portalCreateNether, portalCreateEnd,
 			portalCreateCustom, portalCreateNetherDenyPay,
 			portalCreateNetherDenyLimit, portalCreateEndDenyPay,
 			portalCreateEndDenyLimit, portalCreateCustomDenyPay,
 			portalCreateCustomDenyLimit, portalEnter, shootBow,
 			shootBowDenyPay, shootBowDenyLimit, tameOcelot, tameOcelotDenyPay,
 			tameOcelotDenyLimit, tameWolf, tameWolfDenyPay, tameWolfDenyLimit,
 			paintingPlace, paintingPlaceDenyPay, paintingPlaceDenyLimit,
 			bedEnter, bedEnterDenyPay, bedEnterDenyLimit, bedLeave,
 			bucketEmptyLava, bucketEmptyLavaDenyPay, bucketEmptyLavaDenyLimit,
 			bucketFillLava, bucketFillLavaDenyPay, bucketFillLavaDenyLimit,
 			bucketEmptyWater, bucketEmptyWaterDenyPay,
 			bucketEmptyWaterDenyLimit, bucketFillWater, bucketFillWaterDenyPay,
 			bucketFillWaterDenyLimit, worldChange, death, respawn, itemDrop,
 			itemDropDenyPay, itemDropDenyLimit, eggThrow, gameModeCreative,
 			gameModeCreativeDenyPay, gameModeCreativeDenyLimit,
 			gameModeSurvival, gameModeSurvivalDenyPay,
 			gameModeSurvivalDenyLimit, kick, join, quit, sneak, sneakDenyPay,
 			sneakDenyLimit, sprint, sprintDenyPay, sprintDenyLimit,
 			vehicleEnter, vehicleEnterDenyPay, vehicleEnterDenyLimit,
 			vehicleExit, vehicleExitDenyPay, vehicleExitDenyLimit,
 			blockPlaceStatic, blockDestroyStatic, craftItemStatic,
 			enchantItemStatic, itemDropStatic, commandStatic, pickup,
 			pickupStatic, pickupDenyPay, pickupDenyLimit/*
 														 * ,blockIgnite ,
 														 * blockIgniteValid
 														 */;
 	public int listlimit, bedEnterLimit, bedLeaveLimit, blockDestroyLimit,
 	/* blockIgniteLimit, */blockPlaceLimit, shootBowLimit,
 			bucketEmptyLavaLimit, bucketEmptyWaterLimit, bucketFillLavaLimit,
 			bucketFillWaterLimit, craftLimit, enchantLimit, itemDropLimit,
 			chatLimit, deathLimit, gameModeCreativeLimit,
 			gameModeSurvivalLimit, kickLimit, joinLimit, quitLimit,
 			respawnLimit, sneakLimit, sprintLimit, vehicleEnterLimit,
 			vehicleExitLimit, paintingPlaceLimit, commandLimit,
 			worldChangeLimit, tameOcelotLimit, tameWolfLimit,
 			portalCreateNetherLimit, portalCreateEndLimit,
 			portalCreateCustomLimit, portalEnterLimit, eggThrowLimit,
 			pickupLimit;
 	public double bedEnterPay, bedLeavePay, blockDestroyPay, /* blockIgnitePay, */
 	blockPlacePay, shootBowPay, bucketEmptyLavaPay, bucketEmptyWaterPay,
 			bucketFillLavaPay, bucketFillWaterPay, craftPay, enchantPay,
 			itemDropPay, eggThrowPay, chatPay, deathPay, gameModePay, kickPay,
 			joinPay, quitPay, respawnPay, sneakPay, sprintPay, vehicleEnterPay,
 			vehicleExitPay, paintingPlacePay, tameOcelotPay, tameWolfPay,
 			gameModeCreativePay, gameModeSurvivalPay, commandPay,
 			worldChangePay, portalCreateNetherPay, portalCreateEndPay,
 			portalCreateCustomPay, portalEnterPay, shootBowForce, pickupPay;
 	private final Map<Item, KCItemInfo> values = new HashMap<Item, KCItemInfo>();
 
 	// TODO ability to change config in-game
 
 	// IDEA Ability to change the colors for all parameters
 	// such as item name, amount, data value, id value, enchantment name,
 	// enchantment lvl, page numbers, maybe even header titles
 	/**
 	 * Constructor and initializer
 	 * 
 	 * @param KarmicShare
 	 *            plugin
 	 */
 	public Config(Karmiconomy plugin)
 	{
 		this.plugin = plugin;
 		// Grab config
 		final ConfigurationSection config = plugin.getConfig();
 		// LinkedHashmap of defaults
 		final Map<String, Object> defaults = new LinkedHashMap<String, Object>();
 		defaults.put("listlimit", 10);
 		defaults.put("bed.enter.enabled", false);
 		defaults.put("bed.enter.denyOnLackPay", false);
 		defaults.put("bed.enter.denyOnLimit", false);
 		defaults.put("bed.enter.limit", 10);
 		defaults.put("bed.enter.pay", 0.1);
 		defaults.put("bed.leave.enabled", false);
 		defaults.put("bed.leave.limit", 10);
 		defaults.put("bed.leave.pay", 0.1);
 		defaults.put("block.destroy.enabled", false);
 		defaults.put("block.destroy.denyOnLackPay", false);
 		defaults.put("block.destroy.denyOnLimit", false);
 		defaults.put("block.destroy.static", true);
 		defaults.put("block.destroy.limit", 100);
 		defaults.put("block.destroy.pay", 0.1);
 		/*
 		 * defaults.put("block.ignite.enabled", false);
 		 * defaults.put("block.ignite.denyOnLackPay", false);
 		 * defaults.put("block.ignite.static", true);
 		 * defaults.put("block.ignite.limit", 100);
 		 * defaults.put("block.ignite.pay", 0.1);
 		 */
 		defaults.put("block.place.enabled", false);
 		defaults.put("block.place.denyOnLackPay", false);
 		defaults.put("block.place.denyOnLimit", false);
 		defaults.put("block.place.static", true);
 		defaults.put("block.place.limit", 100);
 		defaults.put("block.place.pay", 0.1);
 		defaults.put("bow.shoot.enabled", false);
 		defaults.put("bow.shoot.denyOnLackPay", false);
 		defaults.put("bow.shoot.denyOnLimit", false);
 		// TODO only pay on force
 		defaults.put("bow.shoot.forcefactor", 1.0);
 		defaults.put("bow.shoot.limit", 100);
 		defaults.put("bow.shoot.pay", 0.1);
 		// TODO milk bucket
 		defaults.put("bucket.empty.lava.enabled", false);
 		defaults.put("bucket.empty.lava.denyOnLackPay", false);
 		defaults.put("bucket.empty.lava.denyOnLimit", false);
 		defaults.put("bucket.empty.lava.limit", 100);
 		defaults.put("bucket.empty.lava.pay", 0.1);
 		defaults.put("bucket.fill.lava.enabled", false);
 		defaults.put("bucket.fill.lava.denyOnLackPay", false);
 		defaults.put("bucket.fill.lava.denyOnLimit", false);
 		defaults.put("bucket.fill.lava.limit", 100);
 		defaults.put("bucket.fill.lava.pay", 0.1);
 		defaults.put("bucket.empty.water.enabled", false);
 		defaults.put("bucket.empty.water.denyOnLackPay", false);
 		defaults.put("bucket.empty.water.denyOnLimit", false);
 		defaults.put("bucket.empty.water.limit", 100);
 		defaults.put("bucket.empty.water.pay", 0.1);
 		defaults.put("bucket.fill.water.enabled", false);
 		defaults.put("bucket.fill.water.denyOnLackPay", false);
 		defaults.put("bucket.fill.water.denyOnLimit", false);
 		defaults.put("bucket.fill.water.limit", 100);
 		defaults.put("bucket.fill.water.pay", 0.1);
 		defaults.put("item.craft.enabled", false);
 		defaults.put("item.craft.static", true);
 		defaults.put("item.craft.denyOnLackPay", false);
 		defaults.put("item.craft.denyOnLimit", false);
 		defaults.put("item.craft.limit", 100);
 		defaults.put("item.craft.pay", 0.1);
 		defaults.put("item.enchant.enabled", false);
 		defaults.put("item.enchant.denyOnLackPay", false);
 		defaults.put("item.enchant.denyOnLimit", false);
 		defaults.put("item.enchant.static", true);
 		defaults.put("item.enchant.limit", 100);
 		defaults.put("item.enchant.pay", 0.1);
 		defaults.put("item.drop.enabled", false);
 		defaults.put("item.drop.denyOnLackPay", false);
 		defaults.put("item.drop.denyOnLimit", false);
 		defaults.put("item.drop.static", true);
 		defaults.put("item.drop.limit", 100);
 		defaults.put("item.drop.pay", 0.1);
 		defaults.put("item.pickup.enabled", false);
 		defaults.put("item.pickup.denyOnLackPay", false);
 		defaults.put("item.pickup.denyOnLimit", false);
 		defaults.put("item.pickup.static", true);
 		defaults.put("item.pickup.limit", 100);
 		defaults.put("item.pickup.pay", 0.1);
 		defaults.put("item.egg.enabled", false);
 		defaults.put("item.egg.limit", 100);
 		defaults.put("item.egg.pay", 0.1);
 		defaults.put("painting.enabled", false);
 		defaults.put("painting.denyOnLackPay", true);
 		defaults.put("painting.denyOnLimit", true);
 		defaults.put("painting.limit", 100);
 		defaults.put("painting.pay", 0.1);
 		defaults.put("player.chat.enabled", false);
 		defaults.put("player.chat.denyOnLackPay", false);
 		defaults.put("player.chat.denyOnLimit", false);
 		defaults.put("player.chat.limit", 10);
 		defaults.put("player.chat.pay", 0.1);
 		defaults.put("player.command.enabled", false);
 		defaults.put("player.command.denyOnLackPay", false);
 		defaults.put("player.command.denyOnLimit", false);
 		defaults.put("player.command.static", true);
 		defaults.put("player.command.limit", 10);
 		defaults.put("player.command.pay", 0.1);
 		defaults.put("player.death.enabled", false);
 		defaults.put("player.death.limit", 100);
 		defaults.put("player.death.pay", -1);
 		defaults.put("player.gamemode.creative.enabled", false);
 		defaults.put("player.gamemode.creative.limit", 10);
 		defaults.put("player.gamemode.creative.pay", -10);
 		defaults.put("player.gamemode.survival.enabled", false);
 		defaults.put("player.gamemode.survival.limit", 1);
 		defaults.put("player.gamemode.survival.pay", 0.1);
 		defaults.put("player.join.enabled", false);
 		defaults.put("player.join.limit", 1);
 		defaults.put("player.join.pay", 10);
 		defaults.put("player.kick.enabled", false);
 		defaults.put("player.kick.limit", 10);
 		defaults.put("player.kick.pay", -10);
 		defaults.put("player.quit.enabled", false);
 		defaults.put("player.quit.limit", 1);
 		defaults.put("player.quit.pay", 0.1);
 		defaults.put("player.respawn.enabled", false);
 		defaults.put("player.respawn.limit", 100);
 		defaults.put("player.respawn.pay", -0.1);
 		defaults.put("player.sneak.enabled", false);
 		defaults.put("player.sneak.denyOnLackPay", false);
 		defaults.put("player.sneak.denyOnLimit", false);
 		defaults.put("player.sneak.limit", 10);
 		defaults.put("player.sneak.pay", 0.1);
 		defaults.put("player.sprint.enabled", false);
 		defaults.put("player.sprint.denyOnLackPay", false);
 		defaults.put("player.sprint.denyOnLimit", false);
 		defaults.put("player.sprint.limit", 10);
 		defaults.put("player.sprint.pay", 0.1);
 		defaults.put("portal.createNether.enabled", false);
 		defaults.put("portal.createNether.denyOnLackPay", false);
 		defaults.put("portal.createNether.denyOnLimit", false);
 		defaults.put("portal.createNether.limit", 10);
 		defaults.put("portal.createNether.pay.nether", 0.1);
 		defaults.put("portal.createEnd.enabled", false);
 		defaults.put("portal.createEnd.denyOnLackPay", false);
 		defaults.put("portal.createEnd.denyOnLimit", false);
 		defaults.put("portal.createEnd.limit", 10);
 		defaults.put("portal.createEnd.pay.ender", 0.1);
 		defaults.put("portal.createCustom.enabled", false);
 		defaults.put("portal.createCustom.denyOnLackPay", false);
 		defaults.put("portal.createCustom.denyOnLimit", false);
 		defaults.put("portal.createCustom.limit", 10);
 		defaults.put("portal.createCustom.pay.custom", 0.1);
 		defaults.put("portal.enter.enabled", false);
 		defaults.put("portal.enter.limit", 10);
 		defaults.put("portal.enter.pay", 0.1);
 		defaults.put("tame.ocelot.enabled", false);
 		defaults.put("tame.ocelot.limit", 10);
 		defaults.put("tame.ocelot.pay", 10);
 		defaults.put("tame.wolf.enabled", false);
 		defaults.put("tame.wolf.limit", 10);
 		defaults.put("tame.wolf.pay", 10);
 		defaults.put("vehicle.enter.enabled", false);
 		defaults.put("vehicle.enter.denyOnLackPay", false);
 		defaults.put("vehicle.enter.denyOnLimit", false);
 		defaults.put("vehicle.enter.limit", 100);
 		defaults.put("vehicle.enter.pay", 0.1);
 		defaults.put("vehicle.exit.enabled", false);
 		defaults.put("vehicle.exit.denyOnLackPay", false);
 		defaults.put("vehicle.exit.denyOnLimit", false);
 		defaults.put("vehicle.exit.limit", 100);
 		defaults.put("vehicle.exit.pay", 0.1);
 		defaults.put("world.change.enabled", false);
 		defaults.put("world.change.limit", 15);
 		defaults.put("world.change.pay", 1.0);
 		defaults.put("mysql.use", false);
 		defaults.put("mysql.host", "localhost");
 		defaults.put("mysql.port", 3306);
 		defaults.put("mysql.database", "minecraft");
 		defaults.put("mysql.user", "username");
 		defaults.put("mysql.password", "pass");
 		defaults.put("mysql.tablePrefix", "kcon_");
 		defaults.put("mysql.import", false);
 		defaults.put("debug.events", false);
 		defaults.put("debug.time", false);
 		defaults.put("debug.economy", false);
 		defaults.put("debug.unhandled", false);
 		defaults.put("version", plugin.getDescription().getVersion());
 		// Insert defaults into config file if they're not present
 		for (final Entry<String, Object> e : defaults.entrySet())
 		{
 			if (!config.contains(e.getKey()))
 			{
 				config.set(e.getKey(), e.getValue());
 			}
 		}
 		// Save config
 		plugin.saveConfig();
 		// Load variables from config
 		/**
 		 * SQL info
 		 */
 		useMySQL = config.getBoolean("mysql.use", false);
 		host = config.getString("mysql.host", "localhost");
 		port = config.getString("mysql.port", "3306");
 		database = config.getString("mysql.database", "minecraft");
 		user = config.getString("mysql.user", "user");
 		password = config.getString("mysql.password", "password");
 		tablePrefix = config.getString("mysql.prefix", "kcon_");
 		importSQL = config.getBoolean("mysql.import", false);
 		// Load all other settings
 		this.loadSettings(config);
 		// Load config for item specific value
 		this.loadItemValueMap();
 		// Finally, do a bounds check on parameters to make sure they are legal
 		this.boundsCheck();
 	}
 
 	public void set(String path, Object o)
 	{
 		final ConfigurationSection config = plugin.getConfig();
 		config.set(path, o);
 		plugin.saveConfig();
 	}
 
 	/**
 	 * Check if updates are necessary
 	 */
 	public void checkUpdate()
 	{
 		// Check if need to update
 		ConfigurationSection config = plugin.getConfig();
 		if (Double.parseDouble(plugin.getDescription().getVersion()) > Double
 				.parseDouble(config.getString("version")))
 		{
 			// Update to latest version
 			plugin.getLogger().info(
 					"Updating to v" + plugin.getDescription().getVersion());
 			this.update();
 		}
 	}
 
 	/**
 	 * This method is called to make the appropriate changes, most likely only
 	 * necessary for database schema modification, for a proper update.
 	 */
 	@SuppressWarnings("unused")
 	private void update()
 	{
 		// Grab current version
 		final double ver = Double.parseDouble(plugin.getConfig().getString(
 				"version"));
 
 		// Update version number in config.yml
 		plugin.getConfig().set("version", plugin.getDescription().getVersion());
 		plugin.saveConfig();
 		plugin.getLogger().info("Upgrade complete");
 	}
 
 	/**
 	 * Reloads info from yaml file(s)
 	 */
 	public void reloadConfig()
 	{
 		// Initial relaod
 		plugin.reloadConfig();
 		// Grab config
 		ConfigurationSection config = plugin.getConfig();
 		this.loadSettings(config);
 		listlimit = config.getInt("listlimit", 10);
 		debugTime = config.getBoolean("debug.time", false);
 		debugEvents = config.getBoolean("debug.events", false);
 		debugEconomy = config.getBoolean("debug.economy", false);
 		// Load config for item specific values
 		this.loadItemValueMap();
 		// Check bounds
 		this.boundsCheck();
 		plugin.getLogger().info("Config reloaded");
 	}
 
 	private void loadSettings(ConfigurationSection config)
 	{
 		/**
 		 * General Settings
 		 */
 		listlimit = config.getInt("listlimit", 10);
 		debugTime = config.getBoolean("debug.time", false);
 		debugEvents = config.getBoolean("debug.events", false);
 		debugEconomy = config.getBoolean("debug.economy", false);
 		debugUnhandled = config.getBoolean("debug.unhandled", false);
 		/**
 		 * Event Settings
 		 */
 		/**
 		 * Bed
 		 */
 		// Enter
 		bedEnter = config.getBoolean("bed.enter.enabled", false);
 		bedEnterDenyLimit = config.getBoolean("bed.enter.denyOnLimit", false);
 		bedEnterDenyPay = config.getBoolean("bed.enter.denyOnLackPay", false);
 		bedEnterLimit = config.getInt("bed.enter.limit", 10);
 		bedEnterPay = config.getDouble("bed.enter.pay", 0.1);
 		// Leave
 		bedLeave = config.getBoolean("bed.leave.enabled", false);
 		bedLeaveLimit = config.getInt("bed.leave.limit", 10);
 		bedLeavePay = config.getDouble("bed.leave.pay", 0.1);
 		/**
 		 * Blocks
 		 */
 		// Destroy
 		blockDestroy = config.getBoolean("block.destroy.enabled", false);
 		blockDestroyDenyLimit = config.getBoolean("block.destroy.denyOnLimit",
 				false);
 		blockDestroyDenyPay = config.getBoolean("block.destroy.denyOnLackPay",
 				false);
 		blockDestroyStatic = config.getBoolean("block.destroy.static", true);
 		blockDestroyLimit = config.getInt("block.destroy.limit", 100);
 		blockDestroyPay = config.getDouble("block.destroy.pay", 0.1);
 		/*
 		 * blockIgnite = config.getBoolean("block.ignite.enabled", false);
 		 * defaults.put("block.ignite.static", true); blockIgniteValid =
 		 * config.getBoolean("block.ignite.denyOnLackPay", false);
 		 * blockIgniteLimit = config.getInt("block.ignite.limit", 100);
 		 * defaults.put("block.ignite.pay", 0.1);
 		 */
 		// place
 		blockPlace = config.getBoolean("block.place.enabled", false);
 		blockPlaceStatic = config.getBoolean("block.place.static", true);
 		blockPlaceDenyLimit = config.getBoolean("block.place.denyOnLimit",
 				false);
 		blockPlaceDenyPay = config.getBoolean("block.place.denyOnLackPay",
 				false);
 		blockPlaceLimit = config.getInt("block.place.limit", 100);
 		blockPlacePay = config.getDouble("block.place.pay", 0.1);
 		/**
 		 * Item
 		 */
 		// craft
 		craftItem = config.getBoolean("item.craft.enabled", false);
 		craftItemDenyPay = config.getBoolean("item.craft.denyOnLackPay", false);
 		craftItemDenyLimit = config.getBoolean("item.craft.denyOnLimit", false);
 		craftItemStatic = config.getBoolean("item.craft.static", true);
 		craftLimit = config.getInt("item.craft.limit", 100);
 		craftPay = config.getDouble("item.craft.pay", 0.1);
 		// enchant
 		enchantItem = config.getBoolean("item.enchant.enabled", false);
 		enchantItemDenyPay = config.getBoolean("item.enchant.denyOnLackPay",
 				false);
 		enchantItemDenyLimit = config.getBoolean("item.enchant.denyOnLimit",
 				false);
 		enchantItemStatic = config.getBoolean("item.enchant.static", true);
 		enchantLimit = config.getInt("item.enchant.limit", 100);
 		enchantPay = config.getDouble("item.enchant.pay", 0.1);
 		// drop
 		itemDrop = config.getBoolean("item.drop.enabled", false);
 		itemDropDenyPay = config.getBoolean("item.drop.denyOnLackPay", false);
 		itemDropDenyLimit = config.getBoolean("item.drop.denyOnLimit", false);
 		itemDropStatic = config.getBoolean("item.drop.static", true);
 		itemDropLimit = config.getInt("item.drop.limit", 100);
 		itemDropPay = config.getDouble("item.drop.pay", 0.1);
 		// pickup
 		pickup = config.getBoolean("item.drop.enabled", false);
 		pickupDenyPay = config.getBoolean("item.drop.denyOnLackPay", false);
 		pickupDenyLimit = config.getBoolean("item.drop.denyOnLimit", false);
 		pickupStatic = config.getBoolean("item.drop.static", true);
 		pickupLimit = config.getInt("item.drop.limit", 100);
 		pickupPay = config.getDouble("item.drop.pay", 0.1);
 		// egg
 		eggThrow = config.getBoolean("item.egg.enabled", false);
 		eggThrowLimit = config.getInt("item.egg.limit", 100);
 		eggThrowPay = config.getDouble("item.egg.pay", 0.1);
 		/**
 		 * Bow
 		 */
 		// shoot
 		shootBow = config.getBoolean("bow.shoot.enabled", false);
 		shootBowDenyLimit = config.getBoolean("bow.shoot.denyOnLimit", false);
 		shootBowDenyPay = config.getBoolean("bow.shoot.denyOnLackPay", false);
 		shootBowForce = config.getDouble("bow.shoot.forcefactor", 1.0);
 		shootBowLimit = config.getInt("bow.shoot.limit", 100);
 		shootBowPay = config.getDouble("bow.shoot.pay", 0.1);
 		/**
 		 * Bucket
 		 */
 		// Empty lava
 		bucketEmptyLava = config.getBoolean("bucket.empty.lava.enabled", false);
 		bucketEmptyLavaDenyPay = config.getBoolean(
 				"bucket.empty.lava.denyOnLackPay", false);
 		bucketEmptyLavaDenyLimit = config.getBoolean(
 				"bucket.empty.lava.denyOnLimit", false);
 		bucketEmptyLavaLimit = config.getInt("bucket.empty.lava.limit", 100);
 		bucketEmptyLavaPay = config.getDouble("bucket.empty.lava.pay", 0.1);
 		// Fill lava
 		bucketFillLava = config.getBoolean("bucket.fill.lava.enabled", false);
 		bucketFillLavaDenyPay = config.getBoolean(
 				"bucket.fill.lava.denyOnLackPay", false);
 		bucketFillLavaDenyLimit = config.getBoolean(
 				"bucket.fill.lava.denyOnLimit", false);
 		bucketFillLavaLimit = config.getInt("bucket.fill.lava.limit", 100);
 		bucketFillLavaPay = config.getDouble("bucket.fill.lava.pay", 0.1);
 		// Empty water
 		bucketEmptyWater = config.getBoolean("bucket.empty.water.enabled",
 				false);
 		bucketEmptyWaterDenyPay = config.getBoolean(
 				"bucket.empty.water.denyOnLackPay", false);
 		bucketEmptyWaterDenyLimit = config.getBoolean(
 				"bucket.empty.water.denyOnLimit", false);
 		bucketEmptyWaterLimit = config.getInt("bucket.empty.water.limit", 100);
 		bucketEmptyWaterPay = config.getDouble("bucket.empty.water.pay", 0.1);
 		// Fill water
 		bucketFillWater = config.getBoolean("bucket.fill.water.enabled", false);
 		bucketFillWaterDenyPay = config.getBoolean(
 				"bucket.fill.water.denyOnLackPay", false);
 		bucketFillWaterDenyLimit = config.getBoolean(
 				"bucket.fill.water.denyOnLimit", false);
 		bucketFillWaterLimit = config.getInt("bucket.fill.water.limit", 100);
 		bucketFillWaterPay = config.getDouble("bucket.fill.water.pay", 0.1);
 		/**
 		 * Painting
 		 */
 		// place
 		paintingPlace = config.getBoolean("painting.enabled", false);
 		paintingPlaceLimit = config.getInt("painting.limit", 100);
 		paintingPlacePay = config.getDouble("painting.pay", 0.1);
 		/**
 		 * Player section
 		 */
 		// Chat
 		chat = config.getBoolean("player.chat.enabled", false);
 		chatDenyPay = config.getBoolean("player.chat.denyOnLackPay", false);
 		chatDenyLimit = config.getBoolean("player.chat.denyOnLimit", false);
 		chatLimit = config.getInt("player.chat.limit", 10);
 		chatPay = config.getDouble("player.chat.pay", 0.1);
 		// command
 		command = config.getBoolean("player.command.enabled", false);
 		commandStatic = config.getBoolean("player.command.static", true);
 		commandDenyPay = config.getBoolean("player.command.denyOnLackPay",
 				false);
 		commandDenyLimit = config.getBoolean("player.command.denyOnLimit",
 				false);
 		commandLimit = config.getInt("player.command.limit", 10);
 		commandPay = config.getDouble("player.command.pay", 0.1);
 		// death
 		death = config.getBoolean("player.death.enabled", false);
 		deathLimit = config.getInt("player.death.limit", 100);
 		deathPay = config.getDouble("player.death.pay", -1);
 		// survival
 		gameModeSurvival = config.getBoolean(
 				"player.gamemode.survival.enabled", false);
 		gameModeSurvivalDenyPay = config.getBoolean(
 				"player.gamemode.survival.denyOnLackPay", false);
 		gameModeSurvivalDenyLimit = config.getBoolean(
 				"player.gamemode.survival.denyOnLimit", false);
 		gameModeSurvivalLimit = config.getInt("player.gamemode.survival.limit",
 				1);
 		gameModeSurvivalPay = config.getDouble("player.gamemode.survival.pay",
 				0.1);
 		// creative
 		gameModeCreative = config.getBoolean(
 				"player.gamemode.creative.enabled", false);
 		gameModeCreativeDenyPay = config.getBoolean(
 				"player.gamemode.creative.denyOnLackPay", false);
 		gameModeCreativeDenyLimit = config.getBoolean(
 				"player.gamemode.creative.denyOnLimit", false);
 		gameModeCreativeLimit = config.getInt("player.gamemode.creative.limit",
 				10);
 		gameModeCreativePay = config.getDouble("player.gamemode.creative.pay",
 				-10);
 		// join
 		join = config.getBoolean("player.join.enabled", false);
 		joinLimit = config.getInt("player.join.limit", 1);
 		joinPay = config.getDouble("player.join.pay", 10);
 		// kick
 		kick = config.getBoolean("player.kick.enabled", false);
 		kickLimit = config.getInt("player.kick.limit", 10);
 		kickPay = config.getDouble("player.kick.pay", -10);
 		// quit
 		quit = config.getBoolean("player.quit.enabled", false);
 		quitLimit = config.getInt("player.quit.limit", 1);
 		quitPay = config.getDouble("player.quit.pay", 0.1);
 		// respawn
 		respawn = config.getBoolean("player.respawn.enabled", false);
 		respawnLimit = config.getInt("player.respawn.limit", 100);
 		respawnPay = config.getDouble("player.respawn.pay", -0.1);
 		// sneak
 		sneak = config.getBoolean("player.sneak.enabled", false);
 		sneakDenyPay = config.getBoolean("player.sneak.denyOnLackPay", false);
 		sneakDenyLimit = config.getBoolean("player.sneak.denyOnLimit", false);
 		sneakLimit = config.getInt("player.sneak.limit", 10);
 		sneakPay = config.getDouble("player.sneak.pay", 0.1);
 		// sprint
 		sprint = config.getBoolean("player.sprint.enabled", false);
 		sprintDenyPay = config.getBoolean("player.sprint.denyOnLackPay", false);
 		sprintDenyLimit = config.getBoolean("player.sprint.denyOnLimit", false);
 		sprintLimit = config.getInt("player.sprint.limit", 10);
 		sprintPay = config.getDouble("player.sprint.pay", 0.1);
 		/**
 		 * Portal
 		 */
 		// create nether
 		portalCreateNether = config.getBoolean("portal.createNether.enabled",
 				false);
 		portalCreateNetherDenyPay = config.getBoolean(
 				"portal.createNether.denyOnLackPay", false);
 		portalCreateNetherDenyLimit = config.getBoolean(
 				"portal.createNether.denyOnLimit", false);
 		portalCreateNetherLimit = config
 				.getInt("portal.createNether.limit", 10);
 		portalCreateNetherPay = config.getDouble(
 				"portal.createNether.pay.nether", 0.1);
 		// create end
 		portalCreateEnd = config.getBoolean("portal.createEnd.enabled", false);
 		portalCreateEndDenyPay = config.getBoolean(
 				"portal.createEnd.denyOnLackPay", false);
 		portalCreateEndDenyLimit = config.getBoolean(
 				"portal.createEnd.denyOnLimit", false);
 		portalCreateEndLimit = config.getInt("portal.createEnd.limit", 10);
 		portalCreateEndPay = config
 				.getDouble("portal.createEnd.pay.ender", 0.1);
 		// create custom
 		portalCreateCustom = config.getBoolean("portal.createCustom.enabled",
 				false);
 		portalCreateCustomDenyPay = config.getBoolean(
 				"portal.createCustom.denyOnLackPay", false);
 		portalCreateCustomDenyLimit = config.getBoolean(
 				"portal.createCustom.denyOnLimit", false);
 		portalCreateCustomLimit = config
 				.getInt("portal.createCustom.limit", 10);
 		portalCreateCustomPay = config.getDouble(
 				"portal.createCustom.pay.custom", 0.1);
 		// enter
 		portalEnter = config.getBoolean("portal.enter.enabled", false);
 		portalEnterLimit = config.getInt("portal.enter.limit", 10);
 		portalEnterPay = config.getDouble("portal.enter.pay", 0.1);
 		/**
 		 * Tame
 		 */
 		// ocelot
 		tameOcelot = config.getBoolean("tame.ocelot.enabled", false);
 		tameOcelotDenyPay = config.getBoolean("tame.ocelot.denyOnLackPay",
 				false);
 		tameOcelotDenyLimit = config.getBoolean("tame.ocelot.denyOnLimit",
 				false);
 		tameOcelotLimit = config.getInt("tame.ocelot.limit", 10);
 		tameOcelotPay = config.getDouble("tame.ocelot.pay", 10);
 		// wolf
 		tameWolf = config.getBoolean("tame.wolf.enabled", false);
 		tameWolfDenyPay = config.getBoolean("tame.wolf.denyOnLackPay", false);
 		tameWolfDenyLimit = config.getBoolean("tame.wolf.denyOnLimit", false);
 		tameWolfLimit = config.getInt("tame.wolf.limit", 10);
 		tameWolfPay = config.getDouble("tame.wolf.pay", 10);
 		/**
 		 * Vehicle
 		 */
 		// enter
 		vehicleEnter = config.getBoolean("vehicle.enter.enabled", false);
 		vehicleEnterDenyPay = config.getBoolean("vehicle.enter.denyOnLackPay",
 				false);
 		vehicleEnterDenyLimit = config.getBoolean("vehicle.enter.denyOnLimit",
 				false);
 		vehicleEnterLimit = config.getInt("vehicle.enter.limit", 100);
 		vehicleEnterPay = config.getDouble("vehicle.enter.pay", 0.1);
 		// exit
 		vehicleExit = config.getBoolean("vehicle.exit.enabled", false);
 		vehicleExitLimit = config.getInt("vehicle.exit.limit", 100);
 		vehicleExitPay = config.getDouble("vehicle.exit.pay", 0.1);
 		/**
 		 * World
 		 */
 		// change
 		worldChange = config.getBoolean("world.change.enabled", false);
 		worldChangeLimit = config.getInt("world.change.limit", 15);
 		worldChangePay = config.getDouble("world.change.pay", 1.0);
 	}
 
 	/**
 	 * Check the bounds on the parameters to make sure that all config variables
 	 * are legal and usable by the plugin
 	 */
 	private void boundsCheck()
 	{
 		// TODO format all doubles to 2 decimal places
 	}
 
 	public double getPayValue(Field type, Item item, String command)
 	{
 		double pay = 0.0;
 		switch (type.getTable())
 		{
 			case DATA:
 			{
 				switch (type)
 				{
 					case CHAT:
 						return chatPay;
 					case BED_ENTER:
 						return bedEnterPay;
 					case BED_LEAVE:
 						return bedLeavePay;
 					case BOW_SHOOT:
 						return shootBowPay;
 					case BUCKET_EMPTY_LAVA:
 						return bucketEmptyLavaPay;
 					case BUCKET_EMPTY_WATER:
 						return bucketEmptyWaterPay;
 					case BUCKET_FILL_LAVA:
 						return bucketFillLavaPay;
 					case BUCKET_FILL_WATER:
 						return bucketFillWaterPay;
 					case DEATH:
 						return deathPay;
 					case EGG_THROW:
 						return eggThrowPay;
 					case CREATIVE:
 						return gameModeCreativePay;
 					case SURVIVAL:
 						return gameModeSurvivalPay;
 					case JOIN:
 						return joinPay;
 					case KICK:
 						return kickPay;
 					case QUIT:
 						return quitPay;
 					case RESPAWN:
 						return respawnPay;
 					case PAINTING_PLACE:
 						return paintingPlacePay;
 					case PORTAL_CREATE_NETHER:
 						return portalCreateNetherPay;
 					case PORTAL_CREATE_END:
 						return portalCreateEndPay;
 					case PORTAL_CREATE_CUSTOM:
 						return portalCreateCustomPay;
 					case PORTAL_ENTER:
 						return portalEnterPay;
 					case SNEAK:
 						return sneakPay;
 					case SPRINT:
 						return sprintPay;
 					case TAME_OCELOT:
 						return tameOcelotPay;
 					case TAME_WOLF:
 						return tameWolfPay;
 					case WORLD_CHANGE:
 						return worldChangePay;
 					default:
 						break;
 				}
 				break;
 			}
 			case ITEMS:
 			{
 				// handle custom item limit
 				switch (type)
 				{
 					case BLOCK_PLACE:
 					{
 						if (!blockPlaceStatic)
 						{
 							if (values.containsKey(item))
 							{
 								return values.get(item).placePay;
 							}
 						}
 						return blockPlacePay;
 					}
 					case BLOCK_DESTROY:
 					{
 						if (!blockDestroyStatic)
 						{
 							if (values.containsKey(item))
 							{
 								return values.get(item).destroyPay;
 							}
 						}
 						return blockDestroyPay;
 					}
 					case ITEM_CRAFT:
 					{
 						if (!craftItemStatic)
 						{
 							if (values.containsKey(item))
 							{
 								return values.get(item).craftPay;
 							}
 						}
 						return craftPay;
 					}
 					case ITEM_DROP:
 					{
 						if (!itemDropStatic)
 						{
 							if (values.containsKey(item))
 							{
 								return values.get(item).dropPay;
 							}
 						}
 						return itemDropPay;
 					}
 					case ITEM_ENCHANT:
 					{
 						if (!enchantItemStatic)
 						{
 							if (values.containsKey(item))
 							{
 								return values.get(item).enchantPay;
 							}
 						}
 						return enchantPay;
 					}
 					case ITEM_PICKUP:
 					{
 						if (!pickupStatic)
 						{
 							if (values.containsKey(item))
 							{
 								return values.get(item).pickupPay;
 							}
 						}
 					}
 					default:
 						break;
 				}
 				break;
 			}
 			case COMMAND:
 			{
 				// TODO handle custom command limit
 				break;
 			}
 			case PORTAL:
 			{
 				switch (type)
 				{
 					case PORTAL_CREATE_NETHER:
 						return portalCreateNetherPay;
 					case PORTAL_CREATE_END:
 						return portalCreateEndPay;
 					case PORTAL_CREATE_CUSTOM:
 						return portalCreateCustomPay;
 					case PORTAL_ENTER:
 						return portalEnterPay;
 					default:
 						break;
 				}
 			}
 			case BUCKET:
 			{
 				switch (type)
 				{
 					case BUCKET_EMPTY_LAVA:
 						return bucketEmptyLavaPay;
 					case BUCKET_EMPTY_WATER:
 						return bucketEmptyWaterPay;
 					case BUCKET_FILL_LAVA:
 						return bucketFillLavaPay;
 					case BUCKET_FILL_WATER:
 						return bucketFillWaterPay;
 					default:
 						break;
 				}
 			}
 			default:
 				break;
 		}
 		return pay;
 	}
 
 	public int getLimitValue(Field type, Item item, String command)
 	{
 		int limit = -1;
 		switch (type.getTable())
 		{
 			case DATA:
 			{
 				switch (type)
 				{
 					case CHAT:
 						return chatLimit;
 					case BED_ENTER:
 						return bedEnterLimit;
 					case BED_LEAVE:
 						return bedLeaveLimit;
 					case BOW_SHOOT:
 						return shootBowLimit;
 					case BUCKET_EMPTY_LAVA:
 						return bucketEmptyLavaLimit;
 					case BUCKET_EMPTY_WATER:
 						return bucketEmptyWaterLimit;
 					case BUCKET_FILL_LAVA:
 						return bucketFillLavaLimit;
 					case BUCKET_FILL_WATER:
 						return bucketFillWaterLimit;
 					case DEATH:
 						return deathLimit;
 					case EGG_THROW:
 						return eggThrowLimit;
 					case CREATIVE:
 						return gameModeCreativeLimit;
 					case SURVIVAL:
 						return gameModeSurvivalLimit;
 					case JOIN:
 						return joinLimit;
 					case KICK:
 						return kickLimit;
 					case QUIT:
 						return quitLimit;
 					case RESPAWN:
 						return respawnLimit;
 					case PAINTING_PLACE:
 						return paintingPlaceLimit;
 					case PORTAL_CREATE_NETHER:
 						return portalCreateNetherLimit;
 					case PORTAL_CREATE_END:
 						return portalCreateEndLimit;
 					case PORTAL_CREATE_CUSTOM:
 						return portalCreateCustomLimit;
 					case PORTAL_ENTER:
 						return portalEnterLimit;
 					case SNEAK:
 						return sneakLimit;
 					case SPRINT:
 						return sprintLimit;
 					case TAME_OCELOT:
 						return tameOcelotLimit;
 					case TAME_WOLF:
 						return tameWolfLimit;
 					case WORLD_CHANGE:
 						return worldChangeLimit;
 					default:
 						break;
 				}
 				break;
 			}
 			case ITEMS:
 			{
 				// handle custom item limit
 				switch (type)
 				{
 					case BLOCK_PLACE:
 					{
 						if (!blockPlaceStatic)
 						{
 							if (values.containsKey(item))
 							{
 								return values.get(item).placeLimit;
 							}
 						}
 						return blockPlaceLimit;
 					}
 					case BLOCK_DESTROY:
 					{
 						if (!blockDestroyStatic)
 						{
 							if (values.containsKey(item))
 							{
 								return values.get(item).destroyLimit;
 							}
 						}
 						return blockDestroyLimit;
 					}
 					case ITEM_CRAFT:
 					{
 						if (!craftItemStatic)
 						{
 							if (values.containsKey(item))
 							{
 								return values.get(item).craftLimit;
 							}
 						}
 						return craftLimit;
 					}
 					case ITEM_DROP:
 					{
 						if (!itemDropStatic)
 						{
 							if (values.containsKey(item))
 							{
 								return values.get(item).dropLimit;
 							}
 						}
 						return itemDropLimit;
 					}
 					case ITEM_ENCHANT:
 					{
 						if (!enchantItemStatic)
 						{
 							if (values.containsKey(item))
 							{
 								return values.get(item).enchantLimit;
 							}
 						}
 						return enchantLimit;
 					}
 					case ITEM_PICKUP:
 					{
 						if (!pickupStatic)
 						{
 							if (values.containsKey(item))
 							{
 								return values.get(item).pickupLimit;
 							}
 						}
 					}
 					default:
 					{
 						if (debugUnhandled)
 						{
 							plugin.getLogger().warning(
 									"Unhandled Deny Pay for " + type.name());
 						}
 						break;
 					}
 				}
 				break;
 			}
 			case COMMAND:
 			{
 				// TODO handle custom command limit
 				break;
 			}
 			case PORTAL:
 			{
 				switch (type)
 				{
 					case PORTAL_CREATE_NETHER:
 						return portalCreateNetherLimit;
 					case PORTAL_CREATE_END:
 						return portalCreateEndLimit;
 					case PORTAL_CREATE_CUSTOM:
 						return portalCreateCustomLimit;
 					case PORTAL_ENTER:
 						return portalEnterLimit;
 					default:
 						break;
 				}
 			}
 			case BUCKET:
 			{
 				switch (type)
 				{
 					case BUCKET_EMPTY_LAVA:
 						return bucketEmptyLavaLimit;
 					case BUCKET_EMPTY_WATER:
 						return bucketEmptyWaterLimit;
 					case BUCKET_FILL_LAVA:
 						return bucketFillLavaLimit;
 					case BUCKET_FILL_WATER:
 						return bucketFillWaterLimit;
 					default:
 						break;
 				}
 			}
 			default:
 				break;
 		}
 		return limit;
 	}
 
 	public boolean getItemDenyPay(Field type, Item item)
 	{
 		switch (type)
 		{
 			case BLOCK_PLACE:
 			{
 				if (!blockPlaceStatic)
 				{
 					if (values.containsKey(item))
 					{
 						return values.get(item).placeDenyPay;
 					}
 				}
 				return blockPlaceDenyPay;
 			}
 			case BLOCK_DESTROY:
 			{
 				if (!blockDestroyStatic)
 				{
 					if (values.containsKey(item))
 					{
 						return values.get(item).destroyDenyPay;
 					}
 				}
 				return blockDestroyDenyPay;
 			}
 			case ITEM_CRAFT:
 			{
 				if (!craftItemStatic)
 				{
 					if (values.containsKey(item))
 					{
 						return values.get(item).craftDenyPay;
 					}
 				}
 				return craftItemDenyPay;
 			}
 			case ITEM_DROP:
 			{
 				if (!itemDropStatic)
 				{
 					if (values.containsKey(item))
 					{
 						return values.get(item).dropDenyPay;
 					}
 				}
 				return itemDropDenyPay;
 			}
 			case ITEM_ENCHANT:
 			{
 				if (!enchantItemStatic)
 				{
 					if (values.containsKey(item))
 					{
 						return values.get(item).enchantDenyPay;
 					}
 				}
 				return enchantItemDenyPay;
 			}
 			case ITEM_PICKUP:
 			{
 				if (!pickupStatic)
 				{
 					if (values.containsKey(item))
 					{
 						return values.get(item).pickupDenyPay;
 					}
 				}
 			}
 			default:
 			{
 				if (debugUnhandled)
 				{
 					plugin.getLogger().warning(
 							"Unhandled Deny Pay for " + type.name());
 				}
 				break;
 			}
 		}
 		return false;
 	}
 
 	public boolean getItemDenyLimit(Field type, Item item)
 	{
 		switch (type)
 		{
 			case BLOCK_PLACE:
 			{
 				if (!blockPlaceStatic)
 				{
 					if (values.containsKey(item))
 					{
 						return values.get(item).placeDenyLimit;
 					}
 				}
 				return blockPlaceDenyLimit;
 			}
 			case BLOCK_DESTROY:
 			{
 				if (!blockDestroyStatic)
 				{
 					if (values.containsKey(item))
 					{
 						return values.get(item).destroyDenyLimit;
 					}
 				}
 				return blockDestroyDenyLimit;
 			}
 			case ITEM_CRAFT:
 			{
 				if (!craftItemStatic)
 				{
 					if (values.containsKey(item))
 					{
 						return values.get(item).craftDenyLimit;
 					}
 				}
 				return craftItemDenyLimit;
 			}
 			case ITEM_DROP:
 			{
 				if (!itemDropStatic)
 				{
 					if (values.containsKey(item))
 					{
 						return values.get(item).dropDenyLimit;
 					}
 				}
 				return itemDropDenyLimit;
 			}
 			case ITEM_ENCHANT:
 			{
 				if (!enchantItemStatic)
 				{
 					if (values.containsKey(item))
 					{
 						return values.get(item).enchantDenyLimit;
 					}
 				}
 				return enchantItemDenyLimit;
 			}
 			case ITEM_PICKUP:
 			{
 				if (!pickupStatic)
 				{
 					if (values.containsKey(item))
 					{
 						return values.get(item).pickupDenyLimit;
 					}
 				}
 			}
 			default:
 			{
 				if (debugUnhandled)
 				{
 					plugin.getLogger().warning(
 							"Unhandled Deny Limit for " + type.name());
 				}
 				break;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Loads the per-item karma values into a hashmap for later usage
 	 */
 	private void loadItemValueMap()
 	{
 		// Load karma file
 		final YamlConfiguration valueFile = this.itemValuesFile();
 		// Load custom karma file into map
 		for (final String entry : valueFile.getKeys(false))
 		{
 			try
 			{
 				// Attempt to parse non data value nodes
 				int key = Integer.parseInt(entry);
 				if (key <= 0)
 				{
 					plugin.getLogger().warning(
 							Karmiconomy.TAG
 									+ " Zero or negative item id for entry: "
 									+ entry);
 				}
 				else
 				{
 					// If it has child nodes, parse those as well
 					if (valueFile.isConfigurationSection(entry))
 					{
 						values.put(new Item(key, Byte.parseByte("" + 0),
 								(short) 0), parseInfo(valueFile, entry));
 					}
 					else
 					{
 						plugin.getLogger().warning("No section for " + entry);
 					}
 				}
 			}
 			catch (final NumberFormatException ex)
 			{
 				// Potential data value entry
 				if (entry.contains("&"))
 				{
 					try
 					{
 						final String[] split = entry.split("&");
 						final int item = Integer.parseInt(split[0]);
 						final int data = Integer.parseInt(split[1]);
 						if (item <= 0)
 						{
 							plugin.getLogger()
 									.warning(
 											Karmiconomy.TAG
 													+ " Zero or negative item id for entry: "
 													+ entry);
 						}
 						else
 						{
 							if (valueFile.isConfigurationSection(entry))
 							{
 								if (item != 373)
 								{
 									values.put(
 											new Item(item, Byte.parseByte(""
 													+ data), (short) data),
 											parseInfo(valueFile, entry));
 								}
 								else
 								{
 									values.put(
 											new Item(item, Byte
 													.parseByte("" + 0),
 													(short) data),
 											parseInfo(valueFile, entry));
 								}
 							}
 							else
 							{
 								plugin.getLogger().warning(
 										"No section for " + entry);
 							}
 						}
 					}
 					catch (ArrayIndexOutOfBoundsException a)
 					{
 						plugin.getLogger()
 								.warning(
 										"Wrong format for "
 												+ entry
 												+ ". Must follow '<itemid>&<datavalue>:' entry.");
 					}
 					catch (NumberFormatException exa)
 					{
 						plugin.getLogger().warning(
 								"Non-integer number for " + entry);
 					}
 				}
 				else
 				{
 					plugin.getLogger().warning("Invalid entry for " + entry);
 				}
 			}
 		}
 		plugin.getLogger().info("Loaded custom values");
 	}
 
 	public Map<Item, KCItemInfo> getItemValueMap()
 	{
 		return values;
 	}
 
 	private KCItemInfo parseInfo(YamlConfiguration config, String path)
 	{
 		final double iCraftPay = config.getDouble(path + ".craftPay", craftPay);
 		final double iEnchantPay = config.getDouble(path + ".enchantPay",
 				enchantPay);
 		final double iPlacePay = config.getDouble(path + ".placePay",
 				blockPlacePay);
 		// final double iIgnitePay = config.getDouble(path + ".ignitePay",
 		// blockIgnitePay);
 		final double iDestroyPay = config.getDouble(path + ".destroyPay",
 				blockDestroyPay);
 		final double iDropPay = config
 				.getDouble(path + ".dropPay", itemDropPay);
 		final int iCraftLimit = config.getInt(path + ".craftLimit", craftLimit);
 		final int iEnchantLimit = config.getInt(path + ".enchantLimit",
 				enchantLimit);
 		final int iPlaceLimit = config.getInt(path + ".placeLimit",
 				blockPlaceLimit);
 		// final int iIgniteLimit = config.getInt(path + ".igniteLimit",
 		// blockIgniteLimit);
 		final int iDestroyLimit = config.getInt(path + ".destroyLimit",
 				blockDestroyLimit);
 		final int iDropLimit = config
 				.getInt(path + ".dropLimit", itemDropLimit);
 		final boolean iCraftDenyPay = config.getBoolean(path + ".craftDenyPay",
 				craftItemDenyPay);
 		final boolean iCraftDenyLimit = config.getBoolean(path
 				+ ".craftDenyLimit", craftItemDenyLimit);
 		final boolean iEnchantDenyPay = config.getBoolean(path
 				+ ".enchantDenyPay", enchantItemDenyPay);
 		final boolean iEnchantDenyLimit = config.getBoolean(path
 				+ ".enchantDenyLimit", enchantItemDenyLimit);
 		final boolean iPlaceDenyPay = config.getBoolean(path + ".placeDenyPay",
 				blockPlaceDenyPay);
 		final boolean iPlaceDenyLimit = config.getBoolean(path
 				+ ".placeDenyLimit", blockPlaceDenyLimit);
 		final boolean iDestroyDenyPay = config.getBoolean(path
 				+ ".destroyDenyPay", blockDestroyDenyPay);
 		final boolean iDestroyDenyLimit = config.getBoolean(path
 				+ ".destroyDenyLimit", blockDestroyDenyLimit);
		final boolean iDropDenyPay = config.getBoolean(path + ".dropDenyPay",
 				itemDropDenyPay);
 		final boolean iDropDenyLimit = config.getBoolean(path
				+ ".dropDenyLimit", itemDropDenyLimit);
 		final int iPickupLimit = config.getInt(path + ".pickupLimit", pickupLimit);
 		final double iPickupPay = config.getDouble(path + ".pickupPay", pickupPay);
 		final boolean iPickupDenyPay = config.getBoolean(path + ".pickupDenyPay", pickupDenyPay);
 		final boolean iPickupDenyLimit = config.getBoolean(path + ".pickupDenyLimit", pickupDenyLimit);
 		KCItemInfo info = new KCItemInfo(iCraftLimit, iCraftPay, iCraftDenyPay,
 				iCraftDenyLimit, iEnchantLimit, iEnchantPay, iEnchantDenyPay,
 				iEnchantDenyLimit, iPlaceLimit, iPlacePay, iPlaceDenyPay,
 				iPlaceDenyLimit, /* iIgniteLimit, iIgnitePay, */
 				iDestroyLimit, iDestroyPay, iDestroyDenyPay, iDestroyDenyLimit,
 				iDropLimit, iDropPay, iDropDenyPay, iDropDenyLimit,
 				iPickupLimit, iPickupPay, iPickupDenyPay, iPickupDenyLimit);
 		return info;
 	}
 
 	// TODO command value file
 
 	/**
 	 * Loads the value file. Contains default values If the value file isn't
 	 * there, or if its empty, then load defaults.
 	 * 
 	 * @return YamlConfiguration file
 	 */
 	private YamlConfiguration itemValuesFile()
 	{
 		final File file = new File(plugin.getDataFolder().getAbsolutePath()
 				+ "/values.yml");
 		// TODO rename
 		final YamlConfiguration valueFile = YamlConfiguration
 				.loadConfiguration(file);
 		if (valueFile.getKeys(false).isEmpty())
 		{
 			// TODO all-inclusive defaults
 			// Defaults
 			valueFile.set("14.dropPay", 5);
 			/*
 			 * valueFile.set("15", 2); valueFile.set("17&0", 2);
 			 * valueFile.set("17&1", 2); valueFile.set("17&2", 2);
 			 * valueFile.set("19", 10); valueFile.set("20", 3);
 			 * valueFile.set("22", 36); valueFile.set("24", 2);
 			 * valueFile.set("35&0", 2); valueFile.set("35&1", 2);
 			 * valueFile.set("35&2", 2); valueFile.set("35&3", 2);
 			 * valueFile.set("35&4", 2); valueFile.set("35&5", 2);
 			 * valueFile.set("35&6", 2); valueFile.set("35&7", 2);
 			 * valueFile.set("35&8", 2); valueFile.set("35&9", 2);
 			 * valueFile.set("35&10", 2); valueFile.set("35&11", 2);
 			 * valueFile.set("35&12", 2); valueFile.set("35&13", 2);
 			 * valueFile.set("35&14", 2); valueFile.set("35&15", 2);
 			 * valueFile.set("41", 54); valueFile.set("45", 6);
 			 * valueFile.set("47", 6); valueFile.set("49", 6);
 			 * valueFile.set("57", 225); valueFile.set("89", 4);
 			 * valueFile.set("102", 12); valueFile.set("264", 25);
 			 * valueFile.set("265", 3); valueFile.set("266", 6);
 			 * valueFile.set("322", 10); valueFile.set("331", 2);
 			 * valueFile.set("351&4", 4);
 			 */
 			// Insert defaults into file if they're not present
 			try
 			{
 				// Save the file
 				valueFile.save(file);
 			}
 			catch (IOException e1)
 			{
 				// INFO Auto-generated catch block
 				plugin.getLogger().warning(
 						"File I/O Exception on saving karma list");
 				e1.printStackTrace();
 			}
 		}
 		return valueFile;
 	}
 
 	// Private class to hold item specific information
 	public class KCItemInfo
 	{
 		public double craftPay, enchantPay, placePay, /* ignitePay, */destroyPay,
 				dropPay, pickupPay;
 		public int craftLimit, enchantLimit, placeLimit, /* igniteLimit, */
 		destroyLimit, dropLimit, pickupLimit;
 		public boolean craftDenyPay, craftDenyLimit, enchantDenyPay,
 				enchantDenyLimit, destroyDenyPay, destroyDenyLimit,
 				placeDenyPay, placeDenyLimit, dropDenyPay, dropDenyLimit, pickupDenyPay, pickupDenyLimit;
 
 		public KCItemInfo(int craftLimit, double craftPay,
 				boolean craftDenyPay, boolean craftDenyLimit, int enchantLimit,
 				double enchantPay, boolean enchantDenyPay,
 				boolean enchantDenyLimit, int placeLimit, double placePay,
 				boolean placeDenyPay, boolean placeDenyLimit,
 				/* int igniteLimit, double ignitePay, */int destroyLimit,
 				double destroyPay, boolean destroyDenyPay,
 				boolean destroyDenyLimit, int dropLimit, double dropPay,
 				boolean dropDenyPay, boolean dropDenyLimit, int pickupLimit,
 				double pickupPay, boolean pickupDenyPay, boolean pickupDenyLimit)
 		{
 			this.craftPay = craftPay;
 			this.enchantPay = enchantPay;
 			this.placePay = placePay;
 			/* this.ignitePay = ignitePay; */
 			this.destroyPay = destroyPay;
 			this.dropPay = dropPay;
 			this.craftLimit = craftLimit;
 			this.enchantLimit = enchantLimit;
 			this.placeLimit = placeLimit;
 			/* this.igniteLimit = igniteLimit; */
 			this.destroyLimit = destroyLimit;
 			this.dropLimit = dropLimit;
 			this.craftDenyPay = craftDenyPay;
 			this.craftDenyLimit = craftDenyLimit;
 			this.enchantDenyPay = enchantDenyPay;
 			this.enchantDenyLimit = enchantDenyLimit;
 			this.destroyDenyPay = destroyDenyPay;
 			this.destroyDenyLimit = destroyDenyLimit;
 			this.placeDenyPay = placeDenyPay;
 			this.placeDenyLimit = placeDenyLimit;
 			this.dropDenyPay = dropDenyPay;
 			this.dropDenyLimit = dropDenyLimit;
 			this.pickupLimit = pickupLimit;
 			this.pickupPay = pickupPay;
 			this.pickupDenyLimit = pickupDenyLimit;
 			this.pickupDenyPay = pickupDenyPay;
 		}
 	}
 }
