 
 package com.bk.sunwidgt.fragment;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import android.app.Fragment;
 import android.content.Context;
 import android.gesture.Gesture;
 import android.gesture.GestureLibraries;
 import android.gesture.GestureLibrary;
 import android.gesture.GestureOverlayView;
 import android.gesture.GestureOverlayView.OnGestureListener;
 import android.gesture.GestureOverlayView.OnGesturePerformedListener;
 import android.gesture.GestureOverlayView.OnGesturingListener;
 import android.gesture.Prediction;
 import android.graphics.Color;
 import android.graphics.Path;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CalendarView;
 import android.widget.ListView;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 import com.bk.sunwidgt.SunWidget;
 import com.bk.sunwidgt.adapter.LocationAdapter;
 import com.bk.sunwidgt.lib.MoonCalculator;
 import com.bk.sunwidgt.lib.SunCalculator;
 
 public class SunFragment extends Fragment {
     private final static String TAG = SunFragment.class.getSimpleName();
     private final static String CALENDAR_TIME = SunFragment.class.getName() + ".CALENDAR_TIME";
     private LocationManager m_locManager;
  /*   private GestureLibrary m_gestureLib;
 
     private Runnable m_nextMonthRunnable = new Runnable() {
 
         @Override
         public void run() {
             changeMonth(true);
         }
 
     };
 
     private Runnable m_prevMonthRunnable = new Runnable() {
 
         @Override
         public void run() {
             changeMonth(false);
         }
 
     };
 */
     
     public static SunFragment newInstance(long calendarTime) {
         final SunFragment fragment = new SunFragment();
         final Bundle b = new Bundle();
         b.putLong(CALENDAR_TIME, calendarTime);
         fragment.setArguments(b);
         return fragment;
     }
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         // mLogger.logMethodName();
         super.onCreate(savedInstanceState);
         m_locManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         Log.i(TAG, "onCreateView");
         final View view = inflater.inflate(com.bk.sunwidgt.R.layout.sun_fragment, container, false);
         final CalendarView calendarView = (CalendarView) view
                 .findViewById(com.bk.sunwidgt.R.id.calendar);
         
         final long calendarTime = getArguments() != null ? getArguments().getLong(CALENDAR_TIME, 0L) : 0L;
         if(calendarTime > 0) {
             calendarView.setDate(calendarTime,false,true);
         }
         
        
         calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
 
             @Override
             public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                 setWeektable();
 
             }
         });
        
         /*
          * calendarView.setOnTouchListener(new View.OnTouchListener() {
          * @Override public boolean onTouch(View view, MotionEvent event) {
          * Log.d(TAG, "calendarView onTouch event=" + event); return false; }
          * });
          */
         /*
          * ((Button) view.findViewById(com.bk.sunwidgt.R.id.next_month))
          * .setOnClickListener(new View.OnClickListener() {
          * @Override public void onClick(View v) { changeMonth(true); } });
          * ((Button) view.findViewById(com.bk.sunwidgt.R.id.prev_month))
          * .setOnClickListener(new View.OnClickListener() {
          * @Override public void onClick(View v) { changeMonth(false); } });
          */
         new Handler().post(new Runnable() {
 
             @Override
             public void run() {
                 setWeektable();
             }
 
         });
         
         return view;
         /*
         final GestureOverlayView gestureOverlayView = new GestureOverlayView(getActivity());
 
         gestureOverlayView.addView(view);
         gestureOverlayView.setGestureVisible(false);
 
         m_gestureLib = GestureLibraries.fromRawResource(getActivity(),
                 com.bk.sunwidgt.R.raw.gestures);
 
         if (!m_gestureLib.load()) {
             Log.w(TAG, "Unable to load Gestures");
         }
         else {
             
             gestureOverlayView.addOnGesturePerformedListener(new OnGesturePerformedListener() {
                 @Override
                 public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
                     ArrayList<Prediction> predictions =
                             m_gestureLib.recognize(gesture); // We want at least
                                                              // one prediction
                     if (predictions.size() > 0) {
                         Prediction prediction = predictions.get(0); // We want
                                                                     // at least
                                                                     // some
                                                                     // confidence
                                                                     // in the
                                                                     // result
                         if (prediction.score > 1.0) {
                             if (prediction.name.startsWith("bknext")) {
                                 new Handler().post(m_nextMonthRunnable);
                             }
                             else if (prediction.name.startsWith("bkprev")) {
                                 new Handler().post(m_prevMonthRunnable);
                             }
                         }
                     }
                 }
             });
 
         }
         return gestureOverlayView;
         */
     }
 
     private void setWeektable() {
         final Location coarseLocation = m_locManager
                 .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
         final double lat = null == coarseLocation ? 25.045792 : coarseLocation.getLatitude();
         final double lng = null == coarseLocation ? 121.453857 : coarseLocation.getLongitude();
 
         Log.d(TAG, "lat=" + lat + " lng=" + lng);
 
         final CalendarView calendarView = (CalendarView) getView().findViewById(
                 com.bk.sunwidgt.R.id.calendar);
         final Calendar cal = Calendar.getInstance();
         cal.setTimeInMillis(calendarView.getDate());
         final Calendar defaultSelectedDay = (Calendar) cal.clone();
 
         cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
         setTableRow(cal, lat, lng, com.bk.sunwidgt.R.id.sun, defaultSelectedDay);
 
         cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
         setTableRow(cal, lat, lng, com.bk.sunwidgt.R.id.mon, defaultSelectedDay);
 
         cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
         setTableRow(cal, lat, lng, com.bk.sunwidgt.R.id.tue, defaultSelectedDay);
 
         cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
         setTableRow(cal, lat, lng, com.bk.sunwidgt.R.id.wed, defaultSelectedDay);
 
         cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
         setTableRow(cal, lat, lng, com.bk.sunwidgt.R.id.thr, defaultSelectedDay);
 
         cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
         setTableRow(cal, lat, lng, com.bk.sunwidgt.R.id.fri, defaultSelectedDay);
 
         cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
         setTableRow(cal, lat, lng, com.bk.sunwidgt.R.id.sat, defaultSelectedDay);
     }
 
     private void setTableRow(Calendar cal, double lat, double lng, int weekid, Calendar selectedDay) {
 
         final TableRow sunriseRow = (TableRow) getView().findViewById(com.bk.sunwidgt.R.id.sunrise);
         final TableRow sunsetRow = (TableRow) getView().findViewById(com.bk.sunwidgt.R.id.sunset);
         final TableRow moonriseRow = (TableRow) getView().findViewById(
                 com.bk.sunwidgt.R.id.moonrise);
         final TableRow moonsetRow = (TableRow) getView().findViewById(com.bk.sunwidgt.R.id.moonset);
 
         final SunCalculator.SunriseSunset sunanswer = SunCalculator.getSunriseSunset(cal, lat, lng,
                 false);
         ((TextView) sunriseRow.findViewById(weekid)).setText(SunWidget.fmtTime
                 .format(sunanswer.sunrise));
         ((TextView) sunsetRow.findViewById(weekid)).setText(SunWidget.fmtTime
                 .format(sunanswer.sunset));
 
         final MoonCalculator.MoonriseMoonset moonanswer = MoonCalculator.getMoonriseMoonset(cal,
                 lat, lng);
 
         if (moonanswer.moonrise != null) {
             ((TextView) moonriseRow.findViewById(weekid)).setText(SunWidget.fmtTime
                     .format(moonanswer.moonrise));
         }
         else {
             ((TextView) moonriseRow.findViewById(weekid)).setText(SunWidget.notimeString);
         }
 
         if (moonanswer.moonset != null) {
 
             ((TextView) moonsetRow.findViewById(weekid)).setText(SunWidget.fmtTime
                     .format(moonanswer.moonset));
         }
         else {
             ((TextView) moonsetRow.findViewById(weekid)).setText(SunWidget.notimeString);
         }
 
         if (cal.equals(selectedDay)) {
             sunriseRow.findViewById(weekid).setBackgroundColor(Color.DKGRAY);
             sunsetRow.findViewById(weekid).setBackgroundColor(Color.DKGRAY);
             moonriseRow.findViewById(weekid).setBackgroundColor(Color.DKGRAY);
             moonsetRow.findViewById(weekid).setBackgroundColor(Color.DKGRAY);
         }
         else {
             sunriseRow.findViewById(weekid).setBackgroundColor(Color.TRANSPARENT);
             sunsetRow.findViewById(weekid).setBackgroundColor(Color.TRANSPARENT);
             moonriseRow.findViewById(weekid).setBackgroundColor(Color.TRANSPARENT);
             moonsetRow.findViewById(weekid).setBackgroundColor(Color.TRANSPARENT);
         }
 
     }
 
     private void changeMonth(boolean isIncreased) {
 
         if (null == getView()) {
             Log.w(TAG, "No view attached to the fragment");
             return;
         }
 
         final CalendarView calendarView = (CalendarView) getView().findViewById(
                 com.bk.sunwidgt.R.id.calendar);
 
         if (null == calendarView) {
             Log.w(TAG, "Unable to find com.bk.sunwidgt.R.id.calendar");
             return;
         }
 
         final Calendar cal = Calendar.getInstance();
         cal.setTimeInMillis(calendarView.getDate());
         cal.add(Calendar.MONDAY, isIncreased ? 1 : -1);
         calendarView.setDate(cal.getTimeInMillis(), true, true);
     }
 
     @Override
     public void onStart() {
         // mLogger.logMethodName();
         super.onStart();
     }
 
     @Override
     public void onStop() {
         // mLogger.logMethodName();
         super.onStop();
     }
 
     @Override
     public void onDestroy() {
         // mLogger.logMethodName();
         super.onDestroy();
     }
 }
