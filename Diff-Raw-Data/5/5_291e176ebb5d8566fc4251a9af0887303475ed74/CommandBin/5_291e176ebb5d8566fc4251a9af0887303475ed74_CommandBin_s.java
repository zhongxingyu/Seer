 package com.caindonaghey.commandbin;
 
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.caindonaghey.commandbin.commands.AfkCommand;
 import com.caindonaghey.commandbin.commands.BindstickCommand;
 import com.caindonaghey.commandbin.commands.BlockCommand;
 import com.caindonaghey.commandbin.commands.BlockplaceCommand;
 import com.caindonaghey.commandbin.commands.BoltCommand;
 import com.caindonaghey.commandbin.commands.BroadcastCommand;
 import com.caindonaghey.commandbin.commands.CarpetCommand;
 import com.caindonaghey.commandbin.commands.ChunkCommand;
 import com.caindonaghey.commandbin.commands.ClearChatCommand;
 import com.caindonaghey.commandbin.commands.ClearCommand;
 import com.caindonaghey.commandbin.commands.CommandBinCommand;
 import com.caindonaghey.commandbin.commands.CraftCommand;
 import com.caindonaghey.commandbin.commands.CreeperCommand;
 import com.caindonaghey.commandbin.commands.DelwarpCommand;
 import com.caindonaghey.commandbin.commands.EffectCommand;
 import com.caindonaghey.commandbin.commands.EnderCommand;
 import com.caindonaghey.commandbin.commands.ExplodeCommand;
 import com.caindonaghey.commandbin.commands.ExplosionbowCommand;
 import com.caindonaghey.commandbin.commands.FeedCommand;
 import com.caindonaghey.commandbin.commands.FireballCommand;
 import com.caindonaghey.commandbin.commands.FireworkCommand;
 import com.caindonaghey.commandbin.commands.FixlagCommand;
 import com.caindonaghey.commandbin.commands.FlyCommand;
 import com.caindonaghey.commandbin.commands.FreezeCommand;
 import com.caindonaghey.commandbin.commands.FsayCommand;
 import com.caindonaghey.commandbin.commands.GmCommand;
 import com.caindonaghey.commandbin.commands.GodCommand;
 import com.caindonaghey.commandbin.commands.HatCommand;
 import com.caindonaghey.commandbin.commands.HealCommand;
 import com.caindonaghey.commandbin.commands.HomeCommand;
 import com.caindonaghey.commandbin.commands.IPCommand;
 import com.caindonaghey.commandbin.commands.ItemdescCommand;
 import com.caindonaghey.commandbin.commands.ItemnameCommand;
 import com.caindonaghey.commandbin.commands.KillCommand;
 import com.caindonaghey.commandbin.commands.KillmobsCommand;
 import com.caindonaghey.commandbin.commands.LockdownCommand;
 import com.caindonaghey.commandbin.commands.MapCommand;
 import com.caindonaghey.commandbin.commands.MsgCommand;
 import com.caindonaghey.commandbin.commands.MuteCommand;
 import com.caindonaghey.commandbin.commands.NearbyCommand;
 import com.caindonaghey.commandbin.commands.NickCommand;
 import com.caindonaghey.commandbin.commands.NotargetCommand;
 import com.caindonaghey.commandbin.commands.OpenEncCommand;
 import com.caindonaghey.commandbin.commands.OpeninvCommand;
 import com.caindonaghey.commandbin.commands.PtimeCommand;
 import com.caindonaghey.commandbin.commands.PutCommand;
 import com.caindonaghey.commandbin.commands.SethomeCommand;
 import com.caindonaghey.commandbin.commands.SetspawnCommand;
 import com.caindonaghey.commandbin.commands.SetwarpCommand;
 import com.caindonaghey.commandbin.commands.ShootCommand;
 import com.caindonaghey.commandbin.commands.SmokeCommand;
 import com.caindonaghey.commandbin.commands.SpartaCommand;
 import com.caindonaghey.commandbin.commands.SpawnCommand;
 import com.caindonaghey.commandbin.commands.SpawnmobCommand;
 import com.caindonaghey.commandbin.commands.SpyCommand;
 import com.caindonaghey.commandbin.commands.StarveCommand;
 import com.caindonaghey.commandbin.commands.TextpackCommand;
 import com.caindonaghey.commandbin.commands.TimeCommand;
 import com.caindonaghey.commandbin.commands.TpAcceptCommand;
 import com.caindonaghey.commandbin.commands.TpCommand;
 import com.caindonaghey.commandbin.commands.TpDenyCommand;
 import com.caindonaghey.commandbin.commands.TpaCommand;
 import com.caindonaghey.commandbin.commands.TpallCommand;
 import com.caindonaghey.commandbin.commands.TphereCommand;
