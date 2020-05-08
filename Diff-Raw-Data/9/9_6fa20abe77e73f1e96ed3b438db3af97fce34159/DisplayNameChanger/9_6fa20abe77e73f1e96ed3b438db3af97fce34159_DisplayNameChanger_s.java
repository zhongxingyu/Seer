 package com.titankingdoms.nodinchan.titanchat.util.displayname;
 
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 
 import com.titankingdoms.nodinchan.titanchat.TitanChat;
 import com.titankingdoms.nodinchan.titanchat.util.Debugger;
 
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
 
 public final class DisplayNameChanger {
 	
 	private final TitanChat plugin;
 	
 	private static final Debugger db = new Debugger(5);
 	
 	public DisplayNameChanger() {
 		this.plugin = TitanChat.getInstance();
 	}
 	
 	public void apply(Player player) {
 		DisplayName display = plugin.getDatabase().find(DisplayName.class).where().ieq("name", player.getName()).findUnique();
 		
 		if (display == null)
 			return;
 		
 		set(player, display.getDisplayName());
 		db.i("DisplayNameChanger: Display name " + display.getDisplayName() + " found and applied to " + player.getName());
 	}
 	
 	public String getDisplayName(OfflinePlayer player) {
		if (player != null && player.isOnline())
 			return player.getPlayer().getDisplayName();
 		
 		DisplayName display = plugin.getDatabase().find(DisplayName.class).where().ieq("name", player.getName()).findUnique();
 		
 		if (display == null)
 			return player.getName();
 		
 		return display.getDisplayName();
 	}
 	
 	public void save(Player player) {
 		DisplayName display = plugin.getDatabase().find(DisplayName.class).where().ieq("name", player.getName()).findUnique();
 		
 		if (display != null) {
 			if (player.getDisplayName().equals(player.getName())) {
 				plugin.getDatabase().delete(display);
 				return;
 			}
 			
 		} else if (!player.getDisplayName().equals(player.getName())) {
 			display = new DisplayName();
 			display.setName(player.getName());
 			
 		} else { return; }
 		
 		display.setDisplayName(player.getDisplayName());
 		plugin.getDatabase().save(display);
 		db.i("DisplayNameChanger: Display name " + display.getDisplayName() + " saved for " + player.getName());
 	}
 	
 	public void set(Player player, String displayname) {
 		if (displayname.length() > 16)
 			displayname = displayname.substring(0, 16);
 		
 		if (plugin.getConfig().getBoolean("display-name.change-player-list"))
 			player.setPlayerListName(displayname);
 		
 		player.setDisplayName(displayname);
 	}
 	
 	public void unload() {
 		for (Player player : plugin.getServer().getOnlinePlayers())
 			save(player);
 	}
 }
