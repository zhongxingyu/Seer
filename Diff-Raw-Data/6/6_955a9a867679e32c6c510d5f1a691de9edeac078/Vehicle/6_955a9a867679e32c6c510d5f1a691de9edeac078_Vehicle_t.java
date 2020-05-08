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
  * Created on May 31, 2007
  */
 package com.soartech.simjr.sim.entities;
 
 import java.util.Map;
 
 import com.soartech.simjr.sensors.DefaultSensorPlatform;
 import com.soartech.simjr.sim.EntityConstants;
 import com.soartech.simjr.sim.EntityPrototype;
 import com.soartech.simjr.sim.EntityTools;
 import com.soartech.simjr.weapons.DefaultWeaponPlatform;
 
 /**
  * @author ray
  */
 public class Vehicle extends AbstractEntity
 {
     private FuelModel fuelModel;
     
     /**
      * @param name
      */
     public Vehicle(String name, EntityPrototype prototype)
     {
         super(name, prototype);
         
         setProperty(EntityConstants.PROPERTY_CATEGORY, EntityConstants.CATEGORY_VEHICLES);
         setProperty(EntityConstants.PROPERTY_CALLSIGN, name);
         
         addCapability(new DefaultWeaponPlatform());
         addCapability(new DefaultSensorPlatform());
         
         addCapability(new EntityContainerCapability());
         addCapability(fuelModel = new FuelModel());
         addCapability(new DisableRadarWhenDestroyed());
     }
         
     /**
      * Removes the existing fuelModel capability and adds newFuelModel
      * 
      * @param newFuelModel The new FuelModel.
      */
    public void replaceFuelModel(FuelModel newFuelModel)
     {
         removeCapability(fuelModel);
         addCapability(newFuelModel);
        fuelModel = newFuelModel;
     }
     
     
     /* (non-Javadoc)
      * @see com.soartech.simjr.sim.entities.AbstractEntity#canUpdatePosition()
      */
     @Override
     protected boolean canUpdatePosition()
     {
      
         return super.canUpdatePosition() && !fuelModel.isEmpty();
     }
 
     /* (non-Javadoc)
      * @see com.soartech.simjr.sim.entities.AbstractEntity#updateProperties(java.util.Map)
      */
     @Override
     protected void updateProperties(Map<String, Object> properties)
     {
         super.updateProperties(properties);
         
         properties.put(EntityConstants.PROPERTY_HEADING, Math.toDegrees(EntityTools.getHeading(this)));
         properties.put(EntityConstants.PROPERTY_BEARING, Math.toDegrees(EntityTools.getBearing(this)));
         properties.put(EntityConstants.PROPERTY_SPEED, getVelocity().length());
         properties.put(EntityConstants.PROPERTY_AGL, EntityTools.getAboveGroundLevel(this));
     }    
 }
