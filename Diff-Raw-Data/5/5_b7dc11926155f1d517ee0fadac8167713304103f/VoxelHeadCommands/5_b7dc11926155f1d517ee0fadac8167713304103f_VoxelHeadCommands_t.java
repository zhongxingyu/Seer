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
 
 import com.lordralex.ralexbot.RalexBot;
 import com.lordralex.ralexbot.api.EventField;
 import com.lordralex.ralexbot.api.EventType;
 import com.lordralex.ralexbot.api.Listener;
 import com.lordralex.ralexbot.api.events.CommandEvent;
 import com.lordralex.ralexbot.api.events.JoinEvent;
 import com.lordralex.ralexbot.api.events.PartEvent;
 import com.lordralex.ralexbot.api.events.QuitEvent;
 import com.lordralex.ralexbot.api.users.BotUser;
 import com.lordralex.ralexbot.settings.Settings;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Level;
 import org.pircbotx.Colors;
 
 /**
  * @author Lord_Ralex
  * @version 1.0
  */
 public class VoxelHeadCommands extends Listener {
 
     private final File dbMinecraft = new File("voxelhead", "minecraft.db");
     private final File dbScrolls = new File("voxelhead", "scrolls.db");
     private static final URL dbMinecraftLink;
     private static final URL dbScrollsLink;
     private final Map<String, String[]> minecraftIndex = new ConcurrentHashMap<>();
     private final Map<String, String[]> scrollsIndex = new ConcurrentHashMap<>();
     private int delay = 1000;
 
     static {
         URL temp;
         try {
             temp = new URL("http://home.ghoti.me:8080/~faqbot/faqdatabase");
         } catch (MalformedURLException ex) {
             RalexBot.getLogger().log(Level.SEVERE, "An error happened while loading the MinecraftHelp FAQ DB", ex);
             temp = null;
         }
         dbMinecraftLink = temp;
         try {
             temp = new URL("http://home.ghoti.me:8080/~faqbot/scrollsfaqdatabase");
         } catch (MalformedURLException ex) {
             RalexBot.getLogger().log(Level.SEVERE, "An error happened while loading the ScrollsHelp FAQ DB", ex);
             temp = null;
         }
         dbScrollsLink = temp;
     }
 
     @Override
     public void setup() {
         minecraftIndex.clear();
         scrollsIndex.clear();
         dbMinecraft.getParentFile().mkdirs();
         dbMinecraft.delete();
         try {
             InputStream reader = dbMinecraftLink.openStream();
             FileOutputStream writer = new FileOutputStream(dbMinecraft);
             copyInputStream(reader, writer);
             BufferedReader filereader = new BufferedReader(new FileReader(dbMinecraft));
             String line;
             while ((line = filereader.readLine()) != null) {
                 if (line.contains("|")) {
                     String key = line.split("\\|")[0];
                     String value = line.split("\\|", 2)[1];
                     minecraftIndex.put(key.toLowerCase(), value.split(";;"));
                 }
             }
         } catch (IOException ex) {
             RalexBot.getLogger().log(Level.SEVERE, "There was an error", ex);
         }
         try {
             InputStream reader = dbScrollsLink.openStream();
             FileOutputStream writer = new FileOutputStream(dbScrolls);
             copyInputStream(reader, writer);
             BufferedReader filereader = new BufferedReader(new FileReader(dbScrolls));
             String line;
             while ((line = filereader.readLine()) != null) {
                 if (line.contains("|")) {
                     String key = line.split("\\|")[0];
                     String value = line.split("\\|", 2)[1];
                     scrollsIndex.put(key.toLowerCase(), value.split(";;"));
                 }
             }
         } catch (IOException ex) {
             RalexBot.getLogger().log(Level.SEVERE, "There was an error", ex);
         }
         delay = Settings.getGlobalSettings().getInt("voxelhead-delay");
     }
 
     @Override
     @EventType(event = EventField.Command)
     public void runEvent(CommandEvent event) {
         if (event.getCommand().equalsIgnoreCase("refresh")) {
             setup();
             event.getSender().sendMessage("Updated local storage");
             return;
         } else {
             boolean allowExec = true;
             List<String> users = event.getChannel().getUsers();
             if (users.contains("VoxelHead")) {
                 allowExec = false;
             }
             if (!allowExec) {
                 return;
             }
             String cmdMethod = event.getCommand().toLowerCase();
             Map<String, String[]> index;
             if (event.getCommand().startsWith("scrolls")) {
                 index = scrollsIndex;
             } else if (event.getCommand().startsWith("minecraft")) {
                 index = minecraftIndex;
             } else if (event.getChannel() != null) {
                if (event.getChannel().getName().startsWith("#minecraft")) {
                     index = minecraftIndex;
                } else if (event.getChannel().getName().startsWith("#scrolls")) {
                     index = scrollsIndex;
                 } else {
                     index = minecraftIndex;
                 }
             } else {
                 index = minecraftIndex;
             }
             cmdMethod = cmdMethod.replace("minecraft", "").replace("scrolls", "");
             switch (cmdMethod) {
                 case ">": {
                     String target = event.getArgs()[0];
                     String channel = event.getChannel().getName();
                     String[] lines = index.get(event.getArgs()[1].toLowerCase());
                     if (lines == null || lines.length == 0) {
                         event.getSender().sendNotice("Voxelhead does not the factoid " + event.getArgs()[1] + " in his database");
                         return;
                     }
                     RunLaterThread thread = new RunLaterThread(event.getArgs()[1].toLowerCase(), target, channel, lines, false);
                     thread.start();
                 }
                 break;
                 case ">>": {
                     String target = event.getArgs()[0];
                     String channel = event.getChannel().getName();
                     String[] lines = index.get(event.getArgs()[1].toLowerCase());
                     if (lines == null || lines.length == 0) {
                         event.getSender().sendNotice("Voxelhead does not the factoid " + event.getArgs()[1] + " in his database");
                         return;
                     }
                     RunLaterThread thread = new RunLaterThread(event.getArgs()[1].toLowerCase(), target, channel, lines, true);
                     thread.start();
                 }
                 break;
                 case "<<": {
                     String target = event.getArgs()[0];
                     String channel = event.getChannel().getName();
                     String[] lines = index.get(event.getArgs()[1].toLowerCase());
                     if (lines == null || lines.length == 0) {
                         event.getSender().sendNotice("Voxelhead does not the factoid " + event.getArgs()[1] + " in his database");
                         return;
                     }
                     RunLaterThread thread = new RunLaterThread(event.getArgs()[1].toLowerCase(), target, channel, lines, true);
                     thread.start();
                 }
                 break;
                 case "<": {
                     String target = event.getSender().getNick();
                     String channel = event.getChannel().getName();
                     String[] lines = index.get(event.getArgs()[0].toLowerCase());
                     if (lines == null || lines.length == 0) {
                         event.getSender().sendNotice("Voxelhead does not the factoid " + event.getArgs()[0] + " in his database");
                         return;
                     }
                     RunLaterThread thread = new RunLaterThread(event.getArgs()[0].toLowerCase(), target, channel, lines, true);
                     thread.start();
                 }
                 break;
                 default: {
                     String target = null;
                     String channel = event.getChannel().getName();
                     String[] lines = index.get(event.getArgs()[0].toLowerCase());
                     if (lines == null || lines.length == 0) {
                         event.getSender().sendNotice("Voxelhead does not the factoid " + event.getArgs()[0] + " in his database");
                         return;
                     }
                     RunLaterThread thread = new RunLaterThread(event.getArgs()[0].toLowerCase(), target, channel, lines, false);
                     thread.start();
                 }
                 break;
             }
         }
     }
 
     @Override
     public String[] getAliases() {
         return new String[]{
             "vh",
             ">",
             "<",
             "<<",
             "scrollsvh",
             "scrolls>",
             "scrolls<",
             "scrolls<<",
             "minecraftvh",
             "minecraft>",
             "minecraft<",
             "minecraft<<"
         };
     }
 
     @Override
     @EventType(event = EventField.Join)
     public void runEvent(JoinEvent event) {
         if (event.getSender().getNick().equalsIgnoreCase("voxelhead")) {
             event.getChannel().sendMessage("Voxelhead has returned. Returning to my cave.");
         }
     }
 
     @Override
     @EventType(event = EventField.Part)
     public void runEvent(PartEvent event) {
         if (event.getSender().getNick().equalsIgnoreCase("voxelhead")) {
             event.getChannel().sendMessage("Voxelhead has left the building. Taking over.");
         }
     }
 
     @Override
     @EventType(event = EventField.Quit)
     public void runEvent(QuitEvent event) {
         if (event.getSender().getNick().equalsIgnoreCase("voxelhead")) {
             BotUser.getBotUser().sendMessage("#minecrafthelp", "Voxelhead has left the building. Taking over.");
         }
     }
 
     private void copyInputStream(InputStream in, FileOutputStream out) throws IOException {
         ReadableByteChannel rbc = Channels.newChannel(in);
         out.getChannel().transferFrom(rbc, 0, 1 << 24);
     }
 
     private class RunLaterThread extends Thread {
 
         private String[] lines;
         private String channel;
         private String user;
         private boolean notice = false;
         private String name;
 
         public RunLaterThread(String na, String u, String c, String[] l, boolean n) {
             lines = l;
             channel = c;
             user = u;
             notice = n;
             name = na;
         }
 
         @Override
         public void run() {
             BotUser bot = BotUser.getBotUser();
             for (String string : lines) {
                 String message;
                 if (user == null) {
                     message = Colors.BOLD + name.toLowerCase() + ": " + Colors.NORMAL + string;
                 } else {
                     message = Colors.BOLD + user + ": " + Colors.NORMAL + "(" + name.toLowerCase() + ") " + string;
                 }
                 if (notice) {
                     bot.sendNotice(user, message);
                 } else {
                     bot.sendMessage(channel, message);
                 }
                 synchronized (RunLaterThread.this) {
                     try {
                         RunLaterThread.this.wait(delay);
                     } catch (InterruptedException ex) {
                         RalexBot.getLogger().log(Level.SEVERE, null, ex);
                     }
                 }
             }
         }
     }
 }
