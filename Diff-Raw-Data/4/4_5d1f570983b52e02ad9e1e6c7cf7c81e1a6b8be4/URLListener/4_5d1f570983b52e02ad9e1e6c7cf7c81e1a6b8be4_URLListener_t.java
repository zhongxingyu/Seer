 package me.blockcat.urlshort;
 
 import java.net.URL;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 
 public class URLListener implements Listener {
 	
 	private URLshort plugin;
 	ExecutorService exec = Executors.newCachedThreadPool();
 	
 	public URLListener(URLshort p) {
 		plugin = p;
 	}
 
 	@EventHandler 
	public void onAsyncPlayerChat (AsyncPlayerChatEvent event) {
 		String msg = event.getMessage();
 		String[] msga = msg.split(" ");
 		String nMsg = "";
 		for (String x : msga) {
 			if(x.contains(".com")||x.contains(".net")||x.contains(".nl")||x.contains(".org")) {
 				try {
 					
 					URL pageURL = new URL("http://www.okij.in/api/s/"+x);
 					Broadcaster bc = new Broadcaster(pageURL, plugin, event.getPlayer());
 					
 					exec.execute(bc);
 					
 					
 					event.setCancelled(true);
 					return;
 				} catch (Exception e) {
 					event.setMessage(msg);
 					return;
 				}
 
 			} else {
 				nMsg = nMsg + x;
 				continue;
 			}
 		}
 		//Player player = event.getPlayer();
 		//plugin.getServer().broadcastMessage(format(player, msg));
 		event.setCancelled(true);
 	}
 	
 	private String format(Player player, String msg) {
 		String prefix;
 		String suffix;
 		String format = plugin.getFormat();
 		
 		if (format.contains("+prefix")) {
 			try {
 			prefix = URLshort.config.getString("Prefix." + URLshort.permission.getPlayerGroups(player)[0]);
 			format = format.replace("+prefix", prefix);
 			} catch(Exception e) {
 				format = format.replace("+prefix","");
 			}
 		}
 		if (format.contains("+suffix")) {
 			try {
 			suffix = URLshort.config.getString("Suffix." + URLshort.permission.getPlayerGroups(player)[0]);
 			format = format.replace("+suffix", suffix);
 			} catch (Exception e) {
 				format.replace("+suffix", "");
 			}
 		}
 		if (format.contains("+name")) {
 			format = format.replace("+name", player.getDisplayName());
 		}
 		if (format.contains("+msg")) {
 			format = format.replace("+msg", msg);
 		}
 		for (ChatColor c : ChatColor.values()) {
 			format = format.replace("&" +c.getChar(), c+"");
 		}
 		
 		
 		return format;
 	}
 
 }
