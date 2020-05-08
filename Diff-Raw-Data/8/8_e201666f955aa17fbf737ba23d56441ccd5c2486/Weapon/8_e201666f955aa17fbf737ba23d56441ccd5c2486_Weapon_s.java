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
 
 import org.bukkit.Material;
 import org.mgenterprises.java.bukkit.gmcfps.Core.InternalEvents.Events.WeaponFiredEvent;
 
 /**
  *
  * @author Manuel Gauto
  */
 public abstract class Weapon {
     private String name;
     private Material material;
     private WeaponManager wm;
     
     public Weapon(WeaponManager wm, String weaponName, Material material){
         this.name = weaponName;
         this.material = material;
     }
     
     public String getName(){
         return this.name;
     }
     
     public Material getMaterial(){
         return this.material;
     }
     
     public WeaponManager getWeaponManager(){
         return this.wm;
     }
     
     public abstract void onWeaponRightClick(WeaponFiredEvent event);
     
     public abstract boolean isThrowable();
     
     public abstract boolean isProjectile();
 }
