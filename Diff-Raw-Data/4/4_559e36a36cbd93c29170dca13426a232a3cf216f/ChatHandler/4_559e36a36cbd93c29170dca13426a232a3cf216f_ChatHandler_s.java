 package net.mayateck.ChatChannels;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class ChatHandler implements Listener{
 	private ChatChannels plugin;
 	public ChatHandler(ChatChannels plugin) {
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 		this.plugin = plugin;
 	}
 	
 	@EventHandler
 	public void onChatted(AsyncPlayerChatEvent e){
 		e.setCancelled(true); // I'm overriding the event and faking a chat. Sorry other plug-ins!
 		Player p = e.getPlayer();
 		String n = p.getName();
 		String m = e.getMessage();
 		Player[] pl = Bukkit.getOnlinePlayers();
 		String c = plugin.getPlayersList().getString("players."+n+".channel");
 		String t = plugin.getConfig().getString("channels."+c+".tag");
 		String tc = plugin.getConfig().getString("channels."+c+".color");
 		if (p.hasPermission("chatchannels.group.admin")){
 			n = "Admin-"+n;
 		} else if (p.hasPermission("chatchannels.group.mod")){
 			n = "Mod-"+n;
 		}
 		String msg=""+tc+t+" "+n+": "+m;
 		if (c.equalsIgnoreCase("admin")){
 			Bukkit.getServer().broadcast(msg, "chatchannels.group.admin");
 		} else if (c.equalsIgnoreCase("mod")){
			Bukkit.getServer().broadcast(msg, "chatchannels.group.admin");
 			Bukkit.getServer().broadcast(msg, "chatchannels.group.mod");
 		} else if (c.equalsIgnoreCase("help")){
 			for (Player cp : pl){
 				if (cp.hasPermission("chatchannels.group.admin") || cp.hasPermission("chatchannels.group.mod") || plugin.getPlayersList().getString("players."+cp.getName()+".channel").equalsIgnoreCase("help")){
 					cp.sendMessage(msg);
 				}
 			}
 		} else if (c.equalsIgnoreCase("zone")){
 			for (Player cp : pl){
				if (cp.getWorld()==p.getWorld()){
 					cp.sendMessage(msg);
 				}
 			}
 		} else if (c.equalsIgnoreCase("local")){
 			for (Player cp : pl){
 				double pX = p.getLocation().getX();
 				double pZ = p.getLocation().getZ();
 				double cpX = cp.getLocation().getX();
 				double cpZ = cp.getLocation().getZ();
 				int dist = plugin.getConfig().getInt("channels.local.distance");
 				boolean xMatch = false;
 				boolean zMatch = false;
 				if (pX-cpX<dist || cpX-pX<dist){
 					xMatch=true;}
 				if (pZ-cpZ<dist || cpZ-pZ<dist){
 					zMatch=true;}
 				if ((cp.getWorld()==p.getWorld() && zMatch==true && xMatch==true) ||cp.hasPermission("chatchannels.group.admin") || cp.hasPermission("chatchannels.group.mod")){
 					cp.sendMessage(msg);
 				}
 			}
 		} else {
 			Bukkit.getServer().broadcastMessage(msg);
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.LOWEST)
 	public void onPlayerJoin(PlayerJoinEvent e){
 		Player p = e.getPlayer();
 		String n = p.getName();
 		String b = plugin.getConfig().getString("channels.broadcast.tag");
 		String c = plugin.getConfig().getString("channels.broadcast.color");
 		if (plugin.getPlayersList().contains("players."+n)){
 			Bukkit.getServer().broadcastMessage(""+c+b+" i"+n+" "+c+"has connected.");
 		} else {
 			plugin.getPlayersList().set("players."+n+".channel", "global");
 			plugin.savePlayersList();
 			Bukkit.getServer().broadcastMessage(""+c+b+" i"+n+" "+c+"has connected for the first time.");
 			Bukkit.getServer().broadcastMessage(""+c+b+" Welcome i"+n+" "+c+"!");
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent e){
 		Player p = e.getPlayer();
 		String n = p.getName();
 		String b = plugin.getConfig().getString("channels.broadcast.tag");
 		String c = plugin.getConfig().getString("channels.broadcast.color");
 		Bukkit.getServer().broadcastMessage(""+c+b+" i"+n+" "+c+"has disconnected.");
 	}
 }
