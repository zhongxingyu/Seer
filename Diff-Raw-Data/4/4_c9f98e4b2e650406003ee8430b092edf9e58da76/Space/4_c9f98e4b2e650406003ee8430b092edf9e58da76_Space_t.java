 package com.araeosia.space;
 
 import com.araeosia.space.util.ChunkPair;
 import com.araeosia.space.util.Planet;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 public class Space extends JavaPlugin {
 
	public Logger logger;
 	public SpaceGenerator spaceGenerator;
 	public SpaceListener spaceListener;
 
 	@Override
 	public void onEnable(){
		logger = this.getLogger();
 		logger.info("Enabling SPACCEEEEEE...");
 		spaceGenerator = new SpaceGenerator(this);
 		spaceListener = new SpaceListener(this);
 		getServer().getPluginManager().registerEvents(spaceListener, this);
 		if(getConfig().isConfigurationSection("planets")){
 			for(String s : getConfig().getConfigurationSection("planets").getKeys(false)){
 				String[] data = s.split("§");
 				ChunkPair cp = new ChunkPair(Integer.parseInt(data[0]), Integer.parseInt(data[1]));
 				if(!spaceGenerator.getPlanets().containsKey(cp)){
 					spaceGenerator.getPlanets().put(cp, new ArrayList<Planet>());
 				}
 				for(String s1 : getConfig().getConfigurationSection("planets."+s).getKeys(false)){
 					spaceGenerator.getPlanets().get(cp).add(Planet.load(s1));
 				}
 			}
 		}
 	}
 	@Override
 	public void onDisable(){
 		logger.info("Disabling Space.");
 		for(ChunkPair cp : spaceGenerator.getPlanets().keySet()){
 			for(Planet p : spaceGenerator.getPlanets().get(cp)){
 				getConfig().set("planets."+cp.toString()+"."+p.save(), true);
 			}
 		}
 	}
 	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id){
 		return spaceGenerator;
 	}
 }
