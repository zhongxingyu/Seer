 package com.htb.cnk;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.text.InputFilter;
 import android.text.method.DigitsKeyListener;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.SimpleAdapter;
 import android.widget.Toast;
 
 import com.htb.cnk.NotificationTableService.MyBinder;
 import com.htb.cnk.data.Info;
 import com.htb.cnk.data.MyOrder;
 import com.htb.cnk.data.NotificationTypes;
 import com.htb.cnk.data.Notifications;
 import com.htb.cnk.data.TableSetting;
 import com.htb.cnk.lib.BaseActivity;
 import com.htb.cnk.lib.Ringtone;
 
 public class TableActivity extends BaseActivity {
 	private final int UPDATE_TABLE_INFOS = 5;
 	private final int DISABLE_GRIDVIEW = 10;
 	private final int PHONE_STATUS = 50;
 	private final int NOTIFICATION_STATUS = 100;
 	private static int ARERTDIALOG = 0;
 	private TableSetting mSettings;
 	private Button mBackBtn;
 	private Button mUpdateBtn;
 	private Button mStatisticsBtn;
 	private Button mManageBtn;
 	private GridView gridview;
 	private ProgressDialog mpDialog;
 	private SimpleAdapter mImageItems;
 	private ItemClickListener mTableClicked;
 	private ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
 	private Notifications mNotificaion = new Notifications();
 	private NotificationTypes mNotificationType = new NotificationTypes();
 	private MyOrder mMyOrder;
 	private AlertDialog.Builder mNetWrorkAlertDialog;
 	private AlertDialog mNetWrorkcancel;
 	private Ringtone mRingtone;
 	private int mTableMsg;
 	private int mRingtoneMsg;
 	private MyReceiver mReceiver;
 	private NotificationTableService.MyBinder binder;
 	private boolean binderFlag = false;
 	private Intent intent;
 
 	@Override
 	protected void onDestroy() {
		unbindService(conn);
 		unregisterReceiver(mReceiver);
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if (ARERTDIALOG == 1) {
 			mNetWrorkcancel.cancel();
 			ARERTDIALOG = 0;
 		}
 		showProgressDlg("正在获取状态，请稍等。。。");
 		binderStart();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.table_activity);
 		mMyOrder = new MyOrder(TableActivity.this);
 		mRingtone = new Ringtone(TableActivity.this);
 		findViews();
 		setClickListeners();
 		mpDialog = new ProgressDialog(TableActivity.this);
 		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		mpDialog.setTitle("请稍等");
 		mpDialog.setIndeterminate(false);
 		mpDialog.setCancelable(false);
 		mNetWrorkAlertDialog = networkDialog();
 		Info.setMode(Info.WORK_MODE_WAITER);
 		new Thread(new getNotificationType()).start();
 
 		intent = new Intent(TableActivity.this, NotificationTableService.class);
 		startService(intent);
 		bindService(intent, conn, Context.BIND_AUTO_CREATE);
 		mReceiver = new MyReceiver();
 		IntentFilter filter = new IntentFilter(
 				NotificationTableService.SERVICE_IDENTIFIER);
 		registerReceiver(mReceiver, filter);
 	}
 
 	public void showProgressDlg(String msg) {
 		mpDialog.setMessage(msg);
 		mpDialog.show();
 	}
 
 	private void findViews() {
 		mBackBtn = (Button) findViewById(R.id.back);
 		mUpdateBtn = (Button) findViewById(R.id.combineTable);
 		mStatisticsBtn = (Button) findViewById(R.id.logout);
 		mManageBtn = (Button) findViewById(R.id.management);
 		gridview = (GridView) findViewById(R.id.gridview);
 	}
 
 	private void setClickListeners() {
 		mTableClicked = new ItemClickListener();
 		mBackBtn.setOnClickListener(backClicked);
 		mUpdateBtn.setOnClickListener(updateClicked);
 		mStatisticsBtn.setOnClickListener(logoutClicked);
 		mManageBtn.setOnClickListener(manageClicked);
 	}
 
 	private ServiceConnection conn = new ServiceConnection() {
 
 		@Override
 		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
 			binder = (MyBinder) arg1;
 			binderFlag = true;
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 
 		}
 	};
 
 	class cleanNotification implements Runnable {
 		public void run() {
 			try {
 				int ret = mNotificaion.cleanNotifications(Info.getTableId());
 				notificationHandle.sendEmptyMessage(ret);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	class getNotificationType implements Runnable {
 		public void run() {
 			try {
 				int ret = mNotificationType.getNotifiycationsType();
 				notificationHandle.sendEmptyMessage(ret);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	//TODO define 
 	class ItemClickListener implements OnItemClickListener {
 
 		public void onItemClick(AdapterView<?> arg0,// The AdapterView where the
 													// click happened
 				View arg1,// The view within the AdapterView that was clicked
 				int arg2,// The position of the view in the adapter
 				long arg3// The row id of the item that was clicked
 		) {
 
 			Info.setTableName(mSettings.getName(arg2));
 			Info.setTableId(mSettings.getId(arg2));
 			int status = mSettings.getStatus(arg2);
 			switch (status) {
 			case 1:
 				Dialog cleanDialog = cleanDialog();
 				cleanDialog.show();
 				break;
 			case 50:
 			case 51:
 				AlertDialog.Builder addPhoneDilog = addPhoneDialog(arg2);
 				addPhoneDilog.show();
 				break;
 			case 100:
 			case 101:
 			case 150:
 			case 151:
 				AlertDialog.Builder notificationDialog = notificationDialog();
 				notificationDialog.show();
 				break;
 			default:
 				AlertDialog.Builder addDialog = addDialog();
 				addDialog.show();
 				break;
 			}
 			mImageItems.notifyDataSetChanged();
 
 		}
 	}
 
 	private AlertDialog.Builder networkDialog() {
 		final AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(
 				TableActivity.this);
 		mAlertDialog.setTitle("错误");// 设置对话框标题
 		mAlertDialog.setMessage("网络连接失败，请检查网络后重试");// 设置对话框内容
 		mAlertDialog.setCancelable(false);
 		mAlertDialog.setPositiveButton("重试",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						dialog.cancel();
 						ARERTDIALOG = 0;
 						showProgressDlg("正在获取状态...");
 						binderStart();
 					}
 				});
 		mAlertDialog.setNegativeButton("退出",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						ARERTDIALOG = 0;
 						dialog.cancel();
 						finish();
 					}
 				});
 
 		return mAlertDialog;
 	}
 
 	private Dialog cleanDialog() {
 		final CharSequence[] cleanitems = { "清台", "转台", "删除菜", "添加菜", "查看菜" };
 		Dialog cleanDialog = new AlertDialog.Builder(TableActivity.this)
 				.setTitle("选择功能")
 				.setItems(cleanitems, new DialogInterface.OnClickListener() {
 	
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						Intent intent = new Intent();
 						switch (which) {
 						case 0:
 							final AlertDialog.Builder mAlertDialog = cleanTableDialog();
 							mAlertDialog.show();
 							break;
 						case 1:
 							final AlertDialog.Builder mChangeDialog = changeTableDialog();
 							mChangeDialog.show();
 							break;
 						case 2:
 							intent.setClass(TableActivity.this,
 									DelOrderActivity.class);
 							TableActivity.this.startActivity(intent);
 							break;
 						case 3:
 							intent.setClass(TableActivity.this,
 									MenuActivity.class);
 							Info.setMode(Info.WORK_MODE_WAITER);
 							TableActivity.this.startActivity(intent);
 							break;
 						case 4:
 							intent.setClass(TableActivity.this,
 									QueryOrderActivity.class);
 							TableActivity.this.startActivity(intent);
 							break;
 						default:
 							break;
 						}
 					}
 				}).create();
 		return cleanDialog;
 	}
 
 	private AlertDialog.Builder cleanTableDialog() {
 		final AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(
 				TableActivity.this);
 		mAlertDialog.setMessage("请确认是否清台");// 设置对话框内容
 		mAlertDialog.setCancelable(false);
 		mAlertDialog.setPositiveButton("清台",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						showProgressDlg("正在清台...");
 						cleanTableThread();
 					}
 				});
 		mAlertDialog.setNegativeButton("取消",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						dialog.cancel();
 					}
 				});
 		return mAlertDialog;
 	}
 
 	private AlertDialog.Builder cleanPhoneDialog(final int position) {
 		final AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(
 				TableActivity.this);
 		mAlertDialog.setMessage("是否清除顾客点的菜");// 设置对话框内容
 		mAlertDialog.setCancelable(false);
 		mAlertDialog.setPositiveButton("是",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						showProgressDlg("正在删除手机点的菜...");
 						cleanPhoneThread(position);
 						dialog.cancel();
 					}
 				});
 		mAlertDialog.setNegativeButton("否",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						dialog.cancel();
 					}
 				});
 		return mAlertDialog;
 	}
 
 	private AlertDialog.Builder addDialog() {
 		final CharSequence[] additems = { "开台-顾客模式 ", "开台-服务模式" };
 
 		AlertDialog.Builder addDialog = new AlertDialog.Builder(
 				TableActivity.this);
 		addDialog.setTitle("选择功能") // 标题
 				.setIcon(R.drawable.ic_launcher) // icon
 				.setItems(additems, new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						mMyOrder.clear();
 						Intent intent = new Intent();
 						if (Info.getMenu() == Info.ORDER_QUCIK_MENU) {
 							intent.setClass(TableActivity.this,
									QuickMenuActivity.class); 
 							TableActivity.this.startActivity(intent);
 						} else {
 							switch (which) {
 							case 0:
 								intent.setClass(TableActivity.this,
 										MenuActivity.class);
 								Info.setMode(Info.WORK_MODE_CUSTOMER);
 								Info.setNewCustomer(true);
 								TableActivity.this.startActivity(intent);
 								TableActivity.this.finish();
 								break;
 							case 1:
 								intent.setClass(TableActivity.this,
 										MenuActivity.class);
 								Info.setMode(Info.WORK_MODE_WAITER);
 								TableActivity.this.startActivity(intent);
 								break;
 							}
 						}
 					}
 				}).create();
 		return addDialog;
 	}
 
 	private AlertDialog.Builder addPhoneDialog(final int position) {
 		final CharSequence[] additems = { "查看顾客已点的菜", "取消顾客已点的菜" };
 
 		AlertDialog.Builder addPhoneDialog = new AlertDialog.Builder(
 				TableActivity.this);
 		addPhoneDialog.setTitle("选择功能") // 标题
 				.setIcon(R.drawable.ic_launcher) // icon
 				.setItems(additems, new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						Intent intent = new Intent();
 						switch (which) {
 						case 0:
 							intent.setClass(TableActivity.this,
 									PhoneActivity.class);
 							TableActivity.this.startActivity(intent);
 							break;
 						case 1:
 							final AlertDialog.Builder phoneDialog = cleanPhoneDialog(position);
 							phoneDialog.show();
 							break;
 						default:
 							break;
 						}
 					}
 				}).create();
 		return addPhoneDialog;
 	}
 
 	private AlertDialog.Builder notificationDialog() {
 		List<String> add = mNotificaion
 				.getNotifiycationsType(Info.getTableId());
 		String[] additems = (String[]) add.toArray(new String[add.size()]);
 		AlertDialog.Builder addPhoneDialog = new AlertDialog.Builder(
 				TableActivity.this);
 		addPhoneDialog.setTitle("客户呼叫需求").setIcon(R.drawable.ic_launcher)
 				.setItems(additems, null)
 				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 
 					}
 				})
 				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						new Thread(new cleanNotification()).start();
 					}
 				}).create();
 		return addPhoneDialog;
 	}
 
 	protected Builder changeTableDialog() {
 		final EditText changeTableText = new EditText(this);
 		changeTableText.setKeyListener(new DigitsKeyListener(false, true));
 		changeTableText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
 		final AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(
 				TableActivity.this);
 		mAlertDialog.setTitle("请输入桌号");
 		mAlertDialog.setIcon(R.drawable.ic_launcher);
 		mAlertDialog.setCancelable(false);
 		mAlertDialog.setView(changeTableText);
 		mAlertDialog.setPositiveButton("确定",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						String changeTId;
 						changeTId = changeTableText.getEditableText()
 								.toString();
 						int destId = Integer.parseInt(changeTId);
 						if (mSettings.getStatusTableId(destId) == 0 && destId <= mSettings.size()) {
 							changeTable(Integer.parseInt(changeTId));
 						} else {
 							Toast.makeText(
 									getApplicationContext(),
 									getResources().getString(
 											R.string.changeTIdWarning),
 									Toast.LENGTH_SHORT).show();
 						}
 					}
 				});
 		mAlertDialog.setNegativeButton("取消",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						dialog.cancel();
 					}
 				});
 		return mAlertDialog;
 	}
 
 	private Handler tableHandle = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 
 			if (msg.what < 0) {
 				if (ARERTDIALOG == 1) {
 					mNetWrorkcancel.cancel();
 				}
 				ARERTDIALOG = 1;
 				mNetWrorkcancel = mNetWrorkAlertDialog.show();
 			} else {
 				switch (msg.what) {
 				case UPDATE_TABLE_INFOS:
 					setTableInfos();
 					if (mSettings.hasPendedPhoneOrder()) {
 						ringtoneHandler.sendEmptyMessage(1);
 					}
 					break;
 				case DISABLE_GRIDVIEW:
 					gridview.setOnItemClickListener(null);
 					break;
 				default:
 					break;
 				}
 			}
 		}
 	};
 
 	private Handler changeTIdHandle = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what < 0) {
 				Toast.makeText(
 						getApplicationContext(),
 						getResources().getString(
 								R.string.changeTIdWarning),
 						Toast.LENGTH_SHORT).show();
 			} else {
 				binder.start();
 				Toast.makeText(
 						getApplicationContext(),
 						getResources().getString(
 								R.string.changeTId),
 						Toast.LENGTH_SHORT).show();
 				}
 			}
 	};
 
 	private void setTableInfos() {
 		if (lstImageItem.size() > 0) {
 			for (int i = 0, n = 0; i < mSettings.size(); i++) {
 				int status = mSettings.getStatus(i);
 				if (status < NOTIFICATION_STATUS
 						&& mNotificaion.getId(n) == mSettings.getId(i)) {
 					status = status + NOTIFICATION_STATUS;
 					n++;
 				}
 				setTableIcon(i, status);
 			}
 		} else {
 			for (int i = 0, n = 0; i < mSettings.size(); i++) {
 				int status = mSettings.getStatus(i);
 				if (status < NOTIFICATION_STATUS
 						&& mNotificaion.getId(n) == mSettings.getId(i)) {
 					status = status + NOTIFICATION_STATUS;
 					n++;
 				}
 				setTableIcon(i, status);
 			}
 			mImageItems = new SimpleAdapter(TableActivity.this, lstImageItem,
 					R.layout.table_item,
 					new String[] { "imageItem", "ItemText" }, new int[] {
 							R.id.ItemImage, R.id.ItemText }) {
 			};
 			gridview.setAdapter(mImageItems);
 		}
 
 		gridview.setVisibility(View.VISIBLE);
 		mImageItems.notifyDataSetChanged();
 		gridview.setOnItemClickListener(mTableClicked);
 	}
 
 	private void setTableIcon(int position, int status) {
 		HashMap<String, Object> map;
 		if (lstImageItem.size() <= position) {
 			map = new HashMap<String, Object>();
 			map.put("ItemText", "第" + mSettings.getName(position) + "桌");
 		} else {
 			map = lstImageItem.get(position);
 		}
 
 		switch (status) {
 		case 0:
 			map.put("imageItem", R.drawable.table_red);
 			break;
 		case 1:
 			map.put("imageItem", R.drawable.table_blue);
 			break;
 		case 50:
 		case 51:
 			map.put("imageItem", R.drawable.table_yellow);
 			break;
 		case 100:
 			map.put("imageItem", R.drawable.table_rednotification);
 			mSettings.setStatus(position, status);
 			break;
 		case 101:
 			map.put("imageItem", R.drawable.table_bluenotification);
 			mSettings.setStatus(position, status);
 			break;
 		case 150:
 		case 151:
 			map.put("imageItem", R.drawable.table_yellownotification);
 			mSettings.setStatus(position, status);
 			break;
 		default:
 			map.put("imageItem", R.drawable.table_red);
 			break;
 		}
 		if (lstImageItem.size() <= position) {
 			lstImageItem.add(map);
 		}
 	}
 
 	private Handler ringtoneHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what > 0) {
 				mRingtone.play();
 			}
 		}
 	};
 
 	private Handler notificationHandle = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				// Todo network failure warning
 			}
 		}
 	};
 
 	private void cleanTableThread() {
 		new Thread() {
 			public void run() {
 				try {
 					Message msg = new Message();
 					int ret;
 					ret = mSettings.cleanTalble(Info.getTableId());
 					if (ret < 0) {
 						tableHandle.sendEmptyMessage(ret);
 						return;
 					}
 					ret = mNotificaion.getNotifiycations();
 					ringtoneHandler.sendEmptyMessage(ret);
 					ret = mSettings.getTableStatusFromServer();
 					if (ret < 0) {
 						tableHandle.sendEmptyMessage(ret);
 						return;
 					}
 					msg.what = UPDATE_TABLE_INFOS;
 					tableHandle.sendMessage(msg);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	private void cleanPhoneThread(final int position) {
 		new Thread() {
 			public void run() {
 				try {
 					Message msg = new Message();
 					int ret;
 					ret = mSettings.updateStatus(Info.getTableId(),
 							mSettings.getStatus(position) - PHONE_STATUS);
 					if (ret < 0) {
 						tableHandle.sendEmptyMessage(ret);
 						return;
 					}
 
 					ret = mMyOrder.cleanServerPhoneOrder(Info.getTableId());
 					if (ret < 0) {
 
 						tableHandle.sendEmptyMessage(ret);
 						return;
 					}
 
 					ret = mNotificaion.getNotifiycations();
 					ringtoneHandler.sendEmptyMessage(ret);
 					ret = mSettings.getTableStatusFromServer();
 					if (ret < 0) {
 						tableHandle.sendEmptyMessage(ret);
 						return;
 					}
 					msg.what = UPDATE_TABLE_INFOS;
 					tableHandle.sendMessage(msg);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	private void binderStart() {
 		if (binderFlag) {
 			binder.start();
 			return;
 		}
 	}
 
 	private OnClickListener backClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			TableActivity.this.finish();
 		}
 	};
 
 	private OnClickListener updateClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 
 		}
 	};
 
 	private OnClickListener manageClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			Intent intent = new Intent();
 			intent.setClass(TableActivity.this, ManageActivity.class);
 			TableActivity.this.startActivity(intent);
 		}
 	};
 
 	private OnClickListener logoutClicked = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			Info.setMode(Info.WORK_MODE_CUSTOMER);
 			Info.setTableId(-1);
 			finish();
 		}
 
 	};
 
 	private void changeTable(final int destTId) {
 		new Thread() {
 			public void run() {
 				try {
 					int ret = mSettings.changeTable(Info.getTableId(), destTId,TableActivity.this);
 					Log.d("DEL", "ret" + ret);
 					changeTIdHandle.sendEmptyMessage(ret);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 
 	}
 
 	public class MyReceiver extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Bundle bundle = intent.getExtras();
 			mRingtoneMsg = bundle.getInt("ringtoneMessage");
 			mTableMsg = bundle.getInt("tableMessage");
 			mSettings = (TableSetting) bundle
 					.getSerializable(NotificationTableService.SER_KEY);
 			tableHandle.sendEmptyMessage(mTableMsg);
 			ringtoneHandler.sendEmptyMessage(mRingtoneMsg);
 		}
 	}
 
 }
