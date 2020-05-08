 package me.limebyte.battlenight.core.util;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import me.limebyte.battlenight.api.battle.Battle;
 import me.limebyte.battlenight.api.util.Messenger;
 import me.limebyte.battlenight.core.BattleNight;
 import me.limebyte.battlenight.core.listeners.HealthListener.Accolade;
 import me.limebyte.battlenight.core.tosort.SafeTeleporter;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Sound;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.scheduler.BukkitRunnable;
 import org.bukkit.scheduler.BukkitTask;
 
 public class BattlePlayer {
 
     private static final int RESPAWN_TIME = 2;
     private static Map<String, BattlePlayer> players = new HashMap<String, BattlePlayer>();
 
     private static final Map<DamageCause, String> deathCauses;
     static {
         deathCauses = new HashMap<DamageCause, String>();
         deathCauses.put(DamageCause.BLOCK_EXPLOSION, "was blown up");
         deathCauses.put(DamageCause.CONTACT, "was pricked");
         deathCauses.put(DamageCause.CUSTOM, "was damaged by unknown");
         deathCauses.put(DamageCause.DROWNING, "drowned");
         deathCauses.put(DamageCause.ENTITY_ATTACK, "was slain");
         deathCauses.put(DamageCause.ENTITY_EXPLOSION, "was blown up");
         deathCauses.put(DamageCause.FALL, "fell to their death");
         deathCauses.put(DamageCause.FALLING_BLOCK, "was crushed");
         deathCauses.put(DamageCause.FIRE, "was set afire");
         deathCauses.put(DamageCause.FIRE_TICK, "was burnt");
         deathCauses.put(DamageCause.LAVA, "tried to swim in lava");
         deathCauses.put(DamageCause.LIGHTNING, "was struck by lightning");
         deathCauses.put(DamageCause.MAGIC, "was killed by magic");
         deathCauses.put(DamageCause.MELTING, "melted away");
         deathCauses.put(DamageCause.POISON, "was poisoned");
         deathCauses.put(DamageCause.PROJECTILE, "was shot");
         deathCauses.put(DamageCause.STARVATION, "starved");
         deathCauses.put(DamageCause.SUFFOCATION, "suffocated");
         deathCauses.put(DamageCause.SUICIDE, "commited suicide");
         deathCauses.put(DamageCause.THORNS, "was pricked");
         deathCauses.put(DamageCause.VOID, "fell into the void");
         deathCauses.put(DamageCause.WITHER, "withered away");
     }
 
     private String name;
     private PlayerStats stats;
 
     private boolean alive;
     private int respawnTaskID;
 
     private BattlePlayer(String name) {
         this.name = name;
         this.stats = new PlayerStats(name);
         this.alive = true;
     }
 
     public static BattlePlayer get(String name) {
         if (players.get(name) == null) {
             players.put(name, new BattlePlayer(name));
         }
         return players.get(name);
     }
 
     public static Map<String, BattlePlayer> getPlayers() {
         return players;
     }
 
     public String getName() {
         return name;
     }
 
     public PlayerStats getStats() {
         return stats;
     }
 
     public boolean isAlive() {
         return alive;
     }
 
     public void kill(Player killer, DamageCause cause, Accolade accolade) {
         if (!alive) return;
         alive = false;
         Player player = Bukkit.getPlayerExact(name);
         for (Player p : Bukkit.getOnlinePlayers()) {
             p.hidePlayer(player);
         }
 
         boolean suicide = true;
 
         if (killer != null && killer != player) {
             BattlePlayer.get(killer.getName()).getStats().addKill(false);
             killer.playSound(killer.getLocation(), Sound.LEVEL_UP, 20f, 1f);
             suicide = false;
         }
 
         killFeed(player, killer, cause, accolade);
         stats.addDeath(suicide);
         player.setAllowFlight(true);
         player.setFlying(true);
         player.setFlySpeed(0);
         BukkitTask task = new RespawnTask(player).runTaskTimer(BattleNight.instance, 0L, 1L);
         respawnTaskID = task.getTaskId();
     }
 
     public void revive() {
         if (alive) return;
         alive = true;
         Bukkit.getScheduler().cancelTask(respawnTaskID);
         Player player = Bukkit.getPlayerExact(name);
         Battle battle = BattleNight.instance.getAPI().getBattleManager().getBattle();
         battle.respawn(player);
         for (Player p : Bukkit.getOnlinePlayers()) {
             p.showPlayer(player);
         }
     }
 
     class RespawnTask extends BukkitRunnable {
         private static final float liftHeight = 1.0F;
 
         private Player player;
         private int timeRemaining;
         private float liftAmount;
         private boolean moveUp;
 
         public RespawnTask(Player player) {
             this.player = player;
             this.timeRemaining = RESPAWN_TIME * 20;
             this.liftAmount = liftHeight / timeRemaining;
             this.moveUp = player.getEyeLocation().clone().add(0, liftHeight, 0).getBlock().isEmpty();
         }
 
         @Override
         public void run() {
 
             if (moveUp) {
                 SafeTeleporter.telePass.add(player.getName());
                 player.teleport(player.getLocation().add(0, liftAmount, 0), TeleportCause.PLUGIN);
                 SafeTeleporter.telePass.remove(player.getName());
             }
 
             if (timeRemaining <= 0) {
                 revive();
                 return;
             }
 
             timeRemaining--;
         }
     }
 
     private static void killFeed(Player player, Player killer, DamageCause cause, Accolade accolade) {
         Messenger messenger = BattleNight.instance.getAPI().getMessenger();
 
         String causeMsg = deathCauses.get(cause);
         if (accolade != null) causeMsg = accolade.getDeathMessage();
         if (causeMsg == null) causeMsg = "died";
 
         String deathMessage = messenger.getColouredName(player) + " " + causeMsg;
 
        if (killer != null) {
             deathMessage += " by " + messenger.getColouredName(killer);
         }
 
         messenger.tellBattle(deathMessage + ".");
     }
 
 }
