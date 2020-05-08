 package snmpadapter;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 
 import snmp.SNMPObject;
 import snmp.SNMPSequence;
 import snmp.SNMPVarBindList;
 import snmp.SNMPv1CommunicationInterface;
 
 public class RMONAdapter extends Thread
 {
 	boolean mRunning;
 	boolean mEnable;
 	
 	int mQueueSize;
 	
 	ArrayList<RMONAlarmObject> mAlarmList;
 	ArrayList<RMONEventObject> mEventList;
 	ArrayList<Integer> mRMONAlarmInterval;
 	
 	public RMONAdapter()
 	{
 		mRunning = true;
 		mEnable = true;
 		
 		mQueueSize = 5;
 		
 		for(int lIndex = 0; lIndex < mQueueSize; lIndex++)
 		{
 			RMONAlarmObject lAObject = new RMONAlarmObject();
 			lAObject.setIndex(lIndex);
 			mAlarmList.add(lAObject);
 			RMONEventObject lEObject = new RMONEventObject();
 			lEObject.setIndex(lIndex);
 			mEventList.add(lEObject);
 			mRMONAlarmInterval.add(0);
 		}  // for
 	}  // RMONAdapter
 	
     @Override
     public void run()
     {
         while(mRunning)
         {
             try
             {
             	if(!mEnable)
             	{
             		continue;
             	}  // if
             	
             	updateRMONObjects();
             }  // try
             catch(Exception pException)
             {
             	pException.printStackTrace();
             }  // 
         }  // while
     }  // void run
     
     public void updateRMONObjects()
     {
 		for(int lIndex = 0; lIndex < mQueueSize; lIndex++)
 		{
 			mRMONAlarmInterval.set(lIndex, mRMONAlarmInterval.get(lIndex) + 1);
 			if(mRMONAlarmInterval.get(lIndex) == mAlarmList.get(lIndex).getInverval() &&
 					mAlarmList.get(lIndex).getInverval() != 0)
 			{
 				updateSNMPCommand(mAlarmList.get(lIndex));
 				
 				mRMONAlarmInterval.set(lIndex, 0);
 			}  // if
 		}  // for
     }  // void performSNMPCommand
     
     public void updateSNMPCommand(RMONAlarmObject pObject)
     {
     	if(pObject.getStatus() != null)
     	{
     		if(pObject.getStatus().equalsIgnoreCase("Start"))
     		{
     			int lInputValue = -1;
     			
     			 try
     		        {
     		        	// Create a communication
     		        	InetAddress lHostAddress = 
     		        			InetAddress.getByName("localhost");
     		        	
     		        	int lVersion = 0;
     		        	
     		        	SNMPv1CommunicationInterface lInterface = new SNMPv1CommunicationInterface(lVersion, lHostAddress, "cs158bwrite");
     		        	    		        	
     		        	SNMPVarBindList lSNMPVar = lInterface.getMIBEntry(pObject.getVariable());
     		        	SNMPSequence lPair = (SNMPSequence)(lSNMPVar.getSNMPObjectAt(0));
     		        	SNMPObject lSNMPValue = lPair.getSNMPObjectAt(1);
     		        	String lSNMPValueType = lSNMPValue.getClass().toString();
     		        			
     		        	if(lSNMPValueType.equals("class snmp.SNMPIngeger"))
     		        	{
     		        		lInputValue = (Integer)lSNMPValue.getValue();
     		        	}  // if
     		        	
     		        	lInterface.closeConnection();
     		        }  // try
     		        catch(UnknownHostException pException)
     		        {
     		        	pException.printStackTrace();
     		        }  // catch
     		        catch(NumberFormatException pException)
     		        {
     		        	pException.printStackTrace();
     		        }  // catch
     		        catch(Exception pException)
     		        {
     		        	pException.printStackTrace();
     		        }  // catch
     			
     			if(lInputValue < 0)
     			{
     				return;
     			}  // if
     			if(lInputValue >= pObject.getRisingThreshold())
     			{
     				logRMONEventObject(pObject.getRisingEventIndex());
     			}  // if
     			else if(lInputValue <= pObject.getFallingThreshold())
     			{
     				logRMONEventObject(pObject.getFallingEventIndex());
     			}  // else
     		}  // if
     	}  // if
     }  // void updateSNMPCommand
     
     public void logRMONEventObject(int pIndex)
     {
     	if(pIndex > 0 && pIndex < mQueueSize)
     	{
     		RMONEventObject lObject = mEventList.get(pIndex);
     		lObject.setLastTimeSent(System.currentTimeMillis() + "");
             
     		BufferedWriter lWriter;
 			try {
 				lWriter = new BufferedWriter(new FileWriter("Output_Event.txt", true));
 				lWriter.write(lObject.toString());
 		        lWriter.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
                        
     		System.out.println(lObject.toString());
     	}  // if
     }  // void logRMONEventObject
     
     public boolean updateQueueSize(int pQueueSize)
     {
     	if(pQueueSize > 0 && pQueueSize < 10)
     	{
     		
     		if(mQueueSize < pQueueSize)
     		{
     			for(int lIndex = mQueueSize; lIndex < pQueueSize; lIndex++)
     			{
     				RMONAlarmObject lAObject = new RMONAlarmObject();
     				lAObject.setIndex(lIndex);
     				mAlarmList.add(lAObject);
     				RMONEventObject lEObject = new RMONEventObject();
     				lEObject.setIndex(lIndex);
     				mEventList.add(lEObject);
     				mRMONAlarmInterval.add(0);
     			}  // for
     		}  // if
     		else if(mQueueSize > pQueueSize)
     		{
     			
     			for(int lIndex = mQueueSize - 1; lIndex >= pQueueSize; lIndex--)
     			{
     				mAlarmList.remove(lIndex);
     				mEventList.remove(lIndex);
     				mRMONAlarmInterval.remove(lIndex);
     			}  // for
     		}  // else if
     			
     		return true;
     	}  // if
     	
     	return false;
     }  // void updateQueueSize
     
     public void updateRMONAlarm(RMONAlarmObject pObject)
     {
     	int pUpdateIndex = pObject.getIndex();
     	
     	if(pUpdateIndex > 0 && pUpdateIndex < mQueueSize)
     	{
     		mAlarmList.set(pUpdateIndex, pObject);
     	}  // if
     }  // void updateRMONAlarm
 
     public void updateRMONEvent(RMONEventObject pObject)
     {
     	int pUpdateIndex = pObject.getIndex();
     	
     	if(pUpdateIndex > 0 && pUpdateIndex < mQueueSize)
     	{
     		mEventList.set(pUpdateIndex, pObject);
     	}  // if
     }  // void updateRMONAlarm
     
     public String getRMONAlarm()
     {
 		StringBuilder lString = new StringBuilder();
 		lString.append("[");
 
 		for(int lIndex = 0; lIndex < mQueueSize; lIndex++)
 		{
 			lString.append(mAlarmList.get(lIndex));
 		}  // for
 		
 		lString.append("]");
 
 		return lString.toString();
     }
     
     public String getRMONEvent()
     {
 		StringBuilder lString = new StringBuilder();
 		lString.append("[");
 
 		for(int lIndex = 0; lIndex < mQueueSize; lIndex++)
 		{
 			lString.append(mEventList.get(lIndex));
 		}  // for
 		
 		lString.append("]");
 
 		return lString.toString();
     }  // String getRMONEvent
     
     public boolean getRunning()
     {
     	return mRunning;
     }  // boolean getRunning
     
     public void setRunning(boolean pRunning)
     {
     	mRunning = pRunning;
     }  // void setRunning
     
     public boolean getEnable()
     {
     	return mEnable;
     }  // getRunning
     
     public void setEnable(boolean pEnable)
     {
     	if(pEnable)
     	{
     		System.out.println("Enable RMON");
     	}  // if
     	else
     	{
     		System.out.println("Disable RMON");
     	}  // else
     	
     	mEnable = pEnable;
     }  // getEnable
 }  // class RMONAdapter
