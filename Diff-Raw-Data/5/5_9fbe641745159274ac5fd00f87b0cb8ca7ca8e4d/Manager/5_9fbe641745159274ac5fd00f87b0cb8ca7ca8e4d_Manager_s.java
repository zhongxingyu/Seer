 package net.dandielo.stats.core;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.dandielo.api.stats.Listener;
 import net.dandielo.api.stats.Stat;
 import net.dandielo.api.stats.Updater;
 import net.dandielo.stats.core.response.ObjectResponse;
 
 public class Manager {	
 	//Manager instance
 	public static Manager instance = new Manager();
 	
 	/**
 	 * Listens to data request from incoming connections
 	 */
 	Map<String, List<StatMethod>> listeners = new HashMap<String, List<StatMethod>>();
 	
 	/**
 	 * Handles incoming data updates
 	 */
 	Map<String, List<StatMethod>> updaters = new HashMap<String, List<StatMethod>>();
 	
 	/**
 	 * A new manager instance
 	 */
 	private Manager()
 	{
 	}
 	
 	public int getListenerCount()
 	{
 		return listeners.size();
 	}
 	
 	private static Object __clazzInstance(Class<?> clazz)
 	{
 		try
 		{
 			return clazz.getConstructor().newInstance();
 		}
 		catch( Exception e ) { e.printStackTrace(); }
 		return null;
 	}
 	
 	/**
 	 * Registers a new listener for the given plugin
 	 * @param plugin
 	 * The plugin
 	 * @param clazz
 	 * that will be registered
 	 */
 	public static void registerListener(String plugin, Class<? extends Listener> clazz)
 	{
 		//load any clazz stat name
 		String clazzStat = "";
 		if ( clazz.isAnnotationPresent(Stat.class) )
 			clazzStat = clazz.getAnnotation(Stat.class).name() + "/";
 		
 		//new listener instance
 		Object inst = __clazzInstance(clazz);
 		
 		//create new list
 		if ( !instance.listeners.containsKey(plugin) )
 			instance.listeners.put(plugin, new ArrayList<StatMethod>());
 		
 		//add all @Stat methods 
 		for ( Method method : clazz.getMethods() )
 		{
 			if ( method.isAnnotationPresent(Stat.class) )
 			{
 				Stat stat = method.getAnnotation(Stat.class);
 				if ( !stat.requestType().update() )
 				    instance.listeners.get(plugin).add(new StatMethod(inst, clazzStat + stat.name(), method));
 			}
 		}
 	}
 	
 	/**
 	 * Registers a new updater for the given plugin
 	 * @param plugin
 	 * The plugin
 	 * @param listener
 	 * that will be registered
 	 */
 	public static void registerUpdater(String plugin, Class<? extends Updater> clazz)
 	{
 		//load any clazz stat name
 		String clazzStat = "";
 		if ( clazz.isAnnotationPresent(Stat.class) )
 			clazzStat = clazz.getAnnotation(Stat.class).name() + "/";
 
 		//new listener instance
 		Object inst = __clazzInstance(clazz);
 		
 		//create new list
 		if ( !instance.updaters.containsKey(plugin) )
 			instance.updaters.put(plugin, new ArrayList<StatMethod>());
 
 		//add all @Stat methods 
 		for ( Method method : clazz.getMethods() )
 		{
 			if ( method.isAnnotationPresent(Stat.class) )
 			{
 				Stat stat = method.getAnnotation(Stat.class);
 				if ( !stat.requestType().get() )
					instance.listeners.get(plugin).add(new StatMethod(inst, clazzStat + stat.name() + "/{value}", method));
 			}
 		}
 	}
 	
 	/**
 	 * Registers a new listener for the given plugin
 	 * @param plugin
 	 * The plugin
 	 * @param clazz
 	 * that will be registered
 	 */
 	public static void registerListener(String plugin, Listener listener)
 	{
 		//get the class
 		Class<? extends Listener> clazz = listener.getClass();
 		
 		//load any clazz stat name
 		String clazzStat = "";
 		if ( clazz.isAnnotationPresent(Stat.class) )
 			clazzStat = clazz.getAnnotation(Stat.class).name() + "/";
 		
 		//create new list
 		if ( !instance.listeners.containsKey(plugin) )
 			instance.listeners.put(plugin, new ArrayList<StatMethod>());
 		
 		//add all @Stat methods 
 		for ( Method method : clazz.getMethods() )
 		{
 			if ( method.isAnnotationPresent(Stat.class) )
 			{
 				Stat stat = method.getAnnotation(Stat.class);
 				if ( !stat.requestType().update() )
 				    instance.listeners.get(plugin).add(new StatMethod(listener, clazzStat + stat.name(), method));
 			}
 		}
 	}
 	
 	/**
 	 * Registers a new updater for the given plugin
 	 * @param plugin
 	 * The plugin
 	 * @param listener
 	 * that will be registered
 	 */
 	public static void registerUpdater(String plugin, Updater updater)
 	{
 		//get the class
 		Class<? extends Updater> clazz = updater.getClass();
 		
 		//load any clazz stat name
 		String clazzStat = "";
 		if ( clazz.isAnnotationPresent(Stat.class) )
 			clazzStat = clazz.getAnnotation(Stat.class).name() + "/";
 		
 		//create new list
 		if ( !instance.updaters.containsKey(plugin) )
 			instance.updaters.put(plugin, new ArrayList<StatMethod>());
 
 		//add all @Stat methods 
 		for ( Method method : clazz.getMethods() )
 		{
 			if ( method.isAnnotationPresent(Stat.class) )
 			{
 				Stat stat = method.getAnnotation(Stat.class);
 				if ( !stat.requestType().get() )
					instance.listeners.get(plugin).add(new StatMethod(updater, clazzStat + stat.name() + "/{value}", method));
 			}
 		}
 	}
 	
 	/**
 	 * Searches for the requested plugin updater, if found calls an update event. 
 	 * @param plugin
 	 * The requested plugin updater
 	 * @param stat
 	 * The requested stat to update
 	 */
 	public static void update(String plugin, String stat)
 	{
 		try
 		{
 			instance.__update(plugin, stat);
 		}
 		catch( Exception e ) { }
 	}
 	
 	public void __update(String plugin, String stat) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
 	{
         Iterator<StatMethod> it = listeners.get(plugin).iterator();
 		while(it.hasNext() && !it.next().invoke(stat));
 	}
 
 	/**
 	 * Searches for the requested plugin listener, if found calls an event to get the stat value. 
 	 * @param plugin
 	 * The plugin that holds out stat
 	 * @param stat
 	 * The stat which value will be gathered
 	 */
 	public static Response get(String plugin, String stat)
 	{
 		try
 		{
 			return instance.__get(plugin, stat);
 		}
 		catch( Exception e ) { e.printStackTrace(); }
 		return new ObjectResponse(null);
 	}
 	
 	public Response __get(String plugin, String stat) 
 	{
 		Iterator<StatMethod> it = listeners.get(plugin).iterator();
 		
 		StatMethod method = null;
 		boolean done = false;
 		while(it.hasNext() && !done) 
 		{
 			try
 			{
 			    done = (method = it.next()).invoke(stat);
 			} catch( Exception e ) { e.printStackTrace(); }
 		}
 	
 		return !done ? new ObjectResponse(null) : method.result(); 
 	}
 	
 	static class StatMethod
 	{
 		private Object instance; 
 		private Method statMethod;
 		private Pattern statPattern;
 		private Response lastResult;
 		
 		public StatMethod(Object inst, String statDefinition, Method method)
 		{
 			statPattern = statPattern(statDefinition);
 			statMethod = method;
 			instance = inst;
 		}
 		
 		public Response result()
 		{
 			return lastResult;
 		}
 
 		public boolean invoke(String request) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
 		{
 			Matcher matcher = statPattern.matcher(request);
 			if ( !matcher.matches() ) return false;
 			
 			List<String> values = new ArrayList<String>();
 			for ( int i = 0 ; i < matcher.groupCount() ; ++i )
 				values.add(matcher.group(i+1));
 			
 			//set the response result
 			Object result = statMethod.invoke(instance, values.isEmpty() ? null : values.toArray());
 			if ( result instanceof Response )
 				lastResult = (Response) result;
 			else
 				lastResult = new ObjectResponse(result);
 			
 			//true because finished
 			return true;
 		}
 	}
 	
 	//the stat name resolving pattern
 	private static Pattern statisticPattern = Pattern.compile("\\{([^\\{\\}/]+)\\}|([^\\{\\}/]+)");
 
 	public static void main(String[] a)
 	{
 		Matcher matcher = statisticPattern.matcher("listenerCount");
 		StringBuffer pattern = new StringBuffer();
 		while(matcher.find())
 		{
 			if ( matcher.group(1) != null )
 				matcher.appendReplacement(pattern, "([^\\\\{\\\\}/]+)");
 			else
 				matcher.appendReplacement(pattern, "$2");
 		}
 		System.out.print(pattern.toString());
 	}
 	/**
 	 * Create a new statistic pattern for a specific statistic
 	 * @param stat
 	 * The statistic definition path
 	 * @return
 	 * the new created pattern
 	 */
 	private static Pattern statPattern(String stat)
 	{
 		Matcher matcher = statisticPattern.matcher(stat);
 		StringBuffer pattern = new StringBuffer();
 		while(matcher.find())
 		{
 			if ( matcher.group(1) != null )
 				matcher.appendReplacement(pattern, "([^/]+)");
 			else
 				matcher.appendReplacement(pattern, "$2");
 		}
 		return Pattern.compile(pattern.toString());
 	}
 }
