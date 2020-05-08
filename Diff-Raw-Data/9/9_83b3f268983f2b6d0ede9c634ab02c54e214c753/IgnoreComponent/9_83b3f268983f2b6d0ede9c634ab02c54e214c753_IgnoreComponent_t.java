 /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  * Copyright 2012 StarTux
  *
  * This file is part of Winthier.
  *
  * Winthier is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Winthier is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Winthier.  If not, see <http://www.gnu.org/licenses/>.
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
 
 package com.winthier;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 public class IgnoreComponent extends AbstractComponent implements CommandExecutor, Listener {
         private static IgnoreComponent instance;
         private Map<String, Set<String>> ignoreLists = Collections.synchronizedMap(new LinkedHashMap<String, Set<String>>());
         private Set<String> muteList = Collections.synchronizedSet(new HashSet<String>());
 
         public IgnoreComponent(WinthierPlugin plugin) {
                 super(plugin, "ignore");
                 instance = this;
         }
 
         @Override
         public void enable() {
                 getPlugin().getCommand("ignore").setExecutor(this);
                 getPlugin().getCommand("mute").setExecutor(this);
                 getPlugin().getCommand("unmute").setExecutor(this);
                 getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
         }
 
         @Override
         public boolean onCommand(CommandSender sender, Command command, String token, String[] args) {
                 if (token.equals("ignore")) {
                         if (!(sender instanceof Player)) {
                                 sender.sendMessage(ChatColor.RED + "Player expected!");
                                 return true;
                         }
                         Player ignorer = (Player)sender;
                         if (args.length == 0) {
                                 Set<String> ignorees = getIgnoreList(ignorer.getName());
                                 if (ignorees.size() == 0) {
                                         sender.sendMessage("You are not ignoring anyone.");
                                 } else {
                                         StringBuilder sb = new StringBuilder("Ignored players:");
                                         for (String ignoree : ignorees) sb.append(" ").append(ignoree);
                                         sender.sendMessage(sb.toString());
                                 }
                                 return true;
                         }
                         if (args.length == 1) {
                                 String ignoree = getPlayer(args[0], ignorer.getName());
                                 if (ignoree == null) {
                                         sender.sendMessage("" + ChatColor.RED + "Player not found: " + args[0]);
                                         return true;
                                 }
                                 boolean ignore = toggleIgnore(ignorer.getName(), ignoree);
                                 sender.sendMessage((ignore ? "Ignoring " : "No longer ignoring ") + ignoree);
                         } else {
                                 for (String arg : args) {
                                         String ignoree = getPlayer(arg, ignorer.getName());
                                         if (ignoree == null) {
                                                 sender.sendMessage("" + ChatColor.RED + "Player not found: " + arg);
                                         } else {
                                                 setIgnore(ignorer.getName(), ignoree, true);
                                                 sender.sendMessage("Ignoring " + ignoree);
                                         }
                                 }
                         }
                         return true;
                 } else if (token.equals("mute")) {
                         if (args.length == 0) {
                                 // list muted players
                                 if (muteList.isEmpty()) {
                                         sender.sendMessage("No players muted");
                                 } else {
                                         StringBuilder sb = new StringBuilder("Muted players:");
                                         synchronized(muteList) {
                                                 for (String name : muteList) sb.append(" ").append(name);
                                         }
                                        sender.sendMessage(sb.toString());
                                 }
                         } else if (args.length == 1) {
                                 // mute a player
                                 String mutee = getPlayer(args[0]);
                                 if (mutee == null) {
                                         sender.sendMessage("" + ChatColor.RED + "Player not found: " + args[0]);
                                 } else {
                                         if (muteList.contains(mutee)) {
                                                 muteList.remove(mutee);
                                                 sender.sendMessage(mutee + " has been unmuted");
                                         } else {
                                                 muteList.add(mutee);
                                                 sender.sendMessage(mutee + " is now muted");
                                         }
                                 }
                         } else {
                                 return false;
                         }
                         return true;
                 } else if (token.equals("unmute")) {
                         if (args.length == 1) {
                                 String mutee = getPlayer(args[0]);
                                 if (mutee == null) {
                                         sender.sendMessage("" + ChatColor.RED + "Player not found: " + args[0]);
                                 } else {
                                         if (muteList.contains(mutee)) {
                                                 muteList.remove(mutee);
                                                 sender.sendMessage(mutee + " has been unmuted");
                                         } else {
                                                 sender.sendMessage("" + ChatColor.RED + mutee + " is not muted");
                                         }
                                 }
                                 return true;
                         }
                 }
                 return false;
         }
 
         @Override
         public void loadConfiguration() {
                 FileConfiguration conf = new YamlConfiguration();
                 try {
                         conf.load(new File(getPlugin().getDataFolder(), "ignore.yml"));
                 } catch (FileNotFoundException fnfe) {
                         // do nothing
                 } catch (Exception e) {
                         e.printStackTrace();
                         return;
                 }
                 ignoreLists.clear();
                 for (String ignorer : conf.getKeys(false)) {
                         for (String ignoree : conf.getStringList(ignorer)) {
                                 setIgnore(ignorer, ignoree, true);
                         }
                 }
         }
 
         @Override
         public void saveConfiguration() {
                 FileConfiguration conf = new YamlConfiguration();
                 synchronized(ignoreLists) {
                         for (String ignorer : ignoreLists.keySet()) {
                                 conf.set(ignorer, new ArrayList<String>(ignoreLists.get(ignorer)));
                         }
                 }
                 try {
                         conf.save(new File(getPlugin().getDataFolder(), "ignore.yml"));
                 } catch (Exception e) {
                         e.printStackTrace();
                         return;
                 }
         }
 
         private String getPlayer(String name, String forPlayer) {
                 OfflinePlayer player = getPlugin().getServer().getPlayer(name);
                 if (player != null) return player.getName();
                 player = getPlugin().getServer().getOfflinePlayer(name);
                 if (player.hasPlayedBefore()) return player.getName();
                 if (forPlayer != null) {
                         synchronized(ignoreLists) {
                                 Set<String> ignoreList = ignoreLists.get(forPlayer);
                                 if (ignoreList != null) {
                                         for (String entry : ignoreList) {
                                                 if (entry.toLowerCase().startsWith(name.toLowerCase())) {
                                                         return entry;
                                                 }
                                         }
                                 }
                         }
                 }
                synchronized(muteList) {
                        for (String entry : muteList) {
                                if (entry.toLowerCase().startsWith(name.toLowerCase())) {
                                        return entry;
                                }
                        }
                }
                 return null;
         }
 
         private String getPlayer(String name) {
                 return getPlayer(name, null);
         }
 
         @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
         public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
                 if (muteList.contains(event.getPlayer().getName())) {
                         event.setCancelled(true);
                         return;
                 }
                 filterRecipients(event.getPlayer().getName(), event.getRecipients());
         }
 
         public void setIgnore(String ignorer, String ignoree, boolean ignore) {
                 synchronized(ignoreLists) {
                         Set<String> ignoreList = ignoreLists.get(ignorer);
                         if (ignoreList == null) {
                                 if (!ignore) return;
                                 ignoreList = new LinkedHashSet<String>();
                                 ignoreLists.put(ignorer, ignoreList);
                         }
                         if (ignore) {
                                 ignoreList.add(ignoree);
                         } else {
                                 ignoreList.remove(ignoree);
                                 if (ignoreList.isEmpty()) ignoreLists.remove(ignorer);
                         }
                 }
         }
 
         public boolean doesIgnore(String ignorer, String ignoree) {
                 synchronized(ignoreLists) {
                         Set<String> ignoreList = ignoreLists.get(ignorer);
                         if (ignoreList == null) return false;
                         return ignoreList.contains(ignoree);
                 }
         }
 
         public boolean toggleIgnore(String ignorer, String ignoree) {
                 synchronized(ignoreLists) {
                         boolean ignore = !doesIgnore(ignorer, ignoree);
                         setIgnore(ignorer, ignoree, ignore);
                         return ignore;
                 }
         }
 
         public void filterRecipients(String ignoree, Collection<Player> recipients) {
                 if (muteList.contains(ignoree)) {
                         recipients.clear();
                         return;
                 }
                 synchronized(ignoreLists) {
                         for (Iterator<Player> it = recipients.iterator(); it.hasNext(); ) {
                                 String ignorer = it.next().getName();
                                 Set<String> ignoreList = ignoreLists.get(ignorer);
                                 if (ignoreList == null) continue;
                                 if (ignoreList.contains(ignoree)) {
                                         it.remove();
                                 }
                         }
                 }
         }
 
         public Set<String> getIgnoreList(String ignorer) {
                 synchronized(ignoreLists) {
                         Set<String> ignoreList = ignoreLists.get(ignorer);
                         if (ignoreList == null) return new HashSet<String>();
                         return new LinkedHashSet<String>(ignoreList);
                 }
         }
 
         public void broadcast(CommandSender speaker, List<String> message, boolean ignoreSelf) {
                 Set<Player> recipients = new HashSet<Player>(Arrays.asList(getPlugin().getServer().getOnlinePlayers()));
                 if (speaker instanceof Player && ignoreSelf) recipients.remove((Player)speaker);
                 filterRecipients(speaker.getName(), recipients);
                 for (Player recipient : recipients) {
                         for (String line : message) recipient.sendMessage(line);
                 }
         }
 
         public void broadcast(CommandSender speaker, String message, boolean ignoreSelf) {
                 broadcast(speaker, Arrays.asList(message), ignoreSelf);
         }
 
         public static IgnoreComponent getInstance() {
                 return instance;
         }
 }
