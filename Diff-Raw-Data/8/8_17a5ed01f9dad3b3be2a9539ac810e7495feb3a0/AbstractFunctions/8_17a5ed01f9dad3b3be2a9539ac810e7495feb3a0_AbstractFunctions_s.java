 package se.raek.ahsa.runtime;
 
 import java.util.List;
 
 public class AbstractFunctions {
 	
 	private AbstractFunctions() {
 	}
 	
 	public static abstract class Function0 implements Function {
 		
 		public final Value apply(List<Value> parameters) {
 			int n = parameters.size();
 			if (n != 0) throw new Function.ArityException(0, n);
 			return invoke();
 		}
 		
 		protected abstract Value invoke();
 		
 	}
 	
 	public static abstract class Function1 implements Function {
 		
 		public final Value apply(List<Value> parameters) {
 			int n = parameters.size();
			if (n != 1) throw new Function.ArityException(0, 1);
 			return invoke(parameters.get(0));
 		}
 		
 		protected abstract Value invoke(Value v0);
 		
 	}
 	
 	public static abstract class Function2 implements Function {
 		
 		public final Value apply(List<Value> parameters) {
 			int n = parameters.size();
			if (n != 2) throw new Function.ArityException(0, 2);
 			return invoke(parameters.get(0), parameters.get(1));
 		}
 		
 		protected abstract Value invoke(Value v0, Value v1);
 		
 	}
 	
 	public static abstract class Function3 implements Function {
 		
 		public final Value apply(List<Value> parameters) {
 			int n = parameters.size();
			if (n != 3) throw new Function.ArityException(0, 3);
 			return invoke(parameters.get(0), parameters.get(1), parameters.get(2));
 		}
 		
 		protected abstract Value invoke(Value v0, Value v1, Value v2);
 		
 	}
 
 }
