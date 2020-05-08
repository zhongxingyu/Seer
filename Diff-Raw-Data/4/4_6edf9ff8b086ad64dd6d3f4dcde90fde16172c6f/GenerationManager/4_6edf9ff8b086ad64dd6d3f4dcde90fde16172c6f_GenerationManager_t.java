 package net.nexisonline.spade;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.nexisonline.spade.populators.DungeonPopulator;
 import net.nexisonline.spade.populators.PonyCaveGenerator;
 import net.nexisonline.spade.populators.SedimentGenerator;
 import net.nexisonline.spade.populators.SpadeEffectGenerator;
 import net.nexisonline.spade.populators.StalactiteGenerator;
 
 import org.bukkit.generator.BlockPopulator;
 import org.bukkit.util.config.Configuration;
 import org.bukkit.util.config.ConfigurationNode;
 
 public class GenerationManager {
 	boolean populate=true;
 	List<Class<? extends SpadeEffectGenerator>> knownBlockPopulators = new ArrayList<Class<? extends SpadeEffectGenerator>>();
 	List<BlockPopulator> populators = new ArrayList<BlockPopulator>();
 	@SuppressWarnings("unchecked")
 	public GenerationManager(SpadePlugin plugin, String world, ConfigurationNode cfg, long seed) {
 		
 		if(cfg.getProperty("populators")==null)
 			cfg.setProperty("populators", getDefaultPopulators());
 		for(Object o : cfg.getList("populators")) {
 			if(o instanceof ConfigurationNode) {
 				ConfigurationNode segNode = (ConfigurationNode)o;
 				String populatorName = segNode.getString("name","");
 				SpadeLogging.info("[GM] Current populator: "+populatorName);
 				if(!populatorName.isEmpty()) {
 					Class<? extends SpadeEffectGenerator> c;
 					try {
 						c = (Class<? extends SpadeEffectGenerator>) Class.forName(populatorName);
 						if(c==null)
 						{
 							SpadeLogging.severe("Unable to find populator: "+populatorName, null);
 							continue;
 						}
 						Method m =c.getMethod("getInstance",SpadePlugin.class, ConfigurationNode.class, long.class);
 						SpadeEffectGenerator seg = (SpadeEffectGenerator) m.invoke(null,plugin,segNode,seed);
 						populators.add(seg);
 					} catch (Exception e) {
 						SpadeLogging.severe("Unable to load populator "+populatorName,e);
 					}
 				}
 			}
 		}
 	}
 
 	private List<ConfigurationNode> getDefaultPopulators() {
 		List<ConfigurationNode> nodes = new ArrayList<ConfigurationNode>();
		ConfigurationNode currentNode;
 /*
 		// Sediment
		currentNode = Configuration.getEmptyNode();
 		currentNode.setProperty("name",SedimentGenerator.class.getName());
 		nodes.add(currentNode);
 */		
 		// Caves
 		currentNode = Configuration.getEmptyNode();
 		currentNode.setProperty("name",PonyCaveGenerator.class.getName());
 		nodes.add(currentNode);
 		
 		// Stalactites
 		currentNode = Configuration.getEmptyNode();
 		currentNode.setProperty("name",StalactiteGenerator.class.getName());
 		nodes.add(currentNode);
 		
 		// Dungeons
 		currentNode = Configuration.getEmptyNode();
 		currentNode.setProperty("name",DungeonPopulator.class.getName());
 		nodes.add(currentNode);
 		
 		return nodes;
 	}
 	
 	public static ConfigurationNode getConfigNodeFor(SpadeEffectGenerator seg) {
 		ConfigurationNode node = seg.getConfiguration();
 		node.setProperty("name", seg.getClass().getName());
 		return node;
 	}
 
 	public List<BlockPopulator> getPopulators() {
 		return populators;
 	}
 
 	public List<ConfigurationNode> getConfig() {
 		List<ConfigurationNode> nodes = new ArrayList<ConfigurationNode>();
 		for(BlockPopulator pop:populators) {
 			nodes.add(getConfigNodeFor((SpadeEffectGenerator) pop));
 		}
 		return nodes;
 	}
 }
