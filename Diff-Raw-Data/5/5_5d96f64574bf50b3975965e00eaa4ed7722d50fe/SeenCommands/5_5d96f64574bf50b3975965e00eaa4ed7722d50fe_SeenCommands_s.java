 /*
  * Copyright (C) 2012 CyborgDev <cyborg@alta189.com>
  *
  * This file is part of CommandKit
  *
  * CommandKit is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CommandKit is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.alta189.cyborg.commandkit.seen;
 
 import com.alta189.cyborg.api.command.CommandContext;
 import com.alta189.cyborg.api.command.CommandResult;
 import com.alta189.cyborg.api.command.CommandSource;
 import com.alta189.cyborg.api.command.ReturnType;
 import com.alta189.cyborg.api.command.annotation.Command;
 
 import static com.alta189.cyborg.api.command.CommandResultUtil.get;
 import static com.alta189.cyborg.commandkit.CommandKit.getDatabase;
 
 public class SeenCommands {
 
 	@Command(name = "seen", desc = "Command for seeing when someone was last active in a channel")
 	public CommandResult seen(CommandSource source, CommandContext context) {
 		if (source.getSource() != CommandSource.Source.USER) {
 			return null;
 		}
 		if (context.getPrefix() == null || !context.getPrefix().equals(".")) {
 			return null;
 		}
		
		if (context.getLocationType() != CommandContext.LocationType.CHANNEL)
 
 		if (context.getArgs() == null || context.getArgs().length < 1) {
 			return get(ReturnType.NOTICE, "Correct usage is .seen <nick>", source, context);
 		}
 		
 		String nick = context.getArgs()[0].toLowerCase();
 		SeenEntry entry = getDatabase().select(SeenEntry.class).where().equal("name",nick ).and().equal("channel", context.getLocation()).execute().findOne();
 		if (entry == null) {
 			return get(ReturnType.MESSAGE, "I have never seen '" + nick + "' active in this channel!", source, context);
 		}
 		StringBuilder builder = new StringBuilder();
 		builder.append(context.getArgs()[0]).append(" was last seen ").append(entry.getDifference(System.currentTimeMillis())).append(" saying: ").append(entry.getSaying());
 
 		return  get(ReturnType.MESSAGE, builder.toString(), source, context);
 	}
 
 }
