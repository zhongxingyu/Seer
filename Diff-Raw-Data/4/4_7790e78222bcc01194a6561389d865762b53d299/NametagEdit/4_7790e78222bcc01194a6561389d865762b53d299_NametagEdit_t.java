 package ca.wacos;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.UUID;
 
 import net.minecraft.server.v1_5_R2.NBTCompressedStreamTools;
 import net.minecraft.server.v1_5_R2.NBTTagCompound;
 import net.minecraft.server.v1_5_R2.NBTTagDouble;
 import net.minecraft.server.v1_5_R2.NBTTagFloat;
 import net.minecraft.server.v1_5_R2.NBTTagList;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class NametagEdit extends JavaPlugin implements Listener {
 	
 	HashMap<String, Location> backs = new HashMap<String, Location>();
 	
 	LinkedHashMap<String, LinkedHashMap<String, String>> groups = null;
 	LinkedHashMap<String, LinkedHashMap<String, String>> config = null;
 	
 	static boolean tabListEnabled = false;
 	static boolean deathMessageEnabled = false;
 	
 	public void onEnable() {
 		ScoreboardManager.load();
 		this.getServer().getPluginManager().registerEvents(this, this);
 		groups = GroupLoader.load(this);
 		load();
 	}
 	public void load() {
 		groups = GroupLoader.load(this);
 		
 		config = ConfigLoader.load(this);
 
 		NametagEdit.tabListEnabled = ConfigLoader.parseBoolean("tab-list-mask", "enabled", config, false);
 		NametagEdit.deathMessageEnabled = ConfigLoader.parseBoolean("death-message-mask", "enabled", config, false);
 		
 		LinkedHashMap<String, LinkedHashMap<String, String>> players = PlayerLoader.load(this);
 		Player[] onlinePlayers = Bukkit.getOnlinePlayers();
 		for (Player p : onlinePlayers) {
 			
 			ScoreboardManager.clear(p.getName());
 			
 			boolean setGroup = true;
 			
 			for (String key : players.keySet().toArray(new String[players.keySet().size()])) {
 				if (p.getName().equals(key)) {
 
 					String prefix = players.get(key).get("prefix");
 					String suffix = players.get(key).get("suffix");
 					if (prefix != null)
 						prefix = formatColors(prefix);
 					if (suffix != null)
 						suffix = formatColors(suffix);
 					ScoreboardManager.overlap(p.getName(), prefix, suffix);
 					
 					setGroup = false;
 				}
 			}
 			if (setGroup) {
 				for (String key : groups.keySet().toArray(new String[groups.keySet().size()])) {
 					if (p.hasPermission(key)) {
 						String prefix = groups.get(key).get("prefix");
 						String suffix = groups.get(key).get("suffix");
 						if (prefix != null)
 							prefix = formatColors(prefix);
 						if (suffix != null)
 							suffix = formatColors(suffix);
 						ScoreboardManager.overlap(p.getName(), prefix, suffix);
 						
 						break;
 					}
 				}
 			}
 			if (NametagEdit.tabListEnabled) {
 				String str = "§f" + p.getName();
 				String tab = "";
 				for (int t = 0; t < str.length() && t < 16; t++)
 					tab += str.charAt(t);
 				p.setPlayerListName(tab);
 			}
 			else {
 				p.setPlayerListName(p.getName());
 			}
 		}
 	}
 	
	@EventHandler (priority = EventPriority.HIGHEST)
 	public void onPlayerJoin(final PlayerJoinEvent e) {
 		
 		ScoreboardManager.clear(e.getPlayer().getName());
 		
 		boolean back = false;
 		
 		for (String name : backs.keySet().toArray(new String[backs.keySet().size()])) {
 			if (name.equals(e.getPlayer().getName())) {
 				back = true;
 				break;
 			}
 		}
 		
 		boolean setGroup = true;
 		
 		LinkedHashMap<String, String> playerData = PlayerLoader.getPlayer(e.getPlayer().getName());
 		if (playerData != null) {
 			String prefix = playerData.get("prefix");
 			String suffix = playerData.get("suffix");
 			if (prefix != null)
 				prefix = formatColors(prefix);
 			if (suffix != null)
 				suffix = formatColors(suffix);
 			if (GroupLoader.DEBUG) {
 				System.out.println("Setting prefix/suffix for " + e.getPlayer().getName() + ": " + prefix + ", " + suffix + " (user)");
 			}
 			ScoreboardManager.overlap(e.getPlayer().getName(), prefix, suffix);
 			setGroup = false;
 		}
 		
 		if (setGroup) {
 			for (String key : groups.keySet().toArray(new String[groups.keySet().size()])) {
 				if (e.getPlayer().hasPermission(key)) {
 					String prefix = groups.get(key).get("prefix");
 					String suffix = groups.get(key).get("suffix");
 					if (prefix != null)
 						prefix = formatColors(prefix);
 					if (suffix != null)
 						suffix = formatColors(suffix);
 					if (GroupLoader.DEBUG) {
 						System.out.println("Setting prefix/suffix for " + e.getPlayer().getName() + ": " + prefix + ", " + suffix + " (node)");
 					}
 					ScoreboardManager.overlap(e.getPlayer().getName(), prefix, suffix);
 					
 					break;
 				}
 			}
 		}
 
 		
 		if (back) {
 			e.getPlayer().teleport(backs.get(e.getPlayer().getName()));
 			while (backs.remove(e.getPlayer().getName()) != null) {}
 		}
 		if (tabListEnabled) {
 			String str = "§f" + e.getPlayer().getName();
 			String tab = "";
 			for (int t = 0; t < str.length() && t < 16; t++)
 				tab += str.charAt(t);
 			e.getPlayer().setPlayerListName(tab);
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerLogin(PlayerLoginEvent e) {
 		Location l = getOfflineLoc(e.getPlayer().getName());
 		if (l != null && l.getWorld() != Bukkit.getWorlds().get(0)) {
 
 			setOfflineLoc(e.getPlayer().getName(), Bukkit.getWorlds().get(0).getSpawnLocation());
 			
 			if (GroupLoader.DEBUG) {
 				System.out.println("Transfering player to main world temporarily to set nametags...");
 			}
 			backs.put(e.getPlayer().getName(), l);
 		}
 	}
 	@EventHandler
 	public void onPlayerDeath(PlayerDeathEvent e) {
 		if (NametagEdit.deathMessageEnabled) {
 			String formattedName = ScoreboardManager.getFormattedName(e.getEntity().getName());
 			if (!formattedName.equals(e.getEntity().getName()))
 				e.setDeathMessage(e.getDeathMessage().replace(formattedName, e.getEntity().getName()));
 		}
 	}
 	private Location getOfflineLoc(String s) {
 		File file = new File(Bukkit.getWorlds().get(0).getName() + "/players/" + s + ".dat");
 		if (!file.exists())
 			return null;
 		try {
 			NBTTagCompound compound = NBTCompressedStreamTools.a(new FileInputStream(file));
 			
 			World w = Bukkit.getWorld(new UUID(compound.getLong("WorldUUIDMost"), compound.getLong("WorldUUIDLeast")));
 			
 			NBTTagList list = compound.getList("Pos");
 			double x = ((NBTTagDouble)list.get(0)).data;
 			double y = ((NBTTagDouble)list.get(1)).data;
 			double z =((NBTTagDouble)list.get(2)).data;
 			list = compound.getList("Rotation");
 
 			float yaw = ((NBTTagFloat)list.get(0)).data;
 			float pitch = ((NBTTagFloat)list.get(1)).data;
 			
 			if (GroupLoader.DEBUG)
 				System.out.println("Loaded location from player file: " + w.getName() + ", " + x + ", " + y + ", " + z + ", " + yaw + ", " + pitch);
 			
 			Location loc = new Location(w, x, y, z, yaw, pitch);
 			
 			return loc;
 			
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	private void setOfflineLoc(String s, Location l) {
 		File file = new File(Bukkit.getWorlds().get(0).getName() + "/players/" + s + ".dat");
 		if (!file.exists())
 			return;
 		try {
 			NBTTagCompound compound = NBTCompressedStreamTools.a(new FileInputStream(file));
 			
 			compound.set("Pos", new NBTTagList());
 			compound.getList("Pos").add(new NBTTagDouble("", l.getX()));
 			compound.getList("Pos").add(new NBTTagDouble("", l.getY()));
 			compound.getList("Pos").add(new NBTTagDouble("", l.getZ()));
 			
 		    compound.set("Rotation", new NBTTagList());
 		    compound.getList("Rotation").add(new NBTTagFloat("", l.getYaw()));
 		    compound.getList("Rotation").add(new NBTTagFloat("", l.getPitch()));
 			
 			compound.setLong("WorldUUIDLeast", l.getWorld().getUID().getLeastSignificantBits());
 			compound.setLong("WorldUUIDMost", l.getWorld().getUID().getMostSignificantBits());
 			
 			NBTCompressedStreamTools.a(compound, new FileOutputStream(file));
 		}
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/*
 	 *
 		if (e.getPlayer().getLocation().getWorld() != Bukkit.getWorlds().get(0)) {
 			tempLocations.put(e.getPlayer().getName(), e.getPlayer().getLocation());
 			e.getPlayer().teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
 		}
 	 */
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		Player senderPlayer = null;
 		if (sender instanceof Player) {
 			senderPlayer = (Player) sender;
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("ne")) {
 			if (senderPlayer != null) {
 				if (!senderPlayer.hasPermission("NametagEdit.use")) {
 					sender.sendMessage("§cYou don't have permission to use this plugin.");
 					return true;
 				}
 			}
 			if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
 				if (senderPlayer != null) {
 					if (!senderPlayer.hasPermission("NametagEdit.reload")) {
 						sender.sendMessage("§cYou don't have permission to reload this plugin.");
 						return true;
 					}
 				}
 				load();
 				sender.sendMessage("§eReloaded group nodes and players.");
 				return true;
 			}
 			if (args.length >= 2) {
 				String operation = args[0];
 				String text = trim(getValue(getText(args)));
 				String target = args[1];
 
 				if (senderPlayer != null) {
 					Player tp = Bukkit.getPlayer(target);
 					if (tp != null && senderPlayer != tp) {
 						if (!senderPlayer.hasPermission("NametagEdit.useall")) {
 							sender.sendMessage("§cYou can only edit your own nametag.");
 							return true;
 						}
 					}
 					else if (!target.equalsIgnoreCase(senderPlayer.getName())) {
 						if (!senderPlayer.hasPermission("NametagEdit.useall")) {
 							sender.sendMessage("§cYou can only edit your own nametag.");
 							return true;
 						}
 					}
 				}
 				
 				if (operation.equalsIgnoreCase("prefix") || operation.equalsIgnoreCase("suffix")) {
 					Player targetPlayer;
 					
 
 					targetPlayer = Bukkit.getPlayer(target);
 					
 					if (text.isEmpty()) {
 						sender.sendMessage("§eNo " + operation.toLowerCase() + " given!");
 						return true;
 					}
 					
 					if (targetPlayer != null) {
 						if (PlayerLoader.getPlayer(targetPlayer.getName()) == null) {
 							ScoreboardManager.clear(targetPlayer.getName());
 						}
 					}
 					
 					String prefix = "";
 					String suffix = "";
 					if (operation.equalsIgnoreCase("prefix"))
 						prefix = formatColors(text);
 					else if (operation.equalsIgnoreCase("suffix"))
 						suffix = formatColors(text);
 					
 					if (targetPlayer != null)
 						ScoreboardManager.update(targetPlayer.getName(), prefix, suffix);
 					if (targetPlayer != null)
 						PlayerLoader.update(targetPlayer.getName(), prefix, suffix);
 					else
 						PlayerLoader.update(target, prefix, suffix);
 					if (targetPlayer != null)
 						sender.sendMessage("§eSet " + targetPlayer.getName() + "\'s " + operation.toLowerCase() + " to \'" + text + "\'.");
 					else
 						sender.sendMessage("§eSet " + target + "\'s " + operation.toLowerCase() + " to \'" + text + "\'.");
 				}
 				else if (operation.equalsIgnoreCase("clear")) {
 					Player targetPlayer;
 					
 
 					targetPlayer = Bukkit.getPlayer(target);
 					if (targetPlayer != null)
 						sender.sendMessage("§eReset " + targetPlayer.getName() + "\'s prefix and suffix.");
 					else
 						sender.sendMessage("§eReset " + target + "\'s prefix and suffix.");
 					if (targetPlayer != null)
 						ScoreboardManager.clear(targetPlayer.getName());
 					if (targetPlayer != null)
 						PlayerLoader.removePlayer(targetPlayer.getName(), null);
 					else
 						PlayerLoader.removePlayer(target, null);
 					
 					if (targetPlayer != null)
 						for (String key : groups.keySet().toArray(new String[groups.keySet().size()])) {
 							if (targetPlayer.hasPermission(key)) {
 								String prefix = groups.get(key).get("prefix");
 								String suffix = groups.get(key).get("suffix");
 								if (prefix != null)
 									prefix = formatColors(prefix);
 								if (suffix != null)
 									suffix = formatColors(suffix);
 								ScoreboardManager.overlap(targetPlayer.getName(), prefix, suffix);
 								
 								break;
 							}
 						}
 				}
 				else {
 					sender.sendMessage("§eUnknown operation \'" + operation + "\', type §a/ne§e for help.");
 					return true;
 				}
 			}
 			else {
 				sender.sendMessage("§e§nNametagEdit command usage:");
 				sender.sendMessage("");
 				sender.sendMessage("§a/ne prefix [player] <text>§e - sets a player's prefix");
 				sender.sendMessage("§a/ne suffix [player] <text>§e - sets a player's suffix");
 				sender.sendMessage("§a/ne clear [player]§e - clears both a player's prefix and suffix.");
 				sender.sendMessage("§a/ne reload§e - reloads the configs");
 			}
 		}
 		return true;
 	}
 	private String getText(String[] args) {
 		String rv = "";
 		for (int t = 2; t < args.length; t++) {
 			if (t == args.length - 1)
 				rv += args[t];
 			else
 				rv += args[t] + " ";
 		}
 		return rv;
 	}
 	private String trim(String input) {
 		if (input.length() > 16) {
 			String temp = input;
 			input = "";
 			for (int t = 0; t < 16; t++)
 				input += temp.charAt(t);
 		}
 		return input;
 	}
 	private static String getValue(String rawValue) {
 		if (!(rawValue.startsWith("\"") && rawValue.endsWith("\""))) {
 			return rawValue;
 		}
 		rawValue = rawValue.trim();
 		String f1 = "";
 		for (int t = 1; t < rawValue.length() - 1; t++) {
 			f1 += rawValue.charAt(t);
 		}
 		return f1;
 	}
 	static String formatColors(String str) {
 		
 		char[] chars = { '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'a', 'b', 'c', 'd', 'e', 'f', 'n', 'r', 'l', 'k', 'o', 'm'};
 		char[] array = str.toCharArray();
 		for (int t = 0; t < array.length - 1; t++) {
 			if (array[t] == '&') {
 				for (char c : chars) {
 					if (c == array[t + 1])
 						array[t] = '§';
 				}
 			}
 		}
 		return new String(array);
 	}
 }
