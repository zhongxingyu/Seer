 /**
  * MobDeathListener.java
  * Purpose: Implements the listener for reacting to mob deaths, caused by a player
  * 
  * @version 1.2.0 11/5/12
  * @author Scott Woodward
  */
 package com.scottwoodward.headhunter.listeners;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Blaze;
 import org.bukkit.entity.CaveSpider;
 import org.bukkit.entity.Chicken;
 import org.bukkit.entity.Cow;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.Enderman;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Ghast;
 import org.bukkit.entity.Golem;
 import org.bukkit.entity.MagmaCube;
 import org.bukkit.entity.MushroomCow;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.PigZombie;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Sheep;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Slime;
 import org.bukkit.entity.Spider;
 import org.bukkit.entity.Squid;
 import org.bukkit.entity.Villager;
 import org.bukkit.entity.Skeleton.SkeletonType;
 import org.bukkit.entity.Zombie;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.PigZapEvent;
 
 import com.scottwoodward.headhunter.helpers.ConfigHelper;
 import com.scottwoodward.headhunter.helpers.DropHelper;
 import com.scottwoodward.headhunter.helpers.HeadType;
 import com.scottwoodward.headhunter.helpers.WorldHelper;
 
 public class EntityDeathListener implements Listener {
 
 	/**
 	 * This is the action executed when an entity dies. If the player was killed
 	 * by another player, the victim will drop a copy of his head.
 	 * 
 	 * @param event
 	 *            is the triggering event, used to get details on killed player
 	 */
 	@EventHandler
 	public void onMobDeath(EntityDeathEvent event) {
 		if (event.getEntity().getKiller() instanceof Player) {
 			World world = event.getEntity().getWorld();
 			if (WorldHelper.isCurrentWorldDisabled(ConfigHelper.getWorlds(), world.getName()) == false) {
 				Location loc = event.getEntity().getLocation();
 				if (event.getEntity() instanceof Player) {
 					if(!(((Player) event.getEntity()).hasPermission("headhunter.noDrop")))
 						DropHelper.drop(HeadType.HUMAN, loc, ((Player) event.getEntity()).getName(), world, event.getEntity().getKiller());
				} else if (event.getEntity() instanceof Zombie) {
 					DropHelper.drop(HeadType.ZOMBIE, loc, null, world, event.getEntity().getKiller());
 				} else if (event.getEntity() instanceof Creeper) {
 					DropHelper.drop(HeadType.CREEPER, loc, null, world, event.getEntity().getKiller());
 				} else if (event.getEntity() instanceof Skeleton) {
 					if (((Skeleton) event.getEntity()).getSkeletonType() == SkeletonType.WITHER) {
 						DropHelper.drop(HeadType.WITHERSKELETON, loc, null, world, event.getEntity().getKiller());
 					} else {
 						DropHelper.drop(HeadType.SKELETON, loc, null, world, event.getEntity().getKiller());
 					}
 				} else if(event.getEntity() instanceof Blaze) {
                     DropHelper.drop(HeadType.HUMAN, loc, "MHF_Blaze", world, event.getEntity().getKiller());
                 } else if(event.getEntity() instanceof CaveSpider) {
                     DropHelper.drop(HeadType.HUMAN, loc, "MHF_CaveSpider", world, event.getEntity().getKiller());
                 } else if(event.getEntity() instanceof Chicken) {
                     DropHelper.drop(HeadType.HUMAN, loc, "MHF_Chicken", world, event.getEntity().getKiller());
                } else if(event.getEntity() instanceof Cow) {
                     DropHelper.drop(HeadType.HUMAN, loc, "MHF_Cow", world, event.getEntity().getKiller());
                 } else if(event.getEntity() instanceof Enderman) {
                     DropHelper.drop(HeadType.HUMAN, loc, "MHF_Enderman", world, event.getEntity().getKiller());
                 } else if(event.getEntity() instanceof Ghast) {
                     DropHelper.drop(HeadType.HUMAN, loc, "MHF_Ghast", world, event.getEntity().getKiller());
                 } else if(event.getEntity() instanceof MushroomCow) {
                     DropHelper.drop(HeadType.HUMAN, loc, "MHF_MushroomCow", world, event.getEntity().getKiller());
                 } else if(event.getEntity() instanceof Pig) {
                     DropHelper.drop(HeadType.HUMAN, loc, "MHF_Pig", world, event.getEntity().getKiller());
                 } else if(event.getEntity() instanceof PigZombie) {
                     DropHelper.drop(HeadType.HUMAN, loc, "MHF_PigZombie", world, event.getEntity().getKiller());
                 } else if(event.getEntity() instanceof Sheep) {
                     DropHelper.drop(HeadType.HUMAN, loc, "MHF_Sheep", world, event.getEntity().getKiller());
                 } else if(event.getEntity() instanceof Spider) {
                     DropHelper.drop(HeadType.HUMAN, loc, "MHF_Spider", world, event.getEntity().getKiller());
                 } else if(event.getEntity() instanceof Squid) {
                     DropHelper.drop(HeadType.HUMAN, loc, "MHF_Squid", world, event.getEntity().getKiller());
                 } else if(event.getEntity() instanceof Villager) {
                     DropHelper.drop(HeadType.HUMAN, loc, "MHF_Villager", world, event.getEntity().getKiller());
                 } else if(event.getEntity() instanceof Golem) {
                     DropHelper.drop(HeadType.HUMAN, loc, "MHF_Golem", world, event.getEntity().getKiller());
                 } else if(event.getEntity() instanceof MagmaCube) {
                     DropHelper.drop(HeadType.HUMAN, loc, "MHF_LavaSlime", world, event.getEntity().getKiller());
                 } else if(event.getEntity() instanceof Slime) {
                     DropHelper.drop(HeadType.HUMAN, loc, "MHF_Slime", world, event.getEntity().getKiller());
                 } 
 			}
 		}
 	}
 
 }
