 package com.titankingdoms.nodinchan.titanchat.channel;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 import com.nodinchan.ncbukkit.loader.Loader;
 import com.titankingdoms.nodinchan.titanchat.TitanChat;
 import com.titankingdoms.nodinchan.titanchat.TitanChat.MessageLevel;
 import com.titankingdoms.nodinchan.titanchat.channel.Channel.Option;
 import com.titankingdoms.nodinchan.titanchat.channel.standard.ServerChannel;
 import com.titankingdoms.nodinchan.titanchat.channel.standard.StandardChannel;
 import com.titankingdoms.nodinchan.titanchat.channel.util.Participant;
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
 
 public final class ChannelManager {
 	
 	private final TitanChat plugin;
 	
 	private static final Debugger db = new Debugger(2);
 	
 	private final Map<String, String> aliases;
 	private final Map<String, Channel> channels;
 	private final Map<String, Participant> participants;
 	private final Map<String, Boolean> silenced;
 	private final Map<String, Channel> types;
 	
 	public ChannelManager() {
 		this.plugin = TitanChat.getInstance();
		
		if (getCustomChannelDir().mkdirs())
			plugin.log(Level.INFO, "Creating custom channels directory...");
		
 		this.aliases = new HashMap<String, String>();
 		this.channels = new LinkedHashMap<String, Channel>();
 		this.participants = new HashMap<String, Participant>();
 		this.silenced = new HashMap<String, Boolean>();
 		this.types = new LinkedHashMap<String, Channel>();
 	}
 	
 	public void createChannel(CommandSender sender, String name, String type) {
 		db.i("ChannelManager: " + sender.getName() + " is creating " + name);
 		Channel channel = getType(type).create(sender, name, Option.NONE);
 		register(channel);
 		
 		if (sender instanceof Player) {
 			channel.join((Player) sender);
 			channel.getAdmins().add(sender.getName());
 		}
 		
 		channel.getConfig().options().copyDefaults(true);
 		channel.saveConfig();
 		
 		sortChannels();
 		
 		plugin.getDefPerms().load(channel);
 		plugin.send(MessageLevel.INFO, sender, "You have created channel " + name + " of type " + channel.getType());
 	}
 	
 	public void deleteChannel(CommandSender sender, String name) {
 		db.i("ChannelManager: " + sender.getName() + " is deleting " + name);
 		Channel channel = getChannel(name);
 		
 		List<Participant> participants = channel.getParticipants();
 		
 		for (Participant participant : participants) {
 			if (participant.getPlayer() != null) {
 				participant.leave(channel);
 				plugin.send(MessageLevel.WARNING, participant.getPlayer(), channel.getName() + " has been deleted");
 			}
 		}
 		
 		channels.remove(name.toLowerCase());
 		
 		sortChannels();
 		new File(plugin.getChannelDir(), channel.getName() + ".yml").delete();
 		plugin.send(MessageLevel.INFO, sender, "You have deleted " + channel.getName());
 	}
 	
 	public boolean exists(String name) {
 		return channels.containsKey(name.toLowerCase());
 	}
 	
 	public boolean existsByAlias(String name) {
 		if (exists(name))
 			return true;
 		
 		if (!aliases.containsKey(name.toLowerCase()))
 			return false;
 		
 		return exists(aliases.get(name.toLowerCase()));
 	}
 	
 	public Channel getChannel(String name) {
 		return channels.get(name.toLowerCase());
 	}
 	
 	public Channel getChannel(Player player) {
 		return getParticipant(player).getCurrentChannel();
 	}
 	
 	public Channel getChannelByAlias(String name) {
 		Channel channel = getChannel(name);
 		
 		if (channel != null)
 			return channel;
 		
 		if (!aliases.containsKey(name.toLowerCase()))
 			return null;
 		
 		return getChannel(aliases.get(name.toLowerCase()));
 	}
 	
 	public List<Channel> getChannels() {
 		return new ArrayList<Channel>(channels.values());
 	}
 	
 	public File getCustomChannelDir() {
 		return new File(plugin.getManager().getAddonManager().getAddonDir(), "channels");
 	}
 	
 	public List<Channel> getDefaultChannels() {
 		List<Channel> defaults = new ArrayList<Channel>();
 		
 		for (Channel channel : channels.values())
 			if (channel.getOption().equals(Option.DEFAULT))
 				defaults.add(channel);
 		
 		return defaults;
 	}
 	
 	public Participant getParticipant(String name) {
 		return participants.get(name.toLowerCase());
 	}
 	
 	public Participant getParticipant(Player player) {
 		return getParticipant(player.getName());
 	}
 	
 	public List<Channel> getStaffChannels() {
 		List<Channel> staffs = new ArrayList<Channel>();
 		
 		for (Channel channel : channels.values())
 			if (channel.getOption().equals(Option.STAFF))
 				staffs.add(channel);
 		
 		return staffs;
 	}
 	
 	public Channel getType(String name) {
 		return types.get(name.toLowerCase());
 	}
 	
 	public List<Channel> getTypes() {
 		return new ArrayList<Channel>(types.values());
 	}
 	
 	public boolean isSilenced(Channel channel) {
 		if (!silenced.containsKey(channel.getName().toLowerCase()))
 			return false;
 		
 		return silenced.get(channel.getName().toLowerCase());
 	}
 	
 	public void load() {
 		if (!plugin.enableChannels()) {
 			plugin.log(Level.INFO, "Channels disabled");
 			register(new ServerChannel());
 			return;
 		}
 		
 		Loader<Channel> loader = new Loader<Channel>(plugin, getCustomChannelDir());
 		
 		register(new StandardChannel());
 		
 		for (Channel channel : loader.load())
 			if (!exists(channel.getName()))
 				register(channel);
 		
 		sortTypes();
 		
 		for (File file : plugin.getChannelDir().listFiles()) {
 			if (!file.getName().endsWith(".yml"))
 				continue;
 			
 			Channel channel = loadChannel(file);
 			
 			if (channel == null || exists(channel.getName()))
 				continue;
 			
 			register(channel);
 		}
 		
 		sortChannels();
 		
 		plugin.getDefPerms().load(getChannels());
 	}
 	
 	private Channel loadChannel(File file) {
 		String name = file.getName().replace(".yml", "");
 		
 		if (name == null || name.equals(""))
 			return null;
 		
 		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
 		
 		Channel type = getType(config.getString("type", ""));
 		
 		if (type == null)
 			return null;
 		
 		Option option = Option.fromName(config.getString("option", ""));
 		
 		if (option == null)
 			return null;
 		
 		return type.load(name, option);
 	}
 	
 	public Participant loadParticipant(Player player) {
 		Participant participant = getParticipant(player);
 		
 		if (participant == null)
 			participant = new Participant(player);
 		
 		for (Channel channel : getChannels()) {
 			if (channel.getConfig().getStringList("participants") == null)
 				continue;
 			
 			if (channel.getConfig().getStringList("participants").contains(player.getName()))
 				channel.join(player);
 		}
 		
 		return participants.put(participant.getName().toLowerCase(), participant);
 	}
 	
 	public boolean nameCheck(String name) {
 		if (name.contains("\\") || name.contains("|") || name.contains("/"))
 			return false;
 		
 		if (name.contains(":") || name.contains("?"))
 			return false;
 		
 		if (name.contains("*") || name.contains("\""))
 			return false;
 		
 		if (name.contains("<") || name.contains(">"))
 			return false;
 		
 		return true;
 	}
 	
 	public void postReload() {
 		load();
 		
 		for (Player player : plugin.getServer().getOnlinePlayers())
 			loadParticipant(player);
 	}
 	
 	public void preReload() {
 		for (Channel channel : getChannels()) {
 			channel.reloadConfig();
 			channel.save();
 			channel.saveParticipants();
 		}
 		
 		this.aliases.clear();
 		this.channels.clear();
 		this.participants.clear();
 		this.silenced.clear();
 		this.types.clear();
 	}
 	
 	public void register(Channel channel) {
 		if (channel.getOption().equals(Option.TYPE)) {
 			if (getType(channel.getType()) == null)
 				types.put(channel.getType().toLowerCase(), channel);
 			
 		} else {
 			if (getChannel(channel.getName()) == null) {
 				channels.put(channel.getName().toLowerCase(), channel);
 				
 				for (String alias : channel.getConfig().getStringList("aliases"))
 					if (!aliases.containsKey(alias.toLowerCase()))
 						aliases.put(alias.toLowerCase(), channel.getName());
 			}
 		}
 	}
 	
 	public void setSilence(Channel channel, boolean silenced) {
 		this.silenced.put(channel.getName().toLowerCase(), silenced);
 	}
 	
 	public void sortChannels() {
 		Map<String, Channel> channels = new LinkedHashMap<String, Channel>();
 		List<String> names = new ArrayList<String>(this.channels.keySet());
 		
 		Collections.sort(names);
 		
 		for (String name : names)
 			channels.put(name, getChannel(name));
 		
 		this.channels.clear();
 		this.channels.putAll(channels);
 	}
 	
 	public void sortTypes() {
 		Map<String, Channel> types = new LinkedHashMap<String, Channel>();
 		List<String> names = new ArrayList<String>(this.types.keySet());
 		
 		Collections.sort(names);
 		
 		for (String name : names)
 			types.put(name, getChannel(name));
 		
 		this.types.clear();
 		this.types.putAll(types);
 	}
 	
 	public void unload() {
 		for (Channel channel : getChannels()) {
 			channel.save();
 			channel.saveParticipants();
 		}
 		
 		this.aliases.clear();
 		this.channels.clear();
 		this.silenced.clear();
 		this.types.clear();
 	}
 }
