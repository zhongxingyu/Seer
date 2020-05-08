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
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * The class <code>Message</code> implements a IRC message.
  * 
  * @author bhuisgen
  * 
  */
 public final class Message
 {
 	/**
 	 * Private constructor.
 	 */
 	private Message ()
 	{
 		m_strSender = null;
 		m_strCommand = null;
 		m_lParams = new ArrayList<String> ();
 		m_bHasLastParam = false;
 	}
 
 	/**
 	 * Create a generic message.
 	 * 
 	 * @param command
 	 *            the command.
 	 * @return the <code>Message</code> object.
 	 */
 	public static final Message create (String command)
 	{
 		if ((command == null) || ("".equals (command)))
 			throw new IllegalArgumentException ("invalid command");
 
 		Message msg = new Message ();
 
 		msg.m_strCommand = command;
 
 		return (msg);
 	}
 
 	/**
 	 * Create a generic message.
 	 * 
 	 * @param sender
 	 *            the sender.
 	 * @param command
 	 *            the command.
 	 * @return the <code>Message</code> object.
 	 */
 	public static final Message create (String sender, String command)
 	{
 		if ((command == null) || ("".equals (command)))
 			throw new IllegalArgumentException ("invalid command");
 
 		Message msg = new Message ();
 
 		msg.m_strSender = sender;
 		msg.m_strCommand = command;
 
 		return (msg);
 	}
 
 	/**
 	 * Create a generic message dedicated to a given target.
 	 * 
 	 * @param command
 	 *            the command.
 	 * @param target
 	 *            the target.
 	 * @return the <code>Message</code> object.
 	 */
 	public static final Message create (String command, Source target)
 	{
 		if ((command == null) || ("".equals (command)))
 			throw new IllegalArgumentException ("invalid command");
 
 		if (target == null)
 			throw new IllegalArgumentException ("invalid target");
 
 		Message msg = new Message ();
 
 		msg.m_strSender = target.getServer ().getName ();
 		msg.m_strCommand = command;
 
 		msg.addParameter (target.getName ());
 
 		return (msg);
 	}
 
 	/**
 	 * Create a server message.
 	 * 
 	 * @param src
 	 *            the sender.
 	 * @param command
 	 *            the command.
 	 * @return the <code>Message</code> object.
 	 */
 	public static final Message create (Server src, String command)
 	{
 		if (src == null)
 			throw new IllegalArgumentException ("invalid source");
 
 		if ((command == null) || ("".equals (command)))
 			throw new IllegalArgumentException ("invalid command");
 
 		Message msg = new Message ();
 
 		msg.m_strSender = src.getName ();
 		msg.m_strCommand = command;
 
 		return (msg);
 	}
 
 	/**
 	 * Create a server message dedicated to a give target.
 	 * 
 	 * @param src
 	 *            the sender.
 	 * @param command
 	 *            the command.
 	 * @param target
 	 *            the target.
 	 * @return the <code>Message</code> object.
 	 */
 	public static final Message create (Server src, String command,
 			Source target)
 	{
 		if (src == null)
 			throw new IllegalArgumentException ("invalid source");
 
 		if ((command == null) || ("".equals (command)))
 			throw new IllegalArgumentException ("invalid command");
 
 		if (target == null)
 			throw new IllegalArgumentException ("invalid target");
 
 		Message msg = new Message ();
 
 		msg.m_strSender = src.getName ();
 		msg.m_strCommand = command;
 
 		msg.addParameter (target.getName ());
 
 		return (msg);
 	}
 
 	/**
 	 * Create a server message dedicated to a channel.
 	 * 
 	 * @param src
 	 *            the sender.
 	 * @param command
 	 *            the command.
 	 * @param target
 	 *            the target.
 	 * @return the <code>Message</code> object.
 	 */
 	public static final Message create (Server src, String command,
 			Channel target)
 	{
 		if (src == null)
 			throw new IllegalArgumentException ("invalid source");
 
 		if ((command == null) || ("".equals (command)))
 			throw new IllegalArgumentException ("invalid command");
 
 		if (target == null)
 			throw new IllegalArgumentException ("invalid target");
 
 		Message msg = new Message ();
 
 		msg.m_strSender = src.getName ();
 		msg.m_strCommand = command;
 
 		msg.addParameter (target.getName ());
 
 		return (msg);
 	}
 
 	/**
 	 * Create an user message.
 	 * 
 	 * @param src
 	 *            the sender.
 	 * @param command
 	 *            the command.
 	 * @return the <code>Message</code> object.
 	 */
 	public static final Message create (User src, String command)
 	{
 		if (src == null)
 			throw new IllegalArgumentException ("invalid source");
 
 		if ((command == null) || ("".equals (command)))
 			throw new IllegalArgumentException ("invalid command");
 
 		Message msg = new Message ();
 
 		msg.m_strSender = src.getMask ();
 		msg.m_strCommand = command;
 
 		return (msg);
 	}
 
 	/**
 	 * Create an user message dedicated to a given target.
 	 * 
 	 * @param src
 	 *            the sender.
 	 * @param command
 	 *            the command.
 	 * @param target
 	 *            the target.
 	 * @return the <code>Message</code> object.
 	 */
 	public static final Message create (User src, String command, Source target)
 	{
 		if (src == null)
 			throw new IllegalArgumentException ("invalid source");
 
 		if ((command == null) || ("".equals (command)))
 			throw new IllegalArgumentException ("invalid command");
 
 		if (target == null)
 			throw new IllegalArgumentException ("invalid target");
 
 		Message msg = new Message ();
 
 		msg.m_strSender = src.getMask ();
 		msg.m_strCommand = command;
 
 		msg.addParameter (target.getName ());
 
 		return (msg);
 	}
 
 	/**
 	 * Create an user message dedicated to a channel.
 	 * 
 	 * @param src
 	 *            the sender.
 	 * @param command
 	 *            the command.
 	 * @param target
 	 *            the target.
 	 * @return the <code>Message</code> object.
 	 */
 	public static final Message create (User src, String command, Channel target)
 	{
 		if (src == null)
 			throw new IllegalArgumentException ("invalid source");
 
 		if ((command == null) || ("".equals (command)))
 			throw new IllegalArgumentException ("invalid command");
 
 		if (target == null)
 			throw new IllegalArgumentException ("invalid target");
 
 		Message msg = new Message ();
 
 		msg.m_strSender = src.getMask ();
 		msg.m_strCommand = command;
 
 		msg.addParameter (target.getName ());
 
 		return (msg);
 	}
 
 	/**
 	 * Add a parameter to the message.
 	 * 
 	 * @param parameter
 	 *            the parameter
 	 */
 	public void addParameter (String parameter)
 	{
 		if ((parameter == null) || ("".equals (parameter)))
 			throw new IllegalArgumentException ("invalid parameter");
		
 		if (m_bHasLastParam)
 			throw new IllegalStateException (
 					"last parameter has been already added");
 
 		m_lParams.add (parameter);
 	}
 
 	/**
 	 * Add the last paramter of the message.
 	 * 
 	 * @param lastParameter
 	 *            the last parameter.
 	 */
 	public void addLastParameter (String lastParameter)
 	{
 		if ((lastParameter == null) || ("".equals (lastParameter)))
 			throw new IllegalArgumentException ("invalid parameter");
		
 		if (m_bHasLastParam)
 			throw new IllegalStateException (
 					"last parameter has been already added");
 
 		m_lParams.add (lastParameter);
 		m_bHasLastParam = true;
 	}
 
 	/**
 	 * Returns a parameter of the message.
 	 * 
 	 * @param index
 	 *            the index of the parameter
 	 * @return the parameter at the given index.
 	 */
 	public String getParameter (int index)
 	{
 		return (m_lParams.get (index));
 	}
 
 	/**
 	 * Returns the parameters array of the message.
 	 * 
 	 * @return the array of parameters.
 	 */
 	public String[] getParameters ()
 	{
 		return (m_lParams.toArray (new String[0]));
 	}
 
 	/**
 	 * Returns the parameters count of the message.
 	 * 
 	 * @return the parameters count.
 	 */
 	public int getParametersCount ()
 	{
 		return (m_lParams.size ());
 	}
 
 	/**
 	 * Returns the message.
 	 * 
 	 * @return the message.
 	 */
 	public String getMessage ()
 	{
 		StringBuilder message = new StringBuilder ();
 
 		if (m_strSender != null)
 		{
 			message.append (':').append (m_strSender).append (' ');
 		}
 
 		message.append (m_strCommand);
 
 		if (!m_lParams.isEmpty ())
 		{
 			int lastParamIndex = m_lParams.size () - 1;
 
 			for (int i = 0; i < lastParamIndex; i++)
 			{
 				message.append (' ').append (m_lParams.get (i));
 			}
 
 			String lastParam = m_lParams.get (lastParamIndex);
 
 			if (m_bHasLastParam || (lastParam.indexOf (' ') != -1)
 					|| (lastParam.indexOf (':') != -1))
 			{
 				message.append (" :").append (lastParam);
 			}
 			else
 			{
 				message.append (' ').append (lastParam);
 			}
 		}
 
 		return (message.toString ());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString ()
 	{
 		return (getMessage ());
 	}
 
 	/**
 	 * Parses the string argument as a <code>Message</code>.
 	 * 
 	 * @param str
 	 *            the string
 	 * @return a <code>Message</code> object.
 	 */
 	public static Message parseMessage (String str)
 	{
 		if (str == null)
 			throw new IllegalArgumentException ("invalid string");
		
 		// [:<prefix>] <command>[ <params> ...]
 
 		Message msg;
 		String from = null;
 		String command = null;
 		int startPos = 0;
 		int endPos = 0;
 
 		// prefix
 		if (str.charAt (0) == ':')
 		{
 			endPos = str.indexOf (' ', 2);
 
 			from = str.substring (1, endPos);
 			startPos = endPos + 1;
 		}
 
 		// command
 		endPos = str.indexOf (' ', startPos);
 		if (endPos == -1)
 			command = str.substring (startPos);
 		else
 			command = str.substring (startPos, endPos);
 
 		msg = Message.create (from, command);
 
 		// parameters
 		if (endPos != -1)
 		{
 			int trailingPos = str.indexOf (" :", endPos);
 
 			if (trailingPos == -1)
 				trailingPos = str.length ();
 
 			while (endPos != -1 && endPos < trailingPos)
 			{
 				startPos = endPos + 1;
 				endPos = str.indexOf (' ', startPos);
 
 				if (endPos != -1)
 					msg.addParameter (str.substring (startPos, endPos));
 			}
 
 			if (endPos == -1 && startPos < str.length ())
 				msg.addParameter (str.substring (startPos));
 			else if (trailingPos + 2 < str.length ())
 				msg.addParameter (str.substring (trailingPos + 2));
 		}
 
 		return (msg);
 	}
 
 	/**
 	 * Returns the sender of the message.
 	 * 
 	 * @return the sender of the message.
 	 */
 	public final String getSender ()
 	{
 		return (m_strSender);
 	}
 
 	/**
 	 * Returns the command of the message.
 	 * 
 	 * @return the command of the message.
 	 */
 	public final String getCommand ()
 	{
 		return (m_strCommand);
 	}
 
 	private String m_strSender;
 	private String m_strCommand;
 	private List<String> m_lParams;
 	private boolean m_bHasLastParam;
 }
