 /*
  * Copyright 1996-2002 by Andruid Kerne. All rights reserved.
  * CONFIDENTIAL. Use is subject to license terms.
  */
 package cm.generic;
 
 import java.net.*;
 
 /**
  * With an eye toward browser/application portability this interface
  * begins to constitute a transparrent wrapper that provides services
  * that come from the runtime environment, such as parameters, and opening web
  * pages for browsing. 
  * <p>
  * 
  * <b>Cooperative programming</b>: Any class that implements this
  * interface needs to include the following line of code at the top of
  * its initialization, in order to make the services provided by this
  * <code>Environment</code> interface globally accessible.
  * <blockquote><pre>
  *       Environment.the.set(this);
  * </pre></blockquote>
  * 
  * The nested class <code>The</code> serves to keep a global reference
  * to the actual instance of the singleton class that implements Environment.
  * (Don't have more than 1 instance that does!)
  * <p>
  * The <i>raison d'etre</i> of this interface is first to allow
  * programs to utilize services in a uniform way, whether they are
  * applets or applications. The <i>raison d'etre</i> of the nested class
  * is to overcome the existence of a single <code>java.applet.Applet</code>
  * instance in the runtime environment which provides services that
  * conceptually, one expects to get from a static, like
  * <code>java.lang.System</code>. This is a simple mechanism that
  * actually does something quite complex, and for complex reasons.
  * <p>
  * 
  * Services are then available globally through syntax such as:
  * <blockquote><pre>
  *       Environment.the.get().parameter("bgcolor");
  * </pre></blockquote>
  */
 public interface Environment
 {
 /**
  * Holds a  reference to the singleton global instance of
  * {@link Environment Environment}, and simple methods for getting and setting
  * this reference.
  */
    public class The extends Debug
    {
       Environment environment;
 
       boolean		hasQuicktime;
       float		javaVersion	= 1.1f;	// minimum expected
       boolean		hasGL;
       boolean		hasAgile2D;
       boolean		hasXML;
       
       public The()
       {
 	 try
 	 {
 	    Class.forName("quicktime.std.movies.Movie");
 	    hasQuicktime	= true;
 	 } catch (ClassNotFoundException e)
 	 {
 	 }
 
 	 try
 	 {
 	    Class.forName("gl4java.awt.GLCanvas");
 	    hasGL		= true;
 	    Debug.println("has gl4Java!");
 	    try
 	    {
 	       Class.forName("agile2D.AgileJFrame ");
 	       hasAgile2D	= true;
 	       Debug.println("has agile 2d");
 	    } catch (ClassNotFoundException e)
 	    {
 	    }
 	 } catch (ClassNotFoundException e)
 	 {
 	 }
 
 	 try
 	 {
 	    Class.forName("javax.xml.transform.dom.DOMSource");
 	    javaVersion		= 1.4f;
 	 } catch (ClassNotFoundException e4)
 	 {
 	    try
 	    {
 	       Class.forName("java.awt.font.TextMeasurer");
 	       javaVersion		= 1.3f;
 	    } catch (ClassNotFoundException e3)
 	    {
 	       try
 	       {
 		  Class.forName("java.util.HashMap");
 		  javaVersion		= 1.2f;
 	       } catch (ClassNotFoundException e2)
 	       {
 	       }
 	    }
 	 }
 	 try
 	 {
 	    Class.forName("org.w3c.dom.Node");
 	    hasXML	= true;
 	 } catch (ClassNotFoundException e2)
 	 {
 	 }
 	 debug("javaVersion=" + javaVersion+" hasXML="+hasXML);
       }
       public The(Environment e)
       {
 	 set(e);
       }
       public void set(Environment e)
       {
 	 environment	= e;
       }
       public Environment get()
       {
 	 return environment;
       }
 /**
  * @return	The version of Java we're using (but not the specific release),
  *		as in 1.2, 1.3, 1.4,...
  */
       public float javaVersion()
       {
 	 return javaVersion;
       }
       public boolean hasQuicktime()
       {
 	 return hasQuicktime;
       }
       public boolean hasAgile2D()
       {
 	 return hasAgile2D;
       }
       public boolean hasGL()
       {
 	 return hasGL;
       }
       public boolean hasXML()
       {
 	 return hasXML;
       }
 /**
  * Get a float parameter from the runtime environment.
  * 
  * @param	name		The name of the parameter's key.
  * @param	defaultValue	Default floating point value, in case param is 
  *				unspecified in the runtime env.
  */
    public float parameterFloat(String paramName, float defaultValue)
    {
       String paramValue	= environment.parameter(paramName);
       float result	= defaultValue;
       if (paramValue != null)
       {
 	 float parsedValue	= Generic.parseFloat(paramValue);
 	 if (!Float.isNaN(parsedValue))
 	    result	= parsedValue;
       }
       return result;
    }
    };
    static The the	= new The();
 
 /**
  * Find out which java runtime we're operating in.
  */
    int		runtimeEnv();
 /**
  * Find out which browser we're running in.
  */
    int		browser();
 /**
  * Show the user a message in the status bar at the bottom of the
  * browser, or some other comparable place.
  */
    void		showStatus(String s);
 /**
  * Show <code>msg</code> in the browser's status bar.
  * 
  * Short form, with for (@link java.cm.applet.Applet#showStatus).
  * Also more robust: avoids breaking when <code>msg</code> is null.
  */
    public void status(String msg);
 
 /**
  * Get a parameter or property, based on a key. Implements a name/value pair.
  * @param	name	The name of the key.
  */
    String	parameter(String name);
 /**
  * Get a boolean parameter from the runtime environment. If the value is the
  * string <code>true</code> or <code>yes</code>, the result is 
  * <code>true</code>; else false.
  * 
  * @param	name	The name of the parameter's key.
  */
    boolean parameterBool(String name);
    
 /**
  * Get an integer parameter from the runtime environment. The default is 0.
  * 
  * @param	name	The name of the parameter's key.
  */
    public int parameterInt(String paramName);
    
 /**
  * Get an integer parameter from the runtime environment. 
  * 
  * @param	name		The name of the parameter's key.
  * @param	defaultValue	Default integer value, in case param is 
  *				unspecified in the runtime env.
  */
    public int parameterInt(String paramName, int defaultValue);
 
 /**
  * Get a float parameter from the runtime environment.
  * 
  * @param	name		The name of the parameter's key.
  * @param	defaultValue	Default floating point value, in case param is 
  *				unspecified in the runtime env.
  */
    public float parameterFloat(String paramName, float defaultValue);
 
 
 /**
  * {@link java.applet.Applet#getCodeBase() java.applet.Applet.getCodeBase()}
  */
     URL codeBase();
 /**
 * {@link java.applet.Applet#getDocumentBase() java.applet.Applet.getDocBase()}
  */
     URL docBase();
 
 /**
  * @return an URL relative to html document
  */   
    URL rel(String relativeURL);
 /**
  * @return an URL relative to the code base.
  */   
    URL codeRelativeURL(String relativeURL);
 /**
  * @return an URL relative to the code base.
  */   
    URL docRelativeURL(String relativeURL);
 
    public static final int	APPLICATION	= -1;
    public static final int	IE		= 0;
    public static final int	NS		= 1;
    public static final int	PLUGIN		= 2;
 
    public void go(URL u, String frame);
 }
