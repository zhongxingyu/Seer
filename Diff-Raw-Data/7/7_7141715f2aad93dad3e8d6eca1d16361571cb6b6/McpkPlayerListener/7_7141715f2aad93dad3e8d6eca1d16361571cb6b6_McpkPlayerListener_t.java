 
 package me.stutiguias.listeners;
 
 import java.sql.Date;
 import java.util.Calendar;
 import java.util.Map;
 import me.stutiguias.mcpk.Mcpk;
 import me.stutiguias.mcpk.PK;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 /**
  *
  * @author Stutiguias
  */
 public class McpkPlayerListener implements Listener {
     
     private final Mcpk plugin;
 
     public McpkPlayerListener(Mcpk plugin){
         this.plugin = plugin;
         
     }
     
     @EventHandler(priority = EventPriority.NORMAL)
     public void PlayerDeath(PlayerDeathEvent event) {
         if(!(event.getEntity().getKiller() instanceof Player)) return;
         String killer = event.getEntity().getKiller().getName();
         if(plugin.IsPk.containsKey(killer)){
             plugin.IsPk.get(killer).setTime(plugin.getCurrentMilli() + plugin.time);
             plugin.IsPk.get(killer).addKills(1);      
             for(Map.Entry<Integer,String> announcekills : plugin.pkmsg.entrySet())
             {
                if(plugin.IsPk.get(killer).getKills() == announcekills.getKey()) { 
                     plugin.getServer().broadcastMessage(parseColor(announcekills.getValue().replace("%player%", killer)));
                }
             }
         }else{
             PK newpk = new PK();
             newpk.setName(killer);
             newpk.setTime(plugin.getCurrentMilli() + plugin.time);
             plugin.IsPk.put(killer, newpk);
         }
         
     
     }
     
     private String parseColor(String message) {
 	 for (ChatColor color : ChatColor.values()) {
             message = message.replaceAll(String.format("&%c", color.getChar()), color.toString());
         }
         return message;
     }
     
     @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
     public void onDamage(EntityDamageEvent event){
       if(!plugin.usenewbieprotect) return;
       if(event instanceof EntityDamageByEntityEvent) {
         EntityDamageByEntityEvent EDE = (EntityDamageByEntityEvent)event;
         Entity attacker;
         if(event.getCause() == DamageCause.PROJECTILE)
         {
            attacker = ((Projectile) EDE.getDamager()).getShooter();
         }else {
            attacker = EDE.getDamager();
         }
        Entity defender;
        try {
            defender = EDE.getEntity();
        }catch(Exception e) {
            return;
        }
         
         if(attacker instanceof Player && defender instanceof Player) {
             Player df = (Player)defender;
             Player at = (Player)attacker;
             PK dfPkPlayer = plugin.DataBase.getPlayer(df.getName());
             PK atPkPlayer = plugin.DataBase.getPlayer(at.getName());
             Date dt = now();
             if(dt.before(dfPkPlayer.getNewBie()) || dt.before(atPkPlayer.getNewBie())) {
                 event.setCancelled(true);
                 event.setDamage(0);
             }
         }
          
       }
     }
     
     @EventHandler(priority= EventPriority.NORMAL)
     public void onPlayerJoin(PlayerJoinEvent event) {
         Player pl = event.getPlayer();
         PK pkPlayer = null;
         try {
           pkPlayer = plugin.DataBase.getPlayer(pl.getName());
         }catch(Exception e){
             e.printStackTrace();
         }
         if(pkPlayer == null) {
             Date dt = now();
             if(!plugin.usenewbieprotect) {
                 plugin.DataBase.createPlayer(pl.getName(), "0", 0,dt); 
                 Mcpk.log.info("[MCPK] New Player Found " + pl.getName());
             }else{
                 dt = addDays(dt, plugin.newbieprotectdays);
                 plugin.DataBase.createPlayer(pl.getName(), "0", 0,dt); 
                 pl.sendMessage(plugin.protecmsg.replace("%d%",String.valueOf(plugin.newbieprotectdays)).replace("%date%",dt.toString()));
                 Mcpk.log.info("[MCPK] New Player Found " + pl.getName() + " is protected until " + dt.toString());
             }
         }
         
     }
     
     public Date addDays(Date date, int days)
     {
         Calendar cal = Calendar.getInstance();
         cal.setTime(date);
         cal.add(Calendar.DATE, days); //minus number would decrement the days
         java.sql.Date dataSql = new java.sql.Date(cal.getTime().getTime()); 
         return dataSql;
     }
     
     public Date now() {
         java.util.Date dataUtil = new java.util.Date();  
         java.sql.Date dataSql = new java.sql.Date(dataUtil.getTime());  
         return dataSql;
     }
 }
