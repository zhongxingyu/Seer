 /* Copyright 2013 Kevin Seiden. All rights reserved.
 
  This works is licensed under the Creative Commons Attribution-NonCommercial 3.0
 
  You are Free to:
     to Share  to copy, distribute and transmit the work
     to Remix  to adapt the work
 
  Under the following conditions:
     Attribution  You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
     Non-commercial  You may not use this work for commercial purposes.
 
  With the understanding that:
     Waiver  Any of the above conditions can be waived if you get permission from the copyright holder.
     Public Domain  Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
     Other Rights  In no way are any of the following rights affected by the license:
         Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
         The author's moral rights;
         Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.
 
  Notice  For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
  http://creativecommons.org/licenses/by-nc/3.0/
 */
 
 package alshain01.FlagsDamage;
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.PotionSplashEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import alshain01.Flags.Flags;
 import alshain01.Flags.Flag;
 import alshain01.Flags.ModuleYML;
 import alshain01.Flags.Registrar;
 import alshain01.Flags.Director;
 import alshain01.Flags.area.Area;
 import alshain01.Flags.area.Siege;
 
 /**
  * Flags - Damage
  * Module that adds damage flags to the plug-in Flags.
  * 
  * @author Alshain01
  */
 public class FlagsDamage extends JavaPlugin {
 	/**
 	 * Called when this module is enabled
 	 */
 	@Override
 	public void onEnable(){
 		// Connect to the data file
 		ModuleYML dataFile = new ModuleYML(this, "flags.yml");
 		
 		// Register with Flags
 		Registrar flags = Flags.instance.getRegistrar();
 		for(String f : dataFile.getModuleData().getConfigurationSection("Flag").getKeys(false)) {
 			ConfigurationSection data = dataFile.getModuleData().getConfigurationSection("Flag." + f);
 			
 			// We don't want to register flags that aren't supported.
 			// It would just muck up the help menu.
 			// Null value is assumed to support all versions.
 			String api = data.getString("MinimumAPI");  
 			if(api != null && !Flags.instance.checkAPI(api)) { continue; }
 			
 			// The description that appears when using help commands.
 			String desc = data.getString("Description");
 			
 			// Register it!  (All flags are defaulting to true in this module)
 			// Be sure to send a plug-in name or group description for the help command!
 			// It can be this.getName() or another string.
 			flags.register(f, desc, true, "Damage");
 		}
 		
 		// Load plug-in events and data
 		Bukkit.getServer().getPluginManager().registerEvents(new EntityDamageListener(), this);
 	}
 	
 	/*
 	 * The event handler for the flags we created earlier
 	 */
 	public class EntityDamageListener implements Listener {
 		@EventHandler(ignoreCancelled = true)
 		private void onEntityDamage(EntityDamageEvent e) {
 			// If the damage is not a to a player, we do nothing.
 			if(!(e.getEntity() instanceof Player)) { return; }
 			
 			if(e.getCause() == DamageCause.ENTITY_ATTACK || e.getCause() == DamageCause.ENTITY_EXPLOSION || e.getCause() == DamageCause.PROJECTILE) {
 				// Handled by subclass events EntityDamageByEntity so that we can retrieve the damager.
 				return;
 			}
 			
 			Flag flag = null;
 			Registrar flags = Flags.instance.getRegistrar();
 			
 			if (e.getCause() == DamageCause.BLOCK_EXPLOSION) {
 				flag = flags.getFlag("DamageBlockExplode");
 			} else if (e.getCause() == DamageCause.CONTACT) {
 				flag = flags.getFlag("DamageBlockContact");
 			} else if (e.getCause() == DamageCause.DROWNING) {
 				flag = flags.getFlag("DamageDrown");
 			} else if (e.getCause() == DamageCause.FALL) {
 				flag = flags.getFlag("DamageFall");
 			} else if (e.getCause() == DamageCause.FIRE) {
 				flag = flags.getFlag("DamageFire");
 			} else if (e.getCause() == DamageCause.FIRE_TICK) {
 				flag = flags.getFlag("DamageBurn");
 			} else if (e.getCause() == DamageCause.LAVA) {
 				flag = flags.getFlag("DamageLava");
 			} else if (e.getCause() == DamageCause.LIGHTNING) {
 				flag = flags.getFlag("DamageLightning");
 			} else if (e.getCause() == DamageCause.MAGIC) {
 				flag = flags.getFlag("DamageMagic");
 			} else if (e.getCause() == DamageCause.MELTING) {
 				flag = flags.getFlag("DamageMelting");
 			} else if (e.getCause() == DamageCause.POISON) {
 				flag = flags.getFlag("DamagePoison");
 			} else if (e.getCause() == DamageCause.STARVATION) {
 				flag = flags.getFlag("DamageStarve");
 			} else if (e.getCause() == DamageCause.SUFFOCATION) {
 				flag = flags.getFlag("DamageSuffocate");
 			} else if (e.getCause() == DamageCause.SUICIDE) {
 				flag = flags.getFlag("DamageSuicide");
 			} else if (e.getCause() == DamageCause.VOID) {
 				flag = flags.getFlag("DamageVoid");
 			} else if (Flags.instance.checkAPI("1.4.5")
 					&& e.getCause() == DamageCause.FALLING_BLOCK) {
 				flag = flags.getFlag("DamageBlockFall");
 			} else if(Flags.instance.checkAPI("1.4.5")
 					&& e.getCause() == DamageCause.WITHER) {
 				flag = flags.getFlag("DamageWither");
 			} else if(Flags.instance.checkAPI("1.5.2")
 					&& e.getCause() == DamageCause.THORNS) {
 				flag = flags.getFlag("DamageThorns");
 			} else {
 				flag = flags.getFlag("DamageOther");
 			}
 		
 			if (flag != null) { // Always guard this, even when it really can't happen.
 				e.setCancelled(!Director.getAreaAt(e.getEntity().getLocation()).getValue(flag, false));
 			}
 		}
 		
 		@EventHandler(ignoreCancelled = true) 
 		private void onEntityDamagedByEntity(EntityDamageByEntityEvent e){
 			// If the damage is not a to a player, we do nothing.
 			if (!(e.getEntity() instanceof Player)){ return; }
 			
 			Flag flag = null;
 			Registrar flags = Flags.instance.getRegistrar();
 			
 			Entity damager = e.getDamager();
 			if (damager instanceof Monster
 					|| (damager instanceof Projectile && ((Projectile)damager).getShooter() instanceof Monster)) {
 				flag = flags.getFlag("DamageMonster");
 			} else if (damager instanceof Player 
 					|| (damager instanceof Projectile && ((Projectile)damager).getShooter() instanceof Player)) {
 				if (!Director.inPvpCombat((Player)e.getEntity())) {
 					// Don't interfere with a battle, you can't attack and then retreat to a protected area (that's cheating)
 					// Uses GP's timer (15 second default).  Not supported by other systems.
 					Area area = Director.getAreaAt(e.getEntity().getLocation());
 					if(!(area instanceof Siege) || !((Siege)area).isUnderSiege()) {
 						// If your under siege, your on your own.  That's part of the game.
 						// Only supported by GP.
 						flag = flags.getFlag("Pvp");
 					}
 				}
 			} else {
 				flag = flags.getFlag("DamageOther"); // Not really sure how you got here, but if you did...
 			}
 			
 			if(flag != null) { // Always guard this, even when it really can't happen.
 				e.setCancelled(!Director.getAreaAt(e.getEntity().getLocation()).getValue(flag, false));
 			}
 		}
 		
 		@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 		private void onPotionSplash(PotionSplashEvent e){
 			Flag flag = Flags.instance.getRegistrar().getFlag("PotionSplash");
 			if(flag == null) { return; }
 			
 			for (LivingEntity entity : e.getAffectedEntities()) {
 				if (!(entity instanceof Player)) { continue; }
 				if (Director.getAreaAt(e.getEntity().getLocation()).getValue(flag, false)) {
 					// Essentially cancels it.
 					// Only way to cancel on a player by player basis instead of the whole effect.
 					e.setIntensity(entity, 0);
 				}
 			}
 		}
 	}
 }
