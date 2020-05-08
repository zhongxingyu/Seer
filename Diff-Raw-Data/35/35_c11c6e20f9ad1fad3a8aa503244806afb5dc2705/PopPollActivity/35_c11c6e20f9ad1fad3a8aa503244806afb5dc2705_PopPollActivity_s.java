 package de.Psychologie.socialintelligence;
 
 import java.util.Calendar;
import java.util.Date;
 
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 
 import android.app.Activity;
import android.content.Intent;
 import android.content.SharedPreferences;
import android.text.Editable;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TimePicker;
 
 public class PopPollActivity extends Activity {
 	
 	Button snooze_button;
 	Button ok_button;
 	Button cancel_button;
 	private TimePicker time;
 	private EditText count;
 	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_pop_poll);
 		
 		
 		// Datenbank Verbindung aufbauen
 		final SQLHandler db = new SQLHandler(PopPollActivity.this);
 		
 		// App startet -> hinterlegten Klingelton abspielen
 		// Meldung etc. Wenn Handy gesperrt �ffnet sich zwar die App, aber User bekommt nix von mit :-)
 		
		 final Alarm pollAlarm = new Alarm(this);
 	
 		snooze_button = (Button) findViewById(R.id.snooze_button);
 		ok_button=(Button) findViewById(R.id.ok_button);
 		cancel_button=(Button) findViewById(R.id.cancel_button);
 		
 		
 		// Wenn Snooze gedr�ck wird
 		snooze_button.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				//Snoozezeit aus den Settings auslesen, sonst 5 Minuten
 				String time= prefs.getString("Sleeptime", "5 Minuten");
 				int snoozetime = Integer.parseInt(time);
 				pollAlarm.setSnooze(snoozetime);
 				db.setSnoozeActiv(true);
 				
 			}
 		});
 		
 		ok_button.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				//Gesamtdauer der Kontakte
 				int hour = time.getCurrentHour();
 				int minute = time.getCurrentMinute();
 				//Anzahl der Kontakte
 				int contacts = Integer.parseInt(count.getText().toString());
 				Calendar cal = Calendar.getInstance();
 				//Zeitpunkt der Antwort
				String answerTime = cal.HOUR+":"+cal.MINUTE;
 				
 				//Datum
				String date = cal.DAY_OF_MONTH+"."+cal.MONTH+"."+cal.YEAR;
 				//Alarmzeit
 				String alarmTime=pollAlarm.currentAlarmTime;
 				String lastAlarmTime = cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+":00";
 				//nächsten Alarm setzen
 				pollAlarm.setNextAlarm();
 				db.setSnoozeActiv(false);
 				db.setLastAlarm(lastAlarmTime);
 				db.setPollEntry(date, alarmTime, answerTime, false, contacts, hour, minute);
 				
 				//if abfrage fehlt, ob nicht mehr Stunden vom letzten Alarmpunkt ausgewält wurden
 				
 			}
 		});
 		
 		cancel_button.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Calendar cal = Calendar.getInstance();
				String date = cal.DAY_OF_MONTH+"."+cal.MONTH+"."+cal.YEAR;
 				String alarmTime=pollAlarm.currentAlarmTime;
 
 				pollAlarm.setNextAlarm();
 				db.setSnoozeActiv(false);
 				//Werte in die DB eintragen
 				db.setPollEntry(date, alarmTime);
 				
 				
 			}
 		});
 		
 		
 		
 		// Wenn OK gedr�ckt
 		// letzten Alarm (ist dieser momentane Alarm) setzen
 		// Calendar cal = Calendar.getInstance();
 		// String lastAlarmTime = cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+":00";
 		// db.setLastAlarm(lastAlarmTime);
 		//db.setPollEntry(date, alarmTime, answerTime, abort, contacts, hour, minute)
 		
 		// Wenn Abbrechen gedr�ckt
 		//db.setPollEntry(date, alarmTime)
 		
 	}
 	
 	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.pop_poll, menu);
 		return true;
 	}
 
 }
