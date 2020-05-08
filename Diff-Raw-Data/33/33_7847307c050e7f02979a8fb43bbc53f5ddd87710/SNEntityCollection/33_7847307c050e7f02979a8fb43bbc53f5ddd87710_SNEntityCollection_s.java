 package com.TeamNovus.Supernaturals.Collections;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.UUID;
 
 import org.bukkit.entity.LivingEntity;
 
 import com.TeamNovus.Supernaturals.Entity.SNEntity;
 
 public class SNEntityCollection {
 	private HashMap<UUID, SNEntity> entities = new HashMap<UUID, SNEntity>();
 
 	public SNEntity get(LivingEntity entity) {
		if(entities.containsKey(entity.getUniqueId())) {
 			return attach(new SNEntity(entity));
 		}
 		
 		return entities.get(entity.getUniqueId());
 	}
 	
	public Collection<SNEntity> getEntites() {
		return entities.values();
	}
	
 	public Boolean attached(SNEntity e) {
 		if (e.getUUID() == null)
 			return false;
 		
 		return entities.containsKey(e.getUUID());
 	}
 	
 	public Boolean detached(SNEntity e) {
 		return !(attached(e));
 	}
 	
	public SNEntity attach(SNEntity e) {		
 		return entities.put(e.getUUID(), e);
 	}
 	
 	public void detach(SNEntity e) {
 		entities.remove(e);
 	}
 	
 }
