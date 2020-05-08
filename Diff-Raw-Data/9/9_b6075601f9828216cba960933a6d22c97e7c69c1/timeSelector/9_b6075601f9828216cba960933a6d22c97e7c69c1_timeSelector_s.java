 package yoloswagswag.swe_app;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 
     /**
      * @class timeSelector
      * @brief Dies ist der Zeitauswahlbildschirm. Dem User wird die Konfiguration der Alarmzeiten
      * ermöglicht.
      * @file timeSelector.java
      */
 
 public class timeSelector extends Activity {
 
     // Zeitauswahlbildschirm
 
     // Speicher für ausgewählte Zeiten
     public static final String SELECTED_TIMES_STORAGE = "selectedTimesStorage";
     public static final String PAST_ALARMS_STORAGE = "pastAlarmsStorage";
 
     // Speicher für ausgewählte Timeslots
     int[][] timeSelector = new int[7][4];
 
     // heutiges Datum
     Calendar currentDate = new GregorianCalendar();
 
     // beim Öffnen ausgewählter Tag immer Montag
     int selectedDay = Calendar.MONDAY;
 
     Button[] timeButtons = new Button[15];
     Button[] weekButtons = new Button[7];
 
     /**
      * @brief Zeitauswahlbildschirm
      * @param Bundle savedInstanceState
      */
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.time_selector);
 
         SharedPreferences selectedTimesSett = getSharedPreferences(SELECTED_TIMES_STORAGE, 0);
 
         // Zuordnung der Buttons
         timeButtons[0] = (Button) findViewById(R.id.button09);
         timeButtons[1] = (Button) findViewById(R.id.button10);
         timeButtons[2] = (Button) findViewById(R.id.button11);
         timeButtons[3] = (Button) findViewById(R.id.button12);
         timeButtons[4] = (Button) findViewById(R.id.button13);
         timeButtons[5] = (Button) findViewById(R.id.button14);
         timeButtons[6] = (Button) findViewById(R.id.button15);
         timeButtons[7] = (Button) findViewById(R.id.button16);
         timeButtons[8] = (Button) findViewById(R.id.button17);
         timeButtons[9] = (Button) findViewById(R.id.button18);
         timeButtons[10] = (Button) findViewById(R.id.button19);
         timeButtons[11] = (Button) findViewById(R.id.button20);
         timeButtons[12] = (Button) findViewById(R.id.button21);
         timeButtons[13] = (Button) findViewById(R.id.button22);
         timeButtons[14] = (Button) findViewById(R.id.button23);
 
         weekButtons[0] = (Button) findViewById(R.id.buttonSun);
         weekButtons[1] = (Button) findViewById(R.id.buttonMon);
         weekButtons[2] = (Button) findViewById(R.id.buttonTue);
         weekButtons[3] = (Button) findViewById(R.id.buttonWed);
         weekButtons[4] = (Button) findViewById(R.id.buttonThu);
         weekButtons[5] = (Button) findViewById(R.id.buttonFri);
         weekButtons[6] = (Button) findViewById(R.id.buttonSat);
 
         // Datum, fuer den aktuell ausgewaehlten tag
         Calendar textDay = (Calendar) currentDate.clone();
         // Text, der ausgewaehltes Datum zeigt
         TextView date = (TextView) findViewById(R.id.chosenDate);
 
         // geht so lange einen Tag weiter, bis der gewaehlte Tag erreicht wird
         while(textDay.get(Calendar.DAY_OF_WEEK)!=selectedDay){
             textDay.roll(Calendar.DAY_OF_MONTH,true);
         }
 
         // schreibt des gewaehlte Datum
         date.setText(textDay.get(Calendar.DAY_OF_MONTH)+"."+(textDay.get(Calendar.MONTH)+1)+".");
 
         if(selectedTimesSett.contains("day6slot3")){
             // wenn es eine alte Zeitauswahl gibt, wird sie geladen
             for (int i=0;i<timeSelector.length;i++){
                 for (int j=0;j<timeSelector[i].length;j++){
                     timeSelector[i][j] = selectedTimesSett.getInt("day"+i+"slot"+j,0);
                 }
             }
         }else{
             // wenn es keine alte Zeitauswahl gibt, wird sie in jedem Slot auf den ersten Eintrag initialisiert
             for(int i=0;i < timeSelector.length; i++){
                 timeSelector[i][0] = 9;
                 timeSelector[i][1] = 13;
                 timeSelector[i][2] = 16;
                 timeSelector[i][3] = 20;
             }
 
         }
 
         // einfaerben der Buttons
         setButtonColors();
 
         // unwaehlbare Buttons ausgrauen und unklickbar machen
         disableButtons();
     }
     /**
      * @brief Alarmzeit updaten
      */
     private void updateAlarm(){
         // gespeicherte Werte (eingestellte Zeiten und letzter Alarm) laden
         SharedPreferences selectedTimesStorage = getSharedPreferences("selectedTimesStorage",0);
         SharedPreferences pastAlarmStorage=getSharedPreferences("pastAlarmsStorage",0);
 
         int nextSlot =0;
         int slotHour = selectedTimesStorage.getInt("day"+(currentDate.get(Calendar.DAY_OF_WEEK)-1)+"slot"+ nextSlot, 0);
 
         // geht durch die gespeicherten Zeiten, bis die erste nach der aktuellen Zeit gefunden wurde
         while(slotHour<= currentDate.get(Calendar.HOUR_OF_DAY)&& nextSlot <4)
         {
             nextSlot++;
             if(nextSlot ==4){
                 // keiner der heutigen Slots -> nimm den ersten von morgen
                slotHour= selectedTimesStorage.getInt("day" + (currentDate.get(Calendar.DAY_OF_WEEK)==7 ?"1" : currentDate.get(Calendar.DAY_OF_WEEK)) + "slot" + 0, 0);
             }else
                 slotHour=selectedTimesStorage.getInt("day"+(currentDate.get(Calendar.DAY_OF_WEEK)-1)+"slot"+ nextSlot, 0);
         }
 
         // neues Calendar-Objekt mit den Alarm-Zeiten
         Calendar nextAlarmTime = new GregorianCalendar();
         nextAlarmTime.set(Calendar.HOUR_OF_DAY, slotHour);
         nextAlarmTime.set(Calendar.MINUTE, 0);
         nextAlarmTime.set(Calendar.SECOND, 0);
         if(nextSlot==4) nextAlarmTime.add(Calendar.DATE,1);
 
         // nochmal sichergehen, dass der Alarm nicht in der Vergangenheit liegt
         if(nextAlarmTime.compareTo(currentDate)==1){
             // (pending)Intent erstellen, der dann von dem Alarm gestartet wird
             PendingIntent alarmIntent = PendingIntent.getActivity(this, pastAlarmStorage.getInt("alarmID",0), new Intent(this, pollActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);
             // Alarm setzen
             AlarmManager alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
             alarmManager.set(AlarmManager.RTC_WAKEUP, nextAlarmTime.getTimeInMillis(), alarmIntent);
         }
     }
 
     /**
      * @brief Bei Betaetigung des OK-Buttons werden ausgewählten Zeiten in ShardedPreferences gespeichert.
      * @param View view
       */
     public void okTime(View view){
 
         // Gewaehlte Zeiten abspeichern
         SharedPreferences settings = getSharedPreferences(SELECTED_TIMES_STORAGE, 0);
         SharedPreferences.Editor editor = settings.edit();
         for(int i=0;i<timeSelector.length;i++){
             for (int j=0;j<timeSelector[i].length;j++){
                 editor.putInt("day"+i+"slot"+j, timeSelector[i][j]);
             }
         }
         editor.commit();
 
         //naechsten Alarm einstellen
         updateAlarm();
 
 
         // Uebergang in den Chillscreen
         if (!this.isTaskRoot())
             this.finish();
         else {
             startActivity(new Intent(this, chillActivity.class));
             this.finish();
         }
 
     }
 
     /**
      * @brief Bei Betätigung eines Tages-Buttons werden Zeitslots zum entsprechenden Tag geladen.
      * Außerdem werden die Buttons umgefärbt.
      * @param View view
       */
     public void daySelect(View view){
         Calendar textDay = (Calendar) currentDate.clone();
 
         // richtigen Tag auswaehlen
         switch(view.getId()){
             case R.id.buttonMon:
                 selectedDay = Calendar.MONDAY;
                 break;
             case R.id.buttonTue:
                 selectedDay = Calendar.TUESDAY;
                 break;
             case R.id.buttonWed:
                 selectedDay = Calendar.WEDNESDAY;
                 break;
             case R.id.buttonThu:
                 selectedDay = Calendar.THURSDAY;
                 break;
             case R.id.buttonFri:
                 selectedDay = Calendar.FRIDAY;
                 break;
             case R.id.buttonSat:
                 selectedDay = Calendar.SATURDAY;
                 break;
             case R.id.buttonSun:
                 selectedDay = Calendar.SUNDAY;
                 break;
             default:
                 break;
         }
 
         // Datum-Text setzen
         TextView date = (TextView) findViewById(R.id.chosenDate);
 
         while(textDay.get(Calendar.DAY_OF_WEEK)!=selectedDay){
             textDay.roll(Calendar.DAY_OF_MONTH,true);
         }
         date.setText(textDay.get(Calendar.DAY_OF_MONTH)+"."+(textDay.get(Calendar.MONTH)+1)+".");
 
         // Farbe und Klickbarkeit der Buttons setzen
         setButtonColors();
         disableButtons();
     }
 
     /**
      * @brief Bei Betätigung eines Zeit(-Slot)-Buttons wird Zeit übernommen und Buttons umgefärbt.
      * @param View view
       */
     public void timeSelect(View view){
 
         // richtige Zeit setzen
         switch(view.getId()){
             case R.id.button09:
                 timeSelector[selectedDay-1][0] = 9;
                 break;
             case R.id.button10:
                 timeSelector[selectedDay-1][0] = 10;
                 break;
             case R.id.button11:
                 timeSelector[selectedDay-1][0] = 11;
                 break;
             case R.id.button12:
                 timeSelector[selectedDay-1][0] = 12;
                 break;
             case R.id.button13:
                 timeSelector[selectedDay-1][1] = 13;
                 break;
             case R.id.button14:
                 timeSelector[selectedDay-1][1] = 14;
                 break;
             case R.id.button15:
                 timeSelector[selectedDay-1][1] = 15;
                 break;
             case R.id.button16:
                 timeSelector[selectedDay-1][2] = 16;
                 break;
             case R.id.button17:
                 timeSelector[selectedDay-1][2] = 17;
                 break;
             case R.id.button18:
                 timeSelector[selectedDay-1][2] = 18;
                 break;
             case R.id.button19:
                 timeSelector[selectedDay-1][2] = 19;
                 break;
             case R.id.button20:
                 timeSelector[selectedDay-1][3] = 20;
                 break;
             case R.id.button21:
                 timeSelector[selectedDay-1][3] = 21;
                 break;
             case R.id.button22:
                 timeSelector[selectedDay-1][3] = 22;
                 break;
             case R.id.button23:
                 timeSelector[selectedDay-1][3] = 23;
                 break;
             default:
                 break;
         }
         // farben neu setzen
         setButtonColors();
     }
     /**
      * @brief Umfärben der Buttons
      */
     public void setButtonColors(){
         // Farben aller Buttons auf rot und die ausgewählten Buttons grün
         for (Button timeButton : timeButtons){
             timeButton.setBackgroundColor(0xffff4444);
         }
         timeButtons[timeSelector[selectedDay - 1][0] - 9].setBackgroundColor(0xff99cc00);
         timeButtons[timeSelector[selectedDay - 1][1] - 9].setBackgroundColor(0xff99cc00);
         timeButtons[timeSelector[selectedDay - 1][2] - 9].setBackgroundColor(0xff99cc00);
         timeButtons[timeSelector[selectedDay - 1][3] - 9].setBackgroundColor(0xff99cc00);
 
         for (Button weekButton : weekButtons) {
             weekButton.setBackgroundColor(0xffff4444);
         }
         weekButtons[selectedDay-1].setBackgroundColor(0xff99cc00);
     }
 
     /**
      * @brief Vergangene Zeitslots sperren. Wenn Alarm in dieser Zeitreihe bereits ausgeführt
      * worden ist, restlichen Zeitslots dieser Zeitreihe sperren.
      */
     private void disableButtons() {
 
         SharedPreferences pastAlarmsSett = getSharedPreferences(PAST_ALARMS_STORAGE,0);
 
         // nur die Buttons fuer heute muessen gesperrt werden
         if(selectedDay == currentDate.get(Calendar.DAY_OF_WEEK)){
             // alle Buttons in der Vergangenheit sperren
             for(int i=0;i<timeButtons.length;i++){
                 if (i+9 <= currentDate.get(Calendar.HOUR_OF_DAY)){
                     timeButtons[i].setTextColor(Color.GRAY);
                     timeButtons[i].setClickable(false);
                 } else {
                     timeButtons[i].setTextColor(Color.WHITE);
                     timeButtons[i].setClickable(true);
                 }
             }
 
             // Alle Buttons eines Slots, der schon einen Alarm gefeuert hat, sperren
             if (pastAlarmsSett.getInt("day",0)==currentDate.get(Calendar.DAY_OF_WEEK)){
                 int disableSlots=0;
                 switch (pastAlarmsSett.getInt("slot",4)){
                     case 0:
                         disableSlots=3;
                         break;
                     case 1:
                         disableSlots=6;
                         break;
                     case 2:
                         disableSlots=10;
                         break;
                     case 3:
                         disableSlots=14;
                         break;
                     default:
                         break;
                 }
                 for (int i=0;i<=disableSlots;i++){
                     timeButtons[i].setTextColor(Color.GRAY);
                     timeButtons[i].setClickable(false);
                 }
             }
         } else {
             for (Button timeButton : timeButtons) {
                 timeButton.setTextColor(Color.WHITE);
                 timeButton.setClickable(true);
             }
         }
     }
 
     /**
      * @brief  Verhalten fuer Back-Button
      * <ul>
      *     <li> Wenn vorher {@link MainActivity} dann wird App geschlossen,</li>
      *     <li> ansonsten Rückkehr zur {@link chillActivity}.</li>
      * </ul>
       */
     @Override
     public void onBackPressed() {
         if (!this.isTaskRoot())
             this.finish();
         else
             super.onBackPressed();
     }
 }
