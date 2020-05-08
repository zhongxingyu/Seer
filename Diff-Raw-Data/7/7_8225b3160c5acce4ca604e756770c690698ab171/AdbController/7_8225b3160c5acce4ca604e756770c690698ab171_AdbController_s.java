 package org.topq.mobile.core;
 
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.concurrent.TimeUnit;
 
 import javax.imageio.ImageIO;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Logger;
 
 import com.android.ddmlib.AdbCommandRejectedException;
 import com.android.ddmlib.AndroidDebugBridge;
 import com.android.ddmlib.IDevice;
 import com.android.ddmlib.InstallException;
 import com.android.ddmlib.NullOutputReceiver;
 import com.android.ddmlib.RawImage;
 import com.android.ddmlib.ShellCommandUnresponsiveException;
 import com.android.ddmlib.SyncService;
 import com.android.ddmlib.TimeoutException;
 
 /**
  * <b>ADB Controller</b><br>
  * Uses the AndroidDebugBridge object to support ADB operations.<br>
  * Holds the ADB TCP Clients.<br>
  * 
  * @see <a
  *      href="http://developer.android.com/guide/developing/tools/adb.html">ADB
  *      documentaion</a>
  * @author topq
  * 
  */
 public class AdbController{
 
 	public enum CommunicationBus {
 		USB, WIFI
 	}
 
 	private File adbLocation;
 	private String deviceIds;
 	private int[] portsList;
 	private String[] devicesList;
 	private String host = "localhost";
 	private int tcpPort = 5555;
 	private CommunicationBus communicationBus = CommunicationBus.USB;
 	private AndroidDebugBridge adb;
 	// private AdbTcpClient[] adbClient;
 	private Logger logger = null;
 
 	private ArrayList<IDevice> devices;
 
 	/**
 	 * Init the system object. Get all the connected devices (if exist), set
 	 * port forwarding & init the TCP connection
 	 */
 	public AdbController(String deviceIds) throws Exception {
 		logger = Logger.getLogger(AdbController.class);
 		setDeviceIds(deviceIds);
 		adbLocation = fendAdbFile();
 		devices = new ArrayList<IDevice>();
 
 		boolean success = true;
 
 		// Init the AndroidDebugBridge object
 		AndroidDebugBridge.init(false);
 		adb = AndroidDebugBridge.createBridge(adbLocation.getAbsolutePath() + File.separator + "adb", true);
 		if (adb == null) {
 			success = false;
 		}
 
 		if (success) {
 			if (communicationBus.equals(CommunicationBus.WIFI)) {
 				// Send ADB connect command for each device in the device list
 				for (int i = 0; i < devicesList.length; i++) {
 					String cmd = "adb connect " + devicesList[i] + ":" + tcpPort;
 					Runtime run = Runtime.getRuntime();
 					Process pr = run.exec(cmd);
 					pr.waitFor();
 					BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
 					long startTime = System.currentTimeMillis();
 					while (!buf.ready() && System.currentTimeMillis() - startTime < 30000) {
 						Thread.sleep(500);
 					}
 					if (buf.ready()) {
 						char[] cbuf = new char[256];
 						buf.read(cbuf);
 						logger.debug(String.valueOf(cbuf));
 						
 					} else{
 						Exception e =new Exception("Unable to communicate with the ADB try to kill the process (task manager) and re-run");
 						logger.error(e);
 						throw e;
 						
 					}
 				}
 			}
 		}
 
 		Thread.sleep(5000);
 
 		// Get devices list
 		if (success) {
 			int count = 0;
 			while (adb.hasInitialDeviceList() == false) {
 				try {
 					Thread.sleep(100);
 					count++;
 				} catch (InterruptedException e) {
 				}
 				if (count > 1000) {
 					success = false;
 					break;
 				}
 			}
 			if (success)
 				getDevicesList();
 		}
 
 		if (!success) {
 			Exception e = new Exception("Unable to connect to any Android devices");
 			logger.error(e);
 			terminate();
 			throw e;
 		} else {
 			logger.info("Successfully connected to Android devices");
 			// Set port forwarding & init the TCP connection
 //			setPortForwarding();
 			// initTcpConnection();
 		}
 	}
 
 	
 
 	/**
 	 * Close system object, close the TCP connections & remove port forwarding
 	 */
 	public void close() {
 		// for (int i=0; i<adbClient.length; i++) {
 		// try {
 		// adbClient[i].closeConnection();
 		// } catch (Exception e) {
 		// e.printStackTrace();
 		// }
 		// }
 		for (int i = 0; i < devices.size(); i++) {
 			try {
 				// If in WiFi mode send a disconnect
 				if (communicationBus.equals(CommunicationBus.WIFI)) {
 					String cmd = "adb disconnect " + devicesList[i] + ":" + tcpPort;
 					Runtime run = Runtime.getRuntime();
 					Process pr = run.exec(cmd);
 					pr.waitFor();
 					BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
 					long startTime = System.currentTimeMillis();
 					while (!buf.ready() && System.currentTimeMillis() - startTime < 30000) {
 						Thread.sleep(500);
 					}
 					if (buf.ready()) {
 						char[] cbuf = new char[256];
 						buf.read(cbuf);
 						logger.debug(String.valueOf(cbuf));
 					} else{
 						Exception e = new Exception("Unable to communicate with the ADB try to kill the process (task manager) and re-run");
 						logger.error(e);
 						throw e;
 
 					}
 				}
 				devices.get(i).removeForward(portsList[i], GeneralEnums.SERVERPORT);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		terminate();
 	}
 
 	// /**
 	// * Init the TCP connection for all the existing devices
 	// * @throws Exception
 	// */
 	// public void initTcpConnection() throws Exception {
 	// adbClient = new AdbTcpClient[devices.size()];
 	// for (int i=0; i<devices.size(); i++) {
 	// // Init the connection to the device
 	// report.report(String.format("Connecting to TCP on Android device (host: %s, port: %d)",
 	// host, portsList[i]));
 	// adbClient[i] = new AdbTcpClient(host, portsList[i]);
 	// }
 	// }
 
 	public void setWiFi() {
 		// devices.get(0).
 	}
 
 	/**
 	 * Close the AndroidDebugBridge
 	 */
 	public void terminate() {
 		AndroidDebugBridge.terminate();
 	}
 
 	/**
 	 * Get IDevice by serial number
 	 * 
 	 * @param deviceSerial
 	 * @return the IDevice with the requested serial number if exists
 	 * @throws Exception
 	 */
 	public IDevice getDevice(String deviceSerial) throws Exception {
 		getDevicesList();
 		for (IDevice device : devices) {
 			if (device.getSerialNumber().equals(deviceSerial.trim())) {
 				return device;
 			}
 		}
 		return null;
 	}
 	
 
 	/**
 	 * Create the Devices list if doesnt exist already. Only online devices will
 	 * be added !
 	 */
 	private void getDevicesList() {
 		if (devices.size() == 0) {
 			IDevice[] allDevices = adb.getDevices();
 			for (IDevice device : allDevices) {
 				logger.debug("Device " + device.getSerialNumber() + " Status: " + device.getState());
 				if (device.isOnline()) {
 					String deviceName = device.getSerialNumber();
 					if (communicationBus.equals(CommunicationBus.WIFI) && deviceName.contains(":"))
 						deviceName = deviceName.substring(0, deviceName.indexOf(":"));
 					if (deviceIds != null && deviceIds.contains(deviceName)) {
 						devices.add(device);
 					} else if (deviceIds == null)
 						devices.add(device);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Set port forwarding for the requested device
 	 * 
 	 * @param deviceSerial
 	 * @param localPort
 	 * @param remotePort
 	 * @throws Exception
 	 */
 	private void setPortForwarding(String deviceSerial, int localPort, int remotePort) throws Exception {
 		IDevice device = getDevice(deviceSerial);
 
 		if (device == null)
 			throw new Exception("Unable to find device with serial number: " + deviceSerial);
 
 		if (device.getState() == IDevice.DeviceState.ONLINE){
 			device.createForward(localPort, remotePort);
 		}else{
 			Exception e = new Exception("Unable to perform port forwarding - " + deviceSerial + " is not online");
 			logger.error(e);
 			throw e;
 		}
 	}
 	
 	/**
 	 * Set port forwarding for all online devices The local ports are defined in
 	 * the sut
 	 * 
 	 * @throws Exception
 	 */
 	public void setPortForwarding() throws Exception {
 		getDevicesList();
 		for (int i = 0; i < devices.size(); i++) {
 			logger.info("Set Port Forwarding " + devices.get(i).getSerialNumber());
 			setPortForwarding(devices.get(i).getSerialNumber(), portsList[i], GeneralEnums.SERVERPORT);
 		}
 	}
 
 	/**
 	 * List all the existing devices & their status
 	 * 
 	 * @throws Exception
 	 */
 	public void showDevices() throws Exception {
 		if (devices.size() > 0) {
 			for (IDevice device : devices) {
 				String deviseWithState = device.getSerialNumber() + " " + device.getState();
 				System.out.println(deviseWithState);
 				logger.info(deviseWithState);
 			}
 		} else {
 			Exception e = new Exception("There are no devices connected");
 			logger.error(e);
 			throw e;
 		}
 	}
 
 	// /**
 	// * Send data using the TCP connection & wait for response
 	// * Parse the response (make conversions if necessary - pixels to mms) and
 	// report
 	// * @param device
 	// * @param data serialised JSON object
 	// */
 	// public void sendData(IDevice device, String data) {
 	// report.report("Send Data to " + device.getSerialNumber());
 	// String result;
 	// try {
 	// result = adbClient[devices.indexOf(device)].sendData(data);
 	// int splitIndex = result.indexOf("{");
 	// if (splitIndex == -1) {
 	// report.report("No data recieved from the device", false);
 	// return;
 	// }
 	// setTestAgainstObject(result);
 	// // TestResponse response =
 	// TestResponse.fromJSON(result.substring(splitIndex));
 	// // String deviceName = device.getSerialNumber();
 	// //
 	// // // Add all the data & the log file to the report
 	// // report.report(response.toString(),
 	// response.getTestStatus().getValue());
 	// // report.addProperty("Resolution", response.getResolution());
 	// // report.addProperty("Screen Size", String.format("%.2f",
 	// response.getScreenSize()));
 	// // if (!response.getUserInput().equals(""))
 	// // report.addProperty("User Input", response.getUserInput());
 	// //
 	// // for (TestResults testResult : response.getTestResults()) {
 	// // // TODO Currently only adding if expected != actual or actual >
 	// expected (in specific cases). might change in the future
 	// // if
 	// (testResult.getResultName().equals(TestResultMessages.MAX_DIAMETER.getValue())
 	// // ||
 	// testResult.getResultName().equals(TestResultMessages.MAX_OFFSET.getValue())
 	// // ||
 	// testResult.getResultName().equals(TestResultMessages.DRAG.getValue())
 	// // ||
 	// testResult.getResultName().equals(TestResultMessages.DROP.getValue())) {
 	// // if (Float.valueOf(testResult.getResultValue()) >
 	// Float.valueOf(testResult.getResultExpected()))
 	// // report.addProperty(testResult.getResultName(), String.format("%.2f",
 	// Float.valueOf(testResult.getResultValue())));
 	// // }
 	// // else if (testResult.getResultName().contains("Velocity")
 	// // || testResult.getResultName().contains("Noise")
 	// // || testResult.getResultName().contains("Sample Rate avarage")) {
 	// // report.addProperty(testResult.getResultName(), String.format("%.2f",
 	// Float.valueOf(testResult.getResultValue())));
 	// // }
 	// // else if
 	// (!testResult.getResultValue().equals(testResult.getResultExpected()))
 	// // report.addProperty(testResult.getResultName(),
 	// testResult.getResultValue());
 	// // }
 	// //
 	// // // Check if test skip - if so, dont grab the log file
 	// // boolean skipped = false;
 	// // for (TestResults testResult : response.getTestResults()) {
 	// // if (testResult.getResultName().equals("Test Skipped")) {
 	// // skipped = true;
 	// // break;
 	// // }
 	// // }
 	// // if (!skipped) {
 	// // String devStr = deviceName;
 	// // if (deviceName.indexOf(":") != -1)
 	// // devStr = deviceName.substring(0,deviceName.indexOf(":"));
 	// // while (devStr.contains("."))
 	// // devStr = devStr.replace(".", "");
 	// // getFileFromDevice(deviceName, GeneralEnums.SD_TOUCH_LOCATION,
 	// GeneralEnums.LOG_FILE_NAME, GeneralEnums.LOCAL_LOG_PATH + "/" + devStr +
 	// "_" + GeneralEnums.LOG_FILE_NAME);
 	// // }
 	// } catch (Exception e) {
 	// report.report("Failed to send / receive data", e);
 	// }
 	// }
 
 	/**
 	 * Get device screen shot now
 	 * 
 	 * @param device
 	 * @throws Exception
 	 */
 	public void getScreenShots(IDevice device, File screenshotFile) throws Exception {
 		logger.info("Screen Shot " + device.getSerialNumber());
 		RawImage ri = device.getScreenshot();
 		display(device.getSerialNumber(), ri, screenshotFile);
 	}
 	
 	public void getScreenShots(IDevice device) throws Exception {
 		getScreenShots(device, null);
 	}
 
 	private void display(String device, RawImage rawImage, File screenshotFile) throws Exception {
 		BufferedImage image = new BufferedImage(rawImage.width, rawImage.height, BufferedImage.TYPE_INT_RGB);
 		// Dimension size = new Dimension(image.getWidth(), image.getHeight());
 
 		int index = 0;
 		int indexInc = rawImage.bpp >> 3;
 		for (int y = 0; y < rawImage.height; y++) {
 			for (int x = 0; x < rawImage.width; x++, index += indexInc) {
 				int value = rawImage.getARGB(index);
 				image.setRGB(x, y, value);
 			}
 		}
 		if (screenshotFile == null){
 			screenshotFile = File.createTempFile("screenshot", ".png");
 			
 		}
 		ImageIO.write(image, "png", screenshotFile);
 		logger.info("ScreenShot can be found in:" + screenshotFile.getAbsolutePath());
 	}
 
 	/**
 	 * Send Device reboot command
 	 * 
 	 * @param device
 	 * @throws Exception
 	 */
 	public void rebootDevice(IDevice device) throws Exception {
 		logger.info("Reboot " + device.getSerialNumber());
 		rebootDevice(device);
 	}
 
 	/**
 	 * Grab file from the device
 	 * 
 	 * @param deviceName
 	 * @param fileLocation
 	 *            file location on the device
 	 * @param fileName
 	 *            file name on the device
 	 * @throws Exception
 	 */
 	public void getFileFromDevice(String deviceName, String fileLocation, String fileName, String localLocation)
 			throws Exception {
 		IDevice device = getDevice(deviceName);
 		if (device == null) {
 			logger.warn("Unable to get file from device");
 			return;
 		}
 		String devStr = deviceName;
 		if (deviceName.indexOf(":") != -1)
 			devStr = deviceName.substring(0, deviceName.indexOf(":"));
 		while (devStr.contains("."))
 			devStr = devStr.replace(".", "");
 
 		try {
 			File local = new File(localLocation.substring(0, localLocation.lastIndexOf(fileName) - 1));
 			if (!local.exists())
 				local.mkdirs();
 			device.getSyncService().pullFile(fileLocation + "/" + fileName, localLocation,
 					SyncService.getNullProgressMonitor());
 	// ReporterHelper.copyFileToReporterAndAddLink(report, new
 			// File(localLocation), devStr + "_" + fileName);
 			// FileUtils.deleteFile(localLocation);
 		} catch (Exception e) {
 			logger.error("Exception ",e);
 			throw e;
 		} 
 	}
 
 	/**
 	 * Push file to the device
 	 * 
 	 * @param deviceName
 	 * @param fileLocation
 	 *            file location on the device
 	 * @param fileName
 	 *            file name on the device
 	 * @throws Exception
 	 */
 	public void pushFileToDevice(String deviceName, String fileLocation, String fileName, String localLocation)
 			throws Exception {
 		IDevice device = getDevice(deviceName);
 		if (device == null) {
 			logger.warn("Unable to push file to device");
 			return;
 		}
 		try {
 			device.getSyncService().pushFile(localLocation, fileLocation + "/" + fileName,
 					SyncService.getNullProgressMonitor());
 		} catch (Exception e) {
 			logger.error("Exception ",e);
 			throw e;
 		} 
 	}
 
 	/**
 	 * Install APK on device
 	 * 
 	 * @param apkLocation
 	 * @throws InstallException 
 	 * @throws IOException 
 	 * @throws ShellCommandUnresponsiveException 
 	 * @throws AdbCommandRejectedException 
 	 * @throws TimeoutException 
 	 */
 	public void installAPK(String serverConfFileLocation,String apkLocation) throws InstallException, TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
 		if (devices.size() == 0)
 			getDevicesList();
 		for (int i = 0; i < devices.size(); i++) {
 			try {
 				devices.get(i).installPackage(apkLocation, true);
 				devices.get(i).executeShellCommand("cp "+serverConfFileLocation +" /data/conf.txt",new NullOutputReceiver());
 			} catch (InstallException e) {
 				logger.error("Error while installing APK file",e);
 				throw e;
 			}
 		}
 	}
 
 	private File fendAdbFile() {
 		File root = new File(System.getenv("ANDROID_HOME"));      
 		try {            
 			String[] extensions = {"exe"};      
 			boolean recursive = true;          
 			Collection<File> files = FileUtils.listFiles(root, extensions, recursive);        
 			for (Iterator<File> iterator = files.iterator(); iterator.hasNext();) {         
 				File file = (File) iterator.next();         
 				if(file.getName().compareTo("adb.exe") == 0){
 					return file.getParentFile();
 				}
 			}    
 		} catch (Exception e) {   
 			e.printStackTrace();       
 		}
 		return null;
 	}
 	
 	/**
 	 * Limor B
 	 * @param pakageName
 	 * @param testClassName
 	 * @param testName
 	 * @throws Exception 
 	 */
	public void runTestOnDevice(String serverConfFileLocation,String apkLocation,String pakageName,String testClassName,String testName,boolean doDeply) throws Exception{
		if(doDeply){
			installAPK(apkLocation,serverConfFileLocation);
		}
		getDevice(deviceIds).executeShellCommand("instrument -e class "+pakageName+"."+testClassName+"#"+testName+" "+pakageName+"/android.test.InstrumentationTestRunner",new NullOutputReceiver());
 		Thread.sleep(TimeUnit.SECONDS.toMillis(2));
 	}
 
 	
 	public CommunicationBus getCommunicationBus() {
 		return communicationBus;
 	}
 
 	/**
 	 * Device connection mode
 	 * 
 	 * @param communicationBus
 	 */
 	public void setCommunicationBus(CommunicationBus communicationBus) {
 		this.communicationBus = communicationBus;
 	}
 
 	/**
 	 * Get all devices
 	 */
 	public ArrayList<IDevice> getDevices() {
 		return devices;
 	}
 
 	public String getDeviceIds() {
 		return deviceIds;
 	}
 
 	/**
 	 * Device IDs - can be either IP address or Serial number
 	 * 
 	 * @param deviceIds
 	 */
 	public void setDeviceIds(String deviceIds) {
 		this.deviceIds = deviceIds;
 		devicesList = deviceIds.split(";");
 		portsList = new int[devicesList.length];
 		for (int i = 0; i < portsList.length; i++)
 			portsList[i] = 1234 + i;
 	}
 
 	public int getTcpPort() {
 		return tcpPort;
 	}
 
 	/**
 	 * Set the port to connect the device (if in WIFI mode)
 	 * 
 	 * @param tcpPort
 	 */
 	public void setTcpPort(int tcpPort) {
 		this.tcpPort = tcpPort;
 	}
 }
