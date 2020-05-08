 package plugin;
 
 import java.io.File;
 import java.util.ArrayList;
 
 
 
 public class PluginManager {
 	
 	public static ArrayList<PluginFile> loadInstalledPlugins() {
		File path = new File("plugins");
 		File files[] = path.listFiles();
 		ArrayList<PluginFile> filePaths = new ArrayList<PluginFile>();
 		try {
 			ClassLoader loader = ClassLoader.getSystemClassLoader();
 			
 			for (File file : files) {
 				if (file.getName().contains(".jar")) {
 					String name = file.getName().replace(".jar", "");
					Plugin plugin = (Plugin) loader.loadClass("plugins." + name).newInstance();
 					PluginFile pFile = new PluginFile(name, file.getPath(), plugin);
 					filePaths.add(pFile);
 				}
 			}
 		}
 		catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		return filePaths;
 	}
 }
