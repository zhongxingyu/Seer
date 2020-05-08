 package me.corriekay.pppopp3.warp;
 
 import java.util.*;
 
 import me.corriekay.pppopp3.Mane;
 import me.corriekay.pppopp3.events.JoinEvent;
 import me.corriekay.pppopp3.events.QuitEvent;
 import me.corriekay.pppopp3.modules.Equestria;
 import me.corriekay.pppopp3.ponyville.Pony;
 import me.corriekay.pppopp3.ponyville.Ponyville;
 import me.corriekay.pppopp3.utils.PSCmdExe;
 
 import org.bukkit.*;
 import org.bukkit.World.Environment;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 
 public class WarpHandler extends PSCmdExe{
 
 	private static WarpHandler wh;
 	private HashMap<String,WarpList> warpHandler = new HashMap<String,WarpList>();
 	private HashMap<String,QueuedWarp> warpQueues = new HashMap<String,QueuedWarp>();
 	private HashMap<String,String> tpQueues = new HashMap<String,String>();
 	private HashMap<String,Location> homes = new HashMap<String,Location>();
 	private HashMap<String,Location> backs = new HashMap<String,Location>();
 	private final int warpCount;
 	private HashMap<World,Location> spawns = new HashMap<World,Location>();
 
 	//private static WarpHandler wh;
 
 	public WarpHandler() throws Exception{
 		super("WarpHandler", "gw", "pw", "pwlist", "pwdel", "pwset", "gwset", "gwdel", "pwplayer", "tp", "tpa", "tpd", "tphere", "home", "sethome", "back", "spawn", "setspawn", "top");
 		wh = this;
 		FileConfiguration config = getNamedConfig("warps.yml");
 		buildWarpList(config);
 		for(Player player : Bukkit.getOnlinePlayers()) {
 			warpHandler.put(player.getName(), getPlayerWarps(player));
 			loadHome(player);
 			loadBack(player);
 		}
 		methodMap.put(EntityDamageByEntityEvent.class, this.getClass().getMethod("onDamage", EntityDamageEvent.class));
 		Bukkit.getScheduler().scheduleSyncRepeatingTask(Mane.getInstance(), new Runnable() {
 			@Override
 			public void run(){
 				HashSet<String> removeMe = new HashSet<String>();
 				for(String name : warpQueues.keySet()) {
 					QueuedWarp qw = warpQueues.get(name);
 					if(qw.countdown()) {
 						removeMe.add(name);
 					}
 				}
 				for(String name : removeMe) {
 					warpQueues.remove(name);
 				}
 			}
 		}, 0, 20);
 		warpCount = config.getInt("warpCount", 7);
 		for(World w : Equestria.get().getParentWorlds()) {
 			Location spawn = getLocFromList((ArrayList<String>)config.getStringList("spawns." + w.getName()));
 			if(spawn == null) {
 				spawns.put(w, w.getSpawnLocation());
 				config.set("spawns." + w.getName(), getListFromLoc(w.getSpawnLocation()));
 			} else {
 				spawns.put(w, spawn);
 			}
 		}
 		saveNamedConfig("warps.yml", config);
 	}
 
 	private void buildWarpList(FileConfiguration config){
 		HashMap<String,Warp> globalWarpsMap = new HashMap<String,Warp>();
 		for(String warpName : config.getConfigurationSection("warps").getKeys(false)) {
 			globalWarpsMap.put(warpName, new Warp(warpName, getLocFromList(config.getStringList("warps." + warpName))));
 		}
 		WarpList global = new WarpList(globalWarpsMap);
 		warpHandler.put("global", global);
 	}
 
 	private WarpList getPlayerWarps(Player player){
 		Pony pony = Ponyville.getPony(player);
 		HashMap<String,Location> warps = pony.getAllNamedWarps();
 		HashMap<String,Warp> warpsList = new HashMap<String,Warp>();
 		for(String name : warps.keySet()) {
 			warpsList.put(name, new Warp(name, warps.get(name)));
 		}
 		return new WarpList(warpsList);
 	}
 
 	private void loadHome(Player player){
 		Pony pony = Ponyville.getPony(player);
 		Location loc = pony.getHomeWarp();
 		homes.put(player.getName(), loc);
 	}
 
 	private void loadBack(Player player){
 		Pony pony = Ponyville.getPony(player);
 		Location loc = pony.getBackWarp();
 		homes.put(player.getName(), loc);
 	}
 
 	private Location getLocFromList(List<String> list){
 		try {
 			String w;
 			double x, y, z;
 			float p, yaw;
 			w = list.get(0);
 			x = Double.parseDouble(list.get(1));
 			y = Double.parseDouble(list.get(2));
 			z = Double.parseDouble(list.get(3));
 			p = Float.parseFloat(list.get(4));
 			yaw = Float.parseFloat(list.get(5));
 			Location l = new Location(Bukkit.getWorld(w), x, y, z);
 			l.setPitch(p);
 			l.setYaw(yaw);
 			return l;
 		} catch(Exception e) {
 			return null;
 		}
 	}
 
 	private ArrayList<String> getListFromLoc(Location loc){
 		try {
 			ArrayList<String> list = new ArrayList<String>();
 			list.add(loc.getWorld().getName());
 			list.add(loc.getX() + "");
 			list.add(loc.getY() + "");
 			list.add(loc.getZ() + "");
 			list.add(loc.getPitch() + "");
 			list.add(loc.getYaw() + "");
 			return list;
 		} catch(Exception e) {
 			return null;
 		}
 	}
 
 	public boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args){
 
 		if(cmd.getName().equals("warpdebug")) {
 			for(String warpCategory : warpHandler.keySet()) {
 				System.out.print(warpCategory + ":");
 				WarpList wl = warpHandler.get(warpCategory);
 				for(String warp : wl.warps()) {
 					System.out.print(warp);
 				}
 			}
 		}
 		if(cmd.getName().equals("pwlist")) {
 			WarpList global = warpHandler.get("global");
 			if(global.size() > 0) {
 				String gWarps = ChatColor.RED + "Global Warps: ";
 				for(String warp : global.warps()) {
 					gWarps += ChatColor.RED + warp + ChatColor.WHITE + ", ";
 				}
 				gWarps = gWarps.substring(0, gWarps.length() - 4);
 				sendMessage(sender, "Heres a list of global warps!");
 				sender.sendMessage(gWarps);
 			}
 			if(sender instanceof Player) {
 				Player player = (Player)sender;
 				WarpList pWarps = warpHandler.get(player.getName());
 				if(pWarps.size() > 0) {
 					String pWarp = ChatColor.RED + "Private Warps: ";
 					for(String warp : pWarps.warps()) {
 						pWarp += ChatColor.RED + warp + ChatColor.WHITE + ", ";
 					}
 					pWarp = pWarp.substring(0, pWarp.length() - 4);
 					sendMessage(player, "Heres a list of your private warps!");
 					player.sendMessage(pWarp);
 				}
 			}
 			return true;
 		}
 		Player player;
 		if(sender instanceof Player) {
 			player = (Player)sender;
 		} else {
 			sendMessage(sender, notPlayer);
 			return true;
 		}
 		if(cmd.getName().equals("top")) {
 			Location loc = player.getLocation();
 			int y = loc.getWorld().getHighestBlockYAt(loc);
 			loc.setY(y);
 			player.teleport(loc);
 			return true;
 		}
 		if(cmd.getName().equals("spawn")) {
 			World w = player.getWorld();
 			if(args.length > 0) {
 				w = Bukkit.getWorld(args[0]);
 			}
 			w = Equestria.get().getParentWorld(w);
			Location l = spawns.get(w);
			if(l == null) {
				sendMessage(player, "Spawn not found!");
				return true;
			}
 			queueWarp(player, spawns.get(w));
 			return true;
 		}
 		if(cmd.getName().equals("setspawn")) {
 			World w = player.getWorld();
 			if(w.getEnvironment() != Environment.NORMAL) {
 				sendMessage(player, "The spawn is located in the overworld silly!");
 				return true;
 			}
 			Location l = player.getLocation();
 
 			ArrayList<String> loc = getListFromLoc(l);
 			sendMessage(player, "Spawn set!");
 			World parent = Equestria.get().getParentWorld(w);
 			spawns.put(parent, l);
 			FileConfiguration config = getNamedConfig("warps.yml");
 			config.set("spawns." + parent.getName(), loc);
 			saveNamedConfig("warps.yml", config);
 			return true;
 		}
 		if(cmd.getName().equals("tpa")) {
 			String target = tpQueues.get(player.getName());
 			if(target == null) {
 				sendMessage(player, "Uh oh! no teleport request!");
 				return true;
 			}
 			Player targetP;
 			targetP = Bukkit.getPlayerExact(target);
 			if(targetP == null) {
 				sendMessage(player, "Uh oh, no teleport request!");
 				return true;
 			}
 			tpQueues.remove(player.getName());
 			sendMessage(player, "Teleport request accepted!");
 			sendMessage(targetP, "Teleport request accepted!");
 			queueWarp(targetP, player.getLocation());
 			return true;
 		}
 		if(cmd.getName().equals("tpd")) {
 			String requester = tpQueues.get(player.getName());
 			if(requester == null) {
 				sendMessage(player, "Uh oh, no teleport request!");
 				return true;
 			}
 			Player targetP = Bukkit.getPlayerExact(requester);
 			if(targetP != null) {
 				sendMessage(targetP, "Teleport request denied from " + player.getDisplayName() + "!");
 			}
 			sendMessage(player, "Teleport request denied!");
 			tpQueues.remove(player.getName());
 			return true;
 		}
 		if(cmd.getName().equals("home")) {
 			Location home = homes.get(player.getName());
 			if(home == null) {
 				sendMessage(player, "Uh oh, you have no home set! Try setting a home with /sethome!");
 				return true;
 			}
 			queueWarp(player, home);
 			return true;
 		}
 		if(cmd.getName().equals("sethome")) {
 			Pony pony = Ponyville.getPony(player);
 			pony.setHomeWarp(player.getLocation());
 			pony.save();
 			homes.put(player.getName(), player.getLocation());
 			sendMessage(player, "Woo hoo! home set! Shall we throw a housewarming party?");
 			return true;
 		}
 		if(cmd.getName().equals("back")) {
 			Location back = backs.get(player.getName());
 			if(back != null) {
 				queueWarp(player, back);
 				return true;
 			} else {
 				sendMessage(player, "Hey, silly! You havnt warped anywhere yet!");
 				return true;
 			}
 		}
 		if(cmd.getName().equals("pwplayer")) {
 			if(args.length < 2) {
 				sendMessage(player, "Heres a list of pwplayer commands!");
 				sendMessage(player, "/pwplayer <player> list - This lists their warps!");
 				sendMessage(player, "/pwplayer <player> <warpname> -  This teleports to one of their warps!");
 				sendMessage(player, "/pwplayer <player> offline - This teleports to the location they last logged off at!");
 				sendMessage(player, "/pwplayer <player> home -  This teleports to their home location!");
 				sendMessage(player, "/pwplayer <player> back -  This teleports to their back location!");
 				return true;
 			}
 			OfflinePlayer target = getOnlineOfflinePlayer(args[0], player);
 			if(target == null) {
 				return true;
 			}
 			Pony pony = Ponyville.getOfflinePony(target.getName());
 			if(args[1].equals("list")) {
 				ArrayList<String> warps = new ArrayList<String>();
 				warps.addAll(pony.getAllNamedWarps().keySet());
 				Collections.sort(warps);
 				if(warps.size() < 1) {
 					sendMessage(player, "That player doesnt have any warps... YET!");
 					return true;
 				}
 				String warpstring = "";
 				for(String warp : warps) {
 					warpstring += ChatColor.LIGHT_PURPLE + warp + ChatColor.WHITE + ", ";
 				}
 				warpstring = warpstring.substring(0, warpstring.length() - 4);
 				sendMessage(player, "Heres a list of this players warps!: " + warpstring);
 				return true;
 			} else if(args[1].equals("offline")) {
 				Location loc = pony.getOfflineWarp();
 				if(loc == null) {
 					sendMessage(player, "Uh oh, that warp isnt set!");
 					return true;
 				}
 				player.teleport(loc);
 				return true;
 			} else if(args[1].equals("home")) {
 				Location loc = pony.getHomeWarp();
 				if(loc == null) {
 					sendMessage(player, "Uh oh, that warp isnt set!");
 					return true;
 				}
 				player.teleport(loc);
 				return true;
 			} else if(args[1].equals("back")) {
 				Location loc = pony.getBackWarp();
 				if(loc == null) {
 					sendMessage(player, "Uh oh, that warp isnt set!");
 					return true;
 				}
 				player.teleport(loc);
 				return true;
 			} else {
 				Location loc = pony.getNamedWarp(args[1]);
 				if(loc == null) {
 					sendMessage(player, "Uh oh, that warp isnt set!");
 					return true;
 				}
 				player.teleport(loc);
 				return true;
 			}
 		}
 		if(args.length < 1) {
 			sendMessage(player, notEnoughArgs);
 			return true;
 		}
 		if(cmd.getName().equals("gw")) {
 			WarpList global = warpHandler.get("global");
 			Location loc = global.getWarp(args[0]);
 			if(loc == null) {
 				sendMessage(player, "Uh oh, I couldnt find that warp!");
 				return true;
 			}
 			queueWarp(player, loc);
 			return true;
 		}
 		if(cmd.getName().equals("pw")) {
 			WarpList playerWarps = warpHandler.get(player.getName());
 			Location loc = playerWarps.getWarp(args[0]);
 			if(loc == null) {
 				sendMessage(player, "Uh oh, I couldnt find that warp!");
 				return true;
 			}
 			queueWarp(player, loc);
 			return true;
 		}
 		if(cmd.getName().equals("pwdel")) {
 			args[0] = args[0].toLowerCase();
 			Location w = warpHandler.get(player.getName()).getWarp(args[0]);
 			if(w == null || args[0].equalsIgnoreCase("other")) {
 				sendMessage(player, "Uh oh, I couldnt find that warp!");
 				return true;
 			}
 			Pony pony = Ponyville.getPony(player);
 			pony.removeNamedWarp(args[0]);
 			pony.save();
 			sendMessage(player, "Warp deleted!");
 			warpHandler.put(player.getName(), getPlayerWarps(player));
 			return true;
 		}
 		if(cmd.getName().equals("pwset")) {
 			args[0] = args[0].toLowerCase();
 			if(args[0].equals("other")) {
 				sendMessage(player, "Uh oh, sorry! thats a reserved name! please choose another warp name!");
 				return true;
 			}
 			Pony pony = Ponyville.getPony(player);
 			int count = 1;
 			for(String warp : pony.getAllNamedWarps().keySet()) {
 				if(!warp.equals(args[0])) {
 					count++;
 				}
 			}
 			if(count > warpCount) {
 				sendMessage(player, "You have too many warps! You need to delete one, or overwrite an existing one to set another warp!");
 				return true;
 			}
 			pony.setNamedWarp(args[0], player.getLocation());
 			pony.save();
 			sendMessage(player, "Wormhole opened! Warp " + args[0] + " set!");
 			warpHandler.put(player.getName(), getPlayerWarps(player));
 			return true;
 		}
 		if(cmd.getName().equals("gwset")) {
 			String name = args[0].toLowerCase();
 			FileConfiguration config = getNamedConfig("warps.yml");
 			config.set("warps." + name, getListFromLoc(player.getLocation()));
 			saveNamedConfig("warps.yml", config);
 			buildWarpList(config);
 			for(Player p : Bukkit.getOnlinePlayers()) {
 				sendMessage(p, "Wormhole opened! New global warp set: " + name + "!");
 			}
 			return true;
 		}
 		if(cmd.getName().equals("gwdel")) {
 			String name = args[0].toLowerCase();
 			WarpList global = warpHandler.get("global");
 			Location loc = global.getWarp(name);
 			if(loc == null) {
 				sendMessage(player, "Uh oh! I couldnt find that warp!");
 				return true;
 			}
 			FileConfiguration config = getNamedConfig("warps.yml");
 			config.set("warps." + name, null);
 			saveNamedConfig("warps.yml", config);
 			buildWarpList(config);
 			for(Player p : Bukkit.getOnlinePlayers()) {
 				sendMessage(p, "Wormhole closed! Global warp " + name + " deleted!");
 			}
 			return true;
 		}
 		if(cmd.getName().equals("tp")) {
 			Player target = getOnlinePlayer(args[0], player);
 			if(target == null) {
 				return true;
 			}
 			if(player.hasPermission("pppopp3.tpbypass")) {
 				player.teleport(target);
 				return true;
 			}
 			sendMessage(target, player.getDisplayName() + ChatColor.LIGHT_PURPLE + " has sent you a teleport request! type /tpa to accept, or type /tpd to deny it!");
 			sendMessage(player, "Yay! you've sent a teleport request to " + target.getDisplayName());
 			tpQueues.put(target.getName(), player.getName());
 			return true;
 		}
 		if(cmd.getName().equals("tphere")) {
 			Player target = getOnlinePlayer(args[0], player);
 			if(target == null) {
 				return true;
 			}
 			target.teleport(player);
 			return true;
 		}
 		return true;
 	}
 
 	private void queueWarp(Player player, Location loc){
 		if(player.hasPermission("pppopp3.tpbypass")) {
 			player.teleport(loc);
 			sendMessage(player, "Using the express admin wormhole!");
 			return;
 		}
 		try {
 			warpQueues.put(player.getName(), new QueuedWarp(player.getName(), loc, 7));
 		} catch(Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		sendMessage(player, "Okay! setting up the wormhole... This process is delicate...");
 	}
 
 	@EventHandler
 	public void onJoin(JoinEvent event){
 		if(event.isJoining()) {
 			Player player = event.getPlayer();
 			if(!player.hasPlayedBefore()) {
 				player.teleport(spawns.get(Bukkit.getWorld("world")));
 			}
 			warpHandler.put(player.getName(), getPlayerWarps(player));
 			loadBack(player);
 			loadHome(player);
 		}
 	}
 
 	@EventHandler
 	public void onQuit(QuitEvent event){
 		Player player = event.getPlayer();
 		if(event.isQuitting()) {
 			warpHandler.remove(player.getName());
 			backs.remove(player.getName());
 			homes.remove(player.getName());
 		}
 		tpQueues.remove(player.getName());
 	}
 
 	@EventHandler (priority = EventPriority.MONITOR)
 	public void onDamage(EntityDamageEvent event){
 		if(event.getEntity() instanceof Player) {
 			Player player = (Player)event.getEntity();
 			if(warpQueues.containsKey(player.getName())) {
 				warpQueues.remove(player.getName());
 				sendMessage(player, "Oh no! Catastrophic failure! Warp aborted to avoid black holes!");
 			}
 			if(tpQueues.containsKey(player.getName())) {
 				tpQueues.remove(player.getName());
 				sendMessage(player, "Oh no! Catastrophic failure! Player teleport aborted to avoid black holes!");
 			}
 		}
 	}
 
 	@EventHandler
 	public void teleport(PlayerTeleportEvent event){
 		if(event.getCause() == TeleportCause.PLUGIN) {
 			event.getTo().getChunk().load();
 			Player player = event.getPlayer();
 			Pony pony = Ponyville.getPony(player);
 			Location back = event.getFrom();
 			pony.setBackWarp(back);
 			pony.save();
 			backs.put(player.getName(), back);
 		}
 	}
 
 	@EventHandler
 	public void onRespawn(PlayerRespawnEvent event){
 		Player player = event.getPlayer();
 		Location loc = homes.get(player.getName());
 		if(loc == null) {
 			Location spawn = spawns.get(Equestria.get().getParentWorld(player.getLocation().getWorld()));
 			event.setRespawnLocation(spawn);
 			return;
 		} else {
 			World overworld, parentOverworld;
 			overworld = Equestria.get().getParentWorld(loc.getWorld());
 			parentOverworld = Equestria.get().getParentWorld(player.getLocation().getWorld());
 			if(overworld != parentOverworld) {
 				event.setRespawnLocation(spawns.get(parentOverworld));
 			} else {
 				event.setRespawnLocation(loc);
 			}
 			return;
 		}
 
 	}
 
 	public static Location getWorldSpawn(){
 		return wh.spawns.get(Bukkit.getWorld("world"));
 	}
 
 	@EventHandler
 	public void onDeath(PlayerDeathEvent event){
 		Player player = event.getEntity();
 		Location loc = player.getLocation();
 		sendMessage(player, "Oh gosh! Are you okay? ..Shoot, silly me, of course youre not okay... Well, for future reference, heres your location: x: " + loc.getBlockX() + " y: " + loc.getBlockY() + " z: " + loc.getBlockZ() + "! Dont lose your stuff!!");
 	}
 }
