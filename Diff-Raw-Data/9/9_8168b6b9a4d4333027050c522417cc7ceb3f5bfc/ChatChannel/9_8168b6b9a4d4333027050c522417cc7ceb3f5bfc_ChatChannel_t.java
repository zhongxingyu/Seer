 package com.caved_in.chatmanager.handlers.chat.channels;
 
 import java.util.*;
 
import com.caved_in.chatmanager.commands.CommandPermissions;
 import com.caved_in.chatmanager.handlers.player.PlayerHandler;
 import com.caved_in.chatmanager.handlers.chat.formatting.ChatFormat;
 
 public class ChatChannel implements IChatChannel
 {
 	private String channelName = "";
 	private String channelPrefix = "";
 	private ChatFormat chatFormat = new ChatFormat("Default", "%sender% - %message%");
 	private List<String> chatMembers = new ArrayList<String>();
 
 	private boolean isPrivate = false;
 
 	private boolean isPermanent = false;
 
 	private boolean hasPermission = false;
	private String channelPermission = "chatmanager.channel.join";
 
 	private boolean hasJoinLeaveMessages = true;
 
 	private String channelCreator = "";
 
 
 	public ChatChannel(String channelName)
 	{
 		this.channelName = channelName;
 		this.channelPrefix = "[" + channelName + "]";
 	}
 
 	/**
 	 * @param channelName
 	 * @param channelPrefix
 	 */
 	public ChatChannel(String channelName, String channelPrefix)
 	{
 		this.channelName = channelName;
 		this.channelPrefix = channelPrefix;
 	}
 
 	/**
 	 * @param channelName
 	 * @param channelPrefix
 	 * @param ChannelCreator
 	 * @param isPrivate
 	 */
 	public ChatChannel(String channelName, String channelPrefix, String ChannelCreator, boolean isPrivate)
 	{
 		this.channelName = channelName;
 		this.channelPrefix = channelPrefix;
 		this.channelCreator = ChannelCreator;
 		this.isPrivate = isPrivate;
 	}
 
 	/**
 	 * @param channelName
 	 * @param channelPrefix
 	 * @param ChatFormat
 	 */
 	public ChatChannel(String channelName, String channelPrefix, ChatFormat ChatFormat)
 	{
 		this.channelName = channelName;
 		this.channelPrefix = channelPrefix;
 		this.chatFormat = ChatFormat;
 	}
 
 	/**
 	 * @param channelName
 	 * @param channelPrefix
 	 * @param ChatFormat
 	 * @param ChannelCreator
 	 * @param isPrivate
 	 */
 	public ChatChannel(String channelName, String channelPrefix, ChatFormat ChatFormat, String ChannelCreator, boolean isPrivate)
 	{
 		this.channelName = channelName;
 		this.channelPrefix = channelPrefix;
 		this.chatFormat = ChatFormat;
 		this.isPrivate = isPrivate;
 		this.channelCreator = ChannelCreator;
 	}
 
 	/**
 	 * @param channelName
 	 * @param channelPrefix
 	 * @param ChatFormat
 	 * @param channelPermission
 	 */
 	public ChatChannel(String channelName, String channelPrefix, ChatFormat ChatFormat, String channelPermission)
 	{
 		this.channelName = channelName;
 		this.channelPrefix = channelPrefix;
 		this.chatFormat = ChatFormat;
 		if (!channelPermission.isEmpty())
 		{
 			this.hasPermission = true;
 			this.channelPermission = channelPermission;
 		}
 	}
 
 	/**
 	 * @param channelName
 	 * @param channelPrefix
 	 * @param ChatFormat
 	 * @param ChannelCreator
 	 * @param isPrivate
 	 * @param channelPermission
 	 */
 	public ChatChannel(String channelName, String channelPrefix, ChatFormat ChatFormat, String ChannelCreator, boolean isPrivate, String channelPermission)
 	{
 		this.channelName = channelName;
 		this.channelPrefix = channelPrefix;
 		this.chatFormat = ChatFormat;
 		this.isPrivate = isPrivate;
 		this.channelCreator = ChannelCreator;
 		if (!channelPermission.isEmpty())
 		{
 			this.hasPermission = true;
 			this.channelPermission = channelPermission;
 		}
 	}
 
 	@Override
 	public String getName()
 	{
 		return this.channelName;
 	}
 
 	@Override
 	public String getPrefix()
 	{
 		return this.channelPrefix;
 	}
 
 	@Override
 	public void setName(String Name)
 	{
 		this.channelName = Name;
 	}
 
 	@Override
 	public void setPrefix(String Name)
 	{
 		this.channelPrefix = Name;
 	}
 
 	@Override
 	public List<String> getChatMembers()
 	{
 		return this.chatMembers;
 	}
 
 	@Override
 	public void addMember(String Name)
 	{
 		if (!isMember(Name))
 		{
 			this.chatMembers.add(Name);
 		}
 	}
 
 	@Override
 	public void removeMember(String Name)
 	{
 		if (isMember(Name))
 		{
 			this.chatMembers.remove(Name);
 		}
 	}
 
 	@Override
 	public boolean isMember(String Name)
 	{
 		return this.chatMembers.contains(Name);
 	}
 
 	@Override
 	public void sendToMembers(String messageSender, String message)
 	{
 		for (String chatMember : this.getChatMembers())
 		{
 			if (PlayerHandler.isOnline(chatMember))
 			{
 				PlayerHandler.getPlayer(chatMember).sendMessage(this.getPrefix() + this.chatFormat.getFormattedMessage(messageSender, message));
 			}
 		}
 	}
 
 	public void sendToMembers(String Message)
 	{
 		for (String Member : this.getChatMembers())
 		{
 			if (PlayerHandler.isOnline(Member))
 			{
 				PlayerHandler.getPlayer(Member).sendMessage(this.getPrefix() + Message);
 			}
 		}
 	}
 
 	@Override
 	public boolean allowJoinLeaveMessages()
 	{
 		return this.hasJoinLeaveMessages;
 	}
 
 	@Override
 	public String getChannelPermission()
 	{
 		return this.channelPermission;
 	}
 
 	@Override
 	public boolean hasPermission()
 	{
		return !(this.channelPermission.equalsIgnoreCase(CommandPermissions.CHANNEL_JOIN_PERMISSION));
 	}
 
 	@Override
 	public boolean isPrivate()
 	{
 		return this.isPrivate;
 	}
 
 	@Override
 	public void setChannelPermission(String Permission)
 	{
 		this.channelPermission = Permission;
 	}
 
 	@Override
 	public void setHasJoinLeaveMessages(boolean Messages)
 	{
 		this.hasJoinLeaveMessages = Messages;
 	}
 
 	@Override
 	public void setPrivate(boolean Private)
 	{
 		this.isPrivate = Private;
 	}
 
 	@Override
 	public String getCreator()
 	{
 		return this.channelCreator;
 	}
 
 	@Override
 	public void setCreator(String Creator)
 	{
 		this.channelCreator = Creator;
 	}
 
 	@Override
 	public boolean isPermanent()
 	{
 		return this.isPermanent;
 	}
 
 	@Override
 	public void setPermanent(boolean Permanant)
 	{
 		this.isPermanent = Permanant;
 	}
 
 	public void setChatFormat(String chatFormat)
 	{
 		this.chatFormat.setFormat(chatFormat);
 	}
 }
