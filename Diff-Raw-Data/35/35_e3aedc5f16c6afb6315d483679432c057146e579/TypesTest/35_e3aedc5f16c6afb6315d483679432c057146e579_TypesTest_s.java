 package org.genericsystem.test;
 
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.genericsystem.core.Cache;
 import org.genericsystem.core.Generic;
 import org.genericsystem.core.Structural;
 import org.genericsystem.core.StructuralImpl;
 import org.genericsystem.example.Example.MyVehicle;
 import org.genericsystem.example.Example.Vehicle;
 import org.genericsystem.generic.Link;
 import org.genericsystem.generic.Relation;
 import org.genericsystem.generic.Type;
 import org.testng.annotations.Test;
 
 @Test
 public class TypesTest extends AbstractTest {
 
 	@Inject
 	private Cache cache;
 
 	public void testExample() {
 		Generic vehicle = cache.find(Vehicle.class);
 		Generic myVehicle = cache.find(MyVehicle.class);
 		assert myVehicle.inheritsFrom(vehicle);
 		assert vehicle.getInheritings().contains(myVehicle);
 	}
 
 	public void testGetAttributes() {
 		Type human = cache.newType("Human");
 		Generic michael = human.newInstance("Michael");
 		Generic quentin = human.newInstance("Quentin");
 		Relation isBrotherOf = human.setRelation("isBrotherOf", human);
 		// isBrotherOf.enableMultiDirectional();
 		quentin.bind(isBrotherOf, michael);
 
 		List<Structural> structurals = quentin.getStructurals();
 		assert structurals.size() >= 2 : structurals.size();
 		assert structurals.contains(new StructuralImpl(isBrotherOf, 0));
 
 		List<Structural> structurals2 = michael.getStructurals();
 		assert structurals2.size() >= 2 : structurals2.size();
 		assert structurals2.contains(new StructuralImpl(isBrotherOf, 0));
 	}
 
 	public void testGetOtherTargets() {
 		Type human = cache.newType("Human");
 		Generic michael = human.newInstance("Michael");
 		Generic quentin = human.newInstance("Quentin");
 		Relation isBrotherOf = human.setRelation("isBrotherOf", human);
 		// isBrotherOf.enableMultiDirectional();
 		Link link = quentin.bind(isBrotherOf, michael);
 
		List<Generic> targets = quentin.getOtherTargets(link);
		assert targets.size() == 1 : targets.size();
		assert targets.contains(michael);
		assert !targets.contains(quentin);
 	}
 }
