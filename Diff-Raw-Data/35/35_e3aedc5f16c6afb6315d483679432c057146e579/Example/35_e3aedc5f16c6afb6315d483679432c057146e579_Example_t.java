 package org.genericsystem.example;
 
 import org.genericsystem.annotation.Components;
 import org.genericsystem.annotation.Extends;
 import org.genericsystem.annotation.SystemGeneric;
 import org.genericsystem.annotation.value.IntValue;
 import org.genericsystem.annotation.value.StringValue;
 import org.genericsystem.core.Engine;
 
 public class Example {
 
 	@SystemGeneric
 	public static class Vehicle {
 
 	}
 
 	@SystemGeneric(SystemGeneric.CONCRETE)
 	@StringValue("myVehicle")
 	public static class MyVehicle extends Vehicle {
 
 	}
 
 	@SystemGeneric
 	@Components(Vehicle.class)
 	public static class Power {
 
 	}
 
 	@SystemGeneric(SystemGeneric.CONCRETE)
 	@Components(Vehicle.class)
 	@IntValue(123)
 	public static class V123 extends Power {
 
 	}
 
 	@SystemGeneric(SystemGeneric.CONCRETE)
 	@Components(MyVehicle.class)
 	@IntValue(136)
 	public static class V136 extends Power {
 
 	}
 
 	@SystemGeneric
 	public static class Color {
 
 	}
 
 	@SystemGeneric(SystemGeneric.CONCRETE)
 	@StringValue("red")
 	public static class Red extends Color {
 
 	}
 
 	@SystemGeneric(SystemGeneric.CONCRETE)
 	@StringValue("yellow")
 	public static class Yellow extends Color {
 
 	}
 
 	@SystemGeneric
 	@Components({ Vehicle.class, Color.class })
 	public static class VehicleColor {
 
 	}
 
 	@SystemGeneric(SystemGeneric.CONCRETE)
 	@Components({ MyVehicle.class, Red.class })
 	@StringValue("myVehicleRed")
 	public static class MyVehicleRed extends VehicleColor {
 
 	}
 
 	@SystemGeneric(SystemGeneric.CONCRETE)
 	@Components({ Vehicle.class, Yellow.class })
 	@StringValue("myVehicleYellow")
 	public static class MyVehicleYellow extends VehicleColor {
 
 	}
 
 	@SystemGeneric
 	public static class Car extends Vehicle {
 
 	}
 
 	@SystemGeneric(SystemGeneric.CONCRETE)
 	@StringValue("myCar")
 	public static class MyCar extends Car {
 
 	}
 
 	@SystemGeneric
 	public static class Time {
 
 	}
 
 	@SystemGeneric(SystemGeneric.CONCRETE)
 	@StringValue("myTime")
 	public static class MyTime extends Time {
 
 	}
 
 	@SystemGeneric
 	@Components({ Vehicle.class, Color.class, Time.class })
 	public static class VehicleColorTime {
 
 	}
 
 	@SystemGeneric(SystemGeneric.CONCRETE)
 	@Components({ MyVehicle.class, Red.class, MyTime.class })
 	@StringValue("myVehicleColorTime")
 	public static class MyVehicleColorTime extends VehicleColorTime {
 
 	}
 
 	@SystemGeneric
 	public static class Human {
 
 	}
 
 	@SystemGeneric(SystemGeneric.CONCRETE)
 	@StringValue("Nicolas")
 	public static class Nicolas extends Human {
 
 	}
 
 	@SystemGeneric(SystemGeneric.CONCRETE)
 	@StringValue("Michael")
 	public static class Michael extends Human {
 
 	}
 
 	@SystemGeneric(SystemGeneric.CONCRETE)
 	@StringValue("Quentin")
 	public static class Quentin extends Human {
 
 	}
 
 	@SystemGeneric
 	@Components({ Human.class, Human.class })
 	public static class IsTallerOrEqualThan {
 
 	}
 
 	@SystemGeneric(SystemGeneric.CONCRETE)
 	@Components({ Nicolas.class, Michael.class })
 	@StringValue("nicolasIsTallerOrEqualThanMichael")
 	public static class NicolasIsTallerOrEqualThanMichael extends IsTallerOrEqualThan {
 
 	}
 
	// @SystemGeneric(SystemGeneric.CONCRETE)
	// @Components({ Nicolas.class, Nicolas.class })
	// @StringValue("nicolasIsTallerOrEqualThanNicolas")
	// public static class NicolasIsTallerOrEqualThanNicolas extends IsTallerOrEqualThan {
	//
	// }
 
 	@SystemGeneric
 	@Components({ Human.class, Human.class })
 	public static class IsBrotherOf {
 
 	}
 
 	@SystemGeneric(SystemGeneric.CONCRETE)
 	@Components({ Quentin.class, Michael.class })
	@StringValue("quentinIsBrotherOfMichael")
	public static class QuentinIsBrotherOfMichael extends IsBrotherOf {
 
 	}
 
 	@SystemGeneric
 	@Components({ Human.class, Human.class })
 	public static class IsBossOf {
 
 	}
 
 	@SystemGeneric(SystemGeneric.CONCRETE)
 	@Components({ Nicolas.class, Michael.class })
 	@StringValue("nicolasIsBossOfMichael")
 	public static class NicolasIsBossOfMichael extends IsBossOf {
 
 	}
 
 	@SystemGeneric
 	@Extends(value = Engine.class, others = { Human.class, Vehicle.class })
 	public static class Transformer {
 
 	}
 
 	@SystemGeneric(SystemGeneric.CONCRETE)
 	@StringValue("myTransformer")
 	public static class MyTransformer extends Transformer {
 
 	}
 }
