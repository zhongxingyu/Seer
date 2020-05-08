 package com.storassa.android.scuolasci;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.analytics.tracking.android.MapBuilder;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import dme.forecastiolib.FIOCurrently;
 import dme.forecastiolib.FIODaily;
 import dme.forecastiolib.FIODataPoint;
 import dme.forecastiolib.ForecastIO;
 
 public class MeteoFragment extends Fragment {
 
    private FIODataPoint[] dataPoint;
    private ArrayList<MeteoItem> meteoItems;
    private ArrayAdapter<MeteoItem> adapter;
    private StartingActivity parentActivity;
    FIODaily daily;
    private int counter = 0;
 
    @Override
    public void onAttach(Activity activity) {
       super.onAttach(activity);
       parentActivity = (StartingActivity) activity;
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
       // Inflate the layout for this fragment
       View result = inflater.inflate(R.layout.meteo_fragment, container, false);
       final ListView meteoListView = (ListView) result
             .findViewById(R.id.meteo_list);
       meteoListView
             .setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                      int position, long id) {
                   // only the first two days can be expanded in hourly
                   // forecast
                   if (id < 2) {
 
                      // Google Analytics tracking
                      trackAction();
 
                      Intent myIntent = new Intent(getActivity(),
                            MeteoActivity.class);
                      Calendar c = Calendar.getInstance();
                      myIntent.putExtra("current_hour", c
                            .get(Calendar.HOUR_OF_DAY));
                      myIntent.putExtra("day", (int) id);
                      myIntent.putExtra("customer", parentActivity.customerName);
                      getActivity().startActivity(myIntent);
                   } else if (id == 2) {
 
                      // Google Analytics tracking
                      trackAction();
 
                      Intent myIntent = new Intent(getActivity(),
                            MeteoActivity.class);
                      Calendar c = Calendar.getInstance();
                      myIntent.putExtra("current_hour", c
                            .get(Calendar.HOUR_OF_DAY));
                      myIntent.putExtra("day", (int) id);
                      myIntent.putExtra("customer", parentActivity.customerName);
                      getActivity().startActivity(myIntent);
 
                   } else {
                      AlertDialog.Builder builder = new AlertDialog.Builder(
                            getActivity());
                      builder.setMessage(R.string.meteo_list_restriction)
                            .setTitle(R.string.warning);
 
                      builder.setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int id) {
                                  ;
                               }
                            });
 
                      AlertDialog dialog = builder.create();
                      dialog.show();
                   }
                }
             });
 
       // retrieve the saved meteo items, if available
       meteoItems = new ArrayList<MeteoItem>();
 
       // initialize the dataPoint array with the max number of forecast
       // plus today
       dataPoint = new FIODataPoint[MAX_FORECAST_DAYS + 1];
 
       // get the meteo information in a different thread
       ExecutorService exec = Executors.newCachedThreadPool();
       exec.execute(new Runnable() {
 
          @Override
          public void run() {
             try {
                String forecastIoKey = getResources().getString(
                      R.string.forecastio_api_key);
                String limoneLatitude = getResources().getString(
                      R.string.limone_latitude);
                String limoneLongitude = getResources().getString(
                      R.string.limone_longitude);
 
                ForecastIO fio = new ForecastIO(forecastIoKey);
                fio.setUnits(ForecastIO.UNITS_SI);
                fio.setExcludeURL("hourly,minutely");
                fio.getForecast(limoneLatitude, limoneLongitude);
                daily = new FIODaily(fio);
 
             } catch (Exception e) {
                // if there are problems print the stack and warn the user
                e.printStackTrace();
                parentActivity.runOnUiThread(new Runnable() {
 
                   @Override
                   public void run() {
                      CommonHelper.exitMessage(R.string.http_issue,
                            R.string.http_issue_dialog_title, parentActivity);
                   }
                });
             }
          }
       });
 
       // wait for the http response or exit after 10s
       Timer timer = new Timer();
       timer.schedule(new TimerTask() {
 
          @Override
          public void run() {
             if (daily != null) {
                // initialize the summary string (plus one for today)
                String[] meteoIconString = new String[MAX_FORECAST_DAYS];
 
                // get the meteo icon for each day
                for (int i = 0; i < MAX_FORECAST_DAYS; i++)
                   meteoIconString[i] = daily.getDay(i).icon()
                         .replace('\"', ' ').trim();
 
                // get the meteo data for next days
                for (int i = 0; i < MAX_FORECAST_DAYS; i++) {
                   dataPoint[i] = daily.getDay(i);
                   meteoItems.add(CommonHelper.getMeteoItemFromDataPoint(
                         dataPoint[i], true));
                }
 
                // get the meteo array adapter and set it to the listview
                int resId = R.layout.meteo_list;
                adapter = new MeteoArrayAdapter(parentActivity, resId,
                      meteoItems, true, 0, 0);
                parentActivity.runOnUiThread(new Runnable() {
                   public void run() {
                      meteoListView.setAdapter(adapter);
                   }
                });
 
                // cancel the waiting thread
                this.cancel();
 
             } else if (counter < WAITING_TICKS) {
                counter++;
 
             } else {
                CommonHelper.exitMessage(R.string.http_issue_dialog_title,
                      R.string.http_issue, getActivity());
             }
 
          }
       }, 0, REPETITION_TIME);
 
       return result;
    }
 
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
       super.onSaveInstanceState(savedInstanceState);
 
    }
 
    // private static final String LIMONE_LATITUDE = "44.2013202";
    // private static final String LIMONE_LONGITUDE = "7.576090300000033";
    // private static final String METEO_API_FIO_KEY =
    // "66d2edf03dbf0185e0cb48f1a23a29ed";
    // TODO put the website for snow reports
 
    private void trackAction() {
       EasyTracker easyTracker = EasyTracker.getInstance(getActivity());
 
       // MapBuilder.createEvent().build() returns a Map of event
       // fields and values that are set and sent with the hit.
       easyTracker.send(MapBuilder.createEvent("ui_action", // category (req)
             "item_selected", // action (required)
             "daily_meteo", // label
             null) // value
             .build());
    }
 
    private static final int MAX_FORECAST_DAYS = 7;
    private static final int REPETITION_TIME = 1000;
    private static final int WAITING_TICKS = 40;
 
 }
