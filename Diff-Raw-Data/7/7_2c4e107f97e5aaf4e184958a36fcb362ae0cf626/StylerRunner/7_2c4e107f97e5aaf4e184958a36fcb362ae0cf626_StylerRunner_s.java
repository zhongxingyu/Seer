 package name.webdizz.styler;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.equinox.app.IApplicationContext;
 import org.eclipse.jdt.core.formatter.CodeFormatterApplication;
 
 public class StylerRunner {
 
     public static void main(final String[] args) throws Exception {
 	if (null == args || (null != args && args.length < 2)) {
	    throw new Exception("You need to specify arguments.");
 	}
 	Map<String, String[]> arguments = new HashMap<String, String[]>();
 	arguments.put(IApplicationContext.APPLICATION_ARGS, args);
 	IApplicationContext context = new StylerApplicationContext(arguments);
 	if (null == context) {
	    throw new Exception("You need to specify arguments.");
 	}
 	IApplication codeFormatter = new CodeFormatterApplication();
 	codeFormatter.start(context);
     }
 }
