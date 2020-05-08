 package ecologylab.appframework;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Stack;
 
 import ecologylab.appframework.types.AssetsState;
 import ecologylab.appframework.types.Preference;
 import ecologylab.appframework.types.PreferencesSet;
 import ecologylab.appframework.types.prefs.MetaPrefSet;
 import ecologylab.appframework.types.prefs.Pref;
 import ecologylab.appframework.types.prefs.PrefInt;
 import ecologylab.appframework.types.prefs.PrefSet;
 import ecologylab.appframework.types.prefs.PrefTranslations;
 import ecologylab.appframework.types.prefs.gui.PrefsEditor;
 import ecologylab.generic.Debug;
 import ecologylab.generic.DownloadProcessor;
 import ecologylab.generic.Generic;
 import ecologylab.io.Assets;
 import ecologylab.io.Files;
 import ecologylab.io.ZipDownload;
 import ecologylab.net.ParsedURL;
 import ecologylab.services.messages.DefaultServicesTranslations;
 import ecologylab.xml.ElementState;
 import ecologylab.xml.TranslationSpace;
 import ecologylab.xml.XmlTranslationException;
 import ecologylab.xml.XmlTranslationExceptionTypes;
 
 /**
  * An instance of Environment, which is an application, rather than an applet,
  * or a servlet.
  * The Environment mechanism is used to enable the provision of contextual
  * runtime configuration parameter services in a way that is 
  * independent of the deployment structure.
  * 
  * @author Andruid
  */
 public class ApplicationEnvironment
 extends Debug
 implements Environment, XmlTranslationExceptionTypes
 {
 	private static final String METAPREFS_XML = "metaprefs.xml";
 
 	/**
 	 * Subdirectory for eclipse launches.
 	 */
 	protected static final String ECLIPSE_PREFS_DIR		= "config/preferences/";
 
 //	private static final String BASE_PREFERENCE_PATH = PREFERENCES_SUBDIR_PATH+"preferences.txt";
 	private static final String ECLIPSE_BASE_PREFERENCE_PATH = ECLIPSE_PREFS_DIR+"preferences.xml";
 	
 	TranslationSpace		translationSpace;
 	
 	/**
 	 * Used for forming codeBase relative ParsedURLs.
 	 * A simulation of the property available in applets.
 	 * The codebase is the address where the java code comes from.
 	 */
 	ParsedURL	codeBase;
 	/**
 	 * Used for forming codeBase relative ParsedURLs.
 	 * A simulation of the property available in applets.
 	 * The docbase is the address where the launching HTML file comes from.
 	 */
 	ParsedURL	docBase;
 	
 	/**
 	 * Set of <code>MetaPref</code>s that describe preferences and provide default values.
 	 */
 	MetaPrefSet metaPrefSet;
 	
 	/**
 	 * Set of actual <code>Pref</code>s being used locally.
 	 */
 	PrefSet prefSet;
 	
 	/**
 	 * Place where <code>Pref</code>s are loaded from and stored to.
 	 */
 	ParsedURL prefsPURL;
 	
 	protected enum LaunchType
 	{
 		JNLP, ECLIPSE, JAR,
 	}
 	
 	LaunchType		launchType;
 	
 	/**
 	 * Create an ApplicationEnvironment. Create an empty properties object for application parameters.
 	 * <p/>
 	 * No command line argument is processed. 
 	 * Only default preferences are loaded, and processed with the default TranslationSpace.
 	 *  
 	 * @param args				The args array, which is treated as a stack with optional entries. They are:
 	 * 							*) JNLP -- if that is the launch method
 	 * 							*) preferences file if you are running in eclipse. Relative to CODEBASE/config/preferences/
 	 * 							*) graphics_device (screen number)
 	 * 							*) screen_size (used in TopLevel --
 	 * 									1 - quarter; 2 - almost half; 3; near full; 4 full)
 	 * @throws XmlTranslationException 
 	 */
 	public ApplicationEnvironment(String applicationName) throws XmlTranslationException
 	{
 	   this(null, applicationName, null);
 	}
 	
 	/**
 	 * Create an ApplicationEnvironment.
 	 * Load preferences from XML file founds in the config/preferences directory.
 	 * Default preferences will be loaded from preferences.xml.
 	 * If there is a 0th command line argument, that is the name of an additional
 	 * preferences file.
 	 *  
 	 * @param applicationName
 	 * @param translationSpace		TranslationSpace used for translating preferences XML.
 	 * 								If this is null, 
 	 * {@link ecologylab.services.messages.DefaultServicesTranslations ecologylab.services.message.DefaultServicesTranslations}
 	 * 								will be used.
 	 * @param args				The args array, which is treated as a stack with optional entries. They are:
 	 * 							*) JNLP -- if that is the launch method
 	 * 							*) preferences file if you are running in eclipse. Relative to CODEBASE/config/preferences/
 	 * 							*) graphics_device (screen number)
 	 * 							*) screen_size (used in TopLevel --
 	 * 									1 - quarter; 2 - almost half; 3; near full; 4 full)
 	 * @throws XmlTranslationException 
 	 */
 	public ApplicationEnvironment(String applicationName, TranslationSpace translationSpace, String args[]) throws XmlTranslationException
 	{
 	   this(null, applicationName, translationSpace, args);
 	}
 	/**
 	 * Create an ApplicationEnvironment.
 	 * Load preferences from XML files found in the config/preferences directory.
 	 * Default preferences will be loaded from preferences.xml.
 	 * If there is a 0th command line argument, that is the name of an additional
 	 * preferences file.
 	 * <p/>
 	 * The default TranslationSpace, from
 	 * {@link ecologylab.services.messages.DefaultServicesTranslations ecologylab.services.message.DefaultServicesTranslations}
 	 * will be used.
 	 *  
 	 * @param applicationName
 	 * {@link ecologylab.services.messages.DefaultServicesTranslations ecologylab.services.message.DefaultServicesTranslations}
 	 * 								will be used.
 	 * @param args				The args array, which is treated as a stack with optional entries. They are:
 	 * 							*) JNLP -- if that is the launch method
 	 * 							*) preferences file if you are running in eclipse. Relative to CODEBASE/config/preferences/
 	 * 							*) graphics_device (screen number)
 	 * 							*) screen_size (used in TopLevel --
 	 * 									1 - quarter; 2 - almost half; 3; near full; 4 full)
 	 * @throws XmlTranslationException 
 	 */
 	public ApplicationEnvironment(String applicationName, String args[]) throws XmlTranslationException
 	{
 	   this(applicationName, (TranslationSpace) null, args);
 	}
 	/**
 	 * Create an ApplicationEnvironment.
 	 * Get the base for finding the path to the "codeBase" by using the
 	 * package path of the baseClass passed in.
 	 * <p/>
 	 * Load preferences from XML file founds in the codeBase/config/preferences directory.
 	 * Default preferences will be loaded from preferences.xml.
 	 * If there is a 0th command line argument, that is the name of an additional
 	 * preferences file.
 	 * <p/>
 	 * Also, sets the Assets cacheRoot to the applicationDir().
 	 * <p/>
 	 * The default TranslationSpace, from
 	 * {@link ecologylab.services.messages.DefaultServicesTranslations ecologylab.services.message.DefaultServicesTranslations}
 	 * will be used.
 	 *  
 	 * @param baseClass				Used for computing codeBase property.
 	 * @param applicationName
 	 * @param args				The args array, which is treated as a stack with optional entries. They are:
 	 * 							*) JNLP -- if that is the launch method
 	 * 							*) preferences file if you are running in eclipse. Relative to CODEBASE/config/preferences/
 	 * 							*) graphics_device (screen number)
 	 * 							*) screen_size (used in TopLevel --
 	 * 									1 - quarter; 2 - almost half; 3; near full; 4 full
 	 * @throws XmlTranslationException 
 	 */
 	public ApplicationEnvironment(Class baseClass, String applicationName, String args[]) throws XmlTranslationException
 	{
 	   this(baseClass, applicationName, null, args);
 	}
 	/**
 	 * Create an ApplicationEnvironment.
 	 * <p/>
 	 * Treats the args array like a stack. If any args are missing (based on their format), they are skipped.
 	 * <p/>
 	 * The first arg we seek is codeBase. This is a path that ends in slash.
 	 * It may be a local relative path, or a URL-based absolute path.
 	 * <p/>
 	 * The next possible arg is a preferences file. This ends with .xml.
 	 * <p/>
 	 * The next 2 possible args are integers, for graphicsDev and screenSize.
 	 * 			graphics_device (screen number) to display window. count from 0.
 	 * 			screenSize		used in TopLevel --
 	 * 								1 - quarter; 2 - almost half; 3; near full; 4 full	 
 	 * <p/>
 	 * Get the base for finding the path to the "codeBase" by using the
 	 * package path of the baseClass passed in.
 	 * <p/>
 	 * Load preferences from XML file founds in the codeBase/config/preferences directory.
 	 * Default preferences will be loaded from preferences.xml.
 	 * If there is a 0th command line argument, that is the name of an additional
 	 * preferences file.
 	 * <p/>
 	 * Also, sets the Assets cacheRoot to the applicationDir().
 	 * <p/>
 	 * The default TranslationSpace, from
 	 * {@link ecologylab.services.messages.DefaultServicesTranslations ecologylab.services.message.DefaultServicesTranslations}
 	 * will be used.
 	 * 
 	 * @param baseClass			Used for computing codeBase property.
 	 * @param applicationName	Name of the application.
 	 * @param translationSpace		TranslationSpace used for translating preferences XML.
 	 * 								If this is null, 
 	 * {@link ecologylab.services.messages.DefaultServicesTranslations ecologylab.services.message.DefaultServicesTranslations}
 	 * 								will be used.
 	 * @param args				The args array, which is treated as a stack with optional entries. They are:
 	 * 							*) JNLP -- if that is the launch method
 	 * 							*) preferences file if you are running in eclipse. Relative to CODEBASE/config/preferences/
 	 * 							*) graphics_device (screen number)
 	 * 							*) screen_size (used in TopLevel --
 	 * 									1 - quarter; 2 - almost half; 3; near full; 4 full)
 	 * @throws XmlTranslationException 
 	 */
 	public ApplicationEnvironment(Class baseClass, String applicationName, TranslationSpace translationSpace,  String args[]) throws XmlTranslationException
 //			String preferencesFileRelativePath, String graphicsDev, String screenSize) 
 	{
 		this.translationSpace		= translationSpace;
 		
 		// this is the one and only singleton Environment
 		Environment.the.set(this);
 		
 		PropertiesAndDirectories.setApplicationName(applicationName);
 
 		// setup os specific system preferences
 		PropertiesAndDirectories.setOSSpecificProperties();
 		
 		if (translationSpace == null)
 			translationSpace	= DefaultServicesTranslations.get();
 		
 		Stack<String> argStack	= new Stack<String>();
 		
 		for (int i = args.length - 1; i>=0; i--)
 			argStack.push(args[i]);
 		
 		String arg;
 		processPrefs(baseClass, translationSpace, argStack);
 		   
 		Debug.initialize();
 
 		arg						= pop(argStack);
 		if (arg == null)
 			return;
 		try
 		{
 			Pref.usePrefInt("graphics_device", Integer.parseInt(arg));
 			
 			arg						= pop(argStack);
 			if (arg == null)
 				return;
 			Pref.usePrefInt("screen_size", Integer.parseInt(arg));
 
 		} catch (NumberFormatException e)
 		{
 			argStack.push(arg);
 		}
 		
 		// could parse more args here
 	}
 	
 	/**
 	 * request User's prefSet from the preferenceServlet and return the prefSetXML string.
 	 * @author eunyee
 	 * @param prefServlet
 	 * @param translationSpace TODO
 	 * @param uid
 	 * @return
 	 */
 	private PrefSet requestPrefFromServlet(String prefServlet, TranslationSpace translationSpace)
 	{
 /*
 		try {
 			ParsedURL purl = new ParsedURL(new URL(prefServlet));
 			
 			PrefSet prefSet = (PrefSet) ElementState.translateFromXML(purl, PrefTranslations.get());
 			return prefSet;
 		} catch (MalformedURLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (XmlTranslationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 */		
 		try 
 		{
 			URL url = new URL(prefServlet);	
 			URLConnection connection = url.openConnection();
 
 			// specify the content type that binary data is sent
 			connection.setRequestProperty("Content-Type", "text/xml");
 
 		    // define a new BufferedReader on the input stream
 		    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 
 		    // receive data from the servlet
 		    String prefSetXML = in.readLine();
 		    PrefSet prfs = null;
 			try 
 			{
 				prfs = PrefSet.load(prefSetXML, translationSpace);
 			} 
 			catch (XmlTranslationException e) 
 			{
 				e.printStackTrace();
 			} 
 			in.close();
 			
 			return prfs;
 		} 
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/**
 	 * Load MetaPrefs and Prefs, if possible
 	 * 
 	 * @param baseClass
 	 * @param translationSpace
 	 * @param argStack
 	 * @throws XmlTranslationException
 	 */
 	private void processPrefs(Class baseClass, TranslationSpace translationSpace, Stack<String> argStack) 
 	throws XmlTranslationException
 	{
 		LaunchType launchType	= LaunchType.ECLIPSE;	// current default
 		
 		// look for launch method identifier in upper case
 		String arg				= pop(argStack);
 		if (arg != null)
 		{
 			String uc				= arg.toUpperCase();
 			if ("JNLP".equals(uc) )
 			{	// tells us how we were launched: e.g., JNLP, ECLIPSE, ...
 				launchType		= LaunchType.JNLP;
 			}
 			else
 			{
 				//TODO -- recognize JAR here !!!
 				argStack.push(arg);
 			}
 		}
 		println("LaunchType = " + launchType);
 		this.launchType			= launchType;
 		// look for codeBase path
 		arg						= pop(argStack);
 		
 		switch (launchType)
 		{
 		case JNLP:
 			// next arg *should* be code base
 			if ((arg != null) && arg.endsWith("/"))
 			{					
 				// JNLP only! (as of now)
 				// right now this only works for http://
 				ParsedURL codeBase	= ParsedURL.getAbsolute(arg, "Setting up codebase");
 				this.setCodeBase(codeBase);
 				
 				// read meta-preferences and preferences from application data dir
 				File applicationDir	= PropertiesAndDirectories.thisApplicationDir();
 				ParsedURL applicationDataPURL					= new ParsedURL(applicationDir);
 				XmlTranslationException metaPrefSetException	= null;
 				ParsedURL metaPrefsPURL	= null;
 				try
 				{
 					ZipDownload.setDownloadProcessor(assetsDownloadProcessor());
 					Assets.downloadPreferencesZip("prefs", null, true);
 					File metaPrefsFile	= Assets.getPreferencesFile(METAPREFS_XML);
 					metaPrefsPURL	= new ParsedURL(metaPrefsFile);
 					metaPrefSet		= MetaPrefSet.load(metaPrefsFile, translationSpace);
 					println("OK: loaded MetaPrefs from " + metaPrefsFile);
 						//MetaPrefSet.load(metaPrefsPURL, translationSpace);
 						//(MetaPrefSet) ElementState.translateFromXML(Assets.getPreferencesFile("metaprefs.xml"), translationSpace);
 				} catch (XmlTranslationException e)
 				{
 					metaPrefSetException	= e;
 				}
				prefsPURL	= applicationDataPURL.getRelative("preferences/prefs.xml");
 
 	            //TODO for eunyee -- test for studies preference and download special studies preferences
 				// When the JNLP has more than two arguments (study case) -- eunyee
 				if( argStack.size() > 0 )
 				{
 					String prefServlet	= "";
 					if( arg.startsWith("http://") )
 					{
 						// PreferencesServlet
 						prefServlet = pop(argStack);	
 					
 						prefSet = requestPrefFromServlet(prefServlet, translationSpace);
 						if( prefSet == null )
 							error("not prefXML string returned from the servlet=" + prefServlet);
 					}
 
 
 				}
 				
 	            // from supplied URL instead of from here
 	            try
 				{
 	            	if( prefSet == null ) // Normal Case
 	            		prefSet 		= PrefSet.load(prefsPURL, translationSpace);
 	            	
 		            println("OK: Loaded Prefs from " + prefSet.translateToXML(true));
 					if (metaPrefSetException != null)
 					{
 						warning("Couldn't load MetaPrefs:");
 						metaPrefSetException.printTraceOrMessage(this, "MetaPrefs", metaPrefsPURL);
 						println("\tContinuing.");
 					}
 				} catch (XmlTranslationException e)
 				{
 					if (metaPrefSetException != null)
 					{
 						error("Can't load MetaPrefs or Prefs. Quitting.");
 						metaPrefSetException.printTraceOrMessage(this, "MetaPrefs", metaPrefsPURL);
 						e.printTraceOrMessage(this, "Prefs", prefsPURL);
 					}
 					else
 					{
 						// meta prefs o.k. we can continue
 						warning("Couldn't load Prefs:");
 						e.printTraceOrMessage(this, "Prefs", prefsPURL);
 						println("\tContinuing.");
 					}
 				}
 			}
 			else
 			{
 				error("No code base argument :-( Can't load preferences.");
 			}
 			break;
 		case ECLIPSE:
 		case JAR:
 			// NB: This gets executed even if arg was null!
 			File localCodeBasePath	= deriveLocalFileCodeBase(baseClass);
 			argStack.push(arg);
 
 			XmlTranslationException metaPrefSetException	= null;
 			File metaPrefsFile		= new File(localCodeBasePath, ECLIPSE_PREFS_DIR + METAPREFS_XML);
 			ParsedURL metaPrefsPURL	= new ParsedURL(metaPrefsFile);
 			try
 			{
 				metaPrefSet	= MetaPrefSet.load(metaPrefsPURL, translationSpace);
 	            println("OK: Loaded MetaPrefs from: " + metaPrefsFile);
 			} catch (XmlTranslationException e)
 			{
 				metaPrefSetException	= e;
 			}
 			
 			// now seek the path to an application specific xml preferences file
 			arg						= pop(argStack);
 			//if (arg == null)
 				//return;
 			if (arg != null)
 			{
 				// load preferences specific to this invocation
 				if (arg.endsWith(".xml"))
 				{
 					File appPrefsFile	= new File(localCodeBasePath, ECLIPSE_PREFS_DIR + arg);
 					ParsedURL prefsPURL	= new ParsedURL(appPrefsFile);
 		            try
					{            	
 						prefSet 		= PrefSet.load(prefsPURL, translationSpace);
 						if (metaPrefSetException != null)
 						{
 							warning("Couldn't load MetaPrefs:");
 							metaPrefSetException.printTraceOrMessage(this, "MetaPrefs", metaPrefsPURL);
 							println("\tContinuing.");
 						}
 						else
 				            println("OK: Loaded Prefs from: " + appPrefsFile);
 
 					} catch (XmlTranslationException e)
 					{
 						if (metaPrefSetException != null)
 						{
 							error("Can't load MetaPrefs or Prefs. Quitting.");
 							metaPrefSetException.printTraceOrMessage(this, "MetaPrefs", metaPrefsPURL);
 							e.printStackTrace();
 							throw e;
 						}
 						else
 						{
 							// meta prefs o.k. we can continue without having loaded Prefs now
 							e.printTraceOrMessage(this, "Couldn't load Prefs", prefsPURL);
 							println("\tContinuing.");
 						}
 					}
 				}
 				else
 					argStack.push(arg);
 			}
 			else
 				argStack.push(arg);	// let the next code handle returning.
 			break;
 		}
 	}
 	
 	/**
 	 * Get the user.dir property. Form a path from it, ending in slash.
 	 * See if there is path within that that includes the package of baseClass.
 	 * If so, remove that component from the path.
 	 * 
 	 * Form a File from this path, and a ParsedURL from the file.
 	 * Set codeBase to this ParsedURL.
 	 * 
 	 * @param baseClass		Class of the subclass of this that is the main program that was executed.
 	 * 
 	 * @return				File that corresponds to the path of the local codeBase.
 	 */
 	private File deriveLocalFileCodeBase(Class baseClass)
 	{
 		// setup codeBase
 		if (baseClass == null)
 			baseClass			= this.getClass();
 		
 		Package basePackage		= baseClass.getPackage();
 		String packageName		= basePackage.getName();
 		String packageNameAsPath= packageName.replace('.', Files.sep);
 
 		String pathName			= System.getProperty("user.dir") + Files.sep;
 		File path				= new File(pathName);
 		String pathString		= path.getAbsolutePath();
 		
 		//println("looking for " + packageNameAsPath +" in " + pathString);
 
 		int packageIndex		= pathString.lastIndexOf(packageNameAsPath);
 		if (packageIndex != -1)
 		{
 			pathString			= pathString.substring(0, packageIndex);
 			path				= new File(pathString + Files.sep);
 		}
 
 		codeBase				= new ParsedURL(path);
 		println("codeBase="+codeBase);
 		return path;
 	}
 
 	/**
      * @see ecologylab.appframework.Environment#runtimeEnv()
      */
     public int runtimeEnv()
     { return APPLICATION;}
     
     /**
      * @see ecologylab.appframework.Environment#status(String)
      */
     public void showStatus(String s) 
     {
 	System.out.println(s);
     }
 
 	/**
 	 * @see ecologylab.appframework.Environment#status(String)
 	 */
 	public void status(String msg) 
 	{
 	if (msg != null)
 	    showStatus(msg);
 	}
 
 	/**
 	 * @see ecologylab.appframework.Environment#lookupStringPreference(String)
 	 */
 	public String lookupStringPreference(String name)
 	{
 //		return properties.getProperty(name);
 		return ((Preference) preferencesRegistry().lookupObject(name)).getValue();
 	}
 
 	/**
 	 * @see ecologylab.appframework.Environment#codeBase()
 	 * return the path to root of the
 	 */
 	public ParsedURL codeBase() 
 	{
 		return codeBase;
 	}
 
 	/**
 	 * @see ecologylab.appframework.Environment#docBase()
 	 * return the current working directory of the application
 	 * which is "c:\web\code\java\cm"
 	 */
 	public ParsedURL docBase()
 	{
 		ParsedURL purl = new ParsedURL(new File(System.getProperty("user.dir")));
 		return purl;
 	}
 	
 	public ParsedURL preferencesDir()
 	{
 		ParsedURL codeBase = codeBase();
 		ParsedURL purl = codeBase.getRelative(ECLIPSE_PREFS_DIR, "forming preferences dir");
 		return purl;
 	}
 	
 	static final String FIREFOX_PATH_WINDOWS	= "C:\\Program Files\\Mozilla Firefox\\firefox.exe";
 //TODO -- use "open" on the mac!!!
 	static final String FIREFOX_PATH_MAC		= "/Applications/Firefox.app/Contents/MacOS/firefox";
 //	static final String FIREFOX_PATH_MAC		= null;
 	static final String SAFARI_PATH_MAC			= "/Applications/Safari.app/Contents/MacOS/Safari";
 	static final String IE_PATH_WINDOWS			= "C:\\Program Files\\Internet Explorer\\IEXPLORE.EXE";
 	
 	static String browserPath;
 	
 	/**
 	 * Get the operating system dependent path to a suitable web browser for navigating to a web page.
 	 * This is also dependent on what web browser(s) the user has installed.
 	 * In particular, we use Firefox if it is in its normal place!
 	 * 
 	 * @return	String that specifies the OS and browser-specific command.
 	 */
 	static String getBrowserPath()
 	{
 		int os				= PropertiesAndDirectories.os();
 		String result		= browserPath;
 		if (result == null)
 		{
 			switch (os)
 			{
 			case PropertiesAndDirectories.WINDOWS:
 				if (!Pref.lookupBoolean("navigate_with_ie"))
 					result		= FIREFOX_PATH_WINDOWS;
 				if (result != null)
 				{
 					File existentialTester	= new File(result);
 					if (!existentialTester.exists())
 					{
 						result					= IE_PATH_WINDOWS;
 						existentialTester	= new File(result);
 						if (!existentialTester.exists())
 							result			= null;
 					}
 				}
 				break;
 			case PropertiesAndDirectories.MAC:
 				result		= "/usr/bin/open";
 				break;
 			default:
 				error(PropertiesAndDirectories.getOsName(), "go(ParsedURL) not supported");
 				break;				
 			}
 			if (result != null)
 			{
 				browserPath			= result;
 			}
 		}
 		return result;
 	}
 	
 	static String[]			cachedNavigateArgs;
 	
 	/**
 	 * Get the operating system dependent path to a suitable web browser for navigating to a web page.
 	 * This is also dependent on what web browser(s) the user has installed.
 	 * In particular, we use Firefox if it is in its normal place!
 	 * 
 	 * @return	String that specifies the OS and browser-specific command.
 	 */
 	static String[] getNavigateArgs()
 	{
 		int os				= PropertiesAndDirectories.os();
 		String[] result		= cachedNavigateArgs;
 		if (result == null)
 		{
 			switch (os)
 			{
 			case PropertiesAndDirectories.WINDOWS:
 				String path		= null;
 				if (!Preference.lookupBoolean("navigate_with_ie"))
 					path		= FIREFOX_PATH_WINDOWS;
 				if (path != null)
 				{
 					File existentialTester	= new File(path);
 					if (existentialTester.exists())
 					{	// cool! firefox
 						result		= new String[3];
 						result[0]	= path;
 						result[1]	= "-new-tab";
 					}
 				}
 				if (result == null)
 				{
 					path					= IE_PATH_WINDOWS;
 					File existentialTester	= new File(path);
 					if (existentialTester.exists())
 					{
 						result		= new String[2];
 						result[0]	= path;
 					}
 				}
 				break;
 			case PropertiesAndDirectories.MAC:
 				result		= new String[4];
 				result[0]	= "/usr/bin/open";
 				result[1]	= "-a";
 				result[2]	= "firefox";
 				break;
 			default:
 				error(PropertiesAndDirectories.getOsName(), "go(ParsedURL) not supported");
 				break;				
 			}
 			if (result != null)
 			{
 				cachedNavigateArgs	= result;
 			}
 		}
 		return result;
 	}
 	/**
 	 * Navigate to the purl using the best browser we can find.
 	 * 
 	 * @param purl
 	 * @param frame
 	 */
 	public void navigate(ParsedURL purl, String frame)
 	{
 		String[] navigateArgs	= getNavigateArgs();
 		if (navigateArgs != null)
 		{
 			String purlString	= purl.toString();
 			int numArgs			= navigateArgs.length;
 			navigateArgs[numArgs - 1]	= purlString;
 			StringBuilder sb	= new StringBuilder();
 			for (int i=0; i<numArgs; i++)
 				sb.append(navigateArgs[i]).append(' ');
 			Debug.println("navigate: " + navigateArgs.toString());
 			try 
 			{
 				Process p = Runtime.getRuntime().exec(navigateArgs);
 			} catch (IOException e)
 			{
 				error("navigate() - caught exception: ");
 				e.printStackTrace();
 			}
 		}
 		else
 			error("navigate() - Can't find browser to navigate to.");
 	}
 
 	public int browser()
 	{
 	   return APPLICATION;
 	}
     /**
      * Called at the end of an invocation. Calls System.exit(code).
      * 
      * @param	code -- 0 for normal. other values are application specific.
      */
     public void exit(int code)
     {
     	System.exit(code);
     }
     
     //---------------------- not Environment Code - other -----------------------------------
 	/**
 	 * Form the parameter file path.
 	 * Use the 0th argument if there is one, to find a file in config.
 	 * If not, use config/interface/params.txt.
 	 * 
 	 * @param args
 	 * @return	preferences file path
 	 */
 	public static String preferencesFileRelativeFromArg0(String[] args) 
 	{
 		if ((args == null) || (args.length == 0))
 			return null;
 		String arg0	= args[0];
 		String lc	= arg0.toLowerCase();
 		
 		return lc.endsWith("xml") ? (ECLIPSE_PREFS_DIR + args[0]) : null;
 	}
 
 	/**
 	 * Set the codebase for the application.
 	 * Should only be done at startup.
 	 */
 	public void setCodeBase(ParsedURL codeBase) 
 	{
 		this.codeBase = codeBase;
 	}
 	/**
 	 * @return Returns the preferencesRegistry.
 	 */
 	public static ObjectRegistry preferencesRegistry()
 	{
 		return Environment.the.preferencesRegistry();
 	}
 	/**
 	 * Find a complex object set in preferences.
 	 * 
 	 * @param name
 	 * @return	ElementState preference object.
 	 */
 	public static ElementState lookupElementStatePreference(String name)
 	{
 		return (ElementState) preferencesRegistry().lookupObject(name);
 	}
 	
 	static <T> T pop(Stack<T> stack)
 	{
 		return stack.isEmpty() ? null : stack.pop();
 	}
 	
 	static <T> void push(Stack<T> stack, T stuff)
 	{
 		if (stuff != null)
 			stack.push(stuff);
 	}
 	/**
 	 * Translation space used to parse Preferences for this Application.
 	 * 
 	 * @return	TranslationSpace in use for parsing Preferences
 	 */
 	public TranslationSpace translationSpace()
 	{
 		return translationSpace;
 	}
 	
 	/**
 	 * The required version number for the MetaPrefs asset.
 	 * 
 	 * @return	0 by default -- ignore version. 
 	 */
 	public float metaPrefsAssetVerison()
 	{
 		return AssetsState.IGNORE_VERSION;
 	}
 	
 	public DownloadProcessor assetsDownloadProcessor()
 	{
 		return new SimpleDownloadProcessor();
 	}
 	
 	/**
 	 * Create and show an editor for preferences, iff the MetaPrefSet and PrefSet are non-null.
 	 * If the PrefSet is null, a new empty one will be created for the editor to use.
 	 * 
 	 * @return
 	 */
 	public PrefsEditor createPrefsEditor()
 	{
 		PrefsEditor result	= null;
 		if (metaPrefSet != null)
 		{
 			if (prefSet == null)
 				prefSet		= new PrefSet();
 			result			= new PrefsEditor(metaPrefSet, prefSet, prefsPURL, false);
 		}
 		return result;
 	}
 	
 	protected LaunchType launchType()
 	{
 		return this.launchType;
 	}
 }
