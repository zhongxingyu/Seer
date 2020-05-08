 package de.philipphock.android.servicetest;
 
 import de.philipphock.android.lib.services.ServiceUtil;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 
 public class MainActivity extends Activity implements ServiceConnection {
 
 	private ServiceTaskService service=null;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		LocalBroadcastManager.getInstance(this).registerReceiver(serviceMessageReveiver, new IntentFilter(GenericConsumerService.SERVICE_STATE));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
	 
 	@Override
 	protected void onDestroy() {
 		LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceMessageReveiver);
 		super.onDestroy();
 		
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		updateServiceUIState();
 			
 			
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		unbindService(this);
 	}
 	
 	private void updateServiceUIState(){
 		Button onStartStopServiceButton = (Button) findViewById(R.id.startstopservice);
 		if (ServiceUtil.isServiceRunning(this, ServiceTaskService.class)){
 			onStartStopServiceButton.setText("stop service");
 		}else{
 			onStartStopServiceButton.setText("start service");
 		}		
 	}
 	
 
 	public void onStartStopService(View v){
 		Intent i = new Intent(this,ServiceTaskService.class);
 		if (ServiceUtil.isServiceRunning(this, ServiceTaskService.class)){
 			stopService(i);
 		}else{
 			startService(i);
			bindService(new Intent(this, ServiceTaskService.class), this,0);
 		}
 		updateServiceUIState();
 	}
 
 	
 	//serviceConnection (note: in tutorials, the use of anonymous classes is preferred)
 	
 	@Override
 	public void onServiceConnected(ComponentName name, IBinder service) {
 		this.service=(ServiceTaskService) ((GenericConsumerService.MyBinder) service ).getService();
 		
 	}
 
 	@Override
 	public void onServiceDisconnected(ComponentName name) {
 		this.service=null;
 	}
 	
 	//broadcast receiver
 	private BroadcastReceiver serviceMessageReveiver = new BroadcastReceiver() {
 		
 		@Override
 		public void onReceive(Context context, Intent intent) {
 				updateServiceUIState();			
 		}
 	};
 }
