 package com.quanleimu.util;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.util.Log;
 
 import com.quanleimu.activity.QuanleimuApplication;
 
 //singleton
 public class BxSender implements Runnable{
 //	private boolean isQueueReady;
 	private Context context = null;
 	private static String apiName = "trackdata";
 	private int sendingTimes = 0;
 	private List<ArrayList<BxTrackData>> queue = null;
 	private static final String SERIALIZABLE_SENDER_DIR = "BxLogDir";
 	private static final String SERIALIZABLE_SENDER_FILE_PREFIX = "bx_sender";//记录文件
 	private static final String SERIALIZABLE_SENDER_FILE_SUFFIX = ".ser";//记录文件
 	
 	private Object sendMutex = new Object();
 
 	
 	
 	//singleton
 	private static BxSender instance = null;
 	public static BxSender getInstance() {
 		if (instance == null) {// && BxMobileConfig.getInstance().getLoggingFlag() == true
 			instance = new BxSender();
 		}
 		return instance;
 	}
 	private BxSender() {//构造器
 		this.context = QuanleimuApplication.context;
 		queue = new ArrayList<ArrayList<BxTrackData>>();
 		startThread();
 	}
 	
 	private void startThread() {
 		new Thread(this).start();
 	}
 	
 	public void notifyNetworkReady()
 	{
 		synchronized (sendMutex) {
 			this.sendMutex.notifyAll();
 		}
 	}
 	
 	public void addToQueue(ArrayList<BxTrackData> dataList) {
 		List<BxTrackData> newList = new ArrayList<BxTrackData>();
 		newList.addAll(dataList);
 		synchronized (queue) {
 			queue.add((ArrayList<BxTrackData>)newList);
 		}
 		
 		//Notify send thread to send data.
 		synchronized (sendMutex) {
 			sendMutex.notifyAll();
 		}
 	}
 	
 	public List<ArrayList<BxTrackData>> getQueue() {
 		return queue;
 	}
 
 	private boolean hasDataToSend() {
 		int size = 0;
 		synchronized (this.queue) {
 			size = queue.size();
 		}
 		return (size > 0) || loadRecord() != null;
 	}
 	
 	private boolean isSendingReady() {
 		return Communication.isNetworkActive();
 	}
 	
 //	private Object loadFromLocal(String file) {
 //		return Util.loadDataFromLocate(this.context, file);
 //	}
 	
 	//save queue
 	public void save() {
 		String fileName = "";
 		for (ArrayList<BxTrackData> data : queue) {
 			saveListToFile(data);
 		} 
 		
 		queue.clear();
 	}
 	
 	private void saveListToFile(ArrayList<BxTrackData> data)
 	{
 		String fileName = SERIALIZABLE_SENDER_FILE_PREFIX + System.currentTimeMillis()/1000 + SERIALIZABLE_SENDER_FILE_SUFFIX;
 		Util.saveSerializableToPath(context, SERIALIZABLE_SENDER_DIR, fileName, data);
 	}
 	
 	
 	//load queue
 //	public void load() {
 //		List<String> list = Util.listFiles(context, SERIALIZABLE_SENDER_DIR);
 //		if (list.size() > 0) {
 //			for(String file : list) {
 //				synchronized (queue) {
 //					queue.add((ArrayList<BxTrackData>)Util.loadSerializable(file));
 //				}
 //				Util.clearData(context, file);
 //				new File(file).delete();
 //			}
 //		}
 //	}
 	
 	private String loadRecord()
 	{
 		List<String> list = Util.listFiles(context, SERIALIZABLE_SENDER_DIR);
 		if (list == null || list.size() == 0)
 		{
 			return null;
 		}
 		
 		return list.get(0);
 		
 	}
 	
 	private String convertListToJson(List<BxTrackData> list) {
 		String result = "[";
 		for (BxTrackData d : list) {
 			result += d.toJsonObj().toString() + ",";
 		}
 		result = result.substring(0, result.length()-1);
 		result += "]";
 		return result;
 	}
 	
 	private boolean sendList(final List<BxTrackData> list) {
 		String jsonStr = convertListToJson(list);
 
 		boolean succed = Communication.executeSyncPostTask(apiName, jsonStr);
 		return succed;
 	}
 	
 	@Override
 	public void run() {
 		while(true) {
 				//file ready & sending ready
 				Log.d("BxSender","ready to send~");
 
 				//First step : send memory data if there is any.
 				ArrayList<BxTrackData> list = null;
 				synchronized (queue) {
					list = queue.remove(0);
 				}
 				
 				if (list != null) {
 					boolean succed = sendList(list);
 					
 					if (!succed) {
 						saveListToFile(list);
 					}
 				}
 				else	// Send persistence data if there is any.
 				{
 					String recordPath = loadRecord();
 					if (recordPath != null)
 					{
 						ArrayList<BxTrackData> singleRecordList = (ArrayList<BxTrackData>)Util.loadSerializable(recordPath);
 						if (singleRecordList != null && sendList(singleRecordList))
 						{
 							new File(recordPath).delete();
 						}
 					}
 				}
 				
 				//Check if we have more data to send.
 				boolean hasMoreData = this.hasDataToSend();
 				
 				while (!isSendingReady() || !hasMoreData) {
 					try {
 						Log.d("BxSender", "wait()");
 						synchronized (sendMutex) {
 							wait(300000);//wait time out 5 min
 						}
 						
 						hasMoreData = this.hasDataToSend();
 						Log.d("BxSender", "wake up~~");
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 				
 				
 		}//while true
 	}
 
 }
