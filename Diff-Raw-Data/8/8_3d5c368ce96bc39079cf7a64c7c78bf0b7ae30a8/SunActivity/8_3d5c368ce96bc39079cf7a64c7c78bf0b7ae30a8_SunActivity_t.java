 package com.bk.sunwidgt.activity;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import com.bk.sunwidgt.SunWidget;
 import com.bk.sunwidgt.lib.MoonCalculator;
 import com.bk.sunwidgt.lib.SunCalculator;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.Window;
 import android.widget.CalendarView;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class SunActivity extends Activity{
     private final static String TAG = SunActivity.class.getSimpleName();
     
     private LocationManager m_locManager;
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         Log.d(TAG, "onCreate");
         super.onCreate(savedInstanceState);
         
         m_locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         
         requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
         setContentView(com.bk.sunwidgt.R.layout.sun_activity);
         
         final CalendarView calendarView = (CalendarView) findViewById(com.bk.sunwidgt.R.id.calendar);
         calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
             
             @Override
             public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                 setWeektable();
                 
             }
         });
         
         setWeektable();
     }
     
     public void onNextMonth(View view) {
         changeMonth(true);
     }
     
     public void onPreviousMonth(View view) {
         changeMonth(false);
     }
     
     private void setWeektable() {
         final Location coarseLocation = m_locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
         final double lat = null == coarseLocation ? 25.045792 : coarseLocation.getLatitude();
         final double lng = null == coarseLocation ? 121.453857 : coarseLocation.getLongitude();
         
         Log.d(TAG, "lat=" + lat + " lng=" + lng);
         
         final CalendarView calendarView = (CalendarView) findViewById(com.bk.sunwidgt.R.id.calendar);
         final Calendar cal = Calendar.getInstance();
         cal.setTimeInMillis(calendarView.getDate());
         final int defaultSelectedWeek = cal.get(Calendar.DAY_OF_WEEK);
         
         cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
         setTableRow(cal,lat,lng,com.bk.sunwidgt.R.id.sun,defaultSelectedWeek);
         
         cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
         setTableRow(cal,lat,lng,com.bk.sunwidgt.R.id.mon,defaultSelectedWeek);
 
         cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
         setTableRow(cal,lat,lng,com.bk.sunwidgt.R.id.tue,defaultSelectedWeek);
         
         cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
         setTableRow(cal,lat,lng,com.bk.sunwidgt.R.id.wed,defaultSelectedWeek);
         
         cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
         setTableRow(cal,lat,lng,com.bk.sunwidgt.R.id.thr,defaultSelectedWeek);
         
         cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
         setTableRow(cal,lat,lng,com.bk.sunwidgt.R.id.fri,defaultSelectedWeek);
         
         cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
         setTableRow(cal,lat,lng,com.bk.sunwidgt.R.id.sat,defaultSelectedWeek);
     }
     
     private void setTableRow(Calendar cal,double lat,double lng,int weekid,int selectedWeek) {
         
         final TableRow sunriseRow = (TableRow) findViewById(com.bk.sunwidgt.R.id.sunrise);
         final TableRow sunsetRow = (TableRow) findViewById(com.bk.sunwidgt.R.id.sunset);
         final TableRow moonriseRow = (TableRow) findViewById(com.bk.sunwidgt.R.id.moonrise);
         final TableRow moonsetRow = (TableRow) findViewById(com.bk.sunwidgt.R.id.moonset);
 
         final SunCalculator.SunriseSunset sunanswer = SunCalculator.getSunriseSunset(cal, lat, lng, false);        
         ((TextView) sunriseRow.findViewById(weekid)).setText(SunWidget.fmtTime.format(sunanswer.sunrise));
         ((TextView) sunsetRow.findViewById(weekid)).setText(SunWidget.fmtTime.format(sunanswer.sunset));
         
         final MoonCalculator.MoonriseMoonset moonanswer = MoonCalculator.getMoonriseMoonset(cal, lat, lng);
         
         if(moonanswer.moonrise != null) {
             ((TextView) moonriseRow.findViewById(weekid)).setText(SunWidget.fmtTime.format(moonanswer.moonrise));
         }
         else {
             ((TextView) moonriseRow.findViewById(weekid)).setText(SunWidget.notimeString);
         }
         
         if(moonanswer.moonset != null) {
         
             ((TextView) moonsetRow.findViewById(weekid)).setText(SunWidget.fmtTime.format(moonanswer.moonset));
         }
         else {
             ((TextView) moonsetRow.findViewById(weekid)).setText(SunWidget.notimeString);
         }
         
     }
     
     private void changeMonth(boolean isIncreased) {
         final CalendarView calendarView = (CalendarView) findViewById(com.bk.sunwidgt.R.id.calendar);
         
         final Calendar cal = Calendar.getInstance();
         cal.setTimeInMillis(calendarView.getDate());
         cal.add(Calendar.MONDAY, isIncreased ? 1 : -1);
         calendarView.setDate(cal.getTimeInMillis(), true, true);
     }
     
     @Override
     protected void onStart() {
         //mLogger.logMethodName();
         super.onStart();
     }
 
     @Override
     protected void onResume() {
         //mLogger.logMethodName();
         super.onResume();
 //        setResult(RESULT_OK);
     }
 
     @Override
     protected void onPause() {
         //mLogger.logMethodName();
         super.onPause();
     }
 
     @Override
     protected void onStop() {
         //mLogger.logMethodName();
         super.onStop();
 //        setResult(RESULT_OK);
         //finish();
         //mLogger.info("Finish activity by onStop");
     }
     @Override
     protected void onDestroy() {
         //mLogger.logMethodName();
         super.onDestroy();
     }
     
     @Override    
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
     }   
 }
