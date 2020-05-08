 package tk.manf.serialisation;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import tk.manf.serialisation.handler.SerialisationHandler;
 import tk.manf.serialisation.handler.flatfile.YAMLSerialisationHandler;
 
 /**
  * Main Plugin
  * <p/>
  * @author Björn 'manf' Heinrichs
  */
 public class SerialisationAPIPlugin extends JavaPlugin {
     /**
      * {@inheritDoc}
      * <p/>
      * <b>This Plugin initialises all Serialisation Handler for their SerialisationType</b>
      * <p/>
      * @see SerialisationType
      * @see SerialisationHandler
      */
     @Override
     public void onEnable() {
         SerialisationType.FLATFILE_YAML.setSerialisationHandler(new YAMLSerialisationHandler(3));
        getLogger().info("Loaded Serialisation Handler");
     }
 }
