 package org.theglicks.bukkit.BDchat.events;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.theglicks.bukkit.BDchat.BDchat;
 import org.theglicks.bukkit.BDchat.BDchatPlayer;
 import org.theglicks.bukkit.BDchat.Channel;
 import com.massivecraft.factions.FPlayer;
 import com.massivecraft.factions.FPlayers;
 
 public class ChatListener implements Listener {
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onChat(AsyncPlayerChatEvent playerChat){
 		
 		BDchatPlayer BDplayer = BDchat.BDchatPlayerList.get(playerChat.getPlayer().getName());
 		Channel playerChannel = BDplayer.getChannel();
 		
 		if(playerChannel.getType().equals("global")){
 			
 		} else if(playerChannel.getType().equals("local_range")){
 			Location senderLocation = playerChat.getPlayer().getLocation();
 			for (BDchatPlayer currentBDplayer : BDchat.BDchatPlayerList.values()){
 				if(!currentBDplayer.getWorld().equals(BDplayer.getWorld())){
					playerChat.getRecipients().remove(currentBDplayer.getPlayer());
				} else {
					if(currentBDplayer.getPlayer().getLocation().distance(senderLocation) > playerChannel.getRange()){
 						playerChat.getRecipients().remove(currentBDplayer.getPlayer());
 					}
 				}
 			}
 		} else if(playerChannel.getType().equals("local_world")){
 			World senderWorld = BDplayer.getWorld();
 			for(BDchatPlayer currentBDplayer: BDchat.BDchatPlayerList.values()){
 				if(!currentBDplayer.getWorld().equals(senderWorld)){
 					playerChat.getRecipients().remove(currentBDplayer.getPlayer());
 				}
 			}
 		} else if(playerChannel.getType().equals("faction_only")){
 			FPlayer fsender = FPlayers.i.get(BDplayer.getPlayer());
 			for (FPlayer fplayer: FPlayers.i.get()){
 				if(!fplayer.getFaction().equals(fsender.getFaction())){
 					playerChat.getRecipients().remove(fplayer.getPlayer());
 				}
 			}
 		}		
 		
 		for(BDchatPlayer currentPlayer: BDchat.BDchatPlayerList.values()){
 			if(!currentPlayer.getPlayer().hasPermission("BDchat." + playerChannel.getName() + ".View")){
 				playerChat.getRecipients().remove(currentPlayer.getPlayer());
 			}
 		}
 		
 		playerChat.setFormat(playerChat.getFormat().replace("[BDchat]", playerChannel.getPrefix().replace(".&", "").replace("&", "") + ChatColor.WHITE));
 		playerChat.setMessage(playerChannel.getFormat().replace(".&", "").replace("&", "") + playerChat.getMessage());
 	}
 }
