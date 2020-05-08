 package name.richardson.james.bukkit.utilities.updater;
 
 import org.bukkit.plugin.Plugin;
 
 public abstract class AbstractPluginUpdater implements PluginUpdater {
 
 	public enum Branch {
 		DEVELOPMENT, STABLE
 	}
 
 	public enum State {
 		UPDATE, NOTIFY, OFF
 	}
 
 	private final String version;
 	private final State state;
 
 	public AbstractPluginUpdater(Plugin plugin, State state) {
 		this.version = plugin.getName();
 		this.state = state;
 	}
 
 	@Override
 	public String getLocalVersion() {
 		return this.version;
 	}
 
 
 	@Override
 	public State getState() {
		return this.state
 	}
 
 }
