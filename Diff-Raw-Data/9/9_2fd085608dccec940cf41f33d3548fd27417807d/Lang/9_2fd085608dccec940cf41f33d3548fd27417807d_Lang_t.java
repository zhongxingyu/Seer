 package ted;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.net.URI;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.Vector;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 /**
  * TED: Torrent Episode Downloader (2005 - 2007)
  * 
  * This is the language class that provides localization for strings used in ted.
  * parts of this code are copyright by Azureus
  * 
  * License:
  * This file is part of ted. ted and all of it's parts are licensed
  * under GNU General Public License (GPL) version 2.0
  * 
  * for more details see: http://en.wikipedia.org/wiki/GNU_General_Public_License
  * 
  * @author Roel
  */
 public class Lang
 {
 	/****************************************************
 	 * GLOBAL VARIABLES
 	 ****************************************************/
 	private static final String BUNDLE_NAME = "ted.translations.tedLang"; //$NON-NLS-1
 	private static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(
 			BUNDLE_NAME, new Locale("en", "US"));
 	public static final Locale LOCALE_ENGLISH = new Locale("en", "US");
 	public static final Locale LOCALE_DEFAULT = new Locale("", ""); // == english
 	private static Locale LOCALE_CURRENT = LOCALE_DEFAULT;
 	private static int bundle_fail_count = 0;
 
 	/****************************************************
 	 * CONSTRUCTORS
 	 ****************************************************/
 	private Lang()
 	{
 	}
 
 	/****************************************************
 	 * PUBLIC METHODS
 	 ****************************************************/
 	/**
 	 * Get string from localization file
 	 * @param key String to find
 	 * @return Value of String in selected language
 	 */
 	public static String getString(String key)
 	{
 		try
 		{
 			return RESOURCE_BUNDLE.getString(key);
 		} 
 		catch (MissingResourceException e)
 		{
 			return '!' + key + '!';
 		}
 	}
 
 	/**
 	 * Set language of ted to specified locale
 	 * @param newLocale
 	 */
 	public static void setLanguage(Locale newLocale)
 	{
 		Locale.setDefault(LOCALE_DEFAULT);
 		
 		ResourceBundle newResourceBundle = null;
 		String bundleFolder = BUNDLE_NAME.replace('.', '/');
 		final String prefix = BUNDLE_NAME.substring(BUNDLE_NAME
 				.lastIndexOf('.') + 1);
 		final String extension = ".properties";
 
 		try
 		{
 			// File userBundleFile = new File(SystemProperties.getUserPath());
 			File appBundleFile = new File(".");
 
 			// Get the jarURL
 			// XXX Is there a better way to get the JAR name?
 			ClassLoader cl = Lang.class.getClassLoader();
 			String sJar = cl.getResource(bundleFolder + extension).toString();
 			sJar = sJar.substring(0, sJar.length() - prefix.length()
 					- extension.length());
 			URL jarURL = new URL(sJar);
 
 			// User dir overrides app dir which overrides jar file bundles
 			URL[] urls = { appBundleFile.toURL(), jarURL };
 
 			newResourceBundle = getResourceBundle("tedLang", newLocale,
 					new URLClassLoader(urls));
 			
 			// do more searches if getBundle failed, or if the language is not the
 			// same and the user wanted a specific country
 			if (newResourceBundle == null
 					|| (!newResourceBundle.getLocale().getLanguage().equals(
 							newLocale.getLanguage()) && !newLocale.getCountry()
 							.equals("")))
 			{
 				Locale foundLocale = newResourceBundle.getLocale();
 				TedLog.debug("changeLocale: "
 								+ (foundLocale.toString().equals("") ? "*Default Language*"
 										: foundLocale.getDisplayLanguage())
 								+ " != " + newLocale.getDisplayName()
 								+ ". Searching without country..");
 				// try it without the country
 				Locale localeJustLang = new Locale(newLocale.getLanguage());
 				newResourceBundle = getResourceBundle("tedLang",
 						localeJustLang, new URLClassLoader(urls));
 
 				if (newResourceBundle == null
 						|| !newResourceBundle.getLocale().getLanguage().equals(
 								localeJustLang.getLanguage()))
 				{
 					// find first language we have in our list
 					TedLog.debug("changeLocale: Searching for language "
 							+ newLocale.getDisplayLanguage()
 							+ " in *any* country..");
 					Locale[] locales = Lang.getAvailableLocales();
 					for (int i = 0; i < locales.length; i++)
 					{
 						if (locales[i].getLanguage() == newLocale.getLanguage())
 						{
 							newResourceBundle = getResourceBundle("tedLang",
 									locales[i], new URLClassLoader(urls));
 							break;
 						}
 					}
 				}
 			}
 		} catch (MissingResourceException e)
 		{
 			TedLog.debug("changeLocale: no resource bundle for "
 					+ newLocale);
 			//Debug.printStackTrace( e );
 			// return false;
 		} catch (Exception e)
 		{
 			//Debug.printStackTrace( e );
 		}
 
 		if (newResourceBundle != null)
 		{
 			//
 			if (!newLocale.toString().equals("en")
 					&& !newResourceBundle.getLocale().equals(newLocale))
 			{
 				String sNewLanguage = newResourceBundle.getLocale()
 						.getDisplayName();
 				if (sNewLanguage == null || sNewLanguage.trim().equals(""))
 					sNewLanguage = "English (default)";
 				TedLog.debug("changeLocale: no message properties for Locale '"
 								+ newLocale.getDisplayName()
 								+ "' ("
 								+ newLocale + "), using '" + sNewLanguage + "'");
 			}
 			newLocale = newResourceBundle.getLocale();
 			Locale.setDefault(newLocale);
 			LOCALE_CURRENT = newLocale;
 			setResourceBundle(new IntegratedResourceBundle(newResourceBundle));
 		}
 	}
 	
 	/**
 	 * Get a list of available locales on the system
 	 * @return
 	 */
 	public static Locale[] getAvailableLocales()
 	{
 		String bundleFolder = BUNDLE_NAME.replace('.', '/');
 		final String prefix = BUNDLE_NAME.substring(BUNDLE_NAME
 				.lastIndexOf('.') + 1);
 		final String extension = ".properties";
 
 		String urlString = Lang.class.getClassLoader().getResource(
 				bundleFolder.concat(extension)).toExternalForm();
 		//System.out.println("urlString: " + urlString);
 		String[] bundles = null;
 
 		if (urlString.startsWith("jar:file:"))
 		{
 
 			File jar = Lang.getJarFileFromURL(urlString);
 
 			if (jar != null)
 			{
 				try
 				{
 					// System.out.println("jar: " + jar.getAbsolutePath());
 					JarFile jarFile = new JarFile(jar);
 					Enumeration entries = jarFile.entries();
 					ArrayList list = new ArrayList(250);
 					while (entries.hasMoreElements())
 					{
 						JarEntry jarEntry = (JarEntry) entries.nextElement();
 						if (jarEntry.getName().startsWith(bundleFolder)
 								&& jarEntry.getName().endsWith(extension))
 						{
 							// System.out.println("jarEntry: " + jarEntry.getName());
 							list.add(jarEntry.getName().substring(
 									bundleFolder.length() - prefix.length()));
 							// "MessagesBundle_de_DE.properties"
 						}
 					}
 					bundles = (String[]) list.toArray(new String[list.size()]);
 				} catch (Exception e)
 				{
 					TedLog.error(e, "Error loading language file");
 				}
 			}
 		} else
 		{
 			File bundleDirectory = new File(URI.create(urlString))
 					.getParentFile();
 			//      System.out.println("bundleDirectory: " +
 			// bundleDirectory.getAbsolutePath());
 
 			bundles = bundleDirectory.list(new FilenameFilter()
 			{
 				public boolean accept(File dir, String name)
 				{
 					return name.startsWith(prefix) && name.endsWith(extension);
 				}
 			});
 		}
 
 		HashSet bundleSet = new HashSet();
 
 		// Add AppDir 2nd
 		File appDir = new File(".");
 		String appBundles[] = appDir.list(new FilenameFilter()
 		{
 			public boolean accept(File dir, String name)
 			{
 				return name.startsWith(prefix) && name.endsWith(extension);
 			}
 		});
 
 		// can be null if app path is borked
 
 		if (appBundles != null)
 		{
 
 			bundleSet.addAll(Arrays.asList(appBundles));
 		}
 		// Any duplicates will be ignored
 		bundleSet.addAll(Arrays.asList(bundles));
 
 		
 		ArrayList foundLocalesList = new ArrayList(bundleSet.size());
 
 		foundLocalesList.add(LOCALE_ENGLISH);
 
 		Iterator val = bundleSet.iterator();
 		int i = 0;
 		while (val.hasNext())
 		{
 			String sBundle = (String) val.next();
 
 			// System.out.println("ResourceBundle: " + bundles[i]);
 			if (prefix.length() + 1 < sBundle.length() - extension.length())
 			{
 				String locale = sBundle.substring(prefix.length() + 1, sBundle
 						.length()
 						- extension.length());
 				//System.out.println("Locale: " + locale);
 				String[] sLocalesSplit = locale.split("_", 3);
 				if (sLocalesSplit.length > 0 && sLocalesSplit[0].length() == 2)
 				{
 					if (sLocalesSplit.length == 3)
 					{
 						foundLocalesList.add(new Locale(sLocalesSplit[0],
 								sLocalesSplit[1], sLocalesSplit[2]));
 					} else if (sLocalesSplit.length == 2
 							&& sLocalesSplit[1].length() == 2)
 					{
 						foundLocalesList.add(new Locale(sLocalesSplit[0],
 								sLocalesSplit[1]));
 					} else
 					{
 						foundLocalesList.add(new Locale(sLocalesSplit[0]));
 					}
 				} else
 				{
 					if (sLocalesSplit.length == 3
 							&& sLocalesSplit[0].length() == 0
 							&& sLocalesSplit[2].length() > 0)
 					{
 						foundLocalesList.add(new Locale(sLocalesSplit[0],
 								sLocalesSplit[1], sLocalesSplit[2]));
 					}
 				}
 			}
 		}
 
 		Locale[] foundLocales = new Locale[foundLocalesList.size()];
 
 		foundLocalesList.toArray(foundLocales);
 
 		try
 		{
 			Arrays.sort(foundLocales, new Comparator()
 			{
 				public final int compare(Object a, Object b)
 				{
 					return ((Locale) a).getDisplayName((Locale) a)
 							.compareToIgnoreCase(
 									((Locale) b).getDisplayName((Locale) b));
 				}
 			});
 		} catch (Throwable e)
 		{
 			// user has a problem whereby a null-pointer exception occurs when sorting the
 			// list - I've done some fixes to the locale list construction but am
 			// putting this in here just in case
 			//Debug.printStackTrace( e );
 		}
 		return foundLocales;
 	}
 
 	/**
 	 * Load a jarfile from an URL
 	 * @param url_str
 	 * @return
 	 */
 	public static File getJarFileFromURL(String url_str)
 	{
 		if (url_str.startsWith("jar:file:"))
 		{
 
 			// java web start returns a url like "jar:file:c:/sdsd" which then fails as the file
 			// part doesn't start with a "/". Add it in!
 			// here's an example
 			// jar:file:C:/Documents%20and%20Settings/stuff/.javaws/cache/http/Dparg.homeip.net/P9090/DMazureus-jnlp/DMlib/XMAzureus2.jar1070487037531!/org/gudy/azureus2/internat/MessagesBundle.properties
 
 			// also on Mac we don't get the spaces escaped
 
 			url_str = url_str.replaceAll(" ", "%20");
 
 			if (!url_str.startsWith("jar:file:/"))
 			{
 
 				url_str = "jar:file:/".concat(url_str.substring(9));
 			}
 
 			try
 			{
 				// 	you can see that the '!' must be present and that we can safely use the last occurrence of it
 
 				int posPling = url_str.lastIndexOf('!');
 
 				String jarName = url_str.substring(4, posPling);
 
 				//        System.out.println("jarName: " + jarName);
 
 				URI uri = URI.create(jarName);
 
 				File jar = new File(uri);
 
 				return (jar);
 
 			} catch (Throwable e)
 			{
 
 				//Debug.printStackTrace( e );
 			}
 		}
 
 		return (null);
 	}
 	
 	/**
 	 * Get a list with strings for yes no
 	 * @return
 	 */
 	public static Object[] getYesNoLocale()
 	{
 		Object[] options = { Lang.getString("TedGeneral.Yes"), Lang.getString("TedGeneral.No")};
 		return options;
 	}
 	/**
 	 * Get a list with strings for yes no cancel
 	 * @return
 	 */
 	public static Object[] getYesNoCancelLocale()
 	{
		Object[] options = { Lang.getString("TedGeneral.Yes"), Lang.getString("TedGeneral.No"), Lang.getString("TedGeneral.ButtonCancel")};
 		return options;
 	}
 
 	
 	/****************************************************
 	 * PRIVATE METHODS
 	 ****************************************************/
 	private static void setResourceBundle(ResourceBundle bundle)
 	{
 		RESOURCE_BUNDLE = bundle;
 	}
 
 	/**
 	 * Load a resourcebundle
 	 * @param name
 	 * @param loc
 	 * @param cl
 	 * @return
 	 */
 	static ResourceBundle getResourceBundle(String name, Locale loc, ClassLoader cl)
 	{
 		try
 		{
 			return (ResourceBundle.getBundle(name, loc, cl));
 
 		} 
 		catch (Throwable e)
 		{
 
 			bundle_fail_count++;
 
 			if (bundle_fail_count == 1)
 			{
 				e.printStackTrace();
 				TedLog.error("Failed to load resource bundle. One possible cause is "
 								+ "that you have installed ted into a directory "
 								+ "with a '!' in it. If so, please remove the '!'.");
 			}
 
 			return (new ResourceBundle()
 			{
 				public Locale getLocale()
 				{
 					return (LOCALE_DEFAULT);
 				}
 
 				protected Object handleGetObject(String key)
 				{
 					return (null);
 				}
 
 				public Enumeration getKeys()
 				{
 					return (new Vector().elements());
 				}
 			});
 		}
 	}
 }
