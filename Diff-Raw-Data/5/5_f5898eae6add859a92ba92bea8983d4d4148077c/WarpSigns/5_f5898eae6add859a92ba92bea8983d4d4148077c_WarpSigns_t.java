 /**
  * Sign format!
  * 
  * getLine(0) returns line #1
  * getLine(1) returns line #2
  * getLine(2) returns line #3
  * getLine(3) returns line #4
  * 
  * Any other getLine after that will result in an ArrayOutOfBoundsException
  * 
  *0*/
 
 
 package me.smithey.can.code;
 
 import java.io.File;
 import java.io.IOException;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class WarpSigns extends JavaPlugin implements Listener {
 
 	YamlConfiguration cfg = YamlConfiguration.loadConfiguration(new File("plugins/WarpSigns/config.yml"));
 
 	public void onEnable() {
 		getServer().getPluginManager().registerEvents(this, this);
 		createConfig();
 		if(cfg.getBoolean("auto-update") == true) {
 			Updater updater = new Updater(this, "warpsigns", this.getFile(), Updater.UpdateType.DEFAULT, false);
 		} else {}
 		try {
 			Metrics metrics = new Metrics(this);
 			metrics.start();
 		} catch (IOException e) {
 			// Failed to submit the stats :-(
 		}
 	}
 
 	public void onDisable() {}
 
 	@EventHandler
 	public void onSignChange(SignChangeEvent e) {
 		Player p = e.getPlayer();
 		if (e.getLine(0).equalsIgnoreCase("[Warp]"))
 			if(p.hasPermission("warpsigns.create")){
 				if (e.getLine(1).isEmpty()) {
 					p.sendMessage(ChatColor.RED + "Please enter a warp name on line 2!");
 					e.setCancelled(true);
 				} else {
					e.setLine(0, ChatColor.GREEN + "[Warp]"; // revert to using chatcolor rather than ampisan.
 					p.sendMessage(ChatColor.GREEN + "WarpSign Created");
 				}
 			}else{
 				p.sendMessage(ChatColor.RED + "You don't have Permission to create a WarpSign");
 				e.setCancelled(true);
 			}
 	}
 
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent ev) {
 		if (ev.getAction() == Action.RIGHT_CLICK_BLOCK) {
 			Block b = ev.getClickedBlock();
 			if ((b.getType() == Material.SIGN_POST) || (b.getType() == Material.WALL_SIGN)) {
 				Sign s = (Sign) b.getState();
 				Player p = ev.getPlayer();
				if (s.getLine(0).equalsIgnoreCase(ChatColor.GREEN + "[Warp]")) { // this will work? no?
 					if(s.getLine(3).startsWith("{") && s.getLine(3).endsWith("}")) { // Detects if they want to use per sign permission
 						String perm = s.getLine(3).replace("{", ""); // Removes {
 						String fperm = perm.replace("}", ""); // Removes }
 						if(p.hasPermission("WarpSigns.use.*") || p.hasPermission("WarpSigns.use." + fperm)) { // Detects if they have the per sign permission
 							ev.getPlayer().performCommand("warp " + s.getLine(1)); // Warps them if they have the per sign perm
 						}else{
 							p.sendMessage(ChatColor.RED + "You are missing the permission WarpSigns.use." + fperm);
 						}
 					} else {
 						if (p.hasPermission("WarpSigns.use") || p.hasPermission("WarpSigns.use.*")){
 							ev.getPlayer().performCommand("warp " + s.getLine(1));
 						} else {
 							p.sendMessage(ChatColor.RED + "You don't have Permission to use a WarpSign");
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void createConfig() { // Generate config
 		YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/WarpSigns/config.yml"));
 		File f = new File("plugins/WarpSigns/config.yml");
 		if(!f.exists()) {
 		config.set("auto-update", true);
 		try {
 			config.save("plugins/WarpSigns/config.yml");
 		} catch (IOException e) {
 			e.printStackTrace();
 			}
 		} else {
 			//do nothing
 		}
 	}
 }
