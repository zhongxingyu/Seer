 package me.limebyte.battlenight.core.listeners;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.battle.Battle;
 import me.limebyte.battlenight.api.util.Messenger;
 import me.limebyte.battlenight.core.battle.SimpleBattle;
 import me.limebyte.battlenight.core.battle.SimpleTeamedBattle;
 import me.limebyte.battlenight.core.tosort.ConfigManager;
 import me.limebyte.battlenight.core.tosort.ConfigManager.Config;
 import me.limebyte.battlenight.core.util.PlayerStats;
 
 import org.bukkit.Bukkit;
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
 
 public class HealthListener extends APIRelatedListener {
 
     private static final Map<DamageCause, String> causes;
     static {
         causes = new HashMap<DamageCause, String>();
         causes.put(DamageCause.BLOCK_EXPLOSION, "was blown up");
         causes.put(DamageCause.CONTACT, "was pricked");
         causes.put(DamageCause.CUSTOM, "was damaged by unknown");
         causes.put(DamageCause.DROWNING, "drowned");
         causes.put(DamageCause.ENTITY_ATTACK, "was slain");
         causes.put(DamageCause.ENTITY_EXPLOSION, "was blown up");
         causes.put(DamageCause.FALL, "fell to their death");
         causes.put(DamageCause.FALLING_BLOCK, "was crushed");
         causes.put(DamageCause.FIRE, "was set afire");
         causes.put(DamageCause.FIRE_TICK, "was burnt");
         causes.put(DamageCause.LAVA, "tried to swim in lava");
         causes.put(DamageCause.LIGHTNING, "was struck by lightning");
         causes.put(DamageCause.MAGIC, "was killed by magic");
         causes.put(DamageCause.MELTING, "melted away");
         causes.put(DamageCause.POISON, "was poisoned");
         causes.put(DamageCause.PROJECTILE, "was shot");
         causes.put(DamageCause.STARVATION, "starved");
         causes.put(DamageCause.SUFFOCATION, "suffocated");
         causes.put(DamageCause.SUICIDE, "commited suicide");
         causes.put(DamageCause.THORNS, "was pricked");
         causes.put(DamageCause.VOID, "fell into the void");
         causes.put(DamageCause.WITHER, "withered away");
     }
 
     public HealthListener(BattleNightAPI api) {
         super(api);
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onEntityDamage(EntityDamageEvent event) {
         if (!(event.getEntity() instanceof Player)) return;
         Player player = (Player) event.getEntity();
         BattleNightAPI api = getAPI();
         Battle battle = api.getBattleManager().getBattle();
 
         if (api.getSpectatorManager().getSpectators().contains(player.getName())) {
             event.setCancelled(true);
             return;
         }
 
         if (!api.getLobby().contains(player) && !battle.containsPlayer(player)) return;
 
         if (event instanceof EntityDamageByEntityEvent) {
             EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
             subEvent.setCancelled(!canBeDamaged(player, battle, subEvent));
         }
 
         if (event.isCancelled()) return;
         if (battle != null && battle.isInProgress()) {
             if (event.getDamage() >= player.getHealth()) {
                player.getWorld().playSound(player.getLocation(), Sound.HURT, 20f, 1.5F);
                 Player killer = player.getKiller();
                 DamageCause cause = event.getCause();
 
                 killFeed(player, killer, cause);
                 battle.respawn(player);
                 event.setCancelled(true);
 
                 PlayerStats stats = PlayerStats.get(player.getName());
                 boolean suicide = true;
 
                 if (killer != null && killer != player) {
                     PlayerStats.get(killer.getName()).addKill(false);
                     battle.addKill(killer);
                    killer.playSound(killer.getLocation(), Sound.LEVEL_UP, 20f, 1.5f);
                     suicide = false;
                 }
 
                 stats.addDeath(suicide);
                 updateLeaders((SimpleBattle) battle, stats);
 
                 // Old Stuff
                 battle.addDeath(player);
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
 
     private boolean canBeDamaged(Player damaged, Battle battle, EntityDamageByEntityEvent event) {
         Entity eDamager = event.getDamager();
         Player damager;
 
         if (eDamager instanceof Projectile) {
             LivingEntity shooter = ((Projectile) eDamager).getShooter();
             if (shooter instanceof Player) {
                 damager = (Player) shooter;
             } else return true;
         } else {
             if (eDamager instanceof Player) {
                 damager = (Player) eDamager;
             } else return true;
         }
 
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
 
     private void killFeed(Player player, Player killer, DamageCause cause) {
         Messenger messenger = getAPI().getMessenger();
 
         String causeMsg = causes.get(cause);
         if (causeMsg == null) causeMsg = "died";
 
         String deathMessage = messenger.getColouredName(player) + " " + causeMsg;
 
         if (killer != null) {
             deathMessage += " by " + messenger.getColouredName(killer);
         }
 
         messenger.tellBattle(deathMessage + ".");
     }
 
     private void updateLeaders(SimpleBattle battle, PlayerStats stats) {
         int leadingScore = 0;
         Set<String> leaders = (battle).leadingPlayers;
         Player leader = Bukkit.getPlayerExact(leaders.iterator().next());
 
         if (leader != null) {
             leadingScore = PlayerStats.get(leader.getName()).getScore();
         }
 
         if (leadingScore > stats.getScore()) return;
         if (leadingScore < stats.getScore()) leaders.clear();
         leaders.add(leader.getName());
     }
 
 }
