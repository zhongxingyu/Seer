 /* Copyright (c) 2012 Walker Crouse, <http://windwaker.net/>
  *
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 package net.windwaker.chat.chan;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import net.windwaker.chat.WindChat;
 import net.windwaker.chat.event.chatter.ChatterChatEvent;
 import net.windwaker.chat.event.chatter.ChatterInviteChangeEvent;
 import net.windwaker.chat.event.chatter.ChatterJoinEvent;
 import net.windwaker.chat.event.chatter.ChatterKickEvent;
 import net.windwaker.chat.event.chatter.ChatterLeaveEvent;
 import net.windwaker.chat.io.yaml.ChatConfiguration;
 import net.windwaker.chat.util.Format;
 import net.windwaker.chat.util.Placeholders;
 
 import org.spout.api.Spout;
 import org.spout.api.chat.ChatArguments;
 import org.spout.api.chat.style.ChatStyle;
 import org.spout.api.command.CommandSource;
 import org.spout.api.data.ValueHolder;
 import org.spout.api.entity.Player;
 import org.spout.api.util.Named;
 
 public class Chatter implements Named {
 	private final WindChat plugin;
 	private final CommandSource parent;
 	private final Set<Channel> channels = new HashSet<Channel>(), invites = new HashSet<Channel>();
 	private Channel activeChannel;
 	private ChatArguments quitMessage;
 	private boolean autoSave;
 	private Chatter lastSender;
 
 	/**
 	 * Constructs a new chatter object
 	 * 
 	 * @param parent
 	 */
 	public Chatter(WindChat plugin, CommandSource parent) {
 		this.parent = parent;
 		this.plugin = plugin;
 	}
 
 	/**
 	 * Sends a private message to this chatter. Only this chatter can see the
 	 * specified message.
 	 *
 	 * @param sender of the message
 	 * @param message to send
 	 */
 	public void sendPrivateMessage(Chatter sender, ChatArguments message) {
 		lastSender = sender;
 		// Incoming
 		ChatArguments from = ChatArguments.fromFormatString(ChatConfiguration.PRIVATE_MESSAGE_FORMAT.getString());
 		Placeholders.format(Placeholders.NAME, from, new ChatArguments(sender.getParent().getName()));
 		Placeholders.format(Placeholders.MESSAGE, from, message);
 		Placeholders.format(Placeholders.ADDRESS, from, new ChatArguments("From"));
 		parent.sendMessage(from);
 		// Outgoing
 		ChatArguments to = ChatArguments.fromFormatString(ChatConfiguration.PRIVATE_MESSAGE_FORMAT.getString());
 		Placeholders.format(Placeholders.NAME, to, new ChatArguments(sender.getParent().getName()));
 		Placeholders.format(Placeholders.MESSAGE, to, message);
 		Placeholders.format(Placeholders.ADDRESS, to, new ChatArguments("To"));
 		sender.getParent().sendMessage(to);
 	}
 
 	/**
 	 * Replies to the last chatter to send a private message to this chatter.
 	 *
 	 * @param message
 	 */
 	public void reply(ChatArguments message) {
 		if (lastSender == null) {
 			throw new IllegalStateException("Cannot send message to null chatter.");
 		}
 		lastSender.sendPrivateMessage(this, message);
 	}
 
 	/**
 	 * Returns the last chatter to send a private message to this chatter.
 	 *
 	 * @return last sender of private message
 	 */
 	public Chatter getLastSender() {
 		return lastSender;
 	}
 
 	/**
 	 * Whether or not the chatter is saved to disk after every property is set
 	 * 
 	 * @return true if auto save
 	 */
 	public boolean isAutoSave() {
 		return autoSave;
 	}
 
 	/**
 	 * Sets whether the chatter should save to disk every property set
 	 * 
 	 * @param autoSave
 	 */
 	public void setAutoSave(boolean autoSave) {
 		this.autoSave = autoSave;
 	}
 
 	/**
 	 * Gets the parent of the chatter.
 	 * 
 	 * @return parent
 	 */
 	public CommandSource getParent() {
 		return parent;
 	}
 
 	/**
 	 * Whether the chatter can hear the sender
 	 * 
 	 * @param sender
 	 * @return true if can hear
 	 */
 	public boolean canHear(Chatter sender, Channel channel) {
 		if (!(parent instanceof Player) || !(sender instanceof Player)) {
 			return true;
 		}
 		int radius = channel.getRadius();
		return ((Player) parent).getTransform().getPosition().getDistance(((Player) sender.getParent()).getTransform().getPosition()) < radius || radius <= 0;
 	}
 
 	/**
 	 * Sets the quit message of the chatter
 	 * 
 	 * @param quitMessage
 	 */
 	public void setQuitMessage(ChatArguments quitMessage) {
 		this.quitMessage = quitMessage;
 		if (autoSave) {
 			save();
 		}
 	}
 
 	/**
 	 * Gets the quit message of the chatter
 	 * 
 	 * @return
 	 */
 	public ChatArguments getQuitMessage() {
 		return quitMessage;
 	}
 
 	/**
 	 * Invites the chatter to a channel
 	 * 
 	 * @param channel
 	 */
 	public void invite(Channel channel) {
 		ChatterInviteChangeEvent event = Spout.getEventManager().callEvent(new ChatterInviteChangeEvent(this, channel, true));
 		if (event.isCancelled()) {
 			return;
 		}
 		channel = event.getChannel();
 		if (!event.isInvited()) {
 			revokeInvite(channel);
 			return;
 		}
 		invites.add(channel);
 		if (autoSave) {
 			save();
 		}
 	}
 
 	/**
 	 * Revokes an invitation to a channel
 	 * 
 	 * @param channel
 	 */
 	public void revokeInvite(Channel channel) {
 		ChatterInviteChangeEvent event = Spout.getEventManager().callEvent(new ChatterInviteChangeEvent(this, channel, false));
 		if (event.isCancelled()) {
 			return;
 		}
 		channel = event.getChannel();
 		if (event.isInvited()) {
 			invite(channel);
 			return;
 		}
 		invites.remove(channel);
 		if (autoSave) {
 			save();
 		}
 	}
 
 	/**
 	 * Whether or not the chatter is invited to the specified channel
 	 * 
 	 * @param channel
 	 * @return true if invited
 	 */
 	public boolean isInvitedTo(Channel channel) {
 		return invites.contains(channel);
 	}
 
 	/**
 	 * Returns all the channels the chatter is invited to
 	 * 
 	 * @return invited channels
 	 */
 	public Set<Channel> getInvites() {
 		return invites;
 	}
 
 	/**
 	 * Sends a chat message to the chatters active channel.
 	 * 
 	 * @param message 
 	 */
 	public void chat(ChatArguments message) {
 		chat(activeChannel, message);
 	}
 
 	/**
 	 * Sends a chat message to the specified channel.
 	 * 
 	 * @param message
 	 */
 	public void chat(Channel channel, ChatArguments message) {
 		ChatterChatEvent event = Spout.getEventManager().callEvent(new ChatterChatEvent(this, channel, message));
 		if (event.isCancelled()) {
 			return;
 		}
 		channel = event.getChannel();
 		message = event.getMessage();
 		if (!parent.hasPermission("windchat.chat." + activeChannel.getName())) {
 			parent.sendMessage(ChatStyle.RED, "You don't have permission to chat in this channel!");
 			return;
 		}
 		if (activeChannel.isMuted(getParent().getName())) {
 			return;
 		}
 		message = channel.censorMessage(message);
 		ChatArguments template = getFormat(Format.CHAT);
 		Placeholders.format(Placeholders.NAME, template, new ChatArguments(parent.getName()));
 		Placeholders.format(Placeholders.MESSAGE, template, message);
 		channel.broadcast(template);
 	}
 
 	/**
 	 * Joins a channel
 	 * 
 	 * @param channel
 	 */
 	public void join(Channel channel) {
 		join(channel, channel.getJoinMessage());
 	}
 
 	/**
 	 * Joins a channel
 	 * 
 	 * @param channel
 	 * @param message
 	 */
 	public void join(Channel channel, ChatArguments message) {
 		join(channel, message, true);
 	}
 
 	/**
 	 * Joins a channel
 	 * 
 	 * @param channel
 	 * @param message
 	 */
 	public void join(Channel channel, ChatArguments message, boolean active) {
 		ChatterJoinEvent event = Spout.getEventManager().callEvent(new ChatterJoinEvent(this, channel, message, active));
 		if (event.isCancelled()) {
 			return;
 		}
 		channel = event.getChannel();
 		if (event.isActive()) {
 			activeChannel = channel;
 		}
 		channels.add(channel);
 		channel.addListener(this);
 		parent.sendMessage(event.getMessage());
 		if (autoSave) {
 			save();
 		}
 	}
 
 	/**
 	 * Leaves a channel
 	 * 
 	 * @param channel
 	 */
 	public void leave(Channel channel) {
 		leave(channel, channel.getLeaveMessage());
 	}
 
 	/**
 	 * Leaves a channel
 	 * 
 	 * @param channel
 	 * @param message
 	 */
 	public void leave(Channel channel, ChatArguments message) {
 		ChatterLeaveEvent event = Spout.getEventManager().callEvent(new ChatterLeaveEvent(this, channel, message));
 		if (event.isCancelled()) {
 			return;
 		}
 		channel = event.getChannel();
 		if (channel.equals(activeChannel)) {
 			throw new IllegalArgumentException("A player may not leave the channel he/she is active in.");
 		}
 		channel.removeListener(parent.getName());
 		channels.remove(channel);
 		parent.sendMessage(event.getMessage());
 		if (autoSave) {
 			save();
 		}
 	}
 
 	/**
 	 * Bans the chatter from the specified channel.
 	 * 
 	 * @param channel
 	 */
 	public void ban(Channel channel) {
 		ban(channel, new ChatArguments(ChatStyle.RED, "Banned from ", channel.getName()));
 	}
 
 	/**
 	 * Bans the chatter from the specified channel.
 	 * 
 	 * @param channel
 	 * @param reason
 	 */
 	public void ban(Channel channel, ChatArguments reason) {
 		channel.ban(parent.getName(), true, reason);
 	}
 
 	/**
 	 * Kicks the chatter from the specified channel
 	 * 
 	 * @param channel
 	 */
 	public void kick(Channel channel) {
 		kick(channel, new ChatArguments(ChatStyle.RED, "Kicked from ", channel.getName()));
 	}
 
 	/**
 	 * Kicks the chatter from the specified channel
 	 * 
 	 * @param channel
 	 * @param reason
 	 */
 	public void kick(Channel channel, ChatArguments reason) {
 		ChatterKickEvent event = Spout.getEventManager().callEvent(new ChatterKickEvent(this, channel, reason));
 		Channel def = plugin.getChannels().getDefault();
 		channel = event.getChannel();
 		if (channel.equals(def)) {
 			throw new IllegalStateException("You cannot kick a player from the default channel.");
 		}
 		if (channel.equals(activeChannel)) {
 			join(def);
 		}
 		leave(channel, new ChatArguments(ChatStyle.RED, "Kicked from ", channel.getName(), ": ", event.getMessage()));
 	}
 
 	/**
 	 * Gets the format of a certain {@link Format} type.
 	 * 
 	 * @param format
 	 * @return format
 	 */
 	public ChatArguments getFormat(Format format) {
 		ChatArguments def = format.getDefault();
 		ValueHolder data = parent.getData(format.toString());
 		if (data != null && data.getString() != null) {
 			def = ChatArguments.fromFormatString(data.getString());
 		}
 		return def;
 	}
 
 	/**
 	 * Gets the channel the chatter is currently chatting in.
 	 * 
 	 * @return active channel.
 	 */
 	public Channel getActiveChannel() {
 		return activeChannel;
 	}
 
 	/**
 	 * Gets all listening channels.
 	 * 
 	 */
 	public Set<Channel> getChannels() {
 		return channels;
 	}
 
 	/**
 	 * Saves the chatter to disk
 	 */
 	public void save() {
 		plugin.getChatters().save(this);
 	}
 
 	@Override
 	public String toString() {
 		return parent.getName();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		return obj instanceof Chatter && ((Chatter) obj).getParent().equals(parent);
 	}
 
 	@Override
 	public int hashCode() {
 		return parent.hashCode();
 	}
 
 	@Override
 	public String getName() {
 		return parent.getName();
 	}
 }
