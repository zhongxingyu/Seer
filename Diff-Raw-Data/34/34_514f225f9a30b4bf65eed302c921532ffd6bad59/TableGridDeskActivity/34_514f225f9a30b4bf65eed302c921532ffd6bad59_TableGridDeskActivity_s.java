 package com.htb.cnk.ui.base;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
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
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.EditText;
 import android.widget.SimpleAdapter;
 
 import com.htb.cnk.DelOrderActivity;
 import com.htb.cnk.MenuActivity;
 import com.htb.cnk.PhoneActivity;
 import com.htb.cnk.QueryOrderActivity;
 import com.htb.cnk.QuickMenuActivity;
 import com.htb.cnk.R;
 import com.htb.cnk.adapter.TableAdapter;
 import com.htb.cnk.data.Info;
 import com.htb.cnk.data.NotificationTypes;
 import com.htb.cnk.data.Notifications;
 import com.htb.cnk.data.PhoneOrder;
 import com.htb.cnk.data.Setting;
 import com.htb.cnk.data.TableSetting;
 import com.htb.cnk.lib.Ringtone;
 import com.htb.cnk.service.MyReceiver;
 import com.htb.cnk.service.NotificationTableService;
 import com.htb.cnk.service.NotificationTableService.MyBinder;
 import com.htb.constant.Table;
 
public class TableGridDeskActivity extends BaseActivity {
 
 	private final static String TAG = "TableGridDeskActivity";
 	protected final int UPDATE_TABLE_INFOS = 500;
 	protected final int DISABLE_GRIDVIEW = 1000;
 	protected final int CHECKOUT_LIST = 1;
 	protected final int COMBINE_DIALOG = 1;
 	protected final int CHANGE_DIALOG = 2;
 
 	protected boolean binderFlag;
 	protected Intent intent;
 
 	private int mTableMsg;
 	private int mRingtoneMsg;
 
 	private MyReceiver mReceiver;
 	protected NotificationTableService.MyBinder binder;
 	protected Notifications mNotification = new Notifications();
 	protected NotificationTypes mNotificationType = new NotificationTypes();
 
 	protected List<String> tableName = new ArrayList<String>();
 	protected List<Integer> selectedTable = new ArrayList<Integer>();
 
 	protected PhoneOrder mPhoneOrder;
 	protected TableSetting mSettings;
 	protected Ringtone mRingtone;
 	protected SimpleAdapter mImageItems;
 
 	protected EditText tableIdEdit;
 	protected EditText personsEdit;
 
 	protected Handler mNotificationHandler;
 	protected Handler mTableHandler = new Handler();
 	protected Handler mRingtoneHandler = new Handler();
 	protected Handler mChangeTIdHandler;
 	protected Handler mCombineTIdHandler;
 	protected Handler mCopyTIdHandler;
 	protected Handler mNotificationTypeHandler;
 
