 package ch.zhaw.simulation.math.exception;
 
 public class EmptyDensityException extends SimulationModelException {
 
 	public EmptyDensityException(Object simObject) {
 		super(simObject, "Der Dichte-Container " + getNameFor(simObject) + " hat keine Dichte zugeordnet!");
 	}
 }
