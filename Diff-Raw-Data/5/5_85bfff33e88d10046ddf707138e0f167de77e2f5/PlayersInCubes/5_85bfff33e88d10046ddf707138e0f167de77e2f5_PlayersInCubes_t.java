 package me.asofold.bukkit.pic;
 
 import java.io.File;
 
 import me.asofold.bukkit.pic.core.PicCore;
 import me.asofold.bukkit.pic.listeners.PicListener;
 import me.asofold.bukkit.pic.util.Utils;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public final class PlayersInCubes extends JavaPlugin {
 	
 	private final PicCore core = new PicCore();
 	private final PicListener listener = new PicListener(core);
 
 	@Override
 	public void onDisable() {
 		core.clear();
 		System.out.println("[PIC] " + getDescription().getFullName() +" is has been disabled.");
 	}
 
 	@Override
 	public void onEnable() {
 		core.reload(new File(getDataFolder(), "config.yml"));
 		getServer().getPluginManager().registerEvents(listener, this);
 		System.out.println("[PIC] " + getDescription().getFullName() +" is now enabled.");
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		if (command != null) label = command.getLabel();
 		label = label.trim().toLowerCase();
 		if (!label.equals("playersincubes")) return false;
 		int len = args.length;
 		String cmd = null;
 		if (len > 0) cmd = args[0].trim().toLowerCase();
		if (len == 1 && cmd.equals("reload")){
 			if (!Utils.checkPerm(sender, "playersincubes.reload")) return true;
 			if (core.reload(new File(getDataFolder(), "config.yml"))) sender.sendMessage("[PIC] Settings reloaded.");
 			else sender.sendMessage("[PIC] Reloading the settings failed.");
 			return true;
 		}
		else if (len == 1 && cmd.equals("stats")){
 			if (!Utils.checkPerm(sender, "playersincubes.stats.view")) return true;
 			sender.sendMessage(core.getStats().getStatsStr(sender instanceof Player));
 			return true;
 		}
 		return false;
 	}
 	
 }
