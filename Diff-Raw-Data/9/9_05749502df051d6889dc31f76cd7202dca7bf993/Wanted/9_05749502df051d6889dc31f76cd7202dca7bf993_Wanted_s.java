 import java.util.*;
 import java.util.logging.*;
 
 public class Wanted extends Plugin {
 	
 	List<PluginRegisteredListener> pluginListeners = new ArrayList<PluginRegisteredListener>();
 	List<PluginLoader.Hook> requiredHooks = Arrays.asList(PluginLoader.Hook.COMMAND, PluginLoader.Hook.SERVERCOMMAND, PluginLoader.Hook.DAMAGE, PluginLoader.Hook.LOGIN);
 	Logger pluginLogger = Logger.getLogger("Minecraft");
	iListener pluginListener = new Listener();
 
 	public void disable() {
 		for (PluginRegisteredListener listener : pluginListeners) {
 			etc.getLoader().removeListener(listener);
 		}
 		pluginLogger.info("Wanted! v0.1 has been disabled!");
 	}
 
 	public void enable() {
 		pluginListener.loadSettings();
 		for (PluginLoader.Hook hook : requiredHooks) {
 			pluginListeners.add(etc.getLoader().addListener(hook, pluginListener, this, PluginListener.Priority.MEDIUM));
 		}
 		pluginLogger.info("Wanted! v0.1 has been initialized!");
 	}
 
 }
