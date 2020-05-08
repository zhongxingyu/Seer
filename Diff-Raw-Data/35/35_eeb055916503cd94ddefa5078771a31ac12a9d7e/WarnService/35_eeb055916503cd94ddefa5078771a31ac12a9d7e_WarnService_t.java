 package de.derflash.plugins.cnwarn.services;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 
 import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlQuery;
 
 import de.cubenation.plugins.utils.chatapi.ChatService;
 import de.derflash.plugins.cnwarn.model.ConfirmOfflineWarnTable;
 import de.derflash.plugins.cnwarn.model.Warn;
 
 public class WarnService {
     private EbeanServer dbConnection;
     private ChatService chatService;
 
     // Hashmap warned offline players until confirm
     private HashMap<Player, ConfirmOfflineWarnTable> offlineWarnings = new HashMap<Player, ConfirmOfflineWarnTable>();
     // Hashset all (!online!) players with not accepted warnings
     private HashSet<Player> notAccepted = new HashSet<Player>();
    private SqlQuery preparedSqlSumRating;
    private SqlQuery preparedSqlOfflinePlayer;
 
     public WarnService(EbeanServer dbConnection, ChatService chatService) {
         this.dbConnection = dbConnection;
         this.chatService = chatService;

        preparedSqlSumRating = dbConnection.createSqlQuery("select sum(rating) as sumrating from cn_warns where playername = :playerName limit 1");
        preparedSqlOfflinePlayer = dbConnection.createSqlQuery("select * from `lb-players` where lower(playername) = lower(:playerName)");
     }
 
     public void clearOld() {
         dbConnection.createSqlUpdate("update `cn_warns` set rating = 0 where to_days(now()) - to_days(`accepted`) > 30").execute();
     }
 
     private Integer getWarnCount(String playerName) {
         return dbConnection.find(Warn.class).where().ieq("playername", playerName).findRowCount();
     }
 
     private Integer getRatingSum(String playerName) {
        preparedSqlSumRating.setParameter("playerName", playerName);
        return preparedSqlSumRating.findUnique().getInteger("sumrating");
     }
 
     public void warnPlayer(String warnedPlayer, Player staffMember, String message, Integer rating) {
         Boolean wasWarned = warnedPlayersContains(warnedPlayer);
 
         Warn newWarn = new Warn();
         newWarn.setPlayername(warnedPlayer);
         newWarn.setStaffname(staffMember.getName());
         newWarn.setMessage(message);
         newWarn.setRating(rating);
         newWarn.setCreated(new Date());
         dbConnection.save(newWarn);
 
         chatService.one(staffMember, "staff.newWarn", warnedPlayer, message, rating.toString());
 
         if (wasWarned) {
             chatService.one(staffMember, "staff.warnExists", warnedPlayer, getWarnCount(warnedPlayer).toString(), getRatingSum(warnedPlayer).toString());
         }
 
         Player player = Bukkit.getServer().getPlayer(warnedPlayer);
         if (player != null) {
             notAccepted.add(player);
         }
     }
 
     public void warnOfflinePlayer(String playerName, Player player, String message, Integer rating) {
         offlineWarnings.put(player, new ConfirmOfflineWarnTable(playerName, message, rating));
         chatService.one(player, "staff.warnOffline", playerName);
     }
 
     public void confirmOfflinePlayerWarning(Player player) {
         if (offlineWarnings.containsKey(player)) {
             String playerName = offlineWarnings.get(player).playerName;
             String message = offlineWarnings.get(player).message;
             Integer rating = offlineWarnings.get(player).rating;
             offlineWarnings.remove(player);
 
             OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(playerName);
 
             if (!hasPlayedBefore(offlinePlayer)) {
                 chatService.one(player, "staff.playerNotJoinedBefore", offlinePlayer.getName());
             } else {
                 warnPlayer(playerName, player, message, rating);
             }
         } else {
             chatService.one(player, "staff.noOfflineWarn");
         }
     }
 
     public void deleteWarning(Integer id, Player staffplayer) {
         String playerName = getPlayerNameFromId(id);
         if (playerName != null) {
             dbConnection.delete(Warn.class, id);
 
             Player onlinePlayer = Bukkit.getServer().getPlayer(playerName);
             if (onlinePlayer != null) {
                 notAccepted.remove(onlinePlayer);
             }
 
             chatService.one(staffplayer, "staff.warnDeleted", id);
         }
     }
 
     private String getPlayerNameFromId(Integer id) {
         Warn warn = dbConnection.find(Warn.class, id);
         if (warn != null) {
             return warn.getPlayername();
         }
         return null;
     }
 
     public void deleteWarnings(String playerName, Player staffplayer) {
         Set<Warn> warns = dbConnection.find(Warn.class).where().ieq("playername", playerName).findSet();
         dbConnection.delete(warns);
 
         Player onlinePlayer = Bukkit.getServer().getPlayer(playerName);
         if (onlinePlayer != null) {
             notAccepted.remove(onlinePlayer);
         }
 
         chatService.one(staffplayer, "staff.deleteAllWarn", playerName);
     }
 
     public void acceptWarnings(String playerName) {
         Set<Warn> unAccWarns = dbConnection.find(Warn.class).where().ieq("playername", playerName).isNull("accepted").findSet();
         for (Warn warn : unAccWarns) {
             warn.setAccepted(new Date());
         }
         dbConnection.save(unAccWarns);
 
         Player onlinePlayer = Bukkit.getServer().getPlayer(playerName);
         if (onlinePlayer != null) {
             notAccepted.remove(onlinePlayer);
         }
     }
 
     public boolean hasUnacceptedWarnings(String playerName) {
         return dbConnection.find(Warn.class).where().ieq("playername", playerName).isNull("accepted").findRowCount() > 0;
     }
 
     public boolean warnedPlayersContains(String playerName) {
         return dbConnection.find(Warn.class).where().ieq("playername", playerName).findRowCount() > 0;
     }
 
     public void showSuggestions(String playerName, Player player) {
         chatService.one(player, "staff.searchWarnedPlayers", playerName);
 
        List<Warn> found = dbConnection.find(Warn.class).where().like("playername", "%" + playerName + "%").setMaxRows(8).setDistinct(true).findList();
 
         if (found.isEmpty()) {
             chatService.one(player, "staff.noSearchEntries");
         } else {
             String out = "";
            for (Warn row : found) {
                String name = row.getPlayername();
                 if (out.length() == 0) {
                    out = name;
                 } else {
                    out = out + ", " + name;
                 }
             }
             chatService.one(player, "staff.searchEntries", out);
         }
     }
 
     public List<Warn> getWarnList(String playerName) {
         return dbConnection.find(Warn.class).where().like("playername", "%" + playerName + "%").findList();
     }
 
     public void addNotAccepted(Player player) {
         notAccepted.add(player);
     }
 
     public void removeNotAccepted(String playerName) {
         notAccepted.remove(playerName);
     }
 
     public boolean containsNotAccepted(Player player) {
         return notAccepted.contains(player);
     }
 
     public boolean hasPlayedBefore(OfflinePlayer player) {
        preparedSqlOfflinePlayer.setParameter("playerName", player.getName());

        return (preparedSqlOfflinePlayer.findUnique() != null);
     }
 }
