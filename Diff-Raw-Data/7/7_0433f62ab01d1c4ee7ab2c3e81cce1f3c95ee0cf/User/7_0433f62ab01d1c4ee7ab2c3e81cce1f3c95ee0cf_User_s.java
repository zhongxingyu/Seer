 /*
  *  hbIRCS
  *  
  *  Copyright 2005 Boris HUISGEN <bhuisgen@hbis.fr>
  * 
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Library General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  */
 
 package fr.hbis.ircs;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * The class <code>User</code> represents a user connected to a server. The
  * class is thread-safe.
  * 
  * @author bhuisgen
  * 
  */
 public final class User extends Source
 {
 	/**
 	 * Constructs a new <code>User</code> object.
 	 */
 	private User ()
 	{
 		m_rwLock = new ReentrantReadWriteLock ();
 		m_client = null;
 		m_server = null;
 		m_strNick = null;
 		m_strHostname = null;
 		m_strUsername = null;
 		m_strRealname = null;
 		m_mode = new Mode ();
 		m_mChannels = new ConcurrentHashMap<String, Channel> ();
 		m_strAwayMessage = null;
 	}
 
 	/**
 	 * Creates a local user.
 	 * 
 	 * @param client
 	 *            the client of the user.
 	 * @param server
 	 *            the server of the user.
 	 * @param nick
 	 *            the nick of the user.
 	 * @param username
 	 *            the username of the user.
 	 * @param realname
 	 *            the realname of the user.
 	 * @param hostname
 	 *            the hostname of the user.
 	 * @return the <code>User</code> object of the user.
 	 */
 	public static final User createLocal (Client client, Server server,
 			String nick, String username, String realname, String hostname)
 	{
 		if (client == null)
 			throw new IllegalArgumentException ("invalid client");
 
 		if (server == null)
 			throw new IllegalArgumentException ("invalid server");
 
 		if ((nick == null) || ("".equals (nick)))
 			throw new IllegalArgumentException ("invalid nick");
 
 		if ((username == null) || ("".equals (username)))
 			throw new IllegalArgumentException ("invalid username");
 
 		if ((realname == null) || ("".equals (realname)))
 			throw new IllegalArgumentException ("invalid realname");
 
 		if ((hostname == null) || ("".equals (hostname)))
 			throw new IllegalArgumentException ("invalid hostname");
 
 		User user = new User ();
 
 		user.m_client = client;
 		user.m_server = server;
 		user.m_strNick = nick;
 		user.m_strUsername = username;
 		user.m_strRealname = realname;
 		user.m_strHostname = hostname;
 
 		m_logger.log (Level.FINE, "new local user created");
 
 		return (user);
 	}
 
 	/**
 	 * Creates a remote user.
 	 * 
 	 * @param server
 	 *            the server of the user.
 	 * @param nick
 	 *            the nick of the user.
 	 * @param username
 	 *            the username of the user.
 	 * @param realname
 	 *            the realname of the user.
 	 * @param hostname
 	 *            the hostname of the user.
 	 * @return the <code>User</code> object of the user.
 	 */
 	public static final User createRemote (Server server, String nick,
 			String username, String realname, String hostname)
 	{
 		if (server == null)
 			throw new IllegalArgumentException ("invalid server");
 
 		if ((nick == null) || ("".equals (nick)))
 			throw new IllegalArgumentException ("invalid nick");
 
 		if ((username == null) || ("".equals (username)))
 			throw new IllegalArgumentException ("invalid username");
 
 		if ((realname == null) || ("".equals (realname)))
 			throw new IllegalArgumentException ("invalid realname");
 
 		if ((hostname == null) || ("".equals (hostname)))
 			throw new IllegalArgumentException ("invalid hostname");
 
 		User user = new User ();
 
 		user.m_server = server;
 		user.m_strNick = nick;
 		user.m_strUsername = username;
 		user.m_strRealname = realname;
 		user.m_strHostname = hostname;
 
 		m_logger.log (Level.FINE, "new remote user created");
 
 		return (user);
 	}
 
 	/**
 	 * Destroys the object.
 	 */
 	public final void destroy ()
 	{
 		m_rwLock.writeLock ().lock ();
 
 		try
 		{
 			m_client = null;
 			m_server = null;
 			m_strNick = null;
 			m_strUsername = null;
 			m_strRealname = null;
 			m_strHostname = null;
 			m_mode = null;
 			m_mChannels.clear ();
 			m_mChannels = null;
 
 			m_logger.log (Level.FINE, "user destroyed");
 		}
 		finally
 		{
 			m_rwLock.writeLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.Source#send(fr.hbis.ircs.Message)
 	 */
 	public void send (Message msg)
 	{
 		if ((msg == null) || ("".equals (msg)))
 			throw new IllegalArgumentException ("invalid message");
 
 		m_rwLock.writeLock ().lock ();
 
 		try
 		{
 			if (m_client != null)
 			{
 				m_client.getConnection ().write (msg.toString ());
 			}
 			else
 			{
 				assert (m_server != null) : "invalid server";
 
 				m_server.send (msg);
 			}
 
 			m_logger.log (Level.INFO, "message sended to user '" + m_strNick
 					+ "': " + msg.toString ());
 		}
 		finally
 		{
 			m_rwLock.writeLock ().unlock ();
 		}
 	}
 
 	/**
 	 * Change the nick of the user.
 	 * 
 	 * @param newNick
 	 *            the new nickname.
 	 */
 	public void changeNick (String newNick)
 	{
 		if ((newNick == null) || ("".equals (newNick)))
 			throw new IllegalArgumentException ("invalid nick");
 
 		m_rwLock.writeLock ().lock ();
 
 		try
 		{
 			String oldNick = m_strNick;
			m_strNick = newNick;
 
 			m_server.changeUserNick (this, m_strNick, newNick);

			m_logger.log (Level.INFO, "user '" + m_strNick + "' change nick: "
 					+ oldNick + " -> " + m_strNick);
 		}
 		finally
 		{
 			m_rwLock.writeLock ().unlock ();
 		}
 	}
 
 	/**
 	 * Change the modes of the user.
 	 * 
 	 * @param mode
 	 *            the modes.
 	 */
 	public void changeMode (String mode)
 	{
 		if (mode == null)
 			throw new IllegalArgumentException ("invalid mode");
 
 		m_rwLock.writeLock ().lock ();
 
 		try
 		{
 			StringBuilder modeLine = new StringBuilder ();
 			boolean flag = false;
 
 			for (int i = 0; i < mode.length (); i++)
 			{
 				char m = mode.charAt (i);
 				boolean done = false;
 
 				switch (m)
 				{
 				case '+':
 				{
 					flag = true;
 					modeLine.append (m);
 
 					break;
 				}
 
 				case '-':
 				{
 					flag = false;
 					modeLine.append (m);
 
 					break;
 				}
 
 				case User.USER_MODE_AWAY:
 				case User.USER_MODE_LOCALOPERATOR:
 					break;
 
 				default:
 				{
 					if (User.USER_MODES.indexOf (m) == -1)
 					{
 						Message message = Message.create (
 								IRC.ERR_UMODEUNKNOWNFLAG, this);
 						message.addLastParameter (IRC.ERRMSG_UMODEUNKNOWNFLAG);
 						send (message);
 
 						m_logger.log (Level.WARNING, "invalid mode from user '"
 								+ m_strNick + "': " + (flag ? "+" : "-") + m);
 
 						break;
 					}
 
 					done = true;
 				}
 				}
 
 				if (done)
 				{
 					if (flag)
 					{
 						m_mode.add (m);
 					}
 					else
 					{
 						m_mode.remove (m);
 					}
 
 					modeLine.append (m);
 				}
 			}
 
 			if (modeLine.length () > 1)
 			{
 				Message message = Message.create (m_server, "MODE", this);
 				message.addParameter (modeLine.toString ());
 				send (message);
 
 				m_logger.log (Level.INFO, "user '" + m_strNick
 						+ "' change mode: " + modeLine.toString ());
 			}
 		}
 		finally
 		{
 			m_rwLock.writeLock ().unlock ();
 		}
 	}
 
 	/**
 	 * Sets of unsets the away message of the user.
 	 * 
 	 * @param message
 	 *            the message to set or <code>null to unset.
 	 */
 	public void changeAway (String message)
 	{
 		m_rwLock.writeLock ().lock ();
 
 		try
 		{
 			m_strAwayMessage = message;
 
 			if (m_strAwayMessage != null)
 			{
 				m_mode.add (User.USER_MODE_AWAY);
 
 				m_logger.log (Level.INFO, "user '" + m_strNick
 						+ "' away enabled");
 			}
 			else
 			{
 				m_mode.remove (User.USER_MODE_AWAY);
 
 				m_logger.log (Level.INFO, "user '" + m_strNick
 						+ "' away disabled");
 			}
 		}
 		finally
 		{
 			m_rwLock.writeLock ().unlock ();
 		}
 	}
 
 	/**
 	 * Checks if a mode is set for this user.
 	 * 
 	 * @param mode
 	 *            the mode to check.
 	 * @return <code>true</code> if the mode is set; <code>false</code>
 	 *         otherwise.
 	 */
 	public boolean isModeSet (char mode)
 	{
 		m_rwLock.readLock ().lock ();
 
 		try
 		{
 			return (m_mode.isModeSet (mode));
 		}
 		finally
 		{
 			m_rwLock.readLock ().unlock ();
 		}
 	}
 
 	/**
 	 * Returns the modes list of the user.
 	 * 
 	 * @return the modes list of the user.
 	 */
 	public String getModesList ()
 	{
 		m_rwLock.readLock ().lock ();
 
 		try
 		{
 			return (m_mode.getModesList ());
 		}
 		finally
 		{
 			m_rwLock.readLock ().unlock ();
 		}
 	}
 
 	/**
 	 * Add a channel to this user.
 	 * 
 	 * @param channel
 	 *            the channel to add.
 	 */
 	public void addChannel (Channel channel)
 	{
 		if (channel == null)
 			throw new IllegalArgumentException ("invalid channel");
 
 		m_rwLock.writeLock ().lock ();
 
 		try
 		{
 			m_mChannels.put (channel.getName (), channel);
 
 			m_logger.log (Level.FINE, "new channel added to user '" + m_strNick
 					+ "': " + channel.getName ());
 		}
 		finally
 		{
 			m_rwLock.writeLock ().unlock ();
 		}
 	}
 
 	/**
 	 * Remove a channel to this user.
 	 * 
 	 * @param channel
 	 *            the channel to remove.
 	 */
 	public void removeChannel (Channel channel)
 	{
 		if (channel == null)
 			throw new IllegalArgumentException ("invalid channel");
 
 		m_rwLock.writeLock ().lock ();
 
 		try
 		{
 			m_mChannels.remove (channel.getName ());
 
 			m_logger.log (Level.FINE, "channel removed from user '" + m_strNick
 					+ "': " + channel.getName ());
 		}
 		finally
 		{
 			m_rwLock.writeLock ().unlock ();
 		}
 	}
 
 	/**
 	 * Returns the channels of the user.
 	 * 
 	 * @return the channels of the user.
 	 */
 	public Channel[] getChannels ()
 	{
 		m_rwLock.readLock ().lock ();
 
 		try
 		{
 			return (m_mChannels.values ().toArray (new Channel[0]));
 		}
 		finally
 		{
 			m_rwLock.readLock ().unlock ();
 		}
 	}
 
 	/**
 	 * Returns the channels count of the user.
 	 * 
 	 * @return the channels count.
 	 */
 	public int getChannelsCount ()
 	{
 		m_rwLock.readLock ().lock ();
 
 		try
 		{
 			return (m_mChannels.size ());
 		}
 		finally
 		{
 			m_rwLock.readLock ().unlock ();
 		}
 	}
 
 	/**
 	 * Returns the mask of the user.
 	 * 
 	 * @return the mask of the user.
 	 */
 	public String getMask ()
 	{
 		m_rwLock.readLock ().lock ();
 
 		try
 		{
 			StringBuilder mask = new StringBuilder ();
 
 			mask.append (getNick ());
 			mask.append (IRC.PREFIX_USERNAME);
 			mask.append (getUsername ());
 			mask.append (IRC.PREFIX_HOSTNAME);
 			mask.append (getHostname ());
 
 			return (mask.toString ());
 		}
 		finally
 		{
 			m_rwLock.readLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString ()
 	{
 		m_rwLock.readLock ().lock ();
 
 		try
 		{
 			return (getMask ());
 		}
 		finally
 		{
 			m_rwLock.readLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.Source#getClient()
 	 */
 	public final Client getClient ()
 	{
 		m_rwLock.readLock ().lock ();
 
 		try
 		{
 			return (m_client);
 		}
 		finally
 		{
 			m_rwLock.readLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.Source#getServer()
 	 */
 	public final Server getServer ()
 	{
 		m_rwLock.readLock ().lock ();
 
 		try
 		{
 			return (m_server);
 		}
 		finally
 		{
 			m_rwLock.readLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.Source#getName()
 	 */
 	public final String getName ()
 	{
 		m_rwLock.readLock ().lock ();
 
 		try
 		{
 			return (getNick ());
 		}
 		finally
 		{
 			m_rwLock.readLock ().unlock ();
 		}
 	}
 
 	/**
 	 * Returns the nick of the user.
 	 * 
 	 * @return the nick of the user.
 	 */
 	public final String getNick ()
 	{
 		m_rwLock.readLock ().lock ();
 
 		try
 		{
 			return (m_strNick);
 		}
 		finally
 		{
 			m_rwLock.readLock ().unlock ();
 		}
 	}
 
 	/**
 	 * Returns the username of the user.
 	 * 
 	 * @return the username.
 	 */
 	public final String getUsername ()
 	{
 		m_rwLock.readLock ().lock ();
 
 		try
 		{
 			return (m_strUsername);
 		}
 		finally
 		{
 			m_rwLock.readLock ().unlock ();
 		}
 	}
 
 	/**
 	 * Returns the realname of the user.
 	 * 
 	 * @return the realname.
 	 */
 	public final String getRealname ()
 	{
 		m_rwLock.readLock ().lock ();
 
 		try
 		{
 			return (m_strRealname);
 		}
 		finally
 		{
 			m_rwLock.readLock ().unlock ();
 		}
 	}
 
 	/**
 	 * Returns the hostname of the user.
 	 * 
 	 * @return the hostname.
 	 */
 	public final String getHostname ()
 	{
 		m_rwLock.readLock ().lock ();
 
 		try
 		{
 			return (m_strHostname);
 		}
 		finally
 		{
 			m_rwLock.readLock ().unlock ();
 		}
 	}
 
 	/**
 	 * Returns the away message of the user.
 	 * 
 	 * @return the away message.
 	 */
 	public final String getAwayMessage ()
 	{
 		m_rwLock.readLock ().lock ();
 
 		try
 		{
 			return (m_strAwayMessage);
 		}
 		finally
 		{
 			m_rwLock.readLock ().unlock ();
 		}
 	}
 
 	private ReentrantReadWriteLock m_rwLock;
 	private Client m_client;
 	private Server m_server;
 	private String m_strNick;
 	private String m_strUsername;
 	private String m_strRealname;
 	private String m_strHostname;
 	private Mode m_mode;
 	private Map<String, Channel> m_mChannels;
 	private String m_strAwayMessage;
 	/* modes */
 	public static final String USER_MODES = "aiwroOs";
 	public static final char USER_MODE_AWAY = 'a';
 	public static final char USER_MODE_INVISIBLE = 'i';
 	public static final char USER_MODE_WALLOPS = 'w';
 	public static final char USER_MODE_RESTRICTED = 'r';
 	public static final char USER_MODE_OPERATOR = 'o';
 	public static final char USER_MODE_LOCALOPERATOR = 'O';
 	public static final char USER_MODE_SERVERNOTICES = 's';
 	private final static Logger m_logger = Logger
 			.getLogger ("fr.hbis.ircs.User");
 
 }
