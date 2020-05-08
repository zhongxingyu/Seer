 package me.cyberkitsune.prefixchat;
 
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 import com.google.common.base.Joiner;
 import com.onarandombox.MultiverseCore.MultiverseCore;
 import com.onarandombox.MultiverseCore.api.MultiverseWorld;
 
 public class KitsuneChatUtils {
 	KitsuneChat plugin;
 	public KitsuneChatUtils(KitsuneChat plugin) {
 		this.plugin = plugin;
 	}
 	
 	public static Set<Player> getNearbyPlayers(int radius, Player target) {
 		Set<Player> nearbyPlayers = new HashSet<Player>();
 		nearbyPlayers.add(target);
 		
 		for(Entity ent : target.getNearbyEntities(radius, radius, radius))
 		{
 			if(ent instanceof Player) {
 				nearbyPlayers.add((Player) ent);
 			}
 		}
 		
 		return nearbyPlayers;
 		
 	}
 	
 	public static Set<Player> getNearbyPlayers(int radius, Player target, AsyncPlayerChatEvent event) {
 		// This function SHOULD be thread-safe.
 		Set<Player> nearbyPlayers = new HashSet<Player>();
 		nearbyPlayers.add(target);
 		
 		for(Entity ent : event.getRecipients())
 		{
 			if (ent.equals(target)) continue;
 			if (!(ent.getWorld().equals(target.getWorld()))) continue;
 			if(target.getLocation().distance(ent.getLocation()) <= radius) {
 				nearbyPlayers.add((Player) ent);
 			}
 		}
 		
 		return nearbyPlayers;
 		
 	}
 	public static String colorizeString(String target) {
 		String colorized = new String();
 		colorized = ChatColor.translateAlternateColorCodes('&', target);
 		
 		return colorized;
 	}
 	
 	public String stripPrefixes(String target) {
 		return (String) target.replaceFirst("\\"+getChannelName(target, true), "");
 	}
 	
 	public String formatChatPrefixes(String target, String formatString, AsyncPlayerChatEvent context) {
 		String output="";
 		if(plugin.vaultEnabled) {
 			output = formatString.replaceAll("\\{sender\\}", plugin.vaultChat.getPlayerPrefix(context.getPlayer())+context.getPlayer().getDisplayName()+plugin.vaultChat.getPlayerSuffix(context.getPlayer()));
 		} else {
 			output = formatString.replaceAll("\\{sender\\}", context.getPlayer().getDisplayName());
 		}
 		if(plugin.multiVerse) {
 			MultiverseCore mvPlug = this.plugin.multiversePlugin;
 			MultiverseWorld mvWorld = mvPlug.getMVWorldManager().getMVWorld(context.getPlayer().getWorld().getName());
 			if(mvWorld == null) {
 				output = output.replaceAll("\\{world\\}", context.getPlayer().getWorld().getName());	
 			} else {
 				output = output.replaceAll("\\{world\\}", mvWorld.getColoredWorldString());
 			}
 		} else {
 			output = output.replaceAll("\\{world\\}", context.getPlayer().getWorld().getName());	
 		}
 		output = output.replaceAll("\\{channel\\}", getChannelName(target, false));
 		output = output.replaceAll("\\{prefix\\}", getChannelName(target, true));
 		output = output.replaceAll("\\{party\\}", (plugin.party.isInAParty(context.getPlayer()) ? plugin.party.getPartyName(context.getPlayer()) : ""));
 		target = target.replaceFirst("\\"+getChannelName(target, true), "");
 		target = target.replaceAll("\\$", "\\\\\\$"); //Friggen dollar signs.
 		if(context.isCancelled()) {
			output = output.replaceAll("\\{message\\}", target);
 		} else {
			output = output.replaceAll("\\{message\\}", "%2\\$s");
 		}
 		output = colorizeString(output);
 		return output;
 	}
     public void chatWatcher(AsyncPlayerChatEvent event) {
 		plugin.mcLog.info(
 				"KitsuneChat "+
 				event.getEventName()+" "+
 				event.getPlayer()+" "+
 				Joiner.on(" ").join(event.getRecipients())+" "+
 				event.getFormat()+" "+
 				event.getMessage()
 				);
 		return;
 	}
 	public String getChannelName(String target, boolean displayPrefix) {
 		if(!displayPrefix) {
 			if(target.startsWith(plugin.getConfig().getString("global.prefix"))) {
 				return "global";
 			} else if(target.startsWith(plugin.getConfig().getString("admin.prefix"))) {
 				return "admin";
 			} else if(target.startsWith(plugin.getConfig().getString("staff.prefix"))) {
 				return "staff";
 			} else if(target.startsWith(plugin.getConfig().getString("world.prefix"))) {
 				return "world";
 			} else if(target.startsWith(plugin.getConfig().getString("party.prefix"))) {
 				return "party";
 			} else if(target.startsWith(plugin.getConfig().getString("local.prefix"))) {
 				return "local";
 			} else {
 				return "local";
 			}
 		} else {
 			if(target.startsWith(plugin.getConfig().getString("global.prefix"))) {
 				return plugin.getConfig().getString("global.prefix");
 			} else if(target.startsWith(plugin.getConfig().getString("admin.prefix"))) {
 				return plugin.getConfig().getString("admin.prefix");
 			} else if(target.startsWith(plugin.getConfig().getString("staff.prefix"))) {
 				return plugin.getConfig().getString("staff.prefix");
 			} else if(target.startsWith(plugin.getConfig().getString("world.prefix"))) {
 				return plugin.getConfig().getString("world.prefix");
 			} else if(target.startsWith(plugin.getConfig().getString("party.prefix"))) {
 				return plugin.getConfig().getString("party.prefix");
 			} else if(target.startsWith(plugin.getConfig().getString("local.prefix"))) {
 				return plugin.getConfig().getString("local.prefix");
 			} else {
 				return plugin.getConfig().getString("local.prefix");
 			}
 		}
 		
 	}
 }
