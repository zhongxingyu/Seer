 package com.martinbrook.TesseractTeams;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 public class TesseractTeams extends JavaPlugin {
 	
 	private HashMap<String, TTeam> teams = new HashMap<String,TTeam>();
 	private HashMap<String, TTeam> players = new HashMap<String,TTeam>();
 	
 	
     @Override
     public void onEnable(){
 		saveDefaultConfig();
 		loadTeams();
         getServer().getPluginManager().registerEvents(new TeamsListener(this), this);
 		
     }
  
     
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
     	String c = cmd.getName().toLowerCase();
     	String response = null;
 
 		if (c.equals("reloadteams") && sender.isOp()) {
 			response = cReloadteams();
 		} else if (c.equals("join") && sender instanceof Player) {
 			response = cJoin((Player) sender);
 		} else if (c.equals("leave") && sender instanceof Player) {
 			response = cLeave((Player) sender);
 		} else if (c.equals("teams")) {
 			response = cTeams();
 		} else if (c.equals("t") && sender instanceof Player) {
 			response = cT((Player) sender, args);
 		}
 		
 		if (response != null)
 			sender.sendMessage(response);
 		
 		return true;
     }
     
     private String cT(Player sender, String[] args) {
     	TTeam t = getTeam(sender);
     	if (t==null) return ChatColor.RED + "Team chat only works if you are on a team!";
     	
     	String message = "";
     	for (String a : args) {
     		message += a + " ";
     	}
     	message = message.substring(0, message.length()-1);
     	
     	for(String p : getMembers(t)) {
     		Player pl = getServer().getPlayerExact(p);
     		if (pl != null && pl.isOnline()) {
     			pl.sendMessage(t.getPrefix() + ChatColor.RESET + " <" + t.getColor() + sender.getDisplayName() + ChatColor.RESET + ">: " + message);
     		}
     			
     	}
     	
     	return null;
     }
     private String cTeams() {
     	String output="";
     	for (Entry<String,TTeam> e : teams.entrySet()) {
     		TTeam t = e.getValue();
     		output += ChatColor.AQUA + "Team " + t.getColor() + t.getName() + " [" + t.getTag() + "]" + ChatColor.AQUA + ":\n   ";
     		String playerlist = "";
     		
     		for (String p : getMembers(t)) {
    				playerlist += p + ", ";
     		}
     		if (playerlist.length() >= 2) {
     			playerlist=playerlist.substring(0,playerlist.length() - 2);
     		}
     		output += playerlist + "\n";
     	}
     	return output;
     }
     private String cReloadteams() {
    		loadTeams();
    		return ChatColor.GREEN + "Teams reloaded";
     }
     
     private String cJoin(Player p) {
     	TTeam t = assignTeam(p);
     	if (t == null) return ChatColor.RED + "You are already on a team. Leave it first with /leave";
     	getServer().broadcastMessage(ChatColor.YELLOW + p.getDisplayName() + " has joined " + t.getColor() + t.getName());
    	return ChatColor.AQUA + "Welcome to " + t.getColor() + t.getName() + ChatColor.AQUA + "!\n"
    			+ ChatColor.GREEN + "You can send private messages to your teammates using the "
    			+ ChatColor.GOLD + "/t" + ChatColor.GREEN + " command.";
     }
     
     private String cLeave(Player p) {
     	TTeam t = removeTeam(p);
     	if (t != null) {
    		getServer().broadcastMessage(ChatColor.YELLOW + p.getDisplayName() + " has left " + t.getColor() + t.getName());
     		return ChatColor.AQUA + "You are no longer a member of any team.";
     	} else {
     		return ChatColor.RED + "Unable to leave. You are not on a team.";
     	}
     }
     
     public ArrayList<String> getMembers(TTeam t) {
     	ArrayList<String> members = new ArrayList<String>();
     	for (Entry<String, TTeam> f : players.entrySet()) {
 			if (f.getValue().equals(t)) {
 				members.add(f.getKey());
 			}
 		}
     	return members;
     }
     public void loadTeams() {
     	reloadConfig();
     	FileConfiguration cfg = getConfig();
     	teams.clear();
     	players.clear();
     	for (String tag : cfg.getKeys(false)) {
     		ConfigurationSection team = cfg.getConfigurationSection(tag);
     		String name = team.getString("name");
     		ChatColor c;
     		try {
 				c = ChatColor.valueOf(team.getString("color").toUpperCase());
 			} catch (IllegalArgumentException e) {
 				c = ChatColor.WHITE;
 			}
     		
     		TTeam t = new TTeam(tag, name, c);
     		teams.put(tag.toLowerCase(), t);
     		for (String player : team.getStringList("players")) {
     			players.put(player.toLowerCase(), t);
     			setDisplayName(player);
     		}
     	}
     	
     	
     }
     
     public void saveTeams() {
     	FileConfiguration cfg = getConfig();
     	for (TTeam t : teams.values()) {
     		cfg.set(t.getTag()+ ".players", getMembers(t));
     	}
     	saveConfig();
     	
     	
     }
     
     public TTeam getTeam(Player player) { return this.getTeam(player.getName()); }
     public TTeam getTeam(String player) {
     	return players.get(player.toLowerCase());
     }
 
     public TTeam assignTeam(Player player) { return this.assignTeam(player.getName()); }
     public TTeam assignTeam(String player) {
     	if (getTeam(player) != null) return null;
     	Random r = new Random();
     	
     	ArrayList<String> tags = new ArrayList<String>();
     	tags.addAll(teams.keySet());
     	
     	
     	teams.keySet();
     	
     	String tag = tags.get(r.nextInt(tags.size()));
     	TTeam t = teams.get(tag);
     	players.put(player.toLowerCase(), t);
     	setDisplayName(player);
     	saveTeams();
     	return t;
     	
     }
     
     public TTeam removeTeam(Player player) { return this.removeTeam(player.getName()); }
     public TTeam removeTeam(String player) {
     	TTeam t = getTeam(player);
     	if (t != null) {
     		players.remove(player.toLowerCase());
     	}
     	setDisplayName(player);
     	saveTeams();
     	return t;
     }
 
 
     public void setDisplayName(String player) {
     	Player p = getServer().getPlayerExact(player);
     	if (p != null)
     		setDisplayName(p);
     }
 	public void setDisplayName(Player player) {
 		TTeam t = getTeam(player);
 		if (t!=null) {
 			player.setDisplayName(t.getColor() + player.getName());
 			String pln = t.getColor() + player.getName();
 			if (pln.length() > 16) pln= pln.substring(0,16);
 			player.setPlayerListName(pln);
		} else {
			player.setDisplayName(player.getName());
 		}
 	}
 
 }
