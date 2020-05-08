 package com.hcmus.dictionaryqlqt;
 
 import bridge.AndroidBridge;
 import bridge.AndroidBridgeListener;
 import manager.WebviewHelper;
 import manager.WordOfDayHandler;
 import model.WordOfDay;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebView;
 import android.widget.ImageView;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 
 /**
  * 
  * @author Minh Khanh
  * 
  * Tab xem word of day
  */
 
 public class TabDailyActivity extends Activity implements AndroidBridgeListener, OnClickListener {
 
 	/*
 	 * webview hien thi noi dung
 	 */
 	private WebView wbvContent;
 	private ImageView btnZoom;
 	
 	/*
 	 * progress dialog khi load noi dung
 	 */
 	private ProgressDialog pgbLoading;
 	
 	private final int WOD = 0;
 	private final int ERROR = 1;
 	
 	/*
 	 * luu trang thai da load hay chua
 	 */
 	private boolean isLoaded = false;
 	private boolean isFullScreen = false;
 	
 	private AndroidBridge bridge;
 	/**
 	 * xu ly cap nhat giao dien tu thread load du lieu
 	 */
 	@SuppressLint("HandlerLeak")
 	private Handler handler = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what == WOD){
 				WordOfDay wod = (WordOfDay) msg.obj;
 				showContent(wod);
 				isLoaded = true;
 			}
 			else if (msg.what == ERROR){
 				if (pgbLoading.isShowing()){
 					pgbLoading.dismiss();
 				}
 				showDialogCheckNetwork();				
 			}
 		};
 	};
 
 	/**
 	 * load wod tu internet
 	 */
 	private Runnable getWod = new Runnable() {
 
 		@Override
 		public void run() {
 			try {
 				WordOfDayHandler wodHandler = new WordOfDayHandler(TabDailyActivity.this);
 				WordOfDay wod = wodHandler.getWod();
 				
				Message msg;
				if (wod != null){
					msg = handler.obtainMessage(WOD, wod);
				}
				else{
					msg = handler.obtainMessage(ERROR, null);
				}
 				handler.sendMessage(msg);
 
 			} catch (Exception e) {
 				Log.e("Daily", "Parser error: " + e.getMessage());
 			}
 		}
 	};
 
 	/**
 	 * khoi tao cac thanh phan giao dien
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_tab_daily);
 
 		wbvContent = (WebView) findViewById(R.id.wbvContent);
 		pgbLoading = new ProgressDialog(this);
 		
 		btnZoom = (ImageView) findViewById(R.id.btnZoom);
 		btnZoom.setOnClickListener(this);
 		
 	}
 	
 	/**
 	 * su kien man hinh hien thi tro lai
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		// kiem da load hay chua
 		if (!isLoaded){
 			loadData();
 		}
 	}
 	
 	/**
 	 * load du lieu
 	 */
 	private void loadData() {
 		if (isOnline()) {
 			// neu mang kha dung thi load du lieu
 			showLoadingDialog();
 			Thread thrLoadData = new Thread(getWod);
 			thrLoadData.start();
 		} else {
 			// hien thi thong bao mang khong kha dung
 			showDialogCheckNetwork();
 		}
 	}
 
 	/**
 	 * kiem tra ket noi mang
 	 * @return true neu co ket noi, false neu nguoc lai
 	 */
 	public boolean isOnline() {
 		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo netInfo = cm.getActiveNetworkInfo();
 		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * hien thi dialog loading khi lay du lieu
 	 */
 	private void showLoadingDialog(){
 		String msg = getResources().getString(R.string.wod_message_loading_dialog);
 		pgbLoading.setMessage(msg);
 		pgbLoading.show();
 	}
 
 	/**
 	 * show dialog kiem tra ket noi
 	 */
 	private void showDialogCheckNetwork() {
 		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);		 
         alertDialog.setTitle(R.string.wod_title_check_network_dialog);
         alertDialog.setMessage(R.string.wod_message_check_network_dialog);
         alertDialog.setPositiveButton("Try again", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog,int which) {
             	loadData();
             } 
         });
  
         alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
             	dialog.dismiss();
             }
         });
         alertDialog.show();
 	}
 	
 	/**
 	 * show wod day len webview
 	 */
 	private void showContent(WordOfDay wod) {
 		bridge = new AndroidBridge();
 		bridge.setListener(this);
 		WebviewHelper.ShowWOD(wbvContent, wod, bridge);
 	}
 
 	/**
 	 * su kien searh tu webview
 	 */
 	@Override
 	public void lookup(final String word) {
 		if (isFullScreen){
 			btnZoom.setImageResource(R.drawable.ic_zoom_in);
 			HomeActivity.getInstance().showTabs();
 			isFullScreen = false;
 		}
 		// chuyen sang tab Dictionary
 		HomeActivity.getInstance().setDictionaryTab(word);
 	}
 	
 	@Override
 	public void speakOut(String text) {}
 
 	@Override
 	public void setFavorite(String word) {}
 
 	@Override
 	public void removeFavorite(String word) {}
 
 	/**
 	 * su kien load noi dung hoan tat
 	 */
 	@Override
 	public void onLoadComplete() {
 		if (pgbLoading.isShowing()){
 			pgbLoading.dismiss();
 		}		
 	}
 
 	/**
 	 * xu ly su kien zoom man hinh
 	 */
 	@Override
 	public void onClick(View v) {
 		if (v.getId() == R.id.btnZoom){
 			if (isFullScreen){
 				btnZoom.setImageResource(R.drawable.ic_zoom_in);
 				HomeActivity.getInstance().showTabs();
 			} 
 			else{
 				btnZoom.setImageResource(R.drawable.ic_zoom_out);
 				HomeActivity.getInstance().hideTabs();
 			}
 			isFullScreen = !isFullScreen;
 		}
 	}
 }
