 package com.lordralex.ralexbot.threads;
 
 import com.lordralex.ralexbot.RalexBot;
 import com.lordralex.ralexbot.api.exceptions.NickNotOnlineException;
 import com.lordralex.ralexbot.api.users.BotUser;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import jline.console.ConsoleReader;
 
 /**
  *
  * @author Joshua
  */
 public final class KeyboardListener extends Thread {
 
     ConsoleReader kb;
     final RalexBot instance;
     BotUser bot;
 
     public KeyboardListener(RalexBot a) throws IOException, NickNotOnlineException {
         setName("Keyboard_Listener_Thread");
         kb = new ConsoleReader();
         instance = a;
     }
 
     @Override
     public void run() {
         bot = new BotUser();
         String line;
         boolean run = true;
         String currentChan = "";
         try {
             while (run) {
                 try {
                     line = kb.readLine();
                     if (line == null) {
                         run = false;
                     } else {
                         if (line.startsWith("$")) {
                             String cmd = line.substring(1).split(" ")[0];
                             if (cmd.equalsIgnoreCase("channel")) {
                                 currentChan = line.split(" ")[1].toLowerCase();
                                 System.out.println("Now talking in " + currentChan);
                             } else if (cmd.equalsIgnoreCase("stop")) {
                                 run = false;
                             } else if (cmd.equalsIgnoreCase("join")) {
                                 bot.joinChannel(line.split(" ")[1]);
                             } else if (cmd.equalsIgnoreCase("leave")) {
                                 bot.leaveChannel(line.split(" ")[1]);
                             } else if (cmd.equalsIgnoreCase("me")) {
                                 String action = line.substring(3);
                                 if (currentChan != null && !currentChan.isEmpty()) {
                                     bot.sendAction(currentChan, action);
                                 }
                             } else if (cmd.equalsIgnoreCase("kick")) {
                                 List<String> args = new ArrayList<>();
                                 String[] parts = line.split(" ");
                                 args.addAll(Arrays.asList(parts));
                                 args.remove(0);
                                 String chan;
                                 if (args.get(0).startsWith("#")) {
                                     chan = args.remove(0);
                                 } else {
                                     chan = currentChan;
                                 }
                                 String target = args.remove(0);
                                 String reason = "";
                                 if (args.size() > 0) {
                                     for (String part : args) {
                                         reason += part + " ";
                                     }
                                     reason = reason.trim();
                                 } else {
                                     reason = bot.getNick() + " has kicked " + target + " from the channel";
                                 }
                                 if (bot.hasOP(chan)) {
                                     bot.kick(target, chan, reason);
                                 } else {
                                     bot.sendMessage("chanserv", "kick " + chan + " " + target + " " + reason);
                                 }
                             }
                         } else {
                             if (currentChan == null || currentChan.isEmpty()) {
                             } else {
                                 bot.sendMessage(currentChan, line);
                             }
                         }
                     }
                 } catch (IOException ex) {
                    ex.printStackTrace(System.out);
                 }
             }
         } catch (Exception e) {
         }
         synchronized (instance) {
             instance.notify();
         }
         kb.shutdown();
         RalexBot.getLogger().info("Ending keyboard listener");
     }
 
     public ConsoleReader getJLine() {
         return kb;
     }
 }
