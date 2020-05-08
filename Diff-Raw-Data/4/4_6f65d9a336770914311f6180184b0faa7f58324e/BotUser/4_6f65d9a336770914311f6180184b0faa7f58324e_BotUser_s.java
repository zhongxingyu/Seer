 /*
  * Copyright (C) 2013 Lord_Ralex
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.lordralex.ralexbot.api.users;
 
 /**
  *
  * @author Joshua
  */
 public class BotUser extends User {
 
     public BotUser() {
         super(bot.getUserBot());
     }
 
     public static BotUser getBotUser() {
         return new BotUser();
     }
 
     public void setNick(String newNick) {
         bot.changeNick(newNick);
     }
 
     public void kick(String nick, String channel) {
         kick(nick, channel, null);
     }
 
     public void kick(String nick, String channel, String reason) {
        if (reason == null || reason.isEmpty()) {
            kick(nick, channel);
            return;
        }
         if (bot.getChannel(channel).isOp(bot.getUserBot())) {
             if (reason == null || reason.isEmpty()) {
                 bot.kick(bot.getChannel(channel), bot.getUser(nick));
             } else {
                 bot.kick(bot.getChannel(channel), bot.getUser(nick), reason);
             }
         } else {
             this.sendMessage("chanserv", "kick " + channel + " " + nick + " " + reason);
         }
     }
 
     public void joinChannel(String channel) {
         bot.joinChannel(channel);
     }
 
     public void leaveChannel(String channel) {
         bot.partChannel(bot.getChannel(channel));
     }
 
     public void sendMessage(String target, String message) {
         bot.sendMessage(target, message);
     }
 
     public void sendNotice(String target, String message) {
         bot.sendNotice(target, message);
     }
 
     public void sendAction(String target, String message) {
         bot.sendAction(target, message);
     }
 
     public void ban(String channel, String mask) {
         if (bot.getChannel(channel).isOp(bot.getUserBot())) {
             bot.ban(bot.getChannel(channel), mask);
         } else {
             this.sendMessage("chanserv", "ban " + channel + " " + mask);
         }
     }
 
     public void unban(String channel, String mask) {
         if (bot.getChannel(channel).isOp(bot.getUserBot())) {
             bot.unBan(bot.getChannel(channel), mask);
         } else {
             this.sendMessage("chanserv", "unban " + channel + " " + mask);
         }
     }
 }
