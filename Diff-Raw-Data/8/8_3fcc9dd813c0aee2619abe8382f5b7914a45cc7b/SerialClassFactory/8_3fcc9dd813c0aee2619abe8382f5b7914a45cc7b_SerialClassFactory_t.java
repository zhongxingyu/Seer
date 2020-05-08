 /**
  * 
  */
 package serial;
 
 import org.apache.log4j.Logger;
 
 /**
  * Factory class for getting the serial classes.
  * @author Alex
  */
 public class SerialClassFactory {
 //> STATIC CONSTANTS
 	/** Package name for javax.comm */
 	public static final String PACKAGE_JAVAXCOMM = "javax.comm";
 	/** Package name for RXTXserial */
 	public static final String PACKAGE_RXTX = "gnu.io";
 	/** Singleton instance of this class */
 	private static SerialClassFactory INSTANCE;
 
 //> INSTANCE PROPERTIES
 	/** Logging object */
 	private final Logger log = Logger.getLogger(this.getClass().getName());
 	/** The name of the package of the serial implementation to use, either {@link #PACKAGE_JAVAXCOMM} or {@value #PACKAGE_RXTX} */
 	private final String serialPackageName;
 
 //> CONSTRUCTORS
 	/**
 	 * Constructs a {@link SerialClassFactory}.
 	 * @param preferredSerialPackageName the name of the serial package we would rather use
 	 */
 	private SerialClassFactory(String preferredSerialPackageName) {
 		if(testSerialPackageName(preferredSerialPackageName)) {
 			this.serialPackageName = preferredSerialPackageName;
 		} else if(!PACKAGE_JAVAXCOMM.equals(preferredSerialPackageName)
 				&& testSerialPackageName(PACKAGE_JAVAXCOMM)) {
 			this.serialPackageName = PACKAGE_JAVAXCOMM;
		} else if(!PACKAGE_RXTX.equals(preferredSerialPackageName)
 				&& testSerialPackageName(PACKAGE_RXTX)) {
 				this.serialPackageName = PACKAGE_RXTX;
 		} else {
 			this.log.error("Failed to load any serial pacakges.  Using RXTX by default, but it won't work.");
 			this.serialPackageName = PACKAGE_RXTX;
 		}
 	}
 
 //> ACCESSORS
 	/** @return {@link #serialPackageName}. */
 	public String getSerialPackageName() {
 		return serialPackageName;
 	}
 	/**
 	 * Test loading a serial package.
 	 * @param serialPackageName the name of the serial package, e.g. {@link #PACKAGE_JAVAXCOMM}
 	 * @return <code>true</code> if the package loaded successfully; <code>false</code> otherwise
 	 */
 	private boolean testSerialPackageName(String serialPackageName) {
 		try {
 			log.info("Attempting to load serial package: " + serialPackageName);
 			Class.forName(serialPackageName + "." + CommPortIdentifier.class.getSimpleName());
 			log.info("Using serial package: " + serialPackageName);
 			return true;
 		} catch(Throwable t) {
 			log.warn("Failed to load serial package: " + serialPackageName, t);
 			return false;
 		}
 	}
 
 //> INSTANCE HELPER METHODS
 
 //> STATIC FACTORIES
 	/**
 	 * The preferred serial package to use.  This package will be tried first, but the others
 	 * will still be tried as well.
 	 * @param preferredSerialPackageName the name of the serial package to prefer
 	 */
	public static final void init(String preferredSerialPackageName) {
 		INSTANCE = new SerialClassFactory(preferredSerialPackageName);
 	}
 
 //> STATIC HELPER METHODS
 	/**
 	 * Get the singleton instance of this class.  The class must previously have been initialised using {@link #init(String)}
 	 * @return the singleton instance of this class
 	 */
	public static final SerialClassFactory getInstance() {
 		return INSTANCE;
 	}
 	
 	/**
 	 * Attempt to get a class by name, first from the {@link #PACKAGE_JAVAXCOMM}, and then from {@link #PACKAGE_RXTX}
 	 * TODO once we have decided which package we are using, we should probably try exclusively to get classes from that package.  E.g. it might be possible to get javax.comm classes even though the library is broken.  If we can detect that, do it here the first time this method is called.
 	 * @param clazz The class whose namesake we should fetch
 	 * @return An implementation of the desired class
 	 */
 	public Class<?> forName(Class<?> clazz) {
 		try {
 			return Class.forName(this.serialPackageName + "." + clazz.getSimpleName());
 		} catch (ClassNotFoundException ex) {
 			throw new IllegalStateException(clazz.getSimpleName() + " class not found in package " + this.serialPackageName);
 		}
 	}
 }
