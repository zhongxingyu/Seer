 package spaceshooters.api.plugins.loading;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Properties;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import org.objectweb.asm.ClassReader;
 
 import spaceshooters.api.plugins.Plugin;
 import spaceshooters.api.plugins.PluginMetadata;
 import spaceshooters.main.Spaceshooters;
 import spaceshooters.util.Logger;
 
 /**
  * This class is responsible for loading all plugins during startup.
  * Plugin loading is performed in a few steps:
  * 1) Look for plugins in the /plugins directory.
  * 2) Add files that were found in the previous step to the classpath.
  * 3) Look for the plugins in the files.
  * 4) Construct plugins.
  * 5) Initialize plugins.
  * 
  * @author Mat
  * 
  */
 public final class PluginLoader {
 	
 	private static boolean added = false;
 	public static final File PLUGIN_LIST_FILE = new File(Spaceshooters.getPluginsPath() + "list.cfg");
 	private static final Properties props = new Properties();
 	private static final ArrayList<Plugin> plugins = new ArrayList<>();
 	
 	static {
 		try {
 			if (!PLUGIN_LIST_FILE.exists()) {
 				PLUGIN_LIST_FILE.getParentFile().mkdirs();
 				PLUGIN_LIST_FILE.createNewFile();
 			}
 			props.load(new FileInputStream(PLUGIN_LIST_FILE));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private PluginLoader() {
 	}
 	
 	/**
 	 * Looks for all plugins during startup and adds them to classpath.
 	 */
 	public static void findPlugins() {
 		if (!added) {
 			File dir = new File(Spaceshooters.getPluginsPath());
 			dir.mkdirs();
 			File[] allFiles = dir.listFiles();
 			Arrays.sort(allFiles);
 			
 			for (File file : allFiles) {
 				if (((file.isFile()) && ((file.getName().endsWith(".jar") || (file.getName().endsWith(".zip")))))) {
 					PluginClassLoader.addFile(file); // Add to classpath.
 					loadPlugin(file);
 				}
 			}
 			
 			added = true;
 		} else {
 			Logger.error("[Plugin Loader] Plugins already initialized! Why are we trying to do it again, huh?");
 		}
 	}
 	
 	/**
 	 * Initializes the plugin from the given file. The file MUST be in classpath.
 	 * 
 	 * @param file
 	 *            The file in which the plugin resides.
 	 */
 	private static void loadPlugin(File file) {
 		List<PluginMetadata.Annotation> metadatas = getPluginsMetadata(file); // Grab plugin's metadata, which contains info that's needed for constructing plugin.
 		for (PluginMetadata.Annotation metadata : metadatas) {
 			try {
 				Class<?> clazz = metadata.getPluginClass();
 				Logger.info("[Plugin Loader] Constructing plugin \"" + metadata.getName() + "\"!");
 				Constructor<?> pluginDefault = clazz.getDeclaredConstructor(PluginMetadata.Annotation.class, File.class);
 				pluginDefault.setAccessible(true);
 				Plugin p = (Plugin) pluginDefault.newInstance(metadata, file);
 				PluginLoader.getPlugins().add(p);
 				Logger.info("[Plugin Loader] Finished constructing plugin \"" + metadata.getName() + "\"!");
 				String state = (String) props.get(metadata.getId());
 				state = (state == null ? "1" : state);
 				if (p.getMetadata().enableOnLoad() && state.equals("1") ? true : false) {
 					p.enable();
 				}
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
 				Logger.error("[Plugin Loader] Failed to construct plugin \"" + metadata.getName() + "\"!");
 				continue;
 			}
 		}
 	}
 	
 	private static List<PluginMetadata.Annotation> getPluginsMetadata(File file) {
 		try (ZipFile zip = new ZipFile(file)) {
 			PluginDiscoverer disc = new PluginDiscoverer();
 			for (ZipEntry entry : Collections.list(zip.entries())) {
 				if (entry.getName().endsWith(".class")) {
 					new ClassReader(zip.getInputStream(entry)).accept(disc, 0);
 				}
 			}
 			return disc.getMetadata();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns all active plugins.
 	 * 
 	 * @return All active plugins.
 	 */
 	public static List<Plugin> getActivePlugins() {
 		CopyOnWriteArrayList<Plugin> active = new CopyOnWriteArrayList<>();
 		for (Plugin p : plugins) {
 			if (p.isEnabled()) {
 				active.add(p);
 			}
 		}
 		return Collections.unmodifiableList(active);
 	}
 	
 	/**
 	 * Returns all plugins which are loaded with this session of the game.
 	 * 
 	 * @return All plugins.
 	 */
 	public static ArrayList<Plugin> getPlugins() {
 		return plugins;
 	}
 	
 	public static Properties getPluginStates() {
 		return props;
 	}
 	
 	public static void savePluginStates() {
 		try {
 			props.store(new FileOutputStream(PLUGIN_LIST_FILE), null);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
