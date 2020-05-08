 /*	
  * Copyright 2007 andee
  * Copyright 2009 Mex (ellism88@gmail.com)
  * 
  *   This program is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package Kill;
 
 /*
  * KillPlugin.java
  *
  * Created on 10 October 2007, 13:18
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 import java.util.List;
 import java.util.logging.Logger;
 
 import com.PluggableBot.plugin.DefaultPlugin;
 import com.db4o.Db4o;
 import com.db4o.ObjectContainer;
 import com.db4o.ObjectSet;
 
 /**
  * Plugin to let you !kill people
  * 
  * @author AndyC 2007
  * @author Martin Ellis (ellism88@gmail.com) 2009
  */
 public class Kill extends DefaultPlugin {
 
 	private ObjectContainer database;
 	
 	private static Logger log = Logger.getLogger(Kill.class.getName());
 
 	/** 
 	 * Creates a new instance of KillPlugin 
 	 */
 	public Kill() {
 		log.info("Kill: loading database");
 		database = Db4o.openFile("Kill.db4o");
 		Db4o.configure().objectClass(KillLists.class).cascadeOnUpdate(true);
 		log.info("Kill: database open");
 	}
 
 	/**
 	 * 
 	 * @param sender
 	 * @return
 	 */
 	private KillLists getKillList(String sender) {
 		KillLists proto = new KillLists(sender);
 		ObjectSet<KillLists> killsContainer = database.get(proto);
 		if (killsContainer.size() == 1) {
 			return killsContainer.get(0);
 		} else {
 			return proto;
 		}
 	}
 
 	private void kill(String sender, String message, String channel) {
 
 		String randomKill = getKillList(sender).getRandomKill();
 		String target = message.substring(6).trim();
 		if (target.toLowerCase().equals(bot.Nick().toLowerCase()))
 			target = sender;
 		bot.Action(channel, randomKill.replaceAll("%NAME", target));
 	}
 
 	/**
 	 * 
 	 * @param sender
 	 * @param message
 	 */
 	private void addKill(String sender, String message) {
 		KillLists killer = getKillList(sender);
 		List<String> listOfUserKills = killer.getKills();
 		if (!listOfUserKills.contains(message.substring(9))) {
 			listOfUserKills.add(message.substring(9));
 			killer.setKills(listOfUserKills);
 			database.set(killer);
 			database.commit();
 			bot.Message(sender, "Added kill: " + message.substring(9));
 		} else {
 			bot.Message(sender, "Kill '" + message.substring(9)
 					+ "' already exosists");
 		}
 	}
 
 	/**
 	 * List a users kills
 	 * @param sender
 	 */
 	private void listKills(String sender) {
 		KillLists killer = getKillList(sender);
 		List<String> listOfUserKills = killer.getKills();
 		if (listOfUserKills.size() > 0) {
 		
		bot.Message(sender, "You kills are : ");
 		for (int i = 0; i < listOfUserKills.size(); ++i) {
 			bot.Message(sender, i + ": " + listOfUserKills.get(i));
 		}
 		} else {
 			bot.Message(sender, "You have not saved a custom kill yet...");
 			bot.Message(sender, "Save one with !addkill");
 		}
 	}
 
 	/**
 	 * Remove a kill for a users kill list.
 	 * @param sender
 	 * @param message
 	 */
 	private void removeKill(String sender, String message) {
 		try {
 			int remove = Integer.parseInt(message.substring(11).trim());
 			KillLists proto = new KillLists(sender);
 			ObjectSet<KillLists> killsContainer = database.get(proto);
 			if (killsContainer.size() == 1) {
 				KillLists killer = killsContainer.get(0);
 				List<String> listOfUserKills = killer.getKills();
 				if (listOfUserKills.size() > remove) {
 					String removed = listOfUserKills.remove(remove);
 					bot.Message(sender, "kill '" + removed + "' was removed");
					killer.setKills(listOfUserKills);
 					database.set(killer);
 					database.commit();
 				}
 			}
 		} catch (Exception e) {
 		}
 	}
 
 	@Override
 	public void onMessage(String channel, String sender, String login,
 			String hostname, String message) {
 		if (message.startsWith("!kill")) {
 			kill(sender, message, channel);
 		} else if (message.startsWith("!addkill")) {
 			addKill(sender, message);
 		} else if (message.startsWith("!listkills")) {
 			listKills(sender);
 		} else if (message.startsWith("!removekill")) {
 			removeKill(sender, message);
 		}
 	}
 
 	@Override
 	public String getHelp() {
 		return "This allows users to order me to kill other users, by using !kill <username>. To customise your kill message, use !addkill, followed by the attack. use %NAME as a placeholder for a user's nick. !listkills, !removekill <number>";
 	}
 
 	@Override
 	public void onPrivateMessage(String sender, String login, String hostname,
 			String message) {
 		if (message.startsWith("!addkill")) {
 			addKill(sender, message);
 		} else if (message.startsWith("!listkills")) {
 			listKills(sender);
 		} else if (message.startsWith("!removekill")) {
 			removeKill(sender, message);
 		}
 	}
 
 	@Override
 	public void unload() {
 		database.close();
 	}
 
 }
