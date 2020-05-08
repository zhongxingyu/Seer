 package edgruberman.bukkit.doorman;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.event.HandlerList;
 
 import edgruberman.bukkit.doorman.commands.DeclarationGet;
 import edgruberman.bukkit.doorman.commands.DeclarationSet;
 import edgruberman.bukkit.doorman.commands.Reload;
 import edgruberman.bukkit.doorman.messaging.ConfigurationCourier;
 import edgruberman.bukkit.doorman.util.CustomPlugin;
 
 public final class Main extends CustomPlugin {
 
     public static ConfigurationCourier courier;
 
     @Override
     public void onLoad() {
         this.putConfigMinimum(CustomPlugin.CONFIGURATION_FILE, "1.0.1");
         this.setPathSeparator('|');
     }
 
     @Override
     public void onEnable() {
         this.reloadConfig();
         Main.courier = ConfigurationCourier.Factory.create(this).setPath("language").setColorCode("color-code").build();
 
         final Map<String, String> switches = new HashMap<String, String>();
         final ConfigurationSection section = this.getConfig().getConfigurationSection("switches");
        if (section != null)
            for (final String name : section.getKeys(false))
                switches.put(name, section.getString(name));
 
         Long grace = this.getConfig().getLong("declaration-grace", -1);
         if (grace != -1) grace = TimeUnit.SECONDS.toMillis(grace);
 
         final RecordKeeper records = new RecordKeeper(this);
         final Doorman doorman = new Doorman(this, records, switches
                 , this.parseGreetingSwitches(switches, "greeting.headers"), this.parseGreetingSwitches(switches, "greeting.arguments"), grace);
         Bukkit.getPluginManager().registerEvents(doorman, this);
 
         final CommandExecutor declarationSet = new DeclarationSet(doorman, records);
         this.getCommand("doorman:declaration.set").setExecutor(declarationSet);
         this.getCommand("doorman:declaration.get").setExecutor(new DeclarationGet(doorman, records, declarationSet));
         this.getCommand("doorman:reload").setExecutor(new Reload(this));
     }
 
     private List<String> parseGreetingSwitches(final Map<String, String> switches, final String path) {
         final List<String> values = this.getConfig().getStringList(path);
         final Iterator<String> it = values.iterator();
         while (it.hasNext()) {
             final String name = it.next();
             if (switches.containsKey(name)) continue;
             this.getLogger().warning("Unrecognized switch specified in " + path + ": " + name);
             it.remove();
         }
         return values;
     }
 
     @Override
     public void onDisable() {
         Main.courier = null;
         HandlerList.unregisterAll(this);
     }
 
 }
