 package com.pwr.zpi;
 
 import java.util.LinkedList;
 
 import android.content.Intent;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.PolylineOptions;
 import com.pwr.zpi.database.Database;
 import com.pwr.zpi.database.entity.SingleRun;
 import com.pwr.zpi.utils.ChartDataHelperContainter;
 import com.pwr.zpi.utils.LineChartDataEvaluator;
 import com.pwr.zpi.utils.Pair;
 import com.pwr.zpi.utils.TimeFormatter;
 import com.pwr.zpi.views.TopBar;
 
 public class SingleRunHistoryActivity extends FragmentActivity implements OnClickListener, OnCheckedChangeListener {
 	
 	protected static final String RUN_ID = "runID";
 	
 	//TODO Debuging REMOVE
 	public static long time1;
 	public static long time2;
 	public static long time3;
 	
 	private GoogleMap mMap;
 	private LatLngBounds.Builder boundsBuilder;
 	
 	private LinkedList<Marker> allMarkers;
 	
 	private TextView distanceTextView;
 	private TextView timeTextView;
 	private TextView avgPaceTextView;
 	private TextView avgSpeedTextView;
 	private Button chartButton;
 	private Button splitsButton;
 	private ProgressBar progressBar;
 	private TextView runNameTextView;
 	private CheckBox annotationsCheckBox;
 	private RelativeLayout leftButton;
 	private RelativeLayout annotationRelativeLayout;
 	
 	private SingleRun run;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.single_run_history_activity);
 		
 		//	loadData(getIntent().getLongExtra(HistoryActivity.ID_TAG, 0));
 		initFields();
 		addListeners();
 		showData();
 		new GetRunFromDB().execute(getIntent().getLongExtra(HistoryActivity.ID_TAG, 0));
 	}
 	
 	private void mapCenter() {
 		
 		LinkedList<LinkedList<Pair<Location, Long>>> traceWithTime = run.getTraceWithTime();
 		if (traceWithTime != null)
 		{
 			boundsBuilder = new LatLngBounds.Builder();
 			double lastDistance = 0;
 			double newDistance = 0;
 			for (LinkedList<Pair<Location, Long>> singleTrace : traceWithTime) {
 				PolylineOptions polyLine = new PolylineOptions();
 				polyLine.color(ActivityActivity.TRACE_COLOR);
 				polyLine.width(ActivityActivity.TRACE_THICKNESS);
 				Location lastLocation = null;
 				for (Pair<Location, Long> singlePoint : singleTrace) {
 					Location location = singlePoint.first;
 					LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
 					polyLine.add(latLng);
 					boundsBuilder.include(latLng);
 					if (lastLocation != null) {
 						newDistance += location.distanceTo(lastLocation);
 						int showDistance = (int) (newDistance / 1000);
 						if (showDistance - (int) (lastDistance / 1000) > 0) {
 							addMarker(location, showDistance);
 						}
 						
 					}
 					lastDistance = newDistance;
 					
 					lastLocation = location;
 					
 				}
				
 			}
 			
 			mMap.setOnCameraChangeListener(new OnCameraChangeListener() {
 				
 				@Override
 				public void onCameraChange(CameraPosition arg0) {
 					// Move camera.
 					LatLngBounds bounds = boundsBuilder.build();
 					mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
 					// Remove listener to prevent position reset on camera move.
 					mMap.setOnCameraChangeListener(null);
 				}
 			});
 			LinkedList<LinkedList<Pair<Location, Long>>> trace = run.getTraceWithTime();
 			
 			if (!trace.isEmpty()) {
 				Location start = trace.getFirst().getFirst().first;
 				Location finish = trace.getLast().getLast().first;
 				addStartAndFinish(new LatLng(start.getLatitude(), start.getLongitude()),
 					new LatLng(finish.getLatitude(),
 						finish.getLongitude()));
 			}
 			progressBar.setVisibility(View.GONE);
 			LatLngBounds bounds = boundsBuilder.build();
 			mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
 		}
 	}
 	
 	private void addStartAndFinish(LatLng startPos, LatLng finishPos) {
 		
 		allMarkers.add(mMap.addMarker(new MarkerOptions().position(startPos).icon(
 			BitmapDescriptorFactory.fromResource(R.drawable.start_pin))));
 		allMarkers.add(mMap.addMarker(new MarkerOptions().position(finishPos).icon(
 			BitmapDescriptorFactory.fromResource(R.drawable.stop_pin))));;
 	}
 	
 	private void addMarker(Location location, int distance) {
 		
 		Marker marker = mMap.addMarker(new MarkerOptions()
 			.position(new LatLng(location.getLatitude(), location.getLongitude())).title(distance + "km")
 			.icon(BitmapDescriptorFactory.fromResource(R.drawable.distance_pin)));
 		marker.showInfoWindow();
 		allMarkers.add(marker);
 	}
 	
 	private void initFields() {
 		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
 		mMap = mapFragment.getMap();
 		chartButton = (Button) findViewById(R.id.buttonCharts);
 		splitsButton = (Button) findViewById(R.id.buttonSplits);
 		distanceTextView = (TextView) findViewById(R.id.TextView1History);
 		timeTextView = (TextView) findViewById(R.id.TextView2History);
 		avgPaceTextView = (TextView) findViewById(R.id.TextView3History);
 		avgSpeedTextView = (TextView) findViewById(R.id.TextView4History);
 		progressBar = (ProgressBar) findViewById(R.id.progressBarSingleRunHistory);
 		annotationsCheckBox = (CheckBox) findViewById(R.id.checkBoxSingleRunAnnotations);
 		runNameTextView = (TextView) findViewById(R.id.textViewSingleRunName);
 		annotationRelativeLayout = (RelativeLayout) findViewById(R.id.relativeLayoutSingleRunAnnotations);
 		annotationsCheckBox.setChecked(true);
 		allMarkers = new LinkedList<Marker>();
 		
 		TopBar topBar = (TopBar) findViewById(R.id.topBarSingleRun);
 		leftButton = topBar.getLeftButton();
 		
 	}
 	
 	private void showData() {
 		
 		Intent intent = getIntent();
 		double distance = intent.getDoubleExtra(HistoryActivity.DISTANCE_TAG, 0);
 		long time = intent.getLongExtra(HistoryActivity.TIME_TAG, 0);
 		// show distance
 		distanceTextView.setText(String.format("%.3f", distance / 1000));
 		
 		// show time
 		timeTextView.setText(TimeFormatter.formatTimeHHMMSS(time));
 		
 		//show avg pace
 		double speed = distance / 1000 / time * 1000 * 60 * 60;
 		double pace = 1 / speed * 60;
 		avgPaceTextView.setText(TimeFormatter.formatTimeMMSSorHHMMSS(pace));
 		
 		//show avgSpeed
 		avgSpeedTextView.setText(String.format("%.2f", speed));
 		
 		runNameTextView.setText(intent.getStringExtra(HistoryActivity.NAME_TAG));
 		
 	}
 	
 	private void addListeners() {
 		chartButton.setOnClickListener(this);
 		splitsButton.setOnClickListener(this);
 		annotationsCheckBox.setOnCheckedChangeListener(this);
 		leftButton.setOnClickListener(this);
 		annotationRelativeLayout.setOnClickListener(this);
 	}
 	
 	@Override
 	public void onBackPressed() {
 		super.onBackPressed();
 		overridePendingTransition(R.anim.in_right_anim, R.anim.out_right_anim);
 		
 	}
 	
 	@Override
 	public void onClick(View view) {
 		if (run != null) {
 			if (view == chartButton) {
 				time1 = System.currentTimeMillis();
 				
 				Intent i = new Intent(SingleRunHistoryActivity.this, ChartActivity.class);
 				ChartDataHelperContainter container = LineChartDataEvaluator.evaluateDate(run);
 				i.putExtra(ChartActivity.CHART_DATA_KEY, container);
 				
 				startActivity(i);
 			}
 			else if (view == splitsButton) {
 				Intent i = new Intent(SingleRunHistoryActivity.this, SplitsActivity.class);
 				
 				i.putExtra(RUN_ID, run.getRunID());
 				startActivity(i);
 				
 			}
 			else if (view == leftButton)
 			{
 				finish();
 				overridePendingTransition(R.anim.in_right_anim, R.anim.out_right_anim);
 			}
 			else if (view == annotationRelativeLayout)
 			{
 				annotationsCheckBox.setChecked(!annotationsCheckBox.isChecked());
 			}
 		}
 	}
 	
 	private class GetRunFromDB extends AsyncTask<Long, Void, SingleRun> {
 		@Override
 		protected SingleRun doInBackground(Long... id) {
 			Database db = new Database(SingleRunHistoryActivity.this);
 			SingleRun run = db.getRun(id[0]);
 			return run;
 		}
 		
 		@Override
 		protected void onPostExecute(SingleRun run) {
 			
 			SingleRunHistoryActivity.this.run = run;
 			
 			mapCenter();
 			splitsButton.setFocusable(true);
 			chartButton.setFocusable(true);
 			splitsButton.setTextColor(getResources().getColor(R.color.single_run_text_light_blue));
 			chartButton.setTextColor(getResources().getColor(R.color.single_run_text_light_blue));
 		}
 		
 	}
 	
 	private void setMarkersVisibile(boolean areVisible) {
 		for (Marker m : allMarkers) {
 			m.setVisible(areVisible);
 		}
 	}
 	
 	@Override
 	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 		setMarkersVisibile(isChecked);
 	}
 }
