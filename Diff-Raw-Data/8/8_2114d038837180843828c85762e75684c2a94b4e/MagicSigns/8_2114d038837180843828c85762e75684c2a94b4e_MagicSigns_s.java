 package de.philworld.bukkit.magicsigns;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.serialization.ConfigurationSerialization;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import de.philworld.bukkit.magicsigns.config.MagicSignSerializationProxy;
 import de.philworld.bukkit.magicsigns.signs.ClearSign;
 import de.philworld.bukkit.magicsigns.signs.CreativeModeSign;
 import de.philworld.bukkit.magicsigns.signs.FeedSign;
 import de.philworld.bukkit.magicsigns.signs.HealSign;
 import de.philworld.bukkit.magicsigns.signs.HealthSign;
 import de.philworld.bukkit.magicsigns.signs.LevelSign;
 import de.philworld.bukkit.magicsigns.signs.MagicSign;
 import de.philworld.bukkit.magicsigns.signs.RocketSign;
 import de.philworld.bukkit.magicsigns.signs.SpeedSign;
 import de.philworld.bukkit.magicsigns.signs.SurvivalModeSign;
 import de.philworld.bukkit.magicsigns.signs.TeleportSign;
 import de.philworld.bukkit.magicsigns.signs.command.CommandSign;
 import de.philworld.bukkit.magicsigns.signs.command.ConsoleCommandSign;
 
 public class MagicSigns extends JavaPlugin {
 
	private SignManager signManager = new SignManager(this);
 	private FileConfiguration config;
 
 	private static MagicSigns instance;
 
 	public static Economy economy = null;
 
 	public static MagicSigns inst() {
 		return instance;
 	}
 
 	@Override
 	public void onEnable() {
 
 		instance = this;
 
 		loadConfiguration();
 
 		// register all sign types
 		signManager.registerSignType(CommandSign.class);
 		signManager.registerSignType(ConsoleCommandSign.class);
 		signManager.registerSignType(SpeedSign.class);
 		signManager.registerSignType(HealSign.class);
 		signManager.registerSignType(HealthSign.class);
 		signManager.registerSignType(ClearSign.class);
 		signManager.registerSignType(TeleportSign.class);
 		signManager.registerSignType(RocketSign.class);
 		signManager.registerSignType(LevelSign.class);
 		signManager.registerSignType(CreativeModeSign.class);
 		signManager.registerSignType(SurvivalModeSign.class);
 		signManager.registerSignType(FeedSign.class);
 
 		// and then load them from the config
 		loadSigns();
 
 		if (setupEconomy()) {
 			getLogger().log(Level.INFO, "Using Vault for economy.");
 		} else {
 			getLogger().log(Level.INFO,
 					"Vault was not found, all signs will be free!");
 		}
 
 		getServer().getPluginManager().registerEvents(
 				new MagicSignsListener(this), this);
 
 		// start metrics
 		try {
 			new Metrics(this).start();
 		} catch (IOException e) {
 			getLogger().log(Level.WARNING, "Error enabling Metrics for MagicSigns:", e);
 		}
 	}
 
 	@Override
 	public void onDisable() {
 		saveSigns();
 	}
 
 	/**
 	 * Public alias for {@link SignManager#registerSignType(Class)}
 	 * @see SignManager#registerSignType(Class)
 	 * @param signType
 	 */
 	public void registerSignType(Class<? extends MagicSign> signType) {
 		signManager.registerSignType(signType);
 	}
 
 	/**
 	 * Loads the configuration and inserts the defaults.
 	 */
 	private void loadConfiguration() {
 		ConfigurationSerialization
 				.registerClass(MagicSignSerializationProxy.class);
 		config = getConfig();
 		config.options().copyDefaults(true);
 		saveConfig();
 	}
 
 	@SuppressWarnings("unchecked")
 	private void loadSigns() {
 		signManager.loadConfig(getConfig());
 
 		List<MagicSignSerializationProxy> list = (List<MagicSignSerializationProxy>) config
 				.get("magic-signs");
 
 		if (list == null || list.isEmpty())
 			return;
 
 		for (MagicSignSerializationProxy proxy : list) {
 			try {
 				signManager.registerSign(proxy.getMagicSign());
 			} catch (Throwable e) {
 				getLogger().log(
 						Level.WARNING,
 						"Error loading Magic Sign from config: "
 								+ e.getMessage(), e);
 			}
 		}
 	}
 
 	private void saveSigns() {
 		// reset list first
 		config.set("magic-signs", null);
 
 		List<MagicSignSerializationProxy> signList = new LinkedList<MagicSignSerializationProxy>();
 		for (MagicSign sign : signManager.getSigns()) {
 			signList.add(sign.serialize());
 		}
 		config.set("magic-signs", signList);
 
 		signManager.saveConfig(getConfig());
 
 		saveConfig();
 	}
 
 	private boolean setupEconomy() {
 		try {
 			RegisteredServiceProvider<Economy> economyProvider = getServer()
 					.getServicesManager().getRegistration(
 							net.milkbowl.vault.economy.Economy.class);
 
 			if (economyProvider == null)
 				return false;
 
 			economy = economyProvider.getProvider();
 
 			return true;
 		} catch (NoClassDefFoundError e) {
 			return false;
 		}
 	}
 
 }
