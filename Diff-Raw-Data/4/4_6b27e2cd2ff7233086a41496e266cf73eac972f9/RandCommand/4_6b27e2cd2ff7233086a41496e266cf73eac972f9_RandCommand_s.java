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
 package uk.co.unitycoders.pircbotx.commands;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.pircbotx.PircBotX;
 import org.pircbotx.hooks.ListenerAdapter;
 import org.pircbotx.hooks.events.MessageEvent;
 
 /**
  * Keeps a log of all the lines said, and randomly speaks one
  *
  * @author Bruce Cowan
  */
 public class RandCommand extends ListenerAdapter<PircBotX>
 {
 	private ArrayList<String> lines;
 	private Random random;
 
 	public RandCommand()
 	{
 		this.lines = new ArrayList<String>();
 		this.random = new Random();
 	}
 
 	@Override
 	public void onMessage(MessageEvent<PircBotX> event) throws Exception
 	{
 		String msg = event.getMessage();
 
 		if (msg.startsWith("!rand"))
 		{
 			int size = this.lines.size();
 			int index = this.random.nextInt(size - 1);
 			event.respond(this.lines.get(index));
 		}
 	}
 }
