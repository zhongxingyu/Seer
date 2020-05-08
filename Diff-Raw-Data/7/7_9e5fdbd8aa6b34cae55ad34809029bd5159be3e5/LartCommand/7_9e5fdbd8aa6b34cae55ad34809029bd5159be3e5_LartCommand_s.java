 /**
  * Copyright © 2012 Bruce Cowan <bruce@bcowan.me.uk>
  * Copyright © 2012 Joseph Walton-Rivers <webpigeon@unitycoders.co.uk>
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
 
 import java.sql.SQLException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.pircbotx.PircBotX;
 import org.pircbotx.hooks.events.MessageEvent;
 import uk.co.unitycoders.pircbotx.commandprocessor.Command;
 
 import uk.co.unitycoders.pircbotx.data.db.DBConnection;
 import uk.co.unitycoders.pircbotx.data.db.LartModel;
 import uk.co.unitycoders.pircbotx.types.Lart;
 
 /**
  * Insults the 1st argument.
  *
  * @author Bruce Cowan
  */
 public class LartCommand
 {
 	private LartModel model;
 	private final Pattern re;
 	private final Pattern alterRe;
 
 	/**
 	 * Creates a {@link LartCommand}.
 	 */
 	public LartCommand()
 	{
 		try
 		{
 			this.model = DBConnection.getLartModel();
 		} catch (Exception ex)
 		{
 			ex.printStackTrace();
 		}
 
 		this.re = Pattern.compile(".lart ([^ ]+) *(.*)?");
 		this.alterRe = Pattern.compile("(\\d) (.*)");
 	}
 
 	@Command(keyword="add")
 	public void onAdd(MessageEvent<PircBotX> event) throws Exception
 	{
 		//TODO this bit could still be nicer
 		String msg = event.getMessage();
 		Matcher matcher = re.matcher(msg);
 		String ops = matcher.group(2);
 
 		try
 		{
 			int num = model.storeLart(event.getChannel(), event.getUser(), ops);
 			event.respond("Lart # "+num+" added");
 		} catch (IllegalArgumentException ex)
 		{
 			event.respond("No $who section given");
 		} catch (SQLException ex)
 		{
 			event.respond("Database Error: "+ex.getMessage());
 		}
 	}
 
 	@Command(keyword="delete")
 	public void onDelete(MessageEvent<PircBotX> event) throws Exception
 	{
 		try
 		{
 			int id = toInt(event);
 
 			boolean result = model.deleteLart(id);
 			if (result)
 				event.respond("Deleted lart #"+id);
 			else
 				event.respond("No such lart in database");
 		} catch (RuntimeException ex)
 		{
 			//reading arguments failed
 			//XXX possibly intregrate some kind of command exception to show
 			//usage of command 1 level up?
 		}
 	}
 
 	@Command(keyword="info")
 	public void onInfo(MessageEvent<PircBotX> event) throws Exception
 	{
 		try
 		{
 			int id = toInt(event);
 			Lart lart = model.getLart(id);
 			String resp = String.format("Channel: %s, Nick: %s, Pattern: %s", lart.getChannel(), lart.getNick(), lart.getPattern());
 			event.respond(resp);
 		} catch(SQLException ex)
 		{
 			event.respond("Database Error: "+ex.getMessage());
 		}
 	}
 
 	@Command(keyword="list")
 	public void onList(MessageEvent<PircBotX> event) throws Exception
 	{
 		StringBuilder builder = new StringBuilder();
 
 		if (this.model.getAllLarts().isEmpty())
 			return;
 
 		for (Lart lart : this.model.getAllLarts())
 		{
 			int id = lart.getID();
 			builder.append(id);
 			builder.append(',');
 		}
 
 		builder.deleteCharAt(builder.length() - 1);
 		event.respond(builder.toString());
 	}
 
 	@Command(keyword="alter")
 	public void onAlter(MessageEvent<PircBotX> event) throws Exception
 	{
 		Matcher matcher = alterRe.matcher(event.getMessage());
 		if (!matcher.matches())
 		{
 			event.respond("Invalid Format for alter");
 			return;
 		}
 
 		try
 		{
 			int id = toInt(event);
 			String pattern = matcher.group(2);
 
 			this.model.alterLart(id, event.getChannel(), event.getUser(), pattern);
 			event.respond("Lart #" + id + " altered");
 		} catch (IllegalArgumentException ex)
 		{
 			event.respond("No $who section given");
 			return;
 		} catch (SQLException ex)
 		{
 			event.respond("Failed to add lart: " + ex.getMessage());
 		}
 	}
 
 	/**
 	 * Insults someone.
 	 *
 	 * @param event the event from {@link #onMessage(MessageEvent)}.
 	 */
 	@Command(keyword="default")
 	public void insult(MessageEvent<PircBotX> event)
 	{
 		//TODO deal with exception from this
 		String nick = event.getMessage().split(" ")[1];
 		String insult;
 		try
 		{
 			String pattern = this.model.getRandomLart().getPattern();
 			insult = pattern.replace("$who", nick);
 			event.getBot().sendAction(event.getChannel(), insult);
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	private int toInt(MessageEvent<PircBotX> event)
 	{
 		try
 		{
 			String msg = event.getMessage();
 			Matcher matcher = re.matcher(msg);
 			if (!matcher.matches())
 			{
 				throw new RuntimeException("invalid command format");
 			}
 			return Integer.parseInt(matcher.group(2));
 		} catch(NumberFormatException ex)
 		{
 			event.respond("Couldn't read int from argument");
 			throw new RuntimeException("invalid command");
 		}
 	}
 }
