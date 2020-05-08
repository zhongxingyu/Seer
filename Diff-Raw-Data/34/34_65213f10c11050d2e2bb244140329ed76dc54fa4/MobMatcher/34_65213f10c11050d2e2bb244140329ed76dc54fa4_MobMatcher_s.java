 package com.comphenix.xp.parser.text;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import com.comphenix.xp.parser.Utility;
 
 import org.bukkit.entity.EntityType;
 import com.google.common.collect.Lists;
 
 /**
  * Used in conjunction with the MobEntityTypeParser to encode multiple entity types per parse token.
  * @author Kristian
  */
 public class MobMatcher {
 	/**
 	 * Every mob category.
 	 * @author Kristian
 	 */
 	public enum Category {
 		// Categories
 		/**
 		 * Utility mobs may be created by and serve the player. 
 		 */
 		UTILITY(EntityType.IRON_GOLEM, EntityType.SNOWMAN),
 		
 		/**
 		 * Passive mobs will never attack the player.
 		 */
 		PASSIVE(EntityType.CHICKEN, EntityType.COW, EntityType.MUSHROOM_COW, EntityType.OCELOT, 
 				EntityType.PIG, EntityType.SHEEP, EntityType.SQUID, EntityType.VILLAGER),
 				
 		/**
 		 * Neutral mobs will not attack the player unless provoked. The act of provoking neutral mobs varies between mobs. 
 		 */
 		NEUTRAL(EntityType.IRON_GOLEM, EntityType.ENDERMAN, EntityType.PIG_ZOMBIE, EntityType.WOLF),
 		
 		/**
 		 * Hostile mobs will attack the player when in range.
 		 */
 		HOSTILE(EntityType.BLAZE, EntityType.CAVE_SPIDER, EntityType.CREEPER, EntityType.GHAST, 
 				EntityType.MAGMA_CUBE, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME, 
 				EntityType.SPIDER, EntityType.ZOMBIE),
 				
 		/**
 		 * Boss mobs have a large amount of health and spawn only once per world. 
 		 */
 		BOSS(EntityType.ENDER_DRAGON),
 				
 		/**
 		 * Special marker indicating a custom category.
 		 */
 		SPECIFIC;
 		
 		private List<EntityType> members;
 		
 		private Category(EntityType... members) {
 			if (members.length == 0)
 				this.members = null;
 			else
 				this.members = Arrays.asList(members);
 		}
 		
 		public List<EntityType> getMembers() {
 			return members;
 		}
 		
 		/**
 		 * Retrieves the category with the same ENUM name, or NULL if no such category exists.
 		 * @param text - name of the category to find.
 		 * @return The found category or NULL if no such category can be found.
 		 */
 		public static Category fromName(String text) {
 			
 			String enumed = Utility.getEnumName(text);
 			
 			for (Category category : values()) {
 				if (category.name().equals(enumed))
 					return category;
 			}
 			
 			// No such category found
 			return null;
 		}
 	}
 	
 	private Category category;
 	private EntityType specific;
 	
 	public MobMatcher(EntityType specificMob) {
 		this.category = Category.SPECIFIC;
 		this.specific = specificMob;
 	}
 	
 	public MobMatcher(Category category) {
 		this.category = category;
 		this.specific = null;
 	}
 	
 	/**
 	 * Retrieves the mob category this matcher is set to.
 	 * @return The current mob category.
 	 */
 	public Category getCategory() {
 		return category;
 	}
 	
 	/**
 	 * Sets the mob category of this matcher.
 	 * @param category - new mob category.
 	 */
 	public void setCategory(Category category) {
 		this.category = category;
 	}
 
 	/**
 	 * Retrieves the specific or single entity type of this matcher.
	 * @return
 	 */
 	public EntityType getSpecific() {
 		return specific;
 	}
 
 	/**
 	 * Set the specific or single entity type of this matcher.
 	 * @param specific - specific entity type.
 	 */
 	public void setSpecific(EntityType specific) {
 		this.specific = specific;
 	}
 	
 	/**
 	 * Retrieves every entity type that matches this rule.
 	 * @return Every matched entity type.
 	 */
 	public List<EntityType> getEntityTypes() {
 		if (category == Category.SPECIFIC)
 			return Lists.newArrayList(specific);
 		else
 			return category.getMembers();
 	}
 	
 	/**
 	 * Adds every EntityType that matches the current rule to the given list.
 	 * @param destination - the given list to add entity types.
 	 */
 	public void addToList(List<EntityType> destination) {
 		if (category == Category.SPECIFIC)
 			destination.add(getSpecific());
 		else
 			destination.addAll(getEntityTypes());
 	}
 	
 	
 	/**
 	 * Flattens the entity list in every matcher.
 	 * @param matchers - list of matchers.
 	 * @return The flattened entity list.
 	 */
 	public static List<EntityType> convertToTypes(List<MobMatcher> matchers) {
 		List<EntityType> entityTypes = new ArrayList<EntityType>();
 		
 		for (MobMatcher matcher : matchers) {
 			matcher.addToList(entityTypes);
 		}
 		
 		// Flatten
 		return entityTypes;
 	}
 }
