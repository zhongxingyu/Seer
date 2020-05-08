 package co.networkery.uvbeenzaned;
 
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import org.bukkit.Location;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class configOps{
 
 	public static ConfigAccessor config;
 	public static ConfigAccessor scores;
 	
 	public configOps(JavaPlugin jp)
 	{
 		config = new ConfigAccessor(jp, "config.yml");
 		scores = new ConfigAccessor(jp, "scores.yml");
 	}
 	
 	public static void load()
 	{
 		config.saveDefaultConfig();
 		if(config.getConfig().getInt("timerdelay") != 0)
 		{
 			SnowballerListener.timerdelay = config.getConfig().getInt("timerdelay");
 		}
 		else
 		{
 			SnowballerListener.timerdelay = 30000;
 		}
 		if(config.getConfig().getInt("teampoints") != 0)
 		{
			SnowballerListener.timerdelay = config.getConfig().getInt("teampoints");
 		}
 		else
 		{
 			SnowballerListener.teampoints = 4;
 		}
 		if(config.getConfig().getString("lobbyspawnlocation") != null && config.getConfig().getString("lobbyspawnlocation") != "")
 		{
 			SnowballerListener.lobbyspawnlocation = LTSTL.str2loc(config.getConfig().getString("lobbyspawnlocation"));
 		}
 		if(config.getConfig().getConfigurationSection("teamcyanarenasides").getKeys(false) != null)
 		{
 			for(String key : config.getConfig().getConfigurationSection("teamcyanarenasides").getKeys(false))
 			{
 				if(config.getConfig().getString("teamcyanarenasides." + key) != null)
 				{
 					SnowballerListener.teamcyanarenasides.put(key, LTSTL.str2loc(config.getConfig().getString("teamcyanarenasides." + key)));
 				}
 			}
 		}
 		if(config.getConfig().getConfigurationSection("teamlimearenasides").getKeys(false) != null)
 		{
 			for(String key : config.getConfig().getConfigurationSection("teamlimearenasides").getKeys(false))
 			{
 				if(config.getConfig().getString("teamlimearenasides." + key) != null)
 				{
 					SnowballerListener.teamlimearenasides.put(key, LTSTL.str2loc(config.getConfig().getString("teamlimearenasides." + key)));
 				}
 			}
 		}
 		//shows system time in ms for an example of what the seed for the random engine will look like
 		//log.info("Current system time in ms: " + Long.toString(System.currentTimeMillis()));
 	}
 	
 	public static void save()
 	{
 		//save timer delay in ms for continuus no admin games
 		  if(SnowballerListener.timerdelay != 0)
 		  {
 			  config.getConfig().set("timerdelay", SnowballerListener.timerdelay);
 		  }
 		  //save default team win points allocation
 		  if(SnowballerListener.teampoints != 0)
 		  {
			  config.getConfig().set("timerpoints", SnowballerListener.teampoints);
 		  }
 		  //save lobby spawn location
 		  if(SnowballerListener.lobbyspawnlocation != null)
 		  {
 			  config.getConfig().set("lobbyspawnlocation", LTSTL.loc2str(SnowballerListener.lobbyspawnlocation));
 		  }
 		  //save team sides in serializable location format
 		  for(Entry<String, Location> entry : SnowballerListener.teamcyanarenasides.entrySet())
 		  {
 			  config.getConfig().set("teamcyanarenasides." + entry.getKey(), LTSTL.loc2str(entry.getValue()));
 		  }
 		  for(Entry<String, Location> entry : SnowballerListener.teamlimearenasides.entrySet())
 		  {
 			  config.getConfig().set("teamlimearenasides." + entry.getKey(), LTSTL.loc2str(entry.getValue()));
 		  }
 		  //temporary collective scores list
 		  HashMap<String, Integer> tmpscores = new HashMap<String, Integer>();
 		  //save team scores
 		  for(Entry<String, Integer> entry : SnowballerListener.teamcyan.entrySet())
 		  {
 			  tmpscores.put(entry.getKey(), entry.getValue());
 		  }
 		  for(Entry<String, Integer> entry : SnowballerListener.teamlime.entrySet())
 		  {
 			  tmpscores.put(entry.getKey(), entry.getValue());
 		  }
 		  for(Entry<String, Integer> entry : tmpscores.entrySet())
 		  {
 			  scores.getConfig().set(entry.getKey(), entry.getValue());
 		  }
 		  //save all config to file
 		  config.saveConfig();
 		  scores.saveConfig();
 	}
 	
 	public static void saveScores()
 	{
 		  //temporary collective scores list
 		  HashMap<String, Integer> tmpscores = new HashMap<String, Integer>();
 		  //save team scores
 		  for(Entry<String, Integer> entry : SnowballerListener.teamcyan.entrySet())
 		  {
 			  tmpscores.put(entry.getKey(), entry.getValue());
 		  }
 		  for(Entry<String, Integer> entry : SnowballerListener.teamlime.entrySet())
 		  {
 			  tmpscores.put(entry.getKey(), entry.getValue());
 		  }
 		  for(Entry<String, Integer> entry : tmpscores.entrySet())
 		  {
 			  scores.getConfig().set(entry.getKey(), entry.getValue());
 		  }
 		  scores.saveConfig();
 	}
 }
