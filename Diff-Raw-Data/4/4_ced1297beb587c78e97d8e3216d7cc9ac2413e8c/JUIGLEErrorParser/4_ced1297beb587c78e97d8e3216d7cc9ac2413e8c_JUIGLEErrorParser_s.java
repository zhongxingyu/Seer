 package ch.ethz.origo.juigle.data;
 
 import java.text.MessageFormat;
 import java.util.ResourceBundle;
 
 /**
  * This class provide parser for Exception messages.
  * 
  * @author Vaclav Souhrada
  * @version 0.1.2 (3/20/2010)
  * @since 0.1.0 (1/30/2010)
  *
  */
 public class JUIGLEErrorParser {
 
 	
 	public static String getJUIGLEErrorMessage(String errorCode) {
		System.out.println(System.getProperty("user.dir"));
 		return JUIGLEErrorParser.parseMessage(errorCode, "ch.ethz.origo.juigle.data.errors");
 	}
 	
 	public static String getErrorMessage(String errorCode, String filePath) {
 		return JUIGLEErrorParser.parseMessage(errorCode, filePath);
 	}
 	
 	private static String parseMessage(String errorCode, String filePath) {
 		String[] args = errorCode.split(":");
 		String pattern = ResourceBundle.getBundle(filePath).getString(args[0]);

 		if (args.length > 1) {
 			MessageFormat formatter = new MessageFormat(pattern);
 			Object[] arguments = new Object[args.length - 1]; // ignore first position
 			for (int i = 1; i < args.length; i++) {
 				arguments[i-1] = args[i];
 			}
 			return formatter.format(arguments);
 		} else {
 			return pattern;
 		}
 	}
 	
 }
