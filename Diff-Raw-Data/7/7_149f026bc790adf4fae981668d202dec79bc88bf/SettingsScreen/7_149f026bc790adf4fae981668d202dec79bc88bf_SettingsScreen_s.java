 package com.flexymind.labirynth.screens.settings;
 
 import com.flexymind.labirynth.R;
 import com.flexymind.labirynth.storage.Settings;
 import com.flexymind.labirynth.storage.SettingsStorage;
 
 import android.app.Activity;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.SeekBar;
 import android.widget.TextView;
 
 public class SettingsScreen extends Activity implements OnClickListener{
 
 	private final int MAXSENS = 100;
 	
 	private static SensorManager sensMan = null;
 	
 	// Для калибровки акселерометра
 	private static int imeasure = 0;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.settings);
 		
 		sensMan = (SensorManager) this.getSystemService(SENSOR_SERVICE);
 		
 		addButtons();
 	}
 	
 	private class BarChangeListener implements SeekBar.OnSeekBarChangeListener {
 
 		public void onProgressChanged(SeekBar seekBar, int progress,
 				boolean fromUser) {
 			if (progress > 0){
 				SettingsStorage.saveSensivity(progress / 10f);
 			}else{
 				SettingsStorage.saveSensivity(1 / 10f);
 			}
 		}
 
 		public void onStartTrackingTouch(SeekBar seekBar) {
 			
 		}
 
 		public void onStopTrackingTouch(SeekBar seekBar) {
 			
 		}
 		
 	}
 	
 	public void addButtons() {
 
 		Button calibrButton = (Button) findViewById(R.id.accelCalibr);
 		SeekBar bar = (SeekBar)findViewById(R.id.seekBar1);
 		TextView text = (TextView)findViewById(R.id.sensText);
 		
 		bar.setMax(MAXSENS);
 		bar.setProgress((int)SettingsStorage.getSensivity() * 10);
 		
 		bar.setOnSeekBarChangeListener(new BarChangeListener());
 		calibrButton.setOnClickListener(this);
 		
 		LinearLayout lay = (LinearLayout)findViewById(R.id.settButtLayout);
 		
		ViewGroup.LayoutParams buttonparams = (ViewGroup.LayoutParams)calibrButton.getLayoutParams();
 		ViewGroup.LayoutParams barparams = (ViewGroup.LayoutParams)bar.getLayoutParams();
 		ViewGroup.LayoutParams textparams = (ViewGroup.LayoutParams)text.getLayoutParams();
 		
 		if (Settings.getAutoScale()) {
 			
 			buttonparams.height = (int)(Settings.getScaleFactorY() * buttonparams.height);
 			buttonparams.width  = (int)(Settings.getScaleFactorX() * buttonparams.width);
 			
 			barparams.height = (int)(Settings.getScaleFactorY() * barparams.height);
 			barparams.width  = (int)(Settings.getScaleFactorX() * barparams.width);
 			
 			textparams.height = (int)(Settings.getScaleFactorY() * textparams.height);
 			textparams.width  = (int)(Settings.getScaleFactorX() * textparams.width);
 			
 			RelativeLayout parent = (RelativeLayout)lay.getParent();
 			RelativeLayout.LayoutParams rlparams = (RelativeLayout.LayoutParams)lay.getLayoutParams();
 			rlparams.topMargin = (int)(Settings.getScaleFactorY() * rlparams.topMargin);
 			rlparams.bottomMargin = (int)(Settings.getScaleFactorY() * rlparams.bottomMargin);
 			rlparams.leftMargin = (int)(Settings.getScaleFactorX() * rlparams.leftMargin);
 			rlparams.rightMargin = (int)(Settings.getScaleFactorX() * rlparams.rightMargin);
 			rlparams.height = (int)(Settings.getScaleFactorY() * rlparams.height);
 			rlparams.width = (int)(Settings.getScaleFactorX() * rlparams.width);
 			
 			lay.removeAllViews();
 			
 			lay.addView(text, textparams);
 			lay.addView(bar, barparams);
 			lay.addView(calibrButton, buttonparams);
 			
 			parent.removeAllViews();
 			parent.addView(lay, rlparams);
 		}
 		
 	}
 	
 	public static final SensorEventListener accelerometerListener = new SensorEventListener() {
 		
 		public void onSensorChanged(SensorEvent event) {
 			if (imeasure++ > 10){
 				SettingsStorage.saveAcellPosition(event.values);
 				sensMan.unregisterListener(accelerometerListener);
 				imeasure = 0;
 			}
 		}
 		
 		public void onAccuracyChanged(Sensor sensor, int accuracy) { }
 
 	};
 	
 	public void onClick(View arg0) {
 		switch(arg0.getId()){
 		case R.id.accelCalibr:
 			sensMan.registerListener(accelerometerListener, sensMan.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
 			break;
 		}
 		
 	}
 	
 }
