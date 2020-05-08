 package manager;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Proxy;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import plug.majorplugin.LazyMajorPlugin;
 import plug.majorplugin.LazyMajorPluginHandler;
 import utilities.InitFileInterpretor;
 import utilities.NameManager;
 import core.Plateform;
 
 /* TODO :
  * Méthode : retourne l'instance d'une classe si elle existe dans le set
  * Au chargement d'un plugin. Vérifier si une instance n'est pas déjà crée
  * LAZYMINOR A implémenter
  * 
  * 
  */
 
 /**
  * This class provides an implementation for PluginManager Class
  * @author Florian
  */
 public final class PluginManager {
 
 	private static PluginManager instance = null;
 	//private String launch = "";
 	private Map<Object, Set<Object>> pluginStore;
 
 	private String majorPluginPath = System.getProperty("user.dir") + "/bin/plug/majorplugin/";
 	private String minorPluginPath = System.getProperty("user.dir") + "/bin/plug/minorplugin/";
 	/**
 	 * PluginManager constructor 
 	 */
 	private PluginManager() {
 		super();
 		pluginStore = new HashMap<Object, Set<Object>>();
 	}
 
 	/**
 	 * Method which returns an instance of PluginManager's class
 	 * @return PluginManager's instance
 	 */
 	public final static PluginManager getInstance() {
 
 		if (PluginManager.instance == null) {
 
 			//Avoid multiple instantiation even with different threads
 			synchronized(PluginManager.class) {
 				if (PluginManager.instance == null) {
 					PluginManager.instance = new PluginManager();
 				}
 			}
 		}
 		return PluginManager.instance;
 	}
 
 	/**
 	 * The method loads all major plugins in the ManagerPlugin
 	 * @param listPlugin The list of all major plugins
 	 */
 	public void loadMajorPlugins(List <String> listPlugin, Plateform core){
 		System.out.println("[LOADING] Loading all plugins in configuration file");
 
 		//Load all major plugin in config file
 		for (String s : listPlugin){
 			loadMajorPlugin(s, core);
 		}
 		System.out.println("[LOADING] Loading of all major plugins succeed \n");
 		System.out.println(this.getAvailablePlugins());
 	}
 
 	/**
 	 * This procedure allow the loading of all minor plugin which are contained in a list
 	 * @param minorPluginList
 	 */
 	public void loadMinorPluginList(List<String> minorPluginList, Plateform core){
 		for(String minorPlugin : minorPluginList){
 			loadMinorPlugin(minorPlugin, core);
 		}
 	}
 
 	/**
 	 * This procedure allow the loading of a minor plugin
 	 * @param minorPlugin
 	 */
 	public void loadMinorPlugin(String minorPluginName, Plateform core){
 		for (Object mapKey : pluginStore.keySet()) {
 
 			if(isMinorPluginOf(mapKey, minorPluginName)){		
 				System.out.println("[LOADING] Loading minor plugin : " + minorPluginName);
 				URL[] urls = new URL[1];
 
 				String packageName = NameManager.getPackageName(minorPluginName);
 
				System.out.println(minorPluginName);
				System.out.println(packageName);
 				try {
 					urls[0] = new URL("file:"+ minorPluginPath + packageName);
 					//System.out.println(urls[0]);
 				} catch (MalformedURLException e) {
 					e.printStackTrace();
 				} 
 
 				URLClassLoader ucl = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
 				Class<?> minorPluginClass;
 
 				try {
 					minorPluginClass = Class.forName(minorPluginName, false, ucl);
 					Object instanceOfObject = minorPluginClass.newInstance();
 
 					String[] nameSplit = minorPluginClass.getName().split("\\.");
 					String packageNameTmp = "";
 
 					packageNameTmp = nameSplit[nameSplit.length-2];
					System.out.println(minorPluginPath+ packageNameTmp.toString());
 					String[] conf = InitFileInterpretor.getProperties(minorPluginPath + packageNameTmp +"/" + packageNameTmp +".ini", "Config");
 
 					//We check if the plugin is lazy first
 					if(conf!=null && InitFileInterpretor.contains(conf, "lazy")){
 						//instanceOfObject = new LazyMajorPlugin(core, minorPluginClass.getName());
 					}
 					else{
						instanceOfObject = minorPluginClass.newInstance();
 					}		
 
 					if(conf!=null && InitFileInterpretor.contains(conf, "singleton")){
						System.out.println(minorPluginName + " est un singleton ! ");
 						//If the plugin has been already charged, we do a copy of this
 						if(hasBeenAlreadyCharged(minorPluginClass.toString())){
 							Object pluginCopy = instanceOfObject;
 							pluginStore.get(mapKey).add(pluginCopy);
 						}
 						else{
 							pluginStore.get(mapKey).add(instanceOfObject);
 						}
 					}
 					else{
						System.out.println(minorPluginName + " n'est pas un singleton ! ");
 						pluginStore.get(mapKey).add(instanceOfObject);
 					}	
 
 					System.out.println("[LOADING] Loading of " + minorPluginName + " succeed \n");
 				} catch (ClassNotFoundException e){
 					e.printStackTrace();
 				}
 				catch (InstantiationException e){
 					e.printStackTrace();
 				}
 				catch ( IllegalAccessException e){
 					e.printStackTrace();
 				} catch (IllegalArgumentException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (SecurityException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} 
 			}
 		}
 	}
 
 	/**
 	 * This method loads a plugin in the ManagerPlugin
 	 * @param plugin the plugin to load
 	 * @throws ClassNotFoundException 
 	 * @throws IllegalAccessException 
 	 * @throws InstantiationException 
 	 */
 	private void loadMajorPlugin(String name, Plateform core){
 
 		//Split the string to have the name and the name of the package
 		String packageName = NameManager.getPackageName(name);
 
 		System.out.println("[LOADING] Loading " + name);
 		URL[] urls = new URL[1];
 		try {
 			urls[0] = new URL("file:" + majorPluginPath + packageName);
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		URLClassLoader ucl = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
 
 		Class<?> pluginClass;
 		try {
 			pluginClass = Class.forName("plug.majorplugin."+name , false, ucl);
 
 			Object instanceOfObject = null;
 			Set<Object> set = new HashSet<Object>();
 
 			String[] conf = InitFileInterpretor.getProperties(majorPluginPath + packageName + packageName.replace("/", "")+".ini", "Config");			
 
 			//We check if the plugin is lazy first
 			if(conf!=null && InitFileInterpretor.contains(conf, "lazy")){
 				instanceOfObject = new LazyMajorPlugin(core, pluginClass.getName());
 				//pluginStore.put(instanceOfObject, set);
 			}
 			else{
 				instanceOfObject = pluginClass.getDeclaredConstructor(Plateform.class).newInstance(core);
 			}		
 
 			if(conf!=null && InitFileInterpretor.contains(conf, "singleton")){
 				System.out.println(name + " est un singleton ! ");
 				//If the plugin has been already charged, we do a copy of this
 				if(hasBeenAlreadyCharged(pluginClass.toString())){
 					Object pluginCopy = instanceOfObject;
 					pluginStore.put(pluginCopy, set);	
 				}
 				else{
 					pluginStore.put(instanceOfObject, set);
 				}
 			}
 			else{
 				System.out.println(name + " n'est pas un singleton ! ");
 				pluginStore.put(instanceOfObject, set);
 			}
 
 			System.out.println("[LOADING] Loading of " + name + " succeed \n");
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}catch (ClassNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 
 	}
 
 	/**
 	 * This method unloads a plugin in the ManagerPlugin
 	 * @param plugin
 	 */
 	public void unloadPlugin(String pluginName) {
 	}
 
 	/**
 	 * Return true if pPlugin is a minorPlugin of mPlugin, false otherwise
 	 * @param mPlugin The major plugin
 	 * @param pPlugin The plugin to determine
 	 * @return A boolean which determines if pPlugin is a minorPlugin of mPlugin
 	 */
 	private boolean isMinorPluginOf(Object mPlugin, String pPlugin){
 		boolean isMinorPlugin = false;
 		try {
 
 			String[] nameSplit = mPlugin.getClass().getName().split("\\.");
 			String packageName = "";
 
 			if(nameSplit[nameSplit.length-1].equals("LazyMajorPlugin")){
 				nameSplit = ((LazyMajorPlugin)mPlugin).getClassName().split("\\.");
 			}
 
 			packageName = nameSplit[nameSplit.length-2];
 
 			String[] tmp = InitFileInterpretor.getProperties(majorPluginPath + packageName +"/" + packageName +".ini", "MinorPlugins");
 
 			if(tmp!=null){
 				List<String> tmpList = new ArrayList<String>(Arrays.asList(tmp));
 				if(tmpList.contains(pPlugin)) isMinorPlugin = true;
 			}
 
 		} catch (SecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}	
 		return isMinorPlugin;
 	}
 
 	/**
 	 * Load all minor plugin needed by the major.
 	 * @param major the major plugin wich we have to load the minors
 	 */
 	public void loadMinorsFromMajor(Object major, Plateform core){
 		String[] nameSplit = major.getClass().getName().split("\\.");
 		String packageName = nameSplit[nameSplit.length-2];
 		
 		String[] minors = InitFileInterpretor.getProperties(majorPluginPath + packageName +"/" + packageName +".ini", "MinorPlugins");
 	
 		for(String s : minors){
 			if(!s.isEmpty()) PluginManager.getInstance().loadMinorPlugin(s, core);
 		}
 	}
 
 	/**
 	 * Display the list of available minor plugins for all major plugins
 	 * @param majorPluginSet The set which contains our MajorPlugins
 	 * @return A string which contains the list of all minor plugin available
 	 */
 	public String getAvailablePlugins(){
 		String res = "[INFO] Plugins available : \n ";
 
 		for (Object mapKey : pluginStore.keySet()) {
 			String[] nameSplit = mapKey.getClass().getName().split("\\.");
 			String packageName = "";
 
 			if(nameSplit[nameSplit.length-1].equals("LazyMajorPlugin")){
 				nameSplit = ((LazyMajorPlugin)mapKey).getClassName().split("\\.");
 			}
 
 			packageName = nameSplit[nameSplit.length-2];
 
 			try {
 				String[] test = InitFileInterpretor.getProperties(majorPluginPath + packageName +"/" + packageName +".ini", "MinorPlugins");
 
 				if(test!=null){
					for(String string : test){
						res += "\t - " + string + "\n" ;
 					}
 				}
 
 			} catch (SecurityException e) {
 				e.printStackTrace();
 			} catch (IllegalArgumentException e) {
 				e.printStackTrace();
 			}
 		}
 		return res;
 	}
 
 	/**
 	 * PluginStore getter
 	 * @return the pluginStore
 	 */
 	public Map<Object, Set<Object>> getPluginStore() {
 		return pluginStore;
 	}
 
 	/**
 	 * PluginStore setter
 	 * @param pluginStore the pluginStore to set
 	 */
 	public void setPluginStore(Map<Object, Set<Object>> pluginStore) {
 		this.pluginStore = pluginStore;
 	}
 
 	public String displayPluginStore(){
 		String res = "[DISPLAY] Display information about plugin Store \n";
 		for (Object mapKey : pluginStore.keySet()) {
 			res += " [ " + mapKey.getClass().getName().toString() + " ,  {" ;
 			for (Object value : pluginStore.get(mapKey)){
 				res += value.getClass().getName().toString() + " , ";
 			}
 			res += "}" + " ] \n";
 		}
 		return res;
 	}
 
 	private boolean hasBeenAlreadyCharged(String className){
 		boolean result = false;
 		for (Set<Object> setObject : pluginStore.values()) {
 			for (Object object : setObject) {
 				if (object.getClass().getName().toString().equals(className)) result = true;
 			}
 		}
 		return result;
 	}
 
 	private static Object getMajorPluginProxy(Object instanceOfObject){
 		InvocationHandler h = new LazyMajorPluginHandler(instanceOfObject);
 		return Proxy.newProxyInstance(instanceOfObject.getClass().getClassLoader(), instanceOfObject.getClass().getInterfaces(), h);
 	}
 
 }
