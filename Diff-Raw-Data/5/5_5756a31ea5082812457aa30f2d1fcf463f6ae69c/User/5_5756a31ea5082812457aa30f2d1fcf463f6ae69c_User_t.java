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
 package fr.hbis.ircs.commands;
 
 import java.util.Date;
 
 import fr.hbis.ircs.Client;
 import fr.hbis.ircs.Command;
 import fr.hbis.ircs.Constants;
 import fr.hbis.ircs.IRC;
 import fr.hbis.ircs.Manager;
 import fr.hbis.ircs.Message;
 import fr.hbis.ircs.Server;
 import fr.hbis.ircs.Source;
 import fr.hbis.ircs.Unknown;
 import fr.hbis.ircs.nio.Connection;
 
 
 /**
  * The class <code>User</code> implements the USER command.
  * 
  * @author bhuisgen
  * 
  */
 public class User extends Command
 {
 	/**
 	 * Construct a new <code>User</code> object.
 	 * 
 	 * @param manager
 	 *            the manager.
 	 */
 	public User (Manager manager)
 	{
 		m_manager = manager;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * fr.hbis.ircs.Command#run(fr.hbis.ircs.Source,
 	 * java.lang.String[])
 	 */
 	public void run (Source src, String[] params)
 	{
 		if (src == null)
 			throw new IllegalArgumentException ("invalid source");
 
 		// USER <username> <hostname> <servername> <realname>
 
 		if ((params == null) || (params.length < 4))
 		{
 			Message message = Message.create (IRC.ERR_NEEDMOREPARAMS, src);
 			message.addParameter ("USER");
 			message.addLastParameter (IRC.ERRMSG_NEEDMOREPARAMS);
 			src.send (message);
 
 			return;
 		}
 
 		doCommand (src, params);
 	}
 
 	/**
 	 * Process the NICK command.
 	 * 
 	 * @param src
 	 *            the source.
 	 * @param params
 	 *            the parameters of the command.
 	 */
 	private void doCommand (Source src, String[] params)
 	{
 		if (src instanceof Unknown)
 		{
 			Unknown unknown = (fr.hbis.ircs.Unknown) src;
 
			if (IRC.MASK_ANY.equals (unknown.getName ()))
 			{
 				unknown.setParameters (params);
 
 				return;
 			}
 
 			Server server = unknown.getServer ();
 			Client client = (Client) unknown.getClient ();
 			Connection connection = client.getConnection ();
 
 			if (!server.login (unknown))
 			{
 				m_manager.disconnectClient (client, "Invalid password");
 
 				return;
 			}
 
 			String username = params[0];
 			String hostname = params[1];
 			String servername = params[2];
 			String realname = params[3];
 
 			fr.hbis.ircs.User user = fr.hbis.ircs.User
 					.createLocal (client, server, unknown.getName (), username,
 							realname, connection.getRemoteHost ());
 
 			client.login (user);
 			m_manager.connectClient (client);
 			broadcastNewUser (user, server);
 
 			Message message = Message.create (IRC.RPL_WELCOME, user);
 			message.addParameter ("Welcome to the "
 					+ server.getNetwork ().toString () + " " + user.getMask ());
 			user.send (message);
 
 			message = Message.create (IRC.RPL_YOURHOST, user);
 			message.addParameter ("Your host is " + server.getName ()
 					+ ", running version " + m_manager.getVersion ());
 			user.send (message);
 
 			message = Message.create (IRC.RPL_CREATED, user);
 			message.addParameter ("This server was created "
 					+ new Date (m_manager.getStartTime ()));
 			user.send (message);
 
 			message = Message.create (IRC.RPL_MYINFO, user);
 			message.addParameter (server.getName () + " "
 					+ m_manager.getVersion () + " "
 					+ fr.hbis.ircs.User.USER_MODES + " "
 					+ fr.hbis.ircs.Channel.CHANNEL_MODES_AVAILABLE);
 			user.send (message);
 
 			message = Message.create (IRC.RPL_ISUPPORT, user);
 			message.addParameter ("NICKLEN=" + IRC.NICKMAXLEN);
 			message.addParameter ("CHANNELLEN=" + IRC.CHANNELMAXLEN);
 			message.addParameter ("TOPICLEN=" + IRC.TOPICMAXLEN);
 			message.addParameter ("PREFIX=(ov)@+");
 			message.addParameter ("CHANTYPES="
 					+ fr.hbis.ircs.Channel.CHANNEL_PREFIXES);
 			message.addParameter ("CHANMODES="
 					+ fr.hbis.ircs.Channel.CHANNEL_MODES_AVAILABLE);
 			message.addParameter ("CASEMAPPING=" + Constants.SERVER_CHARSET);
 			message
 					.addParameter ("NETWORK="
 							+ server.getNetwork ().toString ());
 			message.addParameter ("are supported by this server");
 			user.send (message);
 
 			// LUSERS
 			Command command = m_manager.getCommand ("LUSERS");
 			command.run (user, null);
 
 			// MOTD
 			command = m_manager.getCommand ("MOTD");
 			command.run (user, null);
 		}
 		else
 		{
 			Message message = Message.create (IRC.ERR_ALREADYREGISTRED, src);
 			message.addParameter (IRC.ERRMSG_ALREADYREGISTRED);
 			src.send (message);
 		}
 	}
 
 	/**
	 * Broadcasts a new user connection on all servers of the network.
 	 * 
 	 * @param user
 	 *            the new user
 	 * @param thisServer
 	 *            the server of the user.
 	 */
 	private void broadcastNewUser (fr.hbis.ircs.User user,
 			fr.hbis.ircs.Server thisServer)
 	{
 		for (Server server : thisServer.getNetwork ().getServers ())
 		{
 			if (server == thisServer)
 				continue;
 
 			Message message = Message.create (thisServer, "NICK");
 
 			message.addParameter (user.getNick ());
 			message.addParameter ("1");
 			message.addParameter (user.getUsername ());
 			message.addParameter (user.getHostname ());
 			message.addParameter (Integer.toString (thisServer.getToken ()));
 			message.addParameter (user.getModesList ());
 			message.addParameter (user.getRealname ());
 
 			server.send (message);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.Command#getName()
 	 */
 	public String getName ()
 	{
 		return ("USER");
 	}
 
 	private Manager m_manager;
 }
