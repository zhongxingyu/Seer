 package com.htb.cnk;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.SimpleAdapter;
import android.widget.Toast;
 
 import com.htb.cnk.NotificationTableService.MyBinder;
 import com.htb.cnk.TableClickActivity.tableItemClickListener;
 import com.htb.cnk.data.Info;
 import com.htb.cnk.data.NotificationTypes;
 import com.htb.cnk.data.Notifications;
 import com.htb.cnk.data.PhoneOrder;
 import com.htb.cnk.data.TableSetting;
 import com.htb.cnk.lib.BaseActivity;
 import com.htb.cnk.lib.Ringtone;
 import com.htb.constant.Table;
 
 public class TableBaseActivity extends BaseActivity {
 
 	protected final String IMAGE_ITEM = "imageItem";
 	protected final String ITEM_TEXT = "ItemText";
 	protected int NETWORK_ARERTDIALOG = 0;
 	protected TableSetting mSettings;
 	protected GridView gridview;
 	protected ProgressDialog mpDialog;
 	protected SimpleAdapter mImageItems;
 	protected ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
 	protected Notifications mNotificaion = new Notifications();
 	protected NotificationTypes mNotificationType = new NotificationTypes();
 	protected PhoneOrder mPhoneOrder;
 	protected AlertDialog.Builder mNetWrorkAlertDialog;
 	protected AlertDialog mNetWrorkcancel;
 	protected Ringtone mRingtone;
 	protected int mTableMsg;
 	protected int mRingtoneMsg;
 	protected MyReceiver mReceiver;
 	protected NotificationTableService.MyBinder binder;
 	protected boolean binderFlag;
 	protected Intent intent;
 	public double mTotalPrice;
 	protected AlertDialog.Builder mChangeDialog;
 	protected List<String> tableName = new ArrayList<String>();
 	protected tableItemClickListener mTableClicked;
 	protected Handler mNotificationHandler;
 	protected Handler mTableHandler;
 	protected Handler mRingtoneHandler;
 	protected Handler mTotalPriceTableHandler;
 	protected Handler mChangeTIdHandler;
 	protected Handler mCombineTIdHandler;
 	protected Handler mCopyTIdHandler;
 	protected Handler mCheckOutHandler;
 	protected Handler mNotificationTypeHandler;
 	protected Button mBackBtn;
 	protected Button mUpdateBtn;
 	protected Button mStatisticsBtn;
 	protected Button mManageBtn;
 
 	@Override
 	protected void onDestroy() {
 		unbindService(conn);
 		unregisterReceiver(mReceiver);
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if (NETWORK_ARERTDIALOG == 1) {
 			mNetWrorkcancel.cancel();
 			NETWORK_ARERTDIALOG = 0;
 		}
 		showProgressDlg(getResources().getString(R.string.getStatus));
 		binderStart();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.table_activity);
 
 		mPhoneOrder = new PhoneOrder(TableBaseActivity.this);
 		mSettings = new TableSetting();
 		mRingtone = new Ringtone(TableBaseActivity.this);
 		mpDialog = new ProgressDialog(TableBaseActivity.this);
 		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		mpDialog.setIndeterminate(false);
 		mpDialog.setCancelable(false);
 		mpDialog.setTitle(getResources().getString(R.string.pleaseWait));
 		findViews();
 		Info.setMode(Info.WORK_MODE_WAITER);
 
 		intent = new Intent(TableBaseActivity.this,
 				NotificationTableService.class);
 		startService(intent);
 
 		bindService(intent, conn, Context.BIND_AUTO_CREATE);
 		mReceiver = new MyReceiver();
 		IntentFilter filter = new IntentFilter(
 				NotificationTableService.SERVICE_IDENTIFIER);
 		registerReceiver(mReceiver, filter);
 
 		NotificationType();
 
 	}
 
 	private void findViews() {
 		mBackBtn = (Button) findViewById(R.id.back);
 		mUpdateBtn = (Button) findViewById(R.id.checkOutTable);
 		mStatisticsBtn = (Button) findViewById(R.id.logout);
 		mManageBtn = (Button) findViewById(R.id.management);
 		gridview = (GridView) findViewById(R.id.gridview);
 	}
 
 	public void showProgressDlg(String msg) {
 		mpDialog.setMessage(msg);
 		mpDialog.show();
 	}
 
 	protected ServiceConnection conn = new ServiceConnection() {
 
 		@Override
 		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
 			binder = (MyBinder) arg1;
 			binderFlag = true;
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 		}
 	};
 
 	protected void setTableInfos() {
 		if (lstImageItem.size() > 0) {
 			setStatusAndIcon();
 		} else {
 			setStatusAndIcon();
 			mImageItems = new SimpleAdapter(TableBaseActivity.this,
 					lstImageItem, R.layout.table_item, new String[] {
 							IMAGE_ITEM, ITEM_TEXT }, new int[] {
 							R.id.ItemImage, R.id.ItemText }) {
 			};
 			gridview.setAdapter(mImageItems);
 		}
 		gridview.setVisibility(View.VISIBLE);
 		mImageItems.notifyDataSetChanged();
 		gridview.setOnItemClickListener(mTableClicked);
 	}
 
 	private void setStatusAndIcon() {
 		lstImageItem.clear();
 		for (int i = 0, n = 0; i < mSettings.size(); i++) {
 			int status = mSettings.getStatusIndex(i);
 			if (status < Table.NOTIFICATION_STATUS
 					&& mNotificaion.getId(n) == mSettings.getIdIndex(i)) {
 				status = status + Table.NOTIFICATION_STATUS;
 				n++;
 			}
 			setTableIcon(i, status);
 		}
 	}
 
 	private void setTableIcon(int position, int status) {
 		HashMap<String, Object> map;
 		if (lstImageItem.size() <= position) {
 			map = new HashMap<String, Object>();
 			map.put(ITEM_TEXT, mSettings.getNameIndex(position));
 		} else {
 			map = lstImageItem.get(position);
 		}
 
 		imageItemSwitch(position, status, map);
 		if (lstImageItem.size() <= position) {
 			lstImageItem.add(map);
 		}
 	}
 
 	private void imageItemSwitch(int position, int status,
 			HashMap<String, Object> map) {
 		switch (status) {
 		case 0:
 			map.put(IMAGE_ITEM, R.drawable.table_red);
 			break;
 		case 1:
 			map.put(IMAGE_ITEM, R.drawable.table_blue);
 			break;
 		case 50:
 		case 51:
 			map.put(IMAGE_ITEM, R.drawable.table_yellow);
 			break;
 		case 100:
 			map.put(IMAGE_ITEM, R.drawable.table_rednotification);
 			mSettings.setStatus(position, status);
 			break;
 		case 101:
 			map.put(IMAGE_ITEM, R.drawable.table_bluenotification);
 			mSettings.setStatus(position, status);
 			break;
 		case 150:
 		case 151:
 			map.put(IMAGE_ITEM, R.drawable.table_yellownotification);
 			mSettings.setStatus(position, status);
 			break;
 		default:
 			map.put(IMAGE_ITEM, R.drawable.table_red);
 			break;
 		}
 	}
 
 	protected void NotificationType() {
 		new Thread() {
 			public void run() {
 				try {
 					int ret = mNotificationType.getNotifiycationsType();
 					mNotificationTypeHandler.sendEmptyMessage(ret);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	protected void binderStart() {
 		if (binderFlag) {
 			binder.start();
 			return;
 		}
 	}
 
 	protected void netWorkDialogShow(String messages) {
 		NETWORK_ARERTDIALOG = 1;
 		mNetWrorkcancel = mNetWrorkAlertDialog.setMessage(messages).show();
 	}
 
 	public class MyReceiver extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Bundle bundle = intent.getExtras();
 			mRingtoneMsg = bundle.getInt("ringtoneMessage");
 			mTableMsg = bundle.getInt("tableMessage");
 			mSettings = (TableSetting) bundle
 					.getSerializable(NotificationTableService.SER_KEY);
 			mTableHandler.sendEmptyMessage(mTableMsg);
 			mRingtoneHandler.sendEmptyMessage(mRingtoneMsg);
 		}
 	}
 }
