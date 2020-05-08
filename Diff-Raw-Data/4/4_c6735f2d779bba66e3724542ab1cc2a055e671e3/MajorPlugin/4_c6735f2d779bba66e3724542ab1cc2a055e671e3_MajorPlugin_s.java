 package plug.majorplugin;
 
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import plug.Plugin;
 import plug.minorplugin.MinorPlugin;
 
 
 /*********** TODO ***************/
 // List of minor plugin for one major plugin. Send it to manager plugin.
 
 /**
  * This abstract class is a representation of a Major Plugin.
  * @author mael
  *
  */
 public abstract class MajorPlugin extends Plugin {
 
 	protected List<MinorPlugin> minorPlugins;
 
 	/**
 	 * Constructor of MajorPlugin
 	 * @param String path - The path to the configuration file of the MajorPlugin.
 	 */
 	public MajorPlugin(String path){
 		setConfigurationFilePath(path);
 	}
 
 	/**
 	 * Load all the needed plugins for the MajorPlugin.
 	 */
 	public void loadMinorPlugins(){
 
 		minorPlugins = new ArrayList<MinorPlugin>();
 		String[] minorPluginsName = minorPluginsNeeded();
 		if(minorPluginsName.length > 0)
 			for(String s : minorPluginsName){
 				loadMinorPlugin(s);
 			}
 	}
 	
 	
 
 	/**
 	 * Load a plugin and store it into the minorsPlugin attribute.
 	 * @param String name - the name of the plugin to load. (Contains the packages name like "plug.majorplugin")
 	 */
 	private void loadMinorPlugin(String name){
 		URL[] urls = new URL[1];
 
 		String[] nameSplit = name.split("\\.");
 		String packageName = "";
 		for(int i=0;i<nameSplit.length - 1 ;i++){
 			packageName += nameSplit[i] + "/";
 		}
 
 		try {
 			urls[0] = new URL("file:"+ System.getProperty("user.dir") + "/bin/plug/minorplugin/" + packageName);
 			//System.out.println(urls[0]);
 			//System.out.println(name);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} 
 
 		URLClassLoader ucl = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
 
 		Class<?> ext;
 		try {
 			ext = Class.forName(name, false, ucl);
 			minorPlugins.add((MinorPlugin) ext.newInstance());
 
 		} catch (ClassNotFoundException e){
 			e.printStackTrace();
 		}
 		catch (InstantiationException e){
 			e.printStackTrace();
 		}
 		catch ( IllegalAccessException e){
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Parse the configuration file to return the list of all the needed plugin.
 	 * @return String[] res - All the minor plugins needed. 
 	 */
 	public String[] minorPluginsNeeded(){
 
 		Properties prop = new Properties();
 
 		try {
 			prop.load(new FileReader(getConfigurationFilePath()));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return prop.getProperty("MinorPlugins").split(";");
 	}
 
 
 
 
 }
