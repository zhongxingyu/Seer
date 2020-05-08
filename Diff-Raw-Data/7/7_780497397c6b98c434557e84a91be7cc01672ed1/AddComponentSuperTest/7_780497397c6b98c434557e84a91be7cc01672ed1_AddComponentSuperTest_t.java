 package org.genericsystem.impl;
 
 import org.genericsystem.core.Cache;
 import org.genericsystem.core.GenericSystem;
 import org.genericsystem.generic.Type;
 import org.testng.annotations.Test;
 
 @Test
 public class AddComponentSuperTest extends AbstractTest {
 
 	public void addAttribute() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine();
 		Type vehicle = cache.newType("Vehicle");
 		Type power = cache.newType("power");
 		log.info("metaLevel power " + power.getMetaLevel());
 		assert vehicle.getAttribute(cache, "power") == null;
 		vehicle.addComponent(cache, 0, power);
 		assert vehicle.getAttribute(cache, "power").equals(power);
 	}
 
 	public void addRelation() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine();
 		Type vehicle = cache.newType("Vehicle");
 		Type color = cache.newType("Color");
 		Type vehicleColor = vehicle.addAttribute(cache, "VehicleColor");
 		assert vehicle.getRelation(cache, "VehicleColor") == null;
 		vehicleColor.addComponent(cache, 0, color);
		vehicle.getRelation(cache, "VehicleColor").log();
		assert vehicle.getRelation(cache, "VehicleColor") != null;
		assert vehicle.getRelation(cache, "VehicleColor").getComponent(0).equals(color);
		assert vehicle.getRelation(cache, "VehicleColor").inheritsFrom(cache.getMetaRelation());
 	}
 }
