 package com.jegumi.irishrail.ui;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import roboguice.inject.InjectView;
 import android.annotation.SuppressLint;
 import android.app.ProgressDialog;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.RoboSherlockFragmentActivity;
 import com.google.inject.Inject;
 import com.jegumi.irishrail.IrishRailPreferences;
 import com.jegumi.irishrail.R;
 import com.jegumi.irishrail.adapters.StationsDataArrayAdapter;
 import com.jegumi.irishrail.api.Api;
 import com.jegumi.irishrail.db.DataHelper;
 import com.jegumi.irishrail.db.DbHelper;
 import com.jegumi.irishrail.model.Station;
 import com.jegumi.irishrail.model.StationData;
 import com.jegumi.irishrail.model.TrainMovement;
 import com.jegumi.irishrail.utils.Utils;
 import com.jegumi.irishrail.xml.ListStationDataParser;
 import com.jegumi.irishrail.xml.ListStationsParser;
 import com.jegumi.irishrail.xml.ListTrainsMovementsParser;
 
 public class MainActivity extends RoboSherlockFragmentActivity {
 
     private static String TAG = MainActivity.class.getName();
 
     @InjectView(R.id.from_spinner)
     private Spinner fromSpinner;
     @InjectView(R.id.to_spinner)
     private Spinner toSpinner;
     @InjectView(R.id.stations_data_listview)
     private ListView stationListView;
     @InjectView(R.id.refresh_button)
     private Button refreshbutton;
     @InjectView(R.id.back_direction_button)
     private ImageButton returnButton;
     @InjectView (R.id.no_trains_text_view)
     private TextView noTrainsTextView;
 
     @Inject
     private DataHelper dataHelper;
     @Inject
     private IrishRailPreferences preferences;
     @Inject
     private Api api;
 
     private StationsDataArrayAdapter adapter;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         initFields();
     }
 
     @Override
     protected void onStop() {
         preferences.setLastFrom(fromSpinner.getSelectedItemPosition());
         preferences.setLastTo(toSpinner.getSelectedItemPosition());
         super.onStop();
     }
 
     private void initFields() {
         if (preferences.isDatabaseInit()) {
             fillSpinners();
         } else {
             new GetAllStationsAsyncTask().execute();
         }
 
         fromSpinner.setSelection(preferences.getLastFrom());
         toSpinner.setSelection(preferences.getLastTo());
         refreshbutton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 new GetStationsTrainsAsyncTask().execute();
             }
         });
         returnButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 int from = fromSpinner.getSelectedItemPosition();
                 int to = toSpinner.getSelectedItemPosition();
                 toSpinner.setSelection(from);
                 fromSpinner.setSelection(to);
             }
         });
     }
 
     private void fillSpinners() {
         fillStationsSpinner(fromSpinner);
         fillStationsSpinner(toSpinner);
     }
 
     public void fillStationsSpinner(Spinner spinner) {
         Cursor cursor = dataHelper.getStationsCursor(null);
         startManagingCursor(cursor);
         String[] columns = new String[] { DbHelper.STATION_DESC };
         int[] to = new int[] { android.R.id.text1 };
 
         SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor,
                columns, to);
 
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinner.setAdapter(adapter);
     }
 
     private class GetAllStationsAsyncTask extends AsyncTask<String, Void, Integer> {
         private ProgressDialog dialog = new ProgressDialog(MainActivity.this);
         private List<Station> stations = null;
 
         @Override
         protected void onPreExecute() {
             dialog.setMessage(getString(R.string.loading));
             dialog.show();
         }
 
         @Override
         protected Integer doInBackground(String... params) {
             Api api = new Api();
             int status = Api.ERROR;
             try {
                 String result = api.getAllStations();
                 stations = ListStationsParser.parse(result);
             } catch (Exception e) {
                 Log.e(TAG, "GetAllStationsAsyncTask" + e);
             }
             return status;
         }
 
         @Override
         protected void onPostExecute(Integer result) {
             for (Station station : stations) {
                 dataHelper.insertStation(station);
             }
             fillSpinners();
             preferences.setDatabaseInit(true);
             dialog.dismiss();
         }
     }
 
     private class GetStationsTrainsAsyncTask extends AsyncTask<String, Void, Integer> {
         private ProgressDialog dialog = new ProgressDialog(MainActivity.this);
         private List<StationData> originStationsData = null;
         private List<StationData> toDestinationsStationsData = new ArrayList<StationData>();
         private String origin;
         private String destination;
 
         @Override
         protected void onPreExecute() {
             dialog.setMessage(getString(R.string.loading));
             dialog.show();
             origin = dataHelper.getStationNameById(fromSpinner.getSelectedItemId());
             destination = dataHelper.getStationNameById(toSpinner.getSelectedItemId());
         }
 
         @Override
         protected Integer doInBackground(String... params) {
             int status = Api.ERROR;
             try {
                 String originStationData = api.getStationsTrains(origin);
                 originStationsData = ListStationDataParser.parse(originStationData);
 
                 String strDate = Utils.getCurrentDate();
                 for (StationData stationData : originStationsData) {
                     String resultMov = api.getTrainMovements(stationData.getTrainCode(), strDate);
                     List<TrainMovement> trainMovements = ListTrainsMovementsParser.parse(resultMov);
                     for (TrainMovement trainMovement : trainMovements) {
                         if (isCorrectDirection(stationData, trainMovement)) {
                             stationData.setTrainMovement(trainMovement);
                             toDestinationsStationsData.add(stationData);
                         }
                     }
                 }
                 status = Api.OK;
             } catch (Exception e) {
                 Log.e(TAG, "GetStationsTrainsAsyncTask" + e);
             }
             return status;
         }
 
         @Override
         protected void onPostExecute(Integer result) {
             if (result != Api.ERROR) {
                 adapter = new StationsDataArrayAdapter(MainActivity.this, toDestinationsStationsData);
                 stationListView.setAdapter(adapter);
             }
 
             handleEmptyListMessage();
             dialog.dismiss();
         }
 
         private void handleEmptyListMessage() {
             if (stationListView.getCount() == 0) {
                 noTrainsTextView.setText(getString(R.string.no_trains, origin, destination));
                 noTrainsTextView.setVisibility(View.VISIBLE);
             } else {
                 noTrainsTextView.setVisibility(View.GONE);
             }
         }
 
         private boolean isCorrectDirection(StationData stationData, TrainMovement trainMovement) {
             Calendar originArrive = Utils.stringToTimeCalendar(stationData.getArrival());
             Calendar destinationArrive = Utils.stringToTimeCalendar(trainMovement.getArrival());
             return trainMovement.getLocation().equals(destination) && originArrive.compareTo(destinationArrive) <= 0;
         }
     }
 }
