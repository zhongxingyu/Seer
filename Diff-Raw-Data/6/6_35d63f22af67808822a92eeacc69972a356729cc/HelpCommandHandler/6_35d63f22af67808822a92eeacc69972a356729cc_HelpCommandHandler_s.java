 package com.drtshock.willie.command.misc;
 
 import org.pircbotx.Channel;
 import org.pircbotx.User;
 
 import com.drtshock.willie.Willie;
 import com.drtshock.willie.command.Command;
 import com.drtshock.willie.command.CommandHandler;
 
 public class HelpCommandHandler implements CommandHandler {
 
     @Override
     public void handle(Willie bot, Channel channel, User sender, String[] args) {
         String cmdPrefix = bot.getConfig().getCommandPrefix();
         for (Command command : bot.commandManager.getCommands()) {
            sender.sendMessage(cmdPrefix + command.getName() + " - " + command.getHelp());
         }
     }
 
 }
