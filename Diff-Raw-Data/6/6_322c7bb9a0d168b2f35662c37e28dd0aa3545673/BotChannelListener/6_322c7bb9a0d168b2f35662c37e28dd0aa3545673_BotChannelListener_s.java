 package com.brewtab.ircbot;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 
 import com.brewtab.irc.IRCChannel;
 import com.brewtab.irc.IRCChannelListener;
 import com.brewtab.irc.IRCUser;
 import com.brewtab.ircbot.applets.BotApplet;
 
 public class BotChannelListener implements IRCChannelListener, BotApplet {
     private HashMap<String, BotApplet> applets;
 
     public BotChannelListener() {
         this.applets = new HashMap<String, BotApplet>();
 
         /* Register built in commands */
         this.registerApplet("help", this);
         this.registerApplet("commands", this);
         this.registerApplet("quit", this);
         this.registerApplet("echo", this);
     }
 
     public void registerApplet(String command_name, BotApplet applet) {
         this.applets.put(command_name, applet);
     }
 
     public void registerApplet(BotApplet applet, String... commandNames) {
         for (String command : commandNames) {
             this.applets.put(command, applet);
         }
     }
 
     @Override
     public void onJoin(IRCChannel channel, IRCUser user) {
         // -
     }
 
     @Override
     public void onPart(IRCChannel channel, IRCUser user) {
         // -
     }
 
     @Override
     public void onQuit(IRCChannel channel, IRCUser user) {
         // -
     }
 
     private String[] parseArgs(String argString) {
         ArrayList<String> args = new ArrayList<String>();
         StringBuilder sb = new StringBuilder();
         char c;
         int state = 0;
 
         for (int i = 0; i < argString.length(); i++) {
             c = argString.charAt(i);
 
             switch (state) {
             case 0:
                 /* Consume whitespace */
                 if (Character.isWhitespace(c)) {
                     break;
                 }
                 state = 1;
 
             case 1:
                 if (c == '\"') {
                     /* Enter quoted string */
                     state = 2;
                 } else if (Character.isWhitespace(c)) {
                     /* End of argument */
                     args.add(sb.toString());
                     sb = new StringBuilder();
                     state = 0;
                 } else {
                     sb.append(c);
                 }
                 break;
 
             case 2:
                 if (c == '\\') {
                     state = 3;
                 } else if (c == '\"') {
                     state = 1;
                 } else {
                     sb.append(c);
                 }
                 break;
 
             case 3:
                sb.append(c);
                 state = 2;
                 break;
             }
         }
 
         if (state == 2 || state == 3) {
             /* Unmatched quote */
             return null;
         }
 
         /* Store last argument */
         if (sb.length() > 0) {
             args.add(sb.toString());
         }
 
         return args.toArray(new String[0]);
     }
 
     @Override
     public void onPrivateMessage(IRCChannel channel, String message, IRCUser from) {
         String myNick = channel.getClient().getUser().getNick();
         String command = null;
         String unparsedArgs = null;
         message = message.trim();
 
         /* Possible command */
         if (message.startsWith(myNick + ": ")) {
             String[] parts = message.split("[ ]+", 3);
 
             if (parts.length == 3) {
                 command = parts[1];
                 unparsedArgs = parts[2].trim();
             } else if (parts.length == 2) {
                 command = parts[1];
                 unparsedArgs = "";
             }
         } else if (message.startsWith(".")) {
             String[] parts = message.substring(1).split("[ ]+", 2);
 
             if (parts.length == 2) {
                 command = parts[0];
                 unparsedArgs = parts[1].trim();
             } else if (parts.length == 1) {
                 command = parts[0];
                 unparsedArgs = "";
             }
         }
 
         if (command != null) {
             if (this.applets.containsKey(command)) {
                 String[] args = this.parseArgs(unparsedArgs);
                 BotApplet applet = this.applets.get(command);
 
                 if (args == null) {
                     channel.write("Mismatched quotes in argument");
                 } else {
                     try {
                         applet.run(channel, from, command, args, unparsedArgs);
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                 }
             } else if (message.startsWith(myNick + ": ")) {
                 /*
                  * Only bother to respond to unknown commands if the bot is
                  * asked directly (rather than with a leading '.')
                  */
                 channel.write("Unknown command");
             }
         }
     }
 
     @Override
     public void run(IRCChannel channel, IRCUser from, String command, String[] args, String unparsed) {
         if (command.equals("help") || command.equals("commands")) {
             Set<String> commands = this.applets.keySet();
             StringBuilder sb = new StringBuilder();
 
             sb.append("Commands:");
             for (String cmd : commands) {
                 sb.append(" ");
                 sb.append(cmd);
             }
 
             channel.write(sb.toString());
         } else if (command.equals("quit") && from.getNick().equals("c2nes")) {
             channel.getClient().quit("Leaving");
         } else if (command.equals("echo")) {
             for (String arg : args) {
                 channel.write(arg);
             }
         }
     }
 }
