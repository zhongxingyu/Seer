 package me.gods.raintimer;
 
 import java.util.Calendar;
 import java.util.Locale;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.app.Activity;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class TimerFragment extends Fragment {
     private Spinner eventSpinner;
     private ArrayAdapter<String> adapter;
     private static TextView timerText;
     private static Button switcherButton;
 
     private SharedPreferences settings;
 
     private enum State {
         reset,
         running,
         pause,
         stop
     };
 
     private State state;
     private static long startTime;
     private static long offsetTime;
     private TimerThread timerThread;
     private String currentEvent;
     private SQLiteDatabase db;
 
     final static Handler handler = new Handler(){
         public void handleMessage(Message msg){
             switch (msg.what) {
             case 1:
                 long minus = System.currentTimeMillis() - startTime;
                 timerText.setText(millisToTime(offsetTime + minus));
 
                 break;
             default:
                 break;
             }
 
             super.handleMessage(msg);
         }
     };
 
     @Override
     public void onCreate (Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.state = State.reset;
     }
 
     @Override
     public void onResume () {
         super.onResume();
     }
 
     @Override
     public void onPause () {
         super.onPause();
 
         db.close();
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.fragment_timer, container, false);
 
         db = getActivity().openOrCreateDatabase("raintimer.db", Context.MODE_PRIVATE, null);
         db.execSQL("CREATE TABLE IF NOT EXISTS history (_id INTEGER PRIMARY KEY AUTOINCREMENT, event_name VARCHAR, total_time INT, commit_date VARCHAR)");
 
         settings = this.getActivity().getPreferences(Activity.MODE_PRIVATE);
         String eventList = settings.getString(PreferenceFragment.PREFERENCE_KEY, "[]");
 
         JSONArray eventArray = null;
         try {
             eventArray = new JSONArray(eventList);
         } catch (JSONException e) {
             e.printStackTrace();
         }
 
         int eventLength = eventArray.length() + 1;
         final String[] events = new String[eventLength];
 
         events[0] = getString(R.string.default_event);
 
         for (int i = 1; i < eventLength; i++) {
             try {
                 events[i] = eventArray.getString(i - 1);
             } catch (JSONException e) {
                 e.printStackTrace();
             }
         }
 
         eventSpinner = (Spinner)v.findViewById(R.id.event_spinner);
         adapter = new ArrayAdapter<String>(this.getActivity(), R.layout.spinner_text, events);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         eventSpinner.setAdapter(adapter);
 
         eventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 
             @Override
             public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                 currentEvent = events[arg2];
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> arg0) {
             }
         });
 
         timerText = (TextView)v.findViewById(R.id.timer_text);
         timerText.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View arg0) {
                 if (state == State.running) {
                     state = State.pause;
                     try {
                         timerThread.join();
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                     offsetTime += System.currentTimeMillis() - startTime;
                     startTime = System.currentTimeMillis();
                 } else if (state == State.pause) {
                     state = State.running;
                     startTime = System.currentTimeMillis();
                     timerThread = new TimerThread();
                     timerThread.start();
                 }
             }
         });
 
         switcherButton = (Button)v.findViewById(R.id.timer_switcher);
         switcherButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 switch(state) {
                     case reset:
                         state = State.running;
                         startTime = System.currentTimeMillis();
                         timerThread = new TimerThread();
                         timerThread.start();
 
                         break;
                     case running:
                     case pause:
                         state = State.stop;
                         try {
                             timerThread.join();
                         } catch (InterruptedException e) {
                             e.printStackTrace();
                         }
 
                         offsetTime += System.currentTimeMillis() - startTime;
                         startTime = System.currentTimeMillis();
 
                         if (currentEvent != null && currentEvent != getString(R.string.default_event)) {
                             new Builder(getActivity())
                                     .setMessage("Save this?")
                                     .setTitle("Info")
                                     .setPositiveButton("Save", new DialogInterface.OnClickListener() {
 
                                         @Override
                                         public void onClick(DialogInterface arg0, int arg1) {
                                             String sql = "insert into history values(null, ?, ?, ?)";
                                             Calendar now = Calendar.getInstance();
                                            String dateString = String.format("%02d-%02d-%04d", now.get(Calendar.MONTH) + 1, now.get(Calendar.DATE) + 1, now.get(Calendar.YEAR));
                                            Object[] bindArgs = new Object[] {currentEvent, offsetTime, dateString};  
                                             db.execSQL(sql, bindArgs);
                                         }
                                     })
                                     .setNegativeButton("Cancel", null).show();
                         }
 
                         break;
                     case stop:
                         state = State.reset;
                         timerText.setText("0'00\"00");
                         offsetTime = 0;
 
                         break;
                     default:
                         break;
                 }
 
                 updateButton();
             }
         });
 
         updateButton();
 
         return v;
     }
 
     private static String millisToTime(long millis) {
         long millisecond = (millis / 10) % 100;
         long second = (millis / 1000) % 60;
         long minute = (millis / 1000 / 60) % 60;
 
         return String.format(Locale.getDefault(), "%d'%02d\"%02d", minute, second, millisecond);
     }
 
     public void updateButton() {
         switch(state) {
             case reset:
                 switcherButton.setText("Start");
                 break;
             case running:
                 switcherButton.setText("Stop");
                 break;
             case stop:
                 switcherButton.setText("Reset");
                 break;
             default:
                 break;
         }
     }
 
     public class TimerThread extends Thread {      // thread
         @Override
         public void run(){
             while(state == TimerFragment.State.running){
                 try{
                     Thread.sleep(10);
                     Message message = new Message();
                     message.what = 1;
                     handler.sendMessage(message);
                 }catch (Exception e) {
 
                 }
             }
         }
     }
 }
