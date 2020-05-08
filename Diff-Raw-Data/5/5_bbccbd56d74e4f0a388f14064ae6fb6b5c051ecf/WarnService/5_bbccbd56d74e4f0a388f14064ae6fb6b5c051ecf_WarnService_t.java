 package de.cubenation.plugins.cnwarn.services;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.persistence.PersistenceException;
 
 import com.avaje.ebean.EbeanServer;
 import com.avaje.ebean.SqlQuery;
 import com.avaje.ebean.SqlRow;
 import com.avaje.ebean.Transaction;
 
 import de.cubenation.plugins.cnwarn.model.Warn;
 import de.cubenation.plugins.utils.ArrayConvert;
 import de.cubenation.plugins.utils.BukkitUtils;
 import de.cubenation.plugins.utils.EbeanHelper;
 
 /**
  * With this service, player warnings can be managed. Players warnings have a
  * expiration time that is configurable, default 30 days.
  * 
  * @since 1.1
  */
 public class WarnService {
     // external services
     private final EbeanServer conn;
     private final Logger log;
 
     // Hashset all (!online!) players with not accepted warnings
     private final HashSet<String> notAcceptedWarnedPlayerCache = new HashSet<String>();
 
     private int expirationDays = 30;
 
     private final SqlQuery sqlSumRating;
     private final SqlQuery sqlSearchWarnedPlayer;
     private final SqlQuery sqlLogBlockPlayer;
 
     /**
      * Initial with external services.
      * 
      * @param conn
      *            EbeanServer for database connection
      * @param log
      *            Logger for unexpected errors
      * 
      * @since 1.1
      */
     public WarnService(EbeanServer conn, Logger log) {
         this.conn = conn;
         this.log = log;
 
         sqlSumRating = conn.createSqlQuery("select sum(`rating`) as sumrating from `cn_warns` where lower(`playername`) = lower(:playerName) limit 1");
         sqlSearchWarnedPlayer = conn
                 .createSqlQuery("select distinct `playername` as playername from `cn_warns` where `playername` like :playerName order by `playername` limit 8");
         sqlLogBlockPlayer = conn.createSqlQuery("select * from `lb-players` where lower(`playername`) = lower(:playerName)");
     }
 
     /**
      * Set for expired warns rating value to zero.
      * 
      * @return count of warns that was cleared. On error -1 will be returned.
      * 
      * @since 1.1
      */
     public final int clearExpired() {
         Calendar cal = new GregorianCalendar();
         cal.clear(Calendar.HOUR_OF_DAY);
         cal.clear(Calendar.MINUTE);
         cal.clear(Calendar.SECOND);
         cal.clear(Calendar.MILLISECOND);
         cal.add(Calendar.DAY_OF_MONTH, -expirationDays);
 
         List<Warn> findList = conn.find(Warn.class).where().gt("rating", 0).lt("accepted", cal.getTime()).findList();
         for (Warn find : findList) {
             find.setRating(0);
         }
 
         Transaction transaction = conn.beginTransaction();
         try {
            conn.save(findList.iterator(), transaction);
             transaction.commit();
         } catch (PersistenceException e) {
             log.log(Level.SEVERE, "error on expired warns", e);
             EbeanHelper.rollbackQuiet(transaction);
 
             return -1;
         } finally {
             EbeanHelper.endQuiet(transaction);
         }
 
         return findList.size();
     }
 
     /**
      * Return the count of warns for a player.
      * 
      * @param playerName
      *            not case-sensitive player name
      * @return count of warns
      * 
      * @since 1.1
      */
     public final int getWarnCount(String playerName) {
         return conn.find(Warn.class).where().ieq("playername", playerName).findRowCount();
     }
 
     /**
      * Return the total sum ratings from all player warns.
      * 
      * @param playerName
      *            not case-sensitive player name
      * @return sum of warn ratings
      * 
      * @since 1.1
      */
     public final int getRatingSum(String playerName) {
         sqlSumRating.setParameter("playerName", playerName);
 
         Integer retInt = sqlSumRating.findUnique().getInteger("sumrating");
         return retInt == null ? 0 : retInt;
     }
 
     /**
      * Add a warn for a player. If player is only add cached not acceped warn
      * too.
      * 
      * @param warnedPlayerName
      *            player name that is warned
      * @param staffMemberName
      *            player name that warned the player
      * @param message
      *            warn reason
      * @param rating
      *            the heaviness of the warn
      * @return True, if warn was added. False, if warnedPlayerName or
      *         staffMemberName was null or empty. Also false, if adding was not
      *         successful.
      * 
      * @since 1.1
      */
     public final boolean addWarn(String warnedPlayerName, String staffMemberName, String message, Integer rating) {
         if (warnedPlayerName == null || staffMemberName == null || warnedPlayerName.isEmpty() || staffMemberName.isEmpty()) {
             return false;
         }
 
         Warn newWarn = new Warn();
         newWarn.setPlayerName(warnedPlayerName);
         newWarn.setStaffName(staffMemberName);
         newWarn.setMessage(message);
         newWarn.setRating(rating);
         newWarn.setCreated(new Date());
 
         Transaction transaction = conn.beginTransaction();
         try {
             conn.save(newWarn, transaction);
             transaction.commit();
         } catch (PersistenceException e) {
             log.log(Level.SEVERE, "error on save warn", e);
             EbeanHelper.rollbackQuiet(transaction);
 
             return false;
         } finally {
             EbeanHelper.endQuiet(transaction);
         }
 
         cacheNotAcceptedWarns(warnedPlayerName);
 
         return true;
     }
 
     /**
      * Delete warn by id. Remove cached not accepted warns for the player.
      * 
      * @param id
      *            warn id
      * @return True, if the warn was deleted successful. False, if the warn
      *         could not be found or deleted.
      * 
      * @since 1.1
      */
     public final boolean deleteWarn(int id) {
         Warn warn = conn.find(Warn.class, id);
         if (warn == null) {
             return false;
         }
 
         Transaction transaction = conn.beginTransaction();
         try {
             conn.delete(warn, transaction);
             transaction.commit();
         } catch (PersistenceException e) {
             log.log(Level.SEVERE, "error on delete warn", e);
             EbeanHelper.rollbackQuiet(transaction);
 
             return false;
         } finally {
             EbeanHelper.endQuiet(transaction);
         }
 
         removeCachedNotAcceptedWarns(warn.getPlayerName());
 
         return true;
     }
 
     /**
      * Delete all warns for a player. Remove cached not accepted warns for the
      * player.
      * 
      * @param playerName
      *            not case-sensitive player name
      * @return True, if warns was deleted successful. False, if the playerName
      *         is null or empty. Also false, if no warn exists for deletion.
      * 
      * @since 1.1
      */
     public final boolean deleteWarns(String playerName) {
         if (playerName == null || playerName.isEmpty()) {
             return false;
         }
 
         Set<Warn> warns = conn.find(Warn.class).where().ieq("playername", playerName).findSet();
         if (warns.size() == 0) {
             return false;
         }
 
         Transaction transaction = conn.beginTransaction();
         try {
             conn.delete(warns, transaction);
             transaction.commit();
         } catch (PersistenceException e) {
             log.log(Level.SEVERE, "error on delete warns", e);
             EbeanHelper.rollbackQuiet(transaction);
 
             return false;
         } finally {
             EbeanHelper.endQuiet(transaction);
         }
 
         removeCachedNotAcceptedWarns(playerName);
 
         return true;
     }
 
     /**
      * Accept all non accepted warns for the player. Remove cached not accepted
      * warns for the player.
      * 
      * @param playerName
      *            not case-sensitive player name
      * @return True, if the warns acceptance successful save. False, if the
      *         playerName is null or empty. Also false, if no warn exists for
      *         acceptance or saving is failed.
      * 
      * @since 1.1
      */
     public final boolean acceptWarns(String playerName) {
         if (playerName == null || playerName.isEmpty()) {
             return false;
         }
 
         Set<Warn> unAccWarns = conn.find(Warn.class).where().ieq("playername", playerName).isNull("accepted").findSet();
         if (unAccWarns.size() == 0) {
             return false;
         }
 
         for (Warn warn : unAccWarns) {
             warn.setAccepted(new Date());
         }
 
         Transaction transaction = conn.beginTransaction();
         try {
            conn.save(unAccWarns.iterator(), transaction);
             transaction.commit();
         } catch (PersistenceException e) {
             log.log(Level.SEVERE, "error on accept warn", e);
             EbeanHelper.rollbackQuiet(transaction);
 
             return false;
         } finally {
             EbeanHelper.endQuiet(transaction);
         }
 
         removeCachedNotAcceptedWarns(playerName);
 
         return true;
     }
 
     /**
      * Check if the player has not accepted warns (accepted date is not set)
      * against database.
      * 
      * @param playerName
      *            not case-sensitive player name
      * @return True, if not accepted warns exists in database. Otherwise false.
      *         Also false, if the playerName is null or empty.
      * 
      * @since 1.1
      */
     public final boolean hasPlayerNotAcceptedWarns(String playerName) {
         if (playerName == null || playerName.isEmpty()) {
             return false;
         }
 
         return conn.find(Warn.class).where().ieq("playername", playerName).isNull("accepted").findRowCount() > 0;
     }
 
     /**
      * Check if the player has not accepted warns (accepted date is not set)
      * against database.
      * 
      * @param playerName
      *            not case-sensitive player name
      * @return True, if not accepted warns exists in database. Otherwise false.
      *         Also false, if the playerName is null or empty.
      * 
      * @since 1.1
      */
     public final boolean isPlayersWarned(String playerName) {
         if (playerName == null || playerName.isEmpty()) {
             return false;
         }
 
         return conn.find(Warn.class).where().ieq("playername", playerName).findRowCount() > 0;
     }
 
     /**
      * Search player names with warns.
      * 
      * @param searchPattern
      *            not case-sensitive player name, wildcards '%' can be used.
      * @return List of player names that was found for the searchPattern and has
      *         warns. Return empty Collection, if the searchPattern is empty or
      *         null.
      * 
      * @since 1.1
      */
     public final Collection<String> searchPlayerWithWarns(String searchPattern) {
         if (searchPattern == null || searchPattern.isEmpty()) {
             return new ArrayList<String>();
         }
 
         sqlSearchWarnedPlayer.setParameter("playerName", "%" + searchPattern + "%");
         List<SqlRow> found = sqlSearchWarnedPlayer.findList();
 
         ArrayConvert<SqlRow> wc = new ArrayConvert<SqlRow>() {
             @Override
             protected String convertToString(SqlRow obj) {
                 return obj.getString("playername");
             }
         };
 
         return wc.toCollection(found);
     }
 
     /**
      * Search player warns.
      * 
      * @param searchPattern
      *            not case-sensitive player name, wildcards '%' can be used.
      * @return List of player names that was found for the searchPattern and has
      *         warns. Return empty List, if the searchPattern is empty or null.
      * 
      * @since 1.1
      */
     public final List<Warn> getWarnList(String searchPattern) {
         if (searchPattern == null || searchPattern.isEmpty()) {
             return new ArrayList<Warn>();
         }
 
         return conn.find(Warn.class).where().like("playername", "%" + searchPattern + "%").findList();
     }
 
     /**
      * Checks, if the player has not accpeted warns against cache.
      * 
      * @param playerName
      *            not case-sensitive player name
      * @return True, if the player is online. Otherwise false. Also false, if
      *         the playerName is null or empty.
      * 
      * @since 1.1
      */
     public final boolean cacheNotAcceptedWarns(String playerName) {
         if (playerName == null || playerName.isEmpty()) {
             return false;
         }
 
         if (BukkitUtils.isPlayerOnline(playerName)) {
             notAcceptedWarnedPlayerCache.add(playerName.toLowerCase());
 
             return true;
         }
 
         return false;
     }
 
     /**
      * Remove player from not accepted warns cache.
      * 
      * @param playerName
      *            not case-sensitive player name
      * @return True, if successful removed. False, if the playerName is null or
      *         empty.
      * 
      * @since 1.1
      */
     public final boolean removeCachedNotAcceptedWarns(String playerName) {
         if (playerName == null || playerName.isEmpty()) {
             return false;
         }
 
         notAcceptedWarnedPlayerCache.remove(playerName.toLowerCase());
 
         return true;
     }
 
     /**
      * Checks, if the player has not accpeted warns against cache.
      * 
      * @param playerName
      *            not case-sensitive player name
      * @return True, if the player has not accpeted warns. Otherwise false. Also
      *         false, if the playerName is null or empty.
      * 
      * @since 1.1
      */
     public final boolean hasPlayerNotAcceptedWarnsCached(String playerName) {
         if (playerName == null || playerName.isEmpty()) {
             return false;
         }
 
         return notAcceptedWarnedPlayerCache.contains(playerName.toLowerCase());
     }
 
     /**
      * Checks if the player was sometime before joined the server. Detailed it
      * will checked the LogBlock-player table for that, on the same database
      * connection.
      * 
      * If LogBlock-table not exists, fallback looks to player online status.
      * 
      * @param playerName
      * @return True, if the player had was online before, otherwise false. If
      *         the playerName is null or empty false will be returned.
      * 
      * @since 1.1
      * @see <a
      *      href="http://dev.bukkit.org/bukkit-plugins/logblock/">LogBlock</a>
      */
     public final boolean hasPlayedBefore(String playerName) {
         if (playerName == null || playerName.isEmpty()) {
             return false;
         }
 
         sqlLogBlockPlayer.setParameter("playerName", playerName);
 
         try {
             return (sqlLogBlockPlayer.findUnique() != null);
         } catch (PersistenceException e) {
             log.log(Level.SEVERE, "error on query LogBlock Table", e);
 
             // fallback if player is online
             return BukkitUtils.isPlayerOnline(playerName);
         }
     }
 
     /**
      * Calculate expiration date for warning against settings.
      * 
      * @param warn
      * @return Return the date or null, if warn or accepted date is null
      * 
      * @since 1.2
      */
     public final Date calculateExpirationDate(Warn warn) {
         if (warn == null || warn.getAccepted() == null) {
             return null;
         }
 
         GregorianCalendar acceptedDate = new GregorianCalendar();
         acceptedDate.setTime(warn.getAccepted());
         acceptedDate.add(Calendar.DAY_OF_MONTH, expirationDays);
 
         return acceptedDate.getTime();
     }
 
     /**
      * Return the settings expiration day.
      * 
      * @return Returns expiration days
      * 
      * @since 1.2
      */
     public final int getExpirationDays() {
         return expirationDays;
     }
 
     /**
      * Set the settings expiration day.
      * 
      * @param expirationDays
      *            Must be greater than 0 otherwise, statment will ignored.
      * @return False, if the expirationDays smaller or equals than 0, otherwise
      *         true
      * 
      * @since 1.2
      */
     public final boolean setExpirationDays(int expirationDays) {
         if (expirationDays <= 0) {
             return false;
         }
 
         this.expirationDays = expirationDays;
 
         return true;
     }
 }
