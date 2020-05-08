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
 
 import org.bukkit.entity.Ageable;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Tameable;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 
 public class MobQuery implements Query {
 
 	// DON'T CARE fields are marked with NULL
 	private EntityType type;
 	private DamageCause deathCause;
 	private Boolean spawner;
 	private Boolean baby;
 	private Boolean tamed;
 	
 	public MobQuery(EntityType type) {
 		this(type, null, null, null, null);
 	}
 	
 	public MobQuery(EntityType type, DamageCause deathCause, SpawnReason reason, Boolean baby, Boolean tamed) {
 		this.type = type;
 		this.deathCause = deathCause;
		this.spawner = reason == SpawnReason.SPAWNER;
 		this.baby = baby;
 		this.tamed = tamed;
 	}
 	
 	public MobQuery(LivingEntity entity, SpawnReason reason) {
 		
 		EntityDamageEvent cause = entity.getLastDamageCause();
 		
 		this.type = entity.getType();
 		this.spawner = reason == SpawnReason.SPAWNER;
 		
 		if (cause != null) {
 			this.deathCause = cause.getCause();
 		}
 		// Check age and tame status
 		if (entity instanceof Ageable) {
 			this.baby = !((Ageable) entity).isAdult();
 		}
 		if (entity instanceof Tameable) {
 			this.tamed = ((Tameable)entity).isTamed();
 		}
 	}
 	
 	public DamageCause getDeathCause() {
 		return deathCause;
 	}
 	
 	public EntityType getType() {
 		return type;
 	}
 	
 	public Boolean getSpawner() {
 		return spawner;
 	}
 	
 	public Boolean getBaby() {
 		return baby;
 	}
 	
 	public Boolean getTamed() {
 		return tamed;
 	}
 	
 	public boolean hasType() {
 		return type != null;
 	}
 	
 	public boolean hasDeathCause() {
 		return deathCause != null;
 	}
 	
 	public boolean hasSpawner() {
 		return spawner != null;
 	}
 	
 	public boolean hasBaby() {
 		return baby != null;
 	}
 	
 	public boolean hasTamed() {
 		return tamed != null;
 	}
 	
 	@Override
 	public String toString() {
 		// TODO Auto-generated method stub
 		return String.format("%s|%s|%s|%s|%s", 
 							hasType() ? type : "",
 							hasDeathCause() ? deathCause : "",
 						    Parsing.formatBoolean("spawner", spawner),
 							Parsing.formatBoolean("baby", baby),
 							Parsing.formatBoolean("tamed", tamed));
 	}
 	
 	@Override
 	public Types getQueryType() {
 		return Types.Mobs;
 	}
 }
