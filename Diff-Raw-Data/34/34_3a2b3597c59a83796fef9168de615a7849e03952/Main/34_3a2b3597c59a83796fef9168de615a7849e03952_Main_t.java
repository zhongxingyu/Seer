 package edgruberman.bukkit.guillotine;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.EntityType;
 import org.bukkit.event.HandlerList;
 
 import edgruberman.bukkit.guillotine.commands.Reload;
 import edgruberman.bukkit.guillotine.messaging.ConfigurationCourier;
 import edgruberman.bukkit.guillotine.util.CustomPlugin;
 
 public final class Main extends CustomPlugin {
 
     private static final String KEY_DEFAULT = "(default)";
 
     public static ConfigurationCourier courier;
 
     @Override
    public void onLoad() { this.putConfigMinimum("1.1.1"); }
 
     @Override
     public void onEnable() {
         this.reloadConfig();
         Main.courier = ConfigurationCourier.Factory.create(this).build();
 
         final Executioner executioner = new Executioner(this);
         final ConfigurationSection heads = this.getConfig().getConfigurationSection("heads");
         for (final String keyVictim : heads.getKeys(false)) {
             final EntityType victim = ( !keyVictim.equals(Main.KEY_DEFAULT) ? this.parseEntityType(keyVictim) : null );
             if (victim == null && !keyVictim.equals(Main.KEY_DEFAULT)) continue;
 
             final Map<EntityType, Double> rates = new HashMap<EntityType, Double>();
             final ConfigurationSection killers = heads.getConfigurationSection(keyVictim);
             for (final String keyKiller : killers.getKeys(false)) {
                 final EntityType killer = ( !keyKiller.equals(Main.KEY_DEFAULT) ? this.parseEntityType(keyKiller) : null );
                 if (killer == null && !keyKiller.equals(Main.KEY_DEFAULT)) continue;
                 rates.put(killer, killers.getDouble(keyKiller) / 100D);
             }
 
             executioner.putInstruction(victim, rates);
             for (final Map.Entry<EntityType, Double> rate : rates.entrySet())
                 this.getLogger().log(Level.CONFIG, "Executioner instruction; victim: {0}, killer: {1}, rate: {2,number,#.#%}"
                         , new Object[] {
                             ( victim != null ? victim.name() : Main.KEY_DEFAULT )
                             , ( rate.getKey() != null ? rate.getKey().name() : Main.KEY_DEFAULT )
                             , rate.getValue()
                         }
                 );
         }
 
         Bukkit.getPluginManager().registerEvents(executioner, this);
 
         this.getCommand("guillotine:reload").setExecutor(new Reload(this));
     }
 
     @Override
     public void onDisable() {
         Main.courier = null;
         HandlerList.unregisterAll(this);
 
     }
 
     private EntityType parseEntityType(final String name) {
         try {
             return EntityType.valueOf(name);
         } catch (final IllegalArgumentException e) {
             this.getLogger().warning("Unrecognized EntityType: " + name + "; " + e);
             return null;
         }
     }
 
 }
