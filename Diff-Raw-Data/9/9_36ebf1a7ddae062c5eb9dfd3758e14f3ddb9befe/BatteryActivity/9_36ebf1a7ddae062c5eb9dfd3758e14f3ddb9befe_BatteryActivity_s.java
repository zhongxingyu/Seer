 package ru.nekit.androidsamplelist.samples;
 
 import ru.nekit.androidsamplelist.R;
 import ru.nekit.androidsamplelist.activityExtra.GoUpActivity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.BatteryManager;
 import android.os.Bundle;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class BatteryActivity extends GoUpActivity {
 
 	private static BatteryActivity instance;
 	private ViewHolder view;
 
 	static private class ViewHolder
 	{
 		ProgressBar level;
 		TextView levelText;
 		TextView status;
 		TextView info;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		instance = this;
 		setContentView(R.layout.activity_battery);
 		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
 		updateBatteryInfo(registerReceiver(null, ifilter));
 	}
 
 	private void updateBatteryInfo(Intent batteryStatus)
 	{
 		if( view == null )
 		{
 			view = new ViewHolder();
 			view.level = (ProgressBar)findViewById(R.id.level);
 			view.levelText = (TextView)findViewById(R.id.levelText);
 			view.status = (TextView)findViewById(R.id.status);
 			view.info = (TextView)findViewById(R.id.info);
 		}
 		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
 		boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
 				status == BatteryManager.BATTERY_STATUS_FULL;
 		int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
 		boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
 		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
 		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
 		float batteryPct = level / (float)scale;
 		view.level.setMax(100);
 		view.level.setProgress((int)batteryPct*100);
 		view.levelText.setText((int)batteryPct*100 + " %");
 		view.status.setText( (chargePlug != 0 ?  "Pluged : " + ( usbCharge ? "USB" : "AC" ) +  (isCharging ? ". Changing" : "No charging"): "No Pluged"));
 		view.info.setText("V: " + (float)batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)/100 + " volts. / T: " + (float)batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)/10 + "C");
 	}
 
 	public static class BatteryReceiver extends BroadcastReceiver
 	{
 		@Override
 		public void onReceive(Context context, Intent intent)
 		{
			instance.updateBatteryInfo(intent);
 		}
 	}
 }
