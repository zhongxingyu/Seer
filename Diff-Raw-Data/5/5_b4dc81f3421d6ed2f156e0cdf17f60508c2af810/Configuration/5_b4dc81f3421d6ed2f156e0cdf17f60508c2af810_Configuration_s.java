 package ltguide.base.configuration;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 
 import ltguide.base.Base;
 import ltguide.base.Debug;
 import ltguide.base.utils.DirUtils;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.yaml.snakeyaml.error.YAMLException;
 
 public class Configuration extends YamlConfiguration {
 	private final File file;
 	protected final Base plugin;
 	protected int[] oldVersion;
 	
 	public Configuration(final Base instance) {
 		this(instance, "config.yml");
 	}
 	
 	public Configuration(final Base instance, final String name) {
 		plugin = instance;
 		file = new File(plugin.getDataFolder(), name);
 	}
 	
 	public void reload() {
 		load();
 	}
 	
 	protected void load() {
 		boolean loaded = false;
 		try {
 			load(file);
 			loaded = true;
 		}
 		catch (final FileNotFoundException e) {}
 		catch (final IOException e) {
 			plugin.logException(e, "cannot load " + file);
 		}
 		catch (final InvalidConfigurationException e) {
			if (e.getCause() instanceof YAMLException) plugin.severe("Config file " + file + " isn't valid! " + e.getCause());
 			else if (e.getCause() == null || e.getCause() instanceof ClassCastException) plugin.severe("Config file " + file + " isn't valid!");
 			else plugin.logException(e, "cannot load " + file + ": " + e.getCause().getClass());
 			
 			backup("invalid");
 		}
 		
 		final InputStream inStream = plugin.getResource(file.getName());
 		if (inStream != null) {
 			setDefaults(YamlConfiguration.loadConfiguration(inStream));
 			
 			if (!loaded) plugin.info("Writing default configuration to " + file);
 		}
 		
 		if (!loaded) {
 			options().copyDefaults(true);
 			save();
 		}
 		else upgrade();
 	}
 	
 	public void backup(final String prefix) {
 		try {
 			final File dest = new File(file.getParentFile(), prefix + "-" + file.getName());
 			plugin.info("Copying config file to " + dest);
 			DirUtils.copyFile(file, dest);
 		}
 		catch (final Exception e) {
 			plugin.logException(e, "failed to copy config");
 		}
 	}
 	
 	private void upgrade() {
 		if (Debug.ON) Debug.info("upgrade() " + file);
 		
 		final String current = plugin.getDescription().getVersion();
 		
 		String old = "UNKNOWN";
 		if (isSet("version-nomodify")) old = getString("version-nomodify");
 		
 		if (current.equals(old)) return;
 		
 		oldVersion = getVersionInt(old);
 		set("version-nomodify", current);
 		
 		plugin.warning("Migrating " + file + " from version " + old);
 		backup(old);
 		migrate();
 		save();
 	}
 	
 	protected void migrate() {
 		if (Debug.ON) Debug.info("Configuration migrate()");
 	}
 	
 	public void save() {
 		try {
 			save(file);
 		}
 		catch (final IOException e) {
 			plugin.logException(e, "could not save " + file);
 		}
 	}
 	
 	protected int[] getVersionInt(final String version) {
 		final String[] split = version.split("\\.");
 		final int[] num = new int[split.length];
 		
 		for (int i = 0; i < split.length; i++)
 			try {
 				num[i] = Integer.parseInt(split[i]);
 			}
 			catch (final NumberFormatException e) {
 				num[i] = 0;
 			}
 		
 		return num;
 	}
 	
 	protected boolean versionCompare(final int... compare) {
 		for (int i = 0; i < compare.length && i < oldVersion.length; i++)
 			if (oldVersion[i] > compare[i]) return false;
 		
 		return true;
 	}
 	
 	protected void fixIntRange(final ConfigurationSection cs, final String key, final int min, final int max) {
 		final int value = cs.getInt(key);
 		if (value < min || value > max) {
 			cs.set(key, getDefaultSection().getInt(cs.getCurrentPath() + "." + key));
 			plugin.configWarning(cs, key, value + "; valid: " + min + "-" + max);
 		}
 	}
 	
 	protected void fixBoolean(final ConfigurationSection cs, final String key) {
 		if (!cs.isBoolean(key)) {
 			cs.set(key, getDefaultSection().getBoolean(cs.getCurrentPath() + "." + key));
 			plugin.configWarning(cs, key, cs.get(key) + "; valid: true/false");
 		}
 	}
 }
