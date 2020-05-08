 /*	
  * Copyright 2010 Mex (ellism88@gmail.com)
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
 
 package Admin;
 
 import java.io.File;
 
 import com.PluggableBot.plugin.DefaultPlugin;
 
 /**
  * 
  * @author Mex (ellism88@gmail.com) 2010
  * 
  */
 public class Admin extends DefaultPlugin {
 
 	public static final String ACTION_STRING = "!";
 	public static final String ACTION_UNLOAD = ACTION_STRING + "unload";
 	public static final String ACTION_LOAD = ACTION_STRING + "load";
 	public static final String ACTION_RELOAD = ACTION_STRING + "reload";
 	public static final String ACTION_PART = ACTION_STRING + "part";
 	public static final String ACTION_JOIN = ACTION_STRING + "join";
 	public static final String ACTION_LIST = ACTION_STRING + "list";
 
	public Admin() {
		super();
 		bot.addAdminCommand(ACTION_JOIN, this);
 		bot.addAdminCommand(ACTION_LOAD, this);
 		bot.addAdminCommand(ACTION_UNLOAD, this);
 		bot.addAdminCommand(ACTION_RELOAD, this);
 		bot.addAdminCommand(ACTION_PART, this);
 		bot.addAdminCommand(ACTION_LIST, this);
 	}

 	@Override
 	public String getHelp() {
 		return "Like I would tell you!";
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.PluggableBot.plugin.DefaultPlugin#getAdminHelp()
 	 */
 	@Override
 	public String getAdminHelp() {
 		return ("Admin Commands are: " + ACTION_STRING + ACTION_UNLOAD + ", "
 				+ ACTION_STRING + ACTION_LOAD + ", " + ACTION_STRING
 				+ ACTION_RELOAD + ", " + ACTION_STRING + ACTION_JOIN + ", "
 				+ ACTION_STRING + ACTION_PART);
 	}
 
 	
 	@Override
 	public void onPrivateAdminCommand(String command, String sender,
 			String login, String hostname, String message) {
 		message = message.trim();
 		if (command.equals(ACTION_UNLOAD)) {
 			bot.unloadPlugin(message);
 			bot.Message(sender, "unloaded " + message);
 		} else if (command.equals(ACTION_RELOAD)) {
 			bot.unloadPlugin(message);
 			bot.loadPlugin(message);
 			bot.Message(sender, "reloaded " + message);
 		} else if (command.equals(ACTION_LOAD)) {
 			bot.loadPlugin(message);
 			bot.Message(sender, "Tried to load " + message);
 		} else if (command.equals(ACTION_RELOAD)) {
 			bot.unloadPlugin(message.trim());
 			bot.loadPlugin(message.trim());
 			bot.Message(sender, "reloaded " + message.trim());
 		} else if (command.equals(ACTION_JOIN)) {
 			bot.joinChannel(message);
 			bot.Message(sender, "joined " + message);
 		}else if (command.equals(ACTION_PART)) {
 			bot.partChannel(message);
 			bot.Message(sender, "left channel " + message);
 		}else if (command.equals(ACTION_LIST)) {
 			File pluginsFolder = new File("./plugins");
 			for (String f : pluginsFolder.list()) {
 				if (f.endsWith(".jar")) {
 					bot.Message(sender, f.substring(0, f.length() - 4));
 				}
 			}
 		}
 
 	}
 
 }
