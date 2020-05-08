 package mindless728.BlockStorage;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.io.Serializable;
 
import java.util.Map;
 import java.util.HashMap;
 
 public class BlockStorage extends JavaPlugin {
 	HashMap<JavaPlugin,PluginBlockStorage<Serializable>> pluginStorage;
 	
 	public BlockStorage() {
 		pluginStorage = new HashMap<JavaPlugin,PluginBlockStorage<Serializable>>();
 		System.out.println("BlockStorage version 1.0 installed");
 	}
 	
 	public void onEnable() {
 	}
 	
 	public void onDisable() {
		for(Map.Entry<JavaPlugin,PluginBlockStorage<Serializable>> entry : pluginStorage.entrySet())
			entry.getValue().close();
		pluginStorage.clear();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public <T extends Serializable> PluginBlockStorage<T> getPluginBlockStorage(JavaPlugin plugin) {
 		PluginBlockStorage<T> ret = null;
 		
 		if(pluginStorage.containsKey(plugin))
 			ret = (PluginBlockStorage<T>)pluginStorage.get(plugin);
 		else {
 			ret = new PluginBlockStorage<T>(plugin);
 			pluginStorage.put(plugin,(PluginBlockStorage<Serializable>)ret);
 		}
 		
 		return ret;	
 	}
 }
