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
 package org.mgenterprises.java.bukkit.gmcfps.Core.Weapons.Implementations;
 
import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Snowball;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.mgenterprises.java.bukkit.gmcfps.Core.Weapons.ProjectileWeapon;
 import org.mgenterprises.java.bukkit.gmcfps.Core.Weapons.WeaponManager;
 
 /**
  *
  * @author Manuel Gauto
  */
 public class BasicSniper extends ProjectileWeapon {
 
     private int velocityMulti = 20;
     public BasicSniper(WeaponManager wm) {
        super(wm, "Sniper", Material.IRON_HOE, Material.IRON_INGOT, EntityType.ARROW, 10);
     }
 
     @Override
     public void onWeaponFire(Player p) {
         Projectile projectile = p.launchProjectile(Snowball.class);
         projectile.setVelocity(projectile.getVelocity().multiply(velocityMulti));
     }
 
     @Override
     public void onProjectileHit(EntityDamageByEntityEvent event) {
        Bukkit.broadcastMessage("HIT!");
        
     }
 }
