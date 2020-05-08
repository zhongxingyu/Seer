 /*
 Copyright 2009 Samuel Marshall
 http://www.leafdigital.com/software/hawthorn/
 
 This file is part of hawthorn.
 
 hawthorn is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 hawthorn is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with hawthorn.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.leafdigital.hawthorn;
 
 /** Message sent when somebody says something. */
 public class SayMessage extends Message
 {
 	private final static String TYPE="SAY";
 	static
 	{
 		try
 		{
 			Message.registerType(TYPE,SayMessage.class);
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 
 	private String message;
 
 	/**
 	 * @param time Time of message
 	 * @param channel Channel of message
 	 * @param ip IP address of user
 	 * @param user User who sent message
 	 * @param displayName Display name of user
 	 * @param message Message text
 	 */
 	SayMessage(long time,String channel,String ip,String user,String displayName,
 		String message)
		{
 		super(time,channel,ip,user,displayName);
 		this.message=message;
		}
 
 	/** @return Message text */
 	public String getMessage()
 	{
 		return message;
 	}
 
 	@Override
 	protected String getExtraJS()
 	{
 		return ",text:'"+Hawthorn.escapeJS(message)+"'";
 	}
 
 	@Override
 	protected String getExtra()
 	{
 		return " "+message;
 	}
 
 	@Override
 	public String getServerCommand()
 	{
 		return TYPE;
 	}
 
 	/**
 	 * @param time Time of message
 	 * @param channel Channel of message
 	 * @param ip IP address of user
 	 * @param user User who sent message
 	 * @param displayName Display name of user
 	 * @param extra Bit that goes after all this in the text
 	 * @return New message
 	 */
 	public static SayMessage parseMessage(long time,String channel,String ip,
 		String user,String displayName,String extra)
 	{
 		return new SayMessage(time,channel,ip,user,displayName,extra);
 	}
 
 }