 	protected int currentPage;
 	protected TableAdapter mTableInfo;
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		binderStart();
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		unbindService(conn);
 		unregisterReceiver(mReceiver);
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setNewClass();
 		startService(NotificationTableService.class);
 		bindService(intent, conn, Context.BIND_AUTO_CREATE);
 		mReceiver = new MyReceiver(TableGridDeskActivity.this);
 		registerReceiver(mReceiver);
 		NotificationType();
 	}
 
 	private void setNewClass() {
 		mPhoneOrder = new PhoneOrder(TableGridDeskActivity.this);
 		setSettings(new TableSetting(TableGridDeskActivity.this));
 		mRingtone = new Ringtone(TableGridDeskActivity.this);
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
 
 	protected void registerReceiver(BroadcastReceiver receiver) {
 		IntentFilter filter = new IntentFilter(
 				NotificationTableService.SERVICE_IDENTIFIER);
 		registerReceiver(receiver, filter);
 	}
 
 	protected void startService(Class<?> cla) {
 		intent = new Intent(TableGridDeskActivity.this, cla);
 		startService(intent);
 	}
 
 	DialogInterface.OnClickListener cleanListener = new DialogInterface.OnClickListener() {
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			cleanChioceMode(which);
 		}
 	};
 
 	protected void binderStart() {
 		if (binderFlag) {
 			binder.start();
 			return;
 		}
 	}
 
 	private AlertDialog.Builder cleanDialog() {
 		final CharSequence[] cleanitems = getResources().getStringArray(
 				R.array.openStatus);
 		return mItemDialog.itemChooseFunctionDialog(cleanitems, cleanListener);
 	}
 
 	DialogInterface.OnClickListener cleanTableListener = new DialogInterface.OnClickListener() {
 		@Override
 		public void onClick(DialogInterface dialog, int i) {
 			showProgressDlg(getResources().getString(R.string.cleanTableNow));
 			selectedTable.clear();
 			selectedTable.add(Info.getTableId());
 			cleanTableThread(selectedTable);
 		}
 	};
 
 	private void cleanChioceMode(int which) {
 		switch (which) {
 		case 0:
 			cleanTableDialog().show();
 			break;
 		case 1:
 			changeOrCombineDialog(CHANGE_DIALOG).show();
 			break;
 		case 2:
 			changeOrCombineDialog(COMBINE_DIALOG).show();
 			break;
 		case 3:
 			setClassToActivity(DelOrderActivity.class);
 			break;
 		case 4:
 			Info.setMode(Info.WORK_MODE_WAITER);
 			setClassToActivity(MenuActivity.class);
 			break;
 		case 5:
 			setClassToActivity(QueryOrderActivity.class);
 			break;
 		default:
 			break;
 		}
 	}
 
 	private AlertDialog.Builder cleanTableDialog() {
 		return mTitleAndMessageDialog.messageDialog(false, getResources()
 				.getString(R.string.isCleanTable),
 				getResources().getString(R.string.yes), cleanTableListener,
 				getResources().getString(R.string.no), null);
 	}
 
 	DialogInterface.OnClickListener addListener = new DialogInterface.OnClickListener() {
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			mPhoneOrder.clear();
 			addDialogChoiceMode(which);
 		}
 	};
 
 	private AlertDialog.Builder cleanPhoneDialog(final int position) {
 		DialogInterface.OnClickListener cleanPhoneListener = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int i) {
 				showProgressDlg(getResources().getString(
 						R.string.cleanPhoneOrderNow));
 				cleanPhoneThread(position, Info.getTableId());
 			}
 		};
 
 		return mTitleAndMessageDialog.messageDialog(false, getResources()
 				.getString(R.string.isCleanOrder),
 				getResources().getString(R.string.yes), cleanPhoneListener,
 				getResources().getString(R.string.no), null);
 	}
 
 	private AlertDialog.Builder addDialog() {
 		final CharSequence[] additems = getResources().getStringArray(
 				R.array.normalStatus);
 		return mItemDialog.itemChooseFunctionDialog(additems, addListener);
 	}
 
 	DialogInterface.OnClickListener notificationListener = new DialogInterface.OnClickListener() {
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			cleanNotification();
 		}
 	};
 
 	private void addDialogChoiceMode(int which) {
 		switch (which) {
 		case 0:
 			Info.setMode(Info.WORK_MODE_CUSTOMER);
 			Info.setNewCustomer(true);
 			setClassToActivity(MenuActivity.class);
			TableGridDeskActivity.this.finish();
 			break;
 		case 1:
 			if (Info.getMenu() == Info.ORDER_QUCIK_MENU) {
 				setClassToActivity(QuickMenuActivity.class);
 			} else {
 				Info.setMode(Info.WORK_MODE_WAITER);
 				setClassToActivity(MenuActivity.class);
 			}
 			break;
 		case 2:
 			copyTableDialog().show();
 			break;
 		default:
 			break;
 		}
 	}
 
 	private AlertDialog.Builder addPhoneDialog(final int position) {
 		final CharSequence[] additems = getResources().getStringArray(
 				R.array.phoneStatus);
 		DialogInterface.OnClickListener addPhoneListener = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				addPhoneChoiceMode(position, which);
 			}
 		};
 
 		return mItemDialog.itemChooseFunctionDialog(additems, addPhoneListener);
 	}
 
 	private void addPhoneChoiceMode(final int position, int which) {
 		switch (which) {
 		case 0:
 			setClassToActivity(PhoneActivity.class);
 			break;
 		case 1:
 			cleanPhoneDialog(position).show();
 			break;
 		default:
 			break;
 		}
 	}
 
 	private AlertDialog.Builder notificationDialog() {
 		List<String> add = mNotification
 				.getNotifiycationsType(Info.getTableId());
 		String[] additems = (String[]) add.toArray(new String[add.size()]);
 		return mItemDialog.itemButtonDialog(false,
 				getResources().getString(R.string.customerCall), additems,
 				null, null, notificationListener);
 	}
 
 	protected OnItemClickListener tableItemClickListener = new OnItemClickListener() {
 
 		public void onItemClick(AdapterView<?> arg0,// The AdapterView where the
 													// click happened
 				View arg1,// The view within the AdapterView that was clicked
 				int arg2,// The position of the view in the adapter
 				long arg3// The row id of the item that was clicked
 		) {
 			if (isNameIdStatusLegal(arg2)) {
 				Info.setTableName(mTableInfo.getName(arg2));
 				Info.setTableId(mTableInfo.getId(arg2));
 				tableItemChioceDialog(arg2, mTableInfo.getStatus(arg2));
 			} else {
 				toastText("不能获取信息，请检查设备！");
 			}
 		}
 
 		private boolean isNameIdStatusLegal(int arg2) {
 			return (mTableInfo.getName(arg2)) != null
 					&& (mTableInfo.getId(arg2) != -1)
 					&& (mTableInfo.getStatus(arg2) != -1);
 		}
 
 		private void tableItemChioceDialog(int arg2, int status) {
 			switch (status) {
 			case 0:
 				addDialog().show();
 				break;
 			case 1:
 				cleanDialog().show();
 				break;
 			case 50:
 			case 51:
 				addPhoneDialog(arg2).show();
 				break;
 			case 100:
 			case 101:
 			case 150:
 			case 151:
 				notificationDialog().show();
 				break;
 			default:
 				addDialog().show();
 				break;
 			}
 		}
 
 	};
 
 	private Builder changeOrCombineDialog(final int type) {
 		final AlertDialog.Builder changeTableAlertDialog;
 		View layout = getDialogLayout(R.layout.change_dialog,
 				R.id.change_dialog);
 		personsEdit = (EditText) layout.findViewById(R.id.personsEdit);
 		if (Setting.enabledPersons()) {
 			tableIdEdit = (EditText) layout.findViewById(R.id.tableIdEdit);
 			changeTableAlertDialog = mViewDialog.viewDialog(false, layout);
 		} else {
 			tableIdEdit = editTextListener();
 			changeTableAlertDialog = mViewDialog.viewDialog(false, tableIdEdit);
 		}
 		tableIdEdit.addTextChangedListener(watcher(tableIdEdit));
 		personsEdit.addTextChangedListener(watcher(personsEdit));
 		changeTableAlertDialog.setTitle(getResources().getString(
 				R.string.pleaseInput));
 
 		DialogInterface.OnClickListener changeTablePositiveListener = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				String changePersons;
 				String tableName = tableIdEdit.getText().toString()
 						.toUpperCase();
 				if (Setting.enabledPersons()) {
 					changePersons = personsEdit.getText().toString();
 				} else {
 					changePersons = "0";
 				}
 				if (type == CHANGE_DIALOG) {
 					judgeChangeTable(changePersons, tableName);
 				} else if (type == COMBINE_DIALOG) {
 					judgeCombineTable(changePersons, tableName);
 				}
 			}
 		};
 
 		changeTableAlertDialog.setPositiveButton(
 				getResources().getString(R.string.ok),
 				changeTablePositiveListener);
 		changeTableAlertDialog.setNegativeButton(
 				getResources().getString(R.string.cancel), null);
 		return changeTableAlertDialog;
 	}
 
 	private Builder copyTableDialog() {
 		final EditText copyTableText = editTextListener();
 		copyTableText.addTextChangedListener(watcher(copyTableText));
 
 		DialogInterface.OnClickListener copyTableListener = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				String tableName = copyTableText.getEditableText().toString()
 						.toUpperCase();
 				if (tableName.equals("")) {
 					toastText(R.string.idAndPersonsIsNull);
 				} else if (isBoundaryLegal(tableName, Table.OPEN_TABLE_STATUS)) {
 					copyTable(mSettings.getId(tableName));
 				} else {
 					toastText(R.string.copyTIdwarning);
 				}
 			}
 		};
 
 		return mViewDialog.viewAndTitleAndButtonDialog(false, copyTableText,
 				getResources().getString(R.string.pleaseInput), null,
 				copyTableListener);
 	}
 
 	private EditText editTextListener() {
 		final EditText editText = new EditText(this);
 		// editText.setKeyListener(new DigitsKeyListener(false, true));
 		// editText
 		// .setFilters(new InputFilter[] { new InputFilter.LengthFilter(6) });
 		return editText;
 	}
 
 	private View getDialogLayout(int layout_dialog, int id) {
 		LayoutInflater inflater = getLayoutInflater();
 		View layout = inflater.inflate(layout_dialog,
 				(ViewGroup) findViewById(id));
 		return layout;
 	}
 
 	private void cleanNotification() {
 		new Thread() {
 			public void run() {
 				try {
 					int ret = mNotification
 							.cleanNotifications(Info.getTableId());
 					mNotificationHandler.sendEmptyMessage(ret);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	protected void cleanTableThread(final List<Integer> tableId) {
 		new Thread() {
 			public void run() {
 				try {
 					Message msg = new Message();
 					mTableHandler.sendEmptyMessage(DISABLE_GRIDVIEW);
 					int ret = getSettings().cleanTalble(tableId);
 					if (ret < 0) {
 						mTableHandler.sendEmptyMessage(ret);
 						return;
 					}
 					if (getNotifiycations() < 0)
 						return;
 					if (getTableStatusFromServer() < 0)
 						return;
 					msg.what = UPDATE_TABLE_INFOS;
 					mTableHandler.sendMessage(msg);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	private void cleanPhoneThread(final int position, final int tableId) {
 		new Thread() {
 			public void run() {
 				try {
 					Message msg = new Message();
 					mTableHandler.sendEmptyMessage(DISABLE_GRIDVIEW);
 					int ret = getSettings().updateStatus(tableId,
 							TableSetting.PHONE_ORDER);
 					if (ret < 0) {
 						mTableHandler.sendEmptyMessage(ret);
 						return;
 					}
 					ret = mPhoneOrder.cleanServerPhoneOrder(tableId);
 					if (ret < 0) {
 						mTableHandler.sendEmptyMessage(ret);
 						return;
 					}
 					if (getNotifiycations() < 0)
 						return;
 					if (getTableStatusFromServer() < 0)
 						return;
 					msg.what = UPDATE_TABLE_INFOS;
 					mTableHandler.sendMessage(msg);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	private void combineTable(final int destTId, final int persons) {
 		new Thread() {
 			public void run() {
 				try {
 					int ret = getSettings().combineTable(
 							  Info.getTableId(),
 							destTId, mSettings.getName(Info.getTableId()),
 							mSettings.getName(destTId), persons);
 					mCombineTIdHandler.sendEmptyMessage(ret);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	private void changeTable(final int destTId, final int persons) {
 		mpDialog.show();
 		new Thread() {
 			public void run() {
 				try {
 					int ret = getSettings().changeTable(
 							  Info.getTableId(),
 							destTId, getSettings().getName(Info.getTableId()),
 							getSettings().getName(destTId), persons);
 					mChangeTIdHandler.sendEmptyMessage(ret);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	private void copyTable(final int srcTId) {
 		mpDialog.show();
 		new Thread() {
 			public void run() {
 				try {
 					int ret = getSettings().getOrderFromServer(srcTId);
 					mCopyTIdHandler.sendEmptyMessage(ret);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
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
 
 	private int getTableStatusFromServer() {
 		int ret = getSettings().getTableStatusFromServerActivity();
 		if (ret < 0) {
 			mTableHandler.sendEmptyMessage(ret);
 		}
 		return ret;
 	}
 
 	private void getParseTableSetting(final String msg) {
 		new Thread() {
 			public void run() {
 				try {
 					mTableHandler.sendEmptyMessage(DISABLE_GRIDVIEW);
 					int ret = getSettings().parseTableSetting(msg);
 					if (ret < 0) {
 						mTableHandler.sendEmptyMessage(ret);
 					} else {
 						mTableHandler.sendEmptyMessage(UPDATE_TABLE_INFOS);
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 
 	}
 
 	private boolean isStatusLegal(String tableName, int status) {
 		return getSettings().getStatusTableId(getSettings().getId(tableName)) == status;
 	}
 
 	private boolean isTId(String tableTId) {
 		return (getSettings().getId(tableTId) != -1);
 	}
 
 	private boolean isBoundaryLegal(String tableName, int status) {
 		if (tableName.equals(Info.getTableName())) {
 			return false;
 		}
 		return isTId(tableName) && isStatusLegal(tableName, status);
 	}
 
 	private boolean equalNameAndPersons(String changePersons, String tableName) {
 		return tableName.equals("") || changePersons.equals("");
 	}
 
 	private int getNotifiycations() {
 		int ret = mNotification.getNotifiycations();
 		mRingtoneHandler.sendEmptyMessage(ret);
 		return ret;
 	}
 
 	private void judgeChangeTable(String changePersons, String tableName)
 			throws NumberFormatException {
 		if (equalNameAndPersons(changePersons, tableName)) {
 			toastText(R.string.idAndPersonsIsNull);
 			return;
 		}
 
 		if (isBoundaryLegal(tableName, Table.NORMAL_TABLE_STAUTS)) {
 			changeTable(getSettings().getId(tableName),
 					Integer.parseInt(changePersons));
 		} else {
 			toastText(R.string.changeTIdWarning);
 		}
 	}
 
 	private void judgeCombineTable(String changePersons, String tableName)
 			throws NumberFormatException {
 		if (equalNameAndPersons(changePersons, tableName)) {
 			toastText(R.string.idAndPersonsIsNull);
 			return;
 		}
 
 		if (isBoundaryLegal(tableName, Table.OPEN_TABLE_STATUS)) {
 			combineTable(getSettings().getId(tableName),
 					Integer.parseInt(changePersons));
 		} else {
 			toastText(R.string.combineTIdWarning);
 		}
 	}
 
 	public TextWatcher watcher(final EditText id) {
 		TextWatcher watcher = new TextWatcher() {
 			String tempStr;
 			EditText edit;
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 				tempStr = s.toString();
 				if (tempStr.indexOf("0") == 0) {
 					tempStr = tempStr.substring(1, tempStr.length());
 					edit = id;
 					edit.setText(tempStr);
 				}
 			}
 
 			@Override
 			public void afterTextChanged(Editable arg0) {
 
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 
 		};
 		return watcher;
 	}
 
 	protected void setClassToActivity(Class<?> setClass) {
 		intent.setClass(TableGridDeskActivity.this, setClass);
 		TableGridDeskActivity.this.startActivity(intent);
 	}
 
 	public TableSetting getSettings() {
 		return mSettings;
 	}
 
 	public void setSettings(TableSetting mSettings) {
 		this.mSettings = mSettings;
 	}
 
 	public int getRingtoneMsg() {
 		return mRingtoneMsg;
 	}
 
 	public void setRingtoneMsg(int mRingtoneMsg) {
 		this.mRingtoneMsg = mRingtoneMsg;
 	}
 
 	public int getTableMsg() {
 		return mTableMsg;
 	}
 
 	public void setTableMsg(String mTableMsg) {
 		getParseTableSetting(mTableMsg);
 	}
 
 	public Handler getTableHandler() {
 		return mTableHandler;
 	}
 
 	public void setTableHandler(Handler mTableHandler) {
 		this.mTableHandler = mTableHandler;
 	}
 
 	public Handler getRingtoneHandler() {
 		return mRingtoneHandler;
 	}
 
 	public void setRingtoneHandler(Handler mRingtoneHandler) {
 		this.mRingtoneHandler = mRingtoneHandler;
 		sendRingtoneMsg();
 	}
 
 	public void sendTableMsg() {
 		getTableHandler().sendEmptyMessage(getTableMsg());
 	}
 
 	public void sendRingtoneMsg() {
 		getRingtoneHandler().sendEmptyMessage(getRingtoneMsg());
 	}
 
 }
