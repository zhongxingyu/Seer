 package se.raek.ahsa.runtime;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import se.raek.ahsa.ast.ValueLocation;
 import se.raek.ahsa.ast.VariableLocation;
 
 public final class Store {
 	
 	private final Map<ValueLocation, Value> vals;
 	private final Map<VariableLocation, Value> vars;
 	private final Store parent;
 	
 	public Store() {
 		this.vals = new HashMap<ValueLocation, Value>();
 		this.vars = new HashMap<VariableLocation, Value>();
 		this.parent = null;
 	}
 	
 	public Store(Store parent) {
 		this.vals = new HashMap<ValueLocation, Value>();
 		this.vars = new HashMap<VariableLocation, Value>();
 		this.parent = parent;
 	}
 	
 	@Override
 	public String toString() {
 		return "Store(" + vals + ", " + vars + ", " + parent + ")";
 	}
 	
 	public static class ValueNotFoundException extends RuntimeException {
 		
 		private static final long serialVersionUID = -1588984589012825978L;
 
 		public ValueNotFoundException(ValueLocation val) {
			super("value not found in extended scope: " + val.label);
 		}
 		
 	}
 	
 	public static class ValueAlreadyDefinedException extends RuntimeException {
 		
 		private static final long serialVersionUID = -1588984589012825978L;
 
 		public ValueAlreadyDefinedException(ValueLocation val) {
			super("value already defined in local scope: " + val.label);
 		}
 		
 	}
 	
 	public static class VariableNotFoundException extends RuntimeException {
 		
 		private static final long serialVersionUID = -5315071488234846789L;
 
 		public VariableNotFoundException(VariableLocation var) {
			super("variable not found in local scope: " + var.label);
 		}
 		
 	}
 	
 	public Value lookupValue(ValueLocation val) {
 		if (val == null) throw new NullPointerException();
 		Store sto = this;
 		while (sto != null) {
 			if (sto.vals.containsKey(val)) {
 				return sto.vals.get(val);
 			} else {
 				sto = sto.parent;
 			}
 		}
 		throw new ValueNotFoundException(val);
 	}
 	
 	public void defineValue(ValueLocation val, Value v) {
 		if (val == null || v == null) throw new NullPointerException();
 		if (vals.containsKey(val)) {
 			throw new ValueAlreadyDefinedException(val);
 		} else {
 			vals.put(val, v);
 		}
 	}
 	
 	public Value lookupVariable(VariableLocation var) {
 		if (var == null) throw new NullPointerException();
 		if (vars.containsKey(var)) {
 			return vars.get(var);
 		} else {
 			throw new VariableNotFoundException(var);
 		}
 	}
 	
 	public void assignVariable(VariableLocation var, Value v) {
 		if (var == null || v == null) throw new NullPointerException();
 		vars.put(var, v);
 	}
 
 }
