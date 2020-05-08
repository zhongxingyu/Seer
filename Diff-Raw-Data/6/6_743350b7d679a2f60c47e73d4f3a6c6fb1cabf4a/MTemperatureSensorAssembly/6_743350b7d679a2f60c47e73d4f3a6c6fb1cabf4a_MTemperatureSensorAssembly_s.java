 package de.altimos.mdsd.majordomo.simulator.assemblies;
 
 public class MTemperatureSensorAssembly extends MFloatSensorAssembly {
 
 	public MTemperatureSensorAssembly(String name) {
 		super(name);
 		
		getSpinnerModel().setMinimum(-20);
		getSpinnerModel().setMaximum(80);
		getSpinnerModel().setStepSize(1);
 	}
 
 }
