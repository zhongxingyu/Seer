 package Lihad.BeyondVotes;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BeyondVotes extends JavaPlugin implements Listener {
 	public static FileConfiguration config;
 	protected static String PLUGIN_NAME = "BeyondVotes";
 	protected static String header = "[" + PLUGIN_NAME + "] ";
 	private static Logger log = Logger.getLogger("Minecraft");
 	public static List<Player> PLAYERS = new LinkedList<Player>();
 	public static Map<String,Date> minestatus_map = new HashMap<String,Date>();
 	public static Map<String,Long> tekkit_map = new HashMap<String,Long>();
 	public static Map<String,Long> ftb_map = new HashMap<String,Long>();
 
 	public static List<Location> minestatus_locations = new LinkedList<Location>();
 	public static List<Location> tekkitserverlist_locations = new LinkedList<Location>();
 	public static List<Location> ftbserverlist_locations = new LinkedList<Location>();
 
 	public static List<String> override = new LinkedList<String>();
 	public static Map<String,SignType> selection_enabled = new HashMap<String,SignType>();
 	public static Map<String,SignType> selection_deletion = new HashMap<String,SignType>();
 	public static boolean active = false;
 	public static int lineindex = 0;
 	private int runrotation = 0;
 	public static File whitelist = new File("ftb-white-list.txt");
 
 	SimpleDateFormat parserSDF=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZZZ");
 	public enum SignType{
 		MINESTATUS,TEKKIT,FTB
 	}
 	@Override
 	public void onDisable() {
 		save();
 	}
 	@Override
 	public void onEnable(){
 		config = getConfig();
 		if(config.getList("LocationsMinestatus") != null && config.getList("LocationsTekkitServerList") != null && config.getList("LocationsFTBServerList") != null){
 			for(int i = 0; i<config.getList("LocationsMinestatus").size();i++){
 				minestatus_locations.add(toLocation((String)config.getList("LocationsMinestatus").get(i)));
 			}
 			for(int i = 0; i<config.getList("LocationsTekkitServerList").size();i++){
 				tekkitserverlist_locations.add(toLocation((String)config.getList("LocationsTekkitServerList").get(i)));
 			}
 			for(int i = 0; i<config.getList("LocationsFTBServerList").size();i++){
 				ftbserverlist_locations.add(toLocation((String)config.getList("LocationsFTBServerList").get(i)));
 			}
 		}
 		//Timer related to building the maps off the votifier log
 		this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable(){
 			public void run() {
 				if(active)info("Running vote gatherer off line "+lineindex);
 				Calendar cal = Calendar.getInstance();
 				String line;
 				cal.setTime(new Date(System.currentTimeMillis()));
 				File file = new File("plugins/Votifier/votes.log");
 				try {
 					BufferedReader rd = new BufferedReader(new FileReader(file));
 					int linenumber = 0;
 					while ((line = rd.readLine()) != null) {
 						if(lineindex > linenumber){
 							linenumber++;
 							continue;
 						}else if(line.contains("tekkitserverlist.com")){
 							tekkit_map.put((line.substring(line.indexOf("username:")+9, line.indexOf("address:")-1)).toLowerCase(), Long.parseLong(line.substring(line.indexOf("timeStamp:")+10,line.indexOf("timeStamp:")+20)));
 						}else if(line.contains("Minestatus")){
 							minestatus_map.put((line.substring(line.indexOf("username:")+9, line.indexOf("address:")-1)).toLowerCase(), parserSDF.parse(line.substring(line.indexOf("timeStamp:")+10,line.indexOf("timeStamp:")+35)));
 						}else if(line.contains("ftbserverlist.com")){
 							ftb_map.put((line.substring(line.indexOf("username:")+9, line.indexOf("address:")-1)).toLowerCase(), Long.parseLong(line.substring(line.indexOf("timeStamp:")+10,line.indexOf("timeStamp:")+20)));
 						}
 						linenumber++;
 					}
 					lineindex = linenumber;
 					rd.close();
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (ParseException e) {
 					e.printStackTrace();
 				}
 				
 				//FTB WHITELIST
 				if(whitelist.exists()){
 					if(active)info("Deleting old FTB-Whitelist...");
 					whitelist.delete();
 					if(active)info("Successfully deleted old FTB-Whitelist.");
 				}
 				try {
 					if(active)info("Attempting to create new FTB-Whitelist...");
 					whitelist.createNewFile();
 					if(active)info("Successfully created new FTB-Whitelist.");
 					if(active)info("Building FTB-Whitelist.");
 					for(int i = 0; i<tekkit_map.size();i++){
 						if(System.currentTimeMillis()-(tekkit_map.get(tekkit_map.keySet().toArray()[i].toString())*1000) < 86400000){
 							BufferedWriter output = new BufferedWriter(new FileWriter(whitelist, true));
 							output.newLine();
 							output.write(tekkit_map.keySet().toArray()[i].toString());
 							output.close();
 						}
 					}
 					if(active)info("FTB-Whitelist Build Complete.");
 
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				//
 				if(runrotation == 4)tekkitSpammer();
 				if(runrotation == 8)minestatusSpammer();
 				if(runrotation == 12)ftbSpammer();
 
 				runrotation++;
 				if(runrotation >= 13) runrotation = 0;
 				if(active)info("Ending vote gatherer");
 			}
 		}, 0, 600L);
 
 		getCommand("bv").setExecutor(this);
 		getCommand("bvset").setExecutor(this);
 		getCommand("bvdelete").setExecutor(this);
 		getCommand("bvlookup").setExecutor(this);
 		getCommand("bvoverride").setExecutor(this);
 
 
 		this.getServer().getPluginManager().registerEvents(this, this);
 	}
 
 	@EventHandler
 	public void onPlayerTeleport(PlayerTeleportEvent event){
 		if(event.getTo().getWorld().getName().equalsIgnoreCase("richworld") || event.getTo().getWorld().getName().equalsIgnoreCase("hardcorerichworld")){
 			Player player = event.getPlayer();
 			Calendar calendar = Calendar.getInstance();
 			if(player.isOp())return;
 			else{
 				if((override != null && override.contains(player.getName())) || (!(tekkit_map.containsKey((player).getName().toLowerCase()) && (System.currentTimeMillis()-(tekkit_map.get((player).getName().toLowerCase())*1000) < 86400000)))){
 					event.setTo(getServer().getWorld("tekkit").getSpawnLocation());
 					messageTekkitSpamPlayer(player);
 				}
 				if((override != null && override.contains(player.getName())) || (!(minestatus_map.containsKey((player).getName().toLowerCase()) && (calendar.get(Calendar.DAY_OF_MONTH) == minestatus_map.get((player).getName().toLowerCase()).getDate())))){
 					event.setTo(getServer().getWorld("tekkit").getSpawnLocation());
 					messageMinestatusSpamPlayer(player);
 				}
 				if((override != null && override.contains(player.getName())) || (!(ftb_map.containsKey((player).getName().toLowerCase()) && (System.currentTimeMillis()-(ftb_map.get((player).getName().toLowerCase())*1000) < 86400000)))){
 					event.setTo(getServer().getWorld("tekkit").getSpawnLocation());
 					messageFTBSpamPlayer(player);
 				}
 			}		
 		}
 	}
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event){
 		if(event.getBlock().getWorld().getName().equalsIgnoreCase("richworld") || event.getBlock().getWorld().getName().equalsIgnoreCase("hardcorerichworld")){
 			Player player = event.getPlayer();
 			Calendar calendar = Calendar.getInstance();
 			if(player.isOp())return;
 			if(player == null) return;
 			else{
 				if((override != null && override.contains(player.getName())) || (!(tekkit_map.containsKey((player).getName().toLowerCase()) && (System.currentTimeMillis()-(tekkit_map.get((player).getName().toLowerCase())*1000) < 86400000)))){
 					player.teleport(getServer().getWorld("tekkit").getSpawnLocation());
 					messageTekkitSpamPlayer(player);
 				}
 				if((override != null && override.contains(player.getName())) || (!(minestatus_map.containsKey((player).getName().toLowerCase()) && (calendar.get(Calendar.DAY_OF_MONTH) == minestatus_map.get((player).getName().toLowerCase()).getDate())))){
 					player.teleport(getServer().getWorld("tekkit").getSpawnLocation());
 					messageMinestatusSpamPlayer(player);
 				}
 				if((override != null && override.contains(player.getName())) || (!(ftb_map.containsKey((player).getName().toLowerCase()) && (System.currentTimeMillis()-(ftb_map.get((player).getName().toLowerCase())*1000) < 86400000)))){
 					player.teleport(getServer().getWorld("tekkit").getSpawnLocation());
 					messageFTBSpamPlayer(player);
 				}
 			}		
 		}
 	}
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event){
 		if(event.getClickedBlock() != null && selection_enabled.containsKey(event.getPlayer().getName()) && !minestatus_locations.contains(event.getClickedBlock().getLocation()) && !tekkitserverlist_locations.contains(event.getClickedBlock().getLocation())&& !ftbserverlist_locations.contains(event.getClickedBlock().getLocation())){
 			switch(selection_enabled.get(event.getPlayer().getName())){
 			case MINESTATUS:
 				minestatus_locations.add(event.getClickedBlock().getLocation());
 				break;
 			case TEKKIT:
 				tekkitserverlist_locations.add(event.getClickedBlock().getLocation());
 				break;
 			case FTB:
 				ftbserverlist_locations.add(event.getClickedBlock().getLocation());
 				break;
 			}
 			event.getPlayer().sendMessage("Location Set");
 			selection_enabled.remove(event.getPlayer().getName());
 		}else if(event.getClickedBlock() != null && selection_deletion.containsKey(event.getPlayer().getName()) && minestatus_locations.contains(event.getClickedBlock().getLocation()) && tekkitserverlist_locations.contains(event.getClickedBlock().getLocation())&& ftbserverlist_locations.contains(event.getClickedBlock().getLocation())){
 			switch(selection_deletion.get(event.getPlayer().getName())){
 			case MINESTATUS:
 				minestatus_locations.remove(event.getClickedBlock().getLocation());
 				break;
 			case TEKKIT:
 				tekkitserverlist_locations.remove(event.getClickedBlock().getLocation());
 				break;
 			case FTB:
 				ftbserverlist_locations.remove(event.getClickedBlock().getLocation());
 				break;
 			}			
 			event.getPlayer().sendMessage("Location Removed");
 			selection_deletion.remove(event.getPlayer().getName());
 		}else if(event.getClickedBlock() != null && minestatus_locations.contains(event.getClickedBlock().getLocation())){
 			event.getPlayer().sendMessage(ChatColor.GRAY.toString()+"To receive your prize, just click the link below and vote!");
 			event.getPlayer().sendMessage(ChatColor.GRAY.toString()+"Click ->"+ChatColor.RED.toString()+ChatColor.UNDERLINE.toString()+" http://minestatus.net/2902/vote"+ChatColor.RESET.toString()+ChatColor.GRAY.toString());
 		}else if(event.getClickedBlock() != null && tekkitserverlist_locations.contains(event.getClickedBlock().getLocation())){
 			event.getPlayer().sendMessage(ChatColor.GRAY.toString()+"To receive your prize, just click the link below and vote!");
 			event.getPlayer().sendMessage(ChatColor.GRAY.toString()+"Click ->"+ChatColor.GREEN.toString()+ChatColor.UNDERLINE.toString()+" http://tekkitserverlist.com/server/622/vote"+ChatColor.RESET.toString()+ChatColor.GRAY.toString());
 		}else if(event.getClickedBlock() != null && ftbserverlist_locations.contains(event.getClickedBlock().getLocation())){
 			event.getPlayer().sendMessage(ChatColor.GRAY.toString()+"To receive your prize, just click the link below and vote!");
 			event.getPlayer().sendMessage(ChatColor.GRAY.toString()+"Click ->"+ChatColor.BLUE.toString()+ChatColor.UNDERLINE.toString()+" http://ftbservers.com/server/375/vote"+ChatColor.RESET.toString()+ChatColor.GRAY.toString());
 		}
 	}
 
 	private void tekkitSpammer(){
 		//Method related to the Tekkit Map
 		if(active)info("Running TekkitServerList Vote Spammer....");
 		Player[] players = getPlayers();
 		for(int i = 0;i<players.length;i++){
 			Player player = players[i];
 	        String sIp = (player).getName().toLowerCase();
 	        if(!tekkit_map.containsKey(sIp)){
 	        	if(active)info("Player: "+player.getName()+" with Name ["+sIp+"] was not found.  Spammed");
 	        	messageTekkitSpamPlayer(player);
 	        }else if(tekkit_map.containsKey(sIp) && (System.currentTimeMillis()-(tekkit_map.get(sIp)*1000) > 86400000)){
 	        	if(active)info("Player: "+player.getName()+" with Name ["+sIp+"] was found but hasn't voted in the last 24 hours");
 	        	messageTekkitSpamPlayer(player);
 	        }
 		}
 		if(active)System.out.println("Ending TekkitServerList Vote Spammer....");
 	}
 	private void minestatusSpammer(){
 		//Method related to the Minestatus Map
 		if(active)info("Running Minestatus Vote Spammer....");
 		Player[] players = getPlayers();
 		Calendar calendar = Calendar.getInstance();
 		for(int i = 0;i<players.length;i++){
 			Player player = players[i];
 	        String sIp = (player).getName().toLowerCase();
 	        if(!minestatus_map.containsKey(sIp)){
 	        	if(active)info("Player: "+player.getName()+" with Name ["+sIp+"] was not found.  Spammed");
 	        	messageMinestatusSpamPlayer(player);
 	        }else if(minestatus_map.containsKey(sIp) && (calendar.get(Calendar.DAY_OF_MONTH) != minestatus_map.get(sIp).getDate())){
 	        	if(active)info("Player: "+player.getName()+" with Name ["+sIp+"] was found but hasn't voted today");
 	        	messageMinestatusSpamPlayer(player);
 	        }
 		}
 		if(active)System.out.println("Ending Minestatus Vote Spammer....");
 	}
 	private void ftbSpammer(){
 		//Method related to the Minestatus Map
 		if(active)info("Running FTBSL Vote Spammer....");
 		Player[] players = getPlayers();
 		Calendar calendar = Calendar.getInstance();
 		for(int i = 0;i<players.length;i++){
 			Player player = players[i];
 	        String sIp = (player).getName().toLowerCase();
 	        if(!ftb_map.containsKey(sIp)){
 	        	if(active)info("Player: "+player.getName()+" with Name ["+sIp+"] was not found.  Spammed");
 	        	messageFTBSpamPlayer(player);
 	        }else if(ftb_map.containsKey(sIp) && (calendar.get(Calendar.DAY_OF_MONTH) != minestatus_map.get(sIp).getDate())){
 	        	if(active)info("Player: "+player.getName()+" with Name ["+sIp+"] was found but hasn't voted today");
 	        	messageFTBSpamPlayer(player);
 	        }
 		}
 		if(active)System.out.println("Ending FTBSL Vote Spammer....");
 	}
 	private void messageTekkitSpamPlayer(Player player){
 		player.sendMessage(ChatColor.GRAY.toString()+"Hey! It doesn't look like you've voted on"+ChatColor.DARK_GREEN.toString()+" TekkitServerList!");
 		player.sendMessage(ChatColor.GRAY.toString()+"Click ->"+ChatColor.GREEN.toString()+ChatColor.UNDERLINE.toString()+" http://tekkitserverlist.com/server/622/vote"+ChatColor.RESET.toString()+ChatColor.GRAY.toString()+" and vote!");
		player.sendMessage(ChatColor.GRAY.toString()+"You will recieve "+ChatColor.AQUA.toString()+"5 Diamonds and $500!");
 	}
 	private void messageMinestatusSpamPlayer(Player player){
 		player.sendMessage(ChatColor.GRAY.toString()+"Hey! It doesn't look like you've voted on"+ChatColor.DARK_RED.toString()+" Minestatus"+ChatColor.GRAY.toString()+" today!");
 		player.sendMessage(ChatColor.GRAY.toString()+"Click ->"+ChatColor.RED.toString()+ChatColor.UNDERLINE.toString()+" http://minestatus.net/2902/vote"+ChatColor.RESET.toString()+ChatColor.GRAY.toString()+" and vote!");
		player.sendMessage(ChatColor.GRAY.toString()+"You will recieve "+ChatColor.AQUA.toString()+"5 Diamonds and $500!");
 	}
 	private void messageFTBSpamPlayer(Player player){
 		player.sendMessage(ChatColor.GRAY.toString()+"Hey! It doesn't look like you've voted on"+ChatColor.DARK_BLUE.toString()+" FTBServerList"+ChatColor.GRAY.toString()+" today!");
 		player.sendMessage(ChatColor.GRAY.toString()+"Click ->"+ChatColor.BLUE.toString()+ChatColor.UNDERLINE.toString()+" http://ftbservers.com/server/375/vote"+ChatColor.RESET.toString()+ChatColor.GRAY.toString()+" and vote!");
		player.sendMessage(ChatColor.GRAY.toString()+"You will recieve "+ChatColor.AQUA.toString()+"5 Diamonds and $500!");
 	}
 	private Player[] getPlayers(){
 		return getServer().getOnlinePlayers();
 	}
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if(cmd.getName().equalsIgnoreCase("bv") && args.length == 0 && sender instanceof ConsoleCommandSender) {
 			if(!active){
 				active = true;
 				System.out.println("Debug Active");
 			}else{
 				active = false;
 				System.out.println("Debug Off");
 			}
 			return true;
 		}else if(cmd.getName().equalsIgnoreCase("bvset") && sender instanceof Player && ((Player)sender).isOp()){
 			if(selection_enabled.containsKey(((Player)sender).getName())){
 				selection_enabled.remove(((Player)sender).getName());
 				((Player)sender).sendMessage("Enable Tool Deselected");
 			}else{
 				selection_enabled.put(((Player)sender).getName(),SignType.valueOf(args[0]));
 				selection_deletion.remove(((Player)sender).getName());
 				((Player)sender).sendMessage("Enable Tool Selected.  Please Click a Block");
 			}
 			return true;
 		}else if(cmd.getName().equalsIgnoreCase("bvdelete") && sender instanceof Player && ((Player)sender).isOp()){
 			if(selection_deletion.containsKey(((Player)sender).getName())){
 				selection_deletion.remove(((Player)sender).getName());
 				((Player)sender).sendMessage("Deletion Tool Deselected");
 			}else{
 				selection_deletion.put(((Player)sender).getName(),SignType.valueOf(args[0]));
 				selection_enabled.remove(((Player)sender).getName());
 				((Player)sender).sendMessage("Deletion Tool Selected.  Please Click a Valid Block");
 			}
 			return true;
 		}else if(cmd.getName().equalsIgnoreCase("bvlookup")){
 			if(args.length == 1){
 				if(this.getServer().getPlayer(args[0]) != null){
 					Player player = this.getServer().getPlayer(args[0]);
 					Calendar calendar = Calendar.getInstance();
 					if(!(tekkit_map.containsKey((player).getName().toLowerCase()) && (System.currentTimeMillis()-(tekkit_map.get((player).getName().toLowerCase())*1000) < 86400000))){
 						sender.sendMessage("Player has not voted for TSL this 24h");
 					}
 					if(!(minestatus_map.containsKey((player).getName().toLowerCase()) && (calendar.get(Calendar.DAY_OF_MONTH) == minestatus_map.get((player).getName().toLowerCase()).getDate()))){
 						sender.sendMessage("Player has not voted for Minestatus this day");
 					}
 					if(!(ftb_map.containsKey((player).getName().toLowerCase()) && (System.currentTimeMillis()-(ftb_map.get((player).getName().toLowerCase())*1000) < 86400000))){
 						sender.sendMessage("Player has not voted for FTBSL this day");
 					}
 					
 				}else{
 					sender.sendMessage("Invalid Playername");
 				}
 			}else{
 				sender.sendMessage("Invalid Arguments");
 			}
 			return true;
 		}else if(cmd.getName().equalsIgnoreCase("bvoverride") && (sender instanceof ConsoleCommandSender || ((Player)sender).isOp())){
 			if(args.length == 1){
 				if(this.getServer().getPlayer(args[0]) != null){
 					Player player = this.getServer().getPlayer(args[0]);
 					override.add(player.getName());
 				}else{
 					sender.sendMessage("Invalid Playername");
 				}
 			}else{
 				sender.sendMessage("Invalid Arguments");
 			}
 			return true;
 		}
 		return false;
 	}
 
 	private void save(){
 		List<String> strings = new LinkedList<String>();
 		for(int i = 0;i<minestatus_locations.size();i++){
 			strings.add(toString(minestatus_locations.get(i)));
 		}
 		config.set("LocationsMinestatus", strings);
 		strings = new LinkedList<String>();
 		for(int i = 0;i<tekkitserverlist_locations.size();i++){
 			strings.add(toString(tekkitserverlist_locations.get(i)));
 		}
 		config.set("LocationsTekkitServerList", strings);
 		strings = new LinkedList<String>();
 		for(int i = 0;i<ftbserverlist_locations.size();i++){
 			strings.add(toString(ftbserverlist_locations.get(i)));
 		}
 		config.set("LocationsFTBServerList", strings);
 		this.saveConfig();
 	}
 	private static Location toLocation(String string){
 		String[] array;
 		if(string == null) return null;
 		array = string.split(",");
 		Location location = new Location(org.bukkit.Bukkit.getServer().getWorld(array[3]), Integer.parseInt(array[0]), Integer.parseInt(array[1]), Integer.parseInt(array[2]));
 		return location;
 	}
 	private static String toString(Location location){
 		if(location == null) return null;
 		return (location.getBlockX()+","+location.getBlockY()+","+location.getBlockZ()+","+location.getWorld().getName());
 	}
 	public static void info(String message){ 
 		log.info(header + ChatColor.WHITE + message);
 	}
 	public static void severe(String message){
 		log.severe(header + ChatColor.RED + message);
 	}
 	public static void warning(String message){
 		log.warning(header + ChatColor.YELLOW + message);
 	}
 	public static void log(java.util.logging.Level level, String message){
 		log.log(level, header + message);
 	}
 }
