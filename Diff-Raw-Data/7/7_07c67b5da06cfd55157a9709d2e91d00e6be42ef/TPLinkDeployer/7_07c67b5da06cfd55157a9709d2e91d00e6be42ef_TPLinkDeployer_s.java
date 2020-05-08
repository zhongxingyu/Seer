 package net.freifunk.autodeploy.device.tplink;
 
 import static java.util.concurrent.TimeUnit.SECONDS;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Iterator;
 import java.util.Set;
 
 import net.freifunk.autodeploy.device.DetailedDevice;
 import net.freifunk.autodeploy.device.Device;
 import net.freifunk.autodeploy.device.DeviceDeployer;
 import net.freifunk.autodeploy.selenium.Actor;
 
 import org.openqa.selenium.By;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Splitter;
 import com.google.common.collect.ImmutableSet;
 import com.google.inject.Inject;
 
 /**
  * Deploys the Freifunk firmware to TP-Link devices.
  *
  * @author Andreas Baldeau <andreas@baldeau.net>
  */
 public class TPLinkDeployer implements DeviceDeployer {
 
     public static final Set<Device> SUPPORTED_DEVICES = ImmutableSet.of(
         new Device("WDR3500", "v1"),
         new Device("WDR3600", "v1"),
         new Device("WR741N", "v4"),
         new Device("WR741ND", "v4"),
         new Device("WR841N", "v8"),
         new Device("WR841ND", "v8"),
         new Device("WR842N", "v1"),
         new Device("WR842ND", "v1")
     );
 
     private static final Logger LOG = LoggerFactory.getLogger(TPLinkDeployer.class);
 
     private static final String TP_LINK_WEB_INTERFACE_IP = "192.168.0.1";
     private static final int TP_LINK_WEB_INTERFACE_PORT = 80;
     private static final String TP_LINK_WEB_INTERFACE_USER = "admin";
     private static final String TP_LINK_WEB_INTERFACE_PASSWORD = "admin";
 
     private static final String TP_LINK_WEB_INTERFACE_URL =
         "http://" + TP_LINK_WEB_INTERFACE_USER + ":" + TP_LINK_WEB_INTERFACE_PASSWORD + "@" + TP_LINK_WEB_INTERFACE_IP + ":" + TP_LINK_WEB_INTERFACE_PORT;
 
     // menu
     private static final String MENU_FRAME_NAME = "bottomLeftFrame";
     private static final By SYSTEM_TOOLS_MENU_ITEM = By.xpath("//a[contains(text(),'System Tools')]");
     private static final By FIRMWARE_UPGRADE_MENU_ITEM = By.xpath("//a[contains(text(),'Firmware Upgrade')]");
 
     // status page
     private static final By HARDWARE_VERSION = By.id("hversion");
     private static final By MAC_ADDRESS = By.id("lanMac");
 
     // firmware upgrade page
     private static final String MAIN_FRAME_NAME = "mainFrame";
 
     private static final By FIRMWARE_FILE_CHOOSER = By.cssSelector("input[type=file]");
     private static final By FIRMWARE_UPGRADE_BUTTON = By.cssSelector("input[name=Upgrade]");
 
     private final Actor _actor;
 
     @Inject
     public TPLinkDeployer(
         final Actor actor
     ) {
         _actor = actor;
     }
 
     @Override
     public Device autodetect() {
         LOG.debug("Trying to detect the device.");
         try {
             _actor.waitForWebserverBeingAvailable(TP_LINK_WEB_INTERFACE_IP, TP_LINK_WEB_INTERFACE_PORT, 30, SECONDS);
             goToWebInterface();
 
             final Device device = getDevice();
             LOG.debug("Detected device {}. Checking if it's supported.", device);
             if (isSupported(device)) {
                 LOG.debug("Device {} is supported.", device);
                 return device;
             } else {
                 LOG.debug("Device {} is not supported.", device);
                 return null;
             }
         } catch (final Throwable t) {
             LOG.warn("Auto detection failed.", t);
             return null;
         }
     }
 
     @Override
     public DetailedDevice deploy(final File firmwareImage) throws FileNotFoundException {
         LOG.debug("Starting deployment: firmware = {}", firmwareImage);
         _actor.waitForWebserverBeingAvailable(TP_LINK_WEB_INTERFACE_IP, TP_LINK_WEB_INTERFACE_PORT, 60, SECONDS);
         checkFirmwareImage(firmwareImage);
         goToWebInterface();
         final DetailedDevice detailedDevice = checkSupportedDevice();
         openFirmwareUpgradePage();
         startFirmwareUpgrade(firmwareImage);
         return detailedDevice;
     }
 
     private void checkFirmwareImage(final File firmwareImage) throws FileNotFoundException {
         if (!firmwareImage.exists()) {
             throw new FileNotFoundException("The given firmware image file does not exist: " + firmwareImage);
         }
 
         if (!firmwareImage.isFile()) {
             throw new FileNotFoundException("The given firmware image file is a directory: " + firmwareImage);
         }
     }
 
     private void goToWebInterface() {
         _actor.navigateTo(TP_LINK_WEB_INTERFACE_URL);
     }
 
     private DetailedDevice checkSupportedDevice() {
         LOG.debug("Checking device is supported.");
         final Device device = getDevice();
 
         if (!isSupported(device)) {
             throw new IllegalStateException("Unsupported device: " + device);
         }
 
         final String mac = getMac();
 
         return new DetailedDevice(device, mac);
     }
 
     private Device getDevice() {
         _actor.selectFrame(MAIN_FRAME_NAME);
         final String hardwareVersionString = _actor.getTextOfElement(HARDWARE_VERSION);
         final Device device = hardwareVersionToDevice(hardwareVersionString);
         return device;
     }
 
     private String getMac() {
         _actor.selectFrame(MAIN_FRAME_NAME);
         final String mac = _actor.getTextOfElement(MAC_ADDRESS);
         return mac.toLowerCase().replace("-", ":");
     }
 
     private boolean isSupported(final Device device) {
         return SUPPORTED_DEVICES.contains(device);
     }
 
     private Device hardwareVersionToDevice(final String hardwareVersionString) {
         final Iterator<String> parts = Splitter.on(' ').trimResults().split(hardwareVersionString).iterator();
         final String model = parts.next();
         final String version = parts.next();
 
         final Device device = new Device(model, version);
         return device;
     }
 
     private void openFirmwareUpgradePage() {
         _actor.selectFrame(MENU_FRAME_NAME);
         _actor.clickElement(SYSTEM_TOOLS_MENU_ITEM);
         _actor.clickElement(FIRMWARE_UPGRADE_MENU_ITEM);
     }
 
     private void startFirmwareUpgrade(final File firmwareImage) {
         LOG.debug("Starting firmware upgrade.");
         _actor.selectFrame(MAIN_FRAME_NAME);
         _actor.chooseFile(FIRMWARE_FILE_CHOOSER, firmwareImage);
 
         if (_actor.usesHtmlUnitDriver()) {
             // disable checking of firmware as this is not very reliable and requires unnecessarily complicated handling
             _actor.executeJavascript("doSubmit = function () { return true; }");
         }
 
         _actor.clickElement(FIRMWARE_UPGRADE_BUTTON);
 
         if (!_actor.usesHtmlUnitDriver()) {
             // for the HtmlUnitDriver the check is disabled above and thus no prompt will occur.
             _actor.confirmPrompt();
         }
     }
 }
