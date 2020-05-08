 /*
  * Copyright (C) 2012 CyborgDev <cyborg@alta189.com>
  *
  * This file is part of CyborgFactoids
  *
  * CyborgFactoids is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CyborgFactoids is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.alta189.cyborg.factoids;
 
 import com.alta189.cyborg.api.command.CommandContext;
 import com.alta189.cyborg.api.command.CommandResult;
 import com.alta189.cyborg.api.command.CommandSource;
 import com.alta189.cyborg.api.command.annotation.Command;
 import com.alta189.cyborg.api.util.StringUtils;
 import com.alta189.cyborg.factoids.handlers.util.VariableUtil;
 import com.alta189.cyborg.factoids.util.DateUtil;
 
 import static com.alta189.cyborg.api.command.CommandResultUtil.get;
 import static com.alta189.cyborg.factoids.FactoidManager.getDatabase;
 import static com.alta189.cyborg.perms.PermissionManager.hasPerm;
 
 public class FactoidCommands {
 	@Command(name = "remember", desc = "Remembers a factoid", aliases = {"r"})
 	public CommandResult remember(CommandSource source, CommandContext context) {
 		if (source.getSource() != CommandSource.Source.USER) {
 			return new CommandResult().setBody("You cannot register factoids from the terminal");
 		}
 		if (context.getPrefix() == null || !context.getPrefix().equals(".")) {
 			return null;
 		}
 
 		if (hasPerm(source.getUser(), "factoids.deny")) {
 			return get(com.alta189.cyborg.api.command.ReturnType.NOTICE, "You are not allowed to create factoids", source, context);
 		}
 
 		String raw = StringUtils.toString(context.getArgs(), " ");
 
 		String loc = null;
 		String handler = null;
 		String body = null;
 		String name = null;
 		int start = -1;
 		int end = -1;
 
 		name = raw.substring(0, raw.indexOf(" ")).toLowerCase();
 		if (name.startsWith("-")) {
 			return get(com.alta189.cyborg.api.command.ReturnType.NOTICE, "Factoids cannot start with '-'!", source, context);
 		} else if (name.startsWith("+")) {
 			return get(com.alta189.cyborg.api.command.ReturnType.NOTICE, "Factoids cannot start with '+'!", source, context);
 		}
 
 		int firstIndex = raw.indexOf(" ");
 		String first = raw.substring(firstIndex + 1, firstIndex + 2);
 		if (first.equals("<") || first.equals("[")) {
 			if (raw.contains("<") && raw.contains(">")) {
 
 				start = raw.indexOf("<");
 				end = raw.indexOf(">");
 				if (start < end) {
 					handler = raw.substring(start + 1, end).toLowerCase();
 				}
 			}
 
 			if (raw.contains("[") && raw.contains("]")) {
 				int i = raw.indexOf("[");
 				int y = raw.indexOf("]");
 				if (start == -1 || i < start) {
 					if (i < y) {
 						loc = raw.substring(i + 1, y).toLowerCase();
 						if (end == -1) {
 							end = y;
 						}
 					}
 				}
 			}
 		}
 
 		if (loc == null || loc.isEmpty()) {
 			loc = "global";
 		}
 
 		if (handler == null || handler.isEmpty()) {
 			handler = "reply";
 		}
 
 		if (end == -1) {
 			body = raw.substring(raw.indexOf(" ") + 1);
 			boolean test = body.startsWith(" ");
 			while (test) {
 				body = body.substring(1);
 				test = body.startsWith(" ");
 			}
 		} else {
 			body = raw.substring(end + 1);
 			boolean test = body.startsWith(" ");
 			while (test) {
 				body = body.substring(1);
 				test = body.startsWith(" ");
 			}
 		}
 
 		if (loc.equalsIgnoreCase("local") && context.getLocationType() == CommandContext.LocationType.PRIVATE_MESSAGE) {
 			return get(com.alta189.cyborg.api.command.ReturnType.MESSAGE, "You cannot define a local factoid in a private message", source, context);
 		}
 
 		Factoid factoid = new Factoid();
 		factoid.setName(name.toLowerCase());
 		factoid.setLocation(loc.equalsIgnoreCase("local") ? context.getLocation().toLowerCase() : loc.toLowerCase());
 		factoid.setHandler(handler);
 		factoid.setAuthor(source.getUser());
 		factoid.setContents(body);
 		factoid.setTimestamp(DateUtil.getTodayGMTTimestamp());
 
 		if (getDatabase().select(Factoid.class).where().equal("name", factoid.getName()).and().equal("location", factoid.getLocation()).execute().findOne() != null) {
 			return get(com.alta189.cyborg.api.command.ReturnType.MESSAGE,  "Factoid already exists!", source, context);
 		}
 		
 		if (VariableUtil.lineBreakPattern.matcher(factoid.getContents()).find() && !hasPerm(source.getUser(), "factoids.lined")) {
 			return get(com.alta189.cyborg.api.command.ReturnType.NOTICE, "You don't have permission to create multi-lined factoids", source, context);
 		}
 
 		getDatabase().save(Factoid.class, factoid);
 
 		return get(com.alta189.cyborg.api.command.ReturnType.MESSAGE, "Factoid created!", source, context);
 	}
 
 	@Command(name = "know", desc = "Changes a factoid", aliases = {"no", "k"})
 	public CommandResult know(CommandSource source, CommandContext context) {
 		if (source.getSource() != CommandSource.Source.USER) {
 			return get(com.alta189.cyborg.api.command.ReturnType.NOTICE, "You cannot register factoids from the terminal", source, context);
 		}
 		if (context.getPrefix() == null || !context.getPrefix().equals(".")) {
 			return null;
 		}
 
 		if (hasPerm(source.getUser(), "factoids.deny")) {
 			return get(com.alta189.cyborg.api.command.ReturnType.NOTICE, "You are not allowed to change factoids", source, context);
 		}
 
 		String raw = StringUtils.toString(context.getArgs(), " ");
 
 		String loc = null;
 		String handler = null;
 		String body = null;
 		String name = null;
 		int start = -1;
 		int end = -1;
 
 		name = raw.substring(0, raw.indexOf(" ")).toLowerCase();
 		int firstIndex = raw.indexOf(" ");
 		String first = raw.substring(firstIndex + 1, firstIndex + 2);
 		if (first.equals("<") || first.equals("[")) {
 			if (raw.contains("<") && raw.contains(">")) {
 
 				start = raw.indexOf("<");
 				end = raw.indexOf(">");
 				if (start < end) {
 					handler = raw.substring(start + 1, end).toLowerCase();
 				}
 			}
 
 			if (raw.contains("[") && raw.contains("]")) {
 				int i = raw.indexOf("[");
 				int y = raw.indexOf("]");
 				if (start == -1 || i < start) {
 					if (i < y) {
 						loc = raw.substring(i + 1, y).toLowerCase();
 						if (end == -1) {
 							end = y;
 						}
 					}
 				}
 			}
 		}
 
 		if (loc == null || loc.isEmpty()) {
 			loc = "global";
 		}
 
 		if (handler == null || handler.isEmpty()) {
 			handler = "reply";
 		}
 
 		if (end == -1) {
 			body = raw.substring(raw.indexOf(" ") + 1);
 			boolean test = body.startsWith(" ");
 			while (test) {
 				body = body.substring(1);
 				test = body.startsWith(" ");
 			}
 		} else {
 			body = raw.substring(end + 1);
 			boolean test = body.startsWith(" ");
 			while (test) {
 				body = body.substring(1);
 				test = body.startsWith(" ");
 			}
 		}
 
 		if (loc.equalsIgnoreCase("local") && context.getLocationType() == CommandContext.LocationType.PRIVATE_MESSAGE) {
 			return get(com.alta189.cyborg.api.command.ReturnType.MESSAGE, "You cannot change a local factoid in a private message", source, context);
 		}
 
 		Factoid factoid = new Factoid();
 		factoid.setName(name.toLowerCase());
 		factoid.setLocation(loc.equalsIgnoreCase("local") ? context.getLocation().toLowerCase() : loc.toLowerCase());
 		factoid.setHandler(handler);
 		factoid.setAuthor(source.getUser());
 		factoid.setContents(body);
 		factoid.setTimestamp(DateUtil.getTodayGMTTimestamp());
 
 		Factoid old = getDatabase().select(Factoid.class).where().equal("name", factoid.getName()).and().equal("location", factoid.getLocation()).execute().findOne();
 
 		if (old == null) {
 			return get(com.alta189.cyborg.api.command.ReturnType.MESSAGE, "Factoid doesn't exist!", source, context);
 		}
 
 		if (old.isLocked()) {
 			return get(com.alta189.cyborg.api.command.ReturnType.MESSAGE, "Cannot change because the factoid is locked!", source, context);
 		}
 
 		if (old.isForgotten()) {
 			return get(com.alta189.cyborg.api.command.ReturnType.MESSAGE, "Cannot change because the factoid is forgotten!", source, context);
 		}
 
 		if (VariableUtil.lineBreakPattern.matcher(factoid.getContents()).find() && !hasPerm(source.getUser(), "factoids.lined")) {
 			return get(com.alta189.cyborg.api.command.ReturnType.NOTICE, "You don't have permission to create multi-lined factoids", source, context);
 		}
 
 		factoid.setId(old.getId());
 
 		getDatabase().save(Factoid.class, factoid);
 		
 		return get(com.alta189.cyborg.api.command.ReturnType.MESSAGE, "The factoid has been changed!", source, context);
 	}
 
 	@Command(name = "+", desc = "Shows the source of a factoid")
 	public CommandResult source(CommandSource source, CommandContext context) {
 		if (source.getSource() != CommandSource.Source.USER) {
 			return new CommandResult().setBody("You cannot view factoids from the terminal!");
 		}
 		if (context.getPrefix() == null || !(context.getPrefix().equals("!") || context.getPrefix().equals("?"))) {
 			return null;
 		}
 
 		if (context.getArgs() == null || context.getArgs().length < 1) {
 			return get(com.alta189.cyborg.api.command.ReturnType.NOTICE, "Correct usage is !+ factoid", source, context);
 		}
 
 		String loc = null;
 		if (context.getPrefix().equals("!")) {
 			loc = "global";
 		} else if (context.getPrefix().equals("?") && context.getLocationType() == CommandContext.LocationType.CHANNEL) {
 			loc = context.getLocation().toLowerCase();
 		}
 
 		String name = context.getArgs()[0].toLowerCase();
 
 		Factoid factoid = getDatabase().select(Factoid.class).where().equal("name", name).and().equal("location", loc).execute().findOne();
 		if (factoid == null && !loc.equals("global")) {
 			factoid = getDatabase().select(Factoid.class).where().equal("name", name).and().equal("location", "global").execute().findOne();
 		}
 
 		if (factoid == null) {
 			return get(com.alta189.cyborg.api.command.ReturnType.MESSAGE, "Could not find factoid", source, context);
 		}
 
 		return get(com.alta189.cyborg.api.command.ReturnType.MESSAGE, factoid.toString(), source, context);
 	}
 
 	@Command(name = "-", desc = "Shows the source of a factoid")
 	public CommandResult info(CommandSource source, CommandContext context) {
 		if (source.getSource() != CommandSource.Source.USER) {
 			return new CommandResult().setBody("You cannot view factoids from the terminal!");
 		}
 
 		if (context.getPrefix() == null || !(context.getPrefix().equals("!") || context.getPrefix().equals("?"))) {
 			return null;
 		}
 
 		if (context.getArgs() == null || context.getArgs().length < 1) {
 			return get(com.alta189.cyborg.api.command.ReturnType.NOTICE, "Correct usage is !- factoid", source, context);
 		}
 
 		String loc;
 		if (context.getPrefix().equals("!")) {
 			loc = "global";
 		} else if (context.getPrefix().equals("?") && context.getLocationType() == CommandContext.LocationType.CHANNEL) {
 			loc = context.getLocation().toLowerCase();
 		} else {
 			return null;
 		}
 
 		String name = context.getArgs()[0].toLowerCase();
 
 		Factoid factoid = getDatabase().select(Factoid.class).where().equal("name", name).and().equal("location", loc).execute().findOne();
 		if (factoid == null && !loc.equals("global")) {
 			factoid = getDatabase().select(Factoid.class).where().equal("name", name).and().equal("location", "global").execute().findOne();
 		}
 
 		if (factoid == null) {
 			return get(com.alta189.cyborg.api.command.ReturnType.MESSAGE, "Could not find factoid", source, context);
 		}
 
 		return get(com.alta189.cyborg.api.command.ReturnType.MESSAGE, factoid.getInfo(), source, context);
 	}
 
 	@Command(name = "lock", desc = "Shows the source of a factoid", aliases = {"l"})
 	public CommandResult lock(CommandSource source, CommandContext context) {
 		if (source.getSource() != CommandSource.Source.USER) {
 			return new CommandResult().setBody("You cannot lock factoids from the terminal!");
 		}
 		if (context.getPrefix() == null || !context.getPrefix().equals(".")) {
 			return null;
 		}
 
 		if (context.getArgs() == null || context.getArgs().length < 1) {
 			return get(com.alta189.cyborg.api.command.ReturnType.NOTICE, "Correct usage is .lock factoid [global(default)/local]", source, context);
 		}
 
 		if (!hasPerm(source.getUser(), "factoids.lock")) {
 			return get(com.alta189.cyborg.api.command.ReturnType.NOTICE, "You don't have permission!", source, context);
 		}
 
 		String loc = "global";
 		if (context.getArgs().length >= 2) {
 			String raw = context.getArgs()[1];
 			if (raw.startsWith("[") && raw.endsWith("]")) {
 				loc = raw.substring(1, raw.length() - 1).toLowerCase();
 				if (loc.equals("local")) {
 					loc = context.getLocation().toLowerCase();
 				}
 			}
 		}
 
 		String name = context.getArgs()[0].toLowerCase();
 		Factoid factoid = getDatabase().select(Factoid.class).where().equal("name", name).and().equal("location", loc).execute().findOne();
 		if (factoid == null && context.getLocationType() == CommandContext.LocationType.CHANNEL) {
 			factoid = getDatabase().select(Factoid.class).where().equal("name", name).and().equal("location", "global").execute().findOne();
 		}
 
 		if (factoid == null) {
 			return get(com.alta189.cyborg.api.command.ReturnType.MESSAGE, "Could not find factoid", source, context);
 		}
 
 		if (factoid.isLocked()) {
 			factoid.setLocked(false);
 			getDatabase().save(Factoid.class, factoid);
 			return get(com.alta189.cyborg.api.command.ReturnType.MESSAGE, "Factoid is now unlocked!", source, context);
 		} else {
 			factoid.setLocked(true);
 			factoid.setLocker(source.getUser());
 			getDatabase().save(Factoid.class, factoid);
 			return get(com.alta189.cyborg.api.command.ReturnType.MESSAGE, "Factoid is now locked!", source, context);
 		}
 	}
 
 	@Command(name = "forget", desc = "Shows the source of a factoid", aliases = {"f"})
 	public CommandResult forget(CommandSource source, CommandContext context) {
 		if (source.getSource() != CommandSource.Source.USER) {
 			return new CommandResult().setBody("You cannot forget factoids from the terminal!");
 		}
 		if (context.getPrefix() == null || !context.getPrefix().equals(".")) {
 			return null;
 		}
 
 		if (context.getArgs() == null || context.getArgs().length < 1) {
 			return get(com.alta189.cyborg.api.command.ReturnType.NOTICE, "Correct usage is .forget factoid [global(default)/local]", source, context);
 		}
 
 		if (!hasPerm(source.getUser(), "factoids.forget")) {
 			return get(com.alta189.cyborg.api.command.ReturnType.NOTICE, "You don't have permission!", source, context);
 		}
 
 		String loc = "global";
 		if (context.getArgs().length >= 2) {
 			String raw = context.getArgs()[1];
 			if (raw.startsWith("[") && raw.endsWith("]")) {
 				loc = raw.substring(1, raw.length() - 1).toLowerCase();
 				if (loc.equals("local")) {
 					loc = context.getLocation().toLowerCase();
 				}
 			}
 		}
 
 		String name = context.getArgs()[0].toLowerCase();
 		Factoid factoid = getDatabase().select(Factoid.class).where().equal("name", name).and().equal("location", loc).execute().findOne();
 		if (factoid == null && context.getLocationType() == CommandContext.LocationType.CHANNEL) {
 			factoid = getDatabase().select(Factoid.class).where().equal("name", name).and().equal("location", "global").execute().findOne();
 		}
 
 		if (factoid == null) {
 			return get(com.alta189.cyborg.api.command.ReturnType.NOTICE, "Could not find factoid", source, context);
 		}
 
 		if (factoid.isLocked()) {
 			return get(com.alta189.cyborg.api.command.ReturnType.NOTICE, "Cannot change forgotten because factoid is locked!", source, context);
 		}
 
 		if (factoid.isForgotten()) {
 			factoid.setForgotten(false);
 			getDatabase().save(Factoid.class, factoid);
 			return get(com.alta189.cyborg.api.command.ReturnType.NOTICE, "Factoid is now not forgotten!", source, context);
 		} else {
 			factoid.setForgotten(true);
 			factoid.setForgetter(source.getUser());
 			getDatabase().save(Factoid.class, factoid);
 			return get(com.alta189.cyborg.api.command.ReturnType.NOTICE, "Factoid is now forgotten!", source, context);
 		}
 	}
 }
