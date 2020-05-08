 package ecologylab.generic;
 
 import java.util.*;
 import java.io.*;
 
 import ecologylab.appframework.Environment;
 import ecologylab.appframework.types.prefs.Pref;
 import ecologylab.appframework.types.prefs.PrefInt;
 import ecologylab.io.Files;
 
 
 
 /**
  * A developer-friendly base class and toolset for logging debug messages.
  * 
  * Supports a threshold, aka <code>level</code> with 2 levels of granularity:
  *	1) global	<br>
  *	2) on a per class basis	<br>
  * 
  * This levels are configured via runtime startup params 
  * ( via the JavaScript prefs mechanisms for  applet versions)
  * 
  * in the form of
  *	1) 
  *		debug_global_level = 4;
  *	2) 
  *		debug_levels	= "Parser 3; HTMLPage 2; CollageOp 37";
  * 
  * @author andruid
  */
 public class Debug 
 {
 	private static final String                 SEPARATOR        = ": ";
 
     /**
      * Global flag for printing "interactive debug" statements. See also {@link #debugI(String) debugI()}.
      */
     private static boolean                      interactive;
 
     private static boolean                      logToFile        = false;
 
     private static final HashMap<Class<?>, String> classAbbrevNames = new HashMap<Class<?>, String>();
 
     private static final HashMap<Class<?>, String> packageNames     = new HashMap<Class<?>, String>();
 
     static final int                            FLUSH_FREQUENCY  = 10;
 
     /**
      * Holds class specific debug levels.
      */
     static final HashMap<String, IntSlot>       classLevels      = new HashMap<String, IntSlot>();
 
     /**
      * Global hi watermark. debug() messages with a level less than or equal to this will get printed out.
      */
     // private static PrefInt level = Pref.usePrefInt("debug_global_level", 0);;
 
 
    public static void initialize()
    {
       // class specific
       String levels	= Pref.lookupString("debug_levels");
       //println("Debug.initialize(" + Debug.level+", "+ levels+")");
       if (levels != null)
       {
     	  StringTokenizer tokenizer	= new StringTokenizer(levels,";");
     	  {
     		  try
     		  {
     			  while (tokenizer.hasMoreTokens())
     			  {
     				  String thisSpec		= tokenizer.nextToken();
     				  StringTokenizer specTokenizer= new StringTokenizer(thisSpec);
     				  try
     				  {
     					  String thisClassName	= specTokenizer.nextToken();
     					  int thisLevel		=
     						  Integer.parseInt(specTokenizer.nextToken());
     					  Debug.println("Debug.level\t" + thisClassName + "\t" +
     							  thisLevel);
     					  classLevels.put(thisClassName,
     							  new IntSlot(thisLevel));
     				  } catch (Exception e)
     				  {
     				  }
     			  }
     		  } catch (NoSuchElementException e)
     		  {
     		  }
     	  }
       }
    }
    protected Debug()
    {
       //AllocationDebugger.constructed(this);
    }
 
    public final int level()
    {
       return level(this);
    }
    public static final int level(Object that)
    {
 	   //return level(getClassName(that));
 	   return 0;
    }
    public static final int level(String className)
    {
 /*      int result	= level.value();
       IntSlot slot	= (IntSlot) classLevels.get(className);
       if (slot != null)
 		 result		= slot.value;
       return result;
       */
 	   return 0;
    }
    
 /**
  * @param	messageLevel If less than or equal to the static level,
  * message will get logged. Otherwise, the statement will be ignored.
  */
    //TODO make levels work again
    public static void println(int messageLevel, CharSequence message) 
    {
       //if (messageLevel <= level.value())
 		// println(message);
    }
    public static void printlnI(int messageLevel, CharSequence message) 
    {
       if (interactive)
 		 println(message);
    }
    public static void println(Object o, CharSequence message)
    {
 	  print(o.toString());
 	  print(SEPARATOR);
 	  println(message);
    }
 
    public static void println(String className, CharSequence message)
    {
 	   print(className);
 	   print(SEPARATOR);
 	   println(message);
    }
    public static void printlnI(Object o, CharSequence message)
    {
       if (interactive)
 		 println(o, message);
    }
    public static void printlnI(CharSequence message) 
    {
       if (interactive)
 		 println(message);
    }
 
    /**
     * Print the message to System.err.
     * <p/>
     * If we are logging to a file, also write to the file, but in this case, prepend the Date:<tab>
     * 
     * @param message
     */
    public static void println(CharSequence message) 
    {   	
    	  if (logToFile)
    	  {
    		  try
    		  {
    			  writer.append(new Date().toString());
    			  writer.append(':').append('\t');
    			  writer.append(message);
    			  writer.append('\n');
    			  writer.flush();
    		  } catch (IOException e)
    		  {
    			  e.printStackTrace();
    		  }
   	  }  
    	  else
 		 System.err.println(message);
    }
    public static void print(char c) 
    {
 	   if (logToFile)
 	   {
 		   try
 		   {
 			   writer.append(c);
 		   } catch (IOException e)
 		   {
 			   e.printStackTrace();
 		   }  
 	   }  
 	   else
 		   System.err.print(c);
 	   
    }
    public static void print(CharSequence message) 
    {
    	  if (logToFile)
    	  {
    		  try
    		  {
    			  writer.append(message);
    		  } catch (IOException e)
    		  {
    			  e.printStackTrace();
    		  }  
    	  }  
    	  else
 		 System.err.print(message);
    }
 /**
  * Print a debug message, starting with the abbreviated class name of
  * the object.
  */
    public static void printlnA(Object that, CharSequence message) 
    {
       println(getClassName(that)+SEPARATOR + message/* +" " +level(that) */);
    }
 /**
  * Print a debug message, starting with the abbreviated class name.
  */
    public static void printlnA(Class<?> c, CharSequence message) 
    {
       println(classSimpleName(c)+SEPARATOR + message);
    }
 
    static char PERIOD	= 	'.';
    static char SPACE	=	' ';
 /**
  * This actually seems to be much more efficient than Class. getSimpleName(),
  * because we are smart about using lazy evaluation and caching results in a HashMap.
  * 
  * @return   the abbreviated name of the class - without the package qualifier.
  */
    public static String classSimpleName(Class<?> thatClass)
    {
       String abbrevName	= classAbbrevNames.get(thatClass); 
       if (abbrevName == null)
       {
 		 String fullName	= thatClass.toString();
 		 abbrevName	= fullName.substring(fullName.lastIndexOf(PERIOD) + 1);	 
 		 synchronized (classAbbrevNames)
 		 {
 			classAbbrevNames.put(thatClass, abbrevName);
 		 }
       }
       return abbrevName;
    }
 /**
  * @return   the abbreviated name of the class - without the package qualifier.
  */
    public static String getPackageName(Class<?> thatClass)
    {
       //System.out.println("thatClass.toString() is " + thatClass.toString());
       String packageName = packageNames.get(thatClass);
       if (packageName == null)
       {
 		 String className	= thatClass.toString();
 		 packageName	= className.substring(className.indexOf(SPACE) + 1, className.lastIndexOf(PERIOD));	 
 		 synchronized (packageNames)
 		 {
 		    packageNames.put(thatClass, packageName);
 			//		    packageNames.put(className, packageName);
 		 }
       }
       return packageName;
    }
 /**
  * @return   the abbreviated name of the class - without the package qualifier.
  */
    public static String getClassName(Object o)
    {
       return (o == null) ? "null" : classSimpleName(o.getClass());
    }
 /**
  * @return  the abbreviated name of this class - without the package qualifier.
  */
    public String getClassName()
    {
       return getClassName(this);
    }
    
 /**
  * @return   the package name of the class - without the package qualifier.
  */
    public static String getPackageName(Object o)
    {
       return getPackageName(o.getClass());
    }
 /**
  * @return  the package name of this class - without the package qualifier.
  */
    public String getPackageName()
    {
       return getPackageName(this);
    }
    @Override public String toString()
    {
       return getClassName(this);
    }
    public String superString()
    {
       return super.toString();
    }
    public static String toString(Object o)
    {
       return getClassName(o);
    }
 /**
  * Print a debug message that starts with this.toString().
  */
    public final void debug(CharSequence message)
    {
       println(this, message);
    }
    /**
     * Print a message about an error, starting with this.toString().
     */
    public void error(CharSequence message)
    {
 	   error(this, message);
    }
    /**
     * Print a message about a warning, starting with this.toString().
     */
    public void warning(CharSequence message)
    {
 	   warning(this, message);
    }
    /**
     * Print a message about something that should never happen, starting with this.toString().
     */
    public void weird(CharSequence message)
    {
 	   weird(this, message);
    }
    /**
     * Print a message about an error, starting with that.toString().
     */
    public static void error(Object that, CharSequence message)
    {
 	   emphasized(that, "ERROR - ", message);
    }
    /**
     * Print a message with emphasis.
     * 
     * @param that
     * @param header
     * @param message
     */
    static void emphasized(Object that, String header, CharSequence message)
    {
 	   print('\n');
 	   print(header);
	   print(SEPARATOR);
	   print(that.toString());
 	   print(message);
 	   print('\n');
 	   print('\n');	   
    }
    /**
     * Print a message about a warning, starting with that.toString().
     */
    public static void warning(Object that, CharSequence message)
    {
 	   emphasized(that, "WARNING - ", message);
    }
   
    /**
     * Print a message about something that should never happen, starting with that.toString().
     */
    public static void weird(Object that, CharSequence message)
    {
 	   emphasized(that, "WEIRD - ", message);
    }
   
 /**
  * Print a debug message that starts with this.toString().
  */
    public final void debug(StringBuffer message)
    {
       println(this, message);
    }
 /**
  * Print a debug message that starts with the abbreviated class name of this.
  */
    public final void debugA(CharSequence message)
    {
       printlnA(this, message);
    }
 /**
  * Print a debug message that starts with the abbreviated class name of this.
  */
    public final void debugA(StringBuffer message)
    {
       printlnA(this, message.toString());
    }
    public final void debugI(CharSequence message)
    {
       printlnI(this, message);
    }
    public final void debugI(StringBuffer message)
    {
       printlnI(this, message.toString());
    }
 /**
  * Evaluates the same conditional as Debug usually does implicitly, for explicit use in special static Debug printing scenarios.
  **/
    public static final boolean show(Object that, int messageLevel)
    {
       return messageLevel <= level(that);
    }
    public boolean show(int messageLevel)
    {
       return show(this, messageLevel);
    }
    /**
  * Print a debug message that starts with the abbreviated class name of this,
  * but only if messageLevel is greater than the debug <code>level</code> for
  * this class (see above).
  */
    public final void debug(int messageLevel, CharSequence message)
    {
 //      if (show(messageLevel))
       if (messageLevel <= level())
 		 println(this, message);
    }
    public final void debugA(int messageLevel, CharSequence message)
    {
       if (messageLevel <= level())
 		 printlnA(this, message);
    }
    public static final void println(Object that, int messageLevel,
 		   CharSequence message)
    {
       if (messageLevel <= level(that))
 		 println(that, message);
    }
    public static final void println(String className,
 			      int messageLevel, CharSequence message) 
    {
       if (messageLevel <= level(className))
 		 println(message);
    }
    public static final void printlnA(Object that, int messageLevel, 
 		   CharSequence message)
    {
       if (messageLevel <= level(that))
 		 printlnA(that, message);
    }
    public static final void printlnI(Object that, int messageLevel, 
 		   CharSequence message)
    {
       if (messageLevel <= level(that))
 		 printlnI(that, message);
    }
    public final void debugI(int messageLevel, CharSequence message)
    {
       if (messageLevel <= level())
 		 printlnI(this, message);
    }
    public static final void debug(Object o, CharSequence message, Exception e)
    {
       println(o, message);
       e.printStackTrace();
    }
 
    public static final void toggleInteractive()
    {
       interactive	= !interactive;
       String msg	= "Toggle interactive debug to " + interactive;
       Environment.the.get().status(msg);
       println(msg);
    }
    
    private static BufferedWriter	writer;
 
    public static final void setLoggingFile(String loggingFilePath)  
    {
 	  writer		= Files.openWriter(loggingFilePath);
 	  if (writer == null)
 		 println("Debug.setLoggingFile() CANT OPEN LOGGING FILE: " + loggingFilePath);
 	  else
 		 logToFile	= true;	    	   			
    }
 
    public static void closeLoggingFile()
    {
 	   Files.closeWriter(writer);
    }
 	
 /**
  * @return	state of the global flag for printing "interactive" debug
  *		statements.
  */
    public static boolean getInteractive()
    {
       return interactive;
    }
    
    public static boolean logToFile()
    {
 	  return  logToFile;
    }
 //   protected void finalize()
 //   {
 //      AllocationDebugger.finalized(this);
 //   }
 }
