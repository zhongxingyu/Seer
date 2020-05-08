 package edgruberman.bukkit.guillotine;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 import java.util.logging.Level;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Skull;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.SkullMeta;
 import org.bukkit.plugin.Plugin;
 
 /** makes heads appear from dead bodies **/
 public final class Executioner implements Listener {
 
     private final Random rng = new Random();
     private final Plugin plugin;
 
     /** (victim, (killer, rate)) null victim is default for all victims, null killer is default chance for victim */
     private final Map<EntityType, Map<EntityType, Double>> instructions = new HashMap<EntityType, Map<EntityType, Double>>();
 
     Executioner(final Plugin plugin) {
         this.plugin = plugin;
     }
 
     public void putInstruction(final EntityType type, final Map<EntityType, Double> rates) {
         this.instructions.put(type, rates);
     }
 
     @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR) // give everything else a chance to cancel it
     public void onEntityDeathByEntity(final EntityDeathEvent death) {
         // ignore events when no applicable instruction exists for victim
         Map<EntityType, Double> killers = this.instructions.get(death.getEntityType());
         if (killers == null) killers = this.instructions.get(null);
         if (killers == null) return;
 
        // ignore if killer is not an entity
         final EntityDamageEvent last = death.getEntity().getLastDamageCause();
        if (!(last instanceof EntityDamageByEntityEvent)) return;
 
        // ignore when no applicable rate exists for killer
        final EntityDamageByEntityEvent cause = (EntityDamageByEntityEvent) last;
        final Entity origin = Executioner.origin(cause.getDamager());
        Double rate = killers.get(( origin != null ? origin.getType() : null ));
         if (rate == null) rate = killers.get(null);
         if (rate == null) return;
 
         // randomize creation of head
         final double picked = rate < 1 ? this.rng.nextDouble() : 1;
         if (rate < picked) {
             this.plugin.getLogger().log(Level.FINEST, "Missed head drop chance; victim: {0}, killer: {1}, rate: {2,number,#.#%}, picked: {3,number,#.##%}"
                    , new Object[] { new LazyDescriber(death.getEntity()), new LazyDescriber(cause.getDamager()), rate, picked });
             return;
         }
 
         // create head
         final ItemStack skull = SkullType.of(death.getEntity()).toItemStack();
         final SkullMeta meta = (SkullMeta) skull.getItemMeta();
         if (SkullType.HUMAN.matches(skull)) {
             final String victim = ( death.getEntity() instanceof Player ? ((Player) death.getEntity()).getName() : null );
             meta.setOwner(victim);
         }
         skull.setItemMeta(meta);
 
         // drop head
         final Location drop = death.getEntity().getLocation();
         drop.getWorld().dropItemNaturally(drop, skull);
        this.plugin.getLogger().log(Level.FINEST, "Head dropped; victim: {0}, killer: {1}, rate: {2,number,#.#%}, picked: {3,number,#.##%}"
                , new Object[] { new LazyDescriber(death.getEntity()), new LazyDescriber(cause.getDamager()), rate, picked });
     }
 
     @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR) // give everything else a chance to cancel it
     public void onPlayerSkullBlockRightClick(final PlayerInteractEvent interact) {
         if (interact.getAction() != Action.RIGHT_CLICK_BLOCK) return;
         if (interact.getClickedBlock().getTypeId() != Material.SKULL.getId()) return;
         final Skull state = (Skull) interact.getClickedBlock().getState();
         if (state.getSkullType() != org.bukkit.SkullType.PLAYER) return;
 
         Main.courier.send(interact.getPlayer(), "describe", state.getOwner());
     }
 
 
 
     /** @return origin of entity (shooter for projectiles, null for dispensers) */
     private static Entity origin(final Entity entity) {
         if (entity instanceof Projectile) {
             final Projectile projectile = (Projectile) entity;
             return projectile.getShooter();
         }
 
         return entity;
     }
 
     /** @return human readable description of entity */
     private static String describeOrigin(final Entity entity) {
         final Entity source = Executioner.origin(entity);
 
         if (source == null) {
            return "null(dispenser?)";
 
         } else if (source.getType() == EntityType.PLAYER) {
             return source.getType().name() + "(" + ((Player) source).getName() + ")";
 
         }
 
         return source.getType().name();
     }
 
 
 
     /** delays expansion of string describing entity in the case where logging might not display it */
     private static class LazyDescriber {
 
         private final Entity entity;
 
         private LazyDescriber(final Entity entity) {
             this.entity = entity;
         }
 
         @Override
         public String toString() {
             return Executioner.describeOrigin(this.entity);
         }
 
     }
 
 }
