 package com.titankingdoms.nodinchan.titanchat.permission;
 
 import java.util.EnumSet;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.PluginManager;
 
 import com.titankingdoms.nodinchan.titanchat.TitanChat;
 import com.titankingdoms.nodinchan.titanchat.channel.Channel;
 
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
 
 public final class DefaultPermissions {
 	
 	private final TitanChat plugin;
 	
 	protected static final PermissionDefault ALL = PermissionDefault.TRUE;
 	protected static final PermissionDefault NONE = PermissionDefault.FALSE;
 	protected static final PermissionDefault OP = PermissionDefault.OP;
 	protected static final PermissionDefault PLAYER = PermissionDefault.NOT_OP;
 	
 	public DefaultPermissions() {
 		this.plugin = TitanChat.getInstance();
 	}
 	
 	public void load() {
 		PluginManager pm = plugin.getServer().getPluginManager();
 		
 		Permission staff = new Permission("TitanChat.staff", "Grants all permissions");
 		pm.addPermission(staff);
 		
 		pm.addPermission(new Permission("TitanChat.bypass.*", "Grants bypasses to channels"));
 		pm.getPermission("TitanChat.bypass.*").addParent(staff, true);
 		
 		pm.addPermission(new Permission("TitanChat.update", "Notified about updates"));
 		pm.getPermission("TitanChat.update").addParent(staff, true);
 		
 		pm.addPermission(new Permission("TitanChat.create", "Grants permission to channel creation"));
 		pm.addPermission(new Permission("TitanChat.delete", "Grants permission to channel deletion"));
 		
 		pm.getPermission("TitanChat.create").addParent(staff, true);
 		pm.getPermission("TitanChat.delete").addParent(staff, true);
 		
 		pm.addPermission(new Permission("TitanChat.broadcast", "Grants permission to the broadcast command"));
 		pm.addPermission(new Permission("TitanChat.whisper", "Grants permission to the whisper command"));
		pm.addPermission(new Permission("TitanChat.emote", "Grants permission to the traditional emote command"));
 		pm.addPermission(new Permission("TitanChat.emote.*", "Grants permission to emote in all channels"));
 		
 		pm.getPermission("TitanChat.broadcast").addParent(staff, true);
 		pm.getPermission("TitanChat.whisper").addParent(staff, true);
		pm.getPermission("TitanChat.emote").addParent(staff, true);
 		pm.getPermission("TitanChat.emote.*").addParent(staff, true);
 		
 		pm.addPermission(new Permission("TitanChat.force", "Grants permission to force channel joins"));
 		pm.getPermission("TitanChat.force").addParent(staff, true);
 		
 		pm.addPermission(new Permission("TitanChat.nick", "Grants permission to all display name commands"));
 		pm.addPermission(new Permission("TitanChat.nick.check", "Grants permission to get the username of players"));
 		pm.addPermission(new Permission("TitanChat.nick.change", "Grants permission to change your display name"));
 		pm.addPermission(new Permission("TitanChat.nick.change.other", "Grants permission to change display names"));
 		pm.addPermission(new Permission("TitanChat.nick.reset", "Grants permission to reset your display name"));
 		pm.addPermission(new Permission("TitanChat.nick.reset.other", "Grants permission to reset display names"));
 		
 		pm.getPermission("TitanChat.nick").addParent(staff, true);
 		pm.getPermission("TitanChat.nick.check").addParent("TitanChat.nick", true);
 		pm.getPermission("TitanChat.nick.change").addParent("TitanChat.nick.change.other", true);
 		pm.getPermission("TitanChat.nick.change.other").addParent("TitanChat.nick", true);
 		pm.getPermission("TitanChat.nick.reset").addParent("TitanChat.nick.reset.other", true);
 		pm.getPermission("TitanChat.nick.reset.other").addParent("TitanChat.nick", true);
 		
 		Permission join = new Permission("TitanChat.join.*", "Grants permission to join all channels", ALL);
 		pm.addPermission(join);
 		join.addParent(staff, true);
 		
 		Permission speak = new Permission("TitanChat.speak.*", "Grants permission to speak in all channels");
 		pm.addPermission(speak);
 		speak.addParent(staff, true);
 		
 		Permission voice = new Permission("TitanChat.voice.*", "Grants speaking rights at all times in all channels");
 		pm.addPermission(voice);
 		voice.addParent(staff, true);
 		
 		Permission ban = new Permission("TitanChat.ban.*", "Grants permission to ban in all channels");
 		pm.addPermission(ban);
 		ban.addParent(staff, true);
 		
 		Permission kick = new Permission("TitanChat.kick.*", "Grants permission to kick in all channels");
 		pm.addPermission(kick);
 		kick.addParent(staff, true);
 		
 		Permission mute = new Permission("TitanChat.mute.*", "Grants permission to mute in all channels");
 		pm.addPermission(mute);
 		mute.addParent(staff, true);
 		
 		Permission rank = new Permission("TitanChat.rank.*", "Grants permission to promote and demote in all channels");
 		pm.addPermission(rank);
 		rank.addParent(staff, true);
 		
 		Permission format = new Permission("TitanChat.format", "Grants permission to format code usage");
 		pm.addPermission(format);
 		format.addParent(staff, true);
 		
 		Permission codes = new Permission("TitanChat.format.*", "Grants permission to all format codes");
 		pm.addPermission(codes);
 		codes.addParent(staff, true);
 		
 		for (ChatColor colourStyle : EnumSet.allOf(ChatColor.class)) {
 			char colourC = colourStyle.getChar();
 			Permission code = new Permission("TitanChat.format.&" + colourC, "Grants permission to the format code");
 			pm.addPermission(code);
 			code.addParent(codes, true);
 		}
 	}
 	
 	public void load(Channel channel) {
 		PluginManager pm = plugin.getServer().getPluginManager();
 		
 		String name = channel.getName();
 		
 		Permission spawn = new Permission("TitanChat.spawn." + name, "Sets the channel as default spawn");
 		pm.addPermission(spawn);
 		
 		Permission join = new Permission("TitanChat.join." + name, "Grants permission to join " + name);
 		pm.addPermission(join);
 		join.addParent("TitanChat.join.*", true);
 		
 		Permission speak = new Permission("TitanChat.speak." + name, "Grants permission to speak in " + name);
 		pm.addPermission(speak);
 		join.addParent("TitanChat.speak.*", true);
 		
 		Permission voice = new Permission("TitanChat.voice." + name, "Grants speaking rights at all times in " + name);
 		pm.addPermission(voice);
 		voice.addParent("TitanChat.voice.*", true);
 		
 		Permission ban = new Permission("TitanChat.ban." + name, "Grants permission to ban in " + name);
 		pm.addPermission(ban);
 		ban.addParent("TitanChat.ban.*", true);
 		
 		Permission kick = new Permission("TitanChat.kick." + name, "Grants permission to kick in " + name);
 		pm.addPermission(kick);
 		kick.addParent("TitanChat.kick.*", true);
 		
 		Permission mute = new Permission("TitanChat.mute." + name, "Grants permission to mute in " + name);
 		pm.addPermission(mute);
 		mute.addParent("TitanChat.mute.*", true);
 		
 		Permission rank = new Permission("TitanChat.rank." + name, "Grants permission to promote and demote in " + name);
 		pm.addPermission(rank);
 		rank.addParent("TitanChat.rank.*", true);
 		
 		Permission colour = new Permission("TitanChat.colour." + name, "Grants permission to colour usage in " + name);
 		pm.addPermission(colour);
 		colour.addParent("TitanChat.colour.*", true);
 		
 		Permission style = new Permission("TitanChat.style." + name, "Grants permission to style usage in " + name);
 		pm.addPermission(style);
 		style.addParent("TitanChat.style.*", true);
 		
 		Permission emote = new Permission("TitanChat.emote." + name, "Grants permission to emote in " + name);
 		pm.addPermission(emote);
 		emote.addParent("TitanChat.emote.*", true);
 	}
 	
 	public void load(List<Channel> channels) {
 		for (Channel channel : channels)
 			load(channel);
 	}
 }
