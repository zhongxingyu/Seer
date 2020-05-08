 package org.es.butler;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnInitListener;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 import org.es.api.AgendaApi;
 import org.es.api.WeatherApi;
 import org.es.api.factory.AgendaApiFactory;
 import org.es.api.factory.WeatherApiFactory;
 import org.es.butler.logic.impl.AgendaLogic;
 import org.es.butler.logic.impl.TimeLogic;
 import org.es.butler.logic.impl.WeatherLogic;
 import org.es.butler.pojo.AgendaEvent;
 import org.es.butler.pojo.WeatherData;
 
 import java.util.List;
 import java.util.Locale;
 
 /**
  * Created by Cyril Leroux on 17/06/13.
  */
 public class MainActivity extends Activity implements OnInitListener, OnClickListener {
 
     private static final String TAG = "ButlerActivity";
     private TextToSpeech mTTS;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         mTTS = new TextToSpeech(getApplicationContext(), this);
 
         ((Button) findViewById(R.id.btn_daily_speech)).setOnClickListener(this);
     }
 
     @Override
     protected void onDestroy() {
         if (mTTS != null) {
             mTTS.stop();
             mTTS.shutdown();
             mTTS = null;
         }
         super.onDestroy();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public void onInit(int status) {
         if (status != TextToSpeech.SUCCESS) {
             Log.e(TAG, "TTS initialization failed");
             return;
         }
 
         int result = TextToSpeech.LANG_MISSING_DATA;
         if (mTTS.isLanguageAvailable(Locale.UK) == TextToSpeech.LANG_AVAILABLE) {
             result = mTTS.setLanguage(Locale.UK);
         } else {
             result = mTTS.setLanguage(Locale.US);
         }
 
         if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
             Log.e(TAG, "This Language is not supported");
         }
     }
 
     @Override
     public void onClick(View v) {
 
         switch (v.getId()) {
             case R.id.btn_daily_speech:
                 dailySpeech(true);
                 break;
 
             default:
                 break;
         }
     }
 
     /**
      * Say the daily speech
      *
      * @param force Forces the speech even if the {@link #cancelDailySpeech()} trigger returns true.
      */
     private void dailySpeech(boolean force) {
         if (cancelDailySpeech() && !force) {
             return;
         }
 
         WeatherApi weatherApi = WeatherApiFactory.getWeatherAPI();
         WeatherData weatherData = weatherApi.checkWeather();
 
         AgendaApi agendaApi = AgendaApiFactory.getAgendaApi();
        List<AgendaEvent> todayEvents = agendaApi.checkTodayEvents();
        List<AgendaEvent> upcomingEvents = agendaApi.checkUpcomingEvent();
 
         Time now = new Time();
         now.setToNow();
 
         TimeLogic time = new TimeLogic(now);
         sayHello(time);
         sayTime(time);
 
         WeatherLogic weather = new WeatherLogic(weatherData);
         sayWeather(weather);
 
         AgendaLogic agendaLogicToday = new AgendaLogic(todayEvents);
         AgendaLogic agendaLogicUpcoming = new AgendaLogic(upcomingEvents);
         sayTodayEvents(agendaLogicToday);
         sayUpcomingEvents(agendaLogicUpcoming);
     }
 
     private boolean cancelDailySpeech() {
         // TODO implement the conditions to cancel daily speech
         return false;
     }
 
     /**
      * Say hello out loud.
      * The spoken text depends on the time of day.
      *
      * @param time The time defining the spoken text.
      */
     private void sayHello(final TimeLogic time) {
 
         String text = null;
 
         if (time.isMorning()) {
             text = getString(R.string.good_morning);
 
         } else if (time.isAfternoon()) {
             text = getString(R.string.good_afternoon);
 
         } else if (time.isEvening()) {
             text = getString(R.string.good_evening);
 
         } else if (time.isNight()) {
             text = getString(R.string.good_night);
 
         } else {
            Log.e(TAG, "Unexpected Time value : " + time.getTime().format("HH:mm:ss"));
         }
 
         mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
     }
 
     /**
      * Say the time out loud.
      *
      * @param time the time to speak out loud.
      */
     private void sayTime(final TimeLogic time) {
         mTTS.setSpeechRate(0.9f);
 
         final String text = time.getPronunciation(getApplicationContext());
         if (text == null || text.isEmpty()) {
             Log.e(TAG, "sayTime(), couldn't get pronunciation.");
         }
         mTTS.speak(text, TextToSpeech.QUEUE_ADD, null);
 
         mTTS.setSpeechRate(1f);
     }
 
     private void sayWeather(final WeatherLogic weather) {
         final String text = weather.getPronunciation(getApplicationContext());
         if (text == null || text.isEmpty()) {
             Log.e(TAG, "sayWeather(), couldn't get pronunciation.");
         }
         mTTS.speak(text, TextToSpeech.QUEUE_ADD, null);
     }
 
     private void sayTodayEvents(final AgendaLogic logic) {
 
         final String text = logic.getPronunciation(getApplicationContext());
         if (text == null || text.isEmpty()) {
             Log.e(TAG, "sayTodayEvents(), couldn't get pronunciation.");
         }
         mTTS.speak(text, TextToSpeech.QUEUE_ADD, null);
     }
 
     private void sayUpcomingEvents(final AgendaLogic logic) {
 
         final String text = logic.getPronunciation(getApplicationContext());
         if (text == null || text.isEmpty()) {
             Log.e(TAG, "sayUpcomingEvents(), couldn't get pronunciation.");
         }
         mTTS.speak(text, TextToSpeech.QUEUE_ADD, null);
 
     }
 }
