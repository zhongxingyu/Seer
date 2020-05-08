 package net.battlenexus.bukkit.battlecommands.main;
 
 import java.util.logging.Logger;
 
 import net.battlenexus.bukkit.battlecommands.db.MySQL;
 import net.battlenexus.bukkit.battlecommands.db.SQL;
 import net.battlenexus.bukkit.battlecommands.system.CommandThread;
 
import org.bukkit.Bukkit;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BattleCommands extends JavaPlugin {
 	private SQL sql;
 	final Logger log = Logger.getLogger("Minecraft");
 	private CommandThread t;
 	private static BattleCommands INSTANCE;
 	
 	@Override
 	public void onLoad() {
 		saveDefaultConfig();
 	}
 	
 	@Override
 	public void onEnable() {
 		log("Connecting MySQL");
 		sql = new MySQL();
 		sql.init(this);
 		log("Starting timer..");
 		t = new CommandThread();
 		t.startRun();
 		INSTANCE = this;
 	}
 	
 	@Override
 	public void onDisable() {
 		if (sql != null) {
 			log("Disconnecting MySQL..");
 			sql.disconnect();
 		}
 		if (t != null) {
 			log("Stopping timers..");
 			t.stopRun();
 		}
 	}
 	
 	public SQL getSQL() {
 		return sql;
 	}
 	
 	public static final BattleCommands getInstance() {
 		return INSTANCE;
 	}
 	
 	public void executeCommand(String command) {
 		log.info("Executing " + command);
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
 	}
 	
 	public void log(String message) {
 		log.info("[BattleCommands] " + message);
 	}
 }
