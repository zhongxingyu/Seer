 /*
  * Copyright (c) 2010, Soar Technology, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * * Redistributions of source code must retain the above copyright notice, this
  *   list of conditions and the following disclaimer.
  * 
  * * Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the
  *   documentation and/or other materials provided with the distribution.
  * 
  * * Neither the name of Soar Technology, Inc. nor the names of its contributors
  *   may be used to endorse or promote products derived from this software
  *   without the specific prior written permission of Soar Technology, Inc.
  * 
  * THIS SOFTWARE IS PROVIDED BY SOAR TECHNOLOGY, INC. AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL SOAR TECHNOLOGY, INC. OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
  * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * Created on Jul 12, 2007
  */
 package com.soartech.simjr.sim.entities;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import junit.framework.TestCase;
 
 import com.soartech.simjr.adaptables.Adaptables;
 import com.soartech.simjr.sim.EntityPrototypes;
import com.soartech.simjr.weapons.Weapon;
 import com.soartech.simjr.weapons.WeaponPlatform;
 
 public class MissileTest extends TestCase
 {
 
     public void testUniqueNameIsGenerated()
     {
         Helicopter h = new Helicopter("helicopter", EntityPrototypes.NULL);
         
         WeaponPlatform weapons = Adaptables.adapt(h, WeaponPlatform.class);
         assertNotNull(weapons);
        Weapon weapon = weapons.getWeapon("hellfire");
         Vehicle target = new Vehicle("target", EntityPrototypes.NULL);
         
         Set<String> names = new HashSet<String>();
         for(int i = 0; i < 100; ++i)
         {
             Missile m = new Missile(weapon, target, EntityPrototypes.NULL);
             String name = m.getName();
             assertTrue(!names.contains(name));
             names.add(name);
         }
     }
 }
