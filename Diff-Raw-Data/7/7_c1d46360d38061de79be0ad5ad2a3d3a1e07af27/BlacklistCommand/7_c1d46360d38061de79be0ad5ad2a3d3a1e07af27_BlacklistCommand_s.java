 /*
  *     Copyright (C) 2013  Nodin Chan <nodinchan@live.com>
  *     
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *     
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *     
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package com.titankingdoms.dev.titanchat.command.defaults;
 
 import java.util.Arrays;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.command.CommandSender;
 
 import com.titankingdoms.dev.titanchat.command.Command;
 import com.titankingdoms.dev.titanchat.core.channel.Channel;
 import com.titankingdoms.dev.titanchat.core.participant.Participant;
 import com.titankingdoms.dev.titanchat.vault.Vault;
 
 /**
  * {@link BlacklistCommand} - Command for editting and viewing the blacklist in {@link Channel}s
  * 
  * @author NodinChan
  *
  */
 public final class BlacklistCommand extends Command {
 	
 	public BlacklistCommand() {
 		super("Blacklist");
 		setAliases("ban", "b");
 		setArgumentRange(1, 1024);
 		setDescription("Edit the blacklist of the channel");
 		setUsage("<add/remove/list> [player] [reason]");
 	}
 	
 	@Override
 	public void execute(CommandSender sender, Channel channel, String[] args) {
 		if (channel == null) {
 			sendMessage(sender, "&4Channel not defined");
 			return;
 		}
 		
 		if (args[0].equals("list")) {
 			String list = StringUtils.join(channel.getBlacklist(), ", ");
 			sendMessage(sender, "&6" + channel.getName() + " Blacklist: " + list);
 			
 		} else {
 			if (args.length < 2) {
 				sendMessage(sender, "&4Invalid argument length");
 				
 				String usage = "/titanchat [@<channel>] " + getName().toLowerCase() + " " + getUsage();
 				sendMessage(sender, "&6" + usage);
 				return;
 			}
 		}
 		
 		Participant participant = plugin.getParticipantManager().getParticipant(args[1]);
 		
 		if (args[0].equalsIgnoreCase("add")) {
 			if (channel.getBlacklist().contains(participant.getName())) {
 				sendMessage(sender, "&4" + participant.getDisplayName() + " is already on the blacklist");
 				return;
 			}
 			
 			String reason = StringUtils.join(Arrays.copyOfRange(args, 2, args.length)).trim();
 			
 			channel.leave(participant);
 			channel.getBlacklist().add(participant.getName());
 			participant.sendMessage("&4You have been added to the blacklist of " + channel.getName());
 			
 			if (!reason.isEmpty())
 				participant.sendMessage("&4Reason: " + reason);
 			
 			if (!channel.isParticipating(sender.getName()))
 				sendMessage(sender, "&6" + participant.getDisplayName() + " has been added to blacklist");
 			
 			broadcast(channel, "&6" + participant.getDisplayName() + " has been added to blacklist");
 			
 		} else if (args[0].equalsIgnoreCase("remove")) {
 			if (!channel.getBlacklist().contains(participant.getName())) {
 				sendMessage(sender, "&4" + participant.getDisplayName() + " is not on the blacklist");
 				return;
 			}
 			
 			String reason = StringUtils.join(Arrays.copyOfRange(args, 2, args.length)).trim();
 			
 			channel.getBlacklist().remove(participant.getName());
 			participant.sendMessage("&6You have been removed from the blacklist of " + channel.getName());
 			
 			if (!reason.isEmpty())
 				participant.sendMessage("&6Reason: " + reason);
 			
 			if (!channel.isParticipating(sender.getName()))
 				sendMessage(sender, "&6" + participant.getDisplayName() + " has been removed from blacklist");
 			
 			broadcast(channel, "&6" + participant.getDisplayName() + " has been removed from blacklist");
 			
 		} else {
 			sendMessage(sender, "&4Incorrect usage: /titanchat @[channel] blacklist " + getUsage());
 		}
 	}
 	
 	@Override
 	public boolean permissionCheck(CommandSender sender, Channel channel) {
 		if (channel == null)
 			return false;
 		
 		if (channel.getOperators().contains(sender.getName()))
 			return true;
 		
 		return Vault.hasPermission(sender, "TitanChat.blacklist." + channel.getName());
 	}
 }
