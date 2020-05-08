 /**
  * This file is part of DutchBot.
  *
  * DutchBot is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * DutchBot is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with DutchBot.  If not, see <http://www.gnu.org/licenses/>.
  *
  * @author DutchDude
  * @copyright © 2012, DutchDude
  * 
  * You are encouraged to send any changes you make to this code to the
  * author. See http://github.com/DutchDude/DutchBot.git
  */
 package cd.what.DutchBot;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 
 import cd.what.DutchBot.Events.IChannelJoinEvent;
 import cd.what.DutchBot.Events.IChannelKickEvent;
 import cd.what.DutchBot.Events.IChannelMessageEvent;
 import cd.what.DutchBot.Events.IInviteEvent;
 import cd.what.DutchBot.Events.IPartEvent;
 import cd.what.DutchBot.Events.IPrivateMessageEvent;
 import cd.what.DutchBot.Events.IQuitEvent;
 
 /**
  * Loads all modules and keeps track of them
  * 
  * @author DutchDude
  * 
  */
 public final class ModuleManager {
 
 	/**
 	 * DutchBot instance
 	 */
 	private final DutchBot bot;
 
 	/**
 	 * List with modules
 	 */
 	private final ArrayList<ModuleAbstract> moduleList = new ArrayList<ModuleAbstract>();
 
 	/**
 	 * 
 	 * @param bot
 	 */
 	public ModuleManager(DutchBot bot) {
 		this.bot = bot;
 	}
 
 	/**
 	 * Attempts to load a certain module
 	 * 
 	 * @param Modulename
 	 *            , must be correct Class name
 	 * 
 	 * @throws SecurityException
 	 * @throws NoSuchMethodException
 	 * @throws InvocationTargetException
 	 * @throws IllegalArgumentException
 	 * @throws IllegalAccessException
 	 * @throws InstantiationException
 	 * 
 	 */
 	public void loadModule(String module) throws ClassNotFoundException,
 			NoSuchMethodException, SecurityException, InstantiationException,
 			IllegalAccessException, IllegalArgumentException,
 			InvocationTargetException {
 		@SuppressWarnings("unchecked")
 		Class<ModuleAbstract> o = (Class<ModuleAbstract>) Class.forName(bot
 				.getConfig().getString("modules.namespace",
						"cd.what.DutchBot.Modules")
				+ module);
 		Class<?>[] args = new Class[1];
 		args[0] = DutchBot.class;
 		ModuleAbstract m = o.getConstructor(args).newInstance(bot);
 		m.init();
 		this.moduleList.add(m);
 
 		if ((m instanceof IPrivateMessageEvent || m instanceof IInviteEvent)
 				&& bot.getModuleManager() != this) {
 			bot.getModuleManager().moduleList.add(m);
 		}
 	}
 
 	/**
 	 * Notify the loaded modules implementing IChannelMessageEvent of an event
 	 * 
 	 * @param channel
 	 * @param sender
 	 * @param login
 	 * @param hostname
 	 * @param message
 	 */
 	public void notifyChannelMessageEvent(String channel, String sender,
 			String login, String hostname, String message) {
 		for (ModuleAbstract m : this.moduleList) {
 			if (m instanceof IChannelMessageEvent)
 				((IChannelMessageEvent) m).notifyChannelMessageEvent(channel,
 						sender, login, hostname, message);
 		}
 	}
 
 	/**
 	 * Notify the loaded modules implementing IChannelJoinEvent of an event
 	 * 
 	 * @param channel
 	 * @param sender
 	 * @param login
 	 * @param hostname
 	 */
 	public void notifyChannelJoinEvent(String channel, String sender,
 			String login, String hostname) {
 		for (ModuleAbstract m : this.moduleList) {
 			if (m instanceof IChannelJoinEvent)
 				((IChannelJoinEvent) m).notifyChannelJoinEvent(channel, sender,
 						login, hostname);
 		}
 
 	}
 
 	/**
 	 * Notify the loaded modules implementing IPrivateMessageEvent of an event
 	 * 
 	 * @param sender
 	 * @param login
 	 * @param hostname
 	 * @param message
 	 */
 	public void notifyPrivateMessageEvent(String sender, String login,
 			String hostname, String message) {
 		this.bot.logMessage("Triggered PrivateMessageEvent from " + sender
 				+ ": " + message);
 		for (ModuleAbstract m : this.moduleList) {
 			if (m instanceof IPrivateMessageEvent)
 				((IPrivateMessageEvent) m).notifyPrivateMessageEvent(sender,
 						login, hostname, message);
 		}
 
 	}
 
 	/**
 	 * Notify the loaded modules implementing IInviteEvent of an invite
 	 * 
 	 * @param targetNick
 	 * @param sourceNick
 	 * @param sourceLogin
 	 * @param sourceHostname
 	 * @param channel
 	 */
 	public void notifyInviteEvent(String targetNick, String sourceNick,
 			String sourceLogin, String sourceHostname, String channel) {
 		for (ModuleAbstract m : this.moduleList) {
 			if (m instanceof IInviteEvent)
 				((IInviteEvent) m).notifyInviteEvent(targetNick, sourceNick,
 						sourceLogin, sourceHostname, channel);
 		}
 
 	}
 
 	/**
 	 * Notify the loaded modules implementing IChannelKickEvent of an event
 	 * 
 	 * @param channel
 	 * @param kickerNick
 	 * @param kickerLogin
 	 * @param kickerHostname
 	 * @param recipientNick
 	 * @param reason
 	 */
 	public void notifyChannelKickEvent(String channel, String kickerNick,
 			String kickerLogin, String kickerHostname, String recipientNick,
 			String reason) {
 		for (ModuleAbstract m : this.moduleList) {
 			if (m instanceof IChannelKickEvent)
 				((IChannelKickEvent) m).notifyChannelKickEvent(channel,
 						kickerNick, kickerLogin, kickerHostname, recipientNick,
 						reason);
 		}
 
 	}
 
 	/**
 	 * @param sourceNick
 	 * @param sourceLogin
 	 * @param sourceHostname
 	 * @param reason
 	 */
 	public void notifyQuitEvent(String sourceNick, String sourceLogin,
 			String sourceHostname, String reason) {
 		for (ModuleAbstract m : this.moduleList) {
 			if (m instanceof IQuitEvent)
 				((IQuitEvent) m).notifyQuitEvent(sourceNick, sourceLogin,
 						sourceHostname, reason);
 		}
 
 	}
 
 	/**
 	 * @param channel
 	 * @param sender
 	 * @param login
 	 * @param hostname
 	 */
 	public void notifyPartEvent(String channel, String sender, String login,
 			String hostname) {
 		for (ModuleAbstract m : this.moduleList) {
 			if (m instanceof IPartEvent)
 				((IPartEvent) m).notifyPartEvent(channel, sender, login,
 						hostname);
 		}
 	}
 }
