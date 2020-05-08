 package me.gods.raintimer;
 
 import java.sql.Date;
 import java.text.DateFormatSymbols;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import com.jjoe64.graphview.GraphView;
 import com.jjoe64.graphview.GraphView.GraphViewData;
 import com.jjoe64.graphview.GraphViewSeries;
 import com.jjoe64.graphview.LineGraphView;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.CompoundButton;
 import android.widget.DatePicker;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.Switch;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class HistoryFragment extends Fragment {
     private Spinner eventSpinner;
     private ArrayAdapter<String> adapter;
     private SharedPreferences settings;
     private String currentEvent;
 
     private static Date startDate;
     private static Date endDate;
     private static TextView startDateView;
     private static TextView endDateView;
 
     private Switch modeSwithcer;
     private GraphViewSeries dataSeries;
     private GraphView graphView;
 
     private SQLiteDatabase db;
 
     @Override
     public void onPause () {
         super.onPause();
 
         db.close();
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.fragment_history, container, false);
 
         db = getActivity().openOrCreateDatabase("raintimer.db", Context.MODE_PRIVATE, null);
 
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
 
         eventSpinner = (Spinner)v.findViewById(R.id.event_spinner_history);
         adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, events);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         eventSpinner.setAdapter(adapter);
 
         eventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 
             @Override
             public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                 currentEvent = events[arg2];
                 updateChart();
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> arg0) {
             }
         });
 
         startDate = null;
         endDate = null;
 
         startDateView = (TextView)v.findViewById(R.id.start_date_picker);
         startDateView.setOnClickListener(new View.OnClickListener() {
             
             @Override
             public void onClick(View v) {
                 showDatePickerDialog("Start");
             }
         });
 
         endDateView = (TextView)v.findViewById(R.id.end_date_picker);
         endDateView.setOnClickListener(new View.OnClickListener() {
             
             @Override
             public void onClick(View v) {
                 showDatePickerDialog("End");
             }
         });
 
         modeSwithcer = (Switch)v.findViewById(R.id.mode_switcher);
         modeSwithcer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                 updateChart();
             }
         });
 
         dataSeries = new GraphViewSeries(new GraphViewData[] {
                 new GraphViewData(1, 2.0d)
                 , new GraphViewData(2, 1.5d)
                 , new GraphViewData(3, 2.5d)
                 , new GraphViewData(4, 1.0d)});
         graphView = new LineGraphView(this.getActivity(), "");
         LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
         lp.setMargins(30, 30, 30, 20);
         graphView.setLayoutParams(lp);
         LinearLayout layout = (LinearLayout)v.findViewById(R.id.history_container);
         layout.addView(graphView);
 
         graphView.addSeries(dataSeries);
         graphView.redrawAll();
 
         return v;
     }
 
     public void showDatePickerDialog(String tag) {
         DialogFragment dateFragment = new DatePickerFragment();
         dateFragment.show(getActivity().getFragmentManager(), tag);
     }
 
     public void updateChart() {
         if (currentEvent == null
             || currentEvent.equals(getString(R.string.default_event))
             || startDate == null
             || endDate == null) {
             
         } else if (startDate.compareTo(endDate) > 0 ) {
             Toast.makeText(getActivity().getApplicationContext(), "Invalid Date!", Toast.LENGTH_LONG).show();
         } else {
             graphView.removeSeries(0);
 
             String startDateStr = String.format("%02d-%02d-%04d", startDate.getMonth() + 1, startDate.getDate(), startDate.getYear());
             String endDateStr = String.format("%02d-%02d-%04d", endDate.getMonth() + 1, endDate.getDate(), endDate.getYear());
 
             String rawSQL = "SELECT event_name, commit_date, " + (modeSwithcer.isChecked() ? "avg" : "sum") + "(total_time) as target_time FROM history WHERE event_name = ? AND commit_date >= ? AND commit_date <= ? GROUP BY event_name, commit_date ORDER BY commit_date";
             Cursor c = db.rawQuery(rawSQL, new String[] {currentEvent, startDateStr, endDateStr});
 
             ArrayList<GraphViewData> dataPoints = new ArrayList<GraphViewData>();
             ArrayList<String> pointsDate = new ArrayList<String>();
             double maxY = 0;
             double minY = 24 * 60 * 60 * 1000;
 
             while (c.moveToNext()) {
                 String name = c.getString(c.getColumnIndex("event_name"));
                 int time = c.getInt(c.getColumnIndex("target_time"));
                 String date = c.getString(c.getColumnIndex("commit_date"));
                 Log.i("db", "name=>" + name + ", time=>" + time + ", date=>" + date);
 
                 dataPoints.add(new GraphViewData(dataPoints.size(), time));
                 pointsDate.add(date.substring(0, 5));
 
                 if (time > maxY) {
                     maxY = time;
                 }
                 if (time < minY) {
                     minY = time;
                 }
             }
             c.close();
 
             dataSeries = new GraphViewSeries(dataPoints.toArray(new GraphViewData[0]));
             graphView.addSeries(dataSeries);
             graphView.setHorizontalLabels(pointsDate.toArray(new String[0]));
             graphView.setManualYAxis(true);
             int upBound = (int) (maxY == 0 ? 500 : ((maxY + 1000) - (maxY + 1000) % 1000));
             int bottomBound = (int)((minY == 24 * 60 * 60 * 1000 || minY < 1000) ? 0 : ((minY - 1000) - (minY - 1000) % 1000));
             graphView.setManualYAxisBounds(upBound, bottomBound);
             ArrayList<String> verticalLabels = new ArrayList<String>();
             int split = 7;
 
             for (int i = 0; i < split + 1; i++) {
                 verticalLabels.add(TimerFragment.millisToTime((int)(upBound * (split - i) / split + bottomBound * i / split)));
             }
 
             graphView.setVerticalLabels(verticalLabels.toArray(new String[0]));
             graphView.invalidate();
         }
 
     }
 
     public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             int year, month, day;
 
             if (this.getTag().equals("Start") && startDate == null || this.getTag().equals("End") && endDate == null) {
                 final Calendar c = Calendar.getInstance();
                 year = c.get(Calendar.YEAR);
                 month = c.get(Calendar.MONTH);
                 day = c.get(Calendar.DAY_OF_MONTH);
             } else {
                 Date dateToShow = this.getTag().equals("Start") ? startDate : endDate;
 
                 year = dateToShow.getYear();
                 month = dateToShow.getMonth();
                 day = dateToShow.getDate();
             }
 
             return new DatePickerDialog(getActivity(), this, year, month, day);
         }
 
         @Override
         public void onDateSet(DatePicker arg0, int year, int monthOfYear, int dayOfMonth) {
             if (this.getTag().equals("Start")) {
                 startDate = new Date(year, monthOfYear, dayOfMonth);
                startDateView.setText(String.format("%s-%02d-%04d", new DateFormatSymbols().getMonths()[monthOfYear].substring(0,3), dayOfMonth, year));
             } else {
                 endDate = new Date(year, monthOfYear, dayOfMonth);
                endDateView.setText(String.format("%s-%02d-%04d", new DateFormatSymbols().getMonths()[monthOfYear].substring(0,3), dayOfMonth, year));
             }
             updateChart();
         }
     }
 }
