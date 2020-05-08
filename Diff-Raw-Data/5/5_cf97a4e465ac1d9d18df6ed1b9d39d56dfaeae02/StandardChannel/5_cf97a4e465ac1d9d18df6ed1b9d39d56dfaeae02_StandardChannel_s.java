 package com.titankingdoms.nodinchan.titanchat.channel;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.entity.Player;
 
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
 
 /**
  * StandardChannel - Built-in channels
  * 
  * @author NodinChan
  *
  */
 public final class StandardChannel extends Channel {
 	
 	private final Variables variables;
 	
 	private String password;
 	
 	public StandardChannel(String name) {
 		this(name, Type.UNKNOWN);
 	}
 	
 	public StandardChannel(String name, Type type) {
 		super(name, type);
 		this.variables = new Variables(this);
 		this.password = "";
 	}
 	
 	/**
 	 * Check if the password is correct
 	 * 
 	 * @param password The password entered
 	 * 
 	 * @return True if password matches
 	 */
 	public boolean correctPassword(String password) {
 		return this.password.equals(password);
 	}
 	
 	/**
 	 * Gets the password of the channel
 	 * 
 	 * @return The password if set
 	 */
 	public String getPassword() {
 		return password;
 	}
 	
 	/**
 	 * Gets the variables of the channel
 	 * 
 	 * @return The channel variables
 	 */
 	public final Variables getVariables() {
 		return variables;
 	}
 	
 	@Override
 	public void join(Player player) {
 		super.getParticipants().add(player.getName());
 		
 		if (variables.enableJoinMessages() && plugin.enableJoinMessage()) {
 			for (String participant : super.getParticipants()) {
 				if (plugin.getPlayer(participant) != null && !plugin.getPlayer(participant).equals(player))
 					plugin.sendInfo(plugin.getPlayer(participant), player.getDisplayName() + " has joined the channel");
 			}
 		}
 	}
 	
 	@Override
 	public void leave(Player player) {
 		super.getParticipants().remove(player.getName());
 		
 		if (variables.enableLeaveMessages() && plugin.enableLeaveMessage()) {
 			for (String participant : super.getParticipants()) {
 				if (plugin.getPlayer(participant) != null)
 					plugin.sendInfo(plugin.getPlayer(participant), player.getDisplayName() + " has left the channel");
 			}
 		}
 	}
 	
 	@Override
 	public void save() {
 		getConfig().set("tag", variables.getTag());
 		getConfig().set("chat-display-colour", variables.getChatColour());
 		getConfig().set("name-display-colour", variables.getNameColour());
 		getConfig().set("type", super.getType().getName());
 		getConfig().set("global", super.isGlobal());
 		getConfig().set("colour-code", variables.convert());
 		getConfig().set("password", "");
 		getConfig().set("format", variables.getFormat());
 		getConfig().set("admins", super.getAdminList());
 		getConfig().set("whitelist", super.getWhiteList());
 		getConfig().set("blacklist", super.getBlackList());
 		getConfig().set("followers", super.getFollowerList());
 		saveConfig();
 	}
 	
 	@Override
 	public void sendMessage(Player player, String message) {
 		if (super.isGlobal())
 			sendMessage(player, plugin.getServer().getOnlinePlayers(), message);
 		
 		else {
 			List<Player> recipants = new ArrayList<Player>();
 			
 			for (String name : super.getParticipants()) {
				if (plugin.getPlayer(name) != null)
 					recipants.add(plugin.getPlayer(name));
 			}
 			
 			for (String name : plugin.getChannelManager().getFollowers(this)) {
				if (plugin.getPlayer(name) != null && !recipants.contains(name))
 					recipants.add(plugin.getPlayer(name));
 			}
 			
 			sendMessage(player, recipants, message);
 		}
 	}
 	
 	/**
 	 * Sets the password of the channel
 	 * 
 	 * @param password The new password
 	 */
 	public void setPassword(String password) {
 		this.password = password;
 	}
 }
