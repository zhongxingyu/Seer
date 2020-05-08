 package edu.mines.acmX.exhibit.input_services.hardware;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import edu.mines.acmX.exhibit.input_services.hardware.devicedata.DeviceDataInterface;
 import edu.mines.acmX.exhibit.input_services.hardware.drivers.DriverInterface;
 import edu.mines.acmX.exhibit.input_services.hardware.drivers.InvalidConfigurationFileException;
 import edu.mines.acmX.exhibit.module_management.metas.DependencyType;
 
 /**
  * The HardwareManager acts as a layer of communication for retrieving drivers.
  * Information is loaded via a manifest file which can be modified. The manager
  * will choose the most appropriate driver given a functionality and a specific
  * module's set of permissions. The manager is a singleton to reduce conflicts
  * between driver requests. The manager also checks whether a module's required
  * functionalities are available.
  * 
  * The Manager is responsible for the following tasks:<br/><br/>
  * -Loading the HardwareManager manifest file.<br/>
  * -Validating the manifest file<br/>
  * 		-> 	Checks to see if any of the classpaths provided for interfaces or
  * 			drivers is invalid.<br/>
  * 		->	Checks to see if any functionalities given by drivers are invalid
  * 			i.e. a functionality that does not exist in the <functionalities>
  * 			tag<br/>
  * - Ensuring a given module meta data is able to run<br/>
  * 		->	Checks to see that all required functionalities are supported.<br/>
  * - Clears the driver cache so that persistance of driver data is not possible
  * 		in between module runtimes.<br/>
  * - Re-builds the driver cache given the currently running module meta data.<br/>
  * 		->	Checks the availabilities of drivers that are considered to support
  * 			required functionalities for the module.<br/>
  * - Builds drivers at runtime for functionalities considered optional for the
  * 		currently running module. <br/><br/>
  * 
  * TODO Change so that if the requested driver is unavailable for a function.
  * then it will attempt to load the next one.
  * 
  * @author Aakash Shah
  * @author Ryan Stauffer
  * 
  * @see {@link HardwareManagerMetaData} {@link HardwareManagerManifestLoader}
  * 
  */
 
 public class HardwareManager {
 
 	private static final Logger log = LogManager
 			.getLogger(HardwareManager.class);
 
 	public static final String DEFAULT_MANIFEST_PATH = "hardware_manager_manifest.xml";
 
 	private HardwareManagerMetaData metaData;
 	private Map<String, DependencyType> currentModuleInputTypes;
 
 	private static HardwareManager instance = null;
 	private static String manifest_path = DEFAULT_MANIFEST_PATH;
 
 	private Map<String, List<String>> devices; // available list of devices
 	private Map<String, DriverInterface> deviceDriverCache;
 	private Map<String, String> configFileStore;
 
 	/**
 	 * The constructor of the HardwareManager. This method loads the config
 	 * file, and ensures that the file is valid. <br/>
 	 * 
 	 * @throws HardwareManagerManifestException
 	 * 
 	 * @see {@link #validifyMetaData()} {@link #buildRequiredDevices()}
 	 * 
 	 */
 	private HardwareManager() throws HardwareManagerManifestException {
 
 		loadConfigFile();
 		validifyMetaData();
 		currentModuleInputTypes = new HashMap<String, DependencyType>();
 		devices = new HashMap<String, List<String>>();
 		deviceDriverCache = new HashMap<String, DriverInterface>();
 		configFileStore = new HashMap<String, String>();
 	}
 
 	/**
 	 * Get's an instance of the HardwareManager
 	 * 
 	 * @return A HardwareManager instance
 	 * @throws HardwareManagerManifestException
 	 */
 	public static HardwareManager getInstance()
 			throws HardwareManagerManifestException {
 		if (instance == null) {
 			instance = new HardwareManager();
 		}
 		return instance;
 	}
 	
 	/**
 	 * Sets the location to load the manifest config file from. Upon doing this,
 	 * a new instance of HardwareManager will be initialized.
 	 * 
 	 * @param filepath
 	 * @throws HardwareManagerManifestException
 	 * 
 	 * @see {@link #HardwareManager()}
 	 */
 	public static void setManifestFilepath(String filepath)
 			throws HardwareManagerManifestException {
 		if (null == filepath) {
 			throw new HardwareManagerManifestException("Null filepath given");
 		}
 		manifest_path = filepath;
 		instance = new HardwareManager();
 	}
 	
 	/**
 	 * Loads the configuration file.
 	 * 
 	 * @throws HardwareManagerManifestException
 	 * 
 	 * @see {@link HardwareManagerManifestLoader}
 	 */
 	private void loadConfigFile() throws HardwareManagerManifestException {
 		metaData = HardwareManagerManifestLoader.load(manifest_path);
 	}
 
 	/**
 	 * Responsible for verifying the HardwareManagerMetaData after having been
 	 * loaded from the manifest file. This will ensure that the driver and
 	 * interface classes exist as specified from within the meta-data. Also
 	 * checks to see whether there is a disjoint between the functionalities
 	 * each device supports and interfaces mapped to it.
 	 * 
 	 * @throws HardwareManagerManifestException
 	 *             If the meta-data is incorrect.
 	 * 
 	 * @see {@link HardwareManagerMetaData}
 	 */
 	private void validifyMetaData() throws HardwareManagerManifestException {
 		// verify classes existing
 		Collection<String> interfaces = metaData.getFunctionalities().values();
 		Collection<String> drivers = metaData.getDevices().values();
 		try {
 			for (String i : interfaces) {
 				Class<? extends DeviceDataInterface> cl = Class.forName(i)
 						.asSubclass(DeviceDataInterface.class);
 			}
 
 			for (String i : drivers) {
 				Class<? extends DriverInterface> cl = Class.forName(i)
 						.asSubclass(DriverInterface.class);
 			}
 		} catch (ClassNotFoundException e) {
 			throw new HardwareManagerManifestException(
 					"Invalid interface/driver class");
 		} catch (ExceptionInInitializerError e) {
 			throw new HardwareManagerManifestException(
 					"Invalid interface/driver class");
 		} catch (Exception e) {
 			log.error("Getting an exception");
 		}
 
 		// check support list for being disjoint
 		// Builds a list of all the functionalities requested by all drivers
 		Set<String> supports = new HashSet<String>();
 		for (List<String> deviceSupports : metaData.getDeviceSupports()
 				.values()) {
 			for (String s : deviceSupports) {
 				supports.add(s);
 			}
 		}
 
 		// Checks to see whether the built list is a subset of all provided
 		// functionalities. Note the exception is thrown when a functionality
 		// may not exist in the <functionalities> tag
 		Set<String> available = metaData.getFunctionalities().keySet();
 		if (!available.containsAll(supports)) {
 			throw new HardwareManagerManifestException(
 					"Unknown functionality supported by device");
 		}
 	}
 	
 	/**
 	 * Gets the configuration store so that drivers may load config files if
 	 * needed. From the passed configuration store, we alter the stored map to
 	 * be not "driver name -> config path" but rather
 	 * "canonical driver path -> config path"
 	 * 
 	 * @param config
 	 *            A map of devices->their config files provided from the module
 	 *            manager manifest.
 	 */
 	public void setConfigurationFileStore(Map<String, String> config) {
 		this.configFileStore.clear();
 		// supportedDevices = driver->driver_path
 		Map<String, String> supportedDevices = metaData.getDevices();
 		Set<String> deviceKeys = config.keySet();
 
 		for (String configName : deviceKeys) {
 			if (supportedDevices.containsKey(configName)) {
 				this.configFileStore.put(supportedDevices.get(configName),
 						config.get(configName));
 			}
 		}
 
 	}
 
 	/**
 	 * Allows a driver to figure out whether a config file was provided for the
 	 * given canonical driver path.
 	 * 
 	 * @param driverPath
 	 *            Canonical path to the driver
 	 */
 	public boolean hasConfigFile(String driverPath) {
 		return this.configFileStore.containsKey(driverPath);
 	}
 
 	/**
 	 * 
 	 * @param driverPath
 	 *            Canonical path to the driver
 	 * @return location to the config file indicated in the module manager
 	 *         manifest.
 	 */
 	public String getConfigFile(String driverPath) {
 		return this.configFileStore.get(driverPath);
 	}
 	
 	/**
 	 * Sets the currently running module. Validation checks will not occur here
 	 * as to whether the module can run.
 	 * 
 	 * @param mmd
 	 *            The map of functionalities and their level of dependence
 	 */
 	public void setRunningModulePermissions(Map<String, DependencyType> mmd) {
 		currentModuleInputTypes = mmd;
 	}
 
 	/**
 	 * Checks to see whether the functionalities that are required by the
 	 * currently running module are supported through the HardwareManager
 	 * manifest.
 	 * 
 	 * @param inputTypes
 	 * 
 	 * @throws BadDeviceFunctionalityRequestException
 	 *             on failure
 	 */
 	public void checkPermissions(Map<String, DependencyType> inputTypes)
 			throws BadDeviceFunctionalityRequestException {
 		if (null == inputTypes || inputTypes.isEmpty()) {
 			return;
 		}
 		Set<String> functionalities = inputTypes.keySet();
 		for (String functionality : functionalities) {
 			DependencyType dt = inputTypes.get(functionality);
 			// Ignore optional ones
 			if (dt == DependencyType.REQUIRED) {
 				// Make sure we support this functionality
 				if (!metaData.getFunctionalities().containsKey(functionality)) {
 					throw new BadDeviceFunctionalityRequestException(
 							functionality + " not supported.");
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Calls destroy on all driver objects, then removes all cached driver
 	 * objects and rebuilds this cache based on which devices are stored from
 	 * the next running module's input types.
 	 * 
 	 * @throws InvalidConfigurationFileException
 	 *             Thrown if the driver requires a configuration file that was
 	 *             not present on the store.
 	 */
 	public void resetAllDrivers() throws InvalidConfigurationFileException {
 		for (String driverName : deviceDriverCache.keySet()) {
 			DriverInterface driver = deviceDriverCache.get(driverName);
 			driver.destroy();
 		}
 		devices.clear();
 		deviceDriverCache.clear();
 		buildRequiredDevices();
 	}
 
 	/**
 	 * Goes through all the required functionalities in the module's input
 	 * types and ensures that it is available. This only builds functionalities
 	 * marked as REQUIRED in the module's manifest file. This is so that the
 	 * user may be aware when a module's required functionalities have failed
 	 * to load.
 	 * 
 	 * Optional functionalities that require extra drivers to load are done at
 	 * runtime.
 	 * 
 	 * Ignore the following: Loop through currentModuleInputTypes and build
 	 * the cache off that instead of looking at EVERY driver.
 	 * 
 	 * Add the input-types map into checkPermissions
 	 * 
 	 * Document the fact that it ONLY BUILDS required functionalities. This is
	 * because we have already ensured that all the required ones are supported.
 	 * However, we want to provide the user the knowledge that one of their
 	 * optional functionalities has failed to initialize.
 	 * 
 	 * Therefore, this function only loads required, and a special method inside
	 * the inflateDriver method will load the optional one at runtime.
	 * 
	 * TODO Check if a driver was loaded for the required functionality, throw
	 * error on fail. This would indicate that a functionality considered to be
	 * required does not have an available device supporting it.
 	 * 
 	 * @throws InvalidConfigurationFileException
 	 */
 	public void buildRequiredDevices() throws InvalidConfigurationFileException {
 
 		Set<String> moduleFunctionalities = currentModuleInputTypes.keySet();
 		for (String moduleFunc : moduleFunctionalities) {
 			if (currentModuleInputTypes.get(moduleFunc) == DependencyType.REQUIRED) {
 				// Returns device names
 				List<String> supportingDevices = findDeviceDriversSupporting(moduleFunc);
 
 				buildDriverList(supportingDevices);
 			}
 		}
 	}
 	/**
 	 * @param func The functionality requested
 	 * @return list of strings of driver names supporting the given 
 	 *	functionality
 	 */
 	private List<String> findDeviceDriversSupporting(String func) {
 		List<String> ret = new ArrayList<String>();
 
 		// Driver Name -> List of functionalities
 		Map<String, List<String>> deviceSupports = metaData.getDeviceSupports();
 
 		Set<String> driverNames = deviceSupports.keySet();
 		for (String device : driverNames) {
 			if (deviceSupports.get(device).contains(func)) {
 				ret.add(device);
 			}
 		}
 
 		return ret;
 	}
 
 	/**
 	 * Given a functionality and a list of supporting driver names, detect
 	 * which drivers are available and loads them into the driver cache if not
 	 * already present.
 	 * <br/>
 	 * Furthermore, we rely on the assumption that if a device is available
 	 * then all the functionalities it supports is also available. Therefore,
 	 * we also make a call to add onto our internal map of functionalities to
 	 * available driver paths. 
 	 * 
 	 * @param driverNames list of driver names to check availability for
 	 * @throws InvalidConfigurationFileException
 	 * 
 	 * TODO Change name of exception thrown to match context more. Something
 	 * like 'DriverLoadException'
 	 */
 	// instantiates and checks the availability from the provided list.
 	private void buildDriverList(List<String> driverNames)
 			throws InvalidConfigurationFileException {
 		
 		// Driver Name -> Driver paths
 		Map<String, String> driverPaths = metaData.getDevices();
 
 		for (String name : driverNames) {
 			String path = driverPaths.get(name);
 			// Check if cache already contains this driver
 			if (deviceDriverCache.containsKey(path)) {
 				continue;
 			}
 			try {
 				Class<? extends DriverInterface> cl = Class.forName(path)
 						.asSubclass(DriverInterface.class);
 				Constructor<? extends DriverInterface> ctor = cl
 						.getConstructor();
 				DriverInterface iDriver = ctor.newInstance(); // Check whether
 																// the device is
 																// available
 				if (iDriver.isAvailable()) {
 					// Cache the driver
 					deviceDriverCache.put(path, iDriver);
 					addDeviceFunctionalities(name);
 				}
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			} catch (NoSuchMethodException e) {
 				e.printStackTrace();
 			} catch (SecurityException e) {
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			} catch (IllegalArgumentException e) {
 				e.printStackTrace();
 			} catch (InvocationTargetException e) {
 				throw new InvalidConfigurationFileException(e.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * Given a driver, we add all the functionalities it supports to our
 	 * internal list of available functionalities.
 	 * @param driverName driver that is available
 	 */
 	private void addDeviceFunctionalities(String driverName) {
 		Map<String, List<String>> deviceSupports = metaData.getDeviceSupports();
 		List<String> functionalities = deviceSupports.get(driverName);
 		for (String s : functionalities) {
 			if (devices.containsKey(s)) {
 				devices.get(s).add(metaData.getDevices().get(driverName));
 			} else {
 				List<String> drivers = new ArrayList<String>();
 				drivers.add(metaData.getDevices().get(driverName));
 				devices.put(s, drivers);
 			}
 		}
 	}
 
 	/**
 	 * Constructs a driver object for a given functionality and driver path.
 	 * 
 	 * @param driverPath
 	 * @param functionality
 	 * @return An instance of a driver capable of supporting that functionality
 	 * @throws BadFunctionalityRequestException
 	 *             	no devices support that functionality
 	 * @throws UnknownDriverRequest thrown if the driver is not 
 	 * 				connected/in the cache
 	 */
 	public DeviceDataInterface inflateDriver(String driverPath,
 			String functionality) throws BadFunctionalityRequestException,
 			UnknownDriverRequest {
 		String functionalityPath = getFunctionalityPath(functionality);
 
 		if ("".equals(functionalityPath)) {
 			throw new BadFunctionalityRequestException(functionality
 					+ " is unknown");
 		}
 
 		DeviceDataInterface iDriver = null;
 		if (!deviceDriverCache.containsKey(driverPath)) {
 			throw new UnknownDriverRequest("Requested driver "
 					+ driverPath + " is not available/unknown");
 		}
 		iDriver = (DeviceDataInterface) deviceDriverCache.get(driverPath);
 		return iDriver;
 	}
 
 	/**
 	 * Returns the function class path for a given functionality.
 	 * 
 	 * @param functionality
 	 * @return Interface path for the given functionality
 	 */
 	private String getFunctionalityPath(String functionality) {
 		Map<String, String> fPaths = metaData.getFunctionalities();
 		if (fPaths.containsKey(functionality)) {
 			return fPaths.get(functionality);
 		}
 		return "";
 	}
 
 	/**
 	 * Returns a list of driver paths that support a given functionality. In
 	 * the event that the functionality was considered optional, attempt to
 	 * build the driver and store it into the cache and list of available
 	 * functionalities.
 	 * 
 	 * @param functionality
 	 * @return list of driver paths that support the given functionality
 	 * @throws BadFunctionalityRequestException
 	 *             no devices support that functionality
 	 * @throws InvalidConfigurationFileException In the event this functionality
 	 * 				was considered optional and failed to load correctly.
 	 * 
 	 * @see {@link HardwareManager#findDeviceDriversSupporting(String)}
 	 * 		{@link HardwareManager#buildDriverList(List)}
 	 */
 	public List<String> getDevices(String functionality)
 			throws BadFunctionalityRequestException, InvalidConfigurationFileException {
 		// Check if the functionality was listed in our HardwareManager
 		// manifest.
 		if (!metaData.getFunctionalities().containsKey(functionality)) {
 			throw new BadFunctionalityRequestException(
 					"Bad functionality requested");
 		}
 		if (!devices.containsKey(functionality)) {
 			// Check if this functionality was considered 'OPTIONAL'
 			if (currentModuleInputTypes.get(functionality) == DependencyType.OPTIONAL) {
 				// Returns device names
 				List<String> supportingDevices = findDeviceDriversSupporting(functionality);
 				buildDriverList(supportingDevices);
 				
 			} else {
 				// Ideally this exception should never be thrown. The only time
 				// we enter this block is if dependency was required AND not in
 				// our cache.
 				throw new BadFunctionalityRequestException(
 						"Bad functionality requested");
 			}
 		}
 		// Return our list 
 		return devices.get(functionality);
 	}
 
 	/**
 	 * Allows the user to get an instance of the first available driver
 	 * supporting the specified functionality.
 	 * 
 	 * @param functionality
 	 * @return An instance of a driver capable of supporting that functionality
 	 * @throws BadFunctionalityRequestException
 	 *             no devices support that functionality
 	 * @throws UnknownDriverRequest
 	 * @throws InvalidConfigurationFileException 
 	 */
 	public DeviceDataInterface getInitialDriver(String functionality)
 			throws BadFunctionalityRequestException, UnknownDriverRequest,
 			InvalidConfigurationFileException {
 		
 		List<String> drivers = getDevices(functionality);
 		return inflateDriver(drivers.get(0), functionality);
 	}
 	
 	public int getNumberofDriversInCache() {
 		return deviceDriverCache.size();
 	}
 
 }
