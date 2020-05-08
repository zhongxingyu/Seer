 package com.dbstar.service.client;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 
 import com.dbstar.DbstarDVB.DbstarServiceApi;
 import com.dbstar.DbstarDVB.IDbstarService;
 import com.dbstar.model.EMailItem;
 import com.dbstar.model.ProductItem;
 import com.dbstar.model.ReceiveEntry;
 
 public class GDDBStarClient {
 	private static final String TAG = "GDDBStarClient";
 
 	Context mContext;
 	private Intent mIntent = new Intent();
 	boolean mIsServerCorrupted = false;
 	DbServiceObserver mObserver;
 
 	private IDbstarService mDbstarService = null;
 	private ComponentName mComponentName = new ComponentName(
 			"com.dbstar.DbstarDVB", "com.dbstar.DbstarDVB.DbstarService");
 
 	private boolean mIsBoundToServer = false;
 
 	public boolean isBoundToServer() {
 		return mIsBoundToServer;
 	}
 
 	private ServiceConnection mConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 
 			Log.d(TAG, "+++++++++++GDDBStarClient onServiceConnected+++++++++");
 
 			mDbstarService = IDbstarService.Stub.asInterface(service);
 
 			mIsBoundToServer = true;
 
 			startDvbpush();
 		}
 
 		// this is called when server is stopped abnormally.
 		public void onServiceDisconnected(ComponentName className) {
 			Log.d(TAG, "+++++++++GDDBStarClient onServiceDisconnected+++++++");
 
 			mDbstarService = null;
 //			mIsServerCorrupted = true;
 			mIsBoundToServer = false;
 		}
 	};
 
 	public GDDBStarClient(Context context) {
 		mContext = context;
 	}
 
 	public void start() {
 		mIntent.setComponent(mComponentName);
 		mContext.startService(mIntent);
 		mContext.bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE);
 	}
 
 	public void stop() {
 		mContext.stopService(mIntent);
 		mContext.unbindService(mConnection);
 	}
 
 	public void setObserver(DbServiceObserver oberver) {
 		mObserver = oberver;
 	}
 
 	public void startDvbpush() {
 		if (mDbstarService != null) {
 			try {
 				mDbstarService.initDvbpush();
 				Log.d(TAG, "+++++++++++startDvbpush+++++++++++");
 
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void stopDvbpush() {
 		if (mDbstarService != null) {
 			try {
 				mDbstarService.uninitDvbpush();
 				Log.d(TAG, "+++++++++++ stopDvbpush +++++++++++");
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public boolean startTaskInfo() {
 		boolean result = false;
 		if (mDbstarService != null) {
 			try {
 				mDbstarService.sendCommand(
 						DbstarServiceApi.CMD_DVBPUSH_GETINFO_START, null, 0);
 				result = true;
 				Log.d(TAG, "+++++++++++ startTaskInfo +++++++++++");
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return result;
 	}
 
 	public void notifyDbServer(int msg) {
 		if (mDbstarService != null) {
 			try {
 				Log.d(TAG, " ====== notifyDbServer === msg =  " + msg);
 				mDbstarService.sendCommand(msg, null, 0);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public boolean stopTaskInfo() {
 		boolean result = false;
 		if (mDbstarService != null) {
 			try {
 				mDbstarService.sendCommand(
 						DbstarServiceApi.CMD_DVBPUSH_GETINFO_STOP, null, 0);
 				result = true;
 				Log.d(TAG, "+++++++++++ stopTaskInfo +++++++++++");
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return result;
 	}
 
 	public String getTSSignalStatus() {
 		String status = null;
 
 		try {
 			Intent intent = mDbstarService.sendCommand(
 					DbstarServiceApi.CMD_DVBPUSH_GETTS_STATUS, null, 0);
 
 			byte[] bytes = intent.getByteArrayExtra("result");
 
 			if (bytes != null) {
 				try {
 					status = new String(bytes, "utf-8");
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
 				}
 			}
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 
 		return status;
 	}
 
 	public Object getSmartcardInfo(int type) {
 		Object data = null;
 
 		try {
 			Intent intent = mDbstarService.sendCommand(type, null, 0);
 
 			byte[] bytes = intent.getByteArrayExtra("result");
 
 			Log.d(TAG, "========= get type " + type + " bytes " + bytes);
 			
 			if (bytes != null) {
 				data = parseSmartcardInfo(type, bytes);
 			}
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 
 		return data;
 	}
 
 	public String getEMailContent(String mailId) {
 		String content = null;
 
 		try {
 			Intent intent = mDbstarService.sendCommand(
 					DbstarServiceApi.CMD_DRM_EMAILCONTENT_READ, mailId,
 					mailId.length());
 
 			byte[] bytes = intent.getByteArrayExtra("result");
 
 			if (bytes != null) {
 				try {
					content = new String(bytes, "gb2312");
					Log.d(TAG, " =========== email content == " + content);
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
 				}
 			}
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 
 		return content;
 	}
 
 	private Object parseSmartcardInfo(int type, byte[] bytes) {
 		Object info = null;
 		String data = null;
 		String charset = null;
 		
		if (type == DbstarServiceApi.CMD_DRM_EMAILHEADS_READ) {
 			charset = "gb2312";
 		} else {
 			charset = "utf-8";
 		}
 		
 		try {
 			data = new String(bytes, charset);
 			Log.d(TAG, " =========== smartcard info == " + type + " " + data);
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 
 		if (type == DbstarServiceApi.CMD_DRM_SC_EIGENVALUE_READ) {
 			String[] ids = data.split("\n");
 			info = ids;
 		} else if (type == DbstarServiceApi.CMD_DRM_ENTITLEINFO_READ) {
 			String[] items = data.split("\n");
 			if (items.length == 0) {
 				Log.d(TAG, " no product info !");
 			} else {
 				ArrayList<ProductItem> products = new ArrayList<ProductItem>();
 				for (int i = 0; i < items.length; i++) {
 					String[] item = items[i].split("\t");
 
 					if (item.length == 0) {
 						continue;
 					}
 
 					ProductItem product = new ProductItem();
 					product.OperatorID = item[0];
 					product.ProductID = item[1];
 					product.StartTime = item[2];
 					product.EndTime = item[3];
 					product.LimitCount = item[4];
 
 					products.add(product);
 				}
 
 				if (products.size() > 0) {
 					info = products.toArray(new ProductItem[products.size()]);
 				}
 			}
 		} else if (type == DbstarServiceApi.CMD_DRM_EMAILHEADS_READ) {
 			String[] items = data.split("\n");
 			if (items.length == 0) {
 				Log.d(TAG, " no mails !");
 			} else {
 				ArrayList<EMailItem> mails = new ArrayList<EMailItem>();
 				for (int i = 0; i < items.length; i++) {
 					String[] item = items[i].split("\t");
 
 					if (item.length == 0) {
 						continue;
 					}
 
 					EMailItem mail = new EMailItem();
 					mail.ID = item[0];
 					mail.Date = item[1];
 					mail.Flag = Integer.valueOf(item[2]);
 					mail.Title = item[3];
 					mails.add(mail);
 				}
 
 				if (mails.size() > 0) {
 					info = mails.toArray(new EMailItem[mails.size()]);
 				}
 			}
 		} else {
 			info = data;
 		}
 
 		return info;
 	}
 
 	public String manageCA(int cmd) {
 		String ret = null;
 
 		try {
 			Intent intent = mDbstarService.sendCommand(cmd, null, 0);
 
 			byte[] bytes = intent.getByteArrayExtra("result");
 
 			if (bytes != null) {
 				try {
 					ret = new String(bytes, "utf-8");
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
 				}
 			}
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 
 		return ret;
 	}
 
 	// data format: "1001|task1|23932|23523094823\n1002|task2|234239|12349320\n"
 
 	public ReceiveEntry[] getTaskInfo() {
 		ReceiveEntry[] entries = null;
 
 		Log.d(TAG, "+++++++++++ getTaskInfo +++++++++++");
 
 		if (mDbstarService == null)
 			return entries;
 
 		try {
 			Intent intent = mDbstarService.sendCommand(
 					DbstarServiceApi.CMD_DVBPUSH_GETINFO, null, 0);
 
 			byte[] bytes = intent.getByteArrayExtra("result");
 
 			if (bytes != null) {
 				String info = null;
 				try {
 					info = new String(bytes, "utf-8");
 					// Log.d(TAG, "TaskInfo: " + info);
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
 				}
 
 				String[] items = null;
 				if (info != null) {
 					items = info.split("\n");
 				}
 
 				if (items != null) {
 					entries = new ReceiveEntry[items.length];
 
 					for (int i = 0; i < items.length; i++) {
 						entries[i] = createEntry(items[i]);
 					}
 
 				}
 
 			}
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 
 		return entries;
 	}
 
 	ReceiveEntry createEntry(String data) {
 		ReceiveEntry entry = null;
 
 		if (data == null || data.isEmpty())
 			return entry;
 
 		String[] items = data.split("\t");
 
 		// for (int i = 0; i < items.length; i++) {
 		// Log.d(TAG, "item " + i + " = " + items[i]);
 		// }
 
 		entry = new ReceiveEntry();
 		entry.Id = items[0];
 		entry.Name = items[1];
 		entry.RawProgress = Long.valueOf(items[2]);
 		entry.RawTotal = Long.valueOf(items[3]);
 		// Log.d(TAG, "progress = " + entry.RawProgress + " total = " +
 		// entry.RawTotal);
 		entry.ConvertSize();
 
 		return entry;
 	}
 
 }
