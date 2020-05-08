 package org.genericsystem.impl;
 
 import java.util.Objects;
 import org.genericsystem.core.Cache;
 import org.genericsystem.core.Generic;
 import org.genericsystem.core.GenericImpl;
 import org.genericsystem.core.GenericSystem;
 import org.genericsystem.generic.Attribute;
 import org.genericsystem.generic.Relation;
 import org.genericsystem.generic.Type;
 import org.testng.annotations.Test;
 
 @Test
 public class GetSubTypeTest extends AbstractTest {
 
 	// Type
 
 	public void testGetSubTypeSimpleHierarchyType() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.newType("Vehicle");
 		Type car = vehicle.newSubType("Car");
 		Generic subType = vehicle.getSubType("Car");
 		assert Objects.equals(car, subType);
 	}
 
 	public void testGetSubTypeSimpleHierarchyTypeKO() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.newType("Vehicle");
 		Type car = vehicle.newSubType("Car");
		Generic myCar = car.newInstance("myCar");
 
		assert car.getInstance("myCar").equals(myCar);
		assert !vehicle.getInstances().contains(myCar) : vehicle.getInstances();
		assert vehicle.getAllInstances().contains(myCar) : vehicle.getAllInstances();
 
 		assert cache.getEngine().getSubType("Car").equals(car) : cache.getEngine().getSubType("Car");
 		assert cache.getEngine().getSubTypes().contains(car) : cache.getEngine().getSubTypes();
 	}
 
 	public void testGetSubTypeDoubleHierarchyType() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type human = cache.newType("Human");
 		Type vehicle = cache.newType("Vehicle");
 		vehicle.newSubType("Car");
 		assert human.getSubType("Car") == null;
 	}
 
 	public void testGetSubTypeNonExistingType() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.newType("Vehicle");
 		assert vehicle.getSubType("Alien") == null;
 	}
 
 	public void testGetSubTypeDiamondInheritingType() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type plane = cache.newType("Plane");
 		Type car = cache.newType("Car");
 		cache.newSubType("FlyingCar", car, plane);
 		Generic subTypeFromPlane = plane.getSubType("FlyingCar");
 		Generic subTypeFromCar = car.getSubType("FlyingCar");
 		assert Objects.equals(subTypeFromPlane, subTypeFromCar);
 	}
 
 	// Attribute
 
 	public void testGetSubTypeSimpleHierarchyAttributeOfAttribute() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car = cache.newType("Car");
 		Attribute power = car.setAttribute("Power");
 		power.setAttribute("Unit");
 		assert power.getSubType("Unit") == null;
 	}
 
 	public void testGetSubTypeSimpleHierarchyAttribute() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car = cache.newType("Car");
 		Attribute power = car.setAttribute("Power");
 		Attribute specializedPower = ((GenericImpl) car).setSubAttribute(power, "SpecializedPower");
 		Generic subAttribute = power.getSubType("SpecializedPower");
 		assert Objects.equals(subAttribute, specializedPower);
 	}
 
 	public void testGetSubTypeDoubleHierarchyAttributeOfAttribute() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car = cache.newType("Car");
 		Attribute power = car.setAttribute("Power");
 		power.setAttribute("Unit");
 		Attribute wheel = car.setAttribute("Wheel");
 		assert wheel.getSubType("Unit") == null;
 	}
 
 	public void testGetSubTypeDoubleHierarchyAttribute() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car = cache.newType("Car");
 		Attribute power = car.setAttribute("Power");
 		((GenericImpl) car).setSubAttribute(power, "SpecializedPower");
 		Type human = cache.newType("Human");
 		Attribute name = human.setAttribute("Name");
 		assert name.getSubType("SpecializedPower") == null;
 	}
 
 	public void testGetSubTypeNonExistingAttribute() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.newType("Vehicle");
 		vehicle.setAttribute("Power");
 		assert vehicle.getSubType("Alien") == null;
 	}
 
 	public void testGetSubTypeDiamondInheritingAttribute() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car = cache.newType("Car");
 		Attribute power = car.setAttribute("Power");
 		Attribute wheels = car.setAttribute("Wheels");
 		cache.newSubType("WheelsPower", power, wheels);
 		Generic subTypeFromPower = power.getSubType("WheelsPower");
 		Generic subTypeFromWheels = wheels.getSubType("WheelsPower");
 		assert Objects.equals(subTypeFromPower, subTypeFromWheels);
 	}
 
 	// Relation
 
 	public void testGetSubTypeSimpleHierarchyRelation() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car = cache.newType("Car");
 		Type color = cache.newType("Color");
 		Relation carColor = car.setRelation("CarColor", color);
 		Type electricCar = car.newSubType("ElectricCar");
 		Type subColor = color.newSubType("SubColor");
 		Relation electricCarSubColor = ((GenericImpl) electricCar).setSubAttribute(carColor, "ElectricCarSubColor", electricCar, subColor);
 		Generic subRelation = carColor.getSubType("ElectricCarSubColor");
 		assert Objects.equals(subRelation, electricCarSubColor);
 	}
 
 	public void testGetSubTypeDoubleHierarchyRelation() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car = cache.newType("Car");
 		Type color = cache.newType("Color");
 		Relation carColor = car.setRelation("CarColor", color);
 		Type electricCar = car.newSubType("ElectricCar");
 		Type subColor = color.newSubType("SubColor");
 		((GenericImpl) electricCar).setSubAttribute(carColor, "ElectricCarSubColor", subColor);
 		Type plane = cache.newType("Plane");
 		Type human = cache.newType("Human");
 		Relation pilot = plane.setRelation("Pilot", human);
 		assert pilot.getSubType("ElectricCarSubColor") == null;
 	}
 
 	public void testGetSubTypeDoubleHierarchyRelationWithOtherTargetType() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.newType("Vehicle");
 		Type color = cache.newType("Color");
 		Relation vehicleColor = vehicle.setRelation("VehicleColor", color);
 		Type car = vehicle.newSubType("Car");
 		try {
 			((GenericImpl) car).setSubAttribute(vehicleColor, "CarOutsideColor", cache.newType("Percent"));
 		} catch (IllegalStateException ignore) {}
 	}
 
 	public void testGetSubTypeNonExistingRelation() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car = cache.newType("Car");
 		Type color = cache.newType("Color");
 		car.setRelation("CarColor", color);
 		assert car.getSubType("Pilot") == null;
 	}
 
 	public void testGetSubTypeDiamondInheritingRelation() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car = cache.newType("Car");
 		Type color = cache.newType("Color");
 		Relation carColor = car.setRelation("CarColor", color);
 		Type plane = cache.newType("Plane");
 		Type human = cache.newType("Human");
 		Relation pilot = plane.setRelation("Pilot", human);
 		cache.newSubType("CarColorPilot", carColor, pilot);
 		Generic subTypeFromCarColor = carColor.getSubType("CarColorPilot");
 		Generic subTypeFromPilot = pilot.getSubType("CarColorPilot");
 		assert Objects.equals(subTypeFromCarColor, subTypeFromPilot);
 	}
 }
