 package fr.noogotte.useful_commands.component;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.event.entity.FoodLevelChangeEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import fr.noogotte.useful_commands.UsefulCommandPlugin;
 
 public class AfkComponent  extends Component implements Listener {
 
     private final Set<Player> afks;
 
     public AfkComponent(UsefulCommandPlugin plugin) {
         super(plugin);
         afks = new HashSet<Player>();
     }
 
     public void addPlayer(Player player) {
         afks.add(player);
         if (afks.size() == 1) {
             registerEvents(this);
         }
     }
 
     public void removePlayer(Player player) {
         afks.remove(player);
         if (afks.size() == 0) {
             unregisterEvents(this);
         }
     }
 
    public boolean isAfk(Player player) {
        return afks.contains(player);
     }
 
     @EventHandler
     public void onQuit(PlayerQuitEvent event) {
         Player player = event.getPlayer();
         if (isAfk(player)) {
             removePlayer(player);
         }
     }
 
     @EventHandler
     public void onMove(PlayerMoveEvent event) {
         if (isAfk(event.getPlayer())) {
             Location location = event.getPlayer().getLocation();
             Location from = event.getFrom();  
             Location to = event.getTo();
             if (from.getBlockX() != to.getBlockX()
                     || from.getBlockY() != to.getBlockY()
                     || from.getBlockZ() != to.getBlockZ()
                     || !from.getWorld().equals(to.getWorld())) {
                 event.setTo(location);
             }
         }
     }
 
     @EventHandler
     public void onDropItem(PlayerDropItemEvent event) {
         Player player = event.getPlayer();
         if (isAfk(player)) {
             event.setCancelled(true);
             player.sendMessage(ChatColor.RED
                     + "Vous êtes AFK, vous ne pouvez pas dropper d'objet !");
         }
     }
 
     @EventHandler
     public void onInteract(PlayerInteractEvent event) {
         Player player = event.getPlayer();
         if (isAfk(player)) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void onInteractEntity(PlayerInteractEntityEvent event) {
         Player player = event.getPlayer();
         if (isAfk(player)) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void onRegainHealth(EntityRegainHealthEvent event) {
         Entity entity = event.getEntity();
         if (entity instanceof Player && isAfk((Player) entity)) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void onFoodLevelChange(FoodLevelChangeEvent event) {
         Entity entity = event.getEntity();
         if (entity instanceof Player && isAfk((Player) entity)) {	
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void onTarget(EntityTargetEvent event) {
         Entity entity = event.getTarget();
         if (entity instanceof Player && isAfk((Player) entity)) {
             event.setCancelled(true); 
         }
     }
 
     @EventHandler
     public void onExplode(EntityExplodeEvent event) {
         Entity entity = event.getEntity();
         if (entity instanceof Creature) {
             LivingEntity target = ((Creature) entity).getTarget();
             if (target instanceof Player && isAfk((Player) target)) {
                 event.setCancelled(true);
             }
         }
     }
 
     @EventHandler
     public void onPickupItem(PlayerPickupItemEvent event) {
         if(isAfk(event.getPlayer())) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void entityDamageByEntity(EntityDamageByEntityEvent event) {
        if(isAfk((Player) event.getDamager())) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void entityDamage(EntityDamageEvent event) {
         Entity entity = event.getEntity();
         if (entity instanceof Player && isAfk((Player) entity)) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void onChat(PlayerChatEvent event) {
         if(isAfk(event.getPlayer())) {
             event.setCancelled(true);
             event.getPlayer().sendMessage(ChatColor.RED + "Vous ne pouvez pas parler, vous êtes AFK");
         }
     }
 }
 
