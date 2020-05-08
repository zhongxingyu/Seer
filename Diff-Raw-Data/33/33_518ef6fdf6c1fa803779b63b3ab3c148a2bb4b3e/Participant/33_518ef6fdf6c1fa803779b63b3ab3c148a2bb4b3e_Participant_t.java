 package com.censoredsoftware.demigods.battle;
 
import com.censoredsoftware.demigods.player.DCharacter;
 import org.bukkit.Location;
 import org.bukkit.entity.LivingEntity;
 
import java.util.UUID;
 
 public interface Participant
 {
 	public UUID getId();
 
 	public void setCanPvp(boolean pvp);
 
	public boolean canPvp();
 
 	public Location getCurrentLocation();
 
 	public DCharacter getRelatedCharacter();
 
 	public LivingEntity getEntity();
 }
