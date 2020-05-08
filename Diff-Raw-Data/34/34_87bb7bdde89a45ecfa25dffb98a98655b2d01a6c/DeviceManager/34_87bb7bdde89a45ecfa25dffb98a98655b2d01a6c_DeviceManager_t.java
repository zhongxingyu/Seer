 package com.iped.ipcam.gui;
 
 import java.util.Date;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.HandlerThread;
 import android.os.Message;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.iped.ipcam.engine.CamMagFactory;
 import com.iped.ipcam.engine.ICamManager;
 import com.iped.ipcam.factory.ICustomDialog;
 import com.iped.ipcam.pojo.Device;
 import com.iped.ipcam.utils.AnimUtil;
 import com.iped.ipcam.utils.Constants;
 import com.iped.ipcam.utils.DeviceAdapter;
 import com.iped.ipcam.utils.DialogUtils;
 import com.iped.ipcam.utils.ErrorCode;
 import com.iped.ipcam.utils.FileUtil;
 import com.iped.ipcam.utils.PackageUtil;
 import com.iped.ipcam.utils.RandomUtil;
 import com.iped.ipcam.utils.ToastUtils;
 import com.iped.ipcam.utils.WebCamActions;
 import com.iped.ipcam.view.PullToRefreshListView;
 import com.iped.ipcam.view.PullToRefreshListView.OnRefreshListener;
 
 public class DeviceManager extends ListActivity implements OnClickListener, OnItemLongClickListener {
 
 	private PullToRefreshListView listView = null;
 
 	private DeviceAdapter adapter = null;
 
 	private View deiceListViewFooter;
 	
 	private TextView deviceListViewFootMore;
 	
 	private ProgressBar deviceListViewFootProgress;
 	
 	private final int MENU_EDIT = Menu.FIRST;
 
 	private final int MENU_DEL = Menu.FIRST + 1;
 
 	private final int MENU_PREVIEW = Menu.FIRST + 2;
 
 	private Button autoSearchButton = null;
 
 	private Button manulAddButton = null;
 
 	private Button deviceParaSetButton = null;
 	
 	private Button clearCamButton = null;
 
 	private ICamManager camManager = null;
 
 	private CustomProgressDialog progressDialog = null;
 
 	private CustomProgressDialog queryNewCameraDialog = null;
 	
 	private int lastSelected = 0;
 
 	private String deviceName = "";
 	
 	private String pwd;
 	
 	private Device deviceTmp;
 	
 	private Device device;
 	
 	private CustomProgressDialog m_Dialog = null;
 	
 	private String password = "";
 	
 	private Button sureAddNewDeivce = null;
 	
 	private Button cancleAddNewDeivce = null;
 	
 	private CustomAlertDialog alertDialog = null;
 	
 	private EditText newDeivceId = null;
 	
 	private EditText newDeviceName = null;
 	
 	private Button sureAddNewDeivceResult = null;
 	
 	private Button cancleAddNewDeivceResult = null;
 	
 	
 	//private MonitorSocketThread monitorSocketThread = null;
 	
 	private static final String TAG = "DeviceManager";
 
 	private Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message message) {
 			switch (message.what) {
 			case Constants.HIDETEAUTOSEARCH:
 				dismissProgress();
				//FileUtil.persistentDevice(DeviceManager.this,camManager.getCamList());
 				listView.onRefreshComplete(getString(R.string.pull_to_refresh_update) + new Date().toLocaleString());
				break;
			case Constants.UPDATEDEVICELIST:
				adapter.notifyDataSetChanged();
				FileUtil.persistentDevice(DeviceManager.this,camManager.getCamList());
 				listView.setSelection(0);
 				if(camManager.getCamList().size() >0) {
 					//deviceListViewFootMore.setText(getText(R.string.full_device_online));
 					deiceListViewFooter.setVisibility(View.GONE);
 				} else {
 					deiceListViewFooter.setVisibility(View.VISIBLE);
 					deviceListViewFootMore.setText(getText(R.string.no_device_online));
 				}
 				handler.sendEmptyMessageDelayed(Constants.SEND_UPDATE_DEVICE_LIST_MSG, 200);
 				break;
 			case Constants.DEFAULTUSERSELECT:
 				listView.requestFocusFromTouch();
 				listView.setSelection(lastSelected);
 				break;
 			case Constants.SHOWTOASTMSG:
 				int id = message.arg1;
 				showToast(id);
 				break;
 			case Constants.SHOWRESULTDIALOG:
 				Bundle bundle = message.getData();
 				if(bundle != null) {
 					Object obj = bundle.get("NEW_DEVICE");
 					if(obj instanceof Device) {
 						showResult((Device)obj);
 					}
 				}
 				break;
 			case Constants.SENDGETUNFULLPACKAGEMSG:
 				showToast(R.string.device_manager_find_device_id_error);
 				handler.sendEmptyMessage(Constants.HIDETEAUTOSEARCH);
 				break;
 			case Constants.SENDGETTHREEPORTTIMOUTMSG:
 				showToast(R.string.device_manager_find_device_id_error);
 				handler.sendEmptyMessage(Constants.HIDETEAUTOSEARCH);
 				break;
 			case Constants.SEND_SHOW_ONE_PWD_FIELD_CONFIG_MSG:
 				handler.sendEmptyMessage(Constants.HIDETEAUTOSEARCH);
 				DialogUtils.inputTwoPasswordDialog(DeviceManager.this, camManager.getSelectDevice(), handler, Constants.WEB_CAM_CHECK_PWD_MSG);
 				break;
 			case Constants.SEND_SHOW_TWO_PWD_FIELD_CONFIG_MSG:
 				pwd = (String) message.obj;
 				Intent intent = new Intent(WebCamActions.QUERY_CONFIG_ACTION);
             	intent.setDataAndType(Uri.parse(pwd), WebCamActions.QUERY_CONFIG_MINITYPE);
             	Log.d(TAG, "======>" + pwd);
             	startActivity(intent);
 				break;
 			case Constants.SEND_ADD_NEW_DEVICE_BY_IP_MSG:
 				String pass = (String) message.obj;
 				deviceTmp.setUnDefine2(pass);
 				FileUtil.persistentDevice(DeviceManager.this,camManager.getCamList());
 				break;
 			case Constants.SEND_UPDATE_DEVICE_LIST_MSG:
 				sendBroadcast(new Intent(WebCamActions.SEND_DEVICE_LIST_UPDATE_ACTION));
 				break;
 			case Constants.WEB_CAM_CONNECT_INIT_MSG:
 				String random = RandomUtil.generalRandom();
 				Log.d(TAG, "random = " + random);
 				int initRes = 1;//UdtTools.initialSocket(device.getDeviceID(),random);
 				if(initRes<0) {
 					Log.d(TAG, "initialSocket init error!");
 					ToastUtils.showToast(DeviceManager.this, R.string.webcam_connect_init_error);
 				}else {
 					handler.sendEmptyMessage(Constants.WEB_CAM_CHECK_PWD_STATE_MSG);
 				}
 				break;
 			case Constants.WEB_CAM_CHECK_PWD_STATE_MSG:
 				int resu = PackageUtil.checkPwdState(device.getDeviceID());
 				Log.d(TAG, "device manager checkPwdState result = " + resu);
 				handler.removeMessages(Constants.WEB_CAM_SHOW_CHECK_PWD_DLG_MSG);
 				handler.sendEmptyMessage(Constants.WEB_CAM_HIDE_CHECK_PWD_DLG_MSG);
 				if(resu == 0) { // unset
 					handler.sendEmptyMessage(Constants.SEND_SHOW_INPUT_TWO_PASS_DIALOG_SMG);
 					//DialogUtils.inputTwoPasswordDialog(DeviceManager.this, device, handler, Constants.SEND_SHOW_TWO_PWD_FIELD_PREVIEW_MSG);
 				} else if(resu == 1) {// pwd seted
 					if(device.getUnDefine2() != null && device.getUnDefine2().length()>0) {
 						Message msg = handler.obtainMessage();
 						msg.obj  = device.getUnDefine2();
 						msg.what = Constants.WEB_CAM_CHECK_PWD_MSG;
 						handler.sendMessage(msg);
 					}else {
 						handler.sendEmptyMessage(Constants.SEND_SHOW_INPUT_ONE_PASS_DIALOG_SMG);
 					}
 				} else if(resu == 2){
 					ToastUtils.showToast(DeviceManager.this, R.string.device_manager_pwd_set_err);
 					//DialogUtils.inputOnePasswordDialog(DeviceManager.this, handler, Constants.SEND_SHOW_TWO_PWD_FIELD_PREVIEW_MSG);
 				} else {
 					ToastUtils.showToast(DeviceManager.this, R.string.device_manager_time_out_or_device_off_line);
 				}
 				break;
 			case Constants.WEB_CAM_SHOW_CHECK_PWD_DLG_MSG:
 				showProgressDlg(R.string.webcam_check_pwd_dialog_str);
 				break;
 			case Constants.WEB_CAM_HIDE_CHECK_PWD_DLG_MSG:
 				hideProgressDlg();
 				break;
 			case Constants.SEND_SHOW_INPUT_TWO_PASS_DIALOG_SMG:
 				//DialogUtils.inputTwoPasswordDialog(DeviceManager.this, camManager.getSelectDevice(), handler, Constants.WEB_CAM_CHECK_PWD_MSG);
 				break;
 			case Constants.SEND_SHOW_INPUT_ONE_PASS_DIALOG_SMG:
 				DialogUtils.inputOnePasswordDialog(DeviceManager.this, handler, Constants.WEB_CAM_CHECK_PWD_MSG);
 				break;
 			case Constants.WEB_CAM_CHECK_PWD_MSG:
 				handler.sendEmptyMessage(Constants.WEB_CAM_SHOW_CHECK_PWD_DLG_MSG);
 				password = (String) message.obj;
 				handler.removeCallbacks(checkPwdRunnable);
 				handler.post(checkPwdRunnable);
 				//new CheckPwdThread().start();
 				break;
 			
 			default:
 				break;
 			}
 		}
 	};
 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		UdtTools.startUp();
 		setContentView(R.layout.device_manager);
 		autoSearchButton = (Button) findViewById(R.id.auto_search_button);
 		manulAddButton = (Button) findViewById(R.id.manul_add_button);
 		deviceParaSetButton = (Button) findViewById(R.id.device_manager_button);
 		clearCamButton = (Button) findViewById(R.id.clear_all_button);
 		camManager = CamMagFactory.getCamManagerInstance();
 		initDeviceListView();
 		
 		//adapter = new DeviceAdapter(, this);
 		registerForContextMenu(getListView());
 		autoSearchButton.setOnClickListener(this);
 		manulAddButton.setOnClickListener(this);
 		deviceParaSetButton.setOnClickListener(this);
 		clearCamButton.setOnClickListener(this);
 	}
 
 	private void initDeviceListView() {
 		adapter = new DeviceAdapter(camManager.getCamList(), this);
 		camManager.getCamList().addAll(FileUtil.fetchDeviceFromFile(this));
 		listView = (PullToRefreshListView)getListView();
 		deiceListViewFooter = getLayoutInflater().inflate(R.layout.layout_list_view_footer, null);
 		deviceListViewFootMore = (TextView) deiceListViewFooter.findViewById(R.id.list_view_foot_more);
 		deviceListViewFootProgress = (ProgressBar) deiceListViewFooter.findViewById(R.id.list_view_foot_progress);
 		listView.addFooterView(deiceListViewFooter);
 		listView.setAdapter(adapter);
 		listView.setOnItemClickListener(itemClickListener);
 		listView.setOnItemLongClickListener(this);
 		listView.setOnScrollListener(new OnScrollListener() {
 			
 			@Override
 			public void onScrollStateChanged(AbsListView view, int scrollState) {
 				listView.onScrollStateChanged(view, scrollState);
 			}
 			
 			@Override
 			public void onScroll(AbsListView view, int firstVisibleItem,
 					int visibleItemCount, int totalItemCount) {
 				listView.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
 				if(camManager.getCamList().size() <=0) {
 					listView.onScrollStateChanged(view, SCROLL_STATE_TOUCH_SCROLL );
 				}
 			}
 		});
 		listView.setOnRefreshListner(new OnRefreshListener() {
 			@Override
 			public void onRefresh() {
 				camManager.startThread(handler);
 			}
 		});
 		if(camManager.getCamList().size() >0) {
 			deiceListViewFooter.setVisibility(View.GONE);
 		} 
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		AdapterView.AdapterContextMenuInfo info = null;
 		try {
 			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
 		} catch (Exception e) {
 			return;
 		}
 		Device device = camManager.getDevice(info.position-1);
 		if(device == null) {
 			return;
 		}
 		menu.setHeaderTitle(device.getDeviceName());
 		menu.add(0, MENU_PREVIEW, 1, getString(R.string.device_preview_str));
 		menu.add(0, MENU_DEL, 2, getString(R.string.device_del_str));
 		menu.add(0, MENU_EDIT, 3, getString(R.string.device_edit_str));
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo infor = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 		switch (item.getItemId()) {
 		case MENU_EDIT:
 			//editDevice(device);
 			break;
 
 		case MENU_DEL:
 			int seletIndex = infor.position-1;
 			if (seletIndex>0) {
 				adapter.setChecked(seletIndex-1);
 			}
 			camManager.delCam(seletIndex);
 			handler.sendEmptyMessage(Constants.UPDATEDEVICELIST);
 			break;
 		case MENU_PREVIEW:
 			WebTabWidget.tabHost.setCurrentTabByTag(Constants.VIDEOPREVIEW);
 			sendBroadcast(new Intent(WebCamActions.ACTION_IPPLAY));
 			break;
 		default:
 			break;
 		}
 		return super.onContextItemSelected(item);
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.auto_search_button:
 			showProgress();
 			camManager.startThread(handler);
 			break;
 		case R.id.manul_add_button:
 			addNewDevice();
 			break;
 		case R.id.device_manager_button:
 			device = camManager.getSelectDevice();
 			if(device == null) {
 				Log.d(TAG, "device = " + device);
 				ToastUtils.showToast(DeviceManager.this, R.string.device_params_info_no_device_str);
 				return;
 			}
 			handler.sendEmptyMessage(Constants.WEB_CAM_SHOW_CHECK_PWD_DLG_MSG);
 			HandlerThread handlerThread = new HandlerThread("test");
 			handlerThread.start();
 			Handler myHandler = new Handler(handlerThread.getLooper());
 			myHandler.post(monitorSocketTask);
 			//handler.removeCallbacks(monitorSocketTask);
 			//handler.postDelayed(monitorSocketTask, 100);
 			break;
 		case R.id.clear_all_button:
 			if(camManager.getCamList().size()>0) {
 				final ICustomDialog customDialog = new CustomAlertDialog(this, R.style.thems_customer_alert_dailog);
 		        customDialog.setContentView(R.layout.delete_device_dialog_layout);
 		        customDialog.setTitle(getString(R.string.clear_all_device_title_str));
 		        customDialog.show();
 		        customDialog.findViewById(R.id.web_cam_sure_button).setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						camManager.clearCamList();
 						adapter.setChecked(0);
 						handler.sendEmptyMessage(Constants.UPDATEDEVICELIST);
 						customDialog.dismiss();
 					}
 		        });
 		        customDialog.findViewById(R.id.web_cam_cancl_button).setOnClickListener(new OnClickListener() {
 		        	@Override
 					public void onClick(View v) {
 		        		customDialog.dismiss();
 		        	}
 		        });
 			}
 	        break;
 		case R.id.web_cam_sure_add_new_device:
 			String cameraId = newDeivceId.getText().toString().trim();
 			if (cameraId == null || cameraId.length() <= 0) {
 				ToastUtils.showToast(DeviceManager.this, R.string.device_manager_new_device_id_not_null);
 				return;
 			}
 			//queryNewCameraDialog = ProgressDialog.show(DeviceManager.this, getString(R.string.device_manager_add_query_title_str), getString(R.string.device_manager_add_query_message_str));
 			queryNewCameraDialog = CustomProgressDialog.createDialog(this, R.style.CustomProgressDialog); 
 			queryNewCameraDialog.setTitile(getString(R.string.device_manager_add_query_title_str));
 			queryNewCameraDialog.setMessage(getString(R.string.device_manager_add_query_message_str));
			queryNewCameraDialog.show();
 			new Thread(new QueryCamera(newDeviceName.getText().toString(), cameraId,"","","","", true)).start();
 			alertDialog.dismiss();
 			alertDialog = null;
 			break;
 		case R.id.web_cam_cancl_add_new_device_result:
 		case R.id.web_cam_cancl_add_new_device:
 			if(alertDialog != null && alertDialog.isShowing()) {
 				alertDialog.dismiss();
 				alertDialog = null;
 			}
 			break;
 		default:
 			break;
 		}
 	}
 
 	
 	private void showProgress() {
 		if(progressDialog != null) {
 			dismissProgress();
 		}
 		//progressDialog = new ProgressDialog(DeviceManager.this);
 		progressDialog = CustomProgressDialog.createDialog(this, R.style.CustomProgressDialog); 
 		progressDialog.setMessage(getResources().getString(R.string.auto_search_tips_str));
 		//progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		//progressDialog.setCancelable(false);
 		progressDialog.show();
 	}
 
 	public void dismissProgress() {
 		if (progressDialog != null && progressDialog.isShowing()) {
 			progressDialog.dismiss();
 			progressDialog = null;
 		}
 		
 		if(queryNewCameraDialog != null){
 			queryNewCameraDialog.dismiss();
 			queryNewCameraDialog = null;
 		}
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK && progressDialog != null && progressDialog.isShowing()) {
 			dismissProgress();
 			camManager.stopThread();
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	private OnItemClickListener itemClickListener = new OnItemClickListener() {
 
 		@Override
 		public void onItemClick(AdapterView<?> arg0, View arg1, int index,
 				long arg3) {
 			if(index == 0) {
 				return;
 			}
 			listView.requestFocusFromTouch();
 			lastSelected = index;
 			listView.setSelection(index);
 			camManager.setSelectInde(index-1);
 			adapter.setChecked(index-1);
 			adapter.notifyDataSetChanged();	
 		}
 	};
 
 	@Override
 	public boolean onItemLongClick(AdapterView<?> parent, View view,
 			int position, long id) {
 		if(position == 0) {
 			return true;
 		}
 		listView.requestFocusFromTouch();
 		lastSelected = position;
 		listView.setSelection(position);
 		camManager.setSelectInde(position-1);
 		adapter.setChecked(position-1);
 		adapter.notifyDataSetChanged();	
 		return false;
 	}
 	
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 	}
 	
 	private void addNewDevice() {
 		alertDialog = new CustomAlertDialog(this, R.style.thems_customer_alert_dailog);
 		alertDialog.setTitle(getResources().getString(R.string.device_manager_add_title_str));
 		alertDialog.setContentView(R.layout.device_manager_add);
 		alertDialog.show();
 		sureAddNewDeivce = (Button) alertDialog.findViewById(R.id.web_cam_sure_add_new_device);
 		sureAddNewDeivce.setOnClickListener(this);
 		cancleAddNewDeivce = (Button) alertDialog.findViewById(R.id.web_cam_cancl_add_new_device);
 		cancleAddNewDeivce.setOnClickListener(this);
 		newDeivceId = (EditText) alertDialog.findViewById(R.id.device_manager_new_device_id_edittext);
 		newDeviceName = (EditText) alertDialog.findViewById(R.id.device_manager_add_name_id);
 	}
 
 	protected void onResume() {
 		super.onResume();
 		handler.sendEmptyMessage(Constants.UPDATEDEVICELIST);
 		handler.sendMessageDelayed(
 				handler.obtainMessage(Constants.DEFAULTUSERSELECT), 50);
 	};
 
 	private void showToast(int id) {
 		ToastUtils.showToast(this, id);
 	}
 	
 	private class QueryCamera implements Runnable {
 
 		private String name;
 		
 		private String ip;
 
 		private String gateWay;
 		
 		private String mask;
 		
 		private String dns1;
 		
 		private String dns2;
 		
 		private boolean flag;
 
 		public QueryCamera(String name, String ip, String gateWay, String mask, String dns1,String dns2, boolean flag) {
 			this.name = name;
 			this.ip = ip;
 			this.gateWay = gateWay;
 			this.mask = mask;
 			this.dns1 = dns1;
 			this.dns2 = dns2;
 			this.flag = flag;
 		}
 
 		@Override
 		public void run() {
 				int res = UdtTools.checkCmdSocketEnable(ip);// ip ʵ豸ID
 				System.out.println("res=" + res);
 				if (res>0) {
 					deviceTmp = new Device();
 					deviceTmp.setDeviceName(name);
 					deviceTmp.setDeviceID(ip);
 					Bundle bundle2 = new Bundle();
 					Message msg = handler.obtainMessage();
 					bundle2.putSerializable("NEW_DEVICE", deviceTmp);
 					msg.setData(bundle2);
 					msg.what = Constants.SHOWRESULTDIALOG;
 					handler.sendMessage(msg);
 				}else {
 					String random = RandomUtil.generalRandom();
 					//Log.d(TAG, "random = " + random);
 					int result = UdtTools.monitorCmdSocket(ip, random);
 					Log.d(TAG, "monitor result = " + result);
 					if(result>=0) {
 						deviceTmp = new Device();
 						deviceTmp.setDeviceName(name);
 						deviceTmp.setDeviceID(ip);
 						Bundle bundle2 = new Bundle();
 						Message msg = handler.obtainMessage();
 						bundle2.putSerializable("NEW_DEVICE", deviceTmp);
 						msg.setData(bundle2);
 						msg.what = Constants.SHOWRESULTDIALOG;
 						handler.sendMessage(msg);
 					}else {
 						Message msg = handler.obtainMessage();
 						int tips = 0;
 						switch (result) {
 						case ErrorCode.STUN_ERR_INTERNAL:
 							tips = R.string.webcam_error_code_internel;
 							break;
 						case ErrorCode.STUN_ERR_SERVER:
 							tips = R.string.webcam_error_code_server_not_reached;
 							break;
 						case ErrorCode.STUN_ERR_TIMEOUT:
 							tips = R.string.webcam_error_code_timeout;
 							break;
 						case ErrorCode.STUN_ERR_INVALIDID:
 							tips = R.string.webcam_error_code_unlegal;
 							break;
 						case ErrorCode.STUN_ERR_CONNECT:
 							tips = R.string.webcam_error_code_connect_error;
 							break;
 						case ErrorCode.STUN_ERR_BIND:
 							tips = R.string.webcam_error_code_bind_error;
 							break;
 						default:
 							tips = R.string.device_manager_new_device_add_fail_str;
 							break;
 						}
 						msg.arg1 = tips;
 						msg.what = Constants.SHOWTOASTMSG;
 						handler.sendMessage(msg);
 					}
 				}
 				handler.sendEmptyMessage(Constants.HIDETEAUTOSEARCH);
 		}
 	}
 
 	private void showResult(final Device device) {
 		UdtTools.freeCmdSocket();
 		alertDialog = new CustomAlertDialog(DeviceManager.this, R.style.thems_customer_alert_dailog);
 		alertDialog.setTitle(getResources().getString(R.string.device_manager_new_device_title));
 		alertDialog.setContentView(R.layout.layout_show_add_new_device_result);
 		alertDialog.show();
 		sureAddNewDeivceResult = (Button) alertDialog.findViewById(R.id.web_cam_sure_add_new_device_result);
 		cancleAddNewDeivceResult = (Button) alertDialog.findViewById(R.id.web_cam_cancl_add_new_device_result);
 		cancleAddNewDeivceResult.setOnClickListener(this);
 		sureAddNewDeivceResult.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if(camManager.addCam(device)) {
 					handler.sendEmptyMessage(Constants.UPDATEDEVICELIST);
 					Message msg = handler.obtainMessage();
 					msg.arg1 = R.string.device_manager_new_device_add_success_str;
 					msg.what = Constants.SHOWTOASTMSG;
 					handler.sendMessage(msg);
 				} else {
 					showToast(R.string.device_manager_add_device_is_exist);
 				}
 				alertDialog.dismiss();
 				alertDialog = null;
 			}
 		});
 	}
 	
 	private Runnable monitorSocketTask = new Runnable() {
 		@Override
 		public void run() {
 			//UdtTools.close();
 			String id = device.getDeviceID();
 			int res = UdtTools.checkCmdSocketEnable(id);
 			Log.d(TAG, "UdtTools checkCmdSocketEnable result = " + res);
 			if(res>0) {
 				handler.sendEmptyMessage(Constants.WEB_CAM_HIDE_CHECK_PWD_DLG_MSG);
 				Intent intent = new Intent(WebCamActions.QUERY_CONFIG_ACTION);
 				intent.setType(WebCamActions.QUERY_CONFIG_MINITYPE);
 				startActivity(intent);
 			}else {
 				String random = RandomUtil.generalRandom();
 				//Log.d(TAG, "random = " + random);
 				int result = UdtTools.monitorCmdSocket(id, random);
 				Log.d(TAG, "monitor result = " + result);
 				analyseResult(result, device);
 			}
 		}
 	};
 	
 	private void analyseResult(int result, Device device) {
 		switch (result) {
 		case ErrorCode.STUN_ERR_INTERNAL:
 			ToastUtils.showToast(this, R.string.webcam_error_code_internel);
 			handler.sendEmptyMessage(Constants.WEB_CAM_HIDE_CHECK_PWD_DLG_MSG);
 			return;
 		case ErrorCode.STUN_ERR_SERVER:
 			ToastUtils.showToast(this, R.string.webcam_error_code_server_not_reached);
 			handler.sendEmptyMessage(Constants.WEB_CAM_HIDE_CHECK_PWD_DLG_MSG);
 			return;
 		case ErrorCode.STUN_ERR_TIMEOUT:
 			ToastUtils.showToast(this, R.string.webcam_error_code_timeout);
 			handler.sendEmptyMessage(Constants.WEB_CAM_HIDE_CHECK_PWD_DLG_MSG);
 			return;
 		case ErrorCode.STUN_ERR_INVALIDID:
 			handler.sendEmptyMessage(Constants.WEB_CAM_HIDE_CHECK_PWD_DLG_MSG);
 			ToastUtils.showToast(this, R.string.webcam_error_code_unlegal);
 			return;
 		case ErrorCode.STUN_ERR_CONNECT:
 			handler.sendEmptyMessage(Constants.WEB_CAM_HIDE_CHECK_PWD_DLG_MSG);
 			ToastUtils.showToast(this, R.string.webcam_error_code_connect_error);
 			return;
 		case ErrorCode.STUN_ERR_BIND:
 			handler.sendEmptyMessage(Constants.WEB_CAM_HIDE_CHECK_PWD_DLG_MSG);
 			ToastUtils.showToast(this, R.string.webcam_error_code_bind_error);
 			return;
 		default:
 			break;
 		}
 		HandlerThread handlerThread = new HandlerThread("test1");
 		handlerThread.start();
 		Handler mHandler = new Handler(handlerThread.getLooper());
 		mHandler.post(checkPwdStateRunnable);
 	}
 	
 	private Runnable checkPwdStateRunnable = new Runnable() {
 
 		@Override
 		public void run() {
 			int resu = PackageUtil.checkPwdState(device.getDeviceID());
 			Log.d(TAG, "device manager checkPwdState result = " + resu);
 			if(resu == 0) { // unset
 				handler.sendEmptyMessage(Constants.WEB_CAM_HIDE_CHECK_PWD_DLG_MSG);
 				handler.sendEmptyMessage(Constants.SEND_SHOW_INPUT_TWO_PASS_DIALOG_SMG);
 				//DialogUtils.inputTwoPasswordDialog(DeviceManager.this, device, handler, Constants.SEND_SHOW_TWO_PWD_FIELD_PREVIEW_MSG);
 			} else if(resu == 1) {// pwd seted
 				if(device.getUnDefine2() != null && device.getUnDefine2().length()>0) {
 					Message mesg = handler.obtainMessage();
 					mesg.obj  = device.getUnDefine2();
 					mesg.what = Constants.WEB_CAM_CHECK_PWD_MSG;
 					handler.sendMessage(mesg);
 				}else {
 					handler.sendEmptyMessage(Constants.SEND_SHOW_INPUT_ONE_PASS_DIALOG_SMG);
 					handler.sendEmptyMessage(Constants.WEB_CAM_HIDE_CHECK_PWD_DLG_MSG);
 				}
 			} else if(resu == 2){
 				handler.sendEmptyMessage(Constants.WEB_CAM_HIDE_CHECK_PWD_DLG_MSG);
 				ToastUtils.showToast(DeviceManager.this, R.string.device_manager_pwd_set_err);
 			} else {
 				handler.sendEmptyMessage(Constants.WEB_CAM_HIDE_CHECK_PWD_DLG_MSG);
 				ToastUtils.showToast(DeviceManager.this, R.string.device_manager_time_out_or_device_off_line);
 			}
 		}
 	};
 	
 	
 	private Runnable checkPwdRunnable = new Runnable() {
 
 		@Override
 		public void run() {
 			int checkPwd = PackageUtil.checkPwd(device.getDeviceID(),password);
 			Log.d(TAG, "checkPwd result = " + checkPwd);
 			if(checkPwd == 1) {
 				device.setUnDefine2(password);
 				camManager.updateCam(device);
 				FileUtil.persistentDevice(DeviceManager.this,camManager.getCamList());
 				Intent intent = new Intent(WebCamActions.QUERY_CONFIG_ACTION);
 				intent.setType(WebCamActions.QUERY_CONFIG_MINITYPE);
 				startActivity(intent);
 				handler.sendEmptyMessage(Constants.WEB_CAM_HIDE_CHECK_PWD_DLG_MSG);
 			} else if(checkPwd == -1) {
 				ToastUtils.showToast(DeviceManager.this, R.string.device_manager_pwd_set_err);
 				handler.sendEmptyMessage(Constants.WEB_CAM_HIDE_CHECK_PWD_DLG_MSG);
 				//DialogUtils.inputOnePasswordDialog(DeviceManager.this, handler, Constants.SEND_SHOW_TWO_PWD_FIELD_PREVIEW_MSG);
 				handler.sendEmptyMessage(Constants.SEND_SHOW_INPUT_ONE_PASS_DIALOG_SMG);
 			} else {
 				handler.sendEmptyMessage(Constants.WEB_CAM_HIDE_CHECK_PWD_DLG_MSG);
 				ToastUtils.showToast(DeviceManager.this, R.string.device_manager_time_out_or_device_off_line);
 			}
 		}
 	};
 	
 	private void showProgressDlg(int textId) {
 		if(m_Dialog == null) {
 			//m_Dialog = new ProgressDialog(DeviceManager.this);
 			m_Dialog = CustomProgressDialog.createDialog(this, R.style.CustomProgressDialog);
 			//m_Dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 			m_Dialog.setCancelable(false);
 		}
 		m_Dialog.setMessage(getResources().getString(textId, device.getDeviceID()));
 		if(!m_Dialog.isShowing()) {
 			m_Dialog.show();
 		}
 	}
 	
 	private void hideProgressDlg() {
 		if(m_Dialog != null && m_Dialog.isShowing()) {
 			m_Dialog.dismiss();
 			m_Dialog = null;
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.list_options_menu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 	
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch(item.getItemId()) {
 		case R.id.change_login_pwd:
 			changeLoginPwd();
 			break;
 			default:
 				break;
 		}
 		return super.onMenuItemSelected(featureId, item);
 	}
 	
 	private void changeLoginPwd() {
 		final SharedPreferences settings = getSharedPreferences(WebCam.class.getName(), 0);
 		final ICustomDialog customDialog = new CustomAlertDialog(this, R.style.thems_customer_alert_dailog);
 		customDialog.setContentView(R.layout.layout_modify_pwd_dialog);
         customDialog.setTitle(getResources().getString(R.string.password_modify_title_str));
         customDialog.show();
         customDialog.findViewById(R.id.web_cam_sure_modify).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				 final EditText oldPassword = (EditText) customDialog.findViewById(R.id.firstPassword);
            	  final EditText newPassword = (EditText) customDialog.findViewById(R.id.secondPassword);
            	  final EditText repeadNewPassword = (EditText) customDialog.findViewById(R.id.thirdPassword);
            	  String oldPwd = oldPassword.getText().toString().trim();
                  String newPwd1 = newPassword.getText().toString().trim();
                  String newPwd2 = repeadNewPassword.getText().toString().trim();
                  if(oldPwd== null || oldPwd.length()<=0){
                	  	AnimUtil.animEff(DeviceManager.this, oldPassword, R.anim.shake_anim);
                	  	ToastUtils.showToast(DeviceManager.this, R.string.password_is_null);
 	                return;
                  } 
                  if(!oldPwd.equalsIgnoreCase(settings.getString("PASSWORD", "admin"))) {
                	  	AnimUtil.animEff(DeviceManager.this, oldPassword, R.anim.shake_anim);
                	  	ToastUtils.showToast(DeviceManager.this, R.string.old_input_password_error);
 	                return;
            	  	}
                  if( newPwd1 == null || newPwd1.length()<=0 ){
 	               	  AnimUtil.animEff(DeviceManager.this, newPassword, R.anim.shake_anim);
 	               	  ToastUtils.showToast(DeviceManager.this, R.string.password_is_null);
 		              return;
                  }
                  if(newPwd2 == null ||  newPwd2.length()<=0) {
 	               	  ToastUtils.showToast(DeviceManager.this, R.string.password_is_null);
 	               	  ToastUtils.showToast(DeviceManager.this, R.string.password_is_null);
 		              return;
                  } 
                  if(!newPwd1.equalsIgnoreCase(newPwd2)) {
                	  	ToastUtils.showToast(DeviceManager.this, R.string.password_not_equal);
                 } else {
                	 	settings.edit().putString("PASSWORD", newPwd1).commit();
                	 	ToastUtils.showToast(DeviceManager.this, R.string.password_modify_success_str);
                	 	customDialog.dismiss();
                 }
 			}
         });
         customDialog.findViewById(R.id.web_cam_cancl_modify).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				customDialog.dismiss();
 			}
         });
 	}
 }
