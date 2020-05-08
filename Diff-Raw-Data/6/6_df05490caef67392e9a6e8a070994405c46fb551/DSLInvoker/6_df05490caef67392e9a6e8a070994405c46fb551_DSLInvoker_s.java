 package de.tud.stg.tigerseye.dslsupport;
 
 import groovy.lang.Closure;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import de.tud.stg.tigerseye.dslsupport.logger.DSLSupportLogger;
 
 public final class DSLInvoker{
 	
 	private static final String NO_CONTEXT = "no_context_information";
 
 	private static final DSLSupportLogger logger = new DSLSupportLogger(DSLInvoker.class);
 	
	// TODO(Leo_Roos;Nov 18, 2011) 
 	// Shouldn't cache instances, they might change between files, need additional information about the file _and_ class to have an exact instance definition
 	private static final ConcurrentHashMap<dslObjectKey<?>, DSL> invokers = new ConcurrentHashMap<dslObjectKey<?>, DSL>();
 
 	public static Object eval(Class<? extends DSL> clazz, Closure<?> c) {
 		return eval(NO_CONTEXT,toList(clazz),c);
 	}
 	
 	public static Object eval(String contextInformation, Class<? extends DSL> clazz, Closure<?> c) {
 		return eval(contextInformation, toList(clazz), c);
 	}
 
 	public static Object eval(Class<? extends DSL>[] clazzes, Closure<?> c) {
 		return eval(NO_CONTEXT,Arrays.asList(clazzes) , c);		
 	}
 	
 	public static Object eval(List<Class<? extends DSL>> clazzes, Closure<?> c) {
 		return eval(NO_CONTEXT, clazzes, c);
 	}
 	
 	public static Object eval(String contextInformation, List<Class<? extends DSL>> clazzes, Closure<?> c) {
 		
 		Set<DSL> dsls = new HashSet<DSL>();
 
 		for (Class<? extends DSL> clazz : clazzes) {
 			DSL dsl = getDSL(clazz);
 			if (dsl != null)
 				dsls.add(dsl);
 		}
 
		InterpreterCombiner ic = new InterpreterCombiner(dsls, new HashMap<String, Object>());
 		return ic.eval(c);
 	}
 	
 	public static <T extends DSL> T getDSL(Class<T> clazz) {
 		return getDSL(NO_CONTEXT, clazz);
 	}
 
 	public static <T extends DSL> T getDSL(String contextInformation, Class<T> clazz) {
 		return getDSL(new dslObjectKey<T>(contextInformation, clazz));
 	}
 
 	private static List<Class<? extends DSL>> toList(Class<?>... clazzes) {
 		List<Class<? extends DSL>> result = new ArrayList<Class<? extends DSL>>(clazzes.length);
 		for (Class<?> c : clazzes) {
 			/* Throws exception if not of type DSL */
 			Class<? extends DSL> clazz = (Class<? extends DSL>) c;
 			result.add(clazz);
 		}
 		return result;
 	}
 
 	private static <T extends DSL> T getDSL(dslObjectKey<T> invokerKey) {				
 		DSL object = invokers.get(invokerKey);
 
 		if (object == null) {
 			try {
 				object = invokerKey.clazz.newInstance();
 			} catch (InstantiationException e) {
 				logger.error("Failed to instantiate class", e);
 			} catch (IllegalAccessException e) {
 				logger.error("Failed to instantiate class", e);
 			}
 			
 			invokers.put(invokerKey, object);
 		}
 		
 		if(object == null){
 			throw new IllegalArgumentException("No instance for "+ invokerKey + " was found, nor could it be instantiated.");
 		}
 		
 		return (T) object;
 	}
 
 	static DSL getInvoker(Class<? extends DSL> clazz) {
 		return getDSL(clazz);
 	}
 	
 	private static class dslObjectKey<T>{
 		
 		private final String context;
 		private final Class<T> clazz;
 
 		public dslObjectKey(String context, Class<T> clazz) {
 			this.context = context;
 			this.clazz = clazz;
 		}
 		
 		@Override
 		public boolean equals(Object obj) {
 		    	if (obj == null){
 		    		return false;
 		    	}
 		    	if (obj == this){
 		    		return true;
 		    	}
 		    	if (obj.getClass() != this.getClass()) {
 		    		return false;
 		    	}
 		    	DSLInvoker.dslObjectKey<?> other = (DSLInvoker.dslObjectKey<?>) obj;
 		    	boolean equal = context == other.context && clazz.equals(other.clazz); 
 		    	return equal;
 		    }
 		
 		@Override
 		public int hashCode() {
 			return context.hashCode() * 11 + 7  * clazz.hashCode();
 		}
 		
 		@Override
 		public String toString() {
 			return "DSLObjectKey[context="+context+",class="+clazz+"]";
 		}
 	}
 	
 /*
 	private Class<? extends DSL> clazz;
 
 	private DSLInvoker() {
 		//Not supposed to be instantiated
 	}
 
 	/**
 	 * @deprecated use static methods for consistent access
 	 //
 	@Deprecated
 	public DSLInvoker(Class<? extends DSL> clazz) {
 		this.clazz = clazz;
 	}
 
 	/**
 	 * @deprecated use static methods for consistent access
 	 
 	@Deprecated
 	public Object eval(Closure<?> c) {
 		if (this.clazz == null)
 			throw new IllegalStateException("If instance eval is used the Invoker must have been initialized with a clazz");
 		else
 			return eval(this.clazz, c);
 	}
 	*/
 	
 }
