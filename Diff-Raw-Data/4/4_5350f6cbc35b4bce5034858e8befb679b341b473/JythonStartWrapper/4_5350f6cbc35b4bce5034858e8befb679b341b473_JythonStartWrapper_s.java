 package grisu.jython;
 
 import grisu.jcommons.utils.JythonHelpers;
 
 import org.python.util.jython;
 
 
 public class JythonStartWrapper {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		JythonHelpers.setJythonCachedir();
 
 		jython.main(args);
 
 	}
 
 }
