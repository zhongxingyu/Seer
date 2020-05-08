 package com.drtshock.willie.command.fun;
 
 import java.io.IOException;
 
 import org.pircbotx.Channel;
 import org.pircbotx.Colors;
 import org.pircbotx.User;
 
 import com.drtshock.willie.Willie;
 import com.drtshock.willie.command.CommandHandler;
 import com.drtshock.willie.util.Dictionary;
 
 public class UrbanCommandHandler implements CommandHandler {
 
     @Override
     public void handle(Willie bot, Channel channel, User sender, String[] args) {
         if(args.length == 0) {
             channel.sendMessage(Colors.RED + "Usage: !urban <word|phrase>");
         } else {
             StringBuilder sb = new StringBuilder();
             for(String arg:args) {
                 sb.append(arg).append("+");
             }
             String query = sb.toString();
             query = query.substring(0, query.length() - 1);
             Dictionary.Definition def;
             try {
                 def = Dictionary.URBAN_DICTIONARY.getDefinition(query);
             } catch (IOException e) {
                 def = null;
             }
 
             if(def == null) {
                channel.sendMessage(Colors.RED + "I couldn't not lookup that definition. Sorry.");
                 return;
             }
 
             channel.sendMessage("Word: " + Colors.BLUE + query.replace('+', ' '));
             channel.sendMessage("Definition: " + def.getDefinition());
             channel.sendMessage("For a full definition visit: " + def.getUrl());
         }
     }
 
 }
