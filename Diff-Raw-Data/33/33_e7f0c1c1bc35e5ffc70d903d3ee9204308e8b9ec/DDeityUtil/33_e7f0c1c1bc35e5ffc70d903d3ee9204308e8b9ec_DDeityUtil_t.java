 package com.legit2.Demigods.Utilities;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 
 import com.legit2.Demigods.Utilities.DUtil;
 
 public class DDeityUtil 
 {
 	/*
 	 *  getDeityClass() : Returns the string of the (String)deity's classpath.
 	 */
 	public static String getDeityClass(String deity)
 	{
		return DDataUtil.getPluginData("deity_classes_temp", deity).toString();
 	}
 	
 	/*
 	 *  InvokeDeityMethod() : Invokes a static method (with no paramaters) from inside a deity class.
 	 */
 	@SuppressWarnings("rawtypes")
 	public static Object invokeDeityMethod(String deityClass, String method) throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
 	{		
 		// No Paramaters
 		Class noparams[] = {};
 		
 		// Creates a new instance of the deity class
 		Object obj = Class.forName(deityClass, true, DUtil.getPlugin().getClass().getClassLoader()).newInstance();
 		
 		// Load everything else for the Deity (Listener, etc.)
 		Method toInvoke = Class.forName(deityClass, true, DUtil.getPlugin().getClass().getClassLoader()).getMethod(method, noparams);
 		
		return toInvoke.invoke(obj, (Object[])null);
 	}
 	
 	/*
 	 *  InvokeDeityMethodWithString() : Invokes a static method, with a String, from inside a deity class.
 	 */
 	public static Object invokeDeityMethodWithString(String deityClass, String method, String paramater) throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
 	{			
 		// Creates a new instance of the deity class
 		Object obj = Class.forName(deityClass, true, DUtil.getPlugin().getClass().getClassLoader()).newInstance();
 		
 		// Load everything else for the Deity (Listener, etc.)
 		Method toInvoke = Class.forName(deityClass, true, DUtil.getPlugin().getClass().getClassLoader()).getMethod(method, String.class);
 		
		return toInvoke.invoke(obj, paramater);
 	}
 	
 	/*
 	 *  InvokeDeityMethodWithPlayer() : Invokes a static method, with a Player, from inside a deity class.
 	 */
 	public static Object invokeDeityMethodWithPlayer(String deityClass, String method, Player paramater) throws NoSuchMethodException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
 	{			
 		// Creates a new instance of the deity class
 		Object obj = Class.forName(deityClass, true, DUtil.getPlugin().getClass().getClassLoader()).newInstance();
 		
 		// Load everything else for the Deity (Listener, etc.)
 		Method toInvoke = Class.forName(deityClass, true, DUtil.getPlugin().getClass().getClassLoader()).getMethod(method, Player.class);
 		
		return toInvoke.invoke(obj, paramater);
 	}
 	
 	/*
 	 *  getLoadedDeityNames() : Returns a ArrayList<String> of all the loaded deities' names.
 	 */
 	public static ArrayList<String> getLoadedDeityNames()
 	{
 		ArrayList<String> toReturn = new ArrayList<String>();
 		
 		for(String deity : DDataUtil.getAllPluginData().get("deity_alliances_temp").keySet())
 		{
 			toReturn.add(deity);
 		}
 		
 		return toReturn;
 	}
 	
 	/*
 	 *  getLoadedDeityAlliances() : Returns a ArrayList<String> of all the loaded deities' alliances.
 	 */
 	public static ArrayList<String> getLoadedDeityAlliances()
 	{
 		ArrayList<String> toReturn = new ArrayList<String>();
 		
 		for(Object alliance : DDataUtil.getAllPluginData().get("deity_alliances_temp").values().toArray())
 		{
 			if(toReturn.contains((String) alliance)) continue;
 			toReturn.add((String) alliance);
 		}
 		
 		return toReturn;
 	}
 	
 	/*
 	 *  getDeityAlliance() : Returns a String of a loaded (String)deity's alliance.
 	 */
 	public static String getDeityAlliance(String deity)
 	{
 		String toReturn = (String) DDataUtil.getPluginData("deity_alliances_temp", deity);
 		return toReturn;
 	}
 	
 	/*
 	 *  getDeityClaimItems() : Returns an ArrayList<Material> of a loaded (String)deity's claim items.
 	 */
 	@SuppressWarnings("unchecked")
 	public static ArrayList<Material> getDeityClaimItems(String deity)
 	{
 		ArrayList<Material> toReturn = (ArrayList<Material>) DDataUtil.getPluginData("deity_claim_items_temp", deity);
 		return toReturn;
 	}
 	
 	/*
 	 *  getAllDeitiesInAlliance() : Returns a ArrayList<String> of all the loaded deities' names.
 	 */
 	public static ArrayList<String> getAllDeitiesInAlliance(String alliance)
 	{
 		ArrayList<String> toReturn = new ArrayList<String>();
 		
 		for(String deity : DDataUtil.getAllPluginData().get("deity_alliances_temp").keySet())
 		{
 			if(!(getDeityAlliance(deity)).equalsIgnoreCase(alliance)) continue;
 			toReturn.add(deity);
 		}
 		
 		return toReturn;
 	}
 }
