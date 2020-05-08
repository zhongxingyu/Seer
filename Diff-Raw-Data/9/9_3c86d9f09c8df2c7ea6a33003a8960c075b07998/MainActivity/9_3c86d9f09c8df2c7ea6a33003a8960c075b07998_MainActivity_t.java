 package nl.uva.servodingestweepuntnul;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.RadioButton;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 public class MainActivity extends Activity implements OnTouchListener,
 		OnSeekBarChangeListener, OnCheckedChangeListener {
 	private AdkPort mbedPort;
 	private RadioButton radioButton1;
 	private RadioButton radioButton2;
 	private Button left;
 	private Button right;
 	
 	private boolean isLocal;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		isLocal = true;
 		
 		setContentView(R.layout.activity_main);
 		left = (Button) findViewById(R.id.left);
 		right = (Button) findViewById(R.id.right);
 		left.setOnTouchListener(this);
 		right.setOnTouchListener(this);
 		
 		SeekBar seekbar1 = (SeekBar) findViewById(R.id.seekBar1);
 		seekbar1.setOnSeekBarChangeListener(this);
 		
 		radioButton1 = (RadioButton) findViewById(R.id.radio0);
 		radioButton2 = (RadioButton) findViewById(R.id.radio1);
 		radioButton1.setOnCheckedChangeListener(this);
 		radioButton2.setOnCheckedChangeListener(this);
 		
 		try {
 			mbedPort = new AdkPort(getBaseContext());
 		} catch(IOException e) {
 			radioButton1.setChecked(false);
 			radioButton2.setChecked(true);
 			isLocal = false;
 			Log.i("SD2", "No mbed? ", e);
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onTouch(View v, MotionEvent arg1) {
 		if(arg1.getAction() == MotionEvent.ACTION_DOWN)
 			((Button) v).setBackgroundColor(Color.GRAY);
 		else {
 			((Button) v).setBackgroundColor(Color.WHITE);
 			SeekBar progressBar = (SeekBar) findViewById(R.id.seekBar1);
 			int progress = progressBar.getProgress();
 			
 			if(left.getId() == v.getId()) {
 				// left button pressed
				if(progress > 0)
					progress--;
 			} else if(right.getId() == v.getId()) {
 				// right button pressed
				if(progress < 100)
					progress++;
 			}
 			// Set the progress of the seekBar
 			progressBar.setProgress(progress);
 			
 			// Set the TextView to the new value
 			TextView value = (TextView) findViewById(R.id.value);
 			value.setText("" + progress / 10.0);
 		}
 		return true;
 	}
 	
 	@Override
 	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
 		// Update the value of the TextView to the new value of the seekBar
 		TextView value = (TextView) findViewById(R.id.value);
 		value.setText("" + arg1 / 10.0);
 		byte[] buf = new byte[1];
 		buf[0] = (byte) arg0.getProgress();
		Log.i("Send","Sending value: " + arg1);
 		mbedPort.sendBytes(buf);
 	}
 	
 	@Override
 	public void onStartTrackingTouch(SeekBar arg0) {
 	}
 	
 	@Override
 	public void onStopTrackingTouch(SeekBar arg0) {
 	}
 	
 	@Override
 	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
 		if(radioButton1.getId() == arg0.getId()) {
 			radioButton2.setChecked(!arg1);
 			isLocal = arg1;
 		} else if(radioButton2.getId() == arg0.getId()) {
 			radioButton1.setChecked(!arg1);
 			isLocal = !arg1;
 		}
 	}
 }
