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
 package org.mgenterprises.java.bukkit.gmcfps.Core.Weapons;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.inventory.ItemStack;
 import org.mgenterprises.java.bukkit.gmcfps.Core.InternalEvents.Events.WeaponFiredEvent;
 
 /**
  *
  * @author Manuel Gauto
  */
 public abstract class ProjectileWeapon extends Weapon {
 
     private Material ammoMaterial;
     private EntityType projectileType;
     private int fireDelay;
 
     public ProjectileWeapon(WeaponManager wm, String name, Material m, Material ammoType, EntityType projectileType, int fireDelay) {
         super(wm, name, m);
         this.ammoMaterial = ammoType;
         this.fireDelay = fireDelay;
         this.projectileType = projectileType;
     }
 
     public Material getAmmunitionType() {
         return this.ammoMaterial;
     }
 
     public abstract void onWeaponFire(Player p);
 
     @Override
     public void onWeaponRightClick(WeaponFiredEvent event) {
         //System.out.println(event);
         if(super.getWeaponManager().waiting.contains(event.getPlayer().getName())){
             return;
         }
         boolean hasAmmoLeft = event.getPlayer().getInventory().contains(ammoMaterial);
         if (hasAmmoLeft) {
            int slot = event.getPlayer().getInventory().first(ammoUsed);
             if(slot > -1){
                 ItemStack itemStack = event.getPlayer().getInventory().getItem(slot);
                 itemStack.setAmount(itemStack.getAmount()-1);
                event.getPlayer().getInventory().setItem(itemStack);
                 onWeaponFire(event.getPlayer());
                 scheduleDelay(event.getPlayer());
             }
         }
     }
 
     @Override
     public boolean isThrowable() {
         return false;
     }
 
     @Override
     public boolean isProjectile() {
         return true;
     }
 
     public EntityType getProjectileType(){
         return this.projectileType;
     }
     
     public abstract void onProjectileHit(EntityDamageByEntityEvent event);
     
     private void scheduleDelay(Player p) {
         Bukkit.getScheduler().scheduleSyncDelayedTask(super.getWeaponManager().getFPSCore().getPluginReference(), new DelayRunnable(super.getWeaponManager().getFPSCore(), p), this.fireDelay);
     }
 }
