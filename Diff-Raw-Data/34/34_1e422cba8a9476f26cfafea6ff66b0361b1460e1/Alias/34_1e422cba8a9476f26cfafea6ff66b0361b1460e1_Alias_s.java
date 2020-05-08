 package joren.spawn;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 
 public class Alias {
 	private Class<Entity>[] types;
 	private String params;
 	
 	public Alias()
 	{
 		types = new Class[0];
 		params = "";
 	}
 
 	public Alias(Class<Entity>[] types, String params)
 	{
		Spawn.info("Constructor called, types = " + types.length + " classes, params = " + params);
 		this.types=types;
 		this.params=params;
 	}
 	
 	public Class<Entity>[] getTypes()
 	{
 		return types;
 	}
 
 	public void setTypes(Class<Entity>[] types)
 	{
 		this.types=types;
 	}
 
 	public String getParams()
 	{
 		return params;
 	}
 
 	public void setParams(String params)
 	{
 		this.params=params;
 	}
 }
