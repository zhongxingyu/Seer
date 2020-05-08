 package no.runsafe.runsafeinventories;
 
 import com.google.common.collect.Lists;
 import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.IConsole;
 import no.runsafe.framework.api.IServer;
 import no.runsafe.framework.api.IWorld;
 import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
 import no.runsafe.framework.api.hook.IUniverseMapper;
 import no.runsafe.framework.api.player.IPlayer;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class UniverseHandler implements IConfigurationChanged, IUniverseMapper
 {
 	public UniverseHandler(IConsole output, IServer server)
 	{
 		this.output = output;
 		this.server = server;
 	}
 
 	@Deprecated
 	public String getUniverseName(String worldName)
 	{
 		if (this.universes.containsKey(worldName))
 			return this.universes.get(worldName);
 
 		return worldName;
 	}
 
 	@Deprecated
 	public String getUniverseName(IWorld world)
 	{
 		return world.getUniverse().getName();
 	}
 
 	public boolean universeExists(String universeName)
 	{
 		return this.universes.containsValue(universeName);
 	}
 
 	public boolean worldExists(String worldName)
 	{
 		return server.getWorld(worldName) != null;
 	}
 
 	@Deprecated
 	public boolean isInUniverse(IPlayer player, String universeName)
 	{
 		return this.getUniverseName(player.getWorld()).equals(universeName);
 	}
 
 	@Override
 	public List<String> GetUniverses()
 	{
 		return Lists.newArrayList(worlds.keySet());
 	}
 
 	@Override
 	public List<String> GetWorlds(String universe)
 	{
 		if (!worlds.containsKey(universe))
 			return null;
 		return worlds.get(universe);
 	}
 
 	@Override
 	public String GetUniverse(String world)
 	{
 		if (universes.containsKey(world))
 			return universes.get(world);
 		return null;
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration configuration)
 	{
 		Map<String, List<String>> section = configuration.getConfigSectionsAsList("universes");
 
 		for (String name : section.keySet())
 		{
 			List<String> worlds = section.get(name);
 			this.output.logInformation("Universe %s found with %s worlds.", name, worlds.size());
 			for (String worldName : worlds)
 				this.universes.put(worldName, name);
 
 			if (!this.worlds.containsKey(name))
 				this.worlds.put(name, new ArrayList<String>());
 			this.worlds.get(name).addAll(worlds);
 		}
 	}
 
 	private final HashMap<String, String> universes = new HashMap<String, String>();
 	private final HashMap<String, List<String>> worlds = new HashMap<String, List<String>>();
 	private final IConsole output;
 	private final IServer server;
 }
