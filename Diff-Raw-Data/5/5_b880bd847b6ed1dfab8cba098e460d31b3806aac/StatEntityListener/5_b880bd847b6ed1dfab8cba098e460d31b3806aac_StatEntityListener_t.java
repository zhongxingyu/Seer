 package me.tehbeard.BeardStat.listeners;
 
 import java.util.List;
 
 import me.tehbeard.BeardStat.BeardStat;
 import me.tehbeard.BeardStat.containers.PlayerStatManager;
 
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.ComplexEntityPart;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.ThrownPotion;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.entity.PotionSplashEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
 import org.bukkit.event.entity.EntityTameEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.potion.*;
 
 public class StatEntityListener implements Listener{
 
 
     List<String> worlds;
     private PlayerStatManager playerStatManager;
 
 
     public StatEntityListener(List<String> worlds,PlayerStatManager playerStatManager){
         this.worlds = worlds;
         this.playerStatManager = playerStatManager;
     }
 
     @EventHandler(priority=EventPriority.MONITOR)
     public void onEntityDamage(EntityDamageEvent event) {
 
         if(event.isCancelled()==false && !worlds.contains(event.getEntity().getWorld().getName())){
 
             Entity attacker = null;
             Projectile projectile = null;
             if(event instanceof EntityDamageByEntityEvent){
                 EntityDamageByEntityEvent ed = (EntityDamageByEntityEvent)event;
                 attacker = ed.getDamager();
 
                 
                 //handle arrow attacks
                 if(ed.getDamager() instanceof Projectile){
                     projectile  = ((Projectile)attacker);
                     attacker = projectile.getShooter();
                 }
             }
 
             Entity entity = event.getEntity();
             if(entity instanceof ComplexEntityPart){
                 entity = ((ComplexEntityPart)entity).getParent();
             }
             int damage = event.getDamage();
             DamageCause cause = event.getCause();
             //if the player gets attacked
             if(entity instanceof Player){
                 //global damage count
                 playerStatManager.getPlayerBlob(((Player)entity).getName()).getStat("damagetaken","total").incrementStat(damage);
                 //handle projectiles
                 if(projectile!=null){
                     playerStatManager.getPlayerBlob(((Player)entity).getName()).getStat("damagetaken",projectile.getType().toString().toLowerCase().replace("_", "")).incrementStat(damage);
                 }
 
                 playerStatManager.getPlayerBlob(((Player)entity).getName()).getStat("damagetaken", cause.toString().toLowerCase().replace("_","")).incrementStat(damage);
 
 
                 //pvp damage
                 if(attacker instanceof Player){
                     playerStatManager.getPlayerBlob(((Player)entity).getName()).getStat("damagetaken","player").incrementStat(damage);
                     playerStatManager.getPlayerBlob(((Player)attacker).getName()).getStat("damagedealt","player").incrementStat(damage);
                     //mob damage
                 } else if(attacker!=null){				
                     playerStatManager.getPlayerBlob(((Player)entity).getName()).getStat("damagetaken",attacker.getType().toString().toLowerCase().replace("_", "")).incrementStat(damage);
                 }
 
 
             }else{
                 if(attacker instanceof Player){
                     //global damage dealt
                     playerStatManager.getPlayerBlob(((Player)attacker).getName()).getStat("damagedealt","total").incrementStat(damage);
                     playerStatManager.getPlayerBlob(((Player)attacker).getName()).getStat("damagedealt",cause.toString().toLowerCase().replace("_","")).incrementStat(damage);
                     playerStatManager.getPlayerBlob(((Player)attacker).getName()).getStat("damagedealt",entity.getType().toString().toLowerCase().replace("_","")).incrementStat(damage);
                 }				
             }
 
         }
     }
 
     @EventHandler(priority=EventPriority.MONITOR)
     public void onEntityDeath(EntityDeathEvent event) {
 
        //ignore the blacklisted worlds
        if(worlds.contains(event.getEntity().getWorld().getName())){
            return;
        }

         EntityDamageEvent lastCause = event.getEntity().getLastDamageCause();
         DamageCause cause = null;
         if(lastCause!=null){
             cause = lastCause.getCause();
         }
 
         Entity attacker = null;
         Projectile projectile = null;
         if(lastCause instanceof EntityDamageByEntityEvent){
             attacker = ((EntityDamageByEntityEvent)lastCause).getDamager();
             BeardStat.printDebugCon("attack ID'd Fired");//Type.ENTITY_DEATH
             if(attacker instanceof Projectile){
                 projectile  = ((Projectile)attacker);
                 attacker = projectile.getShooter();
             }
         }
         Entity entity = event.getEntity();
 
         //set attacker and entity total k/d accordingly
         if(entity instanceof Player){
             playerStatManager.getPlayerBlob(((Player)entity).getName()).getStat("deaths","total").incrementStat(1);
             if(cause!=null){
                 playerStatManager.getPlayerBlob(((Player)entity).getName()).getStat("deaths",cause.toString().toLowerCase().replace("_","")).incrementStat(1);
             }
             if(projectile!=null){
                 playerStatManager.getPlayerBlob(((Player)entity).getName()).getStat("deaths",projectile.getType().toString().toLowerCase().replace("_", "")).incrementStat(1);
             }
         }
 
         if(attacker instanceof Player){
             playerStatManager.getPlayerBlob(((Player)attacker).getName()).getStat("kills","total").incrementStat(1);
             if(cause!=null){
                 playerStatManager.getPlayerBlob(((Player)attacker).getName()).getStat("kills",cause.toString().toLowerCase().replace("_","")).incrementStat(1);
             }
             if(projectile!=null){
                 playerStatManager.getPlayerBlob(((Player)attacker).getName()).getStat("kills",projectile.getType().toString().toLowerCase().replace("_", "")).incrementStat(1);
             }
 
         }
 
         //PVP
         if(entity instanceof Player && attacker instanceof Player){
             playerStatManager.getPlayerBlob(((Player)entity).getName()).getStat("deaths","player").incrementStat(1);
             playerStatManager.getPlayerBlob(((Player)attacker).getName()).getStat("kills","player").incrementStat(1);
         }
         //global damage count
 
         //PLAYER KILLS ENTITY
 
         if((entity instanceof Player)==false && attacker instanceof Player){
             //global damage dealt
             //playerStatManager.getPlayerBlob(((Player)attacker).getName()).getStat("kill_by_"+ cause.toString().toLowerCase()).incrementStat(1);
             playerStatManager.getPlayerBlob(((Player)attacker).getName()).getStat("kills", entity.getType().toString().replace("_", "").toLowerCase()).incrementStat(1);
         }				
         //ENTITY KILLS PLAYER
         if((entity instanceof Player) && !(attacker instanceof Player) && attacker !=null){
             playerStatManager.getPlayerBlob(((Player)entity).getName()).getStat("deaths",attacker.getType().toString().replace("_", "")).incrementStat(1);
 
 
         }
 
 
 
     }
     @EventHandler(priority=EventPriority.MONITOR)
     public void onEntityRegainHealth(EntityRegainHealthEvent event) {
 
         if(event.isCancelled()==false && event.getEntity() instanceof Player && !worlds.contains(event.getEntity().getWorld().getName())){
             int amount = event.getAmount();
             RegainReason reason = event.getRegainReason();
             playerStatManager.getPlayerBlob(((Player)event.getEntity()).getName()).getStat("stats","damagehealed").incrementStat(amount);
             if(reason != RegainReason.CUSTOM){
                 playerStatManager.getPlayerBlob(((Player)event.getEntity()).getName()).getStat("stats","heal" + reason.toString().replace("_", "").toLowerCase()).incrementStat(amount);	
             }
         }
     }
     @EventHandler(priority=EventPriority.MONITOR)
     public void onEntityTame(EntityTameEvent event) {
         if(event.isCancelled()==false && event.getOwner() instanceof Player && !worlds.contains(event.getEntity().getWorld().getName())){
             playerStatManager.getPlayerBlob(((Player)event.getOwner()).getName()).getStat("stats","tame"+event.getEntity().getType().toString().toLowerCase().replace("_", "")).incrementStat(1);
         }
     }
     @EventHandler(priority=EventPriority.MONITOR)
     public void onPotionSplash(PotionSplashEvent event){
         if(event.isCancelled()==false && !worlds.contains(event.getPotion().getWorld().getName())){
             ThrownPotion potion = event.getPotion();
 
             for(Entity e :event.getAffectedEntities()){
                 if(e instanceof Player){
                     Player p = (Player) e;
                     playerStatManager.getPlayerBlob(p.getName()).getStat("potions","splashhit").incrementStat(1);
                     //added per potion details
                     for(PotionEffect potionEffect : potion.getEffects()){
                         String effect = potionEffect.getType().toString().toLowerCase().replaceAll("_", "");
                         playerStatManager.getPlayerBlob(p.getName()).getStat("potions","splash" + effect).incrementStat(1);
                     }
                 }
             }
         }
     }
 
     @EventHandler(priority=EventPriority.MONITOR)
     public void onBowShoot(EntityShootBowEvent event){
 
         if(event.isCancelled()==false && !worlds.contains(event.getEntity().getWorld().getName())){
             if(event.getEntity() instanceof Player){
                 Player p = (Player) event.getEntity();
 
                 //total shots fired
                 playerStatManager.getPlayerBlob(p.getName()).getStat("bow","shots").incrementStat(1);
 
                 if(event.getBow().containsEnchantment(Enchantment.ARROW_FIRE)){
                     playerStatManager.getPlayerBlob(p.getName()).getStat("bow","fireshots").incrementStat(1);
                 }
 
                 if(event.getBow().containsEnchantment(Enchantment.ARROW_INFINITE)){
                     playerStatManager.getPlayerBlob(p.getName()).getStat("bow","infiniteshots").incrementStat(1);
                 }
 
             }
         }
     }
 }
