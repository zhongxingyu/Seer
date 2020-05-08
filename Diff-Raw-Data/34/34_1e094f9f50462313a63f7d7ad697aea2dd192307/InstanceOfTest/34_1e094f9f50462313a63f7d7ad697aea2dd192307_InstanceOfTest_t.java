 package org.genericsystem.impl;
 
 import java.util.Objects;
 import org.genericsystem.core.Cache;
 import org.genericsystem.core.Generic;
 import org.genericsystem.core.GenericSystem;
 import org.genericsystem.core.Statics;
 import org.genericsystem.generic.Attribute;
 import org.genericsystem.generic.Holder;
 import org.genericsystem.generic.Type;
 import org.testng.annotations.Test;
 
 @Test
 public class InstanceOfTest extends AbstractTest {
 
 	// public void testGetMetasAttributes() {
 	// Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 	// Type vehicle = cache.newType("Vehicle");
 	// Type car = vehicle.newSubType( "Car");
 	// Attribute vehiclePower = vehicle.addAttribute( "Power");
 	// Generic myVehicle = vehicle.newInstance( "myVehicle");
 	// Generic myCar = car.newInstance( "myCar");
 	// Holder myVehiclePower236 = myVehicle.setValue( vehiclePower, 236);
 	// Attribute carSubPower = car.addSubAttribute( vehiclePower, "subPower");
 	//
 	// Holder myCarPower235 = myCar.setValue( carSubPower, 235);
 	//
 	// assert myVehiclePower236.getMeta().equals(vehiclePower) : myVehiclePower236.getMeta() + "  " + myVehiclePower236.getSupers();
 	// assert myCarPower235.getMeta().equals(carSubPower) : myCarPower235.getMeta();
 	//
 	// assert ((GenericImpl) myVehicle).getMeta().equals(vehicle);
 	// assert ((GenericImpl) myCar).getMeta().equals(car);
 	// assert ((GenericImpl) car).getMeta().equals(cache.getEngine());
 	// assert ((GenericImpl) vehicle).getMeta().equals(cache.getEngine());
 	// }
 
 	// public void testGetMetasRelations() {
 	// Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 	// Type vehicle = cache.newType("Vehicle");
 	// Type car = vehicle.newSubType( "Car");
 	// Type human = cache.newType("Human");
 	// Type pilot = human.newSubType( "Pilot");
 	// Relation driving = human.addRelation( "Driving", vehicle);
 	// Relation piloting = pilot.addSubRelation( driving, "Piloting", car);
 	//
 	// Generic maurice = human.newInstance( "Maurice");
 	// Generic federer = pilot.newInstance( "Federer");
 	//
 	// Generic titine = vehicle.newInstance( "myTitine");
 	// Generic f1 = car.newInstance( "myF1");
 	//
 	// Link drives = maurice.setLink( driving, "Maurice_drives_Titine", titine);
 	// Link pilots = federer.setLink( piloting, "Federer_pilots_F1", f1);
 	//
 	// assert drives.getMeta().equals(driving) : drives.getMeta();
 	//
 	// assert pilots.getMeta().equals(piloting) : pilots.getMeta();
 	// }
 
 	public void testInstanceOf() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 
 		Type vehicle = cache.newType("Vehicle");
 		Generic myVehicle = vehicle.newInstance("myVehicle");
 		assert myVehicle.isInstanceOf(vehicle);
 
 		Type car = vehicle.newSubType("Car");
 		Generic myCar = car.newInstance("myCar");
 		assert myCar.isInstanceOf(car);
 
 		assert myCar.isInstanceOf(vehicle);
 		assert !myVehicle.isInstanceOf(car);
 
 		Attribute vehiclePower = vehicle.setProperty("Power");
 
 		Holder v90 = myVehicle.setValue(vehiclePower, 90);
 		Holder v235 = myCar.setValue(vehiclePower, 235);
 
 		assert v90.isInstanceOf(vehiclePower);
 		assert v235.isInstanceOf(vehiclePower);
 
 		cache.flush();
 
 		assert Objects.equals(myVehicle.getValue(vehiclePower), 90);
 		assert Objects.equals(myCar.getValue(vehiclePower), 235);
 
 		assert v90.equals(myVehicle.getHolder(Statics.CONCRETE, vehiclePower));
 		assert v235.equals(myCar.getHolder(Statics.CONCRETE, vehiclePower));
 
 		v90.remove();
 
 		assert myVehicle.getValue(vehiclePower) == null;
 		assert myVehicle.getHolder(Statics.CONCRETE, vehiclePower) == null;
 
 		cache.flush();
 	}
 
 	public void testMutipleInstanceOf() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type animal = cache.newType("Animal");
 		Type mammal = cache.newType("Mammal");
 
 		Type human = cache.newSubType("Human", animal, mammal);
 		Generic michael = human.newInstance("Michael");
 
 		Type canid = cache.newSubType("Canid", animal, mammal);
 		Generic milou = canid.newInstance("Milou");
 		assert michael.isInstanceOf(human);
 		assert !michael.isInstanceOf(canid);
 		assert michael.isInstanceOf(animal);
 		assert michael.isInstanceOf(mammal);
 
 		assert milou.isInstanceOf(canid);
 		assert !milou.isInstanceOf(human);
 		assert milou.isInstanceOf(animal);
 		assert milou.isInstanceOf(mammal);
 	}
 
 	public void testNewSubTypeWithJustOneSuper() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type vehicle = cache.newType("Vehicle");
 		Type car1 = cache.newSubType("Car1", vehicle);
 		Type car2 = vehicle.newSubType("Car2");
 
		assert car1.getSupers().get(0).equals(car2.getSupers().get(0));
 	}
 
 	public void testNewSubTypeWithoutSuper() {
 		Cache cache = GenericSystem.newCacheOnANewInMemoryEngine().start();
 		Type car1 = cache.newSubType("Car1");
 		assert car1.getSupers().get(0).equals(cache.getEngine());
 	}
 }
