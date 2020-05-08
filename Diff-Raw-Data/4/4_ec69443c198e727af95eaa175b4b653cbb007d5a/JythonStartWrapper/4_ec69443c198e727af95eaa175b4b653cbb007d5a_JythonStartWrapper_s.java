 package grisu.jython;
 
 import org.python.util.jython;
 
import au.org.arcs.jcommons.utils.JythonHelpers;
 
 public class JythonStartWrapper {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		JythonHelpers.setJythonCachedir();
 
 		jython.main(args);
 
 	}
 
 }
