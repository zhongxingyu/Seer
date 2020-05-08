 package in.nikitapek.pumpkinvirus;
 
 import in.nikitapek.pumpkinvirus.commands.CommandPumpkinVirus;
 import in.nikitapek.pumpkinvirus.events.PumpkinVirusListener;
 import in.nikitapek.pumpkinvirus.util.PumpkinVirusConfigurationContext;
 
 import org.bukkit.Bukkit;
 
 import com.amshulman.mbapi.MbapiPlugin;
 
 public final class PumpkinVirusPlugin extends MbapiPlugin {
    private final PumpkinVirusConfigurationContext configurationContext = new PumpkinVirusConfigurationContext(this);

     @Override
     public void onEnable() {
         registerEventHandler(new PumpkinVirusListener(configurationContext));
         registerCommandExecutor(new CommandPumpkinVirus(configurationContext));
 
         super.onEnable();
     }
 
     @Override
     public void onDisable() {
         // Cancels any tasks scheduled by this plugin.
         Bukkit.getScheduler().cancelTasks(this);
     }
 }
