 /*
  * The MIT License
  *
  * Copyright 2013 Manuel Gauto.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.mgenterprises.java.bukkit.gmcfps.Core.BukkitListeners;
 
import org.bukkit.Material;
import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.mgenterprises.java.bukkit.gmcfps.Core.FPSCore;
 import org.mgenterprises.java.bukkit.gmcfps.Core.InternalEvents.Events.WeaponFiredEvent;
 import org.mgenterprises.java.bukkit.gmcfps.Core.InternalEvents.Sources.WeaponFiredSource;
 import org.mgenterprises.java.bukkit.gmcfps.Core.Weapons.Weapon;
 
 /**
  *
  * @author Manuel Gauto
  */
 public class WeaponListeners implements Listener {
 
     private FPSCore core;
 
     public WeaponListeners(FPSCore core) {
         this.core = core;
     }
 
     @EventHandler
     public void onPlayerInteractEvent(PlayerInteractEvent event) {
         if (core.getTeamManager().isParticipating(event.getPlayer())) {
             WeaponFiredSource source = core.getEventManager().getWeaponFiredSource();
             Weapon w = core.getWeaponManager().getWeaponByType(event.getPlayer().getItemInHand().getType());
             WeaponFiredEvent wfe = new WeaponFiredEvent(source, w, event.getPlayer(), event.getPlayer().getLocation());
             core.getEventManager().getWeaponFiredSource().fireEvent(wfe);
         }
     }
     
     public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event){
         if(event.getCause() == DamageCause.PROJECTILE){
             core.getWeaponManager().processProjectile(event);
         }
     }
 }
