 /*
  * Copyright 2011-2012 Paddy Byers
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 
 package org.meshpoint.anode;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.meshpoint.anode.Runtime;
 import org.meshpoint.anode.Runtime.IllegalStateException;
 import org.meshpoint.anode.Runtime.InitialisationException;
 import org.meshpoint.anode.Runtime.NodeException;
 import org.meshpoint.anode.Runtime.StateListener;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.ImageView;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 
 import org.obilink.util.QRCodeEncoder;
 import org.obilink.util.Contents;
 import com.google.zxing.BarcodeFormat;
 import com.google.zxing.WriterException;
 
 
 public class AnodeActivity extends Activity implements StateListener {
 
 	private static String TAG = "anode::AnodeActivity";
 	
 	private String modulesToInstall[] = { "express.zip", "obilink.app" };
 	
 	private Context ctx;
 	private Handler viewHandler = new Handler();
 	private long uiThread;
 	private String instance;
 	private Isolate isolate;
 	
 	private TextView wifiName_textView;
 	private TextView urlToConnect_textView;
 	private ImageView qrcode_imageView;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 				
 		setContentView(R.layout.main);
 
 		Config.init(this);
 		Config.setDefaultAppPath(Config.DEFAULT_APP_PATH);
 		
 		ctx = getApplicationContext();		
 		instance = AnodeService.soleInstance();
 		
 		initUI();
 		
 		uiThread = viewHandler.getLooper().getThread().getId();		
 	}
 
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		updateUI(instance == null ? Runtime.STATE_CREATED : Runtime.STATE_STARTED);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.options, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.start_app_menuItem:
 			return onStartAppSelected();
 		case R.id.config_app_menuItem:
 			return onConfigAppSelected();
 		case R.id.install_apps_menuItem:
 			return onInstallAppsSelected();
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	
 	private boolean onStartAppSelected() {
 		String appPath = Config.getDefaultAppPath();
 		
 		if (new File(appPath).exists()) {
 			startApp(appPath);
 		} else {
 			new AlertDialog.Builder(this)
 			.setTitle("앱 시작")
 			.setMessage("[앱 설정]에서 시작할 앱 위치를 입력해 주세요.")
 			.setPositiveButton("확인", null)
 			.show();
 		}
 		
 		return true;
 	}
 	
 	private boolean onConfigAppSelected() {
 		final EditText input = new EditText(this);
 		input.setText(Config.getDefaultAppPath());
 		input.selectAll();
 		
 		new AlertDialog.Builder(this)
 			.setTitle("앱 설정")
 			.setMessage("메뉴에서 실행할 앱 위치를 입력해 주세요.")
 			.setView(input)
 			.setPositiveButton("적용", new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					Config.setDefaultAppPath(input.getText().toString());
 				}
 			}).setNegativeButton("취소", null)
 			.show();
 
 		return true;
 	}
 
 	private boolean onInstallAppsSelected() {		
 		installModulesFromAssets(modulesToInstall);
 		return true;
 	}
 	
 	private void initUI() {
 		wifiName_textView = (TextView)findViewById(R.id.wifiName_textView);
 		urlToConnect_textView = (TextView)findViewById(R.id.urlToConnect_textView);
 		qrcode_imageView = (ImageView)findViewById(R.id.qrcode_imageView);		
 	}
 
 	private void updateUI(final int state) {
 		String url = null;
 		
 		if (state == Runtime.STATE_STARTED) {
 			String ipaddr = getWifiIpAddress();
 			if (ipaddr != null) 
 				url = "http://" + ipaddr + ":" + Config.getPort();
 		}
 		
 		updateWifiName(getWifiSSID());
 		updateURLToConnect(url);
 		updateQRCode(url);
 	}
 	
 	private void initRuntime(String[] opts) {
 		try {
 			Runtime.initRuntime(ctx, opts);
 		} catch (InitialisationException e) {
 			Log.v(TAG, "initRuntime: exception: " + e + "; cause: " + e.getCause());
 		}
 	}
 
 	private void startApp(String appPath) {
 		String options = getIntent().getStringExtra(AnodeReceiver.OPTS);
 		String instance = getIntent().getStringExtra(AnodeReceiver.INST);
 		String[] opts = options == null ? null : options.split("\\s");
 		initRuntime(opts);
 		try {
 			isolate = Runtime.createIsolate();
 			isolate.addStateListener(this);
 			this.instance = AnodeService.addInstance(instance, isolate);
 			isolate.start(appPath.split("\\s"));
 		} catch (IllegalStateException e) {
 			Log.v(TAG, "isolate start: exception: " + e + "; cause: " + e.getCause());
 		} catch (NodeException e) {
 			Log.v(TAG, "isolate start: exception: " + e);
 		}
 	}
 
 	private void stopApp() {
 		if(instance == null) {
 			Log.v(TAG, "AnodeReceiver.onReceive::stop: no instance currently running for this activity");
 			return;
 		}
 		try {
 			isolate.stop();
 		} catch (IllegalStateException e) {
 			Log.v(TAG, "isolate stop : exception: " + e + "; cause: " + e.getCause());
 		} catch (NodeException e) {
 			Log.v(TAG, "isolate stop: exception: " + e);
 		}
 	}
 
 	@Override
 	public void stateChanged(final int state) {
 		if(Thread.currentThread().getId() == uiThread) {
 			__stateChanged(state);
 		} else {
 			viewHandler.post(new Runnable() {
 				public void run() {
 					__stateChanged(state);
 				}
 			});
 		}
 	}
 	
 	private void __stateChanged(final int state) {
 		updateUI(state);
 		
 		if(state == Runtime.STATE_STOPPED) {
 			AnodeService.removeInstance(instance);
 			instance = null;
 			isolate = null;			
 		}		
 	}
 	
 	private String getWifiSSID() {
 		WifiManager wm = (WifiManager)getSystemService(WIFI_SERVICE);
 
 		WifiInfo wi = wm.getConnectionInfo();
 		if (wi != null)
 			return wi.getSSID();
 		
 		return null;
 	}
 
 	private String getWifiIpAddress() {
 		WifiManager wm = (WifiManager)getSystemService(WIFI_SERVICE);
 
 		WifiInfo wi = wm.getConnectionInfo();
 		if (wi != null)
			return Integer.toString(wi.getIpAddress());
 		
 		return null;
 	}
 	
 	private void updateWifiName(String ssid)
 	{
 		String info = ctx.getResources().getString(R.string.ui_wifi_name);
 
 		if (ssid == null)
 			info += ctx.getResources().getString(R.string.ui_wifi_not_connected);
 		else
 			info += ssid;
 		
 		wifiName_textView.setText(info);
 	}
 	
 	private void updateURLToConnect(String url)
 	{
 		if (url != null)
 			urlToConnect_textView.setText(url);
 		else {
 			String errorText = ctx.getResources().getString(R.string.not_available);
 			urlToConnect_textView.setText(errorText);
 		}
 	}
 	
 	private void updateQRCode(String url)
 	{
 		if (url == null) {
 			qrcode_imageView.setImageBitmap(null);
 			return;
 		}
 		
		int smallerDimension = 200;
 		QRCodeEncoder encoder = new QRCodeEncoder(url, null, Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), smallerDimension);
 		try {
 			Bitmap bmp = encoder.encodeAsBitmap();
 			qrcode_imageView.setImageBitmap(bmp);
 		} catch (WriterException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void installModulesFromAssets(String[] moduleZips) {
 		try {
 			for (String zipName : moduleZips) {
 				String zipPath = extractAsset(zipName, Constants.RESOURCE_DIR);
 				if (zipPath != null) {
 					Intent intent = new Intent(AnodeReceiver.ACTION_INSTALL);
 					intent.putExtra(AnodeReceiver.PATH, zipPath);
 					intent.setClassName(ctx, AnodeService.class.getName());
 					ctx.startService(intent);
 				}
 			}			
 		} catch (IOException e) {
 			Log.v(TAG, "installModulesFromAssets: unable to install modules: " + e);
 		}
 	}
 	
 	/**
 	 * Extract the module/app from assets to the default resource location.
 	 * @throws IOException
 	 */
 	private String extractAsset(String assetName, String destDir) throws IOException {
 		File dir, asset, pkg;
 		if (!(dir = new File(destDir)).exists())
 			dir.mkdirs();
 		
 		if ((asset = new File(destDir, assetName)).exists()) {
 			/* check to see if this timestamp pre-dates the current package */
 			if((pkg = new File(ctx.getPackageResourcePath())).exists()) {
 				if(pkg.lastModified() < asset.lastModified()) {
 					Log.v(TAG, "extractAsset: Asset up to date");
 					return null;
 				}
 			}
 			Log.v(TAG, "extractAsset: Asset present but out of date");
 			asset.delete();
 		}
 		Log.v(TAG, "extractAsset: copying Asset");
 		InputStream in = ctx.getAssets().open(assetName);
 		FileOutputStream out = new FileOutputStream(asset);
 		int read;
 		byte[] buf = new byte[8192];
 		while((read = in.read(buf)) != -1)
 				out.write(buf, 0, read);
 		in.close();
 		out.flush();
 		out.close();
 		
 		return asset.getPath();
 	}
 }
