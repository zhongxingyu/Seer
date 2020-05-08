 package com.theminequest.MineQuest.API.Utils;
 
 import org.bukkit.entity.EntityType;
 
 public class MobUtils {
 	
 	public static EntityType getEntityType(String s){
 		if (s==null)
 			return null;
 		
		for (EntityType type : EntityType.values()){
			if (type.name().equalsIgnoreCase(s.trim()) || type.name().replaceAll("_", "").equalsIgnoreCase(s.trim().replaceAll("_", "")))
				return type;
		}
		
 		EntityType ret = EntityType.fromName(s);
 		if (ret == null) {
 			try {
 				ret = EntityType.valueOf(s.toUpperCase());
 			} catch (IllegalArgumentException e) {}
 		}
 		if (ret == null) {
 			try {
 				ret = EntityType.fromId(Integer.parseInt(s));
 			} catch (NumberFormatException e) {}
 		}
 		return ret;
 	}
 
 }
