 package yoloswagswag.swe_app;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
     /**
      * @class pollActivity
      * @brief Diese Klasse wird bei Aktivierung eines Alarms geöffnet und dient zur Usereingabe der
      * Kontakte.
      * @file pollActivity.java
      */
 
 public class pollActivity extends Activity {
 
     public static final String LAST_TIME_STORAGE = "lastTimeStorage";
     private SharedPreferences lastTimeSett;
     private Calendar currentDay;
     private int nextSlot;
     private MediaPlayer mediaPlayer;
     private CountDownTimer soundTimer;
 
     /**
      * @brief Erstellung der Umfrage bei Alarm
      * @param Bundle savedInstanceState
      */
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.poll_screen);
 
         playSound(this, getAlarmUri());
         checkForLastUserAction();
         // liest letzte Abfragezeit aus shared preferences
         lastTimeSett = getSharedPreferences(LAST_TIME_STORAGE, 0);
         // liest nächste gesetzte Abfragezeit aus shared preferences
         SharedPreferences selectedTimesStorage = getSharedPreferences("selectedTimesStorage", 0);
         currentDay = new GregorianCalendar();
         nextSlot =0;
         int slotHour = selectedTimesStorage.getInt("day" + (currentDay.get(Calendar.DAY_OF_WEEK) - 1) + "slot" + nextSlot, 0);
         // Bestimmt die nächste Alarmzeit aus Shared Preferences, wenn nextSlot=4 ist Alarm am nächsten Tag
         while(slotHour<= currentDay.get(Calendar.HOUR_OF_DAY)&& nextSlot <4)
         {
             nextSlot++;
             if(nextSlot ==4){
                slotHour= selectedTimesStorage.getInt("day" + (currentDay.get(Calendar.DAY_OF_WEEK)==7 ?"1" : currentDay.get(Calendar.DAY_OF_WEEK)) + "slot" + 0, 0);
             }else
                 slotHour= selectedTimesStorage.getInt("day" + (currentDay.get(Calendar.DAY_OF_WEEK) - 1) + "slot" + nextSlot, 0);
         }
         // Wandelt Alarmstunde in konkrete Zeit um
         Calendar nextAlarmTime = new GregorianCalendar();
         nextAlarmTime.set(Calendar.HOUR_OF_DAY, slotHour);
         nextAlarmTime.set(Calendar.MINUTE, 0);
         nextAlarmTime.set(Calendar.SECOND, 0);
         // Nächster Alarm ist Morgen --> Tag um 1 erhöhen
         if(nextSlot==4) nextAlarmTime.add(Calendar.DATE, 1);
 
         updatePastAlarmsStorage();
         if(nextAlarmTime.compareTo(currentDay)==1) startNewAlarm(nextAlarmTime);
 
         TextView pollText = (TextView) findViewById(R.id.pollText);
 
         int lastHour = lastTimeSett.getInt("lastHour",25);
 
         // Erste Abfrage am nächsten Tag (Frage nach gestrigen Signal)
         if(lastHour != 25){
             pollText.setText(getString(R.string.pollText)+ ((currentDay.get(Calendar.HOUR_OF_DAY)<lastHour) ? " gestrigen " : " ")
                     + "Signal um "+lastTimeSett.getInt("lastHour",0)+":00 Uhr Kontakt?");
         }
 
     }
 
 
 
     /**
      *@brief Boolean, zur Überprüfung ob User Anfrage beantwortet hat, wird gesetzt.
      * @param boolean hasUserAnswered
      */
     private void setHasUserAnsweredToPreferences(boolean hasUserAnswered){
         SharedPreferences pastAlarmStorage=getSharedPreferences("pastAlarmsStorage",0);
         SharedPreferences.Editor editor = pastAlarmStorage.edit();
         editor.putBoolean("userHasAnswered", hasUserAnswered);
         editor.commit();
     }
     /**
      * @brief Update des Speichers für letzten Alarm.
      */
     private void updatePastAlarmsStorage(){
         SharedPreferences pastAlarmStorage=getSharedPreferences("pastAlarmsStorage",0);
         SharedPreferences.Editor editor = pastAlarmStorage.edit();
         editor.putInt("hour", currentDay.get(Calendar.HOUR_OF_DAY));
         editor.putInt("day", currentDay.get(Calendar.DAY_OF_WEEK));
         editor.putInt("day_of_month", currentDay.get(Calendar.DAY_OF_MONTH));
         editor.putInt("month", currentDay.get(Calendar.MONTH));
         editor.putInt("year", currentDay.get(Calendar.YEAR));
         editor.putInt("slot", nextSlot - 1);
         editor.putBoolean("userHasAnswered", false);
         editor.putInt("alarmID", pastAlarmStorage.getInt("alarmID",0)+1);
         editor.commit();
 }
     /**
       * @brief Wenn User keine Angabe im letzten Alarm gemacht hat, wird Fehlerzeile in .csv geschrieben.
       */
     private void checkForLastUserAction(){
         SharedPreferences pastAlarmStorage=getSharedPreferences("pastAlarmsStorage", 0);
         if(!pastAlarmStorage.getBoolean("userHasAnswered",true)){
             Calendar lastAlarmDate=new GregorianCalendar(pastAlarmStorage.getInt("year",2013),
                     pastAlarmStorage.getInt("month",1),pastAlarmStorage.getInt("day_of_month",1),
                     pastAlarmStorage.getInt("hour",12),0);
             writeCancelLineInCsv(lastAlarmDate);
         }
     }
     /**
      * @brief Sound wird abgespielt
      * @param Context context
      * @param Uri alert
       */
     private void playSound(Context context, Uri alert){
         mediaPlayer = new MediaPlayer();
         try {
             mediaPlayer.setDataSource(context, alert);
             final AudioManager audioManager = (AudioManager) context
                     .getSystemService(Context.AUDIO_SERVICE);
             if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                 mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                 // Lautstärkeregelung
                 if(audioManager.getRingerMode()==audioManager.RINGER_MODE_SILENT ||
                         audioManager.getRingerMode()==audioManager.RINGER_MODE_VIBRATE){
                     // Lautstärke im Lautlos-Modus bzw. nur Vibrationsmodus
                     mediaPlayer.setVolume((float)Math.log(0),(float)Math.log(0));
                 }
                 else{
                     // Lautstärke bei nicht im Lautlos-Modus
                     mediaPlayer.setVolume((float)Math.log(3),(float)Math.log(3));
                 }
                 mediaPlayer.setLooping(false);
                 mediaPlayer.prepare();
                 mediaPlayer.start();
             }
         } catch (IOException e) {
             Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
         }
         /**
          * @brief startet/ beendet Sound und startet Vibration
          */
         soundTimer = new CountDownTimer(10000, 1000) {
             /**
              * @brief startet Vibration und steuert Sound
              * @param long l
              */
 
             @Override
             public void onTick(long l) {
                 Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                 v.vibrate(200);
             }
 
             /**
              * @brief beendet Sound
              */
             @Override
             public void onFinish() {
                 stopSound(mediaPlayer);
             }
         };
         soundTimer.start();
     }
     /**
      * @brief Alarmton wird bestimmt
       */
     private Uri getAlarmUri() {
         Uri alert = RingtoneManager
                 .getDefaultUri(RingtoneManager.TYPE_ALARM);
         if (alert == null) {
             alert = RingtoneManager
                     .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
             if (alert == null) {
                 alert = RingtoneManager
                         .getDefaultUri(RingtoneManager.TYPE_RINGTONE);
             }
         }
         return alert;
     }
 
     /**
      * @brief stoppt gegebenen Timer
      * @param CountDownTimer timer
       */
     private void stopTimer(CountDownTimer timer){
         if (timer != null) {
             timer.cancel();
             timer = null;
         }
     }
     /**
      * @brief stoppt Sound
      * @param MediaPlayer player
       */
     private void stopSound(MediaPlayer player){
         try {
             if (player != null) {
                 if (player.isPlaying()) {
                     player.stop();
                 }
                 player.release();
                 player = null;
             }
         } catch (IllegalStateException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * @brief neuer Alarm wird initiert
      * @param Calender alarmTime
       */
     public void startNewAlarm(Calendar alarmTime){
         SharedPreferences pastAlarmStorage=getSharedPreferences("pastAlarmsStorage",0);
         Intent intent = new Intent(this, pollActivity.class);
         PendingIntent pendingIntent = PendingIntent.getActivity(this, pastAlarmStorage.getInt("alarmID",0), intent, PendingIntent.FLAG_CANCEL_CURRENT);
 
         AlarmManager alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
         alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);
     }
     /**
      * @brief Update des lastTimeStorage in Shared Preferences
       */
     private void setLastHourAndMinuteToPreferences(){
         SharedPreferences.Editor editor = lastTimeSett.edit();
         editor.putInt("lastHour", currentDay.get(Calendar.HOUR_OF_DAY));
         editor.putInt("lastMinute", currentDay.get(Calendar.MINUTE));
         editor.commit();
     }
 
     /**
      * @brief schreibt Abbruchzeile in .csv
      * @param Calendar date
       */
     private void writeCancelLineInCsv(Calendar date){
         SharedPreferences userCodeStorage=getSharedPreferences("userCodeStorage",0);
         String code = userCodeStorage.getString("userCode",null);
         try {
             File dir = new File(Environment.getExternalStorageDirectory(),"PsychoTest");
             File f = new File(dir, code+".csv");
             FileWriter writer = new FileWriter(f ,true);
             writer.write(code+";"
                     + date.get(Calendar.DAY_OF_MONTH)+"."+ (date.get(Calendar.MONTH)+1)+"."+ date.get(Calendar.YEAR)+";"
                     + date.get(Calendar.HOUR_OF_DAY)+":00" +
                     ";-77;1;-77;-77;-77\n");
             writer.flush();
             writer.close();
             giveUserFeedback();
         } catch (IOException e){
             Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
         }
     }
 
     /**
      * @brief Toast erscheint mit UserRückmeldung
       */
     private void giveUserFeedback(){
         Toast.makeText(this, "Ihre Eingabe war erfolgreich.", Toast.LENGTH_LONG).show();
     }
 
     /**
      * @brief Betätigung OK-Button -> testet ob Eingabezeit möglich ist im akuellen Zeitraum
      * Wenn Eingabezeit möglich werden Daten in .csv gespeichert. Bei zu größer Zeiteingabe kommt
      * Rückmeldung an User.
      * @param View view
       */
     public void okPoll(View view){
         stopTimer(soundTimer);
         stopSound(mediaPlayer);
         setHasUserAnsweredToPreferences(true);
         EditText numberText = (EditText) findViewById(R.id.pollNrEdit);
         EditText hourText = (EditText) findViewById(R.id.pollHourEdit);
         EditText minuteText = (EditText) findViewById(R.id.pollMinuteEdit);
 
         if(numberText.getText().toString() != "" && hourText.getText().toString() != "" && minuteText.getText().toString() != ""){
 
             // maximal mögliche Zeiteingabe berechnen
             int minDiff = currentDay.get(Calendar.MINUTE)-lastTimeSett.getInt("lastMinute", 0) + (currentDay.get(Calendar.HOUR_OF_DAY)-lastTimeSett.getInt("lastHour",0))*60;
             if(minDiff<0) minDiff=24*60-lastTimeSett.getInt("lastMinute",0)-lastTimeSett.getInt("lastHour",0)+currentDay.get(Calendar.MINUTE)+ currentDay.get(Calendar.HOUR_OF_DAY);
             int inputTime = (Integer.parseInt(hourText.getText().toString()))*60 + Integer.parseInt(minuteText.getText().toString());
 
             // Eingabe von möglichen Zeit Speicherung der Daten
             if(inputTime <= minDiff){
                 SharedPreferences userCodeStorage=getSharedPreferences("userCodeStorage",0);
                 String code = userCodeStorage.getString("userCode",null);
                 Calendar updatedCurrentDay = new GregorianCalendar();
 
                 // schreibt Eingabedaten in .csv
                 try {
                     File dir = new File(Environment.getExternalStorageDirectory(),"PsychoTest");
                     File f = new File(dir, code+".csv");
                     FileWriter writer = new FileWriter(f ,true);
                     writer.write(code+";"
                             +updatedCurrentDay.get(Calendar.DAY_OF_MONTH)+"."+(updatedCurrentDay.get(Calendar.MONTH)+1)+"."+updatedCurrentDay.get(Calendar.YEAR)+";"
                             +currentDay.get(Calendar.HOUR_OF_DAY)+":00;"
                             +updatedCurrentDay.get(Calendar.HOUR_OF_DAY)+":"+((updatedCurrentDay.get(Calendar.MINUTE)<10) ? "0"+updatedCurrentDay.get(Calendar.MINUTE) : updatedCurrentDay.get(Calendar.MINUTE))+";"
                             +"0;"
                             +numberText.getText()+";"
                             +hourText.getText()+";"
                             +minuteText.getText()+"\n");
                     writer.flush();
                     writer.close();
                     giveUserFeedback();
                 } catch (IOException e){
                     Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
                 }
 
                 setLastHourAndMinuteToPreferences();
 
                 if (!this.isTaskRoot()){
                     this.finish();
                 } else {
                     startActivity(new Intent(this, chillActivity.class));
                     this.finish();
                 }
             } else {
                 // Zeiteingabe zu groß - Meldung
                 Toast.makeText(this, "So viel Zeit ist seit der letzten Umfrage nicht vergangen!", Toast.LENGTH_LONG).show();
             }
         } else {
             Toast.makeText(this, "Alle Felder müssen ausgefüllt werden!", Toast.LENGTH_LONG).show();
         }
     }
 
     /**
      * @brief Betätigung Abbruch-Button stoppt Sound und setzt Abfrage als beantwortet
      * @param View view
       */
     public void cancelPoll(View view){
         stopTimer(soundTimer);
         stopSound(mediaPlayer);
 
         setHasUserAnsweredToPreferences(true);
         writeCancelLineInCsv(currentDay);
 
         setLastHourAndMinuteToPreferences();
 
         this.finish();
     }
 }
