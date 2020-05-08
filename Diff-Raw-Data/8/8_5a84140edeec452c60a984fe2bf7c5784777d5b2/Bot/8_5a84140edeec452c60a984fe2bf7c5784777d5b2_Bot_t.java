 /**
  * Copyright © 2012 Bruce Cowan <bruce@bcowan.me.uk>
  *
  * This file is part of uc_PircBotX.
  *
  * uc_PircBotX is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * uc_PircBotX is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with uc_PircBotX.  If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.co.unitycoders.pircbotx;
 
 import org.pircbotx.PircBotX;
 import org.pircbotx.hooks.managers.ListenerManager;
 
 import uk.co.unitycoders.pircbotx.commands.JoinsCommand;
 import uk.co.unitycoders.pircbotx.commands.KillerTroutCommand;
 import uk.co.unitycoders.pircbotx.commands.LartCommand;
 import uk.co.unitycoders.pircbotx.commands.RandCommand;
 import uk.co.unitycoders.pircbotx.commands.DateTimeCommand;
 import uk.co.unitycoders.pircbotx.listeners.LinesListener;
 
 public class Bot
 {
 	public static void main(String[] args)
 	{
 		PircBotX bot = new PircBotX();
		ListenerManager<? extends PircBotX> manager = bot.getListenerManager();
 
 		manager.addListener(new DateTimeCommand());
 		manager.addListener(new JoinsCommand());
 		manager.addListener(new KillerTroutCommand());
 		manager.addListener(new LartCommand());
 		manager.addListener(new LinesListener());
 		manager.addListener(new RandCommand());
 
 		try
 		{
 			bot.setName("uc_pircbotx");
 			bot.connect("irc.freenode.net");
 			bot.joinChannel("#unity-coders");
 			bot.setVerbose(true);
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 }
