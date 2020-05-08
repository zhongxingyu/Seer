 package com.inebriator;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.phidgets.InterfaceKitPhidget;
 import com.phidgets.Phidget;
 import com.phidgets.PhidgetException;
 import com.phidgets.event.ErrorEvent;
 import com.phidgets.event.ErrorListener;
 
 public class PhidgetSolenoidController implements SolenoidController {
 
 	private static final Logger LOG = LoggerFactory
 			.getLogger(PhidgetSolenoidController.class);
 
 	private final InterfaceKitPhidget[] phidgets;
 
 	public PhidgetSolenoidController(Integer[] serialNumbers) {
 		this.phidgets = new InterfaceKitPhidget[serialNumbers.length];
 
 		for (int i = 0; i < serialNumbers.length; i++) {
 			phidgets[i] = openAndAttachPhidget(serialNumbers[i]);
 		}
 	}
 
 	@Override
 	public void openSolenoid(Solenoid solenoid) {
 		setSolenoidState(solenoid, true);
 	}
 
 	@Override
 	public void closeSolenoid(Solenoid solenoid) {
 		setSolenoidState(solenoid, false);
 	}
 	
 	@Override
 	public void disconnect() {
 		for (int i = 0; i < phidgets.length; i++) {
 			try {
 				phidgets[i].close();
 			} catch (PhidgetException e) {
 				LOG.warn("Exception while closing Phidget {}", i, e);
 			}
 		}
 	}
 	
 	private static InterfaceKitPhidget openAndAttachPhidget(int serialNumber) {
 		InterfaceKitPhidget phidget;
 
 		Phidget.enableLogging(Phidget.PHIDGET_LOG_INFO, null);
 
 		try {
 			phidget = new InterfaceKitPhidget();
 			phidget.addErrorListener(new ErrorListener() {
 				@Override
 				public void error(ErrorEvent errorEvent) {
 					LOG.error("Phidget error from {}: {}", errorEvent.getSource(), errorEvent.getException());
 				}
 			});
 
 			LOG.info("Connecting to Phidget with serial number [{}]", serialNumber);
 			phidget.open(serialNumber);
 	
 			LOG.info("Waiting for attachment to Phidget with serial number [{}]", serialNumber);
 			phidget.waitForAttachment();
 	
 			LOG.info("Attached to Phidget with serial number [{}]", serialNumber);
 		} catch (PhidgetException e) {
 			throw new RuntimeException("Unable to connect to Phidget with serial number [" + serialNumber + "]", e);
 		}
 
 		return phidget;
 	}
 
 	private void setSolenoidState(Solenoid solenoid, boolean state) {
 		LOG.debug("Setting state for {} to {}", solenoid, state);
 
 		try {
			if (solenoid.getPhidgetId() + 1 > phidgets.length) {
 				throw new RuntimeException("Phidget does not exist for " + solenoid);
			} else if (solenoid.getSolenoidId() + 1 > phidgets[solenoid.getPhidgetId()].getOutputCount() ) {
 				throw new RuntimeException("Output does not exist for " + solenoid);
 			}
 
 			phidgets[solenoid.getPhidgetId()].setOutputState(solenoid.getSolenoidId(), state);
 		} catch (PhidgetException e) {
 			throw new RuntimeException("Unable to set " + solenoid + " to state " + state, e);
 		}
 	}
 
 }
