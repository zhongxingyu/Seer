 package com.titankingdoms.nodinchan.titanchat;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.chat.Chat;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.titankingdoms.nodinchan.titanchat.channel.Channel;
 import com.titankingdoms.nodinchan.titanchat.channel.Channel.Type;
 import com.titankingdoms.nodinchan.titanchat.commands.TitanChatCommandHandler;
 import com.titankingdoms.nodinchan.titanchat.support.CustomChannel;
 import com.titankingdoms.nodinchan.titanchat.support.Support;
 import com.titankingdoms.nodinchan.titanchat.support.SupportLoader;
 import com.titankingdoms.nodinchan.titanchat.util.ConfigManager;
 import com.titankingdoms.nodinchan.titanchat.util.Format;
 
 /*
  *     TitanChat 2.1
  *     Copyright (C) 2012  Nodin Chan <nodinchan@nodinchan.net>
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
 
 public class TitanChat extends JavaPlugin {
 	
 	protected static Logger log = Logger.getLogger("TitanLog");
 	
 	private TitanChatCommandHandler cmdHandler;
 	private ConfigManager configManager;
 	private Format format;
 	private SupportLoader loader;
 	
 	private File channelConfigFile = null;
 	private FileConfiguration channelConfig = null;
 	
 	private boolean silence = false;
 	
 	private List<Channel> channels;
 	private List<com.titankingdoms.nodinchan.titanchat.support.Command> cmds;
 	private List<CustomChannel> customChannels;
 	private List<Support> supports;
 	
 	private Permission permission;
 	private Chat chat;
 	
 	public void assignAdmin(Player player, String channelName) {
 		Channel channel = getChannel(channelName);
 		channel.getAdminList().add(player.getName());
 		sendInfo(player, "You are now an Admin of " + channel.getName());
 	}
 	
 	public void ban(Player player, String channelName) {
 		Channel channel = getChannel(channelName);
 		
 		channel.getAdminList().remove(player.getName());
 		channel.getWhiteList().remove(player.getName());
 		channel.getBlackList().add(player.getName());
 		
 		leaveChannel(player, channelName);
 		sendWarning(player, "You have been banned from " + channel.getName());
 	}
 	
 	public boolean channelExist(String channelName) {
 		for (Channel channel : channels) {
 			if (channel.getName().equalsIgnoreCase(channelName))
 				return true;
 		}
 		
 		return false;
 	}
 	
 	public void channelSwitch(Player player, String oldCh, String newCh) {
 		leaveChannel(player, oldCh);
 		enterChannel(player, newCh);
 	}
 	
 	public boolean correctPass(String channelName, String password) {
 		return getChannel(channelName).getPassword().equals(password);
 	}
 	
 	public void createChannel(Player player, String channelName) {
 		Channel channel = new Channel(this, channelName);
 		channel.setType("public");
 		
 		assignAdmin(player, channelName);
 		enterChannel(player, channelName);
 		sendInfo(player, "You have created " + channel.getName() + " channel");
 	}
 	
 	public String createList(List<String> channels) {
 		StringBuilder str = new StringBuilder();
 		
 		for (String item : channels) {
 			if (str.length() > 0)
 				str.append(", ");
 			
 			str.append(item);
 		}
 		
 		return str.toString();
 	}
 	
 	public void deleteChannel(Player player, String channelName) {
 		Channel channel = getChannel(channelName);
 		
 		for (String participant : channel.getParticipants()) {
 			if (getPlayer(participant) != null) {
 				leaveChannel(getPlayer(participant), channelName);
 				sendWarning(getPlayer(participant), channel.getName() + " has been deleted");
 			}
 		}
 		
 		channels.remove(channel);
 	}
 	
 	public void demote(Player player, String channelName) {
 		Channel channel = getChannel(channelName);
 		channel.getAdminList().remove(player.getName());
 		sendInfo(player, "You have been demoted in " + channel.getName());
 	}
 	
 	public void enterChannel(Player player, String channelName) {
 		Channel channel = getChannel(channelName);
 		channel.getParticipants().add(player.getName());
 		
 		if (configManager.enableJoinMessages()) {
 			for (String participant : channel.getParticipants()) {
 				if (getPlayer(participant) != null && !getPlayer(participant).equals(player))
 					sendInfo(getPlayer(participant), player.getDisplayName() + " has joined the channel");
 			}
 		}
 	}
 	
 	public void follow(Player player, String channelName) {
 		Channel channel = getChannel(channelName);
 		channel.getFollowers().add(player.getName());
 		sendInfo(player, "You have unfollowed " + channel.getName());
 	}
 	
 	public Channel getChannel(String channelName) {
 		for (Channel channel : channels) {
 			if (channel.getName().equalsIgnoreCase(channelName))
 				return channel;
 		}
 		
 		return null;
 	}
 	
 	public Channel getChannel(Player player) {
 		for (Channel channel : channels) {
 			if (channel.getParticipants().contains(player.getName()))
 				return channel;
 		}
 		
 		return null;
 	}
 	
 	public CustomChannel getChannel(Channel channel) {
 		for (CustomChannel customChannel : customChannels) {
 			if (customChannel.getName().equals(channel.getName()))
 				return customChannel;
 		}
 		
 		return null;
 	}
 	
 	public int getChannelAmount() {
 		return channels.size() - customChannels.size();
 	}
 	
 	public FileConfiguration getChannelConfig() {
 		if (channelConfig == null) {
 			reloadChannelConfig();
 		}
 		
 		return channelConfig;
 	}
 	
 	public File getChannelsFolder() {
 		return new File(getDataFolder(), "channels");
 	}
 	
 	public List<com.titankingdoms.nodinchan.titanchat.support.Command> getCommands() {
 		return cmds;
 	}
 	
 	public ConfigManager getConfigManager() {
 		return configManager;
 	}
 	
 	public List<Channel> getChannels() {
 		return channels;
 	}
 	
 	public Channel getDefaultChannel() {
 		for (Channel channel : channels) {
 			if (channel.getType().equals(Type.DEFAULT))
 				return channel;
 		}
 		
 		return null;
 	}
 	
 	public String getExactName(String channelName) {
 		return getChannel(channelName).getName();
 	}
 	
 	public Format getFormat() {
 		return format;
 	}
 	
 	public String getFormat(String channelName) {
 		if (getConfig().get("channels." + getExactName(channelName) + ".format") != null && !getConfig().getString("channels." + getExactName(channelName) + ".format").equalsIgnoreCase(""))
 			return getConfig().getString("channels." + getExactName(channelName) + ".format");
 		
 		return getConfig().getString("formatting.format");
 	}
 	
 	public String getGroupPrefix(Player player) {
 		return chat.getGroupPrefix(player.getWorld(), permission.getPrimaryGroup(player));
 	}
 	
 	public String getGroupSuffix(Player player) {
 		return chat.getGroupSuffix(player.getWorld(), permission.getPrimaryGroup(player));
 	}
 	
 	public Player getPlayer(String name) {
 		return getServer().getPlayer(name);
 	}
 	
 	public String getPlayerPrefix(Player player) {
 		return chat.getPlayerPrefix(player);
 	}
 	
 	public String getPlayerSuffix(Player player) {
 		return chat.getPlayerSuffix(player);
 	}
 	
 	public Channel getStaffChannel() {
 		for (Channel channel : channels) {
 			if (channel.getType().equals(Type.STAFF))
 				return channel;
 		}
 		
 		return null;
 	}
 	
 	public SupportLoader getSupportLoader() {
 		return loader;
 	}
 	
 	public List<Support> getSupports() {
 		return supports;
 	}
 	
 	public File getSupportsFolder() {
 		return new File(getDataFolder(), "supports");
 	}
 	
 	public boolean has(Player player, String permissionNode) {
 		if (permission != null) {
 			return permission.has(player, permissionNode);
 		}
 		
 		return player.hasPermission(permissionNode);
 	}
 	
 	public boolean hasVoice(Player player) {
 		return has(player, "TitanChat.voice");
 	}
 	
 	public void invite(Player player, String channelName) {
 		Channel channel = getChannel(channelName);
 		channel.getInviteList().add(player.getName());
 		
 		sendInfo(player, "You have been invited to chat on " + channel.getName());
 	}
 	
 	public void inviteResponse(Player player, String channelName, boolean accept) {
 		Channel channel = getChannel(channelName);
 		
 		channel.getInviteList().remove(player.getName());
 		
 		if (accept)
 			enterChannel(player, channelName);
 	}
 	
 	public boolean isSilenced() {
 		return silence;
 	}
 	
 	public boolean isStaff(Player player) {
 		if (has(player, "TitanChat.admin"))
 			return true;
 		
 		return false;
 	}
 	
 	public void leaveChannel(Player player, String channelName) {
 		Channel channel = getChannel(channelName);
 		channel.getParticipants().remove(player.getName());
 		
 		if (channel.getType().equals(Type.CUSTOM))
 			getChannel(channel).onLeave(player);
 		
 		else if (configManager.enableLeaveMessages()) {
 			for (String participant : channel.getParticipants()) {
 				if (getPlayer(participant) != null)
 					sendInfo(getPlayer(participant), player.getDisplayName() + " has left the channel");
 			}
 		}
 	}
 	
 	public void log(Level level, String msg) {
 		log.log(level, "[" + this + "] " + msg);
 	}
 	
 	public void mute(Player player, String channelName) {
 		Channel channel = getChannel(channelName);
 		channel.getMuteList().add(player.getName());
 		sendWarning(player, "You have been muted on " + channel.getName());
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
 		if (!(sender instanceof Player)) {
 			if (args[0].equalsIgnoreCase("reload")) {
 				log(Level.INFO, "Reloading configs...");
 				
 				saveConfig();
 				saveChannelConfig();
 				
 				reloadConfig();
 				reloadChannelConfig();
 				
 				try { prepareChannels(); } catch (Exception e) {}
 				
 				log(Level.INFO, "Configs reloaded");
 				return true;
 			}
 			
 			log(Level.INFO, "Please use commands in-game");
 			return true;
 		}
 		
 		String[] arguments = parseCommand(args);
 		
 		Player player = (Player) sender;
 		
 		if (args.length < 1) {
 			player.sendMessage(ChatColor.AQUA + "TitanChat Commands");
 			player.sendMessage(ChatColor.AQUA + "Command: /titanchat [action] [argument]");
 			player.sendMessage(ChatColor.AQUA + "Alias: /tc action [argument]");
 			player.sendMessage(ChatColor.AQUA + "/titanchat commands [page]");
 			return true;
 		}
 		
 		if (cmd.getName().equalsIgnoreCase("titanchat")) {
 			cmdHandler.onCommand(player, args[0], arguments);
 			return true;
 		}
 		
 		sendWarning(player, "Invalid Command");
 		return false;
 	}
 	
 	@Override
 	public void onDisable() {
 		log(Level.INFO, "Clearing useless data...");
 		
 		channels.clear();
 		cmds.clear();
 		customChannels.clear();
 		supports.clear();
 		
 		log(Level.INFO, "is now disabled");
 	}
 	
 	@Override
 	public void onEnable() {
 		log(Level.INFO, "is now enabling...");
 		
 		log(Level.INFO, "Checking for Vault...");
 		
 		PluginManager pm = getServer().getPluginManager();
 		
 		if (pm.getPlugin("Vault") == null) {
 			log(Level.WARNING, "Vault not found!");
 			pm.disablePlugin(this);
 			return;
 		}
 		
 		if (setupPermission()) {
 			log(Level.INFO, permission.getName() + " detected");
 			log(Level.INFO, "Using " + permission.getName() + " for permissions");
 		}
 		
 		if (setupChat()) {
 			log(Level.INFO, "Prefixes and suffixes supported");
 		}
 		
 		log(Level.INFO, "Vault hooked");
 		
 		cmdHandler = new TitanChatCommandHandler(this);
 		configManager = new ConfigManager(this);
 		format = new Format(this);
 		
 		channels = new ArrayList<Channel>();
 		cmds = new ArrayList<com.titankingdoms.nodinchan.titanchat.support.Command>();
 		customChannels = new ArrayList<CustomChannel>();
 		supports = new ArrayList<Support>();
 		
 		File config = new File(getDataFolder(), "config.yml");
 		File channelConfig = new File(getDataFolder(), "channel.yml");
 		
 		if (!config.exists()) {
 			log(Level.INFO, "Loading default config");
 			getConfig().options().copyDefaults(true);
 			getChannelConfig().options().copyHeader(true);
 			saveConfig();
 		}
 		
 		if (!channelConfig.exists()) {
 			log(Level.INFO, "Loading default channel players config");
 			getChannelConfig().options().copyDefaults(true);
 			getChannelConfig().options().copyHeader(true);
 			saveChannelConfig();
 		}
 		
 		if (getChannelsFolder().mkdir())
 			log(Level.INFO, "Loading channels folder...");
 		
 		if (getSupportsFolder().mkdir())
 			log(Level.INFO, "Loading supports folder...");
 		
 		loader = new SupportLoader(this);
 		
 		try { supports.addAll(loader.loadSupports()); } catch (Exception e) {}
 		
 		try { prepareChannels(); } catch (Exception e) {}
 		
 		if (getDefaultChannel() == null) {
 			log(Level.WARNING, "Default channel not defined");
 			pm.disablePlugin(this);
 			return;
 		}
 		
 		pm.registerEvents(new TitanChatPlayerListener(this), this);
 		
 		log(Level.INFO, "is now enabled");
 	}
 	
 	public String[] parseCommand(String[] args) {
 		StringBuilder str = new StringBuilder();
 		
 		for (String arg : args) {
 			if (str.length() > 0)
 				str.append(" ");
 			
 			if (arg.equals(args[0]) || arg.equals(args[1]))
 				continue;
 			
 			str.append(arg);
 		}
 		
 		return str.toString().split(" ");
 	}
 	
 	public void prepareChannels() throws Exception {
 		List<String> globalChannels = new ArrayList<String>();
		customChannels = loader.loadChannels();
 		
		for (CustomChannel customChannel : customChannels) {
 			Channel channel = new Channel(this, customChannel.getName());
 			channel = customChannel.load(channel);
 			channels.add(channel);
 		}
 		
 		for (String channelName : getConfig().getConfigurationSection("channels").getKeys(false)) {
 			if (getChannel(channelName) != null)
 				continue;
 			
 			Channel channel = new Channel(this, channelName);
 			
 			if (getChannelConfig().getStringList("channels." + channel.getName() + ".admins") != null) {
 				for (String name : getChannelConfig().getStringList("channels." + channel.getName() + ".admins")) {
 					channel.getAdminList().add(name);
 				}
 			}
 			
 			if (getChannelConfig().getStringList("channels." + channel.getName() + ".whitelist") != null) {
 				for (String name : getChannelConfig().getStringList("channels." + channel.getName() + ".whitelist")) {
 					channel.getWhiteList().add(name);
 				}
 			}
 			
 			if (getChannelConfig().getStringList("channels." + channel.getName() + ".blacklist") != null) {
 				for (String name : getChannelConfig().getStringList("channels." + channel.getName() + ".blacklist")) {
 					channel.getBlackList().add(name);
 				}
 			}
 			
 			if (getChannelConfig().getStringList("channels." + channel.getName() + ".followers") != null) {
 				for (String name : getChannelConfig().getStringList("channels." + channel.getName() + ".followers")) {
 					channel.getFollowers().add(name);
 				}
 			}
 			
 			channel.setType(getConfig().getString("channels." + channel.getName() + ".type"));
 			
 			if (channel.getType().equals(Type.PASSWORD)) {
 				String password = getConfig().getString("channels." + channel.getName() + ".password");
 				channel.setPassword(password);
 			}
 			
 			if (getConfig().get("channels." + channel.getName() + ".global") != null) {
 				if (getConfig().getBoolean("channels." + channelName + ".global")) {
 					channel.setGlobal(true);
 					globalChannels.add(channelName);
 				}
 			}
 			
 			channels.add(channel);
 		}
 		
 		log(Level.INFO, "No. of channels: " + getChannelAmount());
 		log(Level.INFO, "No. of global broadcasting channels: " + globalChannels.size());
 		log(Level.INFO, "TitanChat Channels Loaded");
 	}
 	
 	public void promote(Player player, String channelName) {
 		assignAdmin(player, channelName);
 		sendInfo(player, "You have been promoted in " + getExactName(channelName));
 	}
 	
 	public void registerCommand(com.titankingdoms.nodinchan.titanchat.support.Command cmd) {
 		cmds.add(cmd);
 	}
 	
 	public void reloadChannelConfig() {
 		if (channelConfigFile == null) {
 			channelConfigFile = new File(getDataFolder(), "channel.yml");
 		}
 		
 		channelConfig = YamlConfiguration.loadConfiguration(channelConfigFile);
 		
 		InputStream defConfigStream = getResource("channel.yml");
 		
 		if (defConfigStream != null) {
 			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
 			channelConfig.setDefaults(defConfig);
 		}
 	}
 	
 	public void saveChannelConfig() {
 		if (channelConfig == null || channelConfigFile == null)
 			return;
 		
 		try {
 			channelConfig.save(channelConfigFile);
 			
 		} catch (IOException e) {
 			log.severe("Could not save config to " + channelConfigFile);
 		}
 	}
 	
 	public void sendInfo(Player player, String info) {
 		player.sendMessage("[TitanChat] " + ChatColor.GOLD + info);
 	}
 	
 	public void sendWarning(Player player, String warning) {
 		player.sendMessage("[TitanChat] " + ChatColor.RED + warning);
 	}
 	
 	public void setSilence(boolean silence) {
 		this.silence = silence;
 	}
 	
 	public boolean setupChat() {
 		RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(Chat.class);
 		
 		if (chatProvider != null) {
 			chat = chatProvider.getProvider();
 		}
 		
 		return (chat != null);
 	}
 	
 	public boolean setupPermission() {
 		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
 		
 		if (permissionProvider != null) {
 			permission = permissionProvider.getProvider();
 		}
 		
 		return (permission != null);
 	}
 	
 	public void unban(Player player, String channelName) {
 		Channel channel = getChannel(channelName);
 		channel.getBlackList().remove(player.getName());
 		
 		whitelistMember(player, channelName);
 		sendInfo(player, "You have been unbanned from " + channel.getName());
 	}
 	
 	public void unfollow(Player player, String channelName) {
 		Channel channel = getChannel(channelName);
 		channel.getFollowers().remove(player.getName());
 		sendInfo(player, "You have unfollowed " + channel.getName());
 	}
 	
 	public void unmute(Player player, String channelName) {
 		Channel channel = getChannel(channelName);
 		channel.getMuteList().remove(player.getName());
 		sendInfo(player, "You have been unmuted on " + channel.getName());
 	}
 	
 	public boolean useDefaultFormat() {
 		return getConfig().getBoolean("formatting.use-built-in");
 	}
 	
 	public void whitelistMember(Player player, String channelName) {
 		Channel channel = getChannel(channelName);
 		channel.getWhiteList().add(player.getName());
 		sendInfo(player, "You are now a Member of " + channel.getName());
 	}
 }
