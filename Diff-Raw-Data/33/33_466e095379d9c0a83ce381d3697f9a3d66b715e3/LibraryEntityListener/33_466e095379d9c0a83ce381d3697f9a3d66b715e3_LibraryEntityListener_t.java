 package com.github.customentitylibrary.listeners;
 
 import com.github.customentitylibrary.CustomEntityLibrary;
 import com.github.customentitylibrary.CustomEntitySpawnEvent;
 import com.github.customentitylibrary.entities.CustomEntityWrapper;
 
 import com.github.customentitylibrary.utils.NMS;
 import net.minecraft.server.v1_6_R2.*;
 
 import org.bukkit.Bukkit;
 import org.bukkit.craftbukkit.v1_6_R2.util.UnsafeList;
 import org.bukkit.entity.*;
 import org.bukkit.entity.Entity;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.Vector;
 
 import java.lang.reflect.Field;
 
 public class LibraryEntityListener implements Listener
 {
 	public LibraryEntityListener()
 	{
 	}
 	
 	public void registerEvents(PluginManager pm, JavaPlugin plugin)
 	{
 		pm.registerEvents(this, plugin);
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void cancelIfImmune(EntityDamageEvent event)
 	{
 		if(event.isCancelled())
 			return;
 		Entity ent = event.getEntity();
 		if(CustomEntityWrapper.instanceOf(ent))
 		{
 			CustomEntityWrapper customEnt = CustomEntityWrapper.getCustomEntity(ent);
 			if(customEnt.isImmune() || customEnt.getType().getImmunities().contains(event.getCause()))
 				event.setCancelled(true);
 		}
 	}
 	
 	//Set priority to monitor because annoying plugins keep innapropriately using the HIGHEST priority, making them
 	//incompatible.
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void calculateHealth(EntityDamageEvent event)
 	{
 		if(event.isCancelled())
 			return;
 		Entity ent = event.getEntity();
 		if(CustomEntityWrapper.instanceOf(ent))
 		{
 			CustomEntityWrapper customEnt = CustomEntityWrapper.getCustomEntity(ent);
 			//Subtract healthX, set the damage as 1, and set the ents normal health back up to 20 unless healthX is below 0
 			//in which case kill the ent
 			double health = customEnt.getHealth();
 			customEnt.setHealth(health - event.getDamage());
 			if(customEnt.getHealth() <= 0)
 				event.setDamage(2000d);
 			else
 			{
 				event.setDamage(1d);
 				double percentHealth = customEnt.getHealth() / customEnt.getMaxHealth();
 				LivingEntity livingEntity = (LivingEntity) ent;
 				livingEntity.setHealth((int) Math.ceil(livingEntity.getMaxHealth() * percentHealth));
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void dealDamage(EntityDamageByEntityEvent event)
 	{
 		if(event.isCancelled())
 			return;
 		Entity damager = event.getDamager();
 		if(CustomEntityWrapper.instanceOf(damager))
 		{
 			CustomEntityWrapper customEnt = CustomEntityWrapper.getCustomEntity(damager);
 			LivingEntity ent = (LivingEntity) event.getEntity();
 			double health = ent.getHealth() - customEnt.getType().getArmorPiercingDamage();
 			if(health < 0)
 				health = 0;
 			ent.setHealth(health);
 			event.setDamage(customEnt.getType().getDamage());
 		}
 		else if(damager instanceof Projectile)
 		{
 			Projectile proj = (Projectile) damager;
 			if(proj.getShooter() != null && CustomEntityWrapper.instanceOf(proj.getShooter()))
 			{
 				CustomEntityWrapper customEnt = CustomEntityWrapper.getCustomEntity(proj.getShooter());
 				LivingEntity ent = (LivingEntity) event.getEntity();
 				double health = ent.getHealth() - customEnt.getType().getArmorPiercingDamage();
 				if(health < 0)
 					health = 0;
 				ent.setHealth(health);
 				event.setDamage(customEnt.getType().getDamage());
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void dealDamageEffects(EntityDamageByEntityEvent event)
 	{
 		if(event.isCancelled())
 			return;
 		Entity ent = event.getEntity();
 		Entity damager = event.getDamager();
 		if (damager instanceof Projectile)
 		{
 			Projectile pj = (Projectile) event.getDamager();
 			damager = pj.getShooter();
 		}
 		if(CustomEntityWrapper.instanceOf(ent))
 		{
 			if(damager instanceof Player)
 				CustomEntityWrapper.getCustomEntity(ent).addAttack((Player) damager, event.getDamage());
 		}
 		else if(ent instanceof LivingEntity && damager instanceof LivingEntity)
 		{
 			if(CustomEntityWrapper.instanceOf(damager))
 			{
 				CustomEntityWrapper customEnt = CustomEntityWrapper.getCustomEntity(damager);
 				customEnt.getType().dealEffects((LivingEntity) ent, (LivingEntity) damager);
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void handleEntitySpawn(CustomEntitySpawnEvent event)
 	{
 		CustomEntityWrapper entity = event.getEntity();
         LivingEntity bukkitEntity = (LivingEntity) entity.getEntity().getBukkitEntity();
 		((LivingEntity) entity.getEntity().getBukkitEntity()).setRemoveWhenFarAway(false);
 		if(entity.getType().isVillager() && entity.getEntity() instanceof EntityZombie)
 			((EntityZombie) entity.getEntity()).setVillager(true);
 		if(entity.getType().isWither() && entity.getEntity() instanceof EntitySkeleton)
 			((EntitySkeleton) entity.getEntity()).setSkeletonType(1);
         if(entity.getType().isBaby())
         {
             //The Ageable interface is not implemented by all entities with a SetBaby method, so reflection is the only way to do this
             //that I'm aware of.
             try
             {
                 bukkitEntity.getClass().getMethod("setBaby").invoke(bukkitEntity);
             } catch(Exception e)
             {
                 try
                 {
                     bukkitEntity.getClass().getMethod("setBaby", boolean.class).invoke(bukkitEntity, true);
                 } catch(Exception e2)
                 {
 					e2.printStackTrace();
                 }
             }
         }
 		if(entity.getType().ignoreInvisible())
 		{
 			UnsafeList targetSelectors;
 			try
 			{
 				Field targetSelectorField = EntityInsentient.class.getDeclaredField("targetSelector");
 
 				targetSelectorField.setAccessible(true);
 
 				PathfinderGoalSelector targetSelector = (PathfinderGoalSelector) targetSelectorField.get(entity.getEntity());
 
 				Field gsa = PathfinderGoalSelector.class.getDeclaredField(NMS.PATHFINDER_LIST);
 				gsa.setAccessible(true);
 
 				targetSelectors = (UnsafeList) gsa.get(targetSelector);
 
 				for(int i = 0; i < targetSelectors.size(); i++)
 				{
 					Field goalField = targetSelectors.get(i).getClass().getDeclaredField("a");
 					goalField.setAccessible(true);
 					PathfinderGoal a = (PathfinderGoal)goalField.get(targetSelectors.get(i));
 					if(a instanceof PathfinderGoalHurtByTarget)
 					{
 						targetSelectors.remove(i);
 					}
 				}
 			} catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 
	@EventHandler(priority = EventPriority.MONITOR)
	public void removeDeadEntity(EntityDeathEvent event)
	{
		LivingEntity entity = event.getEntity();
		if(CustomEntityWrapper.instanceOf(entity))
		{
			EntityInsentient nmsEntity = CustomEntityWrapper.getCustomEntity(entity).getEntity();
			CustomEntityWrapper.getCustomEntities().remove(nmsEntity);
		}
	}

 	@EventHandler(priority = EventPriority.NORMAL)
 	public void reduceKnockback(final EntityDamageByEntityEvent event)
 	{
 		if(event.isCancelled())
 			return;
 		final Entity entity = event.getEntity();
 		if(CustomEntityWrapper.instanceOf(entity))
 		{
 			final CustomEntityWrapper customEntity = CustomEntityWrapper.getCustomEntity(entity);
 			final Vector prevVelocity = entity.getVelocity();
 			if(customEntity.getType().disableKnockbackToSelf())
 			{
 				Bukkit.getScheduler().scheduleSyncDelayedTask(CustomEntityLibrary.plugin, new Runnable()
 				{
 					@Override
 					public void run()
 					{
 						entity.setVelocity(prevVelocity);
 					}
 				},1L);
 			}
 		}
 	}
 }
