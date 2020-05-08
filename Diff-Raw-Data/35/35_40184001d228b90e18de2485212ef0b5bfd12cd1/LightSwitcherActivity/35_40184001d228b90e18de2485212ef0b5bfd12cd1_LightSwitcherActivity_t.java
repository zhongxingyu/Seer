 package de.karlsve.autolightsoff;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import de.karlsve.autolightsoff.LightSwitcher.LightSwitcherBinder;
 
 public class LightSwitcherActivity extends Activity {
 	
 	private ServiceConnection serviceConnection = new ServiceConnection() {
 
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
 			binder = (LightSwitcherBinder)serviceBinder;
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			binder = null;
 		}
 		
 	};
 	
 	LightSwitcherBinder binder = null;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		this.startService(new Intent(this.getApplicationContext(), LightSwitcher.class));
 		this.finish();
 	}
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		if(this.binder != null) {
 			this.unbindService(serviceConnection);
 		}
 	}
 	
 }
