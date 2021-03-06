 package fr.ribesg.bukkit.nworld;
 
 import fr.ribesg.bukkit.ncore.common.NLocation;
 import fr.ribesg.bukkit.ncore.node.world.WorldNode;
 import fr.ribesg.bukkit.nworld.config.Config;
 import fr.ribesg.bukkit.nworld.lang.Messages;
 import fr.ribesg.bukkit.nworld.warp.Warps;
 import fr.ribesg.bukkit.nworld.world.AdditionalWorld;
 import fr.ribesg.bukkit.nworld.world.GeneralWorld.WorldType;
 import fr.ribesg.bukkit.nworld.world.StockWorld;
 import fr.ribesg.bukkit.nworld.world.Worlds;
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.WorldCreator;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.plugin.PluginManager;
 import org.mcstats.Metrics;
 
 import java.io.IOException;
 
 /**
  * The main plugin class.
  *
  * @author Ribesg
  */
 public class NWorld extends WorldNode {
 
 	// Configs
 	private Messages messages;
 	private Config   pluginConfig;
 
 	// Useful Nodes
 	// // None
 
 	// Actual plugin data
 	private Worlds worlds;
 	private Warps  warps;
 
 	@Override
 	protected String getMinCoreVersion() {
 		return "0.4.0";
 	}
 
 	@Override
 	public boolean onNodeEnable() {
 		// Messages first !
 		try {
 			if (!getDataFolder().isDirectory()) {
 				boolean res = getDataFolder().mkdir();
 				if (!res) {
 					getLogger().severe("Unable to create subfolder in /plugins/");
 					return false;
 				}
 			}
 			messages = new Messages();
 			messages.loadMessages(this);
 		} catch (final IOException e) {
 			getLogger().severe("An error occured, stacktrace follows:");
 			e.printStackTrace();
 			getLogger().severe("This error occured when NWorld tried to load messages.yml");
 			return false;
 		}
 
 		worlds = new Worlds();
 		warps = new Warps();
 
 		// Config
 		try {
 			pluginConfig = new Config(this);
 			pluginConfig.loadConfig();
 		} catch (final IOException | InvalidConfigurationException e) {
 			getLogger().severe("An error occured, stacktrace follows:");
 			e.printStackTrace();
 			getLogger().severe("This error occured when NWorld tried to load config.yml");
 			return false;
 		}
 
 		worlds = pluginConfig.getWorlds();
 		warps = pluginConfig.getWarps();
 
 		// This loop will detect newly created worlds
 		// - Default main World, at first plugin start
 		// - Nether & End when activated
 		for (World w : Bukkit.getWorlds()) {
 			warps.worldEnabled(w.getName());
 			if (!worlds.containsKey(w.getName())) {
 				WorldType type = null;
 				switch (w.getEnvironment()) {
 					case NORMAL:
 						type = WorldType.STOCK;
 						break;
 					case NETHER:
 						type = WorldType.STOCK_NETHER;
 						break;
 					case THE_END:
 						type = WorldType.STOCK_END;
 						break;
 				}
 				final StockWorld world = new StockWorld(this,
 				                                        w.getName(),
 				                                        type,
 				                                        new NLocation(w.getSpawnLocation()),
 				                                        pluginConfig.getDefaultRequiredPermission(),
 				                                        true,
 				                                        false);
 				worlds.put(w.getName(), world);
 			}
 		}
 
 		// This loop will create/load additional worlds
 		for (AdditionalWorld w : worlds.getAdditional().values()) {
 			if (w.isEnabled()) {
 				// Create (Load) the world
 				WorldCreator creator = new WorldCreator(w.getWorldName());
 				creator.environment(World.Environment.NORMAL);
 				creator.seed(w.getSeed());
 				World world = Bukkit.createWorld(creator);
 
 				// Re-set spawn location
 				NLocation loc = w.getSpawnLocation();
 				world.setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
 
 				// Check if some warps should be enabled
 				warps.worldEnabled(w.getWorldName());
 
 				// Load Nether if needed
 				if (w.hasNether()) {
 					String netherName = w.getWorldName() + "_nether";
 
 					// Create (Load) the world
 					WorldCreator netherCreator = new WorldCreator(netherName);
 					netherCreator.environment(World.Environment.NETHER);
 					netherCreator.seed(w.getSeed());
 					Bukkit.createWorld(netherCreator);
 
 					// Check if some warps should be enabled
 					warps.worldEnabled(netherName);
 				}
 				// Load End if needed
 				if (w.hasEnd()) {
 					String endName = w.getWorldName() + "_the_end";
 
 					// Create (Load) the world
 					WorldCreator endCreator = new WorldCreator(endName);
 					endCreator.environment(World.Environment.THE_END);
 					endCreator.seed(w.getSeed());
 					Bukkit.createWorld(endCreator);
 
 					// Check if some warps should be enabled
 					warps.worldEnabled(endName);
 				}
 			}
 		}
 
 		// Listener
 		final PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(new NListener(this), this);
 
 		// Commands
 		final WorldCommandExecutor executor = new WorldCommandExecutor(this);
 		getCommand("nworld").setExecutor(executor);
 		getCommand("spawn").setExecutor(executor);
 		getCommand("setspawn").setExecutor(executor);
 		getCommand("warp").setExecutor(executor);
 		getCommand("setwarp").setExecutor(executor);
		getCommand("delwarp").setExecutor(executor);
 
 		// Metrics - Worlds
 		final Metrics.Graph g1 = getMetrics().createGraph("Amount of Worlds");
 		g1.addPlotter(new Metrics.Plotter("Normal") {
 
 			@Override
 			public int getValue() {
 				return getWorlds().sizeNormal();
 			}
 		});
 		g1.addPlotter(new Metrics.Plotter("Nether") {
 
 			@Override
 			public int getValue() {
 				return getWorlds().sizeNether();
 			}
 		});
 		g1.addPlotter(new Metrics.Plotter("End") {
 
 			@Override
 			public int getValue() {
 				return getWorlds().sizeEnd();
 			}
 		});
 
 		// Metrics - Warps
 		final Metrics.Graph g2 = getMetrics().createGraph("Amount of Warps");
 		g2.addPlotter(new Metrics.Plotter() {
 
 			@Override
 			public int getValue() {
 				return getWarps().size();
 			}
 		});
 
 		return true;
 	}
 
 	@Override
 	public void onNodeDisable() {
 		try {
 			getPluginConfig().writeConfig();
 		} catch (final IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/** @see fr.ribesg.bukkit.ncore.node.NPlugin#handleOtherNodes() */
 	@Override
 	protected void handleOtherNodes() {
 		// Nothing to do here for now
 	}
 
 	@Override
 	public Messages getMessages() {
 		return messages;
 	}
 
 	public Config getPluginConfig() {
 		return pluginConfig;
 	}
 
 	private boolean isMainWorld(World world) {
 		return world != null && isMainWorld(world.getName());
 	}
 
 	private boolean isMainWorld(String worldName) {
 		return Bukkit.getWorlds().get(0).getName().equals(worldName);
 	}
 
 	public Warps getWarps() {
 		return warps;
 	}
 
 	public Worlds getWorlds() {
 		return worlds;
 	}
 }
