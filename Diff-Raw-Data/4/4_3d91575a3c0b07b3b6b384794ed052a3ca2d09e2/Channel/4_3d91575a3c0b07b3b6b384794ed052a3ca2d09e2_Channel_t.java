 package fr.ribesg.alix.api;
 import fr.ribesg.alix.api.callback.Callback;
 import fr.ribesg.alix.api.enums.Command;
 import fr.ribesg.alix.api.message.IrcPacket;
 import fr.ribesg.alix.api.message.JoinIrcPacket;
 import fr.ribesg.alix.api.message.NamesIrcPacket;
 import fr.ribesg.alix.internal.callback.internal.NamesCallback;
 import org.apache.log4j.Logger;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.stream.Collectors;
 
 /**
  * Represents an IRC Channel.
  *
  * @author Ribesg
  */
 public class Channel extends Receiver {
 
 	private static final Logger LOGGER = Logger.getLogger(Channel.class.getName());
 
 	/**
 	 * The password of this Channel, if any
 	 */
 	private final String password;
 
 	private String topic;
 
 	private Set<String> users;
 
 	/**
 	 * Channel constructor.
 	 *
 	 * @param server the Server this Channel belongs to
 	 * @param name   the name of the Channel, in the format #foo
 	 */
 	public Channel(final Server server, final String name) {
 		super(server, name);
 		this.password = null;
 	}
 
 	/**
 	 * Password-protected Channel constructor.
 	 *
 	 * @param server   the Server this Channel belongs to
 	 * @param name     the name of the Channel, in the format #foo
 	 * @param password the password of the Channel
 	 */
 	public Channel(final Server server, final String name, final String password) {
 		super(server, name);
 		this.password = password;
 	}
 
 	/**
 	 * @return true if the Channel has a known password, false otherwise
 	 */
 	public boolean hasPassword() {
 		return this.password != null;
 	}
 
 	/**
 	 * @return the password of this Channel, if any
 	 */
 	public String getPassword() {
 		return this.password;
 	}
 
 	/**
 	 * @return the topic of this Channel
 	 */
 	public String getTopic() {
 		return topic;
 	}
 
 	/**
 	 * You should not use this. This is used internally, this doesn't change
 	 * the Server's Channel topic.
 	 *
 	 * @param topic the topic of this Channel
 	 */
 	public void setTopic(final String topic) {
 		this.topic = topic;
 	}
 
 	/**
 	 * @return the users of this Channel, op being represented as @user
 	 * and voices as +user
 	 */
 	public Set<String> getUsers() {
 		return users;
 	}
 
 	/**
 	 * @return the users of this Channel, without op or voiced prefix
 	 */
 	public Set<String> getUserNames() {
 		final Set<String> res = new HashSet<>();
 		for (final String userName : this.users) {
 			if (userName.startsWith("@") || userName.startsWith("+")) {
 				res.add(userName.substring(1));
 			} else {
 				res.add(userName);
 			}
 		}
 		return res;
 	}
 
 	/**
 	 * @return the OP users of this Channel, without the @
 	 */
 	public Set<String> getOps() {
 		return this.users.stream().filter(user -> user.startsWith("@")).map(user -> user.substring(1)).collect(Collectors.toSet());
 	}
 
 	/**
 	 * @return the Voices users of this Channel, without the @
 	 */
 	public Set<String> getVoiced() {
 		return this.users.stream().filter(user -> user.startsWith("+")).map(user -> user.substring(1)).collect(Collectors.toSet());
 	}
 
 	/**
 	 * @param user the user name in any format ('user', '@user', '+user')
 	 *
 	 * @return true if the String '@user' is contained in this Channel's
 	 * user list
 	 */
 	public boolean isOp(final String user) {
 		if (user.startsWith("@") || user.startsWith("+")) {
 			return this.users.contains("@" + user.substring(1));
 		} else {
 			return this.users.contains("@" + user);
 		}
 	}
 
 	/**
 	 * @param user the user name in any format ('user', '@user', '+user')
 	 *
 	 * @return true if the String '+user' is contained in this Channel's
 	 * user list
 	 */
 	public boolean isVoiced(final String user) {
 		if (user.startsWith("@") || user.startsWith("+")) {
 			return this.users.contains("+" + user.substring(1));
 		} else {
 			return this.users.contains("+" + user);
 		}
 	}
 
 	/**
 	 * @param user the user name in any format ('user', '@user', '+user')
 	 *
 	 * @return true if the String '@user' or '+user' is contained in this
 	 * Channel's user list
 	 */
 	public boolean isOpOrVoices(final String user) {
 		if (user.startsWith("@") || user.startsWith("+")) {
 			final String realUser = user.substring(1);
 			return this.users.contains("@" + realUser) || this.users.contains("+" + realUser);
 		} else {
 			return this.users.contains("@" + user) || this.users.contains("+" + user);
 		}
 	}
 
 	/**
 	 * You should not use this. This is used internally to set known users on
 	 * this Channel object.
 	 *
 	 * @param users the Set of Users found on this Channel
 	 */
 	public void setUsers(final Collection<String> users) {
 		if (this.users == null) {
 			this.users = Collections.newSetFromMap(new ConcurrentHashMap<>());
 		}
 		this.users.addAll(users);
 	}
 
 	/**
 	 * You should not used this. This is used internally to update the list
 	 * of users in this Channel.
 	 */
 	public void clearUsers() {
		if (this.users != null) {
			this.users.clear();
		}
 	}
 
 	/**
 	 * Attempt to join this Channel.
 	 *
 	 * @throws IllegalStateException if the Client is not connected
 	 *                               to this Channel's Server
 	 */
 	public void join() {
 		if (getServer().isConnected()) {
 			final Callback joinCallback = new Callback(Command.JOIN.name()) {
 
 				@Override
 				public boolean onIrcPacket(final IrcPacket packet) {
 					final Source user = packet.getPrefixAsSource(this.server);
 					final String channelName = packet.getParameters()[0];
 					if (getName().equals(channelName) && server.getClient().getName().equals(user.getName())) {
 						Channel.this.updateUsers(false);
 						// TODO Other things to update like topic and modes
 						return true;
 					} else {
 						return false;
 					}
 				}
 			};
 
 			if (this.hasPassword()) {
 				this.server.send(new JoinIrcPacket(this.getName(), this.getPassword()), joinCallback);
 			} else {
 				this.server.send(new JoinIrcPacket(this.getName()), joinCallback);
 			}
 		} else {
 			throw new IllegalStateException("Not connected!");
 		}
 	}
 
 	// ###################### //
 	// ## Updating methods ## //
 	// ###################### //
 
 	/**
 	 * This flag indicates if there is an update running for the Users Set.
 	 * It's also use as a mutex to wait for the update to finish.
 	 */
 	private Boolean updatingUsers = false;
 
 	/**
 	 * Update the Users Set.
 	 *
 	 * @param block true if the method should block until the update is done,
 	 *              false otherwise
 	 */
 	public void updateUsers(final boolean block) {
 		if (!updatingUsers) {
 			updatingUsers = true;
 			if (block) {
 				this.server.send(new NamesIrcPacket(this.getName()), true, new NamesCallback(this, updatingUsers));
 				try {
 					updatingUsers.wait();
 				} catch (final InterruptedException e) {
 					LOGGER.error(e);
 				}
 			} else {
 				this.server.send(new NamesIrcPacket(this.getName()), true, new NamesCallback(this));
 			}
 			updatingUsers = false;
 		} else if (block) {
 			try {
 				updatingUsers.wait();
 			} catch (final InterruptedException e) {
 				LOGGER.error(e);
 			}
 		}
 	}
 }
