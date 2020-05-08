 package sk.fiit.stuba.perconik.debug;
 
public class Environment
 {
 	public static final boolean debug = System.getenv("DEBUG") != null;
 
 	private Environment()
 	{
 		throw new AssertionError();
 	}
 }
