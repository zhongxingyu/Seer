 package info.aki017.JapanizeChat;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 
 import biscotte.kana.Kana;
 
 public class JapanizeChatPlayerListener implements Listener {
 	public JapanizeChat plugin;
    Pattern pattern = Pattern.compile("[-]|[-K]|\u00a7");
 	public JapanizeChatPlayerListener(JapanizeChat plugin) {
 		this.plugin = plugin;
 		
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 	
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onPlayerChat(PlayerChatEvent event) {
 		if (event.getMessage().startsWith("/")) return;
 			if (plugin.getConfigHandler().getJapanizeMode(event.getPlayer().getName()).equals("On")){
		        Matcher matcher = pattern.matcher(event.getMessage());
 		        if (!matcher.find(0)){
 				
 		        Kana k = new Kana();
 		        k.setLine(event.getMessage());
 		        k.convert();
 	
 		        plugin.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE+"[Japanize]"+ChatColor.GOLD+k.getLine());// plugin.getConfigHandler().getJapanizeMode(event.getPlayer().getName());
 	        }
 	    }
 	}
 }
