 package ch.zhaw.simulation.math.exception;
 
 public class EmptyDerivativeException extends SimulationModelException {
 	public EmptyDerivativeException(Object simObject) {
 		super(simObject, "Die Ableitung " + getNameFor(simObject) + " ist leer!");
 	}
 }
