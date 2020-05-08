 package com.dbstar.service;
 
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import com.dbstar.util.GDNetworkUtil;
 import com.dbstar.DbstarDVB.DbstarServiceApi;
 import com.dbstar.model.ColumnData;
 import com.dbstar.model.ContentData;
 import com.dbstar.model.EventData;
 import com.dbstar.model.GDCommon;
 import com.dbstar.model.GDSystemConfigure;
 import com.dbstar.model.GDDataModel;
 import com.dbstar.model.GDNetModel;
 import com.dbstar.model.GuideListItem;
 import com.dbstar.model.PreviewData;
 import com.dbstar.model.ReceiveEntry;
 import com.dbstar.model.UserData;
 import com.dbstar.service.client.GDDBStarClient;
 
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Binder;
 import android.os.Message;
 import android.util.Log;
 import android.os.Process;
 
 public class GDDataProviderService extends Service {
 
 	private static final String TAG = "GDDataProviderService";
 
 	public static final int REQUESTTYPE_GETCOLUMNS = 1;
 	public static final int REQUESTTYPE_GETPUBLICATION = 2;
 	public static final int REQUESTTYPE_GETPUBLICATIONSET = 3;
 	public static final int REQUESTTYPE_GETPUBLICATIONS_OFSET = 4;
 	public static final int REQUESTTYPE_GETIMAGE = 6;
 	public static final int REQUESTTYPE_GETDETAILSDATA = 7;
 	public static final int REQUESTTYPE_GETDOWNLOADSTATUS = 8;
 
 	public static final int REQUESTTYPE_GETPOWERCONSUMPTION = 9;
 	public static final int REQUESTTYPE_GETTOTALCOSTBYCHARGETYPE = 10;
 
 	public static final int REQUESTTYPE_SETSETTINGS = 11;
 	public static final int REQUESTTYPE_GETSETTINGS = 12;
 
 	public static final int REQUESTTYPE_STARTGETTASKINFO = 13;
 	public static final int REQUESTTYPE_STOPGETTASKINFO = 14;
 
 	public static final int REQUESTTYPE_GETFAVORITEMOVIE = 15;
 	public static final int REQUESTTYPE_GETFAVORITETV = 16;
 	public static final int REQUESTTYPE_GETFAVORITERECORD = 17;
 	public static final int REQUESTTYPE_GETFAVORITEENTERTAINMENT = 18;
 
 	public static final int REQUESTTYPE_GETGUIDELIST = 20;
 	public static final int REQUESTTYPE_UPDATEGUIDELIST = 21;
 	public static final int REQUESTTYPE_GETPREVIEWS = 22;
 
 	public static final int REQUESTTYPE_GETUSERDATA = 23;
 	public static final int REQUESTTYPE_GETDEVICEDATA = 24;
 	public static final int REQUESTTYPE_GETHELPINFO = 25;
 
 	private static final String PARAMETER_COLUMN_ID = "column_id";
 	private static final String PARAMETER_SET_ID = "set_id";
 	private static final String PARAMETER_PAGENUMBER = "page_number";
 	private static final String PARAMETER_PAGESIZE = "page_size";
 	private static final String PARAMETER_CONTENTDATA = "content_data";
 
 	private static final String PARAMETER_CCID = "cc_id";
 	private static final String PARAMETER_DATESTART = "date_start";
 	private static final String PARAMETER_DATEEND = "date_end";
 	private static final String PARAMETER_CHAREGTYPE = "charge_type";
 
 	private static final String PARAMETER_KEY = "key";
 	private static final String PARAMETER_VALUE = "value";
 
 	private static final String SmartHomePrepertyName = "dbstar.smarthome.started";
 
 	private Object mTaskQueueLock = new Object();
 	private LinkedList<RequestTask> mTaskQueue = null;
 
 	private Object mFinishedTaskQueueLock = new Object();
 	private LinkedList<RequestTask> mFinishedTaskQueue = null;
 
 	private int mThreadCount = 2;
 	private List<WorkerThread> mThreadPool = new LinkedList<WorkerThread>();
 
 	private int mMainThreadId;
 	private int mMainThreadPriority;
 
 	GDSystemConfigure mConfigure = null;
 
 	GDDataModel mDataModel = null;
 	GDNetModel mNetModel = null;
 
 	ConnectivityManager mConnectManager;
 	GDDiskSpaceMonitor mDiskMonitor;
 
 	GDDBStarClient mDBStarClient;
 	GDApplicationObserver mApplicationObserver = null;
 	ClientObserver mPageOberser = null;
 
 	private final IBinder mBinder = new DataProviderBinder();
 	SystemEventHandler mHandler = null;
 
 	boolean mIsSmartHomeServiceStarted = false;
 	boolean mIsDbServiceStarted = false;
 	boolean mIsStorageReady = false;
 	boolean mIsNetworkReady = false;
 
 	String mMacAddress = "";
 
 	private class RequestTask {
 		public static final int INVALID = 0;
 		public static final int ACTIVE = 1;
 
 		long Id;
 		int Type;
 		int Flag;
 
 		ClientObserver Observer;
 		int Index;
 
 		int PageNumber;
 		int PageSize;
 
 		int ColumnLevel;
 
 		Map<String, Object> Parameters;
 
 		Object Key;
 		Object Data;
 
 		public RequestTask() {
 			Flag = ACTIVE;
 		}
 	};
 
 	public void registerAppObserver(GDApplicationObserver observer) {
 		mApplicationObserver = observer;
 	}
 
 	public void unRegisterAppObserver(GDApplicationObserver observer) {
 		if (mApplicationObserver == observer) {
 			mApplicationObserver = null;
 		}
 	}
 
 	public void registerPageObserver(ClientObserver observer) {
 		mPageOberser = observer;
 	}
 
 	public void unRegisterPageObserver(ClientObserver observer) {
 		if (mPageOberser == observer) {
 			mPageOberser = null;
 		}
 	}
 
 	public class DataProviderBinder extends Binder {
 		public GDDataProviderService getService() {
 			return GDDataProviderService.this;
 		}
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mBinder;
 	}
 
 	public void onCreate() {
 		super.onCreate();
 		Log.d(TAG, "onCreate");
 
 		mMainThreadId = Process.myTid();
 		mMainThreadPriority = Process.getThreadPriority(mMainThreadId);
 
 		mHandler = new SystemEventHandler();
 
 		mConfigure = new GDSystemConfigure();
 		mDataModel = new GDDataModel();
 		mNetModel = new GDNetModel();
 		mDiskMonitor = new GDDiskSpaceMonitor(mHandler);
 		mDBStarClient = new GDDBStarClient(this);
 
 		mTaskQueue = new LinkedList<RequestTask>();
 		mFinishedTaskQueue = new LinkedList<RequestTask>();
 
 		for (int i = 0; i < mThreadCount; i++) {
 			WorkerThread thread = new WorkerThread();
 			thread.start();
 
 			mThreadPool.add(thread);
 		}
 		
 		registerUSBReceiver();
 		reqisterConnectReceiver();
 		reqisterSystemMessageReceiver();
 
 		// read configure
 		mConfigure.readConfigure();
 
 		// check storage
 		if (mConfigure.configureStorage()) {
 			String disk = mConfigure.getStorageDisk();
 			Log.d(TAG, "monitor disk " + disk);
 
 			if (!disk.isEmpty()) {
 				mIsStorageReady = true;
 				Log.d(TAG, "disk is ready " + disk);
 
 				mDiskMonitor.addDiskToMonitor(disk);
 			}
 		}
 
 		// check network
 		mIsNetworkReady = isNetworkConnected();
 		Log.d(TAG, "network is connected " + mIsNetworkReady);
 
 		// start Dbstar service
 		mIsDbServiceStarted = false;
 		mDBStarClient.start();
 
 		// start smart home service
 		mIsSmartHomeServiceStarted = false;
 		if (mIsStorageReady && mIsNetworkReady) {
 			startDbStarService();
 		}
 
 		// initialize engine
 		initializeDataEngine();
 		initializeNetEngine();
 	}
 
 	void initializeDataEngine() {
 		mDataModel.initialize(mConfigure);
 		mDataModel.setPushDir(mConfigure.getStorageDir());
 	}
 
 	void deinitializeDataEngine() {
 		mDataModel.deInitialize();
 	}
 
 	void initializeNetEngine() {
 		mNetModel.initialize();
 	}
 
 	void deinitializeNetEngine() {
 		mNetModel.deinitialize();
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 		Log.d(TAG, "onDestroy");
 
 		stopDbStarService();
 		mDBStarClient.stop();
 		mDiskMonitor.stopMonitor();
 
 		deinitializeDataEngine();
 		deinitializeNetEngine();
 
 		unregisterReceiver(mNetworkReceiver);
 		unregisterReceiver(mUSBReceiver);
 		unregisterReceiver(mSystemMessageReceiver);
 
 		cancelAllRequests();
 
 		for (int i = 0; i < mThreadCount; i++) {
 			WorkerThread thread = mThreadPool.get(i);
 			thread.setExit(true);
 		}
 
 		synchronized (mTaskQueueLock) {
 			mTaskQueueLock.notifyAll();
 		}
 	}
 
 	private void startDbStarService() {
 		Log.d(TAG, "++++++++++++++++++startDbStarService++++++++++++++++++++");
 
 		if (mIsSmartHomeServiceStarted)
 			return;
 
 		mIsSmartHomeServiceStarted = true;
 
 		SharedPreferences settings = null;
 		SharedPreferences.Editor editor = null;
 
 		settings = getSharedPreferences(SmartHomePrepertyName, 0);
 		editor = settings.edit();
 		editor.putInt(SmartHomePrepertyName, 1);
 		editor.commit();
 	}
 
 	private void stopDbStarService() {
 		if (!mIsSmartHomeServiceStarted)
 			return;
 
 		mIsSmartHomeServiceStarted = false;
 
 		Log.d(TAG, "stopDbStarService");
 
 		SharedPreferences settings = null;
 		SharedPreferences.Editor editor = null;
 
 		settings = getSharedPreferences(SmartHomePrepertyName, 0);
 		editor = settings.edit();
 		editor.putInt(SmartHomePrepertyName, 0);
 		editor.commit();
 	}
 
 	class SystemEventHandler extends Handler {
 		public void handleMessage(Message msg) {
 			int msgId = msg.what;
 			switch (msgId) {
 			case GDCommon.MSG_TASK_FINISHED: {
 				RequestTask task = dequeueFinishedTask();
 
 				if (task != null) {
 					handleTaskFinished(task);
 				}
 				break;
 			}
 
 			case GDCommon.MSG_MEDIA_MOUNTED: {
 				Bundle data = msg.getData();
 				String disk = data.getString("disk");
 				// Log.d(TAG, "mount storage = " + disk);
 				Log.d(TAG, "++++++++ mount storage ++++++++" + disk);
 
 				String storage = "";
 				if (mConfigure.configureStorage()) {
 					storage = mConfigure.getStorageDisk();
 				}
 
 				if (disk.equals(storage) && mApplicationObserver != null) {
 					mIsStorageReady = true;
 					mConfigure.readConfigure();
 					mConfigure.configureStorage();
 
 					initializeDataEngine();
 
 					Log.d(TAG, " +++++++++++ monitor disk ++++++++" + disk);
 					mDiskMonitor.removeDiskFromMonitor(disk);
 					mDiskMonitor.addDiskToMonitor(disk);
 
 					if (mIsNetworkReady) {
 						startDbStarService();
 					}
 
 					notifyDbstarServiceStorageStatus();
 
 					// restart application
 					mApplicationObserver.initializeApp();
 				}
 				break;
 			}
 
 			case GDCommon.MSG_MEDIA_REMOVED: {
 				Bundle data = msg.getData();
 				String disk = data.getString("disk");
 
 				mDiskMonitor.removeDiskFromMonitor(disk);
 
 				String storage = mConfigure.getStorageDisk();
 				if (disk.equals(storage)) {
 					mIsStorageReady = false;
 
 					notifyDbstarServiceStorageStatus();
 
 					deinitializeDataEngine();
 					mApplicationObserver.deinitializeApp();
 				}
 				break;
 			}
 
 			case GDCommon.MSG_NETWORK_CONNECT:
 				getMacAddress();
 				Log.d(TAG, " +++++++++++++ network connected +++++++++++++");
 				mIsNetworkReady = true;
 				if (mIsStorageReady) {
 					startDbStarService();
 				}
 
 				notifyDbstarServiceNetworkStatus();
 
 				break;
 			case GDCommon.MSG_NETWORK_DISCONNECT:
 				Log.d(TAG, "++++++++++++ network disconnected +++++++++++");
 				mIsNetworkReady = false;
 				notifyDbstarServiceNetworkStatus();
 				stopDbStarService();
 
 				break;
 
 			case GDCommon.MSG_DISK_SPACEWARNING: {
 				Bundle data = msg.getData();
 				String disk = (String) data.get(GDCommon.KeyDisk);
 				if (mApplicationObserver != null) {
 					mApplicationObserver.handleNotifiy(
 							GDCommon.MSG_DISK_SPACEWARNING, disk);
 				}
 				break;
 			}
 
 			case GDCommon.MSG_SYSTEM_FORCE_UPGRADE:
 			case GDCommon.MSG_SYSTEM_UPGRADE: {
 				String packageFile = msg.getData().getString(
 						GDCommon.KeyPackgeFile);
 				mApplicationObserver.handleNotifiy(msgId, packageFile);
 
 				break;
 			}
 			
 			case GDCommon.MSG_USER_UPGRADE_CANCELLED: {
 				mDBStarClient.notifyDbServer(DbstarServiceApi.CMD_UPGRADE_CANCEL);
 				break;
 			}
 
 			case GDCommon.MSG_ADD_TO_FAVOURITE: {
 				String publicationId = msg.getData().getString(
 						GDCommon.KeyPublicationID);
 
 				String publicationSetId = msg.getData().getString(
 						GDCommon.KeyPublicationSetID);
 
 				if (mDataModel != null) {
 					mDataModel.addPublicationToFavourite(publicationSetId,
 							publicationId);
 				}
 				break;
 			}
 			case GDCommon.MSG_DELETE: {
 				String publicationId = msg.getData().getString(
 						GDCommon.KeyPublicationID);
 
 				String publicationSetId = msg.getData().getString(
 						GDCommon.KeyPublicationSetID);
 
 				if (mDataModel != null) {
 					mDataModel.deletePublication(publicationSetId,
 							publicationId);
 
 					if (mPageOberser != null) {
 						EventData.DeleteEvent event = new EventData.DeleteEvent();
 						event.PublicationId = publicationId;
 						event.PublicationSetId = publicationSetId;
 						mPageOberser.notifyEvent(EventData.EVENT_DELETE, event);
 					}
 				}
 				break;
 			}
 
 			default:
 				break;
 			}
 		}
 
 	}
 
 	private void handleTaskFinished(RequestTask task) {
 		Log.d(TAG, "handleTaskFinished type [" + task.Type + "]");
 
 		switch (task.Type) {
 		case REQUESTTYPE_GETCOLUMNS: {
 			if (task.Observer != null) {
 				task.Observer.updateData(task.Type, task.ColumnLevel,
 						task.Index, task.Data);
 			}
 			break;
 		}
 
 		case REQUESTTYPE_GETFAVORITEMOVIE:
 		case REQUESTTYPE_GETFAVORITETV:
 		case REQUESTTYPE_GETFAVORITERECORD:
 		case REQUESTTYPE_GETFAVORITEENTERTAINMENT:
 		case REQUESTTYPE_GETPUBLICATION:
 		case REQUESTTYPE_GETPUBLICATIONSET: {
 			if (task.Observer != null) {
 				task.Observer.updateData(task.Type, null, task.Data);
 			}
 			break;
 		}
 
 		case REQUESTTYPE_GETPUBLICATIONS_OFSET:
 		case REQUESTTYPE_GETDETAILSDATA:
 		case REQUESTTYPE_GETIMAGE: {
 			if (task.Observer != null) {
 				task.Observer.updateData(task.Type, task.PageNumber,
 						task.Index, task.Data);
 			}
 			break;
 		}
 
 		case REQUESTTYPE_GETUSERDATA:
 		case REQUESTTYPE_GETHELPINFO:
 		case REQUESTTYPE_GETPREVIEWS:
 		case REQUESTTYPE_GETGUIDELIST:
 		case REQUESTTYPE_GETDOWNLOADSTATUS: {
 			if (task.Observer != null) {
 				// task.Observer.updateData(task.Type, task.PageNumber,
 				// task.PageSize, task.Data);
 				task.Observer.updateData(task.Type, null, task.Data);
 			}
 			break;
 		}
 
 		case REQUESTTYPE_GETTOTALCOSTBYCHARGETYPE:
 		case REQUESTTYPE_GETPOWERCONSUMPTION: {
 			if (task.Observer != null) {
 				task.Observer.updateData(task.Type, 0, 0, task.Data);
 			}
 			break;
 		}
 
 		case REQUESTTYPE_GETSETTINGS: {
 			if (task.Observer != null) {
 				task.Observer.updateData(task.Type, task.Key, task.Data);
 			}
 			break;
 		}
 
 		default:
 			break;
 		}
 	}
 
 	private void enqueueTask(RequestTask task) {
 		synchronized (mTaskQueueLock) {
 			mTaskQueue.add(task);
 			if (mTaskQueue.size() == 1) {
 				mTaskQueueLock.notifyAll();
 			}
 		}
 	}
 
 	private void enqueueTaskHightPrority(RequestTask task) {
 		synchronized (mTaskQueueLock) {
 			mTaskQueue.addFirst(task);
 			if (mTaskQueue.size() == 1) {
 				mTaskQueueLock.notifyAll();
 			}
 		}
 	}
 
 	private RequestTask dequeueTask() {
 		synchronized (mTaskQueueLock) {
 
 			RequestTask task = mTaskQueue.poll();
 
 			if (task != null && task.Flag == RequestTask.INVALID) {
 				task = null;
 			}
 
 			return task;
 		}
 	}
 
 	private void enqueueFinishedTask(RequestTask task) {
 
 		if (task != null && task.Flag == RequestTask.ACTIVE) {
 			synchronized (mFinishedTaskQueueLock) {
 				mFinishedTaskQueue.add(task);
 			}
 
 			mHandler.sendEmptyMessage(GDCommon.MSG_TASK_FINISHED);
 		} else {
 			Log.d(TAG, "taskFinished : invalide task, dropped!");
 		}
 	}
 
 	private RequestTask dequeueFinishedTask() {
 
 		synchronized (mFinishedTaskQueueLock) {
 			RequestTask task = mFinishedTaskQueue.poll();
 
 			if (task != null && task.Flag == RequestTask.INVALID) {
 				task = null;
 			}
 
 			return task;
 		}
 	}
 
 	private class WorkerThread extends Thread {
 
 		private int mThreadId = -1;
 		private int mThreadPriority = -100;
 		private Object mProcessingTaskLock = new Object();
 		private Object mExitLock = new Object();
 		private RequestTask mProcessingTask;
 
 		public boolean mExit = false;
 
 		public void cancelProcessingTask(ClientObserver observer) {
 			synchronized (mProcessingTaskLock) {
 				if (mProcessingTask != null) {
 					if (mProcessingTask.Observer == observer) {
 						mProcessingTask.Flag = RequestTask.INVALID;
 					}
 				}
 			}
 		}
 
 		public void cancelAllProcessingTask() {
 			synchronized (mProcessingTaskLock) {
 				if (mProcessingTask != null) {
 					mProcessingTask.Flag = RequestTask.INVALID;
 				}
 			}
 		}
 
 		private void setProcessingTask(RequestTask task) {
 			synchronized (mProcessingTaskLock) {
 				mProcessingTask = task;
 			}
 		}
 
 		private void taskFinished(RequestTask task) {
 			Log.d(TAG, "Task [" + task.Id + "] Finished - Thread Id ["
 					+ mThreadId + "]");
 
 			enqueueFinishedTask(task);
 			setProcessingTask(null);
 		}
 
 		private boolean checkExit() {
 			synchronized (mExitLock) {
 				return mExit;
 			}
 		}
 
 		public void setExit(boolean exit) {
 			synchronized (mExitLock) {
 				mExit = exit;
 			}
 		}
 
 		public void run() {
 			mThreadId = Process.myTid();
 			mThreadPriority = Process.getThreadPriority(mThreadId);
 			Log.d(TAG, "Worker Thread [" + mThreadId + "] Priority ["
 					+ mThreadPriority + "]");
 
 			Process.setThreadPriority(Process.THREAD_PRIORITY_LESS_FAVORABLE);
 			mThreadPriority = Process.getThreadPriority(mThreadId);
 			Log.d(TAG, "Worker Thread [" + mThreadId + "] Priority ["
 					+ mThreadPriority + "]");
 
 			while (true) {
 				Log.d(TAG, "@@@ 1 Thread [" + mThreadId + "]-- Begin Run");
 
 				if (checkExit()) {
 					break;
 				}
 
 				synchronized (mTaskQueueLock) {
 					if (mTaskQueue.size() == 0) {
 						try {
 							mTaskQueueLock.wait();
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 				}
 
 				RequestTask task = dequeueTask();
 
 				if (task == null) {
 					// in this case, we call notify and try to wake up this
 					// thread to let it exit.
 					continue;
 				}
 
 				setProcessingTask(task);
 
 				switch (task.Type) {
 				case REQUESTTYPE_GETCOLUMNS: {
 					Object value = null;
 					value = task.Parameters.get(PARAMETER_COLUMN_ID);
 					String columnId = String.valueOf(value);
 
 					ColumnData[] coloumns = mDataModel.getColumns(columnId);
 
 					for (int i = 0; coloumns != null && i < coloumns.length; i++) {
 						ColumnData column = coloumns[i];
 						String iconRootPath = mConfigure.getIconRootDir();
 						coloumns[i].IconNormal = mDataModel
 								.getImage(iconRootPath + "/"
 										+ column.IconNormalPath);
 						coloumns[i].IconFocused = mDataModel
 								.getImage(iconRootPath + "/"
 										+ column.IconFocusedPath);
 						// coloumns[i].IconClicked =
 						// mDataModel.getImage(iconRootPath + "/" +
 						// column.IconClickedPath);
 					}
 
 					task.Data = coloumns;
 
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_GETPUBLICATION: {
 					Object value = null;
 					value = task.Parameters.get(PARAMETER_COLUMN_ID);
 					String columnId = String.valueOf(value);
 
 					ContentData[] datas = mDataModel.getPublications(columnId,
 							null);
 					task.Data = datas;
 
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_GETPUBLICATIONSET: {
 					Object value = null;
 					value = task.Parameters.get(PARAMETER_COLUMN_ID);
 					String columnId = String.valueOf(value);
 
 					ContentData[] contents = mDataModel.getPublicationSets(
 							columnId, null);
 
 					task.Data = contents;
 
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_GETPUBLICATIONS_OFSET: {
 					Object value = null;
 					value = task.Parameters.get(PARAMETER_SET_ID);
 					String setId = String.valueOf(value);
 
 					ContentData[] contents = mDataModel.getPublicationsEx(
 							setId, null);
 
 					if (contents != null && contents.length > 0) {
 						for (int i = 0; i < contents.length; i++) {
 //							String xmlFile = getDetailsDataFile(contents[i]);
 //
 //							mDataModel.getDetailsData(xmlFile, contents[i]);
 							
 							mDataModel.getPublicationVAInfo(contents[i]);
 						}
 					}
 
 					task.Data = contents;
 
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_GETFAVORITEMOVIE:
 				case REQUESTTYPE_GETFAVORITETV:
 				case REQUESTTYPE_GETFAVORITERECORD:
 				case REQUESTTYPE_GETFAVORITEENTERTAINMENT: {
 					break;
 				}
 
 				case REQUESTTYPE_GETDETAILSDATA: {
 					Object value = null;
 					value = task.Parameters.get(PARAMETER_CONTENTDATA);
 					ContentData content = (ContentData) value;
 //					String xmlFile = getDetailsDataFile(content);
 //
 //					mDataModel.getDetailsData(xmlFile, content);
 					
 					mDataModel.getPublicationVAInfo(content);
 					task.Data = content;
 
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_GETIMAGE: {
 					Object value = null;
 					value = task.Parameters.get(PARAMETER_CONTENTDATA);
 					ContentData content = (ContentData) value;
 					String file = getThumbnailFile(content);
 
 					Bitmap image = mDataModel.getImage(file);
 					task.Data = image;
 
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_STARTGETTASKINFO: {
 					mDBStarClient.startTaskInfo();
 					break;
 				}
 
 				case REQUESTTYPE_STOPGETTASKINFO: {
 					mDBStarClient.stopTaskInfo();
 					break;
 				}
 
 				case REQUESTTYPE_GETDOWNLOADSTATUS: {
 					ReceiveEntry[] entries = mDBStarClient.getTaskInfo();
 					task.Data = entries;
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_GETPOWERCONSUMPTION: {
 					Object value = null;
 					value = task.Parameters.get(PARAMETER_CCID);
 					String cc_id = (String) value;
 					value = task.Parameters.get(PARAMETER_DATESTART);
 					String date_start = (String) value;
 					value = task.Parameters.get(PARAMETER_DATEEND);
 					String date_end = (String) value;
 
 					String powerConsumption = mNetModel.getPowerConsumption(
 							cc_id, date_start, date_end);
 					task.Data = powerConsumption;
 
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_GETTOTALCOSTBYCHARGETYPE: {
 					Object value = null;
 					value = task.Parameters.get(PARAMETER_CCID);
 					String cc_id = (String) value;
 					value = task.Parameters.get(PARAMETER_DATESTART);
 					String date_start = (String) value;
 					value = task.Parameters.get(PARAMETER_DATEEND);
 					String date_end = (String) value;
 					value = task.Parameters.get(PARAMETER_CHAREGTYPE);
 					String charge_type = (String) value;
 
 					String totalCost = mNetModel.getTotalCostByChargeType(
 							cc_id, date_start, date_end, charge_type);
 					task.Data = totalCost;
 
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_GETSETTINGS: {
 					task.Data = mDataModel.getSettingValue((String) task.Key);
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_SETSETTINGS: {
 					Object value = null;
 					value = task.Parameters.get(PARAMETER_KEY);
 					String key = (String) value;
 					value = task.Parameters.get(PARAMETER_VALUE);
 					String sValue = (String) value;
 
 					boolean ret = mDataModel.setSettingValue(key, sValue);
 					break;
 				}
 
 				case REQUESTTYPE_GETGUIDELIST: {
 					GuideListItem[] items = mDataModel.getGuideList();
 					task.Data = items;
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_UPDATEGUIDELIST: {
 					mDataModel.updateGuideList((GuideListItem[]) task.Data);
 					// taskFinished(task);
 					
 					break;
 				}
 
 				case REQUESTTYPE_GETPREVIEWS: {
 					String path = mDataModel.getPreviewPath();
 					if (path != null && !path.isEmpty()) {
 						Log.d(TAG, "path= " + path);
 						File dir = new File(path);
 						if (dir != null && dir.exists()) {
 							File[] files = dir.listFiles();
 							if (files != null && files.length > 0) {
 								PreviewData[] data = new PreviewData[files.length];
 								for (int i = 0; i < data.length; i++) {
 									PreviewData item = new PreviewData();
 									item.URI = files[i].getAbsolutePath();
 									item.Type = PreviewData.TypeVideo;
 									data[i] = item;
 								}
 
 								task.Data = data;
 								taskFinished(task);
 							}
 
 						}
 					}
 
 					break;
 				}
 
 				case REQUESTTYPE_GETUSERDATA: {
 					UserData data = new UserData();
 					data.OperatorData = mDataModel.getOperatorInfo();
 					data.CardId = mDataModel.getCardId();
 					data.Products = mDataModel.getProducts();
 
 					task.Data = data;
 					taskFinished(task);
 					break;
 				}
 
 				case REQUESTTYPE_GETHELPINFO: {
 					String help = mDataModel.getHelpInfo();
 					task.Data = help;
 					taskFinished(task);
 					break;
 				}
 
 				default:
 					break;
 				}
 			}
 
 			Log.d(TAG, "Thread [" + mThreadId + "] exit!");
 		}
 	};
 
 	public void getColumns(ClientObserver observer, int level, int index,
 			String columnId) {
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETCOLUMNS;
 		task.Parameters = new HashMap<String, Object>();
 		task.Parameters.put(PARAMETER_COLUMN_ID, columnId);
 		task.Index = index;
 		task.ColumnLevel = level;
 		enqueueTask(task);
 	}
 
 	public void getPublications(ClientObserver observer, String columnId) {
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETPUBLICATION;
 		task.Parameters = new HashMap<String, Object>();
 		task.Parameters.put(PARAMETER_COLUMN_ID, columnId);
 
 		enqueueTask(task);
 	}
 
 	public void getPublicationSets(ClientObserver observer, String columnId) {
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETPUBLICATIONSET;
 		task.Parameters = new HashMap<String, Object>();
 		task.Parameters.put(PARAMETER_COLUMN_ID, columnId);
 
 		enqueueTask(task);
 	}
 
 	public void getPublicationsOfSet(ClientObserver observer, String setId,
 			int pageNumber, int index) {
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETPUBLICATIONS_OFSET;
 		task.Parameters = new HashMap<String, Object>();
 		task.Parameters.put(PARAMETER_SET_ID, setId);
 		task.PageNumber = pageNumber;
 		task.Index = index;
 
 		enqueueTask(task);
 	}
 
 	public void getDetailsData(ClientObserver observer, int pageNumber,
 			int index, ContentData content) {
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETDETAILSDATA;
 		task.PageNumber = pageNumber;
 		task.Index = index;
 		task.Parameters = new HashMap<String, Object>();
 		task.Parameters.put(PARAMETER_CONTENTDATA, content);
 		enqueueTask(task);
 	}
 
 	public void getImage(ClientObserver observer, int pageNumber, int index,
 			ContentData content) {
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETIMAGE;
 		task.PageNumber = pageNumber;
 		task.Index = index;
 		task.Parameters = new HashMap<String, Object>();
 		task.Parameters.put(PARAMETER_CONTENTDATA, content);
 		// enqueueTask(task);
 		enqueueTaskHightPrority(task);
 	}
 
 	public void getPowerConsumption(ClientObserver observer, String cc_id,
 			String date_start, String date_end) {
 		if (!mIsNetworkReady)
 			return;
 
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETPOWERCONSUMPTION;
 
 		task.Parameters = new HashMap<String, Object>();
 		task.Parameters.put(PARAMETER_CCID, cc_id);
 		task.Parameters.put(PARAMETER_DATESTART, date_start);
 		task.Parameters.put(PARAMETER_DATEEND, date_end);
 
 		enqueueTask(task);
 	}
 
 	public void getTotalCostByChargeType(ClientObserver observer, String cc_id,
 			String date_start, String date_end, String charge_type) {
 		if (!mIsNetworkReady)
 			return;
 
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETTOTALCOSTBYCHARGETYPE;
 
 		task.Parameters = new HashMap<String, Object>();
 		task.Parameters.put(PARAMETER_CCID, cc_id);
 		task.Parameters.put(PARAMETER_DATESTART, date_start);
 		task.Parameters.put(PARAMETER_DATEEND, date_end);
 		task.Parameters.put(PARAMETER_CHAREGTYPE, charge_type);
 
 		enqueueTask(task);
 	}
 
 	public void getSettingsValue(ClientObserver observer, String key) {
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETSETTINGS;
 		task.Key = key;
 
 		enqueueTask(task);
 	}
 
 	public void setSettingsValue(String key, String value) {
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Type = REQUESTTYPE_SETSETTINGS;
 
 		task.Parameters = new HashMap<String, Object>();
 		task.Parameters.put(PARAMETER_KEY, key);
 		task.Parameters.put(PARAMETER_VALUE, value);
 
 		enqueueTask(task);
 	}
 
 	public void startGetTaskInfo() {
 
 		RequestTask task = new RequestTask();
 		task.Observer = null;
 		// task.Id = System.currentTimeMillis();
 		task.Type = REQUESTTYPE_STARTGETTASKINFO;
 
 		enqueueTask(task);
 	}
 
 	public void getDownloadStatus(ClientObserver observer) {
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETDOWNLOADSTATUS;
 
 		// task.PageNumber = pageNumber;
 		// task.PageSize = pageSize;
 		enqueueTask(task);
 	}
 
 	public void stopGetTaskInfo() {
 		RequestTask task = new RequestTask();
 		task.Observer = null;
 		// task.Id = System.currentTimeMillis();
 		task.Type = REQUESTTYPE_STOPGETTASKINFO;
 
 		enqueueTask(task);
 	}
 
 	// favorite
 	public void getFavoriteMovie(ClientObserver observer) {
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETFAVORITEMOVIE;
 
 		enqueueTask(task);
 	}
 
 	public void getFavoriteTV(ClientObserver observer) {
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETFAVORITETV;
 
 		enqueueTask(task);
 	}
 
 	public void getAllGuideList(ClientObserver observer) {
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETGUIDELIST;
 
 		enqueueTask(task);
 	}
 
 	public void updateGuideList(ClientObserver observer, GuideListItem[] items) {
 
 		RequestTask task = new RequestTask();
 		// task.Id = System.currentTimeMillis();
 		task.Observer = observer;
 		task.Data = items;
 		task.Type = REQUESTTYPE_UPDATEGUIDELIST;
 
 		enqueueTask(task);
 	}
 
 	public void getPreviews(ClientObserver observer) {
 		RequestTask task = new RequestTask();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETPREVIEWS;
 
 		enqueueTask(task);
 	}
 
 	public void getUserData(ClientObserver observer) {
 		RequestTask task = new RequestTask();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETUSERDATA;
 
 		enqueueTask(task);
 	}
 
 	public void getHelpInfo(ClientObserver observer) {
 		RequestTask task = new RequestTask();
 		task.Observer = observer;
 		task.Type = REQUESTTYPE_GETHELPINFO;
 
 		enqueueTask(task);
 	}
 
 	private String getThumbnailFile(ContentData content) {
 		return mConfigure.getThumbnailFile(content);
 	}
 
 	private String getDetailsDataFile(ContentData content) {
 		return mConfigure.getDetailsDataFile(content);
 	}
 
 	public String getMediaFile(ContentData content) {
 		return mConfigure.getMediaFile(content);
 	}
 
 	public String getDRMFile(ContentData content) {
 		return mConfigure.getDRMFile(content);
 	}
 
 	public String getEBookFile(String category) {
 		return mConfigure.getEbookFile(category);
 	}
 
 	public String getStorageDisk() {
 		return mConfigure.getStorageDisk();
 	}
 
 	// cancel the requests from this observer
 	public void cancelRequests(ClientObserver observer) {
 		synchronized (mTaskQueueLock) {
 			for (int i = 0; i < mTaskQueue.size(); i++) {
 				RequestTask task = mTaskQueue.get(i);
 				if (task.Observer == observer) {
 					task.Flag = RequestTask.INVALID;
 				}
 			}
 		}
 
 		for (int i = 0; i < mThreadPool.size(); i++) {
 			WorkerThread thread = mThreadPool.get(i);
 			thread.cancelProcessingTask(observer);
 		}
 
 		synchronized (mFinishedTaskQueueLock) {
 			for (int i = 0; i < mFinishedTaskQueue.size(); i++) {
 				RequestTask task = mFinishedTaskQueue.get(i);
 				if (task.Observer == observer) {
 					task.Flag = RequestTask.INVALID;
 				}
 			}
 		}
 	}
 
 	public String getCategoryContent(String category) {
 		return mConfigure.getCategoryContent(category);
 	}
 
 	public void getPushedMessage(List<String> retMessages) {
 		mConfigure.getPushedMessage(retMessages);
 	}
 
 	public String getMacAddress() {
 
 		if (mMacAddress.equals("")) {
 			mMacAddress = GDNetworkUtil.getMacAddress(this, mConnectManager);
 		}
 
 		return mMacAddress;
 	}
 
 	public void cancelAllRequests() {
 		Log.d(TAG, "cancelAllRequests!");
 
 		synchronized (mTaskQueueLock) {
 			mTaskQueue.clear();
 		}
 
 		for (int i = 0; i < mThreadPool.size(); i++) {
 			WorkerThread thread = mThreadPool.get(i);
 			thread.cancelAllProcessingTask();
 		}
 
 		synchronized (mFinishedTaskQueueLock) {
 			mFinishedTaskQueue.clear();
 		}
 	}
 
 	public boolean isNetworkConnected() {
 		NetworkInfo networkInfo = mConnectManager.getActiveNetworkInfo();
 
 		return networkInfo != null && networkInfo.isConnected();
 	}
 
 	private void registerUSBReceiver() {
 		IntentFilter usbFilter = new IntentFilter();
 		/* receive USB status change messages */
 		usbFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
 		usbFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
 		// usbFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
 		usbFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
 		usbFilter.addDataScheme("file");
 
 		registerReceiver(mUSBReceiver, usbFilter);
 	}
 
 	private void reqisterConnectReceiver() {
 		mConnectManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		IntentFilter networkFilter = new IntentFilter();
 		/* receive connection change messages */
 		networkFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
 		registerReceiver(mNetworkReceiver, networkFilter);
 	}
 
 	private void reqisterSystemMessageReceiver() {
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(DbstarServiceApi.ACTION_NOTIFY);
		filter.addAction(GDCommon.ActionAddFavourite);
		filter.addAction(GDCommon.ActionDelete);
		filter.addAction(GDCommon.ActionUpgradeCancelled);
 		registerReceiver(mSystemMessageReceiver, filter);
 	}
 
 	private BroadcastReceiver mUSBReceiver = new BroadcastReceiver() {
 
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 			Uri uri = intent.getData();
 			Log.d(TAG, "---- USB device " + action);
 			Log.d(TAG, "---- URI:" + uri.toString());
 
 			if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
 				Message message = mHandler
 						.obtainMessage(GDCommon.MSG_MEDIA_MOUNTED);
 				Bundle data = new Bundle();
 				data.putString("disk", uri.getPath());
 				message.setData(data);
 
 				mHandler.sendMessage(message);
 			} else if (action.equals(Intent.ACTION_MEDIA_REMOVED)
 					|| action.equals(Intent.ACTION_MEDIA_BAD_REMOVAL)
 			/* ||action.equals(Intent.ACTION_MEDIA_UNMOUNTED) */) {
 
 				Message message = mHandler
 						.obtainMessage(GDCommon.MSG_MEDIA_REMOVED);
 				Bundle data = new Bundle();
 				data.putString("disk", uri.getPath());
 				message.setData(data);
 
 				mHandler.sendMessage(message);
 			}
 		}
 
 	};
 
 	private BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {
 
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 
 			if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
 				return;
 
 			boolean noConnectivity = intent.getBooleanExtra(
 					ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
 
 			Log.d(TAG, "noConnectivity = " + noConnectivity);
 			if (noConnectivity) {
 				// There are no connected networks at all
 				mHandler.sendEmptyMessage(GDCommon.MSG_NETWORK_DISCONNECT);
 				return;
 			}
 
 			// case 1: attempting to connect to another network, just wait for
 			// another broadcast
 			// case 2: connected
 			// NetworkInfo networkInfo = (NetworkInfo) intent
 			// .getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
 
 			NetworkInfo networkInfo = mConnectManager.getActiveNetworkInfo();
 
 			if (networkInfo != null) {
 				Log.d(TAG, "getTypeName() = " + networkInfo.getTypeName());
 				Log.d(TAG, "isConnected() = " + networkInfo.isConnected());
 
 				if (networkInfo.isConnected()) {
 					mHandler.sendEmptyMessage(GDCommon.MSG_NETWORK_CONNECT);
 				}
 			}
 		}
 
 	};
 
 	private BroadcastReceiver mSystemMessageReceiver = new BroadcastReceiver() {
 
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 
 			Log.d(TAG, "onReceive System msg " + action);
 
 			if (action.equals(DbstarServiceApi.ACTION_NOTIFY)) {
 
 				int type = intent.getIntExtra("type", 0);
 				Log.d(TAG, "onReceive type " + type);
 
 				switch (type) {
 				case DbstarServiceApi.UPGRADE_NEW_VER_FORCE:
 				case DbstarServiceApi.UPGRADE_NEW_VER: {
 					byte[] bytes = intent.getByteArrayExtra("message");
 					if (bytes != null) {
 						String packageFile = "";
 
 						try {
 							packageFile = new String(bytes, "utf-8");
 						} catch (UnsupportedEncodingException e) {
 							e.printStackTrace();
 						}
 
 						Log.d(TAG, "onReceive packageFile " + packageFile);
 
 						Bundle data = new Bundle();
 						data.putString(GDCommon.KeyPackgeFile, packageFile);
 
 						int msgId = 0;
 						if (type == DbstarServiceApi.UPGRADE_NEW_VER) {
 							msgId = GDCommon.MSG_SYSTEM_UPGRADE;
 						} else if (type == DbstarServiceApi.UPGRADE_NEW_VER_FORCE) {
 							msgId = GDCommon.MSG_SYSTEM_FORCE_UPGRADE;
 						} else {
 							;
 						}
 
 						Message msg = mHandler.obtainMessage(msgId);
 						msg.setData(data);
 						mHandler.sendMessage(msg);
 
 					}
 					break;
 
 				}
 
 				case DbstarServiceApi.STATUS_DVBPUSH_INIT_SUCCESS: {
 					mIsDbServiceStarted = true;
 					notifyDbstarServiceNetworkStatus();
 					notifyDbstarServiceStorageStatus();
 					break;
 				}
 
 				case DbstarServiceApi.STATUS_DVBPUSH_INIT_FAILED: {
 					mIsDbServiceStarted = false;
 					break;
 				}
 
 				default:
 					break;
 				}
 
 			} else if (action
 					.equals(GDCommon.ActionAddFavourite)) {
 				String publicationSetId = intent
 						.getStringExtra("publicationset_id");
 				String publicationId = intent.getStringExtra("publication_id");
 
 				Message msg = mHandler
 						.obtainMessage(GDCommon.MSG_ADD_TO_FAVOURITE);
 				Bundle data = new Bundle();
 				data.putString(GDCommon.KeyPublicationID, publicationId);
 				data.putString(GDCommon.KeyPublicationSetID, publicationSetId);
 				msg.setData(data);
 				mHandler.sendMessage(msg);
 
 			} else if (action.equals(GDCommon.ActionDelete)) {
 				String publicationSetId = intent
 						.getStringExtra("publicationset_id");
 				String publicationId = intent.getStringExtra("publication_id");
 				Message msg = mHandler.obtainMessage(GDCommon.MSG_DELETE);
 				Bundle data = new Bundle();
 				data.putString(GDCommon.KeyPublicationID, publicationId);
 				data.putString(GDCommon.KeyPublicationSetID, publicationSetId);
 				msg.setData(data);
 				mHandler.sendMessage(msg);
 			} else if (action.equals(GDCommon.ActionUpgradeCancelled)) {
 				mHandler.sendEmptyMessage(GDCommon.MSG_USER_UPGRADE_CANCELLED);
 			}
 
 		}
 
 	};
 
 	void notifyDbstarServiceNetworkStatus() {
 		if (!mIsDbServiceStarted)
 			return;
 
 		if (isNetworkConnected()) {
 			mDBStarClient.notifyDbServer(DbstarServiceApi.CMD_NETWORK_CONNECT);
 		} else {
 			mDBStarClient
 					.notifyDbServer(DbstarServiceApi.CMD_NETWORK_DISCONNECT);
 		}
 	}
 
 	void notifyDbstarServiceStorageStatus() {
 		if (!mIsDbServiceStarted)
 			return;
 
 		if (mIsStorageReady) {
 			mDBStarClient.notifyDbServer(DbstarServiceApi.CMD_DISK_MOUNT);
 		} else {
 			mDBStarClient.notifyDbServer(DbstarServiceApi.CMD_DISK_UNMOUNT);
 		}
 	}
 
 }