import com.caindonaghey.commandbin.commands.UpCommand;
 import com.caindonaghey.commandbin.commands.VanishCommand;
 import com.caindonaghey.commandbin.commands.VoteCommand;
 import com.caindonaghey.commandbin.commands.VoteNoCommand;
 import com.caindonaghey.commandbin.commands.VoteYesCommand;
 import com.caindonaghey.commandbin.commands.WarpCommand;
 import com.caindonaghey.commandbin.commands.WeatherCommand;
 import com.caindonaghey.commandbin.commands.WhoCommand;
 import com.caindonaghey.commandbin.commands.WitherCommand;
 import com.caindonaghey.commandbin.listeners.AfkListener;
 import com.caindonaghey.commandbin.listeners.AxeListener;
 import com.caindonaghey.commandbin.listeners.BindListener;
 import com.caindonaghey.commandbin.listeners.BlockedListener;
 import com.caindonaghey.commandbin.listeners.BlockplaceListener;
 import com.caindonaghey.commandbin.listeners.BowListener;
 import com.caindonaghey.commandbin.listeners.CarpetListener;
 import com.caindonaghey.commandbin.listeners.ChunkLoaderListener;
 import com.caindonaghey.commandbin.listeners.DebugListener;
 import com.caindonaghey.commandbin.listeners.FreezeListener;
 import com.caindonaghey.commandbin.listeners.GodListener;
 import com.caindonaghey.commandbin.listeners.HealthListener;
 import com.caindonaghey.commandbin.listeners.LapisListener;
 import com.caindonaghey.commandbin.listeners.LockdownListener;
 import com.caindonaghey.commandbin.listeners.MuteListener;
 import com.caindonaghey.commandbin.listeners.PlayerTargetListener;
 import com.caindonaghey.commandbin.listeners.SlimeListener;
 import com.caindonaghey.commandbin.listeners.SmokeListener;
 import com.caindonaghey.commandbin.listeners.VanishListener;
 import com.caindonaghey.commandbin.listeners.WarpSignListener;
 import com.caindonaghey.commandbin.listeners.WeatherListener;
 
 public class CommandBin extends JavaPlugin {
 	
 	public static CommandBin plugin;
 	public static String language;
 	public static boolean lapisTrampoline = false;
 	public static boolean chunkLoader = false;
 	public static boolean debugOnce = false;
 	public static boolean woodCutter = false;
 	
 	public void onEnable() {
 		registerCommands();
 		registerEvents();
 		registerTimeLock();
 		setupConfig();
 		setupLanguage();
 		setupLapis();
 		setupLoader();
 		setupAxe();
 		classicHurt();
 		timeLock();
 		downfallSetup();
 		System.out.println(Phrases.get("enabled"));
 		System.out.println("CommandBin is sponsored by VPSCraft.net!");
 		plugin = this;
 	}
 	
 	public void onDisable() {
 		System.out.println(Phrases.get("disabled"));
 	}
 	
 	
 	public void registerEvents() {
 		getServer().getPluginManager().registerEvents(new BlockedListener(), this);
 		getServer().getPluginManager().registerEvents(new AfkListener(), this);
 		getServer().getPluginManager().registerEvents(new FreezeListener(), this);
 		getServer().getPluginManager().registerEvents(new GodListener(), this);
 		getServer().getPluginManager().registerEvents(new MuteListener(), this);
 		getServer().getPluginManager().registerEvents(new VanishListener(), this);
 		getServer().getPluginManager().registerEvents(new BowListener(), this);
 		getServer().getPluginManager().registerEvents(new SmokeListener(), this);
 		getServer().getPluginManager().registerEvents(new BindListener(), this);
 		getServer().getPluginManager().registerEvents(new HealthListener(), this);
 		getServer().getPluginManager().registerEvents(new LapisListener(), this);
 		getServer().getPluginManager().registerEvents(new ChunkLoaderListener(), this);
 		getServer().getPluginManager().registerEvents(new BlockplaceListener(), this);
 		getServer().getPluginManager().registerEvents(new DebugListener(), this);
 		getServer().getPluginManager().registerEvents(new AxeListener(), this);
 		getServer().getPluginManager().registerEvents(new CarpetListener(), this);
 		getServer().getPluginManager().registerEvents(new PlayerTargetListener(), this);
 		if(getConfig().getBoolean("settings.aether-slimes")) {
 			getServer().getPluginManager().registerEvents(new SlimeListener(), this);
 		}
 		getServer().getPluginManager().registerEvents(new LockdownListener(), this);
 		getServer().getPluginManager().registerEvents(new WeatherListener(), this);
 		getServer().getPluginManager().registerEvents(new WarpSignListener(), this);
 	}
 	
 	public void registerCommands() {
 		getServer().getPluginCommand("afk").setExecutor(new AfkCommand());
 		getServer().getPluginCommand("block").setExecutor(new BlockCommand());
 		getServer().getPluginCommand("bolt").setExecutor(new BoltCommand());
 		getServer().getPluginCommand("broadcast").setExecutor(new BroadcastCommand());
 		getServer().getPluginCommand("clear").setExecutor(new ClearCommand());
 		getServer().getPluginCommand("explode").setExecutor(new ExplodeCommand());
 		getServer().getPluginCommand("feed").setExecutor(new FeedCommand());
 		getServer().getPluginCommand("freeze").setExecutor(new FreezeCommand());
 		getServer().getPluginCommand("fsay").setExecutor(new FsayCommand());
 		getServer().getPluginCommand("gm").setExecutor(new GmCommand());
 		getServer().getPluginCommand("god").setExecutor(new GodCommand());
 		getServer().getPluginCommand("heal").setExecutor(new HealCommand());
 		getServer().getPluginCommand("kill").setExecutor(new KillCommand());
 		getServer().getPluginCommand("mute").setExecutor(new MuteCommand());
 		getServer().getPluginCommand("time").setExecutor(new TimeCommand());
 		getServer().getPluginCommand("tpaccept").setExecutor(new TpAcceptCommand());
 		getServer().getPluginCommand("tpa").setExecutor(new TpaCommand());
 		getServer().getPluginCommand("tpdeny").setExecutor(new TpDenyCommand());
 		getServer().getPluginCommand("spawn").setExecutor(new SpawnCommand());
 		getServer().getPluginCommand("setspawn").setExecutor(new SetspawnCommand());
 		getServer().getPluginCommand("starve").setExecutor(new StarveCommand());
 		getServer().getPluginCommand("tpall").setExecutor (new TpallCommand());
 		getServer().getPluginCommand("tp").setExecutor (new TpCommand());
 		getServer().getPluginCommand("vanish").setExecutor(new VanishCommand());
 		getServer().getPluginCommand("weather").setExecutor(new WeatherCommand());
 		getServer().getPluginCommand("openinv").setExecutor(new OpeninvCommand());
 		getServer().getPluginCommand("killmobs").setExecutor(new KillmobsCommand());
 		getServer().getPluginCommand("fly").setExecutor(new FlyCommand());
 		getServer().getPluginCommand("chunk").setExecutor(new ChunkCommand());
 		getServer().getPluginCommand("commandbin").setExecutor(new CommandBinCommand());
 		getServer().getPluginCommand("hat").setExecutor(new HatCommand());
 		getServer().getPluginCommand("ip").setExecutor (new IPCommand());
 		getServer().getPluginCommand("tphere").setExecutor(new TphereCommand());
 		getServer().getPluginCommand("explosionbow").setExecutor (new ExplosionbowCommand());
 		getServer().getPluginCommand("nick").setExecutor(new NickCommand());
 		getServer().getPluginCommand("craft").setExecutor(new CraftCommand());
 		getServer().getPluginCommand("clearchat").setExecutor(new ClearChatCommand());
 		getServer().getPluginCommand("home").setExecutor(new HomeCommand());
 		getServer().getPluginCommand("sethome").setExecutor(new SethomeCommand());
 		getServer().getPluginCommand("setwarp").setExecutor(new SetwarpCommand());
 		getServer().getPluginCommand("warp").setExecutor(new WarpCommand());
 		getServer().getPluginCommand("ptime").setExecutor(new PtimeCommand());
 		getServer().getPluginCommand("itemname").setExecutor(new ItemnameCommand());
 		getServer().getPluginCommand("itemdesc").setExecutor(new ItemdescCommand());
 		getServer().getPluginCommand("smoke").setExecutor(new SmokeCommand());
 		getServer().getPluginCommand("delwarp").setExecutor(new DelwarpCommand());
 		getServer().getPluginCommand("spawnmob").setExecutor(new SpawnmobCommand());
 		getServer().getPluginCommand("firework").setExecutor(new FireworkCommand());
 		getServer().getPluginCommand("bindstick").setExecutor(new BindstickCommand());
 		getServer().getPluginCommand("put").setExecutor (new PutCommand());
 		getServer().getPluginCommand("fixlag").setExecutor (new FixlagCommand());
 		getServer().getPluginCommand("creeper").setExecutor(new CreeperCommand());
 		getServer().getPluginCommand("msg").setExecutor(new MsgCommand());
 		getServer().getPluginCommand("shoot").setExecutor(new ShootCommand());
 		getServer().getPluginCommand("openenc").setExecutor(new OpenEncCommand());
 		getServer().getPluginCommand("blockplace").setExecutor(new BlockplaceCommand());
 		getServer().getPluginCommand("spy").setExecutor(new SpyCommand());
 		getServer().getPluginCommand("vote").setExecutor(new VoteCommand());
 		getServer().getPluginCommand("voteyes").setExecutor(new VoteYesCommand());
 		getServer().getPluginCommand("voteno").setExecutor(new VoteNoCommand());
 		getServer().getPluginCommand("carpet").setExecutor(new CarpetCommand());
 		getServer().getPluginCommand("map").setExecutor(new MapCommand());
 		getServer().getPluginCommand("sparta").setExecutor(new SpartaCommand());
 		getServer().getPluginCommand("notarget").setExecutor(new NotargetCommand());
 		getServer().getPluginCommand("ender").setExecutor(new EnderCommand());
 		getServer().getPluginCommand("textpack").setExecutor(new TextpackCommand());
 		getServer().getPluginCommand("effect").setExecutor(new EffectCommand());
 		getServer().getPluginCommand("who").setExecutor(new WhoCommand());
 		getServer().getPluginCommand("lockdown").setExecutor(new LockdownCommand());	
 		getServer().getPluginCommand("wither").setExecutor(new WitherCommand());
 		getServer().getPluginCommand("fireball").setExecutor(new FireballCommand());
 		getServer().getPluginCommand("nearby").setExecutor(new NearbyCommand());
		getServer().getPluginCommand("up").setExecutor(new UpCommand());
 	}
 	
 	public void registerTimeLock() {
 	    getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
 	        public void run() {
 	        	if(TimeCommand.isLockRunning) {
 	        		Bukkit.getServer().getWorld(getConfig().getString("settings.time-lock-world")).setTime(getConfig().getLong("settings.time-lock-time")); // to do. Test
 	        	}
 	        }
 	      }
 	      , 10L, 100L);
 	}
 	
 	public void setupConfig() {
 		if(getConfig().get("settings.language") == null) {
 			getConfig().set("settings.language", "english");
 			saveConfig();
 		}
 		if(getConfig().get("settings.aether-slimes") == null) {
 			getConfig().set("settings.aether-slimes", true);
 			saveConfig();
 		}
 		if(getConfig().get("settings.healthtags") == null) {
 			getConfig().set("settings.healthtags", true);
 			saveConfig();
 		}
 		if(getConfig().get("settings.lapis-trampoline") == null) {
 			getConfig().set("settings.lapis-trampoline", true);
 			saveConfig();
 		}
 		if(getConfig().get("settings.chunk-loader") == null) {
 			getConfig().set("settings.chunk-loader", true);
 			saveConfig();
 		}
 		if(getConfig().get("settings.debugonce") == null) {
 			getConfig().set("settings.debugonce", true);
 			saveConfig();
 		}
 		if(getConfig().get("settings.woodcutter") == null) {
 			getConfig().set("settings.woodcutter", true);
 			saveConfig();
 		}
 		
 		getConfig().options().header(
 				"+-----------------------------\n" +
 				"+ COMMANDBIN CONFIGURATION\n" +
 				"+\n" +
 				"+ Here is the CommandBin Configuration. The settings in here are fully configurable\n" +
 				"+ and will reset if something has gone wrong.\n" +
 				"+ Good luck and have fun with CommandBin!\n" +
 				"+ As a reminder, the latest version is: v" + this.getDescription().getVersion().toString() + "\n" + 
 				"+ ~ Cain\n" +
 				"+-----------------------------");
 		saveConfig();
 	}
 	
 	public void setupLanguage() {
 		if(getConfig().getString("settings.language").equalsIgnoreCase("english")) {
 			language = "english";
 		}
 		if(getConfig().getString("settings.language").equalsIgnoreCase("dutch") || getConfig().getString("settings.language").equalsIgnoreCase("deutch")) {
 			language = "dutch";
 		}
 	}
 	
 	public void setupLapis() {
 		if(getConfig().getBoolean("settings.lapis-trampoline")) {
 			lapisTrampoline = true;
 		}
 	}
 	
 	public void setupLoader() {
 		if(getConfig().getBoolean("settings.chunk-loader")) {
 			chunkLoader = true;
 		}
 	}
 	
 	public void setupDebug() {
 		if(getConfig().getBoolean("settings.debugonce")) {
 			debugOnce = true;
 		}
 	}
 	
 	public void setupAxe() {
 		if(getConfig().getBoolean("settings.woodcutter")) {
 			woodCutter = true;
 		}
 	}
 	
 	public void classicHurt() {
 		if(getConfig().get("settings.classichurt") == null) {
 			getConfig().set("settings.classichurt", true);
 			saveConfig();
 		}
 	}
 	
 	public void timeLock() {
 		if(getConfig().get("settings.time-lock") == null) {
 			getConfig().set("settings.time-lock", false);
 			saveConfig();
 		}
 		
 		if(getConfig().getBoolean("settings.time-lock")) {
 			TimeCommand.isLockRunning = true;
 		}
 	}
 	
 	public void downfallSetup() {
 		if(getConfig().get("settings.enable-downfall") == null) {
 			getConfig().set("settings.enable-downfall", true);
 			saveConfig();
 		}
 	}
 	
 	// v5.09
 	// /map now works in console
 	// Added -l argument to /killmobs, will kill with lightning
 	// Added /sparta command (Shoots entities near you in the air and sets on fire)
 	// Added /notarget command (Stops entities from targeting you)
 	// Added pitch and yaw support to warps and homes
 	// Fixed /warp list returning bad permission errors.
 	// Added non-buggy in-built /spawn command from Bukkit, now relys on CommandBin and has pitch * yaw support.
 	// ^ Please note, if you do not have a spawn set already then it will default to the world, so be sure to set your spawns.
 	// Disabled chunk loaded messages
 	// Added /firework console support
 	// Added /ender chest support to open other peoples ender chests.
 	// Added /textpack command, set everyones texture pack
 	// Added classic damage sound (Enabled true by default)
 	
 
 }
