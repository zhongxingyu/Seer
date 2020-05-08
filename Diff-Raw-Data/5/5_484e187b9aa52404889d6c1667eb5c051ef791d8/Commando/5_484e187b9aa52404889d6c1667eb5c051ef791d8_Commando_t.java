 package no.jckf.commando;
 
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.sk89q.worldedit.bukkit.selections.Selection;
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.Arrays;
 import java.util.HashMap;
 
 public class Commando extends JavaPlugin implements Listener {
 	WorldEditPlugin we;
 	HashMap<Location,String> blocks;
 
 	public void onEnable() {
 		blocks = new HashMap<Location,String>();
 
 		PluginManager pm = getServer().getPluginManager();
 
 		we = (WorldEditPlugin) pm.getPlugin("WorldEdit");
 
 		if (we == null) {
 			getLogger().severe("Could not find WorldEdit!");
 			pm.disablePlugin(this);
 			return;
 		}
 
 		FileConfiguration config = getConfig();
 		for (String key : config.getRoot().getKeys(false)) {
 			String[] loc = config.getString(key + ".location").split("\\|");
 			blocks.put(
 				new Location(
					getServer().getWorld(loc[0]),
 					Double.parseDouble(loc[1]),
 					Double.parseDouble(loc[2]),
 					Double.parseDouble(loc[3])
 				),
				config.getString(key + ".command")
 			);
 		}
 
 		getServer().getPluginManager().registerEvents(this,this);
 	}
 
 	public void onDisable() {
 
 	}
 
 	public boolean onCommand(CommandSender sender,Command command,String label,String[] args) {
 		if (!(sender instanceof Player)) {
 			sender.sendMessage("This command only works in-game.");
 			return true;
 		}
 
 		if (args.length == 0) {
 			return false;
 		}
 
 		Player player = (Player) sender;
 
 		Selection sel = we.getSelection(player);
 
 		if (sel == null) {
 			player.sendMessage(ChatColor.RED + "No selection made.");
 			return true;
 		}
 
 		if (sel.getHeight() > 1 || sel.getWidth() > 1) {
 			player.sendMessage(ChatColor.RED + "Only one block must be selected.");
 			return true;
 		}
 
 		Location loc = sel.getMaximumPoint();
 
 		if (args[0].equalsIgnoreCase("set")) {
 			if (args.length < 2) {
 				return false;
 			}
 
 			blocks.put(loc,StringUtils.join(Arrays.copyOfRange(args,1,args.length)," "));
 
 			player.sendMessage(ChatColor.GREEN + "Command set.");
 		} else if (args[0].equalsIgnoreCase("get")) {
 			if (blocks.containsKey(loc)) {
 				player.sendMessage(ChatColor.GREEN + "Command: " + blocks.get(loc));
 			} else {
 				player.sendMessage(ChatColor.RED + "This block has no associated command.");
 			}
 		} else if (args[0].equalsIgnoreCase("unset")) {
 			if (blocks.containsKey(loc)) {
 				blocks.remove(loc);
 				player.sendMessage(ChatColor.GREEN + "Command unset.");
 			} else {
 				player.sendMessage(ChatColor.RED + "This block has no associated command.");
 			}
 		}
 
 		FileConfiguration config = getConfig();
 		for (String key : config.getRoot().getKeys(false)) {
 			config.set(key,null);
 		}
 		int i = 0;
 		for (Location l : blocks.keySet()) {
 			config.set(
 				i + ".location",
 				l.getWorld().getName() + "|" +
 				l.getBlockX() + "|" +
 				l.getBlockY() + "|" +
 				l.getBlockZ()
 			);
 			config.set(
 				i + ".command",
 				blocks.get(l)
 			);
 
 			i++;
 		}
 		saveConfig();
 
 		return true;
 	}
 
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		switch (event.getAction()) {
 			case LEFT_CLICK_BLOCK:
 			case RIGHT_CLICK_BLOCK:
 				break;
 			default:
 				return;
 		}
 
 		Location location = event.getClickedBlock().getLocation();
 
 		if (blocks.containsKey(location)) {
 			getServer().dispatchCommand(
 				new CommandoSender(StringUtils.capitalize(location.getBlock().getType().toString().replace("_"," ").toLowerCase())),
 				blocks.get(location).replace("@p",event.getPlayer().getName())
 			);
 		}
 	}
 }
