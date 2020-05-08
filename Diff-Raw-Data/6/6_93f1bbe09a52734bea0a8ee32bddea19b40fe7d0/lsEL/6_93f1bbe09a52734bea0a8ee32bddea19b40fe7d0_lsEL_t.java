 package me.aRt3m1s.ls;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.entity.*;
 
 import java.util.Random;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Christian
  * Date: 9/16/11
  * Time: 7:48 AM
  * To change this template use File | Settings | File Templates.
  */
 public class lsEL extends EntityListener{
     public static LongShot plugin;
     public lsEL(LongShot instance) {
         plugin = instance;
     }
     public void onEntityDamage(EntityDamageEvent event){
         if(event instanceof EntityDamageByEntityEvent){
             EntityDamageByEntityEvent ee = (EntityDamageByEntityEvent)event;
             if(ee.getDamager() instanceof Projectile){
                 Projectile projectile =(Projectile) ee.getDamager();
                 if(projectile.getShooter() instanceof Player){
                     Player player = (Player) projectile.getShooter();
                     if(ee.getEntity() instanceof LivingEntity){
                         LivingEntity damagee = (LivingEntity) ee.getEntity();
                         double distance = damagee.getLocation().distance(player.getLocation());
                         int finalDamage = getFinalDamage(distance, ee.getDamage());
                         ee.setDamage(finalDamage);
                         ee.setCancelled(false);
                     }
                 }
             }
         }
     }
     public void onEntityDeath(EntityDeathEvent event){
         if(event.getEntity() instanceof Player){
             Player player = (Player) event.getEntity();
             EntityDamageEvent e = player.getLastDamageCause();
             if(e!=null){
                 if(e.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)){
                     if(e instanceof EntityDamageByEntityEvent){
                         if(((EntityDamageByEntityEvent) e).getDamager() instanceof Player){
                             if(plugin.config.getInt("LongShot.distance/BLOCKS-damagePlus", 1)<=0){
                                 Player damager = (Player)((EntityDamageByEntityEvent) e).getDamager();
                                for(Player allOnlinePlayer: plugin.getServer().getOnlinePlayers()){
                                    allOnlinePlayer.sendMessage(ChatColor.GOLD+player.getName()+
                                            " has been sniped by "+damager.getName());
                                }
                             }
                         }
                     }
                 }
             }
         }
     }
 
     private int getFinalDamage(double distance, int damage) {
         int dBdP = plugin.config.getInt("LongShot.distance/BLOCKS-damagePlus", 1);
         boolean dTOF = plugin.config.getBoolean("LongShot.critical-hits.double", true);
         int range = plugin.config.getInt("LongShot.critical-hits.random-range", 20);
         if(dBdP>0){
             damage += (int)distance/dBdP;
             return damage;
         }else{
             if(dTOF){
                 damage *= 2;
             }else{
                 Random generator2 = new Random( 19580427 );
                 int plusDamage = generator2.nextInt(range);
                 damage += plusDamage;
             }
             return damage;
         }
     }
 }
