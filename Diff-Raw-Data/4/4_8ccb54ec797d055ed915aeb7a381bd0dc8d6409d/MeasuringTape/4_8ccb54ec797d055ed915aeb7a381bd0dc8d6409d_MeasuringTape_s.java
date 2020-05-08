 package de.diddiz.MeasuringTape;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class MeasuringTape extends JavaPlugin
 {
 	private Hashtable<Integer, Session> sessions = new Hashtable<Integer, Session>();
 	private int tapeDelay;
 	private int blocksPerString;
 	private boolean useTargetBlock;
 	private boolean defaultEnabled;
 	private boolean usePermissions;
 
 	public enum MeasuringMode {
 		DISTANCE, VECTORS, AREA, BLOCKS, TRACK, VOLUME;
 	}
 
 	public enum MouseButton	{
 		LEFT, RIGHT;
 	}
 
 	private class Session
 	{
 		public final String user;
 		public Boolean MTEnabled;
 		public ArrayList<Location> pos;
 		public Boolean pos1Set;
 		public Boolean pos2Set;
 		public MeasuringMode mode;
 		public long lastTape;
 
 		public Session (Player player) {
 			user = player.getName();
 			lastTape = 0;
 			mode = MeasuringMode.DISTANCE;
 			MTEnabled = defaultEnabled;
 			ResetPos();
 		}
 
 		public void ResetPos() {
 			pos = new ArrayList<Location>();
 			this.pos.add(null);
 			this.pos.add(null);
 			this.pos1Set = false;
 			this.pos2Set = false;
 		}
 
 		@Override
 		public int hashCode() {
 			return user.hashCode();
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (!user.equalsIgnoreCase(((Session)obj).user))
 				return false;
 			return true;
 		}
 	}
 
 	@Override
 	public void onEnable() {
 		try	{
 			getConfiguration().load();
 			List<String> keys = getConfiguration().getKeys(null);
 			if (!keys.contains("tapeDelay"))
 				getConfiguration().setProperty("tapeDelay", 15);
 			if (!keys.contains("blocksPerString"))
 				getConfiguration().setProperty("blocksPerString", -1);
 			if (!keys.contains("useTargetBlock"))
 				getConfiguration().setProperty("useTargetBlock", true);
 			if (!keys.contains("defaultEnabled"))
 				getConfiguration().setProperty("defaultEnabled", true);
 			if (!keys.contains("usePermissions"))
 				getConfiguration().setProperty("usePermissions", false);
 			getConfiguration().save();
 			tapeDelay = getConfiguration().getInt("tapeDelay", 15);
 			blocksPerString = getConfiguration().getInt("blocksPerString", -1);
 			defaultEnabled = getConfiguration().getBoolean("defaultEnabled", true);
 			usePermissions = getConfiguration().getBoolean("usePermissions", true);
 			useTargetBlock = getConfiguration().getBoolean("useTargetBlock", true);
 			if (usePermissions)	{
 				if (getServer().getPluginManager().getPlugin("Permissions") != null) 
 					getServer().getLogger().info("[MeasuringTape] Permissions enabled");
 				else {
 					usePermissions = false;
 					getServer().getLogger().warning("[MeasuringTape] Permissions plugin not found. Using default permissions.");
 				}
 			}
 		} catch (Exception e) {
 			getServer().getLogger().log(Level.SEVERE, "[MeasuringTape] Exception while reading config.yml", e);
 			getServer().getPluginManager().disablePlugin(this);
 		}
 		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, new MTPlayerListener(), Event.Priority.Normal, this);
 		getServer().getLogger().info("MeasuringTape v" + getDescription().getVersion() + " by DiddiZ enabled");
 	}
 
 	@Override
 	public void onDisable()	{
 		getServer().getLogger().info("MeasuringTape disabled");
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)	{
 		if (cmd.getName().equalsIgnoreCase("mt")) {
 			if ((sender instanceof Player)) {
 				Player player = (Player)sender;
 				Session session = getSession(player);
 				if (args.length == 0) {
 					player.sendMessage(ChatColor.LIGHT_PURPLE + "MeasuringTape v" + getDescription().getVersion() + " by DiddiZ");
 					player.sendMessage(ChatColor.LIGHT_PURPLE + "Type /mt help for help");
 				} else if (args[0].equalsIgnoreCase("tape")) {
 					if (CheckPermission(player, "measuringtape.tape")) {
 						if (player.getInventory().contains(287)) {
 							player.sendMessage(ChatColor.RED + "You have alredy a string"); 
 							player.sendMessage(ChatColor.LIGHT_PURPLE + "Left click: select pos #1; Right click select pos #2."); 
 						} else {
 							long mins = (System.currentTimeMillis() - session.lastTape) / 60000;
 							if (mins >= tapeDelay) {
 								int free = player.getInventory().firstEmpty();
 								if (free >= 0) {
 									player.getInventory().setItem(free, player.getItemInHand());
 									player.setItemInHand(new ItemStack(287, 1));
 									session.lastTape = System.currentTimeMillis();
 									player.sendMessage(ChatColor.GREEN + "Here is your measuring tape."); 
 									player.sendMessage(ChatColor.LIGHT_PURPLE + "Left click: select pos #1; Right click select pos #2."); 
 								} else
 									player.sendMessage(ChatColor.RED + "You have no empty slot in your inventory"); 
 							} else {
 								player.sendMessage(ChatColor.RED + "You got your last tape " + mins + " minutes ago.");
 								player.sendMessage(ChatColor.RED + "You have to wait " + (tapeDelay - mins) + " minutes.");
 							}
 						}
 					} else
 						player.sendMessage(ChatColor.RED + "You aren't allowed to do this.");
 				} else if (args[0].equalsIgnoreCase("read"))
 					ShowDistance(session);
 				else if (args[0].equalsIgnoreCase("unset")) {
 					session.ResetPos();
 					player.sendMessage(ChatColor.GREEN + "Measuring tape rolled up.");
 				} else if (args[0].equalsIgnoreCase("mode")) {
 					if (args.length != 2)
 						player.sendMessage(ChatColor.RED + "Usage: /mt mode [mode]");
 					else if (args[1].equalsIgnoreCase("distance")) {
 						session.mode = MeasuringMode.DISTANCE;
 						player.sendMessage(ChatColor.GREEN + "Measuring mode set to distance.");
 					} else if (args[1].equalsIgnoreCase("vectors")) {
 						session.mode = MeasuringMode.VECTORS;
 						player.sendMessage(ChatColor.GREEN + "Measuring mode set to vectors.");
 					} else if (args[1].equalsIgnoreCase("area")) {
 						session.mode = MeasuringMode.AREA;
 						player.sendMessage(ChatColor.GREEN + "Measuring mode set to area.");
 					} else if (args[1].equalsIgnoreCase("blocks"))	{
 						session.mode = MeasuringMode.BLOCKS;
 						player.sendMessage(ChatColor.GREEN + "Measuring mode set to blocks.");
 					} else if (args[1].equalsIgnoreCase("track")) {
 						session.mode = MeasuringMode.TRACK;
 						session.ResetPos();
 						player.sendMessage(ChatColor.GREEN + "Measuring mode set to track.");
 					} else if (args[1].equalsIgnoreCase("volume"))	{
 						session.mode = MeasuringMode.VOLUME;
 						player.sendMessage(ChatColor.GREEN + "Measuring mode set to volume.");
 					} else
 						player.sendMessage(ChatColor.RED + "Wrong argument. Type /mt help for help.");
 				} else if (args[0].equalsIgnoreCase("tp")) {
 					if (CheckPermission(player, "measuringtape.tp")) {
 						if (session.mode == MeasuringMode.AREA && session.pos1Set && session.pos1Set) {
 							Location diff = getDiff(session.pos.get(0),session.pos.get(1));
 							if ((diff.getBlockX()) % 2 == 0 && (diff.getBlockZ()) % 2 == 0)	{
 								double x = session.pos.get(0).getBlockX() + diff.getBlockX() / 2 + 0.5;
 								double z = session.pos.get(0).getBlockZ() + (diff.getBlockZ()) / 2 + 0.5;
 								player.teleport(new Location(player.getWorld(), x , player.getWorld().getHighestBlockYAt((int)x, (int)z), z, player.getLocation().getYaw(), player.getLocation().getPitch()));
 								player.sendMessage(ChatColor.GREEN + "Teleported to center.");
 							} else 
 								player.sendMessage(ChatColor.RED + "Area has not a single block as center.");
 						} else 
 							player.sendMessage(ChatColor.RED + "Both positions must be set and must be in area mode.");
 					} else
 						player.sendMessage(ChatColor.RED + "You aren't allowed to do this.");
 				} else if (args[0].equalsIgnoreCase("help")) {
 					player.sendMessage(ChatColor.LIGHT_PURPLE + "MeasuringTape Commands:");
 					if (CheckPermission(player, "measuringtape.tape"))
 						player.sendMessage(ChatColor.LIGHT_PURPLE + "/mt tape //Gives a measuring tape");
 					player.sendMessage(ChatColor.LIGHT_PURPLE + "/mt read //Displays the distance again");
 					player.sendMessage(ChatColor.LIGHT_PURPLE + "/mt unset //Unsets both markers");
 					player.sendMessage(ChatColor.LIGHT_PURPLE + "/mt mode [mode] //Toggles measuring mode");
 					player.sendMessage(ChatColor.LIGHT_PURPLE + "/mt modehelp //Displays help to the modes");
 					if (CheckPermission(player, "measuringtape.tp"))
 						player.sendMessage(ChatColor.LIGHT_PURPLE + "/mt tp //Teleports to the center of the selected area");
 					if (session.MTEnabled)
 						player.sendMessage(ChatColor.LIGHT_PURPLE + "/mt disable //Disables string attaching");
 					else
 						player.sendMessage(ChatColor.LIGHT_PURPLE + "/mt enable //Enables string attaching");
 				} else if (args[0].equalsIgnoreCase("modehelp")) {
 					player.sendMessage(ChatColor.LIGHT_PURPLE + "MeasuringTape Modes:");
 					player.sendMessage(ChatColor.LIGHT_PURPLE + "distance - direct distance between both positions");
 					player.sendMessage(ChatColor.LIGHT_PURPLE + "vectors -xyz-vectors between the positions");
 					player.sendMessage(ChatColor.LIGHT_PURPLE + "area - area between the points");
 					player.sendMessage(ChatColor.LIGHT_PURPLE + "blocks - amount of blocks in x, y and z axis between positions");
 					player.sendMessage(ChatColor.LIGHT_PURPLE + "track - distance with multiple points");
 					player.sendMessage(ChatColor.LIGHT_PURPLE + "volume - volume of a cuboid");
 				} else if (args[0].equalsIgnoreCase("enable")) {
 					session.MTEnabled = true;
 					player.sendMessage(ChatColor.LIGHT_PURPLE + "Measuring tape enabled.");
 				} else if (args[0].equalsIgnoreCase("disable")) {
 					session.MTEnabled = false;
 					player.sendMessage(ChatColor.LIGHT_PURPLE + "Measuring tape disabled.");
 				} else
 					player.sendMessage(ChatColor.RED + "Wrong argument. Type /mt help for help.");
 			} else
 				sender.sendMessage("You aren't a player.");
 			return true;
 		} else
 			return false;
 	}
 
 	private class MTPlayerListener extends PlayerListener
 	{ 
 		public void onPlayerInteract(PlayerInteractEvent event) {
			if (event.getItem().getTypeId() == 287 && CheckPermission(event.getPlayer(), "measuringtape.measure")) {
 				if (event.getAction() == Action.LEFT_CLICK_BLOCK)
 					Attach(event.getPlayer(), event.getClickedBlock(), MouseButton.LEFT);
 				else if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
 					Attach(event.getPlayer(), event.getClickedBlock(), MouseButton.RIGHT);
 				else if (event.getAction() == Action.LEFT_CLICK_AIR && useTargetBlock)
 					Attach(event.getPlayer(), event.getPlayer().getTargetBlock(null, Integer.MAX_VALUE), MouseButton.LEFT);
 				else if (event.getAction() == Action.RIGHT_CLICK_AIR && useTargetBlock)
 					Attach(event.getPlayer(), event.getPlayer().getTargetBlock(null, Integer.MAX_VALUE), MouseButton.RIGHT);
 			}
 		}
 	}
 
 	private boolean CheckPermission(Player player, String permission) {
 		if (usePermissions) 
 			return Permissions.Security.permission(player, permission);
 		 else {
 			if (permission.equals("measuringtape.measure"))
 				return true;
 			else if (permission.equals("measuringtape.tape"))
 				return true;
 			else if (permission.equals("measuringtape.tp"))
 				return player.isOp();
 		}
 		return false;
 	}
 
 	private void Attach(Player player, Block block, MouseButton mousebutton) {
 		Session session = getSession(player);
 		if (session.MTEnabled) {
 			Location loc = new Location(block.getWorld(), block.getX(), block.getY(), block.getZ());
 			if (session.mode == MeasuringMode.DISTANCE || session.mode == MeasuringMode.VECTORS || session.mode == MeasuringMode.AREA || session.mode == MeasuringMode.BLOCKS || session.mode == MeasuringMode.VOLUME) {
 				if (mousebutton == MouseButton.LEFT) {
 					session.pos.set(0, loc);
 					if (!session.pos1Set) {
 						session.pos1Set = true;
 						player.sendMessage(ChatColor.GREEN + "Measuring Tape attached to first position");
 					}
 				} else {
 					session.pos.set(1, loc);
 					if (!session.pos2Set) {
 						session.pos2Set = true;
 						player.sendMessage(ChatColor.GREEN + "Measuring Tape attached to second position");
 					}
 				}
 			} else if (session.mode == MeasuringMode.TRACK) {
 				if (!session.pos1Set) {
 					session.pos.set(0, loc);
 					session.pos1Set = true;
 					player.sendMessage(ChatColor.GREEN + "Measuring Tape attached to first position");
 				} else if (!session.pos2Set) {
 					session.pos.set(1, loc);
 					session.pos2Set = true;
 					player.sendMessage(ChatColor.GREEN + "Measuring Tape attached to second position");
 				} else
 					session.pos.add(loc);
 			}
 			if (session.pos1Set && session.pos2Set)
 				ShowDistance(session);
 		}
 	}
 
 	private void ShowDistance(Session session) {
 		Player player = getServer().getPlayer(session.user);
 		if (session.pos1Set && session.pos2Set) {
 			Location diff = getDiff(session.pos.get(0),session.pos.get(1));
 			int x = Math.abs(diff.getBlockX()), y = Math.abs(diff.getBlockY()), z = Math.abs(diff.getBlockZ()); double distance = 0;
 			int stringsAvailable = CountItem(player.getInventory(), 287);
 			int stringsNeeded = 0;
 			String msg = "";
 			switch(session.mode) {
 				case DISTANCE:
 					distance = Math.round(Math.sqrt(x*x + y*y + z*z) * 10) / (double)10;
 					stringsNeeded = (int)Math.ceil(distance / blocksPerString);
 					msg = "Distance: " + distance + "m";
 					break;
 				case VECTORS:
 					stringsNeeded = (int)Math.ceil((x + y + z) / blocksPerString);
 					msg = "Vectors: X" + x + " Y" + z + " Z" + y;
 					break;
 				case AREA:
 					x += 1; z += 1;
 					stringsNeeded = (int)Math.ceil((x + z - 1) / blocksPerString);
 					msg = "Area: " + x + "x" + z + " (" + x*z + " m2)";
 					break;
 				case BLOCKS:
 					x += y + z + 1;
 					stringsNeeded = (int)Math.ceil(x / blocksPerString);
 					msg = "Blocks: " + x;
 					break;
 				case TRACK:
 					for (int i = 1; i < session.pos.size(); i++) {
 						diff = getDiff(session.pos.get(i - 1), session.pos.get(i));
 						distance += Math.sqrt(diff.getBlockX()*diff.getBlockX() + diff.getBlockY()*diff.getBlockY() + diff.getBlockZ()*diff.getBlockZ());
 					}
 					distance = Math.round(distance * 10) / (double)10;
 					stringsNeeded = (int)Math.ceil(distance / blocksPerString);
 					msg = "Track: " + distance + "m";
 					break;
 				case VOLUME:
 					x += 1; y += 1; z += 1;
 					stringsNeeded = (int)Math.ceil((x + y + z - 2) / blocksPerString);
 					msg = "Volume: " + x + "x" + y + "x" + z + " (" +  x*y*z + " m3)";
 					break;
 			}
 			if (stringsNeeded <= stringsAvailable || blocksPerString == -1)
 				player.sendMessage(msg);
 			else
 				player.sendMessage(ChatColor.RED + "You have not enought tape. You need " + (stringsNeeded - stringsAvailable) + " more");
 		} else
 			player.sendMessage(ChatColor.GREEN + "Both positions must be set");
 	}
 
 	private Session getSession(Player player) {
 		Session session = sessions.get(player.getName().hashCode());
 		if (session == null) {
 			session = new Session(player);
 			sessions.put(player.getName().hashCode(), session);
 		}
 		return session;
 	}
 
 	private Location getDiff(Location loc1, Location loc2) {
 		return new Location(loc1.getWorld(), loc2.getBlockX() - loc1.getBlockX(), loc2.getBlockY() - loc1.getBlockY(), loc2.getBlockZ() - loc1.getBlockZ());
 	}
 
 	private Integer CountItem(Inventory invent, Integer itemId)	{
 		int found = 0;
 		for (ItemStack item : invent.getContents()) {
 			if (item.getTypeId() == itemId)
 				found += item.getAmount();
 		}
 		return found;
 	}
 }
