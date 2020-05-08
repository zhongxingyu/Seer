 package com.storassa.android.scuolasci;
 
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.analytics.tracking.android.MapBuilder;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import dme.forecastiolib.FIODataPoint;
 import dme.forecastiolib.FIOHourly;
 import dme.forecastiolib.ForecastIO;
 
 public class MeteoActivity extends Activity {
 
    private FIODataPoint[] dataPoint;
    private ArrayList<MeteoItem> meteoItems;
    private ArrayAdapter<MeteoItem> adapter;
    FIOHourly hourly = null;
    int counter = 0;
    String customerName;
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       // Inflate the menu; this adds items to the action bar if it is present.
       getMenuInflater().inflate(R.menu.activity_meteo, menu);
 
       return true;
 
    }
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_meteo);
 
       Intent intent = getIntent();
       customerName = intent.getStringExtra("customer");
       final int day = intent.getExtras().getInt("day");
       final int currentHour = intent.getExtras().getInt("current_hour");
 
       final ListView listView = (ListView) findViewById(R.id.hourly_meteo_list);
       listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
          @Override
          public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
             EasyTracker easyTracker = EasyTracker
                   .getInstance(MeteoActivity.this);
 
             // MapBuilder.createEvent().build() returns a Map of event
             // fields and values that are set and sent with the hit.
            
             easyTracker.send(MapBuilder.createEvent("ui_action", // category
                   "item_selected", // action
                  "meteo_hourly",  // label
                   null) // value
                   .build());
             Intent intent = new Intent(MeteoActivity.this,
                   BookingActivity.class);
             if (day == 0)
                intent.putExtra("hour", currentHour + position);
             else
                intent.putExtra("hour", position);
             intent.putExtra("customer", customerName);
             intent.putExtra("day", day);
             startActivity(intent);
          }
       });
 
       meteoItems = new ArrayList<MeteoItem>();
 
       dataPoint = new FIODataPoint[MAX_HOURS];
 
       // get the hourly meteo in a new thread
       ExecutorService exec = Executors.newCachedThreadPool();
       exec.execute(new Runnable() {
 
          @Override
          public void run() {
 
             String forecastIoKey = getResources().getString(
                   R.string.forecastio_api_key);
             String limoneLatitude = getResources().getString(
                   R.string.limone_latitude);
             String limoneLongitude = getResources().getString(
                   R.string.limone_longitude);
 
             try {
                ForecastIO fio = new ForecastIO(forecastIoKey);
                fio.setUnits(ForecastIO.UNITS_SI);
                fio.setExcludeURL("currently,minutely,daily");
                fio.getForecast(limoneLatitude, limoneLongitude);
                hourly = new FIOHourly(fio);
             } catch (Exception e) {
                e.printStackTrace();
             }
          }
       });
 
       // wait for the http response or exit after 5s
       final Timer timer = new Timer();
       timer.schedule(new TimerTask() {
 
          @Override
          public void run() {
             // if the data are retrieved
             if (hourly != null) {
                String[] meteoIconString = new String[MAX_HOURS];
 
                // get the number of hours to be retrieved; in case of today,
                // shows only the remaining hours to midnight
                int startHours, endHours;
                if (day == 0) {
                   startHours = 0;
                   endHours = 24 - currentHour;
                } else if (day == 1) {
                   startHours = 24 - currentHour + 1;
                   endHours = 48 - currentHour + 1;
                } else {
                   startHours = 48 - currentHour;
                   endHours = 48;
                }
 
                // get the icons texts (sunny, cloud, etc...) for the hours
                for (int i = startHours; i < endHours; i++)
                   meteoIconString[i - startHours] = hourly.getHour(i).icon()
                         .replace('\"', ' ').trim();
 
                // get the data points (temp. etc...)
                for (int i = startHours; i < endHours; i++) {
                   dataPoint[i - startHours] = hourly.getHour(i);
                   meteoItems.add(CommonHelper.getMeteoItemFromDataPoint(
                         dataPoint[i - startHours], false));
 
                }
 
                // set the meteoItems as the adapter for the ListView
                int resId = R.layout.meteo_list;
                adapter = new MeteoArrayAdapter(MeteoActivity.this, resId,
                      meteoItems, false, day, currentHour);
                final ListView meteoListView = (ListView) findViewById(R.id.hourly_meteo_list);
 
                runOnUiThread(new Runnable() {
                   public void run() {
                      meteoListView.setAdapter(adapter);
                   }
                });
                timer.cancel();
 
                // else, if the deadline is not passed, increment the counter
             } else if (counter < WAITING_TICKS) {
                counter++;
 
                // if the deadline is passed there are issues with the Internet
                // connection. Warn the user and exit
             } else {
                runOnUiThread(new Runnable() {
 
                   @Override
                   public void run() {
                      CommonHelper
                            .exitMessage(R.string.http_issue,
                                  R.string.http_issue_dialog_title,
                                  MeteoActivity.this);
                   }
                });
                timer.cancel();
             }
          }
       }, 0, REPETITION_TIME);
 
    }
 
    private static final int REPETITION_TIME = 1000;
    private final static int MAX_HOURS = 24;
    private final static int WAITING_TICKS = 10;
 }
