 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Created on Dec 10, 2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.concord.sensor.nativelib;
 
 import org.concord.framework.data.DataDimension;
 import org.concord.framework.data.stream.DataConsumer;
 import org.concord.framework.data.stream.DataListener;
 import org.concord.framework.data.stream.DataProducer;
 import org.concord.framework.data.stream.DataStreamEvent;
 import org.concord.framework.text.UserMessageHandler;
 import org.concord.sensor.DeviceConfig;
 import org.concord.sensor.ExperimentRequest;
 import org.concord.sensor.SensorDataManager;
 import org.concord.sensor.SensorDataProducer;
 import org.concord.sensor.SensorRequest;
 import org.concord.sensor.device.impl.DeviceConfigImpl;
 import org.concord.sensor.device.impl.InterfaceManager;
 import org.concord.sensor.device.impl.JavaDeviceFactory;
 
 /**
  * @author Informaiton Services
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class TestNative 
 {
 	static SensorDataProducer dataProducer;
 
 	public static void main(String[] args) 
 	{
 		UserMessageHandler messenger = new MyUserMessageHandler();
 		SensorDataManager  sdManager = new InterfaceManager(messenger);
 		
 		// This should be loaded from the OTrunk.  Each computer
 		// might have a different set of devices configured.
 		DeviceConfig [] dConfigs = new DeviceConfig[1];
 		dConfigs[0] = new DeviceConfigImpl(JavaDeviceFactory.VERNIER_GO_LINK, null);		
 		((InterfaceManager)sdManager).setDeviceConfigs(dConfigs);
 				
 		ExperimentRequest request = new ExperimentRequest(){
 			/* (non-Javadoc)
 			 * @see org.concord.sensor.ExperimentRequest#getPeriod()
 			 */
 			public float getPeriod() 
 			{
 
 				return 0;
 			}
 			
 			/* (non-Javadoc)
 			 * @see org.concord.sensor.ExperimentRequest#getNumberOfSamples()
 			 */
 			public int getNumberOfSamples() 
 			{
 				return -1;
 			}
 			
 			/* (non-Javadoc)
 			 * @see org.concord.sensor.ExperimentRequest#getSensorRequests()
 			 */
 			public SensorRequest[] getSensorRequests() {
 				// TODO Auto-generated method stub
 				SensorRequest [] sensors = new SensorRequest[1];
 				sensors[0] = new SensorRequest(){
 					/* (non-Javadoc)
 					 * @see org.concord.sensor.SensorRequest#getDisplayPrecision()
 					 */
 					public int getDisplayPrecision() {
 						// TODO Auto-generated method stub
 						return -2;
 					}
 					
 					/* (non-Javadoc)
 					 * @see org.concord.sensor.SensorRequest#getRequiredMax()
 					 */
 					public float getRequiredMax() 
 					{
 						return Float.NaN;
 					}
 					
 					/* (non-Javadoc)
 					 * @see org.concord.sensor.SensorRequest#getRequiredMin()
 					 */
 					public float getRequiredMin() 
 					{
 						return Float.NaN;
 					}
 					
 					/* (non-Javadoc)
 					 * @see org.concord.sensor.SensorRequest#getPort()
 					 */
 					public int getPort() {
 						// TODO Auto-generated method stub
 						return 0;
 					}
 					
 					/* (non-Javadoc)
 					 * @see org.concord.sensor.SensorRequest#getSensorParam(java.lang.String)
 					 */
 					public String getSensorParam(String key) {
 						// TODO Auto-generated method stub
 						return null;
 					}
 					
 					/* (non-Javadoc)
 					 * @see org.concord.sensor.SensorRequest#getStepSize()
 					 */
 					public float getStepSize() {
 						// TODO Auto-generated method stub
 						return 0.1f;
 					}
 					/* (non-Javadoc)
 					 * @see org.concord.sensor.SensorRequest#getType()
 					 */
 					public int getType() {
 						// TODO Auto-generated method stub
 						return 0;
 					}
 					/* (non-Javadoc)
 					 * @see org.concord.sensor.SensorRequest#getUnit()
 					 */
 					public DataDimension getUnit() {
 						// TODO Auto-generated method stub
 						return null;
 					}
 				};
 				return null;
 			}
 		};
 		
 		SensorDataProducer sDataProducer = 
 		    sdManager.createDataProducer();
 		sDataProducer.configure(request);
 		sDataProducer.addDataListener(new DataListener(){
 			public void dataReceived(DataStreamEvent dataEvent)
 			{
 				int numSamples = dataEvent.getNumSamples();
 				float [] data = dataEvent.getData();
 				if(numSamples > 0) {
 					System.out.println("" + numSamples + " " +
 								data[0]);
 					System.out.flush();
 				} 
 				else {
 					System.out.println("" + numSamples);
 				}
 			}
 
 			public void dataStreamEvent(DataStreamEvent dataEvent)
 			{				
 				String eventString;
 				int eventType = dataEvent.getType();
 				
 				if(eventType == 1001) return;
 				
 				switch(eventType) {
 					case DataStreamEvent.DATA_READY_TO_START:
 						eventString = "Ready to start";
 					break;
 					case DataStreamEvent.DATA_STOPPED:
 						eventString = "Stopped";
 					break;
 					case DataStreamEvent.DATA_DESC_CHANGED:
 						eventString = "Description changed";
 					break;
 					default:
 						eventString = "Unknown event type";					
 				}
 				
 				System.out.println("Data Event: " + eventString); 
 			}
 		});
 		
 		dataProducer.start();
 		
 		System.out.println("started device");
 		try {
 			Thread.sleep(1000);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		dataProducer.stop();
 		
 		dataProducer.close();
 		
 		System.exit(0);
 	}
 }
 
 class MyUserMessageHandler
 implements UserMessageHandler
 {
 
 	/**
 	 * @see org.concord.framework.text.UserMessageHandler#showOptionMessage(java.lang.String, java.lang.String, java.lang.String[], java.lang.String)
 	 */
 	public int showOptionMessage(String message, String title, String[] options, String defaultOption) {
 		System.out.println(title + ": " + message);
 		String optionStr = "(";
 		for(int i=0; i<options.length; i++) {
 			optionStr += " " + options[i];
 			if(options[i].equals(defaultOption)){
 				optionStr += "+";
 			}
 		}
 		System.out.println(optionStr + " )");
 		return 0;
 	}
 
 	/**
 	 * @see org.concord.framework.text.UserMessageHandler#showMessage(java.lang.String, java.lang.String)
 	 */
 	public void showMessage(String message, String title) {
 		System.out.println(title + ": " + message);
 	}
 	
 }
