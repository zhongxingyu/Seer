 package de.skuzzle.polly.sdk;
 
 import java.util.List;
 import java.util.Set;
 
 import de.skuzzle.polly.sdk.eventlistener.ChannelModeListener;
 import de.skuzzle.polly.sdk.eventlistener.ConnectionListener;
 import de.skuzzle.polly.sdk.eventlistener.JoinPartListener;
 import de.skuzzle.polly.sdk.eventlistener.MessageListener;
 import de.skuzzle.polly.sdk.eventlistener.MessageSendListener;
 import de.skuzzle.polly.sdk.eventlistener.NickChangeListener;
 import de.skuzzle.polly.sdk.eventlistener.QuitListener;
 import de.skuzzle.polly.sdk.eventlistener.UserSpottedListener;
 
 
 /**
  * <p>This class manages all irc related tasks and provides a few events on which you can
  * react.</p>
  *
  * <p>Please note that all events raised by this class are called in another Thread.</p>
  * 
  * @author Simon
  * @since zero day
  * @version RC 1.0
  */
 public interface IrcManager {
 
     /**
     * Quits the irc with the given quit message. Note that this method may be overriden
      * by IRC servers. For example euIrc overrides it by "Life is too short" if you
      * quit shortly after joining.
      * 
      * @param message The quit message.
      */
 	public abstract void quit(String message);
 
 	
 	
 	/**
 	 * Quits the irc using a default quit message. 
 	 */
 	public abstract void quit();
 
 	
 	
 	/**
 	 * Determines whether a user with given nick is online in any channel.
 	 * 
 	 * @param nickName The nickname to check.
 	 * @return <code>true</code> if the user is online on any channel that polly is on.
 	 */
 	public abstract boolean isOnline(String nickName);
 	
 	
 	// ISSUE: 0000052
     /**
      * Determines whether a user with given nick is online in any channel. This method
      * is case insensitive with nicknames.
      * 
      * @param nickName The nickname to check.
      * @return <code>true</code> if the user is online on any channel that polly is on.
      * @since 0.6.1
      */
 	public abstract  boolean isOnlineIgnoreCase(String nickName);
 
 	
 	
 	/**
 	 * Gets a readonly set of all users that are currently online on any channel that 
 	 * polly is on.
 	 *  
 	 * @return A set of nicknames.
 	 */
 	public abstract Set<String> getOnlineUsers();
 
 	
 	
 	/**
 	 * Closes the irc connection. 
 	 */
 	public abstract void disconnect();
 
 	
 	
 	/**
 	 * Determines if polly is currently connected to the irc.
 	 * @return <code>true</code> if polly is connected.
 	 */
 	public abstract boolean isConnected();
 
 	
 	
 	/**
 	 * Gets a list of all channels that polly is currently on.
 	 * 
	 * @return A lit of channel names.
 	 */
 	public abstract List<String> getChannels();
 
 	
 	/**
 	 * Determines whether an user with given nickname is online in the given channel.
 	 * Obviously this requires polly to be on the same channel too.
 	 * 
 	 * @param channel The channel.
 	 * @param nickName The user to check.
 	 * @return <code>true</code> if a user with the given nickname is online in the given
 	 *     channel.
 	 */
 	public abstract boolean isOnChannel(String channel, String nickName);
 
 	
 	
 	/**
 	 * Gets all users which are online on the given channel. Will return an empty list
 	 * if polly is not on the given channel-
 	 * 
 	 * @param channel The channel.
 	 * @return A list of nicknames.
 	 */
 	public abstract List<String> getChannelUser(String channel);
 	
 	
 	
 	/**
 	 * Joins all the channels that are specified in the configuration file as 
 	 * {@link Configuration#CHANNELS}.
 	 * @since 0.9.1
 	 */
 	public abstract void rejoinDefaultChannels();
 	
 	
 	
 	/**
 	 * Lets polly join a channel using a password. Type any String as password if the
 	 * channel you are willing to join does not have a password.
 	 *  
 	 * @param channel The channel to join. Must be preceded by a '#'
 	 * @param password The password for the channel.
 	 * @throws IllegalArgumentException If the channel does not start with a '#'.
 	 */
 	public abstract void joinChannel(String channel, String password);
 	
 	
 	
 	/**
 	 * Lets polly join the given channels. All channelnames must be preceded by a '#'. If
 	 * one channel does not start with a '#', all further channels will not be joined and
 	 * an {@link IllegalArgumentException} is thrown.
 	 * 
 	 * @param channels The channels to join.
 	 */
 	public abstract void joinChannels(String...channels);
 
 	
 	/**
 	 * Lets polly part the given channel using the given message as part message. If 
 	 * polly is not on the channel you are trying to part, nothing happens.
 	 * 
 	 * @param channel The channel to leave.
 	 * @param message The message to show upon leaving.
 	 */
 	public abstract void partChannel(String channel, String message);
 
 	
 	
 	/**
 	 * Kicks an user from the given channel. This method will have no effect if polly has
 	 * insufficient rights on the channel.
 	 * 
 	 * @param channel The channel from which the user shall be kicked.
 	 * @param nickName The user to kick.
 	 * @param reason Kickreason which will be shown in the kick message.
 	 */
 	public abstract void kick(String channel, String nickName, String reason);
 
 	
 	
 	/**
 	 * Grants operator rights to the given user. This method will have no effect if polly
 	 * has insufficient rights on the channel.
 	 * 
 	 * @param channel The channel on which the user shall be granted operator rights.
 	 * @param nickName The user.
 	 */
 	public abstract void op(String channel, String nickName);
 
 	
 	
 	/**
 	 * Removes operator rights for a given user. This method will have no effect if polly
      * has insufficient rights on the channel.
      * 
 	 * @param channel The channel on which the users operator rights shall be removed.
 	 * @param nickName The user to deop.
 	 */
 	public abstract void deop(String channel, String nickName);
 
 
 	
 	/**
 	 * <p>Sends a message to the irc. If the channel parameter is not preceded by a '#', 
 	 * the message will be sent to a user with that name.</p>
 	 * 
 	 * <p>Don't bother sending too long messages. Polly will automatically wrap them
 	 * into lines with suitable length. The actual length of the lines is determined by
 	 * pollys configuration.</p>
 	 *  
 	 * @param channel The channel or user to send a message to.
 	 * @param message The message to send.
 	 */
 	public abstract void sendMessage(String channel, String message);
 	
 	
 	/**
      * <p>Sends a message to the irc. If the channel parameter is not preceded by a '#', 
      * the message will be sent to a user with that name.</p>
      * 
      * <p>This method sends messages with a certain priority. Polly uses the source 
      * parameter to distinguish between different message sources and tries to send them
      * in a fair order.</p>
      * 
      * <p>Don't bother sending too long messages. Polly will automatically wrap them
      * into lines with suitable length. The actual length of the lines is determined by
      * pollys configuration.</p>
      *  
      * @param channel The channel or user to send a message to.
      * @param message The message to send.
      * @param source The source of this message. Used for fair message scheduling when
      *      sending multiple messages.
      * @since 0.6.1
      */
 	public abstract void sendMessage(String channel, String message, Object source);
 
 	
 	
 	/**
      * <p>Sends an action to the irc. If the channel parameter is not preceded by a '#', 
      * the action will be sent to a user with that name.</p>
      * 
      * <p>Don't bother sending too long messages. Polly will automatically wrap them
      * into lines with suitable length. The actual length of the lines is determined by
      * pollys configuration.</p>
      *  
      * @param channel The channel or user to send a message to.
      * @param message The message to send.
 	 */
 	public abstract void sendAction(String channel, String message);
 
 	
 	
 	/**
 	 * Sends a raw irc command to the current server.
 	 * 
 	 * @param command the command string to send.
 	 */
 	public abstract void sendRawCommand(String command);
 	
 	
 	
 	/**
 	 * Sets the topic for the specified channel to the specified String. Nothing
 	 * will happen if polly has not the permissions to change the topic.
 	 * 
 	 * @param channel The channel to change the topic on.
 	 * @param topic The new topic.
 	 * @since Beta 0.2
 	 */
 	public abstract void setTopic(String channel, String topic);
 	
 	
 	
 	/**
 	 * Gets the topic for the specified channel. If polly is not on the channel, an
 	 * empty String will be returned.
 	 * 
 	 * @param channel The channel which topic shall be retrieved.
 	 * @return The channels topic.
 	 * @since Beta 0.2
 	 */
 	public abstract String getTopic(String channel);
 	
 	
 	
 	/**
 	 * Gets pollys current set nickname.
 	 * 
 	 * @return the nickname.
 	 */
 	public abstract String getNickname();
 	
 	
 	
 	/**
 	 * Sets polly irc nickname.
 	 * 
 	 *  @param nickname The new nickname.
 	 */
 	public abstract void setNickname(String nickname);
 	
 	
 	
 	/**
 	 * Sets pollys nickname to the default name and identifies with nickserv.
 	 * @since 0.9.1
 	 */
 	public abstract void setAndIdentifyDefaultNickname();
 	
 	
 	
 	/**
 	 * Adds a {@link MessageSendListener}. It will be notified whenever polly sends a 
 	 * message via IRC.
 	 * 
 	 * @param listener The listener to add.
 	 */
 	public abstract void addMessageSendListener(MessageSendListener listener);
 	
 	
 	
 	/**
 	 * Removes a {@link MessageSendListener}.
 	 * 
 	 * @param listener The listener to remove.
 	 * @since 0.7
 	 */
 	public abstract void removeMessageSendListener(MessageSendListener listener);
 	
 	
 	
 	/**
 	 * Adds a {@link NickChangeListener}. It will be notified each time a user changes 
 	 * its nickname.
 	 *   
 	 * @param listener The listener to add.
 	 */
 	public abstract void addNickChangeListener(NickChangeListener listener);
 
 	
 	
 	/**
 	 * Removes a {@link NickChangeListener} listener.
 	 * 
 	 * @param listener The listener to remove.
 	 */
 	public abstract void removeNickChangeListener(NickChangeListener listener);
 
 	
 	
 	/**
 	 * Adds a {@link JoinPartListener}. It will be notified each time a user joins or
 	 * parts a channel that polly is on.
 	 *  
 	 * @param listener The listener to add.
 	 */
 	public abstract void addJoinPartListener(JoinPartListener listener);
 
 	
 	
 	/**
 	 * Removes a {@link JoinPartListener}.
 	 * 
 	 * @param listener The listener to remove.
 	 */
 	public abstract void removeJoinPartListener(JoinPartListener listener);
 
 	
 	
 	/**
 	 * Adds a {@link QuitListener}. It will be notified each time a user quits from irc.
 	 * 
 	 * @param listener The listener to add.
 	 */
 	public abstract void addQuitListener(QuitListener listener);
 
 	
 	
 	/**
 	 * Removes a {@link QuitListener}.
 	 * 
 	 * @param listener The listener to remove.
 	 */
 	public abstract void removeQuitListener(QuitListener listener);
 
 	
 	
 	/**
 	 * Adds a {@link MessageListener}. It will be notified each time a user sends any 
 	 * message to a channel that polly is on.
 	 * 
 	 * @param listener The listener to add.
 	 */
 	public abstract void addMessageListener(MessageListener listener);
 
 	
 	
 	/**
 	 * Removes a {@link MessageListener}.
 	 * 
 	 * @param listener The listener to remove.
 	 */
 	public abstract void removeMessageListener(MessageListener listener);
 	
 	
 	
     /**
      * Adds a {@link ChannelModeListener}. It will be notified each time the mode of a
      * channel that polly is on is changed.
      * 
      * @param listener The listener to add.
      * @since Beta 0.5
      */
 	public abstract void addChannelModeListener(ChannelModeListener listener);
 	
 	
 	
     /**
      * Removes a {@link ChannelModeListener}.
      * 
      * @param listener The listener to remove.
      * @since Beta 0.5
      */
 	public abstract void removeChannelModeListener(ChannelModeListener listener);
 	
 	
 	
 	/**
 	 * Adds a {@link UserSpottedListener}. It will be notified each time a user comes
 	 * in pollys sight for the first time (= join a channel) or leaves pollys sight
 	 * (part last common channel, quit).
 	 * 
 	 * @param listener The listener to add.
 	 * @since 0.6.0
 	 */
 	public abstract void addUserSpottedListener(UserSpottedListener listener);
 	
 	
 	
 	/**
 	 * Removes a {@link UserSpottedListener}.
 	 * 
      * @param listener The listener to remove.
      * @since 0.6.0
 	 */
 	public abstract void removeUserSpottedListener(UserSpottedListener listener);
 	
 	
 	
 	/**
 	 * Adds a {@link ConnectionListener}. It will get notified each time an irc 
 	 * connection is (re)established or lost.
 	 * 
 	 * @param listener The listener to add.
      * @since 0.6.1
 	 */
 	public abstract void addConnectionListener(ConnectionListener listener);
 	
 	
 	
 	/**
 	 * Removes a {@link ConnectionListener}.
 	 * 
 	 * @param listener The listener to remove.
 	 * @since 0.6.1
 	 */
 	public abstract void removeConnectionListener(ConnectionListener listener);
 }
