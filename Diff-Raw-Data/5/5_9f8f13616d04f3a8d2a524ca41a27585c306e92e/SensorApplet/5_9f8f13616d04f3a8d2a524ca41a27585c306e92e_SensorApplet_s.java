 package org.concord.sensor.applet;
 
 import java.awt.EventQueue;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import java.util.logging.Logger;
 
 import javax.swing.JApplet;
 
 import org.concord.sensor.ExperimentConfig;
 import org.concord.sensor.SensorConfig;
 import org.concord.sensor.SensorRequest;
 import org.concord.sensor.device.SensorDevice;
 import org.concord.sensor.device.impl.DeviceConfigImpl;
 import org.concord.sensor.device.impl.DeviceID;
 import org.concord.sensor.device.impl.JavaDeviceFactory;
 import org.concord.sensor.impl.ExperimentRequestImpl;
 import org.concord.sensor.impl.SensorRequestImpl;
 import org.concord.sensor.impl.SensorUtilJava;
 
 /**
  * This applet expects the following params:
  *   device: the device name to use (supports: golink, labquest, pseudo, manual)
  *     - if 'manual', you will also need to specify:
  *       deviceId (int): the device id you want to use
  *       openString (String; optional): the open string needed to be passed to the device (some devices don't need this)
  *   probeType: The probe type to use (supports: temperature, light, distance, co2, force 5n, force 50n, manual)
  *     - if 'manual', you will also need to specify:
  *       period (float): how often to take a sample
  *       precision (int): how many significant digits to report
  *       min (float): min supported value
  *       max (float): max supported value
  *       sensorPort (int): which port the sensor is attached to the device
  *       stepSize (float): the maximum step size between values
  *       sensorType (int): one of the constants from SensorConfig (eg SensorConfig.QUANTITY_TEMPERATURE)
  * @author aunger
  *
  */
 public class SensorApplet extends JApplet implements SensorAppletAPI {
     private static final long serialVersionUID = 1L;
     private static final Logger logger = Logger.getLogger(SensorApplet.class.getName());
     
 	private JavaDeviceFactory deviceFactory;
 	private SensorDevice device;
 	private JavascriptDataBridge jsBridge;
 	private boolean deviceIsRunning = false;
     
     public enum State {
         READY, RUNNING, STOPPED, UNKNOWN
     }
     
     @Override
     public void destroy() {
     	super.destroy();
     	
 		tearDownDevice();
     }
     
     public boolean initSensorInterface(final String listenerPath) {
     	AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
     		public Boolean run() {
     			try {
     				setupInterface(listenerPath);
     			} catch (Exception e) {
     				e.printStackTrace();
     			}
     			
     			return Boolean.TRUE;
     		}
     	});
         
 		return true;
 	}
 
 	private void setupInterface(final String listenerPath) {
 		// Create the data bridge
 		jsBridge = new JavascriptDataBridge(listenerPath, SensorApplet.this);
 
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				setupDevice();
 
 				jsBridge.sensorsReady();
 			}
 		});
 	}
 
 	public void stopCollecting() {
 		AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
 			public Boolean run() {
 				try {
 					stopDevice();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 
 				return Boolean.TRUE;
 			}
 		});
 	}
 
 	private void stopDevice() {
 		if (device != null && deviceIsRunning) {
 			device.stop(true);
 			deviceIsRunning = false;
 		}
 	}
     
     public void startCollecting() {
 		stopCollecting();
 		
     	AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
     		public Boolean run() {
     			try {
     				startDevice();
     			} catch (Exception e) {
     				e.printStackTrace();
     			}
     			return Boolean.TRUE;
     		}
     	});
     }
 
 	private void startDevice() {
 		if (device == null) {
 			setupDevice();
 		}
 		// Check what is attached, this isn't necessary if you know what you want
 		// to be attached.  But sometimes you want the user to see what is attached
 		ExperimentConfig currentConfig = device.getCurrentConfig();
 		System.out.println("Current sensor config:");
 		SensorUtilJava.printExperimentConfig(currentConfig);
 
 
 		ExperimentRequestImpl request = new ExperimentRequestImpl();
 
 		SensorRequest sensor = getSensorRequest(request);
 
 		request.setSensorRequests(new SensorRequest [] { sensor });
 
 		final ExperimentConfig actualConfig = device.configure(request);
 		System.out.println("Config to be used:");
 		SensorUtilJava.printExperimentConfig(actualConfig);
 
 		deviceIsRunning = device.start();		
 		System.out.println("started device");
 
 		final float [] data = new float [1024];
 		Thread t = new Thread() {
 			public void run() {
 				while(deviceIsRunning){
 					int numSamples = device.read(data, 0, 1, null);
 					if(numSamples > 0) {
 						jsBridge.handleData(numSamples, data);
 					}
 					try {
 						Thread.sleep((long)(actualConfig.getDataReadPeriod()*1000));
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		};
 		t.start();
 	}
 
 	private void setupDevice() {
 		tearDownDevice();
 		deviceFactory = new JavaDeviceFactory();
 		
 		int deviceId = getDeviceId();
 		logger.info("Creating device");
 		device = deviceFactory.createDevice(new DeviceConfigImpl(deviceId, getOpenString(deviceId)));
 		logger.info("Done creating device");
 	}
 
 	private void tearDownDevice() {
 		if(device != null){
 			device.close();
 			device = null;
 		}
 	}
     
     private int getDeviceId() {
     	String id = getParameter("device");
     	logger.info("Got device of: " + id);
     	if (id.equals("golink")) {
 			return DeviceID.VERNIER_GO_LINK_JNA;
     	} else if (id.equals("labquest")) {
     		return DeviceID.VERNIER_LAB_QUEST;
     	} else if (id.equals("manual")) {
     		try {
    			return Integer.parseInt(getParameter("device_id"));
     		} catch (NumberFormatException e) {
    			logger.severe("Invalid 'device_id' param: " + getParameter("device_id"));
     		}
     	}
     	return DeviceID.PSEUDO_DEVICE;
     }
     
     private String getOpenString(int deviceId) {
     	switch (deviceId) {
 		case DeviceID.VERNIER_GO_LINK_JNA:
 		case DeviceID.VERNIER_LAB_QUEST:
 			return null;
 		default:
 			return getParameter("openString");
 		}
     }
     
     private SensorRequest getSensorRequest(ExperimentRequestImpl experiment) {
     	String type = getParameter("probeType");
     	logger.info("Got probeType of: " + type);
     	if (type == null) { type = "temperature"; }
     	type = type.toLowerCase();
     	
 		SensorRequestImpl sensor = new SensorRequestImpl();
 		
 		if (type.equals("light")) {
 			experiment.setPeriod(0.1f);
 			configureSensorRequest(sensor, 0, 0.0f, 4000.0f, 0, 0.1f, SensorConfig.QUANTITY_LIGHT);
 		} else if (type.equals("position") || type.equals("distance")) {
 			experiment.setPeriod(0.1f);
 			configureSensorRequest(sensor, -2, 0.0f, 4.0f, 0, 0.1f, SensorConfig.QUANTITY_DISTANCE);
 		} else if (type.equals("co2")) {
 			experiment.setPeriod(1.0f);
 			configureSensorRequest(sensor, 1, 0.0f, 5000.0f, 0, 20.0f, SensorConfig.QUANTITY_CO2_GAS);
 		} else if (type.equals("force") || type.equals("force 5n")) {
 			experiment.setPeriod(0.01f);
 			configureSensorRequest(sensor, -2, -4.0f, 4.0f, 0, 0.01f, SensorConfig.QUANTITY_FORCE);
 		} else if (type.equals("force 50n")) {
 			experiment.setPeriod(0.01f);
 			configureSensorRequest(sensor, -1, -40.0f, 40.0f, 0, 0.1f, SensorConfig.QUANTITY_FORCE);
 		} else if (type.equals("manual")) {
 			try {
 				experiment.setPeriod(Float.parseFloat(getParameter("period")));
 				configureSensorRequest(sensor,
 						Integer.parseInt(getParameter("precision")),
 						Float.parseFloat(getParameter("min")),
 						Float.parseFloat(getParameter("max")),
 						Integer.parseInt(getParameter("sensorPort")),
 						Float.parseFloat(getParameter("stepSize")),
 						Integer.parseInt(getParameter("sensorType"))
 						);
 			} catch (NumberFormatException e) {
 				logger.severe("One or more manual configuration params was incorrect or unspecified!\n" + 
 						"period: " + getParameter("period") + "\n" + 
 						"precision: " + getParameter("precision") + "\n" + 
 						"min: " + getParameter("min") + "\n" + 
 						"max: " + getParameter("max") + "\n" + 
 						"sensorPort: " + getParameter("sensorPort") + "\n" + 
 						"stepSize: " + getParameter("stepSize") + "\n" + 
 						"sensorType: " + getParameter("sensorType")
 						);
 				// fall back to temperature
 				experiment.setPeriod(0.1f);
 				configureSensorRequest(sensor, -1, 0.0f, 40.0f, 0, 0.1f, SensorConfig.QUANTITY_TEMPERATURE);
 			}
 		} else {
 			// fall back to temperature
 			experiment.setPeriod(0.1f);
 			configureSensorRequest(sensor, -1, 0.0f, 40.0f, 0, 0.1f, SensorConfig.QUANTITY_TEMPERATURE);
 		}
 		experiment.setNumberOfSamples(-1);
 		
 		return sensor;
     }
     
     private void configureSensorRequest(SensorRequestImpl sensor, int precision, float min, float max, int port, float step, int type) {
 		sensor.setDisplayPrecision(precision);
 		sensor.setRequiredMin(min);
 		sensor.setRequiredMax(max);
 		sensor.setPort(port);
 		sensor.setStepSize(step);
 		sensor.setType(type);
     }
 }
