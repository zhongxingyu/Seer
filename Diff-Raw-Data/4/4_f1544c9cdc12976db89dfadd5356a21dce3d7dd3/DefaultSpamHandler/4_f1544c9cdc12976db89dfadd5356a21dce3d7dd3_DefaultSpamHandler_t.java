 package com.dumptruckman.spamhammer;
 
 import com.dumptruckman.spamhammer.api.Config;
 import com.dumptruckman.spamhammer.api.Config.ConfigEntry;
 import com.dumptruckman.spamhammer.api.SpamHammer;
 import com.dumptruckman.spamhammer.api.SpamHandler;
 import com.dumptruckman.spamhammer.util.Language;
 import com.dumptruckman.spamhammer.util.Messager;
 import com.dumptruckman.spamhammer.util.Perms;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Level;
 import org.bukkit.Bukkit;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 
 /**
  * SpamHandler implementation class
  * 
  * Contains the actual functionality
  * 
  * @author dumptruckman,slipcor
  */
 public class DefaultSpamHandler implements SpamHandler {
     protected Config config;
 
     final private ConcurrentHashMap<String, ArrayDeque<Long>> playerChatTimes = new ConcurrentHashMap<String, ArrayDeque<Long>>();
     final private ConcurrentHashMap<String, ArrayDeque<String>> playerChatHistory = new ConcurrentHashMap<String, ArrayDeque<String>>();
     final private ConcurrentHashMap<String, Long> playerActionTime = new ConcurrentHashMap<String, Long>();
 
     final private List<String> mutedPlayers = new ArrayList<String>();
     final private List<String> beenMutedPlayers = new ArrayList<String>();
     final private List<String> beenKickedPlayers = new ArrayList<String>();
     
     final private SpamHammer plugin;
 
     /**
      * SpamHandler constructor
      * 
      * Hands over the plugin configuration and starts the check timer
      * 
      * @param plugin the plug
      */
     public DefaultSpamHandler(final SpamHammer plugin) {
         this.plugin = plugin;
         this.config = plugin.config();
         
         Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
             @Override
             public void run() {
                 checkTimes();
             }
         }, 0, 20L);
     }
 
     /**
      * Clean a player's history and ban him
      * 
      * @param player the player to ban
      */
     @Override
     public void banPlayer(final OfflinePlayer player) {
         if (playerChatHistory.contains(player.getName())) {
             playerChatHistory.get(player.getName()).clear();
         }
         final Player onlinePlayer = player.getPlayer();
         if (onlinePlayer != null && !Perms.BYPASS_BAN.has(onlinePlayer)) {
             player.setBanned(true);
             if (onlinePlayer != null && !Perms.BYPASS_KICK.has(onlinePlayer)) {
                 class RunLater implements Runnable {
                     @Override
                     public void run() {
                         onlinePlayer.kickPlayer(Language.BAN_MESSAGE.toString());
                         plugin.getLogger().log(Level.INFO, "Player banned: {0}", player.getName());
                     }
                 }
                 Bukkit.getScheduler().runTaskLater(plugin, new RunLater(), 1L);
             }
         }
     }
 
     /**
      * Has a player been kicked before?
      * 
      * @param name the player to check
      * 
      * @return true if the player has been kicked
      */
     @Override
     public boolean beenKicked(final OfflinePlayer name) {
         return beenKickedPlayers.contains(name.getName());
     }
 
     /**
      * Has a player been muted before?
      * 
      * @param name the player to check
      * 
      * @return true if the player has been muted
      */
     @Override
     public boolean beenMuted(final OfflinePlayer name) {
         return beenMutedPlayers.contains(name.getName());
     }
 
     /**
      * Check player timings.
      * 
      * Iterate over all players to check if cooldowns should deplete
      */
     private void checkTimes() {
         final long time = System.nanoTime() / 1000000;
         for (String playerName : playerActionTime.keySet()) {
             final OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
             final long action = playerActionTime.get(playerName);
             if (isMuted(player)) {
                 final long muteLength = config.getInt(ConfigEntry.MUTE_LENGTH) * 1000;
                 if (time > (action + muteLength)) {
                     unMutePlayer(player);
                 }
             }
             final long coolOff = config.getInt(ConfigEntry.COOL_OFF) * 1000;
             if ((time > (action + coolOff))
                     && (config.getInt(ConfigEntry.COOL_OFF) != 0)) {
                 if (beenKicked(player)) {
                     removeKickHistory(player);
                 }
                 if (beenMuted(player)) {
                     final Player onlinePlayer = Bukkit.getPlayer(player.getName());
                     if (onlinePlayer != null) {
                         Messager.good(Language.COOL_OFF_MESSAGE, onlinePlayer);
                     }
                     removeMuteHistory(player);
                 }
             }
         }
     }
 
     /**
      * Handle a player chat message
      * 
      * @param player the chatting player
      * @param message the message content
      * @return if the player is spamming
      */
     @Override
     public boolean handleChat(final OfflinePlayer player, final String message) {
         boolean isSpamming = false;
 
         // Detect rate limited messages
         ArrayDeque<Long> times = playerChatTimes.get(player.getName());
         if (times == null) {
             times = new ArrayDeque<Long>();
         }
         final long curtime = System.nanoTime() / 1000000;
         times.add(curtime);
         if (times.size() > config.getInt(ConfigEntry.MESSAGE_LIMIT)) {
             times.remove();
         }
         if (times.isEmpty()) {
             times.add(curtime);
         } else {
             final long timediff = times.getLast() - times.getFirst();
             if (timediff > (config.getInt(ConfigEntry.TIME_PERIOD) * 1000)) {
                 times.clear();
                 times.add(curtime);
             }
         }
         
         if (times.size() >= config.getInt(ConfigEntry.MESSAGE_LIMIT)) {
             isSpamming = true;
         }
         playerChatTimes.put(player.getName(), times);
 
         // Detect duplicate messages
         if (config.getBoolean(ConfigEntry.BLOCK_REPEATS) && !isSpamming) {
             ArrayDeque<String> playerChat = playerChatHistory.get(player.getName());
             if (playerChat == null) {
                 playerChat = new ArrayDeque<String>();
             }
             playerChat.add(message);
             if (playerChat.size() > (config.getInt(ConfigEntry.REPEAT_LIMIT) + 1)) {
                 playerChat.remove();
             }
             playerChatHistory.put(player.getName(), playerChat);
             isSpamming = hasDuplicateMessages(player);
         }
 
         if (isSpamming) {
             playerIsSpamming(player);
         }
         return isSpamming;
     }
 
     /**
      * Did a player say the same thing too often?
      * 
      * @param name the player to check
      * 
      * @return if the player has too many duplicates
      */
     @Override
     public boolean hasDuplicateMessages(final OfflinePlayer name) {
         if (Perms.BYPASS_REPEAT.has(Bukkit.getPlayer(name.getName()))) {
             return false; // if he has the permission, he never has any duplicates
         }
         
         boolean isSpamming = false;
         int samecount = 1;
         String lastMessage = null;
         for (Object m : playerChatHistory.get(name.getName()).toArray()) {
             final String message = m.toString();
             if (lastMessage == null) {
                 lastMessage = message;
                 continue;
             }
             if (message.equals(lastMessage)) {
                 samecount++;
             } else {
                 playerChatHistory.get(name.getName()).clear();
                 playerChatHistory.get(name.getName()).add(message);
                 break;
             }
             isSpamming = (samecount > config.getInt(ConfigEntry.REPEAT_LIMIT));
         }
         return isSpamming;
     }
 
     /**
      * Is a player muted?
      * 
      * @param name the player to check
      * 
      * @return if the player is muted
      */
     @Override
     public boolean isMuted(final OfflinePlayer name) {
         return mutedPlayers.contains(name.getName());
     }
 
     /**
      * Clean a player's history and ban him
      * 
      * @param player the player to ban
      */
     @Override
     public void kickPlayer(final OfflinePlayer player) {
         if (playerChatHistory.get(player.getName()) != null) {
             playerChatHistory.get(player.getName()).clear();
         }
         beenKickedPlayers.add(player.getName());
         playerActionTime.put(player.getName(), System.nanoTime() / 1000000);
         final Player onlinePlayer = player.getPlayer();
         if (onlinePlayer != null && !Perms.BYPASS_KICK.has(onlinePlayer)) {
             class RunLater implements Runnable {
                 @Override
                 public void run() {
                     onlinePlayer.kickPlayer(Language.KICK_MESSAGE.toString());
                     plugin.getLogger().log(Level.INFO, "Player kicked: {0}", player.getName());
                 }
             }
             Bukkit.getScheduler().runTaskLater(plugin, new RunLater(), 1L);
         }
     }
 
     /**
      * Clear a player's history and mute him
      * 
      * @param player the player to mute
      */
     @Override
     public void mutePlayer(final OfflinePlayer player) {
         mutedPlayers.add(player.getName());
         beenMutedPlayers.add(player.getName());
         playerActionTime.put(player.getName(), System.nanoTime() / 1000000);
         playerChatTimes.get(player.getName()).clear();
        if (playerChatHistory.containsKey(player.getName())) {
            playerChatHistory.get(player.getName()).clear();
        }
 
         final Player onlinePlayer = player.getPlayer();
         if (onlinePlayer != null) {
             Messager.bad(Language.MUTE, onlinePlayer, String.valueOf(config.getInt(ConfigEntry.MUTE_LENGTH)));
             plugin.getLogger().log(Level.INFO, "Player muted: {0}", player.getName());
         }
     }
 
     /**
      * A player is spamming. Punish him!
      * 
      * @param name the spamming player
      */
     private void playerIsSpamming(final OfflinePlayer name) {
         final boolean useMute = config.getBoolean(ConfigEntry.USE_MUTE);
         final boolean useKick = config.getBoolean(ConfigEntry.USE_KICK);
         final boolean useBan = config.getBoolean(ConfigEntry.USE_BAN);
         if(useMute && (!beenMuted(name) || (!useKick && !useBan))) {
             mutePlayer(name);
             return;
         }
         if (useKick && (!beenKicked(name) || !useBan)) {
             kickPlayer(name);
             return;
         }
         if (useBan) {
             banPlayer(name);
         }
     }
     
     /**
      * Clear a player's kick history
      * 
      * @param player the player to clear
      */
     @Override
     public void removeKickHistory(final OfflinePlayer player) {
         beenKickedPlayers.remove(player.getName());
     }
     
     /**
      * Clear a player's mute history
      * 
      * @param player the player to clear
      */
     @Override
     public void removeMuteHistory(final OfflinePlayer player) {
         beenMutedPlayers.remove(player.getName());
     }
 
     /**
      * Unban a player
      * 
      * @param player the player to unban
      */
     @Override
     public void unBanPlayer(final OfflinePlayer player) {
         player.setBanned(false);
     }
 
     /**
      * Unmute a player
      * 
      * @param player the player to unmute
      */
     @Override
     public void unMutePlayer(final OfflinePlayer player) {
         mutedPlayers.remove(player.getName());
         final Player onlinePlayer = player.getPlayer();
         if (onlinePlayer != null) {
             Messager.normal(Language.UNMUTE, onlinePlayer);
         }
     }
 }
