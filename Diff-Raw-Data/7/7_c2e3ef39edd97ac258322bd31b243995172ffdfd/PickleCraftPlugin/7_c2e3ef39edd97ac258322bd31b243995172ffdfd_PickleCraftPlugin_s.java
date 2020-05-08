 package net.picklecodes;
 
 import java.util.HashMap;
 import java.util.IllegalFormatException;
 import java.util.List;
 import java.util.logging.Logger;
 
 import net.picklecodes.Modules.IModule;
import net.picklecodes.Modules.BitchinReload.BitchinReloadModule;
 import net.picklecodes.Modules.Counter.CounterModule;
 import net.picklecodes.Modules.IgnoreCraft.IgnoreModule;
 import net.picklecodes.Modules.SignRank.SignRankModule;
 import net.picklecodes.Modules.TeleportAsk.TeleportAskModule;
 import net.picklecodes.Modules.Timber.TimberModule;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.server.PluginDisableEvent;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.sk89q.wepif.PermissionsResolverManager;
 
 /**
  * Copyright (c) 2011-2012
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 2 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  * 
  * @author Pickle
  * 
  */
 public class PickleCraftPlugin extends JavaPlugin implements Listener {
 	public static Logger log = Bukkit.getLogger();
 	private static boolean worldedit;
 	
 	public HashMap<String,IModule> modules = new HashMap<String,IModule>();
 	
 	
 	/*
 	 * returns Player and a Boolean (that defines if multiple players was found).
 	 * first value will be Player
 	 * second will be Boolean
 	 * 
 	 * Will return if An exact match was found,
 	 *  and the second value shall be false.
 	 */
 	public static Object[] getPlayer(String name) {
 		name = name.toLowerCase();
 		Object[] playerAndbool = new Object[2];
 		playerAndbool[0] = null;
 		playerAndbool[1] = false;
 		int playerMatch = 0;
 		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
 			String name2 = p.getName().toLowerCase();
 			if (name2.contains(name)) { 
 				playerAndbool[0] = p;
 				if (name2.equals(name)) {
 					playerAndbool[1] = false;
 					break;
 				}
 				else if (++playerMatch > 1) {
 					playerAndbool[1] = true;
 					break;
 				}
 			}
 		}
 		return playerAndbool;
 	}
 	
 	/**
 	 * stole this from ichat's source :X 
 	 */
     public static String Colorize(String string) {
         return string.replaceAll("(&([a-f0-9]))", "\u00A7$2");
     }
     public String getStringFromConfig(String path) {
     		return Colorize(getConfig().getString(path));
     }
     public String getStringFromConfig(String path, Object... args) {
     	try {
     		return Colorize(String.format(getConfig().getString(path), args));
 		}
 		catch(IllegalFormatException e) {
 			e.printStackTrace();
 			return ChatColor.RED +"Check Config: "+ path;
 		}
     }
     
 	public static boolean hasPerm(Player player, String perm) {
 		if (worldedit) {
 			return PermissionsResolverManager.getInstance().hasPermission(player, perm);
 		}
 		return player.hasPermission(perm);
 	}
 	
 	public static boolean hasWorldEdit() { return worldedit; }
 	
 	public void unloadModule(String module) {
 		IModule mod = modules.get(module);
 		if (mod != null) {
 			mod.onDisable();
 		}
 		modules.remove(module);
 	}
 	public void unloadModule(IModule module) {
 		if (module != null) {
 			module.onDisable();
 			modules.remove(module);
 			
 		}
 	}
 	public void loadModule(String key,IModule module) {
 		if (module != null) {
 			if (!modules.containsKey(key)) {
 				module.onEnable();
 			 	modules.put(key, module);
 			}
 		}
 	}
 	@Override
 	public void onDisable() {
 		getServer().getScheduler().cancelTasks(this);
 		for (IModule module : modules.values()) {
 			module.onDisable();
 		}
 		modules.clear();
 	}
 
 	@Override
 	public void onEnable() {
 		if (Bukkit.getServer().getPluginManager().isPluginEnabled("WorldEdit")){
 			PermissionsResolverManager.initialize(this); //wepif
 			PickleCraftPlugin.worldedit = true;
 		}
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 		/* set up new modules */
 		List<?> mods = getConfig().getList("modules");
 		for (int i=0; i < mods.size(); i++) {
 			String m = String.valueOf(mods.get(i));
			if (m.equalsIgnoreCase("reload")) {
				this.loadModule("reload", new BitchinReloadModule(this));
			}
			else if (m.equalsIgnoreCase("ignore")) {
 				this.loadModule("ignore", new IgnoreModule(this));
 			}
 			else if (m.equalsIgnoreCase("teleport")) {
 				this.loadModule("teleport",new TeleportAskModule(this));
 			}
 			else if (m.equalsIgnoreCase("signrank")) {
 				this.loadModule("signrank",new SignRankModule(this));
 			}
 			else if (m.equalsIgnoreCase("counter")) {
 				this.loadModule("counter",new CounterModule(this));
 			}
 			else if (m.equalsIgnoreCase("timber")) {
 				this.loadModule("timber",new TimberModule(this));
 			}
 		}
 		Bukkit.getPluginManager().registerEvents(this, this);
 
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 		if (command.getName().equalsIgnoreCase("picklecraft")){ 
 			PluginDescriptionFile pdfFile = this.getDescription();
 			sender.sendMessage(ChatColor.DARK_GREEN+pdfFile.getName() +" Version "+ pdfFile.getVersion());
 			for (IModule module : modules.values()) {
 				module.sendCommandList(sender);
 			}
 			return true;
 		}	
 		else if (command.getName().equalsIgnoreCase("pony")){ 
 			if (player != null) {
 				if (args.length >= 1) {
 					Pony.Say(player, args[0]);
 				}
 				else {
 					Pony.SayRandom(player);
 				}
 			}
 			else {
 				sender.sendMessage("This is a player only command.");
 			}
 			return true;
 		}
 		else {
 			boolean success = false;
 			for (IModule module : modules.values()) {
 				if (module.onCommand(sender, command, label, args) == true) { 
 					success = true; 
 				}
 			}
 			return success;
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.LOW)
 	public void onPluginDisabled(PluginDisableEvent event) {
 		if (event.getPlugin().getName() == "WorldEdit") {
 			PickleCraftPlugin.worldedit = false;
 		}
 	}
 	@EventHandler(priority = EventPriority.LOW)
 	public void onPluginEnabled(PluginEnableEvent event) {
 		if (event.getPlugin().getName() == "WorldEdit") {
 			PickleCraftPlugin.worldedit = true;
 			PermissionsResolverManager.initialize(this);
 		}
 	}
 }
 
