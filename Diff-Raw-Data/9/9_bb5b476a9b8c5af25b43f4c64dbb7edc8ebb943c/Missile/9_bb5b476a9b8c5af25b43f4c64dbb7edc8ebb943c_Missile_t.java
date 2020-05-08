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
  * Created on Jun 11, 2007
  */
 package com.soartech.simjr.sim.entities;
 
 import com.soartech.math.Vector3;
 import com.soartech.simjr.sim.Entity;
 import com.soartech.simjr.sim.EntityPrototype;
 import com.soartech.simjr.weapons.Weapon;
 
 /**
  * Basic implementation of a missile that flies at a target and destroys it.
  * 
  * @author ray
  */
 public class Missile extends AbstractFlyout
 {
     /**
      * Construct a missile that hits an entity target.
      * 
      * @param weapon The associated weapon
     * @param target The target entity
      */
     public Missile(Weapon weapon, Entity target, EntityPrototype prototype)
     {
         super(weapon, target, prototype);
         
         // TODO: Flyout should be created via EntityPrototype.createEntity()
         // so caps will get picked up from prototype automagically.
         addCapability(new MilStd2525Provider());
     }
     
     /**
      * Construct a missile that hits a target position
      * 
      * @param weapon The associated weapon
      * @param staticTarget The target position
      */
     public Missile(Weapon weapon, Vector3 staticTarget, EntityPrototype prototype)
     {
         super(weapon, staticTarget, prototype);
     }
     
 }
