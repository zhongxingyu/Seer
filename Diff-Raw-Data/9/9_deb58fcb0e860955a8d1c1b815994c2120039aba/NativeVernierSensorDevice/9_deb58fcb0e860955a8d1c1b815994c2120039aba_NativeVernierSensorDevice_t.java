 /*
  * Last modification information:
 * $Revision: 1.4 $
 * $Date: 2005-02-10 20:43:07 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.sensor.nativelib;
 
 import org.concord.sensor.ExperimentRequest;
 import org.concord.sensor.SensorRequest;
 import org.concord.sensor.device.DeviceReader;
 import org.concord.sensor.device.SensorDevice;
 
 import ccsd.vernier.ExperimentConfig;
 import ccsd.vernier.SensorConfig;
 import ccsd.vernier.NativeBridge;
 import ccsd.vernier.SWIGTYPE_p_float;
 import ccsd.vernier.SWIGTYPE_p_void;
 
 /**
  * NativeSensorDevice
  * Class name and description
  *
  * Date created: Dec 2, 2004
  *
  * @author scott<p>
  *
  */
 public class NativeVernierSensorDevice 
 	implements SensorDevice
 {
 	SWIGTYPE_p_void deviceHandle = null;
 	SWIGTYPE_p_float readValuesBuffer = null;
 	SWIGTYPE_p_float readTimestampsBuffer = null;
 	private boolean open;
 	private boolean nativeLibAvailable = false;
 	private boolean useTimeStamps = false;
 	private int numberOfChannels = 1;
 		
 	/**
 	 * 
 	 */
 	public NativeVernierSensorDevice()
 	{
 		try {
			if(System.getProperty("os.name").startsWith("Windows")) {
				System.loadLibrary("GoIO_DLL");
			} 
 			System.loadLibrary("vernier_ccsd");
 			nativeLibAvailable = true;
 //			System.loadLibrary("blah");
 		} catch (Throwable thr) {
 			thr.printStackTrace();
 		}
 	}
 	
 	public synchronized void open(String config)
 	{
 		if(!nativeLibAvailable) {
 			open = false;
 			return;
 		}
 		
 		open = true;
 		deviceHandle = NativeBridge.SensDev_open(config);
 		if(readValuesBuffer == null) {
 			readValuesBuffer = NativeBridge.new_floatArray(200);
 		}
 		
 		if(readTimestampsBuffer == null) {
 			readTimestampsBuffer = NativeBridge.new_floatArray(200);
 		}
 	}
 	
 	public synchronized void close()
 	{
 		open = false;
 		if(deviceHandle != null) {
 			NativeBridge.SensDev_close(deviceHandle);
 		}
 	}
 		
 	/* (non-Javadoc)
 	 * @see org.concord.sensor.device.AbstractSensorDevice#getErrorMessage(int)
 	 */
 	public String getErrorMessage(int error)
 	{
 		// TODO configure errors and error messages
 		return "no error message yet";
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see org.concord.sensor.device.AbstractSensorDevice#getRightMilliseconds()
 	 */
 	public int getRightMilliseconds()
 	{
 		// TODO Base this on the time between samples from the device
 		return 50;
 	}
 	
 	/**
 	 * Send this directly to the native dll.
 	 * It will need some configuration string which would be set earlier
 	 * so there probably needs to be a cookie that is passed around for
 	 * this device to store things.
 	 * @see org.concord.sensor.SensorDataProducer#isAttached()
 	 */
 	public synchronized boolean isAttached()
 	{
 		if(!open) return false;
 
 		int result = NativeBridge.SensDev_isAttached(deviceHandle);
 		return result == 1;
 	}
 
 	/**
 	 * convert the experimentconfig to native structures.
 	 * this is probably done in the native code???
 	 * @see org.concord.sensor.SensorDataProducer#configure(org.concord.sensor.ExperimentConfig)
 	 */
 	public synchronized org.concord.sensor.ExperimentConfig configure(ExperimentRequest request)
 	{
 		ExperimentConfig requestConfig = 
 			new ExperimentConfig();
 
 		requestConfig.setPeriod(request.getPeriod());
 		requestConfig.setNumberOfSamples(request.getNumberOfSamples());
 		
 		SensorRequest [] sensorRequests = request.getSensorRequests();
 		
 		requestConfig.setNumSensorConfigs(sensorRequests.length);
 		requestConfig.createSensorConfigArray(sensorRequests.length);
 		numberOfChannels = sensorRequests.length;
 		for(int i=0; i<sensorRequests.length; i++) {
 			SensorRequest sensorRequest = sensorRequests[i];
 			SensorConfig sensorConfig = new SensorConfig();
 			sensorConfig.setPort(sensorRequest.getPort());
 			sensorConfig.setType(sensorRequest.getType());
 			sensorConfig.setStepSize(sensorRequest.getStepSize());
 			sensorConfig.setRequiredMax(sensorRequest.getRequiredMax());
 			sensorConfig.setRequiredMin(sensorRequest.getRequiredMin());
 			// TODO this should be based on the default unit
 			// for this type 
 			sensorConfig.setUnitStr("degC");
 			requestConfig.setSensorConfig(sensorConfig, i);
 		}
 		
 		// TODO we need to make sure that java owns this object
 		// that is returned by the sensor device.  Otherwise
 		// the memory will never get freed.
 		org.concord.sensor.ExperimentConfig expConfig = 
 			NativeBridge.configureHelper(deviceHandle, requestConfig);
 		if(expConfig != null) {
 			useTimeStamps = !expConfig.getExactPeriod();
 		}
 		return expConfig;
 	}
 
 	/**
 	 * Can be native.
 	 * 
 	 * @see org.concord.sensor.SensorDataProducer#getCurrentConfig()
 	 */
 	public org.concord.sensor.ExperimentConfig getCurrentConfig()
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/**
 	 * can be native
 	 * @see org.concord.sensor.SensorDataProducer#canDetectSensors()
 	 */
 	public boolean canDetectSensors()
 	{
 		// TODO: send this request to the NativeBridge
 		return false;
 	}
 
 	/**
 	 * 
 	 * native. but with the associated cookie.  unless there is another
 	 * perhaps the cookie can be stored in this object so the native code
 	 * can look it up and then it won't need to be in each of these methods.
 	 * @see org.concord.framework.data.DataFlow#stop()
 	 */
 	public synchronized void stop(boolean wasRunning)
 	{
 		// TODO check for null device
 		NativeBridge.SensDev_stop(deviceHandle);
 	}
 	
 	/**
 	 * native.
 	 * @see org.concord.framework.data.DataFlow#start()
 	 */
 	public synchronized boolean start()
 	{
 		// TODO check for null device
 		NativeBridge.SensDev_start(deviceHandle);
 		
 		return true;
 
 	}
 
 	public synchronized int read(float [] values, int offset, 
 			int nextSampleOffset, DeviceReader reader)
 	{
 		// take our existing native float pointer
 		// pass it to the native read function so it gets filled in
 		// traverse the resutling array (in C) and copy the values 
 		// into this array.  This could be more efficient if the
 		// the array functions generated by swig included a "copy"
 		// function that would copy a section of the array.
 		int numberRead = NativeBridge.SensDev_read(deviceHandle, readValuesBuffer, 
 				readTimestampsBuffer, 200);
 		
 		if (numberRead < 0) {
 			System.err.println("error reading values from device");
 		}
 		
 		int valPos = offset;
 		for(int i=0; i<numberRead; i++) {
 			int firstChannelValuePos = valPos;
 			if(useTimeStamps) {
 				values[valPos] = NativeBridge.floatArray_getitem(readTimestampsBuffer, i);
 				firstChannelValuePos = valPos+1;
 			}
 			for(int j=0; j<numberOfChannels; j++) {
 				values[firstChannelValuePos+j] = 
 					NativeBridge.floatArray_getitem(readValuesBuffer, i*numberOfChannels+j);
 			}
 			
 			valPos += nextSampleOffset;
 		}
 		
 		return numberRead;
 	}
 }
