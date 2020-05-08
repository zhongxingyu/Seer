 /*******************************************************************************
  * Copyright (c) 2012 LSIIT - Université de Strasbourg
  * Copyright (c) 2012 Erkan VALENTIN <erkan.valentin[at]unistra.fr>
  * http://www.senslab.info/
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 
 package com.wfbcl2.info;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.Timer;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ProgressBar;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 public class WifibotLab2Activity extends Activity implements OnClickListener, OnTouchListener, OnSeekBarChangeListener, OnCheckedChangeListener {
 
 	private WifibotCmdSender wcs = null;
 	private static String IP = "192.168.1.106";
 	private static int PORT = 15020;
 	private static int REFRESH_TIME = 100;
 	
 	private static int VOLTAGE_MAX = 18;
 	private static int VOLTAGE_LIMIT = 11;
 	
 	private static int SPEED_MAX = 510;
 	private static int SPEED_DEFAULT = 200;
 	
 	private static int IR_MAX = 255;
 	public static int IR_LIMIT = 60;
 	
 	private Timer timer = null;
 	public int voltage = 0;
 	public int current = 0;
 	public int speed = SPEED_DEFAULT;
 	public int irFl = 0;
 	public int irFr = 0;
 	public int irBl = 0;
 	public int irBr = 0;
 	public boolean onSecurity = false;
 
 	Handler handler = new Handler();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		Button btnForward = (Button) findViewById(R.id.btnForward);
 		btnForward.setOnTouchListener(this);
 
 		Button btnBackward = (Button) findViewById(R.id.btnBackward);
 		btnBackward.setOnTouchListener(this);
 
 		Button btnLeft = (Button) findViewById(R.id.btnLeft);
 		btnLeft.setOnTouchListener(this);
 
 		Button btnRight = (Button) findViewById(R.id.btnRight);
 		btnRight.setOnTouchListener(this);
 
 		Button btnRotate = (Button) findViewById(R.id.btnRotate);
 		btnRotate.setOnTouchListener(this);
 
 		ToggleButton btnConnected = (ToggleButton) findViewById(R.id.btnConnected);
 		btnConnected.setOnClickListener(this);
 
 		SeekBar sbSpeed = (SeekBar) findViewById(R.id.sbSpeed);
 		sbSpeed.setOnSeekBarChangeListener(this);
 		sbSpeed.setMax(SPEED_MAX);
 		sbSpeed.setProgress(speed);
 
 		TextView tvState = (TextView) findViewById(R.id.tvState);
 		tvState.setText("State: Disconnected");
 
 		TextView tvSpeed = (TextView) findViewById(R.id.tvSpeed);
 		tvSpeed.setText("Speed: " + speed);
 
 		TextView tvVoltage = (TextView) findViewById(R.id.tvVoltage);
 		tvVoltage.setText("Voltage: 0");
 
 		ProgressBar pgVoltage = (ProgressBar) findViewById(R.id.pgVoltage);
 		pgVoltage.setMax(VOLTAGE_MAX);
 
 		ProgressBar pgFR = (ProgressBar) findViewById(R.id.pgFR);
 		pgFR.setMax(IR_MAX);
 		
 		ProgressBar pgFL = (ProgressBar) findViewById(R.id.pgFL);
 		pgFL.setMax(IR_MAX);
 
 		ProgressBar pgBR = (ProgressBar) findViewById(R.id.pgBR);
 		pgBR.setMax(IR_MAX);
 		ProgressBar pgBL = (ProgressBar) findViewById(R.id.pgBL);
 		pgBL.setMax(IR_MAX);
 
 		CheckBox cbSecurity = (CheckBox) findViewById(R.id.cbSecurity);
 		cbSecurity.setOnCheckedChangeListener(this);
 
/*		btnConnected.setChecked(false);
 		btnForward.setEnabled(false);
 		btnBackward.setEnabled(false);
 		btnLeft.setEnabled(false);
 		btnRight.setEnabled(false);
 		btnRotate.setEnabled(false);
 		sbSpeed.setEnabled(false);
 		pgVoltage.setEnabled(false);
 		pgFR.setEnabled(false);
 		pgFL.setEnabled(false);
 		pgBR.setEnabled(false);
 		pgBL.setEnabled(false);
		cbSecurity.setEnabled(false);*/
 	}
 
 
 	@Override
 	public boolean onTouch(View elem, MotionEvent event) {
 
 		int action = event.getAction();
 
 		if(elem.getId() == R.id.btnForward) {
 			if (action == MotionEvent.ACTION_DOWN){
 				wcs.forward(speed);
 			}
 			else if (action == MotionEvent.ACTION_UP){
 				wcs.nothing();
 			}
 			else if (action == MotionEvent.ACTION_CANCEL){
 				wcs.nothing();
 			}
 		}
 
 		if(elem.getId() == R.id.btnBackward) {
 			if (action == MotionEvent.ACTION_DOWN){
 				wcs.backward(speed);
 			}
 			else if (action == MotionEvent.ACTION_UP){
 				wcs.nothing();
 			}
 			else if (action == MotionEvent.ACTION_CANCEL){
 				wcs.nothing();
 			}
 		}
 
 		if(elem.getId() == R.id.btnLeft) {
 			if (action == MotionEvent.ACTION_DOWN){
 				wcs.direction(speed, true, true);
 			}
 			else if (action == MotionEvent.ACTION_UP){
 				wcs.nothing();
 			}
 			else if (action == MotionEvent.ACTION_CANCEL){
 				wcs.nothing();
 			}
 		}
 
 		if(elem.getId() == R.id.btnRight) {
 			if (action == MotionEvent.ACTION_DOWN){
 				wcs.direction(speed, false, true);
 			}
 			else if (action == MotionEvent.ACTION_UP){
 				wcs.nothing();
 			}
 			else if (action == MotionEvent.ACTION_CANCEL){
 				wcs.nothing();
 			}
 		}
 
 		if(elem.getId() == R.id.btnRotate) {
 			if (action == MotionEvent.ACTION_DOWN){
 				wcs.rotate(speed, true);
 			}
 			else if (action == MotionEvent.ACTION_UP){
 				wcs.nothing();
 			}
 			else if (action == MotionEvent.ACTION_CANCEL){
 				wcs.nothing();
 			}
 		}
 
 		return false;
 	}
 
 	@Override
 	public void onClick(View v) {
 
 		Button btnForward = (Button) findViewById(R.id.btnForward);
 		Button btnBackward = (Button) findViewById(R.id.btnBackward);
 		Button btnLeft = (Button) findViewById(R.id.btnLeft);
 		Button btnRight = (Button) findViewById(R.id.btnRight);
 		Button btnRotate = (Button) findViewById(R.id.btnRotate);
 		ToggleButton btnConnected = (ToggleButton) findViewById(R.id.btnConnected);
 		TextView tvState = (TextView) findViewById(R.id.tvState);
 		SeekBar sbSpeed = (SeekBar) findViewById(R.id.sbSpeed);
 		TextView tvVoltage = (TextView) findViewById(R.id.tvVoltage);
 		ProgressBar pgVoltage = (ProgressBar) findViewById(R.id.pgVoltage);
 		ProgressBar pgFR = (ProgressBar) findViewById(R.id.pgFR);
 		ProgressBar pgFL = (ProgressBar) findViewById(R.id.pgFL);
 		ProgressBar pgBR = (ProgressBar) findViewById(R.id.pgBR);
 		ProgressBar pgBL = (ProgressBar) findViewById(R.id.pgBL);
 		CheckBox cbSecurity = (CheckBox) findViewById(R.id.cbSecurity);
 
 		if(v.getId() == R.id.btnConnected){
 
 			if(((ToggleButton) v).isChecked()) {
 
 				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
 				String ip = pref.getString("ip", WifibotLab2Activity.IP);
 
 				try {
 					Socket socket = new Socket();
 					socket.connect(new InetSocketAddress(ip, WifibotLab2Activity.PORT), 1000);
 
 					InputStream is = socket.getInputStream();
 					DataInputStream dis = new DataInputStream(is);
 
 					OutputStream out = socket.getOutputStream();
 					DataOutputStream dos = new DataOutputStream(out);
 
 					timer = new Timer();
 					wcs = new WifibotCmdSender(this);
 					wcs.configure(dos,dis);
 					timer.scheduleAtFixedRate(wcs, 0, WifibotLab2Activity.REFRESH_TIME);
 
 					btnForward.setEnabled(true);
 					btnBackward.setEnabled(true);
 					btnLeft.setEnabled(true);
 					btnRight.setEnabled(true);
 					btnRotate.setEnabled(true);
 					sbSpeed.setEnabled(true);
 					pgVoltage.setEnabled(true);
 					pgFR.setEnabled(true);
 					pgFL.setEnabled(true);
 					pgBR.setEnabled(true);
 					pgBL.setEnabled(true);
 					cbSecurity.setEnabled(true);
 
 					tvState.setText("State: Connected");
 
 				}
 				catch (Exception e) {
 					tvState.setText("State: " + e.getMessage());
 					btnConnected.setChecked(false);
 				}
 			}
 			else {
 				btnForward.setEnabled(false);
 				btnBackward.setEnabled(false);
 				btnLeft.setEnabled(false);
 				btnRight.setEnabled(false);
 				btnRotate.setEnabled(false);
 				sbSpeed.setEnabled(false);
 				tvState.setText("State: Disconnected");
 				pgVoltage.setEnabled(false);
 				pgVoltage.setProgress(0);
 				pgFR.setEnabled(false);
 				pgFR.setProgress(0);
 				pgFL.setEnabled(false);
 				pgFL.setProgress(0);
 				pgBR.setEnabled(false);
 				pgBR.setProgress(0);
 				pgBL.setEnabled(false);
 				pgBL.setProgress(0);
 				cbSecurity.setEnabled(false);
 				tvVoltage.setText("Voltage: 0");
 				if(timer != null)
 					timer.cancel();
 			}
 		}
 	}
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}   
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.iSettings:
 			Intent i = new Intent(this, Preferences.class);
 			startActivity(i);
 			return true;
 		case R.id.iAbout:
 			try {
 				PackageInfo manager = getPackageManager().getPackageInfo(getPackageName(), 0);
 				Toast toast = Toast.makeText(
 						this, this.getString(R.string.app_name) + " " + manager.versionName , 1000);
 				toast.show();
 			} catch (Exception e) {
 				//
 			}
 
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		if(timer != null)
 			timer.cancel();
 	}
 
 
 	@Override
 	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
 	}
 
 
 	@Override
 	public void onStartTrackingTouch(SeekBar seekBar) {
 	}
 
 
 	@Override
 	public void onStopTrackingTouch(SeekBar seekBar) {
 		speed = seekBar.getProgress();
 		TextView tvSpeed = (TextView) findViewById(R.id.tvSpeed);
 		tvSpeed.setText("Speed: " + seekBar.getProgress());
 	}
 
 
 	/**
 	 * 
 	 */
 	public Runnable updateUI = new Runnable() {
 		public void run() {
 			TextView tvVoltage = (TextView) findViewById(R.id.tvVoltage);
 			float voltage_value = (float) (WifibotLab2Activity.this.voltage/10.0);
 			tvVoltage.setText("Voltage: " + voltage_value);
 			
 			((TextView) findViewById(R.id.tvCurrent)).setText(WifibotLab2Activity.this.current*100 + "mA");
 
 			ProgressBar pgVoltage = (ProgressBar) findViewById(R.id.pgVoltage);
 			pgVoltage.setProgress((int)voltage_value);
 
 			ProgressBar pgFR = (ProgressBar) findViewById(R.id.pgFR);
 			pgFR.setProgress((int)irFr);
 
 			ProgressBar pgFL = (ProgressBar) findViewById(R.id.pgFL);
 			pgFL.setProgress((int)irFl);
 
 			ProgressBar pgBR = (ProgressBar) findViewById(R.id.pgBR);
 			pgBR.setProgress((int)irBr);
 
 			ProgressBar pgBL = (ProgressBar) findViewById(R.id.pgBL);
 			pgBL.setProgress((int)irBl);
 			
 			//IR label color
 			if(irFr > IR_LIMIT) {
 				((TextView) findViewById(R.id.tvFR)).setTextColor(Color.RED);
 			}
 			else {
 				((TextView) findViewById(R.id.tvFR)).setTextColor(Color.WHITE);
 			}
 			
 			if(irFl > IR_LIMIT) {
 				((TextView) findViewById(R.id.tvFL)).setTextColor(Color.RED);
 			}
 			else {
 				((TextView) findViewById(R.id.tvFL)).setTextColor(Color.WHITE);
 			}
 			
 			if(irBr > IR_LIMIT) {
 				((TextView) findViewById(R.id.tvBR)).setTextColor(Color.RED);
 			}
 			else {
 				((TextView) findViewById(R.id.tvBR)).setTextColor(Color.WHITE);
 			}
 			
 			if(irBl > IR_LIMIT) {
 				((TextView) findViewById(R.id.tvBL)).setTextColor(Color.RED);
 			}
 			else {
 				((TextView) findViewById(R.id.tvBL)).setTextColor(Color.WHITE);
 			}
 			
 			//voltage limit
 			if(voltage < VOLTAGE_LIMIT) {
 				((TextView) findViewById(R.id.tvVoltage)).setTextColor(Color.RED);
 			}
 			else {
 				((TextView) findViewById(R.id.tvVoltage)).setTextColor(Color.WHITE);
 			}
 		}
 	};
 
 	@Override
 	public void onCheckedChanged(CompoundButton cb, boolean state) {
 
 		if(cb.getId() == R.id.cbSecurity) {
 			this.onSecurity = state;
 		}
 	}
 
 }
