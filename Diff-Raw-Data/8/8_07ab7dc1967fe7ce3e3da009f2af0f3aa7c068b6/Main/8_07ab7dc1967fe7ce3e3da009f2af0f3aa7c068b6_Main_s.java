 package emcshop.gui;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.SplashScreen;
 import java.io.File;
 import java.io.IOException;
 import java.lang.Thread.UncaughtExceptionHandler;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import emcshop.cli.Arguments;
 import emcshop.db.DbDao;
 import emcshop.db.DbListener;
 import emcshop.db.DirbyEmbeddedDbDao;
 import emcshop.util.Settings;
 
 /**
  * The main class for the GUI.
  * @author Michael Angstadt
  */
 public class Main {
 	private static final Logger logger = Logger.getLogger(Main.class.getName());
 
 	private static final Set<String> validArgs = new HashSet<String>();
 	static {
 		validArgs.add("help");
 		validArgs.add("db");
 		validArgs.add("settings");
 	}
 
 	//To display the splash screen, this must be added as a VM parameter:
 	//-splash:src/main/resources/emcshop/gui/splash.png
 	public static void main(String[] args) throws Exception {
 		//create the config folder in the user's home directory
 		//File config = new File(System.getProperty("user.home"), ".emc-shopkeeper");
 		File config = new File(".emc-shopkeeper");
 		if (!config.exists() && !config.mkdir()) {
 			throw new IOException("Could not create config folder: " + config.getAbsolutePath());
 		}
 
		File defaultDbFolder = new File(config, "db/data");
 		File defaultSettingsFile = new File(config, "settings.properties");
 
 		Arguments arguments = new Arguments(args);
 
 		if (arguments.exists(null, "help")) {
 			//@formatter:off
 			System.out.print("EMC Shopkeeper\n" + 
 			"by shavingfoam\n" +
 			"\n" +
 			"--db=PATH\n" +
 			"  Specifies the location of the database.\n" +
 			"  Defaults to " + defaultDbFolder.getAbsolutePath() + "\n" +
 			"\n" +
 			"--settings=PATH\n" +
 			"  Specifies the location of the file that has application settings.\n" +
 			"  Defaults to " + defaultSettingsFile.getAbsolutePath() + "\n" +
 			"\n" +
 			"--help\n" +
 			"  Prints this help message.\n");
 			//@formatter:on
 			return;
 		}
 
 		Collection<String> invalidArgs = arguments.invalidArgs(validArgs);
 		if (!invalidArgs.isEmpty()) {
 			System.out.println("The following arguments are invalid:");
 			for (String invalidArg : invalidArgs) {
 				System.out.println("  " + invalidArg);
 			}
 			System.exit(1);
 		}
 
 		String settingsFilePath = arguments.value(null, "settings-file");
 		File settingsFile = (settingsFilePath == null) ? defaultSettingsFile : new File(settingsFilePath);
 		final Settings settings = new Settings(settingsFile);
 
 		String dbPath = arguments.value(null, "db");
 		File dbFolder = (dbPath == null) ? defaultDbFolder : new File(dbPath);
 
		// TODO uncaught exception handler

 		//==========================
 
 		final SplashScreenWrapper splash = new SplashScreenWrapper();
 		splash.setMessage("Starting database...");
 
 		DbListener listener = new DbListener() {
 			@Override
 			public void onCreate() {
 				splash.setMessage("Creating database...");
 			}
 
 			@Override
 			public void onBackup(int oldVersion, int newVersion) {
 				splash.setMessage("Preparing for database migration...");
 			}
 
 			@Override
 			public void onMigrate(int oldVersion, int newVersion) {
 				splash.setMessage("Migrating database...");
 			}
 		};
 
 		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
 			@Override
 			public void uncaughtException(Thread thread, Throwable thrown) {
 				ErrorDialog.show(null, "An error occurred.", thrown);
 			}
 		});
 
		DbDao dao = new DirbyEmbeddedDbDao(dbFolder, listener);
 
 		MainFrame frame = new MainFrame(settings, dao);
 		splash.close();
 		frame.setVisible(true);
 	}
 
 	private static class SplashScreenWrapper {
 		private final SplashScreen splash;
 		private final Graphics2D gfx;
 
 		public SplashScreenWrapper() {
 			splash = SplashScreen.getSplashScreen();
 			if (splash == null) {
 				logger.warning("Splash screen not configured to display.");
 				gfx = null;
 			} else {
 				gfx = splash.createGraphics();
 				if (gfx == null) {
 					logger.warning("Could not get Graphics2D object from splash screen.");
 				}
 			}
 		}
 
 		public void setMessage(String message) {
 			if (gfx != null) {
 				gfx.setComposite(AlphaComposite.Clear);
 				gfx.fillRect(40, 80, 200, 40);
 				gfx.setPaintMode();
 				gfx.setColor(Color.BLACK);
 				gfx.drawString(message, 40, 80);
 				splash.update();
 			}
 		}
 
 		public void close() {
 			if (splash != null) {
 				splash.close();
 			}
 		}
 	}
 }
