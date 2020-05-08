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
 
 import fr.hbis.ircs.Command;
 import fr.hbis.ircs.IRC;
 import fr.hbis.ircs.Manager;
 import fr.hbis.ircs.Message;
 import fr.hbis.ircs.Source;
 
 /**
  * The class <code>Lusers</code> implements the LUSERS command.
  * 
  * @author bhuisgen
  */
 public class Lusers extends Command
 {
 	/**
 	 * Constructs a new <code>Lusers</code> object.
 	 * 
 	 * @param manager
 	 *            the manager.
 	 */
 	public Lusers (Manager manager)
 	{
 		m_manager = manager;
 		m_nMaxGlobal = -1;
 		m_nMaxLocal = -1;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.Command#execute(fr.hbis.ircs.Source,
 	 * java.lang.String[])
 	 */
 	public void execute (Source src, String[] params)
 	{
 		if (src == null)
 			throw new IllegalArgumentException ("invalid source");
 
 		// LUSERS [<mask>[ <target]]
 
 		doCommand (src, params);
 	}
 
 	/**
 	 * Processes the LUSERS command.
 	 * 
 	 * @param src
 	 *            the source.
 	 * @param params
 	 *            the parameters of the command.
 	 */
 	private void doCommand (Source src, String[] params)
 	{
 		if (src instanceof fr.hbis.ircs.User)
 		{
 			if ((params == null) || (params.length == 0))
 			{
 				fr.hbis.ircs.Server server = src.getServer ();
 				fr.hbis.ircs.Network network = server.getNetwork ();
 
 				int serverVisibleCount = server.getUsersCount (
 						fr.hbis.ircs.User.USER_MODE_INVISIBLE, false);
 				int serverInvisibleCount = server.getUsersCount (
 						fr.hbis.ircs.User.USER_MODE_INVISIBLE, true);
 				int curLocal = serverVisibleCount + serverInvisibleCount;
 
 				if (curLocal > m_nMaxLocal)
 					m_nMaxLocal = curLocal;
 
 				int networkVisibleCount = network.getUsersCount (
 						fr.hbis.ircs.User.USER_MODE_INVISIBLE, false);
 				int networkInvisibleCount = network.getUsersCount (
 						fr.hbis.ircs.User.USER_MODE_INVISIBLE, true);
 				int curGlobal = networkVisibleCount + networkInvisibleCount;
 
 				if (curGlobal > m_nMaxGlobal)
 					m_nMaxGlobal = curGlobal;
 
 				Message message = Message.create (IRC.RPL_LUSERCLIENT, src);
 				message.addLastParameter ("There are " + networkVisibleCount
 						+ " users and " + networkInvisibleCount
 						+ " invisible on " + network.getServersCount ()
 						+ " servers");
 				src.send (message);
 
 				message = Message.create (IRC.RPL_LUSEROP, src);
				message.addParameter (Integer.toString (network
 						.getUsersCount (fr.hbis.ircs.User.USER_MODE_OPERATOR,
								true)));
				message.addLastParameter ("operator(s) online");
 				src.send (message);
 
 				message = Message.create (IRC.RPL_LUSERCHANNELS, src);
 				message.addParameter (Integer.toString (network
 						.getChannelsCount ()));
 				message.addLastParameter ("channels formed");
 				src.send (message);
 
 				message = Message.create (IRC.RPL_LUSERME, src);
 				message.addLastParameter ("I have " + serverVisibleCount
 						+ " clients and " + 0 + " servers.");
 				src.send (message);
 
 				message = Message.create ("265", src);
 				message.addLastParameter ("Current local users: " + curLocal
 						+ " Max: " + m_nMaxLocal);
 				src.send (message);
 
 				message = Message.create ("266", src);
 				message.addLastParameter ("Current global users: " + curGlobal
 						+ " Max: " + m_nMaxGlobal);
 				src.send (message);
 			}
 			else
 			{
 
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.Command#getName()
 	 */
 	public String getName ()
 	{
 		return ("LUSERS");
 	}
 
 	private Manager m_manager;
 	private int m_nMaxLocal;
 	private int m_nMaxGlobal;
 }
