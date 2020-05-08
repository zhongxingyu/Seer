 package uk.ac.bham.cs.stroppykettle_v2;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 import at.abraxas.amarino.Amarino;
 import at.abraxas.amarino.AmarinoIntent;
 
 public class MainActivity extends Activity implements OnClickListener {
 
 	private static final String DEVICE_ADDRESS = "00:06:66:08:17:53";
 	private WeightReceiver arduinoReceiver = new WeightReceiver();
	ToggleButton mPowerButton;
 	private TextView mWeightText;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		mPowerButton = (ToggleButton) findViewById(R.id.powerButton);
 		mPowerButton.setOnClickListener(this);
 		mWeightText = (TextView) findViewById(R.id.weightText);

 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		registerReceiver(arduinoReceiver, new IntentFilter(
 				AmarinoIntent.ACTION_RECEIVED));
 		Amarino.connect(this, DEVICE_ADDRESS);
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		Amarino.disconnect(this, DEVICE_ADDRESS);
 		unregisterReceiver(arduinoReceiver);
 	}
 
 	public class WeightReceiver extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String data = null;
 			data = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);
 			if (data != null)
 				mWeightText.setText(data);
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (mPowerButton.isChecked()) {
 			Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'p', 1);
 		} else {
 			Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'p', 0);
 		}
 	}
 
 }
