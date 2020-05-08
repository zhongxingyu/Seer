 package uk.co.mentalspace.android.mwintenttester;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class MainActivity extends Activity implements OnClickListener {
 	private static final String LOGNAME = "MW Tester";
 	private BroadcastReceiver mwReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             MainActivity.this.receiveBroadcast(intent);
         }
     };
 	private boolean mwReceiverIsRegistered = false;
 		
	public static final String MW_ANNOUNCE = "org.metawatch.manager.APPLICATION_ANNOUCE";
 	public static final String MW_UPDATE = "org.metawatch.manager.APPLICATION_UPDATE";
 	public static final String MW_START = "org.metawatch.manager.APPLICATION_START";
 	public static final String MW_STOP = "org.metawatch.manager.APPLICATION_STOP";
 	public static final String MW_NOTIFICATION = "org.metawatch.manager.NOTIFICATION";
 	public static final String MW_VIBRATE = "org.metawatch.manager.VIBRATE";
 	public static final String MW_SILENTMODE = "org.metawatch.manager.SILENTMODE";
 	public static final String MW_WIDGET_UPDATE = "org.metawatch.manager.WIDGET_UPDATE";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if (!mwReceiverIsRegistered) {
 		    registerReceiver(mwReceiver, new IntentFilter(MW_ANNOUNCE));
 		    registerReceiver(mwReceiver, new IntentFilter(MW_NOTIFICATION));
 		    registerReceiver(mwReceiver, new IntentFilter(MW_SILENTMODE));
 		    registerReceiver(mwReceiver, new IntentFilter(MW_START));
 		    registerReceiver(mwReceiver, new IntentFilter(MW_STOP));
 		    registerReceiver(mwReceiver, new IntentFilter(MW_UPDATE));
 		    registerReceiver(mwReceiver, new IntentFilter(MW_VIBRATE));
 		    registerReceiver(mwReceiver, new IntentFilter(MW_WIDGET_UPDATE));
 		    mwReceiverIsRegistered = true;
 		}
 	}
 	
 	@Override
 	protected void onPause() {
 		if (mwReceiverIsRegistered) {
 		    unregisterReceiver(mwReceiver);
 		    mwReceiverIsRegistered = false;
 		}
 		super.onPause();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
 	public void receiveBroadcast(Intent intent) {
 		String action = intent.getAction();
 		log("Rcvd: "+action);
 		
 		if (MW_ANNOUNCE.equals(action)) {
 			log("App id ["+intent.getStringExtra("id")+"], name ["+intent.getStringExtra("name")+"]");
 		}
 		if (MW_UPDATE.equals(action)) {
 			Bitmap bmp = Bitmap.createBitmap(intent.getIntArrayExtra("array"), 96, 96, Bitmap.Config.RGB_565);
 			((ImageView)findViewById(R.id.screen_image)).setImageBitmap(bmp);
 		}
 		if (MW_NOTIFICATION.equals(action)) {
 			Bundle b = intent.getExtras();
 			StringBuffer sb = new StringBuffer();
 			if (b.containsKey("oled1")) sb.append("oled1:"+b.getString("oled1")+"\n");
 			if (b.containsKey("oled1a")) sb.append("oled1a:"+b.getString("oled1a")+"\n");
 			if (b.containsKey("oled1b")) sb.append("oled1b:"+b.getString("oled1b")+"\n");
 			if (b.containsKey("oled2")) sb.append("oled2:"+b.getString("oled2")+"\n");
 			if (b.containsKey("oled2a")) sb.append("oled2a:"+b.getString("oled2a")+"\n");
 			if (b.containsKey("oled2b")) sb.append("oled2b:"+b.getString("oled2b")+"\n");
 			if (b.containsKey("title")) sb.append("title:"+b.getString("title")+"\n");
 			if (b.containsKey("text")) sb.append("text:"+b.getString("text")+"\n");
 			if (b.containsKey("icon")) sb.append("icon: yes\n");
 			if (b.containsKey("sticky")) sb.append("sticky:"+b.getBoolean("sticky")+"\n");
 			if (sb.length() > 0) log("Notification:\n"+sb.toString());
 			
 			if (b.containsKey("array")) {
 				Bitmap bmp = Bitmap.createBitmap(intent.getIntArrayExtra("array"), 96, 96, Bitmap.Config.RGB_565);
 				((ImageView)findViewById(R.id.screen_image)).setImageBitmap(bmp);
 			}
 			if (b.containsKey("buffer")) {
 				byte[] buffer = intent.getByteArrayExtra("buffer");
 				Bitmap bmp = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
 				((ImageView)findViewById(R.id.screen_image)).setImageBitmap(bmp);
 			}
 		}
 	}
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
     	case R.id.menu_Discovery:
     		sendDiscoveryIntent(null);
     		return true;
     	case R.id.menu_Activate:
     		sendActivateIntent(null);
     		return true;
     	case R.id.menu_Deactivate:
     		sendDeactivateIntent(null);
     		return true;
     	default:
     		return super.onOptionsItemSelected(item);
     	}
     }
     
     public String getAppId() {
     	String appId = ((EditText)findViewById(R.id.screen_app_id)).getText().toString();
     	if (null == appId) appId = "";
     	return appId;
     }
 
     public void sendDeactivateIntent(View view) {
 		Intent intent = new Intent("org.metawatch.manager.APPLICATION_DEACTIVATE");
 		Bundle b = new Bundle();
 		String appId = getAppId();
 		b.putString("id", appId);
 		intent.putExtras(b);
 		this.sendBroadcast(intent);
 		log("Send deactivate intent to: "+appId);
     }
     
     public void sendActivateIntent(View view) {
 		Intent intent = new Intent("org.metawatch.manager.APPLICATION_ACTIVATE");
 		Bundle b = new Bundle();
 		String appId = getAppId();
 		b.putString("id", appId);
 		intent.putExtras(b);
 		this.sendBroadcast(intent);
 		log("Send activate intent to: " + appId);
     }
 
     public void sendDiscoveryIntent(View view) {
 		Intent intent = new Intent("org.metawatch.manager.APPLICATION_DISCOVERY");
 		this.sendBroadcast(intent);
 		log("Send announce intent");
     }
 
 	@Override
 	public void onClick(View arg0) {
 		Intent intent = new Intent("org.metawatch.manager.BUTTON_PRESS");
 		Bundle b = new Bundle();
 		String appId = getAppId();
 		b.putString("id", appId);
 		int button = Integer.parseInt(((TextView)arg0).getText().toString());
 		b.putInt("button", button);
 		b.putInt("type", 1); //TODO determine correct type value
 		intent.putExtras(b);
 		this.sendBroadcast(intent);
 		log("Send press for button ["+button+"] to: "+appId);
 	}
 	
 	private void log(String msg) {
 		Log.d(LOGNAME, msg);
 		msg += "\n";
 		((EditText)findViewById(R.id.screen_log_area)).getText().insert(0, msg);
 	}
 }
