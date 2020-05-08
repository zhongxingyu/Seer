 /*
  * This file is part of mmoMinecraft (http://code.google.com/p/mmo-minecraft/).
  * 
  * mmoMinecraft is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package mmo.Chat;
 
 import java.util.HashMap;
 import java.util.HashSet;
import mmo.Core.mmo;
 import mmo.Core.mmoChatEvent;
 import mmo.Core.mmoListener;
import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class Channels extends mmoListener {
 
 	public static HashMap<String, String> tells = new HashMap<String, String>();
 
 	@Override
 	public void onMMOChat(mmoChatEvent event) {
 		if (event.hasFilter("Disabled")) {
 			event.getRecipients().clear();
 		}
 		if (event.hasFilter("Say")) {
 			Player from = event.getPlayer();
 			HashSet<Player> recipients = event.getRecipients();
 			for (Player to : new HashSet<Player>(recipients)) {
 				if (from.getWorld() != to.getWorld()
 						  || from.getLocation().distance(to.getLocation()) > 25) {
 					recipients.remove(to);
 				}
 			}
 		}
 		if (event.hasFilter("Yell")) {
 			Player from = event.getPlayer();
 			HashSet<Player> recipients = event.getRecipients();
 			for (Player to : new HashSet<Player>(recipients)) {
 				if (from.getWorld() != to.getWorld()
 						  || from.getLocation().distance(to.getLocation()) > 300) {
 					recipients.remove(to);
 				}
 			}
 		}
 		if (event.hasFilter("World")) {
 			Player from = event.getPlayer();
 			HashSet<Player> recipients = event.getRecipients();
 			for (Player to : new HashSet<Player>(recipients)) {
 				if (from.getWorld() != to.getWorld()) {
 					recipients.remove(to);
 				}
 			}
 		}
 		if (event.hasFilter("Server")) {
 			// Should really refresh the target list...
 		}
 		boolean isTell = event.hasFilter("Tell");
 		if (isTell || event.hasFilter("Reply")) {
 			HashSet<Player> recipients = new HashSet<Player>();
 			Player from = event.getPlayer();
 			Player to = mmoChat.mmo.server.getPlayer(
 					  isTell
 					  ? mmoChat.mmo.firstWord(event.getMessage())
 					  : tells.get(from.getName()));
 			if (isTell) {
 				event.setMessage(mmoChat.mmo.removeFirstWord(event.getMessage()));
 			}
 			if (to != null) {
 				tells.put(to.getName(), from.getName());
 				recipients.add(from);
 				recipients.add(to);
 				event.setFormat(to, event.getFormat().replaceAll("%2\\$s", "%2\\$s&f tells you"));
				event.setFormat(from, event.getFormat().replaceAll("%2\\$s", "You tell " + mmo.getColor(from, to) + to.getName() + ChatColor.WHITE));
 			} else {
 				tells.remove(from.getName());
 			}
 			event.getRecipients().retainAll(recipients);
 		}
 	}
 }
