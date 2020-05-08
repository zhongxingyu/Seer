 package me.asofold.bukkit.fattnt.config;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import me.asofold.bukkit.fattnt.config.compatlayer.CompatConfig;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.EntityType;
 
 /**
  * World dependent settings.<br>
  * PrioritySettings: Methods will not update sub objects like ConfinementSettings.
  * @author mc_dev
  *
  */
 public class WorldSettings {
 
 	public int priority = 0;
 	
 	public ExplosionSettings explosion;
 	
 	public Map<EntityType, ExplosionSettings> entities = new HashMap<EntityType, ExplosionSettings>();
 	
 	public WorldSettings(){
 	}
 	
 	public WorldSettings(int priority){
 		this.priority = priority;
 		explosion = new ExplosionSettings(priority);
 	}
 
 	public boolean hasValues() {
 		if (explosion.hasValues()) return true;
 		for (ExplosionSettings ees : entities.values()){
 			if (ees.hasValues()) return true;
 		}
 		return false;
 	}
 
 	public void fromConfig(CompatConfig cfg, String prefix){
 		priority = 0;
 		if (cfg.contains(prefix + Path.priority)) priority  = cfg.getInt(prefix + Path.priority, (int) 0);
 		
 		// ExplosionSettings:
 		explosion = new ExplosionSettings(priority);
 		explosion.applyConfig(cfg, prefix, priority);
 		
 		// Entity settings:
 		entities.clear();
 		for (String key : cfg.getStringKeys(prefix + Path.explodingEntities)){
 			EntityType type;
 			try{
 				type = EntityType.valueOf(key.trim().toUpperCase().replace("-", "_"));
 			}
 			catch (Throwable t){
 				Bukkit.getLogger().warning("[FatTnt] Bad entity type ("+key+") at: " + prefix + Path.explodingEntities);
 				continue;
 			}
 			ExplosionSettings ees = new ExplosionSettings(priority);
			ees.applyConfig(cfg, prefix + Path.explodingEntities + Path.sep + key + Path.sep, priority);
 			entities.put(type, ees);
 		}
 	}
 	
 //	public void toConfig(Configuration cfg, String prefix){
 //		if (priority != 0) cfg.set(prefix + Path.confine, priority);
 //		
 //		explosion.toConfig(cfg, prefix);
 //		
 //		for (Entry<EntityType, ExplosionSettings> entry : entities.entrySet()){
 //			entry.getValue().toConfig(cfg, prefix + Path.explodingEntities + Path.sep + entry.getKey().toString() + Path.sep);
 //		}
 //		
 //	}
 
 	/**
 	 * Get maximal maxRadius.
 	 * @return
 	 */
 	public float getMaxRadius(){
 		float maxRadius = explosion.maxRadius.getValue(0.0f); // TODO
 		for (ExplosionSettings ees : entities.values()){
 			maxRadius = Math.max(maxRadius, ees.maxRadius.getValue(0.0f));
 		}
 		return maxRadius;
 	}
 
 	public void applyToExplosionSettings(ExplosionSettings other, EntityType type) {
 		other.applySettings(explosion);
 		if (type == null) return;
 		ExplosionSettings ees = entities.get(type);
		if (ees != null){
			other.applySettings(ees);
		}
 	}
 	
 }
