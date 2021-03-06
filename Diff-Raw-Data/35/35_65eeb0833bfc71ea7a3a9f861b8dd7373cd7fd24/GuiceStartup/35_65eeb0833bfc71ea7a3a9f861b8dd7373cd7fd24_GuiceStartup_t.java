 package net.sf.openrocket.startup;
 
 import java.io.PrintStream;
 import java.util.Locale;
 import java.util.prefs.Preferences;
 
 import net.sf.openrocket.gui.util.SwingPreferences;
 import net.sf.openrocket.l10n.DebugTranslator;
 import net.sf.openrocket.l10n.L10N;
 import net.sf.openrocket.l10n.ResourceBundleTranslator;
 import net.sf.openrocket.l10n.Translator;
 import net.sf.openrocket.logging.LogLevel;
import net.sf.openrocket.logging.LoggingSystemSetup;
 import net.sf.openrocket.logging.PrintStreamLogger;
 import net.sf.openrocket.logging.PrintStreamToSLF4J;
 import net.sf.openrocket.plugin.PluginModule;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 
 
 /**
  * Second class in the OpenRocket startup sequence, responsible for
  * IoC initialization.
  * 
  * This class is responsible for initializing the Guice dependency injection
  * mechanism, and the legacy Application class.
  * 
  * This class must be very cautious about what classes it calls.  This is because
  * the loggers/translators for classes are initialized as static final members during
  * class initialization.  For example, this class MUST NOT use the Prefs class, because
  * using it will cause LineStyle to be initialized, which then receives an invalid
  * (not-yet-initialized) translator.
  * 
  * @author Sampo Niskanen <sampo.niskanen@iki.fi>
  */
 public class GuiceStartup {
 	
 	private final static Logger log = LoggerFactory.getLogger(GuiceStartup.class);
 	
 	/**
 	 * OpenRocket startup main method.
 	 */
 	public static void main(final String[] args) throws Exception {
 		
 		// Check for "openrocket.debug" property before anything else
 		checkDebugStatus();
 		
 		// Initialize logging first so we can use it
 		initializeLogging();
 		
 		// Setup the translations
 		initializeL10n();
 		
 		// Initialize preferences (*after* translator setup!)
 		Application.setPreferences(new SwingPreferences());
 		
 		
 		Injector injector = initializeGuice();
 		injector.getInstance(ApplicationStartup.class).runMain(args);
 		
 	}
 	
 	
 	/**
 	 * Set proper system properties if openrocket.debug is defined.
 	 */
 	private static void checkDebugStatus() {
 		if (System.getProperty("openrocket.debug") != null) {
 			setPropertyIfNotSet("openrocket.debug.menu", "true");
 			setPropertyIfNotSet("openrocket.debug.mutexlocation", "true");
 			setPropertyIfNotSet("openrocket.debug.motordigest", "true");
 			setPropertyIfNotSet("jogl.debug", "all");
 		}
 	}
 	
 	private static void setPropertyIfNotSet(String key, String value) {
 		if (System.getProperty(key) == null) {
 			System.setProperty(key, value);
 		}
 	}
 	
 	
 	
 	/**
 	 * Initializes the logging system.
 	 */
 	public static void initializeLogging() {
		if (System.getProperty("openrocket.debug") != null) {
			LoggingSystemSetup.addConsoleAppender();
		}
 		//Replace System.err with a PrintStream that logs lines to DEBUG, or VBOSE if they are indented.
 		//If debug info is not being output to the console then the data is both logged and written to
 		//stderr.
 		final PrintStream stdErr = System.err;
 		System.setErr(PrintStreamToSLF4J.getPrintStream("STDERR", stdErr));
 	}
 	
 	private static boolean setLogOutput(PrintStreamLogger logger, PrintStream stream, String level, LogLevel defaultLevel) {
 		LogLevel minLevel = LogLevel.fromString(level, defaultLevel);
 		if (minLevel == null) {
 			return false;
 		}
 		
 		for (LogLevel l : LogLevel.values()) {
 			if (l.atLeast(minLevel)) {
 				logger.setOutput(l, stream);
 			}
 		}
 		return true;
 	}
 	
 	
 	
 	
 	/**
 	 * Initializes the localization system.
 	 */
 	private static void initializeL10n() {
 		
 		// Check for locale propery
 		String langcode = System.getProperty("openrocket.locale");
 		
 		if (langcode != null) {
 			
 			Locale l = L10N.toLocale(langcode);
 			log.info("Setting custom locale " + l);
 			Locale.setDefault(l);
 			
 		} else {
 			
 			// Check user-configured locale
 			Locale l = getUserLocale();
 			if (l != null) {
 				log.info("Setting user-selected locale " + l);
 				Locale.setDefault(l);
 			} else {
 				log.info("Using default locale " + Locale.getDefault());
 			}
 			
 		}
 		
 		// Setup the translator
 		Translator t;
 		t = new ResourceBundleTranslator("l10n.messages");
 		if (Locale.getDefault().getLanguage().equals("xx")) {
 			t = new DebugTranslator(t);
 		}
 		
 		log.info("Set up translation for locale " + Locale.getDefault() +
 				", debug.currentFile=" + t.get("debug.currentFile"));
 		
 		Application.setBaseTranslator(t);
 	}
 	
 	
 	
 	
 	private static Locale getUserLocale() {
 		/*
 		 * This method MUST NOT use the Prefs class, since is causes a multitude
 		 * of classes to be initialized.  Therefore this duplicates the functionality
 		 * of the Prefs class locally.
 		 */
 		
 		if (System.getProperty("openrocket.debug.prefs") != null) {
 			return null;
 		}
 		
 		return L10N.toLocale(Preferences.userRoot().node("OpenRocket").get("locale", null));
 	}
 	
 	
 	
 	private static Injector initializeGuice() {
 		Module applicationModule = new ApplicationModule();
 		Module pluginModule = new PluginModule();
 		
 		return Guice.createInjector(applicationModule, pluginModule);
 	}
 	
 }
