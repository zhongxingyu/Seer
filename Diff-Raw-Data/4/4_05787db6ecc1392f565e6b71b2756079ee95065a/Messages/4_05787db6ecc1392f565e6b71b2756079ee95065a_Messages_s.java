 /*******************************************************************************
 * Copyright (c) 2012 MCForge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
 package net.mcforge.chat;
 
 import java.util.ArrayList;
 
 import net.mcforge.iomodel.Player;
 import net.mcforge.server.Server;
 
 public class Messages {
 	protected Server server;
 	
 	public Messages(Server server) { this.server = server; }
 	
 	/**
 	 * Send a message to all players on the server regardless of world
 	 * 
 	 * @param message
 	 */
 	public void serverBroadcast(String message)
 	{
 		for (Player p : server.players)
 			p.sendMessage(message);
 	}
 	
 	/**
 	 * Send a message to all players in the specified world
 	 * 
 	 * @param message
 	 * @param world
 	 */
 	public void worldBroadcast(String message, String world)
 	{
 		for (Player p : server.players)
 		{
 			if(p.world == world)
 				p.sendMessage(message);
 		}
 	}
 	
 	/**
 	 * Send a message to a specified username
 	 * 
 	 * @param message
 	 * @param playerName
 	 */
 	public void sendMessage(String message, String playerName)
 	{
 		for (Player p : server.players)
 			if(p.username == playerName)
 				p.sendMessage(message);
 	}
 	
 	public String[] split(String message) {
 		if (message.equals(""))
 			return new String[] { "" };
 		char[] array = message.toCharArray();
 		String toadd = "";
 		int last = 0;
 		ArrayList<String> temp = new ArrayList<String>();
 		for (int i = 0; i < array.length; i++) {
 			toadd += "" + array[i];
 			if ((i - last) % 63 == 0 && i != 0) {
 				//Prevent word cutoff
 				int finali = i;
 				while (array[finali] != ' ' && finali >= last) {
 					finali--;
 				}
 				String finals = "";
 				for (int ii = last; ii < finali; ii++) {
 					finals += array[ii];
 				}
 				i = finali;
 				last = i;
 				
 				temp.add(finals);
 				toadd = "";
 				finals = "";
 			}
 			//If the current letter is 1 before a multiply of 64
 			//And the current index + 1 is still less than the array
 			//And the current index is a &
 			//Assume its a color code
 			//And add it to the next line
			else if ((i - last) % 63 == 1 && i + 1 < array.length && array[i] == '&') {
 				toadd += "" + array[i + 1];
 				temp.add(toadd);
 				toadd = "";
 				i++;
 			}
 		}
 		if (!toadd.equals(""))
 			temp.add(toadd);
 		return temp.toArray(new String[temp.size()]);
 	}
 	/**
 	 * Joins the elements of the specified array using the specified separator as a separator
 	 * @param separator - The string to separate the joined elements of the array
 	 * @param array - The string array to join
 	 */
 	public static String join(String[] array, String separator) {
 		String ret = "";
 		for (int i = 0; i < array.length; i++) {
 			ret += array[i] + separator;
 		}
 		return ret.substring(0, ret.length() - separator.length());
 	}
 	/**
 	 * Joins the elements of the specified array using " " as a separator
 	 * @param array - The string array to join
 	 */
 	public static String join(String[] array) {
 		return join(array, " ");
 	}
 }
 
