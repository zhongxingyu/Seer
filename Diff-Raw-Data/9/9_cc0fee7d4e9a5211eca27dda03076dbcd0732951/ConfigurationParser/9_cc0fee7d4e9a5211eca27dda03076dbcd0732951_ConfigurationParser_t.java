 package de.noonex.moddropplugin.configuration;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.bukkit.Material;
 
 import de.noonex.moddropplugin.drops.AbstractDrop;
 import de.noonex.moddropplugin.drops.NormalDrop;
 
 @SuppressWarnings("rawtypes")
 public class ConfigurationParser
 {
 	
 	private List<Class> nodes;
 	
 	public ConfigurationParser() throws ClassNotFoundException
 	{
 		nodes = new LinkedList<Class>();
 		
 		try
 		{
 			nodes.add(Class.forName("de.noonex.moddropplugin.configuration.AmountNode"));
 			nodes.add(Class.forName("de.noonex.moddropplugin.configuration.DropNode"));
 			nodes.add(Class.forName("de.noonex.moddropplugin.configuration.WorldNode"));
 		}
 		catch(ClassNotFoundException ex)
 		{
 			throw ex;
 		}
 	}
 	
 	public AbstractDrop ParseString(String sentence)
 	{
		AbstractDrop newDrop = new NormalDrop(Material.STONE, (byte)0);
 		String[] words = sentence.split(" ");
 		
 		for(int i = 0; i < words.length; i++)
 		{
 			for(Class c: nodes)
 			{
 				try
 				{
 					Method m = c.getMethod("IsResponsible");
 					m.invoke(words[i]);
 				}
 				catch (SecurityException e)
 				{
 					e.printStackTrace();
 					continue;
 				}
 				catch (NoSuchMethodException e)
 				{
 					e.printStackTrace();
 					continue;
 				}
 				catch (IllegalArgumentException e)
 				{
 					e.printStackTrace();
 					continue;
 				}
 				catch (IllegalAccessException e)
 				{
 					e.printStackTrace();
 					continue;
 				}
 				catch (InvocationTargetException e)
 				{
 					e.printStackTrace();
 					continue;
 				}
 			}
 		}
 		
 		return newDrop;
 	}
 }
