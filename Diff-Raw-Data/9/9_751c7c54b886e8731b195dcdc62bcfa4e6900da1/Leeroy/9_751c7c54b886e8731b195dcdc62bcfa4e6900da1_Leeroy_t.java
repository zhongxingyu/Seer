 package net.loadingchunks.plugins.Leeroy;
 
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.onarandombox.MultiverseCore.MultiverseCore;
 import net.loadingchunks.plugins.Leeroy.Types.*;
 import net.milkbowl.vault.economy.Economy;
 
 public class Leeroy extends JavaPlugin {
 	
 	public Logger log = Logger.getLogger("Minecraft");
 	public MultiverseCore mvcore;
 	public HashMap<String, Object> NPCList = new HashMap<String, Object>();
 	public LeeroyNPCHandler npcs;
 	public LeeroySQL sql;
 	private LeeroyCommands cmdExecutor;
 	public Economy eco;
 	public HashMap<String, String> inviteList = new HashMap<String, String>();
 
 	public void onEnable() {
 		this.getServer().getPluginManager().registerEvents(new LeeroyPlayerListener(this), this);
 		this.getServer().getPluginManager().registerEvents(new LeeroyNPCListener(this), this);
 
 		this.cmdExecutor = new LeeroyCommands(this);
 		
 		getCommand("leeroy").setExecutor(this.cmdExecutor);
 		getCommand("invite").setExecutor(this.cmdExecutor);
 		getCommand("accept").setExecutor(this.cmdExecutor);
 
 		if(!this.getServer().getPluginManager().isPluginEnabled("Multiverse-Core"))
 		{
 			log.info("[LEEROY] Multiverse not found, disabling...");
 			this.getServer().getPluginManager().disablePlugin(this);
 		}		
 
 		this.mvcore = this.getMVCore();
 		
 		if(!this.mvcore.isEnabled())
 			return;
 		
 		log.info("[LEEROY] Loading Config...");
 
 		this.getConfig();
 		
 		log.info("[LEEROY] Alright chumps let's do this.");
 		
 		log.info("[LEEROY] Time to create the saved NPCs.");
 		
 		log.info("[LEEROY] Border Limits on Home World: Min X = " + this.getConfig().getDouble("home.border.x-min") + ", Max X = " + this.getConfig().getDouble("home.border.x-max") + ", Min Z = " + this.getConfig().getDouble("home.border.z-min") + ", Max Z " + this.getConfig().getDouble("home.border.z-max"));
 
 		this.npcs = new LeeroyNPCHandler(this);
 
 		this.sql = new LeeroySQL(this);
 		
 		if(!this.sql.success)
 			this.getServer().getPluginManager().disablePlugin(this);
 		
 		this.sql.PopNPCs();
 		final Leeroy plugin = this;
		this.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
 			public void run() {
 				plugin.log.info("[LEEROY] Running checks on homeworlds.");
 				List<World> worlds = plugin.getServer().getWorlds();
 				for(World w : worlds)
 				{
 					if(w == null)
 						continue;
 
 					if(w.getName().startsWith("homeworld_") && w.getPlayers().isEmpty())
 					{
 						plugin.log.info("[LEEROY] Checking " + w.getName());
 						if(LeeroyUtils.hasNPC(plugin, w.getName()) && plugin.NPCList.containsKey(w.getName() + "_butler"))
 						{
 							plugin.log.info("[LEEROY] Redundant NPC Found in " + w.getName());
 							((ButlerNPC)plugin.NPCList.get(w.getName() + "_butler")).npc.removeFromWorld();
 							plugin.NPCList.remove(w.getName() + "_butler");
 						}
 						plugin.mvcore.getMVWorldManager().unloadWorld(w.getName());
 					} else if (w.getPlayers().size() > 0 && w.getName().startsWith("homeworld_") && !LeeroyUtils.hasNPC(plugin, w.getName()))
 					{
 						plugin.log.info("[LEEROY] No NPC found in loaded world " + w.getName());
 						Location nl = new Location(w, plugin.getConfig().getDouble("home.butler.x"), plugin.getConfig().getDouble("home.butler.y"), plugin.getConfig().getDouble("home.butler.z"));
 						plugin.npcs.spawn("butler",plugin.getConfig().getString("home.butler.name"), nl, "", "", "", "", false, w.getName(), w.getName() + "_butler");
 					}
 				}
 			}
 		}, 60L, 2400L);
 		
 		// Init Vault
 		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
 		this.eco = economyProvider.getProvider();
 	}
 	
 	public void onDisable() {
 		try {
 			this.sql.con.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		log.info("[LEEROY] JEEEEENKIIIINS");
 	}
 	
 	public MultiverseCore getMVCore() {
 
 		if (this.mvcore == null) {
 			this.mvcore = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");
 
 			if (this.mvcore == null) {
 				this.log.severe("Multiverse-Core *NOT* found! Is it installed and enabled? "
 						+ "If you are using any Beta or Dev builds of Bukkit, make sure you have compatible builds of Multiverse-Core.");
 				this.log.severe("Multiverse Core not found!");
 				return null;
 			} else
 				this.log.info("Multiverse-Core found.");
 
 			this.mvcore.incrementPluginCount();
 		}
 
 		return this.mvcore;
 	}
 }
