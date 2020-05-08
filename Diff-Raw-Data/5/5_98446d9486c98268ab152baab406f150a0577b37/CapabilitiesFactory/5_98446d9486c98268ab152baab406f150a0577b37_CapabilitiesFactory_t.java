 package factory;
 
 import java.io.File;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import org.apache.log4j.Logger;
 import org.openqa.selenium.remote.CapabilityType;
 import org.openqa.selenium.remote.DesiredCapabilities;
 
 import runner.Devices;
 import utils.ApplicationStorage;
 
 /**
  * @author aleksei_mordas
  * 
  */
 public class CapabilitiesFactory {
 
 	private static final String PREFERENCES_APP = "Preferences.app";
 
 	private static String BROWSER_NAME = "iOS";
 
 	private static String VERSION = "6.1";
 
 	private static String PLATFORM = "Mac";
 
 	public static DesiredCapabilities capabilities = new DesiredCapabilities();
 
 	private static final Logger LOGGER = Logger
 			.getLogger(CapabilitiesFactory.class);
 
 	public static DesiredCapabilities createDefaultCapabilities(Devices device) {
 		capabilities.setCapability(CapabilityType.BROWSER_NAME, BROWSER_NAME);
 		capabilities.setCapability(CapabilityType.VERSION, VERSION);
 		capabilities.setCapability(CapabilityType.PLATFORM, PLATFORM);
		capabilities.setCapability("app", ApplicationStorage.getDefaultPathToApp() );
 		capabilities.setCapability("device", device.toString());
 		LOGGER.info("CAPABILITY PATH: " + ApplicationStorage.getDefaultPathToApp() );
 		return capabilities;
 	}
 
 	public static String getSimulatorCapability() {
 		return capabilities.getCapability("device").toString();
 	}
 
 	public static DesiredCapabilities createAndroidCapabilities() {
		capabilities.setCapability("app", ApplicationStorage.getDefaultPathToApk() );
 		capabilities.setCapability("device", "selendroid");
 		capabilities.setCapability("app-package", ApplicationStorage.getDefaultPackage());
         capabilities.setCapability("app-activity",ApplicationStorage.getDefaultActivity());
 		LOGGER.info("CAPABILITY PATH: " + ApplicationStorage.getDefaultPathToApk());
 		return capabilities;
 	}
 
 	public static DesiredCapabilities createIphoneCapabilities() {
 		return createDefaultCapabilities(Devices.IPHONE);
 	}
 
 }
