 package org.concord.waba.extra.probware.probs;
 import org.concord.waba.extra.event.DataListener;
 import org.concord.waba.extra.event.DataEvent;
 import extra.util.DataDesc;
 import org.concord.waba.extra.probware.*;
 import extra.util.*;
 
 public class CCRawData extends CCProb{
 float  			[]rawData = new float[CCInterfaceManager.BUF_SIZE];
 int  			[]rawIntData = new int[CCInterfaceManager.BUF_SIZE];
 int				firstIndex,secondIndex;
 
 
 public final static int		SAMPLING_24BIT_MODE = 0;
 public final static int		SAMPLING_10BIT_MODE = 1;
 
 	String	[]samplingModes =  {"24 Bit","10 Bit"};
     String [] channelNames = {"0", "1"};
     String [] speedNames = {3 + speedUnit};
 
 	PropObject sampProp = new PropObject("Sampling", "Sampling", PROP_SAMPLING, samplingModes);
 	PropObject chanProp = new PropObject("Channel #", "Channel", PROP_CHAN_NUM, channelNames);
 	PropObject speedProp = new PropObject("Speed", "Speed", PROP_SPEED, speedNames);
 
     int curChannel = 0;
 
 	CCRawData(boolean init, String name, int interfaceT){
 		super(init, name, interfaceT);
 
 		probeType = ProbFactory.Prob_RawData;
 		activeChannels = 2;
 		defQuantityName = "Voltage";
 
 		dDesc.setChPerSample(2);
 		dDesc.setIntChPerSample(2);
 		dDesc.setDt(0.0f);
 		dEvent.setDataDesc(dDesc);
 		dEvent.setDataOffset(0);
 		dEvent.setNumbSamples(1);
 		dEvent.setData(rawData);
 		dEvent.setIntData(rawIntData);
 
 		addProperty(sampProp);
 		addProperty(chanProp);
 		addProperty(speedProp);
 
 		unit = CCUnit.UNIT_CODE_VOLT;			
 	}
 
 	public void setDataDescParam(int chPerSample,float dt){
 		dDesc.setDt(dt);
 		dDesc.setChPerSample(chPerSample);
 		dDesc.setIntChPerSample(chPerSample);
 	}
 
 	public String getLabel()
 	{
 		return "Voltage " + "Ch. " + curChannel;
 	}
 
     public boolean startSampling(org.concord.waba.extra.event.DataEvent e){
 		dEvent.type = e.type;
 		dDesc.setDt(e.getDataDesc().getDt());
 		// Change to Volts
 		dDesc.setTuneValue(e.getDataDesc().getTuneValue()/1000f);
 		if(activeChannels == 2){
 			dDesc.setChPerSample(2);
 			dDesc.setIntChPerSample(2);
 			firstIndex = (curChannel == 1)?1:0;
 			secondIndex = (curChannel == 1)?0:1;
 		}else{
 			dDesc.setChPerSample(1);
 			dDesc.setIntChPerSample(1);
 			firstIndex = secondIndex = 0;
 		}
 		return super.startSampling(dEvent);
    }
 
     public boolean dataArrived(DataEvent e)
     {
 		int nOffset 		= e.getDataOffset();
 		int[] data = e.getIntData();
 		int  	chPerSample = e.dataDesc.chPerSample;
 		int		nSamples	= e.getNumbSamples();
 		int 	ndata 		= nSamples*chPerSample;
 		int 	v = 0,v1 = 0;
 		dEvent.type = e.type;
 		dEvent.intTime = e.intTime;
 		int j=0;
 		for(int i = nOffset; i < ndata; i+=chPerSample){
 			if(activeChannels == 1){
 				v = data[i];
 				rawIntData[j] = v;
 				rawData[j] = (float)v*dDesc.tuneValue;
 				j++;
 			}else{
 				v = data[i+firstIndex];
 				rawIntData[j] = v;
 				rawData[j] = (float)v*dDesc.tuneValue;
 				j++;
 
 				v1 = data[i+secondIndex];
 				rawIntData[j] = v1;
 				rawData[j] = (float)v1*dDesc.tuneValue;
 				j++;
 			}
 		}
 		dEvent.setNumbSamples(nSamples);
 		return super.dataArrived(dEvent);
     }
 
 	public boolean visValueChanged(PropObject po)
 	{
 		PropObject sampMode = sampProp;
 		PropObject chNum = chanProp;
 		PropObject speed = speedProp;
 
 		int index = po.getVisIndex();
 		if(po == sampMode){
 			if(index == 0){
 				String [] newSpeedNames = {3 + speedUnit};
 				speed.setVisPossibleValues(newSpeedNames);
 			} else if(index == 1){
 				if(chNum.getVisIndex() == 0){
 					String [] newSpeedNames = {200 + speedUnit, 400 + speedUnit};
 					speed.setVisPossibleValues(newSpeedNames);
 				} else {
 					String [] newSpeedNames = {200 + speedUnit};
 					speed.setVisPossibleValues(newSpeedNames);
 				}
 			}
 		} else if(po == chNum){
 			if(sampMode.getVisIndex() == 1){
 				if(chNum.getVisIndex() == 0){
 					String [] newSpeedNames = {200 + speedUnit, 400 + speedUnit};
 					speed.setVisPossibleValues(newSpeedNames);
 				} else {
 					String [] newSpeedNames = {200 + speedUnit};
 					speed.setVisPossibleValues(newSpeedNames);
 				}				
 			}
 		}
 		return true;
 	}
 
 	// need a function this called to setup the probe before
 	// it is started
 	public int getInterfaceMode()
 	{
 		int modeIndex = sampProp.getIndex();
 		int chIndex = chanProp.getIndex();
 		if(modeIndex == 0){
 			interfaceMode = CCInterfaceManager.A2D_24_MODE;
 			if(chIndex == 0){
 				curChannel = 0;
 				activeChannels = 1;
 			} else {
 				curChannel = 1;
 				activeChannels = 2;
 			}
 		} else if(modeIndex == 1){
 			interfaceMode = CCInterfaceManager.A2D_10_MODE;
 			if(chIndex == 0){
 				curChannel = 0;
 			} else {
 				curChannel = 1;
 				activeChannels = 2;
 			}
			if(speedProp.getIndex() == 0){
 				activeChannels = 2;
 			} else {
 				activeChannels = 1;
 			}
 		}
 
 		return interfaceMode;
 	}
 }
