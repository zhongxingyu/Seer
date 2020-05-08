 /*
 Copyright 2009 Samuel Marshall
 http://www.leafdigital.com/software/hawthorn/
 
 This file is part of Hawthorn.
 
 Hawthorn is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 Hawthorn is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with Hawthorn.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.leafdigital.hawthorn.server;
 
 import java.util.*;
 
 import com.leafdigital.hawthorn.server.HttpServer.Connection;
 
 /** Channel. */
 public class Channel extends HawthornObject
 {
 	private String name;
 
 	/** Indicates that any time/age/number of messages is acceptable */
 	public final static int ANY = -1;
 
 	/** Time to wait for new messages before closing connection */
 	private final static int WAIT_TIME = 60 * 1000;
 
 	/** Time to wait with no activity before assuming a user is absent */
 	private final static int PRESENT_TIMEOUT = 10 * 1000;
 
 	private final static Message[] NO_MESSAGES = {};
 
 	/**
 	 * List of messages remembered in the channel; it remembers messages for a
 	 * certain time
 	 */
 	private LinkedList<Message> messages = new LinkedList<Message>();
 
 	/** List of listeners currently waiting for channel messages */
 	private HashMap<Integer, Listener> listeners =
 		new HashMap<Integer, Listener>();
 
 	/** Index into listeners by user ID */
 	private HashMap<String, LinkedList<Listener>> listenersByUser =
 		new HashMap<String, LinkedList<Listener>>();
 
 	/** Map from banned user ID to time ban expires */
 	private HashMap<String, Long> bans = new HashMap<String, Long>();
 
 	/** Map from user ID of everyone present in the channel */
 	private HashMap<String, UserInfo> present = new HashMap<String, UserInfo>();
 
 	/** Set containing the userid and unique of all messages in this chan */
 	private HashSet<String> uniqueMessages = new HashSet<String>();
 
 	private long lastMessage = 0;
 
 	/** Class handles a user/connection listening for an upcoming message. */
 	private class Listener extends Event
 	{
 		private Connection connection;
 
 		private int timeoutId;
 
 		private long time;
 
 		private String id, user;
 
 		private boolean trusted;
 
 		/**
 		 * Constructs and starts waiting for timeout or message.
 		 *
 		 * @param app Main app object
 		 * @param connection Connection for response
 		 * @param user User ID
 		 * @param trusted True if user is trusted
 		 * @param id ID string
 		 * @param time Time we started waiting for messages
 		 */
 		private Listener(Hawthorn app, Connection connection, String user,
 			boolean trusted, String id, long time)
 		{
 			super(app);
 			this.connection = connection;
 			this.time = time;
 			this.id = id;
 			this.user = user;
 			this.trusted = trusted;
 
 			// Set timer to wait for the event
 			timeoutId =
 				getEventHandler().addTimedEvent(System.currentTimeMillis() + WAIT_TIME,
 					this);
 
 			// Add this to the listeners list
 			listeners.put(timeoutId, this);
 
 			// And add it to the per-user index
 			LinkedList<Listener> existing = listenersByUser.get(user);
 			if(existing == null)
 			{
 				existing = new LinkedList<Listener>();
 				listenersByUser.put(user, existing);
 			}
 			existing.add(this);
 		}
 
 		@Override
 		/*
 		 * * Called when the timeout occurs.
 		 */
 		public void handle() throws OperationException
 		{
 			// Remove from list of listeners
 			synchronized (Channel.this)
 			{
 				// If item wasn't in list anyway, we've already responded, so don't
 				// do anything else
 				if(listeners.remove(timeoutId) == null)
 				{
 					return;
 				}
 			}
 
 			// Send the no-response message
 			sendWaitResponse(connection, id, time, NO_MESSAGES, trusted);
 		}
 
 		/**
 		 * Called when one or more new messages are received.
 		 *
 		 * @param messages Messages
 		 */
 		private void newMessages(Message[] messages)
 		{
 			synchronized (Channel.this)
 			{
 				// If item wasn't in list anyway, we've already responded, so don't
 				// do anything else
 				if(listeners.remove(timeoutId) == null)
 				{
 					return;
 				}
 				// Remove from by-user index
 				LinkedList<Listener> byUser = listenersByUser.get(user);
 				if(byUser != null) // It should not be null, but let's play safe
 				{
 					byUser.remove(this);
 					if(byUser.isEmpty())
 					{
 						listenersByUser.remove(user);
 					}
 				}
 				// Update access time to current in user info
 				UserInfo info = present.get(user);
 				info.access();
 			}
 
 			// Remove event
 			getEventHandler().removeTimedEvent(timeoutId);
 
 			// Send response
 			sendWaitResponse(connection, id, messages[messages.length - 1].getTime(),
 				messages, trusted);
 		}
 	}
 
 	/**
 	 * Details about a user who's currently in the channel.
 	 */
 	private class UserInfo extends Name
 	{
 		private boolean thisServer;
 
 		private long lastAccess;
 
 		/**
 		 * @param thisServer True if user is connected to this server
 		 * @param ip IP address
 		 * @param user User ID
 		 * @param displayName Display name
 		 * @param extra Extra user data
 		 */
 		private UserInfo(boolean thisServer, String ip, String user,
 			String displayName, String extra)
 		{
 			super(user, displayName, extra, ip);
			System.err.println("UserInfo ["+extra+"]");
 			this.thisServer = thisServer;
 			access();
 		}
 
 		/** Updates access time to now */
 		void access()
 		{
 			lastAccess = System.currentTimeMillis();
 		}
 
 		/**
 		 * Updates access time to an arbitrary time; used for wait timeouts. Can
 		 * be reset by later calls to access() - this is intentional because
 		 * 'waiting for a message' counts as access, but not once the wait finishes.
 		 * @param time Time of access
 		 */
 		void access(long time)
 		{
 			lastAccess = time;
 		}
 
 		/** @return True if user has timed out (only applies on owning server) */
 		boolean timedOut()
 		{
 			return thisServer
 				&& lastAccess + PRESENT_TIMEOUT < System.currentTimeMillis();
 		}
 
 		/**
 		 * Obtains a leave message for this user
 		 * @param timeout True for a timeout message
 		 * @return Suitable LeaveMessage
 		 */
 		LeaveMessage newLeaveMessage(boolean timeout)
 		{
 			return new LeaveMessage(System.currentTimeMillis(), getName(), getIP(),
 				getUser(), getApp().getMaskedUser(getUser()), getDisplayName(),
 				getExtra(), timeout);
 		}
 	}
 
 	/**
 	 * @param app App main object
 	 * @param name Name of channel
 	 */
 	public Channel(Hawthorn app, String name)
 	{
 		super(app);
 		this.name = name;
 	}
 
 	/** @return Channel name */
 	public String getName()
 	{
 		return name;
 	}
 
 	/**
 	 * Obtains a unique identifier within this channel (userid + unique id)
 	 * for a UniqueMessage.
 	 * @param m Message
 	 * @return Unique identifier
 	 */
 	private static String getUniqueKey(UniqueMessage m)
 	{
 		return m.getUser() + ":" + m.getUnique();
 	}
 
 	/**
 	 * Cleans up old messsages and sends leave message for timed-out users.
 	 * Called every few seconds.
 	 * @see Channels
 	 * @return True if there are no messages in this channel and nobody is
 	 *   listening so it should be thrown away
 	 */
 	synchronized boolean cleanup()
 	{
 		// Remove old messages
 		long now = System.currentTimeMillis();
 		long then = now - getConfig().getHistoryTime();
 		for(Iterator<Message> i = messages.iterator(); i.hasNext();)
 		{
 			Message m = i.next();
 			if(m.getTime() > then)
 			{
 				break;
 			}
 			if(m instanceof SayMessage)
 			{
 				uniqueMessages.remove(getUniqueKey((SayMessage)m));
 			}
 			i.remove();
 		}
 
 		// Remove users in present list who have timed out
 		LinkedList<UserInfo> timedOut = new LinkedList<UserInfo>();
 		for(UserInfo info : present.values())
 		{
 			if(info.timedOut())
 			{
 				timedOut.add(info);
 			}
 		}
 		for(UserInfo info : timedOut)
 		{
 			LeaveMessage leave = info.newLeaveMessage(true);
 			getApp().getOtherServers().sendMessage(leave);
 			message(leave, false);
 		}
 
 		// Remove old bans
 		for(Iterator<Long> i = bans.values().iterator(); i.hasNext();)
 		{
 			if(i.next() < now)
 			{
 				i.remove();
 			}
 		}
 
 		// If there are no messages and listeners, OK to delete this channel
 		return messages.isEmpty() && listeners.isEmpty() && bans.isEmpty();
 	}
 
 	/**
 	 * Called when a user (local or remote) says something in the channel
 	 *
 	 * @param m Message
 	 * @param remote True if this was from a remote user
 	 */
 	public synchronized void message(Message m, boolean remote)
 	{
 		// Message array
 		Message[] newMessages =
 		{
 			m
 		};
 
 		// Handle presence information
 		if(m instanceof SayMessage)
 		{
 			if(!uniqueMessages.add(getUniqueKey((SayMessage)m)))
 			{
 				// Message is already in channel, so don't add it again
 				return;
 			}
 			UserInfo existing = present.get(m.getUser());
 			if(existing == null)
 			{
 				// User said something, so must be present
 				Message join =
 					new JoinMessage(System.currentTimeMillis(), getName(), m.getIP(),
 						m.getUser(), getApp().getMaskedUser(m.getUser()),
 						m.getDisplayName(), m.getExtra());
 
 				// Note that this autogenerated join message does not need to be
 				// sent to the other servers.
 				newMessages = new Message[]
 				{
 					join, m
 				};
 
 				// Add to presence list
 				present.put(m.getUser(), new UserInfo(!remote, m.getIP(), m.getUser(),
					m.getDisplayName(), m.getExtra()));
 			}
 			else
 			{
 				// Mark that the user said something
 				existing.access();
 			}
 		}
 		else if(m instanceof JoinMessage)
 		{
 			UserInfo existing = present.get(m.getUser());
 			if(existing == null)
 			{
 				// Add to presence list
 				present.put(m.getUser(), new UserInfo(!remote, m.getIP(), m.getUser(),
					m.getDisplayName(), m.getExtra()));
 			}
 			else
 			{
 				// Already joined, so don't send message to listeners
 				return;
 			}
 		}
 		else if(m instanceof LeaveMessage)
 		{
 			// Remove from presence list
 			if(present.remove(m.getUser()) == null)
 			{
 				// They weren't there? Then don't pass on message
 				return;
 			}
 
 			// Note the listeners will automatically be closed by sending this
 			// leave message.
 		}
 		else if(m instanceof BanMessage)
 		{
 			if(!uniqueMessages.add(getUniqueKey((BanMessage)m)))
 			{
 				// Message is already in channel, so don't add it again
 				return;
 			}
 
 			BanMessage ban = (BanMessage)m;
 
 			// See if banned user is in channel
 			UserInfo target = present.remove(ban.getBan());
 			if(target != null)
 			{
 				// User was in channel; generate LeaveMessage
 				newMessages = new Message[]
         {
 					m, target.newLeaveMessage(false)
         };
 			}
 
 			// Remember ban information
 			bans.put(ban.getBan(), ban.getUntil());
 		}
 
 		internalMessage(newMessages);
 	}
 
 	private synchronized void internalMessage(Message[] newMessages)
 	{
 		for(Message m : newMessages)
 		{
 			// Ensure that no two messages have the same time
 			if(m.getTime() == lastMessage)
 			{
 				m.setTime(lastMessage + 1);
 			}
 
 			// Add message
 			messages.add(m);
 			lastMessage = m.getTime();
 
 			// Put message in chat logs if this server is logging
 			if(getConfig().logChat())
 			{
 				getLogger().log(getName(), Logger.Level.NORMAL, m.getLogFormat());
 			}
 		}
 
 		// Pass message(s) to all listeners
 		while(!listeners.isEmpty())
 		{
 			// This sends the message(s) and removes the listener
 			listeners.values().iterator().next().newMessages(newMessages);
 		}
 	}
 
 	/**
 	 * @return A timestamp that can safely be used for requesting future messages
 	 *   if there are no recent ones
 	 */
 	public synchronized long getPreviousTimestamp()
 	{
 		return System.currentTimeMillis()-1;
 	}
 
 	/**
 	 * @param maxAge Maximum age in milliseconds
 	 * @param maxNumber Maximum number of messages (or ANY)
 	 * @param sayOnly If true, returns only standard SAY messages (not JOIN etc)
 	 * @return Array of messages that match the criteria
 	 */
 	public synchronized Message[] recent(int maxAge, int maxNumber,
 		boolean sayOnly)
 	{
 		long then = System.currentTimeMillis() - maxAge;
 		return getSince(then, maxNumber, sayOnly);
 	}
 
 	/**
 	 * Obtains all messages after (but not including) the given time.
 	 * @param then Time
 	 * @param maxNumber Maximum number of messages (or ANY); if limited, only
 	 *   the newest messages will be retrieved
 	 * @param sayOnly If true, returns only standard SAY messages (not JOIN etc)
 	 * @return Array of messages, empty if none
 	 */
 	public synchronized Message[] getSince(long then, int maxNumber,
 		boolean sayOnly)
 	{
 		int count = 0;
 		ListIterator<Message> iterator = messages.listIterator(messages.size());
 		while(iterator.hasPrevious() && (maxNumber == ANY || count < maxNumber))
 		{
 			Message m = iterator.previous();
 			if(m.getTime() <= then)
 			{
 				iterator.next();
 				break;
 			}
 			if(sayOnly && !(m instanceof SayMessage))
 			{
 				continue;
 			}
 			count++;
 		}
 
 		Message[] result = new Message[count];
 		for(int i = 0; i < count; )
 		{
 			Message m = iterator.next();
 			if(sayOnly && !(m instanceof SayMessage))
 			{
 				continue;
 			}
 			result[i++] = m;
 		}
 
 		return result;
 	}
 
 	/**
 	 * Informs the channel that a user is polling it.
 	 * @param ip IP address
 	 * @param user User
 	 * @param displayName Name
 	 * @param extra Extra user data
 	 * @return Delay in milliseconds that the user should wait for new messages
 	 */
 	public synchronized long poll(String ip, String user, String displayName,
 		String extra)
 	{
 		long now = System.currentTimeMillis();
 
 		// User is now present in channel
 		UserInfo existing = present.get(user);
 		if(existing == null)
 		{
 			// Send a join message to local and remote servers
 			JoinMessage join =
 				new JoinMessage(now + 1, getName(), ip, user,
 					getApp().getMaskedUser(user), displayName, extra);
 			getApp().getOtherServers().sendMessage(join);
 			message(join, false);
 			existing = present.get(user);
 		}
 
 		// Work out time we recommend polling again at
 		int minPollTime = getConfig().getMinPollTime(),
 			maxPollTime = getConfig().getMaxPollTime(),
 			pollScaleTime = getConfig().getPollScaleTime();
 		int delay;
 		if(messages.isEmpty())
 		{
 			// If there are no messages at all, use the maximum delay
 			delay = maxPollTime;
 		}
 		else
 		{
 			// Use a delay between min and max, linearly scaled up to pollScaleTime
 			int sinceLast = (int)(now - messages.getLast().getTime());
 			sinceLast = Math.min(sinceLast, pollScaleTime);
 			delay = minPollTime +
 				((maxPollTime - minPollTime) * sinceLast) / pollScaleTime;
 		}
 
 		// Set user so it doesn't time out until after the recommended poll time
 		// (plus a bit)
 		existing.access(now + delay);
 
 		return delay;
 	}
 
 	/**
 	 * @param maxNames Maximum number of names to return or ANY
 	 * @return Names of channel users (note: selection is arbitrary if there are
 	 *         more than maxNames)
 	 */
 	public synchronized Name[] getNames(int maxNames)
 	{
 		Name[] result;
 		if(present.size() < maxNames || maxNames == ANY)
 		{
 			result = new Name[present.size()];
 		}
 		else
 		{
 			result = new Name[maxNames];
 		}
 		Iterator<UserInfo> iterator = present.values().iterator();
 		for(int i = 0; i < result.length; i++)
 		{
 			result[i] = iterator.next();
 		}
 
 		return result;
 	}
 
 	/**
 	 * @param user User ID
 	 * @return True if user is banned
 	 */
 	public synchronized boolean isBanned(String user)
 	{
 		return bans.containsKey(user);
 	}
 
 	/**
 	 * Registers a request for messages on this channel. This marks a user as
 	 * joined to the channel.
 	 *
 	 * @param connection Connection that wants to receive messages
 	 * @param user User ID
 	 * @param displayName User display name
 	 * @param extra Extra user data
 	 * @param id ID number as string
 	 * @param lastTime Time of last message (receives any messages with time
 	 *        greater than this)
 	 * @param trusted True if user is trusted to see user IDs etc
 	 * @throws IllegalArgumentException If you specify too many parameters
 	 */
 	public synchronized void wait(Connection connection, String user,
 		String displayName, String extra, String id, long lastTime, boolean trusted)
 		throws IllegalArgumentException
 	{
 		// Looking for messages since the specified time
 		ListIterator<Message> iterator = messages.listIterator(messages.size());
 		int count = 0;
 		while(iterator.hasPrevious())
 		{
 			Message m = iterator.previous();
 			if(m.getTime() <= lastTime)
 			{
 				iterator.next();
 				break;
 			}
 			count++;
 		}
 
 		if(count > 0)
 		{
 			Message[] result = new Message[count];
 			for(int i = 0; i < count; i++)
 			{
 				result[i] = iterator.next();
 			}
 			sendWaitResponse(connection, id, result[result.length - 1]
 				.getTime(), result, trusted);
 			// When it responds straight away, we don't need for the user to
 			// be present, because this is only equivalent to recent anyhow.
 			// If they're really in the channel they will send another request
 			// immediately.
 			return;
 		}
 
 		// Keep waiting for new messages
 		new Listener(getApp(), connection, user, trusted, id, lastTime);
 
 		// User is now present in channel
 		UserInfo existing = present.get(user);
 		if(existing == null)
 		{
 			String ip = connection.toString();
 
 			// Send a join message to local and remote servers
 			JoinMessage join =
 				new JoinMessage(System.currentTimeMillis() + 1, getName(), ip, user,
 					getApp().getMaskedUser(user), displayName, extra);
 			getApp().getOtherServers().sendMessage(join);
 			message(join, false);
 			existing=present.get(user);
 		}
 
 		// Set it not to timeout until the wait expires
 		existing.access(System.currentTimeMillis()+WAIT_TIME);
 	}
 
 	private void sendWaitResponse(Connection connection, String id,
 		long lastTime, Message[] messages, boolean trusted)
 	{
 		StringBuilder output = new StringBuilder();
 		output.append("hawthorn.waitComplete(");
 		output.append(id);
 		output.append(',');
 		output.append(lastTime);
 		output.append(",[");
 		for(int i = 0; i < messages.length; i++)
 		{
 			if(i != 0)
 			{
 				output.append(',');
 			}
 			output.append(messages[i].getJSFormat(trusted));
 		}
 		output.append("]);");
 		connection.send(output.toString());
 	}
 }
