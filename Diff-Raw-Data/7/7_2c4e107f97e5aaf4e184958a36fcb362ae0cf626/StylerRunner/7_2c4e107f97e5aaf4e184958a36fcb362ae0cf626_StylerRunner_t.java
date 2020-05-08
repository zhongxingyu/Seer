 package name.webdizz.styler;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.equinox.app.IApplicationContext;
 import org.eclipse.jdt.core.formatter.CodeFormatterApplication;
 
 public class StylerRunner {
 
     public static void main(final String[] args) throws Exception {
 	if (null == args || (null != args && args.length < 2)) {
	    System.out.println("You need to specify arguments to run formatter.");
	    System.exit(0);
 	}
 	Map<String, String[]> arguments = new HashMap<String, String[]>();
 	arguments.put(IApplicationContext.APPLICATION_ARGS, args);
 	IApplicationContext context = new StylerApplicationContext(arguments);
 	if (null == context) {
	    System.out.println("Unable to create ApplicationContext, probably due to you need to specify arguments to run formatter.");
	    System.exit(0);
 	}
 	IApplication codeFormatter = new CodeFormatterApplication();
 	codeFormatter.start(context);
     }
 }
