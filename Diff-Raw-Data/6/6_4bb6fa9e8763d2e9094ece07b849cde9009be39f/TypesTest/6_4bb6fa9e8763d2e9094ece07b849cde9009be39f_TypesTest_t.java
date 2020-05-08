 package org.genericsystem.test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.genericsystem.core.Cache;
 import org.genericsystem.core.Generic;
 import org.genericsystem.example.Example.MyVehicle;
 import org.genericsystem.example.Example.Vehicle;
 import org.genericsystem.generic.Attribute;
 import org.genericsystem.generic.Holder;
 import org.genericsystem.generic.Link;
 import org.genericsystem.generic.Relation;
 import org.genericsystem.generic.Type;
 import org.genericsystem.myadmin.beans.GuiGenericsTreeBean;
 import org.genericsystem.myadmin.beans.Structural;
 import org.genericsystem.myadmin.beans.StructuralImpl;
 import org.testng.annotations.Test;
 
 @Test
 public class TypesTest extends AbstractTest {
 
 	@Inject
 	private Cache cache;
 
 	@Inject
 	private GuiGenericsTreeBean genericTreeBean;
 
 	public void testExample() {
 		Generic vehicle = cache.find(Vehicle.class);
 		Generic myVehicle = cache.find(MyVehicle.class);
 		assert myVehicle.inheritsFrom(vehicle);
 		assert vehicle.getInheritings().contains(myVehicle);
 	}
 
 	public void testGetAttributes() {
 		Type human = cache.setType("Human");
 		Generic michael = human.newInstance("Michael");
 		Generic quentin = human.newInstance("Quentin");
 		Relation isBrotherOf = human.setRelation("isBrotherOf", human);
 		// isBrotherOf.enableMultiDirectional();
 		quentin.bind(isBrotherOf, michael);
 
 		List<Structural> structurals = new ArrayList<>();
 		for (Attribute attribute : ((Type) quentin).getAttributes()) {
 			structurals.add(new StructuralImpl(attribute, 0));
 		}
 		assert structurals.size() >= 2 : structurals.size();
 		assert structurals.contains(new StructuralImpl(isBrotherOf, 0));
 
 		List<Structural> structurals2 = new ArrayList<>();
 		for (Attribute attribute : ((Type) quentin).getAttributes()) {
 			structurals2.add(new StructuralImpl(attribute, 0));
 		}
 		assert structurals2.size() >= 2 : structurals2.size();
 		assert structurals2.contains(new StructuralImpl(isBrotherOf, 0));
 	}
 
 	public void testGetOtherTargets() {
 		Type human = cache.setType("Human");
 		Generic michael = human.newInstance("Michael");
 		Generic quentin = human.newInstance("Quentin");
 		Relation isBrotherOf = human.setRelation("isBrotherOf", human);
 		// isBrotherOf.enableMultiDirectional();
 		Link link = quentin.bind(isBrotherOf, michael);
 
 		List<Generic> targetsFromQuentin = quentin.getOtherTargets(link);
 		assert targetsFromQuentin.size() == 1 : targetsFromQuentin.size();
 		assert targetsFromQuentin.contains(michael);
 		assert !targetsFromQuentin.contains(quentin);
 
 		List<Generic> targetsFromMichael = michael.getOtherTargets(link);
 		assert targetsFromMichael.size() == 1 : targetsFromMichael.size();
 		assert targetsFromMichael.contains(quentin);
 		assert !targetsFromMichael.contains(michael);
 	}
 
 	public void testContraint() {
 		Type vehicle = cache.setType("Vehicle");
 		Attribute vehiclePower = vehicle.setProperty("power");
 		Generic myVehicle = vehicle.newInstance("myVehicle");
 		Holder myVehicle123 = myVehicle.setValue(vehiclePower, "123");
 		myVehicle.cancel(myVehicle123);
 		assert !myVehicle123.isAlive();
 	}
 
 }
