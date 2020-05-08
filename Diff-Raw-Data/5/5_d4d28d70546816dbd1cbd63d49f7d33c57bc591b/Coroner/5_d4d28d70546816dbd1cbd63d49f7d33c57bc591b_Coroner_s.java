 package edgruberman.bukkit.obituaries;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Material;
 import org.bukkit.entity.AnimalTamer;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Tameable;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.Plugin;
 
 /**
  * Monitors damage and death events, examines death events and reports findings.
  * Coordinates activities between other investigation classes.
  */
 class Coroner implements Listener {
 
     final Plugin plugin;
     final Translator translator;
     final FireInvestigator investigator;
     final Alchemist alchemist;
     final Map<Entity, Damage> damages = new HashMap<Entity, Damage>();
 
     Coroner (final Plugin plugin, final Translator translator) {
         this.plugin = plugin;
         this.translator = translator;
         this.investigator = new FireInvestigator(this);
         this.alchemist = new Alchemist(this);
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     void clear() {
         HandlerList.unregisterAll(this);
         this.investigator.clear();
         this.alchemist.clear();
         this.damages.clear();
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onEntityDamage(final EntityDamageEvent event) {
         if (!(event.getEntity() instanceof Player)) return;
 
         final Damage damage = new Damage(event);
         this.damages.put(damage.event.getEntity(), damage);
     }
 
    @EventHandler(priority = EventPriority.HIGH) // Before MessageFormatter
     public void onPlayerDeath(final PlayerDeathEvent death) {
         // Create unknown damage report if none previously recorded
         if (!this.damages.containsKey(death.getEntity()))
             this.onEntityDamage(new EntityDamageEvent(death.getEntity(), DamageCause.CUSTOM, 0));
 
         final String message = this.describeDeath(death.getEntity());
         this.remove(death.getEntity());
 
         // Leave default death message if no format specified
         if (message == null) return;
 
         // Show custom death message
         death.setDeathMessage(message);
     }
 
    @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerQuit(final PlayerQuitEvent quit) {
         this.remove(quit.getPlayer());
     }
 
     private void remove(final Entity entity) {
         this.damages.remove(entity);
         this.alchemist.remove(entity);
         this.investigator.remove(entity);
     }
 
     private String describeDeath(final Entity entity) {
         final Damage kill = this.damages.get(entity);
         final String format = this.translator.getDeathMessageFormat(kill.event.getCause());
         if (format == null) return null;
 
         return String.format(format, this.translator.formatName(kill.event.getEntity()), this.describeSource(kill));
     }
 
     private String describeSource(final Damage damage) {
         String description = null;
 
         switch (damage.event.getCause()) {
 
         // Material
         case BLOCK_EXPLOSION:
             if (damage.sourceBlock == null) {
                 // Possibility might exist in CraftBukkit to return a null block for a TNT explosion
                 description = this.translator.formatMaterial(Material.TNT);
             } else {
                 description = this.translator.formatMaterial(damage.sourceBlock);
             }
             break;
 
         // Material
         case CONTACT:
         case SUFFOCATION:
             description = this.translator.formatMaterial(damage.sourceBlock);
             break;
 
         case ENTITY_EXPLOSION: // Entity
             final Entity exploder = ((EntityDamageByEntityEvent) damage.event).getDamager();
             description = this.describeEntity(exploder);
             break;
 
         case ENTITY_ATTACK: // Weapon
             description = this.describeAttacker(damage);
             break;
 
         case PROJECTILE: // Shooter
             final Entity projectile = ((EntityDamageByEntityEvent) damage.event).getDamager();
             description = this.describeEntity(projectile);
             break;
 
         // Potion effects
         case MAGIC:
             description = this.alchemist.getPotion(damage.event.getEntity());
             break;
 
         // Distance fallen
         case FALL:
             description = Integer.toString(damage.event.getDamage() + 3);
             break;
 
         // Combuster
         case FIRE_TICK:
             description = this.investigator.getCombuster(damage.event.getEntity());
             break;
 
         // Lightning
         case LIGHTNING:
             final Entity lightning = ((EntityDamageByEntityEvent) damage.event).getDamager();
             description = this.describeEntity(lightning);
             break;
 
         default:
             break;
 
         }
 
         return description;
     }
 
     /**
      * Describes an entity under the context of being killed by it. Players will
      * use their display names. Other entities will default to their Bukkit
      * class name if a config.yml localized name does not match. Projectiles,
      * Tameables, and Vehicles will include descriptions of their owners if
      * a format in the language file is specified.
      *
      * Examples:
      *   Player = EdGruberman
      *   Arrow = EdGruberman with an arrow
      *   Fireball = a ghast with a fireball
      */
     String describeEntity(final Entity entity) {
         String description = this.translator.formatName(entity);
 
         if (entity instanceof Tameable) {
             final AnimalTamer tamer = ((Tameable) entity).getOwner();
             if (tamer instanceof Entity && this.translator.owners.containsKey("Tameable"))
                 description = String.format(this.translator.owners.get("Tameable"), description, this.describeEntity((Entity) tamer));
         }
 
         if (entity instanceof Projectile && this.translator.owners.containsKey("Projectile")) {
             final LivingEntity shooter = ((Projectile) entity).getShooter();
             String shooterName = null;
             if (shooter == null) {
                 shooterName = this.translator.formatMaterial(Material.DISPENSER);
             } else if (shooter instanceof Entity) {
                 shooterName = this.describeEntity(shooter);
             }
             description = String.format(this.translator.owners.get("Projectile"), description, shooterName);
         }
 
         // Vehicle
         if (!entity.isEmpty() && this.translator.owners.containsKey("Vehicle")) {
             description = String.format(this.translator.owners.get("Vehicle"), description, this.describeEntity(entity.getPassenger()));
         }
 
         return description;
     }
 
     private String describeAttacker(final Damage damage) {
         final Entity attacker = ((EntityDamageByEntityEvent) damage.event).getDamager();
         final String attackerName = this.describeEntity(attacker);
 
         String weapon = null;
         if (damage.sourceItem != null) {
             if (damage.sourceItem.getType() == Material.AIR) {
                 weapon = this.translator.itemDefaults.get(attacker.getType());
             } else {
                 weapon = this.translator.formatItem(damage.sourceItem);
             }
         }
 
         if (weapon == null || this.translator.itemFormat == null) return attackerName;
 
         return String.format(this.translator.itemFormat, attackerName, weapon);
     }
 
 }
