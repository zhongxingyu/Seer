 package ecologylab.appframework;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLDecoder;
 import java.util.Stack;
 
 import ecologylab.appframework.types.prefs.MetaPrefSet;
 import ecologylab.appframework.types.prefs.MetaPrefsTranslationScope;
 import ecologylab.appframework.types.prefs.Pref;
 import ecologylab.appframework.types.prefs.PrefSet;
 import ecologylab.collections.Scope;
 import ecologylab.generic.Debug;
 import ecologylab.io.Assets;
 import ecologylab.io.AssetsRoot;
 import ecologylab.io.Files;
 import ecologylab.net.ParsedURL;
 import ecologylab.platformspecifics.FundamentalPlatformSpecifics;
 import ecologylab.serialization.ClassDescriptor;
 import ecologylab.serialization.SIMPLTranslationException;
 import ecologylab.serialization.SimplTypesScope;
 import ecologylab.serialization.XMLTranslationExceptionTypes;
 import ecologylab.serialization.formatenums.StringFormat;
 
 /**
  * An instance of Environment, which is an application, rather than an applet, or a servlet. The
  * Environment mechanism is used to enable the provision of contextual runtime configuration
  * parameter services in a way that is independent of the deployment structure.
  * 
  * The SingletonApplicationEnvironment differs from ApplicationEnvironment in that it sets the
  * static Environment.the (among other static references). It CANNOT be used in a situation where
  * multiple ApplicationEnvironments will operate in the same JVM. It does, however, simplify the use
  * of Prefs and other statically accessed application components.
  * 
  * @author Andruid
  * @author Zachary O. Toups (zach@ecologylab.net)
  */
 public class SingletonApplicationEnvironment extends ApplicationEnvironment implements Environment,
 		XMLTranslationExceptionTypes, ApplicationPropertyNames
 {
 	public static final String		SCREEN_SIZE										= "screen_size";
 
 	private static final String		METAPREFS_XML									= "metaprefs.xml";
 
 	private static boolean				inUse													= false;
 
 	/** Set of <code>MetaPref</code>s that describe preferences and provide default values. */
 	MetaPrefSet										metaPrefSet;
 
 	// must initialize this before subsequent lookup by scope name.
 	static final SimplTypesScope	META_PREFS_TRANSLATION_SCOPE	= MetaPrefsTranslationScope.get();
 
 	public static enum WindowSize
 	{
 		PASS_PARAMS, QUARTER_SCREEN, ALMOST_HALF, NEAR_FULL, FULL_SCREEN
 	}
 	
   public static final String	TOP_LEVEL_HEIGHT	= "topheight";
   public static final String	TOP_LEVEL_WIDTH		= "topwidth";
 
 	static boolean firefoxExists = false;
 
 	/**
 	 * Create an ApplicationEnvironment. Create an empty properties object for application parameters.
 	 * <p/>
 	 * No command line argument is processed. Only default preferences are loaded, and processed with
 	 * the default TranslationSpace.
 	 * 
 	 * @param args
 	 *          The args array, which is treated as a stack with optional entries. They are: *) JNLP
 	 *          -- if that is the launch method *) preferences file if you are running in eclipse.
 	 *          Relative to CODEBASE/config/preferences/ *) graphics_device (screen number) *)
 	 *          screen_size (used in TopLevel -- 1 - quarter; 2 - almost half; 3; near full; 4 full)
 	 * @throws SIMPLTranslationException
 	 */
 	public SingletonApplicationEnvironment(String applicationName) throws SIMPLTranslationException
 	{
 		this(null, applicationName, null);
 	}
 
 	/**
 	 * Create an ApplicationEnvironment. Load preferences from XML file founds in the
 	 * config/preferences directory. Default preferences will be loaded from preferences.xml. If there
 	 * is a 0th command line argument, that is the name of an additional preferences file.
 	 * 
 	 * @param applicationName
 	 * @param translationScope
 	 *          TranslationSpace used for translating preferences XML. If this is null,
 	 *          {@link ecologylab.oodss.messages.DefaultServicesTranslations
 	 *          ecologylab.oodss.message.DefaultServicesTranslations} will be used.
 	 * @param args
 	 *          The args array, which is treated as a stack with optional entries. They are: *) JNLP
 	 *          -- if that is the launch method *) preferences file if you are running in eclipse.
 	 *          Relative to CODEBASE/config/preferences/ *) graphics_device (screen number) *)
 	 *          screen_size (used in TopLevel -- 1 - quarter; 2 - almost half; 3; near full; 4 full)
 	 * @param prefsAssetVersion
 	 *          TODO
 	 * @throws SIMPLTranslationException
 	 */
 	public SingletonApplicationEnvironment(	String applicationName,
 																					SimplTypesScope translationScope,
 																					String args[],
 																					float prefsAssetVersion) throws SIMPLTranslationException
 	{
 		this(applicationName, translationScope, (SimplTypesScope) null, args, prefsAssetVersion);
 	}
 
 	/**
 	 * Create an ApplicationEnvironment. Load preferences from XML file founds in the
 	 * config/preferences directory. Default preferences will be loaded from preferences.xml. If there
 	 * is a 0th command line argument, that is the name of an additional preferences file.
 	 * 
 	 * @param applicationName
 	 * @param translationScope
 	 *          TranslationSpace used for translating preferences XML. If this is null,
 	 *          {@link ecologylab.oodss.messages.DefaultServicesTranslations
 	 *          ecologylab.oodss.message.DefaultServicesTranslations} will be used.
 	 * @param customPrefs
 	 *          TODO
 	 * @param args
 	 *          The args array, which is treated as a stack with optional entries. They are: *) JNLP
 	 *          -- if that is the launch method *) preferences file if you are running in eclipse.
 	 *          Relative to CODEBASE/config/preferences/ *) graphics_device (screen number) *)
 	 *          screen_size (used in TopLevel -- 1 - quarter; 2 - almost half; 3; near full; 4 full)
 	 * @param prefsAssetVersion
 	 *          TODO
 	 * @throws SIMPLTranslationException
 	 */
 	public SingletonApplicationEnvironment(	String applicationName,
 																					SimplTypesScope translationScope,
 																					Class<? extends Pref<?>>[] customPrefs,
 																					String args[],
 																					float prefsAssetVersion) throws SIMPLTranslationException
 	{
 		this(applicationName, (Scope<?>) null, translationScope, customPrefs, args, prefsAssetVersion);
 	}
 
 	/**
 	 * 
 	 * @param applicationName
 	 * @param translationScope
 	 *          TranslationSpace used for translating preferences XML. If this is null,
 	 *          {@link ecologylab.oodss.messages.DefaultServicesTranslations
 	 *          ecologylab.oodss.message.DefaultServicesTranslations} will be used.
 	 * @param sessionScope
 	 * @param customPrefs
 	 * @param args
 	 *          The args array, which is treated as a stack with optional entries. They are: *) JNLP
 	 *          -- if that is the launch method *) preferences file if you are running in eclipse.
 	 *          Relative to CODEBASE/config/preferences/ *) graphics_device (screen number) *)
 	 *          screen_size (used in TopLevel -- 1 - quarter; 2 - almost half; 3; near full; 4 full)
 	 * @param prefsAssetVersion
 	 * @throws SIMPLTranslationException
 	 */
 	public SingletonApplicationEnvironment(	String applicationName,
 																					Scope<?> sessionScope,
 																					SimplTypesScope translationScope,
 																					Class<? extends Pref<?>>[] customPrefs,
 																					String args[],
 																					float prefsAssetVersion) throws SIMPLTranslationException
 	{
 		this(	(Class<?>) null,
 					applicationName,
 					sessionScope,
 					translationScope,
 					prefsClassArrayToTranslationScope(customPrefs),
 					args,
 					prefsAssetVersion);
 	}
 
 	/**
 	 * Create an ApplicationEnvironment. Load preferences from XML file founds in the
 	 * config/preferences directory. Default preferences will be loaded from preferences.xml. If there
 	 * is a 0th command line argument, that is the name of an additional preferences file.
 	 * 
 	 * @param applicationName
 	 * @param translationScope
 	 *          TranslationSpace used for translating preferences XML. If this is null,
 	 *          {@link ecologylab.oodss.messages.DefaultServicesTranslations
 	 *          ecologylab.oodss.message.DefaultServicesTranslations} will be used.
 	 * @param customPrefsTranslationScope
 	 *          TODO
 	 * @param args
 	 *          The args array, which is treated as a stack with optional entries. They are: *) JNLP
 	 *          -- if that is the launch method *) preferences file if you are running in eclipse.
 	 *          Relative to CODEBASE/config/preferences/ *) graphics_device (screen number) *)
 	 *          screen_size (used in TopLevel -- 1 - quarter; 2 - almost half; 3; near full; 4 full)
 	 * @param prefsAssetVersion
 	 *          TODO
 	 * @throws SIMPLTranslationException
 	 */
 	public SingletonApplicationEnvironment(	String applicationName,
 																					SimplTypesScope translationScope,
 																					SimplTypesScope customPrefsTranslationScope,
 																					String args[],
 																					float prefsAssetVersion) throws SIMPLTranslationException
 	{
 		this(	(Class<?>) null,
 					applicationName,
 					(Scope<?>) null,
 					translationScope,
 					customPrefsTranslationScope,
 					args,
 					prefsAssetVersion);
 	}
 
 	/**
 	 * Create an ApplicationEnvironment. Load preferences from XML files found in the
 	 * config/preferences directory. Default preferences will be loaded from preferences.xml. If there
 	 * is a 0th command line argument, that is the name of an additional preferences file.
 	 * <p/>
 	 * The default TranslationSpace, from
 	 * {@link ecologylab.oodss.messages.DefaultServicesTranslations
 	 * ecologylab.oodss.message.DefaultServicesTranslations} will be used.
 	 * 
 	 * @param applicationName
 	 *          {@link ecologylab.oodss.messages.DefaultServicesTranslations
 	 *          ecologylab.oodss.message.DefaultServicesTranslations} will be used.
 	 * @param args
 	 *          The args array, which is treated as a stack with optional entries. They are: *) JNLP
 	 *          -- if that is the launch method *) preferences file if you are running in eclipse.
 	 *          Relative to CODEBASE/config/preferences/ *) graphics_device (screen number) *)
 	 *          screen_size (used in TopLevel -- 1 - quarter; 2 - almost half; 3; near full; 4 full)
 	 * @throws SIMPLTranslationException
 	 */
 	public SingletonApplicationEnvironment(String applicationName, String args[])
 			throws SIMPLTranslationException
 	{
 		this(applicationName, (SimplTypesScope) null, (SimplTypesScope) null, args, 0);
 	}
 
 	/**
 	 * Create an ApplicationEnvironment. Get the base for finding the path to the "codeBase" by using
 	 * the package path of the baseClass passed in.
 	 * <p/>
 	 * Load preferences from XML file founds in the codeBase/config/preferences directory. Default
 	 * preferences will be loaded from preferences.xml. If there is a 0th command line argument, that
 	 * is the name of an additional preferences file.
 	 * <p/>
 	 * Also, sets the Assets cacheRoot to the applicationDir().
 	 * <p/>
 	 * The default TranslationSpace, from
 	 * {@link ecologylab.oodss.messages.DefaultServicesTranslations
 	 * ecologylab.oodss.message.DefaultServicesTranslations} will be used.
 	 * 
 	 * @param baseClass
 	 *          Used for computing codeBase property.
 	 * @param applicationName
 	 * @param args
 	 *          The args array, which is treated as a stack with optional entries. They are: *) JNLP
 	 *          -- if that is the launch method *) preferences file if you are running in eclipse.
 	 *          Relative to CODEBASE/config/preferences/ *) graphics_device (screen number) *)
 	 *          screen_size (used in TopLevel -- 1 - quarter; 2 - almost half; 3; near full; 4 full
 	 * @throws SIMPLTranslationException
 	 */
 	public SingletonApplicationEnvironment(Class<?> baseClass, String applicationName, String args[])
 			throws SIMPLTranslationException
 	{
 		this(baseClass, applicationName, null, null, null, args, 0);
 	}
 
 	/**
 	 * Additional constructor to hold the session scope for post processing loaded preferences.
 	 * 
 	 * @param applicationName
 	 * @param sessionScope
 	 */
 	public SingletonApplicationEnvironment(	Class<?> baseClass,
 																					String applicationName,
 																					SimplTypesScope translationScope,
 																					String args[],
 																					float prefsAssetVersion) throws SIMPLTranslationException
 	{
 		this(baseClass, applicationName, null, translationScope, null, args, prefsAssetVersion);
 	}
 
 	/**
 	 * Create an ApplicationEnvironment.
 	 * <p/>
 	 * Treats the args array like a stack. If any args are missing (based on their format), they are
 	 * skipped.
 	 * <p/>
 	 * The first arg we seek is codeBase. This is a path that ends in slash. It may be a local
 	 * relative path, or a URL-based absolute path.
 	 * <p/>
 	 * The next possible arg is a preferences file. This ends with .xml.
 	 * <p/>
 	 * The next 2 possible args are integers, for graphicsDev and screenSize. graphics_device (screen
 	 * number) to display window. count from 0. screenSize used in TopLevel -- 1 - quarter; 2 - almost
 	 * half; 3; near full; 4 full
 	 * <p/>
 	 * Get the base for finding the path to the "codeBase" by using the package path of the baseClass
 	 * passed in.
 	 * <p/>
 	 * Load preferences from XML file founds in the codeBase/config/preferences directory. Default
 	 * preferences will be loaded from preferences.xml. If there is a 0th command line argument, that
 	 * is the name of an additional preferences file.
 	 * <p/>
 	 * Also, sets the Assets cacheRoot to the applicationDir().
 	 * <p/>
 	 * The default TranslationSpace, from
 	 * {@link ecologylab.oodss.messages.DefaultServicesTranslations
 	 * ecologylab.oodss.message.DefaultServicesTranslations} will be used.
 	 * 
 	 * @param baseClass
 	 *          Used for computing codeBase property.
 	 * @param applicationName
 	 *          Name of the application.
 	 * @param translationScope
 	 *          TranslationSpace used for translating preferences XML. If this is null,
 	 *          {@link ecologylab.oodss.messages.DefaultServicesTranslations
 	 *          ecologylab.oodss.message.DefaultServicesTranslations} will be used.
 	 * @param args
 	 *          The args array, which is treated as a stack with optional entries. They are: *) JNLP
 	 *          -- if that is the launch method *) preferences file if you are running in eclipse.
 	 *          Relative to CODEBASE/config/preferences/ *) graphics_device (screen number) *)
 	 *          screen_size (used in TopLevel -- 1 - quarter; 2 - almost half; 3; near full; 4 full)
 	 * @param prefsAssetVersion
 	 *          TODO
 	 * @throws SIMPLTranslationException
 	 */
 	public SingletonApplicationEnvironment(	Class<?> baseClass,
 																					String applicationName,
 																					Scope sessionScope,
 																					SimplTypesScope translationScope,
 																					String args[],
 																					float prefsAssetVersion) throws SIMPLTranslationException
 	{
 		this(baseClass, applicationName, sessionScope, translationScope, null, args, prefsAssetVersion);
 	}
 
 	/**
 	 * Create an ApplicationEnvironment.
 	 * <p/>
 	 * Treats the args array like a stack. If any args are missing (based on their format), they are
 	 * skipped.
 	 * <p/>
 	 * The first arg we seek is codeBase. This is a path that ends in slash. It may be a local
 	 * relative path, or a URL-based absolute path.
 	 * <p/>
 	 * The next possible arg is a preferences file. This ends with .xml.
 	 * <p/>
 	 * The next 2 possible args are integers, for graphicsDev and screenSize. graphics_device (screen
 	 * number) to display window. count from 0. screenSize used in TopLevel -- 1 - quarter; 2 - almost
 	 * half; 3; near full; 4 full
 	 * <p/>
 	 * Get the base for finding the path to the "codeBase" by using the package path of the baseClass
 	 * passed in.
 	 * <p/>
 	 * Load preferences from XML file founds in the codeBase/config/preferences directory. Default
 	 * preferences will be loaded from preferences.xml. If there is a 0th command line argument, that
 	 * is the name of an additional preferences file.
 	 * <p/>
 	 * Also, sets the Assets cacheRoot to the applicationDir().
 	 * <p/>
 	 * The default TranslationSpace, from
 	 * {@link ecologylab.oodss.messages.DefaultServicesTranslations
 	 * ecologylab.oodss.message.DefaultServicesTranslations} will be used.
 	 * 
 	 * @param baseClass
 	 *          Used for computing codeBase property.
 	 * @param applicationName
 	 *          Name of the application.
 	 * @param translationScope
 	 *          TranslationSpace used for translating preferences XML. If this is null,
 	 *          {@link ecologylab.oodss.messages.DefaultServicesTranslations
 	 *          ecologylab.oodss.message.DefaultServicesTranslations} will be used.
 	 * @param customPrefs
 	 *          An array of Pref subclasses that are used for this specific application. These classes
 	 *          will be automatically composed into a special translation scope used for translating
 	 *          prefs for the application. Note that translationScope is NOT used for translating the
 	 *          application prefs, but is still required for other translations in the application.
 	 * @param args
 	 *          The args array, which is treated as a stack with optional entries. They are: *) JNLP
 	 *          -- if that is the launch method *) preferences file if you are running in eclipse.
 	 *          Relative to CODEBASE/config/preferences/ *) graphics_device (screen number) *)
 	 *          screen_size (used in TopLevel -- 1 - quarter; 2 - almost half; 3; near full; 4 full)
 	 * @param prefsAssetVersion
 	 *          TODO
 	 * @throws SIMPLTranslationException
 	 */
 	public SingletonApplicationEnvironment(	Class<?> baseClass,
 																					String applicationName,
 																					Scope<?> sessionScope,
 																					SimplTypesScope translationScope,
 																					SimplTypesScope customPrefsTranslationScope,
 																					String args[],
 																					float prefsAssetVersion) throws SIMPLTranslationException
 	{
 		super(baseClass,
 					applicationName,
 					sessionScope,
 					translationScope,
 					customPrefsTranslationScope,
 					args,
 					prefsAssetVersion);
 
 		inUse = true;
 
 		// this is the one and only singleton Environment
 		Environment.the.set(this);
 
 		PropertiesAndDirectories.setApplicationName(applicationName);
 	}
 
 	/**
 	 * Sets the applicationName for PropertiesAndDirectories.
 	 * 
 	 * @see ecologylab.appframework.ApplicationEnvironment#setApplicationName(java.lang.String)
 	 */
 	@Override
 	protected void setApplicationName(String applicationName)
 	{
 		super.setApplicationName(applicationName);
 
 		PropertiesAndDirectories.setApplicationName(applicationName);
 	}
 
 	@Override
 	protected void processArgsAndPrefs(Class<?> baseClass, SimplTypesScope translationScope,
 			float prefsAssetVersion) throws SIMPLTranslationException
 	{
 		String arg;
 		processPrefs(baseClass, translationScope, argStack, prefsAssetVersion);
 
 		Debug.initialize();
 
 		arg = pop(argStack);
 		if (arg == null)
 			return;
 		try
 		{
 			int screenNum = Integer.parseInt(arg);
 			Pref.useAndSetPrefInt("graphics_device", screenNum);
 
 		}
 		catch (NumberFormatException e)
 		{
 			argStack.push(arg);
 		}
 		try
 		{
 			arg = pop(argStack);
 			if (arg == null)
 				return;
 			Pref.useAndSetPrefInt(SCREEN_SIZE, WindowSize.valueOf(arg.toUpperCase()).ordinal());
 		}
 		catch (IllegalArgumentException e)
 		{
 			argStack.push(arg);
 		}
 		if (Pref.lookupInt(SCREEN_SIZE) == 0)
 		{
 			try
 			{
 				arg = pop(argStack);
 				if (arg == null)
 					return;
 				Pref.useAndSetPrefInt(TOP_LEVEL_WIDTH, Integer.parseInt(arg));
 				
 				arg = pop(argStack);
 				if (arg == null)
 					return;
 				Pref.useAndSetPrefInt(TOP_LEVEL_HEIGHT, Integer.parseInt(arg));
 			}
 			catch (IllegalArgumentException e)
 			{
 				argStack.push(arg);
 			}
 		}
 	}
 
 	/**
 	 * request User's prefSet from the preferenceServlet and return the prefSetXML string.
 	 * 
 	 * @author eunyee
 	 * @param prefServlet
 	 * @param translationScope
 	 *          TODO
 	 * @param uid
 	 * @return
 	 */
 	@Override
 	protected PrefSet requestPrefFromServlet(String prefServlet, SimplTypesScope translationScope)
 	{
 		System.out.println("retrieving preferences set from servlet: " + prefServlet);
 		/*
 		 * try { ParsedURL purl = new ParsedURL(new URL(prefServlet));
 		 * 
 		 * PrefSet prefSet = (PrefSet) ElementState.translateFromXML(purl, PrefTranslations.get());
 		 * return prefSet; } catch (MalformedURLException e1) { // TODO Auto-generated catch block
 		 * e1.printStackTrace(); } catch (XmlTranslationException e) { // TODO Auto-generated catch
 		 * block e.printStackTrace(); } return null;
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
 				prfs = PrefSet.loadFromCharSequence(prefSetXML, translationScope);
 				System.out.println("Prefs loaded From Servlet:: ");
 				if (prfs != null)
 					SimplTypesScope.serialize(prfs, System.out, StringFormat.XML);
 					
 				System.out.println(" --- End Prefs");
 			}
 			catch (SIMPLTranslationException e)
 			{
 				e.printStackTrace();
 			}
 			in.close();
 
 			return prfs;
 		}
 		catch (IOException e)
 		{
 			warning("not a <pref_set> servlet URL: " + prefServlet);
 		}
 		return null;
 	}
 
 	/**
 	 * Load MetaPrefs and Prefs, if possible
 	 * 
 	 * @param baseClass
 	 * @param translationScope
 	 * @param argStack
 	 * @param prefsAssetVersion
 	 *          TODO
 	 * @throws SIMPLTranslationException
 	 */
 	private void processPrefs(Class<?> baseClass, SimplTypesScope translationScope,
 			Stack<String> argStack, float prefsAssetVersion) throws SIMPLTranslationException
 	{
 		LaunchType launchType = LaunchType.ECLIPSE; // current default
 
 		// look for launch method identifier in upper case
 		String arg = pop(argStack);
 
 		if (arg != null)
 		{
 			String uc = arg.toUpperCase();
 			if ("JNLP".equals(uc))
 			{ // tells us how we were launched: e.g., JNLP, ECLIPSE, ...
 				launchType = LaunchType.JNLP;
 			}
 			else if ("STUDIES".equals(uc))
 			{
 				launchType = LaunchType.STUDIES;
 			}
 			else
 			{
 				// TODO -- recognize JAR here !!!
 				argStack.push(arg);
 			}
 			LAUNCH_TYPE_PREF.setValue(launchType);
 		}
 		println("LaunchType = " + launchType);
 		this.launchType = launchType;
 		// look for codeBase path
 		arg = pop(argStack);
 
 		// read perhaps meta-preferences and surely preferences from application data dir
 		File applicationDir = PropertiesAndDirectories.thisApplicationDir();
 
 		ParsedURL applicationDataPURL = new ParsedURL(applicationDir);
 		prefsPURL = applicationDataPURL.getRelative("preferences/prefs.xml");
 		debugA("prefsPURL= " + prefsPURL);
 
 		System.out.println("arg: " + arg);
 
 		switch (launchType)
 		{
 		case STUDIES:
 		case JNLP:
 			// next arg *should* be code base
 			if ((arg != null) && arg.endsWith("/"))
 			{
 				// JNLP only! (as of now)
 				// right now this only works for http://
 				ParsedURL codeBase = ParsedURL.getAbsolute(arg, "Setting up codebase");
 				this.setCodeBase(codeBase);
 
 				// XXX is this right?
 				final String host = codeBase.host();
 				if ("localhost".equals(host) || "127.0.0.1".equals(host))
 				{
 					debug("launched from localhost. must be a developer.");
 					LAUNCH_TYPE_PREF.setValue(LaunchType.LOCAL_JNLP);
 				}
 
 				SIMPLTranslationException metaPrefSetException = null;
 				ParsedURL metaPrefsPURL = null;
 				try
 				{
 					AssetsRoot prefAssetsRoot = new AssetsRoot(this, PREFERENCES, null);
 					File metaPrefsFile = Assets.getAsset(	prefAssetsRoot,
 																								METAPREFS_XML,
 																								"prefs",
 																								null,
 																								false,
 																								prefsAssetVersion);
 					metaPrefsPURL = new ParsedURL(metaPrefsFile);
 					metaPrefSet = MetaPrefSet.load(metaPrefsFile, translationScope);
 					println("OK: loaded MetaPrefs from " + metaPrefsFile);
 				}
 				catch (SIMPLTranslationException e)
 				{
 					metaPrefSetException = e;
 				}
 				catch (Exception e)
 				{
 					e.printStackTrace();
 				}
 				// from supplied URL instead of from here
 				try
 				{
 					debugA("Considering prefSet=" + prefSet + "\tprefsPURL=" + prefsPURL);
 					if (prefSet == null) // Normal Case
 					{
 						prefSet = PrefSet.load(prefsPURL, translationScope);
 						if (prefSet != null)
 							println("OK: Loaded Prefs from " + prefsPURL);
 						else
 							println("No Prefs to load from " + prefsPURL);
 					}
 					if (metaPrefSetException != null)
 					{
 						warning("Couldn't load MetaPrefs:");
 						metaPrefSetException.printTraceOrMessage(this, "MetaPrefs", metaPrefsPURL);
 						println("\tContinuing.");
 					}
 				}
 				catch (SIMPLTranslationException e)
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
 
 				debugA("argStack.size() =  " + argStack.size());
 				if (argStack.size() > 0)
 				{
 					String prefSpec = "";
 					if (arg.startsWith("http://"))
 					{
 						// PreferencesServlet
 						prefSpec = pop(argStack);
 
 						if (prefSpec != null)
 						{
 							// load URLEncoded prefs XML straight from the argument
 							PrefSet JNLPPrefSet = loadPrefsFromJNLP(prefSpec);
 
 							if (JNLPPrefSet != null)
 							{
 								if (prefSet == null)
 									prefSet = JNLPPrefSet;
 								else
 									prefSet.append(JNLPPrefSet);
 							}
 							else
 							{ // if we got args straight from jnlp, then continue
 								if (JNLPPrefSet != null)
 									prefSpec = pop(argStack);
 
 								if (prefSpec != null)
 								{
 									PrefSet servletPrefSet = requestPrefFromServlet(prefSpec, translationScope);
 									if (servletPrefSet == null)
 										error("incorrect prefXML string returned from the servlet=" + prefSpec);
 									else
 									{
 										if (prefSet == null)
 											prefSet = servletPrefSet;
 										else
 											prefSet.append(servletPrefSet);
 									}
 								}
 							}
 						}
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
 			File localCodeBasePath = deriveLocalFileCodeBase(baseClass); // sets codeBase()!
 			argStack.push(arg);
 
 			// AssetsRoot prefAssetsRoot = new AssetsRoot(Assets.getAssetsRoot().getRelative(PREFERENCES),
 			// Files.newFile(PropertiesAndDirectories.thisApplicationDir(), PREFERENCES));
 			// Assets.downloadZip(prefAssetsRoot, "prefs", null, false, prefsAssetVersion);
 
 			SIMPLTranslationException metaPrefSetException = null;
 			File metaPrefsFile = new File(localCodeBasePath, ECLIPSE_PREFS_DIR + METAPREFS_XML);
 			ParsedURL metaPrefsPURL = new ParsedURL(metaPrefsFile);
 			try
 			{
 				metaPrefSet = MetaPrefSet.load(metaPrefsPURL, translationScope);
 				println("OK: Loaded MetaPrefs from: " + metaPrefsFile);
 			}
 			catch (SIMPLTranslationException e)
 			{
 				metaPrefSetException = e;
 			}
 
 			// load the application dir prefs from this machine
 			// (e.g., c:\Documents and Settings\andruid\Application
 			// Data\combinFormation\preferences\prefs.xml
 			// these are the ones that get edited interactively!
 
 			prefSet = PrefSet.load(prefsPURL, translationScope);
 			if (prefSet != null)
 				println("Loaded Prefs from: " + prefsPURL);
 			else
 				println("No Prefs to load from: " + prefsPURL);
 
 			// now seek the path to an application specific xml preferences file
 			arg = pop(argStack);
 			// if (arg == null)
 			// return;
 			if (arg != null)
 			{
 				// load preferences specific to this invocation
 				if (arg.endsWith(".xml"))
 				{
 					File argPrefsFile = new File(localCodeBasePath, ECLIPSE_PREFS_DIR + arg);
 					ParsedURL argPrefsPURL = new ParsedURL(argPrefsFile);
 					try
 					{
 						PrefSet argPrefSet = PrefSet.load(argPrefsPURL, translationScope);
 						if (metaPrefSetException != null)
 						{
 							warning("Couldn't load MetaPrefs:");
 							metaPrefSetException.printTraceOrMessage(this, "MetaPrefs", metaPrefsPURL);
 							println("\tContinuing.");
 						}
 						if (argPrefSet != null)
 						{
 							println("OK: Loaded Prefs from: " + argPrefsFile);
 							if (prefSet != null)
 								prefSet.addPrefSet(argPrefSet);
 							else
 								prefSet = argPrefSet;
 						}
 						else
 						{
 							println("");
 							String doesntExist = argPrefsFile.exists() ? "" : "\n\tFile does not exist!!!\n\n";
 							println("ERROR: Loading Prefs from: " + argPrefsFile + doesntExist);
 						}
 
 					}
 					catch (SIMPLTranslationException e)
 					{
 						if (metaPrefSetException != null)
 						{
 							error("Can't load MetaPrefs or Prefs. Quitting.");
 							metaPrefSetException.printTraceOrMessage(this, "MetaPrefs", metaPrefsPURL);
 							e.printStackTrace();
 							throw e;
 						}
 						// meta prefs o.k. we can continue without having loaded Prefs now
 						e.printTraceOrMessage(this, "Couldn't load Prefs", argPrefsPURL);
 						println("\tContinuing.");
 					}
 				}
 				else
 					argStack.push(arg);
 			}
 			else
 				argStack.push(arg); // let the next code handle returning.
 			break;
 		}
 		System.out.println("Printing Prefs:\n");
 		try
 		{
 			if (prefSet != null)
 				SimplTypesScope.serialize(prefSet, System.out, StringFormat.XML);				
 		}
 		catch (SIMPLTranslationException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println("\nPrefs Printed");
 		if (prefSet != null)
 			postProcessPrefs(prefSet);
 	}
 
 	/**
 	 * Look for pref Ops, if delayed: setup their timers, and also set scope for their ops.
 	 * 
 	 * @param prefSet
 	 */
 	private void postProcessPrefs(PrefSet prefSet)
 	{
 		if (sessionScope == null)
 			return;
 		for (Pref<?> pref : prefSet.values())
 			if (pref != null)
 				pref.postLoadHook(sessionScope);
 	}
 
 	private PrefSet loadPrefsFromJNLP(String prefSpec)
 	{
 		PrefSet prefSet = null;
 
 		debugA("loadPrefsFromJNLP()");
 		if (prefSpec.startsWith("%3Cpref_set"))
 		{
 			try
 			{
 				String decodedPrefsXML = URLDecoder.decode(prefSpec, "UTF-8");
 				debugA("Loading prefs from JNLP: " + decodedPrefsXML);
 				
 				for (ClassDescriptor c : translationScope.getClassDescriptors())
 				{
 					debugA(c.toString());
 				}
 
 				prefSet = PrefSet.loadFromCharSequence(decodedPrefsXML, translationScope);
 			}
 			catch (UnsupportedEncodingException e)
 			{
 				e.printStackTrace();
 			}
 			catch (SIMPLTranslationException e)
 			{
 				e.printStackTrace();
 			}
 		}
 		return prefSet;
 	}
 
 	/**
 	 * Get the user.dir property. Form a path from it, ending in slash. See if there is path within
 	 * that that includes the package of baseClass. If so, remove that component from the path.
 	 * 
 	 * Form a File from this path, and a ParsedURL from the file. Set codeBase to this ParsedURL.
 	 * 
 	 * @param baseClass
 	 *          Class of the subclass of this that is the main program that was executed.
 	 * 
 	 * @return File that corresponds to the path of the local codeBase.
 	 */
 	@Override
 	protected File deriveLocalFileCodeBase(Class<?> baseClass)
 	{
 		// setup codeBase
 		if (baseClass == null)
 			baseClass = this.getClass();
 
 		Package basePackage = baseClass.getPackage();
 		String packageName = basePackage.getName();
 		String packageNameAsPath = packageName.replace('.', Files.sep);
 
 		String pathName = System.getProperty("user.dir") + Files.sep;
 		File path = new File(pathName);
 		String pathString = path.getAbsolutePath();
 
 		// println("looking for " + packageNameAsPath +" in " + pathString);
 
 		int packageIndex = pathString.lastIndexOf(packageNameAsPath);
 		if (packageIndex != -1)
 		{
 			pathString = pathString.substring(0, packageIndex);
 			path = new File(pathString + Files.sep);
 		}
 
 		codeBase = new ParsedURL(path);
 		println("codeBase=" + codeBase);
 		return path;
 	}
 
 	/**
 	 * @see ecologylab.appframework.Environment#runtimeEnv()
 	 */
 	@Override
 	public int runtimeEnv()
 	{
 		return APPLICATION;
 	}
 
 	/**
 	 * @see ecologylab.appframework.Environment#status(String)
 	 */
 	@Override
 	public void showStatus(String s)
 	{
 		System.out.println(s);
 	}
 
 	/**
 	 * @see ecologylab.appframework.Environment#status(String)
 	 */
 	@Override
 	public void status(String msg)
 	{
 		if (msg != null)
 			showStatus(msg);
 	}
 
 	/**
 	 * @see ecologylab.appframework.Environment#docBase() return the current working directory of the
 	 *      application which is "c:\web\code\java\cm"
 	 */
 	@Override
 	public ParsedURL docBase()
 	{
 		ParsedURL purl = new ParsedURL(new File(System.getProperty("user.dir")));
 		return purl;
 	}
 
 	@Override
 	public ParsedURL preferencesDir()
 	{
 		ParsedURL codeBase = codeBase();
 		ParsedURL purl = codeBase.getRelative(ECLIPSE_PREFS_DIR, "forming preferences dir");
 		return purl;
 	}
 
 	static final String	FIREFOX_PATH_WINDOWS		= "C:\\Program Files\\Mozilla Firefox\\firefox.exe";
 
 	static final String	FIREFOX_PATH_WINDOWS_64	= "C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe";
 	
 	static final String FIREFOX_PATH_VIRT				= "C:\\Program Files (x86)\\Microsoft Application Virtualization Client\\sfttray.exe";
 
 	// TODO -- use "open" on the mac!!!
 	static final String	FIREFOX_PATH_MAC				= "/Applications/Firefox.app/Contents/MacOS/firefox";
 
 	// static final String FIREFOX_PATH_MAC = null;
 	static final String	SAFARI_PATH_MAC					= "/Applications/Safari.app/Contents/MacOS/Safari";
 
 	static final String	FIREFOX_PATH_LINUX			= "/usr/bin/firefox";
 
 	static final String	IE_PATH_WINDOWS					= "C:\\Program Files\\Internet Explorer\\IEXPLORE.EXE";
 
 	static String				browserPath;
 
 	/**
 	 * Get the operating system dependent path to a suitable web browser for navigating to a web page.
 	 * This is also dependent on what web browser(s) the user has installed. In particular, we use
 	 * Firefox if it is in its normal place!
 	 * 
 	 * @return String that specifies the OS and browser-specific command.
 	 */
 	static String getBrowserPath()
 	{
 		int os = PropertiesAndDirectories.os();
 		String result = browserPath;
 		if (result == null)
 		{
 			switch (os)
 			{
 			case PropertiesAndDirectories.XP:
 			case PropertiesAndDirectories.VISTA_AND_7:
 				if (!Pref.lookupBoolean("navigate_with_ie"))
 					result = FIREFOX_PATH_WINDOWS;
 				if (result != null)
 				{
 					File existentialTester = new File(result);
 					if (!existentialTester.exists())
 					{
 						result = FIREFOX_PATH_WINDOWS_64;
 						existentialTester = new File(result);
 						if (!existentialTester.exists())
 						{
 							result = IE_PATH_WINDOWS;
 							existentialTester = new File(result);
 							if (!existentialTester.exists())
 								result = null;
 						}
 					}
 				}
 				break;
 			case PropertiesAndDirectories.MAC:
 				result = "/usr/bin/open";
 				break;
 			default:
 				error(PropertiesAndDirectories.getOsName(), "go(ParsedURL) not supported");
 				break;
 			}
 			if (result != null)
 			{
 				browserPath = result;
 			}
 		}
 		return result;
 	}
 
 	static String[]	cachedNavigateArgs;
 
 	/**
 	 * Get the operating system dependent path to a suitable web browser for navigating to a web page.
 	 * This is also dependent on what web browser(s) the user has installed. In particular, we use
 	 * Firefox if it is in its normal place!
 	 * 
 	 * @return String that specifies the OS and browser-specific command.
 	 */
 	static String[] getNavigateArgs()
 	{
 		int os = PropertiesAndDirectories.os();
 		String[] result = cachedNavigateArgs;
 		if (result == null)
 		{
 			switch (os)
 			{
 			case PropertiesAndDirectories.XP:
 			case PropertiesAndDirectories.VISTA_AND_7:
 				String path = null;
 				if (!Pref.lookupBoolean("navigate_with_ie"))
 					path = FIREFOX_PATH_WINDOWS;
 				if (path != null)
 				{
 					File existentialTester = new File(path);
 					firefoxExists = existentialTester.exists();
 					if (!firefoxExists)
 					{
 						path = FIREFOX_PATH_WINDOWS_64;
 						existentialTester = new File(path);
 						firefoxExists = existentialTester.exists();
 					}
 					if (firefoxExists)
 					{ // cool! firefox
 						result = new String[3];
 						result[0] = path;
 						result[1] = "-new-tab";
 					} else {
 						//Use the SCC Virtualization Path
 						path = FIREFOX_PATH_VIRT;
 						existentialTester = new File(path);
 						firefoxExists = existentialTester.exists();
 						if(firefoxExists)
 						{
							result = new String[3];
 							result[0] = path;
							result[1] = "/launch \"Mozilla Firefox 11\" -new-tab";
 						}
 					}
 
 				}
 				if (result == null)
 				{
 					path = IE_PATH_WINDOWS;
 					File existentialTester = new File(path);
 					if (existentialTester.exists())
 					{
 						result = new String[2];
 						result[0] = path;
 					}
 				}
 				break;
 			case PropertiesAndDirectories.MAC:
 				result = new String[4];
 				result[0] = "/usr/bin/open";
 				result[1] = "-a";
 				result[2] = "firefox";
 				firefoxExists = true;
 				break;
 			case PropertiesAndDirectories.LINUX:
 				result = new String[2];
 				result[0] = FIREFOX_PATH_LINUX;
 				firefoxExists = true;
 				break;
 			default:
 				error(PropertiesAndDirectories.getOsName(), "go(ParsedURL) not supported");
 				break;
 			}
 			if (result != null)
 			{
 				cachedNavigateArgs = result;
 			}
 		}
 		return result;
 	}
 
 	
 	/**
 	 * Returns true if firefox was found on this system.
 	 * @return
 	 */
 	@Override
 	public boolean hasFirefox()
 	{
 		getNavigateArgs();//sets firefoxExists if true
 		return firefoxExists;
 	}
 
 	
 	/**
 	 * Navigate to the purl using the best browser we can find.
 	 * 
 	 * @param purl
 	 * @param frame
 	 */
 	@Override
 	public void navigate(ParsedURL purl, String frame)
 	{
 		String[] navigateArgs = getNavigateArgs();
 		if (navigateArgs != null && purl != null)
 		{
 			String purlString = purl.toString();
 			int numArgs = navigateArgs.length;
 			navigateArgs[numArgs - 1] = purlString;
 			StringBuilder sb = new StringBuilder();
 			for (int i = 0; i < numArgs; i++)
 				sb.append(navigateArgs[i]).append(' ');
 			Debug.println("navigate: " + sb);
 			try
 			{
 				Process p = Runtime.getRuntime().exec(navigateArgs);
 			}
 			catch (IOException e)
 			{
 				error("navigate() - caught exception: ");
 				e.printStackTrace();
 			}
 		}
 		else
 			error("navigate() - Can't find browser to navigate to.");
 	}
 
 	@Override
 	public int browser()
 	{
 		return APPLICATION;
 	}
 
 	/**
 	 * Called at the end of an invocation. Calls System.exit(code).
 	 * 
 	 * @param code
 	 *          -- 0 for normal. other values are application specific.
 	 */
 	@Override
 	public void exit(int code)
 	{
 		System.exit(code);
 	}
 
 	public static boolean isInUse()
 	{
 		return inUse;
 	}
 
 	public static boolean runningInEclipse()
 	{
 		return LaunchType.ECLIPSE == LAUNCH_TYPE_PREF.value();
 	}
 
 	public static boolean runningLocalhost()
 	{
 		return LaunchType.LOCAL_JNLP == LAUNCH_TYPE_PREF.value();
 	}
 
 	/**
 	 * Create and show an editor for preferences, iff the MetaPrefSet and PrefSet are non-null. If the
 	 * PrefSet is null, a new empty one will be created for the editor to use.
 	 * 
 	 * @return
 	 */
 	public Object createPrefsEditor(final boolean createJFrame, final boolean isStandalone)
 	{
 		Object result = null;
 		if (metaPrefSet != null)
 		{
 			if (prefSet == null)
 				prefSet = new PrefSet();
 			result = FundamentalPlatformSpecifics.get().getOrCreatePrefsEditor(metaPrefSet, prefSet, prefsPURL, createJFrame, isStandalone);
 		}
 		return result;
 	}
 
 	/**
 	 * Create and show an editor for preferences, iff the MetaPrefSet and PrefSet are non-null. If the
 	 * PrefSet is null, a new empty one will be created for the editor to use.
 	 * 
 	 * @return
 	 */
 	public Object createPrefsEditor()
 	{
 		return this.createPrefsEditor(true, false);
 	}
 
 	/**
 	 * 
 	 * @return MetaPrefSet for this application, loaded from standard locations.
 	 */
 	protected MetaPrefSet metaPrefSet()
 	{
 		return metaPrefSet;
 	}
 
 	@Override
 	public String getApplicationName()
 	{
 		return PropertiesAndDirectories.applicationName;
 	}
 }
