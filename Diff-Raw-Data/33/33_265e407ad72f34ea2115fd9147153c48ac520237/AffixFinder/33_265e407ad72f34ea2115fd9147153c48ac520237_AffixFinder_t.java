 /*     Copyright (C) 2012  Nodin Chan <nodinchan@live.com>
  * 
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  * 
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.titankingdoms.nodinchan.titanchat.util;
 
 import net.milkbowl.vault.chat.Chat;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
 
 import com.titankingdoms.nodinchan.titanchat.TitanChat;
 
 public final class AffixFinder {
 	
 	private final TitanChat plugin;
 	
	private final PluginManager pm;
	
	private Permission perm;
	private Chat chat;
 	
 	public AffixFinder() {
 		this.plugin = TitanChat.getInstance();
		this.pm = plugin.getServer().getPluginManager();
		
		if (pm.getPlugin("Vault") != null) {
			this.perm = plugin.getServer().getServicesManager().load(Permission.class);
			this.chat = plugin.getServer().getServicesManager().load(Chat.class);
		}
 	}
 	
 	public String getPrefix(Player player) {
 		String prefix = "";
 		
 		if (chat != null) {
 			try {
 				prefix = chat.getPlayerPrefix(player);
 				
 				if (prefix == null || prefix.isEmpty())
 					prefix = chat.getGroupPrefix(player.getWorld(), perm.getPrimaryGroup(player));
 				
 			} catch (Exception e) {}
 		}
 		
 		if (prefix == null || prefix.isEmpty())
 			prefix = plugin.getInfoHandler().getInfo(player, "prefix", "");
 		
 		return prefix;
 	}
 	
 	public String getSuffix(Player player) {
 		String suffix = "";
 		
 		if (chat != null) {
 			try {
 				suffix = chat.getPlayerSuffix(player);
 				
 				if (suffix == null || suffix.isEmpty())
 					suffix = chat.getGroupSuffix(player.getWorld(), perm.getPrimaryGroup(player));
 				
 			} catch (Exception e) {}
 		}
 		
 		if (suffix == null || suffix.isEmpty())
 			suffix = plugin.getInfoHandler().getInfo(player, "suffix", "");
 		
 		return suffix;
 	}
 }
