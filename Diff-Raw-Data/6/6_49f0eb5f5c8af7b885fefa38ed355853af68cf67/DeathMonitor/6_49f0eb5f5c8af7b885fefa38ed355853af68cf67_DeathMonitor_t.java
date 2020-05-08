 package edgruberman.bukkit.simpledeathnotices;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.AnimalTamer;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Tameable;
 import org.bukkit.entity.Vehicle;
 import org.bukkit.event.Event;
 import org.bukkit.event.entity.EntityDamageByBlockEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.plugin.Plugin;
 
 import edgruberman.bukkit.messagemanager.MessageLevel;
 
 final class DeathMonitor extends org.bukkit.event.entity.EntityListener{
     
     static final String DEFAULT_FORMAT = "%1$s died."; // 1 = Victim, 2 = Killer
     static final String DEFAULT_WEAPON_FORMAT = "%1$s with %2$s"; // 1 = Killer, 2 = Weapon
     static final String DEFAULT_HAND = "a bare fist";
     
     static Map<DamageCause, String> causeFormats = new HashMap<DamageCause, String>();
     static Map<String, String> entityNames = new HashMap<String, String>();
     static Map<String, String> ownerFormats = new HashMap<String, String>();
     static Map<Material, String> materialNames = new HashMap<Material, String>();
     static String weaponFormat = DeathMonitor.DEFAULT_WEAPON_FORMAT;
     static String hand = DeathMonitor.DEFAULT_HAND;
     
     private Map<Entity, EntityDamageEvent> lastDamage = new HashMap<Entity, EntityDamageEvent>();
     
     DeathMonitor (final Plugin plugin) {
         org.bukkit.plugin.PluginManager pluginManager = plugin.getServer().getPluginManager();
         pluginManager.registerEvent(Event.Type.ENTITY_DAMAGE, this, Event.Priority.Monitor, plugin);
         pluginManager.registerEvent(Event.Type.ENTITY_DEATH, this, Event.Priority.Monitor, plugin);
     }
     
     @Override
     public void onEntityDamage(final EntityDamageEvent event) {
         if (event.isCancelled() || !(event.getEntity() instanceof Player)) return;
         
         this.lastDamage.put(event.getEntity(), event);
     }
     
     @Override
     public void onEntityDeath (final EntityDeathEvent death) {
         if (!(death.getEntity() instanceof Player)) return;
         
         EntityDamageEvent damage = this.lastDamage.get(death.getEntity());
         String message = String.format(DeathMonitor.causeFormats.get(damage.getCause())
                 , ((Player) death.getEntity()).getDisplayName()
                 , DeathMonitor.describeDamager(damage)
         );
         Main.messageManager.broadcast(message, MessageLevel.EVENT);
     }
     
     private static String describeDamager(final EntityDamageEvent event) {
         if (event == null) return null;
         
         String description = null;
         
         if (event instanceof EntityDamageByEntityEvent) {
             // Entity description.
             Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
             description = DeathMonitor.describeEntity(damager);
             
         } else if (event instanceof EntityDamageByBlockEvent) {
             // Block material name.
             Block block = ((EntityDamageByBlockEvent) event).getDamager();
             if (block != null) {
                 description = DeathMonitor.materialNames.get(block.getType());
                 if (description == null) description = block.getType().toString().toLowerCase();
             }
             
        } else if (event.getCause() == DamageCause.SUFFOCATION) {
            // Suffocating material.
            description = DeathMonitor.materialNames.get(((Player) event.getEntity()).getEyeLocation().getBlock().getType());
            
         } else if (event.getCause() == DamageCause.FALL) {
             // Falling distance.
             description = Integer.toString(event.getDamage() + 3);
            
         }
         
         return description;
     }
     
     private static String describeEntity(final Entity entity) {
         String description = null;
         
         // For players, use their current display name.
         if (entity instanceof Player) {
             Player player = (Player) entity;
             if (DeathMonitor.weaponFormat != null) {
                 String weapon = DeathMonitor.materialNames.get(player.getItemInHand().getType());
                 if (weapon == null) weapon = DeathMonitor.hand;
                 description = String.format(DeathMonitor.weaponFormat, player.getDisplayName(), weapon);
             } else {
                 description = player.getDisplayName();
             }
             return description;
         }
         
         // For other entities, use their class name stripping Craft from the front.
         String[] entityClass = entity.getClass().getName().split("\\.");
         description = entityClass[entityClass.length - 1].substring("Craft".length());
         
         // Override with localization if specified in configuration.
         if (DeathMonitor.entityNames.containsKey(description))
             description = DeathMonitor.entityNames.get(description);
         
         // Include if entity has an owner of some type.
         
         if (entity instanceof Tameable) {
             AnimalTamer tamer = ((Tameable) entity).getOwner();
             if (tamer instanceof Entity && DeathMonitor.ownerFormats.containsKey("Tameable"))
                 description = String.format(DeathMonitor.ownerFormats.get("Tameable"), description, DeathMonitor.describeEntity((Entity) tamer));
         }
         
         if (entity instanceof Projectile) {
             LivingEntity shooter = ((Projectile) entity).getShooter();
             if (shooter instanceof Entity && DeathMonitor.ownerFormats.containsKey("Projectile"))
                 description = String.format(DeathMonitor.ownerFormats.get("Projectile"), description, DeathMonitor.describeEntity(shooter));
         }
         
         if (entity instanceof Vehicle) {
             Entity passenger = ((Vehicle) entity).getPassenger();
             if (passenger instanceof Entity && DeathMonitor.ownerFormats.containsKey("Vehicle"))
                 description = String.format(DeathMonitor.ownerFormats.get("Vehicle"), description, DeathMonitor.describeEntity(passenger));
         }
         
         return description;
     }
 }
