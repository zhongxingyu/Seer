 package me.limebyte.battlenight.core.listeners;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.battle.Battle;
 import me.limebyte.battlenight.core.battle.SimpleTeamedBattle;
 import me.limebyte.battlenight.core.tosort.ConfigManager;
 import me.limebyte.battlenight.core.tosort.ConfigManager.Config;
 import me.limebyte.battlenight.core.util.BattlePlayer;
 
 import org.bukkit.Location;
 import org.bukkit.Sound;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityRegainHealthEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
 import org.bukkit.util.Vector;
 
 public class HealthListener extends APIRelatedListener {
 
     public HealthListener(BattleNightAPI api) {
         super(api);
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onEntityDamage(EntityDamageEvent event) {
         if (!(event.getEntity() instanceof Player)) return;
         Player player = (Player) event.getEntity();
         Player damager = getKiller(event);
         BattleNightAPI api = getAPI();
         Battle battle = api.getBattleManager().getBattle();
 
         BattlePlayer bPlayer = BattlePlayer.get(player.getName());
         if (!bPlayer.isAlive()) {
             event.setCancelled(true);
             return;
         }
 
         if (api.getSpectatorManager().getSpectators().contains(player.getName())) {
             event.setCancelled(true);
             return;
         }
 
         if (!api.getLobby().contains(player) && !battle.containsPlayer(player)) return;
 
         if (event instanceof EntityDamageByEntityEvent) {
             EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
             subEvent.setCancelled(!canBeDamaged(player, damager, battle));
         }
 
         if (event.isCancelled()) return;
         if (battle != null && battle.isInProgress()) {
             if (event.getDamage() >= player.getHealth()) {
                 player.getWorld().playSound(player.getLocation(), Sound.HURT_FLESH, 20f, 1f);
                 event.setCancelled(true);
                 DamageCause cause = event.getCause();
                 if (damager == null) damager = player.getKiller();
                 Accolade accolade = Accolade.get(player, damager, cause);
 
                 bPlayer.kill(damager, cause, accolade);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onEntityRegainHealth(EntityRegainHealthEvent event) {
         if (!(event.getEntity() instanceof Player)) return;
         Player player = (Player) event.getEntity();
 
         if (!ConfigManager.get(Config.MAIN).getBoolean("StopHealthRegen", true)) return;
         if (!getAPI().getBattleManager().getBattle().containsPlayer(player)) return;
 
         RegainReason reason = event.getRegainReason();
         if (reason == RegainReason.REGEN || reason == RegainReason.SATIATED) {
             event.setCancelled(true);
         }
     }
 
     private boolean canBeDamaged(Player damaged, Player damager, Battle battle) {
         if (damager == null) return true;
 
         BattlePlayer bDamager = BattlePlayer.get(damager.getName());
         if (!bDamager.isAlive()) return false;
 
         if (getAPI().getSpectatorManager().getSpectators().contains(damager.getName())) return false;
 
         if (getAPI().getLobby().contains(damaged)) return false;
 
         if (battle.containsPlayer(damager)) {
             if (damager == damaged) return true;
 
             if (battle instanceof SimpleTeamedBattle) {
                 if (((SimpleTeamedBattle) battle).areEnemies(damager, damaged)) return true;
                 return ConfigManager.get(Config.MAIN).getBoolean("FriendlyFire", false);
             }
 
             return true;
         }
 
         return true;
     }
 
     private Player getKiller(EntityDamageEvent event) {
         Player killer = null;
         if (event instanceof EntityDamageByEntityEvent) {
             Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
             if (damager instanceof Projectile) {
                 LivingEntity shooter = ((Projectile) damager).getShooter();
                 if (shooter instanceof Player) killer = (Player) shooter;
             } else {
                 if (damager instanceof Player) killer = (Player) damager;
             }
         }
         return killer;
     }
 
     public enum Accolade {
         BACKSTAB("$k backstabbed $p.");
 
         private String deathMessage;
 
         private Accolade(String deathMessage) {
             this.deathMessage = deathMessage;
         }
 
         public String getDeathMessage() {
             return deathMessage;
         }
 
         private static Accolade get(Player player, Player killer, DamageCause cause) {
             // Backstab
            if (cause == DamageCause.ENTITY_ATTACK && killer != null) {
                Location playerLoc = player.getLocation();
                Location killerLoc = killer.getLocation();
                 Vector playerVec = playerLoc.getDirection();
                 Vector killerVec = killerLoc.getDirection();
                 float angle = playerVec.angle(killerVec);
                 double range = Math.PI / 3;
                 if (angle <= range) return BACKSTAB;
             }
 
             return null;
         }
     }
 
 }
