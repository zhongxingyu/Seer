 package de.cubenation.plugins.cnstats.services;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.persistence.PersistenceException;
 
 import org.bukkit.Bukkit;
 
 import com.avaje.ebean.EbeanServer;
 import com.avaje.ebean.QueryIterator;
 import com.avaje.ebean.Transaction;
 
 import de.cubenation.plugins.cnstats.CnStats;
 import de.cubenation.plugins.cnstats.model.OnlineTime;
 import de.cubenation.plugins.utils.EbeanHelper;
 
 /**
  * With this service, the player online times can be managed.
  * 
  * @since 1.1
  */
 public class TimeService {
     // external services
     private final CnStats plugin;
     private final EbeanServer conn;
     private final Logger log;
 
     private final HashMap<String, OnlineTime> openTimes = new HashMap<String, OnlineTime>();
     private final ArrayList<OnlineTime> closedTimes = new ArrayList<OnlineTime>();
 
     /**
      * Initial with external services.
      * 
      * @param plugin
      *            Plugin for add asynchron bukkit task.
      * @param conn
      *            EbeanServer for database connection
      * @param log
      *            Logger for unexpected errors
      * 
      * @since 1.1
      */
     public TimeService(CnStats plugin, EbeanServer conn, Logger log) {
         this.plugin = plugin;
         this.conn = conn;
         this.log = log;
     }
 
     /**
      * Saves and clear cached online times with log out date is set in an
      * asyncron bukkit task.
      * 
      * @since 1.1
      */
     public final void saveClosedTimes() {
         if (plugin.isEnabled()) {
             final ArrayList<OnlineTime> closedTimesCopy = new ArrayList<OnlineTime>(closedTimes);
             Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Thread("ClosedTimeSaver") {
                 @Override
                 public void run() {
                     saveClosedTimesCache(closedTimesCopy);
                 }
             });
         } else {
             saveClosedTimesCache(closedTimes);
         }
 
         closedTimes.clear();
     }
 
     private boolean saveClosedTimesCache(Collection<OnlineTime> closedTimes) {
         if (closedTimes == null || closedTimes.isEmpty()) {
             return false;
         }
 
         Transaction transaction = conn.beginTransaction();
         try {
             conn.save(closedTimes.iterator(), transaction);
             transaction.commit();
 
             return true;
         } catch (PersistenceException e) {
             log.log(Level.SEVERE, "error on save closed times", e);
             EbeanHelper.rollbackQuiet(transaction);
 
             return false;
         } finally {
             EbeanHelper.endQuiet(transaction);
         }
     }
 
     /**
      * Saves and clear cached online times with log out date is not set in an
      * asyncron bukkit task.
      * 
      * @since 1.1
      */
     public final void saveOpenTimes() {
         if (plugin.isEnabled()) {
             final ArrayList<OnlineTime> openTimesCopy = new ArrayList<OnlineTime>(openTimes.values());
             Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Thread("OpenTimeSaver") {
                 @Override
                 public void run() {
                     saveOpenTimesCache(openTimesCopy);
                 }
 
             });
         } else {
             saveOpenTimesCache(openTimes.values());
         }
 
         openTimes.clear();
     }
 
     private boolean saveOpenTimesCache(Collection<OnlineTime> openTimes) {
         if (openTimes == null || openTimes.isEmpty()) {
             return false;
         }
 
         Transaction transaction = conn.beginTransaction();
         try {
             conn.save(openTimes.iterator(), transaction);
             transaction.commit();
 
             return true;
         } catch (PersistenceException e) {
             log.log(Level.SEVERE, "error on save open times", e);
             EbeanHelper.rollbackQuiet(transaction);
 
             return false;
         } finally {
             EbeanHelper.endQuiet(transaction);
         }
     }
 
     /**
      * Set log out time for a player to cache. If it's not cached before, it
      * will be read from database.
      * 
      * @param playerName
      *            case-insensitive player name
      * @return True, if successful, otherwise false. Also false, if playerName
      *         is null or empty an online time was not exists in cache or
      *         database.
      * 
      * @since 1.1
      */
     public final boolean setLogOut(String playerName) {
         if (playerName == null || playerName.isEmpty()) {
             return false;
         }
 
         OnlineTime loginTime = openTimes.get(playerName);
         if (loginTime == null) {
            loginTime = conn.find(OnlineTime.class).setMaxRows(1).orderBy("id desc").where().ieq("playe_rname", playerName).findUnique();
         }
 
         if (loginTime != null) {
             loginTime.setLogoutTime(new Date());
             closedTimes.add(loginTime);
             openTimes.remove(playerName);
 
             return true;
         }
 
         return false;
     }
 
     /**
      * Set log in time for a player to cache.
      * 
      * @param playerName
      *            case-insensitive player name
      * @return True, if successful, otherwise false, if playerName is null or
      *         empty.
      * 
      * @since 1.1
      */
     public final boolean addLogIn(String playerName) {
         if (playerName == null || playerName.isEmpty()) {
             return false;
         }
 
         OnlineTime login = new OnlineTime();
         login.setPlayerName(playerName);
         login.setLoginTime(new Date());
 
         openTimes.put(playerName, login);
 
         return true;
     }
 
     /**
      * Set log in time for a player to cache.
      * 
      * @param playerName
      *            case-insensitive player name
      * @return Player online time in hours. Returns 0 if playerName is null or
      *         empty.
      * 
      * @since 1.1
      */
     public int getOnlineTime(String playerName) {
         if (playerName == null || playerName.isEmpty()) {
             return 0;
         }
 
        QueryIterator<OnlineTime> findIterate = conn.find(OnlineTime.class).where().ieq("player_name", playerName).isNotNull("login_time")
                .isNotNull("logout_time").findIterate();
 
         float milliTime = 0;
         while (findIterate.hasNext()) {
             OnlineTime time = findIterate.next();
             milliTime += time.getLogoutTime().getTime() - time.getLoginTime().getTime();
         }
 
         int hours = 0;
         if (milliTime > 0) {
             hours = Math.round(milliTime / (3600 * 1000));
         }
 
         return hours;
     }
 }
