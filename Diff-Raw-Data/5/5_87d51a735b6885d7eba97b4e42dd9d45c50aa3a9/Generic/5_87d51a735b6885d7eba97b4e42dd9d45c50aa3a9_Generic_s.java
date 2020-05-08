 package de.ismll.console;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 
 import de.ismll.bootstrap.CommandLineParser;
 
 public class Generic {
 
 	static Logger log = LogManager.getLogger(Generic.class);
 	
 	public static void main(String[] args) throws ClassNotFoundException, SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
 		if (args.length<1) {
 			log.fatal("Need at least one parameter");
 			System.exit(3);
 		}
 				
 		String className = args[0];
 		log.info("Class to be used: " + className);
 		
 		args = shift(args);
 		Class<?> forName = Class.forName(className);
 		log.debug("Class reference is " + forName);
 		Method method = null ;
 		try {
 			method = forName.getMethod("main", Array.newInstance(String.class,0).getClass());
 			log.debug("main()-Method found at " + method);			
 		} catch (NoSuchMethodException e) {
 			//nooop
 		}
 		if(method != null) {
 			method.invoke(null, new Object[] {args});
 		} else {
 			log.info("No main()-method was found. Checking for Runnable implementation");
 			try {
 				Class<? extends Runnable> asSubclass = forName.asSubclass(Runnable.class);
 				log.info(asSubclass + " implements Runnable. Instantiating ...");
 				Runnable newInstance = asSubclass.newInstance();
 				log.debug("Instantiated. Parsing command line options ...");
 				CommandLineParser.parseCommandLine(args, newInstance);
 				log.info("Parameters parsed. Calling run()-method.");
 				newInstance.run();
 				log.info("Finished.");
 			} catch (ClassCastException e) {
 				StackTraceElement[] stackTrace = e.getStackTrace();
 				boolean bootstraperror=false;
 				if (stackTrace.length>2) {
 					String compareTo = Generic.class.getCanonicalName();
 					String causingClazz = forName.getCanonicalName();
 					if (compareTo!=null 
 							&& /*callee should be this implementation:*/ stackTrace[1].getClassName().equals(compareTo)
 							&& /*causing class should be the implementation that was instantiated*/ !stackTrace[0].getClassName().equals(causingClazz)) {
 						log.error("ClassCastException: Does " + forName + " maybe not implement Runnable? If so, cannot use it.");
 						bootstraperror=true;
 					}
 				}
 				if (!bootstraperror)
 					throw e;
 				return;
 			} catch (InstantiationException e) {
 				log.error("Could not instantiate " + forName + ". Public, parameterless constructor present?");
 			} catch (RuntimeException e) {
 				log.error("Runtime Exception caught: " + e.getMessage(),e);
 				// TODO: Add here maybe interactive debug handling ...
 				return;
 			} 
 		}
 		
 	}
 
 	private static String[] shift(String[] args) {
 		String [] ret = new String[args.length-1];
 		for (int i = 1; i < args.length; i++) 
 			ret[i-1] = args[i];
 		return ret;
 	}
 }
