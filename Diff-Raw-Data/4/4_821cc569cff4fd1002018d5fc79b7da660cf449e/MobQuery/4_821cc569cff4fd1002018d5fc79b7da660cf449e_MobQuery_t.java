 package com.comphenix.xp.lookup;
 
 /**
  *  ExperienceMod - Bukkit server plugin for modifying the experience system in Minecraft.
  *  Copyright (C) 2012 Kristian S. Stangeland
  *
  *  This program is free software; you can redistribute it and/or modify it under the terms of the 
  *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
  *  the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
  *  See the GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License along with this program; 
  *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
  *  02111-1307 USA
  */
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.bukkit.entity.Ageable;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Tameable;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 
 import com.comphenix.xp.parser.Utility;
 import com.google.common.collect.Lists;
 
 public class MobQuery implements Query {
 
 	// DON'T CARE fields are empty
 	private List<EntityType> type;
 	private List<DamageCause> deathCause;
 	private List<Boolean> spawner;
 	private List<Boolean> baby;
 	private List<Boolean> tamed;
 	
 	// Optimize away object creations
 	private static List<EntityType> noTypes = new ArrayList<EntityType>();
 	private static List<DamageCause> noDamages = new ArrayList<DamageCause>();
 	private static List<Boolean> noBooleans = new ArrayList<Boolean>();
 	
 	/**
 	 * Universal query.
 	 */
 	public MobQuery() {
 		this(noTypes, noDamages, noBooleans, noBooleans, noBooleans);
 	}
 	
 	public MobQuery(EntityType type) {
 		this(type, null, null, null, null);
 	}
 	
 	public MobQuery(EntityType type, DamageCause deathCause, SpawnReason reason, Boolean baby, Boolean tamed) {
 		setSingles(type, deathCause, reason, baby, tamed);
 	}
 	
 	public MobQuery(List<EntityType> type, List<DamageCause> deathCause,
 				    List<Boolean> spawner, List<Boolean> baby, List<Boolean> tamed) {
 		this.type = type;
 		this.deathCause = deathCause;
 		this.spawner = spawner;
 		this.baby = baby;
 		this.tamed = tamed;
 	}
 
 	public static MobQuery fromExact(EntityType type, DamageCause deathCause, 
 									 SpawnReason reason, Boolean baby, Boolean tamed) {
 		return new MobQuery(
 				Lists.newArrayList(type), 
 				Lists.newArrayList(deathCause),
 				Lists.newArrayList(reason == null ? null : 
 					(reason == SpawnReason.SPAWNER)),
 				Lists.newArrayList(baby),
 				Lists.newArrayList(tamed)
 		);
 	}
 	
 	public MobQuery(LivingEntity entity, SpawnReason reason) {
 		
 		EntityDamageEvent cause = entity.getLastDamageCause();
 		DamageCause deathCause = null;
 		
		Boolean paramBaby = false;
		Boolean paramTamed = false;
 		
 		if (cause != null) {
 			deathCause = cause.getCause();
 		}
 		
 		// Check age and tame status
 		if (entity instanceof Ageable) {
 			paramBaby = !((Ageable) entity).isAdult();
 		}
 		
 		if (entity instanceof Tameable) {
 			paramTamed = ((Tameable)entity).isTamed();
 		}
 
 		// Load from singles
 		setSingles(entity.getType(), deathCause, reason, paramBaby, paramTamed);
 	}
 	
 	private void setSingles(EntityType type, DamageCause deathCause, SpawnReason reason, Boolean baby, Boolean tamed) {
 		this.type = Utility.getElementList(type);
 		this.deathCause = Utility.getElementList(deathCause);
 		this.baby = Utility.getElementList(baby);
 		this.tamed = Utility.getElementList(tamed);
 		
 		if (reason != null) 
 			this.spawner = Arrays.asList(reason == SpawnReason.SPAWNER);
 		else
 			this.spawner = new ArrayList<Boolean>();
 	}
 
 	public List<DamageCause> getDeathCause() {
 		return deathCause;
 	}
 	
 	public List<EntityType> getType() {
 		return type;
 	}
 	
 	public List<Boolean> getSpawner() {
 		return spawner;
 	}
 	
 	public List<Boolean> getBaby() {
 		return baby;
 	}
 	
 	public List<Boolean> getTamed() {
 		return tamed;
 	}
 	
 	public boolean hasType() {
 		return type != null && !type.isEmpty();
 	}
 	
 	public boolean hasDeathCause() {
 		return deathCause != null && !deathCause.isEmpty();
 	}
 	
 	public boolean hasSpawner() {
 		return spawner != null && !spawner.isEmpty();
 	}
 	
 	public boolean hasBaby() {
 		return baby != null && !baby.isEmpty();
 	}
 	
 	public boolean hasTamed() {
 		return tamed != null && !tamed.isEmpty();
 	}
 	
 	@Override
 	public int hashCode() {
 		return new HashCodeBuilder(17, 31).
 	            append(type).
 	            append(deathCause).
 	            append(spawner).
 	            append(baby).
 	            append(tamed).
 	            toHashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null)
             return false;
         if (obj == this)
             return true;
         if (obj.getClass() != getClass())
             return false;
 
         MobQuery other = (MobQuery) obj;
         return new EqualsBuilder().
             append(type, other.type).
             append(deathCause, other.deathCause).
             append(spawner, other.spawner).
             append(baby, other.baby).
             append(tamed, other.tamed).
             isEquals();
 	}
 	
 	@Override
 	public String toString() {
 		// TODO Auto-generated method stub
 		return String.format("%s|%s|%s|%s|%s", 
 							hasType() ? StringUtils.join(type, ", ") : "",
 							hasDeathCause() ? StringUtils.join(deathCause, ", ") : "",
 						    Utility.formatBoolean("spawner", spawner),
 							Utility.formatBoolean("baby", baby),
 							Utility.formatBoolean("tamed", tamed));
 	}
 	
 	@Override
 	public Types getQueryType() {
 		return Types.Mobs;
 	}
 }
