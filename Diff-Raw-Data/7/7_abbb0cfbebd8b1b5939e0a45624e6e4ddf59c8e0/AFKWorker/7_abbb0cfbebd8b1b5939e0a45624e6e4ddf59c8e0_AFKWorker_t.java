 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.worker
  * Created: 2013/01/02 0:10:20
  */
 package net.syamn.sakuracmd.worker;
 
 import static net.syamn.sakuracmd.storage.I18n._;
 
 import java.util.concurrent.ConcurrentHashMap;
 
 import net.syamn.sakuracmd.SCHelper;
 import net.syamn.sakuracmd.player.PlayerManager;
 import net.syamn.sakuracmd.storage.I18n;
 import net.syamn.utils.Util;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 /**
  * AFKWorker (AFKWorker.java)
  * @author syam(syamn)
  */
 public class AFKWorker {
     private final ConcurrentHashMap<Player, Long> playerTimeStamp = new ConcurrentHashMap<Player, Long>();
     private final ConcurrentHashMap<Player, Long> afkPlayers = new ConcurrentHashMap<Player, Long>();
     
     private final AfkChecker afkChecker;
     private static AFKWorker instance = new AFKWorker();
     
     private AFKWorker(){
         this.afkChecker = new AfkChecker();
     }
     
     public static AFKWorker getInstance(){
         if (instance == null){
             synchronized (AFKWorker.class) {
                 if (instance == null){
                     instance = new AFKWorker();
                 }
             }
         }
         return instance;
     }
     public static void dispose(){
         instance = null;
     }
     
     public AfkChecker getAfkChecker(){
         return afkChecker;
     }
     
     public void updateTimeStamp(final Player player){
         playerTimeStamp.put(player, System.currentTimeMillis());
     }
     
     public void removePlayer(final Player player){
         playerTimeStamp.remove(player);
         afkPlayers.remove(player);
     }
     
     // ** AFK status setter/getter 
     public void setAfk(final Player player){
         setAfk(player, null);
     }
     public void setAfk(final Player player, final String message){
         if (InvisibleWorker.getInstance().isInvisible(player)){
             return;
         }
         
         String afkMsg = _("afkOn", I18n.PLAYER, PlayerManager.getPlayer(player.getName()).getName());
         if (message != null && !message.isEmpty()) afkMsg += ": " + message;
         Util.broadcastMessage(afkMsg, false);
         
         afkPlayers.put(player, Long.valueOf(System.currentTimeMillis()));
         player.setSleepingIgnored(true);
     }
     public void setOnline(final Player player){
        if (!InvisibleWorker.getInstance().isInvisible(player)){
            Util.broadcastMessage(_("afkOff", I18n.PLAYER, PlayerManager.getPlayer(player.getName()).getName()), false);
         }
         
         afkPlayers.remove(player);
         player.setSleepingIgnored(false);
     }
     public boolean isAfk(final Player player){
         return afkPlayers.containsKey(player);
     }
     
     public void updatePlayer(final Player player){
         updateTimeStamp(player);
         if (isAfk(player)){
             setOnline(player);
         }
     }
     
     private class AfkChecker implements Runnable{
         @Override
         public void run(){
             final long now = System.currentTimeMillis();
             final int afkTime = SCHelper.getInstance().getConfig().getAfkTimeInSec() * 1000;
             
             for (final Player player : Bukkit.getOnlinePlayers()){
                 final Long last = playerTimeStamp.get(player);
                 if (last != null && !afkPlayers.containsKey(player) && (now - last) >= afkTime){
                     setAfk(player);
                 }
             }
         }
     }
 }
