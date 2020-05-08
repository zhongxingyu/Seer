 package sk.fiit.stuba.perconik.debug;
 
 import java.io.PrintStream;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import sk.stuba.fiit.perconik.plugin.Activator;
 
public final class Debug
 {
 	private static final String PLUGIN_ID = Activator.PLUGIN_ID;
 
 	private static final PrintStream out = System.out;
 	
 	private Debug()
 	{
 		throw new AssertionError();
 	}
 	
 	private static final void log(final Status status)
 	{
 		Activator.getDefault().getLog().log(status);
 	}
 	
 	public static void print(final String message)
 	{
 		if (Environment.debug)
 		{
 			out.println(message);
 		}
 	}
 	
 	public static void print(final String format, final Object ... args)
 	{
 		print(String.format(format, args));
 	}
 
 	public static void notice(final String message)
 	{
 		if (Environment.debug)
 		{
 			log(new Status(IStatus.INFO, PLUGIN_ID, message));
 		}
 	}
 	
 	public static void notice(final String format, Object ... args)
 	{
 		notice(String.format(format, args));
 	}
 	
 	public static void warning(final String message)
 	{
 		if (Environment.debug)
 		{
 			log(new Status(IStatus.WARNING, PLUGIN_ID, message));
 		}
 	}
 
 	public static void warning(final String format, Object ... args)
 	{
 		warning(String.format(format, args));
 	}
 
 	public static void error(final String message, final Exception e)
 	{
 		log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
 	}
 }
