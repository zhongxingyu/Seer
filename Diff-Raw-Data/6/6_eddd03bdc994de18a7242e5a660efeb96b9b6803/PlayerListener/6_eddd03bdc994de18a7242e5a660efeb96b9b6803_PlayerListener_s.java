 package au.com.addstar.birthdaygift;
 /*
 * BirthdayGift
 * Copyright (C) 2013 add5tar <copyright at addstar dot com dot au>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
 
 import java.util.Date;
 
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.ChatColor;
 
 import au.com.addstar.birthdaygift.BirthdayGift.*;
 
 public class PlayerListener implements Listener {
 	
 	private BirthdayGift plugin;
 	public PlayerListener(BirthdayGift instance) {
 		plugin = instance;
 	}
 	
	@EventHandler(priority = EventPriority.LOW)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		final Player player = event.getPlayer();
 		final Server server = player.getServer();
 		
 		// Do not do broadcasts/join messages if another plugin silenced the join message (eg. VanishNoPacket)
 		final boolean DoBroadcast;
		if (event.getJoinMessage() == "") {
 			DoBroadcast = false;
 		} else {
 			DoBroadcast = true;
 		}
 		
 		// Ignore anyone without the "use" permission
 		if (!plugin.HasPermission(player, "birthdaygift.use")) { return; }
 		
 		final BirthdayRecord birthday = plugin.getPlayerRecord(player.getName());
 		if (plugin.IsPlayerBirthday(birthday)) {
 			plugin.Log("Today is " + player.getName() + "'s birthday!");
 
 			// Set special join message
 			if ((DoBroadcast) && (plugin.JoinMessage != "")) {
 				String msg = plugin.JoinMessage.replaceAll("<PLAYER>", player.getName());
 				msg = ChatColor.translateAlternateColorCodes('&', msg);
 				event.setJoinMessage(msg);
 			}
 
 			// Delay the broadcast so the player sees it as the last message on their screen
 			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 				@Override
 				public void run() {
 					// Broadcast birthday message (if set, and hasn't already happened today)
 					if (!plugin.ReceivedGiftToday(birthday)) {
 						// Broadcast the announcement
 						if ((DoBroadcast) && (plugin.AnnounceMessage != "")) {
 							if (!plugin.AnnouncedToday(birthday)) {
 								plugin.SetAnnounced(player.getName(), new Date());
 								String msg = plugin.AnnounceMessage.replaceAll("<PLAYER>", player.getName());
 								msg = ChatColor.translateAlternateColorCodes('&', msg);
 								server.broadcastMessage(msg);
 							}
 						}
 
 						// Remind player about how to claim
 						String msg = plugin.ClaimMessage.replaceAll("<PLAYER>", player.getName());
 						msg = ChatColor.translateAlternateColorCodes('&', msg);
 						player.sendMessage(msg);
 					}
 				}
 			}, 20L);
 		}
 	}
 }
