 package ch.zhaw.simulation.math.exception;
 
 public class EmptyDerivativeException extends SimulationModelException {
	private static final long serialVersionUID = 1L;

 	public EmptyDerivativeException(Object simObject) {
 		super(simObject, "Die Ableitung " + getNameFor(simObject) + " ist leer!");
 	}
 }
