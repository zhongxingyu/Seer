 package grisu.jython;
 
import grisu.frontend.control.login.LoginManager;
 import grisu.jcommons.utils.JythonHelpers;
 
 import org.python.util.jython;
 
 
 public class JythonStartWrapper {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		JythonHelpers.setJythonCachedir();
 
		LoginManager.initEnvironment();

 		jython.main(args);
 
 	}
 
 }
