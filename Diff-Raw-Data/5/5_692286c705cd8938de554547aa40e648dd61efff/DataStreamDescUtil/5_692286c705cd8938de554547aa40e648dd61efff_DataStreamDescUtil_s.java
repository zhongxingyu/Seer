 /*
  * Created on Jan 26, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.concord.sensor.impl;
 
 import org.concord.framework.data.stream.DataChannelDescription;
 import org.concord.framework.data.stream.DataStreamDescription;
 import org.concord.sensor.ExperimentConfig;
 import org.concord.sensor.ExperimentRequest;
 import org.concord.sensor.SensorConfig;
 import org.concord.sensor.SensorRequest;
 
 /**
  * @author scott
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class DataStreamDescUtil 
 {
 	/**
 	 * The result can be null. 
 	 * 
 	 * @param dDesc
 	 * @param request
 	 * @param result
 	 */
 	public static void setupDescription(DataStreamDescription dDesc,
 			ExperimentRequest request, 
 			ExperimentConfig result)
 	{
 		SensorConfig [] sensConfigs = null;
 		SensorRequest [] sensRequests = request.getSensorRequests();		
 		int firstValueChannelIndex = 0;
 		
 		if(result != null) {
 			sensConfigs = result.getSensorConfigs();
 		}
 		
 		dDesc.setChannelsPerSample(sensRequests.length);
 		
 		if(result != null) {
 			dDesc.setDt(result.getPeriod());
 			if(result.getExactPeriod()) {
 				dDesc.setDataType(DataStreamDescription.DATA_SEQUENCE);
 			} else {
 				dDesc.setDataType(DataStreamDescription.DATA_SERIES);
 				DataChannelDescription chDescrip = new DataChannelDescription();
 				chDescrip.setName("time");
 				chDescrip.setUnit(new SensorUnit("s"));
 				chDescrip.setPrecision(-2);
 				chDescrip.setNumericData(true);
 				dDesc.setChannelDescription(chDescrip, 0);				
 				firstValueChannelIndex = 1;
 				dDesc.setChannelsPerSample(sensRequests.length+1);
 			}
 		} else {
 			dDesc.setDt(request.getPeriod());			
 		}
 		
		for(int i=firstValueChannelIndex; i<sensRequests.length; i++) {
 			DataChannelDescription chDescrip = new DataChannelDescription();
 			if(result != null) {
 				chDescrip.setName(sensConfigs[i].getName());
 			}
 			chDescrip.setUnit(sensRequests[i].getUnit());
 			
 			chDescrip.setPrecision(sensRequests[i].getDisplayPrecision());			
 			chDescrip.setNumericData(true);
			dDesc.setChannelDescription(chDescrip, i);
 		}		
 	}
 }
