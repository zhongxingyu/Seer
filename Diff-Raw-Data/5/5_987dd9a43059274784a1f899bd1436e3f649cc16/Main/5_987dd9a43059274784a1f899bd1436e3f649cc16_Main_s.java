 package org.concord.sensor.labquest.jna;
 
 import java.io.IOException;
 
 import com.sun.jna.Native;
 import com.sun.jna.Pointer;
 
 public class Main {
 	private static LabQuestLibrary labQuestLib;
 	
 	private static NGIOLibrary ngio;
 	private static Pointer hLibrary;
 	private static Pointer hDevice;
 
 	private static LabQuest labQuest;
 
 	public static void main(String[] args) throws IOException {
 		labQuestLib = new LabQuestLibrary();
 		labQuestLib.init();
 		
 		// This doesn't work unfortunately because it won't be called on the 
 		// same thread that called all of the other things.  So it would be 
 		// better if even this code could work by calling things on its own thread
 		// that is supported right now only in the sensor-vernier project which uses 
 		// this project.
 		/*
     	Runtime.getRuntime().addShutdownHook(new Thread(){
 			public void run() {
 				System.err.println("Closing LabQuestLibrary.");
 
 				if(labQuest != null){
 					try {
 						labQuest.close();
 					} catch (LabQuestException e) {
 						e.printStackTrace();
 					}
 				}
 				
 				labQuestLib.cleanup();				
 			}
 		});
 		*/    	
 
 		try {
 			short[] version = labQuestLib.getDLLVersion();
 			System.out.println("major: " + version[0] + 
 					" minor: " + version[1]);
 
			for(int i=0; i<5; i++){
 				labQuestLib.searchForDevices();
 				labQuestLib.printListOfDevices();
				Thread.sleep(500);
 			}
 			String firstDevice = null;
 			firstDevice = labQuestLib.getFirstDeviceName();
 
 			if(firstDevice == null){
 				return;
 			}
 
 			labQuest = labQuestLib.openDevice(firstDevice);
 
 			testMotion();
 		} catch (Throwable t) {
 			t.printStackTrace();
 		}
 
 		if(labQuest != null){
 			try {
 				labQuest.close();
 			} catch (LabQuestException e) {
 				e.printStackTrace();
 			}
 		}
 
 		labQuestLib.cleanup();
 	}
 	
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void test() throws LabQuestException {
 		boolean remoteCollectionActive = false;
 		remoteCollectionActive = labQuest.isRemoteCollectionActive();
 		System.out.println("remote collection active: " + remoteCollectionActive);
 
 		// isremotecollection active appears to always return false.
 		// so even if it isn't active try to acquire ownership
 		labQuest.acquireExclusiveOwnership();
 
 		labQuest.printAttachedSensors();
 
 		int channelOneId = labQuest.getSensorId(NGIOSourceCmds.CHANNEL_ID_ANALOG1);
 
 		String units = "";
 		if(channelOneId >= 20){
 			labQuest.ddsMemReadRecord(NGIOSourceCmds.CHANNEL_ID_ANALOG1, false);
 			GSensorDDSMem sensorDDSMem = labQuest.ddsMemGetRecord(NGIOSourceCmds.CHANNEL_ID_ANALOG1);
 
 			byte [] unitBuf = sensorDDSMem.CalibrationPage[sensorDDSMem.ActiveCalPage].Units;
 			units = Native.toString(unitBuf);
 		}
 
 		// period in seconds
 		labQuest.setMeasurementPeriod((byte)-1, 0.1);
 
 		// send a NGIO_CMD_ID_SET_SENSOR_CHANNEL_ENABLE_MASK
 		labQuest.setSensorChannelEnableMask(0x02);
 
 		// Send a NGIO_CMD_ID_START_MEASUREMENTS
 		labQuest.startMeasurements();
 
 		// NGIO_Device_ReadRawMeasurements();
 		int [] pMeasurementsBuf = new int [1000];
 		for(int count=0; count<10; count++){
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			int numMeasurements = labQuest.readRawMeasurementsAnalog(
 					NGIOSourceCmds.CHANNEL_ID_ANALOG1, 
 					pMeasurementsBuf, pMeasurementsBuf.length);
 			for(int i=0; i<numMeasurements; i++){					
 				float calibratedData = labQuest.calibrateData2(
 						NGIOSourceCmds.CHANNEL_ID_ANALOG1, pMeasurementsBuf[i]); 
 				System.out.println("value: " + calibratedData + " " + units);
 			}
 
 
 		}
 
 		// NGIO_CMD_ID_STOP_MEASUREMENTS
 		labQuest.stopMeasurements();
 
 
 		// need to clear the buffer before reading more
 	}
 
 	public static void testMotion() throws LabQuestException {		
 		labQuest.acquireExclusiveOwnership();
 
 		labQuest.printAttachedSensors();
 				
 		int channelId = labQuest.getSensorId(NGIOSourceCmds.CHANNEL_ID_DIGITAL1);
 		if(channelId != 2){
 			System.err.println("didn't find the motion sensor on the first digitial channel");
 			throw new RuntimeException();
 		}
 		
 		// period in seconds
 		labQuest.setMeasurementPeriod((byte)-1, 1);
 		
 		// send a NGIO_CMD_ID_SET_SENSOR_CHANNEL_ENABLE_MASK
 		labQuest.setSensorChannelEnableMask(1 << 5);
 		
 		labQuest.setSamplingMode(NGIOSourceCmds.CHANNEL_ID_DIGITAL1, 
 				NGIOSourceCmds.SAMPLING_MODE_PERIODIC_MOTION_DETECT);
 		
 		labQuest.clearIO(NGIOSourceCmds.CHANNEL_ID_DIGITAL1);
 		
 		labQuest.startMeasurements();
 				
 		// NGIO_Device_ReadRawMeasurements();
 		int [] pMeasurementsBuf = new int [1000];
 		long [] pTimestampsBuf = new long [1000];
 		for(int count=0; count<10; count++){
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			int numMeasurements = labQuest.readRawMeasurementsMotion(
 					NGIOSourceCmds.CHANNEL_ID_DIGITAL1, pMeasurementsBuf,
 					pTimestampsBuf, pMeasurementsBuf.length);
 		}
 		
 		// NGIO_CMD_ID_STOP_MEASUREMENTS
 		labQuest.stopMeasurements();
 				
 		// need to clear the buffer before reading more
 	}
 
 }
