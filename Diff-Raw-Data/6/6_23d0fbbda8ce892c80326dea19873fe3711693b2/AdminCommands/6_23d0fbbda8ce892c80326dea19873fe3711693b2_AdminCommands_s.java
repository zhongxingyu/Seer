 /*
  * Copyright (C) 2012 CyborgDev <cyborg@alta189.com>
  *
  * This file is part of CyborgAdmin
  *
  * CyborgAdmin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CyborgAdmin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.alta189.cyborg.admin;
 
 import com.alta189.cyborg.Cyborg;
 import com.alta189.cyborg.api.command.CommandContext;
 import com.alta189.cyborg.api.command.CommandResult;
 import com.alta189.cyborg.api.command.CommandSource;
 import com.alta189.cyborg.api.command.ReturnType;
 import com.alta189.cyborg.api.command.annotation.Command;
 import com.alta189.cyborg.api.command.annotation.Usage;
 import com.alta189.cyborg.api.util.StringUtils;
 import org.apache.commons.io.FileUtils;
 import org.pircbotx.Channel;
 
 import static com.alta189.cyborg.api.command.CommandResultUtil.get;
 import static com.alta189.cyborg.perms.PermissionManager.getGroup;
 import static com.alta189.cyborg.perms.PermissionManager.hasPerm;
 
 public class AdminCommands {
 	@Command(name = "disconnect", desc = "Mutes the bot in a specific channel", aliases = {"dc", "quit", "exit"})
 	@Usage(".disconnect")
 	public CommandResult disconnect(CommandSource source, CommandContext context) {
 		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
 			return null;
 		}
 		CommandResult result = new CommandResult();
 		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "admin.echo")) {
 			return result.setReturnType(ReturnType.NOTICE).setBody("You don't have permission!").setTarget(source.getSource() != CommandSource.Source.USER ? null : source.getUser().getNick());
 		}
 		if (context.getArgs() != null && context.getArgs().length >= 1) {
 			Cyborg.getInstance().shutdown(StringUtils.toString(context.getArgs(), " "));
 		} else {
 			Cyborg.getInstance().shutdown();
 		}
 		return null;
 	}
 
 	@Command(name = "joinchannel", desc = "Sends message to target", aliases = {"j", "jc", "join"})
 	@Usage(".joinchannel <channel> [key]")
 	public CommandResult joinchannel(CommandSource source, CommandContext context) {
 		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
 			return null;
 		}
 		CommandResult result = new CommandResult();
 		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "admin.join")) {
 			return result.setReturnType(ReturnType.NOTICE).setBody("You don't have permission!").setTarget(source.getSource() != CommandSource.Source.USER ? null : source.getUser().getNick());
 		}
 		if (context.getArgs() == null || context.getArgs().length < 1) {
 			String body = "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "joinchannel <channel> [key]";
 			return result.setReturnType(ReturnType.NOTICE).setBody(body).setTarget(source.getSource() != CommandSource.Source.USER ? null : source.getUser().getNick());
 		}
 		if (!context.getArgs()[0].startsWith("#")) {
 			return result.setReturnType(ReturnType.NOTICE).setBody("Invalid channel").setTarget(source.getSource() != CommandSource.Source.USER ? null : source.getUser().getNick());
 		}
 		if (context.getArgs().length >= 2) {
 			Cyborg.getInstance().joinChannel(context.getArgs()[0], context.getArgs()[1]);
 		} else {
 			Cyborg.getInstance().joinChannel(context.getArgs()[0]);
 		}
 		return result.setReturnType(ReturnType.MESSAGE).setBody("Joining channel '" + context.getArgs()[0] + "'").setTarget(context.getLocationType() == CommandContext.LocationType.CHANNEL ? context.getLocation() : source.getUser().getNick());
 	}
 
 	@Command(name = "partchannel", desc = "Sends message to target", aliases = {"p", "pc", "part"})
 	@Usage(".partchannel <channel> [key]")
 	public CommandResult partchannel(CommandSource source, CommandContext context) {
 		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
 			return null;
 		}
 		CommandResult result = new CommandResult();
 		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "admin.part")) {
 			return result.setReturnType(ReturnType.NOTICE).setBody("You don't have permission!").setTarget(source.getSource() != CommandSource.Source.USER ? null : source.getUser().getNick());
 		}
 		if (context.getArgs() == null || context.getArgs().length < 1) {
 			String body = "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "partchannel <channel> [reason]";
 			return result.setReturnType(ReturnType.NOTICE).setBody(body).setTarget(source.getSource() != CommandSource.Source.USER ? null : source.getUser().getNick());
 		}
 		if (!context.getArgs()[0].startsWith("#")) {
 			return result.setReturnType(ReturnType.NOTICE).setBody("Invalid channel!").setTarget(source.getSource() != CommandSource.Source.USER ? null : source.getUser().getNick());
 		}
 		Channel channel = Cyborg.getInstance().getChannel(context.getArgs()[0]);
 		if (channel == null) {
 			return result.setReturnType(ReturnType.NOTICE).setBody("I am not in that channel").setTarget(source.getSource() != CommandSource.Source.USER ? null : source.getUser().getNick());
 		}
 		if (context.getArgs().length >= 2) {
 			System.out.println(StringUtils.toString(context.getArgs(), 1, " "));
 			Cyborg.getInstance().partChannel(channel, StringUtils.toString(context.getArgs(), 1, " "));
 		} else {
 			Cyborg.getInstance().partChannel(channel);
 		}
		return result.setReturnType(ReturnType.MESSAGE).setBody("Parting channel '" + context.getArgs()[0] + "'").setTarget(context.getLocationType() == CommandContext.LocationType.CHANNEL ? context.getLocation() : source.getUser().getNick());
 	}
 
 	@Command(name = "echo", desc = "Sends message to target", aliases = {"e", "say"})
 	@Usage(".echo <target> <message>")
 	public CommandResult echo(CommandSource source, CommandContext context) {
 		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
 			return null;
 		}
 		CommandResult result = new CommandResult();
 		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "admin.echo")) {
 			return result.setReturnType(ReturnType.NOTICE).setBody("You don't have permission!").setTarget(source.getSource() != CommandSource.Source.USER ? null : source.getUser().getNick());
 		}
 		if (context.getArgs() == null || context.getArgs().length < 2) {
 			String body = "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "echo <target> <message>";
 			return result.setReturnType(ReturnType.NOTICE).setBody(body).setTarget(source.getSource() != CommandSource.Source.USER ? null : source.getUser().getNick());
 		}
 		String message = StringUtils.toString(context.getArgs(), 1, " ");
 		return result.setReturnType(ReturnType.MESSAGE).setBody(message).setTarget(context.getArgs()[0]).setForced(true);
 	}
 
 	@Command(name = "notice", desc = "Sends notice to target", aliases = {"n"})
 	@Usage(".notice <target> <notice>")
 	public CommandResult notice(CommandSource source, CommandContext context) {
 		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
 			return null;
 		}
 		CommandResult result = new CommandResult();
 		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "admin.notice")) {
 			return result.setReturnType(ReturnType.NOTICE).setBody("You do not have permission!").setTarget(source.getSource() != CommandSource.Source.USER ? null : source.getUser().getNick());
 		}
 		if (context.getArgs() == null || context.getArgs().length < 2) {
 			String body = "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "notice <target> <notice>";
 			return result.setReturnType(ReturnType.NOTICE).setBody(body).setTarget(source.getSource() != CommandSource.Source.USER ? null : source.getUser().getNick());
 		}
 		String message = StringUtils.toString(context.getArgs(), 1, " ");
 		return result.setReturnType(ReturnType.NOTICE).setBody(message).setTarget(context.getArgs()[0]).setForced(true);
 	}
 
 	@Command(name = "action", desc = "Sends action to target", aliases = {"a", "act"})
 	@Usage(".action <target> <action>")
 	public CommandResult action(CommandSource source, CommandContext context) {
 		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
 			return null;
 		}
 		CommandResult result = new CommandResult();
 		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "admin.action")) {
 			return result.setReturnType(ReturnType.NOTICE).setBody("You don't have permission!").setTarget(source.getSource() != CommandSource.Source.USER ? null : source.getUser().getNick());
 		}
 		if (context.getArgs() == null || context.getArgs().length < 2) {
 			String body = "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "action <target> <action>";
 			return result.setReturnType(ReturnType.NOTICE).setBody(body).setTarget(source.getSource() != CommandSource.Source.USER ? null : source.getUser().getNick());
 		}
 		String message = StringUtils.toString(context.getArgs(), 1, " ");
 		return result.setReturnType(ReturnType.ACTION).setBody(message).setTarget(context.getArgs()[0]).setForced(true);
 	}
 
 	@Command(name = "mute", desc = "Mutes the bot in a specific channel")
 	@Usage(".mute <channel>")
 	public CommandResult muteCommand(CommandSource source, CommandContext context) {
 		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
 			return null;
 		}
 		CommandResult result = new CommandResult();
 		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "admin.mute")) {
 			return result.setReturnType(ReturnType.NOTICE).setBody("You don't have permission!").setTarget(source.getSource() != CommandSource.Source.USER ? null : source.getUser().getNick());
 		}
 		if (context.getArgs() == null || context.getArgs().length < 1) {
 			String body = "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "mute <channel>";
 			return result.setReturnType(ReturnType.NOTICE).setBody(body).setTarget(source.getSource() != CommandSource.Source.USER ? null : source.getUser().getNick());
 		}
 		if (!context.getArgs()[0].startsWith("#")) {
 			return result.setReturnType(ReturnType.NOTICE).setBody("Not a valid channel!").setTarget(source.getSource() != CommandSource.Source.USER ? null : source.getUser().getNick());
 		}
 		if (Config.isChannelMuted(context.getArgs()[0])) {
 			return result.setReturnType(ReturnType.NOTICE).setBody("Already muted!").setTarget(source.getSource() != CommandSource.Source.USER ? null : source.getUser().getNick());
 		}
 		Config.addMutedChannel(context.getArgs()[0]);
 		return null;
 	}
 
 	@Command(name = "unmute", desc = "Unutes the bot in a specific channel")
 	@Usage(".unmute <channel>")
 	public CommandResult unmuteCommand(CommandSource source, CommandContext context) {
 		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
 			return null;
 		}
 		CommandResult result = new CommandResult();
 		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "admin.mute")) {
 			return result.setReturnType(ReturnType.NOTICE).setBody("You don't have permission!").setTarget(source.getSource() != CommandSource.Source.USER ? null : source.getUser().getNick());
 		}
 		if (context.getArgs() == null || context.getArgs().length < 1) {
 			String body = "Correct usage is " + (source.getSource() == CommandSource.Source.USER ? "." : "") + "mute <channel>";
 			return result.setReturnType(ReturnType.NOTICE).setBody(body).setTarget(source.getSource() != CommandSource.Source.USER ? null : source.getUser().getNick());
 		}
 		if (!context.getArgs()[0].startsWith("#")) {
 			return result.setReturnType(ReturnType.NOTICE).setBody("Not a valid channel!").setTarget(source.getSource() != CommandSource.Source.USER ? null : source.getUser().getNick());
 		}
 		if (!Config.isChannelMuted(context.getArgs()[0])) {
 			return result.setReturnType(ReturnType.NOTICE).setBody("Channel is not muted").setTarget(source.getSource() != CommandSource.Source.USER ? null : source.getUser().getNick());
 		}
 		Config.removeMutedChannel(context.getArgs()[0]);
 		return null;
 	}
 
 	@Command(name = "listmute", desc = "Unutes the bot in a specific channel")
 	@Usage(".listmute")
 	public CommandResult listmute(CommandSource source, CommandContext context) {
 		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
 			return null;
 		}
 		CommandResult result = new CommandResult();
 		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "admin.mutelist")) {
 			return result.setReturnType(ReturnType.NOTICE).setBody("You don't have permission!").setTarget(source.getSource() != CommandSource.Source.USER ? null : source.getUser().getNick());
 		}
 		StringBuilder builder = new StringBuilder();
 		builder.append("Muted channels: ");
 		int i = 0;
 		for (MutedChannel chan : Config.getMutedChannels()) {
 			i++;
 			if (chan.isMuted()) {
 				builder.append(chan.getName());
 				if (i <= Config.getMutedChannels().size()) {
 					builder.append(", ");
 				}
 			}
 		}
 		if (builder.toString().equalsIgnoreCase("Muted channels: ")) {
 			builder = new StringBuilder().append("There are no muted channels");
 		}
 		return result.setReturnType(ReturnType.MESSAGE).setBody(builder.toString()).setTarget(context.getLocationType() == CommandContext.LocationType.CHANNEL ? context.getLocation() : source.getUser().getNick());
 	}
 
 	@Command(name = "memory", desc = "Returns information on the JVM's memory usage", aliases = {"mem"})
 	@Usage(".memory")
 	public CommandResult memory(CommandSource source, CommandContext context) {
 		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
 			return null;
 		}
 		CommandResult result = new CommandResult();
 		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "admin.memory")) {
 			return result.setReturnType(ReturnType.NOTICE).setBody("You don't have permission!").setTarget(source.getUser().getNick());
 		}
 		String freeMemory = FileUtils.byteCountToDisplaySize(Runtime.getRuntime().freeMemory());
 		String maxMemory = FileUtils.byteCountToDisplaySize(Runtime.getRuntime().maxMemory());
 		String totalMemory = FileUtils.byteCountToDisplaySize(Runtime.getRuntime().totalMemory());
 
 		StringBuilder builder = new StringBuilder();
		builder.append("Maximum Mem: ").append(maxMemory).append(", Used Memory: ").append(totalMemory).append(", Free Memeory: ").append(freeMemory);
 		return get(ReturnType.MESSAGE, builder.toString(), source, context);
 	}
 
 	@Command(name = "cores", desc = "Returns the number of cores available to the JVM")
 	@Usage(".cores")
 	public CommandResult cores(CommandSource source, CommandContext context) {
 		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
 			return null;
 		}
 		CommandResult result = new CommandResult();
 		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "admin.cores")) {
 			return result.setReturnType(ReturnType.NOTICE).setBody("You don't have permission!").setTarget(source.getUser().getNick());
 		}
 		int availableCores = Runtime.getRuntime().availableProcessors();
 
 		StringBuilder builder = new StringBuilder();
 		builder.append("Available Cores: ").append(availableCores);
 		return get(ReturnType.MESSAGE, builder.toString(), source, context);
 	}
 
 	@Command(name = "gc", desc = "Asks the JVM to run the Garbage Collector")
 	@Usage(".gc")
 	public CommandResult gc(CommandSource source, CommandContext context) {
 		if (source.getSource() == CommandSource.Source.USER && (context.getPrefix() == null || !context.getPrefix().equals("."))) {
 			return null;
 		}
 		CommandResult result = new CommandResult();
 		if (source.getSource() == CommandSource.Source.USER && !hasPerm(source.getUser(), "admin.gc")) {
 			return result.setReturnType(ReturnType.NOTICE).setBody("You don't have permission!").setTarget(source.getUser().getNick());
 		}
 
 		System.gc();
 
 		return get(ReturnType.MESSAGE, "Invoked System.gc()", source, context);
 	}
 }
