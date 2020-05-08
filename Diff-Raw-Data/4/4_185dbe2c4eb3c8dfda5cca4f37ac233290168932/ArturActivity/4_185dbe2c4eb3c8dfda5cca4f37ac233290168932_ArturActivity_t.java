 package com.guilhermegarnier.artur;
 
 import java.util.Calendar;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Handler;
 import android.view.View;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.TextView;
 
 public class ArturActivity extends Activity {
 	protected static final String SHARED_PREFS_NAME = "artur_preferences";
 	protected static final String BIRTH_DATE_SHARED_PREF = "birth_date";
 	protected static final String DEFAULT_BIRTH_DATE = "2012-" + Calendar.MAY + "-30";
 
 	protected TextView weeksLabel;
 	protected TextView percentageLabel;
 	protected TextView countdownLabel;
 	protected TextView birthDateLabel;
 	private Button pickDate;
 
 	protected CountDownTimer brewCountDownTimer;
 	private TimerTask timer;
 
 	private Calendar birthDate;
 	private float weeks;
 	private double percentage;
 	private String countdown;
 
 	static final int DATE_DIALOG_ID = 0;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		weeksLabel = (TextView) findViewById(R.id.weeks);
 		percentageLabel = (TextView) findViewById(R.id.percentage);
 		countdownLabel = (TextView) findViewById(R.id.countdown);
 		birthDateLabel = (TextView) findViewById(R.id.birthDate);
 		pickDate = (Button) findViewById(R.id.pickDate);
 
 		pickDate.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				showDialog(DATE_DIALOG_ID);
 			}
 		});
 
 		setDefaultBirthDate();
 		showBirthDate();
 		startTimer();
 	}
 
 	private void setDefaultBirthDate() {
 		SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
 		String[] birthDateFields = sharedPreferences.getString(BIRTH_DATE_SHARED_PREF, DEFAULT_BIRTH_DATE).split("-");
 		birthDate = Calendar.getInstance();
 		birthDate.set(Integer.valueOf(birthDateFields[0]), Integer.valueOf(birthDateFields[1]), Integer.valueOf(birthDateFields[2]), 0, 0, 0);
 	}
 
 	private void showBirthDate() {
 		birthDateLabel.setText(String.format("%02d/%02d/%04d",
 				birthDate.get(Calendar.DAY_OF_MONTH),
 				birthDate.get(Calendar.MONTH)+1,
 				birthDate.get(Calendar.YEAR)));
 	}
 
 	private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
 		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			birthDate.set(year, monthOfYear, dayOfMonth);

 			SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE).edit();
 			editor.putString(BIRTH_DATE_SHARED_PREF, String.format("%04d-%02d-%02d", birthDate.get(Calendar.YEAR), birthDate.get(Calendar.MONTH), birthDate.get(Calendar.DAY_OF_MONTH)));
 			editor.commit();
 
 			showBirthDate();
 			updateDisplay();
 		}
 	};
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DATE_DIALOG_ID:
 			return new DatePickerDialog(this, dateSetListener,
 					birthDate.get(Calendar.YEAR),
 					birthDate.get(Calendar.MONTH),
 					birthDate.get(Calendar.DAY_OF_MONTH));
 		}
 		return null;
 	}
 
 	private void calculate() {
 		final Calendar now = Calendar.getInstance();
 		float diff = birthDate.getTimeInMillis() - now.getTimeInMillis();
 		weeks = 40 - (diff / (1000 * 60 * 60 * 24 * 7));
 		percentage = weeks / 0.4;
 		calculateCountdown(diff);
 	}
 
 	private void calculateCountdown(float diff) {
 		double days = Math.floor(diff / (1000 * 60 * 60 * 24));
 		diff -= (days * 1000 * 60 * 60 * 24);
 		double hour = Math.floor(diff / (1000 * 60 * 60));
 		diff -= (hour * 1000 * 60 * 60);
 		double minute = Math.floor(diff / (1000 * 60));
 		diff -= (minute * 1000 * 60);
 		double second = Math.floor(diff / 1000);
 		countdown = String.format("%.0fd %02.0f:%02.0f:%02.0f", days, hour,
 				minute, second);
 	}
 
 	private void showValues() {
 		weeksLabel.setText(String.format("%.2f", weeks));
 		percentageLabel.setText(String.format("%.2f%%", percentage));
 		countdownLabel.setText(countdown);
 	}
 
 	private void updateDisplay() {
 		calculate();
 		showValues();
 	}
 
 	private void startTimer() {
 		final Handler handler = new Handler();
 		timer = new TimerTask() {
 			public void run() {
 				handler.post(new Runnable() {
 					public void run() {
 						updateDisplay();
 					}
 				});
 			}
 		};
 
 		Timer t = new Timer();
 		t.schedule(timer, 0, 1000);
 	}
 }
