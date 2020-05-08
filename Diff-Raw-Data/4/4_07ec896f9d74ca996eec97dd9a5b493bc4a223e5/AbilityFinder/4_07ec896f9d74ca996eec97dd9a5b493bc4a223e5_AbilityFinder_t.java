 package entities.players.abilities;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Modifier;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Utility method for getting all player abilities on player instantiation.
  * @author gds12
  */
 public final class AbilityFinder {
 	private AbilityFinder(){} //should not be instantiated.
 	
 	private static final int abilitylen = "Ability".length();
 	private static final int classlen = ".class".length();
 	private static final Constructor<?> defaultCon; 
 	
 	/**
 	 * Initialises the default constructor with that of PlayerAbility
 	 */
 	static{
 		Constructor<?> con = null;
 		try {
 			con = PlayerAbility.class.getConstructor();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		}
 		defaultCon = con;
 	}
 	
 	public static List<Class<?>> loadClasses(String packageName)
 			throws ClassNotFoundException, IOException{
 		final List<Class<?>> retclasses = new LinkedList<Class<?>>();
 		final List<Class<?>> classes = getClasses(packageName);
 		for(Class<?> c : classes){
 			try{ 
 				c.getDeclaredConstructor(defaultCon.getParameterTypes());
 			}catch(NoSuchMethodException e){ 
 				continue; 
 			}
 			if(c.isInterface() || Modifier.isAbstract(c.getModifiers())){
 				continue;
 			}
 			retclasses.add(c);
 		}
 		return retclasses;
 	}
 	
 	private static List<Class<?>> getClasses(String packageName) 
 			throws ClassNotFoundException, IOException {
 		ClassLoader cLoader = Thread.currentThread().getContextClassLoader();
 		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
 		if(cLoader != null){
 			String path = packageName.replace('.', '/');
 			Enumeration<URL> resources = cLoader.getResources(path);
 			List<File> dirs = new ArrayList<File>();
 			while (resources.hasMoreElements()) {
 				URL resource = resources.nextElement();
				dirs.add(new File(resource.getFile().replaceAll("%20", " ")));
 			}
 			for (File directory : dirs) {
 				classes.addAll(findClasses(directory, packageName));
 			}
 		}
 		return classes;
 	}
 
 	private static List<Class<?>> findClasses(File dir, String packageName)
 			throws ClassNotFoundException {
		System.out.println("Trying package:"+packageName+", dir:"+dir.getPath());
 		List<Class<?>> classes = new ArrayList<Class<?>>();
 		if (dir.exists()) {
 			File[] files = dir.listFiles();
 			for (File file : files) {
 				if (file.isDirectory()) {
 					classes.addAll(findClasses(file, 
 							packageName + "." + file.getName()));
 				} else if(file.isFile() && file.getName().endsWith(".class")){
 					classes.add(Class.forName(packageName + '.' + 
 							file.getName().substring(0, file.getName().length()
 									- classlen)));
 				}
 			}
 		}
 		return classes;
 	}
 
 	public static Map<String, IPlayerAbility> initialiseAbilities() {
 		Map<String,IPlayerAbility> ret = new HashMap<String, IPlayerAbility>();
 		List<Class<?>> classes;
 		try {
 			classes = loadClasses("entities.players.abilities");
 			for(Class<?> c : classes){
 				String classname = c.getSimpleName();
 				String key = classname.substring(0,classname.length() - abilitylen).toLowerCase();
 				Object inst;
 				try {
 					inst = c.getConstructor(defaultCon.getParameterTypes()).newInstance();
 					if(inst instanceof IPlayerAbility){
 						ret.put(key, (IPlayerAbility) inst);
 					}
 				} catch (IllegalArgumentException e) {
 					e.printStackTrace();
 				} catch (SecurityException e) {
 					e.printStackTrace();
 				} catch (InstantiationException e) {
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					e.printStackTrace();
 				} catch (InvocationTargetException e) {
 					e.printStackTrace();
 				} catch (NoSuchMethodException e) {
 					continue;
 				}
 			}
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 }
