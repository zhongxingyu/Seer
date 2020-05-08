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
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.command.CommandSender;
 
 import com.titankingdoms.dev.titanchat.command.Command;
 import com.titankingdoms.dev.titanchat.core.channel.Channel;
 import com.titankingdoms.dev.titanchat.core.channel.conversation.Conversation;
 import com.titankingdoms.dev.titanchat.core.participant.Participant;
 import com.titankingdoms.dev.titanchat.vault.Vault;
 
 public final class PMCommand extends Command {
 
 	public PMCommand() {
 		super("PM");
 		setAliases("msg", "privmsg");
 		setArgumentRange(1, 1024);
 		setDescription("Private messaging");
 		setUsage("<player> [message]");
 	}
 
 	@Override
 	public void execute(CommandSender sender, Channel channel, String[] args) {
 		Participant target = plugin.getParticipantManager().getParticipant(args[0]);
 		
 		if (!target.isOnline()) {
 			sendMessage(sender, "&4" + target.getDisplayName() + " is currently offline");
 			return;
 		}
 		
 		Participant participant = plugin.getParticipantManager().getParticipant(sender);
 		
 		Conversation conversation = new Conversation();
 		conversation.join(target);
 		
 		if (args.length > 1)
			participant.chat(conversation, StringUtils.join(args, " "));
 		else
 			participant.direct(conversation);
 	}
 
 	@Override
 	public boolean permissionCheck(CommandSender sender, Channel channel) {
 		return Vault.hasPermission(sender, "TitanChat.privmsg");
 	}
 }
