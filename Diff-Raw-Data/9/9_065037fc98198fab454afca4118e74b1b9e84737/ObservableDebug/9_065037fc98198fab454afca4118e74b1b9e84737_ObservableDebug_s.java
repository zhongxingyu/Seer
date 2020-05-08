 package ecologylab.generic;
 
 import java.util.Observable;
 
 /**
  * An {@link java.util.ArrayList ArrayList}, extended with convenient
  * connections to our {@link Debug Debug} methods.
  */
 public class ObservableDebug
 extends Observable
 {
    protected ObservableDebug()
    {
    }
    public final void debug(String message)
    {
       Debug.println(this, message);
    }
    public final void debugA(String message)
    {
       Debug.printlnA(this, message);
    }
    public final void debugI(String message)
    {
       Debug.printlnI(this, message);
    }
 
    public final void debug(int messageLevel, String message)
    {
       Debug.println(this, messageLevel, message);
    }
    
    public final void debugA(int level, String message)
    {
       Debug.printlnA(this, level, message);
    }
    public final void debugI(int level, String message)
    {
       Debug.printlnI(this, level, message);
    }
    
    public final String getClassName()
    {
       return Debug.getClassName(this);
    }
    public static void print(String message) 
    {
       Debug.print(message);
    }
    public static void println(String message)
    {   	
       Debug.println(message);
    }
    public static void println(StringBuffer buffy)
    {   	
       Debug.println(buffy);
    }
    public String toString()
    {
       return Debug.toString(this);
    }
    public static void println(String className,
 			      int messageLevel, String message) 
    {
       Debug.println(className, messageLevel, message);
    }
    public final boolean show(int messageLevel)
    {
       return Debug.show(this, messageLevel);
    }
    public String superString()
    {
       return super.toString();
    }
 //   protected final void finalize()
 //   {
 //      AllocationDebugger.finalized(this);
 //   }
 }
